/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Client.MapleCharacter;
import Client.inventory.Equip;
import Client.inventory.MapleAndroid;
import Client.inventory.MapleInventoryType;
import Config.constants.ItemConstants;
import Net.server.movement.LifeMovementFragment;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.MessageOpcode;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.List;

/**
 * @author PlayDK
 */
public class AndroidPacket {

    /*
     * 召喚機器人
     */
    public static byte[] spawnAndroid(MapleCharacter chr, MapleAndroid android) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_AndroidEnterField.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(android.getType());
        mplew.writePos(android.getPos() == null ? chr.getPosition() : android.getPos());
        mplew.write(android.getStance());
        mplew.writeShort(android.getFh());
        mplew.writeInt(0); // TMS.230
        android.encodeAndroidLook(mplew);
        for (short i = -1200; i > -1207; i--) {
            Equip eq = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
            mplew.writeInt(eq != null ? eq.getItemId() : 0);
            mplew.writeInt(eq != null ? eq.getItemSkin() : 0);
            mplew.writeInt(0); // 顏色?
        }

        return mplew.getPacket();
    }

    /*
     * 機器人移動
     */
    public static byte[] moveAndroid(int chrId, int gatherDuration, int nVal1, Point mPos, Point oPos, List<LifeMovementFragment> res) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_AndroidMove.getValue());
        mplew.writeInt(chrId);
        PacketHelper.serializeMovementList(mplew, gatherDuration, nVal1, mPos, oPos, res, null);

        return mplew.getPacket();
    }

    /*
     * 顯示機器人表情
     */
    public static byte[] showAndroidEmotion(int chrId, int speak, int animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_AndroidActionSet.getValue());
        mplew.writeInt(chrId);
        mplew.write(speak);
        mplew.write(animation); //1234567 = default smiles, 8 = throwing up, 11 = kiss, 14 = googly eyes, 17 = wink...

        return mplew.getPacket();
    }

    /*
     * 更新機器人外觀
     */

    public static byte[] updateAndroidLook(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        MapleAndroid android;
        if (chr == null || (android = chr.getAndroid()) == null) {
            return mplew.getPacket();
        }

        mplew.writeShort(OutHeader.LP_AndroidModified.getValue());
        mplew.writeInt(chr.getId());

        mplew.write(0);
        mplew.writeLong(android.getUniqueId());

        android.encodeAndroidLook(mplew);

        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] updateAndroidEquip(int chrId, boolean unequip, Pair<Integer, Integer> item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        byte op = 0;
        if (ItemConstants.類型.帽子(item.getLeft())) {
            op = 0x01;
        } else if (ItemConstants.類型.披風(item.getLeft())) {
            op = 0x02;
        } else if (ItemConstants.類型.臉飾(item.getLeft())) {
            op = 0x04;
        } else if (ItemConstants.類型.上衣(item.getLeft()) || ItemConstants.類型.套服(item.getLeft())) {
            op = 0x08;
        } else if (ItemConstants.類型.褲裙(item.getLeft())) {
            op = 0x10;
        } else if (ItemConstants.類型.鞋子(item.getLeft())) {
            op = 0x20;
        } else if (ItemConstants.類型.手套(item.getLeft())) {
            op = 0x40;
        }
        if (op == 0) {
            return mplew.getPacket();
        }

        mplew.writeShort(OutHeader.LP_AndroidModified.getValue());
        mplew.writeInt(chrId);

        mplew.write(op);
        mplew.writeInt(unequip ? 0 : item.getLeft());
        mplew.writeInt(unequip ? 0 : item.getRight());
        mplew.writeInt(0);

        mplew.write(0);

        return mplew.getPacket();
    }

    /*
     * 玩家停用機器人
     * v262 更新封包
     */
    public static byte[] deactivateAndroid(int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AndroidLeaveField.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * 移除機器人心臟
     */
    public static byte[] removeAndroidHeart() {
        /*
         * 0x15 智能機器人沒有動力。請裝備機械心臟。
         * 0x16 休息後恢復了疲勞度。
         */
        return CWvsContext.sendMessage(MessageOpcode.MS_AndroidMachineHeartAlertMessage, null);
    }
}
