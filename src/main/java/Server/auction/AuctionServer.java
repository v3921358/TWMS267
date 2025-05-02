package Server.auction;

import Client.MapleCharacter;
import Client.inventory.*;
import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Database.DatabaseException;
import Database.DatabaseLoader.DatabaseConnection;
import Database.mapper.AuctionItemMapper;
import Database.tools.SqlTool;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.Timer;
import Packet.AuctionPacket;
import Packet.MaplePacketCreator;
import Server.ServerType;
import Server.channel.PlayerStorage;
import Server.netty.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static Server.auction.AuctionItemState.*;

/**
 * 拍賣伺服器
 *
 * @author Ethan
 */
public class AuctionServer {

    public static final Map<Integer, Map<Integer, Map<Integer, Map<Integer, Pair<Integer, Integer>>>>> auctions = new HashMap<>();
    public static final AtomicLong runningSNID;
    private static final Logger log = LoggerFactory.getLogger(AuctionServer.class);
    private static final AuctionServer instance = new AuctionServer();

    static {
        runningSNID = new AtomicLong(DatabaseConnection.domain(con -> {
            ResultSet rs = SqlTool.query(con, "SELECT MAX(id) FROM `auction`");
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        }, "讀取MaxAuctionId異常", true) + 1);
    }

    public final Map<Long, AuctionItem> items = new TreeMap<>(Comparator.reverseOrder());
    private final Map<Integer, List<Integer>> collections = new HashMap<>();
    public final ReentrantLock lock = new ReentrantLock();
    private final ReentrantLock sqlLock = new ReentrantLock();
    private ServerConnection init;
    private PlayerStorage players;
    private boolean finishedShutdown = false;
    private short port;
    private short channel;
    private short world;
    private ScheduledFuture schedule;

    public static AuctionServer getInstance() {
        return instance;
    }

