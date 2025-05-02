package SwordieX.enums;

public enum ServerStatus {
    NORMAL(0),
    BUSY(1),
    FULL(2);

    private final byte value;

    ServerStatus(int value) {
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }
}
