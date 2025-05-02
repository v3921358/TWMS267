package Net.server;

import Client.*;
import Client.inventory.*;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.stat.PlayerStats;
import Config.configs.Config;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Config.constants.SkillConstants;
import Config.constants.enums.UserChatMessageType;
import Net.server.buffs.MapleStatEffect;
import Net.server.cashshop.CashItemFactory;
import Net.server.cashshop.CashItemInfo;
import Net.server.maps.AramiaFireWorks;
import Net.server.quest.MapleQuest;
import Opcode.Headler.OutHeader;
import Packet.*;
import Server.world.WorldBroadcastService;
import connection.packet.OverseasPacket;
import SwordieX.overseas.extraequip.ExtraEquipResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class MapleInventoryManipulator {

    private static final Logger log = LoggerFactory.getLogger("ItemLog");

    public static void addRing(MapleCharacter chr, int itemId, int ringId, int sn) {
        CashItemInfo csi = CashItemFactory.getInstance().getItem(sn);
        if (csi == null) {
            return;
        }
        Item ring = chr.getCashInventory().toItem(csi, ringId);
        if (ring == null || ring.getSN() != ringId || ring.getSN() <= 0 || ring.getItemId() != itemId) {
            return;
        }
        chr.getCashInventory().addToInventory(ring);
        chr.send(MTSCSPacket.CashItemBuyDone(ring, sn, chr.getClient().getAccID()));
    }

    public static boolean addbyItem(MapleClient c, Item item) {
        return addbyItem(c, item, false) >= 0;
    }

    public static short addbyItem(MapleClient c, Item item, boolean fromcs) {
        if (item.getSN() <= 0) {
            item.setSN(getUniqueId(item.getItemId(), item.getSN()));
        }
        MapleInventoryType type = ItemConstants.getInventoryType(item.getItemId());
        short newSlot = c.getPlayer().getInventory(type).addItem(item);
        if (newSlot == -1) {
            if (!fromcs) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(InventoryPacket.getShowInventoryFull());
            }
            return newSlot;
        }
        if (ItemConstants.類型.採集道具(item.getItemId())) {
            c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
        }
        if (item.hasSetOnlyId()) {
            item.setSN(MapleInventoryIdentifier.getInstance());
        }
        if (ItemConstants.類型.秘法符文(item.getItemId())) {
            ((Equip) item).setNewArcInfo(c.getPlayer().getJob());
        }
        if (ItemConstants.類型.真實符文(item.getItemId())) {
            ((Equip) item).setNewAutInfo(c.getPlayer().getJob());
        }
        if (ItemConstants.類型.寵物(item.getItemId())) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MaplePet pet = item.getPet();
            if (pet == null) {
                pet = MaplePet.createPet(item.getItemId(), item.getSN());
                item.setPet(pet);
                pet.setInventoryPosition(newSlot);
            }
            item.setSN(MapleInventoryManipulator.getUniqueId(item.getItemId(), pet == null ? item.getSN() : pet.getUniqueId()));
            //設置寵物時間
            if (ii.getLife(item.getItemId()) == 0) {//永恆寵物時間
                item.setExpiration(-1);
            } else if (item.getExpiration() <= 0 && item.getExpiration() != -1) {
                item.setExpiration(System.currentTimeMillis() + (ii.getLife(item.getItemId()) * 24 * 60 * 60 * 1000L));
            }
            c.announce(InventoryPacket.modifyInventory(false, Collections.singletonList(new ModifyInventory(0, item))));
        } else {
            c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, item))));
        }
        c.getPlayer().havePartyQuest(item.getItemId());
        if (!fromcs && (type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.DECORATION))) {
            c.getPlayer().checkCopyItems();
        }
        return newSlot;
    }

    public static int getUniqueId(int itemId, int uniqueid) {
        if (uniqueid > -1) {
            return uniqueid;
        } else if (ItemConstants.getInventoryType(itemId) == MapleInventoryType.CASH || MapleItemInformationProvider.getInstance().isCash(itemId)) { //less work to do
            uniqueid = MapleInventoryIdentifier.getInstance(); //shouldnt be generated yet, so put it here
        }
        return uniqueid;
    }

    public static boolean addById(MapleClient c, int itemId, int quantity, String gmLog) {
        return addById(c, itemId, quantity, null, null, 0, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, int quantity, int state, String gmLog) {
        return addById(c, itemId, quantity, null, null, 0, state, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, int quantity, long period, String gmLog) {
        return addById(c, itemId, quantity, null, null, period, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, int quantity, long period, int state, String gmLog) {
        return addById(c, itemId, quantity, null, null, period, state, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, int quantity, String owner, String gmLog) {
        return addById(c, itemId, quantity, owner, null, 0, 0, gmLog);
    }

    public static byte addId(MapleClient c, int itemId, int quantity, String owner, String gmLog) {
        return addId(c, itemId, quantity, owner, null, 0, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, int quantity, String owner, MaplePet pet, String gmLog) {
        return addById(c, itemId, quantity, owner, pet, 0, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, int quantity, String owner, MaplePet pet, long period, String gmLog) {
        return addById(c, itemId, quantity, owner, pet, period, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, int quantity, String owner, MaplePet pet, long period, int state, String gmLog) {
        return addId(c, itemId, quantity, owner, pet, period, state, gmLog) >= 0;
    }

    public static byte addId(MapleClient c, int itemId, int quantity, String owner, MaplePet pet, long period, int state, String gmLog) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) || (!ii.itemExists(itemId))) {
            c.announce(InventoryPacket.getInventoryFull());
            c.announce(InventoryPacket.showItemUnavailable());
            return -1;
        }
        if (ItemConstants.類型.寵物(itemId) && pet == null) {
            if (c.getPlayer().isDebug()) {
                c.getPlayer().dropMessage(6, "增加道具出錯, 道具是寵物, 可是沒有傳入寵物實例。");
            }
            return -1;
        }

        long comparePeriod = -1;
        if (period > 0) {
            long cur = System.currentTimeMillis();
            if (period < 1000) {
                period = period * 24 * 60 * 60 * 1000 + cur;
            } else if (period < cur) {
                period = period + cur;
            }
            comparePeriod = (period / 1000) * 1000;
        }

        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        int uniqueid = getUniqueId(itemId, pet == null ? -1 : pet.getUniqueId());
        short newSlot = -1;
        if (!type.equals(MapleInventoryType.EQUIP) && !type.equals(MapleInventoryType.DECORATION)) { //如果不是裝備道具
            short slotMax = ii.getSlotMax(itemId);
            List<Item> existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!ItemConstants.類型.可充值道具(itemId)) {
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            Item eItem = i.next();
                            short oldQ = eItem.getQuantity();
                            if (oldQ < slotMax && (eItem.getOwner().equals(owner) || owner == null) && eItem.getExpiration() == comparePeriod) {
                                short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                eItem.setQuantity(newQ);
                                c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(1, eItem))));
                                newSlot = eItem.getPosition();
                            }
                        } else {
                            break;
                        }
                    }
                }
                Item nItem;
                // add new slots if there is still something left
                while (quantity > 0) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    if (newQ != 0) {
                        quantity -= newQ;
                        nItem = new Item(itemId, (byte) 0, newQ, 0, uniqueid, (short) 0);
                        newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                        if (newSlot == -1) {
                            c.announce(InventoryPacket.getInventoryFull());
                            c.announce(InventoryPacket.getShowInventoryFull());
                            return -1;
                        }
                        if (gmLog != null) {
                            nItem.setGMLog(gmLog);
                        }
                        if (owner != null) {
                            nItem.setOwner(owner);
                        }
                        if (period > 0) {
                            nItem.setExpiration(period);
                        }
                        if (pet != null) {
                            nItem.setPet(pet);
                            pet.setInventoryPosition(newSlot);
                        }
                        c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nItem))));
                        if (ItemConstants.類型.可充值道具(itemId) && quantity == 0) {
                            break;
                        }
                    } else {
                        c.getPlayer().havePartyQuest(itemId);
                        c.sendEnableActions();
                        return (byte) newSlot;
                    }
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                Item nItem = new Item(itemId, (byte) 0, (short) Math.min(quantity, slotMax), 0, uniqueid, (short) 0);
                newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    c.announce(InventoryPacket.getInventoryFull());
                    c.announce(InventoryPacket.getShowInventoryFull());
                    return -1;
                }
                if (period > 0) {
                    nItem.setExpiration(period);
                }
                if (gmLog != null) {
                    nItem.setGMLog(gmLog);
                }
                c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nItem))));
            }
        } else {
            //這個是裝備道具
            if (quantity == 1) {
                Item nEquip = ii.getEquipById(itemId, uniqueid);
                if (owner != null) { //設置裝備所有者
                    nEquip.setOwner(owner);
                }
                if (gmLog != null) { //設置裝備獲得日誌
                    nEquip.setGMLog(gmLog);
                }
                if (period > 0) { //設置到期時間
                    nEquip.setExpiration(period);
                }
                if (state > 0) { //設置裝備潛能
                    ii.setPotentialState((Equip) nEquip, state);
                }
                if (nEquip.hasSetOnlyId()) {
                    nEquip.setSN(MapleInventoryIdentifier.getInstance());
                }
                if (ItemConstants.類型.秘法符文(nEquip.getItemId())) {
                    ((Equip) nEquip).setNewArcInfo(c.getPlayer().getJob());
                }
                if (ItemConstants.類型.真實符文(nEquip.getItemId())) {
                    ((Equip) nEquip).setNewAutInfo(c.getPlayer().getJob());
                }
                newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
                if (newSlot == -1) {
                    c.announce(InventoryPacket.getInventoryFull());
                    c.announce(InventoryPacket.getShowInventoryFull());
                    return -1;
                }
                c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nEquip))));
                if (ItemConstants.類型.採集道具(itemId)) {
                    c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
                }
                c.getPlayer().checkCopyItems();
            } else {
                throw new InventoryException("Trying to create equip with non-one quantity");
            }
        }
        c.getPlayer().havePartyQuest(itemId);
        return (byte) newSlot;
    }

    public static Item addbyId_Gachapon(MapleClient c, int itemId, int quantity) {
        return addbyId_Gachapon(c, itemId, quantity, null, 0);
    }

    public static Item addbyId_Gachapon(MapleClient c, int itemId, int quantity, String gmLog) {
        return addbyId_Gachapon(c, itemId, quantity, null, 0);
    }

    public static Item addbyId_Gachapon(MapleClient c, int itemId, int quantity, String gmLog, long period) {
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.USE).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.ETC).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.SETUP).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.DECORATION).getNextFreeSlot() == -1) {
            return null;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) || (!ii.itemExists(itemId))) {
            c.announce(InventoryPacket.getInventoryFull());
            c.announce(InventoryPacket.showItemUnavailable());
            return null;
        }

        long comparePeriod = -1;
        if (period > 0) {
            if (period < 1000) {
                period = period * 24 * 60 * 60 * 1000 + System.currentTimeMillis();
            } else {
                period = period + System.currentTimeMillis();
            }
            comparePeriod = (period / 1000) * 1000;
        }

        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        if (!type.equals(MapleInventoryType.EQUIP) && !type.equals(MapleInventoryType.DECORATION)) {
            short slotMax = ii.getSlotMax(itemId);
            List<Item> existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!ItemConstants.類型.可充值道具(itemId)) {
                Item nItem = null;
                boolean recieved = false;
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            nItem = i.next();
                            short oldQ = nItem.getQuantity();
                            if (oldQ < slotMax && (nItem.getOwner() == null || nItem.getOwner().isEmpty()) && nItem.getExpiration() == comparePeriod) {
                                recieved = true;
                                short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                nItem.setQuantity(newQ);
                                c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(1, nItem))));
                            }
                        } else {
                            break;
                        }
                    }
                }
                // add new slots if there is still something left
                while (quantity > 0) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    if (newQ != 0) {
                        quantity -= newQ;
                        nItem = new Item(itemId, (byte) 0, newQ, 0, getUniqueId(itemId, -1), (short) 0);
                        short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                        if (newSlot == -1 && recieved) {
                            return nItem;
                        } else if (newSlot == -1) {
                            return null;
                        }
                        recieved = true;
                        if (gmLog != null) { //設置裝備獲得日誌
                            nItem.setGMLog(gmLog);
                        }
                        if (period > 0) { //設置到期時間
                            nItem.setExpiration(period);
                        }
                        c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nItem))));
                        if (ItemConstants.類型.可充值道具(itemId) && quantity == 0) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (recieved && nItem != null) {
                    c.getPlayer().havePartyQuest(nItem.getItemId());
                    return nItem;
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                Item nItem = new Item(itemId, (byte) 0, (short) Math.min(quantity, slotMax), 0, getUniqueId(itemId, -1), (short) 0);
                short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    return null;
                }
                if (gmLog != null) { //設置裝備獲得日誌
                    nItem.setGMLog(gmLog);
                }
                if (period > 0) { //設置到期時間
                    nItem.setExpiration(period);
                }
                c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nItem))));
                c.getPlayer().havePartyQuest(nItem.getItemId());
                return nItem;
            }
        } else {
            //這個是裝備道具  裝備道具只能數量為 1
            if (quantity == 1) {
                Item nEquip = ii.randomizeStats(ii.getEquipById(itemId));
                short newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
                if (newSlot == -1) {
                    return null;
                }
                if (gmLog != null) { //設置裝備獲得日誌
                    nEquip.setGMLog(gmLog);
                }
                if (period > 0) { //設置到期時間
                    nEquip.setExpiration(period);
                }
                if (nEquip.hasSetOnlyId()) {
                    nEquip.setSN(MapleInventoryIdentifier.getInstance());
                }
                c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nEquip))));
                c.getPlayer().havePartyQuest(nEquip.getItemId());
                return nEquip;
            } else {
                throw new InventoryException("Trying to create equip with non-one quantity");
            }
        }
        return null;
    }

    public static boolean addFromDrop(MapleClient c, Item item, boolean show) {
        return addFromDrop(c, item, show, false);
    }

    public static boolean addFromDrop(MapleClient c, Item item, boolean show, boolean enhance) {
        return addFromDrop(c, item, show, enhance, true);
    }

    public static boolean addFromDrop(MapleClient c, Item item, boolean show, boolean enhance, boolean updateTick) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (c.getPlayer() == null || (ii.isPickupRestricted(item.getItemId()) && c.getPlayer().haveItem(item.getItemId(), 1, true, false)) || (!ii.itemExists(item.getItemId()))) {
            c.announce(InventoryPacket.getInventoryFull());
            c.announce(InventoryPacket.showItemUnavailable());
            return false;
        }
        int before = c.getPlayer().itemQuantity(item.getItemId());
        short quantity = item.getQuantity();
        MapleInventoryType type = ItemConstants.getInventoryType(item.getItemId());
        if (ItemConstants.類型.寵物(item.getItemId())) {
            if (quantity == 1) {
                long period = item.getTrueExpiration();
                MaplePet pet = item.getPet();
                if (pet == null) {
                    pet = MaplePet.createPet(item.getItemId());
                    if (pet != null && period == -1 && ii.getLife(item.getItemId()) > 0) {
                        period = System.currentTimeMillis() + (ii.getLife(item.getItemId()) * 24 * 60 * 1000L);
                    }
                    item.setPet(pet);
                    item.setExpiration(period);
                }
                short newSlot = c.getPlayer().getInventory(type).addItem(item);
                if (newSlot == -1) {
                    c.announce(InventoryPacket.getInventoryFull());
                    c.announce(InventoryPacket.getShowInventoryFull());
                    return false;
                }
                if (item.getPet() != null) {
                    item.getPet().setInventoryPosition(item.getPosition());
                }
                c.announce(InventoryPacket.modifyInventory(updateTick, Collections.singletonList(new ModifyInventory(0, item)), c.getPlayer()));
            } else {
                throw new RuntimeException("玩家[" + c.getPlayer().getName() + "] 獲得寵物但寵物的數量不為1 寵物ID: " + item.getItemId());
            }
        } else if (!type.equals(MapleInventoryType.EQUIP) && !type.equals(MapleInventoryType.DECORATION)) {
            short slotMax = ii.getSlotMax(item.getItemId());
            List<Item> existing = c.getPlayer().getInventory(type).listById(item.getItemId());
            if (!ItemConstants.類型.可充值道具(item.getItemId())) {
                if (quantity <= 0) { //wth
                    c.announce(InventoryPacket.getInventoryFull());
                    c.announce(InventoryPacket.showItemUnavailable());
                    return false;
                }
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            Item eItem = i.next();
                            short oldQ = eItem.getQuantity();
                            if (oldQ < slotMax && item.getOwner().equals(eItem.getOwner()) && item.getExpiration() == eItem.getExpiration() && item.getFamiliarCard() == null) {
                                short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                eItem.setQuantity(newQ);
                                eItem.setSN(item.getSN());
                                c.announce(InventoryPacket.modifyInventory(updateTick, Collections.singletonList(new ModifyInventory(1, eItem)), c.getPlayer()));
                            }
                        } else {
                            break;
                        }
                    }
                }
                // add new slots if there is still something left
                while (quantity > 0) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    quantity -= newQ;
                    Item nItem = new Item(item.getItemId(), (byte) 0, newQ, item.getAttribute(), getUniqueId(item.getItemId(), item.getSN()), (short) 0);
                    nItem.setExpiration(item.getTrueExpiration());
                    nItem.setOwner(item.getOwner());
                    nItem.setGMLog(item.getGMLog());
                    nItem.setFamiliarCard(item.getFamiliarCard());
                    nItem.setFamiliarid(item.getFamiliarid());
                    if (item.getSN() != -1) {
                        nItem.setSN(item.getSN());
                    }
                    short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                    if (newSlot == -1) {
                        c.announce(InventoryPacket.getInventoryFull());
                        c.announce(InventoryPacket.getShowInventoryFull());
                        item.setQuantity((short) (quantity + newQ));
                        return false;
                    }
                    c.announce(InventoryPacket.modifyInventory(updateTick, Collections.singletonList(new ModifyInventory(0, nItem)), c.getPlayer()));
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                Item nItem = new Item(item.getItemId(), (byte) 0, quantity, item.getAttribute(), getUniqueId(item.getItemId(), item.getSN()), (short) 0);
                nItem.setExpiration(item.getTrueExpiration());
                nItem.setOwner(item.getOwner());
                nItem.setPet(item.getPet());
                nItem.setGMLog(item.getGMLog());
                nItem.setFamiliarCard(item.getFamiliarCard());
                nItem.setFamiliarid(item.getFamiliarid());
                if (item.getSN() != -1) {
                    nItem.setSN(item.getSN());
                }
                short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    c.announce(InventoryPacket.getInventoryFull());
                    c.announce(InventoryPacket.getShowInventoryFull());
                    return false;
                }
                if (item.getPet() != null) {
                    item.getPet().setInventoryPosition(newSlot);
                }
                c.announce(InventoryPacket.modifyInventory(updateTick, Collections.singletonList(new ModifyInventory(0, nItem)), c.getPlayer()));
                c.sendEnableActions();
            }
        } else {
            //裝備道具的數量只能為 1
            if (quantity == 1) {
                if (enhance) { //是否需要重置潛能 也就是角色剛從地上撿取怪物掉落的裝備
                    item = checkEnhanced(item, c.getPlayer());
                }
                if (ItemConstants.類型.秘法符文(item.getItemId())) {
                    ((Equip) item).setNewArcInfo(c.getPlayer().getJob());
                }
                if (ItemConstants.類型.真實符文(item.getItemId())) {
                    ((Equip) item).setNewAutInfo(c.getPlayer().getJob());
                }
                if (item.hasSetOnlyId()) {
                    item.setSN(MapleInventoryIdentifier.getInstance());
                }
                short newSlot = c.getPlayer().getInventory(type).addItem(item);
                if (newSlot == -1) {
                    c.announce(InventoryPacket.getInventoryFull());
                    c.announce(InventoryPacket.getShowInventoryFull());
                    return false;
                }
                c.announce(InventoryPacket.modifyInventory(updateTick, Collections.singletonList(new ModifyInventory(0, item)), c.getPlayer()));
                if (ItemConstants.類型.採集道具(item.getItemId())) {
                    c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
                }
                c.getPlayer().checkCopyItems();
            } else {
                throw new RuntimeException("玩家[" + c.getPlayer().getName() + "] 獲得裝備但裝備的數量不為1 裝備ID: " + item.getItemId());
            }
        }
        if (item.getQuantity() >= 50 && item.getItemId() == 2340000) {
            c.setMonitored(true);
        }
        if (before == 0) {
            switch (item.getItemId()) {
                case AramiaFireWorks.KEG_ID:
                    //c.getPlayer().dropMessage(5, "You have gained a Powder Keg, you can give this in to Aramia of Henesys.");
                    break;
                case AramiaFireWorks.SUN_ID:
                    //c.getPlayer().dropMessage(5, "You have gained a Warm Sun, you can give this in to Maple Tree Hill through @joyce.");
                    break;
                case AramiaFireWorks.DEC_ID:
                    //c.getPlayer().dropMessage(5, "You have gained a Tree Decoration, you can give this in to White Christmas Hill through @joyce.");
                    break;
            }
        }
        c.getPlayer().havePartyQuest(item.getItemId());
        switch (item.getType()) {
            case 1:
                c.announce(EffectPacket.getShowItemGain(item.getItemId(), item.getQuantity(), false));
                break;
            case 2:
            case 4:
            case 5:
            case 6:
                c.announce(MaplePacketCreator.getShowItemGain(item.getItemId(), item.getQuantity()));
                break;
        }
        return true;
    }


    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot) {
        return addItemAndEquip(c, itemId, slot, 0);
    }

    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot, boolean removeItem) {
        return addItemAndEquip(c, itemId, slot, 0, removeItem);
    }

    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot, int state) {
        return addItemAndEquip(c, itemId, slot, state, true);
    }

    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot, int state, boolean removeItem) {
        return addItemAndEquip(c, itemId, slot, null, 0, state, "系統贈送 時間: " + DateUtil.getCurrentDate(), removeItem);
    }

    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot, int state, String gmLog) {
        return addItemAndEquip(c, itemId, slot, null, 0, state, gmLog, true);
    }

    /*
     * 給玩家道具並且自動穿戴
     * c 客戶端
     * itemId 道具ID
     * slot 穿戴道具的位置
     * owner 道具的所有者日誌
     * period 道具的時間
     * state 道具的未鑒定的狀態
     * gmLog 道具的來源日誌或者操作日誌
     * removeItem 是否刪除穿戴位置也有的道具信息
     */
    public static boolean addItemAndEquip(MapleClient c, int itemId, short slot, String owner, long period, int state, String gmLog, boolean removeItem) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        if (!ii.itemExists(itemId) || (!type.equals(MapleInventoryType.EQUIP) && !type.equals(MapleInventoryType.DECORATION))) {
            c.sendEnableActions();
            return false;
        }
        Equip nEquip = ii.getEquipById(itemId);
        return addItemAndEquip(c, nEquip, slot, owner, period, state, gmLog, removeItem);
    }
    public static boolean addItemAndEquip(MapleClient c, Equip equip, short slot, String owner, long period, int state, String gmLog, boolean removeItem) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (slot > 0) {
            c.sendEnableActions();
            return false;
        }
        Item toRemove = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
        if (toRemove != null) {
            if (removeItem) {
                removeFromSlot(c, MapleInventoryType.EQUIPPED, toRemove.getPosition(), toRemove.getQuantity(), false);
            } else {
                short nextSlot = c.getPlayer().getInventory(ItemConstants.getInventoryType(toRemove.getItemId())).getNextFreeSlot();
                if (nextSlot > -1) {
                    MapleInventoryManipulator.unequip(c, toRemove.getPosition(), nextSlot);
                }
            }
        }
        if (owner != null) { //設置裝備所有者
            equip.setOwner(owner);
        }
        if (gmLog != null) { //設置裝備獲得日誌
            equip.setGMLog(gmLog);
        }
        if (period > 0) { //設置到期時間
            if (period < 1000) {
                equip.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
            } else {
                equip.setExpiration(System.currentTimeMillis() + period);
            }
        }
        if (state > 0) { //設置裝備潛能
            ii.setPotentialState(equip, state);
        }
        if (ItemConstants.類型.秘法符文(equip.getItemId())) {
            equip.setNewArcInfo(c.getPlayer().getJob());
        }
        if (ItemConstants.類型.真實符文(equip.getItemId())) {
            equip.setNewAutInfo(c.getPlayer().getJob());
        }
        if (equip.hasSetOnlyId()) {
            equip.setSN(MapleInventoryIdentifier.getInstance());
        }
        equip.setPosition(slot); //設置裝備的位置
        c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip);
        c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, equip))));
        return true;
    }

    private static Item checkEnhanced(Item before, MapleCharacter chr) {
        if (before instanceof Equip) {
            Equip eq = (Equip) before;
            if (eq.getState(false) == 0 && (eq.getRestUpgradeCount() >= 1 || eq.getCurrentUpgradeCount() >= 1) && ItemConstants.卷軸.canScroll(eq.getItemId()) && Randomizer.nextInt(100) >= 90) { //20% chance of pot?
                eq.renewPotential(false);
                //chr.dropMessage(5, "You have obtained an item with hidden Potential.");
            }
        }
        return before;
    }

    public static boolean checkSpace(MapleClient c, int itemid, int quantity, String owner) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (c == null || c.getPlayer() == null || (ii.isPickupRestricted(itemid) && c.getPlayer().haveItem(itemid, 1, true, false)) || (!ii.itemExists(itemid))) {
            return false;
        }
        if (quantity <= 0 && !ItemConstants.類型.可充值道具(itemid)) {
            return false;
        }
        MapleInventoryType type = ItemConstants.getInventoryType(itemid);
        if (c.getPlayer().getInventory(type) == null) {
            return false;
        }
        if (!type.equals(MapleInventoryType.EQUIP) && !type.equals(MapleInventoryType.DECORATION)) {
            short slotMax = ii.getSlotMax(itemid);
            List<Item> existing = c.getPlayer().getInventory(type).listById(itemid);
            if (!ItemConstants.類型.可充值道具(itemid)) {
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    for (Item eItem : existing) {
                        short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && owner != null && owner.equals(eItem.getOwner())) {
                            short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity -= (newQ - oldQ);
                        }
                        if (quantity <= 0) {
                            break;
                        }
                    }
                }
            }
            // add new slots if there is still something left
            int numSlotsNeeded;
            if (slotMax > 0 && !ItemConstants.類型.可充值道具(itemid)) {
                numSlotsNeeded = (int) (Math.ceil(((double) quantity) / slotMax));
            } else {
                numSlotsNeeded = 1;
            }
            return !c.getPlayer().getInventory(type).isFull(numSlotsNeeded - 1);
        } else {
            return !c.getPlayer().getInventory(type).isFull(quantity - 1);
        }
    }

    public static Item removeFromSlotCopy(MapleClient c, MapleInventoryType type, short slot, short quantity) {
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return null;
        }
        Item item = c.getPlayer().getInventory(type).getItem(slot);
        Item copy = null;
        if (item != null) {
            copy = item.copy();
            if (ItemConstants.類型.可充值道具(item.getItemId())) {
                quantity = item.getQuantity();
            }
            c.getPlayer().getInventory(type).removeItem(slot, quantity, false);
            if (item.getQuantity() <= 0 || item.getType() != 2) {
                c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(3, item))));
            } else {
                copy.setQuantity(quantity);
                copy.setSN(MapleInventoryIdentifier.getInstance());
                c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(1, item))));
            }
        }
        return copy;
    }

    public static boolean removeFromSlot(MapleClient c, MapleInventoryType type, short slot, short quantity, boolean fromDrop) {
        return removeFromSlot(c, type, slot, quantity, fromDrop, false);
    }

    public static boolean removeFromSlot(MapleClient c, MapleInventoryType type, short slot, short quantity, boolean fromDrop, boolean consume) {
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return false;
        }
        Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item != null) {
            /*
             * 5370000 - 黑板（7天權） - 把輸入的內容顯示在黑板上。可以在自由市場入口使用，但不能在#c市場#中使用。
             * 5370001 - 黑板（1天權） - 把輸入的內容顯示在黑板上。可以在自由市場入口使用，但不能在#c市場#中使用。
             */
            if ((item.getItemId() == 5370000 || item.getItemId() == 5370001) && c.getPlayer().getChalkboard() != null) {
                c.getPlayer().setChalkboard(null);
            }
            boolean allowZero = consume && ItemConstants.類型.可充值道具(item.getItemId());
            c.getPlayer().getInventory(type).removeItem(slot, quantity, allowZero);
            if (ItemConstants.類型.採集道具(item.getItemId())) {
                c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
            }
            if (item.getQuantity() == 0 && !allowZero) {
                c.announce(InventoryPacket.modifyInventory(fromDrop, Collections.singletonList(new ModifyInventory(3, item))));
            } else {
                c.announce(InventoryPacket.modifyInventory(fromDrop, Collections.singletonList(new ModifyInventory(1, item))));
            }
            return true;
        }
        return false;
    }

    public static boolean removeById(MapleClient c, MapleInventoryType type, int itemId, int quantity, boolean fromDrop, boolean consume) {
        int remremove = quantity;
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return false;
        }
        for (Item item : c.getPlayer().getInventory(type).listById(itemId)) {
            int theQ = item.getQuantity();
            if (remremove <= theQ && removeFromSlot(c, type, item.getPosition(), (short) remremove, fromDrop, consume)) {
                remremove = 0;
                break;
            } else if (remremove > theQ && removeFromSlot(c, type, item.getPosition(), item.getQuantity(), fromDrop, consume)) {
                remremove -= theQ;
            }
        }
        return remremove <= 0;
    }

    public static boolean removeFromSlot_Lock(MapleClient c, MapleInventoryType type, short slot, short quantity, boolean fromDrop, boolean consume) {
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return false;
        }
        Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item != null) {
            return !(ItemAttribute.Seal.check(item.getAttribute()) || ItemAttribute.TradeBlock.check(item.getCAttribute())) && removeFromSlot(c, type, slot, quantity, fromDrop, consume);
        }
        return false;
    }

    public static boolean removeById_Lock(MapleClient c, MapleInventoryType type, int itemId) {
        for (Item item : c.getPlayer().getInventory(type).listById(itemId)) {
            if (removeFromSlot_Lock(c, type, item.getPosition(), (short) 1, false, false)) {
                return true;
            }
        }
        return false;
    }

    public static void removeAllById(MapleClient c, int itemId, boolean checkEquipped) {
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        for (Item item : c.getPlayer().getInventory(type).listById(itemId)) {
            if (item != null) {
                removeFromSlot(c, type, item.getPosition(), item.getQuantity(), true, false);
            }
        }
        if (checkEquipped) {
            Item ii = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(itemId);
            if (ii != null) {
                removeFromSlot(c, MapleInventoryType.EQUIPPED, ii.getPosition(), ii.getQuantity(), true, false);
                c.getPlayer().equipChanged();
            }
        }
    }

    public static void removeAll(MapleClient c, MapleInventoryType type) {
        List<ModifyInventory> mods = new ArrayList<>();
        for (Item item : c.getPlayer().getInventory(type).list()) {
            if (item != null) {
                mods.add(new ModifyInventory(3, item));
            }
        }
        if (!mods.isEmpty()) {
            c.announce(InventoryPacket.modifyInventory(false, mods));
        }
        c.getPlayer().getInventory(type).removeAll();
    }

    public static void removeAllBySN(MapleClient c, int sn) {
        if (c.getPlayer() == null) {
            return;
        }
        boolean locked = false;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        //背包裝備中的複製信息
        List<Item> copyEquipItems = c.getPlayer().getInventory(MapleInventoryType.EQUIP).listBySN(sn);
        for (Item item : copyEquipItems) {
            if (item != null) {
                if (!locked) {
                    int flag = item.getAttribute();
                    flag |= ItemAttribute.Seal.getValue();
                    flag |= ItemAttribute.TradeBlock.getValue();
                    flag |= ItemAttribute.Crafted.getValue();
                    item.setAttribute(flag);
                    item.setOwner("複製裝備");
                    c.getPlayer().forceUpdateItem(item);
                    c.getPlayer().dropMessage(-11, "在背包中發現複製裝備[" + ii.getName(item.getItemId()) + "]已經將其鎖定。");
                    String msgtext = "玩家 " + c.getPlayer().getName() + " ID: " + c.getPlayer().getId() + " (等級 " + c.getPlayer().getLevel() + ") 地圖: " + c.getPlayer().getMapId() + " 在玩家背包中發現複製裝備[" + ii.getName(item.getItemId()) + "]已經將其鎖定。";
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] " + msgtext));
                    log.warn(msgtext + " 道具唯一ID: " + item.getSN());
                    locked = true;
                } else {
                    removeFromSlot(c, MapleInventoryType.EQUIP, item.getPosition(), item.getQuantity(), true, false);
                    c.getPlayer().dropMessage(-11, "在背包中發現複製裝備[" + ii.getName(item.getItemId()) + "]已經將其刪除。");
                }
            }
        }
        //身上裝備中的複製信息
        List<Item> copyEquipedItems = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).listBySN(sn);
        for (Item item : copyEquipedItems) {
            if (item != null) {
                if (!locked) {
                    int flag = item.getAttribute();
                    flag |= ItemAttribute.Seal.getValue();
                    flag |= ItemAttribute.TradeBlock.getValue();
                    flag |= ItemAttribute.Crafted.getValue();
                    item.setAttribute(flag);
                    item.setOwner("複製裝備");
                    c.getPlayer().forceUpdateItem(item);
                    c.getPlayer().dropMessage(-11, "在穿戴中發現複製裝備[" + ii.getName(item.getItemId()) + "]已經將其鎖定。");
                    String msgtext = "玩家 " + c.getPlayer().getName() + " ID: " + c.getPlayer().getId() + " (等級 " + c.getPlayer().getLevel() + ") 地圖: " + c.getPlayer().getMapId() + " 在玩家穿戴中發現複製裝備[" + ii.getName(item.getItemId()) + "]已經將其鎖定。";
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] " + msgtext));
                    log.warn(msgtext + " 道具唯一ID: " + item.getSN());
                    locked = true;
                } else {
                    removeFromSlot(c, MapleInventoryType.EQUIPPED, item.getPosition(), item.getQuantity(), true, false);
                    c.getPlayer().dropMessage(-11, "在穿戴中發現複製裝備[" + ii.getName(item.getItemId()) + "]已經將其刪除。");
                    c.getPlayer().equipChanged();
                }
            }
        }
        //背包時裝中的複製信息
        List<Item> copyDecorationItems = c.getPlayer().getInventory(MapleInventoryType.DECORATION).listBySN(sn);
        for (Item item : copyDecorationItems) {
            if (item != null) {
                if (!locked) {
                    int flag = item.getAttribute();
                    flag |= ItemAttribute.Seal.getValue();
                    flag |= ItemAttribute.TradeBlock.getValue();
                    flag |= ItemAttribute.Crafted.getValue();
                    item.setAttribute(flag);
                    item.setOwner("複製裝備");
                    c.getPlayer().forceUpdateItem(item);
                    c.getPlayer().dropMessage(-11, "在背包中發現複製時裝[" + ii.getName(item.getItemId()) + "]已經將其鎖定。");
                    String msgtext = "玩家 " + c.getPlayer().getName() + " ID: " + c.getPlayer().getId() + " (等級 " + c.getPlayer().getLevel() + ") 地圖: " + c.getPlayer().getMapId() + " 在玩家背包中發現複製時裝[" + ii.getName(item.getItemId()) + "]已經將其鎖定。";
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] " + msgtext));
                    log.warn(msgtext + " 道具唯一ID: " + item.getSN());
                    locked = true;
                } else {
                    removeFromSlot(c, MapleInventoryType.DECORATION, item.getPosition(), item.getQuantity(), true, false);
                    c.getPlayer().dropMessage(-11, "在背包中發現複製時裝[" + ii.getName(item.getItemId()) + "]已經將其刪除。");
                }
            }
        }
    }

    public static void move(MapleClient c, MapleInventoryType type, short src, short dst) {
        if (src < 0 || dst < 0 || src == dst || type == MapleInventoryType.EQUIPPED) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item source = c.getPlayer().getInventory(type).getItem(src);
        Item initialTarget = c.getPlayer().getInventory(type).getItem(dst);
        if (source == null) {
            c.getPlayer().dropMessage(1, "移動道具失敗，找不到移動道具的信息。");
            c.sendEnableActions();
            return;
        }
        boolean bag = false, switchSrcDst = false, bothBag = false;
        short eqIndicator = -1;
        List<ModifyInventory> mods = new ArrayList<>();
        if (dst > c.getPlayer().getInventory(type).getSlotLimit()) {
            if ((type == MapleInventoryType.ETC || type == MapleInventoryType.SETUP || type == MapleInventoryType.USE) && dst > 10000 && dst % 10000 != 0) {
                int eId = c.getPlayer().getExtendedItemId(type.getType(), (dst % 1000) / 100 - 1);
                if (eId > 0) {
                    MapleStatEffect itemEffect = ii.getItemEffect(eId);
                    boolean canMove = false;
                    switch (type.getType()) {
                        case 2:
                            switch (itemEffect.getType()) {
                                case 1:
                                    canMove = source.getItemId() / 10000 == 251;
                                    break;
                                case 2:
                                    canMove = source.getItemId() / 1000 == 2591 || (source.getItemId() / 1000000 == 2 && itemEffect.getType() == ii.getBagType(source.getItemId()));
                                    break;
                                case 3:
                                    canMove = source.getItemId() / 10000 == 204;
                                    break;
                            }
                            break;
                        case 3:
                            switch (itemEffect.getType()) {
                                case 1:
                                    canMove = source.getItemId() / 10000 == 370;
                                    break;
                                case 2:
                                    canMove = source.getItemId() / 10000 == 301 || source.getItemId() / 10000 == 302;
                                    break;
                            }
                            break;
                        case 4:
                            switch (itemEffect.getType()) {
                                case 5:
                                    canMove = source.getItemId() / 10000 == 431;
                                    break;
                                default:
                                    canMove = source.getItemId() / 1000000 == 4 && itemEffect.getType() == ii.getBagType(source.getItemId());
                                    break;
                            }
                            break;
                    }
                    if (dst % 100 > itemEffect.getSlotCount() || itemEffect.getType() <= 0 || !canMove) {
                        c.getPlayer().dropMessage(1, "無法將該道具移動到小背包.");
                        c.sendEnableActions();
                        return;
                    } else {
                        eqIndicator = 0;
                        bag = true;
                    }
                } else {
                    c.getPlayer().dropMessage(1, "無法將該道具移動到小背包.");
                    c.sendEnableActions();
                    return;
                }
            } else {
                c.getPlayer().dropMessage(1, "無法進行此操作.");
                c.sendEnableActions();
                return;
            }
        }
        if (src > c.getPlayer().getInventory(type).getSlotLimit() && (type == MapleInventoryType.ETC || type == MapleInventoryType.SETUP || type == MapleInventoryType.USE) && src > 10000 && src % 10000 != 0) {
            //source should be not null so not much checks are needed
            if (!bag) {
                switchSrcDst = true;
                eqIndicator = 0;
                bag = true;
            } else {
                bothBag = true;
            }
        }
        short olddstQ = -1;
        if (initialTarget != null) {
            olddstQ = initialTarget.getQuantity();
        }
        short oldsrcQ = source.getQuantity();
        short slotMax = ii.getSlotMax(source.getItemId());
        c.getPlayer().getInventory(type).move(src, dst, slotMax);
        if (ItemConstants.類型.採集道具(source.getItemId())) {
            c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
        }
        if (!type.equals(MapleInventoryType.EQUIP) && !type.equals(MapleInventoryType.DECORATION) && initialTarget != null && initialTarget.getItemId() == source.getItemId() && initialTarget.getOwner().equals(source.getOwner()) && initialTarget.getExpiration() == source.getExpiration() && !ItemConstants.類型.可充值道具(source.getItemId()) && !type.equals(MapleInventoryType.CASH)) {
            if ((olddstQ + oldsrcQ) > slotMax) {
                mods.add(new ModifyInventory(bag && (switchSrcDst || bothBag) ? 7 : 1, source));
                mods.add(new ModifyInventory(bag && (switchSrcDst || bothBag) ? 7 : 1, initialTarget));
            } else {
                mods.add(new ModifyInventory(bag && (switchSrcDst || bothBag) ? 8 : 3, source));
                mods.add(new ModifyInventory(bag && (!switchSrcDst || bothBag) ? 7 : 1, initialTarget));
            }
        } else {
            mods.add(new ModifyInventory(bag ? (bothBag ? 9 : 6) : 2, source, src, eqIndicator, switchSrcDst));
        }
        c.announce(InventoryPacket.modifyInventory(true, mods));
    }

    public static void equip(MapleClient c, MapleInventoryType iType, short src, short dst) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        PlayerStats statst = chr.getStat();
        Equip source = (Equip) chr.getInventory(iType).getItem(src);
        if (source == null || source.getDurability() == 0 || ItemConstants.類型.採集道具(source.getItemId())) {
            c.sendEnableActions();
            return;
        }
        if (chr.isAdmin() && Config.isDevelop()) {
            chr.dropMessage(5, "穿戴裝備  " + source.getItemId() + " src: " + src + " dst: " + dst);
        }
        /*
         * 1002140 - 維澤特帽
         * 1003142 - 霸氣·W·能力
         * 1042003 - 維澤特西裝
         * 1062007 - 維澤特西褲
         * 1322013 - 維澤特特殊提包
         * 1003824 - 9999帽子
         */
        if (source.getItemId() == 1003142 || source.getItemId() == 1002140 || source.getItemId() == 1042003 || source.getItemId() == 1062007 || source.getItemId() == 1322013 || source.getItemId() == 1003824) {
            if (!chr.isIntern()) {
                chr.dropMessage(1, "無法佩帶此物品");
                log.info("[作弊] 非管理員玩家: " + chr.getName() + " 非法穿戴GM裝備 " + source.getItemId());
                removeById(c, iType, source.getItemId(), 1, true, false);
                AutobanManager.getInstance().autoban(chr.getClient(), "非法穿戴GM裝備");
                c.sendEnableActions();
                return;
            }
        }
        if (!ii.itemExists(source.getItemId())) {
            c.sendEnableActions();
            return;
        }
        if (dst > -1200 && dst < -999 && !ItemConstants.類型.龍裝備(source.getItemId()) && !ItemConstants.類型.機械(source.getItemId())) {
            if (chr.isAdmin()) {
                chr.dropMessage(5, "穿戴裝備 - 1 " + source.getItemId());
            }
            c.sendEnableActions();
            return;
        } else if ((dst > -6000 && dst < -5002 || (dst >= -999 && dst < -99)) && !ii.isCash(source.getItemId()) && dst != -5200) {
            if (chr.isAdmin()) {
                chr.dropMessage(5, "穿戴裝備 - 2 " + source.getItemId() + " dst: " + dst + " 檢測1: " + (dst <= -1200) + " 檢測2: " + (dst >= -999 && dst < -99) + " 檢測3: " + !ii.isCash(source.getItemId()));
            }
            c.sendEnableActions();
            return;
        } else if ((dst <= -1200 && dst > -1300) && chr.getAndroid() == null) {
            if (chr.isAdmin()) {
                chr.dropMessage(5, "穿戴裝備 - 3 " + source.getItemId() + " dst: " + dst + " 檢測1: " + (dst <= -1200 && dst > -1300) + " 檢測2: " + (chr.getAndroid() == null));
            }
            c.sendEnableActions();
            return;
        } else if ((dst <= -1300 && dst > -1306) && !JobConstants.is天使破壞者(chr.getJob())) {
            if (chr.isAdmin()) {
                chr.dropMessage(5, "穿戴裝備 - 4 " + source.getItemId() + " dst: " + dst + " 檢測1: " + (dst <= -1300 && dst > -1306) + " 檢測2: " + !JobConstants.is天使破壞者(chr.getJob()));
            }
            c.sendEnableActions();
            return;
        }
        if (!ii.canEquip(source, chr.getLevel(), chr.getJob(), chr.getFame(), statst.getTotalStr(), statst.getTotalDex(), statst.getTotalLuk(), statst.getTotalInt(), chr.getStat().getLevelBonus())) {
            if (ServerConfig.WORLD_EQUIPCHECKFAME && chr.getFame() < 0) {
                chr.dropMessage(1, "人氣度小於0，無法穿戴裝備。");
            }
            c.sendEnableActions();
            return;
        }

        if (MapleWeapon.扇子.check(source.getItemId()) && dst != -10 && dst != -11 && dst != -5200) {
            c.sendEnableActions();
            return;
        } else if (ItemConstants.類型.武器(source.getItemId()) && !MapleWeapon.雙刀.check(source.getItemId()) && dst != -10 && dst != -11 && !MapleWeapon.扇子.check(source.getItemId())) {
            c.sendEnableActions();
            return;
        }

        if (dst == -23 && !GameConstants.isMountItemAvailable(source.getItemId(), chr.getJob())) {
            c.sendEnableActions();
            return;
        }
        if (dst == -118 && source.getItemId() / 10000 != 190) { //商城騎寵
            c.sendEnableActions();
            return;
        }
        if (dst == -119 && source.getItemId() / 10000 != 191) { //商城騎寵鞍子
            c.sendEnableActions();
            return;
        }
        if ((dst <= -5000 && dst >= -5002) && source.getItemId() / 10000 != 120) { //圖騰道具
            chr.dropMessage(1, "無法將此裝備佩戴這個地方，該位置只能裝備圖騰道具");
            c.sendEnableActions();
            return;
        }
        if (dst == -5200 && source.getItemId() / 10000 != 155) { // 花狐 扇子
            chr.dropMessage(1, "無法將此裝備佩戴這個地方，該位置只能裝備扇子");
            c.sendEnableActions();
            return;
        }
        if ((dst == -31) && (source.getItemId() / 10000 != 116)) {
            chr.dropMessage(1, "無法將此裝備佩戴這個地方，該位置只能裝備口袋物品道具");
            c.sendEnableActions();
            return;
        }
        if ((dst == -34) && (source.getItemId() / 10000 != 118)) {
            chr.dropMessage(1, "無法將此裝備佩戴這個地方，該位置只能裝備胸章道具");
            c.sendEnableActions();
            return;
        }
        if (dst == -36) {  //項鏈擴充的欄位 T072修改 以前為 -37
            MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
            if (stat == null || stat.getCustomData() == null || (!"0".equals(stat.getCustomData()) && Long.parseLong(stat.getCustomData()) < System.currentTimeMillis())) {
                c.sendEnableActions();
                return;
            }
        }
        if (MapleWeapon.雙刀.check(source.getItemId()) || source.getItemId() / 10000 == 135) {
            dst = (byte) (ii.isCash(source.getItemId()) ? -110 : -10); //盾牌的位置
        }
        if (ItemConstants.類型.龍裝備(source.getItemId()) && JobConstants.is龍魔導士(chr.getJob())) {
            c.sendEnableActions();
            return;
        }
        if (ItemConstants.類型.機械(source.getItemId()) && JobConstants.is機甲戰神(chr.getJob())) {
            c.sendEnableActions();
            return;
        }
        if (ii.isExclusiveEquip(source.getItemId())) { //檢測只能佩戴一種的戒指和項鏈道具
            StructExclusiveEquip exclusive = ii.getExclusiveEquipInfo(source.getItemId());
            if (exclusive != null) {
                List<Integer> theList = chr.getInventory(MapleInventoryType.EQUIPPED).listIds();
                for (Integer i : exclusive.itemIDs) {
                    if (theList.contains(i)) {
                        chr.dropMessage(1, exclusive.msg);
                        c.sendEnableActions();
                        return;
                    }
                }
            }
        }
        if (ItemConstants.類型.秘法符文(source.getItemId())) { //檢測重複佩戴秘法符文
            boolean exist = false;
            for (short i = -1605; i <= -1600; ++i) {
                Equip equiped = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
                if (equiped != null && source.getItemId() == equiped.getItemId()) {
                    exist = true;
                }
            }
            if (exist) {
                c.sendEnableActions();
                return;
            }
        }
        if (ItemConstants.類型.真實符文(source.getItemId())) { //檢測重複佩戴真實符文
            boolean exist = false;
            for (short i = -1705; i <= -1700; ++i) {
                Equip equiped = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
                if (equiped != null && source.getItemId() == equiped.getItemId()) {
                    exist = true;
                }
            }
            if (exist) {
                c.sendEnableActions();
                return;
            }
        }
        Equip target2 = null;
        switch (dst) {
            case -6: { // 褲裙Bottom
                target2 = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -5);
                if (target2 != null && ItemConstants.類型.套服(target2.getItemId())) {
                    if (chr.getInventory(iType).isFull(1)) {
                        c.announce(InventoryPacket.getInventoryFull());
                        c.announce(InventoryPacket.getShowInventoryFull());
                        return;
                    }
                } else {
                    target2 = null;
                }
                break;
            }
            case -5: { // 衣服Top
                target2 = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -6);
                if (target2 != null && ItemConstants.類型.套服(source.getItemId())) {
                    if (chr.getInventory(iType).isFull(1)) {
                        c.announce(InventoryPacket.getInventoryFull());
                        c.announce(InventoryPacket.getShowInventoryFull());
                        return;
                    }
                } else {
                    target2 = null;
                }
                break;
            }
            case -10: { // 盾牌
                target2 = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
                if (MapleWeapon.雙刀.check(source.getItemId())) {
                    if (chr.getJob() != 900 && (!JobConstants.is影武者(chr.getJob()) || target2 == null || !MapleWeapon.短劍.check(target2.getItemId()))) {
                        c.announce(InventoryPacket.getInventoryFull());
                        c.announce(InventoryPacket.getShowInventoryFull());
                        return;
                    }
                    target2 = null;
                } else if (target2 != null && ItemConstants.類型.雙手武器(target2.getItemId(), chr.getJob()) && !ItemConstants.類型.特殊副手(source.getItemId())) { //如果是雙手武器 也就是不能佩戴副手裝備的
                    if (chr.getInventory(iType).isFull(1)) {
                        c.announce(InventoryPacket.getInventoryFull());
                        c.announce(InventoryPacket.getShowInventoryFull());
                        return;
                    }
                } else {
                    target2 = null;
                }
                break;
            }
            case -11: { // 武器
                target2 = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
                if (target2 != null && ItemConstants.類型.雙手武器(source.getItemId(), chr.getJob()) && !ItemConstants.類型.特殊副手(target2.getItemId())) {
                    if (chr.getInventory(iType).isFull(1)) {
                        c.announce(InventoryPacket.getInventoryFull());
                        c.announce(InventoryPacket.getShowInventoryFull());
                        return;
                    }
                } else {
                    target2 = null;
                }
                break;
            }
        }
        source = (Equip) chr.getInventory(iType).getItem(src); // Equip
        Equip target = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst); // Currently equipping
        if (source == null) {
            c.sendEnableActions();
            return;
        }
        boolean itemChanged = false;
        if (ii.isEquipTradeBlock(source.getItemId())) { //禁止交易的裝備
            if (!ItemAttribute.TradeBlock.check(source.getAttribute())) {
                source.addAttribute(ItemAttribute.TradeBlock.getValue());
                itemChanged = true;
            }
        }
        if (ii.isCash(source.getItemId()) && ItemAttribute.TradeOnce.check(source.getAttribute()) && !ItemAttribute.TradeBlock.check(source.getAttribute())) {
            source.removeAttribute(ItemAttribute.TradeOnce.getValue());
            source.addAttribute(ItemAttribute.TradeBlock.getValue());
            itemChanged = true;
        }
        if (ItemConstants.類型.機器人(source.getItemId())) { //智能機器人
            if (source.getAndroid() == null) {
                int uid = MapleInventoryIdentifier.getInstance();
                source.setSN(uid);
                source.setAndroid(MapleAndroid.create(source.getItemId(), uid));
                source.addAttribute(ItemAttribute.AndroidActivated.getValue());
                itemChanged = true;
            }
            chr.removeAndroid();
            chr.setAndroid(source.getAndroid());
        } else if (chr.getAndroid() != null) {
            /*if (dst <= -1300) {
                chr.setAndroid(chr.getAndroid()); //respawn it
            } else */if (dst > -1300 && dst <= -1200) { // equip android
                chr.updateAndroidEquip(false, new Pair<>(source.getItemId(), source.getItemSkin()));
            }
        }
        int charmEXP = source.getCharmEXP();
        if (ii.isCash(source.getItemId()) && charmEXP <= 0) {
            if (ItemConstants.類型.帽子(source.getItemId())) {
                charmEXP = 50;
            } else if (ItemConstants.類型.眼飾(source.getItemId())) {
                charmEXP = 40;
            } else if (ItemConstants.類型.臉飾(source.getItemId())) {
                charmEXP = 40;
            } else if (ItemConstants.類型.耳環(source.getItemId())) {
                charmEXP = 40;
            } else if (ItemConstants.類型.套服(source.getItemId())) {
                charmEXP = 60;
            } else if (ItemConstants.類型.上衣(source.getItemId())) {
                charmEXP = 30;
            } else if (ItemConstants.類型.褲裙(source.getItemId())) {
                charmEXP = 30;
            } else if (ItemConstants.類型.手套(source.getItemId())) {
                charmEXP = 40;
            } else if (ItemConstants.類型.鞋子(source.getItemId())) {
                charmEXP = 40;
            } else if (ItemConstants.類型.披風(source.getItemId())) {
                charmEXP = 30;
            } else if (ItemConstants.類型.武器(source.getItemId()) && !ItemConstants.類型.副手(source.getItemId())) {
                charmEXP = 60;
            } else if (ItemConstants.類型.盾牌(source.getItemId())) {
                charmEXP = 10;
            }
        }
        if (charmEXP > 0 && !ItemAttribute.GetCharm.check(source.getAttribute())) {
            chr.getTrait(MapleTraitType.charm).addExp(charmEXP, chr);
            source.setCharmEXP((short) 0);
            source.addAttribute(ItemAttribute.GetCharm.getValue());
            itemChanged = true;
        }

        chr.getInventory(iType).removeSlot(src);
        //裝備如果有更新信息 必須在設置新位置之前就加入列表
        List<ModifyInventory> mods = new ArrayList<>();
        if (itemChanged) {
            mods.add(new ModifyInventory(3, source)); //刪除道具
            mods.add(new ModifyInventory(0, source)); //獲得道具
        }
        source.setPosition(dst);
        mods.add(new ModifyInventory(2, source, src)); //移動道具

        if (target != null) {
            chr.getInventory(MapleInventoryType.EQUIPPED).removeSlot(dst);
            target.setPosition(src);
            chr.getInventory(iType).addFromDB(target);
        }

        if (target2 != null) {
            short slot = target2.getPosition();
            chr.getInventory(MapleInventoryType.EQUIPPED).removeSlot(slot);
            target2.setPosition(target == null ? src : chr.getInventory(iType).getNextFreeSlot());
            chr.getInventory(iType).addFromDB(target2);
            mods.add(new ModifyInventory(2, target2, slot));
        }

        chr.getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);

        c.announce(InventoryPacket.modifyInventory(true, mods));

        // 內面暴風
        if (source.getItemId() == 1113228) {
            c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.equipInnerStorm(chr.getAccountID(), chr.getId())));
        }
        // 艾爾達斯的祝福
        if (source.getItemId() == 1114402 && chr.getMap() != null && chr.getMap().getBarrierArc() > 0) {
            SkillFactory.getSkill(80011993).getEffect(1).applyTo(chr);
        }

        if (ItemConstants.類型.武器(source.getItemId()) && !ii.isCash(source.getItemId())) {
            chr.dispelEffect(SecondaryStat.Booster);
            chr.dispelEffect(SecondaryStat.NoBulletConsume);
            chr.dispelEffect(SecondaryStat.SoulArrow);
            chr.dispelEffect(SecondaryStat.WeaponCharge);
            chr.dispelEffect(SecondaryStat.AssistCharge);
            chr.dispelEffect(SecondaryStat.StopForceAtomInfo);
            if (dst != -5200) {
                chr.setSoulMP(0);
            }

            if (chr.getHaku() != null) {
                if (dst == -5200) {
                    chr.getHaku().setWeapon(source.getItemId());
                    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                    mplew.writeShort(OutHeader.LP_FoxManModified.getValue());
                    mplew.writeInt(chr.getHaku().getOwner());
                    mplew.write(1);
                    mplew.writeInt(chr.getHaku().getWeapon());
                    mplew.write(0);
                    chr.getMap().broadcastMessage(mplew.getPacket());
                }
            }
        }
        if (source.getItemId() / 10000 == 190 || source.getItemId() / 10000 == 191) { //騎寵道具
            chr.dispelEffect(SecondaryStat.RideVehicle);
            chr.dispelEffect(SecondaryStat.Mechanic);
        }
        if (source.getState(false) >= 17) {
            Map<Integer, SkillEntry> skills = new HashMap<>();
            int[] potentials = {source.getPotential1(), source.getPotential2(), source.getPotential3(), source.getPotential4(), source.getPotential5(), source.getPotential6()};
            for (int i : potentials) {
                if (i > 0) {
                    List<StructItemOption> potentialInfo = ii.getPotentialInfo(i);
                    StructItemOption pot = potentialInfo.get(Math.min(potentialInfo.size() - 1, (source.getReqLevel() - 1) / 10));
                    if (pot != null && pot.get("skillID") > 0) {
                        skills.put(SkillConstants.getSkillByJob(pot.get("skillID"), chr.getJob()), new SkillEntry((byte) 1, (byte) 0, -1));
                    }
                }
            }
            chr.changeSkillLevel_Skip(skills, true);
        }
        if (source.getSocketState() >= 0x13) {
            Map<Integer, SkillEntry> skills = new HashMap<>();
            int[] sockets = {source.getSocket1(), source.getSocket2(), source.getSocket3()};
            for (int i : sockets) {
                if (i > 0) {
                    StructItemOption soc = ii.getSocketInfo(i);
                    if (soc != null && soc.get("skillID") > 0) {
                        skills.put(SkillConstants.getSkillByJob(soc.get("skillID"), chr.getJob()), new SkillEntry((byte) 1, (byte) 0, -1));
                    }
                }
            }
            chr.changeSkillLevel_Skip(skills, true);
        }
        chr.equipChanged();
    }


    public static void unequip(MapleClient c, short src, short dst) {
        final MapleCharacter player = c.getPlayer();
        if (player == null) {
            return;
        }
        Equip source = (Equip) player.getInventory(MapleInventoryType.EQUIPPED).getItem(src);
        if (dst < 0 || source == null) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType iType = ii.isCash(source.getItemId()) ? MapleInventoryType.DECORATION : MapleInventoryType.EQUIP;
        Equip target = (Equip) player.getInventory(iType).getItem(dst);
        if (target != null && src <= 0) {
            c.announce(InventoryPacket.getInventoryFull());
            return;
        }
        int sourceItemID = source.getItemId();
        player.getInventory(MapleInventoryType.EQUIPPED).removeSlot(src);
        if (target != null) {
            player.getInventory(iType).removeSlot(dst);
        }
        source.setPosition(dst);
        player.getInventory(iType).addFromDB(source);
        if (target != null) {
            target.setPosition(src);
            player.getInventory(MapleInventoryType.EQUIPPED).addFromDB(target);
        }
        if (ItemConstants.類型.武器(source.getItemId()) && !ii.isCash(source.getItemId())) {
            player.dispelEffect(SecondaryStat.Booster);
            player.dispelEffect(SecondaryStat.NoBulletConsume);
            player.dispelEffect(SecondaryStat.SoulArrow);
            player.dispelEffect(SecondaryStat.WeaponCharge);
            player.dispelEffect(SecondaryStat.AssistCharge);
            player.dispelEffect(SecondaryStat.StopForceAtomInfo);
            if (src != -5200) {
                player.setSoulMP(0);
            }

            if (player.getHaku() != null) {
                if (src == -5200) {
                    player.getHaku().setWeapon(0);
                    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                    mplew.writeShort(OutHeader.LP_FoxManModified.getValue());
                    mplew.writeInt(player.getHaku().getOwner());
                    mplew.write(1);
                    mplew.writeInt(player.getHaku().getWeapon());
                    mplew.write(0);
                    player.getMap().broadcastMessage(mplew.getPacket());
                }
            }
        } else if (source.getItemId() / 10000 == 190 || source.getItemId() / 10000 == 191) {
            player.dispelEffect(SecondaryStat.RideVehicle);
            player.dispelEffect(SecondaryStat.Mechanic);
        } else if (ItemConstants.類型.機器人(source.getItemId())) { //取消機器人
            player.removeAndroid();
        } else if (ItemConstants.類型.心臟(source.getItemId()) && player.getAndroid() != null) { //取消心臟當機器人不為空
            c.announce(AndroidPacket.removeAndroidHeart());
            player.removeAndroid();
        } else if (player.getAndroid() != null) {
            /*if (src <= -1300) {
                player.setAndroid(player.getAndroid());
            } else */
            if (src > -1300 && src <= -1200) {
                player.updateAndroidEquip(true, new Pair<>(sourceItemID, 0));
            }
        }
        if (source.getState(false) >= 17) {
            Map<Integer, SkillEntry> skills = new HashMap<>();
            int[] potentials = {source.getPotential1(), source.getPotential2(), source.getPotential3(), source.getPotential4(), source.getPotential5(), source.getPotential6()};
            for (int i : potentials) {
                if (i > 0) {
                    List<StructItemOption> potentialInfo = ii.getPotentialInfo(i);
                    StructItemOption pot = potentialInfo.get(Math.min(potentialInfo.size() - 1, (source.getReqLevel() - 1) / 10));
                    if (pot != null && pot.get("skillID") > 0) {
                        skills.put(SkillConstants.getSkillByJob(pot.get("skillID"), player.getJob()), new SkillEntry((byte) 0, (byte) 0, -1));
                    }
                }
            }
            player.changeSkillLevel_Skip(skills, true);
        }
        if (source.getSocketState() >= 0x13) {
            Map<Integer, SkillEntry> skills = new HashMap<>();
            int[] sockets = {source.getSocket1(), source.getSocket2(), source.getSocket3()};
            for (int i : sockets) {
                if (i > 0) {
                    StructItemOption soc = ii.getSocketInfo(i);
                    if (soc != null && soc.get("skillID") > 0) {
                        skills.put(SkillConstants.getSkillByJob(soc.get("skillID"), player.getJob()), new SkillEntry((byte) 0, (byte) 0, -1));
                    }
                }
            }
            player.changeSkillLevel_Skip(skills, true);
        }
        c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(2, source, src))));

        // 內面暴風
        if (source.getItemId() == 1113228) {
            c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.unequipInnerStorm(player.getId())));
        }
        // 艾爾達斯的祝福
        if (source.getItemId() == 1114402 && player.getBuffStatValueHolder(80011993) != null) {
            player.dispelBuff(80011993);
        }
        player.equipChanged();
    }

    public static boolean drop(MapleClient c, MapleInventoryType type, short src, short quantity) {
        return drop(c, type, src, quantity, false);
    }

    public static boolean drop(MapleClient c, MapleInventoryType type, short src, short quantity, boolean npcInduced) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (src < 0) {
            type = MapleInventoryType.EQUIPPED;
        }
        if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return false;
        }
        Item source = c.getPlayer().getInventory(type).getItem(src);
        if (quantity < 0 || source == null || (!npcInduced && ItemConstants.類型.寵物(source.getItemId())) || (quantity == 0 && !ItemConstants.類型.可充值道具(source.getItemId())) || c.getPlayer().inPVP()) {
            c.sendEnableActions();
            return false;
        }
        /*
         * 設置國慶紀念幣無法進行丟棄
         */
        if (!npcInduced && source.getItemId() == 4000463) {
            c.getPlayer().dropMessage(1, "該道具無法丟棄.");
            c.sendEnableActions();
            return false;
        }

        int flag = source.getCAttribute();
        if (quantity > source.getQuantity() && !ItemConstants.類型.可充值道具(source.getItemId())) {
            c.sendEnableActions();
            return false;
        }
        if (ItemAttribute.Seal.check(flag) || (quantity != 1 && (type == MapleInventoryType.EQUIP || type == MapleInventoryType.DECORATION))) { // hack
            c.sendEnableActions();
            return false;
        }
        Point dropPos = new Point(c.getPlayer().getPosition());
        c.getPlayer().getCheatTracker().checkDrop();
        if (quantity < source.getQuantity() && !ItemConstants.類型.可充值道具(source.getItemId())) {
            Item target = source.copy();
            target.setQuantity(quantity);
            source.setQuantity((short) (source.getQuantity() - quantity));
            c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(1, source)))); //發送更新道具數量的封包
            applyDrop(c, target, flag, ii, dropPos);
        } else {
            c.getPlayer().getInventory(type).removeSlot(src);
            if (ItemConstants.類型.採集道具(source.getItemId())) {
                c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
            }
            c.announce(InventoryPacket.modifyInventory(true, Collections.singletonList(new ModifyInventory(3, source)))); //發送刪除道具的封包
            if (src < 0) {
                c.getPlayer().equipChanged();
            }
            if (source.getItemId() / 10000 == 265 || source.getItemId() / 10000 == 308 || source.getItemId() / 10000 == 433) {
                if (source.getExtendSlot() > 0) {
                    c.getPlayer().getInventory(type).removeExtendedSlot(source.getExtendSlot());
                    c.getPlayer().getExtendedSlots(type.getType()).remove(source);
                }
            }
            applyDrop(c, source, flag, ii, dropPos);
        }
        return true;
    }

    private static void applyDrop(MapleClient c, Item item, int flag, MapleItemInformationProvider ii, Point dropPos) {
        if (ItemConstants.類型.寵物(item.getItemId()) || ItemAttribute.TradeBlock.check(flag) || ii.isDropRestricted(item.getItemId()) || ii.isAccountShared(item.getItemId()) || ItemAttribute.AccountSharable.check(flag)) {
            if (ItemAttribute.TradeOnce.check(flag) && !ItemAttribute.AccountSharable.check(flag)) {
                flag &= ~ItemAttribute.TradeOnce.getValue();
                if (ItemAttribute.CutUsed.check(flag)) {
                    flag &= ~ItemAttribute.CutUsed.getValue();
                }
                item.setAttribute(flag);
                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, dropPos, true, true);
            } else {
                c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), item, dropPos);
            }
        } else {
            c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, dropPos, true, true);
        }
    }

    public static void updateItem(MapleClient c, List<? extends Item> items, boolean b) {
        if (c.getPlayer() == null) {
            return;
        }
        List<ModifyInventory> collect = items.stream().map(it -> new ModifyInventory(0, it)).collect(Collectors.toList());
        c.announce(InventoryPacket.modifyInventory(b, collect));
    }

//    public static void drop(MapleClient c, MapleInventoryType type, short src, short quantity, boolean npcInduced, ) {
}
