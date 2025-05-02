package Client.skills.handler.江湖;

import Client.*;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.墨玄.*;
public class 墨玄 extends AbstractSkillHandler {

    public 墨玄() {
        jobs = new MapleJob[]{
                MapleJob.墨玄,
                MapleJob.墨玄1轉,
                MapleJob.墨玄2轉,
                MapleJob.墨玄3轉,
                MapleJob.墨玄4轉
        };

        for (Field field : Config.constants.skills.墨玄.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int baseSkills(MapleCharacter chr, SkillClassApplier applier) {
        int level = chr.getLevel();
        Skill skil;
        final int[] ss = {玄山氣勢, 玄山回歸, 獨門咒語};
        for (int i : ss) {
            if (i == 獨門咒語 && level < 200) {
                continue;
            }
            int skillLevel = i == 玄山氣勢 ? Math.min(10, level / 20 + 1) : 1;
            skil = SkillFactory.getSkill(i);
            if (skil != null && chr.getSkillLevel(skil) < skillLevel) {
                applier.skillMap.put(i, new SkillEntry(skillLevel, (byte) 1, -1));
            }
        }
        if (chr.getJob() >= MapleJob.墨玄1轉.getId()) {
            int[] fixskills = {墨玄_二轉_神功_粉碎腳, 墨玄_一轉_覺醒};
            for (int f : fixskills) {
                skil = SkillFactory.getSkill(f);
                if (skil != null && chr.getSkillLevel(skil) <= 0 && chr.getMasterLevel(skil) <= 0) {
                    applier.skillMap.put(f, new SkillEntry((byte) 0, (byte) (skil.getMasterLevel() == 0 ? skil.getMaxLevel() : skil.getMasterLevel()), SkillFactory.getDefaultSExpiry(skil)));
                }
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 175000001: //玄山 - 第一式 天
            case 175001002: //玄山 - 第一式 天
                return 175001000; // 玄山 - 招式展開[天]
            case 175121002:
                return 175101001;
            case 墨玄_二轉_玄山_招式展開_第一式_地_NEXT: {
                return 墨玄_二轉_玄山_招式展開_第一式_地;
            }
            case 墨玄_三轉_神功_鐵衫功: {
                return 玄山_第3式;
            }

            case 175121004:
            case 墨玄_四轉_神功_亂打連拳:
                return 175121003;

            case 墨玄_一轉_密技_玄山微步_NEXT: {
                return 墨玄_一轉_密技_玄山微步;
            }
            case 墨玄_二轉_神功_粉碎腳_1:
            case 墨玄_二轉_神功_粉碎腳_2: {
                return 墨玄_二轉_神功_粉碎腳;
            }
            case 墨玄_三轉_神功_旋風腳_NEXT: {
                return 墨玄_三轉_神功_旋風腳;
            }
            case 神功_大地崩塌_1:
            case 神功_大地崩塌_2: {
                return 墨玄_四轉_密技_無影腳;
            }
            case 神功_移形換位_1:
            case 神功_移形換位_2: {
                return 神功_移形換位;
            }
            case 絕技_神玄武極_1: {
                return 絕技_神玄武極;
            }
            case 絕技_無我之境_1: {
                return 絕技_無我之境;
            }
            case 神功_破空拳_1:
            case 神功_破空拳_2:
            case 神功_破空拳神力的氣息: {
                return 神功_破空拳;
            }
        }
        return -1;
    }


    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 獨門咒語:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, effect.getDuration() * 1000);
                statups.put(SecondaryStat.MaxLevelBuff, effect.getX());
                return 1;
            case 墨玄_二轉_密技_弓身彈影:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.CannonShooter_BFCannonBall, 0);
                return 1;
            case 墨玄_二轉_密技_狂暴化:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 墨玄_超技能_密技_命運的氣息:
                effect.getInfo().put(MapleStatInfo.time, 60000);
                statups.put(SecondaryStat.MukHyun_IM_GI_EUNG_BYEON, 1);
                statups.put(SecondaryStat.IndieDamR, 10);
                return 1;
            case 墨玄_四轉_密技_護身強氣:
                statups.put(SecondaryStat.MukHyun_HO_SIN_GANG_GI, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 墨玄_二轉_神功_粉碎腳_1:
                effect.getInfo().put(MapleStatInfo.time, 5000);
                statups.put(SecondaryStat.MukHyunRepeat, 1);
                return 1;
            case 墨玄_三轉_密技_移形換位:
                effect.getInfo().put(MapleStatInfo.time, effect.getInfo().get(MapleStatInfo.z));
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
            case 神功_移形換位_1:
                statups.put(SecondaryStat.IndiePeriodicalSkillActivation, 47);
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 墨玄_超技能_絕技_黑風腳:
                effect.getInfo().put(MapleStatInfo.time, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
            case 絕技_無我之境:
                statups.put(SecondaryStat.IndiePeriodicalSkillActivation, 20);
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                return 1;
            case 絕技_暴技:
                statups.put(SecondaryStat.IndieCr, effect.getInfo().get(MapleStatInfo.indieCr));
                statups.put(SecondaryStat.IndieCD, effect.getInfo().get(MapleStatInfo.indieCD));
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                statups.put(SecondaryStat.IndieCheckTimeByClient, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 墨玄_一轉_玄山_招式展開_第一式_天_NEXT: {
                c.ctx((short) 559, "3F 7D 24 B3 23 00 00 6E 00 00 00 B8 0B 00 00 01 00 00 00 01 23 07 00 00 00 A0");
                return 1;
            }
            case 175121001: {
                c.ctx((short) 559, "3F 7D 24 B3 23 00 00 00 00 00 00 88 FF FF FF 00 00 00 00 01 23 07 00 00 00 85");
                return 1;
            }
            case 神功_移形換位: {
                int godPower = 0;
                int time = 30000;
                if (chr.getSkillLevel(墨玄_三轉_入神) > 0) {
                    godPower = (int) chr.getTempValues().getOrDefault("GodPower", 0);
                    godPower = Math.min(5, ++godPower);
                    chr.getTempValues().put("GodPower", godPower);
                    if (chr.getSkillLevel(墨玄_超技能_神力_時間持續) > 0) {
                        time += 10000;
                    }
                }
                //c.write(OverseasPacket.extraSystemResult(ExtraSystemResult.mukhyunPower(0, 0, 3000, 1, 0)));
                //c.write(OverseasPacket.extraSystemResult(ExtraSystemResult.mukhyunPower(1, godPower, time, 0, 0)));
                return 1;
            }
            case 墨玄_超技能_密技_神力渦流:
            case 絕技_神玄武極: {
                int godPower = 0;
                int time = 30000;
                if (chr.getSkillLevel(墨玄_三轉_入神) > 0) {
                    godPower = applier.effect.getSourceId() == 墨玄_超技能_密技_神力渦流 ? 5 : 0;
                    chr.getTempValues().put("GodPower", godPower);
                    if (chr.getSkillLevel(墨玄_超技能_神力_時間持續) > 0) {
                        time += 10000;
                    }
                }
                //c.write(OverseasPacket.extraSystemResult(ExtraSystemResult.mukhyunPower(1, godPower, time, 0, 0)));
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 玄山回歸: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 墨玄_二轉_密技_弓身彈影: {
                final int value = applyto.getBuffedIntValue(SecondaryStat.CannonShooter_BFCannonBall) + (applier.passive ? 1 : -1);
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.CannonShooter_BFCannonBall);
                if (!applier.primary || value < 0 || mbsvh != null && System.currentTimeMillis() < mbsvh.startTime + applier.effect.getT() * 1000L && applier.passive || value > applier.effect.getY()) {
                    return 0;
                }
                applier.duration = 2100000000;
                applier.localstatups.put(SecondaryStat.CannonShooter_BFCannonBall, value);
                return 1;
            }
            case 墨玄_二轉_神功_粉碎腳: {
                final MapleStatEffect skillEffect = applyto.getSkillEffect(墨玄_二轉_神功_粉碎腳_1);
                if (skillEffect != null) {
                    skillEffect.applyBuffEffect(applyto, skillEffect.getBuffDuration(applyto), true);
                }
                return 1;
            }
            case 墨玄_二轉_神功_粉碎腳_1: {
                if (applyto.hasBuffSkill(墨玄_二轉_神功_粉碎腳_1)) {
                    applyto.dispelEffect(墨玄_二轉_神功_粉碎腳_1);
                    return 0;
                }
                return 1;
            }
            case 墨玄_三轉_密技_移形換位: {
                //applyto.write(OverseasPacket.extraSystemResult(ExtraSystemResult.mukhyunSkill_175111004(applier.effect.getSourceId(), 0)));
                return 1;
            }
            case 神功_移形換位: {
                if (!applyto.hasBuffSkill(絕技_無我之境)) {
                    final MapleStatEffect skillEffect = applyto.getSkillEffect(神功_移形換位_1);
                    if (skillEffect != null) {
                        skillEffect.applyBuffEffect(applyto, skillEffect.getBuffDuration(applyto), true);
                    }
                }
                return 1;
            }
            case 絕技_無我之境: {
                if (applyto.hasBuffSkill(神功_移形換位_1)) {
                    applyto.dispelEffect(神功_移形換位_1);
                }
                return 1;
            }
            case 絕技_暴技: {
                applier.startChargeTime = 1;
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onAfterCancelEffect(MapleCharacter player, SkillClassApplier applier) {
        if (!applier.overwrite) {
            if (applier.effect.getSourceId() == 絕技_無我之境) {
                MapleStatEffect effect = player.getSkillEffect(神功_移形換位);
                if (effect != null) {
                    effect.applyTo(player, true);
                }
            }
        }
        return -1;
    }
}
