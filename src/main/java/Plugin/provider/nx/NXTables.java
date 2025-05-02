package Plugin.provider.nx;

import Plugin.provider.nx.util.NxLittleEndianAccessor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.awt.image.BufferedImage;

public class NXTables {
    private AudioBuf[] audioBufs;
    private Bitmap[] bitmaps;
    private String[] strings;

    public NXTables(NxLittleEndianAccessor lea) {
        strings = new String[(int) lea.Header.StringCount];
        audioBufs = new AudioBuf[(int) lea.Header.SoundCount];
        bitmaps = new Bitmap[(int) lea.Header.BitmapCount];
    }

    public ByteBuf getAudioBuf(NxLittleEndianAccessor lea, long index, long length) {
        checkIndex(audioBufs, index);
        if (audioBufs[(int) index] == null) {
            long mark = lea.getPosition();
            try {
                lea.seek(lea.Header.SoundOffset + index * 8);
                audioBufs[(int) index] = new AudioBuf(lea);
            } finally {
                lea.seek(mark);
            }
        }
        return audioBufs[(int) index].getAudioBuf(length);
    }

    public BufferedImage getImage(NxLittleEndianAccessor lea, long index, int width, int height) {
        checkIndex(bitmaps, index);
        if (bitmaps[(int) index] == null) {
            long mark = lea.getPosition();
            try {
                lea.seek(lea.Header.BitmapOffset + index * 8);
                bitmaps[(int) index] = new Bitmap(lea);
            } finally {
                lea.seek(mark);
            }
        }
        return bitmaps[(int) index].getImage(width, height);
    }

    public String getString(NxLittleEndianAccessor lea, long index) {
        checkIndex(strings, index);
        if (strings[(int) index] == null) {
            strings[(int) index] = lea.readMapleString((int) index);
        }
        return strings[(int) index];
    }

    /**
     * Checks if the offset index is legal.
     *
     * @param index the index to check
     * @throws RuntimeException if the offset index is not legal
     */
    protected void checkIndex(Object[] array, long index) {
        if (index > Integer.MAX_VALUE || index >= array.length) {
            throw new RuntimeException("pkgnx cannot support offset indices over " + Integer.MAX_VALUE);
        }
    }

    /**
     * A lazy-loaded equivalent of {@code ByteBuf}.
     *
     * @author Aaron Weiss
     * @version 1.0
     * @since 5/27/13
     */
    protected static class AudioBuf {

        private final NxLittleEndianAccessor lea;
        private final long audioOffset;
        private ByteBuf audioBuf;

        /**
         * Creates a lazy-loaded {@code ByteBuf} for audio.
         *
         * @param lea
         */
        public AudioBuf(NxLittleEndianAccessor lea) {
            this.lea = lea;
            audioOffset = lea.readLong();
        }

        /**
         * Loads a {@code ByteBuf} of the desired {@code length}.
         *
         * @param length the length of the audio data
         * @return the audio buffer
         */
        public ByteBuf getAudioBuf(long length) {
            if (audioBuf == null) {
                lea.seek(audioOffset);
                audioBuf = Unpooled.wrappedBuffer(lea.read((int) length));
            }
            return audioBuf;
        }
    }

    /**
     * A lazy-loaded equivalent of {@code BufferedImage}.
     *
     * @author Aaron Weiss
     * @version 1.0
     * @since 5/27/13
     */
    protected static class Bitmap {

        private final NxLittleEndianAccessor lea;
        private final long bitmapOffset;

        /**
         * Creates a lazy-loaded {@code BufferedImage}.
         *
         * @param lea
         */
        public Bitmap(NxLittleEndianAccessor lea) {
            this.lea = lea;
            bitmapOffset = lea.readLong();
        }

        /**
         * Loads a {@code BufferedImage} of the desired {@code width} and {@code height}.
         *
         * @param width  the width of the image
         * @param height the height of the image
         * @return the loaded image
         */
        public BufferedImage getImage(int width, int height) {
            lea.seek(bitmapOffset);
            /*
            ByteBuf image = Unpooled.wrappedBuffer(
                    Decompressor.decompress(lea.read((int) lea.readUInt()), width * height * 4));
            BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    int b = image.readUnsignedByte();
                    int g = image.readUnsignedByte();
                    int r = image.readUnsignedByte();
                    int a = image.readUnsignedByte();
                    ret.setRGB(w, h, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
            return ret;
            */
            return null;
        }
    }
}
