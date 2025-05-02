package Client.inventory;

import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Net.server.MapleItemInformationProvider;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MapleInventory implements Iterable<Item>, Serializable {

    private static final long serialVersionUID = -7238868473236710891L;
    private final Map<Short, Item> inventory = new LinkedHashMap<>();
    private final MapleInventoryType type;
    private short slotLimit = 0;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new instance of MapleInventory
     */
    public MapleInventory(MapleInventoryType type) {
        this.type = type;
    }

    public Item getItem(short slot) {
        lock.readLock().lock();
        try {
            return inventory.get(slot);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<Short, Item> getInventory() {
        lock.readLock().lock();
        try {
            return inventory;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static final int MAX_SLOT_LIMIT = 128;

    public short getTrueSlotLimit() {
        return slotLimit;
    }

    public short getSlotLimit() {
        if (ServerConfig.DEFAULT_MAX_SLOT) {
            return MAX_SLOT_LIMIT;
        }
        return getTrueSlotLimit();
    }

    public void setSlotLimit(short slot) {
        if (slot > MAX_SLOT_LIMIT || type == MapleInventoryType.CASH || type == MapleInventoryType.DECORATION) {
            slot = MAX_SLOT_LIMIT;
        }
        slotLimit = slot;
    }

    public MapleInventoryType getType() {
        return type;
    }

    public List<Item> newList() {
        lock.readLock().lock();
        try {
            if (inventory.size() <= 0) {
                return Collections.emptyList();
            }
            return new LinkedList<>(inventory.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addFromDB(Item item) {
        lock.writeLock().lock();
        try {
            if (item.getPosition() < 0 && !type.equals(MapleInventoryType.EQUIPPED)) {
                // This causes a lot of stuck problem, until we are done with position checking
                return;
            }
            if (item.getPosition() > 0 && type.equals(MapleInventoryType.EQUIPPED)) {
                // This causes a lot of stuck problem, until we are done with position checking
                return;
            }
            inventory.put(item.getPosition(), item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeItem(short slot) {
        removeItem(slot, (short) 1, false);
    }

    public void removeItem(short slot, short quantity, boolean allowZero) {
        lock.writeLock().lock();
        try {
            Item item = inventory.get(slot);
            if (item == null) {
                return;
            }
            item.setQuantity((short) (item.getQuantity() - quantity));
            if (item.getQuantity() < 0) {
                item.setQuantity((short) 0);
            }
            if (item.getQuantity() == 0 && !allowZero) {
                removeSlot(slot);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeSlot(short slot) {
        lock.writeLock().lock();
        try {
            inventory.remove(slot);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Collection<Item> list() {
        lock.readLock().lock();
        try {
            return inventory.values();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isFull() {
        return inventory.size() >= slotLimit;
    }

    public boolean isFull(int margin) {
        return inventory.size() + margin >= slotLimit;
    }

    public int listSize() {
        lock.readLock().lock();
        try {
            int n = 0;
            for (Item item : list()) {
                if (item.getPosition() > 10000) {
                    ++n;
                }
            }
            return inventory.size() - n;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the next empty slot id, -1 if the inventory is full
     */
    public short getNextFreeSlot() {
        lock.readLock().lock();
        try {
            if (isFull()) {
                return -1;
            }
            for (short i = 1; i <= slotLimit; i++) {
                if (!inventory.containsKey(i)) {
                    return i;
                }
            }
            return -1;
        } finally {
            lock.readLock().unlock();
        }
    }

    public short getNumFreeSlot() {
        lock.readLock().lock();
        try {
            if (isFull()) {
                return 0;
            }
            short free = 0;
            for (short i = 1; i <= slotLimit; i++) {
                if (!inventory.containsKey(i)) {
                    free++;
                }
            }
            return free;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addSlot(short slot) {
        this.slotLimit += slot;
        if (slotLimit > 128) {
            slotLimit = 128;
        }
    }

    /**
     * Returns the item with its slot id if it exists within the inventory,
     * otherwise null is returned
     */
    public Item findById(int itemId) {
        lock.readLock().lock();
        try {
            for (Item item : inventory.values()) {
                if (item.getItemId() == itemId) {
                    return item;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Item findBySN(long sn, int itemId) {
        Item item = findByLiSN(sn);
        if (item.getItemId() != itemId) {
            return null;
        }
        return item;
    }

    public Item findByLiSN(long sn) {
        lock.readLock().lock();
        try {
            for (Item item : inventory.values()) {
                if (item.getSN() == sn) {
                    return item;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int countById(int itemId) {
        lock.readLock().lock();
        try {
            int possesed = 0;
            for (Item item : inventory.values()) {
                if (item.getItemId() == itemId) {
                    possesed += item.getQuantity();
                }
            }
            return possesed;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Item> listById(int itemId) {
        lock.readLock().lock();
        try {
            List<Item> ret = new ArrayList<>();
            for (Item item : inventory.values()) {
                if (item.getItemId() == itemId) {
                    ret.add(item);
                }
            }
            if (ret.size() > 1) {
                Collections.sort(ret);
            }
            return ret;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Item> listBySN(int sn) {
        lock.readLock().lock();
        try {
            List<Item> ret = new ArrayList<>();
            for (Item item : inventory.values()) {
                if (item.getSN() > 0 && item.getSN() == sn) {
                    ret.add(item);
                }
            }
            if (ret.size() > 1) {
                Collections.sort(ret);
            }
            return ret;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Integer> listIds() {
        lock.readLock().lock();
        try {
            List<Integer> ret = new ArrayList<>();
            for (Item item : inventory.values()) {
                if (!ret.contains(item.getItemId())) {
                    ret.add(item.getItemId());
                }
            }
            if (ret.size() > 1) {
                Collections.sort(ret);
            }
            return ret;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Adds the item to the inventory and returns the assigned slot id
     */
    public short addItem(Item item) {
        lock.writeLock().lock();
        try {
            short slotId = getNextFreeSlot();
            if (slotId < 0) {
                return -1;
            }
            inventory.put(slotId, item);
            item.setPosition(slotId);
            return slotId;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void move(short sSlot, short dSlot, short slotMax) {
        lock.writeLock().lock();
        try {
            Item source = inventory.get(sSlot);
            Item target = inventory.get(dSlot);
            if (source == null) {
                throw new InventoryException("Trying to move empty slot");
            }
            if (target == null) {
                if (dSlot < 0 && !type.equals(MapleInventoryType.EQUIPPED)) {
                    // This causes a lot of stuck problem, until we are done with position checking
                    return;
                }
                if (dSlot > 0 && type.equals(MapleInventoryType.EQUIPPED)) {
                    // This causes a lot of stuck problem, until we are done with position checking
                    return;
                }
                source.setPosition(dSlot);
                inventory.put(dSlot, source);
                inventory.remove(sSlot);
            } else if (target.getItemId() == source.getItemId() && !ItemConstants.類型.可充值道具(source.getItemId()) && target.getOwner().equals(source.getOwner()) && target.getExpiration() == source.getExpiration() && target.getFamiliarCard() == null && source.getFamiliarCard() == null) {
                if (type.getType() == MapleInventoryType.EQUIP.getType() || type.getType() == MapleInventoryType.CASH.getType() || type.getType() == MapleInventoryType.DECORATION.getType()) {
                    swap(target, source);
                } else if (source.getQuantity() + target.getQuantity() > slotMax) {
                    source.setQuantity((short) ((source.getQuantity() + target.getQuantity()) - slotMax));
                    target.setQuantity(slotMax);
                } else {
                    target.setQuantity((short) (source.getQuantity() + target.getQuantity()));
                    inventory.remove(sSlot);
                }
            } else {
                swap(target, source);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void swap(Item source, Item target) {
        lock.writeLock().lock();
        try {
            inventory.remove(source.getPosition());
            inventory.remove(target.getPosition());
            short swapPos = source.getPosition();
            source.setPosition(target.getPosition());
            target.setPosition(swapPos);
            inventory.put(source.getPosition(), source);
            inventory.put(target.getPosition(), target);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeExtendedSlot(int slot) {
        lock.writeLock().lock();
        try {
            inventory.values().removeIf(item -> item.getPosition() / 10000 - 1 == slot);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeAll() {
        lock.writeLock().lock();
        try {
            inventory.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /*
     * 獲取裝備中的技能皮膚道具ID信息
     */
    public List<Integer> listSkillSkinIds() {
        lock.readLock().lock();
        try {
            List<Integer> ret = new ArrayList<>();
            for (Item item : inventory.values()) {
                if (item.isSkillSkin() && !ret.contains(item.getItemId())) {
                    ret.add(item.getItemId());
                }
            }
            return ret;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 獲取角色當前使用的弓矢
     *
     * @param level 角色等級
     */
    public Item getArrowSlot(int level) {
        lock.readLock().lock();
        try {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            List<Item> list = new LinkedList<>();
            for (Item item : inventory.values()) {
                if (item.getPosition() <= 128) {
                    list.add(item);
                }
            }
            list.sort(Comparator.naturalOrder());
            for (Item item : list) {
                if (ItemConstants.類型.弓矢(item.getItemId()) && level >= ii.getReqLevel(item.getItemId())) {
                    return item;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 獲取角色當前使用的弩矢
     *
     * @param level 角色等級
     */
    public Item getCrossbowSlot(int level) {
        lock.readLock().lock();
        try {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            List<Item> list = new LinkedList<>();
            for (Item item : inventory.values()) {
                if (item.getPosition() <= 128) {
                    list.add(item);
                }
            }
            list.sort(Comparator.naturalOrder());
            for (Item item : list) {
                if (ItemConstants.類型.弩矢(item.getItemId()) && level >= ii.getReqLevel(item.getItemId())) {
                    return item;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 獲取角色當前使用的子彈
     *
     * @param level 角色等級
     */
    public Item getBulletSlot(int level) {
        lock.readLock().lock();
        try {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            List<Item> list = new LinkedList<>();
            for (Item item : inventory.values()) {
                if (item.getPosition() <= 128) {
                    list.add(item);
                }
            }
            list.sort(Comparator.naturalOrder());
            for (Item item : list) {
                if (ItemConstants.類型.子彈(item.getItemId()) && level >= ii.getReqLevel(item.getItemId())) {
                    return item;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 獲取角色當前使用的飛鏢
     *
     * @param level 角色等級
     */
    public Item getDartsSlot(int level) {
        lock.readLock().lock();
        try {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            List<Item> list = new LinkedList<>();
            for (Item item : inventory.values()) {
                if (item.getPosition() <= 128) {
                    list.add(item);
                }
            }
            list.sort(Comparator.naturalOrder());
            for (Item item : list) {
                if ((ItemConstants.類型.飛鏢(item.getItemId()) && level >= ii.getReqLevel(item.getItemId())) || item.getItemId() / 1000 == 5021) {
                    return item;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Iterator<Item> iterator() {
        return Collections.unmodifiableCollection(inventory.values()).iterator();
    }
}
