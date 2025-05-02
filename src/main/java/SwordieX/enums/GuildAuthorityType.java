package SwordieX.enums;

import lombok.Getter;

public enum GuildAuthorityType {
    /**
     * 邀請公會成員
     */
    Invite(0x1),
    /**
     * 編輯公會介紹
     */
    EditNotice(0x2),
    /**
     * 變更公會成員職位
     */
    ChangeGrade(0x4),
    /**
     * 編輯公會標誌
     */
    EditMark(0x8),
    /**
     * 驅逐公會成員
     */
    Kick(0x10),
    /**
     * 管理公會佈告欄
     */
    BulletinBoard(0x20),
    /**
     * 同意公會成員加入
     */
    AllowJoin(0x40),
    /**
     * 管理公會技能
     */
    ManageSkill(0x100),
    /**
     * 使用公會技能
     */
    UseSkill(0x400),
    /**
     * 進入公會城
     */
    EnterGuildTown(0x1, true),
    /**
     * 使用公會城房間功能
     */
    UseGuildTownRoom(0x2, true);

    @Getter
    private final int val;
    private final boolean isTown;

    GuildAuthorityType(int v) {
        val = v;
        isTown = false;
    }

    GuildAuthorityType(int v, boolean istown) {
        val = v;
        isTown = istown;
    }

    public static boolean hasAuthority(int flag, GuildAuthorityType type) {
        return (flag & type.getVal()) == 1;
    }
}
