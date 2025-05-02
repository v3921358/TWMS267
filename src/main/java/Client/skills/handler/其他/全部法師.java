package Client.skills.handler.其他;

import Client.MapleCharacter;
import Client.SecondaryStat;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Config.constants.skills.通用V核心.法師通用;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.通用V核心.法師通用.虛無型態;
import static Config.constants.skills.通用V核心.法師通用.超載魔力;

public class 全部法師 extends AbstractSkillHandler {

    public 全部法師() {
        for (Field field : 法師通用.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsJob(int jobWithSub) {
        return JobConstants.is法師(jobWithSub);
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 超載魔力:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.Wizard_OverloadMana, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 虛無型態:
                statups.put(SecondaryStat.EtherealForm, 1);

                return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 超載魔力) {
            if (applyto.getBuffedValue(SecondaryStat.Wizard_OverloadMana) != null) {
                applier.overwrite = false;
                applier.localstatups.clear();
            }
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
