package Client.skills.handler.雷普族;

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

import static Config.constants.skills.卡莉.*;

public class 卡莉 extends AbstractSkillHandler {

    public 卡莉() {
        jobs = new MapleJob[]{
                MapleJob.卡莉,
                MapleJob.卡莉1轉,
                MapleJob.卡莉2轉,
                MapleJob.卡莉3轉,
                MapleJob.卡莉4轉
        };

        for (Field field : Config.constants.skills.卡莉.class.getDeclaredFields()) {
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
        final int[] ss = {Config.constants.skills.卡莉.魔法迴路, 回歸領地, Config.constants.skills.卡莉.獨門咒語};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == Config.constants.skills.卡莉.獨門咒語) {
                continue;
            }
            skil = SkillFactory.getSkill(i);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(i, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }


    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 環刃推進器:  // 2 轉
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 召喚查克里_主技能:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.SummonChakri, 1);
                return 1;
            case 召喚查克里_生成:
                effect.getInfo().put(MapleStatInfo.time, 30000);
                statups.put(SecondaryStat.SummonChakri, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 沙漠面紗:  // 4 轉
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 3000);
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
            case 雷普的勇士: // 4 轉 - 意志淨化已在別處進行處理
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 神之種族:
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 虛空強化:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.HideAttack, 1);
                return 1;
            case 虛空突襲:
                effect.getInfo().put(MapleStatInfo.time, 2000);
                statups.put(SecondaryStat.DarkSight, effect.getLevel());
                statups.put(SecondaryStat.Speed, 1);
                return 1;
            case 忘卻:
                effect.getInfo().put(MapleStatInfo.time, 30000);
                effect.getInfo().put(MapleStatInfo.coolTimeR, 50);
                effect.getInfo().put(MapleStatInfo.damR, 30);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
        }

        return -1;
    }

    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case Config.constants.skills.卡莉.神之種族: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(Config.constants.skills.阿戴爾.神之種族);
                applyto.dispelEffect(Config.constants.skills.伊利恩.神之種族);
                applyto.dispelEffect(Config.constants.skills.亞克.神之種族);
                applyto.dispelEffect(Config.constants.skills.卡莉.神之種族);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case HEXA_魔咒_沙塵暴_IV:
            case HEXA_魔咒_沙塵暴_III:
            case HEXA_魔咒_沙塵暴_II:
                return HEXA_魔咒_沙塵暴;
            case HEXA_藝術_亂舞_VI:
                return 藝術_亂舞;
            case 電光石火_一階段:
                return 電光石火_一階段;
            case 電光石火_二階段:
                return 電光石火_二階段;
            case 藝術_新月_1:
                return 藝術_新月;
            case 虛空衝刺_1:
            case 虛空衝刺_2:
            case 虛空衝刺_3:
                return 瘋狂I;
            case 召喚查克里_主技能:
                return 召喚查克里_生成;
            case 虛空衝刺_4:
            case 虛空衝刺_5:
                return 瘋狂II;
            case 154121009:
            case 虛空突襲_1:
                return 虛空突襲;
            case 魔咒_混亂_1:
                return 魔咒_混亂;
            case 虛空破滅_1:
            case 虛空破滅_2:
                return 虛空破滅;
            case 藝術_阿斯特拉_1:
                return 藝術_阿斯特拉;
            case 共鳴_最後通牒_1:
            case 共鳴_最後通牒_2:
                return 共鳴_最後通牒;
        }
        return -1;
    }
}
