package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.ItemLoader;
import Client.inventory.MapleInventoryType;
import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.MerchItemPackage;
import Net.server.shops.HiredMerchant;
import Packet.PlayerShopPacket;
import Server.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.Pair;
import tools.data.MaplePacketReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HiredMerchantHandler {

    private static final Logger log = LoggerFactory.getLogger(HiredMerchantHandler.class);

    public static boolean UseHiredMerchant(MapleClient c, boolean packet) {
        MapleCharacter chr = c.getPlayer();
        if (c.getChannelServer().isShutdown()) {
            chr.dropMessage(1, "伺服器即將關閉維護，暫時無法進行開店。");
            return false;
        }
        if (chr.getMap() != null && chr.getMap().allowPersonalShop()) {
            HiredMerchant merchant = World.getMerchant(chr.getAccountID(), chr.getId());
            if (merchant != null) {
                c.announce(PlayerShopPacket.sendTitleBox(0x08, merchant.getMapId(), merchant.getChannel() - 1));
            } else {
                //System.out.println("是否有道具: " + ItemLoader.僱傭道具.loadItems(false, chr.getId()).isEmpty() + " 是否有楓幣: " + chr.getMerchantMeso());
                if (loadItemFrom_Database(chr) == null) {
                    if (packet) {
                        c.announce(PlayerShopPacket.sendTitleBox(0x07));
                    }
                    return true;
                } else {
                    c.announce(PlayerShopPacket.sendTitleBox(0x09));
                }
            }
        }
        return false;
    }

    public static int getMerchMesos(MapleCharacter chr) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where characterid = ?");
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
                rs.close();
                return 0;
            }
            int mesos = rs.getInt("Mesos");
            rs.close();
            ps.close();
            return mesos > 0 ? mesos : 0;
        } catch (SQLException se) {
            return 0;
        }
    }

    public static void MerchantItemStore(MaplePacketReader slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (c.getChannelServer().isShutdown()) {
            chr.dropMessage(1, "伺服器即將關閉維護，暫時無法進行道具取回。");
            c.sendEnableActions();
            return;
        }
        byte operation = slea.readByte();
        switch (operation) {
            case 0x17: //打開NPC V.115修改 以前0x15
                HiredMerchant merchant = World.getMerchant(chr.getAccountID(), chr.getId());
                if (merchant != null) {
                    c.announce(PlayerShopPacket.merchItemStore(merchant.getMapId(), merchant.getChannel() - 1));
                    chr.setConversation(0);
                } else {
                    MerchItemPackage pack = loadItemFrom_Database(chr);
                    //System.out.println("加載弗洛蘭德道具信息 pack " + pack);
                    if (pack == null) {
                        c.announce(PlayerShopPacket.merchItemStore(999999999, 0));
                        chr.setConversation(0);
                    } else {
                        c.announce(PlayerShopPacket.merchItemStore_ItemData(pack));
                    }
                }
                break;
            case 0x18: //不取回道具
                if (chr.getConversation() != 3) {
                    return;
                }
                c.announce(PlayerShopPacket.merchItemStore((byte) 0x2D));
                break;
            case 0x1F: //取回道具  V.131修改 以前0x1D
                if (chr.getConversation() != 3) {
                    return;
                }
                boolean merch = World.hasMerchant(chr.getAccountID(), chr.getId());
                if (merch) {
                    chr.dropMessage(1, "請關閉現有的商店.");
                    chr.setConversation(0);
                    return;
                }
                MerchItemPackage pack = loadItemFrom_Database(chr);
                if (pack == null) {
                    chr.dropMessage(1, "發生了未知錯誤.");
                    return;
                }
                int checkstatus = check(chr, pack);
                switch (checkstatus) {
                    case 1: //楓幣太多
                        c.announce(PlayerShopPacket.merchItem_Message((byte) 0x25));
                        return;
                    case 2: //背包欄位不足
                        c.announce(PlayerShopPacket.merchItem_Message((byte) 0x28));
                        return;
                    case 3: //道具有數量限制
                        c.announce(PlayerShopPacket.merchItem_Message((byte) 0x26));
                        return;
                }
                if (pack.getMesos() > 0 && chr.getMeso() + pack.getMesos() > ServerConfig.CHANNEL_PLAYER_MAXMESO) {
                    c.announce(PlayerShopPacket.merchItem_Message((byte) 0x25));
                    return;
                }
                if (deletePackage(chr.getId())) {
                    if (pack.getMesos() > 0) {
                        chr.gainMeso(pack.getMesos(), false);
                        log.info("[僱傭] " + chr.getName() + " 僱傭取回獲得楓幣: " + pack.getMesos() + " 時間: " + DateUtil.getCurrentDate());
                        HiredMerchant.log.info(chr.getName() + " 僱傭取回獲得楓幣: " + pack.getMesos());
                    }
                    for (Item item : pack.getItems()) {
                        MapleInventoryManipulator.addFromDrop(c, item, false);
                        HiredMerchant.log.info(chr.getName() + " 僱傭取回獲得道具: " + item.getItemId() + " - " + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + " 數量: " + item.getQuantity());
                    }
                    c.announce(PlayerShopPacket.merchItem_Message((byte) 0x24));
                } else {
                    chr.dropMessage(1, "發生了未知錯誤.");
                }
                break;
            case 0x21:  //退出 V.115修改 以前0x1D
                chr.setConversation(0);
                break;
            default:
                System.out.println("弗洛蘭德：未知的操作類型 " + operation);
                break;
        }
    }

    public static void RemoteStore(MaplePacketReader slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        HiredMerchant merchant = World.getMerchant(chr.getAccountID(), chr.getId());
        if (merchant != null) {
            if (merchant.getChannel() == chr.getClient().getChannel()) {
                merchant.setOpen(false);
                merchant.removeAllVisitors((byte) 0x14, (byte) 0);
                chr.setPlayerShop(merchant);
                c.announce(PlayerShopPacket.getHiredMerch(chr, merchant, false));
            } else {
                c.announce(PlayerShopPacket.sendTitleBox(0x10, 0, merchant.getChannel() - 1));
            }
        } else {
            chr.dropMessage(1, "你沒有開設商店");
        }
        c.sendEnableActions();
    }

    private static int check(MapleCharacter chr, MerchItemPackage pack) {
        if (chr.getMeso() + pack.getMesos() < 0) {
            log.info("[僱傭] " + chr.getName() + " 僱傭取回道具楓幣檢測錯誤 時間: " + DateUtil.getCurrentDate());
            HiredMerchant.log.error(chr.getName() + " 僱傭取回道具楓幣檢測錯誤");
            return 1;
        }
        byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0, decoration = 0;
        for (Item item : pack.getItems()) {
            MapleInventoryType invtype = ItemConstants.getInventoryType(item.getItemId());
            if (invtype == MapleInventoryType.EQUIP) {
                eq++;
            } else if (invtype == MapleInventoryType.USE) {
                use++;
            } else if (invtype == MapleInventoryType.SETUP) {
                setup++;
            } else if (invtype == MapleInventoryType.ETC) {
                etc++;
            } else if (invtype == MapleInventoryType.CASH) {
                cash++;
            } else if (invtype == MapleInventoryType.DECORATION) {
                decoration++;
            }
            if (MapleItemInformationProvider.getInstance().isPickupRestricted(item.getItemId()) && chr.haveItem(item.getItemId(), 1)) {
                log.info("[僱傭] " + chr.getName() + " 僱傭取回道具是否可以撿取錯誤 時間: " + DateUtil.getCurrentDate());
                HiredMerchant.log.error(chr.getName() + " 僱傭取回道具是否可以撿取錯誤");
                return 3;
            }
        }
        if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq || chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use || chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup || chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc || chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash || chr.getInventory(MapleInventoryType.DECORATION).getNumFreeSlot() < decoration) {
            log.info("[僱傭] " + chr.getName() + " 僱傭取回道具背包空間不夠 時間: " + DateUtil.getCurrentDate());
            HiredMerchant.log.error(chr.getName() + " 僱傭取回道具背包空間不夠");
            return 2;
        }
        return 0;
    }

    private static boolean deletePackage(int charId) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE from hiredmerch where characterid = ?");
            ps.setInt(1, charId);
            ps.executeUpdate();
            ps.close();
            ItemLoader.僱傭道具.saveItems(con, null, charId);
            return true;
        } catch (SQLException e) {
            System.out.println("刪除弗洛蘭德道具信息出錯" + e);
            return false;
        }
    }

    private static MerchItemPackage loadItemFrom_Database(MapleCharacter chr) {
        try {
            long mesos = chr.getMerchantMeso();
            Map<Long, Pair<Item, MapleInventoryType>> items = ItemLoader.僱傭道具.loadItems(false, chr.getId());
            if (mesos == 0 && items.isEmpty()) {
                //FileoutputUtil.hiredMerchLog(chr.getName(), "加載弗洛蘭德道具信息 楓幣 " + mesos + " 是否有道具 " + items.size());
                return null;
            }
            MerchItemPackage pack = new MerchItemPackage();
            pack.setMesos(mesos);
            if (!items.isEmpty()) {
                List<Item> iters = new ArrayList<>();
                for (Pair<Item, MapleInventoryType> z : items.values()) {
                    iters.add(z.left);
                }
                pack.setItems(iters);
            }
            HiredMerchant.log.error(chr.getName() + " 弗洛蘭德取回最後返回 楓幣: " + mesos + " 道具數量: " + items.size());
            return pack;
        } catch (SQLException e) {
            System.out.println("加載弗洛蘭德道具信息出錯" + e);
            return null;
        }
    }
}
