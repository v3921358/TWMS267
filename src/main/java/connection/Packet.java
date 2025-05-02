package connection;

import SwordieX.util.Util;

public class Packet implements Cloneable {

    private byte[] data;

    public Packet(byte[] data) {
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public int getLength() {
        if (data != null) {
            return data.length;
        }
        return 0;
    }

    public short getHeader() {
        if (data.length < 2) {
            return (short) 0xFFFF;
        }
        return (short) ((data[0] & 0xFF) + ((data[1] & 0xFF) << 8));
    }

    public void setData(byte[] nD) {
        data = nD;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        if (data == null) return "";
        return "[Pck] | " + Util.readableByteArray(data);
    }

    @Override
    public Packet clone() {
        return new Packet(data);
    }

    public void release() {
    }
}
