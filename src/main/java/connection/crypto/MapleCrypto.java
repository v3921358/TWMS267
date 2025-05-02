package connection.crypto;

import SwordieX.util.Util;

import static connection.crypto.BitTools.multiplyBytes;

/**
 * Artifact from Invictus that improved the old MapleAESOFB by
 * making it easily scaled with session groups while using Apache's
 * MINA library. Ported over for usage within a Netty setup since it will
 * work exactly the same.
 */
public class MapleCrypto {

    /**
     * AES cipher used for cryptography.
     *
     * @see AES#encrypt(byte[]) used in this method.
     */
    private final AES cipher;
    /**
     * Used for checking the packets being sent and received. These are
     * initialized at the starting part of the program by providing the
     * current version the server is running.
     *
     * @see MapleCrypto#checkPacket(byte[], byte[]) checking packet header using these.
     * @see MapleCrypto#checkPacket(int, byte[]) checking packet header using these.
     * @see MapleCrypto#initialize(short) where the values are initialized.
     */
    private static short gVersion, sVersion, rVersion;
    /**
     * Used for renewing the cryptography seed for sending or receiving
     * packets.
     */
    public static final int[] SHUFFLE_BYTES = new int[]{0xEC, 0x3F, 0x77, 0xA4, 0x45, 0xD0, 0x71, 0xBF, 0xB7, 0x98, 0x20, 0xFC,
            0x4B, 0xE9, 0xB3, 0xE1, 0x5C, 0x22, 0xF7, 0x0C, 0x44, 0x1B, 0x81, 0xBD, 0x63, 0x8D, 0xD4, 0xC3,
            0xF2, 0x10, 0x19, 0xE0, 0xFB, 0xA1, 0x6E, 0x66, 0xEA, 0xAE, 0xD6, 0xCE, 0x06, 0x18, 0x4E, 0xEB,
            0x78, 0x95, 0xDB, 0xBA, 0xB6, 0x42, 0x7A, 0x2A, 0x83, 0x0B, 0x54, 0x67, 0x6D, 0xE8, 0x65, 0xE7,
            0x2F, 0x07, 0xF3, 0xAA, 0x27, 0x7B, 0x85, 0xB0, 0x26, 0xFD, 0x8B, 0xA9, 0xFA, 0xBE, 0xA8, 0xD7,
            0xCB, 0xCC, 0x92, 0xDA, 0xF9, 0x93, 0x60, 0x2D, 0xDD, 0xD2, 0xA2, 0x9B, 0x39, 0x5F, 0x82, 0x21,
            0x4C, 0x69, 0xF8, 0x31, 0x87, 0xEE, 0x8E, 0xAD, 0x8C, 0x6A, 0xBC, 0xB5, 0x6B, 0x59, 0x13, 0xF1,
            0x04, 0x00, 0xF6, 0x5A, 0x35, 0x79, 0x48, 0x8F, 0x15, 0xCD, 0x97, 0x57, 0x12, 0x3E, 0x37, 0xFF,
            0x9D, 0x4F, 0x51, 0xF5, 0xA3, 0x70, 0xBB, 0x14, 0x75, 0xC2, 0xB8, 0x72, 0xC0, 0xED, 0x7D, 0x68,
            0xC9, 0x2E, 0x0D, 0x62, 0x46, 0x17, 0x11, 0x4D, 0x6C, 0xC4, 0x7E, 0x53, 0xC1, 0x25, 0xC7, 0x9A,
            0x1C, 0x88, 0x58, 0x2C, 0x89, 0xDC, 0x02, 0x64, 0x40, 0x01, 0x5D, 0x38, 0xA5, 0xE2, 0xAF, 0x55,
            0xD5, 0xEF, 0x1A, 0x7C, 0xA7, 0x5B, 0xA6, 0x6F, 0x86, 0x9F, 0x73, 0xE6, 0x0A, 0xDE, 0x2B, 0x99,
            0x4A, 0x47, 0x9C, 0xDF, 0x09, 0x76, 0x9E, 0x30, 0x0E, 0xE4, 0xB2, 0x94, 0xA0, 0x3B, 0x34, 0x1D,
            0x28, 0x0F, 0x36, 0xE3, 0x23, 0xB4, 0x03, 0xD8, 0x90, 0xC8, 0x3C, 0xFE, 0x5E, 0x32, 0x24, 0x50,
            0x1F, 0x3A, 0x43, 0x8A, 0x96, 0x41, 0x74, 0xAC, 0x52, 0x33, 0xF0, 0xD9, 0x29, 0x80, 0xB1, 0x16,
            0xD3, 0xAB, 0x91, 0xB9, 0x84, 0x7F, 0x61, 0x1E, 0xCF, 0xC5, 0xD1, 0x56, 0x3D, 0xCA, 0xF4, 0x05,
            0xC6, 0xE5, 0x08, 0x49};

