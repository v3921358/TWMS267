package SwordieX.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum GuildResponseType {
    /**
     * 視窗： 建立公會顯示輸入公會名稱視窗
     */
    Res_CreateGuild(0), // v261
    /**
     * 載入公會資訊
     */
    Res_LoadGuild_Done(1), // v261
    /**
     * 視窗： 是否要建立XXX公會視窗
     */
    Res_CreateGuildAgree_Reply(4), // v261
    /**
     * NPC顯示: 公會名字已經存在，會要求重新輸入
     */
    Res_NameAlreadyUsed(5), // v261
    /**
     * 建立公會
     */
    Res_CreateNewGuild_Done(7), // v261
    /**
     * 訊息: 已是加入公會的狀態。
     */
    Res_AlreadyJoinedGuild(8), // v261
    /**
     * NPC顯示: 目前有要求創立的公會。請等一下。
     */
    Res_CreatingGuildAlreadyUsed(9), // v261
    /**
     * NPC顯示: 創立公會時發生問題！請您再試一次。
     */
    Res_CreatingGuildError(10), // v261
    /**
     * NPC顯示: 創立公會時發生問題！我方會退還楓幣。
     */
    Res_CreatingGuildErrorMoney(11), // v261
    /**
     * 取消公會建立
     */
    Res_CancelGuildCreate(12), // v261
    /**
     * 訊息: 已是加入公會的狀態。
     */
    Res_AlreadyJoinedGuild2(15), // v261
    /**
     * 訊息: 您要加入的公會人數已達上限！無法再加入該公會。
     */
    Res_MemberMaximumReached(17), // v261
    /**
     * 訊息: 想要加入的公會已超過可加入的會員數
     */
    Res_ExceedTheLimit(18), // v261
    /**
     * 訊息: 該公會不接受加入申請。
     */
    Res_NotAcceptedJoin(19), // v261
    /**
     * 訊息: 在目前所在頻道找不到該角色。
     */
    Res_ChannelNotFoundUser(20), // v261
    /**
     * 訊息: 搜尋不到邀請加入的角色
     */
    Res_SearchNotFoundUser(21), // v261
    /**
     * 視窗： 玩家加入公會的邀請
     * 同時在公會視窗顯示New跟待核准
     */
    Res_JoinGuild(24), // v261
    /**
     * 訊息: 加入公會最多可申請5個。
     */
    Res_JoinGuildMaxFive(28), // v261
    /**
     * 訊息: 還不能邀請加入公會，請於限制時間過後進行。
     */
    Res_NotJoinGuildTimeBan(29), // v261
    /**
     * 訊息: 已申請加入。
     */
    Res_AlreadyAppliedJoin(30), // v261
    /**
     * 退出公會
     */
    Res_QuitGuild(33), // v261
    /**
     * 退出公會
     */
    Res_QuitGuild2(34), // v261
    /**
     * 訊息: 未加入公會
     */
    Res_NotJoinGuild(35), // v261
    /**
     * 訊息: 簽到
     */
    Res_Sign_In_Today(36), // v261
    /**
     * 退出公會
     */
    Res_QuitGuild3(37), // v261
    /**
     * 訊息: 未加入公會
     */
    Res_NotJoinGuild2(38), // v261
    /**
     * NPC顯示: 公會已解散！若想重新建立公會的話，再過來找我吧！
     */
    Res_GuildDisband(40), // v261
    /**
     * 解散公會時發生問題！請您再試一次。
     */
    Res_GuildDisbandError(42), // v261
    /**
     * 訊息： 公會已解散
     */
    Res_GuildDisbandMsg(43), // v261
    /**
     * 訊息: XXX目前是拒絕公會邀請的狀態
     */
    Res_DeclineInvitationStatusMsg(44), // v261
    /**
     * 訊息: 玩家正在處理別得事情。
     */
    Res_UserBusy(45), // v261
    /**
     * 訊息: 玩家XXX已拒絕您的公會邀請
     */
    Res_InvitationDeclined(46), // v261
    /**
     * 已邀請XXX加入公會
     */
    Res_InvitedMsg(47), // v261
    /**
     * 公會邀請
     */
    Res_Invite(48), // v261
    /**
     * 訊息: 因發生不明錯誤，未能處理公會邀請！
     */
    Res_InviteError(49), // v261
    /**
     * 訊息: 在目前所在頻道找不到該角色。
     */
    Res_ChannelNotFoundUser2(50), // v261
    /**
     * 訊息: 未加入公會
     */
    Res_NotJoinGuild3(51), // v261
    /**
     * 訊息: 已是加入公會的狀態。
     */
    Res_AlreadyJoinedGuild3(52), // v261
    /**
     * 訊息: 您要加入的公會人數已達上限！無法再邀請加入公會。
     */
    Res_InviteMemberMaximumReached(53), // v261
    /**
     * 訊息: GM角色無法創建公會
     */
    Res_GameMasterCannotCreate(54), // v261
    /**
     * NPC顯示: 恭喜!公會的總人數已增加為XX人了!希望您可以更加茁壯，到時候再來見我吧!
     */
    Res_AddMemberMaximum(56), // v261
    /**
     * 增加公會成員時發生問題!請您再試一次。
     */
    Res_AddMemberMaximumError(57), // v261
    /**
     * 更改公會成員名稱
     */
    Res_ChangeMemberName(58), // v261
    /**
     * 更改請求玩家的名稱??
     */
    Res_ChangeRequestUserName(59),
    /**
     * 公會 XXX轉職為XXX。
     * 公會 XXX達到OO級！
     */
    Res_ChangeLevelOrJob(60), // v261
    /**
     * 改變公會成員在線狀態
     */
    Res_NotifyLoginOrLogout(61), // v261
    /**
     * 公會 XXX結婚了。祝福一下。
     */
    Res_MembersMarriedMsg(62), // v261
    /**
     * 訊息: 因發生不明錯誤，未能處理公會邀請！
     */
    Res_InviteError1(64), // v261
    /**
     * 訊息: 因發生不明錯誤，未能處理公會邀請！
     */
    Res_InviteError2(65), // v261
    /**
     * 訊息: 因發生不明錯誤，未能處理公會邀請！
     */
    Res_InviteError3(66), // v261
    /**
     * 目前無法統一變更公會會員的職位，請稍後再試。
     */
    Res_CannotChangeGrade(67), // v261
    /**
     * 變更公會職位
     */
    Res_SetGrade_Done(68), // v261
    Res_SetGrade_Done1(70), //
    Res_SetGrade_Done2(72), //
    /**
     * [XXX]的職位改為[XXX]!
     */
    Res_ChangeGrade(74), // v261
    /**
     * 變更公會貢獻
     */
    Res_SetMemberCommitment_Done(76), // v261
    /**
     * 變更公會標誌
     */
    Res_SetMark_Done(78), // v261
    /**
     * 視窗: 無法產生公會標誌
     */
    Res_SetMarkError(79), // v261
    /**
     * 視窗: 無法產生公會標誌。產生條件: 公會等級2級以上 持有GP 150,000
     */
    Res_SetMarkCondition(80), // v261
    /**
     * 視窗: 無法產生公會標誌。產生條件: 公會等級2級以上 持有GP 150,000
     */
    Res_SetMarkCondition2(81), // v261
    /**
     * 視窗: 無法產生公會標誌。產生條件: 公會等級10級以上 持有GP 225,000
     */
    Res_SetMarkCondition3(82), // v261
    /**
     * 視窗: 上傳公會標誌後尚未超過10分，因此無法登記。
     */
    Res_SetMarkCondition4(83), // v261
    /**
     * 視窗: 公會等級未達10級時，無法登記公會標誌圖
     */
    Res_SetMarkCondition5(84), // v261
    /**
     * 視窗: 無法產生公會標誌。產生條件: 公會等級10級以上 持有GP 225,000
     */
    Res_SetMarkCondition6(85), // v261
    /**
     * 視窗: 圖素容量超過，登入失敗。重新確認後再試。
     */
    Res_SetMarkCondition7(86), // v261
    /**
     * 視窗: 相關功能目前是無法使用的狀態。
     */
    Res_SetMarkCondition8(87), // v261
    /**
     * 視窗: 公會介紹已變更。
     */
    Res_SetNotice_Done(88), // v261
    /**
     * 視窗: 加入公會設定已變更。
     */
    Res_JoinGuildCondition(90), // v261
    /**
     * 視窗: 加入公會宣傳已開始
     */
    Res_GuildRecruitmentStart(92), // v261
    /**
     * 更新公會資訊(總貢獻、等級、公會點數(IGP))
     * Res_IncPoint_Done
     * Res_SetGGP_Done,
     * Res_SetIGP_Done,
     */
    Res_UpdateGuildInfo(98), // v261
    /**
     * 視窗: 顯示公會排名視窗
     * Res_ShowGuildRanking,
     */
    Res_ShowGuildRankUI(99), // v261
    /**
     * 訊息: 剩餘人員不足6人而無法繼續進行遊戲。5秒後終了公會對戰。
     */
    Res_StopGildWarMsg(101), // v261
    /**
     * 訊息: 隊長離開遊戲，無法繼續進行遊戲。5秒後終了公會對戰。
     */
    Res_StopGuildWarMsg2(102), // v261
    /**
     * 訊息: 順序排在下一次要參加的公會會員。請到 XXX-X頻道公會對戰地圖上等待。
     */
    Res_GuildWarNextMemberMsg(103), // v261
    /**
     * 設定公會驗證Token(應該吧)
     */
    Res_Authkey_Update(104), // v261
    /**
     * 重置公會技能
     */
    GuildRes_SetSkill_Reset(105),
    /**
     * 升級公會技能
     */
    Res_SetSkill_Done(106), // v261
    /**
     * 視窗: 1 = 升級失敗；2 = SP重置失敗
     */
    Res_SkillUpOrRestError(107), // v261
    /**
     * 遊戲一開始發送，如果有公會，會發公會ID
     */
    Res_Unk(128),  // v261 == 127 -> v265 = 128
    /**
     * 設定公會簽到獎勵
     */
    Res_SetSignInReward(129), // v261 == 128 -> v265 = 129

    //-----------------
    Res_FindGuild_Done(49),

    Res_CheckGuildName_Available(50),
    Res_CheckGuildName_AlreadyUsed(51),
    Res_CheckGuildName_Unknown(52),


    Res_CreateGuildAgree_Unknown(54),
    //Res_CreateNewGuild_Done(55),
    Res_CreateNewGuild_AlreadyJoined(56),//42?
    Res_CreateNewGuild_GuildNameAlreayExist(57),
    Res_CreateNewGuild_Beginner(58),
    Res_CreateNewGuild_Disagree(59),
    Res_CreateNewGuild_NotFullParty(60),
    Res_CreateNewGuild_Unknown(61),

    Res_JoinGuild_Done(62),
    Res_JoinGuild_AlreadyJoined(63),
    Res_JoinGuild_AlreadyJoinedToUser(64),
    Res_JoinGuild_AlreadyFull(65),
    Res_JoinGuild_LimitRequest(66),
    Res_JoinGuild_UnknownUser(67),
    Res_JoinGuild_NonRequestFindUser(68),
    Res_JoinGuild_Unknown(69),

    Res_JoinRequest_Done(70),
    Res_JoinRequest_DoneToUser(71),
    Res_JoinRequest_AlreadyFull(72),
    Res_JoinRequest_LimitTime(73),
    Res_JoinRequest_Unknown(74),
    Res_JoinCancelRequest_Done(75),

    Res_WithdrawGuild_Done(76),
    Res_WithdrawGuild_NotJoined(77),
    Res_WithdrawGuild_Unknown(78),

    Res_KickGuild_Done(79),
    Res_KickGuild_NotJoined(80),
    Res_KickGuild_Unknown(81),

    Res_RemoveGuild_Done(82),
    Res_RemoveGuild_NotExist(83),
    Res_RemoveGuild_Unknown(84),
    Res_RemoveRequestGuild_Done(85),

    Res_InviteGuild_BlockedUser(86),
    Res_InviteGuild_BlockedRequests(87),
    Res_InviteGuild_AlreadyInvited(88),
    Res_InviteGuild_Rejected(89),

    // Bunch of Create/Join messages, copies of above?

    Res_AdminCannotCreate(97),
    Res_AdminCannotInvite(98),

    Res_IncMaxMemberNum_Done(99),
    Res_IncMaxMemberNum_Unknown(100),


    Res_SetGradeName_Unknown(106),
    Res_SetMemberGrade_Done(107),
    Res_SetMemberGrade_Unknown(108),
    Res_SetMark_Unknown(111),

    Res_InsertQuest(113),
    Res_NoticeQuestWaitingOrder(114),
    Res_SetGuildCanEnterQuest(115),


    Res_GuildQuest_NotEnoughUser(120),
    Res_GuildQuest_RegisterDisconnected(121),
    Res_GuildQuest_NoticeOrder(122),

    Res_SetSkill_Extend_Unknown(125),
    Res_SetSkill_LevelSet_Unknown(126),
    Res_SetSkill_ResetBattleSkill(127),

    Res_UseSkill_Success(128),
    Res_UseSkill_Err(129),

    Res_ChangeName_Done(130),
    Res_ChangeName_Unknown(131),
    Res_ChangeMaster_Done(132),
    Res_ChangeMaster_Unknown(133),

    Res_BlockedBehaviorCreate(134),
    Res_BlockedBehaviorJoin(135),
    Res_BattleSkillOpen(136),
    Res_GetData(137),
    Res_Rank_Reflash(138),
    Res_FindGuild_Error(139),
    Res_ChangeMaster_Pinkbean(140),

    ;
    private final int val;

    GuildResponseType(int val) {
        this.val = val;
    }

    public static GuildResponseType getTypeByVal(int val) {
        return Arrays.stream(values()).filter(grt -> grt.getVal() == val).findAny().orElse(null);
    }
}
