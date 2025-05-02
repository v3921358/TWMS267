package Client.skills.handler.其他;

import Client.MapleCharacter;
import Client.SecondaryStat;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Config.constants.skills.通用V核心.盜賊通用;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.通用V核心.盜賊通用.必死決心;

public class 全部盜賊 extends AbstractSkillHandler {

    public 全部盜賊() {
        for (Field field : 盜賊通用.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsJob(int jobWithSub) {
        return JobConstants.is盜賊(jobWithSub);
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        if (effect.getSourceId() == 必死決心) {
            statups.put(SecondaryStat.IndiePMdR, effect.getInfo().get(MapleStatInfo.y));
            statups.put(SecondaryStat.IndieEVARReduceR, effect.getInfo().get(MapleStatInfo.x));
            statups.put(SecondaryStat.IndieHitDamR, effect.getInfo().get(MapleStatInfo.z));
            statups.put(SecondaryStat.ReadyToDie, 1);
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 必死決心) {
            final int value = applyto.getBuffedIntValue(SecondaryStat.ReadyToDie) + 1;
            if (value > 2) {
                applier.overwrite = false;
                applier.localstatups.clear();
                return 1;
            }
            applier.localstatups.put(SecondaryStat.ReadyToDie, value);
            applier.localstatups.put(SecondaryStat.IndiePMdR, value == 2 ? applier.effect.getQ() : applier.effect.getY());
            applier.localstatups.put(SecondaryStat.IndieHitDamR, value == 2 ? applier.effect.getS() : applier.effect.getZ());
            applier.localstatups.put(SecondaryStat.IndieEVARReduceR, value == 2 ? applier.effect.getW() : applier.effect.getX());
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
