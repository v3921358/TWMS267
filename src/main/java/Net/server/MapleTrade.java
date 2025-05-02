package Net.server;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.ItemAttribute;
import Client.inventory.MapleInventoryType;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Net.server.commands.CommandManager;
import Packet.MaplePacketCreator;
import Packet.PlayerShopPacket;
import Packet.TradePacket;
import Server.world.WorldBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class MapleTrade {

    private static final Logger log = LoggerFactory.getLogger("PlayerTrade");
    private final List<Item> items = new LinkedList<>(); //保存交易中的道具
    private final WeakReference<MapleCharacter> chr;
    private final byte tradingslot;
    private MapleTrade partner = null;
    private List<Item> exchangeItems;
    private int meso = 0, exchangeMeso = 0;
    private boolean locked = false;
    private boolean inTrade = false;
    private final boolean isCash;

    public MapleTrade(byte tradingslot, MapleCharacter chr, boolean isCash) {
        this.tradingslot = tradingslot;
        this.chr = new WeakReference<>(chr);
        this.isCash = isCash;
    }

    /*
     * 交易確定
     * local 玩家自己的交易
     * partner 對方的交易
     */
    public static void completeTrade(MapleCharacter player) {
        MapleTrade local = player.getTrade();
        MapleTrade partner = local.getPartner();
        if (partner == null || local.locked) {
            return;
        }
        local.locked = true; // 確定這個交易
        partner.getChr().getClient().announce(TradePacket.getTradeConfirmation());
        partner.exchangeItems = new LinkedList<>(local.items); // 複製交易中的道具信息
        partner.exchangeMeso = local.meso; // 複製交易中的楓幣
        if (partner.isLocked()) { // 玩家已經點了確定交易
            int lz = local.check(), lz2 = partner.check(); //檢測交易中雙方的背包空間信息和道具是否能交易
            if (lz == 0 && lz2 == 0) { //雙方都通過檢測
                log.info("[交易] -------------------------------------------------------------------------- ");
                local.CompleteTrade();
                partner.CompleteTrade();
                log.info("[交易] " + local.getChr().getName() + " 和 " + partner.getChr().getName() + " 交易完成。");
            } else {
                // 注意 : 如果雙方都確定交易後 交易對象的背包是滿的 交易就自動取消.
                partner.cancel(partner.getChr().getClient(), partner.getChr(), lz == 0 ? lz2 : lz);
                local.cancel(player.getClient(), player, lz == 0 ? lz2 : lz);
            }
            partner.getChr().setTrade(null);
            player.setTrade(null);
        }
    }

    /*
     * 交易取消
     */
    public static void cancelTrade(MapleTrade Localtrade, MapleClient c, MapleCharacter player) {
        Localtrade.cancel(c, player);
        MapleTrade partner = Localtrade.getPartner();
        if (partner != null && partner.getChr() != null) {
            partner.cancel(partner.getChr().getClient(), partner.getChr());
            partner.getChr().setTrade(null);
        }
        player.setTrade(null);
    }

    /*
     * 開始交易
     * 也就是創建1個交易 v261-4 ok
     */
    public static void startTrade(MapleCharacter player, boolean cash) {
        if (player.getTrade() == null) {
            player.setTrade(new MapleTrade((byte) 0, player, cash));
            player.getClient().announce(TradePacket.getTradeStart(player.getClient(), player.getTrade(), (byte) 0, cash));
        } else {
            player.getClient().announce(MaplePacketCreator.serverNotice(5, "不能同時做多件事情。"));
        }
    }

    /*
     * 交易邀請
     */
    public static void inviteTrade(MapleCharacter player, MapleCharacter target) {
        if (player == null || player.getTrade() == null) {
            return;
        }
        if (target != null && target.getTrade() == null) {
            target.setTrade(new MapleTrade((byte) 1, target, player.getTrade().isCash));
            target.getTrade().setPartner(player.getTrade());
            player.getTrade().setPartner(target.getTrade());
            target.getClient().announce(TradePacket.getTradeInvite(player, player.getTrade().isCash));
        } else {
            player.getClient().announce(MaplePacketCreator.serverNotice(5, "對方正在和其他玩家進行交易中。"));
            cancelTrade(player.getTrade(), player.getClient(), player);
        }
    }

    /*
     * 訪問交易 進入開始交易狀態
     */
    public static void visitTrade(MapleCharacter player, MapleCharacter target) {
        if (target != null && player.getTrade() != null && player.getTrade().getPartner() == target.getTrade() && target.getTrade() != null && target.getTrade().getPartner() == player.getTrade()) {
            player.getTrade().inTrade = true;
            target.getClient().announce(PlayerShopPacket.playerInterVisitorAdd(player, 1, player.getTrade().isCash));
            player.getClient().announce(TradePacket.getTradeStart(player.getClient(), player.getTrade(), (byte) 1, player.getTrade().isCash));
            player.dropMessage(-2, "系統提示 : 交易時請仔細查看交易的道具信息");
            target.dropMessage(-2, "系統提示 : 交易時請仔細查看交易的道具信息");
        } else {
            player.getClient().announce(MaplePacketCreator.serverNotice(5, "對方已經取消了交易。"));
        }
    }

    /*
     * 拒絕交易邀請
     */
    public static void declineTrade(MapleCharacter player) {
        MapleTrade trade = player.getTrade();
        if (trade != null) {
            if (trade.getPartner() != null) {
                MapleCharacter other = trade.getPartner().getChr();
                if (other != null && other.getTrade() != null) {
                    other.getTrade().cancel(other.getClient(), other);
                    other.setTrade(null);
                    other.dropMessage(5, player.getName() + " 拒絕了你的交易邀請。");
                }
            }
            trade.cancel(player.getClient(), player);
            player.setTrade(null);
        }
    }

    /*
     * 交易完成
     */
    public void CompleteTrade() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (exchangeItems != null) { // just to be on the safe side...
            List<Item> itemz = new LinkedList<>(exchangeItems);
            for (Item item : itemz) {
                int flag = item.getAttribute();
                if (ItemAttribute.TradeOnce.check(flag)) {
                    flag &= ~ItemAttribute.TradeOnce.getValue();
                }
                if (ItemAttribute.CutUsed.check(flag)) {
                    flag &= ~ItemAttribute.CutUsed.getValue();
                }
                item.setAttribute(flag);
                MapleInventoryManipulator.addFromDrop(chr.get().getClient(), item, false);
                log.info("[交易] " + chr.get().getName() + " 交易獲得道具: " + item.getItemId() + " x " + item.getQuantity() + " - " + ii.getName(item.getItemId()));
            }
            exchangeItems.clear();
        }
        if (exchangeMeso > 0) {
            chr.get().gainMeso(exchangeMeso - GameConstants.getTaxAmount(exchangeMeso), false, false);
            log.info("[交易] " + chr.get().getName() + " 交易獲得楓幣: " + exchangeMeso);
        }
        exchangeMeso = 0;
        chr.get().getClient().announce(TradePacket.TradeMessage(tradingslot, (byte) 0x07));
    }

    /*
     * 交易取消
     * 直接取消交易 也就是退出交易
     */
    public void cancel(MapleClient c, MapleCharacter chr) {
        cancel(c, chr, 0);
    }

    /*
     * 交易取消
     * 0x02 對方中止交易。
     * 0x08 交易成功了。請再確認交易的結果。
     * 0x09 交易失敗了。
     * 0x0A 因部分道具有數量限制只能擁有一個交易失敗了。
     * 0x0D 雙方在不同的地圖不能交易。
     * 0x0E 遊戲文件損壞，無法交易物品。請重新安裝遊戲後，再重新嘗試。
     */
    public void cancel(MapleClient c, MapleCharacter chr, int message) {
        if (items != null) { //為了安全起見 檢測下交易中是否有道具
            List<Item> itemz = new LinkedList<>(items);
            for (Item item : itemz) {
                MapleInventoryManipulator.addFromDrop(c, item, false); //將道具歸還給玩家
            }
            items.clear();
        }
        if (meso > 0) {
            chr.gainMeso(meso, false, false);
        }
        meso = 0;
        c.announce(TradePacket.getTradeCancel(tradingslot, message));
    }

    /*
     * 檢測交易是否確認
     */
    public boolean isLocked() {
        return locked;
    }

    /*
     * 添加楓幣
     */
    public void setMeso(int meso) {
        if (locked || partner == null || meso <= 0 || this.meso + meso <= 0) {
            return;
        }
        if (chr.get().getMeso() >= meso) {
            chr.get().gainMeso(-meso, false, false);
            this.meso += meso;
            chr.get().getClient().announce(TradePacket.getTradeMesoSet((byte) 0, this.meso));
            if (partner != null) {
                partner.getChr().getClient().announce(TradePacket.getTradeMesoSet((byte) 1, this.meso));
            }
        }
    }

    /*
     * 添加道具
     */
    public void addItem(Item item) {
        if (locked || partner == null) {
            return;
        }
        items.add(item);
        chr.get().getClient().announce(TradePacket.getTradeItemAdd((byte) 0, item));
        if (partner != null) {
            partner.getChr().getClient().announce(TradePacket.getTradeItemAdd((byte) 1, item));
        }
    }

    /*
     * 交易聊天
     */
    public void chat(String message) {
//        if (!CommandProcessor.processCommand(chr.get().getClient(), message, CommandType.TRADE)) {
//        new CommandManager
        if(!(new CommandManager(this.getChr().getClient())).formatCommand(message)){
            chr.get().dropMessage(-2, message);
            if (partner != null) {
                partner.getChr().getClient().announce(PlayerShopPacket.playerInterChat(message, 1, chr.get().getName()));
            }
        }
        if (chr.get().getClient().isMonitored()) { //Broadcast info even if it was a command.
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, chr.get().getName() + " 在交易中對 " + partner.getChr().getName() + " 說: " + message));
        } else if (partner != null && partner.getChr() != null && partner.getChr().getClient().isMonitored()) {
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, chr.get().getName() + " 在交易中對 " + partner.getChr().getName() + " 說: " + message));
        }
    }

    public void chatAuto(String message) {
        chr.get().dropMessage(-2, message);
        if (partner != null) {
            partner.getChr().getClient().announce(PlayerShopPacket.playerInterChat(message, 1, chr.get().getName()));
        }
        if (chr.get().getClient().isMonitored()) { //Broadcast info even if it was a command.
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, chr.get().getName() + " said in trade [Automated] with " + partner.getChr().getName() + " 說: " + message));
        } else if (partner != null && partner.getChr() != null && partner.getChr().getClient().isMonitored()) {
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, chr.get().getName() + " said in trade [Automated] with " + partner.getChr().getName() + " 說: " + message));
        }
    }

    public MapleTrade getPartner() {
        return partner;
    }

    public void setPartner(MapleTrade partner) {
        if (locked) {
            return;
        }
        if (this.items != null) {
            this.items.clear();
        }
        if (this.exchangeItems != null) {
            this.exchangeItems.clear();
        }
        this.meso = 0;
        this.exchangeMeso = 0;
        this.partner = partner;
    }

    public MapleCharacter getChr() {
        return chr.get();
    }

    public int getNextTargetSlot() {
        if (items.size() >= 9) {
            return -1;
        }
        int ret = 1; //first slot
        for (Item item : items) {
            if (item.getPosition() == ret) {
                ret++;
            }
        }
        return ret;
    }

    public boolean inTrade() {
        return inTrade;
    }

    /*
     * 添加道具
     */
    public boolean setItems(MapleClient c, Item item, byte targetSlot, int quantity) {
        int target = getNextTargetSlot();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (partner == null || target == -1 || (ItemConstants.類型.寵物(item.getItemId()) && !isCash) || ii.isCash(item.getItemId()) != isCash || isLocked() || (ItemConstants.getInventoryType(item.getItemId(), false) == MapleInventoryType.EQUIP && quantity != 1)) {
            return false;
        }
        int flag = item.getCAttribute();
        if ((ItemAttribute.TradeBlock.check(flag) && !ItemAttribute.TradeOnce.check(flag) && !ItemAttribute.CutUsed.check(flag)) || ItemAttribute.Seal.check(flag)) {
            c.sendEnableActions();
            return false;
        }
        if (ItemAttribute.AccountSharable.check(flag)) {
            c.sendEnableActions();
            return false;
        }
        if (ii.isDropRestricted(item.getItemId()) || ii.isAccountShared(item.getItemId())) {
            if (!(ItemAttribute.TradeOnce.check(flag) || ItemAttribute.CutUsed.check(flag))) {
                c.sendEnableActions();
                return false;
            }
        }
        Item tradeItem = item.copy();
        if (ItemConstants.類型.飛鏢(item.getItemId()) || ItemConstants.類型.子彈(item.getItemId())) {
            tradeItem.setQuantity(item.getQuantity());
            MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(item.getItemId()), item.getPosition(), item.getQuantity(), true);
        } else {
            tradeItem.setQuantity((short) quantity);
            MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(item.getItemId()), item.getPosition(), (short) quantity, true);
        }
        if (item.getPet() != null && item.getPet().getSummoned()) {
            c.getPlayer().unequipSpawnPet(item.getPet(), true, (byte) 0);
        }
        if (targetSlot < 0) {
            targetSlot = (byte) target;
        } else {
            for (Item itemz : items) {
                if (itemz.getPosition() == targetSlot) {
                    targetSlot = (byte) target;
                    break;
                }
            }
        }
        tradeItem.setPosition(targetSlot);
        addItem(tradeItem);
        return true;
    }

    /*
     * 交易中雙方確認後檢測背包空間
     * 0 = 可以完成交易
     * 1 = 背包空間不足
     * 2 = 交易的道具中有撿取限制的道具
     */
    private int check() {
        if (chr.get().getMeso() + exchangeMeso < 0) {
            return 1;
        }
        if (exchangeItems != null) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0, decoration = 0;
            for (Item item : exchangeItems) {
                switch (ItemConstants.getInventoryType(item.getItemId())) {
                    case EQUIP:
                        eq++;
                        break;
                    case USE:
                        use++;
                        break;
                    case SETUP:
                        setup++;
                        break;
                    case ETC:
                        etc++;
                        break;
                    case CASH:
                        cash++;
                        break;
                    case DECORATION:
                        decoration++;
                        break;
                }
                if (ii.isPickupRestricted(item.getItemId()) && chr.get().haveItem(item.getItemId(), 1, true, true)) {
                    return 2;
                }
            }
            if (chr.get().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq || chr.get().getInventory(MapleInventoryType.USE).getNumFreeSlot() < use || chr.get().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup || chr.get().getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc || chr.get().getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash || chr.get().getInventory(MapleInventoryType.DECORATION).getNumFreeSlot() < decoration) {
                return 1;
            }
        }
        return 0;
    }
}
