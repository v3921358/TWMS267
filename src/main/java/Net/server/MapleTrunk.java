package Net.server;

import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.ItemAttribute;
import Client.inventory.ItemLoader;
import Client.inventory.MapleInventoryType;
import Config.constants.ItemConstants;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Database.tools.SqlTool;
import Packet.NPCPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MapleTrunk {

    private static final Logger log = LoggerFactory.getLogger(MapleTrunk.class);
    private final int storageId; //倉庫ID
    private final int accountId; //帳號ID
    private final List<Item> items;
    private final Map<MapleInventoryType, List<Item>> typeItems = new EnumMap<>(MapleInventoryType.class);
    //    private Long meso;
    private short slots;
    private int storageNpcId; //倉庫的NPCID
    private boolean changed = false; //倉庫是否發生改變
    private boolean pwdChecked = false;

    private MapleTrunk(int storageId, short slots, Long meso, int accountId) {
        this.storageId = storageId;
        this.slots = slots;
//        this.meso = meso;
        this.accountId = accountId;
        this.items = new LinkedList<>();
        if (this.slots > 128) {
            this.slots = 128;
            this.changed = true;
        }
    }

    /*
     * 創建1個新的倉庫信息
     */
    public static int create(int accountId) throws SQLException {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO `storages` (`accountid`, `slots`, `meso`) VALUES (?, ?, ?)", DatabaseConnectionEx.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, accountId);
                ps.setInt(2, 4);
                ps.setInt(3, 0);
                ps.executeUpdate();

                int storageid;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        storageid = rs.getInt(1);
                        ps.close();
                        rs.close();
                        return storageid;
                    }
                }
            }
        }
        throw new SQLException("Inserting char failed.");
    }

    /*
     * 從SQL中讀取倉庫信息 如果沒有就創建1個新的倉庫信息
     */
    public static MapleTrunk loadOrCreateFromDB(int accountId) {
        MapleTrunk ret = null;
        int storeId;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM storages WHERE accountid = ?");
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                storeId = rs.getInt("storageid");
                ret = new MapleTrunk(storeId, rs.getShort("slots"), rs.getLong("meso"), accountId);
                rs.close();
                ps.close();
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                for (Pair<Item, MapleInventoryType> mit : ItemLoader.倉庫道具.loadItems(false, accountId).values()) {
                    Item item = mit.getLeft();
                    if (item.getItemId() / 1000000 == 1 && ii.isDropRestricted(item.getItemId()) && !ItemAttribute.TradeOnce.check(item.getAttribute())) {
                        item.addAttribute((short) ItemAttribute.TradeOnce.getValue());
                    }
                    ret.items.add(item);
                }
            } else {
                storeId = create(accountId);
                ret = new MapleTrunk(storeId, (short) 4, (long) 0, accountId);
                rs.close();
                ps.close();
            }
        } catch (SQLException ex) {
//            System.err.println("Error loading storage. accId=" + accountId + "\r\n" + ex);
            log.error("Error loading storage. accId=" + accountId, ex);
        }
        return ret;
    }

    /*
     * 保存倉庫
     */
    public void saveToDB() {
        saveToDB(null);
    }

    /*
     * 保存倉庫
     */
    public void saveToDB(Connection con) {
        if (!changed) {
            return;
        }
        boolean needcolse = false;
        try {
            if (con == null) {
                con = DatabaseConnectionEx.getInstance().getConnection();
            }
            PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ? WHERE storageid = ?");
            ps.setInt(1, slots);
//            ps.setLong(2, meso);
            ps.setInt(2, storageId);
            ps.executeUpdate();
            ps.close();

            List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
            for (Item item : items) {
                itemsWithType.add(new Pair<>(item, ItemConstants.getInventoryType(item.getItemId())));
            }
            ItemLoader.倉庫道具.saveItems(con, itemsWithType, accountId);
            this.changed = false;
        } catch (SQLException ex) {
            log.error("Error saving storage", ex);
        } finally {
            if (needcolse) {
                try {
                    con.close();
                } catch (SQLException e) {
                    log.error("Error saving storage", e);
                }
            }
        }
    }

    /*
     * 取出道具時獲取道具在倉庫的信息
     */
    public Item getItem(short slot) {
        if (slot >= items.size() || slot < 0) {
            return null;
        }
        return items.get(slot);
    }

    /*
     * 取出道具
     */
    public Item takeOut(short slot) {
        this.changed = true;
        Item ret = items.remove(slot);
        MapleInventoryType type = ItemConstants.getInventoryType(ret.getItemId());
        typeItems.put(type, new ArrayList<>(filterItems(type)));
        return ret;
    }

    /*
     * 保存倉庫道具信息
     */
    public void store(Item item) {
        this.changed = true;
        items.add(item);
        MapleInventoryType type = ItemConstants.getInventoryType(item.getItemId());
        typeItems.put(type, new ArrayList<>(filterItems(type)));
    }

    /*
     * 對倉庫道具進行排序
     */
    public void arrange() {
        items.sort(Comparator.comparingInt(Item::getItemId));
        for (MapleInventoryType type : MapleInventoryType.values()) {
            typeItems.put(type, new ArrayList<>(items));
        }
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    private List<Item> filterItems(MapleInventoryType type) {
        List<Item> ret = new LinkedList<>();
        for (Item item : items) {
            if (ItemConstants.getInventoryType(item.getItemId()) == type) {
                ret.add(item);
            }
        }
        return ret;
    }

    public short getSlot(MapleInventoryType type, short slot) {
        short ret = 0;
        List<Item> it = typeItems.get(type);
        if (it == null || slot >= it.size() || slot < 0) {
            return -1;
        }
        for (Item item : items) {
            if (item == it.get(slot)) {
                return ret;
            }
            ret++;
        }
        return -1;
    }

    public void secondPwdRequest(MapleClient c, int npcId) {
        c.announce(NPCPacket.getStoragePwd(npcId == -1));
        if (npcId != -1) {
            this.storageNpcId = npcId;
        }
    }

    public void sendStorage(MapleClient c) {
        items.sort((o1, o2) -> {
            if (ItemConstants.getInventoryType(o1.getItemId()).getType() < ItemConstants.getInventoryType(o2.getItemId()).getType()) {
                return -1;
            } else if (ItemConstants.getInventoryType(o1.getItemId()) == ItemConstants.getInventoryType(o2.getItemId())) {
                return 0;
            } else {
                return 1;
            }
        });
        for (MapleInventoryType type : MapleInventoryType.values()) {
            typeItems.put(type, new ArrayList<>(items));
        }
        c.announce(NPCPacket.getStorage(storageNpcId, slots, items, getMeso()));
    }

    public void update(MapleClient c) {
        c.announce(NPCPacket.arrangeStorage(slots, items, true));
    }

    public void sendStored(MapleClient c, MapleInventoryType type) {
        c.announce(NPCPacket.storeStorage(slots, type, typeItems.get(type)));
    }

    public void sendTakenOut(MapleClient c, MapleInventoryType type) {
        c.announce(NPCPacket.takeOutStorage(slots, type, typeItems.get(type)));
    }

    public long getMeso() {
        return SqlTool.queryAndGet("SELECT meso FROM storages WHERE accountid = ?", rs -> rs.getLong(1), accountId);
    }

    public long getMesoForUpdate(Connection con) {
        Long meso = SqlTool.queryAndGet(con, "SELECT meso FROM storages WHERE accountid = ? FOR UPDATE", rs -> rs.getLong(1), accountId);
        if (meso == null) {
            return 0;
        }
        return meso;
    }

    public void setMeso(Connection con, long meso) {
        if (meso < 0) {
            return;
        }
        this.changed = true;
//        this.meso = meso;
        SqlTool.update(con, "UPDATE storages SET meso = ? WHERE accountid = ?", meso, accountId);
    }

    /*
     * 檢測倉庫道具是否有指定道具ID的重複信息
     */
    public Item findById(int itemId) {
        for (Item item : items) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public void sendMeso(MapleClient c) {
        c.announce(NPCPacket.mesoStorage(slots, getMeso()));
    }

    public boolean isFull() {
        return items.size() >= slots;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(short set) {
        this.changed = true;
        this.slots = set;
    }

    public void increaseSlots(byte gain) {
        this.changed = true;
        this.slots += gain;
    }

    public int getNpcId() {
        return storageNpcId;
    }

    public void close() {
        pwdChecked = false;
        typeItems.clear();
    }

    public void setPwdChecked(boolean value) {
        pwdChecked = value;
    }

    public boolean isPwdChecked() {
        return pwdChecked;
    }
}
