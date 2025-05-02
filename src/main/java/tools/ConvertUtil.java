package tools;

//ConvertUtil.java

import java.math.BigInteger;

/**
 * <p>
 * ConvertUtil<br>
 * 转换工具
 * </p>
 *
 * @author Heping_Ge2333
 * @version 1.0
 * @since 2022/12/23
 */
public class ConvertUtil {

    private ConvertUtil() {
    }

    /**
     * 将字节数组转换为字符串
     *
     * @param bytes 待转换的字节数组
     * @return 转换成的字符串
     */
    public static String convertBytesToString(byte[] bytes) {
        BigInteger no = new BigInteger(1, bytes);
        // Convert message digest into hex value
        String text = no.toString(16);
        while (text.length() < 32) {
            text = "0" + text;
        }
        return text;
    }

}

