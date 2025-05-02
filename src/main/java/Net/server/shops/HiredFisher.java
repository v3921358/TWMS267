package Net.server.shops;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.ItemLoader;
import Client.inventory.MapleInventoryType;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Database.DatabaseLoader.DatabaseConnection;
import Database.tools.SqlTool;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.Timer;
import Net.server.life.MapleMonsterInformationProvider;
import Net.server.maps.MapleFoothold;
import Net.server.maps.MapleMapObjectType;
import Net.server.reward.RewardDropEntry;
import Packet.PlayerShopPacket;
import Server.cashshop.CashShopServer;
import Server.channel.ChannelServer;
import Server.world.WorldFindService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.Randomizer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;


/**
 * 免責聲明：本模擬器源代碼下載自ragezone.com，僅用於技術研究學習，無任何商業行為。
 */
//class HiredFisher(owner: MapleCharacter?, itemId: Int, time: Long) : AbstractPlayerStore(owner, itemId, "", "", 6) {
public final class HiredFisher extends AbstractPlayerStore {
    private final Logger log = LoggerFactory.getLogger(HiredFisher.class);
    private ScheduledFuture<?> schedule = null;
    private ScheduledFuture<?> repeat = null;
    private long startTime = 0L;
    private long stopTime = 0L;

    public void setId(int id) {
        this.id = id;
    }

    private int level = 0;

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public int getFh() {
        return fh;
    }

    public int getId() {
        return id;
    }

    private int fh = 0;
    private int id = 0;
    private int exp = 0;

    public void setExp(int value) {
        this.exp += value;
    }

    public HiredFisher(MapleCharacter owner, final int itemId, final long time) {
        super(owner, itemId, "", "", 6);
        long lastTime = time * 60 * 60 * 1000;
        startTime = System.currentTimeMillis();
        stopTime = System.currentTimeMillis() + lastTime;
        level = owner.getId();
        MapleFoothold below = owner.getMap().getFootholds().findBelow(owner.getPosition());
        fh = ((below != null) ? below.getId() : 0);
        if (time > 0) {
            this.schedule = Timer.EtcTimer.getInstance().schedule(() -> {
                if (getMCOwner() != null && getMCOwner().getHiredFisher() == this) {
                    getMCOwner().setHiredFisher(null);
                }
                removeAllVisitors(-1, -1);
                closeShop(true, true);
            }, lastTime);
        }
        repeat = Timer.EtcTimer.getInstance().register((this::doFish), 60000L);
    }

    public boolean isHiredFisherItemId() {
        final int n = 5601000;
        final int itemId = this.getItemId();
        return n <= itemId && itemId <= 5601002;
    }

    public void doFish() {
        boolean canFish = false;
        double rate = 1.0;
        MapleCharacter player = null;
        int baitId = 0;
        if (!isHiredFisherItemId()) {
            int channel = WorldFindService.getInstance().findChannel(ownerId);
            if (channel > 0) {
                player = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterById(ownerId);
            } else if (channel == -10) {
                player = CashShopServer.getPlayerStorage().getCharacterById(ownerId);
            }
            if (player != null) {
                if (player.getItemQuantity(2300003) > 0) {
                    baitId = 2300003;
                    rate = 1.9;
                } else if (player.getItemQuantity(2300002) > 0) {
                    baitId = 2300002;
                }
                canFish = true;
            }
        }
        if (!(isHiredFisherItemId() || baitId != 0 && canFish)) {
            closeShop(true, true);
            if (player != null && baitId == 0) {
                player.dropMessage(1, "魚餌已經使用完,僱傭釣手已經關閉.");
            }
            return;
        }
        if (player != null && !isHiredFisherItemId() && canFish) {
            player.removeItem(baitId, 1);
        }
        RewardDropEntry rewardDropEntry = MapleMonsterInformationProvider.getInstance().getReward(getMapId(), itemId);
        if (Randomizer.nextInt(1000) < 400.0 * rate && rewardDropEntry != null) {
            boolean useDatabase = true;
            int itemid = useDatabase ? rewardDropEntry.itemId : fh;
            int quantity = useDatabase ? rewardDropEntry.quantity : 1;
            if (itemid == 0) {
                if (Randomizer.isSuccess(50)) {
                    int mesoreward = Randomizer.rand(15, 75000);
                    this.setMeso((long) mesoreward + getMeso());
                } else {
                    long total = GameConstants.getExpNeededForLevel(level);
                    int expreward = Math.min(Randomizer.nextInt(((int) Math.abs(total / (long) 200) + 1)), 500000);
                    setExp(expreward);
                }
            } else {
                if (MapleItemInformationProvider.getInstance().itemExists(itemid)) {
                    boolean add = false;
                    for (MaplePlayerShopItem shopItem : getItems()) {
                        if (ItemConstants.getInventoryType(itemid, false) != MapleInventoryType.EQUIP && itemid == shopItem.item.getItemId()) {
                            shopItem.item.setQuantity((short) (shopItem.item.getQuantity() + 1));
                            add = true;
                            break;
                        }
                    }
                    if (!add) {
                        Item item = ItemConstants.getInventoryType(itemid, false) == MapleInventoryType.EQUIP ? MapleItemInformationProvider.getInstance().getEquipById(itemid) : new Item(itemid, (byte) 0, (short) quantity);
                        addItem(new MaplePlayerShopItem(item, (short) 0, 0));
                    }
                }
            }
        }
    }

