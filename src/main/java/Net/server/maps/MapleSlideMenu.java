package Net.server.maps;

import Client.MapleCharacter;

public class MapleSlideMenu { // UI.wz\UIWindow2.img\SlideMenu\

    public static class SlideMenu0 {

        public static final int above = 0xFF;

        public enum DimensionalMirror {
            PQ0(0, "阿里安特競技場", 682020000, 3, 20, 30, 0, 0, false),
            PQ1(1, "武陵道場", 925020000, 4, 105, above, 0, 0, true),
            PQ2(2, "怪物擂台賽", 980000000, 4, 30, 50, 0, 0, false),
            PQ3(3, "怪物擂台賽", 980030000, 4, 50, above, 0, 0, true),
            PQ4(4, "霧之海", 923020000, 0, 120, above, 0, 0, true),
            PQ5(5, "奈特的金字塔", 926010000, 4, 60, 109, 0, 0, true),
            PQ6(6, "廢棄的地鐵月台", 910320000, 2, 25, 30, 0, 0, false),
            PQ7(7, "幸福村", 209000000, 0, 13, above, 0, 0, false),
            PQ8(8, "黃金寺廟2", 950100000, 9, 110, above, 0, 0, false),
            PQ9(9, "月妙的年糕", 706040000, 0, 30, above, 0, 0, true),
            PQ10(10, "第一次同行", 910340700, 0, 50, above, 0, 0, false),
            PQ11(11, "時空的裂縫", 221023300, 2, 50, above, 0, 0, false),
            PQ12(12, "毒霧森林", 300030100, 1, 70, above, 0, 0, false),
            PQ13(13, "女神的痕跡", 200080101, 1, 70, above, 0, 0, false),
            PQ14(14, "金勾海賊王", 251010404, 2, 70, above, 0, 0, false),
            PQ15(15, "羅密歐和朱麗葉", 261000021, 5, 70, above, 0, 0, false),
            PQ16(16, "侏儒帝王的復活", 211000002, 0, 120, above, 0, 0, false),
            PQ17(17, "龍騎士", 240080000, 2, 120, above, 0, 0, false),
            PQ18(18, "活動地圖", 0, 0, 10, above, 0, 0, false),
            PQ19(19, "萬聖節的樹", 682000000, 0, above, above, 0, 0, false),
            PQ20(20, "活動地圖2", 0, 0, 10, above, 0, 0, false),
            PQ21(21, "陷入危險的坎特", 923040000, 0, 120, above, 0, 0, false),
            PQ22(22, "逃脫", 921160000, 0, 120, above, 0, 0, false),
            PQ23(23, "冰騎士的詛咒", 932000000, 0, 20, above, 0, 0, false),
            PQ24(24, "活動地圖3", 0, 0, 10, above, 0, 0, false),
            PQ25(25, "楓之谷聯盟會議場", 913050010, 0, 75, above, 0, 0, true),
            PQ26(26, "萬聖節的樹2", 682000700, 0, 10, above, 0, 0, false),
            PQ27(27, "阿斯旺解放之戰", 262010000, 0, 40, above, 0, 0, true),
            PQ28(28, "黃金寺廟", 950100000, 9, 105, above, 0, 0, false),
            PQ29(29, "休菲凱曼的畫廊", 956000000, 0, 50, 120, 0, 0, false),
            PQ30(30, "大亂鬥", 960000000, 0, 30, above, 0, 0, true),
            PQ31(31, "櫻花城", 800000000, 0, 10, above, 0, 0, true),
            PQ32(32, "進化系統研究所", 957000000, 0, 105, above, 1802, 1, true),
            PQ33(33, "次元入侵", 940020000, 0, 140, above, 0, 0, true),
            PQ34(34, "休菲凱曼的招待所(初級)", 910002000, 2, 50, above, 0, 0, false),
            PQ35(35, "休菲凱曼的招待所(中級)", 910002010, 2, 70, above, 0, 0, false),
            PQ36(36, "休菲凱曼的招待所(高級)", 910002020, 2, 10, above, 0, 0, false),
            PQ37(37, "湯寶寶", 912080000, 0, 70, above, 0, 0, false),
            PQ38(38, "克林森烏德城", 301000000, 0, 130, above, 0, 0, true),
            PQ39(39, "次元圖書館", 302000000, 0, 100, 125, 0, 0, true),
            PQ40(40, "進入聯合組隊任務", 910002000, 0, 50, above, 0, 0, true),
            PQ41(41, "跨伺服器聯合組隊任務", 708001000, 0, 70, above, 0, 0, true),
            PQ42(42, "露塔必思", 105200000, 0, 125, above, 0, 0, true),
            PQ43(43, "綠野仙蹤", 992000000, 0, 100, above, 0, 0, true),
            PQ44(44, "Friends Story", 100000004, 0, 100, above, 0, 0, false),
            PQ45(45, "怪物公園", 951000000, 0, 75, above, 0, 0, true),
            PQ46(46, "鬼怪公園", 956100000, 0, 125, above, 0, 0, true),
            PQ47(47, "破王烏勒斯", 970072200, 0, 100, above, 0, 0, true),
            PQ48(48, "簡單西格諾斯", 0, 0, 100, above, 0, 0, false),
            PQ49(49, "楓葉英雄", 913050010, 0, 100, above, 0, 0, false),
            PQ50(50, "神操作", 0, 0, 100, above, 0, 0, false),
            PQ200(200, "外星訪問者", 861000000, 0, 200, above, 0, 0, true),
            PQ201(201, "另一個水世界", 860000000, 6, 200, above, 0, 0, true),
            PQ250(250, "艾芙特領域", 867113100, 0, 0, above, 0, 0, true),
            PQ512(512, "維拉仙特", 512000000, 0, 0, above, 0, 0, true),
            PQ666(666, "黑扉城", 610050000, 0, 0, above, 0, 0, true),
            PQ667(667, "精神醫院", 620105500, 0, 0, above, 0, 0, true),
            PQ801(801, "世界旅行", 950000000, 0, 0, above, 0, 0, true),
            PQ802(802, "中式婚禮", 700000000, 0, 0, above, 0, 0, false),
            PQ803(803, "楓葉丘陵", 807000000, 0, 0, above, 0, 0, true),
            PQ804(804, "尖耳狐狸村", 410000000, 0, 0, above, 0, 0, false),
            PQ805(805, "Jett&俠客", 743050000, 0, 0, above, 0, 0, false),
            PQ806(806, "幸福山", 0, 0, 0, above, 0, 0, false),
            PQ807(807, "阿里山", 749080900, 0, 13, above, 0, 0, true),
            PQ808(808, "虛幻之森", 749081200, 0, 0, above, 0, 0, false),
            PQ906(906, "試煉之塔", 808000100, 0, 0, above, 0, 0, true),
            PQ908(908, "比叡山本堂", 811000999, 0, 0, above, 0, 0, true),
            PQ993(993, "進擊的巨人", 814000000, 0, 0, above, 0, 0, true),
            PQ994(994, "春天大地", 867000000, 0, 0, above, 0, 0, true),
            PQ995(995, "聖克梅勒茲", 865000000, 0, 0, above, 0, 0, true),
            ;

