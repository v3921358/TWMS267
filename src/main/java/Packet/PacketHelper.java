package Packet;

import Client.*;
import Client.hexa.MapleHexaSkill;
import Client.hexa.MapleHexaStat;
import Client.inventory.*;
import Client.skills.InnerSkillEntry;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Client.stat.MapleHyperStats;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Config.constants.skills.凱撒;
import Config.constants.skills.幻影俠盜;
import Config.constants.skills.陰陽師;
import Net.server.MapleItemInformationProvider;
import Net.server.movement.LifeMovementFragment;
import Net.server.quest.MapleQuest;
import Net.server.shop.*;
import Net.server.shops.AbstractPlayerStore;
import Net.server.shops.IMaplePlayerShop;
import Net.server.shops.MapleMiniGame;
import connection.OutPacket;
import tools.*;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

public class PacketHelper {

    public static final long MAX_TIME = 150842304000000000L; //00 80 05 BB 46 E6 17 02
    public static final long ZERO_TIME = 94354848000000000L; //00 40 E0 FD 3B 37 4F 01
    public static final long PERMANENT = 150841440000000000L; //00 C0 9B 90 7D E5 17 02

    public static long getKoreanTimestamp(long realTimestamp) {
        return realTimestamp * 10000 + 116445060000000000L;
    }

    public static final long getTime(long realTimestamp) {
        if (realTimestamp == -1L) {
            return 150842304000000000L; // 00 80 05 BB 46 E6 17 02
        }
        if (realTimestamp == -2L) {
            return 94354848000000000L; // 00 40 E0 FD 3B 37 4F 01
        }
        if (realTimestamp == -3L) {
            return 94354848000000000L;
        }
        if (realTimestamp == -4L) {
            return 94354848000000000L;
        }
        return realTimestamp * 10000L + 116445060000000000L;
    }

    /*
     * 藥劑罐信息
     */
    public static void addPotionPotInfo(MaplePacketLittleEndianWriter mplew, MaplePotionPot potionPot) {
        mplew.writeInt(potionPot.getItmeId());
        mplew.writeInt(potionPot.getChrId());
        mplew.writeInt(potionPot.getMaxValue());
        mplew.writeInt(potionPot.getHp());
        mplew.writeInt(potionPot.getMp());
        mplew.writeLong(getTime(potionPot.getStartDate()));
        mplew.writeLong(getTime(potionPot.getEndDate()));
    }