    public boolean isFull(MapleCharacter player) {
        Map<MapleInventoryType, List<Item>> hashMap = new HashMap<>();
        hashMap.put(MapleInventoryType.EQUIP, new ArrayList<>());
        hashMap.put(MapleInventoryType.USE, new ArrayList<>());
        hashMap.put(MapleInventoryType.SETUP, new ArrayList<>());
        hashMap.put(MapleInventoryType.ETC, new ArrayList<>());
        hashMap.put(MapleInventoryType.CASH, new ArrayList<>());
        hashMap.put(MapleInventoryType.DECORATION, new ArrayList<>());
        for (MaplePlayerShopItem shopitem : getItems()) {
            hashMap.get(ItemConstants.getInventoryType(shopitem.item.getItemId())).add(shopitem.item);
        }
        for (Map.Entry<MapleInventoryType, List<Item>> it : hashMap.entrySet()) {
            if (player.getSpace(it.getKey().getType()) < it.getValue().size()) {
                return false;
            }
        }
        for (MaplePlayerShopItem shopitem : getItems()) {
            MapleInventoryManipulator.addFromDrop(player.getClient(), shopitem.item, true);
        }
        getItems().clear();
        return true;
    }

    public void sendSpawnData(MapleClient client) {
        if (client != null) {
            client.announce(PlayerShopPacket.spawnHiredMerchant(this));
        }
    }

    public void sendDestroyData(MapleClient client) {
        if (client != null) {
            client.announce(PlayerShopPacket.destroyHiredMerchant(this.ownerId));
        }
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.HIRED_FISHER;
    }

    @Override
    public int getRange() {
        return GameConstants.maxViewRange();
    }

    public byte getShopType() {
        return 75;
    }

    public void buy(MapleClient c, final int item, final short quantity) {
        //TODO("not implemented")
    }

    public void closeShop(final boolean saveItems, final boolean remove) {
        try {
            if (saveItems) {
                this.saveItems();
                this.items.clear();
            }
            if (remove) {
                ChannelServer.getInstance(this.channel).removeFisher(this);
                this.getMap().broadcastMessage(PlayerShopPacket.destroyHiredMerchant(this.getOwnerId()));
            }
            this.getMap().removeMapObject(this);
        } finally {
            final ScheduledFuture<?> schedule = this.schedule;
            if (schedule != null) {
                schedule.cancel(true);
            }
            final ScheduledFuture<?> repeat = this.repeat;
            if (repeat != null) {
                repeat.cancel(true);
            }
        }
    }

    public boolean saveItems() {
        if (getShopType() != IMaplePlayerShop.HIRED_FISHER) {
            return false;
        }
        return DatabaseConnection.domain(con -> {
            SqlTool.update(con, "DELETE FROM hiredfisher WHERE characterid = ?", ownerId);
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO hiredfisher (characterid, Mesos, exp) VALUES (?, ?, ?)")) {
                ps.setInt(1, ownerId);
                ps.setLong(2, meso.get());
                ps.setInt(3, exp);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        System.err.println("[SaveItems] 保存僱傭釣手信息出錯 - 1");
                        throw new RuntimeException("保存僱傭釣手信息出錯.");
                    }
                }
                List<Pair<Item, MapleInventoryType>> ret = new ArrayList<>();
                ret.stream().filter(it -> it.left != null && (it.left.getQuantity() > 0 || ItemConstants.類型.可充值道具(it.left.getItemId()))).forEach(it -> {
                    Item copy = it.left.copy();
                    ret.add(new Pair<>(copy, ItemConstants.getInventoryType(copy.getItemId())));
                });
                ItemLoader.釣魚道具.saveItems(con, ret, ownerId);
                return true;
            } catch (SQLException e) {
                log.error("[SaveItems] 保存釣魚道具信息出錯 ", e);
                return false;
            }
        });
    }

    public boolean isDone() {
        return System.currentTimeMillis() >= this.stopTime;
    }
}