            private final int id;
            private final int map;
            private final int minLevel;
            private final int maxLevel;
            private final int requieredQuest;
            private final int requieredQuestState;
            private final String name;
            private final boolean show;
            private final int portal;

            DimensionalMirror(int id, String name, int map, int portal, int minLevel, int maxLevel, int requieredQuest, int requieredQuestState, boolean show) {
                this.id = id;
                this.name = name;
                this.map = map;
                this.portal = portal;
                this.minLevel = minLevel;
                this.maxLevel = maxLevel;
                this.requieredQuest = requieredQuest;
                this.requieredQuestState = requieredQuestState;
                this.show = show;
            }

            public int getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public int getMap() {
                return map;
            }

            public int getPortal() {
                return portal;
            }

            public int getMinLevel() {
                return minLevel;
            }

            public int getMaxLevel() {
                return maxLevel;
            }

            public int getRequieredQuest() {
                return requieredQuest;
            }

            public int getRequieredQuestState() {
                return requieredQuestState;
            }

            public boolean isShow() {
                return show;
            }

            public static DimensionalMirror getById(int id) {
                for (DimensionalMirror mirror : DimensionalMirror.values()) {
                    if (mirror.getId() != id) continue;
                    return mirror;
                }
                return null;
            }
        }

        public static String getSelectionInfo(MapleCharacter player, Integer npc) {
            StringBuilder mapselect = new StringBuilder();
            for (DimensionalMirror mirror : DimensionalMirror.values()) {
                if (player.getLevel() >= mirror.getMinLevel() && player.getLevel() <= mirror.getMaxLevel() && mirror.isShow()) {
                    if ((player.getQuestStatus(mirror.getRequieredQuest()) >= mirror.getRequieredQuestState()) || mirror.getRequieredQuest() == 0) {
                        mapselect.append("#").append(mirror.getId()).append("#").append(mirror.getName());
                    }
                }
            }
            if ((mapselect.length() == 0) || mapselect.toString().equals("")) {
                mapselect = new StringBuilder("#-1#沒有可以通過次元之鏡移動的地方。");
            }
            if (npc == 9201231) {
                mapselect = new StringBuilder("#87# Crack in the Dimensional Mirror");
            }
            return mapselect.toString();
        }

