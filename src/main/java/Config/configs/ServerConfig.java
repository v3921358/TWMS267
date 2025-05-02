package Config.configs;

import Net.server.maps.MapleQuickMove;
import tools.config.Property;

import java.util.*;

public final class ServerConfig {


    // MVP 花費金額倍率 可升級
    @Property(key = "mvp.amount.rate", defaultValue = "1.0")
    public static float MVP_AMOUNT_RATE;

    /**
     * 是否登入抬頭提醒
     */
    @Property(key = "WellCome.Message", defaultValue = "Wellcome")
    public static String WELLCOME_MESSAGE;
    /**
     * input data path
     */
    @Property(key = "CLIENT.DATA.INPUT.PATH", defaultValue = "")
    public static String Client_DATA_INPUT_PATH;
    /**
     * 商城跑馬燈
     */
    @Property(key = "CashShop.Message", defaultValue = "Wellcome")
    public static String Cash_Shop_Message;
    /**
     * 是否卷軸使用提醒
     */
    @Property(key = "Scroll.Notice.GM", defaultValue = "false")
    public static boolean SCROLL_NOTICE;
    /**
     * 是否掉落提醒
     */
    @Property(key = "DROP.Notice.GM", defaultValue = "false")
    public static boolean DROP_NOTICE_ON_OFF;
    /**
     * 是否關閉GUI
     */
    @Property(key = "World.Config.GUI", defaultValue = "false")
    public static boolean On_Off_GUI;

    /**
     * 是否開啟SKILL施展提醒
     */
    @Property(key = "Skill.Notice.GM", defaultValue = "false")
    public static boolean SKILL_NOTICE_ON_OFF;

    /*
     * 關於封包加密
     */
    @Property(key = "encrypt", defaultValue = "true")
    public static boolean ENCRYPT;

    /**
     * 是否僅允許GM登入
     */
    @Property(key = "world.server.onlyadmin", defaultValue = "false")
    public static boolean WORLD_ONLYADMIN;

    /**
     * 腳本路徑
     */

    @Property(key = "world.server.scriptspath", defaultValue = "scripts")
    public static String WORLD_SCRIPTSPATH;
    @Property(key = "world.server.scriptspath2", defaultValue = "scripts_custom")
    public static String WORLD_SCRIPTSPATH2;

    /**
     * 是否緩存NPC腳本
     */
    @Property(key = "world.server.cachescript", defaultValue = "false")
    public static boolean WORLD_CACHE_SCRIPT;

    // 登入伺服器設置
    /**
     * 伺服器補丁版本號
     */

    @Property(key = "login.server.minor", defaultValue = "1")
    public static String MapleMinor;
    /**
     * 伺服器名稱
     */
    @Property(key = "login.server.name", defaultValue = "新楓之谷")
    public static String SERVER_NAME;
    /**
     * 頻道選擇界面公告
     */
    @Property(key = "login.server.eventmessage", defaultValue = "")
    public static String LOGIN_EVENTMESSAGE;
    /**
     * 遊戲置頂公告
     */
    @Property(key = "login.server.message", defaultValue = "祝你遊戲愉快！")
    public static String EVENT_MSG;
    /**
     * 伺服器顯示名稱：艾麗亞、普力特等
     */
    @Property(key = "login.server.flag", defaultValue = "0")
    public static byte WORLD_ID;
    /**
     * 伺服器運行狀態
     */
    @Property(key = "login.server.status", defaultValue = "2")
    public static byte LOGIN_SERVERSTATUS;
    /**
     * 最大線上人數
     */
    @Property(key = "login.server.userlimit", defaultValue = "5")
    public static int LOGIN_USERLIMIT;
    /**
     * 單頻道預設最大人數
     */
    @Property(key = "login.server.defaultuserlimit", defaultValue = "0")
    public static int LOGIN_DEFAULTUSERLIMIT;

    /**
     * 是否開啟自動註冊
     */
    @Property(key = "world.autoregister", defaultValue = "true")
    public static boolean AUTORIGISTER;

