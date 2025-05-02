package Net.auth.util;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA公钥/私钥/签名工具包
 * 字符串格式的密钥在未在特殊说明情况下都为BASE64编码格式.
 *
 * @author Ethan
 * @date 20170913
 */
public class RSAUtil {

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 245;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 256;

    public static final byte[] publicKey = {48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -33, -125, -54, -4, -52, -77, 51, 59, -23, 41, -19, 100, 6, -77, 47, 123, -28, -6, -113, 6, -51, 10, 80, -105, 46, -98, 29, -83, -62, 18, 49, 93, 116, -77, -102, -37, -100, -50, -49, 70, -41, -128, -14, 1, 31, -8, 118, 117, 25, 18, 34, 90, -123, -71, 47, -12, -33, 31, -63, 67, -57, 45, -62, -107, -9, -72, 51, 72, -77, -50, -55, 8, 67, 11, 123, -111, -51, -4, -84, -90, -126, -91, -7, 24, 73, -51, -88, -108, 18, 81, 82, 109, 77, 90, -98, 4, -4, -125, 96, 58, -60, -11, -6, 18, 24, 52, 53, 116, -5, 66, -101, 21, 89, -115, -8, 86, 127, 8, -57, 82, 69, 8, 50, -127, 28, 47, -75, -1, -9, -107, -35, -113, -85, -13, 126, -55, -41, -75, 108, -127, 101, -121, -27, -69, -87, -58, -36, -7, 72, -4, 47, -121, -112, -79, 104, -36, -90, -4, -9, 29, 58, 24, 88, 38, -66, 112, -75, -83, -80, 88, 66, 32, -121, -90, 95, -94, -63, -35, -27, -69, 80, -77, -38, 55, 83, 10, 50, -66, 94, -98, 127, -5, 78, -41, 124, -17, 95, 25, 125, -39, 108, -11, -14, 45, 54, 71, 121, -30, -47, 79, 61, 65, -3, 81, 66, 57, 30, 89, -12, 54, 93, -23, -27, 78, 45, -22, 123, 126, -73, 7, -119, -117, 14, -69, -68, 104, 2, -42, 49, 25, -54, 63, 120, -34, -45, -45, 112, -60, 68, 94, -39, -35, -77, 65, -44, 95, 2, 3, 1, 0, 1};

    /**
     * 校验数字签名
     *
     * @param data 已加密数据
     * @param sign 数字签名
     */
    public static boolean verify(byte[] data, String sign) throws Exception {
        EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64.getDecoder().decode(sign));
    }

    /**
     * 公钥解密
     *
     * @param encryptedData 已加密数据
     */
    public static byte[] decryptByPublicKey(byte[] encryptedData) throws Exception {
        EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * 公钥加密
     *
     * @param data 源数据
     */
    public static byte[] encryptByPublicKey(byte[] data) throws Exception {
        EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    public static short getPacketLength(byte[] header) {
        short b1 = (short) (((header[0] ^ 0x11) & 0xFF) << 0);
        short b2 = (short) (((header[1] ^ 0x22) & 0xFF) << 8);
        short b3 = (short) (((header[0] ^ 0x33) & 0xFF) << 16);
        short b4 = (short) (((header[1] ^ 0x44) & 0xFF) << 24);
        return (short) (b4 + b3 + b2 + b1 - 0x19970530);
    }

}
