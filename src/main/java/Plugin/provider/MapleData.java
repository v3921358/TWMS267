package Plugin.provider;

import java.util.List;

public interface MapleData extends MapleDataEntity, Iterable<MapleData> {

    String getName();

    MapleData getChildByPath(String path);

    List<MapleData> getChildren();

    Object getData();

    MapleDataType getType();
}
