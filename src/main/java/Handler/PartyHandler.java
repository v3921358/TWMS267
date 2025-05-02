package Handler;

import Client.MapleCharacter;
import Config.constants.GameConstants;
import Net.server.maps.MapleMap;
import Net.server.quest.MapleQuest;
import Server.channel.ChannelServer;
import Server.world.WorldFindService;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import SwordieX.client.party.PartyResult;
import SwordieX.client.party.PartyType;
import connection.InPacket;
import connection.packet.WvsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static Opcode.Headler.InHeader.*;

public class PartyHandler {

    private static final Logger log = LoggerFactory.getLogger(PartyHandler.class);

    @Handler(op = CP_PartyInvitableSet)
    public static void handlePartyInvitableSet(MapleCharacter chr, InPacket inPacket) {
        if (inPacket.decodeByte() > 0) {
            chr.getQuestRemove(MapleQuest.getInstance(GameConstants.PARTY_INVITE));
        } else {
            chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.PARTY_INVITE));
        }
    }

    @Handler(op = CP_PartyRequest)
    public static void handlePartyRequest(MapleCharacter chr, InPacket inPacket) {
        byte type = inPacket.decodeByte();
        PartyType prt = PartyType.getByVal(type);
        if (prt == null) {
            log.error(String.format("Unknown party request type %d", type));
            return;
        }
        Party party = chr.getParty();
        switch (prt) {
            case PartyReq_CreateNewParty: { // 創建隊伍
                if (party != null) {
                    if (chr.getId() == party.getPartyLeaderID() && party.getMembers().size() == 1) {
                        chr.write(WvsContext.partyResult(PartyResult.createNewParty(party)));
                    } else {
                        chr.write(WvsContext.partyResult(PartyResult.msg(PartyType.PartyRes_CreateNewParty_Done)));
                    }
                    return;
                }
                String name = inPacket.decodeString();
                boolean appliable = inPacket.decodeByte() != 0;
                boolean leaderPick = inPacket.decodeByte() != 0;
                party = Party.createNewParty(appliable, leaderPick, name, chr.getClient().getWorld());
                party.addPartyMember(chr);
                party.broadcast(WvsContext.partyResult(PartyResult.createNewParty(party)));
                break;
            }
            case PartyReq_WithdrawParty: // 退出隊伍
                inPacket.decodeByte();
                        PartyMember leaver = party.getPartyMemberByID(chr.getId());
                        party.broadcast(WvsContext.partyResult(PartyResult.withdrawParty(party, leaver, true, false)));
                        party.removePartyMember(leaver);
                        party.disband();
                        party.updateFull();
                break;
            case PartyReq_InviteParty: { // 組隊邀請
                String invitedName = inPacket.decodeString();
                int theCh = WorldFindService.getInstance().findChannel(invitedName);
                MapleCharacter invited = null;
                if (theCh > 0) {
                    invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterByName(invitedName);
                }
                int res = 0;
                if (invited == null) {
                    res = 7;
                }
                if (res == 0) {
                    boolean allowInvite = invited.getQuestNoAdd(MapleQuest.getInstance(GameConstants.PARTY_INVITE)) == null;
                    if (!allowInvite) {
                        res = 1;
                    } else if (party != null && party.isFull()) {
                        res = 9;
                    } else if (invited.getParty() != null) {
                        res = 10;
                    }
                }
                chr.write(WvsContext.partyResult(PartyResult.invitePartySent(res, invitedName)));
                if (invited != null)
                    invited.write(WvsContext.partyResult(PartyResult.inviteParty(chr)));
                if (res != 0) return;

                if (party == null) {
                    party = Party.createNewParty(false, false, chr.getName() + "的隊伍", chr.getClient().getWorld());
                    PartyMember pm = new PartyMember(chr);
                    party.setPartyLeaderID(pm.getCharID());
                    party.getPartyMembers()[0] = pm;
                    chr.setParty(party);
                    chr.write(WvsContext.partyResult(PartyResult.createNewParty(party)));
                }
                break;
            }
            case PartyReq_KickParty: // 驅逐成員
                if (chr.getId() != party.getPartyLeaderID())
                    return;
                int expelID = inPacket.decodeInt();
                party.expel(expelID);
                break;
            case PartyReq_ChangePartyBoss: // 改變隊長
                if (chr.getId() != party.getPartyLeaderID())
                    return;
                int newLeaderID = inPacket.decodeInt();
                party.changeLeader(newLeaderID, false);
                break;
            case PartyReq_ApplyParty: //尋找組隊後退出自己的隊伍然後加入別人的隊伍
                int partyID = inPacket.decodeInt();
                if (party != null)
                    party.disband();
                party = chr.getClient().getWorld().getPartybyId(partyID);
                MapleCharacter leader = null;
                int res = 0;
                if (party != null) {
                    PartyMember pm = party.getPartyLeader();
                    if (pm == null || (leader = pm.getChr()) == null) {
                        res = 8;
                    }
                } else {
                    res = 8;
                }
                if (res == 0) {
                    if (party.isFull()) {
                        res = 9;
                    } else if (party.getApplyingChar() == chr) {
                        res = 3;
                    } else if (party.getApplyingChar() != null) {
                        res = 2;
                    }
                }
                chr.write(WvsContext.partyResult(PartyResult.applyPartySent(res, leader == null ? "" : leader.getName())));
                if (res != 0) return;

                party.setApplyingChar(chr);
                party.getPartyLeader().getChr().write(WvsContext.partyResult(PartyResult.applyParty(chr)));
                break;
            case PartyReq_PartySetting: //修改隊伍設置
                if (party == null || party.getPartyLeaderID() != chr.getId()) {
                    chr.write(WvsContext.partyResult(PartyResult.msg(PartyType.Unknown)));
                    return;
                }
                String name = inPacket.decodeString();
                boolean appliable = inPacket.decodeByte() != 0;
                boolean leaderPick = inPacket.decodeByte() != 0;
                party.setName(name);
                party.setAppliable(appliable);
                party.setLeaderPick(leaderPick);
                party.broadcast(WvsContext.partyResult(PartyResult.settingChange(party)));
                break;
        }
    }

    @Handler(op = CP_PartyResult)
    public static void handlePartyResult(MapleCharacter chr, InPacket inPacket) {
        byte type = inPacket.decodeByte();
        PartyType pt = PartyType.getByVal(type);
        if (pt == null) {
            log.error(String.format("Unknown party request result type %d", type));
            return;
        }
        switch (pt) {
            case PartyRes_InviteParty_Sent: {
                int op = inPacket.decodeInt();
                if (op != 4 && op != 5) {
                    return;
                }
                int leaderID = inPacket.decodeInt();
                if (chr.getParty() != null) {
                    chr.write(WvsContext.partyResult(PartyResult.msg(PartyType.PartyRes_JoinParty_Remote_Done)));
                    return;
                }
                int theCh = WorldFindService.getInstance().findChannel(leaderID);
                MapleCharacter invited = null;
                if (theCh > 0) {
                    invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterById(leaderID);
                }
                if (invited == null) {
                    return;
                }
                if (op == 5) {
                    invited.write(WvsContext.partyResult(PartyResult.invitePartySent(op, chr.getName())));
                    return;
                }
                Party party = invited.getParty();
                if (party == null) {
                    log.error("party is null, leaderID:" + leaderID);
                    return;
                }
                if (!party.isFull()) {
                    party.addPartyMember(chr);
                    chr.receivePartyMemberHP();
                    chr.updatePartyMemberHP();
                } else {
                    chr.write(WvsContext.partyResult(PartyResult.msg(PartyType.PartyRes_JoinParty_AlreadyFull)));
                }
                break;
            }
            case PartyRes_ApplyParty_Sent:
                int op = inPacket.decodeInt();
                if (op != 4 && op != 5) {
                    return;
                }
                if (chr.getParty() != null) {
                    chr.write(WvsContext.partyResult(PartyResult.msg(PartyType.PartyRes_JoinParty_AlreadyJoined)));
                    return;
                }
                if (chr.getParty().getPartyLeader().getChr() != chr) {
                    return;
                }
                MapleCharacter applier = chr.getParty().getApplyingChar();
                if (applier == null) {
                    return;
                }
                if (op == 4) {
                    chr.getParty().setApplyingChar(null);
                    applier.write(WvsContext.partyResult(PartyResult.applyPartySent(op, chr.getName())));
                    return;
                }
                if (!chr.getParty().isFull()) {
                    chr.getParty().addPartyMember(applier);
                    applier.receivePartyMemberHP();
                    applier.updatePartyMemberHP();
                } else {
                    chr.write(WvsContext.partyResult(PartyResult.msg(PartyType.PartyRes_JoinParty_AlreadyFull)));
                }
                break;
        }
    }

    @Handler(op = CP_PartyMemberCandidateRequest)
    public static void handlePartyMemberCandidateRequest(MapleCharacter chr, InPacket inPacket) {
        if (chr.isInBlockedMap()) {
            chr.dropMessage(5, "無法在這個地方進行搜索.");
            chr.sendEnableActions();
            return;
        }
        MapleMap field = chr.getMap();
        chr.write(WvsContext.partyMemberCandidateResult(field.getCharacters().stream()
                .filter(ch -> !ch.isHidden() && ch.getQuestNoAdd(MapleQuest.getInstance(GameConstants.PARTY_INVITE)) == null && !ch.equals(chr) && ch.getParty() == null)
                .collect(Collectors.toSet())));
    }

    @Handler(op = CP_UrusPartyMemberCandidateRequest)
    public static void handlePartyCandidateRequest(MapleCharacter chr, InPacket inPacket) {
        if (chr.isInBlockedMap()) {
            chr.dropMessage(5, "無法在這個地方進行搜索.");
            chr.sendEnableActions();
            return;
        }
        if (chr.getParty() != null) {
            chr.write(WvsContext.partyCandidateResult(new HashSet<>()));
            return;
        }
        MapleMap field = chr.getMap();
        Set<Party> parties = new HashSet<>();
        for (MapleCharacter ch : field.getCharacters()) {
            if (!ch.isHidden() && ch.getParty() != null && ch.getParty().hasCharAsLeader(ch) && !ch.getParty().isAppliable()) {
                parties.add(ch.getParty());
            }
        }
        chr.write(WvsContext.partyCandidateResult(parties));

    }
}
