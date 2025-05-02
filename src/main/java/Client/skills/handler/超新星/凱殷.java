package Client.skills.handler.超新星;

import Client.*;
import Client.skills.ExtraSkill;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.HexaSKILL;
import Client.skills.handler.SkillClassApplier;
import Client.status.MonsterStatus;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.maps.ForceAtomObject;
import Packet.AdelePacket;
import Packet.MaplePacketCreator;
import tools.Pair;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

import static Config.constants.skills.凱殷.*;

public class 凱殷 extends AbstractSkillHandler {

    public 凱殷() {
        jobs = new MapleJob[]{
                MapleJob.凱殷,
                MapleJob.凱殷1轉,
                MapleJob.凱殷2轉,
                MapleJob.凱殷3轉,
                MapleJob.凱殷4轉
        };

        for (Field field : Config.constants.skills.凱殷.class.getDeclaredFields()) {
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
        final int[] ss = {前往圖倫城市, 獨門咒語};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == 獨門咒語) {
                continue;
            }
            skil = SkillFactory.getSkill(i);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(i, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 63141502:
            case 63141503:
                return 63141500;
            case 63141000:
                return 破塵箭;
            case HexaSKILL.強化龍之爆裂:
                return 400031061;
            case HexaSKILL.強化致命奇襲:
                return 400031065;
            case HexaSKILL.強化死亡降臨:
                return 400031062;
            case HexaSKILL.強化掌握痛苦:
                return 400031066;
            case 63141004:
            case 63141005:
            case 63141006:
                return 處刑_毒針;
            case 具現_衝擊箭:
            case 衝擊箭_1:
                return 衝擊箭;
            case 暗影步伐_1:
            case 暗影步伐_2:
                return 暗影步伐;
            case 暗影迅捷_1:
                return 暗影迅捷;
            case 衝擊箭II_1:
            case 具現_衝擊箭_1:
            case 具現_衝擊箭_2:
                return 衝擊箭II;
            case 具現_散射箭:
            case 具現_散射箭_1:
                return 散射箭;
            case 龍炸裂_1:
                return 龍炸裂;
            case 衝擊箭III_1:
                return 衝擊箭III;
            case 崩壞爆破_1:
            case 崩壞爆破_2:
            case 具現_強化崩壞爆破:
            case 具現_強化崩壞爆破_1:
            case 具現_強化崩壞爆破_2:
            case 具現_強化崩壞爆破_3:
            case 具現_強化崩壞爆破_4:
                return 崩壞爆破;
            case 殘留憤恨_1:
                return 殘留憤恨;
            case 死亡祝福_1:
            case 死亡祝福_2:
                return 死亡祝福;
            case 具現_破塵箭:
            case 具現_破塵箭_1:
            case 具現_破塵箭_2:
                return 破塵箭;
            case 處刑_鎖鏈鐮刀_1:
                return 處刑_鎖鏈鐮刀;
            case 處刑_毒針_1:
                return 處刑_毒針;
            case 暗地狙擊_1:
            case 具現_處刑_暗地狙擊:
            case 具現_處刑_暗地狙擊_1:
            case 具現_處刑_暗地狙擊_2:
                return 暗地狙擊;
            case 死亡降臨_1:
            case 死亡降臨_2:
                return 死亡降臨;
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
            case 事前準備:
            case 事前準備_傳授:
                statups.put(SecondaryStat.IndieDamR, effect.getY());
                return 1;
            case 事前準備_計數:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.NALinkSkill, 1);
                return 1;
            case 暗影步伐:
                effect.getInfo().put(MapleStatInfo.time, effect.getSubTime());
                statups.put(SecondaryStat.DarkSight, effect.getLevel());
                return 1;
            case 主導:
                statups.put(SecondaryStat.NADragonEnchant, 1);
                return 1;
            case 龍炸裂:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.NABrutalPang, 1);
                return 1;
            case 龍息射手加速器:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 殘留憤恨:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.NARemainIncense, 1);
                return 1;
            case 龍之延展:
                effect.getInfo().put(MapleStatInfo.time, effect.getY());
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
            case 超新星勇士:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 化身:
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                statups.put(SecondaryStat.IndieStance, effect.getInfo().get(MapleStatInfo.indieStance));
                statups.put(SecondaryStat.IndiePADR, effect.getInfo().get(MapleStatInfo.indiePadR));
                statups.put(SecondaryStat.NovaArcherIncanation, 1);
                return 1;
            case 死亡降臨:
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                statups.put(SecondaryStat.IndieStance, effect.getInfo().get(MapleStatInfo.indieStance));
                statups.put(SecondaryStat.NAThanatosDescent, 1);
                return 1;
            case 死亡降臨_2:
                effect.getInfo().put(MapleStatInfo.cooltime, 180);
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
            case 掌握痛苦:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.NAOminousStream, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 暗影步伐_1:
                MapleStatEffect effect = chr.getSkillEffect(暗影步伐);
                if (effect != null) {
                    applier.effect = effect;
                }
                return 1;
            case 主導:
                if (chr.getSpecialStat().getMaliceCharge() < 100) {
                    chr.dropMessage(5, "沒有準備任何的惡意之石。");
                    return 0;
                }
                if (chr.hasBuffSkill(主導)) {
                    chr.dropMessage(5, "已經是主導狀態。");
                    return 0;
                }
                chr.handleMaliceCharge(-100);
                return 1;
            case 龍炸裂:
                if (chr.hasBuffSkill(applier.effect.getSourceId())) {
                    Map<Integer, ForceAtomObject> swordsMap = chr.getForceAtomObjects();
                    List<ForceAtomObject> removeList = new ArrayList<>();
                    Iterator<Map.Entry<Integer, ForceAtomObject>> iterator = swordsMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Integer, ForceAtomObject> sword = iterator.next();
                        if (sword.getValue().SkillId == 龍炸裂_1) {
                            removeList.add(sword.getValue());
                            iterator.remove();
                        }
                    }
                    if (!removeList.isEmpty()) {
                        chr.getMap().broadcastMessage(AdelePacket.ForceAtomObjectRemove(chr.getId(), removeList, 1), chr.getPosition());
                    }
                    chr.dispelEffect(applier.effect.getSourceId());
                    return 0;
                }
                return 1;
            case 殘留憤恨:
                if (chr.hasBuffSkill(applier.effect.getSourceId())) {
                    chr.dispelEffect(applier.effect.getSourceId());
                    return 0;
                }
                return 1;
            case 破塵箭:
                effect = chr.getSkillEffect(破塵箭);
                if (effect != null) {
                    int maxValue = effect.getW();
                    int timeout = effect.getU() * 1000;
                    Pair<Integer, Long> skillInfo = (Pair<Integer, Long>) chr.getTempValues().get("MultiSkill" + 破塵箭);
                    if (skillInfo != null) {
                        skillInfo.left -= 1;
                        if (skillInfo.left < 0) {
                            skillInfo.left = 0;
                        }
                        skillInfo.right = System.currentTimeMillis();
                    } else {
                        return 0;
                    }
                    chr.getTempValues().put("MultiSkill" + 破塵箭, skillInfo);
                    chr.send(MaplePacketCreator.multiSkillInfo(破塵箭, skillInfo.left, maxValue, timeout));
                } else {
                    return 0;
                }
                return 1;
        }
        return -1;
    }

    @Override

    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 前往圖倫城市: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 死亡降臨: {
                applyfrom.cancelSkillCooldown(死亡降臨_2);
                return 1;
            }
            case 死亡降臨_2: {
                if (!applier.primary && !applier.passive) {
                    return 0;
                }
                applyfrom.dispelEffect(死亡降臨);
                return 1;
            }
            case 掌握痛苦: {
                if (applier.primary && !applier.passive) {
                    SecondaryStatValueHolder mbsvh;
                    if ((mbsvh = applyfrom.getBuffStatValueHolder(掌握痛苦)) != null) {
                        ForceAtomObject sword = new ForceAtomObject(1, 18, 0, applyfrom.getId(), 0, 掌握痛苦);
                        sword.EnableDelay = 990;
                        sword.Expire = mbsvh.effect.getS2() * 1000 + mbsvh.value * mbsvh.effect.getS() * 1000;
                        Point pt = new Point(applyfrom.getPosition());
                        sword.Position = new Point(pt.x - 102, pt.y - 456);
                        sword.ObjPosition = new Point(pt.x, pt.y);

                        applyfrom.dispelEffect(掌握痛苦);
                        applyfrom.getMap().broadcastMessage(AdelePacket.ForceAtomObject(applyfrom.getId(), Collections.singletonList(sword), 0), applyfrom.getPosition());
                    }
                    return 0;
                }
                return 1;
            }
            case 具現_龍之爆裂:
            case 處刑_致命奇襲: { // 攻擊段數不扣魔，僅這裡扣一次
                if (applyfrom.getCooldownLeftTime(applier.effect.getSourceId()) == 0) {
                    int mpCon = applier.effect.getMpCon();
                    applyfrom.addMP(-mpCon);
                }
            }
            case 處刑_鎖鏈鐮刀:
            case 處刑_毒針:
            case 處刑_衝擊利刃: {
                applyfrom.cancelSkillCooldown(暗影步伐);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.totalDamage > 0 && (applier.ai.skillId == 衝擊箭 || applier.ai.skillId == 衝擊箭II || applier.ai.skillId == 衝擊箭III)) {
            ExtraSkill eskill = new ExtraSkill(衝擊箭_1, new Point(applier.ai.mobAttackInfo.get(0).hitX, applier.ai.mobAttackInfo.get(0).hitY));
            eskill.Value = 1;
            eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
            player.send(MaplePacketCreator.RegisterExtraSkill(applier.ai.skillId, Collections.singletonList(eskill)));
        }

        MapleStatEffect effect;
        if (applier.totalDamage > 0 && containsJob(applier.ai.skillId / 10000)) {
            if ((effect = player.getSkillEffect(主導II)) != null || (effect = player.getSkillEffect(主導)) != null) {
                player.handleMaliceCharge(effect.getX());
            }

            SecondaryStatValueHolder mbsvh;
            if ((mbsvh = player.getBuffStatValueHolder(SecondaryStat.NABrutalPang)) != null && mbsvh.effect != null) {
                int attackCount = (int) (player.getTempValues().getOrDefault("龍炸裂攻擊次數", 0)) + 1;
                player.getTempValues().put("龍炸裂攻擊次數", attackCount);
                if (attackCount >= 5) {
                    player.getTempValues().put("龍炸裂攻擊次數", 0);
                    Map<Integer, ForceAtomObject> swordsMap = player.getForceAtomObjects();

                    ForceAtomObject sword = null;
                    List<Integer> objList = new LinkedList<>();
                    for (int i = 0; i < 3; i++) {
                        for (ForceAtomObject obj : swordsMap.values()) {
                            if (obj.SkillId == 龍炸裂_1 && !objList.contains(obj.Idx)) {
                                objList.add(obj.Idx);
                                break;
                            }
                        }
                        if (objList.size() >= i + 1) {
                            sword = null;
                            continue;
                        }
                        sword = new ForceAtomObject(player.getSpecialStat().gainForceCounter(), 17, i, player.getId(), 0, 龍炸裂_1);
                        sword.Position = new Point(0, 50);
                        sword.ObjPosition = new Point(0, 0);
                        sword.Expire = mbsvh.effect.getW() * 1000;
                        sword.ValueList.add(1);
                        swordsMap.put(sword.Idx, sword);
                        break;
                    }
                    if (sword != null) {
                        player.getMap().broadcastMessage(AdelePacket.ForceAtomObject(player.getId(), Collections.singletonList(sword), 0), player.getPosition());
                    }
                }
            }
        }

        if (applier.totalDamage > 0 && player.getBuffStatValueHolder(SecondaryStat.NABrutalPang) != null && System.currentTimeMillis() - (long) (player.getTempValues().getOrDefault("龍炸裂攻擊冷卻", 0L)) >= 3000) {
            Map<Integer, ForceAtomObject> swordsMap = player.getForceAtomObjects();
            boolean attack = false;
            for (ForceAtomObject sword : swordsMap.values()) {
                if (sword.SkillId == 龍炸裂_1) {
                    player.getMap().broadcastMessage(AdelePacket.ForceAtomObjectAttack(player.getId(), sword.Idx, 1), player.getPosition());
                    attack = true;
                }
            }

            if (attack) {
                player.getTempValues().put("龍炸裂攻擊冷卻", System.currentTimeMillis());
            }
        }
        return 1;
    }
}
