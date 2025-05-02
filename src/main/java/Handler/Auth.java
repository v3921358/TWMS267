package Handler;

import Client.MapleClient;
import Opcode.Headler.OutHeader;
import connection.InPacket;
import connection.OutPacket;

import static Opcode.Headler.InHeader.CP_REQUEST_STATUS_CHECK;
import static Opcode.Headler.InHeader.CP_SECURITY_REQUEST;

public class Auth {

    @Handler(op = CP_SECURITY_REQUEST)
    public static void VerifyTimePulse(MapleClient c, InPacket inPacket) {
        OutPacket say = new OutPacket(OutHeader.LP_SECURITY_REQUEST.getValue());
        byte[] bytes = new byte[3];
        for (int i = 0; i < 3; i++) {
            bytes[i] = (byte) (inPacket.decodeByte() + i + 1);
            if (bytes[i] > 0x7F) {
                bytes[i] = (byte) (bytes[i] - 0xFF - 1);
            }
            say.encodeByte(bytes[i]);
        }
        say.encodeArr(new byte[5]);
        for (int i = 0; i < 3; i++) {
            bytes[i] = (byte) (bytes[i] + 1);
            if (bytes[i] > 0x7F) {
                bytes[i] = (byte) (bytes[i] - 0xFF - 1);
            }
            say.encodeByte(bytes[i]);
        }
        say.encodeArr(new byte[5]);
        c.write(say);
    }


    @Handler(op = CP_REQUEST_STATUS_CHECK) //todo : recv 1822 > send 2709
    public static void VerifyTimePulseNext(MapleClient c, InPacket inPacket) {
        OutPacket say = new OutPacket(OutHeader.LP_REQUEST_STATUS_CHECK.getValue());
        int NextType = inPacket.decodeInt();
        inPacket.decodeInt();
        boolean action = inPacket.decodeBoolean();
        say.encodeInt(NextType);
        say.encodeBoolean(action);
        c.write(say);
    }
}
