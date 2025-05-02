package Client.stat;

import Client.*;
import Client.hexa.HexaFactory;
import Client.hexa.HexaStatCoreEntry;
import Client.hexa.MapleHexaStat;
import Client.inventory.*;
import Client.skills.InnerSkillEntry;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Config.constants.SkillConstants;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.皇家騎士團_技能群組.*;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.collection.SoulCollectionEntry;
import Net.server.life.Element;
import Packet.BuffPacket;
import Packet.EffectPacket;
import Packet.InventoryPacket;
import Plugin.provider.loaders.SkillData;
import Server.world.WorldGuildService;
import Server.world.guild.MapleGuild;
import Server.world.guild.MapleGuildSkill;
import SwordieX.enums.BaseStat;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class PlayerStats {

    static final Logger log = LoggerFactory.getLogger(PlayerStats.class);
    public final static int[] pvpSkills = {1000007, 2000007, 3000006, 4000010, 5000006, 5010004, 11000006, 12000006, 13000005, 14000006, 15000005, 21000005, 22000002, 23000004, 31000005, 32000012, 33000004, 35000005};
    private long nextRecalcStats;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Integer, List<Integer>> setHandling = new HashMap<>();
    private final Map<Integer, Integer> add_skill_damage = new HashMap<>();
    public final Map<Integer, Pair<Integer, Integer>> hpRecover_onAttack = new HashMap<>(); // 攻擊時概率回復HP

    /*
     * 對特定技能的強化
     */
    private final Map<Integer, Integer> add_skill_duration = new HashMap<>(); //增加持續時間
    private final Map<Integer, Integer> add_skill_attackCount = new HashMap<>(); //增加攻擊怪物的次數
    private final Map<Integer, Integer> add_skill_targetPlus = new HashMap<>();  //增加攻擊怪物的數量
    private final Map<Integer, Integer> add_skill_bossDamage = new HashMap<>();  //增加攻擊BOSS的傷害
    private final Map<Integer, Integer> add_skill_dotTime = new HashMap<>();  //對怪物持續傷害BUFF
    private final Map<Integer, Integer> add_skill_prop = new HashMap<>();  //概率
    private final Map<Integer, Integer> add_skill_coolTimeR = new HashMap<>();  //減少技能的冷卻時間
    private final Map<Integer, Integer> add_skill_ignoreMobpdpR = new HashMap<>();  //增加技能攻擊怪物無視防禦
    private final Map<Integer, Integer> add_skill_damage_5th = new HashMap<>(); // 5轉強化技能增加最終傷害
    private final Map<Integer, Integer> add_skill_bulletCount = new HashMap<>(); // 消耗子彈類道具數量增加
    private final Map<Integer, Integer> add_skill_reduceForceCon = new HashMap<>(); // 消耗惡魔精氣減少百分比
    private final Map<Integer, Integer> add_skill_custom_val = new HashMap<>(); //增加持续时间

    @Getter
    public short str, dex, luk, int_;
    @Getter
    public int hp;
    @Getter
    public int mp;
    public int maxhp;
    public int maxmp;
    public int apAddMaxHp;
    public int apAddMaxMp;
    public boolean hasClone, hasPartyBonus;
    public int expBuff, dropBuff, mesoBuff, cashBuff, expRPerM, plusExpRate;
    public double mesoGuard, mesoGuardMeso, incRewardProp;
    //same with incMesoProp/incRewardProp for now
    public int recoverHP, recoverMP, mpconReduce, incMesoRate, incDropRate, incMpCon, incMesoProp, reduceCooltime, cooltimeReduceR, suddenDeathR, expLossReduceR,
            mpRestore, hpRecover_Percent, itemRecoveryUP, BuffUP, skillRecoveryUP, incBuffTime, summonTimeR,
            incAllskill, combatOrders, BuffUP_Summon, dodgeChance, nocoolProp,
            asrR, terR, pickRate,
            pvpDamage, hpRecoverTime = 0, mpRecoverTime = 0, dot, incDotTime, pvpRank, pvpExp, damX, incMaxDamage, incMaxDF,
            gauge_x, mpconMaxPercent, expCardRate, dropCardRate;
    /**
     * 物理防禦力增加#pddX
     */
    public int wdef;
    /**
     * 默認攻擊距離
     */
    public int defRange;
    /**
     * 武器熟練度
     */
    int trueMastery;
    /**
     * 攻擊速度
     */
    public int attackSpeed;
    /**
     * 移動速度
     */
    public int speed;
    /**
     * 最大移動速度
     */
    public int speedMax;
    /**
     * 跳躍力
     */
    public int jump;
    /**
     * 爆擊概率#cr%
     */
    public short critRate;
    /**
     * 爆擊最大傷害倍率#criticaldamage
     */
    public double criticalDamage;
    /**
     * 爆擊最小傷害倍率
     */
    public short incCritDamage;
    /**
     * 格擋增加#stanceProp%
     */
    public int stanceProp;
    /**
     * 物理防禦力#pddR%增加
     */
    public int percent_wdef;
    /**
     * 魔法防禦力#mddR%增加
     */
    public int percent_mdef;
    /**
     * 最大HP增加#mhpR%
     */
    public int incMaxHPR;
    /**
     * 最大MP增加#mmpR%
     */
    public int incMaxMPR;
    /**
     * 力量增加x%
     */
    public int incStrR;
    /**
     * 敏捷增加x%
     */
    public int incDexR;
    /**
     * 智力增加x%
     */
    public int incIntR;
    /**
     * 幸運增加x%
     */
    public int incLukR;
    /**
     * 命中增加x%
     */
    public int percent_acc;
    /**
     * 物理攻擊力增加x%
     */
    public int incPadR;
    /**
     * 魔法攻擊力增加x%
     */
    public int incMadR;
    /**
     * 無視怪x%防禦
     */
    public double ignoreMobpdpR;
    /**
     * 攻擊增加 也就是角色面板的攻擊數字增加x%
     */
    public double percent_damage;
    /**
     * 傷害增加x%
     */
    public double incDamR;
    /**
     * BOSS傷害增加x%
     */
    public int bossDamageR;
    /**
     * 被怪物攻擊受到的傷害減少x%
     */
    public int ignore_mob_damage_rate;
    /**
     * 被動減少受到的傷害的x%
     */
    public double damAbsorbShieldR;
    /**
     * 最終傷害增加x%
     */
    public double incFinalDamage;
    // Elemental properties
    public int raidenCount, raidenPorp; //奇襲者雷電能夠獲得數量和概率
    /**
     * 回復HP%
     */
    public int healHPR;
    /**
     * 回復MP%
     */
    public int healMPR;
    /**
     * 武器熟練度
     */
    private byte passive_mastery;
    private int localstr, localdex, localluk, localint, localmaxhp, localmaxmp, addmaxhp, addmaxmp, lv2mmp;
    private int indieStrFX, indieDexFX, indieLukFX, indieIntFX, indieMhpFX, indieMmpFX, indiePadFX, indieMadFX; //內在技能增加屬性 不計算潛能的百分比增加屬性
    private int mad;
    private int pad;
    private long localbasedamage_max, localbasedamage_min, localmaxbasepvpdamage, localmaxbasepvpdamageL;
    public int maxBeyondLoad;
    public int hpRecover_limit, mpRecover_limit;
    private int finalAttackSkill;
    public int incAttackCount;
    @Getter
    private int incEXPr;
    public int ignoreElement;// 減少攻擊屬性抗性（自然力重置）
    public int reduceForceR;
    public int mpcon_eachSecond;
    private int betamaxhp, betamaxmp; //神之子beta的hp/mp
    private int kannaLinkDamR;
    private int summoned;
    private int arc;

    private final EquipRecalcableStats equipstats = new EquipRecalcableStats();
    @Getter
    private int aut;

    public long getCurrentMaxHp() {
        return this.localmaxhp;
    }

    public long getCurrentMaxMp(MapleCharacter chr) {
        return this.localmaxmp;
    }

    public void recalcLocalStats(MapleCharacter chra) {
        recalcLocalStats(false, chra);
    }

    public void resetLocalStats() {
        apAddMaxHp = 0;
        apAddMaxMp = 0;
        wdef = 0;
        damX = 0;
        addmaxhp = 0;
        addmaxmp = 0;
        lv2mmp = 0;
        localdex = getDex();
        localint = getInt();
        localstr = getStr();
        localluk = getLuk();
        indieDexFX = 0;
        indieIntFX = 0;
        indieStrFX = 0;
        indieLukFX = 0;
        indieMhpFX = 0;
        indieMmpFX = 0;
        speed = 100;
        speedMax = 140;
        jump = 100;
        asrR = 0;
        terR = 0;
        dot = 0;
        incDotTime = 0;
        trueMastery = 0;
        stanceProp = 0;
        percent_wdef = 0;
        percent_mdef = 0;
        incMaxHPR = 0;
        incMaxMPR = 0;
        incStrR = 0;
        incDexR = 0;
        incIntR = 0;
        incLukR = 0;
        percent_acc = 0;
        incPadR = 0;
        incMadR = 0;
        ignoreMobpdpR = 0;
        critRate = 5;
        criticalDamage = 0;
        incDamR = 0.0;
        bossDamageR = 0;
        mad = 0;
        pad = 0;
        dodgeChance = 0;
        nocoolProp = 0;
        pvpDamage = 0;
        mesoGuard = 50.0;
        mesoGuardMeso = 0.0;
        percent_damage = 0.0;
        expBuff = 0;
        cashBuff = 0;
        dropBuff = 0;
        mesoBuff = 0;
        expRPerM = 0;
        plusExpRate = 0;
        expCardRate = 100;
        dropCardRate = 100;
        recoverHP = 0;
        recoverMP = 0;
        mpconReduce = 0;
        incMpCon = 100;
        incMesoProp = 0;
        incMesoRate = 0;
        incDropRate = 0;
        reduceCooltime = 0;
        cooltimeReduceR = 0;
        summonTimeR = 0;
        suddenDeathR = 0;
        expLossReduceR = 0;
        incRewardProp = 0.0; //潛能道具所加的裝備掉落幾率
        hpRecover_Percent = 0;
        mpRestore = 0;
        pickRate = 0;
        incMaxDamage = 0;
        hasPartyBonus = false;
        hasClone = false;
        itemRecoveryUP = 0;
        BuffUP = 0;
        skillRecoveryUP = 0;
        incBuffTime = 0;
        BuffUP_Summon = 0;
        incMaxDF = 0;
        incAllskill = 0;
        combatOrders = 0;
        add_skill_damage.clear();
        setHandling.clear();
        add_skill_bossDamage.clear();
        add_skill_duration.clear(); //超級技能格外增加持續時間
        add_skill_attackCount.clear(); //超級技能格外增加攻擊次數
        add_skill_targetPlus.clear();
        add_skill_dotTime.clear();
        add_skill_prop.clear();
        add_skill_coolTimeR.clear();
        add_skill_ignoreMobpdpR.clear();
        add_skill_damage_5th.clear();
        add_skill_bulletCount.clear();
        add_skill_reduceForceCon.clear();
        raidenCount = 0;
        raidenPorp = 0;
        ignore_mob_damage_rate = 0;
        damAbsorbShieldR = 0;
        mpconMaxPercent = 0;
        maxBeyondLoad = 20;
        hpRecover_limit = 100;
        mpRecover_limit = 100;
        finalAttackSkill = 0;
        incFinalDamage = 1;
        hpRecover_onAttack.clear();
        incAttackCount = 0;
        incEXPr = 0;
        ignoreElement = 0;
        reduceForceR = 0;
        mpcon_eachSecond = 0;
        kannaLinkDamR = 0;
        hpRecoverTime = 0;
        arc = 0;
        aut = 0;
    }

    private void sumEquipLocalStats() {
        wdef += equipstats.wdef;
        addmaxhp += equipstats.addmaxhp;
        addmaxmp += equipstats.addmaxmp;
        localdex += equipstats.localdex;
        localint += equipstats.localint;
        localstr += equipstats.localstr;
        localluk += equipstats.localluk;
        indieDexFX += equipstats.indieDexFX;
        indieIntFX += equipstats.indieIntFX;
        indieStrFX += equipstats.indieStrFX;
        indieLukFX += equipstats.indieLukFX;
        indieMhpFX += equipstats.indieMhpFX;
        indieMmpFX += equipstats.indieMmpFX;
        speed += equipstats.speed;
        jump += equipstats.jump;
        asrR += equipstats.asr;
        terR += equipstats.terR;
        percent_wdef += equipstats.percent_wdef;
        incMaxHPR += equipstats.incMaxHPR;
        incMaxMPR += equipstats.incMaxMPR;
        incStrR += equipstats.incStrR;
        incDexR += equipstats.incDexR;
        incIntR += equipstats.incIntR;
        incLukR += equipstats.incLukR;
        incPadR += equipstats.incPadR;
        incMadR += equipstats.incMadR;
        critRate += equipstats.critRate;
        criticalDamage += equipstats.criticalDamage;
        incDamR += equipstats.incDamR;
        bossDamageR += equipstats.bossDamageR;
        mad += equipstats.mad;
        pad += equipstats.pad;
        pvpDamage += equipstats.pvpDamage;
        recoverHP += equipstats.recoverHP;
        recoverMP += equipstats.recoverMP;
        mpconReduce += equipstats.mpconReduce;
        incMesoProp += equipstats.incMesoProp;
        reduceCooltime += equipstats.reduceCooltime;
        incRewardProp += equipstats.incRewardProp;
        incBuffTime += equipstats.incBuffTime;
        incMaxDF += equipstats.incMaxDF;
        incAllskill += equipstats.incAllskill;
        incAttackCount += equipstats.incAttackCount;
        addIgnoreMobpdpR(equipstats.ignoreMobpdpR);
        expCardRate = equipstats.expCardRate;
        dropCardRate = equipstats.dropCardRate;
        arc += equipstats.arc;
        aut += equipstats.aut;
    }

    public void recalcLocalStats(boolean first_login, MapleCharacter chra) {
        recalcLocalStats(first_login, chra, -1);
    }

    public void recalcLocalStatsInterrupt(boolean first_login, MapleCharacter chra, int flag) throws InterruptedException {
        if (lock.writeLock().tryLock(1, TimeUnit.SECONDS)) {
            try {
                doRecalc(first_login, chra, flag);
//            if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
//                chra.updatePartyMemberHP();
//                chra.checkBloodContract();
//            }
                //增加或改變技能等級
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public void recalcLocalStats(boolean first_login, MapleCharacter chra, int flag) {
        lock.writeLock().lock();
        try {
            doRecalc(first_login, chra, flag);
//            if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
//                chra.updatePartyMemberHP();
//                chra.checkBloodContract();
//            }
            //增加或改變技能等級
        } finally {
            lock.writeLock().unlock();
        }
        //System.out.println("裝備潛能道具掉落幾率 :" + incRewardProp + " 最後爆率: " + getDropBuff());
    }

    private void doRecalc(boolean first_login, MapleCharacter chra, int flag) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        resetLocalStats();
        if (chra.getHpApUsed() > 0) {
            if (JobConstants.is零轉職業(chra.getJob())) {
                apAddMaxHp += 12 * chra.getHpApUsed();
            } else if (JobConstants.is惡魔復仇者(chra.getJob())) {
                apAddMaxHp += 30 * chra.getHpApUsed();
            } else if (JobConstants.is狂狼勇士(chra.getJob()) || JobConstants.is煉獄巫師(chra.getJob())) {
                apAddMaxHp += 40 * chra.getHpApUsed();
            } else if (JobConstants.is劍士(chra.getJob())) {
                apAddMaxHp += 32 * chra.getHpApUsed();
            } else if (JobConstants.is法師(chra.getJob())) {
                apAddMaxHp += 10 * chra.getHpApUsed();
            } else if (JobConstants.is弓箭手(chra.getJob()) || JobConstants.is盜賊(chra.getJob())) {
                apAddMaxHp += 15 * chra.getHpApUsed();
            } else if (JobConstants.is海盜(chra.getJob())) {
                apAddMaxHp += 22 * chra.getHpApUsed();
            }
        }
        if (chra.getMpApUsed() > 0) {
            if (JobConstants.is零轉職業(chra.getJob())) {
                apAddMaxMp += 8 * chra.getMpApUsed();
            } else if (!JobConstants.isNotMpJob(chra.getJob())) {
                if (JobConstants.is劍士(chra.getJob())) {
                    apAddMaxMp += 4 * chra.getMpApUsed();
                } else if (JobConstants.is法師(chra.getJob())) {
                    apAddMaxMp += 30 * chra.getMpApUsed();
                } else if (JobConstants.is弓箭手(chra.getJob()) || JobConstants.is盜賊(chra.getJob()) || JobConstants.is海盜(chra.getJob())) {
                    apAddMaxMp += 10 * chra.getMpApUsed();
                }
            }
        }
        int localmaxhp_ = getMaxHp();
        int localmaxmp_ = getMaxMp();
        for (MapleTraitType t : MapleTraitType.values()) {
            chra.getTrait(t).clearLocalExp();
        }
        if (first_login || RecalcFlag.Equip.check(flag)) {
            equipstats.recalcLocalStats(first_login, chra);
        }
        sumEquipLocalStats();
        localmaxhp_ += equipstats.localmaxhp_;
        localmaxmp_ += equipstats.localmaxmp_;

        //技能加屬性
        handlePassiveSkills(chra);

        //Hexa屬性
        handleHexaStat(chra);

        //添加BUFF加屬性效果
        handleBuffStats(chra);

        //計算熟練度
        CalcPassive_Mastery(chra, equipstats.wt);

        Skill bx;
        int bof;
        /*
         * 公會技能加屬性
         */
        if (chra.getGuildId() > 0) {
            MapleGuild g = WorldGuildService.getInstance().getGuild(chra.getGuildId());
            if (g != null && g.getSkills().size() > 0) {
                long now = System.currentTimeMillis();
                for (MapleGuildSkill gs : g.getSkills()) {
                    Skill skill = SkillFactory.getSkill(gs.getSkillId());
                    if (skill != null && gs.getTimestamp() > now && gs.getActivator().length() > 0) {
                        MapleStatEffect e = skill.getEffect(gs.getLevel());
                        critRate += e.getCritical();
                        pad += e.getPadX();
                        mad += e.getMadX();
                        expBuff += e.getExpR();
                        dodgeChance += e.getER();
                        percent_wdef += e.getPddR();
                        percent_mdef += e.getMDEFRate();
                    }
                }
            }
        }

        // 角色卡系統加屬性
        List<Integer> cardSkills = new LinkedList<>();
        for (Pair<Integer, Integer> ix : chra.getCharacterCard().getCardEffects()) {
            Skill skill = SkillFactory.getSkill(ix.getLeft());
            if (skill == null) {
                continue;
            }
            MapleStatEffect cardEff = skill.getEffect(ix.getRight());
            if (cardEff == null || (cardSkills.contains(ix.getLeft()) && ix.getLeft() < 71001100)) {
                continue;
            }
            cardSkills.add(ix.getLeft());
            percent_wdef += cardEff.getPddR();
            incMaxHPR += cardEff.getMhpR();
            incMaxMPR += cardEff.getMmpR();
            critRate += cardEff.getCritical();
            criticalDamage += cardEff.getCriticalDamage();
            itemRecoveryUP += cardEff.getMPConsumeEff();
            percent_acc += cardEff.getPercentAcc();
            dodgeChance += cardEff.getPercentAvoid();
            jump += cardEff.getPsdJump();
            speed += cardEff.getPsdSpeed();
            expLossReduceR += cardEff.getEXPLossRate();
            asrR += cardEff.getASRRate();
            suddenDeathR += (cardEff.getSuddenDeathR() * 0.5);
            incMesoProp += cardEff.getMesoAcquisition();
            localstr += cardEff.getStrX();
            localdex += cardEff.getDexX();
            localint += cardEff.getIntX();
            localluk += cardEff.getLukX();
            indieStrFX += cardEff.getStrFX();
            indieDexFX += cardEff.getDexFX();
            indieIntFX += cardEff.getIntFX();
            indieLukFX += cardEff.getLukFX();
            localmaxhp_ += cardEff.getMaxHpX();
            localmaxmp_ += cardEff.getMaxMpX();
            pad += cardEff.getPadX();
            mad += cardEff.getMadX();
            bossDamageR += cardEff.getBossDamage();
            cooltimeReduceR += cardEff.getCooltimeReduceR();
            incBuffTime += cardEff.getBuffTimeRate();
            addIgnoreMobpdpR(cardEff.getIgnoreMobpdpR());
            indiePadFX += (cardEff.getLevelToWatkX() * chra.getLevel() * 0.5);
            indieMadFX += (cardEff.getLevelToMatkX() * chra.getLevel() * 0.5);
            addDamAbsorbShieldR(cardEff.getIgnoreMobDamR());
            summonTimeR += cardEff.getSummonTimeInc();
        }
        cardSkills.clear();

        // 楓之谷聯盟系統
        List<Integer> unionSkill = new LinkedList<>();
        if (chra.getMapleUnion() != null && chra.getMapleUnion().getState() > 0) {
            for (Entry<Integer, Integer> entry : chra.getMapleUnion().getSkills().entrySet()) {
                Skill skill = SkillFactory.getSkill(entry.getKey());
                if (skill == null) {
                    continue;
                }
                MapleStatEffect unionEff = skill.getEffect(entry.getValue());
                if (unionEff == null || (unionSkill.contains(entry.getKey()) && entry.getKey() < 71001100)) {
                    continue;
                }
                unionSkill.add(entry.getKey());
                percent_wdef += unionEff.getPddR();
                incMaxHPR += unionEff.getMhpR();
                incMaxMPR += unionEff.getMmpR();
                critRate += unionEff.getCritical();
                criticalDamage += unionEff.getCriticalDamage();
                jump += unionEff.getPsdJump();
                speed += unionEff.getPsdSpeed();
                asrR += unionEff.getASRRate();
                localstr += unionEff.getStrX();
                localdex += unionEff.getDexX();
                localint += unionEff.getIntX();
                localluk += unionEff.getLukX();
                indieMhpFX += unionEff.getHpFX();
                indieStrFX += unionEff.getStrFX();
                indieDexFX += unionEff.getDexFX();
                indieIntFX += unionEff.getIntFX();
                indieLukFX += unionEff.getLukFX();
                addmaxhp += unionEff.getMaxHpX();
                addmaxmp += unionEff.getMaxMpX();
                pad += unionEff.getPadX();
                mad += unionEff.getMadX();
                bossDamageR += unionEff.getBossDamage();
                cooltimeReduceR += unionEff.getCooltimeReduceR();
                incBuffTime += unionEff.getBuffTimeRate();
                addIgnoreMobpdpR(unionEff.getIgnoreMobpdpR());
                indiePadFX += (int) (unionEff.getLevelToWatkX() * chra.getLevel() * 0.5);
                indieMadFX += (int) (unionEff.getLevelToMatkX() * chra.getLevel() * 0.5);
                addDamAbsorbShieldR(unionEff.getIgnoreMobDamR());
                summonTimeR += unionEff.getSummonTimeInc();
            }
            for (int i = 0; i < chra.getMapleUnion().getAddStats().length; ++i) {
                final String oneInfo = chra.getWorldShareInfo(18791, String.valueOf(i));
                final int add = chra.getMapleUnion().getAddStats()[i];
                if (oneInfo != null && add > 0) {
                    switch (Integer.valueOf(oneInfo)) {
                        case 0: {
                            localstr += add * 5;
                            break;
                        }
                        case 1: {
                            localdex += add * 5;
                            break;
                        }
                        case 2: {
                            localint += add * 5;
                            break;
                        }
                        case 3: {
                            localluk += add * 5;
                            break;
                        }
                        case 4: {
                            pad += add;
                            break;
                        }
                        case 5: {
                            mad += add;
                            break;
                        }
                        case 6: {
                            addmaxhp += add * 250;
                            break;
                        }
                        case 7: {
                            addmaxmp += add * 250;
                            break;
                        }
                        case 8: {
                            criticalDamage += add;
                            break;
                        }
                        case 9: {
                            asrR += add;
                            break;
                        }
                        case 10: {
                            //
                            break;
                        }
                        case 11: {
                            critRate += add;
                            break;
                        }
                        case 12: {
                            bossDamageR += add;
                            break;
                        }
                        case 13: {
                            stanceProp += add;
                            break;
                        }
                        case 14: {
                            incBuffTime += add;
                            break;
                        }
                        case 15: {
                            addIgnoreMobpdpR(add);
                            break;
                        }
                    }
                }
            }
        }
        unionSkill.clear();

        // 內在能力技能加屬性
        for (int i = 0; i < 3; i++) {
            InnerSkillEntry innerSkill = chra.getInnerSkills()[i];
            if (innerSkill == null) {
                continue;
            }
            MapleStatEffect innerEffect = SkillFactory.getSkill(innerSkill.getSkillId()).getEffect(innerSkill.getSkillLevel());
            if (innerEffect == null) {
                continue;
            }
            wdef += innerEffect.getPddX();
            percent_wdef += innerEffect.getPddR();
            percent_mdef += innerEffect.getMDEFRate();
            incMaxHPR += innerEffect.getMhpR();
            incMaxMPR += innerEffect.getMmpR();
            dodgeChance += innerEffect.getPercentAvoid();
            nocoolProp += innerEffect.getNocoolProp();
            critRate += innerEffect.getCritical();
            criticalDamage += innerEffect.getCriticalDamage();
            jump += innerEffect.getPsdJump();
            speed += innerEffect.getPsdSpeed();
            indieStrFX += innerEffect.getStrFX();
            indieDexFX += innerEffect.getDexFX();
            indieIntFX += innerEffect.getIntFX();
            indieLukFX += innerEffect.getLukFX();
            localmaxhp_ += innerEffect.getMaxHpX();
            localmaxmp_ += innerEffect.getMaxMpX();
            pad += innerEffect.getPadX();
            mad += innerEffect.getMadX();
            incBuffTime += innerEffect.getBuffTimeRate();
            incMesoRate += innerEffect.getMesoR();
            incDropRate += innerEffect.getDropR();

            if (innerEffect.getDexToStr() > 0) {
                indieStrFX += Math.floor((getDex() * innerEffect.getDexToStr()) / 100.0f);
            }
            if (innerEffect.getStrToDex() > 0) {
                indieDexFX += Math.floor((getStr() * innerEffect.getStrToDex()) / 100.0f);
            }
            if (innerEffect.getIntToLuk() > 0) {
                indieLukFX += Math.floor((getInt() * innerEffect.getIntToLuk()) / 100.0f);
            }
            if (innerEffect.getLukToDex() > 0) {
                indieDexFX += Math.floor((getLuk() * innerEffect.getLukToDex()) / 100.0f);
            }
            if (innerEffect.getLevelToWatk() > 0) {
                pad += Math.floor(chra.getLevel() / innerEffect.getLevelToWatk());
            }
            if (innerEffect.getLevelToMatk() > 0) {
                mad += Math.floor(chra.getLevel() / innerEffect.getLevelToMatk());
            }
            bossDamageR += innerEffect.getBossDamage();
        } //內在能力處理結束

        calculateFame(first_login, chra);

        //處理尖兵多線程系列技能
        int multiThreadHPR = 0, multiThreadMPR = 0;
        if (JobConstants.is傑諾(chra.getJob())) {
            double d = chra.getBuffedIntValue(SecondaryStat.SurplusSupply) / 100.0;
            localstr += d * str;
            localdex += d * dex;
            localluk += d * luk;
            localint += d * int_;
            int[] skillIds = {
                    傑諾.全能增幅I, //30020234 - 多線程Ⅰ - 直接用AP提高的能力值達到一定數值以上時，獲得各個能力值對應的特定獎勵。
                    傑諾.全能增幅II, //36000004 - 多線程Ⅱ - 直接用AP提高的能力值達到一定數值以上時，獲得各個能力值對應的特定獎勵。
                    傑諾.全能增幅III, //36100007 - 多線程Ⅲ - 直接用AP提高的能力值達到一定數值以上時，獲得各個能力值對應的獎勵。\n[需要技能]：#c多線程II1級以上#
                    傑諾.全能增幅IV, //36110007 - 多線程Ⅳ - 直接用AP提高的能力值達到一定數值以上時，獲得各個能力值對應的獎勵。\n[需要技能]：#c多線程III1級以上#
                    傑諾.全能增幅V, //36120010 - 多線程Ⅴ - 直接用AP提高的能力值達到一定數值以上時，獲得各個能力值對應的獎勵。\n[需要技能]：#c多線程IV1級以上#
                    傑諾.全能增幅VI
            };
            MapleStatEffect eff;
            for (int id : skillIds) {
                bx = SkillFactory.getSkill(id);
                if (bx != null) {
                    bof = chra.getSkillLevel(bx);
                    if (bof > 0) {
                        eff = bx.getEffect(bof);
                        if (str >= eff.getX()) {
                            stanceProp += eff.getY();
                        }
                        if (dex >= eff.getX()) {
                            terR += eff.getY();
                            asrR += eff.getZ();
                        }
                        if (luk >= eff.getX()) {
                            dodgeChance += eff.getZ();
                        }
                        if (str >= eff.getX() && dex >= eff.getX() && luk >= eff.getX()) {
                            incDamR += eff.getW();
                            multiThreadHPR += eff.getS();
                            multiThreadMPR += eff.getS();
                        }
                    }
                }
            }
        }

        //計算最大Hp先算傾向系統 在算技能增加上限 最後被動技能增加
        localmaxhp_ += chra.getTrait(MapleTraitType.will).getLevel() / 5 * 100 + addmaxhp;
        localmaxhp_ += Math.floor((incMaxHPR * localmaxhp_) / 100.0f) + indieMhpFX;
        localmaxhp_ += Math.floor(multiThreadHPR * localmaxhp_ / 100.0f);
        localmaxhp = Math.min(ServerConfig.CHANNEL_PLAYER_MAXHP, Math.abs(Math.max(-ServerConfig.CHANNEL_PLAYER_MAXHP, localmaxhp_)));

        //計算最大Mp先算傾向系統 在算被動技能增加 最後計算技能增加上限
        localmaxmp_ += chra.getTrait(MapleTraitType.sense).getLevel() / 5 * 100 + addmaxmp;
        localmaxmp_ += Math.floor((incMaxMPR * localmaxmp_) / 100.0f) + indieMmpFX;
        localmaxmp_ += lv2mmp;
        localmaxmp_ += Math.floor(multiThreadMPR * localmaxmp_ / 100.0f);
        localmaxmp = Math.min(ServerConfig.CHANNEL_PLAYER_MAXMP, Math.abs(Math.max(-ServerConfig.CHANNEL_PLAYER_MAXMP, localmaxmp_)));

        // 特殊職業的Mp設置
        if (JobConstants.is惡魔殺手(chra.getJob())) {
            localmaxmp = GameConstants.getMPByJob(chra.getJob());
            localmaxmp += incMaxDF;
        } else if (JobConstants.is神之子(chra.getJob())) {
            localmaxmp = 100 + incMaxDF;
        } else if (JobConstants.isNotMpJob(chra.getJob())) {
            localmaxmp = 10;
        }

        //神之子beta的hp/mp
        if (JobConstants.is神之子(chra.getJob()) && first_login) {
            betamaxhp = localmaxhp;
            betamaxmp = 100;
        }

        handleHPMPSkills(chra);

        localstr += Math.floor((localstr * incStrR) / 100.0f) + indieStrFX;
        localdex += Math.floor((localdex * incDexR) / 100.0f) + indieDexFX;
        localint += Math.floor((localint * incIntR) / 100.0f) + indieIntFX;
        localluk += Math.floor((localluk * incLukR) / 100.0f) + indieLukFX;
        pad += Math.floor((pad * incPadR) / 100.0f);
        mad += Math.floor((mad * incMadR) / 100.0f);
        localint += Math.floor((localint * incMadR) / 100.0f);
        //計算物理防禦和魔法防禦
        wdef += Math.floor((localstr * 1.5) + ((localdex + localluk) * 0.4));
        wdef += chra.getTrait(MapleTraitType.will).getLevel() / 5;
        int total_percent_def = percent_wdef + percent_mdef;
        wdef = (int) Math.min(99999, wdef + Math.floor((wdef * total_percent_def) / 100.0f));

        addIgnoreMobpdpR(chra.getTrait(MapleTraitType.charisma).getLevel() / 10);
        pvpDamage += chra.getTrait(MapleTraitType.charisma).getLevel() / 10;
        asrR += chra.getTrait(MapleTraitType.will).getLevel() / 5;
        //計算命中值
        recalcPVPRank(chra);
        if (!first_login) {
            int soulskill = 0;
            short soulOption = 0;
            final Equip weapon = (Equip) chra.getInventory(MapleInventoryType.EQUIPPED).getItem(JobConstants.is神之子(chra.getJob()) && chra.isBeta() ? (short) -10 : -11);
            if (weapon != null && weapon.getSoulOptionID() > 0) {
                try {
                    soulskill = ii.getSoulSkill(weapon.getSoulOptionID() - 1);
                    soulOption = weapon.getSoulOption();
                } catch (NullPointerException e) {
                    soulskill = 0;
                    soulOption = 0;
                }
            }
            List<SecondaryStat> list = new ArrayList<>();
            list.add(SecondaryStat.SoulMP);
            list.add(SecondaryStat.FullSoulMP);
            if (chra.getSoulSkillID() > 0 && (soulskill != chra.getSoulSkillID() || soulOption != chra.getSoulOption())) {
                chra.setSoulSkillID(0);
                chra.setSoulOption((short) 0);
                chra.setSoulMP((short) 0);
                chra.setMaxSoulMP(0);
                chra.setShowSoulEffect(false);
                chra.changeSingleSkillLevel(SkillFactory.getSkill(chra.getSoulSkillID()), -1, (byte) 0, -1);
                chra.getClient().announce(BuffPacket.temporaryStatReset(list, chra));
                chra.getMap().broadcastMessage(chra, BuffPacket.cancelForeignBuff(chra, list), false);
            }
            if (chra.getSoulSkillID() <= 0) {
                if (soulskill > 0) {
                    int soul = ii.getSoulSkills().getOrDefault(soulskill, -1);
                    int n7 = 1;
                    final SoulCollectionEntry entry;
                    if (soul >= 0 && chra.getSoulCollection().containsKey(soul) && (entry = ii.getSoulCollection(soul)) != null && (entry.getItems().values().parallelStream().map(n -> (int) Math.pow(2.0, n)).reduce(0, (n2, n3) -> n2 | n3) & chra.getSoulCollection().get(soul)) > 0) {
                        ++n7;
                        chra.updateOneQuestInfo(26467, "skillid", String.valueOf(entry.getSoulSkill()));
                        chra.updateOneQuestInfo(26467, "skillidH", String.valueOf(entry.getSoulSkillH()));
                    }
                    chra.changeSingleSkillLevel(SkillFactory.getSkill(soulskill), 1, (byte) 0, -1L);
                }
                chra.setSoulSkillID(soulskill);
                if (weapon == null || weapon.getSoulSocketID() == 0) {
                    chra.setSoulOption((short) 0);
                    chra.setMaxSoulMP(0);
                    chra.setSoulMP((short) 0);
                } else {
                    chra.setSoulOption(soulOption);
                    chra.setMaxSoulMP(1000);
                    chra.setSoulMP(chra.getSoulMP());
                }
            }
        }
        if (first_login) {
            chra.silentEnforceMaxHpMp();
            relocHeal(chra);
        } else {
            chra.enforceMaxHpMp();
        }
        pad += (int) Math.floor(pad * incPadR / 100.0f);
        mad += (int) Math.floor(mad * incMadR / 100.0f);
        trueMastery = Math.min(100, trueMastery);
        stanceProp = Math.min(100, stanceProp);
        critRate = (short) Math.min(100, critRate);
        percent_damage += incFinalDamage + percent_damage * incFinalDamage / 100.0;
        speed = Math.min(speed, speedMax);
        calculateMaxBaseDamage(chra, equipstats.wt);
        if (hp > localmaxhp) {
            hp = localmaxhp;
        }
        if (mp > localmaxmp) {
            mp = localmaxmp;
        }
    }

    private void handleHPMPSkills(MapleCharacter chra) {
        MapleStatEffect eff;
        if (chra.getJob() == 2217) {
            final double n;
            if ((eff = chra.getSkillEffect(龍魔導士.龍之怒)) != null && localmaxmp > 0 && (n = mp * 100.0 / localmaxmp) >= eff.getX() && n <= eff.getY()) {
                incMadR += eff.getDamage();
            }
        }
    }

    public int getMesoBuff() {
        return 100 + mesoBuff + incMesoRate;
    }

    public int getDropBuff() {
        if (incRewardProp > 1.0) {
            incRewardProp = 1.0;
        }
        return 100 + dropBuff + incDropRate + (int) (incRewardProp * 100);
    }

    private void handlePassiveSkills(MapleCharacter chra) {
        final Item weapon = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
        final Item shield = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
        MapleWeapon wt;
        if (weapon == null) {
            wt = MapleWeapon.沒有武器;
        } else {
            wt = MapleWeapon.getByItemID(weapon.getItemId());
        }
        short job = chra.getJob();
        Skill bx;
        int bof;
        MapleStatEffect eff;
        // 精靈的祝福 和 女皇的祝福 屬性加成
        // 精靈的祝福和女皇的祝福只發動等級更高的效果。
        int elves = SkillConstants.getSkillByJob(12, job), queen = SkillConstants.getSkillByJob(73, job);
        if (chra.getSkillLevel(queen) > chra.getSkillLevel(elves)) {
            if ((eff = chra.getSkillEffect(queen)) != null) {
                this.pad += eff.getX();
                this.mad += eff.getX();
            }
        } else {
            if ((eff = chra.getSkillEffect(elves)) != null) {
                this.pad += eff.getX();
                this.mad += eff.getX();
            }
        }

        // 超級技能加屬性
        if (chra.getLevel() >= 140) {

            eff = chra.getSkillEffect(80000406); //DF - 提高最大惡魔精氣。
            if (eff != null) {
                incMaxDF += eff.getIndieMaxDF();
            }
            bx = SkillFactory.getSkill(80000409); //爆擊發動 - 提高爆擊率。
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0 && bx != null && bx.isHyperStat()) {
                critRate += ((bof <= 5) ? bof : (2 * bof - 5));
            }

            bx = SkillFactory.getSkill(80000414); //攻擊BOSS怪物時的傷害增加 - 提高攻擊BOSS怪物時的傷害。
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0 && bx != null) {
                bossDamageR += ((bof <= 5) ? (3 * bof) : (4 * bof - 5));
            }

            bx = SkillFactory.getSkill(80000416); //狀態異常抗性 - 對怪物的所有狀態異常攻擊擁有抗性。
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0 && bx != null) {
                this.asrR += ((bof <= 5) ? bof : (2 * bof - 5));
            }
        }


        // 卡片收藏者的標誌
        if (chra.getSkillEffect(80000269) != null) {
            for (int i = 0; i < 5; ++i) {
                final String questInfo = chra.getQuestInfo(16345, i + "d");
                if (questInfo != null && !"-1".equals(questInfo)) {
                    final String questInfo2 = chra.getQuestInfo(16345, questInfo);
                    if (questInfo2 != null) {
                        final int intValue = Integer.valueOf(questInfo2);
                        int n = -1;
                        switch (questInfo) {
                            case "0": {
                                n = 0;
                                break;
                            }
                            case "1": {
                                n = 1;
                                break;
                            }
                            case "2": {
                                n = 2;
                                break;
                            }
                            case "3": {
                                n = 3;
                                break;
                            }
                            case "4": {
                                n = 4;
                                break;
                            }
                            case "5": {
                                n = 5;
                                break;
                            }
                            case "9": {
                                n = 6;
                                break;
                            }
                            default:
                                break;
                        }
                        switch (n) {
                            case 0: {
                                this.bossDamageR += intValue;
                                break;
                            }
                            case 1: {
                                this.incBuffTime += intValue;
                                break;
                            }
                            case 2: {
                                this.critRate += intValue;
                                break;
                            }
                            case 3: {
                                this.criticalDamage += intValue;
                                break;
                            }
                            case 4: {
                                this.pad += intValue;
                                this.mad += intValue;
                                break;
                            }
                            case 5: {
                                this.incDamR += intValue;
                                break;
                            }
                            case 6: {
                                this.localstr += intValue;
                                this.localdex += intValue;
                                this.localluk += intValue;
                                this.localint += intValue;
                                break;
                            }
                        }
                    }
                }
            }
        }
//        for (int j = 80000007; j <= 80000297; ++j) {
//            final MapleStatEffect skillEffect17;
//            if ((skillEffect17 = chra.getSkillEffect(j)) != null) {
//                this.pad += skillEffect17.getPadX();
//                this.mad += skillEffect17.getMadX();
//            }
//        }
//        // 應用psdSkills
//        for (int skillId : chra.getSkills().keySet()) {
//            Skill skill = SkillFactory.getSkill(skillId);
//            if (skill == null || skill.getPsdSkills() == null) {
//                continue;
//            }
//            MapleStatEffect effect = chra.getSkillEffect(skill.getId());
//            if (effect == null) {
//                continue;
//            }
//            if (skillId / 10000 == 40000) {
//                for (int psd : skill.getPsdSkills()) {
//                    localstr += effect.getStrX();
//                    localdex += effect.getDexX();
//                    localint += effect.getIntX();
//                    localluk += effect.getLukX();
//                    addSkillDamageIncrease_5th(psd, effect.getDAMRate_5th());
//                    addSkillTargetPlus(psd, effect.getTargetPlus_5th());
//                }
//            } else {
//                for (int psd : skill.getPsdSkills()) {
//                    addSkillDamageIncrease(psd, effect.getDamPlus() > 0 ? effect.getDamPlus() : effect.getDamR());
//                    addSkillTargetPlus(psd, effect.getTargetPlus());
//                }
//            }
//        }
//        // 新手技能和特殊技能
//        if ((eff = chra.getSkillEffect(海盜.海盜的祝福)) != null) {
//            localstr += eff.getStrX();
//            localdex += eff.getDexX();
//            localint += eff.getIntX();
//            localluk += eff.getLukX();
//            addmaxhp += eff.getMaxHpX();
//            addmaxmp += eff.getMaxMpX();
//            addDamAbsorbShieldR(eff.getDamAbsorbShieldR());
//        }
//        if ((eff = chra.getSkillEffect(通用V核心.海盜通用.滿載骰子)) != null) {
//            pad += eff.getPadX();
//        }
//        if ((eff = chra.getSkillEffect(通用V核心.劍士通用.鋼鐵之軀)) != null) {
//            localstr += eff.getStrX();
//            addmaxhp += eff.getMaxHpX();
//        }
//        if ((eff = chra.getSkillEffect(惡魔殺手.後續待發)) != null) {
//            bossDamageR += eff.getBossDamage();
//        }
//        if ((eff = chra.getSkillEffect(幻影俠盜.致命本能_傳授)) != null) {
//            critRate += eff.getCritical();
//        }
//        if ((eff = chra.getSkillEffect(80000003)) != null) {
//            localstr += eff.getStrX();
//            localdex += eff.getDexX();
//            localint += eff.getIntX();
//            localluk += eff.getLukX();
//            pad += eff.getPadX();
//        }
//        if ((eff = chra.getSkillEffect(陰陽師.紫扇傳授_傳授)) != null) {
//            incDamR += eff.getDamR();
//            kannaLinkDamR += eff.getDamR();
//        }
//        if ((eff = chra.getSkillEffect(夜光.滲透_傳授)) != null) {
//            addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//        }
//        if ((eff = chra.getSkillEffect(凱撒.鋼鐵意志_傳授)) != null) {
//            incMaxHPR += eff.getMhpR();
//        }
//        if ((eff = chra.getSkillEffect(傑諾.合成邏輯_傳授)) != null) {
//            incStrR += eff.getStrRate();
//            incDexR += eff.getDexR();
//            incIntR += eff.getIntRate();
//            incLukR += eff.getLukRate();
//        }
//        if ((eff = chra.getSkillEffect(惡魔復仇者.狂暴鬥氣_傳授)) != null) {
//            incDamR += eff.getDamR();
//        }
//        if ((eff = chra.getSkillEffect(貴族.西格諾斯祝福)) != null) {
//            asrR += eff.getASRRate();
//            terR += eff.getTERRate();
//        }
//        if ((eff = chra.getSkillEffect(神之子.時之祝福_傳授)) != null) {
//            addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//            addDamAbsorbShieldR(eff.getDamAbsorbShieldR());
//        }
//        if ((eff = chra.getSkillEffect(凱內西斯.判斷_傳授)) != null) {
//            criticalDamage += eff.getCriticalDamage();
//        }
//        if ((eff = chra.getSkillEffect(隱月.精靈親和)) != null) {
//            summonTimeR += eff.getSummonTimeInc();
//        }
//        if ((eff = chra.getSkillEffect(菈菈.精靈親和)) != null) {
//            summonTimeR += eff.getSummonTimeInc();
//        }
//        if ((eff = chra.getSkillEffect(菈菈.感應)) != null) {
//            incMaxHPR += eff.getMhpR();
//        }
//        if ((eff = chra.getSkillEffect(菈菈.一塵不染)) != null) {
//            incMaxMPR += eff.getMmpR();
//        }
//        if ((eff = chra.getSkillEffect(菈菈.風水地理深化)) != null) {
//            MapleStatEffect effect1;
//            int time = 10000;
//            if ((effect1 = chra.getSkillEffect(菈菈.山之種子)) != null) {
//                time = effect1.getDuration();
//            }
//            add_skill_duration.put(菈菈.山之種子, eff.getDuration() - time);
//        }
//        if ((eff = chra.getSkillEffect(菈菈.喚醒_減少冷卻時間)) != null) {
//            addSkillCooltimeReduce(菈菈.喚醒, eff.getCooltimeReduceR());
//        }
        if (JobConstants.is皇家騎士團(chra.getJob())) {
            if ((eff = chra.getSkillEffect(貴族.女皇的傲氣)) != null) {
                incMaxHPR += eff.getX();
                incMaxMPR += eff.getX();
            }
//            if ((eff = chra.getSkillEffect(貴族.元素精通)) != null) {
//                incPadR += eff.getPadR();
//                incMadR += eff.getMadR();
//            }
            if ((eff = chra.getSkillEffect(貴族.Royal_Link_貴族_自然旋律)) != null) {
                localstr += chra.getLevel() / 2;
            }
            if ((eff = chra.getSkillEffect(破風使者.Royal_Link_破風使者_自然旋律)) != null) {
                localdex += chra.getLevel() / 2;
            }
            if ((eff = chra.getSkillEffect(烈焰巫師.Royal_Link_烈焰巫師_自然旋律)) != null) {
                localint += chra.getLevel() / 2;
            }
            if ((eff = chra.getSkillEffect(暗夜行者.Royal_Link_暗夜行者_自然旋律)) != null) {
                localluk += chra.getLevel() / 2;
            }
        }
//
//        if (JobConstants.is冒險家劍士(job)) {
//            if ((eff = chra.getSkillEffect(劍士.自身強化)) != null) {
//                addDamAbsorbShieldR(eff.getDamAbsorbShieldR());
//                incMaxHPR += eff.getMhpR();
//                wdef += eff.getPddX();
//            }
//            if ((eff = chra.getSkillEffect(劍士.戰鬥技能)) != null) {
//                addmaxhp += eff.getLv2mhp() * chra.getLevel();
//                jump += eff.getPsdJump();
//                speed += eff.getPsdSpeed();
//                speedMax += eff.getSpeedMax();
//                stanceProp += eff.getStanceProp();
//            }
//        } else if (JobConstants.is冒險家法師(job)) {
//            if ((eff = chra.getSkillEffect(法師.魔力之盾)) != null) {
//                wdef += eff.getPddX();
//            }
//            if ((eff = chra.getSkillEffect(法師.魔力增幅)) != null) {
//                incMaxMPR += eff.getMmpR();
//                lv2mmp += eff.getLv2mmp() * chra.getLevel();
//                if (wt == MapleWeapon.短杖) {
//                    critRate += 3;
//                }
//            }
//            if ((eff = chra.getSkillEffect(法師.波動記憶)) != null) {
//                localint += eff.getIntX();
//            }
//        } else if (JobConstants.is冒險家弓箭手(job)) {
//            if ((eff = chra.getSkillEffect(弓箭手.霸王箭)) != null && (wt == MapleWeapon.弩 || wt == MapleWeapon.弓)) {
//                critRate += eff.getProp();
//            }
//            if ((eff = chra.getSkillEffect(弓箭手.精通射手)) != null) {
//                speed += eff.getSpeed();
//                speedMax = eff.getU();
//            }
//        } else if (JobConstants.is冒險家盜賊(job)) {
//            if ((eff = chra.getSkillEffect(盜賊.速度激發)) != null) {
//                speedMax += eff.getSpeedMax();
//            }
//            if ((eff = chra.getSkillEffect(盜賊.幻化術)) != null) {
//                localluk += eff.getLukX();
//            }
//            bx = SkillFactory.getSkill(影武者.迴避); //4000012 - 側移 - 永久提高迴避率。
//            bof = chra.getTotalSkillLevel(bx);
//            if (bof > 0 && bx != null) {
//                dodgeChance += bx.getEffect(bof).getER();
//            }
//        } else if (JobConstants.is冒險家海盜(job)) {
//            bx = SkillFactory.getSkill(海盜.能力極限); //5000000 - 快動作 - 永久增加命中值、移動速度上限、跳躍力。
//            bof = chra.getTotalSkillLevel(bx);
//            if (bof > 0 && bx != null) {
//                eff = bx.getEffect(bof);
//                jump += eff.getPsdJump(); //移動速度上限
//                speedMax += eff.getSpeedMax(); //跳躍力
//            }
//            final MapleStatEffect skillEffect37;
//            if ((skillEffect37 = chra.getSkillEffect(海盜.旋風斬)) != null) {
//                this.critRate += skillEffect37.getCritical();
//                this.criticalDamage += skillEffect37.getCriticalDamage();
//            }
//        } else if (JobConstants.is惡魔(job)) {
//            final MapleStatEffect skillEffect36;
//            if ((skillEffect36 = chra.getSkillEffect(惡魔.魔族之血)) != null) {
//                this.stanceProp += skillEffect36.getX();
//                chra.getTrait(MapleTraitType.will).setLevel(20, chra);
//                chra.getTrait(MapleTraitType.charisma).setLevel(20, chra);
//            }
//        }

        // 取出角色所有的被動技能，自動計算增加的能力值
        for (int nSkillId : chra.getSkills().keySet()) {
            bx = SkillFactory.getSkill(nSkillId);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0 && bx != null) {
                eff = bx.getEffect(bof);
                // 判斷被動技能或輔助技能
                SwordieX.client.character.skills.Skill sk = SkillData.getSkills().get(nSkillId);
                if (sk.isPsd() && sk.getPsdSkills().isEmpty()) {
                    for (MapleStatInfo msi : sk.getCommon().keySet()) {
                        switch (msi) {
                            case str:
                            case strX:
                            case strFX: // 增加STR
                                localstr += eff.getInfo().get(msi);
                                break;
                            case strR: // STR增加百分比
                                incStrR += eff.getStrRate();
                                break;
                            case dex:
                            case dexX:
                            case dexFX: // 增加敏捷
                                localdex += eff.getInfo().get(msi);
                                break;
                            case dexR:  // 敏捷增加百分比
                                incDexR += eff.getDexR();
                                break;
                            case int_:
                            case intX:
                            case intFX: // 智力增加
                                localint += eff.getInfo().get(msi);
                                break;
                            case intR: // 智力增加百分比
                                incIntR += eff.getIntRate();
                                break;
                            case luk:
                            case lukX:
                            case lukFX: // 幸運增加
                                localluk += eff.getInfo().get(msi);
                                break;
                            case lukR: // 幸運增加百分比
                                incLukR += eff.getLukRate();
                                break;
                            case damR: // 傷害增加百分比
                                incDamR += eff.getDamR();
                                break;
                            case mdR: // 魔法最終傷害百分比
                            case pdR: // 物理最終傷害百分比
                                if (nSkillId == 夜光.黑暗強化 || nSkillId == 夜光.光魔法強化) {
                                    break;
                                }
                                addIncFinalDamage(eff.getInfo().get(msi));
                                break;
                            case pad:
                            case padX: // 攻擊力增加
                                pad += eff.getInfo().get(msi);
                                break;
                            case padR: // 攻擊力增加百分比
                                incPadR += eff.getPadR();
                                break;
                            case pdd, pddX, mdd, mddX: // 物理防禦力增加
                                wdef += eff.getInfo().get(msi);
                                break;
                            case pddR:
                                percent_wdef += eff.getInfo().get(msi);
                                break;
                            case mad:
                            case madX: // 魔法攻擊力增加
                                mad += eff.getInfo().get(msi);
                                break;
                            case madR: // 魔法攻擊力增加百分比
                                incMadR += eff.getMadR();
                                break;
                            case mddR: // 魔法防禦百分比
                                percent_mdef += eff.getInfo().get(msi);
                                break;
                            case asrR: // 狀態異常耐性
                                asrR += eff.getInfo().get(msi);
                                break;
                            case terR: // 所有屬性耐性增加
                                terR += eff.getInfo().get(msi);
                                break;
                            case stanceProp: // 檔格機率
                                stanceProp += eff.getInfo().get(msi);
                                break;
                            case mhpR: // 增加最大Hp
                                incMaxHPR += eff.getInfo().get(msi);
                                break;
                            case mmpR: // 增加最大Mp
                                incMaxMPR += eff.getInfo().get(msi);
                                break;
                            case MDF: // 增加最大DF
                                incMaxDF += eff.getInfo().get(msi);
                            case hp: // hp回復量
                                recoverHP += eff.getInfo().get(msi);
                                break;
                            case mp: // mp回復量
                                recoverMP += eff.getInfo().get(msi);
                                break;
                            case cr: // 爆擊率
                                critRate += eff.getInfo().get(msi);
                                break;
                            case criticaldamage: // 爆擊傷害
                                criticalDamage += eff.getInfo().get(msi);
                                break;
                            case er: // 迴避率
                                dodgeChance += eff.getInfo().get(msi);
                                break;
                            case bdR: // Boss傷害率
                                bossDamageR += eff.getInfo().get(msi);
                                break;
                            case mobCountDamR: // 怪物數量傷害率
                                incDamR += eff.getInfo().get(msi);
                                break;
                            case speed:
                            case psdSpeed: // 移動速度
                                speed += eff.getInfo().get(msi);
                                break;
                            case speedMax: // 最大移動速度
                                speedMax += eff.getInfo().get(msi);
                                break;
                            case jump:
                            case psdJump: // 跳躍力
                                jump += eff.getInfo().get(msi);
                                break;
                            case ignoreMobpdpR: // 無視怪物防禦率
                                ignoreMobpdpR += eff.getIgnoreMobpdpR();
                                break;
                            case mastery: // 熟練度
                                trueMastery += eff.getInfo().get(msi);
                                break;
                            case ignoreMobDamR: // 受到傷害減少率
                                addDamAbsorbShieldR(eff.getIgnoreMobDamR());
                                break;
                            case actionSpeed: // 攻擊速度
                                attackSpeed += eff.getInfo().get(msi);
                                break;
                            case acc:
                            case accX: // 命中
                                break;
                            case ar:
                            case accR: // 命中率
                                percent_acc += eff.getPercentAcc(); //提升x%的命中
                                break;
                            case arcX: // 神秘力量
                                arc += eff.getInfo().get(msi);
                                break;
                            case expRPerM: // 經驗值增加
                                expRPerM += eff.getInfo().get(msi);
                                break;
                            case bufftimeR: // buff持續時間
                                incBuffTime += eff.getBuffTimeRate();
                                break;
                            default:
                                break;
                        }
                    }
                    for (Entry<String, String> info : sk.getInfo().entrySet()) {
                        switch (info.getKey()) {
                            case "finalAttack":
                                if (finalAttackSkill < nSkillId) { // 如果id大於原本儲存在變數內的id，應該就是進階最終攻擊
                                    finalAttackSkill = nSkillId;
                                }
                                break;
                            case "massSpell":

                                break;
                            case "magicSteal":
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }


        // 處理額外部分被動技能讀取x、prop
        switch (job) {
            case 110:
            case 111:
            case 112: {
                if ((eff = chra.getSkillEffect(英雄.武器精通)) != null) {
                    if (wt == MapleWeapon.單手斧 || wt == MapleWeapon.雙手斧) {
                        incDamR += 5.0;
                    }
                }
//                if (chra.getSkillLevel(英雄.終極攻擊) > 0) {
//                    finalAttackSkill = 英雄.終極攻擊;
//                }
//                if ((eff = chra.getSkillEffect(英雄.進階終極攻擊)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                }
//                if ((eff = chra.getSkillEffect(英雄.進階鬥氣_機率提升)) != null) {
//                    addSkillProp(英雄.進階鬥氣, eff.getProp());
//                }
//                if ((eff = chra.getSkillEffect(英雄.進階鬥氣_BOSS傷害)) != null) {
//                    addSkillBossDamage(英雄.鬥氣集中, eff.getW());
//                }
//                if ((eff = chra.getSkillEffect(英雄.進階終極攻擊_攻擊提升)) != null) { //1120047 - 進階終極攻擊-額外傷害 - 增加進階終極攻擊提高的物理攻擊力。
//                    pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(英雄.狂暴攻擊_攻擊加成)) != null) {
//                    addSkillAttackCount(英雄.狂暴攻擊, eff.getAttackCount());
//                    addSkillAttackCount(英雄.狂暴攻擊_爆擊, eff.getAttackCount());
//                }
//                if ((eff = chra.getSkillEffect(英雄.進階終極攻擊_機率提升)) != null) {
//                    addSkillProp(英雄.進階終極攻擊, eff.getProp());
//                }
                break;
            }
            case 120:
            case 121:
            case 122: {
//                if ((eff = chra.getSkillEffect(聖騎士.終極之劍)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                }
//                if ((eff = chra.getSkillEffect(聖騎士.盾牌技能)) != null && shield != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                    percent_wdef += eff.getX();
//                    pad += eff.getY();
//                }
//                if ((eff = chra.getSkillEffect(聖騎士.聖騎士大師)) != null) {
//                    wdef += eff.getPddX();
//                    criticalDamage += eff.getCriticalDamage();
//                    critRate += eff.getCritical();
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                    percent_damage += eff.getPdR() + percent_damage * eff.getPdR() / 100.0;
//                    switch (wt) {
//                        case 單手劍:
//                        case 雙手劍: {
//                            criticalDamage += 5;
//                            break;
//                        }
//                        case 單手棍:
//                        case 雙手棍: {
//                            criticalDamage += 3;
//                            addIgnoreMobpdpR(10);
//                            break;
//                        }
//                    }
//                }
//                /*
//                 * 聖騎士超級技能
//                 */
//                bx = SkillFactory.getSkill(聖騎士.騎士衝擊波_攻擊加成); //1220048 - 連環環破-額外攻擊 - 增加連環環破的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(聖騎士.騎士衝擊波, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(聖騎士.鬼神之擊_攻擊加成); //1220050 - 聖域-額外攻擊 - 增加聖域的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(聖騎士.鬼神之擊, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(聖騎士.鬼神之擊_冷卻減免); //1220051 - 聖域-縮短冷卻時間 - 減少聖域的冷卻時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillCooltimeReduce(聖騎士.鬼神之擊, bx.getEffect(bof).getCooltimeReduceR());
//                }
                break;
            }
            case 130:
            case 131:
            case 132: {
//                if ((eff = chra.getSkillEffect(黑騎士.武器精通)) != null) {
//                    wdef += eff.getPddX();
//                    criticalDamage += eff.getCriticalDamage();
//                    if (wt == MapleWeapon.槍) {
//                        incDamR += 5.0;
//                    }
//                }
//                if ((eff = chra.getSkillEffect(黑騎士.終極之槍)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                }
//                bx = SkillFactory.getSkill(黑騎士.體能訓練); //1300009 - 物理訓練 - 通過身體鍛煉，永久性地提高力量和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                bx = SkillFactory.getSkill(黑騎士.暗黑之力); //1310009 - 黑暗至尊 - 增加暴擊率、暴擊最小傷害，有一定幾率將傷害的一部分轉換成體力。但是，#c不能超過最大HP的一半。#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    critRate += eff.getCritical();
//                    criticalDamage += eff.getCriticalMin();
//                    hpRecover_onAttack.put(eff.getSourceId(), new Pair<>(eff.getX(), eff.getProp()));
//                }
//                bx = SkillFactory.getSkill(黑騎士.恢復術); //1310010 - 抵抗力 - 強化自己對異常狀態的抗性。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    asrR += bx.getEffect(bof).getASRRate();
//                    terR += bx.getEffect(bof).getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(黑騎士.闇靈共鳴)) != null) {
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(黑騎士.十字深鎖鏈)) != null) {
//                    percent_damage += eff.getPdR() + percent_damage * eff.getPdR() / 100.0;
//                }
//                if ((eff = chra.getSkillEffect(黑騎士.反抗姿態)) != null) {
//                    stanceProp += eff.getStanceProp();
//                }
//                if ((eff = chra.getSkillEffect(黑騎士.進階武器精通)) != null) {
//                    pad += eff.getPadX();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(黑騎士.轉生)) != null) {
//                    if (localmaxhp > 0 && hp * 100 / localmaxhp > eff.getX()) {
//                        incFinalDamage += eff.getDamage();
//                        speed += eff.getPsdSpeed();
//                        critRate += eff.getCritical();
//                        criticalDamage += eff.getCriticalDamage();
//                        if (chra.getSkillLevel(黑騎士.轉生_爆擊提升) > 0) {
//                            critRate += 20;
//                        }
//                    }
//                }
//                /*
//                 * 黑騎士超級技能
//                 */
//                if ((eff = chra.getSkillEffect(黑騎士.轉生_傷害)) != null && chra.getBuffedValue(MapleBuffStat.Reincarnation) != null) {
//                    percent_damage += eff.getDamage();
//                }
//                if ((eff = chra.getSkillEffect(黑騎士.岡格尼爾之擊_BOSS殺手)) != null) {
//                    addSkillBossDamage(黑騎士.岡格尼爾之擊, eff.getBossDamage());
//                }
//                if ((eff = chra.getSkillEffect(黑騎士.岡格尼爾之擊_無視防禦)) != null) {
//                    addIgnoreMobpdpRate(黑騎士.岡格尼爾之擊, eff.getIgnoreMobpdpR());
//                }
                break;
            }
            case 210:
            case 211:
            case 212: {
//                if ((eff = chra.getSkillEffect(火毒.咒語精通)) != null) {
//                    mad += eff.getX();
//                }
//                bx = SkillFactory.getSkill(火毒.智慧昇華); //2100007 - 智慧激發 - 通過精神修養，永久性增加智力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    localint += bx.getEffect(bof).getIntX();
//                }
//                if ((eff = chra.getSkillEffect(火毒.元素適應_火毒)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(火毒.自然力重置)) != null) {
//                    percent_damage += eff.getMdR();
//                }
//                if ((eff = chra.getSkillEffect(火毒.魔力激發)) != null) {
//                    incMpCon += eff.getCostMpRate();
//                    incDamR += eff.getDamR();
//                }
//                if ((eff = chra.getSkillEffect(火毒.魔法暴擊)) != null) {
//                    critRate += eff.getCritical();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                bx = SkillFactory.getSkill(火毒.終極魔法_火毒); //2110000 - 極限魔力（火，毒） - 增加自己的所有持續傷害技能的持續時間，攻擊受到持續傷害或處於昏迷、凍結、暗黑、麻痺狀態的敵人，可以增加傷害。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incDotTime += eff.getX();
//                }
//                if ((eff = chra.getSkillEffect(火毒.火流星_墜落)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                }
//                if ((eff = chra.getSkillEffect(火毒.神秘狙擊)) != null) {
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                bx = SkillFactory.getSkill(火毒.大師魔法); //2120012 - 魔力精通 - 永久性增加魔力，增加對自己使用的所有增益的持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    mad += eff.getMadX();
//                    incBuffTime += eff.getBuffTimeRate();
//                }
//                /*
//                 * 火毒超級技能
//                 */
//                bx = SkillFactory.getSkill(火毒.致命毒霧_持續效果);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addDotTime(火毒.致命毒霧, bx.getEffect(bof).getDotTime());
//                }
//                bx = SkillFactory.getSkill(火毒.火焰之襲_額外攻擊); //2120048 - 美杜莎之眼-額外攻擊  - 增加美杜莎之眼的攻擊次數
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(火毒.火焰之襲, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(火毒.地獄爆發_無視防禦); //2120050 - 迷霧爆發-無視防禦 - 提高迷霧爆發的無視怪物防禦力效果。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpRate(火毒.地獄爆發, bx.getEffect(bof).getIgnoreMobpdpR());
//                }
//                bx = SkillFactory.getSkill(火毒.地獄爆發_冷卻減免); //2120051 - 迷霧爆發-縮短冷卻時間 - 減少迷霧爆發的冷卻時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillCooltimeReduce(火毒.地獄爆發, bx.getEffect(bof).getCooltimeReduceR());
//                }
                break;
            }
            case 220:
            case 221:
            case 222: {
//                if ((eff = chra.getSkillEffect(冰雷.咒語精通)) != null) {
//                    mad += eff.getX();
//                }
//                bx = SkillFactory.getSkill(冰雷.智慧昇華); //2200007 - 智慧激發 - 通過精神修養，永久性增加智力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    localint += bx.getEffect(bof).getIntX();
//                }
//                if ((eff = chra.getSkillEffect(冰雷.元素適應_雷冰)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(冰雷.自然力重置)) != null) {
//                    percent_damage += eff.getMdR();
//                }
//                if ((eff = chra.getSkillEffect(冰雷.魔法爆擊)) != null) {
//                    critRate += eff.getCritical();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                bx = SkillFactory.getSkill(冰雷.魔力激發); //2210001 - 魔力激化 - 消耗更多的MP，提高所有攻擊魔法的傷害。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMpCon += eff.getCostMpRate();
//                    incDamR += eff.getDamR();
//                }
//                bx = SkillFactory.getSkill(冰雷.大師魔法); //2220013 - 魔力精通 - 永久性增加魔力，增加對自己使用的所有增益的持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    mad += eff.getMadX();
//                    incBuffTime += eff.getBuffTimeRate();
//                }
//                bx = SkillFactory.getSkill(冰雷.神秘狙擊); //2220010 - 神秘瞄準術 - 攻擊時可以無視怪物的部分防禦力，持續攻擊時提升所有攻擊的傷害。有一定概率發動傷害提升效果，最多可以累積5次。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(冰雷.暴風雪_冰槍)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                }
//                /*
//                 * 冰雷超級技能
//                 */
//                bx = SkillFactory.getSkill(冰雷.閃電連擊_攻擊加成); //2220048 - 鏈環閃電-額外攻擊 - 增加鏈環閃電的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(冰雷.閃電連擊, bx.getEffect(bof).getAttackCount());
//                }
                break;
            }
            case 230:
            case 231:
            case 232: {
//                if ((eff = chra.getSkillEffect(主教.咒語精通)) != null) {
//                    mad += eff.getX();
//                }
//                bx = SkillFactory.getSkill(主教.智慧昇華); //2300007 - 智慧激發 - 通過精神修養，永久性增加智力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    localint += bx.getEffect(bof).getIntX();
//                }
//                if ((eff = chra.getSkillEffect(2300003)) != null) {
//                    addDamAbsorbShieldR(eff.getDamAbsorbShieldR());
//                }
//                if ((eff = chra.getSkillEffect(主教.聖靈守護)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(主教.魔法暴擊)) != null) {
//                    critRate += eff.getCritical();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(主教.神聖集中術)) != null) {
//                    critRate += eff.getCritical();
//                }
//                bx = SkillFactory.getSkill(主教.大師魔法); //2320012 - 魔力精通 - 永久性增加魔力，增加對自己使用的所有增益的持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    mad += eff.getMadX();
//                    incBuffTime += eff.getBuffTimeRate();
//                }
//                bx = SkillFactory.getSkill(主教.神秘狙擊); //2320011 - 神秘瞄準術 - 攻擊時可以無視怪物的部分防禦力，持續攻擊時提升所有攻擊的傷害。有一定概率發動傷害提升效果，最多可以累積5次。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                /*
//                 * 主教超級技能
//                 */
//                bx = SkillFactory.getSkill(主教.聖十字魔法盾_持續防禦); //2320044 - 神聖魔法盾-堅持 - 增加神聖魔法盾的持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(主教.聖十字魔法盾, bx.getEffect(bof).getDuration());
//                }
//                if (chra.getSkillEffect(主教.神聖祈禱_抗性提升) != null) {
//                    asrR += 10;
//                    terR += 10;
//                }
//                if ((eff = chra.getSkillEffect(主教.復仇天使)) != null && chra.getBuffedIntValue(MapleBuffStat.VengeanceOfAngel) <= 0) {
//                    incDamR += eff.getZ();
//                }
                break;
            }
//            case 310:
//            case 311:
//            case 312: {
//                if ((eff = chra.getSkillEffect(箭神.終極之弓)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                }
//                bx = SkillFactory.getSkill(箭神.體能訓練); //3100006 - 物理訓練 - 通過身體鍛煉，永久性地提高力量和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                bx = SkillFactory.getSkill(箭神.召喚鳳凰); //3111005 - 火鳳凰 - 召喚帶有火屬性的鳳凰。鳳凰最多同時攻擊4個敵人，有一定概率造成昏迷。另外，永久增加物理/魔法防禦力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    percent_wdef += eff.getPddR(); //增加物防
//                }
//                bx = SkillFactory.getSkill(箭神.集中專注); //3110012 - 精神集中 - 持續攻擊時，集中力逐漸提高，異常狀態抗性持續增加。擁有相應增益的情況下抵抗異常狀態後，應用冷卻時間。此外，永久增加異常狀態抗性及所有屬性抗性。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                bx = SkillFactory.getSkill(箭神.射擊術); //3110014 - 射術精修 - 攻擊時有一定概率在一定程度上無視怪物的防禦力，永久性地增加命中率及總傷害。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                    incPadR += eff.getPadR();
//                }
//                if ((eff = chra.getSkillEffect(箭神.弓術精通)) != null && wt == MapleWeapon.弓) {
//                    pad += eff.getX();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(箭神.躲避)) != null) {
//                    incMaxHPR += eff.getMhpR();
//                }
//                bx = SkillFactory.getSkill(箭神.進階終極攻擊); //3120008 - 進階終極攻擊 - 永久性地增加攻擊力和命中率，大幅提高終極武器的發動概率和傷害。\n需要技能：#c終極弓20級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX();
//                    finalAttackSkill = eff.getSourceId();
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(箭神.會心之眼_時間持續); //3120043 - 火眼晶晶-堅持 - 火眼晶晶持續時間增加。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(箭神.會心之眼, bx.getEffect(bof).getDuration());
//                }
//                bx = SkillFactory.getSkill(箭神.驟雨狂矢_攻擊加成); //3120048 - 驟雨箭矢-額外攻擊 - 增加驟雨箭矢的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(箭神.驟雨狂矢, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(箭神.暴風神射_BOSS傷害); //3120050 - 暴風箭雨-BOSS殺手 - 增加暴風箭雨對BOSS的攻擊力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillBossDamage(箭神.暴風神射, bx.getEffect(bof).getBossDamage());
//                }
//                bx = SkillFactory.getSkill(箭神.暴風神射_多重射擊); //3120051 - 暴風箭雨-靈魂攻擊 - 暴風箭雨的傷害變為原來的#x%，但攻擊次數增多。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(箭神.暴風神射, bx.getEffect(bof).getAttackCount());
//                }
//                break;
//            }
//            case 320:
//            case 321:
//            case 322: {
//                if ((eff = chra.getSkillEffect(神射手.終極之弩)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                }
//                if ((eff = chra.getSkillEffect(神射手.召喚銀隼)) != null) {
//                    percent_wdef += eff.getPddR();
//                }
//                bx = SkillFactory.getSkill(神射手.體能訓練); //3200006 - 物理訓練 - 通過身體鍛煉，永久性地提高力量和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                bx = SkillFactory.getSkill(神射手.止痛藥); //3211011 - 治癒長杖 - 在冒險中，吃下所準備的藥草後，立即擺脫異常狀態。此外，所有屬性抗性及異常狀態抵抗永久增加。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(神射手.躲避)) != null) {
//                    incMaxHPR += eff.getMhpR();
//                }
//                bx = SkillFactory.getSkill(神射手.射擊術); //3210015 - 射術精修 - 攻擊時有一定概率在一定程度上無視怪物的防禦力，永久性地增加命中率及總傷害。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                    incDamR += eff.getDamR();
//                }
//                if ((eff = chra.getSkillEffect(神射手.弩術精通)) != null) {
//                    if (wt == MapleWeapon.弩) {
//                        pad += eff.getX();
//                    }
//                    criticalDamage += eff.getCriticalDamage();
//                }
//
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(神射手.會心之眼_時間延續); //3220043 - 火眼晶晶-堅持 - 火眼晶晶持續時間增加。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(神射手.會心之眼, bx.getEffect(bof).getDuration());
//                }
//                bx = SkillFactory.getSkill(神射手.光速神弩_攻擊加成); //3220048 - 穿透箭-額外攻擊 - 增加穿透箭的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null && (eff = bx.getEffect(bof)) != null) {
//                    addSkillAttackCount(神射手.光速神弩, eff.getAttackCount());
//                    addSkillAttackCount(神射手.光速神弩II, eff.getAttackCount());
//                    addSkillAttackCount(神射手.光速神弩II_1, eff.getAttackCount());
//                    addSkillAttackCount(神射手.覺醒之箭, eff.getAttackCount());
//                    addSkillAttackCount(神射手.覺醒神弩II, eff.getAttackCount());
//                    addSkillAttackCount(神射手.覺醒神弩II_1, eff.getAttackCount());
//                }
//                if ((eff = chra.getSkillEffect(神射手.必殺狙擊_BOSS殺手)) != null) {
//                    addSkillBossDamage(神射手.必殺狙擊, eff.getBossDamage());
//                }
//                break;
//            }
//            case 330:
//            case 331:
//            case 332: {
//                if ((eff = chra.getSkillEffect(開拓者.古代指揮)) != null) {
//                    incMaxHPR += eff.getMhpR();
//                }
//                bx = SkillFactory.getSkill(開拓者.會心之眼_時間持續);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(開拓者.會心之眼, bx.getEffect(bof).getDuration());
//                }
//            }
//            break;
//            case 410:
//            case 411:
//            case 412: {
//                if ((eff = chra.getSkillEffect(夜使者.強力投擲)) != null) {
//                    if (wt == MapleWeapon.拳套) {
//                        critRate += eff.getProp();
//                    }
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                bx = SkillFactory.getSkill(夜使者.體能訓練); //4100007 - 物理訓練 - 通過鍛煉身體，永久提高幸運和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localluk += eff.getLukX();
//                    localdex += eff.getDexX();
//                }
//                bx = SkillFactory.getSkill(夜使者.永恆黑暗); //4110008 - 永恆黑暗 - 和黑暗融為一體，永久增加最大HP、增加狀態異常抗性、增加所有屬性抗性。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR();
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                bx = SkillFactory.getSkill(夜使者.鏢術精通); //4110012 - 嫻熟飛鏢術 - 提高雙飛斬、爆裂飛鏢、風之護符、三連環光擊破的#c總傷害#。\n此外，攻擊時有一定幾率不消耗飛鏢，使當前持有的#c飛鏢增加1個#。\n(但飛鏢數量無法超出最大個數)\n此外嫻熟飛鏢術效果發動時，#c下一次攻擊百分之百造成爆擊#。(在暗器傷人狀態下也可以造成爆擊，但標槍數量不增加。)
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    percent_damage += eff.getPdR();
//                }
//                bx = SkillFactory.getSkill(夜使者.藥劑精通); //4110014 - 藥品吸收 - 提高藥水等恢復道具的效果。但對超級藥水之類按百分比恢復的道具無效。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    itemRecoveryUP += eff.getX();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(夜使者.黑暗平穩)) != null) {
//                    pad += eff.getPadX();
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(夜使者.暗器精通)) != null) {
//                    if (wt == MapleWeapon.拳套) {
//                        pad += eff.getX();
//                    }
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(夜使者.絕對領域)) != null) {
//                    bossDamageR += eff.getBossDamage();
//                }
//                /*
//                 * 超級技能
//                 */
//                if ((eff = chra.getSkillEffect(夜使者.四飛閃_BOSS殺手)) != null) {
//                    addSkillBossDamage(夜使者.四飛閃, eff.getBossDamage());
//                }
//                bx = SkillFactory.getSkill(夜使者.四飛閃_攻擊加成); //4120051 - 四連鏢-額外攻擊  - 增加四連射的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(夜使者.四飛閃, bx.getEffect(bof).getBulletCount());
//                    addSkillBulletCount(夜使者.四飛閃, bx.getEffect(bof).getBulletCount());
//                }
//                break;
//            }
//            case 420:
//            case 421:
//            case 422: {
//                if ((eff = chra.getSkillEffect(4200009)) != null) {
//                    pad += eff.getPadX();
//                }
//                bx = SkillFactory.getSkill(暗影神偷.體能訓練); //4200007 - 物理訓練 - 通過鍛煉身體，永久提高幸運和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localluk += eff.getLukX();
//                    localdex += eff.getDexX();
//                }
//                bx = SkillFactory.getSkill(暗影神偷.盾牌精通); //4200010 - 盾防精通 - 裝備盾牌時，物理防禦力和魔法防禦力增加，迴避率增加。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null && shield != null) {
//                    eff = bx.getEffect(bof);
//                    percent_wdef += eff.getX();
//                    pad += eff.getY();
//                }
//                bx = SkillFactory.getSkill(暗影神偷.貪婪); //4210012 - 貪婪 - 修煉盜賊的秘籍，增加楓幣獲得量。此外略微提高使用楓幣的所有技能的效率。
//                if ((eff = chra.getSkillEffect(暗影神偷.貪婪)) != null) {
//                    pad += eff.getPadX();
//                }
//                bx = SkillFactory.getSkill(暗影神偷.永恆黑暗); //4210013 - 永恆黑暗 - 和黑暗融為一體，永久增加最大HP、增加狀態異常抗性、增加所有屬性抗性。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR();
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(暗影神偷.致命爆擊)) != null) {
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(暗影神偷.進階精準之刀)) != null) {
//                    if (wt == MapleWeapon.短劍) {
//                        pad += eff.getX();
//                    }
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(暗影神偷.暗殺本能)) != null) {
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                /*
//                 * 超級技能
//                 */
//                if ((eff = chra.getSkillEffect(暗影神偷.致命暗殺_魔王剋星)) != null) {
//                    addSkillBossDamage(暗影神偷.致命暗殺, eff.getBossDamage());
//                    addSkillBossDamage(暗影神偷.致命暗殺_1, eff.getBossDamage());
//                    return;
//                }
//                break;
//            }
//            case 430:
//            case 431:
//            case 432:
//            case 433:
//            case 434: {
//                if ((eff = chra.getSkillEffect(影武者.自我速度激發)) != null) {
//                    speedMax += eff.getSpeedMax();
//                }
//                bx = SkillFactory.getSkill(影武者.體能訓練); //4310006 - 物理訓練 - 通過鍛煉身體，永久提高幸運和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localluk += eff.getLukX();
//                    localdex += eff.getDexX();
//                }
//                if ((eff = chra.getSkillEffect(4310005)) != null) {
//                    pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(影武者.竊取生命)) != null) {
//                    hpRecover_onAttack.put(eff.getSourceId(), new Pair<>(eff.getX(), eff.getProp()));
//                }
//                bx = SkillFactory.getSkill(影武者.激進黑暗); //4330008 - 永恆黑暗 - 和黑暗融為一體，永久增加最大HP、增加狀態異常抗性、增加所有屬性抗性。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR();
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(影武者.疾速)) != null) {
//                    if (shield != null && shield.getItemId() / 10000 == 134 && shield.getItemId() != 1342069) {
//                        critRate += eff.getProp();
//                    }
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(影武者.雙刀流精通)) != null) {
//                    if (shield != null && shield.getItemId() / 10000 == 134 && shield.getItemId() != 1342069) {
//                        pad += eff.getX();
//                    }
//                    percent_damage += eff.getPdR();
//                }
//                bx = SkillFactory.getSkill(影武者.幻影替身); //4341006 - 傀儡召喚 - 永久增加暗影雙刀的防禦力和迴避概率，並且使用技能時，將召喚為鏡像分身的分身份離出來，做成土堆。土堆可以吸引敵人的攻擊，並吸收部分傷害，以保護自己不受敵人傷害。只能在鏡像分身技能有效期間內使用。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    percent_wdef += eff.getPddR();
//                }
//                if ((eff = chra.getSkillEffect(影武者.荊棘特效)) != null) {
//                    pad += eff.getPadX();
//                    stanceProp += eff.getStanceProp();
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(影武者.短劍升天_額外攻擊);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(影武者.短劍升天, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(影武者.幻影箭_無視防禦); //4340047 - 幽靈一擊-無視防禦 - 增加幽靈一擊的無視怪物防禦力數值。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpRate(影武者.幻影箭, bx.getEffect(bof).getIgnoreMobpdpR());
//                }
//                bx = SkillFactory.getSkill(影武者.幻影箭_攻擊加成); //4340048 - 幽靈一擊-額外攻擊 - 增加幽靈一擊的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(影武者.幻影箭, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(影武者.短劍護佑_無視防禦); //4340056 - 暴怒刀陣 - 無視防禦 - 增加暴怒刀陣的防禦力無視效果。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpRate(影武者.短刀護佑, bx.getEffect(bof).getIgnoreMobpdpR());
//                }
//                bx = SkillFactory.getSkill(影武者.暴風刃); //4340056 - 暴怒刀陣 - 無視防禦 - 增加暴怒刀陣的防禦力無視效果。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpRate(影武者.暴風刃, 100);
//                }
//                break;
//            }
//            case 510:
//            case 511:
//            case 512: {
//                if ((eff = chra.getSkillEffect(5100011)) != null) {
//                    pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(拳霸.體能突破)) != null) {
//                    incMaxHPR += eff.getMhpR();
//                    stanceProp += eff.getStanceProp();
//                }
//                bx = SkillFactory.getSkill(拳霸.體能訓練); //5100010 - 物理訓練 - 通過身體鍛煉，永久性地提高力量和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX(); //提高力量
//                    localdex += eff.getDexX(); //提高敏捷
//                }
//                final MapleStatEffect skillEffect227;
//                if ((skillEffect227 = chra.getSkillEffect(拳霸.耐久)) != null) {
//                    hpRecoverTime = skillEffect227.getW() * 1000;
//                    healHPR = healMPR = skillEffect227.getX();
//                }
//                if ((eff = chra.getSkillEffect(5110010)) != null) {
//                    addDamAbsorbShieldR(eff.getDamAbsorbShieldR());
//                }
//                if ((eff = chra.getSkillEffect(拳霸.爆擊鬥氣)) != null) {
//                    critRate += eff.getCritical();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(拳霸.戰艦鯨魚號)) != null) {
//                    finalAttackSkill = 拳霸.戰艦鯨魚號_1;
//                }
//                bx = SkillFactory.getSkill(拳霸.拳霸大師); //5121015 - 蛇拳 - 在一定時間內召喚出遺忘的毒蛇之魂，增加攻擊力。被動效果是提高狀態異常和所有屬性抗性，提高拳甲熟練度。\n需要技能：#c精準拳20級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    asrR += eff.getASRRate(); //提高狀態異常
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(拳霸.暴能續發)) != null) {
//                    bossDamageR += eff.getBossDamage();
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(拳霸.閃_連殺_BOSS傷害); //5120047 - 激怒拳-BOSS殺手 - 增加激怒拳的BOSS攻擊力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillBossDamage(拳霸.閃_連殺, bx.getEffect(bof).getBossDamage());
//                }
//                bx = SkillFactory.getSkill(拳霸.閃_連殺_攻擊加成);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(拳霸.閃_連殺, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(拳霸.勾拳爆破_額外攻擊); //5120051 - 能量爆炸-額外攻擊 - 增加能量爆炸的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(拳霸.勾拳爆破, bx.getEffect(bof).getAttackCount());
//                }
//                break;
//            }
//            case 520:
//            case 521:
//            case 522: {
//                bx = SkillFactory.getSkill(槍神.體能訓練); //5200009 - 物理訓練 - 通過身體鍛煉，永久性地提高力量和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX(); //提高力量
//                    localdex += eff.getDexX(); //提高敏捷
//                    addDamAbsorbShieldR(eff.getDamAbsorbShieldR());
//                }
//                if ((eff = chra.getSkillEffect(5210009)) != null) {
//                    pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(槍神.神槍手耐性)) != null) {
//                    percent_wdef += eff.getPddR();
//                    addmaxhp += eff.getX();
//                    addmaxmp += eff.getX();
//                }
//                if ((eff = chra.getSkillEffect(槍神.金屬外殼)) != null) {
//                    incDamR += eff.getDamR();
//                    critRate += eff.getCritical();
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(槍神.進攻姿態)) != null) {
//                    addDamAbsorbShieldR(eff.getIgnoreMobDamR());
//                }
//                if ((eff = chra.getSkillEffect(槍神.無盡追擊)) != null) {
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(槍神.船員指令)) != null) {
//                    addmaxhp += eff.getMaxHpX();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(槍神.槍神奧義)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                    pad += eff.getPadX();
//                }
//                /*
//                 * 超級技能
//                 */
//                if ((eff = chra.getSkillEffect(槍神.爆頭射擊_攻擊加成)) != null) {
//                    addSkillAttackCount(槍神.爆頭射擊, eff.getAttackCount());
//                }
//                if ((eff = chra.getSkillEffect(槍神.爆頭射擊_BOSS傷害)) != null) {
//                    addSkillBossDamage(槍神.爆頭射擊, eff.getBossDamage());
//                }
//                if ((eff = chra.getSkillEffect(槍神.瞬_迅雷_BOSS傷害)) != null) {
//                    addSkillBossDamage(槍神.瞬_迅雷, eff.getBossDamage());
//                }
//                break;
//            }
//            case 501:
//            case 530:
//            case 531:
//            case 532: {
//                bx = SkillFactory.getSkill(重砲指揮官.加農砲升級); //升級火炮 - 對大炮進行改良，永久性地提高攻擊力和防禦力
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    pad += bx.getEffect(bof).getPadX();
//                    wdef += bx.getEffect(bof).getPddX();
//                }
//                if ((eff = chra.getSkillEffect(重砲指揮官.烈火暴擊)) != null) {
//                    critRate += eff.getCritical();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                bx = SkillFactory.getSkill(重砲指揮官.海盜訓練); //海盜訓練 - 通過海盜的秘密修煉，提高力量和敏捷
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                if ((eff = chra.getSkillEffect(重砲指揮官.幸運木桶)) != null) {
//                    percent_damage += eff.getPdR();
//                }
//                bx = SkillFactory.getSkill(重砲指揮官.強化加農砲); //火炮強化 - 永久性地強化大炮，提高攻擊力和攻擊速度
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    pad += bx.getEffect(bof).getPadX();
//                }
//                bx = SkillFactory.getSkill(重砲指揮官.終極狀態); //生命強化 - 永久性地強化體力、防禦力、增加狀態異常抗性
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR(); //以前是這個 好像有點問題加不滿血getHpR()
//                    asrR += eff.getASRRate();
//                    percent_wdef += eff.getPddR();
//                }
//                if ((eff = chra.getSkillEffect(重砲指揮官.海盜精神)) != null) {
//                    bossDamageR += eff.getBossDamage();
//                }
//                bx = SkillFactory.getSkill(重砲指揮官.炎熱加農砲); //極限燃燒彈 - 將大炮的性能提高到極限，永久性地增加傷害。此外，攻擊時有一定概率在一定程度上無視怪物的防禦力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incDamR += eff.getDamR();
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(重砲指揮官.雙胞胎猴子_傷害分裂); //5320043 - 雙胞胎猴子支援-增加 - 降低雙胞胎猴子支援的傷害，增加攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(重砲指揮官.雙胞胎猴子, bx.getEffect(bof).getAttackCount());
//                    addSkillAttackCount(重砲指揮官.雙胞胎猴子_1, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(重砲指揮官.雙胞胎猴子_時間延長); //5320044 - 雙胞胎猴子支援-堅持 - 增加雙胞胎猴子支援的持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(重砲指揮官.雙胞胎猴子, bx.getEffect(bof).getDuration());
//                    addSkillDuration(重砲指揮官.雙胞胎猴子_1, bx.getEffect(bof).getDuration());
//                }
//                bx = SkillFactory.getSkill(重砲指揮官.加農砲火箭_攻擊加成); //5320048 - 加農火箭炮-額外攻擊 - 增加加農火箭炮的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(重砲指揮官.加農砲火箭, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(重砲指揮官.加農砲連擊_獎勵攻擊); //5320051 - 集中炮擊-額外攻擊 - 增加集中炮擊的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(重砲指揮官.加農砲連擊, bx.getEffect(bof).getAttackCount());
//                    addSkillBulletCount(重砲指揮官.加農砲連擊, bx.getEffect(bof).getBulletCount());
//                }
//                break;
//            }
//            case 1100:
//            case 1110:
//            case 1111:
//            case 1112: {
//                // new
//                if ((eff = chra.getSkillEffect(聖魂劍士.SUMMON_元素靈魂)) != null) {
//                    ignoreMobpdpR += eff.getIgnoreMobpdpR();
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.靈魂守護者)) != null) {
//                    addmaxhp += eff.getMaxHpX();
//                    wdef += eff.getPddX();
//                    stanceProp += eff.getStanceProp();
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.靈魂迴響)) != null) {
//                    speed += eff.getPsdSpeed();
//                    jump += eff.getPsdJump();
//                    speedMax += eff.getSpeedMax();
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.堅定信念)) != null) {
//                    pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.精準之劍)) != null) {
//                    trueMastery += eff.getMastery();
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.體能訓練)) != null) {
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//
//
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.光速反應)) != null) {
//                    localstr += eff.getStrX();
//                    // 攻速+2
//                }
//                // old
//                if ((eff = chra.getSkillEffect(聖魂劍士.西格諾斯祝福_劍士)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.SUMMON_元素靈魂)) != null) {
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.鋼鐵意志)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                    addDamAbsorbShieldR(eff.getX());
//                    hpRecoverTime = eff.getW() * 1000;
//                    healHPR = eff.getY();
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.戰吼)) != null) {
//                    pad += eff.getPadX();
//                    localstr += eff.getStrX();
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.靈魂誓約)) != null) {
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                    localint += eff.getIntX();
//                    localluk += eff.getLukX();
//                    stanceProp += eff.getStanceProp();
//                    critRate += eff.getCritical();
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.專家之劍)) != null) {
//                    if (wt == MapleWeapon.單手劍 || wt == MapleWeapon.雙手劍) {
//                        pad += eff.getX();
//                    }
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.無形劍)) != null) {
//                    bossDamageR += eff.getBossDamage();
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.真實之眼_持續時間)) != null) {
//                    addSkillDuration(聖魂劍士.真實之眼, eff.getDuration());
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.分裂與狂斬_額外目標)) != null) {
//                    addSkillAttackCount(聖魂劍士.雙重狂斬, eff.getAttackCount());
//                    addSkillAttackCount(聖魂劍士.雙重狂斬, eff.getAttackCount());
//                }
//                if ((eff = chra.getSkillEffect(聖魂劍士.分裂與狂斬_無視防禦)) != null) {
//                    addSkillBossDamage(聖魂劍士.光輝衝刺, eff.getBossDamage());
//                    addSkillBossDamage(聖魂劍士.熾烈突擊, eff.getBossDamage());
//                }
//                break;
//            }
            case 1200:
            case 1210:
            case 1211:
            case 1212: {
                if ((eff = chra.getSkillEffect(烈焰巫師.西格諾斯祝福_法師)) != null) {
                    asrR += eff.getASRRate();
                    terR += eff.getTERRate();
                }
                if ((eff = chra.getSkillEffect(烈焰巫師.天生才能)) != null) {
                    incMaxMPR += eff.getMmpR();
                    addmaxmp += eff.getLv2mmp() * chra.getLevel();
                    if (wt == MapleWeapon.短杖) {
                        critRate += 3;
                    }
                }
                if ((eff = chra.getSkillEffect(烈焰巫師.咒語磨練)) != null) {
                    mad += eff.getX();
                }
                if ((eff = chra.getSkillEffect(烈焰巫師.解放的魔力)) != null) {
                    incMpCon += eff.getX() - 100;
                    incFinalDamage += eff.getY() - 100;
                }
                if ((eff = chra.getSkillEffect(烈焰巫師.分析弱點)) != null) {
                    critRate += eff.getCritical();
                    criticalDamage += eff.getCriticalDamage();
                }
                if ((eff = chra.getSkillEffect(烈焰巫師.火焰領悟)) != null) {
                    localint += eff.getIntX();
                }
                if ((eff = chra.getSkillEffect(烈焰巫師.魔法真理)) != null) {
                    mad += eff.getX();
                    percent_damage += eff.getMdR();
                    criticalDamage += eff.getCriticalDamage();
                }
                if ((eff = chra.getSkillEffect(烈焰巫師.不會滅的火焰)) != null) {
                    asrR += eff.getASRRate();
                    terR += eff.getTERRate();
                }

                if ((eff = chra.getSkillEffect(烈焰巫師.元素火焰_速發反擊)) != null) {
                    addSkillAttackCount(烈焰巫師.Return_元素之炎I_1, eff.getAttackCount());
                    addSkillAttackCount(烈焰巫師.Return_元素之炎II_1, eff.getAttackCount());
                    addSkillAttackCount(烈焰巫師.Return_元素之炎III_1, eff.getAttackCount());
                    addSkillAttackCount(烈焰巫師.Return_元素之炎IV_1, eff.getAttackCount());
                    addSkillAttackCount(烈焰巫師.Return_元素之炎IV, eff.getAttackCount());
                }
                if ((eff = chra.getSkillEffect(烈焰巫師.滅絕炙陽_範圍增加)) != null) {
                    addSkillAttackCount(烈焰巫師.極致熾烈_1, eff.getAttackCount());
                    addSkillAttackCount(烈焰巫師.極致熾烈, eff.getAttackCount());
                }
                break;
            }
//            case 1300:
//            case 1310:
//            case 1311:
//            case 1312: {
//                if ((eff = chra.getSkillEffect(破風使者.西格諾斯祝福_弓箭手)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(破風使者.MOVE_風影漫步)) != null) {
//                    speed += eff.getPsdSpeed();
//                    jump += eff.getPsdJump();
//                    speedMax += eff.getSpeedMax();
//                }
//                if ((eff = chra.getSkillEffect(破風使者.風之耳語)) != null) {
//                    pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(破風使者.體能訓練)) != null) {
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                if ((eff = chra.getSkillEffect(破風使者.身輕如羽)) != null) {
//                    addDamAbsorbShieldR(eff.getX());
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(破風使者.迴避之風)) != null) {
//                    wdef += eff.getPddX();
//                }
//                if ((eff = chra.getSkillEffect(破風使者.弓術精通)) != null) {
//                    if (wt == MapleWeapon.弓) {
//                        pad += eff.getX();
//                    }
//                    percent_damage += eff.getPdR();
//                    criticalDamage += eff.getCriticalDamage();
//                    bossDamageR += eff.getBossDamage();
//                }
//                if ((eff = chra.getSkillEffect(13120004)) != null) {
//                    incPadR += eff.getPadR();
//                    incDexR += eff.getDexR();
//                    incMaxHPR += eff.getMhpR();
//                }
//                if ((eff = chra.getSkillEffect(破風使者.天空之歌_魔王剋星)) != null) {
//                    addSkillBossDamage(破風使者.天空之歌, eff.getBossDamage());
//                    return;
//                }
//                break;
//            }
//            case 1400:
//            case 1410:
//            case 1411:
//            case 1412: {
//                if ((eff = chra.getSkillEffect(暗夜行者.西格諾斯祝福_盜賊)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.速度激發)) != null) {
//                    speedMax += eff.getSpeedMax();
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.投擲精通)) != null) {
//                    incDamR += eff.getDamR();
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.爆擊投擲)) != null) {
//                    critRate += eff.getProp();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.體能訓練)) != null) {
//                    localluk += eff.getLukX();
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.闇黑調濾)) != null) {
//                    addSkillProp(暗夜行者.SUMMON_元素闇黑, eff.getProp());
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.闇黑調濾Ⅱ)) != null) {
//                    addSkillProp(暗夜行者.SUMMON_元素闇黑, eff.getProp());
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.闇黑調濾Ⅲ)) != null) {
//                    addSkillProp(暗夜行者.SUMMON_元素闇黑, eff.getProp());
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.蝙蝠交流)) != null) {
//                    addSkillProp(暗夜行者.Switch_type_Magic_暗影蝙蝠, eff.getProp());
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.蝙蝠交流Ⅲ)) != null) {
//                    addSkillProp(暗夜行者.Switch_type_Magic_暗影蝙蝠, eff.getProp());
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.激進闇黑)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.腎上腺素)) != null) {
//                    itemRecoveryUP += eff.getX();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.投擲專家)) != null) {
//                    if (wt == MapleWeapon.拳套) {
//                        pad += eff.getX();
//                    }
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.闇黑祝福)) != null) {
//                    pad += eff.getPadX();
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.影吸收)) != null) {
//                    addDamAbsorbShieldR(eff.getIgnoreMobDamR());
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.五連投擲_BOSS殺手)) != null) {
//                    addSkillBossDamage(暗夜行者.五連投擲, eff.getBossDamage());
//                }
//                if ((eff = chra.getSkillEffect(暗夜行者.闇黑天魔_冷卻時間重置)) != null) {
//                    addSkillCooltimeReduce(暗夜行者.SUMMON_ATTACK_闇黑天魔, eff.getCooltimeReduceR()); // 闇黑天魔的冷卻處理
//                }
//                break;
//            }
//            case 1500:
//            case 1510:
//            case 1511:
//            case 1512: {
//                if ((eff = chra.getSkillEffect(閃雷悍將.西格諾斯祝福_海盜)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.雷電使者)) != null) {
//                    addSkillProp(閃雷悍將.SUMMON_元素雷電, eff.getProp());
//                    addSkillAttackCount(閃雷悍將.SUMMON_元素雷電, eff.getV());
//                    speed += eff.getPsdSpeed();
//                    jump += eff.getPsdJump();
//                    speedMax += eff.getSpeedMax();
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.鍛鍊)) != null) {
//                    localstr += eff.getStrX();
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.雷聲)) != null) {
//                    addSkillProp(閃雷悍將.SUMMON_元素雷電, eff.getProp());
//                    addSkillAttackCount(閃雷悍將.SUMMON_元素雷電, eff.getV());
//                    pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(15110023)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(15110024)) != null) {
//                    incDamR += eff.getDamR();
//                    addDamAbsorbShieldR(eff.getDamAbsorbShieldR());
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.雷帝)) != null) {
//                    addSkillProp(閃雷悍將.SUMMON_元素雷電, eff.getProp());
//                    addSkillAttackCount(閃雷悍將.SUMMON_元素雷電, eff.getV());
//                    pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.開天闢地)) != null) {
//                    incDamR += eff.getDamR();
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.火之書)) != null) {
//                    if (wt == MapleWeapon.指虎) {
//                        pad += eff.getX();
//                    }
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.刺激)) != null) {
//                    incMaxHPR += eff.getMhpR();
//                    addDamAbsorbShieldR(eff.getIgnoreMobDamR());
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.雷神)) != null) {
//                    addSkillProp(閃雷悍將.SUMMON_元素雷電, eff.getProp());
//                    addSkillAttackCount(閃雷悍將.SUMMON_元素雷電, eff.getV());
//                    critRate += eff.getCritical();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.疾風_次數強化)) != null) {
//                    addSkillAttackCount(閃雷悍將.疾風, eff.getAttackCount());
//                    addSkillAttackCount(閃雷悍將.颱風, eff.getAttackCount());
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.霹靂_次數強化)) != null) {
//                    addSkillAttackCount(閃雷悍將.霹靂, eff.getAttackCount());
//                }
//                if ((eff = chra.getSkillEffect(閃雷悍將.消滅_魔王剋星)) != null) {
//                    addSkillBossDamage(閃雷悍將.消滅, eff.getBossDamage());
//                }
//                break;
//            }
//            case 2000:
//            case 2100:
//            case 2110:
//            case 2111:
//            case 2112: {
//                if ((eff = chra.getSkillEffect(狂狼勇士.戰鬥衝刺)) != null) {
//                    speed += eff.getPsdSpeed();
//                    speedMax += eff.getSpeedMax();
//                }
//                if ((eff = chra.getSkillEffect(21000000)) != null) {
//                    stanceProp += eff.getStanceProp();
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.終極攻擊)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.吸血術)) != null) {
//                    incMaxHPR += eff.getMhpR();
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.寒冰屬性)) != null) {
//                    incDamR += eff.getDamR();
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.體能訓練)) != null) {
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.進階矛之鬥氣)) != null) {
//                    stanceProp += eff.getStanceProp();
//                    pad += eff.getPadX();
//                    asrR += eff.getASRRate();
//                    critRate += eff.getCritical();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(21110029)) != null) {
//                    wdef += eff.getPddX();
//                    pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.攀爬攻擊)) != null) {
//                    incDamR += eff.getDamR();
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.攻擊戰術)) != null) {
//                    if (wt == MapleWeapon.矛) {
//                        pad += eff.getX();
//                    }
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.防禦戰術)) != null) {
//                    addDamAbsorbShieldR(eff.getT());
//                    incMaxHPR += eff.getMhpR();
//                    wdef += eff.getPddX();
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.進階終極攻擊)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                    pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.鬥氣爆發_時間延續)) != null) {
//                    addSkillDuration(狂狼勇士.鬥氣爆發, eff.getDuration());
//                }
//                if ((eff = chra.getSkillEffect(狂狼勇士.比耀德_攻擊加成)) != null) {
//                    addSkillAttackCount(狂狼勇士.比耀德_3擊, eff.getAttackCount());
//                    addSkillAttackCount(狂狼勇士.比耀德_2擊, eff.getAttackCount());
//                    addSkillAttackCount(狂狼勇士.比耀德_1, eff.getAttackCount());
//                    addSkillAttackCount(狂狼勇士.比耀德, eff.getAttackCount());
//                    return;
//                }
//                break;
//            }
//            case 2200:
//            case 2211:
//            case 2214:
//            case 2217: {
//                if ((eff = chra.getSkillEffect(龍魔導士.龍之火花)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.龍之魂)) != null) {
//                    wdef += eff.getPddX();
//                    speed += eff.getPsdSpeed();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.魔幻鏈)) != null) {
//                    incMaxMPR += eff.getMmpR();
//                    addmaxmp += eff.getLv2mmp() * chra.getLevel();
//                    mad += eff.getMadX();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.智慧昇華)) != null) {
//                    localint += eff.getIntX();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.交感)) != null) {
//                    stanceProp += eff.getStanceProp();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.咒語精通)) != null) {
//                    critRate += eff.getCritical();
//                    mad += eff.getX();
//                    if (wt == MapleWeapon.短杖) {
//                        critRate += 3;
//                    }
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.進階龍之火花)) != null) {
//                    finalAttackSkill = eff.getSourceId();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.龍之躍)) != null) {
//                    addSkillDamageIncrease(龍魔導士.風之環, eff.getS());
//                    addSkillDamageIncrease(龍魔導士.龍之捷, eff.getS2());
//                    addSkillDamageIncrease(龍魔導士.龍之捷_1, eff.getS2());
//                    addSkillDamageIncrease(龍魔導士.龍之捷_2, eff.getS2());
//                    addSkillDamageIncrease(龍魔導士.風之捷_1, eff.getU());
//                    addSkillDamageIncrease(龍魔導士.風之捷_1, eff.getU());
//                    addSkillDamageIncrease(龍魔導士.閃雷之捷, eff.getU2());
//                    addSkillDamageIncrease(龍魔導士.閃雷之捷_攻擊, eff.getU2());
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.自然力重置)) != null) {
//                    percent_damage += eff.getMdR();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.魔法爆擊)) != null) {
//                    critRate += eff.getProp();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.魔法抵抗)) != null) {
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.魔力增幅)) != null) {
//                    incMpCon += eff.getX();
//                    incFinalDamage += eff.getZ();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.龍之潛能)) != null) {
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.龍之氣息)) != null) {
//                    addSkillDamageIncrease(龍魔導士.風之環, eff.getS());
//                    addSkillDamageIncrease(龍魔導士.龍之捷, eff.getS2());
//                    addSkillDamageIncrease(龍魔導士.龍之捷_1, eff.getS2());
//                    addSkillDamageIncrease(龍魔導士.龍之捷_2, eff.getS2());
//                    addSkillDamageIncrease(龍魔導士.風之捷_1, eff.getU());
//                    addSkillDamageIncrease(龍魔導士.風之捷_1, eff.getU());
//                    addSkillDamageIncrease(龍魔導士.閃雷之捷, eff.getU2());
//                    addSkillDamageIncrease(龍魔導士.閃雷之捷_攻擊, eff.getU2());
//                    addSkillDamageIncrease(龍魔導士.閃雷之環, eff.getW());
//                    addSkillDamageIncrease(龍魔導士.龍之躍, eff.getW2());
//                    addSkillDamageIncrease(龍魔導士.龍之躍_攻擊, eff.getW2());
//                    addSkillDamageIncrease(龍魔導士.閃雷之躍, eff.getQ());
//                    addSkillDamageIncrease(龍魔導士.閃雷之躍_攻擊, eff.getQ());
//                    addSkillDamageIncrease(龍魔導士.塵土之躍, eff.getQ2());
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.精通魔法)) != null) {
//                    criticalDamage += eff.getCriticalDamage();
//                    mad += eff.getX();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.歐尼斯的意志)) != null) {
//                    addDamAbsorbShieldR(eff.getIgnoreMobDamR());
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.高階龍之潛能)) != null) {
//                    bossDamageR += eff.getBossDamage();
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.龍之捷_冷卻減免)) != null) {
//                    addSkillCooltimeReduce(龍魔導士.龍之捷, eff.getCooltimeReduceR());
//                    addSkillCooltimeReduce(龍魔導士.龍之捷_1, eff.getCooltimeReduceR());
//                    addSkillCooltimeReduce(龍魔導士.龍之捷_2, eff.getCooltimeReduceR());
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.龍之捷_雷霆攻擊加成)) != null) {
//                    addSkillAttackCount(龍魔導士.閃雷之捷, eff.getAttackCount());
//                    addSkillAttackCount(龍魔導士.閃雷之捷_攻擊, eff.getAttackCount());
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.龍之躍_冷卻減免)) != null) {
//                    addSkillCooltimeReduce(龍魔導士.龍之躍_攻擊, eff.getCooltimeReduceR());
//                    addSkillCooltimeReduce(龍魔導士.龍之躍, eff.getCooltimeReduceR());
//                }
//                if ((eff = chra.getSkillEffect(龍魔導士.龍之氣息_冷卻減免)) != null) {
//                    addSkillCooltimeReduce(龍魔導士.龍之氣息, eff.getCooltimeReduceR());
//                }
//            }
            case 2001:
            case 2300:
            case 2310:
            case 2311:
            case 2312: {
//                if ((eff = chra.getSkillEffect(精靈遊俠.王的資格)) != null) {
//                    chra.getTrait(MapleTraitType.charm).setLevel(30, chra);
//                    jump += eff.getPsdJump();
//                    speed += eff.getPsdSpeed();
//                }
//                if ((eff = chra.getSkillEffect(精靈遊俠.極速雙弩)) != null) {
//                    localdex += eff.getDexX();
//                }
//                final MapleStatEffect skillEffect423;
//                if ((skillEffect423 = chra.getSkillEffect(精靈遊俠.精靈的回復)) != null) {
//                    hpRecoverTime = 4000;
//                    healHPR = healMPR = skillEffect423.getX();
//                }
//                final MapleStatEffect skillEffect425;
//                if ((skillEffect425 = chra.getSkillEffect(精靈遊俠.潛在力量)) != null) {
//                    this.speed += skillEffect425.getPsdSpeed();
//                    this.speedMax = skillEffect425.getU();
//                    this.incDamR += skillEffect425.getDamR();
//                    dodgeChance += skillEffect425.getER();
//                }
//                final MapleStatEffect skillEffect426;
//                if ((skillEffect426 = chra.getSkillEffect(精靈遊俠.鋒利瞄準)) != null) {
//                    this.critRate += skillEffect426.getCritical();
//                }
//                final MapleStatEffect skillEffect427;
//                if ((skillEffect427 = chra.getSkillEffect(23100003)) != null) {
//                    this.incDamR += skillEffect427.getDamR();
//                    this.critRate += skillEffect427.getCritical();
//                }
//                bx = SkillFactory.getSkill(精靈遊俠.終極攻擊_雙弩槍);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    finalAttackSkill = 精靈遊俠.終極攻擊_雙弩槍;
//                }
//                bx = SkillFactory.getSkill(精靈遊俠.體能訓練); //23100008 - 物理訓練 - 通過身體鍛煉，永久性地提高力量和敏捷
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                bx = SkillFactory.getSkill(精靈遊俠.最終一擊); //23100004 - 終結箭 - 用箭矢掃射被衝鋒拳打到空中的敵人。只能在衝鋒拳之後使用。\n需要技能：#c衝鋒拳1級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    this.percent_damage += bx.getEffect(bof).getPdR() + this.percent_damage * bx.getEffect(bof).getPdR() / 100.0;
//                    this.pad += bx.getEffect(bof).getPadX();
//                    dodgeChance += bx.getEffect(bof).getProp();
//                }
                bx = SkillFactory.getSkill(精靈遊俠.遠古意志); //23121004 - 古老意志 - 在一定時間內獲得古代精靈的祝福，傷害和HP增加，永久性地提高火焰咆哮的迴避率。\n需要技能：#c火焰咆哮5級以上#
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0 && bx != null) {
                    dodgeChance += bx.getEffect(bof).getProp();
                }
//                bx = SkillFactory.getSkill(精靈遊俠.進階雙弩槍精通); //23120009 - 雙弩槍專家 - 增加雙弩槍系列武器的熟練度、物理攻擊力和爆擊最小傷害。\n需要技能：#c精準雙弩槍20級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getX();
//                    trueMastery += eff.getMastery();
//                    criticalDamage += eff.getCriticalMin();
//                }
//                bx = SkillFactory.getSkill(精靈遊俠.破防射擊); //23120010 - 防禦突破 - 攻擊時有一定概率100%無視敵人的防禦力。對BOSS怪同樣有效。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpR(bx.getEffect(bof).getX());
//                    criticalDamage += eff.getCriticalMin();
//                    bossDamageR += eff.getBossDamage();
//                    incPadR += eff.getPadR();
//                }
//
//                bx = SkillFactory.getSkill(精靈遊俠.進階終極攻擊); //23120012 - 進階終極攻擊 - 永久性地增加攻擊力和命中率，終極：雙弩槍技能的發動概率和傷害大幅上升。\n需要技能：#c終極：雙弩槍20級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    finalAttackSkill = 精靈遊俠.進階終極攻擊;
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX();
//                    addSkillDamageIncrease(精靈遊俠.雙弩槍精通, eff.getDamage());
//                }
//                bx = SkillFactory.getSkill(精靈遊俠.依古尼斯咆嘯);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX();
//                }
                break;
            }
            case 2003:
            case 2400:
            case 2410:
            case 2411:
            case 2412: {
//                final MapleStatEffect skillEffect443;
//                if ((skillEffect443 = chra.getSkillEffect(幻影俠盜.致命本能)) != null) {
//                    this.critRate += skillEffect443.getCritical();
//                }
                bx = SkillFactory.getSkill(幻影俠盜.高洞察力); //20030206 - 靈敏身手 - 幻影擁有卓越的洞察力和使用各種武器的能力。
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0 && bx != null) {
                    eff = bx.getEffect(bof);
//                    localdex += eff.getDexX();
//                    dodgeChance += eff.getER();
                    chra.getTrait(MapleTraitType.insight).setLevel(20, chra);
                    chra.getTrait(MapleTraitType.craft).setLevel(20, chra);
                }
