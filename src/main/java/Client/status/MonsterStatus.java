package Client.status;

import Config.constants.skills.傑諾;
import Server.Buffstat;

import java.io.Serializable;

/**
 * @author setosan
 */
public enum MonsterStatus implements Serializable, Buffstat {

    //140728A10+1883 == sealSKill

    IndiePDR(true),                        // 角色受到物理傷害減少效果影響
    IndieMDR(true),                        // 角色受到魔法傷害減少效果影響
    IndieAddFinalDamSkill(true),           // 角色受到最終傷害減少效果影響
    IndieSlow(true),                       // 角色獲得速度增加效果

    IndieTriangleFormation(true),
    IndieSixthTriangleFormation(true),
    IndieEnd(true),
    PAD,                             //物理攻擊
    PDR,                             //物理防禦
    MAD,                             //魔法攻擊
    MDR,                             //魔法防禦
    ACC,                             //命中
    EVA,                            //迴避
    Speed,                          //減速
    Stun,                           //眩暈
    Freeze,                         //極凍吐息
    BeforeFreeze,                   // v258新增
    Poison,                         //中毒
    Seal,                           //封印
    Darkness,                       //黑暗
    PowerUp,                        //物理攻擊提升
    MagicUp,                        //魔法攻擊提升
    PGuardUp,                       //物理防禦提升
    MGuardUp,                       //魔法防禦提升
    PImmune,                        //物理免疫
    MImmune,                        //魔法免疫
    Web,                            //影網術
    HardSkin,                       //硬化皮膚
    Ambush,                         //忍者伏擊，此技能好像已刪除
    Venom,                          //巫毒術，此技能好像已刪除
    Blind,                          //黑暗，英雄.恐慌，降低命中
    SealSkill,                      //封印技能
    Dazzle,                         //閃光
    PCounter,                       //物理反射
    MCounter,                       //魔法反射
    RiseByToss,                     //擊飛(扔上天)
    BodyPressure,                   //身體壓力
    Weakness,                       //虛弱
    Showdown,                       //攤開
    DevilCry,                       //
    MagicCrash,                     // 角色使用魔法消除技能，取消敵人的魔法效果
    DamagedElemAttr,                // 角色受到元素屬性傷害，可能用於標記特定的傷害類型
    TotalDamParty,                  //增加怪物受到的傷害
    HitCriDamR,                     // 增加角色的暴擊傷害比例
    Fatality,                       // 角色使用致命一擊技能，造成致命傷害
    Lifting,                        // 角色使用抬起技能，將怪物抬起
    DeadlyCharge,                   // 角色使用致命沖鋒技能，造成致命傷害
    Smite,                          // 角色使用重擊技能，增加攻擊力
    AddDamSkill,                    // 角色使用傷害增加技能
    Incizing,                       // 角色使用切割技能，增加攻擊力
    DodgeBodyAttack,                // 角色閃避物理攻擊
    DebuffHealing,                  // 角色在解除負面狀態時恢復生命值
    AddDamSkill2,                   // 角色使用傷害增加技能
    CUserLocal_BodyAttack,                     // 角色使用身體攻擊技能
    TempMoveAbility,                // 角色獲得臨時移動能力
    FixDamRBuff,                    // 角色使用固定傷害技能，增加傷害
    GhostDisposition,               // 未知效果的角色增益效果
    ElementDarkness,                //SUMMON_元素闇黑
    AreaInstallByHit,               // 在擊中區域內產生效果
    BMageDebuff,                    // 煉獄巫師的負面狀態效果
    JaguarProvoke,                  //豹弩_引發
    JaguarBleeding,                 //豹弩_出血
    PinkbeanFlowerPot,              // 角色召喚皮卡啾的花盆，產生效果
    BattlePvP_Helena_Mark,          // 在戰鬥PvP中，赫連娜標記效果
    PsychicLock,                    // 角色使用靈魂鎖定技能，造成效果
    PsychicLockCoolTime,            // 角色使用靈魂鎖定冷卻時間效果
    PsychicGroundMark,              // 角色使用靈魂地面標記技能，產生效果
    PowerImmune,                    // 角色免疫強力效果
    MultiPMDR,                      // 角色使用多重元素抵抗技能，免疫多種元素傷害
    ElementResetBySummon,           //元素初始化
    BahamutLightElemAddDam,         // 巴哈姆特技能的光屬性傷害增加效果
    LefDebuff,                      // 角色受到詛咒標記效果
    BossPropPlus,                   // 角色對Boss怪物造成的傷害增加效果
    MultiDamSkill,                  // 角色使用多重傷害技能，造成額外傷害
    RWLiftPress,                    // 船長職業的技能效果，抬起敵人
    RWChoppingHammer,               // 船長職業的技能效果，使用大砍錘
    MobAggro,                       // 未知效果的角色增益效果
    ActionState,                    // 未知效果的角色增益效果
    AreaPDR,                        // 菈菈的神木技能，範圍減少怪物防禦
    BuffControl,                    //
    IgnoreFreeze,                   //
    BattlePvP_Ryude_Frozen,         //
    DamR,                           //
    ContinuousHeal,                 //
    HiddenPullingDebuff,            //
    CurseTransition,                // 角色受到遠古詛咒效果影響
    WindBreakerPinpointPierce,      //
    Morph,                          //
    MobLock,                        //
    LWGathering,                    //
    KinesisLawOfGravity,            //
    TargetPlus,                     //
    ReviveOnce,                     //
    BuffFlag,                       //
    HolyShell,                      //
    ZeroCriticalBind,               //
    Panic,                          // 傷痕之劍
    ReduceFinalDamage,              //
    Stalking,                       //
    ChangeMobAction,
    KannaAddAttack,                 //
    ErdaRevert,                     //
    //CatKnitting,                    //
    SummonGhost,                    //
    TimeBomb,                      //
    AddEffect,                     //
    Invincible(true),     //不可戰勝
    Explosion,                     //爆炸
    HangOver,                      //掛起
    Sleep,                         //
    LevelInc,      //
    AfterImage,                    //

