package connection.packet;

import Client.MapleCharacter;
import Opcode.Headler.OutHeader;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import SwordieX.client.party.PartyResult;
import connection.OutPacket;

import java.util.Set;

public class WvsContext {

    public static OutPacket partyResult(PartyResult pri) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_PartyResult);
        outPacket.encode(pri);
        return outPacket;
    }


    public static OutPacket partyMemberCandidateResult(Set<MapleCharacter> chars) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_PartyMemberCandidateResult);

        outPacket.encodeByte(chars.size());
        for (MapleCharacter chr : chars) {
            outPacket.encodeInt(chr.getId());
            outPacket.encodeString(chr.getName());
            outPacket.encodeInt(chr.getJob());
            outPacket.encodeInt(chr.getSubcategory());
            outPacket.encodeInt(chr.getLevel());
        }

        return outPacket;
    }

    public static OutPacket partyCandidateResult(Set<Party> parties) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_PartyCandidateResult);

        outPacket.encodeByte(parties.size());
        for (Party party : parties) {
            MapleCharacter leader = party.getPartyLeader().getChr();
            outPacket.encodeInt(party.getId());
            outPacket.encodeString(leader.getName());
            outPacket.encodeInt(leader.getLevel());
            outPacket.encodeByte(leader.isOnline());
            outPacket.encodeString(party.getName());
            outPacket.encodeByte(party.getMembers().size());
            for (PartyMember pm : party.getMembers()) {
                outPacket.encodeInt(pm.getCharID());
                outPacket.encodeString(pm.getCharName());
                outPacket.encodeInt(pm.getJob());
                outPacket.encodeInt(pm.getSubJob());
                outPacket.encodeInt(pm.getLevel());
                outPacket.encodeByte(pm.isOnline());
                outPacket.encodeByte(-1); // new 200
            }
        }

        return outPacket;
    }
}
