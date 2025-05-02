package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

public final class MovementStatChange extends MovementBase {

    public MovementStatChange(int command, int stat) {
        super(command, stat);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getCommand());
        lew.write(getStat());
    }

}
