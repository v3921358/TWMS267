/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Client.inventory.MaplePotionPot;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.buffs.MapleStatEffect;
import Packet.InventoryPacket;
import tools.data.MaplePacketReader;

/**
 * @author PlayDK
 */
public class PotionPotHandler {

    /*
     * 使用藥劑罐
     */
    public static void PotionPotUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null || chr.getPotionPot() == null || !chr.isAlive()) {
            c.sendEnableActions();
            return;
        }
        long time = System.currentTimeMillis();
        if (chr.getNextConsume() > time) {
            c.announce(InventoryPacket.showPotionPotMsg(0x00, 0x08)); //0x08 被奇怪的氣息所圍繞，暫時無法使用道具。
            c.sendEnableActions();
            return;
        }
        //[AC 00] [14 0E 3A 01] [04 00] [60 CE 58 00]
        slea.skip(4);
        short slot = slea.readShort(); //藥劑罐在背包的位置
        int itemId = slea.readInt(); //藥劑罐道具ID
        Item item = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
        if (item == null || item.getItemId() != itemId || itemId / 10000 != 582) {
            c.sendEnableActions();
            return;
        }
        usePotionPot(chr);
    }

    public static void usePotionPot(MapleCharacter chr) {
        MaplePotionPot potionPot = chr.getPotionPot();
        int potHp = potionPot.getHp();
        int potMp = potionPot.getMp();
        if (potHp == 0 && potMp == 0) {
            chr.getClient().announce(InventoryPacket.updataPotionPot(potionPot));
            chr.getClient().announce(InventoryPacket.showPotionPotMsg(0x00, 0x06)); //0x06 這個藥劑罐是空的，請再次填充。
            return;
        }
        boolean usePot = false; //是否使用了藥劑罐
        int healHp = chr.getStat().getHealHp();
        int healMp = chr.getStat().getHealMp(chr.getJob());
        int usePotHp = potHp >= healHp ? healHp : potHp;
        int usePotMp = potMp >= healMp ? healMp : potMp;
        if (usePotHp > 0) {
            chr.addHP(usePotHp);
            potionPot.setHp(potHp - usePotHp);
            usePot = true;
        }
        if (usePotMp > 0) {
            chr.addMP(usePotMp);
            potionPot.setMp(potMp - usePotMp);
            usePot = true;
        }
        if (usePot && chr.getMap().getConsumeItemCoolTime() > 0) {
            chr.setNextConsume(System.currentTimeMillis() + (chr.getMap().getConsumeItemCoolTime() * 1000L));
        }
        chr.getClient().announce(InventoryPacket.updataPotionPot(potionPot));
        chr.getClient().announce(InventoryPacket.showPotionPotMsg(usePot ? 0x01 : 0x00, 0x00));
    }

    /*
     * 往藥劑罐中加入藥水
     */
    public static void PotionPotAdd(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null || chr.getPotionPot() == null || !chr.isAlive()) {
            c.sendEnableActions();
            return;
        }
        //[AD 00] [A6 34 72 01] [02 00] [88 84 1E 00] [17 00 00 00]   血  23個
        //[AD 00] [26 78 72 01] [03 00] [8A 84 1E 00] [21 00 00 00]   藍  33個
        slea.skip(4);
        short slot = slea.readShort(); //藥水在背包的位置
        int itemId = slea.readInt(); //藥水道具ID
        short quantity = (short) slea.readInt(); //衝入的數量
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || toUse.getQuantity() < quantity) {
            c.sendEnableActions();
            return;
        }
        MaplePotionPot potionPot = chr.getPotionPot();
        MapleStatEffect itemEffect = MapleItemInformationProvider.getInstance().getItemEffect(itemId);
        if (itemEffect != null) {
            //當前藥水1個能衝入的hp
            int addHp = itemEffect.getHp();
            if (itemEffect.getHpR() > 0) {
                int hp = chr.getStat().getCurrentMaxHP(); //角色當前的最大Hp
                if (hp > 100000) {
                    hp = (int) (itemEffect.getHpR() * 100000) - 1;
                } else {
                    hp = (int) (itemEffect.getHpR() * hp);
                }
                addHp += hp;
            }
            addHp = (int) (addHp * 1.2);
            //當前藥水1個能衝入的Mp
            int addMp = itemEffect.getMp();
            if (itemEffect.getMpR() > 0) {
                int mp = chr.getStat().getCurrentMaxMP(); //角色當前的最大Mp
                if (mp > 100000) {
                    mp = (int) (itemEffect.getMpR() * 100000) - 1;
                } else {
                    mp = (int) (itemEffect.getMpR() * mp);
                }
                addMp += mp;
            }
            addMp = (int) (addMp * 1.2);
            if (addHp <= 0 && addMp <= 0) {
                c.announce(InventoryPacket.updataPotionPot(potionPot));
                c.announce(InventoryPacket.showPotionPotMsg(0x00, 0x00)); //0x00 沒有提示。
                return;
            }
            if (potionPot.isFull(addHp, addMp)) {
                c.announce(InventoryPacket.updataPotionPot(potionPot));
                c.announce(InventoryPacket.showPotionPotMsg(0x00, 0x02)); //0x02 這個藥劑罐已經滿了。
                return;
            }
            //開始檢測是否滿了
            boolean isFull = false; //藥劑罐是否已經滿了 或者道具已經使用完成
            short useQuantity = 0;
            while (!isFull) {
                potionPot.addHp(addHp > 0 ? addHp : 0);
                potionPot.addMp(addMp > 0 ? addMp : 0);
                useQuantity++;
                //如果藥劑罐已經滿了或者使用的道具數量等於封包道具數量
                isFull = potionPot.isFull(addHp, addMp) || useQuantity == quantity;
            }
            if (useQuantity > 0) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, useQuantity, false);
            }
            c.announce(InventoryPacket.updataPotionPot(potionPot));
            c.announce(InventoryPacket.showPotionPotMsg(0x02));
        }
        c.sendEnableActions();
    }

    /*
     * 藥劑罐模式設置 也就是是否自動補充藥水
     */
    public static void PotionPotMode(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null || chr.getPotionPot() == null) {
            c.sendEnableActions();
            return;
        }
        c.sendEnableActions();
    }

    /*
     * 增加藥劑罐的容量上限
     */
    public static void PotionPotIncr(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null || chr.getPotionPot() == null) {
            c.sendEnableActions();
            return;
        }
        //[AF 00] [BC 5D BB 01] [00] [48 D2 58 00] [03 00]
        slea.skip(4);
        slea.skip(1); //[00] 未知
        int itemId = slea.readInt(); //擴充道具的ID 5821000
        short slot = slea.readShort(); //擴充道具在背包的位置
        Item toUse = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId != 5821000) {
            c.sendEnableActions();
            return;
        }
        MaplePotionPot potionPot = chr.getPotionPot();
        boolean useItem = potionPot.addMaxValue();
        if (useItem) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (short) 1, false);
        }
        c.announce(InventoryPacket.updataPotionPot(potionPot));
        c.announce(InventoryPacket.showPotionPotMsg(useItem ? 0x03 : 0x00, useItem ? 0x00 : 0x03));
    }
}
