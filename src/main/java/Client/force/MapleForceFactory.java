package Client.force;

import Client.MapleCharacter;
import Client.MapleForceType;
import Client.SecondaryStat;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Client.status.MonsterStatus;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.夜使者;
import Config.constants.skills.冒險家_技能群組.箭神;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Config.constants.skills.皇家騎士團_技能群組.破風使者;
import Net.server.buffs.MapleStatEffect;
import Net.server.buffs.MapleStatEffectFactory;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMapItem;
import Net.server.maps.MapleMapObject;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;
import tools.Randomizer;

import java.awt.*;
import java.util.List;
import java.util.*;

public class MapleForceFactory {

    private static MapleForceFactory instance;
    private final Map<Integer, Map<Integer, ForceInfoEntry>> forceInfos = new HashMap<>();

    public static MapleForceFactory getInstance() {
        if (instance == null) {
            instance = new MapleForceFactory();
        }
        return instance;
    }

    public void initialize() {
        forceInfos.clear();
        MapleData forceData = MapleDataProviderFactory.getEffect().getData("CharacterEff.img").getChildByPath("forceAtom");
        for (MapleData force : forceData) {
            int id = Integer.valueOf(force.getName());
            int firstImpactMin = 45;
            int firstImpactMax = 50;
            int secondImpactMin = 30;
            int secondImpactMax = 35;
            List<Point> startPoints = new ArrayList<>();
            MapleData infod = force.getChildByPath("info");
            if (infod != null) {
                firstImpactMin = MapleDataTool.getInt(infod.getChildByPath("firstImpactMin"), 45);
                firstImpactMax = MapleDataTool.getInt(infod.getChildByPath("firstImpactMax"), 50);
                secondImpactMin = MapleDataTool.getInt(infod.getChildByPath("secondImpactMin"), 30);
                secondImpactMax = MapleDataTool.getInt(infod.getChildByPath("secondImpactMax"), 35);
            }
            MapleData atomd = force.getChildByPath("atom");
            if (atomd != null) {
                Map<Integer, ForceInfoEntry> ret = new HashMap<>();
                for (MapleData data : atomd) {
                    String name = data.getName();
                    if (name.length() <= 2) {
                        int id2 = Integer.valueOf(data.getName());
                        ForceInfoEntry info = new ForceInfoEntry();
//                        MapleData startInfo = data.getChildByPath("startInfo");
//                        if (startInfo != null) {
//                        MapleDataTool.getInt(startInfo.getChildByPath("startYmin"), 0);
//                        MapleDataTool.getInt(startInfo.getChildByPath("startYmax"), 0);
//                        MapleDataTool.getInt(startInfo.getChildByPath("arriveTime"), 0);
//                        MapleDataTool.getInt(startInfo.getChildByPath("updownYmin"), 0);
//                        MapleDataTool.getInt(startInfo.getChildByPath("updownYmax"), 0);
//                        MapleDataTool.getInt(startInfo.getChildByPath("updownTimeMin"), 0);
//                        MapleDataTool.getInt(startInfo.getChildByPath("updownTimeMax"), 0);
//                        }
//                    MapleDataTool.getInt(data.getChildByPath("isHoming"), 0);
//                    MapleDataTool.getInt(data.getChildByPath("holdRotate"), 0);
                        MapleData startPoint = data.getChildByPath("startPoint");
                        if (startPoint != null) {
                            for (MapleData d : startPoint) {
                                for (int i = 0; d.getChildByPath(String.valueOf(i)) != null; i++) {
                                    Point point = MapleDataTool.getPoint(d.getChildByPath(String.valueOf(i)));
                                    if (point != null) {
                                        startPoints.add(point);
                                    }
                                }
                            }
                        }
                        info.startPoints = startPoints;
                        info.firstImpactMin = firstImpactMin;
                        info.firstImpactMax = firstImpactMax;
                        info.secondImpactMin = secondImpactMin;
                        info.secondImpactMax = secondImpactMax;
                        ret.put(id2, info);
                    }
                }
                forceInfos.put(id, ret);
            }
        }
    }

    public final MapleForceAtom getMapleForce(final MapleCharacter chr, final MapleStatEffect effect, final int fromMobOid, final List<Integer> toMobOid) {
        return this.getMapleForce(chr, effect, fromMobOid, 0, toMobOid, chr.getPosition());
    }

    public final MapleForceAtom getMapleForce(final MapleCharacter chr, final MapleStatEffect effect, final int fromMobOid, final List<Integer> toMobOid, final Point pos) {
        return this.getMapleForce(chr, effect, fromMobOid, 0, toMobOid, pos);
    }

