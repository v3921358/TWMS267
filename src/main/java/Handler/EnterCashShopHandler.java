package Handler;

import Client.MapleClient;
import Client.inventory.Item;
import Config.constants.ServerConstants;
import Opcode.Headler.OutHeader;
import Packet.MTSCSPacket;
import Packet.MaplePacketCreator;
import Server.cashshop.CashShopServer;
import connection.OutPacket;
import connection.packet.Login;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.List;

import static Server.cashshop.handler.CashShopOperation.*;

public class EnterCashShopHandler {

    private static MapleClient c;

    public static MapleClient getC() {
        return c;
    }

    public static void Start(MapleClient c) {
        // 73 v267
        c.write(Login.sendServerValues());
        // 74 v267
        c.write(Login.sendServerEnvironment());
        // 67 v267
        c.announce(c.getEncryptOpcodesData(ServerConstants.OpcodeEncryptionKey));
        // 54 v267
        c.outPacket(OutHeader.LP_LOGIN_ACTION_CHECK.getValue());
        // 661 v267
        c.announce(MTSCSPacket.warpchartoCS(c));
        // 663 v267
        c.announce(MTSCSPacket.warpCS(false)); //載入商城物品數據
        // null v265
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
        // 32 v267
        c.write(chatServerResult());
        // 360 v267
        c.write(EventNotice_unk());

        // 2477 v265
        c.write(enterCashShop());
        // 2476 v265
        c.outPacket(2476, "00");
        // 2485 v265
        c.write(enterCashShopOpenWeb());
        // no.?
        CashShopServer.getPlayerStorage().registerPlayer(c.getPlayer());
        // 跑馬燈 遊戲TOP事件訊息
        c.announce(MaplePacketCreator.serverMessage(""));
        // check gift
        List<Pair<Item, String>> gifts = c.getPlayer().getCashInventory().loadGifts();
        // 2467 v264
        c.announce(MTSCSPacket.getCashShopStyleCouponPreviewInfo());
        // 360 v267 +
        c.write(EventNotice_unk());
        // 2453 update user point
        c.announce(CashShopQueryCashResult(c, true, true, true)); //刷新樂豆點和楓點 這個地方盛大發了2次
        // v264 2484
        enterCashShoptype(c);

        // v267
        c.outPacket(2535, "22 00 00 00 00");

        // v267
        c.outPacket(2505, "07 00 00");

        // v267
        c.outPacket(2505, "09 00 00");

        // v267
        c.outPacket(2505, "01 06 00 E8 B5 AB E8 8C B2");

        // v267
        c.outPacket(2505, "0B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");

        // v267
        c.outPacket(2511, "00 00 00 00 00 00 00 00 00 00 00 00");

        CashShopEventMessage(c); // 198
        CashShopEventMessageNext(c); // 344
        c.announce(MTSCSPacket.loadLockerDone(c)); //顯示購買的物品
        c.announce(MTSCSPacket.getCashShopStyleCouponPreviewInfo());
        c.announce(MTSCSPacket.商城禮物信息(gifts)); //顯示禮物
        c.announce(MTSCSPacket.sendWishList(c.getPlayer(), false)); //顯示購物車信息
        c.getPlayer().getCashInventory().checkExpire(c); //檢查商城裡面的道具是否到期
    }

    /**
     * 顯示樂豆點、楓點和里程
     */
    public static byte[] CashShopQueryCashResult(MapleClient c, boolean cs_1, boolean cs_2, boolean cs_3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CashShopQueryCashResult.getValue());
        if(cs_1) {
            mplew.writeInt(c.getPlayer().getCSPoints(1));//樂豆點
        } else {
            mplew.writeInt(0);//樂豆點
        }
        if(cs_2) {
            mplew.writeInt(c.getPlayer().getCSPoints(2));//樂豆點
        } else {
            mplew.writeInt(0);//樂豆點
        }
        if(cs_3) {
            mplew.writeInt(c.getPlayer().getMileage());//里程
        } else {
            mplew.writeInt(0);//樂豆點
        }
        return mplew.getPacket();
    }


    /* @ ver_265 Packet tools by Hertz - Date:2024-10-25 */
    public static OutPacket PingToGame() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_PingCheckResult_ClientToGame);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeShort(0);
        return outPacket;
    }

/* @ ver_265 Packet tools by Hertz - Date:2024-10-25 */
    public static OutPacket chatServerResult() {
        OutPacket outPacket = new OutPacket(OutHeader.CHAT_SERVER_RESULT);
        outPacket.encodeInt(0);
        outPacket.encodeShort(0);
        return outPacket;
    }


    // TODO: V267 +
    public static OutPacket EventNotice_unk() {
        OutPacket say = new OutPacket(OutHeader.LP_CheckProcess.getValue());
        say.encodeInt(25);
        say.encodeInt(1);
        say.encodeString("布萊爾小姐的夢幻快遞");
        say.encodeInt(25);
        say.encodeInt(44);
        say.encodeInt(1);
        say.encodeInt(20220225);
        say.encodeInt(20220301);
        say.encodeInt(100000);
        say.encodeInt(100000);
        say.encodeInt(0);
        say.encodeInt(-1);
        say.encodeInt(0);
        say.encodeInt(0);
        say.encodeInt(0);
        say.encodeInt(0);
        say.encodeInt(0);
        say.encodeInt(0);
        say.encodeByte(0);
        say.encodeString("如果有想移動的現金道具，請利用布萊爾小姐的夢幻快遞！");
        say.encodeInt(0);
        return say;
    }

    /* @ v262 Packet tools by Hertz - Date:2024-07-08 */
    public static OutPacket sendloginAttach(MapleClient c) {
        OutPacket say = new OutPacket(OutHeader.LOGIN_SUCC_ATTACH.getValue());
        return say;
        /* Size: 17 */
    }

    /* @ v262 Packet tools by Hertz - Date:2024-07-08 */
    public static OutPacket getShowQuestCompletion(int quest_cid) {
        OutPacket say = new OutPacket(OutHeader.LP_QuestClear.getValue());
        if (c.getPlayer() != null) {
            say.encodeInt(quest_cid);
        }
        return say;
        /* Size: 17 */
    }

    // TODO: V267 +
    public static OutPacket enterCashShop() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_CashShopChargeParamResult);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        return outPacket;
    }

    /* @ ver_265 Packet tools by Hertz - Date:2024-10-25 */
    public static OutPacket enterCashShopOpenWeb() {
        OutPacket outPacket = new OutPacket(OutHeader.ENTER_CASH_SHOP);
        outPacket.encodeByte(3);
        outPacket.encodeInt(0);
        return outPacket;
    }
}
