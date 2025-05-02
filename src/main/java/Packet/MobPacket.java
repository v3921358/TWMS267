package Packet;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MonsterEffectHolder;
import Client.status.MonsterStatus;
import Config.constants.GameConstants;
import Config.constants.skills.冒險家_技能群組.夜使者;
import Net.server.Obstacle;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Net.server.life.MobSkill;
import Net.server.life.MobSkillFactory;
import Net.server.maps.MapleFoothold;
import Net.server.maps.MapleMap;
import Net.server.maps.MapleNodes;
import Net.server.maps.field.DunkelEliteBoss;
import Net.server.movement.LifeMovementFragment;
import Opcode.Headler.OutHeader;
import Server.BossEventHandler.spawnX.MapleDelayedAttack;
import Server.BossEventHandler.spawnX.MapleFlyingSword;
import Server.BossEventHandler.spawnX.MapleIncinerateObject;
import Server.Buffstat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.HexTool;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

/**
 * *
 * 負責生成怪物Buff相關的數據包
 *
 * @author dongjak
 */
public class MobPacket {
    private static final Logger log = LoggerFactory.getLogger(MobPacket.class);

    /**
     * 怪物傷害數字顯示
     */
    public static byte[] damageMonster(int oid, long damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobCtrlHint.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        if (damage > Integer.MAX_VALUE) {
            mplew.writeInt(Integer.MAX_VALUE);
        } else {
            mplew.writeInt((int) damage);
        }
        return mplew.getPacket();
    }

