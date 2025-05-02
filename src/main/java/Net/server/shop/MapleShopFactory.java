package Net.server.shop;

import Config.constants.ItemConstants;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Database.tools.SqlTool;
import Net.server.MapleItemInformationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MapleShopFactory {

    private static final Logger log = LoggerFactory.getLogger(MapleShopFactory.class);
    private static final Set<Integer> rechargeableItems = new LinkedHashSet<>();
    private static final Set<Integer> blockedItems = new LinkedHashSet<>();
    private static final MapleShopFactory instance = new MapleShopFactory();

    static {
        // 飛鏢
        rechargeableItems.add(2070000);// 海星鏢
        rechargeableItems.add(2070001);// 迴旋鏢
        rechargeableItems.add(2070002);// 黑色利刃
        rechargeableItems.add(2070003);// 雪花鏢
        rechargeableItems.add(2070004);// 梅之鏢
        rechargeableItems.add(2070005);// 雷之鏢
        rechargeableItems.add(2070006);// 日之鏢
        rechargeableItems.add(2070007);// 月牙鏢
        rechargeableItems.add(2070008);// 雪球
        rechargeableItems.add(2070009);// 木製陀螺
        rechargeableItems.add(2070010);// 冰柱
        rechargeableItems.add(2070011);// 楓葉飛鏢
        rechargeableItems.add(2070012);// 紙飛機
        rechargeableItems.add(2070013);// 橘子
        rechargeableItems.add(2070015);// 新手盜賊的飛鏢
        rechargeableItems.add(2070016); //雪球
        rechargeableItems.add(2070019);// 手裡劍-魔
        rechargeableItems.add(2070020);// 鞭炮
        rechargeableItems.add(2070021); //蛋糕標槍
        rechargeableItems.add(2070022);// 閃亮的紙條
        rechargeableItems.add(2070023);// 火牢術飛鏢
        rechargeableItems.add(2070024);// 無限的增加鏢
        rechargeableItems.add(2070026);// 白金飛鏢
        // 子彈
        rechargeableItems.add(2330000);// 彈丸
        rechargeableItems.add(2330001);// 子彈
        rechargeableItems.add(2330002);// 強力子彈
        rechargeableItems.add(2330003);// 高等子彈
        rechargeableItems.add(2330004);// 高爆彈
        rechargeableItems.add(2330005);// 穿甲彈
        rechargeableItems.add(2330006);// 新手海盜的子彈
        rechargeableItems.add(2330007);// 貫穿裝甲的特製子彈
        rechargeableItems.add(2330008);// 巨人彈丸
        rechargeableItems.add(2330016);// 白金彈丸

        blockedItems.add(4170023); //黃金花生
        blockedItems.add(4170024); //冰方塊
        blockedItems.add(4170025); //英雄鑰匙
        blockedItems.add(4170028); //正義之錘
        blockedItems.add(4170029); //神秘茶包
        blockedItems.add(4170031); //美人魚鏡子
        blockedItems.add(4170032); //祖母綠鏡子
        blockedItems.add(4170033); //史皮耐爾鏡子
    }

    private final Map<Integer, MapleShop> shops = new TreeMap<>();
    private final Map<Integer, Integer> shopIDs = new TreeMap<>();

    public static MapleShopFactory getInstance() {
        return instance;
    }

    public void loadShopData() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM shops")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        shopIDs.put(rs.getInt("shopid"), rs.getInt("npcid"));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("加載NPC商店錯誤", e);
        }

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (Map.Entry<Integer, Integer> entry : shopIDs.entrySet()) {
            // 添加商店數據
            MapleShop shop = new MapleShop(entry.getKey(), entry.getValue());
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `shopitems` WHERE `shops_id` = ? ORDER BY `position` ASC")) {
                    ps.setInt(1, entry.getKey());
                    try (ResultSet rs = ps.executeQuery()) {
                        int pos = 0;
                        List<Integer> recharges = new ArrayList<>();
                        for (int nItemID : rechargeableItems) {
                            if (ii.itemExists(nItemID)) {
                                recharges.add(nItemID);
                            }
                        }
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            int itemId = rs.getInt("nItemID");
                            if (!ii.itemExists(itemId) || blockedItems.contains(itemId)) {
                                continue;
                            }
                            MapleShopItem item = MapleShopItem.createFromSql(rs, pos++);
                            if (item.getBuyLimit() > 0 || item.getBuyLimitWorldAccount() > 0) {
                                byte resetType = rs.getByte("resetType");
                                if (resetType < -7) {
                                    resetType = (byte) 0;
                                } else if (resetType < 0 && resetType >= -7) {
                                    int dayOfWeek = Math.abs(resetType);
                                    if (dayOfWeek == 7) {
                                        dayOfWeek = 1;
                                    } else {
                                        dayOfWeek++;
                                    }
                                    resetType = (byte) 3;
                                    List<Long> resetInfo = new LinkedList<>();
                                    Calendar cal = Calendar.getInstance();
                                    cal.add(Calendar.DATE, -7);
                                    cal.set(Calendar.HOUR_OF_DAY, 0);
                                    cal.set(Calendar.MINUTE, 0);
                                    cal.set(Calendar.SECOND, 0);
                                    cal.set(Calendar.MILLISECOND, 0);
                                    for (int i = 0; i < 5; i++) {
                                        if (i > 0) {
                                            cal.add(Calendar.DATE, 7);
                                        }
                                        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                                        resetInfo.add(cal.getTime().getTime());
                                    }
                                    item.setResetInfo(resetInfo);
                                } else if (resetType == 3) {
                                    item.setResetInfo(SqlTool.queryAndGetList(con, "SELECT * FROM `shopitems_resetinfo` WHERE `shopitems_id` = ?", rs2 -> rs2.getLong("resettime"), id));
                                }
                                item.setResetType(resetType);
                            }
                            shop.addItem(item);
                            if (ItemConstants.類型.可充值道具(itemId) && rechargeableItems.contains(item.getItemId())) {
                                recharges.remove((Integer) item.getItemId());
                            }
                        }
                        for (Integer recharge : recharges) {
                            shop.addItem(new MapleShopItem(recharge, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -2, -1, 0, 0, (short) 0));
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("添加商店數據錯誤", e);
            }
            shops.put(entry.getKey(), shop);
        }
        log.trace("商店數據加載完成.");
    }

    public void clear() {
        shopIDs.clear();
        shops.clear();
        loadShopData();
    }

    public MapleShop getShop(int shopId) {
        return shops.get(shopId);
    }

    public void setShopData(String shopid, MapleShop shop) {
        if (shopid == null) {
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO shops VALUES (?, ?, ?)")) {
                    ps.setInt(1, shop.getId());
                    ps.setInt(2, shop.getNpcId());
                    ps.setString(3, "NPC");
                    ps.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                int shopId = Integer.parseInt(shopid);
                SqlTool.update(con, "DELETE FROM `shops` WHERE `shopid` = ?", shopId);
                SqlTool.update(con, "DELETE FROM `shopitems` WHERE `shops_id` = ?", shopId);
                SqlTool.update(con, "INSERT INTO `shops` VALUES (?, ?, ?)", shopId, shop.getNpcId(), "NPC");
                for (MapleShopItem item : shop.getItems()) {
                    SqlTool.update(con, "INSERT INTO `shopitems` (`nItemID`, `nPrice`, `nQuantity`, `position`, `nTokenItemID`, `nTokenPrice`, `nPointQuestID`, `nPointPrice`, `nItemPeriod`, `nPotentialGrade`, `nTabIndex`, `nLevelLimitedMin`, `nLevelLimitedMax`, `ftSellStart`, `ftSellEnd`, `nBuyLimit`, `nBuyLimitWorldAccount`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", item.getItemId(),
                            item.getPrice(),
                            item.getQuantity(),
                            item.getPosition(),
                            item.getTokenItemID(),
                            item.getTokenPrice(),
                            item.getPointQuestID(),
                            item.getPointPrice(),
                            item.getPeriod(),
                            item.getPotentialGrade(),
                            item.getCategory(),
                            item.getMinLevel(),
                            item.getMaxLevel(),
                            item.getSellStart(),
                            item.getSellEnd(),
                            item.getBuyLimit(),
                            item.getBuyLimitWorldAccount());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public MapleShop getShopForNPC(int npcId) {
        if (!shopIDs.containsValue(npcId)) {
            return null;
        }
        int shopId = 0;
        for (Map.Entry<Integer, Integer> entry : shopIDs.entrySet()) {
            if (npcId == entry.getValue()) {
                shopId = entry.getKey();
            }
        }
        return shops.get(shopId);
    }

    public Map<Integer, MapleShop> getAllShop() {
        return shops;
    }
}
