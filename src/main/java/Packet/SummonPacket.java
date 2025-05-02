/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Client.MapleCharacter;
import Config.constants.skills.*;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Config.constants.skills.皇家騎士團_技能群組.聖魂劍士;
import Net.server.maps.MapleDragon;
import Net.server.maps.MapleSkillPet;
import Net.server.maps.MapleSummon;
import Net.server.movement.LifeMovementFragment;
import Net.server.unknown.SummonedMagicAltarInfo;
import Opcode.Headler.OutHeader;
import Server.channel.handler.AttackInfo;
import Server.channel.handler.AttackMobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.List;

/**
 * @author PlayDK
 */
public class SummonPacket {

    private static final Logger log = LoggerFactory.getLogger(SummonPacket.class);

    public static byte[] spawnSummon(MapleSummon summon) {
        MapleCharacter player = summon.getOwner();
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SummonedEnterField.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(summon.getSkillId()); /* 輪迴碑石 80011261 */
        mplew.writeInt(summon.getOwnerLevel()); // chr level5
        mplew.writeInt(summon.getSkillLevel()); // Summon skill level
        mplew.writePos(summon.getPosition());
        mplew.write(summon.getStance());
        mplew.writeShort(summon.getCurrentFH());
        mplew.write(summon.getMovementType().getValue()); //召喚的移動類型
        mplew.write(summon.getAttackType()); // 召喚獸的攻擊類型
        mplew.write(summon.getAnimated()); //是否活動的
        if (summon.is大漩渦()) {
            mplew.writeInt(summon.getMobid());
        } else {
            mplew.writeInt(0);
        }
        mplew.write(0); //未知
        mplew.write(1);
        mplew.writeInt(summon.getShadow());
        mplew.writeInt(0);
        mplew.writeBool(summon.showCharLook());
        if (summon.showCharLook()) {
            summon.getOwner().getAvatarLook().encode(mplew, true);
        }
        switch (summon.getSkillId()) {
            case 機甲戰神.磁場:
                boolean v8 = false;
                mplew.write(v8); // m_nTeslaCoilState
                if (v8) {
                    int v33 = 0;
                    do {
                        mplew.writeShort(0); // pTriangle.x
                        mplew.writeShort(0); // pTriangle.y
                        v33++;
                    } while (v33 < 3);
                }
                break;
            case 皮卡啾.皮卡啾暗影_2:
            case 暗夜行者.Skill_暗影僕從:
            case 暗夜行者.影幻:
            case 暗夜行者.影幻_40:
            case 暗夜行者.影幻_20:
            case 皮卡啾.皮卡啾暗影:
            case 皮卡啾.皮卡啾暗影_1:
            case 聖魂劍士.日月星爆:
            case 暗夜行者.暗影侍從:
            case 精靈遊俠.元素精靈:
            case 精靈遊俠.元素精靈_1:
            case 精靈遊俠.元素精靈_2: {
                int x = 0, y = 0;
                switch (summon.getSkillId()) {
                    case 聖魂劍士.日月星爆: {
                        x = 60;
                        y = 30;
                        break;
                    }
                    case 精靈遊俠.元素精靈: {
                        x = 300;
                        y = 41000;
                        break;
                    }
                    case 精靈遊俠.元素精靈_1: {
                        x = 600;
                        y = 41000;
                        break;
                    }
                    case 精靈遊俠.元素精靈_2: {
                        x = 900;
                        y = 41000;
                        break;
                    }
                    case 暗夜行者.Skill_暗影僕從:
                    case 皮卡啾.皮卡啾暗影: {
                        x = 400;
                        y = 30;
                        break;
                    }
                    case 暗夜行者.影幻_40:
                    case 皮卡啾.皮卡啾暗影_1: {
                        x = 800;
                        y = 60;
                        break;
                    }
                    case 暗夜行者.影幻_20:
                    case 皮卡啾.皮卡啾暗影_2: {
                        x = 1200;
                        y = 90;
                    }
                }
                mplew.writeInt(x);
                mplew.writeInt(y);
                break;
            }
            case 陰陽師.鬼神召喚:
                mplew.writeShort(summon.getPosition().x);
                mplew.writeShort(summon.getPosition().y);
                mplew.writeShort(0);
                mplew.writeShort(0);
                break;
            case 阿戴爾.劍域:
                mplew.writeInt(-1);
                break;
        }

        mplew.write(0);
        int duration = summon.getDuration();
        mplew.writeInt(duration == 2100000000 ? 0 : duration);
        mplew.write(summon.getSkillId() == 陰陽師.雙天狗_左 || summon.getSkillId() == 陰陽師.雙天狗_右 ? 0 : 1);
        mplew.writeInt(summon.isFacingLeft() ? 1 : 0);
        mplew.writeInt(summon.getMoveRange());
        mplew.writeInt(0); // Add
        if (summon.getSkillId() >= 狂豹獵人.召喚美洲豹_銀灰 && summon.getSkillId() - 狂豹獵人.召喚美洲豹_銀灰 <= 8) {
            mplew.writeBool(duration < 2100000000);
            mplew.write(duration < 2100000000 ? 500 : 0);
        }
        boolean b = summon.getSkillId() == 伊利恩.古代水晶 || summon.getSkillId() == 虎影.卷術_吸星渦流;
        mplew.writeBool(b);
        if (b) {
            mplew.writeInt(summon.getAcState1());//this.bJI
            mplew.writeInt(summon.getAcState2());//this.bJJ
        }
        final int[] array;
        switch (summon.getSkillId()) {
            case 重砲指揮官.特種猴子部隊:
            case 重砲指揮官.特種猴子部隊_2:
            case 重砲指揮官.特種猴子部隊_3: {
                array = new int[]{重砲指揮官.特種猴子部隊, 重砲指揮官.特種猴子部隊_2, 重砲指揮官.特種猴子部隊_3};
                break;
            }
            default:
                array = new int[0];
                break;
        }
        mplew.writeInt(array.length);
        for (int i : array) {
            mplew.writeInt(i);
        }

        mplew.writeInt(0);
        mplew.writeInt(-1);
        mplew.write(false);

        // ↓ 抓包 不知道結構
        switch (summon.getSkillId()) {
            case 伊利恩.古代水晶:
                mplew.writeInt(Math.min(summon.getAcState2(), 3));//this.bJJ, 3
                for (int j = 0; j < Math.min(summon.getAcState2(), 3); ++j) {//this.bJJ, 3
                    mplew.writeInt(j + 1);
                    mplew.writeInt(summon.getState(j));//this.bJK[j]
                }
                break;
            case 陰陽師.雙天狗_左:
            case 陰陽師.雙天狗_右:
                mplew.write(0);
                break;
            case 陰陽師.雙天狗_隱藏:
                mplew.writeInt(-1);
                break;
        }
        mplew.write(0);
        mplew.write(0);
        mplew.write(0); /* 增加於版本 V258.2 */
        mplew.write(0); /* 增加於版本 V258.2 */
        mplew.write(0); /* 增加於版本 V258.2 */
        return mplew.getPacket();
    }

