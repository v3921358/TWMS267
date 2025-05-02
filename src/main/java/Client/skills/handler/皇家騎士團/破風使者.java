package Client.skills.handler.皇家騎士團;

import Client.*;
import Client.force.MapleForceAtom;
import Client.force.MapleForceFactory;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.SkillConstants;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Config.constants.skills.皇家騎士團_技能群組.米哈逸;
import Config.constants.skills.皇家騎士團_技能群組.聖魂劍士;
import Config.constants.skills.皇家騎士團_技能群組.閃雷悍將;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Packet.ForcePacket;
import tools.Randomizer;
import tools.Triple;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static Client.skills.handler.HexaSKILL.*;
import static Config.constants.skills.皇家騎士團_技能群組.破風使者.*;

public class 破風使者 extends AbstractSkillHandler {

    public 破風使者() {
        jobs = new MapleJob[]{
                MapleJob.破風使者1轉,
                MapleJob.破風使者2轉,
                MapleJob.破風使者3轉,
                MapleJob.破風使者4轉
        };

        for (Field field : Config.constants.skills.皇家騎士團_技能群組.破風使者.class.getDeclaredFields()) {
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
        Skill skill = SkillFactory.getSkill(Royal_Link_破風使者_自然旋律);
        if (skill != null && chr.getSkillLevel(skill) <= 0) {
            applier.skillMap.put(skill.getId(), new SkillEntry(1, skill.getMaxMasterLevel(), -1));
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 13141501:
            case 13141502:
            case 13141503:
            case 13141504:
            case 13141505:
                return 風之聖諭;
            case 13141000:
                return 天空之歌;
            case 強化狂風呼嘯:
                return 400031003;
            case 強化風轉奇想:
                return 400031022;
            case 強化西爾芙之壁:
                return 400031030;
            case 強化漩渦巨球:
                return 400031058;
            case SUMMON_攻擊型態_風妖精之箭I:
                return Switch_type_風妖精之箭I;
            case SUMMON_攻擊型態_風妖精之箭II_攻擊:
                return Switch_type_風妖精之箭II;
            case SUMMON_攻擊型態_風妖精之箭Ⅲ_攻擊:
                return Switch_type_風妖精之箭Ⅲ;
            case 狂風呼嘯_1:
                return 狂風呼嘯;
            case 西爾芙之壁_1:
                return 西爾芙之壁;
            case 漩渦巨球_1:
                return 漩渦巨球;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case SUMMON_元素風暴:
                effect.getInfo().put(MapleStatInfo.time, 2100000000); /* 值 2100000000 疑似永久開關技能 */
                statups.put(SecondaryStat.CygnusElementSkill, 1);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR)); /* 啟動技能間傷害5%增加 */
                return 1;
            case Switch_type_風妖精之箭I:
            case Switch_type_風妖精之箭II:
            case Switch_type_風妖精之箭Ⅲ:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.TriflingWhimOnOff, 1);
                return 1;
            case 追蹤箭頭:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.GuidedArrow, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 風之祈禱:
                statups.put(SecondaryStat.IllusionStep, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.ACCR, effect.getInfo().get(MapleStatInfo.y));
                statups.put(SecondaryStat.DEXR, effect.getInfo().get(MapleStatInfo.prop));
                statups.put(SecondaryStat.IndieMHPR, effect.getInfo().get(MapleStatInfo.indieMhpR));
                return 1;
            case 暴風加護:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 30000);
                //statups.put(MapleBuffStat.StormWhim, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.StormBringer, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case SUMMON_翡翠花園:
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.z));
                return 1;
            case 強化翡翠花園:
                monsterStatus.put(MonsterStatus.PDR, effect.getInfo().get(MapleStatInfo.w));
                monsterStatus.put(MonsterStatus.Speed, effect.getInfo().get(MapleStatInfo.z));
                return 1;
            case 貫穿箭:
                //不存在時間
                effect.getInfo().put(MapleStatInfo.time, 210000000);
                monsterStatus.put(MonsterStatus.AddDamSkill, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case Royal_西格諾斯騎士:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 會心之眼:
                statups.put(SecondaryStat.SharpEyes, (effect.getInfo().get(MapleStatInfo.x) << 8) + effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 季風:
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 守護者的榮耀:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 風暴使者:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 20000);
                statups.put(SecondaryStat.StormBringer, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 狂風呼嘯:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.HowlingGaleStack, 0);
                return 1;
            case Config.constants.skills.皇家騎士團_技能群組.破風使者.西爾芙之壁:
                statups.put(SecondaryStat.WindBreakerStormGuard, effect.getInfo().get(MapleStatInfo.w));
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        switch (applier.effect.getSourceId()) {
            case Switch_type_風妖精之箭I: {
                if (chr.getSkillEffect(Switch_type_風妖精之箭Ⅲ) != null) {
                    applier.effect = chr.getSkillEffect(Switch_type_風妖精之箭Ⅲ);
                    return 1;
                }
                if (chr.getSkillEffect(Switch_type_風妖精之箭II) != null) {
                    applier.effect = chr.getSkillEffect(Switch_type_風妖精之箭II);
                    return 1;
                }
                return 1;
            }
            case 風轉奇想: {
                List<Integer> moboids = new ArrayList<>();
                for (MapleMapObject obj : chr.getMap().getMapObjectsInRange(chr.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                    moboids.add(obj.getObjectId());
                    if (moboids.size() >= applier.effect.getMobCount()) {
                        break;
                    }
                }
                chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(forceFactory.getMapleForce(chr, applier.effect, 0, moboids)), true);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 守護者的榮耀: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000 || applyto.getJob() / 1000 == 5) {
                    return 0;
                }
                applyto.dispelEffect(聖魂劍士.守護者的榮耀);
                applyto.dispelEffect(烈焰巫師.守護者的榮耀);
                applyto.dispelEffect(Config.constants.skills.皇家騎士團_技能群組.破風使者.守護者的榮耀);
                applyto.dispelEffect(暗夜行者.守護者的榮耀);
                applyto.dispelEffect(閃雷悍將.守護者的榮耀);
                applyto.dispelEffect(米哈逸.明日女皇);
                return 1;
            }
            case 狂風呼嘯: {
                final int value = Math.min(applyto.getBuffedIntValue(SecondaryStat.HowlingGaleStack) + (applier.passive ? 1 : -1), 2);
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.HowlingGaleStack);
                if (!applier.primary || value < 0 || applier.passive && mbsvh != null && System.currentTimeMillis() < mbsvh.startTime + applier.effect.getX() * 700L) {
                    return 0;
                }
                applier.localstatups.put(SecondaryStat.HowlingGaleStack, value);
                return 1;
            }
            case 西爾芙之壁: {
                final SecondaryStatValueHolder buffStatValueHolder32;
                if (applier.primary || (buffStatValueHolder32 = applyto.getBuffStatValueHolder(SecondaryStat.WindBreakerStormGuard)) == null) {
                    return 1;
                }
                final int max2;
                if ((max2 = Math.max(applyto.getBuffedIntValue(SecondaryStat.WindBreakerStormGuard) - 1, 0)) > 0) {
                    applier.duration = buffStatValueHolder32.getLeftTime();
                    applier.localstatups.put(SecondaryStat.WindBreakerStormGuard, max2);
                    return 1;
                }
                applier.overwrite = false;
                applier.localstatups.clear();
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        final MapleForceFactory mmf = MapleForceFactory.getInstance();
        final MapleStatEffect effecForBuffStat5;
        if (containsJob(applyfrom.getJobWithSub()) && (effecForBuffStat5 = applyfrom.getEffectForBuffStat(SecondaryStat.StormBringer)) != null && applyto.isAlive() && effecForBuffStat5.makeChanceResult(applyfrom) && applier.effect != null) {
            final int n5 = 0;
            applyfrom.getMap().broadcastMessage(applyfrom, ForcePacket.forceAtomCreate(mmf.getMapleForce(applyfrom, effecForBuffStat5, n5)), true);
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        final MapleForceFactory mff = MapleForceFactory.getInstance();
        MapleStatEffect effecForBuffStat6;
        if ((effecForBuffStat6 = player.getEffectForBuffStat(SecondaryStat.TriflingWhimOnOff)) != null && applier.effect != null) {
            List<MapleMapObject> mobs = player.getMap().getMapObjectsInRect(applier.effect.calculateBoundingBox(player.getPosition(), player.isFacingLeft(), 500), Collections.singletonList(MapleMapObjectType.MONSTER));
            final ArrayList<Integer> list = new ArrayList<>();
            player.getMap().getAllMonster().forEach(mob -> {
                if (mob.isBoss()) {
                    list.add(mob.getObjectId());
                }
            });
            mobs.forEach(mob -> list.add(mob.getObjectId()));
            final MapleStatEffect skillEffect9;
            if ((skillEffect9 = player.getSkillEffect(Switch_type_風妖精之箭II)) != null) {
                effecForBuffStat6 = skillEffect9;
            }
            final MapleStatEffect skillEffect10;
            if ((skillEffect10 = player.getSkillEffect(Switch_type_風妖精之箭Ⅲ)) != null) {
                effecForBuffStat6 = skillEffect10;
            }
            if (!list.isEmpty() && effecForBuffStat6.getSourceId() != SkillConstants.getLinkedAttackSkill(applier.effect.getSourceId())) {
                if (effecForBuffStat6.makeChanceResult(player)) {
                    final MapleStatEffect skillEffect11;
                    if ((skillEffect11 = player.getSkillEffect(effecForBuffStat6.getSourceId() == Switch_type_風妖精之箭Ⅲ ? effecForBuffStat6.getSourceId() + 7 : effecForBuffStat6.getSourceId() + 5)) != null) {
                        // Fixed: 風妖精之箭次數
                        int times = effecForBuffStat6.getX();
                        for (int i = 0; i < times; i++) {
                            player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, skillEffect11, 0, list)), true);
                        }
                    }
                } else if (Randomizer.nextInt(100) <= effecForBuffStat6.getSubProp()) {
                    player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, effecForBuffStat6, 0, list)), true);
                }
            }
        }
        final MapleStatEffect effecForBuffStat9 = player.getEffectForBuffStat(SecondaryStat.WindBreakerStormGuard);
        if (applier.totalDamage > 0L && effecForBuffStat9 != null && player.getCheatTracker().canNextAllRocket(西爾芙之壁_1, effecForBuffStat9.getW2() * 1000)) {
            final List<MapleMapObject> mapObjectsInRange2 = player.getMap().getMapObjectsInRange(player.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER));
            final ArrayList<Integer> list3 = new ArrayList<>();
            mapObjectsInRange2.forEach(sx2 -> list3.add(sx2.getObjectId()));
            if (!list3.isEmpty()) {
                final ArrayList<Triple<Integer, Integer, Map<Integer, MapleForceAtom>>> list4 = new ArrayList<>();
                for (int l = 0; l < effecForBuffStat9.getQ2(); ++l) {
                    list4.add(new Triple<>(西爾芙之壁_1, 51, Collections.singletonMap(list3.get(Randomizer.nextInt(list3.size())), mff.getMapleForce(player, player.getSkillEffect(西爾芙之壁_1), 0))));
                }
                player.getMap().broadcastMessage(player, ForcePacket.forceTeleAtomCreate(player.getId(), 西爾芙之壁_1, list4), true);
            }
        }
        return 1;
    }
}
