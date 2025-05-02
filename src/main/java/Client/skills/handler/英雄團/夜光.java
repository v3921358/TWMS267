package Client.skills.handler.英雄團;

import Client.*;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.HexaSKILL;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static Config.constants.skills.夜光.*;

public class 夜光 extends AbstractSkillHandler {

    public 夜光() {
        jobs = new MapleJob[]{
                MapleJob.夜光,
                MapleJob.夜光1轉,
                MapleJob.夜光2轉,
                MapleJob.夜光3轉,
                MapleJob.夜光4轉
        };

        for (Field field : Config.constants.skills.夜光.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int baseSkills(MapleCharacter chr, SkillClassApplier applier) {
        if (chr.getLevel() >= 10) {
            Skill skill;
            int[] ss = {英雄共鳴, 光蝕, 暗蝕, 平衡_光明, 光明力量, 星光順移};
            for (int i : ss) {
                if (chr.getLevel() < 200 && i == 英雄共鳴) {
                    continue;
                }
                skill = SkillFactory.getSkill(i);
                if (skill != null && chr.getSkillLevel(skill) <= 0) {
                    applier.skillMap.put(i, new SkillEntry(1, skill.getMaxMasterLevel(), -1));
                }
            }
            int[] fixskills = {星星閃光, 黑暗球體, 光魔法強化, 黑暗魔法強化};
            for (int f : fixskills) {
                skill = SkillFactory.getSkill(f);
                if (chr.getJob() >= f / 10000 && skill != null && chr.getSkillLevel(skill) <= 0 && chr.getMasterLevel(skill) <= 0) {
                    applier.skillMap.put(f, new SkillEntry(0, skill.getMasterLevel() == 0 ? skill.getMaxLevel() : skill.getMasterLevel(), SkillFactory.getDefaultSExpiry(skill)));
                }
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 27141501:
                return 27141500;
            case 27141000:
                return 絕對擊殺;
            case HexaSKILL.強化真理之門:
                return 400021005;
            case HexaSKILL.強化混沌共鳴:
                return 400021041;
            case HexaSKILL.強化光與暗的洗禮:
                return 400021071;
            case HexaSKILL.強化解放寶珠:
                return 400021105;
            case 光之躍:
                return 閃光瞬步;
            case 晨星殞落_爆炸:
                return 晨星殞落;
            case 混沌共鳴_1:
            case 混沌共鳴_2:
                return 混沌共鳴;
            case 解放寶珠_1:
            case 解放寶珠_2:
            case 解放寶珠_3:
            case 解放寶珠_4:
            case 解放寶珠_5:
                return 解放寶珠;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 英雄共鳴:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
                statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
                return 1;
            case 光蝕:
            case 暗蝕:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.Larkness, 1);
                return 1;
            case 平衡_光明:
            case 平衡_黑暗:
                statups.put(SecondaryStat.Larkness, 2);
                statups.put(SecondaryStat.Stance, effect.getInfo().get(MapleStatInfo.prop));
                return 1;
            case 黑暗之眼:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.KeyDownAreaMoving, 1);
                return 1;
            case 閃亮救贖:
                effect.setHpR(effect.getInfo().get(MapleStatInfo.x) / 1000.0);
                return 1;
            case 強化閃光瞬步:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.TeleportMasteryRange, 1);
                return 1;
            case 光暗轉換:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.LifeTidal, 2);
                return 1;
            case 魔力護盾:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.AntiMagicShell, effect.getU());
                return 1;
            case 團隊精神:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.EMAD, effect.getX());
                return 1;
            case 黑暗魔心:
                statups.put(SecondaryStat.ElementalReset, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 黑暗強化:
                statups.put(SecondaryStat.StackBuff, 1);
                return 1;
            case 英雄誓言:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 光柱爆發:
                monsterStatus.put(MonsterStatus.Stun, 1);
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 末日審判:
                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 光與暗的洗禮:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.SwordBaptism, 0);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 平衡解放: {
                if (chr.getLarkness() <= 0) {
                    chr.getSkillEffect(平衡_光明).unprimaryPassiveApplyTo(chr);
                    chr.setLarknessDiraction(1);
                }
                if (chr.getLarkness() >= 10000) {
                    chr.getSkillEffect(平衡_光明).unprimaryPassiveApplyTo(chr);
                    chr.setLarknessDiraction(2);
                }
                chr.updateLarknessStack();
                return 1;
            }
            case 重啟平衡: {
                MapleStatEffect effect = chr.getSkillEffect(平衡_光明);
                effect.applyTo(chr, chr, true, null, effect.getDuration(), false);
                return 1;
            }
            case 混沌共鳴:
            case 混沌共鳴_1:
            case 混沌共鳴_2: {
                final MapleStatEffect eff;
                if ((eff = chr.getSkillEffect(混沌共鳴)) == null) {
                    return 1;
                }
                if (!chr.isSkillCooling(eff.getSourceId())) {
                    chr.registerSkillCooldown(eff.getSourceId(), eff.getCooldown(chr), true);
                    return 1;
                }
                if (chr.isSkillCooling(eff.getSourceId())) {
                    return 0;
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 光暗轉換: {
                applier.localstatups = Collections.singletonMap(SecondaryStat.LifeTidal, applyto.getStat().getLifeTidal());
                return 1;
            }
            case 黑暗強化: {
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.StackBuff);
                applier.buffz = applyto.getBuffedIntZ(SecondaryStat.StackBuff);
                if (mbsvh == null) {
                    return 1;
                }
                if (applier.buffz < applier.effect.getX()) {
                    applier.buffz = Math.min(applier.buffz + 1, applier.effect.getX());
                    applier.duration = mbsvh.getLeftTime();
                    applier.localstatups.put(SecondaryStat.StackBuff, applier.buffz * applier.effect.getDamR());
                    return 1;
                }
                return 0;
            }
            case 平衡_光明: {
                applyto.cancelSkillCooldown(死神鐮刀);
                applyto.cancelSkillCooldown(絕對擊殺);
                if (!applier.passive) {
                    applier.b3 = true;
                    applyto.setTruthGate(true);
                    applyto.dispelEffect(SecondaryStat.Larkness);
                    return 1;
                }
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.Larkness);
                if (mbsvh == null) {
                    return 1;
                }
                applyto.setTruthGate(false);
                if (mbsvh.x == 2) {
                    applier.duration = mbsvh.getLeftTime();
                }
                return 1;
            }
            case 光蝕:
            case 暗蝕: {
                applyto.dispelEffect(SecondaryStat.Larkness);
                return 1;
            }
            case 重啟平衡: {
                applyto.setLarknessDiraction(1);
                applyto.setLarkness(0);
                applyto.updateLarknessStack();
                return 1;
            }
            case 英雄誓言: {
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
            case 真理之門: {
                applyto.setTruthGate(false);
                applyto.getSkillEffect(平衡_光明).unprimaryApplyTo(applyto, null, true);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (containsJob(applyfrom.getJobWithSub()) && applyfrom.getJob() != 2700 && applier.effect != null) {
            final int type = applier.effect.getSourceId() % 1000 / 100; // 1=光明，2=黑暗，3=平衡
            final MapleStatEffect skillEffect15;
            if (applyfrom.getEffectForBuffStat(SecondaryStat.Larkness) == null && (skillEffect15 = applyfrom.getSkillEffect(光蝕)) != null) {
                skillEffect15.unprimaryPassiveApplyTo(applyfrom);
                applyfrom.setLarknessDiraction(2);
                applyfrom.addLarkness(10000);
                applyfrom.updateLarknessStack();
            }
            switch (type) {
                case 1: { // 使用光明技能
                    if (type != applyfrom.getLarknessDiraction()) {
                        if (applyfrom.getBuffedIntValue(SecondaryStat.Larkness) != 2) {
                            applyfrom.addLarkness(-(Randomizer.nextInt(100) + 80));
                            if (applyfrom.getLarkness() <= 0) {
                                applyfrom.setLarknessDiraction(3);
                            }
                            applyfrom.updateLarknessStack();
                        }
                        applyfrom.addHPMP(1, 0);
                    }
                    break;
                }
                case 2: {
                    if (type != applyfrom.getLarknessDiraction()) {
                        if (applyfrom.getBuffedIntValue(SecondaryStat.Larkness) != 2) {
                            applyfrom.addLarkness(Randomizer.nextInt(100) + 80);
                            if (applyfrom.getLarkness() >= 10000) {
                                applyfrom.setLarknessDiraction(3);
                            }
                            applyfrom.updateLarknessStack();
                        }
                    }
                    break;
                }
            }
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        final MapleStatEffect effecForBuffStat9 = player.getEffectForBuffStat(SecondaryStat.StackBuff);
        if (applier.totalDamage > 0L && effecForBuffStat9 != null && applier.effect != null && effecForBuffStat9.makeChanceResult(player)) {
            effecForBuffStat9.unprimaryPassiveApplyTo(player);
        }
        return 1;
    }
}
