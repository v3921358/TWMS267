/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.shop;

/**
 * @author PlayDK
 */
public enum MapleShopResponse {
    ShopRes_BuySuccess(0),
    ShopRes_BuyNoStock(1),
    ShopRes_BuyNoMoney(2),
    ShopRes_BuyNoPoint(3),
    ShopRes_BuyNoFloor(4),
    ShopRes_BuyNoQuest(5),// 不滿足任務要求，無法購買
    ShopRes_BuyNoQuestEx(6),
    ShopRes_BuyInvalidTime(7),
    ShopRes_NotEnoughSpace(10),
    ShopRes_Unk_9(9), // x階級開始可以使用
    ShopRes_166_Add_10(10), // 等級開始可以使用
    ShopRes_SellSuccess(11),
    ShopRes_SellNoStock(12),
    ShopRes_SellIncorrectRequest(13),
    ShopRes_SellOverflow(14),
    ShopRes_SellLimitPriceAtOnetime(15),
    ShopRes_SellUnkonwn(16),
    ShopRes_RechargeSuccess(17),
    ShopRes_RechargeNoStock(18),
    ShopRes_RechargeNoMoney(19),
    ShopRes_RechargeIncorrectRequest(20),
    ShopRes_RechargeUnknown(21),
    ShopRes_BuyNoToken(22), // 道具不够
    ShopRes_BuyNoStarCoin(23), // 星星币数量不足
    ShopRes_LimitLevel_Less(24),
    ShopRes_LimitLevel_More(25),
    ShopRes_CantBuyAnymore(26),
    ShopRes_FailedByBuyLimit(27),
    ShopRes_TradeBlocked(28),
    ShopRes_NpcRandomShopReset(29),
    ShopRes_BuyStockOver(30),
    ShopRes_DisabledNPC(31),
    ShopRes_TradeBlockedNotActiveAccount(32),
    ShopRes_TradeBlockedSnapShot(33),
    ShopRes_MarketTempBlock(34),
    ShopRes_UnableWorld(35),
    ShopRes_UnableShopVersion(36);
    private final int value;

    MapleShopResponse(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