//                bx = SkillFactory.getSkill(幻影俠盜.幻影瞬步); //24001002 - 迅捷幻影 - 永久性提高移動速度、最大移動速度、跳躍力，在跳躍中使用時，可以向前方飛行很遠距離。技能等級越高，移動距離越遠。可以用作跳躍鍵。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    speed += eff.getPsdSpeed();
//                    jump += eff.getPsdJump();
//                    speedMax += eff.getSpeedMax();
//                }
                bx = SkillFactory.getSkill(幻影俠盜.Skill_急速迴避); //24000003 - 快速逃避 - 永久提高迴避率。
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0 && bx != null) {
                    eff = bx.getEffect(bof);
                    dodgeChance += eff.getX();
                }
//                bx = SkillFactory.getSkill(幻影俠盜.幸運富翁); //24100006 - 超級幸運星 - 永久提高幸運。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localluk += eff.getLukX();
//                }
//                bx = SkillFactory.getSkill(幻影俠盜.幸運女神保護); //24111002 - 神秘的運氣 - 最幸運的幻影可以永久性地提高運氣。使用技能時，進入可以避免一次死亡並恢復體力的幸運狀態。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR(); //最大Hp增加
//                    incMaxMPR += eff.getMmpR(); //最大Mp增加
//                    asrR += bx.getEffect(bof).getASRRate(); // 狀態異常耐性增加
//                    terR += bx.getEffect(bof).getTERRate(); // 屬性耐性增加
//                    this.stanceProp += eff.getStanceProp(); // 格擋增加
//                }
//                bx = SkillFactory.getSkill(幻影俠盜.幸運幻影); //24111002 - 神秘的運氣 - 最幸運的幻影可以永久性地提高運氣。使用技能時，進入可以避免一次死亡並恢復體力的幸運狀態。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localluk += eff.getLukX();
//                }
                bx = SkillFactory.getSkill(幻影俠盜.死神卡牌); //24120002 - 黑色秘卡 - 幻影的攻擊造成爆擊時，有一定概率從幻影身上飛出卡片，自動攻擊周圍的敵人。#c此時卡片值增加1。#\n此外迴避概率永久性增加。
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0 && bx != null) {
                    eff = bx.getEffect(bof);
                    dodgeChance += eff.getX();
                }