        public static int[] getDataIntegers(Integer id) {
            DimensionalMirror mirror = DimensionalMirror.getById(id);
            return new int[]{mirror.getMap(), mirror.getPortal()};
        }
    }

    public static class SlideMenu1 {

        public enum 時間通道 {
            // TODO: finish this(add quest ids and map ids)

            YEAR_2021(1, "Year 2021, Average Town", 0, 0, 0, 0),
            YEAR_2099(2, "Year 2099, Midnight Harbor", 0, 0, 0, 0),
            YEAR_2215(3, "Year 2215, Bombed City Center", 0, 0, 0, 0),
            YEAR_2216(4, "Year 2216, Ruined City", 0, 0, 0, 0),
            YEAR_2230(5, "Year 2230, Dangerous Tower", 0, 0, 0, 0),
            YEAR_2503(6, "Year 2503, Air Battleship Hermes", 0, 0, 0, 0);
            private final int id;
            private final int map;
            private final int portal;
            private final int requieredQuest;
            private final int requieredQuestState;
            private final String name;

            時間通道(int id, String name, int map, int portal, int requieredQuest, int requieredQuestState) {
                this.id = id;
                this.name = name;
                this.map = map;
                this.requieredQuest = requieredQuest;
                this.requieredQuestState = requieredQuestState;
                this.portal = portal;
            }

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public int getMap() {
                return map;
            }

            public int getPortal() {
                return portal;
            }

            public int getRequieredQuest() {
                return requieredQuest;
            }

            public int getRequieredQuestState() {
                return requieredQuestState;
            }

            public static 時間通道 getById(int id) {
                for (時間通道 gate : 時間通道.values()) {
                    if (gate.getId() == id) {
                        return gate;
                    }
                }
                return null; // default
            }
        }

        public static String getSelectionInfo(MapleCharacter chr, int npc) {
            String mapselect = "";
            for (時間通道 gate : 時間通道.values()) {
                if ((chr.getQuestStatus(gate.getRequieredQuest()) == gate.getRequieredQuestState()) || gate.getRequieredQuest() == 0) {
                    mapselect += "#" + gate.getId() + "#" + gate.getName();
                }
            }
            if (mapselect == null || mapselect.isEmpty()) {
                mapselect = "#-1# There are no locations you can move to.";
            }
            return mapselect;
        }

        public static int[] getDataIntegers(int id) {
            時間通道 timeGate = 時間通道.getById(id);
            int[] dataIntegers = {timeGate != null ? timeGate.getMap() : 時間通道.YEAR_2099.getMap(), timeGate != null ? timeGate.getPortal() : 時間通道.YEAR_2099.getPortal()}; // Year 2099 as
            // default
            return dataIntegers;
        }
    }

