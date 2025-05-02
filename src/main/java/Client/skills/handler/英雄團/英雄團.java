package Client.skills.handler.英雄團;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Config.constants.skills.通用V核心.英雄團通用;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.通用V核心.英雄團通用.*;

public class 英雄團 extends AbstractSkillHandler {

    public 英雄團() {
        for (Field field : 英雄團通用.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsJob(int jobWithSub) {
        return JobConstants.is英雄團(jobWithSub);
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 普力特的祝福_1:
            case 普力特的祝福_2:
            case 普力特的祝福_3:
            case 普力特的祝福_4:
            case 普力特的祝福_5:
            case 普力特的祝福_6:
                return 普力特的祝福;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 普力特的祝福_1:
            case 普力特的祝福_2:
            case 普力特的祝福_3:
            case 普力特的祝福_4:
            case 普力特的祝福_5:
                statups.put(SecondaryStat.FreudBlessing, effect.getSourceId() % 10 - 4);
                statups.put(SecondaryStat.IndieCooltimeReduce, effect.getInfo().get(MapleStatInfo.indieCooltimeReduce));
                statups.put(SecondaryStat.IndieStance, effect.getInfo().get(MapleStatInfo.indieStance));
                statups.put(SecondaryStat.IndieAllStat, effect.getInfo().get(MapleStatInfo.indieAllStat));
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.IndieMAD, effect.getInfo().get(MapleStatInfo.indieMad));
                statups.put(SecondaryStat.IndieBDR, effect.getInfo().get(MapleStatInfo.indieBDR));
                return 1;
            case 普力特的祝福_6:
                statups.clear();
                statups.put(SecondaryStat.FreudBlessing, 6);
                statups.put(SecondaryStat.IndieCooltimeReduce, effect.getInfo().get(MapleStatInfo.indieCooltimeReduce));
                statups.put(SecondaryStat.IndieStance, effect.getInfo().get(MapleStatInfo.indieStance));
                statups.put(SecondaryStat.IndieAllStat, effect.getInfo().get(MapleStatInfo.indieAllStat));
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.IndieMAD, effect.getInfo().get(MapleStatInfo.indieMad));
                statups.put(SecondaryStat.IndieBDR, effect.getInfo().get(MapleStatInfo.indieBDR));
                statups.put(SecondaryStat.NotDamaged, 1);
                break;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 普力特的祝福:
            case 普力特的祝福_1:
            case 普力特的祝福_2:
            case 普力特的祝福_3:
            case 普力特的祝福_4:
            case 普力特的祝福_5: {
                final MapleStatEffect eff;
                if ((eff = chr.getSkillEffect(普力特的祝福)) != null && !chr.isSkillCooling(eff.getSourceId())) {
                    chr.registerSkillCooldown(eff.getSourceId(), eff.getCooldown(chr), true);
                }
                return 1;
            }
            case 普力特的祝福_6: {
                final MapleStatEffect eff;
                if ((eff = chr.getSkillEffect(普力特的祝福)) == null) {
                    return 1;
                }
                if (!chr.isSkillCooling(eff.getSourceId())) {
                    chr.registerSkillCooldown(eff.getSourceId(), eff.getY() * 1000, true);
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
            case 普力特的祝福_1:
            case 普力特的祝福_2:
            case 普力特的祝福_3:
            case 普力特的祝福_4:
            case 普力特的祝福_5:
            case 普力特的祝福_6: {
                applyto.dispelEffect(SecondaryStat.FreudBlessing);
                return 1;
            }
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
