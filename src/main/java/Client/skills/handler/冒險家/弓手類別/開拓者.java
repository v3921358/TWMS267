package Client.skills.handler.冒險家.弓手類別;

import Client.*;
import Client.force.MapleForceAtom;
import Client.force.MapleForceFactory;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.箭神;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.重砲指揮官;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.life.MobSkill;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Net.server.maps.MapleSummon;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import Packet.BuffPacket;
import Packet.EffectPacket;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Config.constants.skills.開拓者.*;

public class 開拓者 extends AbstractSkillHandler {

    public 開拓者() {
        jobs = new MapleJob[]{
                MapleJob.開拓者1轉,
                MapleJob.開拓者2轉,
                MapleJob.開拓者3轉,
                MapleJob.開拓者4轉
        };

        for (Field field : Config.constants.skills.開拓者.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case HEXA_忘卻遺跡_II_延伸:
            case HEXA_忘卻遺跡_III_延伸:
            case HEXA_忘卻遺跡_IV_延伸:
            case HEXA_忘卻遺跡_V_延伸:
                return HEXA_忘卻遺跡;
            case HEXA_基本爆破_VI_延伸:
                return HEXA_基本爆破_VI;
            case HEXA_附加爆破_VI_延伸:
                return HEXA_附加爆破_VI;
            case 雙重跳躍_1:
            case 雙重跳躍_2:
            case 雙重跳躍_3:
                return 雙重跳躍;
            case 基本爆破_1:
                return 基本爆破;
            case 分裂魔矢_1:
                return 分裂魔矢;
            case 基本爆破強化_1:
                return 基本爆破強化;
            case 基本轉移_1:
                return 基本轉移;
            case 三重衝擊_1:
                return 三重衝擊;
            case 基本釋放4轉:
            case 基本爆破4轉:
            case 基本爆破4轉_1:
            case 基本轉移4轉:
            case 基本轉移4轉_1:
                return 進階基本之力;
            case 連段襲擊_1:
            case 連段襲擊_釋放:
            case 連段襲擊_釋放_1:
            case 連段襲擊_爆破:
            case 連段襲擊_爆破_1:
            case 連段襲擊_轉移:
            case 連段襲擊_轉移_1:
                return 連段襲擊;
            case 古代神矢_釋放:
            case 古代神矢_釋放_1:
            case 古代神矢_爆破_1:
            case 古代神矢_爆破:
            case 古代神矢_轉移:
                return 古代神矢;
            case 黑曜石屏障_1:
            case 黑曜石屏障_釋放:
            case 黑曜石屏障_爆破:
            case 黑曜石屏障_爆破_1:
            case 黑曜石屏障_轉移:
            case 黑曜石屏障_轉移_1:
                return 黑曜石屏障;
            case 究極炸裂_1:
                return 究極炸裂;
            case 遺跡解放_釋放:
            case 遺跡解放_釋放_1:
            case 遺跡解放_爆破:
            case 遺跡解放_爆破_1:
            case 遺跡解放_轉移:
                return 遺跡解放;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 詛咒弱化Ⅰ:
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                statups.put(SecondaryStat.IndieCr, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 遺跡填充Ⅰ:
            case 遺跡填充Ⅱ:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.SecondAtomLockOn, 0);
                statups.put(SecondaryStat.PathFinderAncientGuidance, -1);
                return 1;
            case 基本釋放:
            case 基本釋放強化:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.LastUseSkillAttr, 1);
                return 1;
            case 基本爆破:
            case 基本爆破_1:
            case 基本爆破強化:
            case 基本爆破強化_1:
            case 基本爆破4轉:
            case 基本爆破4轉_1:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.LastUseSkillAttr, 2);
                return 1;
            case 詛咒弱化Ⅱ:
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                statups.put(SecondaryStat.IndiePDDR, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 詛咒弱化Ⅲ:
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                statups.put(SecondaryStat.IndieAsrR, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 基本轉移:
            case 基本轉移_1:
            case 基本轉移4轉:
            case 基本轉移4轉_1:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.CannonShooter_BFCannonBall, 0);
                statups.put(SecondaryStat.LastUseSkillAttr, 3);
                return 1;
            case 詛咒轉移:
                monsterStatus.put(MonsterStatus.CurseTransition, 1);
                return 1;
            case 附加轉移:
                effect.getInfo().put(MapleStatInfo.time, effect.getZ());
                statups.put(SecondaryStat.TempSecondaryStat, effect.getY());
                return 1;
            case 古代神矢_爆破:
                statups.put(SecondaryStat.Stance, 100);
                return 1;
            case 遺跡進化:
            case 遺跡解放_釋放:
            case 遺跡解放_爆破:
            case 遺跡解放_轉移:
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 古代指揮:
                statups.put(SecondaryStat.IndiePMdR, effect.getInfo().get(MapleStatInfo.indiePMdR));
                return 1;
            case 詛咒耐性:
                statups.put(SecondaryStat.IndieAsrR, effect.getInfo().get(MapleStatInfo.s));
                return 1;
            case 連段襲擊:
            case 連段襲擊_釋放:
            case 連段襲擊_爆破:
            case 連段襲擊_轉移:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.LastUseSkillAttr, 0);
                return 1;
            case 會心之眼:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.SharpEyes, (effect.getX() << 8) + effect.getY());
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 傳說冒險:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 黑曜石屏障_1:
            case 黑曜石屏障_爆破_1:
            case 黑曜石屏障_轉移_1:
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                return 1;
            case 黑曜石屏障_釋放:
                statups.put(SecondaryStat.IndieDamReduceR, 1);
                statups.put(SecondaryStat.IndieAllHitDamR, -effect.getX());
