package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

public class MovementStartFallDown extends MovementBase {

    public MovementStartFallDown(int command, int elapse, int moveAction, byte forcedStop) {
        super(command, moveAction, elapse, forcedStop);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getCommand());
        lew.writePos(getPixelsPerSecond());
        lew.writeShort(getFH());
        lew.write(getMoveAction());
        lew.writeShort(getElapse());
        lew.write(getForcedStop());
    }
}
