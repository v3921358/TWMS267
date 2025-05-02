/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.maps;

import Client.MapleClient;
import Config.constants.GameConstants;
import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.concurrent.ScheduledFuture;

/**
 * @author ODINMR
 */
public final class MapleFieldAttackObj extends AnimatedMapleMapObject {

    private final int ownerid;
    private boolean side;
    private ScheduledFuture<?> schedule = null;
    private int state;
    private final int duration;

    public MapleFieldAttackObj(int state, int ownerid, int duration) {
        this.ownerid = ownerid;
        this.state = state;
        this.duration = duration;
    }

    public void cancel() {
        if (this.schedule != null) {
            this.schedule.cancel(true);
            this.schedule = null;
        }
    }

    public int getOwnerId() {
        return this.ownerid;
    }

    public boolean getSide() {
        return this.side;
    }

    public void setSide(boolean side) {
        this.side = side;
    }

    public ScheduledFuture<?> getSchedule() {
        return this.schedule;
    }

    public void setSchedule(ScheduledFuture<?> s) {
        this.schedule = s;
    }

    public int getDuration() {
        return duration;
    }

    public int getState() {
        return state;
    }

    public void setState(int value) {
        state = value;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.FIELD_ATTACK_OBJ;
    }

    @Override
    public int getRange() {
        return GameConstants.maxViewRange();
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_FIELDATTACKOBJ_CREATE.getValue());
        mplew.writeInt(getObjectId());
        mplew.writeInt(state);
        mplew.writeInt(ownerid);
        mplew.writeInt(0);
        mplew.writeBool(false);//V.160 new
        mplew.writePosInt(getPosition());
        mplew.writeBool(side);

        client.announce(mplew.getPacket());
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_FIELDATTACKOBJ_REMOVE_BYLIST.getValue());
        mplew.writeInt(1);
        mplew.writeInt(getObjectId());

        client.announce(mplew.getPacket());
    }
}
