package Client;

public class VMatrixSlot {
    private int slot;
    private int index = -1;
    private int extend = 0;
    private int unlock = 0;

    public VMatrixSlot() {
    }

    public VMatrixSlot(final int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getExtend() {
        return extend;
    }

    public void setExtend(int extend) {
        this.extend = extend;
    }

    public int getUnlock() {
        return unlock;
    }

    public void setUnlock(int unlock) {
        this.unlock = unlock;
    }
}
