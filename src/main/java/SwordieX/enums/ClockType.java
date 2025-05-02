package SwordieX.enums;

public enum ClockType {
    EventTimer(0),
    HMSClock(1),
    SecondsClock(2),
    FromDefault(3),
    TimerGauge(4),
    ShiftTimer(5),
    StopWatch(6),
    PauseTimer(7),
    TimerInfoEx(8),
    WithoutField(9),
    UNK10(10),
    RescueTimer(11),
    UNK12(12),
    UNK13(13),
    UNK14(14),
    UNK15(15),
    UNK16(16),
    UNK17(17),
    UNK101(101),
    UNK102(102),
    UNK103(103),

    Unk40(40),
    None(-1);

    private final byte val;

    ClockType(int val) {
        this.val = (byte) val;
    }

    public byte getVal() {
        return val;
    }
}
