package Config.$Crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class SecureEncryption {

    private static final String ALGORITHM = "AES/CTR/NoPadding"; // Use a secure mode
    private static final int KEY_SIZE_BYTES = 16; // Example: 128-bit key
    private static final int CHUNK_SIZE = 4096; // Process data in chunks

    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        if (key.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException("Invalid key size");
        }
        byte[] iv = new byte[KEY_SIZE_BYTES];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Initialize the Cipher object
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        // Encrypt the data in chunks
        byte[] encryptedData = new byte[0];
        for (int i = 0; i < data.length; i += CHUNK_SIZE) {
            int endIndex = Math.min(i + CHUNK_SIZE, data.length);
            byte[] chunk = Arrays.copyOfRange(data, i, endIndex);
            byte[] encryptedChunk = cipher.update(chunk);
            encryptedData = concatenateByteArrays(encryptedData, encryptedChunk);
        }
        encryptedData = concatenateByteArrays(encryptedData, cipher.doFinal());
        return concatenateByteArrays(iv, encryptedData);
    }

    public static byte[] decrypt(byte[] encryptedData, byte[] key) throws Exception {
        byte[] iv = Arrays.copyOfRange(encryptedData, 0, KEY_SIZE_BYTES);
        byte[] ciphertext = Arrays.copyOfRange(encryptedData, KEY_SIZE_BYTES, encryptedData.length);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        return cipher.doFinal(ciphertext);
    }

    private static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}