    /**
     * 友好的怪物傷害數字顯示
     */
    public static byte[] damageFriendlyMob(MapleMonster mob, long damage, boolean display) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MobCtrlHint.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.write(display ? 1 : 2); //false for when shammos changes map!
        if (display) {
            mplew.writeLong(mob.getHp());
            mplew.writeLong(mob.getMobMaxHp());
        }
        return mplew.getPacket();
    }

    /**
     * 怪物死亡
     */
    public static void killMonster(MapleCharacter chr, int oid, int animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobLeaveField.getValue());
        mplew.writeInt(oid);
        mplew.write(animation); // 0 = 消失 , 1 = 消退, 2+ = special
        switch (animation) {
            case 0:
            case 1:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.write(0);
                if(chr.getKeyValue("DamageGetKillCount") == "1") {
                    chr.getClient().outPacket(OutHeader.LP_GetKillCount.getValue());
                }
                break;
            case 2: {
                mplew.write(0);
                break;
            }
        }
        if (chr != null) {
            chr.send(mplew.getPacket());
            //
            chr.getClient().outPacket(OutHeader.LP_KillMonsterRec.getValue(), 1128);
            chr.getClient().outPacket(OutHeader.LP_KillMonsterRec.getValue(), 1435);
            chr.getClient().outPacket(OutHeader.LP_KillMonsterRec.getValue(), 1436);
            //
        }
    }

    /**
     * 怪物加血
     */
    public static byte[] healMonster(int oid, int heal) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobCtrlHint.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.writeInt(-heal);
        return mplew.getPacket();
    }

    /**
     * 顯示怪物血量
     */
    public static byte[] showMonsterHP(int oid, int remhppercentage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobHPChange.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(remhppercentage);
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * Created by HERTZ on 2023/12/23
     *
     * @notice v260.4怪物移動
     */
    public static byte[] moveMonster(int oid, boolean useskill, int mode, int skillid, int skilllevel, short effectAfter, List<Pair<Short, Short>> list, Map<Integer, Short> map1, Map<Integer, Integer> map2, int gatherDuration, int nVal1, Point mPos, Point oPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobMove.getValue());
        mplew.writeInt(oid);
        mplew.writeBool(useskill);
        mplew.write(mode);
        mplew.writeShort(skillid);//V.160 byte=>short
        mplew.writeShort(skilllevel);//V.160 byte=>short
        mplew.writeShort(effectAfter);
        mplew.writeShort(0);//V.160 new
        mplew.write(list.size());
        for (Pair<Short, Short> pair : list) {
            mplew.writeShort(pair.getLeft());
            mplew.writeShort(pair.getRight());
        }
        mplew.write(map1.size());
        for (short s : map1.values()) {
            mplew.writeShort(s);
        }
        int unk = 0;
        mplew.writeInt(unk);
        if (unk > 0) {
            // MAP_INFO
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
        mplew.writeInt(0);
        mplew.write(map2.size());
        for (int s : map2.values()) {
            mplew.writeInt(s);
        }

        PacketHelper.serializeMovementList(mplew, gatherDuration, nVal1, mPos, oPos, moves, null);

        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] spawnMonster(MapleMonster monster) { // 363節
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobEnterField.getValue());
        mplew.write(0); // 00
        mplew.writeInt(monster.getObjectId()); // D1 07 0A 00
        mplew.write(1); // 01
        mplew.writeInt(monster.getId()); // 94 49 31 00
        if(monster.isCustom()) {
            mplew.writeBool(monster.isCustom());
            mplew.writeLong(monster.getStats().getHp());
            mplew.writeLong(monster.getStats().getMp());
            mplew.writeInt(monster.getForcedMobStat().getExp());
            mplew.writeInt(monster.getForcedMobStat().getWatk());
            mplew.writeInt(monster.getForcedMobStat().getMatk());
            mplew.writeInt(monster.getForcedMobStat().getPDRate());
            mplew.writeInt(monster.getForcedMobStat().getMDRate());
            mplew.writeInt(monster.getForcedMobStat().getAcc());
            mplew.writeInt(monster.getForcedMobStat().getEva());
            mplew.writeInt(0); //kb
            mplew.writeInt(0); // 100
            mplew.writeInt(monster.getForcedMobStat().getLevel()); // 110
            mplew.writeInt(0);
            mplew.writeInt(0); // v262
            mplew.writeInt(252);
            mplew.writeInt(0);
        }
        mplew.writeInt(0);
        mplew.writeInt(254);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeShort(32767);
        mplew.write(16);
        mplew.writeInt(0);
        //----------------------------------
        mplew.writeInt(252);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        //----------------------------------
        mplew.writeInt(0);
        //----------------------------------
        mplew.writeInt(25113);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        //----------------------------------
        mplew.writeInt(25113);
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
        mplew.writeShort(0);
        //----------------------------------
        mplew.writeInt(25113);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0);
        //----------------------------------
        mplew.writeShort((int) monster.getPosition().getX());
        mplew.writeShort((int) monster.getPosition().getY());
        mplew.write(monster.getStance());
        if (monster.getId() == 8910000 || monster.getId() == 8910100 || monster.getId() == 9990033) {
            mplew.write(0);
        }
        mplew.writeShort(monster.getMobFH()); // FH monster.getFh()
        mplew.writeShort(monster.getMobFH()); // FH monster.getFh()
        short spawnType = monster.getAppearType();

        mplew.write(1);
        mplew.writeShort(spawnType); //(-2 新刷出的怪物 -1 已刷出的怪物)
        if (spawnType == -3 || spawnType >= 0) {
            mplew.writeInt(monster.getLinkOid());
        }
        mplew.write(monster.getCarnivalTeam());
        mplew.writeLong(monster.getHp());
        mplew.writeInt(0);
        if (monster.getStats().isPatrol()) {
            mplew.writeInt(monster.getPatrolScopeX1());
            mplew.writeInt(monster.getPatrolScopeX2());
            mplew.writeInt(monster.getStats().getPatrolDetectX());
            mplew.writeInt(monster.getStats().getPatrolSenseX());
        }
        mplew.writeInt(0);//todo monster.getSeperateSoulSrcOID()
        mplew.writeInt(monster.getZoneDataType());//monster.getZoneDataType()
        mplew.writeInt(monster.getShowMobId());
        mplew.write(monster.getUnk());
        mplew.writeInt(-1);
        mplew.writeInt(0);// V.144 新增
        mplew.writeInt(-1);
        mplew.write(monster.getUnk1()); // isBoss?
        mplew.writeInt(0);//todo monster.getAfterAttack()
        mplew.writeInt(monster.getScale()); // 100
        mplew.writeInt(monster.getEliteGrade());// monster.get怪物類型() -1
        if (monster.getEliteGrade() >= 0) {
            mplew.writeInt(monster.getEliteMobActive().size());
            for (int j : monster.getEliteMobActive()) {
                mplew.writeInt(j);
                mplew.writeInt(0);
            }
            mplew.writeInt(monster.getEliteType());
        }
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(0);// V.155 new
        mplew.writeMapleAsciiString("");// V.155 new
        if (monster.getId() == 8880102) {
            mplew.writeInt(monster.getFollowChrID());
        }
        if (monster.getId() / 10000 == 961) {
            mplew.writeMapleAsciiString(""); // 怪物名稱
        }
        mplew.writeInt(0);

        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        if(monster.getId() / 10 == 88000) {
            if (monster.getStats().isSkeleton()) {
                mplew.write(0);
                mplew.write(0);
                mplew.write(monster.getStats().getHitParts().size());
                for (final Entry<String, Integer> entry : monster.getStats().getHitParts().entrySet()) {
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(1);
                    mplew.writeMapleAsciiString(entry.getKey());
                    mplew.write(0);
                    mplew.write(entry.getValue());
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(0);
                }
            }
        } else {
            mplew.write(0);
            mplew.write(0);
            mplew.write(0);
            mplew.writeInt(0);
            if (monster.isBoss()) {
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
            }
        }
        return mplew.getPacket();
    }

    /**
     * 怪物召喚控制
     */
    public static byte[] monsterChangeController(MapleMonster monster, int mode, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobChangeController.getValue());
        mplew.write(mode); // 00
        mplew.writeInt(monster.getObjectId()); // D1 07 0A 00
        mplew.write(type); // 01
        mplew.writeInt(monster.getId()); // 94 49 31 00
        if(monster.isCustom()) {
            mplew.writeBool(monster.isCustom());
            mplew.writeLong(monster.getStats().getHp());
            mplew.writeLong(monster.getStats().getMp());
            mplew.writeInt(monster.getForcedMobStat().getExp());
            mplew.writeInt(monster.getForcedMobStat().getWatk());
            mplew.writeInt(monster.getForcedMobStat().getMatk());
            mplew.writeInt(monster.getForcedMobStat().getPDRate());
            mplew.writeInt(monster.getForcedMobStat().getMDRate());
            mplew.writeInt(monster.getForcedMobStat().getAcc());
            mplew.writeInt(monster.getForcedMobStat().getEva());
            mplew.writeInt(0); //kb
            mplew.writeInt(0); // 100
            mplew.writeInt(monster.getForcedMobStat().getLevel()); // 110
            mplew.writeInt(0);
            mplew.writeInt(0); // v262
            mplew.writeInt(252);
            mplew.writeInt(0);
        }
        mplew.writeInt(0);
        mplew.writeInt(254);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeShort(32767);
        mplew.write(16);
        mplew.writeInt(0);
        //----------------------------------
        mplew.writeInt(252);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        //----------------------------------
        mplew.writeInt(0);
        //----------------------------------
        mplew.writeInt(25113);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        //----------------------------------
        mplew.writeInt(25113);
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
        mplew.writeShort(0);
        //----------------------------------
        mplew.writeInt(25113);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0);
        //----------------------------------
        mplew.writeShort((int) monster.getPosition().getX());
        mplew.writeShort((int) monster.getPosition().getY());
        mplew.write(monster.getStance()); // 5
        if (monster.getId() == 8910000 || monster.getId() == 8910100 || monster.getId() == 9990033) {
            mplew.write(0);
        }
        mplew.writeShort(monster.getMobFH()); // FH monster.getFh()
        mplew.writeShort(monster.getMobFH()); // FH monster.getFh()
        short spawnType = monster.getAppearType();

        mplew.write(1);
        mplew.writeShort(spawnType); //(-2 新刷出的怪物 -1 已刷出的怪物)
        if (spawnType == -3 || spawnType >= 0) {
            mplew.writeInt(monster.getLinkOid());
        }
        mplew.write(monster.getCarnivalTeam());
        mplew.writeLong(monster.getHp());
        mplew.writeInt(0);
        if (monster.getStats().isPatrol()) {
            mplew.writeInt(monster.getPatrolScopeX1());
            mplew.writeInt(monster.getPatrolScopeX2());
            mplew.writeInt(monster.getStats().getPatrolDetectX());
            mplew.writeInt(monster.getStats().getPatrolSenseX());
        }
        mplew.writeInt(0);//todo monster.getSeperateSoulSrcOID()
        mplew.writeInt(monster.getZoneDataType());//monster.getZoneDataType()
        mplew.writeInt(monster.getShowMobId());
        mplew.write(monster.getUnk());
        mplew.writeInt(-1);
        mplew.writeInt(0);// V.144 新增
        mplew.writeInt(-1);
        mplew.write(monster.getUnk1()); // isBoss?
        mplew.writeInt(0);//todo monster.getAfterAttack()
        mplew.writeInt(monster.getScale()); // 100
        mplew.writeInt(monster.getEliteGrade());// monster.get怪物類型() -1
        if (monster.getEliteGrade() >= 0) {
            mplew.writeInt(monster.getEliteMobActive().size());
            for (int j : monster.getEliteMobActive()) {
                mplew.writeInt(j);
                mplew.writeInt(0);
            }
            mplew.writeInt(monster.getEliteType());
        }
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(0);// V.155 new
        mplew.writeMapleAsciiString("");// V.155 new
        if (monster.getId() == 8880102) {
            mplew.writeInt(monster.getFollowChrID());
        }
        if (monster.getId() / 10000 == 961) {
            mplew.writeMapleAsciiString(""); // 怪物名稱
        }
        mplew.writeInt(0);

        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        if(monster.getId() / 10 == 88000) { // EX:殘暴炎魔
            if (monster.getStats().isSkeleton()) {
                mplew.write(0);
                mplew.write(0);
                mplew.write(monster.getStats().getHitParts().size());
                for (final Entry<String, Integer> entry : monster.getStats().getHitParts().entrySet()) {
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(1);
                    mplew.writeMapleAsciiString(entry.getKey());
                    mplew.write(0);
                    mplew.write(entry.getValue());
                    mplew.write(0);
                    mplew.write(0);
                    mplew.write(0);
                }
            }
        } else {
            mplew.write(0);
            mplew.write(0);
            mplew.write(0);
            mplew.writeInt(0);
            if (monster.isBoss()) {
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
            }
        }
        return mplew.getPacket();
    }



    public static byte[] monsterChangeControllerNew(MapleMonster mob, MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobCtrlNew.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static void writeMonsterEndData(MaplePacketLittleEndianWriter mplew, MapleMonster life) {
        mplew.writePos(life.getPosition());
        mplew.write(life.getStance()); // Bitfield
//        if (life.getStats().isSmartPhase() > 0) {
        if (life.getId() == 8910000 || life.getId() == 8910100 || life.getId() == 9990033) {
            mplew.write(0);
        }
        mplew.writeShort(life.getCurrentFH()); // FH life.getFh()
        mplew.writeShort(life.getHomeFH()); // Origin FH
        short spawnType = life.getAppearType();
        mplew.writeShort(spawnType); //(-2 新刷出的怪物 -1 已刷出的怪物)
        if (spawnType == -3 || spawnType >= 0) {
            mplew.writeInt(life.getLinkOid());
        }
        mplew.write(life.getCarnivalTeam());
        mplew.writeLong(life.getHp());
        mplew.writeInt(0);
        if (life.getStats().isPatrol()) {
            mplew.writeInt(life.getPatrolScopeX1());
            mplew.writeInt(life.getPatrolScopeX2());
            mplew.writeInt(life.getStats().getPatrolDetectX());
            mplew.writeInt(life.getStats().getPatrolSenseX());
        }
        mplew.writeInt(0);//todo life.getSeperateSoulSrcOID()
        mplew.writeInt(life.getZoneDataType());//life.getZoneDataType()
        mplew.writeInt(life.getShowMobId());
        mplew.write(life.getUnk());
        mplew.writeInt(-1);
        mplew.writeInt(0);// V.144 新增
        mplew.writeInt(-1);//todo life.getAfterAttack()
        mplew.write(life.getUnk1()); // isBoss?
        int bUnkSize = 0;
        mplew.writeInt(bUnkSize);
        for (int i = 0; i < bUnkSize; i++) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        mplew.writeInt(life.getScale());
        mplew.writeInt(life.getEliteGrade());// life.get怪物類型()
        if (life.getEliteGrade() >= 0) {
            mplew.writeInt(life.getEliteMobActive().size());
            for (int j : life.getEliteMobActive()) {
                mplew.writeInt(j);
                mplew.writeInt(0);
            }
            mplew.writeInt(life.getEliteType());
        }
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(0);// V.155 new
        mplew.writeMapleAsciiString("");// V.155 new
        if (life.getId() == 8880102) {
            mplew.writeInt(life.getFollowChrID());
        }
        if (life.getId() / 10000 == 961) {
            mplew.writeMapleAsciiString(""); // 怪物名稱
        }
        mplew.writeInt(0);

        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);

        if (life.getStats().isSkeleton()) {
            mplew.write(0);
            mplew.write(0);
            mplew.write(life.getStats().getHitParts().size());
            for (final Entry<String, Integer> entry : life.getStats().getHitParts().entrySet()) {
                mplew.writeMapleAsciiString(entry.getKey());
                mplew.write(0);
                mplew.writeInt(entry.getValue());
            }
        }
    }

    /**
     * 怪物自定義屬性
     *
     * @param mplew
     * @param life
     */
    /**
     * 怪物自定義屬性
     *
     * @param mplew
     * @param life
     */
    public static void addForcedMobStat(MaplePacketLittleEndianWriter mplew, MapleMonster life) {
        boolean writeChangedStats = life.getForcedMobStat() != null;
        mplew.writeBool(writeChangedStats);
        if (writeChangedStats) {
            // long + 12組int
            mplew.writeLong(life.getMobMaxHp());//V.153 Int->Long
            mplew.writeInt(life.getMobMaxMp());
            mplew.writeInt(life.getForcedMobStat().getExp());
            mplew.writeInt(life.getForcedMobStat().getWatk());
            mplew.writeInt(life.getForcedMobStat().getMatk());
            mplew.writeInt(life.getForcedMobStat().getPDRate());
            mplew.writeInt(life.getForcedMobStat().getMDRate());
            mplew.writeInt(life.getForcedMobStat().getAcc());
            mplew.writeInt(life.getForcedMobStat().getEva());
            mplew.writeInt(life.getForcedMobStat().getPushed());
            mplew.writeInt(life.getForcedMobStat().getSpeed()); //V.109.1新增 未知
            mplew.writeInt(life.getForcedMobStat().getLevel());
            mplew.writeInt(life.getForcedMobStat().getUserCount());
            mplew.write(false); // TMS.230
        }
    }

    /**
     * 停止怪物召喚控制
     *
     * @return
     */
    public static byte[] stopControllingMonster(MapleMonster monster, MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobChangeController.getValue());
        mplew.write(0);
        mplew.writeInt(monster.getObjectId());
        return mplew.getPacket();
    }


    //    public static byte[] makeMonsterEffect(MapleMonster life, int effect) {
