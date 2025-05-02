package Client.skills.handler.皇家騎士團;

import Client.MapleCharacter;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.貴族.*;

public class 貴族 extends AbstractSkillHandler {

    public 貴族() {
        jobs = new MapleJob[]{
                MapleJob.貴族
        };

        for (Field field : Config.constants.skills.貴族.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int baseSkills(MapleCharacter chr, SkillClassApplier applier) {
        if (JobConstants.is皇家騎士團(chr.getJobWithSub())) {
            Skill skill;
            int[] ss = {英雄的回響, 元素狂刃, 聖地回歸, 元素位移, 元素精通, 元素閃現};
            for (int i : ss) {
                if (chr.getLevel() < 200 && i == 英雄的回響) {
                    continue;
                }
                skill = SkillFactory.getSkill(i);
                if (skill != null && chr.getSkillLevel(skill) <= 0) {
                    applier.skillMap.put(skill.getId(), new SkillEntry(1, skill.getMaxMasterLevel(), -1));
                }
            }
            if (JobConstants.getJobNumber(chr.getJob()) == 4) {
                // Royal_西格諾斯騎士
                skill = SkillFactory.getSkill(chr.getJob() * 10000 + 1000);
                if (skill != null && chr.getSkillEntry(skill.getId()) == null) {
                    applier.skillMap.put(skill.getId(), new SkillEntry(0, skill.getMaxLevel(), -1));
                }
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        if (skillId == 元素位移_超高跳) {
            return 元素位移;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 英雄的回響:
                effect.setRangeBuff(true);
            case 女皇的祈禱:
                effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
                statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
                return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 聖地回歸) {
            applyto.changeMap(applier.effect.getX(), 0);
            return 1;
        }
        return -1;
    }
}
