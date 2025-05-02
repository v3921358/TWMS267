package Client.skills;

import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassFetcher;
import Config.constants.JobConstants;
import Config.constants.SkillConstants;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.*;
import Config.constants.skills.皇家騎士團_技能群組.米哈逸;
import Config.constants.skills.皇家騎士團_技能群組.聖魂劍士;
import Net.server.buffs.MapleStatEffect;
import Net.server.buffs.MapleStatEffectFactory;
import Net.server.life.Element;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataTool;
import Plugin.provider.MapleDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.Randomizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Skill {

    private static final Logger log = LoggerFactory.getLogger(Skill.class);
    private final List<MapleStatEffect> effects = new ArrayList<>();
    private final List<Pair<String, Byte>> requiredSkill = new ArrayList<>();
    private String name = "";
    private final boolean isSwitch = false;
    private final Map<Integer, Integer> bonusExpInfo = new HashMap<>(); //[技能等級] [升級需要的經驗]
    private int id;
    private String psdDamR = "";
    private String targetPlus = "";
    private String minionAttack = "";
    private String minionAbility = "";
    private Element element = Element.NEUTRAL;
    private List<MapleStatEffect> pvpEffects = null;
    private List<Integer> animation = null;
    private int hyper = 0;
    private int hyperStat = 0;
    private int reqLev = 0;
    private int animationTime = 0;
    private int masterLevel = 0;
    private int maxLevel = 0;
    private int delay = 0;
    private int trueMax = 0;
    private int eventTamingMob = 0;
    private int skillType = 0;
    private int fixLevel;
    private int disableNextLevelInfo;
    private int psd = 0;
    //    private int psdSkills = 0;
    private List<Integer> psdSkills = null;
    private int setItemReason;
    private int setItemPartsCount;
    private int maxDamageOver = 999999;
    private int ppRecovery = 0;
    private boolean invisible = false;
    private boolean chargeSkill = false;
    private boolean timeLimited = false;
    private boolean combatOrders = false;
    private boolean pvpDisabled = false;
    private boolean magic = false;
    private boolean casterMove = false;
    private boolean chargingSkill;
    private boolean passiveSkill;
    private boolean selfDestructMinion;
    private boolean rapidAttack;
    private boolean pushTarget = false;
    private boolean pullTarget = false;
    private boolean buffSkill = false;
    private boolean summon = false;
    private boolean notRemoved = false;
    private boolean disable = false;
    private boolean hasMasterLevelProperty = false;
    private boolean petPassive = false;
    private boolean finalAttack = false;
    private boolean soulSkill = false;
    private boolean notCooltimeReduce = false;
    private boolean notCooltimeReset = false;
    private boolean showSummonedBuffIcon = false;
    public static Map<String, Integer> Info = new HashMap<>();
    //技能類型
    public Map<SkillInfo, String> info = new HashMap<>();
    public List<SkillMesInfo> MesList = new ArrayList<>();
    public static Map<String, List<Integer>> SkillMes = new HashMap<>();
    public static List<Integer> SkillMeList = new ArrayList<>();
    //超級技能是否使用CD
    public boolean notIncBuffDuration = false;
    private boolean mesToBoss = false;
    private boolean mobSkill;
    private int vehicleID;
    private boolean profession;
    private boolean ignoreCounter;
    private int hitTime;
    private boolean recipe;
    private int vSkill;
    private static final Lock delayDataLock = new ReentrantLock();
    public static List<Integer> skillList = new ArrayList<>();

    public Skill() {

    }

    public Skill(int id) {
        super();
        this.id = id;
    }

    public static Skill loadFromData(int id, MapleData data, MapleData delayData) {
        boolean showSkill = false;
        if (showSkill) {
            System.out.println("正在解析技能id: " + id + " 名字: " + SkillFactory.getSkillName(id));
            log.trace("正在解析技能id: " + id + " 名字: " + SkillFactory.getSkillName(id), true);
        }
        Skill ret = new Skill(id);
        ret.name = SkillFactory.getSkillName(id);
        boolean isBuff;
        int skillType = MapleDataTool.getInt("skillType", data, -1);
        String elem = MapleDataTool.getString("elemAttr", data, null);
        ret.element = elem != null ? Element.getFromChar(elem.charAt(0)) : Element.NEUTRAL;
        ret.skillType = skillType;
        ret.invisible = MapleDataTool.getInt("invisible", data, 0) > 0;
        MapleData effect = data.getChildByPath("effect");
        MapleData common = data.getChildByPath("common");
        MapleData info = data.getChildByPath("info");
        MapleData info2 = data.getChildByPath("info2");
        MapleData hit = data.getChildByPath("hit");
        MapleData ball = data.getChildByPath("ball");
        ret.mobSkill = data.getChildByPath("mob") != null;
        ret.summon = data.getChildByPath("summon") != null;
        ret.masterLevel = MapleDataTool.getInt("masterLevel", data, 0);
        if (ret.masterLevel > 0) {
            ret.hasMasterLevelProperty = true;
        }
        ret.psd = MapleDataTool.getInt("psd", data, 0);
        if (ret.psd == 1) {
            final MapleData psdSkill = data.getChildByPath("psdSkill");
            if (psdSkill != null) {
                ret.psdSkills = new ArrayList<>();
                data.getChildByPath("psdSkill").getChildren().forEach(it -> ret.psdSkills.add(Integer.valueOf(it.getName())));
            }
        }
        ret.notRemoved = MapleDataTool.getInt("notRemoved", data, 0) > 0;
        ret.notIncBuffDuration = MapleDataTool.getInt("notIncBuffDuration", data, 0) > 0;
        ret.timeLimited = MapleDataTool.getInt("timeLimited", data, 0) > 0;
        ret.combatOrders = MapleDataTool.getInt("combatOrders", data, 0) > 0;
        ret.fixLevel = MapleDataTool.getInt("fixLevel", data, 0);
        ret.disable = MapleDataTool.getInt("disable", data, 0) > 0;
        ret.disableNextLevelInfo = MapleDataTool.getInt("disableNextLevelInfo", data, 0);
        ret.eventTamingMob = MapleDataTool.getInt("eventTamingMob", data, 0);
        ret.vehicleID = MapleDataTool.getInt("vehicleID", data, 0);
        ret.hyper = MapleDataTool.getInt("hyper", data, 0); //超級技能欄位設置 P A
        ret.hyperStat = MapleDataTool.getInt("hyperStat", data, 0); //超級屬性點
        ret.reqLev = MapleDataTool.getInt("reqLev", data, 0); //超級技能需要的等級
        ret.petPassive = MapleDataTool.getInt("petPassive", data, 0) > 0; //是否寵物被動觸發技能
        ret.setItemReason = MapleDataTool.getInt("setItemReason", data, 0); //觸發技能的套裝ID
        ret.setItemPartsCount = MapleDataTool.getInt("setItemPartsCount", data, 0); //觸發技能需要的數量
        ret.ppRecovery = MapleDataTool.getInt("ppRecovery", data, 0); //超能力者pp恢復量
        ret.notCooltimeReduce = MapleDataTool.getInt("notCooltimeReduce", data, 0) > 0;
        ret.notCooltimeReset = MapleDataTool.getInt("notCooltimeReset", data, 0) > 0;
        ret.showSummonedBuffIcon = MapleDataTool.getInt("showSummonedBuffIcon", data, 0) > 0;//是否顯示召喚獸圖標
        ret.profession = (id / 10000 >= 9200 && id / 10000 <= 9204);
        ret.vSkill = MapleDataTool.getInt("vSkill", data, ret.isVSkill() ? 1 : -1);
        if (info != null) {
            ret.pvpDisabled = MapleDataTool.getInt("pvp", info, 1) <= 0;
            ret.magic = MapleDataTool.getInt("magicDamage", info, 0) > 0;
            ret.casterMove = MapleDataTool.getInt("casterMove", info, 0) > 0;
            ret.pushTarget = MapleDataTool.getInt("pushTarget", info, 0) > 0;
            ret.pullTarget = MapleDataTool.getInt("pullTarget", info, 0) > 0;
            ret.rapidAttack = MapleDataTool.getInt("rapidAttack", info, 0) > 0;
            ret.minionAttack = MapleDataTool.getString("minionAttack", info, "");
            ret.minionAbility = MapleDataTool.getString("minionAbility", info, "");
            ret.selfDestructMinion = MapleDataTool.getInt("selfDestructMinion", info, 0) > 0;
            ret.chargingSkill = MapleDataTool.getInt("chargingSkill", info, 0) > 0 || MapleDataTool.getInt("keydownThrowing", info, 0) > 0 || id == 冰雷.冰龍吐息;
        }
        if (info2 != null) {
            ret.ignoreCounter = MapleDataTool.getInt("ignoreCounter", info2, 0) > 0;
        }
        MapleData action_ = data.getChildByPath("action");
        boolean action = false;
        if (action_ == null && data.getChildByPath("prepare/action") != null) {
            action_ = data.getChildByPath("prepare/action");
            action = true;
        }
        isBuff = effect != null && hit == null && ball == null;
        final boolean isHit = hit != null;
        if (action_ != null) {
            String d;
            if (action) { //prepare
                d = MapleDataTool.getString(action_, null);
            } else {
                d = MapleDataTool.getString("0", action_, null);
            }
            if (d != null) {
                isBuff |= d.equals("alert2");
                delayDataLock.lock();
                try {
                    MapleData dd = delayData.getChildByPath(d);
                    if (dd != null) {
                        for (MapleData del : dd) {
                            ret.delay += Math.abs(MapleDataTool.getInt("delay", del, 0));
                        }
                        if (ret.delay > 30) { //then, faster(2) = (10+2)/16 which is basically 3/4
                            ret.delay = (int) Math.round(ret.delay * 11.0 / 16.0); //fastest(1) lolol
                            ret.delay -= ret.delay % 30; //round to 30ms
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    delayDataLock.unlock();
                }
                if (SkillFactory.getDelay(d) != null) { //this should return true always
                    ret.animation = new ArrayList<>();
                    ret.animation.add(SkillFactory.getDelay(d));
                    if (!action) {
                        for (MapleData ddc : action_) {
                            try {
                                if (ddc.getType() == MapleDataType.STRING && !MapleDataTool.getString(ddc, d).equals(d) && !ddc.getName().contentEquals("delay")) {
                                    String c = MapleDataTool.getString(ddc);
                                    if (SkillFactory.getDelay(c) != null) {
                                        ret.animation.add(SkillFactory.getDelay(c));
                                    }
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(String.valueOf(ret.getId()), e);
                            }
                        }
                    }
                }
            }
        }
        ret.chargeSkill = data.getChildByPath("keydown") != null;
        if (info != null) {
            info.getChildren().forEach(mapleData -> {
                if (mapleData.getName().equals("finalAttack") && ((Number) mapleData.getData()).intValue() == 1 && !SkillFactory.getFinalAttackSkills().contains(id)) {
                    ret.finalAttack = true;
                    SkillFactory.getFinalAttackSkills().add(id);
                }
            });
        }
        if (!ret.chargeSkill) {
            switch (id) {
                case 冰雷.冰鋒刃:
                    ret.chargeSkill = true;
                    break;
            }
        }
        //有些技能是老的XML模式
        MapleData levelData = data.getChildByPath("level");
        if (common != null) {
            ret.maxLevel = MapleDataTool.getInt("maxLevel", common, 1); //10 just a failsafe, shouldn't actually happens
            ret.trueMax = ret.maxLevel + (ret.combatOrders ? 2 : (ret.vSkill == 2 ? 10 : (ret.vSkill == 1 ? 5 : 0)));
            if (levelData != null) {
                for (MapleData leve : levelData) {
                    ret.effects.add(MapleStatEffectFactory.loadSkillEffectFromData(ret, leve, id, isHit, isBuff, ret.summon, Byte.parseByte(leve.getName()), null, ret.notRemoved, ret.notIncBuffDuration));
                }
            } else {
                ret.soulSkill = common.getChildByPath("soulmpCon") != null;
                ret.psdDamR = MapleDataTool.getString("damR", common, "");
                ret.targetPlus = MapleDataTool.getString("targetPlus", common, "");
                for (int i = 1; i < 3; i++) {
                    if (data.getChildByPath("info" + (i == 1 ? "" : String.valueOf(i))) != null) {
                        for (MapleData mapleData : data.getChildByPath("info" + (i == 1 ? "" : String.valueOf(i))).getChildren()) {
                            try {
                                SkillInfo Sinfo = SkillInfo.valueOf(mapleData.getName());
                                switch (Sinfo) {
                                    case incDamToStunTarget:
//                                    System.out.println("技能:" + id);
                                        break;
                                    case affectedSkillEffect:
                                    case mes: {
                                        //進行添加..
                                        String key = mapleData.getData().toString();
                                        String[] keys = key.split("&&");
                                        for (String key1 : keys) {
                                            try {
                                                ret.MesList.add(SkillMesInfo.getInfo(key1));
                                            } catch (Exception e) {
                                                System.err.println("加載錯誤技能:" + id);
                                            }
                                        }
//                                    if (ret.MesList.contains(SkillMesInfo.restrict)) {
//                                        System.out.println(ret.toString() + "mes:" + ret.MesList);
//                                    }
                                        break;
                                    }
                                    case mesToBoss:
//                                    System.out.println(ret.toString() + "mestoboss:" + ret.MesList);
                                        ret.mesToBoss = true;
                                        break;
                                }
                                if (Sinfo != null) {
                                    ret.info.put(Sinfo, mapleData.getData().toString());
                                }
                            } catch (Exception e) {
                                System.err.println(id);
                                e.printStackTrace();

                            }
                        }
                    }
                }
                for (int i = 1; i <= ret.trueMax; i++) {
                    ret.effects.add(MapleStatEffectFactory.loadSkillEffectFromData(ret, common, id, isHit, isBuff, ret.summon, i, "x", ret.notRemoved, ret.notIncBuffDuration));
                }
                ret.maxDamageOver = MapleDataTool.getInt("MDamageOver", common, 999999);
            }
        } else {
            if (levelData != null) {
                for (MapleData leve : levelData) {
                    ret.effects.add(MapleStatEffectFactory.loadSkillEffectFromData(ret, leve, id, isHit, isBuff, ret.summon, Byte.parseByte(leve.getName()), null, ret.notRemoved, ret.notIncBuffDuration));
                }
                ret.maxLevel = ret.effects.size();
                ret.trueMax = ret.effects.size();
            }
        }
        boolean loadPvpSkill = false;
        if (loadPvpSkill) {
            MapleData level2 = data.getChildByPath("PVPcommon");
            if (level2 != null) {
                ret.pvpEffects = new ArrayList<>();
                for (int i = 1; i <= ret.trueMax; i++) {
                    ret.pvpEffects.add(MapleStatEffectFactory.loadSkillEffectFromData(ret, level2, id, isHit, isBuff, ret.summon, i, "x", ret.notRemoved, ret.notIncBuffDuration));
                }
            }
        }
        MapleData reqDataRoot = data.getChildByPath("req");
        if (reqDataRoot != null) {
            for (MapleData reqData : reqDataRoot.getChildren()) {
                ret.requiredSkill.add(new Pair<>(reqData.getName(), (byte) MapleDataTool.getInt(reqData, 1)));
            }
        }
        ret.animationTime = 0;
        if (effect != null) {
            for (MapleData effectEntry : effect) {
                ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
            }
        }
        ret.hitTime = 0;
        if (hit != null) {
            for (MapleData hitEntry : hit) {
                ret.hitTime += MapleDataTool.getIntConvert("delay", hitEntry, 0);
            }
        }
        MapleData dat = data.getChildByPath("skillList");
        if (dat != null) {
            for (MapleData da : dat.getChildren()) {
                skillList.add(MapleDataTool.getInt(da, 0));
            }
        }
        ret.buffSkill = isBuff;
        switch (id) {
            case 夜光.星星閃光:
            case 夜光.黑暗球體:
            case 夜光.黑暗魔法強化:
                ret.masterLevel = ret.maxLevel;
                break;
        }

        MapleData growthInfo = data.getChildByPath("growthInfo/level");
        if (growthInfo != null) {
            for (MapleData expData : growthInfo.getChildren()) {
                ret.bonusExpInfo.put(Integer.parseInt(expData.getName()), MapleDataTool.getInt("maxExp", expData, 100000000));
            }
        }
        return ret;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MapleStatEffect getEffect(int level) {
        return getEffect(false, level);
    }

    public MapleStatEffect getPVPEffect(int level) {
        return getEffect(true, level - 1);
    }

    private MapleStatEffect getEffect(boolean ispvp, int level) {
        List<MapleStatEffect> effects = ispvp ? pvpEffects : this.effects;
        if (effects.size() < level) {
            if (effects.size() > 0) { //incAllskill
                return effects.get(effects.size() - 1);
            }
            return null;
        } else if (level <= 0) {
            return null;
        }
        return effects.get(level - 1);
    }

    public int getSkillType() {
        return skillType;
    }

    public List<Integer> getAllAnimation() {
        return animation;
    }

    public int getAnimation() {
        if (animation == null) {
            return -1;
        }
        return animation.get(Randomizer.nextInt(animation.size()));
    }

    public void setAnimation(List<Integer> animation) {
        this.animation = animation;
    }

    public List<Integer> getPsdSkills() {
        return psdSkills;
    }

    public int getPsd() {
        return psd;
    }

    public String getPsdDamR() {
        return psdDamR;
    }

    public String getTargetPlus() {
        return targetPlus;
    }

    public boolean isPVPDisabled() {
        return pvpDisabled;
    }

    public boolean isChargeSkill() {
        return chargeSkill;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public boolean isNotRemoved() {
        return notRemoved;
    }

    public boolean isRapidAttack() {
        return rapidAttack;
    }

    public boolean isPassiveSkill() {
        return passiveSkill;
    }

    public boolean isChargingSkill() {
        return chargingSkill;
    }

    public boolean hasRequiredSkill() {
        return requiredSkill.size() > 0;
    }

    public List<Pair<String, Byte>> getRequiredSkills() {
        return requiredSkill;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getTrueMax() {
        return trueMax;
    }

    public boolean combatOrders() {
        return combatOrders;
    }

    public boolean canBeLearnedBy(int job) {
        int skillForJob = id / 10000;
        if (JobConstants.is零轉職業(skillForJob)) {
            return skillForJob == JobConstants.getBeginner((short) job);
        }
        int skillGrade = JobConstants.getJobGrade(skillForJob);
        int jobGrade = JobConstants.getJobGrade(job);
        if (skillGrade > jobGrade) {
            return false;
        }
        if (JobConstants.is冒險家(skillForJob)) {
            if (JobConstants.getJobBranch(skillForJob) != JobConstants.getJobBranch(job)) {
                return false;
            }
            if ((skillForJob / 10) % 10 != 0 && (skillForJob / 10) % 10 != (job / 10) % 10) {
                return false;
            }
            switch (id) {
                case 影武者.迴避:
                case 影武者.二段跳:
                case 影武者.狂刃刺擊:
                    return JobConstants.is影武者(job);
                case 盜賊.幻化術:
                case 盜賊.魔法棒:
                case 盜賊.隱身術:
                case 盜賊.速度激發:
                case 盜賊.二段跳:
                case 卡莉.電光石火_一階段:
                case 盜賊.劈空斬:
                case 盜賊.雙飛斬:
                case 盜賊.終極隱身術:
                    return JobConstants.is盜賊(job) && !JobConstants.is影武者(job);
            }
            return true;
        }
        if (JobConstants.is皇家騎士團(skillForJob) || JobConstants.is末日反抗軍(skillForJob)) {
            if (JobConstants.is惡魔殺手(skillForJob)) {
                return JobConstants.is惡魔殺手(job);
            }
            if (JobConstants.is惡魔復仇者(skillForJob)) {
                return JobConstants.is惡魔復仇者(job);
            }
            return skillForJob / 100 == job / 100;
        }
        return JobConstants.getBeginner((short) skillForJob) == JobConstants.getBeginner((short) job);
    }

    public boolean isTimeLimited() {
        return timeLimited;
    }

    public boolean isFourthJob() {
        if (true) {
            return SkillConstants.isMasterLevelSkill(id);
        }
        if (id / 10000 == 11212) {
            return false;
        }
        if (isHyperSkill()) {
            return true;
        }
        switch (id) {
            case 英雄.戰鬥精通:
            case 黑騎士.闇靈復仇:
            case 拳霸.防禦姿態:
            case 拳霸.雙倍幸運骰子:
            case 槍神.進攻姿態:
            case 槍神.雙倍幸運骰子:
            case 精靈遊俠.旋風月光翻轉:
            case 精靈遊俠.進階光速雙擊:
            case 精靈遊俠.勇士的意志:
            case 影武者.飛毒殺:
            case 影武者.疾速:
            case 影武者.致命的飛毒殺:
            case 箭神.射擊術:
            case 狂豹獵人.狂暴天性:
            case 重砲指揮官.楓葉淨化:
            case 狂狼勇士.快速移動:
            case 狂狼勇士.動力精通II:
            case 狂狼勇士.終極研究II:
            case 狂狼勇士.楓葉淨化:
            case 龍魔導士.楓葉淨化:
            case 龍魔導士.歐尼斯的意志:
            case 米哈逸.戰鬥大師:
                return false;
        }
        switch (id / 10000) {
            case 2312:
            case 2412:
            case 2712:
            case 3122:
            case 6112:
            case 6512:
            case 14212:
                return true;
            case 10100:
                return id == 神之子.進階威力震擊;
            case 10110:
                return id == 神之子.進階武器投擲 || id == 神之子.進階迴旋之刃;
            case 10111:
                return id == 神之子.進階旋風 || id == 神之子.進階旋風急轉彎 || id == 神之子.進階旋風落葉斬;
            case 10112:
                return id == 神之子.進階碎地猛擊 || id == 神之子.進階暴風裂擊;
        }
        if ((getMaxLevel() <= 15 && !invisible && getMasterLevel() <= 0)) {
            return false;
        }
        if (JobConstants.is龍魔導士(id / 10000) && id / 10000 < 3000) { //龍神技能
            return ((id / 10000) % 10) >= 7;
        }
        if (id / 10000 >= 430 && id / 10000 <= 434) { //暗影雙刀技能
            return ((id / 10000) % 10) == 4 || getMasterLevel() > 0;
        }
        return ((id / 10000) % 10) == 2 && id < 90000000 && !isBeginnerSkill();
    }

    public boolean isVSkill() {
        return id >= 400000000 && id < 400060000;
    }

    public Element getElement() {
        return element;
    }

    public int getAnimationTime() {
        return animationTime;
    }

    public boolean getDisable() {
        return disable;
    }

    public int getFixLevel() {
        return this.fixLevel;
    }

    public int getMasterLevel() {
        return masterLevel;
    }

    public int getMaxMasterLevel() {
        return masterLevel <= 0 ? 0 : maxLevel;
    }

    public int getDisableNextLevelInfo() {
        return this.disableNextLevelInfo;
    }

    public int getDelay() {
        return delay;
    }

    public int getTamingMob() {
        return eventTamingMob;
    }

    public int getHyper() {
        return hyper;
    }

    public int getReqLevel() {
        return reqLev;
    }

    public int getMaxDamageOver() {
        return maxDamageOver;
    }

    public int getBonusExpInfo(int level) {
        if (bonusExpInfo.isEmpty()) {
            return -1;
        }
        if (bonusExpInfo.containsKey(level)) {
            return bonusExpInfo.get(level);
        }
        return -1;
    }

    public Map<Integer, Integer> getBonusExpInfo() {
        return bonusExpInfo;
    }

    public boolean isMagic() {
        return magic;
    }

    public boolean isMovement() {
        return casterMove;
    }

    public boolean isPush() {
        return pushTarget;
    }

    public boolean isPull() {
        return pullTarget;
    }

    public boolean isBuffSkill() {
        return buffSkill;
    }

    public boolean isSummonSkill() {
        return summon;
    }

    public boolean isNonAttackSummon() {
        return summon && minionAttack.isEmpty() && (minionAbility.isEmpty() || minionAbility.equals("taunt"));
    }

    public boolean isNonExpireSummon() {
        return selfDestructMinion;
    }

    public boolean isHyperSkill() {
        return hyper > 0 && reqLev > 0;
    }

    public boolean isHyperStat() {
        return hyperStat > 0;
    }

    /**
     * @return 公會技能
     */
    public boolean isGuildSkill() {
        int jobId = id / 10000;
        return jobId == 9100;
    }

    /**
     * 新手技能
     */
    public boolean isBeginnerSkill() {
        int jobId = id / 10000;
        return JobConstants.notNeedSPSkill(jobId);
    }

    /**
     * 管理員技能
     */
    public boolean isAdminSkill() {
        int jobId = id / 10000;
        return jobId == 800 || jobId == 900;
    }

    /**
     * 內在能力技能
     */
    public boolean isInnerSkill() {
        int jobId = id / 10000;
        return jobId == 7000;
    }

    /**
     * 特殊技能
     */
    public boolean isSpecialSkill() {
        int jobId = id / 10000;
        return jobId == 7000 || jobId == 7100 || jobId == 8000 || jobId == 9000 || jobId == 9100 || jobId == 9200 || jobId == 9201 || jobId == 9202 || jobId == 9203 || jobId == 9204;
    }

    public int getSkillByJobBook() {
        return getSkillByJobBook(id);
    }

    public int getSkillByJobBook(int skillid) {
        final int n2;
        if ((n2 = skillid / 10000) / 1000 > 0 || n2 < 100) {
            return -1;
        }
        final int cj;
        if ((cj = SkillConstants.dY(n2)) == 4 && skillid % 10000 == 1054) {
            return 5;
        }
        return cj;
//        switch (skillid / 10000) {
//            case 112:
//            case 122:
//            case 132:
//            case 212:
//            case 222:
//            case 232:
//            case 312:
//            case 322:
//            case 412:
//            case 422:
//            case 512:
//            case 522:
//                return 4;
//            case 111:
//            case 121:
//            case 131:
//            case 211:
//            case 221:
//            case 231:
//            case 311:
//            case 321:
//            case 411:
//            case 421:
//            case 511:
//            case 521:
//                return 3;
//            case 110:
//            case 120:
//            case 130:
//            case 210:
//            case 220:
//            case 230:
//            case 310:
//            case 320:
//            case 410:
//            case 420:
//            case 510:
//            case 520:
//                return 2;
//            case 100:
//            case 200:
//            case 300:
//            case 400:
//            case 500:
//                return 1;
//        }
//        return -1;
    }

    /**
     * 是否寵物被動觸發技能
     */
    public boolean isPetPassive() {
        return petPassive;
    }

    /**
     * 觸發技能的套裝ID
     */
    public int getSetItemReason() {
        return setItemReason;
    }

    /**
     * 觸發技能的套裝需要的件數
     */
    public int geSetItemPartsCount() {
        return setItemPartsCount;
    }

    /**
     * 是否是開關技能
     *
     * @return
     */
    public boolean isSwitch() {
        return isSwitch;
    }

    /*
     * 種族特性本能技能
     */
    public boolean isTeachSkills() {
        return SkillConstants.isTeachSkills(id);
    }

    /*
     * 鏈接技能技能
     */
    public boolean isLinkSkills() {
        return SkillConstants.isLinkSkills(id);
    }

    public boolean is老技能() {
        switch (id) {
            //case 聖魂劍士.魔天一擊old:
            //case 聖魂劍士.劍氣縱橫old:
            //case 聖魂劍士.靈魂old:
            //case 聖魂劍士.自身強化old:
            //case 聖魂劍士.HP增加old:
            //case 聖魂劍士.守護拳套_防具old:
            case 聖魂劍士.精準之劍:
            case 聖魂劍士.體能訓練:
                return true;
        }
        return false;
    }

    public boolean isAngelSkill() {
        return SkillConstants.is天使祝福戒指(id);
    }

    public boolean isLinkedAttackSkill() {
        return SkillConstants.isLinkedAttackSkill(id);
    }

    public boolean isDefaultSkill() {
        return getFixLevel() > 0;
    }

    public int getPPRecovery() {
        return ppRecovery;
    }

    public boolean isSoulSkill() {
        return soulSkill;
    }

    public void setSoulSkill(boolean soulSkill) {
        this.soulSkill = soulSkill;
    }

    public boolean isNotCooltimeReduce() {
        return notCooltimeReduce;
    }

    public boolean isNotCooltimeReset() {
        return notCooltimeReset;
    }

    public boolean isMesToBoss() {
        return mesToBoss;
    }

    public boolean isMobSkill() {
        return mobSkill;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public boolean isProfession() {
        return profession;
    }

    public boolean isIgnoreCounter() {
        return ignoreCounter;
    }

    public int getHitTime() {
        return hitTime;
    }

    public AbstractSkillHandler getHandler() {
        return SkillClassFetcher.getHandlerBySkill(id);
    }

    public void setRecipe(boolean recipe) {
        this.recipe = recipe;
    }

    public boolean isRecipe() {
        return recipe;
    }

    private enum SkillType {

        BUFF_ICO(10),
        PASSIVE(30, 31, 50, 51),//不同類型的被動技能
        PASSIVE_TRUE(50),//唯一類型的被動技能
        MONSTER_DEBUFF(32),//怪物異常效果技能
        SPAWN_OBJECT(33),//基本上全是召喚類技能
        MONSTER_DEBUFF_OR_CANCEL(34),//用於取消怪物特定效果的技能
        SINGLE_EFFECT(35),//非攻擊技能 楓葉淨化
        PROTECTIVE_MIST(36),//在地圖中召喚中特定的技能效果 如（煙幕彈）
        RESURRECT(38),//復活玩家技能
        MOVEMENT(40),//移動相關技能
        MOVEMENT_RANDOM(42),//龍之氣息 隨便移動到地圖上某個地方
        KEY_COMBO_ATTACK(52),//連擊技能
        COVER_SKILL(98),//雙重攻擊 終極攻擊 超級體 重裝武器精通 猴子衝擊
        ;//效果分類是特別奇怪的··但基本上都是 >= 10 (不包含上述聲明)
        final int[] vals;

        SkillType(int... vals) {
            this.vals = vals;
        }
    }

    @Override
    public String toString() {
        return SkillFactory.getSkillName(id) + "(" + id + ")";
    }

    /**
     * **
     * 是否顯示召喚獸圖標
     *
     * @return
     */
    public boolean isSummonedBuffIcon() {
        return showSummonedBuffIcon;
    }

    public boolean isInfo(SkillInfo info) {
        return this.info.containsKey(info);
    }

    public boolean getMesInfo(SkillMesInfo info) {
        for (SkillMesInfo skillMesInfo : MesList) {
            if (skillMesInfo == info) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getSkillList() {
        return skillList;
    }
}
