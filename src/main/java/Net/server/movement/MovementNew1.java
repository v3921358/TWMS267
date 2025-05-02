package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

public class MovementNew1 extends MovementBase {
    public MovementNew1(int command, int elapse, int moveAction, byte forcedStop) {
        super(command, moveAction, elapse, forcedStop);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter mplew) {
        mplew.write(getCommand());
        mplew.writeInt(getUnk3());
        mplew.write(getMoveAction());
        mplew.writeShort(getElapse());
        mplew.write(getForcedStop());
    }

}