    /**
     * Constructor for MapleAES. Creates the cipher that will be
     * used for the cryptography segment as well as the lock used for the
     * same segment.
     */
    public MapleCrypto() {
        cipher = new AES();

        cipher.setKey(keyBytes);
    }

    private static byte[] keyBytes = new byte[0];

    /**
     * Initializes the send and receive version values for packet
     * checking using the current version.
     *
     * @param v the current version this service is running.
     */
    public static void initialize(short v) {
        String[] skeys = new String[]{
                "2923BE84E16CD6AE529049F1F1BBE9EBB3A6DB3C870C3E99245E0D1C06B747DE",
                "B3124DC843BB8BA61F035A7D0938251F5DD4CBFC96F5453B130D890A1CDBAE32",
                "888138616B681262F954D0E7711748780D92291D86299972DB741CFA4F37B8B5",
                "209A50EE407836FD124932F69E7D49DCAD4F14F2444066D06BC430B7323BA122",
                "F622919DE18B1FDAB0CA9902B9729D492C807EC599D5E980B2EAC9CC53BF67D6",
                "BF14D67E2DDC8E6683EF574961FF698F61CDD11E9D9C167272E61DF0844F4A77",
                "02D7E8392C53CBC9121E33749E0CF4D5D49FD4A4597E35CF3222F4CCCFD3902D",
                "48D38F75E6D91D2AE5C0F72B788187440E5F5000D4618DBE7B0515073B33821F",
                "187092DA6454CEB1853E6915F8466A0496730ED9162F6768D4F74A4AD0576876",
                "5B628A8A8F275CF7E5874A3B329B614084C6C3B1A7304A10EE756F032F9E6AEF",
                "762DD0C2C9CD68D4496A792508614014B13B6AA51128C18CD6A90B87978C2FF1",
                "10509BC8814329288AF6E99E47A18148316CCDA49EDE81A38C9810FF9A43CDCF",
                "5E4EE1309CFED9719FE2A5E20C9BB44765382A4689A982797A7678C263B126DF",
                "DA296D3E62E0961234BF39A63F895EF16D0EE36C28A11E201DCBC2033F410784",
                "0F1405651B2861C9C5E72C8E463608DCF3A88DFEBEF2EB71FFA0D03B75068C7E",
                "8778734DD0BE82BEDBC246412B8CFA307F70F0A754863295AA5B68130BE6FCF5",
                "CABE7D9F898A411BFDB84F68F6727B1499CDD30DF0443AB4A66653330BCBA110",
                "5E4CEC034C73E605B4310EAAADCFD5B0CA27FFD89D144DF4792759427C9CC1F8",
                "CD8C87202364B8A687954CB05A8D4E2D99E73DB160DEB180AD0841E96741A5D5",
                "9FE4189F15420026FE4CD12104932FB38F735340438AAF7ECA6FD5CFD3A195CE"
        };
        // TwMS規律金鑰處理
        if (v >= 176) {
            keyBytes = Util.getByteArrayByString(skeys[v % 20]);
            for (int i = 0; i < keyBytes.length; i = i + 4) {
                for (int n = 1; n <= 3; n++) {
                    keyBytes[i + n] = 0;
                }
            }
        } else {
            keyBytes = new byte[]{
                    /* v258_OBT */
                    (byte) 0x5A, 0x00, 0x00, 0x00,
                    (byte) 0x2A, 0x00, 0x00, 0x00,
                    (byte) 0xA1, 0x00, 0x00, 0x00,
                    (byte) 0xB4, 0x00, 0x00, 0x00,
                    (byte) 0x32, 0x00, 0x00, 0x00,
                    (byte) 0x4D, 0x00, 0x00, 0x00,
                    (byte) 0x56, 0x00, 0x00, 0x00,
                    (byte) 0xC8, 0x00, 0x00, 0x00
                    /*
                    //v248
                    (byte) 0x18, 0x00, 0x00, 0x00,
                    (byte) 0x64, 0x00, 0x00, 0x00,
                    (byte) 0x85, 0x00, 0x00, 0x00,
                    (byte) 0xF8, 0x00, 0x00, 0x00,
                    (byte) 0x96, 0x00, 0x00, 0x00,
                    (byte) 0x16, 0x00, 0x00, 0x00,
                    (byte) 0xD4, 0x00, 0x00, 0x00,
                    (byte) 0xD0, 0x00, 0x00, 0x00
                    //v246
                    (byte) 0x02, 0x00, 0x00, 0x00,
                    (byte) 0x2C, 0x00, 0x00, 0x00,
                    (byte) 0x12, 0x00, 0x00, 0x00,
                    (byte) 0x9E, 0x00, 0x00, 0x00,
                    (byte) 0xD4, 0x00, 0x00, 0x00,
                    (byte) 0x59, 0x00, 0x00, 0x00,
                    (byte) 0x32, 0x00, 0x00, 0x00,
                    (byte) 0xCF, 0x00, 0x00, 0x00
                    (byte) 0x29, 0x00, 0x00, 0x00,
                    (byte) 0xE1, 0x00, 0x00, 0x00,
                    (byte) 0x52, 0x00, 0x00, 0x00,
                    (byte) 0xF1, 0x00, 0x00, 0x00,
                    (byte) 0xB3, 0x00, 0x00, 0x00,
                    (byte) 0x87, 0x00, 0x00, 0x00,
                    (byte) 0x24, 0x00, 0x00, 0x00,
                    (byte) 0x06, 0x00, 0x00, 0x00
                    */
            };
        }

        gVersion = v;
        sVersion = (short) ((((0xFFFF - v) >>> 8) & 0xFF) | (((0xFFFF - v) << 8) & 0xFF00));
        rVersion = (short) (((v >>> 8) & 0xFF) | ((v << 8) & 0xFF00));
    }

