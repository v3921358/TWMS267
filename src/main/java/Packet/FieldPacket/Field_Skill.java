package Packet.FieldPacket;

import Net.server.life.MapleMonster;
import Net.server.maps.field.FieldSkill;
import Opcode.Headler.OutHeader;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

public class Field_Skill {

    /* event Plugin.script use */
    public static byte[] changeMobZone(MapleMonster mob) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobZoneChange.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(1);
        return mplew.getPacket();
    }

    /* event Plugin.script use */
    public static byte[] changePhase(MapleMonster mob) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobPhaseChange.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] useFieldSkill(FieldSkill fieldSkill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(fieldSkill.getSkillId());
        mplew.writeInt(fieldSkill.getSkillLevel());
        switch (fieldSkill.getSkillId()) {
            case 100008: {
                mplew.writeInt(fieldSkill.getSummonedSequenceInfoList().size());
                for (FieldSkill.SummonedSequenceInfo info : fieldSkill.getSummonedSequenceInfoList()) {
                    mplew.writeInt(info.getPosition().x);
                    mplew.writeInt(info.getPosition().y);
                }
                break;
            }
            case 100011: {
                mplew.writeInt(fieldSkill.getLaserInfoList().size());
                for (FieldSkill.LaserInfo info : fieldSkill.getLaserInfoList()) {
                    mplew.writeInt(info.getPosition().x);
                    mplew.writeInt(info.getPosition().y);
                    mplew.writeInt(info.getUnk1());
                    mplew.writeInt(info.getUnk2());
                }
                break;
            }
            case 100014:
            case 100016: {
                mplew.writeInt(fieldSkill.getThunderInfo().size());
                mplew.writeInt(fieldSkill.getSkillId() == 100016 ? 1400 : 2700);
                mplew.write(1);
                for (FieldSkill.ThunderInfo th : fieldSkill.getThunderInfo()) {
                    mplew.writePosInt(th.getStartPosition());
                    mplew.writePosInt(th.getEndPosition());
                    mplew.writeInt(th.getInfo());
                    mplew.writeInt(th.getDelay());
                }
            }
            case 100020: {
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
                    mplew.writeInt(fh.getPos().x);
                    mplew.writeInt(fh.getPos().y);
                    mplew.write(fh.isFacingLeft());
                }
                mplew.write(false);
                break;
            }
            case 100023: {
                int result = 2;
                mplew.writeInt(8880608);
                mplew.writeInt(1);
                mplew.writeInt(0);
                mplew.writeInt(100);
                mplew.writeInt(80);
                mplew.writeInt(240);
                mplew.writeInt(1530);
                mplew.writeInt(250);
                mplew.writeInt(result);
                for (int i = 0; i < result; ++i) {
                    if (i == 0) {
                        mplew.writeInt(Randomizer.rand(-864, 10));
                        mplew.writeInt(Randomizer.rand(30, 915));
                        mplew.writeInt(Randomizer.rand(810, 3420));
                        continue;
                    }
                    mplew.writeInt(Randomizer.rand(300, 915));
                    mplew.writeInt(Randomizer.rand(-864, 10));
                    mplew.writeInt(Randomizer.rand(810, 3420));
                }
                break;
            }
            case 100024: {
                mplew.writeInt(7);
                mplew.writeInt(Randomizer.rand(0, 6));
                mplew.writeInt(3060);
                mplew.writeInt(2700);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
            }
        }
        return mplew.getPacket();
    }

}
