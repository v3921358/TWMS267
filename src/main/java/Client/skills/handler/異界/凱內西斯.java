package Client.skills.handler.異界;

import Client.MapleCharacter;
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
import Net.server.life.MapleMonster;
import Packet.MaplePacketCreator;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.凱內西斯.*;

public class 凱內西斯 extends AbstractSkillHandler {

    public 凱內西斯() {
        jobs = new MapleJob[]{
                MapleJob.凱內西斯,
                MapleJob.凱內西斯1轉,
                MapleJob.凱內西斯2轉,
                MapleJob.凱內西斯3轉,
                MapleJob.凱內西斯4轉
        };

        for (Field field : Config.constants.skills.凱內西斯.class.getDeclaredFields()) {
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
        int[] ss = {心靈攻擊, 回歸, 心靈本能, 英雄共鳴};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == 英雄共鳴) {
                continue;
            }
            skil = SkillFactory.getSkill(i);
            if (chr.getJob() >= i / 10000 && skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(i, new SkillEntry(skil.getMaxLevel(), skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 142141000:
                return 終極技_梅泰利爾;
            case HexaSKILL.強化心靈龍捲風:
                return 400021008;
            case HexaSKILL.強化終極技_移動物質:
                return 400021048;
            case HexaSKILL.強化終極技_心靈彈丸:
                return 400021074;
            case HexaSKILL.強化引力法則:
                return 400021096;
            case 猛烈心靈:
            case 猛烈心靈2:
                return 擷取心靈;
            case 猛烈心靈2_1:
            case 猛烈心靈2_最後一擊:
            case 終極技_心靈射擊:
                return 擷取心靈2;
            case 永恆壞滅_攻擊:
                return 永恆壞滅;
            case 心靈推手2_共享:
                return 心靈推手2;
            case 心靈推手3_共享:
                return 心靈推手3;
            case 瘋狂潰擊_1:
                return 瘋狂潰擊;
            case 心靈漫步_1:
                return 心靈漫步;
            case 心靈填充_1:
                return 心靈填充;
            case 心靈龍捲風_1:
            case 心靈龍捲風_2:
            case 心靈龍捲風_3:
            case 心靈龍捲風_4:
            case 心靈龍捲風_5:
            case 心靈龍捲風_6:
                return 心靈龍捲風;
            case 終極_移動物質_1:
                return 終極_移動物質;
            case 終極_心靈彈丸_1:
            case 終極_心靈彈丸_2:
                return 終極_心靈彈丸;
            case 引力法則_1:
            case 引力法則_2:
            case 引力法則_Area:
                return 引力法則;

        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 英雄共鳴:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
                statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
                return 1;
            case 心靈本能:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.KinesisPsychicEnergeShield, 1);
                return 1;
            case ESP加速器:
                statups.put(SecondaryStat.IndieBooster, effect.getInfo().get(MapleStatInfo.indieBooster));
                return 1;
            case 心靈力場:
            case 心靈力場2:
                monsterStatus.put(MonsterStatus.IndiePDR, -(Integer) effect.getInfo().get(MapleStatInfo.s));
                monsterStatus.put(MonsterStatus.IndieMDR, -(Integer) effect.getInfo().get(MapleStatInfo.s));
                monsterStatus.put(MonsterStatus.IndieSlow, -(Integer) effect.getInfo().get(MapleStatInfo.s));
                monsterStatus.put(MonsterStatus.PsychicGroundMark, effect.getInfo().get(MapleStatInfo.s));
                return 1;
            case 心靈推手:
            case 心靈推手2:
            case 心靈推手3:
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 心靈遊動:
                statups.put(SecondaryStat.NewFlying, 1);
                return 1;
            case 異界祝禱:
                effect.setPartyBuff(true); // 技能描述不與其他楓祝共享
                statups.put(SecondaryStat.IndieStatRBasic, 150); // maybe 15% 18min 百分比增加Stat
                return 1;
            case 心靈超越:
                statups.put(SecondaryStat.KinesisPsychicOver, 1);
                return 1;
            case 心靈龍捲風:
                statups.put(SecondaryStat.Kinesis_DustTornado, 3);
                return 1;
            case 心碎擷取:
                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 引力法則:
                statups.put(SecondaryStat.KinesisLawOfGravity, 2);
                return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 回歸: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 心靈本能: {
                if (applyto.getBuffedValue(SecondaryStat.KinesisPsychicEnergeShield) != null) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                }
                return 1;
            }
            case 心靈遊動: {
                applier.duration += 500;
                return 1;
            }
            case 心靈填充: {
                applyto.handlePPCount(Math.max((30 - applyto.getSpecialStat().getPP()) / 2, 1));
                return 1;
            }
            case 猛烈心靈2_1: {
                final MapleStatEffect eff;
                if ((eff = applyto.getSkillEffect(擷取心靈_鋼鐵肌膚)) != null) {
                    eff.applyTo(applyto);
                }
                return 1;
            }
            case 心靈突破: {
                int add = Math.max(1, applyto.getSpecialStat().getMindBreakCount()) * applier.effect.getIndiePMdR();
                applier.localstatups.put(SecondaryStat.IndiePMdR, add);
                if (applyto.getSkillEffect(心靈突破_強化效果) != null) {
                    applier.localstatups.put(SecondaryStat.IndiePMdR, add * 2);
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applyfrom.hasBuffSkill(心靈突破)) {
            applyfrom.getSpecialStat().setMindBreakCount(Math.min(applyfrom.getSkillEffect(心靈突破).getW(), applyfrom.getSpecialStat().getMindBreakCount() + (applyto.isBoss() ? 5 : 1)));
            applyfrom.getSkillEffect(心靈突破).applyTo(applyfrom, null, true);
        }
        if (applier.effect != null && applyto != null && applyfrom != null) {
            switch (applier.effect.getSourceId()) {
                case 引力法則:
                    applyfrom.getSkillEffect(引力法則_Area).applyAffectedArea(applyfrom, applyfrom.getPosition());
                    break;
                case 引力法則_2:
                    applyfrom.getMap().broadcastMessage(MaplePacketCreator.objSkillEffect(applyto.getObjectId(), 引力法則_2, applyfrom.getId(), applyto.getPosition()));
                    break;
            }
        }
        return 1;
    }
}
