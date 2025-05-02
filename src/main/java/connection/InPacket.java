package connection;

import Config.constants.ServerConstants;
import SwordieX.util.Position;
import SwordieX.util.Rect;
import SwordieX.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import tools.data.MaplePacketReader;

import java.nio.charset.Charset;
import java.util.Arrays;

public class InPacket extends Packet {
    private final ByteBuf byteBuf;
    private boolean loopback;
    private short packetID;
    private static final Charset CHARSET = ServerConstants.MapleType.getByType(ServerConstants.MapleRegion).getCharset();

    /**
     * Creates a new InPacket with a given buffer.
     *
     * @param byteBuf The buffer this InPacket has to be initialized with.
     */
    public InPacket(ByteBuf byteBuf) {
        super(byteBuf.array());
        this.byteBuf = byteBuf.copy();
    }

    /**
     * Creates a new InPacket with no data.
     */
    public InPacket() {
        this(Unpooled.buffer());
    }

    /**
     * Creates a new InPacket with given data.
     *
     * @param data The data this InPacket has to be initialized with.
     */
    public InPacket(byte[] data) {
        this(Unpooled.copiedBuffer(data));
    }

    @Override
    public int getLength() {
        return byteBuf.capacity();
    }

    @Override
    public byte[] getData() {
        return byteBuf.array();
    }

    @Override
    public InPacket clone() {
        return new InPacket(byteBuf);
    }

    /**
     * Reads a single byte of the ByteBuf.
     *
     * @return The byte that has been read.
     */
    public byte decodeByte() {
        return byteBuf.readByte();
    }

    public short decodeUByte() {
        return byteBuf.readUnsignedByte();
    }

    public boolean decodeBoolean() {
        return byteBuf.readBoolean();
    }

    /**
     * Reads an <code>amount</code> of bytes from the ByteBuf.
     *
     * @param amount The amount of bytes to read.
     * @return The bytes that have been read.
     */
    public byte[] decodeArr(int amount) {
        byte[] arr = new byte[amount];
        if (amount > byteBuf.readableBytes()) {
            throw new IndexOutOfBoundsException("緩衝區中沒有足夠的位元組來讀取.");
        }
        for (int i = 0; i < amount; i++) {
            arr[i] = byteBuf.readByte();
        }
        return arr;
    }

    /**
     * Reads an integer from the ByteBuf.
     *
     * @return The integer that has been read.
     */
    public int decodeInt() {
        return byteBuf.readIntLE();

    }

    /**
     * Reads a short from the ByteBuf.
     *
     * @return The short that has been read.
     */
    public short decodeShort() {
        return byteBuf.readShortLE();
    }

    /**
     * Reads a char array of a given length of this ByteBuf.
     *
     * @param amount The length of the char array
     * @return The char array as a String
     */
    public String decodeString(int amount) {
        return new String(decodeArr(amount), CHARSET);
    }

    /**
     * Reads a String, by first reading a short, then reading a char array of that length.
     *
     * @return The char array as a String
     */
    public String decodeString() {
        int amount = decodeShort();
        if (amount > byteBuf.readableBytes()) {
            throw new IndexOutOfBoundsException("緩衝區中沒有足夠的位元組來讀取.");
        }
        return decodeString(amount);
    }

    public byte[] decodeRawString() {
        int amount = decodeShort();
        if (amount > byteBuf.readableBytes()) {
            throw new IndexOutOfBoundsException("緩衝區中沒有足夠的位元組來讀取.");
        }
        return decodeArr(amount);
    }

    @Override
    public String toString() {
        return Util.readableByteArray(Arrays.copyOfRange(getData(), getData().length - getUnreadAmount(), getData().length)); // Substring after copy of range xd
    }

    public String toString(int length) {
        return Util.readableByteArray(Arrays.copyOfRange(getData(), length, getData().length)); // Substring after copy of range xd
    }

    /**
     * Reads and returns a long from this net.swordie.ms.connection.packet.
     *
     * @return The long that has been read.
     */
    public long decodeLong() {
        return byteBuf.readLongLE();
    }

    /**
     * Reads a position (short x, short y) and returns this.
     *
     * @return The position that has been read.
     */
    public Position decodePosition() {
        return new Position(decodeShort(), decodeShort());
    }

    /**
     * Reads a position (int x, int y) and returns this.
     *
     * @return The position that has been read.
     */
    public Position decodePositionInt() {
        return new Position(decodeInt(), decodeInt());
    }

    /**
     * Reads a rectangle (short l, short t, short r, short b) and returns this.
     *
     * @return The rectangle that has been read.
     */
    public Rect decodeShortRect() {
        return new Rect(decodePosition(), decodePosition());
    }

    /**
     * Reads a rectangle (int l, int t, int r, int b) and returns this.
     *
     * @return The rectangle that has been read.
     */
    public Rect decodeIntRect() {
        return new Rect(decodePositionInt(), decodePositionInt());
    }

    public int decodeZigZagVarints() {
        int n = decodeVarints();
        return (n >> 1) ^ -(n & 1);
    }

    public int decodeVarints() {
        int ret = 0;
        int offset = 0;
        while (true) {
            int n = decodeByte();
            if ((n & 0x80) != 0x80) {
                ret |= (n << offset);
                return ret;
            } else {
                ret |= ((n & 0x7F) << offset);
                offset += 7;
            }
        }
    }

    public int decodeReversedVarints() {
        int ret = 0;
        int offset = 0;
        while (true) {
            int n = decodeByte();
            if ((n & 1) != 1) {
                ret |= ((n >> 1) << offset);
                return ret;
            } else {
                ret |= (((n & 0xFE) >> 1) << offset);
                offset += 7;
            }
        }
    }

    /**
     * Returns the amount of bytes that are unread.
     *
     * @return The amount of bytes that are unread.
     */
    public int getUnreadAmount() {
        return byteBuf.readableBytes();
    }

    public void release() {
        byteBuf.release();
    }

    public int readerIndex() {
        return byteBuf.readerIndex();
    }

    public void readerIndex(int var1) {
        byteBuf.readerIndex(var1);
    }

    public MaplePacketReader toPacketReader() {
        MaplePacketReader reader = new MaplePacketReader(byteBuf.array());
        reader.seek(readerIndex());
        return reader;
    }

    public boolean isLoopback() {
        return loopback;
    }

    public void setLoopback(boolean loopback) {
        this.loopback = loopback;
    }

    public short getPacketID() {
        return packetID;
    }
}