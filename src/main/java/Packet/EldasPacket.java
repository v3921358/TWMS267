package Packet;

import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;

public class EldasPacket {

    /*
     * 獲得200靈魂艾爾達的氣息
     */
    public static byte[] Eldas_200(String args) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ProgressMessageFont.getValue());
        mplew.writeInt(3);
        mplew.writeInt(20);
        mplew.writeInt(9);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeMapleAsciiString(args);
        return mplew.getPacket();
    }
}
