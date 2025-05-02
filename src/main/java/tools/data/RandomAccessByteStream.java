/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * Provides an abstract layer to a byte stream. This layer can be accessed
 * randomly.
 *
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
public class RandomAccessByteStream implements ByteStream {

    private int pos = 0;
    private final RandomAccessFile raf;
    private long bytesRead = 0;

    /**
     * Class constructor. Wraps this object around a RandomAccessFile.
     *
     * @param raf The RandomAccessFile instance to wrap this around.
     * @see RandomAccessFile
     */
    public RandomAccessByteStream(final RandomAccessFile raf) {
        super();
        this.raf = raf;
    }

    public final long getPosition() {
        return pos;
        // return raf.getFilePointer();
    }

    public final void seek(long offset) throws IOException {
        pos = (int) offset;
        raf.seek(offset);
    }

    @Override
    public long getBytesRead() {
        return bytesRead;
    }

    /**
     * Reads a byte off of the file.
     *
     * @return The byte read as an integer.
     */
    public final int readByte() {
        int temp;
        try {
            temp = (byte) raf.read();
            pos++;
            bytesRead++;
            return temp & 0xFF;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final String toString(final boolean b) { // ?
        return toString();
    }

    /**
     * Get the number of bytes available for reading.
     *
     * @return The number of bytes available for reading as a long integer.
     */
    public final long available() {
        try {
            return raf.length() - raf.getFilePointer();
        } catch (IOException e) {
            System.err.println("ERROR" + e);
            return 0;
        }
    }

    public final byte[] toByteArray() throws IOException {
        int nRead;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[16384];
        while ((nRead = raf.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public final void write(final int b) {
        try {
            raf.write(b);
            pos++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void write(final byte[] b) {
        for (int x = 0; x < b.length; x++) {
            write(b[x]);
        }
    }

    public final void writeShort(final int i) {
        write((byte) (i & 0xFF));
        write((byte) ((i >>> 8) & 0xFF));
    }

    public final void writeInt(final int i) {
        write((byte) (i & 0xFF));
        write((byte) ((i >>> 8) & 0xFF));
        write((byte) ((i >>> 16) & 0xFF));
        write((byte) ((i >>> 24) & 0xFF));
    }

    public final void writeAsciiString(String s) {
        write(s.getBytes(CHARSET));
    }

    public final void writeLong(final long l) {
        write((byte) (l & 0xFF));
        write((byte) ((l >>> 8) & 0xFF));
        write((byte) ((l >>> 16) & 0xFF));
        write((byte) ((l >>> 24) & 0xFF));
        write((byte) ((l >>> 32) & 0xFF));
        write((byte) ((l >>> 40) & 0xFF));
        write((byte) ((l >>> 48) & 0xFF));
        write((byte) ((l >>> 56) & 0xFF));
    }

    public final void close() throws IOException {
        raf.close();
    }
}
