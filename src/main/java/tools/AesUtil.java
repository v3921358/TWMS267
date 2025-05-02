package tools;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AesUtil {
    private static final int GCM_TAG_LENGTH = 16; // GCM 標籤長度
    private static final int IV_LENGTH = 12; // GCM 模式通常使用 12 字節的 IV

    private static Cipher cipher; // 保留 Cipher 實例
    private static final Map<String, String> cache = new HashMap<>(); // 緩存解密內容

    static {
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成 AES 密鑰
    public static SecretKeySpec generateKey(String deviceId, String salt, int iterations, int keyLength) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(deviceId.toCharArray(), salt.getBytes(), iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    // AES 加密，並將隨機 IV 附加到密文中
    public static String encrypt(String data, SecretKeySpec key) throws Exception {
        // 生成隨機 IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        // 初始化 Cipher
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        // 執行加密操作
        byte[] encrypted = cipher.doFinal(data.getBytes());

        // 將 IV 附加到密文的前面並進行 Base64 編碼
        byte[] encryptedWithIv = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, IV_LENGTH); // 附加 IV
        System.arraycopy(encrypted, 0, encryptedWithIv, IV_LENGTH, encrypted.length); // 附加密文

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    // AES 解密，從加密數據中提取 IV 並解密
    public static String decrypt(String encryptedData, SecretKeySpec key) throws Exception {

        // 解碼 Base64 並提取 IV
        byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedData);
        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted = new byte[encryptedWithIv.length - IV_LENGTH];

        System.arraycopy(encryptedWithIv, 0, iv, 0, IV_LENGTH); // 提取 IV
        System.arraycopy(encryptedWithIv, IV_LENGTH, encrypted, 0, encrypted.length); // 提取密文

        // 初始化 Cipher
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        // 執行解密操作
        byte[] decrypted = cipher.doFinal(encrypted);
        String decryptedString = new String(decrypted);

        // 將解密內容放入緩存
        cache.put(HashUtil.md5(encryptedData), decryptedString);

        return decryptedString;
    }

    public static String decryptScriptByMachineHash(String deviceId, String salt, String encryptedString) throws Exception {
        // 檢查緩存
        var md5Key = HashUtil.md5(encryptedString);
        if (cache.containsKey(md5Key)) {
            return cache.get(md5Key);
        }

        SecretKeySpec key = generateKey(deviceId, salt, 100, 128);
        return decrypt(encryptedString, key);
    }

    public static void main(String[] args) throws Exception {
        String deviceId = "601e2b0a7fd7fd455d1b3aabe3ef92b89a3fe3a67d1b5fc2874af50f0a612ec85ad4325e0bebe817012e446717ee132b6ae56d1650f54cf470e0da802985292"; // 替换为设备唯一标识符
        String salt = "MapleStory"; // 可以使用随机生成的盐值，增强安全性
        SecretKeySpec key = generateKey(deviceId, salt, 100, 128); // 生成256位AES密钥

        String data = "var top = 666;"; // 要加密的数据

        // 加密
        String encryptedData = encrypt(data, key);
        System.out.println("Encrypted: " + encryptedData);

        // 解密
        String decryptedData = decrypt(encryptedData, key);
        System.out.println("Decrypted: " + decryptedData);
    }
}
