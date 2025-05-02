package Config.constants.enums;


public enum CashItemModFlag {

    // 道具ID
    ITEM_ID(0x1),
    // 數量
    COUNT(0x2),
    // 價錢(打折後)
    PRICE(0x4),
    //
    BONUS(0x8),
    // 排序
    PRIORITY(0x10),
    // 道具時間
    PERIOD(0x20),
    // 楓葉點數
    MAPLE_POINT(0x40),
    // 楓幣
    MESO(0x80),
    //
    FOR_PREMIUM_USER(0x100),
    // 性別
    COMMODITY_GENDER(0x200),
    // 出售中
    ON_SALE(0x400),
    // 商品狀態 0-NEW,1-SALE,2-HOT,3-EVENT,其他-無
    CLASS(0x800),
    //
    LIMIT(0x1000),
    //
    PB_CASH(0x2000),
    //
    PB_POINT(0x4000),
    //
    PB_GIFT(0x8000),
    // 套組訊息
    PACKAGE_SN(0x10000),
    // 需求人氣
    REQ_POP(0x20000),
    // 需求等級
    REQ_LEV(0x40000),
    // 開始販售時間
    TERM_START(0x80000),
    // 結束販售時間
    TERM_END(0x100000),
    //
    REFUNDABLE(0x200000),
    //
    BOMB_SALE(0x400000),
    // 分類訊息
    CATEGORY_INFO(0x800000),
    // 伺服限制
    WORLD_LIMIT(0x1000000),
    //
    TOKEN(0x2000000),
    // 限量
    LIMIT_MAX(0x4000000),
    // 需求任務
    CHECK_QUEST_ID(0x8000000),
    // 原始價格
    ORIGINAL_PRICE(0x10000000),
    // 打折
    DISCOUNT(0x20000000),
    // 折扣率
    DISCOUNT_RATE(0x40000000),
    // 里程折扣與是否衹能使用里程
    MILEAGE_INFO(0x80000000),
    //
    ZERO(0x100000000L),
    // 需求任務2
    CHECK_QUEST_ID_2(0x200000000L),
    //
    UNK34(0x400000000L),
    //
    UNK35(0x800000000L),
    //
    UNK36(0x1000000000L),
    //
    COUPON_TYPE(0x2000000000L),
    //
    UNK38(0x4000000000L),
    //
    UNK39(0x8000000000L),
    //
    UNK40(0x10000000000L),
    //
    UNK41(0x20000000000L),
    //
    UNK42(0x40000000000L),
    //
    UNK43(0x80000000000L),
    //
    UNK44(0x100000000000L),
    ;

    private final long value;

    CashItemModFlag(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public boolean contains(long flags) {
        return (flags & value) != 0;
    }
}
