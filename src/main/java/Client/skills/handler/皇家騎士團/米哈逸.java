package Client.skills.handler.皇家騎士團;

import Client.MapleCharacter;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.HexaSKILL;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Config.constants.skills.皇家騎士團_技能群組.破風使者;
import Config.constants.skills.皇家騎士團_技能群組.聖魂劍士;
import Config.constants.skills.皇家騎士團_技能群組.閃雷悍將;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.皇家騎士團_技能群組.米哈逸.*;

public class 米哈逸 extends AbstractSkillHandler {

    public 米哈逸() {
        jobs = new MapleJob[]{
                MapleJob.米哈逸,
                MapleJob.米哈逸1轉,
                MapleJob.米哈逸2轉,
                MapleJob.米哈逸3轉,
                MapleJob.米哈逸4轉
        };

        for (Field field : Config.constants.skills.皇家騎士團_技能群組.米哈逸.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int baseSkills(MapleCharacter chr, SkillClassApplier applier) {
        Skill skill;
        int[] ss = {英雄的回響, 聖地回歸, 元素精通};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == 英雄的回響) {
                continue;
            }
            skill = SkillFactory.getSkill(i);
            if (skill != null && chr.getSkillLevel(skill) <= 0) {
                applier.skillMap.put(skill.getId(), new SkillEntry(1, skill.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 51141501:
            case 51141502:
                return HexaSKILL.輝煌聖劍;
            case 51141000:
            case 51141001:
                return 閃光交叉;
            case HexaSKILL.強化熾天覆七重圓環:
                return 400011011;
            case HexaSKILL.強化光輝聖劍:
                return 400011032;
            case HexaSKILL.強化魂光劍擊:
                return 400011083;
            case HexaSKILL.強化光之勇氣:
                return 400011127;
            case 皇家之盾_9:
            case 皇家之盾_10:
                return 進階皇家之盾;
            case 皇家之盾_1:
            case 皇家之盾_2:
            case 皇家之盾_3:
            case 皇家之盾_4:
            case 皇家之盾_5:
            case 皇家之盾_6:
            case 皇家之盾_7:
            case 皇家之盾_8:
                return 皇家之盾;
            case 光輝聖劍_1:
            case 光輝聖劍_2:
            case 光輝聖劍_3:
            case 光輝聖劍_4:
            case 光輝聖劍_5:
                return 光輝聖劍;
            case 靈魂威嚴:
                return 魂光劍擊;
            case 光之勇氣_1:
            case 光之勇氣_2:
            case 光之勇氣_3:
            case 光之勇氣_4:
                return 光之勇氣;
            case 光明追擊_JUMP_UP:
                return 光明追擊_MOVE;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 閃光突刺:
            case 光明追擊_MOVE:
            case 光明追擊_JUMP_UP:
                return 1;
            case Switch_開關_靈魂連接:
                effect.setPartyBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                effect.getInfo().put(MapleStatInfo.damR, 5);
                statups.put(SecondaryStat.IndieDotHealMP, -5);
            case 英雄的回響:
                effect.setRangeBuff(true);
                return 1;
            case 女皇的祈禱:
                effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
                statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
                return 1;
            case 光之守護:
                statups.put(SecondaryStat.MichaelStanceLink, 0);
                return 1;
            case 光之守護_傳授:
                statups.put(SecondaryStat.Stance, effect.getInfo().get(MapleStatInfo.prop));
                return 1;
            case 鋼鐵之軀:
                statups.clear();
                statups.put(SecondaryStat.DamAbsorbShield, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.IndieMHPR, effect.getInfo().get(MapleStatInfo.indieMhpR));
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 光之勇氣:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 25000);
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1); // 檢查屬性
                statups.put(SecondaryStat.IncMaxHP, 98);
                statups.put(SecondaryStat.IndieDrainHP, 7);
                return 1;
            case 快速之劍:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 皇家之盾_1:
                effect.getInfo().put(MapleStatInfo.time, 4000);
                statups.put(SecondaryStat.BodyRectGuardPrepare, 1);
                statups.put(SecondaryStat.ManaReflection, 1);
                statups.put(SecondaryStat.NotDamaged, 4);
                statups.put(SecondaryStat.PAD, 5);
                return 1;
            case 靈魂抗性:
                statups.put(SecondaryStat.AsrR, effect.getInfo().get(MapleStatInfo.y));
                statups.put(SecondaryStat.TerR, effect.getInfo().get(MapleStatInfo.z));
                statups.put(SecondaryStat.DDR, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 魔防消除:
                monsterStatus.put(MonsterStatus.MagicCrash, 1);
                return 1;
            case 進階防禦:
                monsterStatus.put(MonsterStatus.Seal, 1);
                return 1;
            case Royal_西格諾斯騎士:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case Switch_開關_靈魂之怒:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.Enrage, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.EnrageCr, effect.getInfo().get(MapleStatInfo.z));
                statups.put(SecondaryStat.EnrageCrDamMin, effect.getInfo().get(MapleStatInfo.y) - 1);
                statups.put(SecondaryStat.IndiePADR, 25);
                statups.put(SecondaryStat.IndieBDR, 10);
                return 1;
            case 閃光交叉:
            case 靈魂突擊:
                monsterStatus.put(MonsterStatus.Blind, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 明日女皇:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 熾天覆七重圓環:
                statups.put(SecondaryStat.Michael_RhoAias, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 魂光劍擊:
                statups.put(SecondaryStat.IndieCr, effect.getInfo().get(MapleStatInfo.indieCr));
                statups.put(SecondaryStat.IndieIgnoreMobpdpR, effect.getInfo().get(MapleStatInfo.indieIgnoreMobpdpR));
                statups.put(SecondaryStat.IndiePADR, effect.getInfo().get(MapleStatInfo.indiePadR));
                statups.put(SecondaryStat.MichaelSwordOfLight, effect.getInfo().get(MapleStatInfo.x));
                return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 聖地回歸: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 光之守護: {
                if (!applier.passive) {
                    return 1;
                }
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.MichaelStanceLink);
                if (mbsvh == null) {
                    return 0;
                }
                final int max = Math.max(0, mbsvh.value - 1);
                if (max > 0) {
                    applier.duration = mbsvh.getLeftTime();
                    applier.localstatups.put(SecondaryStat.MichaelStanceLink, max);
                    return 1;
                }
                applier.overwrite = false;
                applier.localstatups.clear();
                return 1;
            }
            case 靈魂抗性: {
                MapleStatEffect eff;
                if ((eff = applyto.getSkillEffect(靈魂抗性_免疫附體)) != null) {
                    applier.localstatups.put(SecondaryStat.TerR, applier.effect.getY() + eff.getY());
                    applier.localstatups.put(SecondaryStat.AsrR, applier.effect.getZ() + eff.getZ());
                }
                if ((eff = applyto.getSkillEffect(靈魂抗性_鋼鐵身軀)) != null) {
                    applier.localstatups.put(SecondaryStat.DDR, applier.effect.getX() + eff.getX());
                }
                return 1;
            }
            case 皇家之盾_1:
            case 皇家之盾_2:
            case 皇家之盾_3:
            case 皇家之盾_4:
            case 皇家之盾_5: {
                applier.b3 = true;
                applyto.registerSkillCooldown(皇家之盾, 6000, true);
                return 1;
            }
            case 皇家之盾_6:
            case 皇家之盾_7:
            case 皇家之盾_8:
            case 皇家之盾_9:
            case 皇家之盾_10: {
                applier.localstatups.put(SecondaryStat.RoyalGuardState, Math.min(applyto.getBuffedIntValue(SecondaryStat.RoyalGuardState) + 1, 5));
                applier.localstatups.put(SecondaryStat.IndiePAD, applier.effect.getW());
                applyto.dispelEffect(SecondaryStat.RoyalGuardState);
                applier.maskedstatups.put(SecondaryStat.NotDamaged, 1);
                applier.maskedDuration = 2000;
                return 1;
            }
            case 明日女皇: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000 || applyto.getJob() / 1000 == 1) {
                    return 0;
                }
                applyto.dispelEffect(聖魂劍士.守護者的榮耀);
                applyto.dispelEffect(烈焰巫師.守護者的榮耀);
                applyto.dispelEffect(破風使者.守護者的榮耀);
                applyto.dispelEffect(暗夜行者.守護者的榮耀);
                applyto.dispelEffect(閃雷悍將.守護者的榮耀);
                applyto.dispelEffect(Config.constants.skills.皇家騎士團_技能群組.米哈逸.明日女皇);
                return 1;
            }
        }
        return -1;
    }
}