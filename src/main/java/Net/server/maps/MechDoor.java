/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.maps;

import Client.MapleCharacter;
import Client.MapleClient;
import Packet.MaplePacketCreator;

import java.awt.*;

public class MechDoor extends MapleMapObject {

    private int owner, partyid, id;

    public MechDoor() {

    }

    public MechDoor(MapleCharacter owner, Point pos, int id) {
        super();
        this.owner = owner.getId();
        this.partyid = owner.getParty() == null ? 0 : owner.getParty().getId();
        setPosition(pos);
        this.id = id;
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(MaplePacketCreator.spawnMechDoor(this, false));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.removeMechDoor(this, false));
    }

    public int getOwnerId() {
        return this.owner;
    }

    public int getPartyId() {
        return this.partyid;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.TOWN_PORTAL;
    }
}
