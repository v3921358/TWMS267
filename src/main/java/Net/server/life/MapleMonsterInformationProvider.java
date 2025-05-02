package Net.server.life;

import Client.inventory.MapleInventoryType;
import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Net.server.MapleItemInformationProvider;
import Net.server.reward.RewardDropEntry;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProvider;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.loaders.StringData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MapleMonsterInformationProvider {

    private static final Logger log = LoggerFactory.getLogger(MapleMonsterInformationProvider.class);
    private static final MapleMonsterInformationProvider instance = new MapleMonsterInformationProvider();
    private static final MapleDataProvider stringDataWZ = MapleDataProviderFactory.getString();
    private static final MapleData mobStringData = stringDataWZ.getData("MonsterBook.img");
    private final Map<Integer, List<MonsterDropEntry>> drops = new TreeMap<>();
    private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList<>();
    private final Map<Integer, Map<Integer, List<RewardDropEntry>>> specialdrops = new HashMap<>();

    public static MapleMonsterInformationProvider getInstance() {
        return instance;
    }

    public Map<Integer, List<MonsterDropEntry>> getAllDrop() {
        return drops;
    }

    public List<MonsterGlobalDropEntry> getGlobalDrop() {
        return globaldrops;
    }

    public Map<Integer, Map<Integer, List<RewardDropEntry>>> getFishDrop() {
        return specialdrops;
    }

    public void setDropData(int mobid, List<MonsterDropEntry> dropEntries) {
        setDropData(String.valueOf(mobid), dropEntries);
    }

    public void setDropData(String mobid, List<MonsterDropEntry> dropEntries) {
        update(Integer.parseInt(mobid), dropEntries);
        List<MonsterDropEntry> dropInfo = drops.computeIfAbsent(Integer.parseInt(mobid), k -> new LinkedList<>());
        dropInfo.clear();
        try {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data WHERE dropperid = ?")) {
                    ps.setInt(1, Integer.parseInt(mobid));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            int itemId = rs.getInt("itemid");
                            int chance = rs.getInt("chance");
                            List<MonsterDropEntry> dropList;
                            if (itemId > 0 && !ii.itemExists(itemId)) {
                                continue;
                            }

                            MonsterDropEntry dropEntry = new MonsterDropEntry(
                                    id,
                                    itemId,
                                    chance,
                                    rs.getInt("minimum_quantity"),
                                    rs.getInt("maximum_quantity"),
                                    rs.getInt("questid"),
                                    rs.getBoolean("onlySelf"),
                                    rs.getInt("period"),
                                    "數據庫");
                            String[] split = rs.getString("channel").replace("[", "").replace("]", "").replace(" ", "").split(",");
                            Set<Integer> collect = Arrays.stream(split).filter(it -> !it.isEmpty()).map(Integer::valueOf).collect(Collectors.toSet());
                            dropEntry.channels.addAll(collect);
                            dropInfo.add(dropEntry);
                        }
                    }
                }
            }
            boolean hasMeso = false;
            for (MonsterDropEntry mde : dropInfo) {
                if (mde.itemId == 0) {
                    hasMeso = true;
                    break;
                }
            }
            if (!hasMeso) {
                final MapleMonsterStats mons = MapleLifeFactory.getMonsterStats(Integer.parseInt(mobid));
                if (mons != null) {
                    addMeso(mons, dropInfo);
                }
            }
        } catch (SQLException e) {
            log.error("導入怪物爆率錯誤", e);
        }
    }

    public void setGlobalDropData(String id, MonsterGlobalDropEntry monsterGlobalDropEntry) {
        updateGlobal(Integer.parseInt(id), monsterGlobalDropEntry);
        globaldrops.clear();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data_global")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int itemId = rs.getInt("itemid");
                        if (itemId > 0 && !ii.itemExists(itemId)) {
                            continue;
                        }
                        MonsterGlobalDropEntry e = new MonsterGlobalDropEntry(
                                rs.getInt("id"),
                                rs.getInt("itemid"),
                                rs.getInt("chance"),
                                rs.getInt("continent"),
                                rs.getByte("dropType"),
                                rs.getInt("minimum_quantity"),
                                rs.getInt("maximum_quantity"),
                                rs.getInt("questid"),
                                rs.getInt("min_mob_level"),
                                rs.getInt("max_mob_level"),
                                rs.getBoolean("onlySelf"),
                                rs.getInt("period"),
                                "數據庫"
                        );
                        String[] split = rs.getString("channel").replace("[", "").replace("]", "").replace(" ", "").split(",");
                        Set<Integer> collect = Arrays.stream(split).filter(it -> !it.isEmpty()).map(Integer::valueOf).collect(Collectors.toSet());
                        e.channels.addAll(collect);
                        globaldrops.add(e);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("導入怪物爆率錯誤", e);
        }
    }

    public void removeDropData(int mobid) {
        drops.remove(mobid);
        update(mobid, null);
    }

    public void update(int mobid, List<MonsterDropEntry> dropEntries) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM drop_data WHERE dropperid = ?")) {
                ps.setInt(1, mobid);
                ps.execute();
            }
        } catch (SQLException ignored) {
        }

        if (dropEntries != null) {
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO drop_data VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    dropEntries.forEach(monsterDropEntry -> {
                        try {
                            ps.setInt(1, mobid);
                            ps.setInt(2, monsterDropEntry.itemId);
                            ps.setInt(3, monsterDropEntry.minimum);
                            ps.setInt(4, monsterDropEntry.maximum);
                            ps.setInt(5, monsterDropEntry.questid);
                            ps.setInt(6, monsterDropEntry.chance);
                            String itemName = MapleItemInformationProvider.getInstance().getName(monsterDropEntry.itemId);
                            switch (monsterDropEntry.itemId) {
                                case 0:
                                    itemName = "楓幣";
                                    break;
                                case -1:
                                    itemName = "樂豆點";
                                    break;
                                case -2:
                                    itemName = "楓葉點數";
                                    break;
                                case -3:
                                    itemName = "里程";
                                    break;
                            }
                            ps.setString(7, itemName);
                            ps.setString(8, monsterDropEntry.channels.toString());
                            ps.setBoolean(9, monsterDropEntry.onlySelf);
                            ps.setInt(10, monsterDropEntry.period);
                            ps.execute();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public void updateGlobal(int id, MonsterGlobalDropEntry monsterGlobalDropEntry) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM drop_data_global WHERE id = ?")) {
                ps.setInt(1, id);
                ps.execute();
            }
        } catch (SQLException ignored) {
        }

        if (monsterGlobalDropEntry != null) {
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO drop_data_global VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    try {
                        ps.setInt(1, monsterGlobalDropEntry.continent);
                        ps.setInt(2, monsterGlobalDropEntry.dropType);
                        ps.setInt(3, monsterGlobalDropEntry.itemId);
                        ps.setInt(4, monsterGlobalDropEntry.Minimum);
                        ps.setInt(5, monsterGlobalDropEntry.Maximum);
                        ps.setInt(6, monsterGlobalDropEntry.questid);
                        ps.setInt(7, monsterGlobalDropEntry.chance);
                        String itemName = MapleItemInformationProvider.getInstance().getName(monsterGlobalDropEntry.itemId);
                        switch (monsterGlobalDropEntry.itemId) {
                            case 0:
                                itemName = "楓幣";
                                break;
                            case -1:
                                itemName = "樂豆點";
                                break;
                            case -2:
                                itemName = "楓葉點數";
                                break;
                            case -3:
                                itemName = "里程";
                                break;
                        }
                        ps.setString(8, itemName);
                        ps.setString(9, monsterGlobalDropEntry.channels.toString());
                        ps.setInt(10, monsterGlobalDropEntry.minMobLevel);
                        ps.setInt(11, monsterGlobalDropEntry.maxMobLevel);
                        ps.setBoolean(12, monsterGlobalDropEntry.onlySelf);
                        ps.setInt(13, monsterGlobalDropEntry.period);
                        ps.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }
            } catch (SQLException ignored) {
            }
        }
    }

    public void load() {
        try {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            Map<Integer, List<MonsterDropEntry>> tmpDropInfo = new HashMap<>();
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            int dropperId = rs.getInt("dropperid");
                            int itemId = rs.getInt("itemid");
                            int chance = rs.getInt("chance");
                            List<MonsterDropEntry> dropList;
                            if (itemId > 0 && !ii.itemExists(itemId)) {
                                continue;
                            }
                            //MapleMonsterStats mons = MapleLifeFactory.getMonsterStats(dropperId);
                            if (tmpDropInfo.containsKey(dropperId)) {
                                dropList = tmpDropInfo.get(dropperId);
                            } else {
                                dropList = new ArrayList();
                            }

                            MonsterDropEntry dropEntry = new MonsterDropEntry(
                                    id,
                                    itemId,
                                    chance,
                                    rs.getInt("minimum_quantity"),
                                    rs.getInt("maximum_quantity"),
                                    rs.getInt("questid"),
                                    rs.getBoolean("onlySelf"),
                                    rs.getInt("period"),
                                    "數據庫");
                            String[] split = rs.getString("channel").replace("[", "").replace("]", "").replace(" ", "").split(",");
                            Set<Integer> collect = Arrays.stream(split).filter(it -> !it.isEmpty()).map(Integer::valueOf).collect(Collectors.toSet());
                            dropEntry.channels.addAll(collect);
                            dropList.add(dropEntry);
                            tmpDropInfo.put(dropperId, dropList);
                        }
                    }
                }
            }
            boolean hasMeso;
            for (Entry<Integer, List<MonsterDropEntry>> entry : tmpDropInfo.entrySet()) {
                hasMeso = false;
                for (MonsterDropEntry dropEntry : entry.getValue()) {
                    if (dropEntry.itemId == 0) {
                        hasMeso = true;
                        break;
                    }
                }
                if (!hasMeso) {
                    final MapleMonsterStats mons = MapleLifeFactory.getMonsterStats(entry.getKey());
                    if (mons != null) {
                        addMeso(mons, entry.getValue());
                    }
                }
            }
            drops.putAll(tmpDropInfo);

            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data_global")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int itemId = rs.getInt("itemid");
                            if (itemId > 0 && !ii.itemExists(itemId)) {
                                continue;
                            }
                            MonsterGlobalDropEntry e = new MonsterGlobalDropEntry(
                                    rs.getInt("id"),
                                    rs.getInt("itemid"),
                                    rs.getInt("chance"),
                                    rs.getInt("continent"),
                                    rs.getByte("dropType"),
                                    rs.getInt("minimum_quantity"),
                                    rs.getInt("maximum_quantity"),
                                    rs.getInt("questid"),
                                    rs.getInt("min_mob_level"),
                                    rs.getInt("max_mob_level"),
                                    rs.getBoolean("onlySelf"),
                                    rs.getInt("period"),
                                    "數據庫"
                            );
                            String[] split = rs.getString("channel").replace("[", "").replace("]", "").replace(" ", "").split(",");
                            Set<Integer> collect = Arrays.stream(split).filter(it -> !it.isEmpty()).map(Integer::valueOf).collect(Collectors.toSet());
                            e.channels.addAll(collect);
                            globaldrops.add(e);
                        }
                    }
                }
            }

            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data_special")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int itemId = rs.getInt("itemid");
                            if (itemId > 0 && !ii.itemExists(itemId)) {
                                continue;
                            }
                            int mapid = rs.getInt("mapid"), dropperid = rs.getInt("dropperid");
                            Map<Integer, List<RewardDropEntry>> drop_data_special = specialdrops.computeIfAbsent(mapid, k -> new HashMap<>());
                            List<RewardDropEntry> list = drop_data_special.computeIfAbsent(mapid, k -> new ArrayList<>());

                            list.add(new RewardDropEntry(
                                    rs.getInt("itemid"),
                                    rs.getInt("chance"),
                                    rs.getInt("quantity"),
                                    rs.getInt("msgType"),
                                    rs.getInt("period"),
                                    rs.getInt("state")));
                            drop_data_special.put(dropperid, list);
                            specialdrops.put(mapid, drop_data_special);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("導入怪物爆率錯誤", e);
        }
    }


    public List<MonsterDropEntry> retrieveDrop(int monsterId) {
//        return drops.computeIfAbsent(monsterId, k -> new ArrayList<>());
        return drops.getOrDefault(monsterId, Collections.emptyList());
    }


    public List<RewardDropEntry> retrieveSpecialDrop(int mapid, int dropperId) {
        Map<Integer, List<RewardDropEntry>> map = specialdrops.get(mapid);
        if (map != null) {
            return map.getOrDefault(dropperId, Collections.emptyList());
        }
        return Collections.emptyList();
//        return specialdrops.computeIfAbsent(mapid, k -> new HashMap<>()).computeIfAbsent(dropperId, k -> new ArrayList<>());
    }

    public RewardDropEntry getReward(int mapid, int dropperId) {
        List<RewardDropEntry> dropEntry = retrieveSpecialDrop(0, dropperId);
        dropEntry.addAll(retrieveSpecialDrop(mapid, dropperId));
        int chance = (int) Math.floor(Math.random() * 1000);
        List<RewardDropEntry> ret = new ArrayList<>();
        for (RewardDropEntry de : dropEntry) {
            if (de.chance >= chance) {
                ret.add(de);
            }
        }
        if (ret.isEmpty()) {
            return null;
        }
        Collections.shuffle(ret);
        return ret.get(Randomizer.nextInt(ret.size()));
    }

    public void addMeso(MapleMonsterStats mons, List<MonsterDropEntry> ret) {
        addMeso(ret, mons.getId(), mons.getLevel(), mons.isBoss(), mons.isPartyBonus(), mons.dropsMesoCount());
    }

    public void addMeso(MapleMonster mob, List<MonsterDropEntry> ret) {
        addMeso(ret, mob.getId(), mob.getMobLevel(), mob.isBoss(), mob.getStats().isPartyBonus(), mob.getStats().dropsMesoCount());
    }

    private void addMeso(List<MonsterDropEntry> ret, int id, int level, boolean isBoss, boolean isPartyBonous, int dropsMesoCount) {
        if (!ServerConfig.ADD_DEFAULT_MESO) {
            return;
        }
        double divided = (level < 100 ? (level < 10 ? (double) level : 10.0) : (level / 10.0));
        int maxMeso = level * (int) Math.ceil(level / divided);
        if (isBoss && !isPartyBonous) {
            maxMeso *= 3;
        }
        for (int i = 0; i < dropsMesoCount; i++) {
            if (id >= 9600086 && id <= 9600098) { //外星人楓幣爆率
                int meso = (int) Math.floor(Math.random() * 500 + 1000);
                ret.add(new MonsterDropEntry(0, 0, 20000, (int) Math.floor(0.46 * meso), meso, 0, false, 0, "楓幣添加"));
            } else {
                ret.add(new MonsterDropEntry(0, 0, isBoss && !isPartyBonous ? 800000 : (isPartyBonous ? 600000 : 400000), (int) Math.floor(0.66 * maxMeso), maxMeso, 0, false, 0, "楓幣添加"));
            }
        }
    }

    public void clearDrops() {
        drops.clear();
        globaldrops.clear();
        specialdrops.clear();
        load();
    }

    public boolean contains(List<MonsterDropEntry> e, int toAdd) {
        for (MonsterDropEntry f : e) {
            if (f.itemId == toAdd) {
                return true;
            }
        }
        return false;
    }

    public String getDrops(int item, boolean showId) {
        List<Integer> dropsfound = new LinkedList<>();
        for (int mobId : StringData.mobStrings.keySet()) {
            for (MonsterDropEntry b : retrieveDrop(mobId)) {
                if (b.itemId == item) {
                    if (!dropsfound.contains(mobId)) {
                        dropsfound.add(mobId);
                    }
                }
            }
        }
        String droplist = "";
        for (int c : dropsfound) {
            droplist += "#o" + c + (showId ? "#(" + c + ")" : "") + "\r\n";
        }
        return droplist;
    }

    public int chanceLogic(int itemId) { //not much logic in here. most of the drops should already be there anyway.
//        switch (itemId) {
//            case 4280000: //永恆的謎之蛋
//            case 4280001: //重生的謎之蛋
//            case 2049301: //裝備強化卷軸
//            case 2049401: //潛能附加卷軸
//                return 5000;
//            case 2049300: //高級裝備強化卷軸
//            case 2049400: //高級潛能附加卷軸
//            case 1002419: //楓葉帽
//                return 2000;
//            case 1002938: //安全帽（1日）
//                return 50;
//        }
        if (ItemConstants.getInventoryType(itemId, false) == MapleInventoryType.EQUIP) {
            return 8000; //with *10
        } else if (ItemConstants.getInventoryType(itemId) == MapleInventoryType.SETUP || ItemConstants.getInventoryType(itemId) == MapleInventoryType.CASH) {
            return 500;
        } else {
            switch (itemId / 10000) {
                case 204: //卷軸
                    return 1800;
                case 207: //飛鏢之類
                case 233: //子彈之類
                    return 3000;
                case 229: //技能書
                    return 400;
                case 401: //礦
                case 402: //礦
                    return 5000;
                case 403:
                    return 4000; //lol
            }
            return 8000;
        }
    }
    //MESO DROP: level * (level / 10) = max, min = 0.66 * max
    //explosive Reward = 7 meso drops
    //boss, ffaloot = 2 meso drops
    //boss = level * level = max
    //no mesos if: mobid / 100000 == 97 or 95 or 93 or 91 or 90 or removeAfter > 0 or invincible or onlyNormalAttack or friendly or dropitemperiod > 0 or cp > 0 or point > 0 or fixeddamage > 0 or selfd > 0 or mobType != null and mobType.charat(0) == 7 or PDRate <= 0
}
