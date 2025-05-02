package Client.skills.handler.英雄團;

import Client.*;
import Client.force.MapleForceFactory;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.HexaSKILL;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.SkillConstants;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Config.constants.skills.隱月.*;

public class 隱月 extends AbstractSkillHandler {

    public 隱月() {
        jobs = new MapleJob[]{
                MapleJob.隱月,
                MapleJob.隱月1轉,
                MapleJob.隱月2轉,
                MapleJob.隱月3轉,
                MapleJob.隱月4轉
        };

        for (Field field : Config.constants.skills.隱月.class.getDeclaredFields()) {
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
        int[] ss = {縮地, 精靈凝聚1式, 精靈親和, 英雄的回響};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == 英雄的回響) {
                continue;
            }
            skil = SkillFactory.getSkill(i);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(i, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        int[] fixskills = {/*隱月.巨型衝擊, */重拳};
        for (int f : fixskills) {
            skil = SkillFactory.getSkill(f);
            if (chr.getJob() >= f / 10000 && skil != null && chr.getSkillLevel(skil) <= 0 && chr.getMasterLevel(skil) <= 0) {
                applier.skillMap.put(f, new SkillEntry(0, skil.getMasterLevel() == 0 ? skil.getMaxLevel() : skil.getMasterLevel(), SkillFactory.getDefaultSExpiry(skil)));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 25141501:
            case 25141502:
            case 25141503:
            case 25141504:
            case 25141505:
            case 25141506:
                return HexaSKILL.狐神附體;
            case 25141000:
                return 鬼斬;
            case HexaSKILL.強化精靈聚集:
                return 400051010;
            case HexaSKILL.強化鬼武陣:
                return 400051022;
            case HexaSKILL.強化真_鬼斬:
                return 400051043;
            case HexaSKILL.強化破碎連拳:
                return 400051078;
            case 拳擊:
                return 巨型衝擊;
            case 跳躍:
                return 靈巧跳躍;
            case 小狐仙精通_1:
                return 小狐仙精通;
            case 靈魂帳幕_1:
            case 銷魂屏障:
                return 靈魂帳幕;
            case 換魂_1:
                return 換魂;
            case 火狐精通_1:
                return 火狐精通;
            case 爆流拳_1:
            case 爆流拳_2:
            case 爆流拳_3:
                return 爆流拳;
            case 衝擊拳_1:
            case 衝擊拳_2:
            case 衝擊拳_衝擊波:
                return 衝擊拳;
            case 重拳_2:
                return 重拳;
            case 波浪拳波動:
            case 巨浪打擊_2:
                return 巨浪打擊;
            case 精靈凝聚極大化:
                return 精靈降臨_極限;
            case 精靈的化身_1:
                return 精靈的化身;
            case 鬼武陣_1:
                return 鬼武陣;
            case 破碎連拳_1:
                return 破碎連拳;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 英雄的回響:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
                statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
                return 1;
            case 換魂:
                statups.put(SecondaryStat.ReviveOnce, effect.getInfo().get(MapleStatInfo.x));
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                effect.setOverTime(true);
                return 1;
            case 死裡逃生:
            case 死裡逃生_傳授:
                effect.setOverTime(true);
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.PreReviveOnce, effect.getInfo().get(MapleStatInfo.prop));
                return 1;
            case 小狐仙:
                effect.getInfo().put(MapleStatInfo.time, 2100000000); /* 值 2100000000 疑似永久開關技能 */
                statups.put(SecondaryStat.HiddenPossession, 1);
                return 1;
            case 靈魂結界:
                statups.put(SecondaryStat.SpiritGuard, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 波浪拳波動:
                monsterStatus.put(MonsterStatus.Speed, -effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 靈魂分離術:
                monsterStatus.put(MonsterStatus.SeperateSoulP, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 弱化:
                monsterStatus.put(MonsterStatus.ACC, -effect.getInfo().get(MapleStatInfo.y));
                monsterStatus.put(MonsterStatus.EVA, -effect.getInfo().get(MapleStatInfo.z));
                monsterStatus.put(MonsterStatus.AddDamSkill2, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 死魂烙印:
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 英雄誓約:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 精靈降臨_極限:
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.IndieBooster, effect.getInfo().get(MapleStatInfo.indieBooster));
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                statups.put(SecondaryStat.IndieBDR, effect.getInfo().get(MapleStatInfo.indieBDR));
                statups.put(SecondaryStat.IndieIgnoreMobpdpR, effect.getInfo().get(MapleStatInfo.indieIgnoreMobpdpR));
                statups.put(SecondaryStat.HiddenHyperLinkMaximization, 1);
                return 1;
            case 精靈聚集:
                statups.put(SecondaryStat.TempSecondaryStat, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 鬼武陣:
                statups.put(SecondaryStat.IndieBuffIcon, effect.getInfo().get(MapleStatInfo.x));
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        switch (applier.effect.getSourceId()) {
            case 小狐仙精通: {
                List<Integer> oids = IntStream.range(0, slea.readByte()).mapToObj(i -> slea.readInt()).collect(Collectors.toList());
                chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(forceFactory.getMapleForce(chr, chr.getSkillEffect(小狐仙精通_1), 0, oids)), true);
                return 1;
            }
            case 火狐精通: {
                List<Integer> oids = IntStream.range(0, slea.readByte()).mapToObj(i -> slea.readInt()).collect(Collectors.toList());
                chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(forceFactory.getMapleForce(chr, chr.getSkillEffect(火狐精通_1), 0, oids)), true);
                return 1;
            }
            case 精靈聚集: {
                chr.clearCooldown(true);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 小狐仙: {
                if (applyto.getBuffedValue(SecondaryStat.HiddenPossession) != null) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                }
                return 1;
            }
            case 靈魂結界: {
                final SecondaryStatValueHolder mbsvh;
                if ((mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.SpiritGuard)) == null) {
                    return 1;
                }
                int value = mbsvh.value;
                value = applier.passive ? Math.max(0, value - 1) : Math.min(applier.effect.getX(), value + 1);
                if (value > 0) {
                    applier.duration = mbsvh.getLeftTime();
                    applier.localstatups.put(SecondaryStat.SpiritGuard, value);
                    return 1;
                }
                applier.overwrite = false;
                applier.localstatups.clear();
                return 1;
            }
            case 英雄誓約: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(Config.constants.skills.狂狼勇士.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.龍魔導士.英雄歐尼斯);
                applyto.dispelEffect(Config.constants.skills.夜光.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.精靈遊俠.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.幻影俠盜.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.隱月.英雄誓約);
                return 1;
            }
            case 鬼武陣_1: {
                applier.cancelEffect = false;
                applier.b7 = false;
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        final MapleStatEffect skillEffect16 = applyfrom.getSkillEffect(弱化);
        if (applier.totalDamage > 0L && skillEffect16 != null) {
            skillEffect16.applyMonsterEffect(applyfrom, applyto, skillEffect16.getMobDebuffDuration(applyfrom));
        }
        if (applier.effect != null && applier.effect.getSourceId() == 靈魂分離術 && applyto.isAlive() && applyto.getStats().isMobile() && applyto.getEffectHolder(MonsterStatus.SeperateSoulP) == null && applyto.getEffectHolder(MonsterStatus.SeperateSoulC) == null) {
            final MapleMonster monster = MapleLifeFactory.getMonster(applyto.getId());
            assert monster != null;
            monster.setHp(applyto.getHp());
            monster.registerKill(applier.effect.getMobDebuffDuration(applyfrom));
            final MonsterEffectHolder meh = new MonsterEffectHolder(applyfrom.getId(), applier.effect.getX(), System.currentTimeMillis(), applier.effect.getMobDebuffDuration(applyfrom), applier.effect);
            meh.moboid = applyto.getObjectId();
            monster.getEffects().computeIfAbsent(MonsterStatus.SeperateSoulC, k -> new LinkedList<>()).add(meh);
            monster.setSeperateSoulSrcOID(applyto.getObjectId());
            monster.setSoul(true);
            monster.setPosition(applyto.getPosition());
            applyfrom.getMap().spawnMonster(monster, -1, false);
            applyto.setSeperateSoulSrcOID(monster.getObjectId());
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        MapleStatEffect eff;
        if ((eff = player.getEffectForBuffStat(SecondaryStat.TempSecondaryStat)) != null && player.getCheatTracker().canNextElementalFocus()) {
            player.getClient().announce(MaplePacketCreator.RegisterElementalFocus(SkillConstants.hn()));
            player.getClient().announce(MaplePacketCreator.UserElementalFocusResult(player.getId(), eff.getSourceId()));
        }
        final MapleStatEffect effecForBuffStat11 = player.getEffectForBuffStat(SecondaryStat.HiddenPossession);
        if (applier.totalDamage > 0L && effecForBuffStat11 != null) {
            final MapleForceFactory mff = MapleForceFactory.getInstance();
            List<MapleMapObject> mobs;
            if (player.getSkillEffect(小狐仙精通) != null) {
                if (player.getSkillEffect(小狐仙精通).makeChanceResult()) {
                    mobs = player.getMap().getMapObjectsInRect(effecForBuffStat11.calculateBoundingBox(player.getPosition(), player.isFacingLeft(), 100), Collections.singletonList(MapleMapObjectType.MONSTER));
                    final ArrayList<Integer> list2 = new ArrayList<>();
                    for (MapleMapObject mob : mobs) {
                        if (list2.size() >= player.getSkillEffect(小狐仙精通).getBulletCount()) {
                            break;
                        }
                        list2.add(mob.getObjectId());
                    }
                    if (!list2.isEmpty()) {
                        player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, player.getSkillEffect(小狐仙精通_1), 0, list2)), true);
                    }
                }
            }
            if (player.getSkillEffect(火狐精通) != null) {
                if (player.getSkillEffect(火狐精通).makeChanceResult()) {
                    mobs = player.getMap().getMapObjectsInRect(effecForBuffStat11.calculateBoundingBox(player.getPosition(), player.isFacingLeft(), 100), Collections.singletonList(MapleMapObjectType.MONSTER));
                    final ArrayList<Integer> list2 = new ArrayList<>();
                    for (MapleMapObject mob : mobs) {
                        if (list2.size() >= player.getSkillEffect(火狐精通).getBulletCount()) {
                            break;
                        }
                        list2.add(mob.getObjectId());
                    }
                    if (!list2.isEmpty()) {
                        player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, player.getSkillEffect(火狐精通_1), 0, list2)), true);
                    }
                }
            }
        }
        return 1;
    }
}
