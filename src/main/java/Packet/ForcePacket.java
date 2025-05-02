package Packet;

import Client.force.MapleForceAtom;
import Client.force.MapleForceInfo;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Net.server.life.MapleMonster;
import Opcode.Headler.OutHeader;
import tools.DateUtil;
import tools.Randomizer;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.List;
import java.util.Map;

public class ForcePacket {

    public static byte[] UserExplosionAttack(MapleMonster monster) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserExplosionAttack.getValue());
        mplew.writeInt(傑諾.三角列陣);
        mplew.writePos(monster.getPosition());
        mplew.writeInt(3);
        mplew.writeInt(monster.getObjectId());
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] forceAtomCreate(MapleForceAtom force) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ForceAtomCreate.getValue());
        mplew.writeBool(force.isFromMob());
        mplew.writeInt(force.getOwnerId());
        if (force.isFromMob()) {
            mplew.writeInt(force.getFromMobOid());
        }
        final int type = force.getForceType();
        mplew.writeInt(type);
        if (type > 0 && type != 9 && type != 14 && type != 29 && type != 35 && type != 42) {
            if (type != 36 && type != 37) {
                mplew.write(1);
                switch (type) {
                    case 2:
                    case 3:
                    case 6:
                    case 7:
                    case 11:
                    case 12:
                    case 13:
                    case 17:
                    case 19:
                    case 20:
                    case 23:
                    case 24:
                    case 25:
                    case 27:
                    case 28:
                    case 30:
                    case 32:
                    case 34:
                    case 38:
                    case 39:
                    case 40:
                    case 41:
                    case 47:
                    case 48:
                    case 49:
                    case 52:
                    case 53:
                    case 54:
                    case 55:
                    case 56:
                    case 57:
                    case 58:
                    case 60:
                    case 64:
                    case 65:
                    case 66:
                    case 67:
                    case 72:
                    case 73:
                    case 75:
                    case 76: {
                        mplew.writeInt(force.getToMobOid().size());
                        for (int oid : force.getToMobOid()) {
                            mplew.writeInt(oid);
                        }
                        break;
                    }
                    default: {
                        mplew.writeInt(force.getFirstMobID());
                        if (type == 62) {
                            if (force.getFirstMobID() > 0) {
                                int count = force.getFirstMobID();
                                do {
                                    mplew.writeInt(0);
                                    count--;
                                } while (count > 0);

                            }
                        }
                        break;
                    }
                }
            }
            mplew.writeInt(force.getSkillId());
        }
        switch (type) {
            case 29:
            case 42:
                mplew.writeInt(force.getSkillId());
                if (force.getSkillId() == 煉獄巫師.冥界死神) {
                    mplew.writeInt(0);
                }
                break;
        }
        for (MapleForceInfo info : force.getInfo()) {
            mplew.write(1);
            mplew.writeInt(info.getKey());
            mplew.writeInt(0);
            mplew.writeInt(info.getInc());
            mplew.writeInt(info.getFirstImpact()); // 飛出距離
            mplew.writeInt(info.getSecondImpact());
            mplew.writeInt(info.getAngle());
            mplew.writeInt(info.getStartDelay());
            mplew.writePosInt(info.getPosition());
            mplew.writeInt(DateUtil.getTime(info.getTime()));
            mplew.writeInt(info.getMaxHitCount());
            mplew.writeInt(info.getEffectIdx());
            mplew.writeInt(0);
            mplew.write(0);
            if (force.getSkillId() >= 80011585 && (force.getSkillId() <= 80011591 || force.getSkillId() == 80011635) && info.pos2 != null) {
                mplew.writeInt(info.pos2.x);
                mplew.writeInt(info.pos2.y);
            }
        }
        mplew.write(0);

        switch (type) {
            case 11: {
                mplew.writeRect(force.getRect());
                mplew.writeInt(force.getBulletItemID());
                break;
            }
            case 9: {
                mplew.writeRect(force.getRect());
                break;
            }
            case 15: {
                mplew.writeRect(force.getRect());
                mplew.write(0);//V.160 new
                break;
            }
            case 29: {
                mplew.writeRect(force.getRect());
                mplew.writePosInt(force.getPos2());
                break;
            }
            case 2:
            case 4:
            case 16:
            case 20:
            case 26:
            case 30:
            case 33:
            case 61:
            case 64:
            case 67:
            case 69:
            case 74:
            case 76:
            case 85:
            case 94: {
                mplew.writePosInt(force.getForcedTarget());
                break;
            }
            case 17:
            case 77: {
                mplew.writeInt(force.getArriveDir());
                mplew.writeInt(force.getArriveRange());
                break;
            }
            case 18: {
                mplew.writePosInt(force.getForcedTarget());
                break;
            }
            case 27: {
                // rect
                mplew.writeInt(-500);
                mplew.writeInt(-350);
                mplew.writeInt(500);
                mplew.writeInt(350);

                mplew.writeInt(0);
                break;
            }
            case 28:
            case 34: {
                mplew.writeRect(force.getRect());
                mplew.writeInt(force.getInfo().size());
                break;
            }
            case 57:
            case 58:
            case 86:
            case 87: {
                mplew.writeRect(force.getRect());
                mplew.writeInt(0); // 1000 * value  time ??
                mplew.writePosInt(force.getForcedTarget());
                break;
            }
            case 36:
            case 39:
            case 89: {
                mplew.writeInt(type == 39 ? 0 : 5);
                mplew.writeInt(type == 39 ? 0 : 550);
                mplew.writeInt(0);
                mplew.writeRect(force.getRect());
                if (type == 36 || type == 89) {
                    mplew.writeRect(force.getRect2());
                    mplew.writeInt(0);
                }
                break;
            }
            case 37:
            case 91: {
                mplew.writeInt(0);
                mplew.writeRect(force.getRect());
                mplew.writeInt(200);
                mplew.writeInt(0);
                break;
            }
            case 42: {
                // rect
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            }
            case 49: {
                mplew.writeInt(0);
                mplew.writeInt(force.getArriveDir());
                // rect
                mplew.writePosInt(force.getForcedTarget());
                mplew.writePosInt(force.getForcedTarget());
                break;
            }
            case 50: {
                mplew.writePosInt(force.getForcedTarget());
                mplew.writeInt(0);
                break;
            }
            case 7: {
                // rect
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            }
        }
        switch (force.getSkillId()) {
            case 隱月.小狐仙精通_1:
            case 隱月.火狐精通_1:
            case 25141505:
                mplew.writeInt(0);
                break;
            case 聖騎士.雷神戰槌:
                mplew.writeInt(0x8000);
                mplew.write(2);
                break;
            case 幻影俠盜.黑傑克_1:
                mplew.writeInt(0);
                mplew.writeInt(force.getRect().x);
                mplew.writeInt(force.getRect().y);
                break;
        }
        if (type == 35) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] showGuidedArrow(int key, int id, int objectID) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ForceAtomAction.getValue());
        mplew.writeInt(id);
        mplew.writeInt(key);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(objectID);
        return mplew.getPacket();
    }

    public static byte[] showBeads(int chrId, Map<Integer, Map<Integer, MapleForceAtom>> map) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ForceTeleAtomCreate.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(亞克.咒術子彈);
        mplew.writeInt(map.size());
        for (Map.Entry<Integer, Map<Integer, MapleForceAtom>> e : map.entrySet()) {
            switch (e.getKey()) {
                case 亞克.深淵技能: {
                    mplew.writeInt(46);
                    break;
                }
                case 亞克.迸發技能: {
                    mplew.writeInt(45);
                    break;
                }
                case 亞克.緋紅技能: {
                    mplew.writeInt(44);
                    break;
                }
                case 亞克.原始技能: {
                    mplew.writeInt(43);
                    break;
                }
            }
            mplew.writeInt(e.getKey());
            mplew.writeInt(e.getValue().size());
            for (Map.Entry<Integer, MapleForceAtom> entry : e.getValue().entrySet()) {
                mplew.writeInt(entry.getKey());
                for (MapleForceInfo info : entry.getValue().getInfo()) {
                    mplew.write(1);
                    mplew.writeInt(info.getKey());
                    mplew.writeInt(900 + Randomizer.nextInt(10));
                    mplew.writeInt(info.getFirstImpact());
                    mplew.writeInt(info.getSecondImpact());
                    mplew.writeInt(info.getAngle());
                    mplew.writeInt(info.getInc());
                    mplew.writePosInt(info.getPosition());
                    mplew.writeInt(DateUtil.getTime(info.getTime()));
                    mplew.writeInt(info.getMaxHitCount());
                    mplew.writeInt(info.getEffectIdx());
                    mplew.writeInt(0);
                }
                mplew.write(0);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] forceTeleAtomCreate(int chrId, int skill, List<Triple<Integer, Integer, Map<Integer, MapleForceAtom>>> list) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ForceTeleAtomCreate.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(skill);
        mplew.writeInt(list.size());
        for (Triple<Integer, Integer, Map<Integer, MapleForceAtom>> ga : list) {
            final Map<Integer, MapleForceAtom> map = ga.getRight();
            mplew.writeInt(ga.getMid());
            mplew.writeInt(ga.getLeft());
            mplew.writeInt(map.size());
            for (Map.Entry<Integer, MapleForceAtom> o : map.entrySet()) {
                final MapleForceAtom entry = o.getValue();
                mplew.writeInt(o.getKey());
                for (MapleForceInfo info : entry.getInfo()) {
                    mplew.write(1);
                    mplew.writeInt(info.getKey());
                    mplew.writeInt(900 + Randomizer.nextInt(10));
                    mplew.writeInt(info.getFirstImpact());
                    mplew.writeInt(info.getSecondImpact());
                    mplew.writeInt(info.getAngle());
                    mplew.writeInt(info.getInc());
                    mplew.writePosInt(info.getPosition());
                    mplew.writeInt(DateUtil.getTime(info.getTime()));
                    mplew.writeInt(info.getMaxHitCount());
                    mplew.writeInt(info.getEffectIdx());
                    mplew.writeInt(0);
                }
                mplew.write(0);
            }
        }
        return mplew.getPacket();
    }
}
