package Packet.FieldPacket;

import tools.data.MaplePacketLittleEndianWriter;

public class 飢餓的武藤 {

    public static byte[] StartHungryMuto() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(1868);
        mplew.writeInt(3);
        mplew.writeInt(4034963);
        mplew.writeInt(4);
        mplew.writeInt(1);
        mplew.writeInt(130000);
        mplew.writeInt(45000);
        mplew.writeInt(3);
        mplew.writeInt(2435868);
        mplew.writeInt(10);
        mplew.writeInt(0);
        mplew.writeInt(2435860);
        mplew.writeInt(5);
        mplew.writeInt(0);
        mplew.writeInt(2435864);
        mplew.writeInt(5);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
}
