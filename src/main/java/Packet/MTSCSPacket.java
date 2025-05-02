package Packet;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleStat;
import Client.MemoEntry;
import Client.inventory.Item;
import Client.inventory.MaplePotionPot;
import Config.configs.CSInfoConfig;
import Config.constants.enums.CashItemModFlag;
import Config.constants.enums.CashItemResult;
import Config.constants.enums.MemoOptType;
import Net.server.MTSStorage;
import Net.server.RaffleItem;
import Net.server.RafflePool;
import Net.server.cashshop.CashItemFactory;
import Net.server.cashshop.CashItemInfo.CashModInfo;
import Net.server.cashshop.CashShop;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MTSCSPacket {

    public static byte[] warpchartoCS(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetCashShop.getValue());
        PacketHelper.addCharacterInfo(mplew, c.getPlayer(), -1);

        return mplew.getPacket();
    }

    public static byte[] warpCS(final boolean custom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetCashShopInfo.getValue());
        if (custom) {
            mplew.writeHexString(CSInfoConfig.CASH_CASHSHOPPACK);
        } else {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
            mplew.writeZeroBytes(6);

            // 1452EF810
            mplew.writeInt(2);
            mplew.writeInt(110100165);
            mplew.writeInt(110100166);
            // 商城道具
            Map<Integer, CashModInfo> allModInfo = CashItemFactory.getInstance().getAllModInfo();
            mplew.writeShort(allModInfo.size());
            for (Map.Entry<Integer, CashModInfo> it : allModInfo.entrySet()) {
                CashModInfo cmi = it.getValue();
                mplew.writeInt(cmi.getSn());
                writeModItemData(mplew, cmi);
            }
            // 套組名稱
            short nCashPackageNameSize = 0;
            mplew.writeShort(nCashPackageNameSize);
            for (int i = 0; i < nCashPackageNameSize; i++) {
                mplew.writeInt(0); // nSN
                mplew.writeMapleAsciiString(""); // m_mCashPackageName
            }
            // 點裝道具隨機箱
            Map<Integer, List<Pair<Integer, Integer>>> randomItemInfo = CashItemFactory.getInstance().getRandomItemInfo();
            final int rndItemSize = (int) randomItemInfo.keySet().stream().filter(k -> k / 1000 == 5533).count();
            mplew.writeInt(rndItemSize);
            for (Map.Entry<Integer, List<Pair<Integer, Integer>>> it : randomItemInfo.entrySet()) {
                mplew.writeInt(it.getKey());
                if (it.getKey() / 1000 != 5533) {
                    break;
                }
                mplew.writeInt(it.getValue().size());
                for (Pair<Integer, Integer> itt : it.getValue()) {
                    mplew.writeInt(itt.left);
                }
            }
            // 限制期限的髮型/整形券
            int nCount = 0;
            mplew.writeInt(nCount);
            for (int i = 0; i < nCount; i++) {
                mplew.writeInt(i);
                mplew.writeInt(0); // 道具SN[F7 A4 98 00] [F8 A4 98 00]
                mplew.writeInt(0); // 道具ID[96 95 4E 00閃亮髮型卷] [C8 9D 4E 00閃亮整型卷]
                mplew.writeLong(0); // [00 A0 C1 29 E5 82 CE 01]
                mplew.writeLong(0); // [00 80 39 0F 01 93 CE 01]
                mplew.writeInt(0); // [00 00 00 00]

                int[] unkData = {0, 20, 30, 40, 50};
                mplew.writeInt(unkData.length);
                for (int j = 0; j < unkData.length; j++) {
                    mplew.writeInt(unkData[i]);
                }
            }

            // 主頁推薦商品
            Map<Integer, Byte> allBaseNewInfo = CashItemFactory.getInstance().getAllBaseNewInfo();
            mplew.writeInt(allBaseNewInfo.size());
            for (Map.Entry<Integer, Byte> it : allBaseNewInfo.entrySet()) {
                mplew.write(it.getValue());
                mplew.writeInt(it.getKey());
            }

            // sub_141B039F0
            nCount = 0;
            mplew.writeInt(nCount);
            for (int i = 0; i < nCount; i++) {
                mplew.writeInt(0);
                // sub_141B03B90
                mplew.writeLong(0);
                mplew.writeLong(0);
                mplew.writeLong(0);
                mplew.writeMapleAsciiString("");
            }

            // CCashShop::DecodeCustomizedPackage
            nCount = 0;
            mplew.writeInt(nCount);
            for (int i = 0; i < nCount; i++) {
                mplew.write(i);
                mplew.writeInt(0);
            }

            // SearchHelper
            nCount = 0;
            mplew.writeInt(nCount);
            for (int i = 0; i < nCount; i++) {
                mplew.writeMapleAsciiString("");
                mplew.writeMapleAsciiString("");
            }

            // m_aStock
            nCount = 0;
            mplew.writeShort(nCount);
            for (int i = 0; i < nCount; i++) {
                mplew.writeLong(0);
            }

            // m_aLimitGoods 自定義禮包?  (240 應該是 454 bytes)
            Map<Integer, CashModInfo> customPackages = new HashMap<>();
            mplew.writeShort(customPackages.size());
            if (customPackages.size() > 0) {
                mplew.writeInt(customPackages.size());
                for (Map.Entry<Integer, CashModInfo> it : customPackages.entrySet()) {
                    mplew.writeInt(it.getKey());
                    mplew.writeInt(it.getValue().getSn());
                    mplew.writeZeroBytes(36);
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.write(0);
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    for (int i = 0; i < 7; i++) {
                        mplew.writeInt(0);
                    }
                    mplew.write(0);
                    mplew.writeMapleAsciiString("");
                    mplew.writeInt(0x3C);
                }
            }

            // Unknown
            nCount = 0;
            mplew.writeInt(nCount);
            for (int i = 0; i < nCount; i++) {
                mplew.writeInt(0);
                int nCount2 = 0;
                mplew.writeInt(nCount2);
                for (int j = 0; j < nCount2; j++) {
                    mplew.writeZeroBytes(24);
                }
            }

            // 道具抽獎隨機箱
            int[] boxes = new int[]{
                    5060048, // 黃金蘋果
                    5060086, // 魔法畫框
                    5680796, // 聊天貼圖隨機箱
                    5222138, // 寵物隨機箱
                    5222123, // 時尚隨機箱
                    5537000 // 萌獸卡牌包
            };
            mplew.writeInt(boxes.length);
            for (int boxID : boxes) {
                byte sw = 1;
                mplew.write(sw);
                if (sw > 0) {
                    mplew.writeInt(boxID);
                    List<RaffleItem> mainRewards = RafflePool.getMainReward(boxID);
                    mplew.writeShort(mainRewards.size());
                    for (RaffleItem item : mainRewards) {
                        mplew.writeInt(item.getItemId());
                    }
                }
            }
        }
        return mplew.getPacket();
    }

    public static void writeModItemData(MaplePacketLittleEndianWriter mplew, CashModInfo cmi) {
        long flags = cmi.getFlags();
        mplew.writeLong(flags);
        if (CashItemModFlag.ITEM_ID.contains(flags)) {
            mplew.writeInt(cmi.getItemid());
        }
        if (CashItemModFlag.COUNT.contains(flags)) {
            mplew.writeShort(cmi.getCount());
        }
        if (CashItemModFlag.PRIORITY.contains(flags)) {
            mplew.write(cmi.getPriority());
        }
        if (CashItemModFlag.PRICE.contains(flags)) {
            mplew.writeInt(cmi.getPrice());
        }
        if (CashItemModFlag.ORIGINAL_PRICE.contains(flags)) {
            mplew.writeInt(cmi.getOriginalPrice());
        }
        if (CashItemModFlag.TOKEN.contains(flags)) {
            mplew.writeInt(0);
        }
        if (CashItemModFlag.BONUS.contains(flags)) {
            mplew.write(cmi.getCsClass());
        }
        if (CashItemModFlag.ZERO.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.PERIOD.contains(flags)) {
            mplew.writeShort(cmi.getPeriod());
        }
        if (CashItemModFlag.REQ_POP.contains(flags)) {
            mplew.writeShort(cmi.getFameLimit());
        }
        if (CashItemModFlag.REQ_LEV.contains(flags)) {
            mplew.writeShort(cmi.getLevelLimit());
        }
        if (CashItemModFlag.MAPLE_POINT.contains(flags)) {
            mplew.writeInt(0);
        }
        if (CashItemModFlag.MESO.contains(flags)) {
            mplew.writeInt(cmi.getMeso());
        }
        if (CashItemModFlag.FOR_PREMIUM_USER.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.COMMODITY_GENDER.contains(flags)) {
            mplew.write(cmi.getGender());
        }
        if (CashItemModFlag.ON_SALE.contains(flags)) {
            mplew.writeBool(cmi.isShowUp());
        }
        if (CashItemModFlag.CLASS.contains(flags)) {
            mplew.write(cmi.getMark());
        }
        if (CashItemModFlag.LIMIT.contains(flags)) {
            mplew.write(1);
        }
        if (CashItemModFlag.PB_CASH.contains(flags)) {
            mplew.writeShort(0);
        }
        if (CashItemModFlag.PB_POINT.contains(flags)) {
            mplew.writeShort(0);
        }
        if (CashItemModFlag.PB_GIFT.contains(flags)) {
            mplew.writeShort(0);
        }
        if (CashItemModFlag.PACKAGE_SN.contains(flags)) {
            int[] sns = new int[0];
            mplew.write(sns.length);
            for (int sn : sns) {
                mplew.writeInt(sn);
            }
        }
        if (CashItemModFlag.TERM_START.contains(flags)) {
            String date = String.valueOf(cmi.getTermStart());
            if (date.length() >= 4) {
                mplew.writeShort(Short.parseShort(date.substring(0, 4)));
                date = date.substring(4);
            } else {
                mplew.writeShort(0);
                date = "";
            }
            for (int i = 0; i < 5; i++) {
                if (date.length() >= 2) {
                    mplew.writeShort(Short.parseShort(date.substring(0, 2)));
                    date = date.substring(2);
                } else {
                    mplew.writeShort(0);
                }
            }
        }
        if (CashItemModFlag.TERM_END.contains(flags)) {
            String date = String.valueOf(cmi.getTermEnd());
            if (date.length() >= 4) {
                mplew.writeShort(Short.parseShort(date.substring(0, 4)));
                date = date.substring(4);
            } else {
                mplew.writeShort(0);
                date = "";
            }
            for (int i = 0; i < 5; i++) {
                if (date.length() >= 2) {
                    mplew.writeShort(Short.parseShort(date.substring(0, 2)));
                    date = date.substring(2);
                } else {
                    mplew.writeShort(0);
                }
            }
        }
        if (CashItemModFlag.REFUNDABLE.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.BOMB_SALE.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.CATEGORY_INFO.contains(flags)) {
            mplew.writeShort(cmi.getCategories()); // [byte] [byte]
        }
        if (CashItemModFlag.WORLD_LIMIT.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.LIMIT_MAX.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.CHECK_QUEST_ID.contains(flags)) {
            mplew.writeInt(0);
        }
        if (CashItemModFlag.DISCOUNT.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.DISCOUNT_RATE.contains(flags)) {
            mplew.writeDouble(0);
        }

        if (CashItemModFlag.MILEAGE_INFO.contains(flags)) {
            mplew.write(0); // 里程折扣
            mplew.write(0); // 只能里程購入
        }
        if (CashItemModFlag.CHECK_QUEST_ID_2.contains(flags)) {
            mplew.writeInt(0);
        }
        if (CashItemModFlag.UNK34.contains(flags)) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (CashItemModFlag.UNK35.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.UNK36.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.COUPON_TYPE.contains(flags)) {
            mplew.writeInt(0);
        }
        if (CashItemModFlag.UNK38.contains(flags)) {
            mplew.writeInt(0);
        }
        if (CashItemModFlag.UNK39.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.UNK40.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.UNK41.contains(flags)) {
            mplew.write(0);
        }
        if (CashItemModFlag.UNK42.contains(flags)) {
            mplew.write(0);
        }
    }

    public static byte[] playCashSong(final int itemid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_PlayJukeBox.getValue());
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    /*
     * 添加玩家使用音樂盒效果
     */
    public static byte[] addCharBox(MapleCharacter c, final int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserMiniRoomBalloon.getValue());
        mplew.writeInt(c.getId());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    /*
     * 取消玩家使用音樂盒效果
     */
    public static byte[] removeCharBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserMiniRoomBalloon.getValue());
        mplew.writeInt(c.getId());
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] useCharm(final int type, final byte charmsleft, final byte daysleft, final int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_ProtectOnDieItemUse.getValue());
        mplew.writeInt(type);
        mplew.write(charmsleft);
        mplew.write(daysleft);
        switch (type) {
            case 1:
            case 2:
                break;
            default:
                mplew.writeInt(itemId);
        }

        return mplew.getPacket();
    }

    public static byte[] useWheel(final byte charmsleft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_UpgradeTombItemUse.getValue());
        mplew.write(charmsleft);

        return mplew.getPacket();
    }

    public static byte[] sendGoldHammerResult(final int n, final int n2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_GoldHammerResult.getValue());
        mplew.write(0);
        mplew.write(n);
        mplew.writeInt(n2);

        return mplew.getPacket();
    }

    /*
     * 白金鎚子效果
     */
    public static byte[] sendPlatinumHammerResult(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_PlatinumHammerResult.getValue());
        mplew.write(value);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] changePetFlag(final int uniqueId, final boolean added, final int flagAdded) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserPetSkillChanged.getValue());
        mplew.writeLong(uniqueId);
        mplew.write((added ? 1 : 0));
        mplew.writeShort(flagAdded);

        return mplew.getPacket();
    }

    public static byte[] changePetName(MapleCharacter chr, String newname, final int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_PetNameChanged.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(0);
        mplew.writeMapleAsciiString(newname);
        mplew.writeInt(slot);

        return mplew.getPacket();
    }

    public static byte[] MemoLoad(List<MemoEntry> memos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MemoResult.getValue());
        mplew.write(MemoOptType.MemoRes_Load.getValue());
        mplew.write(memos.size());
        for (MemoEntry memo : memos) {
            mplew.writeInt(memo.id);
            mplew.write(0);
            mplew.writeInt(0);
            mplew.writeMapleAsciiString(memo.sender);
            mplew.writeMapleAsciiString(memo.message);
            mplew.writeLong(PacketHelper.getTime(memo.timestamp));
            mplew.write(memo.pop);
        }

        return mplew.getPacket();
    }

    public static byte[] MemoSend() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MemoResult.getValue());
        mplew.write(MemoOptType.MemoRes_Send_Succeed.getValue());

        return mplew.getPacket();
    }

    public static byte[] MemoWarn(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MemoResult.getValue());
        mplew.write(MemoOptType.MemoRes_Send_Warning.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] MemoReceive() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MemoResult.getValue());
        mplew.write(MemoOptType.MemoNotify_Receive.getValue());

        return mplew.getPacket();
    }

    public static byte[] MemoDelete() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MemoResult.getValue());
        mplew.write(MemoOptType.MemoRes_Delete_Succeed.getValue());
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /*
     * 小黑板
     */
    public static byte[] useChalkboard(int charid, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserADBoard.getValue());
        mplew.writeInt(charid);
        if (msg == null || msg.length() == 0) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(msg);
        }

        return mplew.getPacket();
    }

    /*
     * 使用瞬移之石
     */
    public static byte[] getTrockRefresh(MapleCharacter chr, byte vip, boolean delete) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /*
         * 高級瞬移之石
         * 2F 00
         * 03 - 添加
         * 02
         * C0 A7 23 06
         * FF C9 9A 3B
         * FF C9 9A 3B
         * FF C9 9A 3B
         * FF C9 9A 3B
         * FF C9 9A 3B
         * FF C9 9A 3B
         * FF C9 9A 3B
         * FF C9 9A 3B
         * FF C9 9A 3B
         * /...困#.??????????????????
         */
        mplew.writeShort(OutHeader.LP_MapTransferResult.getValue());
        mplew.write(delete ? 2 : 3);
        mplew.write(vip);
        if (vip == 1) {
            int[] map = chr.getRegRocks();
            for (int i = 0; i <= 4; i++) {
                mplew.writeInt(map[i]);
            }
        } else if (vip == 2) {
            int[] map = chr.getRocks();
            for (int i = 0; i <= 9; i++) {
                mplew.writeInt(map[i]);
            }
        } else if (vip == 3) {
            int[] map = chr.getHyperRocks();
            for (int i = 0; i <= 12; i++) {
                mplew.writeInt(map[i]);
            }
        }
        return mplew.getPacket();
    }

    /*
     * 使用時空或者超時空卷錯誤提示
     */
    public static byte[] getTrockMessage(byte op) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MapTransferResult.getValue());
        /*
         * 0x05 因未知原因無法移動 0x0B 因某種原因，不能去那裡
         */
        mplew.writeShort(op);

        return mplew.getPacket();
    }

    /*
     * 加載完商城道具就是這個包
     */
    public static byte[] enableCSUse(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CS_USE.getValue());
        /*
         * 0x10 顯示Vip服務界面
         */
        mplew.write(type);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /*
     * 商城購買藥劑罐更新藥劑罐信息
     */
    public static byte[] updatePotionPot(MaplePotionPot potionPot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

//        mplew.writeShort(OutHeader.CS_POTION_POT_UPDATE.getValue());
//        PacketHelper.addPotionPotInfo(mplew, potionPot);
        return mplew.getPacket();
    }

    /**
     * 顯示樂豆點、楓點和里程
     */
    public static byte[] CashShopQueryCashResult(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CashShopQueryCashResult.getValue());
        mplew.writeInt(chr.getCSPoints(1));//樂豆點
        mplew.writeInt(chr.getCSPoints(2));//楓點
        mplew.writeInt(chr.getMileage());//里程
        return mplew.getPacket();
    }

    /**
     *
     */
    public static byte[] SetItemDayTime(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SET_ITEM_DAY_TIME.getValue());
        long Time = System.currentTimeMillis();
        mplew.writeInt(Time);
        return mplew.getPacket();
    }

    public static byte[] showMileageInfo(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CS_CheckMileageResult.getValue());
        mplew.writeInt(c.getMileage());//里程
        // 累積明細
        List<Pair<Triple<Integer, Integer, Integer>, Long>> rechargeRecordList = c.getMileageRechargeRecords();
        mplew.writeInt(rechargeRecordList.size()); // 總數
        for (Pair<Triple<Integer, Integer, Integer>, Long> record : rechargeRecordList) {
            mplew.writeLong(PacketHelper.getTime(record.getRight())); // 日期
            mplew.writeInt(record.getLeft().getLeft()); // 金額
            mplew.writeInt(record.getLeft().getMid()); // 內容 1 - 購買儲值 2 - 活動儲值
            mplew.writeInt(record.getLeft().getRight()); // 狀態
        }

        // 使用明細
        List<Pair<Triple<Integer, Integer, Integer>, Long>> purchaseRecordList = c.getMileagePurchaseRecords();
        mplew.writeInt(purchaseRecordList.size()); // 總數
        for (Pair<Triple<Integer, Integer, Integer>, Long> record : purchaseRecordList) {
            mplew.writeLong(PacketHelper.getTime(record.getRight())); // 日期
            mplew.writeInt(record.getLeft().getLeft()); // 金額
            mplew.writeInt(record.getLeft().getMid()); // 道具ID
            mplew.writeInt(record.getLeft().getRight()); // 110200010
            mplew.writeInt(1); // 狀態
        }

        // 預定刪除
        List<Pair<Integer, Long>> recordList = c.getMileageRecords();
        mplew.writeInt(recordList.size()); // 總數
        for (Pair<Integer, Long> record : recordList) {
            mplew.writeInt(record.getLeft()); // 金額
            mplew.writeLong(PacketHelper.getTime(record.getRight())); // 日期
        }

        return mplew.getPacket();
    }

    /**
     * 刷新角色在商城中的楓幣信息
     */
    public static byte[] updataMeso(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCharStatChanged.getValue());
        mplew.writeLong(MapleStat.楓幣.getValue());
        mplew.writeLong(chr.getMeso());

        return mplew.getPacket();
    }

    /*
     * 顯示商城道具欄物品
     * getCSInventory
     */
    public static byte[] loadLockerDone(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_LoadLocker_Done);
        CashShop mci = c.getPlayer().getCashInventory();
        mplew.write(0); //V.109 新增
        mplew.writeShort(mci.getItemsSize());
        for (Item itemz : mci.getInventory()) {
            addCashItemInfo(mplew, itemz, c.getAccID(), 0);
            mplew.write(0);
        }
        mplew.writeShort(c.getPlayer().getTrunk().getSlots()); // 倉庫數量
        mplew.writeShort(c.getAccCharSlots()); // 可以創建的角色數量
        mplew.writeShort(0);
        mplew.writeShort(c.getPlayer().getMapleUnion().getAllUnions().size()); // 已創建的角色數量

        return mplew.getPacket();
    }

    /*
     * 顯示商城的禮物
     * getCSGifts
     */
    public static byte[] 商城禮物信息(List<? extends Pair<Item, String>> gifts) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_LoadGift_Done);
        mplew.writeShort(gifts.size());
        for (Pair<Item, String> gift : gifts) {
            mplew.writeLong(gift.getLeft().getSN());
            mplew.writeInt((gift.getLeft()).getItemId());
            mplew.writeAsciiString((gift.getLeft()).getGiftFrom(), 15);
            mplew.writeAsciiString(gift.getRight(), 75);
        }

        return mplew.getPacket();
    }

    /*
     * 商城購物車
     * sendWishList
     */
    public static byte[] sendWishList(MapleCharacter chr, boolean update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write((update ? CashItemResult.CashItemRes_SetWish_Done : CashItemResult.CashItemRes_LoadWish_Done));
        int[] list = chr.getWishlist();
        for (int i = 0; i < 12; i++) {
            mplew.writeInt(list[i] != -1 ? list[i] : 0);
        }
        return mplew.getPacket();
    }

    /*
     * 購買商城物品
     * showBoughtCSItem
     */
    public static byte[] CashItemBuyDone(Item item, int sn, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_Buy_Done);
        addCashItemInfo(mplew, item, accid, sn);
        return mplew.getPacket();
    }

    /*
     * 商城送禮物
     */
    public static byte[] 商城送禮(int itemid, int quantity, String receiver) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_Gift_Done);
        mplew.writeMapleAsciiString(receiver);
        mplew.writeInt(itemid);
        mplew.writeShort(quantity);

        return mplew.getPacket();

    }

    public static byte[] 擴充道具欄(int inv, int slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_IncSlotCount_Done);
        mplew.write(inv);
        mplew.writeShort(slots);
        mplew.writeInt(0); //V.114新增 未知
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] 擴充倉庫(int slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_IncTrunkCount_Done);
        mplew.writeShort(slots);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] 購買角色卡(int slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_IncCharSlotCount_Done);
        mplew.writeShort(slots);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] 擴充項鏈(int days) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_EnableEquipSlotExt_Done);
        mplew.writeShort(0);
        mplew.writeShort(days);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /*
     * 商城-->背包
     */
    public static byte[] moveItemToInvFormCs(Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_MoveLtoS_Done);
        mplew.write(item.getQuantity());
        mplew.writeShort(item.getPosition());
        PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
        mplew.writeInt(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    /*
     * 背包-->商城
     */
    public static byte[] moveItemToCsFromInv(Item item, int accId, int sn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_MoveStoL_Done);
        mplew.write(0);
        addCashItemInfo(mplew, item, accId, sn);
        mplew.write(0);
        return mplew.getPacket();
    }

    /*
     * 商城刪除道具
     */
    public static byte[] 商城刪除道具(int uniqueid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_Destroy_Done);
        mplew.writeLong(uniqueid);

        return mplew.getPacket();
    }

    /*
     * 商城道具到期
     */
    public static byte[] cashItemExpired(long uniqueid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_Expire_Done);
        mplew.writeLong(uniqueid);

        return mplew.getPacket();
    }

    /*
     * 商城換購道具
     */
    public static byte[] 商城換購道具(int uniqueId, int Money) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_Rebate_Done);
        mplew.writeLong(uniqueId);
        mplew.writeInt(Money);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /*
     * 商城購買禮包
     */
    public static byte[] 商城購買禮包(Map<Integer, ? extends Item> packageItems, int accId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_BuyPackage_Done);
        mplew.write(packageItems.size());
