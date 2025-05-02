package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

public final class MovementOffsetX extends MovementBase {
    public MovementOffsetX(int command, int elapse, int moveAction, byte forcedStop) {
        super(command, moveAction, elapse, forcedStop);
    }

    public void serialize(MaplePacketLittleEndianWriter mplew) {
        mplew.write(getCommand());
        mplew.writePos(getPosition());
        mplew.writePos(getPixelsPerSecond());
        mplew.writeShort(getOffset().x);
        mplew.write(getMoveAction());
        mplew.writeShort(getElapse());
        mplew.write(getForcedStop());
    }

}
