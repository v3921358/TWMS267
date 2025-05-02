package Client.skills.handler.英雄團;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Client.force.MapleForceAtom;
import Client.force.MapleForceFactory;
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
import Net.server.maps.SavedLocationType;
import Packet.EffectPacket;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static Config.constants.skills.幻影俠盜.*;

public class 幻影俠盜 extends AbstractSkillHandler {

    public 幻影俠盜() {
        jobs = new MapleJob[]{
                MapleJob.幻影俠盜,
                MapleJob.幻影俠盜1轉,
                MapleJob.幻影俠盜2轉,
                MapleJob.幻影俠盜3轉,
                MapleJob.幻影俠盜4轉
        };

        for (Field field : Config.constants.skills.幻影俠盜.class.getDeclaredFields()) {
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
        int[] ss = {英雄共鳴, 水晶花園傳送, 幻影斗蓬, 高洞察力, 技能竊取, 技能管理, 審判方針AUTO_MANUAL};
        for (int i : ss) {
            if (chr.getLevel() < 200 && i == 英雄共鳴) {
                continue;
            }
            skil = SkillFactory.getSkill(i);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(i, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
        }
        if (chr.getJob() == MapleJob.幻影俠盜4轉.getId()) {
            skil = SkillFactory.getSkill(審判);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(審判, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
            skil = SkillFactory.getSkill(卡牌審判);
            if (skil != null && chr.getSkillLevel(skil) > 0) {
                applier.skillMap.put(卡牌審判, new SkillEntry(0, 0, -1));
            }
        } else {
            skil = SkillFactory.getSkill(卡牌審判);
            if (skil != null && chr.getSkillLevel(skil) <= 0) {
                applier.skillMap.put(卡牌審判, new SkillEntry(1, skil.getMaxMasterLevel(), -1));
            }
            skil = SkillFactory.getSkill(審判);
            if (skil != null && chr.getSkillLevel(skil) > 0) {
                applier.skillMap.put(審判, new SkillEntry(0, 0, -1));
            }
        }
        return -1;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 24141502:
            case 24141501:
                return HexaSKILL.命運詠嘆調;
            case 24141000:
                return 卡牌風暴;
            case HexaSKILL.強化命運鬼牌:
                return 400041009;
            case HexaSKILL.強化黑傑克:
                return 400041022;
            case HexaSKILL.強化幻影標記:
                return 400041040;
            case HexaSKILL.強化間隙破壞:
                return 400041055;
            case 幻影瞬步_1:
                return 幻影瞬步;
            case 多重幻影:
                return 國王突刺;
            case 最終的夕陽1:
                return 最終的夕陽;
            case 死神卡牌_反轉卡:
                return 死神卡牌;
            case 玫瑰四重曲_1:
                return 玫瑰四重曲;
            case 鬼牌_1:
            case 鬼牌_2:
            case 鬼牌_3:
            case 鬼牌_4:
            case 鬼牌_5:
            case 鬼牌_6:
                return 命運鬼牌;
            case 黑傑克_1:
            case 黑傑克_2:
            case 黑傑克_3:
                return 黑傑克;
            case 幻影標記_1:
            case 幻影標記_2:
                return 幻影標記;
            case 間隙破壞_1:
                return 間隙破壞;
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
            case 卡牌審判:
            case 審判:
                statups.put(SecondaryStat.Judgement, 0);
                return 1;
            case 幻影斗蓬:
                statups.put(SecondaryStat.Invisible, effect.getInfo().get(MapleStatInfo.x)); //隱形狀態
                statups.put(SecondaryStat.IndieCooltimeReduce, effect.getInfo().get(MapleStatInfo.indieCooltimeReduce)); // 每次移動冷卻3秒 消除首次移動?
                return 1;
            case 艾麗亞祝禱: //聖歌祈禱 - 受到聖歌的祈禱，攻擊力大幅上升，有一定概率無視敵人的防禦力。
                statups.put(SecondaryStat.DamR, effect.getInfo().get(MapleStatInfo.damR));
                statups.put(SecondaryStat.IgnoreTargetDEF, effect.getInfo().get(MapleStatInfo.damR));
                return 1;
            case 幸運幻影: //神秘的運氣 - 最幸運的幻影可以永久性地提高運氣。使用技能時，進入可以避免一次死亡並恢復體力的幸運狀態。
                effect.getInfo().put(MapleStatInfo.time, 900000);
                statups.put(SecondaryStat.ReviveOnce, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 楓葉祝福:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 最終的夕陽1:
                monsterStatus.put(MonsterStatus.PDR, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 英雄誓言:
                effect.setPartyBuff(true);
                statups.put(SecondaryStat.IndieDamR, effect.getInfo().get(MapleStatInfo.indieDamR));
                return 1;
            case 鬼牌_2:
                statups.put(SecondaryStat.DotHealHPPerSecond, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.DotHealMPPerSecond, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 鬼牌_3:
                //statups.put(MapleBuffStat.HowlingDefence, effect.getInfo().get(MapleStatInfo.z));
                return 1;
            case 鬼牌_5:
                //statups.put(MapleBuffStat.HowlingDefence, effect.getInfo().get(MapleStatInfo.z));
                statups.put(SecondaryStat.IndieAsrR, effect.getInfo().get(MapleStatInfo.indieAsrR));
                return 1;
            case 鬼牌_6:
                statups.put(SecondaryStat.DotHealHPPerSecond, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.DotHealMPPerSecond, effect.getInfo().get(MapleStatInfo.x));
                //statups.put(MapleBuffStat.HowlingDefence, effect.getInfo().get(MapleStatInfo.z));
                return 1;
            case 命運鬼牌:
                statups.put(SecondaryStat.KeyDownMoving, 50);
                return 1;
            case 鬼牌_1:
                effect.getInfo().put(MapleStatInfo.bulletCount, 60);
                return 1;
            case 水晶花園傳送:
                effect.setMoveTo(effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 幻影標記:
                statups.put(SecondaryStat.PhantomMarkOfPhantomTarget, 1);
                statups.put(SecondaryStat.PhantomMarkOfPhantomOwner, 1);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        MapleForceFactory forceFactory = MapleForceFactory.getInstance();
        switch (applier.effect.getSourceId()) {
            case 水晶花園傳送: {
                chr.saveLocation(SavedLocationType.CRYSTALGARDEN);
                return 1;
            }
            case 卡牌審判:
            case 審判: {
                if (chr.getJudgementStack() > 0) {
                    chr.setJudgementStack(0);
                    chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(forceFactory.getMapleForce(chr, applier.effect, 0)), true);
                    return 1;
                }
                c.announce(MaplePacketCreator.sendSkillUseResult(false, 0));
                return 1;
            }
            case 幻影斗蓬: {
                chr.getSkillEffect(幻影斗蓬).unprimaryPassiveApplyTo(chr);
                return 1;
            }
            case 黑傑克: {
                MapleForceAtom mfa = forceFactory.getMapleForce(chr, applier.effect, 0);
                byte unk1 = slea.readByte();
                byte stat = slea.readByte();
                byte unk2 = slea.readByte();
                byte unk3 = slea.readByte();
                mfa.setFirstMobID(slea.readInt());
                slea.skip(3);
                mfa.getRect().x = slea.readInt();
                mfa.getRect().y = slea.readInt();
                chr.putAtomAttackRecord(黑傑克_3, 0);
                chr.putAtomAttackRecord(黑傑克, 0);
                chr.getTempValues().put("useBlackJack", false);
                chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(mfa), true);
                if (stat == 2) {
                    // 解放
                    chr.getClient().announce(MaplePacketCreator.SetForceAtomTarget(黑傑克_1, c.getPlayer().getId(), 3, mfa.getFirstMobID()));
                    chr.getTempValues().put("useBlackJack", true);
                }
                return 1;
            }
            case 幻影標記: {
                List<ExtraSkill> eskills = new LinkedList<>();
                for (int i = 0; i < 7; i++) {
                    ExtraSkill eskill = new ExtraSkill(i < 6 ? 幻影標記_1 : 幻影標記_2, chr.getPosition());
                    eskill.Value = 1;
                    eskill.FaceLeft = chr.isFacingLeft() ? 0 : 1;
                    eskills.add(eskill);
                }
                chr.getClient().announce(MaplePacketCreator.RegisterExtraSkill(applier.effect.getSourceId(), eskills));
                chr.dispelEffect(幻影標記);
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 水晶花園傳送: {
                applyto.changeMap(applier.effect.getX(), 0);
                return 1;
            }
            case 卡牌審判:
            case 審判: {
                int dice = Randomizer.nextInt(applier.effect.getSourceId() == 卡牌審判 ? 2 : 4);
                applier.buffz = applier.effect.getV();
                switch (dice) {
                    case 0: //爆擊概率增加 %
                        applier.buffz = applier.effect.getV();
                        break;
                    case 1: //物品掉落率增加 %
                        applier.buffz = applier.effect.getW();
                        break;
                    case 2: //狀態異常抗性/屬性抗性分別增加 %
                        applier.buffz = applier.effect.getX() * 100 + applier.effect.getY();
                        break;
                    case 3: //攻擊時，將傷害的x%轉換為HP
                        ++dice;
                        applier.buffz = applier.effect.getZ();
                        break;
                }
                applier.localstatups.put(SecondaryStat.Judgement, dice + 1);
                applyto.getClient().announce(EffectPacket.showDiceEffect(-1, applier.effect.getSourceId(), applier.effect.getLevel(), dice, -1, false));
                applyto.getMap().broadcastMessage(applyto, EffectPacket.showDiceEffect(applyto.getId(), applier.effect.getSourceId(), applier.effect.getLevel(), dice, -1, false), false);
                return 1;
            }
            case 幸運幻影: {
                if (!applier.primary) {
                    applier.localstatups.clear();
                    applier.duration = 4000;
                    applier.localstatups.put(SecondaryStat.NotDamaged, 1);
                    applyto.registerSkillCooldown(applier.effect, true);
                }
                return 1;
            }
            case 英雄誓言: {
                if (applyfrom.getJob() / 1000 != applyto.getJob() / 1000) {
                    return 0;
                }
                applyto.dispelEffect(Config.constants.skills.狂狼勇士.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.龍魔導士.英雄歐尼斯);
                applyto.dispelEffect(Config.constants.skills.夜光.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.精靈遊俠.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.幻影俠盜.英雄誓言);
                applyto.dispelEffect(Config.constants.skills.隱月.英雄誓約);
                return 1;
            }
            case 鬼牌_2:
            case 鬼牌_3:
            case 鬼牌_4:
            case 鬼牌_5:
            case 鬼牌_6: {
                applyto.getClient().announce(applier.effect.isSkill() ? EffectPacket.showBuffEffect(applyto, false, 命運鬼牌, applier.effect.getLevel(), 1, applier.pos) : EffectPacket.showBuffItemEffect(-1, applier.effect.getSourceId()));
                applier.b3 = true;
                return 1;
            }
            case 幻影標記: {
                if (!applier.primary) {
                    applier.localstatups.put(SecondaryStat.PhantomMarkOfPhantomTarget, Math.min(7, Math.max(0, applyto.getBuffedIntValue(SecondaryStat.PhantomMarkOfPhantomTarget) + 1)));
                    return 1;
                }
                applier.overwrite = false;
                applier.localstatups.clear();
                return 1;
            }
        }
        return -1;
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        if (applier.effect != null && applier.effect.getSourceId() == 玫瑰四重曲) {
            applier.effect.applyAffectedArea(applyfrom, applyto.getPosition());
        }
        final MapleStatEffect skillEffect18 = applyfrom.getSkillEffect(幻影標記);
        if (applier.effect != null && containsJob(applier.effect.getSourceId() / 10000) && applier.effect.getSourceId() % 10000 / 1000 == 1 && skillEffect18 != null) {
            if (applyto.getObjectId() != applyfrom.getLinkMobObjectID()) {
                applyfrom.setLinkMobObjectID(applyto.getObjectId());
                applyfrom.dispelEffect(幻影標記);
            }
            skillEffect18.unprimaryPassiveApplyTo(applyfrom);
        }
        return 1;
    }
}
