/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.movement;

import tools.data.MaplePacketLittleEndianWriter;

/**
 * @author admin
 */
public final class MovementAngle extends MovementBase {
    public MovementAngle(int command, int elapse, int moveAction, byte forcedStop) {
        super(command, moveAction, elapse, forcedStop);
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter slea) {
        slea.write(getCommand());
        slea.writePos(getPosition());
        slea.writePos(getPixelsPerSecond());
        slea.writeShort(getFH());
        slea.write(getMoveAction());
        slea.writeShort(getElapse());
        slea.write(getForcedStop());
    }
}
