package Client.skills.handler.超新星;

import Client.*;
import Client.force.MapleForceFactory;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.HexaSKILL;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleSummon;
import Packet.ForcePacket;
import Packet.SummonPacket;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Config.constants.skills.天使破壞者.*;

public class 天使破壞者 extends AbstractSkillHandler {

    public 天使破壞者() {
        jobs = new MapleJob[]{
                MapleJob.天使破壞者,
                MapleJob.天使破壞者1轉,
                MapleJob.天使破壞者2轉,
                MapleJob.天使破壞者3轉,
                MapleJob.天使破壞者4轉
        };

        for (Field field : Config.constants.skills.天使破壞者.class.getDeclaredFields()) {
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
        int[] ss = {專用咒語, 繼承人, 魔法起重機, 白日夢, 魔法變身};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == 專用咒語) {
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
            case 65141502:
            case 65141501:
                return 65141500;
            case 65141000:
                return 三位一體;
            case HexaSKILL.強化能量爆炸:
                return 400051011;
            case HexaSKILL.強化聚光燈:
                return 400051018;
            case HexaSKILL.強化小萌新吉祥物:
                return 400051046;
            case HexaSKILL.強化三位一體融合:
                return 400051072;
            case 魔法跳耀_1:
                return 魔法跳耀;
            case 刺殺爆破_1:
                return 刺殺爆破;
            case 靈魂探求者_攻擊:
                return 靈魂探求者;
            case 三位一體_2擊:
            case 三位一體_3擊:
                return 三位一體;
            case 靈魂震動_1:
                return 靈魂震動;
            case 靈魂探求者精通:
                return 索魂精通;
            case 聚光燈_1:
            case 聚光燈_2:
            case 聚光燈_3:
                return 聚光燈;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 專用咒語:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
                statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
                return 1;
            case 靈魂傳動:
                statups.put(SecondaryStat.PowerTransferGauge, effect.getInfo().get(MapleStatInfo.y) * 1000);
                return 1;
            case 凝視靈魂:
                statups.put(SecondaryStat.SoulGazeCriDamR, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 索魂精通:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.AngelicBursterSoulSeeker, 1);
                return 1;
            case 終極契約:
                statups.put(SecondaryStat.IndieStance, effect.getInfo().get(MapleStatInfo.indieStance));
                statups.put(SecondaryStat.Stance, 0);
                statups.put(SecondaryStat.EnrageCr, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.IndieTerR, effect.getInfo().get(MapleStatInfo.terR));
                statups.put(SecondaryStat.IndieAsrR, effect.getInfo().get(MapleStatInfo.asrR));
                return 1;
            case 靈魂深造:
                statups.put(SecondaryStat.IndieBDR, effect.getInfo().get(MapleStatInfo.indieBDR));
                statups.put(SecondaryStat.IndieIgnoreMobpdpR, effect.getInfo().get(MapleStatInfo.indieIgnoreMobpdpR));
                statups.put(SecondaryStat.SoulExalt, 1);
                return 1;
            case 超新星之勇士:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 能量爆炸:
                statups.put(SecondaryStat.EnergyBust, 1);
                return 1;
            case 魔力綵帶:
                monsterStatus.put(MonsterStatus.Fatality, 1);
                return 1;
            case 刺殺爆破_1:
            case 刺殺爆破:
                monsterStatus.put(MonsterStatus.Explosion, 1);
                return 1;
            case 親和力Ⅳ:
                effect.getInfo().put(MapleStatInfo.time, 5000);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 聚光燈:
                statups.put(SecondaryStat.FifthSpotLight, effect.getLevel());
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        if (applier.effect.getSourceId() == 靈魂探求者) {
            applier.pos = slea.readPos();
            List<Integer> oids = IntStream.range(0, slea.readByte()).mapToObj(i -> slea.readInt()).collect(Collectors.toList());
            chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(forceFactory.getMapleForce(chr, applier.effect, 0, oids)), true);
            chr.handelAngelReborn(applier.effect);
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 白日夢: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 靈魂傳動: {
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.PowerTransferGauge);
                if (mbsvh != null) {
                    final int value = Math.min(applyto.getBuffedIntValue(SecondaryStat.PowerTransferGauge), 99999);
                    applier.duration = mbsvh.getLeftTime();
                    applier.localstatups.put(SecondaryStat.PowerTransferGauge, value);
                }
                return 1;
            }
            case 索魂精通: {
                if (applyto.getBuffedValue(SecondaryStat.AngelicBursterSoulSeeker) != null) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                }
                return 1;
            }
            case 三位一體: {
                int min = Math.min(applyto.getBuffedIntValue(SecondaryStat.Trinity) / applier.effect.getX() + 1, 4);
                if (min > 3) {
                    return 0;
                }
                applier.localstatups.put(SecondaryStat.Trinity, min * applier.effect.getX());
                return 1;
            }
            case 能量爆炸: {
                if (!applier.primary) {
                    applyto.dispelEffect(Config.constants.skills.天使破壞者.能量爆炸);
                    return 0;
                }
                return 1;
            }
            case 聚光燈: {
                if (!applier.primary) {
                    return 0;
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        final MapleForceFactory mmf = MapleForceFactory.getInstance();
        final MapleStatEffect effecForBuffStat11 = applyfrom.getEffectForBuffStat(SecondaryStat.AngelicBursterSoulSeeker);
        if (applier.totalDamage > 0L && effecForBuffStat11 != null && effecForBuffStat11.makeChanceResult(applyfrom) && applier.effect != null && applier.effect.getSourceId() != 繼承人 && applier.effect.getSourceId() != 靈魂探求者_攻擊) {
            applyfrom.getMap().broadcastMessage(applyfrom, ForcePacket.forceAtomCreate(mmf.getMapleForce(applyfrom, effecForBuffStat11, 0, Collections.singletonList(applyto.getObjectId()))), true);
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        final MapleStatEffect effecForBuffStat12 = player.getEffectForBuffStat(SecondaryStat.PowerTransferGauge);
        if (applier.totalDamage > 0L && effecForBuffStat12 != null) {
            player.setBuffStatValue(SecondaryStat.PowerTransferGauge, 靈魂傳動, (int) Math.min(player.getBuffedIntValue(SecondaryStat.PowerTransferGauge) + applier.totalDamage * effecForBuffStat12.getY() / 100L, 99999L));
            effecForBuffStat12.unprimaryPassiveApplyTo(player);
        }
        player.handelAngelReborn(applier.effect);
        final MapleSummon summonBySkillID4 = player.getSummonBySkillID(小萌新吉祥物);
        if ((applier.totalDamage > 0L & applier.effect != null) && applier.effect.getSourceId() != 小萌新吉祥物 && summonBySkillID4 != null && player.getCheatTracker().canNextAllRocket(小萌新吉祥物, 1500)) {
            player.getClient().announce(SummonPacket.SummonedAssistAttackRequest(player.getId(), summonBySkillID4.getObjectId(), Randomizer.nextBoolean() ? 10 : 11));
        }
        return 1;
    }
}
