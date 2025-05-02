package Opcode.Opcode;

import tools.data.WritableIntValueHolder;

public enum GuildOpcode implements WritableIntValueHolder {

    GuildReq_BattleSkillOpen(-1),
    GuildReq_RemoveGuild(-1),
    嘗試輸入建立公會名稱(0),
    GuildReq_LoadGuild(0),
    送出檢查公會名稱(1),
    //2
    //3
    //4
    //5
    公會名稱_是否使用(4),
    公會名稱_已被使用(5),
    GuildReq_WithdrawGuild(6),
    GuildReq_KickGuild(7),
    //8
    //9
    GuildReq_SetMemberGrade(10),
    //11
    GuildReq_SetGradeNameAndAuthority(12),
    //13
    //14
    //15
    GuildReq_SetMark(16),
    GuildReq_SetNotice(17),
    GuildReq_InputMark(18),
    GuildReq_AddGuildAD(19),
    //20
    //21
    //22
    //23
    //24
    //25
    //26
    //27
    //28
    GuildReq_ResetGuildSkill(29),
    GuildReq_ChangeGuildMaster(30),
    //31
    GuildReq_FindGuildByGID(32),
    GuildReq_LoadMyApplicationList(33),
    GuildReq_InviteGuild(34),
    GuildReq_SignInIn(35),
    //36
    GuildReq_SetNewNotice(37),
    //38
    GuildReq_Search(39),
    GuildReq_FindGuildByCid(40),
    GuildRes_InputGuildName(41),
    GuildRes_CreateGuildAgree(42),
    取消創建(42),
    GuildRes_CheckQuestWaiting2(43),
    GuildReq_SkillLevelSetUp(44),
    GuildReq_UseGuildSkill(45),
    //46
    //47
    GuildRes_CreateNewGuild_Block(48),
    GuildRes_CreateNewAlliance_Block(49),
    GuildReq_SetRank(50),
    //51
    GuildReq_GuildHelp(52),
    GuildRes_LoadGuild_Done(53),
    //54
    GuildRes_FindGuild_Done(55),
    GuildRes_CheckGuildName_Available(56),
    GuildRes_CheckGuildName_AlreadyUsed(57),
    GuildRes_CheckGuildName_Unknown(58),
    GuildRes_CreateGuildAgree_Reply(59),
    //60
    GuildRes_CreateNewGuild_Done(61),
    GuildRes_CreateNewGuild_AlreayJoined(62),
    GuildRes_CreateNewGuild_GuildNameAlreayExist(63),
    GuildRes_CreateNewGuild_Beginner(63),
    GuildRes_CreateNewGuild_Disagree(65),
    GuildRes_CreateNewGuild_NotFullParty(66),
    GuildRes_CreateNewGuild_Unknown(67),
    GuildRes_Unk181Add78(68),
    GuildRes_JoinGuild_Done(69),
    GuildRes_JoinGuild_AlreadyJoined(70),
    //71
    GuildRes_JoinGuild_AlreadyFull(72),
    GuildRes_JoinGuild_ApplicantListFull(73),
    GuildRes_JoinGuild_Set_Refuse(74),
    GuildRes_JoinGuild_UnknownUser(75),
    GuildRes_JoinGuild_NonRequestFindUser(76),
    //77
    GuildRes_JoinRequest_Done(78),
    GuildRes_JoinRequest_DoneToUser(79),
    GuildRes_JoinRequest_AddListDone(80),
    //81
    GuildRes_JoinRequest_AlreadyFull(82),
    GuildRes_JoinRequest_LimitTime(83),
    //84
    GuildRes_JoinRequest_Unknown(85),
    GuildRes_JoinCancelRequest_Done(86),
    GuildRes_WithdrawGuild_Done(87),
    //88
    GuildRes_WithdrawGuild_NotJoined(89),
    GuildRes_WithdrawGuild_Unknown(90),
    GuildRes_KickGuild_Done(91),
    GuildRes_KickGuild_NotJoined(92),
    GuildRes_KickGuild_Unknown(93),
    GuildRes_RemoveGuild_Done(94),
    GuildRes_RemoveGuild_NotExist(95),
    GuildRes_RemoveGuild_Unknown(96),
    GuildRes_RemoveRequestGuild_Done(97),
    GuildRes_InviteGuild_BlockedUser(98),
    GuildRes_InviteGuild_Set_Rejected(99),
    GuildRes_InviteGuild_UserBusy(100),
    GuildRes_InviteGuild_Rejected(101),
    GuildRes_InviteGuild_Invited(102),
    GuildRes_InviteGuild_Done(103),
    GuildRes_InviteGuild_Unknown(104),
    GuildRes_InviteGuild_NotJoinGuild(105),
    GuildRes_InviteGuild_NonRequestFindUser(106),
    GuildRes_InviteGuild_AlreadyJoined(107),
    GuildRes_InviteGuild_NoAnyMore(108),
    GuildRes_AdminCannotCreate(109),
    GuildRes_AdminCannotInvite(110),
    GuildRes_IncMaxMemberNum_Done(111),
    GuildRes_IncMaxMemberNum_Unknown(112),
    GuildRes_ChangeMemberName(113),
    GuildRes_ChangeRequestUserName(114),
    GuildRes_ChangeLevelOrJob(115),
    GuildRes_NotifyLoginOrLogout(116),
    //117
    //118
    //119
    //120
    //121
    GuildRes_SetGradeNameAndAuthority_Done(122),
    GuildRes_SetGradeNameAndAuthority_Unknown(123),
    //124
    //125
    //126
    GuildRes_SetMemberGrade_Done(129), // v264 = 128 -> v265 = 129
    GuildRes_SetMemberGrade_Unknown(130),  // v265
    GuildRes_SetMemberCommitment_Done(131),  // v265
    //131
    GuildRes_SetMark_Done(132),
    GuildRes_SetMark_Unknown(133),
    //134 GP不足無法更換圖標
    GuildRes_SetMark_Limit_Lv2(135),
    GuildRes_SetMark_Limit_Lv10(136),
    GuildRes_SetMark_TimeLimit(137),
    GuildRes_SetMark_Level_Limit(138),
    //139
    GuildRes_SetMark_Unavailable(140),
    //141 取消公會申請
    GuildRes_SetNotice_Done(142),
    //143
    GuildRes_SetGuildSettingUpdateDone(144),
    //145
    GuildRes_SetGuildADDone(146),
    //147
    //148
    GuildRes_InsertQuest(149),
    GuildRes_NoticeQuestWaitingOrder(150),
    GuildRes_SetGuildCanEnterQuest(151),
    GuildRes_IncPoint_Done(152),
    GuildRes_ShowGuildRanking(153),
    GuildRes_SetGGP_Done(154),
    GuildRes_GuildQuest_NotEnoughUser(155),
    GuildRes_GuildQuest_RegisterDisconnected(156),
    GuildRes_GuildQuest_NoticeOrder(157),
    GuildRes_Authkey_Update(158),
    GuildRes_SetSkill_Reset(159),
    GuildRes_SetSkill_Done(160),
    GuildRes_SetSkill_Extend_Unknown(161),
    GuildRes_SetSkill_LevelSet_Unknown(162),
    GuildRes_SetSkill_SpecialSkillReset(163),
    GuildRes_UseSkill_SpecialSkillResetErr(164),
    GuildRes_UseSkill_ResetNoEnoughGP(165),
    GuildRes_UseSkill_ResetDone(166),
    GuildRes_UseSkill_ResetError(167),
    //168
    //169
    GuildRes_UseSkill_Success(170),
    //171
    //172
    GuildRes_ChangeName_Done(173),
    GuildRes_ChangeName_Unknown(174),
    GuildRes_ChangeMaster_Done(175),
    GuildRes_ChangeMaster_Unknown(176),
    GuildRes_BlockedBehaviorCreate(177),
    GuildRes_BlockedBehaviorJoin(178),
    GuildRes_BattleSkillOpen(179),
    //180
    //181
    //182
    GuildRes_SignInResult(183),
    GuildRes_SignInInfoYesterday(184),
    //185
    GuildRes_SignInInfo(186),
    GuildRes_SignInAlready(187),
    GuildRes_LoadApplyListDone(188);

    private short code;

    GuildOpcode(final int code) {
        this.code = (short) code;
    }

    @Override
    public short getValue() {
        return code;
    }

    @Override
    public short getCode() {
        return code;
    }

    @Override
    public void setValue(final short code) {
        this.code = code;
    }

    @Override
    public void setValue(Short code) {

    }

    public static GuildOpcode getByAction(short code) {
        for (GuildOpcode it : values()) {
            if (it.code == code) {
                return it;
            }
        }
        return null;
    }
}
