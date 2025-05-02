package Net.server.shop;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Client.inventory.ModifyInventory;
import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Config.constants.enums.ConversationType;
import Config.constants.skills.冒險家_技能群組.夜使者;
import Config.constants.skills.冒險家_技能群組.槍神;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Net.server.AutobanManager;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.buffs.MapleStatEffect;
import Packet.InventoryPacket;
import Packet.NPCPacket;
import tools.DateUtil;
import tools.FileoutputUtil;
import tools.Pair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class MapleShop {
    public static final Pattern LOG_PATTERN = Pattern.compile("^商店購買 (\\d+), .+$");

    private final int id;
    private final int npcId;
    private final List<MapleShopItem> items;
    private final List<Pair<Integer, String>> ranks = new ArrayList<>();
    private int shopItemId;

    public MapleShop(int id, int npcId) {
        this.id = id;
        this.npcId = npcId;
        this.shopItemId = 0;
        this.items = new ArrayList<>();
    }

    public void addItem(MapleShopItem item) {
        items.add(item);
    }

    public void removeItem(MapleShopItem item) {
        items.remove(item);
    }

    public List<MapleShopItem> getItems() {
        return items;
    }

    public List<MapleShopItem> getItems(MapleClient c) {
        List<MapleShopItem> itemsPlusRebuy = new ArrayList<>(items);
        if (c.getPlayer().isScriptShop()) {
            for (MapleShopItem item : items) {
                if (item.isRechargeableItem() && item.getPrice() == 0) {
                    itemsPlusRebuy.remove(item);
                }
            }
        }
        int i = 0;
        for (MapleShopItem si : c.getPlayer().getRebuy()) {
            if (i >= 10) {
                break;
            }
            itemsPlusRebuy.add(si);
            i++;
        }
        return itemsPlusRebuy;
    }

    public final int getBuyLimitItemIndex(final int itemId) {
        for (MapleShopItem item : items) {
            if (item.isRechargeableItem() && item.getPrice() == 0) {
                continue;
            }
            if (item.getItemId() == itemId && (item.getBuyLimit() > 0 || item.getBuyLimitWorldAccount() > 0)) {
                return items.indexOf(item);
            }
        }
        return -1;
    }

    public void sendShop(MapleClient c) {
        sendShop(c, false);
    }

    public void sendShop(MapleClient c, boolean fromScript) {
        sendShop(c, getNpcId(), fromScript);
    }

    public void sendShop(MapleClient c, int customNpc) {
        sendShop(c, customNpc, false);
    }

    public void sendShop(MapleClient c, int customNpc, boolean fromScript) {
        if (c.getPlayer().isAdmin()) {
            c.getPlayer().dropDebugMessage(0, "[開啟" + (fromScript ? "腳本" : "") + "商店] 商店ID: " + id + " 商店NPC: " + customNpc);
        }
        c.getPlayer().setConversation(ConversationType.ON_NPC_SHOP);
        c.getPlayer().setShop(this);
        c.getPlayer().setScriptShop(fromScript);
        NpcShopBuyLimit buyLimit = c.getPlayer().getBuyLimit().get(this.id);
        if (buyLimit == null) {
            buyLimit = c.getPlayer().getAccountBuyLimit().get(this.id);
        }
        List<Integer> list = new ArrayList<>();
        final List<Integer> mu;
        if (buyLimit != null && !(mu = buyLimit.getInfo()).isEmpty()) {
            for (int aMu : mu) {
                final int n = getBuyLimitItemIndex(aMu);
                if (n != -1) {
                    list.add(n);
                }
            }
            c.getPlayer().checkBuyLimit();
            c.announce(NPCPacket.ResetBuyLimitCount(id, list));
        }
        c.announce(NPCPacket.getNPCShop(customNpc, this, c));
    }

    public void sendItemShop(MapleClient c, int itemId) {
        this.shopItemId = itemId;
        sendShop(c);
    }

    public String getItemName(int itemId) {
        return MapleItemInformationProvider.getInstance().getName(itemId);
    }

    /*
     * 購買道具
     */
    public void buy(MapleClient c, int itemId, short quantity, short position) {
        final MapleCharacter player = c.getPlayer();
        MapleShopResponse response = MapleShopResponse.ShopRes_BuySuccess;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleShopItem shopItem = findBySlotAndId(c, itemId, position);
        long time = System.currentTimeMillis();
        int index = -1;
        if (quantity <= 0 || shopItem == null) {
            response = MapleShopResponse.ShopRes_BuyNoStock;
            c.announce(NPCPacket.confirmShopTransaction(response, this, c, index, -1));
            return;
        }
        if (shopItem.getSellStart() != -2 && shopItem.getSellStart() > time || shopItem.getSellEnd() != -1 && shopItem.getSellEnd() < time) {
            response = MapleShopResponse.ShopRes_BuyInvalidTime;
        } else if (c.getPlayer().getLevel() < shopItem.getMinLevel()) {
            response = MapleShopResponse.ShopRes_LimitLevel_More;
            index = shopItem.getMinLevel();
        } else if (shopItem.getMaxLevel() > 0 && c.getPlayer().getLevel() > shopItem.getMaxLevel()) {
            response = MapleShopResponse.ShopRes_LimitLevel_Less;
            index = shopItem.getMaxLevel();
        } else if (shopItem.getRebuy() != null) {
            if (c.getPlayer().getRebuy().isEmpty()) {
                c.sendEnableActions();
                return;
            }
            if (shopItem.getRebuy() == null) {
                response = MapleShopResponse.ShopRes_BuyNoStock;
            } else {
                long price = shopItem.getPrice();
                if (price > 0 && c.getPlayer().getMeso() < price) {
                    response = MapleShopResponse.ShopRes_BuyNoMoney;
                } else if (!MapleInventoryManipulator.checkSpace(c, itemId, quantity * shopItem.getQuantity(), "")) {
                    response = MapleShopResponse.ShopRes_NotEnoughSpace;
                } else {
                    index = c.getPlayer().getRebuy().indexOf(shopItem);
                    c.getPlayer().gainMeso(-price, false);
                    MapleInventoryManipulator.addbyItem(c, shopItem.getRebuy());
                    c.getPlayer().getRebuy().remove(shopItem);
                }
            }
        } else if (shopItem.getTokenItemID() > 0 && !player.haveItem(shopItem.getTokenItemID(), shopItem.getTokenPrice() * quantity)) {
            response = MapleShopResponse.ShopRes_BuyNoToken;
        } else if (shopItem.getPointPrice() > 0 && player.getQuestPoint(shopItem.getPointQuestID()) < shopItem.getPointPrice() * quantity) {
            response = MapleShopResponse.ShopRes_BuyNoPoint;
        } else if (shopItem.getPrice() > 0 && player.getMeso() < shopItem.getPrice() * quantity) {
            response = MapleShopResponse.ShopRes_BuyNoMoney;
        } else if (shopItem.getBuyLimit() > 0 && player.getBuyLimit(this.id, itemId) + quantity > shopItem.getBuyLimit()) {
            response = MapleShopResponse.ShopRes_BuyNoStock;
        } else if (shopItem.getBuyLimitWorldAccount() > 0 && player.getAccountBuyLimit(this.id, itemId) + quantity > shopItem.getBuyLimitWorldAccount()) {
            response = MapleShopResponse.ShopRes_BuyNoStock;
        } else if (!MapleInventoryManipulator.checkSpace(c, itemId, quantity * shopItem.getQuantity(), "")) {
            response = MapleShopResponse.ShopRes_NotEnoughSpace;
        } else {
            if (shopItem.getCategory() >= 0) {
                boolean passed = true;
                int y = 0;
                for (Pair<Integer, String> i : getRanks()) {
                    if (c.getPlayer().haveItem(i.left, 1, true, true) && shopItem.getCategory() >= y) {
                        passed = true;
                        break;
                    }
                    y++;
                }
                if (!passed) {
                    c.getPlayer().dropMessage(1, "You need a higher rank.");
                    c.sendEnableActions();
                    return;
                }
            }
            if (shopItem.getTokenItemID() > 0) {
                MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(shopItem.getTokenItemID()), shopItem.getTokenItemID(), ItemConstants.類型.可充值道具(itemId) ? shopItem.getTokenPrice() : (shopItem.getTokenPrice() * quantity), true, false);
                FileoutputUtil.logToFile("logs/Data/代幣購買.txt", "時間: " + FileoutputUtil.NowTime2() + " 帳號: " + c.getAccountName() + " 消耗: " + (shopItem.getTokenPrice() * quantity) + "個 " + getItemName(shopItem.getTokenItemID()) + shopItem.getTokenItemID() + "購買: " + quantity + "個 " + getItemName(itemId) + itemId + "\r\n");
            }
            if (shopItem.getPointPrice() > 0) {
                int price = ItemConstants.類型.可充值道具(itemId) ? shopItem.getPointPrice() : (shopItem.getPointPrice() * quantity);
                if (player.getWorldShareInfo(shopItem.getPointQuestID(), "point") != null) {
                    player.gainWorldShareQuestPoint(shopItem.getPointQuestID(), -price);
                } else {
                    player.gainQuestPoint(shopItem.getPointQuestID(), -price);
                }
            }
            if (shopItem.getPrice() > 0) {
                player.gainMeso(-(ItemConstants.類型.可充值道具(itemId) ? shopItem.getPrice() : (shopItem.getPrice() * quantity)), false);
                FileoutputUtil.logToFile("logs/Data/商店購買.txt", "時間: " + FileoutputUtil.NowTime2() + " 帳號: " + c.getAccountName() + " 消耗楓幣: " + shopItem.getPrice() + "元 購買: " + quantity + "個 " + getItemName(itemId) + itemId + "\r\n");
            }
            if (shopItem.getBuyLimit() > 0 || shopItem.getBuyLimitWorldAccount() > 0) {
                long resetTime = 0;
                if (shopItem.getResetType() == 2) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    resetTime = cal.getTimeInMillis();
                } else if (shopItem.getResetType() == 3) {
                    long timeNow = System.currentTimeMillis();
                    for (long timeRS : shopItem.getResetInfo()) {
                        if (timeRS <= timeNow) {
                            continue;
                        }
                        resetTime = timeRS;
                        break;
                    }
                }
                if (shopItem.getBuyLimit() > 0) {
                    player.setBuyLimit(id, itemId, quantity, resetTime);
                    final int buyLimit = player.getBuyLimit(id, itemId);
                    c.announce(NPCPacket.SetBuyLimitCount(id, position, itemId, buyLimit, resetTime));
                }
                if (shopItem.getBuyLimitWorldAccount() > 0) {
                    player.setAccountBuyLimit(id, itemId, quantity, resetTime);
                    final int buyLimit = player.getAccountBuyLimit(id, itemId);
                    c.announce(NPCPacket.SetBuyLimitCount(id, position, itemId, buyLimit, resetTime));
                }
            }
            if (ItemConstants.類型.可充值道具(itemId)) {
                quantity = ii.getSlotMax(shopItem.getItemId());
            }
            MapleInventoryManipulator.addById(c, itemId, quantity * shopItem.getQuantity(), shopItem.getPeriod(), shopItem.getPotentialGrade(), "商店購買 " + id + ", " + npcId + " 時間 " + DateUtil.getCurrentDate());
//            long price = ItemConstants.類型.可充值道具(itemId) ? shopItem.getPrice() : (shopItem.getPrice() * quantity);
//            long meso = shopItem.getPointQuestID() == 0 ? c.getPlayer().getMeso() : c.getPlayer().getPQPoint();
//            if (price >= 0 && meso >= price) {
//                if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
//                    if (shopItem.getPointQuestID() == 0) {
//                        c.getPlayer().gainMeso(-price, false);
//                    } else {
//                        c.getPlayer().gainPQPoint(-price);
//                    }
//                    if (ItemConstants.類型.寵物(itemId)) {
//                        MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1, "Bought from shop " + id + ", " + npcId + " on " + DateUtil.getCurrentDate());
//                    } else {
//                        if (!ItemConstants.類型.可充值道具(itemId)) {
//                            int state = shopItem.getPotentialGrade();
//                            long period = shopItem.getPeriod();
//                            MapleInventoryManipulator.addById(c, itemId, quantity, period, state, "商店購買 " + id + ", " + npcId + " 時間 " + DateUtil.getCurrentDate());
//                        } else {
//                            quantity = ii.getSlotMax(shopItem.getItemId());
//                            MapleInventoryManipulator.addById(c, itemId, quantity, "商店購買 " + id + ", " + npcId + " 時間 " + DateUtil.getCurrentDate());
//                        }
//                    }
//                    c.announce(NPCPacket.confirmShopTransaction(MapleShopResponse.ShopRes_BuySuccess, this, c, -1));
//                } else {
//                    c.announce(NPCPacket.confirmShopTransaction(MapleShopResponse.ShopRes_NotEnoughSpace, this, c, -1));
//                }
//            }
//        } else if (shopItem.getTokenItemID() > 0 && shopItem.getTokenPrice() > 0 && c.getPlayer().haveItem(shopItem.getTokenItemID(), shopItem.getTokenPrice() * quantity, false, true)) {
//            if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
//                MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(shopItem.getTokenItemID()), shopItem.getTokenItemID(), shopItem.getTokenPrice() * quantity, false, false);
//                if (ItemConstants.類型.寵物(itemId)) {
//                    MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1, "商店購買 " + id + ", " + npcId + " 時間 " + DateUtil.getCurrentDate());
//                } else {
//                    if (!ItemConstants.類型.可充值道具(itemId)) {
//                        int state = shopItem.getPotentialGrade();
//                        long period = shopItem.getPeriod();
//                        MapleInventoryManipulator.addById(c, itemId, quantity, period, state, "商店購買 " + id + ", " + npcId + " 時間 " + DateUtil.getCurrentDate());
//                    } else {
//                        quantity = ii.getSlotMax(shopItem.getItemId());
//                        MapleInventoryManipulator.addById(c, itemId, quantity, "商店購買 " + id + ", " + npcId + " 時間 " + DateUtil.getCurrentDate());
//                    }
//                }
//                c.announce(NPCPacket.confirmShopTransaction(MapleShopResponse.ShopRes_BuySuccess, this, c, -1));
//            } else {
//                c.announce(NPCPacket.confirmShopTransaction(MapleShopResponse.ShopRes_NotEnoughSpace, this, c, -1));
//            }
        }
        c.announce(NPCPacket.confirmShopTransaction(response, this, c, index, itemId));
    }

    /*
     * 賣出道具
     */
    public void sell(MapleClient c, MapleInventoryType type, short slot, short quantity) {
        if (c.getPlayer().isScriptShop()) {
            c.getPlayer().dropMessage(1, "這個商店無法出售道具。");
            c.announce(NPCPacket.confirmShopTransaction(MapleShopResponse.ShopRes_SellSuccess, this, c, -1, -1));
            return;
        }
        if (quantity <= 0) {
            quantity = 1;
        }
        Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item == null) {
            return;
        }
        if (ItemConstants.類型.飛鏢(item.getItemId()) || ItemConstants.類型.子彈(item.getItemId())) {
            quantity = item.getQuantity();
        }
        if (item.getItemId() == 4000463) {
            c.getPlayer().dropMessage(1, "該道具無法賣出.");
            c.announce(NPCPacket.confirmShopTransaction(MapleShopResponse.ShopRes_SellSuccess, this, c, -1, -1));
            return;
        }
        if (quantity < 0) {
            AutobanManager.getInstance().addPoints(c, 1000, 0, "賣出道具 " + quantity + " " + item.getItemId() + " (" + type.name() + "/" + slot + ")");
            return;
        }
        if (ItemConstants.類型.可充值道具(item.getItemId())) {
            quantity = item.getQuantity();
        }
        short iQuant = item.getQuantity(); //當前道具的數量
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.cantSell(item.getItemId()) || ItemConstants.類型.寵物(item.getItemId())) {
            return;
        }
        if (quantity <= iQuant && (iQuant > 0 || ItemConstants.類型.可充值道具(item.getItemId()))) {
            double price;
            if (ItemConstants.類型.飛鏢(item.getItemId()) || ItemConstants.類型.子彈(item.getItemId())) {
                price = ii.getUnitPrice(item.getItemId()) / (double) ii.getSlotMax(item.getItemId());
            } else {
                price = ii.getPrice(item.getItemId());
            }
            long recvMesos = (long) Math.ceil(price * quantity);
            if (c.getPlayer().getMeso() + recvMesos > ServerConfig.CHANNEL_PLAYER_MAXMESO) {
                c.getPlayer().dropMessage(1, "攜帶楓幣不能超過" + ServerConfig.CHANNEL_PLAYER_MAXMESO + ".");
                c.announce(NPCPacket.confirmShopTransaction(MapleShopResponse.ShopRes_SellSuccess, this, c, -1, -1));
                return;
            }
            List<MapleShopItem> rebuy = c.getPlayer().getRebuy();
            if (item.getQuantity() == quantity) { //賣掉的數量等於道具擁有的數量
                rebuy.add(new MapleShopItem(item.copy(), recvMesos, item.getQuantity())); //複製所有的道具信息
            } else {
                rebuy.add(new MapleShopItem(item.copyWithQuantity(quantity), recvMesos, quantity)); //複製賣出道具數量的信息
            }
            if (rebuy.size() > 10) {
                rebuy.remove(0);
            }
            MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
            if (price != -1.0 && recvMesos > 0) {
                c.getPlayer().gainMeso(recvMesos, false);
            }
            c.announce(NPCPacket.confirmShopTransaction(MapleShopResponse.ShopRes_SellSuccess, this, c, -1, -1)); //賣出道具
        }
    }

    /*
     * 對飛鏢.子彈進行充值
     */
    public void recharge(MapleClient c, short slot) {
        if (c.getPlayer().isScriptShop()) {
            c.getPlayer().dropMessage(1, "這個商店無法儲值道具。");
            c.announce(NPCPacket.confirmShopTransaction(MapleShopResponse.ShopRes_RechargeSuccess, this, c, -1, -1));
            return;
        }
        MapleShopResponse response = MapleShopResponse.ShopRes_RechargeSuccess;
        Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (item == null || (!ItemConstants.類型.飛鏢(item.getItemId()) && !ItemConstants.類型.子彈(item.getItemId()))) {
            c.announce(NPCPacket.confirmShopTransaction(MapleShopResponse.ShopRes_RechargeIncorrectRequest, this, c, -1, -1));
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        short slotMax = ii.getSlotMax(item.getItemId());
        MapleStatEffect effect = null;
        if (ItemConstants.類型.飛鏢(item.getItemId())) {
            effect = c.getPlayer().getSkillEffect(夜使者.精準暗器);
            if (effect == null) {
                effect = c.getPlayer().getSkillEffect(暗夜行者.投擲精通);
            }
        } else if (ItemConstants.類型.子彈(item.getItemId())) {
            effect = c.getPlayer().getSkillEffect(槍神.精通槍法);
        }
        if (effect != null) {
            slotMax += effect.getY();
        }
        if (item.getQuantity() >= slotMax) {
            response = MapleShopResponse.ShopRes_RechargeIncorrectRequest;
        }
        int price = (int) Math.round(ii.getUnitPrice(item.getItemId()) * (slotMax - item.getQuantity()));
        if (c.getPlayer().getMeso() < price) {
            response = MapleShopResponse.ShopRes_RechargeNoMoney;
        }
        if (response == MapleShopResponse.ShopRes_RechargeSuccess) {
            item.setQuantity(slotMax);
            c.getPlayer().gainMeso(-price, false, false);
            c.announce(InventoryPacket.modifyInventory(false, Collections.singletonList(new ModifyInventory(1, item)))); //發送更新道具數量的封包
        }
        c.announce(NPCPacket.confirmShopTransaction(response, this, c, -1, -1)); //充值飛鏢/子彈
    }

    protected MapleShopItem findBySlotAndId(int itemId, int slot) {
        MapleShopItem shopItem = items.get(slot);
        if (shopItem != null && shopItem.getItemId() == itemId) {
            return shopItem;
        }
        return null;
    }

    protected MapleShopItem findBySlotAndId(MapleClient c, int itemId, int pos) {
        List<MapleShopItem> items = getItems(c);
        if (pos >= items.size()) {
            return null;
        }
        MapleShopItem shopItem = items.get(pos);
        //System.err.println("查找商店道具 - 道具ID: " + shopItem.getItem() + " 是否符合: " + (shopItem.getItem() == itemId));
        if (shopItem != null && shopItem.getItemId() == itemId) {
            return shopItem;
        }
        return null;
    }

    /*
     * 創建1個新的商店
     */
//    public static MapleShop createFromDB(int id, boolean isShopId) {
//        MapleShop ret = null;
//        int shopId;
//        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
//        try {
//            Connection con = DatabaseConnection.getConnection();
//            PreparedStatement ps = con.prepareStatement(isShopId ? "SELECT * FROM shops WHERE shopid = ?" : "SELECT * FROM shops WHERE npcid = ?");
//            ps.setInt(1, id);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                shopId = rs.getInt("shopid");
//                ret = new MapleShop(shopId, rs.getInt("npcid"));
//                rs.close();
//                ps.close();
//            } else {
//                rs.close();
//                ps.close();
//                return null;
//            }
//            ps = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
//            ps.setInt(1, shopId);
//            rs = ps.executeQuery();
//            List<Integer> recharges = new ArrayList<>(rechargeableItems);
//            while (rs.next()) {
//                if (!ii.itemExists(rs.getInt("itemid")) || blockedItems.contains(rs.getInt("itemid"))) {
//                    continue;
//                }
//                if (ItemConstants.類型.飛鏢(rs.getInt("itemid")) || ItemConstants.類型.子彈(rs.getInt("itemid"))) {
//                    MapleShopItem starItem = new MapleShopItem((short) 1, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("reqitem"), rs.getInt("reqitemq"), rs.getInt("period"), rs.getInt("state"), rs.getInt("category"), rs.getInt("minLevel"), (short) 0);
//                    ret.addItem(starItem);
//                    if (rechargeableItems.contains(starItem.getItem())) {
//                        recharges.remove(Integer.valueOf(starItem.getItem()));
//                    }
//                } else {
//                    ret.addItem(new MapleShopItem((short) 1000, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("reqitem"), rs.getInt("reqitemq"), rs.getInt("period"), rs.getInt("state"), rs.getInt("category"), rs.getInt("minLevel"), (short) 0));
//                }
//            }
//            for (Integer recharge : recharges) {
//                ret.addItem(new MapleShopItem((short) 1, recharge, 0, 0, 0, 0, 0, 0, 0, (short) 0));
//            }
//            rs.close();
//            ps.close();
//
//            ps = con.prepareStatement("SELECT * FROM shopranks WHERE shopid = ? ORDER BY rank ASC");
//            ps.setInt(1, shopId);
//            rs = ps.executeQuery();
//            while (rs.next()) {
//                if (!ii.itemExists(rs.getInt("itemid"))) {
//                    continue;
//                }
//                ret.ranks.add(new Pair(rs.getInt("itemid"), rs.getString("name")));
//            }
//            rs.close();
//            ps.close();
//        } catch (SQLException e) {
//            System.err.println("Could not load shop");
//        }
//        return ret;
//    }

    public int getNpcId() {
        return npcId;
    }

    public int getId() {
        return id;
    }

    public int getShopItemId() {
        return shopItemId;
    }

    public List<Pair<Integer, String>> getRanks() {
        return ranks;
    }
}
