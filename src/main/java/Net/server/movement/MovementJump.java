package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

public final class MovementJump extends MovementBase {

    public MovementJump(int command, int elapse, int moveAction, byte forcedStop) {
        super(command, moveAction, elapse, forcedStop);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter mplew) {
        mplew.write(getCommand());
        mplew.writePos(getPixelsPerSecond());
        if (getCommand() == 21 || getCommand() == 22) {
            mplew.writeShort(getFootStart());
        }
        if (getCommand() == 62) {
            mplew.writePos(getOffset());
        }
        mplew.write(getMoveAction());
        mplew.writeShort(getElapse());
        mplew.write(getForcedStop());
    }
}
