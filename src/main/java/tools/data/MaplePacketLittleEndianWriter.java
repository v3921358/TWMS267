package tools.data;

import Config.constants.ServerConstants;
import Opcode.Headler.OutHeader;
import tools.HexTool;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Writes a maplestory-packet little-endian stream of bytes.
 */
public final class MaplePacketLittleEndianWriter {

    private final ByteArrayOutputStream baos;
    private static final Charset CHARSET = ServerConstants.MapleType.getByType(ServerConstants.MapleRegion).getCharset();
    private static final Map<String, byte[]> HexMap = new HashMap<>();

    /**
     * Constructor - initializes this stream with a default size.
     */
    public MaplePacketLittleEndianWriter() {
        this(32);
    }

    /**
     * Constructor - initializes this stream with size
     * <code>size</code>.
     *
     * @param size The size of the underlying stream.
     */
    public MaplePacketLittleEndianWriter(int size) {
        this.baos = new ByteArrayOutputStream(size);
    }

    public MaplePacketLittleEndianWriter(OutHeader opcode) {
        this(32);
        writeShort(opcode.getValue());
    }

    /**
     * Write the number of zero bytes
     */
    public void writeZeroBytes(final int i) {
        for (int x = 0; x < i; x++) {
            baos.write(0);
        }
    }

    /**
     * Write an array of bytes to the stream.
     *
     * @param b The bytes to write.
     */
    public void write(final byte[] b) {
        baos.write(b, 0, b.length);
    }

    /**
     * Write a byte to the stream.
     *
     * @param b The byte to write.
     */
    public void write(final byte b) {
        baos.write(b);
    }

    /**
     * Write a byte in integer form to the sequence.
     *
     * @param b The byte as an Integer to write.
     */
    public void write(final int b) {
        baos.write((byte) b);
    }


    /**
     * 以Boolean類型來寫入一個Byte
     *
     * @param b The byte as an Boolean to write.
     */
    public void write(final boolean b) {
        baos.write((byte) (b ? 1 : 0));
    }

    /**
     * Write a short integer to the stream.
     *
     * @param i The short integer to write.
     * @return
     */
    public int writeShort(final int i) {
        baos.write((byte) (i & 0xFF));
        baos.write((byte) ((i >>> 8) & 0xFF));
        return i;
    }

    /**
     * Writes an integer to the stream.
     *
     * @param i The integer to write.
     * @return
     */
    public int writeInt(final int i) {
        baos.write((byte) (i & 0xFF));
        baos.write((byte) ((i >>> 8) & 0xFF));
        baos.write((byte) ((i >>> 16) & 0xFF));
        baos.write((byte) ((i >>> 24) & 0xFF));
        return i;
    }

    public void writeInt(final long n) {
        baos.write((byte) (n & 0xFFL));
        baos.write((byte) (n >>> 8 & 0xFFL));
        baos.write((byte) (n >>> 16 & 0xFFL));
        baos.write((byte) (n >>> 24 & 0xFFL));
    }

    public void writeReversedInt(final long l) {
        baos.write((byte) ((l >>> 32) & 0xFF));
        baos.write((byte) ((l >>> 40) & 0xFF));
        baos.write((byte) ((l >>> 48) & 0xFF));
        baos.write((byte) ((l >>> 56) & 0xFF));
    }

    /**
     * Writes an CHARSET string the the stream.
     *
     * @param s The CHARSET string to write.
     */
    public void writeAsciiString(final String s) {
        write(s.getBytes(CHARSET));
    }

    /**
     * Writes a null-terminated CHARSET string to the sequence.
     *
     * @param s   The CHARSET string to write.
     * @param max The maximum length of the string
     */
    public void writeAsciiString(final String s, final int max) {
        if (s == null) {
            return;
        }
        byte[] bytes = s.getBytes(CHARSET);
        int len = Math.min(s.getBytes(CHARSET).length, max);
        write(bytes);
        for (int i = len; i < max; i++) {
            write(0);
        }
    }

    /**
     * Writes a Maple Name CHARSET string to the sequence.
     *
     * @param s The CHARSET string to write.
     */
    public void writeMapleNameString(String s) {
        if (s.getBytes().length > 13) {
            s = s.substring(0, 13);
        }
        writeAsciiString(s);
        for (int x = s.getBytes().length; x < 13; x++) {
            write(0);
        }
    }

