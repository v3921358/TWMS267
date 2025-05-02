package Client.inventory;

public enum EnchantScrollFlag {
    物攻(0x1),
    魔攻(0x2),
    力量(0x4),
    敏捷(0x8),
    智力(0x10),
    幸運(0x20),
    物防(0x40),
    魔防(0x80),
    Hp(0x100),
    Mp(0x200),
    命中(0x400),
    迴避(0x800),
    跳躍(0x1000),
    速度(0x2000),
    手技(0x4000);

    private final int value;

    public final boolean check(final int mask) {
        return (mask & this.value) != 0x0;
    }

    public final int getValue() {
        return value;
    }

    EnchantScrollFlag(final int value) {
        this.value = value;
    }
}