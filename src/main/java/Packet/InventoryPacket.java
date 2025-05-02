/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Client.MapleCharacter;
import Client.MessageOption;
import Client.inventory.*;
import Client.skills.InnerSkillEntry;
import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Config.constants.enums.ScrollIconType;
import Config.constants.skills.冒險家_技能群組.暗影神偷;
import Net.server.MapleItemInformationProvider;
import Net.server.maps.MapleMapItem;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.MessageOpcode;
import Server.channel.handler.EnchantHandler;
import Server.channel.handler.ItemScrollHandler;
import connection.OutPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author PlayDK
 */
public class InventoryPacket {

    private static final Logger log = LoggerFactory.getLogger(InventoryPacket.class);

    public static byte[] updateInventorySlotLimit(byte invType, byte newSlots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_InventoryGrow.getValue());
        mplew.write(invType);
        mplew.write(newSlots);

        return mplew.getPacket();
    }

    /**
     * Created by HERTZ on 2023/12/23
     *
     * @notice V257 更新寵物狀態
     */

    /* 00 00 02 00 00 00 00 03 05 64 00 00 05 64 00 03 9E 53 4C 00 01 2C 02 A3 0E 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 B0 67 A7 41 BC 50 B6 E3 00 00 00 00 00 01 00 00 64 00 C4 A4 3F 0E 7C DA 01 00 00 7F 1E 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 64 00 00 00 */
    public static byte[] updatePet(MaplePet pet, Item item, boolean summoned) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_InventoryOperation.getValue());
        mplew.write(0);
        mplew.write(0); // 1149++
        mplew.writeInt(2);
        mplew.write(0);
        mplew.write(3);
        mplew.write(5);
        mplew.writeShort(pet.getInventoryPosition());
        mplew.write(0);
        mplew.write(5);
        mplew.writeShort(pet.getInventoryPosition());
        mplew.write(3);
        OutPacket outPacket = new OutPacket();
        item.encodePetRaw(outPacket, pet, summoned);
        mplew.write(outPacket.getData());

        return mplew.getPacket();
    }

    /**
     * Created by HERTZ on 2023/12/23
     *
     * @notice 寵物強化箱 - [ 86 56 10 01 / 01 / 05 / 00 00 00 00 / 00 00 00 00 / 00 00 00]
     */
    public static byte[] petAddSkillEffect(MapleCharacter player, MaplePet pet) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.PET_ADD_SKILL_EFFECT.getValue());
        mplew.write(player.getId());
        mplew.write(1);
        mplew.writeInt(pet.getAddSkill());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] modifyInventory(boolean updateTick, List<ModifyInventory> mods) {
        return modifyInventory(updateTick, mods, null);
    }

    public static byte[] modifyInventory(boolean updateTick, List<ModifyInventory> mods, MapleCharacter chr) {
        return modifyInventory(updateTick, mods, chr, false);
    }

    /*
     * 0 = 獲得道具
     * 1 = 更新道具數量
     * 2 = 移動道具
     * 3 = 刪除道具
     * 4 = 刷新裝備經驗
     * 5 = 移動道具小背包到背包
     * 6 = 小背包更新道具
     * 7 = 小背包刪除道具
     * 8 = 移動位置小背包裡面的道具
     * 9 = 小背包獲得道具
     */
    public static byte[] modifyInventory(boolean updateTick, List<ModifyInventory> mods, MapleCharacter chr, boolean active) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_InventoryOperation.getValue());
        mplew.writeBool(updateTick);
        mplew.write(0); // v255
        mplew.writeInt(mods.size()); //更新的次數
        mplew.write(0);
        int addMovement = -1;
        for (ModifyInventory mod : mods) {
            mplew.write(mod.getMode());
            mplew.write(mod.getInventoryType());
            boolean oldpos = mod.getMode() == 2 || mod.getMode() == 9 || (mod.getMode() == 6 && !mod.switchSrcDst());
            mplew.writeShort(oldpos ? mod.getOldPosition() : mod.getPosition());
            switch (mod.getMode()) {
                case 0:  //獲得道具
                    PacketHelper.GW_ItemSlotBase_Encode(mplew, mod.getItem());
                    break;
                case 1:  //更新道具數量
                    mplew.writeShort(mod.getQuantity());
                    break;
                case 2:  //移動道具
                    mplew.writeShort(mod.getPosition());
                    if (mod.getPosition() < 0 || mod.getOldPosition() < 0) {
                        addMovement = mod.getOldPosition() < 0 ? 1 : 2;
                    }
                    break;
                case 3:  //刪除道具
                    if (mod.getPosition() < 0) {
                        addMovement = 2;
                    }
                    break;
                case 4:  // 刷新經驗值
                    mplew.writeLong(((Equip) mod.getItem()).getSealedExp());
                    break;
                case 6: //移動道具小背包到背包
                    mplew.writeShort(!mod.switchSrcDst() ? mod.getPosition() : mod.getOldPosition());
                    if (mod.getIndicator() != -1) {
                        mplew.writeShort(mod.getIndicator());
                    }
                    break;
                case 7: //小背包更新道具
                    mplew.writeShort(mod.getQuantity());
                    break;
                case 8: //小背包刪除道具
                    //這個地方無需處理
                    break;
                case 9: //移動位置小背包裡面的道具
                    mplew.writeShort(mod.getPosition());
                    break;
                case 10: //小背包獲得道具
                    PacketHelper.GW_ItemSlotBase_Encode(mplew, mod.getItem());
                    break;
            }
            mod.clear();
        }
        if (addMovement > -1) {
            mplew.write(addMovement);
        }

        return mplew.getPacket();
    }

    public static byte[] getInventoryFull() {
        return modifyInventory(true, Collections.emptyList());
    }

    public static byte[] getInventoryStatus() {
        return modifyInventory(false, Collections.emptyList());
    }

    public static byte[] getShowInventoryFull() {
        return getShowInventoryStatus(0xFF);
    }

    public static byte[] showItemUnavailable() {
        return getShowInventoryStatus(0xFE);
    }

    public static byte[] getShowInventoryStatus(int mode) {
        MessageOption option = new MessageOption();
        option.setMode(mode);
        return CWvsContext.sendMessage(MessageOpcode.MS_DropPickUpMessage, option);
    }

    public static byte[] showScrollTip(boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_SCROLL_TIP.getValue());
        mplew.writeInt(success ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getScrollEffect(int chrId, Equip.ScrollResult scrollSuccess, boolean legendarySpirit, boolean whiteScroll, int scroll, int toScroll) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //[DB 00] [46 57 01 00] [00] [00] [00] [00] 沒有成功
        mplew.writeShort(OutHeader.LP_UserItemUpgradeEffect.getValue());
        mplew.writeInt(chrId);
        switch (scrollSuccess) {
            case 失敗:
                mplew.write(0);
                break;
            case 成功:
                mplew.write(1);
                break;
            case 消失:
                mplew.write(2);
                break;
            default:
                throw new IllegalArgumentException("effect in illegal range");
        }
        mplew.write(legendarySpirit ? 1 : 0);
        mplew.writeInt(scroll);
        mplew.writeInt(toScroll);
        mplew.write(0); // 1祝福
        mplew.write(0);

        return mplew.getPacket();
    }

    /*
     * 使用方塊
     */
    public static byte[] getPotentialEffect(int chrId, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserItemReleaseEffect.getValue());
        mplew.writeInt(chrId);
        mplew.write(1);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    /*
     * 使用放大鏡
     */
    public static byte[] showMagnifyingEffect(int chrId, short pos, boolean isPotAdd) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserItemReleaseEffect.getValue());
        mplew.writeInt(chrId);
        mplew.writeShort(pos);
        mplew.write(isPotAdd ? 1 : 0); //T071新增 是否擴展潛能
        return mplew.getPacket();
    }


    /* 附加 */
    public static byte[] showPotentialReset(int chrId, boolean bonus, int itemid, int debris, int equipId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserItemUnreleaseEffect.getValue());
        mplew.writeInt(chrId);
        mplew.write(bonus);
        mplew.writeInt(itemid);
        mplew.writeInt(debris);
        mplew.writeInt(equipId);
        return mplew.getPacket();
    }

    public static byte[] showBlackCubeResults() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MemorialCubeModified.getValue());
        mplew.write(0);

        return mplew.getPacket();
    }

    /*
     * 增加擴展潛能效果
     */
    public static byte[] showPotentialEx(int chrId, boolean success, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserItemAdditionalSlotExtendEffect.getValue());
        mplew.writeInt(chrId);
        mplew.write(success);
        mplew.writeInt(itemid);
        mplew.writeInt(0);
        mplew.writeInt(1012478);
        return mplew.getPacket();
    }

    public static byte[] showFireWorksEffect(int chrId, boolean success, int itemid, boolean bUnk, int nUnk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserItemFireWorksEffect.getValue());
        mplew.writeInt(chrId);
        mplew.write(success);
        mplew.writeInt(itemid);
        mplew.write(bUnk);
        if (!bUnk) {
            mplew.writeInt(nUnk);
        }

        return mplew.getPacket();
    }


    /*
     * 道具合成提示
     */
    public static byte[] showSynthesizingMsg(int itemId, int giveItemId, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CashLookChangeResult.getValue());
        mplew.write(success ? 1 : 0);
        mplew.writeInt(itemId);
        mplew.writeInt(giveItemId); //神匠的禮物
        return mplew.getPacket();
    }

    public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte nDropType) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_DropEnterField.getValue());
        // 寫入 dropType (保留欄位，固定為 0)
        mplew.write(0);
        // 寫入 nDropType：1 = 楓幣、2 = 無動畫、3 = 出現後消失物品（Fade）、4 = 出現後消失物品
        mplew.write(nDropType);
        // 寫入掉落物件的 Object ID (4 bytes)
        mplew.writeInt(drop.getObjectId());
        // 判斷是否為楓幣 (若 drop.getMeso() > 0 為楓幣)
        boolean bIsMoney = drop.getMeso() > 0;
        mplew.writeBool(bIsMoney); // 通常寫入 1 個 byte (true = 1, false = 0)
        // 寫入掉落動畫資訊：動作類型、速度與亂數 (各 4 bytes)
        mplew.writeInt(drop.getDropMotionType());
        mplew.writeInt(drop.getDropSpeed());
        mplew.writeInt(drop.getRand());
        // 寫入掉落物品的 ID (4 bytes)
        mplew.writeInt(drop.getItemId());
        // 寫入擁有者 ID (4 bytes)
        mplew.writeInt(drop.getOwnerID());
        // 寫入擁有類型：0 = 非擁有者 timeout，1 = 同隊 timeout，2 = FFA，3 = 爆炸/FFA (1 byte)
        mplew.write(drop.getOwnType());
        // 寫入掉落目的座標 (Point：通常以 X 與 Y 各 4 bytes 寫入)
        mplew.writePos(dropto);
        // 寫入 2 個保留整數 (4 bytes * 2)
        mplew.writeInt(0);
        mplew.writeInt(0);
        // 寫入來源物件 ID (4 bytes)
        mplew.writeInt(drop.getSourceOID());
        mplew.writeInt(0);
        mplew.writeLong(0L);
        mplew.writeInt(0);
        mplew.write(0);
        // 以下為未知或保留欄位（sub_1404DFEE0 區塊）
        mplew.writeLong(0L);
        mplew.writeInt(0);
        mplew.writeLong(0L);
        // 若掉落為楓幣且使用特定技能（例如 暗影神偷.血腥掠奪術）則寫入特殊 flag (值 4)；否則 0
        mplew.write(bIsMoney && drop.getSkill() == 暗影神偷.血腥掠奪術 ? 4 : 0);
        // 寫入兩個 boolean (各 1 byte)
        mplew.write(false);
        mplew.write(false);
        // 寫入一個保留整數 (4 bytes)
        mplew.writeInt(0);
        // 若掉落類型非「無動畫」且 nDropType < 5，則寫入掉落來源位置及延遲值
        if (nDropType != 2 && nDropType < 5) {
            mplew.writePos(dropfrom);
            mplew.writeInt(drop.getDelay());
        }
        // 寫入一個 boolean 表示是否為爆炸掉落 (false)
        mplew.write(false);
        mplew.write(0); //TODO:UNK+ V267
        // 如果掉落為物品（非楓幣）且 itemId 不在 2910000 / 2910001 範圍內，則加入到期時間資訊
        if (drop.getItemId() != 2910000 && drop.getItemId() != 2910001 && !bIsMoney) {
            PacketHelper.addExpirationTime(mplew, drop.getItem().getExpiration());
        }
        // 以下依照正確楓包內容寫入剩餘資料：
        mplew.write(0);                 // 1 byte 保留
        mplew.writeInt(0);              // 4 bytes
        mplew.writeInt(0);              // 4 bytes
        mplew.write((drop.getPointType() > 0 ? 2 : drop.getState()) & 0xF); // 1 byte：點光效或狀態 (低 4 bit)
        mplew.write(0);                 // 1 byte 保留
        mplew.writeInt(0);              // 4 bytes
        mplew.writeInt(0);              // 4 bytes
        return mplew.getPacket();
    }


    public static byte[] explodeDrop(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DropLeaveField.getValue());
        mplew.write(4); // 4 = Explode
        mplew.writeInt(oid);
        mplew.writeShort(655);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] removeItemFromMap(int oid, int animation, int chrId) {
        return removeItemFromMap(oid, animation, chrId, 0);
    }

    public static byte[] removeItemFromMap(int oid, int animation, int chrId, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DropLeaveField.getValue());
        mplew.write(animation); // 0 = Expire, 1 = without animation, 2 = pickup, 4 = explode, 5 = pet pickup
        mplew.writeInt(oid);
        if (animation >= 2) {
            mplew.writeInt(chrId);
            if (animation == 5) { // allow pet pickup?
                mplew.writeInt(slot);
            }
        }

        return mplew.getPacket();
    }

    /*
     * 藥劑罐使用返回的提示
     * 0 = 使用失敗
     * 1 = 使用成功
     */
    public static byte[] showPotionPotMsg(int reason) {
        return showPotionPotMsg(reason, 0x00);
    }

    public static byte[] showPotionPotMsg(int reason, int msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.POTION_POT_MSG.getValue());
        mplew.write(reason);
        if (reason == 0) {
            /*
             * 0x00 沒有提示
             * 0x01 沒有物品
             * 0x02 這個藥劑罐已經滿了。
             * 0x03 你的藥劑罐容量已達最大值。
             * 0x04 藥劑魔瓶不能用在生銹的藥劑罐上。請用除銹劑為你的藥劑罐除銹。
             * 0x05 你的藥劑罐還沒有生銹。
             * 0x06 這個藥劑罐是空的，請再次填充。
             * 0x08 被奇怪的氣息所圍繞，暫時無法使用道具。
             */
            mplew.write(msg);
        }

        return mplew.getPacket();
    }

    /*
     * 更新藥劑罐信息
     */
    public static byte[] updataPotionPot(MaplePotionPot potionPot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.POTION_POT_UPDATE.getValue());
        PacketHelper.addPotionPotInfo(mplew, potionPot);

        return mplew.getPacket();
    }

    /*
     * 顯示角色當前裝備的技能皮膚信息
     */
    public static byte[] showSkillSkin(Map<Integer, Integer> skillskinlist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_SKILL_SKIN.getValue());
        mplew.writeInt(0x02);
        mplew.writeInt(skillskinlist.size()); //當前全部裝備的中的技能皮膚
        //循環發送信息[技能ID] [皮膚ID]
        for (Map.Entry<Integer, Integer> skillskin : skillskinlist.entrySet()) {
            mplew.writeInt(skillskin.getKey());
            mplew.writeInt(skillskin.getValue());
        }

        return mplew.getPacket();
    }

    /**
     * 其他玩家更換傷害皮膚效果
     */
    public static byte[] showDamageSkin(int chrId, int skinId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserSetDamageSkin.getValue());
        mplew.writeInt(chrId); //玩家ID
        mplew.writeInt(skinId); //更換的傷害皮膚ID
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * @param chrId
     * @param skinId
     * @return
     */
    public static final byte[] showDamageSkin_Premium(int chrId, int skinId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserSetDamageSkin.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(skinId);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] enchantingOperation(int mode, int success, Item toScroll, Item scrolled, List<EnchantScrollEntry> scrollEntries, String string, boolean safe, boolean pcDiscount, int sfDiscount, int mvpLevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_EquipmentEnchantDisplay.getValue());
        mplew.write(mode);
        switch (mode) {
            case 50: { /* 選擇卷軸介面 */
                mplew.write(0);
                mplew.write(scrollEntries.size());
                for (EnchantScrollEntry scroll : scrollEntries) {
                    mplew.writeInt(scroll.getIcon()); // 控制捲軸外觀
                    mplew.writeMapleAsciiString(scroll.getName()); // 捲軸名稱
                    mplew.writeInt(scroll.getType()); // 捲軸說明類型
                    mplew.writeInt(scroll.getIcon() == ScrollIconType.亞克回真卷軸.ordinal() ? 6 : (scroll.getIcon() > ScrollIconType.卷軸_15Percentage.ordinal() ? 1 : 0));
                    mplew.writeInt(scroll.getOption());
                    mplew.writeInt(scroll.getMask());
                    for (int val : scroll.getEnchantScrollStat().values()) {
                        mplew.writeInt(val);
                    }
                    mplew.writeInt(scroll.getCost());
                    mplew.writeInt(scroll.getCost());
                    mplew.write(scroll.getSuccessRate() == 100 ? 1 : 0);
                }
                break;
            }
            case 51: {
                mplew.write(0);
                break;
            }
            case 52: {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                Equip equip = (Equip) toScroll;
                byte enhance = equip.getStarForceLevel();
                safe = safe ? enhance >= 12 && enhance <= 16 : safe;
                int reqLevel = ii.getReqLevel(equip.getItemId());
                boolean isSuperior = ii.isSuperiorEquip(equip.getItemId());
                long meso = EnchantHandler.getStarForceMeso(reqLevel, enhance, isSuperior);
                if (meso < 0) {
                    log.error("出現錯誤：找不到強化需求楓幣");
                    meso = 0;
                }
                int successprop = (isSuperior ? ItemScrollHandler.sfSuccessPropSup[enhance] : ItemScrollHandler.sfSuccessProp[enhance]) * 10;
                int destroyType = (isSuperior ? ItemScrollHandler.sfDestroyPropSup[enhance] : ItemScrollHandler.sfDestroyProp[enhance]);
                destroyType = destroyType > 0 ? (enhance < 13 ? 1 : enhance < 16 ? 2 : enhance < 22 ? 3 : enhance < 24 ? 4 : 5) : 0;
                boolean fall = (isSuperior && enhance > 0) || (enhance >= 10 && enhance % 5 != 0);
                mplew.writeBool(fall); // 失敗是否降級 ? 1 : 0
                meso *= safe ? 2 : 1;
                long discountMeso = meso;
                if (sfDiscount > 0) {
                    discountMeso *= sfDiscount / 100.0;
                }
                if (mvpLevel >= 5 && enhance <= 17 && !isSuperior) {
                    discountMeso *= mvpLevel == 5 ? 0.97 : mvpLevel == 6 ? 0.95 : 0.9;
                }
                mplew.writeLong(discountMeso); // 消耗量
                mplew.writeLong(discountMeso != meso && sfDiscount > 0 ? meso : 0); // 折扣原始消耗量 0 - 不使用折扣
                mplew.writeLong(discountMeso != meso && mvpLevel >= 5 ? meso : 0); // MVP原始消耗量 0 - 不使用折扣
                mplew.write(mvpLevel >= 5); // 是否MVP特權, 顯示MVP
                mplew.write(pcDiscount); // 是否網吧特權, 顯示PC
                mplew.writeLong(ServerConfig.SF_MP_AMOUNT);
                mplew.writeLong(ServerConfig.SF_MP_SAFE_AMOUNT);
                mplew.writeInt(successprop);
                mplew.writeInt(destroyType);  // 損壞類型 0 - 無, 1 - 非常低, 2 - 低, 3 - 有機率, 4 - 高, 5 - 會爆
                mplew.writeInt(0); // 原始成功概率 0 - 不顯示成功率變更
                mplew.writeInt(0);
                mplew.writeBool(equip.getFailCount() >= 2);
                writeMaskEnchantScroll(mplew, toScroll);
                break;
            }
            case 53: {
                mplew.write(0);
                mplew.writeInt(DateUtil.getTime());
                break;
            }
            case 100: {
                mplew.write(0);
                mplew.writeInt(success);
                mplew.writeMapleAsciiString(string);
                PacketHelper.GW_ItemSlotBase_Encode(mplew, toScroll);
                if (success == 2) {
                    mplew.writeShort(0);
                    break;
                }
                PacketHelper.GW_ItemSlotBase_Encode(mplew, scrolled);
                break;
            }
            case 101: {
                mplew.writeInt(success);
                mplew.writeMapleAsciiString(string);
                PacketHelper.GW_ItemSlotBase_Encode(mplew, toScroll);
                if (success == 2) {
                    mplew.writeShort(0);
                    break;
                }
                PacketHelper.GW_ItemSlotBase_Encode(mplew, scrolled);
                break;
            }
            case 102: {
                mplew.writeInt(success);
                break;
            }
            case 103: {
                PacketHelper.GW_ItemSlotBase_Encode(mplew, toScroll);
                PacketHelper.GW_ItemSlotBase_Encode(mplew, scrolled);
                break;
            }
            case 104: {
                mplew.write(1);
                break;
            }
            case 105: {
                PacketHelper.GW_ItemSlotBase_Encode(mplew, toScroll);
                PacketHelper.GW_ItemSlotBase_Encode(mplew, scrolled);
            }
        }
        return mplew.getPacket();
    }
    public static void writeMaskEnchantScroll(MaplePacketLittleEndianWriter mplew, Item item) {
        Map<EnchantScrollFlag, Integer> scrollList = EnchantHandler.getEnchantScrollList(item);
        int mask = 0;
        for (EnchantScrollFlag flag : scrollList.keySet()) {
            if (scrollList.containsKey(flag) && scrollList.get(flag) > 0) {
                mask |= flag.getValue();
            }
        }
        mplew.writeInt(mask);
        if (mask != 0) {
            for (EnchantScrollFlag flag : EnchantScrollFlag.values()) {
                if (scrollList.containsKey(flag) && scrollList.get(flag) > 0) {
                    mplew.writeInt(scrollList.get(flag));
                }
            }
        }
    }

    public static byte[] getZeroWeaponInfo(int weaponlevel, int level, int weapon1, int weapon2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_ZERO_WEAPON_INFO.getValue());
        mplew.writeShort(0);
        mplew.writeInt(weaponlevel);
        mplew.writeInt(level);
        mplew.writeInt(weapon1);
        mplew.writeInt(weapon2);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] getZeroWeaponChangePotential(int meso, int wp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_CHANGE_POTENTIAL_MESO.getValue());
        mplew.writeInt(1);
        mplew.writeInt(meso);
        mplew.writeInt(wp);
        mplew.writeShort(1);

        return mplew.getPacket();
    }

    public static byte[] showZeroWeaponChangePotentialResult(boolean succ) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_CHANGE_POTENTIAL_RESULT.getValue());
        mplew.write(1);
        mplew.writeBool(succ);

        return mplew.getPacket();
    }

    public static byte[] showHyunPotentialResult(List<Integer> potids) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_POTENTIAL_RESULT.getValue());
        mplew.writeShort(potids == null ? 1 : 0);
        mplew.writeInt(0);
        if (potids != null) {
            mplew.writeInt(potids.size() / 2);
            mplew.writeInt(potids.size());
            potids.forEach(mplew::writeInt);
        }

        return mplew.getPacket();
    }

    public static byte[] encodeReturnEffectConfirm(final Item toScroll, final int scrollId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ReturnEffectConfirm.getValue());
        mplew.writeLong(toScroll == null ? -1 : toScroll.getSN());
        mplew.write(toScroll != null);
        if (toScroll != null) {
            PacketHelper.GW_ItemSlotBase_Encode(mplew, toScroll);
            mplew.writeInt(scrollId);
        }

        return mplew.getPacket();
    }

    public static byte[] encodeReturnEffectModified(final Item toScroll, final int scrollId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ReturnEffectModified.getValue());
        mplew.write(toScroll != null);
        if (toScroll != null) {
            PacketHelper.GW_ItemSlotBase_Encode(mplew, toScroll);
            mplew.writeInt(scrollId);
        }

        return mplew.getPacket();
    }

    public static byte[] showCubeResetResult(final int n, final Item toScroll, final int n2, final int n3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort((n2 == 5062090) ? OutHeader.LP_MemorialCubeResult.getValue() : ((n2 == 5062503) ? OutHeader.LP_WhiteAdditionalCubeResult.getValue() : OutHeader.LP_BlackCubeResult.getValue()));
        mplew.writeLong(toScroll.getSN());
        mplew.write(1);
        PacketHelper.GW_ItemSlotBase_Encode(mplew, toScroll);
        mplew.writeInt(n2);
        mplew.writeInt(n);
        mplew.writeInt(n3);
        return mplew.getPacket();
    }

    public static byte[] showCubeResult(final int chrid, final boolean upgrade, final int cubeid, final int position, final int cube_quantity, final Item equip) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        short opcode = 0;
        switch (cubeid) {
            case 5062022: {
                opcode = OutHeader.SHOW_SHININGMIRROR_CUBE_RESULT.getValue();
                break;
            }
            case 5062009: { // 紅色方塊
                opcode = OutHeader.LP_UserItemRedCubeResult.getValue();
                break;
            }
            case 5062500: // 附加方塊
            case 5062501: // [MS特價] 大師附加奇幻方塊
            case 5062502: {
                opcode = OutHeader.LP_UserItemInGameCubeResult.getValue();
                break;
            }
            case 2730000: // 附加系列
            case 2730001: // 附加系列
            case 2730002: // 附加系列
            case 2730004: // 附加系列
            case 2730005: // 附加系列
            case 2730006: {
                opcode = OutHeader.LP_AttachCube.getValue();
                break;
            }
            default: {
                opcode = OutHeader.LP_UserItemInGameCubeResult.getValue();
                break;
            }
        }
        mplew.writeShort(opcode);
        mplew.writeInt(chrid);
        mplew.write(upgrade);
        mplew.writeInt(cubeid);
        mplew.writeInt(position);
        mplew.writeInt(cube_quantity);
        PacketHelper.GW_ItemSlotBase_Encode(mplew, equip);

        return mplew.getPacket();
    }

    public static byte[] showTapJoyInfo(final int slot, final int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        int i1 = itemid % 10 - 1;
        mplew.writeShort(OutHeader.TAP_JOY_INFO.getValue());
        mplew.write(5); // size
        mplew.writeInt(i1);
        mplew.writeInt(slot);
        mplew.writeInt(itemid);
        mplew.write(0);
        mplew.writeInt(350 * (i1 / 2 + 2));
        final int size = ItemConstants.TapJoyReward.getStages().size() / 2;
        mplew.writeInt(5840000 + i1);
        mplew.writeInt(size);
        for (int i = 0; i < size; ++i) {
            mplew.writeInt(i);
            mplew.writeInt(ItemConstants.TapJoyReward.getItemIdAndSN(i * 2).getLeft());
            mplew.writeInt(ItemConstants.TapJoyReward.getItemIdAndSN(i * 2).getRight());
            mplew.writeInt(ItemConstants.TapJoyReward.getItemIdAndSN(i * 2 + 1).getLeft());
            mplew.writeInt(ItemConstants.TapJoyReward.getItemIdAndSN(i * 2 + 1).getLeft());
            mplew.writeInt(100);
            mplew.writeInt(350 * (i / 2 + 2));
            mplew.writeInt(4009441 + i);
            mplew.writeInt(5840000 + i);
        }

        return mplew.getPacket();
    }

    public static byte[] showTapJoy(final int reward) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.TAP_JOY.getValue());
        mplew.writeInt(reward);

        return mplew.getPacket();
    }

    public static byte[] showTapJoyDone(final int mode, final int itemid, final int intValue3, final int gainslot, final int intValue) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.TAP_JOY_DONE.getValue());
        mplew.write(mode);
        mplew.writeInt(itemid);
        mplew.writeInt(intValue3);
        mplew.writeInt(4);
        mplew.writeInt(gainslot);
        mplew.writeInt(0);
        mplew.writeInt(intValue);

        return mplew.getPacket();
    }

    public static byte[] showTapJoyNextStage(final MapleCharacter player, final int n, final int n2, final int n3, final int n4) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.TAP_JOY_NEXT_STAGE.getValue());
        mplew.write(n);
        mplew.writeInt(n3);
        mplew.writeInt(5);
        mplew.writeInt(n2);
        mplew.writeInt(n3);
        mplew.write(n4);
        mplew.writeInt(player.getCSPoints(n4));

        return mplew.getPacket();
    }

    public static byte[] UserToadsHammerResult(final short mode, final Equip equip, final short n2, final List<EnchantScrollEntry> scrollEntryList) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ToadsHammerRequestResult.getValue());
        mplew.writeShort(mode);
        switch (mode) {
            case 1: {
                PacketHelper.GW_ItemSlotBase_Encode(mplew, equip);
                mplew.writeShort(n2);
                break;
            }
            case 2: {
                mplew.write(0);
                mplew.write(scrollEntryList.size());
                for (final EnchantScrollEntry scrollEntry : scrollEntryList) {
                    mplew.writeInt(scrollEntry.getIcon());
                    mplew.writeMapleAsciiString(scrollEntry.getName());
                    mplew.writeInt((scrollEntry.getType()));
                    mplew.writeInt((scrollEntry.getOption()));
                    mplew.writeInt(scrollEntry.getMask());
                    for (int val : scrollEntry.getEnchantScrollStat().values()) {
                        mplew.writeInt(val);
                    }
                    mplew.writeInt(scrollEntry.getCost());
                    mplew.writeInt(scrollEntry.getCost());
                    mplew.write((scrollEntry.getSuccessRate() == 100) ? 1 : 0);
                }
                break;
            }
            case 0: {
                PacketHelper.GW_ItemSlotBase_Encode(mplew, equip);
                break;
            }
        }
        return mplew.getPacket();
    }

    public static byte[] ChangeNameResult(int i, int i1) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserRenameResult.getValue());
        mplew.write(i);
        if (i == 9) {
            mplew.writeInt(i1);
        }
        return mplew.getPacket();
    }

    public static byte[] UserDamageSkinSaveResult(int type, int i1, MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserDamageSkinSaveResult.getValue());
        mplew.write(type);
        switch (type) {
            case 0:
            case 1:
            case 2:
                mplew.write(0);
                break;
            case 3:
                PacketHelper.addDamageSkinInfo(mplew, player);
                break;
            case 4:
                final String questInfo = player.getOneInfo(56829, "count");
                mplew.writeInt((questInfo == null) ? ServerConfig.defaultDamageSkinSlot : Integer.valueOf(questInfo));
                PacketHelper.addDamageSkinInfo(mplew, player);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] CharacterPotentialResult(List<InnerSkillEntry> list, int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CharacterPotentialResult.getValue());
        mplew.writeInt(list.size());
        for (InnerSkillEntry ise : list) {
            mplew.writeInt(ise.getSkillId());
            mplew.write(ise.getSkillLevel());
            mplew.write(ise.getPosition());
            mplew.write(ise.getRank());
        }
        mplew.writeInt(itemId);
        mplew.writeLong(0L);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();

    }

    public static byte[] hiddenTailAndEar(int id, boolean b, boolean equals) {
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.HIDE_SHAMAN_INFO);
        hh.writeInt(id);
        hh.writeBool(b);
        hh.writeBool(equals);
        return hh.getPacket();
    }

}
