package Net.server.maps;

public enum SavedLocationType {

    FREE_MARKET(0), //自由市場
    MULUNG_TC(1), //次元門
    WORLDTOUR(2), //旅遊中心
    FLORINA(3),
    FISHING(4), //釣魚
    RICHIE(5),
    EVENT(6), //任務地圖
    AMORIA(7), //結婚地圖
    CHRISTMAS(8), //聖誕地圖?
    TURNEGG(9), //轉蛋機
    BPReturn(10),
    CRYSTALGARDEN(11);
    private final int index;

    SavedLocationType(int index) {
        this.index = index;
    }

    public static SavedLocationType fromString(String Str) {
        return valueOf(Str);
    }

    public int getValue() {
        return index;
    }
}