    /**
     * 移除召喚獸
     *
     * @param summon
     * @param animated
     * @return
     */
    public static byte[] removeSummon(MapleSummon summon, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SummonedLeaveField.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.write(animated ? 0x05 : summon.getRemoveStatus());

        return mplew.getPacket();
    }

    public static byte[] moveSummon(int chrId, int oid, int gatherDuration, int nVal1, Point mPos, Point oPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SummonedMove.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(oid);
        PacketHelper.serializeMovementList(mplew, gatherDuration, nVal1, mPos, oPos, moves, null);

        return mplew.getPacket();
    }

    public static byte[] summonAttack(final MapleCharacter chr, final int summonOid, final AttackInfo ai, final boolean darkFlare) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SummonedAttack.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(summonOid);
        mplew.writeInt(chr.getLevel()); //好像這個地方不在減去人物等級1級
        mplew.write(ai.display);
        mplew.write(ai.numAttackedAndDamage); //這個地方應該是 numAttackedAndDamage

        for (AttackMobInfo mai : ai.mobAttackInfo) {
            if (mai.damages != null) {
                mplew.writeInt(mai.mobId); // 怪物的工作ID
                if (mai.mobId > 0) {
                    mplew.write(7);
                    for (long damage : mai.damages) {
                        mplew.writeLong(damage);
                    }
                }
            }
        }
        mplew.writeBool(darkFlare); // 是否是黑暗雜耍技能
        mplew.write(0);
//        mplew.writePos(summon.getTruePosition());
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] summonSkill(int chrId, int summonSkillId, int newStance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SummonedSkill.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);
        mplew.writeInt(0);//V.149 new

        return mplew.getPacket();
    }

    public static byte[] SummonedMagicAltar(int cid, int oid, SummonedMagicAltarInfo smai) {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.LP_SummonedSkill);
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.write(smai.action);
        mplew.writeInt(0);
        if (smai.skillId == 烈焰巫師.火蜥蜴的惡作劇) {
            mplew.writeInt(smai.a7);
        }
        mplew.writeInt(smai.skillId);
        mplew.writeInt(smai.skillLv);
        mplew.writeInt(smai.a1);
        mplew.writeInt(smai.a2);
        mplew.writeInt(smai.a3);
        mplew.writePos(smai.position);
        mplew.writeRect(smai.area);
        mplew.writeInt(smai.a4);
        mplew.write(smai.a5);
        mplew.writeInt(smai.a6);
        mplew.writeInt(smai.subSummon.size());
        for (final SummonedMagicAltarInfo.SubInfo sub : smai.subSummon) {
            mplew.writeInt(smai.skillId);
            mplew.writeInt(smai.skillLv);
            mplew.writeInt(sub.a1);
            mplew.writeShort(sub.a8);
            mplew.writePos(sub.position);
            mplew.writeInt(sub.a2);
            mplew.write(sub.a3);
            mplew.writeBool(sub.b1);
            if (sub.b1) {
                mplew.writeInt(sub.a4);
                mplew.writeInt(sub.a5);
            }
            mplew.writeBool(sub.b2);
            if (sub.b2) {
                mplew.writeInt(sub.a6);
                mplew.writeInt(sub.a7);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] damageSummon(int chrId, int sumoid, int type, int damage, int monsterIdFrom, boolean b) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SummonedAttackDone.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(sumoid);
        mplew.write(type);
        mplew.writeInt(damage);
        mplew.writeInt(monsterIdFrom);
        mplew.writeBool(b);

        return mplew.getPacket();
    }

    /**
     * Created by HERTZ on 2023/12/23
     *
     * @notice EX:輪迴碑石落下
     */
    public static byte[] summonedSetAbleResist(int chrId, int sumoid, byte b) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SummonedSetAbleResist.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(sumoid);
        mplew.write(b);
        return mplew.getPacket();
    }

    public static byte[] SummonedAssistAttackRequest(int cid, int summonoid, int n) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SummonedAssistAttackRequest.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonoid);
        mplew.write(n); // tms 263.1 change
        return mplew.getPacket();
    }

    public static byte[] spawnDragon(MapleDragon dragon) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DragonEnterField.getValue());
        mplew.writeInt(dragon.getOwner());
        mplew.writeInt(dragon.getPosition().x);
        mplew.writeInt(dragon.getPosition().y);
        mplew.write(dragon.getStance());
        mplew.writeShort(0);
        mplew.writeShort(dragon.getJobId());

        return mplew.getPacket();
    }

    public static byte[] removeDragon(int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DragonLeaveField.getValue());
        mplew.writeInt(chrId);

        return mplew.getPacket();
    }

    public static byte[] moveDragon(MapleDragon dragon, int gatherDuration, int nVal1, Point mPos, Point oPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DragonMove.getValue());
        mplew.writeInt(dragon.getOwner());
        PacketHelper.serializeMovementList(mplew, gatherDuration, nVal1, mPos, oPos, moves, null);

        return mplew.getPacket();
    }

    public static byte[] spawnSkillPet(MapleSkillPet lw) { //召喚小白
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SkillPetTransferField.getValue());

        mplew.writeInt(lw.getOwner());
        mplew.writeInt(lw.getObjectId());
        mplew.writeInt(lw.getSkillId());
        mplew.write(lw.getState());
        mplew.writePos(lw.getPosition());
        mplew.write(lw.getStance());
        mplew.writeShort(lw.getCurrentFH());

        return mplew.getPacket();
    }

    public static byte[] moveSkillPet(int cid, int oid, int gatherDuration, int nVal1, Point mPos, Point oPos, List<LifeMovementFragment> move) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SkillPetMove.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        PacketHelper.serializeMovementList(mplew, gatherDuration, nVal1, mPos, oPos, move, null);

        return mplew.getPacket();
    }

    public static byte[] SkillPetAction(int cid, int oid, byte val1, byte val2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SkillPetAction.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.write(val1);
        mplew.write(val2);
        mplew.writeMapleAsciiString("");

        return mplew.getPacket();
    }

    public static byte[] FoxManEnterField(MapleSkillPet lw) { //召喚大白
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_FoxManEnterField.getValue());

        mplew.writeInt(lw.getOwner());
        mplew.writeShort(0);
        mplew.writePos(lw.getPosition());
        mplew.write(lw.getStance());
        mplew.writeShort(lw.getCurrentFH());
        mplew.writeInt(lw.getSpecialState());
        mplew.writeInt(lw.getWeapon());

        return mplew.getPacket();
    }

    public static byte[] changeFoxManStace(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_FoxManExclResult.getValue());
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    public static byte[] summonGost(int chrId, int summonOid1, int summonOid2, int skillLevel, Point pos_x, short pos_y) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SUMMON_GHOST.getValue());
        mplew.writeInt(chrId);
        mplew.writeShort(skillLevel);
        mplew.writeInt(summonOid1);
        mplew.writeInt(summonOid2);
        mplew.writePos(pos_x);
        mplew.writeShort(pos_y - 110);
        mplew.writeShort(pos_y - 50);
        return mplew.getPacket();
    }

    public static byte[] SummonedSkillState(MapleSummon summon, int i) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SummonedSkillState.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(i);
        if (i == 2) {
            mplew.writeInt(Math.min(summon.getAcState2(), 3));
            for (i = 0; i < Math.min(summon.getAcState2(), 3); ++i) {
                mplew.writeInt(i + 1);
                mplew.writeInt(summon.getState(i));
            }
        }
        return mplew.getPacket();
    }

    public static byte[] SummonedForceMove(MapleSummon summon, int skillId, int skillLevel, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SummonedForceMove.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(skillId);
        mplew.writeInt(skillLevel);
        mplew.writeInt(pos.x);
        mplew.writeInt(pos.y);
        return mplew.getPacket();
    }

    public static byte[] SummonedForceReturn(int ownerId, int objectId, Point position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_SummonedForceReturn);
        mplew.writeInt(ownerId);
        mplew.writeInt(objectId);
        mplew.writePosInt(position);
        return mplew.getPacket();
    }

    public static byte[] SummonedStateChange(MapleSummon summon, int n, int s1, int s2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SummonedStateChange.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(n);
        switch (n) {
            case 1:
            case 2: {
                mplew.writeInt(s1);
                mplew.writeInt(s2);
                break;
            }
        }
        return mplew.getPacket();
    }

    public static byte[] SummonedSpecialEffect(MapleSummon summon, int i) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SummonedSpecialEffect.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(i);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] SummonedCrystalAttack(MapleSummon summon, int skillID) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SummonedCrystalAttack.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(skillID);
        return mplew.getPacket();
    }

    public static byte[] SummonMagicCircleAttack(MapleSummon summon, int n, Point pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_SummonedMagicCircleAttack);
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(n);
        mplew.writeBool(true);
        mplew.writePosInt(pos);
        return mplew.getPacket();
    }

    public static byte[] FoxManShowChangeEffect(MapleSkillPet pet) {

        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.LP_FoxManShowChangeEffect);
        hh.writeInt(pet.getOwner());
        return hh.getPacket();
    }

    public static byte[] FoxManLeaveField(MapleSkillPet pet) {
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.LP_FoxManLeaveField);
        hh.writeInt(pet.getOwner());
        return hh.getPacket();
    }

    public static byte[] SkillPetState(final MapleSkillPet pet) {
        final MaplePacketLittleEndianWriter hh = new MaplePacketLittleEndianWriter();

        hh.writeOpcode(OutHeader.LP_SkillPetState);
        hh.writeInt(pet.getOwner());
        hh.writeInt(pet.getObjectId());
        hh.write(pet.getState());

        return hh.getPacket();
    }
}