    // 非官方命名
    Burned(true),//燃燒
    BalogDisable(true),//
    ExchangeAttack(true),//交換攻擊
    AddBuffStat(true),//增加buff屬性
    LinkTeam(true),//連接隊伍
    SoulExplosion(true),//靈魂爆炸
    SeperateSoulP(true),//分離靈魂p
    SeperateSoulC(true),//分離靈魂c
    TrueSight(true),//真實視野
    //Unk_2          ( true),
    Ember(true),//灰燼
    Unk_3(true),
    StatResetSkill(true),
    NEWUNK96(true),
    NEWUNK97(true),
    NEWUNK98(true),
    NEWUNK99(true),
    NEWUNK100(true),
    NEWUNK101(true),
    NEWUNK102(true),
    Laser(true),//激光
    Unk_163_Add_107(true),
    NEWUNK132(true),
    NEWUNK133(true),
    ;
    

    /*IndiePDR(0x80000000, 0, true),    // 角色受到物理傷害減少效果影響
    IndieMDR(0x40000000, 0, true),    // 角色受到魔法傷害減少效果影響
    IndieAddFinalDamSkill(0x20000000, 0, true),   // 角色受到最終傷害減少效果影響
    IndieSlow(0x10000000, 0, true),  // 角色獲得速度增加效果

    // 0x8000000, 0
    PAD(0x4000000, 0),//物理攻擊
    PDR(0x2000000, 0),//物理防禦
    MAD(0x1000000, 0),//魔法攻擊
    MDR(0x800000, 0),//魔法防禦
    ACC(0x400000, 0),//命中
    EVA(0x200000, 0),//迴避
    Speed(0x100000, 0),//減速
    Stun(0x80000, 0),//眩暈
    Freeze(0x40000, 0),//極凍吐息
    Poison(0x20000, 0),//中毒
    Seal(0x10000, 0),//封印
    Darkness(0x8000, 0),//黑暗
    PowerUp(0x4000, 0),//物理攻擊提升
    MagicUp(0x2000, 0),//魔法攻擊提升
    PGuardUp(0x1000, 0),//物理防禦提升
    MGuardUp(0x800, 0),//魔法防禦提升
    PImmune(0x400, 0),//物理免疫
    MImmune(0x200, 0),//魔法免疫
    Web(0x100, 0),//影網術
    HardSkin(0x80, 0),//硬化皮膚
    Ambush(0x40, 0),//忍者伏擊，此技能好像已刪除
    Venom(0x20, 0),//巫毒術，此技能好像已刪除
    Blind(0x10, 0),//黑暗，英雄.恐慌，降低命中
    SealSkill(0x8, 0),//封印技能
    Dazzle(0x4, 0),//閃光？
    PCounter(0x2, 0),//物理反射
    MCounter(0x1, 0),//魔法反射
    RiseByToss(0x80000000, 1),//扔上天？
    BodyPressure(0x40000000, 1),//身體壓力？
    Weakness(0x20000000, 1),//虛弱
    Showdown(0x10000000, 1),//攤開？
    DevilCry(31, 1),
    MagicCrash(0x8000000, 1),         // 角色使用魔法消除技能，取消敌人的魔法效果
    DamagedElemAttr(0x4000000, 1),    // 角色受到元素属性伤害，可能用于标记特定的伤害类型
    TotalDamParty(0x1000000, 1),//增加怪物受到的傷害
    HitCriDamR(0x800000, 1),           // 增加角色的暴击伤害比例
    Fatality(0x400000, 1),             // 角色使用致命一击技能，造成致命伤害
    Lifting(0x200000, 1),              // 角色使用抬起技能，将怪物抬起
    DeadlyCharge(0x100000, 1),         // 角色使用致命冲锋技能，造成致命伤害
    Smite(0x80000, 1),                 // 角色使用重击技能，增加攻击力
    AddDamSkil(0x40000, 1),            // 角色使用伤害增加技能
    Incizing(0x20000, 1),              // 角色使用切割技能，增加攻击力
    DodgeBodyAttack(0x10000, 1),       // 角色闪避物理攻击
    DebuffHealing(0x8000, 1),          // 角色在解除负面状态时恢复生命值
    AddDamSkill2(0x4000, 1),           // 角色使用伤害增加技能
    CUserLocal_BodyAttack(0x2000, 1),             // 角色使用身体攻击技能
    TempMoveAbility(0x1000, 1),        // 角色获得临时移动能力
    FixDamRBuff(0x800, 1),             // 角色使用固定伤害技能，增加伤害
    GhostDisposition(0x400, 1),                // 未知效果的角色增益效果
    ElementDarkness(0x200, 1),   //SUMMON_元素闇黑
    AreaInstallByHit(0x100, 1),      // 在击中区域内产生效果
    BMageDebuff(0x80, 1),            // 战神职业的负面状态效果
    JaguarProvoke(0x40, 1),//豹弩_引發
    JaguarBleeding(0x20, 1),//豹弩_出血
    DarkLightning(0x10, 1),                  // 角色使用黑暗闪电技能，造成黑暗伤害 // remove
    PinkbeanFlowerPot(0x10, 1),              // 角色召唤品克缤的花盆，产生效果
    BattlePvP_Helena_Mark(0x8, 1),           // 在战斗PvP中，赫连娜标记效果
    PsychicLock(0x4, 1),                     // 角色使用灵魂锁定技能，造成效果
    PsychicLockCoolTime(0x2, 1),             // 角色使用灵魂锁定冷却时间效果
    PsychicGroundMark(0x1, 1),               // 角色使用灵魂地面标记技能，产生效果
    PowerImmune(0x80000000, 2),              // 角色免疫强力效果
    MultiPMDR(0x40000000, 2),                // 角色使用多重元素抵抗技能，免疫多种元素伤害
    ElementResetBySummon(0x20000000, 2),//元素初始化
    BahamutLightElemAddDam(0x10000000, 2),   // 巴哈姆特技能的光属性伤害增加效果
    LefDebuff(0x8000000, 2),                 // 角色受到诅咒标记效果
    BossPropPlus(0x4000000, 2),              // 角色对Boss怪物造成的伤害增加效果
    MultiDamSkill(0x2000000, 2),             // 角色使用多重伤害技能，造成额外伤害
    RWLiftPress(0x1000000, 2),               // 船长职业的技能效果，抬起敌人
    RWChoppingHammer(0x800000, 2),           // 船长职业的技能效果，使用大砍锤
    MobAggro(0x400000, 2),                      // 未知效果的角色增益效果
    ActionState(0x200000, 2),                      // 未知效果的角色增益效果
    Ticktock(0x100000, 2),                   // 角色获得滴答声效果
    AreaPDR(0x100000, 2),
    BuffControl(0x80000, 2),                       // 未知效果的角色增益效果
    IgnoreFreeze(0x40000, 2),                       // 未知效果的角色增益效果
    BattlePvP_Ryude_Frozen(0x20000, 2),                       // 未知效果的角色增益效果
    DamR(0x10000, 2),
    ContinuousHeal(0x8000, 2),
    HiddenPullingDebuff(0x4000, 2),
    CurseTransition(0x2000, 2),              // 角色受到远古诅咒效果影响
    WindBreakerPinpointPierce(0x1000, 2),            // 未知效果的角色增益效果
    Morph(0x800, 2),
    MobLock(0x400, 2),
    LWGathering(0x200, 2),
    KinesisLawOfGravity(0x100, 2),
    TargetPlus(0x80, 2),
    ReviveOnce(0x40, 2),
    BuffFlag(0x20, 2),
    HolyShell(0x10, 2),
    ZeroCriticalBind(0x8, 2),
    Panic(0x4, 2), // 傷痕之劍
    ReduceFinalDamage(0x2, 2),
    Stalking(0x1, 2),
    KannaAddAttack(0x1, 2),
    ErdaRevert(0x1, 2),
    CatKnitting(0x80000000, 3),
    SummonGhost(0x40000000, 3),
    TimeBomb(0x40000000, 3),
    Treasure(0x40000000, 3),//寶藏 //remove
    AddEffect(0x20000000, 3),     // 角色添加效果
    HillaCount(94), // remove
    Invincible(0x8000000, 3, true),//不可戰勝
    Explosion(0x4000000, 3),//爆炸
    HangOver(0x2000000, 3),//掛起
    Sleep(0x1000000, 3),
    LevelInc(0x800000, 3),
    AfterImage(0x800000, 3),
    Burned(0x400000, 3, true),//燃燒
    BalogDisable(0x200000, 3, true),//
    ExchangeAttack(0x100000, 3, true),//交換攻擊
    AddBuffStat(0x80000, 3, true),//增加buff屬性
    LinkTeam(0x40000, 3, true),//連接隊伍
    SoulExplosion(0x20000, 3, true),//靈魂爆炸
    SeperateSoulP(0x10000, 3, true),//分離靈魂p
    SeperateSoulC(0x8000, 3, true),//分離靈魂c
    Ember(0x4000, 3, true),//灰燼
    TrueSight(0x2000, 3, true),//真實視野
    Unk_2(0x1000, 3, true),
    Laser(0x800, 3, true),//激光
    StatResetSkill(0x400, 3, true),
    NEWUNK96(0x200, 3, true),
    NEWUNK97(0x100, 3, true),
    NEWUNK98(0x80, 3, true),
    NEWUNK99(0x40, 3, true),
    NEWUNK100(0x20, 3, true),
    NEWUNK101(0x10, 3, true),
    NEWUNK102(0x8, 3, true),
    Unk_163_Add_107(0x4, 3, true),
    Unk_232_Add_1(0x2, 3, true),
    NONE(0, 0);*/

