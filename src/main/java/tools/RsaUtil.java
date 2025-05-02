package tools;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaUtil {
    // 2048位公钥和私钥，以Base64编码字符串表示
    private static final String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvDr4mReaXRCNapgtK3zrHa7Hqyl/rLPkhcqZa4kM2Vo7EJNzx94PP7asqpFWCrpP0zlRXtyS4y4URGVVFCflItBzYu/9uKMYt7M97sjF9U9XR5HpvhiNEL2fcYrAE1SBB2GfwUj+NIY3o7Ek3gsmqOsdUPFNQhKSMzRF0TDGy1xa3jdncFYTYIk/wqt0Y6dTzjQsV7anIHK1L7s69XeF8IZhlKXZ9cWsI9V3PFg1bcXwAZqbLIPxe/AByFIG8HwxqLam+1XAlF6uNxP+JtJAgzsrxEs+mKFjqjjDG+GcjM5hdyKcTj6M3p6aqqn0dWvZYcDIvsmb+PCBH2oVnnvstwIDAQAB";
    private static final String privateKeyStr = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC8OviZF5pdEI1qmC0rfOsdrserKX+ss+SFyplriQzZWjsQk3PH3g8/tqyqkVYKuk/TOVFe3JLjLhREZVUUJ+Ui0HNi7/24oxi3sz3uyMX1T1dHkem+GI0QvZ9xisATVIEHYZ/BSP40hjejsSTeCyao6x1Q8U1CEpIzNEXRMMbLXFreN2dwVhNgiT/Cq3Rjp1PONCxXtqcgcrUvuzr1d4XwhmGUpdn1xawj1Xc8WDVtxfABmpssg/F78AHIUgbwfDGotqb7VcCUXq43E/4m0kCDOyvESz6YoWOqOMMb4ZyMzmF3IpxOPozenpqqqfR1a9lhwMi+yZv48IEfahWee+y3AgMBAAECggEBALomo+ZbVljFXuXVO46viqIfUO3wK4jzm4PJOnMD5cYqmxEoki9OZ8w974L8GpifawRcwiFLiKTN1FNT15EIZ25HsmVdLJHmEjLSO+SjgEq7PXjVT9Gk3BV7Qmz6qDw1rSlMnb58XpI5Tls/fFNvubsCUkrTaPF4BkhHYOiiEWfY6t2UCl/oMFGqtRTG8RkLHbtpiJ/jX72/fizgN3xdhSkC8bYvuKfFA0Igvp8BG/UZ35WVetfkLjZaV+hM8V40C5KCpmo0xm/4ITIt2INdfjXyOxjF3oefb08IQ6ZdpLqLVn8OCU9Wyz41I8y1Mzl2Bb4w7tsk38NW5Qb3Bfqo2KECgYEA4Rt4otCimjJzE2I8sT7CDVbunqqEyxmCMMT6JU2I+DZb3Ib3dyyg+jp8tUI+0N6a4EPvYBTHsvd9F18Kat2yTF2z1ScbmCPOMdQ385B3X0GbJx+ZWpAidnjnSbOGGGH2lrYEuv5nJULS0Hw83JQXTgRmyO3aCkKyWaCtV2oHajECgYEA1g/t2KFBTIALndBRu0ezW7bAkhEsOHjnJAMd/7RaoW9K9PCKqAk7BfGH3kdHozPgZzQ5/+AfJ8KlA6PjSA5b+RJQeTcbOW7P+IQe79KN4741bZEEWZ0TlhvobxGG/t4wPH/3WR/Xvfq5gYlqEGSF0E7ZXXphY0DR3k4sif9To2cCgYEAkoPu4Qoqy3JKtDMcjcDrTQNoDJ2wEQFpW5TZu63bmLLI16CBMXA61qN6x/92IzzAUXfmNgNQd4veP3f8r9HcWxgiFHp/22ZyrrwSLtW43Kc29R/8EJX/2FyZLb9LaFNazH1sVsl/GSGFVW2Hr4o0IPN8cwtc/5CpxOxv5pV8fgECgYEAoCpTAF/HiAHWr0ILpWFEpj7bX29R8v8jkyJx871ygo/POe4xEQG3E/9gdcRHqalLIm1FaFq9dPaCmXKqwGNeFcLKS6gbyqFLIttYeoDEgb70IwL6ikKpQQuoolIu+8wrs1jcLedReWh8HJxAq/tK1E3q/bJv49/AJ+G+GZLddckCgYAnbS6qTP6dfehXuAt5QUv24vHLdQJuYKGG6HNdoVbRH171j42RMQis1/ovbLqkNNKr1cmt0HvfPb2CnnRNETbgh05c0FpnDiHLQ2a9dSa2Bu51GvwduPKP12Y4H6vNS3YmgEfVPgkbDJdLgY3dZyJTyVxrBGvkJJWbFTYgB/w4IQ==";

    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    static {
        try {
            // 初始化公钥
            byte[] publicBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(publicSpec);

            // 初始化私钥
            byte[] privateBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateBytes);
            privateKey = keyFactory.generatePrivate(privateSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 公钥加密
    public static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);  // 将加密后的字节数组转为Base64编码
    }

    // 私钥解密
    public static String decrypt(String encryptedText) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);  // Base64解码
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);  // 解密后的字节数组转为字符串
    }

    public static void main(String[] args) throws Exception {
        try {
            String originalText = "This is a secret message";

            // 使用公钥加密
            String encryptedText = encrypt(originalText);
            System.out.println("Encrypted Text: " + encryptedText);

            // 使用私钥解密
            String decryptedText = decrypt(encryptedText);
            System.out.println("Decrypted Text: " + decryptedText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
