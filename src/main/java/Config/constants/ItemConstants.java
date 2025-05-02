/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Config.constants;

import Client.inventory.Equip;
import Client.inventory.MapleInventoryType;
import Client.inventory.MapleWeapon;
import Config.configs.CSInfoConfig;
import Config.constants.enums.ChairType;
import Net.server.MapleItemInformationProvider;
import Net.server.StructItemOption;
import Net.server.StructSetItem;
import tools.Pair;
import tools.Randomizer;

import java.util.*;

/**
 * @author PlayDK
 */
public class ItemConstants {

    public static final int[] 靈魂結晶 = new int[]{2591010, 2591011, 2591012, 2591013, 2591014, 2591015, 2591016, 2591017, 2591018, 2591019, 2591020, 2591021, 2591022, 2591023, 2591024, 2591025, 2591026, 2591027, 2591028, 2591029, 2591030, 2591031, 2591032, 2591033, 2591034, 2591035, 2591036, 2591037, 2591038, 2591039, 2591040, 2591041, 2591042, 2591043, 2591044, 2591045, 2591046, 2591047, 2591048, 2591049, 2591050, 2591051, 2591052, 2591053, 2591054, 2591055, 2591056, 2591057, 2591058, 2591059, 2591060, 2591061, 2591062, 2591063, 2591064, 2591065, 2591066, 2591067, 2591068, 2591069, 2591070, 2591071, 2591072, 2591073, 2591074, 2591075, 2591076, 2591077, 2591078, 2591079, 2591080, 2591081, 2591082, 2591085, 2591086, 2591087, 2591088, 2591089, 2591090, 2591091, 2591092, 2591093, 2591094, 2591095, 2591096, 2591097, 2591098, 2591099, 2591100, 2591101, 2591102, 2591103, 2591104, 2591105, 2591106, 2591107, 2591108, 2591109, 2591110, 2591111, 2591112, 2591113, 2591114, 2591115, 2591116, 2591117, 2591118, 2591119, 2591120, 2591121, 2591122, 2591123, 2591124, 2591125, 2591126, 2591127, 2591128, 2591129, 2591130, 2591131, 2591132, 2591133, 2591134, 2591135, 2591136, 2591137, 2591138, 2591139, 2591140, 2591141, 2591142, 2591143, 2591144, 2591145, 2591146, 2591147, 2591148, 2591149, 2591150, 2591151, 2591152, 2591153, 2591154, 2591155, 2591156, 2591157, 2591158, 2591159, 2591160, 2591161, 2591162, 2591163, 2591164, 2591165, 2591166, 2591167, 2591168, 2591169, 2591170, 2591171, 2591172, 2591173, 2591174, 2591175, 2591176, 2591177, 2591178, 2591179, 2591180, 2591181, 2591182, 2591183, 2591184, 2591185, 2591186, 2591187, 2591188, 2591189, 2591190, 2591191, 2591192, 2591193, 2591194, 2591195, 2591196, 2591197, 2591198, 2591199, 2591200, 2591201, 2591202, 2591203, 2591204, 2591205, 2591206, 2591207, 2591208, 2591209, 2591210, 2591211, 2591212, 2591213, 2591214, 2591215, 2591216, 2591217, 2591218, 2591219, 2591220, 2591221, 2591222, 2591223, 2591224, 2591225, 2591226, 2591227, 2591228, 2591229, 2591230, 2591231, 2591232, 2591233, 2591234, 2591235, 2591236, 2591237, 2591238, 2591239, 2591240, 2591241, 2591242, 2591243, 2591244, 2591245, 2591246, 2591247, 2591248, 2591249, 2591250, 2591251, 2591252, 2591253, 2591254, 2591255, 2591256, 2591257, 2591258, 2591259, 2591260, 2591261, 2591262, 2591263, 2591264, 2591265, 2591266, 2591267, 2591268, 2591269, 2591270, 2591271, 2591272, 2591273, 2591274, 2591275, 2591276, 2591277, 2591278, 2591279, 2591288, 2591289, 2591290, 2591291, 2591292, 2591293, 2591294, 2591295, 2591296, 2591297, 2591298, 2591299, 2591300, 2591301, 2591302, 2591303, 2591304, 2591305, 2591306, 2591307, 2591308, 2591309, 2591310, 2591311, 2591312, 2591313, 2591314, 2591315, 2591316, 2591317, 2591318, 2591319, 2591320, 2591321, 2591322, 2591323, 2591324, 2591325, 2591326, 2591327, 2591328, 2591329, 2591330, 2591331, 2591332, 2591333, 2591334, 2591335, 2591336, 2591337, 2591338, 2591339, 2591340, 2591341, 2591342, 2591343, 2591344, 2591345, 2591346, 2591347, 2591348, 2591349, 2591350, 2591351, 2591352, 2591353, 2591354, 2591355, 2591356, 2591357, 2591358, 2591359, 2591360, 2591361, 2591362, 2591363, 2591364, 2591365, 2591366, 2591367, 2591368, 2591369, 2591370, 2591371, 2591372, 2591373, 2591374, 2591375, 2591376, 2591377, 2591378, 2591379, 2591380, 2591381};
    public static final short[] 靈魂結晶技能 = new short[]{177, 102, 103, 104, 131, 132, 201, 101, 102, 103, 104, 131, 132, 201, 105, 106, 107, 108, 133, 134, 202, 105, 106, 107, 108, 133, 134, 202, 109, 110, 111, 112, 135, 136, 203, 113, 114, 115, 116, 204, 151, 152, 137, 403, 603, 121, 122, 123, 124, 206, 155, 156, 139, 403, 603, 117, 118, 119, 120, 207, 153, 154, 138, 403, 603, 167, 168, 169, 170, 208, 171, 172, 177, 0, 0, 0, 0, 101, 102, 103, 104, 131, 132, 201, 101, 102, 103, 104, 131, 132, 201, 105, 106, 107, 108, 133, 134, 202, 105, 106, 107, 108, 133, 134, 202, 109, 110, 111, 112, 135, 136, 203, 113, 114, 115, 116, 204, 151, 152, 137, 117, 118, 119, 120, 207, 153, 154, 138, 121, 122, 123, 124, 206, 155, 156, 139, 101, 102, 103, 104, 131, 132, 201, 163, 164, 165, 166, 210, 151, 152, 175, 0, 101, 102, 103, 104, 131, 132, 201, 163, 164, 165, 166, 210, 151, 152, 175, 167, 168, 169, 170, 208, 171, 172, 177, 179, 180, 181, 182, 183, 184, 201, 185, 186, 187, 188, 205, 153, 154, 189, 0, 179, 180, 181, 182, 183, 184, 201, 185, 186, 187, 188, 205, 153, 154, 189, 109, 110, 111, 112, 135, 136, 203, 117, 118, 119, 120, 207, 153, 154, 138, 0, 109, 110, 111, 112, 135, 136, 203, 117, 118, 119, 120, 205, 153, 154, 138, 101, 102, 103, 104, 131, 132, 201, 167, 168, 169, 170, 208, 173, 172, 177, 0, 101, 102, 103, 104, 131, 132, 201, 167, 168, 169, 170, 208, 173, 172, 177, 167, 168, 169, 170, 208, 171, 172, 177, 0, 121, 186, 187, 188, 205, 153, 154, 189, 0, 185, 186, 187, 188, 207, 153, 154, 189, 0, 185, 186, 187, 188, 205, 153, 154, 189, 0, 185, 186, 187, 188, 207, 153, 154, 189, 0, 185, 186, 187, 188, 206, 153, 154, 189, 0, 121, 186, 187, 188, 205, 153, 154, 189, 185, 186, 187, 188, 205, 153, 154, 189, 185, 186, 187, 188, 205, 153, 154, 189, 185, 186, 187, 188, 207, 153, 154, 189, 185, 186, 187, 188, 206, 153, 154, 189};
    public static final int[] 航海材料 = new int[]{3100000, 3100001, 3100002, 3100003, 3100004, 3100005, 3100006, 3100007, 3100008, 3100010, 3100011};

    // 150套裝
    public static int[] fa() {
        return new int[]{2510538, 2510539, 2510540, 2510541, 2510542, 2510543, 2510544, 2510545, 2510546, 2510547, 2510548, 2510549, 2510550, 2510551, 2510552, 2510553, 2510554, 2510555, 2510556, 2510557, 2510558, 2510559, 2510560, 2510561, 2510562, 2510563, 2510564, 2510565, 2510566, 2510567, 2510621, 2510255, 2510256, 2510257, 2510258, 2510259, 2510528, 2510529, 2510530, 2510531, 2510532, 2510533, 2510534};
    }

    // 君主英勇配方
    public static int[] fb() {
        return new int[]{2510483, 2510484, 2510485, 2510486, 2510487, 2510488, 2510489, 2510490, 2510491, 2510492, 2510493, 2510494, 2510495, 2510496, 2510497, 2510498, 2510499, 2510500, 2510501, 2510502, 2510503, 2510504, 2510505, 2510506, 2510507, 2510508, 2510509, 2510510, 2510511, 2510512, 2510513, 2510514, 2510515, 2510516, 2510517, 2510518, 2510519, 2510520, 2510521, 2510522, 2510523, 2510524, 2510525, 2510526, 2510527};
    }

    // 礦石
    public static int[] fc() {
        return new int[]{4021000, 4021001, 4021002, 4021003, 4021004, 4021005, 4021006, 4021007, 4021008, 4021009, 4021012, 4021010, 4021013, 4021014, 4021015, 4021016, 4021019, 4021020, 4021021, 4021022, 4021031, 4021032, 4021033, 4021034, 4021035, 4021036, 4021037, 4021038, 4021039, 4021040, 4021041, 4021042, 4023023, 4023024, 4023025, 4023026, 4023021, 4023022};
    }

    public static boolean isForGM(int itemId) {
        return (itemId >= 2049335 && itemId <= 2049349)
                || // 強化捲軸
                itemId == 2430011
                || // 特務召喚
                itemId == 2430012
                || // 移除特務
                itemId == 2430124
                || // GM測試
                itemId == 2002085;// GM的無敵飲料
    }

    public static class 卷軸 {

        public static boolean canScroll(final int itemId) {
            return (itemId / 100000 != 19 && itemId / 100000 != 16) || (類型.心臟(itemId) && itemId != 1672030 && itemId != 1672031 && itemId != 1672032);
        }

        public static boolean canHammer(final int itemId) {
            switch (itemId) {
                case 1122000: // 闇黑龍王項鍊
                case 1122076: // 混沌闇黑龍王的項鍊
                    return false;
            }
            return canScroll(itemId);
        }

        public static int getChaosNumber(int itemId) {
            switch (itemId) {
                case 2049116:// 驚訝的混沌卷軸 60%
                    return 10;
                case 2049119:// 驚訝的混沌卷軸60%
                case 2049132:// 驚訝的混沌卷軸 30%
                case 2049133:// 驚訝的混沌卷軸 50%
                case 2049134:// 驚訝的混沌卷軸 70%
                    return 8;
                case 2049135:// 驚訝樂觀的混沌卷軸 20%
                case 2049136:// 驚訝樂觀的混沌卷軸 20%
                case 2049137:// 驚訝樂觀的混沌卷軸 40%
                    return 7;
                case 2049140:// 珠寶戒指的驚訝的混沌卷軸 40%
                case 2049142:// 驚訝的混沌卷軸 40%
                case 2049145:// 珠寶工藝驚訝的混沌卷軸 40%
                case 2049152:// 驚訝的混沌卷軸 60%
                case 2049153:// 驚訝樂觀的混沌卷軸
                case 2049156:// 驚訝的混沌卷軸 20%
                case 2049159:// 驚訝的混沌卷軸 50%
                case 2049165:// 驚訝的混沌卷軸 50%
                    return 6;
            }
            return 5;
        }

