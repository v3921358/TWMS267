package Plugin.provider.wz;

import Plugin.provider.wz.util.WzLittleEndianAccessor;

import java.util.LinkedList;
import java.util.List;

public class WzHeader {
    public final String Ident;
    public final String Copyright;
    public final long FSize;
    public final int FStart;
    public final long DataStartPosition;

    public WzHeader(WzLittleEndianAccessor slea) {
        this.Ident = slea.readAsciiString(4);
        this.FSize = slea.readLong();
        this.FStart = slea.readInt();
        this.Copyright = slea.readNullTerminatedAsciiString();

        boolean encverMissing = false;
        int encver = -1;

        if (FSize >= 2) {
            slea.seek(FStart);
            encver = slea.readShort();

            // encver always less than 256
            if (encver > 0xff) {
                encverMissing = true;
            } else if (encver == 0x80) {
                // there's an exceptional case that the first field of data part is a compressed int which determined property count,
                // if the value greater than 127 and also to be a multiple of 256, the first 5 bytes will become to
                //   80 00 xx xx xx
                // so we additional check the int value, at most time the child node count in a wz won't greater than 65536.
                if (FSize >= 5) {
                    slea.seek(FStart);
                    int propCount = slea.readCompressedInt();
                    if (propCount > 0 && (propCount & 0xff) == 0 && propCount <= 0xffff) {
                        encverMissing = true;
                    }
                }
            }
        } else {
            // Obviously, if data part have only 1 byte, encver must be deleted.
            encverMissing = true;
        }

        DataStartPosition = FStart + (encverMissing ? 0 : 2);

        if (encverMissing) {
            // not sure if nexon will change this magic version, just hard coded.
            this.SetWzVersion(777);
            //this.VersionChecked = true;
        } else {
            this.SetOrdinalVersionDetector(encver);
        }

        TryGetNextVersion();
        slea.hash = getHashVersion();
    }

    private IWzVersionDetector versionDetector;

    public int getWzVersion() {
        return versionDetector == null ? 0 : versionDetector.getWzVersion();
    }

    public int getHashVersion() {
        return versionDetector == null ? 0 : versionDetector.getHashVersion();
    }

    public void TryGetNextVersion() {
        if (versionDetector != null) {
            versionDetector.TryGetNextVersion();
        }
    }

    public void SetWzVersion(int wzVersion) {
        this.versionDetector = new FixedVersion(wzVersion);
    }

    public void SetOrdinalVersionDetector(int encryptedVersion) {
        this.versionDetector = new OrdinalVersionDetector(encryptedVersion);
    }

    public static int CalcHashVersion(int wzVersion) {
        int sum = 0;
        String versionStr = String.valueOf(wzVersion);
        for (int j = 0; j < versionStr.length(); j++) {
            sum <<= 5;
            sum += Integer.parseInt(String.valueOf(versionStr.charAt(j))) + 1;
        }
        return sum;
    }

    public interface IWzVersionDetector {
        void TryGetNextVersion();

        int getWzVersion();

        int getHashVersion();
    }

    public static class FixedVersion implements IWzVersionDetector {
        public FixedVersion(int wzVersion) {
            this.WzVersion = wzVersion;
            this.HashVersion = CalcHashVersion(wzVersion);
        }

        private boolean hasReturned;
        public int WzVersion;
        public int HashVersion;

        @Override
        public int getWzVersion() {
            return WzVersion;
        }

        @Override
        public int getHashVersion() {
            return HashVersion;
        }

        @Override
        public void TryGetNextVersion() {
            if (!hasReturned) {
                hasReturned = true;
            }
        }
    }

    public static class OrdinalVersionDetector implements IWzVersionDetector {
        public OrdinalVersionDetector(int encryptVersion) {
            this.EncryptedVersion = encryptVersion;
            this.versionTest = new LinkedList<>();
            this.hasVersionTest = new LinkedList<>();
            this.startVersion = -1;
        }

        public int EncryptedVersion;
        private int startVersion;
        private final List<Integer> versionTest;
        private final List<Integer> hasVersionTest;

        @Override
        public int getWzVersion() {
            int idx = this.versionTest.size() - 1;
            return idx > -1 ? this.versionTest.get(idx) : 0;
        }

        @Override
        public int getHashVersion() {
            int idx = this.hasVersionTest.size() - 1;
            return idx > -1 ? this.hasVersionTest.get(idx) : 0;
        }

        @Override
        public void TryGetNextVersion() {
            for (int i = startVersion + 1; i < Short.MAX_VALUE; i++) {
                int sum = CalcHashVersion(i);
                int enc = 0xff
                        ^ ((sum >> 24) & 0xFF)
                        ^ ((sum >> 16) & 0xFF)
                        ^ ((sum >> 8) & 0xFF)
                        ^ ((sum) & 0xFF);

                // if encver does not passed, try every version one by one
                if (enc == this.EncryptedVersion) {
                    this.versionTest.add(i);
                    this.hasVersionTest.add(sum & 0x7FFFFFFF);
                    startVersion = i;
                    return;
                }
            }
        }
    }
}