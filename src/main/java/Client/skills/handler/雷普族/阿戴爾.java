package Client.skills.handler.雷普族;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.maps.ForceAtomObject;
import Net.server.maps.MapleSummon;
import Packet.MaplePacketCreator;
import Packet.SummonPacket;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Config.constants.skills.阿戴爾.*;

public class 阿戴爾 extends AbstractSkillHandler {

    public 阿戴爾() {
        jobs = new MapleJob[]{
                MapleJob.阿戴爾,
                MapleJob.阿戴爾1轉,
                MapleJob.阿戴爾2轉,
                MapleJob.阿戴爾3轉,
                MapleJob.阿戴爾4轉
        };

        for (Field field : Config.constants.skills.阿戴爾.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int baseSkills(MapleCharacter chr, SkillClassApplier applier) {
        int level = chr.getLevel();
        Skill skil;
        final int[] ss = {再訪, 魔法迴路, 信念, 獨門咒語};
        for (int i : ss) {
            if ((level < 200 && i == 獨門咒語) || (level < 20 && i == 信念)) {
                continue;
            }
            skil = SkillFactory.getSkill(i);
            int skillLv = 1;
            if (i == 信念) {
                skillLv = (int) Math.floor(Math.min(level, 200) / 20);
            }
            if (skil != null && chr.getSkillLevel(skil) < skillLv) {
                applier.skillMap.put(i, new SkillEntry(skillLv, skil.getMaxMasterLevel(), -1));
            }
        }
        int[] fixskills = {懸浮};
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
            case HEXA_無限劍製_延伸:
                return HEXA_無限劍製;
            case HEXA_切割_VI_延伸:
            case HEXA_切割_VI:
                return 切割;
            case HEXA_奇蹟_VI:
                return 奇蹟;
            case HEXA_碎片_VI:
                return 碎片;
            case 毀滅_1:
            case 毀滅_2:
                return 毀滅;
            case 魔劍共鳴_1:
            case 魔劍共鳴_2:
                return 魔劍共鳴;
            case 創造_2:
                return 創造;
            case 跳躍_1:
                return 跳躍;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 獨門咒語:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
                statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
                return 1;
            case 狂風:
                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 推進器:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 雷普的勇士:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 創造:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.LWCreation, 1);
                return 1;
            case 奇蹟:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.LWWonder, 1);
                return 1;
            case 懸浮:
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                statups.put(SecondaryStat.NewFlying, 1);
                return 1;
            case 乙太:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.LWSwordGauge, 0);
                return 1;
            case 魔劍共鳴_2:
                statups.put(SecondaryStat.IndieIgnoreMobpdpR, effect.getInfo().get(MapleStatInfo.y));
                statups.put(SecondaryStat.LWResonanceBuff, 1);
                return 1;
            case 高潔精神:
                statups.put(SecondaryStat.LefWarriorNobility, effect.getInfo().get(MapleStatInfo.x));
                return 1;
           case 死亡標記:
               effect.getInfo().put(MapleStatInfo.time, 2100000000);
               statups.put(SecondaryStat.Curse, 1);
               return 1;
            case 乙太結晶:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                return 1;
            case 護堤:
                effect.getInfo().put(MapleStatInfo.time, effect.getInfo().get(MapleStatInfo.q) * 1000);
                statups.put(SecondaryStat.IndieApplySuperStance, 1);
                statups.put(SecondaryStat.IndieAllHitDamR, -effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                statups.put(SecondaryStat.AntiMagicShell, 1);
                statups.put(SecondaryStat.KeyDownEnable, 1);
                statups.put(SecondaryStat.LWDike, 1);
                return 1;
            case 無限:
                effect.getInfo().put(MapleStatInfo.time, 4000);
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
            case 復原:
                statups.put(SecondaryStat.IndieDamR, effect.getY());
                statups.put(SecondaryStat.LWRestore, 1);
                return 1;
            case 乙太風暴:
                statups.put(SecondaryStat.DevilishPower, effect.getLevel());
                return 1;
            case 神之種族:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 魔力爆裂:
                effect.getInfo().put(MapleStatInfo.time, effect.getZ());
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 魔劍共鳴_1:
            case 魔劍共鳴_2:
            case 魔劍共鳴: {
                int objectId = slea.readInt();
                MapleSummon summon = chr.getMap().getSummonByOid(objectId);
                if (summon != null && summon.getOwnerId() == chr.getId()) {
                    chr.getMap().broadcastMessage(SummonPacket.removeSummon(summon, false));
                    chr.getMap().removeMapObject(summon);
                    chr.removeVisibleMapObjectEx(summon);
                    chr.removeSummon(summon);
                    chr.dispelSkill(summon.getSkillId());
                }
                return 1;
            }
            case 追蹤:
            case 碎片:
            case 無限: {
                List<Integer> oids = IntStream.range(0, slea.readByte()).mapToObj(i -> slea.readInt()).collect(Collectors.toList());
                chr.handleAdeleObjectSword(applier.effect, oids.reversed());
                return 1;
            }
            case 乙太結晶: {
                applier.pos = slea.readPos();
                return 1;
            }
            case 創造: {
                chr.handleAdeleObjectSword(applier.effect, null);
                return 1;
            }
            case 魔力爆裂: {
                Rectangle[] rectangles = new Rectangle[applier.ai.skillSpawnInfo.size()];
                for (int i = 0; i < applier.ai.skillSpawnInfo.size(); i++) {
                    rectangles[i] = applier.effect.calculateBoundingBox2(applier.ai.skillSpawnInfo.get(i).getRight(), false);
                }
                if (rectangles.length > 0) {
                    c.announce(MaplePacketCreator.UserAreaInfosPrepare(applier.effect.getSourceId(), 0, rectangles));
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 再訪: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 乙太結晶: {
                if (applyto.getSummonCountBySkill(乙太結晶) >= applier.effect.getX()) {
                    applyto.removeSummonBySkillID(applier.effect.getSourceId(), 1);
                }
                applier.cancelEffect = false;
                return 1;
            }
            case 創造: {
                if (applyto.getBuffStatValueHolder(創造) != null) {
                    applyto.dispelEffect(創造);
                    return 0;
                }
                return 1;
            }
            case 奇蹟: {
                if (applyto.getBuffStatValueHolder(奇蹟) != null) {
                    applyto.dispelEffect(奇蹟);
                    return 0;
                }
                return 1;
            }
            case 死亡標記: {
                if (applyto.getLinkMobObjectID() <= 0) {
                    return 0;
                }
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
            case 乙太風暴: {
                int count = 0;
                for (ForceAtomObject sword : applyto.getForceAtomObjects().values()) {
                    if (sword.SkillId == applyto.getJob()) {
                        count++;
                    }
                }
                applier.localstatups.put(SecondaryStat.DevilishPower, count);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applier.totalDamage > 0L && applier.effect != null && applyto.isAlive() && applier.effect.getSourceId() == 死亡標記) {
            applyfrom.setLinkMobObjectID(applyto.getObjectId());
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.effect != null) {
            if ((applier.effect.getSourceId() == 刺擊 || applier.effect.getSourceId() == 十字斬 || applier.effect.getSourceId() == 切割 || applier.effect.getSourceId() == 踐踏)) {
                if (player.getBuffedValue(SecondaryStat.LWCreation) != null && !player.isSkillCooling(創造_1)) {
                    MapleStatEffect effect;
                    if ((effect = player.getSkillEffect(創造_1)) != null) {
                        int jobGrade = JobConstants.getJobGrade(player.getJob());
                        int coolTime = effect.getSubTime();
                        if (jobGrade >= 3) {
                            coolTime -= effect.getS();
                        }
                        if (jobGrade >= 4) {
                            coolTime -= effect.getS();
                        }
                        player.registerSkillCooldown(effect.getSourceId(), coolTime, true);
                        player.handleAdeleObjectSword(effect, Collections.emptyList());
                    }
                }
                if (player.getBuffedValue(SecondaryStat.LWWonder) != null && !player.isSkillCooling(碎片)) {
                    MapleStatEffect effect;
                    if (applier.totalDamage > 0 && (effect = player.getSkillEffect(碎片)) != null) {
                        player.registerSkillCooldown(effect, true);
                        player.handleAdeleObjectSword(effect, Collections.emptyList());
                    }
                }
                MapleStatEffect effect;
                if (applier.totalDamage > 0 && (effect = player.getSkillEffect(乙太)) != null) {
                    player.handleAdeleCharge(effect.getS());
                }
            }
            MapleStatEffect effect;
            if ((effect = player.getSkillEffect(乙太結晶)) != null) {
                boolean ret = false;
                switch (applier.effect.getSourceId()) {
                    case 創造:
                        ret = applier.effect.getQ() >= 100 || Randomizer.nextInt(100) < applier.effect.getQ();
                        break;
                    case 追蹤:
                    case 綻放:
                    case 無限:
                        ret = applier.effect.makeChanceResult(player);
                        break;
                }
                if (ret) {
                    effect.applyTo(player);
                }
            }
            if (applier.totalDamage > 0 && applier.effect.getSourceId() == 回歸 && (effect = player.getSkillEffect(回歸)) != null) {
                player.handleAdeleCharge(effect.getY());
            }
        }
        return 1;
    }
}