//        return spawnMonster(life, effect, 0);
//    }
    public static <E extends Buffstat> void writeNewMask(MaplePacketLittleEndianWriter mplew, Collection<E> statups) {
        int[] mask = new int[GameConstants.MAX_MOBSTAT];
        for (E statup : statups) {
            mask[statup.getPosition()] |= statup.getValue();
        }
        for (int aMask : mask) {
            mplew.writeInt(aMask);
        }
    }

    private static void writeMonsterMask(final MaplePacketLittleEndianWriter mplew, final Set<MonsterStatus> set) {
        final int[] mask = new int[GameConstants.MAX_MOBSTAT];
        for (final MonsterStatus status : set) {
            final int position = status.getPosition();
            mask[position] |= status.getValue();
        }
        for (int aMask : mask) {
            mplew.writeInt(aMask);
        }
    }

    public static byte[] cancelMonsterStatus(MapleMonster monster, EnumMap<MonsterStatus, MonsterEffectHolder> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobStatReset.getValue());
        mplew.writeInt(monster.getObjectId());
        writeMonsterMask(mplew, statups.keySet());
        for (Entry<MonsterStatus, MonsterEffectHolder> entry : statups.entrySet()) {
            if (entry.getKey() == MonsterStatus.Burned) {
                final List<MonsterEffectHolder> holders = monster.getIndieEffectHolder(MonsterStatus.Burned);
                switch (entry.getValue().sourceID) {
                    case 夜使者.刺客刻印:
                    case 夜使者.夜使者刻印:
                        mplew.writeInt(1);
                        entry.getValue().dotSuperpos = 0;
                        holders.add(entry.getValue());
                        break;
                    default:
                        mplew.writeInt(0);
                        break;
                }
                mplew.writeInt(holders.size());
                for (MonsterEffectHolder holder : holders) {
                    mplew.writeInt(holder.fromChrID);
                    mplew.writeInt(holder.sourceID);
                    mplew.writeInt(holder.dotSuperpos);
                }
            }
        }
        mplew.write(5);
        for (Entry<MonsterStatus, MonsterEffectHolder> entry : statups.entrySet()) {
            if (entry.getKey().ordinal() < MonsterStatus.PAD.ordinal()) {
                encodeIndieMonsterStatus(mplew, monster, entry.getKey());
            }
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * 顯示操作結果
     */
    public static byte[] showResults(int mobid, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MobCatchEffect.getValue());
        mplew.writeInt(mobid);
        mplew.writeBool(success);
        mplew.write(1);

        return mplew.getPacket();
    }

    /**
     * 撲捉怪物
     */
    public static byte[] catchMonster(int mobid, int itemid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MobStealEffect.getValue());
        mplew.writeInt(mobid);
        mplew.writeInt(itemid);
        mplew.write(success);

        return mplew.getPacket();
    }

    public static byte[] unknown(int moboid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(0x3C9);
        mplew.writeLong(moboid);

        return mplew.getPacket();
    }

    public static byte[] showMobSkillDelay(int moboid, MobSkill mobSkill, int effectAfter, List<Rectangle> rectangles) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobSkillDelay.getValue());
        mplew.writeInt(moboid);
        mplew.writeInt(effectAfter);
        mplew.writeInt(mobSkill.getSourceId());
        mplew.writeInt(mobSkill.getLevel());
        mplew.writeInt(mobSkill.getAreaSequenceDelay());
        mplew.writeInt(0);//V.160 new
        mplew.writeInt(rectangles.size());
        rectangles.forEach(mplew::writeRect);
        return mplew.getPacket();
    }

    public static byte[] showMonsterSpecialSkill(int moboid, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MobForcedSkillAction.getValue());
        mplew.writeInt(moboid);
        mplew.writeInt(1);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] MobRequestResultEscortInfo(MapleMonster mob, MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobRequestResultEscortInfo.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(map.getNodes().size());
        mplew.writeInt(mob.getPosition().x);
        mplew.writeInt(mob.getPosition().y);
        for (MapleNodes.MapleNodeInfo nodeInfo : map.getNodes()) {
            mplew.writeInt(nodeInfo.x);
            mplew.writeInt(nodeInfo.y);
            mplew.writeInt(nodeInfo.attr);
            if (nodeInfo.attr == 2) {
                mplew.writeInt(500);
            }
        }
        mplew.writeZeroBytes(6);
        return mplew.getPacket();
    }

    public static byte[] mobEscortStopEndPermission(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MobEscortStopEndPermmision.getValue());
        mplew.writeInt(oid);

        return mplew.getPacket();
    }

    public static byte[] MobEscortStopSay(final int n, final int n2, final int n3, final String s, final int n4) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        (mplew).writeOpcode(OutHeader.LP_MobEscortStopSay);
        mplew.writeInt(n);
        mplew.writeInt(n3);
        mplew.writeInt(n2);
        mplew.write((n2 >= 100000) ? 1 : 0);
        mplew.write((s != null && s.length() > 0) ? 1 : 0);
        if (s != null && s.length() > 0) {
            mplew.writeMapleAsciiString(s);
        }
        mplew.writeInt(n4);
        return mplew.getPacket();
    }

    public static byte[] getBossHatred(final Map<String, Integer> aggroRank) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AggroRankInfo.getValue());
        mplew.writeInt(aggroRank.get(0));
        // mplew.writeAsciiString(aggroRank.get(0));
        return mplew.getPacket();
    }

    public static byte[] showMonsterNotice(final int chrid, final int type, final String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SmartMobNoticeMsg.getValue());
        MapleCharacter player = MapleCharacter.getCharacterById(chrid);
        // EX:炎魔的手臂彈開攻擊
        mplew.writeInt(0);
        mplew.writeInt(player.getMap().getMonsters().getFirst().getId());
        mplew.writeInt(2);
        mplew.writeInt(4);
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    public static byte[] controlLaser(final int moboid, final int angle, final int x, final boolean isFirstUse) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobLaserControl.getValue());
        mplew.writeInt(moboid);
        mplew.writeInt(10 + Randomizer.nextInt(5));
        mplew.writeInt(x);
        mplew.writeBool(true);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] showMonsterPhaseChange(final int moid, final int reduceDamageType) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobPhaseChange.getValue());
        mplew.writeInt(moid);
        mplew.writeInt(reduceDamageType);
        return mplew.getPacket();
    }

    public static byte[] changeMonsterZone(final int moid, final int reduceDamageType) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobZoneChange.getValue());
        mplew.writeInt(MapleLifeFactory.getMonster(moid).getObjectId());
        mplew.writeInt(reduceDamageType);
        return mplew.getPacket();
    }

    public static byte[] monsterDemianDelayedAttackCreate(final int moboid, final int skilllevel, final Map<Integer, Point> pointMap, final boolean isFacingLeft) {
        return monsterDemianDelayedAttackCreate(moboid, 42, skilllevel, 0, null, pointMap, isFacingLeft);
    }

    public static byte[] monsterDemianDelayedAttackCreate(final int moboid, final int skilllevel, final int n3, final int n4, final Point point, final boolean isFacingLeft) {
        return monsterDemianDelayedAttackCreate(moboid, skilllevel, n3, n4, point, null, isFacingLeft);
    }

    public static byte[] monsterDemianDelayedAttackCreate(final int moboid, final int skilllevel, final int n3, final int n4, final Point point, final Map<Integer, Point> pointMap, final boolean isFacingLeft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MobDemianDelayedAttackCreate.getValue());
        mplew.writeInt(moboid);
        mplew.writeInt(170);
        mplew.writeInt(skilllevel);
        if (skilllevel == 42) {
            mplew.writeBool(isFacingLeft);
            mplew.writeInt(n3);
            mplew.writeInt(pointMap.size());
            for (final Entry<Integer, Point> entry : pointMap.entrySet()) {
                a(mplew, entry.getKey(), entry.getValue());
                mplew.writeInt(Randomizer.rand(73, 95));
            }
        } else if (skilllevel > 44 && skilllevel <= 47) {
            mplew.writeBool(isFacingLeft);
            mplew.writeInt(n3);
            a(mplew, n4, point);
        }

        return mplew.getPacket();
    }

    public static void a(MaplePacketLittleEndianWriter mplew, final int n, final Point point) {
        mplew.writeInt(n);
        mplew.writeInt(point.x);
        mplew.writeInt(point.y);
    }

    public static byte[] teleportMonster(final int moboid, final boolean b, final int n2, final Point point, final int n3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MobTeleport.getValue());
        mplew.writeInt(moboid);
        mplew.writeBool(b);
        if (!b) {
            mplew.writeInt(n2);
            switch (n2) {
                case 3:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 100: {
                    mplew.writePosInt(point);
                    break;
                }
                case 4: {
                    mplew.writeInt(n3);
                    break;
                }
            }
        }

        return mplew.getPacket();
    }

    public static byte[] cancelMonsterAction(final MapleMonster monster, final int n) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MobSuspendReset.getValue());
        mplew.writeInt(monster.getObjectId());
        mplew.write(n);

        return mplew.getPacket();
    }

    public static byte[] mobMoveControl(int oid, Point position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobFlyTarget.getValue());
        mplew.writeInt(oid);
        mplew.writePosInt(position);
        return mplew.getPacket();
    }

    public static byte[] bounceAttackSkill(int oid, final MobSkill skill, Point position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_BounceAttackSkill.getValue());
        final int sourceID = skill.getSourceId();
        final int level = skill.getLevel();
        final boolean b = skill.getX() == 100000;
        mplew.writeInt(oid);
        mplew.writeInt(sourceID);
        mplew.writeInt(level);
        mplew.writeBool(b);
        if (b) {
            mplew.writeInt(1);
            mplew.write(0);
            mplew.writeInt(skill.getY());
            mplew.writeInt(skill.getX());
            mplew.writeInt(skill.getZ());
            mplew.writeInt(skill.getW());
            mplew.writeInt(0);
            for (oid = 0; oid <= 0; ++oid) {
                mplew.writeInt(20);
            }
        } else {
            mplew.writeInt(skill.getX());
            mplew.writeInt(skill.getY());
            mplew.writeInt(5);
            for (int i = 0; i <= 5; ++i) {
                mplew.writeInt(i + 1);
                mplew.writeInt(20);
                mplew.writeInt(20);
            }
            mplew.writeInt(skill.getZ());
            mplew.writeInt(skill.getW());
            mplew.writeInt(skill.getDuration());
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(1);
            mplew.writeBool(skill.getLt2() != null && skill.getRb2() != null);
            if (sourceID == 217 && (level == 3 || level == 4 | level == 21)) {
                mplew.writeInt(position.x);
                mplew.writeInt(position.y);
            }
            if (skill.getLt2() != null && skill.getRb2() != null) {
                mplew.writeInt(skill.getLt2().x);
                mplew.writeInt(skill.getLt2().y);
                mplew.writeInt(skill.getRb2().x);
                mplew.writeInt(skill.getRb2().y);
            }
        }
        return mplew.getPacket();
    }

    /* BOSS Lucid */
    public static byte[] lucidButterflyAttack(final int mode, final List<Integer> list, final Point pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_LUCID_ATTACK_MODE.getValue());
        mplew.writeInt(mode);
        mplew.writeInt(mode);
        switch (mode) {
            case 0: {  /* 創建蝴蝶 */
                mplew.writeInt(1);
                mplew.writeInt(Randomizer.nextInt(8));
                mplew.writeInt(pos.x);
                mplew.writeInt(pos.y);
                break;
            }
            case 1: {  /* 移動蝴蝶 */
                mplew.writeInt(1);
                mplew.writeInt(Randomizer.nextInt(8));
                break;
            }
            case 2: {  /* 蝴蝶攻擊 */
                mplew.writeInt(3);
                mplew.writeInt(list.size());
                list.forEach(mplew::writeInt);
                break;
            }
        }
        return mplew.getPacket();
    }


    /**
     * @主核心版本:257
     * @日期:2024/1/2
     * @PacketAuthor:Hertz
     * @return_露希妲右龍炮
     */

    public static byte[] lucidDragonAttack(MapleClient c, int phase, int posX, int posY, int createPosX, int createPosY, boolean isLeft) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LucidDragonAttack.getValue());
        c.getPlayer().getMap().showWeatherEffectNotice("露希妲召喚了強力的召喚獸.", 222, 2000);
        mplew.writeHexString("01 00 00 00 52 00 00 00 54 FF FF FF 52 00 00 00 10 FD FF FF 00");
        return mplew.getPacket();
    }


    /**
     * @主核心版本:257
     * @日期:2024/1/2
     * @PacketAuthor:Hertz
     * @return_號角充能
     */

    public static byte[] lucidDreamHorn(int count) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_LUCID_DREAM_HORN.getValue());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] lucidDreamHorn2(MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_LUCID_DREAM_HORN.getValue());
        mplew.writeHexString("00 00 00 00 02 00 00 00 01");
        return mplew.getPacket();
    }

    /* 仙塵 */
    public static byte[] SpawnLucidDream(List<Point> pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_LUCID_ATTACK_MODE.getValue());
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeInt(pos.size());
        for (Point p : pos) {
            mplew.writeInt(p.x);
            mplew.writeInt(p.y);
        }
        return mplew.getPacket();
    }


    /* Lucid 花的陷阱 */
    public static byte[] lucidFieldAttack(MapleClient c, final int skillID, final int mode, final int n3, final List<Integer> list, final Point pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_DO_SKILL.getValue());
        mplew.writeInt(skillID);
        mplew.writeInt(mode);
        switch (mode) {
            case 1:
            case 2:
            case 3: {
                mplew.writeInt(n3);
                mplew.writeInt(pos.x);
                mplew.writeInt(pos.y);
                mplew.write(0);
                break;
            }
            case 5: {
                mplew.writeInt(n3);
                mplew.writeInt(list.size());
                list.forEach(mplew::writeInt);
                break;
            }
            case 4:
            case 10: {
                mplew.writeInt(5);
                for (int i = 0; i < 6; ++i) {
                    mplew.writeInt(Randomizer.nextInt(2));
                    mplew.writeInt(Randomizer.nextInt(2));
                    mplew.writeInt(112);
                    mplew.writeInt(Randomizer.nextInt(60) + i * 60);
                }
                break;
            }
            case 6: {
                mplew.writeInt(Randomizer.nextInt(8));
                break;
            }
            case 8: {
                mplew.writeInt(0);
                break;
            }
        }
        return mplew.getPacket();
    }

    /* Lucid 半月攻擊 */
    /*public static byte[] lucidFieldAttack(int startDelay, List<Integer> intervals) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_LUCID_CRESCENT_MOON.getValue());
        mplew.writeInt(238); //包頭 int - 0xEE = 238.
        mplew.writeInt(5);
        mplew.writeInt(startDelay);
        mplew.writeInt(intervals.size());
        for (Iterator<Integer> iterator = intervals.iterator(); iterator.hasNext();) {
            int interval = ((Integer) iterator.next()).intValue();
            mplew.writeInt(interval);
        }
        return mplew.getPacket();
    }*/


    public static byte[] lucidFieldFoothold(final boolean b, List<String> list) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LucidFieldFoothold.getValue());
        mplew.writeBool(b);
        mplew.writeInt(list.size());
        list.forEach(mplew::writeMapleAsciiString);
        return mplew.getPacket();
    }

    public static byte[] lucidSpecialHorn(final boolean b, final int n, final boolean b2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_LUCID_DREAM_HORN.getValue());
        mplew.write(b);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(n);
        mplew.write(b2);
        return mplew.getPacket();
    }

    public static byte[] lucidFieldFly(final boolean end) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LucidFieldFly.getValue());
        mplew.writeBool(end);
        return mplew.getPacket();
    }

    public static byte[] lucidSpecialAttack(final int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LucidSpecialAttack.getValue());
        mplew.writeInt(mode);
        switch (mode) {
            case 0:
                mplew.writeInt(0);
                mplew.writeInt(0);
            case 1:
            case 3:
                mplew.writeInt(Randomizer.rand(30, 45));
                mplew.writeInt(Randomizer.rand(45, 55));
                mplew.writeInt(Randomizer.rand(600, 700));
                mplew.writeInt(Randomizer.rand(10, 30));
                break;
            case 4:
            case 5:
                mplew.writeInt(1);
                mplew.writeInt(1);
                mplew.writeInt(113);
                mplew.writeInt(33);
                mplew.writeInt(1077);
                mplew.writeInt(16);
                mplew.writeInt(15);
                mplew.writeInt(2);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] lucidFieldFootholdBreak(List<String> list) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LucidFieldFootholdBreak.getValue());
        mplew.writeInt(list.size());
        for (String s1 : list) {
            mplew.writeMapleAsciiString(s1);
        }
        return mplew.getPacket();
    }

    public static byte[] mobStatSet(final MapleMonster monster, final Map<MonsterStatus, Integer> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobStatSet.getValue());
        mplew.writeInt(monster.getObjectId());
        Map<MonsterStatus, MonsterEffectHolder> holderMap = monster.getMonsterHolderMap(statups);
        writeMonsterStatusEffectData(mplew, monster, holderMap);
        int count = 1;
        for (MonsterEffectHolder holder : holderMap.values()) {
            if (holder.value > 0) {
                count++;
                break;
            }
        }
        mplew.write(0); // delay 延遲多少毫秒之後顯示頭上debuff
        mplew.write(0);
        mplew.write(8);
        return mplew.getPacket();
    }

    public static void encodeIndieMonsterStatus(MaplePacketLittleEndianWriter mplew, MapleMonster
            monster, MonsterStatus stat) {
        final List<MonsterEffectHolder> holders = monster.getIndieEffectHolder(stat);
        mplew.writeInt(holders.size());
        for (MonsterEffectHolder holder : holders) {
            mplew.writeInt(holder.sourceID);
            mplew.writeInt(holder.value);
            mplew.writeInt(holder.startTime);
            mplew.writeInt(0);
            mplew.writeInt(holder.getLeftTime());
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

    public static void writeMonsterStatusEffectData(MaplePacketLittleEndianWriter mplew, MapleMonster
            monster, Map<MonsterStatus, MonsterEffectHolder> holderMap) {
        writeMonsterMask(mplew, holderMap.keySet());
        for (Entry<MonsterStatus, MonsterEffectHolder> entry : holderMap.entrySet()) {
            if (entry.getKey().ordinal() < MonsterStatus.PAD.ordinal()) {
                encodeIndieMonsterStatus(mplew, monster, entry.getKey());
            } else if (entry.getKey().ordinal() < MonsterStatus.Burned.ordinal()) {
                int level = 0;
                if (entry.getValue().effect instanceof MobSkill) {
                    level = entry.getValue().effect.getLevel();
                }
                int sourceID;
                if (entry.getValue().effect == null || entry.getValue().effect.getSourceId() != entry.getValue().sourceID) {
                    sourceID = entry.getValue().sourceID;
                } else {
                    sourceID = entry.getValue().effect.getSourceId();
                }
                int nValue = entry.getValue().value;
                mplew.writeInt(nValue);
                if (level > 0) {
                    mplew.writeShort(sourceID);
                    mplew.writeShort(level);
                } else {
                    mplew.writeInt(sourceID);
                }
                mplew.writeShort(40);
            }
        }
        if (holderMap.containsKey(MonsterStatus.PDR)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.MDR)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.Speed)) {
            mplew.write(holderMap.get(MonsterStatus.Speed).z);
        }
        if (holderMap.containsKey(MonsterStatus.Freeze)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.BeforeFreeze)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.PCounter)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.MCounter)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.PCounter) || holderMap.containsKey(MonsterStatus.MCounter)) {
            mplew.writeInt(500);
            mplew.write(1);
            mplew.writeInt(500);
        }
        if (holderMap.containsKey(MonsterStatus.ReduceFinalDamage)) {
            mplew.write(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.TotalDamParty)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.Fatality)) {
            mplew.writeInt(holderMap.get(MonsterStatus.Fatality).fromChrID);
            mplew.writeInt(0);
            mplew.writeInt(2 * (holderMap.get(MonsterStatus.Fatality).value / 3));
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.GhostDisposition)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.CurseTransition)) {
            mplew.writeInt(1);
        }
        if (holderMap.containsKey(MonsterStatus.ElementDarkness)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.DeadlyCharge)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.Incizing)) {
            mplew.writeInt(holderMap.get(MonsterStatus.Incizing).fromChrID);
            mplew.writeInt(0); // 10 effect U
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.BMageDebuff)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.BattlePvP_Helena_Mark)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.MultiPMDR)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.ElementResetBySummon)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.BahamutLightElemAddDam)) {
            mplew.writeInt(0);
            mplew.writeInt(holderMap.get(MonsterStatus.BahamutLightElemAddDam).fromChrID);
        }
        if (holderMap.containsKey(MonsterStatus.MultiDamSkill)) {
            mplew.writeInt(holderMap.get(MonsterStatus.MultiDamSkill).value);
        }
        if (holderMap.containsKey(MonsterStatus.LefDebuff)) {
            mplew.writeInt(4);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }

        if (holderMap.containsKey(MonsterStatus.BuffControl)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.BattlePvP_Ryude_Frozen)) {
            mplew.writeInt(0);
        }

        if (holderMap.containsKey(MonsterStatus.Poison)) {
            mplew.writeInt(holderMap.get(MonsterStatus.Poison).fromChrID);
        }
        if (holderMap.containsKey(MonsterStatus.Ambush)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.WindBreakerPinpointPierce)) {
            mplew.writeInt(0);
        }
        if (holderMap.containsKey(MonsterStatus.MobLock)) {
            mplew.writeInt(holderMap.get(MonsterStatus.MobLock).fromChrID);
        }
        if (holderMap.containsKey(MonsterStatus.LWGathering)) {
            mplew.writeInt(0);
        }
        /* 傷痕之劍 HERO */
        if (holderMap.containsKey(MonsterStatus.Panic)) {
            mplew.writeInt(-30);
            mplew.writeInt(-20);
        }
        if (holderMap.containsKey(MonsterStatus.Explosion)) {
            mplew.writeInt(0); // 14181181 ?
        }
        // default
        if (holderMap.containsKey(MonsterStatus.Burned)) {
            final List<MonsterEffectHolder> holders = monster.getIndieEffectHolder(MonsterStatus.Burned);
            mplew.write(holders.size());
            for (final MonsterEffectHolder holder : holders) {
                mplew.writeInt(holder.fromChrID);
                mplew.writeInt(holder.sourceID);
                mplew.writeLong(holder.dotDamage);
                mplew.writeInt(holder.dotInterval);
                mplew.writeInt(holder.getCancelTime());
                mplew.writeInt(holder.getLeftTime() << 1);
                mplew.writeInt(holder.getLeftTime() / holder.dotInterval);
                mplew.writeInt(holder.dotSuperpos);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(300);
                mplew.writeInt(holder.level);
                mplew.writeInt(holder.dotDamage);
                mplew.writeInt(0); // TODO: v256多1個INT, 測試新增， 需要抓封包確認位置
                mplew.writeInt(0);
            }
        }
        // default
        if (holderMap.containsKey(MonsterStatus.BalogDisable)) {
            mplew.write(0);
            mplew.write(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.ExchangeAttack)) {
            mplew.write(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.AddBuffStat)) {
            mplew.write(0);
            if (false) {
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
            }
        }
        // default
        if (holderMap.containsKey(MonsterStatus.LinkTeam)) {
            mplew.writeMapleAsciiString("");
        }
        // default
        if (holderMap.containsKey(MonsterStatus.NEWUNK96)) {
            mplew.writeInt(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.NEWUNK97)) {
            mplew.writeLong(0L);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.NEWUNK98)) {
            mplew.writeInt(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.NEWUNK99)) {
            mplew.writeInt(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.NEWUNK101)) {
            mplew.writeInt(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.NEWUNK102)) {
            mplew.writeInt(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.NEWUNK100)) {
            mplew.writeShort(0);
        }
        // default
        //if (holderMap.containsKey(MonsterStatus.SoulExplosion)) {
        //    mplew.writeInt(0);
        //    mplew.writeInt(0);
        //    mplew.writeInt(0);
        //}
        // default
        if (holderMap.containsKey(MonsterStatus.SeperateSoulP)) {
            MonsterEffectHolder holder = holderMap.get(MonsterStatus.SeperateSoulP);
            mplew.writeInt(holder != null ? holder.value : 0);
            mplew.writeInt(holder != null ? holder.moboid : 0);
            mplew.writeShort(0);
            mplew.writeInt(holder != null ? holder.value : 0);
            mplew.writeInt(holder != null ? holder.sourceID : 0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.SeperateSoulC)) {
            MonsterEffectHolder holder = holderMap.get(MonsterStatus.SeperateSoulC);
            mplew.writeInt(holder != null ? holder.value : 0);
            mplew.writeInt(holder != null ? holder.moboid : 0);
            mplew.writeShort(0);
            mplew.writeInt(holder != null ? holder.value : 0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.TrueSight)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(holderMap.get(MonsterStatus.TrueSight).value);
            mplew.writeInt(holderMap.get(MonsterStatus.TrueSight).sourceID);
            mplew.writeInt(holderMap.get(MonsterStatus.TrueSight).sourceID > 0 ? (int) System.currentTimeMillis() : 0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.Ember)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.StatResetSkill)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
