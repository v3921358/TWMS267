package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

public final class MovementNormal extends MovementBase {
    public MovementNormal(int command, int elapse, int moveAction, byte forcedStop) {
        super(command, moveAction, elapse, forcedStop);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter mplew) {
        mplew.write(getCommand());
        mplew.writePos(getPosition());
        mplew.writePos(getPixelsPerSecond());
        mplew.writeShort(getFH());
        if (this.getCommand() == 15 || this.getCommand() == 17) {
            mplew.writeShort(getFootStart());
        }
        mplew.writePos(getOffset());
        mplew.writeShort(getUnk1());
        mplew.write(getMoveAction());
        mplew.writeShort(getElapse());
        mplew.write(getForcedStop());
    }
}
