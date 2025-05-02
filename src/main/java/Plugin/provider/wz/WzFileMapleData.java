package Plugin.provider.wz;

import Plugin.provider.MapleData;
import Plugin.provider.MapleDataEntity;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataType;
import Plugin.provider.wz.util.WzLittleEndianAccessor;
import tools.data.BufferedRandomAccessFile;
import tools.data.RandomAccessByteStream;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class WzFileMapleData implements MapleData {
    private final WzIMGFile file;
    private String name;
    private MapleDataType type;
    private List<MapleData> children = null;
    private Object data;
    private long entryOffset;
    private final String wzFile;
    private final String parent;
    private MapleData hotfixParent = null;

    public WzFileMapleData(WzIMGFile file, String wzFile, String parent, long entryOffset) {
        this.file = file;
        this.wzFile = wzFile;
        this.parent = parent;
        this.entryOffset = entryOffset;
    }

    public void setHotfixParent(MapleData dat) {
        hotfixParent = dat;
    }

    @Override
    public String getPath() {
        MapleDataEntity mde;
        MapleDataEntity ode = this;
        StringBuilder path = new StringBuilder(getName());
        while ((mde = ode.getParent()) != ode && mde != null) {
            ode = mde;
            if (!mde.getName().isEmpty()) {
                path.insert(0, mde.getName() + "/");
            }
        }
        return path.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MapleDataType getType() {
        return type;
    }

    @Override
    public List<MapleData> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
            List<String> skipChilds = new LinkedList<>();
            List<MapleData> cret = new LinkedList<>();
            List<String> subcret = new LinkedList<>();
            if (file == null || !file.getRoot().getName().isEmpty()) {
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
            }
            if (entryOffset != -1 && type == MapleDataType.EXTENDED) {
                try (BufferedRandomAccessFile raf = new BufferedRandomAccessFile(wzFile, "r")) {
                    WzLittleEndianAccessor wlea = new WzLittleEndianAccessor(new RandomAccessByteStream(raf));
                    parseEntry(wlea, skipChilds, subcret, true);
                    finish();
                } catch (IOException e) {
                    e.fillInStackTrace();
                }
            }
            for (String sub : subcret) {
                WzFileMapleData dat = new WzFileMapleData(file, wzFile, parent + (parent.isEmpty() ? "" : "/") + name, -1);
                dat.setType(MapleDataType.EXTENDED);
                dat.setName(sub);
                dat.setHotfixParent(this);
                children.add(dat);
            }
            children.addAll(cret);
        }
        return Collections.unmodifiableList(children);
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
    public Object getData() {
        return data;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(MapleDataType type) {
        this.type = type;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void addChild(WzFileMapleData entry) {
        children.add(entry);
    }

    @Override
    public Iterator<MapleData> iterator() {
        return getChildren().iterator();
    }

    @Override
    public String toString() {
        return getName() + ":" + getData();
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

    private void parseEntry(WzLittleEndianAccessor wlea, List<String> skipChilds, List<String> checkChilds, boolean getChild) {
        wlea.seek(entryOffset);
        String type = wlea.readStringBlock(file.getOffset());
        switch (type) {
            case "Property": {
                if (!getChild) {
                    return;
                }
                setType(MapleDataType.PROPERTY);
                // Unknown, seems to be some identifier for PCOM.dll to read as a BMS config
                // file (ascii), whenever not 0?
                wlea.readByte();
                // Unknown, seems to be unused
                wlea.readByte();
                // Amount of variants
                int children = wlea.readCompressedInt();
                for (int i = 0; i < children; i++) {
                    WzFileMapleData cEntry = new WzFileMapleData(file, wzFile, parent + (parent.isEmpty() ? "" : "/") + name, wlea.getPosition());
                    cEntry.parseData(wlea, checkChilds);
                    if (!skipChilds.contains(cEntry.name)) {
                        addChild(cEntry);
                    }
                }
                break;
            }
            case "Canvas": {
                if (!getChild) {
                    return;
                }
                setType(MapleDataType.CANVAS);
                wlea.readByte();
                byte marker = wlea.readByte();
                switch (marker) { // do nothing
                    case 0:
                        break;
                    case 1:
                        wlea.readByte();
                        wlea.readByte();
                        int children = wlea.readCompressedInt();
                        for (int i = 0; i < children; i++) {
                            WzFileMapleData child = new WzFileMapleData(file, wzFile, parent + (parent.isEmpty() ? "" : "/") + name, wlea.getPosition());
                            child.parseData(wlea, checkChilds);
                            if (!skipChilds.contains(child.name)) {
                                addChild(child);
                            }
                        }
                        break;
                    default:
                        System.out.println("Canvas marker != 1 (" + marker + ")");
                        break;
                }
                int width = wlea.readCompressedInt();
                int height = wlea.readCompressedInt();
                int format = wlea.readCompressedInt();
                int format2 = wlea.readByte();
                wlea.readInt();
                int dataLength = wlea.readInt() - 1;
                wlea.readByte();
                setData(new PngMapleCanvas(width, height, dataLength, format + format2, null));
                wlea.skip(dataLength);
                break;
            }
            case "Shape2D#Vector2D": {
                setType(MapleDataType.VECTOR);
                int x = wlea.readCompressedInt();
                int y = wlea.readCompressedInt();
                setData(new Point(x, y));
                break;
            }
            case "Shape2D#Convex2D": {
                if (!getChild) {
                    return;
                }
                int children = wlea.readCompressedInt();
                for (int i = 0; i < children; i++) {
                    WzFileMapleData cEntry = new WzFileMapleData(file, wzFile, parent + (parent.isEmpty() ? "" : "/") + name, wlea.getPosition());
                    cEntry.parseEntry(wlea, new LinkedList<>(), new LinkedList<>(), false);
                    if (!skipChilds.contains(cEntry.name)) {
                        addChild(cEntry);
                    }
                }
                break;
            }
            case "Sound_DX8": {
                setType(MapleDataType.SOUND);
                wlea.readByte();
                int dataLength = wlea.readCompressedInt();
                wlea.readCompressedInt(); // no clue what this is
                int offset = (int) wlea.getPosition();
                setData(new ImgMapleSound(dataLength, offset - file.getOffset()));
                //wlea.seek(endOfExtendedBlock);
                break;
            }
            case "UOL": {
                setType(MapleDataType.UOL);
                wlea.readByte();
                setData(wlea.readStringBlock(file.getOffset()));
                break;
            }
            default: {
                throw new RuntimeException("Unhandeled extended type: " + type);
            }
        }
    }

    private void parseData(WzLittleEndianAccessor wlea, List<String> checkChilds) {
        // Variant name (node name)
        setName(wlea.readStringBlock(file.getOffset()));
        checkChilds.remove(name);
        byte type = wlea.readByte();
        switch (type) {
            case 0:
                setType(MapleDataType.IMG_0x00);
                break;
            case 2:
            case 11: // ??? no idea, since 0.49
                setType(MapleDataType.SHORT);
                setData(wlea.readShort());
                break;
            case 3:
            case 19:
                setType(MapleDataType.INT);
                setData(wlea.readCompressedInt());
                break;
            case 20:
                setType(MapleDataType.LONG);
                setData(wlea.readLongValue());
                break;
            case 4:
                setType(MapleDataType.FLOAT);
                setData(wlea.readFloatValue());
                break;
            case 5:
                setType(MapleDataType.DOUBLE);
                setData(wlea.readDouble());
                break;
            case 8:
                setType(MapleDataType.STRING);
                setData(wlea.readStringBlock(file.getOffset()));
                break;
            case 9:
                setType(MapleDataType.EXTENDED);
                long endOfExtendedBlock = wlea.readInt();
                endOfExtendedBlock += wlea.getPosition();
                entryOffset = wlea.getPosition();
                parseEntry(wlea, new LinkedList<>(), new LinkedList<>(), false);
                wlea.seek(endOfExtendedBlock);
                break;
            default:
                System.out.println("Unknown Image type " + type);
        }
    }

    private void finish() {
        ((ArrayList<MapleData>) children).trimToSize();
    }
}