package Client.inventory;

public enum ItemAttribute {

    Seal(0x01), //鎖定
    NonSlip(0x02), //鞋子防滑
    ColdProof(0x04), //披風防寒
    TradeBlock(0x08),//不可交換
    TradeOnce(0x10),//可以交換1次
    GetCharm(0x20),//裝備時獲得魅力
    AndroidActivated(0x40),
    Crafted(0x80),
    NonCurse(0x100), //防爆卷軸
    LuckyChance(0x200), //幸運日卷軸
    CutUsed(0x400),//宿命剪刀
    Exclusive(0x800),//固有
    AccountSharable(0x1000),//轉存吊牌
    ProtectRUC(0x2000), //保護升級次數
    ProtectScroll(0x4000), //卷軸防護
    RegressScroll(0x8000), // 恢復卡
    Hyalinize(0x20000000),//變透明
    // 楓方塊可剪刀一次狀態
    AnimaCube(0x40000000);
    private final int i;

    ItemAttribute(int i) {
        this.i = i;
    }

    public int getValue() {
        return i;
    }

    public boolean check(int flag) {
        return (flag & i) == i;
    }
}
