package Net.auth.client;

import tools.newCRand32;
import tools.Randomizer;

import java.io.Serializable;

public class SimpleCryptStatus implements Serializable {
    private final byte[] key;
    private final byte[] ivSend;
    private final byte[] ivRecv;
    private final newCRand32 rand = new newCRand32();

    public SimpleCryptStatus() {
        key = new byte[32];
        ivSend = new byte[4];
        ivRecv = new byte[4];
        initialize();
    }

    public SimpleCryptStatus(byte[] key, byte[] ivRecv, byte[] ivSend, int seed1, int seed2, int seed3) {
        this.key = key;
        this.ivRecv = ivRecv;
        this.ivSend = ivSend;
        rand.seed(seed1, seed2, seed3);
    }

    private void initialize() {
        for (int i = 0; i < 4; i++) {
            key[i * 4] = (byte) Randomizer.nextInt(255);
            key[(i + 4) * 4] = (byte) Randomizer.nextInt(255);
            ivSend[i] = (byte) Randomizer.nextInt(255);
            ivRecv[i] = (byte) Randomizer.nextInt(255);
        }
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getIvSend() {
        return ivSend;
    }

    public byte[] getIvRecv() {
        return ivRecv;
    }

    public long getTick() {
        return rand.random();
    }
}