        public static int getSuccessTablet(final int scrollId, final int level) {
            switch (scrollId % 1000 / 100) {
                case 2:
                    // 2047_2_00 = armor, 2047_3_00 = accessory
                    switch (level) {
                        case 0:
                            return 70;
                        case 1:
                            return 55;
                        case 2:
                            return 43;
                        case 3:
                            return 33;
                        case 4:
                            return 26;
                        case 5:
                            return 20;
                        case 6:
                            return 16;
                        case 7:
                            return 12;
                        case 8:
                            return 10;
                        default:
                            return 7;
                    }
                case 3:
                    switch (level) {
                        case 0:
                            return 70;
                        case 1:
                            return 35;
                        case 2:
                            return 18;
                        case 3:
                            return 12;
                        default:
                            return 7;
                    }
                default:
                    switch (level) {
                        case 0:
                            return 70;
                        case 1:
                            return 50; // -20
                        case 2:
                            return 36; // -14
                        case 3:
                            return 26; // -10
                        case 4:
                            return 19; // -7
                        case 5:
                            return 14; // -5
                        case 6:
                            return 10; // -4
                        default:
                            return 7; // -3
                    }
            }
        }

        public static int getCurseTablet(final int scrollId, final int level) {
            switch (scrollId % 1000 / 100) {
                case 2:
                    // 2047_2_00 = armor, 2047_3_00 = accessory
                    switch (level) {
                        case 0:
                            return 10;
                        case 1:
                            return 12;
                        case 2:
                            return 16;
                        case 3:
                            return 20;
                        case 4:
                            return 26;
                        case 5:
                            return 33;
                        case 6:
                            return 43;
                        case 7:
                            return 55;
                        case 8:
                            return 70;
                        default:
                            return 100;
                    }
                case 3:
                    switch (level) {
                        case 0:
                            return 12;
                        case 1:
                            return 18;
                        case 2:
                            return 35;
                        case 3:
                            return 70;
                        default:
                            return 100;
                    }
                default:
                    switch (level) {
                        case 0:
                            return 10;
                        case 1:
                            return 14; // +4
                        case 2:
                            return 19; // +5
                        case 3:
                            return 26; // +7
                        case 4:
                            return 36; // +10
                        case 5:
                            return 50; // +14
                        case 6:
                            return 70; // +20
                        default:
                            return 100; // +30
                    }
            }
        }

