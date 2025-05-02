package Net.server.life;

import Client.MapleClient;
import Net.server.maps.MapleMapObjectType;
import Net.server.shop.MapleShopFactory;
import Packet.NPCPacket;

public class MapleNPC extends AbstractLoadedMapleLife {

    private final int mapid;
    private String name = "MISSINGNO";
    private boolean custom = false;
    private int ownerid = 0;
    private boolean move;

    public MapleNPC(int id, String name, int mapid) {
        super(id);
        this.name = name;
        this.mapid = mapid;
    }

    public boolean hasShop() {
        return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
    }

    public void sendShop(MapleClient c) {
        MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (getId() < 9901000 && getId() != 9000069 && getId() != 9000133) {
            client.announce(NPCPacket.spawnNPC(this));
            client.announce(NPCPacket.spawnNPCRequestController(this, true));
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(NPCPacket.removeNPC(getObjectId()));
        if (!isHidden() && client.getPlayer() != null && client.getPlayer().getMap() != null && client.getPlayer().getMap().isNpcHide(getId())) {
            client.announce(NPCPacket.removeNPCController(getObjectId(), false));
        }
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.NPC;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public int getMapid() {
        return mapid;
    }

    public int getOwnerid() {
        return ownerid;
    }

    public void setOwnerid(int ownerid) {
        this.ownerid = ownerid;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

}
