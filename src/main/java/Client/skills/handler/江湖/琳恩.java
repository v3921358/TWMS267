package Client.skills.handler.江湖;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.skills.菈菈;
import Net.server.buffs.MapleStatEffect;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.琳恩.*;

public class 琳恩 extends AbstractSkillHandler {

    public 琳恩() {
        jobs = new MapleJob[]{
                MapleJob.琳恩,
                MapleJob.琳恩1轉,
                MapleJob.琳恩2轉,
                MapleJob.琳恩3轉,
                MapleJob.琳恩4轉
        };

        for (Field field : 菈菈.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int baseSkills(MapleCharacter chr, SkillClassApplier applier) {
        int level = chr.getLevel();
        Skill skil;
        final int[] ss = {森林之吻, 同行, 集中, 英雄的回響};
        for (int i : ss) {
            if (i == 英雄的回響 && level < 200) {
                continue;
            }
            int skillLevel = 1;
            skil = SkillFactory.getSkill(i);
            if (skil != null && chr.getSkillLevel(skil) < skillLevel) {
                applier.skillMap.put(i, new SkillEntry(skillLevel, skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 守護神羈絆_潘_1:
                return 守護神羈絆_潘;
            case 守護神羈絆_沛尼_1:
                return 守護神羈絆_沛尼;
            case 粉碎大地_1:
                return 粉碎大地;
            case 暗襲_1:
                return 暗襲;
            case 守護神羈絆_法伊安_1:
                return 守護神羈絆_法伊安;
            case 集中_森林的保護_1:
                return 集中_森林的保護;
            case 覺醒:
            case 耗盡:
                return 集中_覺醒;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            /*case 琳恩_英雄的迴響:
                effect.getInfo().put(MapleStatInfo.pdR, 4); // 4%
                return 1;*/
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 森林之吻: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
        }
        return -1;
    }
}
