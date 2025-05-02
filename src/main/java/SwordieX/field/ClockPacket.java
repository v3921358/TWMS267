package SwordieX.field;

import connection.OutPacket;
import SwordieX.enums.ClockType;

public class ClockPacket {

    private ClockType clockType;

    private int arg1, arg2, arg3, arg4;
    private boolean bool;

    private ClockPacket() {
    }

    private ClockPacket(ClockType clockType) {
        this.clockType = clockType;
    }

    private ClockPacket(ClockType clockType, int arg1) {
        this.clockType = clockType;
        this.arg1 = arg1;
        this.arg2 = 0;
    }

    private ClockPacket(ClockType clockType, int arg1, int arg2) {
        this.clockType = clockType;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    private ClockPacket(ClockType clockType, int arg1, int arg2, int arg3, int arg4) {
        this.clockType = clockType;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.arg4 = arg4;
    }

    public static ClockPacket eventTimer(int time) {
        return new ClockPacket(ClockType.EventTimer, time);
    }

    public static ClockPacket hmsClock(byte ctype, byte hour, byte minutes, byte seconds) {
        return new ClockPacket(ClockType.HMSClock, ctype, hour, minutes, seconds);
    }

    public static ClockPacket secondsClock(long seconds) {
        return new ClockPacket(ClockType.SecondsClock, (int) (seconds / 1000));
    }

    public static ClockPacket removeClock() {
        return new ClockPacket(ClockType.SecondsClock, -1);
    }

    public static ClockPacket templateClock(byte template, int n) {
        return new ClockPacket(ClockType.FromDefault, template, n);
    }

    public static ClockPacket timerGauge(int current, int max) {
        return new ClockPacket(ClockType.TimerGauge, max, current);
    }

    public static ClockPacket shiftTimer(boolean left, int time) {
        ClockPacket cp = new ClockPacket(ClockType.ShiftTimer, time);
        cp.bool = left;
        return cp;
    }

    public static ClockPacket TimerInfoEx(int time, int elapseTime) {
        return new ClockPacket(ClockType.TimerInfoEx, time, elapseTime);
    }

    public static ClockPacket stopWatch(int time) {
        return new ClockPacket(ClockType.StopWatch, time * 1000);
    }

    public static ClockPacket pauseTimer(boolean paused, int time, int n) {
        ClockPacket cp = new ClockPacket(ClockType.PauseTimer, time, n);
        cp.bool = paused;
        return cp;
    }

    public static ClockPacket withoutField(boolean clear, int time) {
        ClockPacket cp = new ClockPacket(ClockType.WithoutField, time);
        cp.bool = clear;
        return cp;
    }

    public static ClockPacket unk40(int v1, int v2) {
        return new ClockPacket(ClockType.Unk40, v1, v2);
    }

    public static ClockPacket unk103(int v1, int v2) {
        return new ClockPacket(ClockType.UNK103, v1, v2);
    }

    public void encode(OutPacket outPacket) {
        outPacket.encodeByte(clockType.getVal());
        switch (clockType) {
            case SecondsClock: // ok
                outPacket.encodeInt(arg1);
                break;
            case HMSClock:
                outPacket.encodeByte(arg1); //Clock-Type
                outPacket.encodeByte(arg2);
                outPacket.encodeByte(arg3);
                outPacket.encodeByte(arg4);
                break;
            case FromDefault:
                outPacket.encodeByte(arg1);
                outPacket.encodeInt(arg2);
                break;
            case TimerGauge:
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                break;
            case ShiftTimer:
                outPacket.encodeByte(bool);
                outPacket.encodeInt(arg1);
                break;
            case StopWatch:
                outPacket.encodeInt(arg1);
                break;
            case PauseTimer: // OK
                outPacket.encodeByte(bool);
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                break;
            case TimerInfoEx:  // not found
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                break;
            case WithoutField:
                outPacket.encodeByte(bool);
                outPacket.encodeInt(arg1);
                break;
            case UNK10:
                outPacket.encodeByte(bool);
                if (bool) {
                    outPacket.encodeInt(arg1);
                }
                break;
            case RescueTimer:
                outPacket.encodeInt(arg1);
                break;
            case UNK12:
            case UNK13:
                outPacket.encodeInt(arg1);
                outPacket.encodeByte(bool);
                break;
            case UNK14:
                outPacket.encodeInt(arg1);
                outPacket.encodeString("");
                break;
            case UNK15:
                outPacket.encodeByte(bool);
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                break;
            case UNK16:
                outPacket.encodeInt(arg1);
                break;
            case UNK17:
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                outPacket.encodeInt(arg3);
                outPacket.encodeInt(0);
                break;
            case UNK101:
                outPacket.encodeInt(arg1);
                break;
            case UNK102:
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                break;
            case UNK103:
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                break;
            case Unk40: // not found
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                break;
        }
    }
}
