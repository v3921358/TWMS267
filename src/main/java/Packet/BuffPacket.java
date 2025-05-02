/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Client.*;
import Config.constants.GameConstants;
import Config.constants.JobConstants;
import Config.constants.SkillConstants;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.劍豪;
import Config.constants.skills.夜光;
import Config.constants.skills.狂狼勇士;
import Config.constants.skills.通用V核心;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MobSkill;
import Opcode.Headler.OutHeader;
import Server.Buffstat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.*;

/**
 * @author PlayDK
 */
public class BuffPacket {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(BuffPacket.class);

    /*
     * 更新夜光當前界面的光暗點數
     */
    public static byte[] updateLuminousGauge(int points, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ChangeLarknessStack.getValue());
        mplew.writeInt(points);
        mplew.write(type);
        mplew.writeInt(DateUtil.getTime(System.currentTimeMillis()));

        return mplew.getPacket();
    }

    public static byte[] giveBuff(MapleCharacter chr, MapleStatEffect effect, Map<SecondaryStat, Integer> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_TemporaryStatSet.getValue());
        final EnumMap<SecondaryStat, SecondaryStatValueHolder> holderMap = new EnumMap<>(SecondaryStat.class);
        boolean isWriteIntValue = false;
        for (final Map.Entry<SecondaryStat, Integer> entry : statups.entrySet()) {
            SecondaryStatValueHolder mbsvh = chr.getBuffStatValueHolder(entry.getKey(), entry.getValue());
            if (mbsvh == null) {
                switch (entry.getKey()) {
                    case InnerStorm:
                        mbsvh = new SecondaryStatValueHolder(entry.getValue(), 0);
                        break;
                    case RWCylinder:
                        mbsvh = new SecondaryStatValueHolder(entry.getValue(), 1);
                        break;
                    case AnimaThiefTaoistGauge:
                        mbsvh = new SecondaryStatValueHolder(chr.getSpecialStat().getHoYoungRune(), chr.getJob());
                        break;
                    case AnimaThiefTaoistType:
                        mbsvh = new SecondaryStatValueHolder(chr.getSpecialStat().getHoYoungState1(), chr.getJob());
                        break;
                    case LWSwordGauge:
                        mbsvh = new SecondaryStatValueHolder(chr.getSpecialStat().getAdeleCharge(), chr.getJob());
                        break;
                    case NADragonGauge:
                        mbsvh = new SecondaryStatValueHolder(chr.getSpecialStat().getMaliceCharge(), MapleJob.凱殷.getId());
                        mbsvh.localDuration = 2100000000;
                        break;
                    case KinesisPsychicPoint:
                        mbsvh = new SecondaryStatValueHolder(chr.getSpecialStat().getPP(), chr.getJob());
                        mbsvh.localDuration = 2100000000;
                        break;
                    case SoulMP:
                        mbsvh = new SecondaryStatValueHolder(chr.getSoulMP(), entry.getValue());
                        mbsvh.localDuration = 2100000000;
                        break;
                    case FullSoulMP:
                        mbsvh = new SecondaryStatValueHolder(chr.getSoulOption(), entry.getValue());
                        mbsvh.localDuration = 640000;
                        break;
                    case SpecterGauge:
                        mbsvh = new SecondaryStatValueHolder(1, chr.getJob());
                        mbsvh.z = chr.getSpecialStat().getErosions();
                        mbsvh.localDuration = 2100000000;
                        break;
                    case SpellBullet_Plain:
                        mbsvh = new SecondaryStatValueHolder(1, chr.getJob());
                        mbsvh.z = chr.getSpecialStat().getPureBeads() << 1;
                        mbsvh.localDuration = 2100000000;
                        break;
                    case SpellBullet_Scarlet:
                        mbsvh = new SecondaryStatValueHolder(1, chr.getJob());
                        mbsvh.z = chr.getSpecialStat().getFlameBeads();
                        mbsvh.localDuration = 2100000000;
                        break;
                    case SpellBullet_Gust:
                        mbsvh = new SecondaryStatValueHolder(1, chr.getJob());
                        mbsvh.z = chr.getSpecialStat().getGaleBeads();
                        mbsvh.localDuration = 2100000000;
                        break;
                    case SpellBullet_Abyss:
                        mbsvh = new SecondaryStatValueHolder(1, chr.getJob());
                        mbsvh.z = chr.getSpecialStat().getAbyssBeads();
                        mbsvh.localDuration = 2100000000;
                        break;
                    case FlameWizardInfiniteFlame:
                        mbsvh = new SecondaryStatValueHolder(1, chr.getJob());
                        mbsvh.z = entry.getValue();
                        mbsvh.localDuration = 2100000000;
                        break;
                    case MobZoneState:
                        mbsvh = new SecondaryStatValueHolder(1, 0);
                        mbsvh.z = entry.getValue();
                        mbsvh.localDuration = 2100000000;
                        break;
                }
            }
            if (mbsvh != null) {
                holderMap.put(entry.getKey(), mbsvh);
                if (SkillConstants.isWriteBuffIntValue(entry.getKey())) {
                    isWriteIntValue = true;
                }
            }
        }
        if (holderMap.containsKey(SecondaryStat.DawnShield_WillCare)) {
            holderMap.put(SecondaryStat.DawnShield_ExHP, new SecondaryStatValueHolder(0, holderMap.get(SecondaryStat.DawnShield_WillCare).sourceID));
        }
        writeBuffMask(mplew, holderMap.keySet());

        for (Map.Entry<SecondaryStat, SecondaryStatValueHolder> entry : holderMap.entrySet()) {
            switch (entry.getKey()) {
                case DawnShield_ExHP:
                case DawnShield_WillCare:
                case InnerStorm:
                case EXP_CARD:
                    break;
                default: {
                    if (!entry.getKey().canStack() && entry.getValue() != null) {
                        int level = 0;
                        if (entry.getValue().effect instanceof MobSkill) {
                            level = entry.getValue().effect.getLevel();
                        }
                        int nValue = entry.getValue().value;
                        switch (entry.getKey()) {
                            case MobZoneState:
                                nValue = 1;
                                break;
                            case DotHealHPPerSecond:
                                nValue = chr.getStat().getCurrentMaxHP() * nValue / 100;
                                break;
                            case DotHealMPPerSecond:
                                nValue = chr.getStat().getCurrentMaxMP() * nValue / 100;
                                break;
                        }
                        if (isWriteIntValue) {
                            mplew.writeInt(nValue);
                        } else {
                            mplew.writeShort(nValue);
                        }
                        if (level > 0) {
                            mplew.writeShort(entry.getValue().sourceID);
                            mplew.writeShort(level);
                        } else {
                            mplew.writeInt(entry.getValue().sourceID);
                        }
                        mplew.writeInt(冰雷.元素適應_雷冰 == entry.getValue().sourceID || entry.getValue().getLeftTime() == 2100000000 ? 0 : entry.getValue().getLeftTime());
                        if (entry.getKey() == SecondaryStat.SummonProp) {
                            if (isWriteIntValue) {
                                mplew.writeInt(nValue);
                            } else {
                                mplew.writeShort(nValue);
                            }
                            if (effect.isSkill()) {
                                mplew.write(0);
                            }
                            if (level > 0) {
                                mplew.writeShort(entry.getValue().sourceID);
                                mplew.writeShort(level);
                            } else {
                                mplew.writeInt(entry.getValue().sourceID);
                            }
                            mplew.writeShort(0);
                            mplew.writeInt(entry.getValue().getLeftTime() == 2100000000 ? 0 : entry.getValue().getLeftTime());
                        }
                    }
                    break;
                }
            }
        }
        writeBuffData(mplew, holderMap, effect, chr);// 正常Buff有長度為9的封包
        encodeForClient(chr, mplew, holderMap);

        for (SecondaryStat stat : holderMap.keySet()) {
            if (stat.canStack() && !SkillConstants.isSpecialStackBuff(stat)) {
                encodeIndieBuffStat(mplew, chr, stat);
            }
        }
        if (holderMap.containsKey(SecondaryStat.OutSide)) {
            mplew.writeInt(1000);
        }
        if (holderMap.containsKey(SecondaryStat.LefGloryWing)) {
            mplew.writeInt(chr.getBuffedIntZ(SecondaryStat.LefGloryWing));
            mplew.writeInt(chr.getBuffedIntZ(SecondaryStat.LefGloryWing));
        }
        if (holderMap.containsKey(SecondaryStat.LefBuffMastery)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.WeaponVariety)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.OverloadMode)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.SpecterGauge)) {
            mplew.writeInt(holderMap.get(SecondaryStat.SpecterGauge).z);
        }
        if (holderMap.containsKey(SecondaryStat.SpellBullet_Plain)) {
            mplew.writeInt(holderMap.get(SecondaryStat.SpellBullet_Plain).z);
            mplew.writeInt(holderMap.get(SecondaryStat.SpellBullet_Plain).z);
        }
        if (holderMap.containsKey(SecondaryStat.SpellBullet_Scarlet)) {
            mplew.writeInt(holderMap.get(SecondaryStat.SpellBullet_Scarlet).z);
            mplew.writeInt(holderMap.get(SecondaryStat.SpellBullet_Scarlet).z);
        }
        if (holderMap.containsKey(SecondaryStat.SpellBullet_Gust)) {
            mplew.writeInt(holderMap.get(SecondaryStat.SpellBullet_Gust).z);
            mplew.writeInt(holderMap.get(SecondaryStat.SpellBullet_Gust).z);
        }
        if (holderMap.containsKey(SecondaryStat.SpellBullet_Abyss)) {
            mplew.writeInt(holderMap.get(SecondaryStat.SpellBullet_Abyss).z);
            mplew.writeInt(holderMap.get(SecondaryStat.SpellBullet_Abyss).z);
        }
        if (holderMap.containsKey(SecondaryStat.FlameWizardInfiniteFlame)) {
            mplew.writeInt(chr.getBuffedIntValue(SecondaryStat.FlameWizardInfiniteFlame));
        }
        if (holderMap.containsKey(SecondaryStat.PhantomMarkOfPhantomOwner)) {
            mplew.writeInt(chr.getLinkMobObjectID());
        }
        if (holderMap.containsKey(SecondaryStat.PhantomMarkOfPhantomTarget)) {
            mplew.writeInt(chr.getBuffedIntValue(SecondaryStat.PhantomMarkOfPhantomTarget));
        }
        if (holderMap.containsKey(SecondaryStat.NightWalkerBat)) {
            mplew.writeInt(chr.getBuffedIntValue(SecondaryStat.NightWalkerBat));
        }
        if (holderMap.containsKey(SecondaryStat.DecBaseDamageDebuff)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.LimitEquipStatDebuff)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.ComboCounter)) {
            mplew.writeInt("1".equals(chr.getOneInfo(1544, String.valueOf(英雄.鬥氣集中))) ? 1 : 0); // 0 = 顯示特效, 1 = 沒有特效 // x
            mplew.writeInt(0x50000A); // 0A 00 50 00 <== 0xA wz[v]最終傷害增加量提升, 0x50 wz[prop]機率 // m
        }
        if (holderMap.containsKey(SecondaryStat.FifthGoddessBless)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.PathFinderAncientGuidance)) {
            SecondaryStatValueHolder mbsvh = holderMap.get(SecondaryStat.PathFinderAncientGuidance);
            mplew.writeInt(mbsvh.startTime);
            mplew.writeInt(mbsvh.z);
        }
        if (holderMap.containsKey(SecondaryStat.HolySymbol)) {
            SecondaryStatValueHolder mbsvh = holderMap.get(SecondaryStat.HolySymbol);
            mplew.writeInt(mbsvh.sourceID == 主教.神聖祈禱 ? mbsvh.fromChrID : 0);
            mplew.writeInt(mbsvh.sourceID == 主教.神聖祈禱 ? 20 : 0);
            mplew.writeInt(mbsvh.sourceID == 主教.神聖祈禱 && mbsvh.DropRate > 0 ? 1 : 0);
            mplew.writeInt(chr.getBuffStatValueHolder(SecondaryStat.IndieAsrR, 主教.神聖祈禱) != null ? 1 : 0);
            mplew.write(mbsvh.z);
            mplew.write(1);
            mplew.writeInt(mbsvh.DropRate);
        }
        if (holderMap.containsKey(SecondaryStat.MinigameStat)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.AnimaThiefTaoistType)) {
            mplew.writeInt(chr.getSpecialStat().getHoYoungState2());
            mplew.writeInt(chr.getSpecialStat().getHoYoungState3());
        }
        if (holderMap.containsKey(SecondaryStat.AnimaThiefTaoistGauge)) {
            mplew.writeInt(chr.getSpecialStat().getHoYoungScroll());
        }
        if (holderMap.containsKey(SecondaryStat.AnimaThiefMetaphysics)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.NoviceMagicianLink)) {
            mplew.writeInt(holderMap.get(SecondaryStat.NoviceMagicianLink).z);
        }
        if (holderMap.containsKey(SecondaryStat.XenonHoloGramGraffiti)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.LefWarriorNobility)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.RevenantGauge)) {
            mplew.writeInt(holderMap.get(SecondaryStat.RevenantGauge).z);
        }
        if (holderMap.containsKey(SecondaryStat.DeathDance)) {
            mplew.writeInt(holderMap.get(SecondaryStat.DeathDance).z);
            mplew.writeInt(holderMap.get(SecondaryStat.DeathDance).x);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.ShadowShield)) {
            mplew.writeInt(holderMap.get(SecondaryStat.ShadowShield).z);
        }
        if (holderMap.containsKey(SecondaryStat.BMageAuraYellow)) {
            mplew.writeInt(738263040);
            mplew.writeInt(1);
        }
        if (holderMap.containsKey(SecondaryStat.BMageAuraDrain)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.BMageAuraBlue)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.BMageAuraDark)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.BMageAuraDebuff)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.BMageAuraUnion)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.IceAura)) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(1);
        }
        if (holderMap.containsKey(SecondaryStat.KnightsAura)) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(1);
        }
        if (holderMap.containsKey(SecondaryStat.ZeroAuraStr)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.NovaArcherIncanation)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.AranComboTempestAura)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.XenonBursterLaser)) {
            mplew.writeInt(holderMap.get(SecondaryStat.XenonBursterLaser).z);
        }
        if (holderMap.containsKey(SecondaryStat.BMageAbyssalLightning)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.KinesisLawOfGravity)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.LefMageCrystalGate)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.HolyWater)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.OrbitalExplosion)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.WeaponVarietyFinale)) {
            mplew.writeInt(holderMap.get(SecondaryStat.WeaponVarietyFinale).z);
        }
        if (holderMap.containsKey(SecondaryStat.Equinox)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.DarknessAura)) {
            mplew.writeInt(holderMap.get(SecondaryStat.DarknessAura).z);
        }
        if (holderMap.containsKey(SecondaryStat.SerpentScrew)) {
            SecondaryStatValueHolder mbsvh = holderMap.get(SecondaryStat.SerpentScrew);
            mplew.writeInt(mbsvh.NormalMobKillCount);
            mplew.writeInt(mbsvh.AttackBossCount);
        }
        if (holderMap.containsKey(SecondaryStat.EquinoxActive)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.NAThanatosDescent)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.NABrutalPang)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.Magnet)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.ATScrollPassive)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.NewFlying)) {
            mplew.writeInt(holderMap.get(SecondaryStat.NewFlying).z);
        }
        if (holderMap.containsKey(SecondaryStat.ReincarnationMission)) {
            mplew.writeInt(holderMap.get(SecondaryStat.ReincarnationMission).z);
        }
        if (holderMap.containsKey(SecondaryStat.QuiverFullBurst)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.ElementSoul)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.FlashMirage)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.SpiritGuard)) { // 隱月 - 靈魂結界
            mplew.writeInt(holderMap.get(SecondaryStat.SpiritGuard).value);
        }
        mplew.writeInt(0); // effectDelay
        mplew.write(0);
        mplew.write(1);
        mplew.write(1);
        mplew.writeBool(chr.isShowSoulEffect()); // bTemporaryOnShow || 英雄.鬥氣集中
        mplew.write(0);

        for (SecondaryStat stat : holderMap.keySet()) {
            if (SkillConstants.isMovementAffectingStat(stat)) {
                mplew.write(0);
                break;
            }
        }
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(statups.keySet().size());
        return mplew.getPacket();
    }

    public static void encodeForClient(MapleCharacter chr, MaplePacketLittleEndianWriter mplew, EnumMap<SecondaryStat, SecondaryStatValueHolder> holderMap) {
        for (Map.Entry<SecondaryStat, SecondaryStatValueHolder> entry : holderMap.entrySet()) {
            if (entry.getKey().canStack() && SkillConstants.isSpecialStackBuff(entry.getKey())) {
                mplew.writeInt(entry.getValue().value);
                mplew.writeInt(entry.getValue().sourceID);
                mplew.write(entry.getKey() == SecondaryStat.PartyBooster || entry.getKey() == SecondaryStat.SecondAtomLockOn ? 1 : 0);
                mplew.writeInt(entry.getKey() == SecondaryStat.SecondAtomLockOn ? 1 : entry.getKey() == SecondaryStat.PartyBooster ? 2 : 0);
                if (entry.getKey() == SecondaryStat.PartyBooster) {
                    mplew.write(1);
                    mplew.writeInt(2);
                    mplew.writeShort((entry.getValue().getLeftTime() / 1000));
                } else if (entry.getKey() == SecondaryStat.Curse) {
                    mplew.writeInt(chr.getLinkMobObjectID());
                    mplew.writeInt(0);
                } else if (entry.getKey() == SecondaryStat.RideVehicleExpire) {
                    mplew.writeShort(10);
                } else if (entry.getKey() == SecondaryStat.GuidedBullet) {
                    mplew.writeInt(chr.getLinkMobObjectID());
                    mplew.writeInt(0);
                } else if (entry.getKey() == SecondaryStat.DashSpeed) {
                    mplew.writeShort(10);
                } else if (entry.getKey() == SecondaryStat.DashJump) {
                    mplew.writeShort(10);
                }
            }
        }
    }

    public static void encodeIndieBuffStat(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, SecondaryStat stat) {
        int sourceID;
        final List<SecondaryStatValueHolder> holders = chr.getIndieBuffStatValueHolder(stat);
        mplew.writeInt(holders.size());
        for (SecondaryStatValueHolder holder : holders) {
            mplew.writeInt(holder.sourceID);
            mplew.writeInt(holder.value);
            mplew.writeInt(holder.startTime);
            mplew.writeInt(holder.getStartChargeTime() == 0 ? (System.currentTimeMillis() - holder.startTime) : holder.getStartChargeTime());
            mplew.writeInt(holder.localDuration == 2100000000 ? 0 : holder.localDuration);
            mplew.writeInt(0);
            int nUnkCount = 0;
            mplew.writeInt(nUnkCount);
            for (int nn = 0; nn < nUnkCount; nn++) {
                mplew.writeInt(1);
                mplew.writeInt(1);
            }
            nUnkCount = 0;
            mplew.writeInt(nUnkCount);
            for (int nn = 0; nn < nUnkCount; nn++) {
                mplew.writeInt(0);
                mplew.writeInt(0);
            }
        }
    }

    public static void writeBuffData(MaplePacketLittleEndianWriter mplew, Map<SecondaryStat, SecondaryStatValueHolder> holderMap, MapleStatEffect effect, MapleCharacter player) {
        int n2;
        if (holderMap.containsKey(SecondaryStat.SoulMP)) {
            SecondaryStatValueHolder mbsvh = holderMap.get(SecondaryStat.SoulMP);
            mplew.writeInt(1000); // maxSoulMP
            mplew.writeInt(mbsvh.sourceID);
        }
        if (holderMap.containsKey(SecondaryStat.FullSoulMP)) {
            mplew.writeInt(player.getCooldownLeftTime(player.getSoulSkillID()));
        }
        int nBuffForSpecSize = 0;
        mplew.writeShort(nBuffForSpecSize);
        for (int i = 0; i < nBuffForSpecSize; ++i) {
            mplew.writeInt(0); // dwItemID
            mplew.write(0); // bEnable
        }
        //mplew.writeInt(0);
        mplew.write(holderMap.getOrDefault(SecondaryStat.DefenseAtt, new SecondaryStatValueHolder(0, 0)).value); // DefenseAtt
        mplew.write(holderMap.getOrDefault(SecondaryStat.DefenseState, new SecondaryStatValueHolder(0, 0)).value);
        mplew.write(0);
        mplew.writeInt(effect != null && effect.getSourceId() == 通用V核心.法師通用.虛無型態 ? 0xffe300 : 0);//V.149 new
        if (holderMap.containsKey(SecondaryStat.Dice)) {
            for (int i = 0; i < 21; ++i) {
                mplew.writeInt(SkillConstants.getDiceValue(i, holderMap.get(SecondaryStat.Dice).value, effect));
            }
        }
        if (holderMap.containsKey(SecondaryStat.BlackMageCreate)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.BlackMageDestroy)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.KeyDownMoving)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.Judgement)) {
            mplew.writeInt(holderMap.get(SecondaryStat.Judgement).z);
        }
        if (holderMap.containsKey(SecondaryStat.Infinity)) {
            mplew.writeInt(holderMap.get(SecondaryStat.Infinity).localDuration);
        }
        if (holderMap.containsKey(SecondaryStat.StackBuff)) {
            mplew.write(holderMap.get(SecondaryStat.StackBuff).z);
        }
        if (holderMap.containsKey(SecondaryStat.Trinity)) {
            mplew.write(holderMap.get(SecondaryStat.Trinity).value / 5);
        }
        if (holderMap.containsKey(SecondaryStat.ElementalCharge)) {
            assert effect != null;
            final int n = holderMap.get(SecondaryStat.ElementalCharge).value / effect.getX();
            mplew.write(n);
            mplew.writeShort(effect.getY() * n);
            mplew.write(effect.getU() * n);
            mplew.write(effect.getW() * n);
        }
        if (holderMap.containsKey(SecondaryStat.LifeTidal)) {
            assert effect != null;
            n2 = 0;
            switch (holderMap.get(SecondaryStat.LifeTidal).value) {
                case 1: {
                    n2 = effect.getX();
                    break;
                }
                case 2: {
                    n2 = effect.getProp();
                    break;
                }
                case 3: {
                    n2 = player.getBuffedIntZ(SecondaryStat.LifeTidal);
                }
            }
            mplew.writeInt(n2);
        }
        if (holderMap.containsKey(SecondaryStat.AntiMagicShell)) {
            SecondaryStatValueHolder mbsvh = holderMap.get(SecondaryStat.AntiMagicShell);
            int value = mbsvh.value;
            switch (mbsvh.sourceID) {
                case 火毒.元素適應_火毒:
                    value = value < mbsvh.effect.getY() ? 1 : 0;
                    break;
                case 冰雷.元素適應_雷冰:
                    value = mbsvh.z > 0 ? 1 : 0;
                    break;
                case 主教.聖靈守護:
                    value = 1;
                    break;
                default:
                    value = value == 2 ? 1 : 0;
                    break;
            }
            mplew.write(value);
            mplew.writeInt(mbsvh.z);
        }
        if (holderMap.containsKey(SecondaryStat.Larkness)) {
            SecondaryStatValueHolder mbsvh = holderMap.get(SecondaryStat.Larkness);
            assert mbsvh.effect != null;
            int value = mbsvh.value;
            mplew.writeInt(value == 1 ? mbsvh.sourceID : 夜光.暗蝕);
            mplew.writeInt(200000000);
            mplew.writeInt(value == 1 ? 0 : 夜光.光蝕);
            mplew.writeInt(200000000);
            mplew.writeInt(player.getLarkness());
            mplew.writeInt(-1);
            mplew.writeInt(value == 2 && player.hasTruthGate() ? 1 : 0);
        }
        if (holderMap.containsKey(SecondaryStat.IgnoreTargetDEF)) {
            mplew.writeInt(player.getBuffedIntZ(SecondaryStat.IgnoreTargetDEF));
        }
        if (holderMap.containsKey(SecondaryStat.StopForceAtomInfo)) {
            assert effect != null;
            PacketHelper.write劍刃之壁(mplew, player, effect.getSourceId());
        }
        if (holderMap.containsKey(SecondaryStat.StrikerElectricUsed)) {
            mplew.writeInt(player.getBuffedIntZ(SecondaryStat.StrikerElectricUsed));
        }
        if (holderMap.containsKey(SecondaryStat.SmashStack)) {
            int value = holderMap.get(SecondaryStat.SmashStack).value;
            n2 = 0;
            if (value >= 100) {
                n2 = 1;
            } else if (value >= 300) {
                n2 = 2;
            }
            mplew.writeInt(n2);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.MobZoneState)) {
            // for {
            mplew.writeInt(holderMap.get(SecondaryStat.MobZoneState).z);
            // }
            mplew.writeInt(-1);
        }
