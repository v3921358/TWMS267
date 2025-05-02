package Client.skills.handler.皇家騎士團;

import Client.*;
import Client.force.MapleForceFactory;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.HexaSKILL;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Config.constants.skills.皇家騎士團_技能群組.破風使者;
import Config.constants.skills.皇家騎士團_技能群組.米哈逸;
import Config.constants.skills.皇家騎士團_技能群組.聖魂劍士;
import Config.constants.skills.皇家騎士團_技能群組.閃雷悍將;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Packet.MaplePacketCreator;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Config.constants.skills.皇家騎士團_技能群組.烈焰巫師.*;

public class 烈焰巫師 extends AbstractSkillHandler {

    public 烈焰巫師() {
        jobs = new MapleJob[]{
                MapleJob.烈焰巫師1轉,
                MapleJob.烈焰巫師2轉,
                MapleJob.烈焰巫師3轉,
                MapleJob.烈焰巫師4轉
        };

        for (Field field : Config.constants.skills.皇家騎士團_技能群組.烈焰巫師.class.getDeclaredFields()) {
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
        Skill skill = SkillFactory.getSkill(Royal_Link_烈焰巫師_自然旋律);
        if (skill != null && chr.getSkillLevel(skill) <= 0) {
            applier.skillMap.put(skill.getId(), new SkillEntry(1, skill.getMaxMasterLevel(), -1));
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 12141501:
            case 12141502:
                return 12141500;
            case 12141000:
            case 12141001:
            case 12141002:
            case 12141003:
            case 12141004:
            case 12141005:
            case 12141006:
                return SUMMON_元素火焰IV;
            case HexaSKILL.強化炙熱元素之炎:
                return 400021004;
            case HexaSKILL.強化烈炎爆發:
                return 400021042;
            case HexaSKILL.強化無盡之炎燄:
                return 400021072;
            case HexaSKILL.強化火蜥蜴的惡作劇:
                return 400021092;
            case Return_元素之炎I_1:
                return 元素之炎;
            case Return_元素之炎II_1:
                return 元素之炎II;
            case Return_元素之炎III_1:
                return 元素之炎III;
            case Return_元素之炎IV_1:
            case Return_元素之炎IV:
                return 元素之炎IV;
            case 極致熾烈_1:
                return 極致熾烈;
            case 燃燒軍團_1:
                return 燃燒軍團;
            case 火焰防護_1:
                return 火焰防護;
            case 火焰之魂_獅子:
            case 火焰之魂_狐狸:
                return 火焰之魂;
            case 烈炎爆發_1:
            case 烈炎爆發_2:
            case 烈炎爆發_3:
                return 烈炎爆發;
            case 本鳳凰_無敵狀態:
                return 本鳳凰;
            case 龍氣息_最後一擊:
                return 鳳凰爆裂;
            case 燃燒_1:
                return 燃燒;

            case 火蜥蜴的惡作劇_1:
                return 火蜥蜴的惡作劇;

            case 火步行_JUMP_BEGIN:
            case 火步行_JUMP_STAGE_2:
                return 火步行_JUMP;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 大災變:
                statups.put(SecondaryStat.NotDamaged, 1);
                effect.getInfo().put(MapleStatInfo.time, 5 * 1000);
                return 1;
            case 火焰之魂:
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1); // 檢查屬性
                statups.put(SecondaryStat.IndiePDDR, effect.getInfo().get(MapleStatInfo.y)); //給予無視
                return 1;
            case Switch_type_Magic_魔法爆發:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.AttackCountX, 3);
                return 1;
            case 火球連結:
                effect.getInfo().put(MapleStatInfo.time, 100000);
                statups.put(SecondaryStat.ReturnTeleport, 1);
                return 1;
            case 焚燒:
                effect.setMpR(effect.getInfo().get(MapleStatInfo.x) / 100.0); // 恢復MP 比例
                return 1;
            case SUMMON_元素火焰:
            case SUMMON_元素火焰II:
            case SUMMON_元素火焰III:
            case SUMMON_元素火焰IV:
                statups.put(SecondaryStat.IndieMAD, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 燃燒軍團:
                statups.put(SecondaryStat.IndieBooster, effect.getInfo().get(MapleStatInfo.indieBooster));
                return 1;
            case 本鳳凰:
                statups.put(SecondaryStat.SiphonVitality, 1);
                return 1;
            case 本鳳凰_無敵狀態:
                effect.getInfo().put(MapleStatInfo.time, 3 * 2100000000);
                statups.put(SecondaryStat.NotDamaged, 1);
                return 1;
            case 火焰防護:
                statups.put(SecondaryStat.DamageReduce, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 火焰之魂_獅子:
            case 火焰之魂_狐狸:
                statups.put(SecondaryStat.IgnoreTargetDEF, 1);
                return 1;
            case 火之書:
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
            case 鳳凰爆裂:
                effect.getInfo().put(MapleStatInfo.time, 15000);
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1); // 檢查屬性
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 無盡之炎燄:
                effect.getInfo().put(MapleStatInfo.time, 3 * 2100000000);
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1); // 檢查屬性
                statups.put(SecondaryStat.IndieHitDamR, 8); // 檢查屬性
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        switch (applier.effect.getSourceId()) {
            case 火球連結: {
                Point p = slea.readPos(); // before
                slea.readPos();
                slea.skip(1);
                boolean b = slea.readByte() > 0;
                if (b) {
                    slea.skip(11);
                    Point old = slea.readPos(); // before
                    chr.setPosition(p);
                    chr.getMap().objectMove(-1, chr, null);
                    chr.removeSummon(火球連結);
                    c.announce(MaplePacketCreator.userTeleport(false, 2, chr.getId(), old));
                }
                return 1;
            }
            case 漩渦: { // TODO 重構MapleSummon
                applier.pos = slea.readPos();
                slea.readByte();
                slea.readShort();
                final MapleMonster mobObject;
                if ((mobObject = chr.getMap().getMonsterByOid(slea.readInt())) == null) {
                    c.announce(MaplePacketCreator.sendSkillUseResult(false, 0));
                    return 0;
                }
                chr.getSpecialStat().setMaelstromMoboid(mobObject.getId());
                return 1;
            }
            case 火焰之魂_獅子:
            case 火焰之魂_狐狸: {
                chr.getSkillEffect(火焰之魂).applyTo(chr);
                return 1;
            }
            case 烈炎爆發_2: {
                List<Integer> oids = IntStream.range(0, slea.readByte()).mapToObj(i -> slea.readInt()).collect(Collectors.toList());
                forceFactory.getMapleForce(chr, applier.effect, 0, oids);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 燃燒: {
                applier.buffz = applyto.getBuffedIntZ(SecondaryStat.FlameDischarge);
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.FlameDischarge);
                if (applier.passive && mbsvh != null) {
                    if (applier.primary) {
                        applier.buffz = Math.max(0, applier.buffz - 2);
                    } else {
                        applier.buffz = Math.min(6, applier.buffz + 1);
                    }
                    applier.duration = mbsvh.getLeftTime();
                }
                return 1;
            }
            case 火焰之魂_獅子: {
                applier.localstatups.put(SecondaryStat.IgnoreTargetDEF, applyto.getTotalSkillLevel(火焰之魂));
                applyto.dispelEffect(火焰之魂_狐狸);
                return 1;
            }
            case 火焰之魂_狐狸: {
                applier.localstatups.put(SecondaryStat.IgnoreTargetDEF, applyto.getTotalSkillLevel(火焰之魂));
                applyto.dispelEffect(火焰之魂_獅子);
                return 1;
            }
            case 守護者的榮耀: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000 || applyto.getJob() / 1000 == 5) {
                    return 0;
                }
                applyto.dispelEffect(聖魂劍士.守護者的榮耀);
                applyto.dispelEffect(Config.constants.skills.皇家騎士團_技能群組.烈焰巫師.守護者的榮耀);
                applyto.dispelEffect(破風使者.守護者的榮耀);
                applyto.dispelEffect(暗夜行者.守護者的榮耀);
                applyto.dispelEffect(閃雷悍將.守護者的榮耀);
                applyto.dispelEffect(米哈逸.明日女皇);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        final MapleStatEffect effecForBuffStat4 = applyfrom.getEffectForBuffStat(SecondaryStat.FlameDischarge);
        if (applier.totalDamage > 0L && effecForBuffStat4 != null && applyto != null && applyto.isAlive()) {
            effecForBuffStat4.applyMonsterEffect(applyfrom, applyto, effecForBuffStat4.getDotTime(applyfrom) * 1000);
            if (applyfrom.getSkillEffect(烈炎爆發) != null) {
                effecForBuffStat4.unprimaryPassiveApplyTo(applyfrom);
            }
        }
        return 1;
    }
}