    public final MapleForceAtom getMapleForce(final MapleCharacter chr, final MapleStatEffect effect, final int fromMobOid) {
        return this.getMapleForce(chr, effect, fromMobOid, 0, Collections.emptyList(), chr.getPosition());
    }

    public final MapleForceAtom getMapleForce(final MapleCharacter chr, final MapleStatEffect effect, final int attackCount, final int fromMobOid) {
        return this.getMapleForce(chr, effect, attackCount, fromMobOid, 0, Collections.emptyList(), chr.getPosition());
    }

    public final MapleForceAtom getMapleForce(final MapleCharacter chr, final MapleStatEffect effect, final int fromMobOid, final int firstMobId, final List<Integer> toMobOid, final Point point) {
        return this.getMapleForce(chr, effect, 1, fromMobOid, firstMobId, toMobOid, point);
    }

    private MapleForceAtom getMapleForce(final MapleCharacter chr, final MapleStatEffect effect, final int attackCount, final int fromMobOid, int firstMobId, final List<Integer> toMobOid, final Point pos) {
        final MapleForceAtom force = new MapleForceAtom();
        force.setFromMob(fromMobOid > 0);
        force.setOwnerId(chr.getId());
        force.setBulletItemID(0);
        force.setArriveDir(0);
        force.setArriveRange(500);
        force.setForcedTarget(pos);
        force.setToMobOid(toMobOid);
        switch (effect.getSourceId()) {
            case 火毒.藍焰斬: {
                force.setFromMobOid(0);
                force.setForceType(MapleForceType.審判之焰.ordinal());
                force.setFirstMobID(0);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 2, 20, 50, 500, pos));
                break;
            }
            case 箭神.魔幻箭筒:
            case 箭神.無限箭筒: {
                force.setFromMobOid(0);
                force.setForceType(MapleForceType.三彩箭矢.ordinal());
                force.setFirstMobID(firstMobId);
                force.setSkillId((effect.getSourceId() == 箭神.無限箭筒) ? 箭神.魔幻箭筒_4轉 : 箭神.魔幻箭筒_2轉);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 10, 100, 53, pos));
                break;
            }
            case 夜使者.刺客刻印_1:
            case 夜使者.刺客刻印: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(MapleForceType.刺客刻印.ordinal());
                force.setFirstMobID(0);
                force.setSkillId(夜使者.刺客刻印_飛鏢);
                force.setRect(MapleStatEffectFactory.calculateBoundingBox(pos, true, new Point(-120, -100), new Point(100, 100), 0));
                Item dartsSlot = chr.getInventory(MapleInventoryType.CASH).getDartsSlot(chr.getLevel());
                if (dartsSlot == null) {
                    dartsSlot = chr.getInventory(MapleInventoryType.USE).getDartsSlot(chr.getLevel());
                }
                force.setBulletItemID(dartsSlot == null ? 0 : dartsSlot.getItemId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 40, 100, 200, pos));
                break;
            }
            case 夜使者.夜使者刻印: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(MapleForceType.刺客刻印.ordinal());
                force.setFirstMobID(0);
                force.setSkillId(夜使者.夜使者的標記);
                Item dartsSlot = chr.getInventory(MapleInventoryType.CASH).getDartsSlot(chr.getLevel());
                if (dartsSlot == null) {
                    dartsSlot = chr.getInventory(MapleInventoryType.USE).getDartsSlot(chr.getLevel());
                }
                force.setBulletItemID(dartsSlot == null ? 0 : dartsSlot.getItemId());
                force.setRect(MapleStatEffectFactory.calculateBoundingBox(pos, true, new Point(-120, -100), new Point(100, 100), 0));
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 2, 40, 100, 200, pos));
                break;
            }
            case 烈焰巫師.元素之炎: {
                force.setSkillId(烈焰巫師.Return_元素之炎I_1);
                force.setForceType(MapleForceType.軌道烈焰.ordinal());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 40, 100, 30, pos));
                break;
            }
            case 烈焰巫師.元素之炎II: {
                force.setSkillId(烈焰巫師.Return_元素之炎II_1);
                force.setForceType(MapleForceType.軌道烈焰.ordinal());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 2, 40, 100, 30, pos));
                break;
            }
            case 烈焰巫師.元素之炎III: {
                force.setSkillId(烈焰巫師.Return_元素之炎III_1);
                force.setForceType(MapleForceType.軌道烈焰.ordinal());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 3, 40, 100, 30, pos));
                break;
            }
            case 烈焰巫師.元素之炎IV: {
                force.setSkillId(烈焰巫師.Return_元素之炎IV);
                force.setForceType(MapleForceType.軌道烈焰.ordinal());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 4, 25, 100, 30, pos));
                break;
            }
            case 破風使者.Switch_type_風妖精之箭I:
            case 破風使者.Switch_type_風妖精之箭II:
            case 破風使者.Switch_type_風妖精之箭Ⅲ: {
                force.setForceType(7);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 2, 42, chr.isFacingLeft() ? 90 : 280, 48, pos));
                break;
            }
            case 13101027:
            case 破風使者.SUMMON_攻擊型態_風妖精之箭II_攻擊:
            case 破風使者.SUMMON_攻擊型態_風妖精之箭Ⅲ_攻擊: {
                force.setForceType(7);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 42, chr.isFacingLeft() ? 90 : 280, 48, pos));
                break;
            }

            case 破風使者.暴風加護: {
                force.setForceType(8);
                force.setFirstMobID(firstMobId);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 15, chr.isFacingLeft() ? 90 : 280, 60, pos));
                break;
            }
            case 暗夜行者.SUMMON_暗影蝙蝠_召喚獸:
            case 暗夜行者.SUMMON_暗影蝙蝠_攻擊: {
                force.setForceType(force.isFromMob() ? MapleForceType.SUMMON_暗影蝙蝠_反彈.ordinal() : MapleForceType.Switch_type_Magic_暗影蝙蝠.ordinal());
                force.setSkillId(暗夜行者.SUMMON_暗影蝙蝠_攻擊);
                force.setFromMobOid(fromMobOid);
                force.setFirstMobID(firstMobId);
                force.setRect(MapleStatEffectFactory.calculateBoundingBox(pos, true, new Point(-120, -100), new Point(100, 100), 0));
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), force.isFromMob() ? 1 : 2, 32, 280, force.isFromMob() ? 30 : 300, pos));
                break;
            }
            case 幻影俠盜.命運鬼牌:
            case 幻影俠盜.鬼牌_1: {
                force.setForceType(MapleForceType.幻影卡牌.ordinal());
                force.setFirstMobID(firstMobId);
                force.setSkillId(幻影俠盜.鬼牌_1);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), chr.getSkillEffect(幻影俠盜.死神卡牌) != null ? 2 : 1, 35, chr.isFacingLeft() ? 90 : 280, 60, pos));
                break;
            }
            case 幻影俠盜.炫目卡牌:
            case 幻影俠盜.死神卡牌: {
                force.setForceType(MapleForceType.幻影卡牌.ordinal());
                force.setFirstMobID(firstMobId);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), (effect.getSourceId() == 幻影俠盜.死神卡牌) ? 2 : 1, 25, chr.isFacingLeft() ? 90 : 280, 60, pos));
                break;
            }
            case 幻影俠盜.卡牌審判:
            case 幻影俠盜.審判: {
                force.setForceType(1);
                force.setFirstMobID(firstMobId);
                force.setSkillId((effect.getSourceId() == 幻影俠盜.審判) ? 幻影俠盜.死神卡牌 : 幻影俠盜.炫目卡牌);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), (effect.getSourceId() == 幻影俠盜.審判) ? 2 : 1, 25, chr.isFacingLeft() ? 90 : 280, 60, pos));
                break;
            }
            case 隱月.小狐仙精通_1:
            case 隱月.火狐精通_1: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(force.isFromMob() ? 4 : 13);
                force.setFirstMobID(firstMobId);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), (effect.getSourceId() == 隱月.小狐仙精通_1) ? (force.isFromMob() ? 4 : 1) : (force.isFromMob() ? 5 : 2), 25, chr.isFacingLeft() ? 90 : 280, 60, pos));
                break;
            }
            case 惡魔殺手.惡魔狂斬:
            case 惡魔殺手.惡魔狂斬1:
            case 惡魔殺手.惡魔狂斬2:
            case 惡魔殺手.惡魔狂斬3:
            case 惡魔殺手.地獄犬:
            case 惡魔殺手.惡魔覺醒_1:
            case 惡魔殺手.惡魔覺醒_2:
            case 惡魔殺手.惡魔覺醒_3:
            case 惡魔殺手.惡魔覺醒_4: {
                firstMobId = Randomizer.nextInt((chr.getSkillLevel(惡魔殺手.強化惡魔之力) > 0) ? 10 : 6);
                force.setFromMobOid(fromMobOid);
                force.setForceType(0);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), firstMobId, 30, 65, 0, pos));
                break;
            }
            case 惡魔復仇者.盾牌追擊:
            case 惡魔復仇者.盾牌追擊_攻擊: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(force.isFromMob() ? 4 : 3);
                force.setFirstMobID(firstMobId);
                force.setSkillId(惡魔復仇者.盾牌追擊_攻擊);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 3, force.isFromMob() ? 80 : 20, chr.isFacingLeft() ? Randomizer.rand(30, 90) : Randomizer.rand(200, 280), force.isFromMob() ? 60 : 500, pos));
                break;
            }
            case 機甲戰神.追蹤飛彈:
            case 機甲戰神.進階追蹤飛彈: {
                force.setForceType(20);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), (effect.getSourceId() == 機甲戰神.追蹤飛彈) ? 0 : 2, 37, chr.isFacingLeft() ? 90 : 280, 512, pos));
                break;
            }
            case 傑諾.神盾系統: {
                force.setForceType(MapleForceType.神盾系統.ordinal());
                force.setSkillId(傑諾.神盾系統_攻擊);
                force.setFromMobOid(fromMobOid);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 32, 96, 256, pos));
                break;
            }
            case 傑諾.追縱火箭: {
                force.setForceType(MapleForceType.傑諾火箭.ordinal());
                force.setSkillId(傑諾.追縱火箭);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 20, 34, 0, pos));
                break;
            }
            case 陰陽師.夜雀: {
                force.setFirstMobID(firstMobId);
                force.setForceType(59);
                force.setSkillId(陰陽師.夜雀);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 66, 77, 661, pos));
                break;
            }
            case 凱撒.意志之劍:
            case 凱撒.進階意志之劍:
            case 凱撒.意志之劍_重磅出擊: {
                force.setForceType(2);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 2, 16, chr.isFacingLeft() ? 90 : 280, 16, pos));
                break;
            }
            case 凱撒.意志之劍_變身:
            case 凱撒.進階意志之劍_變身:
            case 凱撒.意志之劍_重磅出擊_1: {
                force.setForceType(2);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 3, 16, chr.isFacingLeft() ? 90 : 280, 16, pos));
                break;
            }
            case 天使破壞者.靈魂探求者_攻擊:
            case 天使破壞者.靈魂探求者:
            case 天使破壞者.索魂精通: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(force.isFromMob() ? 4 : 3);
                force.setFirstMobID(firstMobId);
                force.setSkillId(天使破壞者.靈魂探求者_攻擊);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, force.isFromMob() ? 32 : 16, chr.isFacingLeft() ? 90 : 280, force.isFromMob() ? 32 : 300, pos));
                break;
            }
            case 凱內西斯.心靈傳動: {
                force.setForceType(22);
                force.setFirstMobID(firstMobId);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 18, chr.isFacingLeft() ? 90 : 280, 300, pos));
                break;
            }
            case 火毒.持續制裁者: {
                force.setForceType(28);
                force.setFirstMobID(firstMobId);
                force.setSkillId(effect.getSourceId());
                force.setRect(effect.getBounds());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 36, chr.isFacingLeft() ? 30 : 50, 528, pos));
                List<Integer> t = new LinkedList<>();
                for (int i = 0; i < force.getInfo().size(); i++) {
                    t.add(0);
                }
                force.setToMobOid(t);
                break;
            }
            case 箭神.殘影之矢:
            case 箭神.殘影之矢_1: {
                force.setFromMobOid(0);
                force.setForceType(31);
                force.setFirstMobID(firstMobId);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 20, 70, 53, pos));
                break;
            }
            case 幻影俠盜.黑傑克:
            case 幻影俠盜.黑傑克_1: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(33);
                force.setFirstMobID(firstMobId);
                force.setSkillId(幻影俠盜.黑傑克_1);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 32, 70, 70, (fromMobOid == 0) ? 760 : 0, pos));
                break;
            }
            case 破風使者.風轉奇想: {
                force.setForceType(34);
                force.setFirstMobID(firstMobId);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 36, chr.isFacingLeft() ? 30 : 50, 528, pos));
                break;
            }
            case 機甲戰神.微型導彈箱: {
                force.setForceType(30);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 48, 37, 1440, pos));
                break;
            }

            case 通用V核心.弓箭手通用.追蹤箭頭: {
                force.setForceType(27);
                force.setSkillId(effect.getSourceId());
                force.setRect(MapleStatEffectFactory.calculateBoundingBox(pos, true, new Point(-120, -100), new Point(100, 100), 0));
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 48, 90, 840, pos));
                break;
            }
            case 伊利恩.技藝_暗器:
            case 伊利恩.技藝_暗器Ⅱ: {
                force.setForceType(36);
                force.setSkillId(effect.getSourceId());
                force.setRect(effect.getBounds());
                force.setRect2(effect.getBounds2());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 2, 2, 50, 390, pos));
                break;
            }
            case 伊利恩.榮耀之翼_強化暗器_2: {
                force.setForceType(41);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 48, 8, 310, pos));
                break;
            }
            case 伊利恩.技藝_暗器Ⅱ_1: {
                force.setForceType(39);
                force.setSkillId(effect.getSourceId());
                force.setRect(effect.getBounds());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 64, 24, 310, pos));
                break;
            }
            case 伊利恩.榮耀之翼_強化暗器: {
                force.setForceType(37);
                force.setSkillId(effect.getSourceId());
                force.setRect(effect.getBounds());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 2, 50, 390, pos));
                break;
            }
            case 80011585:
            case 80011586:
            case 80011587:
            case 80011588:
            case 80011589:
            case 80011590:
            case 80011635: {
                force.setForceType(17);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 2, 16, chr.isFacingLeft() ? 90 : 280, 16, pos));
                break;
            }
            case 烈焰巫師.烈炎爆發_2:
            case 烈焰巫師.烈炎爆發_3: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(3);
                force.setFirstMobID(firstMobId);
                force.setSkillId(烈焰巫師.烈炎爆發_2);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 6, 10, 70, 90, pos));
                break;
            }
            case 亞克.深淵技能: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(46);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 10, 70, 900 + Randomizer.nextInt(10), pos));
                break;
            }
            case 亞克.迸發技能: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(45);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 10, 70, 900 + Randomizer.nextInt(10), pos));
                break;
            }
            case 亞克.緋紅技能: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(44);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 10, 70, 900 + Randomizer.nextInt(10), pos));
                break;
            }
            case 亞克.原始技能: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(43);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 10, 70, 900 + Randomizer.nextInt(10), pos));
                break;
            }
            case 亞克.逼近的死亡_1: {
                force.setForceType(47);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 3, Randomizer.rand(160, 172), 300, pos));
                break;
            }
            case 亞克.歸來的憎恨: {
                force.setForceType(48);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 41, 0, 0, pos));
                break;
            }
            case 破風使者.西爾芙之壁_1: {
                force.setForceType(51);
                force.setSkillId(effect.getSourceId());
                force.setFromMobOid(fromMobOid);
                force.setSkillId(破風使者.西爾芙之壁_1);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 10, 16, Randomizer.rand(48, 70), 0, pos));
                break;
            }
            case 夜使者.達克魯的秘傳: {
                force.setForceType(49);
                force.setSkillId(夜使者.達克魯的秘傳);
                force.setForcedTarget(((chr.getSummonBySkillID(夜使者.達克魯的秘傳) != null) ? new Point(chr.getSummonBySkillID(夜使者.達克魯的秘傳).getPosition().x, chr.getSummonBySkillID(夜使者.達克魯的秘傳).getPosition().y - 250) : new Point()));
                force.setArriveDir(((chr.getSummonBySkillID(夜使者.達克魯的秘傳) != null) ? chr.getSummonBySkillID(夜使者.達克魯的秘傳).getObjectId() : 0));
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 2, Randomizer.rand(40, 45), Randomizer.rand(1, 64), 200, pos));
                break;
            }
            case 箭神.焰箭齊發:
            case 箭神.焰箭齊發_1: {
                force.setForceType(50);
                force.setSkillId(箭神.焰箭齊發_1);
                force.setForcedTarget(pos);
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, Randomizer.rand(32, 45), Randomizer.rand(1, 64), 56, pos));
                break;
            }
            case 龍魔導士.星宮射線: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(29);
                force.setSkillId(龍魔導士.星宮射線);
                force.setRect(MapleStatEffectFactory.calculateBoundingBox(pos, true, new Point(-120, -100), new Point(100, 100), 0));
                force.setPos2(((chr.getSummonBySkillID(龍魔導士.星宮射線) != null) ? new Point(chr.getSummonBySkillID(龍魔導士.星宮射線).getPosition().x, chr.getSummonBySkillID(龍魔導士.星宮射線).getPosition().y - 250) : new Point()));
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, Randomizer.rand(32, 48), Randomizer.rand(32, 48), 0, pos));
                break;
            }
            case 開拓者.基本釋放:
            case 開拓者.基本釋放強化: {
                force.setForceType(56);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo_暗紅釋魂(chr, toMobOid.size(), 1));
                break;
            }
            /*case 開拓者.基本釋放4轉: {
                force.setForceType(56);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo_暗紅釋魂(chr, toMobOid.size(), 2));
                break;
            }*/
            case 箭神.殘影幻象_1: {
                force.setForceType(56);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 3, Randomizer.rand(20, 40), chr.isFacingLeft() ? 90 : 280, 60, pos));
                break;
            }
            case 開拓者.附加釋放: {
                force.setForceType(57);
                force.setSkillId(effect.getSourceId());
                force.setRect(effect.calculateBoundingBox3(new Point()));
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, Randomizer.rand(32, 48), Randomizer.rand(32, 48), 60, pos));
                break;
            }
            case 開拓者.附加爆破: {
                force.setForceType(57);
                force.setSkillId(effect.getSourceId());
                force.setRect(effect.calculateBoundingBox3(new Point()));
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 2, Randomizer.rand(32, 48), Randomizer.rand(32, 48), 60, pos));
                break;
            }
            case 開拓者.分裂魔矢:
            case 開拓者.分裂魔矢_1: {
                force.setForceType(58);
                force.setSkillId(effect.getSourceId());
                force.setRect(effect.calculateBoundingBox3(new Point()));
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, Randomizer.rand(32, 48), Randomizer.rand(32, 48), 120, pos));
                break;
            }
            case 虎影.魔封葫蘆符: {
                force.setForceType(63);
                force.setFirstMobID(chr.getId());
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo_魔封葫蘆符(chr, attackCount, 1));
                break;
            }
            case 虎影.幻影分身符_1: {
                force.setForceType(60);
                force.setFromMob(false);
                force.setToMobOid(toMobOid);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 45, 0, 0, pos));
                break;
            }
            case 虎影.卷術_蝴蝶之夢_1: {
                force.setForceType(61);
                force.setFromMob(false);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), Randomizer.rand(0, 3), Randomizer.rand(32, 48), Randomizer.rand(70, 130), 0, pos));
                break;
            }
            case 虎影.仙技_極大分身亂舞_1: {
                force.setForceType(60);
                force.setFromMob(false);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 45, 0, 0, pos));
                break;
            }
            case 80002888: {
                force.setFromMobOid(fromMobOid);
                force.setForceType(29);
                force.setSkillId(80002888);
                force.setRect(MapleStatEffectFactory.calculateBoundingBox(pos, true, new Point(-120, -100), new Point(100, 100), 0));
                force.setPos2(((chr.getSummonBySkillID(80002888) != null) ? new Point(chr.getSummonBySkillID(80002888).getPosition().x, chr.getSummonBySkillID(80002888).getPosition().y - 250) : new Point()));
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 8, Randomizer.rand(32, 48), Randomizer.rand(32, 48), 0, pos));
                break;
            }
            case 墨玄.神功_移形換位_2:
            case 墨玄.絕技_無我之境_1: {
                force.setForceType(60);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 1, 40, 50, 0, pos));
                break;
            }
            case 聖騎士.雷神戰槌: {
                force.setForceType(67);
                force.setSkillId(effect.getSourceId());
                force.setInfo(this.getForceInfo(chr, effect, attackCount, force.getForceType(), 0, 1, 109, 427, pos));
                break;
            }
        }
        return force;
    }

    private List<MapleForceInfo> getForceInfo(final MapleCharacter chr, final MapleStatEffect effect, final int attackCount, final int forceType, final int inc, final int firstImpact, final int angle, final int startDelay, Point pos) {
        List<MapleForceInfo> infos = new ArrayList<>();
        Map<Integer, ForceInfoEntry> map = forceInfos.get(forceType);
        ForceInfoEntry entry;
        if (map != null && (entry = map.get(Math.min(map.size(), inc + 1))) != null) {
            int mobCount = effect.getBulletCount(chr);
            switch (effect.getSourceId()) {
                case 幻影俠盜.卡牌審判:
                    mobCount = 5;
                    break;
                case 幻影俠盜.審判:
                    mobCount = 10;
                    break;
                case 機甲戰神.進階追蹤飛彈:
                    MapleStatEffect eff;
                    if ((eff = chr.getEffectForBuffStat(SecondaryStat.BombTime)) != null) {
                        mobCount += eff.getX();
                    }
                    break;
                case 火毒.持續制裁者:
                    mobCount = effect.getX();
                    int maxCount = effect.getZ();
                    for (MapleMapObject obj : chr.getMap().getMonstersInRect(effect.calculateBoundingBox(chr.getPosition()))) {
                        if (((MapleMonster) obj).getEffectHolder(chr.getId(), MonsterStatus.Burned) != null && ++mobCount >= maxCount) {
                            break;
                        }
                    }
                    break;
                case 開拓者.基本釋放強化:
                case 開拓者.基本釋放4轉:
                    for (MapleMapObject obj : chr.getMap().getMonstersInRect(effect.calculateBoundingBox(chr.getPosition()))) {
                        if (((MapleMonster) obj).isBoss()) {
                            mobCount += 1;
                            break;
                        }
                    }
                    break;
                case 開拓者.附加釋放:
                case 開拓者.附加爆破:
                    if (chr.getBuffStatValueHolder(開拓者.遺跡進化) != null) {
                        mobCount += 1;
                    }
                    break;
            }
            for (int i = 0; i < mobCount; ++i) {
                final MapleForceInfo info = new MapleForceInfo();
                info.setKey(chr.getSpecialStat().gainForceCounter());
                info.setInc(inc);
                info.setFirstImpact(Randomizer.rand(firstImpact - 6, firstImpact));
                info.setSecondImpact(Randomizer.rand(entry.secondImpactMin, entry.secondImpactMax));
                info.setAngle(Randomizer.rand(angle - 20, angle));
                info.setStartDelay(startDelay);
                Point point = new Point();
                if (effect.getSourceId() == 伊利恩.技藝_暗器 || effect.getSourceId() == 伊利恩.技藝_暗器Ⅱ || effect.getSourceId() == 伊利恩.榮耀之翼_強化暗器) {
                    info.setFirstImpact(50);
                    info.setSecondImpact(50);
                    info.setAngle(0);
                    point = new Point(-48, 7);
                }
                if (effect.getSourceId() == 伊利恩.技藝_暗器Ⅱ_1 || effect.getSourceId() == 開拓者.分裂魔矢 || effect.getSourceId() == 開拓者.分裂魔矢_1 || effect.getSourceId() == 開拓者.附加釋放 || effect.getSourceId() == 開拓者.附加爆破) {
                    point = pos;
                }
                if (effect.getSourceId() == 火毒.持續制裁者 || effect.getSourceId() == 破風使者.風轉奇想) {
                    point = new Point(Randomizer.rand(effect.getLt2().x, effect.getRb2().x) + chr.getPosition().x, Randomizer.rand(effect.getLt2().y, effect.getRb2().y) + chr.getPosition().y);
                }
                if (effect.getSourceId() == 機甲戰神.微型導彈箱) {
                    point = new Point(Randomizer.rand(effect.getLt().x, effect.getRb().x) + chr.getPosition().x, Randomizer.rand(effect.getLt().y, effect.getRb().y) + chr.getPosition().y);
                }
                if (effect.getSourceId() == 通用V核心.弓箭手通用.追蹤箭頭 && effect.getSourceId() == 破風使者.追蹤箭頭) {
                    info.setFirstImpact(43);
                    info.setSecondImpact(3);
                    info.setAngle(90);
                }

                if (effect.getSourceId() == 虎影.幻影分身符_1) {
                    info.setAngle(Randomizer.nextInt(12) * 30);
                    info.setSecondImpact(3);
                    info.setMaxHitCount(0);
                }
                if (effect.getSourceId() == 虎影.仙技_極大分身亂舞_1) {
                    info.setAngle(Randomizer.nextInt(12) * 30);
                    info.setSecondImpact(3);
                    info.setMaxHitCount(0);
                    info.setInc(i + 1);
                    info.setSecondImpact(3);
                    info.setMaxHitCount(1);
                    info.setStartDelay(45);
                }
                if (effect.getSourceId() == 陰陽師.夜雀) {
                    info.setSecondImpact(10);
                }
                if (effect.getSourceId() == 箭神.殘影幻象_1) {
                    point = new Point(-28, -150);
                }
                info.setPosition(point);
                info.setTime(System.currentTimeMillis());
                info.setMaxHitCount(effect.getMobCount());
                info.setEffectIdx(0);
                infos.add(info);
            }
        }
        return infos;
    }

    public List<MapleForceInfo> getForceInfo_惡魔DF(MapleCharacter chr, int attackCount, int inc) {
        List<MapleForceInfo> infos = new ArrayList<>();
        final Map<Integer, ForceInfoEntry> map = forceInfos.get(MapleForceType.惡魔DF.ordinal());
        ForceInfoEntry entry;
        if (map != null && map.size() > 0) {
            entry = map.values().iterator().next();
        } else {
            entry = null;
        }
        for (int i = 0; i < attackCount; i++) {
            MapleForceInfo info = new MapleForceInfo();
            info.setKey(chr.getSpecialStat().gainForceCounter());
            info.setInc(inc);
            info.setFirstImpact(entry == null ? 0 : Randomizer.rand(entry.firstImpactMin, entry.firstImpactMax));
            info.setSecondImpact(entry == null ? 0 : Randomizer.rand(entry.secondImpactMin, entry.secondImpactMax));
            info.setAngle(46);
            info.setStartDelay(0);
            info.setPosition(new Point(0, 0));
            info.setMaxHitCount(0);
            info.setEffectIdx(0);
            infos.add(info);
        }
        return infos;
    }

    public List<MapleForceInfo> getForceInfo_楓幣炸彈(final MapleCharacter chr, final List<MapleMapItem> list, final int n) {
        final ArrayList<MapleForceInfo> infos = new ArrayList<>();
        final Map<Integer, ForceInfoEntry> map = forceInfos.get(MapleForceType.楓幣炸彈.ordinal());
        final ForceInfoEntry entry;
        if (map != null && (entry = map.get(Math.min(map.size(), 2))) != null) {
            for (MapleMapItem mdrop : list) {
                MapleForceInfo info = new MapleForceInfo();
                info.setKey(chr.getSpecialStat().gainForceCounter());
                info.setInc(1);
                info.setFirstImpact(Randomizer.rand(entry.firstImpactMin, entry.firstImpactMax));
                info.setSecondImpact(Randomizer.rand(entry.secondImpactMin, entry.secondImpactMax));
                info.setAngle(Randomizer.rand(20, 45));
                info.setStartDelay(n);
                info.setPosition(new Point(mdrop.getPosition()));
                info.setTime(System.currentTimeMillis());
                info.setMaxHitCount(0);
                info.setEffectIdx(0);
                infos.add(info);
            }
        }
        return infos;
    }

    private List<MapleForceInfo> getForceInfo_暗紅釋魂(MapleCharacter chr, int attackCount, int inc) {
        List<MapleForceInfo> infos = new ArrayList<>();
        for (int i = 0; i < attackCount; i++) {
            MapleForceInfo info = new MapleForceInfo();
            info.setKey(chr.getSpecialStat().gainForceCounter());
            info.setInc(inc);
            info.setFirstImpact(Randomizer.rand(20, 40));
            info.setSecondImpact(Randomizer.rand(5, 10));
            info.setAngle(Randomizer.rand(5, 15));
            info.setStartDelay(60);
            info.setPosition(new Point(-70, -10));
            info.setMaxHitCount(0);
            info.setEffectIdx(0);
            infos.add(info);
        }
        return infos;
    }

    private List<MapleForceInfo> getForceInfo_魔封葫蘆符(MapleCharacter chr, int attackCount, int inc) {
        List<MapleForceInfo> infos = new ArrayList<>();
        for (int i = 0; i < attackCount; i++) {
            MapleForceInfo info = new MapleForceInfo();
            info.setKey(chr.getSpecialStat().gainForceCounter());
            info.setInc(1);
            info.setFirstImpact(5);
            info.setSecondImpact(30);
            info.setAngle(0);
            info.setStartDelay(0);
            info.setPosition(new Point());
            info.setMaxHitCount(0);
            info.setEffectIdx(0);
            info.setTime(System.currentTimeMillis());
            infos.add(info);
        }
        return infos;
    }

    public MapleForceAtom getMapleForce(final MapleCharacter chr, final MapleStatEffect effect, final List<Integer> oids, final Collection<Point> list) {
        MapleForceAtom force = new MapleForceAtom();
        force.setOwnerId(chr.getId());
        force.setForceType((effect.getSourceId() == 亞克.歸來的憎恨) ? 48 : 23);
        force.setFirstMobID(0);
        force.setToMobOid(oids);
        force.setSkillId(effect.getSourceId());
        List<MapleForceInfo> infos = new ArrayList<>();
        Map<Integer, ForceInfoEntry> map = forceInfos.get((effect.getSourceId() == 亞克.歸來的憎恨) ? 48 : 23);
        ForceInfoEntry entry;
        if (map != null && (entry = map.get(Math.min(map.size(), ((effect.getSourceId() == 龍魔導士.強化的魔法殘骸) ? 2 : ((effect.getSourceId() == 亞克.歸來的憎恨) ? 0 : 1)) + 1))) != null) {
            for (Point point : list) {
                MapleForceInfo info = new MapleForceInfo();
                info.setKey(chr.getSpecialStat().gainForceCounter());
                info.setInc((effect.getSourceId() == 龍魔導士.強化的魔法殘骸) ? 2 : ((effect.getSourceId() == 亞克.歸來的憎恨) ? 0 : 1));
                info.setFirstImpact(Randomizer.rand(entry.firstImpactMin, entry.firstImpactMax));
                info.setSecondImpact((effect.getSourceId() == 亞克.歸來的憎恨) ? 100 : Randomizer.rand(entry.secondImpactMin, entry.secondImpactMax));
                info.setAngle(Randomizer.rand(64, 79));
                info.setStartDelay(512);
                info.setPosition(point);
                info.setTime(System.currentTimeMillis());
                info.setMaxHitCount(1);
                info.setEffectIdx(0);
                infos.add(info);
            }
        }
        force.setInfo(infos);
        return force;
    }
}
