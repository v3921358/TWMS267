package Server.auction;

public class AuctionOptType {
    /**
     * 進入拍賣
     */
    public static final int INIT = 0;
    /**
     * 離開拍賣
     */
    public static final int EXIT = 1;
    /**
     * 上架道具
     */
    public static final int SET_ITEM = 10; // 261
    /**
     * 重新上架
     */
    public static final int RESET_ITEM = 11;
    /**
     * 下架道具
     */
    public static final int DELETE_ITEM = 12;
    /**
     * 購買道具
     */
    public static final int BUY_ITEM = 20;
    /**
     * 購買道具_帶數量
     */
    public static final int BUY_ITEM_BUNDLE = 21;
    /**
     * 領取報酬
     */
    public static final int RECEIVE_PAYMENT = 30;
    /**
     * 領取物品
     */
    public static final int RETURN_ITEM = 31;
    /**
     * 首頁搜索
     */
    public static final int LOAD_ALL_ITEM = 40;
    /**
     * 行情搜索
     */
    public static final int LOAD_QUOTATION = 41;
    /**
     * 添加收藏物品
     */
    public static final int ADD_COLLECTION = 45;
    /**
     * 收藏窗口
     */
    public static final int LOAD_COLLECTION = 46;
    /**
     * 刪除收藏物品
     */
    public static final int DELETE_COLLECTION = 47;
    /**
     * 出售窗口
     */
    public static final int LOAD_SELL = 50;
    /**
     * 完成窗口
     */
    public static final int LOAD_STORE = 51;
    /**
     * 更新狀態
     */
    public static final int UPDATE_STATE = 70;
    /**
     * 更新完成狀態
     */
    public static final int UPDATE_STATE_FINISH = 71;
    /**
     * 更新道具數量
     */
    public static final int UPDATE_ITEM_COUNT = 72;
    /**
     * 更新道具數量狀態
     */
    public static final int UPDATE_ITEM_COUNT_STATE = 73;
    /**
     * 道具不存在
     */
    public static final int ITEM_NOT_EXIST = 102;
    /**
     * 楓幣不足
     */
    public static final int NOT_ENOUGH_MONEY = 106;
    /**
     * 背包已滿
     */
    public static final int BAG_IS_FULL = 116;

}