    public static void encInit(int seqSnd, byte[] delta, boolean decrypt) {
        for (int i = 0; i < delta.length; i++) {
            if (decrypt) {
                delta[i] -= (byte) seqSnd;
            } else {
                delta[i] += (byte) seqSnd;
            }
        }
    }

    /**
     * Cryptography segment of MapleAESOFB.
     *
     * @param delta the input data to be put into stage for finalized encryption
     *              or to be finally decryption.
     * @param gamma the input seed for this cryptography stage. This value is
     *              renewed after each encryption by the corresponding encoder or decoder.
     * @return the bytes having been converted to a stage for encryption or
     * being fully decrypted.
     */
    public byte[] crypt(byte[] delta, byte[] gamma) {
        int a = delta.length;
        int b = 0x5B0;
        int c = 0;
        while (a > 0) {
            byte[] d = multiplyBytes(gamma, 4, 4);
            if (a < b) {
                b = a;
            }
            for (int e = c; e < (c + b); e++) {
                if ((e - c) % d.length == 0) {
                    try {
                        cipher.encrypt(d);
                    } catch (Exception ex) {
                        ex.printStackTrace(); // may eventually want to remove this
                    }
                }
                delta[e] ^= d[(e - c) % d.length];
            }
            c += b;
            a -= b;
            b = 0x5B4;
        }
        return delta;
    }

    /**
     * Creates a header for the new packet to be sent to the opposite
     * end of the session (Server to Channel for this engine). Contains
     * information about the net.swordie.ms.connection.packet: whether or not it is valid and the actual
     * length of the net.swordie.ms.connection.packet.
     *
     * @param delta the input packet length before adding the header.
     * @param gamma the input sending seed before changing it.
     * @return the header to be sent with this net.swordie.ms.connection.packet.
     */
    public static byte[] getHeader(int delta, byte[] gamma) {
        if (delta >= 0xFF00) {
            return getHeaderNew(delta, gamma);
        }
        int a = (gamma[3] ^ sVersion) & 0xFF;
        a |= ((gamma[2] << 8) ^ sVersion) & 0xFF00;
        int b = ((delta << 8) & 0xFF00) | (delta >>> 8);
        int c = a ^ b;
        byte[] ret = new byte[4];
        ret[0] = (byte) ((a >>> 8) & 0xFF);
        ret[1] = (byte) (a & 0xFF);
        ret[2] = (byte) ((c >>> 8) & 0xFF);
        ret[3] = (byte) (c & 0xFF);
        return ret;
    }