    public static class SlideMenu2 {

        public enum 高級網咖時空石 {
            TOWN_0(0, "弓箭手村", 100000000, 0),
            TOWN_1(1, "魔法森林", 101000000, 0),
            TOWN_2(2, "勇士之村", 102000000, 0),
            TOWN_3(3, "墮落城市", 103000000, 0),
            TOWN_4(4, "維多利亞港", 104000000, 0),
            TOWN_5(5, "奇幻村", 105000000, 0),
            TOWN_6(6, "鯨魚號", 120000100, 0),
            TOWN_7(7, "耶雷弗", 130000000, 0),
            TOWN_8(8, "瑞恩", 140000000, 0),
            TOWN_9(9, "天空之城", 200000000, 0),
            TOWN_10(10, "冰原雪域", 211000000, 0),
            TOWN_11(11, "玩具城", 220000000, 0),
            TOWN_12(12, "地球防禦本部", 221000000, 0),
            TOWN_13(13, "童話村", 222000000, 0),
            TOWN_14(14, "水之都", 230000000, 0),
            TOWN_15(15, "神木村", 240000000, 0),
            TOWN_16(16, "武陵", 250000000, 0),
            TOWN_17(17, "靈藥幻境", 251000000, 0),
            TOWN_18(18, "納希沙漠", 260000000, 0),
            TOWN_19(19, "瑪加提亞", 261000000, 0),
            TOWN_20(20, "六條岔道", 104020000, 0),
            TOWN_21(21, "櫻花處", 101050000, 0),
            TOWN_22(22, "萬神殿", 400000000, 0),
            TOWN_23(23, "克里提亞斯", 241000100, 0),
            TOWN_24(24, "天堂", 310070000, 0),
            ;
            private final int id, map, portal;
            private final String name;

            高級網咖時空石(int id, String name, int map, int portal) {
                this.id = id;
                this.name = name;
                this.map = map;
                this.portal = portal;
            }

            public int getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public int getMap() {
                return this.map;
            }

            public int getPortal() {
                return this.portal;
            }

            public static 高級網咖時空石 getById(int id) {
                for (高級網咖時空石 town : 高級網咖時空石.values()) {
                    if (town.getId() != id) continue;
                    return town;
                }
                return null;
            }
        }

        public static String getSelectionInfo(MapleCharacter chr, int npc) {
            String mapselect = "";
            for (高級網咖時空石 gate : 高級網咖時空石.values()) {
                mapselect += "#" + gate.getId() + "#" + gate.getName();
            }
            if ("".equals(mapselect)) {
                mapselect = "#-1# There are no locations you can move to.";
            }
            return mapselect;
        }

        public static int[] getDataIntegers(int id) {
            高級網咖時空石 toenTeleport = 高級網咖時空石.getById(id);
            int[] dataIntegers = {toenTeleport != null ? toenTeleport.getMap() : -1, toenTeleport != null ? toenTeleport.getPortal() : -1};
            return dataIntegers;
        }
    }

    public static class SlideMenu3 {

        public enum 時空石 {

            TOWN_0(0, "弓箭手村", 100000000, 0),
            TOWN_1(1, "魔法森林", 101000000, 0),
            TOWN_2(2, "勇士之村", 102000000, 0),
            TOWN_3(3, "墮落城市", 103000000, 0),
            TOWN_4(4, "維多利亞港", 104000000, 0),
            TOWN_5(5, "奇幻村", 105000000, 0),
            TOWN_6(6, "鯨魚號", 120000100, 0),
            TOWN_7(7, "耶雷弗", 130000000, 0),
            TOWN_8(8, "瑞恩", 140000000, 0),
            TOWN_9(9, "天空之城", 200000000, 0),
            TOWN_10(10, "冰原雪域", 211000000, 0),
            TOWN_11(11, "玩具城", 220000000, 0),
            TOWN_12(12, "地球防禦本部", 221000000, 0),
            TOWN_13(13, "童話村", 222000000, 0),
            TOWN_14(14, "水之都", 230000000, 0),
            TOWN_15(15, "神木村", 240000000, 0),
            TOWN_16(16, "武陵", 250000000, 0),
            TOWN_17(17, "靈藥幻境", 251000000, 0),
            TOWN_18(18, "納希沙漠", 260000000, 0),
            TOWN_19(19, "瑪迦提亞", 261000000, 0),
            TOWN_20(20, "埃德爾斯坦", 310000000, 0),
            TOWN_21(21, "櫻花處", 101050000, 0),
            TOWN_22(22, "萬神殿", 400000000, 0),
            TOWN_23(23, "克里提亞斯", 241000100, 0),
            TOWN_24(24, "天堂", 310070000, 0),
            TOWN_25(25, "野蠻之星", 402000000, 0),
            ;
            private final int id, map, portal;
            private final String name;

