package Opcode.Opcode;

public enum EffectOpcode {

    // 等級提升
    UserEffect_LevelUp(0),
    // 近端技能特效
    UserEffect_SkillUse(1),
    // 遠端技能特效
    UserEffect_SkillUseBySummoned(2),
    // 3
    // 特殊技能特效
    UserEffect_SkillAffected(4),
    // 機甲戰神-輔助機器特效
    UserEffect_SkillAffected_Ex(5),
    UserEffect_SkillAffected_Select(6),
    UserEffect_SkillSpecialAffected(7),
    // 物品獲得/丟棄文字特效
    UserEffect_Quest(8),
    // 寵物等級提升
    UserEffect_Pet(9),
    // 技能飛行體特效
    UserEffect_SkillSpecial(10),
    // 抵抗異常狀態
    UserEffect_Resist(11),
    // 使用護身符
    UserEffect_ProtectOnDieItemUse(12),
    // 13
    UserEffect_PlayPortalSE(14),
    // 職業變更
    UserEffect_JobChanged(15),
    // 任務完成
    UserEffect_QuestComplete(16),
    // 回復特效(Byte)
    UserEffect_IncDecHPEffect(17),
    UserEffect_BuffItemEffect(18),
    UserEffect_SquibEffect(19),
    // 拾取怪物卡片
    UserEffect_MonsterBookCardGet(20), /* 怪物書卡獲得 */
    UserEffect_LotteryUse(21), /* 彩票使用 */
    UserEffect_ItemLevelUp(22), /* 物品升級 */
    UserEffect_ItemMaker(23), /* 物品製作 */
    // 24 [Int] MESO+
    UserEffect_ExpItemConsumed(25),
    // 連續擊殺時獲得的經驗提示
    UserEffect_FieldExpItemConsumed(26),
    // 顯示WZ的效果
    UserEffect_ReservedEffect(27),
    // 聊天窗顯示"消耗1個原地復活術 ，於角色所在原地進行復活！（尚餘Byte個）"
    UserEffect_UpgradeTombItemUse(28),
    UserEffect_BattlefieldItemUse(29),
    // 顯示WZ的效果2
    UserEffect_AvatarOriented(30),
    UserEffect_AvatarOrientedRepeat(31),
    UserEffect_AvatarOrientedMultipleRepeat(32),
    UserEffect_IncubatorUse(33),
    // WZ聲音
    UserEffect_PlaySoundWithMuteBGM(34),
    // WZ聲音
    UserEffect_PlayExclSoundWithDownBGM(35),
    // 商城道具效果
    UserEffect_SoulStoneUse(36),
    // 回復特效(Int)
    UserEffect_IncDecHPEffect_EX(37),
    UserEffect_IncDecHPRegenEffect(38),
    // 採集/挖礦
    UserEffect_EffectUOL(39),
    // 40
    // 41  UI/Sboating.img/Basic/gaugebar
    // 42
    // 43
    // 44
    // 45
    // 46
    // 47
    UserEffect_PvPRage(48),
    UserEffect_PvPChampion(49),
    UserEffect_PvPGradeUp(50),
    UserEffect_PvPRevive(51),
    UserEffect_JobEffect(52),
    // 背景變黑
    UserEffect_FadeInOut(53),
    UserEffect_MobSkillHit(54),
    UserEffect_AswanSiegeAttack(55),
    // 影武者出生劇情背景黑暗特效
    UserEffect_BlindEffect(56),
    UserEffect_BossShieldCount(57),
    // 天使技能充能效果
    UserEffect_ResetOnStateForOnOffSkill(58), // Sound/Field.img/Romio/discovery
    UserEffect_JewelCraft(59),
    UserEffect_ConsumeEffect(60),
    UserEffect_PetBuff(61),
    UserEffect_LotteryUIResult(62),
    UserEffect_LeftMonsterNumber(63),
    UserEffect_ReservedEffectRepeat(64),
    UserEffect_RobbinsBomb(65),
    UserEffect_SkillMode(66),
    UserEffect_ActQuestComplete(67),
    UserEffect_Point(68),
    UserEffect_SpeechBalloon(69), // NPC說話特效

    /*V264*/
    UNK_v258_70(71),
    UNK_v258_71(72),
    UNK_v258_72(73),
    UserEffect_TextEffect(74), // 特殊頂部訊息[ 如燃燒場地 257 update] V264+
    UNK_v245_71(75),

    UserEffect_SkillPreLoopEnd(75), // 暗夜行者技能特效
    UNK_v245_73(76),
    //
    UserEffect_Aiming(77), // Effect/BasicEff.img/aiming/%d
    UNK_v245_75(78),
    UserEffect_PickUpItem(79),// 獲得道具頂部提示 UI/UIWindow.img/FloatNotice/%d/DrawOrigin/icon
    UserEffect_BattlePvP_IncDecHp(80), // [int] [str]
    UserEffect_BiteReceiveSuccess(81), // Effect/OnUserEff.img/urus/catch 烏勒斯接住人時候Catch字樣
    UserEffect_BiteReceiveFail(82), // Effect/ItemEff.img/2270002/fail
    UserEffect_IncDecHPEffect_Delayed(83),
    UserEffect_Lightness(84),
    // 花狐技能
    UserEffect_HakuSkill(85),
    UNK_v245_83(86), // MonsterBattleWin
    UNK_v245_84(87),
    UNK_v245_85(88),
    UNK_v245_86(89), // [int]
    UNK_v245_87(90),
    UNK_v245_88(91),
    UserEffect_HoYoungHeal(92),//TODO check
    UNK_v245_90(93),
    UNK_v245_91(94),
    UNK_v245_92(95),
    UNK_v245_93(96),
    UserEffect_SpeedMirage(97),
    UNK_v258_99(99), // [long][str]
    UNK_v258_100(100),
    UNK_v258_101(101),
    UNK_v258_103(103), // 跟 1,2一樣
    UNK_v258_104(104), // 跟4一樣
    UNK_v258_105(105), // [int][byte] Effect/CharacterEff.img/HexaSkillEff/back
    ;

    private final short code;

    EffectOpcode(int code) {
        this.code = (short) code;
    }

    public short getValue() {
        return code;
    }
}
