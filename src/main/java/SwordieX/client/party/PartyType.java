package SwordieX.client.party;

import SwordieX.util.Util;

public enum PartyType {

    PartyReq_LoadParty(0),
    PartyReq_CreateNewParty(1),
    PartyReq_WithdrawParty(3),
    PartyReq_InviteParty(4),
    PartyReq_InviteIntrusion(5),
    PartyReq_KickParty(6),
    PartyReq_ChangePartyBoss(7),
    PartyReq_ApplyParty(8),
    PartyReq_SetAppliable(9),
    PartyReq_ClearIntrusion(10),
    PartyReq_CreateNewParty_Group(11),
    PartyReq_JoinParty_Group(12),
    PartyReq_PartySetting(13),

    PartyRes_LoadParty_Done(28),
    PartyRes_CreateNewParty_Done(29),
    // 已經加入其他組。
    PartyRes_CreateNewParty_AlreayJoined(31),
    PartyRes_WithdrawParty_Done(32),
    // 沒有參加的組隊。
    PartyRes_WithdrawParty_NotJoined(33),
    PartyRes_JoinParty_Remote_Done(34),
    PartyRes_JoinParty_Local_Done(35),
    // 已經加入其他組。
    PartyRes_JoinParty_AlreadyJoined(36),
    // 組隊成員已滿。
    PartyRes_JoinParty_AlreadyFull(37),
    PartyRes_JoinParty_OverDesiredSize(38),
    // 因發生不明錯誤，未能處理組隊邀請！
    PartyRes_JoinParty_Unknown(39),
    // 對方在無法進行行動的地圖上。
    UserInLimitField(39),
    // 因發生不明錯誤，未能處理組隊邀請！
    Unknown(40),
    // 找不到組隊，請再次確認組隊資訊。
    PartyRes_JoinIntrusion_UnknownParty(42),  // V263
    PartyRes_InviteParty_Sent(43),  // V263
    PartyRes_InviteIntrusion_Sent(44),  // V263
    PartyRes_ApplyParty_Sent(45),  // V263
    // 目前地圖上無法進行。
    FieldLimit(45),
    Unk45(46),
    // 無法邀請正在進行自動配對的玩家至隊伍。
    PartyRes_FoundPossibleMember(47),
    // 無法向正在進行自動配對的玩家發送加入隊伍請求。
    PartyRes_FoundPossibleParty(48),
    // 退出和剔除組隊功能受限的地圖。
    PartyRes_KickParty_FieldLimit(49),
    // 因發生不明錯誤，未能處理組隊邀請！
    PartyRes_KickParty_Unknown(50),
    // 剔除隊員功能有限制。
    KickParty_Limit(51),
    // 因發生不明錯誤，未能處理組隊邀請！
    Unk51(52),
    PartyRes_ChangePartyBoss_Done(53),
    // 只能轉讓給位在相同場所的隊員！
    PartyRes_ChangePartyBoss_NotSameField(54),
    // 目前沒有其他隊員在與隊長相同區域！無法進行轉讓。
    PartyRes_ChangePartyBoss_NoMemberInSameField(55),
    // 只能轉讓給位在相同頻道內的隊員唷！
    PartyRes_ChangePartyBoss_NotSameChannel(56),
    // 因發生不明錯誤，未能處理組隊邀請！
    PartyRes_ChangePartyBoss_Unknown(57),
    // 已超過團隊隊長入場的冷卻時間，因此選定新的團隊隊長。
    ChangePartyBoss_TimeOut(58),

    Unk58(59),
    Unk59(60),
    SetMemberData(61),
    // 因發生不明錯誤，未能處理組隊邀請！
    Unknown1(62),
    // 在現在地圖無法進行。
    PartyRes_CanNotInThisField(63),
    PartyRes_PartySettingDone(64),
    // 目前無法使用招募功能。
    InviteIntrusion_CantUse(65),
    Unk65(66),
    Unk66(67),
    Unk67(68),
    Unk68(69),
    Unk69(70),
    Unk70(71),
    Unk71(72),
    Unk72(73),
    // 因發生不明錯誤，未能處理組隊邀請！
    Unknown2(74),
    Unk74(75),
    Unk75(76),
    Unk76(77),
    // 因發生不明錯誤，未能處理組隊邀請！
    Unknown3(78),
    PartyInfo_TownPortalChanged(79),
    No(-1);

    private final byte val;

    PartyType(int val) {
        this.val = (byte) val;
    }

    public static PartyType getByVal(byte type) {
        return Util.findWithPred(values(), v -> v.getVal() == type);
    }

    public byte getVal() {
        return val;
    }
}
