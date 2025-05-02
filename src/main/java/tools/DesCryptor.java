package tools;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

public class DesCryptor {

    private Key DESKEY;

    public DesCryptor(final String key) {
        this.init(key);
    }

    public DesCryptor() {
        this.init("*%(*&#(^");
    }

    private void init(final String key) {
        try {
            this.DESKEY = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public final String encrypt(String str) {
        try {
            return Base64.getEncoder().encodeToString(this.desEncrypt(str.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public final String decrypt(String str) {
        try {
            return new String(this.desDecrypt(Base64.getDecoder().decode(str)), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private byte[] desEncrypt(byte[] data) {
        try {
            final Cipher instance;
            (instance = Cipher.getInstance("DES")).init(1, this.DESKEY, SecureRandom.getInstance("SHA1PRNG"));
            return instance.doFinal(data);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private byte[] desDecrypt(byte[] data) {
        try {
            final Cipher instance;
            (instance = Cipher.getInstance("DES")).init(2, this.DESKEY, SecureRandom.getInstance("SHA1PRNG"));
            return instance.doFinal(data);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
