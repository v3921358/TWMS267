package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.ItemLoader;
import Client.inventory.MapleInventoryType;
import Config.constants.ItemConstants;
import Database.DatabaseLoader;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.MerchItemPackage;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Net.server.shops.HiredFisher;
import Packet.MaplePacketCreator;
import Server.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.data.MaplePacketReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 免責聲明：本模擬器源代碼下載自ragezone.com，僅用於技術研究學習，無任何商業行為。
 */
public class HiredFisherHandler {

    private static final Logger log = LoggerFactory.getLogger("HiredFisher");

    public static void UseHiredFisher(MaplePacketReader slea, MapleCharacter player) {
        byte operation = slea.readByte();
        HiredFisher hiredFisher;
        MapleMapObject arg9999;
        switch (operation) {
            case 0:
                check(player.getClient(), true);
                break;
            case 3: {
                slea.readInt();
                ((HiredFisher) player.getMap().getMapObject(slea.readInt(), MapleMapObjectType.HIRED_FISHER)).closeShop(true, true);
                break;
            }
            case 4:
                slea.readInt();
                hiredFisher = (HiredFisher) player.getMap().getMapObject(slea.readInt(), MapleMapObjectType.HIRED_FISHER);
                if (hiredFisher.isHiredFisherItemId() && player.getId() == hiredFisher.getOwnerId()) {
                    if (!hiredFisher.isDone()) {
                        player.send(MaplePacketCreator.openFishingStorage(43, hiredFisher, null, 0));
                    }
                    return;
                }
                player.send(MaplePacketCreator.openFishingStorage(28, hiredFisher, null, 0));
                break;
            case 6:
                slea.readInt();
                hiredFisher = (HiredFisher) player.getMap().getMapObject(slea.readInt(), MapleMapObjectType.HIRED_FISHER);
                if (!hiredFisher.isHiredFisherItemId() && player.getId() == hiredFisher.getOwnerId()) {
                    if (hiredFisher.isFull(player)) {
                        player.send(MaplePacketCreator.openFishingStorage(28, hiredFisher, null, 0));
                        return;
                    }
                    player.send(MaplePacketCreator.openFishingStorage(33, null, null, 0));
                }
                break;
            case 9:
                player.send(MaplePacketCreator.openFishingStorage(35, null, loadItemFrom_Database(player), player.getId()));
                break;
            case 10: {
                boolean hasFisher = World.hasFisher(player.getAccountID(), player.getId());
                if (hasFisher) {
                    player.dropMessage(1, "請關閉現有的僱傭釣手.");
                    player.setConversation(0);
                    return;
                }
                MerchItemPackage pack = loadItemFrom_Database(player);
                if (pack == null) {
                    player.dropMessage(1, "發生了未知錯誤.");
                    return;
                }
                if (!checkRetrieve(player, pack)) {
                    player.send(MaplePacketCreator.openFishingStorage(33, null, null, 0));
                    return;
                }
                if (deletePackage(player.getId())) {
                    if (pack.getMesos() > 0) {
                        player.gainMeso(pack.getMesos(), false);
                        log.info(player.getName() + "僱傭釣手取回獲得楓幣: " + pack.getMesos());
                    }
                    if (pack.getExp() > 0) {
                        player.gainExp(pack.getExp(), true, false, false);
                        log.info(player.getName() + "僱傭釣手取回獲得經驗: " + pack.getExp());
                    }
                    for (Item item : pack.getItems()) {
                        MapleInventoryManipulator.addbyItem(player.getClient(), item, true);
                        log.info(player.getName() + "僱傭釣手取回獲得道具: " + item.getItemId() + " - " + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + " 數量: " + item.getQuantity());
                    }
                    player.send(MaplePacketCreator.openFishingStorage(35, null, loadItemFrom_Database(player), player.getId()));
                    return;
                }
                player.dropMessage(1, "發生了未知錯誤.");
                player.send(MaplePacketCreator.openFishingStorage(29, null, loadItemFrom_Database(player), player.getId()));
            }
        }
    }