    // 其他設置
    /**
     * 禁止獲得經驗值
     */
    @Property(key = "world.bangainexp", defaultValue = "false")
    public static boolean WORLD_BANGAINEXP;

    /**
     * 禁止所有交易
     */
    @Property(key = "world.bantrade", defaultValue = "false")
    public static boolean WORLD_BANTRADE;

    /**
     * 禁止怪物掉寶
     */
    @Property(key = "world.bandropitem", defaultValue = "false")
    public static boolean WORLD_BANDROPITEM;

    /**
     * 停用潛能系統
     */
    @Property(key = "world.disablepotential", defaultValue = "false")
    public static boolean DISABLE_POTENTIAL;

    /**
     * 玩家人氣小於0禁止穿戴裝備
     */
    @Property(key = "world.equipcheckfame", defaultValue = "false")
    public static boolean WORLD_EQUIPCHECKFAME;

    /**
     * 所有裝備可鑲嵌星岩
     */
//    @Property(key = "world.allsocket", defaultValue = "false")
    public static boolean ALL_SOCKET = false;

    /**
     * 啟用傷害校驗系統
     */
    @Property(key = "server.verifydamage", defaultValue = "false")
    public static boolean SERVER_VERIFY_DAMAGE;
    /**
     * 啟用GUI更新角色
     */
    @Property(key = "server.updatePlayerInGUI", defaultValue = "true")
    public static boolean updatePlayerInGUI;

    /**
     * 預設的裝備可剪刀次數
     */
    @Property(key = "world.defaultcuttable", defaultValue = "-1")
    public static short DEFAULT_CUTTABLE;

    /**
     * 登入連接埠
     */
    @Property(key = "login.server.port", defaultValue = "8484")
    public static short LOGIN_PORT;

    /**
     * 登入連接埠_備用
     */
    @Property(key = "login.server.port_bak", defaultValue = "5000")
    public static short LOGIN_PORT_備用;
    /**
     * /**
     * <p>
     * 商城連接埠
     */
    @Property(key = "cash.server.port", defaultValue = "8600")
    public static short CASH_PORT;
    /**
     * 聊天連接埠
     */
    @Property(key = "chat.server.port", defaultValue = "8283")
    public static short CHAT_PORT;
    /**
     * 聊天連接埠
     */
    @Property(key = "packet.server.port", defaultValue = "7574")
    public static short SEND_PACKET_PORT;
    /**
     * 拍賣場連接埠
     */
    @Property(key = "auction.server.port", defaultValue = "8700")
    public static short AUCTION_PORT;
    /**
     * 頻道數量
     */
    @Property(key = "channel.server.ports", defaultValue = "5")
    public static int CHANNELS_PER_WORLD;
    /**
     * 頻道連接埠
     */
    @Property(key = "channel.server.port.start", defaultValue = "7575")
    public static short CHANNEL_START_PORT;


    // 遊戲倍率設置
    /**
     * 基礎經驗倍率設置
     */
    @Property(key = "channel.rate.baseexp", defaultValue = "100")
    public static int CHANNEL_RATE_BASEEXP;
    /**
     * 伺服器經驗倍率設置
     */
    @Property(key = "channel.rate.exp", defaultValue = "10")
    public static int CHANNEL_RATE_EXP;
    /**
     * 楓幣倍率設置
     */
    @Property(key = "channel.rate.meso", defaultValue = "10")
    public static int CHANNEL_RATE_MESO;
    /**
     * 掉寶概率設置
     */
    @Property(key = "channel.rate.drop", defaultValue = "10")
    public static int CHANNEL_RATE_DROP;
    /**
     * 特殊道具全域掉寶率
     */
    @Property(key = "channel.rate.globaldrop", defaultValue = "10")
    public static int CHANNEL_RATE_GLOBALDROP;
    /**
     * 專業技能經驗倍數
     */
    @Property(key = "channel.rate.trait", defaultValue = "1")
    public static int CHANNEL_RATE_TRAIT;
    /**
     * 潛能等級改變幾率 1~1000
     */
    @Property(key = "channel.rate.potentiallevel", defaultValue = "1")
    public static int CHANNEL_RATE_POTENTIALLEVEL;

