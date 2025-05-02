package Client.skills.handler.皇家騎士團;

import Client.*;
import Client.skills.ExtraSkill;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Config.constants.skills.皇家騎士團_技能群組.破風使者;
import Config.constants.skills.皇家騎士團_技能群組.米哈逸;
import Config.constants.skills.皇家騎士團_技能群組.聖魂劍士;
import Config.constants.skills.貴族;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Opcode.Headler.OutHeader;
import Packet.MaplePacketCreator;
import tools.data.MaplePacketLittleEndianWriter;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static Client.skills.handler.HexaSKILL.*;
import static Config.constants.skills.皇家騎士團_技能群組.閃雷悍將.*;

public class 閃雷悍將 extends AbstractSkillHandler {

    public 閃雷悍將() {
        jobs = new MapleJob[]{
                MapleJob.閃雷悍將1轉,
                MapleJob.閃雷悍將2轉,
                MapleJob.閃雷悍將3轉,
                MapleJob.閃雷悍將4轉
        };

        for (Field field : Config.constants.skills.皇家騎士團_技能群組.閃雷悍將.class.getDeclaredFields()) {
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
        Skill skill = SkillFactory.getSkill(貴族.Royal_Link_貴族_自然旋律);
        if (skill != null && chr.getSkillLevel(skill) <= 0) {
            applier.skillMap.put(skill.getId(), new SkillEntry(1, skill.getMaxMasterLevel(), -1));
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 15141501:
                return 浪馳雷掣;
            case 15141001:
            case 15141000:
                return 消滅;
            case 強化神雷合一:
                return 400051007;
            case 強化巨鯊狂浪:
                return 400051016;
            case 強化雷神槍擊:
                return 400051044;
            case 強化槍雷連擊:
                return 400051058;
            case 海浪_1:
                return 海浪;
            case 神雷合一_1:
                return 神雷合一;
            case 雷神槍擊_1:
                return 雷神槍擊;
            case 槍雷連擊_1:
            case 槍雷連擊_2:
            case 槍雷連擊_3:
            case 槍雷連擊_4:
            case 槍雷連擊_5:
            case 槍雷連擊_6:
            case 槍雷連擊_7:
            case 槍雷連擊_8:
            case 槍雷連擊_9:
                return 槍雷連擊;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 開天闢地:
                statups.put(SecondaryStat.StrikerHyperElectric, 1);
                return 1;
            case SUMMON_元素雷電:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.CygnusElementSkill, 1); //主動BUFF是這個
                statups.put(SecondaryStat.IgnoreTargetDEF, 5); //被動BUFF是這個默認為5點
                return 1;
            case 引雷:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.ShadowPartner, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 颱風:
            case 疾風:
                effect.setOverTime(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 極速指虎:
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
            case 最終極速:
                statups.put(SecondaryStat.PartyBooster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 神雷合一:
                statups.put(SecondaryStat.LightningUnion, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 雷神槍擊:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.StrikerComboStack, 0);
                return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case SUMMON_元素雷電: {
                if (!applier.passive) {
                    return 1;
                }
                applier.localstatups.clear();
                applier.b3 = false;
                applier.maskedDuration = 30000;
                applier.buffz = Math.min(applyto.getBuffedIntZ(SecondaryStat.IgnoreTargetDEF) + 1, applier.effect.getAttackCount(applyto));
                applier.maskedstatups.put(SecondaryStat.IgnoreTargetDEF, (applier.buffz * (applyto.getSkillEffect(開天闢地) != null ? 9 : applier.effect.getX())));
                final SecondaryStatValueHolder buffStatValueHolder11;
                if ((buffStatValueHolder11 = applyto.getBuffStatValueHolder(SecondaryStat.CygnusElementSkill)) != null) {
                    applier.duration = buffStatValueHolder11.getLeftTime();
                }
                final MapleStatEffect skillEffect12;
                if ((skillEffect12 = applyto.getSkillEffect(引雷)) != null && applyto.isSkillCooling(引雷)) {
                    applyto.reduceSkillCooldown(引雷, skillEffect12.getY() * 1000);
                }
                return 1;
            }
            case 颱風:
            case 疾風: {
                applier.localstatups.put(SecondaryStat.IndieDamR, applyto.getBuffedIntZ(SecondaryStat.IgnoreTargetDEF) * applier.effect.getY());
                return 1;
            }
            case 開天闢地: {
                applyto.cancelSkillCooldown(疾風);
                applyto.cancelSkillCooldown(颱風);
                return 1;
            }
            case 守護者的榮耀: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000 || applyto.getJob() / 1000 == 5) {
                    return 0;
                }
                applyto.dispelEffect(聖魂劍士.守護者的榮耀);
                applyto.dispelEffect(烈焰巫師.守護者的榮耀);
                applyto.dispelEffect(破風使者.守護者的榮耀);
                applyto.dispelEffect(暗夜行者.守護者的榮耀);
                applyto.dispelEffect(Config.constants.skills.皇家騎士團_技能群組.閃雷悍將.守護者的榮耀);
                applyto.dispelEffect(米哈逸.明日女皇);
                return 1;
            }
            case 神雷合一: {
                if (applier.passive) {
                    return 0;
                }
                return 1;
            }
            case 雷神槍擊: {
                applier.duration = 2100000000;
                applier.localstatups.put(SecondaryStat.StrikerComboStack, Math.min(applier.effect.getX(), Math.max(0, applyto.getBuffedIntValue(SecondaryStat.StrikerComboStack) + 1)));
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        final MapleStatEffect effecForBuffStat9;
        if (containsJob(applyfrom.getJobWithSub()) && (effecForBuffStat9 = applyfrom.getEffectForBuffStat(SecondaryStat.CygnusElementSkill)) != null && effecForBuffStat9.makeChanceResult(applyfrom) && applier.effect != null && applier.effect.getSourceId() != 疾風 && applier.effect.getSourceId() != 颱風 && applier.effect.getSourceId() != 消滅) {
            effecForBuffStat9.unprimaryPassiveApplyTo(applyfrom);
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        MapleStatEffect eff = player.getEffectForBuffStat(SecondaryStat.LightningUnion);
        if (applier.effect != null && applier.totalDamage > 0L && eff != null) {
            final MapleClient client = player.getClient();
            final int sourceID = applier.effect.getSourceId();
            final int sourceID2 = eff.getSourceId();
            final int level = eff.getLevel();
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(OutHeader.ThunderSkillAction.getValue());
            mplew.writeInt(sourceID);
            mplew.writeInt(sourceID2);
            mplew.writeInt(level);
            mplew.writeInt(0);
            // do [int]
            client.announce(mplew.getPacket());
        }
        final MapleStatEffect skillEffect13 = player.getSkillEffect(雷神槍擊);
        if (applier.effect != null && applier.totalDamage > 0L && applier.ai.raytheonPike > 0 && skillEffect13 != null) {
            if (player.getBuffedIntValue(SecondaryStat.StrikerComboStack) >= 8) {
                List<ExtraSkill> eskills = new LinkedList<>();
                for (int i = 0; i < 7; i++) {
                    ExtraSkill eskill = new ExtraSkill(i == 0 ? 雷神槍擊 : 雷神槍擊_1, player.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                    eskills.add(eskill);
                }
                player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(雷神槍擊, eskills));
                player.dispelEffect(SecondaryStat.StrikerComboStack);
                return 1;
            }
            skillEffect13.unprimaryPassiveApplyTo(player);
        }
        return 1;
    }
}
