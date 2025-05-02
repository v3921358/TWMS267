/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.inventory;

/**
 * @author PlayDK
 */
public enum SocketFlag {

    可以鑲嵌(0x01),
    已打孔01(0x02),
    已打孔02(0x04),
    已打孔03(0x08),
    已鑲嵌01(0x10),
    已鑲嵌02(0x20),
    已鑲嵌03(0x40);
    private final int i;

    SocketFlag(int i) {
        this.i = i;
    }

    public short getValue() {
        return (short) i;
    }

    public boolean check(int flag) {
        return (flag & i) == i;
    }
}
