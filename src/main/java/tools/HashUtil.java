package tools;

//HashUtil.java

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>
 * HashUtil<br>
 * 哈希算法工具
 * </p>
 *
 * @author Heping_Ge2333
 * @version 1.0
 * @since 2022/12/23
 */
@Slf4j
public class HashUtil {

    /**
     * MD5
     */
    public static final String MD5 = "MD5";
    /**
     * SHA-256
     */
    public static final String SHA_256 = "SHA-256";
    /**
     * SHA-512
     */
    public static final String SHA_512 = "SHA-512";

    private static final String NO_SUCH_ALGO_ERROR_MSG = "找不到指定的算法";

    private HashUtil() {
    }

    /**
     * 获取对象的哈希值
     *
     * @param algo 算法名称
     * @param msg 原字符串
     * @return 字节数组形式的哈希值
     */
    public static byte[] getHash(String algo, String msg) {
        try {
            MessageDigest md = MessageDigest.getInstance(algo);
            md.update(msg.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest();
            return bytes;
        } catch (NoSuchAlgorithmException e) {
            log.error(NO_SUCH_ALGO_ERROR_MSG + " " + algo + " ！", e);
            return null;
        }
    }

    /**
     * 获取对象的MD5哈希值
     *
     * @param obj 原对象
     * @return 原对象的MD5值
     */
    public static String md5(Object obj) {
        return ConvertUtil.convertBytesToString(getHash(MD5, obj.toString()));
    }

    /**
     * 获取对象的MD5哈希值
     *
     * @param obj 原对象
     * @return 原对象的MD5值
     */
    public static String sha256(Object obj) {
        return ConvertUtil.convertBytesToString(getHash(SHA_256, obj.toString()));
    }

    /**
     * 获取对象的MD5哈希值
     *
     * @param obj 原对象
     * @return 原对象的MD5值
     */
    public static String sha512(Object obj) {
        return ConvertUtil.convertBytesToString(getHash(SHA_512, obj.toString()));
    }

}