    private static final long serialVersionUID = 0L;
    private final int position;
    private final int flag;
    private final boolean isDefault;
    private final int value;
    private boolean stacked;

//     MonsterStatus(int value, int position) {
// //        this.i = value;
//         this.position = position;
// //        this.i = 1 << 31 - value % 32;
// //        this.position = (int) Math.floor(value / 32);
//         this.isDefault = false;
//         this.value = value;
//     }

//     MonsterStatus(int value, int position, boolean isDefault) {
// //        this.i = value;
//         this.position = position;
// //        this.i = 1 << 31 - value % 32;
// //        this.position = (int) Math.floor(value / 32);
//         this.isDefault = isDefault;
//         this.value = value;
//     }

    MonsterStatus() {
        this.position = this.ordinal() >> 5;
        this.flag = this.ordinal();
        this.value = 1 << (31 - (flag & 31));
        this.isDefault = false;
        setStacked(name().startsWith("Indie"));
    }

    MonsterStatus(boolean isDefault) {
        this.position = this.ordinal() >> 5;
        this.flag = this.ordinal();
        this.value = 1 << (31 - (flag & 31));
        this.isDefault = isDefault;
        setStacked(name().startsWith("Indie"));
    }

    MonsterStatus(int flag) {
        this.position = flag >> 5;
        this.flag = flag;
        this.value = 1 << (31 - (flag & 31));
        this.isDefault = false;
        setStacked(name().startsWith("Indie"));
    }

