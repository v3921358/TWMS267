package SwordieX.client.party;

import Client.MapleCharacter;
import connection.Encodable;
import connection.OutPacket;
import tools.data.MaplePacketLittleEndianWriter;

public class PartyResult implements Encodable {

    private final PartyType type;
    private Party party;
    private PartyMember member;
    private MapleCharacter chr;
    private TownPortal townPortal;
    private int arg1, arg2;
    private boolean bool, bool2;
    private String str;

    public PartyResult(PartyType type) {
        this.type = type;
    }

    @Override
    public void encode(OutPacket outPacket) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        outPacket.encodeByte(type.getVal());
        switch (type) {
            case PartyReq_InviteParty:
            case PartyReq_InviteIntrusion:   //邀請組隊 OK
                outPacket.encodeInt(chr.getAccountID());
                outPacket.encodeInt(chr.getId());
                outPacket.encodeString(chr.getName());
                outPacket.encodeInt(chr.getLevel());
                outPacket.encodeInt(chr.getJob());
                outPacket.encodeInt(0);
                break;
            case PartyReq_ApplyParty:
                outPacket.encodeInt(party.getPartyLeader().getChr().getAccountID());
                outPacket.encodeInt(party.getPartyLeader().getCharID());
                outPacket.encodeString(party.getPartyLeader().getCharName());
                outPacket.encodeInt(party.getPartyLeader().getLevel());
                outPacket.encodeInt(party.getPartyLeader().getJob());
                outPacket.encodeInt(0);
                break;
            case PartyRes_LoadParty_Done: {
                outPacket.encodeByte(arg1);
                if (arg1 > 0) {
                    party.encode(outPacket);
                } else {
                    outPacket.encodeByte(arg2);
                    if (arg2 > 0) {
                        outPacket.encodeInt(0);

                        party.encodeSimple(outPacket);
                    }
                }
                break;
            }
            case PartyRes_CreateNewParty_Done:
                outPacket.encodeInt(party.getId());
                outPacket.encodeByte(0);

                party.getTownPortal().encode(outPacket, true); // short here, but int in the decodeBuffer

                party.getPartyLeader().encode(outPacket);
                party.encodeStatus(outPacket);
                party.encodeUnknownData(outPacket);

                outPacket.encodeInt(party.getPartyLeaderID());
                break;
            case PartyRes_CreateNewParty_AlreayJoined:
                break;
            case PartyRes_WithdrawParty_Done:
                outPacket.encodeInt(party.getPartyLeaderID());
                outPacket.encodeByte(bool);
                break;
            case PartyRes_WithdrawParty_NotJoined:
                break;
            case PartyRes_JoinParty_Remote_Done:
                outPacket.encodeString(member.getCharName()); // sJoinerName
                outPacket.encodeByte(0); // new 188
                outPacket.encodeInt(0); // new 208
                party.encode(outPacket);
                break;
            case PartyRes_JoinParty_Local_Done:
                outPacket.encodeByte(0);
                outPacket.encodeInt(0);
                outPacket.encodeByte(0);
                break;
            case PartyRes_JoinParty_AlreadyJoined:
            case PartyRes_JoinParty_AlreadyFull:
            case PartyRes_JoinParty_OverDesiredSize:
            case UserInLimitField:
            case PartyRes_JoinIntrusion_UnknownParty:
                break;
            case PartyRes_InviteParty_Sent:
            case PartyRes_InviteIntrusion_Sent:
            case PartyRes_ApplyParty_Sent:
                outPacket.encodeInt(arg1);
                outPacket.encodeString(str);
                break;
            case FieldLimit:
                break;
            case Unk45:
                outPacket.encodeString("");
                break;
            case PartyRes_FoundPossibleMember:
            case PartyRes_KickParty_FieldLimit:
            case KickParty_Limit:
                break;
            case PartyRes_ChangePartyBoss_Done:
                outPacket.encodeInt(member.getCharID());
                outPacket.encodeByte(arg1); // nReason
                break;
            case PartyRes_ChangePartyBoss_NotSameField:
            case PartyRes_ChangePartyBoss_NoMemberInSameField:
            case PartyRes_ChangePartyBoss_NotSameChannel:
            case ChangePartyBoss_TimeOut:
            case Unk58:
            case Unk59:
                break;
            case SetMemberData:
                outPacket.encodeByte(1);
                outPacket.encodeInt(member.getCharID());
                outPacket.encodeInt(arg1);
                member.encodeVal(outPacket, arg1);
                break;
            case PartyRes_CanNotInThisField:
                break;
            case PartyRes_PartySettingDone:
                party.encodeStatus(outPacket);
                break;
            case InviteIntrusion_CantUse:
                break;
            case Unk65:
                int b2 = 0;
                outPacket.encodeInt(b2);
                if (b2 > 0) {
                    if (b2 != 1) {
                        if (b2 != 2 && b2 == 9) {
                            party.encodeUnkData(outPacket);
                        } else {
                            party.encodeUnknownData(outPacket);
                            int cout1 = 0;
                            outPacket.encodeInt(cout1);
                            for (int i = 0; i < cout1; i++) {
                                outPacket.encodeInt(0);
                            }
                        }
                    }
                } else {
                    party.encodeUnknownData(outPacket);
                }
                break;
            case Unk66:
                outPacket.encodeInt(0);
                break;
            case Unk67:
            case Unk69:
                int v1 = 0;
                outPacket.encodeInt(v1);
                if (v1 == 14) {
                    outPacket.encodeInt(0);
                    outPacket.encodeByte(0);
                    int cout1 = 0;
                    outPacket.encodeInt(cout1);
                    for (int i = 0; i < cout1; i++) {
                        outPacket.encodeString("");
                    }
                }
                break;
            case Unk68:
                outPacket.encodeInt(0);
                break;
            case Unk70:
                int v2 = 0;
                outPacket.encodeInt(v2);
                switch (v2) {
                    case 0:
                        outPacket.encodeString("");
                        outPacket.encodeInt(0);

                        party.encodeSimple(outPacket);
                        break;
                    case 1:
                        break;
                    case 3:
                        outPacket.encodeString("");
                        break;
                    case 4:
                        outPacket.encodeString("");
                        outPacket.encodeInt(0);
                        break;
                    case 8:
                        outPacket.encodeInt(0);
                        break;
                    case 9:
                    case 10:
                    case 14:
                    case 15:
                    case 18:
                    case 19:
                    default:
                        break;
                }
                break;
            case Unk71:
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeByte(0);
                break;
            case Unk72:
                outPacket.encodeInt(0);
                break;
            case Unk74:
                // sub_141A7A630
                int v3 = 0;
                outPacket.encodeInt(v3);
                switch (v3) {
                    case 1:
                        outPacket.encodeInt(0);
                        outPacket.encodeByte(0);
                        outPacket.encodeString("");
                        break;
                    case 2:
                        outPacket.encodeInt(0);
                        party.encodeUnknownData(outPacket);
                        break;
                    case 3:
                        outPacket.encodeInt(0);
                        outPacket.encodeByte(0);
                        member.encode(outPacket);
                        break;
                    case 4:
                        outPacket.encodeInt(0);
                        outPacket.encodeInt(0);
                        break;
                    case 5:
                        outPacket.encodeInt(0);
                        outPacket.encodeByte(0);
                        outPacket.encodeInt(0);
                        int t1 = 0;
                        outPacket.encodeInt(t1);
                        member.encodeVal(outPacket, t1);
                        break;
                    case 6:
                        outPacket.encodeInt(0);
                        outPacket.encodeInt(0);
                        break;
                    case 7:
                        outPacket.encodeInt(0);
                        outPacket.encodeInt(0);
                        break;
                    case 8:
                        outPacket.encodeInt(0);
                        sub_1408AF7D0(outPacket);
                        break;
                    case 9:
                        outPacket.encodeInt(0);
                        party.encodeUnkData(outPacket);
                        break;
                    case 10:
                        sub_141A7F2F0(outPacket);
                        break;
                    case 11:
                        outPacket.encodeInt(0);
                        boolean b3 = false;
                        outPacket.encodeByte(b3);
                        if (b3) {
                            party.encodeSimple(outPacket);
                        } else {
                            sub_141A7F2F0(outPacket);
                        }
                        break;
                }
                break;
            case Unk75:
                int v4 = 0;
                outPacket.encodeInt(v4);
                if (v4 == 7) {
                    outPacket.encodeInt(0);
                } else if (v4 == 8) {
                    sub_1408AF7D0(outPacket);
                }
                break;
            case Unk76:
                outPacket.encodeByte(0);
                break;
            case PartyInfo_TownPortalChanged:
                outPacket.encodeByte(arg1);
                townPortal.encode(outPacket, true);
                break;
        }
    }

    public void sub_141A7F2F0(OutPacket outPacket) {
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        party.encodeSimple(outPacket);
    }

    public void sub_1408AF7D0(OutPacket outPacket) {
        member.encode(outPacket);
        outPacket.encodeLong(0);
    }

    public static PartyResult createNewParty(Party party) {
        PartyResult pr = new PartyResult(PartyType.PartyRes_CreateNewParty_Done);
        pr.party = party;
        return pr;
    }

    public static PartyResult loadParty(Party party) {
        PartyResult pr = new PartyResult(PartyType.PartyRes_LoadParty_Done);
        pr.party = party;
        pr.arg1 = 0;
        pr.arg2 = 0;
        return pr;
    }

    public static PartyResult changePartyBoss(Party party, int reason) {
        PartyResult pr = new PartyResult(PartyType.PartyRes_ChangePartyBoss_Done);
        pr.party = party;
        pr.member = party.getPartyLeader();
        pr.arg1 = reason;
        return pr;
    }

    public static PartyResult withdrawParty(Party party, PartyMember kickedMember, boolean partyStillExists, boolean expelled) {
        PartyResult pr = new PartyResult(PartyType.PartyRes_WithdrawParty_Done);
        pr.party = party;
        pr.member = kickedMember;
        pr.bool = partyStillExists;
        pr.bool2 = expelled;
        return pr;
    }

    public static PartyResult joinPartyLocal(Party party, PartyMember member) {
        PartyResult pr = new PartyResult(PartyType.PartyRes_JoinParty_Local_Done);
        pr.party = party;
        pr.member = member;
        return pr;
    }

    public static PartyResult joinPartyRemote(Party party, PartyMember member) {
        PartyResult pr = new PartyResult(PartyType.PartyRes_JoinParty_Remote_Done);
        pr.party = party;
        pr.member = member;
        return pr;
    }

    public static PartyResult msg(PartyType type) {
        return new PartyResult(type);
    }

    public static PartyResult inviteParty(MapleCharacter chr) {
        PartyResult pr = new PartyResult(PartyType.PartyReq_InviteParty);
        pr.chr = chr;
        return pr;
    }

    public static PartyResult inviteIntrusion(MapleCharacter chr) {
        PartyResult pr = new PartyResult(PartyType.PartyReq_InviteIntrusion);
        pr.chr = chr;
        return pr;
    }

    public static PartyResult applyParty(MapleCharacter chr) {
        PartyResult pr = new PartyResult(PartyType.PartyReq_ApplyParty);
        pr.chr = chr;
        return pr;
    }

    public static PartyResult invitePartySent(int arg1, String str) {
        PartyResult pr = new PartyResult(PartyType.PartyRes_InviteParty_Sent);
        /*
         * 0 = 邀請'%s'加入組隊。
         * 1 = '%s'玩家目前為拒絕組隊狀態。
         * 2 = %s'正在處理別的事情。
         * 3 = 已經邀請'%s'至隊伍。
         * 4 = '%s'玩家拒絕了組隊邀請。
         * 5 = null
         * 6 = null
         * 7 = 現在在伺服器無法找到'%s'。
         * 8 = 找不到組隊，請再次確認組隊資訊。
         * 9 = 組隊成員已滿。
         * 10 = '%s'已有加入的隊伍。
         * */
        pr.arg1 = arg1;
        pr.str = str;
        return pr;
    }

    public static PartyResult applyPartySent(int arg1, String str) {
        PartyResult pr = new PartyResult(PartyType.PartyRes_ApplyParty_Sent);
        /*
         * 0 - 已向'%s'申請加入組隊。
         * 1 - %s現在為拒絕組隊加入申請狀態。
         * 2 - %s'玩家正在處理別的事情。
         * 3 - 已申請加入'%s'的組隊。
         * 4 - %s拒絕了組隊加入申請。
         * 5 - null
         * 6 - null
         * 7 = 現在在伺服器無法找到'%s'。
         * 8 = 找不到組隊，請再次確認組隊資訊。
         * 9 = 組隊成員已滿。
         * 10 = '%s'已有加入的隊伍。
         * */
        pr.arg1 = arg1;
        pr.str = str;
        return pr;
    }

    public static PartyResult setMemberData(PartyMember member, int type) {
        PartyResult pr = new PartyResult(PartyType.SetMemberData);
        pr.member = member;
        pr.arg1 = type;
        return pr;
    }

    public static PartyResult settingChange(Party party) {
        PartyResult pr = new PartyResult(PartyType.PartyRes_PartySettingDone);
        pr.party = party;
        return pr;
    }


    public static PartyResult townPortalChanged(int animation, TownPortal townPortal) {
        PartyResult pr = new PartyResult(PartyType.PartyInfo_TownPortalChanged);
        pr.townPortal = townPortal;
        pr.arg1 = animation;
        return pr;
    }
}
