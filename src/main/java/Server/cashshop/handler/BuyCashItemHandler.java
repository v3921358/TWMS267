/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.cashshop.handler;

import Client.MapleCharacter;
import Client.MapleCharacterUtil;
import Client.MapleClient;
import Client.MapleQuestStatus;
import Client.inventory.*;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.enums.CashItemRequestType;
import Config.constants.enums.CashItemResult;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Net.server.*;
import Net.server.cashshop.CashItemFactory;
import Net.server.cashshop.CashItemInfo;
import Net.server.cashshop.CashShop;
import Net.server.quest.MapleQuest;
import Opcode.Headler.OutHeader;
import Packet.MTSCSPacket;
import Packet.MaplePacketCreator;
import Server.channel.ChannelServer;
import Server.world.WorldFindService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.*;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Hertz
 */
public class BuyCashItemHandler {
    private static final Logger log = LoggerFactory.getLogger(BuyCashItemHandler.class);

    public static void CashShopCashItemRequest(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        CashShop cs = chr.getCashInventory();
        if (cs == null) {
            return;
        }
        int action = slea.readByte() & 0xFF;
        CashItemFactory cashinfo = CashItemFactory.getInstance();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        CashItemRequestType request = CashItemRequestType.getByAction(action);
        if (request == null) {
            c.dispose();
            log.warn("Unknown CashItemRequest 0x" + Integer.toHexString(action));
            return;
        }
        byte result = -1;
        switch (request) {
            case CashItemReq_Buy: {
                int toCharge = slea.readByte()+1; // 0 = 里程 / 1=樂豆點 / 2楓點
                boolean useMileage = slea.readBool(); // 01
                boolean onlyMileage = slea.readBool(); // 00
                int snCS = slea.readInt();//購買的道具ID
                CashItemInfo cItem = cashinfo.getItem(snCS);
                if (snCS / 100000 == 1102) {
                    toCharge = 1;
                    useMileage = true;
                    onlyMileage = true;
                }
                if (toCharge == 2) {
                    // 是否允許楓點購買
                    if (!ServerConfig.CHANNEL_ENABLEPOINTSBUY) {
                        chr.dropMessage(1, "此商品無法使用楓點購買，請選擇其他商品吧。");
                        c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                        return;
                    }
                    useMileage = false;
                }
                if (cItem.getMileageRate() == 0) {
                    useMileage = false;
                }
                if (snCS / 100000 != 1102 && (!cItem.isOnlyMileage() || !useMileage)) {
                    onlyMileage = false;
                }
                int point = cItem.getPrice();
                int mileage = onlyMileage ? point : useMileage ? (int) Math.floor(point * cItem.getMileageRate() / 100) : 0;
                point -= mileage;
                if (cItem == null) {
                    result = CashItemResult.CashItemFailReason_Unknown;
                } else if (cs.getInventory().size() >= 500) {
                    result = CashItemResult.CashItemFailReason_ItemLockerIsFull;
                } else {
                    if (cItem.onSale()) {
                        if (cItem.getLimitMax() > 0 && cItem.getLimitMax() <= getBuyLog(chr, snCS)) {
                            result = CashItemResult.CashItemFailReason_PurchaseLimitOver;
                        } else if (chr.getCSPoints(toCharge) < point || chr.getMileage() < mileage || cItem.getPrice() <= 0) {
                            result = CashItemResult.CashItemFailReason_NoRemainCash;
                        } else if (!cItem.genderEquals(chr.getGender()) && chr.getAndroid() == null) {
                            result = CashItemResult.CashItemFailReason_NoAndroid;
                        } else if (cItem.getTermStart() > 0 && cItem.getTermEnd() > 0) {
                            int day = Integer.valueOf(new SimpleDateFormat("yyyyMMddHH").format(System.currentTimeMillis()));
                            if (cItem.getTermStart() >= day || day >= cItem.getTermEnd()) {
                                result = CashItemResult.CashItemFailReason_NotSaleTerm;
                            }
                        }
                    }
                }
                if (result != -1) {
                    c.announce(MTSCSPacket.sendBuyFailed(result));
                    break;
                }
                Item item = cs.toItem(cItem, -1, "");
                if (item == null) {
                    c.announce(MTSCSPacket.sendBuyFailed(CashItemResult.CashItemFailReason_Unknown));
                    break;
                }
                if (!ii.isCash(item.getItemId()) || !chr.modifyCSPoints(toCharge, -point) || chr.modifyMileage(-mileage) != 0) {
                    break;
                }
                if (toCharge == 1 && point > 0 && mileage == 0) {
                    if (chr.modifyMileage((int) Math.ceil(point * 5 / 100), 1) == 0) {
                        // todo 獲得提示
                    }
                }
                cs.addToInventory(item);
                if (toCharge == 1) {
                    item.setAttribute(ItemAttribute.TradeOnce.getValue()); //設置道具可以交易一次
                }
                c.announce(MTSCSPacket.CashItemBuyDone(item, cItem.getSN(), c.getAccID()));
                addCashshopLog(c, cItem.getSN(), cItem.getItemId(), toCharge, point, mileage, cItem.getCount(), chr.getName() + " 購買道具: " + ii.getName(cItem.getItemId()));
                if (cItem.getLimitMax() > 0) {
                    c.announce(MTSCSPacket.購買記錄(snCS, getBuyLog(chr, cItem.getSN())));
                }
                if (item.getItemId() == 5820000) { //如果購買的是藥劑罐
                    if (chr.getPotionPot() == null) {
                        MaplePotionPot pot = MaplePotionPot.createPotionPot(chr.getId(), item.getItemId(), item.getTrueExpiration());
                        if (pot == null) {
                            chr.dropMessage(1, "創建1個新的藥劑罐出現錯誤.");
                            c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                            return;
                        }
                        chr.setPotionPot(pot);
                    }
                    c.announce(MTSCSPacket.updatePotionPot(chr.getPotionPot()));
                }
                break;
            }
            case CashItemReq_BuyDone: // 2305
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            case CashItemReq_LoadLocker: // 2306
                c.announce(MTSCSPacket.loadLockerDone(c));
                break;
            case CashItemReq_LoadWish: // 2306
                c.announce(MTSCSPacket.sendWishList(chr, false));
                c.announce(MTSCSPacket.SetItemDayTime(c));
                break;
            case CashItemReq_Couple:
            case CashItemReq_FriendShip: {
                String SecondPwd = slea.readMapleAsciiString();
                if (!c.CheckSecondPassword(SecondPwd)) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x00));
                    return;
                }
                boolean useMileage = slea.readByte() == 1;
                int snCS = slea.readInt();
                CashItemInfo item = cashinfo.getItem(snCS);
                if (item.getMileageRate() == 0) {
                    useMileage = false;
                }
                int point = item.getPrice();
                int mileage = useMileage ? (int) Math.floor(point * item.getMileageRate() / 100) : 0;
                point -= mileage;
                slea.skip(4); //[00 00 00 00] V.114.1新增
                String partnerName = slea.readMapleAsciiString();
                String msg = slea.readMapleAsciiString();
                if (item == null || !ItemConstants.類型.特效裝備(item.getItemId()) || chr.getCSPoints(1) < point || chr.getMileage() < mileage || msg.length() > 73 || msg.length() < 1) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x00));
                    return;
                } else if (!item.genderEquals(chr.getGender())) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x07));
                    return;
                } else if (chr.getCashInventory().getItemsSize() >= 100) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x18));
                    return;
                } else if (!ii.isCash(item.getItemId())) { //非商城道具
                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法購買戒指道具.");
                    return;
                }
