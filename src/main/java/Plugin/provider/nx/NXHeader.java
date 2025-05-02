package Plugin.provider.nx;

import Plugin.provider.nx.util.NxLittleEndianAccessor;

public class NXHeader {
    public final String Ident;
    public final long NodeCount, NodeOffset, StringCount, StringOffset, BitmapCount, BitmapOffset, SoundCount, SoundOffset;

    public NXHeader(NxLittleEndianAccessor nlea) {
        Ident = nlea.readString(4);
        if (!Ident.equals("PKG4"))
            throw new RuntimeException("Cannot read file. Invalid format " + Ident + ", expecting ");
        NodeCount = nlea.readUInt();
        NodeOffset = nlea.readLong();
        StringCount = nlea.readUInt();
        StringOffset = nlea.readLong();
        BitmapCount = nlea.readUInt();
        BitmapOffset = nlea.readLong();
        SoundCount = nlea.readUInt();
        SoundOffset = nlea.readLong();
    }
}
