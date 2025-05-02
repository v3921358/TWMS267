package Client;

import Client.skills.Skill;
import Client.skills.SkillFactory;
import Client.stat.PlayerStats;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Config.constants.SkillConstants;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.夜使者;
import Config.constants.skills.冒險家_技能群組.箭神;
import Config.constants.skills.皇家騎士團_技能群組.米哈逸;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.Element;
import Net.server.life.ElementalEffectiveness;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMapObjectType;
import Server.channel.handler.AttackInfo;
import tools.newCRand32;
import tools.Randomizer;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Objects;

public class CalcDamage {

    private final newCRand32 rand = new newCRand32();

    public final void setSeed(int s1, int s2, int s3) {
        this.rand.seed(s1, s2, s3);
    }

    public static double randomInRange(final long randomNum, long max, long min) {
        final double newRandNum = randomNum - (new BigInteger(String.valueOf(randomNum)).multiply(new BigInteger("1801439851")).shiftRight(32).longValue() >>> 22) * 1.0E7;
        double value;
        if (min != max) {
            if (min > max) {
                final long temp = max;
                max = min;
                min = temp;
            }
            value = (max - min) * newRandNum / 9999999.0 + min;
        } else {
            value = max;
        }
        return value;
    }

    public final long random() {
        return this.rand.random();
    }

    public final long getRandomDamage(MapleCharacter chr, boolean crit) {
        final long[] array = new long[11];
        for (int i = 0; i < 11; ++i) {
            array[i] = this.rand.random();
        }
        final boolean cr2 = randomInRange(array[Randomizer.nextInt(11)], 100L, 0L) < chr.getStat().critRate;
        double damage = randomInRange(array[Randomizer.nextInt(11)], chr.getStat().getCurrentMaxBaseDamage(), chr.getStat().getCurrentMinBaseDamage());
        if (crit || cr2) {
            damage += damage * (randomInRange(array[Randomizer.nextInt(11)], 20L, 50L) + chr.getStat().criticalDamage) / 100.0;
        }
        return (long) Math.min(damage, 10000000000L);
    }

    public final double calcDamage(final MapleCharacter chr, final AttackInfo ai, final int idx, final MapleMonster monster, final boolean isBoss) {
        return this.calcDamage(chr, ai, idx, chr.getStat().getCurrentMaxBaseDamage(), monster, isBoss, false);
    }

