/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.channel.handler;

import Client.MapleClient;
import Client.inventory.Equip;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Config.constants.ItemConstants;
import Config.constants.enums.UserChatMessageType;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Packet.MTSCSPacket;
import tools.Randomizer;
import tools.data.MaplePacketReader;

/**
 * @author PlayDK
 */
public class UseHammerHandler {

    public static void UseHammer(MaplePacketReader slea, MapleClient c) {
        /*
         * Send GoldHammerRequest [01A2] (22)
         * A2 01
         * 85 F9 B8 01 - 時間?
         * 02 00 00 00 - 金錘子的位置
         * 70 B0 25 00 - 金錘子的ID
         * 01 00 00 00 - 未知 難道是升級1次?
         * 0E 00 00 00 - 要升級的裝備位置
         * ?咘?....p?.........
         */
        c.getPlayer().updateTick(slea.readInt());
        int hammerSlot = slea.readInt();
        int hammerItemid = slea.readInt();
        slea.readInt();
        int equipSlot = slea.readInt();
        Item useItem = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) hammerSlot);
        Equip toItem = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) equipSlot);
        if (c.getPlayer().isAdmin()) {
            c.getPlayer().dropMessage(5, "黃金鐵鎚:" + useItem + "  裝備:" + toItem);
        }
        if (useItem == null || useItem.getQuantity() <= 0 || useItem.getItemId() != hammerItemid || c.getPlayer().hasBlockedInventory() || !ItemConstants.類型.黃金鐵鎚(useItem.getItemId())) {
            c.sendEnableActions();
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (toItem != null) {
            if (ItemConstants.卷軸.canHammer(toItem.getItemId()) && ii.getSlots(toItem.getItemId()) > 0 && toItem.getViciousHammer() < 1) {
                toItem.setViciousHammer((byte) (toItem.getViciousHammer() + 1));
                int successRate = MapleItemInformationProvider.getInstance().getScrollSuccess(hammerItemid);
                if (hammerItemid == 2470000) {
                    successRate = 100;
                }
                if (c.getPlayer().isDebug()) {
                    c.getPlayer().dropSpouseMessage(UserChatMessageType.系統, "黃金鐵鎚提煉 - 成功機率  =  " + successRate + "%");
                }
                if (Randomizer.nextInt(100) < successRate) {
                    toItem.setRestUpgradeCount((byte) (toItem.getRestUpgradeCount() + 1));
                    c.announce(MTSCSPacket.sendGoldHammerResult(0, 0));
                } else {
                    c.announce(MTSCSPacket.sendGoldHammerResult(0, 1));
                }
                c.getPlayer().forceUpdateItem(toItem);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (byte) hammerSlot, (short) 1, false, true);
            } else {
                c.getPlayer().dropMessage(5, "無法使用黃金鐵鎚提煉的道具。");
                c.announce(MTSCSPacket.sendGoldHammerResult(0, 1));
            }
        }
    }

    public static void PlatinumHammerResponse(MaplePacketReader slea, MapleClient c) {
        c.getPlayer().updateTick(slea.readInt());
        int hammerSlot = slea.readInt();
        int hammerItemid = slea.readInt();
        slea.readInt();
        int equipSlot = slea.readInt();
        Item useItem = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) hammerSlot);
        Equip toItem = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) equipSlot);
        if (c.getPlayer().isAdmin()) {
            c.getPlayer().dropMessage(5, "白金鎚子:" + useItem + "  裝備:" + toItem);
        }
        if (useItem == null || useItem.getQuantity() <= 0 || useItem.getItemId() != hammerItemid || c.getPlayer().hasBlockedInventory() || !ItemConstants.類型.白金鎚子(useItem.getItemId())) {
            c.sendEnableActions();
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (toItem != null) {
            if (ItemConstants.卷軸.canHammer(toItem.getItemId()) && ii.getSlots(toItem.getItemId()) > 0 && toItem.getPlatinumHammer() < 5) {
                int successRate;
                switch (toItem.getPlatinumHammer()) {
                    case 0:
                        successRate = 60;
                        break;
                    case 1:
                        successRate = 45;
                        break;
                    case 2:
                        successRate = 30;
                        break;
                    case 3:
                        successRate = 15;
                        break;
                    case 4:
                        successRate = 5;
                        break;
                    default:
                        successRate = 0;
                }
                if (c.getPlayer().isDebug()) {
                    c.getPlayer().dropSpouseMessage(UserChatMessageType.系統, "白金鎚子提煉(次數：" + (toItem.getPlatinumHammer() + 1) + ") - 成功機率: " + successRate + "%");
                }
                if (Randomizer.isSuccess(successRate)) {
                    toItem.setRestUpgradeCount((byte) (toItem.getRestUpgradeCount() + 1));
                    toItem.setPlatinumHammer((byte) (toItem.getPlatinumHammer() + 1));
                    c.announce(MTSCSPacket.sendPlatinumHammerResult(2));
                } else {
                    c.announce(MTSCSPacket.sendPlatinumHammerResult(3));
                }
                c.getPlayer().forceUpdateItem(toItem);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (byte) hammerSlot, (short) 1, false, true);
            } else {
                c.getPlayer().dropMessage(5, "無法使用白金鎚子提煉的道具。");
                c.announce(MTSCSPacket.sendPlatinumHammerResult(3));
            }
        }
    }

    public static void GoldHammerResponse(MaplePacketReader slea, MapleClient c) {
        c.announce(MTSCSPacket.sendGoldHammerResult(2, slea.readInt()));
    }
}
