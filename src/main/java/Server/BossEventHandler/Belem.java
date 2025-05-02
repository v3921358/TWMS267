package Server.BossEventHandler;

import Client.MapleCharacter;
import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;

public class Belem {

    public static void StartBelemOnPacket(MapleCharacter player) {
        if (player.getMap().getId() == 105200510) {
            BanBanClockEffect(player);
        }
    }

    public static byte[] DropAttack(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.JOCKER_SKILL.getValue());
        mplew.writeHexString("09 00 44 72 6F 70 53 74 6F 6E 65 00 00 00 00 0B 00 00 00 47 03 00 00 BB 01 00 00 77 FB FF FF BB 01 00 00 5F FF FF FF BB 01 00 00 89 F8 FF FF BB 01 00 00 95 F6 FF FF BB 01 00 00 7D FA FF FF BB 01 00 00 59 00 00 00 BB 01 00 00 65 FE FF FF BB 01 00 00 9B F5 FF FF BB 01 00 00 6B FD FF FF BB 01 00 00 53 01 00 00 BB 01 00 00");
        player.send(mplew.getPacket());
        return mplew.getPacket();
    }


    public static byte[] BanBanClockEffect(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AffectedAreaCreated.getValue());
        mplew.writeHexString("7F 01 00 00 01 E7 58 7E 01 BF 00 00 00 02 00 07 00 F7 FC FF FF E9 00 00 00 89 FE FF FF E8 01 00 00 00 00 00 00 C0 FD C7 01 00 00 00 00 F1 FF FF FF 00 00 00 00 00 00 00 00 00 BE 00 00 00 80 43 00 00 00 00 00 00 00 00 00 00 00 01 00");
        player.dropMessage(28, "在時間之空隙產生了”裂痕”。");
        player.send(mplew.getPacket());
        return mplew.getPacket();
    }
}
