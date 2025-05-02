package Client.skills.handler.雷普族;

import Client.MapleCharacter;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Config.constants.skills.通用V核心.雷普族通用;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.通用V核心.雷普族通用.魔法迴路效能全開;
import static Config.constants.skills.通用V核心.雷普族通用.魔法迴路效能全開_1;

public class 雷普族 extends AbstractSkillHandler {

    public 雷普族() {
        for (Field field : 雷普族通用.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsJob(int jobWithSub) {
        return JobConstants.is雷普族(jobWithSub);
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        if (skillId == 魔法迴路效能全開_1) {
            return 魔法迴路效能全開;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        if (effect.getSourceId() == 魔法迴路效能全開) {
            statups.put(SecondaryStat.LPMagicCircuitFullDrive, 1);
            statups.put(SecondaryStat.IndieDamR, 1);
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 魔法迴路效能全開) {
            final SecondaryStatValueHolder mbsvh;
            if ((mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.LPMagicCircuitFullDrive)) != null) {
                applier.duration = mbsvh.getLeftTime();
            }
            applier.localstatups.put(SecondaryStat.LPMagicCircuitFullDrive, 1);
            applier.localstatups.put(SecondaryStat.IndieDamR, Math.max(1, Math.min(applier.effect.getY(), (100 - applyfrom.getStat().getMPPercent()) / (100 / applier.effect.getY()))));
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
