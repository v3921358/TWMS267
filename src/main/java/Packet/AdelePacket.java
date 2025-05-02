package Packet;

import Client.MapleCharacter;
import Client.MapleClient;
import Net.server.maps.ForceAtomObject;
import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.util.List;

public class AdelePacket {

    private MapleCharacter chr;

    public MapleCharacter getChr() {
        return this.chr;
    }

    public static byte[] ForceAtomObjectRemove(int chrId, int obj, int var2) {
        MaplePacketLittleEndianWriter p = new MaplePacketLittleEndianWriter();
        p.writeShort(OutHeader.LP_ForceAtomObjectRemove.getValue());
        p.writeInt(chrId);
        p.writeInt(1);
        p.writeInt(obj);
        p.writeInt(0);
        p.writeInt(var2);
        return p.getPacket();
    }

    public static byte[] ForceAtomObjectRemove(int chrId, List<ForceAtomObject> var1, int var2) {
        MaplePacketLittleEndianWriter p = new MaplePacketLittleEndianWriter();
        p.writeShort(OutHeader.LP_ForceAtomObjectRemove.getValue());
        p.writeInt(chrId);
        p.writeInt(var1.size());
        for (ForceAtomObject sword : var1) {
            p.write(sword.Idx);
        }
        p.writeInt(0);
        p.writeInt(var2);
        return p.getPacket();
    }

    /* v263-4 fix 阿戴爾 skill 碎片 */
    public static byte[] Remove_Sword_Effect(MaplePacketReader slea, MapleClient c) {
        int size = slea.readInt();
        int obj = slea.readInt();
        int unk = slea.readInt();
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ForceAtomObjectRemove.getValue());
        mplew.writeInt(c.getPlayer().getId());
        mplew.writeInt(1);
        mplew.writeInt(obj);
        mplew.writeInt(0);
        mplew.writeInt(1);
        c.getPlayer().getMap().removeSummon(obj);
        return mplew.getPacket();
    }


    public static byte[] ForceAtomObjectAttack(int chrId, int var1, int var2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ForceAtomObjectAttack.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(var1);
        if (var2 == 6 || var2 == 5) {
            mplew.writeInt(3);
        } else if (var2 == 4 || var2 == 3) {
            mplew.writeInt(2);
        } else if (var2 == 2 || var2 == 1) {
            mplew.writeInt(1);
        } else {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static void encodeForceAtomObject(MaplePacketLittleEndianWriter p, ForceAtomObject sword) {
        p.writeInt(sword.Idx);
        p.writeInt(sword.Idk3);
        p.writeInt(sword.DataIndex);
        p.writeInt(sword.Index);
        p.writeInt(sword.OwnerId);
        p.writeInt(sword.Target);
        p.writeInt(sword.CreateDelay);
        p.writeInt(sword.EnableDelay);
        p.writeInt(sword.Rotate);
        p.writeInt(sword.SkillId);
        p.writeInt(sword.Idk4);
        p.writeInt(sword.Idk1);
        p.writeInt(sword.Expire);
        p.writePosInt(sword.Position);
        p.writeInt(sword.Idk5);
        p.writeInt(sword.Idk2);
        p.writePosInt(sword.ObjPosition);
        p.writeBool(sword.B1);
        p.writeBool(sword.B2);
        p.writeBool(sword.B3);
        p.writeInt(sword.ValueList.size());
        for (int i : sword.ValueList) {
            p.writeInt(i);
        }
    }

    public static byte[] ForceAtomObject(int chrId, List<ForceAtomObject> swords, int n) {
        MaplePacketLittleEndianWriter p = new MaplePacketLittleEndianWriter();
        p.writeShort(OutHeader.LP_ForceAtomObject.getValue());
        p.writeInt(chrId);
        p.writeInt(swords.size());
        for (ForceAtomObject sword : swords) {
            encodeForceAtomObject(p, sword);
        }
        p.writeInt(n);
        return p.getPacket();
    }

    public static byte[] AdeleChargeResult(boolean suc) {
        MaplePacketLittleEndianWriter p = new MaplePacketLittleEndianWriter(OutHeader.LP_AdeleChargeResult);
        p.writeBool(suc);
        return p.getPacket();
    }

    public static byte[] removeSecondAtom(int cid, int objectId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.REMOVE_SECOND_ATOM.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(1);
        mplew.writeInt(objectId);
        mplew.writeInt(0);
        mplew.writeInt(1); // 351 new
        return mplew.getPacket();
    }

}
