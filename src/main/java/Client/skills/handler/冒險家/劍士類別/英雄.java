package Client.skills.handler.冒險家.劍士類別;

import Client.*;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.神射手;
import Config.constants.skills.重砲指揮官;
import Config.constants.skills.開拓者;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Packet.BuffPacket;
import Packet.MaplePacketCreator;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static Config.constants.skills.冒險家_技能群組.type_劍士.英雄.*;

public class 英雄 extends AbstractSkillHandler {

    public 英雄() {
        jobs = new MapleJob[]{
                MapleJob.狂戰士,
                MapleJob.十字軍,
                MapleJob.英雄
        };

        for (Field field : Config.constants.skills.冒險家_技能群組.type_劍士.英雄.class.getDeclaredFields()) {
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
            case HEXA_強化燃燒靈魂之劍:
                return 燃燒靈魂之劍;
            case HEXA_強化空間斬:
                return 空間斬;
            case HEXA_強化劍之幻象:
                return 劍之幻象;
            case HEXA_強化鬥氣本能:
                return 鬥氣本能;
            case HEXA_狂暴攻擊_IV_延伸:
                return HEXA_狂暴攻擊_IV;
            case 1120013:
            case 狂暴攻擊_爆擊:
                return 狂暴攻擊;
            case 劍之幻象_1:
            case 劍之幻象_2:
            case 400011127:
                return 劍之幻象;
            case 劍士意念_1:
                return 劍士意念;
            case 燃燒靈魂之劍_1:
                return 燃燒靈魂之劍;
            case 空間斬_1:
                return 空間斬;
            case 鬥氣本能_1:
            case 鬥氣本能_2:
            case 鬥氣本能_3:
                return 鬥氣本能;
            case 1141501:
            case HEXA_聖劍降臨:
                return 80003365;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 鬥氣集中:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.ComboCounter, 1);
                return 1;
            case 激勵:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.IndiePowerGuard, effect.getInfo().get(MapleStatInfo.indiePowerGuard));
                return 1;
            case 傷痕之劍:
                statups.put(SecondaryStat.CrusaderPanic, 1);
                effect.setDebuffTime(effect.getV() * 20000);
                monsterStatus.put(MonsterStatus.Panic, 1);
                return 1;
            case 烈焰翔斬:
                monsterStatus.put(MonsterStatus.Incizing, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 魔防消除:
                monsterStatus.put(MonsterStatus.MagicCrash, 1);
                return 1;
            case 鬥氣爆發:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.Enrage, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.EnrageCr, effect.getInfo().get(MapleStatInfo.z));
                statups.put(SecondaryStat.EnrageCrDamMin, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 傳說冒險:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 劍士意念:
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.IndieCr, 30);
                statups.put(SecondaryStat.Stance, 100);
                statups.put(SecondaryStat.AsrR, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.TerR, effect.getInfo().get(MapleStatInfo.y));
                statups.put(SecondaryStat.ComboCounter, 11);
                return 1;
            case 劍士意念_1:
                effect.getInfo().put(MapleStatInfo.cooltimeMS, effect.getInfo().get(MapleStatInfo.subTime));
                return 1;
            case 空間斬:
                effect.getInfo().put(MapleStatInfo.time, 1680);
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
            case 空間斬_1:
                statups.put(SecondaryStat.IndieDamReduceR, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 鬥氣本能:
                statups.put(SecondaryStat.HeroComboInstinct, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case HEXA_聖劍降臨:
                effect.getInfo().put(MapleStatInfo.time, 7000);
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 燃燒靈魂之劍) {
            if (!chr.isSkillCooling(燃燒靈魂之劍)) {
                chr.registerSkillCooldown(燃燒靈魂之劍, applier.effect.getX() * 1000, true);
                return 1;
            }
            final SecondaryStatValueHolder buffStatValueHolder2;
            if ((buffStatValueHolder2 = chr.getBuffStatValueHolder(燃燒靈魂之劍)) != null && chr.getSummonBySkillID(燃燒靈魂之劍) != null) {
                final int w = buffStatValueHolder2.getLeftTime();
                chr.removeSummonBySkillID(燃燒靈魂之劍, 5);
                final MapleStatEffect skillEffect7;
                if ((skillEffect7 = chr.getSkillEffect(燃燒靈魂之劍_1)) != null) {
                    skillEffect7.applyTo(chr, w);
                }
                return 0;
            }
            final SecondaryStatValueHolder buffStatValueHolder3;
            if ((buffStatValueHolder3 = chr.getBuffStatValueHolder(燃燒靈魂之劍_1)) != null && chr.getSummonBySkillID(燃燒靈魂之劍_1) != null) {
                chr.removeSummonBySkillID(燃燒靈魂之劍_1, 5);
                final MapleStatEffect skillEffect8 = chr.getSkillEffect(燃燒靈魂之劍);
                final int w2 = buffStatValueHolder3.getLeftTime();
                if (skillEffect8 != null) {
                    skillEffect8.applyTo(chr, w2);
                }
                return 0;
            }
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 劍士意念: {
                applier.buffz = applier.effect.getU2();
                return 1;
            }
            case 傳說冒險: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(Config.constants.skills.冒險家_技能群組.type_劍士.英雄.傳說冒險);
                applyto.dispelEffect(聖騎士.傳說冒險);
                applyto.dispelEffect(黑騎士.傳說冒險);
                applyto.dispelEffect(火毒.傳說冒險);
                applyto.dispelEffect(冰雷.傳說冒險);
                applyto.dispelEffect(主教.傳說冒險);
                applyto.dispelEffect(箭神.傳說冒險);
                applyto.dispelEffect(神射手.傳說冒險);
                applyto.dispelEffect(開拓者.傳說冒險);
                applyto.dispelEffect(暗影神偷.傳說冒險);
                applyto.dispelEffect(夜使者.傳說冒險);
                applyto.dispelEffect(影武者.傳說冒險);
                applyto.dispelEffect(拳霸.傳說冒險);
                applyto.dispelEffect(槍神.傳說冒險);
                applyto.dispelEffect(重砲指揮官.傳說冒險);
                return 1;
            }
            case 燃燒靈魂之劍: {
                MapleStatEffect eff = applyfrom.getSkillEffect(進階鬥氣);
                if (eff == null) {
                    eff = applyfrom.getSkillEffect(鬥氣集中);
                }
                SecondaryStatValueHolder mbsvh = applyfrom.getBuffStatValueHolder(SecondaryStat.ComboCounter);
                if (eff != null && mbsvh != null && mbsvh.value < eff.getX()) {
                    mbsvh.value = eff.getX();
                    applyfrom.send(BuffPacket.giveBuff(applyfrom, mbsvh.effect, Collections.singletonMap(SecondaryStat.ComboCounter, mbsvh.effect.getSourceId())));
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        MapleStatEffect effect;
        if ((effect = applyfrom.getEffectForBuffStat(SecondaryStat.CrusaderPanic)) != null && effect.makeChanceResult(applyfrom)) {
            effect.applyMonsterEffect(applyfrom, applyto, effect.getMobDebuffDuration(applyfrom));
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        MapleStatEffect effect;
        SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.ComboCounter);
        if (mbsvh != null && mbsvh.effect != null && applier.totalDamage > 0L && applier.ai.skillId != 鬥氣本能_1 && applier.ai.skillId != 鬥氣本能_2 && applier.ai.skillId != 鬥氣本能_3) {
            effect = player.getSkillEffect(鬥氣綜合);
            MapleStatEffect effectEnchant = player.getSkillEffect(進階鬥氣);
            if (effect == null) {
                effect = player.getSkillEffect(鬥氣集中);
            }
            if (effect != null) {
                int maxCombo = effectEnchant != null ? effectEnchant.getX() : effect.getX();
                if (mbsvh.value < maxCombo + 1 && effect.makeChanceResult(player)) {
                    mbsvh.value += 1;
                    if (effectEnchant != null && effectEnchant.makeChanceResult(player) && mbsvh.value < maxCombo + 1) {
                        mbsvh.value += 1;
                    }
                    player.send(BuffPacket.giveBuff(player, mbsvh.effect, Collections.singletonMap(SecondaryStat.ComboCounter, mbsvh.effect.getSourceId())));
                }
            }
        }

        mbsvh = player.getBuffStatValueHolder(SecondaryStat.Stance);
        if (mbsvh != null && applier.ai.mobAttackInfo.size() > 0 && mbsvh.effect != null && mbsvh.effect.getSourceId() == 劍士意念 && mbsvh.z > 0 && (effect = player.getSkillEffect(劍士意念_1)) != null && !player.isSkillCooling(劍士意念_1)) {
            MapleMonster mob = player.getMap().getMobObject(applier.ai.mobAttackInfo.get(0).mobId);
            if (mob != null) {
                mbsvh.z--;
                player.send(BuffPacket.giveBuff(player, mbsvh.effect, Collections.singletonMap(SecondaryStat.Stance, mbsvh.effect.getSourceId())));
                effect.applyTo(player, mob.getPosition(), true);
            }
        }

        if (applier.totalDamage > 0L && applier.effect != null && applier.effect.getSourceId() == 空間斬) {
            player.getSkillEffect(空間斬_1).applyTo(player, true);
        }

        if (player.getEffectForBuffStat(SecondaryStat.HeroComboInstinct) != null && player.getCheatTracker().canNextBonusAttack(3000)) {
            player.getClient().announce(MaplePacketCreator.userBonusAttackRequest(鬥氣本能_1, 0, Collections.emptyList()));
            player.getClient().announce(MaplePacketCreator.userBonusAttackRequest(鬥氣本能_2, 0, Collections.emptyList()));
            player.getClient().announce(MaplePacketCreator.userBonusAttackRequest(鬥氣本能_3, 0, Collections.emptyList()));
        }
        return 1;
    }
}
