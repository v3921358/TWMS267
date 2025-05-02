package Client.skills.handler.末日反抗軍;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.skills.ExtraSkill;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Packet.MaplePacketCreator;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.*;

import static Config.constants.skills.爆拳槍神.*;

public class 爆拳槍神 extends AbstractSkillHandler {

    public 爆拳槍神() {
        jobs = new MapleJob[]{
                MapleJob.爆拳槍神1轉,
                MapleJob.爆拳槍神2轉,
                MapleJob.爆拳槍神3轉,
                MapleJob.爆拳槍神4轉
        };

        for (Field field : Config.constants.skills.爆拳槍神.class.getDeclaredFields()) {
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
        Skill skil;
        int[] ss = {旋轉加農砲, 彈丸填裝};
        for (int i : ss) {
            skil = SkillFactory.getSkill(i);
            if (chr.getLevel() >= i / 10000 && skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(i, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 神聖連發重擊_1:
            case 神聖連發重擊_2:
            case 神聖連發重擊_3:
            case 神聖連發重擊_4:
            case 神聖連發重擊_5:
                return 神聖連發重擊;
            case 釋能衝擊椎_1:
            case 釋能衝擊椎_2:
            case 釋能衝擊椎_3:
            case 釋能衝擊椎_4:
                return 釋能衝擊椎;
            case 王之子_1:
                return 王之子;
            case 擺動_1:
                return 擺動;
            case 錘之碎擊_1:
            case 錘之碎擊_2:
                return 錘之碎擊;
            case 旋轉衝擊椎_1:
            case 旋轉衝擊椎_2:
            case 旋轉衝擊椎_3:
            case 旋轉衝擊椎_4:
            case 旋轉衝擊椎_5:
            case 旋轉衝擊椎_6:
            case 旋轉衝擊椎_7:
                return 旋轉衝擊椎;
            case 颶風混裂_1:
            case 颶風混裂_2:
                return 颶風混裂;
            case 旋轉加農砲_雙重棒:
                return 雙重棒;
            case 旋轉加農砲_連發重擊:
                return 連發重擊;
            case 雙重壓迫_1:
                return 雙重壓迫;
            case 末日飄移_爆炸:
                return 末日飄移;
            case 衝擊波動_1:
                return 衝擊波動;
            case 彈丸填裝:
                return 旋轉加農砲;
            case 釋能衝擊椎_7:
                return 強化旋轉加農砲III;
            case 釋能衝擊椎_6:
                return 強化旋轉加農砲II;
            case 釋能衝擊椎_5:
                return 強化旋轉加農砲;
            case 衝擊椎破壞者:
                return 毀滅左輪;
            case 火神重擊_1:
                return 火神重擊;
            case 燃燒破壞者_1:
            case 燃燒破壞者_2:
            case 燃燒破壞者_3:
            case 燃燒破壞者_4:
            case 燃燒破壞者_5:
            case 燃燒破壞者_6:
            case 燃燒破壞者_7:
                return 燃燒破壞者;
            case 殘影衝擊_1:
            case 殘影衝擊_2:
                return 殘影衝擊;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 釋能衝擊椎_2:
            case 釋能衝擊椎_3:
            case 釋能衝擊椎_4:
                effect.getInfo().put(MapleStatInfo.time, 7000);
                statups.put(SecondaryStat.RWOverHeat, 1);
                return 1;
            case 續航防盾:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.RWBarrier, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 旋轉加農砲_連發重擊:
            case 末日飄移:
            case 旋轉加農砲_雙重棒:
            case 旋轉衝擊椎_1:
                return 1;
            case 彈丸填裝:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.RWCylinder, 1);
                return 1;
            case 王之子_1:
            case 擺動_1:
            case 擺動:
            case 王之子:
                statups.put(SecondaryStat.RWMovingEvar, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 組合訓練:
            case 組合訓練II:
                statups.put(SecondaryStat.RWCombination, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 神聖連發重擊:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.RWMagnumBlow, 1);
                return 1;
            case 極大加農:
                statups.put(SecondaryStat.RWMaximizeCannon, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 旋轉衝擊椎:
                effect.getInfo().put(MapleStatInfo.time, 3500);
                statups.put(SecondaryStat.NotDamaged, 1);

                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 旋轉加農砲:
                effect.getInfo().put(MapleStatInfo.time, -1);
                statups.put(SecondaryStat.RWCylinder, 1);
                return 1;
            case 護腕加速器:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 錘之碎擊:
            case 錘之碎擊_1:
            case 錘之碎擊_2:
                monsterStatus.put(MonsterStatus.AddDamSkill, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.RWChoppingHammer, effect.getInfo().get(MapleStatInfo.y));
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 自由意志:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 毀滅左輪:
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 旋轉加農砲:
            case 王之子:
            case 錘之碎擊:
            case 擺動: {
                return 0;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 續航防盾: {
                int value = applyto.getBuffedIntValue(SecondaryStat.RWBarrier);
                if (applyto.getBuffedValue(SecondaryStat.RWBarrier) != null) {
                    if (!applier.primary) {
                        value -= value * applier.effect.getY() / 100 + applier.effect.getZ();
                    }
                } else {
                    final int shield = applyto.getHurtHP() * applier.effect.getX() / 10;
                    value = Math.max(0, shield < value * applier.effect.getY() / 100 + applier.effect.getZ() ? 0 : shield);
                }
                applier.localstatups.put(SecondaryStat.RWBarrier, Math.max(0, value));
                if (value <= 0) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                }
                return 1;
            }
            case 王之子_1:
            case 王之子:
            case 擺動_1:
            case 擺動: {
                if (!applier.primary) {
                    applier.duration = 1500;
                }
                return 1;
            }
            case 釋能衝擊椎_2:
            case 釋能衝擊椎_3:
            case 釋能衝擊椎_4:
            case 釋能衝擊椎: {
                applyto.dispelEffect(SecondaryStat.RWOverHeat);
                applyto.handleCylinder(-8);
                if (applyto.getBuffedIntValue(SecondaryStat.RWMaximizeCannon) > 0) {
                    applier.duration = 1000;
                }
                return 1;
            }
            case 彈丸填裝: {
                if (!applier.passive) {
                    applyto.handleAmmoClip(8);
                }
                return 1;
            }
            case 神聖連發重擊: {
                if (!applier.primary) {
                    return 0;
                }
                if (!applier.passive) {
                    return 1;
                }
                final int value = applyto.getBuffedIntValue(SecondaryStat.RWMagnumBlow);
                if (value < applier.effect.getSubTime()) {
                    applier.localstatups.put(SecondaryStat.RWMagnumBlow, Math.min(value + 1, applier.effect.getSubTime()));
                    return 1;
                }
                return 0;
            }
            case 組合訓練: {
                final int n34 = applyto.getBuffedIntValue(SecondaryStat.RWCombination) + 1;
                applier.localstatups.put(SecondaryStat.CombatFrenzy, Math.min(applier.effect.getX(), n34));
                if (n34 >= 6) {
                    applier.localstatups.put(SecondaryStat.IndieBooster, 1);
                }
                return 1;
            }
            case 組合訓練II: {
                final int n35 = applyto.getBuffedIntValue(SecondaryStat.RWCombination) + 1;
                applier.localstatups.put(SecondaryStat.RWCombination, Math.min(applier.effect.getX(), n35));
                applier.localstatups.put(SecondaryStat.IndieCr, Math.min(applier.effect.getQ() * n35, applier.effect.getQ() * applier.effect.getX()));
                if (n35 >= 6) {
                    applier.localstatups.put(SecondaryStat.IndieBooster, 1);
                }
                return 1;
            }
            case 自由意志: {
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
        }
        return -1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.effect != null) {
            switch (applier.effect.getSourceId()) {
                case 釋能衝擊椎_2:
                case 釋能衝擊椎_3:
                case 釋能衝擊椎_4:
                case 釋能衝擊椎: {
                    List<Integer> exList = null;
                    if (applier.effect.getSourceId() == 釋能衝擊椎_4) {
                        exList = Arrays.asList(釋能衝擊椎_1, 旋轉加農砲_連發重擊, 釋能衝擊椎_5, 釋能衝擊椎_6, 釋能衝擊椎_7);
                    } else if (applier.effect.getSourceId() == 釋能衝擊椎_3) {
                        exList = Arrays.asList(釋能衝擊椎_1, 旋轉加農砲_連發重擊, 釋能衝擊椎_5, 釋能衝擊椎_6);
                    } else if (applier.effect.getSourceId() == 釋能衝擊椎_2) {
                        exList = Arrays.asList(釋能衝擊椎_1, 旋轉加農砲_連發重擊, 釋能衝擊椎_5);
                    } else {
                        exList = Arrays.asList(釋能衝擊椎_1, 旋轉加農砲_連發重擊);
                    }
                    if (exList != null) {
                        List<ExtraSkill> eskills = new LinkedList<>();
                        for (int skill : exList) {
                            ExtraSkill eskill = new ExtraSkill(skill, player.getPosition());
                            eskill.Value = 1;
                            eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                            eskills.add(eskill);
                        }
                        player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), eskills));
                    }
                    player.handleCylinder(-8);
                    final MapleStatEffect skillEffect15;
                    if ((skillEffect15 = player.getSkillEffect(彈丸填裝)) != null) {
                        skillEffect15.unprimaryPassiveApplyTo(player);
                    }
                    break;
                }
                case 旋轉加農砲_連發重擊:
                case 旋轉加農砲_雙重棒:
                case 旋轉衝擊椎_1: {
                    if (player.getBuffedIntValue(SecondaryStat.RWOverHeat) <= 0) {
                        player.handleCylinder(1);
                        final MapleStatEffect skillEffect14;
                        if ((skillEffect14 = player.getSkillEffect(彈丸填裝)) != null) {
                            skillEffect14.unprimaryPassiveApplyTo(player);
                            return 1;
                        }
                    }
                    break;
                }
                case 末日飄移_爆炸:
                case 末日飄移:
                case 旋轉衝擊椎_2:
                case 旋轉衝擊椎_3:
                case 旋轉衝擊椎_4:
                case 旋轉衝擊椎_5:
                case 旋轉衝擊椎_6: {
                    player.handleAmmoClip(-1);
                    if (player.getSpecialStat().getBullet() <= 0) {
                        player.dispelEffect(SecondaryStat.RWCylinder);
                        player.handleAmmoClip(8);
                    }
                    final MapleStatEffect skillEffect14;
                    if ((skillEffect14 = player.getSkillEffect(彈丸填裝)) != null) {
                        skillEffect14.unprimaryPassiveApplyTo(player);
                        return 1;
                    }
                    break;
                }
                case 神聖連發重擊: {
                    player.dispelEffect(SecondaryStat.RWMagnumBlow);
                    if (player.getCylinder() > 0) {
                        ExtraSkill eskill = new ExtraSkill(旋轉加農砲精通, player.getPosition());
                        eskill.Value = 1;
                        eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                        player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), Collections.singletonList(eskill)));
                    }
                    break;
                }
                case 旋轉衝擊椎: {
                    player.handleAmmoClip(8);
                    final MapleStatEffect skillEffect17;
                    if ((skillEffect17 = player.getSkillEffect(彈丸填裝)) != null) {
                        skillEffect17.unprimaryPassiveApplyTo(player);
                    }
                    ExtraSkill eskill = new ExtraSkill(旋轉衝擊椎_7, player.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                    player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), Collections.singletonList(eskill)));
                    break;
                }
                case 衝擊波動: {
                    List<Integer> exList = Arrays.asList(釋能衝擊椎_1, 旋轉加農砲_連發重擊);
                    List<ExtraSkill> eskills = new LinkedList<>();
                    for (int skill : exList) {
                        ExtraSkill eskill = new ExtraSkill(skill, player.getPosition());
                        eskill.Value = 1;
                        eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                        eskills.add(eskill);
                    }
                    player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), eskills));
                    if (player.getBuffStatValueHolder(毀滅左輪) != null) {
                        player.getClient().announce(MaplePacketCreator.userBonusAttackRequest(衝擊椎破壞者, 0, Collections.emptyList()));
                    }
                    break;
                }
                case 錘之碎擊_1:
                case 錘之碎擊: {
                    player.getSkillEffect(錘之碎擊_2).applyAffectedArea(player, player.getPosition());
                    break;
                }
                case 連發重擊:
                case 雙重棒:
                case 二段游移:
                case 颶風混裂:
                case 火神重擊: {
                    ExtraSkill eskill = new ExtraSkill(旋轉加農砲精通, player.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                    player.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), Collections.singletonList(eskill)));
                    if (player.getBuffStatValueHolder(毀滅左輪) != null) {
                        player.getClient().announce(MaplePacketCreator.userBonusAttackRequest(衝擊椎破壞者, 0, Collections.emptyList()));
                        break;
                    }
                    break;
                }
            }
            switch (applier.effect.getSourceId()) {
                case 王之子_1:
                case 錘之碎擊_1:
                case 擺動_1: {
                    MapleStatEffect l801;
                    if ((l801 = player.getSkillEffect(衝撞精通)) == null) {
                        break;
                    }
                    if (player.getSkillEffect(屬性強化精通) != null) {
                        l801 = player.getSkillEffect(屬性強化精通);
                    }
                    player.handleAmmoClip(l801.getW());
                    final MapleStatEffect skillEffect18;
                    if ((skillEffect18 = player.getSkillEffect(彈丸填裝)) != null) {
                        skillEffect18.unprimaryPassiveApplyTo(player);
                    }
                    break;
                }
                case 連發重擊:
                case 雙重棒:
                case 旋轉衝擊椎: {
                    MapleStatEffect l802;
                    if ((l802 = player.getSkillEffect(組合訓練)) != null) {
                        if (player.getSkillEffect(組合訓練II) != null) {
                            l802 = player.getSkillEffect(組合訓練II);
                        }
                        l802.unprimaryPassiveApplyTo(player);
                    }
                    break;
                }
            }
        }
        return 1;
    }
}
