package Config.constants.enums;

public enum ZeroMask {
    EYE_ACCESSORY(3),
    HAT(1),
    FOREHEAD(2),
    EARRING(4),
    CAPE(9),
    TOP(5),
    GLOVE(8),
    WEAPON(11),
    BOTTOM(6),
    SHOE(7),
    RING3(12),
    RING2(13),
    RING1(15);

    private final int type;

    ZeroMask(int type) {
        this.type = type;
    }

    public int getFlag() {
        return 1 << ordinal();
    }

    public static ZeroMask getByType(final int n) {
        for (ZeroMask zm : values()) {
            if (zm.type == n) {
                return zm;
            }
        }
        return null;
    }
}
