package Config.constants.enums;

public enum BossListType {
    FindPart(2),
    Waiting(5),
    Join(7),
    Exit(8),
    ;

    private final int value;

    BossListType(int value) {
        this.value = value;
    }

    public static BossListType getType(int type) {
        for (BossListType bl : values()) {
            if (bl.getValue() == type) {
                return bl;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }
}