    public final double calcDamage(final MapleCharacter chr, final AttackInfo ai, final int idx, double maxBaseDamage, final MapleMonster monster, final boolean isBoss, final boolean isCritical) {
        long limitBreak = 10000000000L;
        double elementDamR = 1.0;
        final PlayerStats stat = chr.getStat();
        if (ai.skillId > 0) {
            final int skillLevel = chr.getSkillLevel(SkillConstants.getLinkedAttackSkill(ai.skillId));
            final Skill skill = SkillFactory.getSkill(ai.skillId);
            assert skill != null;
            limitBreak = Math.max(limitBreak, skill.getMaxDamageOver());
            MapleStatEffect effect = skill.getEffect(skillLevel);
            final Element element = skill.getElement();
            if (element != null) {
                final ElementalEffectiveness effectiveness = monster.getStats().getEffectiveness(element);
                if (Objects.requireNonNull(effectiveness) == ElementalEffectiveness.免疫) {
                    elementDamR = (1.0 + chr.getStat().ignoreElement) / 100.0;
                } else {
                    double eiValue = effectiveness.getValue();
                    eiValue += eiValue * chr.getStat().ignoreElement / 100.0;
                    switch (element) {
                        case 火: {
                            elementDamR = eiValue * (stat.getElementFire() + stat.getElementBoost(element)) / 100.0;
                            break;
                        }
                        case 冰: {
                            elementDamR = eiValue * (stat.getElementIce() + stat.getElementBoost(element)) / 100.0;
                            break;
                        }
                        case 雷: {
                            elementDamR = eiValue * (stat.getElementLight() + stat.getElementBoost(element)) / 100.0;
                            break;
                        }
                        case 毒: {
                            elementDamR = eiValue * (stat.getElementPsn() + stat.getElementBoost(element)) / 100.0;
                            break;
                        }
                        default: {
                            elementDamR = eiValue * (stat.getElementDef() + stat.getElementBoost(element)) / 100.0;
                            break;
                        }
                    }
                }
            }
            if (effect != null) {
                double skillDamR = effect.getDamage() + effect.getDamage() * stat.getSkillDamageIncrease(ai.skillId) / 100.0;
                switch (ai.skillId) {
                    case 黑騎士.拉曼查之槍: {
                        if (ai.unInt1 > 0) {
                            skillDamR = effect.getY();
                            break;
                        }
                        break;
                    }
                    case 神射手.光速神弩: {
                        skillDamR += effect.getDamage() * (idx * 20.0) / 100.0;
                        break;
                    }
                    case 夜使者.刺客刻印_飛鏢:
                    case 夜使者.夜使者的標記: {
                        skillDamR += effect.getX() * chr.getLevel();
                        break;
                    }
                    case 箭神.疾風箭矢: {
                        skillDamR = effect.getX();
                        break;
                    }
                    case 龍魔導士.魔法殘骸:
                    case 龍魔導士.強化的魔法殘骸: {
                        skillDamR += skillDamR * effect.getW() * 3.0 / 100.0;
                        break;
                    }
                    case 狂豹獵人.召喚美洲豹_銀灰:
                    case 狂豹獵人.召喚美洲豹_暗黃:
                    case 狂豹獵人.召喚美洲豹_血紅:
                    case 狂豹獵人.召喚美洲豹_紫光:
                    case 狂豹獵人.召喚美洲豹_深藍:
                    case 狂豹獵人.召喚美洲豹_傑拉:
                    case 狂豹獵人.召喚美洲豹_白雪:
                    case 狂豹獵人.召喚美洲豹_歐尼斯:
                    case 狂豹獵人.召喚美洲豹_地獄裝甲:
                    case 狂豹獵人.爪攻擊:
                    case 狂豹獵人.歧路:
                    case 狂豹獵人.音暴:
                    case 狂豹獵人.美洲豹靈魂:
                    case 狂豹獵人.閃光雨:
                    case 狂豹獵人.狂豹之怒: {
                        skillDamR = effect.getY() + Math.min(chr.getLevel(), 180) * effect.getDamage();
                        break;
                    }
                    case 狂豹獵人.另一個咬擊: {
                        skillDamR = effect.getY() + Math.min(chr.getLevel(), 180) * effect.getDamage();
                        break;
                    }
                    case 機甲戰神.戰鬥機器_巨人錘: {
                        if (ai.attackType == AttackInfo.AttackType.BodyAttack) {
                            skillDamR = effect.getY() + effect.getY() * stat.getSkillDamageIncrease(ai.skillId) * 1.2 / 100.0;
                            break;
                        }
                        if (ai.attackType == AttackInfo.AttackType.SummonedAttack) {
                            skillDamR *= 2.0;
                            break;
                        }
                        break;
                    }
                    case 機甲戰神.機器人工廠_RM1: {
                        if (chr.getSkillEffect(ai.skillId) != null) {
                            effect = chr.getSkillEffect(ai.skillId);
                        }
                        skillDamR = effect.getSelfDestruction() + effect.getSelfDestruction() * stat.getSkillDamageIncrease(ai.skillId) / 100.0;
                        break;
                    }
                    case 傑諾.追縱火箭: {
                        skillDamR *= 2.0;
                        break;
                    }
                    case 傑諾.能量領域_貫通: {
                        if (chr.getSkillEffect(ai.skillId) != null) {
                            effect = chr.getSkillEffect(ai.skillId);
                        }
                        skillDamR = effect.getDamage() + effect.getDamage() * stat.getSkillDamageIncrease(ai.skillId) / 100.0;
                        break;
                    }
                    case 米哈逸.皇家之盾_6:
                    case 米哈逸.皇家之盾_7:
                    case 米哈逸.皇家之盾_8:
                    case 米哈逸.皇家之盾_9:
                    case 米哈逸.皇家之盾_10:
                    case 皮卡啾.皮卡啾攻擊:
                    case 皮卡啾.咕嚕咕嚕:
                    case 皮卡啾.皮卡啾攻擊_4:
                    case 皮卡啾.皮卡啾攻擊_5:
                    case 皮卡啾.皮卡啾攻擊_6:
                    case 皮卡啾.咕嚕咕嚕_1:
                    case 皮卡啾.天空豆豆地上: {
                        skillDamR = (effect = skill.getEffect(chr.getLevel())).getDamage() + effect.getDamage() * stat.getSkillDamageIncrease(ai.skillId) / 100.0;
                        break;
                    }
                    case 皮卡啾.皮卡啾的品格: {
                        skillDamR = (effect = SkillFactory.getSkill(皮卡啾.皮卡啾的品格傷害).getEffect(chr.getLevel())).getDamage() + effect.getDamage() * stat.getSkillDamageIncrease(ai.skillId) / 100.0;
                        break;
                    }
                    case 惡魔殺手.惡魔覺醒_1:
                    case 惡魔殺手.惡魔覺醒_2:
                    case 惡魔殺手.惡魔覺醒_3:
                    case 惡魔殺手.惡魔覺醒_4: {
                        skillDamR *= 2.0;
                        break;
                    }
                    case 機甲戰神.多重屬性_M_FL:
                    case 機甲戰神.多重屬性_M_FL_1: {
                        skillDamR *= 1.5;
                        break;
                    }
                    case 陰陽師.雪女招喚: {
                        skillDamR = (effect = SkillFactory.getSkill(陰陽師.雪女招喚_1).getEffect(chr.getLevel())).getDamage() + effect.getDamage() * stat.getSkillDamageIncrease(ai.skillId) / 100.0;
                        break;
                    }
                    case 凱撒.超新星守護者: {
                        skillDamR = (effect = SkillFactory.getSkill(ai.skillId).getEffect(chr.getLevel())).getDamage() + effect.getDamage() * stat.getSkillDamageIncrease(ai.skillId) / 100.0;
                        break;
                    }
                    case 隱月.鬼武陣:
                    case 隱月.鬼武陣_1: {
                        skillDamR = effect.getV();
                        break;
                    }
                    case 爆拳槍神.錘之碎擊_1: {
                        skillDamR = effect.getQ2() + effect.getQ2() * stat.getSkillDamageIncrease(ai.skillId) / 100.0;
                        break;
                    }
                }
                final MapleStatEffect skillEffect;
                //if (JobConstants.is神之子(chr.getJob()) && chr.isBeta() && (skillEffect = chr.getSkillEffect(神之子.琉之力)) != null && ai.mobCount < effect.getMobCount(chr)) {
                //    skillDamR += (effect.getMobCount(chr) - ai.mobCount) * skillEffect.getMobCountDamage();
                //}
                if (JobConstants.is卡蒂娜(chr.getJob())) {
                    final Integer buffedValue = chr.getBuffedValue(SecondaryStat.WeaponVariety);
                    final MapleStatEffect effecForBuffStat = chr.getEffectForBuffStat(SecondaryStat.WeaponVariety);
                    if (buffedValue != null && effecForBuffStat != null) {
                        skillDamR *= skillDamR * effecForBuffStat.getX() / 100.0;
                    }
                }
                maxBaseDamage = maxBaseDamage * skillDamR / 100.0;
            }
        }
        Skill skill = SkillFactory.getSkill(英雄.伺機攻擊);
        MapleStatEffect effect = skill.getEffect(chr.getSkillLevel(skill));
        if (effect != null && (monster.getEffectHolder(MonsterStatus.Stun) != null || monster.getEffectHolder(MonsterStatus.Darkness) != null || monster.getEffectHolder(MonsterStatus.Freeze) != null)) {
            maxBaseDamage *= (100.0 + effect.getX()) / 100.0;
        }
        if (JobConstants.is冒險家法師(chr.getJob())) {
            if (chr.getJob() >= 211 && chr.getJob() <= 212) {
                skill = SkillFactory.getSkill(火毒.終極魔法_火毒);
            } else if (chr.getJob() >= 221 && chr.getJob() <= 222) {
                skill = SkillFactory.getSkill(冰雷.終極魔法_雷冰);
            }
            assert skill != null;
            if ((effect = skill.getEffect(chr.getSkillLevel(skill))) != null && (monster.getEffectHolder(MonsterStatus.Burned) != null || monster.getEffectHolder(MonsterStatus.Speed) != null || monster.getEffectHolder(MonsterStatus.Stun) != null || monster.getEffectHolder(MonsterStatus.Darkness) != null || monster.getEffectHolder(MonsterStatus.Freeze) != null)) {
                maxBaseDamage *= (100.0 + effect.getZ()) / 100.0;
            }
        }
        if (JobConstants.is惡魔殺手(chr.getJob()) && (effect = chr.getSkillEffect(惡魔殺手.邪惡酷刑)) != null && (monster.getEffectHolder(MonsterStatus.Burned) != null || monster.getEffectHolder(MonsterStatus.Speed) != null || monster.getEffectHolder(MonsterStatus.Stun) != null || monster.getEffectHolder(MonsterStatus.Darkness) != null || monster.getEffectHolder(MonsterStatus.Freeze) != null)) {
            maxBaseDamage *= (100.0 + effect.getX()) / 100.0;
        }
        if (JobConstants.is米哈逸(chr.getJob()) && (effect = chr.getSkillEffect(米哈逸.靈魂重擊)) != null && (monster.getEffectHolder(MonsterStatus.Burned) != null || monster.getEffectHolder(MonsterStatus.Speed) != null || monster.getEffectHolder(MonsterStatus.Stun) != null || monster.getEffectHolder(MonsterStatus.Darkness) != null || monster.getEffectHolder(MonsterStatus.Freeze) != null)) {
            maxBaseDamage *= (100.0 + effect.getX()) / 100.0;
        }
        MonsterEffectHolder holder = monster.getEffectHolder(MonsterStatus.AddDamSkill2);
        if (holder != null) {
            maxBaseDamage += maxBaseDamage * holder.value / 100.0;
        }
        if ((holder = monster.getEffectHolder(MonsterStatus.TotalDamParty)) != null) {
            maxBaseDamage += maxBaseDamage * holder.value / 100.0;
        }
        if ((effect = chr.getEffectForBuffStat(SecondaryStat.GuidedBullet)) != null) {
            maxBaseDamage += maxBaseDamage * effect.getX() / 100.0;
        }
        if ((holder = monster.getEffectHolder(MonsterStatus.TotalDamParty)) != null) {
            maxBaseDamage += maxBaseDamage * holder.value / 100.0;
        }
        if (JobConstants.is箭神(chr.getJob())) {
            effect = chr.getEffectForBuffStat(SecondaryStat.BowMasterMortalBlow);
            final int buffedIntValue = chr.getBuffedIntValue(SecondaryStat.BowMasterMortalBlow);
            if (effect != null && buffedIntValue >= effect.getX()) {
                maxBaseDamage += maxBaseDamage * effect.getY() / 100.0;
            }
        } else if (JobConstants.is神射手(chr.getJob())) {
            double incFinalDam = 1.0;
            if ((effect = chr.getSkillEffect(神射手.天降羽箭)) != null) {
                double maxfd = effect.getDamR();
                incFinalDam += incFinalDam * maxfd / 100.0;
            }
            if ((effect = chr.getSkillEffect(神射手.永不屈服)) != null && chr.getMap().getMapObjectsInRange(monster.getPosition(), 80, Collections.singletonList(MapleMapObjectType.MONSTER)).size() <= 1) {
                incFinalDam += incFinalDam * effect.getDamR() / 100.0;
            }
            maxBaseDamage += maxBaseDamage * incFinalDam;
        }
        final double mobPDR = monster.getStats().getPDRate();
        final double counteredDamR = Math.max(0, 100.0 - (mobPDR - mobPDR * chr.getStat().getIgnoreMobpdpR(ai.skillId) / 100.0));
        final double crDam = 50 + stat.criticalDamage;
        if (isCritical) {
            maxBaseDamage += maxBaseDamage * crDam / 100.0;
        }
        if (isBoss) {
            maxBaseDamage += maxBaseDamage * stat.bossDamageR / (100.0 + stat.incDamR);
        }
        maxBaseDamage *= elementDamR;
        maxBaseDamage += maxBaseDamage * stat.getSkillDamageIncrease_5th(SkillConstants.getLinkedAttackSkill(ai.skillId)) / 100.0;
        maxBaseDamage = Math.max(1, maxBaseDamage * (counteredDamR / 100.0));
        int dl = chr.getLevel() - monster.getMobLevel();
        if (dl > 20) {
            maxBaseDamage *= 1.2;
        } else if (dl < -20) {
            maxBaseDamage *= 0.8;
        } else {
            maxBaseDamage += maxBaseDamage * (dl / 100.0);
        }
        return Math.min(Math.max(1, maxBaseDamage), limitBreak);
    }

}