//        // default
//        if (holderMap.containsKey(MonsterStatus.Laser)) {
//            final boolean b = holderMap.get(MonsterStatus.Laser).value != 0;
//            final int n4 = (int) ((System.currentTimeMillis() - holderMap.get(MonsterStatus.Laser).startTime) / 1100.0 * 10.0 % 360.0);
//            mplew.writeInt(b ? holderMap.get(MonsterStatus.Laser).value : 0);
//            mplew.writeShort(b ? 223 : 0);
//            mplew.writeShort(b ? holderMap.get(MonsterStatus.Laser).effect.getLevel() : 0);
//            mplew.writeInt(b ? DateUtil.getTime() : 0);
//            mplew.writeInt(b ? 1 : 0);
//            mplew.writeInt(b ? n4 : 0);
//        }
        // default
        if (holderMap.containsKey(MonsterStatus.Laser)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.Unk_163_Add_107)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.NEWUNK133)) {
            mplew.writeLong(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.ChangeMobAction)) {
            mplew.writeInt(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.NEWUNK132)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeShort(0);
        }
        // default
        if (holderMap.containsKey(MonsterStatus.IndieAddFinalDamSkill)) {
            mplew.writeInt(holderMap.get(MonsterStatus.IndieAddFinalDamSkill).fromChrID);
            mplew.writeInt(holderMap.get(MonsterStatus.IndieAddFinalDamSkill).z);
            mplew.writeInt(holderMap.get(MonsterStatus.IndieAddFinalDamSkill).fromChrID != 0 ? 30 : 0);
        }