            時空石(int id, String name, int map, int portal) {
                this.id = id;
                this.name = name;
                this.map = map;
                this.portal = portal;
            }

            public int getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public int getMap() {
                return this.map;
            }

            public int getPortal() {
                return this.portal;
            }

            public static 時空石 getById(int id) {
                for (時空石 town : 時空石.values()) {
                    if (town.getId() != id) continue;
                    return town;
                }
                return null;
            }
        }

        public static String getSelectionInfo(MapleCharacter player, int npc) {
            String mapselect = "";
            for (時空石 gate : 時空石.values()) {
                mapselect = mapselect + "#" + gate.getId() + "#" + gate.getName();
            }
            if ("".equals(mapselect)) {
                mapselect = "#-1# There are no locations you can move to.";
            }
            return mapselect;
        }

        public static int[] getDataIntegers(int selection) {
            時空石 toenTeleport = 時空石.getById(selection);
            int[] dataIntegers = {toenTeleport != null ? toenTeleport.getMap() : -1, toenTeleport != null ? toenTeleport.getPortal() : -1};
            return dataIntegers;
        }
    }

    public static class SlideMenu4 {

        public enum 武陵道場 {
            /*
             #0# Recover 50% HP
             #1# Recover 100% HP
             #2# MaxHP + 10000 (Duration: 10 min)
             #3# Weapon/Magic ATT + 30 (Duration: 10 min)
             #4# Weapon/Magic ATT + 60 (Duration: 10 min)
             #5# Weapon/Magic DEF + 2500 (Duration: 10 min)
             #6# Weapon/Magic DEF + 4000 (Duration: 10 min)
             #7# Accuracy/Avoidability + 2000 (Duration: 10 min)
             #8# Speed/Jump MAX (Duration: 10 min)
             #9# Attack Speed + 1 (Duration: 10 min)
             */

            BUFF0(0, "Recover 50% HP", 2022855),
            BUFF1(1, "Recover 100% HP", 2022856),
            BUFF2(2, "MaxHP + 10000 (Duration: 10 min)", 2022857),
            BUFF3(3, "Weapon/Magic ATT + 30 (Duration: 10 min)", 2022858),
            BUFF4(4, "Weapon/Magic ATT + 60 (Duration: 10 min)", 2022859),
            BUFF5(5, "Weapon/Magic DEF + 2500 (Duration: 10 min)", 2022860),
            BUFF6(6, "Weapon/Magic DEF + 4000 (Duration: 10 min)", 2022861),
            BUFF7(7, "Accuracy/Avoidability + 2000 (Duration: 10 min)", 2022862),
            BUFF8(8, "Speed/Jump MAX (Duration: 10 min)", 2022863),
            BUFF9(9, "Attack Speed + 1 (Duration: 10 min)", 2022864);
            private final int id, buff;
            private final String name;

            武陵道場(int id, String name, int buff) {
                this.id = id;
                this.name = name;
                this.buff = buff;
            }

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public int getBuff() {
                return buff;
            }

            public static 武陵道場 getById(int id) {
                for (武陵道場 buff : 武陵道場.values()) {
                    if (buff.getId() == id) {
                        return buff;
                    }
                }
                return null;
            }
        }

