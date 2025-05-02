package Client.skills.handler.其他;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.force.MapleForceAtom;
import Client.force.MapleForceFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Config.constants.skills.通用V核心.弓箭手通用;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Packet.ForcePacket;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static Config.constants.skills.通用V核心.弓箭手通用.爆擊強化;
import static Config.constants.skills.通用V核心.弓箭手通用.追蹤箭頭;

public class 全部弓箭手 extends AbstractSkillHandler {

    public 全部弓箭手() {
        for (Field field : 弓箭手通用.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsJob(int jobWithSub) {
        return JobConstants.is弓箭手(jobWithSub);
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 追蹤箭頭:
                effect.getInfo().put(MapleStatInfo.time, 2100000000); /* 值 2100000000 疑似永久開關技能 */
                statups.put(SecondaryStat.GuidedArrow, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 爆擊強化:
                statups.put(SecondaryStat.Cr2CriDamR, effect.getInfo().get(MapleStatInfo.x));
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        if (applier.effect.getSourceId() == 追蹤箭頭) {
            if (chr.getSpecialStat().getGuidedArrow() != null) {
                chr.dispelEffect(SecondaryStat.GuidedArrow);
            }
            final MapleForceAtom force = forceFactory.getMapleForce(chr, applier.effect, 0, Collections.singletonList(0));
            chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(force), true);
            chr.getSpecialStat().setGuidedArrow(force);
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 追蹤箭頭) {
            if (applier.passive) {
                return 0;
            }
            applier.localstatups.remove(SecondaryStat.IndieBuffIcon);
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