    public static boolean check(MapleClient c, boolean bl2) {
        MapleCharacter player = c.getPlayer();
        if (c.getChannelServer().isShutdown()) {
            player.dropMessage(1, "伺服器即將關閉維護，暫時無法進行。");
            return false;
        }
        if (player.getMap() != null && player.getMap().allowFishing()) {
            HiredFisher b2 = World.getFisher(player.getAccountID(), player.getId());
            if (b2 != null) {
                player.getClient().announce(MaplePacketCreator.openFishingStorage(15, null, null, 0));
            } else {
                if (loadItemFrom_Database(player) == null) {
                    if (bl2) {
                        player.getClient().announce(MaplePacketCreator.openFishingStorage(14, null, null, 0));
                    }
                    return true;
                }
                player.getClient().announce(MaplePacketCreator.openFishingStorage(19, null, null, 0));
            }
        }
        return false;
    }

    private static MerchItemPackage loadItemFrom_Database(MapleCharacter player) {
        try {
            Map<Long, Pair<Item, MapleInventoryType>> loadItems = ItemLoader.釣魚道具.loadItems(false, player.getId());
            if (loadItems.isEmpty()) {
                return null;
            }
            MerchItemPackage pack = new MerchItemPackage();
            load(player, pack);
            List<Item> list = new ArrayList<>();
            for (Pair<Item, MapleInventoryType> pair : loadItems.values()) {
                list.add(pair.left);
            }
            pack.setItems(list);
            log.info(player.getName() + "釣魚場保管員 道具數量: " + loadItems.size());
            return pack;
        } catch (SQLException e) {
            log.error("加載釣魚場保管員道具信息出錯" + e);
            return null;
        }

    }

    private static boolean checkRetrieve(MapleCharacter player, MerchItemPackage itemPackage) {
        if (player.getMeso() + itemPackage.getMesos() < 0) {
            log.error(player.getName() + "僱傭釣魚取回道具楓幣檢測錯誤");
            return false;
        }
        short eq = 0;
        short use = 0;
        short setup = 0;
        short etc = 0;
        short cash = 0;
        short decoration = 0;
        for (Item e2 : itemPackage.getItems()) {
            MapleInventoryType m2 = ItemConstants.getInventoryType(e2.getItemId());
            if (m2 != null) {
                switch (m2) {
                    case EQUIP: {
                        eq++;
                        break;
                    }
                    case USE: {
                        use++;
                        break;
                    }
                    case SETUP: {
                        setup++;
                        break;
                    }
                    case ETC: {
                        etc++;
                        break;
                    }
                    case CASH: {
                        cash++;
                        break;
                    }
                    case DECORATION: {
                        decoration++;
                        break;
                    }
                    default:
                        return false;
                }
            }
            if (MapleItemInformationProvider.getInstance().isPickupRestricted(e2.getItemId()) && player.haveItem(e2.getItemId(), 1)) {
                log.error(player.getName() + "釣魚道具是否可以撿取錯誤");
                return false;
            }
        }
        if (player.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq || player.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use || player.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup || player.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc || player.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash || player.getInventory(MapleInventoryType.DECORATION).getNumFreeSlot() < decoration) {
            log.error(player.getName() + "僱傭釣魚取回道具背包空間不夠");
            return false;
        }
        return true;
    }

    private static boolean deletePackage(final int cid) {
        DatabaseLoader.DatabaseConnection conn = new DatabaseLoader.DatabaseConnection(true);
        Connection con = conn.getConnection();
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM hiredfisher WHERE characterid = ?")) {
            ps.setInt(1, cid);
            ps.executeUpdate();
            ItemLoader.釣魚道具.saveItems(con, null, cid);
            conn.commit();
            return true;
        } catch (SQLException e) {
            log.error("刪除僱傭釣手道具信息出錯", e);
            conn.rollback();
            return false;
        } finally {
            conn.close();
        }
    }

    private static void load(final MapleCharacter player, final MerchItemPackage pack) {
        DatabaseLoader.DatabaseConnection conn = new DatabaseLoader.DatabaseConnection(true);
        Connection con = conn.getConnection();
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM hiredfisher WHERE characterid = ?")) {
            ps.setInt(1, player.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pack.setExp(rs.getLong("exp"));
                    pack.setMesos(rs.getLong("mesos"));
                }
            }
            conn.commit();
        } catch (SQLException e) {
            log.error("獲取楓幣經驗信息出錯", e);
            conn.rollback();
        } finally {
            conn.close();
        }
    }
}