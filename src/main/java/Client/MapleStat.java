package Client;

public enum MapleStat {

    皮膚(0x1), // short
    臉型(0x2), // int
    髮型(0x4), // int
    寵物(0x8), //
    等級(0x10), // byte
    職業(0x20), // int
    力量(0x40), // short
    敏捷(0x80), // short
    智力(0x100), // short
    幸運(0x200), // short
    HP(0x400), // int
    MAXHP(0x800), // int
    MP(0x1000), // int
    MAXMP(0x2000), // int
    AVAILABLEAP(0x4000), // short
    AVAILABLESP(0x8000), // short (depends)
    經驗(0x10000), // V.110修改為 long
    人氣(0x20000), // int
    楓幣(0x40000), // V.110修改為 long
    疲勞(0x80000), //疲勞
    領導力(0x100000), //領袖
    洞察力(0x200000), //洞察
    意志(0x400000), //意志
    手藝(0x800000), //手技
    感性(0x1000000), //感性
    魅力(0x2000000), //魅力
    TODAYS_TRAITS(0x4000000), //今日獲得
    TRAIT_LIMIT(0x8000000),
    BATTLE_EXP(0x10000000),
    BATTLE_RANK(0x20000000),
    BATTLE_POINTS(0x40000000),
    ICE_GAGE(0x80000000L),
    VIRTUE(0x100000000L),
    性別(0x200000000L),
    CHARISMA(1048576),
    PET(1572872),
    INSIGHT(2097152),
    WILL(4194304),
    CRAFT(8388608),
    SENSE(16777216),
    CHARM(33554432),
    RECOVERY_POTION_WITH_HP(67109888),
    RECOVERY_POTION_WITH_MP(67112960),
    RECOVERY_POTION_WITH_HPMP(67113984);

    private final long i;

    MapleStat(long i) {
        this.i = i;
    }

    public static MapleStat getByValue(long value) {
        for (final MapleStat stat : MapleStat.values()) {
            if (stat.i == value) {
                return stat;
            }
        }
        return null;
    }

    public long getValue() {
        return i;
    }

    public enum Temp {

        力量(0x1),
        敏捷(0x2),
        智力(0x4),
        幸運(0x8),
        物攻(0x10),
        魔攻(0x20),
        物防(0x40),
        魔防(0x80),
        命中(0x100),
        迴避(0x200),
        速度(0x400),
        跳躍(0x800);
        private final int i;

        Temp(int i) {
            this.i = i;
        }

        public int getValue() {
            return i;
        }
    }
}
