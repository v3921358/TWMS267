package Plugin.provider.nx;

import Plugin.provider.MapleDataEntity;
import Plugin.provider.MapleDataEntry;
import Plugin.provider.MapleDataType;

public class NxIMGFile {

    protected final NXTables tables;
    private final MapleDataEntry file;
    private final NXNode root;

    public NxIMGFile(String path, MapleDataEntry file, NXHeader Header, NXTables tables) {
        this.file = file;
        root = new NXNode(this, path, "", file.getOffset(), Header);
        root.setName(file.getName());
        root.setType(MapleDataType.EXTENDED);
        this.tables = tables;
    }

    public MapleDataEntity getParent() {
        return file.getParent();
    }

    public long getOffset() {
        return file.getOffset();
    }

    public NXNode getRoot() {
        return root;
    }

    public NXTables getTables() {
        return tables;
    }
}
