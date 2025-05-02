package Client.skills.handler.冒險家;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.status.MonsterStatus;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.冒險家_技能群組.海盜.海盜旗幟;
import static Config.constants.skills.冒險家_技能群組.海盜.衝鋒;

public class 海盜 extends AbstractSkillHandler {

    public 海盜() {
        jobs = new MapleJob[]{
                MapleJob.海盜
        };

        for (Field field : Config.constants.skills.冒險家_技能群組.海盜.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 衝鋒:
                statups.put(SecondaryStat.DashSpeed, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.DashJump, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 海盜旗幟:
                monsterStatus.put(MonsterStatus.AreaPDR, effect.getW());
                statups.put(SecondaryStat.IndieStatRBasic, effect.getInfo().get(MapleStatInfo.indieStatRBasic));
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 海盜旗幟) {
            applier.pos = slea.readPos();
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
