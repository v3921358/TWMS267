package Net.server.maps;

import Client.MapleClient;
import Client.SecondaryStat;

import java.awt.*;

/**
 * 楓之谷地圖上所有對象(例如:玩家、怪物、召喚獸、NPC等)的抽像基類.
 *
 * @author dongjak
 */
public abstract class MapleMapObject {

    private final Point position = new Point();
    private int objectId;
    private int dwOwnerID = 0;
    private boolean custom;

    public Point getPosition() {
        return new Point(position);
    }

    public int getDwOwnerID() {
        return dwOwnerID;
    }

    public Point getTruePosition() {
        return this.position;
    }

    public void setDwOwnerID(int dwOwnerID) {
        this.dwOwnerID = dwOwnerID;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public void setPosition(Point position) {
        this.position.x = position.x;
        this.position.y = position.y;
    }

    public int getObjectId() {
        return objectId;
    }


    public void setObjectId(int id) {
        this.objectId = id;
    }

    public Rectangle getBounds() {
        return new Rectangle(this.getPosition().x - 50, this.getPosition().y - 37, 50, 75);
    }

    public abstract MapleMapObjectType getType();

    public abstract int getRange();

    public abstract void sendSpawnData(MapleClient client);

    public abstract void sendDestroyData(MapleClient client);

    @Override
    public String toString() {
        return "Type:" + this.getType().name() + " ObjectID:" + this.objectId;
    }

    public boolean getDiseases(SecondaryStat i) {
        return false;
    }

}