        public static String getSelectionInfo(MapleCharacter chr, int npc) {
            String buffselect = "";
            for (武陵道場 buffdata : 武陵道場.values()) {
                buffselect += "#" + buffdata.getId() + "# " + buffdata.getName();
            }
            return buffselect;
        }

        public static int[] getDataIntegers(int id) {
            武陵道場 dataInteger = 武陵道場.getById(id);
            int[] dataIntegers = {dataInteger != null ? dataInteger.getBuff() : 武陵道場.BUFF0.getBuff(), 0};
            return dataIntegers;
        }
    }

    public static class SlideMenu5 {

        public enum 萬神殿次元傳點 {

            TOWN_0(0, "六條岔道", 104020000, 2),
            TOWN_1(1, "弓箭手村", 100000000, 0),
            TOWN_2(2, "魔法森林", 101000000, 0),
            TOWN_3(3, "勇士之村", 102000000, 0),
            TOWN_4(4, "墮落城市", 103000000, 0),
            TOWN_5(5, "維多利亞港", 104000000, 0),
            TOWN_6(6, "奇幻村", 105000000, 0),
            TOWN_7(7, "鯨魚號", 120000100, 0),
            TOWN_8(8, "耶雷弗", 130000000, 0),
            TOWN_9(9, "瑞恩", 140000000, 0),
            TOWN_10(10, "天空之城", 200000000, 0),
            TOWN_11(11, "冰原雪域", 211000000, 0),
            TOWN_12(12, "玩具城", 220000000, 0),
            TOWN_13(13, "地球防禦本部", 221000000, 0),
            TOWN_14(14, "童話村", 222000000, 0),
            TOWN_15(15, "水之都", 230000000, 0),
            TOWN_16(16, "神木村", 240000000, 0),
            TOWN_17(17, "武陵", 250000000, 0),
            TOWN_18(18, "靈藥幻境", 251000000, 0),
            TOWN_19(19, "納希沙漠", 260000000, 0),
            TOWN_20(20, "瑪迦提亞", 261000000, 0),
            TOWN_21(21, "埃德爾斯坦", 310000000, 0),
            TOWN_22(22, "櫻花處", 101050000, 0),
            TOWN_23(23, "克里提亞斯", 241000100, 0),
            TOWN_24(24, "天堂", 310070000, 0),
            TOWN_25(25, "萬神殿", 400000000, 0),
            TOWN_26(26, "被遺棄的露營地", 105300000, 0),
            TOWN_27(27, "野蠻之星", 402000000, 0),
            TOWN_28(28, "賽爾尼溫", 410000500, 0, 260),
            TOWN_29(29, "阿爾克斯", 410003010, 0, 270),
            TOWN_30(30, "奧迪溫", 410007000, 0, 275),
//            TOWN_99(99, "未知", 0, 0),
            ;
            private final int id, map, portal, minLevel;
            private final String name;

            萬神殿次元傳點(int id, String name, int map, int portal) {
                this.id = id;
                this.name = name;
                this.map = map;
                this.portal = portal;
                this.minLevel = 1;
            }

            萬神殿次元傳點(int id, String name, int map, int portal, int minLevel) {
                this.id = id;
                this.name = name;
                this.map = map;
                this.portal = portal;
                this.minLevel = minLevel;
            }

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public int getMap() {
                return map;
            }

            public int getPortal() {
                return portal;
            }

            public int getMinLevel() {
                return minLevel;
            }

            public static 萬神殿次元傳點 getById(int id) {
                for (萬神殿次元傳點 town : 萬神殿次元傳點.values()) {
                    if (town.getId() == id) {
                        return town;
                    }
                }
                return null;
            }
        }

        public static String getSelectionInfo(MapleCharacter chr, int npc) {
            String mapselect = "";
            for (萬神殿次元傳點 gate : 萬神殿次元傳點.values()) {
                mapselect += "#" + gate.getId() + "#" + gate.getName();
            }
            if (mapselect == null || "".equals(mapselect)) {
                mapselect = "#-1# There are no locations you can move to.";
            }
            return mapselect;
        }

