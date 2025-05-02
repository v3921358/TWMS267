package Net.server.shops;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.ItemAttribute;
import Config.constants.GameConstants;
import Net.server.MapleInventoryManipulator;
import Packet.PlayerShopPacket;

import java.util.ArrayList;
import java.util.List;

public class MaplePlayerShop extends AbstractPlayerStore {

    private final List<String> bannedList = new ArrayList<>();
    private int boughtnumber = 0;

    public MaplePlayerShop(MapleCharacter owner, int itemId, String desc) {
        super(owner, itemId, desc, "", 6); //以前是3個人 V.100改為6個
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = items.get(item);
        if (pItem.bundles > 0) {
            Item newItem = pItem.item.copy();
            newItem.setQuantity((short) (quantity * newItem.getQuantity()));

            if (ItemAttribute.TradeOnce.check(newItem.getAttribute())) {
                newItem.removeAttribute(ItemAttribute.TradeOnce.getValue());
            } else if (ItemAttribute.CutUsed.check(newItem.getAttribute())) {
                newItem.removeAttribute(ItemAttribute.CutUsed.getValue());
            }
            long gainmeso = pItem.price * quantity;
            if (c.getPlayer().getMeso() >= gainmeso) {
                if (getMCOwner().getMeso() + gainmeso > 0 && MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner()) && MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
                    pItem.bundles -= quantity;
                    bought.add(new BoughtItem(newItem.getItemId(), quantity, gainmeso, c.getPlayer().getName()));
                    c.getPlayer().gainMeso(-gainmeso, false);
                    getMCOwner().gainMeso(gainmeso, false);
                    if (pItem.bundles <= 0) {
                        boughtnumber++;
                        if (boughtnumber == items.size()) {
                            closeShop(true, true);
                            return;
                        }
                    }
                } else {
                    c.getPlayer().dropMessage(1, "Your inventory is full.");
                }
            } else {
                c.getPlayer().dropMessage(1, "You do not have enough mesos.");
            }
            getMCOwner().getClient().announce(PlayerShopPacket.shopItemUpdate(this));
        }
    }

    @Override
    public byte getShopType() {
        return IMaplePlayerShop.PLAYER_SHOP;
    }

    @Override
    public void closeShop(boolean sellout, boolean remove) {
        byte error = (byte) (sellout ? 0x11 : 0x03);
        MapleCharacter owner = getMCOwner();
        removeAllVisitors(error, 1);
        getMap().removeMapObject(this);

        for (MaplePlayerShopItem items : getItems()) {
            if (items.bundles > 0) {
                Item newItem = items.item.copy();
                newItem.setQuantity((short) (items.bundles * newItem.getQuantity()));
                if (MapleInventoryManipulator.addFromDrop(owner.getClient(), newItem, false)) {
                    items.bundles = 0;
                } else {
                    saveItems(); //O_o
                    break;
                }
            }
        }
        owner.setPlayerShop(null);
        update();
        getMCOwner().getClient().announce(PlayerShopPacket.shopErrorMessage(error, 0));
    }

    public void banPlayer(String name) {
        if (!bannedList.contains(name)) {
            bannedList.add(name);
        }
        int slot = -1;
        for (int i = 0; i < getMaxSize(); i++) {
            MapleCharacter chr = getVisitor(i);
            if (chr != null && chr.getName().equals(name)) {
                slot = i + 1;
                break;
            }
        }

        if (slot != -1) {
            for (int i = 0; i < getMaxSize(); i++) {
                MapleCharacter chr = getVisitor(i);
                if (chr != null && chr.getName().equals(name)) {
                    chr.send(PlayerShopPacket.shopErrorMessage(5, slot));
                    chr.setPlayerShop(null);
                    removeVisitor(chr);
                }
            }
        }
    }

    public boolean isBanned(String name) {
        return bannedList.contains(name);
    }

    @Override
    public int getRange() {
        return GameConstants.maxViewRange();
    }
}
