package Client.skills.handler.冒險家.弓手類別;

import Client.*;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.箭神;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.重砲指揮官;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.life.MobSkill;
import Opcode.Headler.OutHeader;
import Packet.EffectPacket;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static Config.constants.skills.神射手.*;

public class 神射手 extends AbstractSkillHandler {

    public 神射手() {
        jobs = new MapleJob[]{
                MapleJob.弩弓手,
                MapleJob.狙擊手,
                MapleJob.神射手
        };

        for (Field field : Config.constants.skills.神射手.class.getDeclaredFields()) {
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
            case HEXA_裂空狙擊_延伸:
                return HEXA_裂空狙擊;
            case HEXA_覺醒必殺狙擊_VII:
            case HEXA_覺醒必殺狙擊_VI:
                return HEXA_必殺狙擊_VI;
            case 回歸之箭_1:
                return 回歸之箭;
            case 光速神弩_1:
            case 光速神弩II_1:
                return 光速神弩II;
            case 覺醒神弩:
                return 覺醒之箭;
            case 覺醒神弩II:
            case 覺醒神弩II_1:
            case 全神貫注:
            case 強化必殺狙擊:
            case 強化必殺狙擊_1:
                return 進階覺醒之箭;
            case 真必殺狙擊_1:
                return 真必殺狙擊;
            case 分裂之矢_1:
                return 分裂之矢;
            case 能量弩矢_1:
            case 能量弩矢_2:
                return 能量弩矢;
            case 極速射擊:
                return 連射十字弓砲彈;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 覺醒之箭:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.EnhancePiercing, 0);
                return 1;
            case 召喚銀隼:
                effect.setDebuffTime(effect.getX() * 1000);
                monsterStatus.put(MonsterStatus.Freeze, 1);

                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 回歸之箭:
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 反向傷害:
                statups.put(SecondaryStat.PowerTransferGauge, 0);
                return 1;
            case 幻像箭影:
                effect.setDebuffTime(effect.getSubTime() * 1000);
                monsterStatus.put(MonsterStatus.Stun, 1);

                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 會心之眼:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.SharpEyes, (effect.getX() << 8) + effect.getY());
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 傳說冒險:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 全神貫注:
                statups.put(SecondaryStat.IndieIgnoreMobpdpR, effect.getX());
                statups.put(SecondaryStat.IndiePMdR, effect.getZ());
                return 1;
            case 專注弱點:
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                statups.put(SecondaryStat.IndieIgnoreMobpdpR, effect.getInfo().get(MapleStatInfo.indieIgnoreMobpdpR));
                statups.put(SecondaryStat.IgnoreTargetDEF, 0);
                statups.put(SecondaryStat.BullsEye, (effect.getX() << 8) + effect.getY());
                return 1;
            case 真必殺狙擊:
                statups.put(SecondaryStat.CursorSniping, effect.getX());
                return 1;
            case 分裂之矢:
                statups.put(SecondaryStat.SplitArrow, 1);
                return 1;
            case 連射十字弓砲彈:
                statups.put(SecondaryStat.RepeatinCartrige, effect.getX());
                return 1;
        }
        return -1;
    }

    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 幻像箭影) {
            applier.pos = slea.readPos();
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 覺醒之箭: {
                applier.localstatups.put(SecondaryStat.EnhancePiercing, Math.min(applyto.getBuffedIntValue(SecondaryStat.EnhancePiercing) + applier.effect.getX(), applier.effect.getY()));
                return 1;
            }
            case 止痛藥: {
                List<SecondaryStatValueHolder> mbsvhs = new LinkedList<>();
                for (Map.Entry<SecondaryStat, List<SecondaryStatValueHolder>> entry : applyto.getAllEffects().entrySet()) {
                    if (!entry.getKey().isNormalDebuff() && !entry.getKey().isCriticalDebuff()) {
                        continue;
                    }
                    entry.getValue().stream().filter(mbsvh -> mbsvh.effect instanceof MobSkill).forEach(mbsvhs::add);
                }
                if (mbsvhs.size() > 0) {
                    mbsvhs.forEach(mbsvh -> applyto.cancelEffect(mbsvh.effect, mbsvh.startTime));
                }
                return 1;
            }
            case 會心之眼: {
                MapleStatEffect effect;
                applier.buffz = 0;
                if ((effect = applyfrom.getSkillEffect(會心之眼_無視防禦)) != null) {
                    applier.buffz = effect.getIndieIgnoreMobpdpR();
                }
                if ((effect = applyfrom.getSkillEffect(會心之眼_爆擊提升)) != null) {
                    applier.localstatups.put(SecondaryStat.SharpEyes, applier.localstatups.get(SecondaryStat.SharpEyes) + (effect.getX() << 8));
                }
                return 1;
            }
            case 反向傷害: {
                if (applyto.getBuffedValue(SecondaryStat.PowerTransferGauge) == null) {
                    applyto.send(EffectPacket.showBlessOfDarkness(-1, 反向傷害));
                    applyto.getMap().broadcastMessage(applyto, EffectPacket.showBlessOfDarkness(applyto.getId(), 反向傷害), false);
                }
                applier.localstatups.put(SecondaryStat.PowerTransferGauge, applyto.getStat().getCurrentMaxHP() * applier.effect.getZ() / 100);
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
                applyto.dispelEffect(Config.constants.skills.神射手.傳說冒險);
                applyto.dispelEffect(Config.constants.skills.開拓者.傳說冒險);
                applyto.dispelEffect(暗影神偷.傳說冒險);
                applyto.dispelEffect(夜使者.傳說冒險);
                applyto.dispelEffect(影武者.傳說冒險);
                applyto.dispelEffect(拳霸.傳說冒險);
                applyto.dispelEffect(槍神.傳說冒險);
                applyto.dispelEffect(重砲指揮官.傳說冒險);
                return 1;
            }
            case 真必殺狙擊: {
                if (!applier.primary && !applier.att) {
                    applier.localstatups.clear();
                    applier.localstatups.put(SecondaryStat.IndieNotDamaged, 1);
                    return 1;
                }
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.CursorSniping);
                int value;
                if (mbsvh != null) {
                    value = mbsvh.value - 1;
                    applier.duration = mbsvh.getLeftTime();
                } else {
                    value = applier.localstatups.get(SecondaryStat.CursorSniping);
                }
                applier.localstatups.put(SecondaryStat.CursorSniping, value);
                if (value <= 0) {
                    applier.overwrite = false;
                    applier.duration = 0;
                }
                return 1;
            }
            case 連射十字弓砲彈: {
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.RepeatinCartrige);
                int value;
                if (mbsvh != null) {
                    value = mbsvh.z - 1;
                    mbsvh.z = value;
                    applier.duration = mbsvh.getLeftTime();
                } else {
                    value = applier.localstatups.get(SecondaryStat.RepeatinCartrige) * applier.effect.getV();
                }
                applier.buffz = value;
                if ((double) value % applier.effect.getV() != 0.0) {
                    return 0;
                }
                applier.localstatups.put(SecondaryStat.RepeatinCartrige, value / applier.effect.getV());
                if (value <= 0) {
                    applier.overwrite = false;
                    applier.duration = 0;
                }
                return 1;
            }
            case 極速射擊: {
                applyfrom.getSkillEffect(連射十字弓砲彈).applyBuffEffect(applyfrom, applyto, 0, applier.primary, applier.att, applier.passive, applier.pos);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        MapleStatEffect effect;
        if (applier.totalDamage > 0L && (effect = applyfrom.getSkillEffect(反向傷害)) != null) {
            effect.unprimaryPassiveApplyTo(applyfrom);
        }
        if ((effect = applyfrom.getSkillEffect(致命箭)) != null && Randomizer.isSuccess(effect.getX())) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(OutHeader.LP_MobSpecialEffectBySkill.getValue());
            mplew.writeInt(applyto.getObjectId());
            mplew.writeInt(effect.getSourceId());
            mplew.writeInt(applyfrom.getId());
            mplew.writeShort(0);
            applyfrom.getMap().broadcastMessage(applyfrom, mplew.getPacket(), true);
            applyfrom.addHPMP(effect.getZ(), effect.getZ());
        }
        if ((effect = applyfrom.getSkillEffect(強化必殺狙擊)) != null && (applier.effect.getSourceId() == 必殺狙擊 || applier.effect.getSourceId() == 強化必殺狙擊)) {
            Pair<Long, Integer> debuffInfo = (Pair<Long, Integer>) applyfrom.getTempValues().getOrDefault("必殺狙擊Debuff", new Pair(0L, 0));
            if (applier.effect.getSourceId() == 強化必殺狙擊) {
                if (applyto.isAlive()) {
                    debuffInfo.left = System.currentTimeMillis() + effect.getDuration();
                    debuffInfo.right = applyto.getObjectId();
                    applyfrom.getTempValues().put("必殺狙擊Debuff", debuffInfo);
                    sendSnipeStatSet(applyfrom);
                }
            } else {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.SnipeExtraAttack);
                mplew.writeInt(強化必殺狙擊_1);
                mplew.writeInt(0);
                mplew.writeInt(1);
                mplew.writeInt(debuffInfo.getRight());
                mplew.writeInt(761);
                applyfrom.send(mplew.getPacket());

                debuffInfo.left = 0L;
                sendSnipeStatSet(applyfrom);
            }
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        MapleStatEffect effect;
        if (applier.ai.skillId != 光速神弩 && applier.ai.skillId != 覺醒神弩II_1 && applier.ai.skillId != 光速神弩II_1 && applier.ai.skillId != 必殺狙擊 && applier.ai.mobAttackInfo.size() > 0 && (effect = player.getSkillEffect(全神貫注)) != null && effect.getSkill().getSkillList().contains(applier.ai.skillId) && !player.isSkillCooling(全神貫注)) {
            player.registerSkillCooldown(全神貫注, 500, true);
            effect.applyBuffEffect(player, player, effect.getBuffDuration(player), false, false, true, null);
        }
        switch (applier.ai.skillId) {
            case 覺醒神弩:
            case 覺醒神弩II_1:
            case 強化必殺狙擊:
                player.dispelEffect(SecondaryStat.EnhancePiercing);
                break;
        }
        if (applier.ai.mobAttackInfo.size() > 0 && (effect = player.getSkillEffect(覺醒之箭)) != null) {
            if (applier.ai.skillId == 光速神弩 || applier.ai.skillId == 光速神弩II_1 || (applier.ai.skillId == 必殺狙擊 && player.getSkillEffect(進階覺醒之箭) != null)) {
                effect.applyBuffEffect(player, player, effect.getBuffDuration(player), false, false, true, null);
            }
        }
        if (applier.effect != null && applier.effect.getSourceId() == 真必殺狙擊_1 && (effect = player.getEffectForBuffStat(SecondaryStat.CursorSniping)) != null) {
            effect.applyBuffEffect(player, player, effect.getBuffDuration(player), false, true, true, null);
        }
        return 1;
    }

    public int onAfterCancelEffect(MapleCharacter player, SkillClassApplier applier) {
        if (!applier.overwrite) {
            if (applier.effect.getSourceId() == 真必殺狙擊) {
                if (applier.localstatups.containsKey(SecondaryStat.CursorSniping)) {
                    applier.effect.applyBuffEffect(player, player, 2000, false, false, true, null);
                }
            }
        }
        return -1;
    }

    public static void sendSnipeStatSet(MapleCharacter chr) {
        Object obj = chr.getTempValues().get("必殺狙擊Debuff");
        if (obj == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Pair<Long, Integer> debuffInfo = (Pair<Long, Integer>) obj;
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.SnipeStatSet);
        mplew.writeInt(強化必殺狙擊);
        if (now < debuffInfo.getLeft()) {
            mplew.write(1);
            mplew.writeInt(1);
            mplew.writeInt(debuffInfo.getRight());
            mplew.writeInt(1);
            mplew.writeInt(0);
            mplew.writeInt(Math.max(0, debuffInfo.getLeft() - now));
            mplew.writeInt(783);
        } else {
            mplew.write(0);
            chr.getTempValues().remove("必殺狙擊Debuff");
        }
        chr.send(mplew.getPacket());
    }
}
