package Net.server.shop;

import Client.inventory.Item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MapleShopItem {

    private int buyLimit; //允許購買次數
    private int buyLimitWorldAccount;
    private int itemId; //物品ID
    private long price; //物品價格
    private int tokenItemID; //自定義購買物品需要的道具ID
    private int tokenPrice; //購買需要的道具數量
    private int period; //該道具購買後的天數
    private int potentialGrade; //道具狀態 1=未鑒定
    private int category; //商店道具分類
    private int minLevel; //購買道具最小等級
    private int maxLevel;//購買道具最大等級
    private Item rebuy; //回購的道具信息
    private int position; //道具位置
    private int pointQuestID;
    private int pointPrice;
    private long sellStart;
    private long sellEnd;
    private long[] resetInfo;
    private short quantity;
    private short buyable;
    private byte resetType;

    private MapleShopItem() {
    }

    /**
     * 回購信息相關
     * 道具信息
     * 道具價格
     * 道具數量
     */
    public MapleShopItem(Item rebuy, long price, short buyable) {
        this.itemId = rebuy.getItemId();
        this.price = price;
        this.tokenItemID = 0;
        this.tokenPrice = 0;
        this.period = 0;
        this.potentialGrade = 0;
        this.category = 0;
        this.minLevel = 0;
        this.maxLevel = 0;
        this.position = -1;
        this.rebuy = rebuy;
        this.pointQuestID = 0;
        this.pointPrice = 0;
        this.sellStart = -2;
        this.sellEnd = -1;
        this.buyLimit = 0;
        this.buyLimitWorldAccount = 0;
        this.quantity = buyable;
        this.buyable = buyable;
    }

    /**
     * 加載商店信息相關
     */
    public MapleShopItem(int itemId, long price, int position, int tokenItemID, int tokenPrice, int pointQuestID, int pointPrice, int itemPeriod, int potentialGrade, int tabIndex, int minLevel, int maxLevel, long sellStart, long sellEnd, int buyLimit, int buyLimitWorldAccount, short buyable) {
        this.itemId = itemId;
        this.price = price;
        this.tokenItemID = tokenItemID;
        this.tokenPrice = tokenPrice;
        this.period = itemPeriod;
        this.potentialGrade = potentialGrade;
        this.category = tabIndex;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.position = position;
        this.rebuy = null;
        this.pointQuestID = pointQuestID;
        this.pointPrice = pointPrice;
        this.sellStart = sellStart;
        this.sellEnd = sellEnd;
        this.buyLimit = buyLimit;
        this.buyLimitWorldAccount = buyLimitWorldAccount;
        this.quantity = 1;
        this.buyable = buyable;
    }

    public int getBuyLimit() {
        return buyLimit;
    }

    public int getBuyLimitWorldAccount() {
        return buyLimitWorldAccount;
    }

    public int getItemId() {
        return itemId;
    }

    public long getPrice() {
        return price;
    }

    public int getTokenItemID() {
        return tokenItemID;
    }

    public int getTokenPrice() {
        return tokenPrice;
    }

    public int getCategory() {
        return category;
    }

    public int getPeriod() {
        return period;
    }

    public int getPotentialGrade() {
        return potentialGrade;
    }

    public Item getRebuy() {
        return rebuy;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getPosition() {
        return position;
    }

    public boolean isRechargeableItem() {
        int n = itemId / 10000;
        return n == 233 || n == 207;
    }

    public int getPointQuestID() {
        return pointQuestID;
    }

    public int getPointPrice() {
        return pointPrice;
    }

    public long getSellStart() {
        return sellStart;
    }

    public long getSellEnd() {
        return sellEnd;
    }

    public long[] getResetInfo() {
        return resetInfo;
    }

    public void setQuantity(short quantity) {
        if (quantity > 1) {
            this.quantity = quantity;
        } else {
            this.quantity = 1;
        }
    }

    public short getQuantity() {
        return quantity;
    }

    public short getBuyable() {
        return buyable;
    }

    public void setResetInfo(List<Long> resetInfo) {
        this.resetInfo = resetInfo.stream().mapToLong(Long::longValue).toArray();
    }

    public static MapleShopItem createFromSql(ResultSet rs, int pos) throws SQLException {
        MapleShopItem item = new MapleShopItem();
        item.itemId = rs.getInt("nItemID");
        item.price = rs.getLong("nPrice");
        item.tokenItemID = rs.getInt("nTokenItemID");
        item.tokenPrice = rs.getInt("nTokenPrice");
        item.pointQuestID = rs.getInt("nPointQuestID");
        item.pointPrice = rs.getInt("nPointPrice");
        item.period = rs.getInt("nItemPeriod");
        item.potentialGrade = rs.getInt("nPotentialGrade");
        item.category = rs.getInt("nTabIndex");
        item.minLevel = rs.getShort("nLevelLimitedMin");
        item.maxLevel = rs.getShort("nLevelLimitedMax");
        item.buyLimit = rs.getInt("nBuyLimit");
        item.buyLimitWorldAccount = rs.getInt("nBuyLimitWorldAccount");
        item.sellStart = rs.getLong("ftSellStart");
        item.sellEnd = rs.getLong("ftSellEnd");
        item.setQuantity(rs.getShort("nQuantity"));
        item.position = pos;
        return item;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public byte getResetType() {
        return resetType;
    }

    public void setResetType(byte resetType) {
        this.resetType = resetType;
    }
}
