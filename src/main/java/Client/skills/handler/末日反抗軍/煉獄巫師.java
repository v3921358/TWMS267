package Client.skills.handler.末日反抗軍;

import Client.MapleCharacter;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.HexaSKILL;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleSummon;
import Packet.SummonPacket;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.煉獄巫師.*;

public class 煉獄巫師 extends AbstractSkillHandler {

    public 煉獄巫師() {
        jobs = new MapleJob[]{
                MapleJob.煉獄巫師1轉,
                MapleJob.煉獄巫師2轉,
                MapleJob.煉獄巫師3轉,
                MapleJob.煉獄巫師4轉
        };

        for (Field field : Config.constants.skills.煉獄巫師.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 32141000:
                return 32001014;
            case HexaSKILL.強化聯盟光環:
                return 400021006;
            case HexaSKILL.強化黑魔祭壇:
                return 400021047;
            case HexaSKILL.強化冥界死神:
                return 400021069;
            case HexaSKILL.強化深淵閃電:
                return 400021087;
            case 急速之躍:
                return 瞬間移動_Ver2;
            case 黑暗閃電_1:
                return 黑暗閃電;
            case 黑暗世紀_1:
                return 黑暗世紀;
            case 鬥王杖擊_第2擊:
                return 鬥王杖擊;
            case 聯盟繩索_攻擊:
                return 聯盟繩索;
            case 深淵閃電_1:
            case 深淵閃電_2:
            case 深淵閃電_3:
                return 深淵閃電;
            case 緋紅契約_1:
                return 緋紅契約;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 黑暗閃電:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.DarkLighting, 1);
                return 1;
            case 死神:
            case 死神契約I:
            case 死神契約II:
            case 死神契約III:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.BMageDeath, 0);
                return 1;
            case 黃色光環:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.BMageAuraYellow, effect.getLevel());
                statups.put(SecondaryStat.IndieSpeed, effect.getInfo().get(MapleStatInfo.indieSpeed));
                statups.put(SecondaryStat.IndieBooster, effect.getInfo().get(MapleStatInfo.indieBooster));
                return 1;
            case 紅色光環:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.BMageAuraDrain, effect.getLevel());
                statups.put(SecondaryStat.AranDrain, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 藍色光環:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.DamAbsorbShield, effect.getInfo().get(MapleStatInfo.y));
                statups.put(SecondaryStat.IndieAsrR, effect.getInfo().get(MapleStatInfo.indieAsrR));
                statups.put(SecondaryStat.BMageAuraBlue, effect.getLevel());
                return 1;
            case 黑色光環:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                statups.put(SecondaryStat.BMageAuraDark, effect.getLevel());
                return 1;
            case 減益效果光環:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.BMageAuraDebuff, effect.getLevel());
                monsterStatus.put(MonsterStatus.BMageDebuff, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 煉獄鬥氣:
                statups.clear();
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.Enrage, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.EnrageCr, effect.getInfo().get(MapleStatInfo.z));
                statups.put(SecondaryStat.EnrageCrDamMin, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 魔法屏障:
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 長杖極速:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 黑色鎖鏈:
            case 黑暗世紀:
                monsterStatus.put(MonsterStatus.Seal, 1);
                return 1;
            case 瞬間移動爆發:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.TeleportMasteryRange, 1);
                return 1;
            case 死之奧義:
                statups.put(SecondaryStat.AttackCountX, 2);
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 自由意志:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 聯盟繩索:
                statups.put(SecondaryStat.BattlePvP_Helena_Mark, effect.getLevel());
                monsterStatus.put(MonsterStatus.BMageDebuff, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 黑魔祭壇:
                statups.put(SecondaryStat.CannonShooter_BFCannonBall, 0);
                return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 黃色光環:
            case 紅色光環:
            case 藍色光環:
            case 黑色光環:
            case 減益效果光環: {
                if ((applyto.getSkillEffect(黃色光環)) != null) {
                    applyto.dispelEffect(SecondaryStat.BMageAuraYellow);
                }
                if ((applyto.getSkillEffect(紅色光環)) != null) {
                    applyto.dispelEffect(SecondaryStat.BMageAuraDrain);
                }
                if ((applyto.getSkillEffect(藍色光環)) != null) {
                    applyto.dispelEffect(SecondaryStat.BMageAuraBlue);
                }
                if ((applyto.getSkillEffect(黑色光環)) != null) {
                    applyto.dispelEffect(SecondaryStat.BMageAuraDark);
                }
                if ((applyto.getSkillEffect(減益效果光環)) != null) {
                    applyto.dispelEffect(SecondaryStat.BMageAuraDebuff);
                }
                if (applier.effect.getSourceId() == 黑色光環 && applyto.getSkillEffect(黑色光環_魔王剋星) != null) {
                    applier.localstatups.put(SecondaryStat.IndieBDR, applier.effect.getIndieBDR());
                }
                return 1;
            }
            case 魔法屏障: {
                if (applier.primary) {
                    return 1;
                }
                return 0;
            }
            case 黑暗閃電: {
                if (applier.passive) {
                    return 0;
                }
                return 1;
            }
            case 死神:
            case 死神契約I:
            case 死神契約II:
            case 死神契約III: {
                int maxValue = 0;
                MapleStatEffect eff;
                if ((eff = applyto.getSkillEffect(死神)) != null) {
                    maxValue = eff.getX();
                }
                if ((eff = applyto.getSkillEffect(死神契約I)) != null) {
                    maxValue = eff.getX();
                }
                if ((eff = applyto.getSkillEffect(死神契約II)) != null) {
                    maxValue = eff.getX();
                }
                if ((eff = applyto.getSkillEffect(死神契約III)) != null) {
                    maxValue = eff.getX();
                }
                if (applyto.getEffectForBuffStat(SecondaryStat.AttackCountX) != null) {
                    maxValue = 1;
                }
                int value = applyto.getBuffedIntValue(SecondaryStat.BMageDeath);
                if (applyto.getBuffedValue(SecondaryStat.BMageDeath) != null) {
                    applier.localstatups.clear();
                    applier.applySummon = false;
                    ++value;
                }
                value = Math.min(value, maxValue);
                applier.localstatups.put(SecondaryStat.BMageDeath, value);
                if (value == maxValue && applyto.getCheatTracker().canNext死神契約()) {
                    final MapleSummon summon = applyto.getSummonBySkillID(applier.effect.getSourceId());
                    if (summon != null) {
                        applier.localstatups.put(SecondaryStat.BMageDeath, 0);
                        applyto.getClient().announce(SummonPacket.SummonedAssistAttackRequest(applyto.getId(), summon.getObjectId(), 0));
                    }
                }
                return 1;
            }
            case 自由意志: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(Config.constants.skills.惡魔復仇者.惡魔韌性);
                applyto.dispelEffect(Config.constants.skills.爆拳槍神.自由意志);
                applyto.dispelEffect(Config.constants.skills.煉獄巫師.自由意志);
                applyto.dispelEffect(Config.constants.skills.狂豹獵人.自由意志);
                applyto.dispelEffect(Config.constants.skills.機甲戰神.自由意志);
                return 1;
            }
            case 聯盟繩索: {
                MapleStatEffect eff;
                if ((eff = applyto.getSkillEffect(黃色光環)) != null) {
                    applier.localstatups.putAll(eff.getStatups());
                }
                if ((eff = applyto.getSkillEffect(紅色光環)) != null) {
                    applier.localstatups.putAll(eff.getStatups());
                }
                if ((eff = applyto.getSkillEffect(藍色光環)) != null) {
                    applier.localstatups.putAll(eff.getStatups());
                }
                if ((eff = applyto.getSkillEffect(黑色光環)) != null) {
                    applier.localstatups.putAll(eff.getStatups());
                }
                if ((eff = applyto.getSkillEffect(減益效果光環)) != null) {
                    applier.localstatups.putAll(eff.getStatups());
                }
                return 1;
            }
            case 黑魔祭壇: {
                applier.applySummon = !applier.passive;
                applier.localstatups.clear();
                if (applier.applySummon) {
                    applier.localstatups.put(SecondaryStat.IndieBuffIcon, 1);
                }
                final int value = applyto.getBuffedIntValue(SecondaryStat.CannonShooter_BFCannonBall) + (applier.passive ? 1 : -1);
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.CannonShooter_BFCannonBall);
                if (!applier.primary || value < 0 || mbsvh != null && System.currentTimeMillis() < mbsvh.startTime + 500L) {
                    return 0;
                }
                applier.maskedDuration = 2100000000;
                applier.maskedstatups.put(SecondaryStat.CannonShooter_BFCannonBall, value);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        final MapleStatEffect effecForBuffStat10;
        if ((effecForBuffStat10 = applyfrom.getEffectForBuffStat(SecondaryStat.BMageDeath)) != null && (applier.effect == null || applier.effect.getSourceId() != effecForBuffStat10.getSourceId())) {
            effecForBuffStat10.applyTo(applyfrom, true);
        }
        if (!applyto.isAlive()) {
            if (applyfrom.getSkillLevel(紅色光環) > 0) {
                MapleStatEffect eff = applyfrom.getSkillEffect(紅色光環);
                int toHeal = eff.getKillRecoveryR();
                applyfrom.addHPMP((int) ((toHeal / 100.0) * applyfrom.getStat().getCurrentMaxHP()), 0, false, true);
            }
        }
        return -1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        MapleStatEffect eff = player.getEffectForBuffStat(SecondaryStat.ComboDrain);
        if (applier.totalDamage > 0L && eff != null && player.getCheatTracker().canNextBonusAttack(5000)) {
            player.addHPMP((int) Math.min(player.getStat().getCurrentMaxHP() * 15L / 100, applier.totalDamage / 100L), 0, false, true);
        }
        return 1;
    }
}
