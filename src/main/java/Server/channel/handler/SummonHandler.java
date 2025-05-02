package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Client.force.MapleForceFactory;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Client.skills.SummonSkillEntry;
import Config.constants.SkillConstants;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.夜使者;
import Config.constants.skills.冒險家_技能群組.影武者;
import Config.constants.skills.冒險家_技能群組.槍神;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Config.constants.skills.皇家騎士團_技能群組.聖魂劍士;
import Net.server.MapleItemInformationProvider;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.maps.*;
import Net.server.movement.LifeMovementFragment;
import Net.server.unknown.SummonedMagicAltarInfo;
import Opcode.Opcode.EffectOpcode;
import Packet.*;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SummonHandler {

    private static final Logger log = LoggerFactory.getLogger(MovementParse.class);

    public static void MoveDragon(MaplePacketReader slea, MapleCharacter chr) {
        final int gatherDuration = slea.readInt();
        final int nVal1 = slea.readInt();
        final Point mPos = slea.readPos();
        final Point oPos = slea.readPos();
        List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 5);
        if (chr != null && chr.getDragon() != null && res.size() > 0) {
            MovementParse.updatePosition(res, chr.getDragon(), 0);
            if (!chr.isHidden()) {
                chr.getMap().broadcastMessage(chr, SummonPacket.moveDragon(chr.getDragon(), gatherDuration, nVal1, mPos, oPos, res), chr.getPosition());
            }
        }
    }

    /*
     * 龍飛行
     */
    public static void DragonFly(MaplePacketReader slea, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null || chr.getDragon() == null) {
            return;
        }
        /*
         * 1902040 - 第1階段龍 - (無描述)
         * 1902041 - 第2階段龍 - (無描述)
         * 1902042 - 第3階段龍 - (無描述)
         * 1912033 - 第1階段龍鞍 - (無描述)
         * 1912034 - 第2階段龍鞍 - (無描述)
         * 1912035 - 第3階段龍鞍 - (無描述)
         */
        int type = slea.readInt();
        int mountId = type == 0 ? slea.readInt() : 0;
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.showDragonFly(chr.getId(), type, mountId), chr.getPosition());
    }

    public static void MoveSummon(MaplePacketReader slea, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        int objid = slea.readInt();
        MapleMapObject obj = chr.getMap().getMapObject(objid, MapleMapObjectType.SUMMON);
        if (obj == null) {
            return;
        }
        if (obj instanceof MapleDragon) {
            MoveDragon(slea, chr);
            return;
        }
        MapleSummon sum = (MapleSummon) obj;
        if (sum.getOwnerId() != chr.getId() || sum.getSkillLevel() <= 0 || sum.getMovementType() == SummonMovementType.STOP) {
            return;
        }
        final int gatherDuration = slea.readInt();
        final int nVal1 = slea.readInt();
        final Point mPos = slea.readPos();
        final Point oPos = slea.readPos();
        List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 4);
        MovementParse.updatePosition(res, sum, 0);
        if (res.size() > 0) {
            chr.getMap().broadcastMessage(chr, SummonPacket.moveSummon(chr.getId(), sum.getObjectId(), gatherDuration, nVal1, mPos, oPos, res), sum.getPosition());
        }
    }

    public static void DamageSummon(MaplePacketReader slea, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null) {
            return;
        }
        int sumoid = slea.readInt(); //召喚獸的工作ID
        MapleSummon summon = chr.getMap().getSummonByOid(sumoid);
        if (summon == null || summon.getOwnerId() != chr.getId() || summon.getSkillLevel() <= 0 || !chr.isAlive()) {
            return;
        }
        int type = slea.readByte(); //受到傷害的類型
        int damage = slea.readInt(); //受到傷害的數字
        int monsterIdFrom = slea.readInt(); //怪物的ID
        final boolean b = slea.readByte() != 0;
        MapleMonster monster = null;
        if (slea.available() >= 4 && (monster = chr.getMap().getMonsterByOid(slea.readInt())) == null) {
            return;
        }
        MapleStatEffect effect;
        if (damage > 0) {
            if (monster != null && summon.getSkillId() == 神射手.幻像箭影 && (effect = summon.getEffect()) != null) {
                final AttackInfo ai = new AttackInfo();
                ai.skillId = summon.getSkillId();
                ai.display = 134;
                ai.numAttackedAndDamage = 17;
                ai.mobCount = 1;
                ai.hits = 1;
                long theDmg = (long) effect.getY() * damage / 100;
                AttackMobInfo mai = new AttackMobInfo();
                mai.mobId = monster.getObjectId();
                mai.damages = new long[]{theDmg};
                ai.mobAttackInfo.add(mai);
                chr.getMap().broadcastMessage(chr, SummonPacket.summonAttack(chr, sumoid, ai, true), true);
                monster.damage(chr, summon.getSkillId(), theDmg, false);
            }
            summon.addSummonHp(-damage);
            if (summon.getSummonHp() <= 0) {
                chr.dispelEffect(summon.getSkillId());
            }
        }
        if (summon.getSkillId() == 影武者.幻影替身 && (effect = chr.getSkillEffect(影武者.暗影迴避)) != null && chr.getCheatTracker().canNextShadowDodge()) {
            effect.unprimaryPassiveApplyTo(chr);
        }
        chr.getMap().broadcastMessage(chr, SummonPacket.damageSummon(chr.getId(), sumoid, type, damage, monsterIdFrom, b), false);
    }

    /**
     * Summom attack v257
     */
    public static void UserSummonAttack(final MaplePacketReader slea, final MapleClient c, final MapleCharacter chr) {
        MapleMap map = chr.getMap();
        int objid = slea.readInt();
        MapleMapObject obj = map.getMapObject(objid, MapleMapObjectType.SUMMON);
        if (!(obj instanceof MapleSummon summon)) {
            chr.dropMessage(5, "召喚獸的持續時間到而消失..");
            return;
        }
        AttackInfo ai = DamageParse.parseSummonAttack(slea, chr);
        if (ai == null) {
            c.sendEnableActions();
            return;
        }
        if (summon.getOwnerId() != chr.getId() || summon.getSkillLevel() <= 0 || !chr.isAlive()) {
            chr.dropMessage(5, "出現錯誤.");
            return;
        }

        int skillId = summon.getSkillId();
        switch (summon.getSkillId()) {
            case 黑騎士.追隨者: {
                final SecondaryStatValueHolder mbsvh;
                if ((mbsvh = chr.getBuffStatValueHolder(SecondaryStat.Beholder)) == null) {
                    break;
                }
                skillId = mbsvh.sourceID;
                if (ai.skillId > 0) {
                    skillId = ai.skillId;
                    break;
                }
                break;
            }
            case 狂豹獵人.召喚美洲豹_銀灰:
            case 狂豹獵人.召喚美洲豹_暗黃:
            case 狂豹獵人.召喚美洲豹_血紅:
            case 狂豹獵人.召喚美洲豹_紫光:
            case 狂豹獵人.召喚美洲豹_深藍:
            case 狂豹獵人.召喚美洲豹_傑拉:
            case 狂豹獵人.召喚美洲豹_白雪:
            case 狂豹獵人.召喚美洲豹_歐尼斯:
            case 狂豹獵人.召喚美洲豹_地獄裝甲: {
                final int jaguar = chr.getSpecialStat().getJaguarSkillID();
                if (jaguar > 0) {
//                    skillId = jaguar;
                    break;
                }
                break;
            }
            case 聖魂劍士.ATTACK_極樂之境_II: {
                chr.removeSummon(summon, 5);
                break;
            }
            case 主教.天秤之使_1: {
                ai.skillId = 主教.天秤之使_1;
                break;
            }
        }
        int linkSkillId = SkillConstants.getLinkedAttackSkill(skillId);
        Skill skill = SkillFactory.getSkill(skillId);
        if (skill == null) {
            return;
        }
        ai.attackType = AttackInfo.AttackType.SummonedAttack;
        SummonSkillEntry sse = SkillFactory.getSummonData(skillId);
        int attackCount = 0;
        if (sse != null) {
            attackCount = sse.attackCount;
            ai.summonMobCount = sse.mobCount;
        }
        MapleStatEffect effect;
        if (ai.skillId <= 0) {
            ai.skillId = linkSkillId;
            effect = chr.getSkillEffect(linkSkillId);
        } else {
            effect = chr.getSkillEffect(ai.skillId);
        }
        if (effect == null) {
            SecondaryStatValueHolder holder = chr.getBuffStatValueHolder(SecondaryStat.IndieBuffIcon, linkSkillId);
            if (holder != null) {
                effect = holder.effect;
            }
        }
        if (effect == null) {
            chr.dropMessage(5, "召喚獸攻擊處理出錯。effect==null:" + (effect == null) + " 技能ID：" + skillId);
            return;
        }
        switch (effect.getSourceId()) {
            case 黑騎士.追隨者衝擊:
            case 黑騎士.追隨者衝擊II:
            case 黑騎士.追隨者衝擊III:
                if (chr.isSkillCooling(黑騎士.追隨者衝擊)) {
                    return;
                }
                int cooldown = effect.getCooldown(chr);
                if (cooldown > 0) {
                    chr.registerSkillCooldown(黑騎士.追隨者衝擊, cooldown, true);
                }
                break;
            case 火毒.火炎神之怒號:
            case 黑騎士.闇靈衝擊:
                if (!chr.isSkillCooling(effect.getSourceId())) {
                    if (effect.getCooldown(chr) > 0) {
                        chr.registerSkillCooldown(effect, true);
                    }
                }
                break;
        }
        attackCount = Math.max(attackCount, effect.getAttackCount(chr));
        ai.summonMobCount = Math.max(ai.summonMobCount, effect.getMobCount(chr));
        DamageParse.calcDamage(ai, chr, attackCount, effect);
        chr.getMap().broadcastMessage(chr, SummonPacket.summonAttack(chr, summon.getObjectId(), ai, false), false);
        DamageParse.applyAttack(ai, skill, chr, effect, true);
        chr.getSpecialStat().setJaguarSkillID(0);
        switch (summon.getSkillId()) {
            case 機甲戰神.機器人工廠_機器人:
            case 陰陽師.式神炎舞_1:
            case 劍豪.嘯月五影劍:
            case 隱月.鬼武陣_1:
                chr.removeSummon(summon, ai.display);
                break;
        }
        if (ai.unInt1 > 0 && linkSkillId == 機甲戰神.磁場) {
            chr.removeSummonBySkillID(機甲戰神.磁場, 0);
            chr.removeSummonBySkillID(機甲戰神.磁場, 0);
            chr.removeSummonBySkillID(機甲戰神.磁場, 0);
        }
    }

    public static void RemoveSummon(MaplePacketReader slea, MapleClient c) {
        int objid = slea.readInt();
        MapleMapObject obj = c.getPlayer().getMap().getMapObject(objid, MapleMapObjectType.SUMMON);
        if (obj == null || !(obj instanceof MapleSummon summon)) {
            return;
        }
        if (summon.getOwnerId() != c.getPlayer().getId() || summon.getSkillLevel() <= 0) {
            c.getPlayer().dropMessage(5, "移除召喚獸出現錯誤.");
            return;
        }
        if (c.getPlayer().isDebug()) {
            c.getPlayer().dropSpouseMessage(UserChatMessageType.管理員對話, "收到移除召喚獸信息 - 召喚獸技能ID: " + summon.getSkillId() + " 技能名字 " + SkillFactory.getSkillName(summon.getSkillId()));
        }
        if (summon.getSkillId() == 機甲戰神.磁場) {
            return;
        }
        c.getPlayer().getMap().broadcastMessage(SummonPacket.removeSummon(summon, false));
        c.getPlayer().getMap().removeMapObject(summon);
        c.getPlayer().removeVisibleMapObjectEx(summon);
        c.getPlayer().removeSummon(summon);
        c.getPlayer().dispelSkill(summon.getSkillId());
        if (summon.is天使召喚獸()) {
            int buffId = summon.getSkillId() % 10000 == 1087 ? 2022747 : summon.getSkillId() % 10000 == 1179 ? 2022823 : 2022746;
            c.getPlayer().dispelBuff(buffId); //取消天使加的BUFF效果
        }
        if (summon.getSkillId() == 天使破壞者.能量爆炸) {
            c.getPlayer().send(MaplePacketCreator.userBonusAttackRequest(天使破壞者.能量爆炸, 0, Collections.emptyList()));
        }
    }

    public static void SummonedSkill(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        MapleMapObject obj = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.SUMMON);
        if (!(obj instanceof MapleSummon sum)) {
            return;
        }
        int skillId = slea.readInt();
        chr.updateTick(slea.readInt());
        if (slea.available() > 0L) {
            slea.readByte();
        }
        Skill skill = SkillFactory.getSkill(skillId);
        if (sum.getOwnerId() != chr.getId() || sum.getSkillLevel() <= 0 || !chr.isAlive() || skill == null) {
            return;
        }
        MapleStatEffect effect = skill.getEffect(SkillConstants.is召喚獸戒指(sum.getSkillId()) ? 1 : chr.getSkillLevel(SkillConstants.getLinkedAttackSkill(skillId)));
        if (effect == null) {
            chr.dropMessage(6, "[Summon Skill] Skill effect is null, SkillId:" + skillId);
            return;
        }
        if (chr.isDebug()) {
            chr.dropMessage(6, "[Summon Skill] " + effect);
        }
        switch (skillId) {
            case 黑騎士.追隨者: {
                int hpHeal = Math.min(2000, effect.getHp() * chr.getLevel());
                if (chr.getSkillLevel(黑騎士.追隨者_治療強化傷害) > 0) {
                    hpHeal = chr.getStat().getCurrentMaxHP() * 10 / 100;
                }
                chr.addHPMP(hpHeal, 0, false, true);
                chr.getMap().broadcastMessage(chr, SummonPacket.summonSkill(chr.getId(), sum.getObjectId(), 13), true);
                chr.send(EffectPacket.encodeUserEffectLocal(skillId, EffectOpcode.UserEffect_SkillAffected, chr.getLevel(), effect.getLevel()));
                chr.getMap().broadcastMessage(chr, EffectPacket.onUserEffectRemote(chr, skillId, EffectOpcode.UserEffect_SkillAffected, chr.getLevel(), effect.getLevel()), false);
                break;
            }
            case 黑騎士.黑暗守護: {
                effect.unprimaryPassiveApplyTo(chr);
                chr.getMap().broadcastMessage(chr, SummonPacket.summonSkill(chr.getId(), sum.getObjectId(), 12), true);
                chr.send(EffectPacket.encodeUserEffectLocal(skillId, EffectOpcode.UserEffect_SkillAffected, chr.getLevel(), effect.getLevel()));
                chr.getMap().broadcastMessage(chr, EffectPacket.onUserEffectRemote(chr, skillId, EffectOpcode.UserEffect_SkillAffected, chr.getLevel(), effect.getLevel()), false);
                break;
            }
            case 機甲戰神.機器人工廠_RM1: {
                if (chr.canSummon(2000)) {
                    for (int i = 0; i < 3; i++) {
                        chr.getSkillEffect(機甲戰神.機器人工廠_機器人).applyTo(chr, sum.getPosition(), true);
                    }
                }
                break;
            }
            case 機甲戰神.輔助機器_H_EX:
            case 機甲戰神.輔助機器強化: {
                Party party = chr.getParty();
                Rectangle rect = effect.calculateBoundingBox(sum.getPosition(), sum.isFacingLeft());
                if (party != null) {
                    for (PartyMember member : party.getMembers()) {
                        if (member.getChr() != null && member.getChr().getMap() == chr.getMap() && rect.contains(member.getChr().getPosition())) {
                            member.getChr().addHPMP(effect.getHcHp(), 0);
                        }
                    }
                    break;
                }
                if (rect.contains(chr.getPosition())) {
                    chr.addHPMP(effect.getHcHp(), 0);
                    break;
                }
                break;
            }
            case 隱月.鬼武陣: {
                for (int j = 0; j < 2; ++j) {
                    if (chr.getSummonCountBySkill(隱月.鬼武陣_1) <= 10) {
                        chr.getSkillEffect(隱月.鬼武陣_1).applyTo(chr, sum.getPosition(), true);
                    }
                }
                break;
            }
            case 夜使者.達克魯的秘傳:
                chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(MapleForceFactory.getInstance().getMapleForce(chr, effect, 0)), true);
                break;
            case 伊利恩.水晶技能_和諧連結: {
                chr.getMap().broadcastMessage(chr, SummonPacket.SummonedSkillState(sum, 1), true);
                effect.applyTo(chr);
                sum.setState(1, 0);
                chr.getMap().broadcastMessage(chr, SummonPacket.SummonedSkillState(sum, 2), true);
                chr.getMap().broadcastMessage(chr, SummonPacket.SummonedStateChange(sum, 3, 0, 0), true);
                break;
            }
            case 伊利恩.水晶技能_德烏斯: {
                if (chr.getSummonCountBySkill(伊利恩.水晶技能_德烏斯_1) < 5) {
                    chr.getSkillEffect(伊利恩.水晶技能_德烏斯_1).applyTo(chr, sum.getPosition(), true);
                    break;
                }
                break;
            }
            case 主教.天秤之使: {
                SkillFactory.getSkill(主教.天秤之使_2).getEffect(effect.getLevel()).unprimaryPassiveApplyTo(chr);
                break;
            }
            case 槍神.召喚船員_2轉:
            case 槍神.召喚船員_3轉: {
                slea.readPosInt();
                slea.readByte();
                int nCount = slea.readInt();
                List<Integer> mobids = new LinkedList<>();
                for (int i = 0; i < nCount; i++) {
                    mobids.add(slea.readInt());
                }
                List<ForceAtomObject> createList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    ForceAtomObject fobj = new ForceAtomObject(chr.getSpecialStat().gainForceCounter(), 34, i, chr.getId(), 0, 槍神.召喚船員_2轉_1);
                    fobj.Idk3 = 1;
                    if (!mobids.isEmpty()) {
                        fobj.Target = mobids.get(i % mobids.size());
                    }
                    fobj.CreateDelay = 450;
                    fobj.EnableDelay = 480;
                    fobj.Idk1 = 10;
                    fobj.Expire = 4000;
                    fobj.Position = new Point(0, 1);
                    Point p = null;
                    if (fobj.Target > 0) {
                        MapleMonster mob = chr.getMap().getMobObject(fobj.Target);
                        if (mob != null) {
                            p = new Point(mob.getPosition());
                        }
                    }
                    if (p == null) {
                        p = new Point(chr.getPosition());
                    }
                    fobj.ObjPosition = new Point(p);
                    createList.add(fobj);
                }
                if (!createList.isEmpty()) {
                    chr.getMap().broadcastMessage(AdelePacket.ForceAtomObject(chr.getId(), createList, 0), chr.getPosition());
                }
                chr.send(EffectPacket.showSkillAffected(-1, skillId, effect.getLevel(), 0));
                chr.getMap().broadcastMessage(chr, EffectPacket.showSkillAffected(chr.getId(), skillId, effect.getLevel(), 0), false);
                break;
            }
            default: {
                if (SkillConstants.is召喚獸戒指(sum.getSkillId())) {
                    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    final int em = SkillConstants.eM(skillId);
                    final MapleStatEffect eff;
                    if (em > 0 && (eff = ii.getItemEffect(em)) != null) {
                        eff.applyTo(chr, null, true);
                    }
                }
            }
        }
        c.announce(EffectPacket.showSkillAffected(0, skillId, 1, 0));
        chr.getMap().broadcastMessage(chr, EffectPacket.showSkillAffected(chr.getId(), skillId, 1, 0), chr.getPosition());

    }

    public static void SkillPetMove(MaplePacketReader slea, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        final int index = slea.readInt();
        slea.readByte();
        final int gatherDuration = slea.readInt();
        final int nVal1 = slea.readInt();
        final Point mPos = slea.readPos();
        final Point oPos = slea.readPos();
        List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 7);

        if (res != null && !res.isEmpty()) {
            if (slea.available() != 0) {
                log.error("slea.available() != 0 (花狐移動錯誤) 封包: " + slea.toString(true));
                return;
            }
            MapleSkillPet skillPet = chr.getSkillPet();
            if (skillPet == null || skillPet.getObjectId() != index) {
                return;
            }
            MovementParse.updatePosition(res, skillPet, 0);
            chr.getMap().broadcastMessage(chr, SummonPacket.moveSkillPet(chr.getId(), index, gatherDuration, nVal1, mPos, oPos, res), false);
        }
    }

    public static void FoxManActionSetUseRequest(MaplePacketReader slea, MapleCharacter chr) {
        Point pos = slea.readPos();
        int skillType = slea.readInt();
        byte unk2 = slea.readByte();
        byte unk3 = slea.readByte();
        byte unk4 = slea.readByte();
        MapleSkillPet haku = chr.getHaku();
        if (haku == null || haku.getState() != 2) {
            return;
        }
        Skill bHealing = SkillFactory.getSkill(陰陽師.幻醒_花狐);
        int bHealingLvl = chr.getTotalSkillLevel(bHealing);
        boolean forth = true;
        if (bHealingLvl <= 0 || bHealing == null) {
            bHealing = SkillFactory.getSkill(陰陽師.影朋_花狐);
            bHealingLvl = chr.getTotalSkillLevel(bHealing);
            forth = false;
        }
        if (bHealingLvl <= 0 || bHealing == null) {
            return;
        }

        int skillId = 0;
        switch (skillType) {
            case 1:
                if (forth) {
                    skillId = 陰陽師.花狐的恢復_貳;
                } else {
                    skillId = 陰陽師.花狐的回復;
                }
                break;
            case 3:
                if (forth) {
                    skillId = 陰陽師.花炎結界2;
                } else {
                    skillId = 陰陽師.花炎結界;
                }
                break;
            case 4:
                if (forth) {
                    skillId = 陰陽師.花狐的祝福2;
                } else {
                    skillId = 陰陽師.花狐的祝福;
                }
                break;
            case 5:
                if (forth) {
                    skillId = 陰陽師.幽玄氣息2;
                } else {
                    skillId = 陰陽師.幽玄氣息;
                }
                break;
            default:
                chr.dropMessage(1, "[Error]請將訊息反饋給管理員:\r\n[" + pos.x + "," + pos.y + "][" + skillType + "][" + unk2 + "][" + unk3 + "][" + unk4 + "]");
                return;
        }
        if (chr.isSkillCooling(skillId)) {
            return;
        }
        Skill skill = SkillFactory.getSkill(skillId);
        if (skill == null) {
            return;
        }
        MapleStatEffect effect = skill.getEffect(bHealingLvl);
        if (effect == null) {
            return;
        }
        effect.applyTo(chr);
        if (effect.getCooldown(chr) > 0) {
            chr.registerSkillCooldown(skillId, System.currentTimeMillis(), effect.getCooldown(chr));
        }
        chr.getMap().broadcastMessage(chr, SummonPacket.changeFoxManStace(chr.getId()), true);
        chr.send(EffectPacket.showHakuSkillUse(skillType, -1, bHealingLvl));
        chr.getMap().broadcastMessage(chr, EffectPacket.showHakuSkillUse(skillType, chr.getId(), bHealingLvl), false);
    }

    public static void SummonedMagicAltar(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        int oid = slea.readInt();
        MapleMapObject obj = player.getMap().getMapObject(oid, MapleMapObjectType.SUMMON);
        if (!(obj instanceof MapleSummon)) {
            return;
        }
        SummonedMagicAltarInfo smai = new SummonedMagicAltarInfo();
        smai.action = slea.readByte();
        final int skillId = ((MapleSummon) obj).getSkillId();
        if (skillId == 烈焰巫師.火蜥蜴的惡作劇) {
            smai.a7 = slea.readInt();
        }
        smai.skillId = slea.readInt();
        smai.skillLv = slea.readInt();
        smai.a1 = slea.readInt();
        smai.a2 = slea.readInt();
        smai.a3 = slea.readInt();
        smai.position = slea.readPos();
        smai.area = slea.readRect();
        smai.a4 = slea.readInt();
        smai.a5 = slea.readByte();
        smai.a6 = slea.readInt();
        final ArrayList<Integer> list = new ArrayList<>();
        for (int nCount = slea.readInt(), i = 0; i < nCount; ++i) {
            final SummonedMagicAltarInfo.SubInfo sub = new SummonedMagicAltarInfo.SubInfo();
            slea.readInt(); // skillId
            slea.readInt(); // skillLv
            sub.a1 = slea.readInt();
            sub.a8 = slea.readShort();
            sub.position = slea.readPos();
            sub.a2 = slea.readInt();
            sub.a3 = slea.readByte();
            if (slea.readByte() > 0) {
                sub.b1 = true;
                sub.a4 = slea.readInt();
                sub.a5 = slea.readInt();
            }
            if (slea.readByte() > 0) {
                sub.b2 = true;
                sub.a6 = slea.readInt();
                sub.a7 = slea.readInt();
            }
            list.add(sub.a1);
            smai.subSummon.add(sub);
        }
        c.announce(MaplePacketCreator.VSkillObjectAction(smai.skillId, smai.skillLv, list));
        player.getMap().broadcastMessage(player, SummonPacket.SummonedMagicAltar(player.getId(), oid, smai), false);
    }

    public static void SummonedAction(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        int oid = slea.readInt();
        MapleMapObject obj = player.getMap().getMapObject(oid, MapleMapObjectType.SUMMON);
        if (!(obj instanceof MapleSummon summon)) {
            return;
        }
        if (summon.getOwnerId() != player.getId() || summon.getSkillLevel() <= 0 || !player.isAlive()) {
            return;
        }
        player.getSpecialStat().setJaguarSkillID(0);
    }

    public static void SummonedSarahAction(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        int oid = slea.readInt();
        MapleMapObject obj = player.getMap().getMapObject(oid, MapleMapObjectType.SUMMON);
        if (!(obj instanceof MapleSummon)) {
            return;
        }
        MapleStatEffect effect = null;
        if (player.getSkillEffect(伊利恩.即刻反應_文明爭戰Ⅱ) != null) {
            effect = player.getSkillEffect(伊利恩.即刻反應_文明爭戰Ⅱ);
        } else if (player.getSkillEffect(伊利恩.即刻反應_文明爭戰) != null) {
            effect = player.getSkillEffect(伊利恩.即刻反應_文明爭戰);
        }
        if (effect != null) {
            effect.applyTo(player);
        }
    }

    public static void SummonedJavelinAction(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        int oid = slea.readInt();
        MapleMapObject obj = player.getMap().getMapObject(oid, MapleMapObjectType.SUMMON);
        if (!(obj instanceof MapleSummon)) {
            return;
        }
        slea.readInt();
        slea.readInt();
        final int skillId = slea.readInt();
        if (player.getSkillEffect(skillId) != null && !player.isSkillCooling(skillId)) {
            player.registerSkillCooldown(player.getSkillEffect(skillId), true);
            c.announce(SummonPacket.SummonedCrystalAttack((MapleSummon) obj, skillId));
        }
    }

    // TODO : checkValue
    public static void SkillPetAction(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        slea.readInt();
        player.updateTick(slea.readInt());
        final byte val1 = slea.readByte();
        final byte val2 = slea.readByte();
        if (player.getSkillPet() == null) {
            return;
        }
        player.getMap().broadcastMessage(player, SummonPacket.SkillPetAction(player.getId(), player.getSkillPet().getObjectId(), val1, val2), false);
    }

    public static void SubSummon(MaplePacketReader slea, MapleCharacter player) {
    }
}
