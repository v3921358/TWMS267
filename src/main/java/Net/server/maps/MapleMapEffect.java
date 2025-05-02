package Net.server.maps;

import Client.MapleClient;
import Packet.MTSCSPacket;
import Packet.MaplePacketCreator;

import java.util.concurrent.ScheduledFuture;

public class MapleMapEffect {

    private String msg = "";
    private int itemId = 0, effectType = -1;
    private boolean active = true;
    private boolean jukebox = false;
    ScheduledFuture<?> scheduledFuture;

    public MapleMapEffect(String msg, int itemId) {
        this.msg = msg;
        this.itemId = itemId;
        this.effectType = -1;
    }

    public MapleMapEffect(String msg, int itemId, int effectType) {
        this.msg = msg;
        this.itemId = itemId;
        this.effectType = effectType;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return this.scheduledFuture;
    }

    public boolean isJukebox() {
        return this.jukebox;
    }

    public void setJukebox(boolean actie) {
        this.jukebox = actie;
    }

    public byte[] makeDestroyData() {
        return jukebox ? MTSCSPacket.playCashSong(0, "") : MaplePacketCreator.removeMapEffect();
    }

    public byte[] makeStartData() {
        return jukebox ? MTSCSPacket.playCashSong(itemId, msg) : MaplePacketCreator.startMapEffect(msg, itemId, effectType, active);
    }

    public void sendStartData(MapleClient c) {
        c.announce(makeStartData());
    }
}
