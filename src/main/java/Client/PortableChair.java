package Client;

import tools.Triple;

public class PortableChair {
    private int itemId;
    private int type;
    private int meso;
    private String msg;
    private int unk;
    private Triple[] arr;
    private int un2;
    private int un3;
    private int un4;
    private byte un5;

    public PortableChair(int itemId) {
        this.itemId = itemId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMeso() {
        return meso;
    }

    public void setMeso(int meso) {
        this.meso = meso;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getUnk() {
        return unk;
    }

    public void setUnk(int unk) {
        this.unk = unk;
    }

    public Triple[] getArr() {
        return arr;
    }

    public void setArr(Triple[] arr) {
        this.arr = arr;
    }

    public int getUn2() {
        return un2;
    }

    public void setUn2(int un2) {
        this.un2 = un2;
    }

    public int getUn3() {
        return un3;
    }

    public void setUn3(int un3) {
        this.un3 = un3;
    }

    public int getUn4() {
        return un4;
    }

    public void setUn4(int un4) {
        this.un4 = un4;
    }

    public byte getUn5() {
        return un5;
    }

    public void setUn5(byte un5) {
        this.un5 = un5;
    }
}
