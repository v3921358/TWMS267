package Client.skills.handler.冒險家.盜賊類別;

import Client.*;
import Client.force.MapleForceFactory;
import Client.skills.ExtraSkill;
import Client.skills.Skill;
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
import Config.constants.skills.冒險家_技能群組.影武者;
import Config.constants.skills.冒險家_技能群組.暗影神偷;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.神射手;
import Config.constants.skills.重砲指揮官;
import Config.constants.skills.開拓者;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.maps.ForceAtomObject;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Packet.AdelePacket;
import Packet.BuffPacket;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import tools.Randomizer;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static Config.constants.skills.冒險家_技能群組.夜使者.*;

public class 夜使者 extends AbstractSkillHandler {

    public 夜使者() {
        jobs = new MapleJob[]{
                MapleJob.刺客,
                MapleJob.暗殺者,
                MapleJob.夜使者
        };

        for (Field field : Config.constants.skills.冒險家_技能群組.夜使者.class.getDeclaredFields()) {
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
            case HEXA_禁咒之符_III:
            case HEXA_禁咒之符_II:
                return HEXA_禁咒之符;
            case HEXA_四飛閃_IV_延伸:
                return HEXA_四飛閃_IV;
            case 爆破鏢_1:
                return 爆破鏢;
            case 刺客刻印_飛鏢:
                return 刺客刻印;
            case 挑釁契約_1:
                return 挑釁契約;
            case 夜使者的標記:
                return 夜使者刻印;
            case 散式投擲_雙飛斬:
            case 散式投擲_三飛閃:
            case 散式投擲_四飛閃:
                return 散式投擲;
            case 飛閃起爆符_1:
            case 飛閃起爆符_2:
                return 飛閃起爆符;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 刺客刻印:
            case 夜使者刻印:
                effect.setOverTime(true);
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.NightLordMark, 1);

                effect.setDebuffTime(effect.getDotTime() * 1000);
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 影分身:
                statups.put(SecondaryStat.ShadowPartner, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 絕對領域:
                monsterStatus.put(MonsterStatus.IndiePDR, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.PAD, effect.getInfo().get(MapleStatInfo.z));
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 挑釁契約:
                effect.setDebuffTime(effect.getDuration());
                monsterStatus.put(MonsterStatus.Showdown, effect.getInfo().get(MapleStatInfo.x));

                effect.getInfo().put(MapleStatInfo.time, effect.getS2() * 1000);
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 傳說冒險:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 出血毒素:
                statups.clear();
                statups.put(SecondaryStat.BleedingToxin, 1);
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));

                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 散式投擲:
                statups.put(SecondaryStat.NightLord_SpreadThrow, 1);
                return 1;
            case 達克魯的秘傳:
                effect.getInfo().put(MapleStatInfo.bulletCount, effect.getInfo().get(MapleStatInfo.bulletCount) * 5 + effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 飛閃起爆符:
                statups.put(SecondaryStat.ThrowBlasting, effect.getInfo().get(MapleStatInfo.x));
                return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 刺客刻印: {
                if (applyto.getBuffedValue(SecondaryStat.NightLordMark) != null) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                }
                return 1;
            }
            case 飛閃起爆符: {
                applyto.cancelSkillCooldown(飛閃起爆符_1);
                return 1;
            }
            case 挑釁契約: {
                if (applyto.getBuffStatValueHolder(挑釁契約) != null) {
                    return 0;
                }
                Point p = applier.pos != null ? applier.pos : applyto.getPosition();
                List<MapleMapObject> mobs = applyto.getMap().getMonstersInRect(SkillFactory.getSkill(挑釁契約_1).getEffect(applier.effect.getLevel()).calculateBoundingBox(p));
                List<ForceAtomObject> createList = new ArrayList<>();
                for (int i = 0; i < applier.effect.getU(); i++) {
                    ForceAtomObject obj = new ForceAtomObject(applyto.getSpecialStat().gainForceCounter(), 33, i, applyto.getId(), Randomizer.rand(-360, 360), 挑釁契約_1);
                    obj.Idk3 = 1;
                    obj.CreateDelay = 810;
                    obj.EnableDelay = 930;
                    obj.Idk1 = 30;
                    obj.Expire = 2810;
                    obj.Position = new Point(0, 1);
                    obj.ObjPosition = new Point(p);
                    obj.ObjPosition.x += Randomizer.rand(-100, 100);
                    obj.ObjPosition.y += Randomizer.rand(-100, -20);
                    createList.add(obj);
                    if (!mobs.isEmpty()) {
                        MapleMonster mob = (MapleMonster) mobs.get(i % mobs.size());
                        if (mob != null) {
                            obj.Target = mob.getObjectId();
                        }
                    }
                }
                if (!createList.isEmpty()) {
                    applyto.getMap().broadcastMessage(AdelePacket.ForceAtomObject(applyto.getId(), createList, 0), applyto.getPosition());
                }
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
                applyto.dispelEffect(神射手.傳說冒險);
                applyto.dispelEffect(開拓者.傳說冒險);
                applyto.dispelEffect(暗影神偷.傳說冒險);
                applyto.dispelEffect(Config.constants.skills.冒險家_技能群組.夜使者.傳說冒險);
                applyto.dispelEffect(影武者.傳說冒險);
                applyto.dispelEffect(拳霸.傳說冒險);
                applyto.dispelEffect(槍神.傳說冒險);
                applyto.dispelEffect(重砲指揮官.傳說冒險);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applier.totalDamage > 0) {
            MapleStatEffect effect = applyfrom.getEffectForBuffStat(SecondaryStat.NightLordMark);
            final MapleStatEffect effect2;
            if (effect != null && (effect2 = applyfrom.getSkillEffect(夜使者刻印)) != null) {
                effect = effect2;
            }
            if (applier.effect == null || (applier.effect.getSourceId() != 刺客刻印_飛鏢 && applier.effect.getSourceId() != 夜使者的標記)) {
                MonsterEffectHolder meh;
                if (effect == null) {
                    meh = applyto.getEffectHolder(applyfrom.getId(), MonsterStatus.Burned, 刺客刻印);
                    if (meh == null) {
                        meh = applyto.getEffectHolder(applyfrom.getId(), MonsterStatus.Burned, 夜使者刻印);
                    }
                } else {
                    meh = applyto.getEffectHolder(applyfrom.getId(), MonsterStatus.Burned, effect.getSourceId());
                    if (meh == null) {
                        if (applyto.isAlive()) {
                            effect.applyMonsterEffect(applyfrom, applyto, effect.getMobDebuffDuration(applyfrom));
                        } else if (effect.makeChanceResult(applyfrom)) {
                            meh = new MonsterEffectHolder(effect.getSourceId(), effect.getLevel(), 1);
                            meh.effect = effect;
                        }
                    }
                }
                if (meh != null && meh.effect != null) {
                    final List<MapleMapObject> mobs = applyfrom.getMap().getMapObjectsInRange(applyto.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER));
                    List<Integer> list = mobs.stream().map(MapleMapObject::getObjectId).collect(Collectors.toList());
                    applyfrom.getMap().broadcastMessage(applyfrom, ForcePacket.forceAtomCreate(MapleForceFactory.getInstance().getMapleForce(applyfrom, meh.effect, applyto.getObjectId(), list, applyto.getPosition())), true);
                    applyto.removeEffect(applyfrom.getId(), meh.sourceID);
                }
            }
        }
        MapleStatEffect effect;
        if (containsJob(applyfrom.getJobWithSub()) && applier.totalDamage > 0L && (effect = applyfrom.getSkillEffect(飛毒殺)) != null) {
            final MapleStatEffect skillEffect8;
            if ((skillEffect8 = applyfrom.getSkillEffect(致命飛毒殺)) != null) {
                effect = skillEffect8;
            }
            effect.applyMonsterEffect(applyfrom, applyto, effect.getMobDebuffDuration(applyfrom));
        }
        if (applier.totalDamage > 0L && applyto.isAlive() && (effect = applyfrom.getEffectForBuffStat(SecondaryStat.BleedingToxin)) != null) {
            effect.applyMonsterEffect(applyfrom, applyto, effect.getMobDebuffDuration(applyfrom));
        }
        return -1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.totalDamage > 0L) {
            Skill skill = SkillFactory.getSkill(飛閃起爆符);
            if (applier.effect != null && skill != null && player.getSkillLevel(skill) > 0) {
                int add_skillId = 0;
                int cooldownSkillId = 0;
                int add_skillValue = 0;
                for (int nSkill : skill.getSkillList()) {
                    if (nSkill == applier.effect.getSourceId()) {
                        add_skillId = 飛閃起爆符_1;
                        cooldownSkillId = 飛閃起爆符_1;
                        add_skillValue = 1;
                        break;
                    }
                }
                int skillLevel;
                MapleStatEffect effect;
                if (add_skillId != 0 && (skillLevel = SkillConstants.getLinkedAttackSkill(飛閃起爆符)) > 0 && (skill = SkillFactory.getSkill(飛閃起爆符)) != null && (effect = skill.getEffect(player.getSkillLevel(skillLevel))) != null && !player.isSkillCooling(cooldownSkillId)) {
                    SecondaryStatValueHolder holder;
                    if ((holder = player.getBuffStatValueHolder(SecondaryStat.ThrowBlasting)) == null || holder.value <= 0) {
                        player.registerSkillCooldown(cooldownSkillId, effect.getSubTime(), true);
                    } else {
                        add_skillId = 飛閃起爆符_2;
                        add_skillValue = Math.max(2, Math.min(4, applier.ai.mobCount));
                        holder.value -= add_skillValue;
                        if (holder.value <= 0) {
                            player.dispelBuff(飛閃起爆符);
                        } else {
                            player.send(BuffPacket.giveBuff(player, holder.effect, Collections.singletonMap(SecondaryStat.ThrowBlasting, holder.sourceID)));
                        }
                    }
                    ExtraSkill eskill = new ExtraSkill(add_skillId, new Point(applier.ai.mobAttackInfo.get(0).hitX, applier.ai.mobAttackInfo.get(0).hitY));
                    eskill.Value = add_skillValue;
                    eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                    player.send(MaplePacketCreator.RegisterExtraSkill(飛閃起爆符, Collections.singletonList(eskill)));
                }
            }
        }
        return -1;
    }
}