//                final MapleStatEffect skillEffect449;
//                if ((skillEffect449 = chra.getSkillEffect(幻影俠盜.爆擊天賦)) != null) {
//                    this.critRate += skillEffect449.getCritical();
//                    this.percent_damage += skillEffect449.getPdR();
//                }
//                bx = SkillFactory.getSkill(幻影俠盜.進階手杖精通); //24120006 - 手杖專家 - 增加手杖系列武器的熟練度、物理攻擊力、爆擊最小傷害。\n需要技能：#c精準手杖20級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX(); //增加攻擊
//                    trueMastery += eff.getMastery(); //熟練度
//                    criticalDamage += eff.getCriticalMin(); //爆擊最小傷害
//                    this.percent_damage += eff.getPdR() + this.percent_damage * eff.getPdR() / 100.0;
//                }
//                bx = SkillFactory.getSkill(幻影俠盜.艾麗亞祝禱); //24121004，消耗MP#mpCon，出現艾麗亞的幻影\\n[被動效果:傷害增加#damR%，攻擊時無視敵人防禦率#ignoreMobpdpR%，格擋增加#stanceProp%]
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incDamR += eff.getDamR();
//                    ignoreMobpdpR += eff.getIgnoreMobpdpR();
//                    stanceProp += eff.getStanceProp();
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(幻影俠盜.卡牌風暴_減少冷卻時間); //24120044 - 卡片風暴-縮短冷卻時間 - 卡片風暴的冷卻時間縮短。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillCooltimeReduce(幻影俠盜.卡牌風暴, bx.getEffect(bof).getCooltimeReduceR());
//                }
                break;
            }
