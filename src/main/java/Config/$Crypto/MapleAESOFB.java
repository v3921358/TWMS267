package Config.$Crypto;

import tools.BitTools;
import tools.HexTool;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author TMS-HERTZ
 * @version 262.5
 * @return key以及加解密處理
 */
public class MapleAESOFB {

    private static final String[] skeys = new String[]{
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

    /**
     * @author by Taiwan twms and SyncTwMs team [ Hertz ]  fix...
     * @version TMS v245
     * 註記: KEY ZLZ.DLL
     * 時間: 2023-8
     */

    private static SecretKeySpec skey = new SecretKeySpec(new byte[]{
            /* v258_OBT */
            (byte) 0x5A, 0x00, 0x00, 0x00,
            (byte) 0x2A, 0x00, 0x00, 0x00,
            (byte) 0xA1, 0x00, 0x00, 0x00,
            (byte) 0xB4, 0x00, 0x00, 0x00,
            (byte) 0x32, 0x00, 0x00, 0x00,
            (byte) 0x4D, 0x00, 0x00, 0x00,
            (byte) 0x56, 0x00, 0x00, 0x00,
            (byte) 0xC8, 0x00, 0x00, 0x00}, "AES");


    private static final byte[] funnyBytes = new byte[]{(byte) 0xEC, (byte) 0x3F, (byte) 0x77, (byte) 0xA4, (byte) 0x45, (byte) 0xD0, (byte) 0x71, (byte) 0xBF, (byte) 0xB7, (byte) 0x98, (byte) 0x20, (byte) 0xFC,
            (byte) 0x4B, (byte) 0xE9, (byte) 0xB3, (byte) 0xE1, (byte) 0x5C, (byte) 0x22, (byte) 0xF7, (byte) 0x0C, (byte) 0x44, (byte) 0x1B, (byte) 0x81, (byte) 0xBD, (byte) 0x63, (byte) 0x8D, (byte) 0xD4, (byte) 0xC3,
            (byte) 0xF2, (byte) 0x10, (byte) 0x19, (byte) 0xE0, (byte) 0xFB, (byte) 0xA1, (byte) 0x6E, (byte) 0x66, (byte) 0xEA, (byte) 0xAE, (byte) 0xD6, (byte) 0xCE, (byte) 0x06, (byte) 0x18, (byte) 0x4E, (byte) 0xEB,
            (byte) 0x78, (byte) 0x95, (byte) 0xDB, (byte) 0xBA, (byte) 0xB6, (byte) 0x42, (byte) 0x7A, (byte) 0x2A, (byte) 0x83, (byte) 0x0B, (byte) 0x54, (byte) 0x67, (byte) 0x6D, (byte) 0xE8, (byte) 0x65, (byte) 0xE7,
            (byte) 0x2F, (byte) 0x07, (byte) 0xF3, (byte) 0xAA, (byte) 0x27, (byte) 0x7B, (byte) 0x85, (byte) 0xB0, (byte) 0x26, (byte) 0xFD, (byte) 0x8B, (byte) 0xA9, (byte) 0xFA, (byte) 0xBE, (byte) 0xA8, (byte) 0xD7,
            (byte) 0xCB, (byte) 0xCC, (byte) 0x92, (byte) 0xDA, (byte) 0xF9, (byte) 0x93, (byte) 0x60, (byte) 0x2D, (byte) 0xDD, (byte) 0xD2, (byte) 0xA2, (byte) 0x9B, (byte) 0x39, (byte) 0x5F, (byte) 0x82, (byte) 0x21,
            (byte) 0x4C, (byte) 0x69, (byte) 0xF8, (byte) 0x31, (byte) 0x87, (byte) 0xEE, (byte) 0x8E, (byte) 0xAD, (byte) 0x8C, (byte) 0x6A, (byte) 0xBC, (byte) 0xB5, (byte) 0x6B, (byte) 0x59, (byte) 0x13, (byte) 0xF1,
            (byte) 0x04, (byte) 0x00, (byte) 0xF6, (byte) 0x5A, (byte) 0x35, (byte) 0x79, (byte) 0x48, (byte) 0x8F, (byte) 0x15, (byte) 0xCD, (byte) 0x97, (byte) 0x57, (byte) 0x12, (byte) 0x3E, (byte) 0x37, (byte) 0xFF,
            (byte) 0x9D, (byte) 0x4F, (byte) 0x51, (byte) 0xF5, (byte) 0xA3, (byte) 0x70, (byte) 0xBB, (byte) 0x14, (byte) 0x75, (byte) 0xC2, (byte) 0xB8, (byte) 0x72, (byte) 0xC0, (byte) 0xED, (byte) 0x7D, (byte) 0x68,
            (byte) 0xC9, (byte) 0x2E, (byte) 0x0D, (byte) 0x62, (byte) 0x46, (byte) 0x17, (byte) 0x11, (byte) 0x4D, (byte) 0x6C, (byte) 0xC4, (byte) 0x7E, (byte) 0x53, (byte) 0xC1, (byte) 0x25, (byte) 0xC7, (byte) 0x9A,
            (byte) 0x1C, (byte) 0x88, (byte) 0x58, (byte) 0x2C, (byte) 0x89, (byte) 0xDC, (byte) 0x02, (byte) 0x64, (byte) 0x40, (byte) 0x01, (byte) 0x5D, (byte) 0x38, (byte) 0xA5, (byte) 0xE2, (byte) 0xAF, (byte) 0x55,
            (byte) 0xD5, (byte) 0xEF, (byte) 0x1A, (byte) 0x7C, (byte) 0xA7, (byte) 0x5B, (byte) 0xA6, (byte) 0x6F, (byte) 0x86, (byte) 0x9F, (byte) 0x73, (byte) 0xE6, (byte) 0x0A, (byte) 0xDE, (byte) 0x2B, (byte) 0x99,
            (byte) 0x4A, (byte) 0x47, (byte) 0x9C, (byte) 0xDF, (byte) 0x09, (byte) 0x76, (byte) 0x9E, (byte) 0x30, (byte) 0x0E, (byte) 0xE4, (byte) 0xB2, (byte) 0x94, (byte) 0xA0, (byte) 0x3B, (byte) 0x34, (byte) 0x1D,
            (byte) 0x28, (byte) 0x0F, (byte) 0x36, (byte) 0xE3, (byte) 0x23, (byte) 0xB4, (byte) 0x03, (byte) 0xD8, (byte) 0x90, (byte) 0xC8, (byte) 0x3C, (byte) 0xFE, (byte) 0x5E, (byte) 0x32, (byte) 0x24, (byte) 0x50,
            (byte) 0x1F, (byte) 0x3A, (byte) 0x43, (byte) 0x8A, (byte) 0x96, (byte) 0x41, (byte) 0x74, (byte) 0xAC, (byte) 0x52, (byte) 0x33, (byte) 0xF0, (byte) 0xD9, (byte) 0x29, (byte) 0x80, (byte) 0xB1, (byte) 0x16,
            (byte) 0xD3, (byte) 0xAB, (byte) 0x91, (byte) 0xB9, (byte) 0x84, (byte) 0x7F, (byte) 0x61, (byte) 0x1E, (byte) 0xCF, (byte) 0xC5, (byte) 0xD1, (byte) 0x56, (byte) 0x3D, (byte) 0xCA, (byte) 0xF4, (byte) 0x05,
            (byte) 0xC6, (byte) 0xE5, (byte) 0x08, (byte) 0x49};

    private final short mapleVersion;
    /**
     * Logger for this class.
     */
    private byte[] iv;
    private Cipher cipher;

    public MapleAESOFB(byte[] iv, short mapleVersion, boolean bIsSend) {
        // TwMS規律金鑰處理
        if (mapleVersion >= 176) {
            byte[] skeyBytes = HexTool.getByteArrayFromHexString(skeys[mapleVersion % 20]);
            for (int i = 0; i < skeyBytes.length; i = i + 4) {
                for (int n = 1; n <= 3; n++) {
                    skeyBytes[i + n] = 0;
                }
            }
            skey = new SecretKeySpec(skeyBytes, "AES");
        }

        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.err.print("錯誤 " + e);
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, skey);
        } catch (InvalidKeyException e) {
            System.err.print("啟動加密計算工具錯誤.請確實你是否使用了無限制加固型密碼系統的(.jar)文件." + e);
        }
        this.setIv(iv);

