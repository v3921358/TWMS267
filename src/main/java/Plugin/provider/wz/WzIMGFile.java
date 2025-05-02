package Plugin.provider.wz;

import Plugin.provider.MapleDataEntity;
import Plugin.provider.MapleDataFileEntry;
import Plugin.provider.MapleDataType;

public class WzIMGFile {

    private final MapleDataFileEntry file;
    private final WzFileMapleData root;

    public WzIMGFile(String path, MapleDataFileEntry file) {
        this.file = file;
        root = new WzFileMapleData(this, path, "", file.getOffset());
        root.setName(file.getName());
        root.setType(MapleDataType.EXTENDED);
    }

    public MapleDataEntity getParent() {
        return file.getParent();
    }

    public long getOffset() {
        return file.getOffset();
    }

    public WzFileMapleData getRoot() {
        return root;
    }
}