        public static int[] getDataIntegers(int selection) {
            萬神殿次元傳點 toenTeleport = 萬神殿次元傳點.getById(selection);
            int[] dataIntegers = {toenTeleport != null ? toenTeleport.getMap() : -1, toenTeleport != null ? toenTeleport.getPortal() : -1, toenTeleport != null ? toenTeleport.getMinLevel() : -1};
            return dataIntegers;
        }
    }

    public static class SlideMenu6 {

        public enum belitase_lomometsa的航路 {

            TOWN_0(0, "弓箭手村", 100000000, 0),
            TOWN_1(1, "魔法森林", 101000000, 0),
            TOWN_2(2, "勇士之村", 102000000, 0),
            TOWN_3(3, "墮落城市", 103000000, 0),
            TOWN_4(4, "維多利亞港", 104000000, 0),
            TOWN_5(5, "奇幻村", 105000000, 0),
            TOWN_6(6, "鯨魚號", 120000100, 0),
            TOWN_7(7, "耶雷弗", 130000000, 0),
            TOWN_8(8, "瑞恩", 140000000, 0),
            TOWN_9(9, "天空之城", 200000000, 0),
            TOWN_10(10, "冰原雪域", 211000000, 0),
            TOWN_11(11, "玩具城", 220000000, 0),
            TOWN_12(12, "地球防禦本部", 221000000, 0),
            TOWN_13(13, "童話村", 222000000, 0),
            TOWN_14(14, "水之都", 230000000, 17),
            TOWN_15(15, "神木村", 240000000, 0),
            TOWN_16(16, "武陵", 250000000, 0),
            TOWN_17(17, "靈藥幻境", 251000000, 0),
            TOWN_18(18, "納希沙漠", 260000000, 0),
            TOWN_19(19, "瑪迦提亞", 261000000, 0),
            TOWN_20(20, "埃德爾斯坦", 310000000, 0),
            TOWN_21(21, "櫻花處", 101050000, 0),
            TOWN_22(22, "萬神殿", 400000000, 0),
            TOWN_23(23, "克里提亞斯", 241000100, 0),
            TOWN_24(24, "天堂", 310070000, 0),
            TOWN_25(25, "野蠻之星", 402000000, 0),
            TOWN_26(26, "賽爾尼溫", 410000500, 0, 260),
            TOWN_27(27, "阿爾克斯", 410003010, 0, 270),
            TOWN_28(28, "奧迪溫", 410007000, 0, 275),
            ;
            private final int id, map, portal, minLevel;
            private final String name;

            belitase_lomometsa的航路(int id, String name, int map, int portal) {
                this.id = id;
                this.name = name;
                this.map = map;
                this.portal = portal;
                this.minLevel = 1;
            }

            belitase_lomometsa的航路(int id, String name, int map, int portal, int minLevel) {
                this.id = id;
                this.name = name;
                this.map = map;
                this.portal = portal;
                this.minLevel = minLevel;
            }

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public int getMap() {
                return map;
            }

            public int getPortal() {
                return portal;
            }

            public int getMinLevel() {
                return minLevel;
            }

            public static belitase_lomometsa的航路 getById(int id) {
                for (belitase_lomometsa的航路 town : belitase_lomometsa的航路.values()) {
                    if (town.getId() == id) {
                        return town;
                    }
                }
                return null;
            }
        }

        public static String getSelectionInfo(MapleCharacter chr, int npc) {
            String mapselect = "";
            for (belitase_lomometsa的航路 gate : belitase_lomometsa的航路.values()) {
                mapselect += "#" + gate.getId() + "#" + gate.getName();
            }
            if ("".equals(mapselect)) {
                mapselect = "#-1# There are no locations you can move to.";
            }
            return mapselect;
        }

        public static int[] getDataIntegers(int selection) {
            belitase_lomometsa的航路 toenTeleport = belitase_lomometsa的航路.getById(selection);
            int[] dataIntegers = {toenTeleport != null ? toenTeleport.getMap() : -1, toenTeleport != null ? toenTeleport.getPortal() : -1, toenTeleport != null ? toenTeleport.getMinLevel() : -1};
            return dataIntegers;
        }
    }
}
