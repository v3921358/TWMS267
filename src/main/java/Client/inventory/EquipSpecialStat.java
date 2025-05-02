/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.inventory;

/**
 * @author PlayDK
 */
public enum EquipSpecialStat {
    /**
     * 0x1
     */
    可使用捲軸次數(0),
    /**
     * 0x2
     */
    捲軸強化次數(1),
    /**
     * 0x4
     */
    狀態(2),
    /**
     * 0x8
     */
    裝備技能(3),//FLAG_LevelUpType
    /**
     * 0x10
     */
    裝備等級(4),
    /**
     * 0x20
     */
    裝備經驗(5),
    /**
     * 0x40
     */
    耐久度(6),
    /**
     * 0x80
     */
    鎚子(7),
    //大亂鬥傷害(8),
    /**
     * 0x100
     */
    套用等級減少(8),//FLAG_iReduceReq
    /**
     * 0x200
     */
    ENHANCT_BUFF(9),//FLAG_SpecialAttribute
    /**
     * 0x400
     */
    DURABILITY_SPECIAL(10),//FLAG_DurabilityMAX
    /**
     * 0x800
     */
    REQUIRED_LEVEL(11),//FLAG_iIncReq
    /**
     * 0x1000
     */
    YGGDRASIL_WISDOM(12),//FLAG_GrowthEnchant
    /**
     * 0x2000
     */
    FINAL_STRIKE(13), //最終一擊卷軸成功 FLAG_PSEnchant
    /**
     * 0x4000
     */
    BOSS傷(14),
    /**
     * 0x8000
     */
    無視防禦(15),
    /**
     * 0x10000
     */
    總傷害(16),
    /**
     * 0x20000
     */
    全屬性(17),
    /**
     * 0x40000
     */
    剪刀次數(18),
    /**
     * 0x80000
     */
    輪迴星火(19), //long
    /**
     * 0x100000
     */
    星力強化(20); //int
    private final int value;

    EquipSpecialStat(int value) {
        this.value = value;
    }

    public int getFlag() {
        return 1 << value;
    }

    public int getValue() {
        return value;
    }

    public final boolean check(final int flag) {
        return (flag & getFlag()) != 0;
    }
}
