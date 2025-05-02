package Plugin.provider;

/**
 * @author twms Hertz
 * @notice 初步增強了 歷遍檔案的速度
 * @ver 245.
 */

public class MapleDataEntry implements MapleDataEntity {

    private final String name;
    private final int size;
    private final int checksum;
    private final MapleDataEntity parent;
    private final long offset;

    MapleDataEntry(String name, int size, int checksum, long offset, MapleDataEntity parent) {
        super();
        this.name = name;
        this.size = size;
        this.checksum = checksum;
        this.offset = offset;
        this.parent = parent;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getChecksum() {
        return checksum;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public MapleDataEntity getParent() {
        return parent;
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
}
