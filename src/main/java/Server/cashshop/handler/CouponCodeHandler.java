/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.cashshop.handler;

import Client.MapleCharacter;
import Client.MapleCharacterUtil;
import Client.MapleClient;
import Client.inventory.Item;
import Config.constants.ItemConstants;
import Net.server.MapleInventoryManipulator;
import Packet.MTSCSPacket;
import tools.DateUtil;
import tools.Triple;
import tools.data.MaplePacketReader;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author PlayDK
 */
public class CouponCodeHandler {

    public static void handlePacket(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        slea.skip(1);
        String code = slea.readMapleAsciiString();
        if (code.length() < 20) {
            c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
            return;
        }
        Triple<Boolean, Integer, Integer> info = null;
        try {
            info = MapleCharacterUtil.getNXCodeInfo(code);
        } catch (SQLException e) {
            System.out.print("錯誤 getNXCodeInfo" + e);
        }
        if (info != null && info.left) {
            int type = info.mid, item = info.right;
            try {
                MapleCharacterUtil.setNXCodeUsed(chr.getName(), code);
            } catch (SQLException e) {
                System.out.print("錯誤 setNXCodeUsed" + e);
                c.announce(MTSCSPacket.商城錯誤提示(0));
                return;
            }
            /*
             * Explanation of type! Basically, this makes coupon codes do different things!
             *
             * Type 1: 樂豆點,
             * Type 2: 楓點
             * Type 3: 物品道具.. use SN
             * Type 4: 楓幣
             */
            Map<Integer, Item> itemz = new HashMap<>();
            int maplePoints = 0, mesos = 0;
            switch (type) {
                case 1: //楓點
                case 2:
                    c.getPlayer().modifyCSPoints(type, item, false);
                    maplePoints = item;
                    break;
                case 3:
                    short slot = MapleInventoryManipulator.addId(c, item, 1, "", "商城道具卡兌換 時間: " + DateUtil.getCurrentDate());
                    if (slot <= -1) {
                        c.announce(MTSCSPacket.商城錯誤提示(0));
                        return;
                    } else {
                        itemz.put(item, chr.getInventory(ItemConstants.getInventoryType(item)).getItem(slot));
                    }
                    break;
                case 4: //楓幣
                    chr.gainMeso(item, false);
                    mesos = item;
                    break;
            }
            c.announce(MTSCSPacket.showCouponRedeemedItem(itemz, mesos, maplePoints, c));
            c.announce(MTSCSPacket.CashShopQueryCashResult(chr));
        } else {
            c.announce(MTSCSPacket.商城錯誤提示(info == null ? 0x0E : 0x10));
        }
    }
}