//        int size = 0;
        for (Map.Entry<Integer, ? extends Item> it : packageItems.entrySet()) {
            addCashItemInfo(mplew, it.getValue(), accId, it.getKey());
            mplew.write(0);
//            if (ItemConstants.類型.寵物(it.getValue().getItemId()) || ItemConstants.getInventoryType(it.getValue().getItemId()) == MapleInventoryType.EQUIP) {
//                size++;
//            }
        }
//        mplew.writeInt(size);
//        for (Item it : packageItems.values()) {
//            if (ItemConstants.類型.寵物(it.getItemId()) || ItemConstants.getInventoryType(it.getItemId()) == MapleInventoryType.EQUIP) {
//                PacketHelper.addItemInfo(mplew, it);
//            }
//        }
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    /*
     * 商城贈送禮包
     */
    public static byte[] 商城送禮包(int itemId, int quantity, String receiver) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_GiftPackage_Done);
        mplew.writeMapleAsciiString(receiver);
        mplew.writeInt(itemId);
        mplew.writeShort(quantity);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /*
     * 商城購買任務物品
     */
    public static byte[] 商城購買任務道具(int price, short quantity, byte position, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_BuyNormal_Done);
        mplew.writeInt(1);
        mplew.writeInt(quantity); // 不確定
//        mplew.writeShort(quantity);
//        mplew.writeShort((short) position);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] 楓點兌換道具() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.楓點兌換道具);

        return mplew.getPacket();
    }

    public static byte[] 購買記錄(int sn, int quantity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.購買記錄);
        mplew.writeInt(sn);
        mplew.writeInt(quantity);

        return mplew.getPacket();
    }

    public static void addCashItemInfo(MaplePacketLittleEndianWriter mplew, Item item, int accId, int sn) {
        CashItemFactory cashinfo = CashItemFactory.getInstance();
        mplew.writeLong(item.getSN() > 0 ? item.getSN() : 0);
        mplew.writeInt(accId);
        mplew.writeInt(0);
        mplew.writeInt(item.getItemId());
        mplew.writeInt(sn);
        mplew.writeShort(item.getQuantity());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeLong(item.getTrueExpiration());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.write(0);
        mplew.write(1);
        mplew.write(2);
        mplew.writeInt(item.getItemId());
        mplew.write(1);
        mplew.writeInt(sn);
        mplew.writeInt(0);
        mplew.writeLong(item.getTrueExpiration());
        mplew.writeInt(-1);
        mplew.write(0);
        mplew.writeInt(1);
        mplew.writeHexString("00 00 04 00 00 00 00 00 00 00 90 90 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 30 00 35 00 00 00 0A 00");
    }

    public static byte[] 商城錯誤提示(int err) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.CashItemRes_Gift_Failed);
        mplew.write(err);

        return mplew.getPacket();
    }

    /*
     * 商城領獎卡提示
     */
    public static byte[] showCouponRedeemedItem(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.writeShort(CashItemResult.領獎卡提示);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.writeShort(26);
        mplew.writeInt(itemid);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /*
     * 商城領獎卡提示
     */
    public static byte[] showCouponRedeemedItem(Map<Integer, ? extends Item> items, int mesos, int maplePoints, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write(CashItemResult.領獎卡提示);
        mplew.write(items.size());
        for (Map.Entry<Integer, ? extends Item> it : items.entrySet()) {
            addCashItemInfo(mplew, it.getValue(), c.getAccID(), it.getKey());
            mplew.write(0);
        }
        mplew.writeInt(maplePoints);
        mplew.writeInt(0); // Normal items size
        mplew.writeInt(mesos);

        return mplew.getPacket();
    }

    /*
     * 不發這個好像買不了商城道具
     */
    public static byte[] redeemResponse() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        /*
         * T075 0xB9
         * V111 0xBC
         */
        mplew.write(CashItemResult.註冊商城);
        mplew.writeInt(0);
        mplew.writeInt(1);

        return mplew.getPacket();
    }

    /*
     * 商城中打開箱子
     */
    public static byte[] 商城打開箱子(Item item, Long uniqueId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CashShopCashItemResult.getValue());
        mplew.write((int) CashItemResult.打開箱子);
        mplew.writeLong(uniqueId);
        mplew.writeInt(0);
        PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
        mplew.writeInt(item.getPosition()); //道具在背包中的位置
        mplew.writeZeroBytes(3);

        return mplew.getPacket();
    }

    public static int getTime() {
        return Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
    }

    /*
     * 使用楓幣包失敗
     */
    public static byte[] sendMesobagFailed() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MesoGive_Failed.getValue());
        return mplew.getPacket();
    }

    /*
     * 使用楓幣包成功
     */
    public static byte[] sendMesobagSuccess(int mesos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MesoGive_Succeeded.getValue());
        mplew.writeInt(mesos);
        return mplew.getPacket();
    }

    public static byte[] sendMTS(List<? extends MTSStorage.MTSItemInfo> items, int tab, int type, int page, int pages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        /*
         * T065 0x14
         */
        mplew.write(0x15); //operation
        mplew.writeInt(pages); //total items
        mplew.writeInt(items.size()); //number of items on this page
        mplew.writeInt(tab);
        mplew.writeInt(type);
        mplew.writeInt(page);
        mplew.write(1);
        mplew.write(1);

        for (MTSStorage.MTSItemInfo item : items) {
            addMTSItemInfo(mplew, item);
        }
        mplew.write(0); //0 or 1?

        return mplew.getPacket();
    }

    /*
     * 在拍賣中顯示角色楓點
     */
    public static byte[] showMTSCash(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.GET_MTS_TOKENS.getValue());
        mplew.writeInt(chr.getCSPoints(2));

        return mplew.getPacket();
    }

    public static byte[] getMTSWantedListingOver(int nx, int items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(0x3D);
        mplew.writeInt(nx);
        mplew.writeInt(items);

        return mplew.getPacket();
    }

    public static byte[] getMTSConfirmSell() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(0x1D);

        return mplew.getPacket();
    }

    public static byte[] getMTSFailSell() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(0x1E);
        mplew.write(0x42);

        return mplew.getPacket();
    }

    public static byte[] getMTSConfirmBuy() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(0x33);

        return mplew.getPacket();
    }

    public static byte[] getMTSFailBuy() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(0x34);
        mplew.write(0x42);

        return mplew.getPacket();
    }

    public static byte[] getMTSConfirmCancel() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(0x25);

        return mplew.getPacket();
    }

    public static byte[] getMTSFailCancel() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(0x26);
        mplew.write(0x42);

        return mplew.getPacket();
    }

    public static byte[] getMTSConfirmTransfer(int quantity, int pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(quantity);
        mplew.writeInt(pos);

        return mplew.getPacket();
    }

    private static void addMTSItemInfo(MaplePacketLittleEndianWriter mplew, MTSStorage.MTSItemInfo item) {
        PacketHelper.GW_ItemSlotBase_Encode(mplew, item.getItem());
        mplew.writeInt(item.getId()); //id
        mplew.writeInt(item.getTaxes()); //this + below = price
        mplew.writeInt(item.getPrice()); //price
        mplew.writeZeroBytes(8);
        mplew.writeLong(PacketHelper.getTime(item.getEndingDate()));
        mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
        mplew.writeMapleAsciiString(item.getSeller()); //char name
        mplew.writeZeroBytes(28);
    }

    public static byte[] getNotYetSoldInv(List<? extends MTSStorage.MTSItemInfo> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(0x23);
        mplew.writeInt(items.size());
        for (MTSStorage.MTSItemInfo item : items) {
            addMTSItemInfo(mplew, item);
        }

        return mplew.getPacket();
    }

    public static byte[] getTransferInventory(List<? extends Item> items, boolean changed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(0x21);

        mplew.writeInt(items.size());
        int i = 0;
        for (Item item : items) {
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
            mplew.writeInt(Integer.MAX_VALUE - i); //fake ID
            mplew.writeZeroBytes(56);//really just addMTSItemInfo
            i++;
        }
        mplew.writeInt(-47 + i - 1);
        mplew.write(changed ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] addToCartMessage(boolean fail, boolean remove) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        if (remove) {
            if (fail) {
                mplew.write(0x2C);
                mplew.writeInt(-1);
            } else {
                mplew.write(0x2B); //T065 0x28
            }
        } else {
            if (fail) {
                mplew.write(0x2A);
                mplew.writeInt(-1);
            } else {
                mplew.write(0x29); //T065 0x26
            }
        }

        return mplew.getPacket();
    }

    public static byte[] sendBuyFailed(byte result) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(CashItemResult.CashItemRes_Buy_Failed);
        mplew.write(result);

        return mplew.getPacket();
    }

    public static byte[] sendCoupleDone(final int accId, final String s, final int sn, final Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(CashItemResult.CashItemRes_Couple_Done);
        addCashItemInfo(mplew, item, accId, sn);
        mplew.write(0);
        mplew.writeMapleAsciiString(s);
        mplew.writeInt(item.getItemId());
        mplew.writeShort(item.getQuantity());
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] sendCoupleFailed(byte result) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MTS_OPERATION.getValue());
        mplew.write(CashItemResult.CashItemRes_Couple_Failed);
        mplew.write(result);
        if (result == CashItemResult.CashItemRes_IncBuyCharCount_Failed || result == CashItemResult.CashItemRes_IncBuyCharCount_Done) {
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static byte[] RechargeWeb(String website) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CashShopChargeParamResult.getValue());
        mplew.writeMapleAsciiString(website);
        return mplew.getPacket();
    }

    public static byte[] showAvatarRandomBox(boolean full, long cashId, int quantity, Item item, int accid, boolean showItemName, boolean smega) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CS_AVATAR_RANDOM_BOX.getValue());
        int ful = 148;
        int notful = 147;
        mplew.write(full ? ful : notful);
        if (!full) {
            mplew.writeLong(cashId);
            mplew.writeInt(quantity);
            addCashItemInfo(mplew, item, accid, 0);
            mplew.write(0);
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
            mplew.writeInt(item.getItemId());
            mplew.write(showItemName);
            mplew.write(smega);
        }

        return mplew.getPacket();
    }

    public static byte[] getCashShopPreviewInfo() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CashShopPreviewInfo.getValue());

        byte unkCouponTypeSize = 0;
        mplew.write(unkCouponTypeSize);
        for (int i = 0; i < unkCouponTypeSize; i++) {
            short unkCouponSize = 0;
            mplew.write(unkCouponSize > 0);
            if (unkCouponSize > 0) {
                mplew.writeInt(0); // nCouponItemId
                mplew.write(0);
                mplew.writeShort(unkCouponSize);
                for (int j = 0; j < unkCouponSize; j++) {
                    mplew.writeInt(0); // MaleRewardItemId
                    mplew.writeInt(0); // FemaleRewardItemId
                }
            }
        }

        byte unkCoupon2TypeSize = 0;
        mplew.write(unkCoupon2TypeSize);
        for (int i = 0; i < unkCoupon2TypeSize; i++) {
            short unkCoupon2Size = 0;
            mplew.write(unkCoupon2Size > 0);
            if (unkCoupon2Size > 0) {
                mplew.writeInt(0); // nCouponItemId
                short unkCoupon2RewardListSize = 0;
                mplew.writeShort(unkCoupon2RewardListSize);
                for (int j = 0; j < unkCoupon2RewardListSize; j++) {
                    byte unkCoupon2RewardSize = 0;
                    mplew.write(unkCoupon2RewardSize > 0);
                    if (unkCoupon2RewardSize > 0) {
                        mplew.writeShort(unkCoupon2RewardSize);
                        for (int k = 0; k < unkCoupon2RewardSize; k++) {
                            mplew.writeInt(0); // MaleRewardItemId
                            mplew.writeInt(0); // FemaleRewardItemId
                        }
                    }
                }
            }
        }

        return mplew.getPacket();
    }

    public static byte[] getCashShopStyleCouponPreviewInfo() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CashShopStyleCouponPreviewInfo.getValue());

        mplew.write(0);

        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis())); // 開始時間

        Map<Integer, Pair<List<Integer>, List<Integer>>> couponList = RafflePool.getRoyalCouponList();
        mplew.writeInt(couponList.size());
        for (Map.Entry<Integer, Pair<List<Integer>, List<Integer>>> couponInfo : couponList.entrySet()) {
            mplew.writeInt(0);
            mplew.writeInt(couponInfo.getKey());
            mplew.writeInt((couponInfo.getValue().getLeft() == null ? 0 : couponInfo.getValue().getLeft().size()) + (couponInfo.getValue().getRight() == null ? 0 : couponInfo.getValue().getRight().size()));
            mplew.writeLong(PacketHelper.getTime(-2));
            mplew.writeLong(PacketHelper.getTime(-1));
            mplew.writeInt((couponInfo.getValue().getLeft() == null || couponInfo.getValue().getLeft().isEmpty() ? 0 : 1) + (couponInfo.getValue().getRight() == null || couponInfo.getValue().getRight().isEmpty() ? 0 : 1));
            if (couponInfo.getValue().getLeft() != null && !couponInfo.getValue().getLeft().isEmpty()) {
                mplew.write(0);
                mplew.writeInt(2); // 類型 1 選擇 2 隨機
                mplew.writeInt(couponInfo.getValue().getLeft().size());
                for (int styleID : couponInfo.getValue().getLeft()) {
                    mplew.writeInt(styleID);
                }
            }
            if (couponInfo.getValue().getRight() != null && !couponInfo.getValue().getRight().isEmpty()) {
                mplew.write(1);
                mplew.writeInt(2); // ??
                mplew.writeInt(couponInfo.getValue().getRight().size());
                for (int styleID : couponInfo.getValue().getRight()) {
                    mplew.writeInt(styleID);
                }
            }
        }


        return mplew.getPacket();
    }
}
