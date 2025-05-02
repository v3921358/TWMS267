package Client.skills.handler.冒險家.海盜類別;

import Client.*;
import Client.skills.ExtraSkill;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.拳霸;
import Config.constants.skills.冒險家_技能群組.槍神;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.神射手;
import Config.constants.skills.通用V核心;
import Config.constants.skills.開拓者;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Opcode.Headler.OutHeader;
import Packet.EffectPacket;
import Packet.MaplePacketCreator;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static Config.constants.skills.重砲指揮官.*;

public class 重砲指揮官 extends AbstractSkillHandler {

    public 重砲指揮官() {
        jobs = new MapleJob[]{
                MapleJob.砲手,
                MapleJob.重砲兵,
                MapleJob.重砲兵隊長,
                MapleJob.重砲指揮官
        };

        for (Field field : Config.constants.skills.重砲指揮官.class.getDeclaredFields()) {
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
            case HEXA_加農砲連擊_VI:
                return 加農砲連擊;
            case HEXA_究極炸裂加農砲_II:
                return HEXA_究極炸裂加農砲;
            case 火藥桶破壞_爆炸:
                return 火藥桶破壞;
            case 幸運木桶_1:
                return 幸運木桶;
            case 狂爆猴子_爆炸:
                return 狂暴猴子;
            case 迷你砲彈_1:
            case 迷你砲彈_2:
                return 迷你砲彈;
            case 雙胞胎猴子_1:
                return 雙胞胎猴子;
            case ICBM_2:
            case ICBM_3:
                return ICBM;
            case 特種猴子部隊_1:
            case 特種猴子部隊_2:
            case 特種猴子部隊_3:
                return 特種猴子部隊;
            case 精準轟炸_1:
            case 精準轟炸_2:
            case 精準轟炸_3:
                return 精準轟炸;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 緊急退後:
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.z));
                return 1;
            case 猴子的魔法:
            case 神聖猴子的咒語:
                statups.put(SecondaryStat.IndieMHP, effect.getInfo().get(MapleStatInfo.indieMhp));
                statups.put(SecondaryStat.IndieMMP, effect.getInfo().get(MapleStatInfo.indieMmp));
                statups.put(SecondaryStat.IndieACC, effect.getInfo().get(MapleStatInfo.indieAcc));
                statups.put(SecondaryStat.IndieEVA, effect.getInfo().get(MapleStatInfo.indieEva));
                statups.put(SecondaryStat.IndieJump, effect.getInfo().get(MapleStatInfo.indieJump));
                statups.put(SecondaryStat.IndieSpeed, effect.getInfo().get(MapleStatInfo.indieSpeed));
                statups.put(SecondaryStat.IndieAllStat, effect.getInfo().get(MapleStatInfo.indieAllStat));
                return 1;
            case 狂暴猴子:
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 幸運木桶:
                effect.setDebuffTime(effect.getV() * 1000);
                monsterStatus.put(MonsterStatus.Burned, 1);
                monsterStatus.put(MonsterStatus.IndieSlow, 90);

