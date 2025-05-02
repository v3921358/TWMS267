package Net.server.maps.field;

import Net.server.maps.MapleMap;


public class BossSerenField extends MapleMap {

    private int boss = 0;

    private final int time = 4; //一開始為3進場時才會是正確時間0
    private final int sunlightValue = 0;

    public static void init() {
    }


    public BossSerenField(int mapid, int channel, int returnMapId, float monsterRate) {
        super(mapid, channel, returnMapId, monsterRate);
    }

    public final void setSeren(final int bossid) {
        this.boss = bossid;
    }

    public final int getBoss() {
        return this.boss;
    }

    public final int getCurrentDayTime() {
        return time;
    }

    public final int getsunlightValue() {
        return sunlightValue;
    }

    public final int getDayTime() {
        //0 > 1 > 2 > 3
        return time;
    }


}
