package SwordieX.client.party;

import Client.MapleCharacter;
import Net.server.maps.MapleMap;
import Plugin.script.binding.ScriptEvent;
import connection.Encodable;
import connection.OutPacket;
import connection.packet.WvsContext;
import SwordieX.util.Position;
import SwordieX.world.World;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public class Party implements Encodable {
    @Setter
    @Getter
    private int id;
    @Getter
    private PartyMember[] partyMembers = new PartyMember[6];
    @Setter
    @Getter
    private boolean appliable;
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private int partyLeaderID;
    @Setter
    @Getter
    private World world;
    @Setter
    @Getter
    private MapleCharacter applyingChar;
    //    private Instance instance;
    @Setter
    @Getter
    private boolean duoParty;
    @Setter
    @Getter
    private boolean leaderPick;

    public boolean isFull() {
        return Arrays.stream(getPartyMembers()).noneMatch(Objects::isNull);
    }

    public boolean isEmpty() {
        return Arrays.stream(getPartyMembers()).allMatch(Objects::isNull);
    }

    public void changeLeaderDC() {
        PartyMember lpm = null;
        for (PartyMember pm : getPartyMembersInSameField(getPartyLeader())) {
            if (pm != null && lpm != pm && (lpm == null || lpm.getLevel() < pm.getLevel())) {
                lpm = pm;
            }
        }
        changeLeader(lpm == null ? 0 : lpm.getCharID(), false);
    }

    public void changeLeader(int newLeaderID, boolean force) {
        if (newLeaderID == partyLeaderID) {
            return;
        }
        PartyMember pm;
        PartyMember leader = getPartyLeader();
        if (newLeaderID <= 0 || (pm = getPartyMemberByID(newLeaderID)) == null) {
            broadcast(WvsContext.partyResult(PartyResult.msg(PartyType.PartyRes_ChangePartyBoss_NoMemberInSameField)));
            return;
        }
        if (!force && leader != null) {
            if (pm.getFieldID() != leader.getFieldID()) {
                broadcast(WvsContext.partyResult(PartyResult.msg(PartyType.PartyRes_ChangePartyBoss_NotSameField)));
                return;
            }
            if (pm.getChannel() != leader.getChannel()) {
                broadcast(WvsContext.partyResult(PartyResult.msg(PartyType.PartyRes_ChangePartyBoss_NotSameChannel)));
                return;
            }
        }
        setPartyLeaderID(newLeaderID);
        broadcast(WvsContext.partyResult(PartyResult.changePartyBoss(this, 0)));
    }

    /**
     * Adds a {@link MapleCharacter} to this Party. Will do nothing if this Party is full.
     *
     * @param chr The Char to add.
     */
    public void addPartyMember(MapleCharacter chr) {
        if (isFull()) {
            return;
        }
        PartyMember pm = new PartyMember(chr);
        if (isEmpty()) {
            setPartyLeaderID(chr.getId());
        }
        PartyMember[] partyMembers = getPartyMembers();
        boolean added = false;
        for (int i = 0; i < partyMembers.length; i++) {
            if (partyMembers[i] == null) {
                partyMembers[i] = pm;
                chr.setParty(this);
                added = true;
                break;
            }
        }
        if (added && chr.getId() != getPartyLeaderID()) {
            chr.write(WvsContext.partyResult(PartyResult.joinPartyLocal(this, pm)));
            chr.write(WvsContext.partyResult(PartyResult.loadParty(this)));
            broadcast(WvsContext.partyResult(PartyResult.joinPartyRemote(this, pm)));
        }
    }

    public TownPortal getTownPortal() {
        PartyMember pm = Arrays.stream(getPartyMembers()).filter(Objects::nonNull)
                .filter(p -> p.getTownPortal() != null)
                .findFirst().orElse(null);
        return pm != null ? pm.getTownPortal() : new TownPortal();
    }

    public PartyMember getPartyLeader() {
        return Arrays.stream(getPartyMembers()).filter(p -> p != null && p.getCharID() == getPartyLeaderID()).findFirst().orElse(null);
    }

    public boolean hasCharAsLeader(MapleCharacter chr) {
        return getPartyLeaderID() == chr.getId();
    }

    public void disband() {
        for (MapleCharacter chr : getOnlineChars()) {
            ScriptEvent eim = chr.getEventInstance();
            if (eim != null) eim.getHooks().partyMemberDischarged(chr);
        }
        broadcast(WvsContext.partyResult(PartyResult.withdrawParty(this, getPartyLeader(), false, false)));
        for (MapleCharacter chr : getOnlineChars()) {
            if(chr.getParty() != null){
                chr.setParty(null);
            }
            if (chr.getPyramidSubway() != null)
                chr.getPyramidSubway().fail(chr);
        }
        partyMembers = new PartyMember[6];
        getWorld().removeParty(this);
        setWorld(null);
    }

    public List<MapleCharacter> getOnlineChars() {
        return getOnlineMembers().stream().filter(pm -> pm.getChr() != null).map(PartyMember::getChr).collect(Collectors.toList());
    }

    public Set<MapleCharacter> getCharsInSameField(MapleCharacter chr) {
        return getOnlineChars().stream().filter(c -> c.getMap() == chr.getMap() && c.getClient().getChannel() == chr.getClient().getChannel()).collect(Collectors.toSet());
    }

    /**
     * Gets a list of party members in the same Field instance as the given Char, excluding the given Char.
     *
     * @param partyMember the given PartyMember
     * @return a set of Characters that are in the same field as the given Char
     */
    public Set<PartyMember> getPartyMembersInSameField(PartyMember partyMember) {
        return getOnlineMembers().stream()
                .filter(pm -> partyMember != null && partyMember.getChr() != null && pm.getChr() != null
                        && pm != partyMember && pm.getChr().getMapId() == partyMember.getFieldID()
                        && pm.getChr().getClient().getChannel() == partyMember.getChr().getClient().getChannel())
                .collect(Collectors.toSet());
    }

    public List<PartyMember> getOnlineMembers() {
        return Arrays.stream(getPartyMembers()).filter(pm -> pm != null && pm.isOnline()).collect(Collectors.toList());
    }

    public List<PartyMember> getMembers() {
        return Arrays.stream(getPartyMembers()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void updateFull() {
        broadcast(WvsContext.partyResult(PartyResult.loadParty(this)));
    }

    public PartyMember getPartyMemberByID(int charID) {
        return Arrays.stream(getPartyMembers()).filter(p -> p != null && p.getCharID() == charID).findFirst().orElse(null);
    }

    public void broadcast(OutPacket outPacket) {
        for (PartyMember pm : getOnlineMembers()) {
            pm.getChr().write(outPacket);
        }
    }

    public void broadcast(OutPacket outPacket, MapleCharacter exceptChar) {
        for (PartyMember pm : getOnlineMembers()) {
            if (!pm.getChr().equals(exceptChar)) {
                pm.getChr().write(outPacket);
            }
        }
    }

    public void removePartyMember(PartyMember partyMember) {
        for (int i = 0; i < getPartyMembers().length; i++) {
            PartyMember pm = getPartyMembers()[i];
            if (pm != null && pm.equals(partyMember)) {
                if (pm.getChr() != null) {
                    if (pm.getChr().getPyramidSubway() != null && pm.isOnline()) {
                        pm.getChr().getPyramidSubway().fail(pm.getChr());
                    }
                    if (pm.getChr().getEventInstance() != null && pm.isOnline())
                        pm.getChr().getEventInstance().getHooks().partyMemberDischarged(pm.getChr());
                    pm.getChr().setParty(null);
//                    if (pm.getChr().getEventInstance()!= null && pm.isOnline()){
//                        pm.getChr().getEventInstance().getHooks().playerDisconnected(pm.getChr());
//                    }
//                    pm.getChr().dropSpouseMessage(UserChatMessageType.getByType(1),"removePartyMember");
//                    eim.getHooks().partyMemberDischarged(chr);
//                    if (pm.getChr().getEventInstance() != null && pm.isOnline())
//                        pm.getChr().getEventInstance().leftParty(pm.getChr());
                }
                getPartyMembers()[i] = null;
                break;
            }
        }
    }

    public void expel(int expelID) {
        PartyMember leaver = getPartyMemberByID(expelID);
        broadcast(WvsContext.partyResult(PartyResult.withdrawParty(this, leaver, true, true)));
        removePartyMember(leaver);
        updateFull();
    }

    public static Party createNewParty(boolean appliable, boolean leaderPick, String name, World world) {
        Party party = new Party();
        party.setAppliable(appliable);
        party.setLeaderPick(leaderPick);
        party.setName(name);
        party.setWorld(world);
        world.addParty(party);
        return party;
    }

    public int getAvgLevel() {
        Collection<PartyMember> partyMembers = getMembers();
        return partyMembers.stream()
                .mapToInt(pm -> pm.getChr().getLevel())
                .sum() / partyMembers.size();
    }

    public boolean isPartyMember(MapleCharacter chr) {
        return getPartyMemberByID(chr.getId()) != null;
    }

    public void updatePartyMemberInfoByChr(MapleCharacter chr) {
        if (!isPartyMember(chr)) {
            return;
        }
        PartyMember pm = getPartyMemberByID(chr.getId());
        List<Integer> changedTypes = pm.getChangedTypes(chr);
        pm.updateInfoByChar(chr);
        for (int type : changedTypes) {
            broadcast(WvsContext.partyResult(PartyResult.setMemberData(pm, type)));
        }
        updateFull();
    }

    /**
     * Returns the average party member's level, according to the given Char's field.
     *
     * @param chr the chr to get the map to
     * @return the average level of the party in the Char's field
     */
    public int getAvgPartyLevel(MapleCharacter chr) {
        MapleMap field = chr.getMap();
        return (int) getOnlineMembers().stream().filter(om -> om.getChr().getMap() == field)
                .mapToInt(PartyMember::getLevel).average().orElse(chr.getLevel());
    }

    /**
     * Checks if this Party has a member with the given character id.
     *
     * @param charID the charID to look for
     * @return if the corresponding char is in the party
     */
    public boolean hasPartyMember(int charID) {
        return getPartyMemberByID(charID) != null;
    }

    @Override
    public void encode(OutPacket outPacket) {
        outPacket.encodeInt(id);
        outPacket.encodeByte(0);

        encodeMembers(outPacket);

        for (PartyMember pm : partyMembers) {
            outPacket.encodeInt(pm != null ? pm.getFieldID() : 0); // partychar.getChannel() != forchannel -> 999999999
        }
        for (PartyMember pm : partyMembers) {
            TownPortal townPortal;
            if (pm == null || (townPortal = pm.getTownPortal()) == null) {
                townPortal = new TownPortal();
                if (pm != null) {
                    townPortal.setTownID(999999999);
                    townPortal.setFieldID(999999999);
                    townPortal.setFieldPortal(new Position(-1, -1));
                }
            }
            townPortal.encode(outPacket, false);
        }
        outPacket.encodeBoolean(false);
        encodeStatus(outPacket);

        encodeUnknownData(outPacket);
    }

    public void encodeSimple(OutPacket outPacket) {
        outPacket.encodeByte(0);
        encodeMembers(outPacket);
        encodeUnknownData(outPacket);
    }

    public void encodeStatus(OutPacket outPacket) {
        outPacket.encodeString(name); // [1E 00 A4 40 B0 5F A8 D3 B2 D5 B6 A4 7E 21 00 65 61 72 00 1D 00 00 00 00 00 00 00 01 00 00 00 09]
        outPacket.encodeByte(isAppliable() && !isFull()); //是否公開 公開 = 1 不公開 = 0
        outPacket.encodeByte(isLeaderPick());
    }

    public void encodeMembers(OutPacket outPacket) {
        for (PartyMember pm : partyMembers) {
            if (pm == null) {
                pm = new PartyMember(null);
            }
            pm.encode(outPacket);
        }
        outPacket.encodeInt(partyLeaderID);
        List<PartyMember> unkPartyMembers = new ArrayList<>();
        outPacket.encodeInt(unkPartyMembers.size());
        for (PartyMember pm : unkPartyMembers) {
            pm.encode(outPacket);
            outPacket.encodeLong(0);
        }
    }

    public void encodeUnknownData(OutPacket outPacket) {
        int v2 = -1;
        outPacket.encodeInt(v2);
        if (v2 >= 0) {
            outPacket.encodeByte(0);
            outPacket.encodeString("");
            outPacket.encodeInt(0);
            outPacket.encodeInt(0);
            outPacket.encodeInt(0);
            outPacket.encodeInt(0);
            outPacket.encodeInt(0);
        }
        if (false) { // ????
            outPacket.encodeLong(0);
            encodeUnkData(outPacket);
        }
    }

    public void encodeUnkData(OutPacket outPacket) {
        outPacket.encodeString("");

        outPacket.encodeString("");
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeByte(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeString("");
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
    }
}
