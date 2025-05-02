package Client.inventory;

public enum EnhanceResultType {
    NO_DESTROY((short) 0x1),
    UPGRADE_TIER((short) 0x2),
    SCROLL_SUCCESS((short) 0x4),
    EQUIP_MARK((short) 0x80);

    private final short value;

    public final boolean check(int n) {
        return (n & this.value) != 0;
    }

    public final short getValue() {
        return this.value;
    }

    EnhanceResultType(short value) {
        this.value = value;
    }
}