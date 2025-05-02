package Server.channel.handler;

import Client.MapleAntiMacro;
import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.ItemAttribute;
import Client.inventory.MapleInventoryType;
import Config.configs.Config;
import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Config.constants.enums.MiniRoomOptType;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.MapleTrade;
import Net.server.maps.FieldLimitType;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Net.server.shops.*;
import Opcode.Headler.OutHeader;
import Packet.MaplePacketCreator;
import Packet.PlayerShopPacket;
import Server.world.WorldBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.Pair;
import tools.StringUtil;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.util.Arrays;

public class PlayerInteractionHandler {

    private static final Logger log = LoggerFactory.getLogger(PlayerInteractionHandler.class);

    public static void PlayerInteraction(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        byte mode = slea.readByte();
        MiniRoomOptType action = MiniRoomOptType.getByAction(mode);
        if (action == null) {
            log.warn("Unknown MiniRoomOptType: 0x" + Integer.toHexString(mode).toUpperCase() + " " + slea);
            return;
        }
        if (MapleAntiMacro.isAntiNow(chr.getName())) {
            chr.dropMessage(5, "被使用測謊機時無法操作。");
            c.sendEnableActions();
            return;
        }
        chr.setScrolledPosition((short) 0);
        if (chr.isAdmin()) {
            chr.dropMessage(5, "玩家互動操作類型: " + action);
        }
        switch (action) {
            case MRP_Create: { //創建
                byte createType = slea.readByte();
                if (chr.getPlayerShop() != null || c.getChannelServer().isShutdown() || chr.hasBlockedInventory()) {
                    chr.dropMessage(1, "現在還不能進行.");
                    c.sendEnableActions();
                    return;
                }
                if (createType == 4 || createType == 7) { //交易, 7=cash， 4=normal
                    if (ServerConfig.WORLD_BANTRADE) {
                        chr.dropMessage(1, "管理員禁用了交易功能.");
                        c.sendEnableActions();
                        return;
                    }
                    MapleTrade.startTrade(chr, createType == 7); ///////////////////////////////////////////>>>>>>>>>>>>>>>>>>>>>>>>
                } else if (createType == 1 || createType == 2 || createType == 5 || createType == 6 || createType == 0x4B) { // shop
                    if (!chr.getMap().getMapObjectsInRange(chr.getPosition(), 142, Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.HIRED_MERCHANT)).isEmpty() || !chr.getMap().getPortalsInRange(chr.getPosition(), 142).isEmpty()) {
                        chr.dropMessage(1, "無法在這個地方使用.");
                        c.sendEnableActions();
                        return;
                    }
                    if (createType == 1 || createType == 2) {
                        if (FieldLimitType.MINIGAMELIMIT.check(chr.getMap().getFieldLimit()) || chr.getMap().allowPersonalShop()) {
                            chr.dropMessage(1, "無法在這個地方使用.");
                            c.sendEnableActions();
                            return;
                        }
                    }
                    String desc = slea.readMapleAsciiString();
                    String pass = "";
                    if (slea.readByte() > 0) {
                        pass = slea.readMapleAsciiString();
                    }
                    if (createType == 1 || createType == 2) {
                        slea.readShort(); //item pos
                        int piece = slea.readByte();
                        int itemId = createType == 1 ? (4080000 + piece) : 4080100;
                        if (!chr.haveItem(itemId) || (c.getPlayer().getMapId() >= 910000001 && c.getPlayer().getMapId() <= 910000022)) {
                            return;
                        }
                        MapleMiniGame game = new MapleMiniGame(chr, itemId, desc, pass, createType); //itemid
                        game.setPieceType(piece);
                        chr.setPlayerShop(game);
                        game.setAvailable(true);
                        game.setOpen(true);
                        game.send(c);
                        chr.getMap().addMapObject(game);
                        game.update();
                    } else if (chr.getMap().allowPersonalShop() || createType == 0x4B && chr.getMap().allowFishing()) {
                        Item shop = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slea.readShort());
                        if (shop == null || shop.getQuantity() <= 0 || shop.getItemId() != slea.readInt()) {
                            return;
                        }
                        if (createType == 5) { //玩家自己販賣道具
                            MaplePlayerShop mps = new MaplePlayerShop(chr, shop.getItemId(), desc);
                            chr.setPlayerShop(mps);
                            chr.getMap().addMapObject(mps);
                            c.announce(PlayerShopPacket.getPlayerStore(chr, true));
                        } else if (HiredMerchantHandler.UseHiredMerchant(chr.getClient(), false)) { //僱傭商店
                            //剩餘6個封包 前2位是 道具在背包的位置 後面四位是道具ID
                            HiredMerchant merch = new HiredMerchant(chr, shop.getItemId(), desc);
                            chr.setPlayerShop(merch);
                            chr.getMap().addMapObject(merch);
                            c.announce(PlayerShopPacket.getHiredMerch(chr, merch, true));
                        } else if (createType == 75 && HiredFisherHandler.check(c, false)) {
                            c.announce(PlayerShopPacket.FishNotice());
                            int time = shop.getItemId() >= 5601000 && shop.getItemId() <= 5601002 ? (shop.getItemId() - 5601000 + 1) * 6 : 0;
                            HiredFisher fisher = new HiredFisher(chr, shop.getItemId(), time);
                            fisher.setId(c.getChannelServer().addFisher(fisher));
                            chr.setHiredFisher(fisher);
                            if (fisher.isHiredFisherItemId()) {
                                chr.removeItem(shop.getItemId(), 1);
                            }
                            chr.getMap().addMapObject(fisher);
                            c.announce(PlayerShopPacket.spawnHiredMerchant(fisher));
                            c.announce(PlayerShopPacket.FishExit());
                            c.announce(PlayerShopPacket.updateHiredMerchant(fisher));
                            break;
                        }
                    }
                }
                break;
            }
            case MRP_Invite: {
                if (chr.getMap() == null) {
                    return;
                }
                MapleCharacter chrr = chr.getMap().getPlayerObject(slea.readInt());
                if (chrr == null || c.getChannelServer().isShutdown() || chrr.hasBlockedInventory()) {
                    c.sendEnableActions();
                    return;
                }
                MapleTrade.inviteTrade(chr, chrr);
                break;
            }
            case MRP_InviteResult: {
                MapleTrade.declineTrade(chr);
                break;
            }
            case MRP_Enter: { //訪問
                if (c.getChannelServer().isShutdown()) {
                    c.sendEnableActions();
                    return;
                }
                if (chr.getTrade() != null && chr.getTrade().getPartner() != null && !chr.getTrade().inTrade()) {
                    MapleTrade.visitTrade(chr, chr.getTrade().getPartner().getChr());
                } else if (chr.getMap() != null && chr.getTrade() == null) {
                    int obid = slea.readInt();
                    MapleMapObject ob = chr.getMap().getMapObject(obid, MapleMapObjectType.HIRED_MERCHANT);
                    if (ob == null) {
                        ob = chr.getMap().getMapObject(obid, MapleMapObjectType.SHOP);
                    }
                    if (ob instanceof IMaplePlayerShop ips && chr.getPlayerShop() == null) {
                        if (ob instanceof HiredMerchant) {
                            HiredMerchant merchant = (HiredMerchant) ips;
                            if (merchant.isOwner(chr) && merchant.isOpen() && merchant.isAvailable()) {
                                merchant.setOpen(false);
                                merchant.removeAllVisitors(0x14, (byte) 1);
                                chr.setPlayerShop(ips);
                                c.announce(PlayerShopPacket.getHiredMerch(chr, merchant, false));
                            } else {
                                if (!merchant.isOpen() || !merchant.isAvailable()) {
                                    chr.dropMessage(1, "主人正在整理商店物品\r\n請稍後再度光臨！");
                                } else {
                                    if (ips.getFreeSlot() == -1) {
                                        chr.dropMessage(1, "店舖已達到最大人數\r\n請稍後再度光臨！");
                                    } else if (merchant.isInBlackList(chr.getName())) {
                                        chr.dropMessage(1, "你被禁止進入該店舖");
                                    } else {
                                        chr.setPlayerShop(ips);
                                        merchant.addVisitor(chr);
                                        c.announce(PlayerShopPacket.getHiredMerch(chr, merchant, false));
                                    }
                                }
                            }
                        } else {
                            if (ips instanceof MaplePlayerShop && ((MaplePlayerShop) ips).isBanned(chr.getName())) {
                                c.announce(PlayerShopPacket.shopErrorMessage(true, 0x11, 0));
                            } else {
                                if (ips.getFreeSlot() < 0 || ips.getVisitorSlot(chr) > -1 || !ips.isOpen() || !ips.isAvailable()) {
                                    c.announce(PlayerShopPacket.getMiniGameFull());
                                } else {
                                    if (slea.available() > 0 && slea.readByte() > 0) { //a password has been entered
                                        String pass = slea.readMapleAsciiString();
                                        if (!pass.equals(ips.getPassword())) {
                                            c.getPlayer().dropMessage(1, "你輸入的密碼不正確.");
                                            return;
                                        }
                                    } else if (ips.getPassword().length() > 0) {
                                        c.getPlayer().dropMessage(1, "你輸入的密碼不正確.");
                                        return;
                                    }
                                    chr.setPlayerShop(ips);
                                    ips.addVisitor(chr);
                                    if (ips instanceof MapleMiniGame) {
                                        ((MapleMiniGame) ips).send(c);
                                    } else {
                                        c.announce(PlayerShopPacket.getPlayerStore(chr, false));
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
            case MRP_Chat: {
                chr.updateTick(slea.readInt());
                String message = slea.readMapleAsciiString();
                if (chr.getTrade() != null) {
                    chr.getTrade().chat(message);
                } else if (chr.getPlayerShop() != null) {
                    IMaplePlayerShop ips = chr.getPlayerShop();
                    ips.broadcastToVisitors(PlayerShopPacket.playerInterChat(chr.getName() + " : " + message, ips.getVisitorSlot(chr), chr.getName()));
                    if (ips.getShopType() == IMaplePlayerShop.HIRED_MERCHANT) {
                        ips.getMessages().add(new Pair<>(chr.getName() + " : " + message, ips.getVisitorSlot(chr)));
                    }
                    if (chr.getClient().isMonitored()) { //Broadcast info even if it was a command.
                        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, chr.getName() + " said in " + ips.getOwnerName() + " shop : " + message));
                    }
                }
                break;
            }
            case MRP_Leave: {
                if (chr.getTrade() != null) {
                    MapleTrade.cancelTrade(chr.getTrade(), chr.getClient(), chr);
                } else {
                    IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips == null) { //should be null anyway for owners of hired merchants (maintenance_off)
                        return;
                    }
                    if (ips.isOwner(chr) && ips.getShopType() != 1) {
                        ips.closeShop(false, ips.isAvailable()); //how to return the items?
                    } else {
                        ips.removeVisitor(chr);
                    }
                    chr.setPlayerShop(null);
                }
                break;
            }
            case MRP_Balloon: {
                // c.getPlayer().haveItem(mode, 1, false, true)
                IMaplePlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop.isOwner(chr) && shop.getShopType() < 3 && !shop.isAvailable()) {
                    if (chr.getMap().allowPersonalShop()) {
                        if (c.getChannelServer().isShutdown()) {
                            chr.dropMessage(1, "伺服器即將關閉維護，暫時無法進行此操作。.");
                            c.sendEnableActions();
                            shop.closeShop(shop.getShopType() == IMaplePlayerShop.HIRED_MERCHANT, false);
                            return;
                        }
                        if (shop.getShopType() == IMaplePlayerShop.HIRED_MERCHANT && HiredMerchantHandler.UseHiredMerchant(chr.getClient(), false)) {
                            HiredMerchant merchant = (HiredMerchant) shop;
                            merchant.setStoreid(c.getChannelServer().addMerchant(merchant));
                            merchant.setOpen(true);
                            merchant.setAvailable(true);
                            shop.saveItems();
                            chr.getMap().broadcastMessage(PlayerShopPacket.spawnHiredMerchant(merchant));
                            chr.setPlayerShop(null);
                        } else if (shop.getShopType() == 2) {
                            shop.setOpen(true);
                            shop.setAvailable(true);
                            shop.update();
                        }
                    } else {
                        chr.getClient().disconnect(true, false);
                        c.getSession().close();
                    }
                }
                break;
            }
            case TRP_PutItem:
            case TRP_PutItem_2:
            case TRP_PutItem_3:
            case TRP_PutItem_MAX: {
//                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
                Item item = chr.getInventory(ivType).getItem(slea.readShort());
                short quantity = slea.readShort();
                byte targetSlot = slea.readByte();
                if (chr.getTrade() != null && item != null) {
                    /*
                     * 設置國慶紀念幣無法進行交易
                     */
                    boolean canTrade = true;
                    if (item.getItemId() == 4000463 && !canTrade) {
                        chr.dropMessage(1, "該道具無法進行交易.");
                        c.sendEnableActions();
                    } else if ((quantity <= item.getQuantity() && quantity >= 0) || ItemConstants.類型.飛鏢(item.getItemId()) || ItemConstants.類型.子彈(item.getItemId())) {
                        chr.getTrade().setItems(c, item, targetSlot, quantity);
                    }
                }
                break;
            }
            case TRP_PutMoney:
            case TRP_PutMoney_2:
            case TRP_PutMoney_3:
            case TRP_PutMoney_MAX: {
                MapleTrade trade = chr.getTrade();
                if (trade != null) {
                    trade.setMeso(slea.readInt());
                }
                break;
            }
            case TRP_Trade:
            case TRP_Trade_2:
            case TRP_Trade_3:
            case TRP_Trade_MAX: {
                if (chr.getTrade() != null) {
                    MapleTrade.completeTrade(chr);
                    break;
                }
                break;
            }
            case PSP_PutItem:
            case PSP_PutItem_2:
            case PSP_PutItem_3:
            case PSP_PutItem_MAX:
            case ESP_PutItem: //0x1F
            case ESP_PutItem_2:
            case ESP_PutItem_3:
            case ESP_PutItem_MAX: {
                /*
                 * D4 00
                 * 15
                 * 02 - type
                 * 02 00 - 位置
                 * 01 00
                 * 01 00
                 * 10 27 00 00 - 販賣的價格
                 */
                MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
                short slot = slea.readShort();
                short bundles = slea.readShort(); // How many in a bundle
                short perBundle = slea.readShort(); // Price per bundle
                long price = slea.readLong();
                if (price <= 0 || bundles <= 0 || perBundle <= 0) {
                    chr.dropMessage(1, "添加物品出現錯誤(1)");
                    c.sendEnableActions();
                    return;
                }
                IMaplePlayerShop shop = chr.getPlayerShop();
                if (shop == null || !shop.isOwner(chr) || shop instanceof MapleMiniGame) {
                    return;
                }
                Item ivItem = chr.getInventory(type).getItem(slot);
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (ivItem != null) {
                    long check = bundles * perBundle;
                    if (check > 32767 || check <= 0) { //This is the better way to check.
                        return;
                    }
                    short bundles_perbundle = (short) (bundles * perBundle);
                    if (ivItem.getQuantity() >= bundles_perbundle) {
                        int flag = ivItem.getCAttribute();
                        if (ItemAttribute.Seal.check(flag)) {
                            c.sendEnableActions();
                            return;
                        }
                        if (ItemAttribute.TradeBlock.check(flag) && !ItemAttribute.TradeOnce.check(flag)) {
                            c.sendEnableActions();
                            return;
                        }
                        if (ItemAttribute.AccountSharable.check(flag)) {
                            c.sendEnableActions();
                            return;
                        }
                        if (ii.isDropRestricted(ivItem.getItemId()) || ii.isAccountShared(ivItem.getItemId())) {
                            if (!(ItemAttribute.TradeOnce.check(flag) || ItemAttribute.CutUsed.check(flag))) {
                                c.sendEnableActions();
                                return;
                            }
                        }
                        /*
                         * 設置國慶紀念幣無法進行販賣
                         */
                        if (ivItem.getItemId() == 4000463) {
                            chr.dropMessage(1, "該道具無法進行販賣.");
                            c.sendEnableActions();
                            return;
                        }
                        if (bundles_perbundle >= 50 && ivItem.getItemId() == 2340000) {
                            c.setMonitored(true); //hack check
                        }
                        if (ItemConstants.getLowestPrice(ivItem.getItemId()) > price) {
                            c.getPlayer().dropMessage(1, "The lowest you can sell this for is " + ItemConstants.getLowestPrice(ivItem.getItemId()));
                            c.sendEnableActions();
                            return;
                        }
                        if (ItemConstants.類型.飛鏢(ivItem.getItemId()) || ItemConstants.類型.子彈(ivItem.getItemId())) {
                            MapleInventoryManipulator.removeFromSlot(c, type, slot, ivItem.getQuantity(), true);
                            Item sellItem = ivItem.copy();
                            shop.addItem(new MaplePlayerShopItem(sellItem, (short) 1, price));
                        } else {
                            MapleInventoryManipulator.removeFromSlot(c, type, slot, bundles_perbundle, true);
                            Item sellItem = ivItem.copy();
                            sellItem.setQuantity(perBundle);
                            shop.addItem(new MaplePlayerShopItem(sellItem, bundles, price));
                        }
                        c.announce(PlayerShopPacket.shopItemUpdate(shop));
                    } else {
                        chr.dropMessage(1, "添加物品的數量錯誤。如果是飛鏢，子彈之類請充了後在進行販賣。");
                    }
                }
                break;
            }
            case PSP_BuyItem:
            case PSP_BuyItem_2:
            case PSP_BuyItem_3:
            case PSP_BuyItem_MAX:
            case ESP_BuyItem: //0x23
            case ESP_BuyItem_2:
            case ESP_BuyItem_3:
            case ESP_BuyItem_MAX: {
                //[CD 00] [16] [00] [01 00] [41 62 E3 B1]
                int item = slea.readByte();
                short quantity = slea.readShort(); //數量
                //slea.skip(4);
                IMaplePlayerShop shop = chr.getPlayerShop();
                if (shop == null || shop.isOwner(chr) || shop instanceof MapleMiniGame || item >= shop.getItems().size()) {
                    c.announce(PlayerShopPacket.Merchant_Buy_Error((byte) 0x0D));
                    c.sendEnableActions();
                    return;
                }
                MaplePlayerShopItem tobuy = shop.getItems().get(item);
                if (tobuy == null) {
                    c.announce(PlayerShopPacket.Merchant_Buy_Error((byte) 0x0A));
                    c.sendEnableActions();
                    return;
                }
                long check = tobuy.bundles * quantity;
                long check2 = tobuy.price * quantity; //價格
                long check3 = tobuy.item.getQuantity() * quantity; //數量
                if (check <= 0 || check2 > ServerConfig.CHANNEL_PLAYER_MAXMESO || check2 <= 0 || check3 > 32767 || check3 < 0) { //This is the better way to check.
                    c.announce(PlayerShopPacket.Merchant_Buy_Error((byte) 0x0D));
                    c.sendEnableActions();
                    return;
                }
                if (chr.getMeso() - (check2) < 0) {
                    c.announce(PlayerShopPacket.Merchant_Buy_Error((byte) 0x02));
                    c.sendEnableActions();
                    return;
                }
                if (tobuy.bundles < quantity || (tobuy.bundles % quantity != 0 && ItemConstants.類型.裝備(tobuy.item.getItemId())) // Buying
                        || chr.getMeso() - (check2) > ServerConfig.CHANNEL_PLAYER_MAXMESO || shop.getMeso() + (check2) < 0 || shop.getMeso() + (check2) > ServerConfig.CHANNEL_PLAYER_MAXMESO) {
                    c.announce(PlayerShopPacket.Merchant_Buy_Error((byte) 0x04));
                    c.sendEnableActions();
                    return;
                }
                if (quantity >= 50 && tobuy.item.getItemId() == 2340000) {
                    c.setMonitored(true); //hack check
                }
                shop.buy(c, item, quantity);
                shop.broadcastToVisitors(PlayerShopPacket.shopItemUpdate(shop));
                break;
            }
            case ESP_PutPurchaseItem: {
                /*
                 * [D4 00] [17]
                 * F5 95 1F 00 - 求購道具的ID
                 * 01 00 -
                 * 01 00
                 * E8 03 00 00 - 求購道具的價格
                 */
                chr.dropMessage(1, "當前伺服器暫不支持求購道具.");
                break;
            }
            case MRP_CheckSSN2: {
                byte acType = slea.readByte(); //未知
                byte type = slea.readByte(); //模式
                if (type == 6) {
                    slea.skip(3); //未知 01 00 20
                    int obid = slea.readInt();
                    MapleMapObject ob = chr.getMap().getMapObject(obid, MapleMapObjectType.HIRED_MERCHANT);
                    if (ob instanceof IMaplePlayerShop ips && chr.getPlayerShop() == null) {
                        if (ob instanceof HiredMerchant) {
                            HiredMerchant merchant = (HiredMerchant) ips;
                            if (merchant.isOwner(chr) && merchant.isOpen() && merchant.isAvailable()) {
                                merchant.setOpen(false);
                                merchant.removeAllVisitors(0x14, (byte) 1);
                                chr.setPlayerShop(ips);
                                c.announce(PlayerShopPacket.getHiredMerch(chr, merchant, false));
                            } else {
                                if (!merchant.isOpen() || !merchant.isAvailable()) {
                                    chr.dropMessage(1, "主人正在整理商店物品\r\n請稍後再度光臨！");
                                } else if (ips.getFreeSlot() == -1) {
                                    chr.dropMessage(1, "店舖已達到最大人數\r\n請稍後再度光臨！");
                                } else if (merchant.isInBlackList(chr.getName())) {
                                    chr.dropMessage(1, "你被禁止進入該店舖");
                                }
                            }
                        }
                    }
                } else if (type == 7) {
                    String secondPwd = slea.readMapleAsciiString();
                    int cid = slea.readInt();
                    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                    mplew.writeShort(OutHeader.LP_TradeRoom.getValue());
                    mplew.write(action.getValue());
                    mplew.write(acType);
                    mplew.write(type);
                    mplew.write(c.CheckSecondPassword(secondPwd));
                    mplew.writeInt(cid);
                    mplew.write(0);
                    c.announce(mplew.getPacket());
                    c.sendEnableActions();
                } else {
                    c.sendEnableActions();
                }
                break;
            }
            case ESP_MoveItemToInventory: {
                slea.skip(1);
                int slot = slea.readShort();
                IMaplePlayerShop shop = chr.getPlayerShop();
                if (chr.isAdmin()) {
                    chr.dropMessage(5, "移除商店道具: 道具數量 " + shop.getItems().size() + " slot " + slot);
                }
                if (shop == null || !shop.isOwner(chr) || shop instanceof MapleMiniGame || shop.getItems().size() <= 0 || shop.getItems().size() <= slot || slot < 0) {
                    return;
                }
                MaplePlayerShopItem item = shop.getItems().get(slot);
                if (item != null) {
                    if (item.bundles > 0) {
                        Item item_get = item.item.copy();
                        long check = item.bundles * item.item.getQuantity();
                        if (check < 0 || check > 32767) {
                            if (chr.isAdmin()) {
                                chr.dropMessage(5, "移除商店道具出錯: check " + check);
                            }
                            return;
                        }
                        item_get.setQuantity((short) check);
                        if (item_get.getQuantity() >= 50 && item.item.getItemId() == 2340000) {
                            c.setMonitored(true); //hack check
                        }
                        if (MapleInventoryManipulator.checkSpace(c, item_get.getItemId(), item_get.getQuantity(), item_get.getOwner())) {
                            MapleInventoryManipulator.addFromDrop(c, item_get, false);
                            item.bundles = 0;
                            shop.removeFromSlot(slot);
                        }
                    }
                }
                c.announce(PlayerShopPacket.shopItemUpdate(shop));
                break;
            }
            case ESP_GoOut: //開啟僱傭商店
            case PSP_RegisterBlackList: { //維護僱傭商店後在開啟
                IMaplePlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop instanceof HiredMerchant && shop.isOwner(chr) && shop.isAvailable()) {
                    shop.setOpen(true);
                    shop.saveItems();
                    shop.getMessages().clear();
                    shop.removeAllVisitors(-1, -1);
                    chr.setPlayerShop(null);
                }
                c.sendEnableActions();
                break;
            }
            case ESP_ArrangeItem: {
                IMaplePlayerShop imps = chr.getPlayerShop();
                if (imps != null && imps.isOwner(chr) && !(imps instanceof MapleMiniGame)) {
                    for (int i = 0; i < imps.getItems().size(); i++) {
                        if (imps.getItems().get(i).bundles == 0) {
                            imps.getItems().remove(i);
                        }
                    }
                    if (chr.getMeso() + imps.getMeso() > 0) {
                        chr.gainMeso(imps.getMeso(), false);
                        HiredMerchant.log.info(chr.getName() + " 僱傭整理獲得楓幣: " + imps.getMeso() + " 時間: " + DateUtil.getCurrentDate());
                        imps.setMeso(0);
                    }
                    c.announce(PlayerShopPacket.shopItemUpdate(imps));
                }
                break;
            }
            case ESP_WithdrawAll: {
                IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == IMaplePlayerShop.HIRED_MERCHANT && merchant.isOwner(chr)) {
                    c.announce(PlayerShopPacket.hiredMerchantOwnerLeave());
                    merchant.removeAllVisitors(-1, -1);
                    chr.setPlayerShop(null);
                    merchant.closeShop(true, true);
                } else {
                    chr.dropMessage(1, "關閉商店出現未知錯誤.");
                    c.sendEnableActions();
                }
                break;
            }
            case ESP_AdminChangeTitle: { // Changing store name, only Admin
                // slea.readInt(); 要修改的僱傭商店的角色所有者ID
                chr.dropMessage(1, "暫不支持管理員修改僱傭商店的名字.");
                c.sendEnableActions();
                break;
            }
            case ESP_DeliverVisitList: {
                IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == IMaplePlayerShop.HIRED_MERCHANT && merchant.isOwner(chr)) {
                    ((HiredMerchant) merchant).sendVisitor(c);
                }
                break;
            }
            case ESP_DeliverBlackList: {
                IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == IMaplePlayerShop.HIRED_MERCHANT && merchant.isOwner(chr)) {
                    ((HiredMerchant) merchant).sendBlackList(c);
                }
                break;
            }
            case ESP_AddBlackList: {
                IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == IMaplePlayerShop.HIRED_MERCHANT && merchant.isOwner(chr)) {
                    ((HiredMerchant) merchant).addBlackList(slea.readMapleAsciiString());
                }
                break;
            }
            case ESP_DeleteBlackList: {
                IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == IMaplePlayerShop.HIRED_MERCHANT && merchant.isOwner(chr)) {
                    ((HiredMerchant) merchant).removeBlackList(slea.readMapleAsciiString());
                }
                break;
            }
            case ESP_SetTitle: {
                IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == IMaplePlayerShop.HIRED_MERCHANT && merchant.isOwner(chr)) {
                    String desc = slea.readMapleAsciiString();
                    if (((HiredMerchant) merchant).canChangeName()) {
                        merchant.setDescription(desc);
                    } else {
                        c.announce(MaplePacketCreator.craftMessage("還不能變更名稱，還需要等待" + ((HiredMerchant) merchant).getChangeNameTimeLeft() + "秒。"));
                    }
                }
                break;
            }
            case PSP_Ban: {
                IMaplePlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop.getShopType() == IMaplePlayerShop.PLAYER_SHOP) {
                    slea.skip(1);
                    String name = slea.readMapleAsciiString();
                    ((MaplePlayerShop) shop).banPlayer(name);
                }
                break;
            }
