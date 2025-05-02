package Client.skills.handler.末日反抗軍;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.HexaSKILL;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.maps.MapleSummon;
import Packet.SummonPacket;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.惡魔殺手.*;

public class 惡魔殺手 extends AbstractSkillHandler {

    public 惡魔殺手() {
        jobs = new MapleJob[]{
                MapleJob.惡魔殺手1轉,
                MapleJob.惡魔殺手2轉,
                MapleJob.惡魔殺手3轉,
                MapleJob.惡魔殺手4轉
        };

        for (Field field : Config.constants.skills.惡魔殺手.class.getDeclaredFields()) {
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
            Skill skil = SkillFactory.getSkill(死亡詛咒);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(skil.getId(), new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 31141503:
            case 31141502:
            case 31141501:
                return 31141500;
            case 31141001:
            case 31141000:
                return 惡魔衝擊;
            case HexaSKILL.強化惡魔覺醒:
                return 400011006;
            case HexaSKILL.強化耶夢加得:
                return 400011057;
            case HexaSKILL.強化奧爾特羅斯:
                return 400011077;
            case HexaSKILL.強化惡魔毀滅:
                return 400011110;
            case 惡魔狂斬1:
            case 惡魔狂斬2:
            case 惡魔狂斬3:
                return 惡魔狂斬;
            case 惡魔末日烈焰1:
                return 惡魔末日烈焰;
            case 惡魔覺醒_1:
            case 惡魔覺醒_2:
            case 惡魔覺醒_3:
            case 惡魔覺醒_4:
                return 惡魔覺醒;
            case 奧爾特羅斯_1:
                return 奧爾特羅斯;
            case 惡魔毀滅_1:
            case 惡魔毀滅_2:
                return 惡魔毀滅;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 惡魔推進器:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 惡魔追擊:
            case 死亡之握:
            case 黑暗拘束:
                monsterStatus.put(MonsterStatus.Seal, 1);
                return 1;
            case 黑暗復仇:
                statups.put(SecondaryStat.PowerGuard, effect.getInfo().get(MapleStatInfo.x));

                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 血腥烏鴉:
                effect.setHpR(effect.getX() / 100.0);
                return 1;
            case 惡魔衝擊:
                monsterStatus.put(MonsterStatus.Speed, effect.getX());
                return 1;
            case 魔力吶喊:
                statups.put(SecondaryStat.DevilCry, 1);
                effect.getInfo().put(MapleStatInfo.prop, 100);

                monsterStatus.put(MonsterStatus.Showdown, effect.getInfo().get(MapleStatInfo.w));
                monsterStatus.put(MonsterStatus.MDR, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.PDR, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.MAD, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.PAD, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.ACC, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 變形:
                statups.put(SecondaryStat.DamR, effect.getInfo().get(MapleStatInfo.damR));
                statups.put(SecondaryStat.DevilishPower, effect.getLevel());
                statups.put(SecondaryStat.IndieMHPR, effect.getInfo().get(MapleStatInfo.indieMhpR));
                return 1;
            case 無限力量:
                statups.put(SecondaryStat.InfinityForce, 1);
                return 1;
            case 吸血鬼之觸:
                statups.put(SecondaryStat.VampiricTouch, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 惡魔韌性:
                effect.setPartyBuff(true);
                return 1;
            case 高貴血統:
                effect.getInfo().put(MapleStatInfo.time, 2100000000); /* 值 2100000000 疑似永久開關技能 */
                statups.put(SecondaryStat.ShadowPartner, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 惡魔覺醒:
                statups.put(SecondaryStat.IndieCr, effect.getInfo().get(MapleStatInfo.indieCr));
                statups.put(SecondaryStat.IndiePMdR, effect.getInfo().get(MapleStatInfo.indiePMdR));
                return 1;
            case 耶夢加得:
                effect.getInfo().put(MapleStatInfo.attackCount, effect.getInfo().get(MapleStatInfo.attackCount) + 3);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 奧爾特羅斯) {
            chr.getSkillEffect(奧爾特羅斯_1).applyTo(chr, new Point(chr.getPosition().x + 100, chr.getPosition().y));
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 變形) {
            if (applier.passive) {
                return 0;
            }
            if (applyfrom.getSkillLevel(變形_無視反射) > 0) {
                applier.maskedDuration = applier.duration * 20 / 100;
                applier.maskedstatups.put(SecondaryStat.IndieIgnorePCounter, 1);
                applier.maskedstatups.put(SecondaryStat.IgnorePImmune, 1);
            }
            return 1;
        }
        return -1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.effect != null && applier.totalDamage > 0 && containsJob(player.getJobWithSub()) && applier.effect.getSourceId() == 地獄犬) {
            applier.mpHeal = 50;
        }

        final MapleSummon summonBySkillID2;
        if ((summonBySkillID2 = player.getSummonBySkillID(奧爾特羅斯)) != null && applier.totalDamage > 0L && player.getCheatTracker().canNextAllRocket(奧爾特羅斯, 3000)) {
            player.getClient().announce(SummonPacket.SummonedAssistAttackRequest(player.getId(), summonBySkillID2.getObjectId(), 0));
        }
        final MapleSummon summonBySkillID3;
        if ((summonBySkillID3 = player.getSummonBySkillID(奧爾特羅斯_1)) != null && applier.totalDamage > 0L && player.getCheatTracker().canNextAllRocket(奧爾特羅斯_1, 5000)) {
            player.getClient().announce(SummonPacket.SummonedAssistAttackRequest(player.getId(), summonBySkillID3.getObjectId(), 0));
        }
        return 1;
    }
}
