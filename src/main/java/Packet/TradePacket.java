/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Config.constants.enums.MiniRoomOptType;
import Net.server.MapleTrade;
import Opcode.Headler.OutHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketLittleEndianWriter;

/**
 * @author PlayDK
 */
public class TradePacket {

    private static final Logger log = LoggerFactory.getLogger(TradePacket.class);

    /*
     * 玩家交易邀請
     */
    public static byte[] getTradeInvite(MapleCharacter chr, boolean isCash) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TradeRoom.getValue());
        mplew.writeInt(21);
        if (isCash) {
            mplew.write(7);
        } else {
            mplew.write(4);
        }
        mplew.writeInt(chr.getAccountID());
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeInt(chr.getId());
        mplew.writeInt(79);
        chr.sendEnableActions();
        return mplew.getPacket();
    }

    /*
     * 玩家交易設置楓幣
     */
    public static byte[] getTradeMesoSet(byte number, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TradeRoom.getValue());
        mplew.writeInt(MiniRoomOptType.TRP_PutMoney.getValue());
        mplew.write(number);
        mplew.writeLong(meso);
        return mplew.getPacket();
    }

    /*
     * 玩家交易放入道具
     */
    public static byte[] getTradeItemAdd(byte number, Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TradeRoom.getValue());
        mplew.writeInt(MiniRoomOptType.TRP_PutItem.getValue());
        mplew.write(number);
        mplew.write(item.getPosition());
        PacketHelper.GW_ItemSlotBase_Encode(mplew, item);

        return mplew.getPacket();
    }

    /*
     * 交易開始
     * 雙方角色都進入交易界面
     */
    public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number, boolean cash) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_TradeRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_EnterResult.getValue());
        if (cash) {
            mplew.write(7);
        } else {
            mplew.write(4);
        }
        mplew.write(2); //應該是交易的人數
        mplew.write(number);
        if (number == 1) {
            mplew.write(0);
            trade.getPartner().getChr().getAvatarLook().encode(mplew, true);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
            mplew.writeShort(trade.getPartner().getChr().getJob());
            mplew.writeInt(0);
        }
        mplew.write(number);
        c.getPlayer().getAvatarLook().encode(mplew, !cash);
        mplew.writeMapleAsciiString(c.getPlayer().getTrade().getChr().getName());
        mplew.writeShort(c.getPlayer().getTrade().getChr().getJob());
        mplew.writeInt(0);
        mplew.write(0xFF);
        c.sendEnableActions();
        return mplew.getPacket();
    }

    public static byte[] getTradeConfirmation() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_TradeRoom.getValue());
        mplew.writeInt(MiniRoomOptType.TRP_Trade.getValue());

        return mplew.getPacket();
    }

    public static byte[] TradeMessage(byte number, byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TradeRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_Leave.getValue());
        mplew.write(number);
        /*
         * 0x01 已經關閉了。
         * 0x02 對方中止交易。
         * 0x07 交易成功了。請再確認交易的結果。
         * 0x08 交易失敗了。
         * 0x09 因部分道具有數量限制只能擁有一個交易失敗了。
         * 0x0C 雙方在不同的地圖不能交易。
         * 0x0D 遊戲文件損壞，無法交易物品。請重新安裝遊戲後，再重新嘗試。
         */
        mplew.write(message);
        return mplew.getPacket();
    }

    /*
     * 玩家交易取消 v266
     */
    public static byte[] getTradeCancel(byte number, int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TradeRoom.getValue());
        mplew.writeInt(MiniRoomOptType.MRP_Leave.getValue());
        mplew.write(0);
        mplew.write(2);
        return mplew.getPacket();
    }
}