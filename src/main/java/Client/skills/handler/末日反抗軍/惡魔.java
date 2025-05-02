package Client.skills.handler.末日反抗軍;

import Client.MapleCharacter;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.惡魔.*;

public class 惡魔 extends AbstractSkillHandler {

    public 惡魔() {
        jobs = new MapleJob[]{
                MapleJob.惡魔
        };

        for (Field field : Config.constants.skills.惡魔.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int baseSkills(MapleCharacter chr, SkillClassApplier applier) {
        Skill skil;
        int[] ss = {惡魔跳躍, 魔族之血, 英雄共鳴};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == 英雄共鳴) {
                continue;
            }
            skil = SkillFactory.getSkill(i);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(i, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        if (effect.getSourceId() == 英雄共鳴) {
            effect.setRangeBuff(true);
            effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
            statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
            return 1;
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 惡魔跳躍_1:
            case 惡魔跳躍_2:
            case 惡魔跳躍_3:
                return 惡魔跳躍;
            case 召喚喵怪仙人_1:
                return 召喚喵怪仙人;
        }
        return -1;
    }
}