    public void init() {
        port = ServerConfig.AUCTION_PORT;
        world = 0;
        this.channel = -20;
        players = new PlayerStorage(-20);
        schedule = Timer.ExpiredTimer.getInstance().schedule(new ExpiredCheckThread(), 60000);

        lock.lock();
        List<AuctionItem> auctionItems = SqlTool.queryAndGetList("SELECT * FROM `auction` WHERE `world` = ?", new AuctionItemMapper(), world);
        try {
            for (AuctionItem ai : auctionItems) {
                Map<Long, Pair<Item, MapleInventoryType>> map = ItemLoader.拍賣道具.loadItems(false, ai.id);
                if (!map.isEmpty()) {
                    Iterator<Pair<Item, MapleInventoryType>> iterator = map.values().iterator();
                    if (iterator.hasNext()) { //應該只存在一個
                        ai.item = iterator.next().left;
                    }
                }
                this.items.put(ai.id, ai);
            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            lock.unlock();
        }
        run();
    }

    public void run() {
        try {
            init = new ServerConnection(port, 0, -20, ServerType.AuctionServer);
            init.run();
            //log.info("拍賣場伺服器綁定連接埠: " + port + ".");
        } catch (final Exception e) {
            throw new RuntimeException("拍賣場伺服器綁定連接埠 " + port + " 失敗", e);
        }
    }

    public void updateAuctionItem(final AuctionItem auctionItem) {
        lock.lock();
        try {
            SqlTool.update("UPDATE `auction` SET `number` = ?, `other_id` = ?, `other` = ?, `state` = ?, `startdate` = ?, `expiredate` = ?, `donedate` = ? WHERE `id` = ?", auctionItem.number, auctionItem.other_id, auctionItem.other, auctionItem.state, auctionItem.startdate, auctionItem.expiredate, auctionItem.donedate, auctionItem.id);
        } finally {
            lock.unlock();
        }
    }

    public final void changeAuctionItemWorld(final AuctionItem auctionItem) {
        DatabaseConnection.domain(con -> {
            sqlLock.lock();
            try {
                SqlTool.update(con, "DELETE FROM `auction` WHERE `id` = ? ", auctionItem.id);
                SqlTool.update(con, "INSERT INTO `auction` (`id`, `world`, `accounts_id`, `characters_id`, `owner`, `other_id`, `other`, `itemid`, `number`, `type`, `price`, `state`, `startdate`, `expiredate`, `donedate`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", auctionItem.id, world, auctionItem.accounts_id, auctionItem.characters_id, auctionItem.owner, auctionItem.other_id, auctionItem.other, auctionItem.itemid, auctionItem.number, auctionItem.type, auctionItem.price, auctionItem.state, auctionItem.startdate, auctionItem.expiredate, auctionItem.donedate);
                if (auctionItem.item != null) {
                    ItemLoader.拍賣道具.saveItems(con, Collections.singletonList(new Pair<>(auctionItem.item, ItemConstants.getInventoryType(auctionItem.itemid))), auctionItem.id);
                }
            } finally {
                sqlLock.unlock();
            }
            return null;
        });
    }

    public final AuctionItem terminateById(final MapleCharacter player, final long n) {
        this.lock.lock();
        try {
            AuctionItem auctionItem = this.items.get(n);
            if (auctionItem != null && auctionItem.characters_id == player.getId() && auctionItem.state == ONSALE) {
                auctionItem.state = TERMINATE;
                auctionItem.donedate = System.currentTimeMillis();
                return auctionItem;
            }
            return null;
        } finally {
            this.lock.unlock();
        }
    }

    public final List<AuctionItem> getAllOnsaleItemByPlayerId(final int n) {
        this.lock.lock();
        try {
            return items.values().parallelStream().filter(item -> item.characters_id == n && item.state == ONSALE).collect(Collectors.toList());
        } finally {
            this.lock.unlock();
        }
    }

    public final List<AuctionItem> getAllCollectionItemByPlayerId(final int n) {
        this.lock.lock();
        try {
            return items.values().parallelStream().filter(item -> item.characters_id == n && item.state == ONSALE).collect(Collectors.toList());
        } finally {
            this.lock.unlock();
        }
    }

    public final int getItemCountByPlayerId(int n) {
        this.lock.lock();
        try {
            return (int) items.values().parallelStream().filter(item -> item.characters_id == n && (item.state == ONSALE || item.state == TERMINATE)).count();
        } finally {
            this.lock.unlock();
        }
    }

    public final List<AuctionItem> getAllNotOnsaleItemByPlayerId(final int n) {
        this.lock.lock();
        try {
            return items.values().parallelStream().filter(item -> item.characters_id == n && item.state > ONSALE).collect(Collectors.toList());
        } finally {
            this.lock.unlock();
        }
    }

    public final int getAllNotOnsaleItemCountByPlayerId(int n) {
        this.lock.lock();
        try {
            return (int) items.values().parallelStream().filter(item -> item.characters_id == n && item.state > ONSALE && item.state <= DIFFERENCE).count();
        } finally {
            this.lock.unlock();
        }
    }

    public final List<AuctionItem> findItem(final String fstr, final int minItemId, final int maxItemId, final int minLevel, final int maxLevel, final int grade) {
        this.lock.lock();
        try {

            final List<AuctionItem> list = new ArrayList<>();
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            for (AuctionItem item : this.items.values()) {
                if (item.state == 0 && item.item != null && (fstr.isEmpty() || ii.getName(item.itemid).contains(fstr)) && ii.getReqLevel(item.itemid) >= minLevel && ii.getReqLevel(item.itemid) <= maxLevel && item.itemid >= minItemId && item.itemid <= maxItemId) {
                    if (grade != -1 && item.item instanceof Equip) {
                        if ((((Equip) item.item).getState(false) & grade) == 0x0) {
                            list.add(item);
                        }
                    } else {
                        list.add(item);
                    }
                }
            }
            return list;
        } finally {
            this.lock.unlock();
        }
    }

    public final List<AuctionItem> findBuyerItemByLevel(final int minLevel, final int maxLevel) {
        this.lock.lock();
        try {

            final List<AuctionItem> list = new ArrayList<>();
            final MapleItemInformationProvider s283 = MapleItemInformationProvider.getInstance();
            for (AuctionItem item : this.items.values()) {
                if (item.state == BUYER_DONE && s283.getReqLevel(item.itemid) >= minLevel && s283.getReqLevel(item.itemid) <= maxLevel) {
                    list.add(item);
                }
            }
            return list;
        } finally {
            this.lock.unlock();
        }
    }

    public final AuctionItem getItemBySN(final long n) {
        this.lock.lock();
        try {
            return this.items.get(n);
        } finally {
            this.lock.unlock();
        }
    }

    public final void buy(final MapleCharacter player, final int auctionOptType, final long n, int number) {
        if (number < 0 || number > Short.MAX_VALUE) {
            player.getClient().getSession().close();
        }
        this.lock.lock();
        try {
            final AuctionItem ai;
            if ((ai = this.items.get(n)) != null && ai.characters_id != player.getId() && ai.state == 0 && ai.item != null) {
                final long n2 = ai.price * number;
                if (((ai.type == 2) ? player.getCSPoints(2) : player.getMeso()) < ai.price) {
                    player.getClient().announce(AuctionPacket.auctionResult(auctionOptType, (ai.type == 2) ? 900 : 106));
                    return;
                }
                if (!MapleInventoryManipulator.checkSpace(player.getClient(), ai.itemid, number, "")) {
                    player.getClient().announce(AuctionPacket.auctionResult(auctionOptType, 1));
                    return;
                }
                if (number > ai.number) {
                    player.getClient().announce(AuctionPacket.auctionResult(auctionOptType, 102));
                    return;
                }
                if (/*ai.type == 1 &&*/player.getMeso() < n2) {
                    player.getClient().announce(AuctionPacket.auctionResult(auctionOptType, 106));
                    return;
                }
//                if (ai.type == 2 && player.getCSPoints(2) < n2) {
//                    player.getClient().announce(AuctionPacket.auctionResult(auctionOptType, 900));
//                    return;
//                }
//                if (ai.type == 1) {
                player.gainMeso(-n2, false);
//                } else {
//                    if (ai.type != 2) {
//                        return;
//                    }
//                    player.modifyCSPoints(2, -(int) n2);
//                }
                final Item copy = ai.item.copy();
                boolean b584 = false;
                final long akd = ai.price;
                if (number < ai.item.getQuantity() && copy.getType() == 2 && !ItemConstants.類型.可充值道具(copy.getItemId())) {
                    copy.setQuantity((short) number);
                    ai.item.setQuantity((short) (ai.item.getQuantity() - number));
                    ai.number = ai.item.getQuantity();
                    b584 = true;
                } else {
                    ai.state = SELLER_DONE;
                    ai.item = null;
                    ai.price = akd * ai.number;
                    ai.number = 0;
                }
                if (ItemAttribute.TradeOnce.check(copy.getAttribute())) {
                    copy.removeAttribute(ItemAttribute.TradeOnce.getValue());
                }
                if (ItemAttribute.CutUsed.check(copy.getAttribute())) {
                    copy.removeAttribute(ItemAttribute.CutUsed.getValue());
                }
                if (copy.getType() == 2) {
                    copy.setSN(MapleInventoryIdentifier.getInstance());
                }
                final AuctionItem b586 = new AuctionItem();
                (b586).id = AuctionServer.runningSNID.getAndIncrement();
                b586.accounts_id = player.getAccountID();
                b586.characters_id = player.getId();
                b586.owner = player.getName();
                b586.state = BUYER_DONE;
                b586.type = ai.type;
                b586.itemid = copy.getItemId();
                if (ItemConstants.類型.可充值道具(copy.getItemId())) {
                    b586.number = 1;
                    b586.price = akd;
                } else {
                    b586.number = copy.getQuantity();
                    b586.price = akd * copy.getQuantity();
                }
                b586.startdate = ai.startdate;
                b586.expiredate = System.currentTimeMillis() + 31536000000L;
                b586.donedate = System.currentTimeMillis();
                b586.item = copy;
                b586.other_id = ai.characters_id;
                b586.other = ai.owner;
                if (b584) {
                    final AuctionItem ain;
                    (ain = new AuctionItem()).id = AuctionServer.runningSNID.getAndIncrement();
                    ain.accounts_id = ai.accounts_id;
                    ain.characters_id = ai.characters_id;
                    ain.owner = ai.owner;
                    ain.state = SELLER_DONE;
                    ain.type = ai.type;
                    ain.itemid = copy.getItemId();
                    ain.price = ai.price * copy.getQuantity();
                    ain.startdate = ai.startdate;
                    ain.expiredate = System.currentTimeMillis() + 31536000000L;
                    ain.donedate = System.currentTimeMillis();
                    ain.number = copy.getQuantity();
                    ain.other_id = player.getId();
                    ain.other = player.getName();
                    this.changeAuctionItemWorld(ain);
                    this.items.put(ain.id, ain);
                } else {
                    ai.donedate = System.currentTimeMillis();
                }
                this.changeAuctionItemWorld(ai);
                this.changeAuctionItemWorld(b586);
                this.items.put(b586.id, b586);
                player.getClient().announce(MaplePacketCreator.showCharCash(player));
                player.getClient().announce(AuctionPacket.updateAuctionItemInfo(auctionOptType, b586));
            } else {
                player.getClient().announce(AuctionPacket.auctionResult(auctionOptType, 102));
            }
        } finally {
            this.lock.unlock();
        }
    }

    public PlayerStorage getPlayerStorage() {
        return players;
    }

    public short getPort() {
        return port;
    }

    public void shutdown() {
        if (finishedShutdown) {
            return;
        }
        log.info("正在關閉拍賣場伺服器...");
        players.disconnectAll();
        log.info("拍賣場伺服器解除連接埠綁定...");
        init.close();
        if (schedule != null) {
            schedule.cancel(false);
            schedule = null;
        }
        finishedShutdown = true;
    }

    private final class ExpiredCheckThread implements Runnable {

        @Override
        public void run() {
            lock.lock();
            try {
                List<Long> expiredItems = new ArrayList<>();
                final long currentTimeMillis = System.currentTimeMillis();
                items.forEach((n5, auctionItem) -> {
                    if (auctionItem.state == ONSALE) {
                        if (currentTimeMillis >= auctionItem.expiredate) {
                            auctionItem.state = TERMINATE;
                            auctionItem.donedate = currentTimeMillis;
                            auctionItem.expiredate = currentTimeMillis + 31536000000L;//一年後
                            changeAuctionItemWorld(auctionItem);
                        } else {
                        }
                    } else {
                        if (currentTimeMillis >= auctionItem.expiredate) {
                            expiredItems.add(n5);
                        } else {
                        }
                    }
                });
                expiredItems.forEach(id -> {
                    items.remove(id);
                    sqlLock.lock();
                    try {
                        SqlTool.update("DELETE FROM `auction` WHERE `id` = ? ", id);
                    } finally {
                        sqlLock.unlock();
                    }
                });
            } finally {
                lock.unlock();
            }
        }
    }
}
