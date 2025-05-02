package Server.auction;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.ItemAttribute;
import Client.inventory.MapleInventoryType;
import Config.constants.ItemConstants;
import Config.constants.ServerConstants;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Opcode.Headler.OutHeader;
import Packet.AuctionPacket;
import Packet.ChatPacket;
import Packet.MaplePacketCreator;
import Packet.PacketHelper;
import Server.channel.handler.InterServerHandler;
import Server.world.CharacterTransfer;
import SwordieX.client.party.PartyMember;
import SwordieX.client.party.PartyResult;
import connection.packet.Login;
import connection.packet.WvsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static Server.auction.AuctionItemState.ONSALE;
import static Server.auction.AuctionItemState.TERMINATE;
import static Server.auction.AuctionOptType.*;

public class AuctionHandler {
    private static final Logger log = LoggerFactory.getLogger("Auction");

    public static void AuctionRequest(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null) return;
        int opt = slea.readInt();
        AuctionServer as = AuctionServer.getInstance();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        switch (opt) {
            case INIT: {
                c.announce(AuctionPacket.loadStore(as.getAllNotOnsaleItemByPlayerId(player.getId())));
                c.announce(AuctionPacket.loadSell(as.getAllOnsaleItemByPlayerId(player.getId())));
                c.announce(AuctionPacket.loadCollection(Collections.emptyList()));
                c.announce(AuctionPacket.auctionResult(opt, 0));
                c.announce(AuctionPacket.ActionCheck(c));

                List<List<AuctionItem>> quotationLists = splitList(as.findBuyerItemByLevel(0, 255), 100);
                quotationLists.stream().findFirst().ifPresent(list -> c.announce(AuctionPacket.loadQuotation(list)));

                c.announce(AuctionPacket.auctionResult(AuctionOptType.LOAD_QUOTATION, 0));
                final List<List<AuctionItem>> itemLists = splitList(as.findItem("", 0, 9999999, 0, 255, -1), 100);
                itemLists.stream().findFirst().ifPresent(list -> c.announce(AuctionPacket.loadAllItem(list)));
                if (!itemLists.isEmpty()) {
                    c.announce(AuctionPacket.auctionResult(AuctionOptType.LOAD_ALL_ITEM, 0));
                    itemLists.forEach(list2 -> c.announce(AuctionPacket.loadAllItem(list2)));
                }
                break;
            }
            //    case EXIT: {
            //        InterServerHandler.ExitAuction(c, player);
            //        break;
            //    }
            case SET_ITEM: {
                slea.readInt();
                final int invid = slea.readInt();
                final int count = slea.readInt();
                final long price = slea.readLong();
                final int hours = slea.readInt();
                final byte itemType = slea.readByte();
                final int slot = slea.readInt();
//                final int type = slea.readInt();
                log.info("[上架物品] 玩家:" + c.getPlayer().getName() + " 道具:" + invid + " 數量:" + count + " 價格:" + price + " 時間:" + hours + "小時");
                final Item item = player.getInventory(MapleInventoryType.getByType(itemType)).getItem((short) slot);
                if (count < 0 || count > Short.MAX_VALUE || item == null || item.getItemId() != invid || item.getQuantity() < count || !(hours == 24 || hours == 48) || (!player.isSilverMvp() && hours > 24)) {
                    c.announce(AuctionPacket.auctionResult(opt, 1));
                    return;
                }
                if ((ItemAttribute.TradeBlock.check(item.getCAttribute()) || ii.isTradeBlock(item.getItemId())) && !ItemAttribute.TradeOnce.check(item.getCAttribute()) && !ItemAttribute.CutUsed.check(item.getAttribute())) {
                    c.announce(AuctionPacket.auctionResult(opt, 119));
                    return;
                }
                if (ItemAttribute.AccountSharable.check(item.getCAttribute())) {
                    c.announce(AuctionPacket.auctionResult(opt, 119));
                    return;
                }
                if (player.getMeso() < 2000L) {
                    c.announce(AuctionPacket.auctionResult(opt, 106));
                    return;
                }
                int maxSlot = 10;
                if (player.isSilverMvp()) {
                    maxSlot = 30;
                }
                if (as.getItemCountByPlayerId(player.getId()) >= maxSlot) {
                    c.announce(AuctionPacket.auctionResult(opt, 103));
                    return;
                }
                final Item item1;
                if ((item1 = MapleInventoryManipulator.removeFromSlotCopy(c, MapleInventoryType.getByType(itemType), (short) slot, (short) count)) != null) {
                    final AuctionItem auctionItem = new AuctionItem();
                    auctionItem.accounts_id = player.getAccountID();
                    auctionItem.characters_id = player.getId();
                    auctionItem.owner = player.getName();
                    auctionItem.state = 0;
//                    auctionItem.type = type;
                    auctionItem.itemid = item1.getItemId();
                    auctionItem.price = price;
                    auctionItem.startdate = System.currentTimeMillis();
                    auctionItem.expiredate = System.currentTimeMillis() + hours * 60 * 60 * 1000L;
                    auctionItem.donedate = 0L;
                    if (ItemConstants.類型.可充值道具(item1.getItemId())) {
                        auctionItem.number = 1;
                    } else {
                        auctionItem.number = item1.getQuantity();
                    }
                    auctionItem.item = item1;
                    as.lock.lock();
                    try {
                        auctionItem.id = AuctionServer.runningSNID.getAndIncrement();
                        as.changeAuctionItemWorld(auctionItem);
                        as.items.put(auctionItem.id, auctionItem);
                    } finally {
                        as.lock.unlock();
                    }
                    if (item.getPet() != null && item.getPet().getSummoned()) {
                        player.unequipSpawnPet(item.getPet(), true, (byte) 0);
                    }
                    player.gainMeso(-2000L, false);
                    c.announce(AuctionPacket.updateAuctionItemInfo(UPDATE_STATE, auctionItem));
                    c.announce(AuctionPacket.auctionResult(opt, 0));
                    c.announce(AuctionPacket.characterModifiedEX(player, -1L));
//                    c.announce(AuctionPacket.loadSell(as.getAllOnsaleItemByPlayerId(player.getId())));
                    item1.setGMLog(player + " " + item1.getName() + "道具放入拍賣！");
                    player.saveToDB(false, true);
                    return;
                }
                c.announce(AuctionPacket.auctionResult(opt, 1));
                break;
            }
            case RESET_ITEM: {
                final long sn = slea.readInt();
                slea.readInt();
                slea.readInt();
                slea.readInt();
                slea.readInt();
                final int itemId = slea.readInt();
                slea.readInt();
                slea.readLong();
                slea.readLong();
                slea.readLong();
                slea.readInt();
                slea.readInt();
//                final int type = slea.readInt();
                final AuctionItem item = as.getItemBySN(sn);
                if (player.getMeso() < 2000L) {
                    c.announce(AuctionPacket.auctionResult(opt, 106));
                    return;
                }
                if (item != null && item.itemid == itemId /*&& item.type == type*/ && item.state == TERMINATE) {
                    log.info("[重新上架物品] 玩家:" + c.getPlayer().getName() + " 道具:" + item.itemid + " 數量:" + item.number + " 價格:" + item.price);
                    player.gainMeso(-2000L, false);
                    item.state = ONSALE;
                    item.startdate = System.currentTimeMillis();
                    item.expiredate = System.currentTimeMillis() + 86400000L;
                    item.donedate = 0L;
                    AuctionServer.getInstance().updateAuctionItem(item);
                    c.announce(AuctionPacket.auctionResult(opt, 0));
                    c.announce(AuctionPacket.loadSell(as.getAllOnsaleItemByPlayerId(player.getId())));
                    c.announce(AuctionPacket.loadStore(as.getAllNotOnsaleItemByPlayerId(player.getId())));
                    return;
                }
                break;
            }
            case BUY_ITEM: {
                final long n3 = slea.readInt();
                slea.readLong();
                if (as.getAllNotOnsaleItemCountByPlayerId(player.getId()) >= 10) {
                    c.announce(AuctionPacket.auctionResult(opt, 103));
                    return;
                }
                as.buy(player, opt, n3, 1);
                c.announce(AuctionPacket.characterModifiedEX(player, -1L));
                final List<List<AuctionItem>> c1205;
                if ((c1205 = splitList(as.findItem("", 0, 9999999, 0, 255, -1), 98)) != null/* && !c1205.isEmpty()*/) {
                    c.announce(AuctionPacket.auctionResult(AuctionOptType.LOAD_ALL_ITEM, 0));
                    c1205.forEach(list3 -> c.announce(AuctionPacket.loadAllItem(list3)));
                    return;
                }
                break;
            }
            case BUY_ITEM_BUNDLE: {
                final long n4 = slea.readInt();
                slea.readLong();
                final int int8 = slea.readInt();
//                slea.readInt();
                as.buy(player, opt, n4, int8);
                c.announce(AuctionPacket.characterModifiedEX(player, -1L));
                final List<List<AuctionItem>> c1206;
                if ((c1206 = splitList(as.findItem("", 0, 9999999, 0, 255, -1), 98)) != null/* && !c1206.isEmpty()*/) {
                    c.announce(AuctionPacket.auctionResult(AuctionOptType.LOAD_ALL_ITEM, 0));
                    c1206.forEach(list4 -> c.announce(AuctionPacket.loadAllItem(list4)));
                    return;
                }
                break;
            }
            case LOAD_ALL_ITEM: {
                final boolean b1298;
                if (b1298 = (slea.readByte() > 0)) {
                    break;
                }
                final int int9 = slea.readInt();
                final String an = slea.readMapleAsciiString();
                slea.readMapleAsciiString();
                final int int10 = slea.readInt();
                final int int11 = slea.readInt();
                final int int12 = slea.readInt();
                final int int13 = slea.readInt();
                slea.readLong();
                slea.readLong();
                final int int14 = slea.readInt();
                slea.readByte();
                for (int int15 = slea.readInt(), i = 0; i < int15; ++i) {
                    slea.readInt();
                    slea.readInt();
                    slea.readInt();
                }
                int n5 = 0;
                int n6 = 0;
                final Map<Integer, Map<Integer, Map<Integer, Pair<Integer, Integer>>>> map = AuctionServer.auctions.get(int9);
                final Map<Integer, Map<Integer, Pair<Integer, Integer>>> map2;
                final Map<Integer, Pair<Integer, Integer>> map3;
                if ((map) != null && (map2 = map.get(int10)) != null && (map3 = map2.get(int11)) != null) {
                    for (final Pair pair : map3.values()) {
                        if (n5 == 0) {
                            n5 = (int) pair.left;
                        } else {
                            n5 = Math.min(n5, (int) pair.left);
                        }
                        if (n6 == 0) {
                            n6 = (int) pair.right;
                        } else {
                            n6 = Math.max(n6, (int) pair.right);
                        }
                    }
                }
                final List<AuctionItem> a;
                if ((a = as.findItem(an, n5, n6, int12, int13, int14)).isEmpty()) {
                    c.announce(AuctionPacket.auctionResult(opt, 102));
                    break;
                }
                c.announce(AuctionPacket.auctionResult(AuctionOptType.LOAD_ALL_ITEM, 0));
                final List<List<AuctionItem>> c1207;
                if ((c1207 = splitList(a, 98)) != null /*&& !c1207.isEmpty()*/) {
                    c1207.forEach(list5 -> c.announce(AuctionPacket.loadAllItem(list5)));
                }
                break;
            }
            case LOAD_QUOTATION: {
                slea.readLong();
                slea.readLong();
                slea.readByte();
                final int int16 = slea.readInt();
                final int int17 = slea.readInt();
                slea.readLong();
                slea.readLong();
                slea.readInt();
//                slea.readInt();
                final List<List<AuctionItem>> c1208;
                if ((c1208 = splitList(as.findBuyerItemByLevel(int16, int17), 98)) != null && !c1208.isEmpty()) {
                    c.announce(AuctionPacket.auctionResult(AuctionOptType.LOAD_QUOTATION, 0));
                    c1208.forEach(list6 -> c.announce(AuctionPacket.loadQuotation(list6)));
                    return;
                }
                break;
            }
            case LOAD_SELL: {
                c.announce(AuctionPacket.loadSell(as.getAllOnsaleItemByPlayerId(player.getId())));
                break;
            }
            case LOAD_STORE: {
                c.announce(AuctionPacket.loadStore(as.getAllNotOnsaleItemByPlayerId(player.getId())));
                break;
            }
            case DELETE_ITEM: {
                final AuctionItem d587;
                if ((d587 = as.terminateById(player, slea.readInt())) != null) {
                    AuctionServer.getInstance().updateAuctionItem(d587);
                    c.announce(AuctionPacket.auctionResult(opt, 0));
                    c.announce(AuctionPacket.loadSell(as.getAllOnsaleItemByPlayerId(player.getId())));
                    c.announce(AuctionPacket.loadStore(as.getAllNotOnsaleItemByPlayerId(player.getId())));
                    return;
                }
                c.announce(AuctionPacket.auctionResult(opt, 114));
                break;
            }
            case RETURN_ITEM: {
                final long long2 = slea.readLong();
                slea.readInt();
                slea.readInt();
                slea.readInt();
                final int int18 = slea.readInt();
                slea.readInt();
                slea.readLong();
                slea.readLong();
                slea.readLong();
                final int int19 = slea.readInt();
                slea.readInt();
                final AuctionItem r2;
                if ((r2 = as.getItemBySN(long2)) == null || r2.characters_id != player.getId() || r2.state <= 0 || r2.state > 5 || r2.item == null) {
                    c.announce(AuctionPacket.auctionResult(opt, 1));
                    return;
                }
                final Item item = r2.item;
                if (MapleInventoryManipulator.checkSpace(c, int18, int19, "")) {
                    r2.item = null;
                    String log = null;
                    switch (r2.state) {
                        case 2: {
                            log = "拍賣交易完成取出!";
                            break;
                        }
                        case 4: {
                            log = "拍賣未完成交易退回取出!";
                            break;
                        }
                        default: {
                            log = "其他情況完成取出!";
                            break;
                        }
                    }
                    r2.state += 5;
                    r2.expiredate = System.currentTimeMillis() + 259200000L;
                    MapleInventoryManipulator.addbyItem(c, item, true);
                    item.setGMLog(player + " " + item + " " + log);
                    as.changeAuctionItemWorld(r2);
                    c.announce(AuctionPacket.auctionResult(opt, 0));
                    c.announce(AuctionPacket.characterModifiedEX(player, -1L));
                    c.announce(AuctionPacket.loadStore(as.getAllNotOnsaleItemByPlayerId(player.getId())));
                    player.saveToDB(false, true);
                    return;
                }
                c.announce(AuctionPacket.auctionResult(opt, 116));
                break;
            }
            case RECEIVE_PAYMENT: {
                final long long3 = slea.readLong();
                slea.readInt();
                slea.readInt();
                slea.readInt();
                slea.readInt();
                slea.readInt();
                slea.readLong();
                slea.readLong();
                slea.readLong();
                slea.readInt();
                slea.readInt();
//                slea.readInt();
                final AuctionItem r3;
                if ((r3 = as.getItemBySN(long3)) == null || r3.characters_id != player.getId() || r3.state != 3 || r3.item != null) {
                    c.announce(AuctionPacket.auctionResult(opt, 1));
                    break;
                }
                final long n7 = r3.price - r3.price * (player.isSilverMvp() ? 3 : 5) / 100L;
                if (((r3.type == 2) ? player.getCSPoints(2) : player.getMeso()) + n7 <= ((r3.type == 2) ? 2147483647L : 99999999999L)) {
                    if (r3.type == 2) {
                        player.modifyCSPoints(2, (int) n7);
                    } else {
                        player.gainMeso(n7, false);
                    }
                    r3.donedate = System.currentTimeMillis();
                    r3.expiredate = System.currentTimeMillis() + 259200000L;
                    r3.state += 5;
                    as.changeAuctionItemWorld(r3);
                    c.announce(AuctionPacket.auctionResult(opt, 0));
                    c.announce(AuctionPacket.characterModifiedEX(player, -1L));
                    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                    mplew.writeOpcode(OutHeader.AuctionResult);
                    mplew.writeInt(RECEIVE_PAYMENT);
                    mplew.writeInt(0);
                    mplew.writeInt(long3);
                    c.announce(mplew.getPacket());
                    c.announce(AuctionPacket.loadStore(as.getAllNotOnsaleItemByPlayerId(player.getId())));
                    player.saveToDB(false, true);
                    break;
                }
                c.announce(AuctionPacket.auctionResult(opt, 106));
                break;
            }
            case ADD_COLLECTION: {
                int auctionId = slea.readInt();
                int itemId = slea.readInt();

                break;
            }
            case DELETE_COLLECTION: {
                int auctionId = slea.readInt();
                break;
            }
            default:
                log.warn("未處理的拍賣操作：" + opt);
                break;
        }
    }

    private static List<List<AuctionItem>> splitList(final List<AuctionItem> list, final int each) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        final ArrayList<List<AuctionItem>> list2 = new ArrayList<>();
        final int n2 = list.size() % each;
        final int n3 = list.size() / each;
        for (int i = 0; i < n3; ++i) {
            list2.add(list.subList(i * each, (i + 1) * each));
        }
        if (n2 > 0) {
            list2.add(list.subList(n3 * each, n3 * each + n2));
        }
        return list2;
    }

    public static void EnterAuctionRequest(MapleClient c, MapleCharacter player) {
        if (player == null) {
            return;
        }
        if (player.hasBlockedInventory()) {
            c.sendEnableActions();
            return;
        }
        InterServerHandler.EnterAuction(c, player);
    }

    public static void EnterAuction(CharacterTransfer transfer, MapleClient c) {
        MapleCharacter player = MapleCharacter.ReconstructChr(transfer, c, false);
        c.setPlayer(player);
        c.setAccID(player.getAccountID());

        if (!c.CheckIPAddress()) { // Remote hack
            c.getSession().close();
            return;
        }
        if (player.getParty() != null) {
            PartyMember pm = player.getParty().getPartyMemberByID(player.getId());
            if (pm != null) {
                pm.setFieldID(0);
                pm.setChannel(0);
                player.getParty().broadcast(WvsContext.partyResult(PartyResult.setMemberData(pm, 3)));
                player.getParty().updateFull();
            }
        }
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
        AuctionServer.getInstance().getPlayerStorage().registerPlayer(player);
        c.announce(MaplePacketCreator.serverMessage(""));
        MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.LP_SetAuction);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        PacketHelper.addCharacterInfo(mplew, player, -1L);
        PacketHelper.addExpirationTime(mplew, System.currentTimeMillis());
        c.announce(mplew.getPacket());
        AuctionPacket.EnterActionRequestAuth(c); // 30
        c.write(Login.sendServerValues()); // 73
        c.write(Login.sendServerEnvironment()); // 74
        c.write(Login.sendWZCheckList()); // 51
        c.announce(c.getEncryptOpcodesData(ServerConstants.OpcodeEncryptionKey)); // 66 LP_OpcodeEncryption
        c.write(Login.sendPingCheckResultClientToGame()); // 29
        AuctionPacket.LoginActionCheck(c); // 54
        c.announce(ChatPacket.getChatLoginResult()); // 32
    }

    public static void AuctionExit(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        int type = slea.readInt();
        if (type == 1) {
            InterServerHandler.ExitAuction(c, player);
        }
    }
}
