package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;

public class MovementBase implements LifeMovement {

    private Point position, pixelsPerSecond, offset;
    private int elapse, moveAction, command, fh, stat, unk2, unk3;
    private short footStart, unk1;
    private byte forcedStop;

    public MovementBase(int command, int stat) {
        setCommand(command);
        setStat(stat);
    }

    public MovementBase(int command, int moveAction, int elapse, byte forcedStop) {
        setCommand(command);
        setMoveAction(moveAction);
        setElapse(elapse);
        setForcedStop(forcedStop);
    }

    @Override
    public void serialize(final MaplePacketLittleEndianWriter mplew) {
        mplew.write(getCommand());
        mplew.write(getMoveAction());
        mplew.writeShort(getElapse());
        mplew.write(getForcedStop());
    }

    public final int getCommand() {
        return this.command;
    }

    public final void setCommand(int command) {
        this.command = command;
    }

    public final int getElapse() {
        return elapse;
    }

    public final void setElapse(int elapse) {
        this.elapse = elapse;
    }

    public final int getMoveAction() {
        return moveAction;
    }

    public final void setMoveAction(int moveAction) {
        this.moveAction = moveAction;
    }

    public final byte getForcedStop() {
        return forcedStop;
    }

    public final void setForcedStop(byte forcedStop) {
        this.forcedStop = forcedStop;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPixelsPerSecond() {
        return pixelsPerSecond;
    }

    public void setPixelsPerSecond(Point wobble) {
        this.pixelsPerSecond = wobble;
    }

    public Point getOffset() {
        return this.offset;
    }

    public void setOffset(Point offset) {
        this.offset = offset;
    }

    public int getFH() {
        return fh;
    }

    public void setFH(short fh) {
        this.fh = fh;
    }

    public short getFootStart() {
        return this.footStart;
    }

    public void setFootStart(final short footStart) {
        this.footStart = footStart;
    }

    public final int getStat() {
        return this.stat;
    }

    public final void setStat(int stat) {
        this.stat = stat;
    }

    public void setUnk1(short unk1) {
        this.unk1 = unk1;
    }

    public short getUnk1() {
        return unk1;
    }

    public int getUnk2() {
        return unk2;
    }

    public void setUnk2(int unk2) {
        this.unk2 = unk2;
    }

    public int getUnk3() {
        return unk3;
    }

    public void setUnk3(int unk3) {
        this.unk3 = unk3;
    }

}
