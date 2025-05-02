package Packet;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Config.constants.enums.MiniRoomOptType;
import Net.server.MerchItemPackage;
import Net.server.shops.*;
import Net.server.shops.AbstractPlayerStore.BoughtItem;
import Net.server.shops.AbstractPlayerStore.VisitorInfo;
import Opcode.Headler.OutHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PlayerShopPacket {

    private static final Logger log = LoggerFactory.getLogger(PlayerShopPacket.class);

    public static byte[] sendTitleBox(int message) {
        return sendTitleBox(message, 0, 0);
    }

    public static byte[] sendTitleBox(int message, int mapId, int ch) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_EntrustedShopCheckResult.getValue());
        mplew.write(message);
        switch (message) {
            case 0x07: //雙擊僱傭商店卡 彈出輸入僱傭商店的名字窗口
            case 0x09: //請向自由市場入口處的弗蘭德裡領取物品後，重新再試。
            case 0x0F: //請通過弗蘭德裡領取物品。
                break;
            case 0x08: //提示僱傭商店開設在什麼地方
            case 0x10: //x頻道開設有商店。您想要移動到該頻道嗎？ 這個時候 mapId = 0
                mplew.writeInt(mapId);
                mplew.write(ch);
                break;
        }

        return mplew.getPacket();
    }

    public static byte[] sendPlayerBox(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserMiniRoomBalloon.getValue());
        mplew.writeInt(chr.getId());
        PacketHelper.addAnnounceBox(mplew, chr);

        return mplew.getPacket();
    }

    public static byte[] getHiredMerch(MapleCharacter chr, HiredMerchant merch, boolean firstTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_EnterResult.getValue());
        mplew.write(0x06); //V.109修改 這個是商店的類型
        mplew.write(merch.getMaxSize()); //以前是0x04 V.100修改
        mplew.writeShort(merch.getVisitorSlot(chr));
        mplew.writeInt(merch.getItemId());
        mplew.writeMapleAsciiString("僱傭商人");
        for (Pair<Byte, MapleCharacter> storechr : merch.getVisitors()) {
            mplew.write(storechr.left);
            storechr.right.getAvatarLook().encode(mplew, true);
            mplew.writeMapleAsciiString(storechr.right.getName());
            mplew.writeShort(storechr.right.getJob());
        }
        mplew.write(-1);
        mplew.writeShort(merch.isOwner(chr) ? merch.getMessages().size() : 0);
        if (merch.isOwner(chr)) {
            for (int i = 0; i < merch.getMessages().size(); i++) {
                mplew.writeMapleAsciiString(merch.getMessages().get(i).getLeft());
                mplew.write(merch.getMessages().get(i).getRight());
            }
        }
        mplew.writeMapleAsciiString(merch.getOwnerName());
        if (merch.isOwner(chr)) {
            /*
             * 74 5B 02 00 - 23:57
             * 8B C5 03 00 - 23:55
             */
            mplew.writeInt(merch.getTimeLeft(firstTime));
            mplew.write(firstTime ? 1 : 0);
            mplew.write(merch.getBoughtItems().size());
            for (BoughtItem SoldItem : merch.getBoughtItems()) {
                mplew.writeInt(SoldItem.id);
                mplew.writeShort(SoldItem.quantity); // number of purchased
                mplew.writeLong(SoldItem.totalPrice); // total price
                mplew.writeMapleAsciiString(SoldItem.buyer); // name of the buyer
            }
            mplew.writeLong(merch.getMeso());
        }
        mplew.writeInt(merch.getObjectId()); //V.106新增
        mplew.writeMapleAsciiString(merch.getDescription());
        mplew.write(0x10);
        mplew.writeLong(merch.getMeso());
        mplew.write(merch.getItems().size());
        for (MaplePlayerShopItem item : merch.getItems()) {
            mplew.writeShort(item.bundles);
            mplew.writeShort(item.item.getQuantity());
            mplew.writeLong(item.price);
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item.item);
        }
        mplew.writeShort(0); //求購的道具信息

        return mplew.getPacket();
    }

    public static byte[] getPlayerStore(MapleCharacter chr, boolean firstTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        IMaplePlayerShop ips = chr.getPlayerShop();
        mplew.writeInt(MiniRoomOptType.MRP_EnterResult.getValue());
        switch (ips.getShopType()) {
            case 2:
                mplew.write(5);
                mplew.write(7);
                break;
            case 3:
                mplew.write(2);
                mplew.write(2);
                break;
            case 4:
                mplew.write(1);
                mplew.write(2);
                break;
        }
        mplew.writeShort(ips.getVisitorSlot(chr));
        ((MaplePlayerShop) ips).getMCOwner().getAvatarLook().encode(mplew, false);
        mplew.writeMapleAsciiString(ips.getOwnerName());
        mplew.writeShort(((MaplePlayerShop) ips).getMCOwner().getJob());
        for (Pair<Byte, MapleCharacter> storechr : ips.getVisitors()) {
            mplew.write(storechr.left);
            storechr.right.getAvatarLook().encode(mplew, false);
            mplew.writeMapleAsciiString(storechr.right.getName());
            mplew.writeShort(storechr.right.getJob());
        }
        mplew.write(0xFF);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString(ips.getDescription());
        mplew.write(24);
        mplew.write(ips.getItems().size());
        for (MaplePlayerShopItem item : ips.getItems()) {
            mplew.writeShort(item.bundles);
            mplew.writeShort(item.item.getQuantity());
            mplew.writeLong(item.price);
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item.item);
        }
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /*
     * 互動類聊天通用！
     */
    public static byte[] playerInterChat(String message, int slot, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_Chat.getValue());
        mplew.write(MiniRoomOptType.MRP_UserChat.getValue());
        mplew.write(slot);
        mplew.writeMapleAsciiString(name);//V.181 new
        mplew.writeMapleAsciiString(message);
        mplew.writeInt(0);
        PacketHelper.addChaterName(mplew, name, message);

        return mplew.getPacket();
    }

    /**
     * 提示錯誤信息
     */
    public static byte[] shopErrorMessage(int error, int type) {
        return shopErrorMessage(false, error, type);
    }

    public static byte[] shopErrorMessage(boolean room, int error, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(room ? MiniRoomOptType.MRP_EnterResult.getValue() : MiniRoomOptType.MRP_Leave.getValue());
        mplew.write(type);
        mplew.write(error);

        return mplew.getPacket();
    }

    /*
     * 召喚僱傭商店
     */
    public static byte[] spawnHiredMerchant(AbstractPlayerStore hm) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_EmployeeEnterField.getValue());
        mplew.writeInt(hm.getOwnerId());
        mplew.writeInt(hm.getItemId());
        mplew.writePos(hm.getPosition());
        mplew.writeShort(hm instanceof HiredFisher ? ((HiredFisher) hm).getFh() : 0);
        mplew.writeMapleAsciiString(hm.getOwnerName());
        PacketHelper.addInteraction(mplew, hm);

        return mplew.getPacket();
    }

    /*
     * 取消僱傭商店
     */
    public static byte[] destroyHiredMerchant(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_EmployeeLeaveField.getValue());
        mplew.writeInt(id);

        return mplew.getPacket();
    }

    /*
     * 更新商店道具
     */
    public static byte[] shopItemUpdate(IMaplePlayerShop shop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.PSP_Refresh.getValue());
        if (shop.getShopType() == 1) {
            mplew.writeLong(shop.getMeso()); //購買的時候顯示價格
        }
        mplew.write(shop.getItems().size());
        for (MaplePlayerShopItem item : shop.getItems()) {
            mplew.writeShort(item.bundles);
            mplew.writeShort(item.item.getQuantity());
            mplew.writeLong(item.price);
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item.item);
        }
        mplew.writeShort(0); //求購的道具信息更新

        return mplew.getPacket();
    }

    /*
     * 玩家同意交易後添加角色
     */
    public static byte[] playerInterVisitorAdd(MapleCharacter chr, int slot, boolean isCash) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (isCash) {
            mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
            mplew.write(MiniRoomOptType.MRP_Enter.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_TradeRoom.getValue());
            mplew.writeInt(MiniRoomOptType.MRP_Enter.getValue());
        }

        mplew.write(slot);
        chr.getAvatarLook().encode(mplew, isCash);
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeShort(chr.getJob());
        mplew.writeInt(0);
        mplew.write(0);
        chr.sendEnableActions();
        return mplew.getPacket();
    }

    /**
     * 玩家互動退出
     */
    public static byte[] playerInterVisitorLeave(byte slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_Leave.getValue());
        mplew.write(slot);

        return mplew.getPacket();
    }

    /**
     * 僱傭商店錯誤提示
     */
    public static byte[] Merchant_Buy_Error(byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        /*
         * V.106 0x2C
         * V.110 0x2D
         */
        mplew.writeInt(MiniRoomOptType.PSP_BuyResult.getValue());
        /*
         * 0x01 物品不夠。
         * 0x02 楓幣不足
         * 0x03 該商品的價格太貴。你買不起
         * 0x04 超過對方的最大金額 無法交易.
         * 0x05 請確認是不是你的背包空間不夠。
         * 0x06 這種道具不能拿兩個以上。
         * 0x07 性別不符，無法購買。
         * 0x08 未滿7歲的人無法購買該物品。
         * 0x09 由於存在有效時間結束而消失的物品，購買已取消。
         * 0x0A 正在驗證中，現金道具的購買已取消。
         * 0x0B 該角色不能執行該操作。
         * 0x0C 遊戲文件損壞，無法交易物品。請重新安裝遊戲後，在重新嘗試。
         * 0x0D 發生未知錯誤，不能交易。
         */
        mplew.write(message);

        return mplew.getPacket();
    }

    /*
     * 更新僱傭商店
     * True - 更新僱傭商店信息
     * Flase - 更改僱傭商店名字
     */
    public static byte[] updateHiredMerchant(AbstractPlayerStore shop) {
        return updateHiredMerchant(shop, true);
    }

    public static byte[] updateHiredMerchant(AbstractPlayerStore shop, boolean update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(update ? OutHeader.UPDATE_HIRED_MERCHANT.getValue() : OutHeader.CHANGE_HIRED_MERCHANT_NAME.getValue());
        mplew.writeInt(shop.getOwnerId());
        PacketHelper.addInteraction(mplew, shop);

        return mplew.getPacket();
    }

    /*
     * 僱傭商店關閉完成
     */
    public static byte[] hiredMerchantOwnerLeave() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.ESP_WithdrawAllResult.getValue());
        mplew.write(0);

        return mplew.getPacket();
    }

    /*
     * 弗蘭德裡操作信息
     */
    public static byte[] merchItem_Message(byte op) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /*
         * 已領取所有道具
         * 0x24 已領取所有道具與楓幣
         * 0x25 因商店倉庫內的金額過多，未能領取楓幣與道具
         * 0x26 因道具有數量的限制，未能領取楓幣與道具
         * 0x27 因手續費不足，未能領取楓幣與道具
         * 0x28 因背包位不足，未能領取進步與道具
         */
        mplew.writeShort(OutHeader.LP_StoreBankGetAllResult.getValue());
        mplew.write(op);

        return mplew.getPacket();
    }

    public static byte[] merchItemStore(byte op) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_StoreBankResult.getValue());
        /*
         * 發送打開僱傭商店的封包
         */
        mplew.write(op);
        if (op == 0x2B) { //已經過去x天，因此需要收取全部楓幣的100% x楓幣作為手續費。確定要取款嗎？
            mplew.writeInt(0); //過去了多少天數
            mplew.writeLong(0); //取回需要楓幣多少
        } else {
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    /*
     * 提示僱傭商店開設在什麼地方
     */
    public static byte[] merchItemStore(int mapId, int ch) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_StoreBankResult.getValue());
        /*
         * V.110 0x28
         * V.115 0x2B
         */
        mplew.write(MiniRoomOptType.ESP_BuyResult.getValue());
        mplew.writeInt(9030000); //對話的NpcId
        mplew.writeInt(mapId);
        mplew.write(mapId != 999999999 ? ch : 0);

        return mplew.getPacket();
    }

    public static byte[] merchItemStore_ItemData(MerchItemPackage pack) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_StoreBankResult.getValue());
        /*
         * V.103 0x26
         * V.115 0x29
         */
        mplew.write(MiniRoomOptType.ESP_SellItem_3.getValue());
        mplew.writeInt(9030000); // Fredrick
        mplew.writeInt(32272); // always the same..?
        mplew.writeZeroBytes(5);
        mplew.writeLong(pack.getMesos());
        mplew.write(0);
        mplew.write(pack.getItems().size());
        for (Item item : pack.getItems()) {
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
        }
        mplew.writeZeroBytes(3);

        return mplew.getPacket();
    }

    public static byte[] openMiniGameBox(MapleClient c, MapleMiniGame minigame) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_EnterResult.getValue());
        mplew.write(minigame.getGameType());
        mplew.write(minigame.getMaxSize());
        mplew.writeShort(minigame.getVisitorSlot(c.getPlayer()));
        minigame.getMCOwner().getAvatarLook().encode(mplew, false);
        mplew.writeMapleAsciiString(minigame.getOwnerName());
        mplew.writeShort(minigame.getMCOwner().getJob());
        for (Pair<Byte, MapleCharacter> visitorz : minigame.getVisitors()) {
            mplew.write(visitorz.getLeft());
            visitorz.getRight().getAvatarLook().encode(mplew, false);
            mplew.writeMapleAsciiString(visitorz.getRight().getName());
            mplew.writeShort(visitorz.getRight().getJob());
        }
        mplew.write(-1);
        mplew.write(0);
        addGameInfo(mplew, minigame.getMCOwner(), minigame);
        for (Pair<Byte, MapleCharacter> visitorz : minigame.getVisitors()) {
            mplew.write(visitorz.getLeft());
            addGameInfo(mplew, visitorz.getRight(), minigame);
        }
        mplew.write(-1);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.writeShort(minigame.getPieceType());
        return mplew.getPacket();
    }

    /*
     * 遊戲準備就緒
     */
    public static byte[] getMiniGameReady(boolean ready) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt((ready ? MiniRoomOptType.MGRP_Ready.getValue() : MiniRoomOptType.MGRP_CancelReady.getValue())); //0x38 : 0x39
        return mplew.getPacket();
    }

    public static byte[] getMiniGameExitAfter(boolean ready) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(ready ? MiniRoomOptType.MGRP_LeaveEngage.getValue() : MiniRoomOptType.MGRP_LeaveEngageCancel.getValue());//0x36 : 0x37
        return mplew.getPacket();
    }

    public static byte[] getMiniGameStart(int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MGRP_Start.getValue());//3B
        mplew.write(loser == 1 ? 0 : 1);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameSkip(int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MGRP_TimeOver.getValue());//0x3D
        //owner = 1 visitor = 0?
        mplew.write(slot);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameRequestTie() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MGRP_TieRequest.getValue());//0x30
        return mplew.getPacket();
    }

    public static byte[] getMiniGameDenyTie() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MGRP_TieResult.getValue());//0x31
        return mplew.getPacket();
    }

    public static byte[] getMiniGameRequestRetreat() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MGRP_RetreatRequest.getValue());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameDenyRetreat() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MGRP_RetreatRequest.getValue());
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_EnterResult.getValue());
        mplew.write(0);
        mplew.write(2);
        mplew.writeMapleAsciiString("");
        return mplew.getPacket();
    }

    public static byte[] getMiniGameMoveOmok(int move1, int move2, int move3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.ORP_PutStoneChecker.getValue());//0x3E
        mplew.writeInt(move1);
        mplew.writeInt(move2);
        mplew.write(move3);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameNewVisitor(MapleCharacter c, int slot, MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_Enter.getValue());//4
        mplew.write(slot);
        c.getAvatarLook().encode(mplew, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeShort(c.getJob());
        addGameInfo(mplew, c, game);
        return mplew.getPacket();
    }

    public static void addGameInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, MapleMiniGame game) {
        mplew.writeInt(game.getGameType()); // start of visitor; unknown
        mplew.writeInt(game.getWins(chr));
        mplew.writeInt(game.getTies(chr));
        mplew.writeInt(game.getLosses(chr));
        mplew.writeInt(game.getScore(chr)); // points
        mplew.writeInt(0); // 台版自己加的
    }

    public static byte[] getMiniGameClose(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_Leave.getValue());//0xA
        mplew.write(1);
        mplew.write(number);
        return mplew.getPacket();
    }

    public static byte[] getMatchCardStart(MapleMiniGame game, int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MGRP_Start.getValue());//0x3B
        mplew.write(loser == 1 ? 0 : 1);
        int times = game.getPieceType() == 1 ? 20 : (game.getPieceType() == 2 ? 30 : 12);
        mplew.write(times);
        for (int i = 1; i <= times; i++) {
            mplew.writeInt(game.getCardId(i));
        }
        return mplew.getPacket();
    }

    public static byte[] getMatchCardSelect(int turn, int slot, int firstslot, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MGP_TurnUpCard.getValue());//0x42
        mplew.write(turn);
        mplew.write(slot);
        if (turn == 0) {
            mplew.write(firstslot);
            mplew.write(type);
        }
        return mplew.getPacket();
    }

    public static byte[] getMiniGameResult(MapleMiniGame game, int type, int x) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MGRP_GameResult.getValue());//0x3C
        mplew.write(type); //lose = 0, tie = 1, win = 2
        game.setPoints(x, type);
        if (type != 0) {
            game.setPoints(x == 1 ? 0 : 1, type == 2 ? 0 : 1);
        }
        if (type != 1) {
            if (type == 0) {
                mplew.write(x == 1 ? 0 : 1); //who did it?
            } else {
                mplew.write(x);
            }
        }
        addGameInfo(mplew, game.getMCOwner(), game);
        for (Pair<Byte, MapleCharacter> visitorz : game.getVisitors()) {
            addGameInfo(mplew, visitorz.right, game);
        }

        return mplew.getPacket();
    }

    /*
     * 僱傭商店訪問者名單
     */
    public static byte[] MerchantVisitorView(Map<String, VisitorInfo> visitor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.ESP_DeliverVisitList.getValue()); //V.110修改 以前是 0x24
        mplew.writeShort(visitor.size());
        for (Entry<String, VisitorInfo> ret : visitor.entrySet()) {
            mplew.writeMapleAsciiString(ret.getKey());
            mplew.writeInt(ret.getValue().getInTime()); //訪問時間是幾分鐘前
        }

        return mplew.getPacket();
    }

    /*
     * 僱傭商店黑名單
     */
    public static byte[] MerchantBlackListView(List<String> blackList) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.ESP_DeliverBlackList.getValue()); //V.110修改 以前是 0x25
        mplew.writeShort(blackList.size());
        for (String visit : blackList) {
            mplew.writeMapleAsciiString(visit);
        }
        return mplew.getPacket();
    }

    public static byte[] FishNotice() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_CreateResult.getValue());
        mplew.write(75);
        mplew.write(24);

        return mplew.getPacket();
    }

    public static byte[] FishExit() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MiniRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_Leave.getValue());
        mplew.write(0);
        mplew.write(17);

        return mplew.getPacket();
    }
}