//            case 2004: //夜光
//            case 2700:
//            case 2710:
//            case 2711:
//            case 2712: {
//                bx = SkillFactory.getSkill(夜光.光明力量); //20040221 - 光之力量 - 與命運對抗的意志和魔法師特有的洞察力。智力值很高，受光之力量的保護而不受暗黑效果侵襲。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localint += eff.getIntX();
//                    chra.getTrait(MapleTraitType.will).setLevel(20, chra);
//                    chra.getTrait(MapleTraitType.insight).setLevel(20, chra);
//                }
//                bx = SkillFactory.getSkill(夜光.滲透); //20040218 - 穿透 - 用穿透一切阻礙的光之力量，無視敵人的部分防禦力
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpR(bx.getEffect(bof).getIgnoreMobpdpR());
//                }
//                bx = SkillFactory.getSkill(夜光.極速詠唱);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    localint += bx.getEffect(bof).getIntX();
//                    //attackSpeed -= 2;
//                }
//                bx = SkillFactory.getSkill(夜光.閃光瞬步); //27001002 - 光束瞬移 - 變成光束移動。按住方向鍵施展技能就能瞬移到目標方向。瞬移後的短暫時間內會變成透明狀態。作為被動效果，永久增加移動速度和跳躍力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    jump += eff.getPsdJump();
//                    speed += eff.getPsdSpeed();
//                }
//                bx = SkillFactory.getSkill(夜光.魔法防禦); //27000003 - 普通魔法防護 - 受到的傷害由一定比例的MP代替。額外的永久增加物理、魔法防禦力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    wdef += eff.getPddX();
//                }
//                bx = SkillFactory.getSkill(夜光.擴充魔力);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    incMaxMPR += bx.getEffect(bof).getMmpR();
//                }
//                final MapleStatEffect skillEffect462;
//                if ((skillEffect462 = chra.getSkillEffect(夜光.咒語精通)) != null) {
//                    this.mad += skillEffect462.getX();
//                }
//                bx = SkillFactory.getSkill(夜光.黑暗祝福); //27100008 - 黑暗祝福
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    this.mad += bx.getEffect(bof).getMadX();
//                    this.damAbsorbShieldR += bx.getEffect(bof).getX();
//
//                }
//                bx = SkillFactory.getSkill(夜光.智慧昇華); //27100006 - 智慧激發 - 永久提升智力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    localint += bx.getEffect(bof).getIntX();
//                }
//                bx = SkillFactory.getSkill(夜光.魔力護盾); //27111004 - 抵抗之魔法盾 - 使用可以無視狀態異常的保護罩。使用無視狀態異常的效果，且使用次數超過指定次數以上時，進入冷卻時間。作為被動效果，增加所有屬性抗性和狀態異常抵抗力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    asrR += bx.getEffect(bof).getASRRate();
//                    terR += bx.getEffect(bof).getTERRate();
//                }
//                bx = SkillFactory.getSkill(夜光.光暗精通);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    this.addSkillDuration(夜光.平衡_光明, bx.getEffect(bof).getDuration());
//                }
//                final MapleStatEffect skillEffect466;
//                if ((skillEffect466 = chra.getSkillEffect(夜光.晨星殞落)) != null) {
//                    this.incFinalDamage += skillEffect466.getMdR();
//                }
//                final MapleStatEffect skillEffect467;
//                if ((skillEffect467 = chra.getSkillEffect(夜光.黑暗魔心)) != null) {
//                    this.percent_damage += skillEffect467.getMdR();
//                    this.addIgnoreMobpdpR(skillEffect467.getIgnoreMobpdpR());
//                }
//                final MapleStatEffect skillEffect468;
//                if ((skillEffect468 = chra.getSkillEffect(夜光.精通魔法)) != null) {
//                    this.mad += skillEffect468.getX();
//                    this.criticalDamage += skillEffect468.getCriticalDamage();
//                }
//                break;
//            }
//            case 2005: //隱月
//            case 2500:
//            case 2510:
//            case 2511:
//            case 2512: {
//                final MapleStatEffect skillEffect475;
//                if ((skillEffect475 = chra.getSkillEffect(隱月.精靈凝聚1式)) != null) {
//                    this.hpRecover_onAttack.put(skillEffect475.getSourceId(), new Pair<>(skillEffect475.getX(), 50));
//                    this.stanceProp += skillEffect475.getStanceProp();
//                    this.addDamAbsorbShieldR(skillEffect475.getDamAbsorbShieldR());
//                }
//                bx = SkillFactory.getSkill(隱月.乾坤護體); //25000105 - 乾坤一體 - 吸收天地之息，調節身體，增加物理防禦力/魔法防禦力和最大HP/MP。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR(); //最大Hp增加
//                    incMaxMPR += eff.getMmpR(); //最大Mp增加
//                    wdef += eff.getPddX(); //物理防禦力
//                }
//                bx = SkillFactory.getSkill(隱月.緊急迴避); //25101205 - 後方移動 - 向後方滑動，永久增加迴避率。\n#c在使用技能過程中可以移動。#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    dodgeChance += eff.getX();
//                }
//                final MapleStatEffect skillEffect477;
//                if ((skillEffect477 = chra.getSkillEffect(隱月.精靈降臨2式)) != null) {
//                    this.speed += skillEffect477.getPsdSpeed();
//                }
//                bx = SkillFactory.getSkill(隱月.肌肉鍛鍊); //25100108 - 力量鍛煉 - 通過鍛煉身體, 永久性增加力量。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                }
//                bx = SkillFactory.getSkill(隱月.精靈降臨3式); //25110107 - 精靈凝聚第3招 - 強化與精靈的團結，永久增加攻擊力和傷害。\n[需要技能]：#c精靈凝聚第2招10級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX();
//                    incDamR += eff.getDamR();
//                }
//                bx = SkillFactory.getSkill(隱月.魂體強化); //25110108 - 招魂式 - 將精靈的力量與身體融合，物理防禦力、魔法防禦力、增加狀態異常抗性、所有屬性抗性增加。[需要技能]：#c乾坤一體20級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    wdef += eff.getPddX(); //物理防禦力
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                bx = SkillFactory.getSkill(隱月.精靈降臨4式); //25120112 - 精靈凝聚第4招 - 強化與精靈的團結，永久性地在攻擊時無視敵人的部分防禦力，BOSS攻擊力增加。[需要技能]：#c精靈凝聚第3招20級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR()); //無視防禦
//                    bossDamageR += eff.getBossDamage(); //BOSS傷害
//                }
//                final MapleStatEffect skillEffect482;
//                if ((skillEffect482 = chra.getSkillEffect(隱月.高級指虎熟練)) != null) {
//                    this.percent_damage += skillEffect482.getPdR();
//                    this.criticalDamage += skillEffect482.getCriticalDamage();
//                }
//                final MapleStatEffect skillEffect483;
//                if ((skillEffect483 = chra.getSkillEffect(隱月.看穿弱點)) != null) {
//                    this.critRate += skillEffect483.getCritical();
//                }
//                final MapleStatEffect skillEffect485;
//                if ((skillEffect485 = chra.getSkillEffect(隱月.鬼斬_魔王剋星)) != null) {
//                    this.addSkillBossDamage(隱月.鬼斬, skillEffect485.getBossDamage());
//                }
//                /*
//                 * 處理超級技能
//                 */
//                bx = SkillFactory.getSkill(隱月.鬼斬_次數強化); //25120148 - 鬼斬-額外攻擊 - 增加鬼斬的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(隱月.鬼斬, bx.getEffect(bof).getAttackCount());
//                }
//                break;
//            }
//            case 3100:
//            case 3110:
//            case 3111:
//            case 3112: //惡魔獵手
//                final MapleStatEffect skillEffect490;
//                if ((skillEffect490 = chra.getSkillEffect(惡魔殺手.惡魔之怒)) != null) {
//                    this.bossDamageR += skillEffect490.getBossDamage();
//                }
//                final MapleStatEffect skillEffect491;
//                if ((skillEffect491 = chra.getSkillEffect(惡魔殺手.黑暗敏捷)) != null) {
//                    this.jump += skillEffect491.getPsdJump();
//                    this.speed += skillEffect491.getPsdSpeed();
//                }
//                bx = SkillFactory.getSkill(惡魔殺手.HP增加); //HP增加 - 最大HP永久增加
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    incMaxHPR += bx.getEffect(bof).getMhpR();
//                }
//                bx = SkillFactory.getSkill(惡魔殺手.變形); //變形 - 被動效果 : 最大HP #mhpR%增加]
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    incMaxHPR += bx.getEffect(bof).getMhpR();
//                }
//                bx = SkillFactory.getSkill(惡魔殺手.體能訓練); //物理訓練 - 永久性地增加力量和敏捷
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                final MapleStatEffect skillEffect495;
//                if ((skillEffect495 = chra.getSkillEffect(惡魔殺手.憤怒)) != null) {
//                    this.pad += skillEffect495.getPadX();
//                    this.critRate += skillEffect495.getCritical();
//                }
//                final MapleStatEffect skillEffect497;
//                if ((skillEffect497 = chra.getSkillEffect(31110004)) != null) {
//                    this.percent_wdef += skillEffect497.getPddR();
//                    this.asrR += skillEffect497.getASRRate();
//                    this.terR += skillEffect497.getTERRate();
//                }
//                bx = SkillFactory.getSkill(惡魔殺手.精神集中); //31110007 - 精神集中 - 通過集中精神，永久性地增加傷害，使攻擊速度提高1個階段
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    incDamR += bx.getEffect(bof).getDamR();
//                }
//                final MapleStatEffect skillEffect499;
//                if ((skillEffect499 = chra.getSkillEffect(惡魔殺手.強化惡魔之力)) != null) {
//                    this.recoverMP += skillEffect499.getY();
//                }
//                bx = SkillFactory.getSkill(惡魔殺手.黑暗拘束); //黑暗束縛 - 有一定概率使周圍的多個敵人陷入無法行動的狀態，造成持續傷害。擁有一定概率無視怪物防禦力的被動效果，黑暗束縛的效果對BOSS同樣有效
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpR(bx.getEffect(bof).getIgnoreMobpdpR());
//                }
//                bx = SkillFactory.getSkill(惡魔殺手.堅硬肌膚); //未知 難道是皮膚硬化 - 永久性地強化身體，減少敵人造成的傷害
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addDamAbsorbShieldR(bx.getEffect(bof).getT());
//                }
//                bx = SkillFactory.getSkill(惡魔殺手.進階武器精通); //進階精準武器 - 將單手鈍器、單手斧系列武器的熟練度提高到極限，增加爆擊最小傷害和物理攻擊力。\n需要技能：#c精準武器20級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX();
//                    trueMastery += eff.getMastery();
//                    criticalDamage += eff.getCriticalMin();
//                }
//                bx = SkillFactory.getSkill(惡魔殺手.高貴血統); //31121054 - 藍血 - 喚醒自己血液中的貴族血統，讓力量覺醒。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    reduceForceR += bx.getEffect(bof).getReduceForceR();
//                    incMaxDamage += bx.getEffect(bof).getMaxDamageOver();
//                }
//                final MapleStatEffect skillEffect507;
//                if ((skillEffect507 = chra.getSkillEffect(惡魔殺手.變形_力量常駐)) != null) {
//                    addSkillReduceForceCon(惡魔殺手.變形, skillEffect507.getReduceForceR());
//                }
//                if ((eff = chra.getSkillEffect(惡魔殺手.惡魔衝擊_力量常駐)) != null) {
//                    addSkillReduceForceCon(惡魔殺手.惡魔衝擊, eff.getReduceForceR());
//                }
//                bx = SkillFactory.getSkill(惡魔殺手.惡魔狂斬_強化傷害); //31120044 - 惡魔呼吸-額外攻擊 - 增加惡魔呼吸的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(惡魔殺手.惡魔佈雷斯, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(惡魔殺手.惡魔衝擊_攻擊加成); //31120050 - 惡魔衝擊波-額外攻擊 - 增加惡魔衝擊波的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(惡魔殺手.惡魔衝擊, bx.getEffect(bof).getAttackCount());
//                }
//
//                break;
//            case 3101:
//            case 3120:
//            case 3121:
//            case 3122: { //惡魔復仇者
//                final MapleStatEffect skillEffect511;
//                if ((skillEffect511 = chra.getSkillEffect(惡魔復仇者.效能提升)) != null) {
//                    this.itemRecoveryUP += skillEffect511.getX();
//                }
//                if (chra.getSkillEffect(惡魔復仇者.轉換星力) != null) {
//                    final int[] array = {0, 30, 70, 120, 180, 900};
//                    final int[] array2 = {60, 85, 110, 135, 160};
//                    int n3 = 0;
//                    do {
//                        if (array[n3] < this.getStarForce() && array[n3 + 1] >= this.getStarForce()) {
//                            break;
//                        }
//                    } while (++n3 < 5);
//                    if (n3 < 5) {
//                        this.addmaxhp += this.getStarForce() * array2[n3];
//                    }
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.狂暴鬥氣); //30010241 - 野性狂怒 - 由於憤怒，傷害增加。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incDamR += eff.getDamR();
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.生命吸收); //31210002 - 生命吸收 - 永久增加通過生命吸收吸收的HP數值。\r\n需要技能：#c生命吸收10級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    hpRecover_onAttack.put(惡魔復仇者.生命吸收, new Pair<>(eff.getX(), eff.getProp()));
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.惡魔敏捷); //31010003 - 惡魔之力 - 永久增加移動速度和最高移動速度、跳躍力、爆擊率。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    jump += eff.getPsdJump();
//                    speed += eff.getPsdSpeed();
//                    this.speedMax += eff.getSpeedMax();
//                    this.critRate += eff.getCritical();
//                }
//                final MapleStatEffect skillEffect515;
//                if ((skillEffect515 = chra.getSkillEffect(31200003)) != null) {
//                    this.pad += skillEffect515.getPadX();
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.鋼鐵意志); //31200004 - 銅牆鐵壁 - 憑借堅強的意志，使物理防禦力和魔法防禦力大幅提高。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    percent_wdef += eff.getPddR(); //增加物防
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.潛在力量);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addmaxhp += eff.getMaxHpX();
//                    this.wdef += eff.getPddX();
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.魔劍精通); //31200005 - 亡命劍精通 - 永久增加亡命劍的熟練度和命中值。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.潛在力量); //31200006 - 心靈之力 - 永久增加力量和防禦力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addmaxhp += eff.getMaxHpX();
//                    wdef += eff.getPddX();
//                }
//                final MapleStatEffect skillEffect520;
//                if ((skillEffect520 = chra.getSkillEffect(31220004)) != null) {
//                    this.incDamR += skillEffect520.getDamR();
//                }
//                final MapleStatEffect skillEffect521;
//                if ((skillEffect521 = chra.getSkillEffect(惡魔復仇者.進階魔劍精通)) != null) {
//                    this.pad += skillEffect521.getPadX();
//                    this.criticalDamage += skillEffect521.getCriticalDamage();
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.楓葉祝福);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR();
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.強化超越);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    finalAttackSkill = 惡魔復仇者.強化超越;
//                    if (chra.getSkillLevel(惡魔復仇者.超越_機率提升) > 0) {
//                        addSkillProp(惡魔復仇者.強化超越, 10);
//                    }
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.進階生命吸收); //31210006 - 進階生命吸收 - 永久增加通過生命吸收吸收的HP數值。\r\n需要技能：#c生命吸收10級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    hpRecover_onAttack.put(惡魔復仇者.生命吸收, new Pair<>(eff.getX(), 100));
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.防衛技術); //31220005 - 防禦專精 - 永久提高使用盾牌的能力，有一定概率無視敵人的防禦力，提高盾牌技能#c持盾突擊#和#c追擊盾#的攻擊力。盾牌技能的防禦力無視比例增加2倍。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(惡魔復仇者.盾牌追擊_追加目標); //31220050 - 追擊盾-額外目標 - 永久增加追擊盾攻擊的怪物數量。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(惡魔復仇者.盾牌追擊, bx.getEffect(bof).getZ());
//                    addSkillAttackCount(惡魔復仇者.盾牌追擊_攻擊, bx.getEffect(bof).getZ());
//                }
//                bx = SkillFactory.getSkill(惡魔復仇者.超越_超載解放);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    maxBeyondLoad = 18;
//                }
//                break;
//            }
            case 3200: //幻靈
            case 3210:
            case 3211:
            case 3212: {
                bx = SkillFactory.getSkill(煉獄巫師.長杖精通_2轉); //32100006 - 長杖精通 - 長杖系列武器熟練度#mastery%，魔法攻擊力增加#x，爆擊機率增加#cr%。
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0 && bx != null) {
                    eff = bx.getEffect(bof);
                    mad += eff.getX();
                }
