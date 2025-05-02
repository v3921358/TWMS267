package Net.server.maps;

import Client.MapleClient;
import Packet.MaplePacketCreator;

import java.awt.*;

public class MapleRandomPortal extends MapleMapObject {

    private Type appearType;
    private int mapid;
    private int owerid;
    private long startTime;
    private long duration;

    MapleRandomPortal(Type appearType, int mapid, int owerid, int duration, Point position) {
        setPosition(position);
        this.appearType = appearType;
        this.mapid = mapid;
        this.owerid = owerid;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }

    public Type getAppearType() {
        return appearType;
    }

    public void setAppearType(Type appearType) {
        this.appearType = appearType;
    }

    public int getMapid() {
        return mapid;
    }

    public void setMapid(int mapid) {
        this.mapid = mapid;
    }

    public int getOwerid() {
        return owerid;
    }

    public void setOwerid(int owerid) {
        this.owerid = owerid;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.RANDOM_PORTAL;
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(MaplePacketCreator.getRandomPortalCreated(this));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.getRandomPortalRemoved(this));
    }

    public enum Type {
        None("undefined"),
        Event("random_portal_event"),
        PolloFritto("random_portal_pollo_fritto"),
        Inferno("random_portal_inferno"),
        ;

        private final String script;

        Type(String script) {
            this.script = script;
        }

        public String getScript() {
            return script;
        }
    }
}
