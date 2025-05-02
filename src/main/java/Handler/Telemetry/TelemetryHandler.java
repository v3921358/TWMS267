package Handler.Telemetry;

import Client.MapleClient;
import Handler.Handler;
import connection.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.StringObfuscation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static Opcode.Headler.InHeader.CHEAT_ENGINE;
import static Opcode.Headler.InHeader.PROCESS_REPORT;

public class TelemetryHandler {

    private static final Logger logger = LoggerFactory.getLogger("ClientLog");

    @Handler(op = PROCESS_REPORT)
    public static byte[] handleProcessReport(MapleClient client, InPacket get) {
        int type = get.decodeInt();
        System.out.println("type = " + type);
        if (type == 24) {
            int rand = get.decodeInt();
            int len = get.decodeInt() ^ rand;
            byte[] buff = get.decodeArr(len);
            decryptProcessReport(len, rand, buff);
            InPacket iPacket = new InPacket(buff);
            int opc = iPacket.decodeInt();
            byte[] obs = iPacket.decodeRawString();
            String deobs = StringObfuscation.DeobfuscateRor(obs, 3);
            System.out.println(opc + "," + deobs);
            return obs;
        } else if (type == 49) {
            String str = get.decodeString();
            System.out.println(str);
        } else if (type == 76) {
            int a = get.decodeInt();
            int b = get.decodeInt();
            System.out.println(a + "," + b);
        } else if (type == 85) {
            int a = get.decodeInt();
            System.out.println(a);
        }
        return new byte[0];
    }

    @Handler(op = CHEAT_ENGINE)
    public static void handleCheatEngine(MapleClient client, InPacket iPacket) {
        int type = iPacket.decodeInt();
        byte[] raw = iPacket.decodeRawString();
        String clog = StringObfuscation.DeobfuscateRor(raw, 3);
        logger.info("客戶端回報：" + clog);
    }

    public static void decryptProcessReport(int len, int rand, byte[] buf) {
        ByteBuffer buffer = ByteBuffer.wrap(buf, 0, len);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int i = 0;
        if (len >= 4) {
            int v23 = 0;
            do {
                int currentInt = buffer.getInt();
                int xorResult = ((currentInt + v23) ^ rand) + (rand >>> 6) + rand + 0x4C99E329;
                int decryptedInt = xorResult ^ 0xA0A0B0B0;
                buffer.putInt(buffer.position() - Integer.BYTES, decryptedInt);
                i += 4;
                v23 += 4 * rand;
            } while (i + 4 <= len);
        }

        if (i < len) {
            do {
                byte currentByte = buffer.get();
                byte decryptedByte = (byte) (((currentByte - (i * rand)) + (rand >>> 6) + 39) ^ (rand >>> 1));
                decryptedByte ^= 0xBC;
                buffer.put(buffer.position() - 1, decryptedByte);
                i += 1;
            } while (i < len);
        }
    }
}