                statups.put(SecondaryStat.IndieCD, 0);
                statups.put(SecondaryStat.Roulette, 0);
                return 1;
            case 幸運骰子:
            case 雙倍幸運骰子:
                statups.put(SecondaryStat.Dice, 0);
                return 1;
            case 迷你砲彈:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.CannonShooter_MiniCannonBall, 0);
                return 1;
            case 磁錨:
                effect.setDebuffTime(effect.getSubTime() * 1000);
                monsterStatus.put(MonsterStatus.Stun, 1);

                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 雙胞胎猴子:
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 傳說冒險:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 壓制砲擊:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.IndieBooster, effect.getInfo().get(MapleStatInfo.indieSpeed));
                statups.put(SecondaryStat.BuckShot, 1);
                return 1;
            case ICBM:
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
            case 超級巨型加農砲彈:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.CannonShooter_BFCannonBall, 0);
                return 1;
            case 精準轟炸_3:
                statups.put(SecondaryStat.IndieDamR, effect.getIndieDamR());
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 與猴子並肩作戰: {
                String statData = chr.getOneInfo(7786, "sw");
                if (statData == null || statData.equals("0")) {
                    statData = String.valueOf(1);
                } else {
                    statData = String.valueOf(0);
                }
                chr.updateOneInfo(7786, "sw", statData, true);
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.CallMonkey);
                mplew.writeInt(chr.getId());
                mplew.write(Integer.parseInt(statData));
                chr.getMap().broadcastMessage(mplew.getPacket());
                return 0;
            }
            case 磁錨:
            case 雙胞胎猴子:
            case 特種猴子部隊: {
                applier.pos = slea.readPos();
                return 1;
            }
            case ICBM_2: {
                applier.pos = new Point(slea.readPosInt());
                c.announce(MaplePacketCreator.UserCreateAreaDotInfo(c.getPlayer().getMap().getAndAddObjectId(), applier.effect.getSourceId(), applier.effect.calculateBoundingBox(applier.pos, true)));
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 回家_加農砲: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 幸運木桶: {
                int dice = Randomizer.nextInt(4) + 1;
                applyto.getClient().announce(EffectPacket.showDiceEffect(-1, applier.effect.getSourceId(), applier.effect.getLevel(), dice, -1, false));
                applyto.getMap().broadcastMessage(applyto, EffectPacket.showDiceEffect(applyto.getId(), applier.effect.getSourceId(), applier.effect.getLevel(), dice, -1, false), false);
                if (dice == 2) {
                    applier.localstatups.put(SecondaryStat.IndieCD, applier.effect.getS());
                }
                applier.localstatups.put(SecondaryStat.Roulette, dice);
                return 1;
            }
            case 幸運骰子: {
                int dice = Randomizer.nextInt(6) + 1;
                if (applyto.getSpecialStat().getRemoteDice() > 0) {
                    dice = applyto.getSpecialStat().getRemoteDice();
                    applyto.getSpecialStat().setRemoteDice(-1);
                }
                if (dice == 1) {
                    applyto.reduceSkillCooldown(幸運骰子, 90000);
                }
                applyto.send(MaplePacketCreator.spouseMessage(UserChatMessageType.系統, "幸運骰子 技能發動[" + dice + "]號效果。"));
                applier.localstatups.put(SecondaryStat.Dice, dice);
                applyto.getClient().announce(EffectPacket.showDiceEffect(-1, applier.effect.getSourceId(), applier.effect.getLevel(), dice, -1, false));
                applyto.getMap().broadcastMessage(applyto, EffectPacket.showDiceEffect(applyto.getId(), applier.effect.getSourceId(), applier.effect.getLevel(), dice, -1, false), false);
                return 1;
            }
            case 雙倍幸運骰子: {
                int remote = 0;
                int trueSource = applier.effect.getSourceId();
                int trueLevel = applier.effect.getLevel();
                MapleStatEffect effect = applyfrom.getSkillEffect(通用V核心.海盜通用.滿載骰子);
                if (effect != null) {
                    remote = applyfrom.getBuffedIntValue(SecondaryStat.LoadedDice);
                    trueSource = effect.getSourceId();
                    trueLevel = effect.getLevel();
                }
                int[] array = new int[1 + (trueSource == 通用V核心.海盜通用.滿載骰子 ? 1 : 0) + (applier.effect.makeChanceResult(applyto) ? 1 : 0)];
                for (int i = 0; i < array.length; i++) {
                    if (i == 0 && remote > 0) {
                        array[i] = remote;
                    } else {
                        array[i] = Randomizer.rand(1, 6);
                        if (array.length == 3 && array[0] == array[1] && array[1] == array[2] && Randomizer.isSuccess(50)) {
                            array[i] = Randomizer.rand(1, 6);
                        }
                    }
                }
                int buffId = 0;
                for (int i = 0; i < array.length; i++) {
                    if (array[i] == 1) {
                        applyto.reduceSkillCooldown(雙倍幸運骰子, 90000);
                    }
                    buffId += array[i] * (int) Math.pow(10, i);
                    if (array[i] > 0) {
                        applyto.send(MaplePacketCreator.spouseMessage(UserChatMessageType.系統, "雙倍幸運骰子 技能發動[" + array[i] + "]號效果。"));
                    }
                }
                if (trueSource == 通用V核心.海盜通用.滿載骰子) {
                    applyto.getClient().announce(EffectPacket.showDiceEffect(-1, trueSource, trueLevel, -1, 1, false));
                    applyto.getMap().broadcastMessage(applyto, EffectPacket.showDiceEffect(applyto.getId(), trueSource, trueLevel, -1, 1, false), false);
                    for (int i = 0; i < array.length; i++) {
                        if (array[i] > 0) {
                            applyto.getClient().announce(EffectPacket.showDiceEffect(-1, trueSource, trueLevel, array[i], i == array.length - 1 ? 0 : -1, i != 0));
                            applyto.getMap().broadcastMessage(applyto, EffectPacket.showDiceEffect(applyto.getId(), trueSource, trueLevel, array[i], i == array.length - 1 ? 0 : -1, i != 0), false);
                        }
                    }
                    applyto.getClient().announce(EffectPacket.showDiceEffect(-1, trueSource, trueLevel, -1, 2, false));
                    applyto.getMap().broadcastMessage(applyto, EffectPacket.showDiceEffect(applyto.getId(), trueSource, trueLevel, -1, 2, false), false);
                } else {
                    applyto.getClient().announce(EffectPacket.showDiceEffect(-1, trueSource, trueLevel, buffId, -1, false));
                    applyto.getMap().broadcastMessage(applyto, EffectPacket.showDiceEffect(applyto.getId(), trueSource, trueLevel, buffId, -1, false), false);
                }
                applier.localstatups.put(SecondaryStat.Dice, buffId);
                return 1;
            }
            case 迷你砲彈: {
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.CannonShooter_MiniCannonBall);
                final int value = (mbsvh == null ? 0 : mbsvh.value) + (applier.passive ? 1 : -1);
                if (!applier.primary || value < 0 || value > applier.effect.getY() || (mbsvh != null && applier.passive && System.currentTimeMillis() < mbsvh.startTime + applier.effect.getW() * 1000L)) {
                    return 0;
                }
                applier.localstatups.put(SecondaryStat.CannonShooter_MiniCannonBall, value);
                if (!applier.passive) {
                    ExtraSkill eskill = new ExtraSkill(!applier.att ? 迷你砲彈_1 : 迷你砲彈_2, applier.pos);
                    eskill.FaceLeft = eskill.SkillID == 迷你砲彈_1 ? applyfrom.isFacingLeft() ? 1 : 0 : -1;
                    eskill.Delay = eskill.SkillID == 迷你砲彈_1 ? 330 : 360;
                    eskill.Value = 1;
                    applyfrom.send(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), Collections.singletonList(eskill)));
                }
                return 1;
            }
            case 傳說冒險: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(英雄.傳說冒險);
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
                applyto.dispelEffect(Config.constants.skills.重砲指揮官.傳說冒險);
                return 1;
            }
            case 壓制砲擊: {
                if (applyto.getBuffStatValueHolder(applier.effect.getSourceId()) != null) {
                    applyto.dispelEffect(applier.effect.getSourceId());
                    return 0;
                }
                return 1;
            }
            case 超級巨型加農砲彈: {
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.CannonShooter_BFCannonBall);
                final int value = (mbsvh == null ? 0 : mbsvh.value) + (applier.passive ? 1 : -1);
                if (!applier.primary || value < 0 || value > applier.effect.getY() || (mbsvh != null && applier.passive && System.currentTimeMillis() < mbsvh.startTime + applier.effect.getQ() * 1000L)) {
                    return 0;
                }
                applier.localstatups.put(SecondaryStat.CannonShooter_BFCannonBall, value);
                return 1;
            }
            case 精準轟炸: {
                applyto.getSpecialStat().setPoolMakerCount(applier.effect.getY());
                applyto.send(MaplePacketCreator.poolMakerInfo(true, applyto.getSpecialStat().getPoolMakerCount(), applier.effect.getCooldown()));
                return 1;
            }

        }
        return -1;
    }

    @Override
    public int onApplySummonEffect(final MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 雙胞胎猴子: {
                applier.pos.x += 25;
                Skill skill = SkillFactory.getSkill(雙胞胎猴子_1);
                if (skill != null) {
                    MapleStatEffect effect = skill.getEffect(applier.effect.getLevel());
                    if (effect != null) {
                        effect.applySummonEffect(applyto, new Point(applier.pos.x - 90, applier.pos.y), applier.duration, applyto.getSpecialStat().getMaelstromMoboid(), applier.startTime);
                    }
                }
                return 1;
            }
            case 特種猴子部隊: {
                Skill skill = SkillFactory.getSkill(特種猴子部隊_2);
                if (skill != null) {
                    MapleStatEffect effect = skill.getEffect(applier.effect.getLevel());
                    if (effect != null) {
                        effect.applySummonEffect(applyto, applier.pos, applier.duration, applyto.getSpecialStat().getMaelstromMoboid(), applier.startTime);
                    }
                }
                skill = SkillFactory.getSkill(特種猴子部隊_3);
                if (skill != null) {
                    MapleStatEffect effect = skill.getEffect(applier.effect.getLevel());
                    if (effect != null) {
                        effect.applySummonEffect(applyto, applier.pos, applier.duration, applyto.getSpecialStat().getMaelstromMoboid(), applier.startTime);
                    }
                }
                return 1;
            }
        }
        return -1;
    }


    @Override
    public int onApplyMonsterEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 幸運木桶) {
            int value = applyfrom.getBuffedIntValue(SecondaryStat.Roulette);
            if (value == 3) {
                applier.localmobstatups.remove(MonsterStatus.Burned);
                applier.prop = applier.effect.getW();
            } else if (value == 4) {
                applier.localmobstatups.remove(MonsterStatus.IndieSlow);
                applier.prop = 0;
            } else {
                return 0;
            }
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        SecondaryStatValueHolder mbsvh;
        if (applier.effect != null && applier.totalDamage > 0 && (mbsvh = applyfrom.getBuffStatValueHolder(SecondaryStat.Roulette)) != null && mbsvh.effect != null && mbsvh.value >= 3 && mbsvh.value <= 4) {
            int duration;
            if (mbsvh.value == 4) {
                duration = mbsvh.effect.getDotTime();
            } else {
                duration = mbsvh.effect.getDebuffTime();
            }
            mbsvh.effect.applyMonsterEffect(applyfrom, applyto, mbsvh.effect.calcMobDebuffDuration(duration, applyfrom));
        }
        if (applier.totalDamage > 0 && applier.effect != null
                && (mbsvh = applyfrom.getBuffStatValueHolder(SecondaryStat.CannonShooter_MiniCannonBall)) != null && mbsvh.effect != null && mbsvh.value > 0
                && "1".equals(applyfrom.getOneInfo(1544, String.valueOf(迷你砲彈)))
                && !applyfrom.isSkillCooling(mbsvh.effect.getSourceId())) {
            switch (applier.effect.getSourceId()) {
                case 迷你砲彈_1:
                case 迷你砲彈_2:
                case 磁錨:
                case 雙胞胎猴子:
                case 雙胞胎猴子_1:
                case 特種猴子部隊:
                case 特種猴子部隊_1:
                case 特種猴子部隊_2:
                case 特種猴子部隊_3:
                    break;
                default:
                    mbsvh.effect.applyTo(applyfrom, applyfrom, mbsvh.effect.getBuffDuration(applyfrom), true, true, false, applyto.getPosition());
                    break;
            }
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.effect != null && applier.ai.skillId == 精準轟炸_1 && player.isSkillCooling(精準轟炸)) {
            if (player.getSpecialStat().getPoolMakerCount() > 0) {
                player.getSpecialStat().setPoolMakerCount(player.getSpecialStat().getPoolMakerCount() - 1);
                player.send(MaplePacketCreator.poolMakerInfo(player.getSpecialStat().getPoolMakerCount() > 0, player.getSpecialStat().getPoolMakerCount(), player.getCooldownLeftTime(精準轟炸)));
                if (applier.effect != null && applier.effect.getSourceId() == 精準轟炸_1 && player.getMap().getAffectedAreaObject(player.getId(), 精準轟炸_2).size() < 2) {
                    player.getSkillEffect(精準轟炸_2).applyAffectedArea(player, applier.ai.skillposition);
                }
            } else {
                player.getSpecialStat().setPoolMakerCount(0);
                player.send(MaplePacketCreator.poolMakerInfo(false, 0, 0));
            }
        }
        return 1;
    }
}
