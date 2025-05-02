package Client.skills.handler.皇家騎士團;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Config.constants.skills.皇家騎士團_技能群組.破風使者;
import Config.constants.skills.皇家騎士團_技能群組.米哈逸;
import Config.constants.skills.皇家騎士團_技能群組.聖魂劍士;
import Config.constants.skills.皇家騎士團_技能群組.閃雷悍將;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Packet.EffectPacket;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Map;

import static Client.skills.handler.HexaSKILL.*;
import static Config.constants.skills.皇家騎士團_技能群組.暗夜行者.*;

public class 暗夜行者 extends AbstractSkillHandler {

    public 暗夜行者() {
        jobs = new MapleJob[]{
                MapleJob.暗夜行者1轉,
                MapleJob.暗夜行者2轉,
                MapleJob.暗夜行者3轉,
                MapleJob.暗夜行者4轉
        };

        for (Field field : Config.constants.skills.皇家騎士團_技能群組.暗夜行者.class.getDeclaredFields()) {
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
        Skill skill;
        int[] ss = {Royal_Link_暗夜行者_自然旋律, 影跳_JUMP_BEGIN, Skill_急速迴避};
        for (int i : ss) {
            skill = SkillFactory.getSkill(i);
            if (chr.getJob() >= i / 10000 && skill != null && chr.getSkillLevel(skill) <= 0) {
                applier.skillMap.put(skill.getId(), new SkillEntry(1, skill.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 強化影之槍:
                return 400041008;
            case 強化暗影侍從:
                return 400041028;
            case 強化暗影吞噬:
                return 400041037;
            case 強化暗影投擲:
                return 400041059;
            case HEXA_靜謐之夜_I:
            case HEXA_靜謐之夜_II:
                return HEXA_靜謐之夜;
            case HEXA_五連投擲_VI_延伸_I:
            case HEXA_五連投擲_VI_延伸_II:
            case HEXA_五連投擲_VI_延伸_III:
                return HEXA_五連投擲_VI;
            case 星塵爆炸:
                return 星塵;
            case 14101021: //三倍緩慢
                return 三連投擲;
            case 五倍緩慢:
                return 四連投擲;
            case 四倍緩慢:
                return 五連投擲;
            case SUMMON_暗影蝙蝠_攻擊:
            case SUMMON_暗影蝙蝠_反彈:
            case SUMMON_暗影蝙蝠_召喚獸:
                return Switch_type_Magic_暗影蝙蝠;
            case 吸收活力_1:
                return 吸收活力;
            case 影幻_40:
            case 影幻_20:
                return 影幻;
            case 影之槍_1:
            case 影之槍_2:
                return 影之槍;
            case 暗影投擲_1:
                return 暗影投擲;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case SUMMON_暗影蝙蝠_攻擊:
                effect.getInfo().put(MapleStatInfo.mobCount, 3);
                return 1;
            case SUMMON_元素闇黑:
                effect.getInfo().put(MapleStatInfo.time, 2100000000); /* 值 2100000000 疑似永久開關技能 */
                statups.put(SecondaryStat.ElementDarkness, 1);
                return 1;
            case 速度激發:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieSpeed, 40); //speed + 40 180s
                statups.put(SecondaryStat.IndieJump, 20); //jump + 20 180s
                return 1;
            case Switch_type_Magic_暗影蝙蝠:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.NightWalkerBat, 1);
                return 1;
            case Skill_暗影僕從:
                statups.put(SecondaryStat.ShadowServant, 1);
                return 1;
            case Switch_type_Dead_闇黑蔓延:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.ReviveOnce, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case Switch_type_Spawn_飢餓蝙蝠:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.IndieDotHealHP, 1);
                return 1;
            case Rang_影跡:
                statups.put(SecondaryStat.Stance, 100);
                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 吸收活力:
                statups.put(SecondaryStat.SiphonVitality, 1);
                statups.put(SecondaryStat.IncMaxHP, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 道米尼奧:
                statups.put(SecondaryStat.Dominion, 1);
                statups.put(SecondaryStat.IndiePMdR, effect.getInfo().get(MapleStatInfo.indiePMdR));
                statups.put(SecondaryStat.IndieStance, effect.getInfo().get(MapleStatInfo.indieStance));
                statups.put(SecondaryStat.IndieCr, effect.getInfo().get(MapleStatInfo.indieCr));
                return 1;
            case 影幻:
                statups.put(SecondaryStat.ShadowIllusion, 1);
                return 1;
            case SUMMON_暗影蝙蝠_召喚獸:
                effect.getInfo().put(MapleStatInfo.time, 60000); /* 召喚 x1 蝙蝠 持續60s */
                return 1;
            case 黑暗面:
                statups.put(SecondaryStat.DarkSight, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 極速暗殺:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case Royal_西格諾斯騎士:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 守護者的榮耀:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 影之槍:
                statups.put(SecondaryStat.WizardIgnite, effect.getLevel());
                return 1;
            case 暗影吞噬:
                statups.put(SecondaryStat.IndiePMdR, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 隱身術:
                statups.put(SecondaryStat.DarkSight, effect.getX());
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 影幻) {
            chr.getSkillEffect(Skill_暗影僕從).applyTo(chr);
            chr.getSkillEffect(影幻_40).applyTo(chr);
            chr.getSkillEffect(影幻_20).applyTo(chr);
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case Switch_type_Magic_暗影蝙蝠: {
                if (applyto.getBuffedIntValue(SecondaryStat.NightWalkerBat) > 0) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                }
                return 1;
            }
            case 吸收活力: {
                final int min3 = Math.min(5, applyto.getBuffedIntValue(SecondaryStat.SiphonVitality) + 1);
                int y = applier.effect.getY();
                if (applyto.getSkillLevel(吸收活力_血魔加成) > 0) {
                    y += 300;
                }
                if (applyto.getSkillLevel(吸收活力_強化) > 0) {
                    applier.localstatups.put(SecondaryStat.IndiePDD, min3 * 100);
                }
                if (applyto.getSkillLevel(吸收活力_抗性提升) > 0) {
                    applier.localstatups.put(SecondaryStat.IndieAsrR, 2 * min3);
                }
                applier.localstatups.put(SecondaryStat.SiphonVitality, min3);
                applier.localstatups.put(SecondaryStat.IncMaxHP, y * min3);
                return 1;
            }
            case SUMMON_暗影蝙蝠_召喚獸: {
                applier.b7 = false;
                if (applyto.getSummonCountBySkill(SUMMON_暗影蝙蝠_召喚獸) >= 2 + (applyto.getSkillLevel(蝙蝠交流) > 0 ? 1 : 0) + (applyto.getSkillLevel(蝙蝠交流Ⅱ) > 0 ? 1 : 0) + (applyto.getSkillLevel(蝙蝠交流Ⅲ) > 0 ? 1 : 0)) {
                    return 0;
                }
                return 1;
            }
            case Switch_type_Dead_闇黑蔓延: {
                if (applier.passive) {
                    applier.localstatups.clear();
                    applier.duration = 2000;
                    applier.localstatups.put(SecondaryStat.NotDamaged, 1);
                    applyto.getClient().announce(EffectPacket.show黑暗重生(-1, Switch_type_Dead_闇黑蔓延, 3000));
                    applyto.getMap().broadcastMessage(applyto, EffectPacket.show黑暗重生(applyto.getId(), Switch_type_Dead_闇黑蔓延, 3000), false);
                }
                return 1;
            }
            case 守護者的榮耀: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000 || applyto.getJob() / 1000 == 5) {
                    return 0;
                }
                applyto.dispelEffect(聖魂劍士.守護者的榮耀);
                applyto.dispelEffect(烈焰巫師.守護者的榮耀);
                applyto.dispelEffect(破風使者.守護者的榮耀);
                applyto.dispelEffect(守護者的榮耀);
                applyto.dispelEffect(閃雷悍將.守護者的榮耀);
                applyto.dispelEffect(米哈逸.明日女皇);
                return 1;
            }
            case 暗影吞噬: {
                applier.localstatups.put(SecondaryStat.IndiePMdR, Math.min(applier.effect.getQ(), applier.effect.getY() * applyfrom.getSpecialStat().getShadowBite()));
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applyfrom.getEffectForBuffStat(SecondaryStat.ShadowSpear) != null && applyto.isAlive() && applier.effect != null && applier.effect.getSourceId() != SUMMON_ATTACK_闇黑天魔 && applier.effect.getSourceId() != 影之槍_1 && applyfrom.getCheatTracker().canNextBonusAttack(3000)) {
            applyfrom.getSkillEffect(影之槍_1).applyAffectedArea(applyfrom, applyto.getPosition());
        }
        if (applier.effect != null && applier.effect.getSourceId() == 暗影吞噬) {
            if (applyto != null) {
                if (applyto.isBoss()) {
                    applyfrom.getSpecialStat().addShadowBite(applier.effect.getDuration(), applier.effect.getW());
                } else if (!applyto.isAlive()) {
                    applyfrom.getSpecialStat().addShadowBite(applier.effect.getDuration(), 1);
                }
            }
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        final MapleStatEffect effecForBuffStat7 = player.getEffectForBuffStat(SecondaryStat.NightWalkerBat);
        if (applier.totalDamage > 0L && effecForBuffStat7 != null && applier.effect != null && applier.effect.getBulletCount() > 1) {
            player.getCheatTracker().addShadowBat();
            final MapleStatEffect skillEffect12;
            if ((skillEffect12 = player.getSkillEffect(SUMMON_暗影蝙蝠_召喚獸)) != null && player.getCheatTracker().canSpawnShadowBat()) {
                skillEffect12.applyTo(player);
            }
        }
        return 1;
    }
}