//            case GIVE_UP: {
//                IMaplePlayerShop ips = chr.getPlayerShop();
//                if (ips != null && ips instanceof MapleMiniGame) {
//                    MapleMiniGame game = (MapleMiniGame) ips;
//                    if (game.isOpen()) {
//                        break;
//                    }
//                    game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 0, game.getVisitorSlot(chr)));
//                    game.nextLoser();
//                    game.setOpen(true);
//                    game.update();
//                    game.checkExitAfterGame();
//                }
//                break;
//            }
            case MGRP_Ban: {
                IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame) {
                    if (!ips.isOpen()) {
                        break;
                    }
                    ips.removeAllVisitors(3, 1);
                }
                break;
            }
            case MGRP_CancelReady:
            case MGRP_Ready: {
                IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (!game.isOwner(chr) && game.isOpen()) {
                        game.setReady(game.getVisitorSlot(chr));
                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameReady(game.isReady(game.getVisitorSlot(chr))));
                    }
                }
                break;
            }
            case MGRP_Start: {
                IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (game.isOwner(chr) && game.isOpen()) {
                        for (int i = 1; i < ips.getSize(); i++) {
                            if (!game.isReady(i)) {
                                return;
                            }
                        }
                        game.setGameType();
                        game.shuffleList();
                        if (game.getGameType() == 1) {
                            game.broadcastToVisitors(PlayerShopPacket.getMiniGameStart(game.getLoser()));
                        } else {
                            game.broadcastToVisitors(PlayerShopPacket.getMatchCardStart(game, game.getLoser()));
                        }
                        game.setOpen(false);
                        game.update();
                    }
                }
                break;
            }
            case MGRP_TieRequest: {
                IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (game.isOpen()) {
                        break;
                    }
                    if (game.isOwner(chr)) {
                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameRequestTie(), false);
                    } else {
                        game.getMCOwner().getClient().announce(PlayerShopPacket.getMiniGameRequestTie());
                    }
                    game.setRequestedTie(game.getVisitorSlot(chr));
                }
                break;
            }
            case MGRP_TieResult: {
                IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (game.isOpen()) {
                        break;
                    }
                    if (game.getRequestedTie() > -1 && game.getRequestedTie() != game.getVisitorSlot(chr)) {
                        if (slea.readByte() > 0) {
                            game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 1, game.getRequestedTie()));
                            game.nextLoser();
                            game.setOpen(true);
                            game.update();
                            game.checkExitAfterGame();
                        } else {
                            game.broadcastToVisitors(PlayerShopPacket.getMiniGameDenyTie());
                        }
                        game.setRequestedTie(-1);
                    }
                }
                break;
            }
            case MGRP_GiveUpRequest: {
                final IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (game.isOpen()) {
                        break;
                    }
                    game.resetLastCharacter();
                    game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 0, game.getVisitorSlot(chr)));
                    game.nextLoser();
                    game.setOpen(true);
                    game.update();
                    game.checkExitAfterGame();
                }
                break;
            }
            case MGRP_GiveUpResult: {
                break;
            }
            case MGRP_RetreatRequest: {
                final IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (game.isOpen()) {
                        break;
                    }
                    if (game.isOwner(chr)) {
                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameRequestRetreat(), false);
                    } else {
                        game.getMCOwner().getClient().announce(PlayerShopPacket.getMiniGameRequestRetreat());
                    }
                    game.setRequestedTie(game.getVisitorSlot(chr));
                }
                break;
            }
            case MGRP_RetreatResult: {
                final IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (game.isOpen()) {
                        break;
                    }
                    if (slea.readByte() > 0) {
                        ips.broadcastToVisitors(PlayerShopPacket.getMiniGameSkip(ips.getVisitorSlot(chr)));
                        game.resetLastCharacter();
                        game.nextLoser();
                    } else {
                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameDenyRetreat());
                    }
                    game.setRequestedTie(-1);
                }
                break;
            }
            case MGRP_TimeOver: {
                IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (game.isOpen()) {
                        break;
                    }
//                    if (game.getLoser() != ips.getVisitorSlot(chr)) {
//                        ips.broadcastToVisitors(PlayerShopPacket.playerInterChat("Turn could not be skipped by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + ips.getVisitorSlot(chr), ips.getVisitorSlot(chr), chr.getName()));
//                        return;
//                    }
                    ips.broadcastToVisitors(PlayerShopPacket.getMiniGameSkip(ips.getVisitorSlot(chr)));
                    game.nextLoser();
                }
                break;
            }
            case ORP_PutStoneChecker: {
                IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (game.isOpen()) {
                        break;
                    }
//                    if (game.getLoser() != game.getVisitorSlot(chr)) {
//                        game.broadcastToVisitors(PlayerShopPacket.playerInterChat("Omok could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr), game.getVisitorSlot(chr), chr.getName()));
//                        return;
//                    }
                    game.setPiece(slea.readInt(), slea.readInt(), slea.readByte(), chr);
                }
                break;
            }
            case MGP_TurnUpCard: {
                IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (game.isOpen()) {
                        break;
                    }
//                    if (game.getLoser() != game.getVisitorSlot(chr)) {
//                        game.broadcastToVisitors(PlayerShopPacket.playerInterChat("Card could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr), game.getVisitorSlot(chr), chr.getName()));
//                        return;
//                    }
                    if (slea.readByte() != game.getTurn()) {
                        game.broadcastToVisitors(PlayerShopPacket.playerInterChat("Omok could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr) + " Turn: " + game.getTurn(), game.getVisitorSlot(chr), chr.getName()));
                        return;
                    }
                    int slot = slea.readByte() & 0xFF;
                    int turn = game.getTurn();
                    int fs = game.getFirstSlot();
                    if (turn == 1) {
                        game.setFirstSlot(slot);
                        if (game.isOwner(chr)) {
                            game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, turn), false);
                        } else {
                            game.getMCOwner().getClient().announce(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, turn));
                        }
                        game.setTurn(0); //2nd turn nao
                        return;
                    } else if (game.getCardId(fs + 1) == game.getCardId(slot + 1)) {
                        game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, game.isOwner(chr) ? 2 : 3));
                        game.setPoints(game.getVisitorSlot(chr)); //correct.. so still same loser. diff turn tho
                    } else {
                        game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, game.isOwner(chr) ? 0 : 1));
                        game.nextLoser();//wrong haha
                    }
                    game.setTurn(1);
                    game.setFirstSlot(0);
                }
                break;
            }
            case MGRP_LeaveEngage:
            case MGRP_LeaveEngageCancel: {
                IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame game) {
                    if (game.isOpen()) {
                        break;
                    }
                    game.setExitAfter(chr);
                    game.broadcastToVisitors(PlayerShopPacket.getMiniGameExitAfter(game.isExitAfter(chr)));
                }
                break;
            }
            default: {
                if (Config.isDevelop()) {
                    log.warn("玩家互動未知的操作類型: 0x" + StringUtil.getLeftPaddedStr(Integer.toHexString(mode).toUpperCase(), '0', 2) + " " + slea);
                }
                c.sendEnableActions();
                break;
            }
        }
    }
}