//        if (holderMap.containsKey(SecondaryStat.NextSpecificSkillDamageUp)) {
//            mplew.writeInt(2);
//            mplew.writeInt(152120001); //暗器II
//            mplew.writeInt(400021000); //超載魔力
//        }
        if (holderMap.containsKey(SecondaryStat.Slow)) {
            mplew.write(0);
        }
        if (holderMap.containsKey(SecondaryStat.IgnoreMobpdpR)) {
            mplew.write(0);
        }
        if (holderMap.containsKey(SecondaryStat.BdR)) {
            mplew.write(1);
        }
        if (holderMap.containsKey(SecondaryStat.DropRIncrease)) {
            mplew.writeInt(0);
            mplew.write(0);
        }
        if (holderMap.containsKey(SecondaryStat.PoseType)) {
            mplew.write(0);
            mplew.write(0);
        }
        if (holderMap.containsKey(SecondaryStat.Beholder)) {
            mplew.writeInt(player.getSkillLevel(黑騎士.追隨者支配) > 0 ? 黑騎士.追隨者支配 : 黑騎士.追隨者);
        }
        if (holderMap.containsKey(SecondaryStat.CrossOverChain)) {
            mplew.writeInt(holderMap.get(SecondaryStat.CrossOverChain).z);
        }
        if (holderMap.containsKey(SecondaryStat.ImmuneBarrier)) {
            mplew.writeInt(holderMap.get(SecondaryStat.ImmuneBarrier).z);
        }
        if (holderMap.containsKey(SecondaryStat.Stance)) {
            mplew.writeInt(holderMap.get(SecondaryStat.Stance).z);
        }
        if (holderMap.containsKey(SecondaryStat.SharpEyes)) {
            mplew.writeInt(holderMap.get(SecondaryStat.SharpEyes).z);
        }
        if (holderMap.containsKey(SecondaryStat.AdvancedBless)) {
            SecondaryStatValueHolder mbsvh = holderMap.get(SecondaryStat.AdvancedBless);
            mplew.writeInt(mbsvh.BDR);
            mplew.writeInt(mbsvh.z);
        }
        if (holderMap.containsKey(SecondaryStat.UsefulAdvancedBless)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.Bless)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.DotHealHPPerSecond)) {
            mplew.writeInt(holderMap.get(SecondaryStat.DotHealHPPerSecond).localDuration);
        }
        if (holderMap.containsKey(SecondaryStat.DotHealMPPerSecond)) {
            mplew.writeInt(holderMap.get(SecondaryStat.DotHealMPPerSecond).localDuration);
        }
        if (holderMap.containsKey(SecondaryStat.FlameDischarge)) {
            mplew.writeInt(holderMap.get(SecondaryStat.FlameDischarge).value); // b
            mplew.writeInt(holderMap.get(SecondaryStat.FlameDischarge).value); // c
            mplew.writeInt(holderMap.get(SecondaryStat.FlameDischarge).value); // y
        }
        if (holderMap.containsKey(SecondaryStat.KnockBack)) {
            mplew.writeInt(holderMap.get(SecondaryStat.KnockBack).value);
        }
        if (holderMap.containsKey(SecondaryStat.ShieldAttack)) {//靈魂吸取專家？
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.SSFShootingAttack)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.PinkbeanAttackBuff)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.RoyalGuardState)) {
            mplew.writeInt(holderMap.get(SecondaryStat.IndiePAD).value);
            mplew.writeInt(holderMap.get(SecondaryStat.RoyalGuardState).value);
        }
        if (holderMap.containsKey(SecondaryStat.MichaelSoulLink)) {
            SecondaryStatValueHolder mbsvh = holderMap.get(SecondaryStat.MichaelSoulLink);
            mplew.writeInt(mbsvh.fromChrID == player.getId() ? mbsvh.value : 0);
            mplew.writeBool(player.getParty() == null || player.getParty().getMembers().size() == 1);
            mplew.writeInt(mbsvh.fromChrID);
            mplew.writeInt(mbsvh.fromChrID != player.getId() ? mbsvh.effect.getLevel() : 0);
        }
        // SPAWN
        if (holderMap.containsKey(SecondaryStat.AdrenalinBoost)) {
            assert effect != null;
            mplew.write(effect.getSourceId() == 狂狼勇士.鬥氣爆發 ? 1 : 0);
        }
        if (holderMap.containsKey(SecondaryStat.RWCylinder)) {
            mplew.write(player.getBullet());
            mplew.writeShort(player.getCylinder());
            mplew.write(0);//V.160 new
        }
        if (holderMap.containsKey(SecondaryStat.HitStackDamR)) {
            mplew.writeInt(holderMap.get(SecondaryStat.HitStackDamR).z);
        }
        if (holderMap.containsKey(SecondaryStat.RWMagnumBlow)) {
            mplew.writeShort(0);
            mplew.write(0);
        }
        if (holderMap.containsKey(SecondaryStat.BladeStance)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.DarkSight)) {
            mplew.writeInt(660);
            mplew.writeInt(660);
        }
        if (holderMap.containsKey(SecondaryStat.Stigma)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.CriticalGrowing)) {
            mplew.writeInt(holderMap.get(SecondaryStat.CriticalGrowing).z);
        }
        if (holderMap.containsKey(SecondaryStat.PickPocket)) {
            mplew.writeInt(holderMap.get(SecondaryStat.PickPocket).z);
        }
        if (holderMap.containsKey(SecondaryStat.PairingUser)) {
            mplew.writeShort(holderMap.get(SecondaryStat.PairingUser).z);
        }
        if (holderMap.containsKey(SecondaryStat.Frenzy)) {
            mplew.writeShort(holderMap.get(SecondaryStat.Frenzy).z);
        }
        if (holderMap.containsKey(SecondaryStat.ShadowSpear)) {
            mplew.writeShort(35);
        }
        if (holderMap.containsKey(SecondaryStat.Michael_RhoAias)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(SecondaryStat.VampDeath)) {
            mplew.writeInt(3);
        }
        if (holderMap.containsKey(SecondaryStat.HolyMagicShell)) {
            mplew.writeInt(holderMap.get(SecondaryStat.HolyMagicShell).z);
        }
    }

    /*
     * 其他玩家看到別人取消BUFF狀態
     */
    public static byte[] cancelForeignBuff(MapleCharacter chr, List<SecondaryStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserTemporaryStatReset.getValue());
        mplew.writeInt(chr.getId());
        writeBuffMask(mplew, statups);
        for (SecondaryStat stat : statups) {
            if (stat.canStack() && !SkillConstants.isSpecialStackBuff(stat)) {
                encodeIndieBuffStat(mplew, chr, stat);
            }
        }
        if (statups.contains(SecondaryStat.PoseType)) {
            mplew.write(1);
        }
        if (statups.contains(SecondaryStat.BattleSurvivalDefence)) {
            mplew.write(1);
        }

        for (SecondaryStat statup : statups) {
            if (SkillConstants.isMovementAffectingStat(statup)) {
                mplew.write(0);
                break;
            }
        }
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] giveForeignBuff(MapleCharacter player, Map<SecondaryStat, Integer> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserTemporaryStatSet.getValue());
        mplew.writeInt(player.getId());
        writeForeignBuff(mplew, player, statups, false);

        mplew.write(0);

        return mplew.getPacket();
    }

    public static void writeForeignBuff(MaplePacketLittleEndianWriter mplew, MapleCharacter player, Map<SecondaryStat, Integer> statups, boolean isChrinfo) {
        int sourceid;
        int n3;
        writeBuffMask(mplew, statups.keySet());
        if (statups.containsKey(SecondaryStat.Speed)) {
            mplew.write(player.getBuffedIntValue(SecondaryStat.Speed));
        }
        if (statups.containsKey(SecondaryStat.ComboCounter)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ComboCounter));
        }
        if (statups.containsKey(SecondaryStat.BlessedHammer)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BlessedHammer));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BlessedHammer));
        }
        if (statups.containsKey(SecondaryStat.WeaponCharge)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.WeaponCharge));
            mplew.writeInt(player.getBuffSource(SecondaryStat.WeaponCharge));
        }
        if (statups.containsKey(SecondaryStat.ElementalCharge)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ElementalCharge));
        }
        if (statups.containsKey(SecondaryStat.Stun)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Stun);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.Shock)) {
            mplew.write(1);
        }
        if (statups.containsKey(SecondaryStat.Darkness)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Darkness);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.Seal)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Seal);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.Weakness)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Weakness);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.WeaknessMdamage)) {
            mplew.writeShort(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.Curse)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Curse);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.Slow)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Slow);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.PvPRaceEffect)) {
            mplew.writeShort(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.TimeBomb)) {
            mplew.writeShort(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.Team)) {
            mplew.write(0);
        }
        if (statups.containsKey(SecondaryStat.DisOrder)) {
            mplew.writeShort(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.Thread)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Thread));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Thread));
        }
        if (statups.containsKey(SecondaryStat.Poison)) {
            mplew.writeShort(1);
        }
        if (statups.containsKey(SecondaryStat.Poison)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Poison);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.ShadowPartner)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ShadowPartner));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ShadowPartner));
        }
        if (statups.containsKey(SecondaryStat.DarkSight)) {
            mplew.writeInt(1000000);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.SoulArrow)) {
        }
        if (statups.containsKey(SecondaryStat.Morph)) {
            mplew.writeShort(player.getEffectForBuffStat(SecondaryStat.Morph).getMorph(player));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Morph));
        }
        if (statups.containsKey(SecondaryStat.Ghost)) {
            mplew.writeShort(0);
        }
        if (statups.containsKey(SecondaryStat.Attract)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Attract);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.Magnet)) {
            mplew.writeShort(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.MagnetArea)) {
            mplew.writeShort(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.NoBulletConsume)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.NoBulletConsume));
        }
        if (statups.containsKey(SecondaryStat.BanMap)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.BanMap);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.Barrier)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Barrier));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Barrier));
        }
        if (statups.containsKey(SecondaryStat.DojangShield)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DojangShield));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DojangShield));
        }
        if (statups.containsKey(SecondaryStat.ReverseInput)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.ReverseInput);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.RespectPImmune)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.RespectPImmune));
        }
        if (statups.containsKey(SecondaryStat.RespectMImmune)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.RespectMImmune));
        }
        if (statups.containsKey(SecondaryStat.DefenseAtt)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.DefenseState)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.DefenseState));
        }
        if (statups.containsKey(SecondaryStat.DojangBerserk)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DojangBerserk));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DojangBerserk));
        }
        if (statups.containsKey(SecondaryStat.DojangInvincible)) {
            // empty if block
        }
        if (statups.containsKey(SecondaryStat.RepeatEffect)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.RepeatEffect));
            mplew.writeInt(player.getBuffSource(SecondaryStat.RepeatEffect));
        }
        if (statups.containsKey(SecondaryStat.RepeatEffect2)) {
            mplew.writeShort(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.StopPortion)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.StopPortion);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.StopMotion)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.StopMotion);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.Fear)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Fear);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.MagicShield)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.Flying)) {
            // empty if block
        }
        if (statups.containsKey(SecondaryStat.Frozen)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Frozen);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.Frozen2)) {
            mplew.writeShort(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.Web)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Web);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.DrawBack)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DrawBack));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DrawBack));
        }
        if (statups.containsKey(SecondaryStat.FinalCut)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.FinalCut));
            mplew.writeInt(player.getBuffSource(SecondaryStat.FinalCut));
        }
        if (statups.containsKey(SecondaryStat.Sneak)) {
            // empty if block
        }
        if (statups.containsKey(SecondaryStat.BeastForm)) {
            // empty if block
        }
        if (statups.containsKey(SecondaryStat.Mechanic)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Mechanic));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Mechanic));
        }
        if (statups.containsKey(SecondaryStat.BlessingArmorIncPAD)) {
            // empty if block
        }
        if (statups.containsKey(SecondaryStat.Inflation)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Inflation));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Inflation));
        }
        if (statups.containsKey(SecondaryStat.Explosion)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Explosion));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Explosion));
        }
        if (statups.containsKey(SecondaryStat.DarkTornado)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DarkTornado));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DarkTornado));
        }
        if (statups.containsKey(SecondaryStat.AmplifyDamage)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AmplifyDamage));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AmplifyDamage));
        }
        if (statups.containsKey(SecondaryStat.HideAttack)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.HideAttack));
            mplew.writeInt(player.getBuffSource(SecondaryStat.HideAttack));
        }
        if (statups.containsKey(SecondaryStat.HolyMagicShell)) {
            // empty if block
        }
        if (statups.containsKey(SecondaryStat.DevilishPower)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DevilishPower));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DevilishPower));
        }
        if (statups.containsKey(SecondaryStat.SpiritLink)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SpiritLink));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SpiritLink));
        }
        if (statups.containsKey(SecondaryStat.Event)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Event));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Event));
        }
        if (statups.containsKey(SecondaryStat.Event2)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Event2));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Event2));
        }
        if (statups.containsKey(SecondaryStat.DeathMark)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DeathMark));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DeathMark));
        }
        if (statups.containsKey(SecondaryStat.PainMark)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.PainMark));
            mplew.writeInt(player.getBuffSource(SecondaryStat.PainMark));
        }
        if (statups.containsKey(SecondaryStat.Lapidification)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.Lapidification);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.VampDeath)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.VampDeath));
            mplew.writeInt(player.getBuffSource(SecondaryStat.VampDeath));
        }
        if (statups.containsKey(SecondaryStat.VampDeathSummon)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.VampDeathSummon));
            mplew.writeInt(player.getBuffSource(SecondaryStat.VampDeathSummon));
        }
        if (statups.containsKey(SecondaryStat.VenomSnake)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.VenomSnake));
            mplew.writeInt(player.getBuffSource(SecondaryStat.VenomSnake));
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.PyramidEffect)) {
            mplew.writeInt(-1);
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.PinkbeanRollingGrade)) {
            mplew.write(0);
        }
        if (statups.containsKey(SecondaryStat.IgnoreTargetDEF)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.IgnoreTargetDEF));
            mplew.writeInt(player.getBuffSource(SecondaryStat.IgnoreTargetDEF));
        }
        if (statups.containsKey(SecondaryStat.StrikerElectricUsed)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.StrikerElectricUsed));
            mplew.writeInt(player.getBuffSource(SecondaryStat.StrikerElectricUsed));
        }
        if (statups.containsKey(SecondaryStat.Invisible)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Invisible));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Invisible));
        }
        if (statups.containsKey(SecondaryStat.Judgement)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Judgement));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Judgement));
        }
        if (statups.containsKey(SecondaryStat.KeyDownAreaMoving)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.KeyDownAreaMoving));
            mplew.writeInt(player.getBuffSource(SecondaryStat.KeyDownAreaMoving));
        }
        if (statups.containsKey(SecondaryStat.StackBuff)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.StackBuff));
        }
        if (statups.containsKey(SecondaryStat.Larkness)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Larkness));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Larkness));
        }
        if (statups.containsKey(SecondaryStat.ReshuffleSwitch)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ReshuffleSwitch));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ReshuffleSwitch));
        }
        if (statups.containsKey(SecondaryStat.SpecialAction)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SpecialAction));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SpecialAction));
        }
        if (statups.containsKey(SecondaryStat.StopForceAtomInfo)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.StopForceAtomInfo));
            mplew.writeInt(player.getBuffSource(SecondaryStat.StopForceAtomInfo));
        }
        if (statups.containsKey(SecondaryStat.SoulGazeCriDamR)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SoulGazeCriDamR));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SoulGazeCriDamR));
        }
        if (statups.containsKey(SecondaryStat.PowerTransferGauge)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.PowerTransferGauge));
            mplew.writeInt(player.getBuffSource(SecondaryStat.PowerTransferGauge));
        }
        if (statups.containsKey(SecondaryStat.FifthAdvWarriorShield)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.FifthAdvWarriorShield));
            mplew.writeInt(player.getBuffSource(SecondaryStat.FifthAdvWarriorShield));
        }
        if (statups.containsKey(SecondaryStat.AffinitySlug)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AffinitySlug));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AffinitySlug));
        }
        if (statups.containsKey(SecondaryStat.SoulExalt)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SoulExalt));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SoulExalt));
        }
        if (statups.containsKey(SecondaryStat.HiddenPieceOn)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.HiddenPieceOn));
            mplew.writeInt(player.getBuffSource(SecondaryStat.HiddenPieceOn));
        }
        if (statups.containsKey(SecondaryStat.SmashStack)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SmashStack));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SmashStack));
        }
        if (statups.containsKey(SecondaryStat.MobZoneState)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.MobZoneState));
            mplew.writeInt(player.getBuffSource(SecondaryStat.MobZoneState));
        }
        if (statups.containsKey(SecondaryStat.GiveMeHeal)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.GiveMeHeal));
            mplew.writeInt(player.getBuffSource(SecondaryStat.GiveMeHeal));
        }
        if (statups.containsKey(SecondaryStat.TouchMe)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.TouchMe));
            mplew.writeInt(player.getBuffSource(SecondaryStat.TouchMe));
        }
        if (statups.containsKey(SecondaryStat.Contagion)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Contagion));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Contagion));
        }
        if (statups.containsKey(SecondaryStat.Contagion)) {
            mplew.writeInt(0); // time
        }
        if (statups.containsKey(SecondaryStat.ComboUnlimited)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ComboUnlimited));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ComboUnlimited));
        }
        if (statups.containsKey(SecondaryStat.IgnoreAllCounter)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.IgnoreAllCounter));
            mplew.writeInt(player.getBuffSource(SecondaryStat.IgnoreAllCounter));
        }
        if (statups.containsKey(SecondaryStat.IgnorePImmune)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.IgnorePImmune));
            mplew.writeInt(player.getBuffSource(SecondaryStat.IgnorePImmune));
        }
        if (statups.containsKey(SecondaryStat.IgnoreAllImmune)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.IgnoreAllImmune));
            mplew.writeInt(player.getBuffSource(SecondaryStat.IgnoreAllImmune));
        }
        if (statups.containsKey(SecondaryStat.IgnoreAllAbout)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.IgnoreAllAbout));
            mplew.writeInt(player.getBuffSource(SecondaryStat.IgnoreAllAbout));
        }
        if (statups.containsKey(SecondaryStat.FinalJudgement)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.FinalJudgement));
            mplew.writeInt(player.getBuffSource(SecondaryStat.FinalJudgement));
        }
        if (statups.containsKey(SecondaryStat.FireAura)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.FireAura));
            mplew.writeInt(player.getBuffSource(SecondaryStat.FireAura));
        }
        if (statups.containsKey(SecondaryStat.VengeanceOfAngel)) {
            // empty if block
        }
        if (statups.containsKey(SecondaryStat.HeavensDoor)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.HeavensDoor));
            mplew.writeInt(player.getBuffSource(SecondaryStat.HeavensDoor));
        }
        if (statups.containsKey(SecondaryStat.DamAbsorbShield)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DamAbsorbShield));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DamAbsorbShield));
        }
        if (statups.containsKey(SecondaryStat.AntiMagicShell)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AntiMagicShell));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AntiMagicShell));
        }
        if (statups.containsKey(SecondaryStat.NotDamaged)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NotDamaged));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NotDamaged));
        }
        if (statups.containsKey(SecondaryStat.BleedingToxin)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BleedingToxin));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BleedingToxin));
        }
        if (statups.containsKey(SecondaryStat.WindBreakerFinal)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.WindBreakerFinal));
            mplew.writeInt(player.getBuffSource(SecondaryStat.WindBreakerFinal));
        }
        if (statups.containsKey(SecondaryStat.KarmaBlade)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.KarmaBlade));
            mplew.writeInt(player.getBuffSource(SecondaryStat.KarmaBlade));
        }
        if (statups.containsKey(SecondaryStat.IgnoreMobDamR)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.IgnoreMobDamR));
            mplew.writeInt(player.getBuffSource(SecondaryStat.IgnoreMobDamR));
        }
        if (statups.containsKey(SecondaryStat.Asura)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Asura));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Asura));
        }
        if (statups.containsKey(SecondaryStat.MegaSmasher)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.MegaSmasher));
            mplew.writeInt(player.getBuffSource(SecondaryStat.MegaSmasher));
        }
        if (statups.containsKey(SecondaryStat.MegaSmasher)) {
            mplew.writeInt(0); // time
        }
        if (statups.containsKey(SecondaryStat.UnityOfPower)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.UnityOfPower));
            mplew.writeInt(player.getBuffSource(SecondaryStat.UnityOfPower));
        }
        if (statups.containsKey(SecondaryStat.Stimulate)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Stimulate));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Stimulate));
        }
        if (statups.containsKey(SecondaryStat.ReturnTeleport)) {
            mplew.write(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.ReturnTeleport);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.CapDebuff)) {
            mplew.writeShort(1);
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.CapDebuff);
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getSourceId());
            mplew.writeShort(mbsvh == null ? 0 : mbsvh.effect.getLevel());
        }
        if (statups.containsKey(SecondaryStat.OverloadCount)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.OverloadCount));
            mplew.writeInt(player.getBuffSource(SecondaryStat.OverloadCount));
        }
        if (statups.containsKey(SecondaryStat.FireBomb)) {
            mplew.write(player.getBuffedIntValue(SecondaryStat.FireBomb));
            mplew.writeInt(player.getBuffSource(SecondaryStat.FireBomb));
        }
        if (statups.containsKey(SecondaryStat.SurplusSupply)) {
            mplew.write(player.getBuffedIntValue(SecondaryStat.SurplusSupply));
        }
        if (statups.containsKey(SecondaryStat.NewFlying)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NewFlying));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NewFlying));
        }
        if (statups.containsKey(SecondaryStat.NaviFlying)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NaviFlying));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NaviFlying));
        }
        if (statups.containsKey(SecondaryStat.AmaranthGenerator)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AmaranthGenerator));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AmaranthGenerator));
        }
        if (statups.containsKey(SecondaryStat.CygnusElementSkill)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.CygnusElementSkill));
            mplew.writeInt(player.getBuffSource(SecondaryStat.CygnusElementSkill));
        }
        if (statups.containsKey(SecondaryStat.StrikerHyperElectric)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.StrikerHyperElectric));
            mplew.writeInt(player.getBuffSource(SecondaryStat.StrikerHyperElectric));
        }
        if (statups.containsKey(SecondaryStat.EventPointAbsorb)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.EventPointAbsorb));
            mplew.writeInt(player.getBuffSource(SecondaryStat.EventPointAbsorb));
        }
        if (statups.containsKey(SecondaryStat.EventAssemble)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.EventAssemble));
            mplew.writeInt(player.getBuffSource(SecondaryStat.EventAssemble));
        }
        if (statups.containsKey(SecondaryStat.Translucence)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Translucence));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Translucence));
        }
        if (statups.containsKey(SecondaryStat.PoseType)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.PoseType));
            mplew.writeInt(player.getBuffSource(SecondaryStat.PoseType));
        }
        if (statups.containsKey(SecondaryStat.CosmicForge)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.CosmicForge));
            mplew.writeInt(player.getBuffSource(SecondaryStat.CosmicForge));
        }
        if (statups.containsKey(SecondaryStat.ElementSoul)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ElementSoul));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ElementSoul));
        }
        //if (statups.containsKey(SecondaryStat.GlimmeringTime)) {
        //    mplew.writeShort(player.getBuffedIntValue(SecondaryStat.GlimmeringTime));
        //    mplew.writeInt(player.getBuffSource(SecondaryStat.GlimmeringTime));
        //}
        if (statups.containsKey(SecondaryStat.Reincarnation)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Reincarnation));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Reincarnation));
        }
        if (statups.containsKey(SecondaryStat.Beholder)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Beholder));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Beholder));
        }
        if (statups.containsKey(SecondaryStat.QuiverCatridge)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.QuiverCatridge));
            mplew.writeInt(player.getBuffSource(SecondaryStat.QuiverCatridge));
        }
        if (statups.containsKey(SecondaryStat.UserControlMob)) {
            // empty if block
        }
        if (statups.containsKey(SecondaryStat.ImmuneBarrier)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.ImmuneBarrier));
        }
        if (statups.containsKey(SecondaryStat.ImmuneBarrier)) {
            mplew.writeInt(player.getBuffedIntZ(SecondaryStat.ImmuneBarrier));
        }
        if (statups.containsKey(SecondaryStat.GiantBossDeathCnt)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.GiantBossDeathCnt));
            mplew.writeInt(player.getBuffSource(SecondaryStat.GiantBossDeathCnt));
        }