    MonsterStatus(int flag, boolean isDefault) {
        this.position = flag >> 5;
        this.flag = flag;
        this.value = 1 << (31 - (flag & 31));
        this.isDefault = isDefault;
        setStacked(name().startsWith("Indie"));
    }

    public int getFlag() {
        return this.flag;
    }

    public boolean isStacked() {
        return this.stacked;
    }

    public void setStacked(boolean stacked) {
        this.stacked = stacked;
    }

    /* 通用技能 */
    public static int genericSkill(MonsterStatus stat) {
        switch (stat) {
            case Stun: {
                return 90001001;
            }
            case Speed: {
                return 90001002;
            }
            case Poison: {
                return 90001003;
            }
            case PCounter: {
                return 90001004;
            }
            case Seal: {
                return 90001005;
            }
            case MagicCrash: {
                return 1111007;
            }
            case Darkness: {
                return 4121003;
            }
            case Venom: {
                return 5211004;
            }
            case Ambush: {
                return 4121004;
            }
            case Explosion: {
                return 傑諾.三角列陣;
            }
        }
        return 0;
    }

    @Override
    public int getPosition() {
        return position;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public int getValue() {
        return value;
    }

    public Integer getOrder() {
        return position;
    }

    public boolean isIndieStat() {
        return ordinal() < PAD.ordinal() || this == Burned;
    }

    @Override
    public String toString() {
        return name() + "(0x" + Integer.toHexString(value) + ", " + position + ")";
    }
}