//        if (holderMap.containsKey(MonsterStatus.CatKnitting)) {
//            mplew.writeInt(0);
//            mplew.writeInt(0);
//        }
    }

    public static byte[] MobDamaged(MapleMonster monster, int n, long hpHeal, boolean damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_MobDamaged);
        mplew.writeInt(monster.getObjectId());
        mplew.write(n);
        mplew.writeLong(damage ? hpHeal : (-hpHeal));
        if (n != 2 && damage) {
            mplew.writeLong(monster.getHp());
            mplew.writeLong(monster.getMobMaxHp());
        }
        return mplew.getPacket();
    }

    public static byte[] MobAffected(int objectId, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_MobAffected);
        mplew.writeInt(objectId);
        mplew.writeInt(skillId);
        mplew.writeShort(0);
        mplew.writeBool(false);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] MobAttackBlock(int objectId, int i) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_MobAttackBlock);
        mplew.writeInt(objectId);
        mplew.writeInt(i);
        while (i > 0) {
            mplew.writeInt(0);
            i--;
        }
        return mplew.getPacket();
    }

    /* 陰陽師 御身消滅 雜技(混) */
    public static byte[] enableSoulBomb(int unk, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SOUL_BOMB.getValue());
        mplew.writeShort(unk);
        mplew.write(1);
        mplew.writePos(pos);
        return mplew.getPacket();
    }

    public static byte[] enableOnlyFsmAttack(MapleMonster mob, int skill, int unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ENABLE_ONLYFSM_ATTACK.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(skill);
        mplew.writeInt(unk);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] forcedSkillAction(int objectId, int value, boolean unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobForcedSkillAction.getValue());
        mplew.writeInt(objectId);
        mplew.writeInt(value);
        mplew.write(unk);
        return mplew.getPacket();
    }

    public static byte[] mobBarrier(int objectId, int percent) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.MONSTER_BARRIER.getValue());
        mplew.writeInt(objectId);
        mplew.writeInt(0);
        mplew.writeLong(percent);
        return mplew.getPacket();
    }

    public static byte[] mobBarrier(MapleMonster monster) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.MONSTER_BARRIER.getValue());
        mplew.writeInt(monster.getObjectId());
        mplew.writeInt(monster.getShieldPercent());
        mplew.writeLong(monster.getStats().getHp());
        return mplew.getPacket();
    }

    public static byte[] setAfterAttack(int objectid, int afterAttack, int attackCount, MapleCharacter player, boolean left) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobSetAfterAttack.getValue());
        mplew.writeInt(objectid);
        mplew.writeShort(afterAttack);
        mplew.writeInt(attackCount);
        mplew.writeInt(player.getPosition().x);
        mplew.write(left);
        return mplew.getPacket();
    }

    public static byte[] setAttackZakumArm(int oid, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ZAKUM_ATTACK.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(type);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] createObtacleAtom(int count, int type1, int type2, int damage, int speed, MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ObtacleAtomCreate.getValue());
        mplew.write(0);
        mplew.write(count);
        mplew.write(0);
        int n5 = Randomizer.nextInt(200000);
        for (int i2 = 0; i2 < count; ++i2) {
            MapleFoothold foothold = map.getFootholds().getAllRelevants().get(Randomizer.nextInt(map.getFootholds().getAllRelevants().size()));
            int n6 = foothold.getY2();
            int n7 = Randomizer.rand(map.getLeft(), map.getRight());
            Point point = map.calcPointBelow(new Point(n7, n6));
            if (point == null) {
                point = new Point(n7, n6);
            }
            mplew.write(1);
            mplew.writeInt(Randomizer.rand(type1, type2));
            mplew.writeInt(n5 + i2);
            mplew.writeInt((int) point.getX());
            mplew.writeInt(map.getTop());
            mplew.writeInt((int) point.getX());
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()) + 100);
            mplew.writeInt(Randomizer.rand(5, 5));
            mplew.write(0);
            mplew.writeInt(Randomizer.rand(100, 100));
            mplew.writeInt(0);
            mplew.writeInt(Randomizer.rand(500, 1300));
            mplew.writeInt(0);
            mplew.writeInt(damage);//傷害係數
            mplew.writeInt(Randomizer.rand(1, 4));
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()));
            mplew.writeInt(0);//
            mplew.writeInt(0);
            mplew.writeInt(speed);//沒這個就不會掉->速度
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()));
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }
    public static byte[] createObstacle2(MapleMonster mob, Obstacle ob, byte type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ObtacleAtomCreate2.getValue());
        mplew.write(type);
        mplew.write(ob != null);
        if (ob != null) {
            mplew.writeInt(ob.getKey()); // type
            mplew.writeInt(Randomizer.nextInt()); // crc
            mplew.writeInt(ob.getOldPosition().x);
            mplew.writeInt(ob.getOldPosition().y);
            mplew.writeInt(ob.getNewPosition().x);
            mplew.writeInt(ob.getNewPosition().y);
            mplew.writeInt(ob.getRange());
            mplew.writeZeroBytes(17); // 351 new
            mplew.writeInt(ob.getTrueDamage()); // HP% Damage
            mplew.writeInt(ob.getDelay());
            mplew.writeInt(ob.getHeight());
            mplew.writeInt(ob.getVperSec());
            mplew.writeInt(ob.getMaxP()); // 바닥 도달까지 속도
            mplew.writeInt(ob.getLength());
            mplew.writeInt(ob.getAngle());
            mplew.writeInt(ob.getUnk());
            mplew.writeInt(0); // 351 new
            mplew.writeZeroBytes(1); // 351 new
        }

        mplew.writeInt(0); // 351 new
        mplew.writeInt(0); // 351 new

        return mplew.getPacket();
    }


    public static byte[] UseSkill(int objectId, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.FORCE_ACTION.getValue());
        mplew.writeInt(objectId);
        mplew.writeInt((value >= 10) ? (value - 10) : value);
        mplew.write((value >= 10) ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] ShowBlackMageSkill(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.writeShort((type == 42570) ? 42570 : 45386);
        mplew.writeShort(390);
        mplew.write(0);
        mplew.writeInt(2);
        mplew.writeShort((type == 42570) ? 0 : type);
        return mplew.getPacket();
    }

    public static byte[] TeleportMonster(MapleMonster monster_, boolean afterAction, int type, Point point) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobTeleport.getValue());
        mplew.writeInt(monster_.getObjectId());
        mplew.write(afterAction);
        if (!afterAction) {
            mplew.writeInt(type);
            switch (type) {
                case 0:
                case 3:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 14:
                case 16:
                    mplew.writeInt(point.x);
                    mplew.writeInt(point.y);
                    break;
                case 4:
                    mplew.writeInt(0); break;
            }
        }
        return mplew.getPacket();
    }

    public static byte[] mobBarrierEffect(int objectId, String eff, String sound, String ui) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.MONSTER_BARRIER_EFFECT.getValue());
        mplew.writeInt(objectId);
        mplew.write(true);
        mplew.writeMapleAsciiString(eff);
        mplew.writeInt(1);
        mplew.write(true);
        mplew.writeMapleAsciiString(sound);
        mplew.write(true);
        mplew.writeMapleAsciiString(ui);
        mplew.writeInt(-1);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] CreateObstacle3(List<Obstacle> obs) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ObtacleAtomCreate.getValue());
        mplew.writeInt(0);
        mplew.writeInt(obs.size());
        mplew.write(0);
        for (Obstacle ob : obs) {
            mplew.write(1);
            mplew.writeInt(ob.getKey());
            mplew.writeInt(Randomizer.nextInt());
            mplew.writeInt((ob.getOldPosition()).x);
            mplew.writeInt((ob.getOldPosition()).y);
            mplew.writeInt((ob.getNewPosition()).x);
            mplew.writeInt((ob.getNewPosition()).y);
            mplew.writeInt(ob.getRange());
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(ob.getTrueDamage());
            mplew.writeInt(ob.getHeight());
            mplew.writeInt(ob.getVperSec());
            mplew.writeInt(ob.getMaxP());
            mplew.writeInt(ob.getLength());
            mplew.writeInt(ob.getAngle());
            mplew.writeInt(ob.getUnk());
            mplew.writeInt(ob.getDelay());
        }
        mplew.write(0);
        mplew.writeInt(4212352);
        return mplew.getPacket();
    }

    public static byte[] FieldSummonAttack(int type, boolean useskill, Point pos, int objid, List<Pair<Long, Integer>> damageinfo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SPEICAL_SUMMON_ATTACK.getValue());
        mplew.writeInt(type);
        mplew.writeInt(useskill ? 1 : 0);
        mplew.writePosInt(pos);
        mplew.writeInt(objid);
        mplew.writeInt(damageinfo.size());
        int i = 1;
        for (Pair<Long, Integer> info : damageinfo) {
            mplew.writeInt(i);
            mplew.writeLong(info.getLeft().longValue());
            mplew.writeInt(info.getRight().intValue());
            i++;
        }
        return mplew.getPacket();
    }


    public static byte[] FieldSummonTeleport(Point pos, boolean left) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SPEICAL_SUMMON_TELEPORT.getValue());
        mplew.writeInt(3);
        mplew.writePosInt(pos);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] ZakumAttack(MapleMonster mob, String skeleton) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ZAKUM_ATTACK.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeMapleAsciiString(skeleton);
        return mplew.getPacket();
    }

    public static byte[] eliteBossAttack(MapleCharacter chr, MapleMonster dunkel, List<DunkelEliteBoss> eliteBosses, boolean isLeft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.DunkelEliteBossAttak.getValue());

        for (DunkelEliteBoss eb : eliteBosses) {
            mplew.write((eb != null) ? 1 : 0);
            if (eb == null) {
                return mplew.getPacket();
            }
            chr.getMap().showWeatherEffectNotice("親衛隊長頓凱爾：接招吧！我的究極劍氣連星星都能劈開！", 272, 5000);
            mplew.writeShort(eb.getbosscode());
            mplew.writeShort(eb.getv1());
            mplew.writeInt(eb.getUnk1());
            mplew.writeInt(eb.getUnk2());
            mplew.writeInt(eb.getUnk3());
            mplew.writeInt(eb.getUnk4());
            mplew.writeInt(dunkel.getObjectId());
            mplew.writeInt((chr != null) ? chr.getId() : 0);
            mplew.writeInt(isLeft ? 0 : 1);
            mplew.writeInt(eb.getUnk5());
            mplew.writeInt(eb.getUnk6());
            mplew.writeInt(eb.getUnk7());
            mplew.writeShort(eb.getv2());
            mplew.writeShort(eb.getArrowvelocity());
            mplew.writeShort(eb.getv3());
            mplew.write(eb.getb1());
            mplew.write(eb.getb2());
            mplew.writePosInt(eb.getP1());
            mplew.writePosInt(eb.getP2());
            mplew.writePosInt(eb.getP3());
            mplew.writeShort(eb.getv4());
            for (int i = 0; i < eb.getv4(); i++) {
                mplew.writePosInt(new Point(Randomizer.rand(-782, 774), 29));
            }
            mplew.writeShort(eb.getv5());
            if (eb.getv5() == 2) {
                mplew.write(HexTool.getByteArrayFromHexString("49 FD FF FF FB FF FF FF 00 00 00 00 E4 02 00 00 FB FF FF FF 01 00 00 00"));
            }
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] changeMobZone(MapleMonster mob) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobZoneChange.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(mob.getPhase());
        return mplew.getPacket();
    }

    public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel, int attackIdx) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobCtrlAck.getValue());
        mplew.writeInt(objectid);
        mplew.writeShort(moveid);
        mplew.write(useSkills);
        mplew.writeInt(currentMp);
        mplew.writeInt(skillId);
        mplew.writeShort(skillLevel);
        mplew.writeInt(attackIdx);
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] BlockAttack(MapleMonster mob, List<Integer> ids) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BLOCK_ATTACK.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(ids.size());
        for (Integer a : ids) {
            mplew.writeInt(a.intValue());
        }
        return mplew.getPacket();
    }

    public static byte[] CorruptionChange(byte phase, int qty) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_DemianCorruptionChange.getValue());
        mplew.write(phase);
        mplew.writeInt(qty);
        return mplew.getPacket();
    }


    public static byte[] StigmaImage(MapleCharacter chr, boolean down) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ADD_STIGMA.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(down ? 0 : 1);
        return mplew.getPacket();
    }

    public static byte[] FlyingSwordTarget(MapleFlyingSword mfs) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_DEMIAN_FIELD_TARGET.getValue());
        mplew.writeInt(mfs.getObjectId());
        mplew.writeInt(mfs.getTarget().getId());
        return mplew.getPacket();
    }

   /* public static byte[] FlyingSwordMakeEnterRequest(MapleFlyingSword mfs) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DemianFlyingSwordMakeEnterRequest.getValue());
        mplew.writeInt(mfs.getObjectId());
        mplew.writeInt(mfs.getTarget().getId());
        return mplew.getPacket();
    }*/

    public static byte[] onDemianDelayedAttackCreate(int skillId, int skillLevel, MapleDelayedAttack mda) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.DEMIAN_ATTACK_CREATE.getValue());
        mplew.writeInt(mda.getOwner().getObjectId());
        mplew.writeInt(skillId);
        mplew.writeInt(skillLevel);
        switch (skillLevel) {
            case 44:
            case 45:
            case 46:
            case 47:
                mplew.write(mda.isIsfacingLeft());
                mplew.writeInt(1);
                mplew.writeInt(mda.getObjectId());
                mplew.writeInt((mda.getPos()).x);
                mplew.writeInt((mda.getPos()).y);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] onDemianDelayedAttackCreate(MapleMonster mob, int skillId, int skillLevel, List<MapleDelayedAttack> mda) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.DEMIAN_ATTACK_CREATE.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(skillId);
        mplew.writeInt(skillLevel);
        if (skillLevel == 42) {
            mplew.write(mob.isFacingLeft() ? 1 : 0);
            mplew.writeInt(1);
            mplew.writeInt(mda.size());
            for (MapleDelayedAttack att : mda) {
                mplew.writeInt(att.getObjectId());
                mplew.writeInt((att.getPos()).x);
                mplew.writeInt((att.getPos()).y);
                mplew.writeInt(att.getAngle());
            }
        }
        return mplew.getPacket();
    }

    public static byte[] AfterAttack(MapleMonster monster, int skillid, int skilllevel, boolean left, int unk1, int unk2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.DEMIAN_ATTACK_CREATE.getValue());
        mplew.writeInt(monster.getObjectId());
        mplew.writeInt(skillid);
        mplew.writeInt(skilllevel);
        mplew.write(left);
        mplew.writeInt(unk1);
        mplew.writeInt(unk2);
        int x = 0;
        if (skilllevel == 45) {
            x = left ? -680 : 680;
        } else if (skilllevel == 46) {
            x = left ? -482 : 482;
        } else if (skilllevel == 47 || skilllevel == 61) {
            x = left ? -600 : 600;
        }
        mplew.writeInt((monster.getPosition()).x + x);
        mplew.writeInt((monster.getPosition()).y);
        return mplew.getPacket();
    }

    public static byte[] AfterAttacklist(MapleMonster monster, int skillid, int skilllevel, boolean left, int unk1, List<Point> data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.DEMIAN_ATTACK_CREATE.getValue());
        mplew.writeInt(monster.getObjectId());
        mplew.writeInt(skillid);
        mplew.writeInt(skilllevel);
        mplew.write(left);
        mplew.writeInt(unk1);
        mplew.writeInt(data.size());
        int i = 0;
        for (Point a : data) {
            mplew.writeInt(4 + i);
            mplew.writeInt(a.x);
            mplew.writeInt(a.y);
            mplew.writeInt(Randomizer.rand(80, 120));
            i++;
        }
        return mplew.getPacket();
    }


    /**
     * Created by HERTZ on 2023/12/23
     *
     * @notice 召喚墓碑
     */

    public static byte[] incinerateObject(MapleIncinerateObject mio, boolean isSpawn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_DEMIAN_CREAT_TOMBSTONE.getValue());
        mplew.writeHexString("00 00 00 00 2C 04 00 00 10 00 00 00 C4 09 00 00 01 00 00 00 23 00 4D 61 70 2F 4F 62 6A 2F 42 6F 73 73 44 65 6D 69 61 6E 2E 69 6D 67 2F 64 65 6D 69 61 6E 2F 61 6C 74 61 72 00");
        return mplew.getPacket();
    }


    public static byte[] ChangePhaseDemian(MapleMonster mob, int unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.DEMIAN_PHASE_CHANGE.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(unk);
        return mplew.getPacket();
    }


    public static byte[] demianRunaway(MapleMonster monster, byte type, MobSkill mobSkill, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.DEMIAN_RUNAWAY.getValue());
        mplew.writeInt(monster.getObjectId());
        mplew.write(type);

        switch (type) {
            case 0:
                mplew.writeInt(mobSkill.getSkillLevel());
                mplew.writeInt(duration); // 10000ms
                mplew.writeShort(0);
                mplew.write(1);
                break;
            case 1:
                mplew.write(0);
                mplew.writeInt(30); // ?
                mplew.writeInt(mobSkill.getSkillId());
                mplew.writeInt(mobSkill.getSkillLevel());
                break;
        }

        return mplew.getPacket();
    }

    public static void demianAttacked(MaplePacketReader slea, MapleClient c) {
        int objectId = slea.readInt();

        MapleMonster mob = c.getPlayer().getMap().getMonsterByOid(objectId);

        if (mob != null) {
            int skillId = slea.readInt();

            if (skillId == 214)

                MobSkillFactory.getMobSkill(170, 51).applyEffect(c.getPlayer(), mob, 1000, mob.isFacingLeft());
        }
    }

    public static byte[] createObstacle(MapleMonster mob, List<Obstacle> obs, byte type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ObtacleAtomCreate.getValue());
        mplew.writeInt(0); // objectId?
        mplew.writeInt(obs.size());
        mplew.write(type);
        if (type == 4) {
            mplew.writeInt(mob.getId());

            mplew.writeInt(mob.getPosition().x);
            mplew.writeInt(obs.get(0).getOldPosition().y);
            mplew.writeInt(obs.get(0).getHeight());
            mplew.writeInt(0);
        }
        for (Obstacle ob : obs) {
            mplew.write(1);
            mplew.writeInt(ob.getKey());
            mplew.writeInt(Randomizer.nextInt());
            mplew.writeInt(ob.getOldPosition().x);
            mplew.writeInt(ob.getOldPosition().y);
            mplew.writeInt(ob.getNewPosition().x);
            mplew.writeInt(ob.getNewPosition().y);
            mplew.writeInt(ob.getRange());
            mplew.writeZeroBytes(16);
            mplew.writeInt(ob.getTrueDamage());
            mplew.writeInt(ob.getDelay());
            mplew.writeInt(ob.getHeight());
            mplew.writeInt(ob.getVperSec());
            mplew.writeInt(ob.getMaxP()); // 바닥 도달까지 속도
            mplew.writeInt(ob.getLength());
            mplew.writeInt(ob.getAngle());
            mplew.writeInt(ob.getUnk());
            if (type == 5) {
                mplew.writeInt(mob.getId());

                //포지션
                mplew.writeInt(mob.getPosition().x);
                mplew.writeInt(obs.get(0).getOldPosition().y);
                mplew.writeInt(obs.get(0).getHeight());
                mplew.writeInt(0);
            }
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] jinHillahSpirit(int objectId, int cid, Rectangle rect, Point pos, int skillLv) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.JINHILLAH_SPIRIT.getValue());
        mplew.writeHexString("E5 AD 5A 05 07 00 00 00 01 00 00 00 27 01 00 00 F6 00 00 00 03 00 00 00 01 01 00 00 00 30 02 FC FE FA 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 B5 FF FF FF B0 FF FF FF E7 FF FF FF 30 02 00 00 0D 00 00 00 B0 FF FF FF D1 FF FF FF 30 02 00 00 53 00 00 00 B0 FF FF FF 9B 00 00 00 30 02 00 00 53 00 00 00 B0 FF FF FF 9B 00 00 00 30 02 00 00 01 00 00 00 28 01 00 00 F6 00 00 00 03 00 00 00 01 01 00 00 00 E8 FE FC FE F4 01 00 00 00 00 00 00 00 00 00 00 05 00 00 00 AF FF FF FF B0 FF FF FF 09 00 00 00 30 02 00 00 C5 FF FF FF B0 FF FF FF B1 FF FF FF 30 02 00 00 E7 FF FF FF B0 FF FF FF F4 FF FF FF 30 02 00 00 7B 00 00 00 B0 FF FF FF 9A 00 00 00 30 02 00 00 8A 00 00 00 B0 FF FF FF 54 00 00 00 30 02 00 00 01 00 00 00 29 01 00 00 F6 00 00 00 03 00 00 00 01 01 00 00 00 B8 FC FC FE EE 02 00 00 00 00 00 00 00 00 00 00 05 00 00 00 B2 FF FF FF B0 FF FF FF CE FF FF FF 30 02 00 00 F3 FF FF FF B0 FF FF FF C1 FF FF FF 30 02 00 00 2A 00 00 00 B0 FF FF FF 7B 00 00 00 30 02 00 00 4B 00 00 00 B0 FF FF FF 39 00 00 00 30 02 00 00 85 00 00 00 B0 FF FF FF 89 00 00 00 30 02 00 00 01 00 00 00 2A 01 00 00 F6 00 00 00 03 00 00 00 01 01 00 00 00 00 00 FC FE E8 03 00 00 00 00 00 00 00 00 00 00 04 00 00 00 B5 FF FF FF B0 FF FF FF E7 FF FF FF 30 02 00 00 0D 00 00 00 B0 FF FF FF D1 FF FF FF 30 02 00 00 53 00 00 00 B0 FF FF FF 9B 00 00 00 30 02 00 00 53 00 00 00 B0 FF FF FF 9B 00 00 00 30 02 00 00 01 00 00 00 2B 01 00 00 F6 00 00 00 03 00 00 00 01 01 00 00 00 18 01 FC FE E2 04 00 00 00 00 00 00 00 00 00 00 05 00 00 00 AF FF FF FF B0 FF FF FF 09 00 00 00 30 02 00 00 C5 FF FF FF B0 FF FF FF B1 FF FF FF 30 02 00 00 E7 FF FF FF B0 FF FF FF F4 FF FF FF 30 02 00 00 7B 00 00 00 B0 FF FF FF 9A 00 00 00 30 02 00 00 8A 00 00 00 B0 FF FF FF 54 00 00 00 30 02 00 00 01 00 00 00 2C 01 00 00 F6 00 00 00 03 00 00 00 01 01 00 00 00 48 03 FC FE DC 05 00 00 00 00 00 00 00 00 00 00 05 00 00 00 B2 FF FF FF B0 FF FF FF CE FF FF FF 30 02 00 00 F3 FF FF FF B0 FF FF FF C1 FF FF FF 30 02 00 00 2A 00 00 00 B0 FF FF FF 7B 00 00 00 30 02 00 00 4B 00 00 00 B0 FF FF FF 39 00 00 00 30 02 00 00 85 00 00 00 B0 FF FF FF 89 00 00 00 30 02 00 00 01 00 00 00 2D 01 00 00 F6 00 00 00 03 00 00 00 01 01 00 00 00 D0 FD FC FE D6 06 00 00 00 00 00 00 00 00 00 00 05 00 00 00 B2 FF FF FF B0 FF FF FF CE FF FF FF 30 02 00 00 F3 FF FF FF B0 FF FF FF C1 FF FF FF 30 02 00 00 2A 00 00 00 B0 FF FF FF 7B 00 00 00 30 02 00 00 4B 00 00 00 B0 FF FF FF 39 00 00 00 30 02 00 00 85 00 00 00 B0 FF FF FF 89 00 00 00 30 02 00 00");
        return mplew.getPacket();
    }


    public static final byte[] SpeakingMonster(MapleMonster mob, int type, int unk2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobSpeaking.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(type);
        mplew.writeInt(unk2);
        return mplew.getPacket();
    }

    public static byte[] HillaDrainStart(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.HILLA_HP_DRAIN_START.getValue());
        mplew.writeInt(oid);
        return mplew.getPacket();
    }


    public static byte[] SendMobCrcKeyChanged() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CRC_KEY_FOR_MONSTER.getValue());
        mplew.writeShort(-1); //  FF FF
        return mplew.getPacket();
    }
}
