package Client.skills.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import tools.data.MaplePacketReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractSkillHandler {

    protected MapleJob[] jobs = new MapleJob[0];
    protected List<Integer> skills = new LinkedList<>();

    public List<Integer> getSkills() {
        return skills;
    }

    public boolean containsSkill(int search) {
        if (skills == null) {
            return false;
        }
        return skills.contains(search);
    }

    public boolean containsJob(int jobWithSub) {
        for (MapleJob job : jobs) {
            if (job.getIdWithSub() == jobWithSub) {
                return true;
            }
        }
        return false;
    }

    public int baseSkills(MapleCharacter chr, SkillClassApplier applier) {
        AbstractSkillHandler handler = SkillClassFetcher.getHandlerByJob(JobConstants.getBeginner(chr.getJobWithSub()));
        int handleRes = -1;
        if (handler != null && handler != this) {
            handleRes = handler.baseSkills(chr, applier);
        }
        return handleRes;
    }

    public int getLinkedSkillID(int skillId) {
        return -1;
    }

    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        return -1;
    }

    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        return -1;
    }

    public int onApplyTo(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        return -1;
    }

    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        return -1;
    }

    public int onAfterRegisterEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        return -1;
    }

    public int onApplySummonEffect(final MapleCharacter applyto, SkillClassApplier applier) {
        return -1;
    }

    public int onAttack(final MapleCharacter player, final MapleMonster monster, SkillClassApplier applier) {
        return -1;
    }

    public int onApplyMonsterEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        return -1;
    }

    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        return -1;
    }

    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        return -1;
    }

    public int onAfterCancelEffect(MapleCharacter player, SkillClassApplier applier) {
        return -1;
    }
}
