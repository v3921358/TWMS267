package Client;

import Server.Buffstat;
import tools.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum SecondaryStat implements Buffstat {
    IndiePAD(true),              // 增加角色的物理攻击力
    IndieMAD(true),              // 增加角色的魔法攻击力
    IndiePDD(true),              // 增加角色的物理防御力
    IndieMHP(true),              // 增加角色的最大生命值
    IndieMHPR(true),             // 增加角色的最大生命值恢复率
    IndieMMP(true),              // 增加角色的最大魔法值
    IndieMMPR(true),             // 增加角色的最大魔法值恢复率
    IndieACC(true),              // 增加角色的命中率
    IndieEVA(true),              // 增加角色的闪避率
    IndieJump(true),             // 增加角色的跳跃力
    IndieSpeed(true),           // 增加角色的移动速度
    IndieAllStat(true),         // 增加角色的所有属性（力量、敏捷、智力、运气）
    IndieStatRBasic(true),      // 增加角色的主要属性（力量、敏捷、智力、运气）百分比
    IndieDodgeCriticalTime(true), // 减少角色遭受暴击和命中的时间
    IndieEXP(true),             // 增加角色获得的经验值
    IndieBooster(true),         // 增加使用技能后的连续攻击速度
    IndieFixedDamageR(true),    // 减少受到的固定伤害
    PyramidStunBuff(true),      // 角色在金字塔地图中受到的眩晕效果
    PyramidFrozenBuff(true),    // 角色在金字塔地图中受到的冰冻效果
    PyramidFireBuff(true),      // 角色在金字塔地图中受到的火焰效果
    PyramidBonusDamageBuff(true), // 增加角色在金字塔地图中造成的伤害
    IndieRelaxEXP(true),        // 增加角色的休息时获得的经验值
    IndieSTR(true),             // 增加角色的力量属性
    IndieDEX(true),             // 增加角色的敏捷属性
    IndieINT(true),             // 增加角色的智力属性
    IndieLUK(true),             // 增加角色的运气属性
    IndieDamR(true),            // 增加角色对怪物造成的伤害
    IndieScriptBuff(true),      // 触发特定脚本的角色增益效果
    IndieMDF(true),             // 增加角色的魔法防御力
    IndieAsrR(true),            // 增加角色的所有属性抗性
    IndieTerR(true),            // 增加角色的状态异常抗性
    IndieCr(true),              // 增加角色的暴击率
    IndiePDDR(true),            // 增加角色的物理防御力无视率
    IndieCD(true),              // 增加角色技能的冷却速度
    IndieBDR(true),             // 增加角色的爆击伤害
    IndieStatR(true),           // 增加角色的所有属性百分比
    IndieStance(true),          // 角色进入霸体状态，免疫部分控制效果
    IndieIgnoreMobpdpR(true),   // 增加角色对怪物防御力无视率
    IndieEmpty(true),           // 一个空的操作码，可能是为了占位或其他用途
    IndiePADR(true),            // 增加角色的物理攻击力无视率
    IndieMADR(true),            // 增加角色的魔法攻击力无视率
    IndieEVAR(true),            // 增加角色的所有属性抗性无视率
    IndieDrainHP(true),
    //    IndiePartyExpRate(true),
//    IndieCrMin(true),
    IndiePMdR(true),            // 增加角色的物理和魔法防御力百分比
    IndieForceJump(true),          // 强制增加角色的跳跃力
    IndieForceSpeed(true),         // 强制增加角色的移动速度
    IndieQrPointTerm(true),
    //    IndieWaterSmashBuff(true),
    IndieLoopEffect(true),
    IndiePeriodicalSkillActivation(true),
    IndieEventForSkill(true),
    IndieItemDropRate(true),   // 增加角色的掉落率提升效果
    IndieBuffIcon(true),           // 角色召唤效果，可能是召唤出其他单位进行战斗 // old
    IndieCooltimeReduce(true),          // 减少角色技能的冷却时间
    IndieNotDamaged(true),         // 使角色处于无敌状态，免疫伤害和控制效果
    IndieKeydown(true),            // 未知效果的生成怪物增益效果
    IndieDamReduceR(true),    // 减少角色受到的所有伤害
    IndieSpecificSkill(true),
    IndieEVARReduceR(true), // 減少總迴避率
    IndieHitDamR(true), // 受擊傷增加
    IndieApplySuperStance(true),
    IndieFlyAcc(true),
    IndieAllHitDamR(true), // 减少角色受到的伤害
    IndieArc(true),
    IndieAut(true),
    IndiePowerGuard(true), // 角色使用技能时反弹部分伤害给攻击者
    IndieDropPer(true),
    IndieMesoAmountRate(true),
    IndieCheckTimeByClient(true), // 减少角色受到的诅咒类效果的影响
    IndieDotHealHP(true),
    IndieDotHealMP(true),
    IndieNormalDamReduceR(true),
    IndieIgnorePCounter(true),  // 角色的攻击无视敌人的反击效果
    IndieBarrier(true), // 角色获得一个护盾，吸收一定量的伤害
    IndieStarForce(true),
    IndieNBDR(true),
    IndieMonsterCollectionR(true),
    IndieMPConReduceR(true),
    IndieAntiMagicShell(true),
    IndieDecDamTargetMob(true),
    IndieStatCount(true),    // 统计角色属性增益效果的数量
    PAD,                      // 增加物理攻击力
    PDD,                      // 增加物理防御力
    MAD,                      // 增加魔法攻击力
    ACC,                      // 增加命中率
    EVA,                      // 增加闪避率
    Craft,                    // 角色的制作技能效果
    Speed,                    // 增加移动速度
    Jump,                     // 增加跳跃力
    MagicGuard,               // 角色使用技能时，将部分伤害转化为魔法消耗
    DarkSight,                // 角色进入隐身状态
    Booster,                  // 增加使用技能后的攻击速度
    PowerGuard,               // 角色使用技能时反弹部分伤害给攻击者
    MaxHP,                    // 增加最大生命值
    MaxMP,                    // 增加最大魔法值
    Invincible,               // 角色进入无敌状态，免疫伤害和控制效果
    SoulArrow,                // 角色增加魔法箭效果，使普通攻击变为远程攻击
    Stun,                     // 角色受到眩晕效果
    Poison,                   // 角色受到中毒效果
    Seal,                     // 角色受到封印效果
    Darkness,                 // 角色受到黑暗效果
    ComboCounter,              // 鬥氣集中
    BlessedHammer,
    BlessedHammerActive,
    WeaponCharge,      // 角色武器充能，提高一段时间内的攻击力
    HolySymbol,        // 角色使用神圣之符技能，提高经验掉落率
    MesoUp,            // 角色获得金币掉落率提升效果
    ShadowPartner,      // 角色使用暗影伙伴技能，生成一个分身协助战斗
    ThiefSteal,
    PickPocket,        // 角色使用扒窃技能，偷取怪物身上的金币
    BloodyExplosion,         // 角色使用金币保护技能，消耗金币抵挡一定伤害
    Thaw,              // 解除角色冰冻状态
    Weakness,          // 角色受到虚弱效果
    Curse,             // 角色受到诅咒效果
    Slow,              // 角色受到减速效果
    Morph,             // 角色受到形变效果
    Regen,             // 角色生命和魔法值恢复速度增加
    BasicStatUp,       // 楓葉祝福
    Stance,            // 角色进入霸体状态，免疫部分控制效果
    SharpEyes,         // 角色使用鹰眼技能，增加暴击率和命中率
    ManaReflection,   // 角色使用魔法反射技能，将部分伤害反射给攻击者
    Attract,           // 角色使用引诱技能，吸引怪物攻击
    NoBulletConsume,   // 角色使用技能时不消耗弹药
    Infinity,          // 角色使用无限技能，消耗魔法值增加攻击力
    AdvancedBless,     // 角色受到高级祝福效果
    IllusionStep,       // 角色使用幻影步技能，获得一定的回避率
    Blind,
    Concentration,        // 角色使用专注技能，增加攻击力和魔法力
    BanMap,               // 禁止角色进入特定地图
    MaxLevelBuff,         // 限制角色获得等级上限提升的效果
    MesoUpByItem,         // 角色使用物品增加金币掉落率
    MesoAmountRate,               // 未知效果的角色增益效果
    MesoAmountRateRune,               // 未知效果的角色增益效果
    Ghost,                // 角色进入幽灵状态，无法被怪物攻击
    Barrier,              // 角色进入防御状态，减少受到的伤害
    ReverseInput,         // 反转角色的输入控制
    ItemUpByItem,         // 角色使用物品提升物品效果
    RespectPImmune,       // 角色免疫物理伤害
    RespectMImmune,       // 角色免疫魔法伤害
    DefenseAtt,           // 角色攻击时进行防御
    DefenseState,         // 角色进入防御状态
    DojangBerserk,        // 角色进入道馆狂暴状态
    DojangInvincible,     // 角色进入道馆无敌状态
    DojangShield,         // 角色在道馆获得护盾效果
    SoulMasterFinal,      // 战神职业的终极技能效果
    WindBreakerFinal,     // 箭神职业的终极技能效果
    KarmaBlade,
    ElementalReset,       // 角色使用元素重置技能，恢复冷却技能
    HideAttack,           // 角色隐藏状态下使用技能攻击
    EventRate,             // 增加角色获得事件物品的掉落率
    ComboAbilityBuff,     // 狂狼勇士COMBO
    ComboDrain, /* 連擊組合 */
    ComboBarrier, /* 連擊屏障*/
    BodyPressure,            // 角色受到压迫状态，无法移动和使用技能
    RepeatEffect,            // 重复播放角色效果，可能是一些特殊状态的持续效果
    ExpBuffRate,             // 增加角色获得的经验值
    StopPortion,             // 停止角色的部分能力效果
    StopMotion,              // 停止角色的动作
    Fear,                   // 角色受到致盲效果
    HiddenPieceOn,           // 角色使用隐藏碎片技能，获得一些效果
    MagicShield,             // 角色使用魔法护盾技能，吸收一定量的伤害
    MagicResistance,         // 增加角色魔法属性抗性
    SoulStone,               // 角色使用灵魂石技能，增加攻击力
    Flying,                  // 角色进入飞行状态
    Frozen,                  // 角色进入冰冻状态，无法移动和使用技能
    AssistCharge,            // 角色使用辅助充能技能，获得辅助技能的效果
    Enrage,                  // 角色进入愤怒状态，增加攻击力
    DrawBack,                // 角色受到后退效果
    NotDamaged,              // 角色进入不受伤害状态
    FinalCut,                // 角色使用最后一击技能，造成额外伤害
    HowlingAttackDamage,     // 角色使用嚎叫技能，增加攻击力
    BeastForm,       // 角色变身为野兽状态，增加攻击力
    Dance,                   // 角色进入舞蹈状态，增加移动速度
    EMHP,                    // 增加角色的最大生命值百分比
    EMMP,                    // 增加角色的最大魔法值百分比
    EPAD,                    // 增加角色的物理攻击力百分比
    EMAD,                    // 增加角色的魔法攻击力百分比
    EPDD,                    // 增加角色的物理防御力百分比
    Guard,                   // 角色进入保护状态，减少受到的伤害
    Cyclone,                  // 未知效果的角色增益效果
    MorphAvatar,                  // 未知效果的角色增益效果
    RidingExpireInfoSave,                  // 未知效果的角色增益效果
    NextSpecificSkillDamageUp,           // 角色使用标枪技能，增加伤害
    PinkbeanMinibeenMove,    // 皮卡啾迷你移動   KMS 362 BY HERTZ.
    GravityConstraint,
    Sneak,                   // 角色使用潜行技能，进入潜行状态
    Mechanic,                // 角色进入机械状态，可能是职业特有状态
    BeastFormMaxHP,          // 野兽形态下增加角色的最大生命值
    Dice,                    // 角色使用骰子技能，获得随机效果
    BlessingArmor,           // 角色使用护身符技能，增加防御力
    DamR,                    // 增加角色造成的伤害
    TeleportMasteryOn,       // 角色使用瞬移技能，增加瞬移效果
    CombatOrders,            // 增加角色技能的效果
    Beholder,                // 角色使用魔眼技能，生成一个魔眼辅助战斗
    DispelItemOption,        // 角色使用物品消除装备的选项效果
    Inflation,               // 角色使用通货膨胀技能，消耗金币增加攻击力
    OnixDivineProtection,    // 角色使用石盾技能，增加防御力
    Web,                     // 角色使用网技能，将怪物固定在地方
    Bless,                   // 角色受到祝福效果
    TimeBomb,                // 角色使用定时炸弹技能，延时爆炸造成伤害
    DisOrder,                // 角色受到混乱效果
    Thread,                  // 角色受到缠绕效果
    Team,                    // 角色受到团队效果
    Explosion,               // 角色使用爆炸技能，造成范围伤害
    BuffLimit,               // 角色获得增益效果的上限
    STR,                     // 增加角色力量属性
    INT,                     // 增加角色智力属性
    DEX,                     // 增加角色敏捷属性
    LUK,                     // 增加角色运气属性
    DispelItemOptionByField, // 地图上消除装备的选项效果
    DarkTornado,             // 角色使用黑暗龙卷风技能，造成范围伤害
    PVPDamage,               // 角色在PvP中造成伤害增加
    PvPScoreBonus,           // 角色在PvP中得分奖励增加
    PvPInvincible,           // 角色在PvP中进入无敌状态
    PvPRaceEffect,            // 角色在PvP中获得种族效果
    WeaknessMdamage,         // 弱点物理伤害，增加对怪物的物理伤害
    Frozen2,                 // 角色进入冰冻状态，无法移动和使用技能
    PVPDamageSkill,          // 角色在PvP中造成技能伤害增加
    AmplifyDamage,           // 增加角色造成的所有伤害
    Shock,                   // 角色受到震动效果
    InfinityForce,           // 角色使用无限技能，增加攻击力和魔法力
    IncMaxHP,                // 增加角色最大生命值
    IncMaxMP,                // 增加角色最大魔法值
    HolyMagicShell,          // 角色受到圣洁魔法盾效果
    KeyDownTimeIgnore,       // 忽略角色技能按键持续时间
    ArcaneAim,               // 角色使用奥秘瞄准技能，增加命中率
    MasterMagicOn,           // 角色进入主魔法状态，增加魔法攻击力
    AsrR,                    // 减少角色受到的所有伤害
    TerR,                    // 减少角色受到的特定类型伤害
    DamAbsorbShield,         // 吸收一定量的伤害，转化为魔法消耗
    DevilishPower,           // 角色进入恶魔力量状态，增加攻击力和魔法力
    Roulette,                // 角色使用轮盘技能，获得随机效果
    RouletteStack,
    SpiritLink,              // 角色使用灵魂链接技能，将伤害分担给其他角色
    AsrRByItem,              // 通过物品减少角色受到的所有伤害
    Event,                   // 角色获得活动效果
    CriticalBuff,            // 角色暴击率增加效果
    DropRate,                // 增加物品掉落率
    PlusExpRate,             // 增加经验值获取率
    ItemInvincible,          // 角色使用物品获得无敌效果
    Awake,                   // 觉醒技能效果
    ItemCritical,            // 物品的暴击率效果
    ItemEvade,               // 物品的闪避率效果
    Event2,                  // 角色获得活动效果
    VampiricTouch,           // 角色使用吸血之触技能，将伤害转化为生命回复
    DDR,                     // 减少角色受到的所有伤害
    IncTerR,                 // 增加角色受到的特定类型伤害
    IncAsrR,                 // 增加角色受到的所有伤害
    DeathMark,               // 角色被标记为死亡状态
    UsefulAdvancedBless,     // 有效的高级祝福效果
    Lapidification,          // 角色被石化效果影响
    VenomSnake,              // 角色受到毒蛇效果影响
    CarnivalAttack,          // 角色在嘉年华中获得攻击增益效果
    CarnivalDefence,         // 角色在嘉年华中获得防御增益效果
    CarnivalExp,             // 角色在嘉年华中获得经验值增益效果
    SlowAttack,              // 角色的攻击速度减缓
    PyramidEffect,           // 角色受到金字塔效果影响
    HollowPointBullet,       // 角色使用空心子弹技能，造成额外伤害
    KeyDownMoving,           // 角色持续按键移动
    IgnoreTargetDEF,         // 忽略目标的防御
    StrikerElectricUsed,
    ReviveOnce,              // 角色可以复活一次
    Invisible,               // 角色进入隐形状态
    EnrageCr,                // 愤怒状态的爆击率增加效果
    EnrageCrDamMin,          // 愤怒状态的爆击伤害增加效果
    Judgement,               // 角色受到审判效果影响
    DojangLuckyBonus,        // 角色在道馆中获得幸运奖励
    PainMark,                // 角色被痛苦标记效果影响
    Magnet,                  // 角色受到磁铁效果影响
    MagnetArea,               // 角色受到磁铁区域效果影响
    GuidedArrow,
    StraightForceAtomTargets,
    LefBuffMastery,
    TempSecondaryStat,
    CoalitionSupportSoldierStorm,
    LefMageLinkSkill,
    SixthJavelinStack,
    SixthGloryWingJavelinStack,
    SixthCrystalStack,
    ShadowMomentum,
    GrandCross,
    YetiCook,
    PinkbeanCheer,
    DropPer,
    VampDeath,
    BlessingArmorIncPAD,
    KeyDownAreaMoving,
    Larkness,
    StackBuff,
    AntiMagicShell,
    LifeTidal,
    HitCriDamR,
    SmashStack,
    PartyBarrier,
    ReshuffleSwitch,
    SpecialAction,
    VampDeathSummon,
    StopForceAtomInfo,
    SoulGazeCriDamR,
    SoulRageCount,
    PowerTransferGauge,
    AffinitySlug,
    Trinity,
    IncMaxDamage,
    BossShield,
    MobZoneState,
    GiveMeHeal,
    TouchMe,
    Contagion,
    ComboUnlimited,
    SoulExalt,
    IgnoreAllCounter,
    IgnorePImmune,
    IgnoreAllImmune,
    IgnoreAllAbout,
    FinalJudgement,
    FireAura,
    VengeanceOfAngel,
    HeavensDoor,
    Preparation,
    BullsEye,
    IncEffectHPPotion,
    IncEffectMPPotion,
    BleedingToxin,
    IgnoreMobDamR,
    Asura,
    MegaSmasher,
    FlipTheCoin,
    UnityOfPower,
    Stimulate,
    ReturnTeleport,
    DropRIncrease,
    IgnoreMobpdpR,
    BdR,
    CapDebuff,
    Exceed,
    DiabolikRecovery,
    FinalAttackProp,
    ExceedOverload,
    OverloadCount,
    BuckShot,
    FireBomb,
    HalfstatByDebuff,
    SurplusSupply,
    SetBaseDamage,
    EVAR,
    NewFlying,
    AmaranthGenerator,
    CygnusElementSkill,
    StrikerHyperElectric,
    EventPointAbsorb,
    EventAssemble,
    StormBringer,
    ACCR,
    DEXR,
    Translucence,
    PoseType,
    CosmicForge,
    ElementSoul,
    CosmicOrb,
    //GlimmeringTime,
    //TrueSight,
    SoulExplosion,

    SoulMP,
    FullSoulMP,
    SoulSkillDamageUp,
    ElementalCharge,
    Restoration,
    CrossOverChain,
    Reincarnation,
    ReincarnationMission,
    ReincarnationOnOff,
    ChillingStep,
    DotBasedBuff,
    SixthDotBasedBuff,
    BlessEnsenble,
    ComboCostInc,
    NaviFlying,
    Holding,
    QuiverCatridge,
    UserControlMob,
    ImmuneBarrier,
    CriticalGrowing,
    LastUseSkillAttr,
    QuickDraw,
    BowMasterConcentration,
    TimeFastABuff,
    TimeFastBBuff,
    GatherDropR,
    AimBox2D,
    CursorSniping,
    DebuffTolerance,
    TearsOfFairy,
    DotHealHPPerSecond,
    DotHealMPPerSecond,
    SpiritGuard,
    PreReviveOnce,
    SetBaseDamageByBuff,
    LimitMP,
    ReflectDamR,
    ComboTempest,
    MHPCutR,
    MMPCutR,
    SelfWeakness,
    ElementDarkness,
    FlareTrick, /* 耀班 */
    FlameDischarge,
    Dominion, /* 道米尼歐 */
    SiphonVitality,
    DarknessAscension,
    BossWaitingLinesBuff,
    DamageReduce, /* 減少傷害 */
    ShadowServant,
    ShadowIllusion,
    KnockBack,
    AddAttackCount,
    ComplusionSlant,
    JaguarSummoned, /* 美洲虎召喚 */
    JaguarCount,
    SSFShootingAttack,
    DevilCry,
    ShieldAttack,
    DarkLighting,
    AttackCountX,
    BMageDeath,
    BombTime,
    NoDebuff,
    BattlePvP_Mike_Shield,
    BattlePvP_Mike_Bugle,
    XenonAegisSystem,
    AngelicBursterSoulSeeker,
    HiddenPossession,
    NightWalkerBat,
    NightLordMark,
    WizardIgnite,
    FireBarrier,
    ChangeFoxMan,
    PairingUser,
    Frenzy,
    ShadowSpear,
    PvPFlag,
    BladeStanceMode,
    BladeStancePower,
    KenjiCounter,
    EvasionMaster,
    SelfHyperBodyIncPAD,
    SelfHyperBodyMaxHP,
    SelfHyperBodyMaxMP,
    BladeStanceBooster,
    UNK449,
    UNK450,
    EvasionUpgrade,
    CriticalBuffAdd,
    FoxBless,
    BossDamageRate,
    RepeatEffect2,
    AntiEvilShield,
    TotalDAMr,
    GiantBossDeathCnt,
    UNK462,
    AntiDebuff,
    UNK464,
    MastemaGuard,
    Elysion,
    QuiverFullBurst,
    SwordBaptism,
    WildGrenade,
    ChainMantisADV,
    FieldGimmickFear,
    ImmuneStun,
    BattlePvP_Helena_Mark,
    BattlePvP_Darklord_Explosion,
    BattlePvP_Helena_WindSpirit,
    BattlePvP_LangE_Protection,
    BattlePvP_LeeMalNyun_ScaleUp,
    BattlePvP_Revive,
    PinkbeanAttackBuff,
    PinkbeanRelax,
    PinkbeanRollingGrade,
    PinkbeanYoYoStack,
    RandAreaAttack,
    RandAreaAttack2,
    ShineRuneAttack,
    NextAttackEnhance,
    AranBeyonderDamAbsorb,
    PirateDesire,
    AranCombotempastOption,
    ViperTimeLeap,
    RoyalGuardState,
    BodyRectGuardPrepare,
    MichaelSoulLink,
    MichaelStanceLink,
    TriflingWhimOnOff,
    AddRangeOnOff,
    KinesisPsychicPoint,
    KinesisPsychicOver,
    KinesisIncMastery,
    KinesisPsychicEnergeShield,
    BladeStance,
    DebuffActiveSkillHPCon,
    DebuffIncHP,
    BowMasterMortalBlow,
    AngelicBursterSoulResonance,
    Fever,
    RpSiksin,
    TeleportMasteryRange,
    FixCoolTime,
    IncMobRateDummy,
    AdrenalinBoost,
    AranDrain,
    AranBoostEndHunt,
    HiddenHyperLinkMaximization,
    RWCylinder,
    RWCombination,
    RWVulkanPunch,
    RWMagnumBlow,
    RWBarrier,
    RWBarrierHeal,
    RWMaximizeCannon,
    RWOverHeat,
    UsingScouter,
    RWMovingEvar,
    Stigma,
    MahaInstall,
    HeavensDoorNotTime,
    RuneStoneNoTime,
    XenonRoketRunch,
    TransformOverMan,
    EnergyBust,
    LightningUnion,
    BulletParty,
    LoadedDice,
    BishopPray,
    ChainArtsFury,
    MichaelFixDamDecTime,
    PinkbeanYoYoAddDamR,
    Warrior_AuraWeapon,
    Warrior_AuraWeaponStack,
    Wizard_OverloadMana,
    Michael_RhoAias,
    Kinesis_DustTornado,
    NightLord_SpreadThrow,
    HowlingGaleStack,
    CannonShooter_BFCannonBall,
    CannonShooter_MiniCannonBall,
    Shadower_ShadowAssault,
    CreateEventMeso,
    MesoRanger_MesoDizerX,
    DawnShield_ExHP,
    DawnShield_WillCare,
    SkillDeployment,
    InnerStorm,
    Wet,
    SpecialTombPL,
    GrabbedByMob,
    SummonProp,
    ReduceMP,
    KenjiShadowAttackBuff,
    UNK498,
    WorldExpBuff,
    WorldDropBuff,
    CurseRingBuff,
    EXP_CARD,
    GhostLiberationStack,
    KannaSiksinAutoAttack,
    CoronaBuffOverlap,
    FifthAdvWarriorShield,
    SplitArrow,
    FreudBlessing,
    OverloadMode,
    FifthSpotLight,
    OutSide,
    WeaponVariety,
    LefGloryWing,
    Shadower_Assassination,
    ConvertAD,
    EtherealForm,
    ReadyToDie,
    Oblivion,
    Cr2CriDamR,
    BlackMageCreate,
    BlackMageDestroy,  // <--- 尚未處理
    BlackMageMonochrome,  // <--- 尚未處理
    HitStackDamR,
    BuffControlDebuff, // KMS 帕普爾·庫斯  // <--- 尚未處理
    DispersionDamage, // KMS 帕普爾·炸彈  // <--- 尚未處理
    HarmonyLink,
    LefFastCharge,
    BuffIconNoShadow,
    CrystalChargeBuffIcon,
    LioMachinaCombineBuffIcon, // KMS 神狀態  // <--- 尚未處理
    CoalitionSupportWarplaneBuffIcon,
    BattlePvP_Ryude_Frozen,
    BattlePvP_Ryude_Buff,
    AntiMagicShellByJewelMaking,
    DamageRateSetUpForApc,
    SpecterGauge,
    SpecterMode,
    SpellBullet_Plain,
    SpellBullet_Scarlet,
    SpellBullet_Gust,
    SpellBullet_Abyss,
    ComingDeath,
    GreatOldAbyss,
    SixthPhoenix,
    HolyAdvent,
    ZodiacBurst,
    CombatFrenzy,
    LPSpellAmplification,
    LPInfinitySpell,
    LPMagicCircuitFullDrive,
    LPMagicCircuitFullDriveStack,
    LPBattleMode, // 亞克連結技能 548
    LPHoldSpecterGauge, //記憶來源  // <--- 尚未處理
    DiscoveryWeather,
    BossWill_Infection, // Will座標施法  // <--- 尚未處理
    PvpApcTeam,
    HolyMagicShellReUse,
    StrikerComboStack,
    HeroComboInstinct,
    KhaliUltimatum,
    WindBreakerStormGuard,
    FlameWizardInfiniteFlame,
    MichaelSwordOfLight,
    PhantomMarkOfPhantomOwner,
    PhantomMarkOfPhantomTarget,
    RescuedSoldierBuffIcon1,
    RescuedSoldierBuffIcon2,
    FireBirdSupport,
    FireBirdSupportActive,
    AuraOfLife,
    MemoryOfJourney,
    MiracleTime,
    ItemUpgradeR,
    BufftimeRforActive,
    DecBaseDamageDebuff,
    LimitConsumeDebuff,
    LimitEquipStatDebuff,
    BlackMageWeaponDestruction,
    BlackMageWeaponCreation,
    BattlePvP_LangE_LiverStack,
    BattlePvP_Mike_Amor,
    SiphonVitalityBarrier,
    PathFinderAncientGuidance,
    BattlePvP_KeyDown,
    BattlePvP_Wongki_FlyingCharge,
    BattlePvP_Wongki_AwesomeFairy,
    BattlePvP_Mugong_PandaZone,
    BattleSurvivalDefence,
    BattleSurvivalInvincible,
    EventPvPDefence,
    EventPvPInvincible,
    EventSoccerBall,
    EventSoccerMomentBuff,
    ZeroDamageDebuff,
    FifthGoddessBless,
    FifthGoddessBlessStack,
    CommonItemSkillContinuous,
    PinkbeanMatryoshka,
    NewtroWarriors,
    SilverMane,
    SilverManeBuff,
    BattlePVPAttBuff,
    MinigameStat,
    LuckyPapylus,
    AnimaThiefTaoistType,
    AnimaThiefTaoistGauge,
    AnimaThiefCloneAttack,
    AnimaThiefFifthCloneAttack,
    AnimaThiefButterfly,
    MiracleDrug,
    AnimaThiefMetaphysics,
    NoviceMagicianLink,
    NerotaPower,
    LibraryMissionGuard,
    XenonHoloGramGraffiti,
    KeyDownEnable,
    DracoSlasherNoCooltime,
    WillOfSword,
    LWSwordGauge,
    LWCreation,
    LWDike,
    LWWonder,
    LWRestore,
    LefWarriorNobility,
    LWResonanceBuff, /* 魔劍共鳴 */
    RunePurification, /* 淨化符文 */
    RuneContagion, /* 傳輸符文 */
    DebuffHallucination, /* 戴斯克黑暗 */
    BMageAuraYellow, /* 黃色光環 */
    BMageAuraDrain, /* 紅色光環 */
    BMageAuraBlue, /* 藍色光環 */
    BMageAuraDark, /* 黑色光環 */
    BMageAuraDebuff, /* DEBUFF光環 */
    BMageAuraUnion, /* 聯盟光環 */
    IceAura, /* 騎士光環 不明所以是甚麼? */
    KnightsAura, /* 騎士光環 不明所以是甚麼? */
    ZeroAuraStr,
    NovaArcherIncanation,
    AnimaThiefMetaphysicsBuff,
    RevenantGauge,
    DeathDance,
    ShadowShield,
    AranComboTempestAura,
    XenonBursterLaser,
    BMageAbyssalLightning,
    StrikerThunderChain,
    MercedesRoyalKnight,
    PhantomDefyingFate,
    FWSalamanderMischiefAttack,
    KinesisLawOfGravity,
    RepeatinCartrige,
    LefMageCrystalGate,
    ThrowBlasting,
    AnimaThiefIllusion,
    DarknessAura,
    WeaponVarietyFinale,
    Equinox,
    EquinoxActive,
    ZeroEgoWeaponAlpha,
    RelicUnboundD,
    EvanSpiralOfMana,
    RWAfterImage,
    NADragonGauge,
    NADragonEnchant,
    NADeathBlessing,
    NAThanatosDescent,
    NAAnnihilation,
    NARemainIncense,
    NAOminousStream,
    NABrutalPang,
    MobFlashBang, // 賽蓮 -
    IgnorePriestDispel, // 賽蓮 -
    LimitRecovery, // 賽蓮 -
    NALinkSkill,
    StrikerChainReaction,
    LimitHealFactor,
    AdminCritical,
    AdminCriticalDam,
    AdminIgnoreTargetDEF,
    AdminBDR,
    AdminBuffTimeR,
    AdminActionSpeed,
    AdrenalinActivate, // <---- 尚未處理
    ATScrollPassive,
    YetiFuryGauge, // <---- 尚未處理
    YetiFuryMode, // <---- 尚未處理
    YetiMushroom, // <---- 尚未處理
    YetiMyFriendPepe, // <---- 尚未處理
    PinkbeanShowTime, // <---- 尚未處理
    DecFinalDamageDebuff,
    AMEarthVeinOnOff,
    AMPlanting,
    AMStoneShield,
    AMAbsorptionRiver,
    AMAbsorptionWind,
    AMAbsorptionSun,
    AMArtificialEarthVein,
    IgnoreFrictionOnOff,
    AnimaLotus,
    AMLinkSkill,
    AMEVTeleport,
    BossFieldFinalDamR,
    StormWhim,
    SeaSerpent,
    SerpentStone,
    SerpentScrew,
    FlameSweep,
    Cosmos,
    EnhancePiercing,
    EnhanceSniping,
    //UltimateSniping,
    //UltimatePiercing,
    //EnhanceQuadrupleThrow,
    UnwearyingRun,
    CrusaderPanic, // 傷痕之劍
    IceAuraZone,
    HolyWater,
    EpicDungeonGauge,
    TriumphFeather,
    SixthAngelsRay,
    GrandFinale,
    WarInTheShade,
    FlashMirage,
    HolyBlood,
    OrbitalExplosion,
    DragonSlave,
    LimitBreakFinalAttack,
    TranscendentLight,
    SixthAssassination,
    SixthAssassinationDarkSight,
    ArtificialEvolution,
    SixthFrozenLightning,
    KinesisUltimateBPM,
    ElementalKnight,
    LarknessStop,
    SummonChakri,
    ShadowAcceleration,
    ShadowBurst,
    DeceivingBlade,
    KaringGoongiAdvantage,
    KaringDoolAdvantage,
    BossAggro,
    Confinement,
    GrabAndThrow,
    DarkCloud,
    UserAroundAttackDebuff,
    UserTrackingAreaWarning,
    RPEventStat,
    FlameOrbMultiAttack,
    SixthStormArrowEx,
    SuperFistEnrageStack,
    WildVulcanAdv,
    WildVulcanAdvStack,
    NaturesBelief,
    AdrenalinSurge,
    LifeDeathControl,
    MissileBarrage,
    ForsakenRelic,
    Nightmare,
    LefPirateSpecterMark,
    RapidFire,
    SacredBastion,
    PathFinderForceCompulsion,
    AnimaThiefTaoistUpgrade,
    AnimaThiefFlameStrike,
    BlasterFileBunker,
    HowlingOfNature,
    KannaFifthAttract,
    KannaHakuHide,
    NeoTokyoBossThesis,
    NeoTokyoBossAntiThesis,
    NeoTokyoBossBomb,
    NeoTokyoBossPowOfLife,
    SixthHakuman,
    SixthShinBatto,
    EnergyCharged,
    DashSpeed,
    DashJump,
    RideVehicle,
    PartyBooster,
    GuidedBullet,
    Undead,
    RideVehicleExpire,
    RelicGauge,
    SecondAtomLockOn,
    ErdaStack,
    ErdaRevert,
    DescentOfSunRingType,
    MukHyunChosic,
    MukHyunDivine,
    MukHyunSingongCycle,
    MukHyun_SEON_PUNG_GAG,
    MukHyun_HO_SIN_GANG_GI,
    MukHyunNoDivineDec,
    MukhyunLinkSkill,


    None,
    /////////////取代
    MukHyunAwakeMushin,
    MukHyunRepeat,
    MukHyun_IM_GI_EUNG_BYEON,
    Unk176_466, // BlackMageCreate
    Unk200_527, // BlackMageDestroy
    BattlePvPHelenaMark, // BattlePvP_Helena_Mark
    BattlePvPLangEProtection, // BattlePvP_LangE_Protection
    DivineEcho, // PairingUser
    RhoAias, // Michael_RhoAias
    WingsOfGlory, // LefGloryWing
    LucentBrand, // LefBuffMastery
    Unk199_528, // BattlePvP_Ryude_Frozen
    Unk202_580, // BattlePvP_LangE_LiverStack
    Unk205_587, // BattlePvP_KeyDown
    Unk205_589, // BattlePvP_Wongki_AwesomeFairy
    Unk188_500, // OutSide
    Unk199_545, // BossWill_Infection
    WillowDodge, //
    EyeForEye, //
    Jinsoku, //
    HayatoBooster, //
    HayatoCr, //
    HayatoBoss, //
    HayatoPAD, //
    HayatoHPR, //
    IaijutsuBlade, //
    HayatoMPR, //
    HayatoStance, //
    BattoujutsuAdvance, //
    MastemasMark, //
    IgnorePCounter, //
    PrimalGrenade, //
    HowlingCritical, //
    HowlingMaxMP, //
    HowlingDefence, //
    HowlingEvasion, //
    BeastFormDamageUp, //
    ExtraSkillCTS, //
    ZeroAuraSpd, //
    RelicEmblem, //
    AncientGuidance, //
    Unk188_492, //
    TridentStrike, //
    LightningCascade, //
    ComboInstinct, //
    LightOfSpirit, //
    RiftOfDamnation, //
    MahasFury, //
    SpreadThrow, //
    WindEnergy, //
    GaleBarrier, //
    Albatross, //
    DEF, //
    AbyssalCast, //
    GustCast, //
    ScarletCast, //
    BasicCast, //
    SpecterState, //
    SpecterEnergy, //
    AbyssalRecall, //
    InfinitySpell, //
    ImpendingDeath, //
    ChargeSpellAmplifier, //
    PhantomMarkMobStat, //
    PhantomMark, //
    Benediction, //
    QuiverBarrage, //
    AdvancedQuiver, //
    MeltDown, //
    Ember, //
    OmegaBlaster, //
    VoidStrike, //
    MuscleMemory, //
    KillingPoint, //
    TrickBladeMobStat, //
    MesoGuard, //
    ShadowAssault, //
    ;

    private final int value;
    private final int pos;
    public int stat;
    private boolean stacked = false;

    SecondaryStat() {
        this.stat = this.ordinal();
        this.value = 1 << 31 - this.ordinal() % 32;
        this.pos = (int) Math.floor(this.ordinal() / 32);
    }

    SecondaryStat(boolean stacked) {
        this.stat = this.ordinal();
        this.value = 1 << 31 - this.ordinal() % 32;
        this.pos = (int) Math.floor(this.ordinal() / 32);
        this.stacked = stacked;
    }

    public static Map<SecondaryStat, Integer> getSpawnList() {
        HashMap<SecondaryStat, Integer> SpawnStatsList = new HashMap<>();
        SpawnStatsList.put(IndieForceSpeed, 0);
        SpawnStatsList.put(IndieLoopEffect, 0);
        SpawnStatsList.put(IndieBuffIcon, 0);
        SpawnStatsList.put(IndieNotDamaged, 0);
        SpawnStatsList.put(IndieKeydown, 0);
        SpawnStatsList.put(IndieSpecificSkill, 0);
        SpawnStatsList.put(IndieFlyAcc, 0);
        SpawnStatsList.put(IndieAllHitDamR, 0);
        SpawnStatsList.put(IndieBarrier, 0);
        SpawnStatsList.put(IndieStarForce, 0);
        SpawnStatsList.put(IndieNBDR, 0);
        SpawnStatsList.put(IndieMonsterCollectionR, 0);
        SpawnStatsList.put(IndieAntiMagicShell, 0);
        SpawnStatsList.put(BlessingArmor, 0);
        SpawnStatsList.put(PyramidEffect, 0);
        SpawnStatsList.put(BattlePvP_Helena_Mark, 0);
        SpawnStatsList.put(BattlePvP_LangE_Protection, 0);
        SpawnStatsList.put(PinkbeanRollingGrade, 0);
        SpawnStatsList.put(AdrenalinBoost, 0);
        SpawnStatsList.put(RWBarrier, 0);
        SpawnStatsList.put(Kinesis_DustTornado, 0);
        SpawnStatsList.put(BattlePvP_Ryude_Frozen, 0);
        SpawnStatsList.put(BattlePvP_LangE_LiverStack, 0);
        SpawnStatsList.put(BattlePvP_KeyDown, 0);
        SpawnStatsList.put(EnergyCharged, 0);
        SpawnStatsList.put(DashSpeed, 0);
        SpawnStatsList.put(DashJump, 0);
        SpawnStatsList.put(RideVehicle, 0);
        SpawnStatsList.put(PartyBooster, 0);
        SpawnStatsList.put(GuidedBullet, 0);
        SpawnStatsList.put(Undead, 0);
        SpawnStatsList.put(RideVehicleExpire, 0);
        SpawnStatsList.put(RelicGauge, 0);
        SpawnStatsList.put(SecondAtomLockOn, 0);
        return SpawnStatsList;
    }

    public static final SecondaryStat[] isAuraCTS = new SecondaryStat[] {
            BMageAuraYellow,
            BMageAuraBlue,
            BMageAuraDark,
            BMageAuraDebuff,
            KnightsAura,
            MichaelSoulLink,
    };

    @Override
    public int getPosition() {
        return pos;
    }

    @Override
    public int getValue() {
        return value;
    }

    public boolean canStack() {
        return stacked;
    }

    @Override
    public String toString() {
        return name() + "(" + stat + ")";
    }

    public static SecondaryStat getByValue(int value) {
        return Arrays.stream(values()).filter(it -> it.stat == value).findFirst().orElse(null);
    }

    private int flag;
    private int x;

    public int getFlag() {
        return this.flag;
    }


    public boolean isNormalDebuff() {
        switch (this) {
            case Seal:
            case Darkness:
            case Weakness:
            case Curse:
            case Poison:
            case Slow:
            case StopPortion:
            case DispelItemOption:
            case Fear:
            case IndieJump:
                return true;
        }
        return false;
    }

    public boolean isCriticalDebuff() {
        switch (this) {
            case Stun:
            case Attract:
            case ReverseInput:
            case BanMap:
            case Lapidification:
            case Morph:
            case DarkTornado:
            case Frozen:
                return true;
        }
        return false;
    }

    public boolean isSpecialBuff() {
        switch (this) {
            case EnergyCharged:
            case DashSpeed:
            case DashJump:
            case RideVehicle:
            case PartyBooster:
            case GuidedBullet:
            case Undead:
            case RideVehicleExpire:
            case RelicGauge:
            case SecondAtomLockOn: {
                return true;
            }
            default: {
                return false;
            }
        }
    }


    public int getX() {
        return this.x;
    }


    public static boolean isEncode4Byte(final Map<SecondaryStat, Pair<Integer, Integer>> statups) {
        final SecondaryStat[] array = new SecondaryStat[]{
                SecondaryStat.CarnivalDefence,
                SecondaryStat.SpiritLink,
                SecondaryStat.DojangLuckyBonus,
                SecondaryStat.SoulGazeCriDamR,
                SecondaryStat.PowerTransferGauge,
                SecondaryStat.ReturnTeleport,
                SecondaryStat.ShadowPartner,
                SecondaryStat.SetBaseDamage,
                SecondaryStat.QuiverCatridge,
                SecondaryStat.ImmuneBarrier,
                SecondaryStat.NaviFlying,
                SecondaryStat.Dance,
                SecondaryStat.SetBaseDamageByBuff,
                SecondaryStat.DotHealHPPerSecond,
                SecondaryStat.DotHealMPPerSecond,
                SecondaryStat.IncMaxDamage,
                SecondaryStat.Cyclone,
                SecondaryStat.GrabbedByMob,
                SecondaryStat.MagnetArea,
                SecondaryStat.PairingUser,
                SecondaryStat.MegaSmasher,
                SecondaryStat.VampDeath,
                SecondaryStat.FifthAdvWarriorShield,
                SecondaryStat.RidingExpireInfoSave,
                SecondaryStat.RWBarrier,
                SecondaryStat.SiphonVitalityBarrier,
        };
        for (final SecondaryStat stat : array) {
            if (statups.containsKey(stat)) {
                return true;
            }
        }
        return false;
    }


    public static void main(String[] args) {
        //设置迁移量
        for (SecondaryStat stat : values()) {
            stat.change();
        }
        //打印
        for (int i = 0; i <= values()[values().length - 1].stat; i++) {
            SecondaryStat stat = getByValue(i);
            if (stat != null) {
                System.out.println(stat.name() + " = " + stat.stat + ",");
            } else {
                System.out.println("\t// " + i);
            }
        }
    }

    private void change() {
        int change = 0;
        if (stat >= 64 && stat <= 690) {
            change = 1;
        } else if (stat >= 691 && stat <= 718) {
            change = 2;
        } else if (stat >= 719) {
            change = 13;
        }

        stat += change;
    }
}