    // 玩家參數設置
    /**
     * 最大能力值上限
     */
    @Property(key = "channel.player.maxap", defaultValue = "30000")
    public static short CHANNEL_PLAYER_MAXAP;
    /**
     * 最大HP上限
     */
    @Property(key = "channel.player.maxhp", defaultValue = "500000")
    public static int CHANNEL_PLAYER_MAXHP;
    /**
     * 最大MP上限
     */
    @Property(key = "channel.player.maxmp", defaultValue = "500000")
    public static int CHANNEL_PLAYER_MAXMP;
    /**
     * 持有楓幣上限
     */
    @Property(key = "channel.player.maxmeso", defaultValue = "99999999999")
    public static long CHANNEL_PLAYER_MAXMESO;
    /**
     * 傷害上限校驗
     */
    @Property(key = "channel.player.damageLimit", defaultValue = "10000000000")
    public static long DAMAGE_LIMIT;
    /**
     * 新手出生地圖
     */
    @Property(key = "channel.player.beginnermap", defaultValue = "10000")
    public static int CHANNEL_PLAYER_BEGINNERMAP;

    /**
     * 最大可創建角色數量
     */
    @Property(key = "channel.player.maxcharacters", defaultValue = "6")
    public static byte CHANNEL_PLAYER_MAXCHARACTERS;
    /**
     * 每日免費復活次數
     */
    @Property(key = "channel.player.resufreecount", defaultValue = "5")
    public static int CHANNEL_PLAYER_RESUFREECOUNT;

    /**
     * 是否自動完成缺少腳本的腳本任務（此開關僅對GM角色有效，普通角色鎖定為true）
     */
    @Property(key = "channel.player.autocompletequest", defaultValue = "false")
    public static boolean CHANNEL_PLAYER_AUTOCOMPLETEQUEST;

    /**
     * 是否所有技能無冷卻時間
     */
    @Property(key = "channel.player.disablecooldown", defaultValue = "false")
    public static boolean CHANNEL_PLAYER_DISABLECOOLDOWN;

    // 遊戲參數設置
    /**
     * PQLOG刷新時間
     */
    @Property(key = "world.refreshtime", defaultValue = "0")
    public static int WORLD_REFRESH_TIME;
    /**
     * 排行榜刷新時間間隔（單位：分鐘）
     */
    @Property(key = "world.refreshrank", defaultValue = "120")
    public static int WORLD_REFRESHRANK;
    /**
     * 開啟自動封號
     */
    @Property(key = "world.autoban", defaultValue = "true")
    public static boolean WORLD_AUTOBAN;
    /**
     * 禁止使用的技能列表
     */
    @Property(key = "world.blockskills", defaultValue = "")
    public static String WORLD_BLOCKSKILLS;
    /**
     * 禁止開放的職業
     */
    @Property(key = "world.closejobs", defaultValue = "")
    public static String WORLD_CLOSEJOBS;
    /**
     * 隱藏的NPC
     */
    @Property(key = "world.hidenpcs", defaultValue = "{}")
    public static String WORLD_HIDENPCS;
    public static final Map<Integer, Set<Integer>> WORLD_HIDENPCS_MAP = new HashMap<>();
    /**
     * 是否開啟PVP
     */
    @Property(key = "channel.server.openpvp", defaultValue = "false")
    public static boolean CHANNEL_OPENPVP;
    /**
     * PVP地圖列表
     */
    @Property(key = "channel.server.pvpmaps", defaultValue = "910000018")
    public static String CHANNEL_PVPMAPS;
    /**
     * 開啟自由市場聊天訊息用小黑板顯示
     */
    @Property(key = "channel.server.chalkboard", defaultValue = "false")
    public static boolean CHANNEL_CHALKBOARD;
    /**
     * 創建公會所需楓幣
     */
    @Property(key = "channel.server.createguildcost", defaultValue = "5000000")
    public static int CHANNEL_CREATEGUILDCOST;
    /**
     * 是否開啟商城道具可用楓點購買
     */
    @Property(key = "channel.server.enablepointsbuy", defaultValue = "true")
    public static boolean CHANNEL_ENABLEPOINTSBUY;
    /**
     * 是否關閉角色DEBUFF
     */
    @Property(key = "channel.server.applyplayerdebuff", defaultValue = "false")
    public static boolean CHANNEL_APPLYPLAYERDEBUFF;
    /**
     * 是否關閉怪物BUFF(怪物)
     */
    @Property(key = "channel.server.applymonsterstatus", defaultValue = "false")
    public static boolean CHANNEL_APPLYMONSTERSTATUS;
    /**
     * 事件腳本設置
     */
    @Property(key = "channel.server.events", defaultValue = "")
    public static String CHANNEL_EVENTS;

