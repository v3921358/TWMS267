package Client.skills.handler.冒險家.弓手類別;

import Client.*;
import Client.force.MapleForceFactory;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.SkillConstants;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.重砲指揮官;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.life.MobSkill;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import Packet.EffectPacket;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import Server.channel.handler.AttackInfo;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

import static Config.constants.skills.冒險家_技能群組.箭神.*;

public class 箭神 extends AbstractSkillHandler {

    public 箭神() {
        jobs = new MapleJob[]{
                MapleJob.獵人,
                MapleJob.遊俠,
                MapleJob.箭神
        };

        for (Field field : Config.constants.skills.冒險家_技能群組.箭神.class.getDeclaredFields()) {
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
            case HEXA_暴風神射_VI_亂射模式:
                return HEXA_暴風神射_VI;
            case HEXA_箭影紛飛_延伸_II:
            case HEXA_箭影紛飛_延伸:
                return HEXA_箭影紛飛;
            case 閃光幻象_1:
                return 閃光幻象;
            case 回歸箭筒_1:
                return 回歸箭筒;
            case 箭座_攻擊:
                return 箭座;
            case 魔幻箭筒_2轉:
                return 魔幻箭筒;
            case 魔幻箭筒_4轉:
                return 無限箭筒;
            case 箭雨_1:
                return 箭雨;
            case 殘影之矢_1:
                return 殘影之矢;
            case 焰箭齊發_1:
                return 焰箭齊發;
            case 殘影幻象_1:
                return 殘影幻象;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 魔幻箭筒:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.QuiverCatridge, 1);
                return 1;
            case 閃光幻象:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.FlashMirage, 1);
                return 1;
            case 召喚鳳凰:
                effect.setDebuffTime(4000);
                monsterStatus.put(MonsterStatus.Stun, 1);

                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 回歸箭筒:
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 致命箭:
                statups.put(SecondaryStat.BowMasterMortalBlow, 0);
                return 1;
            case 集中專注:
                statups.put(SecondaryStat.BowMasterConcentration, 0);
                return 1;
            case 會心之眼:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.SharpEyes, (effect.getX() << 8) + effect.getY());
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 波紋衝擊:
                monsterStatus.put(MonsterStatus.IndieSlow, Math.abs(effect.getS()));
                return 1;
            case 傳說冒險:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 戰鬥準備:
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.Preparation, 1);
                return 1;
            case 箭雨:
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 箭雨_1:
                effect.getInfo().put(MapleStatInfo.time, 2500);
                return 1;
            case 殘影之矢:
                statups.put(SecondaryStat.TempSecondaryStat, 1);
                return 1;
            case 焰箭齊發:
                statups.put(SecondaryStat.QuiverFullBurst, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 殘影幻象:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.ShadowShield, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect instanceof MobSkill) {
            applyto.dispelEffect(SecondaryStat.BowMasterConcentration);
            applyto.send(EffectPacket.showBlessOfDarkness(-1, 集中專注));
            applyto.getMap().broadcastMessage(applyto, EffectPacket.showBlessOfDarkness(applyto.getId(), 集中專注), false);
            return -1;
        }
        switch (applier.effect.getSourceId()) {
            case 魔幻箭筒: {
                if (applier.primary) {
                    int mode = 1;
                    if (applyfrom.getSkillEffect(無限箭筒) != null && applyfrom.getBuffedIntValue(SecondaryStat.QuiverCatridge) == 1) {
                        mode = 2;
                        applier.localstatups.put(SecondaryStat.QuiverCatridge, 2);
                    }
                    applyto.send(EffectPacket.showSkillMode(-1, applier.effect.getSourceId(), mode, 0));
                    applyto.getMap().broadcastMessage(applyto, EffectPacket.showSkillMode(applyto.getId(), applier.effect.getSourceId(), mode, 0), false);
                }
                return 1;
            }
            case 閃光幻象:
                if (applier.primary) {
                    if (applyto.getBuffedValue(SecondaryStat.FlashMirage) != null) {
                        applyto.dispelEffect(閃光幻象);
                        return 0;
                    }
                } else {
                    int value = applyto.getBuffedIntValue(SecondaryStat.FlashMirage) + 1;
                    MapleStatEffect effect = applyto.getSkillEffect(閃光幻象II);
                    if (effect == null) {
                        effect = applier.effect;
                    }
                    if (value > effect.getU()) {
                        value = effect.getU();
                        if (!applyto.isSkillCooling(閃光幻象_1)) {
                            applyto.registerSkillCooldown(閃光幻象_1, applier.effect.getCooldown(applyfrom), true);
                            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                            mplew.writeShort(OutHeader.SpeedMirageAttack.getValue());
                            mplew.writeInt(effect.getW());
                            mplew.writeInt(0);
                            applyto.send(mplew.getPacket());
                        }
                    }
                    applier.localstatups.put(SecondaryStat.FlashMirage, value);
                }
                return 1;
            case 致命箭: {
                if (applyto.getBuffStatValueHolder(SecondaryStat.IndieDamR, 致命箭) != null) {
                    return 0;
                }
                final int value = applyto.getBuffedIntValue(SecondaryStat.BowMasterMortalBlow) + 1;
                if (value > applier.effect.getX()) {
                    applyto.dispelEffect(致命箭);
                    applier.localstatups.clear();
                    applier.localstatups.put(SecondaryStat.IndieDamR, applier.effect.getY());
                    applier.localstatups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                } else {
                    applier.duration = 2100000000;
                    applier.localstatups.put(SecondaryStat.BowMasterMortalBlow, value);
                }
                return 1;
            }
            case 集中專注: {
                applier.localstatups.put(SecondaryStat.BowMasterConcentration, Math.min(applyto.getBuffedIntValue(SecondaryStat.BowMasterConcentration) + 1, 100 / applier.effect.getX()));
                applyto.send(EffectPacket.showBuffEffect(applyto, false, applier.effect.getSourceId(), applyto.getLevel(), 0, null));
                applyto.getMap().broadcastMessage(applyto, EffectPacket.showBuffEffect(applyto, true, applier.effect.getSourceId(), applyto.getLevel(), 0, null), false);
                return 1;
            }
            case 會心之眼: {
                MapleStatEffect effect;
                applier.buffz = 0;
                if ((effect = applyfrom.getSkillEffect(會心之眼_無視防禦)) != null) {
                    applier.buffz = effect.getIndieIgnoreMobpdpR();
                }
                if ((effect = applyfrom.getSkillEffect(會心之眼_爆擊提升)) != null) {
                    applier.localstatups.put(SecondaryStat.SharpEyes, applier.localstatups.get(SecondaryStat.SharpEyes) + (effect.getX() << 8));
                }
                return 1;
            }
            case 破甲射擊: {
                applier.b3 = true;
                applier.duration = 3000;
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
                applyto.dispelEffect(Config.constants.skills.冒險家_技能群組.箭神.傳說冒險);
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
            case 殘影之矢: {
                if (!applier.primary) {
                    return 0;
                }
                int duration = applier.effect.calcBuffDuration(applier.effect.getS() * 1000, applyfrom);
                applyfrom.getTempValues().put("殘影之矢時間", new Pair(System.currentTimeMillis(), duration));
                applyfrom.send(MaplePacketCreator.InhumanSpeedAttackeRequest(applyfrom.getId(), (byte) 1, duration));
                return 1;
            }
            case 殘影幻象: {
                applier.buffz = 0;
                if (!applier.primary) {
                    SecondaryStatValueHolder mbsvh;
                    if ((mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.ShadowShield)) == null || mbsvh.z >= applier.effect.getX()) {
                        return 0;
                    }
                    applier.buffz = Math.min(applier.effect.getX(), mbsvh.z + 1);
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 魔幻箭筒_2轉:
            case 魔幻箭筒_4轉:
            case 終極之弓:
            case 進階終極攻擊:
            case 召喚鳳凰:
            case 箭座_攻擊:
            case 閃光幻象_1:
            case 殘影幻象_1:
            case 殘影之矢:
            case 殘影之矢_1:
                return -1;
        }

        final MapleForceFactory mff = MapleForceFactory.getInstance();
        SecondaryStatValueHolder mbsvh;
        if ((mbsvh = applyfrom.getBuffStatValueHolder(SecondaryStat.QuiverCatridge)) != null && mbsvh.effect != null && mbsvh.value == 1) {
            MapleStatEffect effect;
            int prop;
            if (applyfrom.getSkillEffect(魔幻箭筒_4轉) == null) {
                prop = mbsvh.effect.getU();
                effect = mbsvh.effect;
            } else {
                effect = applyfrom.getSkillEffect(無限箭筒);
                prop = effect.getU();
            }
            if (effect != null && Randomizer.isSuccess(prop)) {
                applyfrom.getMap().broadcastMessage(applyfrom, ForcePacket.forceAtomCreate(mff.getMapleForce(applyfrom, effect, 0, applyto.getObjectId(), null, null)), true);
            }
        }
        if (applyto != null && applyfrom.getBuffStatValueHolder(SecondaryStat.IndieDamR, 箭雨) != null && applyfrom.getCheatTracker().canNextBonusAttack(5000)) {
            applyfrom.getSkillEffect(箭雨_1).applyTo(applyfrom);
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.ai.skillId == 箭座_攻擊 || applier.ai.skillId == 終極之弓 || applier.ai.skillId == 進階終極攻擊 || applier.ai.attackType == AttackInfo.AttackType.SummonedAttack) {
            return -1;
        }
        final MapleForceFactory mff = MapleForceFactory.getInstance();
        SecondaryStatValueHolder mbsvh;
        MapleStatEffect effect;
        if (applier.ai.mobAttackInfo.size() > 0) {
            if ((mbsvh = player.getBuffStatValueHolder(SecondaryStat.QuiverCatridge)) != null) {
                if (mbsvh.value == 2) {
                    effect = player.getSkillEffect(無限箭筒);
                    if (effect != null && Randomizer.isSuccess(effect.getW())) {
                        player.addHPMP(player.getStat().getCurrentMaxHP() * effect.getX() / 100, 0, false);
                        player.send(EffectPacket.showBlessOfDarkness(-1, 魔幻箭筒));
                        player.getMap().broadcastMessage(player, EffectPacket.showBlessOfDarkness(player.getId(), 魔幻箭筒), false);
                    }
                }
            } else if ((effect = player.getSkillEffect(魔幻箭筒)) != null) {
                effect.unprimaryPassiveApplyTo(player);
            }
        }
        if (applier.ai.attackType == AttackInfo.AttackType.ShootAttack && applier.ai.mobAttackInfo.size() > 0 && (effect = player.getSkillEffect(致命箭)) != null) {
            final int value = player.getBuffedIntValue(SecondaryStat.BowMasterMortalBlow) + 1;
            if (value >= effect.getX()) {
                for (int i = 0; i < Math.min(applier.ai.mobAttackInfo.size(), 2); i++) {
                    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                    mplew.writeShort(OutHeader.LP_MobSpecialEffectBySkill.getValue());
                    mplew.writeInt(applier.ai.mobAttackInfo.get(i).mobId);
                    mplew.writeInt(effect.getSourceId());
                    mplew.writeInt(player.getId());
                    mplew.writeShort(0);
                    player.getMap().broadcastMessage(player, mplew.getPacket(), true);
                }
            }
            effect.unprimaryPassiveApplyTo(player);
        }
        if (applier.ai.mobAttackInfo.size() > 0 && (effect = player.getSkillEffect(集中專注)) != null) {
            effect.unprimaryPassiveApplyTo(player);
        }
        if (applier.ai.attackType == AttackInfo.AttackType.ShootAttack && applier.ai.mobAttackInfo.size() > 0 && (mbsvh = player.getBuffStatValueHolder(閃光幻象)) != null) {
            boolean apply = true;
            switch (SkillConstants.getLinkedAttackSkill(applier.ai.skillId)) {
                case 箭座:
                case 暴風神射:
                case 殘影幻象:
                case 殘影之矢:
                case 焰箭齊發:
                case 箭雨:
                    int value = (int) player.getTempValues().getOrDefault("閃光幻象暴風技能累計", 0) + 1;
                    if (value >= mbsvh.effect.getU2()) {
                        value = 0;
                    } else {
                        apply = false;
                    }
                    player.getTempValues().put("閃光幻象暴風技能累計", value);
                    break;
            }
            if (apply) {
                mbsvh.effect.unprimaryPassiveApplyTo(player);
            }
        }
        if (applier.ai.attackType == AttackInfo.AttackType.ShootAttack && applier.ai.mobAttackInfo.size() > 0 && (effect = player.getSkillEffect(破甲射擊)) != null) {
            MapleCoolDownValueHolder mcvh = player.getSkillCooldowns().get(破甲射擊);
            if (mcvh == null || mcvh.getLeftTime() <= 0) {
                player.registerSkillCooldown(破甲射擊, effect.getY() * 1000, true);
                player.send(EffectPacket.showBuffEffect(player, false, applier.effect.getSourceId(), player.getLevel(), 1, null));
                player.getMap().broadcastMessage(player, EffectPacket.showBuffEffect(player, true, applier.effect.getSourceId(), player.getLevel(), 1, null), false);
            } else if (mcvh.getLeftTime() > 1000) {
                player.registerSkillCooldown(破甲射擊, Math.max(1000, mcvh.getLeftTime() - (effect.getW() * 1000)), true);
            }
        }
        if (applier.ai.attackType == AttackInfo.AttackType.ShootAttack && SkillConstants.getLinkedAttackSkill(applier.ai.skillId) != 殘影之矢 && applier.ai.mobAttackInfo.size() > 0) {
            if ((mbsvh = player.getBuffStatValueHolder(SecondaryStat.TempSecondaryStat, 殘影之矢)) != null) {
                Object timeDat = player.getTempValues().getOrDefault("殘影之矢時間", null);
                Pair<Long, Integer> timeInfo;
                long timeNow = System.currentTimeMillis();
                int duration = mbsvh.effect.calcBuffDuration(mbsvh.effect.getS() * 1000, player);
                if (timeDat == null) {
                    timeInfo = new Pair(timeNow - 1000, duration);
                } else {
                    timeInfo = (Pair<Long, Integer>) timeDat;
                    duration += timeInfo.getRight();
                }
                if (timeNow - (long) timeDat >= 1000) {
                    timeInfo.left = timeNow;
                    duration -= Math.max(0, timeNow - ((long) timeDat));
                    player.send(MaplePacketCreator.InhumanSpeedAttackeRequest(player.getId(), (byte) 1, duration));
                }
                timeInfo.right = duration;
                player.getTempValues().put("殘影之矢時間", timeInfo);
            } else if (player.isSkillCooling(殘影之矢) && (effect = player.getSkillEffect(殘影之矢)) != null) {
                int value = (int) player.getTempValues().getOrDefault("殘影之矢技能累計", 0) + 1;
                if (value >= effect.getU()) {
                    value = 0;
                    player.send(EffectPacket.showBuffEffect(player, false, 殘影之矢_1, player.getLevel(), 1, null));
                    player.getMap().broadcastMessage(player, EffectPacket.showBuffEffect(player, true, 殘影之矢_1, player.getLevel(), 1, null), false);
                    player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, SkillFactory.getSkill(殘影之矢_1).getEffect(effect.getLevel()), 0, 0, Collections.emptyList(), player.getPosition())), true);
                }
                player.getTempValues().put("殘影之矢技能累計", value);
            }
        }

        final MapleStatEffect effecForBuffStat6;
        if ((effecForBuffStat6 = player.getEffectForBuffStat(SecondaryStat.QuiverFullBurst)) != null && player.getCheatTracker().canNextAllRocket(焰箭齊發_1, 2500)) {
            final Iterator<MapleMapObject> iterator2 = player.getMap().getMapObjectsInRange(player.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER)).iterator();
            for (int n8 = 0; n8 < effecForBuffStat6.getMobCount() && iterator2.hasNext(); ++n8) {
                player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, effecForBuffStat6, 0, 0, null, iterator2.next().getPosition())), true);
            }
        }
        if (applier.ai.attackType == AttackInfo.AttackType.ShootAttack && applier.ai.mobAttackInfo.size() > 0 && (mbsvh = player.getBuffStatValueHolder(SecondaryStat.ShadowShield)) != null && mbsvh.effect != null && mbsvh.z > 0 && !player.isSkillCooling(殘影幻象_1)) {
            effect = SkillFactory.getSkill(殘影幻象_1).getEffect(mbsvh.effect.getLevel());
            if (effect != null) {
                List<MapleMapObject> objs = player.getMap().getMonstersInRect(effect.calculateBoundingBox(player.getPosition(), player.isFacingLeft()));
                if (!objs.isEmpty()) {
                    List<Integer> toMobOid = new LinkedList<>();
                    for (int i = 0; i < effect.getBulletCount(); i++) {
                        toMobOid.add(objs.get(i % objs.size()).getObjectId());
                    }
                    Point p = new Point(player.getPosition());
                    player.registerSkillCooldown(殘影幻象_1, (int) (mbsvh.effect.getInfoD().get(MapleStatInfo.t) * 1000), false);
                    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.LP_UserEffectLocal);
                    mplew.write(EffectOpcode.UserEffect_SkillSpecial.getValue());
                    mplew.writeInt(mbsvh.effect.getSourceId());
                    mplew.writeInt(mbsvh.effect.getLevel());
                    mplew.writePosInt(p);
                    player.send(mplew.getPacket());
                    p.y -= 100;
                    player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, effect, 0, 0, toMobOid, p)), true);
                }
            }
        }
        return 1;
    }

    @Override
    public int onAfterCancelEffect(MapleCharacter player, SkillClassApplier applier) {
        if (!applier.overwrite) {
            if (applier.effect.getSourceId() == 殘影之矢) {
                player.getTempValues().remove("殘影之矢時間");
                player.send(MaplePacketCreator.InhumanSpeedAttackeRequest(player.getId(), (byte) 0, 0));
            }
        }
        return -1;
    }
}
