package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

public class MovementTeleport extends MovementBase {

    public MovementTeleport(int command, int elapse, int moveAction, byte forcedStop) {
        super(command, moveAction, elapse, forcedStop);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getCommand());
        lew.writePos(getPosition());
        lew.writeShort(getFH());
        lew.writeInt(getUnk2());
        lew.write(getMoveAction());
        lew.writeShort(getElapse());
        lew.write(getForcedStop());
    }
}
