package Server.netty;

import tools.BitTools;

public class MapleCustomEncryption {

    private static final int ITERATIONS = 6;
    private static final int ENCRYPT_CONSTANT = 0x48;
    private static final int DECRYPT_CONSTANT = 0x13;

    public static byte[] encryptData(byte[] data) {
        processData(data, false);
        return data;
    }

    public static byte[] decryptData(byte[] data) {
        processData(data, true);
        return data;
    }

    private static void processData(byte[] data, boolean decrypt) {
        byte remember = 0;
        byte nextRemember;

        for (int j = 1; j <= ITERATIONS; j++) {
            nextRemember = 0;
            boolean isEven = j % 2 == 0;

            for (int i = isEven ? 0 : data.length - 1; isEven ? i < data.length : i >= 0; i += isEven ? 1 : -1) {
                byte cur = data[i];

                if (decrypt) {
                    cur = subtract(cur, isEven, ENCRYPT_CONSTANT, data.length, nextRemember, remember);
                } else {
                    cur = add(cur, isEven, ENCRYPT_CONSTANT, data.length, remember);
                }

                data[i] = cur;
            }

            remember = nextRemember;
        }
    }

    private static byte add(byte cur, boolean isEven, int constant, int length, byte remember) {
        cur = BitTools.rollLeft(cur, isEven ? 3 : 4);
        cur += length;
        cur ^= remember;
        cur = BitTools.rollRight(cur, length & 0xFF);
        cur = (byte) (~cur & 0xFF);
        cur += isEven ? constant : 0;

        return cur;
    }

    private static byte subtract(byte cur, boolean isEven, int constant, int length, byte nextRemember, byte remember) {
        cur -= isEven ? constant : 0;
        cur = (byte) (~cur & 0xFF);
        cur = BitTools.rollLeft(cur, length & 0xFF);
        nextRemember = cur;
        cur ^= remember;
        cur -= length;
        cur = BitTools.rollRight(cur, isEven ? 3 : 4);

        return cur;
    }
}