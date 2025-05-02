package Net.server;

import Config.constants.ItemConstants;
import Database.DatabaseLoader.DatabaseConnectionEx;
import tools.DateUtil;
import tools.LotteryRandom;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RafflePool {

    public enum RaffleType {
        黃金蘋果(5060048),
        魔法畫框(5060086),
        幸運的金色寶箱(2028394),
        幸運的銀色寶箱(2028393),
        傷害字型(5060049),
        萌獸卡牌包(5537000),
        艾比寶箱(5060032),
        潘朵拉寶箱(5060028),
        幸運寶箱(5060057),
        幸運紅包(2630018),
        魔法豎琴(5060025);

        private final int itemId;

        RaffleType(int id) {
            itemId = id;
        }

        public int getItemId() {
            return itemId;
        }
    }

    private static final List<Integer> allTypes = new LinkedList<>();
    private static final List<RaffleItem> allItems = new LinkedList<>();
    private static final Map<Integer, List<RaffleItem>> items = new LinkedHashMap<>();
    private static final Map<Integer, Integer> dates = new LinkedHashMap<>();
    private static final Map<Integer, Integer> periods = new LinkedHashMap<>();
    private static final Map<Integer, List<Integer>> allPeriods = new LinkedHashMap<>();
    private static final Map<Integer, Integer> durations = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Integer>> gainlogs = new LinkedHashMap<>();

    public static void checkPool() {
        loadAllTypes();
        for (int type : allTypes) {
            if (!dates.containsKey(type) || !periods.containsKey(type) || !durations.containsKey(type)) {
                continue;
            }
            long startTime = DateUtil.getStringToTime(dates.get(type) + "0000");
            long checkTime = DateUtil.getStringToTime(DateUtil.getPreDate("d", -durations.get(type)).replace("-", "") + "0000");
            if (startTime <= checkTime) {
                allPeriods.remove(type);
                loadAllPeriods();
                try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                    int nextPeriods = periods.get(type);
                    for (int i = 0; i <= Math.floor((checkTime - startTime) / (durations.get(type) * 86400000)); i++) {
                        if (allPeriods.containsKey(type)) {
                            for (int p : allPeriods.get(type)) {
                                if (p <= nextPeriods) {
                                    continue;
                                }
                                nextPeriods = p;
                                break;
                            }
                            if (nextPeriods == periods.get(type)) {
                                nextPeriods = 1;
                            }
                            periods.put(type, nextPeriods);
                        } else {
                            nextPeriods = 1;
                            break;
                        }
                    }
                    try (PreparedStatement ps = con.prepareStatement("UPDATE raffle_period SET period = ?, start_date = ? WHERE type = ?")) {
                        ps.setInt(1, nextPeriods);
                        ps.setInt(2, DateUtil.getTime() / 100);
                        ps.setInt(3, type);
                        ps.execute();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM raffle_log WHERE type = ?")) {
                        ps.setInt(1, type);
                        ps.execute();
                    }
                } catch (SQLException ex) {
                }
                allItems.clear();
                dates.remove(type);
                periods.remove(type);
                durations.remove(type);
                items.remove(type);
                gainlogs.remove(type);
                loadAllItems();
                loadPeriods();
                loadItems();
                loadLogs();
            }
        }
    }

    public static void reload() {
        allTypes.clear();
        loadAllTypes();
        allItems.clear();
        loadAllItems();
        allPeriods.clear();
        loadAllPeriods();
        dates.clear();
        periods.clear();
        durations.clear();
        loadPeriods();
        items.clear();
        loadItems();
        gainlogs.clear();
        loadLogs();
    }

    public static void loadAllTypes() {
        if (!allTypes.isEmpty()) {
            return;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            for (RaffleType rt : RaffleType.values()) {
                try (PreparedStatement ps = con.prepareStatement("UPDATE raffle_pool SET type = ? WHERE type = ?")) {
                    ps.setInt(1, rt.getItemId());
                    ps.setInt(2, rt.ordinal());
                    ps.execute();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE raffle_period SET type = ? WHERE type = ?")) {
                    ps.setInt(1, rt.getItemId());
                    ps.setInt(2, rt.ordinal());
                    ps.execute();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE raffle_log SET type = ? WHERE type = ?")) {
                    ps.setInt(1, rt.getItemId());
                    ps.setInt(2, rt.ordinal());
                    ps.execute();
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT type from raffle_pool GROUP BY type")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    allTypes.add(rs.getInt("type"));
                }
            }
        } catch (SQLException ex) {
        }
    }

    public static void loadAllItems() {
        if (!allItems.isEmpty()) {
            return;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM raffle_pool")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    allItems.add(new RaffleItem(
                            rs.getInt("id"),
                            rs.getInt("period"),
                            rs.getInt("itemId"),
                            rs.getInt("quantity"),
                            rs.getInt("chance"),
                            rs.getInt("smega") != 0,
                            rs.getInt("type"),
                            rs.getInt("allow") == 1)
                    );
                }
            }
        } catch (SQLException ex) {
        }
    }

    public static void loadItems() {
        loadAllTypes();
        for (int type : allTypes) {
            if (items.containsKey(type)) {
                continue;
            }
            for (RaffleItem ri : allItems) {
                if (!ri.isAllow() || ri.getType() != type) {
                    continue;
                }
                if (!periods.containsKey(type) || (ri.getPeriod() > 0 && periods.get(type) >= 0 && ri.getPeriod() != periods.get(type))) {
                    continue;
                }
                if (!items.containsKey(type)) {
                    items.put(type, new LinkedList<>());
                }
                if (!items.get(type).contains(ri)) {
                    items.get(type).add(ri);
                }
            }
        }
    }

    public static void loadPeriods() {
        loadAllTypes();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            for (int type : allTypes) {
                if (periods.containsKey(type)) {
                    continue;
                }
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM raffle_period WHERE type = ? LIMIT 1")) {
                    ps.setInt(1, type);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        periods.put(type, rs.getInt("period"));
                        durations.put(type, rs.getInt("duration"));
                        dates.put(type, rs.getInt("start_date"));
                    }
                }
                if (!periods.containsKey(type)) {
                    periods.put(type, 1);
                    durations.put(type, type == 5537000 ? 1 : 7);
                    dates.put(type, DateUtil.getTime() / 100);
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO raffle_period(type, period, duration, start_date) VALUES (?, ?, ?, ?)")) {
                        ps.setInt(1, type);
                        ps.setInt(2, periods.get(type));
                        ps.setInt(3, durations.get(type));
                        ps.setInt(4, dates.get(type));
                        ps.execute();
                    }
                }
            }
        } catch (SQLException ex) {
        }
    }

    public static void loadAllPeriods() {
        loadAllTypes();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            for (int type : allTypes) {
                if (allPeriods.containsKey(type)) {
                    continue;
                }
                try (PreparedStatement ps = con.prepareStatement("SELECT type, period FROM raffle_pool WHERE type = ? AND period >= 0 GROUP BY period")) {
                    ps.setInt(1, type);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        if (!allPeriods.containsKey(type)) {
                            allPeriods.put(type, new LinkedList<>());
                        }
                        if (!allPeriods.get(type).contains(rs.getInt("period"))) {
                            allPeriods.get(type).add(rs.getInt("period"));
                            Collections.sort(allPeriods.get(type));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
        }
    }

    public static void loadLogs() {
        loadAllTypes();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            for (int type : allTypes) {
                if (gainlogs.containsKey(type)) {
                    continue;
                }
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM raffle_log WHERE type = ? AND period = ?")) {
                    ps.setInt(1, type);
                    ps.setInt(2, periods.get(type));
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        if (!gainlogs.containsKey(type)) {
                            gainlogs.put(type, new LinkedHashMap<>());
                        }
                        gainlogs.get(type).put(rs.getInt("itemId"), rs.getInt("quantity"));
                    }
                }
            }
        } catch (SQLException ex) {
        }
    }

    public static List<RaffleItem> getAllItems() {
        loadAllItems();
        if (allItems.isEmpty()) {
            return new LinkedList<>();
        }
        return new LinkedList<>(allItems);
    }

    public static List<RaffleItem> getAllItems(int type) {
        if (!items.containsKey(type)) {
            loadAllItems();
            loadAllPeriods();
            loadPeriods();
            loadItems();
            loadLogs();
        }
        if (!items.containsKey(type) || items.get(type) == null) {
            return new LinkedList<>();
        }
        return items.get(type);
    }

    private static final int ALL_REWARD = 0;
    private static final int MAIN_REWARD = 1;
    private static final int MINOR_REWARD = 2;

    public static List<RaffleItem> getItems(int type) {
        return getItems(ALL_REWARD, type);
    }

    public static List<RaffleItem> getItems(int itemType, int type) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<RaffleItem> its;
        switch (itemType) {
            case MAIN_REWARD:
                its = getMainReward(type);
                break;
            case MINOR_REWARD:
                its = getMinorReward(type);
                break;
            default:
                its = getAllItems(type);
                break;
        }
        List<RaffleItem> itemList = new LinkedList<>(its);
        if (!its.isEmpty()) {
            for (RaffleItem ri : its) {
                if (!ii.itemExists(ri.getItemId()) && !ii.isSkinExist(ri.getItemId()) && !ii.isHairExist(ri.getItemId()) && !ii.isFaceExist(ri.getItemId())) {
                    itemList.remove(ri);
                }
                if (ri.getQuantity() >= 0) {
                    if (ri.getQuantity() == 0 || (gainlogs.containsKey(ri.getType()) && gainlogs.get(ri.getType()).containsKey(ri.getItemId()) && ri.getQuantity() <= gainlogs.get(ri.getType()).get(ri.getItemId()))) {
                        itemList.remove(ri);
                    }
                }
            }
        }
        return itemList;
    }

    public static RaffleItem randomItem(int type) {
        return randomItem(type, -1);
    }

    public static RaffleItem randomItem(int type, int nGender) {
        checkPool();
        List<RaffleItem> itemList = getItems(MAIN_REWARD, type);
        if (2028394 == type) { // 幸運的金色寶箱子
            itemList.addAll(getItems(MAIN_REWARD, 5060048)); // 黃金蘋果大獎
        }
        itemList.addAll(getItems(MINOR_REWARD, type));
        LotteryRandom lr = getRandomByGender(itemList, nGender);
        RaffleItem itR = null;
        if (lr.size() > 0) {
            itR = (RaffleItem) lr.random();
        }
        return itR != null && gainRaffle(itR) ? itR : null;
    }

    private static LotteryRandom getRandomByGender(List<RaffleItem> itemList, int nGender) {
        LotteryRandom random = new LotteryRandom();
        if (itemList.isEmpty()) {
            return random;
        }
        for (RaffleItem rf : itemList) {
            if (ItemConstants.類型.getGender(rf.getItemId()) == nGender || ItemConstants.類型.getGender(rf.getItemId()) >= 2 || nGender < 0) {
                random.addData(rf, rf.getChance());
            }
        }
        return random;
    }

    public static boolean gainRaffle(RaffleItem item) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!ii.itemExists(item.getItemId()) && !ii.isSkinExist(item.getItemId()) && !ii.isHairExist(item.getItemId()) && !ii.isFaceExist(item.getItemId())) {
            return false;
        }
        if (item.getQuantity() >= 0) {
            if (item.getQuantity() == 0) {
                return false;
            }
            int amount = !gainlogs.containsKey(item.getType()) || !gainlogs.get(item.getType()).containsKey(item.getItemId()) ? 0 : gainlogs.get(item.getType()).get(item.getItemId());
            if (item.getQuantity() <= amount) {
                return false;
            }
            amount++;
            if (!gainlogs.containsKey(item.getType())) {
                gainlogs.put(item.getType(), new LinkedHashMap<>());
            }
            gainlogs.get(item.getType()).put(item.getItemId(), amount);
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM raffle_log WHERE type = ? AND period = ? AND itemId = ?")) {
                    ps.setInt(1, item.getType());
                    ps.setInt(2, item.getPeriod());
                    ps.setInt(3, item.getItemId());
                    ps.execute();
                }
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO raffle_log(type, period, itemId, quantity) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, item.getType());
                    ps.setInt(2, item.getPeriod());
                    ps.setInt(3, item.getItemId());
                    ps.setInt(4, amount);
                    ps.execute();
                }
            } catch (SQLException ex) {
            }
        }
        return true;
    }

    public static List<RaffleItem> getMainReward(int type) {
        checkPool();
        List<RaffleItem> all = getAllItems(type);
        List<RaffleItem> mainRewards = new LinkedList<>();
        for (RaffleItem ri : all) {
            if (ri.getPeriod() > 0 && ri.isSmega()) {
                mainRewards.add(ri);
            }
        }
        return mainRewards;
    }

    public static List<RaffleItem> getMinorReward(int type) {
        checkPool();
        List<RaffleItem> all = getAllItems(type);
        List<RaffleItem> minorRewards = new LinkedList<>();
        for (RaffleItem ri : all) {
            if (ri.getPeriod() == 0 || !ri.isSmega()) {
                minorRewards.add(ri);
            }
        }
        return minorRewards;
    }

    public static long getNextPeriodDate(int type) {
        checkPool();
        if (!durations.containsKey(type) || !dates.containsKey(type)) {
            loadAllItems();
            loadAllPeriods();
            loadPeriods();
            loadItems();
            loadLogs();
        }
        if (!durations.containsKey(type) || !dates.containsKey(type)) {
            return DateUtil.getStringToTime(DateUtil.getPreDate("d", 1).replace("-", "") + "0000") - 60000;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(DateUtil.getStringToTime(dates.get(type) + "0000"));
        calendar.add(Calendar.DAY_OF_MONTH, durations.get(type));
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis() - 60000;
    }

    public static int getDate(int type) {
        if (!dates.containsKey(type)) {
            loadPeriods();
        }
        return dates.get(type);
    }

    public static int getPeriod(int type) {
        if (!periods.containsKey(type)) {
            loadPeriods();
        }
        return periods.get(type);
    }

    public static int getDuration(int type) {
        if (!durations.containsKey(type)) {
            loadPeriods();
        }
        return durations.get(type);
    }

    public static void setDate(int type, int value) {
        dates.put(type, value);
    }

    public static void setPeriod(int type, int value) {
        periods.put(type, value);
    }

    public static void setDuration(int type, int value) {
        durations.put(type, value);
    }

    public static List<Integer> getAllPeriod(int type) {
        if (!allPeriods.containsKey(type)) {
            loadAllPeriods();
        }
        return allPeriods.get(type);
    }

    public static List<Integer> getAllType() {
        loadAllTypes();
        return allTypes;
    }

    public static Map<Integer, Pair<List<Integer>, List<Integer>>> getRoyalCouponList() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Map<Integer, Pair<List<Integer>, List<Integer>>> couponList = new LinkedHashMap<>();
        for (int couponId : RafflePool.getAllType()) {
            if (couponId / 1000 != 5151 && couponId / 100 != 51521 && couponId / 100 != 51523 && (couponId >= 5150000 && couponId < 5153000 || couponId / 1000 == 5154)) {
                if (!couponList.containsKey(couponId)) {
                    couponList.put(couponId, new Pair(new LinkedList<>(), new LinkedList<>()));
                }
                for (RaffleItem ri : RafflePool.getAllItems(couponId)) {
                    if ((RafflePool.getPeriod(couponId) == ri.getPeriod() || ri.getPeriod() == 0) && (ii.isHairExist(ri.getItemId()) || ii.isFaceExist(ri.getItemId())) && ri.getItemId() < 1000000) {
                        int nStyleGenderCode = ItemConstants.類型.getGender(ri.getItemId());
                        if (nStyleGenderCode == 0 || nStyleGenderCode >= 2) {
                            couponList.get(couponId).getLeft().add(ri.getItemId());
                        }
                        if (nStyleGenderCode == 1 || nStyleGenderCode >= 2) {
                            couponList.get(couponId).getRight().add(ri.getItemId());
                        }
                    }
                }
            }
        }
        return couponList;
    }
}