//                bx = SkillFactory.getSkill(煉獄巫師.紅色光環); //32101009 - 紅色光環 - [被動效果：每4秒恢復最大HP的#y%+#hp，每當消滅敵人時恢復最大HP的#killRecoveryR%]
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    recoverHP += eff.getHp();
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.智慧昇華); //32100007 - 智慧昇華 - 永久增加智力 #intX。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localint += eff.getIntX();
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.普通轉換); //32100008 - 普通轉化 - 增加最大 HP #mhpR%。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR();
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.藍色光環); //32101009 - 藍色光環 - [被動效果 : 狀態異常耐性增加#asrR、所有屬性耐性增加#terR%、防禦力增加#pddR%、被擊傷害減少#ignoreMobDamR%]
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                    percent_wdef += eff.getPddR();
//                    addDamAbsorbShieldR(eff.getIgnoreMobDamR());
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.戰鬥精通); //32110001 - 戰鬥精通 - 最終傷害提升#mdR%、爆擊傷害提升#criticaldamage%。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incFinalDamage += eff.getMdR();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.格擋); //32110018 - 格擋 - 格擋機率增加 #stanceProp%。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    stanceProp += eff.getStanceProp();
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.神經刺激); //32110019 - 神經刺激 - 增加迴避率#er%、移動速度#psdSpeed、最大移動速度#speedMax
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    dodgeChance += eff.getER();
//                    speed += eff.getPsdSpeed();
//                    speedMax += eff.getSpeedMax();
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.黑色光環); //32121017 - 黑色光環 - [被動效果：魔力增加#madR%]
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMadR += eff.getMadR();
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.煉獄鬥氣); //32121010 - 煉獄鬥氣 - [被動效果 : 最大HP增加#mhpR%、最大MP增加#mmpR%]
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR(); //最大Hp增加
//                    incMaxMPR += eff.getMmpR(); //最大Mp增加
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.長杖專家); //32120016 - 長杖專家 - 長杖熟練度增加量提升為#mastery%、魔力提升#x、爆擊傷害提升#criticaldamage%
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    trueMastery += eff.getMastery();
//                    mad += eff.getX();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.技能加速); //32120020 - 技能加速 - 增加最終傷害#mdR%、魔力#madR%、傷害#damR%、怪物防禦率無視#ignoreMobpdpR%
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incFinalDamage += eff.getMdR();
//                    incMadR += eff.getMadR();
//                    incDamR += eff.getDamR();
//                    ignoreMobpdpR += eff.getIgnoreMobpdpR();
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(煉獄巫師.黑暗世紀_冷卻減免); //32120057 - 黑暗創世-縮短冷卻時間 - 減少黑暗創世的冷卻時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillCooltimeReduce(煉獄巫師.黑暗世紀, bx.getEffect(bof).getCooltimeReduceR());
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.魔法屏障_冷卻減免); //32120063 - 避難所-縮短冷卻時間 - 減少避難所的冷卻時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillCooltimeReduce(煉獄巫師.魔法屏障, bx.getEffect(bof).getCooltimeReduceR());
//                }
//                bx = SkillFactory.getSkill(煉獄巫師.魔法屏障_時間持續); //32120064 - 避難所-堅持 - 提升避難所的持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(煉獄巫師.魔法屏障, bx.getEffect(bof).getDuration());
//                }
                break;
            }
