package Client.skills.handler.冒險家;

import Client.MapleCharacter;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Config.constants.skills.冒險家_技能群組.影武者;
import Config.constants.skills.重砲指揮官;
import Config.constants.skills.開拓者;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.冒險家_技能群組.type_劍士.初心者.回歸楓之谷;
import static Config.constants.skills.冒險家_技能群組.type_劍士.初心者.英雄的迴響;

public class 初心者 extends AbstractSkillHandler {

    public 初心者() {
        jobs = new MapleJob[]{
                MapleJob.初心者,
                MapleJob.初心者_影武,
                MapleJob.初心者_重砲,
                MapleJob.初心者_開拓
        };

        for (Field field : Config.constants.skills.冒險家_技能群組.type_劍士.初心者.class.getDeclaredFields()) {
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
        if (JobConstants.is影武者(chr.getJobWithSub()) || MapleJob.初心者_影武.getIdWithSub() == chr.getJobWithSub()) {
            skil = SkillFactory.getSkill(影武者.回歸墮落城市);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(skil.getId(), new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        } else if (JobConstants.is開拓者(chr.getJobWithSub()) || MapleJob.初心者_開拓.getIdWithSub() == chr.getJobWithSub()) {
            int[] ss = {開拓者.回歸帕爾坦, 開拓者.古代的詛咒};
            for (int i : ss) {
                int skillLevel = 1;
                if (i == 1298) {
                    skillLevel = chr.getJob() == 301 ? 1 : chr.getJob() == 330 ? 2 : chr.getJob() == 331 ? 3 : chr.getJob() == 332 ? 4 : 0;
                }
                skil = SkillFactory.getSkill(i);
                if (skil != null && chr.getSkillLevel(skil) < skillLevel) {
                    applier.skillMap.put(skil.getId(), new SkillEntry(skillLevel, skil.getMaxMasterLevel(), -1));
                }
            }
        } else if (JobConstants.is重砲指揮官(chr.getJobWithSub()) || MapleJob.初心者_重砲.getIdWithSub() == chr.getJobWithSub()) {
            skil = SkillFactory.getSkill(重砲指揮官.回家_加農砲);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(skil.getId(), new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        } else {
            skil = SkillFactory.getSkill(回歸楓之谷);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(skil.getId(), new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        if (chr.getLevel() >= 200) {
            skil = SkillFactory.getSkill(英雄的迴響);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(skil.getId(), new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        if (effect.getSourceId() == 英雄的迴響) {
            effect.setRangeBuff(true);
            effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
            statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 回歸楓之谷) {
            applyto.changeMap(applier.effect.getX(), 0);
            return 1;
        }
        return -1;
    }

    @Override
    public int onAttack(final MapleCharacter player, final MapleMonster monster, SkillClassApplier applier) {
        AbstractSkillHandler holder = SkillClassFetcher.getHandlerByJob(player.getJobWithSub());
        if (holder == this) {
            return -1;
        }
        return holder.onAttack(player, monster, applier);
    }

    @Override
    public int onApplyMonsterEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        AbstractSkillHandler holder = SkillClassFetcher.getHandlerByJob(applyfrom.getJobWithSub());
        if (holder == this) {
            return -1;
        }
        return holder.onApplyMonsterEffect(applyfrom, applyto, applier);
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        AbstractSkillHandler holder = SkillClassFetcher.getHandlerByJob(applyfrom.getJobWithSub());
        if (holder == this) {
            return -1;
        }
        return holder.onApplyAttackEffect(applyfrom, applyto, applier);
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        AbstractSkillHandler holder = SkillClassFetcher.getHandlerByJob(player.getJobWithSub());
        if (holder == this) {
            return -1;
        }
        return holder.onAfterAttack(player, applier);
    }
}
