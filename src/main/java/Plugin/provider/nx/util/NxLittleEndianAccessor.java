package Plugin.provider.nx.util;

import Plugin.provider.nx.NXHeader;
import tools.data.ByteStream;
import tools.data.MaplePacketReader;

import java.nio.charset.StandardCharsets;

public class NxLittleEndianAccessor extends MaplePacketReader {
    public final NXHeader Header;

    public NxLittleEndianAccessor(ByteStream bs) {
        this(bs, null);
    }

    public NxLittleEndianAccessor(ByteStream bs, NXHeader header) {
        super(bs);
        if (header == null) {
            Header = new NXHeader(this);
        } else {
            Header = header;
        }
    }

    public String readString(int n) {
        byte ret[] = new byte[n];
        for (int x = 0; x < n; x++) {
            ret[x] = readByte();
        }
        try {
            return new String(ret, StandardCharsets.UTF_8);
        } catch (Exception e) {
        }
        return "";
    }

    public String readMapleString(int index) {
        long mark = getPosition();
        seek(Header.StringOffset + index * 8L);
        seek(readLong());
        String ReturnString = readMapleString();
        seek(mark);
        return ReturnString;
    }

    public String readMapleString() {
        int size = readUShort();
        if (size < 0) {
            throw new NegativeArraySizeException("readMapleString size=" + size);
        }
        return readString(size);
    }
}
