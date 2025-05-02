/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Client.force.MapleForceFactory;
import Client.inventory.MapleAndroid;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Client.stat.PlayerStats;
import Config.constants.JobConstants;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.影武者;
import Config.constants.skills.冒險家_技能群組.暗影神偷;
import Config.constants.skills.冒險家_技能群組.箭神;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Config.constants.skills.皇家騎士團_技能群組.破風使者;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.*;
import Opcode.Opcode.EffectOpcode;
import Packet.BuffPacket;
import Packet.EffectPacket;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author admin
 */
public class TakeDamageHandler {

    private static final Logger log = LoggerFactory.getLogger(TakeDamageHandler.class);

    public static void TakeDamage(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        } else if (chr.isHidden() || chr.isGm() && chr.isInvincible()) {
            c.announce(EffectPacket.showSpecialEffect(EffectOpcode.UserEffect_Resist));
            chr.getMap().broadcastMessage(chr, EffectPacket.showForeignEffect(chr.getId(), EffectOpcode.UserEffect_Resist), false);
            c.sendEnableActions();
            return;
        }
        UserHitInfo info = new UserHitInfo(chr.getId());
        PlayerStats stats = chr.getStat();
        MapleMonster attacker = null;
        MapleMonsterStats monsterStats = null;
        int diseaseSkill = 0;
        int diseaseLevel = 0;
        slea.readInt();//V.163 new
        chr.updateTick(slea.readInt());
        info.setType(slea.readByte()); //-4 為煙霧造成, -3 和 -2 為地圖造成的傷害.
        slea.readByte(); // Element - 0x00 = elementless, 0x01 = ice, 0x02 = fire, 0x03 = lightning
        slea.readByte();
        info.setDamage(slea.readInt());//受傷數值
        info.setCritical(slea.readByte() > 0);
        info.setUnkb(slea.readByte() > 0);
        if (info.getType() >= -1) { //V.160
            info.setObjectID(slea.readInt());
            slea.readInt();
            slea.readInt();
            slea.readInt();
            slea.readInt();
            slea.readInt();
            slea.readInt();
            slea.readInt();
            slea.readInt();
            slea.readInt();
            info.setTemplateID(slea.readInt());
            slea.readByte();
            slea.readByte();
            slea.readInt(); // TemplateID
            slea.readInt(); // oid
            attacker = chr.getMap().getMobObject(info.getObjectID());
            info.setDirection(slea.readByte());
            monsterStats = MapleLifeFactory.getMonsterStats(info.getTemplateID());
            if (attacker != null && ((info.getTemplateID() != 0 && attacker.getId() != info.getTemplateID()) || attacker.isFake() || attacker.getStats().isEscort())) {
                return;
            }
            info.setSkillID(slea.readInt());
            info.setRefDamage(slea.readInt());
            info.setDefType(slea.readByte());
            if (slea.readByte() > 1 || info.getRefDamage() > 0) {
                info.setRefPhysical(slea.readByte() > 0);
                info.setRefOid(slea.readInt());
                info.setRefType(slea.readByte());
                slea.readShort();
                slea.readShort();
                info.setPos(slea.readPos());
            }
            info.setOffset(slea.readByte());
            info.setOffset_d(slea.readInt());
        }
        slea.readInt();
        diseaseLevel = slea.readInt();
        diseaseSkill = slea.readByte();
        if (info.isRefPhysical() && info.getRefDamage() > 0 && chr.getSkillEffect(info.getSkillID()) != null) {
            if (attacker != null) {
                attacker.damage(chr, info.getSkillID(), info.getRefDamage(), false);
            }
            final MapleStatEffect skillEffect = chr.getSkillEffect(info.getSkillID());
            if (skillEffect != null) {
                skillEffect.applyMonsterEffect(chr, attacker, skillEffect.getMobDebuffDuration(chr) > 0 ? skillEffect.getMobDebuffDuration(chr) : (skillEffect.getSubTime() * 1000));
            }
        }
        if (attacker != null && info.getDamage() > 0) {
            long refDamage = 0L;
            for (Pair<Integer, Integer> pair : stats.getDamageReflect().values()) {
                if (Randomizer.isSuccess(pair.right)) {
                    refDamage += pair.left * 100.0 * info.getDamage() / 100.0;
                }
            }
            if (refDamage > 0L) {
                attacker.damage(chr, -1, refDamage, false);
            }
        }
        if (info.getType() != -1) {
            final MobAttackInfo mobAttackInfo;
            if (monsterStats != null && (mobAttackInfo = monsterStats.getMobAttack(info.getType())) != null) {
                diseaseSkill = mobAttackInfo.getDiseaseSkill();
                diseaseLevel = mobAttackInfo.getDiseaseLevel();
            }
        } else {
            Pair<Integer, Integer> bodyDisease;
            if (monsterStats != null && (bodyDisease = monsterStats.getBodyDisease()) != null) {
                diseaseSkill = bodyDisease.getLeft();
                diseaseLevel = bodyDisease.getRight();
            }
        }
        if (diseaseSkill > 0 && diseaseLevel > 0) {
            final MobSkill mobSkill;
            if ((mobSkill = MobSkillFactory.getMobSkill(diseaseSkill, diseaseLevel)) != null && (info.getDamage() == -1 || info.getDamage() > 0)) {
                mobSkill.unprimaryPassiveApplyTo(chr);
            }
        }
        if (info.getDamage() > 0) {
            int hploss = info.getDamage();
            int mpcost = 0;
            SecondaryStatValueHolder mbsvh;
            if (chr.getBuffedIntValue(SecondaryStat.MagicGuard) > 0) {
                final int magicShield = hploss * chr.getBuffedIntValue(SecondaryStat.MagicGuard) / 100;
                if (chr.getStat().getMp() >= magicShield) {
                    hploss -= magicShield;
                    mpcost -= magicShield;
                }
            }
            MapleStatEffect eff;
            if ((eff = chr.getSkillEffect(烈焰巫師.火的反動力)) != null) {
                final int magicShield = hploss * eff.getX() / 100;
                if (chr.getStat().getMp() >= magicShield) {
                    hploss -= magicShield;
                    mpcost -= magicShield;
                }
            }
            if ((eff = chr.getSkillEffect(夜光.魔法防禦)) != null) {
                final int magicShield = hploss * eff.getX() / 100;
                if (chr.getStat().getMp() >= magicShield) {
                    hploss -= magicShield;
                    mpcost -= magicShield;
                }
            }
            if (chr.getStat().damAbsorbShieldR > 0.0) {
                hploss -= (int) Math.floor(hploss * chr.getStat().damAbsorbShieldR / 100.0);
            }
            if ((mbsvh = chr.getBuffStatValueHolder(SecondaryStat.PowerTransferGauge, 神射手.反向傷害)) != null) {
                final int lost = Math.min(hploss * mbsvh.effect.getX() / 100, mbsvh.value);
                hploss -= lost;
                if (lost >= mbsvh.value) {
                    chr.dispelEffect(SecondaryStat.PowerTransferGauge);
                } else {
                    mbsvh.value -= lost;
                    chr.send(BuffPacket.giveBuff(chr, mbsvh.effect, Collections.singletonMap(SecondaryStat.PowerTransferGauge, mbsvh.effect.getSourceId())));
                }
            }
            if ((eff = chr.getEffectForBuffStat(SecondaryStat.SpiritGuard)) != null) {
                hploss = 0;
                eff.unprimaryPassiveApplyTo(chr);
            }
            if ((eff = chr.getEffectForBuffStat(SecondaryStat.BloodyExplosion)) != null) {
                final MapleStatEffect effect = chr.getSkillEffect(暗影神偷.貪婪);
                final int mesoShield = (int) (hploss * (effect != null ? effect.getV() + 50 : 50) / 100.0);
                int mesoChange = mesoShield * eff.getX() / 100;
                if (effect != null) {
                    mesoChange -= mesoChange * effect.getW() / 100;
                }
                mesoChange = Math.max(1, mesoChange);
                if (chr.getMeso() >= mesoChange) {
                    chr.gainMeso(-mesoChange, false);
                    hploss -= mesoShield;
                }
            }
            if ((eff = chr.getEffectForBuffStat(SecondaryStat.RWBarrier)) != null) {
                final int buffedIntValue2 = chr.getBuffedIntValue(SecondaryStat.RWBarrier);
                final int min = Math.min(hploss, buffedIntValue2);
                final int n11 = buffedIntValue2 - min;
                hploss -= min;
                chr.setBuffStatValue(SecondaryStat.RWBarrier, 爆拳槍神.續航防盾, n11);
                eff.applyTo(chr);
            }
            if ((eff = chr.getEffectForBuffStat(SecondaryStat.PowerTransferGauge)) != null) {
                final int value = chr.getBuffedIntValue(SecondaryStat.PowerTransferGauge);
                if (JobConstants.is天使破壞者(chr.getJob())) {
                    final int min = Math.min(hploss * eff.getX() / 100, value);
                    chr.setBuffStatValue(SecondaryStat.PowerTransferGauge, 天使破壞者.靈魂傳動, Math.min(Math.max(0, value - min), 99999));
                    hploss -= min;
                    eff.unprimaryPassiveApplyTo(chr);
                }
            }
            if ((eff = chr.getEffectForBuffStat(SecondaryStat.ImmuneBarrier)) != null) {
                final int value = chr.getBuffedIntValue(SecondaryStat.ImmuneBarrier);
                if (JobConstants.is神之子(chr.getJob())) {
                    final int min = Math.min(hploss * eff.getX() / 100, value);
                    chr.setBuffStatValue(SecondaryStat.ImmuneBarrier, 神之子.免疫護罩, Math.min(Math.max(0, value - min), 99999));
                    hploss -= min;
                    eff.unprimaryPassiveApplyTo(chr);
                }
            }
            if ((eff = chr.getEffectForBuffStat(SecondaryStat.KinesisPsychicEnergeShield)) != null && chr.getSpecialStat().getPP() > 0) {
                hploss -= hploss * eff.getX() / 100;
                chr.handlePPCount(-1);
            }
            if ((eff = chr.getEffectForBuffStat(SecondaryStat.EtherealForm)) != null) {
                if (chr.getStat().getMp() > 0 && chr.getStat().getMp() >= Math.min(eff.getX(), chr.getStat().getMp())) {
                    hploss = 0;
                    chr.addHPMP(0, -Math.min(eff.getX(), chr.getStat().getMp()), false);
                } else {
                    hploss = Math.min(eff.getY(), chr.getStat().getHp());
                }
            }
            if ((eff = chr.getEffectForBuffStat(SecondaryStat.HitStackDamR)) != null) {
                eff.unprimaryPassiveApplyTo(chr);
            }
            if ((eff = chr.getEffectForBuffStat(SecondaryStat.WindBreakerStormGuard)) != null) {
                hploss = 0;
                eff.unprimaryPassiveApplyTo(chr);
            }
            if ((eff = chr.getEffectForBuffStat(SecondaryStat.LefWarriorNobility)) != null) {
                hploss -= hploss * (100 - eff.getX()) / 100;
            }
            final int value = chr.getBuffedIntValue(SecondaryStat.LefBuffMastery);
            if (value > 0 && (eff = chr.getSkillEffect(伊利恩.聖靈護盾)) != null) {
                hploss -= hploss * eff.getX() / 100;
                final Skill skill = SkillFactory.getSkill(伊利恩.祝福標誌_1);
                if (skill != null) {
                    eff.unprimaryPassiveApplyTo(chr);
                    final MapleStatEffect effect;
                    if ((effect = skill.getEffect(value - 1)) != null) {
                        effect.unprimaryPassiveApplyTo(chr);
                    } else {
                        chr.dispelEffect(伊利恩.祝福標誌_1);
                    }
                }
            }
            mbsvh = chr.getBuffStatValueHolder(SecondaryStat.ComboCounter);
            if (mbsvh != null && mbsvh.effect != null) {
                MapleStatEffect effect = chr.getSkillEffect(英雄.鬥氣綜合);
                MapleStatEffect effectEnchant = chr.getSkillEffect(英雄.進階鬥氣);
                if (effect != null) {
                    int subProp = effect.getSubProp();
                    if (mbsvh.value < effect.getX() + 1 && Randomizer.nextInt(100) < subProp) {
                        mbsvh.value += 1;
                        if (effectEnchant != null && effectEnchant.makeChanceResult(chr) && mbsvh.value < effect.getX() + 1) {
                            mbsvh.value += 1;
                        }
                        chr.send(BuffPacket.giveBuff(chr, mbsvh.effect, Collections.singletonMap(SecondaryStat.ComboCounter, mbsvh.effect.getSourceId())));
                    }
                }
            }
            if ((mbsvh = chr.getBuffStatValueHolder(SecondaryStat.ShadowShield)) != null && mbsvh.effect != null && mbsvh.z > 0 && (hploss >= chr.getStat().getHp() || hploss >= chr.getStat().getCurrentMaxHP() * mbsvh.effect.getY() / 100)) {
                if (hploss > chr.getStat().getHp()) {
                    hploss = chr.getStat().getHp();
                }
                hploss = hploss * (100 - mbsvh.effect.getQ()) / 100;
                mbsvh.z--;
                chr.send(BuffPacket.giveBuff(chr, mbsvh.effect, Collections.singletonMap(SecondaryStat.ShadowShield, mbsvh.effect.getSourceId())));
            }
            int hplossend = hploss;
            List<SecondaryStatValueHolder> mbsvhs = chr.getIndieBuffStatValueHolder(SecondaryStat.IndieBarrier);
            for (SecondaryStatValueHolder m : mbsvhs) {
                if (m.value >= hplossend) {
                    m.value -= hplossend;
                    hplossend = 0;
                } else {
                    hplossend -= m.value;
                    m.value = 0;
                }
                chr.send(BuffPacket.giveBuff(chr, m.effect, Collections.singletonMap(SecondaryStat.IndieBarrier, m.effect.getSourceId())));
                if (hplossend <= 0) {
                    hplossend = 0;
                    break;
                }
            }
            chr.addHPMP(-hplossend, mpcost, false);
            info.setDamage(hploss);
            chr.getCheatTracker().setLastAttackTime();
        }
        if (chr.isDebug()) {
            chr.dropDebugMessage(1, "[玩家受傷] 受傷類型: " + info.getTypeName() + " 受傷數值: " + info.getDamage());
        }
        chr.getSpecialStat().setHurtHP(info.getDamage());
        applyDamageTaken(chr, attacker, info);
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.damagePlayer(info), false);

        MapleAndroid android = chr.getAndroid();
        if (android != null) {
            android.showEmotion(chr, "alert");
        }
    }

    public static void applyDamageTaken(MapleCharacter player, MapleMonster attacker, UserHitInfo info) {
        MapleForceFactory ff = MapleForceFactory.getInstance();
        boolean guard = false;
        SecondaryStatValueHolder mbsvh = player.getBuffStatValueHolder(SecondaryStat.HolyMagicShell);
        if (mbsvh != null && mbsvh.effect != null) {
            if (mbsvh.value > 0) {
                guard = true;
                mbsvh.value -= 1;
                Map<SecondaryStat, Integer> statups = new LinkedHashMap<>();
                for (SecondaryStat stat : mbsvh.effect.getStatups().keySet()) {
                    statups.put(stat, mbsvh.effect.getSourceId());
                }
                player.send(BuffPacket.giveBuff(player, mbsvh.effect, statups));
            }
            if (mbsvh.value <= 0) {
                player.cancelEffect(mbsvh.effect, true, -1L, Collections.singletonMap(SecondaryStat.HolyMagicShell, 0));
            }
        }
        switch (player.getJob()) {
            case 121:
            case 122: {
                MapleMonster monster;
                MapleStatEffect effect;
                if (info.getRefOid() > 0 && (monster = player.getMap().getMobObject(info.getRefOid())) != null && (effect = player.getSkillEffect(聖騎士.盾牌技能)) != null) {
                    effect.applyMonsterEffect(player, monster, effect.getMobDebuffDuration(player));
                }
                if (!guard && info.getDamage() <= 0) {
                    mbsvh = player.getBuffStatValueHolder(SecondaryStat.BlessingArmor);
                    if (mbsvh != null && mbsvh.effect != null) {
                        if (mbsvh.value > 0) {
                            mbsvh.value -= 1;
                            Map<SecondaryStat, Integer> statups = new LinkedHashMap<>();
                            for (SecondaryStat stat : mbsvh.effect.getStatups().keySet()) {
                                statups.put(stat, mbsvh.effect.getSourceId());
                            }
                            player.send(BuffPacket.giveBuff(player, mbsvh.effect, statups));
                        }
                        if (mbsvh.value <= 0) {
                            player.cancelEffect(mbsvh.effect, true, -1L, Collections.singletonMap(SecondaryStat.BlessingArmor, 0));
                        }
                    }
                }
                if (!player.isSkillCooling(聖騎士.祝福護甲) && (effect = player.getSkillEffect(聖騎士.祝福護甲)) != null && effect.makeChanceResult(player)) {
                    effect.unprimaryPassiveApplyTo(player);
                    int cooldown = effect.getCooldown(player);
                    if (cooldown > 0) {
                        player.registerSkillCooldown(effect.getSourceId(), cooldown, true);
                    }
                }
                break;
            }
            case 311:
            case 312: {
                if (info.getDamage() <= 0 && player.getSkillEffect(箭神.躲避) != null) {
                    player.getClient().announce(MaplePacketCreator.sendCritAttack());
                }
                break;
            }
            case 321:
            case 322: {
                if (info.getDamage() <= 0 && player.getSkillEffect(神射手.躲避) != null) {
                    player.getClient().announce(MaplePacketCreator.sendCritAttack());
                }
                break;
            }
            case 331:
            case 332: {
                if (info.getDamage() <= 0 && player.getSkillEffect(開拓者.躲避) != null) {
                    player.getClient().announce(MaplePacketCreator.sendCritAttack());
                }
                break;
            }
            case 433:
            case 434: {
                final MapleStatEffect effect;
                if (info.getDamage() <= 0 && (effect = player.getSkillEffect(影武者.暗影迴避)) != null && player.getCheatTracker().canNextShadowDodge()) {
                    effect.unprimaryPassiveApplyTo(player);
                }
                break;
            }
            case 1311:
            case 1312: {
                final MapleStatEffect effect;
                if (info.getDamage() <= 0 && (effect = player.getSkillEffect(破風使者.迴避之風)) != null && player.getCheatTracker().canNextShadowDodge()) {
                    effect.unprimaryPassiveApplyTo(player);
                }
                break;
            }
            case 3111:
            case 3112: {
                final MapleStatEffect skillEffect8;
                if (info.getDefType() > 0 && (skillEffect8 = player.getSkillEffect(惡魔殺手.力量防禦)) != null) {
                    player.addHPMP(skillEffect8.getY(), 0);
                    player.addHPMP(0, skillEffect8.getZ(), false);
                }
                break;
            }
            case 3311:
            case 3312: {
                if (info.getDamage() <= 0 && player.getSkillEffect(狂豹獵人.迴避) != null) {
                    player.getClient().announce(MaplePacketCreator.sendCritAttack());
                }
                break;
            }
            case 3611:
            case 3612: {
                if (player.getEffectForBuffStat(SecondaryStat.ShadowPartner) != null) {
                    player.getSpecialStat().setShadowHP(player.getSpecialStat().getShadowHP() - info.getDamage());
                    if (player.getSpecialStat().getShadowHP() <= 0) {
                        player.dispelEffect(SecondaryStat.ShadowPartner);
                    }
                }
                MapleStatEffect effect;
                if ((effect = player.getEffectForBuffStat(SecondaryStat.XenonAegisSystem)) != null && effect.makeChanceResult(player) && player.getCheatTracker().canNextAegisSystem()) {
                    player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(ff.getMapleForce(player, effect, 0)), true);
                }
                if ((effect = player.getEffectForBuffStat(SecondaryStat.StackBuff)) != null) {
                    effect.unprimaryPassiveApplyTo(player);
                }
                if (info.getDamage() > 0 && player.getChair() != null && player.getChair().getItemId() == 3010587) {
                    player.getMap().removeAffectedArea(player.getId(), 傑諾.時空膠囊);
                }
                break;
            }
            case 3700:
            case 3710:
            case 3711:
            case 3712: {
                final MapleStatEffect effect;
                if ((effect = player.getSkillEffect(爆拳槍神.續航防盾)) != null && player.getBuffedValue(SecondaryStat.RWBarrier) == null) {
                    effect.applyTo(player);
                }
                break;
            }
            case 4111:
            case 4112: {
                final MapleStatEffect effect;
                if (info.getDamage() <= 0 && (effect = player.getSkillEffect(劍豪.柳身)) != null && effect.makeChanceResult(player)) {
                    effect.unprimaryPassiveApplyTo(player);
                }
                break;
            }
            case 4210:
            case 4211:
            case 4212: {
                final MapleStatEffect effect;
                if (info.getDamage() > 0 && (effect = player.getEffectForBuffStat(SecondaryStat.FireBarrier)) != null) {
                    effect.unprimaryPassiveApplyTo(player);
                    return;
                }
                break;
            }
            case 5100:
            case 5110:
            case 5111:
            case 5112: {
                if (player.getBuffedValue(SecondaryStat.BodyRectGuardPrepare) != null && info.getDamage() <= 0) {
                    player.getClient().announce(EffectPacket.showRoyalGuardAttack());
                }
                final MapleStatEffect effect;
                if ((effect = player.getEffectForBuffStat(SecondaryStat.MichaelStanceLink)) != null) {
                    effect.unprimaryPassiveApplyTo(player);
                }
                break;
            }
            case 6510:
            case 6511:
            case 6512: {
                break;
            }
            case 10000:
            case 10100:
            case 10110:
            case 10111:
            case 10112: {
                final MapleStatEffect effect;
                if (info.getDamage() > 0 && player.isBeta() && (effect = player.getSkillEffect(神之子.免疫護罩)) != null && effect.makeChanceResult(player)) {
                    effect.unprimaryPassiveApplyTo(player);
                    break;
                }
                break;
            }
        }
    }

    public static class UserHitInfo {

        private final int characterID;
        private boolean critical;
        private boolean unkb;
        private int damage;
        private int skillID;
        private int refDamage;
        private byte defType;
        private boolean refPhysical;
        private byte refType;
        private Point pos;
        private byte offset;
        private int offset_d = 0;
        private byte type = 0;
        private int objectID = 0;
        private byte direction = 0;
        private int refOid = 0;
        private int templateID;
        private String typeName;

        public UserHitInfo(final int characterID) {
            this.characterID = characterID;
        }

        public int getCharacterID() {
            return this.characterID;
        }

        public boolean isCritical() {
            return this.critical;
        }

        public boolean isUnkb() {
            return this.unkb;
        }

        public int getDamage() {
            return this.damage;
        }

        public int getSkillID() {
            return this.skillID;
        }

        public int getRefDamage() {
            return this.refDamage;
        }

        public byte getDefType() {
            return this.defType;
        }

        public boolean isRefPhysical() {
            return this.refPhysical;
        }

        public byte getRefType() {
            return this.refType;
        }

        public Point getPos() {
            return this.pos;
        }

        public byte getOffset() {
            return this.offset;
        }

        public int getOffset_d() {
            return this.offset_d;
        }

        public byte getType() {
            return this.type;
        }

        public int getObjectID() {
            return this.objectID;
        }

        public byte getDirection() {
            return this.direction;
        }

        public int getRefOid() {
            return this.refOid;
        }

        public int getTemplateID() {
            return this.templateID;
        }

        public void setCritical(boolean critical) {
            this.critical = critical;
        }

        public void setUnkb(boolean unkb) {
            this.unkb = unkb;
        }

        public void setDamage(int damage) {
            this.damage = damage;
        }

        public void setSkillID(int skillID) {
            this.skillID = skillID;
        }

        public void setRefDamage(int refDamage) {
            this.refDamage = refDamage;
        }

        public void setDefType(byte defType) {
            this.defType = defType;
        }

        public void setRefPhysical(boolean refPhysical) {
            this.refPhysical = refPhysical;
        }

        public void setRefType(byte refType) {
            this.refType = refType;
        }

        public void setPos(Point pos) {
            this.pos = pos;
        }

        public void setOffset(byte offset) {
            this.offset = offset;
        }

        public void setOffset_d(int offset_d) {
            this.offset_d = offset_d;
        }

        public void setType(byte type) {
            this.type = type;
            setTypeName();
        }

        private void setTypeName() {
            String typeName;
            switch (this.type) {
                case -3:
                    typeName = "場景傷害";
                    break;
                case -1:
                    typeName = "怪物碰撞";
                    break;
                case 0:
                    typeName = "近戰攻擊";
                    break;
                case 1:
                    typeName = "爆炸攻擊";
                    break;
                case 2:
                    typeName = "爆炸攻擊";
                    break;
                case 3:
                    typeName = "魔法攻擊";
                    break;
                case 4:
                    typeName = "BOSS攻擊";
                    break;
                case -2:
                default:
                    typeName = "未知傷害";
                    break;
            }
            this.typeName = typeName;
        }

        public void setObjectID(int objectID) {
            this.objectID = objectID;
        }

        public void setDirection(byte direction) {
            this.direction = direction;
        }

        public void setRefOid(int refOid) {
            this.refOid = refOid;
        }

        public void setTemplateID(int templateID) {
            this.templateID = templateID;
        }

        public String getTypeName() {
            return typeName;
        }
    }
}