//                statups.put(MapleBuffStat.黑曜石屏障, 1);
                return 1;
            case 黑曜石屏障:
            case 黑曜石屏障_爆破:
            case 黑曜石屏障_轉移:
                statups.put(SecondaryStat.IndieDamReduceR, 1);
                return 1;
            case 遺跡解放_釋放_1:
                effect.getInfo().put(MapleStatInfo.time, 4000);
                statups.put(SecondaryStat.RelicUnboundD, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        switch (applier.effect.getSourceId()) {
            case 基本釋放:
            case 基本釋放強化: {
                applier.pos = slea.readPos();
                List<Integer> oids = IntStream.range(0, slea.readByte()).mapToObj(i -> slea.readInt()).collect(Collectors.toList());
                if (!oids.isEmpty()) {
                    chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(forceFactory.getMapleForce(chr, applier.effect, 0, oids)), true);
                }
                MapleStatEffect effect = chr.getSkillEffect(附加爆破);
                if (effect != null && chr.getBuffedIntValue(SecondaryStat.LastUseSkillAttr) == 2 && effect.makeChanceResult(chr)) {
                    List<Integer> mobOids = new ArrayList<>();
                    if (oids.size() > 0) {
                        for (int i = 0; i < effect.getBulletCount() + (chr.getBuffStatValueHolder(遺跡進化) != null ? 1 : 0); i++) {
                            mobOids.add(oids.get(i % oids.size()));
                        }
                        MapleForceAtom mapleForce = forceFactory.getMapleForce(chr, effect, 0, mobOids, chr.getPosition());
                        chr.getMap().broadcastMessage(ForcePacket.forceAtomCreate(mapleForce), chr.getPosition());
                    }
                }
                return 1;
            }
            case 渡鴉召喚: {
                applier.pos = slea.readPos();
                return 1;
            }
            case 遺跡解放_轉移: {
                if (slea.readByte() != 0) {
                    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.SummonedSkillUseRequest);
                    mplew.writeInt(遺跡解放_轉移);
                    mplew.writePosInt(chr.getPosition());
                    mplew.write(0);
                    mplew.writeInt(0); // nCount
                    chr.send(mplew.getPacket());
                    applier.effect = null;
                    return 1;
                }
                int nCount = slea.readInt();
                if (nCount <= 0) {
                    return 0;
                }
                for (int i = 0; i < nCount; i++) {
                    applier.effect.applyBuffEffect(chr, chr, applier.effect.getSummonDuration(chr), false, false, false, slea.readPos());
                    slea.skip(3);
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyTo(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (!applier.passive) {
            int force = applyfrom.getBuffedIntValue(SecondaryStat.SecondAtomLockOn);
            int forceCon = applier.effect.getForceCon();
            if (force >= forceCon && applier.effect.getSourceId() == 究極炸裂) {
                forceCon = force;
            }
            if (force < forceCon) {
                return 0;
            }
            handleRelicsGain(applyfrom, -forceCon);
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect instanceof MobSkill) {
            SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.IndieAsrR, 詛咒耐性);
            if (mbsvh != null) {
                int maxValue = mbsvh.effect.getS() + (mbsvh.effect.getX() * mbsvh.effect.getY());
                if (mbsvh.value < maxValue) {
                    mbsvh.value = Math.min(maxValue, mbsvh.value + mbsvh.effect.getX());
                    applyto.send(BuffPacket.giveBuff(applyto, mbsvh.effect, Collections.singletonMap(SecondaryStat.IndieAsrR, mbsvh.effect.getSourceId())));
                }
            }
            return -1;
        }
        switch (applier.effect.getSourceId()) {
            case 回歸帕爾坦: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 基本釋放:
            case 基本釋放強化:
            case 基本爆破:
            case 基本爆破_1:
            case 基本爆破強化:
            case 基本爆破強化_1:
            case 基本爆破4轉:
            case 基本爆破4轉_1:
                MapleStatEffect effect = applyto.getSkillEffect(附加轉移);
                if (effect != null && applyto.getBuffedIntValue(SecondaryStat.LastUseSkillAttr) == 3) {
                    effect.applyBuffEffect(applyto, applyto, effect.getBuffDuration(applyto), false, false, true, null);
                }
                break;
        }
        switch (applier.effect.getSourceId()) {
            case 遺跡填充Ⅰ:
            case 遺跡填充Ⅱ: {
                applier.buffz = 0;
                return 1;
            }
            case 基本轉移:
            case 基本轉移_1:
            case 基本轉移4轉:
            case 基本轉移4轉_1: {
                if (applier.att) {
                    return 0;
                }
                final int value = Math.min(applier.effect.getY(), Math.max(0, applyto.getBuffedIntValue(SecondaryStat.CannonShooter_BFCannonBall) + (!applier.primary && applier.passive ? 1 : -1)));
                applier.localstatups.put(SecondaryStat.CannonShooter_BFCannonBall, value);
                if (!applier.primary || applier.passive) {
                    applier.localstatups.remove(SecondaryStat.LastUseSkillAttr);
                }
                return 1;
            }
            case 古代指揮: {
                applyto.addHPMP(applier.effect.getY(), applier.effect.getY());
                return 1;
            }
            case 會心之眼: {
                MapleStatEffect effect;
                applier.buffz = 0;
                if ((effect = applyfrom.getSkillEffect(會心之眼_無視防禦)) != null) {
                    applier.buffz = effect.getIndieIgnoreMobpdpR();
                }
                if ((effect = applyfrom.getSkillEffect(會心之眼_爆擊)) != null) {
                    applier.localstatups.put(SecondaryStat.SharpEyes, applier.localstatups.get(SecondaryStat.SharpEyes) + (effect.getX() << 8));
                }
                return 1;
            }
            case 遺跡進化: {
                handleRelicsGain(applyto, 1000);
                return 1;
            }
            case 傳說冒險: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(英雄.傳說冒險);
                applyto.dispelEffect(聖騎士.傳說冒險);
                applyto.dispelEffect(黑騎士.傳說冒險);
                applyto.dispelEffect(火毒.傳說冒險);
                applyto.dispelEffect(冰雷.傳說冒險);
                applyto.dispelEffect(主教.傳說冒險);
                applyto.dispelEffect(箭神.傳說冒險);
                applyto.dispelEffect(Config.constants.skills.神射手.傳說冒險);
                applyto.dispelEffect(Config.constants.skills.開拓者.傳說冒險);
                applyto.dispelEffect(暗影神偷.傳說冒險);
                applyto.dispelEffect(夜使者.傳說冒險);
                applyto.dispelEffect(影武者.傳說冒險);
                applyto.dispelEffect(拳霸.傳說冒險);
                applyto.dispelEffect(槍神.傳說冒險);
                applyto.dispelEffect(重砲指揮官.傳說冒險);
                return 1;
            }
            case 渡鴉風暴: {
                applyto.dispelEffect(渡鴉召喚);
                return 1;
            }
            case 黑曜石屏障: {
                if (applyfrom == applyto && applier.primary) {
                    applier.effect.applyAffectedArea(applyto, applier.pos);
                    MapleStatEffect effect = SkillFactory.getSkill(黑曜石屏障_1).getEffect(applier.effect.getLevel());
                    effect.applyBuffEffect(applyfrom, applyto, effect.getBuffDuration(applyfrom), applier.primary, applier.att, applier.passive, applier.pos);
                }
                return 1;
            }
            case 黑曜石屏障_爆破: {
                if (applyfrom == applyto && applier.primary) {
                    applier.effect.applyAffectedArea(applyto, applier.pos);
                    MapleStatEffect effect = SkillFactory.getSkill(黑曜石屏障_爆破_1).getEffect(applier.effect.getLevel());
                    effect.applyBuffEffect(applyfrom, applyto, effect.getBuffDuration(applyfrom), applier.primary, applier.att, applier.passive, applier.pos);
                }
                return 1;
            }
            case 黑曜石屏障_轉移: {
                if (applyfrom == applyto && applier.primary) {
                    applier.effect.applyAffectedArea(applyto, applier.pos);
                    MapleStatEffect effect = SkillFactory.getSkill(黑曜石屏障_轉移_1).getEffect(applier.effect.getLevel());
                    effect.applyBuffEffect(applyfrom, applyto, effect.getBuffDuration(applyfrom), applier.primary, applier.att, applier.passive, applier.pos);
                }
                return 1;
            }
            case 遺跡解放_釋放_1: {
                if (!applier.primary) {
                    return 0;
                }
                return 1;
            }
            case 遺跡解放_轉移: {
                if (applier.primary) {
                    return 0;
                }
                return 1;
            }
        }
        if (applier.localstatups.containsKey(SecondaryStat.LastUseSkillAttr) && applier.localstatups.get(SecondaryStat.LastUseSkillAttr) != 0) {
            int time = 0;
            MapleStatEffect effect = applyto.getSkillEffect(遺跡填充Ⅱ);
            if (effect == null) {
                effect = applyto.getSkillEffect(遺跡填充Ⅰ);
                if (effect != null) {
                    time = (int) (effect.getInfoD().get(MapleStatInfo.t) * 1000);
                }
            } else {
                time = effect.getW() * 1000;
            }
            SecondaryStatValueHolder mbsvh;
            if (time > 0 && (mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.LastUseSkillAttr)) != null && mbsvh.value != applier.localstatups.get(SecondaryStat.LastUseSkillAttr)) {
                applyto.reduceSkillCooldown(分裂魔矢, time);
                applyto.reduceSkillCooldown(三重衝擊, time);
                applyto.reduceSkillCooldown(稜之共鳴, time);
                applyto.reduceSkillCooldown(連段襲擊, time);
                applyto.reduceSkillCooldown(古代神矢, time);
            }
        }
        return -1;
    }

    @Override
    public int onAfterRegisterEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.localstatups.containsKey(SecondaryStat.SecondAtomLockOn)) {
            SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.SecondAtomLockOn);
            if (mbsvh != null) {
                mbsvh.sourceID = 0;
            }
        }
        if (applier.localstatups.containsKey(SecondaryStat.PathFinderAncientGuidance)) {
            SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.PathFinderAncientGuidance);
            if (mbsvh != null) {
                mbsvh.sourceID = applyto.getJob();
            }
        }
        if (applier.localstatups.containsKey(SecondaryStat.LastUseSkillAttr)) {
            SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.LastUseSkillAttr);
            if (mbsvh != null) {
                mbsvh.sourceID = applyto.getSkillEffect(遺跡填充Ⅱ) != null ? 2 : 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applier.effect == null) {
            return -1;
        }
        switch (applier.effect.getSourceId()) {
            case 分裂魔矢:
                List<MapleMapObject> mobs = applyfrom.getMap().getMapObjectsInRange(applyfrom.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER));
                List<Integer> mobOids = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    mobOids.add(mobs.get(Randomizer.nextInt(mobs.size())).getObjectId());
                }
                applyfrom.getMap().broadcastMessage(applyfrom, ForcePacket.forceAtomCreate(MapleForceFactory.getInstance().getMapleForce(applyfrom, applyfrom.getSkillEffect(分裂魔矢_1), applyto.getId(), mobOids, applyto.getPosition())), true);
                break;
            case 連段襲擊:
            case 連段襲擊_釋放:
            case 連段襲擊_爆破:
            case 連段襲擊_轉移:
                MapleStatEffect effect = applyfrom.getSkillEffect(詛咒轉移);
                if (effect != null) {
                    effect.applyMonsterEffect(applyfrom, applyto, effect.getMobDebuffDuration(applyfrom));
                }
                break;
        }
        SecondaryStatValueHolder mbsvh;
        if (applyto.isAlive() && (mbsvh = applyfrom.getBuffStatValueHolder(附加轉移)) != null && mbsvh.effect != null) {
            if (mbsvh.value > 0) {
                mbsvh.value--;
                MapleStatEffect effect = applyfrom.getSkillEffect(詛咒轉移);
                if (effect != null && mbsvh.effect.makeChanceResult(applyfrom)) {
                    effect.applyMonsterEffect(applyfrom, applyto, effect.getMobDebuffDuration(applyfrom));
                }
            }
            if (mbsvh.value <= 0) {
                applyfrom.dispelEffect(附加轉移);
            } else {
                applyfrom.send(BuffPacket.giveBuff(applyfrom, mbsvh.effect, Collections.singletonMap(SecondaryStat.TempSecondaryStat, -1)));
            }
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.effect == null) {
            return -1;
        }
        MapleStatEffect effect = player.getSkillEffect(遺跡填充Ⅱ);
        if (effect == null) {
            effect = player.getSkillEffect(遺跡填充Ⅰ);
        }
        if (applier.ai.mobAttackInfo.size() > 0 && effect != null) {
            switch (applier.effect.getSourceId()) {
                case 基本爆破:
                case 基本爆破_1:
                case 基本爆破強化:
                case 基本爆破強化_1:
                case 基本爆破4轉:
                case 基本爆破4轉_1:
                case 基本轉移:
                case 基本轉移_1:
                case 基本轉移4轉:
                case 基本轉移4轉_1:
                    handleRelicsGain(player, effect.getY());
                    break;
                case 基本釋放:
                case 基本釋放強化:
                    handleRelicsGain(player, effect.getX());
                    break;
                case 渡鴉召喚:
                    handleRelicsGain(player, effect.getS());
                    break;
                case 渡鴉風暴:
                    handleRelicsGain(player, effect.getV());
                    break;
            }
        }

        final MapleForceFactory mff = MapleForceFactory.getInstance();
        List<MapleMapObject> mobs;
        switch (applier.effect.getSourceId()) {
            case 連段襲擊:
            case 連段襲擊_釋放:
            case 連段襲擊_爆破:
            case 連段襲擊_轉移:
                player.getClient().announce(MaplePacketCreator.userBonusAttackRequest(applier.effect.getSourceId() + 1, 600, Collections.emptyList()));
                break;
            case 基本爆破:
            case 基本爆破_1:
            case 基本爆破強化:
            case 基本爆破強化_1:
            case 基本爆破4轉:
            case 基本爆破4轉_1:
                effect = player.getSkillEffect(附加釋放);
                if (applier.ai.mobAttackInfo.size() > 0 && effect != null && player.getBuffedIntValue(SecondaryStat.LastUseSkillAttr) == 1 && effect.makeChanceResult(player)) {
                    mobs = player.getMap().getMapObjectsInRange(player.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER));
                    List<Integer> mobOids = new ArrayList<>();
                    if (mobs.size() > 0) {
                        for (int i = 0; i < effect.getBulletCount() + (player.getBuffStatValueHolder(遺跡進化) != null ? 1 : 0); i++) {
                            mobOids.add(mobs.get(i % mobs.size()).getObjectId());
                        }
                        MapleForceAtom mapleForce = mff.getMapleForce(player, effect, 0, mobOids, player.getPosition());
                        player.getMap().broadcastMessage(ForcePacket.forceAtomCreate(mapleForce), player.getPosition());
                    }
                }
                break;
        }
        if (applier.ai.skillId != 遺跡解放_釋放_1 && applier.ai.mobAttackInfo.size() > 0 && player.getSummonBySkillID(遺跡解放_釋放) != null && player.getBuffStatValueHolder(遺跡解放_釋放_1) == null) {
            player.getSkillEffect(遺跡解放_釋放_1).applyTo(player);
        }
        if (applier.ai.skillId != 遺跡解放_爆破_1 && applier.ai.skillId != 遺跡解放_轉移) {
            boolean cd1 = player.isSkillCooling(遺跡解放_爆破);
            boolean cd2 = player.isSkillCooling(遺跡解放_轉移);
            for (MapleSummon sum : player.getSummonsReadLock()) {
                int value = -1;
                int b = 0;
                switch (sum.getSkillId()) {
                    case 遺跡解放_爆破:
                        if (!cd1) {
                            value = 遺跡解放_爆破;
                            if (!player.isSkillCooling(遺跡解放_爆破)) {
                                player.registerSkillCooldown(遺跡解放_爆破, 1000, false);
                            }
                        }
                        break;
                    case 遺跡解放_轉移:
                        if (!cd2) {
                            value = 0;
                            b = 1;
                            if (!player.isSkillCooling(遺跡解放_轉移)) {
                                player.registerSkillCooldown(遺跡解放_轉移, 4000, false);
                            }
                        }
                        break;
                }
                if (value >= 0) {
                    player.send(MaplePacketCreator.summonedBeholderRevengeInfluence(player.getId(), sum.getObjectId(), value, b));
                }
            }
            player.unlockSummonsReadLock();
        }
        return 1;
    }

    public void handleRelicsGain(MapleCharacter player, int value) {
        if (value == 0) {
            return;
        }
        MapleStatEffect effect = player.getSkillEffect(遺跡填充Ⅱ);
        int maxValue;
        if (effect == null) {
            effect = player.getSkillEffect(遺跡填充Ⅰ);
            maxValue = effect.getU();
        } else {
            maxValue = effect.getV();
        }
        if (player.getBuffStatValueHolder(SecondaryStat.SecondAtomLockOn) == null || player.getBuffStatValueHolder(SecondaryStat.PathFinderAncientGuidance) == null) {
            effect.applyBuffEffect(player, player, 2100000000, false, false, true, null);
        }

        Map<SecondaryStat, Integer> statups = new LinkedHashMap<>();
        SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.SecondAtomLockOn);
        if (mbsvh != null) {
            if (mbsvh.value > maxValue) {
                mbsvh.value = maxValue;
            }
            value = Math.min(maxValue - mbsvh.value, value);
            mbsvh.value = Math.max(0, mbsvh.value + value);
            if (mbsvh.effect != effect) {
                mbsvh.effect = effect;
            }
            statups.put(SecondaryStat.SecondAtomLockOn, -1);
        } else {
            value = 0;
        }

        if (value > 0 && (mbsvh = player.getBuffStatValueHolder(SecondaryStat.PathFinderAncientGuidance)) != null) {
            mbsvh.z = Math.min(maxValue, mbsvh.z + value);
            if (mbsvh.effect != effect) {
                mbsvh.effect = effect;
            }
            MapleStatEffect eff;
            if (mbsvh.z >= maxValue && (eff = player.getSkillEffect(古代指揮)) != null) {
                mbsvh.z = 0;
                eff.applyBuffEffect(player, player, eff.getBuffDuration(player), false, false, true, null);
                player.send(EffectPacket.encodeUserEffectLocal(eff.getSourceId(), EffectOpcode.UserEffect_SkillUse, player.getLevel(), eff.getLevel()));
                player.getMap().broadcastMessage(player, EffectPacket.onUserEffectRemote(player, eff.getSourceId(), EffectOpcode.UserEffect_SkillUse, player.getLevel(), eff.getLevel()), false);
            }
            statups.put(SecondaryStat.PathFinderAncientGuidance, -1);
        }

        if (!statups.isEmpty()) {
            player.send(BuffPacket.giveBuff(player, effect, statups));
        }
    }
}
