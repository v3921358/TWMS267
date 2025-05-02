/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Client.inventory.Item;
import Opcode.Headler.OutHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketLittleEndianWriter;

/**
 * @author PlayDK
 */
public class WhisperPacket {

    private static final Logger log = LoggerFactory.getLogger(WhisperPacket.class);

    public static byte[] getWhisper(String sender, int channel, String text, Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_Whisper.getValue());
        mplew.write(18);
        mplew.writeHexString("B7 B4 33 00 08 00");
        PacketHelper.addChaterName(mplew, sender, text);
        return mplew.getPacket();
    }

    public static byte[] getWhisperReply(String target, byte reply) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_Whisper.getValue());
        mplew.write(18);
        mplew.writeHexString("B7 B4 33 00");
        PacketHelper.addChaterName(mplew, target, target);
        return mplew.getPacket();
    }

    public static byte[] getFindReplyWithMap(String target, int mapid, boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_Whisper.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(1);
        mplew.writeInt(mapid);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] getFindReply(String target, int channel, boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Whisper.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(3);
        mplew.writeInt(channel - 1);

        return mplew.getPacket();
    }

    public static byte[] getFindReplyWithCS(String target, boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Whisper.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(2);
        mplew.writeInt(-1);

        return mplew.getPacket();
    }

    public static byte[] getFindReplyWithMTS(String target, boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Whisper.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(0);
        mplew.writeInt(-1);

        return mplew.getPacket();
    }
}
