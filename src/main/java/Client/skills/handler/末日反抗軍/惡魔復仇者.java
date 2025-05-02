package Client.skills.handler.末日反抗軍;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.force.MapleForceFactory;
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
import Packet.ForcePacket;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Config.constants.skills.惡魔復仇者.*;

public class 惡魔復仇者 extends AbstractSkillHandler {

    public 惡魔復仇者() {
        jobs = new MapleJob[]{
                MapleJob.惡魔復仇者1轉,
                MapleJob.惡魔復仇者2轉,
                MapleJob.惡魔復仇者3轉,
                MapleJob.惡魔復仇者4轉
        };

        for (Field field : Config.constants.skills.惡魔復仇者.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int baseSkills(MapleCharacter chr, SkillClassApplier applier) {
        super.baseSkills(chr, applier);
        if (chr.getLevel() >= 10) {
            Skill skil;
            int[] ss = {血之限界, 超越, 效能提升, 轉換星力};
            for (int i : ss) {
                skil = SkillFactory.getSkill(i);
                if (skil != null && chr.getSkillLevel(skil) <= 0) {
                    applier.skillMap.put(i, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
                }
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 超越:
                return 血之限界;
            case 狂暴鬥氣:
            case 狂暴鬥氣_傳授:
                return 血之限界;
            case 31241501:
            case 31241502:
                return HexaSKILL.魔劍鎮魂曲;
            case 31241000:
            case 31241001:
                return 盾牌追擊;
            case HexaSKILL.強化惡魔狂亂:
                return 400011010;
            case HexaSKILL.強化赤紅火槍:
                return 400011038;
            case HexaSKILL.強化次元之刃:
                return 400011090;
            case HexaSKILL.強化亡靈:
                return 400011112;
            case 超越_十文字斬_1:
            case 超越_十文字斬_2:
            case 超越_十文字斬_3:
            case 超越_十文字斬_4:
                return 超越_十文字斬;
            case 超越_惡魔風暴_1:
            case 超越_惡魔風暴_2:
            case 超越_惡魔風暴_3:
            case 超越_惡魔風暴_4:
                return 超越_惡魔風暴;
            case 超越_月光斬_1:
            case 超越_月光斬_2:
            case 超越_月光斬_3:
            case 超越_月光斬_4:
                return 超越_月光斬;
            case 盾牌衝鋒_1:
                return 盾牌衝鋒;
            case 超越_逆十文字斬_1:
            case 超越_逆十文字斬_2:
            case 超越_逆十文字斬_3:
            case 超越_逆十文字斬_4:
                return 超越_逆十文字斬;
            case 盾牌追擊_攻擊:
                return 盾牌追擊;
            case 惡魔狂亂_魔族之血:
                return 惡魔狂亂;
            case 血腥盛宴_1:
            case 血腥盛宴_2:
            case 血腥盛宴_3:
                return 血腥盛宴;
            case 次元之刃_1:
                return 次元之刃;
            case 憤怒荊棘:
            case 憤怒荊棘_1:
            case 憤怒荊棘_2:
            case 亡靈_1:
                return 亡靈;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 超越:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.OverloadCount, 1);
                return 1;
            case 血之限界:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.LifeTidal, 3);
                return 1;
            case 超載解放:
                statups.put(SecondaryStat.IndieMHPR, effect.getInfo().get(MapleStatInfo.indieMhpR));
                return 1;
            case 噬魂爆發:
                effect.setHpR(effect.getInfo().get(MapleStatInfo.y) / 100.0);
                return 1;
            case 禁忌契約:
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 邪惡強化:
                statups.put(SecondaryStat.AsrR, effect.getInfo().get(MapleStatInfo.y));
                statups.put(SecondaryStat.TerR, effect.getInfo().get(MapleStatInfo.z));
                statups.put(SecondaryStat.DamAbsorbShield, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 急速療癒:
                statups.put(SecondaryStat.DiabolikRecovery, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.IndieMHPR, effect.getInfo().get(MapleStatInfo.indieMhpR));
                return 1;
            case 急速惡魔:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 防禦粉碎:
                monsterStatus.put(MonsterStatus.PDR, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.MDR, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 血獄:
                monsterStatus.put(MonsterStatus.Seal, 1);
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 惡魔韌性:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 惡魔狂亂:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                effect.getInfo().put(MapleStatInfo.cooltime, effect.getInfo().get(MapleStatInfo.z));
                statups.put(SecondaryStat.Frenzy, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 血腥盛宴:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.SiphonVitalityBarrier, 1);
                return 1;
            case 次元之刃_1:
                statups.put(SecondaryStat.DevilishPower, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 亡靈:
                statups.put(SecondaryStat.RevenantGauge, 1);
                return 1;
            case 亡靈_1:
                effect.getInfo().put(MapleStatInfo.time, 38500);
                statups.put(SecondaryStat.DeathDance, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        switch (applier.effect.getSourceId()) {
            case 盾牌追擊: {
                applier.pos = slea.readPos();
                List<Integer> oids = IntStream.range(0, slea.readByte()).mapToObj(i -> slea.readInt()).collect(Collectors.toList());
                chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(forceFactory.getMapleForce(chr, applier.effect, 0, oids)), true);
                return 1;
            }
            case 惡魔狂亂: {
                if (chr.getBuffedValue(SecondaryStat.Frenzy) != null) {
                    chr.registerSkillCooldown(applier.effect, true);
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 血之限界: {
                applier.buffz = applyto.getStat().getMaxHp();
                return 1;
            }
            case 超越_十文字斬:
            case 超越_惡魔風暴:
            case 超越_月光斬:
            case 超越_逆十文字斬: {
                if (!applier.passive) {
                    return 0;
                }
                applier.localstatups.put(SecondaryStat.Exceed, Math.min(applyto.getBuffedIntValue(SecondaryStat.Exceed) + 1, 4));
                return 1;
            }
            case 超越: {
                int value = applyto.getBuffedIntValue(SecondaryStat.OverloadCount) + 1;
                int lastAttack = applyto.getCheatTracker().getLastAttackSkill();
                if (lastAttack > 0 && applyto.getBuffSource(SecondaryStat.OverloadCount) > 0 && SkillConstants.getLinkedAttackSkill(lastAttack) != applyto.getBuffSource(SecondaryStat.Exceed)) {
                    ++value;
                }
                applier.localstatups.put(SecondaryStat.OverloadCount, Math.min(value, applyto.getStat().maxBeyondLoad));
                return 1;
            }
            case 超載解放: {
                int value = applyto.getBuffedIntValue(SecondaryStat.OverloadCount);
                applyto.setBuffStatValue(SecondaryStat.OverloadCount, 超越, value / 2 - 1);
                applyto.getSkillEffect(超越).unprimaryPassiveApplyTo(applyto);
                int hpRecover = applyto.getStat().getCurrentMaxHP() * applier.effect.getX() / 100 * value / applyto.getStat().maxBeyondLoad;
                if (!applyto.isDebug() && hpRecover > 0 && applyto.getEffectForBuffStat(SecondaryStat.Frenzy) != null) {
                    hpRecover = Math.min(applyto.getStat().getCurrentMaxHP() / 100, hpRecover);
                }
                applyto.addHPMP(hpRecover, 0, false);
                final MapleStatEffect eff = applyto.getSkillEffect(進階生命吸收);
                if (eff != null) {
                    applier.localstatups.put(SecondaryStat.IndiePMdR, eff.getY() * value / applyto.getStat().maxBeyondLoad);
                } else {
                    applier.localstatups.put(SecondaryStat.IndiePMdR, applier.effect.getIndiePMdR() * value / applyto.getStat().maxBeyondLoad);
                }
                return 1;
            }
            case 邪惡強化: {
                MapleStatEffect eff;
                if ((eff = applyto.getSkillEffect(邪惡強化_減少傷害)) != null) {
                    applier.localstatups.put(SecondaryStat.DamAbsorbShield, applier.effect.getX() + eff.getX());
                }
                if ((eff = applyto.getSkillEffect(邪惡強化_效能提升I)) != null) {
                    applier.localstatups.put(SecondaryStat.AsrR, applier.effect.getY() + eff.getX());
                }
                if ((eff = applyto.getSkillEffect(邪惡強化_效能提升II)) != null) {
                    applier.localstatups.put(SecondaryStat.TerR, applier.effect.getZ() + eff.getX());
                }
                return 1;
            }
            case 惡魔韌性: {
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
            case 惡魔狂亂: {
                if (!applier.passive && applyto.getBuffedValue(SecondaryStat.Frenzy) != null) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                    return 1;
                }
                applier.buffz = (100 - applyto.getStat().getHPPercent()) / applier.effect.getU();
                applier.localstatups.put(SecondaryStat.Frenzy, Math.max(1, applier.buffz));
                return 1;
            }
            case 血腥盛宴: {
                if (!applier.primary) {
                    return 0;
                }
                applier.overwrite = false;
                applier.startChargeTime = System.currentTimeMillis();
                applier.localstatups.put(SecondaryStat.IndieDamReduceR, (int) applier.startChargeTime);
                return 1;
            }
            case 次元之刃: {
                if (applyfrom.hasSummonBySkill(次元之刃)) {
                    applyto.cancelEffect(applier.effect, applier.overwrite, -1, applier.localstatups);
                    applyfrom.getSkillEffect(次元之刃_1).applyBuffEffect(applyfrom, 7000, applier.primary);
                    return 0;
                }
                return 1;
            }
            case 亡靈: {
                applier.buffz = (int) applyfrom.getTempValues().getOrDefault("亡靈HP消耗", 0);
                applyfrom.getTempValues().remove("亡靈HP消耗");
                return 1;
            }
            case 亡靈_1: {
                applier.buffz = (int) applyfrom.getTempValues().getOrDefault("亡靈怒氣", 0);
                applyfrom.getTempValues().remove("亡靈怒氣");
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        int value = player.getBuffedIntValue(SecondaryStat.OverloadCount);
        MapleStatEffect eff = player.getSkillEffect(超越苦痛);
        if ((eff) != null && value > 0) {
            value = Math.min(0, value - eff.getX());
        }
        if (applier.hpHeal > 0) {
            applier.hpHeal -= applier.hpHeal * value / 100;
        }

        if(applier.effect == null){
            return -1;
        }
        if (player.getSkillEffect(超越) != null) {
            int linkSkill = SkillConstants.getLinkedAttackSkill(applier.effect.getSourceId());
            if (applier.effect != null && (linkSkill == 超越_十文字斬 || linkSkill == 超越_逆十文字斬 || linkSkill == 超越_惡魔風暴 || linkSkill == 超越_月光斬)) {
                player.getSkillEffect(linkSkill).unprimaryPassiveApplyTo(player);
                player.getSkillEffect(超越).unprimaryPassiveApplyTo(player);
            }
            if (applier.effect != null && applier.effect.getSourceId() == 瞬千刃) {
                int n = player.getBuffedIntValue(SecondaryStat.OverloadCount) + 4;
                player.setBuffStatValue(SecondaryStat.OverloadCount, 超越, n);
                player.getSkillEffect(超越).unprimaryPassiveApplyTo(player);
            }

            if (applier.effect != null && applier.totalDamage > 0L) {
                switch (applier.effect.getSourceId()) {
                    case 血腥盛宴_1:
                    case 血腥盛宴_2:
                    case 血腥盛宴_3: {
                        player.addHPMP(applier.effect.getX(), 0);
                        player.registerSkillCooldown(player.getSkillEffect(血腥盛宴), true);
                        break;
                    }
                }
            }
        }
        return 1;
    }
}
