package Packet.FieldPacket;

import Client.MapleCharacter;
import Net.server.Timer;
import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;

public class 安哈林防禦_ {

    public static void StartANHALIN(MapleCharacter player) {
        if (player.getMapId() == 993195700) {
            Timer.EventTimer.getInstance().schedule(() -> {
                ShowBoss_Message(player);
            }, 7000);
            Timer.EventTimer.getInstance().schedule(() -> {
                ShowNpc_Message(player);
            }, 3500);
        }
    }

    public static byte[] ShowBoss_Message(MapleCharacter player) {
        /* 你是誰?這裡是朕的城堡 */
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);
        mplew.writeHexString("00 DA 20 4E 00 20 00 20 20 20");
        mplew.writeHexString("A7 41 AC 4F BD D6 A1 4B A1 43 B3 6F B8 CC AC 4F AE D3 AA BA AB B0 B3 F9 A1 49");
        mplew.writeHexString("20 20 20 05 00 00 00 00");
        return mplew.getPacket();
    }

    public static byte[] 清空能量儀(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);
        mplew.writeHexString("02 00 00 00 04 00 00 00 00 00 00 00");
        return mplew.getPacket();
    }

    public static byte[] ShowNpc_Message(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);
        mplew.writeHexString("62 D4 2D 00 D0 07 00 00 3A 00");
        mplew.writeMapleAsciiString("請擊殺周圍怪物蒐集狂魔力能量。不久後肉身的碎片會出現在左右。");
        mplew.writeHexString("00 00 00");
        return mplew.getPacket();
    }

    public static byte[] ShowNpc_Message_2(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);
        mplew.writeHexString("62 D4 2D 00 D0 07 00 00 2C 00");
        mplew.writeMapleAsciiString("攻擊正要開始了。使用狂魔力發射器來應付吧！");
        mplew.writeHexString("00 00 00");
        return mplew.getPacket();
    }


    public static byte[] Effect_減少或增加(int type, int Mex) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ANHALIN_CLEAR_POWER.getValue());
        mplew.writeInt(type); // 2 = + / 4 = -
        mplew.writeZeroBytes(4);
        mplew.writeInt(Mex);  // max 700
        return mplew.getPacket();
    }


    public static byte[] ShowNpc_Message4(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);
        mplew.writeInt(3003490); /* 安哈林 NPCID */
        mplew.writeInt(2000);
        mplew.writeInt(58);
        mplew.writeMapleAsciiString("請擊殺周圍怪物蒐集狂魔力能量。不久後肉身的碎片會出現在左右。");
        mplew.write(0);
        return mplew.getPacket();
    }


    public static byte[] ShowNpc_Message5(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);
        mplew.writeInt(3003490); /* 安哈林 NPCID */
        mplew.writeInt(2000);
        mplew.writeInt(58);
        mplew.writeMapleAsciiString("請擊殺周圍怪物蒐集狂魔力能量。不久後肉身的碎片會出現在左右。");
        mplew.write(0);
        return mplew.getPacket();
    }


    public static byte[] ShowNpc_Message6(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);
        mplew.writeInt(3003490); /* 安哈林 NPCID */
        mplew.writeInt(2000);
        mplew.writeInt(58);
        mplew.writeMapleAsciiString("請擊殺周圍怪物蒐集狂魔力能量。不久後肉身的碎片會出現在左右。");
        mplew.write(0);
        return mplew.getPacket();
    }

}