//        if (statups.containsKey(SecondaryStat.ShamanMode)) {
//            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ShamanMode));
//            mplew.writeInt(player.getBuffSource(SecondaryStat.ShamanMode));
//        }
//        if (statups.containsKey(SecondaryStat.Chachacha)) {
//            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Chachacha));
//            mplew.writeInt(player.getBuffSource(SecondaryStat.Chachacha));
//        }
        if (statups.containsKey(SecondaryStat.Fever)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Fever));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Fever));
        }
        if (statups.containsKey(SecondaryStat.PvPFlag)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.PvPFlag));
            mplew.writeInt(player.getBuffSource(SecondaryStat.PvPFlag));
        }
        if (statups.containsKey(SecondaryStat.FullSoulMP)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.FullSoulMP)); // 不確定
            mplew.writeInt(player.getSoulSkillID());
            mplew.writeInt(0); // time
        }
        if (statups.containsKey(SecondaryStat.AntiMagicShell)) {
            mplew.writeBool(player.getBuffSource(SecondaryStat.AntiMagicShell) == 主教.聖靈守護);
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.AntiMagicShell));
        }
        if (statups.containsKey(SecondaryStat.Dance)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.SpiritGuard)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.SpiritGuard));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SpiritGuard));
        }
        if (statups.containsKey(SecondaryStat.MastemaGuard)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.MastemaGuard));
            mplew.writeInt(player.getBuffSource(SecondaryStat.MastemaGuard));
        }
        if (statups.containsKey(SecondaryStat.ComboTempest)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ComboTempest));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ComboTempest));
        }
        if (statups.containsKey(SecondaryStat.HalfstatByDebuff)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.HalfstatByDebuff));
            mplew.writeInt(player.getBuffSource(SecondaryStat.HalfstatByDebuff));
        }
        if (statups.containsKey(SecondaryStat.ComplusionSlant)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ComplusionSlant));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ComplusionSlant));
        }
        if (statups.containsKey(SecondaryStat.JaguarSummoned)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.JaguarSummoned));
            mplew.writeInt(player.getBuffSource(SecondaryStat.JaguarSummoned));
        }
        if (statups.containsKey(SecondaryStat.BombTime)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BombTime));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BombTime));
        }
        if (statups.containsKey(SecondaryStat.TransformOverMan)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.TransformOverMan));
            mplew.writeInt(player.getBuffSource(SecondaryStat.TransformOverMan));
        }
        if (statups.containsKey(SecondaryStat.EnergyBust)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.EnergyBust));
            mplew.writeInt(player.getBuffSource(SecondaryStat.EnergyBust));
        }
        if (statups.containsKey(SecondaryStat.LightningUnion)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.LightningUnion));
            mplew.writeInt(player.getBuffSource(SecondaryStat.LightningUnion));
        }
        if (statups.containsKey(SecondaryStat.BulletParty)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BulletParty));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BulletParty));
        }
        if (statups.containsKey(SecondaryStat.LoadedDice)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.LoadedDice));
            mplew.writeInt(player.getBuffSource(SecondaryStat.LoadedDice));
        }
        if (statups.containsKey(SecondaryStat.BishopPray)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BishopPray));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BishopPray));
        }
        if (statups.containsKey(SecondaryStat.DarkLighting)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DarkLighting));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DarkLighting));
        }
        if (statups.containsKey(SecondaryStat.AttackCountX)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AttackCountX));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AttackCountX));
        }
        if (statups.containsKey(SecondaryStat.FireBarrier)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.FireBarrier));
            mplew.writeInt(player.getBuffSource(SecondaryStat.FireBarrier));
        }
        if (statups.containsKey(SecondaryStat.KeyDownMoving)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.KeyDownMoving));
            mplew.writeInt(player.getBuffSource(SecondaryStat.KeyDownMoving));
        }
        if (statups.containsKey(SecondaryStat.MichaelSoulLink)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.MichaelSoulLink));
            mplew.writeInt(player.getBuffSource(SecondaryStat.MichaelSoulLink));
        }
        if (statups.containsKey(SecondaryStat.KinesisPsychicEnergeShield)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.KinesisPsychicEnergeShield));
            mplew.writeInt(player.getBuffSource(SecondaryStat.KinesisPsychicEnergeShield));
        }
        if (statups.containsKey(SecondaryStat.BladeStance)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BladeStance));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BladeStance));
        }
        if (statups.containsKey(SecondaryStat.BladeStance)) {
            mplew.writeInt(player.getBuffSource(SecondaryStat.BladeStance));
        }
        if (statups.containsKey(SecondaryStat.Fever)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Fever));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Fever));
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.AdrenalinBoost)) {
            mplew.writeInt(player.getBuffSource(SecondaryStat.AdrenalinBoost));
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.RWBarrier)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.RWVulkanPunch)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.RWMagnumBlow)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.SerpentScrew)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.SerpentScrew));
        }
        if (statups.containsKey(SecondaryStat.BossAggro)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BossAggro));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BossAggro));
        }
        if (statups.containsKey(SecondaryStat.Cosmos)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.Cosmos));
        }
        if (statups.containsKey(SecondaryStat.GuidedArrow)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.GuidedArrow));
            mplew.writeInt(player.getBuffSource(SecondaryStat.GuidedArrow));
        }
        if (statups.containsKey(SecondaryStat.StraightForceAtomTargets)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.StraightForceAtomTargets));
            mplew.writeInt(player.getBuffSource(SecondaryStat.StraightForceAtomTargets));
        }
        if (statups.containsKey(SecondaryStat.LefBuffMastery)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.LefBuffMastery));
            mplew.writeInt(player.getBuffSource(SecondaryStat.LefBuffMastery));
        }
        if (statups.containsKey(SecondaryStat.TempSecondaryStat)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.TempSecondaryStat));
            mplew.writeInt(player.getBuffSource(SecondaryStat.TempSecondaryStat));
        }
        if (statups.containsKey(SecondaryStat.CoalitionSupportSoldierStorm)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.CoalitionSupportSoldierStorm));
            mplew.writeInt(player.getBuffSource(SecondaryStat.CoalitionSupportSoldierStorm));
        }
        if (statups.containsKey(SecondaryStat.Stigma)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Stigma));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Stigma));
        }
        if (statups.containsKey(SecondaryStat.PairingUser)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.PairingUser));
            mplew.writeInt(player.getBuffSource(SecondaryStat.PairingUser));
        }
        if (statups.containsKey(SecondaryStat.Michael_RhoAias)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.Michael_RhoAias));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Michael_RhoAias));
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.Kinesis_DustTornado)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.Kinesis_DustTornado));
        }
        if (statups.containsKey(SecondaryStat.MahaInstall)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.MahaInstall));
        }
        if (statups.containsKey(SecondaryStat.Wizard_OverloadMana)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.Wizard_OverloadMana));
        }
        if (statups.containsKey(SecondaryStat.CursorSniping)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.CursorSniping));
            mplew.writeInt(player.getBuffSource(SecondaryStat.CursorSniping));
        }
        if (statups.containsKey(SecondaryStat.OutSide)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.OutSide));
            mplew.writeInt(player.getBuffSource(SecondaryStat.OutSide));
        }
        if (statups.containsKey(SecondaryStat.FifthSpotLight)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.FifthSpotLight));
            mplew.writeInt(player.getBuffSource(SecondaryStat.FifthSpotLight));
        }
        if (statups.containsKey(SecondaryStat.OverloadMode)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.OverloadMode));
            mplew.writeInt(player.getBuffSource(SecondaryStat.OverloadMode));
        }
        if (statups.containsKey(SecondaryStat.FreudBlessing)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.FreudBlessing));
            mplew.writeInt(player.getBuffSource(SecondaryStat.FreudBlessing));
        }
        if (statups.containsKey(SecondaryStat.BlessedHammerActive)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BlessedHammerActive));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BlessedHammerActive));
        }
        if (statups.containsKey(SecondaryStat.ConvertAD)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ConvertAD));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ConvertAD));
        }
        if (statups.containsKey(SecondaryStat.EtherealForm)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.EtherealForm));
            mplew.writeInt(player.getBuffSource(SecondaryStat.EtherealForm));
        }
        if (statups.containsKey(SecondaryStat.ReadyToDie)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ReadyToDie));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ReadyToDie));
        }
        if (statups.containsKey(SecondaryStat.Oblivion)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Oblivion));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Oblivion));
        }
        if (statups.containsKey(SecondaryStat.Cr2CriDamR)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Cr2CriDamR));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Cr2CriDamR));
        }
        if (statups.containsKey(SecondaryStat.BlackMageCreate)) {
            mplew.writeShort(0);
            mplew.writeInt(10);
        }
        if (statups.containsKey(SecondaryStat.BlackMageDestroy)) {
            mplew.writeShort(0);
            mplew.writeInt(15);
        }
        if (statups.containsKey(SecondaryStat.BlackMageMonochrome)) {
            mplew.writeInt(80002623);
            mplew.writeInt(80002623);
        }
        if (statups.containsKey(SecondaryStat.HitStackDamR)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.HitStackDamR));
            mplew.writeInt(player.getBuffSource(SecondaryStat.HitStackDamR));
        }
        if (statups.containsKey(SecondaryStat.LefGloryWing)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.LefGloryWing));
            mplew.writeInt(player.getBuffSource(SecondaryStat.LefGloryWing));
        }
        if (statups.containsKey(SecondaryStat.BuffControlDebuff)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BuffControlDebuff));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BuffControlDebuff));
        }
        if (statups.containsKey(SecondaryStat.BuffControlDebuff)) {
            mplew.writeInt(0); // time
        }
        if (statups.containsKey(SecondaryStat.DispersionDamage)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.DispersionDamage));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DispersionDamage));
        }
        if (statups.containsKey(SecondaryStat.HarmonyLink)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.HarmonyLink));
            mplew.writeInt(player.getBuffSource(SecondaryStat.HarmonyLink));
        }
        if (statups.containsKey(SecondaryStat.LefFastCharge)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.LefFastCharge));
            mplew.writeInt(player.getBuffSource(SecondaryStat.LefFastCharge));
        }
        if (statups.containsKey(SecondaryStat.SpecterMode)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SpecterMode));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SpecterMode));
        }
        if (statups.containsKey(SecondaryStat.ComingDeath)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ComingDeath));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ComingDeath));
        }
        if (statups.containsKey(SecondaryStat.GreatOldAbyss)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.GreatOldAbyss));
            mplew.writeInt(player.getBuffSource(SecondaryStat.GreatOldAbyss));
        }
        if (statups.containsKey(SecondaryStat.SixthPhoenix)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SixthPhoenix));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SixthPhoenix));
        }
        if (statups.containsKey(SecondaryStat.BossWill_Infection)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BossWill_Infection));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BossWill_Infection));
        }
        if (statups.containsKey(SecondaryStat.DispersionDamage)) {
            mplew.writeInt(0); // time
        }
        if (statups.containsKey(SecondaryStat.MichaelSwordOfLight)) {
        }
        if (statups.containsKey(SecondaryStat.GrandCross)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.GrandCross));
            mplew.writeInt(player.getBuffSource(SecondaryStat.GrandCross));
        }
        if (statups.containsKey(SecondaryStat.SiphonVitalityBarrier)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SiphonVitalityBarrier));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SiphonVitalityBarrier));
        }
        if (statups.containsKey(SecondaryStat.BattlePvP_Wongki_FlyingCharge)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BattlePvP_Wongki_FlyingCharge));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BattlePvP_Wongki_FlyingCharge));
        }
        if (statups.containsKey(SecondaryStat.BattlePvP_Wongki_AwesomeFairy)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BattlePvP_Wongki_AwesomeFairy));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BattlePvP_Wongki_AwesomeFairy));
        }
        if (statups.containsKey(SecondaryStat.BattleSurvivalDefence)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BattleSurvivalDefence));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BattleSurvivalDefence));
        }
        if (statups.containsKey(SecondaryStat.BattleSurvivalInvincible)) {
        }
        if (statups.containsKey(SecondaryStat.EventPvPDefence)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.EventPvPDefence));
            mplew.writeInt(player.getBuffSource(SecondaryStat.EventPvPDefence));
        }
        if (statups.containsKey(SecondaryStat.EventPvPInvincible)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.EventPvPInvincible));
            mplew.writeInt(player.getBuffSource(SecondaryStat.EventPvPInvincible));
        }
        if (statups.containsKey(SecondaryStat.EventSoccerBall)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.EventSoccerBall));
            mplew.writeInt(player.getBuffSource(SecondaryStat.EventSoccerBall));
        }
        if (statups.containsKey(SecondaryStat.PinkbeanMatryoshka)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.PinkbeanMatryoshka));
            mplew.writeInt(player.getBuffSource(SecondaryStat.PinkbeanMatryoshka));
        }
        if (statups.containsKey(SecondaryStat.MinigameStat)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.MinigameStat));
            mplew.writeInt(player.getBuffSource(SecondaryStat.MinigameStat));
        }
        if (statups.containsKey(SecondaryStat.AnimaThiefFifthCloneAttack)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AnimaThiefFifthCloneAttack));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AnimaThiefFifthCloneAttack));
        }
        if (statups.containsKey(SecondaryStat.BlackMageWeaponDestruction)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BlackMageWeaponDestruction));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BlackMageWeaponDestruction));
        }
        if (statups.containsKey(SecondaryStat.BlackMageWeaponCreation)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BlackMageWeaponCreation));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BlackMageWeaponCreation));
        }
        if (statups.containsKey(SecondaryStat.LibraryMissionGuard)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.LibraryMissionGuard));
            mplew.writeInt(player.getBuffSource(SecondaryStat.LibraryMissionGuard));
        }
        if (statups.containsKey(SecondaryStat.XenonHoloGramGraffiti)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.XenonHoloGramGraffiti));
            mplew.writeInt(player.getBuffSource(SecondaryStat.XenonHoloGramGraffiti));
        }
        if (statups.containsKey(SecondaryStat.QuiverFullBurst)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.QuiverFullBurst));
            mplew.writeInt(player.getBuffSource(SecondaryStat.QuiverFullBurst));
        }
        if (statups.containsKey(SecondaryStat.LefWarriorNobility)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.LefWarriorNobility));
            mplew.writeInt(player.getBuffSource(SecondaryStat.LefWarriorNobility));
        }
        if (statups.containsKey(SecondaryStat.RunePurification)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.RunePurification));
            mplew.writeInt(player.getBuffSource(SecondaryStat.RunePurification));
        }
        if (statups.containsKey(SecondaryStat.RuneContagion)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.RuneContagion));
            mplew.writeInt(player.getBuffSource(SecondaryStat.RuneContagion));
        }
        if (statups.containsKey(SecondaryStat.DebuffHallucination)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DebuffHallucination));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DebuffHallucination));
        }
        if (statups.containsKey(SecondaryStat.BMageAuraYellow)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BMageAuraYellow));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BMageAuraYellow));
        }
        if (statups.containsKey(SecondaryStat.BMageAuraDrain)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BMageAuraDrain));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BMageAuraDrain));
        }
        if (statups.containsKey(SecondaryStat.BMageAuraBlue)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BMageAuraBlue));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BMageAuraBlue));
        }
        if (statups.containsKey(SecondaryStat.BMageAuraDark)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BMageAuraDark));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BMageAuraDark));
        }
        if (statups.containsKey(SecondaryStat.BMageAuraDebuff)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BMageAuraDebuff));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BMageAuraDebuff));
        }
        if (statups.containsKey(SecondaryStat.BMageAuraUnion)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BMageAuraUnion));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BMageAuraUnion));
        }
        if (statups.containsKey(SecondaryStat.IceAura)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.IceAura));
            mplew.writeInt(player.getBuffSource(SecondaryStat.IceAura));
        }
        if (statups.containsKey(SecondaryStat.KnightsAura)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.KnightsAura));
            mplew.writeInt(player.getBuffSource(SecondaryStat.KnightsAura));
        }
        if (statups.containsKey(SecondaryStat.ZeroAuraStr)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ZeroAuraStr));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ZeroAuraStr));
        }
        if (statups.containsKey(SecondaryStat.NovaArcherIncanation)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NovaArcherIncanation));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NovaArcherIncanation));
        }
        if (statups.containsKey(SecondaryStat.AranComboTempestAura)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AranComboTempestAura));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AranComboTempestAura));
        }
        if (statups.containsKey(SecondaryStat.XenonBursterLaser)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.XenonBursterLaser));
            mplew.writeInt(player.getBuffSource(SecondaryStat.XenonBursterLaser));
        }
        if (statups.containsKey(SecondaryStat.DarknessAura)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DarknessAura));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DarknessAura));
        }
        if (statups.containsKey(SecondaryStat.ShadowShield)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ShadowShield));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ShadowShield));
        }
        if (statups.containsKey(SecondaryStat.EquinoxActive)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.EquinoxActive));
            mplew.writeInt(player.getBuffSource(SecondaryStat.EquinoxActive));
        }
        if (statups.containsKey(SecondaryStat.NAThanatosDescent)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NAThanatosDescent));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NAThanatosDescent));
        }
        if (statups.containsKey(SecondaryStat.NAAnnihilation)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NAAnnihilation));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NAAnnihilation));
        }
        if (statups.containsKey(SecondaryStat.ATScrollPassive)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ATScrollPassive));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ATScrollPassive));
        }
        if (statups.containsKey(SecondaryStat.YetiFuryMode)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.YetiFuryMode));
            mplew.writeInt(player.getBuffSource(SecondaryStat.YetiFuryMode));
        }
        if (statups.containsKey(SecondaryStat.AMAbsorptionRiver)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AMAbsorptionRiver));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AMAbsorptionRiver));
        }
        if (statups.containsKey(SecondaryStat.AMAbsorptionWind)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AMAbsorptionWind));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AMAbsorptionWind));
        }
        if (statups.containsKey(SecondaryStat.AMAbsorptionSun)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AMAbsorptionSun));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AMAbsorptionSun));
        }
        if (statups.containsKey(SecondaryStat.UnwearyingRun)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.UnwearyingRun));
            mplew.writeInt(player.getBuffSource(SecondaryStat.UnwearyingRun));
        }
        if (statups.containsKey(SecondaryStat.IceAuraZone)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.IceAuraZone));
            mplew.writeInt(player.getBuffSource(SecondaryStat.IceAuraZone));
        }
        if (statups.containsKey(SecondaryStat.FlashMirage)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.FlashMirage));
            mplew.writeInt(player.getBuffSource(SecondaryStat.FlashMirage));
        }
        if (statups.containsKey(SecondaryStat.HolyBlood)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.HolyBlood));
            mplew.writeInt(player.getBuffSource(SecondaryStat.HolyBlood));
        }
        if (statups.containsKey(SecondaryStat.Infinity)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Infinity));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Infinity));
        }
        if (statups.containsKey(SecondaryStat.TeleportMasteryOn)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.TeleportMasteryOn));
            mplew.writeInt(player.getBuffSource(SecondaryStat.TeleportMasteryOn));
        }
        if (statups.containsKey(SecondaryStat.ChillingStep)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ChillingStep));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ChillingStep));
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.BlessingArmor)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.LimitBreakFinalAttack)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.LimitBreakFinalAttack));
            mplew.writeInt(player.getBuffSource(SecondaryStat.LimitBreakFinalAttack));
        }
        if (statups.containsKey(SecondaryStat.TranscendentLight)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.TranscendentLight));
            mplew.writeInt(player.getBuffSource(SecondaryStat.TranscendentLight));
        }
        if (statups.containsKey(SecondaryStat.ArtificialEvolution)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ArtificialEvolution));
            mplew.writeInt(player.getBuffSource(SecondaryStat.ArtificialEvolution));
        }
        if (statups.containsKey(SecondaryStat.Frenzy)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.Frenzy));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Frenzy));
        }
        if (statups.containsKey(SecondaryStat.LefMageCrystalGate)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.LefMageCrystalGate));
            mplew.writeInt(player.getBuffSource(SecondaryStat.LefMageCrystalGate));
        }
        if (statups.containsKey(SecondaryStat.KinesisPsychicPoint)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.KinesisPsychicPoint));
            mplew.writeInt(player.getBuffSource(SecondaryStat.KinesisPsychicPoint));
        }
        if (statups.containsKey(SecondaryStat.Confinement)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.Confinement));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Confinement));
        }
        if (statups.containsKey(SecondaryStat.GrabAndThrow)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.GrabAndThrow));
            mplew.writeInt(player.getBuffSource(SecondaryStat.GrabAndThrow));
        }
        if (statups.containsKey(SecondaryStat.DarkCloud)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.DarkCloud));
            mplew.writeInt(player.getBuffSource(SecondaryStat.DarkCloud));
        }
        if (statups.containsKey(SecondaryStat.UserAroundAttackDebuff)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.UserAroundAttackDebuff));
            mplew.writeInt(player.getBuffSource(SecondaryStat.UserAroundAttackDebuff));
        }
        if (statups.containsKey(SecondaryStat.KaringDoolAdvantage)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.KaringDoolAdvantage));
            mplew.writeInt(player.getBuffSource(SecondaryStat.KaringDoolAdvantage));
        }
        if (statups.containsKey(SecondaryStat.RPEventStat)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.RPEventStat));
            mplew.writeInt(player.getBuffSource(SecondaryStat.RPEventStat));
        }
        if (statups.containsKey(SecondaryStat.NaturesBelief)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NaturesBelief));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NaturesBelief));
        }
        if (statups.containsKey(SecondaryStat.AdrenalinSurge)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AdrenalinSurge));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AdrenalinSurge));
        }
        if (statups.containsKey(SecondaryStat.AnimaThiefFlameStrike)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.AnimaThiefFlameStrike));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AnimaThiefFlameStrike));
        }
        if (statups.containsKey(SecondaryStat.BladeStanceMode)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BladeStanceMode));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BladeStanceMode));
        }
        if (statups.containsKey(SecondaryStat.BladeStanceMode)) {
            mplew.writeInt(-劍豪.拔刀姿勢);
        }
        if (statups.containsKey(SecondaryStat.BladeStanceBooster)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BladeStanceBooster));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BladeStanceBooster));
        }
        if (statups.containsKey(SecondaryStat.BladeStancePower)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BladeStancePower));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BladeStancePower));
        }
        if (statups.containsKey(SecondaryStat.SelfHyperBodyIncPAD)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SelfHyperBodyIncPAD));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SelfHyperBodyIncPAD));
        }
        if (statups.containsKey(SecondaryStat.SelfHyperBodyMaxHP)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SelfHyperBodyMaxHP));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SelfHyperBodyMaxHP));
        }
        if (statups.containsKey(SecondaryStat.SelfHyperBodyMaxMP)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SelfHyperBodyMaxMP));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SelfHyperBodyMaxMP));
        }
        if (statups.containsKey(SecondaryStat.CriticalBuffAdd)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.CriticalBuffAdd));
            mplew.writeInt(player.getBuffSource(SecondaryStat.CriticalBuffAdd));
        }
        if (statups.containsKey(SecondaryStat.FireBarrier)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.FireBarrier));
            mplew.writeInt(player.getBuffSource(SecondaryStat.FireBarrier));
        }
        if (statups.containsKey(SecondaryStat.BossDamageRate)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BossDamageRate));
            mplew.writeInt(player.getBuffSource(SecondaryStat.BossDamageRate));
        }
        if (statups.containsKey(SecondaryStat.Stance)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Stance));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Stance));
        }
        if (statups.containsKey(SecondaryStat.SkillDeployment)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SkillDeployment));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SkillDeployment));
        }
        if (statups.containsKey(SecondaryStat.AntiEvilShield)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.AntiEvilShield));
            mplew.writeInt(player.getBuffSource(SecondaryStat.AntiEvilShield));
        }
        if (statups.containsKey(SecondaryStat.KenjiCounter)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.KenjiCounter));
            mplew.writeInt(player.getBuffSource(SecondaryStat.KenjiCounter));
        }
        if (statups.containsKey(SecondaryStat.Cyclone)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.Cyclone));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Cyclone));
        }
        if (statups.containsKey(SecondaryStat.Wet)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Wet));
            mplew.writeInt(player.getBuffSource(SecondaryStat.Wet));
        }
