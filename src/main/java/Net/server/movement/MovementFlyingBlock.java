package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

public final class MovementFlyingBlock extends MovementBase {

    public MovementFlyingBlock(int command, int elapse, int moveAction, byte forcedStop) {
        super(command, moveAction, elapse, forcedStop);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getCommand());
        lew.writePos(getPosition());
        lew.writePos(getPixelsPerSecond());
        lew.write(getMoveAction());
        lew.writeShort(getElapse());
        lew.write(getForcedStop());
    }
}
