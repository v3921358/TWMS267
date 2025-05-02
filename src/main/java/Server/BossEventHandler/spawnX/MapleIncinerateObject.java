package Server.BossEventHandler.spawnX;

import Client.MapleClient;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;

import java.util.concurrent.ScheduledFuture;

public class MapleIncinerateObject extends MapleMapObject {
    private int x;

    private int y;

    private ScheduledFuture<?> schedule;

    public MapleIncinerateObject(int x, int y) {
        setX(x);
        setY(y);
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.INCINERATE;
    }

    /**
     * @return
     */
    @Override
    public int getRange() {
        return 0;
    }

    /**
     * @param client
     */
    @Override
    public void sendSpawnData(MapleClient client) {

    }

    /**
     * @param client
     */
    @Override
    public void sendDestroyData(MapleClient client) {

    }

    public ScheduledFuture<?> getSchedule() {
        return this.schedule;
    }

    public void setSchedule(ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }
}
