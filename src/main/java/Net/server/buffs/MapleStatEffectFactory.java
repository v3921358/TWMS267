package Net.server.buffs;

import Client.MapleCharacter;
import Client.MapleTraitType;
import Client.SecondaryStat;
import Client.inventory.MapleInventoryType;
import Client.skills.Skill;
import Client.skills.SkillInfo;
import Client.skills.SkillMesInfo;
import Client.skills.handler.AbstractSkillHandler;
import Client.status.MonsterStatus;
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
import Config.constants.skills.冒險家_技能群組.箭神;
import Config.constants.skills.皇家騎士團_技能群組.破風使者;
import Net.server.MapleOverrideData;
import Net.server.MapleStatInfo;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataTool;
import Plugin.provider.MapleDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.CaltechEval;
import tools.Pair;
import tools.Triple;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MapleStatEffectFactory {

    private static Logger log = LoggerFactory.getLogger(MapleStatEffectFactory.class);

    /**
     * 加載技能的BUFF狀態
     */
    public static MapleStatEffect loadSkillEffectFromData(Skill skill, MapleData source, int skillid, boolean isHit, boolean overtime, boolean isSummon, int level, String variables, boolean notRemoved, boolean notIncBuffDuration) {
        return loadFromData(skill, source, skillid, isHit, overtime, isSummon, level, variables, notRemoved, notIncBuffDuration);
    }

    /**
     * 加載道具的BUFF狀態
     */
    public static MapleStatEffect loadItemEffectFromData(MapleData source, int itemid) {
        return loadFromData(null, source, -itemid, false, false, false, 1, null, false, false);
    }

    /**
     * 添加一些常用但BUFF的參數不為0的BUFF狀態信息
     */
    private static void addBuffStatPairToListIfNotZero(Map<SecondaryStat, Integer> list, SecondaryStat buffstat, Integer val) {
        if (val != 0) {
            list.put(buffstat, val);
        }
    }

    public static int parseEval(String data, int level) {
        String variables = "x";
        String dddd = data.toLowerCase().replace(variables, String.valueOf(level));
        if (dddd.charAt(0) == '-') { //-30+3*x
            if (dddd.charAt(1) == 'u' || dddd.charAt(1) == 'd') { //-u(x/2)
                dddd = "n(" + dddd.substring(1) + ")"; //n(u(x/2))
            } else {
                dddd = "n" + dddd.substring(1); //n30+3*x
            }
        } else if (dddd.charAt(0) == '=') { //lol nexon and their mistakes
            dddd = dddd.substring(1);
        }
        return (int) (new CaltechEval(dddd).evaluate());
    }

    private static int parseEval(String path, MapleData source, int def, String variables, int level) {
        return parseEval(path, source, def, variables, level, "");
    }

    private static int parseEval(String path, MapleData source, int def, String variables, int level, String d) {
        return (int) parseEvalDouble(path, source, def, variables, level, d);
    }

    private static double parseEvalDouble(String path, MapleData source, int def, String variables, int level) {
        return parseEvalDouble(path, source, def, variables, level, "");
    }

    private static double parseEvalDouble(String path, MapleData source, int def, String variables, int level, String d) {
        if (variables == null) {
            return MapleDataTool.getIntConvert(path, source, def);
        } else {
            String dddd;
            if (d.isEmpty()) {
                MapleData dd = source.getChildByPath(path);
                if (dd == null) {
                    return def;
                }
                if (dd.getType() != MapleDataType.STRING) {
                    return MapleDataTool.getIntConvert(path, source, def);
                }
                dddd = MapleDataTool.getString(dd).toLowerCase().replace("\r\n", "");
            } else {
                dddd = d;
            }
            dddd = dddd.replace(variables, String.valueOf(level));
            if (dddd.isEmpty()) {
                return 0.0;
            } else if (dddd.charAt(0) == '-') { //-30+3*x
                if (dddd.charAt(1) == 'u' || dddd.charAt(1) == 'd') { //-u(x/2)
                    dddd = "n(" + dddd.substring(1) + ")"; //n(u(x/2))
                } else {
                    dddd = "n" + dddd.substring(1); //n30+3*x
                }
            } else if (dddd.charAt(0) == '=') { //lol nexon and their mistakes
                dddd = dddd.substring(1);
            } else if (dddd.endsWith("y")) {
                dddd = dddd.substring(4).replace("y", String.valueOf(level));
            } else if (dddd.contains("%")) {
                dddd = dddd.replace("%", "/100");
            }
            return new CaltechEval(dddd).evaluate();
        }
    }

    private static MapleStatEffect loadFromData(Skill skillObj, MapleData source, int sourceid, boolean isHit, boolean overTime, boolean isSummon, int level, String variables, boolean notRemoved, boolean notIncBuffDuration) {
        MapleStatEffect ret = new MapleStatEffect();
        ret.setSourceid(sourceid);
        ret.setLevel((byte) level);
        ret.setHit(isHit);

        if (source == null) {
            return ret;
        }

        EnumMap<MapleStatInfo, Integer> info = new EnumMap<>(MapleStatInfo.class);
        EnumMap<MapleStatInfo, Double> infoDouble = new EnumMap<>(MapleStatInfo.class);
        double val;
        for (MapleStatInfo i : MapleStatInfo.values()) {
            try {
                if (i.isSpecial()) {
                    val = parseEvalDouble(i.name().substring(0, i.name().length() - 1), source, i.getDefault(), variables, level, MapleOverrideData.getOverrideValue(sourceid, i.name()));
                } else {
                    val = parseEvalDouble(i.name(), source, i.getDefault(), variables, level, MapleOverrideData.getOverrideValue(sourceid, i.name()));
                }
                if (val % 1.0 != 0.0) {
                    infoDouble.put(i, val);
                }
                info.put(i, (int) val);
            } catch (Exception e) {
                log.error("加載技能數據出錯，id:" + sourceid + ", msi: " + i, e);
            }
        }
        ret.setInfo(info);
        ret.setInfoD(infoDouble);
        ret.setHpR(parseEval("hpR", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "hpR")) / 100.0);
        ret.setMpR(parseEval("mpR", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "mpR")) / 100.0);
        ret.setIgnoreMob((short) parseEval("ignoreMobpdpR", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "ignoreMobpdpR")));
        ret.setThaw((short) parseEval("thaw", source, 0, variables, level));
        ret.setInterval(parseEval("interval", source, 0, variables, level));
        ret.setExpinc(parseEval("expinc", source, 0, variables, level));
        ret.setExp(parseEval("exp", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "exp")));
        ret.setMorphId(parseEval("morph", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "morph")));
        ret.setCosmetic(parseEval("cosmetic", source, 0, variables, level));
        ret.setSlotCount((byte) parseEval("slotCount", source, 0, variables, level)); //礦(藥)背包道具需要
        ret.setSlotPerLine((byte) parseEval("slotPerLine", source, 0, variables, level)); //礦(藥)背包道具需要
        ret.setPreventslip((byte) parseEval("preventslip", source, 0, variables, level));
        ret.setUseLevel((short) parseEval("useLevel", source, 0, variables, level));
        ret.setImmortal((byte) parseEval("immortal", source, 0, variables, level));
        ret.setType((byte) parseEval("type", source, 0, variables, level));
        ret.setBs((byte) parseEval("bs", source, 0, variables, level));
        ret.setIndiePdd((short) parseEval("indiePdd", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "indiePdd")));
        ret.setIndieMdd((short) parseEval("indieMdd", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "indieMdd")));
        ret.setExpBuff(parseEval("expBuff", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "expBuff")));
        ret.setCashup(parseEval("cashBuff", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "cashBuff")));
        ret.setItemup(parseEval("itemupbyitem", source, 0, variables, level));
        ret.setMesoup(parseEval("mesoupbyitem", source, 0, variables, level));
        ret.setBerserk(parseEval("berserk", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "berserk")));
        ret.setBerserk2(parseEval("berserk2", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "berserk2")));
        ret.setBooster(parseEval("booster", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "booster")));
        ret.setLifeId((short) parseEval("lifeId", source, 0, variables, level));
        ret.setInflation((short) parseEval("inflation", source, 0, variables, level));
        ret.setImhp((short) parseEval("imhp", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "imhp")));
        ret.setImmp((short) parseEval("immp", source, 0, variables, level, MapleOverrideData.getOverrideValue(sourceid, "immp")));
        ret.setIllusion(parseEval("illusion", source, 0, variables, level));
        ret.setConsumeOnPickup(parseEval("consumeOnPickup", source, 0, variables, level));
        if (ret.getConsumeOnPickup() == 1) {
            if (parseEval("party", source, 0, variables, level) > 0) {
                ret.setConsumeOnPickup(2);
            }
        }
        ret.setRecipe(parseEval("recipe", source, 0, variables, level));
        ret.setRecipeUseCount((byte) parseEval("recipeUseCount", source, 0, variables, level));
        ret.setRecipeValidDay((byte) parseEval("recipeValidDay", source, 0, variables, level));
        ret.setReqSkillLevel((byte) parseEval("reqSkillLevel", source, 0, variables, level));
        ret.setEffectedOnAlly((byte) parseEval("effectedOnAlly", source, 0, variables, level));
        ret.setEffectedOnEnemy((byte) parseEval("effectedOnEnemy", source, 0, variables, level));
