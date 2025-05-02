package Client.skills.handler.冒險家.劍士類別;

import Client.*;
import Client.force.MapleForceAtom;
import Client.force.MapleForceFactory;
import Client.skills.ExtraSkill;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
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
import Opcode.Headler.OutHeader;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import Server.channel.handler.AttackMobInfo;
import SwordieX.client.party.PartyMember;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.*;

import static Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士.*;

public class 聖騎士 extends AbstractSkillHandler {

    public 聖騎士() {
        jobs = new MapleJob[]{
                MapleJob.見習騎士,
                MapleJob.騎士,
                MapleJob.聖騎士
        };

        for (Field field : Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士.class.getDeclaredFields()) {
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
            case HEXA_騎士衝擊波_IV:
                return 騎士衝擊波;
            case 神聖烙印_1:
            case 神聖衝擊_1:
                return 神聖烙印;
            case 神聖審判_1:
                return 神聖審判;
            case 完全無敵:
                return 神域護佑;
            case 祝福之鎚_強化:
                return 祝福之鎚;
            case 雷神戰槌_1:
                return 雷神戰槌;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 神聖揮擊:
                effect.setDebuffTime(effect.getY() * 1000);
                monsterStatus.put(MonsterStatus.Stun, 1);
                return 1;
            case 騎士密令:
                effect.setDebuffTime(effect.getY() * 1000);
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 神聖凝聚:
            case 進階神聖凝聚:
                statups.put(SecondaryStat.ElementalCharge, 1);
                return 1;
            case 復原:
                effect.setHpR(effect.getInfo().get(MapleStatInfo.x) / 100.0);
                statups.put(SecondaryStat.Restoration, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 高貴威嚴:
                monsterStatus.put(MonsterStatus.IndiePDR, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.IndieMDR, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.PAD, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.MAD, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.Blind, effect.getInfo().get(MapleStatInfo.z));
                return 1;
            case 超衝擊防禦:
                statups.clear();
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.KnightsAura, effect.getLevel());
                return 1;
            case 戰鬥命令:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.CombatOrders, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 盾牌技能:
                monsterStatus.put(MonsterStatus.Stun, 1);
                return 1;
            case 祝福護甲:
                statups.clear();
                statups.put(SecondaryStat.BlessingArmor, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.BlessingArmorIncPAD, effect.getInfo().get(MapleStatInfo.epad));
                return 1;
            case 魔防消除:
                monsterStatus.put(MonsterStatus.MagicCrash, 1);
                break;
            case 聖靈祝福:
                statups.put(SecondaryStat.IndiePMdR, effect.getInfo().get(MapleStatInfo.indiePMdR));
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 守護者精神:
                statups.clear();
                statups.put(SecondaryStat.NotDamaged, 1);
                return 1;
            case 神之滅擊:
                monsterStatus.put(MonsterStatus.Freeze, 1);
                monsterStatus.put(MonsterStatus.Smite, 1);
                return 1;
            case 傳說冒險:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 神域護佑:
                statups.clear();
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                statups.put(SecondaryStat.IndieIgnorePCounter, 1);
                return 1;
            case 神聖團結:
                statups.put(SecondaryStat.PairingUser, 1);
                return 1;
            case 祝福之鎚:
                effect.getInfo().put(MapleStatInfo.time, effect.getV() * 1000);
                statups.put(SecondaryStat.BlessedHammer, 1);
                return 1;
            case 祝福之鎚_強化:
                statups.put(SecondaryStat.BlessedHammerActive, effect.getLevel());
                return 1;
            case 聖十字架:
                statups.put(SecondaryStat.IndieApplySuperStance, 1);
                statups.put(SecondaryStat.IndieAllHitDamR, -effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.GrandCross, 1);
                statups.put(SecondaryStat.ImmuneStun, 1);
                return 1;
            case 雷神戰槌:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.CannonShooter_BFCannonBall, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 守護者精神: {
                MapleCharacter effChr = null;
                for (MapleCharacter ochr : chr.getMap().getCharactersInRect(applier.effect.calculateBoundingBox(chr.getOldPosition(), chr.isFacingLeft()))) {
                    if (ochr.getParty() != null && ochr.getParty().getId() == chr.getParty().getId() && !ochr.isAlive() && ochr.getId() != chr.getId()) {
                        if (effChr == null || ochr.getPosition().distance(chr.getPosition()) < effChr.getPosition().distance(chr.getPosition())) {
                            effChr = ochr;
                        }
                    }
                }
                if (effChr != null) {
                    effChr.heal();
                    applier.effect.applyTo(chr, effChr, true, null, applier.effect.getDuration());
                } else {
                    return 0;
                }
                return 1;
            }
            case 雷神戰槌: {
                if (chr.getBuffedIntValue(SecondaryStat.CannonShooter_BFCannonBall) > 0) {
                    List<Integer> toMobOid = new LinkedList<>();
                    byte nCount = slea.readByte();
                    for (int i = 0; i < nCount; i++) {
                        toMobOid.add(slea.readInt());
                    }
                    final MapleForceAtom force = MapleForceFactory.getInstance().getMapleForce(chr, applier.effect, 0, toMobOid);
                    chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(force), true);
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 神聖揮擊:
            case 神聖衝擊:
            case 神聖烙印:
            case 騎士衝擊波: {
                int value = applyto.getBuffedIntValue(SecondaryStat.ElementalCharge);
                final int skillID = (applyto.getSkillLevel(進階神聖凝聚) > 0) ? 進階神聖凝聚 : 神聖凝聚;
                MapleStatEffect eff = applyto.getEffectForBuffStat(SecondaryStat.ElementalCharge);
                if (eff == null) {
                    eff = applyto.getSkillEffect(進階神聖凝聚);
                    if (eff == null) {
                        eff = applyto.getSkillEffect(神聖凝聚);
                    }
                } else if (value < eff.getX() * eff.getZ()) {
                    value = Math.min(value + eff.getX(), eff.getX() * eff.getZ());
                    applyto.setBuffStatValue(SecondaryStat.ElementalCharge, skillID, value);
                }
                if (value <= eff.getX() * eff.getZ()) {
                    eff.applyBuffEffect(applyto, applyto, eff.getBuffDuration(applyto), false, false, true, null);
                    if ((eff = applyto.getSkillEffect(祝福之鎚)) != null) {
                        eff.applyBuffEffect(applyto, applyto, 2100000000, true, false, true, null);
                    }
                }
                return 1;
            }
            case 復原: {
                final SecondaryStatValueHolder mbsvh;
                if ((mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.Restoration)) != null) {
                    applier.localstatups.put(SecondaryStat.Restoration, Math.min(mbsvh.value + applier.effect.getY(), applier.effect.getY() * 5));
                }
                applyto.addHPMP((int) (Math.max(applier.effect.getX() - (mbsvh == null ? 0 : mbsvh.value), 10) / 100.0 * applyto.getStat().getCurrentMaxHP()), 0, false);
                return 1;
            }
            case 神聖凝聚:
            case 進階神聖凝聚: {
                if (applyto.getBuffedValue(SecondaryStat.ElementalCharge) != null) {
                    applier.localstatups.put(SecondaryStat.ElementalCharge, applyto.getBuffedValue(SecondaryStat.ElementalCharge));
                } else {
                    applier.localstatups.put(SecondaryStat.ElementalCharge, applier.effect.getX());
                }
                return 1;
            }
            case 守護者精神: {
                if (applyfrom == applyto) {
                    return 0;
                }
                return 1;
            }
            case 傳說冒險: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(英雄.傳說冒險);
                applyto.dispelEffect(Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士.傳說冒險);
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
            case 神聖團結: {
                if (applyfrom.getParty() == null) {
                    return 1;
                }
                if (applyto != applyfrom) {
                    applier.localstatups.put(SecondaryStat.PairingUser, applyfrom.getId());
                    return 1;
                }
                for (PartyMember a3 : applyfrom.getParty().getMembers()) {
                    if ((a3).getChr() != null && a3.getFieldID() == applyfrom.getMapId() && a3.getChannel() == applyfrom.getClient().getChannel() && a3.getCharID() != applyfrom.getId() && applier.effect.calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft()).contains(a3.getChr().getPosition())) {
                        applier.effect.applyTo(applyfrom, a3.getChr(), applier.duration, applier.primary, false, applier.passive, applyfrom.getPosition());
                        applier.localstatups.put(SecondaryStat.PairingUser, a3.getCharID());
                        break;
                    }
                }
                return 1;
            }
            case 祝福之鎚: {
                if (!applier.primary) {
                    return 0;
                }
                applier.localstatups.put(SecondaryStat.BlessedHammer, Math.min(5, applyto.getBuffedIntValue(SecondaryStat.BlessedHammer) + 1));
                return 1;
            }
            case 祝福之鎚_強化: {
                if (!applier.primary) {
                    return 0;
                }
                return 1;
            }
            case 雷神戰槌: {
                if (!applier.primary) {
                    return 0;
                }
                Integer value = applyfrom.getBuffedValue(SecondaryStat.CannonShooter_BFCannonBall);
                if (value != null) {
                    applier.localstatups.put(SecondaryStat.CannonShooter_BFCannonBall, Math.max(0, value - 1));
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onAttack(final MapleCharacter player, final MapleMonster monster, SkillClassApplier applier) {
        if (applier.theSkill.getId() == 雷神戰槌) {
            ExtraSkill eskill = new ExtraSkill(雷神戰槌_1, monster.getPosition());
            eskill.FaceLeft = -1;
            eskill.Value = 1;
            player.send(MaplePacketCreator.RegisterExtraSkill(雷神戰槌, Collections.singletonList(eskill)));
        }
        return -1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.ai.skillId == 雷神戰槌) {
            applier.passive = true;
        }
        if (applier.ai.skillId == 神聖烙印) {
            ExtraSkill eskill = new ExtraSkill(神聖烙印_1, applier.ai.position);
            eskill.FaceLeft = (applier.ai.direction & 0x80) != 0 ? 1 : 0;
            eskill.Delay = 180;
            eskill.Value = 1;
            player.send(MaplePacketCreator.RegisterExtraSkill(applier.ai.skillId, Collections.singletonList(eskill)));
        }
        MapleStatEffect effect = player.getSkillEffect(神聖審判);
        if (applier.ai.skillId == 騎士衝擊波 && effect != null) {
            Map<Integer, Pair<Long, Integer>> divineJudgmentInfos = (Map<Integer, Pair<Long, Integer>>) player.getTempValues().computeIfAbsent("神聖審判計數", k -> new LinkedHashMap<>());
            List<Integer> attacks = new LinkedList<>();
            long time = System.currentTimeMillis();
            int duration = effect.getMobDebuffDuration(player);
            for (AttackMobInfo ami : applier.ai.mobAttackInfo) {
                if (!divineJudgmentInfos.containsKey(ami.mobId) || divineJudgmentInfos.get(ami.mobId).getLeft() <= time) {
                    divineJudgmentInfos.put(ami.mobId, new Pair<>(time + duration, 1));
                } else {
                    divineJudgmentInfos.get(ami.mobId).left = time + duration;
                    if (divineJudgmentInfos.get(ami.mobId).right + 1 >= effect.getX()) {
                        attacks.add(ami.mobId);
                        divineJudgmentInfos.get(ami.mobId).right = 0;
                    } else {
                        divineJudgmentInfos.get(ami.mobId).right += 1;
                    }
                }
            }
            Iterator<Map.Entry<Integer, Pair<Long, Integer>>> iterator = divineJudgmentInfos.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Pair<Long, Integer>> entry = iterator.next();
                if (entry.getValue().getLeft() <= time || player.getMap().getMobObject(entry.getKey()) == null) {
                    iterator.remove();
                }
            }

            MaplePacketLittleEndianWriter mplew;
            if (attacks.size() > 0) {
                mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(OutHeader.DivineJudgmentAttack.getValue());
                mplew.writeInt(神聖審判_1);
                mplew.writeInt(5);
                mplew.writeInt(0);
                mplew.writeInt(attacks.size());
                for (int oid : attacks) {
                    mplew.writeInt(oid);
                    mplew.writeInt(342);
                }
                player.send(mplew.getPacket());
            }

            mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(OutHeader.DivineJudgmentStatSet.getValue());
            mplew.write(true);
            mplew.writeInt(divineJudgmentInfos.size());
            for (int oid : divineJudgmentInfos.keySet()) {
                mplew.writeInt(oid);
            }
            mplew.writeInt(divineJudgmentInfos.size());
            for (Map.Entry<Integer, Pair<Long, Integer>> entry : divineJudgmentInfos.entrySet()) {
                mplew.writeInt(entry.getKey());
                mplew.writeInt(entry.getValue().getRight());
                mplew.writeInt(time - (entry.getValue().getLeft() - duration));
                mplew.writeInt(duration);
            }
            player.send(mplew.getPacket());

            iterator = divineJudgmentInfos.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Pair<Long, Integer>> entry = iterator.next();
                if (entry.getValue().getRight() <= 0) {
                    iterator.remove();
                }
            }
        }
        return 1;
    }
}
