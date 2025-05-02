package Net.server.events;

public enum MapleEventType {

    Coconut("椰子比賽", new int[]{109080000}), //楓之谷活動 - 椰子比賽
    CokePlay("CokePlay", new int[]{109080010}), //?????? - ?-???? ??
    Fitness("向高地", new int[]{109040000, 109040001, 109040002, 109040003, 109040004}), //楓之谷活動 - 向高地&lt;待機室>
    OlaOla("上樓~上樓", new int[]{109030001, 109030002, 109030003}), //楓之谷活動 - 上樓~上樓~&lt;第1階段>
    OxQuiz("OX問答", new int[]{109020001}), //楓之谷活動 - OX問答
    Survival("", new int[]{809040000, 809040100}),
    Snowball("雪球賽", new int[]{109060000}); //楓之谷活動 - 雪球賽
    public final String desc; //活動描述介紹
    public final int[] mapids; //活動舉行的地圖

    MapleEventType(String desc, int[] mapids) {
        this.desc = desc;
        this.mapids = mapids;
    }

    public static MapleEventType getByString(String splitted) {
        for (MapleEventType t : MapleEventType.values()) {
            if (t.name().equalsIgnoreCase(splitted)) {
                return t;
            }
        }
        return null;
    }
}
