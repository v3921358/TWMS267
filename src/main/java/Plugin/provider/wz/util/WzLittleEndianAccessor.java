package Plugin.provider.wz.util;

import Plugin.provider.wz.WzHeader;
import tools.data.ByteStream;
import tools.data.MaplePacketReader;

public class WzLittleEndianAccessor extends MaplePacketReader {

    public int hash;
    public WzHeader header;

    public enum WzMapleVersion {
        GMS, EMS, BMS, CLASSIC, GENERATE,
    }

    private static final byte[] encKey = null;

    static {
        byte[] iv;
        // WZ_MSEAIV
        // WZ_GMSIV
        iv = null;
    }

    public WzLittleEndianAccessor(ByteStream bs) {
        super(bs);
    }

    public String readStringAtOffset(long offset) {
        return readStringAtOffset(offset, false);
    }

    public String readStringAtOffset(long offset, boolean readByte) {
        long currentOffset = getPosition();
        seek(offset);
        if (readByte) {
            readByte();
        }
        String returnString = readString();
        seek(currentOffset);
        return returnString;
    }

    private String readString() {
        byte smallLength = readByte();

        if (smallLength == 0x00) {
            return "";
        }

        int length;
        StringBuilder retString = new StringBuilder();
        if (smallLength > 0) { // Unicode
            int mask = 0xAAAA;
            if (smallLength == Byte.MAX_VALUE) {
                length = readInt();
            } else {
                length = smallLength;
            }
            if (length <= 0) {
                return "";
            }

            for (int i = 0; i < length; i++) {
                short encryptedChar = readShort();
                encryptedChar ^= (short) mask;
                encryptedChar ^= (short) (((encKey == null ? 0 : encKey[i * 2 + 1]) << 8) + (encKey == null ? 0 : encKey[i * 2]));
                retString.append((char) encryptedChar);
                mask++;
            }
        } else { // ASCII
            byte mask = (byte) 0xAA;
            if (smallLength == Byte.MIN_VALUE) {
                length = readInt();
            } else {
                length = -smallLength;
            }
            if (length < 0) {
                return "";
            }

            for (int i = 0; i < length; i++) {
                byte encryptedChar = readByte();
                encryptedChar ^= mask;
                encryptedChar ^= (byte) (encKey == null ? 0 : encKey[i]);
                retString.append((char) encryptedChar);
                mask++;
            }
        }
        return retString.toString();
    }

    public int readCompressedInt() {
        byte sb = readByte();
        if (sb == Byte.MIN_VALUE) {
            return readInt();
        }
        return sb;
    }

    public long readLongValue() {
        byte b = readByte();
        if (b == Byte.MIN_VALUE) {
            return readLong();
        }
        return b;
    }

    public float readFloatValue() {
        byte b = readByte();
        if (b == Byte.MIN_VALUE) {
            return readFloat();
        }
        return 0.0f;
    }

    public long readOffset() {
        long offset = getPosition();
        offset = ~(offset - header.FStart);
        offset *= hash;
        offset -= 0x581C3F6D;
        int distance = (int) offset & 0x1F;
        offset = (offset << distance) | (offset >> (32 - distance));
        long encryptedOffset = readUInt();
        offset ^= encryptedOffset;
        offset += header.FStart * 2L;
        return offset;
    }

    public String readStringBlock(long offset) {
        byte b = readByte();
        switch (b) {
            case 0x00, 0x03, 0x04, 0x73 -> {
                return readString();
            }
            case 0x01, 0x02, 0x1B -> {
                return readStringAtOffset(readInt() + offset);
            }
            default ->
                    throw new RuntimeException("Unknown extension image identifier: " + b + " at offset " + (getPosition() - offset));
        }
    }
}