    /**
     * 地圖刷新菁英怪物所需殺怪數量
     */
    @Property(key = "channel.monster.elitecount", defaultValue = "500")
    public static int ELITE_COUNT;

    // 資料庫設置
    @Property(key = "db.ip", defaultValue = "localhost")
    public static String DB_IP;

    @Property(key = "db.port", defaultValue = "3306")
    public static String DB_PORT;

    @Property(key = "db.name", defaultValue = "tms")
    public static String DB_NAME;

    @Property(key = "db.user", defaultValue = "root")
    public static String DB_USER;

    @Property(key = "db.password", defaultValue = "")
    public static String DB_PASSWORD;

    //    @Property(key = "db.timeout", defaultValue = "1800000")
    public static int DB_TIMEOUT = 300000;

    //    @Property(key = "db.MinPoolSize", defaultValue = "20")
    public static int DB_MINPOOLSIZE = 20;

    //    @Property(key = "db.InitialPoolSize", defaultValue = "30")
    public static int DB_INITIALPOOLSIZE = 30;

    //    @Property(key = "db.MaxPoolSize", defaultValue = "400")
    public static int DB_MAXPOOLSIZE = 1000;

    //    @Property(key = "db.setuppath", defaultValue = "D:\\MySQL\\MySQL Server 5.6")
    public static String DB_SETUPPATH;

    //    @Property(key = "db.backuppath", defaultValue = "D:\\數據庫備份")
    public static String DB_BACKUPPATH;

    //    @Property(key = "db.autobackuptime", defaultValue = "120")
    public static int DB_AUTOBACKUPTIME;

    @Property(key = "check.cheatitemexcludes", defaultValue = "")
    public static String CHEAT_ITEM_EXCLUDES;

    public static final List<Integer> CHEAT_ITEM_EXCLUDES_LIST = new ArrayList<>();

    @Property(key = "pet.defaultflag", defaultValue = "7807")
    public static short PET_DEFAULT_FLAG;

    @Property(key = "familiar.sealcost", defaultValue = "200")
    public static int FAMILIAR_SEAL_COST;

    @Property(key = "world.limitednames", defaultValue = "royalFace,animaOn")
    public static String WORLD_LIMITEDNAMES;
    public static final List<String> WORLD_LIMITEDNAMES_LIST = new ArrayList<>();

    @Property(key = "starforce.maplepoint.amount", defaultValue = "9")
    public static long SF_MP_AMOUNT;

    @Property(key = "starforce.maplepointsafe.amount", defaultValue = "50")
    public static long SF_MP_SAFE_AMOUNT;

    @Property(key = "starforce.curse.enable", defaultValue = "true")
    public static boolean SF_ENABLE_CURSE;

    @Property(key = "map.maxburningfieldstep", defaultValue = "10")
    public static int MAX_BREAKTIMEFIELD_STEP;

    @Property(key = "inventory.cancutitems", defaultValue = "")
    public static String CAN_CUT_ITEMS;

    public static final List<Integer> CAN_CUT_ITEMS_LIST = new ArrayList<>();

    @Property(key = "inventory.accshareitems", defaultValue = "")
    public static String ACCOUNT_SHARE_ITEMS;

    public static final List<Integer> ACCOUNT_SHARE_ITEMS_LIST = new ArrayList<>();

    @Property(key = "tespia", defaultValue = "false")
    public static boolean TESPIA;