//                if (cashinfo.isBlockedCashItemId(item.getItem()) || cashinfo.isBlockCashSnId(snCS)) {
//                    chr.dropMessage(1, "該道具禁止購買.");
//                    c.announce(MTSCSPacket.updateCouponsInfo(chr));
//                    return;
//                }
                Triple<Integer, Integer, Integer> info = MapleCharacterUtil.getInfoByName(partnerName, chr.getWorld()); //[角色ID 帳號ID 性別]
                if (info == null || info.getLeft() <= 0) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x07));
                } else if (info.getMid() == c.getAccID() || info.getLeft() == chr.getId()) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x06));
                } else {
                    if (info.getRight() == chr.getGender() && action == 0x23) {
                        c.announce(MTSCSPacket.商城錯誤提示(0x1A));
                        return;
                    }
                    int err = MapleRing.createRing(item.getItemId(), chr, partnerName, msg, info.getLeft(), item.getSN());
                    if (err != 1) {
                        c.announce(MTSCSPacket.商城錯誤提示(0x01));
                        return;
                    }
                    chr.modifyCSPoints(1, -point, false);
                    chr.modifyMileage(-mileage);
                    c.announce(MTSCSPacket.商城送禮(item.getItemId(), item.getCount(), partnerName));
                    addCashshopLog(c, item.getSN(), item.getItemId(), 1, point, mileage, item.getCount(), chr.getName() + " 購買戒指: " + ii.getName(item.getItemId()) + " 送給 " + partnerName);
                    chr.sendNote(partnerName, partnerName + " 您已收到" + chr.getName() + "送給您的禮物，請進入現金商城查看！");
                    int chz = WorldFindService.getInstance().findChannel(partnerName);
                    if (chz > 0) {
                        MapleCharacter receiver = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterByName(partnerName);
                        if (receiver != null) {
                            receiver.showNote();
                        }
                    }
                }
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr)); //刷新下免得卡住
                break;
            }
            case CashItemReq_Gift: { //點擊角色信息後選擇購物車送禮
                chr.dropMessage(1, "暫不支持，直接選了點送禮吧！");
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            }
            case CashItemReq_SetWish: { // 加入購物車    T071 OK
                chr.clearWishlist();
                if (slea.available() < 40) {
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                int[] wishlist = new int[12];
                for (int i = 0; i < 12; i++) {
                    wishlist[i] = slea.readInt();
                }
                chr.setWishlist(wishlist);
                c.announce(MTSCSPacket.sendWishList(chr, true));
                break;
            }
            case CashItemReq_IncSlotCount: { // 增加道具欄
                int toCharge = slea.readByte() + 1;
                boolean coupon = true/*slea.readByte() > 0*/; // TMS 220+ 商城只有8格的
                if (coupon) {
                    int snCS = slea.readInt();
                    CashItemInfo cItem = cashinfo.getItem(snCS);
                    if (cItem == null || cItem.getItemId() / 1000 < 9111 || cItem.getItemId() / 1000 > 9114) {
                        chr.dropMessage(1, "未知錯誤");
                        c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                        break;
                    }
                    boolean useMileage = slea.readByte() == 1;
                    boolean onlyMileage = slea.readByte() == 1;
                    if (snCS / 100000 == 1102) {
                        toCharge = 1;
                        useMileage = true;
                        onlyMileage = true;
                    }
                    if (toCharge == 2) {
                        // 是否允許楓點購買
                        if (!ServerConfig.CHANNEL_ENABLEPOINTSBUY) {
                            chr.dropMessage(1, "此商品無法使用楓點購買，請選擇其他商品吧。");
                            c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                            return;
                        }
                        useMileage = false;
                    }
                    if (cItem.getMileageRate() == 0) {
                        useMileage = false;
                    }
                    if (snCS / 100000 != 1102 && (!cItem.isOnlyMileage() || !useMileage)) {
                        onlyMileage = false;
                    }
                    int point = cItem.getPrice();
                    int mileage = onlyMileage ? point : useMileage ? (int) Math.floor(point * cItem.getMileageRate() / 100) : 0;
                    point -= mileage;
                    int types = (cItem.getItemId() - 9110000) / 1000;
                    MapleInventoryType type = MapleInventoryType.getByType((byte) types);
                    if (chr.isDebug()) {
                        System.out.println("增加道具欄  snCS " + snCS + " 擴充: " + types);
                    }
                    if (chr.getCSPoints(toCharge) >= point && chr.getMileage() >= mileage && chr.getInventory(type).getSlotLimit() < 121) {
                        chr.modifyCSPoints(toCharge, -point, false);
                        chr.modifyMileage(-mileage);
                        chr.getInventory(type).addSlot((byte) 8);
                        addCashshopLog(c, cItem.getSN(), cItem.getItemId(), 1, point, mileage, cItem.getCount(), chr.getName() + " 擴充道具欄: " + ii.getName(cItem.getItemId()));
                        c.announce(MTSCSPacket.擴充道具欄(type.getType(), chr.getInventory(type).getSlotLimit()));
                    } else {
                        chr.dropMessage(1, "擴充失敗，點數餘額不足或者欄位已超過上限。");
                    }
                } else {
                    MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
                    if (chr.getCSPoints(toCharge) >= 600 && chr.getInventory(type).getSlotLimit() < 125) {
                        chr.modifyCSPoints(toCharge, -600, false);
                        chr.getInventory(type).addSlot((byte) 4);
                        c.announce(MTSCSPacket.擴充道具欄(type.getType(), chr.getInventory(type).getSlotLimit()));
                    } else {
                        chr.dropMessage(1, "擴充失敗，點數餘額不足或者欄位已超過上限。");
                    }
                }
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            }
            case CashItemReq_IncTrunkCount: { // 增加倉庫容量
                int toCharge = slea.readByte() + 1;
                int snCS = slea.readInt();
                CashItemInfo cItem = cashinfo.getItem(snCS);
                if (cItem == null || cItem.getItemId() / 1000 != 9110) {
                    chr.dropMessage(1, "未知錯誤");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    break;
                }
                boolean useMileage = slea.readByte() == 1;
                boolean onlyMileage = slea.readByte() == 1;
                if (snCS / 100000 == 1102) {
                    toCharge = 1;
                    useMileage = true;
                    onlyMileage = true;
                }
                if (toCharge == 2) {
                    // 是否允許楓點購買
                    if (!ServerConfig.CHANNEL_ENABLEPOINTSBUY) {
                        chr.dropMessage(1, "此商品無法使用楓點購買，請選擇其他商品吧。");
                        c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                        return;
                    }
                    useMileage = false;
                }
                if (cItem.getMileageRate() == 0) {
                    useMileage = false;
                }
                if (snCS / 100000 != 1102 && (!cItem.isOnlyMileage() || !useMileage)) {
                    onlyMileage = false;
                }
                int point = cItem.getPrice();
                int mileage = onlyMileage ? point : useMileage ? (int) Math.floor(point * cItem.getMileageRate() / 100) : 0;
                point -= mileage;
                if (chr.getCSPoints(toCharge) >= point && chr.getMileage() >= mileage && chr.getTrunk().getSlots() < 128) {
                    chr.modifyCSPoints(toCharge, -point, false);
                    chr.modifyMileage(-mileage);
                    addCashshopLog(c, cItem.getSN(), cItem.getItemId(), 1, point, mileage, cItem.getCount(), chr.getName() + " 擴充倉庫: " + ii.getName(cItem.getItemId()));
                    byte slot = (byte) (128 - chr.getTrunk().getSlots());
                    if (slot > 8) {
                        slot = (byte) 8;
                    }
                    chr.getTrunk().increaseSlots(slot);
                    chr.getTrunk().saveToDB();
                    c.announce(MTSCSPacket.擴充倉庫(chr.getTrunk().getSlots()));
                } else {
                    chr.dropMessage(1, "倉庫擴充失敗，點數餘額不足或者欄位已超過上限。");
                }
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            }
            case CashItemReq_IncCharSlotCount: { // 角色增加會員卡
                int toCharge = slea.readByte() + 1;
                int snCS = slea.readInt();
                CashItemInfo item = cashinfo.getItem(snCS, false);
                int slots = c.getAccCharSlots();
                if (item == null || item.getItemId() != 5430000) {
                    String msg = "角色欄擴充失敗，找不到指定的道具信息或者道具ID不正確。";
                    if (chr.isAdmin()) {
                        msg = item == null ? "角色欄擴充失敗:\r\n找不到道具的信息或者道具沒有出售\r\n當前道具的SNid: " + snCS : "角色欄擴充失敗:\r\n道具ID是否正確: " + (item.getItemId() == 5430000) + " 當前ID：" + item.getItemId();
                    }
                    chr.dropMessage(1, msg);
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                boolean useMileage = slea.readByte() == 1;
                boolean onlyMileage = slea.readByte() == 1;
                if (snCS / 100000 == 1102) {
                    toCharge = 1;
                    useMileage = true;
                    onlyMileage = true;
                }
                if (toCharge == 2) {
                    // 是否允許楓點購買
                    if (!ServerConfig.CHANNEL_ENABLEPOINTSBUY) {
                        chr.dropMessage(1, "此商品無法使用楓點購買，請選擇其他商品吧。");
                        c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                        return;
                    }
                    useMileage = false;
                }
                if (item.getMileageRate() == 0) {
                    useMileage = false;
                }
                if (snCS / 100000 != 1102 && (!item.isOnlyMileage() || !useMileage)) {
                    onlyMileage = false;
                }
                int point = item.getPrice();
                int mileage = onlyMileage ? point : useMileage ? (int) Math.floor(point * item.getMileageRate() / 100) : 0;
                point -= mileage;
                if (chr.getCSPoints(toCharge) < point || chr.getMileage() < mileage || slots >= GameConstants.MAX_CHARS_SLOTS) {
                    chr.dropMessage(1, "角色欄擴充失敗，點數餘額不足或者欄位已超過上限。");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                if (c.gainAccCharSlot()) {
                    chr.modifyCSPoints(toCharge, -point, false);
                    chr.modifyMileage(-mileage);
                    addCashshopLog(c, item.getSN(), item.getItemId(), 1, point, mileage, item.getCount(), chr.getName() + " 擴充角色欄: " + ii.getName(item.getItemId()));
                    chr.dropMessage(1, "角色欄擴充成功，當前欄位: " + (slots + 1));
                } else {
                    chr.dropMessage(1, "角色欄擴充失敗。");
                }
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            }
            case CashItemReq_IncBuyCharCount: { // 購買角色卡
//                int toCharge = slea.readByte() + 1;
//                int sn = slea.readInt();
//                boolean useMileage = slea.readByte() == 1;
//                boolean onlyMileage = slea.readByte() == 1;
//                CashItemInfo item = cashinfo.getItem(sn);
                chr.dropMessage(1, "暫時不支援。");
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            }
            case CashItemReq_EnableEquipSlotExt: {  // 項鏈擴充
                int toCharge = slea.readByte() + 1;
                int sn = slea.readInt();
                CashItemInfo item = cashinfo.getItem(sn);
                if (item == null || item.getItemId() / 10000 != 555) {
                    chr.dropMessage(1, "項鏈擴充失敗。");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                boolean useMileage = slea.readByte() == 1;
                boolean onlyMileage = slea.readByte() == 1;
                if (sn / 100000 == 1102) {
                    toCharge = 1;
                    useMileage = true;
                    onlyMileage = true;
                }
                if (toCharge == 2) {
                    // 是否允許楓點購買
                    if (!ServerConfig.CHANNEL_ENABLEPOINTSBUY) {
                        chr.dropMessage(1, "此商品無法使用楓點購買，請選擇其他商品吧。");
                        c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                        return;
                    }
                    useMileage = false;
                }
                if (item.getMileageRate() == 0) {
                    useMileage = false;
                }
                if (sn / 100000 != 1102 && (!item.isOnlyMileage() || !useMileage)) {
                    onlyMileage = false;
                }
                int point = item.getPrice();
                int mileage = onlyMileage ? point : useMileage ? (int) Math.floor(point * item.getMileageRate() / 100) : 0;
                point -= mileage;
                if (chr.getCSPoints(toCharge) < point || chr.getMileage() < mileage) {
                    chr.dropMessage(1, "項鏈擴充失敗，點數餘額不足或者出現其他錯誤。");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                MapleQuestStatus marr = chr.getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
                if (marr != null && marr.getCustomData() != null && ("0".equals(marr.getCustomData()) || Long.parseLong(marr.getCustomData()) >= System.currentTimeMillis())) {
                    chr.dropMessage(1, "項鏈擴充失敗，您已經進行過項鏈擴充。");
                    c.announce(MaplePacketCreator.pendantSlot(false));
                } else {
                    long days = 0;
                    if (item.getItemId() == 5550000) { //項鏈擴充（30天權）
                        days = 30;
                    } else if (item.getItemId() == 5550001) {  //項鏈擴充（7天權）
                        days = 7;
                    }
                    String customData = String.valueOf(System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000));
                    chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT)).setCustomData(customData);
                    chr.modifyCSPoints(toCharge, -point, false);
                    chr.modifyMileage(-mileage);
                    addCashshopLog(c, item.getSN(), item.getItemId(), 1, point, mileage, item.getCount(), chr.getName() + " 擴充墜飾欄: " + ii.getName(item.getItemId()));
                    chr.dropMessage(1, "項鏈擴充成功，本次擴充花費:\r\n" + (onlyMileage ? "里程" : toCharge == 1 ? "樂豆點" : "楓點") + item.getPrice() + " 點" + (!onlyMileage && useMileage ? ("[里程抵扣:" + mileage + "]") : "") + "，持續時間為: " + days + " 天。");
                    c.announce(MaplePacketCreator.pendantSlot(true));
                }
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            }
            case CashItemReq_Destroy2: {//刪除商城道具
                slea.readMapleAsciiString();
                slea.readByte();
                int uniqueId = (int) slea.readLong();
                Item item = cs.findByCashId(uniqueId);
                if (item == null) {
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    if (chr.isDebug()) {
                        System.out.println("刪除商城道具 - 道具為空 刪除失敗");
                    }
                    return;
                }
                cs.removeFromInventory(item);
                c.announce(MTSCSPacket.商城刪除道具(uniqueId));
                break;
            }
            case CashItemReq_Destroy: { //刪除商城道具
                int uniqueId = (int) slea.readLong();
                Item item = cs.findByCashId(uniqueId);
                if (item == null) {
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    if (chr.isDebug()) {
                        System.out.println("刪除商城道具 - 道具為空 刪除失敗");
                    }
                    return;
                }
                cs.removeFromInventory(item);
                c.announce(MTSCSPacket.商城刪除道具(uniqueId));
                break;
            }
            case CashItemReq_MoveLtoS: { // 商城 => 背包
                Item item = cs.findByCashId((int) slea.readLong());
                if (chr.isDebug()) {
                    log.info("商城 => 背包 - 道具是否為空 " + (item == null));
                }
                if (item == null) {
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                if (chr.getInventory(ItemConstants.getInventoryType(item.getItemId())).addItem(item) != -1) {
                    cs.removeFromInventory(item);
                    c.announce(MTSCSPacket.moveItemToInvFormCs(item));
                    if (chr.isDebug()) {
                        log.info("商城 => 背包 - 移動成功");
                    }
                }
                break;
            }
            case CashItemReq_MoveStoL: { // 背包 => 商城
                long cashId = slea.readLong();
                int itemId = slea.readInt();//V.161 new
                MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
                Item item = chr.getInventory(type).findByLiSN(cashId);
                if (item != null && item.getQuantity() > 0 && item.getSN() > 0 && chr.getCashInventory().getItemsSize() < 100) {
                    Item item_ = item.copy();
                    int sn = cashinfo.getSnFromId(item_.getItemId());
                    if (item.getPet() != null && item.getPet().getSummoned()) {
                        chr.unequipSpawnPet(item.getPet(), true, (byte) 0);
                    }
                    chr.getInventory(type).removeItem(item.getPosition(), item.getQuantity(), false);
                    item_.setPosition((byte) 0);
                    chr.getCashInventory().addToInventory(item_);
                    c.announce(MTSCSPacket.moveItemToCsFromInv(item_, c.getAccID(), sn));
                } else {
                    chr.dropMessage(1, "移動失敗。");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                }
                break;
            }
            case CashItemReq_Rebate: { // 換購
                /*
                 * 7F 01
                 * 1D
                 * 01 00 20
                 * 46 00 00 00 00 00 00 00 - uniqueid
                 */
 /*   slea.skip(2);
                slea.readMapleAsciiString(); //[01 00 20]
                int toCharge = 2; //是樂豆點還是楓點
                int uniqueId = (int) slea.readLong();
                Item item = cs.findByCashId(uniqueId);
                if (item == null) {
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                int snCS = cashinfo.getSnFromId(item.getItemId());
                CashItemInfo cItem = cashinfo.getItem(snCS, false);
                if (cItem == null || cashinfo.isBlockRefundableItemId(item.getItemId())) {
                    if (chr.isAdmin()) {
                        if (cItem == null) {
                            chr.dropMessage(1, "換購失敗:\r\n道具是否為空: " + (cItem == null));
                        } else {
                            chr.dropMessage(1, "換購失敗:\r\n道具禁止回購: " + cashinfo.isBlockRefundableItemId(item.getItemId()));
                        }
                    } else {
                        chr.dropMessage(1, "換購失敗，當前道具不支持換購。");
                    }
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                if (!ii.isCash(cItem.getItemId())) {
                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法換購道具.");
                    return;
                }
                int Money = cItem.getPrice() / 10 * 3; //獲得的楓點價格是原價*0.3
                cs.removeFromInventory(item);
                chr.modifyCSPoints(toCharge, Money, false);
                c.announce(MTSCSPacket.商城換購道具(uniqueId, Money));
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));*/
                chr.dropMessage(1, "暫時不支持。");
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            }

            case CashItemReq_BuyPackage: { // 購買禮包
                int toCharge = slea.readByte() + 1; //是樂豆點還是楓點
                int snCsId = slea.readInt(); //禮包的SNid
                slea.readInt(); //禮包道具的數量  int count =
                if (snCsId == 10200551 || snCsId == 10200552 || snCsId == 10200553) {
                    chr.dropMessage(1, "當前伺服器未開放購買商城活動欄裡面的道具.");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
//                if (cashinfo.isBlockCashSnId(snCsId)) {
//                    chr.dropMessage(1, "該禮包禁止購買.");
//                    c.announce(MTSCSPacket.updateCouponsInfo(chr));
//                    return;
//                }
                CashItemInfo item = cashinfo.getItem(snCsId, false);
                List<Integer> packageIds = null;
                if (item != null) {
                    packageIds = cashinfo.getPackageItems(item.getItemId());
                }
                if (item == null || packageIds == null) {
                    String msg = "未知錯誤";
                    if (chr.isAdmin()) {
                        if (item == null) {
                            msg += "\r\n\r\n 禮包道具信息為空";
                        }
                        if (packageIds == null) {
                            msg += "\r\n\r\n 禮包道具裡面的物品道具為空";
                        }
                    }
                    chr.dropMessage(1, msg);
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                } else if (chr.getCSPoints(toCharge) < item.getPrice()) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x03));
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                } else if (!item.genderEquals(c.getPlayer().getGender())) {
                    chr.dropMessage(1, "性別不符合");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                } else if (c.getPlayer().getCashInventory().getItemsSize() >= (100 - packageIds.size())) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x18));
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                if (item.getPrice() <= 0) {
                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法購買禮包道具.");
                    return;
                }
                chr.modifyCSPoints(toCharge, -item.getPrice(), false);
                Map<Integer, Item> packageItems = new HashMap<>(); //定義最終發送的禮包道具信息
                for (int i : packageIds) {
                    CashItemInfo cii = cashinfo.getSimpleItem(i);
                    if (cii == null) {
                        continue;
                    }
                    Item itemz = chr.getCashInventory().toItem(cii);
                    if (itemz == null || itemz.getSN() <= 0) {
                        continue;
                    }
//                    if (cashinfo.isBlockedCashItemId(item.getItem())) {
//                        continue;
//                    }
                    if (!ii.isCash(itemz.getItemId())) {
                        log.info("[作弊] " + chr.getName() + " 商城非法購買禮包道具.道具: " + itemz.getItemId() + " - " + ii.getName(itemz.getItemId()));
                        AutobanManager.getInstance().autoban(chr.getClient(), "商城非法購買禮包道具.");
                        continue;
                    }
                    packageItems.put(i, itemz);
                    chr.getCashInventory().addToInventory(itemz);
                    addCashshopLog(c, snCsId, itemz.getItemId(), toCharge, item.getPrice(), 0, itemz.getQuantity(), chr.getName() + " 購買禮包: " + ii.getName(itemz.getItemId()) + " - " + i);
                }
                c.announce(MTSCSPacket.商城購買禮包(packageItems, c.getAccID()));
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            }
            case CashItemReq_GiftPackage: { //商城送禮包
                String SecondPwd = slea.readMapleAsciiString();
                if (!c.CheckSecondPassword(SecondPwd)) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x00));
                    return;
                }
                int snCsId = slea.readInt(); //禮包的SNID
                CashItemInfo item = cashinfo.getItem(snCsId);
                String partnerName = slea.readMapleAsciiString();
                String msg = slea.readMapleAsciiString();
                if (item == null || chr.getCSPoints(1) < item.getPrice() || msg.length() > 73 || msg.length() < 1) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x03));
                    return;
                }
