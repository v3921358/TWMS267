package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

public class MovementNew2 extends MovementBase {
    public MovementNew2(int command, int elapse, int moveAction, byte forcedStop) {
        super(command, moveAction, elapse, forcedStop);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter mplew) {
        mplew.write(getCommand());
        mplew.writeInt(getUnk2());
        mplew.write(getMoveAction());
        mplew.writeShort(getElapse());
        mplew.write(getForcedStop());
    }

}
