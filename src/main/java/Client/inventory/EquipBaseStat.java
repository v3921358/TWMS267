package Client.inventory;

/**
 *
 */
public enum EquipBaseStat {
    /**
     * 0x1
     */
    力量(0),
    /**
     * 0x2
     */
    敏捷(1),
    /**
     * 0x4
     */
    智力(2),
    /**
     * 0x8
     */
    幸運(3),
    /**
     * 0x10
     */
    MaxHP(4),
    /**
     * 0x20
     */
    MaxMP(5),
    /**
     * 0x40
     */
    攻擊力(6),
    /**
     * 0x80
     */
    魔力(7),
    /**
     * 0x100
     */
    防禦力(8),
    /**
     * 0x200
     */
    魔法防禦力(9),
    /**
     * 0x400
     */
    靈敏度(10),
    /**
     * 0x800
     */
    移動速度(11),
    /**
     * 0x1000
     */
    跳躍力(12),
    ;
    private final int value;

    EquipBaseStat(int i) {
        value = i;
    }

    public int getFlag() {
        return 1 << value;
    }

    public int getValue() {
        return value;
    }

    public boolean check(int flag) {
        return (flag & getFlag()) != 0;
    }

}
