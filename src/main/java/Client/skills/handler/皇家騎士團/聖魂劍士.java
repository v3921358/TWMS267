package Client.skills.handler.皇家騎士團;

import Client.MapleCharacter;
import Client.MapleJob;
import Client.SecondaryStat;
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
import Config.constants.skills.皇家騎士團_技能群組.閃雷悍將;
import Config.constants.skills.貴族;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.maps.MapleSummon;
import Packet.MaplePacketCreator;
import Packet.SummonPacket;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static Client.skills.handler.HexaSKILL.銀河星爆;
import static Config.constants.skills.皇家騎士團_技能群組.聖魂劍士.*;

public class 聖魂劍士 extends AbstractSkillHandler {

    private MapleCharacter chr;

    public MapleCharacter getChr() {
        return chr;
    }

    public 聖魂劍士() {
        jobs = new MapleJob[]{
                MapleJob.聖魂劍士1轉,
                MapleJob.聖魂劍士2轉,
                MapleJob.聖魂劍士3轉,
                MapleJob.聖魂劍士4轉
        };

        for (Field field : Config.constants.skills.皇家騎士團_技能群組.聖魂劍士.class.getDeclaredFields()) {
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
            case 500004060:
                return 400011142;
            case 500004061:
                return 400011055;
            case 500004062:
                return 400011088;
            case 500004063:
                return 400011048;
            case 11141502:
            case 11141503:
                return 銀河星爆;

            case SUMMON_元素靈魂I:
            case SUMMON_元素靈魂II:
                return SUMMON_元素靈魂;

            case 靈魂躍進_JUMP_UP:
                return 靈魂躍進_JUMP;

            case 宇宙物質:
            case 宇宙物質_I:
                return 宇宙轟炸;

            case 天體觀測:
            case 天體觀測II:
            case 天體觀測III:
                return 雙重力量;

            case ATTACK_TYPE_黃泉十字架:
            case ATTACK_極樂之境_II:
                return 極樂之境;
            case 月光之舞_空中old:
                return 光輝衝刺;
            case 雙重狂斬:
                return 雙重狂斬;
            case 靈魂穿透_I:
                return 靈魂穿透;
            case 宇宙融合_爆炸:
                return 宇宙融合;
            case 日月星爆_1:
            case 日月星爆_2:
                return 日月星爆;
            case ATTACK_日月分裂:
                return ATTACK_靈魂蝕日;
            case 閃焰重擊_1:
                return 閃焰重擊;

            case 11001126:
            case 11100128:
            case 11110128:
            case 11111130:
            case 11120117:
            case 11141100:
                return 沉月;

            case 11001226:
            case 11100228:
            case 11110228:
            case 11111230:
            case 11120217:
            case 11141200:
                return 旭日;

            case 沉月:
                return 旭日;

            case 靈魂祝福I:
            case 靈魂祝福II:
            case 靈魂祝福III:
                return SUMMON_元素靈魂;



        }
        return -1;
    }


    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            // new --------------
            case SUMMON_元素靈魂:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.ElementSoul, effect.getLevel());
                statups.put(SecondaryStat.CosmicForge, 1);
                return 1;
            case 靈魂祝福I:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 2100000000); /* 值 2100000000 疑似永久開關技能 */
                effect.getInfo().put(MapleStatInfo.pad, 7);
                statups.put(SecondaryStat.CosmicForge, 1);
                return 1;
            case 靈魂祝福II:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 2100000000); /* 值 2100000000 疑似永久開關技能 */
                effect.getInfo().put(MapleStatInfo.pad, 6);
                statups.put(SecondaryStat.CosmicForge, 1);
                return 1;
            case 靈魂祝福III:
                effect.setRangeBuff(true);
                effect.getInfo().put(MapleStatInfo.time, 2100000000); /* 值 2100000000 疑似永久開關技能 */
                effect.getInfo().put(MapleStatInfo.pad, 5);
                statups.put(SecondaryStat.CosmicForge, 1);
                return 1;
            case 沉月:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.PoseType, 1); //1 = 月光 2 = 旭日
                return 1;
            case 旭日:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.PoseType, 2); //1 = 月光 2 = 旭日
                return 1;
            case 雙重力量:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                //statups.put(SecondaryStat.GlimmeringTime, 1);
                return 1;
            case 雙重力量_沉月:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.PoseType, 1);
                return 1;
            case 雙重力量_旭日:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.PoseType, 2);
                return 1;
            // old ----------------
            case 宇宙融合:
                statups.put(SecondaryStat.ACCR, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.IndiePAD, effect.getInfo().get(MapleStatInfo.indiePad));
                statups.put(SecondaryStat.IndieScriptBuff, effect.getInfo().get(MapleStatInfo.indieMaxDamageOver));
                return 1;
            case 大師之魂_沉月:
                statups.put(SecondaryStat.BuckShot, effect.getLevel());
                statups.put(SecondaryStat.IndieCr, effect.getInfo().get(MapleStatInfo.indieCr));
                return 1;
            case 大師之魂_旭日:
                statups.put(SecondaryStat.IndieBooster, effect.getInfo().get(MapleStatInfo.indieBooster));
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 真實之眼:
                effect.getInfo().put(MapleStatInfo.time, 30000);
                monsterStatus.put(MonsterStatus.PDR, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.MDR, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.TrueSight, 0);
                return 1;
            case 光速反應:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 西格諾斯騎士:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 守護者的榮耀:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 日月星爆:
                statups.put(SecondaryStat.TempSecondaryStat, 1);
                return 1;
            case 極樂之境:
                statups.put(SecondaryStat.Elysion, effect.getLevel());
                return 1;

        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 旭日: {
                applyto.dispelEffect(沉月);
                MapleStatEffect eff = applyto.getSkillEffect(雙重力量_旭日);
                if (eff != null) {
                    applier.localstatups.putAll(eff.getStatups());
                }
                return 1;
            }
            case 沉月: {
                applyto.dispelEffect(旭日);
                MapleStatEffect eff = applyto.getSkillEffect(雙重力量_沉月);
                if (eff != null) {
                    applier.localstatups.putAll(eff.getStatups());
                }
                return 1;
            }
            case 雙重力量: {
                int value = applyto.getBuffedIntValue(SecondaryStat.PoseType);
                if (value <= 0) {
                    return 0;
                } else {
                    MapleStatEffect eff = applyto.getSkillEffect(value == 1 ? 雙重力量_沉月 : 雙重力量_旭日);
                    if (eff != null) {
                        eff.applyTo(applyto);
                    }
                }
                return 1;
            }
            case 雙重狂斬: {
                applier.b4 = false;
                return 1;
            }
            case 守護者的榮耀: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000 || applyto.getJob() / 1000 == 5) {
                    return 0;
                }
                applyto.dispelEffect(Config.constants.skills.皇家騎士團_技能群組.聖魂劍士.守護者的榮耀);
                applyto.dispelEffect(烈焰巫師.守護者的榮耀);
                applyto.dispelEffect(破風使者.守護者的榮耀);
                applyto.dispelEffect(暗夜行者.守護者的榮耀);
                applyto.dispelEffect(閃雷悍將.守護者的榮耀);
                applyto.dispelEffect(米哈逸.明日女皇);
                return 1;
            }
            case 極樂之境: {
                if (applier.passive) {
                    return 0;
                }
                applyto.reduceSkillCooldown(宇宙融合_爆炸, 9999);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (player.getCheatTracker().canNextBonusAttack(5000) && player.getBuffStatValueHolder(SecondaryStat.TempSecondaryStat, 日月星爆) != null) {
            if (player.getBuffedIntValue(SecondaryStat.PoseType) == 1) {
                player.getClient().announce(MaplePacketCreator.userBonusAttackRequest(日月星爆_1, 0, Collections.emptyList()));
            } else {
                player.getClient().announce(MaplePacketCreator.userBonusAttackRequest(日月星爆_2, 0, Collections.emptyList()));
            }
        }
        if (player.getBuffStatValueHolder(雙重力量) != null){
            if(player.getBuffStatValueHolder(沉月) != null) {
                player.dispelEffect(沉月);
                player.getSkillEffect(旭日).applyTo(player);
            } else {
                player.dispelEffect(旭日);
                player.getSkillEffect(沉月).applyTo(player);
            }
        }
        if (applier.effect != null && applier.effect.getSourceId() == ATTACK_TYPE_黃泉十字架) {
            final MapleSummon summonBySkillID;
            if ((summonBySkillID = player.getSummonBySkillID(ATTACK_極樂之境_II)) != null) {
                final Rectangle a = applier.effect.calculateBoundingBox(player.getPosition(), player.isFacingLeft(), 500);
                if (a.contains(summonBySkillID.getPosition())) {
                    summonBySkillID.setAcState1(summonBySkillID.getAcState1() + 1);
                    player.getMap().broadcastMessage(player, SummonPacket.SummonedSkillState(summonBySkillID, 1), true);
                }
                player.getSummonsOIDsBySkillID(ATTACK_極樂之境_II).size();
                return 1;
            }
            player.getSkillEffect(ATTACK_極樂之境_II).applyTo(player, new Point(player.getPosition().x + (player.isFacingLeft() ? -100 : 100), player.getPosition().y));
        }
        return 1;
    }
}
