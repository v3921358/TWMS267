package Client.skills.handler.冒險家.法師類別;

import Client.*;
import Client.force.MapleForceFactory;
import Client.skills.ExtraSkill;
import Client.skills.Skill;
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
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.神射手;
import Config.constants.skills.重砲指揮官;
import Config.constants.skills.開拓者;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.Element;
import Net.server.life.MapleMonster;
import Net.server.life.MobSkill;
import Net.server.maps.ForceAtomObject;
import Net.server.maps.MapleAffectedArea;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Opcode.Opcode.EffectOpcode;
import Packet.*;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

import static Config.constants.skills.冒險家_技能群組.type_法師.火毒.*;

public class 火毒大魔導士 extends AbstractSkillHandler {

    public 火毒大魔導士() {
        jobs = new MapleJob[]{
                MapleJob.火毒巫師,
                MapleJob.火毒魔導士,
                MapleJob.火毒大魔導士
        };

        for (Field field : 火毒.class.getDeclaredFields()) {
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
            case HEXA_劇毒煉獄_II_延伸:
            case HEXA_煉獄之毒:
                return HEXA_劇毒煉獄;
            case HEXA_火焰之襲_IV:
            case HEXA_火焰之襲_III:
            case HEXA_火焰之襲_II:
                return HEXA_火焰之襲;
            case 燎原之火_MIST:
                return 燎原之火;
            case 劇毒領域_1:
                return 劇毒領域;
            case 火流星_墜落:
                return 火流星;
            case 劇毒新星_1:
                return 劇毒新星;
            case 劇毒連鎖_1:
            case 劇毒連鎖_2:
                return 劇毒連鎖;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 元素吸收:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.DotBasedBuff, 1);
                return 1;
            case 藍焰斬:
            case 毒霧:
            case 致命毒霧:
            case 劇毒新星:
            case 持續制裁者:
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 精神強化:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieMAD, effect.getInfo().get(MapleStatInfo.indieMad));
                return 1;
            case 燎原之火:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.WizardIgnite, 1);
                return 1;
            case 召喚火魔:
                effect.getInfo().put(MapleStatInfo.summonCount, 1);
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 劇毒領域:
                monsterStatus.put(MonsterStatus.Burned, 1);
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 元素適應_火毒:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.AntiMagicShell, effect.getY());
                return 1;
            case 瞬間移動精通:
                statups.put(SecondaryStat.TeleportMasteryOn, 1);
                monsterStatus.put(MonsterStatus.Stun, 1);
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 瞬間移動爆發:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.RpSiksin, 1);
                return 1;
            case 火焰之襲:
                effect.getInfo().put(MapleStatInfo.time, effect.getInfo().get(MapleStatInfo.time) * 2);
                monsterStatus.put(MonsterStatus.Stun, 1);
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 炙焰毒火:
                monsterStatus.put(MonsterStatus.IndieSlow, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.DodgeBodyAttack, 1);
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 魔力無限:
                effect.setHpR(effect.getInfo().get(MapleStatInfo.y) / 100.0);
                effect.setMpR(effect.getInfo().get(MapleStatInfo.y) / 100.0);
                statups.put(SecondaryStat.Stance, effect.getInfo().get(MapleStatInfo.prop));
                statups.put(SecondaryStat.Infinity, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 神秘狙擊:
                effect.getInfo().put(MapleStatInfo.time, 5000);
                statups.put(SecondaryStat.ArcaneAim, 1);
                return 1;
            case 傳說冒險:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 火靈結界:
                monsterStatus.put(MonsterStatus.Burned, 1);

                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.FireAura, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        switch (applier.effect.getSourceId()) {
            case 藍焰斬: {
                chr.getSpecialStat().gainFieldSkillCounter(藍焰斬);
                List<Integer> oids = IntStream.range(0, slea.readByte()).mapToObj(i -> slea.readInt()).toList();
                List<ForceAtomObject> createList = new ArrayList<>();
                for (int i = 0; i < applier.effect.getBulletCount(); i++) {
                    ForceAtomObject obj = new ForceAtomObject(chr.getSpecialStat().gainForceCounter(), 35, i, chr.getId(), 0, applier.effect.getSourceId());
                    obj.Idk3 = 1;
                    if (!oids.isEmpty()) {
                        obj.Target = oids.get(i % oids.size());
                    }
                    obj.CreateDelay = 480;
                    obj.EnableDelay = 720;
                    obj.Idk1 = 1;
                    obj.Expire = 5000;
                    obj.Position = new Point(0, 1);
                    obj.ObjPosition = new Point(chr.getPosition());
                    obj.ObjPosition.x += (int) (chr.getPosition().getX() + Randomizer.rand(-100, 100));
                    obj.ObjPosition.y += (int) (chr.getPosition().getY() + Randomizer.rand(-100, 100));
                    createList.add(obj);
                }
                if (!createList.isEmpty()) {
                    chr.getMap().broadcastMessage(AdelePacket.ForceAtomObject(chr.getId(), createList, 0), chr.getPosition());
                }
                chr.getTempValues().put("藍焰斬Count", createList.size());
                return 1;
            }
            case 持續制裁者: {
                List<Integer> oids = new ArrayList<>();
                for (MapleMapObject monster : chr.getMap().getMapObjectsInRange(chr.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                    oids.add(monster.getObjectId());
                    if (oids.size() >= applier.effect.getMobCount()) {
                        break;
                    }
                }
                chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(forceFactory.getMapleForce(chr, applier.effect, 0, oids)), true);
                return 1;
            }
            case 劇毒連鎖_1: {
                ForceAtomObject obj = new ForceAtomObject(chr.getSpecialStat().gainForceCounter(), 38, 0, chr.getId(), 0, applier.effect.getSourceId());
                Pair<Integer, Point> spawninfo = null;
                if (!applier.ai.skillSpawnInfo.isEmpty()) {
                    spawninfo = applier.ai.skillSpawnInfo.getFirst();
                }
                if (spawninfo != null) {
                    obj.Target = spawninfo.getLeft();
                }
                obj.Expire = 10000;
                obj.Position = new Point(0, 1);
                obj.ObjPosition = new Point(chr.getPosition().getLocation());
                obj.ObjPosition.y -= 43;
                obj.addX(chr.getSpecialStat().getFieldSkillCounter(劇毒連鎖));
                chr.getMap().broadcastMessage(AdelePacket.ForceAtomObject(chr.getId(), Collections.singletonList(obj), 0), chr.getPosition());
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyTo(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 元素適應_火毒) {
            applier.cooldown = 0;
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect instanceof MobSkill) {
            boolean isCriticalDebuff = false;
            for (SecondaryStat stat : applier.localstatups.keySet()) {
                if (stat.isCriticalDebuff()) {
                    isCriticalDebuff = true;
                    break;
                }
            }
            SecondaryStatValueHolder mbsvh;
            if (isCriticalDebuff && (mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.AntiMagicShell)) != null && mbsvh.value > 0) {
                int mpCon = applyto.getStat().getCurrentMaxHP() * mbsvh.effect.getX() / 100;
                if (applyto.getStat().getHp() < mpCon) {
                    return -1;
                }
                applyto.addMP(-mpCon, true);
                applyto.send(EffectPacket.showBlessOfDarkness(-1, mbsvh.effect.getSourceId()));
                applyto.getMap().broadcastMessage(applyto, EffectPacket.showBlessOfDarkness(applyto.getId(), mbsvh.effect.getSourceId()), false);
                mbsvh.value--;
                if (mbsvh.value > 0) {
                    applyto.send(BuffPacket.giveBuff(applyto, mbsvh.effect, Collections.singletonMap(SecondaryStat.AntiMagicShell, mbsvh.effect.getSourceId())));
                } else {
                    applyto.dispelEffect(SecondaryStat.AntiMagicShell);
                    applyto.registerSkillCooldown(mbsvh.effect, true);
                }
                return 0;
            }
            return -1;
        }
        switch (applier.effect.getSourceId()) {
            case 燎原之火: {
                if (applyto.getBuffedValue(SecondaryStat.WizardIgnite) != null) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                }
                return 1;
            }
            case 元素吸收: {
                int count = 0;
                for (MapleMapObject obj : applyfrom.getMap().getMapObjectsInRange(applyfrom.getPosition(), applier.effect.getRange(), Collections.singletonList(MapleMapObjectType.MONSTER))) {
                    if (((MapleMonster) obj).getEffectHolder(applyfrom.getId(), MonsterStatus.Burned) != null && ++count >= 5) {
                        break;
                    }
                }
                if (count <= 0) {
                    applyfrom.dispelEffect(元素吸收);
                    return 0;
                }
                applier.localstatups.put(SecondaryStat.DotBasedBuff, count);
                return 0;
            }
            case 瞬間移動精通: {
                applier.duration = 2100000000;
                return 1;
            }
            case 神秘狙擊: {
                if (applyto.getBuffedValue(SecondaryStat.ArcaneAim) != null) {
                    applier.localstatups.put(SecondaryStat.ArcaneAim, Math.min(applier.effect.getY(), applyto.getBuffedIntValue(SecondaryStat.ArcaneAim) + 1));
                }
                return 1;
            }
            case 劇毒連鎖: {
                applyfrom.getSpecialStat().gainFieldSkillCounter(劇毒連鎖);
                return -1;
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
                applyto.dispelEffect(夜使者.傳說冒險);
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
    public int onApplyMonsterEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 瞬間移動精通) {
            final EnumMap<MonsterStatus, MonsterEffectHolder> statups = new EnumMap<>(MonsterStatus.class);
            int prop = applier.effect.getSubProp();
            long currentTimeMillis = System.currentTimeMillis();
            if (Randomizer.isSuccess(prop)) {
                statups.put(MonsterStatus.Stun, new MonsterEffectHolder(applyfrom.getId(), 1, currentTimeMillis, applier.effect.calcMobDebuffDuration(applier.effect.getDuration(), applyfrom), applier.effect));
            }
            prop = applier.effect.getProp();
            if (Randomizer.isSuccess(prop)) {
                MonsterEffectHolder holder = new MonsterEffectHolder(applyfrom.getId(), 1, currentTimeMillis, applier.effect.getMobDebuffDuration(applyfrom), applier.effect);
                applier.effect.setDotData(applyfrom, holder);
                statups.put(MonsterStatus.Burned, holder);
            }
            if (!statups.isEmpty()) {
                applyto.registerEffect(statups);
                Map<MonsterStatus, Integer> writeStatups = new LinkedHashMap<>();
                for (MonsterStatus stat : statups.keySet()) {
                    writeStatups.put(stat, applier.effect.getSourceId());
                }
                applyfrom.getMap().broadcastMessage(MobPacket.mobStatSet(applyto, writeStatups), applyto.getPosition());
            }
            return 0;
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        MapleStatEffect effect;
        if ((effect = applyfrom.getSkillEffect(魔力吸收)) != null && effect.makeChanceResult(applyfrom)) {
            int rate;
            if (!applyto.getStats().isBoss()) {
                rate = effect.getX();
            } else {
                rate = effect.getY();
            }
            int absorbMp = Math.min(applyto.getMobMaxMp() * rate / 100, applyto.getMp());
            if (absorbMp > 0) {
                applyto.setMp(applyto.getMp() - absorbMp);
                applyfrom.addMP(absorbMp);
                applyfrom.send(EffectPacket.encodeUserEffectLocal(effect.getSourceId(), EffectOpcode.UserEffect_SkillUse, applyfrom.getLevel(), effect.getLevel()));
                applyfrom.getMap().broadcastMessage(applyfrom, EffectPacket.onUserEffectRemote(applyfrom, effect.getSourceId(), EffectOpcode.UserEffect_SkillUse, applyfrom.getLevel(), effect.getLevel()), false);
            }
        }
        final MapleStatEffect effecForBuffStat;
        if (applier.effect != null && applier.effect.getSourceId() != 燎原之火_MIST && (effecForBuffStat = applyfrom.getEffectForBuffStat(SecondaryStat.WizardIgnite)) != null && effecForBuffStat.makeChanceResult(applyfrom)) {
            final Skill jk = SkillFactory.getSkill(applier.effect.getSourceId());
            final MapleStatEffect skillEffect;
            if ((skillEffect = applyfrom.getSkillEffect(燎原之火_MIST)) != null && jk.getElement() == Element.火) {
                skillEffect.applyAffectedArea(applyfrom, applyto.getPosition());
            }
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.effect != null) {
            if (applier.effect.getSourceId() == 致命毒霧) {
                applier.effect.applyAffectedArea(player, new Point(applier.ai.forcedXSh, applier.ai.forcedYSh));
            }

            if (applier.effect.getSourceId() == 劇毒領域_1 || applier.effect.getSkill().getElement() == Element.火) {
                Rectangle rect;
                if (applier.effect.getSourceId() == 劇毒領域_1) {
                    rect = applier.ai.rect;
                } else {
                    rect = applier.effect.calculateBoundingBox(new Point(applier.ai.forcedX, applier.ai.forcedY), (applier.ai.direction & 0x80) != 0);
                }
                for (MapleAffectedArea area : player.getMap().getAllAffectedAreasThreadsafe()) {
                    if (area.getSkillID() != 劇毒領域) {
                        continue;
                    }
                    Rectangle aRect = area.getBounds();
                    if (rect.contains(aRect) || aRect.contains(rect) || aRect.equals(rect) || rect.intersects(aRect)) {
                        ExtraSkill eskill = new ExtraSkill(劇毒領域_1, area.getPosition());
                        eskill.TriggerSkillID = applier.effect.getSourceId();
                        eskill.Delay = 240;
                        eskill.Value = 1;
                        eskill.TargetOID = area.getObjectId();
                        player.send(MaplePacketCreator.RegisterExtraSkill(劇毒領域, Collections.singletonList(eskill)));
                    }
                }
            }

            if (applier.effect.getSourceId() == 地獄爆發) {
                if (applier.ai.mobAttackInfo.size() > 0) {
                    player.cancelSkillCooldown(炙焰毒火);
                }
                if (applier.ai.mists != null) {
                    for (int id : applier.ai.mists) {
                        MapleAffectedArea mist = player.getMap().getAffectedAreaByOid(id);
                        if (mist != null && mist.getSkillID() == 致命毒霧 && mist.getOwnerId() == player.getId()) {
                            mist.cancel();
                            player.getMap().disappearMapObject(mist);
                        }
                    }
                }
            }

            if (applier.effect.getSourceId() == 藍焰斬 && player.getTempValues().containsKey("藍焰斬Count")) {
                int nCount = (int) player.getTempValues().get("藍焰斬Count");
                if (nCount < applier.effect.getX()) {
                    List<MapleMapObject> mobs = player.getMap().getMonstersInRect(applier.effect.calculateBoundingBox(applier.ai.skillposition, false));
                    if (!mobs.isEmpty()) {
                        List<ForceAtomObject> createList = new ArrayList<>();
                        int bulletCount = Math.min(2, applier.effect.getX() - nCount);
                        for (int i = 0; i < bulletCount; i++) {
                            ForceAtomObject obj = new ForceAtomObject(player.getSpecialStat().gainForceCounter(), 35, i, player.getId(), 0, applier.effect.getSourceId());
                            obj.Idk3 = 1;
                            obj.CreateDelay = 180;
                            obj.EnableDelay = 480;
                            obj.Idk1 = 1;
                            obj.Expire = 5000;
                            obj.Position = new Point(0, 1);
                            obj.addX(player.getSpecialStat().getFieldSkillCounter(藍焰斬));
                            MapleMonster mob = (MapleMonster) mobs.get(i % mobs.size());
                            if (mob != null) {
                                obj.Target = mob.getObjectId();
                            }
                            obj.ObjPosition = new Point(applier.ai.skillposition);
                            obj.ObjPosition.x += Randomizer.rand(-100, 100);
                            obj.ObjPosition.y += Randomizer.rand(-100, 100);
                            createList.add(obj);
                        }
                        if (!createList.isEmpty()) {
                            player.getMap().broadcastMessage(AdelePacket.ForceAtomObject(player.getId(), createList, 0), player.getPosition());
                        }
                        player.getTempValues().put("藍焰斬Count", nCount + createList.size());
                    }
                }
            }
        }
        return 1;
    }
}
