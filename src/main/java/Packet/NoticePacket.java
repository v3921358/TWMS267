package Packet;

import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;

public class NoticePacket {

    public static byte[] BlackHeavenNotice(String Notice) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_BlowWeather.getValue());
        mplew.writeHexString("00 F2 20 4E 00 1F 00");
        mplew.writeMapleAsciiString(Notice);
        mplew.writeInt(1091);
        mplew.write(0);
        return mplew.getPacket();
    }
}
