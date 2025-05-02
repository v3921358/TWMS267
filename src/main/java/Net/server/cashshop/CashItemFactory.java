package Net.server.cashshop;

import Database.DatabaseLoader.DatabaseConnectionEx;
import Net.server.cashshop.CashItemInfo.CashModInfo;
import Plugin.provider.*;
import Plugin.provider.loaders.CashData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CashItemFactory {

    private static final Logger log = LoggerFactory.getLogger(CashItemFactory.class);
    private final static int[] bestItems = new int[]{30200045, 50000080, 30200066, 50400016, 30100092};
    private static final CashItemFactory instance = new CashItemFactory();
    private final List<CashItemCategory> categories = new LinkedList<>();
    private final Map<Integer, CashModInfo> itemMods = new HashMap<>();
    private final Map<Integer, CashItemForSql> menuItems = new HashMap<>();
    private final Map<Integer, CashItemForSql> categoryItems = new HashMap<>();
    private Map<Integer, CashItemInfo> itemStats = new HashMap<>(); //商城道具狀態
    private Map<Integer, Integer> idLookup = new HashMap<>(); //商城道具的SN集合
    private final Map<Integer, CashItemInfo> oldItemStats = new HashMap<>(); //老版本的商城道具狀態
    private final Map<Integer, Integer> oldIdLookup = new HashMap<>(); //老版本的商城道具的SN集合
    private Map<Integer, List<Integer>> itemPackage = new HashMap<>(); //禮包信息
    private final Map<Integer, List<Pair<Integer, Integer>>> openBox = new HashMap<>(); //箱子道具物品
    private final MapleDataProvider data = MapleDataProviderFactory.getEtc();
    private final MapleData commodities = data.getData("Commodity.img");
    private List<Integer> blockRefundableItemId = new LinkedList<>(); //禁止使用回購的道具 也就是有些道具有多個SN信息 而每個SN下的價格又不一樣
    private final Map<Integer, Byte> baseNewItems = new HashMap<>();

    public static CashItemFactory getInstance() {
        return instance;
    }

    public void initialize() {
        blockRefundableItemId.clear();
        int onSaleSize = 0;
        Map<Integer, Integer> fixId = new HashMap<>(); //檢測WZ中是否有重複價格的道具 [SN] [itemId]
        //加載商城道具
        for (MapleData field : commodities.getChildren()) {
            int SN = MapleDataTool.getIntConvert("SN", field, 0);
            int itemId = MapleDataTool.getIntConvert("ItemId", field, 0);
            int count = MapleDataTool.getIntConvert("Count", field, 1);
            int price = MapleDataTool.getIntConvert("Price", field, 0);
            int meso = MapleDataTool.getIntConvert("Meso", field, 0);
            int originalPrice = MapleDataTool.getIntConvert("originalPrice", field, 0);
            int period = MapleDataTool.getIntConvert("Period", field, 0);
            int gender = MapleDataTool.getIntConvert("Gender", field, 2);
            byte csClass = (byte) MapleDataTool.getIntConvert("Class", field, 0);
            byte priority = (byte) MapleDataTool.getIntConvert("Priority", field, 0);
            int termStart = MapleDataTool.getIntConvert("termStart", field, 0);
            int termEnd = MapleDataTool.getIntConvert("termEnd", field, 0);
            boolean onSale = MapleDataTool.getIntConvert("OnSale", field, 0) > 0 || isOnSalePackage(SN); //道具是否出售
            boolean bonus = MapleDataTool.getIntConvert("Bonus", field, 0) > 0; //是否有獎金紅利？
            boolean refundable = MapleDataTool.getIntConvert("Refundable", field, 0) == 0; //道具是否可以回購
            boolean discount = MapleDataTool.getIntConvert("discount", field, 0) > 0; //是否打折出售
            int mileageRate = MapleDataTool.getIntConvert("mileageRate", field, 0); // 里程抵扣率
            boolean onlyMileage = MapleDataTool.getIntConvert("onlyMileage", field, 0) >= 0; //可全里程購買
            int LimitMax = MapleDataTool.getIntConvert("LimitMax", field, 0); // 限購數量
            if (onSale) {
                onSaleSize++;
            }
            CashItemInfo stats = new CashItemInfo(field.getName(), itemId, count, price, originalPrice, meso, SN, period, gender, csClass, priority, termStart, termEnd, onSale, bonus, refundable, discount, mileageRate, onlyMileage, LimitMax);
            if (SN > 0) {
                itemStats.put(SN, stats);
                if (idLookup.containsKey(itemId)) {
                    fixId.put(SN, itemId);
                    blockRefundableItemId.add(itemId);
                }
                idLookup.put(itemId, SN);
            }
        }
        //log.info("共加載 " + itemStats.size() + " 個商城道具 有 " + onSaleSize + " 個道具處於出售狀態..."); /* 暫時關閉 */
        //log.info("其中有 " + fixId.size() + " 重複價格的道具和 " + blockRefundableItemId.size() + " 個禁止換購的道具."); /* 暫時關閉 */
        //加載商城禮包的信息
        MapleData packageData = data.getData("CashPackage.img");
        for (MapleData root : packageData.getChildren()) {
            if (root.getChildByPath("SN") == null) {
                continue;
            }
            List<Integer> packageItems = new ArrayList<>();
            for (MapleData dat : root.getChildByPath("SN").getChildren()) {
                packageItems.add(MapleDataTool.getIntConvert(dat));
            }
            itemPackage.put(Integer.parseInt(root.getName()), packageItems);
        }
        //log.info("共加載 " + itemPackage.size() + " 個商城禮包..."); /* 暫時關閉 */
        //加載老的商城道具信息
        onSaleSize = 0;
        MapleDataDirectoryEntry root = data.getRoot();
        for (MapleDataEntry topData : root.getFiles()) {
            if (topData.getName().startsWith("OldCommodity")) {
                MapleData Commodity = data.getData(topData.getName());
                for (MapleData field : Commodity.getChildren()) {
                    int SN = MapleDataTool.getIntConvert("SN", field, 0);
                    int itemId = MapleDataTool.getIntConvert("ItemId", field, 0);
                    int count = MapleDataTool.getIntConvert("Count", field, 1);
                    int price = MapleDataTool.getIntConvert("Price", field, 0);
                    int meso = MapleDataTool.getIntConvert("Meso", field, 0);
                    int originalPrice = MapleDataTool.getIntConvert("originalPrice", field, 0);
                    int period = MapleDataTool.getIntConvert("Period", field, 0);
                    int gender = MapleDataTool.getIntConvert("Gender", field, 2);
                    byte csClass = (byte) MapleDataTool.getIntConvert("Class", field, 0);
                    byte priority = (byte) MapleDataTool.getIntConvert("Priority", field, 0);
                    int termStart = MapleDataTool.getIntConvert("termStart", field, 0);
                    int termEnd = MapleDataTool.getIntConvert("termEnd", field, 0);
                    boolean onSale = MapleDataTool.getIntConvert("OnSale", field, 0) > 0 || isOnSalePackage(SN); //道具是否出售
                    boolean bonus = MapleDataTool.getIntConvert("Bonus", field, 0) >= 0; //是否有獎金紅利？
                    boolean refundable = MapleDataTool.getIntConvert("Refundable", field, 0) == 0; //道具是否可以回購
                    boolean discount = MapleDataTool.getIntConvert("discount", field, 0) >= 0; //是否打折出售
                    int mileageRate = MapleDataTool.getIntConvert("mileageRate", field, 0); // 里程抵扣率
                    boolean onlyMileage = MapleDataTool.getIntConvert("onlyMileage", field, 0) >= 0; //可全里程購買
                    int LimitMax = MapleDataTool.getIntConvert("LimitMax", field, 0); // 限購數量
                    if (onSale) {
                        onSaleSize++;
                    }
                    CashItemInfo stats = new CashItemInfo(field.getName(), itemId, count, price, originalPrice, meso, SN, period, gender, csClass, priority, termStart, termEnd, onSale, bonus, refundable, discount, mileageRate, onlyMileage, LimitMax);
                    if (SN > 0) {
                        oldItemStats.put(SN, stats);
                        oldIdLookup.put(itemId, SN);
                    }
                }
            }
        }
        //log.info("共加載 " + oldItemStats.size() + " 個老的商城道具 有 " + onSaleSize + " 個道具處於出售狀態..."); /* 暫時關閉 */


        CashData.loadCashCommodities();
        itemStats = CashData.getItemStats();
        idLookup = CashData.getIdLookup();
        blockRefundableItemId = CashData.getBlockRefundableItemId();
        CashData.loadCashPackages();
        itemPackage = CashData.getItemPackage();
        CashData.loadCashOldCommodities();


        loadMoifiedItemInfo();
        loadRandomItemInfo();

        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashshop_categories"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CashItemCategory cat = new CashItemCategory(rs.getInt("categoryid"), rs.getString("name"), rs.getInt("parent"), rs.getInt("flag"), rs.getInt("sold"));
                    categories.add(cat);
                }
            } catch (SQLException e) {
                log.error("Failed to load cash shop categories. ", e);
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashshop_menuitems"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CashItemForSql item = new CashItemForSql(rs.getInt("category"), rs.getInt("subcategory"), rs.getInt("parent"), rs.getString("image"), rs.getInt("sn"), rs.getInt("itemid"), rs.getInt("flag"), rs.getInt("price"), rs.getInt("discountPrice"), rs.getInt("quantity"), rs.getInt("expire"), rs.getInt("gender"), rs.getInt("likes"));
                    menuItems.put(item.getSN(), item);
                }
            } catch (SQLException e) {
                log.error("Failed to load cash shop menuitems. ", e);
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashshop_items"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CashItemForSql item = new CashItemForSql(rs.getInt("category"), rs.getInt("subcategory"), rs.getInt("parent"), rs.getString("image"), rs.getInt("sn"), rs.getInt("itemid"), rs.getInt("flag"), rs.getInt("price"), rs.getInt("discountPrice"), rs.getInt("quantity"), rs.getInt("expire"), rs.getInt("gender"), rs.getInt("likes"));
                    categoryItems.put(item.getSN(), item);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to load cash shop items. ", e);
        }
    }

    public boolean isOnSalePackage(int snId) {
        return snId >= 170200002 && snId <= 170200013;
    }

    public void loadMoifiedItemInfo() {
        itemMods.clear();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashshop_modified_items"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CashModInfo ret = new CashModInfo(
                            rs.getInt("serial"),
                            rs.getInt("discount_price"),
                            rs.getInt("mark"),
                            rs.getInt("showup") > 0,
                            rs.getInt("itemid"),
                            rs.getInt("priority"),
                            rs.getInt("package") > 0,
                            rs.getInt("period"),
                            rs.getInt("gender"),
                            rs.getInt("count"),
                            rs.getInt("meso"),
                            rs.getInt("csClass"),
                            rs.getInt("termStart"),
                            rs.getInt("termEnd"),
                            rs.getInt("fameLimit"),
                            rs.getInt("levelLimit"),
                            rs.getInt("categories"),
                            rs.getByte("bast_new") > 0);
                    itemMods.put(ret.getSn(), ret);
                    final CashItemInfo cc = itemStats.get(ret.getSn());
                    if (cc != null) {
                        ret.toCItem(cc); //init
                        if (ret.isBase_new()) {
                            baseNewItems.put(ret.getSn(), cc.getCsClass());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("cashshop_modified_items_error: ", e);
        }
        //log.info("共加載 " + itemMods.size() + " 個商品"); /* 暫時關閉 */
    }

    public void loadRandomItemInfo() {
        openBox.clear();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashshop_randombox"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    List<Pair<Integer, Integer>> boxItems = openBox.computeIfAbsent(rs.getInt("randboxID"), integer -> new ArrayList<>());
                    boxItems.add(new Pair<>(rs.getInt("itemSN"), rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            log.error("加載商城隨機箱子的訊息出錯.", e);
        }

        //log.info("共加載 " + openBox.size() + " 個商城隨機箱子的訊息..."); /* 暫時關閉 */
    }

    public CashItemInfo getSimpleItem(int sn) {
        return itemStats.get(sn);
    }

    public Map<Integer, CashItemInfo> getAllItem() {
        return itemStats;
    }

    public boolean isBlockRefundableItemId(int itemId) {
        return blockRefundableItemId.contains(itemId);
    }

    public CashModInfo getModInfo(int sn) {
        return itemMods.get(sn);
    }

    public Map<Integer, CashModInfo> getAllModInfo() {
        return itemMods;
    }

    public Map<Integer, Byte> getAllBaseNewInfo() {
        return baseNewItems;
    }

    public CashItemInfo getItem(int sn) {
        return getItem(sn, true);
    }

    public CashItemInfo getItem(int sn, boolean checkSale) {
        CashItemInfo stats = itemStats.get(sn);
        CashModInfo z = getModInfo(sn);
        if (z != null && z.isShowUp()) {
            return z.toCItem(stats);
        }
        if (stats == null) {
            return null;
        }
        return checkSale && !stats.onSale() ? null : stats;
    }

    public CashItemForSql getMenuItem(int sn) {
        for (CashItemForSql ci : getMenuItems()) {
            if (ci.getSN() == sn) {
                return ci;
            }
        }
        return null;
    }

    public CashItemForSql getAllItem(int sn) {
        for (CashItemForSql ci : getAllItems()) {
            if (ci.getSN() == sn) {
                return ci;
            }
        }
        return null;
    }

    public List<Integer> getPackageItems(int itemId) {
        return itemPackage.get(itemId);
    }

    /*
     * 隨機箱子道具
     */
    public Map<Integer, List<Pair<Integer, Integer>>> getRandomItemInfo() {
        return openBox;
    }

    public boolean hasRandomItem(int itemId) {
        return openBox.containsKey(itemId);
    }

    public List<Pair<Integer, Integer>> getRandomItem(int itemId) {
        return openBox.get(itemId);
    }

    public int[] getBestItems() {
        return bestItems;
    }

    public int getLinkItemId(int itemId) {
        switch (itemId) {
            case 5000029: //寶貝龍
            case 5000030: //綠龍
            case 5000032: //藍龍
            case 5000033: //黑龍
            case 5000035: //紅龍
                return 5000028; //進化龍
            case 5000048: //娃娃機器人
            case 5000049: //機器人(藍色)
            case 5000050: //機器人(紅色)
            case 5000051: //機器人(綠色)
            case 5000052: //機器人(金色)
                return 5000047; //羅伯
        }
        return itemId;
    }

    public int getSnFromId(int itemId) {
        if (idLookup.containsKey(itemId)) {
            return idLookup.get(itemId);
        }
        return 0;
    }

    public List<CashItemCategory> getCategories() {
        return categories;
    }

    public List<CashItemForSql> getMenuItems(int type) {
        List<CashItemForSql> items = new LinkedList<>();
        for (CashItemForSql ci : menuItems.values()) {
            if (ci.getSubCategory() / 10000 == type) {
                items.add(ci);
            }
        }
        return items;
    }

    public List<CashItemForSql> getMenuItems() {
        List<CashItemForSql> items = new LinkedList<>();
        for (CashItemForSql ci : menuItems.values()) {
            items.add(ci);
        }
        return items;
    }

    public List<CashItemForSql> getAllItems(int type) {
        List<CashItemForSql> items = new LinkedList<>();
        for (CashItemForSql ci : categoryItems.values()) {
            if (ci.getSubCategory() / 10000 == type) {
                items.add(ci);
            }
        }
        return items;
    }

    public List<CashItemForSql> getAllItems() {
        List<CashItemForSql> items = new LinkedList<>();
        for (CashItemForSql ci : categoryItems.values()) {
            items.add(ci);
        }
        return items;
    }

    public List<CashItemForSql> getCategoryItems(int subcategory) {
        List<CashItemForSql> items = new LinkedList<>();
        for (CashItemForSql ci : categoryItems.values()) {
            if (ci.getSubCategory() == subcategory) {
                items.add(ci);
            }
        }
        return items;
    }
}
