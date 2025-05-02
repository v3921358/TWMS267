package Plugin.script.binding;

import Client.MapleCharacter;
import Net.server.maps.MapleMap;
import SwordieX.client.party.Party;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ScriptParty {
    @Getter
    private final Party party;

    public ScriptParty(Party pt) {

        this.party = pt;
    }

    public int getId() {
        return getParty().getId();
    }


    public void changeMap(int mapId) {
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            chr.changeMap(mapId, 0);
        }
    }

    public void changeMap(int mapId, int portal) {
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            chr.changeMap(mapId, portal);
        }
    }

    public void changeMap(MapleMap map) {
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            chr.changeMap(map);
        }
    }

    public List<ScriptPlayer> getLocalMembers() {
        List<ScriptPlayer> members = new ArrayList<>();
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            members.add(new ScriptPlayer(chr));
        }
        return members;
    }

    public ScriptPlayer getLeader() {
        return new ScriptPlayer(getParty().getPartyLeader().getChr());
    }

    public int getMembersCount() {
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        return getParty().getCharsInSameField(leader).size();
    }

    public int getMembersCount(int minLevel,int maxLevel) {
        int count = 0;
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            if (chr.getLevel() >= minLevel && chr.getLevel() <= maxLevel) {
                count++;
            }
        }

        return count;
    }

    public boolean isAllMembersAllowedPQ(String pqLog, int maxEnter) {
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            if (chr.getPQLog(pqLog) >= maxEnter) {
                return false;
            }
        }
        return true;
    }

    public ScriptPlayer getNotAllowedMember(String pqLog, int maxEnter) {
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            if (chr.getPQLog(pqLog) >= maxEnter) {
                return new ScriptPlayer(chr);
            }
        }
        return null;
    }


    public boolean isAllMembersHasItem(int itemId, int quantity) {
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            if (!chr.haveItem(itemId, quantity)) {
                return false;
            }
        }
        return true;
    }

    public String getNotHasItemMember(int itemId, int quantity) {
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            if (!chr.haveItem(itemId, quantity)) {
                return chr.getName();
            }
        }
        return null;
    }

    public void loseItem(int itemId, int quantity) {
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            if (chr.isOnline()) {
                chr.removeItem(itemId, quantity);
            }
        }
    }

    public void loseItem(int itemId) {
        MapleCharacter leader = getParty().getPartyLeader().getChr();
        for (MapleCharacter chr : getParty().getCharsInSameField(leader)) {
            if (chr.isOnline()) {
                chr.removeItem(itemId);
            }
        }
    }
}
