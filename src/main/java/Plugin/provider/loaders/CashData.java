package Plugin.provider.loaders;

import Config.configs.Config;
import Config.constants.ServerConstants;
import Net.server.cashshop.CashItemInfo;
import Plugin.provider.*;
import SwordieX.util.Util;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class CashData {
    private static final Logger log = LoggerFactory.getLogger(CashData.class);
    private static MapleDataProvider data;
    private static MapleData commodities;
    @Getter
    private static Map<Integer, CashItemInfo> itemStats = new HashMap<>(); //商城道具狀態
    @Getter
    private static Map<Integer, Integer> idLookup = new HashMap<>(); //商城道具的SN集合
    @Getter
    private static Map<Integer, CashItemInfo> oldItemStats = new HashMap<>(); //老版本的商城道具狀態
    @Getter
    private static Map<Integer, Integer> oldIdLookup = new HashMap<>(); //老版本的商城道具的SN集合
    @Getter
    private static Map<Integer, List<Integer>> itemPackage = new HashMap<>(); //禮包信息
    @Getter
    private static List<Integer> blockRefundableItemId = new LinkedList<>(); //禁止使用回購的道具 也就是有些道具有多個SN信息 而每個SN下的價格又不一樣

    public static boolean isOnSalePackage(int snId) {
        return snId >= 170200002 && snId <= 170200013;
    }

    public static void saveCashCommodities(String dir) {
        Util.makeDirIfAbsent(dir);
        File file = new File(String.format("%s/CashCommodities.dat", dir));
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            return;
        }
        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file))) {
            dataOutputStream.writeInt(itemStats.size());
            for (Map.Entry<Integer, CashItemInfo> cis : itemStats.entrySet()) {
                dataOutputStream.writeInt(cis.getKey());
                dataOutputStream.writeUTF(cis.getValue().getName());
                dataOutputStream.writeInt(cis.getValue().getItemId());
                dataOutputStream.writeInt(cis.getValue().getCount());
                dataOutputStream.writeInt(cis.getValue().getPrice());
                dataOutputStream.writeInt(cis.getValue().getMeso());
                dataOutputStream.writeInt(cis.getValue().getOriginalPrice());
                dataOutputStream.writeInt(cis.getValue().getPeriod());
                dataOutputStream.writeInt(cis.getValue().getGender());
                dataOutputStream.writeInt(cis.getValue().getTermStart());
                dataOutputStream.writeInt(cis.getValue().getTermEnd());
                dataOutputStream.writeInt(cis.getValue().getMileageRate());
                dataOutputStream.writeInt(cis.getValue().getLimitMax());
                dataOutputStream.write(cis.getValue().getCsClass());
                dataOutputStream.write(cis.getValue().getPriority());
                dataOutputStream.writeBoolean(cis.getValue().onSale());
                dataOutputStream.writeBoolean(cis.getValue().isBonus());
                dataOutputStream.writeBoolean(cis.getValue().isRefundable());
                dataOutputStream.writeBoolean(cis.getValue().isDiscount());
                dataOutputStream.writeBoolean(cis.getValue().isOnlyMileage());
            }
            dataOutputStream.writeInt(idLookup.size());
            for (Map.Entry<Integer, Integer> il : idLookup.entrySet()) {
                dataOutputStream.writeInt(il.getKey());
                dataOutputStream.writeInt(il.getValue());
            }
            dataOutputStream.writeInt(blockRefundableItemId.size());
            for (Integer dat : blockRefundableItemId) {
                dataOutputStream.writeInt(dat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadCashCommodities(File file) {
        if (!file.exists()) {
            loadDatFromWz();
            return;
        }
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file))) {
            // 讀取Cash Commodities總數
            int maxcount = dataInputStream.readInt();
            for (int i = 0; i < maxcount; i++) {
                int SN = dataInputStream.readInt();
                String wzName = dataInputStream.readUTF();
                int itemId = dataInputStream.readInt();
                int count = dataInputStream.readInt();
                int price = dataInputStream.readInt();
                int meso = dataInputStream.readInt();
                int originalPrice = dataInputStream.readInt();
                int period = dataInputStream.readInt();
                int gender = dataInputStream.readInt();
                int termStart = dataInputStream.readInt();
                int termEnd = dataInputStream.readInt();
                int mileageRate = dataInputStream.readInt();
                int LimitMax = dataInputStream.readInt();
                byte csClass = dataInputStream.readByte();
                byte priority = dataInputStream.readByte();
                boolean onSale = dataInputStream.readBoolean();
                boolean bonus = dataInputStream.readBoolean();
                boolean refundable = dataInputStream.readBoolean();
                boolean discount = dataInputStream.readBoolean();
                boolean onlyMileage = dataInputStream.readBoolean();
                CashItemInfo cii = new CashItemInfo(wzName, itemId, count, price, originalPrice, meso, SN, period, gender, csClass, priority, termStart, termEnd, onSale, bonus, refundable, discount, mileageRate, onlyMileage, LimitMax);
                itemStats.put(SN, cii);
            }
            // 讀取IdLookup總數
            maxcount = dataInputStream.readInt();
            for (int i = 0; i < maxcount; i++) {
                idLookup.put(dataInputStream.readInt(), dataInputStream.readInt());
            }
            // 讀取BlockRefundableItemId總數
            maxcount = dataInputStream.readInt();
            for (int i = 0; i < maxcount; i++) {
                blockRefundableItemId.add(dataInputStream.readInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadCashCommodities() {
        long start = System.currentTimeMillis();
        String dir = ServerConstants.DAT_DIR + "/cash/CashCommodities.dat";
        File f = new File(dir);
        loadCashCommodities(f);
        //System.out.println(String.format("Loaded %s ItemStats from data files in %dms.", getSkills().size(), System.currentTimeMillis() - start));
    }

    private static void loadCashCommoditiesFromWz() {
        blockRefundableItemId.clear();
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
    }

    public static void saveCashPackages(String dir) {
        Util.makeDirIfAbsent(dir);
        File file = new File(String.format("%s/CashPackages.dat", dir));
        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file))) {
            dataOutputStream.writeInt(itemPackage.size());
            for (Map.Entry<Integer, List<Integer>> ip : itemPackage.entrySet()) {
                dataOutputStream.writeInt(ip.getKey());
                dataOutputStream.writeInt(ip.getValue().size());
                for (Integer dat : ip.getValue()) {
                    dataOutputStream.writeInt(dat);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void loadCashPackages(File file) {
        if (!file.exists()) {
            loadDatFromWz();
            return;
        }
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file))) {
            // 讀取Cash Packages總數
            int maxcount = dataInputStream.readInt();
            for (int i = 0; i < maxcount; i++) {
                int key = dataInputStream.readInt();
                int itemCount = dataInputStream.readInt();
                List<Integer> packageItems = new ArrayList<>();
                for (int j = 0; j < itemCount; j++) {
                    packageItems.add(dataInputStream.readInt());
                }
                itemPackage.put(key, packageItems);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadCashPackages() {
        long start = System.currentTimeMillis();
        String dir = ServerConstants.DAT_DIR + "/cash/CashPackages.dat";
        File f = new File(dir);
        loadCashPackages(f);
        //System.out.println(String.format("Loaded %s ItemStats from data files in %dms.", getSkills().size(), System.currentTimeMillis() - start));
    }

    private static void loadCashPackagesFromWz() {
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
    }

    public static void saveCashOldCommodities(String dir) {
        Util.makeDirIfAbsent(dir);
        File file = new File(String.format("%s/CashOldCommodities.dat", dir));
        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file))) {
            dataOutputStream.writeInt(oldItemStats.size());
            for (Map.Entry<Integer, CashItemInfo> cis : oldItemStats.entrySet()) {
                dataOutputStream.writeInt(cis.getKey());
                dataOutputStream.writeUTF(cis.getValue().getName());
                dataOutputStream.writeInt(cis.getValue().getItemId());
                dataOutputStream.writeInt(cis.getValue().getCount());
                dataOutputStream.writeInt(cis.getValue().getPrice());
                dataOutputStream.writeInt(cis.getValue().getMeso());
                dataOutputStream.writeInt(cis.getValue().getOriginalPrice());
                dataOutputStream.writeInt(cis.getValue().getPeriod());
                dataOutputStream.writeInt(cis.getValue().getGender());
                dataOutputStream.writeInt(cis.getValue().getTermStart());
                dataOutputStream.writeInt(cis.getValue().getTermEnd());
                dataOutputStream.writeInt(cis.getValue().getMileageRate());
                dataOutputStream.writeInt(cis.getValue().getLimitMax());
                dataOutputStream.write(cis.getValue().getCsClass());
                dataOutputStream.write(cis.getValue().getPriority());
                dataOutputStream.writeBoolean(cis.getValue().onSale());
                dataOutputStream.writeBoolean(cis.getValue().isBonus());
                dataOutputStream.writeBoolean(cis.getValue().isRefundable());
                dataOutputStream.writeBoolean(cis.getValue().isDiscount());
                dataOutputStream.writeBoolean(cis.getValue().isOnlyMileage());
            }
            dataOutputStream.writeInt(oldIdLookup.size());
            for (Map.Entry<Integer, Integer> il : oldIdLookup.entrySet()) {
                dataOutputStream.writeInt(il.getKey());
                dataOutputStream.writeInt(il.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadCashOldCommodities(File file) {
        if (!file.exists()) {
            loadDatFromWz();
            return;
        }
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file))) {
            // 讀取Cash Old Commodities總數
            int maxcount = dataInputStream.readInt();
            for (int i = 0; i < maxcount; i++) {
                int SN = dataInputStream.readInt();
                String wzName = dataInputStream.readUTF();
                int itemId = dataInputStream.readInt();
                int count = dataInputStream.readInt();
                int price = dataInputStream.readInt();
                int meso = dataInputStream.readInt();
                int originalPrice = dataInputStream.readInt();
                int period = dataInputStream.readInt();
                int gender = dataInputStream.readInt();
                int termStart = dataInputStream.readInt();
                int termEnd = dataInputStream.readInt();
                int mileageRate = dataInputStream.readInt();
                int LimitMax = dataInputStream.readInt();
                byte csClass = dataInputStream.readByte();
                byte priority = dataInputStream.readByte();
                boolean onSale = dataInputStream.readBoolean();
                boolean bonus = dataInputStream.readBoolean();
                boolean refundable = dataInputStream.readBoolean();
                boolean discount = dataInputStream.readBoolean();
                boolean onlyMileage = dataInputStream.readBoolean();
                CashItemInfo cii = new CashItemInfo(wzName, itemId, count, price, originalPrice, meso, SN, period, gender, csClass, priority, termStart, termEnd, onSale, bonus, refundable, discount, mileageRate, onlyMileage, LimitMax);
                oldItemStats.put(SN, cii);
            }
            // 讀取OldIdLookup總數
            maxcount = dataInputStream.readInt();
            for (int i = 0; i < maxcount; i++) {
                oldIdLookup.put(dataInputStream.readInt(), dataInputStream.readInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadCashOldCommodities() {
        long start = System.currentTimeMillis();
        String dir = ServerConstants.DAT_DIR + "/cash/CashOldCommodities.dat";
        File f = new File(dir);
        loadCashOldCommodities(f);
        //System.out.println(String.format("Loaded %s ItemStats from data files in %dms.", getSkills().size(), System.currentTimeMillis() - start));
    }

    private static void loadCashOldCommoditiesFromWz() {
        //加載老的商城道具信息
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
                    CashItemInfo stats = new CashItemInfo(field.getName(), itemId, count, price, originalPrice, meso, SN, period, gender, csClass, priority, termStart, termEnd, onSale, bonus, refundable, discount, mileageRate, onlyMileage, LimitMax);
                    if (SN > 0) {
                        oldItemStats.put(SN, stats);
                        oldIdLookup.put(itemId, SN);
                    }
                }
            }
        }
    }

    public static void generateDatFiles() {
        System.out.println("Started generating Cash Commodities data.");
        long start = System.currentTimeMillis();
        loadCashCommoditiesFromWz();
        saveCashCommodities(ServerConstants.DAT_DIR + "/cash");
        System.out.println(String.format("Completed generating Cash Commodities data in %dms.", System.currentTimeMillis() - start));

        System.out.println("Started generating Cash Packages data.");
        start = System.currentTimeMillis();
        loadCashPackagesFromWz();
        saveCashPackages(ServerConstants.DAT_DIR + "/cash");
        System.out.println(String.format("Completed generating Cash Packages data in %dms.", System.currentTimeMillis() - start));

        System.out.println("Started generating Cash Old Commodity data.");
        start = System.currentTimeMillis();
        loadCashOldCommoditiesFromWz();
        saveCashOldCommodities(ServerConstants.DAT_DIR + "/cash");
        System.out.println(String.format("Completed generating Cash Old Commodity data in %dms.", System.currentTimeMillis() - start));

        System.out.println("Started loading Cash common data.");
        start = System.currentTimeMillis();
        clear();
        System.out.println(String.format("Completed loaded Cash common data in %dms.", System.currentTimeMillis() - start));
    }

    public static void main(String[] args) {
        Config.load();
        MapleDataProviderFactory.init();
        loadDatFromWz();
    }

    public static void loadDatFromWz() {
        data = MapleDataProviderFactory.getEtc();
        commodities = data.getData("Commodity.img");
        generateDatFiles();
    }

    public static void clear() {
//        getSkills().clear();
        loadCashCommodities();
        loadCashPackages();
        loadCashOldCommodities();
    }
}
