package Server.cashshop.handler;

import Client.MapleClient;
import Client.inventory.Equip;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Config.constants.ItemConstants;
import Config.constants.ServerConstants;
import Net.server.MTSCart;
import Net.server.MTSStorage;
import Net.server.MTSStorage.MTSItemInfo;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Packet.MTSCSPacket;
import tools.data.MaplePacketReader;

public class MTSOperation {

    public static void MTSOperation(final MaplePacketReader slea, final MapleClient c) {
        final MTSCart cart = MTSStorage.getInstance().getCart(c.getPlayer().getId());
        //System.out.println(slea.toString());
        if (slea.available() <= 0) {
            doMTSPackets(cart, c);
            return;
        }
        final byte op = slea.readByte();
        if (op == 2) { //put up for sale
            final byte invType = slea.readByte(); //1 = equip 2 = everything else
            if (invType != 1 && invType != 2) { //pet?
                c.announce(MTSCSPacket.getMTSFailSell());
                doMTSPackets(cart, c);
                return;
            }
            final int itemid = slea.readInt(); //itemid
            if (slea.readByte() != 0) {
                c.announce(MTSCSPacket.getMTSFailSell());
                doMTSPackets(cart, c);
                return;//we don't like uniqueIDs
            }
            slea.skip(12); //expiration, -1, don't matter
            short stars = 1, quantity = 1;
            short slot = 0;
            if (invType == 1) {
                slea.skip(32);
            } else {
                stars = slea.readShort(); //the entire quantity of the item
            }
            slea.readMapleAsciiString();//owner
            //again? =/
            if (invType == 1) {
                slea.skip(50);
                slot = (short) slea.readInt();
                slea.skip(4); //skip the quantity int, equips are always 1
            } else {
                slea.readShort(); //flag
                if (ItemConstants.類型.飛鏢(itemid) || ItemConstants.類型.子彈(itemid)) {
                    slea.skip(8);//recharge ID thing
                }
                slot = (short) slea.readInt();
                if (ItemConstants.類型.飛鏢(itemid) || ItemConstants.類型.子彈(itemid)) {
                    quantity = stars; //this is due to stars you need to use the entire quantity, not specified
                    slea.skip(4); //so just skip the quantity int
                } else {
                    quantity = (short) slea.readInt(); //specified quantity
                }
            }
            final int price = slea.readInt();
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleInventoryType type = ItemConstants.getInventoryType(itemid);
            final Item item = c.getPlayer().getInventory(type).getItem(slot).copy();
            if (ii.isCash(itemid) || quantity <= 0 || item == null || item.getQuantity() <= 0 || item.getItemId() != itemid || item.getSN() > 0 || item.getQuantity() < quantity || price < ServerConstants.MIN_MTS || c.getPlayer().getMeso() < ServerConstants.MTS_MESO || cart.getNotYetSold().size() >= 10 || ii.isDropRestricted(itemid) || ii.isAccountShared(itemid) || item.getExpiration() > -1 || item.getAttribute() > 0) {
                c.announce(MTSCSPacket.getMTSFailSell());
                doMTSPackets(cart, c);
                return;
            }
            if (type == MapleInventoryType.EQUIP || type == MapleInventoryType.DECORATION) {
                final Equip eq = (Equip) item;
                if (eq.getState(false) > 0 || eq.getStarForceLevel() > 0 || eq.getDurability() > -1) {
                    c.announce(MTSCSPacket.getMTSFailSell());
                    doMTSPackets(cart, c);
                    return;
                }
            }
            if (quantity >= 50 && item.getItemId() == 2340000) {
                c.setMonitored(true); //hack check
            }
            final long expiration = (System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000));
            item.setQuantity(quantity);
            MTSStorage.getInstance().addToBuyNow(cart, item, price, c.getPlayer().getId(), c.getPlayer().getName(), expiration);
            MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
            c.getPlayer().gainMeso(-ServerConstants.MTS_MESO, false);
            c.announce(MTSCSPacket.getMTSConfirmSell());
        } else if (op == 5) { //change page/tab
            cart.changeInfo(slea.readInt(), slea.readInt(), slea.readInt());
        } else if (op == 6) { //search
            cart.changeInfo(slea.readInt(), slea.readInt(), 0);
            slea.readInt(); //always 0?
            cart.changeCurrentView(MTSStorage.getInstance().getSearch(slea.readInt() > 0, slea.readMapleAsciiString(), cart.getType(), cart.getTab()));
        } else if (op == 7) { //cancel sale
            if (!MTSStorage.getInstance().removeFromBuyNow(slea.readInt(), c.getPlayer().getId(), true)) {
                c.announce(MTSCSPacket.getMTSFailCancel());
            } else {
                c.announce(MTSCSPacket.getMTSConfirmCancel());
                sendMTSPackets(cart, c, true);
                return;
            }
        } else if (op == 8) { //transfer item
            final int id = Integer.MAX_VALUE - slea.readInt(); //fake id
            if (id >= cart.getInventory().size()) {
                c.getPlayer().dropMessage(1, "Please try it again later.");
                sendMTSPackets(cart, c, true);
                return;
            }
            final Item item = cart.getInventory().get(id); //by index
            //System.out.println("NumItems: " + cart.getInventory().size() + ", ID: " + id + ", ItemExists?: " + (item != null));
            if (item != null && item.getQuantity() > 0 && MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
                Item item_ = item.copy();
                short pos = MapleInventoryManipulator.addbyItem(c, item_, true);
                if (pos >= 0) {
                    if (item_.getPet() != null) {
                        item_.getPet().setInventoryPosition(pos);
                    }
                    cart.removeFromInventory(item);
                    c.announce(MTSCSPacket.getMTSConfirmTransfer(ItemConstants.getInventoryType(item_.getItemId()).getType(), pos)); //IF this is actually pos and pos
                    sendMTSPackets(cart, c, true);
                    return;
                } else {
                    //System.out.println("addByItem is less than 0");
                    c.announce(MTSCSPacket.getMTSFailBuy());
                }
            } else {
                //System.out.println("CheckSpace return false");
                c.announce(MTSCSPacket.getMTSFailBuy());
            }
        } else if (op == 9) { //add to cart
            final int id = slea.readInt();
            if (MTSStorage.getInstance().checkCart(id, c.getPlayer().getId()) && cart.addToCart(id)) {
                c.announce(MTSCSPacket.addToCartMessage(false, false));
            } else {
                c.announce(MTSCSPacket.addToCartMessage(true, false));
            }
        } else if (op == 10) { //delete from cart
            final int id = slea.readInt();
            if (cart.getCart().contains(id)) {
                cart.removeFromCart(id);
                c.announce(MTSCSPacket.addToCartMessage(false, true));
            } else {
                c.announce(MTSCSPacket.addToCartMessage(true, true));
            }
        } else if (op == 16 || op == 17) { //buyNow, buy from cart
            final MTSItemInfo mts = MTSStorage.getInstance().getSingleItem(slea.readInt());
            if (mts != null && mts.getCharacterId() != c.getPlayer().getId()) {
                if (c.getPlayer().getCSPoints(1) > mts.getRealPrice()) {
                    if (MTSStorage.getInstance().removeFromBuyNow(mts.getId(), c.getPlayer().getId(), false)) {
                        c.getPlayer().modifyCSPoints(1, -mts.getRealPrice(), false);
                        MTSStorage.getInstance().getCart(mts.getCharacterId()).increaseOwedNX(mts.getPrice());
                        c.announce(MTSCSPacket.getMTSConfirmBuy());
                        sendMTSPackets(cart, c, true);
                        return;
                    } else {
                        c.announce(MTSCSPacket.getMTSFailBuy());
                    }
                } else {
                    c.announce(MTSCSPacket.getMTSFailBuy());
                }
            } else {
                c.announce(MTSCSPacket.getMTSFailBuy());
            }
        } else if (c.getPlayer().isAdmin()) {
            //System.out.println("New MTS Op " + op + ", \n" + slea.toString());
        }
        doMTSPackets(cart, c);
    }

    public static void MTSUpdate(final MTSCart cart, final MapleClient c) {
        final int a = MTSStorage.getInstance().getCart(c.getPlayer().getId()).getSetOwedNX();
        c.getPlayer().modifyCSPoints(1, a, false);
        c.announce(MTSCSPacket.getMTSWantedListingOver(0, 0));
        doMTSPackets(cart, c);
    }

    private static void doMTSPackets(final MTSCart cart, final MapleClient c) {
        sendMTSPackets(cart, c, false);
    }

    private static void sendMTSPackets(final MTSCart cart, final MapleClient c, final boolean changed) {
        c.announce(MTSStorage.getInstance().getCurrentMTS(cart));
        c.announce(MTSStorage.getInstance().getCurrentNotYetSold(cart));
        c.announce(MTSStorage.getInstance().getCurrentTransfer(cart, changed));
        c.announce(MTSCSPacket.showMTSCash(c.getPlayer()));
        MTSStorage.getInstance().checkExpirations();
    }
}
