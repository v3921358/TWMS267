package Client.skills.handler.其他;

import Client.MapleCharacter;
import Client.SecondaryStat;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Config.constants.skills.通用V核心.劍士通用;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.通用V核心.劍士通用.*;

public class 全部劍士 extends AbstractSkillHandler {

    public 全部劍士() {
        for (Field field : 劍士通用.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsJob(int jobWithSub) {
        return JobConstants.is劍士(jobWithSub);
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        if (skillId == 幻靈武具_1) {
            return 幻靈武具;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 幻靈武具:
                effect.getInfo().put(MapleStatInfo.time, 80000 + (effect.getLevel() * 1000));
                statups.put(SecondaryStat.Warrior_AuraWeaponStack, 1);
                statups.put(SecondaryStat.Warrior_AuraWeapon, effect.getLevel());
                return 1;
            case 幻靈武具_1:
                effect.getInfo().put(MapleStatInfo.attackCount, 15);
                return 1;
            case 鋼鐵之軀:
                statups.put(SecondaryStat.IndieAsrR, effect.getInfo().get(MapleStatInfo.indieAsrR));
                statups.put(SecondaryStat.IndieApplySuperStance, 1);
                statups.put(SecondaryStat.HitStackDamR, 1);
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
