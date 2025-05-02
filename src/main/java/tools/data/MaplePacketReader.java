package tools.data;

import Config.constants.ServerConstants;
import connection.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Provides an abstract accessor to a generic Little Endian byte stream. This
 * accessor is seekable.
 *
 * @author Frz
 * @version 1.0
 * @see MaplePacketReader
 * @since Revision 323
 */
public class MaplePacketReader {
    private static final Logger log = LoggerFactory.getLogger(MaplePacketReader.class);
    private final ByteStream bs;
    private static final Charset CHARSET = ServerConstants.MapleType.getByType(ServerConstants.MapleRegion).getCharset();
    private short header;

    /**
     * Class constructor Provide a seekable input stream to wrap this object
     * around.
     *
     * @param bs The byte stream to wrap this around.
     */
    public MaplePacketReader(ByteStream bs) {
        this.bs = bs;
//        this.header = readShort();
    }

    public MaplePacketReader(byte[] data) {
        this.bs = new ByteArrayByteStream(data);
    }

    public ByteStream getByteStream() {
        return this.bs;
    }

    /**
     * Seek the pointer to
     * <code>offset</code>
     *
     * @param offset The offset to seek to.
     * @see ByteArrayByteStream#seek
     */
    public void seek(long offset) {
        try {
            bs.seek(offset);
        } catch (IOException e) {
            System.err.println("Seek failed" + e);
        }
    }

    /**
     * Get the current position of the pointer.
     *
     * @return The current position of the pointer as a long integer.
     * @see ByteArrayByteStream#getPosition
     */
    public long getPosition() {
        return bs.getPosition();
    }

    /**
     * Skip
     * <code>num</code> number of bytes in the stream.
     *
     * @param num The number of bytes to skip.
     */
    public void skip(int num) {
        seek(getPosition() + num);
    }

    public short getHeader() {
        return header;
    }

    public void setHeader(short header) {
        this.header = header;
    }

    public int readByteAsInt() {
        return bs.readByte();
    }

    /**
     * Read a single byte from the stream.
     *
     * @return The byte read.
     * @see ByteArrayByteStream#readByte
     */
    public byte readByte() {
        return (byte) bs.readByte();
    }

    /**
     * Reads an integer from the stream.
     *
     * @return The integer read.
     */
    public int readInt() {
        int byte1 = bs.readByte();
        int byte2 = bs.readByte();
        int byte3 = bs.readByte();
        int byte4 = bs.readByte();
        // change 262
        return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    public final long readUInt() {
        int value = readInt();
        long value2 = 0;
        if (value < 0) {
            value2 += 0x80000000L;
        }
        return (value & 0x7FFFFFFF) + value2;
    }

    /**
     * Reads a short integer from the stream.
     *
     * @return The short read.
     */
    public short readShort() {
        int byte1 = bs.readByte();
        int byte2 = bs.readByte();
        return (short) ((byte2 << 8) + byte1);
    }

    /**
     * Reads a short integer from the stream.
     *
     * @return The short read.
     */
    public int readUShort() {
        int value = readShort();
        if (value < 0) {
            value += 65536;
        }
        return value;
    }

    /**
     * Reads a single character from the stream.
     *
     * @return The character read.
     */
    public char readChar() {
        return (char) readShort();
    }

    /**
     * Reads a long integer from the stream.
     *
     * @return The long integer read.
     */
    public long readLong() {
        long byte1 = bs.readByte();
        long byte2 = bs.readByte();
        long byte3 = bs.readByte();
        long byte4 = bs.readByte();
        long byte5 = bs.readByte();
        long byte6 = bs.readByte();
        long byte7 = bs.readByte();
        long byte8 = bs.readByte();
        return (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) + (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    /**
     * Reads a floating point integer from the stream.
     *
     * @return The float-type integer read.
     */
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads a double-precision integer from the stream.
     *
     * @return The double-type integer read.
     */
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Reads an ASCII string from the stream with length
     * <code>n</code>.
     *
     * @param n Number of characters to read.
     * @return The string read.
     */
    public String readAsciiString(int n) {
        byte[] ret = new byte[n];
        for (int x = 0; x < n; x++) {
            ret[x] = readByte();
        }
        try {
            return new String(ret, CHARSET);
        } catch (Exception e) {
            log.error("readAsciiString", e);
        }
        return "";
    }

    /**
     * Reads a null-terminated string from the stream.
     *
     * @return The string read.
     */
    public final String readNullTerminatedAsciiString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b;
        while (true) {
            b = readByte();
            if (b == 0) {
                break;
            }
            baos.write(b);
        }
        byte[] buf = baos.toByteArray();
        char[] chrBuf = new char[buf.length];
        for (int x = 0; x < buf.length; x++) {
            chrBuf[x] = (char) buf[x];
        }
        return String.valueOf(chrBuf);
    }

    /**
     * Gets the number of bytes read from the stream so far.
     *
     * @return A long integer representing the number of bytes read.
     * @see ByteArrayByteStream#getBytesRead()
     */
    public long getBytesRead() {
        return bs.getBytesRead();
    }

    /**
     * Reads a MapleStory convention lengthed ASCII string. This consists of a
     * short integer telling the length of the string, then the string itself.
     *
     * @return The string read.
     */
    public String readMapleAsciiString() {
        int size = readShort();
        if (size < 0) {
            throw new NegativeArraySizeException("readMapleAsciiString size=" + size);
        }
        return readAsciiString(size);
    }

    /**
     * Reads a MapleStory Position information. This consists of 2 short
     * integer.
     *
     * @return The Position read.
     */
    public Point readPos() {
        int x = readShort();
        int y = readShort();
        return new Point(x, y);
    }

    public Point readPosInt() {
        int x = readInt();
        int y = readInt();
        return new Point(x, y);
    }

    public Rectangle readRect() {
        int x = readInt();
        int y = readInt();
        return new Rectangle(x, y, readInt() - x, readInt() - y);
    }

    public int readZigZagVarints() {
        int n = readVarints();
        return (n >> 1) ^ -(n & 1);
    }

    public int readVarints() {
        int ret = 0;
        int offset = 0;
        while (true) {
            int n = readByteAsInt();
            if ((n & 0x80) != 0x80) {
                ret |= (n << offset);
                return ret;
            } else {
                ret |= ((n & 0x7F) << offset);
                offset += 7;
            }
        }
    }

    public int readReversedVarints() {
        int ret = 0;
        int offset = 0;
        while (true) {
            int n = readByteAsInt();
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
     * Reads
     * <code>num</code> bytes off the stream.
     *
     * @param num The number of bytes to read.
     * @return An array of bytes with the length of <code>num</code>
     */
    public byte[] read(int num) {
        byte[] ret = new byte[num];
        for (int x = 0; x < num; x++) {
            ret[x] = readByte();
        }
        return ret;
    }

    /**
     * @see ByteArrayByteStream#available
     */
    public long available() {
        return bs.available();
    }

    public InPacket toInPacket() {
        if (bs instanceof ByteArrayByteStream babs) {
            InPacket inPacket = new InPacket(babs.getBytes());
            inPacket.readerIndex((int) babs.getPosition());
            return inPacket;
        }
        return null;
    }

    /**
     * @see Object#toString
     */
    @Override
    public String toString() {
        return bs.toString();
    }

    public String toString(boolean b) {
        return bs.toString(b);
    }

    public boolean readBool() {
        return readByte() > 0;
    }
}
