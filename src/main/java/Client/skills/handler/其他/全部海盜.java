package Client.skills.handler.其他;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Config.constants.skills.通用V核心.海盜通用;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Packet.EffectPacket;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.通用V核心.海盜通用.*;

public class 全部海盜 extends AbstractSkillHandler {

    public 全部海盜() {
        for (Field field : 海盜通用.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsJob(int jobWithSub) {
        return JobConstants.is海盜(jobWithSub);
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        if (skillId == 幸運骰子) {
            return 滿載骰子;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 滿載骰子:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.LoadedDice, 0);
                return 1;
            case 超速動能:
                statups.put(SecondaryStat.ConvertAD, effect.getInfo().get(MapleStatInfo.x));
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 滿載骰子) {
            applier.passive = true;
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 滿載骰子: {
                if (applyto.getSpecialStat().getRemoteDice() < 0) {
                    return 0;
                }
                applier.localstatups.put(SecondaryStat.LoadedDice, applyto.getSpecialStat().getRemoteDice());
                return 1;
            }
            case 幸運骰子: {
                int dice = Randomizer.nextInt(6) + 1;
                if (applyto.getSpecialStat().getRemoteDice() > 0) {
                    dice = applyto.getSpecialStat().getRemoteDice();
                    applyto.getSpecialStat().setRemoteDice(-1);
                }
                if (dice == 1) {
                    applyto.reduceSkillCooldown(幸運骰子, 90000);
                }
                applyto.dropMessage(11, "幸運骰子 技能發動[" + dice + "]號效果。");
                applier.localstatups.put(SecondaryStat.Dice, dice);
                applyto.getClient().announce(EffectPacket.showDiceEffect(-1, applier.effect.getSourceId(), applier.effect.getLevel(), dice, -1, false));
                applyto.getMap().broadcastMessage(applyto, EffectPacket.showDiceEffect(applyto.getId(), applier.effect.getSourceId(), applier.effect.getLevel(), dice, -1, false), false);
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
