/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Opcode.Headler.OutHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.data.MaplePacketLittleEndianWriter;

public class ChatPacket {

    private static final Logger log = LoggerFactory.getLogger(ChatPacket.class);

    public static byte[] getChatLoginAUTH() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_WorldInformation.getValue());
        mplew.writeHexString("2D 06 00 52 65 62 6F 6F 74 00 00 00 00 01 00 00 00 00 00 03 2C 00 B0 C8 A5 B2 B8 6A A9 77 51 52 B3 CC B0 AA A8 BE C5 40 A1 41 AB 4F C5 40 B1 62 B8 B9 A6 77 A5 FE A1 41 A4 5D AB 4F C5 40 A5 4C A4 48 1E 08 00 52 65 62 6F 6F 74 2D 31 07 00 00 00 2D 00 00 08 00 52 65 62 6F 6F 74 2D 32 07 00 00 00 2D 01 00 08 00 52 65 62 6F 6F 74 2D 33 07 00 00 00 2D 02 00 08 00 52 65 62 6F 6F 74 2D 34 07 00 00 00 2D 03 00 08 00 52 65 62 6F 6F 74 2D 35 07 00 00 00 2D 04 00 08 00 52 65 62 6F 6F 74 2D 36 07 00 00 00 2D 05 00 08 00 52 65 62 6F 6F 74 2D 37 07 00 00 00 2D 06 00 08 00 52 65 62 6F 6F 74 2D 38 07 00 00 00 2D 07 00 08 00 52 65 62 6F 6F 74 2D 39 07 00 00 00 2D 08 00 09 00 52 65 62 6F 6F 74 2D 31 30 07 00 00 00 2D 09 00 09 00 52 65 62 6F 6F 74 2D 31 31 07 00 00 00 2D 0A 00 09 00 52 65 62 6F 6F 74 2D 31 32 07 00 00 00 2D 0B 00 09 00 52 65 62 6F 6F 74 2D 31 33 07 00 00 00 2D 0C 00 09 00 52 65 62 6F 6F 74 2D 31 34 07 00 00 00 2D 0D 00 09 00 52 65 62 6F 6F 74 2D 31 35 0C 00 00 00 2D 0E 00 09 00 52 65 62 6F 6F 74 2D 31 36 07 00 00 00 2D 0F 00 09 00 52 65 62 6F 6F 74 2D 31 37 07 00 00 00 2D 10 00 09 00 52 65 62 6F 6F 74 2D 31 38 07 00 00 00 2D 11 00 09 00 52 65 62 6F 6F 74 2D 31 39 07 00 00 00 2D 12 00 09 00 52 65 62 6F 6F 74 2D 32 30 07 00 00 00 2D 13 00 09 00 52 65 62 6F 6F 74 2D 32 31 07 00 00 00 2D 14 00 09 00 52 65 62 6F 6F 74 2D 32 32 07 00 00 00 2D 15 00 09 00 52 65 62 6F 6F 74 2D 32 33 0C 00 00 00 2D 16 00 09 00 52 65 62 6F 6F 74 2D 32 34 07 00 00 00 2D 17 00 09 00 52 65 62 6F 6F 74 2D 32 35 07 00 00 00 2D 18 00 09 00 52 65 62 6F 6F 74 2D 32 36 07 00 00 00 2D 19 00 09 00 52 65 62 6F 6F 74 2D 32 37 07 00 00 00 2D 1A 00 09 00 52 65 62 6F 6F 74 2D 32 38 07 00 00 00 2D 1B 00 09 00 52 65 62 6F 6F 74 2D 32 39 07 00 00 00 2D 1C 00 09 00 52 65 62 6F 6F 74 2D 33 30 07 00 00 00 2D 1D 00 00 00 00 00 00 00 01 1F 00 00 00 1E 02 00 1D 01 00 1C 01 00 1B 01 00 1A 01 00 19 01 00 18 01 00 17 01 00 16 01 00 15 01 00 14 01 00 13 01 00 12 01 00 11 01 00 10 01 00 0F 01 00 0E 01 00 0D 01 00 0C 01 00 0B 01 00 0A 01 00 09 01 00 08 01 00 07 01 00 06 01 00 05 01 00 04 01 00 03 01 00 02 01 00 01 01 00 00 01 00");
        return mplew.getPacket();
    }

    public static byte[] getChatLoginResult() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CHAT_SERVER_RESULT.getValue());
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0); //add + tms V260-
        return mplew.getPacket();
    }

    public static byte[] buddyChat(int fromaccid, int fromchrid, String chattext) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.BUDDY_CHAT.getValue());
        mplew.writeInt(fromaccid);
        mplew.writeInt(fromaccid);
        mplew.writeInt(fromchrid);
        mplew.writeLong(DateUtil.getFileTimestamp(System.currentTimeMillis()));
        mplew.writeMapleAsciiString(chattext);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] guildChat(int fromaccid, int fromguildid, int fromchrid, String chattext) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.GUILD_CHAT.getValue());
        mplew.writeInt(fromaccid);
        mplew.writeInt(fromguildid);
        mplew.writeInt(fromaccid);
        mplew.writeInt(fromchrid);
        mplew.writeLong(DateUtil.getFileTimestamp(System.currentTimeMillis()));
        mplew.writeMapleAsciiString(chattext);
        mplew.write(0);

        return mplew.getPacket();
    }
}