    /**
     * Writes a maple-convention CHARSET string to the stream.
     *
     * @param s The CHARSET string to use maple-convention to write.
     */
    public void writeMapleAsciiString(final String s) {
        if (s == null) {
            writeShort(0);
            return;
        }
        writeShort(s.getBytes(CHARSET).length);
        writeAsciiString(s);
    }

    public void writeMapleAsciiString(String s, final int max) {
        writeShort(max);
        writeAsciiString(s, max);
    }

    public void writeMapleAsciiString(String[] arrstring) {
        int n2 = 0;
        for (String string : arrstring) {
            if (string != null) {
                n2 += string.getBytes(CHARSET).length;
            }
        }
        if (n2 < 1) {
            writeShort(0);
            return;
        }
        writeShort((short) (n2 + arrstring.length - 1));
        for (int i = 0; i < arrstring.length; ++i) {
            if (arrstring[i] != null) {
                writeAsciiString(arrstring[i]);
            }
            if (i < arrstring.length - 1) {
                write(0);
            }
        }
    }

    /**
     * Writes a 2D 4 byte position information
     *
     * @param s The Point position to write.
     */
    public void writePos(final Point s) {
        writeShort(s.x);
        writeShort(s.y);
    }

    /**
     * Writes a 2D 8 byte position information
     *
     * @param s The Point position to write.
     */
    public void writePosInt(Point s) {
        writeInt(s.x);
        writeInt(s.y);
    }

    /**
     * Writes a 4 int 16 byte Rectangle information
     *
     * @param s The Rectangle to write.
     */
    public void writeRect(final Rectangle s) {
        writeInt(s.x);
        writeInt(s.y);
        writeInt(s.x + s.width);
        writeInt(s.y + s.height);
    }

    /**
     * Write a long integer to the stream.
     *
     * @param l The long integer to write.
     */
    public void writeLong(final long l) {
        writeInt((int) l);
        writeReversedInt(l);
    }

    public void writeReversedLong(final long l) {
        writeReversedInt(l);
        writeInt((int) l);
    }

    /**
     * 寫入布爾值 true ? 1 : 0
     *
     * @param b The boolean to write.
     */
    public void writeBool(final boolean b) {
        write(b ? 1 : 0);
    }

    /**
     * 寫入反向布爾值 true ? 0 : 1
     *
     * @param b The boolean to write.
     */
    public void writeReversedBool(final boolean b) {
        write(b ? 0 : 1);
    }

    public void writeDouble(final double b) {
        writeLong(Double.doubleToLongBits(b));
    }

    public void writeHexString(final String s) {
        write(HexMap.computeIfAbsent(s, k -> HexTool.getByteArrayFromHexString(s)));
    }

    public void writeOpcode(WritableIntValueHolder op) {
        writeShort(op.getValue());
    }

    public void writeFile(final File file) {
        byte[] bytes = new byte[0];
        if (file != null && file.exists()) {
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                System.err.println("檔案太大");
            } else {
                bytes = new byte[(int) length];
                try (FileInputStream is = new FileInputStream(file)) {
                    int numRead = is.read(bytes);
                    writeInt(numRead);
                    write(bytes);
                } catch (IOException e) {
                    System.err.println("讀取檔案失敗:" + e);
                }
            }
        } else {
            writeInt(0);
        }
    }

    public void writeZigZagVarints(int b) {
        b = (b << 1) ^ (b >> 31);
        writeVarints(b);
    }

    public void writeVarints(int b) {
        while (true) {
            if ((b & (~0x7F)) == 0) {
                write(b);
                break;
            } else {
                write((b & 0x7F) | 0x80);
                b >>= 7;
            }
        }
    }

    public void writeReversedVarints(int b) {
        while (true) {
            if ((b & (~0x7F)) == 0) {
                write(b << 1);
                break;
            } else {
                write(((b & 0x7F) << 1) | 1);
                b >>= 7;
            }
        }
    }

    /**
     * Gets a
     * <code>MaplePacket</code> instance representing this sequence of bytes.
     *
     * @return A <code>MaplePacket</code> with the bytes in this stream.
     */
    public byte[] getPacket() {
        return baos.toByteArray();
    }

    /**
     * Changes this packet into a human-readable hexadecimal stream of bytes.
     *
     * @return This packet as hex digits.
     */
    @Override
    public String toString() {
        return HexTool.toString(baos.toByteArray());
    }
}
