package Handler;

import Client.MapleClient;
import Opcode.Headler.OutHeader;
import connection.InPacket;
import connection.OutPacket;

import static Opcode.Headler.InHeader.CP_SPIRT_WEAPON;

public class skillHandler {

    @Handler(op = CP_SPIRT_WEAPON)
    public static void skillAuto(MapleClient c, InPacket inPacket) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_SPIRT_WEAPON);
        int skillID = inPacket.decodeInt();
        outPacket.encodeInt(skillID);
        outPacket.encodeByte(1);
        c.write(outPacket);
    }
}
