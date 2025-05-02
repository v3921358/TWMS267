package Client.skills.handler.英雄團;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.force.MapleForceFactory;
import Client.skills.ExtraSkill;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.HexaSKILL;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.SkillConstants;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMap;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Net.server.maps.MapleSummon;
import Opcode.Headler.OutHeader;
import Packet.EffectPacket;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import Packet.SummonPacket;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

import static Config.constants.skills.龍魔導士.*;

public class 龍魔導士 extends AbstractSkillHandler {

    public 龍魔導士() {
        jobs = new MapleJob[]{
                MapleJob.龍魔導士,
                MapleJob.龍魔導士1轉,
                MapleJob.龍魔導士2轉,
                MapleJob.龍魔導士3轉,
                MapleJob.龍魔導士4轉
        };

        for (Field field : Config.constants.skills.龍魔導士.class.getDeclaredFields()) {
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
        int[] ss = {回家, 英雄的回響, 繼承的意志};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == 英雄的回響) {
                continue;
            }
            skil = SkillFactory.getSkill(i);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(i, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 22201501:
                return HexaSKILL.龍星群;
            case 22201001:
            case 22201000:
                return 魔力之環IV;
            case HexaSKILL.強化元素滅殺破:
                return 400021012;
            case HexaSKILL.強化聖龍突襲:
                return 400021046;
            case HexaSKILL.強化星宮射線:
                return 400021073;
            case HexaSKILL.強化魔力之旋:
                return 400021095;
            case 龍飛行:
                return 80001000;
            case 龍之捷_1:
            case 龍之捷_2:
            case 風之捷:
            case 風之捷_1:
            case 風之捷_攻擊:
            case 迅捷_回來吧:
                return 龍之捷;
            case 潛水_回來吧:
            case 龍之躍_攻擊:
            case 閃雷之躍:
            case 閃雷之躍_攻擊:
            case 閃雷之捷:
            case 閃雷之捷_攻擊:
                return 龍之躍;
            case 氣息_回來吧:
            case 大地氣息_攻擊:
            case 塵土之躍:
            case 風之氣息:
            case 迅捷_回來吧_1:
                return 龍之氣息;
            case 龍之主_1:
                return 龍之主;
            case 元素滅殺破_1:
            case 元素滅殺破_2:
            case 元素滅殺破_3:
            case 元素滅殺破_4:
                return 元素滅殺破;
            case 歐尼斯之氣息:
            case 粉碎_回歸:
                return 聖龍突襲;
            case 魔力之環IV_1:
                return 魔力之環IV;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 英雄的回響:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
                statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
                return 1;
            case 魔心防禦:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.MagicGuard, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 英雄歐尼斯:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 歐尼斯的意志:
                statups.put(SecondaryStat.Stance, effect.getInfo().get(MapleStatInfo.prop));
                return 1;
            case 龍之主:
                effect.getInfo().put(MapleStatInfo.time, effect.getInfo().get(MapleStatInfo.y) * 1000);
                statups.put(SecondaryStat.NotDamaged, 1);
                statups.put(SecondaryStat.NewFlying, 1);
                statups.put(SecondaryStat.RideVehicleExpire, 1939007);
                return 1;
            case 魔法抵抗:
                statups.put(SecondaryStat.MagicResistance, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 自然力重置:
                statups.put(SecondaryStat.ElementalReset, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 迅捷_回來吧:
                monsterStatus.put(MonsterStatus.Weakness, effect.getInfo().get(MapleStatInfo.x));
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        switch (applier.effect.getSourceId()) {
            case 氣息_回來吧: {
                final MapleStatEffect skillEffect9;
                if ((skillEffect9 = chr.getSkillEffect(迅捷_回來吧_1)) != null) {
                    skillEffect9.applyTo(chr);
                }
                return 1;
            }
            case 魔法殘骸:
            case 強化的魔法殘骸: {
                final Map<Integer, Point> wreckagesMap = chr.getWreckagesMap();
                final ArrayList<Integer> oids = new ArrayList<>();
                for (MapleMapObject monster : chr.getMap().getMapObjectsInRange(chr.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                    oids.add(monster.getObjectId());
                    if (oids.size() >= applier.effect.getMobCount()) {
                        break;
                    }
                }
                if (!wreckagesMap.isEmpty() && !oids.isEmpty()) {
                    chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(forceFactory.getMapleForce(chr, applier.effect, oids, wreckagesMap.values())), true);
                    // SendDeleteWreckage
                    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                    mplew.writeShort(OutHeader.LP_DEL_WRECKAGE.getValue());
                    mplew.writeInt(chr.getId());
                    mplew.writeInt(wreckagesMap.size());
                    mplew.write(0);
                    mplew.write(0);//V.181 new
                    wreckagesMap.keySet().forEach(mplew::writeInt);
                    chr.getMap().broadcastMessage(chr, mplew.getPacket(), true);
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 回家: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 龍之主: {
                return 1;
            }
            case 英雄歐尼斯: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(Config.constants.skills.狂狼勇士.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.龍魔導士.英雄歐尼斯);
                applyto.dispelEffect(Config.constants.skills.夜光.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.精靈遊俠.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.幻影俠盜.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.隱月.英雄誓約);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        final MapleForceFactory mmf = MapleForceFactory.getInstance();
        if (applier.effect != null && SkillConstants.eD(applier.effect.getSourceId())) {
            MapleStatEffect eff = applyfrom.getSkillEffect(魔法殘骸);
            if (applyfrom.getSkillEffect(強化的魔法殘骸) != null) {
                eff = applyfrom.getSkillEffect(強化的魔法殘骸);
            }
            if (eff != null && applyfrom.getEvanWreckages().size() < eff.getX()) {
                final int addWreckages = applyfrom.addWreckages(new Point(applyto.getPosition()), eff.getDuration());
                final MapleMap map = applyfrom.getMap();
                final int id = applyfrom.getId();
                final Point position = applyto.getPosition();
                final int n7 = eff.getDuration() / 1000;
                final int sourceid = eff.getSourceId();
                final int size = applyfrom.getEvanWreckages().size();
                byte[] packet = EffectPacket.DragonWreckage(id, position, n7, addWreckages, sourceid, 1, size);
                map.broadcastMessage(applyfrom, packet, true);
            }
        }
        final MapleSummon summon = applyfrom.getSummonBySkillID(星宮射線);
        if (applier.effect != null && summon != null && containsJob(applier.effect.getSourceId() / 10000) && summon.getAcState1() < summon.getEffect().getX()) {
            summon.setAcState1(Math.min(summon.getAcState1() + 1, summon.getEffect().getX()));
            applyfrom.getMap().broadcastMessage(applyfrom, ForcePacket.forceAtomCreate(mmf.getMapleForce(applyfrom, summon.getEffect(), applyto.getObjectId(), 0, null, applyto.getPosition())), true);
            applyfrom.getMap().broadcastMessage(applyfrom, SummonPacket.SummonedStateChange(summon, 2, summon.getAcState1(), 0), true);
            if (summon.getAcState1() >= summon.getEffect().getX()) {
                final MapleMap map2 = applyfrom.getMap();
                map2.broadcastMessage(applyfrom, SummonPacket.SummonMagicCircleAttack(summon, 10, summon.getPosition()), true);
            }
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.effect != null) {
            if (applier.effect.getSourceId() == 元素滅殺破) {
                List<ExtraSkill> eskills = new LinkedList<>();
                for (int i = 0; i < 4; i++) {
                    ExtraSkill eskill = new ExtraSkill(元素滅殺破_1 + i, player.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                    eskills.add(eskill);
                }
                player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), eskills));
            }
            final MapleStatEffect skillEffect14;
            if (SkillConstants.i0(applier.effect.getSourceId()) && (skillEffect14 = player.getSkillEffect(交感)) != null && player.getBuffStatValueHolder(交感) == null) {
                skillEffect14.applyTo(player);
            }
        }
        return 1;
    }
}
