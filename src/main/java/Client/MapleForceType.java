package Client;

public enum MapleForceType {
    惡魔DF(false),
    幻影卡牌(false),
    劍刃之壁,
    審判之焰,
    追擊盾_反射(false),
    神盾系統(false),
    傑諾火箭,
    狂風肆虐,
    暴風滅世(false),
    UNK_09,
    三彩箭矢(false),
    刺客刻印,
    楓幣炸彈,
    靈狐,
    UNK_0E,
    Switch_type_Magic_暗影蝙蝠,
    SUMMON_暗影蝙蝠_反彈(false),
    軌道烈焰(false),
    UNK_12,
    UNK_13,
    輔助導彈,
    UNK_15,
    心靈傳動(false),
    魔法殘骸,
    UNK_18,
    UNK_19,
    UNK_1A,
    心雷合一,
    制裁火球,
    愛星能量,
    UNK_1E,
    殘影之矢,
    UNK_20,
    UNK_21,
    UNK_22,
    UNK_23,
    ;


    private final boolean isMultiMob;

    MapleForceType() {
        isMultiMob = true;
    }

    MapleForceType(boolean isMultiMob) {
        this.isMultiMob = isMultiMob;
    }

    public boolean isMultiMob() {
        return isMultiMob;
    }

}
