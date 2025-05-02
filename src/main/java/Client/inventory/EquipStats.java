/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.inventory;

/**
 * @author PlayDK
 */
public enum EquipStats {

    可使用捲軸次數(0x1, 1, 1),
    捲軸強化次數(0x2, 1, 1),
    力量(0x4, 2, 1),
    敏捷(0x8, 2, 1),
    智力(0x10, 2, 1),
    幸運(0x20, 2, 1),
    MaxHP(0x40, 2, 1),
    MaxMP(0x80, 2, 1),
    攻擊力(0x100, 2, 1),
    魔力(0x200, 2, 1),
    防禦力(0x400, 2, 1),
    魔法防禦力(0x800, 2, 1),
    命中(0x1000, 2, 1),
    閃避(0x2000, 2, 1),
    靈敏度(0x4000, 2, 1),
    移動速度(0x8000, 2, 1),
    跳躍力(0x10000, 2, 1),
    狀態(0x20000, 4, 1),
    裝備技能(0x40000, 2, 1),//FLAG_LevelUpType
    裝備等級(0x80000, 2, 1),
    裝備經驗(0x100000, 8, 1),
    耐久度(0x200000, 4, 1),
    鎚子(0x400000, 4, 1),
    大亂鬥傷害(0x800000, 2, 1),
    套用等級減少(0x1000000, 1, 1),//FLAG_iReduceReq
    ENHANCT_BUFF(0x2000000, 2, 1),//FLAG_SpecialAttribute
    DURABILITY_SPECIAL(0x4000000, 4, 1),//FLAG_DurabilityMAX
    REQUIRED_LEVEL(0x8000000, 1, 1),//FLAG_iIncReq
    YGGDRASIL_WISDOM(0x10000000, 1, 1),//FLAG_GrowthEnchant
    FINAL_STRIKE(0x20000000, 1, 1), //最終一擊卷軸成功 FLAG_PSEnchant
    BOSS傷(0x40000000, 1, 1),
    無視防禦(0x80000000, 1, 1);
    private final int value, datatype, first;

    EquipStats(int value, int datatype, int first) {
        this.value = value;
        this.datatype = datatype;
        this.first = first;
    }

    public int getValue() {
        return value;
    }

    public int getDatatype() {
        return datatype;
    }

    public int getPosition() {
        return first;
    }

    public boolean check(int flag) {
        return (flag & value) != 0;
    }

    public enum EnhanctBuff {

        UPGRADE_TIER(0x1),
        NO_DESTROY(0x2),
        SCROLL_SUCCESS(0x4);
        private final int value;

        EnhanctBuff(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) value;
        }

        public boolean check(int flag) {
            return (flag & value) != 0;
        }
    }
}