    @Property(key = "testpia.multiplayer", defaultValue = "false")
    public static boolean MULTIPLAYER_TEST;

    @Property(key = "map.effect", defaultValue = "0")
    public static int MAP_EFFECT;

    //    @Property(key = "world.jmssoulweaponsys", defaultValue = "true")
    public static boolean JMS_SOULWEAPON_SYSTEM = true;

    @Property(key = "inventory.itemmaxslot", defaultValue = "{}")
    public static String ITEM_MAXSLOT;
    public static final Map<Integer, Short> ITEM_MAXSLOT_MAP = new HashMap<>();

    public static final List<MapleQuickMove> QUICK_MOVE_LIST = new LinkedList<>();

    @Property(key = "world.createcharburning", defaultValue = "0")
    public static int CREATE_CHAR_BURNING;

    @Property(key = "world.createcharburningdays", defaultValue = "7")
    public static int CREATE_CHAR_BURNING_DAYS;

    @Property(key = "noEncryptHosts", defaultValue = "")
    public static String noEncryptHosts;

    public static List<String> noEncryptHost_List = new LinkedList<>();

    @Property(key = "mobPointMinLv", defaultValue = "150")
    public static int mobPointMinLv;

    @Property(key = "ptyPointModifier", defaultValue = "1")
    public static int ptyPointModifier;

    @Property(key = "mileageDailyLimitMax", defaultValue = "100")
    public static int mileageDailyLimitMax;

    @Property(key = "mileageMonthlyLimitMax", defaultValue = "500")
    public static int mileageMonthlyLimitMax;

    @Property(key = "mileageAsMaplePoint", defaultValue = "false")
    public static boolean mileageAsMaplePoint;

    @Property(key = "partyQuestRevive", defaultValue = "false")
    public static boolean partyQuestRevive;

    @Property(key = "mobPointNeedPickup", defaultValue = "true")
    public static boolean mobPointNeedPickup;

    @Property(key = "world.player.enablerebornbuff", defaultValue = "false")
    public static boolean EnableRebornBuff;

    @Property(key = "familiarIncDAMrHard", defaultValue = "false")
    public static boolean familiarIncDAMrHard;

    @Property(key = "goldenAppleFragmentNoTimeLimit", defaultValue = "false")
    public static boolean goldenAppleFragmentNoTimeLimit;

    @Property(key = "KMS_NirvanaFlameTier", defaultValue = "false")
    public static boolean KMS_NirvanaFlameTier;

    // 道場怪物設置
    @Property(key = "channel.server.dojoMobMaxHpR", defaultValue = "100")
    public static int dojoMobMaxHpR;
    @Property(key = "channel.server.dojoMobAtkR", defaultValue = "100")
    public static int dojoMobAtkR;
    @Property(key = "channel.server.dojoMobDefenseRateR", defaultValue = "100")
    public static int dojoMobDefenseRateR;

    @Property(key = "HideBulbQuest", defaultValue = "false")
    public static boolean HideBulbQuest;

    @Property(key = "BlockChairs", defaultValue = "")
    public static String BLOCK_CHAIRS;
    public static List<Integer> BLOCK_CHAIRS_SET = new LinkedList<>();

    @Property(key = "defaultDamageSkinSlot", defaultValue = "30")
    public static int defaultDamageSkinSlot;

    @Property(key = "min_donate", defaultValue = "100")
    public static int MIN_DONATE;

    @Property(key = "TeachCost", defaultValue = "0,0,0,0,5000000,6000000,7000000,8000000,9000000,10000000")
    public static String TeachCostData;

    public static List<Integer> TeachCost = new LinkedList<>();

    @Property(key = "map.rune.close", defaultValue = "false")
    public static boolean RUNE_CLOSE;

    @Property(key = "defaultMeso", defaultValue = "true")
    public static boolean ADD_DEFAULT_MESO;

    @Property(key = "defaultMaxSlot", defaultValue = "false")
    public static boolean DEFAULT_MAX_SLOT;


    @Property(key = "ServerSpawnMobSec", defaultValue = "7")
    public static int ServerSpawnMobSec;
}
