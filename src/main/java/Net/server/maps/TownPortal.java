package Net.server.maps;

import Client.MapleCharacter;
import Client.MapleClient;
import Config.constants.GameConstants;
import Net.server.MaplePortal;
import Packet.MaplePacketCreator;
import Packet.TownPortalPacket;
import SwordieX.client.party.PartyResult;
import connection.packet.WvsContext;
import SwordieX.util.Position;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TownPortal extends MapleMapObject {

    private final WeakReference<MapleCharacter> owner;
    private final MapleMap townMap;
    private final MaplePortal townPortal;
    private final MapleMap fieldMap;
    private final int skillId, ownerId;
    private final Point fieldPosition;
    private int state = 0;

    public TownPortal(MapleCharacter owner, int skillId) {
        this.owner = new WeakReference<>(owner);
        this.ownerId = owner.getId();
        this.fieldMap = owner.getMap();
        this.fieldPosition = owner.getPosition();
        this.townMap = this.fieldMap.getReturnMap();
        this.townPortal = getFreePortal();
        this.skillId = skillId;
        setPosition(this.fieldPosition);
    }

    public TownPortal(final TownPortal originTownPortal) {
        super();
        this.owner = originTownPortal.owner;
        this.ownerId = originTownPortal.ownerId;
        this.fieldMap = originTownPortal.fieldMap;
        this.fieldPosition = originTownPortal.fieldPosition;
        this.townMap = originTownPortal.townMap;
        this.townPortal = originTownPortal.townPortal;
        this.skillId = originTownPortal.skillId;
        setPosition(townPortal.getPosition());
    }

    public int getSkillId() {
        return skillId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        MapleCharacter player = client.getPlayer();
        if (player == null) { // 所有人可見
            return;
        }
        if (fieldMap.getId() != client.getPlayer().getMapId() && townMap.getId() != client.getPlayer().getMapId() && getOwnerId() != client.getPlayer().getId() && (getOwner() == null || getOwner().getParty() == null || client.getPlayer().getParty() == null || getOwner().getParty().getId() != client.getPlayer().getParty().getId())) {
            return;
        }
        final boolean isTown = getTownMap().getId() == player.getMapId();
        final Point doorPoint = isTown ? getTownPortal().getPosition() : getFieldPosition();
        client.announce(TownPortalPacket.onTownPortalCreated(this));
        if (getOwner().getParty() != null && (getOwner() == player || getOwner().getParty().getPartyMemberByID(player.getId()) != null)) {
            SwordieX.client.party.TownPortal tp = new SwordieX.client.party.TownPortal();
            tp.setTownID(getTownMap().getId());
            tp.setFieldID(getFieldMap().getId());
            tp.setSkillID(getSkillId());
            tp.setFieldPortal(new Position(doorPoint.x, doorPoint.y));
            client.write(WvsContext.partyResult(PartyResult.townPortalChanged(1, tp)));
        }
        client.announce(MaplePacketCreator.onTownPortal(getTownMap().getId(), getFieldMap().getId(), getSkillId(), doorPoint));
        player.enableActions(true);
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        MapleCharacter player = client.getPlayer();
        if (player == null) { // 所有人可見
            return;
        }
        client.announce(TownPortalPacket.onTownPortalRemoved(getOwnerId(), true));
        if (fieldMap.getId() == client.getPlayer().getMapId() || getOwnerId() == client.getPlayer().getId() || (getOwner() != null && getOwner().getParty() != null && client.getPlayer().getParty() != null && getOwner().getParty().getId() == client.getPlayer().getParty().getId()) || getTownMap().getId() == client.getPlayer().getMapId()) {
            if (getOwner().getParty() != null && (getOwnerId() == client.getPlayer().getId() || getOwner().getParty().getPartyMemberByID(client.getPlayer().getId()) != null)) {
                SwordieX.client.party.TownPortal tp = new SwordieX.client.party.TownPortal();
                tp.setTownID(999999999);
                tp.setFieldID(999999999);
                tp.setFieldPortal(new Position(-1, -1));
                client.write(WvsContext.partyResult(PartyResult.townPortalChanged(1, tp)));
            }
            client.announce(MaplePacketCreator.onTownPortal(999999999, 999999999, 0, null));
        }

    }

    public void warp(MapleCharacter chr, boolean toTown) {
        if (chr.getId() != getOwnerId() && (getOwner() == null || getOwner().getParty() == null || chr.getParty() == null || getOwner().getParty().getId() != chr.getParty().getId())) {
            chr.getClient().sendEnableActions();
        } else if (toTown) {
            chr.changeMapToPosition(townMap, townPortal.getPosition());
        } else {
            chr.changeMapToPosition(fieldMap, fieldPosition);
        }
    }

    private MaplePortal getFreePortal() {
        final List<MaplePortal> freePortals = new ArrayList<>();

        for (final MaplePortal port : townMap.getPortals()) {
            if (port.getType() == 6) {
                freePortals.add(port);
            }
        }
        freePortals.sort(Comparator.comparingInt(MaplePortal::getId));
        for (final TownPortal townPortal : townMap.getAllTownPortalsThreadsafe()) {
            if (townPortal.getOwner() != null && townPortal.getOwner().getParty() != null && getOwner() != null && getOwner().getParty() != null && getOwner().getParty().getPartyMemberByID(townPortal.getOwnerId()) != null) {
                freePortals.remove(townPortal.getTownPortal());
            }
        }
        if (freePortals.size() <= 0) {
            return null;
        }
        return freePortals.iterator().next();
    }

    public MapleCharacter getOwner() {
        return owner.get();
    }

    public MapleMap getTownMap() {
        return townMap;
    }

    public MaplePortal getTownPortal() {
        return townPortal;
    }

    public MapleMap getFieldMap() {
        return fieldMap;
    }

    public Point getFieldPosition() {
        return fieldPosition;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.TOWN_PORTAL;
    }

    @Override
    public int getRange() {
        return GameConstants.maxViewRange();
    }

    @Override
    public int getObjectId() {
        return ownerId;
    }
}
