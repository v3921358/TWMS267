package Net.server.buffs;

import Client.*;
import Client.inventory.Equip;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Client.skills.SkillMesInfo;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.stat.PlayerStats;
import Client.status.MonsterStatus;
import Config.configs.FireRangbConfig;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Config.constants.SkillConstants;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.初心者;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.法師;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.皇家騎士團_技能群組.*;
import Net.auth.Auth;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.MapleStatInfo;
import Net.server.life.Element;
import Net.server.life.MapleMonster;
import Net.server.life.MobSkill;
import Net.server.maps.*;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import Packet.*;
import Server.channel.ChannelServer;
import Server.world.World;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import connection.packet.FieldPacket;
import SwordieX.field.fieldeffect.FieldEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.Randomizer;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MapleStatEffect implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private static final Logger log = LoggerFactory.getLogger(MapleStatEffect.class);
    Map<MapleStatInfo, Integer> info;
    Map<MapleStatInfo, Double> infoD;
    private int debuffTime = 0;
    private Map<MapleTraitType, Integer> traits;
    private boolean overTime;
    boolean partyBuff = false;
    boolean rangeBuff = false;
    private boolean notRemoved, notIncBuffDuration; //不能取消的BUFF
    private EnumMap<SecondaryStat, Integer> statups;
    private List<Pair<Integer, Integer>> availableMap;
    private Map<MonsterStatus, Integer> monsterStatus;
    private Point lt, rb, lt2, rb2, lt3, rb3;
    private int level;
    //private List<Pair<Integer, Integer>> randomMorph;
    private List<SecondaryStat> cureDebuffs;
    private List<Integer> petsCanConsume, familiars, randomPickup;
    private List<Triple<Integer, Integer, Integer>> rewardItem;
    private byte slotCount, slotPerLine; //礦(藥)背包道具需要
    private byte recipeUseCount, recipeValidDay, reqSkillLevel, effectedOnAlly, effectedOnEnemy, type, preventslip, immortal, bs;
    private short ignoreMob, mesoR, thaw, lifeId, imhp, immp, inflation, useLevel, indiePdd, indieMdd, mobSkill, mobSkillLevel; // incPVPdamage,
    private double hpR, mpR;
    private int sourceid, recipe, moveTo, moneyCon, morphId = 0, expinc, exp, consumeOnPickup, charColor, interval, rewardMeso, totalprob, cosmetic;
    private int expBuff, itemup, mesoup, cashup, berserk, illusion, booster, berserk2;
    private boolean ruleOn;
    private boolean bxi = false;
    private boolean hit;

    /**
     * 添加被動效果
     *
     * @param applyto 角色
     * @param obj     對像
     */
    public void applyPassive(MapleCharacter applyto, MapleMapObject obj) {
        /*判斷技能是否有概率獲得特定的增益效果*/
        if (makeChanceResult()) {
            /*sourceid 技能ID，此處單獨引用 sourceid 無需判斷是否為 skill*/
            switch (sourceid) {
                case 火毒.魔力吸收:
                case 冰雷.魔力吸收:
                case 主教.魔力吸收:
                    if (obj == null || obj.getType() != MapleMapObjectType.MONSTER) {
                        /*如果對像為空或者對象的類型不是怪物就直接返回*/
                        return;
                    }
                    MapleMonster mob = (MapleMonster) obj;
                    /*取當前怪物的狀態信息，判斷是否為BOSS*/
                    if (!mob.getStats().isBoss()) {
                        /* absorbMp 吸收MP的計算方法：技能X值除以100乘以怪物的最大MP，得到的結果如果小於或等於怪物當前的MP，就賦值給 absorbMp 反之 將怪物當前的mp賦值給 absorbMp*/
                        int absorbMp = Math.min((int) (mob.getMobMaxMp() * (getX() / 100.0)), mob.getMp());
                        /* 判斷吸收MP的結果值是否大於0*/
                        if (absorbMp > 0) {
                            /*設置怪物當前的MP：怪物當前的MP減去被吸收的MP。*/
                            mob.setMp(mob.getMp() - absorbMp);
                            /*設置角色當前的MP：角色當前的MP加上吸收到的MP*/
                            applyto.getStat().setMp(applyto.getStat().getMp() + absorbMp);
                            /*發送給角色吸收MP的效果包*/
                            applyto.getClient().announce(EffectPacket.encodeUserEffectLocal(sourceid, EffectOpcode.UserEffect_SkillUse, applyto.getLevel(), level));
                            /*發送給角色當前所在地圖其他玩家的效果廣播包*/
                            applyto.getMap().broadcastMessage(applyto, EffectPacket.onUserEffectRemote(applyto, sourceid, EffectOpcode.UserEffect_SkillUse, applyto.getLevel(), level), false);
                        }
                    }
                    break;
            }
        }
    }

    public boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null, getBuffDuration(chr), false);
    }

    public boolean applyTo(MapleCharacter chr, int duration) {
        return applyTo(chr, chr, true, null, duration, false);
    }

    public boolean applyTo(MapleCharacter chr, boolean passive) {
        return applyTo(chr, chr, true, null, getBuffDuration(chr), passive);
    }

    public boolean unprimaryApplyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, false, pos, getBuffDuration(chr), false);
    }

    public boolean unprimaryApplyTo(MapleCharacter chr, Point pos, boolean passive) {
        return applyTo(chr, chr, false, pos, getBuffDuration(chr), passive);
    }

    public boolean unprimaryPassiveApplyTo(MapleCharacter chr) {
        return applyTo(chr, chr, false, null, getBuffDuration(chr), true);
    }

    public boolean attackApplyTo(MapleCharacter chr, boolean passive, Point pos) {
        return applyTo(chr, chr, getBuffDuration(chr), false, true, passive, pos);
    }

    public boolean applyTo(MapleCharacter chr, Point pos, boolean passive) {
        return applyTo(chr, chr, true, pos, getBuffDuration(chr), passive);
    }

    public boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, getBuffDuration(chr), true, false, false, pos);
    }

    public boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos, int newDuration) {
        return applyTo(applyfrom, applyto, primary, pos, newDuration, false);
    }

    public boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos, int newDuration, boolean passive) {
        return applyTo(applyfrom, applyto, newDuration, primary, false, passive, pos);
    }

    public boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, int newDuration, boolean primary, boolean att, boolean passive, Point pos) {
        //xx使用棧變量加快訪問速度
        int sourceid = getSourceId();
        if (sourceid == 管理員.終極隱藏 && applyto.isHidden()) {
            applyto.cancelEffect(this, false, -1);
            return true;
        }
        int cooldown = getCooldown(applyfrom);
        int level = this.level;
        PlayerStats playerStats = applyfrom.getStat();
        int itemConNo = info.get(MapleStatInfo.itemConNo);
        int itemCon = info.get(MapleStatInfo.itemCon);
        AbstractSkillHandler sh = getSkillHandler();
        int result = -1;
        if (sh != null) {
            SkillClassApplier applier = new SkillClassApplier();
            applier.effect = this;
            applier.duration = newDuration;
            applier.primary = primary;
            applier.att = att;
            applier.passive = passive;
            applier.cooldown = cooldown;
            applier.pos = pos;
            result = sh.onApplyTo(applyfrom, applyto, applier);
            if (result == 0) {
                return false;
            } else if (result == 1) {
                newDuration = applier.duration;
                primary = applier.primary;
                att = applier.att;
                passive = applier.passive;
                cooldown = applier.cooldown;
                pos = applier.pos;
            }
        }
        if (itemConNo != 0 && !applyto.inPVP()) {
            if (!applyto.haveItem(itemCon, itemConNo, false, true)) {
                return false;
            }
            MapleInventoryManipulator.removeById(applyto.getClient(), ItemConstants.getInventoryType(itemCon), itemCon, itemConNo, false, true);
        }
        boolean rapidAttack = SkillConstants.isRapidAttackSkill(sourceid);
        if (!passive) {
            //計算HP變化
            int hpChange, hpHeal = 0, hpcost = 0, mpChange, mpHeal = 0, mpcost = 0;
            final boolean busihua = applyfrom.getBuffedValue(SecondaryStat.BanMap) != null;
            int effhp = getHp();
            if (effhp != 0 && sourceid != 主教.天使之箭 && sourceid != 主教.群體治癒 && sourceid != 主教.和平使者) {
                if (!isSkill()) {
                    hpHeal += alchemistModifyVal(applyfrom, effhp, true);
                    if (busihua) {
                        hpHeal /= 2;
                    }
                } else {
                    hpHeal += MapleStatEffectFactory.makeHealHP(effhp / 100.0, playerStats.getTotalMagic(), 3, 5);
                    if (busihua) {
                        hpHeal = -hpHeal;
                    }
                }
            }
            if (this.hpR != 0.0) {
                hpHeal += getHpMpChange(applyfrom, true) / (busihua ? 2 : 1);
            }
            final MapleStatEffect eff = applyfrom.getEffectForBuffStat(SecondaryStat.Thaw);
            if (getHpRCon() != 0 && (eff == null || !eff.isSkill())) {
                hpcost += (int) (getHpRCon() * playerStats.getCurrentMaxHP() / 100.0);
            }
            if (getHpCon() != 0 && (eff == null || !eff.isSkill())) {
                hpcost += getHpCon();
            }
            if (JobConstants.isNotMpJob(applyfrom.getJob()) || applyfrom.getBuffedIntValue(SecondaryStat.OverloadMode) > 0) {
                mpHeal = 0;
            } else {
                int effmp = getMp();
                if (effmp != 0) {
                    mpHeal += alchemistModifyVal(applyfrom, effmp, true);
                }
                if (mpR != 0.0) {
                    mpHeal += getHpMpChange(applyfrom, false);
                }
            }
            int mpCon = getMpCon();
            if (JobConstants.is惡魔殺手(applyfrom.getJob())) {
                int forceCon = getForceCon(applyfrom);
                if (applyfrom.getBuffedValue(SecondaryStat.InfinityForce) != null) {
                    mpcost = 0;
                } else {
                    mpcost = forceCon;
                }
            } else if (mpCon != 0) {
                if (!JobConstants.is夜光(applyfrom.getJob()) || !isHit() || sourceid % 1000 / 100 != 2 || (applyfrom.getBuffSource(SecondaryStat.Larkness) != 夜光.暗蝕 && applyfrom.getBuffSource(SecondaryStat.Larkness) != 夜光.平衡_光明)) {
//                    int mpconMaxPercent = getDamage() > 0 && !isSummonSkill() ? applyfrom.getStat().mpconMaxPercent * applyfrom.getStat().getCurrentMaxMp(applyfrom.getJob()) / 100 : 0;
                    mpcost += (int) ((mpCon - mpCon * playerStats.mpconReduce / 100) * (applyfrom.getStat().incMpCon / 100.0));
                }
            }
            SecondaryStatValueHolder mb = applyfrom.getBuffStatValueHolder(SecondaryStat.TeleportMasteryOn);
            if (sourceid == 法師.瞬間移動 && mb != null) {
                mpcost += mb.effect.getY();
            }
            if (JobConstants.is凱殷(applyfrom.getJob())) {
                switch (sourceid) {
                    case 凱殷.具現_衝擊箭_2:
                    case 凱殷.具現_散射箭_1:
                    case 凱殷.具現_強化崩壞爆破_1:
                    case 凱殷.具現_破塵箭_1:
                    case 凱殷.具現_處刑_暗地狙擊_1:
                        applyfrom.dispelEffect(凱殷.主導);
                        break;
                    case 凱殷.處刑_致命奇襲:
                    case 凱殷.具現_龍之爆裂:
                    case 凱殷.掌握痛苦:
                        mpcost = 0;
                        break;
                }
            }
            if (JobConstants.is虎影(applyfrom.getJob())) {
                if (applyfrom.getSkillEffect(虎影.符咒道力) != null) {
                    int atGauge1Con = getAtGauge1Con();
                    int atGauge2Con = getAtGauge2Con();
                    final int atGauge2Inc = getAtGauge2Inc();
                    int scrollDiff = 0;
                    if (atGauge2Con > 0) {
                        scrollDiff = -atGauge2Con;
                    } else if (atGauge2Inc > 0 && applyfrom.getSkillEffect(虎影.卷軸道力) != null) {
                        scrollDiff = atGauge2Inc;
                    }
                    applyfrom.handleHoYoungValue(-atGauge1Con, scrollDiff);
                }
            }
            if (applyfrom.getBuffedValue(SecondaryStat.Wizard_OverloadMana) != null) {
                if (JobConstants.isNotMpJob(applyfrom.getJob())) {
                    hpcost += playerStats.getCurrentMaxHP() / 100;
                } else {
                    mpcost += playerStats.getCurrentMaxMP() * 2 / 100;
                }
            }
            if (hpHeal > 0 && !is血腥盛宴() && applyto.getEffectForBuffStat(SecondaryStat.Frenzy) != null) {
                hpHeal = Math.min(playerStats.getCurrentMaxHP() / 100, hpHeal);
            }
            if (sourceid == 惡魔復仇者.惡魔狂亂 && applyto.getBuffedValue(SecondaryStat.Frenzy) != null) { // 取消惡魔狂亂時不扣血
                hpcost = 0;
            }
            if (hpcost > 0 && applyto.getStat().getHp() <= Math.abs(hpcost)) {
                hpcost = 0;
            }
            if (applyfrom == applyto) {
                hpChange = hpHeal - hpcost;
                mpChange = mpHeal - mpcost;
                if (hpcost > 0 && sourceid == 惡魔復仇者.亡靈) {
                    applyfrom.getTempValues().put("亡靈HP消耗", hpcost);
                }
            } else {
                hpChange = hpHeal;
                mpChange = mpHeal;
            }
            applyto.addHPMP(hpChange, Math.min(mpChange, applyto.getStat().getCurrentMaxMP() * applyto.getStat().mpRecover_limit / 100), !rapidAttack && !att);
        }
        if (!isSkill()) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int value = ii.getItemProperty(-sourceid, "spec/charismaEXP", 0);
            if (value > 0) {
                applyto.getTrait(MapleTraitType.charisma).addExp(value, applyto);
            }
            value = ii.getItemProperty(-sourceid, "spec/insightEXP", 0);
            if (value > 0) {
                applyto.getTrait(MapleTraitType.insight).addExp(value, applyto);
            }
            value = ii.getItemProperty(-sourceid, "spec/willEXP", 0);
            if (value > 0) {
                applyto.getTrait(MapleTraitType.will).addExp(value, applyto);
            }
            value = ii.getItemProperty(-sourceid, "spec/craftEXP", 0);
            if (value > 0) {
                applyto.getTrait(MapleTraitType.craft).addExp(value, applyto);
            }
            value = ii.getItemProperty(-sourceid, "spec/senseEXP", 0);
            if (value > 0) {
                applyto.getTrait(MapleTraitType.sense).addExp(value, applyto);
            }
            value = ii.getItemProperty(-sourceid, "spec/charmEXP", 0);
            if (value > 0) {
                applyto.getTrait(MapleTraitType.charm).addExp(value, applyto);
            }
        }
        int fixCoolTime = statups == null ? 0 : statups.getOrDefault(SecondaryStat.FixCoolTime, 0);
        if (fixCoolTime > 0) {
            Skill skil;
            int leftTime;
            List<MapleCoolDownValueHolder> coolDowns = applyto.getCooldowns();
            for (MapleCoolDownValueHolder mc : coolDowns) {
                leftTime = mc.getLeftTime();
                if (leftTime <= fixCoolTime) {
                    continue;
                }
                skil = SkillFactory.getSkill(mc.skillId);
                if (skil == null || skil.isVSkill()) {
                    continue;
                }
                applyto.reduceSkillCooldown(mc.skillId, leftTime - fixCoolTime);
            }
        }
        int powerCon;
        if (primary && (powerCon = getPowerCon()) > 0 && applyfrom.getBuffedValue(SecondaryStat.AmaranthGenerator) == null) {
            if (applyfrom.getBuffedIntValue(SecondaryStat.SurplusSupply) < powerCon) {
                return false;
            }
            applyfrom.applyXenonEnegy(-powerCon);
        }
        int ppRecovery = info.get(MapleStatInfo.ppRecovery);
        if (ppRecovery > 0) {
            applyto.handlePPCount(ppRecovery);
        }
        int ppCon = getPPCon();
        if ((sourceid == 凱內西斯.終極技_深層衝擊 || sourceid == 凱內西斯.終極_心靈彈丸) && ppCon > 0) {
            applyto.handlePPCount(-ppCon);
        }
        if (isReturnScroll()) {  //回城卷處理
            applyReturnScroll(applyto);
        }
        if (recipe > 0) {
            if (applyto.getSkillLevel(recipe) > 0 || applyto.getProfessionLevel(recipe / 10000 * 10000) < reqSkillLevel) {
                return false;
            }
            applyto.changeSingleSkillLevel(SkillFactory.getCraft(recipe), Integer.MAX_VALUE, recipeUseCount, recipeValidDay > 0 ? System.currentTimeMillis() + recipeValidDay * 24L * 60 * 60 * 1000 : -1L);
        }
        Skill skill = null;
        if (isSkill()) {
            skill = SkillFactory.getSkill(sourceid);
        }
        if (this instanceof MobSkill mSkill) {
            if (mSkill.getEmotion() != -1) {
                applyto.send(UIPacket.UserEmotionLocal(mSkill.getEmotion(), newDuration));
            }
            for (SecondaryStat stat : statups.keySet()) {
                if (stat == SecondaryStat.GiantBossDeathCnt) {
                    continue;
                }
                if (applyto.getBuffedValue(stat) != null) {
                    return false;
                }
            }
        }
        if (this instanceof MobSkill || primary || ((att || passive) && !rapidAttack && skill != null && !skill.isChargeSkill())) {
            applyBuffEffect(applyfrom, applyto, newDuration, primary, att, passive, pos);
        }
        if (is時空門()) {
            applyto.removeAllTownPortal();
            applyto.notifyChanges();
            applyto.setTownPortalLeaveTime(System.currentTimeMillis() + info.get(MapleStatInfo.time));
            TownPortal townPortal = new TownPortal(applyto, sourceid); // Current Map door
            if (townPortal.getTownPortal() != null) {
                applyto.getMap().spawnTownPortal(townPortal);
                applyto.addTownPortal(townPortal);
                TownPortal townPortalInTown = new TownPortal(townPortal); // Town door
                applyto.addTownPortal(townPortalInTown);
                townPortalInTown.getTownMap().spawnTownPortal(townPortalInTown);
                applyto.notifyChanges();
            } else {
                applyto.dropMessage(5, "無法使用時空門，村莊不可容納。");
            }
        }
        int debuffDuration;
        if ((debuffDuration = getMobDebuffDuration(applyfrom)) > 0 && debuffDuration != 2100000000 && primary && !monsterStatus.isEmpty() && getMobCount() > 0 && (sourceid == 80011540 || getAttackCount() <= 0) && info.get(MapleStatInfo.hcReflect) <= 0) {
            switch (sourceid) {
                case 重砲指揮官.幸運木桶:
                    break;
                default:
                    applyToMonster(applyfrom, debuffDuration);
                    break;
            }
        }
        if (primary && isMist()) {
            applyAffectedArea(applyto, pos);
        }
        if (cureDebuffs != null) {
            for (SecondaryStat stat : cureDebuffs) {
                applyfrom.dispelEffect(stat);
            }
        }
        if (is楓葉淨化()) {
            applyto.dispelEffect(SecondaryStat.BanMap);
            applyto.dispelEffect(SecondaryStat.Attract);
            applyto.dispelEffect(SecondaryStat.StopPortion);
            applyto.dispelEffect(SecondaryStat.DispelItemOption);
            applyto.dispelEffect(SecondaryStat.ReverseInput);
        }
        if (sourceid == 重砲指揮官.幸運木桶) {
            int value = applyto.getBuffedIntValue(SecondaryStat.Roulette);
            if (value == 2) {
                cooldown = 0;
            } else {
                cooldown /= 2;
            }
        }
        if (80011492 == sourceid) { // 燃燒之戒
            Equip eq = null;
            for (Item item : applyfrom.getInventory(MapleInventoryType.EQUIPPED).listById(1114400)) { // 燃燒之戒
                if (((Equip) item).isMvpEquip()) {
                    eq = (Equip) item;
                    break;
                }
            }
            int maxStep = 10;
            boolean canBurningAllField = false;
            int enhanceNum = 0;
            if (eq != null) {
                canBurningAllField = true;
                boolean forever = eq.getExpiration() < 0;
                if ((!forever && eq.getStarForceLevel() < 10)) { //1星 在 10星 之前 保持技能
                    enhanceNum = 1;
                    cooldown = FireRangbConfig.FIRE_MAP_COOLDOWN_1_9;
                    maxStep = FireRangbConfig.FIRE_MAP_STAGE_1_9;
                } else if ((!forever && eq.getStarForceLevel() < 15)) { //10星 在 15星 之前 保持技能
                    enhanceNum = 10;
                    cooldown = FireRangbConfig.FIRE_MAP_COOLDOWN_10_14;
                    maxStep = FireRangbConfig.FIRE_MAP_STAGE_10_14;
                } else if ((!forever && eq.getStarForceLevel() < 20)) { //15星 在 20星 之前 保持技能
                    enhanceNum = 15;
                    cooldown = FireRangbConfig.FIRE_MAP_COOLDOWN_15_19;
                    maxStep = FireRangbConfig.FIRE_MAP_STAGE_15_19;
                } else if ((!forever && eq.getStarForceLevel() < 25)) { //20星 在 25星 之前 保持技能
                    enhanceNum = 20;
                    cooldown = FireRangbConfig.FIRE_MAP_COOLDOWN_20_24;
                    maxStep = FireRangbConfig.FIRE_MAP_STAGE_20_24;
                } else if ((!forever && eq.getStarForceLevel() >= 25)) { //25星 在 30星 之前 保持技能
                    enhanceNum = 25;
                    cooldown = FireRangbConfig.FIRE_MAP_COOLDOWN_25_29;
                    maxStep = FireRangbConfig.FIRE_MAP_STAGE_25_29;
                } else if ((!forever && eq.getStarForceLevel() >= 30)) { //25星 在 30星 之前 保持技能
                    enhanceNum = 30;
                    cooldown = FireRangbConfig.FIRE_MAP_COOLDOWN_30;
                    maxStep = FireRangbConfig.FIRE_MAP_STAGE_30;
                } else {
                    enhanceNum = 31;
                    cooldown = FireRangbConfig.FIRE_MAP_COOLDOWN_30;
                    maxStep = FireRangbConfig.FIRE_MAP_STAGE_30;
                }
            }
            if (applyfrom.getMap() == null) {
                return false;
            }
            if (!canBurningAllField && !applyfrom.getMap().isBreakTimeField()) {
                applyfrom.dropSpouseMessage(UserChatMessageType.系統, "只能在燃燒場地內使用。");
                return false;
            }
            if (applyfrom.getMap().getBreakTimeFieldStep() >= maxStep) {
                applyfrom.dropSpouseMessage(UserChatMessageType.系統, "只能在低於" + maxStep + "階段的燃燒場地內使用。");
                return false;
            }
            if (enhanceNum > 0) {
                //applyfrom.dropSpouseMessage(UserChatMessageType.淺黃, "特殊裝備:[ "+ enhanceNum +"強化星力_燃燒之戒 " + (enhanceNum > 1 ? ("[" + enhanceNum + "]") : "") + ",偵測到在場地施放," + maxStep + "，該冷卻時間:" + cooldown + "分鐘﹞..");
                cooldown *= 1000;
            }
            applyfrom.dropSpouseMessage(UserChatMessageType.系統, "系統檢測觸發:釋放燃燒之戒-冷卻30分鐘！");
            applyfrom.send(EffectPacket.onUserEffectRemote(null, getSourceId(), EffectOpcode.UserEffect_SkillUse, applyfrom.getLevel(), getLevel()));
            applyfrom.getMap().broadcastMessage(applyfrom, EffectPacket.onUserEffectRemote(applyfrom, getSourceId(), EffectOpcode.UserEffect_SkillUse, applyfrom.getLevel(), getLevel()), false);
            applyfrom.getMap().broadcastMessage(applyfrom, FieldPacket.fieldEffect(FieldEffect.playSound("Sound/FieldSkill.img/100011/1/laser", 100, 0, 0)), true);
            applyfrom.getMap().setBreakTimeFieldStep(maxStep);
            applyfrom.getMap().updateBreakTimeField();
            applyfrom.getMap().broadcastMessage(applyfrom.getMap().getBreakTimeFieldStepPacket());
        }
        if (sourceid == 惡魔復仇者.血腥盛宴) {
            cooldown = 0;
        } else if (sourceid == 通用V核心.海盜通用.滿載骰子) {
            if (passive) {
                cooldown = 0;
            }
        } else if (80011540 == sourceid) { // 露希妲的噩夢
            Equip eq = null;
            for (Item item : applyfrom.getInventory(MapleInventoryType.EQUIPPED).listById(1033000)) { // 露希妲耳環
                if (((Equip) item).isMvpEquip()) {
                    eq = (Equip) item;
                    break;
                }
            }
            if (eq != null) {
                int enhanceNum = 0;
                boolean forever = eq.getExpiration() < 0;
                if ((!forever && !applyfrom.isSilverMvp()) || eq.getStarForceLevel() < 15) {
                    enhanceNum = 1;
                    cooldown = 105;
                } else if ((!forever && !applyfrom.isGoldMvp()) || eq.getStarForceLevel() < 20) {
                    enhanceNum = 15;
                    cooldown = 100;
                } else if ((!forever && !applyfrom.isDiamondMvp()) || eq.getStarForceLevel() < 25) {
                    enhanceNum = 20;
                    cooldown = 95;
                } else {
                    enhanceNum = 25;
                    cooldown = 90;
                }
                if (enhanceNum > 0) {
                    //applyfrom.dropSpouseMessage(UserChatMessageType.系統, "MVP露希妲耳環" + (enhanceNum > 1 ? ("[" + enhanceNum + "★]") : "") + "效果啟動，冷卻時間:" + cooldown + "秒");
                }
                cooldown *= 1000;
            }
        } else if (80011273 == sourceid && Auth.checkPermission("MVPEquip_1113220")) { // 幽暗抱擁
            Equip eq = null;
            for (Item item : applyfrom.getInventory(MapleInventoryType.EQUIPPED).listById(1113220)) { // 幽暗戒指
                if (((Equip) item).isMvpEquip()) {
                    eq = (Equip) item;
                    break;
                }
            }
            if (eq != null) {
                boolean forever = eq.getExpiration() < 0;
                if ((!forever && !applyfrom.isSilverMvp()) || eq.getStarForceLevel() < 15) {
                    cooldown = 4;
                } else if ((!forever && !applyfrom.isGoldMvp()) || eq.getStarForceLevel() < 20) {
                    cooldown = 3;
                } else if ((!forever && !applyfrom.isDiamondMvp()) || eq.getStarForceLevel() < 25) {
                    cooldown = 2;
                } else {
                    cooldown = 0;
                }
                cooldown *= 1000;
            }
        }
        if ((skill == null || !skill.isChargeSkill() || (!SkillConstants.isKeydownSkillCancelGiveCD(sourceid) && applyfrom.getKeyDownSkill_Time() == 0))
                && ((att && !passive) || primary) && applyfrom == applyto && cooldown > 0 && !applyfrom.isSkillCooling(sourceid)) {
            applyfrom.registerSkillCooldown(SkillConstants.getCooldownLinkSourceId(sourceid), cooldown, true);
        }
        int soulMpCon;
        if (primary && (soulMpCon = getSoulMpCon()) > 0) {
            if (applyto.getSoulMP() < (ServerConfig.JMS_SOULWEAPON_SYSTEM ? applyto.getMaxSoulMP() : soulMpCon)) {
                return false;
            }
            applyto.checkSoulState(true);
        }
        if (applyfrom == applyto) {
            if (isRangeBuff()) {
                for (MapleCharacter chr : applyfrom.getMap().getCharactersInRect(calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft()))) {
                    if (applyfrom.getId() != chr.getId()) {
                        applyTo(applyfrom, chr, newDuration, primary, att, passive, pos);
                    }
                }
            } else {
                final Party party = applyfrom.getParty();
                if (party != null) {
                    if (isPartyBuff()) {
                        final Rectangle rect = calculateBoundingBox(pos != null ? pos : applyfrom.getPosition(), applyfrom.isFacingLeft());
                        for (PartyMember member : party.getMembers()) {
                            if (member.getCharID() != applyfrom.getId() && member.getChr() != null && member.getChr().getMap() == applyfrom.getMap() && rect.contains(member.getChr().getPosition()) && member.getChr().isAlive()) {
                                applyTo(applyfrom, member.getChr(), newDuration, primary, att, passive, pos);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, int newDuration, boolean primary, boolean att, boolean passive, Point pos) {
        int sourceid = getSourceId();
        int level = getLevel();
        if ((!primary && SkillConstants.isRapidAttackSkill(sourceid)) || (passive && !primary && getSummonMovementType() != null && statups.size() <= 1)) {
            return;
        }
        if (this instanceof MobSkill) {
            MapleStatEffect effect;
            // 黑翼胸章
            if ((statups.containsKey(SecondaryStat.ReverseInput) || statups.containsKey(SecondaryStat.Seal)) && (effect = applyto.getSkillEffect(80011158)) != null) {
                effect.unprimaryPassiveApplyTo(applyto);
                return;
            }
        }
        int localDuration = newDuration;
        int maskedDuration = 0; //這個是設置1個自動BUFF的意思 也就是註冊的消失的時間 但是BUFF的持續時間是另外1個 也就是1個間隔的意思
        Map<SecondaryStat, Integer> localstatups = new EnumMap<>(statups);
        Map<SecondaryStat, Integer> maskedstatups = new EnumMap<>(SecondaryStat.class);
        Map<SecondaryStat, Pair<Integer, Integer>> sendstatups = new EnumMap<>(SecondaryStat.class);
        long currentTimeMillis = System.currentTimeMillis();
        long startChargeTime = 0L;
        int direction = 1;
        boolean b3 = false;
        boolean b4 = true;
        boolean b5 = true;
        boolean overwrite = true;
        boolean cancelEffect = true;
        boolean b7 = true;
        boolean applySummon = true;
        int buffz = getZ();
        AbstractSkillHandler sh = getSkillHandler();
        if (sh == null) {
            sh = SkillClassFetcher.getHandlerByJob(applyto.getJobWithSub());
        }
        int result = -1;
        if (sh != null) {
            SkillClassApplier applier = new SkillClassApplier();
            applier.effect = this;
            applier.primary = primary;
            applier.att = att;
            applier.passive = passive;
            applier.pos = pos;
            applier.duration = localDuration;
            applier.maskedDuration = maskedDuration;
            applier.localstatups = localstatups;
            applier.maskedstatups = maskedstatups;
            applier.sendstatups = sendstatups;
            applier.startChargeTime = startChargeTime;
            applier.b3 = b3;
            applier.b4 = b4;
            applier.b5 = b5;
            applier.overwrite = overwrite;
            applier.cancelEffect = cancelEffect;
            applier.b7 = b7;
            applier.applySummon = applySummon;
            applier.buffz = buffz;
            result = sh.onApplyBuffEffect(applyfrom, applyto, applier);
            if (result == 0) {
                return;
            } else if (result == 1) {
                primary = applier.primary;
                att = applier.att;
                passive = applier.passive;
                pos = applier.pos;
                localDuration = applier.duration;
                maskedDuration = applier.maskedDuration;
                localstatups = applier.localstatups;
                maskedstatups = applier.maskedstatups;
                sendstatups = applier.sendstatups;
                startChargeTime = applier.startChargeTime;
                b3 = applier.b3;
                b4 = applier.b4;
                b5 = applier.b5;
                overwrite = applier.overwrite;
                cancelEffect = applier.cancelEffect;
                b7 = applier.b7;
                applySummon = applier.applySummon;
                buffz = applier.buffz;
            }
        }

        if (result == -1) {
            switch (sourceid) {
                case -Reborn.REBORN_BUFF_ITEM:
                    localstatups = Reborn.getStatups(applyto.getReborns());
                    localDuration = 2100000000;
                    break;
                case 800: {
                    if (this instanceof MobSkill effect) {
                        localstatups.put(SecondaryStat.GiantBossDeathCnt, Math.min(applyto.getBuffedIntValue(SecondaryStat.GiantBossDeathCnt) + 1, effect.getLimit()));
                        if (localstatups.get(SecondaryStat.GiantBossDeathCnt) >= effect.getLimit()) {
                            applyto.dispelEffect(SecondaryStat.GiantBossDeathCnt);
                            if (applyto.isAlive()) {
                                applyto.addHPMP(-100, 0);
                            }
                            return;
                        }
                    }
                    break;
                }
                case 80010040: {
                    b3 = true;
                    break;
                }
                case 80011158: {
                    b3 = true;
                    b5 = false;
                    applyto.dispelEffect(SecondaryStat.ReverseInput);
                    applyto.dispelEffect(SecondaryStat.Seal);
                    applyto.dropMessage(-5, "黑翼胸章的潛在力量在保護著你。");
                    break;
                }
                case 80011159: {
                    b3 = true;
                    b5 = false;
                    break;
                }
                case 400011066: {
                    final SecondaryStatValueHolder mbsvh;
                    if (passive && (mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.HitStackDamR)) != null) {
                        buffz = Math.min(applyto.getBuffedIntZ(SecondaryStat.HitStackDamR) + 1, getY());
                        localDuration = mbsvh.getLeftTime();
                        localstatups.put(SecondaryStat.HitStackDamR, 1);
                        break;
                    }
                    break;
                }
                case 80002888:
                    if (att) {
                        return;
                    }
                    buffz = 0;
                    break;
                case 80012015:
                    SecondaryStatValueHolder holder;
                    if ((holder = applyto.getBuffStatValueHolder(SecondaryStat.ErdaStack)) != null) {
                        holder.value = 1;
                        applyto.send(BuffPacket.giveBuff(applyto, holder.effect, Collections.singletonMap(SecondaryStat.ErdaStack, holder.sourceID)));
                    }
                    break;
                default: {
                    if (GameConstants.getMountItem(sourceid, applyto) <= 0) {
                        break;
                    }
                    if (!primary) {
                        return;
                    }
                    final MapleStatEffect eff;
                    if (sourceid == 機甲戰神.合金盔甲_人型 && (eff = applyto.getSkillEffect(機甲戰神.合金盔甲終極)) != null) {
                        localstatups.put(SecondaryStat.EMHP, eff.getEnhancedHP());
                        localstatups.put(SecondaryStat.EMMP, eff.getEnhancedMP());
                        localstatups.put(SecondaryStat.EPAD, eff.getEnhancedWatk());
                        localstatups.put(SecondaryStat.EPDD, eff.getEnhancedWdef());
                    }
                    applyto.dispelEffect(SecondaryStat.RideVehicle);
                    localDuration = 2100000000;
                    localstatups.put(SecondaryStat.RideVehicle, GameConstants.getMountItem(sourceid, applyto));
                    break;
                }
            }
        }
        if (applyto.isInvincible() && sourceid == 初心者.金剛不壞) {
            if (applyto.isGm()) {
                applyto.dropMessage(9, "定制技能 - GM無敵[原技能: 金剛不壞](在角色無敵狀態才會生效)");
            }
            localstatups.clear();
//            localstatups.put(MapleBuffStat.IndieNotDamaged, 1);
            localstatups.put(SecondaryStat.DojangInvincible, 1);
            localstatups.put(SecondaryStat.HitStackDamR, 1);
            localstatups.put(SecondaryStat.IndieStance, 100);
        }
        if (80011248 == sourceid) { // 黎明神盾
            localstatups.put(SecondaryStat.DawnShield_ExHP, applyto.getStat().getCurrentMaxHP());
        }
        //取消一些技能BUFF的效果，以免重複
        if (cancelEffect) {
            applyto.cancelEffect(this, overwrite, -1, localstatups);
        }
        if (cancelEffect && !maskedstatups.isEmpty()) {
            applyto.cancelEffect(this, overwrite, -1L, maskedstatups);
        }
        final EnumMap<SecondaryStat, Integer> writeStatups = new EnumMap<>(SecondaryStat.class);
        for (final Entry<SecondaryStat, Pair<Integer, Integer>> entry : sendstatups.entrySet()) {
            writeStatups.put(entry.getKey(), entry.getValue().getLeft());
        }
        //設置BUFF技能的消失時間 和 註冊角色的BUFF狀態信息
        if (!sendstatups.isEmpty()) {
            applyto.registerEffect(sendstatups, buffz, applyfrom.getId(), currentTimeMillis, startChargeTime, localDuration, new CancelEffectAction(applyto, this, currentTimeMillis, localstatups));
        } else if (localDuration > 0 && !localstatups.isEmpty()) {
            applyto.registerEffect(this, localstatups, buffz, applyfrom.getId(), currentTimeMillis, startChargeTime, localDuration, new CancelEffectAction(applyto, this, currentTimeMillis, localstatups));
        }
        if (maskedDuration > 0 && !maskedstatups.isEmpty()) {
            final long startTime = System.currentTimeMillis() + 1L;
            applyto.registerEffect(this, maskedstatups, buffz, applyfrom.getId(), startTime, startChargeTime, maskedDuration, new CancelEffectAction(applyto, this, startTime, maskedstatups));
            localstatups.putAll(maskedstatups);
        }

        if (sh != null) {
            SkillClassApplier applier = new SkillClassApplier();
            applier.effect = this;
            applier.primary = primary;
            applier.att = att;
            applier.passive = passive;
            applier.pos = pos;
            applier.duration = localDuration;
            applier.maskedDuration = maskedDuration;
            applier.localstatups = localstatups;
            applier.maskedstatups = maskedstatups;
            applier.sendstatups = sendstatups;
            applier.startTime = currentTimeMillis;
            applier.startChargeTime = startChargeTime;
            applier.b3 = b3;
            applier.b4 = b4;
            applier.b5 = b5;
            applier.overwrite = overwrite;
            applier.cancelEffect = cancelEffect;
            applier.b7 = b7;
            applier.applySummon = applySummon;
            applier.buffz = buffz;
            result = sh.onAfterRegisterEffect(applyfrom, applyto, applier);
            if (result == 0) {
                return;
            } else if (result == 1) {
                primary = applier.primary;
                att = applier.att;
                passive = applier.passive;
                pos = applier.pos;
                localDuration = applier.duration;
                maskedDuration = applier.maskedDuration;
                localstatups = applier.localstatups;
                maskedstatups = applier.maskedstatups;
                sendstatups = applier.sendstatups;
                currentTimeMillis = applier.startTime;
                startChargeTime = applier.startChargeTime;
                b3 = applier.b3;
                b4 = applier.b4;
                b5 = applier.b5;
                overwrite = applier.overwrite;
                cancelEffect = applier.cancelEffect;
                b7 = applier.b7;
                applySummon = applier.applySummon;
                buffz = applier.buffz;
            }
        }

        if (!(this instanceof MobSkill) && isSkill() && 墨玄.絕技_暴技 != sourceid) {
            final Skill skill = SkillFactory.getSkill(sourceid);
            b7 = (skill) != null && skill.isInvisible();
        }
        if (localstatups.size() > 1 && (!b7 || SkillConstants.is召喚獸戒指(sourceid))) {
            localstatups.remove(SecondaryStat.IndieBuffIcon);
        }
        for (final SecondaryStat stat : localstatups.keySet()) {
            if (!writeStatups.containsKey(stat)) {
                writeStatups.put(stat, sourceid);
            }
        }
        Map<SecondaryStat, Integer> foreignStatups = new EnumMap<>(SecondaryStat.class);
        for (Entry<SecondaryStat, Integer> entry : localstatups.entrySet()) {
            if (SkillConstants.isShowForgenBuff(entry.getKey()) && entry.getKey() != SecondaryStat.GuidedBullet && entry.getKey() != SecondaryStat.PartyBooster) {
                foreignStatups.put(entry.getKey(), entry.getValue());
            }
        }
        if (applySummon && getSummonMovementType() != null) {
            applySummonEffect(applyto, pos, localDuration, applyto.getSpecialStat().getMaelstromMoboid(), currentTimeMillis);
        }
        if (foreignStatups.size() > 0) {
            applyto.getMap().broadcastMessage(BuffPacket.giveForeignBuff(applyto, foreignStatups));
        }
        if (localstatups.size() > 0) {
            applyto.getClient().announce(BuffPacket.giveBuff(applyto, this, writeStatups));
        }
        if (!(this instanceof MobSkill)) {
            if (b3 || applyfrom != applyto) {
                if (applyfrom == applyto && b5) {
                    applyto.getClient().announce(isSkill() ? EffectPacket.showBuffEffect(applyto, false, sourceid, level, direction, pos) : EffectPacket.showBuffItemEffect(-1, sourceid));
                } else {
                    applyto.getClient().announce(isSkill() ? EffectPacket.showSkillAffected(-1, sourceid, level, direction) : EffectPacket.showBuffItemEffect(-1, sourceid));
                }
            }
            if (b4 && (isSkill() && primary || SkillConstants.isRapidAttackSkill(sourceid))) {
                if (applyfrom == applyto) {
                    applyto.getMap().broadcastMessage(applyto, isSkill() ? EffectPacket.showBuffEffect(applyto, true, sourceid, level, direction, pos) : EffectPacket.showBuffItemEffect(applyto.getId(), sourceid), applyto.getPosition());
                    return;
                }
                applyto.getMap().broadcastMessage(applyto, isSkill() ? EffectPacket.showSkillAffected(applyto.getId(), sourceid, level, direction) : EffectPacket.showBuffItemEffect(applyto.getId(), sourceid), applyto.getPosition());
            }
        }
    }

    public AbstractSkillHandler getSkillHandler() {
        return SkillClassFetcher.getHandlerBySkill(sourceid);
    }

    public void applySummonEffect(final MapleCharacter applyto, Point pos, int duration, int mobOid, long startTime) {
        AbstractSkillHandler handler = getSkillHandler();
        int handleRes = -1;
        if (handler != null) {
            SkillClassApplier applier = new SkillClassApplier();
            applier.effect = this;
            applier.pos = pos;
            applier.duration = duration;
            applier.mobOid = mobOid;
            applier.startTime = startTime;
            handleRes = handler.onApplySummonEffect(applyto, applier);
            if (handleRes == 0) {
                return;
            } else if (handleRes == 1) {
                pos = applier.pos;
                duration = applier.duration;
                mobOid = applier.mobOid;
                startTime = applier.startTime;
            }
        }
        final SummonMovementType movementType = getSummonMovementType();
        if (movementType == null) {
            return;
        }
        int sourceid = this.sourceid;
        if (applyto.isDebug()) {
            applyto.dropDebugMessage(1, "[Spawn Summon] Effect:" + this);
        }
        int limit = 1;
        switch (sourceid) {
            case 陰陽師.式神炎舞_1:
            case 伊利恩.水晶技能_德烏斯_1: {
                limit = 5;
                break;
            }
            case 陰陽師.鬼神召喚: {
                limit = 2;
                break;
            }
            case 重砲指揮官.雙胞胎猴子_1: {
                limit = applyto.getSkillLevel(重砲指揮官.雙胞胎猴子_設置強化) > 0 ? 2 : 1;
                break;
            }
            case 英雄.劍士意念_1: {
                limit = 3;
                break;
            }
        }
        if (MapleSummon.getSummonMaxCount(sourceid) != 1) {
            try {
                long timeNow = System.currentTimeMillis();
                List<MapleSummon> summons = applyto.getSummonsReadLock();
                ListIterator<MapleSummon> summonIterator = summons.listIterator(summons.size());
                int summonCount = limit;
                while (summonIterator.hasPrevious()) {
                    final MapleSummon summon = summonIterator.previous();
                    if (summon.getSkillId() == sourceid || summon.getParentSummon() == sourceid) {
                        int maxCount = summon.getSummonMaxCount();
                        if (maxCount != 1 && summon.getCreateTime() + summon.getDuration() > timeNow) {
                            if (maxCount == -1 || summonCount++ < maxCount) {
                                continue;
                            }
                        }
                        if (applyto.isDebug()) {
                            applyto.dropDebugMessage(1, "[Summon] Remove Summon Effect:" + summon.getEffect());
                        }
                        applyto.getMap().disappearMapObject(summon);
                        summonIterator.remove();
                    }
                }
            } finally {
                applyto.unlockSummonsReadLock();
            }

            List<SecondaryStatValueHolder> holderList = applyto.getEffects().get(SecondaryStat.IndieBuffIcon);
            if (holderList != null) {
                List<Integer> linkSummons = MapleSummon.getLinkSummons(sourceid);
                linkSummons.add(sourceid);

                Iterator<SecondaryStatValueHolder> holderIterator = holderList.iterator();
                while (holderIterator.hasNext()) {
                    final SecondaryStatValueHolder mbsvh = holderIterator.next();
                    if (mbsvh != null && mbsvh.effect != null) {
                        for (int ls : linkSummons) {
                            if (mbsvh.effect.getSourceId() == ls && applyto.getSummonBySkillID(ls) == null) {
                                mbsvh.cancel();
                                holderIterator.remove();
                                if (applyto.isDebug()) {
                                    applyto.dropDebugMessage(1, "[BUFF] Deregister:" + SecondaryStat.IndieBuffIcon);
                                }
                            }
                        }
                    }
                }
                if (holderList.isEmpty()) {
                    applyto.getEffects().remove(SecondaryStat.IndieBuffIcon);
                }
            }
        }
        final int[] oidArray = new int[2];
        final Point summonTeamPos = new Point();
        for (int stance = 0; stance < limit; stance++) {
            if (pos == null) {
                pos = applyto.getPosition();
            }
            switch (sourceid) {
                case 重砲指揮官.雙胞胎猴子_1: {
                    pos = new Point(pos.x + stance * -90, pos.y);
                    break;
                }
                case 陰陽師.式神炎舞_1: {
                    pos = new Point(pos.x + (applyto.isFacingLeft() ? -100 : 100) * stance, pos.y);
                    break;
                }
                case 陰陽師.鬼神召喚: {
                    pos = new Point(pos.x + (stance == 0 ? -400 : 800), pos.y);
                    break;
                }
            }
            final MapleSummon summon = new MapleSummon(applyto, this, pos, movementType, duration, getRange(), mobOid, startTime);
            if (info.get(MapleStatInfo.hcSummonHp) > 0) {
                summon.setSummonHp(getX());
            }
            switch (sourceid) {
                case 陰陽師.鬼神召喚: {
                    summon.setStance(stance);
                    break;
                }
                case 神射手.幻像箭影: {
                    summon.setSummonHp(getX());
                    break;
                }
                case 重砲指揮官.雙胞胎猴子_1: {
                    if (stance == 1) {
                        summon.setShadow(重砲指揮官.雙胞胎猴子_設置強化);
                    }
                    break;
                }
                case 暗夜行者.SUMMON_暗影蝙蝠_召喚獸: {
                    summon.setShadow(applyto.getSkillLevel(暗夜行者.蝙蝠交流Ⅲ) > 0 ? 暗夜行者.蝙蝠交流Ⅲ : applyto.getSkillLevel(暗夜行者.蝙蝠交流Ⅱ) > 0 ? 暗夜行者.蝙蝠交流Ⅱ : applyto.getSkillLevel(暗夜行者.蝙蝠交流) > 0 ? 暗夜行者.蝙蝠交流 : 0);
                    break;
                }
                case 機甲戰神.磁場: {
//                    summon.a(applyto.getSummonsOIDsBySkillID(機甲戰神.磁場).size(), summon.getPosition());
                    break;
                }
                case 阿戴爾.乙太結晶: {
                    summon.setCurrentFh(0);
                    break;
                }
            }
            applyto.addSummon(summon);
            summon.setAnimated(1);
            applyto.getMap().spawnMapObject(-1, summon, null);
            summon.setAnimated(0);
            final List<Integer> summons = applyto.getSummonsOIDsBySkillID(機甲戰神.磁場);
            if (sourceid == 機甲戰神.磁場 && summons.size() >= 3) {
                applyto.getClient().announce(MaplePacketCreator.teslaTriangle(applyto.getId(), summons.get(0), summons.get(1), summons.get(2)));
            }
            if (sourceid == 陰陽師.鬼神召喚) {
                oidArray[stance] = summon.getObjectId();
                if (stance == 0) {
                    summonTeamPos.x = summon.getPosition().x;
                } else {
                    summonTeamPos.y = summon.getPosition().x;
                }
                if (stance == 1) {
                    applyto.getMap().broadcastMessage(applyto, SummonPacket.summonGost(applyto.getId(), oidArray[0], oidArray[1], level, summonTeamPos, (short) summon.getPosition().getY()), true);
                }
            }
            if (sourceid == 暗夜行者.暗影侍從) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(OutHeader.LP_SummonedAvatarSync.getValue());
                mplew.writeInt(summon.getOwnerId());
                if (summon.showCharLook()) {
                    summon.getOwner().getAvatarLook().encode(mplew, true);
                    applyto.getClient().announce(mplew.getPacket());
                }
            }
            if (sourceid == 菈菈.釋放_波瀾之江_1 || sourceid == 菈菈.釋放_波瀾之江_3) {
                int[] skills = new int[9];
                int[] list;
                switch (sourceid) {
                    case 菈菈.釋放_波瀾之江_3:
                        list = new int[]{菈菈.釋放_波瀾之江_4, 菈菈.釋放_波瀾之江_5};
                        break;
                    default:
                        list = new int[]{菈菈.釋放_波瀾之江_2};
                        break;
                }
                for (int i = 0; i < skills.length; i++) {
                    skills[i] = list[Randomizer.nextInt(list.length)];
                }
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

                mplew.writeShort(OutHeader.SUMMON_SKILLS.getValue());
                mplew.writeInt(sourceid);
                mplew.writeInt(skills.length);
                for (int skillid : skills) {
                    mplew.writeInt(skillid);
                }
                applyto.send(mplew.getPacket());
            }
            applyto.getSpecialStat().setMaelstromMoboid(0);
        }
        if (sourceid != 阿戴爾.乙太結晶) {
            applyto.sendEnableActions();
        }
    }

    /**
     * 回城卷處理
     */
    public boolean applyReturnScroll(MapleCharacter applyto) {
        if (moveTo == -1 || sourceid == -2031010 || sourceid == -2030021) {
            return false;
        }
        //applyto.getMap().getReturnMapId() != applyto.getMapId() || 暫時不要這個檢測
        //特別課程邀請信 騎士卷軸 這個貌似還是檢測不到 不管了
        MapleMap target = null;
        boolean nearest = false;
        if (moveTo == 999999999) {
            nearest = true;
            if (applyto.getMap().getReturnMapId() != 999999999) {
                target = applyto.getMap().getReturnMap();
            }
        } else {
            target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory().getMap(moveTo);
            if (target.getId() == 931050500 && target != applyto.getMap()) {
                applyto.changeMap(target, target.getPortal(0));
                return true;
            }
        }
        if (target == null || target == applyto.getMap() || nearest && applyto.getMap().isTown()) {
            return false;
        }
        applyto.changeMap(target, target.getPortal(0));
        return true;
    }

    public boolean is靈魂之石() {
        return isSkill() && sourceid == 22181003;
    }

    public void w(boolean bl2) {
        this.bxi = bl2;
    }

    public boolean jR() {
        if (lt == null || rb == null || !bxi) {
            return is靈魂之石();
        }
        return bxi;
    }

    public Rectangle getBounds() {
        return calculateBoundingBox(new Point(0, 0));
    }

    public Rectangle calculateBoundingBox(Point posFrom) {
        return calculateBoundingBox(posFrom, false);
    }

    public Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        return calculateBoundingBox(posFrom, facingLeft, 0);
    }

    public Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft, int addedRange) {
        return MapleStatEffectFactory.calculateBoundingBox(posFrom, facingLeft, lt, rb, info.get(MapleStatInfo.range) + addedRange);
    }

    public Rectangle getBounds2() {
        return calculateBoundingBox2(new Point(0, 0));
    }

    public Rectangle calculateBoundingBox2(Point posFrom) {
        return calculateBoundingBox2(posFrom, false);
    }

    public Rectangle calculateBoundingBox2(Point posFrom, boolean facingLeft) {
        return calculateBoundingBox2(posFrom, facingLeft, 0);
    }

    public Rectangle calculateBoundingBox2(Point posFrom, boolean facingLeft, int addedRange) {
        return MapleStatEffectFactory.calculateBoundingBox(posFrom, facingLeft, lt2, rb2, info.get(MapleStatInfo.range) + addedRange);
    }

    public Rectangle getBounds3() {
        return calculateBoundingBox3(new Point(0, 0));
    }


    public Rectangle calculateBoundingBox3(Point posFrom) {
        return calculateBoundingBox3(posFrom, false);
    }

    public Rectangle calculateBoundingBox3(Point posFrom, boolean facingLeft) {
        return calculateBoundingBox3(posFrom, facingLeft, 0);
    }

    public Rectangle calculateBoundingBox3(Point posFrom, boolean facingLeft, int addedRange) {
        return MapleStatEffectFactory.calculateBoundingBox(posFrom, facingLeft, lt3, rb3, info.get(MapleStatInfo.range) + addedRange);
    }

    public double getMaxDistance() { //lt = infront of you, rb = behind you; not gonna distance the two points since this is in relative to player position which is (0,0) and not both directions, just one
        int maxX = Math.max(Math.abs(lt == null ? 0 : lt.x), Math.abs(rb == null ? 0 : rb.x));
        int maxY = Math.max(Math.abs(lt == null ? 0 : lt.y), Math.abs(rb == null ? 0 : rb.y));
        return Math.pow(Math.pow(maxX, 2) + Math.pow(maxY, 2), (1d / 2));
    }

    /*
     * 切換頻道或者進入商城出來後 給角色BUFF 不需要發送封包
     */
    public void silentApplyBuff(MapleCharacter chr, long starttime, int localDuration, Map<SecondaryStat, Integer> statup, int chrId) {
        int maskedDuration = 0;
        int newDuration = (int) (starttime + localDuration - System.currentTimeMillis());
        if (sourceid == 火毒.魔力無限 || sourceid == 冰雷.魔力無限 || sourceid == 主教.魔力無限) {
            maskedDuration = alchemistModifyVal(chr, 4000, false);
        }
        chr.registerEffect(this, statup, 0, chrId, starttime, 0, maskedDuration > 0 ? maskedDuration : newDuration, new CancelEffectAction(chr, this, starttime, statup));
    }

    public void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, int newDuration) {
        applyBuffEffect(applyfrom, applyto, newDuration, primary, false, false, null);
    }

    public void applyBuffEffect(MapleCharacter applyfrom, int newDuration, boolean passive) {
        applyBuffEffect(applyfrom, applyfrom, newDuration, false, false, passive, null);
    }

    private int getHpMpChange(MapleCharacter applyfrom, boolean hpchange) {
        int change = 0;
        if (hpR != 0 || mpR != 0) {
            double healHpRate = hpchange ? hpR : mpR;
            if (applyfrom.isDebug()) {
                applyfrom.dropMessage(-5, (hpchange ? "[H" : "[M") + "P Rate]  Default: " + healHpRate);
            }
            int maxChange = healHpRate < 1.0 ? Math.min(49999, (int) Math.floor(99999 * healHpRate)) : 99999;
            int current = hpchange ? applyfrom.getStat().getCurrentMaxHP() : applyfrom.getStat().getCurrentMaxMP();
            change = Math.abs((int) (current * healHpRate)) > Math.abs(maxChange) ? maxChange : (int) (current * healHpRate);
        }
        return change;
    }

    public int alchemistModifyVal(MapleCharacter chr, int val, boolean withX) {
        Skill s = SkillFactory.getSkill(sourceid);
        if (s != null && s.isHyperSkill()) {
            return val;
        }
        if (isSkill()) {
            return val * (100 + (withX ? chr.getStat().skillRecoveryUP : chr.getStat().incBuffTime + (getSummonMovementType() == null ? 0 : chr.getStat().BuffUP_Summon))) / 100;
        }
        return val * (100 + (withX ? chr.getStat().itemRecoveryUP : chr.getStat().BuffUP)) / 100;
    }

    public void setLt(Point Lt) {
        lt = Lt;
    }

    public void setRb(Point Rb) {
        rb = Rb;
    }

    public void setLt2(Point Lt) {
        lt2 = Lt;
    }

    public void setRb2(Point Rb) {
        rb2 = Rb;
    }

    public void setLt3(Point Lt) {
        lt3 = Lt;
    }

    public void setRb3(Point Rb) {
        rb3 = Rb;
    }

    public Skill getSkill() {
        return SkillFactory.getSkill(sourceid);
    }

    public boolean isGmBuff() {
        switch (sourceid) {
            case 10001075: //Empress Prayer
            case 管理員.終極祝福: // GM dispel
            case 管理員.終極輕功: // GM haste
            case 管理員.終極祈禱: // GM Holy Symbol
            case 管理員.GM的祝福: // GM Bless
            case 管理員.復活: // GM resurrection
            case 管理員.hyper_body: // GM Hyper body

            case 9101000:
            case 9101001:
            case 9101002:
            case 9101003:
            case 9101005:
            case 9101008:
                return true;
            default:
                return JobConstants.is零轉職業(sourceid / 10000) && sourceid % 10000 == 1005;
        }
    }

    public boolean isInflation() {
        return inflation > 0;
    }

    public int getInflation() {
        return inflation;
    }

    public boolean ke() {
        switch (this.sourceid) {
            case 黑騎士.追隨者衝擊:
            case 火毒.火靈結界:
            case 主教.天怒:
            case 幻影俠盜.盜亦有道H:
            case 狂豹獵人.障礙:
            case 80001242: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public boolean isMonsterBuff() {
        switch (sourceid) {
            case 聖騎士.高貴威嚴:
            case 重砲指揮官.緊急退後:
            case 影武者.閃光彈:
            case 90001002:
            case 90001003:
            case 90001004:
            case 90001005:
            case 90001006:
            case 英雄.魔防消除:
            case 聖騎士.魔防消除:
            case 黑騎士.魔防消除:
            case 米哈逸.魔防消除:
            case 機甲戰神.輔助機器_H_EX:
            case 隱月.波浪拳波動:
            case 聖魂劍士.真實之眼:
            case 狂豹獵人.挑釁:
            case 主教.群體治癒:
                return isSkill();
        }
        return false;
    }

    /*
     * 是否組隊BUFF效果
     */
    public boolean isPartyBuff() {
        if (lt == null || rb == null) {
            return false;
        }
        return partyBuff;
    }

    public boolean isRangeBuff() {
        if (lt == null || rb == null) {
            return false;
        }
        return rangeBuff;
    }

    public boolean is幻影() {
        return isSkill() && sourceid == 暗夜行者.影幻;
    }

    public boolean is蓄能系統() {
        return isSkill() && sourceid == 傑諾.蓄能系統;
    }

    public int getHp() {
        return info.get(MapleStatInfo.hp);
    }

    public int getHpFX() {
        return info.get(MapleStatInfo.hpFX);
    }

    public int getMp() {
        return info.get(MapleStatInfo.mp);
    }

    public int getMpCon() {
        return info.get(MapleStatInfo.mpCon);
    }

    /**
     * 靈力消耗量
     *
     * @return 消耗量
     */
    public int getEpCon() {
        return info.get(MapleStatInfo.epCon);
    }

    /**
     * *
     * 持續秒數(每X秒造成傷害X)
     *
     * @return
     */
    public int getDotInterval() {
        return info.get(MapleStatInfo.dotInterval);
    }

    /**
     * *
     * 持續傷害重疊次數
     *
     * @return
     */
    public int getDOTStack() {
        return info.get(MapleStatInfo.dotSuperpos);
    }

    public double getHpR() {
        return hpR;
    }

    public double getMpR() {
        return mpR;
    }

    public int getMastery() {
        return info.get(MapleStatInfo.mastery);
    }

    public int getPad() {
        return info.get(MapleStatInfo.pad);
    }

    public int getPadR() {
        return info.get(MapleStatInfo.padR);
    }

    public int getMad() {
        return info.get(MapleStatInfo.mad);
    }

    public int getWdef() {
        return info.get(MapleStatInfo.pdd);
    }

    public int getWdef2Dam() {
        return info.get(MapleStatInfo.pdd2dam);
    }

    public int getMdef() {
        return info.get(MapleStatInfo.mdd);
    }

    /**
     * 增加命中力
     *
     * @return
     */
    public int getAcc() {
        return info.get(MapleStatInfo.acc);
    }

    public int getAcc2Dam() {
        return info.get(MapleStatInfo.acc2dam);
    }

    /**
     * 增加迴避值
     *
     * @return
     */
    public int getAvoid() {
        return info.get(MapleStatInfo.eva);
    }

    /**
     * 移動速度
     *
     * @return
     */
    public int getSpeed() {
        return info.get(MapleStatInfo.speed);
    }

    public int getJump() {
        return info.get(MapleStatInfo.jump);
    }

    /**
     * 最大移動速度提高
     *
     * @return
     */
    public int getSpeedMax() {
        return info.get(MapleStatInfo.speedMax);
    }

    /**
     * *
     * 移動速度提高或增加
     *
     * @return
     */
    public int getPsdSpeed() {
        return info.get(MapleStatInfo.psdSpeed);
    }

    /**
     * 跳躍力提高或者增加
     *
     * @return
     */
    public int getPsdJump() {
        return info.get(MapleStatInfo.psdJump);
    }

    /**
     * BUFF的持續時間
     */
    public int getDuration() {
        return info.get(MapleStatInfo.time);
    }

    public int getBuffDuration(MapleCharacter applyfrom) {
        if (this instanceof MobSkill) {
            return calcDebuffBuffDuration(getDuration(), applyfrom);
        }
        if (getSummonMovementType() != null) {
            return getSummonDuration(applyfrom);
        }
        return calcBuffDuration(getDuration(), applyfrom);
    }

    public int calcBuffDuration(int duration, MapleCharacter applyfrom) {
        if (getSummonMovementType() != null) {
            return calcSummonDuration(duration, applyfrom);
        }
        int time = 0;
        if (duration < 2100000000) {
            if (isSkill()) {
                Skill skill = SkillFactory.getSkill(sourceid);
                // 解放的輪之力
                if (80002280 == sourceid) {
                    MapleStatEffect effect = applyfrom.getSkillEffect(龍魔導士.輪之堅持);
                    if (effect == null || !JobConstants.is龍魔導士(applyfrom.getJob())) {
                        effect = applyfrom.getSkillEffect(龍魔導士.輪之堅持_傳授);
                    }
                    if (effect != null) {
                        time += (int) Math.floor(duration * effect.getX() / 100.0D);
                    }
                } else if (skill != null && !notIncBuffDuration && (米哈逸.光之守護_傳授 == sourceid || skill.canBeLearnedBy(applyfrom.getJobWithSub())) && !skill.isHyperSkill() && !skill.isVSkill()) {
                    time += duration * applyfrom.getStat().incBuffTime / 100;
                }
                time += applyfrom.getStat().getDuration(sourceid);
            }
        }
        return duration + time;
    }

    public int calcDebuffBuffDuration(int duration, MapleCharacter applyfrom) {
        if (duration == 2100000000 || statups.containsKey(SecondaryStat.Lapidification)) {
            return duration;
        }
        return duration * Math.max(100 - Math.min(70, applyfrom.getStat().asrR * 6 / 10 + 25), 10) / 100;
    }

    public int getSummonDuration(MapleCharacter applyfrom) {
        return calcSummonDuration(getDuration(), applyfrom);
    }

    public int calcSummonDuration(int duration, MapleCharacter applyfrom) {
        int time = 0;
        if (duration < 2100000000) {
            if (isSkill()) {
                Skill skill = SkillFactory.getSkill(sourceid);
                if (skill != null && !notIncBuffDuration && skill.canBeLearnedBy(applyfrom.getJobWithSub()) && !skill.isHyperSkill() && !skill.isVSkill()) {
                    time += duration * applyfrom.getStat().summonTimeR / 100;
                }
                time += applyfrom.getStat().getDuration(sourceid);
            }
        }
        return duration + time;
    }

    public void setDebuffTime(int time) {
        debuffTime = time;
    }

    public int getDebuffTime() {
        if (debuffTime > 0) {
            return debuffTime;
        } else {
            return getDuration();
        }
    }

    public int getMobDebuffDuration(MapleCharacter applyfrom) {
        int duration = debuffTime;
        if (duration <= 0) {
            duration = getDotTime(applyfrom);
            if (duration <= 300) {
                duration *= 1000;
            }
            if (duration <= 0) {
                duration = getDuration();
            } else {
                return duration;
            }
        }
        return calcMobDebuffDuration(duration, applyfrom);
    }

    public int calcMobDebuffDuration(int duration, MapleCharacter applyfrom) {
        return duration;
    }

    /**
     * 對怪物BUFF的持續時間
     */
    public int getSubTime() {
        return info.get(MapleStatInfo.subTime);
    }

    /**
     * 是否BUFF狀態技能
     */
    public boolean isOverTime() {
        return overTime;
    }

    /**
     * 不會被取消的BUFF
     */
    public boolean isNotRemoved() {
        return notRemoved;
    }

    public EnumMap<SecondaryStat, Integer> getStatups() {
        return statups;
    }

    public EnumMap<SecondaryStat, Integer> getWriteStatups() {
        return statups.keySet().parallelStream().collect(Collectors.toMap(it -> it, it -> getSourceId(), (a, b) -> b, () -> new EnumMap<>(SecondaryStat.class)));
    }

    /**
     * BUFF狀態是否是同1個技能裡面的
     */
    public boolean sameSource(MapleStatEffect effect) {
        return effect != null && (/*SkillConstants.getLinkedAttackSkill(effect.sourceid) == sourceid || */sourceid == effect.sourceid) && isSkill() == effect.isSkill();
    }

    public int getQ() {
        return info.get(MapleStatInfo.q);
    }

    public int getQ2() {
        return info.get(MapleStatInfo.q2);
    }

    public int getS() {
        return info.get(MapleStatInfo.s);
    }

    public int getS2() {
        return info.get(MapleStatInfo.s2);
    }

    public int getT() {
        return info.get(MapleStatInfo.t);
    }

    public int getU() {
        return info.get(MapleStatInfo.u);
    }

    public int getV() {
        return info.get(MapleStatInfo.v);
    }

    public int getW() {
        return info.get(MapleStatInfo.w);
    }

    public int getW2() {
        return info.get(MapleStatInfo.w2);
    }

    public int getX() {
        return info.get(MapleStatInfo.x);
    }

    public int getY() {
        return info.get(MapleStatInfo.y);
    }

    public int getZ() {
        return info.get(MapleStatInfo.z);
    }

    public int getDamage() {
        if (sourceid == 狂豹獵人.美洲豹靈魂) {
            return info.get(MapleStatInfo.y);
        }
        return info.get(MapleStatInfo.damage);
    }

    public int getMadR() {
        return info.get(MapleStatInfo.madR);
    }

    public int getPVPDamage() {
        return info.get(MapleStatInfo.PVPdamage);
    }

    /*
     * 獲取技能攻擊次數
     */
    public int getAttackCount() {
        return info.get(MapleStatInfo.attackCount);
    }

    /*
     * 獲取技能攻擊次數 + 額外增加次數
     */
    public int getAttackCount(MapleCharacter applyfrom) {
        return info.get(MapleStatInfo.attackCount) + applyfrom.getStat().getAttackCount(sourceid);
    }

    /**
     * *
     * 攻擊次數
     *
     * @return
     */
    public int getBulletCount() {
        return info.get(MapleStatInfo.bulletCount);
    }

    public int getBulletCount(MapleCharacter applyfrom) {
        return info.get(MapleStatInfo.bulletCount) + applyfrom.getStat().getSkillBulletCount(sourceid);
    }

    /*
     * 使用技能消耗子彈/飛鏢多少發
     */
    public int getBulletConsume() {
        return info.get(MapleStatInfo.bulletConsume);
    }

    /**
     * 攻擊怪物個數
     *
     * @return
     */
    public int getMobCount() {
        return info.get(MapleStatInfo.mobCount);
    }

    /*
     * 獲取技能攻擊怪物的數量 + 額外增加數量
     */
    public int getMobCount(MapleCharacter applyfrom) {
        return info.get(MapleStatInfo.mobCount) + applyfrom.getStat().getMobCount(sourceid);
    }

    public int getMoneyCon() {
        return moneyCon;
    }

    /**
     * 冷卻時間(減少)
     *
     * @return
     */
    public int getCooltimeReduceR() {
        return info.get(MapleStatInfo.coolTimeR);
    }

    /*
     * 楓幣獲得量增加x%
     */
    public int getMesoAcquisition() {
        return info.get(MapleStatInfo.mesoR);
    }

    /*
     * 獲取技能的冷卻時間
     */
    public int getCooldown() {
        int cooldown = info.get(MapleStatInfo.cooltime) * 1000;
        if (cooldown <= 0 && info.get(MapleStatInfo.cooltimeMS) > 0) {
            cooldown = info.get(MapleStatInfo.cooltimeMS);
        }
        return cooldown;
    }

    public int getCooldown(MapleCharacter applyfrom) {
        Skill skill = SkillFactory.getSkill(sourceid);
        int cooldown = getCooldown();
        if (!isSkill() || skill == null) {
            return cooldown;
        }
        Skill sourceSkill = null;
        int linkId = SkillConstants.getCooldownLinkSourceId(sourceid);
        if (linkId != sourceid) {
            sourceSkill = skill;
            skill = SkillFactory.getSkill(linkId);
            cooldown = skill.getEffect(getLevel()).getCooldown();
        }
        if (applyfrom != null) {
            if (黑騎士.轉生_狀態 == sourceid) {
                SecondaryStatValueHolder mbsvh = applyfrom.getBuffStatValueHolder(SecondaryStat.ReincarnationOnOff);
                if (mbsvh != null && mbsvh.effect != null) {
                    MapleStatEffect effect = applyfrom.getSkillEffect((mbsvh.effect.getSourceId() - 1000) + mbsvh.value);
                    if (effect != null) {
                        return effect.getCooldown(applyfrom);
                    }
                }
            } else if (is平衡技能() && (applyfrom.getBuffSource(SecondaryStat.Larkness) == 夜光.重啟平衡 || applyfrom.getBuffSource(SecondaryStat.Larkness) == 夜光.平衡_光明)) { //平衡狀態下無冷卻時間
                return 0;
            } else if (sourceid == 箭神.箭座 || (applyfrom.getBuffedValue(SecondaryStat.StrikerHyperElectric) != null && (sourceid == 閃雷悍將.疾風 || sourceid == 閃雷悍將.颱風))) {
                return 0;
            }
            if (!skill.isNotCooltimeReduce() && !skill.isNotCooltimeReset() && Randomizer.isSuccess(applyfrom.getStat().nocoolProp)) {
                if (applyfrom.isDebug()) {
                    String sb = skill + " 冷卻時間 => 默認: " + cooldown + "ms" + cooldown + "  [內潛: 依 " + applyfrom.getStat().nocoolProp + "%機率, 沒有冷卻時間" +
                            " 最終時間: " + 0 + "ms";
                    applyfrom.dropMessage(-5, sb);
                }
                return 0;
            }
            if (JobConstants.is幻影俠盜(applyfrom.getJob())) {
                int cooltime = SkillConstants.getStolenHyperSkillColltime(skill.getId()) * 1000;
                if (skill.getId() == 幻影俠盜.幻影斗蓬) {
                    int count = 0;
                    if (applyfrom.getTempValues().get("skill" + skill.getId()) != null) {
                        count = Integer.parseInt(applyfrom.getTempValues().get("skill" + skill.getId()).toString());
                    }
                    cooldown = getCooldown() * count;
                }
            } else if (cooldown > 0) {
                StringBuilder sb = new StringBuilder(skill + " 冷卻時間 => 默認: " + cooldown + "ms");
                // 技能減少CD
                cooldown *= (100 - applyfrom.getStat().getReduceCooltimeRate(skill.getId())) / 100.0;
                if (applyfrom.getStat().getReduceCooltimeRate(skill.getId()) > 0) {
                    sb.append(" [超級技能減少: ").append(applyfrom.getStat().getReduceCooltimeRate(skill.getId())).append("%]");
                }
                // 精靈遊俠戰地聯盟卡
                if (applyfrom.getStat().getCooltimeReduceR() > 0 && cooldown > 1000) {
                    cooldown *= (100 - applyfrom.getStat().getCooltimeReduceR()) / 100.0;
                    cooldown = Math.max(1000, cooldown);
                    sb.append(" [楓之谷聯盟減少: ").append(applyfrom.getStat().getCooltimeReduceR()).append("%]");
                }
                // 裝備減少CD
                if (!skill.isHyperSkill()) {
                    int equipReduceCoolTime = applyfrom.getStat().getReduceCooltime();
                    if (equipReduceCoolTime > 0) {
                        boolean hasReduceCoolTime = false;
                        if (cooldown > 10000) {
                            int time = (int) Math.min(Math.ceil((cooldown - 10000) / 1000.0), equipReduceCoolTime);
                            cooldown -= time * 1000;
                            equipReduceCoolTime -= time;
                            sb.append(" [裝備減少時間: ").append(time).append("秒]");
                            hasReduceCoolTime = true;
                        }
                        if (cooldown > 5000 && equipReduceCoolTime > 0) {
                            cooldown *= (100 - equipReduceCoolTime * 5) / 100.0;
                            sb.append(" [裝備減少時間: ").append(equipReduceCoolTime * 5).append("%]");
                            hasReduceCoolTime = true;
                        }
                        if (hasReduceCoolTime) {
                            cooldown = Math.max(5000, cooldown);
                        }
                    }
                }
                if (applyfrom.isDebug()) {
                    sb.append(" 最終時間: ").append(cooldown).append("ms");
                    applyfrom.dropMessage(-5, sb.toString());
                }
            }
            int fixCoolTime;
            if (!skill.isVSkill() && (fixCoolTime = applyfrom.getBuffedIntValue(SecondaryStat.FixCoolTime)) > 0 && cooldown > fixCoolTime) {
                cooldown = fixCoolTime;
            }
        }
        return cooldown;
    }

    public Map<MonsterStatus, Integer> getMonsterStatus() {
        return monsterStatus;
    }

    public int getBerserk() {
        return berserk;
    }

    public boolean is必殺狙擊() {
        return isSkill() && sourceid == 神射手.必殺狙擊;
    }

    public boolean is平衡技能() {
        return isSkill() && (sourceid == 夜光.死神鐮刀 || sourceid == 夜光.絕對擊殺);
    }

    public boolean is潛入() {
        return isSkill() && (sourceid == 20021001 || sourceid == 20031001 || sourceid == 市民.潛入 || sourceid == 30011001 || sourceid == 30021001 || sourceid == 60001001 || sourceid == 60011001);
    }

    public boolean is魔力無限() {
        return isSkill() && (sourceid == 火毒.魔力無限 || sourceid == 冰雷.魔力無限 || sourceid == 主教.魔力無限);
    }

    public boolean is騎乘技能_() {
        return isSkill() && (SkillConstants.is騎乘技能(sourceid) || sourceid == 80001000 || SkillFactory.getMountLinkId(sourceid) > 0);
    }

    public boolean is騎乘技能() {
        return isSkill() && (is騎乘技能_() || GameConstants.getMountItem(sourceid, null) != 0) && !is合金盔甲();
    }

    public boolean is時空門() {
        return sourceid == 主教.時空門 || sourceid == 通用V核心.實用的時空門 || sourceid % 10000 == 8001;
    }

    public boolean is鬥氣爆發() {
        return isSkill() && sourceid == 英雄.鬥氣爆發;
    }

    public boolean is惡魔衝擊() {
        return isSkill() && sourceid == 惡魔殺手.惡魔衝擊;
    }

    public boolean isCharge() {
        switch (sourceid) {
            case 狂狼勇士.寒冰屬性:
                return isSkill();
        }
        return false;
    }

    public boolean isPoison() {
        return info.get(MapleStatInfo.dot) > 0 && info.get(MapleStatInfo.dotTime) > 0;
    }

    /*
     * 是否為煙霧效果
     */
    private boolean isMist() {
        switch (sourceid) {
            case 卡蒂娜.鏈之藝術_漩渦:
            case 龍魔導士.迅捷_回來吧_1:
            case 神之子.進階碎地猛擊:
            case 亞克.迷惑之拘束_1:
            case 1076: //0001076 - 奧茲的火牢術屏障 - 召喚的奧茲在一定時間內在自身周圍形成火幕。火幕內的怪物有一定概率處於著火狀態，持續受到傷害。特定等級提升時，技能等級可以提升1。
            case 火毒.燎原之火_MIST:
            case 火毒.致命毒霧:
            case 主教.神聖之泉:
            case 夜使者.絕對領域:
            case 暗影神偷.煙幕彈:
            case 12111005:
            case 煉獄巫師.魔法屏障:
            case 隱月.束縛術:
            case 烈焰巫師.燃燒軍團:
            case 機甲戰神.輔助機器_H_EX:
            case 機甲戰神.輔助機器強化:
            case 狂豹獵人.連弩陷阱:
            case 狂豹獵人.鑽孔集裝箱:
            case 陰陽師.靈脈的氣息:
            case 陰陽師.陰陽除靈符:
            case 陰陽師.結界_櫻:
            case 陰陽師.結界_鈴蘭:
            case 狂狼勇士.瑪哈的領域_MIST:
            case 重砲指揮官.ICBM_3:
            case 破風使者.SUMMON_翡翠花園:
            case 破風使者.強化翡翠花園:
            case 14111006:
            case 22161003:
            case 龍魔導士.氣息_回來吧:
            case 隱月.銷魂屏障:
            case 隱月.精靈的化身_1:
            case 35121010:
            case 機甲戰神.扭曲領域:
            case 傑諾.時空膠囊:
            case 爆拳槍神.錘之碎擊_1:
            case 米哈逸.閃光交叉_安裝:
            case 凱撒.龍烈焰:
            case 凱撒.飛劍風暴:
            case 80001431:
            case 神之子.時間扭曲:
            case 皮卡啾.博拉多利:
            case 皮卡啾.帕拉美:
            case 皮卡啾.愛美麗:
            case 伊利恩.朗基努斯領域:
            case 海盜.海盜旗幟:
            case 惡魔復仇者.惡魔狂亂_魔族之血:
            case 凱撒.意志之劍_重磅出擊_2:
            case 凱撒.意志之劍_重磅出擊_3:
            case 冰雷.冰河紀元:
            case 冰雷.落雷凝聚_1:
            case 冰雷.落雷凝聚_2:
            case 冰雷.冰雪結界_1:
            case 龍魔導士.歐尼斯之氣息:
            case 龍魔導士.粉碎_回歸:
            case 龍魔導士.元素滅殺破:
            case 夜光.混沌共鳴:
            case 夜光.混沌共鳴_1:
            case 夜光.混沌共鳴_2:
            case 箭神.箭雨_1:
            case 狂豹獵人.豹魂嵐擊:
            case 重砲指揮官.ICBM_2:
            case 墨玄.絕技_神玄武極:
            case 菈菈.神木:
                return true;
        }
        return false;
    }

    private boolean is楓葉淨化() {
        switch (sourceid) {
            case 英雄.楓葉淨化:
            case 聖騎士.楓葉淨化:
            case 黑騎士.楓葉淨化:
            case 火毒.楓葉淨化:
            case 冰雷.楓葉淨化:
            case 主教.楓葉淨化:
            case 箭神.楓葉淨化:
            case 神射手.楓葉淨化:
            case 夜使者.楓葉淨化:
            case 暗影神偷.楓葉淨化:
            case 拳霸.楓葉淨化:
            case 槍神.楓葉淨化:
            case 狂狼勇士.楓葉淨化:
            case 龍魔導士.楓葉淨化:
            case 影武者.楓葉淨化:
            case 煉獄巫師.楓葉淨化:
            case 狂豹獵人.楓葉淨化:
            case 機甲戰神.楓葉淨化:
            case 爆拳槍神.楓葉淨化:
            case 重砲指揮官.楓葉淨化:
            case 精靈遊俠.勇士的意志:
            case 幻影俠盜.楓葉淨化:
            case 夜光.楓葉淨化:
            case 傑諾.楓葉淨化:
            case 凱撒.超新星勇士意志:
            case 凱殷.超新星勇士的意志:
            case 天使破壞者.超新星勇士的意志:
            case 劍豪.曉月意志:
            case 陰陽師.曉月櫻花:
            case 凱內西斯.精神淨化:
            case 神射手.止痛藥:
            case 凱撒.超新星的意志:
            case 神之子.時間扭曲:
            case 通用V核心.艾爾達斯的意志:
            case 開拓者.楓葉淨化:
            case 虎影.阿尼瑪勇士的意志:
            case 阿戴爾.雷普勇士的意志:
            case 卡莉.雷普勇士的意志:
                return isSkill();
        }
        return false;
    }

    public boolean is矛之鬥氣() {
        return isSkill() && sourceid == 狂狼勇士.矛之鬥氣;
    }

    public boolean isMorph() {
        return morphId > 0;
    }

    public int getMorph() {
        return morphId;
    }

    public boolean is凱撒終極型態() {
        return isSkill() && (sourceid == 凱撒.終極型態 || sourceid == 凱撒.進階終極形態);
    }

    public boolean is凱撒超終極型態() {
        return isSkill() && sourceid == 凱撒.超_終極型態;
    }

    public boolean is超越攻擊狀態() {
        switch (sourceid) {
            case 惡魔復仇者.超越_十文字斬:
            case 惡魔復仇者.超越_惡魔風暴:
            case 惡魔復仇者.超越_月光斬:
            case 惡魔復仇者.超越_逆十文字斬:
                return isSkill();
        }
        return false;
    }

    public int getMorph(MapleCharacter chr) {
        int morph = getMorph();
        switch (morph) {
            case 1000:
            case 1001:
            case 1003:
                return morph + (chr.getGender() == 1 ? 100 : 0);
        }
        return morph;
    }

    public int getLevel() {
        return level;
    }

    public boolean isSummonSkill() {
        Skill summon = SkillFactory.getSkill(sourceid);
        return isSkill() && summon != null && summon.isSummonSkill();
    }

    public SummonMovementType getSummonMovementType() {
        if (!isSkill() || !isSummonSkill()) {
            return null;
        }
        if (this.sourceid == 通用V核心.弓箭手通用.追蹤箭頭 && this.sourceid == 破風使者.追蹤箭頭) {
            return null;
        }
        if (is戒指技能()) {
            return SummonMovementType.WALK; //1
        }
        switch (sourceid) {
            case 11111029:
            case 3111002:
            case 3120012:
            case 3211002:
            case 3220012:
            case 5211001:
            case 5220002:
            case 5220023:
            case 5220024:
            case 5220025:
            case 13111004:
            case 33101008:
            case 35111005:
            case 35111011:
            case 51121016:
            case 131001007:
            case 131001022:
            case 131001025:
            case 131002022:
            case 131003022:
            case 131003023:
            case 131004022:
            case 131004023:
            case 131005022:
            case 131005023:
            case 131006022:
            case 131006023:
            case 400001022:
            case 400001039:
            case 400001064:
            case 400021069:
            case 400021071:
            case 400021073:
            case 400021095:
            case 400041044:
            case 154110010:
            case 暗夜行者.Switch_type_Magic_暗影蝙蝠:
            case 暗夜行者.影幻:
                return null;
            case 火毒.劇毒領域:
            case 冰雷.閃電球_1:
            case 主教.天使之泉:
            case 箭神.回歸箭筒:
            case 神射手.回歸之箭:
            case 神射手.幻像箭影:
            case 暗影神偷.絕殺領域:
            case 夜使者.絕殺領域:
            case 夜使者.達克魯的秘傳:
            case 影武者.幻影替身:
            case 槍神.攻城轟炸機:
            case 槍神.海盜砲擊艇_1:
            case 槍神.砲艇標記:
            case 槍神.海盜砲擊艇:
            case 重砲指揮官.磁錨:
            case 重砲指揮官.雙胞胎猴子:
            case 重砲指揮官.雙胞胎猴子_1:
            case 重砲指揮官.滾動彩虹加農砲:
            case 烈焰巫師.漩渦:
            case 破風使者.SUMMON_翡翠花園:
            case 破風使者.強化翡翠花園:
            case 14111010:
            case 暗夜行者.SUMMON_ATTACK_闇黑天魔:
            case 卡莉.藝術_阿斯特拉:
            case 卡莉.藝術_阿斯特拉_1:
            case 卡莉.死亡綻放:
            case 22171052:
            case 龍魔導士.聖歐尼斯龍:
            case 33111003:
            case 機甲戰神.機甲大砲_RM7:
            case 機甲戰神.磁場:
            case 機甲戰神.輔助機器_H_EX:
            case 機甲戰神.輔助機器強化:
            case 機甲戰神.戰鬥機器_巨人錘:
            case 機甲戰神.機器人工廠_RM1:
            case 35121010:
            case 傑諾.能量領域_貫通:
            case 傑諾.能量領域_力場:
            case 傑諾.能量領域_支援:
            case 陰陽師.鬼夜叉_老么:
            case 陰陽師.鬼夜叉_二哥:
            case 陰陽師.式神炎舞_1:
            case 陰陽師.鬼神召喚:
            case 陰陽師.鬼夜叉_大哥:
            case 陰陽師.鬼夜叉_老大:
            case 凱撒.地龍襲擊:
            case 凱撒.石化:
            case 80002230:
            case 80002888:
            case 80002889:
            case 皮卡啾.全員集合:
            case 皮卡啾.博拉多利:
            case 皮卡啾.愛美麗:
            case 阿戴爾.乙太結晶:
            case 通用V核心.反抗軍通用.末日反抗步兵陣:
            case 通用V核心.曉之陣通用.集結曉之陣_上杉謙信:
            case 通用V核心.曉之陣通用.集結曉之陣_安倍晴明:
            case 通用V核心.曉之陣通用.集結曉之陣_菖蒲:
            case 通用V核心.曉之陣通用.集結曉之陣_武田信玄:
            case 惡魔殺手.耶夢加得:
            case 聖魂劍士.ATTACK_極樂之境_II:
            case 夜光.真理之門:
            case 煉獄巫師.黑魔祭壇:
            case 阿戴爾.劍域:
            case 80011261: // 輪迴
            case 500001001: //雅努斯 黃昏
            case 500001002: //雅努斯 清晨 X3
            case 500001003: //雅努斯
            case 500001004: //雅努斯
            case 阿戴爾.劍域_持續:
            case 陰陽師.靈石召喚:
            case 伊利恩.神怒寶劍:
            case 卡蒂娜.召喚_AD大砲:
            case 天使破壞者.能量爆炸:
            case 機甲戰神.微型導彈箱:
            case 隱月.鬼武陣:
            case 虎影.歪曲縮地符:
            case 虎影.歪曲縮地符_向門傳送:
            case 虎影.卷術_吸星渦流:
            case 虎影.卷術_山靈召喚:
            case 冰雷.冰雪之精神:
            case 開拓者.遺跡解放_釋放:
            case 開拓者.遺跡解放_爆破:
            case 開拓者.遺跡解放_轉移:
            case 菈菈.釋放_波瀾之江_1:
            case 菈菈.釋放_旋風_1:
            case 菈菈.釋放_波瀾之江_3:
            case 菈菈.釋放_旋風_3:
            case 英雄.劍士意念_1:
                return SummonMovementType.STOP;
            case 隱月.鬼武陣_1:
                return SummonMovementType.固定一段距離;
            case 暗夜行者.SUMMON_暗影蝙蝠_召喚獸:
            case 精靈遊俠.元素騎士:
            case 精靈遊俠.元素騎士1:
            case 精靈遊俠.元素騎士2:
            case 33101011:
            case 陰陽師.雙天狗_左:
            case 陰陽師.雙天狗_右:
            case 皮卡啾.迷你啾出動_1:
            case 伊利恩.瑪奇納:
            case 開拓者.渡鴉召喚:
            case 虎影.追擊鬼火符:
                return SummonMovementType.FLY;
            case 機甲戰神.機器人工廠_機器人:
            case 菈菈.山之種子:

                return SummonMovementType.WALK_RANDOM;
            case 黑騎士.追隨者:
            case 火毒.召喚火魔:
            case 冰雷.閃電球:
            case 冰雷.召喚冰魔:
            case 主教.召喚聖龍:
            case 主教.天秤之使:
            case 主教.天秤之使_1:
            case 箭神.召喚鳳凰:
            case 神射手.召喚銀隼:
            case 烈焰巫師.SUMMON_元素火焰:
            case 烈焰巫師.Return_元素之炎II_1:
            case 烈焰巫師.Return_元素之炎III_1:
            case 烈焰巫師.Return_元素之炎IV_1:
            case 12001004:
            case 烈焰巫師.SUMMON_元素火焰II:
            case 烈焰巫師.SUMMON_元素火焰III:
            case 12111004:
            case 烈焰巫師.SUMMON_元素火焰IV:
            case 14001005:
            case 煉獄巫師.死神:
            case 煉獄巫師.死神契約I:
            case 煉獄巫師.死神契約II:
            case 煉獄巫師.死神契約III:
            case 35111001:
            case 35111009:
            case 35111010:
            case 伊利恩.里幽:
            case 伊利恩.水晶技能_德烏斯:
            case 弓箭手.演變:
            case 惡魔.召喚喵怪仙人:
            case 英雄.燃燒靈魂之劍:
            case 陰陽師.雪女招喚_1:
            case 陰陽師.鬼夜叉_大鬼封魂陣:
            case 神射手.分裂之矢_1:
            case 機甲戰神.多重屬性_M_FL:
            case 虎影.仙技_降臨怪力亂神:
                return SummonMovementType.WALK;
            case 暗夜行者.Skill_暗影僕從:
            case 暗夜行者.影幻_40:
            case 暗夜行者.影幻_20:
            case 聖魂劍士.日月星爆:
                return SummonMovementType.WALK_CLONE;
            case 狂豹獵人.召喚美洲豹_銀灰:
            case 狂豹獵人.召喚美洲豹_暗黃:
            case 狂豹獵人.召喚美洲豹_血紅:
            case 狂豹獵人.召喚美洲豹_紫光:
            case 狂豹獵人.召喚美洲豹_深藍:
            case 狂豹獵人.召喚美洲豹_傑拉:
            case 狂豹獵人.召喚美洲豹_白雪:
            case 狂豹獵人.召喚美洲豹_歐尼斯:
            case 狂豹獵人.召喚美洲豹_地獄裝甲:
                return SummonMovementType.JAGUAR;
            case 神之子.武器投擲:
            case 神之子.進階武器投擲:
            case 凱撒.超新星守護者:
            case 凱撒.超新星守護者_1:
            case 凱撒.超新星守護者_2:
                return SummonMovementType.FIX_V_MOVE;
            case 陰陽師.雙天狗_隱藏:
            case 伊利恩.古代水晶:
            case 英雄.燃燒靈魂之劍_1:
            case 暗夜行者.暗影侍從:
                return SummonMovementType.固定跟隨攻擊;
            case 重砲指揮官.特種猴子部隊:
            case 重砲指揮官.特種猴子部隊_2:
            case 重砲指揮官.特種猴子部隊_3:
                return SummonMovementType.UNKNOWN_16;
            case 機甲戰神.巨型航母:
                return SummonMovementType.UNKNOWN_17;
            default:
                return SummonMovementType.WALK_SMART;

        }
    }

    public boolean is戒指技能() {
        return SkillConstants.is召喚獸戒指(sourceid);
    }

    public boolean isSkill() {
        return sourceid >= 0;
    }

    public boolean is合金盔甲() {
        return isSkill() && (sourceid == 機甲戰神.合金盔甲_人型 || sourceid == 機甲戰神.合金盔甲_戰車);
    }

    public boolean is召喚美洲豹() {
        return isSkill() && SkillConstants.is美洲豹(sourceid);
    }

    public boolean is拔刀姿勢() {
        return isSkill() && sourceid == 劍豪.拔刀姿勢;
    }

    /**
     * 機率計算結果，根據隨機數據對比得到結果
     *
     * @return true ? 執行 : 不執行
     */
    public boolean makeChanceResult() {
        return getProp() >= 100 || Randomizer.nextInt(100) < getProp();
    }

    public boolean makeChanceResult(MapleCharacter chr) {
        return Randomizer.nextInt(100) < getProp(chr);
    }

    /**
     * 機率值
     *
     * @return 值
     */
    public int getProp() {
        return info.get(MapleStatInfo.prop);
    }

    public int getProp(MapleCharacter chr) {
        int prop = getProp();
//        int fixProp = prop + (100 - prop) * chr.getStat().getAddSkillProp(sourceid) / 100;
        return prop + chr.getStat().getAddSkillProp(sourceid);
    }

    /**
     * 額外機率
     *
     * @return
     */
    public int getSubProp() {
        return info.get(MapleStatInfo.subProp);
    }

    /*
     * 無視怪物防禦
     */
    public int getIgnoreMobpdpR() {
        return info.get(MapleStatInfo.ignoreMobpdpR);
    }

    /*
     * 增加Hp
     */
    public int getEnhancedHP() {
        return info.get(MapleStatInfo.emhp);
    }

    /*
     * 增加Mp
     */
    public int getEnhancedMP() {
        return info.get(MapleStatInfo.emmp);
    }

    /*
     * 增加物理攻擊
     */
    public int getEnhancedWatk() {
        return info.get(MapleStatInfo.epad);
    }

    /*
     * 增加魔法攻擊
     */
    public int getEnhancedMatk() {
        return info.get(MapleStatInfo.emad);
    }

    /*
     * 增加物理防禦
     */
    public int getEnhancedWdef() {
        return info.get(MapleStatInfo.epdd);
    }

    /*
     * 增加魔法防禦
     */
    public int getEnhancedMdef() {
        return info.get(MapleStatInfo.emdd);
    }

    /**
     * *
     * 持續傷害%比
     *
     * @return
     */
    public int getDot() {
        return info.get(MapleStatInfo.dot);
    }

    /**
     * ***
     * 持續總時間
     *
     * @return
     */
    public int getDotTime() {
        return info.get(MapleStatInfo.dotTime);
    }

    public int getDotTime(MapleCharacter chr) {
        int dotTime = getDotTime() + chr.getStat().getDotTime(sourceid);
        return dotTime + dotTime * chr.getStat().incDotTime / 100;
    }

    /*
     * 爆擊概率
     */
    public int getCritical() {
        return info.get(MapleStatInfo.cr);
    }

    /*
     * 爆擊最大傷害
     */
    public int getCriticalMax() {
        return info.get(MapleStatInfo.criticaldamageMax);
    }

    /*
     * 爆擊最小傷害
     */
    public int getCriticalMin() {
        return info.get(MapleStatInfo.criticaldamageMin);
    }

    /*
     * 命中增加 x%
     */
    public int getArRate() {
        return info.get(MapleStatInfo.ar);
    }

    public int getASRRate() {
        return info.get(MapleStatInfo.asrR);
    }

    public int getTERRate() {
        return info.get(MapleStatInfo.terR);
    }

    /*
     * 攻擊傷害提高 百分比
     */
    public int getDamR() {
        return info.get(MapleStatInfo.damR);
    }

    public int getDamPlus() {
        return info.get(MapleStatInfo.damPlus);
    }

    /*
     * 攻擊傷害提高 百分比
     */
    public int getDAMRate_5th() {
        return info.get(MapleStatInfo.damR_5th);
    }

    /*
     * 魔攻傷害提高 百分比
     */
    public int getMdR() {
        return info.get(MapleStatInfo.mdR);
    }

    /*
     * 攻擊傷害提高 百分比
     */
    public int getPdR() {
        return info.get(MapleStatInfo.pdR);
    }

    /*
     * 楓幣獲得量增加x%
     */
    public short getMesoRate() {
        return mesoR;
    }

    public int getEXP() {
        return exp;
    }

    /*
     * 物理防禦力的x%追加到魔法防禦力
     */
    public int getWdefToMdef() {
        return info.get(MapleStatInfo.pdd2mdd);
    }

    /*
     * 魔法防禦力的x%追加到物理防禦力
     */
    public int getMdefToWdef() {
        return info.get(MapleStatInfo.mdd2pdd);
    }

    /*
     * 迴避值提升HP上限 - HP上限增加迴避值的x%
     */
    public int getAvoidToHp() {
        return info.get(MapleStatInfo.eva2hp);
    }

    /*
     * 命中值提升MP上限 - MP上限增加命中值的x%
     */
    public int getAccToMp() {
        return info.get(MapleStatInfo.acc2mp);
    }

    /*
     * 力量提升敏捷 - 投資了AP力量的x%追加到敏捷
     */
    public int getStrToDex() {
        return info.get(MapleStatInfo.str2dex);
    }

    /*
     * 敏捷提升力量 - 投資了AP敏捷的x%追加到力量
     */
    public int getDexToStr() {
        return info.get(MapleStatInfo.dex2str);
    }

    /*
     * 智力提升幸運 - 投資了AP智力的x%追加到幸運
     */
    public int getIntToLuk() {
        return info.get(MapleStatInfo.int2luk);
    }

    /*
     * 幸運提升敏捷 - 投資了AP幸運的x%追加到敏捷
     */
    public int getLukToDex() {
        return info.get(MapleStatInfo.luk2dex);
    }

    /*
     * Hp增加攻擊傷害
     */
    public int getHpToDamageX() {
        return info.get(MapleStatInfo.mhp2damX);
    }

    /*
     * Mp增加攻擊傷害
     */
    public int getMpToDamageX() {
        return info.get(MapleStatInfo.mmp2damX);
    }

    /*
     * 升級增加最大HP上限
     */
    public int getLv2mhp() {
        return info.get(MapleStatInfo.lv2mhp);
    }

    /*
     * 升級增加最大MP上限
     */
    public int getLv2mmp() {
        return info.get(MapleStatInfo.lv2mmp);
    }

    /*
     * 升級增加增加攻擊傷害
     */
    public int getLevelToDamageX() {
        return info.get(MapleStatInfo.lv2damX);
    }

    /*
     * 升級增加物理攻擊力 - 每x級攻擊力增加1
     */
    public int getLevelToWatk() {
        return info.get(MapleStatInfo.lv2pad);
    }

    /*
     * 升級增加魔法攻擊力 - 每x級魔法攻擊力增加1
     */
    public int getLevelToMatk() {
        return info.get(MapleStatInfo.lv2mad);
    }

    /*
     * 升級增加物理攻擊力 - 每5級攻擊力增加1
     */
    public int getLevelToWatkX() {
        return info.get(MapleStatInfo.lv2pdX);
    }

    /*
     * 升級增加魔法攻擊力 - 每5級魔法攻擊力增加1
     */
    public int getLevelToMatkX() {
        return info.get(MapleStatInfo.lv2mdX);
    }

    /**
     * 死亡時經驗減少 X%
     *
     * @return
     */
    public int getEXPLossRate() {
        return info.get(MapleStatInfo.expLossReduceR);
    }

    /**
     * 增加增益效果時間 X%
     *
     * @return
     */
    public int getBuffTimeRate() {
        return info.get(MapleStatInfo.bufftimeR);
    }

    public int getSuddenDeathR() {
        return info.get(MapleStatInfo.suddenDeathR);
    }

    /**
     * 增加召喚獸時間 X%
     *
     * @return
     */
    public int getSummonTimeInc() {
        return info.get(MapleStatInfo.summonTimeR);
    }

    /**
     * 增加MP藥物效果 X%
     *
     * @return
     */
    public int getMPConsumeEff() {
        return info.get(MapleStatInfo.mpConEff);
    }

    /*
     * 增加物理攻擊力
     */
    public int getPadX() {
        return info.get(MapleStatInfo.padX);
    }

    /*
     * 增加魔法攻擊力
     */
    public int getMadX() {
        return info.get(MapleStatInfo.madX);
    }

    /*
     * 最大Hp增加 按百分比
     */
    public int getMhpR() {
        return info.get(MapleStatInfo.mhpR);
    }

    /*
     * 最大Mp增加 按百分比
     */
    public int getMmpR() {
        return info.get(MapleStatInfo.mmpR);
    }

    /*
     * 受到怪物攻擊的傷害減少x%
     */
    public int getIgnoreMobDamR() {
        return info.get(MapleStatInfo.ignoreMobDamR);
    }

    /*
     * 防禦率無視x%
     */
    public int getIndieIgnoreMobpdpR() {
        return info.get(MapleStatInfo.indieIgnoreMobpdpR);
    }

    /*
     * 受到傷害減少x%
     */
    public int getDamAbsorbShieldR() {
        return info.get(MapleStatInfo.damAbsorbShieldR);
    }

    public int getConsume() {
        return consumeOnPickup;
    }

    /**
     * 自爆傷害
     *
     * @return
     */
    public int getSelfDestruction() {
        return info.get(MapleStatInfo.selfDestruction);
    }

    public int getGauge() {
        return info.get(MapleStatInfo.gauge);
    }

    public int getCharColor() {
        return charColor;
    }

    public List<Integer> getPetsCanConsume() {
        return petsCanConsume;
    }

    public boolean isReturnScroll() {
        return !isSkill() && moveTo != -1;
    }

    public int getRange() {
        return info.get(MapleStatInfo.range);
    }

    /*
     * 迴避率增加 x%
     */
    public int getER() {
        return info.get(MapleStatInfo.er);
    }

    public int getPrice() {
        return info.get(MapleStatInfo.price);
    }

    public int getExtendPrice() {
        return info.get(MapleStatInfo.extendPrice);
    }

    public int getPeriod() {
        return info.get(MapleStatInfo.period);
    }

    public int getReqGuildLevel() {
        return info.get(MapleStatInfo.reqGuildLevel);
    }

    public int getExpR() {
        return info.get(MapleStatInfo.expR).byteValue();
    }

    public short getLifeID() {
        return lifeId;
    }

    public short getUseLevel() {
        return useLevel;
    }

    /*
     * 礦(藥)背包道具需要
     */
    public byte getSlotCount() {
        return slotCount;
    }

    public byte getSlotPerLine() {
        return slotPerLine;
    }

    /*
     * 增加力量
     */
    public int getStr() {
        return info.get(MapleStatInfo.str);
    }

    public int getStrX() {
        return info.get(MapleStatInfo.strX);
    }

    public int getStrFX() {
        return info.get(MapleStatInfo.strFX);
    }

    public int getStrRate() {
        return info.get(MapleStatInfo.strR);
    }

    /*
     * 增加敏捷
     */
    public int getDex() {
        return info.get(MapleStatInfo.dex);
    }

    public int getDexX() {
        return info.get(MapleStatInfo.dexX);
    }

    public int getDexFX() {
        return info.get(MapleStatInfo.dexFX);
    }

    public int getDexR() {
        return info.get(MapleStatInfo.dexR);
    }

    /*
     * 增加智力
     */
    public int getInt() {
        return info.get(MapleStatInfo.int_);
    }

    public int getIntX() {
        return info.get(MapleStatInfo.intX);
    }

    public int getIntFX() {
        return info.get(MapleStatInfo.intFX);
    }

    public int getIntRate() {
        return info.get(MapleStatInfo.intR);
    }

    /*
     * 增加幸運
     */
    public int getLuk() {
        return info.get(MapleStatInfo.luk);
    }

    public int getLukX() {
        return info.get(MapleStatInfo.lukX);
    }

    public int getLukFX() {
        return info.get(MapleStatInfo.lukFX);
    }

    public int getLukRate() {
        return info.get(MapleStatInfo.lukR);
    }

    /*
     * 最大HP增加
     */
    public int getMaxHpX() {
        return info.get(MapleStatInfo.mhpX);
    }

    /*
     * 最大MP增加
     */
    public int getMaxMpX() {
        return info.get(MapleStatInfo.mmpX);
    }

    /*
     * 命中值增加
     */
    public int getAccX() {
        return info.get(MapleStatInfo.accX);
    }

    /*
     * 命中值增加 x%
     */
    public int getPercentAcc() {
        return info.get(MapleStatInfo.accR);
    }

    /*
     * 迴避值增加
     */
    public int getAvoidX() {
        return info.get(MapleStatInfo.evaX);
    }

    /*
     * 迴避值增加 x%
     */
    public int getPercentAvoid() {
        return info.get(MapleStatInfo.evaR);
    }

    /*
     * 物理防禦力增加
     */
    public int getPddX() {
        return info.get(MapleStatInfo.pddX);
    }

    /*
     * 魔法防禦力增加
     */
    public int getMdefX() {
        return info.get(MapleStatInfo.mddX);
    }

    /*
     * Hp增加
     */
    public int getIndieMHp() {
        return info.get(MapleStatInfo.indieMhp);
    }

    /*
     * Mp增加
     */
    public int getIndieMMp() {
        return info.get(MapleStatInfo.indieMmp);
    }

    /*
     * 百分比MaxHp增加
     */
    public int getIndieMhpR() {
        return info.get(MapleStatInfo.indieMhpR);
    }

    /*
     * 百分比MaxMp增加
     */
    public int getIndieMmpR() {
        return info.get(MapleStatInfo.indieMmpR);
    }

    /*
     * 所有屬性增加
     */
    public int getIndieAllStat() {
        return info.get(MapleStatInfo.indieAllStat);
    }

    /*
     * 爆擊概率增加 %
     */
    public int getIndieCr() {
        return info.get(MapleStatInfo.indieCr);
    }

    /**
     * *
     * 增加攻擊力
     *
     * @return 攻擊力
     */
    public int getEpdd() {
        return info.get(MapleStatInfo.epad);
    }

    /**
     * 依 xx%機率, 沒有冷卻時間
     *
     * @return 機率
     */
    public int getNocoolProp() {
        return info.get(MapleStatInfo.nocoolProp);
    }

    public short getIndiePdd() {
        return indiePdd;
    }

    public short getIndieMdd() {
        return indieMdd;
    }

    /*
     * 攻擊力提高 %
     */
    public int getIndieDamR() {
        return info.get(MapleStatInfo.indieDamR);
    }

    /*
     * 提高攻擊速度
     */
    public int getIndieBooster() {
        return info.get(MapleStatInfo.indieBooster);
    }

    public byte getType() {
        return type;
    }

    /*
     * 攻擊BOSS時，傷害增加x%
     */
    public int getBossDamage() {
        return info.get(MapleStatInfo.bdR);
    }

    /*
     * 攻擊時怪物數量少於技能的數量傷害提高
     */
    public int getMobCountDamage() {
        return info.get(MapleStatInfo.mobCountDamR);
    }

    public int getInterval() {
        return interval;
    }

    public List<Pair<Integer, Integer>> getAvailableMaps() {
        return availableMap;
    }

    /*
     * 增加物防 按百分比
     */
    public int getPddR() {
        return info.get(MapleStatInfo.pddR);
    }

    /*
     * 增加魔防 按百分比
     */
    public int getMDEFRate() {
        return info.get(MapleStatInfo.mddR);
    }

    /*
     * 新增變量
     */
    public int getKillSpree() {
        return info.get(MapleStatInfo.kp);
    }

    /*
     * 技能傷害最大值
     */
    public int getMaxDamageOver() {
        return info.get(MapleStatInfo.MDamageOver);
    }

    /*
     * 技能傷害最大值
     */
    public int getIndieMaxDamageOver() {
        return info.get(MapleStatInfo.indieMaxDamageOver);
    }

    /*
     * 消耗更多的 Mp 來增加技能的傷害
     */
    public int getCostMpRate() {
        return info.get(MapleStatInfo.costmpR);
    }

    /*
     * 技能Mp消耗減少 %
     */
    public int getMPConReduce() {
        return info.get(MapleStatInfo.mpConReduce);
    }

    /*
     * 惡魔的最大DF增加 也就是惡魔精氣
     */
    public int getIndieMaxDF() {
        return info.get(MapleStatInfo.MDF);
    }

    /**
     * 格外增加攻擊怪物的數量
     */
    public int getTargetPlus() {
        return info.get(MapleStatInfo.targetPlus);
    }

    /**
     * 格外增加攻擊怪物的數量-五轉強化技能
     */
    public int getTargetPlus_5th() {
        return info.get(MapleStatInfo.targetPlus_5th);
    }

    /**
     * 使用技能消耗惡魔精氣
     */
    public int getForceCon() {
        return info.get(MapleStatInfo.forceCon);
    }

    public int getAtGauge1Con() {
        return info.get(MapleStatInfo.atGauge1Con);
    }

    public int getAtGauge2Con() {
        return info.get(MapleStatInfo.atGauge2Con);
    }

    public int getAtGauge2Inc() {
        return info.get(MapleStatInfo.atGauge2Inc);
    }

    /**
     * 虎影 道术种类
     *
     * @return 1=天 2=地 3=人
     */
    public int getAtSkillType() {
        return info.get(MapleStatInfo.atSkillType);
    }

    public int getForceCon(MapleCharacter chr) {
        int forceCon = getForceCon();
        return forceCon - forceCon * chr.getStat().getSkillReduceForceCon(sourceid) / 100;
    }

    public int getReduceForceR() {
        return info.get(MapleStatInfo.reduceForceR);
    }

    /*
     * 使用靈魂技能
     */
    public int getSoulMpCon() {
        return info.get(MapleStatInfo.soulmpCon);
    }

    /*
     * 使用pp技能消耗
     */
    public int getPPCon() {
        return info.get(MapleStatInfo.ppCon);
    }

    public int getKillRecoveryR() {
        return info.get(MapleStatInfo.killRecoveryR);
    }

    public boolean isOnRule() {
        return ruleOn;
    }

    public boolean is疾風() {
        return isSkill() && sourceid == 閃雷悍將.颱風 || sourceid == 閃雷悍將.疾風;
    }

    public void applyAffectedArea(MapleCharacter chr, Point pos) {

        if (pos == null) {
            pos = chr.getPosition();
        }
        MapleAffectedArea area = new MapleAffectedArea(calculateBoundingBox(new Point(pos.x, pos.y + (sourceid == 惡魔復仇者.惡魔狂亂_魔族之血 ? 40 : 0)), chr.isFacingLeft()), chr, this, new Point(pos));
        if (chr.isDebug()) {
            chr.dropSpouseMessage(UserChatMessageType.公告, "[Affected Area]技能：" + area.getEffect() + " 持續時間：" + area.getDuration());
        }
        if (!area.isPoisonMist() && sourceid != 主教.神聖之水 && sourceid != 重砲指揮官.精準轟炸_2 && sourceid != 通用V核心.盜賊通用.爆破飛毒殺) {
            chr.getMap().removeAffectedArea(chr.getId(), sourceid);
        }
        chr.getMap().createAffectedArea(area);
    }

    public void applyToMonster(MapleCharacter chr, int duration) {
        int n2 = 0;
        for (final MapleMapObject monster : chr.getMap().getMapObjectsInRect(calculateBoundingBox(chr.getPosition(), chr.isFacingLeft()), Collections.singletonList(MapleMapObjectType.MONSTER))) {
            if (makeChanceResult(chr) && n2 < getMobCount()) {
                applyMonsterEffect(chr, (MapleMonster) monster, duration);
            }
            ++n2;
        }
    }

    public boolean applyMonsterEffect(final MapleCharacter chr, final MapleMonster monster, int duration) {
        if (monster == null || !monster.isAlive() || !isSkill()) {
            return false;
        }

        int prop = getProp(chr);
        Map<MonsterStatus, Integer> localstatups = new EnumMap<>(monsterStatus);

        AbstractSkillHandler handler = getSkillHandler();
        if (handler == null) {
            handler = SkillClassFetcher.getHandlerByJob(chr.getJobWithSub());
        }
        int handleRes = -1;
        if (handler != null) {
            SkillClassApplier applier = new SkillClassApplier();
            applier.effect = this;
            applier.duration = duration;
            applier.prop = prop;
            applier.localmobstatups = localstatups;
            handleRes = handler.onApplyMonsterEffect(chr, monster, applier);
            if (handleRes == 0) {
                return false;
            } else if (handleRes == 1) {
                prop = applier.prop;
                duration = applier.duration;
                localstatups = applier.localmobstatups;
            }
        }

        final Skill skill = getSkill();
        int sourceid = this.sourceid;
        if (skill == null) {
            return false;
        } else {
            switch (monster.getStats().getEffectiveness(skill.getElement())) {
                case 免疫:
                case 增強: {
                    return false;
                }
            }
        }
        if (monster.getStats().isEscort() || monster.isFake() || monster.getStats().isNoDoom() && localstatups.containsKey(MonsterStatus.Venom)) {
            return false;
        }
        final EnumMap<MonsterStatus, MonsterEffectHolder> statups = new EnumMap<>(MonsterStatus.class);
        if (prop == 0 || Randomizer.isSuccess(prop)) {
            for (Entry<MonsterStatus, Integer> entry : localstatups.entrySet()) {
                final MonsterStatus status = entry.getKey();
                if (!skill.isMesToBoss() && !skill.MesList.contains(SkillMesInfo.restrict) && SkillConstants.isMoveImpactStatus(status) && (monster.isBoss() || monster.getStats().isIgnoreMoveImpact())) {
                    if ((skill.getId() == 夜使者.絕對領域 && chr.getSkillLevel(夜使者.絕對領域_BOSS殺手) <= 0 && monster.isBoss())) {
                        continue;
                    } else if (skill.getId() != 夜使者.絕對領域 && skill.getId() != 凱內西斯.心碎擷取) {
                        continue;
                    }
                }
                if (!SkillConstants.isSmiteStatus(status) || sourceid == 虎影.仙技_分身遁甲太乙仙人_攻擊 || (sourceid != 80011540 && monster.canNextSmite()) || (sourceid == 80011540 && monster.canNextLucidSmite())) {
                    switch (sourceid) {
                        case 煉獄巫師.減益效果光環: {
                            if (monster.isBuffed(status)) {
                                continue;
                            }
                            break;
                        }
                        case 夜使者.飛毒殺:
                        case 夜使者.致命飛毒殺:
                        case 暗影神偷.飛毒殺:
                        case 暗影神偷.致命飛毒殺:
                        case 影武者.飛毒殺:
                        case 影武者.致命的飛毒殺: {
                            switch (monster.getStats().getEffectiveness(Element.毒)) {
                                case 免疫:
                                case 增強: {
                                    return false;
                                }
                                default: {
                                    break;
                                }
                            }
                            break;
                        }
                        case 卡蒂娜.召喚_狼牙棒_2:
                            sourceid = 卡蒂娜.召喚_狼牙棒_1;
                            break;
                        case 虎影.卷術_微生強變: {
                            if (status == MonsterStatus.Morph && monster.isBoss()) {
                                continue;
                            }
                            break;
                        }
                    }

                    int value = entry.getValue();
                    int z = 0;
                    if (status == MonsterStatus.Speed && JobConstants.is冰雷(chr.getJob())) {
                        z = 1;
                        Object obj = chr.getTempValues().remove("冰雪之精神攻擊數量");
                        if ((obj instanceof Boolean) && (boolean) obj) {
                            z = 3;
                        }
                    }

                    if (status == MonsterStatus.Morph) {
                        value = Randomizer.nextBoolean() ? 2400500 : 2400501;
                    }
                    MapleStatEffect eff;
                    switch (sourceid) {
                        case 夜使者.挑釁契約:
                            if ((eff = chr.getSkillEffect(夜使者.挑釁契約_強化效果)) != null) {
                                value += eff.getX();
                            }
                            break;
                        case 夜使者.絕對領域:
                            if ((eff = chr.getSkillEffect(夜使者.絕對領域_強化效果)) != null) {
                                switch (status) {
                                    case IndiePDR:
                                        value += eff.getX();
                                        break;
                                    case PAD:
                                        value += eff.getZ();
                                        break;
                                }
                            }
                            if ((eff = chr.getSkillEffect(夜使者.絕對領域_緩慢)) != null) {
                                switch (status) {
                                    case Speed:
                                        value += eff.getY();
                                        break;
                                }
                            }
                            break;
                    }

                    boolean b;
                    switch (sourceid) {
                        case 暗影神偷.黑影切斷: {
                            b = true;
                            break;
                        }
                        case 主教.天使之箭:
                            z = 1;
                        case 箭神.魔幻箭筒_4轉:
                        case 狂豹獵人.另一個咬擊:
                        case 神之子.碎甲:
                        case 伊利恩.詛咒之印_怪物狀態:
                        case 傑諾.三角列陣: {
                            b = monster.getEffectHolder(status, status.isIndieStat() ? sourceid : -1) != null;
                            break;
                        }
                        case 暗夜行者.SUMMON_元素闇黑: {
                            b = monster.getEffectHolder(chr.getId(), MonsterStatus.Burned, sourceid) != null;
                            break;
                        }
                        default: {
                            switch (status) {
                                case CurseTransition:
                                    b = true;
                                    break;
                                case Speed:
                                    b = JobConstants.is冰雷(chr.getJob());
                                    break;
                                default:
                                    b = false;
                                    break;
                            }
                            break;
                        }
                    }
                    if (b) {
                        MonsterEffectHolder myPoison = monster.getEffectHolder(chr.getId(), status, status.isIndieStat() ? sourceid : -1);
                        if (myPoison != null) {
                            switch (sourceid) {
                                case 主教.天使之箭:
                                    z = Math.min(5, myPoison.z + z);
                                    break;
                                case 伊利恩.詛咒之印_怪物狀態:
                                    int max = 1;
                                    if (chr.getSkillEffect(伊利恩.詛咒之印) != null) {
                                        max = 5;
                                    } else if (chr.getSkillEffect(伊利恩.熟練詛咒之印) != null) {
                                        max = 3;
                                    }
                                    value = Math.min(max, myPoison.value + value);
                                    break;
                                default:
                                    if (status == MonsterStatus.Speed && JobConstants.is冰雷(chr.getJob())) {
                                        z = Math.min(5, myPoison.z + z);
                                        value = Math.max(-75, myPoison.value + value);
                                    } else if (status == MonsterStatus.CurseTransition) {
                                        value = Math.min(myPoison.effect == null ? 5 : myPoison.effect.getX(), myPoison.value + value);
                                    } else {
                                        value = Math.min(3, myPoison.value + 1);
                                    }
                                    break;
                            }
                        }
                    }

                    int localDuration = duration;
                    switch (sourceid) {
                        case 影武者.閃光彈:
                            if (monster.isBoss()) {
                                localDuration = localDuration * 50 / 100;
                            }
                            break;
                    }
                    MonsterEffectHolder holder = new MonsterEffectHolder(chr.getId(), value, System.currentTimeMillis(), localDuration, this);
                    if (status == MonsterStatus.AddDamSkill && getSourceId() == 精靈遊俠.閃電之鋒) {
                        holder.sourceID = 精靈遊俠.伊修塔爾之環;
                    }
                    holder.z = z;

                    holder.moboid = monster.getSeperateSoulSrcOID();
                    if (status == MonsterStatus.Burned) {
                        setDotData(chr, holder);
                        value = holder.value;
                    }
                    statups.put(status, holder);
                    if (chr.isDebug()) {
                        chr.dropDebugMessage(0, "[MobBuff] Register Stat:" + status + " value:" + value);
                    }
                } else {
                    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                    mplew.writeShort(OutHeader.LP_MobTimeResist.getValue());
                    mplew.writeInt(monster.getObjectId());
                    mplew.writeInt(1);
                    mplew.writeInt(sourceid);
                    mplew.writeShort(92);
                    mplew.writeInt(chr.getId());
                    mplew.write(1);
                    chr.getMap().broadcastMessage(chr, mplew.getPacket(), true);
                }
            }
            if (localstatups.containsKey(MonsterStatus.MagicCrash) || sourceid == 幻影俠盜.靈魂竊取) {
                List<MonsterStatus> list = new ArrayList<>();
                list.add(MonsterStatus.PGuardUp);
                list.add(MonsterStatus.MGuardUp);
                list.add(MonsterStatus.PowerUp);
                list.add(MonsterStatus.MagicUp);
                list.add(MonsterStatus.HardSkin);
                if (sourceid == 幻影俠盜.靈魂竊取) {
                    list.add(MonsterStatus.PCounter);
                    list.add(MonsterStatus.MCounter);
                }
                monster.removeEffect(list);
            }
            if (chr.isDebug()) {
                chr.dropDebugMessage(1, "[MobBuff] Register Effect:" + this + " Duration:" + duration);
            }
        }
        if (!statups.isEmpty()) {
            monster.registerEffect(statups);
            Map<MonsterStatus, Integer> writeStatups = new LinkedHashMap<>();
            for (MonsterStatus stat : statups.keySet()) {
                writeStatups.put(stat, sourceid);
            }
            chr.getMap().broadcastMessage(MobPacket.mobStatSet(monster, writeStatups), monster.getPosition());
        }
        return !statups.isEmpty();
    }

    public void setDotData(final MapleCharacter chr, final MonsterEffectHolder holder) {
        final long damage = Math.min(chr.getCalcDamage().getRandomDamage(chr, false) * getDot() / 100L, Integer.MAX_VALUE);
        holder.value = (int) damage * holder.value;
        holder.dotInterval = getDotInterval() * 1000;
        holder.dotDamage = damage;
        holder.localDuration = getDotTime(chr) * 1000;
        holder.dotSuperpos = info.get(MapleStatInfo.dotSuperpos);
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public Map<MapleStatInfo, Integer> getInfo() {
        return this.info;
    }

    public Map<MapleStatInfo, Double> getInfoD() {
        return this.infoD;
    }

    public Map<MapleTraitType, Integer> getTraits() {
        return this.traits;
    }

    public List<Pair<Integer, Integer>> getAvailableMap() {
        return this.availableMap;
    }

    public Point getLt() {
        return this.lt;
    }

    public Point getRb() {
        return this.rb;
    }

    public Point getLt2() {
        return this.lt2;
    }

    public Point getRb2() {
        return this.rb2;
    }

    public Point getLt3() {
        return this.lt3;
    }

    public Point getRb3() {
        return this.rb3;
    }

    public List<SecondaryStat> getCureDebuffs() {
        return this.cureDebuffs;
    }

    public List<Integer> getFamiliars() {
        return this.familiars;
    }

    public List<Integer> getRandomPickup() {
        return this.randomPickup;
    }

    public List<Triple<Integer, Integer, Integer>> getRewardItem() {
        return this.rewardItem;
    }

    public byte getRecipeUseCount() {
        return this.recipeUseCount;
    }

    public byte getRecipeValidDay() {
        return this.recipeValidDay;
    }

    public byte getReqSkillLevel() {
        return this.reqSkillLevel;
    }

    public byte getEffectedOnAlly() {
        return this.effectedOnAlly;
    }

    public byte getEffectedOnEnemy() {
        return this.effectedOnEnemy;
    }

    public byte getPreventslip() {
        return this.preventslip;
    }

    public byte getImmortal() {
        return this.immortal;
    }

    public byte getBs() {
        return this.bs;
    }

    public short getIgnoreMob() {
        return this.ignoreMob;
    }

    public int getDropR() {
        return info.get(MapleStatInfo.dropR);
    }

    public int getMesoR() {
        return info.get(MapleStatInfo.mesoR);
    }

    public short getThaw() {
        return this.thaw;
    }

    public short getImhp() {
        return this.imhp;
    }

    public short getImmp() {
        return this.immp;
    }

    public short getMobSkill() {
        return this.mobSkill;
    }

    public short getMobSkillLevel() {
        return this.mobSkillLevel;
    }

    public int getSourceId() {
        return this.sourceid;
    }

    public int getRecipe() {
        return this.recipe;
    }

    public int getMoveTo() {
        return this.moveTo;
    }

    public int getMorphId() {
        return this.morphId;
    }

    public int getExpinc() {
        return this.expinc;
    }

    public int getConsumeOnPickup() {
        return this.consumeOnPickup;
    }

    public int getRewardMeso() {
        return this.rewardMeso;
    }

    public int getTotalprob() {
        return this.totalprob;
    }

    public int getCosmetic() {
        return this.cosmetic;
    }

    public int getExpBuff() {
        return this.expBuff;
    }

    public int getItemup() {
        return this.itemup;
    }

    public int getMesoup() {
        return this.mesoup;
    }

    public int getCashup() {
        return this.cashup;
    }

    public int getIllusion() {
        return this.illusion;
    }

    public int getBooster() {
        return this.booster;
    }

    public int getBerserk2() {
        return this.berserk2;
    }

    public boolean isRuleOn() {
        return this.ruleOn;
    }

    public boolean isBxi() {
        return this.bxi;
    }

    public boolean isHit() {
        return this.hit;
    }

    public void setInfo(Map<MapleStatInfo, Integer> info) {
        this.info = info;
    }

    public void setInfoD(Map<MapleStatInfo, Double> info) {
        this.infoD = info;
    }

    public void setTraits(Map<MapleTraitType, Integer> traits) {
        this.traits = traits;
    }

    public void setOverTime(boolean overTime) {
        this.overTime = overTime;
    }

    public void setPartyBuff(boolean partyBuff) {
        this.partyBuff = partyBuff;
    }

    public void setRangeBuff(boolean rangeBuff) {
        this.rangeBuff = rangeBuff;
    }

    public void setNotRemoved(boolean notRemoved) {
        this.notRemoved = notRemoved;
    }

    public void setNotIncBuffDuration(boolean notIncBuffDuration) {
        this.notIncBuffDuration = notIncBuffDuration;
    }

    public void setStatups(EnumMap<SecondaryStat, Integer> statups) {
        this.statups = statups;
    }

    public void setAvailableMap(List<Pair<Integer, Integer>> availableMap) {
        this.availableMap = availableMap;
    }

    public void setMonsterStatus(Map<MonsterStatus, Integer> monsterStatus) {
        this.monsterStatus = monsterStatus;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCureDebuffs(List<SecondaryStat> cureDebuffs) {
        this.cureDebuffs = cureDebuffs;
    }

    public void setPetsCanConsume(List<Integer> petsCanConsume) {
        this.petsCanConsume = petsCanConsume;
    }

    public void setFamiliars(List<Integer> familiars) {
        this.familiars = familiars;
    }

    public void setRandomPickup(List<Integer> randomPickup) {
        this.randomPickup = randomPickup;
    }

    public void setRewardItem(List<Triple<Integer, Integer, Integer>> rewardItem) {
        this.rewardItem = rewardItem;
    }

    public void setSlotCount(byte slotCount) {
        this.slotCount = slotCount;
    }

    public void setSlotPerLine(byte slotPerLine) {
        this.slotPerLine = slotPerLine;
    }

    public void setRecipeUseCount(byte recipeUseCount) {
        this.recipeUseCount = recipeUseCount;
    }

    public void setRecipeValidDay(byte recipeValidDay) {
        this.recipeValidDay = recipeValidDay;
    }

    public void setReqSkillLevel(byte reqSkillLevel) {
        this.reqSkillLevel = reqSkillLevel;
    }

    public void setEffectedOnAlly(byte effectedOnAlly) {
        this.effectedOnAlly = effectedOnAlly;
    }

    public void setEffectedOnEnemy(byte effectedOnEnemy) {
        this.effectedOnEnemy = effectedOnEnemy;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public void setPreventslip(byte preventslip) {
        this.preventslip = preventslip;
    }

    public void setImmortal(byte immortal) {
        this.immortal = immortal;
    }

    public void setBs(byte bs) {
        this.bs = bs;
    }

    public void setIgnoreMob(short ignoreMob) {
        this.ignoreMob = ignoreMob;
    }

    public void setMesoR(short mesoR) {
        this.mesoR = mesoR;
    }

    public void setThaw(short thaw) {
        this.thaw = thaw;
    }

    public void setLifeId(short lifeId) {
        this.lifeId = lifeId;
    }

    public void setImhp(short imhp) {
        this.imhp = imhp;
    }

    public void setImmp(short immp) {
        this.immp = immp;
    }

    public void setInflation(short inflation) {
        this.inflation = inflation;
    }

    public void setUseLevel(short useLevel) {
        this.useLevel = useLevel;
    }

    public void setIndiePdd(short indiePdd) {
        this.indiePdd = indiePdd;
    }

    public void setIndieMdd(short indieMdd) {
        this.indieMdd = indieMdd;
    }

    public void setMobSkill(short mobSkill) {
        this.mobSkill = mobSkill;
    }

    public void setMobSkillLevel(short mobSkillLevel) {
        this.mobSkillLevel = mobSkillLevel;
    }

    public void setHpR(double hpR) {
        this.hpR = hpR;
    }

    public void setMpR(double mpR) {
        this.mpR = mpR;
    }

    public void setSourceid(int sourceid) {
        this.sourceid = sourceid;
    }

    public void setRecipe(int recipe) {
        this.recipe = recipe;
    }

    public void setMoveTo(int moveTo) {
        this.moveTo = moveTo;
    }

    public void setMoneyCon(int moneyCon) {
        this.moneyCon = moneyCon;
    }

    public void setMorphId(int morphId) {
        this.morphId = morphId;
    }

    public void setExpinc(int expinc) {
        this.expinc = expinc;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setConsumeOnPickup(int consumeOnPickup) {
        this.consumeOnPickup = consumeOnPickup;
    }

    public void setCharColor(int charColor) {
        this.charColor = charColor;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setRewardMeso(int rewardMeso) {
        this.rewardMeso = rewardMeso;
    }

    public void setTotalprob(int totalprob) {
        this.totalprob = totalprob;
    }

    public void setCosmetic(int cosmetic) {
        this.cosmetic = cosmetic;
    }

    public void setExpBuff(int expBuff) {
        this.expBuff = expBuff;
    }

    public void setItemup(int itemup) {
        this.itemup = itemup;
    }

    public void setMesoup(int mesoup) {
        this.mesoup = mesoup;
    }

    public void setCashup(int cashup) {
        this.cashup = cashup;
    }

    public void setBerserk(int berserk) {
        this.berserk = berserk;
    }

    public void setIllusion(int illusion) {
        this.illusion = illusion;
    }

    public void setBooster(int booster) {
        this.booster = booster;
    }

    public void setBerserk2(int berserk2) {
        this.berserk2 = berserk2;
    }

    public void setRuleOn(boolean ruleOn) {
        this.ruleOn = ruleOn;
    }

    public void setBxi(boolean bxi) {
        this.bxi = bxi;
    }

    public void applyToParty(MapleMap map, MapleCharacter player) {
        if (player.getParty() != null) {
            for (MapleCharacter chr : map.getPartyMembersInRange(player.getParty(), player.getPosition(), 500)) {
                if (chr.getBuffStatValueHolder(sourceid) == null) {
                    applyBuffEffect(player, chr, 10000, true, false, true, chr.getPosition());
                }
            }
        }
        if (!monsterStatus.isEmpty()) {
            applyToMonster(player, 5000);
        }
    }

    /*
     * 取消BUFF的線程操作
     */
    public static class CancelEffectAction implements Runnable {

        private final MapleStatEffect effect;
        private WeakReference<MapleCharacter> target;
        private final long startTime;
        private final Map<SecondaryStat, Integer> statup;
        private int targetID;

        public CancelEffectAction(MapleCharacter target, MapleStatEffect effect, long startTime, Map<SecondaryStat, Integer> statup) {
            this.effect = effect;
            this.target = new WeakReference<>(target);
            this.startTime = startTime;
            this.statup = statup;
            this.targetID = target.getId();
        }

        public void changeTarget(MapleCharacter target) {
            this.target = new WeakReference<>(target);
            this.targetID = target.getId();
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.cancelEffect(effect, false, startTime, statup);
            }
            World.TemporaryStat.CancelStat(targetID, new LinkedList<>(statup.keySet()), effect, startTime);
        }
    }

    public boolean MakeDebuffChanceResult() {
        return info.get(MapleStatInfo.subProp) >= 100 || Randomizer.nextInt(100) < info.get(MapleStatInfo.subProp)
                || info.get(MapleStatInfo.prop) >= 100 || Randomizer.nextInt(100) < info.get(MapleStatInfo.prop)
                || info.get(MapleStatInfo.hcProp) >= 100 || Randomizer.nextInt(100) < info.get(MapleStatInfo.hcProp);
    }

    public Integer getHcTime() {
        return info.get(MapleStatInfo.hcTime);
    }

    public int gethcSubProp() {
        return info.get(MapleStatInfo.hcSubProp);
    }

    public int getHpRCon() {
        return info.get(MapleStatInfo.hpRCon);
    }

    public int getHpCon() {
        return info.get(MapleStatInfo.hpCon);
    }

    public int getPowerCon() {
        return info.get(MapleStatInfo.powerCon);
    }

    public int getIndieBDR() {
        return info.get(MapleStatInfo.indieBDR);
    }

    public int getIndiePMdR() {
        return info.get(MapleStatInfo.indiePMdR);
    }

    public int getCriticalDamage() {
        return info.get(MapleStatInfo.criticaldamage);
    }

    public int getStanceProp() {
        return info.get(MapleStatInfo.stanceProp);
    }

    public int getU2() {
        return info.get(MapleStatInfo.u2);
    }

    public int getHcHp() {
        return info.get(MapleStatInfo.hcHp);
    }

    public boolean isNotIncBuffDuration() {
        return notIncBuffDuration;
    }

    public boolean is血腥盛宴() {
        return isSkill() && (sourceid == 惡魔復仇者.血腥盛宴_1 || sourceid == 惡魔復仇者.血腥盛宴_2 || sourceid == 惡魔復仇者.血腥盛宴_3);
    }


    @Override
    public String toString() {
        return (isSkill() ? SkillFactory.getSkillName(this.sourceid) : MapleItemInformationProvider.getInstance().getName(Math.abs(this.sourceid))) + "[" + this.sourceid + "] Level：" + this.getLevel();
    }
}