        public static int getMaxEnhance(int itemId) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int level = ii.getReqLevel(itemId);
            boolean isSuperiorEquip = ii.isSuperiorEquip(itemId);
            int enhanceTimes;
            if (level < 95) {
                enhanceTimes = isSuperiorEquip ? 3 : 5;
            } else if (level < 110) {
                enhanceTimes = isSuperiorEquip ? 5 : 8;
            } else if (level < 120) {
                enhanceTimes = isSuperiorEquip ? 8 : 10;
            } else if (level < 130) {
                enhanceTimes = isSuperiorEquip ? 10 : 15;
            } else if (level < 140) {
                enhanceTimes = isSuperiorEquip ? 12 : 20;
            } else {
                enhanceTimes = isSuperiorEquip ? 15 : 25;
            }
            return enhanceTimes;
        }
    }

    public static class 方塊 {

        public static boolean canUseCube(final Equip equip, final int itemid) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            switch (itemid) {
                case 2711002: { // 泰米克方塊
                    return isZeroWeapon(equip.getItemId());
                }
                case 2711007: { // 10週年武器專用方塊
                    StructSetItem set = ii.getSetItem(410); // 十週年聖者組 [10週年]
                    return set != null && set.itemIDs.contains(equip.getItemId()) && 類型.武器(equip.getItemId());
                }
                case 5062100: { // 楓葉奇幻方塊(罕見)
                    StructSetItem set = ii.getSetItem(33); // 緋紅楓葉組 [7週年]
                    return set != null && set.itemIDs.contains(equip.getItemId()) && equip.getState(false) < 20;
                }
                case 5062102: { // [6週年]奇幻方塊
                    List<Integer> list = Arrays.asList(1462116, 1342039, 1402109, 1472139, 1332147, 1322105, 1442135, 1452128, 1312071, 1382123, 1492100, 1372099, 1432098, 1422072, 1302172, 1482101, 1412070);
                    return list.contains(equip.getItemId()) && 類型.武器(equip.getItemId());
                }
                case 5062103: { // 夢幻的神奇方塊
                    StructSetItem set = ii.getSetItem(100); // 十週年組 [8週年]
                    StructSetItem set2 = ii.getSetItem(103); // 十週年高級組 [8週年]
                    return set != null && set2 != null && set.itemIDs.contains(equip.getItemId()) && set2.itemIDs.contains(equip.getItemId());
                }
                case 2711000: // 可疑的方塊
                case 2711001: { // 奇怪的方塊
                    return equip.getState(false) < 18;
                }
                case 2710000: // 奇怪的方塊(罕見)
                case 2711005: // 工匠的方塊
                case 5062000: // 奇幻方塊
                case 5062004: { // 星星方塊
                    return equip.getState(false) < 20;
                }
                default: {
                    return true;
                }
            }
        }

        public enum CubeType {

            特殊(0x01),
            稀有(0x02),
            罕見(0x04),
            傳說(0x08),
            等級下降(0x10),
            調整潛能條數(0x20),
            洗後無法交易(0x40),
            對等(0x80),
            去掉無用潛能(0x100),
            前兩條相同(0x200),
            附加潛能(0x600),
            點商光環(0x800),
            ;

            private final int value;

            CubeType(final int value) {
                this.value = value;
            }

            public final int getValue() {
                return this.value;
            }

            public final boolean check(final int n) {
                return (n & this.value) == this.value;
            }
        }

        public static int getDefaultPotentialFlag(final int itemid) {
            int flag = CubeType.特殊.getValue() | CubeType.稀有.getValue() | CubeType.罕見.getValue() | CubeType.傳說.getValue();
            switch (itemid) {
                case 2711000: // 可疑的方塊
                case 2711001: // 奇怪的方塊
                case 2711009: { // 可疑的方塊
                    flag -= CubeType.罕見.getValue();
                    flag -= CubeType.傳說.getValue();
                    break;
                }
                case 2710000: {  // 可疑的方塊
                    flag -= CubeType.傳說.getValue();
                    flag |= CubeType.等級下降.getValue();
                    break;
                }
                case 2710001: { // 情誼方塊(洗後裝備不可交換)
                    flag -= CubeType.傳說.getValue();
                }
                case 3994895: { // 楓方塊
                    flag |= CubeType.洗後無法交易.getValue();
                    break;
                }
                case 2711005: // 工匠的方塊
                case 2711007: // 10週年武器專用方塊
                case 5062000: { // 奇幻方塊
                    flag -= CubeType.傳說.getValue();
                    break;
                }
                case 5062001: { // 超級奇幻方塊
                    flag -= CubeType.傳說.getValue();
                    flag |= CubeType.調整潛能條數.getValue();
                    break;
                }
                case 5062004: { // 星星方塊
                    flag -= CubeType.傳說.getValue();
                    flag |= CubeType.去掉無用潛能.getValue();
                    break;
                }
                case 5062013: { // 太陽方塊
                    flag |= CubeType.去掉無用潛能.getValue();
                }
                case 5062005: // 驚奇方塊
                case 5062006: // 白金奇幻方塊
                case 5062022: { // 新對等方塊
                    flag |= CubeType.對等.getValue();
                    break;
                }
                case 5062008: // 鏡射方塊
                case 5062019: { // 閃耀鏡射方塊
                    flag |= CubeType.前兩條相同.getValue();
                    break;
                }
                case 2730000: // 可疑
                case 2730001: // 可疑
                case 2730002: // 可疑
                case 2730003: // 可疑
                case 2730004: // 可疑
                case 2730005: // 可疑
                case 2730006: // 可疑
                case 5062500: // 大師附加奇幻方塊
                case 5062501: // [MS特價] 大師附加奇幻方塊
                case 5062502:
                case 5062503:{
                    flag |= CubeType.附加潛能.getValue();
                    break;
                }
            }
            if (MapleItemInformationProvider.getInstance().isCash(itemid)) {
                flag |= CubeType.點商光環.getValue();
            }
            return flag;
        }

        public static boolean potentialIDFits(final int potentialID, final int newstate, final int i) {
            // first line is always the best
            // but, sometimes it is possible to get second/third line as well
            // may seem like big chance, but it's not as it grabs random potential ID anyway
            switch (newstate) {
                case 20:
                    return (i == 1 || Randomizer.nextInt(20) == 0 ? potentialID >= 40000 : potentialID >= 30000 && potentialID < 60004); // xml say so
                case 19:
                    return (i == 1 || Randomizer.nextInt(20) == 0 ? potentialID >= 30000 && potentialID < 40000 : potentialID >= 20000 && potentialID < 30000);
                case 18:
                    return (i == 1 || Randomizer.nextInt(20) == 0 ? potentialID >= 20000 && potentialID < 30000 : potentialID >= 10000 && potentialID < 20000);
                case 17:
                    return (i == 1 || Randomizer.nextInt(20) == 0 ? potentialID >= 10000 && potentialID < 20000 : potentialID < 10000);
                default:
                    return false;
            }
        }

        public static boolean optionTypeFits(final int optionType, final int itemId) {
            switch (optionType) {
                case 10: // 武器、盾牌、副手和能源
                    return 類型.武器(itemId) || 類型.副手(itemId) || 類型.能源(itemId);
                case 11: // 除了武器的全部裝備
                    return !類型.武器(itemId);
                case 20: // 除了配飾和武器的全部裝備
                    return !類型.飾品(itemId) && !類型.武器(itemId);
                case 40: // 配飾
                    return 類型.飾品(itemId);
                case 51: // 帽子
                    return 類型.帽子(itemId);
                case 52: // 披風
                    return 類型.披風(itemId);
                case 53: // 上衣、褲子與套服
                    return 類型.上衣(itemId) || 類型.套服(itemId) || 類型.褲裙(itemId);
                case 54: // 手套
                    return 類型.手套(itemId);
                case 55: // 鞋子
                    return 類型.鞋子(itemId);
                default:
                    return true;
            }
        }

        public static boolean isAllowedPotentialStat(Equip eqp, int opID, boolean bonus, boolean cash) { // For now
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            boolean superPot = false;
            // 判斷潛能是主潛還是附潛
            int type = opID / 1000 % 10;
            if ((bonus && ((!superPot && type != 2) || (superPot && type >= 1))) || (!bonus && type == 2)) {
                return false;
            }
            // 點商光環清除罕見以上潛能的非常的垃圾純數字潛能
            if ((opID % 1000 <= 14 || opID % 1000 == 81) && type != 1 && opID < 60000 && cash) {
                return false;
            }

            int state = opID % 1000;
            return superPot && !bonus ? (state != 4 && state != 9 && state != 24 && (state < 13 || state > 18)) : opID < 60000;
        }

        public static int[] getCubeRankUpRate(final int itemid) {
            switch (itemid) {
                case 5062005: // 驚奇方塊
                case 5062006: // 白金奇幻方塊
                case 5062008: // 鏡射方塊
                case 5062009: // 紅色方塊
                case 5062017: // 閃耀方塊
                case 5062019: // 閃耀鏡射方塊
                case 5062020: // 閃炫方塊
                case 5062021: // 新對等方塊
                case 5062026: // 結合方塊
                case 5062500: // 附加方塊
                case 5062503: { // 白色附加方塊
                    return new int[]{60000, 18000, 3000};
                }
                case 5062010: // 黑色方塊
                case 5062013: { // 太陽方塊
                    return new int[]{150000, 35000, 10000};
                }
                default: {
                    return new int[]{47619, 19608, 4975};
                }
            }
        }

        public static int getCubeDebris(final int itemid) {
            switch (itemid) {
                case 5062000:
                case 5062001: // 超級奇幻方塊
                case 5062100: // 楓葉奇幻方塊
                case 5062102: // [7週年]神奇方塊
                case 5062103: { // 奇異奇幻方塊
                    return 2430112;
                }
                case 5062002: // 傳說方塊
                case 5062022: {
                    return 2430481;
                }
                case 5062004: { // 星星方塊
                    return 2432114;
                }
                case 5062005: { // 驚奇方塊
                    return 2430759;
                }
                case 5062006: { // 白金奇幻方塊
                    return 2431427;
                }
                case 5062009: { // 紅色方塊
                    return 2431893;
                }
                case 5062010: { // 黑色方塊
                    return 2431894;
                }
                case 5062013: { // 太陽方塊
                    return 2432115;
                }
                case 5062090: { // 記憶方塊
                    return 2431445;
                }
                case 5062500: // 大師附加奇幻方塊
                case 5062501: { // [MS特價] 大師附加奇幻方塊
                    return 2430915;
                }
                case 5062024: {
                    return 2434125;
                }
                case 5062502: {
                    return 2433547;
                }
                case 5062503: { // 白色附加方塊
                    return 2434782;
                }
                default: {
                    return 0;
                }
            }
        }

        public static boolean canLockCube(int itemId) {
            switch (itemId) {
                case 5062000:// 奇幻方塊
                case 5062004:// 星星方塊
                case 5062006:// 白金奇幻方塊
                case 5062013:// 太陽方塊
                    return true;
                default:
                    return false;
            }
        }

        public static long getMapleCubeCost(int times, int potentialState) {
            potentialState -= 1;
            if (potentialState < 0) {
                return 100;
            }
            long cost = 0;
            long[] mapleCubeCostPlus = {100, 10000, 500000, 20000000};
            long[] mapleCubeCostInitial = {100, 100000, 1000000, 10000000};
            long[] mapleCubeCostMax = {15000, 47400000, 5113000000L, 9999999999L};
            if (times >= 50) {
                cost = mapleCubeCostMax[potentialState];
            } else {
                for (int i = 1; i <= times; i++) {
                    long plus = 1;
                    for (int j = 0; j < i / (potentialState == 0 ? 10 : 5); j++) {
                        switch (potentialState) {
                            case 0:
                                plus += 1;
                                break;
                            case 1:
                                plus *= 2;
                                break;
                            case 2:
                                plus *= 2 + (j == 0 ? 2 : 0);
                                break;
                            case 3:
                                plus *= 2 + (j == 3 ? 1 : 0);
                                break;
                            default:
                                break;
                        }
                    }
                    cost += mapleCubeCostPlus[potentialState] * plus;
                }
            }
            cost += mapleCubeCostInitial[potentialState];
            cost = cost > mapleCubeCostMax[potentialState] ? mapleCubeCostMax[potentialState] : cost;
            return cost;
        }

        public static long getCubeNeedMeso(Equip equip) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int reqLevel = ii.getReqLevel(equip.getItemId());
            float nConstant = 0;
            if (reqLevel > 120) {
                nConstant = 20;
            } else if (reqLevel > 70) {
                nConstant = 2.5F;
            } else if (reqLevel > 30) {
                nConstant = 0.5F;
            }
            return Math.max((int) Math.ceil(Math.pow(reqLevel, 2) * nConstant), 0);
        }

        public static boolean isUselessPotential(final StructItemOption itemOption) {
            boolean useless = false;
            for (final String s : itemOption.data.keySet()) {
                if (itemOption.get(s) > 0) {
                    switch (s) {
                        case "incSTRr":
                        case "incDEXr":
                        case "incINTr":
                        case "incLUKr":
                        case "incMHPr":
                        case "incPADr":
                        case "incMADr":
                        case "incCriticaldamageMin":
                        case "incCriticaldamageMax":
                        case "incDAMr":
                        case "incTerR":
                        case "incAsrR":
                        case "ignoreTargetDEF":
                        case "incMaxDamage":
                        case "reduceCooltime":
                        case "boss":
                        case "incMesoProp":
                        case "incRewardProp":
                        case "level":
                        case "attackType":
                            break;
                        default: {
                            useless = true;
                            break;
                        }
                    }
                }
            }
            return useless;
        }
    }

    public static class 類型 {

        // <editor-fold defaultstate="collapsed" desc="道具一級分類">
        public static boolean 帽子(int itemid) {
            return itemid / 10000 == 100;
        }

        public static boolean 臉飾(int itemid) {
            return itemid / 10000 == 101;
        }

        public static boolean 眼飾(int itemid) {
            return itemid / 10000 == 102;
        }

        public static boolean 耳環(int itemid) {
            return itemid / 10000 == 103;
        }

        public static boolean 上衣(int itemid) {
            return itemid / 10000 == 104;
        }

        public static boolean 套服(int itemId) {
            return itemId / 10000 == 105;
        }

        public static boolean 褲裙(int itemid) {
            return itemid / 10000 == 106;
        }

        public static boolean 鞋子(int itemid) {
            return itemid / 10000 == 107;
        }

        public static boolean 手套(int itemid) {
            return itemid / 10000 == 108;
        }

        public static boolean 盾牌(int itemid) {
            return itemid / 10000 == 109;
        }

        public static boolean 披風(int itemid) {
            return itemid / 10000 == 110;
        }

        public static boolean 戒指(int itemid) {
            return itemid / 10000 == 111;
        }

        public static boolean 墜飾(int itemid) {
            return itemid / 10000 == 112;
        }

        public static boolean 腰帶(int itemid) {
            return itemid / 10000 == 113;
        }

        public static boolean 勳章(int itemid) {
            return itemid / 10000 == 114;
        }

        public static boolean 肩飾(int itemid) {
            return itemid / 10000 == 115;
        }

        public static boolean 口袋道具(int itemid) {
            return itemid / 10000 == 116;
        }

        public static boolean 胸章(int itemId) {
            return itemId / 10000 == 118;
        }

        public static boolean 能源(final int itemid) {
            return itemid / 10000 == 119;
        }

        public static boolean 圖騰(final int itemid) {
            return itemid / 10000 == 120;
        }

        public static boolean 鏟(final int itemid) {
            return itemid / 10000 == 150;
        }

        public static boolean 十字鎬(final int itemid) {
            return itemid / 10000 == 151;
        }

        public static boolean 引擎(int itemid) {
            return itemid / 10000 == 161;
        }

        public static boolean 手臂(int itemid) {
            return itemid / 10000 == 162;
        }

        public static boolean 腿(int itemid) {
            return itemid / 10000 == 163;
        }

        public static boolean 機殼(int itemid) {
            return itemid / 10000 == 164;
        }

        public static boolean 晶體管(int itemid) {
            return itemid / 10000 == 165;
        }

        public static boolean 機器人(int itemid) {
            return itemid / 10000 == 166;
        }

        public static boolean 心臟(int itemId) {
            return itemId / 10000 == 167;
        }

        public static boolean 拼圖(int itemId) {
            return itemId / 10000 == 168;
        }

        public static boolean 秘法符文(int itemId) {
            return itemId / 1000 == 1712;
        }

        public static boolean 真實符文(int itemId) {
            return itemId / 1000 == 1713;
        }

        public static boolean 寵物裝備(int itemid) {
            return itemid / 10000 >= 180 && itemid / 10000 <= 183;
        }

        public static boolean 騎寵(int itemid) {
            return itemid / 10000 == 190 || itemid / 10000 == 193;
        }

        public static boolean 馬鞍(int itemid) {
            return itemid / 10000 == 191;
        }

        public static boolean 龍面具(int itemid) {
            return itemid / 10000 == 194;
        }

        public static boolean 龍墜飾(int itemid) {
            return itemid / 10000 == 195;
        }

        public static boolean 龍之翼(int itemid) {
            return itemid / 10000 == 196;
        }

        public static boolean 龍尾巴(int itemid) {
            return itemid / 10000 == 197;
        }

        public static boolean 弓矢(int itemid) {
            return itemid / 1000 == 2060;
        }

        public static boolean 弩矢(int itemid) {
            return itemid / 1000 == 2061;
        }

        public static boolean 飛鏢(int itemid) {
            return itemid / 10000 == 207;
        }

        public static boolean 子彈(int itemid) {
            return itemid / 10000 == 233;
        }

        public static boolean 萌獸卡(int itemid) {
            return itemid / 10000 == 284;
        }

        public static boolean 寵物(int id) {
            return id / 10000 == 500;
        }

        // </editor-fold>
        public static boolean 防具(int itemid) {
            return 帽子(itemid) || 上衣(itemid) || 套服(itemid) || 褲裙(itemid) || 鞋子(itemid) || 手套(itemid) || 披風(itemid);
        }

        public static boolean 飾品(int itemid) {
            return 臉飾(itemid) || 眼飾(itemid) || 耳環(itemid) || 戒指(itemid) || 墜飾(itemid) || 腰帶(itemid) || 勳章(itemid) || 肩飾(itemid) || 口袋道具(itemid) || 胸章(itemid) || 能源(itemid) || 圖騰(itemid);
        }

        public static boolean 副手(int itemid) {
            return 盾牌(itemid) || MapleWeapon.雙刀.check(itemid) || 特殊副手(itemid);
        }

        public static boolean 武器(int itemid) {
            MapleWeapon type = MapleWeapon.getByItemID(itemid);
            return type != MapleWeapon.沒有武器;
        }

        public static boolean 特殊副手(int itemid) {
            return itemid / 10000 == 135;
        }

        public static boolean 機械(final int itemid) {
            return 引擎(itemid) || 手臂(itemid) || 腿(itemid) || 機殼(itemid) || 晶體管(itemid);
        }

        public static boolean 龍裝備(final int itemid) {
            return 龍面具(itemid) || 龍墜飾(itemid) || 龍之翼(itemid) || 龍尾巴(itemid);
        }

        public static boolean 可充值道具(int itemid) {
            return 飛鏢(itemid) || 子彈(itemid);
        }

        public static boolean 採集道具(int itemid) {
            return 鏟(itemid) || 十字鎬(itemid);
        }

        public static boolean 單手武器(int itemid) {
            return 武器(itemid) && !雙手武器(itemid);
        }

        public static boolean 雙手武器(final int itemid) {
            return 雙手武器(itemid, 0);
        }

        public static boolean 雙手武器(final int itemid, final int job) {
            if (MapleWeapon.火槍.check(itemid)) {
                return !(job >= 570 && job <= 572);
            }
            if (MapleWeapon.雙手劍.check(itemid)) {
                return !(job >= 6100 && job <= 6112);
            }
            return 武器(itemid) && MapleWeapon.getByItemID(itemid).isTwoHand();
        }

        public static boolean 物理武器(int itemid) {
            return 武器(itemid) && MapleWeapon.getByItemID(itemid).getAttribute() != MapleWeapon.Attribute.Magic;
        }

        public static boolean 魔法武器(int itemid) {
            return 武器(itemid) && MapleWeapon.getByItemID(itemid).getAttribute() == MapleWeapon.Attribute.Magic;
        }

        public static boolean 騎寵道具(int itemid) {
            return 騎寵(itemid) || 馬鞍(itemid);
        }

        public static int getGender(int itemid) {
            int ret = itemid / 1000 % 10;
            if (膚色(itemid)) {
                return 2;
            } else if (臉型(itemid)) {
                if (String.valueOf(itemid).length() == 8) {
                    itemid /= 1000;
                    ret = itemid / 1000 % 10;
                }
                switch (ret) {
                    case 0:
                    case 3:
                    case 5:
                    case 7:
                        return 0;
                    case 1:
                    case 4:
                    case 6:
                    case 8:
                        return 1;
                    case 2:
                    case 9:
                    default:
                        return 2;
                }
            } else if (髮型(itemid)) {
                if (String.valueOf(itemid).length() == 8) {
                    itemid /= 1000;
                    ret = itemid / 1000 % 10;
                }
                switch (ret) {
                    case 0:
                    case 3:
                    case 5:
                    case 6:
                        return 0;
                    case 1:
                    case 4:
                    case 7:
                    case 8:
                        return 1;
                    case 2:
                    case 9:
                    default:
                        return 2;
                }
            }
            return ret;
        }

        public static boolean 裝備(int itemid) {
            return (itemid / 10000 >= 100) && (itemid / 10000 < 200);
        }

        public static boolean 消耗(int itemid) {
            return (itemid / 10000 >= 200) && (itemid / 10000 < 300);
        }

        public static boolean 裝飾(int itemid) {
            return (itemid / 10000 >= 300) && (itemid / 10000 < 400);
        }

        public static boolean 其他(int itemid) {
            return (itemid / 10000 >= 400) && (itemid / 10000 < 500);
        }

        public static boolean 特殊(int itemid) {
            return itemid / 10000 >= 500;
        }

        public static boolean 友情裝備(int itemid) {
            switch (itemid) {
                case 1112800: // 幸運草友情戒
                case 1112801: // 小白花友情戒
                case 1112802: // 星星友情戒
                case 1112810: // 聖誕祝福戒指
                case 1112811: // 聖誕派對戒指
                case 1112812: // 我的麻吉好友
                case 1112817: // 蝴蝶友情經驗值戒指 30天
                case 1112822: // 重力戒指

                case 1049000: // 友情套頭T恤
                    return true;
            }
            return false;
        }

        public static boolean 戀人裝備(int itemid) {
            switch (itemid) {
                case 1112001: // 戀人戒指
                case 1112002: // 紅心雲情侶戒
                case 1112003: // 邱比特戒指
                case 1112005: // 桃心銀戒
                case 1112006: // 女王銀戒
                case 1112007: // 槲寄生戒指
                case 1112012: // 純真玫瑰戒指
                case 1112013: // 愛情紅線戒指
                case 1112014:
                case 1112015: // 白金戒指
                case 1112016: // 雪花戒指
                case 1112816: // 當我們愛在一起戒指
                case 1112820: // 龍鳳戒指

                case 1048000: // 情人套頭T
                case 1048001:
                case 1048002:
                    return true;
            }
            return false;
        }

        public static boolean 結婚戒指(int itemid) {
            switch (itemid) {
                case 1112300: // 月光1克拉結婚戒指
                case 1112301: // 月光2克拉結婚戒指
                case 1112302: // 月光3克拉結婚戒指
                case 1112303: // 星光1克拉結婚戒指
                case 1112304: // 星光2克拉結婚戒指
                case 1112305: // 星光3克拉結婚戒指
                case 1112306: // 金心1克拉結婚戒指
                case 1112307: // 金心2克拉結婚戒指
                case 1112308: // 金心3克拉結婚戒指
                case 1112309: // 銀鑽1克拉結婚戒指
                case 1112310: // 銀鑽2克拉結婚戒指
                case 1112311: // 銀鑽3克拉結婚戒指
                case 1112313: // 銀婚式一般戒
                case 1112314: // 銀婚式高級戒
                case 1112315: // 肉麻夫婦的結婚戒指1克拉
                case 1112316: // 肉麻夫婦的結婚戒指2克拉
                case 1112317: // 肉麻夫婦的結婚戒指3克拉
                case 1112318: // 鴛鴦夫婦的結婚戒指1克拉
                case 1112319: // 鴛鴦夫婦的結婚戒指2克拉
                case 1112320: // 鴛鴦夫婦的結婚戒指3克拉
                case 1112321: // 愛情粉紅戒 - 教堂結婚的情侶可裝備的結婚戒指。離婚時愛情粉紅戒消失。
                case 1112804: // 結婚戒指 - 結完婚時系統將自動配發一隻結婚戒指，角色必須擁有結婚戒指才能申請離婚
                    return true;
            }
            return false;
        }

        public static boolean 特效裝備(int itemid) {
            return 友情裝備(itemid) || 戀人裝備(itemid) || 結婚戒指(itemid);
        }

        public static boolean 管理員裝備(final int itemid) {
            switch (itemid) {
                case 1002140:// 維澤特帽
                case 1003142:// 維澤特帽
                case 1003274:// 維澤特帽
                case 1042003:// 維澤特西裝
                case 1042223:// 維澤特西裝
                case 1062007:// 維澤特西褲
                case 1062140:// 維澤特西褲
                case 1322013:// 維澤特手提包
                case 1322106:// 維澤特手提包
                case 1002959:
                    return true;
            }
            return false;
        }

        public static boolean 城鎮傳送卷軸(final int itemid) {
            return itemid >= 2030000 && itemid < 2040000;
        }

        public static boolean 普通升級卷軸(int itemid) {
            return itemid >= 2040000 && itemid <= 2048100 && !特殊卷軸(itemid);
        }

        public static boolean 阿斯旺卷軸(int itemid) {
            // return
            // MapleItemInformationProvider.getInstance().getEquipStats(scroll.getItemId()).containsKey("tuc");
            // should add this ^ too.
            return itemid >= 2046060 && itemid <= 2046069 || itemid >= 2046141 && itemid <= 2046145 || itemid >= 2046519 && itemid <= 2046530 || itemid >= 2046701 && itemid <= 2046712;
        }

        public static boolean 提升卷(int itemid) { // 龍騎士獲得的強化牌板
            return itemid >= 2047000 && itemid < 2047310;
        }

        public static boolean 輪迴星火(int id) {
            return id / 100 == 20487 || id / 100 == 50645;
        }

        public static boolean 暗黑輪迴星火(int id) {
            return id == 2048768 || id == 2048786 || id == 2048767 || id == 2048766 || id == 2048755 || id == 2048753 || id == 5064501;
        }

        public static boolean 白衣卷軸(int itemid) {
            return itemid / 100 == 20490;
        }

        public static boolean 混沌卷軸(int itemid) {
            if (itemid >= 2049105 && itemid <= 2049110) {
                return false;
            }
            return itemid / 100 == 20491 || itemid == 2040126;
        }

        public static boolean 樂觀混沌卷軸(int itemid) {
            if (!混沌卷軸(itemid)) {
                return false;
            }
            switch (itemid) {
                case 2049122:// 樂觀的混卷軸50%
                case 2049129:// 樂觀的混卷軸 50%
                case 2049130:// 樂觀的混卷軸 30%
                case 2049131:// 樂觀的混卷軸 20%
                case 2049135:// 驚訝樂觀的混卷軸 20%
                case 2049136:// 驚訝樂觀的混卷軸 20%
                case 2049137:// 驚訝樂觀的混卷軸 40%
                case 2049141:// 珠寶戒指的樂觀的混卷軸 30%
                case 2049155:// 珠寶工藝樂觀的混卷軸 30%
                case 2049153:// 驚訝樂觀的混卷軸
                    return true;
            }
            return false;
        }

        public static boolean 飾品卷軸(int itemid) {
            return itemid / 100 == 20492;
        }

        public static boolean 裝備強化卷軸(int itemid) {
            return itemid / 100 == 20493 || itemid / 100 == 26440;
        }

        public static boolean 黃金鐵鎚(int itemid) {
            return itemid / 1000 == 2470;
        }

        public static boolean 白金鎚子(int itemid) {
            return itemid / 1000 == 2472;
        }

        public static boolean 潛能卷軸(int itemid) {
            return (itemid / 100 == 20494 || itemid / 100 == 20497 || itemid == 5534000) && !附加潛能卷軸(itemid);
        }

        public static boolean 附加潛能卷軸(int itemid) {
            return (itemid / 100 == 20483 && !附加潛能印章(itemid)) || itemid / 10 == 204973;
        }

        public static boolean 卡勒瑪附加傳說潛在能力賦予卷軸100(int itemid) {
            return itemid / 100 == 26441;
        }


        public static boolean 潛能印章(int itemid) {
            return itemid / 100 == 20495;
        }

        public static boolean 附加潛能印章(int itemId) {
            return (itemId >= 2048200 && itemId <= 2048304) || itemId == 2048336;
        }

        public static boolean 回真卷軸(int itemid) {
            switch (itemid) {
                case 5064200:// 完美回真卡
                case 5064201:// 星光回真卷軸
                    return true;
                default:
                    return itemid / 100 == 20496;
            }
        }

        public static boolean 幸運日卷軸(int itemid) {
            switch (itemid) {
                case 5063100:// 幸運保護券(防爆+幸運)
                case 5068000:// 寵物專用幸運日卷軸
                    return true;
                default:
                    return itemid / 1000 == 2530;
            }
        }

        public static boolean 保護卷軸(int itemid) {
            switch (itemid) {
                case 5063100:// 幸運保護券(防爆+幸運)
                case 5064000:// 裝備保護卷軸(無法用於尊貴或12星以上)
                case 5064002:// 星光裝備保護卷軸(105以下的裝備且無法用於尊貴或12星以上)
                case 5064003:// 尊貴裝備保護卷軸(無法用於非尊貴以及尊貴7星以上)
                case 5064004:// [MS特價] 裝備保護卷軸(無法用於尊貴或12星以上)
                    return true;
                default:
                    return itemid / 1000 == 2531;
            }
        }

        public static boolean 安全卷軸(int itemid) {
            switch (itemid) {
                case 5064100:// 安全盾牌卷軸
                case 5064101:// 星光安全盾牌卷軸(105以下的裝備)
                case 5068100:// 寵物安全盾牌卷軸
                    return true;
                default:
                    return itemid / 1000 == 2532;
            }
        }

        public static boolean 卷軸保護卡(int itemid) {
            switch (itemid) {
                case 5064300:// 卷軸保護卡
                case 5064301:// 星光卷軸保護卡(105以下的裝備)
                case 5068200:// 寵物卷軸保護卡
                    return true;
            }
            return false;
        }

        public static boolean 恢復卡(int itemid) {
            // 恢復卡
            return itemid == 5064400;
        }

        public static boolean 靈魂卷軸_附魔器(int itemid) {
            return itemid / 1000 == 2590;
        }

        public static boolean 靈魂寶珠(int itemid) {
            return itemid / 1000 == 2591;
        }

        public static boolean 武器攻擊力卷軸(final int itemid) {
            return itemid / 100 == 20478 || itemid / 100 == 20469 || itemid / 100 == 20479;
        }

        public static boolean 海外服特殊卷軸(int itemid) {
            return itemid / 10000 == 261 || itemid / 1000 == 2046;
        }

        public static boolean 特殊卷軸(final int itemid) {
            switch (itemid) {
                case 2040727: // 鞋子防滑卷軸
                case 2041058: // 披風防寒卷軸
                case 2610200: // 寵物裝備透明藥水
                    return true;
                default:
                    return 幸運日卷軸(itemid) || 保護卷軸(itemid) || 安全卷軸(itemid) || 卷軸保護卡(itemid) || 恢復卡(itemid);
            }
        }

        public static boolean 真楓葉之心(int itemId) {
            switch (itemId) {
                case 1122122: //真．楓葉之心(劍士用)
                case 1122123: //真．楓葉之心(法師用)
                case 1122124: //真．楓葉之心(弓箭手用)
                case 1122125: //真．楓葉之心(盜賊用)
                case 1122126: //真．楓葉之心(海盜用)
                    return true;
            }
            return false;
        }

        public static boolean 特殊潛能道具(final int itemid) {
            if (itemid / 100 == 10121 && itemid % 100 >= 64 && itemid % 100 <= 74 && itemid % 100 != 65 && itemid % 100 != 66) {//恰吉
                return true;
            } else if (itemid / 10 == 112212 && (itemid % 10 >= 2 && itemid % 10 <= 6)) {// 真. 楓葉之心
                return true;
            } else // 卡爾頓的鬍子
                if (itemid >= 1122224 && itemid <= 1122245) {// 心之項鍊
                    return true;
                } else return itemid / 10 == 101244;
        }

        public static boolean 強化寶石(int id) {
            return id >= 4250000 && id <= 4251402;
        }

        public static boolean 無法潛能道具(final int itemid) {
            return false;
        }

        public static boolean 膚色(final int itemid) {
            return itemid / 1000 == 2 || itemid / 1000 == 12 || itemid < 2000;
        }

        public static boolean 臉型(int itemid) {
            if (String.valueOf(itemid).length() == 8) {
                itemid /= 1000;
            }
            return itemid / 10000 == 2 || itemid / 10000 == 5;
        }

        public static boolean 髮型(int itemid) {
            if (String.valueOf(itemid).length() == 8) {
                itemid /= 1000;
            }
            return itemid / 10000 == 3 || itemid / 10000 == 4 || itemid / 10000 == 6;
        }

        public static boolean 男臉型(int id) {
            return 臉型(id) && getGender(id) == 0;
        }

        public static boolean 女臉型(int id) {
            return 臉型(id) && getGender(id) == 1;
        }

        public static boolean 通用臉型(int id) {
            return 臉型(id) && getGender(id) == 2;
        }

        public static boolean 男髮型(int id) {
            return 髮型(id) && getGender(id) == 0;
        }

        public static boolean 女髮型(int id) {
            return 髮型(id) && getGender(id) == 1;
        }

        public static boolean 通用髮型(int id) {
            return 髮型(id) && getGender(id) == 2;
        }
    }

    public static int getStyleBaseID(int styleID) {
        if (類型.膚色(styleID)) {
            return styleID % 100;
        } else if (類型.髮型(styleID)) {
            if (String.valueOf(styleID).length() == 8)
                styleID /= 1000;
            return (styleID / 10) * 10;
        } else if (類型.臉型(styleID)) {
            if (String.valueOf(styleID).length() == 8)
                styleID /= 1000;
            return (styleID / 1000) * 1000 + (styleID % 100);
        } else {
            return styleID;
        }
    }

    public static int getStyleColorID(int styleID) {
        if (類型.髮型(styleID)) {
            if (String.valueOf(styleID).length() == 8) {
                styleID /= 1000;
            }
            return styleID % 10;
        } else if (類型.臉型(styleID)) {
            if (String.valueOf(styleID).length() == 8) {
                styleID /= 1000;
            }
            return (styleID % 1000) / 100 * 100;
        } else {
            return styleID;
        }
    }

    public static int getStyleMixColorID(int styleID) {
        if (類型.髮型(styleID)) {
            return styleID % (String.valueOf(styleID).length() == 8 ? 10000 : 10);
        } else if (類型.臉型(styleID)) {
            int mix = 0;
            if (String.valueOf(styleID).length() == 8) {
                mix = styleID % 1000;
                styleID /= 1000;
            }
            styleID = (styleID % 1000) / 100 * 100;
            if (mix > 99) styleID = styleID * 1000 + mix;
            return styleID;
        } else {
            return styleID;
        }
    }

    public static int changeStyleID(int src, int styleID) {
        if (類型.膚色(styleID)) return styleID;
        int color = getStyleMixColorID(src);
        styleID = getStyleBaseID(styleID);
        if (color > 99) styleID *= 1000;
        return styleID + color;
    }

    public static int getFamiliarByItemID(int itemId) {
        return MapleItemInformationProvider.getInstance().getFamiliarID(itemId);
    }

    public static MapleInventoryType getInventoryType(int itemId) {
        return getInventoryType(itemId, true);
    }

    public static MapleInventoryType getInventoryType(int itemId, boolean trueType) {
        byte type = (byte) (itemId / 1000000);
        if ((type < 1 || type > 5) && type != 9 && type != 36) {
            return MapleInventoryType.UNDEFINED;
        }
        MapleInventoryType iType = MapleInventoryType.getByType(type);
        if (trueType && MapleInventoryType.EQUIP == iType && MapleItemInformationProvider.getInstance().isCash(itemId)) {
            iType = MapleInventoryType.DECORATION;
        }
        return iType;
    }

    public static boolean isDamageSkinItem(int itemId) {
        return MapleItemInformationProvider.getInstance().getDamageSkinBox().containsKey(itemId);
    }

    public static boolean isOtherGem(int id) {
        switch (id) {
            case 4001174: //練習用鞋子
            case 4001175: //兒童鞋
            case 4001176: //作業用鏟子
            case 4001177: //白色棉T恤
            case 4001178: //沙灘鞋
            case 4001179: //訓練用光線槍
            case 4001180: //外出用手套
            case 4001181: //連指手套
            case 4001182: //清掃用拖把
            case 4001183: //修煉服
            case 4001184: //結實的耙子
            case 4001185: //溫暖的皮靴
            case 4001186: //王的頭巾
            case 4031980: //黃金砧子
            case 2041058: //披風防寒卷軸
            case 2040727: //鞋子防滑卷軸
            case 1032062: //元素耳環
            case 4032334: //狼的生命水
            case 4032312: //紅珠玉
            case 1142156: //龍神
            case 1142157: //傳說中的龍神
                return true; //mostly quest items
        }
        return false;
    }

    public static boolean isNoticeItem(int itemId) {

        switch (itemId) {
            case 1012438:// Lv160:// 漩渦文身(無描述)
            case 1022211:// Lv160:// 漩渦眼鏡(無描述)
            case 1032224:// Lv160:// 漩渦耳環(無描述)
            case 1122269:// Lv160:// 漩渦吊墜(無描述)
            case 1132247:// Lv160:// 漩渦腰帶(無描述)
            case 1152160:// Lv160:// 漩渦護肩(無描述)
            case 1003976:// Lv160:// 漩渦帽子(無描述)
            case 1102623:// Lv160:// 漩渦披風(無描述)
            case 1082556:// Lv160:// 漩渦手套(無描述)
            case 1052669:// Lv160:// 漩渦皇家外套(無描述)
            case 1072870:// Lv160:// 漩渦鞋(無描述)
            case 1212089:// Lv160:// 漩渦雙頭杖(無描述)
            case 1222084:// Lv160:// 漩渦靈魂手銃(無描述)
            case 1232084:// Lv160:// 漩渦惡魔劍(無描述)
            case 1242090:// Lv160:// 漩渦鎖鏈劍(無描述)
            case 1302297:// Lv160:// 漩渦劍(無描述)
            case 1312173:// Lv160:// 漩渦斧(無描述)
            case 1322223:// Lv160:// 漩渦錘(無描述)
            case 1332247:// Lv160:// 漩渦匕首(無描述)
            case 1342090:// Lv160:// 漩渦刀(無描述)
            case 1362109:// Lv160:// 漩渦手杖(無描述)
            case 1372195:// Lv160:// 漩渦短杖(無描述)
            case 1382231:// Lv160:// 漩渦長杖(無描述)
            case 1402220:// Lv160:// 漩渦雙手劍(無描述)
            case 1412152:// Lv160:// 漩渦雙手戰斧(無描述)
            case 1422158:// Lv160:// 漩渦巨錘(無描述)
            case 1432187:// Lv160:// 漩渦矛(無描述)
            case 1442242:// Lv160:// 漩渦戟(無描述)
            case 1452226:// Lv160:// 漩渦弓(無描述)
            case 1462213:// Lv160:// 漩渦弩(無描述)
            case 1472235:// Lv160:// 漩渦拳甲(無描述)
            case 1482189:// Lv160:// 漩渦衝拳(無描述)
            case 1492199:// Lv160:// 漩渦手銃(無描述)
            case 1522113:// Lv160:// 漩渦雙翼短杖(無描述)
            case 1532118:// Lv160:// 漩渦手炮(無描述)
            case 1252033:// Lv160:// 漩渦虎梳魔法棒(無描述)
            case 1312065:// Lv140:// 獅心勇士斧(無描述)
            case 1322096:// Lv140:// 獅心震雷釘(無描述)
            case 1402095:// Lv140:// 獅心戰鬥彎刀(無描述)
            case 1412065:// Lv140:// 獅心戰鬥斧(無描述)
            case 1422066:// Lv140:// 獅心巨錘(無描述)
            case 1432086:// Lv140:// 獅心長槍(無描述)
            case 1442116:// Lv140:// 獅心矛(無描述)
            case 1232014:// Lv140:// 獅心痛苦命運(無描述)
            case 1302152:// Lv140:// 獅心彎刀(無描述)
            case 1212014:// Lv140:// 龍尾黑甲凶靈(無描述)
            case 1372084:// Lv140:// 龍尾精靈短杖(無描述)
            case 1382104:// Lv140:// 龍尾戰鬥長杖(無描述)
            case 1452111:// Lv140:// 鷹翼組合弓(無描述)
            case 1462099:// Lv140:// 鷹翼重弩(無描述)
            case 1522018:// Lv140:// 龍翼巨弩槍(無描述)
            case 1242042:// Lv140:// 渡鴉之魂女王意志之劍(無描述)
            case 1332130:// Lv140:// 渡鴉之魂短刀(無描述)
            case 1222014:// Lv140:// 鯊齒靈魂吸取者(無描述)
            case 1242014:// Lv140:// 鯊齒女王意志之劍(無描述)
            case 1482084:// Lv140:// 鯊齒巨鷹爪(無描述)
            case 1492085:// Lv140:// 鯊齒銳利手銃(無描述)
            case 1532018:// Lv140:// 鯊齒火焰炮(無描述)
            case 1152108:// Lv140:// 獅心戰鬥護肩(無描述)
            case 1152110:// Lv140:// 龍尾法師護肩(無描述)
            case 1152111:// Lv140:// 鷹翼哨兵護肩(無描述)
            case 1152112:// Lv140:// 渡鴉之魂獵人護肩(無描述)
            case 1152113:// Lv140:// 鯊齒船長護肩(無描述)
            case 1003172:// Lv140:// 獅心戰鬥頭盔(無描述)
            case 1003173:// Lv140:// 龍尾法師帽子(無描述)
            case 1003174:// Lv140:// 鷹翼哨兵便帽(無描述)
            case 1003175:// Lv140:// 渡鴉之魂追蹤者帽(無描述)
            case 1003176:// Lv140:// 鯊齒船長帽(無描述)
            case 1102275:// Lv140:// 獅心戰鬥披風(無描述)
            case 1102276:// Lv140:// 龍尾法師披風(無描述)
            case 1102277:// Lv140:// 鷹翼哨兵披風(無描述)
            case 1102278:// Lv140:// 渡鴉之魂獵人披風(無描述)
            case 1102279:// Lv140:// 鯊齒船長披風(無描述)
            case 1082295:// Lv140:// 獅心戰鬥護腕(無描述)
            case 1082296:// Lv140:// 龍尾法師手套(無描述)
            case 1082297:// Lv140:// 鷹翼哨兵手套(無描述)
            case 1082298:// Lv140:// 渡鴉之魂追蹤者手套(無描述)
            case 1082299:// Lv140:// 鯊齒船長手套(無描述)
            case 1052314:// Lv140:// 獅心戰鬥鎖子甲(無描述)
            case 1052315:// Lv140:// 龍尾法師長袍(無描述)
            case 1052316:// Lv140:// 鷹翼哨兵服(無描述)
            case 1052317:// Lv140:// 渡鴉之魂追蹤者盔甲(無描述)
            case 1052318:// Lv140:// 鯊齒船長外套(無描述)
            case 1072485:// Lv140:// 獅心戰鬥鞋(無描述)
            case 1072486:// Lv140:// 龍尾法師鞋(無描述)
            case 1072487:// Lv140:// 鷹翼哨兵鞋(無描述)
            case 1072488:// Lv140:// 渡鴉之魂追蹤者鞋(無描述)
            case 1072489:// Lv140:// 鯊齒船長鞋(無描述)
            case 1112915:// Lv0:// 藍調戒指
            case 1112793:// Lv0:// 快樂指環
            case 5062000:// 神奇方塊
            case 5062002:// 高級神奇方塊
            case 2340000:// 祝福卷軸
            case 5062500:// 大師附加神奇方塊
            case 2614000:// 突破一萬之石
            case 2614001:// 突破十萬之石
            case 2614002:// 突破百萬之石
            case 2614003:// 突破一萬之石
            case 2614004:// 突破十萬之石
            case 2614005:// 突破百萬之石
            case 2614006:// 突破一萬之石
            case 2614007:// 突破十萬之石
            case 2614008:// 突破百萬之石
            case 2614009:// 突破一萬之石
            case 2614010:// 突破十萬之石
            case 2614011:// 突破百萬之石
            case 2614012:// 突破一萬之石
            case 2614013:// 突破十萬之石
            case 2614014:// 突破百萬之石
            case 2614015:// 突破一萬之石
            case 2614016:// 突破十萬之石
            case 2614017:// 突破百萬之石
            case 2431738:// 楓點500商品券
            case 2431739:// 楓點1000商品券
            case 2431740:// 楓點1500商品券
            case 2431741:// 楓點3000商品券
            case 2431742:// 楓點4000商品券
            case 2431743:// 楓點10000商品券
//            case 4021011: //純潔靈魂的火花 - 鍛造重生裝備時的必要材料。
//            case 4021012: //強烈靈魂的淨水 - 鍛造永恆裝備時的必要材料。
//            case 4020013: //夢碎片 - 充滿了夢的碎片。
//            case 4021019: //夢之石 - 黑魔法師的夢凝聚而成的石頭
//            case 4021020: //混沌碎片 - 含有黑暗混沌力量的金屬。打獵140級以上怪物時，有非常低的概率可以獲得。
//            case 4021021: //賢者之石 - 含有煉金術的精髓的礦物。乍一看像是液體。分解105級以上裝備時偶爾可以發現。
//            case 4021022: //太初精髓 - 含有世界起源時期的純粹氣息的神秘石頭。運氣好的話，可以在跳動的心臟和金色花堆中發現。
//            case 4310015: //鬥神證物 - 戰爭之神送給勇敢者的證物。可以感覺到未知的力量。
//
//            case 2430112: //神奇方塊碎片 - 從神奇方塊上掉落的碎塊。雙擊使用物品，可以交換有用的東西。
//            case 2028061: //不可思議的卷軸卷 - 使用後封印解除，變成卷軸。誰也不知道會變成什麼卷軸。
//            case 2028062: //不可思議的配方卷 - 使用後封印解除，變成配方。誰也不知道會變成什麼配方。
//            case 2290285: //[能手冊]神秘能手冊 - 使用後可以變成特定技能能手冊的神秘能手冊。
                return true;
            default:
                return false;
        }
    }

    /*
     * 星岩系統
     */
    public static int getNebuliteGrade(int id) {
        if (id / 10000 != 306) {
            return -1;
        }
        if (id >= 3060000 && id < 3061000) {
            return 0; //[D]級星岩
        } else if (id >= 3061000 && id < 3062000) {
            return 1; //[C]級星岩
        } else if (id >= 3062000 && id < 3063000) {
            return 2; //[B]級星岩
        } else if (id >= 3063000 && id < 3064000) {
            return 3; //[A]級星岩
        }
        return 4; //[S]級星岩
    }

    public static int getLowestPrice(int itemId) {
        switch (itemId) {
            case 2340000: //祝福卷軸
            case 2531000: //防爆卷軸
            case 2530000: //幸運日卷軸
                return 50000000;
        }
        return -1;
    }

    public static int getModifier(int itemId, int up) {
        if (up <= 0) {
            return 0;
        }
        switch (itemId) {
            case 2022459: //星緣的獎勵1
            case 2860179:
            case 2860193:
            case 2860207:
                return 130;
            case 2022460: //佳佳的報答1
            case 2022462: //佳佳的報答3
            case 2022730: //豐收的冬天
                return 150;
            case 2860181:
            case 2860195:
            case 2860209:
                return 200;
        }
        if (itemId / 10000 == 286) { //familiars
            return 150;
        }
        return 200;
    }

    public static short getSlotMax(int itemId) {
        switch (itemId) {
            case 4030003: //俄羅斯方塊
            case 4030004: //俄羅斯方塊
            case 4030005: //俄羅斯方塊
                return 1;
            case 4001168: //金楓葉
            case 4031306:
            case 4031307:
            case 3993000: //吉祥裝飾
            case 3993002: //竹子吉祥裝飾
            case 3993003: //紅色福袋
                return 100;
            case 5220010: //高級快樂百寶券
            case 5220013:
                return 1000;
            case 5220020:
                return 2000;
        }
        return 0;
    }

    public static boolean isDropRestricted(int itemId) {
        return itemId == 3012000
                || itemId == 4030004 //俄羅斯方塊
                || itemId == 1052098 //海盜套裝
                || itemId == 1052202;//玩具皮卡啾套服
    }

    public static boolean isPickupRestricted(int itemId) {
        return itemId == 4030003 //俄羅斯方塊
                || itemId == 4030004; //俄羅斯方塊
    }

    public static short getStat(int itemId, int def) {
        switch (itemId) {
            case 1002419: //楓葉帽
                return 5;
            case 1002959:
                return 25;
            case 1142002: //任務狂人勳章
                return 10;
            case 1122121:
                return 7;
        }
        return (short) def;
    }

    public static short getHpMp(int itemId, int def) {
        switch (itemId) {
            case 1122121:
                return 500;
            case 1142002: //任務狂人勳章
            case 1002959:
                return 1000;
        }
        return (short) def;
    }

    public static short getATK(int itemId, int def) {
        switch (itemId) {
            case 1122121:
                return 3;
            case 1002959:
                return 4;
            case 1142002: //任務狂人勳章
                return 9;
        }
        return (short) def;
    }

    public static short getDEF(int itemId, int def) {
        switch (itemId) {
            case 1122121:
                return 250;
            case 1002959:
                return 500;
        }
        return (short) def;
    }

    public static int getRewardPot(int itemid, int closeness) {
        switch (itemid) {
            case 2440000: //道具橘子寶寶
                switch (closeness / 10) {
                    case 0:
                    case 1:
                    case 2:
                        return 2028041 + (closeness / 10);
                    case 3:
                    case 4:
                    case 5:
                        return 2028046 + (closeness / 10);
                    case 6:
                    case 7:
                    case 8:
                        return 2028049 + (closeness / 10);
                }
                return 2028057; //非常甜美的果實
            case 2440001: //道具鑽石寶寶
                switch (closeness / 10) {
                    case 0:
                    case 1:
                    case 2:
                        return 2028044 + (closeness / 10);
                    case 3:
                    case 4:
                    case 5:
                        return 2028049 + (closeness / 10);
                    case 6:
                    case 7:
                    case 8:
                        return 2028052 + (closeness / 10);
                }
                return 2028060; //非常燦爛的鑽石
            case 2440002: //福滿月妙
                return 2028069; //可愛的福滿月妙
            case 2440003: //迷你西瓜盆景
                return 2430278; //黃金楓葉果實
            case 2440004: //第一個西瓜花盆
                return 2430381; //第一個佳佳牌西瓜
            case 2440005: //第二個西瓜花盆
                return 2430393; //第二個佳佳牌西瓜
        }
        return 0;
    }

    public static boolean isLogItem(int itemId) {
        switch (itemId) {
            case 4000463: // 國慶紀念幣
            case 2340000: // 祝福卷軸
            case 2049000: // 白醫卷軸
            case 2049001: // 白醫卷軸
            case 2049002: // 白醫卷軸
            case 2040006: // 詛咒白醫卷軸
            case 2040007: // 詛咒白醫卷軸
            case 2040303: // 耳環智力必成卷
            case 2040403: // 上衣防禦必成卷
            case 2040506: // 全身盔甲敏捷必成卷
            case 2040507: // 全身盔甲防禦必成卷
            case 2040603: // 褲裙防禦必成卷
            case 2040709: // 鞋子敏捷必成卷
            case 2040710: // 鞋子跳躍必成卷
            case 2040711: // 鞋子速度必成卷
            case 2040806: // 手套敏捷必成卷
            case 2040903: // 盾牌防禦必成卷
            case 2041024: // 披風魔法防禦必成卷
            case 2041025: // 披風物理防禦必成卷
            case 2043003: // 單手劍攻擊必成卷
            case 2043103: // 單手斧攻擊必成卷
            case 2043203: // 單手鈍器攻擊必成卷
            case 2043303: // 短劍攻擊必成卷
            case 2043703: // 短杖攻擊必成卷
            case 2043803: // 長杖攻擊必成卷
            case 2044003: // 雙手劍攻擊必成卷
            case 2044103: // 雙手斧攻擊必成卷
            case 2044203: // 雙手鈍器攻擊必成卷
            case 2044303: // 槍攻擊必成卷
            case 2044403: // 矛攻擊必成卷
            case 2044503: // 弓攻擊必成卷
            case 2044603: // 弩攻擊必成卷
            case 2044908: // 短槍攻擊必成卷
            case 2044815: // 指節攻擊必成卷
            case 2044019: // 雙手劍魔力必成卷
            case 2044703: // 拳套攻擊必成卷
                return true;
        }
        return false;
    }

    public static int getNeedHonor(int lockLevel, int lockCount) {
        int needHonor = 0;
        switch (lockLevel) {
            case 0: { // 特殊
                needHonor = 100;
                break;
            }
            case 1: { // 稀有
                needHonor = 200;
                break;
            }
            case 2: {
                needHonor = lockCount == 0 ? 1500 : lockCount == 1 ? 3000 : 5500;
                break;
            }
            case 3: {
                needHonor = lockCount == 0 ? 8000 : lockCount == 1 ? 11000 : 16000;
                break;
            }
        }
        return needHonor;
    }

    /**
     * 打獵可獲得經驗的椅子
     */
    public static boolean isSetupExpRate(int itemid) {
        return itemid / 10000 == 302;
    }

    public static boolean isZeroWeapon(final int weapon) {
        return MapleWeapon.琉.check(weapon) || MapleWeapon.璃.check(weapon);
    }

    public static int getArcExpNeededForLevel(short arcLevel, int type) {
        return arcLevel * arcLevel + 11;
    }

    public static long getArcMesoNeededForLevel(short arcLevel, int type) {
        switch (type) {
            case 1:
                return 3110000 + 3960000L * arcLevel;
            case 2:
                return 6220000 + 4620000L * arcLevel;
            case 3:
                return 9330000 + 5280000L * arcLevel;
            default:
                return 11196000 + 5940000L * arcLevel;
        }
    }

    public static int getAutExpNeededForLevel(short autLevel, int type) {
        return 9 * autLevel * autLevel + 20 * autLevel;
    }

    public static long getAutMesoNeededForLevel(short autLevel, int type) {
        return 100000 * (long) Math.floor((1.0 + 0.1 * type) * Math.floor(970.2 + 884.4 * autLevel));
    }

    public static int getEquipSummon(int itemId) {
        switch (itemId) {
            case 1112585:
            case 1112594:
                return 1085;
            case 1112586:
                return 1087;
            case 1112663:
                return 80000155;
            case 1112735:
                return 80001154;
            case 1113008:
                return 80000052;
            case 1113009:
                return 80000053;
            case 1113010:
                return 80000054;
            case 1114200:
                return 80001518;
            case 1114219:
                return 80001715;
            case 1114201:
                return 80001519;
            case 1114220:
                return 80001716;
            case 1114202:
                return 80001520;
            case 1114221:
                return 80001717;
            case 1114203:
                return 80001521;
            case 1114222:
                return 80001718;
            case 1114204:
                return 80001522;
            case 1114223:
                return 80001719;
            case 1114205:
                return 80001523;
            case 1114224:
                return 80001720;
            case 1114206:
                return 80001524;
            case 1114225:
                return 80001721;
            case 1114207:
            case 1114213:
            case 1114226:
            case 1114238:
                return 80011103;
            case 1114208:
            case 1114214:
            case 1114227:
            case 1114239:
                return 80011104;
            case 1114209:
            case 1114215:
            case 1114228:
            case 1114240:
                return 80011105;
            case 1114210:
            case 1114216:
            case 1114229:
            case 1114241:
                return 80011106;
            case 1114211:
            case 1114217:
            case 1114230:
            case 1114242:
                return 80011107;
            case 1114212:
            case 1114218:
            case 1114231:
            case 1114243:
                return 80011108;
            case 1113020:
                return 80001262;
            case 1113189:
                return 80010067;
            case 1113204:
                return 80010075;
            case 1113190:
                return 80010068;
            case 1113205:
                return 80010076;
            case 1113191:
                return 80010069;
            case 1113206:
                return 80010077;
            case 1113192:
                return 80010070;
            case 1113207:
                return 80010078;
            case 1113193:
                return 80010071;
            case 1113208:
                return 80010079;
            case 1113194:
                return 80010072;
            case 1113209:
                return 80010080;
            default:
                return -1;
        }
    }

    public static boolean isDemonShield(int itemId) {
        return itemId < 1100000 && itemId >= 1099000;
    }

    public static int getMaxLevel(int itemId) {
        Map<String, Map<String, Integer>> inc = MapleItemInformationProvider.getInstance().getEquipIncrements(itemId);
        return inc != null ? (inc.size()) : 0;
    }

    public static int getExpForLevel(int i, int itemId) {
        if (GameConstants.isReverseItem(itemId)) {
            return GameConstants.getReverseRequiredEXP(i);
        } else if (getMaxLevel(itemId) > 0) {
            return GameConstants.getTimelessRequiredEXP(i);
        }
        return 0;
    }

    public static boolean is漩渦裝備(int itemId) {
        switch (itemId) {
            case 1003976:
            case 1012438:
            case 1022211:
            case 1032224:
            case 1052669:
            case 1072870:
            case 1082556:
            case 1102623:
            case 1122269:
            case 1132247:
            case 1152160:
            case 1212089:
            case 1222084:
            case 1232084:
            case 1242090:
            case 1252033:
            case 1302297:
            case 1312173:
            case 1322223:
            case 1332247:
            case 1342090:
            case 1362109:
            case 1372195:
            case 1382231:
            case 1402220:
            case 1412152:
            case 1422158:
            case 1432187:
            case 1442242:
            case 1452226:
            case 1462213:
            case 1472235:
            case 1482189:
            case 1492199:
            case 1522113:
            case 1532118: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static boolean ee(int itemId) {
        return itemId / 10000 == 110;
    }

    public static boolean ef(int itemId) {
        return itemId / 10000 == 111;
    }

    public static int getZeroWeaponNeededLevel(int n) {
        switch (n) {
            case 1: {
                n = 100;
                break;
            }
            case 2: {
                n = 110;
                break;
            }
            case 3: {
                n = 130;
                break;
            }
            case 4: {
                n = 140;
                break;
            }
            case 5: {
                n = 160;
                break;
            }
            case 6: {
                n = 180;
                break;
            }
            default: {
                n = 200;
                break;
            }
        }
        return n;
    }

    public static int getEffectItemID(int itemId) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Map<String, Integer> stats = ii.getItemBaseInfo(itemId);
        if (stats.containsKey("effectItemID")) {
            return stats.get("effectItemID");
        }
        return 0;
    }

    public static short[] getEquipedSlot(int itemId) {
        boolean isCash = MapleItemInformationProvider.getInstance().isCash(itemId);
        if (類型.帽子(itemId)) {
            if (isCash) {
                return new short[]{-101};
            } else {
                return new short[]{-1};
            }
        } else if (類型.臉飾(itemId)) {
            if (isCash) {
                return new short[]{-102};
            } else {
                return new short[]{-2};
            }
        } else if (類型.眼飾(itemId)) {
            if (isCash) {
                return new short[]{-103};
            } else {
                return new short[]{-3};
            }
        } else if (類型.耳環(itemId)) {
            if (isCash) {
                return new short[]{-104};
            } else {
                return new short[]{-4};
            }
        } else if (類型.上衣(itemId) || 類型.套服(itemId)) {
            if (isCash) {
                return new short[]{-105};
            } else {
                return new short[]{-5};
            }
        } else if (類型.褲裙(itemId)) {
            if (isCash) {
                return new short[]{-106};
            } else {
                return new short[]{-6};
            }
        } else if (類型.鞋子(itemId)) {
            if (isCash) {
                return new short[]{-107};
            } else {
                return new short[]{-7};
            }
        } else if (類型.手套(itemId)) {
            if (isCash) {
                return new short[]{-108};
            } else {
                return new short[]{-8};
            }
        } else if (類型.披風(itemId)) {
            if (isCash) {
                return new short[]{-109};
            } else {
                return new short[]{-9};
            }
        } else if (類型.副手(itemId) || MapleWeapon.琉.check(itemId)) {
            if (isCash) {
                return new short[]{-110};
            } else {
                return new short[]{-10};
            }
        } else if (類型.武器(itemId) && !MapleWeapon.琉.check(itemId)) {
            if (isCash) {
                return new short[]{-111};
            } else {
                return new short[]{-11};
            }
        } else if (類型.戒指(itemId)) {
            if (isCash) {
                return new short[]{-112, -113, -115, -116};
            } else {
                return new short[]{-12, -13, -15, -16};
            }
        } else if (類型.墜飾(itemId)) {
            return new short[]{-17, -36};
        } else if (類型.騎寵(itemId)) {
            return new short[]{-18};
        } else if (類型.馬鞍(itemId)) {
            return new short[]{-19};
        } else if (類型.勳章(itemId)) {
            return new short[]{-21};
        } else if (類型.腰帶(itemId)) {
            return new short[]{-22};
        } else if (類型.肩飾(itemId)) {
            return new short[]{-28};
        } else if (類型.口袋道具(itemId)) {
            return new short[]{-31};
        } else if (類型.機器人(itemId)) {
            return new short[]{-32};
        } else if (類型.心臟(itemId)) {
            return new short[]{-33};
        } else if (類型.胸章(itemId)) {
            return new short[]{-34};
        } else if (類型.能源(itemId)) {
            return new short[]{-35};
        } else if (類型.寵物裝備(itemId)) {
            return new short[]{-114, -124, -126};
        } else if (類型.龍面具(itemId)) {
            return new short[]{-1000};
        } else if (類型.龍墜飾(itemId)) {
            return new short[]{-1001};
        } else if (類型.龍之翼(itemId)) {
            return new short[]{-1002};
        } else if (類型.龍尾巴(itemId)) {
            return new short[]{-1003};
        } else if (類型.引擎(itemId)) {
            return new short[]{-1100};
        } else if (類型.手臂(itemId)) {
            return new short[]{-1101};
        } else if (類型.腿(itemId)) {
            return new short[]{-1102};
        } else if (類型.機殼(itemId)) {
            return new short[]{-1103};
        } else if (類型.晶體管(itemId)) {
            return new short[]{-1104};
        } else if (類型.圖騰(itemId)) {
            return new short[]{-5000, -5001, -5002};
        } else {
            return new short[0];
        }
    }

    public static class TapJoyReward {

        private static final Map<Integer, Pair<Integer, Integer>> stages = new TreeMap<>();
        private static final Map<Integer, List<Integer>> rewards = new HashMap<>();

        public static void init() {
            if (!rewards.isEmpty()) {
                rewards.clear();
            }
            final ArrayList<Integer> list = new ArrayList<>();
            final String[] split = CSInfoConfig.CASH_PBTAPREWARD1.split(",");
            for (int length = split.length, i = 0; i < length; ++i) {
                if (split[i].isEmpty()) {
                    continue;
                }
                list.add(Integer.valueOf(split[i]));
            }
            rewards.put(0, list);
            final ArrayList<Integer> list2 = new ArrayList<>();
            final String[] split2 = CSInfoConfig.CASH_PBTAPREWARD2.split(",");
            for (int length2 = split2.length, j = 0; j < length2; ++j) {
                if (split2[j].isEmpty()) {
                    continue;
                }
                list2.add(Integer.valueOf(split2[j]));
            }
            rewards.put(1, list2);
            final ArrayList<Integer> list3 = new ArrayList<>();
            final String[] split3 = CSInfoConfig.CASH_PBTAPREWARD3.split(",");
            for (int length3 = split3.length, k = 0; k < length3; ++k) {
                if (split3[k].isEmpty()) {
                    continue;
                }
                list3.add(Integer.valueOf(split3[k]));
            }
            rewards.put(2, list3);
        }

        public static Map<Integer, Pair<Integer, Integer>> getStages() {
            if (stages.isEmpty()) {
                stages.put(0, new Pair<>(5224000, 50400358));
                stages.put(1, new Pair<>(5224001, 50400359));
                stages.put(2, new Pair<>(5224009, 50400454));
                stages.put(3, new Pair<>(5224010, 50400455));
                stages.put(4, new Pair<>(5224011, 50400456));
                stages.put(5, new Pair<>(0, 0));
            }
            return stages;
        }

        public static Integer getSN(final int itemid) {
            int intValue = -1;
            for (final Map.Entry<Integer, Pair<Integer, Integer>> entry : getStages().entrySet()) {
                if (entry.getValue().getLeft() == itemid) {
                    intValue = entry.getKey();
                    break;
                }
            }
            return intValue;
        }

        public static List<Integer> getRewardList(final int rank) {
            return rewards.get(rank);
        }

        public static Pair<Integer, Integer> getItemIdAndSN(final int rank) {
            return stages.get(rank);
        }
    }

    public static boolean eI(final int id) {
        int type = id / 10000;
        switch (type) {
            case 101:
                return true;
            case 102:
                return true;
            case 103:
                return true;
            case 111:
                return true;
            case 112:
                return true;
            case 113:
                return true;
            case 114:
                return true;
            case 115:
                return true;
            case 116:
                return true;
            case 118:
                return true;
            case 119:
                return true;
            case 120:
                return true;
        }
        return false;
    }

    public static boolean eJ(final int id) {
        int type = id / 10000;
        return type == 111 || type == 112 || type == 113;
    }

    public static boolean ez(final int n) {
        return n / 10000 >= 180 && n / 10000 <= 183;
    }

    public static boolean eH(final int id) {
        int type = id / 10000;
        switch (type) {
            case 100:
                return true;
            case 104:
                return true;
            case 105:
                return true;
            case 106:
                return true;
            case 107:
                return true;
            case 108:
                return true;
            case 110:
                return true;
            case 109:
                return true;
        }
        return false;
    }

    public static Map<Integer, ChairType> CHAIR_TYPE = new LinkedHashMap<>();

    public static ChairType getChairType(int itemId) {
        if (itemId / 10000 != 301) {
            return ChairType.NORMAL;
        }
        if (CHAIR_TYPE.containsKey(itemId)) {
            return CHAIR_TYPE.get(itemId);
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        ChairType type;
        if (ii.getItemProperty(itemId, "info/towerTop", null) != null) {
            type = ChairType.TOWER;
        } else if (ii.getItemProperty(itemId, "info/textInfo", null) != null) {
            type = ChairType.TEXT;
        } else if (ii.getItemProperty(itemId, "info/mesoChair", null) != null) {
            type = ChairType.MESO;
        } else if (ii.getItemProperty(itemId, "info/lvChairInfo", null) != null) {
            type = ChairType.LV;
        } else {
            String customChair = ii.getItemProperty(itemId, "info/customChair", null);
            if (customChair != null) {
                String sType = ii.getItemProperty(itemId, "info/customChair/type", null);
                switch (sType) {
                    case "popChair":
                        type = ChairType.POP;
                        break;
                    case "timeChair":
                        type = ChairType.TIME;
                        break;
                    case "starForceChair":
                        type = ChairType.STARFORCE;
                        break;
                    case "trickOrTreatChair":
                        type = ChairType.TRICK_OR_TREAT;
                        break;
                    case "celebChair":
                        type = ChairType.CELEBRATE;
                        break;
                    case "randomChair":
                        type = ChairType.RANDOM;
                        break;
                    case "identityChair":
                        type = ChairType.IDENTITY;
                        break;
                    case "mirrorChair":
                        type = ChairType.MIRROR;
                        break;
                    case "popButtonChair":
                        type = ChairType.POP_BUTTON;
                        break;
                    case "rollingHouseChair":
                        type = ChairType.ROLLING_HOUSE;
                        break;
                    case "androidChair":
                        type = ChairType.ANDROID;
                        break;
                    case "mannequinChair":
                        type = ChairType.MANNEQUIN;
                        break;
                    case "rotatedSleepingBagChair":
                        type = ChairType.ROTATED_SLEEPING_BAG_CHAIR;
                        break;
                    case "eventPointChair":
                        type = ChairType.EVENT_POINT;
                        break;
                    case "hashTagChair":
                        type = ChairType.HASH_TAG;
                        break;
                    case "petChair":
                        type = ChairType.PET;
                        break;
                    case "scoreChair":
                        type = ChairType.SCORE;
                        break;
                    case "scaleAvatarChair":
                        type = ChairType.SCALE_AVATAR;
                        break;
                    case "wasteChair":
                        type = ChairType.WASTE;
                        break;
                    case "2019rollingHouseChair":
                        type = ChairType.ROLLING_HOUSE_2019;
                        break;
                    case "TraitsChair":
                        type = ChairType.TRAITS;
                        break;
                    case "charLvChair":
                        type = ChairType.CHAR_LV;
                        break;
                    case "eventPointGenderlyChair":
                        type = ChairType.EVENT_POINT_GENDERLY;
                        break;
                    case "eventPointCloneChair":
                        type = ChairType.EVENT_POINT_CLONE;
                        break;
                    case "yetiChair":
                        type = ChairType.YETI;
                        break;
                    case "mapleGlobeChair":
                        type = ChairType.MAPLE_GLOBE;
                        break;
                    default:
                        System.err.println("unknown custom chair type: " + sType);
                        type = ChairType.NORMAL;
                        break;
                }
            } else {
                type = ChairType.NORMAL;
            }
        }
        CHAIR_TYPE.put(itemId, type);
        return type;
    }

    public static boolean isItemType(String type, int itemID) {
        switch (type) {
            case "帽子":
                return 類型.帽子(itemID);
            case "臉飾":
                return 類型.臉飾(itemID);
            case "眼飾":
                return 類型.眼飾(itemID);
            case "耳環":
                return 類型.耳環(itemID);
            case "上衣":
                return 類型.上衣(itemID);
            case "套服":
                return 類型.套服(itemID);
            case "褲裙":
                return 類型.褲裙(itemID);
            case "鞋子":
                return 類型.鞋子(itemID);
            case "手套":
                return 類型.手套(itemID);
            case "盾牌":
                return 類型.盾牌(itemID);
            case "披風":
                return 類型.披風(itemID);
            case "戒指":
                return 類型.戒指(itemID);
            case "墜飾":
                return 類型.墜飾(itemID);
            case "腰帶":
                return 類型.腰帶(itemID);
            case "勳章":
                return 類型.勳章(itemID);
            case "肩飾":
                return 類型.肩飾(itemID);
            case "口袋道具":
                return 類型.口袋道具(itemID);
            case "胸章":
                return 類型.胸章(itemID);
            case "能源":
                return 類型.能源(itemID);
            case "圖騰":
                return 類型.圖騰(itemID);
            case "鏟":
                return 類型.鏟(itemID);
            case "十字鎬":
                return 類型.十字鎬(itemID);
            case "引擎":
                return 類型.引擎(itemID);
            case "手臂":
                return 類型.手臂(itemID);
            case "腿":
                return 類型.腿(itemID);
            case "晶體管":
                return 類型.晶體管(itemID);
            case "機器人":
                return 類型.機器人(itemID);
            case "心臟":
                return 類型.心臟(itemID);
            case "拼圖":
                return 類型.拼圖(itemID);
            case "秘法符文":
                return 類型.秘法符文(itemID);
            case "真實符文":
                return 類型.真實符文(itemID);
            case "寵物裝備":
                return 類型.寵物裝備(itemID);
            case "騎寵":
                return 類型.騎寵(itemID);
            case "馬鞍":
                return 類型.馬鞍(itemID);
            case "龍面具":
                return 類型.龍面具(itemID);
            case "龍墜飾":
                return 類型.龍墜飾(itemID);
            case "龍之翼":
                return 類型.龍之翼(itemID);
            case "龍尾巴":
                return 類型.龍尾巴(itemID);
            case "弓矢":
                return 類型.弓矢(itemID);
            case "弩矢":
                return 類型.弩矢(itemID);
            case "飛鏢":
                return 類型.飛鏢(itemID);
            case "子彈":
                return 類型.子彈(itemID);
            case "萌獸卡":
                return 類型.萌獸卡(itemID);
            case "寵物":
                return 類型.寵物(itemID);
            case "防具":
                return 類型.防具(itemID);
            case "飾品":
                return 類型.飾品(itemID);
            case "副手":
                return 類型.副手(itemID);
            case "武器":
                return 類型.武器(itemID);
            case "特殊副手":
                return 類型.特殊副手(itemID);
            case "機械":
                return 類型.機械(itemID);
            case "龍裝備":
                return 類型.龍裝備(itemID);
            case "可充值道具":
                return 類型.可充值道具(itemID);
            case "採集道具":
                return 類型.採集道具(itemID);
            case "單手武器":
                return 類型.單手武器(itemID);
            case "雙手武器":
                return 類型.雙手武器(itemID);
            case "物理武器":
                return 類型.物理武器(itemID);
            case "魔法武器":
                return 類型.魔法武器(itemID);
            case "騎寵道具":
                return 類型.騎寵道具(itemID);
            case "裝備":
                return 類型.裝備(itemID);
            case "消耗":
                return 類型.消耗(itemID);
            case "裝飾":
                return 類型.裝飾(itemID);
            case "其他":
                return 類型.其他(itemID);
            case "特殊":
                return 類型.特殊(itemID);
            case "友情裝備":
                return 類型.友情裝備(itemID);
            case "戀人裝備":
                return 類型.戀人裝備(itemID);
            case "結婚戒指":
                return 類型.結婚戒指(itemID);
            case "特效裝備":
                return 類型.特效裝備(itemID);
            case "管理員裝備":
                return 類型.管理員裝備(itemID);
            case "城鎮傳送卷軸":
                return 類型.城鎮傳送卷軸(itemID);
            case "普通升級卷軸":
                return 類型.普通升級卷軸(itemID);
            case "阿斯旺卷軸":
                return 類型.阿斯旺卷軸(itemID);
            case "提升卷":
                return 類型.提升卷(itemID);
            case "輪迴星火":
                return 類型.輪迴星火(itemID);
            case "白衣卷軸":
                return 類型.白衣卷軸(itemID);
            case "混沌卷軸":
                return 類型.混沌卷軸(itemID);
            case "樂觀混沌卷軸":
                return 類型.樂觀混沌卷軸(itemID);
            case "飾品卷軸":
                return 類型.飾品卷軸(itemID);
            case "裝備強化卷軸":
                return 類型.裝備強化卷軸(itemID);
            case "黃金鐵鎚":
                return 類型.黃金鐵鎚(itemID);
            case "白金鎚子":
                return 類型.白金鎚子(itemID);
            case "潛能卷軸":
                return 類型.潛能卷軸(itemID);
            case "附加潛能卷軸":
                return 類型.附加潛能卷軸(itemID);
            case "潛能印章":
                return 類型.潛能印章(itemID);
            case "附加潛能印章":
                return 類型.附加潛能印章(itemID);
            case "回真卷軸":
                return 類型.回真卷軸(itemID);
            case "幸運日卷軸":
                return 類型.幸運日卷軸(itemID);
            case "保護卷軸":
                return 類型.保護卷軸(itemID);
            case "安全卷軸":
                return 類型.安全卷軸(itemID);
            case "卷軸保護卡":
                return 類型.卷軸保護卡(itemID);
            case "靈魂卷軸":
            case "附魔器":
                return 類型.靈魂卷軸_附魔器(itemID);
            case "靈魂寶珠":
                return 類型.靈魂寶珠(itemID);
            case "武器攻擊力卷軸":
                return 類型.武器攻擊力卷軸(itemID);
            case "海外服特殊卷軸":
                return 類型.海外服特殊卷軸(itemID);
            case "特殊卷軸":
                return 類型.特殊卷軸(itemID);
            case "真楓葉之心":
                return 類型.真楓葉之心(itemID);
            case "特殊潛能道具":
                return 類型.特殊潛能道具(itemID);
            case "強化寶石":
                return 類型.強化寶石(itemID);
            case "膚色":
                return 類型.膚色(itemID);
            case "臉型":
                return 類型.臉型(itemID);
            case "髮型":
                return 類型.髮型(itemID);
            case "男臉型":
                return 類型.男臉型(itemID);
            case "女臉型":
                return 類型.女臉型(itemID);
            case "通用臉型":
                return 類型.通用臉型(itemID);
            case "男髮型":
                return 類型.男髮型(itemID);
            case "女髮型":
                return 類型.女髮型(itemID);
            case "通用髮型":
                return 類型.通用髮型(itemID);
            default:
                for (MapleWeapon weapon : MapleWeapon.values()) {
                    if (weapon.name().equalsIgnoreCase(type)) {
                        return weapon.check(itemID);
                    }
                }
                return false;
        }
    }
}