//        ret.incPVPdamage = (short) parseEval("incPVPDamage", source, 0, variables, level);
        ret.setMoneyCon(parseEval("moneyCon", source, 0, variables, level));
        int x = ret.getX();
        ret.setMoveTo(parseEval("moveTo", source, x > 100000000 && x <= 999999999 ? x : -1, variables, level));
//        ret.repeatEffect = ret.is戰法靈氣(); //自動重複使用的BUFF

        int charColor = 0;
        String cColor = MapleDataTool.getString("charColor", source, null);
        if (cColor != null) {
            charColor |= Integer.parseInt("0x" + cColor.substring(0, 2));
            charColor |= Integer.parseInt("0x" + cColor.substring(2, 4) + "00");
            charColor |= Integer.parseInt("0x" + cColor.substring(4, 6) + "0000");
            charColor |= Integer.parseInt("0x" + cColor.substring(6, 8) + "000000");
        }
        ret.setCharColor(charColor);
        EnumMap<MapleTraitType, Integer> traits = new EnumMap<>(MapleTraitType.class);
        for (MapleTraitType t : MapleTraitType.values()) {
            int expz = parseEval(t.name() + "EXP", source, 0, variables, level);
            if (expz != 0) {
                traits.put(t, expz);
            }
        }
        ret.setTraits(traits);
        List<SecondaryStat> cure = new ArrayList<>(5);
        if (parseEval("poison", source, 0, variables, level) > 0) {
            cure.add(SecondaryStat.Poison);
        }
        if (parseEval("seal", source, 0, variables, level) > 0) {
            cure.add(SecondaryStat.Seal);
        }
        if (parseEval("darkness", source, 0, variables, level) > 0) {
            cure.add(SecondaryStat.Darkness);
        }
        if (parseEval("weakness", source, 0, variables, level) > 0) {
            cure.add(SecondaryStat.Weakness);
        }
        if (parseEval("curse", source, 0, variables, level) > 0) {
            cure.add(SecondaryStat.Curse);
        }
        if (parseEval("painmark", source, 0, variables, level) > 0) {
            cure.add(SecondaryStat.PainMark);
        }
        ret.setCureDebuffs(cure);
        List<Integer> petsCanConsume = new ArrayList<>();
        for (int i = 0; true; i++) {
            int dd = parseEval(String.valueOf(i), source, 0, variables, level);
            if (dd > 0) {
                petsCanConsume.add(dd);
            } else {
                break;
            }
        }
        ret.setPetsCanConsume(petsCanConsume);
        MapleData mdd = source.getChildByPath("0");
        if (mdd != null && mdd.getChildren().size() > 0) {
            ret.setMobSkill((short) parseEval("mobSkill", mdd, 0, variables, level));
            ret.setMobSkillLevel((short) parseEval("level", mdd, 0, variables, level));
        } else {
            ret.setMobSkill((short) 0);
            ret.setMobSkillLevel((short) 0);
        }
        MapleData pd = source.getChildByPath("randomPickup");
        if (pd != null) {
            ArrayList<Integer> randomPickup = new ArrayList<>();
            for (MapleData p : pd) {
                randomPickup.add(MapleDataTool.getInt(p));
            }
            ret.setRandomPickup(randomPickup);
        }
        MapleData ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.setLt((Point) ltd.getData());
            ret.setRb((Point) source.getChildByPath("rb").getData());
        }
        MapleData lt2d = source.getChildByPath("lt2");
        if (lt2d != null) {
            ret.setLt2((Point) lt2d.getData());
            ret.setRb2((Point) source.getChildByPath("rb2").getData());
        }
        MapleData lt3d = source.getChildByPath("lt3");
        if (lt3d != null) {
            ret.setLt3((Point) lt3d.getData());
            ret.setRb3((Point) source.getChildByPath("rb3").getData());
        }
        MapleData ltc = source.getChildByPath("con");
        if (ltc != null) {
            List<Pair<Integer, Integer>> availableMap = new ArrayList<>();
            for (MapleData ltb : ltc) {
                availableMap.add(new Pair<>(MapleDataTool.getInt("sMap", ltb, 0), MapleDataTool.getInt("eMap", ltb, 999999999)));
            }
            ret.setAvailableMap(availableMap);
        }
        int totalprob = 0;
        MapleData lta = source.getChildByPath("reward");
        if (lta != null) {
            ret.setRewardMeso(parseEval("meso", lta, 0, variables, level));
            MapleData ltz = lta.getChildByPath("case");
            if (ltz != null) {
                ArrayList<Triple<Integer, Integer, Integer>> rewardItem = new ArrayList<>();
                for (MapleData lty : ltz) {
                    rewardItem.add(new Triple<>(MapleDataTool.getInt("id", lty, 0), MapleDataTool.getInt("count", lty, 0), MapleDataTool.getInt("prop", lty, 0)));
                    totalprob += MapleDataTool.getInt("prob", lty, 0);
                }
                ret.setRewardItem(rewardItem);
            }
        } else {
            ret.setRewardMeso(0);
        }
        ret.setTotalprob(totalprob);
        // start of Net.server calculated stuffs
        if (ret.isSkill()) {
            int priceUnit = ret.getInfo().get(MapleStatInfo.priceUnit); // Guild skills
            if (priceUnit > 0) {
                int price = ret.getInfo().get(MapleStatInfo.price);
                int extendPrice = ret.getInfo().get(MapleStatInfo.extendPrice);
                ret.getInfo().put(MapleStatInfo.price, price * priceUnit);
                ret.getInfo().put(MapleStatInfo.extendPrice, extendPrice * priceUnit);
            }
            switch (sourceid) {
                case 英雄.終極攻擊:
                case 聖騎士.終極之劍:
                case 黑騎士.終極之槍:
                case 箭神.終極之弓:
                case 神射手.終極之弩:
                case 火毒.瞬間移動精通:
                case 冰雷.瞬間移動精通:
                case 主教.瞬間移動精通:
                case 煉獄巫師.黑暗閃電:
                case 狂豹獵人.終極攻擊:
                case 狂豹獵人.進階終極攻擊: //V.100新增
                case 龍魔導士.龍之火花:
                case 龍魔導士.歐尼斯的意志:
                case 英雄.進階終極攻擊:
                case 箭神.進階終極攻擊:
                case 精靈遊俠.終極攻擊_雙弩槍:
                case 精靈遊俠.進階終極攻擊:
                case 狂狼勇士.終極攻擊: //V.100新增
                case 狂狼勇士.進階終極攻擊: //V.100新增
                case 狂豹獵人.召喚美洲豹_銀灰:
                case 狂豹獵人.召喚美洲豹_暗黃:
                case 狂豹獵人.召喚美洲豹_血紅:
                case 狂豹獵人.召喚美洲豹_紫光:
                case 狂豹獵人.召喚美洲豹_深藍:
                case 狂豹獵人.召喚美洲豹_傑拉:
                case 狂豹獵人.召喚美洲豹_白雪:
                case 狂豹獵人.召喚美洲豹_歐尼斯:
                case 狂豹獵人.召喚美洲豹_地獄裝甲:
                    ret.getInfo().put(MapleStatInfo.mobCount, 6);
                    break;
                case 惡魔復仇者.強化超越:
                    ret.getInfo().put(MapleStatInfo.attackCount, 2);
                    break;
                case 夜光.光明長槍:
                    ret.getInfo().put(MapleStatInfo.attackCount, 4);
                    break;
                case 傑諾.追縱火箭:
                    ret.getInfo().put(MapleStatInfo.attackCount, 4);
                    break;
                case 凱撒.意志之劍:
                case 凱撒.意志之劍_變身:
                    ret.getInfo().put(MapleStatInfo.attackCount, 3);
                    break;
                case 凱撒.進階意志之劍:
                case 凱撒.進階意志之劍_變身:
                    ret.getInfo().put(MapleStatInfo.attackCount, 5);
                    break;
                case 卡莉.藝術_阿斯特拉:
                    ret.getInfo().put(MapleStatInfo.attackCount, 8);
                    break;
                case 破風使者.Switch_type_風妖精之箭I:
                case 破風使者.Switch_type_風妖精之箭II:
                case 破風使者.Switch_type_風妖精之箭Ⅲ:
                case 破風使者.暴風加護:
                    ret.getInfo().put(MapleStatInfo.attackCount, 6);
                    break;
            }
        }
        if (!ret.isSkill() && ret.getInfo().get(MapleStatInfo.time) > -1) {
            ret.setOverTime(true);
        } else {
            if (ret.getInfo().get(MapleStatInfo.time) < 1000) {
                ret.getInfo().put(MapleStatInfo.time, (ret.getInfo().get(MapleStatInfo.time) * 1000L) >= Integer.MAX_VALUE ? Integer.MAX_VALUE : ret.getInfo().get(MapleStatInfo.time) * 1000); // items have their times stored in ms, of course
            }
            //ret.getInfo().put(MapleStatInfo.subTime, (ret.getInfo().get(MapleStatInfo.subTime)));
            ret.setOverTime(overTime || ret.isMorph() || ret.is戒指技能() || ret.getSummonMovementType() != null);
            ret.setNotRemoved(notRemoved);
            ret.setNotIncBuffDuration(notIncBuffDuration);
        }

        Map<MonsterStatus, Integer> monsterStatus = new EnumMap<>(MonsterStatus.class);
        EnumMap<SecondaryStat, Integer> statups = new EnumMap<>(SecondaryStat.class);

        AbstractSkillHandler handler = ret.getSkillHandler();
        int handleRes = -1;
        if (handler != null) {
            handleRes = handler.onSkillLoad(statups, monsterStatus, ret);
            if (handleRes == 0) {
                return ret;
            }
        }

        if (handleRes == -1 && ret.isOverTime() && ret.getSummonMovementType() == null) {
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.STR, ret.getInfo().get(MapleStatInfo.str));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.INT, ret.getInfo().get(MapleStatInfo.int_));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.DEX, ret.getInfo().get(MapleStatInfo.dex));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.LUK, ret.getInfo().get(MapleStatInfo.luk));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.PAD, ret.getInfo().get(MapleStatInfo.pad));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.PDD, ret.getInfo().get(MapleStatInfo.pdd));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.MAD, ret.getInfo().get(MapleStatInfo.mad));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.ACC, ret.getInfo().get(MapleStatInfo.acc));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.EVA, ret.getInfo().get(MapleStatInfo.eva));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.EVAR, ret.getInfo().get(MapleStatInfo.evaR));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.Craft, 0);
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.Speed, sourceid == 煉獄巫師.黃色光環 ? ret.getInfo().get(MapleStatInfo.x) : ret.getInfo().get(MapleStatInfo.speed));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.Jump, ret.getInfo().get(MapleStatInfo.jump));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.EMHP, ret.getInfo().get(MapleStatInfo.emhp));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.EMMP, ret.getInfo().get(MapleStatInfo.emmp));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.EPAD, ret.getInfo().get(MapleStatInfo.epad));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.EMAD, ret.getInfo().get(MapleStatInfo.emad));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.EPDD, ret.getInfo().get(MapleStatInfo.epdd));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.Booster, ret.getBooster());
            if (sourceid != 狂豹獵人.狂獸附體) { //龍神的這個技能是被動加的HP上限 所以這個地方就不在加了
                addBuffStatPairToListIfNotZero(statups, SecondaryStat.MaxHP, ret.getInfo().get(MapleStatInfo.mhpR));
            }
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.MaxMP, ret.getInfo().get(MapleStatInfo.mmpR));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.Thaw, (int) ret.getThaw());
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.MesoUpByItem, ItemConstants.getModifier(Math.abs(ret.getSourceId()), ret.getMesoup())); // defaults to 2x
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.DefenseState, ret.getIllusion()); //複製克隆BUFF
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.DojangBerserk, ret.getBerserk2());
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.RepeatEffect, ret.getBerserk());
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.ExpBuffRate, ret.getExpBuff()); // 經驗
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.Inflation, ret.getInflation());
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.DropRate, ItemConstants.getModifier(Math.abs(ret.getSourceId()), ret.getItemup())); // defaults to 2x
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.DropRate, ret.getInfo().get(MapleStatInfo.dropRate));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.PlusExpRate, ret.getInfo().get(MapleStatInfo.plusExpRate));

            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndiePAD, ret.getInfo().get(MapleStatInfo.indiePad));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieMAD, ret.getInfo().get(MapleStatInfo.indieMad));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndiePDD, ret.getInfo().get(MapleStatInfo.indiePdd));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieMHP, (int) ret.getImhp());
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieMMP, (int) ret.getImmp());
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieMHP, ret.getInfo().get(MapleStatInfo.indieMhp));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieMMP, ret.getInfo().get(MapleStatInfo.indieMmp));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieACC, ret.getInfo().get(MapleStatInfo.indieAcc));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieEVA, ret.getInfo().get(MapleStatInfo.indieEva));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieJump, ret.getInfo().get(MapleStatInfo.indieJump));
            if (sourceid != 機甲戰神.合金盔甲_人型 && sourceid != 機甲戰神.合金盔甲終極) {
                addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieSpeed, ret.getInfo().get(MapleStatInfo.indieSpeed));
            }
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieAllStat, ret.getInfo().get(MapleStatInfo.indieAllStat));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieEXP, ret.getInfo().get(MapleStatInfo.indieExp));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieBooster, ret.getInfo().get(MapleStatInfo.indieBooster));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieSTR, ret.getInfo().get(MapleStatInfo.indieSTR));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieDEX, ret.getInfo().get(MapleStatInfo.indieDEX));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieINT, ret.getInfo().get(MapleStatInfo.indieINT));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieLUK, ret.getInfo().get(MapleStatInfo.indieLUK));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieDamR, ret.getInfo().get(MapleStatInfo.indieDamR));
            addBuffStatPairToListIfNotZero(statups, SecondaryStat.IndieCr, ret.getInfo().get(MapleStatInfo.indieCr));
        }

        //自動添加持續傷害
        if (skillObj != null) {
            if (skillObj.isInfo(SkillInfo.dotType)) {//持續傷害
                addDebuffStatPairToListIfNotZero(monsterStatus, MonsterStatus.Burned, ret.getInfo().get(MapleStatInfo.dot), ret.getInfo().get(MapleStatInfo.dot));
            }
            if (skillObj.isInfo(SkillInfo.dot)) {//持續傷害
                addDebuffStatPairToListIfNotZero(monsterStatus, MonsterStatus.Burned, ret.getInfo().get(MapleStatInfo.dot), ret.getInfo().get(MapleStatInfo.dot));
            }
            if (skillObj.getMesInfo(SkillMesInfo.stun)) {//暈
                addDebuffStatPairToListIfNotZero(monsterStatus, MonsterStatus.Stun, 1, 1);
            }
            if (skillObj.getMesInfo(SkillMesInfo.darkness)) {//黑暗
                addDebuffStatPairToListIfNotZero(monsterStatus, MonsterStatus.Blind, 1, 1);
            }
            if (skillObj.getMesInfo(SkillMesInfo.seal)) {//封印
                addDebuffStatPairToListIfNotZero(monsterStatus, MonsterStatus.Seal, 1, 1);
            }
            if (skillObj.getMesInfo(SkillMesInfo.cold)) {//寒冷的
                addDebuffStatPairToListIfNotZero(monsterStatus, MonsterStatus.Speed, ret.getInfo().get(MapleStatInfo.s), ret.getInfo().get(MapleStatInfo.s));
            }
            if (skillObj.getMesInfo(SkillMesInfo.freeze)) {//結冰
                addDebuffStatPairToListIfNotZero(monsterStatus, MonsterStatus.Freeze, 1, 1);
            }
            if (skillObj.getMesInfo(SkillMesInfo.slow)) {//緩慢
                addDebuffStatPairToListIfNotZero(monsterStatus, MonsterStatus.Speed, ret.getInfo().get(MapleStatInfo.x), ret.getInfo().get(MapleStatInfo.x));
            }
        }

        //追加經驗值和道具
        addDebuffStatPairToListIfNotZero(monsterStatus, MonsterStatus.Showdown, ret.getInfo().get(MapleStatInfo.expR), ret.getInfo().get(MapleStatInfo.expR));

        if (handleRes != -1) {
        } else if (ret.isSkill()) {
            switch (sourceid) {
                case 80001079:
                    statups.put(SecondaryStat.CarnivalAttack, ret.info.get(MapleStatInfo.damage));
                    break;
                case 80001080:
                    statups.put(SecondaryStat.CarnivalDefence, ret.info.get(MapleStatInfo.x) + ret.info.get(MapleStatInfo.x) * 1000);
                    break;
                case 80001081:
                    statups.put(SecondaryStat.CarnivalExp, ret.info.get(MapleStatInfo.x));
                    break;
                case 80011247:
                    ret.info.put(MapleStatInfo.time, 1000);
                    statups.put(SecondaryStat.BodyRectGuardPrepare, 1);
                    break;
                case 80011248:
                    statups.clear();
                    statups.put(SecondaryStat.IndiePDD, ret.getInfo().get(MapleStatInfo.indiePdd));
                    statups.put(SecondaryStat.IndieStance, ret.getInfo().get(MapleStatInfo.indieStance));
                    statups.put(SecondaryStat.DawnShield_ExHP, 0);
                    break;
                case 80011249:
                    statups.put(SecondaryStat.DawnShield_WillCare, ret.getInfo().get(MapleStatInfo.x));
                    break;
                case 80011993:
                    ret.info.put(MapleStatInfo.time, 2100000000);
                    ret.info.put(MapleStatInfo.mpCon, 0);
                    statups.put(SecondaryStat.ErdaStack, 1);
                    break;
                case 80003342: // 永續戒指
                    statups.put(SecondaryStat.KaringDoolAdvantage, 1);
                case 80001089:
                case 80001242:
                    statups.put(SecondaryStat.NewFlying, 1);
                    break;
                case 80011513:
                    statups.put(SecondaryStat.DamAbsorbShield, ret.info.get(MapleStatInfo.x));
                    statups.put(SecondaryStat.IndieEXP, ret.info.get(MapleStatInfo.y));
                    break;
                case 9101008:
                    statups.put(SecondaryStat.MaxHP, ret.getInfo().get(MapleStatInfo.x));
                    statups.put(SecondaryStat.MaxMP, ret.getInfo().get(MapleStatInfo.y));
                    break;
                case 9101002: //沒有這個GM技能
                    statups.put(SecondaryStat.HolySymbol, ret.getInfo().get(MapleStatInfo.x));
                    break;
                case 80001034: //神聖拯救者的祝福
                case 80001035: //神聖拯救者的祝福
                case 80001036: //神聖拯救者的祝福
                    statups.put(SecondaryStat.Event, 1);
                    break;
                case 9101003: //沒有這個GM技能
                    statups.clear();
                    statups.put(SecondaryStat.IndiePAD, ret.getInfo().get(MapleStatInfo.indiePad));
                    statups.put(SecondaryStat.IndieMAD, ret.getInfo().get(MapleStatInfo.indieMad));
                    statups.put(SecondaryStat.IndieMHPR, ret.getInfo().get(MapleStatInfo.indieMhpR));
                    statups.put(SecondaryStat.IndieMMPR, ret.getInfo().get(MapleStatInfo.indieMmpR));
                    statups.put(SecondaryStat.PDD, ret.getInfo().get(MapleStatInfo.pdd));
                    statups.put(SecondaryStat.Speed, ret.getInfo().get(MapleStatInfo.speed));
                    break;
                case 9101000: //沒有這個GM技能
                    ret.setHpR(1.0);
                    break;
                case 80001428: // 重生的輪行蹤
                    statups.clear();
                    statups.put(SecondaryStat.IndieAsrR, ret.getInfo().get(MapleStatInfo.indieAsrR));
                    statups.put(SecondaryStat.IndieStance, ret.getInfo().get(MapleStatInfo.indieStance));
                    statups.put(SecondaryStat.DotHealHPPerSecond, ret.getInfo().get(MapleStatInfo.dotHealHPPerSecondR));
                    statups.put(SecondaryStat.DotHealMPPerSecond, ret.getInfo().get(MapleStatInfo.dotHealMPPerSecondR));
                    break;
                case 80001430: // 崩壞之輪行蹤
                    statups.clear();
                    statups.put(SecondaryStat.IndieBooster, ret.getInfo().get(MapleStatInfo.indieBooster));
                    statups.put(SecondaryStat.IndieDamR, ret.getInfo().get(MapleStatInfo.indieDamR));
                    break;
                case 80001432: // 破滅之輪行蹤
                case 80001754: // 解放黑暗之輪
                    statups.clear();
                    statups.put(SecondaryStat.IndieDamR, ret.getInfo().get(MapleStatInfo.indieDamR));
                    break;
                case 80002902:
                    ret.setOverTime(false);
                    statups.clear();
                    statups.put(SecondaryStat.Blind, 1);
                    break;
                case 80001752:
                case 80001756: // 解放雷之輪
                    statups.put(SecondaryStat.RandAreaAttack, ret.getInfo().get(MapleStatInfo.x));
                    break;
                case 80001761:
                case 80001753:
                case 80001757: // 解放地震之輪
                    statups.clear();
                    statups.put(SecondaryStat.IndieJump, ret.getInfo().get(MapleStatInfo.indieJump));
                    statups.put(SecondaryStat.IndieSpeed, ret.getInfo().get(MapleStatInfo.indieSpeed));
                    statups.put(SecondaryStat.IndieNotDamaged, 1);
                    statups.put(SecondaryStat.Inflation, ret.getInfo().get(MapleStatInfo.x));
                    break;
                case 80001875: // 輪之力解放-超越
                    statups.put(SecondaryStat.FixCoolTime, ret.getInfo().get(MapleStatInfo.fixCoolTime));
                    break;
                case 80001876: // 輪之力解放-轟炸
                    statups.put(SecondaryStat.RideVehicle, 1939006);
                    break;
                case 80002280: // 解放的輪之力
                    statups.put(SecondaryStat.IndieEXP, ret.getInfo().get(MapleStatInfo.indieExp));
                    break;
                case 80002281: // 解放貪欲之輪
                    statups.put(SecondaryStat.MesoUp, 100); // mesoAmountUp
                    break;
                case 80002282: // 封印的輪之力
                    statups.put(SecondaryStat.RuneStoneNoTime, 1);
                    break;
                case 80002888: // 淨化之輪解放
                    statups.put(SecondaryStat.RunePurification, 1);
                    break;
                case 80002889: // 光束之輪解放
                    statups.put(SecondaryStat.IndieBuffIcon, 1);
                    break;
                case 80002890: // 轉移之輪解放
                    statups.put(SecondaryStat.RuneContagion, 1);
                    break;
                case 80001371: // 妖精密語
                    statups.put(SecondaryStat.IndieMHPR, ret.getInfo().get(MapleStatInfo.indieMhpR));
                    statups.put(SecondaryStat.IndieMMPR, ret.getInfo().get(MapleStatInfo.indieMmpR));
                    statups.put(SecondaryStat.IndieBDR, ret.getInfo().get(MapleStatInfo.x));
                    break;
                case 80001312:
                case 80001313:
                case 80001314:
                case 80001315:
                    statups.put(SecondaryStat.RideVehicle, 1932187 + (sourceid - 80001312));
                    break;
                case 80001155:
                    statups.put(SecondaryStat.IndieDamR, ret.getInfo().get(MapleStatInfo.indieDamR));
                    break;
                case 80011158:
                    statups.clear();
                    statups.put(SecondaryStat.IndiePADR, ret.getInfo().get(MapleStatInfo.x));
                    break;
                case 80001218: // 無雙之力
                    statups.put(SecondaryStat.SoulSkillDamageUp, ret.getX());
                    break;
                case 90001006:
                    monsterStatus.put(MonsterStatus.Freeze, 1);
                    ret.getInfo().put(MapleStatInfo.time, ret.getInfo().get(MapleStatInfo.time) * 2);
                    break;
                case 9101004:
                    ret.getInfo().put(MapleStatInfo.time, 0);
                    statups.put(SecondaryStat.DarkSight, ret.getInfo().get(MapleStatInfo.x));
                    break;
                case 23111004:
                    statups.put(SecondaryStat.AddAttackCount, ret.getInfo().get(MapleStatInfo.x));
                    break;
                case 36101002:
                    statups.put(SecondaryStat.CriticalBuff, ret.getInfo().get(MapleStatInfo.x));
                    break;
                case 36121004:
                    statups.put(SecondaryStat.Stance, ret.getInfo().get(MapleStatInfo.x));
                    statups.put(SecondaryStat.IgnoreTargetDEF, ret.getInfo().get(MapleStatInfo.y));
                    break;
                case 9001020: //沒有這個GM技能
                case 9101020: //沒有這個GM技能
                    monsterStatus.put(MonsterStatus.Seal, 1);
                    break;
                case 90001002: //沒有這個GM技能
                    monsterStatus.put(MonsterStatus.Speed, ret.getInfo().get(MapleStatInfo.x));
                    break;
                case 80011540: //露希妲的惡夢 * 耳環 SKILL */
                    monsterStatus.put(MonsterStatus.Smite, 1);
                    break;
                case 90001003:
                    monsterStatus.put(MonsterStatus.Poison, 1);
                    break;
                case 90001005:
                    monsterStatus.put(MonsterStatus.Seal, 1);
                    break;
            }
            if (JobConstants.is零轉職業(sourceid / 10000)) { //新手技能BUFF處理
                switch (sourceid % 10000) {
                    //angelic blessing: HACK, we're actually supposed to use the passives for atk/matk buff
                    case 99:  //破冰巨劍
                    case 104: //蝸居詛咒
                        monsterStatus.put(MonsterStatus.Speed, 1);
                        ret.getInfo().put(MapleStatInfo.time, ret.getInfo().get(MapleStatInfo.time) * 2); // freezing skills are a little strange
                        break;
                    case 103: //霸天斧
                        monsterStatus.put(MonsterStatus.Stun, 1);
                        break;
                    case 1001: //團隊治療
                        if (ret.is潛入()) { //潛入BUFF
                            statups.put(SecondaryStat.Sneak, ret.getInfo().get(MapleStatInfo.x));
                        } else {
                            statups.put(SecondaryStat.Regen, ret.getInfo().get(MapleStatInfo.x));
                        }
                        break;
                    case 1010: //金剛霸體
                        ret.getInfo().put(MapleStatInfo.time, 2100000000);
                        statups.put(SecondaryStat.DojangInvincible, 1);
                        statups.put(SecondaryStat.NotDamaged, 1);
                        break;
                    case 1011: //狂暴戰魂
                        statups.put(SecondaryStat.DojangBerserk, ret.getInfo().get(MapleStatInfo.x));
                        break;
                    case 1026: //飛翔
                    case 1142: //飛翔
                        ret.getInfo().put(MapleStatInfo.time, 2100000000);
                        statups.put(SecondaryStat.Flying, 1);
                        break;
                    case 8001: //好用的時空門
                        statups.put(SecondaryStat.SoulArrow, ret.getInfo().get(MapleStatInfo.x));
                        break;
                    case 8002: //好用的火眼晶晶
                        statups.put(SecondaryStat.SharpEyes, (ret.getInfo().get(MapleStatInfo.x) << 8) + ret.getInfo().get(MapleStatInfo.y) + ret.getInfo().get(MapleStatInfo.criticaldamageMax));
                        break;
                    case 8003: //好用的神聖之火
                        statups.put(SecondaryStat.MaxHP, ret.getInfo().get(MapleStatInfo.x));
                        statups.put(SecondaryStat.MaxMP, ret.getInfo().get(MapleStatInfo.x));
                        break;
                    case 8004: //強化戰鬥命令
                        statups.put(SecondaryStat.CombatOrders, ret.getInfo().get(MapleStatInfo.x));
                        break;
                    case 8005: //強化進階祝福
                        statups.clear();
                        statups.put(SecondaryStat.AdvancedBless, ret.getInfo().get(MapleStatInfo.x));
                        statups.put(SecondaryStat.IndieMHP, ret.getInfo().get(MapleStatInfo.indieMhp));
                        statups.put(SecondaryStat.IndieMMP, ret.getInfo().get(MapleStatInfo.indieMmp));
                        break;
                    case 8006: //強化極速領域
                        statups.put(SecondaryStat.PartyBooster, ret.getInfo().get(MapleStatInfo.x));
                        break;
                    case 169://九死一生
                        statups.put(SecondaryStat.PreReviveOnce, 1);
                        ret.getInfo().put(MapleStatInfo.time, 2100000000);
                        ret.setOverTime(true);
                        break;
                }
            }
        } else {
            switch (sourceid) {
                case 2022746: //天使的祝福
                case 2022747: //黑天使的祝福
                case 2022823: //白天使的祝福
                    statups.clear(); //no atk/matk
                    statups.put(SecondaryStat.RepeatEffect, 1);
                    int value = sourceid == 2022746 ? 5 : sourceid == 2022747 ? 10 : 12;
                    statups.put(SecondaryStat.IndiePAD, value);
                    statups.put(SecondaryStat.IndieMAD, value);
                    break;
                case 2003596: // 高級BOSS殺手的秘藥 
                    statups.put(SecondaryStat.IndieBDR, ret.getInfo().get(MapleStatInfo.indieBDR));
                    break;
                case 2023632: // 深海釣魚場料理 
                    statups.put(SecondaryStat.IndieBDR, ret.getInfo().get(MapleStatInfo.indieBDR));
                    break;
            }
        }
        if (ret.getSummonMovementType() != null || isSummon) {
            statups.put(SecondaryStat.IndieBuffIcon, 1);
        }
        if (SkillConstants.is召喚獸戒指(sourceid)) {
            ret.getInfo().put(MapleStatInfo.time, 2100000000);
        }
        if (ret.isMorph()) {
            statups.put(SecondaryStat.Morph, ret.getMorph());
            if (ret.is凱撒終極型態() || ret.is凱撒超終極型態()) {
                statups.put(SecondaryStat.Stance, ret.getInfo().get(MapleStatInfo.prop));
                statups.put(SecondaryStat.CriticalBuff, ret.getInfo().get(MapleStatInfo.cr));
                statups.put(SecondaryStat.IndieDamR, ret.getInfo().get(MapleStatInfo.indieDamR));
                statups.put(SecondaryStat.IndieBooster, ret.getInfo().get(MapleStatInfo.indieBooster));
            }
        }
        if (ret.is超越攻擊狀態()) {
            statups.clear();
            ret.getInfo().put(MapleStatInfo.time, 15000);
            statups.put(SecondaryStat.Exceed, 1);
        }
        ret.setStatups(statups);
        ret.setMonsterStatus(monsterStatus);
        return ret;
    }

    /**
     * 獲取騎寵的 MountId
     */
    public static int parseMountInfo(MapleCharacter player, int skillid) {
        if (skillid == 80001000 || SkillConstants.is騎乘技能(skillid)) {
            if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -123) != null && player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -124) != null) {
                return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -123).getItemId();
            }
            return parseMountInfo_Pure(player, skillid);
        } else {
            return GameConstants.getMountItem(skillid, player);
        }
    }

    static int parseMountInfo_Pure(MapleCharacter player, int skillid) {
        if (skillid == 80001000 || SkillConstants.is騎乘技能(skillid)) {
            if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null && player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -19) != null) {
                return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId();
            }
            return 0;
        } else {
            return GameConstants.getMountItem(skillid, player);
        }
    }

    public static int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
    }

    public static Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft, Point lt, Point rb, int range) {
        Rectangle rect;
        if (lt == null || rb == null) {
            rect = new Rectangle((facingLeft ? (-200 - range) : 0) + posFrom.x, (-100 - range) + posFrom.y, 200 + range, 100 + range);
        } else {
            Point mylt;
            Point myrb;
            if (facingLeft) {
                mylt = new Point(lt.x + posFrom.x - range, lt.y + posFrom.y);
                myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
            } else {
                myrb = new Point(lt.x * -1 + posFrom.x + range, rb.y + posFrom.y);
                mylt = new Point(rb.x * -1 + posFrom.x, lt.y + posFrom.y);
            }
            rect = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        }
        if (rect.width < 0) {
            int x = rect.x;
            rect.x += rect.width;
            rect.width = x - rect.x;
        }
        if (rect.height < 0) {
            int y = rect.y;
            rect.y += rect.height;
            rect.height = y - rect.y;
        }
        return rect;
    }

    /**
     * 添加一些固定的DeBuff
     */
    private static void addDebuffStatPairToListIfNotZero(Map<MonsterStatus, Integer> list, MonsterStatus buffstat, Integer val, Integer x) {
        if (val != 0 && (!list.containsKey(buffstat) || list.get(buffstat) == 0)) {
            list.put(buffstat, x);
        }
    }
}
