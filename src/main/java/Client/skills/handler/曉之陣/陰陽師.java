package Client.skills.handler.曉之陣;

import Client.*;
import Client.force.MapleForceFactory;
import Client.inventory.Equip;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
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
import Net.server.life.MapleMonster;
import Net.server.maps.MapleSummon;
import Packet.*;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static Config.constants.skills.陰陽師.*;

public class 陰陽師 extends AbstractSkillHandler {

    public 陰陽師() {
        jobs = new MapleJob[]{
                MapleJob.陰陽師,
                MapleJob.陰陽師1轉,
                MapleJob.陰陽師2轉,
                MapleJob.陰陽師3轉,
                MapleJob.陰陽師4轉
        };

        for (Field field : Config.constants.skills.陰陽師.class.getDeclaredFields()) {
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
        int[] ss = {五行的陰陽師, 無限的靈力, 花狐的同行, 英雄共鳴, 五星的歸還};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == 英雄共鳴) {
                continue;
            }
            skil = SkillFactory.getSkill(i);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(i, new SkillEntry(skil.getMaxLevel(), skil.getMaxMasterLevel(), -1));
            }
        }
        return -1;
    }

    public boolean is紫扇仰波(int skillId) {
        int linkedSkillID = getLinkedSkillID(skillId);
        return linkedSkillID == 紫扇仰波 || linkedSkillID == 紫扇仰波_貳 || linkedSkillID == 紫扇仰波_參 || linkedSkillID == 紫扇仰波_肆;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 42141501:
            case 42141502:
                return HexaSKILL.十尾妖狐的羈絆;
            case HexaSKILL.雪女召喚強化:
                return 400021017;
            case HexaSKILL.靈石召喚強化:
                return 400021054;
            case HexaSKILL.怨靈解放陣強化:
                return 400021078;
            case HexaSKILL.鬼夜叉_大鬼封魂陣強化:
                return 400021114;
            case 靈脈轉移_1:
                return 靈脈轉移;
            case 花狐的回復:
            case 花炎結界:
            case 花狐的祝福:
            case 幽玄氣息:
                return 影朋_花狐;
            case 花炎結界2:
            case 花狐的祝福2:
            case 幽玄氣息2:
                return 幻醒_花狐;
            case 式神炎舞_1:
                return 式神炎舞;
            case 紫扇仰波_2擊:
            case 紫扇仰波_3擊:
                return 紫扇仰波;
            case 紫扇仰波_肆_1擊:
            case 紫扇仰波_肆_2擊:
            case 紫扇仰波_肆_3擊:
                return 紫扇仰波_肆;
            case 紫扇仰波_參_1擊:
            case 紫扇仰波_參_2擊:
            case 紫扇仰波_參_3擊:
                return 紫扇仰波_參;
            case 紫扇仰波_貳_1擊:
            case 紫扇仰波_貳_2擊:
            case 紫扇仰波_貳_3擊:
                return 紫扇仰波_貳;
            case 紫光白狐_被動:
                return 紫光白狐;
            case 結界_櫻_靈脈結合:
                return 結界_櫻;
            case 雙天狗_左:
            case 雙天狗_右:
                return 雙天狗;
            case 迎式神_發動:
                return 迎式神;
            case 妖繪釋放_炒炭:
                return 妖繪釋放;
            case 結界_鈴蘭_靈脈結合:
                return 結界_鈴蘭;
            case 靈脈的氣息:
                return 風水師;
            case 雪女招喚_1:
                return 雪女招喚;
            case 怨靈解放陣_1:
            case 怨靈解放陣_2:
            case 怨靈解放陣_3:
                return 怨靈解放陣;
            case 鬼夜叉_大鬼封魂陣_1:
                return 鬼夜叉_大鬼封魂陣;
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
            case 影朋_花狐:
                statups.put(SecondaryStat.ChangeFoxMan, 1);
                return 1;
            case 花炎結界:
            case 花炎結界2:
                statups.put(SecondaryStat.FireBarrier, 3);
                return 1;
            case 幽玄氣息:
            case 幽玄氣息2:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.Stance, 0);
                statups.put(SecondaryStat.IgnoreTargetDEF, 0);
                statups.put(SecondaryStat.BlessEnsenble, 5);
                return 1;
            case 花狐的祝福:
            case 花狐的祝福2:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.FoxBless, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 迎式神:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.KannaSiksinAutoAttack, effect.getLevel());
                return 1;
            case 結界_櫻:
                monsterStatus.put(MonsterStatus.PAD, effect.getInfo().get(MapleStatInfo.x));
                monsterStatus.put(MonsterStatus.MAD, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 扇_孔雀:
                statups.put(SecondaryStat.Booster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 曉月勇者:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 公主的加護:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 雪女招喚:
                statups.put(SecondaryStat.ReduceMP, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 陰陽除靈符:
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 退魔流星符:
                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 紫扇仰波_貳:
                monsterStatus.put(MonsterStatus.Burned, 1);
                return 1;
            case 紫扇仰波_肆:
                monsterStatus.put(MonsterStatus.Burned, 4);
                return 1;
            case 一鬼踏殲:
                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 破魔陣:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.AntiEvilShield, effect.getLevel());
                return 1;
            case 怨靈解放陣:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.GhostLiberationStack, 0);
                return 1;
            case 靈石召喚:
                statups.put(SecondaryStat.IndieAsrR, effect.getW());
                return 1;
            case 鬼夜叉_大鬼封魂陣:
                statups.put(SecondaryStat.KannaFifthAttract, 1);
                return 1;
            case 鬼夜叉_大鬼封魂陣_1:
                effect.getInfo().put(MapleStatInfo.time, effect.getInfo().get(MapleStatInfo.attackDelay));
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 雙天狗: {
                Point pos = slea.readPos();
                MapleSummon summon = chr.getSummonBySkillID(雙天狗_隱藏);
                if (summon != null) {
                    final int ownerId = summon.getOwnerId();
                    final int objectId = summon.getObjectId();

                    chr.getMap().broadcastMessage(SummonPacket.SummonedForceReturn(ownerId, objectId, pos), chr.getPosition());
                    chr.getMap().broadcastMessage(SummonPacket.SummonedForceMove(summon, 雙天狗, applier.effect.getLevel(), pos), chr.getPosition());
                    chr.getMap().disappearMapObject(summon);
                    chr.removeSummon(summon);
                    for (int i = 0; i < 2; i++) {
                        summon = chr.getSummonBySkillID(雙天狗_左 + i);
                        if (summon != null) {
                            chr.getMap().broadcastMessage(SummonPacket.SummonedForceMove(summon, summon.getSkillId(), summon.getSkillLevel(), pos), chr.getPosition());
                            chr.getMap().broadcastMessage(SummonPacket.SummonMagicCircleAttack(summon, 9, pos), chr.getPosition());
                        }
                    }
                }
                return 1;
            }
            case 雪女招喚: {
                chr.getSkillEffect(雪女招喚_1).applyTo(chr);
                return 1;
            }
            case 影朋_花狐: {
                if (chr.getHaku() != null) {
                    if (chr.getSkillEffect(幻醒_花狐) != null) {
                        chr.getHaku().setSpecialState(2);
                    }
                    chr.getHaku().setState(2);
                    chr.getHaku().update(chr);
                }
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyTo(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 鬼夜叉_大鬼封魂陣) {
            applier.cooldown = 0;
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 五星的歸還: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 花狐的回復:
            case 花狐的恢復_貳: {
                applyto.addHPMP(applyto.getStat().getCurrentMaxHP() * applier.effect.getHp() / 100, 0, false);
                return 1;
            }
            case 花炎結界:
            case 花炎結界2: {
                if (applier.primary || applyto.getBuffedValue(SecondaryStat.FireBarrier) == null) {
                    return 1;
                }
                SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.FireBarrier);
                if (mbsvh != null) {
                    applier.duration = mbsvh.getLeftTime();
                }
                int value = applyto.getBuffedIntValue(SecondaryStat.FireBarrier) - 1;
                applier.localstatups.put(SecondaryStat.FireBarrier, Math.max(0, value));
                if (value <= 0) {
                    applier.overwrite = false;
                    applier.localstatups.clear();
                }
                return 1;
            }
            case 幽玄氣息:
            case 幽玄氣息2: {
                final int n = applier.effect.getSourceId() == 幽玄氣息 ? 3 : 5;
                int count = 0;
                if (applyfrom.getParty() != null) {
                    count = applyfrom.getParty().getCharsInSameField(applyfrom).size();
                } else if (applyfrom == applyto) {
                    count = 1;
                }
                Integer value = applyto.getBuffedValue(SecondaryStat.BlessEnsenble);
                if (value != null) {
                    count *= n;
                    if (count == value) {
                        return 0;
                    }
                    applier.localstatups.put(SecondaryStat.BlessEnsenble, count);
                    if (count <= 0) {
                        applier.overwrite = false;
                        applier.localstatups.clear();
                    }
                } else {
                    if (count <= 0) {
                        return 0;
                    }
                    applier.localstatups.put(SecondaryStat.BlessEnsenble, count * n);
                }
                return 1;
            }
            case 花狐的祝福:
            case 花狐的祝福2: {
                Item item = applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -5200);
                if (item != null) {
                    Equip fan = (Equip) item;
                    applier.localstatups.put(SecondaryStat.FoxBless, (int) Math.floor(fan.getTotalMad() * applier.effect.getStatups().get(SecondaryStat.FoxBless) / 100.0D));
                } else {
                    applier.localstatups.put(SecondaryStat.FoxBless, 0);
                }
                return 1;
            }
            case 雙天狗_隱藏: {
                applier.localstatups.clear();
                return 1;
            }
            case 破魔陣: {
                if (applier.att) {
                    return 0;
                }
                return 1;
            }
            case 公主的加護: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(Config.constants.skills.劍豪.公主的加護);
                applyto.dispelEffect(Config.constants.skills.陰陽師.公主的加護);
                return 1;
            }
            case 怨靈解放陣: {
                if (applier.primary && !applier.passive) {
                    applier.localstatups.put(SecondaryStat.GhostLiberationStack, Math.min(5, applyto.getBuffedIntValue(SecondaryStat.GhostLiberationStack) + 1));
                    return 1;
                }
                return 0;
            }
            case 怨靈解放陣_2: {
                if (applier.primary && !applier.passive) {
                    SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.GhostLiberationStack);
                    mbsvh.value = 0;
                    applyto.send(BuffPacket.giveBuff(applyto, mbsvh.effect, Collections.singletonMap(SecondaryStat.GhostLiberationStack, mbsvh.effect.getSourceId())));
                    return 1;
                }
                return 0;
            }
            case 靈石召喚: {
                if (applier.att) {
                    return 0;
                }
                if (applier.passive) {
                    applyto.addHPMP(applier.effect.getW(), applier.effect.getY());
                }
                return 1;
            }
            case 鬼夜叉_大鬼封魂陣: {
                if (applier.att) {
                    return 0;
                }
                if (applyto.getSummonBySkillID(applier.effect.getSourceId()) != null) {
                    applyto.dispelEffect(applier.effect.getSourceId());
                    return 0;
                }
                SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.KannaFifthAttract);
                if (mbsvh != null && mbsvh.sourceID == 0) {
                    mbsvh.z = 0;
                }
                applier.maskedstatups.put(SecondaryStat.IndieNotDamaged, 1);
                applier.maskedstatups.put(SecondaryStat.IndieIgnorePCounter, 1);
                applier.maskedDuration = 2000;
                applier.b7 = false;
                applier.buffz = 0;
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applier.effect != null) {
            if (applyfrom.getSkillEffect(靈脈的氣息) != null && (is紫扇仰波(applier.effect.getSourceId()) || applier.effect.getSourceId() == 陰陽除靈符 || applier.effect.getSourceId() == 刺刺櫻)) {
                if (Randomizer.isSuccess(applyfrom.getSkillEffect(靈脈的氣息).getW()) && applyfrom.getMap().getAffectedAreaObject(applyfrom.getId(), 靈脈的氣息).size() < 3) {
                    applyfrom.getSkillEffect(靈脈的氣息).applyAffectedArea(applyfrom, applyto.getPosition());
                }
            }
            if (applier.effect.getSourceId() == 陰陽除靈符) {
                applier.effect.applyAffectedArea(applyfrom, applyto.getPosition());
            }
            final MapleStatEffect skillEffect;
            if (applyto.isAlive() && applier.effect.getSourceId() != 夜雀 && (skillEffect = applyfrom.getSkillEffect(夜雀)) != null && skillEffect.makeChanceResult(applyfrom)) {
                applyfrom.getMap().broadcastMessage(applyfrom, ForcePacket.forceAtomCreate(MapleForceFactory.getInstance().getMapleForce(applyfrom, skillEffect, 0, applyto.getObjectId(), null, applyto.getPosition())), true);
            }
        }
        if (!applyto.isAlive()) {
            if (applyfrom.getSkillLevel(御身消滅) > 0 && (applier.effect == null || (applier.effect != null && applier.effect.getSourceId() != 御身消滅))) {
                applyfrom.getClient().announce(MobPacket.enableSoulBomb(500, applyto.getPosition()));
            }
            if (applyfrom.getSkillLevel(吸生纏氣) > 0) {
                MapleStatEffect eff = applyfrom.getSkillEffect(吸生纏氣);
                int toHeal = 0;
                if (applyto.isBoss()) {
                    toHeal = eff.getX();
                } else {
                    toHeal = applyfrom.getSkillLevel(吸生纏氣);
                }
                applyfrom.addHPMP((int) ((toHeal / 100.0) * applyfrom.getStat().getCurrentMaxHP()), 0, false, true);
            }
        }
        return 1;
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        if (applier.effect != null) {
            switch (applier.effect.getSourceId()) {
                case 鬼夜叉_老么:
                case 鬼夜叉_二哥:
                case 鬼夜叉_大哥:
                case 鬼夜叉_老大: {
                    if ((applier.ai.display & 0x10) != 0) {
                        player.dispelEffect(applier.effect.getSourceId());
                    }
                    break;
                }
            }
            if ((is紫扇仰波(applier.effect.getSourceId()) || applier.effect.getSourceId() == 破邪連擊符) && player.getSkillEffect(怨靈解放陣) != null && !player.isSkillCooling(怨靈解放陣) && !player.isSkillCooling(怨靈解放陣_2)) {
                ExtraSkill eskill = new ExtraSkill(怨靈解放陣, player.getPosition());
                eskill.Value = 1;
                eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                player.send(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), Collections.singletonList(eskill)));
            }
            if (applier.effect.getSourceId() == 雪女招喚) {
                player.addHPMP(applier.effect.getY(), 0);
            }
        }
        return 1;
    }

    @Override
    public int onAfterCancelEffect(MapleCharacter player, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 雙天狗_左:
            case 雙天狗_右:
                if (!applier.overwrite) {
                    player.removeSummon(雙天狗_隱藏);
                }
                return 1;
        }
        return -1;
    }
}
