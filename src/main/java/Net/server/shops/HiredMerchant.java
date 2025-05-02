package Net.server.shops;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.ItemAttribute;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.Timer.EtcTimer;
import Net.server.maps.MapleMapObjectType;
import Packet.MaplePacketCreator;
import Packet.PlayerShopPacket;
import Server.channel.ChannelServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class HiredMerchant extends AbstractPlayerStore {

    public static final Logger log = LoggerFactory.getLogger("HiredMerchant");
    private final List<String> blacklist;
    private final long start;
    public ScheduledFuture<?> schedule;
    private int storeid;
    private long lastChangeNameTime = 0; //改變僱傭商店名稱的時間

    public HiredMerchant(MapleCharacter owner, int itemId, String desc) {
        super(owner, itemId, desc, "", 6); //以前是3個人 V.100改為6個
        start = System.currentTimeMillis();
        blacklist = new LinkedList<>();
        this.schedule = EtcTimer.getInstance().schedule(() -> {
            if (getMCOwner() != null && getMCOwner().getPlayerShop() == HiredMerchant.this) {
                getMCOwner().setPlayerShop(null);
            }
            removeAllVisitors(-1, -1);
            closeShop(true, true);
        }, 1000 * 60 * 60 * 24);
    }

    @Override
    public byte getShopType() {
        return IMaplePlayerShop.HIRED_MERCHANT;
    }

    public void setStoreid(int storeid) {
        this.storeid = storeid;
    }

    public List<MaplePlayerShopItem> searchItem(int itemSearch) {
        List<MaplePlayerShopItem> itemz = new LinkedList<>();
        for (MaplePlayerShopItem item : items) {
            if (item.item.getItemId() == itemSearch && item.bundles > 0) {
                itemz.add(item);
            }
        }
        return itemz;
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = items.get(item);
        Item shopItem = pItem.item;
        Item newItem = shopItem.copy();
        short perbundle = newItem.getQuantity();
        long theQuantity = (pItem.price * quantity);
        newItem.setQuantity((short) (quantity * perbundle));

        if (ItemAttribute.TradeOnce.check(newItem.getAttribute())) {
            newItem.removeAttribute(ItemAttribute.TradeOnce.getValue());
        } else if (ItemAttribute.CutUsed.check(newItem.getAttribute())) {
            newItem.removeAttribute(ItemAttribute.CutUsed.getValue());
        }

        if (MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner()) && c.getPlayer().getInventory(ItemConstants.getInventoryType(newItem.getItemId())).getNextFreeSlot() > -1) {
            long gainmeso = getMeso() + theQuantity - GameConstants.EntrustedStoreTax(theQuantity);
            if (gainmeso > 0) {
                setMeso(gainmeso);
                pItem.bundles -= quantity; // Number remaining in the store
                MapleInventoryManipulator.addFromDrop(c, newItem, false);
                bought.add(new BoughtItem(newItem.getItemId(), quantity, theQuantity, c.getPlayer().getName()));
                c.getPlayer().gainMeso(-theQuantity, false);
                saveItems();
                MapleCharacter chr = getMCOwnerWorld();
                StringBuilder msg = new StringBuilder();
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                msg.append("物品");
                msg.append(ii.getName(newItem.getItemId()));
                msg.append(" 通過精靈商人售出（");
                msg.append(perbundle).append("x").append(quantity);
                msg.append("）剩餘數量：");
                msg.append(pItem.bundles).append(" 購買者：").append(c.getPlayer().getName());
                if (chr != null) {
                    chr.send(MaplePacketCreator.showRedNotice(msg.toString()));
                }
                log.info(chr != null ? chr.getName() : getOwnerName() + " 僱傭商店賣出: " + newItem.getItemId() + " - " + msg + " 價格: " + theQuantity);
            } else {
                c.getPlayer().dropMessage(1, "楓幣不足.");
                c.sendEnableActions();
            }
        } else {
            c.getPlayer().dropMessage(1, "背包已滿.");
            c.sendEnableActions();
        }
    }

    @Override
    public void closeShop(boolean saveItems, boolean remove) {
        if (schedule != null) {
            schedule.cancel(false);
        }
        if (saveItems) {
            saveItems();
            items.clear();
        }
        if (remove) {
            ChannelServer.getInstance(channel).removeMerchant(this);
            getMap().broadcastMessage(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
        }
        getMap().removeMapObject(this);
        schedule = null;
    }

    public int getTimeLeft() {
        return (int) (System.currentTimeMillis() - start);
    }

    public int getTimeLeft(boolean first) {
        if (first) {
            return (int) start;
        }
        return 60 * 60 * 24 - (int) (System.currentTimeMillis() - start) / 1000;
    }

    public int getStoreId() {
        return storeid;
    }

    /*
     * 檢測是否能修改僱傭商店名稱
     */
    public boolean canChangeName() {
        if (lastChangeNameTime + 60000 > System.currentTimeMillis()) {
            return false;
        }
        lastChangeNameTime = System.currentTimeMillis();
        return true;
    }

    public int getChangeNameTimeLeft() {
        int time = 60 - (int) (System.currentTimeMillis() - lastChangeNameTime) / 1000;
        return time > 0 ? time : 1;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.HIRED_MERCHANT;
    }

    @Override
    public int getRange() {
        return GameConstants.maxViewRange();
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        if (isAvailable()) {
            client.announce(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (isAvailable()) {
            client.announce(PlayerShopPacket.spawnHiredMerchant(this));
        }
    }

    public boolean isInBlackList(String bl) {
        return blacklist.contains(bl);
    }

    public void addBlackList(String bl) {
        blacklist.add(bl);
    }

    public void removeBlackList(String bl) {
        blacklist.remove(bl);
    }

    public void sendBlackList(MapleClient c) {
        c.announce(PlayerShopPacket.MerchantBlackListView(blacklist));
    }

    public void sendVisitor(MapleClient c) {
        c.announce(PlayerShopPacket.MerchantVisitorView(visitorsList));
    }
}
