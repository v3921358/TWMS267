package Packet;

import Client.MapleCharacter;
import Client.inventory.Item;
import Net.server.fieldskill.FieldSkill;
import Net.server.fieldskill.FieldSkillFactory;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMist;
import Net.server.maps.MapleNodes;
import Opcode.Headler.OutHeader;
import Server.channel.handler.AttackInfo;
import tools.Randomizer;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.List;

public class CField {

    public static byte[] showWillEffect(MapleCharacter chr, int subeffectid, int skillid, int skillLevel) {
        return showEffect(chr, skillLevel, skillid, 87, subeffectid, 0, (byte) 0, true, null, null, null, null);
    }
    
    

    public static byte[] getUpdateEnvironment(final List<MapleNodes.Environment> list) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DynamicObjUrusSync.getValue());
        mplew.writeInt(list.size());
        for (MapleNodes.Environment mp : list) {
            mplew.writeMapleAsciiString(mp.getName());
            mplew.write(false);
            mplew.writeInt(mp.isShow() ? 1 : 0);
            mplew.writeInt(mp.getX());
            mplew.writeInt(mp.getY());
        }

        return mplew.getPacket();
    }

    public static byte[] getFieldSkillAdd(int skillid, int skilllv, List<Triple<Point, Integer, Integer>> skillinfo, boolean remove) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(remove ? OutHeader.LP_FieldSkillRemove.getValue() : OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(skillid);
        mplew.writeInt(skilllv);
        return mplew.getPacket();
    }


    public static byte[] BlackMageDeathCountEffect() {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(OutHeader.SHOW_BLACK_MAGE_DEATH_COUNT_EFFECT.getValue());
        return packet.getPacket();
    }

    public static byte[] setDeathCount(MapleCharacter chr, int count) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SET_DEAD_COUNT.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(count);
        return mplew.getPacket();
    }

    public static byte[] showDeathCount(MapleCharacter chr, int count) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_EVENT_SET_DEAD_COUNT.getValue());
        mplew.writeShort(count);
        mplew.writeShort(1);
        mplew.writeInt(chr.getId());
        mplew.writeInt(count);
        return mplew.getPacket();
    }

    public static byte[] showEffect(MapleCharacter chr, int oldskillid, int skillid, int effectid, int subeffectid, int subeffectid2, byte direction, boolean own, Point pos, String txt, Item item, AttackInfo at) {
        boolean a;
        int j;
        boolean i;
        int b;
        boolean reset, z;
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (own) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
        }
        mplew.writeInt(chr.getId());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.writeInt(227);
        mplew.writeInt(3);
        return mplew.getPacket();
    }


    public static byte[] sub_1E4D510(MaplePacketLittleEndianWriter mplew, int subeffectid, int skillid, int skillLevel) {
        mplew.write(subeffectid);
        mplew.writeInt(skillid);
        mplew.writeInt(skillLevel);
        return mplew.getPacket();
    }

    public static byte[] sub_1E4DCD0(MaplePacketLittleEndianWriter mplew, int skillId, int skillLv, int type) {
        mplew.writeInt(skillId);
        mplew.writeInt(skillLv);
        if (skillId == 100017) {
            mplew.writeShort(type);
        }
        return mplew.getPacket();
    }

    public static byte[] DamagePlayer2(int dam) {
        MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
        pw.writeShort(OutHeader.DAMAGE_PLAYER2.getValue());
        pw.writeInt(dam);
        return pw.getPacket();
    }

    public static byte[] getFieldLaserAdd(int skillid, int skilllv, List<Triple<Point, Integer, Integer>> info) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(skillid);
        mplew.writeInt(skilllv);
        mplew.writeInt(info.size());
        for (Triple<Point, Integer, Integer> sinfo : info) {
            mplew.writePosInt(sinfo.getLeft());
            mplew.writeInt(sinfo.getMid().intValue());
            mplew.writeInt(sinfo.getRight().intValue());
        }
        return mplew.getPacket();
    }
    
    public static byte[] getFieldFinalLaserAdd(int skillid, int skilllv, List<Triple<Point, Point, Integer>> info, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(skillid);
        mplew.writeInt(skilllv);
        mplew.writeInt(info.size());
        mplew.writeInt((skillid == 100016) ? 1400 : 2700);
        mplew.write(1);
        for (Triple<Point, Point, Integer> sinfo : info) {
            mplew.writePosInt(sinfo.getLeft());
            mplew.writePosInt(sinfo.getMid());
            mplew.writeInt(sinfo.getRight().intValue());
            mplew.writeInt(delay);
        }
        return mplew.getPacket();
    }
    
    public static byte[] getFieldSkillEffectAdd(int skillid, int skilllv, int mobid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(skillid);
        mplew.writeInt(skilllv);
        mplew.writeInt(mobid);
        mplew.writeInt(0);
        return mplew.getPacket();
    }


    public static byte[] getFieldSkillAdd(int skillid, int skilllv, boolean remove) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(remove ? OutHeader.LP_FieldSkillRemove.getValue() : OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(skillid);
        mplew.writeInt(skilllv);
        return mplew.getPacket();
    }

    public static byte[] getFieldSkillEffectAdd(int skillid, int skilllv, List<Point> startPoint) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(skillid);
        mplew.writeInt(skilllv);
        mplew.writeInt(startPoint.size());
        for (Point sp : startPoint) {
            mplew.writePosInt(sp);
        }
        return mplew.getPacket();
    }

    public static byte[] getFieldFootHoldAdd(int skillid, int skilllv, List<Triple<Point, String, Integer>> info, boolean remove) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(remove ? OutHeader.LP_FieldSkillRemove.getValue() : OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(skillid);
        mplew.writeInt(skilllv);
        mplew.writeInt(info.size());
        for (Triple<Point, String, Integer> sinfo : info) {
            mplew.writePosInt(sinfo.getLeft());
            mplew.writeMapleAsciiString(sinfo.getMid());
            mplew.writeInt(sinfo.getRight().intValue());
        }
        return mplew.getPacket();
    }

    public static byte[] getSelectPower(int type, int code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ActionBarResult.getValue());
        mplew.writeInt(type);
        mplew.writeInt(code);
        switch (type) {
            case 8:
                mplew.writeInt(1);
                mplew.writeInt(80002623);
                mplew.writeInt(3);
                mplew.writeInt(1);
                mplew.writeInt(543169517);
                break;
            case 9:
                mplew.writeInt(80002623);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] useFieldSkill(FieldSkill fieldSkill) {
        int result, i;
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(fieldSkill.getSkillId());
        mplew.writeInt(fieldSkill.getSkillLevel());
        switch (fieldSkill.getSkillId()) {
            case 100008:
                mplew.writeInt(fieldSkill.getSummonedSequenceInfoList().size());
                for (FieldSkill.SummonedSequenceInfo info : fieldSkill.getSummonedSequenceInfoList()) {
                    mplew.writeInt((info.getPosition()).x);
                    mplew.writeInt((info.getPosition()).y);
                }
                break;
            case 100011:
                mplew.writeInt(fieldSkill.getLaserInfoList().size());
                for (FieldSkill.LaserInfo info : fieldSkill.getLaserInfoList()) {
                    mplew.writeInt((info.getPosition()).x);
                    mplew.writeInt((info.getPosition()).y);
                    mplew.writeInt(info.getUnk1());
                    mplew.writeInt(info.getUnk2());
                }
                break;
            case 100013:
                mplew.writeInt(fieldSkill.getEnvInfo().size());
                for (MapleNodes.Environment env : fieldSkill.getEnvInfo()) {
                    mplew.writeInt(env.getX());
                    mplew.writeInt(env.getY());
                    mplew.writeMapleAsciiString(env.getName());
                    mplew.writeInt(env.isShow() ? 1 : 0);
                }
                break;

            case 100014:
            case 100016:
                mplew.writeInt(fieldSkill.getThunderInfo().size());
                mplew.writeInt((fieldSkill.getSkillId() == 100016) ? 1400 : 2700);
                mplew.write(1);
                for (FieldSkill.ThunderInfo th : fieldSkill.getThunderInfo()) {
                    mplew.writePosInt(th.getStartPosition());
                    mplew.writePosInt(th.getEndPosition());
                    mplew.writeInt(th.getInfo());
                    mplew.writeInt(th.getDelay());
                }

            case 100020:
                mplew.writeInt(0);
                mplew.writeInt(0);
                for (FieldSkill.FieldFootHold fh : fieldSkill.getFootHolds()) {
                    mplew.write(true);
                    mplew.writeInt(fh.getDuration());
                    mplew.writeInt(fh.getInterval());
                    mplew.writeInt(fh.getAngleMin());
                    mplew.writeInt(fh.getAngleMax());
                    mplew.writeInt(fh.getAttackDelay());
                    mplew.writeInt(fh.getZ() + fh.getSet());
                    mplew.writeInt(fh.getZ());
                    mplew.writeMapleAsciiString("");
                    mplew.writeMapleAsciiString("");
                    mplew.writeRect(fh.getRect());
                    mplew.writeInt((fh.getPos()).x);
                    mplew.writeInt((fh.getPos()).y);
                    mplew.write(fh.isFacingLeft());
                }
                mplew.write(false);
                break;

            case 100023:
                result = 2;
                mplew.writeInt(8880608);
                mplew.writeInt(1);
                mplew.writeInt(0);
                mplew.writeInt(100);
                mplew.writeInt(80);
                mplew.writeInt(240);
                mplew.writeInt(1530);
                mplew.writeInt(250);
                mplew.writeInt(result);
                for (i = 0; i < result; i++) {
                    if (i == 0) {
                        mplew.writeInt(Randomizer.rand(-864, 10));
                        mplew.writeInt(Randomizer.rand(30, 915));
                        mplew.writeInt(Randomizer.rand(810, 3420));
                    } else {

                        mplew.writeInt(Randomizer.rand(300, 915));
                        mplew.writeInt(Randomizer.rand(-864, 10));
                        mplew.writeInt(Randomizer.rand(810, 3420));
                    }
                }
                break;
            case 100024:
                mplew.writeInt(7);
                mplew.writeInt(Randomizer.rand(0, 6));
                mplew.writeInt(3060);
                mplew.writeInt(2700);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] useFieldSkill(int skillId, int skillLevel) {
        int result, i;
        FieldSkill fieldSkill = FieldSkillFactory.getFieldSkill(skillId, skillLevel);
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(skillId);
        mplew.writeInt(skillLevel);
        switch (skillId) {
            case 100008:
                mplew.writeInt(fieldSkill.getSummonedSequenceInfoList().size());
                for (FieldSkill.SummonedSequenceInfo info : fieldSkill.getSummonedSequenceInfoList()) {
                    mplew.writeInt((info.getPosition()).x);
                    mplew.writeInt((info.getPosition()).y);
                }
                break;

            case 100011:
                mplew.writeInt(fieldSkill.getLaserInfoList().size());
                for (FieldSkill.LaserInfo info : fieldSkill.getLaserInfoList()) {
                    mplew.writeInt((info.getPosition()).x);
                    mplew.writeInt((info.getPosition()).y);
                    mplew.writeInt(info.getUnk1());
                    mplew.writeInt(info.getUnk2());
                }
                break;
            case 100013:
                mplew.writeInt(fieldSkill.getEnvInfo().size());
                for (MapleNodes.Environment env : fieldSkill.getEnvInfo()) {
                    mplew.writeInt(env.getX());
                    mplew.writeInt(env.getY());
                    mplew.writeMapleAsciiString(env.getName());
                    mplew.writeInt(env.isShow() ? 1 : 0);
                }
                break;
            case 100014:
            case 100016:
                mplew.writeInt(fieldSkill.getThunderInfo().size());
                mplew.writeInt((fieldSkill.getSkillId() == 100016) ? 1400 : 2700);
                mplew.write(1);
                for (FieldSkill.ThunderInfo th : fieldSkill.getThunderInfo()) {
                    mplew.writePosInt(th.getStartPosition());
                    mplew.writePosInt(th.getEndPosition());
                    mplew.writeInt(th.getInfo());
                    mplew.writeInt(th.getDelay());
                }

            case 100020:
                mplew.writeInt(0);
                mplew.writeInt(0);
                for (FieldSkill.FieldFootHold fh : fieldSkill.getFootHolds()) {
                    mplew.write(true);
                    mplew.writeInt(fh.getDuration());
                    mplew.writeInt(fh.getInterval());
                    mplew.writeInt(fh.getAngleMin());
                    mplew.writeInt(fh.getAngleMax());
                    mplew.writeInt(fh.getAttackDelay());
                    mplew.writeInt(fh.getZ() + fh.getSet());
                    mplew.writeInt(fh.getZ());
                    mplew.writeMapleAsciiString("");
                    mplew.writeMapleAsciiString("");
                    mplew.writeRect(fh.getRect());
                    mplew.writeInt((fh.getPos()).x);
                    mplew.writeInt((fh.getPos()).y);
                    mplew.write(fh.isFacingLeft());
                }
                mplew.write(false);
                break;

            case 100023:
                result = 2;
                mplew.writeInt(8880608);
                mplew.writeInt(1);
                mplew.writeInt(0);
                mplew.writeInt(100);
                mplew.writeInt(80);
                mplew.writeInt(240);
                mplew.writeInt(1530);
                mplew.writeInt(250);
                mplew.writeInt(result);
                for (i = 0; i < result; i++) {
                    if (i == 0) {
                        mplew.writeInt(Randomizer.rand(-864, 10));
                        mplew.writeInt(Randomizer.rand(30, 915));
                        mplew.writeInt(Randomizer.rand(810, 3420));
                    } else {

                        mplew.writeInt(Randomizer.rand(300, 915));
                        mplew.writeInt(Randomizer.rand(-864, 10));
                        mplew.writeInt(Randomizer.rand(810, 3420));
                    }
                }
                break;
            case 100024:
                mplew.writeInt(7);
                mplew.writeInt(Randomizer.rand(0, 6));
                mplew.writeInt(3060);
                mplew.writeInt(2700);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case 100025:
                mplew.writeInt(-150);
                mplew.writeInt(-330);
                mplew.writeInt(150);
                mplew.writeInt(0);
                mplew.writeInt(1080);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(1);
                mplew.writeInt(8881031);
                mplew.writeInt(6);
                mplew.writeInt(1);
                mplew.write(0);
                mplew.writeInt(8);
                for(int x = 0; x < 8; x++) {
                    mplew.writeShort(Randomizer.rand(-1736, 1736));
                    mplew.writeShort(Randomizer.rand(-500, 500));
                }
               break;
        }
        return mplew.getPacket();
    }

    public static byte[] changePhase(int obj, int phase) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobPhaseChange.getValue());
        mplew.writeInt(500003);
        mplew.writeInt(1);
        return mplew.getPacket();
    }


    public static byte[] FlagRaceSkill(int... args) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ActionBarResult.getValue());
        mplew.writeInt(args[0]);
        if (args[0] == 5) {
            mplew.writeInt(args[1]);
            mplew.writeInt(args[2]);
            mplew.writeInt(args[3]);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] spawnMist(MapleMist mist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AffectedAreaCreated.getValue());
        mplew.writeInt(mist.getOwnerId());
        mplew.write(mist.isMobMist() ? 1 : mist.isPoisonMist());
        mplew.writeInt(mist.getOwnerId());
        int skillId = (mist.getMobSkill() != null) ? mist.getMobSkill().getSkillId() : ((mist.getMobSkill() != null) ? mist.getMobSkill().getSkillId() : 0);
        if (mist.getMobSkill() == null) {
            switch (skillId) {
                case 21121057:
                    skillId = 21121068;
                    break;
                case 400011058:
                    skillId = 400011060;
                    break;
            }
            mplew.writeInt(skillId);
        } else {
            mplew.writeInt(mist.getMobSkill().getSkillId());
        }
        if (mist.getMobSkill() != null) {
            mplew.writeShort(mist.getMobSkill().getSkillLevel());
        } else {
            mplew.writeShort(mist.getSkillLevel());
        }
        mplew.writeShort(mist.getSkillDelay());
        if (mist.getMobSkill() != null) {
            mplew.writeInt((mist.getMobSkill().getSkillId() == 186 || mist.getMobSkill().getSkillId() == 227) ? 8 : mist.isPoisonMist());
        } else {
            mplew.writeInt(mist.isPoisonMist());
        }
        if (mist.getMob().getTruePosition() != null) {
            mplew.writePos(mist.getMob().getTruePosition());
        } else if (mist.getOwner() != null) {
            mplew.writePos(mist.getOwner().getTruePosition());
        } else if (mist.getMob() != null) {
            mplew.writePos(mist.getMob().getTruePosition());
        } else if (mist.getMobSkill().getSkillId() == 183 && mist.getSkillLevel() == 13) {
            mplew.writePos(mist.getMob().getTruePosition());
        } else {
            mplew.writeShort((mist.getBox()).x);
            mplew.writeShort((mist.getBox()).y);
        }
        if (mist.getMobSkill() != null) {
            mplew.writeShort((skillId == 186 && (mist.getSkillLevel() == 3 || mist.getSkillLevel() == 5 || mist.getSkillLevel() == 6)) ? mist.getCustomx() : (mist.getOwner().getMaplePoints()));
            mplew.writeShort(mist.getMobSkill().getForce());
        } else {
            mplew.writeInt(0);
        }
        mplew.writeInt((mist.getDamup() > 0) ? mist.getDamup() : ((skillId == 131 && mist.getSkillLevel() == 28) ? 5 : 0));
        mplew.write((skillId == 131 && mist.getSkillLevel() == 28));
        mplew.writeInt((skillId == 400011060) ? 200 : ((mist.getMob() != null && mist.getMob().getId() / 10000 == 895) ? 210 : ((skillId == 217 && mist.getSkillLevel() == 21) ? 180 : ((skillId == 186 && mist.getSkillLevel() == 3) ? 190 : 0))));
        return mplew.getPacket();
    }

    public static byte[] removeMist(MapleMonster mist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AffectedAreaRemoved.getValue());
        mplew.writeInt(mist.getObjectId());
        mplew.writeInt(0);
        return mplew.getPacket();
    }


    public static byte[] ClearObstacles() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ObtacleAtomClear.getValue());
        return mplew.getPacket();
    }

    public static byte[] StigmaTime(int i) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.STIGMA_TIME.getValue());
        mplew.writeInt(i);
        return mplew.getPacket();
    }

    public static byte[] unlockSkill() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ResetOnStateForOnOffSkill.getValue());
        return mplew.getPacket();
    }


    public static byte[] showNormalEffect(MapleCharacter chr, int effectid, boolean own) {
        return showEffect(chr, 0, 0, effectid, 0, 0, (byte) 0, own, null, null, null, null);
    }


    public static byte[] UseSkillWithUI(int unk, int skillid, int skilllevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.USE_SKILL_WITH_UI.getValue());
        mplew.writeInt(unk);
        if (unk > 0) {
            mplew.writeInt(unk);
            mplew.write(false);
            mplew.writeInt(1);
            mplew.write(false);
            mplew.writeInt(skillid);
            mplew.writeInt(skilllevel);
            mplew.writeZeroBytes(23);
        }
        return mplew.getPacket();
    }

    public static byte[] portalTeleport(String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.PORTAL_TELEPORT.getValue());
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }


    public static byte[] showFieldSkillEffect(MapleCharacter chr, int skillid, int skillLevel) {
        return showEffect(chr, skillLevel, skillid, 46, 0, 0, (byte) 0, true, null, null, null, null);
    }

    public static byte[] showFieldSkillEffect(MapleCharacter chr, int skillid, byte skillLevel) {
        return showEffect(chr, skillLevel, skillid, 36, 0, 0, (byte) 0, true, null, null, null, null);
    }

    public static byte[] Respawn(int cid, int hp) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(OutHeader.RESPAWN.getValue());
        packet.writeInt(cid);
        packet.writeInt(hp);
        return packet.getPacket();
    }

    public static byte[] onUserTeleport(int x, int y) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(OutHeader.LP_UserTeleport.getValue());
        packet.writeInt(x);
        packet.writeInt(y);
        packet.write(0);
        return packet.getPacket();
    }

    public static byte[] JinHillah(int type, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.JIN_HILLAH.getValue());
        mplew.writeInt(type);
        switch (type) {
            case 0: // updateCandle
                mplew.writeInt(chr.getMap().getCandles()); // All candles
                mplew.write(false); // unk
                break;
            case 1: // handleCandle
                mplew.writeInt(chr.getMap().getLightCandles()); // candles with fire
                break;
            case 2: // clearCandle
                break;
            case 3: // updateMyDeathCount
                mplew.writeInt(chr.getDeathCounts().length); //All deathCount
                for (int i = 0; i < chr.getDeathCounts().length; ++i) {
                    mplew.writeInt(0); // unk
                    mplew.write(chr.getDeathCounts()[i]); // 0 : red, 1 : green, 2 : none
                }
                break;
            case 4: // makeSandGlass
                mplew.writeInt(chr == null ? 150000 : chr.getMap().getSandGlassTime() - System.currentTimeMillis()); // Duration
                mplew.writeInt(247); // skillId
                mplew.writeInt(1); // skillLv
                break;
            case 5: // clearSandGlass
                break;
            case 6: // spawnAlter
                mplew.writeInt(0); // x
                mplew.writeInt(266); // y
                mplew.writeInt(30);//chr.getMap().getReqTouched()); // reqTouched
                break;
            case 7: // updateAlter
                mplew.writeInt(30 - chr.getMap().getReqTouched());
                break;
            case 8: // removeAlter
                mplew.write(chr.getMap().getReqTouched() == 0); // isSuccess
                break;
            //There is no no.9
            case 10: // updateDeathCounts
                mplew.writeInt(chr.getId());
                mplew.writeInt(chr.liveCounts());
                break;
            case 11: // successAlter
                break;
        }
        return mplew.getPacket();
    }
}