//        if (statups.containsKey(SecondaryStat.WaterSmashTeam)) {
//            mplew.writeInt(player.getBuffSource(SecondaryStat.WaterSmashTeam));
//        }
//        if (statups.containsKey(SecondaryStat.WaterSmashClass)) {
//            mplew.writeInt(player.getBuffSource(SecondaryStat.WaterSmashClass));
//        }
//        if (statups.containsKey(SecondaryStat.WaterSmashBuffCount)) {
//            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.WaterSmashBuffCount));
//            mplew.writeInt(player.getBuffSource(SecondaryStat.WaterSmashBuffCount));
//        }
        if (statups.containsKey(SecondaryStat.SpecialTombPL)) {
            mplew.writeInt(player.getBuffSource(SecondaryStat.SpecialTombPL));
        }
        if (statups.containsKey(SecondaryStat.GrabbedByMob)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.GrabbedByMob));
            mplew.writeInt(player.getBuffSource(SecondaryStat.GrabbedByMob));
        }
        if (statups.containsKey(SecondaryStat.ReduceMP)) {
            mplew.writeInt(player.getBuffSource(SecondaryStat.ReduceMP));
        }
        if (statups.containsKey(SecondaryStat.CoronaBuffOverlap)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.CoronaBuffOverlap));
            mplew.writeInt(player.getBuffSource(SecondaryStat.CoronaBuffOverlap));
        }
        if (statups.containsKey(SecondaryStat.MukHyun_HO_SIN_GANG_GI)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.MukHyun_HO_SIN_GANG_GI));
            mplew.writeInt(player.getBuffSource(SecondaryStat.MukHyun_HO_SIN_GANG_GI));
        }