        if (bIsSend) {
            mapleVersion = (short) (0xFFFF - mapleVersion);
        }

        this.mapleVersion = (short) (((mapleVersion >> 8) & 0xFF) | ((mapleVersion << 8) & 0xFF00));
    }

    public MapleAESOFB(byte[] key, byte[] iv, short mapleVersion) {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.err.print("错误 " + e);
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        } catch (InvalidKeyException e) {
            System.err.print("启动加密计算工具错误.请确实你是否使用了无限制加固型密码系统的(.jar)文件." + e);
        }
        this.setIv(iv);
        this.mapleVersion = (short) (((mapleVersion >> 8) & 0xFF) | ((mapleVersion << 8) & 0xFF00));
    }

    /**
     * Gets the packet length from a header.
     *
     * @param packetHeader The header as an integer.
     * @return The length of the packet.
     */
    public static int getPacketLength(int packetHeader) {
        int ivBytes = ((packetHeader >>> 24) & 0xFF) | ((packetHeader >>> 8) & 0xFF00);
        int xorredSize = ((packetHeader >>> 8) & 0xFF) | ((packetHeader << 8) & 0xFF00);
        return (xorredSize ^ ivBytes) & 0xFFFF;
    }

    public static int getLongPacketLength(int hword, int lword) {
        int ivBytes = ((hword >>> 24) & 0xFF) | ((hword >>> 8) & 0xFF00);
        return lword ^ ivBytes;
    }

    /**
     * Gets the packet length from a header.
     *
     * @param packetHeader The header as a byte array.
     * @return The length of the packet.
     */
    public static int getPacketLength(byte[] packetHeader) {
        if (packetHeader.length < 4) {
            return -1;
        }
        return (((packetHeader[0] ^ packetHeader[2]) & 0xFF) | (((packetHeader[1] ^ packetHeader[3]) << 8) & 0xFF00));
    }

    /**
     * Gets a new IV from
     * <code>oldIv</code>
     *
     * @param oldIv The old IV to get a new IV from.
     * @return The new IV.
     */
    public static byte[] getNewIv(byte[] oldIv) {
        byte[] in = {(byte) 0xf2, 0x53, (byte) 0x50, (byte) 0xc6}; // magic
        for (int x = 0; x < 4; x++) {
            funnyShit(oldIv[x], in);
            //System.out.println(HexTool.toString(in));
        }
        return in;
    }

    /**
     * Does funny stuff.
     * <code>this.OldIV</code> must not equal
     * <code>in</code> Modifies
     * <code>in</code> and returns it for
     * convenience.
     *
     * @param inputByte The byte to apply the funny stuff to.
     * @param in        Something needed for all this to occur.
     */
    public static void funnyShit(byte inputByte, byte[] in) {
        byte elina = in[1];
        byte moritz = funnyBytes[elina & 0xFF];
        moritz -= inputByte;
        in[0] += moritz;
        moritz = in[2];
        moritz ^= funnyBytes[inputByte & 0xFF];
        elina -= moritz & 0xFF;
        in[1] = elina;
        elina = in[3];
        moritz = elina;
        elina -= in[0] & 0xFF;
        moritz = funnyBytes[moritz & 0xFF];
        moritz += inputByte;
        moritz ^= in[2];
        in[2] = moritz;
        elina += funnyBytes[inputByte & 0xFF] & 0xFF;
        in[3] = elina;

        int merry = (in[0]) & 0xFF;
        merry |= (in[1] << 8) & 0xFF00;
        merry |= (in[2] << 16) & 0xFF0000;
        merry |= (in[3] << 24) & 0xFF000000;
        int ret_value = merry >>> 0x1d;
        merry <<= 3;
        ret_value |= merry;

        in[0] = (byte) (ret_value & 0xFF);
        in[1] = (byte) ((ret_value >> 8) & 0xFF);
        in[2] = (byte) ((ret_value >> 16) & 0xFF);
        in[3] = (byte) ((ret_value >> 24) & 0xFF);
    }

    /**
     * Credits to @Diamondo25 / @mapleshark
     *
     * @param pValue = value
     * @param pIv    = old Iv
     */
    public static void Morph(byte pValue, byte[] pIv) {
        byte tableInput = funnyBytes[pValue];
        pIv[0] += (byte) (funnyBytes[pIv[1]] - pValue);
        pIv[1] -= (byte) (pIv[2] ^ tableInput);
        pIv[2] ^= (byte) (funnyBytes[pIv[3]] + pValue);
        pIv[3] -= (byte) (pIv[0] - tableInput);

        int Value = (int) (pIv[0]) & 0xFF | (pIv[1] << 8) & 0xFF00 | (pIv[2] << 16) & 0xFF0000 | (pIv[3] << 24) & 0xFF000000;
        Value = (Value >> 0x1D | Value << 0x03);

        pIv[0] = (byte) (Value & 0xFF);
        pIv[1] = (byte) ((Value >> 8) & 0xFF);
        pIv[2] = (byte) ((Value >> 16) & 0xFF);
        pIv[3] = (byte) ((Value >> 24) & 0xFF);
    }


    /**
     * For debugging/testing purposes only.
     *
     * @return The IV.
     */
    public byte[] getIv() {
        return this.iv;
    }

    /**
     * Sets the IV of this instance.
     *
     * @param iv The new IV.
     */
    private void setIv(byte[] iv) {
        this.iv = iv;
    }

    /**
     * Encrypts
     * <code>data</code> and generates a new IV.
     *
     * @param data The bytes to encrypt.
     * @return The encrypted bytes.
     */
    public byte[] crypt(byte[] data) {
        int remaining = data.length;
        int llength = 0x5B0;
        int start = 0;

        try {
            while (remaining > 0) {
                byte[] myIv = BitTools.multiplyBytes(this.iv, 4, 4);
                if (remaining < llength) {
                    llength = remaining;
                }
                for (int x = start; x < (start + llength); x++) {
                    if ((x - start) % myIv.length == 0) {
                        byte[] newIv = cipher.doFinal(myIv);
                        System.arraycopy(newIv, 0, myIv, 0, myIv.length);
                        // System.out.println("Iv is now " + HexTool.toString(this.iv));
                    }
                    data[x] ^= myIv[(x - start) % myIv.length];
                }
                start += llength;
                remaining -= llength;
                llength = 0x5B4;
            }
            updateIv();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return data;
    }

    public byte[] crypt(byte[] data, boolean second) {
        if (!second) {
            return crypt(data);
        }
        final byte b = this.iv[0];
        int min;
        for (int length = data.length, i = 0; i < length; i += min) {
            min = Math.min((i == 0) ? 1456 : 1460, length - i);
            for (int j = 0; j < min; ++j) {
                final int n = i + j;
                data[n] += b;
            }
        }
        this.updateIv();
        return data;
    }

    public byte[] recvCrypt(byte[] data) {
        final byte b = this.iv[0];
        int min;
        for (int length = data.length, i = 0; i < length; i += min) {
            min = Math.min((i == 0) ? 1456 : 1460, length - i);
            for (int j = 0; j < min; ++j) {
                final int n = i + j;
                data[n] -= b;
            }
        }
        this.updateIv();
        return data;
    }

    /**
     * Generates a new IV.
     */
    private void updateIv() {
        this.iv = getNewIv(this.iv);
    }

    /**
     * Generates a packet header for a packet that is
     * <code>length</code>
     * long.
     *
     * @param length How long the packet that this header is for is.
     * @return The header.
     */
    public byte[] getPacketHeader(long length) {
        ByteBuffer buffer;
        long uSeqSnd = ((long)(this.iv[3] ^ mapleVersion) << 8) & 0xFF00 | (this.iv[2] ^ (mapleVersion >> 8)) & 0xFF;
        if (length >= 0xFF00) {
            buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putShort((short)uSeqSnd);
            buffer.putShort((short)(0xFF00 ^ uSeqSnd));
            buffer.putInt((int) (length ^ uSeqSnd));
        } else {
            buffer = ByteBuffer.allocate(4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putShort((short)uSeqSnd);
            buffer.putShort((short)(length ^ uSeqSnd));
        }
        return buffer.array();
    }

//    public static byte[] getNewIv(byte oldIv[]) {
//        byte[] newIv = {(byte) 0xf2, 0x53, (byte) 0x50, (byte) 0xc6};
//        for (int x = 0; x < 4; x++) {
//            Morph(oldIv[x], newIv);
//        }
//        return newIv;
//    }

    /**
     * Check the packet to make sure it has a header.
     *
     * @param packet The packet to check.
     * @return <code>True</code> if the packet has a correct header,
     * <code>false</code> otherwise.
     */
    public boolean checkPacket(byte[] packet) {
        return ((((packet[0] ^ iv[2]) & 0xFF) == ((mapleVersion >> 8) & 0xFF)) && (((packet[1] ^ iv[3]) & 0xFF) == (mapleVersion & 0xFF)));
    }

    /**
     * Check the header for validity.
     *
     * @param packetHeader The packet header to check.
     * @return <code>True</code> if the header is correct,
     * <code>false</code>
     * otherwise.
     */
    public boolean checkPacket(int packetHeader) {
        return checkPacket(new byte[]{(byte) ((packetHeader >> 24) & 0xFF), (byte) ((packetHeader >> 16) & 0xFF)});
    }

    /**
     * Returns the IV of this instance as a string.
     *
     * @return
     */
    @Override
    public String toString() {
        return "IV: " + HexTool.toString(this.iv);
    }

    /* 演算法 */
    private byte[] generateKeyFromVersion(short mapleVersion) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        byte[] versionBytes = new byte[2];
        versionBytes[0] = (byte) ((mapleVersion >> 8) & 0xFF);
        versionBytes[1] = (byte) (mapleVersion & 0xFF);
        byte[] combined = new byte[versionBytes.length + salt.length];
        System.arraycopy(versionBytes, 0, combined, 0, versionBytes.length);
        System.arraycopy(salt, 0, combined, versionBytes.length, salt.length);
        return java.security.MessageDigest.getInstance("SHA-256").digest(combined);
    }
}