//
//            case 3300:
//            case 3310:
//            case 3311:
//            case 3312: {
//                bx = SkillFactory.getSkill(狂豹獵人.召喚美洲豹_銀灰);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    incPadR += Math.min(180, chra.getLevel());
//                    addSkillDamageIncrease(狂豹獵人.召喚美洲豹_銀灰, Math.min(180, chra.getLevel()));
//                    addSkillDamageIncrease(狂豹獵人.召喚美洲豹_暗黃, Math.min(180, chra.getLevel()));
//                    addSkillDamageIncrease(狂豹獵人.召喚美洲豹_血紅, Math.min(180, chra.getLevel()));
//                    addSkillDamageIncrease(狂豹獵人.召喚美洲豹_紫光, Math.min(180, chra.getLevel()));
//                    addSkillDamageIncrease(狂豹獵人.召喚美洲豹_深藍, Math.min(180, chra.getLevel()));
//                    addSkillDamageIncrease(狂豹獵人.召喚美洲豹_傑拉, Math.min(180, chra.getLevel()));
//                    addSkillDamageIncrease(狂豹獵人.召喚美洲豹_白雪, Math.min(180, chra.getLevel()));
//                    addSkillDamageIncrease(狂豹獵人.召喚美洲豹_歐尼斯, Math.min(180, chra.getLevel()));
//                    addSkillDamageIncrease(狂豹獵人.召喚美洲豹_地獄裝甲, Math.min(180, chra.getLevel()));
//                }
//                eff = SkillFactory.getSkill(狂豹獵人.爪攻擊).getEffect(1);
//                if (eff != null) {
//                    addSkillDamageIncrease(狂豹獵人.爪攻擊, eff.getDamage() + Math.max(180, chra.getLevel()));
//                }
//                eff = SkillFactory.getSkill(狂豹獵人.歧路).getEffect(1);
//                if (eff != null) {
//                    addSkillDamageIncrease(狂豹獵人.歧路, eff.getDamage() + Math.max(180, chra.getLevel()) * 2);
//                }
//
//                bx = SkillFactory.getSkill(狂豹獵人.自動射擊設備); //33000005 - 自動射擊裝置 - 將反抗者的技術和弩弓結合，可以更快、更簡單地發射箭矢。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX(); //提升攻擊力
//                    percent_acc += eff.getPercentAcc(); //提升x%的命中
//                }
//                bx = SkillFactory.getSkill(狂豹獵人.體能訓練); //33100010 - 物理訓練 - 通過鍛煉身體，永久性地提高力量和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                bx = SkillFactory.getSkill(狂豹獵人.美洲豹精通); //33120000 - 神弩手 - 弩系列武器的熟練度和物理攻擊力、爆擊最小傷害。\n前置技能：#c精準弩10級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    incMaxHPR += bx.getEffect(bof).getMhpR();
//                }
//                bx = SkillFactory.getSkill(狂豹獵人.美洲豹連接);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPad() * eff.getX();
//                    critRate += eff.getY() * eff.getX();
//                    criticalDamage += 2 * eff.getX();
//                }
//                bx = SkillFactory.getSkill(狂豹獵人.狂獸附體); //33120000 - 神弩手 - 弩系列武器的熟練度和物理攻擊力、爆擊最小傷害。\n前置技能：#c精準弩10級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    incMaxHPR += bx.getEffect(bof).getMhpR();
//                }
//                bx = SkillFactory.getSkill(狂豹獵人.弩術精通); //33120000 - 神弩手 - 弩系列武器的熟練度和物理攻擊力、爆擊最小傷害。\n前置技能：#c精準弩10級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getX();
//                    trueMastery += eff.getMastery();
//                    criticalDamage += eff.getCriticalMin();
//                }
//                bx = SkillFactory.getSkill(狂豹獵人.狂暴天性); //33120010 - 野性本能 - 攻擊時無視怪物的部分物理防禦力，有一定概率迴避敵人的攻擊。永久增加所有屬性抗性。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                    dodgeChance += eff.getER();
//                }
//                bx = SkillFactory.getSkill(狂豹獵人.進階終極攻擊); //33120011 - 進階終極攻擊 - 永久性地增加攻擊力和命中率，終極弓弩技能的發動概率和傷害大幅上升。\n需要技能：#c終極弓弩20級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX();
//                    addSkillDamageIncrease(狂豹獵人.終極攻擊, eff.getDamage());
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(狂豹獵人.狂野機關砲_無視防禦); //33120051 - 奧義箭亂舞-無視防禦 - 增加奧義箭亂舞的無視防禦力效果。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpRate(狂豹獵人.狂野機關砲, bx.getEffect(bof).getIgnoreMobpdpR());
//                }
//                break;
//            }
//            case 3500:
//            case 3510:
//            case 3511:
//            case 3512: {
//                bx = SkillFactory.getSkill(機甲戰神.合金盔甲_人型);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addmaxhp += eff.getMaxHpX();
//                    addmaxmp += eff.getMaxMpX();
//                }
//                bx = SkillFactory.getSkill(機甲戰神.體能訓練); //物理訓練 - 通過身體鍛煉，永久性地提高力量和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                bx = SkillFactory.getSkill(機甲戰神.機甲戰神精通); //機械精通
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    pad += bx.getEffect(bof).getPadX();
//                }
//                bx = SkillFactory.getSkill(機甲戰神.機甲防禦系統); //機械精通
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    incMaxHPR += bx.getEffect(bof).getMhpR();
//                    incMaxMPR += bx.getEffect(bof).getMmpR();
//                }
//                bx = SkillFactory.getSkill(機甲戰神.機器人精通); //機器人精通 - 提高所有召喚機器人的攻擊力、自爆傷害和持續時間
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addSkillDamageIncrease(機甲戰神.機器人工廠_RM1, eff.getX());
//                    BuffUP_Summon += eff.getY();
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(機甲戰神.磁場_持續時間); //35120044 - 磁場-堅持 - 增加磁場的持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(機甲戰神.磁場, bx.getEffect(bof).getDuration());
//                }
//                bx = SkillFactory.getSkill(機甲戰神.磁場_冷卻減免); //35120045 - 磁場-縮短冷卻時間 - 減少磁場的冷卻時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillCooltimeReduce(機甲戰神.磁場, bx.getEffect(bof).getCooltimeReduceR());
//                }
//                bx = SkillFactory.getSkill(機甲戰神.輔助機器_H_EX_持續時間); //35120048 - 支援波動器：H-EX-堅持 - 增加支援波動器：H-EX的持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillCooltimeReduce(機甲戰神.輔助機器_H_EX, bx.getEffect(bof).getCooltimeReduceR());
//                }
//                bx = SkillFactory.getSkill(機甲戰神.巨型火炮_IRON_B_攻擊加成); //35120051 - 集中射擊：IRON-B-額外攻擊 - 增加集中射擊：IRON-B的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(機甲戰神.巨型火炮_IRON_B, bx.getEffect(bof).getAttackCount());
//                }
//                break;
//            }
//            case 3002: //尖兵
//            case 3600:
//            case 3610:
//            case 3611:
//            case 3612: {
//                bx = SkillFactory.getSkill(傑諾.合成邏輯); //30020233 - 混合邏輯 - 採用混合邏輯設計，所有能力值永久提高。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incStrR += eff.getStrRate();
//                    incDexR += eff.getDexR();
//                    incIntR += eff.getIntRate();
//                    incLukR += eff.getLukRate();
//                }
//                bx = SkillFactory.getSkill(傑諾.系統強化); //36000003 - 神經系統改造 - 強化脊柱的神經系統，提高和運動有關的所有數值。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    jump += eff.getPsdJump();
//                    speed += eff.getPsdSpeed();
//                    speedMax += eff.getSpeedMax();
//                }
//                if ((eff = chra.getSkillEffect(傑諾.效率管道)) != null) {
//                    incMaxHPR += eff.getMhpR();
//                    incMaxMPR += eff.getMmpR();
//                }
//                if ((eff = chra.getSkillEffect(傑諾.線性透視)) != null) {
//                    critRate += eff.getCritical();
//                }
//                bx = SkillFactory.getSkill(傑諾.少量支援); //36100005 - 精英支援 - 永久增加所有能力值
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                    localint += eff.getIntX();
//                    localluk += eff.getLukX();
//                }
//                bx = SkillFactory.getSkill(傑諾.能量劍精通); //36100006 - 尖兵精通 - 熟練度和物理攻擊力提高。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    pad += bx.getEffect(bof).getPadX();
//                }
//                bx = SkillFactory.getSkill(傑諾.全面防禦); //36111003 - 雙重防禦 - 額外迴避率100%，之後每次迴避/受到攻擊時，迴避率和受到的傷害減少。此外，防禦力永久增加。\n[需要技能]：#c直線透視5級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                    localint += eff.getIntX();
//                    localluk += eff.getLukX();
//                    wdef += eff.getPddX();
//                }
//                if ((eff = chra.getSkillEffect(傑諾.能量劍專家)) != null) {
//                    pad += eff.getPadX();
//                    criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(傑諾.攻擊矩陣)) != null) {
//                    stanceProp += eff.getStanceProp();
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(傑諾.偽裝掃蕩_防禦無視); //36120047 - 聚能脈衝炮 - 無視防禦 - 增加聚能脈衝炮的無視防禦比例
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpRate(傑諾.偽裝掃蕩_狙擊, bx.getEffect(bof).getIgnoreMobpdpR());
//                    addIgnoreMobpdpRate(傑諾.偽裝掃蕩_砲擊, bx.getEffect(bof).getIgnoreMobpdpR());
//                    addIgnoreMobpdpRate(傑諾.偽裝掃蕩_轟炸, bx.getEffect(bof).getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(傑諾.能量領域_持續效果)) != null) {
//                    addSkillDuration(傑諾.能量領域_貫通, eff.getDuration());
//                    addSkillDuration(傑諾.能量領域_力場, eff.getDuration());
//                    addSkillDuration(傑諾.能量領域_支援, eff.getDuration());
//                    return;
//                }
//                break;
//            }
//            case 3700:
//            case 3710:
//            case 3711:
//            case 3712: {
//                bx = SkillFactory.getSkill(爆拳槍神.旋轉加農砲);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0) {
//                    chra.getSpecialStat().setMaxBullet(3);
//                }
//                bx = SkillFactory.getSkill(爆拳槍神.強化旋轉加農砲); // 汽缸數值上限擴張為4格，彈丸最大上限增加為4個
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0) {
//                    chra.getSpecialStat().setMaxBullet(4);
//                }
//                bx = SkillFactory.getSkill(爆拳槍神.強化旋轉加農砲II); // 最大汽缸數值擴充為5格，最大彈丸增加為 5個
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0) {
//                    chra.getSpecialStat().setMaxBullet(5);
//                }
//                bx = SkillFactory.getSkill(爆拳槍神.強化旋轉加農砲III); // 最多汽缸數值6格來擴展，最多子彈增加6個
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0) {
//                    chra.getSpecialStat().setMaxBullet(6);
//                }
//                bx = SkillFactory.getSkill(爆拳槍神.護腕精通_2轉);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0) {
//                    pad += bx.getEffect(bof).getPadX();
//                }
//                bx = SkillFactory.getSkill(爆拳槍神.續航防盾);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0) {
//                    incMaxHPR += bx.getEffect(bof).getMhpR();
//                }
//                bx = SkillFactory.getSkill(爆拳槍神.衝撞精通);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0) {
//                    percent_damage += bx.getEffect(bof).getDamR();
//                }
//                bx = SkillFactory.getSkill(爆拳槍神.屬性強化精通);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0) {
//                    addIgnoreMobpdpR(bx.getEffect(bof).getIgnoreMobpdpR());
//                }
//                break;
//            }
//            case 4100: //劍豪
//            case 4110:
//            case 4111:
//            case 4112: {
//                if (chra.getJob() >= 4111) {
//                    bx = SkillFactory.getSkill(劍豪.迅速);
//                    bof = chra.getTotalSkillLevel(bx);
//                    if (bof > 0 && bx != null) {
//                        eff = bx.getEffect(bof);
//                        dodgeChance += eff.getPercentAvoid();
//                    }
//                }
//                if (chra.getJob() >= 4110) {
//                    bx = SkillFactory.getSkill(劍豪.秘劍_斑鳩);
//                    bof = chra.getTotalSkillLevel(bx);
//                    if (bof > 0 && bx != null) {
//                        eff = bx.getEffect(bof);
//                        criticalDamage += eff.getCriticalMax();
//                        criticalDamage += eff.getCriticalMin();
//                    }
//                }
//                if (chra.getJob() >= 4100) {
//                    bx = SkillFactory.getSkill(劍豪.劍豪道);
//                    bof = chra.getTotalSkillLevel(bx);
//                    if (bof > 0 && bx != null) {
//                        eff = bx.getEffect(bof);
//                        localstr += eff.getStrX();
//                        localdex += eff.getDexX();
//                    }
//                }
//                if ((eff = chra.getSkillEffect(劍豪.武神招來)) != null) {
//                    incMaxHPR += eff.getMhpR();
//                    incMaxMPR += eff.getMmpR();
//                    pad += eff.getPadX();
//                    speed += eff.getPsdSpeed();
//                    jump += eff.getPsdJump();
//                }
//                bx = SkillFactory.getSkill(劍豪.神速無雙_次數強化);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addSkillAttackCount(劍豪.神速無雙, eff.getAttackCount());
//                }
//                bx = SkillFactory.getSkill(劍豪.瞬殺斬_次數強化);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addSkillAttackCount(劍豪.瞬殺斬, eff.getAttackCount());
//                }
//                bx = SkillFactory.getSkill(劍豪.一閃_次數強化);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addSkillAttackCount(劍豪.一閃, eff.getAttackCount());
//                }
//                bx = SkillFactory.getSkill(劍豪.一閃_減少冷卻時間);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillCooltimeReduce(劍豪.一閃, bx.getEffect(bof).getCooltimeReduceR());
//                }
//                break;
//            }
//            case 4200: // 陰陽師
//            case 4210:
//            case 4211:
//            case 4212:
//                if ((eff = chra.getSkillEffect(陰陽師.五行的陰陽師)) != null) {
//                    incMaxHPR += eff.getMhpR();
//                    mad += (chra.getStat().getCurrentMaxHP() / 700);
//                    addDamAbsorbShieldR(eff.getX());
//                }
//                if ((eff = chra.getSkillEffect(陰陽師.結界_櫻_速靈)) != null) {
//                    addSkillCooltimeReduce(陰陽師.結界_櫻, eff.getCooltimeReduceR());
//                }
//                if ((eff = chra.getSkillEffect(陰陽師.結界_鈴蘭_速靈)) != null) {
//                    addSkillCooltimeReduce(陰陽師.結界_鈴蘭, eff.getCooltimeReduceR());
//                }
//                break;
//            case 5100: //米哈爾
//            case 5110:
//            case 5111:
//            case 5112: {
//                bx = SkillFactory.getSkill(米哈逸.靈魂盾牌); //51000001 - 靈魂盾 - 提升靈魂盾的力量，提高防禦力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    percent_wdef += eff.getPddX();
//                    percent_mdef += eff.getMdefX();
//                }
//                bx = SkillFactory.getSkill(米哈逸.靈魂迅捷); //51000002 - 靈魂敏捷 - 永久提升命中值和移動速度、跳躍力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    jump += eff.getPsdJump();
//                    speed += eff.getPsdSpeed();
//                }
//                bx = SkillFactory.getSkill(米哈逸.增加HP); //51000000 - 增加HP - 永久增加最大HP。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    incMaxHPR += bx.getEffect(bof).getMhpR();
//                }
//                bx = SkillFactory.getSkill(米哈逸.體能訓練); //51100000 - 物理訓練 - 通過鍛煉身體，永久提升力量和敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    localdex += eff.getDexX();
//                }
//                bx = SkillFactory.getSkill(米哈逸.魔力恢復);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    recoverHP += eff.getHp();
//                    recoverMP += eff.getMp();
//                }
//                bx = SkillFactory.getSkill(米哈逸.癒合); //51110001 - 專注 - 集中精神，永久增加力量，攻擊力提升1階段。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                }
//                bx = SkillFactory.getSkill(米哈逸.戰鬥大師); //51120000 - 戰鬥精通 - 攻擊時無視一定程度的怪物防禦力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpR(bx.getEffect(bof).getIgnoreMobpdpR());
//                }
//                bx = SkillFactory.getSkill(米哈逸.進階終極攻擊); //51120002 - 進階終結攻擊 - 永久增加攻擊力和命中率，終結攻擊的發動概率和傷害值大幅提升。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX();
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(米哈逸.靈魂抗性_持續效果);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(米哈逸.靈魂抗性, bx.getEffect(bof).getDuration());
//                }
//                bx = SkillFactory.getSkill(米哈逸.靈魂突擊_獎勵加成); //51120051 - 靈魂抨擊-額外攻擊 - 增加靈魂抨擊的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(米哈逸.靈魂突擊, bx.getEffect(bof).getAttackCount());
//                }
//                break;
//            }
//            case 6000: //凱撒
//            case 6100:
//            case 6110:
//            case 6111:
//            case 6112: {
//                bx = SkillFactory.getSkill(凱撒.鋼鐵意志); //60000222 - 鋼鐵之牆 - 具備鋼鐵意志的凱撒獲得額外體力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    incMaxHPR += bx.getEffect(bof).getMhpR();
//                }
//                bx = SkillFactory.getSkill(凱撒.鎧甲保護); //61000003 - 皮膚保護 - 強化皮膚，永久提升防禦力，有一定概率進入不被擊退的狀態。和變身的穩如泰山效果重疊。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    percent_wdef += eff.getPddX();
//                    percent_mdef += eff.getMdefX();
//                }
//                bx = SkillFactory.getSkill(凱撒.龍旋); //61001002 - 雙重跳躍 - 跳躍中再次跳躍一次後，移動至遠處。額外的永久增加移動速度和最大移動速度。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    speed += bx.getEffect(bof).getPsdSpeed();
//                }
//                bx = SkillFactory.getSkill(凱撒.戰力強化); //61100007 - 內心火焰 - 永久提升力量和HP。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    incMaxHPR += eff.getMhpR();
//                }
//                bx = SkillFactory.getSkill(凱撒.進階戰力強化); //61110007 - 進階內心火焰 - 永久提升力量和HP。\n前置技能：#c內心火焰10級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localstr += eff.getStrX();
//                    incMaxHPR += eff.getMhpR();
//                }
//                bx = SkillFactory.getSkill(凱撒.進階意志之劍); //61120007 - 進階劍刃之壁 - 強化劍刃之壁技能。召喚3把使用中的劍。召喚出的劍畫出一道軌跡，尋找並攻擊怪物。額外的永久提升攻擊力。\n前置技能：#c劍刃之壁20級以上#
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    pad += bx.getEffect(bof).getPadX();
//                }
//                bx = SkillFactory.getSkill(凱撒.勇氣); //61120011 - 無敵之勇 - 無視怪物的防禦。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addIgnoreMobpdpR(bx.getEffect(bof).getIgnoreMobpdpR());
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(凱撒.藍焰恐懼_持續); //61120044 - 怒雷屠龍斬-堅持 - 提升怒雷屠龍斬的減速持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(凱撒.藍焰恐懼, bx.getEffect(bof).getDuration());
//                    addSkillDuration(凱撒.藍焰恐懼_變身, bx.getEffect(bof).getDuration());
//                }
//                bx = SkillFactory.getSkill(凱撒.藍焰恐懼_加碼攻擊); //61120045 - 怒雷屠龍斬-額外攻擊 - 提升怒雷屠龍斬的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(凱撒.藍焰恐懼, bx.getEffect(bof).getAttackCount());
//                    addSkillAttackCount(凱撒.藍焰恐懼_變身, bx.getEffect(bof).getAttackCount());
//                }
//                bx = SkillFactory.getSkill(凱撒.龍烈焰_持續擠壓); //61120047 - 惡魔之息-暴怒堅持 - 提升惡魔之息產生的火焰持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(凱撒.龍烈焰_暴怒, bx.getEffect(bof).getDuration());
//                }
//                bx = SkillFactory.getSkill(凱撒.龍劍風_持續); //61120050 - 扇擊-堅持 - 提升扇擊的持續時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillDuration(凱撒.龍劍風, bx.getEffect(bof).getDuration());
//                    addSkillDuration(凱撒.展翅飛翔, bx.getEffect(bof).getDuration());
//                    addSkillDuration(凱撒.龍劍風_變身, bx.getEffect(bof).getDuration());
//                }
//                bx = SkillFactory.getSkill(凱撒.龍劍風_額外攻擊); //61120051 - 扇擊-額外攻擊 - 提升扇擊的攻擊次數。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillAttackCount(凱撒.龍劍風, bx.getEffect(bof).getAttackCount());
//                    addSkillAttackCount(凱撒.展翅飛翔, bx.getEffect(bof).getAttackCount());
//                    addSkillAttackCount(凱撒.龍劍風_變身, bx.getEffect(bof).getAttackCount());
//                }
//                break;
//            }
//            case 6003: //凱殷
//            case 6300:
//            case 6310:
//            case 6311:
//            case 6312: {
//                bx = SkillFactory.getSkill(凱殷.殺手);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR();
//                }
//                bx = SkillFactory.getSkill(凱殷.研磨);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxMPR += eff.getMmpR();
//                }
//                bx = SkillFactory.getSkill(凱殷.適應死亡);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR();
//                }
//                break;
//            }
//            case 6002:
//            case 6400:
//            case 6410:
//            case 6411:
//            case 6412: {
//                bx = SkillFactory.getSkill(卡蒂娜.魔鬼步伐);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    speedMax += eff.getSpeedMax();
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.收集幸運草);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localluk += eff.getLukX();
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.武器變換Ⅰ);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addSkillDamageIncrease(卡蒂娜.召喚_切割彎刀, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_切割彎刀, eff.getInfo().get(MapleStatInfo.damPlus));
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.快捷服務心態Ⅰ);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX();
//                    criticalDamage += eff.getInfo().get(MapleStatInfo.criticaldamage);
//                    critRate += eff.getCritical();
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.鍛鍊身體);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localluk += eff.getLukX();
//                    localdex += eff.getDexX();
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.武器變換Ⅱ);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addSkillDamageIncrease(卡蒂娜.召喚_切割彎刀, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_黑焰鉤爪, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_炸裂迴旋, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_炸裂迴旋_1, eff.getInfo().get(MapleStatInfo.damPlus));
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.調和);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR();
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.基礎探測);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addIgnoreMobpdpR(eff.getInfo().get(MapleStatInfo.ignoreMobpdpR));
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.武器變換Ⅲ);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addSkillDamageIncrease(卡蒂娜.召喚_切割彎刀, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_黑焰鉤爪, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_炸裂迴旋, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_炸裂迴旋_1, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_散彈射擊, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_紫焱幻刀, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_炸彈投擲_1, eff.getInfo().get(MapleStatInfo.damPlus));
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.進階武器精通);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX();
//                    criticalDamage += eff.getInfo().get(MapleStatInfo.criticaldamage);
//                    critRate += eff.getCritical();
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.快捷服務心態Ⅱ);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    pad += eff.getPadX();
//                    criticalDamage += eff.getInfo().get(MapleStatInfo.criticaldamage);
//                    critRate += eff.getCritical();
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.非鏈之藝術_強化);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addSkillDamageIncrease(卡蒂娜.召喚_黑焰鉤爪, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_炸裂迴旋, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_炸裂迴旋_1, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_散彈射擊, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_紫焱幻刀, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_炸彈投擲, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_炸彈投擲_1, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_狼牙棒, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_狼牙棒_1, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_狼牙棒_2, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_重擊磚頭, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_重擊磚頭_1, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_重擊磚頭_2, eff.getInfo().get(MapleStatInfo.damPlus));
//                    addSkillDamageIncrease(卡蒂娜.召喚_重擊磚頭_3, eff.getInfo().get(MapleStatInfo.damPlus));
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.非鏈之藝術_強化);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addSkillBossDamage(卡蒂娜.召喚_黑焰鉤爪, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_炸裂迴旋, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_炸裂迴旋_1, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_散彈射擊, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_紫焱幻刀, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_炸彈投擲, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_炸彈投擲_1, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_狼牙棒, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_狼牙棒_1, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_狼牙棒_2, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_重擊磚頭, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_重擊磚頭_1, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_重擊磚頭_2, eff.getBossDamage());
//                    addSkillBossDamage(卡蒂娜.召喚_重擊磚頭_3, eff.getBossDamage());
//                }
//                bx = SkillFactory.getSkill(卡蒂娜.鏈之藝術_束縛鎖鏈_重置冷卻時間);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addSkillCooltimeReduce(卡蒂娜.鏈之藝術_束縛鎖鏈, eff.getCooltimeReduceR());
//                }
//                break;
//            }
//            case 6001: //天使破壞者
//            case 6500:
//            case 6510:
//            case 6511:
//            case 6512: {
//                bx = SkillFactory.getSkill(天使破壞者.寧靜心靈); //65110005 - 內心平和 - 內心的平和，使得自身不會受到外界衝擊的干擾。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    wdef += eff.getPddX();
//                    addmaxhp += eff.getMaxHpX();
//                }
//                bx = SkillFactory.getSkill(天使破壞者.抒情十字); //65001002 - 火眼金睛 - 注入愛絲卡達的力量，攻擊速度提高1個階段。並且提高HP。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    addmaxhp += eff.getMaxHpX();
//                }
//                bx = SkillFactory.getSkill(天使破壞者.親和力Ⅰ); //65000003 - 親和Ⅰ - 提升與愛絲卡達的親和力，身體感到輕盈。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    jump += eff.getPsdJump();
//                    speed += eff.getPsdSpeed();
//                }
//                bx = SkillFactory.getSkill(天使破壞者.靈魂射手); //65100003 - 精準靈魂手銃 - 提升靈魂手銃的熟練度和命中值、物理攻擊力。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    pad += bx.getEffect(bof).getPadX();
//                }
//                bx = SkillFactory.getSkill(天使破壞者.內在力量); //65100004 - 內心之火 - 永久提升敏捷。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    localdex += bx.getEffect(bof).getDexX();
//                }
//                bx = SkillFactory.getSkill(天使破壞者.親和力Ⅱ); //65100005 - 親和Ⅱ - 提升與愛絲卡達的親和力，接受到戰鬥的經驗，抵抗力提升。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    asrR += eff.getASRRate();
//                    terR += eff.getTERRate();
//                }
//                bx = SkillFactory.getSkill(天使破壞者.親和力Ⅲ); //65110006 - 親和Ⅲ - 提升與愛絲卡達的親和力，提升敏捷，熟讀秘傳。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    localdex += eff.getDexX();
//                    percent_damage += eff.getDamR();
//                }
//                /*
//                 * 超級技能
//                 */
//                bx = SkillFactory.getSkill(天使破壞者.魔力綵帶_減少冷卻時間); //65120048 - 大地衝擊波-縮短冷卻時間 - 縮短大地衝擊波的冷卻時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillCooltimeReduce(天使破壞者.魔力綵帶, bx.getEffect(bof).getCooltimeReduceR());
//                }
//                bx = SkillFactory.getSkill(天使破壞者.三位一體_無視防禦); //65120050 - 靈魂共鳴-縮短冷卻時間 - 縮短靈魂共鳴的冷卻時間。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    addSkillCooltimeReduce(天使破壞者.靈魂震動, bx.getEffect(bof).getCooltimeReduceR());
//                }
//                break;
//            }
//            case 10000: //神之子
//            case 10100:
//            case 10110:
//            case 10111:
//            case 10112: {
//                if (chra.isBeta()) {
//                    bx = SkillFactory.getSkill(神之子.琉之力); //101000103 - 精準大劍 - 提高大劍系列武器的熟練度、攻擊力和攻擊速度。此外，施展大劍系列技能時，受到攻擊的敵人數量越少，傷害越強。
//                    bof = chra.getTotalSkillLevel(bx);
//                    if (bof > 0 && bx != null) {
//                        eff = bx.getEffect(bof);
//                        pad += eff.getPadX();
//                        incDamR += eff.getMobCountDamage();
//                        bossDamageR += eff.getBossDamage();
//                    }
//                    bx = SkillFactory.getSkill(神之子.堅固體魄); //101100102 - 固態身體 - 強化貝塔的身體，可以增加物理防禦力、魔法防禦力、增加狀態異常抗性、增加所有屬性抗性，同時會提高穩如泰山的幾率。
//                    bof = chra.getTotalSkillLevel(bx);
//                    if (bof > 0 && bx != null) {
//                        eff = bx.getEffect(bof);
//                        wdef += eff.getPddX();
//                        asrR += eff.getASRRate();
//                        terR += eff.getTERRate();
//                    }
//                } else {
//                    bx = SkillFactory.getSkill(神之子.璃之力); //101000203 - 精準太刀 - 提高太刀系列武器的熟練度和攻擊力。使用太刀時增加傷害、攻擊速度、移動速度和跳躍力。
//                    bof = chra.getTotalSkillLevel(bx);
//                    if (bof > 0 && bx != null) {
//                        eff = bx.getEffect(bof);
//                        pad += eff.getPadX();
//                        jump += eff.getPsdJump();
//                        speed += eff.getPsdSpeed();
//                        addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                    }
//                    bx = SkillFactory.getSkill(神之子.肉體強化); //101100203 - 強化之軀 - 強化阿爾法的身體，增加最大HP、最大時間之力和暴擊率。
//                    bof = chra.getTotalSkillLevel(bx);
//                    if (bof > 0 && bx != null) {
//                        eff = bx.getEffect(bof);
//                        incMaxHPR += eff.getMhpR();
//                        critRate += eff.getCritical(); //爆擊概率
//                    }
//                    bx = SkillFactory.getSkill(神之子.聖靈裂襲); //101120207 - 聖光照耀 - 增加阿爾法的最大和最小暴擊傷害，同時有一定的幾率給對像造成出血狀態，並恢復自己的HP。
//                    bof = chra.getTotalSkillLevel(bx);
//                    if (bof > 0 && bx != null) {
//                        eff = bx.getEffect(bof);
//                        criticalDamage += eff.getCriticalMax();
//                    }
//                }
//                bx = SkillFactory.getSkill(神之子.時之意志); //100000279 - 決意時刻 - 繼承並獲得倫娜女神的強大力量。
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    percent_damage += eff.getDamR();
//                    localstr += bx.getEffect(bof).getStrX();
//                    incMaxHPR += eff.getMhpR();
//                    speedMax += eff.getSpeedMax();
//                }
//                break;
//            }
//            case 14200:
//            case 14210:
//            case 14211:
//            case 14212: {
//                bx = SkillFactory.getSkill(凱內西斯.心靈內在1);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR();
//                }
//                bx = SkillFactory.getSkill(凱內西斯.心靈內在2);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incMaxHPR += eff.getMhpR();
//                }
//                bx = SkillFactory.getSkill(凱內西斯.精神集中_維持);
//                bof = chra.getTotalSkillLevel(bx);
//                if (bof > 0 && bx != null) {
//                    eff = bx.getEffect(bof);
//                    incBuffTime += eff.getBuffTimeRate();
//                }
//                break;
//            }
//            case 15002:
//            case 15100:
//            case 15110:
//            case 15111:
//            case 15112:
//                if ((eff = chra.getSkillEffect(阿戴爾.入門)) != null) {
//                    this.addmaxhp += eff.getMaxHpX();
//                }
//                if ((eff = chra.getSkillEffect(阿戴爾.耐性)) != null) {
//                    this.incMaxHPR += eff.getMhpR();
//                }
//                if ((eff = chra.getSkillEffect(阿戴爾.登峰造極)) != null) {
//                    this.incMaxHPR += eff.getMhpR();
//                }
//                if ((eff = chra.getSkillEffect(阿戴爾.特性培養)) != null) {
//                    this.healHPR = eff.getX();
//                    this.healMPR = eff.getX();
//                    MapleStatEffect eff2;
//                    if ((eff2 = chra.getSkillEffect(阿戴爾.特性培養_治癒強化)) != null) {
//                        this.healHPR += eff2.getX();
//                        this.healMPR += eff2.getX();
//                    }
//                    this.hpRecoverTime = eff.getY();
//                }
//                if ((eff = chra.getSkillEffect(阿戴爾.綻放_減少冷卻時間)) != null) {
//                    addSkillCooltimeReduce(阿戴爾.綻放, eff.getCooltimeReduceR());
//                }
//                break;
//            case 15000:
//            case 15200:
//            case 15210:
//            case 15211:
//            case 15212: {
//                if ((eff = chra.getSkillEffect(伊利恩.精通魔法護腕)) != null) {
//                    this.critRate += eff.getCritical();
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.祝福標誌)) != null) {
//                    this.incMaxHPR += eff.getMhpR();
//                    this.incMaxMPR += eff.getMmpR();
//                    this.incDamR += eff.getDamR();
//                    this.wdef += eff.getPddX();
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.精通雷普)) != null) {
//                    this.incMaxMPR += eff.getMmpR();
//                    this.incMaxHPR += eff.getMhpR();
//                    this.incDamR += eff.getDamR();
//                    this.jump += eff.getPsdJump();
//                    this.speed += eff.getPsdSpeed();
//                    this.speedMax += eff.getSpeedMax();
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.開拓命運)) != null) {
//                    this.localint += eff.getIntX();
//                    this.percent_damage += eff.getMadR();
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.無盡的研究)) != null) {
//                    this.mad += eff.getMadX();
//                    this.critRate += eff.getCritical();
//                    criticalDamage += eff.getInfo().get(MapleStatInfo.criticaldamage);
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.水晶的秘密)) != null) {
//                    this.bossDamageR += eff.getBossDamage();
//                    addIgnoreMobpdpR(eff.getInfo().get(MapleStatInfo.ignoreMobpdpR));
////                    this.cc += eff.getMdR() + eff.getMdR() * this.cc / 100.0;
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.水晶技能_和諧連結)) != null) {
//                    this.asrR += eff.getASRRate();
//                    this.terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.暗器_額外攻擊)) != null) {
//                    this.addSkillAttackCount(伊利恩.技藝_暗器Ⅱ, eff.getAttackCount());
//                    this.addSkillAttackCount(伊利恩.技藝_暗器Ⅱ_1, eff.getAttackCount());
//                    this.addSkillAttackCount(伊利恩.榮耀之翼_強化暗器, eff.getAttackCount());
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.暗器_BOSS殺手)) != null) {
//                    this.addSkillBossDamage(伊利恩.技藝_暗器Ⅱ, eff.getBossDamage());
//                    this.addSkillBossDamage(伊利恩.技藝_暗器Ⅱ_1, eff.getBossDamage());
//                    this.addSkillBossDamage(伊利恩.榮耀之翼_強化暗器, eff.getBossDamage());
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.朗基努斯_額外攻擊)) != null) {
//                    this.addSkillAttackCount(伊利恩.技藝_朗基努斯, eff.getAttackCount());
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.朗基努斯_重置冷卻時間)) != null) {
//                    this.addSkillCooltimeReduce(伊利恩.技藝_朗基努斯, eff.getCooltimeReduceR());
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.德烏斯_額外攻擊)) != null) {
//                    this.addSkillAttackCount(伊利恩.水晶技能_德烏斯, eff.getAttackCount());
//                    this.addSkillAttackCount(伊利恩.水晶技能_德烏斯_1, eff.getAttackCount());
//                }
//                if ((eff = chra.getSkillEffect(伊利恩.德烏斯_BOSS殺手)) != null) {
//                    this.addSkillBossDamage(伊利恩.水晶技能_德烏斯, eff.getBossDamage());
//                    this.addSkillBossDamage(伊利恩.水晶技能_德烏斯_1, eff.getBossDamage());
//                }
//                break;
//            }
//            case 15500:
//            case 15510:
//            case 15511:
//            case 15512:
//                if ((eff = chra.getSkillEffect(亞克.開始融合)) != null) {
//                    this.incMaxHPR += eff.getMhpR();
//                    this.incMaxMPR += eff.getMmpR();
//                    this.addDamAbsorbShieldR(eff.getDamAbsorbShieldR());
//                }
//                break;
//            case 16000:
//            case 16400:
//            case 16410:
//            case 16411:
//            case 16412:
//                if ((eff = chra.getSkillEffect(虎影.輕功)) != null) {
//                    this.speed += eff.getPsdSpeed();
//                    this.speedMax += eff.getSpeedMax();
//                }
//                if ((eff = chra.getSkillEffect(虎影.仙扇熟練)) != null) {
//                    this.pad += eff.getPadX();
//                }
//                if ((eff = chra.getSkillEffect(虎影.心眼)) != null) {
//                    this.critRate += eff.getCritical();
//                    this.criticalDamage += eff.getCriticalDamage();
//                }
//                if ((eff = chra.getSkillEffect(虎影.雲身)) != null) {
//                    this.jump += eff.getPsdJump();
//                    addSkillDuration(虎影.筋斗云, eff.getDuration());
//                    addSkillCustomVal(虎影.筋斗云, 17 * eff.getLevel());
//                }
//                if ((eff = chra.getSkillEffect(虎影.身體鍛鍊)) != null) {
//                    this.localluk += eff.getLukX();
//                }
//                if ((eff = chra.getSkillEffect(虎影.修羅)) != null) {
//                    this.pad += eff.getPadX();
//                    this.critRate += eff.getCritical();
//                    this.criticalDamage += eff.getCriticalDamage();
//                    this.bossDamageR += eff.getBossDamage();
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                if ((eff = chra.getSkillEffect(虎影.金剛)) != null) {
//                    this.incMaxHPR += eff.getMhpR();
//                    this.stanceProp += eff.getStanceProp();
//                    addDamAbsorbShieldR(eff.getIgnoreMobDamR());
//                }
//                if ((eff = chra.getSkillEffect(虎影.調式)) != null) {
//                    this.asrR += eff.getASRRate();
//                    this.terR += eff.getTERRate();
//                }
//                if ((eff = chra.getSkillEffect(虎影.高級仙扇熟練)) != null) {
//                    this.pad += eff.getPadX();
//                    addIncFinalDamage(eff.getPdR());
//                }
//                if ((eff = chra.getSkillEffect(虎影.得道)) != null) {
//                    this.pad += eff.getPadX();
//                    percent_damage += eff.getDamage();
//                }
//                if ((eff = chra.getSkillEffect(虎影.點睛)) != null) {
//                    this.pad += eff.getPadX();
//                    this.critRate += eff.getCritical();
//                    this.criticalDamage += eff.getCriticalDamage();
//                    addIncFinalDamage(eff.getPdR());
//                    addIgnoreMobpdpR(eff.getIgnoreMobpdpR());
//                }
//                break;
//            case 17000:
//            case 17500:
//            case 17510:
//            case 17511:
//            case 17512:
//                if ((eff = chra.getSkillEffect(墨玄.覺醒)) != null) {
//                    this.critRate += eff.getCritical();
//                    this.pad += eff.getPadX();
//                    healHPR = eff.getX();
//                    healMPR = 0;
//                    hpRecoverTime = eff.getY();
//                    mpRecoverTime = 0;
//                    if ((eff = chra.getSkillEffect(墨玄.墨玄_超技能_覺醒_額外治癒)) != null) {
//                        healHPR += eff.getX();
//                    }
//                }
//                if ((eff = chra.getSkillEffect(墨玄.墨玄_二轉_外功修練)) != null) {
//                    this.localdex += eff.getDexX();
//                    this.wdef += eff.getPddX();
//                    this.incMaxHPR += eff.getMhpR();
//                    addDamAbsorbShieldR(eff.getIgnoreMobDamR());
//                }
//                break;
        }
//        bx = SkillFactory.getSkill(惡魔殺手.後續待發);
//        bof = chra.getSkillLevel(bx);
//        if (bof > 0 && bx != null) {
//            eff = bx.getEffect(bof);
//            bossDamageR += eff.getBossDamage();
//        }
//
//        if (JobConstants.is末日反抗軍(chra.getJob())) {
//            bx = SkillFactory.getSkill(市民.效能); //效率提升 - 為了解決物資缺乏問題，學習高效使用藥水的方法。
//            bof = chra.getTotalSkillLevel(bx);
//            if (bof > 0 && bx != null) {
//                itemRecoveryUP += bx.getEffect(bof).getX() - 100;
//            }
//        }

    }

    private void handleHexaStat(MapleCharacter chra) {
        // 處理HEXA屬性核心，檢查是否等級大於260及是否學習技能 HEXA能力值(500071000)
        if (chra.getLevel() >= 260 && chra.getSkillLevel(500071000) > 0) {
            for (MapleHexaStat mhs : chra.getHexaStats().values()) {
                // 取出核心所有能力值類別
                List<Pair<Integer, Integer>> types = new ArrayList<>();
                types.add(new Pair<>(mhs.getPreset() == 1 ? mhs.getMain1() : mhs.getMain0(), mhs.getPreset() == 1 ? mhs.getMain1Lv() : mhs.getMain0Lv()));
                types.add(new Pair<>(mhs.getPreset() == 1 ? mhs.getAddit1S1() : mhs.getAddit0S1(), mhs.getPreset() == 1 ? mhs.getAddit1S1Lv() : mhs.getAddit0S1Lv()));
                types.add(new Pair<>(mhs.getPreset() == 1 ? mhs.getAddit1S2() : mhs.getAddit0S2(), mhs.getPreset() == 1 ? mhs.getAddit1S2Lv() : mhs.getAddit0S2Lv()));

                for (int t = 0; t < types.size(); t++) {
                    HexaStatCoreEntry hsce = t == 0 ? HexaFactory.getHexaMainStats(types.get(t).getLeft()) : HexaFactory.getAdditionalHexaStats(types.get(t).getLeft());
                    switch (hsce.getStat()) {
                        case "cdPerM": // 爆擊傷害
                            criticalDamage += hsce.getLevelAddStat().get(types.get(t).getRight());
                            break;
                        case "bdRPerM": // Boss傷害
                            bossDamageR += hsce.getLevelAddStat().get(types.get(t).getRight());
                            break;
                        case "ignoreMobpdpRPerM": // 無視防禦力
                            ignoreMobpdpR += hsce.getLevelAddStat().get(types.get(t).getRight());
                            break;
                        case "damRPerM": // 傷害
                            percent_damage += hsce.getLevelAddStat().get(types.get(t).getRight());
                            break;
                        case "padX": // 物理攻擊
                            pad += hsce.getLevelAddStat().get(types.get(t).getRight());
                            break;
                        case "madX": // 魔法攻擊
                            mad += hsce.getLevelAddStat().get(types.get(t).getRight());
                            break;
                        case "indieStat": { // 職業主屬性
                            // 判斷職業加主屬性
                            int j = chra.getJob();
                            // STR
                            if (JobConstants.is冒險家劍士(j) || JobConstants.is拳霸(j)
                                    || JobConstants.is重砲指揮官(j) || JobConstants.is米哈逸(j)
                                    || JobConstants.is聖魂劍士(j) || JobConstants.is閃雷悍將(j)
                                    || JobConstants.is惡魔殺手(j) || JobConstants.is狂狼勇士(j)
                                    || JobConstants.is隱月(j) || JobConstants.is凱撒(j)
                                    || JobConstants.is劍豪(j) || JobConstants.is神之子(j)
                                    || JobConstants.is皮卡啾(j) || JobConstants.is爆拳槍神(j)
                                    || JobConstants.is雪吉拉(j) || JobConstants.is阿戴爾(j) || JobConstants.is亞克(j)) {
                                localstr += hsce.getLevelAddStat().get(types.get(t).getRight()) * 100;
                                // INT
                            } else if (JobConstants.is冒險家法師(j) || JobConstants.is烈焰巫師(j)
                                    || JobConstants.is龍魔導士(j) || JobConstants.is夜光(j)
                                    || JobConstants.is陰陽師(j) || JobConstants.is煉獄巫師(j)
                                    || JobConstants.is凱內西斯(j)
                                    || JobConstants.is伊利恩(j) || JobConstants.is菈菈(j)) {
                                localint += hsce.getLevelAddStat().get(types.get(t).getRight()) * 100;
                                // LUK
                            } else if (JobConstants.is冒險家盜賊(j) || JobConstants.is暗夜行者(j)
                                    || JobConstants.is幻影俠盜(j) || JobConstants.is卡蒂娜(j)
                                    || JobConstants.is卡莉(j) || JobConstants.is虎影(j)) {
                                localluk += hsce.getLevelAddStat().get(types.get(t).getRight());
                                // DEX
                            } else if (JobConstants.is冒險家弓箭手(j) || JobConstants.is槍神(j)
                                    || JobConstants.is破風使者(j) || JobConstants.is狂豹獵人(j)
                                    || JobConstants.is機甲戰神(j) || JobConstants.is精靈遊俠(j)
                                    || JobConstants.is天使破壞者(j) || JobConstants.is墨玄(j) || JobConstants.is凱殷(j)) {
                                localdex += hsce.getLevelAddStat().get(types.get(t).getRight()) * 100;
                                // HP
                            } else if (JobConstants.is惡魔復仇者(j)) {
                                localmaxhp += hsce.getLevelAddStat().get(types.get(t).getRight()) * 2100;
                            } else if (JobConstants.is傑諾(j)) {
                                localstr += hsce.getLevelAddStat().get(types.get(t).getRight()) * 48;
                                localluk += hsce.getLevelAddStat().get(types.get(t).getRight()) * 48;
                                localdex += hsce.getLevelAddStat().get(types.get(t).getRight()) * 48;
                            } else {
                                System.out.println("尚未設定HEXA屬性核心主屬性" + j);
                            }
                            break;
                        }
                        default:
                            System.out.println("未知的Hexa屬性核心能力值:" + hsce.getStat());
                            break;
                    }
                }
            }
        }
    }

    private void handleBuffStats(MapleCharacter chra) {
        Integer buff = chra.getBuffedValue(SecondaryStat.IndiePAD);
        if (buff != null) {
            pad += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.IndieMAD);
        if (buff != null) {
            mad += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.IndiePDD);
        if (buff != null) {
            wdef += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.IndieMHP);
        if (buff != null) {
            addmaxhp += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.IndieMHPR);
        if (buff != null) {
            incMaxHPR += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.IndieMMP);
        if (buff != null) {
            addmaxmp += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.IndieMMPR);
        if (buff != null) {
            incMaxMPR += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.IndieACC);
        if (buff != null) {
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieJump)) != null) {
            jump += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieSpeed)) != null) {
            speed += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.IndieAllStat);
        if (buff != null) {
            localstr += buff;
            localdex += buff;
            localint += buff;
            localluk += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieEXP)) != null) {
            incEXPr += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieSTR)) != null) {
            localstr += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieDEX)) != null) {
            localdex += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieINT)) != null) {
            localint += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieLUK)) != null) {
            localluk += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieDamR)) != null) {
            incDamR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieAsrR)) != null) {
            asrR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieTerR)) != null) {
            terR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieCr)) != null) {
            critRate += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieCD)) != null) {
            criticalDamage += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndiePDDR)) != null) {
            percent_wdef += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.DDR)) != null) {
            percent_wdef += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieBDR)) != null) {
            bossDamageR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieStance)) != null) {
            stanceProp += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieIgnoreMobpdpR)) != null) {
            addIgnoreMobpdpR(buff);
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndiePADR)) != null) {
            incPadR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.DamR)) != null) {
            incDamR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndieMADR)) != null) {
            incMadR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IndiePMdR)) != null) {
            percent_damage += buff;
        }

        buff = chra.getBuffedValue(SecondaryStat.PAD);
        if (buff != null) {
            pad += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.MAD);
        if (buff != null) {
            mad += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.PDD);
        if (buff != null) {
            wdef += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.Jump);
        if (buff != null) {
            jump += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.Speed);
        if (buff != null) {
            speed += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.MaxHP);
        if (buff != null) {
            incMaxHPR += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.MaxMP);
        if (buff != null) {
            incMaxMPR += buff;
        }
        buff = chra.getBuffedSkill_Y(SecondaryStat.DarkSight);
        if (buff != null) {
            incDamR += buff;
            bossDamageR += buff;
        }


        if ((buff = chra.getBuffedValue(SecondaryStat.STR)) != null) {
            localstr += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.DEX)) != null) {
            localdex += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.INT)) != null) {
            localint += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.LUK)) != null) {
            localluk += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.ComboCounter);
        MapleStatEffect effect = chra.getEffectForBuffStat(SecondaryStat.ComboCounter);
        if (buff != null && effect != null) {
            pad += (buff - 1) * effect.getY();
            bossDamageR += (buff - 1) * getSkillBossDamage(英雄.鬥氣集中);
            int skillLevel = chra.getSkillLevel(英雄.進階鬥氣);
            if (skillLevel > 0) {
                effect = SkillFactory.getSkill(英雄.進階鬥氣).getEffect(skillLevel);
                double n = (buff - 1) * (effect.getV() + (chra.getSkillLevel(英雄.進階鬥氣_強化傷害) > 0 ? 2 : 0));
                percent_damage += n + n * effect.getV() / 100.0;
            } else if ((skillLevel = chra.getSkillLevel(英雄.鬥氣綜合)) > 0) {
                effect = SkillFactory.getSkill(英雄.鬥氣綜合).getEffect(skillLevel);
                int n = (buff - 1) * (effect.getDamR() + (chra.getSkillLevel(英雄.進階鬥氣_強化傷害) > 0 ? 2 : 0));
                percent_damage += n + n * effect.getDamR() / 100.0;
            }
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.HolySymbol)) != null) {
            incEXPr = buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.BasicStatUp)) != null) {
            final double value = buff / 100.0;
            localstr += (int) (str * value);
            localdex += (int) (dex * value);
            localint += (int) (int_ * value);
            localluk += (int) (luk * value);
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.Stance)) != null) {
            stanceProp += buff;
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.Enrage)) != null) {
            switch (effect.getSourceId()) {
                case 英雄.鬥氣爆發: {
                    incFinalDamage += effect.getX();
                    break;
                }
                case 煉獄巫師.煉獄鬥氣: {
                    incDamR += effect.getX();
                    break;
                }
                case 米哈逸.Switch_開關_靈魂之怒: {
                    percent_damage += effect.getX() + percent_damage * effect.getX() / 100.0;
                    break;
                }
            }
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.EnrageCrDamMin)) != null) {
            criticalDamage += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.AsrR)) != null) {
            asrR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.TerR)) != null) {
            terR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.CombatOrders)) != null) {
            combatOrders += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.ElementalCharge);
        effect = chra.getEffectForBuffStat(SecondaryStat.ElementalCharge);
        if (buff != null && effect != null) {
            final int value = buff / effect.getX();
            pad += effect.getY() * value;
            asrR += 2 * value;
            incDamR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.BlessingArmorIncPAD)) != null) {
            pad += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.IgnoreTargetDEF);
        effect = chra.getEffectForBuffStat(SecondaryStat.IgnoreTargetDEF);
        if (buff != null && effect != null) {
            addIgnoreMobpdpR(buff);
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.CrossOverChain)) != null) {
            percent_damage += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.EMHP)) != null) {
            addmaxhp += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.EMMP)) != null) {
            addmaxmp += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.EPAD)) != null) {
            pad += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.EMAD)) != null) {
            mad += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.EPDD)) != null) {
            wdef += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.DamAbsorbShield)) != null) {
            addDamAbsorbShieldR(buff);
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.DotBasedBuff)) != null) {
            incFinalDamage += buff * ((chra.getJob() == 212) ? 5 : 3);
        }
        buff = chra.getBuffedValue(SecondaryStat.ArcaneAim);
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.ArcaneAim)) != null && buff != null) {
            incDamR += buff * effect.getX();
        }
        buff = chra.getBuffedValue(SecondaryStat.Infinity);
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.Infinity)) != null && buff != null) {
            incFinalDamage += (buff - 1) * effect.getDamage();
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.ElementalReset)) != null) {
            ignoreElement += buff;
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.AdvancedBless)) != null) {
            pad += effect.getX();
            mad += effect.getY();
            wdef += effect.getZ();
            mpconReduce += effect.getMPConReduce();
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.Bless)) != null) {
            pad += effect.getX();
            mad += effect.getY();
            wdef += effect.getZ();
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.VengeanceOfAngel)) != null) {
            ignoreElement += effect.getW();
            addSkillAttackCount(主教.天使之箭, effect.getY());
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.BlessEnsenble)) != null) {
            incFinalDamage += buff;
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.Preparation)) != null) {
            bossDamageR += effect.getY();
            stanceProp += effect.getX();
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.SharpEyes)) != null) {
            criticalDamage += (buff & 0xFF);
            critRate += (buff >> 8 & 0xFF);
        }
        buff = chra.getBuffedValue(SecondaryStat.BowMasterConcentration);
        effect = chra.getEffectForBuffStat(SecondaryStat.BowMasterConcentration);
        if (buff != null && effect != null) {
            asrR += buff * effect.getX();
        }
        buff = chra.getBuffedZ(SecondaryStat.HitStackDamR);
        effect = chra.getEffectForBuffStat(SecondaryStat.HitStackDamR);
        if (buff != null && effect != null) {
            incDamR += buff * effect.getX();
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.ConvertAD)) != null) {
            pad += equipstats.weaponAttack * buff / 100;
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.DarkSight)) != null) {
            if (chra.getSkillLevel(4210015) > 0 && JobConstants.is暗影神偷(chra.getJob())) {
                incFinalDamage += 5.0;
            }
            final MapleStatEffect skillEffect;
            if ((skillEffect = chra.getSkillEffect(影武者.進階隱身術)) != null) {
                incFinalDamage += skillEffect.getY();
            }
            if (effect.getSourceId() == 盜賊.終極隱身術) {
                incFinalDamage += effect.getY() + effect.getY() * incFinalDamage / 100.0;
            }
        }
        buff = chra.getBuffedValue(SecondaryStat.CriticalGrowing);
        int buffedIntZ = chra.getBuffedIntZ(SecondaryStat.CriticalGrowing);
        if (buff != null) {
            critRate += buff;
            criticalDamage += buffedIntZ;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.Asura)) != null) {
            addIgnoreMobpdpR(buff);
        }
        effect = chra.getEffectForBuffStat(SecondaryStat.Dice);
        buff = chra.getBuffedValue(SecondaryStat.Dice);
        if (effect != null && buff != null) {
            boolean b = false;
            final int[] array;
            if (buff >= 100) {
                b = true;
                array = new int[]{buff / 100};
            } else if (buff >= 10) {
                array = new int[]{buff / 10, buff % 10};
            } else {
                array = new int[]{buff};
            }
            for (int length = array.length, i = 0; i < length; ++i) {
                switch (array[i]) {
                    case 2: {
                        percent_wdef += effect.getPddR() * (b ? 2 : 1);
                        break;
                    }
                    case 3: {
                        incMaxHPR += effect.getMhpR() * (b ? 2 : 1);
                        incMaxMPR += effect.getMmpR() * (b ? 2 : 1);
                        break;
                    }
                    case 4: {
                        critRate += effect.getCritical() * (b ? 2 : 1);
                        break;
                    }
                    case 5: {
                        incDamR += effect.getDamR() * (b ? 2 : 1);
                        break;
                    }
                    case 6: {
                        incEXPr += effect.getExpR() * (b ? 2 : 1);
                        break;
                    }
                    case 7: {
                        addIgnoreMobpdpR(effect.getW() * (b ? 2 : 1));
                        break;
                    }
                }
            }
        }
        if (chra.getBuffedIntValue(SecondaryStat.Roulette) == 1) {
            finalAttackSkill = 重砲指揮官.幸運木桶_1;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.QuickDraw)) != null && buff > 1) {
            incDamR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.DEXR)) != null) {
            indieDexFX += buff * dex / 100.0;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.CriticalBuff)) != null) {
            critRate += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IncMaxHP)) != null) {
            addmaxhp += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.IncMaxMP)) != null) {
            indieMmpFX += buff;
        }
        if (chra.getBuffedValue(SecondaryStat.StrikerHyperElectric) != null) {
            addSkillCooltimeReduce(閃雷悍將.疾風, 100);
            addSkillCooltimeReduce(閃雷悍將.颱風, 100);
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.AranDrain)) != null) {
            hpRecover_onAttack.put(effect.getSourceId(), new Pair<>(effect.getX(), 100));
        }
        if (chra.getBuffedValue(SecondaryStat.IndieBuffIcon) != null) {
            asrR += 80;
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.WindBreakerFinal)) != null) {
            finalAttackSkill = effect.getSourceId();
        }
        effect = chra.getEffectForBuffStat(SecondaryStat.AddAttackCount);
        buff = chra.getBuffedValue(SecondaryStat.AddAttackCount);
        if (effect != null && buff != null) {
            incFinalDamage += effect.getX() * buff;
        }
        effect = chra.getEffectForBuffStat(SecondaryStat.Judgement);
        buff = chra.getBuffedValue(SecondaryStat.Judgement);
        if (effect != null && buff != null) {
            switch (buff) {
                case 1: {
                    critRate += effect.getV();
                    break;
                }
                case 2: {
                    dropBuff += effect.getW();
                    break;
                }
                case 3: {
                    asrR += effect.getX();
                    terR += effect.getY();
                    break;
                }
                case 5: {
                    hpRecover_onAttack.put(effect.getSourceId(), new Pair<>(effect.getZ(), 100));
                    break;
                }
            }
        }
        effect = chra.getEffectForBuffStat(SecondaryStat.LifeTidal);
        buff = chra.getBuffedValue(SecondaryStat.LifeTidal);
        if (effect != null && buff != null) {
            if (buff == 2) {
                critRate += effect.getProp();
            } else if (buff == 1) {
                incDamR += effect.getX();
            }
        }
        buff = chra.getBuffedValue(SecondaryStat.StackBuff);
        effect = chra.getEffectForBuffStat(SecondaryStat.StackBuff);
        if (buff != null && effect != null) {
            if (effect.getSourceId() == 夜光.黑暗強化) {
                incDamR += buff;
            }
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.InfinityForce)) != null) {
            reduceForceR = 100;
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.InfinityForce)) != null) {
            addSkillCooltimeReduce(惡魔殺手.魔力吶喊, 50);
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.DiabolikRecovery)) != null) {
            hpRecoverTime = effect.getW() * 1000;
            healHPR = effect.getX();
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.DotHealHPPerSecond)) != null) {
            this.hpRecoverTime = 4000;
            this.healHPR = effect.getX();
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.DotHealMPPerSecond)) != null) {
            this.mpRecoverTime = 4000;
            this.healMPR = effect.getX();
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.AttackCountX)) != null) {
            addSkillAttackCount(煉獄巫師.三重之矛, effect.getAttackCount());
            addSkillAttackCount(煉獄巫師.四重攻擊, effect.getAttackCount());
            addSkillAttackCount(煉獄巫師.絕命攻擊, effect.getAttackCount());
            addSkillAttackCount(煉獄巫師.終極攻擊, effect.getAttackCount());
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.HowlingAttackDamage)) != null) {
            incPadR += buff;
            incMadR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.BeastForm)) != null) {
            incPadR += buff;
        }
        buff = chra.getBuffedValue(SecondaryStat.JaguarCount);
        effect = chra.getEffectForBuffStat(SecondaryStat.JaguarCount);
        if (buff != null && effect != null) {
            critRate += buff * effect.getY();
            criticalDamage += buff * effect.getZ();
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.JaguarSummoned)) != null) {
            asrR += effect.getASRRate();
            criticalDamage += effect.getCriticalDamage();
        }
        if (chra.getBuffedValue(SecondaryStat.Mechanic) != null) {
            stanceProp += 100;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.BombTime)) != null) {
            addSkillBulletCount(機甲戰神.進階追蹤飛彈, buff);
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.SurplusSupply)) != null) {
            incStrR += buff;
            incDexR += buff;
            incIntR += buff;
            incLukR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.SelfHyperBodyMaxHP)) != null) {
            incMaxHPR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.SelfHyperBodyMaxMP)) != null) {
            incMaxMPR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.EvasionMaster)) != null) {
            incDamR += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.SelfHyperBodyIncPAD)) != null) {
            pad += buff;
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.ReshuffleSwitch)) != null) {
            switch (effect.getSourceId()) {
                case 凱撒.洗牌交換_防禦模式: {
                    wdef += effect.getPddX();
                    incMaxHPR += effect.getMhpR();
                    final int[] array3 = {凱撒.防禦模式, 凱撒.二階防禦模式, 凱撒.三階防禦模式};
                    for (int j = 0; j < 3; ++j) {
                        if ((effect = chra.getSkillEffect(array3[j])) != null) {
                            wdef += effect.getPddX();
                            incMaxHPR += effect.getMhpR();
                        }
                    }
                    break;
                }
                case 凱撒.洗牌交換_攻擊模式: {
                    pad += effect.getPadX();
                    critRate += effect.getCritical();
                    bossDamageR += effect.getBossDamage();
                    final int[] array4 = {凱撒.攻擊模式, 凱撒.二階攻擊模式, 凱撒.三階攻擊模式};
                    for (int k = 0; k < 3; ++k) {
                        if ((effect = chra.getSkillEffect(array4[k])) != null) {
                            pad += effect.getPadX();
                            critRate += effect.getCritical();
                            bossDamageR += effect.getBossDamage();
                        }
                    }
                    break;
                }
            }
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.SmashStack)) != null) {
            if (buff >= 100) {
                speed += 5;
                jump += 10;
                stanceProp += 20;
            } else if (buff >= 300) {
                speed += 10;
                jump += 20;
                stanceProp += 40;
            }
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.SoulGazeCriDamR)) != null) {
            criticalDamage += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.AffinitySlug)) != null) {
            incDamR += buff;
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.Larkness)) != null) {
            if (effect.getSourceId() == 夜光.平衡_光明) {
                addSkillCooltimeReduce(夜光.死神鐮刀, 100);
                addSkillCooltimeReduce(夜光.絕對擊殺, 100);
            }
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.FireAura)) != null) {
            mpcon_eachSecond -= effect.getMpCon();
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.IceAura)) != null) {
            mpcon_eachSecond -= effect.getMpCon();
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.BMageAuraYellow)) != null && JobConstants.is煉獄巫師(chra.getJob())) {
            mpcon_eachSecond -= effect.getMpCon();
        }
        if ((effect = chra.getEffectForBuffStat(SecondaryStat.Frenzy)) != null) {
            hpRecover_limit = effect.getW();
            percent_damage += chra.getBuffedIntZ(SecondaryStat.Frenzy) + percent_damage * chra.getBuffedIntZ(SecondaryStat.Frenzy) / 100.0;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.ItemUpByItem)) != null) {
            dropBuff += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.DropRate)) != null) {
            dropBuff += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.ExpBuffRate)) != null) {
            expBuff += buff - 100;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.PlusExpRate)) != null) {
            plusExpRate += buff;
        }
        if ((buff = chra.getBuffedValue(SecondaryStat.OverloadMode)) != null) {
            mpRecover_limit = 0;
        }
    }

    public boolean checkEquipLevels(MapleCharacter chr, long gain) {
        boolean changed = false;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<Equip> all = new ArrayList<>(equipstats.equipLevelHandling);
        for (Equip eq : all) {
            int lvlz = eq.getEquipLevel();
            eq.setItemEXP(Math.min(eq.getItemEXP() + gain, Long.MAX_VALUE));

            if (eq.getEquipLevel() > lvlz) { //lvlup
                for (int i = eq.getEquipLevel() - lvlz; i > 0; i--) {
                    //升級裝備屬性
                    final Map<String, Map<String, Integer>> inc = ii.getEquipIncrements(eq.getItemId());
                    int extra = eq.getYggdrasilWisdom();
                    if (extra == 1) {
                        inc.get(String.valueOf(lvlz + i - 1)).put("incSTRMin", 1);
                        inc.get(String.valueOf(lvlz + i - 1)).put("incSTRMax", 3);
                    } else if (extra == 2) {
                        inc.get(String.valueOf(lvlz + i - 1)).put("incDEXMin", 1);
                        inc.get(String.valueOf(lvlz + i - 1)).put("incDEXMax", 3);
                    } else if (extra == 3) {
                        inc.get(String.valueOf(lvlz + i - 1)).put("incINTMin", 1);
                        inc.get(String.valueOf(lvlz + i - 1)).put("incINTMax", 3);
                    } else if (extra == 4) {
                        inc.get(String.valueOf(lvlz + i - 1)).put("incLUKMin", 1);
                        inc.get(String.valueOf(lvlz + i - 1)).put("incLUKMax", 3);
                    }
                    if (inc != null && inc.containsKey(String.valueOf(lvlz + i - 1)) && inc.get(String.valueOf(lvlz + i - 1)) != null) {
                        eq = ii.levelUpEquip(eq, inc.get(String.valueOf(lvlz + i - 1)));
                    }
                    //檢測裝備是否可以獲得技能
                    if (GameConstants.getStatFromWeapon(eq.getItemId()) == null && ItemConstants.getMaxLevel(eq.getItemId()) < (lvlz + i) && Math.random() < 0.1 && eq.getIncSkill() <= 0 && ii.getEquipSkills(eq.getItemId()) != null) {
                        for (int zzz : ii.getEquipSkills(eq.getItemId())) {
                            final Skill skil = SkillFactory.getSkill(zzz);
                            if (skil != null && skil.canBeLearnedBy(chr.getJobWithSub())) { //dont go over masterlevel :D
                                eq.setIncSkill(skil.getId());
                                chr.dropMessage(5, "武器：" + skil.getName() + " 已獲得新的等級提升！");
                            }
                        }
                    }
                }
                changed = true;
            }
            chr.forceUpdateItem(eq.copy());
        }
        if (changed) {
            chr.equipChanged();
            chr.send(EffectPacket.showItemLevelupEffect());
            chr.getMap().broadcastMessage(chr, EffectPacket.showForeignItemLevelupEffect(chr.getId()), false);
        }
        return changed;
    }

    public boolean checkEquipDurabilitys(MapleCharacter chr, int gain) {
        return checkEquipDurabilitys(chr, gain, false);
    }

    public boolean checkEquipDurabilitys(MapleCharacter chr, int gain, boolean aboveZero) {
        if (chr.inPVP()) {
            return true;
        }
        List<Equip> all = new ArrayList<>(equipstats.durabilityHandling);
        for (Equip item : all) {
            if (item != null && ((item.getPosition() >= 0) == aboveZero)) {
                item.setDurability(item.getDurability() + gain);
                if (item.getDurability() < 0) { //shouldnt be less than 0
                    item.setDurability(0);
                }
            }
        }
        for (Equip eqq : all) {
            if (eqq != null && eqq.getDurability() == 0 && eqq.getPosition() < 0) { //> 0 went to negative
                if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                    chr.send(InventoryPacket.getInventoryFull());
                    chr.send(InventoryPacket.getShowInventoryFull());
                    return false;
                }
                equipstats.durabilityHandling.remove(eqq);
                short pos = chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                MapleInventoryManipulator.unequip(chr.getClient(), eqq.getPosition(), pos);
            } else if (eqq != null) {
                chr.forceUpdateItem(eqq.copy());
            }
        }
        return true;
    }

    public void checkEquipSealed(MapleCharacter chr, long gain) {
        List<Equip> all = new ArrayList<>(equipstats.sealedEquipHandling);
        List<ModifyInventory> mods = new ArrayList<>();
        for (Equip eqq : all) {
            if (eqq != null && !GameConstants.canSealedLevelUp(eqq.getItemId(), eqq.getSealedLevel(), eqq.getSealedExp())) {
                eqq.gainSealedExp(gain);
                mods.add(new ModifyInventory(4, eqq)); //刪除裝備
            }
        }
        chr.send(InventoryPacket.modifyInventory(true, mods, chr));
    }

    public void handleProfessionTool(MapleCharacter chra) {
        if (chra.getProfessionLevel(92000000) > 0 || chra.getProfessionLevel(92010000) > 0) {
            for (Item item : chra.getInventory(MapleInventoryType.EQUIP).newList()) { //goes to first harvesting tool and stops
                Equip equip = (Equip) item;
                if (equip.getDurability() != 0 && (equip.getItemId() / 10000 == 150 && chra.getProfessionLevel(92000000) > 0) || (equip.getItemId() / 10000 == 151 && chra.getProfessionLevel(92010000) > 0)) {
                    if (equip.getDurability() > 0) {
                        equipstats.durabilityHandling.add(equip);
                    }
                    equipstats.harvestingTool = equip.getPosition();
                    break;
                }
            }
        }
    }

    /**
     * 計算被動熟練度(武器)
     *
     * @param player
     * @param wt
     */
    private void CalcPassive_Mastery(MapleCharacter player, MapleWeapon wt) {
        if (wt == MapleWeapon.沒有武器) {
            passive_mastery = 0;
            return;
        }
        int skil;
        boolean acc = true;
        switch (wt) {
            case 弓:
                skil = JobConstants.is破風使者(player.getJob()) ? 破風使者.精準之弓 : 箭神.精準之弓;
                break;
            case 拳套:
                skil = player.getJob() >= 410 && player.getJob() <= 412 ? 夜使者.精準暗器 : 暗夜行者.投擲精通;
                break;
            case 手杖:
                skil = player.getTotalSkillLevel(幻影俠盜.進階手杖精通) > 0 ? 幻影俠盜.進階手杖精通 : 幻影俠盜.手杖精通;
                break;
            case 加農砲:
                skil = 重砲指揮官.精通加農砲;
                break;
            case 雙刀:
            case 短劍:
                skil = player.getJob() >= 430 && player.getJob() <= 434 ? 影武者.精準雙刀 : 暗影神偷.精準之刀;
                break;
            case 弩:
                skil = JobConstants.is末日反抗軍(player.getJob()) ? 狂豹獵人.精準之弩 : 神射手.精準之弩;
                break;
            case 單手斧:
            case 單手棍:
                skil = JobConstants.is惡魔殺手(player.getJob()) ? 惡魔殺手.武器精通 : (JobConstants.is聖魂劍士(player.getJob()) ? 聖魂劍士.精準之劍 : (JobConstants.is米哈逸(player.getJob()) ? 米哈逸.精準之劍 : (player.getJob() >= 110 && player.getJob() <= 112 ? 英雄.武器精通 : 聖騎士.武器精通)));
                break;
            case 雙手斧:
            case 單手劍:
            case 雙手劍:
            case 雙手棍:
                skil = JobConstants.is凱撒(player.getJob()) ? 凱撒.劍技專精 : JobConstants.is聖魂劍士(player.getJob()) ? 聖魂劍士.精準之劍 : (JobConstants.is米哈逸(player.getJob()) ? 米哈逸.精準之劍 : (player.getJob() >= 110 && player.getJob() <= 112 ? 英雄.武器精通 : 聖騎士.武器精通));
                break;
            case 矛:
                skil = JobConstants.is狂狼勇士(player.getJob()) ? 狂狼勇士.精準之矛 : 黑騎士.武器精通;
                break;
            case 槍:
                skil = 黑騎士.武器精通;
                break;
            case 指虎:
                skil = JobConstants.is閃雷悍將(player.getJob()) ? 閃雷悍將.精通指虎 : JobConstants.is隱月(player.getJob()) ? 隱月.指虎熟練 : 拳霸.精通指虎;
                break;
            case 火槍:
                skil = JobConstants.is末日反抗軍(player.getJob()) ? 機甲戰神.機甲戰神精通 : 槍神.精通槍法;
                break;
            case 雙弩槍:
                skil = 精靈遊俠.雙弩槍精通;
                break;
            case 短杖:
            case 長杖:
                acc = false;
                skil = JobConstants.is末日反抗軍(player.getJob()) ? 煉獄巫師.長杖精通_2轉 : (player.getJob() <= 212 ? 火毒.咒語精通 : (player.getJob() <= 222 ? 冰雷.咒語精通 : (player.getJob() <= 232 ? 主教.咒語精通 : (player.getJob() <= 2000 ? 烈焰巫師.咒語磨練 : 龍魔導士.咒語精通))));
                break;
            case 閃亮克魯:
                acc = false;
                skil = 夜光.咒語精通;
                break;
            case 靈魂射手:
                skil = 天使破壞者.靈魂射手;
                break;
            case 魔劍:
                skil = 惡魔復仇者.魔劍精通;
                break;
            case 能量劍:
                skil = 傑諾.能量劍精通;
                break;
            case 琉:
                skil = 神之子.琉之力;
                break;
            case 璃:
                skil = 神之子.璃之力;
                break;
            case 記憶長杖:
                acc = false;
                skil = 琳恩.森林的力量IV;
                break;
            case ESP限製器:
                acc = false;
                skil = 凱內西斯.ESP精通;
                break;
            case 鎖鍊:
                skil = 卡蒂娜.武器精通;
                break;
            case 魔法護腕:
                acc = false;
                skil = 伊利恩.精通魔法護腕;
                break;
            default:
                passive_mastery = 0;
                return;
        }
        trueMastery = wt.getBaseMastery();
        if (player.getSkillLevel(skil) <= 0) {
            return;
        }
        MapleStatEffect eff = player.getSkillEffect(skil);
        if (eff == null) {
            return;
        }
        passive_mastery = (byte) eff.getMastery();
        trueMastery += eff.getMastery();
        int n = -1;
        switch (player.getJob()) {
            case 112: {
                n = 英雄.進階鬥氣;
                break;
            }
            case 122: {
                n = 聖騎士.聖騎士大師;
                break;
            }
            case 132: {
                n = 黑騎士.進階武器精通;
                break;
            }
            case 212: {
                n = 火毒.召喚火魔;
                break;
            }
            case 222: {
                n = 冰雷.召喚冰魔;
                break;
            }
            case 231:
            case 232: {
                n = 主教.神聖集中術;
                break;
            }
            case 312: {
                n = 箭神.弓術精通;
                break;
            }
            case 322: {
                n = 神射手.弩術精通;
                break;
            }
            case 412: {
                n = 夜使者.暗器精通;
                break;
            }
            case 422: {
                n = 暗影神偷.進階精準之刀;
                break;
            }
            case 434: {
                n = 影武者.雙刀流精通;
                break;
            }
            case 512: {
                n = 拳霸.拳霸大師;
                break;
            }
            case 522: {
                n = 槍神.槍神奧義;
                break;
            }
            case 532: {
                n = 重砲指揮官.炎熱加農砲;
                break;
            }
            case 1112: {
                n = 聖魂劍士.專家之劍;
                break;
            }
            case 1212: {
                n = 烈焰巫師.魔法真理;
                break;
            }
            case 1312: {
                n = 破風使者.弓術精通;
                break;
            }
            case 1412: {
                n = 暗夜行者.投擲專家;
                break;
            }
            case 1512: {
                n = 閃雷悍將.火之書;
                break;
            }
            case 2112: {
                n = 狂狼勇士.攻擊戰術;
                break;
            }
            case 2217:
            case 2218: {
                n = 龍魔導士.精通魔法;
                break;
            }
            case 2312: {
                n = 精靈遊俠.進階雙弩槍精通;
                break;
            }
            case 2412: {
                n = 幻影俠盜.進階手杖精通;
                break;
            }
            case 2512: {
                n = 隱月.高級指虎熟練;
                break;
            }
            case 2712: {
                n = 夜光.精通魔法;
                break;
            }
            case 3112: {
                n = 惡魔殺手.進階武器精通;
                break;
            }
            case 3212: {
                n = 煉獄巫師.長杖專家;
                break;
            }
            case 3312: {
                n = 狂豹獵人.弩術精通;
                break;
            }
            case 3512: {
                n = 機甲戰神.合金盔甲終極;
                break;
            }
            case 3612: {
                n = 傑諾.能量劍專家;
                break;
            }
            case 3712: {
                n = 爆拳槍神.護腕精通_4轉;
                break;
            }
            case 4212: {
                n = 陰陽師.華扇;
                break;
            }
            case 5112: {
                n = 米哈逸.進階精準之劍;
                break;
            }
            case 6112: {
                n = 凱撒.進階之劍精通;
                break;
            }
            case 6412: {
                n = 卡蒂娜.進階武器精通;
                break;
            }
            case 14212: {
                n = 凱內西斯.進階ESP精通;
                break;
            }
            case 15212: {
                n = 伊利恩.水晶的秘密;
                break;
            }
        }
        if (n > 0 && (eff = player.getSkillEffect(n)) != null) {
            trueMastery -= eff.getMastery();
            trueMastery += eff.getMastery();
            if (n == 聖騎士.聖騎士大師) {
                switch (wt) {
                    case 單手劍:
                    case 單手棍: {
                        trueMastery += 3;
                        break;
                    }
                }
            }
        }
    }

    private void calculateFame(boolean first_login, MapleCharacter player) {
        player.getTrait(MapleTraitType.charm).addLocalExp(player.getFame());
        for (MapleTraitType t : MapleTraitType.values()) {
            MapleTrait trait = player.getTrait(t);
            if (trait.recalcLevel() && !first_login) {
                player.updateSingleStat(trait.getType().getStat(), trait.getLocalTotalExp());
            }
        }
    }

    public short getCriticalRate() {
        lock.readLock().lock();
        try {
            return critRate;
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getCriticalDamage() {
        lock.readLock().lock();
        try {
            return criticalDamage;
        } finally {
            lock.readLock().unlock();
        }
    }

    public byte passive_mastery() {
        return passive_mastery; //* 5 + 10 for mastery %
    }

    /*
     * 飛鏢
     * 弓箭
     * 弩矢
     * 子彈
     * 等攻擊傷害加成
     */
    public double calculateMaxProjDamage(int projectileWatk, MapleCharacter chra) {
        if (projectileWatk < 0) {
            return 0;
        } else {
            Item weapon_item = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
            MapleWeapon weapon = weapon_item == null ? MapleWeapon.沒有武器 : MapleWeapon.getByItemID(weapon_item.getItemId());
            int mainstat, secondarystat;
            switch (weapon) {
                case 弓: //弓
                case 弩: //弩
                case 火槍: //短槍
                    mainstat = localdex; //敏捷
                    secondarystat = localstr; //力量
                    break;
                case 拳套: //拳套
                    mainstat = localluk; //幸運
                    secondarystat = localdex; //敏捷
                    break;
                default:
                    mainstat = 0;
                    secondarystat = 0;
                    break;
            }
            float maxProjDamage = weapon.getMaxDamageMultiplier(chra.getJob()) * (4 * mainstat + secondarystat) * (projectileWatk / 100.0f);
            maxProjDamage += maxProjDamage * (percent_damage / 100.0f);
            return maxProjDamage;
        }
    }

    private long calcBaseDamage(int mainStat, int secStat, int tertStat, int pad, double maxWeaponDamageMultiplier) {
        return (long) ((tertStat + secStat + 4L * mainStat) / 100.0 * (pad * maxWeaponDamageMultiplier));
    }

    private long calcHybridMaxDamage(int stat1, int stat2, int stat3, int stat4, int pad, double finalDamage) {
        return (long) ((stat1 * 3.5 + stat2 * 3.5 + stat3 * 3.5 + stat4) / 100.0 * (pad * finalDamage));
    }

    private long calcMaxDamageByHp(int rawHp, int totalHp, int str, int pad, double finalDamage) {
        return (long) (((int) (rawHp / 3.5) + 0.8 * ((int) ((totalHp - rawHp) / 3.5)) + str) / 100.0 * (pad * finalDamage));
    }

    public long getMinDamage(MapleCharacter chra) {
        return (long) (getMaxDamage(chra) * trueMastery / 100.0);
    }

    public long getMaxDamage(MapleCharacter chra) {
        short job = chra.getJob();
        BaseStat mainStat = GameConstants.getMainStatForJob(job);
        int attStat = mainStat == BaseStat.inte ? mad : pad;
        int totalMainStat = 0;
        int totalSecStat = 0;
        if (JobConstants.is傑諾(job)) {
            return calcHybridMaxDamage(localstr, localdex, localluk, 0, attStat, equipstats.wt.getMaxDamageMultiplier(chra.getJob()));
        }
        switch (mainStat) {
            case str:
                totalMainStat = localstr;
                totalSecStat = localdex;
                break;
            case dex:
                totalMainStat = localdex;
                totalSecStat = localstr;
                break;
            case inte:
                totalMainStat = localint;
                totalSecStat = localluk;
                break;
            case luk:
                totalMainStat = localluk;
                totalSecStat = localdex;
                break;
            case mhp:
                return calcMaxDamageByHp(localmaxhp, localmaxhp, localstr, attStat, equipstats.wt.getMaxDamageMultiplier(chra.getJob()));
            default:
                System.out.println("getMaxDamage錯誤");
                break;
        }
        return (long) (calcBaseDamage(totalMainStat, totalSecStat, 0, attStat, equipstats.wt.getMaxDamageMultiplier(chra.getJob())) * percent_damage * (1 + (incDamR / 100.0)));
    }

    public void calculateMaxBaseDamage(MapleCharacter chra, MapleWeapon wt) {
        if (this.pad <= 0) {
            localbasedamage_max = 1;
            localmaxbasepvpdamage = 1;
            return;
        }
        int job = chra.getJob();
        int grade = JobConstants.getJobBranch(job);
        int ad = pad;
        int adfx = indiePadFX;
        if (grade == 2) {
            if (mad > 0) {
                ad = mad;
                adfx = indieMadFX;
            }
        }
        double damage;
        if (grade != 2 || JobConstants.is凱內西斯(job) || JobConstants.is夜光(job)) {
            int damstat = 0;
            switch (wt) {
                case 能量劍: {
                    damstat = (int) (3.5 * (localstr + localdex + localluk));
                    break;
                }
                case 弓:
                case 弩:
                case 火槍:
                case 雙弩槍:
                case 靈魂射手: {
                    damstat = 4 * localdex + localstr;
                    break;
                }
                case 短劍:
                case 雙刀: {
                    damstat = 4 * localluk + localdex + localstr;
                    if (grade != 4 && grade != 6) {
                        damstat = localstr + localdex + localluk;
                    }
                    break;
                }
                case 手杖:
                case 拳套: {
                    damstat = 4 * localluk + localdex;
                    if (JobConstants.is暗夜行者(job)) {
                        damstat = (int) (4 * localluk + 2.5 * localdex);
                    }
                    if (job == 2003) {
                        damstat = 4 * localstr + localdex;
                    }
                    break;
                }
                case 魔劍: {
                    damstat = 2 * getMaxHp() / 7 + localstr;
                    break;
                }
                default: {
                    if (grade == 2) {
                        damstat = 4 * localint + localluk;
                    } else {
                        damstat = 4 * localstr + localdex;
                    }
                }
            }
            double weapondam = wt.getMaxDamageMultiplier(job);
            damage = weapondam * ad * damstat / 100.0;
            if (JobConstants.is惡魔復仇者(job)) {
                int addhp = localmaxhp - getMaxHp();
                if (addhp > 0) {
                    damage += weapondam * ad * 2.0 * (addhp / 7.0) / 100.0 * 0.8;
                }
            }
        } else {
            damage = 1.0 * ad * (4 * localint + localluk) / 100.0;
        }
        damage = damage * (1.0 + incDamR / 100.0) * (1.0 + percent_damage / 100.0) + 0.5;
        damage += damage * kannaLinkDamR / 100.0;
        localbasedamage_max = (long) (Math.floor(damage) + adfx);
        localbasedamage_min = Math.round(damage * trueMastery / 100.0) + adfx;

    }

    public float getHealHP() {
        return healHPR;
    }

    public float getHealMP() {
        return healMPR;
    }

    /*
     * 自動回復血和藍
     */
    public void relocHeal(MapleCharacter chra) {
        int playerjob = chra.getJob();
        //重置恢復數據信息和時間
        healHPR = 10 + recoverHP;
        healMPR = JobConstants.isNotMpJob(chra.getJob()) ? 0 : (3 + mpRestore + recoverMP + (localint / 10));
        mpRecoverTime = 0;
        hpRecoverTime = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (playerjob == 5111 || playerjob == 5112) {
            Skill skill = SkillFactory.getSkill(米哈逸.魔力恢復); //51110000 - 自我恢復 - 每4秒回復HP和MP，戰鬥中也能恢復。
            int lvl = chra.getSkillLevel(skill);
            if (lvl > 0 && skill != null) {
                MapleStatEffect effect = skill.getEffect(lvl);
                healHPR += effect.getHp();
                hpRecoverTime = 4000;
                healMPR += effect.getMp();
                mpRecoverTime = 4000;
            }
        } else if (playerjob == 6111 || playerjob == 6112) {
            Skill skill = SkillFactory.getSkill(凱撒.自我恢復); //61110006 - 自我恢復 - 持續補充體力和MP。
            int lvl = chra.getSkillLevel(skill);
            if (lvl > 0 && skill != null) {
                MapleStatEffect effect = skill.getEffect(lvl);
                healHPR += effect.getX();
                hpRecoverTime = effect.getY(); //或者 eff.getW() * 1000
                healMPR += effect.getX();
                mpRecoverTime = effect.getY(); //或者 eff.getW() * 1000
            }
        }
        if (chra.getChair() != null) { // 椅子恢復的血量 暫時默認這個數字
            Pair<Integer, Integer> ret = ii.getChairRecovery(chra.getChair().getItemId());
            if (ret != null) {
                healHPR += ret.getLeft();
                if (hpRecoverTime == 0) {
                    hpRecoverTime = 4000;
                }
                healMPR += JobConstants.isNotMpJob(chra.getJob()) ? 0 : ret.getRight();
                if (mpRecoverTime == 0 && !JobConstants.isNotMpJob(chra.getJob())) {
                    hpRecoverTime = 4000;
                }
            }
        } else if (chra.getMap() != null) { // 地圖自動恢復的倍率
            float recvRate = chra.getMap().getRecoveryRate();
            if (recvRate > 0) {
                healHPR *= recvRate;
                healMPR *= recvRate;
            }
        }
    }

    public void zeroData(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, int mask, boolean beta) {
        if (JobConstants.is神之子(chr.getJob())) {
            mplew.writeShort(mask);
            if ((mask & 0x1) != 0x0) {
                mplew.writeBool(beta);
            }
            if ((mask & 0x2) != 0x0) {
                mplew.writeInt(betamaxhp);
            }
            if ((mask & 0x4) != 0x0) {
                mplew.writeInt(betamaxmp);
            }
            if ((mask & 0x8) != 0x0) {
                mplew.write(chr.getSecondSkinColor());
            }
            if ((mask & 0x10) != 0x0) {
                mplew.writeInt(chr.getSecondHair());
            }
            if ((mask & 0x20) != 0x0) {
                mplew.writeInt(chr.getSecondFace());
            }
            if ((mask & 0x40) != 0x0) {
                mplew.writeInt(getMaxHp());
            }
            if ((mask & 0x80) != 0x0) {
                mplew.writeInt(getMaxMp());
            }
            if ((mask & 0x100) != 0x0) {
                String oneInfo = chr.getOneInfo(52999, "zeroMask");
                mplew.writeLong(oneInfo == null ? 0 : Long.parseLong(oneInfo));
            }
        }
    }

    public int getSkillIncrement(int skillID) {
        try {
            if (lock.readLock().tryLock(1, TimeUnit.SECONDS)) {
                try {
                    return equipstats.skillsIncrement.getOrDefault(skillID, 0);
                } finally {
                    lock.readLock().unlock();
                }
            }
        } catch (InterruptedException ignore) {

        }
        return 0;
    }

    public int getEquipmentSkill(int skillID) {

        try {
            if (lock.readLock().tryLock(1, TimeUnit.SECONDS)) {
                try {
                    return equipstats.equipmentSkills.getOrDefault(skillID, 0);
                } finally {
                    lock.readLock().unlock();
                }
            }
        } catch (InterruptedException ignore) {

        }
        return 0;
    }

    public int getElementBoost(Element key) {
        lock.readLock().lock();
        try {
            return equipstats.elemBoosts.getOrDefault(key, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getSkillDamageIncrease(int key) {
        lock.readLock().lock();
        try {
            return add_skill_damage.getOrDefault(key, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void heal_noUpdate(MapleCharacter chra) {
        setHp(getCurrentMaxHP(), chra);
        setMp(getCurrentMaxMP());
    }

    public void heal(MapleCharacter chra) {
        heal_noUpdate(chra);
        chra.updateSingleStat(MapleStat.HP, getCurrentMaxHP());
        chra.updateSingleStat(MapleStat.MP, getCurrentMaxMP());
    }

    public void recalcPVPRank(MapleCharacter chra) {
        this.pvpRank = 10;
        this.pvpExp = chra.getTotalBattleExp();
        for (int i = 0; i < 10; i++) {
            if (pvpExp > GameConstants.getPVPExpNeededForLevel(i + 1)) {
                pvpRank--;
                pvpExp -= GameConstants.getPVPExpNeededForLevel(i + 1);
            }
        }
    }

    public int getLifeTidal() {
        return getHPPercent() >= getMPPercent() ? 2 : 1;
    }

    public int getHPPercent() {
        lock.readLock().lock();
        try {
            return (localmaxhp > 0) ? (hp * 100 / localmaxhp) : 1;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getMPPercent() {
        lock.readLock().lock();
        try {
            return (localmaxmp > 0) ? (mp * 100 / localmaxmp) : 1;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void init(MapleCharacter chra) {
        recalcLocalStats(chra);
    }

    public short getInt() {
        return int_;
    }

    public void setStr(short str, MapleCharacter chra) {
        this.str = str;
        recalcLocalStats(chra);
    }

    public void setDex(short dex, MapleCharacter chra) {
        this.dex = dex;
        recalcLocalStats(chra);
    }

    public void setLuk(short luk, MapleCharacter chra) {
        this.luk = luk;
        recalcLocalStats(chra);
    }

    public void setInt(short int_, MapleCharacter chra) {
        this.int_ = int_;
        recalcLocalStats(chra);
    }

    public int getHealHp() {
        return Math.max(localmaxhp - hp, 0);
    }

    public int getHealMp(int job) {
        if (JobConstants.isNotMpJob(job)) {
            return 0;
        }
        return Math.max(localmaxmp - mp, 0);
    }

    public void setHp(int newhp) {
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;
    }

    public boolean setHp(int newhp, MapleCharacter chra) {
        return setHp(newhp, false, chra);
    }

    public boolean setHp(int newhp, boolean silent, MapleCharacter chra) {
        int oldHp = hp;
        setHp(newhp);
        if (chra != null) {
            if (!silent) {
                chra.updatePartyMemberHP();
            }
        }
        return hp != oldHp;
    }

    public boolean setMp(int newmp) {
        int oldMp = mp;
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        this.mp = tmp;
        return mp != oldMp;
    }

    public void setInfo(int maxhp, int maxmp, int hp, int mp) {
        this.maxhp = maxhp;
        this.maxmp = maxmp;
        this.hp = hp;
        this.mp = mp;
    }

    public int getMaxHp() {
        return getMaxHp(true);
    }

    public int getMaxHp(boolean local) {
        if (local) {
            return Math.min(ServerConfig.CHANNEL_PLAYER_MAXHP, maxhp + apAddMaxHp);
        }
        return maxhp;
    }

    public int getMaxMp() {
        return getMaxMp(true);
    }

    public int getMaxMp(boolean local) {
        if (local) {
            return Math.min(ServerConfig.CHANNEL_PLAYER_MAXMP, maxmp + apAddMaxMp);
        }
        return maxmp;
    }

    public int getTotalDex() {
        lock.readLock().lock();
        try {
            return localdex;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getTotalInt() {
        lock.readLock().lock();
        try {
            return localint;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getTotalStr() {
        lock.readLock().lock();
        try {
            return localstr;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getTotalLuk() {
        lock.readLock().lock();
        try {
            return localluk;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getTotalMagic() {
        lock.readLock().lock();
        try {
            return mad;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getSpeed() {
        lock.readLock().lock();
        try {
            return speed;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getJump() {
        lock.readLock().lock();
        try {
            return jump;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getTotalWatk() {
        lock.readLock().lock();
        try {
            return pad;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 實際最大HP
     *
     * @return
     */
    public int getCurrentMaxHP() {
        lock.readLock().lock();
        try {
            return localmaxhp;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 實際最大MP
     *
     * @return
     */
    public int getCurrentMaxMP() {
        lock.readLock().lock();
        try {
            return localmaxmp;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getBetaMaxHP() {
        lock.readLock().lock();
        try {
            return betamaxhp;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setBetaMaxHP(int hp) {
        lock.writeLock().lock();
        try {
            this.betamaxhp = hp;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int getBetaMaxMP() {
        lock.readLock().lock();
        try {
            return betamaxmp;
        } finally {
            lock.readLock().unlock();
        }
    }

    public long getCurrentMaxBaseDamage() {
        lock.readLock().lock();
        try {
            return localbasedamage_max;
        } finally {
            lock.readLock().unlock();
        }
    }

    public long getCurrentMinBaseDamage() {
        lock.readLock().lock();
        try {
            return localbasedamage_min;
        } finally {
            lock.readLock().unlock();
        }
    }

    public float getCurrentMaxBasePVPDamage() {
        lock.readLock().lock();
        try {
            return localmaxbasepvpdamage;
        } finally {
            lock.readLock().unlock();
        }
    }

    public float getCurrentMaxBasePVPDamageL() {
        lock.readLock().lock();
        try {
            return localmaxbasepvpdamageL;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 檔格(不會因為怪物攻擊後退的機率)
     *
     * @return int
     */
    public int getStanceProp() {
        lock.readLock().lock();
        try {
            return stanceProp;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 所有技能的冷卻時間 : -#reduceCooltime秒(最多減少5秒)
     */
    public int getCooltimeReduceR() {
        lock.readLock().lock();
        try {
            return cooltimeReduceR;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 召喚獸有效時間增加 #summonTimeR%
     */
    public int getSummonTimeR() {
        lock.readLock().lock();
        try {
            return summonTimeR;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getReduceCooltime() {
        lock.readLock().lock();
        try {
            return reduceCooltime;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 額外增加攻擊次數
     */
    public int getAttackCount(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_attackCount.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 額外增加攻擊怪物數量
     */
    public int getMobCount(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_targetPlus.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 技能冷卻時間減少
     */
    public int getReduceCooltimeRate(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_coolTimeR.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 無視怪物防禦
     */
    public double getIgnoreMobpdpR() {
        lock.readLock().lock();
        try {
            return ignoreMobpdpR;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 額外增加技能無視怪物防禦
     *
     * @param skillId
     * @return
     */
    public double getIgnoreMobpdpR(int skillId) {
        lock.readLock().lock();
        try {
            int val = getSkillIgnoreMobpdpRate(skillId);
            return ignoreMobpdpR + (100.0 - ignoreMobpdpR) * (val / 100.0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 傷害%
     */
    public double getDamageRate() {
        lock.readLock().lock();
        try {
            return incDamR;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * BOSS傷害%
     */
    public int getBossDamageRate() {
        lock.readLock().lock();
        try {
            return bossDamageR;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getStarForce() {
        lock.readLock().lock();
        try {
            return equipstats.starForce;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getArc() {
        lock.readLock().lock();
        try {
            return arc;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 角色對BOSS的攻擊加成
     */
    public int getSkillBossDamage(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_bossDamage.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 格外增加技能的持續時間
     */
    public int getDuration(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_duration.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getDotTime(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_dotTime.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 增加技能的傷害
     */
    public void addSkillDamageIncrease(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_damage.merge(skillId, val, (a, b) -> a + b);
    }

    /**
     * 增加技能的傷害
     */
    public void addSkillDamageIncrease_5th(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_damage_5th.merge(skillId, val, (a, b) -> a + b);
    }

    public int getSkillDamageIncrease_5th(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_damage_5th.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 增加技能攻擊怪物的數量
     */
    public void addSkillTargetPlus(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_targetPlus.merge(skillId, val, (a, b) -> a + b);
    }

    /**
     * 增加技能攻擊怪物的次數
     */
    public void addSkillAttackCount(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_attackCount.merge(skillId, val, (a, b) -> a + b);
    }

    /**
     * 增加技能對BOSS的傷害
     */
    public void addSkillBossDamage(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_bossDamage.merge(skillId, val, (a, b) -> a + b);
    }

    /**
     * 增加技能攻擊怪物的無視概率
     */
    public void addIgnoreMobpdpRate(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_ignoreMobpdpR.merge(skillId, val, (a, b) -> a + b);
    }

    public int getSkillIgnoreMobpdpRate(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_ignoreMobpdpR.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 增加技能BUFF的持續時間
     */
    public void addSkillDuration(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_duration.merge(skillId, val, (a, b) -> a + b);
    }

    public void addSkillCustomVal(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_custom_val.merge(skillId, val, (a, b) -> a + b);
    }

    public int getSkillCustomVal(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_custom_val.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 增加技能攻擊怪物的中毒效果的持續時間
     */
    public void addDotTime(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_dotTime.merge(skillId, val, (a, b) -> a + b);
    }

    /**
     * 技能冷去時間減少
     */
    public void addSkillCooltimeReduce(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_coolTimeR.merge(skillId, val, (a, b) -> a + b);
    }

    /**
     * 增加技能觸發的概率
     */
    public void addSkillProp(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_prop.merge(skillId, val, (a, b) -> a + b);
    }

    public int getGauge_x() {
        return gauge_x;
    }

    public int getAddSkillProp(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_prop.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addDamAbsorbShieldR(int val) {
        damAbsorbShieldR += (100.0 - damAbsorbShieldR) * (val / 100.0);
    }

    public void addIgnoreMobpdpR(final double val) {
        ignoreMobpdpR += (100.0 - ignoreMobpdpR) * (val / 100.0);
    }

    public double getIncFinalDamage() {
        lock.readLock().lock();
        try {
            return incFinalDamage;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addIncFinalDamage(final double val) {
        incFinalDamage = incFinalDamage * (100.0 + val) / 100.0;
    }

    public void addSkillBulletCount(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_bulletCount.merge(skillId, val, (a, b) -> a + b);
    }

    public int getSkillBulletCount(int skillId) {
        lock.readLock().lock();
        try {
            return add_skill_bulletCount.getOrDefault(skillId, 0);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getIncBuffTime() {
        lock.readLock().lock();
        try {
            return incBuffTime;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getFinalAttackSkill() {
        lock.readLock().lock();
        try {
            return finalAttackSkill;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 返回有召喚獸的裝備
     *
     * @return
     */
    public Pair<Integer, Integer> getEquipSummon() {
        Pair<Integer, Integer> pair = new Pair<>(0, 0);
        lock.readLock().lock();
        try {
            final List<Integer> equipSummons = equipstats.equipSummons;
            if (summoned > 0 && !equipSummons.contains(summoned)) {
                pair = new Pair<>(summoned, 0);
                summoned = 0;
            } else if (summoned == 0 && !equipSummons.isEmpty()) {
                summoned = equipSummons.get(0);
                pair = new Pair<>(summoned, 1);
            }
        } finally {
            lock.readLock().unlock();
        }
        return pair;
    }

    @Override
    public String toString() {
        return "PlayerStats{"
                + "ignoreMobpdpR=" + ignoreMobpdpR
                + ", incDamR=" + incDamR
                + ", bossDamageR=" + bossDamageR
                + ", localstr=" + localstr
                + ", localdex=" + localdex
                + ", localluk=" + localluk
                + ", localint=" + localint
                + ", localmaxhp=" + localmaxhp
                + ", localmaxmp=" + localmaxmp
                + ", localbasedamage_max=" + localbasedamage_max
                + ", localbasedamage_min=" + localbasedamage_min
                + '}';
    }

    public int getSkillReduceForceCon(int skillId) {
        lock.readLock().lock();
        try {
            int n = reduceForceR;
            if (add_skill_reduceForceCon.containsKey(skillId)) {
                n += add_skill_reduceForceCon.get(skillId);
            }
            return Math.min(100, n);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addSkillReduceForceCon(int skillId, int val) {
        if (skillId < 0 || val <= 0) {
            return;
        }
        add_skill_reduceForceCon.merge(skillId, val, (a, b) -> a + b);
    }

    private boolean canNextRecalcStat() {
        long curr = System.currentTimeMillis();
        if (curr > nextRecalcStats) {
            nextRecalcStats = curr + 2000L;
            return true;
        }
        return false;
    }

    public int getRecallRingId() {
        lock.readLock().lock();
        try {
            return equipstats.recallRingId;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getElementDef() {
        lock.readLock().lock();
        try {
            return equipstats.element_def;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getElementIce() {
        lock.readLock().lock();
        try {
            return equipstats.element_ice;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getElementFire() {
        lock.readLock().lock();
        try {
            return equipstats.element_fire;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getElementLight() {
        lock.readLock().lock();
        try {
            return equipstats.element_light;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getElementPsn() {
        lock.readLock().lock();
        try {
            return equipstats.element_psn;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<Integer, Pair<Integer, Integer>> getHPRecoverItemOption() {
        lock.readLock().lock();
        try {
            return equipstats.hpRecover_itemOption;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<Integer, Pair<Integer, Integer>> getMPRecoverItemOption() {
        lock.readLock().lock();
        try {
            return equipstats.mpRecover_itemOption;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<Integer, Pair<Integer, Integer>> getDamageReflect() {
        lock.readLock().lock();
        try {
            return equipstats.DAMreflect;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getPassivePlus() {
        try {
            lock.readLock().tryLock(1, TimeUnit.SECONDS);
            try {
                return equipstats.passivePlus;
            } finally {
                lock.readLock().unlock();
            }
        } catch (InterruptedException ignore) {
        }
        return 0;
    }

    public int getHarvestingTool() {
        lock.readLock().lock();
        try {
            return equipstats.harvestingTool;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean canFish() {
        lock.readLock().lock();
        try {
            return equipstats.canFish;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean canFishVIP() {
        lock.readLock().lock();
        try {
            return equipstats.canFishVIP;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<Integer, List<Integer>> getEquipmentBonusExps() {
        lock.readLock().lock();
        try {
            return equipstats.equipmentBonusExps;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getQuestBonus() {
        lock.readLock().lock();
        try {
            return equipstats.questBonus;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getLevelBonus() {
        lock.readLock().lock();
        try {
            return equipstats.levelBonus;
        } finally {
            lock.readLock().unlock();
        }
    }

    public enum RecalcFlag {
        FirstLogin,
        Equip,
        ;

        final int flag;

        RecalcFlag() {
            flag = 1 << this.ordinal();
        }

        final boolean check(int n) {
            return (n & flag) != 0;
        }
    }
}
