package Plugin.provider.xml;

import Plugin.provider.MapleData;
import Plugin.provider.MapleDataEntity;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataType;
import Plugin.provider.wz.WzFileMapleData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class XMLDomMapleData implements MapleData {

    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static final Map<String, Object> dataCache = new HashMap<>(); // 新增的快取
    private Node node;
    private File imageDataDir;

    private XMLDomMapleData(Node node, File file) {
        this.node = node;
        this.imageDataDir = file;
    }

    public XMLDomMapleData(FileInputStream fis, File imageDataDir) {
        try {
            this.node = documentBuilderFactory.newDocumentBuilder().parse(fis).getFirstChild();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        this.imageDataDir = imageDataDir;
    }

    public MapleData getChildByPath(String path) {
        Map<String, MapleData> hotfix = MapleDataProviderFactory.getHotfixDatas();
        if (hotfix.isEmpty()) {
            String thisPath = getPath() + "/" + path;
            for (Map.Entry<String, MapleData> entry : hotfix.entrySet()) {
                if (entry.getKey().startsWith("-") && thisPath.equalsIgnoreCase(entry.getKey().substring(1))) {
                    return null;
                } else if (thisPath.equalsIgnoreCase(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        String[] segments = path.split("/");
        if (segments[0].equals("..")) {
            return ((MapleData) getParent()).getChildByPath(path.substring(path.indexOf("/") + 1));
        }

        Node myNode = node;
        for (String segment : segments) {
            NodeList childNodes = myNode.getChildNodes();
            boolean foundChild = false;
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode != null && childNode.getAttributes() != null && childNode.getAttributes().getNamedItem("name") != null && childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getAttributes().getNamedItem("name").getNodeValue().equals(segment)) {
                    myNode = childNode;
                    foundChild = true;
                    break;
                }
            }
            if (!foundChild) {
                return null;
            }
        }
        return new XMLDomMapleData(myNode, new File(imageDataDir, getName() + "/" + path).getParentFile());
    }

    public List<MapleData> getChildren() {
        List<String> skipChilds = new LinkedList<>();
        List<MapleData> cret = new LinkedList<>();
        List<String> subcret = new LinkedList<>();
        Map<String, MapleData> hotfix = MapleDataProviderFactory.getHotfixDatas();
        if (hotfix != null) {
            String thisPath = getPath();
            for (Map.Entry<String, MapleData> entry : hotfix.entrySet()) {
                if (entry.getKey().startsWith("-") && entry.getKey().substring(1).startsWith(thisPath)) {
                    if (entry.getKey().substring(1).equalsIgnoreCase(thisPath)) {
                        return new ArrayList<>();
                    } else {
                        String hfPath = entry.getKey().substring(1);
                        if (hfPath.replace(thisPath, "").split("/").length == 2) {
                            skipChilds.add(hfPath.substring(hfPath.lastIndexOf("/") + 1));
                        }
                    }
                } else if (entry.getKey().startsWith(thisPath)) {
                    if (entry.getKey().equalsIgnoreCase(thisPath)) {
                        List<MapleData> children = entry.getValue().getChildren();
                        for (MapleData child : children) {
                            if (child instanceof WzFileMapleData) {
                                ((WzFileMapleData) child).setHotfixParent(this);
                            }
                        }
                        return children;
                    } else {
                        if (entry.getKey().replace(thisPath, "").split("/").length == 2) {
                            if (entry.getValue() instanceof WzFileMapleData) {
                                ((WzFileMapleData) entry.getValue()).setHotfixParent(this);
                            }
                            cret.add(entry.getValue());
                            skipChilds.add(entry.getKey().substring(entry.getKey().lastIndexOf("/") + 1));
                        } else {
                            String child = entry.getKey().replace(thisPath + "/", "");
                            subcret.add(child.substring(0, child.indexOf("/")));
                        }
                    }
                }
            }
        }
        List<MapleData> ret = new ArrayList<>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            String childName = childNode == null || childNode.getAttributes() == null || childNode.getAttributes().getNamedItem("name") == null ? "" : childNode.getAttributes().getNamedItem("name").getNodeValue();
            if (childNode != null && childNode.getNodeType() == Node.ELEMENT_NODE && !skipChilds.contains(childName)) {
                if (subcret.contains(childName)) {
                    subcret.remove(childName);
                }
                ret.add(new XMLDomMapleData(childNode, new File(imageDataDir, getName())));
            }
        }
        String parent = imageDataDir.getPath().replaceAll("\\\\", "/");
        for (String sub : subcret) {
            WzFileMapleData dat = new WzFileMapleData(null, "", parent + (parent.isEmpty() ? "" : "/") + getName(), -1);
            dat.setType(MapleDataType.EXTENDED);
            dat.setName(sub);
            dat.setHotfixParent(this);
            ret.add(dat);
        }
        ret.addAll(cret);
        return ret;
    }


    public Object getData() {
        NamedNodeMap attributes = node.getAttributes();
        MapleDataType type = getType();
        String cacheKey = getName() + "_" + type.toString(); // 快取的鍵值
        if (dataCache.containsKey(cacheKey)) {
            return dataCache.get(cacheKey); // 從快取中返回資料
        }
        Object data = null;
        switch (type) {
            case DOUBLE: {
                return Double.parseDouble(attributes.getNamedItem("value").getNodeValue());
            }
            case FLOAT: {
                return Float.parseFloat(attributes.getNamedItem("value").getNodeValue());
            }
            case INT: {
                try {
                    return Integer.parseInt(attributes.getNamedItem("value").getNodeValue());
                } catch (Exception e) {
                    return Long.parseLong(attributes.getNamedItem("value").getNodeValue());
                }
            }
            case LONG: {
                return Long.parseLong(attributes.getNamedItem("value").getNodeValue());
            }
            case SHORT: {
                return Short.parseShort(attributes.getNamedItem("value").getNodeValue());
            }
            case STRING:
            case UOL: {
                return attributes.getNamedItem("value").getNodeValue();
            }
            case VECTOR: {
                return new Point(Integer.parseInt(attributes.getNamedItem("x").getNodeValue()), Integer.parseInt(attributes.getNamedItem("y").getNodeValue()));
            }
            case CANVAS: {
                return new FileStoredPngMapleCanvas(Integer.parseInt(attributes.getNamedItem("width").getNodeValue()), Integer.parseInt(attributes.getNamedItem("height").getNodeValue()), new File(imageDataDir, getName() + ".png"));
            }
        }
        dataCache.put(cacheKey, data); // 將資料放入快取
        return data;
        //當需要讀取相同的資料時，將直接從快取中返回，而不需要再次讀取，從而提高了讀取速度
    }

    public MapleDataType getType() {
        String nodeName = node.getNodeName();
        switch (nodeName) {
            case "imgdir":
                return MapleDataType.PROPERTY;
            case "canvas":
                return MapleDataType.CANVAS;
            case "convex":
                return MapleDataType.CONVEX;
            case "sound":
                return MapleDataType.SOUND;
            case "uol":
                return MapleDataType.UOL;
            case "double":
                return MapleDataType.DOUBLE;
            case "float":
                return MapleDataType.FLOAT;
            case "long":
                return MapleDataType.LONG;
            case "int":
                return MapleDataType.INT;
            case "short":
                return MapleDataType.SHORT;
            case "string":
                return MapleDataType.STRING;
            case "vector":
                return MapleDataType.VECTOR;
            case "null":
                return MapleDataType.IMG_0x00;
        }
        return null;
    }

    @Override
    public MapleDataEntity getParent() {
        Node parentNode = node.getParentNode();
        if (parentNode.getNodeType() == Node.DOCUMENT_NODE) {
            return null;
        }
        return new XMLDomMapleData(parentNode, imageDataDir.getParentFile());
    }

    @Override
    public String getPath() {
        return imageDataDir.getPath().replaceAll("\\\\", "/") + "/" + getName();
    }

    @Override
    public String getName() {
        return node.getAttributes().getNamedItem("name").getNodeValue();
    }

    @Override
    public Iterator<MapleData> iterator() {
        return getChildren().iterator();
    }
}
