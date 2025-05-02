// let mBoss = exports;

/**
 * name 名字
 * index img中的圖片index
 * difficulty 用於顯示UI上可挑戰的難度
 * minPlayers 隊伍最少人數
 * maxPlayers 隊伍最多人數
 * maxDayLimit 每天最大入場次數
 * maxWeekLimit 每週最大入場次數
 * onlyOne 副本是否頻道唯一
 */
const BossList = {
    Balog: {
        name: "巴洛古",
        index: 0,
        event: "boss_balog_",
        difficulty: [0],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Zakum: {
        name: "炎魔",
        index: 1,
        event: "boss_zakum_",
        difficulty: [0, 1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Hontale: {
        name: "闇黑龍王",
        index: 2,
        event: "boss_hontale_",
        difficulty: [0, 1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Hillah: {
        name: "希拉",
        index: 3,
        event: "boss_hillah_",
        difficulty: [1, 2],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Pierre: {
        name: "比艾樂",
        index: 4,
        event: "boss_pierre_",
        difficulty: [1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Banban: {
        name: "斑斑",
        index: 5,
        event: "boss_banban_",
        difficulty: [1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Bloody: {
        name: "血腥皇后",
        index: 6,
        event: "boss_bloody_",
        difficulty: [1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Bellum: {
        name: "貝倫",
        index: 7,
        event: "boss_bellum_",
        difficulty: [1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Vanleon: {
        name: "凡雷恩",
        index: 8,
        event: "boss_vanleon_",
        difficulty: [0, 1, 2],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Akayrum: {
        name: "阿卡伊農",
        index: 9,
        event: "boss_akayrum_",
        difficulty: [0, 1],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Magnus: {
        name: "梅格耐斯",
        index: 10,
        event: "boss_magnus_",
        difficulty: [0, 1, 2],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Pinkbeen: {
        name: "皮卡啾",
        index: 11,
        event: "boss_pinkbeen_",
        difficulty: [1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Shinas: {
        name: "西格諾斯",
        index: 12,
        event: "boss_shinas_",
        difficulty: [1, 2],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Suu: {
        name: "史烏",
        index: 13,
        event: "boss_suu_",
        difficulty: [1, 2, 4],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    // Ursus: {
    //     name: "烏勒斯",
    //     index: 14,
    //     event: "boss_ursus_",
    //     difficulty: [0],
    //     minPlayers: 1,
    //     maxPlayers: 6,
    //     maxDayLimit: 1,
    //     maxWeekLimit: 1,
    //     onlyOne: false
    // },
    Demian: {
        name: "戴米安",
        index: 15,
        event: "boss_demian_",
        difficulty: [1, 2],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Beidler: {
        name: "培羅德",
        index: 16,
        event: "boss_beidler_",
        difficulty: [1],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Ranmaru: {
        name: "森蘭丸",
        index: 17,
        event: "boss_ranmaru_",
        difficulty: [1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    PrincessNo: {
        name: "濃姬",
        index: 18,
        event: "boss_princessno_",
        difficulty: [1],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Lucid: {
        name: "露希妲",
        index: 19,
        event: "boss_lucid_",
        difficulty: [0, 1, 2],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Caoong: {
        name: "卡翁",
        index: 21,
        event: "boss_caoong_",
        difficulty: [1],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Papulatus: {
        name: "拉圖斯",
        index: 22,
        event: "boss_papulatus_",
        difficulty: [0, 1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Will: {
        name: "威爾",
        index: 23,
        event: "boss_will_",
        difficulty: [0, 1, 2],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    JinHillah: {
        name: "真‧希拉",
        index: 24,
        event: "boss_jinhillah_",
        difficulty: [1, 2],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    BlackMage: {
        name: "黑魔法師",
        index: 25,
        event: "boss_blackmage_",
        difficulty: [2, 4],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Dusk: {
        name: "戴斯克",
        index: 26,
        event: "boss_dusk_",
        difficulty: [1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Dunkel: {
        name: "頓凱爾",
        index: 27,
        event: "boss_dunkel_",
        difficulty: [1, 2],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Seren: {
        name: "受選的賽蓮",
        index: 28,
        event: "boss_seren_",
        difficulty: [1, 2, 4],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Slime: {
        name: "守護天使綠水靈",
        index: 29,
        event: "boss_slime_",
        difficulty: [1, 3],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Kalos: {
        name: "監視者卡洛斯",
        index: 30,
        event: "boss_kalos_",
        difficulty: [0, 1, 3, 4],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
    Karing: {
        name: "咖凌",
        index: 31,
        event: "boss_karing_",
        difficulty: [0, 1, 2, 4],
        minPlayers: 1,
        maxPlayers: 6,
        maxDayLimit: 1,
        maxWeekLimit: 1,
        onlyOne: false
    },
}

/**
 * 難度 與BossList中的difficulty關係對應 用於拼接event_name
 */
const BossDifficulty = ["easy", "normal", "hard", "chaos", "extreme"];


export { BossList, BossDifficulty }
