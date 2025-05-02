package Packet;

import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;

public class HyperPacket {


    public static byte[] getHyperUIReset() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.HYPER_PRESET_UI_RESET.getValue());
        mplew.writeHexString("01 D3 53 51 00 01");
        return mplew.getPacket();
    }
}
