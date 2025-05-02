package Net.server.events;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// UI.wz\UIWindow5.img\dimensionalMirror\
public enum DimensionMirrorEvent {

    PQ0(0, 5, 925020000, "武陵道場", "挑戰武陵道場後確認自己的力量吧。", 105, 0, 1207, Arrays.asList(1082392, 1082393, 1082394), false),
    PQ1(1, 2, 913050010, "楓之谷聯盟 會議場", "為了楓之谷聯盟的誕生進行大陸會議，然後會正式開始跟魔法師的戰爭。", 75, 0, 7998, Arrays.asList(1142769, 1142804, 3015030, 3700350, 3017016), false),
    PQ2(2, 0, 262010000, "試煉之塔", "你要移動到試煉之塔嗎？", 120, 0, 57616, Arrays.asList(1132291, 1032253, 1122320, 1012539, 1022253, 1142321), false),
    PQ3(3, 0, 957000000, "進化系統研究所", "體驗進化系統的功能吧。", 105, 1801, 1819, Arrays.asList(1162013, 4001832, 2431935, 2431936), false),
    PQ4(4, 0, 940020000, "次元入侵", "次元的侵略者是誰呢？", 140, 0, 31836, Arrays.asList(1082488, 1082489, 1142527, 1142528, 1142529, 1142530), false),
    PQ5(5, 0, 301000000, "克林森烏德城", "從克拉奇亞飛來的訊息… 他的內容是？", 130, 31241, 31265, Arrays.asList(1142619, 2431935, 2431936), false),
    PQ6(6, 0, 302000000, "次元圖書館", "在次元圖書館體驗過去的故事吧。", 100, 0, 32670, Arrays.asList(1122263, 2431892), false),
    PQ7(7, 1, 910002000, "進行組隊任務入場", "一起的話趣味也 UP~組隊後享受特別的任務吧。", 50, 0, 7887, Arrays.asList(1003762, 1022073, 1132013, 1022175, 1902048, 2432131), true),
    PQ8(8, 0, 992000000, "the seed", "征服無法控制的海底之塔 the seed後幫助愛麗西亞的思念吧。", 140, 42009, 7839, Arrays.asList(2028263, 2432465), false),
    PQ9(9, 3, 100000004, "皇家神獸學院", "體驗楓之谷第一個皇家神獸學院任務吧！", 100, 32707, 33041, Arrays.asList(1182079, 3015119, 3010875, 2432776, 2432788), false),
    PQ10(10, 0, 951000000, "怪物公園REBORN", "驚人的經驗值和特別的星期補償正在等你喔！", 105, 0, 7900, Arrays.asList(2434746, 2434747, 2434748, 2434749, 2434750, 2434751, 2434745), false),
    PQ11(11, 0, 105200000, "露塔必思", "要擊殺復活的封印守護者來阻擋他們不能再使用黑暗之力。", 125, 300000, 30028, Arrays.asList(1003715, 1003716, 1003717, 1003718), false),
    PQ12(12, 0, 970072200, "烏勒斯", "要不要挑戰看看破王烏勒斯的威嚴？", 100, 0, 33553, Arrays.asList(3015279, 2434509, 2434389, 3700334, 1142879), true),
    PQ13(13, 0, 956100000, "鬼怪公園", "狩獵鬼魂，淨化童話村的邪惡氣息吧。", 125, 0, 30201, Arrays.asList(3700598, 3018450, 5010181, 5010182, 5010183), false),
    PQ500(500, 0, 865000000, "克梅勒茲共和國", "你要移動到克梅勒茲共和國嗎？", 140, 17600, 17699, Arrays.asList(4310100, 1003984, 1052673, 1072874, 1082559), false),
    PQ504(504, 0, 807000000, "楓葉丘陵", "你要移動到楓葉丘陵嗎？", 10, 0, 7090, Collections.emptyList(), false),
    PQ505(505, 0, 749080900, "阿里山", "你要移動到阿里山嗎？", 33, 55234, 55255, List.of(2434004), false),
    PQ507(507, 0, 811000999, "比叡山本堂", "你要移動到比叡山嗎？", 140, 58913, 58971, Arrays.asList(2630594, 3010864, 2432758), false),
    PQ508(508, 0, 800000000, "菇菇神社", "移動到擁有奇妙故事的菇菇神社吧。", 100, 58720, 7017, Arrays.asList(1102887, 2047983, 2047982, 2047981, 4001832), false),
    PQ509(509, 0, 813300000, "降魔的十字旅團", "你要移動到降魔的十字旅團嗎？", 60, 0, 58420, Arrays.asList(1113200, 1113203), false),
    PQ510(510, 0, 867113100, "艾芙特領域", "早晚都隨隨便便，而且天氣也隨心情變化的這裡！我為什麼會在這裡呢？！我要回到楓之谷世界啦！", 75, 63020, 63255, Arrays.asList(1202237, 1202238, 1202239, 1202240), false),
    PQ511(511, 0, 867200110, "阿爾布本營地", "阿爾布的村民正在等您呢。", 33, 64010, 64154, Arrays.asList(1005191, 2439151, 1143117, 3018143, 1143118, 2028372, 2048752), false),
    PQ512(512, 0, 867151000, "楓之谷探險隊", "前往神秘又危險的未知之地展開冒險吧！", 101, 65630, 65665, Arrays.asList(1202287, 2048760, 2672140, 2672141), false),
    ;

    private final String name;
    private final String info;
    private final int id, pos, mapID, needQuestID, questID, limitLevel;
    private final List<Integer> rewards;
    private final boolean team;

    DimensionMirrorEvent(final int id, final int pos, final int mapID, final String name, final String info, final int limitLevel, final int needQuestID, final int questID, final List<Integer> rewards, final boolean team) {
        this.id = id;
        this.pos = pos;
        this.mapID = mapID;
        this.name = name;
        this.info = info;
        this.limitLevel = limitLevel;
        this.needQuestID = needQuestID;
        this.questID = questID;
        this.rewards = rewards;
        this.team = team;
    }

    public final boolean isTeam() {
        return this.team;
    }

    public final int getPos() {
        return this.pos;
    }

    public final int getMapID() {
        return this.mapID;
    }

    public final int getNeedQuestID() {
        return this.needQuestID;
    }

    public final int getQuestID() {
        return this.questID;
    }

    public final String getInfo() {
        return this.info;
    }

    public final String getName() {
        return this.name;
    }

    public final List<Integer> getRewards() {
        return this.rewards;
    }

    public final int getID() {
        return this.id;
    }

    public final int getLimitLevel() {
        return this.limitLevel;
    }
}