//                if (cashinfo.isBlockCashSnId(snCsId)) {
//                    chr.dropMessage(1, "該禮包禁止購買.");
//                    c.announce(MTSCSPacket.updateCouponsInfo(chr));
//                    return;
//                }
                Triple<Integer, Integer, Integer> info = MapleCharacterUtil.getInfoByName(partnerName, chr.getWorld());
                if (info == null || info.getLeft() <= 0) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x07)); //請確認角色名是否錯誤。
                } else if (info.getLeft() == chr.getId() || info.getMid() == c.getAccID()) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x06)); //無法向本人的賬號贈送禮物。請用該角色登陸，然後購買。
                } else if (!item.genderEquals(info.getRight())) {
                    c.announce(MTSCSPacket.商城錯誤提示(0x08)); //此道具對性別有限制。請確認接收人的性別。
                } else {
                    if (item.getPrice() <= 0) {
                        AutobanManager.getInstance().autoban(chr.getClient(), "商城非法購買禮包道具.");
                        return;
                    }
                    chr.getCashInventory().gift(info.getLeft(), chr.getName(), msg, item.getSN(), MapleInventoryIdentifier.getInstance());
                    chr.modifyCSPoints(1, -item.getPrice(), false);
                    //chr.dropMessage(1, "您成功的將禮包送給[" + partnerName + "]花費樂豆點" + item.getUnitPrice() + "點.");
                    c.announce(MTSCSPacket.商城送禮包(item.getItemId(), item.getCount(), partnerName));
                    chr.sendNote(partnerName, partnerName + " 您已收到" + chr.getName() + "送給您的禮物，請進入現金商城查看！");
                    addCashshopLog(c, item.getSN(), item.getItemId(), 1, item.getPrice(), 0, item.getCount(), chr.getName() + " 贈送禮包給 " + partnerName);
                    int chz = WorldFindService.getInstance().findChannel(partnerName);
                    if (chz > 0) {
                        MapleCharacter receiver = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterByName(partnerName);
                        if (receiver != null) {
                            receiver.showNote();
                        }
                    }
                }
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr)); //刷新下免得卡住
                break;
            }
            case CashItemReq_BuyNormal: { // 購買任務道具
                CashItemInfo item = cashinfo.getItem(slea.readInt());
                if (item == null || !MapleItemInformationProvider.getInstance().isQuestItem(item.getItemId())) {
                    chr.dropMessage(1, "該道具不是任務物品");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                } else if (chr.getMeso() < item.getPrice() || item.getPrice() <= 0) {
                    chr.dropMessage(1, "楓幣不足");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                } else if (chr.getItemQuantity(item.getItemId()) > 0) {
                    chr.dropMessage(1, "你已經有這個道具\r\n不能購買.");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                } else if (chr.getInventory(ItemConstants.getInventoryType(item.getItemId())).getNextFreeSlot() < 0) {
                    chr.dropMessage(1, "背包空間不足");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
//                if (cashinfo.isBlockedCashItemId(item.getItem())) {
//                    chr.dropMessage(1, CashShopServer.getCashBlockedMsg(item.getItem()));
//                    c.announce(MTSCSPacket.updateCouponsInfo(chr));
//                    return;
//                }
                if (item.getItemId() == 4031063 || item.getItemId() == 4031191 || item.getItemId() == 4031192) {
                    byte pos = MapleInventoryManipulator.addId(c, item.getItemId(), item.getCount(), null, "商城: 任務物品" + " 在 " + DateUtil.getCurrentDate());
                    if (pos < 0) {
                        c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                        return;
                    }
                    chr.gainMeso(-item.getPrice(), false);
                    c.announce(MTSCSPacket.updataMeso(chr));
                    c.announce(MTSCSPacket.商城購買任務道具(item.getPrice(), (short) item.getCount(), pos, item.getItemId()));
                } else {
                    AutobanManager.getInstance().autoban(chr.getClient(), "商城非法購買任務道具.");
                }
                break;
            }
            case CashItemReq_TradeDone: {
                slea.readByte(); //00 未知
                int snCS = slea.readInt();
                slea.readInt(); //01 00 00 00 數量?
                if (snCS == 50200031 && chr.getCSPoints(1) >= 500) {
                    chr.modifyCSPoints(1, -500, false);
                    chr.modifyCSPoints(2, 500, false);
                    chr.dropMessage(1, "兌換楓葉點數成功");
                } else if (snCS == 50200032 && chr.getCSPoints(1) >= 1000) {
                    chr.modifyCSPoints(1, -1000, false);
                    chr.modifyCSPoints(2, 1000, false);
                    chr.dropMessage(1, "兌換楓葉點數成功");
                } else if (snCS == 50200033 && chr.getCSPoints(1) >= 5000) {
                    chr.modifyCSPoints(1, -5000, false);
                    chr.modifyCSPoints(2, 5000, false);
                    chr.dropMessage(1, "兌換楓葉點數成功");
                } else {
                    chr.dropMessage(1, "沒有找到這個道具的信息。");
                }
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            }
            case CashItemReq_PurchaseRecord: { // 購買記錄
                int snCS = slea.readInt();
                c.announce(MTSCSPacket.購買記錄(snCS, getBuyLog(chr, snCS)));
                break;
            }
            case CashItemReq_DeletePurchaseRecord: { // 這個很重要 不發送這個包無法購買商城道具
                c.announce(MTSCSPacket.redeemResponse());
                break;
            }
            case CashItemReq_UseCashRandomItem: { //在商城中打開箱子
                long uniqueId = slea.readLong();
                Item boxItem = cs.findByCashId((int) uniqueId);
                if (boxItem == null || !cashinfo.hasRandomItem(boxItem.getItemId())) {
                    chr.dropMessage(1, "打開箱子失敗，伺服器找不到對應的道具信息。");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                List<Pair<Integer, Integer>> boxItemSNs = cashinfo.getRandomItem(boxItem.getItemId());
                if (boxItemSNs.isEmpty()) {
                    chr.dropMessage(1, "打開箱子失敗，伺服器找不到對應的道具信息。");
                    c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                    return;
                }
                Pair<Integer, Integer> snCS = boxItemSNs.get(Randomizer.nextInt(boxItemSNs.size()));
                CashItemInfo cItem = cashinfo.getItem(snCS.getLeft(), false);
                if (cItem != null) {
                    Item item = cs.toItem(cItem);
                    if (item != null && item.getSN() > 0 && item.getItemId() == cItem.getItemId() && item.getQuantity() == cItem.getCount()) {
                        if (chr.getInventory(ItemConstants.getInventoryType(item.getItemId())).addItem(item) != -1) {
                            cs.removeFromInventory(boxItem);
                            item.setAttribute(ItemAttribute.TradeOnce.getValue());
                            c.announce(MTSCSPacket.商城打開箱子(item, uniqueId));
                        } else {
                            chr.dropMessage(1, "打開箱子失敗，請確認背包是否有足夠的空間。");
                        }
                    }
                }
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                break;
            }
            default:
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                System.out.println("商城操作未知的操作類型: 0x" + StringUtil.getLeftPaddedStr(Integer.toHexString(action).toUpperCase(), '0', 2) + " " + slea);
                break;
        }
        c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
    }

    public static void 商城送禮(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        String SecondPwd = slea.readMapleAsciiString();
        if (!c.CheckSecondPassword(SecondPwd)) {
            c.announce(MTSCSPacket.商城錯誤提示(0x00));
            return;
        }
        int snCS = slea.readInt();
        CashItemFactory cashinfo = CashItemFactory.getInstance();
        CashItemInfo item = cashinfo.getItem(snCS);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        String partnerName = slea.readMapleAsciiString();
        String msg = slea.readMapleAsciiString();
        if (snCS == 92000046) {
            AutobanManager.getInstance().autoban(chr.getClient(), "商城非法購買道具.");
            return;
        }
//        if (cashinfo.isBlockedCashItemId(item.getItem()) || cashinfo.isBlockCashSnId(snCS)) {
//            chr.dropMessage(1, "該道具禁止購買.");
//            c.announce(MTSCSPacket.updateCouponsInfo(chr));
//            return;
//        }
        //System.out.println("商城 => 送禮 - 送給: " + partnerName + " 信息: " + msg);
        if (item == null || chr.getCSPoints(1) < item.getPrice() || msg.length() > 73 || msg.length() < 1) { //dont want packet editors gifting random stuff =P
            c.announce(MTSCSPacket.商城錯誤提示(0x03));
            //System.out.println("商城送禮: 錯誤 - 1");
            return;
        }
        Triple<Integer, Integer, Integer> info = MapleCharacterUtil.getInfoByName(partnerName, chr.getWorld());
        if (info == null || info.getLeft() <= 0) {
            c.announce(MTSCSPacket.商城錯誤提示(0x07)); //請確認角色名是否錯誤。
            //System.out.println("商城送禮: 錯誤 - 2");
        } else if (info.getLeft() == chr.getId() || info.getMid() == c.getAccID()) {
            c.announce(MTSCSPacket.商城錯誤提示(0x06)); //無法向本人的賬號贈送禮物。請用該角色登陸，然後購買。
            //System.out.println("商城送禮: 錯誤 - 3");
        } else if (!item.genderEquals(info.getRight())) {
            c.announce(MTSCSPacket.商城錯誤提示(0x08)); //此道具對性別有限制。請確認接收人的性別。
            //System.out.println("商城送禮: 錯誤 - 4");
        } else {
            if (!ii.isCash(item.getItemId())) {
                log.info("[作弊] " + chr.getName() + " 商城非法購買禮物道具.道具: " + item.getItemId() + " - " + ii.getName(item.getItemId()));
                chr.dropMessage(1, "購買商城禮物道具出現錯誤.");
                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
                //AutobanManager.getInstance().autoban(chr.getClient(), "商城非法購買道具.");
                return;
            }
            if (item.getPrice() <= 0) {
                AutobanManager.getInstance().autoban(chr.getClient(), "商城非法贈送禮包道具.");
                return;
            }
            //System.out.println("商城送禮: OK");
            chr.getCashInventory().gift(info.getLeft(), chr.getName(), msg, item.getSN(), MapleInventoryIdentifier.getInstance());
            chr.modifyCSPoints(1, -item.getPrice(), false);
            c.announce(MTSCSPacket.商城送禮(item.getItemId(), item.getCount(), partnerName));
            c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
            addCashshopLog(c, item.getSN(), item.getItemId(), 1, item.getPrice(), 0, item.getCount(), chr.getName() + " 購買道具: " + ii.getName(item.getItemId()) + " 送給 " + partnerName);
            chr.sendNote(partnerName, partnerName + " 您已收到" + chr.getName() + "送給您的禮物，請進入現金商城查看！");
            int chz = WorldFindService.getInstance().findChannel(partnerName);
            if (chz > 0) {
                MapleCharacter receiver = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterByName(partnerName);
                if (receiver != null) {
                    receiver.showNote();
                }
            }
        }
    }

    /*
     * 打開閃耀的隨機箱
     */
    public static void openAvatarRandomBox(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        CashShop cs = chr.getCashInventory();
        Item item = cs.findByCashId(slea.readLong());
        if (item == null) {
            c.announce(MTSCSPacket.showAvatarRandomBox(true, 0, 0, null, 0, false, false));
            c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
            return;
        }
        RaffleItem gitem = RafflePool.randomItem(item.getItemId());
        if (gitem == null) {
            c.announce(MTSCSPacket.showAvatarRandomBox(true, 0, 0, null, 0, false, false));
            c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Item reward;
        if (ii.isCash(gitem.getItemId())) {
            if (ItemConstants.getInventoryType(gitem.getItemId(), false) == MapleInventoryType.EQUIP) {
                reward = ii.getEquipById(gitem.getItemId());
            } else {
                MaplePet pet = null;
                long period = -1;
                if (ItemConstants.類型.寵物(gitem.getItemId())) {
                    if (pet == null) {
                        pet = MaplePet.createPet(gitem.getItemId());
                        if (pet != null && period == -1 && ii.getLife(gitem.getItemId()) > 0) {
                            period = System.currentTimeMillis() + (ii.getLife(gitem.getItemId()) * 24 * 60 * 60 * 1000L);
                        }
                    } else {
                        pet = null;
                    }
                }
                reward = new Item(gitem.getItemId(), (byte) 0, (short) 1, 0, MapleInventoryManipulator.getUniqueId(gitem.getItemId(), pet == null ? -1 : pet.getUniqueId()), (short) 0);
                reward.setPet(pet);
                if (pet != null) {
                    reward.addAttribute(ItemAttribute.RegressScroll.getValue());
                }
                reward.setExpiration(period);
            }
            reward.setGMLog("從" + ii.getName(item.getItemId()) + "抽取 時間 " + DateUtil.getCurrentDate());
            cs.addToInventory(reward);
        } else {
            reward = MapleInventoryManipulator.addbyId_Gachapon(c, gitem.getItemId(), 1, "從" + ii.getName(item.getItemId()) + "抽取 時間 " + DateUtil.getCurrentDate());
        }

        if (reward != null) {
            item.setQuantity((short) (item.getQuantity() - 1));
            if (item.getQuantity() <= 0) {
                cs.removeFromInventory(item);
            }
            c.announce(MTSCSPacket.showAvatarRandomBox(false, item.getSN(), item.getQuantity(), reward, c.getAccID(), true, gitem.isSmega()));
            c.announce(MTSCSPacket.loadLockerDone(c));
        } else {
            c.announce(MTSCSPacket.showAvatarRandomBox(true, 0, 0, null, 0, false, false));
        }
        c.announce(MTSCSPacket.CashShopQueryCashResult(chr));

//        switch (item.getItemId()) {
//            case 5222000: // 月光寶盒
//            case 5222004: // 月光精品禮盒
//            case 5222006: // 神奇服飾箱
//            case 5222123: // 時尚隨機箱
//                break;
//            default:
//                c.announce(MTSCSPacket.showAvatarRandomBox(true, item.getSN(), null, c.getAccID()));
//                c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
//                return;
//        }
//
//        if (item != null && item.getQuantity() > 0 && MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
//            final CashItemInfo rewardItem = CashItemFactory.getInstance().getItem(RandomRewards.getAvatarRandomBoxReward(), false);
//            Item itemz = cs.toItem(rewardItem);
//            cs.addToInventory(itemz);
//            c.announce(MTSCSPacket.showAvatarRandomBox(false, item.getSN(), itemz, c.getAccID()));
//            if (item.getQuantity() <= 1) {
//                cs.removeFromInventory(item);
//            } else {
//                item.setQuantity((short) (item.getQuantity() - 1));
//            }
//            c.announce(MTSCSPacket.loadLockerDone(c));
//        } else {
//            c.announce(MTSCSPacket.showAvatarRandomBox(true, item.getSN(), null, c.getAccID()));
//        }
//        c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
    }

    public static void addCashshopLog(MapleClient c, int SN, int itemId, int type, int price, int mileage, int count, String itemLog) {
        if (c == null) {
            return;
        }
        MapleCharacter chr = c.getPlayer();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO cashshop_log (accId, chrId, name, SN, itemId, type, price, mileage, count, cash, points, itemlog) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, c.getAccID());
            ps.setInt(2, chr == null ? 0 : chr.getId());
            ps.setString(3, chr == null ? "" : chr.getName());
            ps.setInt(4, SN);
            ps.setInt(5, itemId);
            ps.setInt(6, type);
            ps.setInt(7, price);
            ps.setInt(8, mileage);
            ps.setInt(9, count);
            ps.setInt(10, c.getCSPoints(1));
            ps.setInt(11, c.getCSPoints(2));
            ps.setString(12, itemLog);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            log.error(chr == null ? ("帳號" + c.getAccountName() + " ID: " + c.getAccID()) : ("玩家: " + chr.getName() + " ID: " + chr.getId()) + " 購買商城道具保存日誌出錯.", e);
        }
    }

    private static int getBuyLog(MapleCharacter chr, int SN) {
        if (chr == null) {
            return 0;
        }
        int quantity = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(id) FROM cashshop_log WHERE SN = ? AND accId = ? AND DATE_FORMAT(Time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')");
            ps.setInt(1, SN);
            ps.setInt(2, chr.getAccountID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                quantity = rs.getInt(1);
            }
            ps.close();
        } catch (SQLException e) {
        }
        return quantity;
    }

    public static void ReceiveMvpGradePacket(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_CashShop_MVP_ItemGive_Result);
        if (chr == null) {
            mplew.writeInt(0);
            chr.send(mplew.getPacket());
            return;
        }
        int level = chr.getMvpLevel();
        if (level < 1) {
            mplew.writeInt(0);
            chr.send(mplew.getPacket());
            return;
        }
        level = level < 5 ? 4 : level;
        String gp = chr.getWorldShareInfo(6, "gp");
        int today = Integer.parseInt(DateUtil.getCurrentDate("dd"));
        String now = DateUtil.getCurrentDate("yyyyMM")
                + (today > 20 ? "03" : today > 10 ? "02" : "01")
                + StringUtil.getLeftPaddedStr(String.valueOf(level), '0', 2);
        if (now.equals(gp)) {
            mplew.writeInt(0);
            chr.send(mplew.getPacket());
            return;
        }
        Map<Integer, Map<Integer, Integer>> gps_data = new LinkedHashMap();
        // 銅牌
        gps_data.put(4, new LinkedHashMap());
        gps_data.get(4).put(2023544, 2); // MVP超級力量加持
        gps_data.get(4).put(2000055, 50); // MVP恢復藥水
        gps_data.get(4).put(2450171, 2); // MVP經驗值2倍券
        gps_data.get(4).put(2631755, 2); // 超性能擴音器交換券
        gps_data.get(4).put(2631762, 1); // 身心修練館入場用符咒(3小時)1張交換券
        // 銀牌
        gps_data.put(5, new LinkedHashMap());
        gps_data.get(5).put(2023544, 3); // MVP超級力量加持
        gps_data.get(5).put(2000055, 150); // MVP恢復藥水
        gps_data.get(5).put(2450171, 3); // MVP經驗值2倍券
        gps_data.get(5).put(2631755, 3); // 超性能擴音器交換券
        gps_data.get(5).put(2023926, 3); // MVP追加經驗值50%優惠券
        gps_data.get(5).put(2631762, 2); // 身心修練館入場用符咒(3小時)1張交換券
        gps_data.get(5).put(2631765, 3); // 怪物公園免費使用券1張交換券
        // 金牌
        gps_data.put(6, new LinkedHashMap());
        gps_data.get(6).put(2023544, 5); // MVP超級力量加持
        gps_data.get(6).put(2000055, 200); // MVP恢復藥水
        gps_data.get(6).put(2450171, 5); // MVP經驗值2倍券
        gps_data.get(6).put(2631755, 5); // 超性能擴音器交換券
        gps_data.get(6).put(2023926, 10); // MVP追加經驗值50%優惠券
        gps_data.get(6).put(2631769, 3); // MVP PLUS EXP天氣效果1個交換券
        gps_data.get(6).put(2631762, 2); // 身心修練館入場用符咒(3小時)1張交換券
        gps_data.get(6).put(2631764, 1); // 身心修練館入場用符咒(12小時)1張交換券
        gps_data.get(6).put(2631765, 5); // 怪物公園免費使用券1張交換券
        // 鑽石
        gps_data.put(7, new LinkedHashMap());
        gps_data.get(7).put(2023544, 10); // MVP超級力量加持
        gps_data.get(7).put(2450171, 10); // MVP經驗值2倍券
        gps_data.get(7).put(2631755, 10); // 超性能擴音器交換券
        gps_data.get(7).put(2023926, 20); // MVP追加經驗值50%優惠券
        gps_data.get(7).put(2631769, 5); // MVP PLUS EXP天氣效果1個交換券
        gps_data.get(7).put(2631762, 2); // 身心修練館入場用符咒(3小時)1張交換券
        gps_data.get(7).put(2631764, 2); // 身心修練館入場用符咒(12小時)1張交換券
        gps_data.get(7).put(2631765, 10); // 怪物公園免費使用券1張交換券
        // 紅鑽
        gps_data.put(8, new LinkedHashMap());
        gps_data.get(8).put(2023544, 15); // MVP超級力量加持
        gps_data.get(8).put(2450171, 15); // MVP經驗值2倍券
        gps_data.get(8).put(2631755, 15); // 超性能擴音器交換券
        gps_data.get(8).put(2023926, 30); // MVP追加經驗值50%優惠券
        gps_data.get(8).put(2631769, 10); // MVP PLUS EXP天氣效果1個交換券
        gps_data.get(8).put(2631762, 2); // 身心修練館入場用符咒(3小時)1張交換券
        gps_data.get(8).put(2631764, 3); // 身心修練館入場用符咒(12小時)1張交換券
        gps_data.get(8).put(2631765, 15); // 怪物公園免費使用券1張交換券

        int rgp = gp != null && !gp.isEmpty() && gp.length() >= 10 && now.substring(0, 8).equals(gp.substring(0, 8)) ? Integer.parseInt(gp.substring(8)) : 0;
        Map<Integer, Integer> gps = new LinkedHashMap();
        for (Map.Entry<Integer, Integer> entry : gps_data.get(level).entrySet()) {
            int quantity = entry.getValue();
            if (rgp > 0) {
                quantity -= gps_data.get(rgp).getOrDefault(entry.getKey(), 0);
            }
            if (quantity > 0) {
                gps.put(entry.getKey(), quantity);
            }
        }

        if (chr.getSpace(2) < gps.size()) {
            mplew.writeInt(3);
            mplew.writeInt(gps.size());
            chr.send(mplew.getPacket());
            return;
        }

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_MONTH, 11);
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        for (Map.Entry<Integer, Integer> entry : gps.entrySet()) {
            MapleInventoryManipulator.addById(chr.getClient(), entry.getKey(), entry.getValue().intValue(), date.getTimeInMillis() - System.currentTimeMillis(), 0, "MVP禮物禮包");
        }

        mplew.writeInt(4);
        mplew.writeInt(level);
        mplew.writeInt(level >= 5 && level < 8 ? 1 : 0);
        mplew.writeInt(1); // 0 - 每日禮包 1 - 禮物禮包 2 - 特殊/皇家禮包
        mplew.writeInt(gps.size());
        for (Map.Entry<Integer, Integer> entry : gps.entrySet()) {
            mplew.writeInt(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        chr.send(mplew.getPacket());

        chr.updateWorldShareInfo(6, "gp", now);
    }

    public static void ReceiveMvpLevelPacket(MaplePacketReader slea, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_CashShop_MVP_ItemGive_Result);

        if (chr == null) {
            mplew.writeInt(0);
            chr.send(mplew.getPacket());
            return;
        }
        int receiveLevel = slea.readInt();
        if (chr.getMvpLevel() < receiveLevel || receiveLevel < 1) {
            mplew.writeInt(0);
            chr.send(mplew.getPacket());
            return;
        }

        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_MONTH, 1);
        date.set(Calendar.HOUR_OF_DAY, 6);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        if (System.currentTimeMillis() <= date.getTimeInMillis()) {
            mplew.writeInt(1);
            chr.send(mplew.getPacket());
            return;
        }

        String questInfo;
        if (receiveLevel >= 8) {
            questInfo = chr.getOneInfo(100561, "rp_" + receiveLevel);
        } else {
            questInfo = chr.getWorldShareInfo(6, "sp_" + receiveLevel);
        }
        if (questInfo == null || questInfo.contains("R")) {
            mplew.writeInt(0);
            chr.send(mplew.getPacket());
            return;
        }

        if (!chr.haveSpace(2)) {
            mplew.writeInt(3);
            mplew.writeInt(1);
            chr.send(mplew.getPacket());
            return;
        }
        int receiveItemID;
        switch (receiveLevel) {
            case 1:
                receiveItemID = 2434724;
                break;
            case 2:
                receiveItemID = 2434758;
                break;
            case 3:
                receiveItemID = 2434759;
                break;
            case 4:
                receiveItemID = 2434760;
                break;
            case 5:
                receiveItemID = 2434725;
                break;
            case 6:
                receiveItemID = 2434726;
                break;
            case 7:
                receiveItemID = 2434727;
                break;
            case 8:
            default:
                receiveItemID = 2631756;
                break;
        }
        MapleInventoryManipulator.addById(chr.getClient(), receiveItemID, 1, DateUtil.getLastTimeOfMonth() - System.currentTimeMillis(), 0, "MVP階級禮包");

        mplew.writeInt(4);
        mplew.writeInt(receiveLevel);
        mplew.writeInt(0);
        mplew.writeInt(receiveLevel > 7 ? 3 : 2); // 0 - 每日禮包 1 - 禮物禮包 2 - 特殊禮包 3 - 皇家禮包
        mplew.writeInt(1);
        mplew.writeInt(receiveItemID);
        mplew.writeInt(1);
        chr.send(mplew.getPacket());

        if (receiveLevel >= 8) {
            chr.updateOneQuestInfo(100561, "rp_" + receiveLevel, questInfo + "R");
        } else {
            chr.updateWorldShareInfo(6, "sp_" + receiveLevel, "R" + questInfo);
        }
        if (receiveLevel >= 5) {
            if (receiveLevel >= 8) {
                chr.updateInfoQuest(100563, null);
            } else {
                chr.updateWorldShareInfo(90 + receiveLevel - 5, null);
            }
            String d = chr.getWorldShareInfo(242, "d");
            String nowD = DateUtil.getCurrentDate("yyyyMM");
            if (d == null || d.isEmpty() || !nowD.equals(d)) {
                chr.updateWorldShareInfo(242, "d", nowD);
                chr.updateWorldShareInfo(242, "r1", "0");
                //chr.updateWorldShareInfo(242, "r2", "0");
            }
        }
    }
}