    public static byte[] getHeaderNew(int delta, byte[] gamma) {
        int iv = ((gamma[3] & 0xFF) | ((gamma[2] << 8) & 0xFF00)) ^ sVersion;
        byte[] ret = new byte[8];
        ret[0] = (byte) ((iv >>> 8) & 0xFF);
        ret[1] = (byte) (iv & 0xFF);
        ret[2] = ret[0];
        ret[3] = (byte) (ret[1] ^ 0xFF);
        ret[4] = (byte) ((delta & 0xFF) ^ ret[0]);
        ret[5] = (byte) (((delta >>> 8) & 0xFF) ^ ret[1]);
        ret[6] = (byte) (delta >>> 16 & 0xFF);
        ret[7] = (byte) (delta >>> 24 & 0xFF);

        return ret;
    }

    /**
     * Gets the length of the packet given the received header.
     *
     * @param delta the packet header to be used to find the packet length.
     * @return the length of the received net.swordie.ms.connection.packet.
     */
    public static int getLength(int delta) {
        int a = ((delta >>> 16) ^ (delta & 0xFFFF));
        a = ((a << 8) & 0xFF00) | ((a >>> 8) & 0xFF);
        return a;
    }

    /**
     * Checks the packet to make sure it is valid and that the session
     * between the net.swordie.ms.client and server is secure and legitimate.
     *
     * @param delta the packet header from the received packet (4 bytes in length).
     * @param gamma the current receive seed.
     * @return whether or not the packet is valid (consequently, if not valid,
     * the session is terminated usually).
     */
    public static boolean checkPacket(byte[] delta, byte[] gamma) {
        return ((((delta[0] ^ gamma[2]) & 0xFF) == ((rVersion >>> 8) & 0xFF))
                && (((delta[1] ^ gamma[3]) & 0xFF) == (rVersion & 0xFF)));
    }

    /**
     * @param delta foreign header.
     * @param gamma current receive seed.
     * @see MapleCrypto#checkPacket(byte[], byte[]) same thing as this method,
     * except that we convert the integer packet header (4 bytes combined)
     * into an array of the first 2 bytes of the integer packet header.
     */
    public static boolean checkPacket(int delta, byte[] gamma) {
        byte[] a = new byte[2];
        a[0] = (byte) ((delta >>> 24) & 0xFF);
        a[1] = (byte) ((delta >>> 16) & 0xFF);
        return checkPacket(a, gamma);
    }

    /**
     * Shuffles the seed (or IV) used for this cryptography stage.
     * Almost always called when this cryptography stage is finished since
     * rolling the seed is important to keeping a valid session.
     *
     * @param delta the old seed or IV to be changed into the new one.
     * @return the new seed or IV to be used for this stage of cryptography
     * for the next packet sent or received.
     */
    public static byte[] getNewIv(byte[] delta) {
        byte[] ret = delta;
        int[] nIv = {0xF2, 0x53, 0x50, 0xC6};
        for (int i = 0; i < 4; i++) {
            int a = (ret[i] & 0xFF);
            int b = SHUFFLE_BYTES[a];
            nIv[0] += SHUFFLE_BYTES[nIv[1]] - a;
            nIv[1] -= nIv[2] ^ b;
            nIv[2] ^= SHUFFLE_BYTES[nIv[3]] + a;
            nIv[3] -= nIv[0] - b;
            int c = nIv[0] & 0xFF;
            c |= (nIv[1] << 8) & 0xFF00;
            c |= (nIv[2] << 16) & 0xFF0000;
            c |= (nIv[3] << 24) & 0xFF000000;
            int d = (c << 3) | (c >>> 0x1D);
            nIv[0] = (d & 0xFF);
            nIv[1] = ((d >>> 8) & 0xFF);
            nIv[2] = ((d >>> 16) & 0xFF);
            nIv[3] = ((d >>> 24) & 0xFF);
        }
        for (int i = 0; i < 4; i++) {
            ret[i] = (byte) nIv[i];
        }
        return ret;
    }
}