    public static void writeBuffMask(MaplePacketLittleEndianWriter mplew, Collection<Pair<SecondaryStat, Pair<Integer, Integer>>> statups) {
        int[] mask = new int[31];
        for (Pair<SecondaryStat, Pair<Integer, Integer>> statup : statups) {
            mask[statup.left.getPosition() - 1] = mask[statup.left.getPosition() - 1] | statup.left.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[i - 1]);
        }
    }

    public static List<Pair<SecondaryStat, List<SecondaryStatValueHolder>>> sortIndieBuffStats(Map<SecondaryStat, List<SecondaryStatValueHolder>> statups) {
        boolean changed;
        List<Pair<SecondaryStat, List<SecondaryStatValueHolder>>> statvals = new ArrayList<>();
        for (Entry<SecondaryStat, List<SecondaryStatValueHolder>> stat : statups.entrySet()) {
            statvals.add(new Pair<>(stat.getKey(), stat.getValue()));
        }
        do {
            changed = false;
            int i = 0;
            int k = 1;
            for (int iter = 0; iter < statvals.size() - 1; iter++) {
                Pair<SecondaryStat, List<SecondaryStatValueHolder>> a = statvals.get(i);
                Pair<SecondaryStat, List<SecondaryStatValueHolder>> b = statvals.get(k);
                if (a != null && b != null && a.left.getFlag() > b.left.getFlag()) {
                    Pair<SecondaryStat, List<SecondaryStatValueHolder>> swap = new Pair<>(a.left, a.right);
                    statvals.remove(i);
                    statvals.add(i, b);
                    statvals.remove(k);
                    statvals.add(k, swap);
                    changed = true;
                }
                i++;
                k++;
            }
        }
        while (changed);
        return statvals;
    }

    public static void addPartTimeJob(MaplePacketLittleEndianWriter mplew, MaplePartTimeJob parttime) {
        mplew.write(parttime.getJob());
        if (parttime.getJob() > 0 && parttime.getJob() <= 5) {
            //mplew.writeHexString("6B E2 D0 01 30 C0 D4 DD");
            mplew.writeReversedLong(parttime.getTime());
        } else {
            mplew.writeReversedLong(getTime(-2));
        }
        mplew.writeInt(parttime.getReward());
        mplew.writeBool(parttime.getReward() > 0);
    }

    public static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time) {
        mplew.writeLong(getTime(time));
    }

    public static void addItemPosition(MaplePacketLittleEndianWriter mplew, Item item, boolean trade, boolean bagSlot) {
        short pos;
        if (item == null) {
            pos = 0;
        } else {
            pos = item.getPosition();
            if (pos < 0) {
                pos = (short) Math.abs(pos);
                if (pos > 100 && pos < 1000) {
                    pos -= 100;
                }
            }
            if (bagSlot) {
                pos = (short) ((pos % 100) - 1);
            }
        }
        if (bagSlot) {
            mplew.writeInt(pos);
        } else if (!trade) {
            mplew.writeShort(pos);
        } else {
            mplew.write(pos);
        }
    }

    public static void GW_ItemSlotBase_Encode(MaplePacketLittleEndianWriter mplew, Item item) {
        if (item == null) {
            throw new NullPointerException("addItemInfo item is null.");
        }
        OutPacket outPacket = new OutPacket();
        item.encode(outPacket);

        mplew.write(outPacket.getData());
    }

    public static void serializeMovementList(MaplePacketLittleEndianWriter mplew, int gatherDuration, int nVal1, Point mPos, Point oPos, List<LifeMovementFragment> moves, int[] arrays) {
        mplew.writeInt(gatherDuration);
        mplew.writeInt(nVal1);
        mplew.writePos(mPos);
        mplew.writePos(oPos);
        if (moves == null) { // from mobMove and idk why null
            mplew.writeShort(0);
        } else {
            mplew.writeShort(moves.size());
            for (LifeMovementFragment move : moves) {
                move.serialize(mplew);
            }
        }

        if (arrays != null) {
            // Changed from mplew.write(arrays.length);
            mplew.writeShort(arrays.length);
            for (int nVal : arrays) {
                mplew.writeInt(nVal); // Assuming nVal should be written as an integer
            }
        }
    }


    public static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(chr) && chr.getPlayerShop().getShopType() != 1 && chr.getPlayerShop().isAvailable()) {
            addInteraction(mplew, chr.getPlayerShop());
        } else {
            mplew.write(0);
        }
    }

    public static void addInteraction(MaplePacketLittleEndianWriter mplew, IMaplePlayerShop shop) {
        mplew.write(shop.getGameType());
        mplew.writeInt(((AbstractPlayerStore) shop).getObjectId());
        mplew.writeMapleAsciiString(shop.getDescription());
        if (shop.getShopType() != 1) {
            mplew.write(shop.getPassword().length() > 0 ? 1 : 0); //password = false
        }
        int id = shop.getItemId() % 100;
        if (shop instanceof MapleMiniGame mini) {
            id = mini.getPieceType();
        } else if (shop.getShopType() == 75) {
            id = 0;
        }
        mplew.write(id); //應該是商店的外觀 以前是: shop.getItem() % 10   shop.getItem() - 5030000
        mplew.write(shop.getShopType() != 75 ? shop.getSize() : 0); //current size
        mplew.write(shop.getMaxSize()); //full slots... 4 = 4-1=3 = has slots, 1-1=0 = no slots
        if (shop.getShopType() != 1) {
            mplew.write(shop.isOpen() ? 0 : 1);
        }
        PacketHelper.addChaterName(mplew, "", "");
    }

    /**
     * 添加角色相關數據：屬性、道具、任務、技能等
     *
     * @param mplew
     * @param chr
     * @param flag
     */
    public static void addCharacterInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, long flag) {
        for (int i = 0; i < 100; i++) {
            mplew.write(1);
        }
        mplew.write(0);

        for (int i = 0; i < 3; ++i) {
            mplew.writeInt(JobConstants.is皇家騎士團(chr.getJob()) || JobConstants.is米哈逸(chr.getJob()) ? -6 : -1);
        }
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(0);
        if ((flag & 0x1L) != 0x0L) {
            chr.getCharacterStat().encode(mplew);
            mplew.write(chr.getBuddylist().getCapacity());//好友欄上限
            mplew.write(chr.getBlessOfFairyOrigin() != null); // 精靈的祝福
            if (chr.getBlessOfFairyOrigin() != null) {
                mplew.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
            }
            mplew.write(chr.getBlessOfEmpressOrigin() != null); // 女皇的祝福
            if (chr.getBlessOfEmpressOrigin() != null) {
                mplew.writeMapleAsciiString(chr.getBlessOfEmpressOrigin());
            }
            // 終極冒險家訊息
            MapleQuestStatus ultExplorer = chr.getQuestNoAdd(MapleQuest.getInstance(GameConstants.ULT_EXPLORER));
            mplew.write((ultExplorer != null) && (ultExplorer.getCustomData() != null));
            if ((ultExplorer != null) && (ultExplorer.getCustomData() != null)) {
                mplew.writeMapleAsciiString(ultExplorer.getCustomData());
            }
            /*1B0*/
            mplew.writeLong(133709900400000000L); //創建角色的日期
            mplew.writeHexString("00 40 E0 FD 3B 37 4F 01"); // Zero Timer ->

            int v7 = 2;
            do {
                mplew.writeInt(0);
                while (true) {
                    int res = -1;
                    mplew.write(res);
                    if (res == -1) {
                        break;
                    }
                    mplew.writeInt(0);
                }
                v7 += 36;
            } while (v7 < 74);
        }
        // DBChar_Option62
        mplew.writeShort(0);
        mplew.writeLong(getTime(-2)); // Zero Timer ->

        // DBChar_Option_66
        final String questInfo = chr.getOneInfo(56829, "count");
        mplew.writeInt((questInfo == null) ? ServerConfig.defaultDamageSkinSlot : Integer.valueOf(questInfo));// DamageSkinMaxSolt

        // DBChar_Option_67
        addDamageSkinInfo(mplew, chr);

        if ((flag & 0x2L) != 0x0L) { // DBChar_Option1
            mplew.writeLong(chr.getMeso()); // 楓幣 V.110修改以前是 Int
            mplew.writeInt(chr.getId());  // 角色ID
            mplew.writeInt(chr.getBeans()); // 豆豆
            mplew.writeInt(chr.getCSPoints(2)); // 楓葉點數
        }
        if ((flag & 0x40L) != 0x0L) {
            mplew.writeInt(chr.getInventory(MapleInventoryType.EQUIPPED).getSlotLimit()); // equip slots
            mplew.writeInt(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit()); // equip slots
            mplew.writeInt(chr.getInventory(MapleInventoryType.USE).getSlotLimit()); // use slots
            mplew.writeInt(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit()); // set-up slots
            mplew.writeInt(chr.getInventory(MapleInventoryType.ETC).getSlotLimit()); // etc slots
            mplew.writeInt(chr.getInventory(MapleInventoryType.CASH).getSlotLimit()); // cash slots
            mplew.writeInt(chr.getInventory(MapleInventoryType.DECORATION).getSlotLimit()); // decoration slots
        }
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        List<Item> equippedList = iv.newList(); //獲取裝備中的道具列表
        Collections.sort(equippedList); //對道具進行排序
        List<Item> equipped = new ArrayList<>(); // 普通裝備
        List<Item> equippedCash = new ArrayList<>(); // 現金裝備
        List<Item> equippedDragon = new ArrayList<>(); // 龍裝備
        List<Item> equippedMechanic = new ArrayList<>(); // 機甲裝備
        List<Item> equippedAndroid = new ArrayList<>(); // 機器人的裝備
        List<Item> equippedLolitaCash = new ArrayList<>(); // 天使破壞者裝備
        List<Item> equippedBit = new ArrayList<>(); // 拼圖
        List<Item> equippedZeroBetaCash = new ArrayList<>(); // 神之子培塔時裝
        List<Item> equippedArcane = new ArrayList<>(); // 秘法符文
        List<Item> equippedAuthenticSymbol = new ArrayList<>(); // 真實符文
        List<Item> equippedTotem = new ArrayList<>(); // 圖騰
        List<Item> equippedMonsterEqp = new ArrayList<>(); // 獸魔裝備
        List<Item> equippedHakuFan = new ArrayList<>(); // 花狐裝備
        List<Item> equippedUnknown = new ArrayList<>(); // 未知
        List<Item> equippedCashPreset = new ArrayList<>(); // 現金裝備
        for (Item item : equippedList) {
            if (item.getPosition() < 0 && item.getPosition() > -100) { // 普通裝備
                equipped.add(item);
            } else if (item.getPosition() <= -1000 && item.getPosition() > -1100) { // 龍裝備 龍面具(1000), 龍墜飾(1001), 龍之翼(1002), 龍尾巴(1003)
                equippedDragon.add(item);
            } else if (item.getPosition() <= -1100 && item.getPosition() > -1200) { // 機甲裝備 戰神引擎(1100), 戰神手臂(1101), 戰神腿部(1102), 戰神身軀(1103), 戰神電晶體(1104)
                equippedMechanic.add(item);
            } else if (item.getPosition() <= -1400 && item.getPosition() > -1500) { // 拼圖(1400)~(1425)
                equippedBit.add(item);
            } else if (item.getPosition() <= -5000 && item.getPosition() >= -5002) { // 圖騰(5000)~(5002)
                equippedTotem.add(item);
            } else if (item.getPosition() <= -1600 && item.getPosition() > -1700) { // 秘法符文 (1600)~(1605)
                equippedArcane.add(item);
            } else if (item.getPosition() <= -1700 && item.getPosition() > -1800) { // 真實符文 (1700)~(1705)
                equippedAuthenticSymbol.add(item);
            } else if (item.getPosition() <= -1200 && item.getPosition() > -1300) { // 機器人的裝備 帽子(1200), 披風(1201), 臉飾(1202), 上衣(1203), 褲裙(1204), 鞋子(1205), 手套(1206)
                equippedAndroid.add(item);
            } else if (item.getPosition() <= -1300 && item.getPosition() > -1400) { // 天使破壞者裝備 帽子(1300), 披風(1301), 臉飾(1302), 上衣(1303), 手套(1304)
                equippedLolitaCash.add(item);
            } else if (item.getPosition() <= -1500 && item.getPosition() > -1600) { // 神之子培塔時裝 眼飾(1500), 帽子(1501), 臉飾(1502), 耳環(1503), 披風(1504), 上衣(1505), 手套(1506), 武器(1507), 褲裙(1508), 鞋子(1509), 戒指1(1510), 戒指2(1511)
                equippedZeroBetaCash.add(item);
            } else if (item.getPosition() <= -100 && item.getPosition() > -1000) { //現金裝備
                equippedCash.add(item);
            } else if (item.getPosition() > 10000 && item.getPosition() < 10200) { // 未知
                chr.getSkillSkin().put(MapleItemInformationProvider.getInstance().getSkillSkinFormSkillId(item.getItemId()), item.getItemId());
                equippedUnknown.add(item);
            }
        }
        if ((flag & 0x80L) != 0x0L) {
            mplew.writeBool(false);
            iv = chr.getInventory(MapleInventoryType.EQUIP);
            List<MapleAndroid> androids = new LinkedList<>();
            List<Item> items20000 = new ArrayList<>();
            List<Item> items21000 = new ArrayList<>();
            List<Item> equip = new ArrayList<>();
            for (Item item : iv.list()) {
                if (((Equip) item).getAndroid() != null) {
                    androids.add(((Equip) item).getAndroid());
                }
                if (item.getPosition() >= 21000) {
                    items21000.add(item);
                } else if (item.getPosition() >= 20000) {
                    items20000.add(item);
                } else {
                    equip.add(item);
                }
            }
            // 1
            mplew.writeShort(0);
            // 2
            encodeInventory(mplew, equipped, chr);
            // 開始加載裝備欄道具    3
            encodeInventory(mplew, equip, chr);
            // 開始加載龍裝備    4
            encodeInventory(mplew, equippedDragon, chr);
            // 開始加載機甲裝備    5
            encodeInventory(mplew, equippedMechanic, chr);
            // 開始加載拼圖    6
            encodeInventory(mplew, equippedBit, chr);
            // 開始加載獸魔裝備    7
            encodeInventory(mplew, equippedTotem, chr);
            // 開始加載秘法符文    8
            encodeInventory(mplew, equippedArcane, chr);
            // 開始加載真實符文    9
            encodeInventory(mplew, equippedAuthenticSymbol, chr);
            // 開始加載花狐裝備    10
            encodeInventory(mplew, equippedHakuFan, chr);
            // 11
            mplew.writeShort(0);
            // 12
            mplew.writeShort(0);
            // 13
            mplew.writeShort(0);
            // 14
            mplew.writeShort(0); // 267 ++ unk type equip
            // 開始加載圖騰    15
            encodeInventory(mplew, equippedTotem, chr);
            // 未知    16
            encodeInventory(mplew, equippedUnknown, chr);
            // 技能皮膚 從20000開始    已裝備?    17
            encodeInventory(mplew, items20000, chr);
            // 技能皮膚 從21000開始    未裝備    18
            encodeInventory(mplew, items21000, chr);
        }
        if ((flag & 0x10L) != 0x0L) {
            encodeInventory(mplew, Collections.emptyList(), chr); // 18
            encodeInventory(mplew, Collections.emptyList(), chr); // 19
        }
        if ((flag & 0x2000L) != 0x0L) {
            mplew.writeBool(false);
            // 開始加載身上的時裝    1
            encodeInventory(mplew, equippedCash, chr);
            iv = chr.getInventory(MapleInventoryType.DECORATION);
            List<Item> decoration = new ArrayList<>();
            for (Item item : iv.list()) {
                if (item.getPosition() < 129) {
                    decoration.add(item);
                }
            }
            // 開始加載時裝欄的道具     2
            encodeInventory(mplew, decoration, chr);
            // 開始加載機器人的時裝     3
            encodeInventory(mplew, equippedAndroid, chr);
            // 開始加載天使破壞者時裝   4
            encodeInventory(mplew, equippedLolitaCash, chr);
            // 開始加載神之子培塔時裝   5
            encodeInventory(mplew, equippedZeroBetaCash, chr);
            // 開始載入現金裝備分頁     6
            encodeInventory(mplew, equippedCashPreset, chr);

            mplew.writeShort(0); //7
            mplew.writeShort(0); //8
        }
        if ((flag & 0x8L) != 0x0L) {
            // 開始加載消耗欄道具    1
            iv = chr.getInventory(MapleInventoryType.USE);
            List<Item> items = new ArrayList<>();
            for (Item item : iv.list()) {
                if (item.getPosition() < 129) {
                    items.add(item);
                }
            }
            encodeInventory(mplew, items, chr);
        }
        if ((flag & 0x10L) != 0x0L) {
            // 開始加載裝飾欄道具    2
            iv = chr.getInventory(MapleInventoryType.SETUP);
            List<Item> items = new ArrayList<>();
            for (Item item : iv.list()) {
                if (item.getPosition() < 129) {
                    items.add(item);
                }
            }
            encodeInventory(mplew, items, chr);
        }
        if ((flag & 0x20L) != 0x0L) {
            // 開始加載其他欄道具    3
            iv = chr.getInventory(MapleInventoryType.ETC);
            List<Item> items = new ArrayList<>();
            for (Item item : iv.list()) {
                if (item.getPosition() < 129) {
                    items.add(item);
                }
            }
            encodeInventory(mplew, items, chr);
        }
        List<MaplePet> pets = new ArrayList<>();
        if ((flag & 0x40L) != 0x0L) {
            // 開始加載現金欄道具    4
            iv = chr.getInventory(MapleInventoryType.CASH);
            List<Item> items = new ArrayList<>();
            for (Item item : iv.list()) {
                items.add(item);
                if (item.getPet() != null) {
                    pets.add(item.getPet());
                }
            }
            encodeInventory(mplew, items, chr);
        }
        MapleInventoryType eiv;
        if ((flag & 0x8L) != 0x0L) {
            //開始加載消耗欄擴展背包道具    1
            eiv = MapleInventoryType.USE;
            List<Item> exSlots = chr.getExtendedSlots(eiv.getType());
            mplew.writeInt(exSlots.size());
            for (Item it : exSlots) {
                mplew.writeInt(it.getExtendSlot());
                mplew.writeInt(it.getItemId());
                chr.getInventory(eiv).list().stream()
                        .filter(item -> item.getPosition() > (it.getExtendSlot() * 100 + 10100) && item.getPosition() < (it.getExtendSlot() * 100 + 10200))
                        .forEach(item -> {
                            addItemPosition(mplew, item, false, true);
                            GW_ItemSlotBase_Encode(mplew, item);
                        });
                mplew.writeInt(-1);
            }
        }
        if ((flag & 0x10L) != 0x0L) {
            //開始加載裝飾欄擴展背包道具    2
            eiv = MapleInventoryType.SETUP;
            List<Item> exSlots = chr.getExtendedSlots(eiv.getType());
            mplew.writeInt(exSlots.size());
            for (Item it : exSlots) {
                mplew.writeInt(it.getExtendSlot());
                mplew.writeInt(it.getItemId());
                chr.getInventory(eiv).list().stream()
                        .filter(item -> item.getPosition() > (it.getExtendSlot() * 100 + 10100) && item.getPosition() < (it.getExtendSlot() * 100 + 10200))
                        .forEach(item -> {
                            addItemPosition(mplew, item, false, true);
                            GW_ItemSlotBase_Encode(mplew, item);
                        });
                mplew.writeInt(-1);
            }
        }
        if ((flag & 0x20L) != 0x0L) {
            //開始加載其他欄擴展背包道具    3
            eiv = MapleInventoryType.ETC;
            List<Item> exSlots = chr.getExtendedSlots(eiv.getType());
            mplew.writeInt(exSlots.size());
            for (Item it : exSlots) {
                mplew.writeInt(it.getExtendSlot());
                mplew.writeInt(it.getItemId());
                chr.getInventory(eiv).list().stream()
                        .filter(item -> item.getPosition() > (it.getExtendSlot() * 100 + 10100) && item.getPosition() < (it.getExtendSlot() * 100 + 10200))
                        .forEach(item -> {
                            addItemPosition(mplew, item, false, true);
                            GW_ItemSlotBase_Encode(mplew, item);
                        });
                mplew.writeInt(-1);
            }
        }
        if ((flag & 0x1000000L) != 0x0L) {// nSenseEXP
            mplew.writeInt(0);
        }
        if ((flag & 0x40000000L) != 0x0L) {// DayLimit.nWill
            mplew.writeInt(0);
        }
        if ((flag & 0x800000L) != 0x0L) { // 吃蟲寶石君
            mplew.write(0);
        }
        // SkillInfo
        if ((flag & 0x100L) != 0x0L) {
            Map<Integer, SkillEntry> skills = chr.getSkills(true);
            mplew.write(1);  //V.100新加
            mplew.writeShort(skills.size());
            for (Entry<Integer, SkillEntry> skillinfo : skills.entrySet()) {
                Skill skill = SkillFactory.getSkill(skillinfo.getKey());
                mplew.writeInt(skill.getId());
                if (skill.isLinkSkills()) { //別人傳授給角色的技能 寫別人角色的ID
                    mplew.writeInt(skillinfo.getValue().teachId);
                } else if (skill.isTeachSkills()) { //如果是自己的傳授技能 傳授對像不為空寫傳授對象的角色ID 如果為空寫 自己的角色ID
                    mplew.writeInt(skillinfo.getValue().teachId > 0 ? skillinfo.getValue().teachId : chr.getId()); //skillinfo.getValue().skillevel
                } else {
                    mplew.writeInt(skillinfo.getValue().skillevel);
                }
                addExpirationTime(mplew, skillinfo.getValue().expiration);
                if (skill.isFourthJob()) {
                    mplew.writeInt(skillinfo.getValue().masterlevel);
                }
                if (skill.getId() == 陰陽師.紫扇傳授 || skill.getId() == 陰陽師.紫扇傳授_傳授) {
                    mplew.writeInt(skillinfo.getValue().masterlevel);
                }
            }
            //傳授技能的等級
            Map<Integer, SkillEntry> teachList = chr.getLinkSkills();
            mplew.writeShort(teachList.size());
            for (Entry<Integer, SkillEntry> skill : teachList.entrySet()) {
                mplew.writeInt(skill.getKey());
                mplew.writeShort(skill.getValue().skillevel - 1);
            }
            // getSonOfLinkedSkills
            Map<Integer, Pair<Integer, SkillEntry>> sonOfLinkedSkills = chr.getSonOfLinkedSkills();
            mplew.writeInt(sonOfLinkedSkills.size());
            for (Entry<Integer, Pair<Integer, SkillEntry>> entry : sonOfLinkedSkills.entrySet()) {
                writeSonOfLinkedSkill(mplew, entry.getKey(), entry.getValue());
            }

            mplew.write((int) chr.getInfoQuestValueWithKey(2498, "hyperstats"));
            for (int i = 0; i <= 2; i++) {
                mplew.writeInt(chr.loadHyperStats(i).size());
                for (MapleHyperStats mhsz : chr.loadHyperStats(i)) {
                    mplew.writeInt(mhsz.getPosition());
                    mplew.writeInt(mhsz.getSkillid());
                    mplew.writeInt(mhsz.getSkillLevel());
                }
            }
        }
        // CoolDownInfo
        if ((flag & 0x8000L) != 0x0L) {
            List<MapleCoolDownValueHolder> cooldowns = chr.getCooldowns();
            mplew.writeShort(cooldowns.size());
            for (MapleCoolDownValueHolder cooling : cooldowns) {
                mplew.writeInt(cooling.skillId);
                int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
                mplew.writeInt(timeLeft / 1000); //V.103修改為int
                //System.out.println("技能冷卻 - 技能ID: " + cooling.skillId + " 剩餘時間: " + (timeLeft / 1000) + " 秒");
            }
        }
        if ((flag & 0x1L) != 0x0L) {
            for (int n = 0; n < 6; n++) {
                mplew.writeInt(0);
            }
            for (int n = 0; n < 6; n++) {
                mplew.write(0);
            }
        }
        // QuestInfo
        if ((flag & 0x200L) != 0x0L) {
            List<MapleQuestStatus> started = chr.getStartedQuests();
            boolean bUnk = true;
            mplew.write(bUnk);
            mplew.writeShort(started.size());
            for (MapleQuestStatus q : started) { // 檢測是否接過任務
                mplew.writeInt(q.getQuest().getId()); // 任務ID
//            mplew.writeShort(0); // 若任務ID不存在為0，否則為-1
                if (q.hasMobKills()) {
                    StringBuilder sb = new StringBuilder();
                    for (int kills : q.getMobKills().values()) {
                        sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
                    }
                    mplew.writeMapleAsciiString(sb.toString());
                } else {
                    mplew.writeMapleAsciiString(q.getCustomData() == null ? "" : q.getCustomData());
                }
            }
            if (!bUnk) {
                mplew.writeShort(0); // for UInt
            }
            mplew.writeShort(0); // String String
        }
        if ((flag & 0x4000L) != 0x0L) {
            boolean bUnk = true;
            mplew.write(bUnk);
            List<MapleQuestStatus> completed = chr.getCompletedQuests();
            if (ServerConfig.HideBulbQuest) {
                List<MapleQuest> questList = new LinkedList<>();
                for (MapleQuestStatus q : completed) {
                    questList.add(q.getQuest());
                }
                List<Integer> ignoreQuests = Arrays.asList(5741, 5742, 5743, 5744, 5745);
                for (MapleQuest q : MapleQuest.GetBulbQuest()) {
                    if (!questList.contains(q) && !ignoreQuests.contains(q.getId())) {
                        completed.add(new MapleQuestStatus(q, 2));
                        questList.add(q);
                    }
                }
                int[] extraQuests = {32510};
                for (int nQ : extraQuests) {
                    MapleQuest q = MapleQuest.getInstance(nQ);
                    if (!questList.contains(q)) {
                        completed.add(new MapleQuestStatus(q, 2));
                        questList.add(q);
                    }
                }
            }
            mplew.writeShort(completed.size());
            for (MapleQuestStatus q : completed) {
                mplew.writeInt(q.getQuest().getId());
                mplew.writeLong(getTime(q.getCompletionTime()));//int to long at V.149
            }
            if (!bUnk) {
                mplew.writeShort(0); // for UInt
            }
        }
        if ((flag & 0x400L) != 0x0L) {
            mplew.writeShort(0);
        }
        /*
         * RingInfo
         */
        if ((flag & 0x800L) != 0x0L) {
            Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> aRing = chr.getRings(true);
            //戀人戒指
            List<MapleRing> cRing = aRing.getLeft();
            mplew.writeShort(cRing.size());
            for (MapleRing ring : cRing) { // 35
                mplew.writeInt(ring.getPartnerChrId());
                mplew.writeAsciiString(ring.getPartnerName(), 15);
                mplew.writeLong(ring.getRingId());
                mplew.writeLong(ring.getPartnerRingId());
            }
            //好友戒指
            List<MapleRing> fRing = aRing.getMid();
            mplew.writeShort(fRing.size());
            for (MapleRing ring : fRing) { // 39
                mplew.writeInt(ring.getPartnerChrId());
                mplew.writeAsciiString(ring.getPartnerName(), 15);
                mplew.writeLong(ring.getRingId());
                mplew.writeLong(ring.getPartnerRingId());
                mplew.writeInt(ring.getItemId());
            }
            //結婚戒指
            List<MapleRing> mRing = aRing.getRight();
            mplew.writeShort(mRing.size());
            for (MapleRing ring : mRing) {// 52
                mplew.writeInt(chr.getMarriageId());
                mplew.writeInt(chr.getId());
                mplew.writeInt(ring.getPartnerChrId());
                mplew.writeShort(3); //1 = engaged 3 = married
                mplew.writeInt(ring.getItemId());
                mplew.writeInt(ring.getItemId());
                mplew.writeAsciiString(chr.getName(), 15);
                mplew.writeAsciiString(ring.getPartnerName(), 15);
            }
        }
        /*
         * RocksInfo
         */
        if ((flag & 0x1000L) != 0x0L) {
            int[] mapz = chr.getRegRocks();
            for (int i = 0; i < 5; i++) { // VIP teleport map
                mplew.writeInt(mapz[i]);
            }
            int[] map = chr.getRocks();
            for (int i = 0; i < 10; i++) { // VIP teleport map
                mplew.writeInt(map[i]);
            }
            int[] maps = chr.getHyperRocks();
            for (int i = 0; i < 13; i++) { // VIP teleport map
                mplew.writeInt(maps[i]);
            }
        }
        /*
         * QuestDataInfo
         * 將任務數據根據共享級別分開存放
         */

        if ((flag & 0x40000L) != 0x0L) {
            Map<Integer, String> questInfos = new LinkedHashMap<>();
            for (Entry<Integer, String> quest : chr.getInfoQuest_Map().entrySet()) {
                questInfos.put(quest.getKey(), quest.getValue());
            }
            for (Entry<Integer, String> wsi : chr.getWorldShareInfo().entrySet()) {
                if (!GameConstants.isWorldShareQuest(wsi.getKey())) {
                    questInfos.put(wsi.getKey(), wsi.getValue());
                }
            }
            mplew.writeShort(questInfos.size());
            for (Entry<Integer, String> quest : questInfos.entrySet()) {
                mplew.writeInt(quest.getKey());
                mplew.writeMapleAsciiString(quest.getValue() == null ? "" : quest.getValue());
            }
        }
        /*if ((flag & 0x20L) != 0x0L) {
            short nCount = 0;
            mplew.writeShort(nCount);
            for (int i = 0; i < nCount; i++) {
                mplew.writeInt(0);
                //AvatarLook::Decode
            }
        }*/
        if ((flag & 0x80000L) != 0x0L) {
            short nCount = 0;
            mplew.writeShort(nCount);
            for (int i = 0; i < nCount; i++) {
                mplew.writeInt(0);
                mplew.writeShort(0);
            }
        }

        mplew.writeBool(true);

        if ((flag & 0x8000000000L) != 0x0L) {
            int nCount = 0;
            mplew.writeInt(nCount);
            for (int j = 0; j < nCount; ++j) {
                mplew.writeInt(26);
                mplew.writeMapleAsciiString("Present=7");
            }
        }
        if ((flag & 0x100000000000L) != 0x0L) {
            int nCount = 1;
            mplew.writeInt(nCount);
            for (int k = 0; k < nCount; ++k) {
                mplew.writeInt(4475);
                mplew.writeInt(-1);
            }
        }
        if ((flag & 0x200000L) != 0x0L) {
            addJaguarInfo(mplew, chr); // 狂豹獵人的豹子信息 不是該職業就不發送
        }
        if ((flag & 0x800L) != 0x0L) {
            if (JobConstants.is神之子(chr.getJob())) {
                chr.getStat().zeroData(mplew, chr, 0xffff, chr.isBeta());
            }
        }
        if ((flag & 0x4000000L) != 0x0L) {
            mplew.writeShort(chr.getBuyLimit().size() + chr.getAccountBuyLimit().size());
            for (Entry<Integer, NpcShopBuyLimit> entry : chr.getBuyLimit().entrySet()) {
                final int shopId = entry.getKey();
                final NpcShopBuyLimit buyLimit = entry.getValue();
                final MapleShop shop = MapleShopFactory.getInstance().getShop(shopId);
                mplew.writeInt(shopId);
                mplew.writeShort((shop != null) ? buyLimit.getData().size() : 0);
                if (shop != null) {
                    for (Entry<Integer, BuyLimitData> o2 : buyLimit.getData().entrySet()) {
                        final int itemId = o2.getKey();
                        final BuyLimitData data = o2.getValue();
                        final int count = data.getCount();
                        final long date = data.getDate();
                        mplew.writeInt(shopId);
                        mplew.writeShort(shop.getBuyLimitItemIndex(o2.getKey()));
                        mplew.writeInt(itemId);
                        mplew.writeShort(count);
                        addExpirationTime(mplew, date);
                        mplew.writeMapleAsciiString("");
                        mplew.writeInt(0);
                    }
                }
            }
        }
        if ((flag & 0x4000000L) != 0x0L) {
            mplew.writeShort(chr.getBuyLimit().size() + chr.getAccountBuyLimit().size());
            for (Entry<Integer, NpcShopBuyLimit> entry : chr.getBuyLimit().entrySet()) {
                final int shopId = entry.getKey();
                final NpcShopBuyLimit buyLimit = entry.getValue();
                final MapleShop shop = MapleShopFactory.getInstance().getShop(shopId);
                mplew.writeInt(shopId);
                mplew.writeShort((shop != null) ? buyLimit.getData().size() : 0);
                if (shop != null) {
                    for (Entry<Integer, BuyLimitData> o2 : buyLimit.getData().entrySet()) {
                        final int itemId = o2.getKey();
                        final BuyLimitData data = o2.getValue();
                        final int count = data.getCount();
                        final long date = data.getDate();
                        mplew.writeInt(shopId);
                        mplew.writeShort(shop.getBuyLimitItemIndex(o2.getKey()));
                        mplew.writeInt(itemId);
                        mplew.writeShort(count);
                        addExpirationTime(mplew, date);
                        mplew.writeMapleAsciiString("");
                        mplew.writeInt(0);
                    }
                }
            }
        }

//        if ((flag & 0x2000000000000000L) != 0x0L) {
//            int nCount = 0;
//            mplew.writeShort(nCount);
//            for (int i = 0; i < nCount; i++) {
//                mplew.writeInt(0);
//                mplew.writeShort(0);
//                // sub_1403B6BE0
//            }
//        }

        //V.160 new:
        /*if ((flag & 0x20000000L) != 0x0L) {
            int nCount = 0;
            mplew.writeShort(nCount);
            for (int i = 0; i < nCount; i++) {
                int nnCount = 0;
                mplew.writeShort(nnCount);
                int a1 = 0;
                mplew.writeInt(a1); // 9063002
                if (nnCount > 0 && a1 > 0) {
                    for (int j = 0; j < nnCount; j++) {
                        mplew.writeInt(0); // 9063002
                        mplew.writeShort(0); // 36
                        mplew.writeInt(0); // 2439267
                        mplew.writeShort(0); // 1
                        mplew.writeLong(0); // 2019/3/27 下午 5:22
                    }
                }
            }
        }*/

        // 68
        if ((flag & 0x4000000L) != 0x0L) {
            mplew.writeShort(0);
//            for (Entry<Integer, NpcShopBuyLimit> entry : chr.getBuyLimit().entrySet()) {
//                final int shopId = entry.getKey();
//                final NpcShopBuyLimit buyLimit = entry.getValue();
//                final MapleShop shop = MapleShopFactory.getInstance().getShop(shopId);
//                mplew.writeInt(shopId);
//                mplew.writeShort((shop != null) ? buyLimit.getData().size() : 0);
//                if (shop != null) {
//                    for (Entry<Integer, BuyLimitData> o2 : buyLimit.getData().entrySet()) {
//                        final int itemId = o2.getKey();
//                        final BuyLimitData data = o2.getValue();
//                        final int count = data.getCount();
//                        final long date = data.getDate();
//                        mplew.writeInt(shopId);
//                        mplew.writeShort(shop.getBuyLimitItemIndex(o2.getKey()));
//                        mplew.writeInt(itemId);
//                        mplew.writeShort(count);
//                        addExpirationTime(mplew, date);
//                        mplew.writeMapleAsciiString("");
//                        mplew.writeInt(0);
//                    }
//                }
//            }
        }
        // 69
        mplew.writeInt(0);
        // for { int, int, int, int }

        //end
        if ((flag & 0x20000000L) != 0x0L) {
            //獲取複製技能數裝備的技能列表
            for (int i = 0; i < 16; i++) {
                mplew.writeInt(chr.getStealMemorySkill(i));
            }
        }
        if ((flag & 0x10000000L) != 0x0L) {
            //裝備中的技能
            int[] p_skills = {幻影俠盜.盜亦有道Ⅰ, 幻影俠盜.盜亦有道Ⅱ, 幻影俠盜.盜亦有道Ⅲ, 幻影俠盜.盜亦有道Ⅳ, 幻影俠盜.盜亦有道H};
            for (int i : p_skills) {
                mplew.writeInt(chr.getEquippedStealSkill(i));
            }
        }
        if ((flag & 0x80000000L) != 0x0L) {
            for (int j = 0; j < 3; j++) { // v262新增潛能分頁
                mplew.writeShort(chr.getInnerSkillSize()); //內在能力技能數量
                for (int i = 0; i < chr.getInnerSkillSize(); i++) {
                    InnerSkillEntry innerSkill = chr.getInnerSkills()[i];
                    if (innerSkill != null) {
                        mplew.write(innerSkill.getPosition()); // key
                        mplew.writeInt(innerSkill.getSkillId()); // id 7000000 id ++
                        mplew.write(innerSkill.getSkillLevel());  // level
                        mplew.write(innerSkill.getRank()); // rank, C, B, A, and S
                    } else {
                        mplew.writeZeroBytes(7);
                    }
                }
            }
        }
        if ((flag & 0x40000000000000L) != 0x0L) {
            mplew.writeShort(chr.getSoulCollection().size());
            for (Entry<Integer, Integer> entry : chr.getSoulCollection().entrySet()) {
                mplew.writeInt(entry.getKey());
                mplew.writeInt(entry.getValue());
            }
        }
        if ((flag & 0x100000000L) != 0x0L) {
            mplew.writeInt(1); //榮譽等級//118已經不存在了
            mplew.writeInt(chr.getHonor()); //聲望點數
        }
        /*
        if ((flag & 0x4000L) != 0x0L) {//  OX Quiz
            int nCount = 0;
            mplew.writeShort(nCount);
            for (int i = 0; i < nCount; i++) {
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeMapleAsciiString("");
                mplew.write(0);
                mplew.writeLong(0);
                mplew.writeInt(0);
                mplew.writeMapleAsciiString("");
                mplew.write(0);
                mplew.write(0);
                mplew.writeLong(0);
                mplew.writeMapleAsciiString("");
            }
        }
        */
        if ((flag & 0x800000000000L) != 0x0L) {// 經驗椅子
            int nCount = 0;
            mplew.writeShort(nCount);
            for (int i = 0; i < nCount; i++) {
                mplew.writeZeroBytes(20);
            }
        }
        if ((flag & 0x1000000000000L) != 0x0L) {
            addRedLeafInfo(mplew, chr); // v258(sub_1403FB2C0)
        }
        if ((flag & 0x2000000000000L) != 0x0L) {
            mplew.writeShort(0);
        }
        if ((flag & 0x200000000L) != 0x0L) {
            mplew.write(1);
            mplew.writeShort(0);
        }
        if ((flag & 0x400000000L) != 0x0L) {
            mplew.write(0);
            // GW_ItemSlotBase::Decode
        }
        if ((flag & 0x800000000L) != 0x0L) {
            writeDressUpInfo(mplew, chr);
        }
        if ((flag & 0x20000000000000L) != 0x0L) { // ActiveDamageSkin
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeLong(getTime(-2L));
        }
        /*if ((flag & 0x10L) != 0x0L) {
            writeEsInfo(mplew, chr);
        }*/
        if ((flag & 0x20000000000L) != 0x0L) {
            mplew.write(0);
        }
        //V.160 new:
        if ((flag & 0x4000000000000000L) != 0x0L) {
            mplew.writeInt(-1);
            mplew.writeInt(-1157267456);
            mplew.writeLong(0L);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeShort(0);// V.181 new
        }
        //end
        if ((flag & 0x40000000000L) != 0x0L) { // LinkPoint
            mplew.writeInt(chr.getLove()); //V.112新增 好感度
            mplew.writeLong(getTime(-2)); //00 40 E0 FD 3B 37 4F 01
            mplew.writeInt(0);
        }
        // v133 start RunnerGameRecord
        if ((flag & 0x80000000000000L) != 0x0L) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeLong(PacketHelper.getTime(-2));
            mplew.writeInt(10);
        }
        if ((flag & 0x200000000000000L) != 0x0L) {
            mplew.writeInt(0); // -> int byte byte byte
            mplew.writeInt(0);
            mplew.writeLong(0L);

            mplew.write(0);
            mplew.write(0);
        }
        /*
          賬號下角色共享任務數據
         */
        Map<Integer, String> wsInfos = new LinkedHashMap<>();
        for (Entry<Integer, String> wsi : chr.getWorldShareInfo().entrySet()) {
            if (GameConstants.isWorldShareQuest(wsi.getKey())) {
                wsInfos.put(wsi.getKey(), wsi.getValue());
            }
        }
        mplew.writeShort(wsInfos.size());
        for (Entry<Integer, String> quest : wsInfos.entrySet()) {
            mplew.writeInt(quest.getKey());
            mplew.writeMapleAsciiString(quest.getValue() == null ? "" : quest.getValue());
        }
        // v133 end
        if ((flag & 0x100000000000000L) != 0x0L) {
            mplew.writeShort(chr.getMobCollection().size());
            for (Entry<Integer, String> entry : chr.getMobCollection().entrySet()) {
                mplew.writeInt(entry.getKey());
                mplew.writeMapleAsciiString(entry.getValue());
            }
        }

        mplew.writeInt(0); // sub_58B140 // v255 - sub_1403DCFF0 -> int str

        if ((flag & 0x400000000000000L) != 0x0L) {
            mplew.writeShort(0);
        }
        if ((flag & 0x400000000000000L) != 0x0L) {
            mplew.writeShort(0); // v264 add
        }
        if ((flag & 0x800000000000000L) != 0x0L) {
            // VCoreSkill
            VCorePacket.writeVCoreSkillData(mplew, chr);
        }
        // v258 New
        // sub_1403F5620
        chr.loadHexSkills();
        encodeHexaSkills(mplew, chr);
        // sub_1403F5620 end

        // v258 Add
        // sub_1403F5FD0
        chr.loadHexStats();
        encodeSixStats(mplew, chr);
        // sub_1403F5FD0 end

        if ((flag & 0x1000000000000000L) != 0x0L) {
            int size = 0;
            mplew.writeInt(size);
            for (int i = 0; i < size; i++) {
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                for (int j = 0; j < 3; j++) {
                    mplew.writeInt(0);
                }
                mplew.writeLong(0);
            }
        }

        if ((flag & 0x1000000000000000L) != 0x0L) {
            int size = 0;
            mplew.writeInt(size);
            for (int i = 0; i < size; i++) {
                mplew.writeInt(0);
            }
        }

        if ((flag & 0x1000000000000000L) != 0x0L) { // TMS 229 done Achievement 成就系統
            mplew.writeInt(chr.getClient().getAccID());
            mplew.writeInt(chr.getId());
            mplew.writeInt(0);
            mplew.writeInt(-1);
            mplew.writeInt(Integer.MAX_VALUE);
            mplew.writeLong(getTime(-2));
            // sub_1403C7AE0
            int a2 = 0;
            mplew.writeInt(a2);
            for (int i = 0; i < a2; i++) {
                mplew.writeInt(0);
                mplew.write(0);
                mplew.write(0);
                mplew.writeLong(DateUtil.getFileTimestamp(System.currentTimeMillis())); // or getTime(-2)
                int unk = 41;
                mplew.writeInt(unk);
                if (unk == 42) {
                    mplew.writeMapleAsciiString("");
                    // Plugin.script=0;user_lvup=104
                    // Plugin.script=0;user_lvup=17
                    // Plugin.script=0;field_enter=1
                    // union_attacker_power_change=0;Plugin.script=0
                } else {
                    mplew.writeLong(0); // int int
                }
            }
            int v6 = 0;
            mplew.writeInt(v6);
            for (int i = 0; i < v6; i++) {
                mplew.writeInt(0);
                mplew.write(0);
                mplew.writeLong(0);
            }
        }
        
        if ((flag & 0x20L) != 0x0L) { // TMS 229 done ItemSlotEtc
            int v6 = 0;
            mplew.writeInt(v6);// 未知，V.144新增
            for (int i = 0; i < v6; i++) {
                mplew.writeLong(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeLong(0);
                mplew.writeLong(0);
                mplew.writeLong(0);
            }
        }

        for (int num = 3; num > 0; num--) { // TMS 229 done
            if ((flag & (num <= 2 ? 0x8000000000000000L : 0x10000000L)) != 0x0L) {
                encodeCombingRoomInventory(mplew, chr.getSalon().getOrDefault(num, new LinkedList<>()));
            }
        }

        if ((flag & 0x8000000L) != 0x0L) { // TMS 229 done Emoticons
            int nCount = 0;
            mplew.writeInt(nCount); // Emoticons
            for (int i = 0; i < nCount; i++) {
                mplew.writeInt(0);
                mplew.writeZeroBytes(14);
            }

            nCount = 0;
            mplew.writeInt(nCount); //EmoticonTabs
            for (int i = 0; i < nCount; i++) {
                mplew.writeShort(0);
                mplew.writeInt(0);
            }
            mplew.writeShort(8);

            nCount = 0;
            mplew.writeInt(nCount); // SavedEmoticon
            for (int i = 0; i < nCount; i++) {
                mplew.writeShort(0);
                mplew.writeZeroBytes(25);
            }

            nCount = 0;
            mplew.writeInt(nCount); //SavedEmoticon
            for (int i = 0; i < nCount; i++) {
                mplew.writeMapleAsciiString("");
                mplew.writeZeroBytes(25);
            }
        }
        if ((flag & 0x2000000000000000L) != 0x0L) { // TMS 238 done
            mplew.writeInt(pets.size());
            for (MaplePet pet : pets) {
                mplew.writeLong(pet.getUniqueId());
                int a2 = 0;
                mplew.writeInt(a2);
                for (int j = 0; j < a2; j++) {
                    mplew.writeInt(0);
                }
            }
        }
        if ((flag & 0x4000000000000L) != 0x0L) { // TMS 229 done
            mplew.write(1);
            String string = chr.getOneInfo(17008, "T");
            String string2 = chr.getOneInfo(17008, "L");
            String string3 = chr.getOneInfo(17008, "E");
            mplew.write(string == null ? 0 : Integer.valueOf(string));
            mplew.writeInt(string2 == null ? 1 : Integer.valueOf(string2));
            mplew.writeInt(string3 == null ? 0 : Integer.valueOf(string3));
            mplew.writeInt(100 - chr.getPQLog("航海能量"));
            mplew.writeLong(getTime(System.currentTimeMillis()));

            String questinfo = chr.getInfoQuest(17018);
            String[] questinfos = questinfo.split(";");
            mplew.writeShort(!questinfo.isEmpty() ? questinfos.length : 0);
            for (String questinfo1 : questinfos) {
                if (!questinfo1.isEmpty()) {
                    String[] split = questinfo1.split("=");
                    mplew.write(Integer.valueOf(split[0]));
                    mplew.writeInt(Integer.valueOf(split[1]));
                    mplew.writeInt(0);
                }
            }
            mplew.writeShort(ItemConstants.航海材料.length);
            for (int i : ItemConstants.航海材料) {
                mplew.writeInt(i);
                mplew.writeInt(chr.getPQLog(String.valueOf(i)));
                mplew.writeLong(getTime(System.currentTimeMillis()));
            }
        }
//        if ((flag & 0x8000000000000L) != 0x0L) { // TMS 229 done
//            mplew.write(0);
//        }
        if ((flag & 0x8000000000000L) != 0x0L) { // TMS 229 done
            // 內面耀光技能
            List<Integer> buffs = new LinkedList<>();
            if (chr.getKeyValue("InnerGlareBuffs") != null) {
                for (String s : chr.getKeyValue("InnerGlareBuffs").split(",")) {
                    if (s.isEmpty()) {
                        continue;
                    }
                    buffs.add(Integer.parseInt(s));
                }
            }
            mplew.writeReversedVarints(buffs.size());
            for (int buffId : buffs) {
                mplew.writeInt(buffId);
            }
        }
    }

    public static void showCharacterInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, long flag) {
        if ((flag & 0x80L) != 0x0L) {
            mplew.writeInt(chr.getInventory(MapleInventoryType.EQUIPPED).getSlotLimit()); // equip slots
            MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
            List<Item> Inbudy = iv.newList(); //獲取裝備中的道具列表
            Collections.sort(Inbudy); //對道具進行排序
            for (short slotEquip = 0; slotEquip <= 128; slotEquip++) {
                if (iv.getItem(slotEquip) != null) {
                    mplew.writeShort(iv.getItem(slotEquip).getPosition());
                    mplew.write(iv.getItem(slotEquip).getType());
                    mplew.writeInt(iv.getItem(slotEquip).getItemId());
                    mplew.write(0);
                    mplew.writeLong(150842304000000000L);
                    mplew.writeInt(iv.getItem(slotEquip).getExtendSlot()); // = -1 穿戴
                    mplew.writeBool(false);
                    mplew.writeShort(0);
////        mplew.writeInt(65537);
////        mplew.write(1);
////        mplew.writeShort(0);
////        mplew.writeInt(257);
////        mplew.writeInt(0);
////        mplew.writeInt(0);
////        mplew.writeInt(0);
////        mplew.writeInt(0);
////        mplew.write(0);
////        mplew.writeInt(257);
////        mplew.writeInt(65537);
////        mplew.writeInt(0);
////        mplew.writeInt(1);
////        mplew.writeInt(1);
////        mplew.writeShort(0);
////        mplew.writeInt(1);
////        mplew.writeInt(0);
////        mplew.write(0);
////        mplew.writeInt(257);
////        mplew.writeZeroBytes(37);
////        mplew.write(0);
////
////        for (int i = 0; i < 3; ++i) {
////            mplew.writeInt(JobConstants.is皇家騎士團(chr.getJob()) || JobConstants.is米哈逸(chr.getJob()) ? -6 : -1);
////        }
////        mplew.write(0);
////        mplew.writeInt(0);
////        mplew.write(0);
////
////        if ((flag & 0x1L) != 0x0L) {
////            chr.getCharacterStat().encode(mplew);
////            mplew.write(chr.getBuddylist().getCapacity());//好友欄上限
////            mplew.write(chr.getBlessOfFairyOrigin() != null); // 精靈的祝福
////            if (chr.getBlessOfFairyOrigin() != null) {
////                mplew.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
////            }
////            mplew.write(chr.getBlessOfEmpressOrigin() != null); // 女皇的祝福
////            if (chr.getBlessOfEmpressOrigin() != null) {
////                mplew.writeMapleAsciiString(chr.getBlessOfEmpressOrigin());
////            }
////            // 終極冒險家訊息
////            MapleQuestStatus ultExplorer = chr.getQuestNoAdd(MapleQuest.getInstance(GameConstants.ULT_EXPLORER));
////            mplew.write((ultExplorer != null) && (ultExplorer.getCustomData() != null));
////            if ((ultExplorer != null) && (ultExplorer.getCustomData() != null)) {
////                mplew.writeMapleAsciiString(ultExplorer.getCustomData());
////            }
////            mplew.writeLong(DateUtil.getFileTimestamp(-2L)); // CreateDate ?
////            mplew.writeLong(DateUtil.getFileTimestamp(-2L));
////            for (int i = 2; i < 74; i += 36) {
////                mplew.writeInt(0);
////                mplew.write(-1);
////            }
////
////            // v260 add
////            mplew.writeShort(0);
////            mplew.writeLong(DateUtil.getFileTimestamp(-2L));
////        }
////        if ((flag & 0x2L) != 0x0L) {
////            mplew.writeLong(chr.getMeso()); // 楓幣 V.110修改以前是 Int
////            mplew.writeInt(chr.getId());  // 角色ID
////            mplew.writeInt(chr.getBeans()); // 豆豆
////            mplew.writeInt(chr.getCSPoints(2)); // 楓葉點數
////        }
////        if ((flag & 0x2000000L) != 0x0L && (flag & 0x8L) != 0x0L) { //  & 0x2000008
////            int nCount = 0;
////            mplew.writeInt(nCount);
////            for (int i = 0; i < nCount; i++) {
////                mplew.writeInt(0);
////                mplew.writeInt(0);
////                mplew.writeInt(0);
////                mplew.writeLong(0);
////            }
////        }
//////        if ((flag >>> 32 & 0x4000000L) != 0x0L | (flag & 0x8L) != 0x0L) { // TMS 218 沒找到
//////            mplew.writeInt(chr.getPotionPot() != null ? 1 : 0); //藥劑罐信息
//////            if (chr.getPotionPot() != null) {
//////                addPotionPotInfo(mplew, chr.getPotionPot());
//////            }
//////        }
////        if ((flag & 0x80L) != 0x0L) {
////            mplew.writeInt(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit()); // equip slots
////            mplew.writeInt(chr.getInventory(MapleInventoryType.USE).getSlotLimit()); // use slots
////            mplew.writeInt(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit()); // set-up slots
////            mplew.writeInt(chr.getInventory(MapleInventoryType.ETC).getSlotLimit()); // etc slots
////            mplew.writeInt(chr.getInventory(MapleInventoryType.CASH).getSlotLimit()); // cash slots
////            mplew.writeInt(chr.getInventory(MapleInventoryType.DECORATION).getSlotLimit()); // decoration slots
////        }
////        /*
////         * 項鏈擴充過期時間
////         */
////        /*if ((flag & 0x100000L) != 0x0L) {
////            MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
////            if (stat != null && stat.getCustomData() != null && ("0".equals(stat.getCustomData()) || Long.parseLong(stat.getCustomData()) > System.currentTimeMillis())) {
////                mplew.writeLong("0".equals(stat.getCustomData()) ? getTime(-1) : getTime(Long.parseLong(stat.getCustomData())));
////            } else {
////                mplew.writeLong(getTime(-2));
////            }
////        }*/
////        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
////        List<Item> equippedList = iv.newList(); //獲取裝備中的道具列表
////        Collections.sort(equippedList); //對道具進行排序
////        List<Item> equipped = new ArrayList<>(); // 普通裝備
////        List<Item> equippedCash = new ArrayList<>(); // 現金裝備
////        List<Item> equippedDragon = new ArrayList<>(); // 龍裝備
////        List<Item> equippedMechanic = new ArrayList<>(); // 機甲裝備
////        List<Item> equippedAndroid = new ArrayList<>(); // 機器人的裝備
////        List<Item> equippedLolitaCash = new ArrayList<>(); // 天使破壞者裝備
////        List<Item> equippedBit = new ArrayList<>(); // 拼圖
////        List<Item> equippedZeroBetaCash = new ArrayList<>(); // 神之子培塔時裝
////        List<Item> equippedArcane = new ArrayList<>(); // 秘法符文
////        List<Item> equippedAuthenticSymbol = new ArrayList<>(); // 真實符文
////        List<Item> equippedTotem = new ArrayList<>(); // 圖騰
////        List<Item> equippedMonsterEqp = new ArrayList<>(); // 獸魔裝備
////        List<Item> equippedHakuFan = new ArrayList<>(); // 花狐裝備
////        List<Item> equippedUnknown = new ArrayList<>(); // 未知
////        List<Item> equippedCashPreset = new ArrayList<>(); // 現金裝備
////        for (Item item : equippedList) {
////            if (item.getPosition() < 0 && item.getPosition() > -100) { // 普通裝備
////                equipped.add(item);
////            } else if (item.getPosition() <= -100 && item.getPosition() > -1000) { //現金裝備
////                equippedCash.add(item);
////            } else if (item.getPosition() <= -1000 && item.getPosition() > -1100) { // 龍裝備 龍面具(1000), 龍墜飾(1001), 龍之翼(1002), 龍尾巴(1003)
////                equippedDragon.add(item);
////            } else if (item.getPosition() <= -1100 && item.getPosition() > -1200) { // 機甲裝備 戰神引擎(1100), 戰神手臂(1101), 戰神腿部(1102), 戰神身軀(1103), 戰神電晶體(1104)
////                equippedMechanic.add(item);
////            } else if (item.getPosition() <= -1200 && item.getPosition() > -1300) { // 機器人的裝備 帽子(1200), 披風(1201), 臉飾(1202), 上衣(1203), 褲裙(1204), 鞋子(1205), 手套(1206)
////                equippedAndroid.add(item);
////            } else if (item.getPosition() <= -1300 && item.getPosition() > -1310) { // 天使破壞者裝備 帽子(1300), 披風(1301), 臉飾(1302), 上衣(1303), 手套(1304)
////                equippedLolitaCash.add(item);
////            } else if (item.getPosition() <= -1400 && item.getPosition() > -1500) { // 拼圖(1400)~(1425)
////                equippedBit.add(item);
////            } else if (item.getPosition() <= -1500 && item.getPosition() > -1600) { // 神之子培塔時裝 眼飾(1500), 帽子(1501), 臉飾(1502), 耳環(1503), 披風(1504), 上衣(1505), 手套(1506), 武器(1507), 褲裙(1508), 鞋子(1509), 戒指1(1510), 戒指2(1511)
////                equippedZeroBetaCash.add(item);
////            } else if (item.getPosition() <= -1600 && item.getPosition() > -1606) { // 秘法符文 (1600)~(1605)
////                equippedArcane.add(item);
////            } else if (item.getPosition() <= -1700 && item.getPosition() > -1706) { // 真實符文 (1700)~(1705)
////                equippedAuthenticSymbol.add(item);
////            } else if (item.getPosition() <= -1800 && item.getPosition() > -17830) { // 現金裝備分頁(1801)~(1830)
////                equippedCashPreset.add(item);
////            } else if (item.getPosition() <= -5000 && item.getPosition() > -5003) { // 圖騰(5000)~(5002)
////                equippedTotem.add(item);
////            } else if (item.getPosition() <= -5100 && item.getPosition() > -5107) { // 獸魔裝備 帽子(5101), 披風(5102), 上衣(5103), 手套(5104), 鞋子(5105), 武器(5106)
////                equippedMonsterEqp.add(item);
////            } else if (item.getPosition() == -5200) { // 花狐裝備 扇子(5200)
////                equippedHakuFan.add(item);
////            } else if (item.getPosition() <= -6000 && item.getPosition() > -6200) { // 未知
////                chr.getSkillSkin().put(MapleItemInformationProvider.getInstance().getSkillSkinFormSkillId(item.getItemId()), item.getItemId());
////                equippedUnknown.add(item);
////            }
////        }
////        if ((flag & 0x4L) != 0x0L) {
////            mplew.writeBool(false);
////
////            // 開始加載身上的普通裝備    1
////            encodeInventory(mplew, equipped, chr);
////            iv = chr.getInventory(MapleInventoryType.EQUIP);
////            List<MapleAndroid> androids = new LinkedList<>();
////            List<Item> items20000 = new ArrayList<>();
////            List<Item> items21000 = new ArrayList<>();
////            List<Item> equip = new ArrayList<>();
////            for (Item item : iv.list()) {
////                if (((Equip) item).getAndroid() != null) {
////                    androids.add(((Equip) item).getAndroid());
////                }
////                if (item.getPosition() >= 21000) {
////                    items21000.add(item);
////                } else if (item.getPosition() >= 20000) {
////                    items20000.add(item);
////                } else {
////                    equip.add(item);
////                }
////            }
////            // 開始加載裝備欄道具    2
////            encodeInventory(mplew, equip, chr);
////            // 開始加載龍裝備    3
////            encodeInventory(mplew, equippedDragon, chr);
////            // 開始加載機甲裝備    4
////            encodeInventory(mplew, equippedMechanic, chr);
////            // 開始加載拼圖    5
////            encodeInventory(mplew, equippedBit, chr);
////            // 開始加載獸魔裝備    6
////            encodeInventory(mplew, equippedMonsterEqp, chr);
////            // 開始加載秘法符文    7
////            encodeInventory(mplew, equippedArcane, chr);
////            // 開始加載真實符文    8
////            encodeInventory(mplew, equippedAuthenticSymbol, chr);
////            // 開始加載花狐裝備    9
////            encodeInventory(mplew, equippedHakuFan, chr);
////            // 開始加載圖騰    10
////            encodeInventory(mplew, equippedTotem, chr);
////            // 未知    11
////            encodeInventory(mplew, equippedUnknown, chr);
////            // 技能皮膚 從20000開始    已裝備?    12
////            encodeInventory(mplew, items20000, chr);
////            // 技能皮膚 從21000開始    未裝備    13
////            encodeInventory(mplew, items21000, chr);
////            /*mplew.writeInt(androids.size());
////            for (MapleAndroid android : androids) {
////                mplew.writeLong(android.getUniqueId());
////                android.encodeAndroidLook(mplew);
////            }*/
////        }
////        if ((flag & 0x10L) != 0x0L) {
////            encodeInventory(mplew, Collections.emptyList(), chr); // 14
////            encodeInventory(mplew, Collections.emptyList(), chr); // 15
////        }
////        if ((flag & 0x2000L) != 0x0L) {
////            mplew.writeBool(false);
////            // 開始加載身上的時裝    1
////            encodeInventory(mplew, equippedCash, chr);
////            iv = chr.getInventory(MapleInventoryType.DECORATION);
////            List<Item> decoration = new ArrayList<>();
////            for (Item item : iv.list()) {
////                if (item.getPosition() < 129) {
////                    decoration.add(item);
////                }
////            }
////            // 開始加載時裝欄的道具     2
////            encodeInventory(mplew, decoration, chr);
////            // 開始加載機器人的時裝     3
////            encodeInventory(mplew, equippedAndroid, chr);
////            // 開始加載天使破壞者時裝   4
////            encodeInventory(mplew, equippedLolitaCash, chr);
////            // 開始加載神之子培塔時裝   5
////            encodeInventory(mplew, equippedZeroBetaCash, chr);
////            // 開始載入現金裝備分頁     6
////            encodeInventory(mplew, equippedCashPreset, chr);
////
////            mplew.writeShort(0); //7
////            mplew.writeShort(0); //8
////        }
////        if ((flag & 0x8L) != 0x0L) {
////            // 開始加載消耗欄道具    1
////            iv = chr.getInventory(MapleInventoryType.USE);
////            List<Item> items = new ArrayList<>();
////            for (Item item : iv.list()) {
////                if (item.getPosition() < 129) {
////                    items.add(item);
////                }
////            }
////            encodeInventory(mplew, items, chr);
////        }
////        if ((flag & 0x10L) != 0x0L) {
////            // 開始加載裝飾欄道具    2
////            iv = chr.getInventory(MapleInventoryType.SETUP);
////            List<Item> items = new ArrayList<>();
////            for (Item item : iv.list()) {
////                if (item.getPosition() < 129) {
////                    items.add(item);
////                }
////            }
////            encodeInventory(mplew, items, chr);
////        }
////        if ((flag & 0x20L) != 0x0L) {
////            // 開始加載其他欄道具    3
////            iv = chr.getInventory(MapleInventoryType.ETC);
////            List<Item> items = new ArrayList<>();
////            for (Item item : iv.list()) {
////                if (item.getPosition() < 129) {
////                    items.add(item);
////                }
////            }
////            encodeInventory(mplew, items, chr);
////        }
////        List<MaplePet> pets = new ArrayList<>();
////        if ((flag & 0x40L) != 0x0L) {
////            // 開始加載現金欄道具    4
////            iv = chr.getInventory(MapleInventoryType.CASH);
////            List<Item> items = new ArrayList<>();
////            for (Item item : iv.list()) {
////                items.add(item);
////                if (item.getPet() != null) {
////                    pets.add(item.getPet());
////                }
////            }
////            encodeInventory(mplew, items, chr);
////        }
////        MapleInventoryType eiv;
////        if ((flag & 0x8L) != 0x0L) {
////            //開始加載消耗欄擴展背包道具    1
////            eiv = MapleInventoryType.USE;
////            List<Item> exSlots = chr.getExtendedSlots(eiv.getType());
////            mplew.writeInt(exSlots.size());
////            for (Item it : exSlots) {
////                mplew.writeInt(it.getExtendSlot());
////                mplew.writeInt(it.getItemId());
////                chr.getInventory(eiv).list().stream()
////                        .filter(item -> item.getPosition() > (it.getExtendSlot() * 100 + 10100) && item.getPosition() < (it.getExtendSlot() * 100 + 10200))
////                        .forEach(item -> {
////                            addItemPosition(mplew, item, false, true);
////                            GW_ItemSlotBase_Encode(mplew, item);
////                        });
////                mplew.writeInt(-1);
////            }
////        }
////        if ((flag & 0x10L) != 0x0L) {
////            //開始加載裝飾欄擴展背包道具    2
////            eiv = MapleInventoryType.SETUP;
////            List<Item> exSlots = chr.getExtendedSlots(eiv.getType());
////            mplew.writeInt(exSlots.size());
////            for (Item it : exSlots) {
////                mplew.writeInt(it.getExtendSlot());
////                mplew.writeInt(it.getItemId());
////                chr.getInventory(eiv).list().stream()
////                        .filter(item -> item.getPosition() > (it.getExtendSlot() * 100 + 10100) && item.getPosition() < (it.getExtendSlot() * 100 + 10200))
////                        .forEach(item -> {
////                            addItemPosition(mplew, item, false, true);
////                            GW_ItemSlotBase_Encode(mplew, item);
////                        });
////                mplew.writeInt(-1);
////            }
////        }
////        if ((flag & 0x20L) != 0x0L) {
////            //開始加載其他欄擴展背包道具    3
////            eiv = MapleInventoryType.ETC;
////            List<Item> exSlots = chr.getExtendedSlots(eiv.getType());
////            mplew.writeInt(exSlots.size());
////            for (Item it : exSlots) {
////                mplew.writeInt(it.getExtendSlot());
////                mplew.writeInt(it.getItemId());
////                chr.getInventory(eiv).list().stream()
////                        .filter(item -> item.getPosition() > (it.getExtendSlot() * 100 + 10100) && item.getPosition() < (it.getExtendSlot() * 100 + 10200))
////                        .forEach(item -> {
////                            addItemPosition(mplew, item, false, true);
////                            GW_ItemSlotBase_Encode(mplew, item);
////                        });
////                mplew.writeInt(-1);
////            }
////        }
////        if ((flag & 0x1000000L) != 0x0L) {// nSenseEXP
////            mplew.writeInt(0);
////        }
////        if ((flag & 0x40000000L) != 0x0L) {// DayLimit.nWill
////            mplew.writeInt(0);
////        }
////        if ((flag & 0x800000L) != 0x0L) { // 吃蟲寶石君
////            mplew.write(0);
////        }
////        // SkillInfo
////        if ((flag & 0x100L) != 0x0L) {
////            Map<Integer, SkillEntry> skills = chr.getSkills(true);
////            mplew.write(1);  //V.100新加
////            mplew.writeShort(skills.size());
////            for (Entry<Integer, SkillEntry> skillinfo : skills.entrySet()) {
////                Skill skill = SkillFactory.getSkill(skillinfo.getKey());
////                mplew.writeInt(skill.getId());
////                if (skill.isLinkSkills()) { //別人傳授給角色的技能 寫別人角色的ID
////                    mplew.writeInt(skillinfo.getValue().teachId);
////                } else if (skill.isTeachSkills()) { //如果是自己的傳授技能 傳授對像不為空寫傳授對象的角色ID 如果為空寫 自己的角色ID
////                    mplew.writeInt(skillinfo.getValue().teachId > 0 ? skillinfo.getValue().teachId : chr.getId()); //skillinfo.getValue().skillevel
////                } else {
////                    mplew.writeInt(skillinfo.getValue().skillevel);
////                }
////                addExpirationTime(mplew, skillinfo.getValue().expiration);
////                if (skill.isFourthJob()) {
////                    mplew.writeInt(skillinfo.getValue().masterlevel);
////                }
////                if (skill.getId() == 陰陽師.紫扇傳授 || skill.getId() == 陰陽師.紫扇傳授_傳授) {
////                    mplew.writeInt(skillinfo.getValue().masterlevel);
////                }
////            }
////            //傳授技能的等級
////            Map<Integer, SkillEntry> teachList = chr.getLinkSkills();
////            mplew.writeShort(teachList.size());
////            for (Entry<Integer, SkillEntry> skill : teachList.entrySet()) {
////                mplew.writeInt(skill.getKey());
////                mplew.writeShort(skill.getValue().skillevel - 1);
////            }
////            // getSonOfLinkedSkills
////            Map<Integer, Pair<Integer, SkillEntry>> sonOfLinkedSkills = chr.getSonOfLinkedSkills();
////            mplew.writeInt(sonOfLinkedSkills.size());
////            for (Entry<Integer, Pair<Integer, SkillEntry>> entry : sonOfLinkedSkills.entrySet()) {
////                writeSonOfLinkedSkill(mplew, entry.getKey(), entry.getValue());
////            }
////
////            mplew.write((int) chr.getInfoQuestValueWithKey(2498, "hyperstats"));
////            for (int i = 0; i <= 2; i++) {
////                mplew.writeInt(chr.loadHyperStats(i).size());
////                for (MapleHyperStats mhsz : chr.loadHyperStats(i)) {
////                    mplew.writeInt(mhsz.getPosition());
////                    mplew.writeInt(mhsz.getSkillid());
////                    mplew.writeInt(mhsz.getSkillLevel());
////                }
////            }
////        }
////        // CoolDownInfo
////        if ((flag & 0x8000L) != 0x0L) {
////            List<MapleCoolDownValueHolder> cooldowns = chr.getCooldowns();
////            mplew.writeShort(cooldowns.size());
////            for (MapleCoolDownValueHolder cooling : cooldowns) {
////                mplew.writeInt(cooling.skillId);
////                int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
////                mplew.writeInt(timeLeft / 1000); //V.103修改為int
////                //System.out.println("技能冷卻 - 技能ID: " + cooling.skillId + " 剩餘時間: " + (timeLeft / 1000) + " 秒");
////            }
////        }
////        if ((flag & 0x1L) != 0x0L) {
////            for (int n = 0; n < 6; n++) {
////                mplew.writeInt(0);
////            }
////            for (int n = 0; n < 6; n++) {
////                mplew.write(0);
////            }
////        }
////        // QuestInfo
////        if ((flag & 0x200L) != 0x0L) {
////            List<MapleQuestStatus> started = chr.getStartedQuests();
////            boolean bUnk = true;
////            mplew.write(bUnk);
////            mplew.writeShort(started.size());
////            for (MapleQuestStatus q : started) { // 檢測是否接過任務
////                mplew.writeInt(q.getQuest().getId()); // 任務ID
//////            mplew.writeShort(0); // 若任務ID不存在為0，否則為-1
////                if (q.hasMobKills()) {
////                    StringBuilder sb = new StringBuilder();
////                    for (int kills : q.getMobKills().values()) {
////                        sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
////                    }
////                    mplew.writeMapleAsciiString(sb.toString());
////                } else {
////                    mplew.writeMapleAsciiString(q.getCustomData() == null ? "" : q.getCustomData());
////                }
////            }
////            if (!bUnk) {
////               mplew.writeShort(0); // for UInt
////           }
////           mplew.writeShort(0); // String String
////       }
////       if ((flag & 0x4000L) != 0x0L) {
////           boolean bUnk = true;
////           mplew.write(bUnk);
////           List<MapleQuestStatus> completed = chr.getCompletedQuests();
////           if (ServerConfig.HideBulbQuest) {
////               List<MapleQuest> questList = new LinkedList<>();
////               for (MapleQuestStatus q : completed) {
////                   questList.add(q.getQuest());
////               }
////               List<Integer> ignoreQuests = Arrays.asList(5741, 5742, 5743, 5744, 5745);
////               for (MapleQuest q : MapleQuest.GetBulbQuest()) {
////                   if (!questList.contains(q) && !ignoreQuests.contains(q.getId())) {
////                       completed.add(new MapleQuestStatus(q, 2));
////                       questList.add(q);
////                   }
////               }
////               int[] extraQuests = {32510};
////               for (int nQ : extraQuests) {
////                   MapleQuest q = MapleQuest.getInstance(nQ);
////                   if (!questList.contains(q)) {
////                       completed.add(new MapleQuestStatus(q, 2));
////                       questList.add(q);
////                   }
////               }
////           }
////           mplew.writeShort(completed.size());
////           for (MapleQuestStatus q : completed) {
////               mplew.writeInt(q.getQuest().getId());
////               mplew.writeLong(getTime(q.getCompletionTime()));//int to long at V.149
////           }
////           if (!bUnk) {
////               mplew.writeShort(0); // for UInt
////           }
////       }
////       if ((flag & 0x400L) != 0x0L) {
////           mplew.writeShort(0);
////       }
////       /*
////        * RingInfo
////        */
////       if ((flag & 0x800L) != 0x0L) {
////           Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> aRing = chr.getRings(true);
////           //戀人戒指
////           List<MapleRing> cRing = aRing.getLeft();
////           mplew.writeShort(cRing.size());
////           for (MapleRing ring : cRing) { // 35
////               mplew.writeInt(ring.getPartnerChrId());
////               mplew.writeAsciiString(ring.getPartnerName(), 15);
////               mplew.writeLong(ring.getRingId());
////               mplew.writeLong(ring.getPartnerRingId());
////           }
////           //好友戒指
////           List<MapleRing> fRing = aRing.getMid();
////           mplew.writeShort(fRing.size());
////           for (MapleRing ring : fRing) { // 39
////               mplew.writeInt(ring.getPartnerChrId());
////               mplew.writeAsciiString(ring.getPartnerName(), 15);
////               mplew.writeLong(ring.getRingId());
////               mplew.writeLong(ring.getPartnerRingId());
////               mplew.writeInt(ring.getItemId());
////           }
////           //結婚戒指
////           List<MapleRing> mRing = aRing.getRight();
////           mplew.writeShort(mRing.size());
////           for (MapleRing ring : mRing) {// 52
////               mplew.writeInt(chr.getMarriageId());
////               mplew.writeInt(chr.getId());
////               mplew.writeInt(ring.getPartnerChrId());
////               mplew.writeShort(3); //1 = engaged 3 = married
////               mplew.writeInt(ring.getItemId());
////               mplew.writeInt(ring.getItemId());
////               mplew.writeAsciiString(chr.getName(), 15);
////               mplew.writeAsciiString(ring.getPartnerName(), 15);
////           }
////       }
////       /*
////        * RocksInfo
////        */
////       if ((flag & 0x1000L) != 0x0L) {
////           int[] mapz = chr.getRegRocks();
////           for (int i = 0; i < 5; i++) { // VIP teleport map
////               mplew.writeInt(mapz[i]);
////           }
////           int[] map = chr.getRocks();
////           for (int i = 0; i < 10; i++) { // VIP teleport map
////               mplew.writeInt(map[i]);
////           }
////           int[] maps = chr.getHyperRocks();
////           for (int i = 0; i < 13; i++) { // VIP teleport map
////               mplew.writeInt(maps[i]);
////           }
////       }
////       /*
////        * QuestDataInfo
////        * 將任務數據根據共享級別分開存放
////        */
///
////       if ((flag & 0x40000L) != 0x0L) {
////           Map<Integer, String> questInfos = new LinkedHashMap<>();
////           for (Entry<Integer, String> quest : chr.getInfoQuest_Map().entrySet()) {
////               questInfos.put(quest.getKey(), quest.getValue());
////           }
////           for (Entry<Integer, String> wsi : chr.getWorldShareInfo().entrySet()) {
////               if (!GameConstants.isWorldShareQuest(wsi.getKey())) {
////                   questInfos.put(wsi.getKey(), wsi.getValue());
////               }
////           }
////           mplew.writeShort(questInfos.size());
////           for (Entry<Integer, String> quest : questInfos.entrySet()) {
////               mplew.writeInt(quest.getKey());
////               mplew.writeMapleAsciiString(quest.getValue() == null ? "" : quest.getValue());
////           }
////       }
////       /*if ((flag & 0x20L) != 0x0L) {
////           short nCount = 0;
////           mplew.writeShort(nCount);
////           for (int i = 0; i < nCount; i++) {
////               mplew.writeInt(0);
////               //AvatarLook::Decode
////           }
////       }*/
////       if ((flag & 0x80000L) != 0x0L) {
////           short nCount = 0;
////           mplew.writeShort(nCount);
////           for (int i = 0; i < nCount; i++) {
////               mplew.writeInt(0);
////               mplew.writeShort(0);
////           }
////       }
///
////       mplew.writeBool(true);
///
////       if ((flag & 0x8000000000L) != 0x0L) {
////           int nCount = 0;
////           mplew.writeInt(nCount);
////           for (int j = 0; j < nCount; ++j) {
////               mplew.writeInt(26);
////               mplew.writeMapleAsciiString("Present=7");
////           }
////       }
////       if ((flag & 0x100000000000L) != 0x0L) {
////           int nCount = 1;
////           mplew.writeInt(nCount);
////           for (int k = 0; k < nCount; ++k) {
////               mplew.writeInt(4475);
////               mplew.writeInt(-1);
////           }
////       }
////       if ((flag & 0x200000L) != 0x0L) {
////           addJaguarInfo(mplew, chr); // 狂豹獵人的豹子信息 不是該職業就不發送
////       }
////       if ((flag & 0x800L) != 0x0L) {
////           if (JobConstants.is神之子(chr.getJob())) {
////               chr.getStat().zeroData(mplew, chr, 0xffff, chr.isBeta());
////           }
////       }
////       if ((flag & 0x4000000L) != 0x0L) {
////           mplew.writeShort(chr.getBuyLimit().size() + chr.getAccountBuyLimit().size());
////           for (Entry<Integer, NpcShopBuyLimit> entry : chr.getBuyLimit().entrySet()) {
////               final int shopId = entry.getKey();
////               final NpcShopBuyLimit buyLimit = entry.getValue();
////               final MapleShop shop = MapleShopFactory.getInstance().getShop(shopId);
////               mplew.writeInt(shopId);
////               mplew.writeShort((shop != null) ? buyLimit.getData().size() : 0);
////               if (shop != null) {
////                   for (Entry<Integer, BuyLimitData> o2 : buyLimit.getData().entrySet()) {
////                       final int itemId = o2.getKey();
////                       final BuyLimitData data = o2.getValue();
////                       final int count = data.getCount();
////                       final long date = data.getDate();
////                       mplew.writeInt(shopId);
////                       mplew.writeShort(shop.getBuyLimitItemIndex(o2.getKey()));
////                       mplew.writeInt(itemId);
////                       mplew.writeShort(count);
////                       addExpirationTime(mplew, date);
////                       mplew.writeMapleAsciiString("");
////                       mplew.writeInt(0);
////                   }
////               }
////           }
////           for (Entry<Integer, NpcShopBuyLimit> entry : chr.getAccountBuyLimit().entrySet()) {
////               final int shopId = entry.getKey();
////               final NpcShopBuyLimit buyLimit = entry.getValue();
////               final MapleShop shop = MapleShopFactory.getInstance().getShop(shopId);
////               mplew.writeInt(shopId);
////               mplew.writeShort((shop != null) ? buyLimit.getData().size() : 0);
////               if (shop != null) {
////                   for (Entry<Integer, BuyLimitData> o2 : buyLimit.getData().entrySet()) {
////                       final int itemId = o2.getKey();
////                       final BuyLimitData data = o2.getValue();
////                       final int count = data.getCount();
////                       final long date = data.getDate();
////                       mplew.writeInt(shopId);
////                       mplew.writeShort(shop.getBuyLimitItemIndex(o2.getKey()));
////                       mplew.writeInt(itemId);
////                       mplew.writeShort(count);
////                       addExpirationTime(mplew, date);
////                       mplew.writeMapleAsciiString("");
////                       mplew.writeInt(0);
////                   }
////               }
////           }
////       }
///
////       if ((flag & 0x2000000000000000L) != 0x0L) {
////           int nCount = 0;
////           mplew.writeShort(nCount);
////           for (int i = 0; i < nCount; i++) {
////               mplew.writeInt(0);
////               mplew.writeShort(0);
////               // sub_1403B6BE0
////           }
////       }
///
////       //V.160 new:
////       /*if ((flag & 0x20000000L) != 0x0L) {
////           int nCount = 0;
////           mplew.writeShort(nCount);
////           for (int i = 0; i < nCount; i++) {
////               int nnCount = 0;
////               mplew.writeShort(nnCount);
////               int a1 = 0;
////               mplew.writeInt(a1); // 9063002
////               if (nnCount > 0 && a1 > 0) {
////                   for (int j = 0; j < nnCount; j++) {
////                       mplew.writeInt(0); // 9063002
////                       mplew.writeShort(0); // 36
////                       mplew.writeInt(0); // 2439267
////                       mplew.writeShort(0); // 1
////                       mplew.writeLong(0); // 2019/3/27 下午 5:22
////                   }
////               }
////           }
////       }*/
////       //end
////       if ((flag & 0x20000000L) != 0x0L) {
////           //獲取複製技能數裝備的技能列表
////           for (int i = 0; i < 16; i++) {
////               mplew.writeInt(chr.getStealMemorySkill(i));
////           }
////       }
////       if ((flag & 0x10000000L) != 0x0L) {
////           //裝備中的技能
////           int[] p_skills = {幻影俠盜.盜亦有道Ⅰ, 幻影俠盜.盜亦有道Ⅱ, 幻影俠盜.盜亦有道Ⅲ, 幻影俠盜.盜亦有道Ⅳ, 幻影俠盜.盜亦有道H};
////           for (int i : p_skills) {
////               mplew.writeInt(chr.getEquippedStealSkill(i));
////           }
////       }
////       if ((flag & 0x80000000L) != 0x0L) {
////           mplew.writeShort(chr.getInnerSkillSize()); //內在能力技能數量
////           for (int i = 0; i < chr.getInnerSkillSize(); i++) {
////               InnerSkillEntry innerSkill = chr.getInnerSkills()[i];
////               if (innerSkill != null) {
////                   mplew.write(innerSkill.getPosition()); // key
////                   mplew.writeInt(innerSkill.getSkillId()); // id 7000000 id ++
//                   mplew.write(innerSkill.getSkillLevel());  // level
//                   mplew.write(innerSkill.getRank()); // rank, C, B, A, and S
//               } else {
//                   mplew.writeZeroBytes(7);
//               }
//           }
//       }
//       if ((flag & 0x40000000000000L) != 0x0L) {
//           mplew.writeShort(chr.getSoulCollection().size());
//           for (Entry<Integer, Integer> entry : chr.getSoulCollection().entrySet()) {
//               mplew.writeInt(entry.getKey());
//               mplew.writeInt(entry.getValue());
//           }
//       }
//       if ((flag & 0x100000000L) != 0x0L) {
//           mplew.writeInt(1); //榮譽等級//118已經不存在了
//           mplew.writeInt(chr.getHonor()); //聲望點數
//       }
//       /*
//       if ((flag & 0x4000L) != 0x0L) {//  OX Quiz
//           int nCount = 0;
//           mplew.writeShort(nCount);
//           for (int i = 0; i < nCount; i++) {
//               mplew.writeInt(0);
//               mplew.writeInt(0);
//               mplew.writeMapleAsciiString("");
//               mplew.write(0);
//               mplew.writeLong(0);
//               mplew.writeInt(0);
//               mplew.writeMapleAsciiString("");
//               mplew.write(0);
//               mplew.write(0);
//               mplew.writeLong(0);
//               mplew.writeMapleAsciiString("");
//           }
//       }
//       */
//       if ((flag & 0x800000000000L) != 0x0L) {// 經驗椅子
//           int nCount = 0;
//           mplew.writeShort(nCount);
//           for (int i = 0; i < nCount; i++) {
//               mplew.writeZeroBytes(20);
//           }
//       }
//       if ((flag & 0x1000000000000L) != 0x0L) {
//           addRedLeafInfo(mplew, chr); // v258(sub_1403FB2C0)
//       }
//       if ((flag & 0x2000000000000L) != 0x0L) {
//           mplew.writeShort(0);
//       }
//       if ((flag & 0x200000000L) != 0x0L) {
//           mplew.write(1);
//           mplew.writeShort(0);
//       }
//       if ((flag & 0x400000000L) != 0x0L) {
//           mplew.write(0);
//           // GW_ItemSlotBase::Decode
//       }
//       if ((flag & 0x800000000L) != 0x0L) {
//           writeDressUpInfo(mplew, chr);
//       }
//       if ((flag & 0x20000000000000L) != 0x0L) { // ActiveDamageSkin
//           mplew.writeInt(0);
//           mplew.writeInt(0);
//           mplew.writeLong(getTime(-2L));
//       }
//       /*if ((flag & 0x10L) != 0x0L) {
//           writeEsInfo(mplew, chr);
//       }*/
//       if ((flag & 0x20000000000L) != 0x0L) {
//           mplew.write(0);
//       }
//       //V.160 new:
//       if ((flag & 0x4000000000000000L) != 0x0L) {
//           mplew.writeInt(-1);
//           mplew.writeInt(-1157267456);
//           mplew.writeLong(0L);
//           mplew.writeInt(0);
//           mplew.writeInt(0);
//           mplew.writeShort(0);// V.181 new
//       }
//       //end
//       if ((flag & 0x40000000000L) != 0x0L) {
//           mplew.writeInt(chr.getLove()); //V.112新增 好感度
//           mplew.writeLong(getTime(-2)); //00 40 E0 FD 3B 37 4F 01
//           mplew.writeInt(0);
//       }
//       // v133 start RunnerGameRecord
//       if ((flag & 0x80000000000000L) != 0x0L) {
//           mplew.writeInt(chr.getId());
//           mplew.writeInt(0);
//           mplew.writeInt(0);
//           mplew.writeInt(0);
//           mplew.writeLong(PacketHelper.getTime(-2));
//           mplew.writeInt(10);
//       }
//       if ((flag & 0x200000000000000L) != 0x0L) {
//           mplew.writeInt(0); // -> int byte byte byte
//           mplew.writeInt(0);
////           mplew.writeLong(0L);
///
////           // sub_1403C4C70
////           mplew.writeInt(0); // -> int byte byte byte
////           mplew.writeInt(0);
////       }
////       /*
////         賬號下角色共享任務數據
////        */
////       Map<Integer, String> wsInfos = new LinkedHashMap<>();
////       for (Entry<Integer, String> wsi : chr.getWorldShareInfo().entrySet()) {
////           if (GameConstants.isWorldShareQuest(wsi.getKey())) {
////               wsInfos.put(wsi.getKey(), wsi.getValue());
////           }
////       }
////       mplew.writeShort(wsInfos.size());
////       for (Entry<Integer, String> quest : wsInfos.entrySet()) {
////           mplew.writeInt(quest.getKey());
////           mplew.writeMapleAsciiString(quest.getValue() == null ? "" : quest.getValue());
////       }
////       // v133 end
////       if ((flag & 0x100000000000000L) != 0x0L) {
////           mplew.writeShort(chr.getMobCollection().size());
////           for (Entry<Integer, String> entry : chr.getMobCollection().entrySet()) {
////               mplew.writeInt(entry.getKey());
////               mplew.writeMapleAsciiString(entry.getValue());
////           }
////       }
///
////       mplew.writeInt(0); // sub_58B140 // v255 - sub_1403DCFF0 -> int str
///
////       if ((flag & 0x400000000000000L) != 0x0L) {
////           mplew.writeShort(0);
////       }
////       if ((flag & 0x800000000000000L) != 0x0L) {
////           // VCoreSkill
////           VCorePacket.writeVCoreSkillData(mplew, chr);
////       }
////       // v258 New
////       // sub_1403F5620
////       chr.loadHexSkills();
////       encodeHexaSkills(mplew, chr);
////       // sub_1403F5620 end
///
////       // v258 Add
////       // sub_1403F5FD0
////       chr.loadHexStats();
////       encodeSixStats(mplew, chr);
////       // sub_1403F5FD0 end
///
////       if ((flag & 0x1000000000000000L) != 0x0L) { // TMS 229 done Achievement
////           mplew.writeInt(chr.getClient().getAccID());
////           mplew.writeInt(chr.getId());
////           mplew.writeInt(0);
////           mplew.writeInt(-1);
////           mplew.writeInt(Integer.MAX_VALUE);
////           mplew.writeLong(getTime(-2));
////           // sub_1403C7AE0
////           int a2 = 0;
////           mplew.writeInt(a2);
////           for (int i = 0; i < a2; i++) {
////               mplew.writeInt(0);
////               mplew.write(0);
////               mplew.write(0);
////               mplew.writeLong(DateUtil.getFileTimestamp(System.currentTimeMillis())); // or getTime(-2)
////               int unk = 41;
////               mplew.writeInt(unk);
////               if (unk == 42) {
////                   mplew.writeMapleAsciiString("");
////                   // Plugin.script=0;user_lvup=104
////                   // Plugin.script=0;user_lvup=17
////                   // Plugin.script=0;field_enter=1
////                   // union_attacker_power_change=0;Plugin.script=0
////               } else {
////                   mplew.writeLong(0); // int int
////               }
////           }
////           int v6 = 0;
////           mplew.writeInt(v6);
////           for (int i = 0; i < v6; i++) {
////               mplew.writeInt(0);
////               mplew.write(0);
////               mplew.writeLong(0);
////           }
////       }
////       if ((flag & 0x20L) != 0x0L) { // TMS 229 done
////           int v6 = 0;
////           mplew.writeInt(v6);// 未知，V.144新增
////           for (int i = 0; i < v6; i++) {
////               mplew.writeLong(0);
////               mplew.writeInt(0);
////               mplew.writeInt(0);
////               mplew.writeLong(0);
////               mplew.writeLong(0);
////               mplew.writeLong(0);
////           }
////       }
//
////       for (int num = 3; num > 0; num--) { // TMS 229 done
////           if ((flag & (num <= 2 ? 0x8000000000000000L : 0x10000000L)) != 0x0L) {
////               encodeCombingRoomInventory(mplew, chr.getSalon().getOrDefault(num, new LinkedList<>()));
////           }
////       }
///
////       if ((flag & 0x8000000L) != 0x0L) { // TMS 229 done Emoticons
////           int nCount = 0;
////           mplew.writeInt(nCount); // Emoticons
////           for (int i = 0; i < nCount; i++) {
////               mplew.writeInt(0);
////               mplew.writeZeroBytes(14);
////           }
//
////           nCount = 0;
////           mplew.writeInt(nCount); //EmoticonTabs
////           for (int i = 0; i < nCount; i++) {
////               mplew.writeShort(0);
////               mplew.writeInt(0);
////           }
////           mplew.writeShort(8);
//
////           nCount = 0;
////           mplew.writeInt(nCount); // SavedEmoticon
////           for (int i = 0; i < nCount; i++) {
////               mplew.writeShort(0);
////               mplew.writeZeroBytes(25);
////           }
///
////           nCount = 0;
////           mplew.writeInt(nCount); //SavedEmoticon
////           for (int i = 0; i < nCount; i++) {
////               mplew.writeMapleAsciiString("");
////               mplew.writeZeroBytes(25);
////           }
////       }
////       if ((flag & 0x2000000000000000L) != 0x0L) { // TMS 238 done
////           mplew.writeInt(pets.size());
////           for (MaplePet pet : pets) {
////               mplew.writeLong(pet.getUniqueId());
////               int a2 = 0;
////               mplew.writeInt(a2);
////               for (int j = 0; j < a2; j++) {
////                   mplew.writeInt(0);
////               }
////           }
////       }
////       if ((flag & 0x4000000000000L) != 0x0L) { // TMS 229 done
////           mplew.write(1);
////           String string = chr.getOneInfo(17008, "T");
////           String string2 = chr.getOneInfo(17008, "L");
////           String string3 = chr.getOneInfo(17008, "E");
////           mplew.write(string == null ? 0 : Integer.valueOf(string));
////           mplew.writeInt(string2 == null ? 1 : Integer.valueOf(string2));
////           mplew.writeInt(string3 == null ? 0 : Integer.valueOf(string3));
////           mplew.writeInt(100 - chr.getPQLog("航海能量"));
////           mplew.writeLong(getTime(System.currentTimeMillis()));
//
////           String questinfo = chr.getInfoQuest(17018);
////           String[] questinfos = questinfo.split(";");
////           mplew.writeShort(!questinfo.isEmpty() ? questinfos.length : 0);
////           for (String questinfo1 : questinfos) {
////               if (!questinfo1.isEmpty()) {
////                   String[] split = questinfo1.split("=");
////                   mplew.write(Integer.valueOf(split[0]));
////                   mplew.writeInt(Integer.valueOf(split[1]));
////                   mplew.writeInt(0);
////               }
////           }
////           mplew.writeShort(ItemConstants.航海材料.length);
////           for (int i : ItemConstants.航海材料) {
////               mplew.writeInt(i);
////               mplew.writeInt(chr.getPQLog(String.valueOf(i)));
////               mplew.writeLong(getTime(System.currentTimeMillis()));
////           }
////       }
////       if ((flag & 0x8000000000000L) != 0x0L) { // TMS 229 done
////           mplew.write(0);
////       }
////       if ((flag & 0x8000000000000L) != 0x0L) { // TMS 229 done
////           // 內面耀光技能
////           List<Integer> buffs = new LinkedList<>();
////           if (chr.getKeyValue("InnerGlareBuffs") != null) {
////               for (String s : chr.getKeyValue("InnerGlareBuffs").split(",")) {
////                   if (s.isEmpty()) {
////                       continue;
////                   }
////                   buffs.add(Integer.parseInt(s));
////               }
////           }
////           mplew.writeReversedVarints(buffs.size());
////           for (int buffId : buffs) {
////               mplew.writeInt(buffId);
////           }
                }
            }
        }
    }

    public static void encodeHexaSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getHexaSkills().size());
        for (MapleHexaSkill mhs : chr.getHexaSkills().values()) {
            mplew.writeInt(mhs.getId()); // id
            mplew.writeInt(mhs.getSkilllv()); // level
            mplew.write(1);
            mplew.write(0);
        }
        mplew.write(0);
        int HEXA2_Size = 0;
        mplew.writeInt(HEXA2_Size);
        for (int i = 0; i < HEXA2_Size; i++) {
            mplew.writeInt(0);
        }
    }

    public static void encodeSixStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getHexaStats().size());
        for (Entry<Integer, MapleHexaStat> msss : chr.getHexaStats().entrySet()) {
            mplew.writeInt(msss.getValue().getSolt());
            // preset0
            mplew.writeInt(msss.getValue().getSolt());
            mplew.writeInt(0);
            mplew.writeInt(1);
            mplew.writeInt(msss.getValue().getMain0());
            mplew.writeInt(msss.getValue().getMain0Lv());
            mplew.writeInt(msss.getValue().getAddit0S1());
            mplew.writeInt(msss.getValue().getAddit0S1Lv());
            mplew.writeInt(msss.getValue().getAddit0S2());
            mplew.writeInt(msss.getValue().getAddit0S2Lv());
            //preset1
            mplew.writeInt(msss.getValue().getMain1() == -1 ? 0 : msss.getValue().getSolt());
            mplew.writeInt(msss.getValue().getMain1() == -1 ? 0 : 1);
            mplew.writeInt(msss.getValue().getMain1() == -1 ? 0 : 1);
            mplew.writeInt(msss.getValue().getMain1() == -1 ? 0 : msss.getValue().getMain1());
            mplew.writeInt(msss.getValue().getMain1() == -1 ? 0 : msss.getValue().getMain1Lv());
            mplew.writeInt(msss.getValue().getMain1() == -1 ? 0 : msss.getValue().getAddit1S1());
            mplew.writeInt(msss.getValue().getMain1() == -1 ? 0 : msss.getValue().getAddit1S1Lv());
            mplew.writeInt(msss.getValue().getMain1() == -1 ? 0 : msss.getValue().getAddit1S2());
            mplew.writeInt(msss.getValue().getMain1() == -1 ? 0 : msss.getValue().getAddit1S2Lv());
        }
        mplew.writeInt(chr.getHexaStats().size());
        for (Entry<Integer, MapleHexaStat> msss : chr.getHexaStats().entrySet()) {
            mplew.writeInt(msss.getValue().getSolt());
            mplew.writeInt(msss.getValue().getSolt());
            mplew.writeInt(msss.getValue().getPreset());
        }
        int HEXA5_Size = 0;
        mplew.writeInt(HEXA5_Size);
        for (int i = 0; i < HEXA5_Size; i++) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        int HEXA6_Size = 0;
        mplew.writeInt(HEXA6_Size);
        for (int i = 0; i < HEXA6_Size; i++) {
            mplew.writeInt(0);
        }
    }

    public static void encodeCombingRoomInventory(MaplePacketLittleEndianWriter mplew, List<Integer> styles) {
        mplew.write(styles.size());
        mplew.write(styles.size());
        for (int i = 1; i <= 102; i++) { // TMS 232 9 -> 102
            mplew.write(styles.size() >= i);
            if (styles.size() >= i) {
                encodeCombingRoomSlot(mplew, styles.get(i - 1));
            }
        }
    }

    public static void encodeCombingRoomSlot(MaplePacketLittleEndianWriter mplew, int styleID) {
        mplew.write(0);
        mplew.writeInt(styleID);
        mplew.writeInt(styleID);
    }

    public static void addRedLeafInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        int[] idarr = new int[]{9410165, 9410166, 9410167, 9410168, 9410198};
        mplew.writeInt(chr.getClient().getAccID());
        mplew.writeInt(chr.getId());
        mplew.writeInt(idarr.length);
        mplew.writeInt(0);
        for (int i = 0; i < idarr.length; i++) {
            mplew.writeInt(idarr[i]);
            mplew.writeInt(chr.getFriendShipPoints()[i]);
        }
    }

    public static void encodeInventory(MaplePacketLittleEndianWriter mplew, List<Item> list, MapleCharacter chr) {
        List<Item> items = new LinkedList<>(list);
        items.add(null);
        for (Item item : items) {
            addItemPosition(mplew, item, false, false);
            if (item == null) {
                break;
            }
            GW_ItemSlotBase_Encode(mplew, item);
        }
    }

    public static void addShopInfo(MaplePacketLittleEndianWriter mplew, MapleShop shop, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        mplew.writeInt(0); // 固定數值
        mplew.writeInt(2025021205); // 當前時間戳
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        List<MapleShopItem> shopItems = shop.getItems(c);
        mplew.writeShort(shopItems.size()); // 商品總數量
        for (MapleShopItem item : shopItems) {
            mplew.writeInt(0); // 未知填充數據
            mplew.writeInt(0);
            mplew.writeInt(item.getItemId()); // 物品ID
            mplew.writeInt(0);
            mplew.writeInt(0); // 可能是物品唯一標識符
            mplew.writeInt(0);
            mplew.writeLong(94354848000000000L); // 開始時間
            mplew.writeInt(0); // 潛能等級
            mplew.writeInt(item.getPrice()); // 售價
            mplew.writeInt(0); // v264+ unk
            mplew.writeInt(0); // v264+ unk
            mplew.writeInt(0); // v264+ unk
            mplew.writeInt(item.getPointQuestID()); // 點數兌換任務
            mplew.writeInt(0); // v264+未知
            mplew.writeInt(item.getPointPrice()); // 點數價格
            mplew.writeInt(item.getBuyLimitWorldAccount()); // 帳戶購買限制
            mplew.writeInt(item.getMinLevel()); // 最低等級
            mplew.writeShort((short) item.getMinLevel()); // 4.5
            mplew.writeShort((short) item.getMaxLevel()); // 最高等級
            mplew.writeInt(0); // v264+未知
            mplew.write(0);
            mplew.write(0);
            mplew.write(0);
            mplew.writeLong(94354848000000000L); // 開始時間
            mplew.writeLong(150842304000000000L); // 結束時間
            mplew.writeInt(0); // 任務額外值

            mplew.writeInt(0);
            mplew.writeShort(0);
            mplew.writeInt(0);

            mplew.writeInt(1); // V.160 新增
            mplew.writeInt(0); // 任務ID
            mplew.writeShort(0); // 未知
            mplew.writeShort(0); // 未知
            mplew.write(0); // V.151 新增
            mplew.writeInt(0); // 任務額外ID
            mplew.writeMapleAsciiString(""); // 任務額外鍵值
            mplew.writeInt(0); // 任務額外值
            mplew.writeInt(0); // V.160 新增
            mplew.write(0); // TMS V.220 新增
            mplew.writeMapleAsciiString(""); // 未知

            int slotMax = ii.getSlotMax(item.getItemId());
            if (ItemConstants.類型.可充值道具(item.getItemId())) {
                mplew.writeDouble(ii.getUnitPrice(item.getItemId()));
            } else {
                int quantity = item.getQuantity() == 0 ? slotMax : item.getQuantity();
                mplew.writeShort(quantity); // 數量
                slotMax = quantity > 1 ? 1 : item.getBuyable() == 0 ? slotMax : item.getBuyable(); // 可購買數量
            }
            mplew.writeShort(slotMax); //
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeMapleAsciiString("1900010100");
            mplew.writeMapleAsciiString("2079010100");

            // 可能是額外限制或任務數據
            for (int i = 0; i < 4; i++) {
                mplew.writeInt(0);
            }
            mplew.writeInt(9410165);
            mplew.writeInt(0);
            mplew.writeInt(9410166);
            mplew.writeInt(0);
            mplew.writeInt(9410167);
            mplew.writeInt(0);
            mplew.writeInt(9410168);
            mplew.writeInt(0);
            mplew.writeInt(9410198);
            mplew.writeInt(0);

            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.write(0);
            mplew.write(0);

            // 回購欄的道具信息
            Item rebuy = item.getRebuy();
            mplew.write(rebuy == null ? 0 : 1);
            if (rebuy != null) {
                GW_ItemSlotBase_Encode(mplew, rebuy);
            }
        }
    }


    public static void addJaguarInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if (JobConstants.is狂豹獵人(chr.getJob())) {
            mplew.write(chr.getIntNoRecord(GameConstants.JAGUAR));
//            mplew.writeHexString("F8 76 12 00 72 CE 20 00 31 95 4E 00 1D 5D 7C 00 00 00 00 00");
            for (int i = 1; i <= 5; i++) {
                mplew.writeInt(0); //probably mobID of the 5 mobs that can be captured.
            }
        }
    }


    public static void addSkillPets(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        //mplew.write(JobConstants.is陰陽師(chr.getJob()) ? 1 : 0);
        if (JobConstants.is陰陽師(chr.getJob()) && chr.getHaku() != null) {
            mplew.write(1);
            mplew.writeInt(chr.getHaku().getObjectId());
            mplew.writeInt(陰陽師.花狐的同行);
            mplew.write(1);//chr.getLittleWhite().isShow() ? 1 : 2
            mplew.writePos(chr.getHaku().getPosition());
            mplew.write(0);
            mplew.writeShort(chr.getHaku().getStance());
        }
    }

    public static void writeDressUpInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter player) {
        boolean bl2 = JobConstants.is天使破壞者(player.getJob());
        if (bl2 && player.getKeyValue("Longcoat") == null) {
            player.setKeyValue("Longcoat", "1051291");
        }
        mplew.writeInt(bl2 ? player.getSecondFace() : 0);
        mplew.writeInt(bl2 ? player.getSecondHair() : 0);
        mplew.writeInt(bl2 ? Integer.valueOf(player.getKeyValue("Longcoat")) : 0);
        mplew.write(bl2 ? player.getSecondSkinColor() : 0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(0);
    }

    public static void write劍刃之壁(MaplePacketLittleEndianWriter mplew, MapleCharacter player, int sourceid) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        int n4 = 0;
        switch (sourceid) {
            case 凱撒.意志之劍: {
                n4 = 1;
                break;
            }
            case 凱撒.進階意志之劍: {
                n4 = 2;
                break;
            }
            case 凱撒.意志之劍_變身: {
                n4 = 3;
                break;
            }
            case 凱撒.進階意志之劍_變身: {
                n4 = 4;
            }
        }
        int n5 = sourceid == 凱撒.意志之劍 || sourceid == 凱撒.意志之劍_變身 ? 3 : (sourceid == 0 ? 0 : 5);
        for (int i2 = 0; i2 < n5; ++i2) {
            arrayList.add(0);
        }
        mplew.writeInt(n4);
        mplew.writeInt(n5);
        mplew.writeInt(player.getBuffedIntZ(SecondaryStat.StopForceAtomInfo));
        mplew.writeInt(arrayList.size());
        arrayList.forEach(mplew::writeInt);
    }

    public static void writeSonOfLinkedSkill(MaplePacketLittleEndianWriter mplew, int skillId, Pair<Integer, SkillEntry> skillinfo) {
        mplew.writeInt(skillinfo.getLeft());
        mplew.writeInt(skillinfo.getRight().teachId);
        mplew.writeInt(skillId);
        mplew.writeShort(skillinfo.getRight().skillevel);
        mplew.writeLong(PacketHelper.getTime(skillinfo.getRight().expiration));
        mplew.writeInt(skillinfo.getRight().teachTimes);
    }

    public static void writeEsInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        MapleInventory inventory = chr.getInventory(MapleInventoryType.ELAB);
        List<Item> list = inventory.newList();
        List<Item> list2 = new ArrayList<>();
        for (Item item : list) {
            if (item.getESPos() != 0) {
                list2.add(item);
            }
        }
        mplew.writeShort(list2.size());
        for (Item item : list2) {
            mplew.writeShort(item.getESPos() - 1);
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getQuantity());
        }
        mplew.writeShort(inventory.list().size());
        for (Item item : inventory.list()) {
            mplew.writeShort(item.getESPos() - 1);
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getQuantity());
        }
    }

    public static void addDamageSkinInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        int skinId = chr.getDamageSkin();
        mplew.writeInt(chr.getDamSkinList().size());
        for (int id : chr.getDamSkinList()) {
            addDamageSkinInfo0(mplew, id, id >= 0 ? MapleItemInformationProvider.getInstance().getDamageSkinItemId(id) : 0, 0, "");
        }

        addDamageSkinInfo0(mplew, skinId, skinId >= 0 ? MapleItemInformationProvider.getInstance().getDamageSkinItemId(skinId) : 0, 0, ""); // 預設: 2438159 這是基本傷害字型。\r\n\r\n\r\n\r\n\r\n
        addDamageSkinInfo0(mplew, -1, 0, 1, "");
        addDamageSkinInfo0(mplew, -1, 0, 1, ""); // TMS 229
    }

    private static void addDamageSkinInfo0(MaplePacketLittleEndianWriter mplew, int skinId, int skinItemId, int b, String s) {
        mplew.writeInt(skinId);
        mplew.writeInt(skinItemId);
        mplew.writeLong(PacketHelper.getTime(-2)); // Zero Timer ->
    }

    public static void addChaterName(MaplePacketLittleEndianWriter mplew, String speekerName, String text) {
        addChaterName(mplew, speekerName, text, 0);
    }

    public static void addChaterName(MaplePacketLittleEndianWriter mplew, String speekerName, String text, int chrId) {
        mplew.writeMapleAsciiString(speekerName);//V.153 new
        mplew.writeMapleAsciiString(text);//V.153 new
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeShort(6);
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(0x03);
        mplew.write(0x00);
        mplew.write(0x06);
    }

    public static List<Pair<SecondaryStat, Pair<Integer, Integer>>> sortBuffStats(Map<SecondaryStat, Pair<Integer, Integer>> statups) {
        boolean changed;
        List<Pair<SecondaryStat, Pair<Integer, Integer>>> statvals = new ArrayList<>();
        for (Entry<SecondaryStat, Pair<Integer, Integer>> stat : statups.entrySet()) {
            statvals.add(new Pair<>(stat.getKey(), stat.getValue()));
        }
        do {
            changed = false;
            int i = 0;
            int k = 1;
            for (int iter = 0; iter < statvals.size() - 1; iter++) {
                Pair<SecondaryStat, Pair<Integer, Integer>> a = statvals.get(i);
                Pair<SecondaryStat, Pair<Integer, Integer>> b = statvals.get(k);
                if (a != null && b != null && a.left.getFlag() > b.left.getFlag()) {
                    Pair<SecondaryStat, Pair<Integer, Integer>> swap = new Pair<>(a.left, a.right);
                    statvals.remove(i);
                    statvals.add(i, b);
                    statvals.remove(k);
                    statvals.add(k, swap);
                    changed = true;
                }
                i++;
                k++;
            }
        } while (changed);
        return statvals;
    }
}