//        if (statups.containsKey(SecondaryStat.ShamanIgnoreTargetDEF)) {
//            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ShamanIgnoreTargetDEF));
//            mplew.writeInt(player.getBuffSource(SecondaryStat.ShamanIgnoreTargetDEF));
//        }
        if (statups.containsKey(SecondaryStat.WindBreakerStormGuard)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.WindBreakerStormGuard));
            mplew.writeInt(player.getBuffSource(SecondaryStat.WindBreakerStormGuard));
        }
        if (statups.containsKey(SecondaryStat.NeoTokyoBossThesis)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NeoTokyoBossThesis));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NeoTokyoBossThesis));
        }
        if (statups.containsKey(SecondaryStat.NeoTokyoBossAntiThesis)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NeoTokyoBossAntiThesis));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NeoTokyoBossAntiThesis));
        }
        if (statups.containsKey(SecondaryStat.NeoTokyoBossBomb)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NeoTokyoBossBomb));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NeoTokyoBossBomb));
        }
        if (statups.containsKey(SecondaryStat.NeoTokyoBossPowOfLife)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.NeoTokyoBossPowOfLife));
            mplew.writeInt(player.getBuffSource(SecondaryStat.NeoTokyoBossPowOfLife));
        }
        if (statups.containsKey(SecondaryStat.SixthHakuman)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SixthHakuman));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SixthHakuman));
        }
        if (statups.containsKey(SecondaryStat.SixthShinBatto)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SixthShinBatto));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SixthShinBatto));
        }
        mplew.write(0);
        mplew.write(0);
        mplew.write(JobConstants.is聖魂劍士(player.getJob()) ? 5 : 0);
        mplew.writeInt(0);

        if (statups.containsKey(SecondaryStat.BlackMageCreate)) {
            mplew.writeInt(10);
        }
        if (statups.containsKey(SecondaryStat.BlackMageDestroy)) {
            mplew.writeInt(15);
        }
        if (statups.containsKey(SecondaryStat.PoseType)) {
            mplew.write(player.getBuffedValue(SecondaryStat.PoseType) != null ? 1 : 0);
            mplew.write(0);
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.BattlePvP_Helena_Mark)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.BattlePvP_LangE_Protection)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.MichaelSoulLink)) {
            SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.MichaelSoulLink);
            mplew.writeInt(mbsvh.fromChrID == player.getId() ? statups.get(SecondaryStat.MichaelSoulLink) : 0);
            mplew.writeBool(mbsvh.fromChrID == player.getId() && statups.get(SecondaryStat.MichaelSoulLink) <= 1);
            mplew.writeInt(mbsvh.fromChrID);
            mplew.writeInt(statups.get(SecondaryStat.MichaelSoulLink) > 1 ? mbsvh.effect.getLevel() : 0);
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.AdrenalinBoost)) {
            mplew.write(player.getBuffSource(SecondaryStat.AdrenalinBoost) == 狂狼勇士.鬥氣爆發 ? 1 : 0);
        }
        if (statups.containsKey(SecondaryStat.Stigma)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.Stigma));
        }
        if (statups.containsKey(SecondaryStat.PairingUser)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.PairingUser));
        }
        if (statups.containsKey(SecondaryStat.Frenzy)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.Frenzy));
        }
        if (statups.containsKey(SecondaryStat.SerpentScrew)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.SerpentScrew));
        }
        if (statups.containsKey(SecondaryStat.BloodyExplosion)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.BloodyExplosion));
        }
        if (statups.containsKey(SecondaryStat.ShadowSpear)) {
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.ShadowSpear));
        }
        if (statups.containsKey(SecondaryStat.Michael_RhoAias)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.Michael_RhoAias));
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.VampDeath)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.VampDeath));
        }
        if (statups.containsKey(SecondaryStat.LefGloryWing)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.LefBuffMastery)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.BattlePvP_Ryude_Frozen)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.BattlePvP_LangE_LiverStack)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        // SPAWN
        if (statups.containsKey(SecondaryStat.BattlePvP_KeyDown)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.BattlePvP_Wongki_AwesomeFairy)) {
            mplew.writeInt(0);
        }
        PacketHelper.write劍刃之壁(mplew, player, player.getBuffSource(SecondaryStat.StopForceAtomInfo));
        n3 = isChrinfo ? Randomizer.nextInt() : 1;
        // SPAWN 1
        if (statups.containsKey(SecondaryStat.DashSpeed)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(1);
            mplew.writeInt(n3);
            mplew.writeShort(0);
        }
        // SPAWN 2
        if (statups.containsKey(SecondaryStat.DashJump)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(1);
            mplew.writeInt(n3);
            mplew.writeShort(0);
        }
        // SPAWN 3
        if (statups.containsKey(SecondaryStat.RideVehicle)) {
            sourceid = player.getBuffSource(SecondaryStat.RideVehicle);
            if (sourceid > 0) {
                MaplePacketCreator.addMountId(mplew, player, sourceid);
                mplew.writeInt(sourceid);
            } else {
                mplew.writeInt(0);
                mplew.writeInt(0);
            }
            mplew.write(1);
            mplew.writeInt(n3);
        }
        // SPAWN 4
        if (statups.containsKey(SecondaryStat.PartyBooster)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.PartyBooster));
            mplew.writeInt(player.getBuffSource(SecondaryStat.PartyBooster));
            mplew.write(1);
            mplew.writeInt(n3);
        }
        // SPAWN 5
        if (statups.containsKey(SecondaryStat.GuidedBullet)) {
            mplew.write(1);
            mplew.writeInt(Randomizer.nextInt());
            mplew.writeZeroBytes(10);
            mplew.write(1);
            mplew.writeInt(n3);
        }
        // SPAWN 6
        if (statups.containsKey(SecondaryStat.Undead)) {
            mplew.writeZeroBytes(16);
            mplew.write(1);
            mplew.writeInt(n3);
        }
        // SPAWN 7
        if (statups.containsKey(SecondaryStat.RelicGauge)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(1);
            mplew.writeInt(n3);
        }
        // SPAWN 8
        if (statups.containsKey(SecondaryStat.RideVehicleExpire)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.RideVehicleExpire));
            mplew.writeInt(player.getBuffSource(SecondaryStat.RideVehicleExpire));
            mplew.writeShort(player.getBuffedIntValue(SecondaryStat.RideVehicleExpire) > 0 ? 10 : 0);
            mplew.write(1);
            mplew.writeInt(n3);
        }
        // SPAWN 9
        if (statups.containsKey(SecondaryStat.SecondAtomLockOn)) {
            mplew.writeInt(player.getBuffedIntValue(SecondaryStat.SecondAtomLockOn));
            mplew.writeInt(player.getBuffSource(SecondaryStat.SecondAtomLockOn));
            mplew.writeShort(0);
            mplew.write(1);
            mplew.writeInt(n3);
        }
        // SPAWN 10
        if (statups.containsKey(SecondaryStat.Curse)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(1);
            mplew.writeInt(n3);
            mplew.writeZeroBytes(8);
        }
        for (int i = 0; i < 13; i++) {
            // 像giveBuff的stack Buff
            int nCount = 0;
            mplew.writeInt(nCount);
            for (int j = nCount; j > 0; j--) {
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                int nCount1 = 0;
                mplew.writeInt(nCount1);
                for (int k = nCount; k > 0; k--) {
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                }
                int nCount2 = 0;
                mplew.writeInt(nCount2);
                for (int k = nCount; k > 0; k--) {
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                }
            }
        }
        if (statups.containsKey(SecondaryStat.OutSide)) {
            mplew.writeInt(1000);
        }
        if (statups.containsKey(SecondaryStat.KeyDownMoving)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.BossWill_Infection)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.ComboCounter)) {
            mplew.writeHexString("68 04 00 00");
            mplew.writeInt(0);

        }

        if (statups.containsKey(SecondaryStat.MinigameStat)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.XenonHoloGramGraffiti)) {
            mplew.writeInt(0);
        }
        mplew.write(0x00);
        mplew.write(0x6D);
        if (statups.containsKey(SecondaryStat.LefWarriorNobility)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.BMageAuraYellow)) {
            mplew.writeInt(player.getBuffedValue(SecondaryStat.BMageAuraYellow) != null ? 1 : 0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.BMageAuraDrain)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.BMageAuraBlue)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.BMageAuraDark)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.BMageAuraDebuff)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.BMageAuraUnion)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.IceAura)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.KnightsAura)) {
            mplew.writeInt(player.getId());
            mplew.writeInt(1);
        }
        if (statups.containsKey(SecondaryStat.ZeroAuraStr)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.NovaArcherIncanation)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.AranComboTempestAura)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.XenonBursterLaser)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.ShadowShield)) {
            mplew.writeInt(player.getBuffedIntZ(SecondaryStat.ShadowShield));
        }
        if (statups.containsKey(SecondaryStat.Infinity)) {
            mplew.writeInt(0); // time
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.YetiFuryMode)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.GrabAndThrow)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.RPEventStat)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
//        if (statups.containsKey(SecondaryStat.UNK_240_ADD_543)) {
//            mplew.writeInt(0);
//        }
        if (statups.containsKey(SecondaryStat.NeoTokyoBossBomb)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.NeoTokyoBossPowOfLife)) {
            mplew.writeInt(0);
        }
    }

    /**
     * 取消BUFF狀態
     */
    public static byte[] temporaryStatReset(List<SecondaryStat> statups, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TemporaryStatReset.getValue());
        if (statups.contains(SecondaryStat.DawnShield_WillCare)) {
            statups.add(SecondaryStat.DawnShield_ExHP);
        }
        if (statups.contains(SecondaryStat.Warrior_AuraWeapon)) {
            statups.remove(SecondaryStat.IndieBuffIcon);
        }
        mplew.writeBool(true); // TMS229
        mplew.writeBool(true); // TMS229
        mplew.write(statups.size());
        writeBuffMask(mplew, statups);
        statups.sort(Comparator.naturalOrder());
        boolean disease = false;
        for (final SecondaryStat stat : statups) {
            if (!disease) {
                disease = MapleDisease.containsStat(stat);
            }
            if (stat.canStack() && !SkillConstants.isSpecialStackBuff(stat)) {
                encodeIndieBuffStat(mplew, chr, stat);
            }
        }
        for (SecondaryStat statup : statups) {
            if (SkillConstants.isMovementAffectingStat(statup)) {
                mplew.write(0);
                break;
            }
        }
        if (statups.contains(SecondaryStat.PoseType)) {
            mplew.write(1);
        }
        if (statups.contains(SecondaryStat.BattleSurvivalDefence)) {
            mplew.write(0);
        }
        if (statups.contains(SecondaryStat.RideVehicleExpire)) {
            mplew.write(1);
        }
        if (statups.contains(SecondaryStat.RideVehicle)) {
            mplew.write(3);
        } else if (disease) {
            mplew.write(3);
            mplew.write(0);
            mplew.write(1);
        }
        mplew.write(0);

        return mplew.getPacket();
    }

    public static <E extends Buffstat> void writeSingleMask(MaplePacketLittleEndianWriter mplew, E statup) {
        writeBuffMask(mplew, Collections.singletonList(statup));
    }

    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mplew, Map<E, Integer> statups) {
        writeBuffMask(mplew, statups.keySet());
    }

    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mplew, Collection<E> statups) {
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];
        for (E statup : statups) {
            mask[statup.getPosition()] |= statup.getValue();
        }
        for (int aMask : mask) {
            mplew.writeInt(aMask);
        }
    }

    public static byte[] giveMobZoneState(MapleCharacter chr, int objectId) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.MobZoneState, objectId));
    }

    public static byte[] setHoYoungRune(MapleCharacter chr) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.AnimaThiefTaoistGauge, 0));
    }

    public static byte[] setHoYoungState(MapleCharacter chr) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.AnimaThiefTaoistType, 0));
    }

    public static byte[] setAdeleCharge(MapleCharacter chr) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.LWSwordGauge, 0));
    }

    public static byte[] setMaliceCharge(MapleCharacter chr) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.NADragonGauge, 0));
    }

    public static byte[] showPP(MapleCharacter chr) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.KinesisPsychicPoint, 0));
    }

    public static byte[] setErosions(MapleCharacter chr) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.SpecterGauge, 0));
    }

    public static byte[] setPureBeads(MapleCharacter chr) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.SpellBullet_Plain, 0));
    }

    public static byte[] setFlameBeads(MapleCharacter chr) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.SpellBullet_Scarlet, 0));
    }

    //
    public static byte[] setGaleBeads(MapleCharacter chr) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.SpellBullet_Gust, 0));
    }

    public static byte[] setAbyssBeads(MapleCharacter chr) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.SpellBullet_Abyss, 0));
    }

    public static byte[] setInfinitiFlameCharge(MapleCharacter chr, int value) {
        return giveBuff(chr, null, Collections.singletonMap(SecondaryStat.FlameWizardInfiniteFlame, value));
    }


}
