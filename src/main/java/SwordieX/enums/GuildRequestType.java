package SwordieX.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum GuildRequestType {

    /**
     * 檢查公會名稱
     */
    Req_CheckGuildName(1), // v261
    /**
     * 同意公會建立
     */
    Req_CreateGuildAgree(2), // v261
    /**
     * 同意加入公會
     */
    Req_AllowJoin(6), // v261
    /**
     * 修改公會職稱跟權限
     */
    Req_EditGrade(14), // v261
    /**
     * 新增公會職稱及權限
     */
    Req_AddGrade(15), // v261
    /**
     * 設定公會Logo
     */
    Req_SetMark(18), // v261
    /**
     * 設定公會介紹
     */
    Req_SetNotice(19), // v261
    /**
     * 入會條件設定
     */
    Req_JoinGuildCondition(20), // v261
    /**
     * 公會招募
     */
    Req_GuildRecruitment(21), // v261
    /**
     * 提升技能等級
     */
    Req_SkillLevelSetUp(40), // v261
    /**
     * 取消公會建立
     */
    Req_CreateGuildCancel(41), // v261
    /**
     * 公會視窗的小幫手
     */
    Req_GuildHelper(46), // v261
    /**
     * 公會搜尋
     */
    Req_Search(47), // v261

    // ------------------------
    Req_LoadGuild(-2), // 0
    Req_FindGuildByCid(-2), // 1
    Req_FindGuildByGID(-2), // 2
    Req_InputGuildName(3), // 3
    Req_CreateNewGuild(6), // 6


    Req_InviteGuild(7),
    Req_JoinGuild(8),
    Req_JoinGuildDirect(9),
    Req_UpdateJoinState(10),
    Req_WithdrawGuild(11),
    Req_KickGuild(12),
    Req_RemoveGuild(13),
    Req_IncMaxMemberNum(-2), // 14

    Req_ChangeLevel(-2), // 15
    Req_ChangeJob(-2), // 16
    Req_SetGuildName(-2), // 17

    Req_InputMark(22),

    Req_CheckQuestWaiting(23),
    Req_CheckQuestWaiting2(24),
    Req_InsertQuestWaiting(25),
    Req_CancelQuestWaiting(26),
    Req_RemoveQuestCompleteGuild(27),

    Req_IncPoint(28),
    Req_IncCommitment(29),
    Req_DecGGP(30),
    Req_DecIGP(31),

    Req_SetQuestTime(32),
    Req_ShowGuildRanking(33),

    Req_SetSkill(34),
    Req_ResetGuildBattleSkill(36),
    Req_UseActiveSkill(37),
    Req_UseADGuildSkill(38),
    Req_ExtendSkill(39),
    Req_ChangeGuildMaster(-2), // 40
    Req_FromGuildMember_GuildSkillUse(41),

    Req_SetGGP(42),
    Req_SetIGP(43),

    Req_BattleSkillOpen(44),

    Req_CreateNewGuild_Block(-2), // 46
    Req_CreateNewAlliance_Block(-2), // 47
    ;
    private final int val;

    GuildRequestType(int val) {
        this.val = val;
    }

    public static GuildRequestType getTypeByVal(int val) {
        return Arrays.stream(values()).filter(grt -> grt.getVal() == val).findAny().orElse(null);
    }

}
