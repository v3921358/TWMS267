package Client.skills.handler.冒險家.法師類別;

import Client.*;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.JobConstants;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
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
import Net.server.Timer;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.Element;
import Net.server.life.MapleMonster;
import Net.server.life.MobSkill;
import Net.server.maps.MapleFoothold;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Opcode.Opcode.EffectOpcode;
import Packet.EffectPacket;
import Packet.MobPacket;
import SwordieX.client.party.Party;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static Config.constants.skills.冒險家_技能群組.type_法師.冰雷.*;

public class 冰雷大魔導士 extends AbstractSkillHandler {

    public 冰雷大魔導士() {
        jobs = new MapleJob[]{
                MapleJob.冰雷巫師,
                MapleJob.冰雷魔導士,
                MapleJob.冰雷大魔導士
        };

        for (Field field : 冰雷.class.getDeclaredFields()) {
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
            case HEXA_極凍領域_IV:
            case HEXA_極凍領域_III:
            case HEXA_極凍領域_II:
                return HEXA_極凍領域;
            case HEXA_閃電連擊_IV_延伸:
                return HEXA_閃電連擊_IV;
            case 閃電球_1:
                return 閃電球;
            case 暴風雪_冰槍:
                return 暴風雪;
            case 冰雪結界_1:
            case 冰雪結界_2:
                return 冰雪結界;
            case 冰河紀元:
                return 冰河紀元_1;
            case 落雷凝聚_1:
            case 落雷凝聚_2:
                return 落雷凝聚;
            case 眾神之雷_1:
                return 眾神之雷;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 冰錐劍:
            case 冰風暴:
            case 冰川之墻:
            case 暴風雪:
            case 冰鋒刃:
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.s));
                effect.getInfo().put(MapleStatInfo.time, effect.getInfo().get(MapleStatInfo.time) * 2);
                return 1;
            case 寒冰迅移:
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.s));
                statups.put(SecondaryStat.ChillingStep, 1);
                return 1;
            case 精神強化:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieMAD, effect.getInfo().get(MapleStatInfo.indieMad));
                return 1;
            case 元素適應_雷冰:
                statups.put(SecondaryStat.AntiMagicShell, 1);
                return 1;
            case 閃電球:
            case 閃電球_1:
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 瞬間移動精通:
                statups.put(SecondaryStat.TeleportMasteryOn, 1);
                monsterStatus.put(MonsterStatus.Stun, 1);
                return 1;
            case 瞬間移動爆發:
            case 瞬間移動精通_提升距離:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.RpSiksin, 1);
                return 1;
            case 閃電連擊:
                monsterStatus.put(MonsterStatus.Stun, 1);
                return 1;
            case 冰龍吐息:
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                statups.put(SecondaryStat.IndieIgnorePCounter, 1);
                monsterStatus.put(MonsterStatus.IndiePDR, effect.getInfo().get(MapleStatInfo.y));
                monsterStatus.put(MonsterStatus.IndieMDR, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 魔力無限:
                effect.setHpR(effect.getInfo().get(MapleStatInfo.y) / 100.0);
                effect.setMpR(effect.getInfo().get(MapleStatInfo.y) / 100.0);
                statups.put(SecondaryStat.Stance, effect.getInfo().get(MapleStatInfo.prop));
                statups.put(SecondaryStat.Infinity, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 召喚冰魔:
                effect.setDebuffTime(effect.getInfo().get(MapleStatInfo.subTime) * 1000);
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.s));
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 傳說冒險:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 神秘狙擊:
                effect.getInfo().put(MapleStatInfo.time, 5000);
                statups.put(SecondaryStat.ArcaneAim, 1);
                return 1;
            case 冰雪結界:
                effect.setDebuffTime(effect.getInfo().get(MapleStatInfo.time));
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.s));

                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.IndieTerR, effect.getInfo().get(MapleStatInfo.z));
                statups.put(SecondaryStat.IndieAsrR, effect.getInfo().get(MapleStatInfo.w));
                statups.put(SecondaryStat.IceAura, 1);
                return 1;
            case 冰雪結界_1:
                statups.put(SecondaryStat.IndieTerR, effect.getInfo().get(MapleStatInfo.z));
                statups.put(SecondaryStat.IndieAsrR, effect.getInfo().get(MapleStatInfo.w));
                statups.put(SecondaryStat.IceAuraZone, 1);
                return 1;
            case 冰雪結界_2:
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                return 1;
            case 冰河紀元:
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.s));
                return 1;
            case 冰雪之精神:
                effect.setDebuffTime(effect.getSubTime() * 1000);
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.s));
                return 1;
            case 落雷凝聚_1:
                effect.getInfo().put(MapleStatInfo.time, 250);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 落雷凝聚) {
            applier.pos = slea.readPos();
            slea.readByte();
            for (int size = slea.readInt(), i = 0; i < size; ++i) {
                Point pos1 = slea.readPos();
                Timer.MapTimer.getInstance().schedule(() -> chr.getSkillEffect(落雷凝聚_1).applyAffectedArea(chr, pos1), i * 200L);
            }
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyTo(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 元素適應_雷冰) {
            applier.cooldown = 0;
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect instanceof MobSkill) {
            boolean isCriticalDebuff = false;
            for (SecondaryStat stat : applier.localstatups.keySet()) {
                if (stat.isCriticalDebuff()) {
                    isCriticalDebuff = true;
                    break;
                }
            }
            SecondaryStatValueHolder mbsvh;
            if (isCriticalDebuff && (mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.AntiMagicShell)) != null && mbsvh.value > 0) {
                if (mbsvh.z == 0) {
                    mbsvh.effect.unprimaryPassiveApplyTo(applyto);
                    applyto.registerSkillCooldown(mbsvh.effect, true);
                }
                return 0;
            }
            return -1;
        }
        switch (applier.effect.getSourceId()) {
            case 元素適應_雷冰: {
                if (applier.primary) {
                    applier.duration = 2100000000;
                    applier.buffz = 0;
                } else {
                    applier.buffz = applier.duration;
                }
                return 1;
            }
            case 寒冰迅移:
            case 瞬間移動精通: {
                applier.duration = 2100000000;
                return 1;
            }
            case 閃電球: {
                final SecondaryStatValueHolder buffStatValueHolder;
                if ((buffStatValueHolder = applyfrom.getBuffStatValueHolder(閃電球_1)) != null) {
                    applyfrom.cancelEffect(buffStatValueHolder.effect, true, buffStatValueHolder.startTime);
                }
                return 1;
            }
            case 閃電球_1: {
                applyfrom.dispelEffect(閃電球);
                return 1;
            }
            case 神秘狙擊: {
                if (applyto.getBuffedValue(SecondaryStat.ArcaneAim) != null) {
                    applier.localstatups.put(SecondaryStat.ArcaneAim, Math.min(applier.effect.getY(), applyto.getBuffedIntValue(SecondaryStat.ArcaneAim) + 1));
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
                applyto.dispelEffect(重砲指揮官.傳說冒險);
                return 1;
            }
            case 冰雪結界: {
                if (applyfrom == applyto) {
                    applyfrom.dispelEffect(冰雪結界_1);
                    applyfrom.dispelEffect(冰雪結界_2);
                } else {
                    if (applyfrom.getBuffStatValueHolder(冰雪結界_1) != null) {
                        return 0;
                    }
                    applier.localstatups.remove(SecondaryStat.IceAura);
                }
                return 1;
            }
            case 冰雪結界_1: {
                if (applyfrom == applyto) {
                    applyfrom.dispelEffect(冰雪結界);
                    if (applier.primary) {
                        SkillFactory.getSkill(冰雪結界_2).getEffect(applier.effect.getLevel()).applyBuffEffect(applyfrom, applier.duration, false);
                        return 0;
                    }
                } else {
                    if (applyfrom.getBuffStatValueHolder(冰雪結界) != null) {
                        return 0;
                    }
                    applier.localstatups.remove(SecondaryStat.IceAuraZone);
                }
                applier.duration = 2100000000;
                return 1;
            }
            case 落雷凝聚: {
                applier.b3 = true;
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyMonsterEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applier.effect == null) {
            return -1;
        }
        switch (applier.effect.getSourceId()) {
            case 暴風雪:
                applier.prop = 100;
                return 1;
            case 瞬間移動精通:
                applier.prop = applier.effect.getSubProp();
                return 1;
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        MapleStatEffect effect;
        if ((effect = applyfrom.getSkillEffect(魔力吸收)) != null && effect.makeChanceResult(applyfrom)) {
            int rate;
            if (!applyto.getStats().isBoss()) {
                rate = effect.getX();
            } else {
                rate = effect.getY();
            }
            int absorbMp = Math.min(applyto.getMobMaxMp() * rate / 100, applyto.getMp());
            if (absorbMp > 0) {
                applyto.setMp(applyto.getMp() - absorbMp);
                applyfrom.addMP(absorbMp);
                applyfrom.send(EffectPacket.encodeUserEffectLocal(effect.getSourceId(), EffectOpcode.UserEffect_SkillUse, applyfrom.getLevel(), effect.getLevel()));
                applyfrom.getMap().broadcastMessage(applyfrom, EffectPacket.onUserEffectRemote(applyfrom, effect.getSourceId(), EffectOpcode.UserEffect_SkillUse, applyfrom.getLevel(), effect.getLevel()), false);
            }
        }
        if (applier.effect == null) {
            return -1;
        }
        if (applier.effect.getSourceId() != 閃電球 && applier.effect.getSourceId() != 閃電球_1 && applier.effect.getSkill().getElement() == Element.雷) {
            MonsterEffectHolder meh = applyto.getEffectHolder(MonsterStatus.Speed);
            if (meh != null && meh.z > 0) {
                int eVal = (int) Math.ceil(meh.value / (double) meh.z);
                meh.z--;
                meh.value = Math.min(meh.value - eVal, 0);
                applyfrom.getMap().broadcastMessage(MobPacket.mobStatSet(applyto, Collections.singletonMap(MonsterStatus.Speed, meh.sourceID)), applyfrom.getPosition());
            }
        }
        return -1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        final MapleStatEffect skillEffect3;
        if (applier.effect != null && applier.effect.getSourceId() == 冰河紀元_1 && (skillEffect3 = player.getSkillEffect(冰河紀元)) != null) {
            Rectangle rect = applier.effect.calculateBoundingBox(player.getPosition());
            List<Rectangle> allArea = new LinkedList<>();
            Point p;
            for (MapleFoothold fh : player.getMap().getFootholds().getAllRelevants()) {
                if (fh.isWall()) {
                    continue;
                }
                p = new Point((fh.getX1() + fh.getX2()) / 2, (fh.getY1() + fh.getY2()) / 2);
                p.y += 30;
                Rectangle area = skillEffect3.calculateBoundingBox(p);
                if (!rect.getBounds().contains(area)) {
                    continue;
                }
                boolean found = false;
                for (Rectangle rArea : allArea) {
                    if (rArea.getBounds().intersects(area) || rArea.getBounds().equals(area)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }
                allArea.add(area);
                skillEffect3.applyAffectedArea(player, p);
            }
        }
        return 1;
    }

    @Override
    public int onAfterCancelEffect(MapleCharacter player, SkillClassApplier applier) {
        if (!applier.overwrite) {
            switch (applier.effect.getSourceId()) {
                case 閃電球_1: {
                    MapleStatEffect effect = player.getSkillEffect(閃電球);
                    if (effect != null) {
                        effect.applyTo(player, true);
                    }
                    break;
                }
                case 冰雪結界_2: {
                    MapleStatEffect effect = player.getSkillEffect(冰雪結界);
                    if (effect != null) {
                        effect.applyTo(player, true);
                    }
                    break;
                }
            }
        }
        return -1;
    }

    public static void handleIceReiki(MapleCharacter chr) {
        SecondaryStatValueHolder mbsvh;
        if (JobConstants.is冰雷(chr.getJob()) && (mbsvh = chr.getBuffStatValueHolder(SecondaryStat.IceAura)) != null) {
            if (mbsvh.effect != null && chr.getStat().getMp() >= mbsvh.effect.getMpCon()) {
                chr.addMP(-mbsvh.effect.getMpCon());
                //進行尋找周圍怪物
                int mobCount = 15;
                int CurrCount = 0;
                int duration = mbsvh.effect.getMobDebuffDuration(chr);

                Rectangle bounds = mbsvh.effect.calculateBoundingBox(chr.getPosition(), chr.isFacingLeft());
                List<MapleMapObject> affected = chr.getMap().getMapObjectsInRect(bounds, Collections.singletonList(MapleMapObjectType.MONSTER));
                for (MapleMapObject mo : affected) {
                    MapleMonster monster = (MapleMonster) mo;
                    if (mbsvh.effect.getMonsterStatus().size() > 0 && monster != null) {
                        if (++CurrCount < mobCount) {
                            mbsvh.effect.applyMonsterEffect(chr, monster, duration);
                        } else {
                            break;
                        }
                    }
                }

                Party party = chr.getParty();
                if (party != null) {
                    List<MapleMapObject> affectedC = chr.getMap().getMapObjectsInRect(bounds, Collections.singletonList(MapleMapObjectType.PLAYER));
                    for (MapleMapObject obj : affectedC) {
                        MapleCharacter tchr = (MapleCharacter) obj;
                        if (tchr == null || tchr == chr) {
                            continue;
                        }
                        if (party.getPartyMemberByID(tchr.getId()) != null && tchr.getBuffStatValueHolder(冰雪結界) == null) {
                            mbsvh.effect.applyBuffEffect(chr, tchr, mbsvh.effect.getBuffDuration(chr), false, false, true, null);
                        }
                    }
                }
            } else {
                chr.dispelEffect(SecondaryStat.IceAura);
            }
        } else if ((mbsvh = chr.getBuffStatValueHolder(冰雪結界)) != null) {
            MapleCharacter fchr = chr.getMap().getPlayerObject(mbsvh.fromChrID);
            if (fchr == null || fchr.getParty() != chr.getParty() || fchr.getBuffStatValueHolder(SecondaryStat.IceAura) == null || !mbsvh.effect.calculateBoundingBox(fchr.getPosition()).contains(fchr.getPosition())) {
                chr.dispelEffect(冰雪結界);
            }
        }
    }
}
