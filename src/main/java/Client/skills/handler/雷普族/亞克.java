package Client.skills.handler.雷普族;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.force.MapleForceAtom;
import Client.force.MapleForceFactory;
import Client.skills.ExtraSkill;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Opcode.Headler.OutHeader;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

import static Config.constants.skills.亞克.*;

public class 亞克 extends AbstractSkillHandler {

    public 亞克() {
        jobs = new MapleJob[]{
                MapleJob.亞克,
                MapleJob.亞克1轉,
                MapleJob.亞克2轉,
                MapleJob.亞克3轉,
                MapleJob.亞克4轉
        };

        for (Field field : Config.constants.skills.亞克.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int baseSkills(MapleCharacter chr, SkillClassApplier applier) {
        Skill skil;
        final int[] ss = {魔法迴路, 侵蝕控制, 獨創技能};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == 獨創技能) {
                continue;
            }
            skil = SkillFactory.getSkill(i);
            if (chr.getJob() >= i / 10000 && skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(i, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case HEXA_恆古凝視的深淵_延伸:
                return HEXA_恆古凝視的深淵;
            case HEXA_原始猛擊_VI:
                return 原始猛擊;
            case 精神力枯竭:
                return 幽靈侵蝕;
            case 原始技能:
            case 原始加持:
                return 咒術子彈;
            case 神秘跳躍_1:
            case 本能跳躍:
            case 浮游:
                return 神秘跳躍;
            case 永無止盡的痛苦_1:
                return 永無止盡的痛苦;
            case 迷惑之拘束_1:
            case 迷惑之拘束_2:
                return 迷惑之拘束;
            case 深淵猛擊_1:
            case 深淵技能:
            case 深淵技能_1:
            case 深淵加持效果:
            case 無法控制住的混沌:
            case 無法控制住的混沌_1:
                return 深淵猛擊;
            case 不會結束的凶夢:
                return 不會結束的噩夢;
            case 深淵之恐懼:
                return 暗中蠕動的恐懼;
            case 迸發技能:
            case 迸發技能_1:
            case 迸發加持:
            case 迸發猛擊_2:
            case 無法填滿的飢餓:
            case 無法填滿的飢餓_1:
            case 無法填滿的飢餓_2:
                return 迸發猛擊;
            case 回想起的凶夢:
                return 回想起的噩夢;
            case 逼近的死亡_1:
                return 逼近的死亡;
            case 無法停止的衝動_1:
            case 無法停止的本能:
            case 無法停止的本能_1:
                return 無法停止的衝動;
            case 無法遺忘的凶夢:
                return 無法遺忘的噩夢;
            case 緋紅技能:
            case 緋紅加持:
            case 緋紅猛擊_1:
            case 緋紅猛擊_2:
            case 緋紅猛擊_3:
            case 緋紅猛擊_4:
            case 無法消除的傷痕:
            case 無法消除的傷痕_1:
            case 無法消除的傷痕_2:
                return 緋紅猛擊;
            case 根源的記憶_1:
                return 根源的記憶;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 無我:
            case 無我_傳授:
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.q));
                statups.put(SecondaryStat.LPBattleMode, 1);
                return 1;
            case 獨創技能:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
                statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
                return 1;
            case 幽靈侵蝕:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.IndieStance, effect.getInfo().get(MapleStatInfo.indieStance));
                statups.put(SecondaryStat.SpecterMode, 1);
                return 1;
            case 逼近的死亡:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.ComingDeath, 1);
                return 1;
            case 指虎極速:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 永無止盡的痛苦:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.KeyDownMoving, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 充能技能增幅:
                statups.put(SecondaryStat.LPSpellAmplification, 1);
                return 1;
            case 戰鬥狂亂:
                statups.put(SecondaryStat.CombatFrenzy, 1);
                return 1;
            case 雷普的勇士:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 暗中蠕動的恐懼:
                statups.put(SecondaryStat.NotDamaged, 1);
                statups.put(SecondaryStat.KeyDownMoving, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 迷惑之拘束:
                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 深淵技能:
                effect.getInfo().put(MapleStatInfo.bulletCount, 8);
                return 1;
            case 迸發技能:
                effect.getInfo().put(MapleStatInfo.bulletCount, 6);
                return 1;
            case 緋紅技能:
                effect.getInfo().put(MapleStatInfo.bulletCount, 8);
                return 1;
            case 原始技能:
                effect.getInfo().put(MapleStatInfo.bulletCount, 2);
                return 1;
            case 無法遺忘的凶夢:
            case 無法消除的傷痕:
            case 回想起的凶夢:
            case 無法填滿的飢餓_2:
            case 不會結束的凶夢:
            case 無法控制住的混沌:
                effect.setHpR(effect.getInfo().get(MapleStatInfo.w) / 100.0);
                return 1;
            case 原始加持:
                statups.put(SecondaryStat.Speed, effect.getInfo().get(MapleStatInfo.speed));
                statups.put(SecondaryStat.IndieStance, effect.getInfo().get(MapleStatInfo.indieStance));
                return 1;
            case 緋紅加持:
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.IndieCr, effect.getInfo().get(MapleStatInfo.indieCr));
                return 1;
            case 迸發加持:
                statups.put(SecondaryStat.IndieEVAR, effect.getInfo().get(MapleStatInfo.indieEvaR));
                statups.put(SecondaryStat.IndieBooster, 1);
                return 1;
            case 深淵加持效果:
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                statups.put(SecondaryStat.IndieBDR, effect.getInfo().get(MapleStatInfo.indieBDR));
                statups.put(SecondaryStat.IndieIgnoreMobpdpR, effect.getInfo().get(MapleStatInfo.indieIgnoreMobpdpR));
                return 1;
            case 浮游:
                statups.put(SecondaryStat.NewFlying, 1);
                effect.getInfo().put(MapleStatInfo.time, 3000);
                return 1;
            case 侵蝕控制:
                statups.put(SecondaryStat.SpecterMode, 1);
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.IndieStance, effect.getInfo().get(MapleStatInfo.indieStance));
                return 1;
            case 神之種族:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 根源的記憶:
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
            case 無限技能:
                statups.put(SecondaryStat.LPInfinitySpell, effect.getInfo().get(MapleStatInfo.x));
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        switch (applier.effect.getSourceId()) {
            case 侵蝕控制: {
                final MapleStatEffect skillEffect10;
                if ((skillEffect10 = chr.getSkillEffect(幽靈侵蝕)) == null) {
                    return 1;
                }
                if (chr.getBuffedIntValue(SecondaryStat.SpecterMode) > 0) {
                    chr.dispelEffect(幽靈侵蝕);
                    return 1;
                }
                skillEffect10.applyTo(chr);
                return 1;
            }
            case 迷惑之拘束_1: {
                final MapleStatEffect skillEffect6;
                if ((skillEffect6 = chr.getSkillEffect(幽靈侵蝕)) != null) {
                    skillEffect6.applyTo(chr);
                }
                return 1;
            }
            case 咒術子彈: {
                final int ao4 = slea.readByte();
                final List<Integer> list16 = new ArrayList<>();
                final Map<Integer, Map<Integer, MapleForceAtom>> hashMap = new HashMap<>();
                for (int n1100 = 0; n1100 < ao4; ++n1100) {
                    list16.add(slea.readInt());
                }
                if (chr.getPureBeads() > 0) {
                    Map<Integer, MapleForceAtom> map2 = hashMap.computeIfAbsent(原始技能, k -> new HashMap<>());
                    for (int n1101 = 0; n1101 < chr.getPureBeads(); ++n1101) {
                        map2.put(list16.size(), forceFactory.getMapleForce(chr, chr.getSkillEffect(原始技能), 0));
                    }
                }
                if (chr.getFlameBeads() > 0) {
                    Map<Integer, MapleForceAtom> map3 = hashMap.computeIfAbsent(緋紅技能, k -> new HashMap<>());
                    for (int n1102 = 0; n1102 < chr.getFlameBeads(); ++n1102) {
                        map3.put(list16.size(), forceFactory.getMapleForce(chr, chr.getSkillEffect(緋紅技能), 0));
                    }
                }
                if (chr.getGaleBeads() > 0) {
                    Map<Integer, MapleForceAtom> map4 = hashMap.computeIfAbsent(迸發技能, k -> new HashMap<>());
                    for (int n1103 = 0; n1103 < chr.getGaleBeads(); ++n1103) {
                        map4.put(list16.size(), forceFactory.getMapleForce(chr, chr.getSkillEffect(迸發技能), 0));
                    }
                }
                if (chr.getAbyssBeads() > 0) {
                    Map<Integer, MapleForceAtom> map5 = hashMap.computeIfAbsent(深淵技能, k -> new HashMap<>());
                    for (int n1104 = 0; n1104 < chr.getAbyssBeads(); ++n1104) {
                        map5.put(list16.size(), forceFactory.getMapleForce(chr, chr.getSkillEffect(深淵技能), 0));
                    }
                }
                chr.getMap().broadcastMessage(chr, ForcePacket.showBeads(chr.getId(), hashMap), true);
                chr.addPureBeads(-5);
                chr.addFlameBeads(-5);
                chr.addGaleBeads(-5);
                chr.addAbyssBeads(-5);
                return 1;
            }
            case 歸來的憎恨: {
                final Map<Integer, Point> wreckagesMap = chr.getWreckagesMap();
                final ArrayList<Integer> oids = new ArrayList<>();
                for (MapleMapObject monster : chr.getMap().getMapObjectsInRange(chr.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                    oids.add(monster.getObjectId());
                    if (oids.size() >= applier.effect.getMobCount()) {
                        break;
                    }
                }
                if (!wreckagesMap.isEmpty() && !oids.isEmpty()) {
                    chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(forceFactory.getMapleForce(chr, applier.effect, oids, wreckagesMap.values())), true);
                    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                    mplew.writeShort(OutHeader.LP_DEL_WRECKAGE.getValue());
                    mplew.writeInt(chr.getId());
                    mplew.writeInt(wreckagesMap.size());
                    mplew.write(0);
                    mplew.write(0);//V.181 new
                    wreckagesMap.keySet().forEach(mplew::writeInt);
                    chr.getMap().broadcastMessage(chr, mplew.getPacket(), true);
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 無我:
            case 無我_傳授: {
                int value = applyto.getBuffedIntValue(SecondaryStat.LPBattleMode);
                if ((value = (applier.passive ? Math.max(0, value - 1) : Math.min(applier.effect.getX(), value + 1))) > 0) {
                    applier.localstatups.put(SecondaryStat.IndieDamR, applier.localstatups.get(SecondaryStat.IndieDamR) + (value * applier.effect.getY()));
                    applier.localstatups.put(SecondaryStat.LPBattleMode, value);
                    return 1;
                }
                applier.overwrite = false;
                applier.localstatups.clear();
                return 1;
            }
            case 逼近的死亡: {
                if (applyto.getBuffedValue(SecondaryStat.ComingDeath) != null) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                }
                return 1;
            }
            case 迷惑之拘束_1: {
                applyto.getClient().announce(MaplePacketCreator.UserCreateAreaDotInfo(1, 迷惑之拘束_1, applier.effect.calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft())));
                return 1;
            }
            case 戰鬥狂亂: {
                applier.localstatups.put(SecondaryStat.CombatFrenzy, Math.min(applier.effect.getX(), applyto.getBuffedIntValue(SecondaryStat.CombatFrenzy) + 1));
                return 1;
            }
            case 回想起的噩夢:
            case 不會結束的噩夢: {
                applyto.registerSkillCooldown(applyto.getSkillEffect(無法遺忘的噩夢), true);
                return 1;
            }
            case 神之種族: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(Config.constants.skills.阿戴爾.神之種族);
                applyto.dispelEffect(Config.constants.skills.伊利恩.神之種族);
                applyto.dispelEffect(Config.constants.skills.亞克.神之種族);
                applyto.dispelEffect(Config.constants.skills.卡莉.神之種族);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.effect != null && applier.totalDamage > 0L) {
            switch (applier.effect.getSourceId()) {
                case 原始技能:
                case 逼近的死亡_1:
                case 緋紅技能:
                case 迸發技能:
                case 歸來的憎恨:
                case 深淵技能: {
                    break;
                }
                default: {
                    final MapleStatEffect effecForBuffStat18;
                    if (player.getBuffedValue(SecondaryStat.SpecterMode) != null && (effecForBuffStat18 = player.getEffectForBuffStat(SecondaryStat.ComingDeath)) != null) {
                        List<MapleMapObject> mobs = player.getMap().getMapObjectsInRect(effecForBuffStat18.calculateBoundingBox(player.getPosition(), player.isFacingLeft(), 500), Collections.singletonList(MapleMapObjectType.MONSTER));
                        final ArrayList<Integer> list = new ArrayList<>();
                        mobs.forEach(mob -> list.add(mob.getObjectId()));
                        if (!list.isEmpty()) {
                            player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(MapleForceFactory.getInstance().getMapleForce(player, player.getSkillEffect(逼近的死亡_1), 0, list)), true);
                        }
                    }
                    final MapleStatEffect skillEffect26;
                    if ((player.getCheatTracker().getLastAttackSkill() == 深淵猛擊 || player.getCheatTracker().getLastAttackSkill() == 原始猛擊) && applier.effect.getSourceId() != 深淵猛擊 && applier.effect.getSourceId() != 原始猛擊 && (skillEffect26 = player.getSkillEffect(戰鬥狂亂)) != null) {
                        skillEffect26.applyTo(player);
                    }
                    break;
                }
            }
            switch (applier.effect.getSourceId()) {
                case 原始猛擊: {
                    player.addPureBeads(1);
                    return 1;
                }
                case 緋紅猛擊:
                case 緋紅猛擊_3: {
                    player.addFlameBeads(1);
                    ExtraSkill eskill = new ExtraSkill(緋紅猛擊_1, player.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                    player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), Collections.singletonList(eskill)));
                    return 1;
                }
                case 無法遺忘的噩夢: { // TODO 固定次數後觸發
                    if (player.getSkillEffect(侵蝕控制) == null && player.getSkillEffect(幽靈侵蝕) != null) {
//                            player.getSkillEffect(幽靈侵蝕).applyTo(player);
                    }
                    return 1;
                }
                case 迸發猛擊: {
                    player.addGaleBeads(1);
                    return 1;
                }
                case 深淵猛擊: {
                    player.addAbyssBeads(1);
                    ExtraSkill eskill = new ExtraSkill(緋紅猛擊_1, player.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                    player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), Collections.singletonList(eskill)));
                    return 1;
                }
                case 迷惑之拘束: {
                    if (player.getSkillEffect(幽靈侵蝕) != null) {
                        player.getSkillEffect(幽靈侵蝕).applyTo(player);
                    }
                    ExtraSkill eskill = new ExtraSkill(迷惑之拘束_1, player.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                    player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), Collections.singletonList(eskill)));
                    return 1;
                }
                case 無法控制住的混沌: {
                    ExtraSkill eskill = new ExtraSkill(無法控制住的混沌_1, player.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                    player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), Collections.singletonList(eskill)));
                    return 1;
                }
                case 無法消除的傷痕: {
                    ExtraSkill eskill = new ExtraSkill(無法消除的傷痕_1, player.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                    player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), Collections.singletonList(eskill)));
                    return 1;
                }
                case 根源的記憶: {
                    final MapleStatEffect skillEffect27;
                    if ((skillEffect27 = player.getSkillEffect(幽靈侵蝕)) != null && player.getBuffedValue(SecondaryStat.SpecterMode) == null) {
                        skillEffect27.applyTo(player);
                        break;
                    }
                    return 1;
                }
            }
        }
        return -1;
    }
}
