package Client.skills.handler.冒險家.盜賊類別;

import Client.*;
import Client.force.MapleForceAtom;
import Client.force.MapleForceFactory;
import Client.inventory.Item;
import Client.skills.ExtraSkill;
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
import Config.constants.skills.冒險家_技能群組.夜使者;
import Config.constants.skills.冒險家_技能群組.影武者;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.神射手;
import Config.constants.skills.重砲指揮官;
import Config.constants.skills.開拓者;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMapItem;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Opcode.Opcode.EffectOpcode;
import Packet.EffectPacket;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

import static Config.constants.skills.冒險家_技能群組.暗影神偷.*;

public class 暗影神偷 extends AbstractSkillHandler {

    public 暗影神偷() {
        jobs = new MapleJob[]{
                MapleJob.俠盜,
                MapleJob.神偷,
                MapleJob.暗影神偷
        };

        for (Field field : Config.constants.skills.冒險家_技能群組.暗影神偷.class.getDeclaredFields()) {
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
            case HEXA_一刀兩斷_III_延伸:
            case HEXA_一刀兩斷_II_延伸:
                return HEXA_一刀兩斷;
            case HEXA_致命暗殺_IV_延伸:
            case HEXA_致命暗殺_IV_延伸_II:
            case HEXA_致命暗殺_IV_延伸_III:
                return HEXA_致命暗殺_IV;
            case 二段跳:
                return 盜賊迅捷;
            case 致命暗殺_1:
                return 致命暗殺;
            case 楓幣炸彈_攻擊:
                return 楓幣炸彈;
            case 楓幣炸彈_1:
            case 楓幣炸彈_2:
            case 殺意:
            case 血腥掠奪術: // < - Fix by hertz v245
                return 血腥掠奪術;
            case 滅殺刃影_1:
            case 滅殺刃影_2:
            case 滅殺刃影_3:
                return 滅殺刃影;
            case 黑影切斷_1:
            case 黑影切斷_2:
                return 黑影切斷;
            case 滅鬼斬靈陣_1:
            case 滅鬼斬靈陣_2:
            case 滅鬼斬靈陣_3:
            case 滅鬼斬靈陣_4:
                return 滅鬼斬靈陣;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 妙手術:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.ThiefSteal, 1);
                return 1;
            case 勇者掠奪術:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.PickPocket, 1);
                return 1;
            case 血腥掠奪術:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.PickPocket, 1);
                return 1;
            case 影分身:
                statups.put(SecondaryStat.ShadowPartner, effect.getX());
                return 1;
            case 致命暗殺:
                effect.getInfo().put(MapleStatInfo.time, 45000);
                statups.put(SecondaryStat.Exceed, 1);
                return 1;
            case 致命暗殺_1:
                effect.getInfo().put(MapleStatInfo.time, 10000);
                statups.put(SecondaryStat.Shadower_Assassination, 1);
                return 1;
            case 殺意:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.BloodyExplosion, 1);
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 傳說冒險:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 翻轉硬幣:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.FlipTheCoin, 1);
                return 1;
            case 滅殺刃影:
            case 滅殺刃影_1:
            case 滅殺刃影_2:
            case 滅殺刃影_3:
                effect.getInfo().put(MapleStatInfo.time, 5000);
                statups.put(SecondaryStat.Shadower_ShadowAssault, 3);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        switch (applier.effect.getSourceId()) {
            case 黑暗瞬影: {
                MapleStatEffect effect;
                if (chr.isSkillCooling(盜賊.隱身術) || (effect = chr.getSkillEffect(盜賊.隱身術)) == null) {
                    return 0;
                }
                effect.applyTo(chr);
                return 1;
            }
            case 楓幣炸彈:
            case 楓幣炸彈_1: {
                final ArrayList<Integer> moboids = new ArrayList<>();
                MapleMonster bossMob = null;
                for (MapleMapObject o : chr.getMap().getMapObjectsInRange(chr.getPosition(), applier.effect.getRange(), Collections.singletonList(MapleMapObjectType.MONSTER))) {
                    moboids.add(o.getObjectId());
                    if (((MapleMonster) o).isBoss() && (bossMob == null || bossMob.getMaxHP() < ((MapleMonster) o).getMaxHP())) {
                        bossMob = ((MapleMonster) o);
                    }
                }
                if (bossMob != null) {
                    moboids.clear();
                    moboids.add(bossMob.getObjectId());
                }
                if (moboids.isEmpty()) {
                    return 0;
                }
                final List<MapleMapItem> stealMesoObject = chr.getMap().getStealMesoObject(chr, applier.effect.getBulletCount(), applier.effect.getRange());
                for (MapleMapItem item : stealMesoObject) {
                    item.setEnterType((byte) 0);
                    chr.getMap().disappearMapObject(item);
                }
                if (stealMesoObject.isEmpty()) {
                    return 0;
                }
                final MapleForceAtom force = new MapleForceAtom();
                force.setOwnerId(chr.getId());
                force.setBulletItemID(0);
                force.setArriveDir(0);
                force.setArriveRange(500);
                force.setForcedTarget(null);
                force.setFirstMobID(0);
                final ArrayList<Integer> oids = new ArrayList<>();
                for (int i = 0; i < applier.effect.getMobCount(); i++) {
                    oids.add(moboids.get(i % moboids.size()));
                }
                force.setToMobOid(oids);
                if (applier.effect.getSourceId() == 楓幣炸彈) {
                    force.setSkillId(楓幣炸彈_攻擊);
                    force.setForceType(MapleForceType.楓幣炸彈.ordinal());
                } else if (applier.effect.getSourceId() == 楓幣炸彈_1) {
                    force.setSkillId(楓幣炸彈_2);
                    force.setForceType(75);
                }
                force.setInfo(forceFactory.getForceInfo_楓幣炸彈(chr, stealMesoObject, 1000));
                chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(force), true);
                chr.send(EffectPacket.encodeUserEffectLocal(applier.effect.getSourceId(), EffectOpcode.UserEffect_SkillUse, chr.getLevel(), applier.effect.getLevel()));
                chr.getMap().broadcastMessage(chr, EffectPacket.onUserEffectRemote(chr, applier.effect.getSourceId(), EffectOpcode.UserEffect_SkillUse, chr.getLevel(), applier.effect.getLevel()), false);

                MapleStatEffect effect;
                if (chr.getBuffStatValueHolder(殺意) == null && (effect = chr.getSkillEffect(殺意)) != null && applier.effect.getSourceId() == 楓幣炸彈_1) {
                    effect.applyBuffEffect(chr, chr, 2100000000, false, false, true, null);
                }
                return 1;
            }
            case 暗影霧殺: {
                c.announce(MaplePacketCreator.sendSkillUseResult(true, applier.effect.getSourceId()));
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 勇者掠奪術:
            case 血腥掠奪術: {
                if (applier.effect.getSourceId() != applyto.getBuffSource(SecondaryStat.PickPocket)) {
                    final List<MapleMapItem> stealMesoObject = applyto.getMap().getStealMesoObject(applyto, -1, -1);
                    for (MapleMapItem item : stealMesoObject) {
                        applyto.getMap().disappearMapObject(item);
                    }
                }
                if (applier.passive) {
                    applier.buffz = applyto.getBuffedIntZ(SecondaryStat.PickPocket);
                    final List stealMesos = applyto.getMap().getStealMesoObject(applyto, applier.effect.getY(), -1);
                    if (applier.buffz != stealMesos.size()) {
                        applier.buffz = stealMesos.size();
                        return 1;
                    }
                } else {
                    applier.buffz = 0;
                }
                return !applier.passive ? 1 : 0;
            }
            case 致命暗殺_1: {
                if (applier.passive) {
                    Object z = applyfrom.getTempValues().remove("致命暗殺減益OID");
                    int oid = (z instanceof Integer) ? (int) z : 0;
                    SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.Shadower_Assassination);
                    if (mbsvh != null && mbsvh.z == oid) {
                        applier.localstatups.put(SecondaryStat.Shadower_Assassination, Math.min(mbsvh.value + 1, 3));
                    }
                    applier.buffz = oid;
                    return 1;
                }
                return 0;
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
                applyto.dispelEffect(Config.constants.skills.冒險家_技能群組.暗影神偷.傳說冒險);
                applyto.dispelEffect(夜使者.傳說冒險);
                applyto.dispelEffect(影武者.傳說冒險);
                applyto.dispelEffect(拳霸.傳說冒險);
                applyto.dispelEffect(槍神.傳說冒險);
                applyto.dispelEffect(重砲指揮官.傳說冒險);
                return 1;
            }
            case 翻轉硬幣: {
                int value = Math.min(applyto.getBuffedIntValue(SecondaryStat.FlipTheCoin), applier.effect.getY() + 1);
                if (!JobConstants.is幻影俠盜(applyto.getJob())) {
                    value = value < applier.effect.getY() + 1 ? value + 1 : value;
                    applier.localstatups.put(SecondaryStat.FlipTheCoin, value);
                }
                return 1;
            }
            case 滅殺刃影:
            case 滅殺刃影_1:
            case 滅殺刃影_2:
            case 滅殺刃影_3: {
                applier.b4 = false;
                if (!applier.primary) {
                    return 0;
                }
                int value = applyto.getBuffedIntValue(SecondaryStat.Shadower_ShadowAssault);
                if (applyto.getBuffedValue(SecondaryStat.Shadower_ShadowAssault) == null) {
                    return 1;
                }
                applyto.dispelEffect(SecondaryStat.Shadower_ShadowAssault);
                applier.localstatups.put(SecondaryStat.Shadower_ShadowAssault, --value);
                if (value <= 0) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                }
                return 1;
            }
            case 滅鬼斬靈陣: {
                List<Integer> exList = Arrays.asList(滅鬼斬靈陣_4, 滅鬼斬靈陣_2, 滅鬼斬靈陣_1, 滅鬼斬靈陣_3, 滅鬼斬靈陣_2, 滅鬼斬靈陣_1, 滅鬼斬靈陣_3, 滅鬼斬靈陣_2, 滅鬼斬靈陣_1, 滅鬼斬靈陣_3, 滅鬼斬靈陣_2, 滅鬼斬靈陣_1);
                List<ExtraSkill> eskills = new LinkedList<>();
                for (int skill : exList) {
                    ExtraSkill eskill = new ExtraSkill(skill, applyto.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = applyto.isFacingLeft() ? 0 : 1;
                    eskills.add(eskill);
                }
                applyto.send(MaplePacketCreator.RegisterExtraSkill(滅鬼斬靈陣, eskills));
                return 0;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applier.totalDamage > 0L) {
            MapleStatEffect effect = applyfrom.getEffectForBuffStat(SecondaryStat.ThiefSteal);
            if (effect != null && applyto.isShouldDropAssassinsMark() && Randomizer.isSuccess(effect.getZ())) {
                applyto.setShouldDropAssassinsMark(false);
                final MapleMapItem mdrop = new MapleMapItem(new Item(!applyto.isBoss() ? 2431835 : 2431850, (byte) 0, (short) 1), applyto.getPosition(), applyto, applyfrom, (byte) 0, false, 0);
                mdrop.setOnlySelfID(applyfrom.getId());
                mdrop.setSourceOID(applyto.getObjectId());
                applyfrom.getMap().spawnMobDrop(mdrop, applyto, applyfrom);
            }
            effect = applyfrom.getEffectForBuffStat(SecondaryStat.PickPocket);
            if (effect != null) {
                int prop = effect.getProp(applyfrom);
                if (applier.effect.getSourceId() == 冷血連擊) {
                    prop = prop * applier.effect.getX() / 100;
                }
                for (int i = 0; i < applier.effect.getAttackCount(); i++) {
                    if (Randomizer.isSuccess(prop) && applyfrom.getBuffedIntZ(SecondaryStat.PickPocket) < effect.getY()) {
                        Point p = new Point(applyto.getPosition());
                        p.y = applyfrom.getMap().getFootholds().findBelow(applyto.getPosition()).getPoint1().y;
                        applyfrom.getMap().spawnMobMesoDrop(1, p, applyto, applyfrom, true, (byte) 0, 0, effect.getSourceId());
                        effect.unprimaryPassiveApplyTo(applyfrom);
                    }
                }
            }
            if ((effect = applyfrom.getSkillEffect(飛毒殺)) != null) {
                final MapleStatEffect effect1;
                if ((effect1 = applyfrom.getSkillEffect(致命飛毒殺)) != null) {
                    effect = effect1;
                }
                effect.applyMonsterEffect(applyfrom, applyto, effect.getDotTime() * 1000);
            }
            if (applier.effect != null && applier.effect.getSourceId() == 致命暗殺_1) {
                applyfrom.dispelEffect(殺意);
                if (applyfrom.getSkillEffect(黑影切斷) != null) {
                    applyfrom.getTempValues().put("致命暗殺減益OID", applyto.getObjectId());
                    applier.effect.unprimaryPassiveApplyTo(applyfrom);
                }
            }
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        MapleStatEffect eff = player.getEffectForBuffStat(SecondaryStat.DarkSight);
        if (eff != null && eff.getSourceId() == 盜賊.隱身術 && (eff = player.getSkillEffect(進階隱身術)) != null) {
            int prop = player.getMap().getAllAffectedAreasThreadsafe().stream().anyMatch(mist -> (mist.getSkillID() == 煙幕彈 || mist.getSkillID() == 暗影霧殺) && mist.getOwnerId() == player.getId() && mist.getArea().contains(player.getPosition())) ? 100 : eff.getProp(player);
            if (applier.effect != null && prop != 100) {
                switch (applier.effect.getSourceId()) {
                    case 致命暗殺:
                        prop = 100;
                        break;
                    case 致命暗殺_1:
                        prop = 0;
                        break;
                }
            }
            if (!Randomizer.isSuccess(prop)) {
                player.dispelEffect(SecondaryStat.DarkSight);
            }
        }
        if (!applier.ai.mobAttackInfo.isEmpty() && (eff = player.getEffectForBuffStat(SecondaryStat.FlipTheCoin)) != null) {
            eff.applyBuffEffect(player, player, 2100000000, false, false, true, null);
        }
        return 1;
    }

    @Override
    public int onAfterCancelEffect(MapleCharacter player, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 勇者掠奪術:
            case 血腥掠奪術:
                if (!applier.overwrite) {
                    final List<MapleMapItem> stealMesoObject = player.getMap().getStealMesoObject(player, -1, -1);
                    for (MapleMapItem item : stealMesoObject) {
                        player.getMap().disappearMapObject(item);
                    }
                }
                break;
        }
        return -1;
    }
}
