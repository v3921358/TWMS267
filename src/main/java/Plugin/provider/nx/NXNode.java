package Plugin.provider.nx;

import Plugin.provider.MapleData;
import Plugin.provider.MapleDataEntity;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataType;
import Plugin.provider.nx.util.NxLittleEndianAccessor;
import Plugin.provider.wz.WzFileMapleData;
import tools.data.BufferedRandomAccessFile;
import tools.data.RandomAccessByteStream;

import java.io.IOException;
import java.util.*;

public class NXNode implements MapleData {

    public static final int NODE_SIZE = 20;

    private NxIMGFile file;
    public NXHeader Header;
    private String name;
    private MapleDataType type;
    private List<MapleData> children = null;
    private Object data;
    private long nodeOffset;
    private final String nxFile;
    private final String parent;
    private MapleData hotfixParent = null;

    public NXNode(NxIMGFile file, String nxFile, String parent, long nodeOffset, NXHeader Header) {
        this.file = file;
        this.nxFile = nxFile;
        this.parent = parent;
        this.nodeOffset = nodeOffset;
        this.Header = Header;
    }

    public void setHotfixParent(MapleData dat) {
        hotfixParent = dat;
    }

    @Override
    public MapleData getChildByPath(String path) {
        String[] segments = path.split("/");
        if (segments[0].equals("..")) {
            return ((MapleData) getParent()).getChildByPath(path.substring(path.indexOf("/") + 1));
        }
        MapleData ret = this;
        for (String segment : segments) {
            boolean foundChild = false;
            for (MapleData child : ret.getChildren()) {
                if (child.getName().equals(segment)) {
                    ret = child;
                    foundChild = true;
                    break;
                }
            }
            if (!foundChild) {
                return null;
            }
        }
        return ret;
    }

    @Override
    public List<MapleData> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
            List<String> skipChilds = new LinkedList<>();
            List<MapleData> cret = new LinkedList<>();
            List<String> subcret = new LinkedList<>();
            Map<String, MapleData> hotfix = MapleDataProviderFactory.getHotfixDatas();
            if (!hotfix.isEmpty()) {
                String thisPath = getPath();
                for (Map.Entry<String, MapleData> entry : hotfix.entrySet()) {
                    if (entry.getKey().startsWith("-") && entry.getKey().substring(1).startsWith(thisPath)) {
                        if (entry.getKey().substring(1).equalsIgnoreCase(thisPath)) {
                            return children;
                        } else {
                            String hfPath = entry.getKey().substring(1);
                            if (hfPath.replace(thisPath, "").split("/").length == 2) {
                                skipChilds.add(hfPath.substring(hfPath.lastIndexOf("/") + 1));
                            }
                        }
                    } else if (entry.getKey().startsWith(thisPath)) {
                        if (entry.getKey().equalsIgnoreCase(thisPath)) {
                            children = entry.getValue().getChildren();
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
            try (BufferedRandomAccessFile raf = new BufferedRandomAccessFile(nxFile, "r")) {
                NxLittleEndianAccessor nlea = new NxLittleEndianAccessor(new RandomAccessByteStream(raf), Header);
                parseEntry(nlea, skipChilds, subcret, true);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String sub : subcret) {
                WzFileMapleData dat = new WzFileMapleData(null, "", parent + (parent.isEmpty() ? "" : "/") + name, -1);
                dat.setType(MapleDataType.EXTENDED);
                dat.setName(sub);
                dat.setHotfixParent(this);
                children.add(dat);
            }
            children.addAll(cret);
        }
        return Collections.unmodifiableList(children);
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public MapleDataType getType() {
        return type;
    }

    public void setType(MapleDataType type) {
        this.type = type;
    }

    @Override
    public Iterator<MapleData> iterator() {
        return getChildren().iterator();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addChild(NXNode entry) {
        children.add(entry);
    }

    @Override
    public MapleDataEntity getParent() {
        if (hotfixParent != null) {
            return hotfixParent;
        } else if (parent.equals(file.getRoot().getName())) {
            return file.getRoot();
        } else if (!parent.isEmpty()) {
            String path = parent;
            if (parent.startsWith(file.getRoot().getName())) {
                path = path.replaceFirst(file.getRoot().getName() + "/", "");
            }
            return file.getRoot().getChildByPath(path);
        } else {
            return file.getParent();
        }
    }

    @Override
    public String getPath() {
        MapleDataEntity mde;
        MapleDataEntity ode = this;
        String path = getName();
        while ((mde = ode.getParent()) != ode && mde != null) {
            ode = mde;
            if (!mde.getName().isEmpty()) {
                path = mde.getName() + "/" + path;
            }
        }
        return path;
    }

    private void parseEntry(NxLittleEndianAccessor nlea, List<String> skipChilds, List<String> checkChilds, boolean getChild) {
        nlea.seek(nodeOffset);
        setName(file.getTables().getString(nlea, nlea.readUInt()));
        if (checkChilds.contains(name)) {
            checkChilds.remove(name);
        }
        int fCID = nlea.readInt();
        short cCount = nlea.readShort();
        short type = nlea.readShort();
        switch (type) {
            case 0:
                if (fCID <= 0) {
                    setType(MapleDataType.NONE);
                } else {
                    setType(MapleDataType.EXTENDED);
                }
                break;
            case 1:
                long number = nlea.readLong();
                if (number >= Short.MIN_VALUE && number <= Short.MAX_VALUE) {
                    setType(MapleDataType.SHORT);
                    setData((short) number);
                } else if (number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE) {
                    setType(MapleDataType.INT);
                    setData((int) number);
                } else {
                    setType(MapleDataType.LONG);
                    setData(number);
                }
                break;
            case 2:
                number = nlea.readLong();
                double dNumber = Double.longBitsToDouble(number);
                if (dNumber >= Float.MIN_VALUE && dNumber <= Float.MAX_VALUE) {
                    setType(MapleDataType.FLOAT);
                    setData((float) dNumber);
                } else {
                    setType(MapleDataType.DOUBLE);
                    setData(dNumber);
                }
                break;
            case 3:
                setType(MapleDataType.STRING);
                setData(file.getTables().getString(nlea, (int) nlea.readLong()));
                break;
            case 4:
                setType(MapleDataType.VECTOR);
                setData(nlea.readPosInt());
                break;
            case 5:
                setType(MapleDataType.CANVAS);
                break;
            case 6:
                setType(MapleDataType.SOUND);
                break;
        }
        if ((fCID > 0 || cCount > 0) && getChild) {
            for (int i = 0; i < cCount; i++) {
                long offset = nlea.Header.NodeOffset + (fCID + i) * 20;
                NXNode cNode = new NXNode(file, nxFile, parent + (parent.isEmpty() ? "" : "/") + name, offset, Header);
                cNode.parseEntry(nlea, new LinkedList<>(), new LinkedList<>(), false);
                if (!skipChilds.contains(cNode.name)) {
                    addChild(cNode);
                }
            }
        }
    }

    private void finish() {
        ((ArrayList<MapleData>) children).trimToSize();
    }
}
