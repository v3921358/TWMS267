package Client;

import Client.anticheat.CheatTracker;
import Client.anticheat.ReportType;
import Client.force.MapleForceAtom;
import Client.force.MapleForceFactory;
import Client.hexa.MapleHexaSkill;
import Client.hexa.MapleHexaStat;
import Client.inventory.*;
import Client.inventory.MapleRing.RingComparator;
import Client.skills.*;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.stat.DeadDebuff;
import Client.stat.MapleHyperStats;
import Client.stat.PlayerStats;
import Client.status.MonsterStatus;
import Client.status.MonsterStatusEffect;
import Config.configs.Config;
import Config.configs.FishingConfig;
import Config.configs.ServerConfig;
import Config.constants.*;
import Config.constants.ItemConstants.類型;
import Config.constants.enums.UIReviveType;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.初心者;
import Config.constants.skills.冒險家_技能群組.type_劍士.劍士;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.拳霸;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Database.tools.SqlTool;
import Net.server.*;
import Net.server.Timer.CoolDownTimer;
import Net.server.Timer.MapTimer;
import Net.server.Timer.WorldTimer;
import Net.server.buffs.MapleStatEffect;
import Net.server.buffs.MapleStatEffect.CancelEffectAction;
import Net.server.carnival.MapleCarnivalChallenge;
import Net.server.carnival.MapleCarnivalParty;
import Net.server.cashshop.CashShop;
import Net.server.commands.PlayerRank;
import Net.server.life.*;
import Net.server.maps.*;
import Net.server.maps.events.Event_PyramidSubway;
import Net.server.maps.field.ActionBarField.MapleFieldActionBar;
import Net.server.quest.MapleQuest;
import Net.server.reward.RewardDropEntry;
import Net.server.shop.*;
import Net.server.shops.HiredFisher;
import Net.server.shops.IMaplePlayerShop;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import Opcode.Opcode.MessageOpcode;
import Packet.*;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;
import Plugin.script.ScriptManager;
import Plugin.script.binding.ScriptEvent;
import Server.BossEventHandler.Seren;
import Server.channel.ChannelServer;
import Server.login.JobType;
import Server.world.*;
import Server.world.guild.MapleGuild;
import Server.world.guild.MapleGuildCharacter;
import Server.world.messenger.MapleMessenger;
import Server.world.messenger.MapleMessengerCharacter;
import Server.world.messenger.MessengerRankingWorker;
import SwordieX.client.character.Burning;
import SwordieX.client.character.CharacterStat;
import SwordieX.client.character.avatar.AvatarData;
import SwordieX.client.character.avatar.AvatarLook;
import SwordieX.client.character.keys.FuncKeyMap;
import SwordieX.client.character.keys.Keymapping;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import SwordieX.client.party.PartyResult;
import connection.OutPacket;
import connection.Packet;
import connection.packet.FieldPacket;
import connection.packet.OverseasPacket;
import connection.packet.UserRemote;
import connection.packet.WvsContext;
import SwordieX.field.ClockPacket;
import SwordieX.field.fieldeffect.FieldEffect;
import SwordieX.overseas.extraequip.ExtraEquipResult;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.*;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MapleCharacter extends AnimatedMapleMapObject implements Serializable {

    private static final long serialVersionUID = 845748950829L;

    private static final Logger log = LoggerFactory.getLogger(MapleCharacter.class);
    private static int gachexp;
    private static boolean changed_soulcollection;
    private final List<MapleProcess> Process = new LinkedList<>();
    private final AtomicInteger cMapCount = new AtomicInteger(1);
    private final AtomicInteger vCoreSkillIndex = new AtomicInteger(0);
    private final AtomicLong exp = new AtomicLong(); //新的經驗結果
    private final AtomicLong meso = new AtomicLong();
    private final AtomicLong killMonsterExp = new AtomicLong(0);
    private final transient PlayerObservable playerObservable = new PlayerObservable(this);
    private final Map<Integer, MapleQuestStatus> quests;
    private final Map<Integer, Map<Integer, MapleQuestStatus>> worldAccountQuests = null;
    private final Map<Integer, Map<Integer, String>> worldAccountQuestInfo = null;
    private final Map<Integer, SkillEntry> skills; //角色技能
    private final Map<Integer, SkillEntry> linkSkills;
    private final Map<Integer, Pair<Integer, SkillEntry>> sonOfLinkedSkills;
    private final transient ReentrantLock effectLock = new ReentrantLock();
    private final PlayerStats stats;
    private final MapleCharacterCards characterCard;
    private final EnumMap<MapleTraitType, MapleTrait> traits;
    //聖騎士攻擊的屬性
    private final transient Element elements = null;
    private final Point flamePoint = null; //設置需要移動的坐標位置
    // 上次保存時間
    private final Long lastSavetime = 0L;
    private final List<Integer> damSkinList;
    private final List<MapleSummon> allLinksummon = new ArrayList<>();
    // Force Atom 記錄攻擊次數
    private final Map<Integer, Integer> atomsAttackRecords = new HashMap<>();
    private final ReentrantLock wreckagesLock = new ReentrantLock();
    private final List<Triple<Integer, Long, Point>> evanWreckages = new LinkedList<>();
    private final ReentrantLock cooldownLock = new ReentrantLock();
    private final CalcDamage calcDamage = new CalcDamage();
    private final Map<Integer, NpcShopBuyLimit> buyLimit = new HashMap<>();
    private final Map<Integer, NpcShopBuyLimit> accountBuyLimit = new HashMap<>();
    private final Map<Integer, Integer> soulCollection = new HashMap<>();
    private final Map<Integer, String> mobCollection = new LinkedHashMap<>();
    private final List<SecondaryStat> tempStatsToRemove = new ArrayList<>();
    private final AtomicInteger AIFamiliarID = new AtomicInteger(1);
    private final Lock saveLock = new ReentrantLock();
    private final List<InnerSkillEntry> tempInnerSkills = new ArrayList<>();
    private final Map<String, Object> tempValues = new HashMap<>();
    public int lightning, siphonVitality, armorSplit, PPoint, combination, killingpoint, stackbuff, combinationBuff, BULLET_SKILL_ID,
            SpectorGauge, LinkofArk, FlowofFight, Stigma, bulletParty;
    public int batt, clearWeb, forceBlood, fightJazzSkill, nextBlessSkill, empiricalStack, adelResonance, silhouetteMirage, repeatingCrossbowCatridge, dojoCoolTime, dojoStartTime;
    /* this is seren */
    public int SerenStunGauge, cylindergauge;
    private String name, chalktext, BlessOfFairy_Origin, BlessOfEmpress_Origin, teleportname;
    private long lastChangeMapTime, lastfametime, keydown_skill, nextConsume, pqStartTime, lastSummonTime, mapChangeTime, lastFishingTime,
            lastFairyTime, lastmonsterCombo, lastHPTime, lastMPTime, lastExpirationTime, lastBlessOfDarknessTime;
    private byte gender, initialSpawnPoint, skinColor, guildrank = 5, allianceRank = 5, world, subcategory;
    private short mulung_energy, fatigue, hpApUsed, mpApUsed, job, remainingAp, scrolledPosition, availableCP, totalCP;
    private int level;
    private int accountid;
    private int id;
    private int hair;
    private int face;
    private int mapid;
    private int fame;
    private int pvpExp;
    private int pvpPoints;
    private int totalWins;
    private int totalLosses;
    private int hitcountbat;
    private int batcount;
    private int guildid = 0;
    private int fallcounter; /*maplepoints, acash,*/
    private int itemEffect;
    private int itemEffectType;
    private int points;
    private int vpoints;
    private int criticalgrowth;
    private int rank = 1;
    private int rankMove = 0;
    private int jobRank = 1;
    private int jobRankMove = 0;
    private int marriageId;
    private int marriageItemId;
    private int touchedrune;
    private int monsterCombo;
    private int currentrep;
    private int totalrep;
    private int coconutteam;
    private int followid;
    private int challenge;
    private int guildContribution = 0;
    private int todayonlinetime;
    private int totalonlinetime;
    private int weaponPoint = -1;
    private PortableChair chair;
    private Point old;
    private int[] wishlist, rocks, savedLocations, regrocks, hyperrocks, remainingSp = new int[10];
    private transient AtomicInteger inst, insd;
    private List<Integer> lastmonthfameids, lastmonthbattleids;
    private List<MechDoor> mechDoors;
    private MaplePet[] spawnPets; //召喚中的寵物
    private transient List<MapleShopItem> rebuy; //回購
    private MapleImp[] imps;
    private transient Set<MapleMonster> controlledMonsters;
    private transient Set<MapleMapObject> visibleMapObjects;
    private transient ReentrantReadWriteLock visibleMapObjectsLock;
    private transient ReentrantLock summonsLock;
    private transient Map<Integer, SkillCustomInfo> customInfo;
    private transient ReentrantLock controlMonsterLock;
    private transient ReentrantLock atomRecordsLock;
    private transient ReentrantReadWriteLock itemLock;
    private transient ReentrantLock addhpmpLock;
    private transient Lock rLCheck;
    private transient MapleAndroid android;
    private Map<Integer, String> questinfo;
    private Map<Integer, String> worldShareInfo;
    private Map<String, String> keyValue; //新增角色信息
    private InnerSkillEntry[] innerSkills; //角色內在能力技能
    private transient Map<SecondaryStat, List<SecondaryStatValueHolder>> effects;
    private transient Map<Integer, MapleCoolDownValueHolder> skillCooldowns;
    private transient List<MapleSummon> summons;
    private Map<ReportType, Integer> reports;
    private MonsterBook monsterbook;
    private CashShop cs;
    private BuddyList buddylist;
    private transient CheatTracker anticheat; //外掛檢測系統
    private transient MapleClient client;
    private transient Party party;
    private transient MapleMap map;
    private transient MapleShop shop;
    private transient MapleDragon dragon;
    private transient MapleExtractor extractor;
    private transient RockPaperScissors rps;
    private MapleTrunk trunk;
    private transient MapleTrade trade;
    private MapleMount mount;
    private String BattleGrondJobName;
    private MapleMessenger messenger;
    private transient IMaplePlayerShop playerShop;
    private boolean invincible, canTalk, followinitiator, followon, smega, hasSummon;
    private MapleGuildCharacter mgc;
    //    private transient EventInstanceManager eventInstance;
    private transient ScriptEvent eventInstance;
    private MapleInventory[] inventory;
    private SkillMacro[] skillMacros = new SkillMacro[5];
    @Getter
    private Map<Integer, FuncKeyMap> funcKeyMaps;
    private MapleQuickSlot quickslot;
    private transient ScheduledFuture<?> mapTimeLimitTask;
    private transient ScheduledFuture<?> chalkSchedule; //小黑板處理線程
    private transient ScheduledFuture<?> questTimeLimitTask; //小黑板處理線程
    private transient Event_PyramidSubway pyramidSubway = null;
    private transient List<Integer> pendingExpiration = null;
    private transient Map<Integer, SkillEntry> pendingSkills = null;
    private transient Map<Integer, Integer> linkMobs;
    private boolean changed_wishlist;
    private boolean changed_trocklocations;
    private boolean changed_skillmacros;
    private boolean changed_savedlocations;
    private boolean changed_questinfo;
    private boolean changed_worldshareinfo;
    private boolean changed_skills;
    private boolean changed_reports;
    private boolean changed_vcores;
    private boolean changed_innerSkills;
    private boolean changed_keyValue;
    private boolean changed_buylimit;
    private boolean changed_accountbuylimit;
    private boolean changed_mobcollection;
    private boolean changed_familiars;
    private int decorate; //魔族之紋
    private boolean isbanned = false; //檢測是否封號
    private int beans; //豆豆信息
    private int warning; //使用非法程序警告次數
    private int reborns, reborns1, reborns2, reborns3, apstorage; //轉生繫統
    private byte deathcount, gmLevel, secondgender, secondSkinColor, cardStack, wolfscore, sheepscore,
            pandoraBoxFever, fairyExp, numClones;
    private int honor; //榮譽系統
    private Timestamp createDate; //角色創建的時間
    //好感度系統
    private int love; //角色好感度
    private long lastlovetime; //最後加好感度的時間
    private Map<Integer, Long> lastdayloveids; //今天給別人加好感度的列表 角色ID 給角色加好感的時間
    //新增變量
    private int playerPoints; //角色里程
    private int playerEnergy; //角色活力值
    //新增PVP屬性
    private transient MaplePvpStats pvpStats;
    private int pvpDeaths; //死亡次數
    private int pvpKills; //殺敵次數
    private int pvpVictory; //連續擊殺
    //藥劑罐系統
    private MaplePotionPot potionPot;
    //檢測是否正在保存角色數據
    private boolean isSaveing;
    //角色一些特殊變量數據
    private PlayerSpecialStats specialStats;
    private Timestamp todayonlinetimestamp;
    //角色連續擊殺怪物處理
    private int mobKills; //連續擊殺怪物的數量
    private long lastMobKillTime; //判斷連續擊殺怪物的時間間隔
    //火焰傳動處理
    private int flameMapId; //設置的地圖
    //角色1次對怪物的最大傷害
    private long totDamageToMob;
    //角色最後使用符文的時間
    private long lastFuWenTime;
    //尖兵扣除電池時間的檢測
//    private long checkXenonBatteryTime; //檢測電池在60秒內使用的時間
    //最後使用的攻擊技能ID
    private int lastAttackSkillId;
    //檢測夜行者攻擊命中次數
    private int attackHit;
    //NPC間隔時間
    private long lasttime = 0;
    private long currenttime = 0;
    private long deadtime = 300L;
    // 名流爆擊
    private transient ScheduledFuture<?> celebrityCrit;
    private transient Channel chatSession;
    // 自定義里程
    private Map<String, Integer> credit;
    // 靈魂武器點數
    private int soulMP = 0;
    // 客戶端進程信息
    private long lastCheckProcess;
    private int friendshiptoadd;
    private int[] friendshippoints = new int[5];
    // 每日簽到狀態
    private MapleSigninStatus siginStatus;
    // 特效
    private List<Integer> effectSwitch;
    //萌獸圖鑒
    private List<MonsterFamiliar> familiars;
    private MonsterFamiliar summonedFamiliar;
    private volatile long logintime;
    private volatile int lastOnlineTime = -1;
    private WeakReference<MapleReactor> reactor = new WeakReference<>(null);
    private transient MapleCarnivalParty carnivalParty;
    private transient Deque<MapleCarnivalChallenge> pendingCarnivalRequests;
    //    private int reviveCount = -1;
    private boolean attclimit = false;
    private Map<Byte, List<Item>> extendedSlots;
    private long ltime = 0;
    private Map<Integer, VCoreSkillEntry> vCoreSkills;
    private transient long lastUseVSkillTime = 0;
    private int buffValue = 0;
    private long lastComboTime = 0;
    private HiredFisher hiredFisher;
    // 冒險聯盟
    private MapleUnion mapleUnion;
    private boolean changed_mapleUnion = false;
    private boolean scriptShop = false;
    private int burningChrType = Burning.無;
    private long burningChrTime = -2;
    private boolean overMobLevelTip = false;
    /* blackmage */
    private int blackmagewb;
    /* DUSK */
    private long lastSpawnBlindMobtime = System.currentTimeMillis();
    /**
     * 記錄惡魔獵手藍血減cd需要吸收的精氣數目
     */
    private int allreadyForceGet = 0;
    //終極無限恢復線程
    private transient ScheduledFuture<?> UltimateScheduled;
    private int allForce = 0;
    private int judgementStack;
    private int mobZoneState;
    private int larknessDiraction = 3;
    private int larkness;
    private boolean truthGate;
    private int soulSkillID;
    private short soulOption;
    private boolean showSoulEffect;
    private int maxSoulMP;
    private int linkMobObjectID;
    private boolean inGameCurNode;
    private Map<Integer, VMatrixSlot> vMatrixSlot = new TreeMap<>();
    private MapleFieldActionBar actionBar;
    private MapleSkillPet skillPet = null;
    private int moonlightValue, sunlightValue;
    private long lastChangeFullSoulMP = 0;
    private int antiMacroFails = 0;
    private long runeNextActionTime = 0;
    private Map<Integer, List<Integer>> salon = new HashMap<>();
    private int[] runeStoneAction = null;
    private Map<Integer, Pair<Integer, Long>> fairys;
    private SpecialChairTW specialChairTW;
    private SpecialChair specialChair;
    private boolean stopComboKill = false;
    //
    private List<TownPortal> townportals;
    private long townPortalLeaveTime;
    /* 死亡次數 */
    private int[] deathCounts;
    private final ScriptManager scriptManager = new ScriptManager(this);
    private final Map<Integer, MapleHexaStat> hexaStats = new HashMap<>();
    private final Map<Integer, MapleHexaSkill> hexaSkills = new HashMap<>();
    private boolean online;
    /* DUSK 處理 */
    private int HowlingGaleCount, YoyoCount, WildGrenadierCount, VerseOfRelicsCount,
            BHGCCount, RandomPortal, fwolfattackcount, BlockCount, BlockCoin, MesoChairCount, tempmeso, eventcount, duskGauge;
    private long fwolfdamage, LastMovement, PlatformerStageEnter, AggressiveDamage;
    private boolean hasfwolfportal, isfwolfkiller, isWatchingWeb, oneMoreChance, isDuskBlind, eventkillmode;
    private int beholderSkill1;
    private int beholderSkill2;
    /* blackmage */
    private transient Map<SecondaryStat, Pair<MobSkill, Integer>> diseases;
    /* this is will */
    private int moonGauge;

    private final List<String> scriptManagerDebug = new ArrayList<>();

    // js引擎中的共享變數
    @Getter
    @Setter
    private Map<String, Object> variable = new ConcurrentHashMap<String, Object>();

    //    分身功能（傷害倍數）
    @Getter
    @Setter
    private int separation = 1;

    //    無CD
    @Getter
    @Setter
    private boolean gmcooldown = false;

    //    自動攻擊
    @Getter
    @Setter
    private int autoAttack = -1;
    @Getter
    @Setter
    private ScheduledExecutorService timerInstance;



    protected MapleCharacter(boolean ChannelServer) {
        setStance(0);
        setPosition(new Point(0, 0));
        inventory = new MapleInventory[MapleInventoryType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values()) {
            inventory[type.ordinal()] = new MapleInventory(type);
        }
        keyValue = new ConcurrentHashMap<>(); //新增角色信息處理
        questinfo = new LinkedHashMap<>();
        worldShareInfo = new ConcurrentHashMap<>();
        quests = new LinkedHashMap<>(); // Stupid erev quest.
        skills = new LinkedHashMap<>(); //角色技能
        linkSkills = new LinkedHashMap<>();
        sonOfLinkedSkills = new LinkedHashMap<>();
        innerSkills = new InnerSkillEntry[3]; //角色內在能力技能 默認只能3個
        stats = new PlayerStats(); //角色屬性計算
        characterCard = new MapleCharacterCards(); //角色卡系統
        for (int i = 0; i < remainingSp.length; i++) {
            remainingSp[i] = 0;
        }
        traits = new EnumMap<>(MapleTraitType.class);
        for (MapleTraitType t : MapleTraitType.values()) {
            traits.put(t, new MapleTrait(t));
        }
        specialStats = new PlayerSpecialStats(); //角色特殊變量處理
        specialStats.resetSpecialStats();
        familiars = new LinkedList<>();
        damSkinList = new ArrayList<>();
        if (ChannelServer) {
            isSaveing = false;
            changed_reports = false;
            changed_skills = false;
            changed_wishlist = false;
            changed_trocklocations = false;
            changed_skillmacros = false;
            changed_savedlocations = false;
            changed_questinfo = false;
            changed_innerSkills = false;
            changed_keyValue = false;
            changed_vcores = false;
            scrolledPosition = 0;
            criticalgrowth = 0;
            mulung_energy = 0;
            keydown_skill = 0;
            nextConsume = 0;
            pqStartTime = 0;
            mapChangeTime = 0;
            lastmonsterCombo = 0;
            monsterCombo = 0;
            lastFishingTime = 0;
            lastFairyTime = System.currentTimeMillis();
            fairys = new LinkedHashMap<>();
            lastHPTime = 0;
            lastMPTime = 0;
            lastExpirationTime = 0; //裝備到期檢測
            lastBlessOfDarknessTime = 0; //黑暗祝福檢測
            old = new Point(0, 0);
            coconutteam = 0;
            followid = 0;
            marriageItemId = 0;
            fallcounter = 0;
            challenge = 0;
            lastSummonTime = 0;
            blackmagewb = 1;
            townPortalLeaveTime = -1;
            hasSummon = false;
            invincible = false;
            this.clearWeb = 0;
            this.SerenStunGauge = 0;
            canTalk = true;
            this.moonGauge = 0;
            followinitiator = false;
            followon = false;
            rebuy = new ArrayList<>(); //商店回購
            linkMobs = new HashMap<>();
            reports = new EnumMap<>(ReportType.class);
            teleportname = "";
            smega = true;
            spawnPets = new MaplePet[3]; //當前召喚的寵物
            wishlist = new int[12]; //V.112修改為12個
            duskGauge = 0;
            isDuskBlind = false;
            rocks = new int[10];
            regrocks = new int[5];
            hyperrocks = new int[13];
            imps = new MapleImp[3];
            friendshippoints = new int[5];
            extendedSlots = new HashMap<>();
            effects = new LinkedHashMap<>();
            skillCooldowns = new LinkedHashMap<>();
            inst = new AtomicInteger(0);// 1 = NPC/ Quest, 2 = Duey, 3 = Hired Merch store, 4 = Storage
            insd = new AtomicInteger(-1);
            funcKeyMaps = new LinkedHashMap<>();
            quickslot = new MapleQuickSlot();
            townportals = new ArrayList<>();
            mechDoors = new ArrayList<>();
            itemLock = new ReentrantReadWriteLock();
            rLCheck = itemLock.readLock();
            controlledMonsters = new LinkedHashSet<>();
            controlMonsterLock = new ReentrantLock();
            summons = new LinkedList<>();
            summonsLock = new ReentrantLock();
            visibleMapObjects = new LinkedHashSet<>();
            visibleMapObjectsLock = new ReentrantReadWriteLock();
            addhpmpLock = new ReentrantLock();
            atomRecordsLock = new ReentrantLock();

            SavedLocationType[] slt_arr = SavedLocationType.values();
            savedLocations = new int[slt_arr[slt_arr.length - 1].getValue() + 1];
            for (int i = 0; i < slt_arr.length; i++) {
                savedLocations[i] = -1;
            }
            todayonlinetimestamp = new Timestamp(System.currentTimeMillis());
            credit = new LinkedHashMap<>();
            effectSwitch = new ArrayList<>();
            pendingCarnivalRequests = new LinkedList<>();
            vCoreSkills = new LinkedHashMap<>();
        }
    }

    /**
     * 新角色默認的數據
     */
    public static MapleCharacter getDefault(MapleClient client, JobType type) {
        MapleCharacter ret = new MapleCharacter(false);
        ret.client = client;
        ret.map = null;
        ret.exp.set(0);
        ret.job = (short) type.job.getId();
        ret.subcategory = (byte) type.job.getSub();
        ret.level = 1;
        ret.remainingAp = 0;
        ret.fame = 0; //人氣值
        ret.love = 0; //好感度
        ret.accountid = client.getAccID();
        ret.buddylist = new BuddyList((byte) 20);
        ret.burningChrType = ServerConfig.CREATE_CHAR_BURNING;
        if (ret.burningChrType > Burning.無) {
            ret.burningChrTime = System.currentTimeMillis() + (ServerConfig.CREATE_CHAR_BURNING_DAYS * 24 * 60 * 60 * 1000L);
        }

        ret.stats.str = 12;
        ret.stats.dex = 5;
        ret.stats.int_ = 4;
        ret.stats.luk = 4;
        ret.stats.maxhp = 50;
        ret.stats.hp = 50;
        ret.stats.maxmp = 50;
        ret.stats.mp = 50;
        gachexp = 0;
        ret.friendshiptoadd = 0;
        ret.friendshippoints = new int[]{0, 0, 0, 0, 0};

        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ret.client.setAccountName(rs.getString("name"));
                ret.client.setMaplePoints(rs.getInt("mPoints"));
                ret.points = rs.getInt("points");
                ret.vpoints = rs.getInt("vpoints");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error getting character default" + e);
        }
        return ret;
    }

    public static MapleCharacter ReconstructChr(CharacterTransfer ct, MapleClient client, boolean isChannel) {
        MapleCharacter ret = new MapleCharacter(true); // Always true, it's change channel
        ret.client = client;
        if (client != null) {
            client.setAccID(ct.accountid);
            client.setAccountName(ct.accountname);
            client.setMaplePoints(ct.maplePoint);
            client.setTempIP(ct.tempIP);
            client.setGmLevel(ct.gmLevel);
        }
        if (!isChannel && client != null) {
            ret.client.setChannel(ct.channel);
        }
        ret.id = ct.characterid;
        ret.name = ct.name;
        ret.level = ct.level;
        ret.fame = ct.fame; //人氣值
        ret.love = ct.love; //好感度

        ret.stats.str = ct.str;
        ret.stats.dex = ct.dex;
        ret.stats.int_ = ct.int_;
        ret.stats.luk = ct.luk;
        ret.stats.maxhp = ct.maxhp;
        ret.stats.maxmp = ct.maxmp;
        ret.stats.hp = ct.hp;
        ret.stats.mp = ct.mp;

        ret.chalktext = ct.chalkboard;
        ret.exp.set(ret.level > GameConstants.MAX_LEVEL ? 0 : ct.exp);
        ret.hpApUsed = ct.hpApUsed;
        ret.mpApUsed = ct.mpApUsed;
        ret.remainingSp = ct.remainingSp;
        ret.remainingAp = ct.remainingAp;
        ret.meso.set(ct.meso);
        ret.skinColor = ct.skinColor;
        ret.gender = ct.gender;
        ret.job = ct.job;
        ret.hair = ct.hair;
        ret.face = ct.face;
        ret.accountid = ct.accountid;
        ret.totalWins = ct.totalWins;
        ret.totalLosses = ct.totalLosses;
        ret.mapid = ct.mapid;
        ret.initialSpawnPoint = ct.initialSpawnPoint;
        ret.world = ct.world;
        ret.guildid = ct.guildid;
        ret.guildrank = ct.guildrank;
        ret.guildContribution = ct.guildContribution;
        ret.allianceRank = ct.alliancerank;
        ret.points = ct.points;
        ret.vpoints = ct.vpoints;
        ret.fairys = ct.fairys;
        ret.marriageId = ct.marriageId;
        ret.currentrep = ct.currentrep;
        ret.totalrep = ct.totalrep;
        gachexp = ct.gachexp;
        ret.pvpExp = ct.pvpExp;
        ret.pvpPoints = ct.pvpPoints;
        ret.decorate = ct.decorate; //魔族之紋
        ret.beans = ct.beans; //豆豆數量
        ret.warning = ct.warning; //警告次數
        //轉生繫統
        ret.reborns = ct.reborns;
        ret.reborns1 = ct.reborns1;
        ret.reborns2 = ct.reborns2;
        ret.reborns3 = ct.reborns3;
        ret.apstorage = ct.apstorage;
        //榮譽系統
        ret.honor = ct.honor;
        ret.playerPoints = ct.playerPoints; //角色里程
        ret.playerEnergy = ct.playerEnergy; //角色活力值
        ret.pvpDeaths = ct.pvpDeaths; //死亡次數
        ret.beholderSkill1 = 0;
        ret.beholderSkill2 = 0;
        ret.pvpKills = ct.pvpKills; //殺敵次數
        ret.pvpVictory = ct.pvpVictory; //連續擊殺
        if (ret.guildid > 0) {
            ret.mgc = new MapleGuildCharacter(ret);
        }
        ret.fatigue = ct.fatigue;
        ret.buddylist = new BuddyList(ct.buddysize);
        ret.subcategory = ct.subcategory;
        ret.friendshiptoadd = ct.friendshiptoadd;
        ret.friendshippoints = ct.friendshippoints;
        ret.soulMP = ct.soulcount;
        ret.inventory = ct.inventorys;
        ret.ltime = ct.ltime;

        int messengerid = ct.messengerid;
        if (messengerid > 0) {
            ret.messenger = WorldMessengerService.getInstance().getMessenger(messengerid);
        }
        int partyid = ct.partyid;
        if (partyid >= 0) {
            Party party = client.getWorld().getPartybyId(partyid);
            if (party != null && party.getPartyMemberByID(ret.id) != null) {
                ret.party = party;
            }
        }

        MapleQuestStatus queststatus_from;
        for (Entry<Integer, MapleQuestStatus> qs : ct.Quest.entrySet()) {
            queststatus_from = qs.getValue();
            queststatus_from.setQuest(qs.getKey());
            ret.quests.put(qs.getKey(), queststatus_from);
        }
        for (Entry<Integer, SkillEntry> qs : ct.skills.entrySet()) {
            ret.skills.put(qs.getKey(), qs.getValue());
            if (SkillConstants.isLinkSkills(qs.getKey()) && ret.linkSkills.size() < 12) {
                ret.linkSkills.put(qs.getKey(), qs.getValue());
            }
        }
        for (Entry<MapleTraitType, Integer> t : ct.traits.entrySet()) {
            ret.traits.get(t.getKey()).setExp(t.getValue());
        }
        for (Entry<Byte, Integer> qs : ct.reports.entrySet()) {
            ret.reports.put(ReportType.getById(qs.getKey()), qs.getValue());
        }
        ct.sonOfLinedSkills.forEach((key, value) -> ret.sonOfLinkedSkills.put(key, value));
        ct.vcoreskills.forEach((key, value) -> ret.vCoreSkills.put(ret.vCoreSkillIndex.getAndIncrement(), value));
        ret.innerSkills = ct.innerSkills;
        ret.BlessOfFairy_Origin = ct.BlessOfFairy;
        ret.BlessOfEmpress_Origin = ct.BlessOfEmpress;
        ret.skillMacros = ct.skillmacro;
        ret.spawnPets = ct.spawnPets;
        ret.funcKeyMaps = ct.funcKeyMaps;
        ret.quickslot = new MapleQuickSlot(ct.quickslot);
        ret.keyValue = ct.KeyValue;
        ret.questinfo = ct.InfoQuest;
        ret.worldShareInfo = ct.worldShareInfo;
        ret.savedLocations = ct.savedlocation;
        ret.wishlist = ct.wishlist;
        ret.rocks = ct.rocks;
        ret.regrocks = ct.regrocks;
        ret.hyperrocks = ct.hyperrocks;
        ret.buddylist.loadFromTransfer(ct.buddies);
        // ret.lastfametime
        // ret.lastmonthfameids
        ret.keydown_skill = 0; // Keydown skill can't be brought over
        ret.lastfametime = ct.lastfametime;
        ret.lastmonthfameids = ct.famedcharacters;
        ret.lastmonthbattleids = ct.battledaccs;
        ret.extendedSlots = ct.extendedSlots;
        ret.lastlovetime = ct.lastLoveTime;
        ret.lastdayloveids = ct.loveCharacters;
        ret.trunk = ct.storage;
        ret.pvpStats = ct.pvpStats; //Pvp另外計算的屬性
        ret.potionPot = ct.potionPot; //藥劑罐信息
        ret.specialStats = ct.SpecialStats; //特殊屬性
        ret.cs = ct.cs;
        ret.imps = ct.imps;
        ret.anticheat = ct.anticheat; //外掛檢測系統
        ret.anticheat.start(ret.getId());
        ret.rebuy = ct.rebuy;
        ret.mount = new MapleMount(ret, ct.mount_itemid, SkillConstants.getSkillByJob(1004, ret.job), ct.mount_Fatigue, ct.mount_level, ct.mount_exp);
        ret.todayonlinetime = ct.todayonlinetime;
        ret.totalonlinetime = ct.totalonlinetime;
        ret.weaponPoint = ct.weaponPoint;
        ret.credit = ct.credit;
        ret.effectSwitch = ct.effectSwitch;
        ret.familiars = ct.familiars;
        ret.mapleUnion = ct.mapleUnion;
        ret.summonedFamiliar = ct.summonedFamiliar;
        ret.vMatrixSlot = ct.vMatrixSlot;
        ret.lastOnlineTime = ct.onlineTime;
        ret.logintime = ct.loginTime;
        ret.soulMP = ct.soulMP;
        ret.soulSkillID = ct.soulSkillID;
        ret.soulOption = ct.soulOptionID;
        ret.maxSoulMP = ct.maxSoulMP;
        ret.salon = ct.salon;
        ret.burningChrType = ct.burningChrType;
        ret.burningChrTime = ct.burningChrTime;

        if (isChannel) {
            MapleMapFactory mapFactory = ChannelServer.getInstance(ct.channel).getMapFactory();
            ret.map = mapFactory.getMap(ret.mapid);
            if (ret.map == null) { //char is on a map that doesn't exist warp it to henesys
                ret.map = mapFactory.getMap(100000000);
            } else if (ret.map.getForcedReturnId() != 999999999 && ret.map.getForcedReturnMap() != null) {
                ret.map = ret.map.getForcedReturnMap();
            }
            MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
            if (portal == null) {
                portal = ret.map.getPortal(0); // char is on a spawnpoint that doesn't exist - select the first spawnpoint instead
                ret.initialSpawnPoint = 0;
            }
            ret.setPosition(portal.getPosition());
            ret.characterCard.loadCards(client, true);
            ret.stats.recalcLocalStats(true, ret);
        } else {
            ret.messenger = null;
        }

        return ret;
    }

    /**
     * 加載角色信息
     */
    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) {
        return loadCharFromDB(charid, client, channelserver, null);
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelServer, Map<Integer, CardData> cads) {
        MapleCharacter ret = new MapleCharacter(channelServer);
        ret.client = client;
        ret.id = charid;

        PreparedStatement ps = null;
        PreparedStatement pse;
        ResultSet rs = null;

        try {

            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    throw new RuntimeException("加載角色失敗原因(角色沒有找到).");
                }
                ret.name = rs.getString("name");
                ret.level = rs.getShort("level");
                ret.fame = rs.getInt("fame"); //人氣值
                ret.love = rs.getInt("love"); //好感度

                ret.stats.str = rs.getShort("str");
                ret.stats.dex = rs.getShort("dex");
                ret.stats.int_ = rs.getShort("int");
                ret.stats.luk = rs.getShort("luk");
                ret.job = rs.getShort("job");
                ret.stats.maxhp = rs.getInt("maxhp");
                ret.stats.maxmp = JobConstants.isNotMpJob(ret.job) ? GameConstants.getMPByJob(ret.job) : rs.getInt("maxmp");
                ret.stats.hp = rs.getInt("hp");
                ret.stats.mp = rs.getInt("mp");
                ret.exp.set(ret.level >= GameConstants.MAX_LEVEL ? 0 : rs.getLong("exp"));
                ret.hpApUsed = rs.getShort("hpApUsed");
                ret.mpApUsed = rs.getShort("mpApUsed");
                String[] sp = rs.getString("sp").split(",");
                for (int i = 0; i < ret.remainingSp.length; i++) {
                    ret.remainingSp[i] = Integer.parseInt(sp[i]);
                }
                ret.remainingAp = rs.getShort("ap");
                ret.meso.set(rs.getLong("meso"));
                ret.gender = rs.getByte("gender");

                // 讀取數據前檢測一下玩家的髮型臉型是否存在，免得客戶端出現問題
                byte skin = rs.getByte("skincolor");
                int hair = rs.getInt("hair");
                int face = rs.getInt("face");
                if (!MapleItemInformationProvider.getInstance().isSkinExist(skin)) {
                    skin = 0;
                }
                if (!MapleItemInformationProvider.getInstance().isHairExist(hair)) {
                    hair = ret.gender == 0 ? 30000 : 31000;
                }
                if (!MapleItemInformationProvider.getInstance().isFaceExist(face)) {
                    face = ret.gender == 0 ? 20000 : 21000;
                }
                ret.skinColor = skin;
                ret.hair = hair;
                ret.face = face;

                ret.accountid = rs.getInt("accountid");
                if (client != null) {
                    client.setAccID(ret.accountid);
                }
                ret.mapid = rs.getInt("map");
                if (client.getChannelServer().getMapFactory().getMap(ret.mapid) == null) {
                    ret.mapid = 950000100;
                }
                ret.initialSpawnPoint = rs.getByte("spawnpoint");
                ret.world = rs.getByte("world");
                ret.guildid = rs.getInt("guildid");
                ret.guildrank = rs.getByte("guildrank");
                ret.allianceRank = rs.getByte("allianceRank");
                ret.guildContribution = rs.getInt("guildContribution");
                ret.totalWins = rs.getInt("totalWins");
                ret.totalLosses = rs.getInt("totalLosses");
                ret.currentrep = rs.getInt("currentrep");
                ret.totalrep = rs.getInt("totalrep");
                if (ret.guildid > 0 && client != null) {
                    ret.mgc = new MapleGuildCharacter(ret);
                }
                gachexp = rs.getInt("gachexp");
                ret.buddylist = new BuddyList(rs.getByte("buddyCapacity"));
                ret.subcategory = rs.getByte("subcategory");
                ret.mount = new MapleMount(ret, 0, SkillConstants.getSkillByJob(1004, ret.job), (byte) 0, (byte) 1, 0);
                //排名系統
                ret.rank = rs.getInt("rank");
                ret.rankMove = rs.getInt("rankMove");
                ret.jobRank = rs.getInt("jobRank");
                ret.jobRankMove = rs.getInt("jobRankMove");
                //結婚的對象角色ID
                ret.marriageId = rs.getInt("marriageId");
                //疲勞度
                ret.fatigue = rs.getShort("fatigue");
                //PVP系統
                ret.pvpExp = rs.getInt("pvpExp");
                ret.pvpPoints = rs.getInt("pvpPoints");
                //傾向系統
                for (MapleTrait t : ret.traits.values()) {
                    t.setExp(rs.getInt(t.getType().name()));
                }
                //魔族之紋
                ret.decorate = rs.getInt("decorate");
                //豆豆數量
                ret.beans = rs.getInt("beans");
                //非法程序使用警告次數
                ret.warning = rs.getInt("warning");
                //轉生繫統
                ret.reborns = rs.getInt("reborns");
                ret.reborns1 = rs.getInt("reborns1");
                ret.reborns2 = rs.getInt("reborns2");
                ret.reborns3 = rs.getInt("reborns3");
                ret.apstorage = rs.getInt("apstorage");
                //榮譽系統
                ret.honor = rs.getInt("honor");
                //新增變量
                ret.playerPoints = rs.getInt("playerPoints");
                ret.playerEnergy = rs.getInt("playerEnergy");
                //Pvp變量
                ret.pvpDeaths = rs.getInt("pvpDeaths"); //死亡次數
                ret.pvpKills = rs.getInt("pvpKills"); //殺敵次數
                ret.pvpVictory = rs.getInt("pvpVictory"); //連續擊殺次數
                ret.todayonlinetime = rs.getInt("todayonlinetime");
                ret.totalonlinetime = rs.getInt("totalonlinetime");
                ret.weaponPoint = rs.getInt("wp");
                ret.friendshiptoadd = rs.getInt("friendshiptoadd");
                String[] points = rs.getString("friendshippoints").split(",");
                for (int i = 0; i < 5; i++) {
                    ret.friendshippoints[i] = Integer.parseInt(points[i]);
                }
                int nBurnType = rs.getByte("burningChrType");
                Timestamp lBurnTime = rs.getTimestamp("burningChrTime");
                if (nBurnType >= Burning.無 && lBurnTime != null && System.currentTimeMillis() < lBurnTime.getTime()) {
                    ret.burningChrType = nBurnType;
                    ret.burningChrTime = lBurnTime.getTime();
                }

                String[] pets = null;
                if (channelServer && client != null) {
                    //Pvp屬性
                    ret.pvpStats = MaplePvpStats.loadOrCreateFromDB(ret.accountid);
                    //外掛檢測系統
                    ret.anticheat = new CheatTracker(ret.getId());
                    //加載角色地圖信息
                    MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
                    ret.map = mapFactory.getMap(ret.mapid);
                    if (ret.map == null) { //如果地圖存在就設置地圖為射手
                        ret.map = mapFactory.getMap(100000000);
                    }
                    //地圖的傳送點
                    MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                    if (portal == null) {
                        portal = ret.map.getPortal(0); // char is on a spawnpoint that doesn't exist - select the first spawnpoint instead
                        ret.initialSpawnPoint = 0;
                    }
                    ret.setPosition(portal.getPosition());
                    //組隊信息
                    int partyid = rs.getInt("party");
                    if (partyid >= 0) {
                        Party party = client.getWorld().getPartybyId(partyid);
                        if (party != null && party.getPartyMemberByID(ret.id) != null) {
                            ret.party = party;
                        }
                    }
                    //寵物信息
                    pets = rs.getString("pets").split(",");
                    // 梳化間-髮型欄位數
                    List<Integer> salon_hair = new LinkedList<>();
                    int salon_hairSize = Math.min(102, Math.max(rs.getInt("salon_hair"), 3));
                    for (int i = 0; i < salon_hairSize; i++) {
                        salon_hair.add(0);
                    }
                    ret.salon.put(3, salon_hair);
                    // 梳化間-臉型欄位數
                    List<Integer> salon_face = new LinkedList<>();
                    int salon_faceSize = Math.min(102, Math.max(rs.getInt("salon_face"), 3));
                    for (int i = 0; i < salon_faceSize; i++) {
                        salon_face.add(0);
                    }
                    ret.salon.put(2, salon_face);
                    // 梳化間-膚色欄位數
                    List<Integer> salon_skin = new LinkedList<>();
                    int salon_skinSize = Math.min(6, Math.max(rs.getInt("salon_skin"), 0));
                    for (int i = 0; i < salon_skinSize; i++) {
                        salon_skin.add(0);
                    }
                    ret.salon.put(1, salon_skin);
                    rs.close();
                    ps.close();
                    //舉報信息
                    ps = con.prepareStatement("SELECT * FROM reports WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        if (ReportType.getById(rs.getByte("type")) != null) {
                            ret.reports.put(ReportType.getById(rs.getByte("type")), rs.getInt("count"));
                        }
                    }
                    ret.setLTime();
                }

                rs.close();
                ps.close();
                /*
                 * 加載角色卡系統
                 */
                if (cads != null) {
                    ret.characterCard.setCards(cads);
                } else if (client != null) {
                    ret.characterCard.loadCards(client, channelServer);
                }
                //加載角色的一些特殊處理信息
                ps = con.prepareStatement("SELECT * FROM character_keyvalue WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString("key") == null) {
                        continue;
                    }
                    ret.keyValue.put(rs.getString("key"), rs.getString("value"));
                }
                rs.close();
                ps.close();
                //加載任務完成信息
                ps = con.prepareStatement("SELECT * FROM questinfo WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.questinfo.put(rs.getInt("quest"), rs.getString("customData"));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT * FROM `accounts_questinfo` WHERE `accounts_id` = ? AND `world` = ?");
                ps.setInt(1, ret.accountid);
                ps.setInt(2, ret.world);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.worldShareInfo.put(rs.getInt("quest"), rs.getString("customData"));
                }
                rs.close();
                ps.close();
                if (channelServer) {
                    //加載任務信息
                    ps = con.prepareStatement("SELECT * FROM queststatus WHERE account = ? AND world = ?");
                    ps.setInt(1, ret.accountid);
                    ps.setInt(2, ret.world);
                    rs = ps.executeQuery();
                    pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");
                    while (rs.next()) {
                        int id = rs.getInt("quest");
                        MapleQuest q = MapleQuest.getInstance(id);
                        byte stat = rs.getByte("status");
                        if ((stat == 1 || stat == 2) && channelServer && (q == null || q.isBlocked())) { //bigbang
                            continue;
                        }
                        if (stat == 1 && channelServer && !q.canStart(ret, null)) { //bigbang
                            continue;
                        }
                        MapleQuestStatus status = new MapleQuestStatus(q, stat);
                        long cTime = rs.getLong("time");
                        if (cTime > -1) {
                            status.setCompletionTime(cTime * 1000);
                        }
                        status.setForfeited(rs.getInt("forfeited"));
                        status.setCustomData(rs.getString("customData"));
                        status.setFromChrID(rs.getInt("characterid"));
                        ret.quests.put(id, status);
                        pse.setLong(1, rs.getLong("queststatusid"));
                        try (ResultSet rsMobs = pse.executeQuery()) {
                            while (rsMobs.next()) {
                                status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                            }
                        }
                    }
                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ? AND account IS NULL");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        int id = rs.getInt("quest");
                        if (ret.quests.containsKey(id)) {
                            continue;
                        }
                        MapleQuest q = MapleQuest.getInstance(id);
                        byte stat = rs.getByte("status");
                        if ((stat == 1 || stat == 2) && channelServer && (q == null || q.isBlocked())) { //bigbang
                            continue;
                        }
                        if (stat == 1 && channelServer && !q.canStart(ret, null)) { //bigbang
                            continue;
                        }
                        MapleQuestStatus status = new MapleQuestStatus(q, stat);
                        long cTime = rs.getLong("time");
                        if (cTime > -1) {
                            status.setCompletionTime(cTime * 1000);
                        }
                        status.setForfeited(rs.getInt("forfeited"));
                        status.setCustomData(rs.getString("customData"));
                        ret.quests.put(id, status);
                        pse.setLong(1, rs.getLong("queststatusid"));
                        try (ResultSet rsMobs = pse.executeQuery()) {
                            while (rsMobs.next()) {
                                status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                            }
                        }
                    }
                    rs.close();
                    ps.close();
                    pse.close();
                    //====================================================================================
                    //加載角色包裹的最大數量
                    ps = con.prepareStatement("SELECT * FROM inventoryslot WHERE characters_id = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        throw new RuntimeException("No Inventory slot column found in SQL. [inventoryslot]");
                    } else {
                        ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getShort("equip"));
                        ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getShort("use"));
                        ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getShort("setup"));
                        ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getShort("etc"));
                        ret.getInventory(MapleInventoryType.CASH).setSlotLimit(rs.getShort("cash"));
                        ret.getInventory(MapleInventoryType.DECORATION).setSlotLimit(rs.getShort("decoration"));
                    }
                    ps.close();
                    rs.close();
                    //背包信息
                    for (byte i = 2; i <= 4; i++) {
                        ret.extendedSlots.put(i, new ArrayList<>());
                    }
                    //加載角色裝備道具
                    for (Pair<Item, MapleInventoryType> mit : ItemLoader.裝備道具.loadItems(false, charid).values()) {
                        ret.getInventory(mit.getRight()).addFromDB(mit.getLeft());
                        if (mit.getLeft() instanceof Equip eqp) {
                            if (類型.秘法符文(eqp.getItemId())) {
                                eqp.recalcArcStat(ret.job);
                            }
                            if (類型.真實符文(eqp.getItemId())) {
                                eqp.recalcAutStat(ret.job);
                            }
                        }
                        if (mit.getLeft().getExtendSlot() > 0) {
                            ret.extendedSlots.get(mit.getRight().getType()).add(mit.getLeft());
                        }
                    }
                    //寵物信息
                    if (pets != null) {
                        MapleQuestStatus stat = ret.getQuestNAdd(MapleQuest.getInstance(GameConstants.ALLOW_PET_LOOT));
                        for (String p : pets) {
                            Item item = ret.getInventory(MapleInventoryType.CASH).getItem(Short.parseShort(p));
                            if (item == null || item.getPet() == null || !類型.寵物(item.getItemId())) {
                                break;
                            }
                            ret.addSpawnPet(item.getPet());
                            item.getPet().setPos(new Point(ret.getPosition()));
                            item.getPet().setCanPickup(stat.getCustomData() == null || stat.getCustomData().equals("1"));
                        }
                    }
                    //加載角色的賬號樂豆點之類的信息
                    ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                    ps.setInt(1, ret.accountid);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        ret.getClient().setAccountName(rs.getString("name"));
                        ret.getClient().setGmLevel(rs.getInt("gm"));
                        ret.getClient().setMaplePoints(rs.getInt("mPoints"));
                        ret.getClient().setSecondPassword(rs.getString("2ndpassword"));
                        if (ret.getClient().getSecondPassword() != null && rs.getString("salt2") != null) {
                            ret.getClient().setSecondPassword(LoginCrypto.rand_r(ret.getClient().getSecondPassword()));
                        }
                        ret.getClient().setSalt2(rs.getString("salt2"));
                        ret.points = rs.getInt("points");
                        ret.vpoints = rs.getInt("vpoints");
                        /*
                         * 年：calendar.get(Calendar.YEAR)
                         * 月：calendar.get(Calendar.MONTH)+1
                         * 日：calendar.get(Calendar.DAY_OF_MONTH)
                         * 星期：calendar.get(Calendar.DAY_OF_WEEK)-1
                         */
                        if (rs.getTimestamp("lastlogon") != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(rs.getTimestamp("lastlogon").getTime());
                            if (cal.get(Calendar.DAY_OF_WEEK) + 1 == Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                                //ret.acash += 500; //去掉這個給樂豆點的
                            }
                        }
                        if (rs.getInt("banned") > 0) {
                            rs.close();
                            ps.close();
                            ret.getClient().getSession().close();
                            throw new RuntimeException("加載的角色為封號狀態，服務端斷開這個連接...");
                        }
                        rs.close();
                        ps.close();
                        //更新角色賬號的登錄時間
                        ps = con.prepareStatement("UPDATE accounts SET lastlogon = CURRENT_TIMESTAMP() WHERE id = ?");
                        ps.setInt(1, ret.accountid);
                        ps.executeUpdate();
                    } else {
                        rs.close();
                    }
                    ps.close();

                    ps = con.prepareStatement("SELECT * FROM `vmatrixslot` WHERE `characters_id` = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        VMatrixSlot slot = new VMatrixSlot();
                        slot.setExtend(rs.getInt("extend"));
                        slot.setSlot(rs.getInt("slot"));
                        slot.setUnlock(rs.getInt("unlock"));
                        ret.getVMatrixSlot().put(slot.getSlot(), slot);
                    }
                    for (int i = 0; i < VMatrixOption.EquipSlotMax; ++i) {
                        ret.getVMatrixSlot().putIfAbsent(i, new VMatrixSlot(i));
                    }
                    rs.close();
                    ps.close();

                    ps = con.prepareStatement("SELECT * FROM vcoreskill WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        //                    if (MapleItemInformationProvider.getInstance().getVcores().containsKey(rs.getInt("vcoreid"))) {
                        ret.addVCoreSkill(new VCoreSkillEntry(rs.getInt("vcoreid"), rs.getInt("level"), rs.getInt("exp"), rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), -1, rs.getInt("slot"), rs.getInt("index")));
                        //                    }
                    }
                    rs.close();
                    ps.close();

                    //加載角色技能信息
                    ps = con.prepareStatement("SELECT * FROM skills WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    Skill skil;
                    int phantom = 0;
                    boolean resetSkill = false;
                    while (rs.next()) {
                        int skid = rs.getInt("skillid"); //技能ID
                        int skillForJob = skid / 10000;
                        skil = SkillFactory.getSkill(skid);
                        int skl = rs.getInt("skilllevel"); //當前技能等級
                        byte msl = rs.getByte("masterlevel"); //技能最大等級
                        int teachId = rs.getInt("teachId"); //技能傳授者ID
                        int teachTimes = rs.getInt("teachTimes");
                        byte position = rs.getByte("position");  //幻影複製技能位置
                        if (skil != null && SkillConstants.isApplicableSkill(skid) && (skl <= skil.getMaxLevel() || (skid >= 92000000 && skid <= 99999999)) && msl <= skil.getMaxLevel()) {
                            if (skil.is老技能()) { //如果是老的技能 直接跳過
                                ret.changed_skills = true;
                                continue;
                            }
                            if ((position >= 0 && position < 15) && phantom < 15) {
                                if (JobConstants.is幻影俠盜(ret.job) && skil.getSkillByJobBook() != -1) {
                                    msl = skil.isFourthJob() ? (byte) skil.getMasterLevel() : 0;
                                    ret.skills.put(skid, new SkillEntry(skl, msl, -1, teachId, teachTimes, position));
                                }
                                phantom++;
                            } else {
                                if (skil.isLinkSkills() && ret.linkSkills.size() < 12) {
                                    skl = SkillConstants.getLinkSkillslevel(skil, teachId, ret.level);
                                    ret.linkSkills.put(skid, new SkillEntry(skl, msl, rs.getLong("expiration"), teachId, teachTimes));
                                } else if (skil.isTeachSkills()) {
                                    skl = SkillConstants.getLinkSkillslevel(skil, teachId, ret.level);
                                } else if (skil.getFixLevel() > 0) {
                                    skl = skil.getFixLevel();
                                } else if (skillForJob >= 40000 && skillForJob <= 40005) {
                                    if (ret.getVCoreSkillLevel(skid) <= 0) {
                                        ret.changed_skills = true;
                                        continue;
                                    }
                                    skl = ret.getVCoreSkillLevel(skid);
                                }
                                ret.skills.put(skid, new SkillEntry(skl, msl, rs.getLong("expiration"), teachId, teachTimes));
                            }
                        } else {
                            if (!resetSkill && (skil == null || skil.is老技能() || skl > skil.getMaxLevel() || msl > skil.getMaxLevel()) && !JobConstants.notNeedSPSkill(skillForJob) && JobConstants.isSameJob(ret.job, skillForJob)) {
                                int jobNumber = JobConstants.getJobNumber(skillForJob);
                                if (jobNumber > 0 && jobNumber < 5) {
                                    resetSkill = true;
                                }
                            }
                        }
                    }
                    rs.close();
                    ps.close();
                    if (resetSkill) {
                        ret.spReset(false);
                    }

                    if (client != null) {
                        ps = con.prepareStatement("SELECT id, level FROM characters WHERE accountid = ? AND world = ?");
                        ps.setInt(1, ret.accountid);
                        ps.setInt(2, ret.world);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            int level = rs.getInt("level");
                            try (PreparedStatement psee = con.prepareStatement("SELECT skillid, masterlevel, expiration ,teachId, teachTimes FROM skills WHERE characterid = ?")) {
                                psee.setInt(1, id);
                                try (ResultSet rse = psee.executeQuery()) {
                                    while (rse.next()) {
                                        int skillid = SkillConstants.getLinkSkillId(rse.getInt("skillid"));
                                        Skill skill = SkillFactory.getSkill(rse.getInt("skillid"));
                                        int teachId = rse.getInt("teachId");
                                        int teachTimes = rse.getInt("teachTimes");
                                        int linkSkillslevel = SkillConstants.getLinkSkillslevel(skill, teachId, level);
                                        if (skill != null && skill.isTeachSkills() && linkSkillslevel > 0) {
                                            byte masterlevel = rse.getByte("masterlevel");
                                            ret.sonOfLinkedSkills.put(skillid, new Pair<>(id, new SkillEntry(linkSkillslevel, masterlevel, rse.getLong("expiration") > 0 && System.currentTimeMillis() < rse.getLong("expiration") + 86400000 ? rse.getLong("expiration") : -2, teachId > 0 ? teachId : id, teachTimes)));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    rs.close();
                    ps.close();
                    //加載冒險聯盟信息
                    ret.mapleUnion = new MapleUnion();
                    ps = con.prepareStatement("SELECT `id`,`job`,`level`,`name` FROM characters WHERE accountid = ? ORDER BY level DESC");
                    ps.setInt(1, ret.accountid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final int chrid = rs.getInt("id");
                        final short job = rs.getShort("job");
                        final short level = rs.getShort("level");
                        final String name = rs.getString("name");
                        if (level >= (JobConstants.is神之子(job) ? 130 : 60)) {
                            ret.mapleUnion.getAllUnions().put(chrid, new MapleUnionEntry(chrid, name, level, job));
                        }
                    }
                    ps = con.prepareStatement("SELECT `characters_id`, `type`, `rotate`, `boardindex`, `local` FROM `mapleunion` WHERE `accounts_id` = ? AND `world` = ?");
                    ps.setInt(1, ret.accountid);
                    ps.setInt(2, ret.world);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final int charactersId = rs.getInt("characters_id");
                        final MapleUnionEntry union = ret.mapleUnion.getAllUnions().get(charactersId);
                        if (union != null) {
                            final int boardindex = rs.getInt("boardindex");
                            final int rotate = rs.getInt("rotate");
                            final int local = rs.getInt("local");
                            final int type = rs.getInt("type");
                            if (boardindex <= -1) {
                                continue;
                            }
                            MapleUnionEntry union2 = new MapleUnionEntry(charactersId, union.getName(), union.getLevel(), union.getJob());
                            union2.setBoardIndex(boardindex);
                            union2.setRotate(rotate);
                            union2.setLocal(local);
                            union2.setType(type);
                            ret.mapleUnion.getFightingUnions().put(charactersId, union2);
                        }
                    }

                    //加載角色能在能力技能
                    ps = con.prepareStatement("SELECT skillid, skilllevel, position, `rank` FROM innerskills WHERE characterid = ? LIMIT 3");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        int skid = rs.getInt("skillid"); //技能ID
                        skil = SkillFactory.getSkill(skid);
                        int skl = rs.getInt("skilllevel"); //當前技能等級
                        byte position = rs.getByte("position");  //內在技能位置
                        byte rank = (byte) Math.min(Math.max(rs.getByte("rank"), 0), 3);  //rank, C, B, A, and S
                        if (skil != null && skil.isInnerSkill() && position >= 1 && position <= 3) {
                            if (skl > skil.getMaxLevel()) {
                                skl = (byte) skil.getMaxLevel();
                            }
                            InnerSkillEntry InnerSkill = new InnerSkillEntry(skid, skl, position, rank, false);
                            ret.innerSkills[position - 1] = InnerSkill; //這個地方用的數組 數組是從 0 開始計算 所以減去1
                        }
                    }
                    rs.close();
                    ps.close();
                    //設置獲取精靈祝福和女皇祝福的等級信息
                    ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? AND id <> ? ORDER BY level DESC");
                    ps.setInt(1, ret.accountid);
                    ps.setInt(2, ret.id);
                    rs = ps.executeQuery();
                    int maxlevel_ = 0, maxlevel_2 = 0;
                    while (rs.next()) {
                        if (rs.getInt("id") != charid) { //去掉角色自己的ID
                            if (JobConstants.is皇家騎士團(rs.getShort("job"))) {
                                int maxlevel = (rs.getShort("level") / 5);
                                if (maxlevel > 24) {
                                    maxlevel = 24;
                                }
                                if (maxlevel > maxlevel_2 || maxlevel_2 == 0) {
                                    maxlevel_2 = maxlevel;
                                    ret.BlessOfEmpress_Origin = rs.getString("name");
                                }
                            }
                            int maxlevel = (rs.getShort("level") / 10);
                            if (maxlevel > 20) {
                                maxlevel = 20;
                            }
                            if (maxlevel > maxlevel_ || maxlevel_ == 0) {
                                maxlevel_ = maxlevel;
                                ret.BlessOfFairy_Origin = rs.getString("name");
                            }
                        }
                    }
                    if (ret.BlessOfFairy_Origin == null) {
                        ret.BlessOfFairy_Origin = ret.name;
                    }
                    int skillid = JobConstants.getBOF_ForJob(ret.job);
                    ret.skills.put(skillid, new SkillEntry(maxlevel_, (byte) 0, -1, 0, 0));
                    if (SkillFactory.getSkill(JobConstants.getEmpress_ForJob(ret.job)) != null) {
                        if (ret.BlessOfEmpress_Origin == null) {
                            ret.BlessOfEmpress_Origin = ret.BlessOfFairy_Origin;
                        }
                        ret.skills.put(JobConstants.getEmpress_ForJob(ret.job), new SkillEntry(maxlevel_2, (byte) 0, -1, 0, 0));
                    }
                    ps.close();
                    rs.close();
                    // END
                    //加載技能宏信息
                    ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    int position;
                    while (rs.next()) {
                        position = rs.getInt("position");
                        SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                        ret.skillMacros[position] = macro;
                    }
                    rs.close();
                    ps.close();
                    // 加載萌獸數據
                    ps = con.prepareStatement("SELECT * FROM familiars WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final int aiFamiliarID = ret.getAIFamiliarID();
                        final MonsterFamiliar monsterFamiliar = new MonsterFamiliar(aiFamiliarID, rs.getInt("familiar"), client.getAccID(), charid, rs.getString("name"), rs.getByte("grade"), rs.getByte("level"), rs.getInt("exp"), rs.getInt("skillid"), rs.getInt("option1"), rs.getInt("option2"), rs.getInt("option3"), rs.getByte("summon") != 0, rs.getByte("lock") != 0);
                        ret.getFamiliars().add(monsterFamiliar);
                        if (monsterFamiliar.isSummoned()) {
                            monsterFamiliar.initPad();
                            ret.initFamiliar(monsterFamiliar);
                        }
                    }
                    //加載角色鍵盤設置信息
                    for (int i = 0; i < FuncKeyMap.MAX_LAYOUT; i++) {
                        ret.funcKeyMaps = FuncKeyMap.load(con, charid, i, ret.funcKeyMaps);
                    }
                    //加載快捷鍵(quickslot)設置
                    ps = con.prepareStatement("SELECT `index`, `key` FROM quickslot WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    List<Pair<Integer, Integer>> quickslots = ret.quickslot.Layout();
                    while (rs.next()) {
                        quickslots.add(new Pair<>(rs.getInt("index"), rs.getInt("key")));
                    }
                    rs.close();
                    ps.close();
                    ret.quickslot.unchanged();
                    //加載角色地圖信息？
                    ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        int locationType = rs.getInt("locationtype");
                        if (locationType >= 0 && locationType < ret.savedLocations.length) {
                            ret.savedLocations[locationType] = rs.getInt("map");
                        }
                    }
                    rs.close();
                    ps.close();
                    //加載角色使用人氣的信息
                    ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    ret.lastfametime = 0;
                    ret.lastmonthfameids = new ArrayList<>(31);
                    while (rs.next()) {
                        ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                        ret.lastmonthfameids.add(rs.getInt("characterid_to"));
                    }
                    rs.close();
                    ps.close();
                    //加載角色使用好感度的信息
                    ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM lovelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 1");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    ret.lastlovetime = 0;
                    ret.lastdayloveids = new LinkedHashMap<>();
                    while (rs.next()) {
                        ret.lastlovetime = Math.max(ret.lastlovetime, rs.getTimestamp("when").getTime());
                        ret.lastdayloveids.put(rs.getInt("characterid_to"), rs.getTimestamp("when").getTime());
                    }
                    rs.close();
                    ps.close();
                    //未知
                    ps = con.prepareStatement("SELECT `accid_to`,`when` FROM battlelog WHERE accid = ? AND DATEDIFF(NOW(),`when`) < 30");
                    ps.setInt(1, ret.accountid);
                    rs = ps.executeQuery();
                    ret.lastmonthbattleids = new ArrayList<>();
                    while (rs.next()) {
                        ret.lastmonthbattleids.add(rs.getInt("accid_to"));
                    }
                    rs.close();
                    ps.close();
                    //加載好友信息
                    ret.buddylist.loadFromDb(charid);
                    //加載倉庫信息
                    ret.trunk = MapleTrunk.loadOrCreateFromDB(ret.accountid);
                    //加載商城信息
                    ret.cs = new CashShop(ret.accountid, charid, ret.getJob());
                    //加載禮物信息
                    ps = con.prepareStatement("SELECT sn FROM wishlist WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    int i = 0;
                    while (rs.next()) {
                        ret.wishlist[i] = rs.getInt("sn");
                        i++;
                    }
                    while (i < 12) {
                        ret.wishlist[i] = 0;
                        i++;
                    }
                    rs.close();
                    ps.close();
                    //加載縮地石保存信息
                    ps = con.prepareStatement("SELECT mapid,vip FROM trocklocations WHERE characterid = ? LIMIT 28");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    int r = 0;
                    int reg = 0;
                    int hyper = 0;
                    while (rs.next()) {
                        if (rs.getInt("vip") == 0) {
                            ret.regrocks[reg] = rs.getInt("mapid");
                            reg++;
                        } else if (rs.getInt("vip") == 1) {
                            ret.rocks[r] = rs.getInt("mapid");
                            r++;
                        } else if (rs.getInt("vip") == 2) {
                            ret.hyperrocks[hyper] = rs.getInt("mapid");
                            hyper++;
                        }
                    }
                    while (reg < 5) {
                        ret.regrocks[reg] = 999999999;
                        reg++;
                    }
                    while (r < 10) {
                        ret.rocks[r] = 999999999;
                        r++;
                    }
                    while (hyper < 13) {
                        ret.hyperrocks[hyper] = 999999999;
                        hyper++;
                    }
                    rs.close();
                    ps.close();
                    //加載道具寶寶信息
                    ps = con.prepareStatement("SELECT * FROM imps WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    r = 0;
                    while (rs.next()) {
                        ret.imps[r] = new MapleImp(rs.getInt("itemid"));
                        ret.imps[r].setLevel(rs.getByte("level"));
                        ret.imps[r].setState(rs.getByte("state"));
                        ret.imps[r].setCloseness(rs.getShort("closeness"));
                        ret.imps[r].setFullness(rs.getShort("fullness"));
                        r++;
                    }
                    rs.close();
                    ps.close();
                    //加載坐騎信息
                    ps = con.prepareStatement("SELECT * FROM mountdata WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw new RuntimeException("在數據庫中沒有找到角色的坐騎信息...");
                    }
                    Item mount = ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
                    ret.mount = new MapleMount(ret, mount != null ? mount.getItemId() : 0, 80001000, rs.getByte("Fatigue"), rs.getByte("Level"), rs.getInt("Exp"));
                    ps.close();
                    rs.close();
                    //加載藥劑罐信息
                    ps = con.prepareStatement("SELECT * FROM character_potionpots WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        ret.potionPot = new MaplePotionPot(charid, rs.getInt("itemId"), rs.getInt("hp"), rs.getInt("mp"), rs.getInt("maxValue"), rs.getLong("startDate"), rs.getLong("endDate"));
                    }
                    ps.close();
                    rs.close();
                    //加載自定義里程
                    ps = con.prepareStatement("SELECT * FROM character_credit WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.credit.put(rs.getString("name"), rs.getInt("value"));
                    }
                    ps.close();
                    rs.close();
                    /*
                      加載效果開關
                     */
                    ps = con.prepareStatement("SELECT * FROM effectswitch WHERE `characterid` = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.effectSwitch.add(rs.getInt("pos"));
                    }
                    ps.close();
                    rs.close();
                    /*
                      加載梳化間內容
                     */
                    ps = con.prepareStatement("SELECT * FROM salon WHERE characterid = ? ORDER BY position");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    String sMixValue;
                    byte nMixValue;
                    while (rs.next()) {
                        int type = rs.getInt("type");
                        if (ret.salon.containsKey(type)) {
                            int pos = rs.getInt("position");
                            ret.salon.get(type).remove(pos);
                            ret.salon.get(type).add(pos, rs.getInt("itemId"));
                        }
                    }
                    ps.close();
                    rs.close();
                    // 加載buylimit
                    ps = con.prepareStatement("SELECT * FROM `characters_buylimit` WHERE `characters_id` = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.buyLimit.computeIfAbsent(rs.getInt("shop_id"), NpcShopBuyLimit::new).update(rs.getInt("itemid"), rs.getInt("count"), rs.getTimestamp("data").getTime());
                    }
                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT * FROM `accounts_buylimit` WHERE `account_id` = ? AND world = ?");
                    ps.setInt(1, ret.accountid);
                    ps.setInt(2, ret.world);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.accountBuyLimit.computeIfAbsent(rs.getInt("shop_id"), NpcShopBuyLimit::new).update(rs.getInt("itemid"), rs.getInt("count"), rs.getTimestamp("data").getTime());
                    }
                    rs.close();
                    ps.close();
                    // 加載靈魂收集
                    ps = con.prepareStatement("SELECT * FROM `character_soulcollection` WHERE `characters_id` = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.soulCollection.put(rs.getInt("page"), rs.getInt("setsoul"));
                    }
                    rs.close();
                    ps.close();
                    // 加載靈魂收集
                    ps = con.prepareStatement("SELECT `recordid`, `data` FROM `accounts_mobcollection` WHERE `accounts_id` = ? AND `world` = ?");
                    ps.setInt(1, ret.accountid);
                    ps.setInt(2, ret.world);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.mobCollection.put(rs.getInt("recordid"), rs.getString("data"));
                    }
                    rs.close();
                    ps.close();
                    // 初始化角色屬性
                    for (Entry<Integer, VCoreSkillEntry> vcores : ret.getVCoreSkill().entrySet()) {
                        VCoreSkillEntry vcore = vcores.getValue();
                        if (vcore != null && vcore.getSlot() == 2) {
                            VMatrixSlot slot = ret.getVMatrixSlot().get(vcore.getIndex());
                            if (slot != null) {
                                slot.setIndex(vcores.getKey());
                                for (int j = 1; j <= 3; j++) {
                                    int vskillId = vcore.getSkill(j);
                                    Skill skill = SkillFactory.getSkill(vskillId);
                                    if (vskillId > 0 && skill != null) {
                                        SkillEntry skillEntry;
                                        if (vcore.getType() == 0 || vcore.getType() == 2) {
                                            if (ret.getSkills().containsKey(skill.getId())) {
                                                continue;
                                            }
                                            skillEntry = new SkillEntry(vcore.getLevel() + slot.getExtend(), skill.getMasterLevel(), -1, 0, 0, (byte) -1);
                                        } else {
                                            if (vcore.getType() != 1) {
                                                continue;
                                            }
                                            if (ret.getSkills().containsKey(skill.getId())) {
                                                skillEntry = ret.getSkills().get(skill.getId());
                                                skillEntry.skillevel = Math.min(skillEntry.skillevel + vcore.getLevel() + slot.getExtend(), skill.getTrueMax());
                                            } else {
                                                skillEntry = new SkillEntry(vcore.getLevel() + slot.getExtend(), skill.getMasterLevel(), -1, 0, 0, (byte) -1);
                                            }
                                        }
                                        ret.skills.put(skill.getId(), skillEntry);
                                    }
                                }
                            } else {
                                vcore.setSlot(1);
                            }
                        }
                    }
                    ret.stats.recalcLocalStats(true, ret);
                } else { // 不是在頻道加載
                    for (Pair<Item, MapleInventoryType> mit : ItemLoader.裝備道具.loadItems(true, charid).values()) {
                        ret.getInventory(mit.getRight()).addFromDB(mit.getLeft());
                    }
                    ret.stats.recalcPVPRank(ret);
                }
            }
        } catch (SQLException ess) {
            log.error("加載角色數據信息出錯...", ess);
            if (ret.getClient() != null) {
                ret.getClient().getSession().close();
            }
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignore) {
            }
        }
        return ret;
    }

    /*
     * 保存新角色到數據庫
     */
    public static void saveNewCharToDB(MapleCharacter chr, JobType type, boolean oldkey) {
        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnectionEx.getInstance().getConnection();
            ps = con.prepareStatement("SELECT count(id) FROM characters WHERE accountid = ?");
            ps.setInt(1, chr.accountid);
            rs = ps.executeQuery();

            int position = 0;
            if (rs.next()) {
                position = rs.getInt(1);
            }
            ps.close();
            rs.close();

            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);

            ps = con.prepareStatement("INSERT INTO characters (position, level, str, dex, luk, `int`, hp, mp, maxhp, maxmp, sp, ap, skincolor, gender, job, hair, face, map, meso, party, buddyCapacity, pets, decorate, subcategory, friendshippoints, burningChrType, burningChrTime, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", DatabaseConnectionEx.RETURN_GENERATED_KEYS);
            int index = 0;
            ps.setInt(++index, position);
            ps.setInt(++index, chr.level); // Level
            PlayerStats stat = chr.stats;
            ps.setShort(++index, stat.getStr()); // Str
            ps.setShort(++index, stat.getDex()); // Dex
            ps.setShort(++index, stat.getInt()); // Int
            ps.setShort(++index, stat.getLuk()); // Luk
            ps.setInt(++index, stat.getHp()); // Hp
            ps.setInt(++index, stat.getMp()); // Mp
            ps.setInt(++index, stat.getMaxHp(false)); // maxHp
            ps.setInt(++index, stat.getMaxMp(false)); // maxMp
            StringBuilder sps = new StringBuilder();
            for (int i = 0; i < chr.remainingSp.length; i++) {
                sps.append(chr.remainingSp[i]);
                sps.append(",");
            }
            String sp = sps.toString();
            ps.setString(++index, sp.substring(0, sp.length() - 1));
            ps.setShort(++index, chr.remainingAp); // Remaining AP

            ps.setByte(++index, chr.skinColor);
            ps.setByte(++index, chr.gender);
            ps.setShort(++index, chr.job);
            ps.setInt(++index, chr.hair);
            ps.setInt(++index, chr.face);
            ps.setInt(++index, ServerConfig.CHANNEL_PLAYER_BEGINNERMAP > -1 ? ServerConfig.CHANNEL_PLAYER_BEGINNERMAP : type.mapId); //設置出生地圖
            ps.setLong(++index, chr.meso.get()); // Meso 楓幣
            ps.setInt(++index, -1); // Party
            ps.setByte(++index, chr.buddylist.getCapacity()); // Buddylist
            ps.setString(++index, "-1,-1,-1");
            ps.setInt(++index, chr.decorate);
            ps.setInt(++index, chr.subcategory); //for now
            ps.setString(++index, chr.friendshippoints[0] + "," + chr.friendshippoints[1] + "," + chr.friendshippoints[2] + "," + chr.friendshippoints[3] + "," + chr.friendshippoints[4]);
            ps.setByte(++index, (byte) chr.burningChrType);
            if (chr.burningChrTime <= 0) {
                ps.setNull(++index, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(++index, new Timestamp(chr.burningChrTime));
            }
            ps.setInt(++index, chr.accountid);
            ps.setString(++index, chr.name);
            ps.setByte(++index, chr.world);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                chr.id = rs.getInt(1);
            } else {
                ps.close();
                rs.close();
                log.error("生成新角色到數據庫出錯...");
            }
            ps.close();
            rs.close();
            /*
             * 保存新角色的任務信息
             */
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `account`, `world`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)", DatabaseConnectionEx.RETURN_GENERATED_KEYS);
            pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
            for (MapleQuestStatus q : chr.quests.values()) {
                if (q.isWorldShare()) {
                    ps.setInt(1, chr.accountid);
                    ps.setInt(2, chr.world);
                    ps.setNull(3, Types.INTEGER);
                } else {
                    ps.setNull(1, Types.INTEGER);
                    ps.setNull(2, Types.INTEGER);
                    ps.setInt(3, chr.id);
                }
                ps.setInt(4, q.getQuest().getId());
                ps.setInt(5, q.getStatus());
                ps.setInt(6, (int) (q.getCompletionTime() / 1000));
                ps.setInt(7, q.getForfeited());
                ps.setString(8, q.getCustomData());
                ps.execute();
                rs = ps.getGeneratedKeys();
                if (q.hasMobKills()) {
                    rs.next();
                    for (int mob : q.getMobKills().keySet()) {
                        pse.setLong(1, rs.getLong(1));
                        pse.setInt(2, mob);
                        pse.setInt(3, q.getMobKills(mob));
                        pse.execute();
                    }
                }
                rs.close();
            }
            ps.close();
            pse.close();
            /*
             * 保存新角色的其他設置信息
             */
            ps = con.prepareStatement("INSERT INTO character_keyvalue (`characterid`, `key`, `value`) VALUES (?, ?, ?)");
            ps.setInt(1, chr.id);
            //    for (Entry<String, String> key : chr.keyValue.entrySet()) {
            ps.setString(2, "DAMAGE_SKIN");
            ps.setString(3, "0");
            ps.execute();
            //   }
            ps.close();
            /*
             * 保存角色的技能
             */
            ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration, teachId, teachTimes) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            for (Entry<Integer, SkillEntry> skill : chr.skills.entrySet()) {
                if (SkillConstants.isApplicableSkill(skill.getKey())) { //do not save additional skills
                    ps.setInt(2, skill.getKey());
                    ps.setInt(3, skill.getValue().skillevel);
                    ps.setByte(4, (byte) skill.getValue().masterlevel);
                    ps.setLong(5, skill.getValue().expiration);
                    ps.setInt(6, skill.getValue().teachId);
                    ps.setInt(7, skill.getValue().teachTimes);
                    ps.execute();
                }
            }
            ps.close();
            /*
             * 生成新角色的背包空間數量
             */
            ps = con.prepareStatement("INSERT INTO inventoryslot (characters_id, `equip`, `use`, `setup`, `etc`, `cash`, `decoration`) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setShort(2, (byte) 48); // Eq
            ps.setShort(3, (byte) 48); // Use
            ps.setShort(4, (byte) 48); // Setup
            ps.setShort(5, (byte) 48); // ETC
            ps.setShort(6, (byte) MapleInventory.MAX_SLOT_LIMIT); // Cash
            ps.setShort(7, (byte) MapleInventory.MAX_SLOT_LIMIT); // Decoration
            ps.execute();
            ps.close();
            /*
             * 生成新角色的坐騎信息
             */
            ps = con.prepareStatement("INSERT INTO mountdata (characterid, `Level`, `Exp`, `Fatigue`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setByte(2, (byte) 1);
            ps.setInt(3, 0);
            ps.setByte(4, (byte) 0);
            ps.execute();
            ps.close();
            /*
             * 生成新角色的鍵盤設置信息
             */
            FuncKeyMap.init(con, chr.id, oldkey);

            // 設定快捷鍵
            // 基礎
            int[] qs_basic = {42, 82, 71, 73, 29, 83, 79, 81, 2, 3, 4, 5, 16, 17, 18, 19, 6, 7, 8, 9, 20, 30, 31, 32, 10, 11, 33, 34, 37, 38, 49, 50};
            // 進階
            int[] qs_adv = {16, 17, 18, 19, 30, 31, 32, 33, 2, 3, 4, 5, 29, 56, 44, 45, 6, 7, 8, 9, 46, 22, 23, 36, 10, 11, 37, 38, 24, 25, 49, 50};
            MapleQuickSlot qs = chr.getQuickSlot();
            if (qs == null) {
                qs = new MapleQuickSlot();
            }
            for (int i = 0; i < 32; i++) {
                qs.addQuickSlot(i, (oldkey ? qs_basic[i] : qs_adv[i]));
            }
            qs.saveQuickSlots(con, chr.id);

            List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
            for (MapleInventory iv : chr.inventory) {
                for (Item item : iv.list()) {
                    itemsWithType.add(new Pair<>(item, iv.getType()));
                }
            }
            ItemLoader.裝備道具.saveItems(con, itemsWithType, chr.id);
            con.commit();
        } catch (Exception e) {
            log.error("[charsave] Error saving character data", e);
            try {
                con.rollback();
            } catch (SQLException ex) {
                log.error("[charsave] Error Rolling Back", ex);
            }
        } finally {
            try {
                if (pse != null) {
                    pse.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (SQLException e) {
                log.error("[charsave] Error going back to autocommit mode", e);
            }
        }
    }

    public static void deleteWhereCharacterId(Connection con, String sql, int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public static void deleteWhereCharacterId_NoLock(Connection con, String sql, int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.execute();
        ps.close();
    }

    public static boolean ban(String id, String reason, boolean accountId, int gmlevel, boolean hellban) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps;
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.execute();
                ps.close();
                return true;
            }
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }
            boolean ret = false;
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int z = rs.getInt(1);
                PreparedStatement psb = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ? AND gm < ?");
                psb.setString(1, reason);
                psb.setInt(2, z);
                psb.setInt(3, gmlevel);
                psb.execute();
                psb.close();
                if (gmlevel > 100) { //如果是最高管理員封號 就進行下面的處理
                    PreparedStatement psa = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                    psa.setInt(1, z);
                    ResultSet rsa = psa.executeQuery();
                    if (rsa.next()) {
                        String sessionIP = rsa.getString("sessionIP");
                        if (sessionIP != null && sessionIP.matches("/[0-9]{1,3}\\..*")) {
                            PreparedStatement psz = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                            psz.setString(1, sessionIP);
                            psz.execute();
                            psz.close();
                        }
                        String macData = rsa.getString("macs");
                        if (macData != null && !macData.equalsIgnoreCase("00-00-00-00-00-00") && macData.length() >= 17) {
                            PreparedStatement psm = con.prepareStatement("INSERT INTO macbans VALUES (DEFAULT, ?)");
                            psm.setString(1, macData);
                            psm.execute();
                            psm.close();
                        }
                        if (hellban) {
                            PreparedStatement pss = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE email = ?" + (sessionIP == null ? "" : " OR SessionIP = ?"));
                            pss.setString(1, reason);
                            pss.setString(2, rsa.getString("email"));
                            if (sessionIP != null) {
                                pss.setString(3, sessionIP);
                            }
                            pss.execute();
                            pss.close();
                        }
                    }
                    rsa.close();
                    psa.close();
                }
                ret = true;
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
            System.err.println("Error while banning" + ex);
        }
        return false;
    }

    public static byte checkExistance(int accid, int cid) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM hiredmerch WHERE accountid = ? AND characterid = ?")) {
                ps.setInt(1, accid);
                ps.setInt(2, cid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ps.close();
                        rs.close();
                        return 1;
                    }
                }
            }
            return 0;
        } catch (SQLException se) {
            return -1;
        }
    }

    public static void removePartTime(int cid) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM parttime WHERE cid = ?")) {
                ps.setInt(1, cid);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            System.out.println("無法刪除打工信息: " + ex);
        }
    }

    public static void addPartTime(MaplePartTimeJob partTime) {
        if (partTime.getCharacterId() < 1) {
            return;
        }
        addPartTime(partTime.getCharacterId(), partTime.getJob(), partTime.getTime(), partTime.getReward());
    }

    public static void addPartTime(int cid, byte job, long time, int reward) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO parttime (cid, job, time, reward) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, cid);
                ps.setByte(2, job);
                ps.setLong(3, time);
                ps.setInt(4, reward);
                ps.execute();
            }
        } catch (SQLException ex) {
            System.out.println("無法添加打工信息: " + ex);
        }
    }

    public static MaplePartTimeJob getPartTime(int cid) {
        MaplePartTimeJob partTime = new MaplePartTimeJob(cid);
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM parttime WHERE cid = ?")) {
                ps.setInt(1, cid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        partTime.setJob(rs.getByte("job"));
                        partTime.setTime(rs.getLong("time"));
                        partTime.setReward(rs.getInt("reward"));
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("無法查詢打工信息: " + ex);
        }
        return partTime;
    }

    public static boolean hasSkill(final Map<Integer, SkillEntry> cskills, final int skillid, int level) {
        if (cskills.get(skillid) != null) {
            return cskills.get(skillid).skillevel == level && cskills.containsKey(skillid);
        }
        return cskills.containsKey(skillid);
    }

    public static MapleCharacter getOnlineCharacterById(int cid) {
        return WorldFindService.getInstance().findCharacterById(cid);
    }

    public static MapleCharacter getCharacterById(int n2) {
        MapleCharacter player = getOnlineCharacterById(n2);
        return player == null ? MapleCharacter.loadCharFromDB(n2, new MapleClient(null, null, null), true) : player;
    }

    public static int getLevelbyid(int cid) {
        int level = -1;
        try (Connection conn = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT `level` FROM characters WHERE id = ?")) {
                ps.setInt(1, cid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        level = rs.getInt("level");
                    }
                }
            }
        } catch (SQLException e) {
            log.error("讀取角色等級失敗", e);
        }
        return level;
    }

    private static int getJobLvSP(int nJob, int nLevel) {
        if (JobConstants.is零轉職業(nJob) || JobConstants.is皮卡啾(nJob) || JobConstants.is雪吉拉(nJob)) {
            return 0;
        }
        if (nLevel > 10) {
            int num = 0;
            if (nLevel <= 100 || (JobConstants.is神之子(nJob) && nLevel <= 200)) {
                num = 3;
            } else if (nLevel <= 140) {
                num = 2 + (int) Math.ceil(nLevel % 100 / 10.0);
                if (nLevel % 10 % 3 == 0) {
                    num *= 2;
                }
            }
            if (JobConstants.is影武者(nJob)) {
                if (nLevel > 30 && nLevel <= 50) {
                    num = 4;
                }
            }
            return num;
        }
        return 0;
    }

    private static Pair<Integer, Integer> getJobChangeSP(int nJob, int nSubcategory, int skillBook) {
        if (JobConstants.is零轉職業(nJob) || JobConstants.is管理員(nJob) || JobConstants.is神之子(nJob) || JobConstants.is皮卡啾(nJob) || JobConstants.is雪吉拉(nJob)) {
            return null;
        }

        int changeSp = 5;
        if (nSubcategory != 1) {
            switch (skillBook) {
                case 0:
                    if (JobConstants.is夜光(nJob)) {
                        changeSp = 30;
                    } else {
                        changeSp = 5;
                    }
                    break;
                case 1:
                    if (JobConstants.is隱月(nJob) || JobConstants.is劍豪(nJob) || JobConstants.is陰陽師(nJob)) {
                        changeSp = 5;
                    } else if (JobConstants.is菈菈(nJob)) {
                        changeSp = 9;
                    } else {
                        changeSp = 4;
                    }
                    break;
                case 2:
                    if (JobConstants.is隱月(nJob) || JobConstants.is劍豪(nJob) || JobConstants.is陰陽師(nJob)) {
                        changeSp = 5;
                    } else {
                        changeSp = 4;
                    }
                    break;
                case 3:
                    changeSp = 3;
                    break;
            }
        } else {
            switch (skillBook) {
                case 4:
                    changeSp = 0;
                    break;
                case 5:
                    changeSp = 3;
                    break;
            }
        }
        return new Pair(skillBook, changeSp);
    }

    public static void addReward(int accid, int cid, long start, long end, int type, long amount, int itemId, String desc) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO rewards (`accid`, `cid`, `start`, `end`, `type`, `amount`, `itemId`, `desc`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                if (accid > 0) {
                    ps.setInt(1, accid);
                } else {
                    ps.setNull(1, Types.INTEGER);
                }
                if (cid > 0) {
                    ps.setInt(2, cid);
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setLong(3, start);
                ps.setLong(4, end);
                ps.setInt(5, type);
                ps.setLong(6, amount);
                ps.setInt(7, itemId);
                ps.setString(8, desc);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Unable to obtain reward: ", e);
        }
    }

    public int[] getDeathCounts() {
        return this.deathCounts;
    }

    public void setDeathCounts(final int[] deathCounts) {
        this.deathCounts = deathCounts;
    }

    public int liveCounts() {
        int c = 0;
        for (int i = 0; i < this.deathCounts.length; ++i) {
            if (this.deathCounts[i] == 1) {
                ++c;
            }
        }
        return c;
    }

    public int DeadCounts() {
        int c = 0;
        for (int i = 0; i < this.deathCounts.length; ++i) {
            if (this.deathCounts[i] == 2 || this.deathCounts[i] == 0) {
                ++c;
            }
        }
        return c;
    }

    public MapleUnion getMapleUnion() {
        return mapleUnion;
    }

    public void setMapleUnionChanged(boolean b) {
        changed_mapleUnion = b;
    }

    public void getPercentDamage(final MapleMonster monster, final int skillid, final int skillLevel, final int percent, final boolean show) {
        final MapleCharacter chr = this;
        int reduce = 0;
        final int minushp = -(int) (chr.getStat().getCurrentMaxHp() * (percent - reduce) / 100L);
        int oldskillid;
        int effectid = 0;
        this.getMap().broadcastMessage(CField.showEffect(chr, skillid, effectid, 45, 0, 0, (byte) 0, true, null, null, null, null));

        this.getMap().broadcastMessage(CField.showEffect(chr, skillid, effectid, 45, 0, 0, (byte) 0, true, null, null, null, null));
        this.getMap().broadcastMessage(CField.showEffect(chr, 0, minushp, 36, 0, 0, (byte) 0, true, null, null, null, null));
        this.getMap().broadcastMessage(CField.showEffect(chr, 0, minushp, 36, 0, 0, (byte) 0, false, null, null, null, null));
        chr.addHP(minushp);
    }

    //    public synchronized void saveToCache() {
//        saveToCache(new CharacterTransfer(this));
//    }
//
//    public synchronized void saveToCache(CharacterTransfer ct) {
//        try {
//            RedisUtil.hset(RedisUtil.KEYNAMES.PLAYER_DATA.getKeyName(), String.valueOf(id), JsonUtil.getMapperInstance().writeValueAsString(ct));
//        } catch (JsonProcessingException e) {
//            log.error("更新緩存出錯", e);
//        }
//    }
//    public void clearCache() {
//        RedisUtil.hdel(RedisUtil.KEYNAMES.PLAYER_DATA.getKeyName(), String.valueOf(id));
//    }
    public Runnable saveToDB(boolean dc, boolean fromcs) {
        ChannelServer.getSaveExecutor().execute(() -> {
            try {
                saveToDB0(dc, fromcs);
            } catch (Exception e) {
                log.info("[Save char to DataBase Error]", e);
            }
        });
        return null;
    }

    /*
     * 保存角色到數據庫
     */
    public void saveToDB0(boolean dc, boolean fromcs) {
        saveOnlineTime();

        if (isSaveing && !dc) {
//            log.info(MapleClient.getLogMessage(getClient(), "正在保存數據，本次操作返回."));
            return;
        }
        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

//        Lock lock = World.Client.getLockById(id);
        try {
//            saveLock.lock();
//            lock.lock();
            con = DatabaseConnectionEx.getInstance().getConnection();
//            while (con == null) {
//                con = Database.DatabaseLoader.DatabaseConnectionEx.getInstance().getConnection();
//            }
            isSaveing = true;
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);

            ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                ps.close();
                rs.close();

                ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpApUsed = ?, mpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, pets = ?, subcategory = ?, marriageId = ?, currentrep = ?, totalrep = ?, gachexp = ?, fatigue = ?, charm = ?, charisma = ?, craft = ?, insight = ?, sense = ?, will = ?, totalwins = ?, totallosses = ?, pvpExp = ?, pvpPoints = ?, decorate = ?, beans = ?, warning = ?, reborns = ?, reborns1 = ?, reborns2 = ?, reborns3 = ?, apstorage = ?, honor = ?, love = ?, playerPoints = ?, playerEnergy = ?, pvpDeaths = ?, pvpKills = ?, pvpVictory = ?, todayonlinetime = ?, totalonlinetime = ?, friendshiptoadd = ?, friendshippoints = ?, name = ?, wp = ?, salon_hair = ?, salon_face = ?, salon_skin = ?, burningChrType = ?, burningChrTime = ? WHERE id = ?", DatabaseConnectionEx.RETURN_GENERATED_KEYS);
                int index = 0;
                ps.setInt(++index, level);
                ps.setInt(++index, fame);
                ps.setShort(++index, stats.getStr());
                ps.setShort(++index, stats.getDex());
                ps.setShort(++index, stats.getLuk());
                ps.setShort(++index, stats.getInt());
                ps.setLong(++index, level >= GameConstants.MAX_LEVEL ? 0 : Math.abs(exp.get()));
                ps.setInt(++index, stats.getHp() < 1 ? 50 : stats.getHp());
                ps.setInt(++index, stats.getMp());
                ps.setInt(++index, stats.getMaxHp(false));
                ps.setInt(++index, stats.getMaxMp(false));
                StringBuilder sps = new StringBuilder();
                for (int aRemainingSp : remainingSp) {
                    sps.append(aRemainingSp);
                    sps.append(",");
                }
                String sp = sps.toString();
                ps.setString(++index, sp.substring(0, sp.length() - 1));
                ps.setShort(++index, remainingAp);
                ps.setByte(++index, skinColor);
                ps.setByte(++index, gender);
                ps.setShort(++index, job);
                ps.setInt(++index, hair);
                ps.setInt(++index, face);
                if (!fromcs && map != null) {
                    if (map.getForcedReturnId() != 999999999 && map.getForcedReturnMap() != null) {
                        mapid = map.getForcedReturnId();
                    } else {
                        mapid = stats.getHp() < 1 ? map.getReturnMapId() : map.getId();
                    }
                }
                ps.setInt(++index, GameConstants.getOverrideReturnMap(mapid));
                ps.setLong(++index, meso.get());
                ps.setShort(++index, hpApUsed);
                ps.setShort(++index, mpApUsed);
                if (map == null) {
                    ps.setByte(++index, (byte) 0);
                } else {
                    MaplePortal closest = map.findClosestSpawnpoint(getPosition());
                    ps.setByte(++index, (byte) (closest != null ? closest.getId() : 0));
                }
                ps.setInt(++index, party == null ? -1 : party.getId());
                ps.setShort(++index, buddylist.getCapacity());
                StringBuilder petz = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    if (spawnPets[i] != null && spawnPets[i].getSummoned()) {
                        spawnPets[i].saveToDb();
                        petz.append(spawnPets[i].getInventoryPosition());
                        petz.append(",");
                    } else {
                        petz.append("-1,");
                    }
                }
                String petstring = petz.toString();
                ps.setString(++index, petstring.substring(0, petstring.length() - 1));
                ps.setByte(++index, subcategory);
                ps.setInt(++index, marriageId);
                ps.setInt(++index, currentrep);
                ps.setInt(++index, totalrep);
                ps.setInt(++index, gachexp);
                ps.setShort(++index, fatigue);
                ps.setInt(++index, traits.get(MapleTraitType.charm).getTotalExp());
                ps.setInt(++index, traits.get(MapleTraitType.charisma).getTotalExp());
                ps.setInt(++index, traits.get(MapleTraitType.craft).getTotalExp());
                ps.setInt(++index, traits.get(MapleTraitType.insight).getTotalExp());
                ps.setInt(++index, traits.get(MapleTraitType.sense).getTotalExp());
                ps.setInt(++index, traits.get(MapleTraitType.will).getTotalExp());
                ps.setInt(++index, totalWins);
                ps.setInt(++index, totalLosses);
                ps.setInt(++index, pvpExp);
                ps.setInt(++index, pvpPoints);
                // 魔族之紋
                ps.setInt(++index, decorate);
                // 豆豆信息
                ps.setInt(++index, beans);
                // 警告次數
                ps.setInt(++index, warning);
                // 轉生繫統
                ps.setInt(++index, reborns);
                ps.setInt(++index, reborns1);
                ps.setInt(++index, reborns2);
                ps.setInt(++index, reborns3);
                ps.setInt(++index, apstorage);
                // 榮譽系統
                ps.setInt(++index, honor);
                // 好感度
                ps.setInt(++index, love);
                // 新增變量
                ps.setInt(++index, playerPoints);
                ps.setInt(++index, playerEnergy);
                // Pvp變量
                ps.setInt(++index, pvpDeaths);
                ps.setInt(++index, pvpKills);
                ps.setInt(++index, pvpVictory);
                ps.setInt(++index, todayonlinetime + (int) ((System.currentTimeMillis() - todayonlinetimestamp.getTime()) / 60000));
                ps.setInt(++index, totalonlinetime + (int) ((System.currentTimeMillis() - todayonlinetimestamp.getTime()) / 60000));
                ps.setInt(++index, friendshiptoadd);
                ps.setString(++index, friendshippoints[0] + "," + friendshippoints[1] + "," + friendshippoints[2] + "," + friendshippoints[3] + "," + friendshippoints[4]);
                // 保存角色名字和ID
                ps.setString(++index, name);
                ps.setInt(++index, weaponPoint);
                ps.setInt(++index, !salon.containsKey(3) ? 3 : salon.get(3).size());
                ps.setInt(++index, !salon.containsKey(2) ? 3 : salon.get(2).size());
                ps.setInt(++index, !salon.containsKey(1) ? 0 : salon.get(1).size());
                ps.setByte(++index, (byte) burningChrType);
                if (burningChrTime <= 0) {
                    ps.setNull(++index, Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(++index, new Timestamp(burningChrTime));
                }
                ps.setInt(++index, id);
                if (ps.executeUpdate() < 1) {
                    ps.close();
                    log.error("Character not in DatabaseFile.database (" + id + ")");
                }
                ps.close();
                /*
                 * 儲存美容相簿數據
                 */
                deleteWhereCharacterId(con, "DELETE FROM salon WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO salon (characterid, type, position, itemId) VALUES (?, ?, ?, ?)");
                ps.setInt(1, id);
                for (Entry<Integer, List<Integer>> entry : salon.entrySet()) {
                    ps.setInt(2, entry.getKey());
                    int i = 0;
                    for (int itemID : entry.getValue()) {
                        if (itemID != 0) {
                            ps.setInt(3, i);
                            ps.setInt(4, itemID);
                            ps.execute();
                        }
                        i++;
                    }
                }
                ps.close();
                /*
                 * 保存技能宏設置
                 */
                if (changed_skillmacros) {
                    deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
                    for (int i = 0; i < 5; i++) {
                        SkillMacro macro = skillMacros[i];
                        if (macro != null) {
                            ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
                            ps.setInt(1, id);
                            ps.setInt(2, macro.getSkill1());
                            ps.setInt(3, macro.getSkill2());
                            ps.setInt(4, macro.getSkill3());
                            ps.setString(5, macro.getName());
                            ps.setInt(6, macro.getShout());
                            ps.setInt(7, i);
                            ps.execute();
                            ps.close();
                        }
                    }
                }
                /*
                 * 保存道具欄的數量信息
                 */
                deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characters_id = ?");
                ps = con.prepareStatement("INSERT INTO inventoryslot (`characters_id`, `equip`, `use`, `setup`, `etc`, `cash`, `decoration`) VALUES (?, ?, ?, ?, ?, ?, ?)");
                ps.setInt(1, id);
                ps.setShort(2, getInventory(MapleInventoryType.EQUIP).getSlotLimit());
                ps.setShort(3, getInventory(MapleInventoryType.USE).getSlotLimit());
                ps.setShort(4, getInventory(MapleInventoryType.SETUP).getSlotLimit());
                ps.setShort(5, getInventory(MapleInventoryType.ETC).getSlotLimit());
                ps.setShort(6, getInventory(MapleInventoryType.CASH).getSlotLimit());
                ps.setShort(7, getInventory(MapleInventoryType.DECORATION).getSlotLimit());
                ps.execute();
                ps.close();
                /*
                 * 保存裝備信息
                 */
                List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
                for (MapleInventory iv : inventory) {
                    for (Item item : iv.list()) {
                        itemsWithType.add(new Pair<>(item, iv.getType()));
                    }
                }
                if (getTrade() != null && dc) {
                    MapleTrade.cancelTrade(getTrade(), client, this);
                }
                ItemLoader.裝備道具.saveItems(con, itemsWithType, id);
                /*
                 * 保存角色的一些特殊操作處理
                 */
                if (changed_keyValue) {
                    deleteWhereCharacterId(con, "DELETE FROM character_keyvalue WHERE characterid = ?");
                    ps = con.prepareStatement("INSERT INTO character_keyvalue (`characterid`, `key`, `value`) VALUES (?, ?, ?)");
                    ps.setInt(1, id);
                    for (Entry<String, String> key : keyValue.entrySet()) {
                        ps.setString(2, key.getKey());
                        ps.setString(3, key.getValue());
                        ps.execute();
                    }
                    ps.close();
                }
                /*
                 * 保存任務信息
                 */
                if (changed_questinfo) {
                    deleteWhereCharacterId(con, "DELETE FROM questinfo WHERE characterid = ?");
                    ps = con.prepareStatement("INSERT INTO questinfo (`characterid`, `quest`, `customData`) VALUES (?, ?, ?)");
                    ps.setInt(1, id);
                    for (Entry<Integer, String> q : questinfo.entrySet()) {
                        ps.setInt(2, q.getKey());
                        ps.setString(3, q.getValue());
                        ps.execute();
                    }
                    ps.close();
                }
                if (changed_worldshareinfo) {
                    SqlTool.update(con, "DELETE FROM accounts_questinfo WHERE accounts_id = ? and world = ?", accountid, world);
                    ps = con.prepareStatement("INSERT INTO accounts_questinfo (`accounts_id`, `world`, `quest`, `customData`) VALUES (?, ?, ?, ?)");
                    ps.setInt(1, accountid);
                    ps.setInt(2, world);
                    for (Entry<Integer, String> q : worldShareInfo.entrySet()) {
                        ps.setInt(3, q.getKey());
                        ps.setString(4, q.getValue());
                        ps.execute();
                    }
                    ps.close();
                }
                /*
                 * 保存任務狀態信息
                 */
                deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `account`, `world`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)", DatabaseConnectionEx.RETURN_GENERATED_KEYS);
                pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
                List<MapleQuestStatus> lquests = new ArrayList<>(quests.values());
                ps.setInt(3, id);
                for (MapleQuestStatus q : lquests) {
                    if (q.isWorldShare()) {
                        ps.setInt(1, accountid);
                        ps.setInt(2, world);
                    } else {
                        ps.setNull(1, Types.INTEGER);
                        ps.setNull(2, Types.INTEGER);
                    }
                    ps.setInt(4, q.getQuest().getId());
                    ps.setInt(5, q.getStatus());
                    ps.setInt(6, (int) (q.getCompletionTime() / 1000));
                    ps.setInt(7, q.getForfeited());
                    ps.setString(8, q.getCustomData());
                    ps.execute();
                    rs = ps.getGeneratedKeys();
                    if (q.hasMobKills()) {
                        rs.next();
                        for (int mob : q.getMobKills().keySet()) {
                            pse.setLong(1, rs.getLong(1));
                            pse.setInt(2, mob);
                            pse.setInt(3, q.getMobKills(mob));
                            pse.execute();
                        }
                    }
                    rs.close();
                }
                ps.close();
                pse.close();
                /*
                 * 保存技能數據信息
                 */
                if (changed_skills) {
                    deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
                    ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration, teachId, teachTimes, position) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    ps.setInt(1, id);
                    for (Entry<Integer, SkillEntry> skill : skills.entrySet()) {
                        if (SkillConstants.isApplicableSkill(skill.getKey())) { //do not save additional skills
                            ps.setInt(2, skill.getKey());
                            ps.setInt(3, skill.getValue().skillevel);
                            ps.setByte(4, (byte) skill.getValue().masterlevel);
                            ps.setLong(5, skill.getValue().expiration);
                            ps.setInt(6, skill.getValue().teachId);
                            ps.setInt(7, skill.getValue().teachTimes);
                            ps.setByte(8, skill.getValue().position);
                            ps.execute();
                        }
                    }
                    ps.close();
                }
                /*
                 * 保存內在技能數據信息
                 */
                if (changed_innerSkills) {
                    deleteWhereCharacterId(con, "DELETE FROM innerskills WHERE characterid = ?");
                    for (int i = 0; i < 3; i++) {
                        InnerSkillEntry InnerSkill = innerSkills[i];
                        if (InnerSkill != null) {
                            ps = con.prepareStatement("INSERT INTO innerskills (characterid, skillid, skilllevel, position, `rank`) VALUES (?, ?, ?, ?, ?)");
                            ps.setInt(1, id);
                            ps.setInt(2, InnerSkill.getSkillId());
                            ps.setInt(3, InnerSkill.getSkillLevel());
                            ps.setByte(4, InnerSkill.getPosition());
                            ps.setByte(5, InnerSkill.getRank());
                            ps.execute();
                            ps.close();
                        }
                    }
                }
                /*
                 * 保存冒險聯盟數據
                 */
                if (changed_mapleUnion) {
                    ps = con.prepareStatement("DELETE FROM `mapleunion` WHERE `accounts_id` = ? AND `world` = ?");
                    ps.setInt(1, accountid);
                    ps.setInt(2, world);
                    ps.execute();
                    ps = con.prepareStatement("INSERT INTO mapleunion (`accounts_id`, `world`, `characters_id`, `type`, `rotate`, `boardindex`, `local`) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    ps.setInt(1, accountid);
                    ps.setInt(2, world);
                    for (MapleUnionEntry union : mapleUnion.getFightingUnions().values()) {
                        ps.setInt(3, union.getCharacterId());
                        ps.setInt(4, union.getType());
                        ps.setInt(5, union.getRotate());
                        ps.setInt(6, union.getBoardIndex());
                        ps.setInt(7, union.getLocal());
                        ps.execute();
                    }
                    ps.close();
                }
                /*
                 * 保存技能冷取時間信息
                 */
                List<MapleCoolDownValueHolder> coolDownInfo = getCooldowns();
                if (dc && coolDownInfo.size() > 0) {
                    ps = con.prepareStatement("INSERT INTO skills_cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)");
                    ps.setInt(1, getId());
                    for (MapleCoolDownValueHolder cooling : coolDownInfo) {
                        ps.setInt(2, cooling.skillId);
                        ps.setLong(3, cooling.startTime);
                        ps.setLong(4, cooling.length);
                        ps.execute();
                    }
                    ps.close();
                }
                /*
                 * 保存傳送點位置信息
                 */
                if (changed_savedlocations) {
                    deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
                    ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`) VALUES (?, ?, ?)");
                    ps.setInt(1, id);
                    for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                        if (savedLocationType.getValue() >= 0 && savedLocations.length > savedLocationType.getValue() && savedLocations[savedLocationType.getValue()] != -1) {
                            ps.setInt(2, savedLocationType.getValue());
                            ps.setInt(3, savedLocations[savedLocationType.getValue()]);
                            ps.execute();
                        }
                    }
                    ps.close();
                }
                /*
                 * 保存角色被舉報的信息
                 */
                if (changed_reports) {
                    deleteWhereCharacterId(con, "DELETE FROM reports WHERE characterid = ?");
                    ps = con.prepareStatement("INSERT INTO reports VALUES(DEFAULT, ?, ?, ?)");
                    for (Entry<ReportType, Integer> achid : reports.entrySet()) {
                        ps.setInt(1, id);
                        ps.setByte(2, achid.getKey().i);
                        ps.setInt(3, achid.getValue());
                        ps.execute();
                    }
                    ps.close();
                }
                /*
                 * 保存好友信息
                 */
                if (buddylist.changed()) {
                    deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ?");
                    ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, ?)");
                    ps.setInt(1, id);
                    for (BuddylistEntry entry : buddylist.getBuddies()) {
                        ps.setInt(2, entry.getCharacterId());
                        ps.setInt(3, entry.isVisible() ? 5 : 7);
                        ps.execute();
                    }
                    ps.close();
                    buddylist.setChanged(false);
                }
                ps = con.prepareStatement("UPDATE accounts SET `mPoints` = ? WHERE id = ?");
                ps.setInt(1, client.getMaplePoints());
                ps.setInt(2, accountid);
                ps.execute();
                ps.close();
                /*
                 * 保存角色樂豆點信息
                 * 此字段已过时，废弃
                 */
//                ps = con.prepareStatement("UPDATE accounts SET `points` = ?, `vpoints` = ? WHERE id = ?");
//                ps.setInt(1, points);
//                ps.setInt(2, vpoints);
//                ps.setInt(3, accountid);
//                ps.execute();
//                ps.close();
                /*
                 * 保存倉庫信息
                 */
                if (trunk != null) {
                    trunk.saveToDB(con);
                }
                /*
                 * 保存商城信息
                 */
                if (cs != null) {
                    cs.save(con);
                }
                PlayerNPC.updateByCharId(con, this);
                /*
                 * 保存鍵盤設置信息
                 */
                for (int i = 0; i < FuncKeyMap.MAX_LAYOUT; i++) {
                    funcKeyMaps.get(i).save(con, id, i);
                }
                /*
                 * 保存QuickSlot設置信息
                 */
                quickslot.saveQuickSlots(con, id);
                /*
                 * 保存坐騎信息
                 */
                if (mount != null) {
                    mount.saveMount(con, id);
                }
                /*
                 * 保存機器人信息
                 */
                if (android != null) {
                    android.saveToDb(con);
                }
                /*
                 * 保存Pvp屬性信息
                 */
                if (pvpStats != null) {
                    pvpStats.saveToDb(con, accountid);
                }
                /*
                 * 保存藥劑罐信息
                 */
                if (potionPot != null) {
                    potionPot.saveToDb(con);
                }
                /*
                 * 保存萌獸信息
                 */
                if (changed_familiars) {
                    deleteWhereCharacterId(con, "DELETE FROM familiars WHERE characterid = ?");
                    ps = con.prepareStatement("INSERT INTO familiars (characterid, familiar, name, level, exp, grade, skillid, option1, option2, option3, summon, `lock`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    ps.setInt(1, id);
                    for (MonsterFamiliar familiar : familiars) {
                        if (familiar.getFamiliar() == 0) {
                            continue;
                        }
                        ps.setInt(2, familiar.getFamiliar());
                        ps.setString(3, familiar.getName());
                        ps.setInt(4, familiar.getLevel());
                        ps.setInt(5, familiar.getExp());
                        ps.setInt(6, familiar.getGrade());
                        ps.setInt(7, familiar.getSkill());
                        ps.setInt(8, familiar.getOption1());
                        ps.setInt(9, familiar.getOption2());
                        ps.setInt(10, familiar.getOption3());
                        ps.setInt(11, familiar.isSummoned() ? 1 : 0);
                        ps.setInt(12, familiar.isLock() ? 1 : 0);
                        ps.execute();
                    }
                    ps.close();
                }
                /*
                 * 保存道具寶寶信息
                 */
                deleteWhereCharacterId(con, "DELETE FROM imps WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO imps (characterid, itemid, closeness, fullness, state, level) VALUES (?, ?, ?, ?, ?, ?)");
                ps.setInt(1, id);
                for (MapleImp imp : imps) {
                    if (imp != null) {
                        ps.setInt(2, imp.getItemId());
                        ps.setShort(3, imp.getCloseness());
                        ps.setShort(4, imp.getFullness());
                        ps.setByte(5, imp.getState());
                        ps.setByte(6, imp.getLevel());
                        ps.execute();
                    }
                }
                ps.close();
                /*
                 * 保存禮物信息
                 */
                if (changed_wishlist) {
                    deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?");
                    for (int i = 0; i < getWishlistSize(); i++) {
                        ps = con.prepareStatement("INSERT INTO wishlist(characterid, sn) VALUES(?, ?) ");
                        ps.setInt(1, getId());
                        ps.setInt(2, wishlist[i]);
                        ps.execute();
                        ps.close();
                    }
                }
                /*
                 * 保存縮地石信息
                 */
                if (changed_trocklocations) {
                    /*
                     * rocks = new int[10];
                     * regrocks = new int[5];
                     * hyperrocks = new int[13];
                     */
                    deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
                    for (int regrock : regrocks) {
                        if (regrock != 999999999) {
                            ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 0)");
                            ps.setInt(1, getId());
                            ps.setInt(2, regrock);
                            ps.execute();
                            ps.close();
                        }
                    }
                    for (int rock : rocks) {
                        if (rock != 999999999) {
                            ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 1)");
                            ps.setInt(1, getId());
                            ps.setInt(2, rock);
                            ps.execute();
                            ps.close();
                        }
                    }
                    for (int hyperrock : hyperrocks) {
                        if (hyperrock != 999999999) {
                            ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 2)");
                            ps.setInt(1, getId());
                            ps.setInt(2, hyperrock);
                            ps.execute();
                            ps.close();
                        }
                    }
                }
                /*
                 * 保存自定義里程信息
                 */
                deleteWhereCharacterId(con, "DELETE FROM character_credit WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO character_credit(characterid, name, value) VALUES(?, ?, ?)");
                for (Entry<String, Integer> i : credit.entrySet()) {
                    ps.setInt(1, getId());
                    ps.setString(2, i.getKey());
                    ps.setInt(3, i.getValue());
                    ps.execute();
                }
                ps.close();
                /*
                  保存效果開關
                 */
                deleteWhereCharacterId(con, "DELETE FROM effectswitch WHERE `characterid` = ?");
                ps = con.prepareStatement("INSERT INTO effectswitch (characterid, pos) VALUES (?, ?)");
                for (Integer effect : effectSwitch) {
                    ps.setInt(1, getId());
                    ps.setInt(2, effect);
                    ps.execute();
                }
                ps.close();
                if (changed_vcores) {
                    deleteWhereCharacterId(con, "DELETE FROM `vcoreskill` WHERE `characterid` = ?");
                    for (VCoreSkillEntry vCoreSkillEntry : vCoreSkills.values()) {
                        if (vCoreSkillEntry.getSlot() > 0) {
                            ps = con.prepareStatement("INSERT INTO `vcoreskill` (`characterid`, `vcoreid`, `level`, `exp`, `skill1`, `skill2`, `skill3`, `slot`, `index`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            ps.setInt(1, getId());
                            ps.setInt(2, vCoreSkillEntry.getVcoreid());
                            ps.setInt(3, vCoreSkillEntry.getLevel());
                            ps.setInt(4, vCoreSkillEntry.getExp());
                            ps.setInt(5, vCoreSkillEntry.getSkill1());
                            ps.setInt(6, vCoreSkillEntry.getSkill2());
                            ps.setInt(7, vCoreSkillEntry.getSkill3());
                            ps.setInt(8, vCoreSkillEntry.getSlot());
                            ps.setInt(9, vCoreSkillEntry.getIndex());
                            ps.execute();
                            ps.close();
                        }
                    }
                    deleteWhereCharacterId(con, "DELETE FROM `vmatrixslot` WHERE `characters_id` = ?");
                    for (VMatrixSlot vMatrixSlot : vMatrixSlot.values()) {
                        ps = con.prepareStatement("INSERT INTO `vmatrixslot` (`characters_id`, `slot`, `extend`, `unlock`) VALUES (?, ?, ?, ?)");
                        ps.setInt(1, getId());
                        ps.setInt(2, vMatrixSlot.getSlot());
                        ps.setInt(3, vMatrixSlot.getExtend());
                        ps.setInt(4, vMatrixSlot.getUnlock());
                        ps.execute();
                        ps.close();
                    }
                }
                if (changed_buylimit) {
                    deleteWhereCharacterId(con, "DELETE FROM `characters_buylimit` WHERE characters_id = ?");
                    for (Entry<Integer, NpcShopBuyLimit> entry : buyLimit.entrySet()) {
                        int shopid = entry.getKey();
                        for (Entry<Integer, BuyLimitData> datas : entry.getValue().getData().entrySet()) {
                            ps = con.prepareStatement("INSERT INTO `characters_buylimit` (`characters_id`, `shop_id`, `itemid`, `count`, `data`) VALUES (?, ?, ?, ?, ?)");
                            ps.setInt(1, getId());
                            ps.setInt(2, shopid);
                            ps.setInt(3, datas.getKey());
                            ps.setInt(4, datas.getValue().getCount());
                            ps.setTimestamp(5, new Timestamp(datas.getValue().getDate()));
                            ps.execute();
                            ps.close();
                        }
                    }
                }
                if (changed_accountbuylimit) {
                    SqlTool.update(con, "DELETE FROM `accounts_buylimit` WHERE account_id = ? AND world = ?", accountid, world);
                    for (Entry<Integer, NpcShopBuyLimit> entry : accountBuyLimit.entrySet()) {
                        int shopid = entry.getKey();
                        for (Entry<Integer, BuyLimitData> datas : entry.getValue().getData().entrySet()) {
                            ps = con.prepareStatement("INSERT INTO `accounts_buylimit` (`account_id`, `world`, `shop_id`, `itemid`, `count`, `data`) VALUES (?, ?, ?, ?, ?, ?)");
                            ps.setInt(1, accountid);
                            ps.setInt(2, world);
                            ps.setInt(3, shopid);
                            ps.setInt(4, datas.getKey());
                            ps.setInt(6, datas.getValue().getCount());
                            ps.setTimestamp(6, new Timestamp(datas.getValue().getDate()));
                            ps.execute();
                            ps.close();
                        }
                    }
                }
                if (changed_soulcollection) {
                    deleteWhereCharacterId(con, "DELETE FROM `character_soulcollection` WHERE characters_id = ?");
                    for (Entry<Integer, Integer> entry : soulCollection.entrySet()) {
                        ps = con.prepareStatement("INSERT INTO `character_soulcollection` (`characters_id`, `page`, `setsoul`) VALUES (?, ?, ?)");
                        ps.setInt(1, getId());
                        ps.setInt(2, entry.getKey());
                        ps.setInt(3, entry.getValue());
                        ps.execute();
                        ps.close();
                    }
                }
                if (changed_mobcollection) {
                    SqlTool.update(con, "DELETE FROM `accounts_mobcollection` WHERE `accounts_id` = ? AND `world` = ?", accountid, world);
                    for (Entry<Integer, String> entry : mobCollection.entrySet()) {
                        SqlTool.update(con, "INSERT INTO `accounts_mobcollection` (`accounts_id`, `world`, `recordid`, `data`) VALUES (?, ?, ?, ?)", accountid, world, entry.getKey(), entry.getValue());
                    }
                }
                changed_wishlist = false;
                changed_trocklocations = false;
                changed_skillmacros = false;
                changed_savedlocations = false;
                changed_questinfo = false;
                changed_worldshareinfo = false;
                changed_skills = false;
                changed_reports = false;
                changed_keyValue = false;
                changed_vcores = false;
                changed_mapleUnion = false;
                changed_buylimit = false;
                changed_accountbuylimit = false;
                changed_soulcollection = false;
                changed_mobcollection = false;
                changed_familiars = false;
                con.commit();
            }
        } catch (SQLException e) {
            log.error("[charsave] 儲存角色數據出現錯誤 .", e);
            try {
                con.rollback();
            } catch (SQLException ex) {
                log.error("[charsave] Error Rolling Back", e);
            }
        } finally {
            isSaveing = false;
            try {
                if (ps != null) {
                    ps.close();
                }
                if (pse != null) {
                    pse.close();
                }
                if (rs != null) {
                    rs.close();
                }
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                con.close();
            } catch (SQLException e) {
                log.error("[charsave] Error going back to autocommit mode", e);
            }
        }
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        deleteWhereCharacterId(con, sql, id);
    }

    public final int[] getFriendShipPoints() {
        return friendshippoints;
    }

    public final void setFriendShipPoints(int joejoe, int hermoninny, int littledragon, int ika, int Wooden) {
        this.friendshippoints[0] = joejoe;
        this.friendshippoints[1] = hermoninny;
        this.friendshippoints[2] = littledragon;
        this.friendshippoints[3] = ika;
        this.friendshippoints[4] = Wooden;
    }

    public final int getFriendShipToAdd() {
        return friendshiptoadd;
    }

    public final void setFriendShipToAdd(int points) {
        this.friendshiptoadd = points;
    }

    public final void addFriendShipToAdd(int points) {
        this.friendshiptoadd += points;
    }

    /*
     * 獲得角色的屬性狀態
     */
    public PlayerStats getStat() {
        return stats;
    }

    /*
     * 獲得角色的特殊屬性狀態
     */
    public PlayerSpecialStats getSpecialStat() {
        return specialStats;
    }

    /*
     * 地圖時間
     */
    public void cancelMapTimeLimitTask() {
        if (mapTimeLimitTask != null) {
            mapTimeLimitTask.cancel(false);
            mapTimeLimitTask = null;
        }
    }

    public void startQuestTimeLimitTask(int quest, int time) {
        send(MaplePacketCreator.startQuestTimeLimit(quest, time));
        questTimeLimitTask = MapTimer.getInstance().register(() -> send(MaplePacketCreator.stopQuestTimeLimit(quest)), time);
    }

    public void startMapTimeLimitTask(int time, final MapleMap to) {
        if (time <= 0) { //jail
            time = 1;
        }
        cancelMapTimeLimitTask();
        to.broadcastMessage(FieldPacket.clock(ClockPacket.secondsClock(time)));
        to.broadcastMessage(FieldPacket.clock(ClockPacket.secondsClock(time)));
        final MapleMap ourMap = getMap();
        time *= 1000;
        mapTimeLimitTask = MapTimer.getInstance().register(() -> {
            if (ourMap.getId() == GameConstants.JAIL) {
                getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_TIME)).setCustomData(String.valueOf(System.currentTimeMillis()));
                getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_QUEST)).setCustomData("0"); //release them!
            }
            changeMap(to, to.getPortal(0));
        }, time, time);
    }

    public int getTouchedRune() {
        return touchedrune;
    }

    public void setTouchedRune(int type) {
        touchedrune = type;
    }

    public long getRuneTimeStamp() {
        return Long.parseLong(getKeyValue("LastTouchedRune"));
    }

    public void setRuneTimeStamp(long time) {
        setKeyValue("LastTouchedRune", String.valueOf(time));
    }

    /*
     * 新增角色特殊操作處理
     */
    public Map<String, String> getKeyValue_Map() {
        return keyValue;
    }

    public String getKeyValue(String key) {
        return keyValue.get(key);
    }

    public void setKeyValue(String key, String values) {
        if (values == null) {
            keyValue.remove(key);
        } else {
            keyValue.put(key, values);
        }
        changed_keyValue = true;
    }

    /*
     * 任務信息
     */
    public void updateInfoQuest(int questid, String data) {
        updateInfoQuest(questid, data, true);
    }

    public void updateInfoQuest(int questid, String data, boolean show) {
        if (data == null || data.isEmpty()) {
            questinfo.remove(questid);
        } else {
            questinfo.put(questid, data);
        }
        if (worldShareInfo.containsKey(questid) || GameConstants.isWorldShareQuest(questid)) {
            updateWorldShareInfo(questid, data, show);
            return;
        }
        changed_questinfo = true;
        if (show) {
            client.announce(MaplePacketCreator.updateInfoQuest(questid, data));
        }
    }

    public void removeInfoQuest(int questid) {
        if (questinfo.containsKey(questid)) {
            this.updateInfoQuest(questid, "");
            questinfo.remove(questid);
            changed_questinfo = true;
        }
    }

    public String getInfoQuest(int questid) {
        if (questinfo.containsKey(questid)) {
            return questinfo.get(questid);
        }
        return "";
    }

    public String getInfoQuestStatS(int id, String stat) {
        String info = getInfoQuest(id);
        if (info != null && info.length() > 0 && info.contains(stat)) {
            int startIndex = info.indexOf(stat) + stat.length() + 1;
            int until = info.indexOf(";", startIndex);
            return info.substring(startIndex, until != -1 ? until : info.length());
        }
        return "";
    }

    public int getInfoQuestStat(int id, String stat) {
        String statz = getInfoQuestStatS(id, stat);
        return (statz == null || "".equals(statz)) ? 0 : Integer.parseInt(statz);
    }

    public long getInfoQuestValueWithKey(int questId, String key) {
        final String questInfo = this.getInfoQuest(questId);
        if (questInfo == null) {
            return -1L;
        }
        final String[] split;
        final String[] data = split = questInfo.split(";");
        for (final String s : split) {
            if (s.startsWith(key + "=")) {
                final String newkey = s.replace(key + "=", "");
                final String newkey2 = newkey.replace(";", "");
                final long dd = Long.valueOf(newkey2);
                return dd;
            }
        }
        return -1L;
    }

    public PlayerObservable getPlayerObservable() {
        return playerObservable;
    }

    public void setInfoQuestStat(int id, String stat, int statData) {
        setInfoQuestStat(id, stat, String.valueOf(statData));
    }

    public void setInfoQuestStat(int id, String stat, String statData) {
        String info = getInfoQuest(id);
        if (info.length() == 0 || !info.contains(stat)) {
            updateInfoQuest(id, stat + "=" + statData + (info.length() == 0 ? "" : ";") + info);
        } else {
            String newInfo = stat + "=" + statData;
            String beforeStat = info.substring(0, info.indexOf(stat));
            int from = info.indexOf(";", info.indexOf(stat) + stat.length());
            String afterStat = from == -1 ? "" : info.substring(from + 1);
            updateInfoQuest(id, beforeStat + newInfo + (afterStat.length() != 0 ? (";" + afterStat) : ""));
        }
    }

    /*
     * 檢測任務ID中完成的信息中是否包含 某個字符
     */
    public boolean containsInfoQuest(int questid, String data) {
        return questinfo.containsKey(questid) && questinfo.get(questid).contains(data);
    }

    public int getNumQuest() {
        int i = 0;
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 2 && !(q.isCustom())) {
                i++;
            }
        }
        return i;
    }

    public byte getQuestStatus(int questId) {
        MapleQuest qq = MapleQuest.getInstance(questId);
        if (getQuestNoAdd(qq) == null) {
            return 0;
        }
        return getQuestNoAdd(qq).getStatus();
    }

    public MapleQuestStatus getQuest(int questId) {
        return getQuest(MapleQuest.getInstance(questId));
    }

    public MapleQuestStatus getQuest(MapleQuest quest) {
        if (!quests.containsKey(quest.getId())) {
            return new MapleQuestStatus(quest, (byte) 0);
        }
        return quests.get(quest.getId());
    }

    public boolean needQuestItem(int questId, int itemId) {
        if (questId <= 0) {
            return true;
        }
        MapleQuest quest = MapleQuest.getInstance(questId);
        return getInventory(ItemConstants.getInventoryType(itemId)).countById(itemId) < quest.getAmountofItems(itemId);
    }

    public void setQuestAdd(MapleQuest quest, byte status, String customData) {
        if (!quests.containsKey(quest.getId())) {
            MapleQuestStatus stat = new MapleQuestStatus(quest, status);
            stat.setCustomData(customData);
            quests.put(quest.getId(), stat);
        }
    }

    public MapleQuestStatus getQuestNAdd(MapleQuest quest) {
        if (!quests.containsKey(quest.getId())) {
            MapleQuestStatus status = new MapleQuestStatus(quest, (byte) 0);
            quests.put(quest.getId(), status);
            return status;
        }
        return quests.get(quest.getId());
    }

    public MapleQuestStatus getQuestNoAdd(MapleQuest quest) {
        return quests.get(quest.getId());
    }

    public MapleQuestStatus getQuestRemove(MapleQuest quest) {
        MapleQuestStatus result = quests.remove(quest.getId());
        if (result != null && result.isWorldShare()) {
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM queststatus WHERE account = ? AND world = ? AND quest = ?");
                ps.setInt(1, accountid);
                ps.setInt(2, world);
                ps.setInt(3, quest.getId());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException throwables) {
            }
        }
        return result;
    }

    /*
     * 更新任務信息
     */
    public void updateQuest(MapleQuestStatus quest) {
        updateQuest(quest, false);
    }

    public void updateQuest(MapleQuestStatus quest, boolean update) {
        if (quest.getStatus() == 0) {
            if (quests.containsKey(quest.getQuest().getId())) {
                getQuestRemove(quest.getQuest());
            }
        } else {
            quests.put(quest.getQuest().getId(), quest);
        }
        if (!quest.isCustom()) { //如果任務ID小於 99999 也就是說不是自定義任務
            client.announce(MaplePacketCreator.updateQuest(quest));
            if (quest.getStatus() == 1 && !update) {
                client.announce(MaplePacketCreator.updateQuestInfo(quest.getQuest().getId(), quest.getNpc(), 0, quest.getStatus() == 1));
            }
        }
    }

    public Map<Integer, String> getInfoQuest_Map() {
        return questinfo;
    }

    public Map<Integer, MapleQuestStatus> getQuest_Map() {
        return quests;
    }

    /*
     * 釣魚設置
     */
    public void startFishingTask() {
        if (FishingConfig.FISHING_ENABLE) {
            cancelFishingTask();
            lastFishingTime = System.currentTimeMillis();
            int fishingTime = isGm() ? FishingConfig.FISHING_TIME_GM : (stats.canFishVIP() ? FishingConfig.FISHING_TIME_VIP : FishingConfig.FISHING_TIME);
            dropMessage(-1, "開始釣魚，當前釣魚間隔時長為：" + (fishingTime / 1000) + "秒。");
            dropMessage(-11, "開始釣魚，當前釣魚間隔時長為：" + (fishingTime / 1000) + "秒。");
        }
    }

    public boolean canFish(long now) {
        if (!FishingConfig.FISHING_ENABLE) {
            return false;
        }
        int fishingTime = isGm() ? FishingConfig.FISHING_TIME_GM : (stats.canFishVIP() ? FishingConfig.FISHING_TIME_VIP : FishingConfig.FISHING_TIME);
        return lastFishingTime > 0 && lastFishingTime + fishingTime < now;
    }

    public void cancelFishingTask() {
        lastFishingTime = 0;
    }

    public Map<SecondaryStat, List<SecondaryStatValueHolder>> getEffects() {
        effectLock.lock();
        try {
            return effects;
        } finally {
            effectLock.unlock();
        }
    }

    // Effect - start
    public Map<SecondaryStat, List<SecondaryStatValueHolder>> getAllEffects() {
        effectLock.lock();
        try {
            return new LinkedHashMap<>(effects);
        } finally {
            effectLock.unlock();
        }
    }

    public final List<SecondaryStatValueHolder> getIndieBuffStatValueHolder(SecondaryStat stat) {
        effectLock.lock();
        try {
            List<SecondaryStatValueHolder> list = new ArrayList<>();
            if (effects.containsKey(stat)) {
                list.addAll(effects.get(stat));
            }
            return list;
        } finally {
            effectLock.unlock();
        }
    }

    public final List<SecondaryStatValueHolder> getChrBuffStatValueHolder(int cid) {
        effectLock.lock();
        try {
            return effects.values().stream().flatMap(Collection::stream).filter(mbsvh -> mbsvh.fromChrID == cid).collect(Collectors.toList());
        } finally {
            effectLock.unlock();
        }
    }

    public SecondaryStatValueHolder getBuffStatValueHolder(SecondaryStat stat) {
        return getBuffStatValueHolder(stat, -1);
    }

    public SecondaryStatValueHolder getBuffStatValueHolder(int skillID) {
        effectLock.lock();
        try {
            return effects.values().stream().flatMap(Collection::stream).filter(mbsvh -> mbsvh.effect.getSourceId() == skillID).findFirst().orElse(null);
        } finally {
            effectLock.unlock();
        }
    }

    public SecondaryStatValueHolder getBuffStatValueHolder(SecondaryStat stat, int skillID) {
        effectLock.lock();
        try {
            return effects.get(stat) == null ? null : effects.get(stat).stream().filter(mbsvh -> (mbsvh).effect.getSourceId() == skillID || skillID == -1).findFirst().orElse(null);
        } finally {
            effectLock.unlock();
        }
    }

    public final void setBuffStatValue(final SecondaryStat stat, final int skillID, final int value) {
        effectLock.lock();
        try {
            if (effects.get(stat) != null) {
                for (SecondaryStatValueHolder mbsvh : effects.get(stat)) {
                    if (mbsvh.effect.getSourceId() == skillID) {
                        mbsvh.value = value;
                        break;
                    }
                }
            }
        } finally {
            effectLock.unlock();
        }
    }

    public MapleStatEffect getEffectForBuffStat(SecondaryStat stat) {
        SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect;
    }

    public void giveDebuff(final Map<SecondaryStat, Pair<Integer, Integer>> disease, final MobSkill skill) {
        final Map<Map<SecondaryStat, Pair<Integer, Integer>>, Pair<Integer, Integer>> diseases = new HashMap<>();
        diseases.put(disease, new Pair<>(skill.getX(), skill.getDuration()));
        this.giveDebuff(disease, skill);

    }

    public MapleStatEffect getSkillEffect(int skillID) {
        MapleStatEffect effect = null;
        Skill skill = SkillFactory.getSkill(skillID);
        if (skill != null) {
            effect = skill.getEffect(getSkillLevel(SkillConstants.getLinkedAttackSkill(skillID)));
        }
        return effect;
    }

    public int getBuffedIntValue(SecondaryStat stat) {
        Integer value = getBuffedValue(stat);
        return value == null ? 0 : value;
    }

    public void giveBlackMageBuff() {
        this.getMap().broadcastMessage(CField.getSelectPower(5, 39));
        this.setSkillCustomInfo(80002625, 1L, 0L);
        SkillFactory.getSkill(80002625).getEffect(1).applyTo(this);
    }

    public final int getBuffedIntX(final SecondaryStat stat) {
        Integer value = getBuffedX(stat);
        return value == null ? 0 : value;
    }

    public int getBuffedIntZ(SecondaryStat stat) {
        Integer value = getBuffedZ(stat);
        return value == null ? 0 : value;
    }

    public final Integer getBuffedX(final SecondaryStat stat) {
        effectLock.lock();
        try {
            int n = 0;
            boolean find = false;
            if (effects.containsKey(stat)) {
                for (SecondaryStatValueHolder mbsvh : effects.get(stat)) {
                    find = true;
                    n += mbsvh.x;
                    if (!stat.canStack()) {
                        break;
                    }
                }
            }
            return find ? n : null;
        } finally {
            effectLock.unlock();
        }
    }

    public final Integer getBuffedZ(final SecondaryStat stat) {
        effectLock.lock();
        try {
            int n = 0;
            boolean find = false;
            if (effects.containsKey(stat)) {
                for (SecondaryStatValueHolder mbsvh : effects.get(stat)) {
                    find = true;
                    n += mbsvh.z;
                    if (!stat.canStack()) {
                        break;
                    }
                }
            }
            return find ? n : null;
        } finally {
            effectLock.unlock();
        }
    }

    public final Integer getBuffedValue(final SecondaryStat stat, final int skillID) {
        effectLock.lock();
        try {
            return effects.containsKey(stat) ? effects.get(stat).stream().filter(mbsvh -> mbsvh.effect.getSourceId() == skillID).findFirst().map(mbsvh -> mbsvh.value).orElse(null) : null;
        } finally {
            effectLock.unlock();
        }
    }

    public long getSkillCustomValue0(final int skillid) {
        if (this.customInfo.containsKey(skillid)) {
            return this.customInfo.get(skillid).getValue();
        }
        return 0L;
    }

    public final Integer getBuffedValue(final SecondaryStat stat) {
        effectLock.lock();
        try {
            int n = 0;
            boolean find = false;
            if (effects.containsKey(stat)) {
                for (SecondaryStatValueHolder mbsvh : effects.get(stat)) {
                    find = true;
                    n += mbsvh.value;
                    if (!stat.canStack()) {
                        break;
                    }
                }
            }
            return find ? n : null;
        } finally {
            effectLock.unlock();
        }
    }

    public int getBuffSource(SecondaryStat stat) {
        SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return 0;
        }
        return mbsvh.effect.getSourceId();
    }

    public List<SecondaryStat> getBuffStats(MapleStatEffect effect, long startTime) {
        effectLock.lock();
        try {
            List<SecondaryStat> list = new ArrayList<>();
            for (Entry<SecondaryStat, List<SecondaryStatValueHolder>> entry : effects.entrySet()) {
                for (SecondaryStatValueHolder mbsvh : entry.getValue()) {
                    if (mbsvh.effect != null && mbsvh.effect.sameSource(effect) && (startTime == -1L || startTime == mbsvh.startTime)) {
                        list.add(entry.getKey());
                    }
                }
            }
            return list;
        } finally {
            effectLock.unlock();
        }
    }

    public final void removeEffect(final SecondaryStat stat, final int skillID, final long startTime) {
        effectLock.lock();
        try {
            if (effects.get(stat) != null) {
                effects.get(stat).removeIf(mbsvh -> mbsvh.effect.getSourceId() == skillID && (mbsvh.startTime == startTime || startTime == -1L));
            }
        } finally {
            effectLock.unlock();
        }
    }

    public final void removeAllEffect() {
        effectLock.lock();
        try {
            World.TemporaryStat.SaveData(this);
            final Iterator<Entry<SecondaryStat, List<SecondaryStatValueHolder>>> iterator = effects.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<SecondaryStat, List<SecondaryStatValueHolder>> entry = iterator.next();
                final Iterator<SecondaryStatValueHolder> iterator2 = entry.getValue().iterator();
                while (iterator2.hasNext()) {
                    SecondaryStatValueHolder mbsvh = iterator2.next();
                    if (!World.TemporaryStat.IsSaveStat(entry.getKey(), mbsvh)) {
                        mbsvh.cancel();
                    }
                    iterator2.remove();
                }
                iterator.remove();
            }
            setSoulMP(0);
        } finally {
            effectLock.unlock();
        }
    }

    public final void removeDebuffs() {
        List<Integer> list = new ArrayList<>();
        effectLock.lock();
        try {
            for (List<SecondaryStatValueHolder> holders : effects.values()) {
                for (SecondaryStatValueHolder mbsvh : holders) {
                    if (mbsvh.effect instanceof MobSkill && !mbsvh.effect.isNotRemoved()) {
                        list.add(mbsvh.effect.getSourceId());
                    }
                }
            }
        } finally {
            effectLock.unlock();
        }
        list.forEach(this::dispelEffect);
    }

    public final void removeBuffs(final boolean normal) {
        List<Integer> list = new ArrayList<>();
        List<Integer> notRemove = new ArrayList<>();
        effectLock.lock();
        try {
            for (Entry<SecondaryStat, List<SecondaryStatValueHolder>> entry : effects.entrySet()) {
                for (SecondaryStatValueHolder mbsvh : entry.getValue()) {
                    if (mbsvh.effect != null
                            && (World.TemporaryStat.IsNotRemoveSaveStat(entry.getKey(), mbsvh)
                            || notRemove.contains(mbsvh.effect.getSourceId())
                            || mbsvh.effect.getSourceId() == 黑騎士.轉生)) {
                        notRemove.add(mbsvh.effect.getSourceId());
                        continue;
                    }
                    if (!(mbsvh.effect instanceof MobSkill) && (!normal || !mbsvh.effect.isNotRemoved())) {
                        list.add(mbsvh.effect.getSourceId());
                    }
                }
            }
            setSoulMP(0);
        } finally {
            effectLock.unlock();
        }
        list.forEach(this::dispelEffect);
    }

    public final void dispelEffect(final int skillID) {
        final SecondaryStatValueHolder buffStatValueHolder;
        if ((buffStatValueHolder = this.getBuffStatValueHolder(skillID)) != null) {
            this.cancelEffect(buffStatValueHolder.effect, buffStatValueHolder.startTime);
        }
    }

    public final void dispelEffect(final SecondaryStat stat) {
        final SecondaryStatValueHolder buffStatValueHolder;
        if ((buffStatValueHolder = this.getBuffStatValueHolder(stat)) != null) {
            this.cancelEffect(buffStatValueHolder.effect, buffStatValueHolder.startTime);
        }
    }

    public void dispelEffect(int skillID, SecondaryStat stat) {
        final SecondaryStatValueHolder buffStatValueHolder;
        if ((buffStatValueHolder = this.getBuffStatValueHolder(stat, skillID)) != null) {
            this.cancelEffect(buffStatValueHolder.effect, true, buffStatValueHolder.startTime, Collections.singletonMap(stat, buffStatValueHolder.value));
        }
    }

    public void cancelEffect(MapleStatEffect effect, long startTime) {
        cancelEffect(effect, false, startTime, effect.getStatups());
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        cancelEffect(effect, overwrite, startTime, effect.getStatups());
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime, Map<SecondaryStat, Integer> statups) {
        if (effect == null) {
            return;
        }
        List<SecondaryStat> buffStats;
        if (!overwrite) { //不是重新使用BUFF狀態
            buffStats = getBuffStats(effect, startTime);
            if (buffStats.contains(SecondaryStat.ElementalCharge) && getBuffStatValueHolder(SecondaryStat.BlessedHammer) != null) {
                buffStats.add(SecondaryStat.BlessedHammer);
                buffStats.add(SecondaryStat.BlessedHammerActive);
            }
        } else {
            buffStats = new ArrayList<>(statups.size());
            buffStats.addAll(statups.keySet());
        }
        if (buffStats.isEmpty()) {
            return;
        }
        if (!overwrite && effect.getSourceId() == 拳霸.海龍螺旋) {
            SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(SecondaryStat.SerpentScrew);
            if (mbsvh != null && mbsvh.value >= 10) {
                reduceSkillCooldown(拳霸.海龍螺旋, mbsvh.value / effect.getU() * effect.getX() * 1000);
            }
        }

        // 秒殺DEBUFF結束
        boolean ow = overwrite;
        Arrays.stream(
                new SecondaryStat[]{
                        SecondaryStat.Lapidification,
                        SecondaryStat.Attract
                }).forEach(s -> {
            if (!ow && buffStats.contains(s)) {
                SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(s);
                if (mbsvh == null || mbsvh.getLeftTime() <= 0) {
                    addHPMP(-100, 0);
                }
            }
        });

        //取消註冊的BUFF信息
        deregisterBuffStats(buffStats, effect, overwrite, startTime);
        if (effect.getSourceId() == 幻影俠盜.幻影斗蓬) {
            SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(SecondaryStat.Invisible);
            registerSkillCooldown(幻影俠盜.幻影斗蓬, getSkillEffect(幻影俠盜.幻影斗蓬).getCooldown(this), true);
            if (getTempValues().get("skill" + 幻影俠盜.幻影斗蓬) != null) {
                getTempValues().put("skill" + 幻影俠盜.幻影斗蓬, 0);
            }
        }
        if (!overwrite && effect.getSourceId() == 80011248) { // 黎明神盾結束 意志關懷
            SkillFactory.getSkill(80011249).getEffect(1).applyTo(this);
            send(EffectPacket.showBuffEffect(this, false, 80011249, 1, 0, null));
        }
        if (buffStats.contains(SecondaryStat.Larkness) && startTime > 0 && larknessDiraction != 3) {
            if (larkness == 0) {
                getSkillEffect(夜光.暗蝕).unprimaryPassiveApplyTo(this);
            } else if (larkness == 10000) {
                getSkillEffect(夜光.光蝕).unprimaryPassiveApplyTo(this);
            }
        }
        if (JobConstants.is劍豪(job) && buffStats.contains(SecondaryStat.BladeStanceMode)) {
            applyHayatoStance(0);
        }
        AbstractSkillHandler sh = SkillClassFetcher.getHandlerBySkill(effect.getSourceId());
        if (sh == null) {
            sh = SkillClassFetcher.getHandlerByJob(getJobWithSub());
        }
        if (sh != null) {
            SkillClassApplier applier = new SkillClassApplier();
            applier.effect = effect;
            applier.overwrite = overwrite;
            applier.localstatups = statups;
            int result = sh.onAfterCancelEffect(this, applier);
            if (result == 0) {
            } else if (result == 1) {
                effect = applier.effect;
                overwrite = applier.overwrite;
                statups = applier.localstatups;
            }
        }
    }

    /**
     * 取消角色註冊的BUFF狀態信息
     */
    private void deregisterBuffStats(List<SecondaryStat> stats, MapleStatEffect effect, boolean overwrite, long startTime) {
        List<MapleSummon> summonList = new ArrayList<>();
        effectLock.lock();
        try {
            for (SecondaryStat stat : stats) {
                if (stat == SecondaryStat.AMPlanting) {
                    continue;
                }
                if (effects.containsKey(stat)) {
                    List<SecondaryStatValueHolder> holderList = effects.get(stat);
                    Iterator<SecondaryStatValueHolder> holderIterator = holderList.iterator();
                    while (holderIterator.hasNext()) {
                        final SecondaryStatValueHolder mbsvh = holderIterator.next();
                        if (mbsvh.effect.sameSource(effect) && (mbsvh.startTime == startTime || startTime == -1L) || !mbsvh.effect.sameSource(effect) && !stat.canStack()) {
                            if (!overwrite && stat == SecondaryStat.RevenantGauge) {
                                tempValues.put("亡靈怒氣", mbsvh.z);
                            }
                            if (!overwrite && mbsvh.effect.getSourceId() == 80011248) { // 黎明神盾結束 意志關懷
                                mbsvh.value = 0;
                                mbsvh.localDuration = 0;
                                send(BuffPacket.giveBuff(this, effect, Collections.singletonMap(SecondaryStat.DawnShield_ExHP, effect.getSourceId())));
                            }
                            boolean remove = true;
                            if (effect.is時空門()) {
                                this.removeAllTownPortal();
                            } else if (stat == SecondaryStat.IndieBuffIcon) {
                                this.summonsLock.lock();
                                try {
                                    long timeNow = System.currentTimeMillis();
                                    final Iterator<MapleSummon> summonIterator = this.summons.iterator();
                                    while (summonIterator.hasNext()) {
                                        final MapleSummon summon = summonIterator.next();
                                        int skillID = mbsvh.effect.getSourceId();
                                        if ((summon.getSkillId() != skillID && summon.getParentSummon() != skillID) || (summon.getCreateTime() != startTime && startTime != -1L)) {
                                            continue;
                                        }
                                        //remove = remove || summon.getSkillId() == 烈焰巫師.火焰之魂_獅子 || summon.getSkillId() == 烈焰巫師.火焰之魂_狐狸 || summon.getSkillId() == skillID + 999 || (skillID == 1085 || skillID == 1087 || skillID == 1090) && summon.getSkillId() == skillID - 999;
                                        if (!overwrite && summon.getSkillId() == 虎影.歪曲縮地符) {
                                            remove = false;
                                        }
                                        if (remove) {
                                            if (summon.getSummonMaxCount() != 1 && summon.getCreateTime() + summon.getDuration() > timeNow) {
                                                remove = false;
                                                continue;
                                            }
                                            if (this.isDebug()) {
                                                this.dropDebugMessage(1, "[Summon] Remove Summon Effect:" + summon.getEffect());
                                            }
                                            summonList.add(summon);
                                            summonIterator.remove();
                                        }
                                    }
                                } finally {
                                    this.summonsLock.unlock();
                                }
                            } else {
                                if (JobConstants.is陰陽師(job) && stat == SecondaryStat.ChangeFoxMan && this.getHaku() != null) {
                                    this.getHaku().setState(1);
                                    this.getHaku().update(this);
                                }
                            }
                            if (remove) {
                                mbsvh.cancel();
                                holderIterator.remove();
                                if (isDebug()) {
                                    dropDebugMessage(1, "[BUFF] Deregister:" + stat);
                                }
                            }
                        }
                    }
                    if (holderList.isEmpty() || effect.getSourceId() == 暗夜行者.Switch_type_Magic_暗影蝙蝠) {
                        effects.remove(stat);
                    }
                }
            }
            // <editor-fold defaultstate="collapsed" desc="Deleted Code">
            /*
            int effectSize = effects.size();
            ArrayList<Pair<MapleBuffStat, MapleBuffStatValueHolder>> effectsToRemove = new ArrayList<>();
            for (MapleBuffStat stat : stats) {
                getAllEffects().forEach(buffs -> {
                    if (buffs.getLeft() == stat && (effect == null || buffs.getRight().effect.sameSource(effect))) {
                        effectsToRemove.add(buffs);
                        MapleBuffStatValueHolder mbsvh = buffs.getRight();
                        if (stat == MapleBuffStat.IndieSummoned
                                || stat == MapleBuffStat.靈魂助力
                                || stat == MapleBuffStat.DAMAGE_BUFF
                                || stat == MapleBuffStat.地雷
                                || stat == MapleBuffStat.IndiePAD
                                || stat == MapleBuffStat.影子侍從
                                || stat == MapleBuffStat.召喚美洲豹
                                || stat == MapleBuffStat.黑暗幻影) {
                            int summonId = mbsvh.effect.getSourceid();
                            List<MapleSummon> toRemove = new ArrayList<>();
                            visibleMapObjectsLock.writeLock().lock();
                            summonsLock.lock();
                            try {
                                for (MapleSummon summon : summons) {
                                    if (summon.getSkillId() == summonId
                                            || ((summonId == 86 || summonId == 88 || summonId == 91) && summon.getSkillId() == summonId + 999)
                                            || ((summonId == 1085 || summonId == 1087 || summonId == 1090) && summon.getSkillId() == summonId - 999)
                                            || SkillConstants.is美洲豹(summon.getSkillId())
                                            || (summonId == 雙弩.精靈騎士 || summonId == 雙弩.精靈騎士1 || summonId == 雙弩.精靈騎士2)
                                            || (summon.getSkillId() == 夜行者.影子侍從 || summon.getSkillId() == 夜行者.黑暗幻影_影子20 || summon.getSkillId() == 夜行者.黑暗幻影_影子40)) {
                                        map.broadcastMessage(SummonPacket.removeSummon(summon, overwrite));
                                        map.removeMapObject(summon);
                                        visibleMapObjects.remove(summon);
                                        toRemove.add(summon);
                                    }
                                }
                                summons.removeAll(toRemove);
                            } finally {
                                summonsLock.unlock();
                                visibleMapObjectsLock.writeLock().unlock();
                            }
                            if (summonId == 神射手.火鳳凰 || summonId == 箭神.冰鳳凰) {
                                cancelEffectFromBuffStat(MapleBuffStat.精神連接);
                            }
                        } else if (stat == MapleBuffStat.龍之力) {
                            lastDragonBloodTime = 0;
                        } else if (stat == MapleBuffStat.恢復效果 || mbsvh.effect.getSourceid() == 機械師.金屬機甲_戰車) {
                            lastRecoveryTime = 0;
                        } else if (stat == MapleBuffStat.導航輔助 || stat == MapleBuffStat.神秘瞄準術) {
                            linkMobs.clear();
                        } else if (stat == MapleBuffStat.惡魔恢復) {
                            lastRecoveryTimeEM = 0;
                        } else if (stat == MapleBuffStat.避柳) {
                            this.setBuffedValue(stat, 0);
                        }
                    }
                });
            }
            int toRemoveSize = effectsToRemove.size();
            for (Pair<MapleBuffStat, MapleBuffStatValueHolder> toRemove : effectsToRemove) {
                if (effects.contains(toRemove)) {
                    if (toRemove.getRight().schedule != null) {
                        toRemove.getRight().schedule.cancel(false);
                        toRemove.getRight().schedule = null;
                    }
                    effects.remove(toRemove);
                }
            }
            effectsToRemove.clear();
            boolean ok = (effectSize - effects.size() == toRemoveSize);
            if (isShowPacket()) {
                dropDebugMessage(1, "[BUFF信息] 取消BUFF 以前BUFF總數: " + effectSize + " 現在BUFF總數 " + effects.size() + " 取消的BUFF數量: " + toRemoveSize + " 是否相同: " + ok);
            }
            if (!ok) {
                log.error("取消BUFF錯誤", this.getName() + " - " + this.getJob() + " 取消BUFF出現錯誤 技能ID: " + (effect != null ? effect.getSourceid() : "???"), true);
            }
             */
            //</editor-fold>
        } finally {
            effectLock.unlock();
        }
        if (this.map != null && !summonList.isEmpty()) {
            MapleStatEffect eff = getSkillEffect(陰陽師.鬼夜叉_大鬼封魂陣);
            summonList.forEach(summon -> {
                this.map.disappearMapObject(summon);
                if (eff != null && (summon.getSkillId() == 陰陽師.鬼夜叉_老么 || summon.getSkillId() == 陰陽師.鬼夜叉_二哥 || summon.getSkillId() == 陰陽師.鬼夜叉_大哥 || summon.getSkillId() == 陰陽師.鬼夜叉_老大)) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long time = currentTimeMillis - summon.getCreateTime();
                    SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(SecondaryStat.KannaFifthAttract);
                    if (mbsvh == null) {
                        registerEffect(Collections.singletonMap(SecondaryStat.KannaFifthAttract, new Pair(陰陽師.鬼夜叉_大鬼封魂陣, 0)), (int) time, getId(), currentTimeMillis, 0L, 2100000000, null);
                        mbsvh = getBuffStatValueHolder(SecondaryStat.KannaFifthAttract);
                    } else if (mbsvh.sourceID == 0 && mbsvh.z < eff.getU() * 1000) {
                        mbsvh.z += time;
                    } else {
                        return;
                    }
                    mbsvh.sourceID = 0;
                    send(BuffPacket.giveBuff(this, null, Collections.singletonMap(SecondaryStat.KannaFifthAttract, 陰陽師.鬼夜叉_大鬼封魂陣)));
                }
            });
        }
        cancelPlayerBuffs(stats, overwrite);
        if (effect.isSkill() && (effect.getSourceId() == 管理員.終極隱藏 || effect.getSourceId() == 9101004)) {
            map.broadcastBelowGmLvMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
            map.broadcastBelowGmLvMessage(this, EffectPacket.getEffectSwitch(getId(), getEffectSwitch()), false);
        }
    }

    /**
     * 發送取消角色的BUFF狀態封包
     */
    private void cancelPlayerBuffs(List<SecondaryStat> buffstats, boolean overwrite) {
        buffstats.removeIf(stat -> stat == SecondaryStat.SurplusSupply);
        if (overwrite) {
            List<SecondaryStat> buffStatX = new ArrayList<>();
            for (SecondaryStat stat : buffstats) {
                if (stat.canStack()) {
                    switch (stat) {
                        case RideVehicle:
                        case RideVehicleExpire:
                            continue;
                    }
                    buffStatX.add(stat);
                }
            }
            if (buffStatX.size() <= 0) {
                return; //無需發送任何封包 不作處理
            }
            buffstats = buffStatX;
        }
        try {
            stats.recalcLocalStatsInterrupt(false, this, -1);
        } catch (InterruptedException ignore) {

        }
        if (!buffstats.isEmpty() && client != null) {
            client.announce(BuffPacket.temporaryStatReset(buffstats, this));
            if (map != null) {
                map.broadcastMessage(this, BuffPacket.cancelForeignBuff(this, buffstats), false);
            }
            if (buffstats.contains(SecondaryStat.FifthAdvWarriorShield)) {
                send(MaplePacketCreator.userBonusAttackRequest(劍士.突擊之盾_1, 0, Collections.emptyList()));
            }
            if (buffstats.contains(SecondaryStat.RevenantGauge)) {
                getSkillEffect(惡魔復仇者.亡靈_1).applyTo(this);
            }
//            if (buffstats.contains(SecondaryStat.ShamanModeChangeHyperBuff)) {
//                CheckAnimalMode(this, getBuffedIntValue(SecondaryStat.ShamanMode));
//            }
            final MapleSummon summon = this.getSummonBySkillID(伊利恩.古代水晶);
            if (buffstats.contains(SecondaryStat.CrystalChargeBuffIcon) && summon != null) {
                summon.resetAncientCrystal();
                this.client.announce(SummonPacket.SummonedSkillState(summon, 2));
                if (this.map != null) {
                    this.map.broadcastMessage(this, SummonPacket.SummonedStateChange(summon, 2, summon.getAcState1(), summon.getAcState2()), true);
                    this.map.broadcastMessage(this, SummonPacket.SummonedSpecialEffect(summon, 2), true);
                }
            }
        }
    }

    public String getBattleGrondJobName() {
        return this.BattleGrondJobName;
    }

    public void setBattleGrondJobName(final String BattleGrondJobName) {
        this.BattleGrondJobName = BattleGrondJobName;
    }

    public final void registerEffect(final Map<SecondaryStat, Pair<Integer, Integer>> statups, final int z, final int chrID, final long startTime, final long startChargeTime, final int duration, final CancelEffectAction cancelAction) {
        effectLock.lock();
        try {
            boolean hided = false;
            for (final Entry<SecondaryStat, Pair<Integer, Integer>> entry : statups.entrySet()) {
                if (entry.getValue().getLeft() == 管理員.終極隱藏 || entry.getValue().getLeft() == 9101004) {
                    if (!hided) {
                        hided = !hided;
                        map.broadcastBelowGmLvMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
                    }
                }
                if (this.isDebug()) {
                    this.dropDebugMessage(0, "[BUFF] Register Stat:" + entry.getKey() + " value:" + entry.getValue());
                }
                effects.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(new SecondaryStatValueHolder(chrID, entry.getValue().getRight(), z, startTime, startChargeTime, duration, getSkillEffect(entry.getValue().getLeft()), cancelAction));
            }
        } finally {
            effectLock.unlock();
        }

        if (statups != null && statups.size() > 0) {
            stats.recalcLocalStats(false, this);
        }
    }

    public final void registerEffect(final MapleStatEffect effect, final Map<SecondaryStat, Integer> statups, final int z, final int from, final long startTime, final long startChargeTime, final int duration, final CancelEffectAction cancelAction) {
        if (effect.isSkill() && (effect.getSourceId() == 管理員.終極隱藏 || effect.getSourceId() == 9101004)) {
            map.broadcastBelowGmLvMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
        }
        effectLock.lock();
        try {
            for (final Entry<SecondaryStat, Integer> entry : statups.entrySet()) {
                final SecondaryStat stat = entry.getKey();
                if (this.isDebug()) {
                    this.dropDebugMessage(0, "[BUFF] Register Stat:" + stat + " value:" + entry.getValue());
                }
                effects.computeIfAbsent(stat, k -> new ArrayList<>()).add(new SecondaryStatValueHolder(from, entry.getValue(), z, startTime, startChargeTime, duration, effect, cancelAction));
            }
            if (this.isDebug() && !effect.getStatups().isEmpty()) {
                this.dropDebugMessage(1, "[BUFF] Register Effect: " + effect + " Duration:" + duration);
            }
        } finally {
            effectLock.unlock();
        }

        if (statups != null && statups.size() > 0) {
            stats.recalcLocalStats(false, this);
        }
    }

    public Integer getBuffedSkill_X(SecondaryStat stat) {
        SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect.getX();
    }

    public Integer getBuffedSkill_Y(SecondaryStat stat) {
        SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect.getY();
    }

    public boolean isBuffFrom(SecondaryStat stat, Skill skill) {
        SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        return mbsvh != null && mbsvh.effect != null && skill != null && mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }

    public boolean hasBuffSkill(int skillId) {
        return getBuffStatValueHolder(skillId) != null;
    }

    public void setBuffStatValue(SecondaryStat stat, int value) {
        SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public void setSchedule(SecondaryStat stat, ScheduledFuture<?> sched) {
        SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return;
        }
        mbsvh.schedule.cancel(false);
        mbsvh.schedule = sched;
    }

    public Long getBuffedStartTime(SecondaryStat stat) {
        SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(stat);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.startTime;
    }

    public void dispel() {
        if (!isHidden()) {
            List<SecondaryStatValueHolder> allBuffs = new LinkedList<>();
            getAllEffects().values().forEach(allBuffs::addAll);
            for (SecondaryStatValueHolder mbsvh : allBuffs) {
                if (mbsvh.effect.isSkill() && mbsvh.schedule != null && !mbsvh.effect.isMorph() && !mbsvh.effect.isGmBuff() && !mbsvh.effect.is騎乘技能() && !mbsvh.effect.is矛之鬥氣() && !mbsvh.effect.is蓄能系統() && !mbsvh.effect.isNotRemoved()) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }
            allBuffs.clear();
        }
    }

    public void dispelSkill(int skillId) {
        List<SecondaryStatValueHolder> allBuffs = new LinkedList<>();
        getAllEffects().values().forEach(allBuffs::addAll);
        for (SecondaryStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillId) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
        allBuffs.clear();
    }

    /*
     * 清除職業ID所有的BUFF狀態
     */
    public void dispelBuffByJobId(int jobId) {
        List<SecondaryStatValueHolder> allBuffs = new LinkedList<>();
        getAllEffects().values().forEach(allBuffs::addAll);
        for (SecondaryStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() / 10000 == jobId) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
        allBuffs.clear();
    }

    /*
     * 清除所有召喚獸
     */
    public void dispelSummons() {
        List<SecondaryStatValueHolder> allBuffs = new LinkedList<>();
        getAllEffects().values().forEach(allBuffs::addAll);
        for (SecondaryStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.getSummonMovementType() != null) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
        allBuffs.clear();
    }

    /*
     * 清除指定技能ID的BUFF狀態
     */
    public void dispelBuff(int buffId) {
        List<SecondaryStatValueHolder> allBuffs = new LinkedList<>();
        getAllEffects().values().forEach(allBuffs::addAll);
        for (SecondaryStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.getSourceId() == buffId) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
        allBuffs.clear();
    }

    public void cancelMorphs() {
        List<SecondaryStatValueHolder> allBuffs = new LinkedList<>();
        getAllEffects().values().forEach(allBuffs::addAll);
        for (SecondaryStatValueHolder mbsvh : allBuffs) {
            switch (mbsvh.effect.getSourceId()) {
                case 凱撒.終極型態:
                case 凱撒.進階終極形態:
                case 凱撒.超_終極型態:
                    return; // Since we can't have more than 1, save up on loops
                default:
                    if (mbsvh.effect.isMorph()) {
                        cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                    }
            }
        }
        allBuffs.clear();
    }

    /*
     * 獲取變身BUFF狀態的技能ID
     */
    public int getMorphState() {
        List<SecondaryStatValueHolder> allBuffs = new LinkedList<>();
        getAllEffects().values().forEach(allBuffs::addAll);
        for (SecondaryStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMorph()) {
                return mbsvh.effect.getSourceId();
            }
        }
        allBuffs.clear();
        return -1;
    }

    /*
     * 切換頻道或者進入商城出來後 給角色BUFF 不需要發送封包
     */
    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        if (buffs == null) {
            return;
        }
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime, mbsvh.localDuration, mbsvh.statup, mbsvh.fromChrId);
        }
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List<PlayerBuffValueHolder> ret = new ArrayList<>();
        Map<Pair<Integer, Byte>, Integer> alreadyDone = new HashMap<>(); //已經完成的
        getAllEffects().forEach((k, v) -> v.forEach(mbsvh -> {
            mbsvh.cancel();
            Pair<Integer, Byte> key = new Pair<>(mbsvh.effect.getSourceId(), (byte) mbsvh.effect.getLevel());
            if (alreadyDone.containsKey(key)) {
                ret.get(alreadyDone.get(key)).statup.put(k, mbsvh.value);
            } else {
                alreadyDone.put(key, ret.size());
                Map<SecondaryStat, Integer> map = new EnumMap<>(SecondaryStat.class);
                map.put(k, mbsvh.value);
                ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect, map, mbsvh.localDuration, mbsvh.fromChrID));
            }
        }));
        return ret;
    }

    /*
     * 尖兵能量恢復
     */
    public int getPowerCountByJob() {
        switch (getJob()) {
            case 3610:
                return 10;
            case 3611:
                return 15;
            case 3612:
                return 20;
        }
        return 5;
    }

    public void silentEnforceMaxHpMp() {
        stats.setMp(stats.getMp());
        stats.setHp(stats.getHp(), true, this);
    }

    public void enforceMaxHpMp() {
        Map<MapleStat, Long> statup = new EnumMap<>(MapleStat.class);
        if (stats.getMp() > stats.getCurrentMaxMP()) {
            stats.setMp(stats.getMp());
            statup.put(MapleStat.MP, (long) stats.getMp());
        }
        if (stats.getHp() > stats.getCurrentMaxHP()) {
            stats.setHp(stats.getHp(), this);
            statup.put(MapleStat.HP, (long) stats.getHp());
        }
        if (statup.size() > 0) {
            client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        }
    }

    public MapleMap getMap() {
        return map;
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public byte getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBlessOfFairyOrigin() {
        return this.BlessOfFairy_Origin;
    }

    public String getBlessOfEmpressOrigin() {
        return this.BlessOfEmpress_Origin;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int newLevel) {
        if (newLevel <= 0) {
            newLevel = 1;
        } else if (newLevel >= GameConstants.MAX_LEVEL) {
            newLevel = GameConstants.MAX_LEVEL;
        }
        this.level = newLevel;
    }

    public int getFame() {
        return fame;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public int getFallCounter() {
        return fallcounter;
    }

    public void setFallCounter(int fallcounter) {
        this.fallcounter = fallcounter;
    }

    public int getCriticalGrowth() {
        return criticalgrowth;
    }

    public void setCriticalGrowth(int critical) {
        this.criticalgrowth = critical;
    }

    public MapleClient getClient() {
        return client;
    }

    public void setClient(MapleClient client) {
        this.client = client;
    }

    public long getExp() {
        return exp.get();
    }

    public void setExp(long exp) {
        this.exp.set(exp);
    }

    public short getRemainingAp() {
        return remainingAp;
    }

    public void setRemainingAp(short remainingAp) {
        this.remainingAp = remainingAp;
    }

    public int getRemainingSp() {
        return remainingSp[JobConstants.getSkillBookByJob(job)];
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp[JobConstants.getSkillBookByJob(job)] = remainingSp; //default
    }

    public int getRemainingSp(int skillbook) {
        return remainingSp[skillbook];
    }

    public int[] getRemainingSps() {
        return remainingSp;
    }

    public int getRemainingSpSize() {
        int ret = 0;
        for (int aRemainingSp : remainingSp) {
            if (aRemainingSp > 0) {
                ret++;
            }
        }
        return ret;
    }

    public short getHpApUsed() {
        return hpApUsed;
    }

    public void setHpApUsed(short hpApUsed) {
        this.hpApUsed = hpApUsed;
        stats.recalcLocalStats(this);
    }

    public short getMpApUsed() {
        return mpApUsed;
    }

    public void setMpApUsed(short mpApUsed) {
        this.mpApUsed = mpApUsed;
        stats.recalcLocalStats(this);
    }

    public void useHpAp(int amount) {
        if (amount == 0) {
            return;
        }
        setHpApUsed((short) (hpApUsed + amount));
        Map<MapleStat, Long> statup = new EnumMap<>(MapleStat.class);
        statup.put(MapleStat.MAXHP, (long) stats.getMaxHp());
        client.announce(MaplePacketCreator.updatePlayerStats(statup, true, this));
    }

    public void useMpAp(int amount) {
        if (amount == 0) {
            return;
        }
        setMpApUsed((short) (mpApUsed + amount));
        Map<MapleStat, Long> statup = new EnumMap<>(MapleStat.class);
        statup.put(MapleStat.MAXMP, (long) stats.getMaxMp());
        client.announce(MaplePacketCreator.updatePlayerStats(statup, true, this));
    }

    /*
     * 是否隱身狀態
     */
    public boolean isHidden() {
        return getBuffSource(SecondaryStat.DarkSight) / 1000000 == 9;
    }

    public byte getSkinColor() {
        return skinColor;
    }

    public void setSkinColor(byte skinColor) {
        this.skinColor = skinColor;
    }

    public short getJob() {
        return job;
    }

    public void setJob(int jobId) {
        this.job = (short) jobId;
    }

    public short getJobWithSub() {
        return (short) MapleJob.getIdWithSub(job, subcategory);
    }

    public void writeJobData(MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(getJob());
        mplew.writeShort(getSubcategory());
    }

    /*
     * 角色性別
     */
    public byte getGender() {
        return gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

//    public int getEventReviveCount() {
//        int nEventCount = eventInstance == null ? -1 : eventInstance.getReviveCount();
//        if (reviveCount < 0) {
//            if (nEventCount < 0) {
//                return -1;
//            } else {
//                return nEventCount;
//            }
//        } else {
//            if (nEventCount < 0) {
//                return reviveCount;
//            } else {
//                return reviveCount + nEventCount;
//            }
//        }
//    }

//    public int getReviveCount() {
//        return reviveCount;
//    }
//
//    public void setReviveCount(int reviveCount) {
//        this.reviveCount = reviveCount;
//    }
//
//    public void restReviveCount() {
//        this.reviveCount = -1;
//    }

    /*
     * 第2角色的性別
     */
    public byte getSecondGender() {
        //如果是神之子 那麼第2性別為1
        if (JobConstants.is神之子(job)) {
            return 1;
        }
        return gender;
    }

    public void zeroTag() {
        if (!JobConstants.is神之子(job)) {
            return;
        }
        this.addhpmpLock.lock();
        try {
            final int betaMaxHP = this.stats.getBetaMaxHP();
            final int hp = this.stats.getHp();
            setKeyValue("Zero_Look", isBeta() ? "0" : "1");
            this.stats.setBetaMaxHP(hp);
            this.stats.setHp(betaMaxHP);
        } finally {
            this.addhpmpLock.unlock();
        }
        this.updateHPMP(false);
        this.modifiedAvatar();
        client.announce(MaplePacketCreator.zeroInfo(this, 0xffff, isBeta()));
    }

    public boolean isBeta() {
        return "1".equals(getKeyValue("Zero_Look"));
    }

    /*
     * 角色髮型
     */
    public int getHair() {
        return hair;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    /*
     * 第2角色的髮型
     * 37623 神之子
     */
    public int getSecondHair() {
        if (JobConstants.is神之子(job)) {
            if (getKeyValue("Second_Hair") == null) {
                setKeyValue("Second_Hair", "37623");
            }
            return Integer.parseInt(getKeyValue("Second_Hair"));
        } else if (JobConstants.is天使破壞者(job)) {
            if (getKeyValue("Second_Hair") == null) {
                setKeyValue("Second_Hair", "37141");
            }
            return Integer.parseInt(getKeyValue("Second_Hair"));
        }
        return hair;
    }

    public void setSecondHair(int hair) {
        setKeyValue("Second_Hair", String.valueOf(hair));
    }

    public byte getSecondSkinColor() {
        if (JobConstants.is神之子(job) || JobConstants.is天使破壞者(job)) {
            if (this.getKeyValue("Second_Skin") == null) {
                this.setKeyValue("Second_Skin", "0");
            }
            return Byte.parseByte(this.getKeyValue("Second_Skin"));
        }
        return skinColor;
    }

    public void setSecondSkinColor(final byte b) {
        setKeyValue("Second_Skin", String.valueOf(b));
    }

    /*
     * 角色臉型
     */
    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    /*
     * 第2角色的臉型
     * 21290 神之子
     */
    public int getSecondFace() {
        if (JobConstants.is神之子(job)) {
            if (getKeyValue("Second_Face") == null) {
                setKeyValue("Second_Face", "21290");
            }
            return Integer.parseInt(getKeyValue("Second_Face"));
        } else if (JobConstants.is天使破壞者(job)) {
            if (getKeyValue("Second_Face") == null) {
                setKeyValue("Second_Face", "21173");
            }
            return Integer.parseInt(getKeyValue("Second_Face"));
        }
        return face;
    }

    public void setSecondFace(int face) {
        setKeyValue("Second_Face", String.valueOf(face));
    }

    public boolean changeBeauty(int styleID) {
        return changeBeauty(styleID, false);
    }

    public boolean changeBeauty(int styleID, boolean isSecond) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (類型.膚色(styleID)) {
            if (!ii.isSkinExist(styleID)) {
                return false;
            }
            if (isSecond) {
                setSecondSkinColor((byte) styleID);
                if (JobConstants.is神之子(getJob())) {
                    send(MaplePacketCreator.zeroInfo(this, 0x8, true));
                }
            } else {
                setSkinColor((byte) styleID);
                updateSingleStat(MapleStat.皮膚, styleID);
            }
        } else if (類型.臉型(styleID)) {
            if (!ii.isFaceExist(styleID)) {
                return false;
            }
            if (isSecond) {
                setSecondFace(styleID);
                if (JobConstants.is神之子(getJob())) {
                    send(MaplePacketCreator.zeroInfo(this, 0x20, true));
                }
            } else {
                setFace(styleID);
                updateSingleStat(MapleStat.臉型, styleID);
            }
        } else if (類型.髮型(styleID)) {
            if (!ii.isHairExist(styleID)) {
                return false;
            }
            if (isSecond) {
                setSecondHair(styleID);
                if (JobConstants.is神之子(getJob())) {
                    send(MaplePacketCreator.zeroInfo(this, 0x10, true));
                }
            } else {
                setHair(styleID);
                updateSingleStat(MapleStat.髮型, styleID);
            }
        } else {
            return false;
        }
        if (isSecond) {
            if (JobConstants.is天使破壞者(getJob())) {
                send(MaplePacketCreator.DressUpInfoModified(this));
            }
        } else {
            equipChanged();
        }
        return true;
    }

    public boolean changeAndroidBeauty(int styleID) {
        if (android == null) {
            return false;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (類型.膚色(styleID)) {
            if (!ii.isSkinExist(styleID)) {
                return false;
            }
            android.setSkin(styleID);
        } else if (類型.臉型(styleID)) {
            if (!ii.isFaceExist(styleID)) {
                return false;
            }
            android.setFace(styleID);
        } else if (類型.髮型(styleID)) {
            if (!ii.isHairExist(styleID)) {
                return false;
            }
            android.setHair(styleID);
        } else {
            return false;
        }
        android.saveToDb();
        updateAndroidLook();
        return true;
    }

    public Point getOldPosition() {
        return old;
    }

    public void setOldPosition(Point x) {
        this.old = x;
    }

    /*
     * 前1次使用的攻擊技能ID
     */
    public int getLastAttackSkillId() {
        return lastAttackSkillId;
    }

    public void setLastAttackSkillId(int skillId) {
        this.lastAttackSkillId = skillId;
    }

    public void setRemainingSp(int remainingSp, int skillbook) {
        this.remainingSp[skillbook] = remainingSp;
    }

    public boolean isInvincible() {
        return invincible;
    }

    public void setInvincible(boolean invinc) {
        invincible = invinc;
        if (invincible) {
            SkillFactory.getSkill(初心者.金剛不壞).getEffect(1).applyTo(this);
        } else {
            dispelBuff(初心者.金剛不壞);
        }
    }

    /*
     * 外掛檢測系統
     */
    public CheatTracker getCheatTracker() {
        return anticheat;
    }

    /*
     * 好友列表
     */
    public BuddyList getBuddylist() {
        return buddylist;
    }

    /*
     * 加減角色人氣
     */
    public void addFame(int famechange) {
        this.fame += famechange;
        getTrait(MapleTraitType.charm).addLocalExp(famechange);
        insertRanking("人氣排行", fame);
    }

    /*
     * 更新角色人氣
     */
    public void updateFame() {
        updateSingleStat(MapleStat.人氣, this.fame);
    }

    /*
     * 加減角色人氣 更新玩家人氣 是否在對話框提示
     */
    public void gainFame(int famechange, boolean show) {
        this.fame += famechange;
        updateSingleStat(MapleStat.人氣, this.fame);
        if (show && famechange != 0) {
            client.announce(MaplePacketCreator.getShowFameGain(famechange));
        }
    }

    public void updateHair(int hair) {
        setHair(hair);
        updateSingleStat(MapleStat.髮型, hair);
        equipChanged();
    }

    public void updateFace(int face) {
        setFace(face);
        updateSingleStat(MapleStat.臉型, face);
        equipChanged();
    }

    /*
     * 切換地圖
     */
    public void changeMapBanish(int mapid, String portal, String msg) {
        if (msg != null) {
            dropMessage(5, msg);
        }
        MapleMap maps = client.getChannelServer().getMapFactory().getMap(mapid);
        changeMap(maps, maps.getPortal(portal));
    }

    public final void changeMap(final int mapID, final int portalID) {
        final MapleMap map = this.client.getChannelServer().getMapFactory().getMap(mapID);
        this.changeMap(map, map.getPortal(portalID));
    }

    public void changeMapToPosition(MapleMap to, Point pos) {
        changeMapInternal(to, pos, MaplePacketCreator.sendFieldToPosition(this, to, pos));
    }

    public void changeMap(MapleMap to, Point pos) {
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(this, to, 0x80, true));
    }

    public void changeMap(MapleMap to) {
        changeMapInternal(to, to.getPortal(0).getPosition(), MaplePacketCreator.getWarpToMap(this, to, 0, false));
    }

    public void changeMap(MapleMap to, MaplePortal pto) {
        changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(this, to, pto.getId(), false));
    }

    public final void reviveMap(final MapleMap map, final MaplePortal portal) {
        this.changeMapInternal(map, portal.getPosition(), MaplePacketCreator.getWarpToMap(this, map, portal.getId(), true));
    }

    private void changeMapInternal(MapleMap to, Point pos, byte[] warpPacket) {
        if (to == null) {
            dropMessage(5, "changeMapInternal to Null");
            return;
        }
        if (MapleAntiMacro.isAntiNow(name)) {
            dropMessage(5, "被使用測謊機時無法操作。");
            return;
        }
        if (!isIntern() && !to.canEnterField(id)) {
            dropMessage(5, "地圖已經開啟防搶圖模式。");
            return;
        }
        int nowmapid = map.getId();
        this.saveToDB(false, false);

        boolean pyramid = pyramidSubway != null;
        if (map.getId() == nowmapid) {
            updateLastChangeMapTime();
            client.announce(warpPacket);
            boolean shouldChange = client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null;
            client.announce(EffectPacket.playPortalSE());
            map.userLeaveField(this);
            if (shouldChange) {
                map = to;
                setStance(0);
                setPosition(new Point(pos.x, pos.y - 50));
                to.userEnterField(this);
                stats.recalcLocalStats(this);
            }
        }
        if (pyramid && pyramidSubway != null) { //checks if they had pyramid before AND after changing
            pyramidSubway.onChangeMap(this, to.getId());
        }
        setOverMobLevelTip(false);
        playerObservable.update();
        if (JobConstants.is凱殷(job)) {
            Iterator<Entry<Integer, ForceAtomObject>> iterator = getForceAtomObjects().entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Integer, ForceAtomObject> sword = iterator.next();
                if (sword.getValue().SkillId == 凱殷.龍炸裂_1) {
                    iterator.remove();
                }
            }
        }

        if (eventInstance != null) {
            eventInstance.getHooks().playerChangedMap(this, this.getMap());
//            eventInstance.changedMap(this, to.getId());
        }
    }

    public void cancelChallenge() {
        if (challenge != 0 && client.getChannelServer() != null) {
            MapleCharacter chr = client.getChannelServer().getPlayerStorage().getCharacterById(challenge);
            if (chr != null) {
                chr.dropMessage(6, getName() + " 拒絕了您的請求.");
                chr.setChallenge(0);
            }
            dropMessage(6, "您的請求被拒絕.");
            challenge = 0;
        }
    }

    /*
     * 離開地圖處理設置
     */
    public void leaveMap(MapleMap map) {
        visibleMapObjectsLock.readLock().lock();
        final List<MapleMapObject> toRemove;
        try {
            toRemove = new ArrayList<>(visibleMapObjects);
        } finally {
            visibleMapObjectsLock.readLock().unlock();
        }
        Iterator<MapleMapObject> iterator = toRemove.iterator();
        while (iterator.hasNext()) {
            MapleMapObject mmo = iterator.next();
            iterator.remove();
            if (mmo.getType() == MapleMapObjectType.MONSTER) {
                ((MapleMonster) mmo).removeController(this);
            }
        }
        clearVisibleMapObject();
        cancelMapTimeLimitTask();
        clearLinkMid();
        cancelFishingTask();
        cancelChallenge();
        resetConversation();
        if (!getMechDoors().isEmpty()) {
            removeMechDoor();
        }
        if (getTrade() != null) {
            MapleTrade.cancelTrade(getTrade(), client, this);
        }
        setChair(null);
        getTempValues().clear();
    }

    public void clearVisibleMapObject() {
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.clear();
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
    }

    /*
     * 改變職業
     */
    public void changeJob(int newJob) {
        try {
            int oldJob = job;
            int oldSub = subcategory;
            for (MapleJob j : MapleJob.values()) {
                if (j.getIdWithSub() == newJob && newJob != j.getId()) {
                    newJob = j.getId();
                    setSubcategory(j.getSub());
                    break;
                }
            }
            dispelEffect(SecondaryStat.ShadowPartner);
            int tmpJob = newJob;
            if (JobConstants.is管理員(newJob)
                    || (JobConstants.getJobBranch(newJob) == 5 && !JobConstants.isSameJob(oldJob, newJob))
                    || JobConstants.getJobBranch(oldJob) != JobConstants.getJobBranch(newJob)
                    || JobConstants.is傑諾(oldJob) && !JobConstants.is傑諾(newJob) || !JobConstants.is傑諾(oldJob) && JobConstants.is傑諾(newJob)
                    || JobConstants.is惡魔復仇者(oldJob) && !JobConstants.is惡魔復仇者(newJob) || !JobConstants.is惡魔復仇者(oldJob) && JobConstants.is惡魔復仇者(newJob)) {
                resetStats(4, 4, 4, 4);
            }
            this.job = (short) newJob;
            //雙刀
            if (this.getSubcategory() == 1 && newJob == 400) {
                tmpJob = 400 + 65536;
            }
            updateSingleStat(MapleStat.職業, tmpJob);
            if (!JobConstants.is零轉職業(newJob) && !JobConstants.is神之子(newJob)) { //非新手職業和神之子職業
                Pair<Integer, Integer> sps = MapleCharacter.getJobChangeSP(newJob, getSubcategory(), JobConstants.getSkillBookByJob(newJob));
                if (sps != null) {
                    remainingSp[sps.getLeft()] += sps.getRight();
                    if (JobConstants.isSeparatedSpJob(newJob)) {
                        client.announce(UIPacket.getSPMsg(sps.getRight().byteValue(), (short) newJob));
                    }
                }
                if (newJob % 10 >= 1 && JobConstants.isSameJob(oldJob, newJob)) {
                    remainingAp += 5;
                    updateSingleStat(MapleStat.AVAILABLEAP, remainingAp);
                }
                if (level == 10) {
                    resetStats(4, 4, 4, 4);
                }
                if (JobConstants.is龍魔導士(job)) { //龍神
                    if (dragon == null) {
                        dragon = new MapleDragon(this);
                    }
                    dragon.setJobid(job);
                    dragon.sendSpawnData(client);
                }
                updateSingleStat(MapleStat.AVAILABLESP, 0);
            }
            if (JobConstants.is零轉職業(oldJob) && !JobConstants.is零轉職業(newJob) || !JobConstants.isSameJob(oldJob, newJob) || oldSub != subcategory) {
                short oldBeginner = JobConstants.getBeginner((short) oldJob);
                short newBeginner = JobConstants.getBeginner((short) newJob);
                spReset(oldBeginner != newBeginner ? oldBeginner : -1);
            }
            if (!JobConstants.isSameJob(oldJob, newJob)) {
                resetVSkills();
                if (!isIntern()) {
                    short slot = getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                    if (slot != -1) {
                        MapleInventoryManipulator.unequip(client, (short) -10, slot);
                    }
                    slot = getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                    if (slot != -1) {
                        MapleInventoryManipulator.unequip(client, (short) -11, slot);
                    }
                }
            }
            if (level >= 10) {
                if (isIntern()) {
                    Skill skil = SkillFactory.getSkill(SkillConstants.getSkillByJob(1007, getJob()));
                    if (skil != null && getSkillLevel(skil) <= 0) {
                        dropMessage(-1, "恭喜你獲得強化合成技能。");
                        changeSingleSkillLevel(skil, skil.getMaxLevel(), (byte) skil.getMaxLevel());
                    }
                }
                Skill skil = SkillFactory.getSkill(SkillConstants.getSkillByJob(1003, getJob()));
                if (skil != null && getSkillLevel(skil) <= 0) {
                    dropMessage(-1, "恭喜你獲得神匠之魂技能。");
                    changeSingleSkillLevel(skil, skil.getMaxLevel(), (byte) skil.getMaxLevel());
                }
            }

            int maxhp = stats.getMaxHp(false), maxmp = stats.getMaxMp(false);

            switch (job) {
                case 100: // 劍士
                case 1100: // 魂騎士1轉
                case 2100: // 戰神1轉
                case 3200: // 幻靈斗師1轉
                case 5100: // 米哈爾1轉
                case 6100: // 凱撒1轉
                    maxhp += Randomizer.rand(200, 250);
                    break;
                case 3100: // 惡魔獵手1轉
                    maxhp += Randomizer.rand(200, 250);
                    break;
                case 3110: // 惡魔獵手2轉
                    maxhp += Randomizer.rand(300, 350);
                    break;
                case 3101: //惡魔復仇者1轉
                case 3120: //惡魔復仇者2轉
                    maxhp += Randomizer.rand(500, 800);
                    break;
                case 200: // 魔法師
                case 2200: // 龍神1轉
                case 2700: // 夜光法師1轉
                    maxmp += Randomizer.rand(100, 150);
                    break;
                case 300: // 弓箭手
                case 400: // 盜賊
                case 500: // 海盜
                case 501: // 海盜炮手
                case 509: // 海盜 - 新
                case 2300: // 雙弩精靈1轉
                case 2400: // 幻影1轉
                case 3300: // 狂豹獵人1轉
                case 3500: // 機械師1轉
                case 3600: // 尖兵1轉
                    maxhp += Randomizer.rand(100, 150);
                    maxmp += Randomizer.rand(25, 50);
                    break;
                case 110: // 狂戰士
                case 120: // 見習騎士
                case 130: // 槍騎兵
                case 1110: // 魂騎士2轉
                case 2110: // 戰神2轉
                case 3210: // 幻靈斗師2轉
                case 5110: // 米哈爾2轉
                    maxhp += Randomizer.rand(300, 350);
                    break;
                case 6110: // 凱撒2轉
                    maxhp += Randomizer.rand(350, 400);
                    maxmp += Randomizer.rand(120, 180);
                    break;
                case 210: // 火毒巫師
                case 220: // 冰雷巫師
                case 230: // 牧師
                case 2710: // 夜光法師2轉
                    maxmp += Randomizer.rand(400, 450);
                    break;
                case 310: // 獵人
                case 320: // 弩弓手
                case 410: // 刺客
                case 420: // 俠盜
                case 430: // 見習刀客
                case 510: // 拳手
                case 520: // 火槍手
                case 530: // 火炮手
                case 580: // 拳手
                case 590: // 火槍手
                case 2310: // 雙弩精靈2轉
                case 2410: // 幻影2轉
                case 1310: // 風靈使者2轉
                case 1410: // 夜行者2轉
                case 3310: // 狂豹獵人2轉
                case 3510: // 機械師2轉
                case 3610: // 尖兵2轉
                    maxhp += Randomizer.rand(200, 250);
                    maxhp += Randomizer.rand(150, 200);
                    break;
                case 900: // 管理員
                case 800: // 管理者
                    maxhp += 99999;
                    maxmp += 99999;
                    break;
            }
            if (maxhp >= ServerConfig.CHANNEL_PLAYER_MAXHP) {
                maxhp = ServerConfig.CHANNEL_PLAYER_MAXHP;
            }
            if (maxmp >= ServerConfig.CHANNEL_PLAYER_MAXMP) {
                maxmp = ServerConfig.CHANNEL_PLAYER_MAXMP;
            }
            if (JobConstants.is神之子(job)) {
                maxmp = 100;
                checkZeroWeapon();
            } else if (JobConstants.is陰陽師(job)) {
                maxmp = 100;
            } else if (JobConstants.isNotMpJob(job)) {
                maxmp = 10;
            }
            stats.setInfo(maxhp, maxmp, stats.getCurrentMaxHP(), stats.getCurrentMaxMP());
            characterCard.recalcLocalStats(this);
            stats.recalcLocalStats(this);
            Map<MapleStat, Long> statup = new EnumMap<>(MapleStat.class);
            statup.put(MapleStat.HP, (long) stats.getCurrentMaxHP());
            statup.put(MapleStat.MP, (long) stats.getCurrentMaxMP());
            statup.put(MapleStat.MAXHP, (long) maxhp);
            statup.put(MapleStat.MAXMP, (long) maxmp);
            client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
            notifyChanges();
            guildUpdate();
            if (dragon != null) {
                map.broadcastMessage(SummonPacket.removeDragon(this.id));
                dragon = null;
            }
            baseSkills(); //修復技能
            if (JobConstants.is龍魔導士(newJob)) { //龍神
                if (getBuffedValue(SecondaryStat.RideVehicle) != null) {
                    dispelEffect(SecondaryStat.RideVehicle);
                }
                makeDragon();
            }
            if (JobConstants.is陰陽師(newJob)) {
                if (getBuffedValue(SecondaryStat.RideVehicle) != null) {
                    dispelEffect(SecondaryStat.RideVehicle);
                }
                initHaku();
            }
            if (newJob == 3300) { //狂豹獵人
                String customData = "1=1;2=1;3=1;4=1;5=1;6=1;7=1;8=1;9=1";
                setQuestAdd(MapleQuest.getInstance(GameConstants.美洲豹管理), (byte) 1, customData);
                client.announce(MaplePacketCreator.updateInfoQuest(GameConstants.美洲豹管理, customData));
                client.announce(MaplePacketCreator.updateJaguar(this));
            }
            updateJobItems();
            map.broadcastMessage(this, EffectPacket.showJobChanged(getId(), getJob()), false);
            map.broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
            if (android != null) {
                android.showEmotion(this, "job");
            }
            playerObservable.update();
        } catch (Exception e) {
            log.error("轉職錯誤", e); //all jobs throw errors :(
        }
    }

    public void updateJobItems() {
        int n = 1;
        final int job = this.job;
        int n2 = 0;
        switch (job) {
            case 3001: {
                n2 = 1099001;
                break;
            }
            case 3100: {
                n2 = 1099000;
                break;
            }
            case 3112: {
                n = 2;
            }
            case 3110:
            case 3111: {
                n2 = 1099001 + job % 10 + job % 100 / 10;
                break;
            }
            case 3101: {
                MapleInventoryManipulator.addItemAndEquip(getClient(), 1050249, (short) (-5), null, -1L, 0, "From System", false);
                MapleInventoryManipulator.addItemAndEquip(getClient(), 1070029, (short) (-7), null, -1L, 0, "From System", false);
                MapleInventoryManipulator.addItemAndEquip(getClient(), 1102505, (short) (-9), null, -1L, 0, "From System", false);
                MapleInventoryManipulator.addItemAndEquip(getClient(), 1099006, (short) (-10), null, -1L, 0, "From System", true);
                MapleInventoryManipulator.addItemAndEquip(getClient(), 1232001, (short) (-11), null, -1L, 0, "From System", true);
                setHair((getGender() == 0) ? 36460 : 37450);
                updateSingleStat(MapleStat.髮型, getHair());
                n2 = 1099006;
                break;
            }
            case 3122: {
                n = 2;
            }
            case 3120:
            case 3121: {
                n2 = 1099005 + job % 10 + job % 100 / 10;
                break;
            }
            case 5112: {
                n = 2;
            }
            case 5100:
            case 5110:
            case 5111: {
                n2 = 1098000 + job % 10 + job % 100 / 10;
                break;
            }
            case 6001: {
                n2 = 1352600;
                break;
            }
            case 6512: {
                n = 2;
            }
            case 6500:
            case 6510:
            case 6511: {
                n2 = 1352601 + job % 10 + job % 100 / 10;
                break;
            }
            case 6000: {
                n2 = 1352500;
                break;
            }
            case 6112: {
                n = 2;
            }
            case 6100:
            case 6110:
            case 6111: {
                n2 = 1352500 + job % 10 + job % 100 / 10;
                break;
            }
            case 3002: {
                n2 = 1353000;
                break;
            }
            case 3612: {
                n = 2;
            }
            case 3600:
            case 3610:
            case 3611: {
                n2 = 1353001 + job % 10 + job % 100 / 10;
                break;
            }
            default: {
                break;
            }
        }
        if (n2 != 0) {
            MapleInventoryManipulator.addItemAndEquip(getClient(), n2, (short) (-10), null, -1L, n, "From System when Change job. Update Item", true);
        }
    }

    public void checkZeroItem() {
        if (job != 10112 || level < 100) {
            return;
        }
        if (getKeyValue("Zero_Item") == null) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            //刪除以前的道具
            int[] toRemovePos = {-9, -5, -7};
            for (int pos : toRemovePos) {
                Item toRemove = getInventory(MapleInventoryType.EQUIPPED).getItem((short) pos);
                if (toRemove != null) {
                    MapleInventoryManipulator.removeFromSlot(client, MapleInventoryType.EQUIPPED, toRemove.getPosition(), toRemove.getQuantity(), false);
                }
            }
            //給神之子裝備
            int[][] equips = new int[][]{{1003840, -1}, //帽子
                    {1032202, -4}, //耳環
                    {1052606, -5}, //衣服
                    {1072814, -7}, //鞋子
                    {1082521, -8}, //手套
                    {1102552, -9}, //披風
                    {1113059, -12}, //戒指  無潛能
                    {1113060, -13}, //戒指  無潛能
                    {1113061, -15}, //戒指  無潛能
                    {1113062, -16}, //戒指  無潛能
                    {1122260, -17}, //項鏈
                    {1132231, -29}, //腰帶
                    {1152137, -30}, //護肩  無潛能
            };
            for (int[] i : equips) {
                if (ii.itemExists(i[0])) {
                    Equip equip = ii.getEquipById(i[0]);
                    equip.setPosition((byte) i[1]);
                    equip.setQuantity((short) 1);
                    if (i[1] != -12 && i[1] != -13 && i[1] != -15 && i[1] != -16 && i[1] != -30) {
                        equip.renewPotential(false);
                    }
                    equip.setGMLog("系統贈送");
                    forceReAddItem_NoUpdate(equip, MapleInventoryType.EQUIPPED);
                    client.announce(InventoryPacket.modifyInventory(false, Collections.singletonList(new ModifyInventory(0, equip)))); //發送獲得道具的封包
                }
            }
            equipChanged();
            MapleInventoryManipulator.addById(client, 1142634, 1, "系統贈送");
            MapleInventoryManipulator.addById(client, 2001530, 100, "系統贈");
            //給角色技能
            Map<Integer, SkillEntry> list = new HashMap<>();
            int[] skillIds = {神之子.琉之力, 神之子.璃之力};
            for (int i : skillIds) {
                Skill skil = SkillFactory.getSkill(i);
                if (skil != null && getSkillLevel(skil) <= 0) {
                    list.put(i, new SkillEntry((byte) 8, (byte) skil.getMaxLevel(), -1));
                }
            }
            if (!list.isEmpty()) {
                changeSkillsLevel(list);
            }
            setKeyValue("Zero_Item", "True");
        }
        if (getQuestStatus(40914) != 2) {
            MapleQuest.getInstance(40914).forceComplete(this, 0);
        }
    }

    public void checkZeroWeapon() {
        if (level < 100) {
            return;
        }
        int lazuli = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11).getItemId();
        int lapis = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10).getItemId();
        if (lazuli == getZeroWeapon(false) && lapis == getZeroWeapon(true)) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (int i = 0; i < 2; i++) {
            int itemId = i == 0 ? getZeroWeapon(false) : getZeroWeapon(true);
            Equip equip = ii.getEquipById(itemId);
            equip.setPosition((short) (i == 0 ? -11 : -10));
            equip.setQuantity((short) 1);
            equip.setGMLog("神之子升級贈送, 時間:" + DateUtil.getNowTime());
            equip.renewPotential(false);
            forceReAddItem_NoUpdate(equip, MapleInventoryType.EQUIPPED);
            client.announce(InventoryPacket.modifyInventory(false, Collections.singletonList(new ModifyInventory(0, equip)))); //發送獲得道具的封包
        }
        equipChanged();
    }

    public int getZeroWeapon(boolean lapis) {
        if (level < 100) {
            return lapis ? 1562000 : 1572000;
        }
        int weapon = lapis ? 1562001 : 1572001;
        if (level < 160) {
            weapon += (level % 100) / 10;
        } else if (level < 170) {
            weapon += 5;
        } else {
            weapon += 6;
        }
        return weapon;
    }

    /*
     * 修復技能
     */
    public void baseSkills() {
        checkZeroItem();
        checkInnerSkill();
        checkHyperAP();
        Map<Integer, SkillEntry> skillMap = new HashMap<>();
        int job = this.job, level = this.level;
        Skill skil;
        List<Integer> baseSkills = SkillFactory.getSkillsByLowerJob(job);
        if (baseSkills != null) {
            for (int i : baseSkills) {
                skil = SkillFactory.getSkill(i);
                if (skil == null || skil.isInvisible() || skil.isBeginnerSkill() || getLevel() < skil.getReqLevel()) {
                    continue;
                }
                SkillEntry entry = skills.get(skil.getId());
                if (entry == null) {
                    entry = new SkillEntry((byte) 0, (byte) 0, SkillFactory.getDefaultSExpiry(skil));
                }
                // FixLevel修正
                if (skil.getFixLevel() > 0 && skil.getId() != 皮卡啾.收納達人 && skil.getId() != 雪吉拉.收納達人) {
                    entry.skillevel = (byte) skil.getFixLevel();
                    int lv = entry.skillevel - getSkillLevel(i);
                    if (lv > 0) {
                        skillMap.put(i, entry);
                    }
                }
                // MasterLevel修正
                if (skil.isFourthJob() && entry.masterlevel <= 0 && skil.getMasterLevel() > 0) {
                    entry.masterlevel = (byte) skil.getMasterLevel();
                    skillMap.put(i, entry);
                } else if (!skil.isFourthJob() && entry.masterlevel > 0 && skil.getMasterLevel() > 0) {
                    entry.masterlevel = 0;
                    skillMap.put(i, entry);
                }
            }
        }

        // 處理傳授技能
        int teachskill = JobConstants.getTeachSkillID(job);
        if (teachskill != -1) {
            int nowLinkLevel = 0;
            int maxLinkLevel = 2;
            if (JobConstants.is神之子(job)) {
                nowLinkLevel = level < 100 ? 0 : level < 125 ? 1 : level < 150 ? 2 : level < 175 ? 3 : level < 200 ? 4 : 5;
                maxLinkLevel = 5;
            } else {
                nowLinkLevel = level < 70 ? 0 : level < 120 ? 1 : 2;
            }
            skil = SkillFactory.getSkill(teachskill);
            if (skil != null) {
                maxLinkLevel = Math.min(skil.getMaxLevel(), maxLinkLevel);
            }
            if (skil == null) {
                if (isAdmin()) {
                    dropDebugMessage("[傳授技能] 更新傳授技能出錯，技能不存在。");
                }
            } else if (!skills.containsKey(skil) || skills.get(skil).skillevel < nowLinkLevel || skills.get(skil).skillevel > maxLinkLevel) {
                skillMap.put(teachskill, new SkillEntry((byte) Math.min(maxLinkLevel, nowLinkLevel), (byte) (nowLinkLevel > 0 ? nowLinkLevel : -1), SkillFactory.getDefaultSExpiry(skil)));
            }
        }

        // 收納達人處理
        int addSlot = 0;
        short beginner = JobConstants.getBeginner((short) job);
        if (JobConstants.is皮卡啾(job) || JobConstants.is雪吉拉(job)) {
            skil = SkillFactory.getSkill((beginner * 10000) + 111);
            if (skil != null && getSkillLevel(skil) < 1) {
                skillMap.put(skil.getId(), new SkillEntry((byte) 1, (byte) 0, SkillFactory.getDefaultSExpiry(skil)));
                addSlot = 48;
            }
        } else if ((JobConstants.is冒險家(job) && JobConstants.is海盜(job)) || JobConstants.is隱月(job) || JobConstants.is凱內西斯(job)) {
            skil = SkillFactory.getSkill((beginner * 10000) + 112);
            if (skil != null && getSkillLevel(skil) < 2 && JobConstants.getJobGrade(job) > 1) {
                skillMap.put(skil.getId(), new SkillEntry((byte) 2, (byte) 0, SkillFactory.getDefaultSExpiry(skil)));
                addSlot = 48;

                skil = SkillFactory.getSkill((beginner * 10000) + 111);
                if (skil != null && getSkillLevel(skil) != 0) {
                    changeSkillLevel(skil, (byte) -1, (byte) 0);
                }
            } else if ((skil = SkillFactory.getSkill((beginner * 10000) + 111)) != null && getSkillLevel(skil) < 1 && JobConstants.getJobGrade(job) == 1) {
                skillMap.put(skil.getId(), new SkillEntry((byte) 1, (byte) 0, SkillFactory.getDefaultSExpiry(skil)));
                addSlot = 36;
            }
        }
        if (addSlot > 0) {
            for (MapleInventoryType type : MapleInventoryType.values()) {
                if (type.getType() <= MapleInventoryType.UNDEFINED.getType() || type.getType() >= MapleInventoryType.CASH.getType()) {
                    continue;
                }
                MapleInventory inv = getInventory(type);
                if (inv == null || inv.getSlotLimit() >= addSlot) {
                    continue;
                }
                inv.setSlotLimit((byte) addSlot);
                client.announce(InventoryPacket.updateInventorySlotLimit(type.getType(), (byte) addSlot));
            }
        }

        AbstractSkillHandler handler = SkillClassFetcher.getHandlerByJob(getJobWithSub());
        int handleRes = -1;
        if (handler != null) {
            SkillClassApplier applier = new SkillClassApplier();
            applier.skillMap = skillMap;
            handleRes = handler.baseSkills(this, applier);
            if (handleRes == 0) {
                return;
            } else if (handleRes == 1) {
                skillMap = applier.skillMap;
            }
        }
        if (!skillMap.isEmpty()) {
            changeSkillsLevel(skillMap);
        }
    }

    public void makeDragon() {
        dragon = new MapleDragon(this);
    }

    public MapleDragon getDragon() {
        return dragon;
    }

    public void setDragon(MapleDragon d) {
        this.dragon = d;
    }

    public MapleSkillPet getSkillPet() {
        return this.skillPet;
    }

    public void setSpawnSkillPet(final MapleSkillPet am) {
        this.skillPet = am;
    }

    public void initHaku() {
//        lw = new MapleSkillPet(this);
//        map.broadcastMessage(SummonPacket.spawnLittleWhite(lw));//showLittleWhite
        this.setSpawnSkillPet(new MapleSkillPet(this));
    }

    public MapleSkillPet getHaku() {
        return skillPet;
    }

    public void gainAp(short ap) {
        this.remainingAp += ap;
        updateSingleStat(MapleStat.AVAILABLEAP, this.remainingAp);
    }

    public void gainSP(int sp) {
        this.remainingSp[JobConstants.getSkillBookByJob(job)] += sp; //default
        updateSingleStat(MapleStat.AVAILABLESP, 0);
        client.announce(UIPacket.getSPMsg((byte) sp, job));
    }

    public void gainSP(int sp, int skillbook) {
        this.remainingSp[skillbook] += sp; //default
        updateSingleStat(MapleStat.AVAILABLESP, 0);
        if (sp >= 0) {
            client.announce(UIPacket.getSPMsg((byte) sp, (short) 0));
        }
    }

    public void resetSP(int sp) {
        for (int i = 0; i < remainingSp.length; i++) {
            this.remainingSp[i] = sp;
        }
        updateSingleStat(MapleStat.AVAILABLESP, 0);
    }

    public void resetAPSP() {
        resetSP(0);
        gainAp((short) -this.remainingAp);
    }

    public int getHitCountBat() {
        return hitcountbat;
    }

    public void setHitCountBat(int hitcount) {
        this.hitcountbat = hitcount;
    }

    public int getBatCount() {
        return batcount;
    }

    public void setBatCount(int count) {
        this.batcount = count;
    }

    /*
     * 專業技能技能設置
     */
    public List<Integer> getProfessions() {
        List<Integer> prof = new ArrayList<>();
        for (int i = 9200; i <= 9204; i++) {
            if (getProfessionLevel(i * 10000) > 0) {
                prof.add(i);
            }
        }
        return prof;
    }

    public byte getProfessionLevel(int id) {
        int ret = getSkillLevel(id);
        if (ret <= 0) {
            return 0;
        }
        return (byte) ((ret >>> 24) & 0xFF); //the last byte
    }

    public short getProfessionExp(int id) {
        int ret = getSkillLevel(id);
        if (ret <= 0) {
            return 0;
        }
        return (short) (ret & 0xFFFF); //the first two byte
    }

    public boolean addProfessionExp(int id, int expGain) {
        int ret = getProfessionLevel(id);
        if (ret <= 0 || ret >= 12) {
            return false;
        }
        int newExp = getProfessionExp(id) + expGain;
        if (newExp >= GameConstants.getProfessionEXP(ret)) {
            //gain level
            changeProfessionLevelExp(id, ret + 1, newExp - GameConstants.getProfessionEXP(ret));
            int traitGain = (int) Math.pow(2, ret + 1);
            switch (id) {
                case 92000000: //採藥
                    traits.get(MapleTraitType.sense).addExp(traitGain, this);
                    break;
                case 92010000: //採礦
                    traits.get(MapleTraitType.will).addExp(traitGain, this);
                    break;
                case 92020000: //裝備製作
                case 92030000: //飾品製作
                case 92040000: //煉金術
                    traits.get(MapleTraitType.craft).addExp(traitGain, this);
                    break;
            }
            return true;
        } else {
            changeProfessionLevelExp(id, ret, newExp);
            return false;
        }
    }

    /*
     * 改變專業技能技能的等級
     */
    public void changeProfessionLevelExp(int id, int level, int exp) {
        changeSingleSkillLevel(SkillFactory.getSkill(id), ((level & 0xFF) << 24) + (exp & 0xFFFF), (byte) 10);
    }

    /*
     * 改變單個技能技能的等級
     */
    public void changeSkillLevel(Skill skill, byte newLevel, int newMasterlevel) {
        changeSingleSkillLevel(skill, newLevel, newMasterlevel, -1);
    }

    public void changeSingleSkillLevel(Skill skill, int newLevel, int newMasterlevel) { //1 month
        if (skill == null) {
            return;
        }
        changeSingleSkillLevel(skill, newLevel, newMasterlevel, SkillFactory.getDefaultSExpiry(skill));
    }

    public void changeSingleSkillLevel(int skillid, int newLevel, int newMasterlevel) { //1 month
        Skill skill = SkillFactory.getSkill(skillid);
        if (skill != null) {
            changeSingleSkillLevel(skill, newLevel, newMasterlevel, SkillFactory.getDefaultSExpiry(skill));
        }
    }

    public void changeSingleSkillLevel(int skillid, int newLevel, int newMasterlevel, long expiration) { //1 month
        changeSingleSkillLevel(SkillFactory.getSkill(skillid), newLevel, newMasterlevel, expiration);
    }

    public void changeSingleSkillLevel(Skill skill, int newLevel, int newMasterlevel, long expiration) {
        Map<Integer, SkillEntry> list = new HashMap<>();
        boolean hasRecovery = false, recalculate = false;
        if (changeSkillData(skill, newLevel, newMasterlevel, expiration)) { // no loop, only 1
            list.put(skill.getId(), new SkillEntry(newLevel, newMasterlevel, expiration, getSkillTeachId(skill), getSkillTeachTimes(skill), getSkillPosition(skill)));
            if (SkillConstants.isRecoveryIncSkill(skill.getId())) {
                hasRecovery = true;
            }
            if (skill.getId() < 80000000) {
                recalculate = true;
            }
        }
        if (list.isEmpty()) { // nothing is changed
            return;
        }
        client.announce(MaplePacketCreator.updateSkills(list));
        reUpdateStat(hasRecovery, recalculate);
    }

    public void maxSkillsByJob(int jobid) {
        if (this.getJob() == 16000 || (this.getJob() >= 16400 && this.getJob() <= 16412)) {
            if (getSkillLevel(160000076) < 10) {
                changeSkillLevel(SkillFactory.getSkill(160000076), (byte) 10, (byte) 10);
            }
        }
        MapleData data = MapleDataProviderFactory.getSkill().getData(StringUtil.getLeftPaddedStr("" + jobid, '0', 3) + ".img");
        byte maxLevel = 0;
        for (MapleData skill : data) {
            if (skill != null) {
                for (MapleData skillId : skill.getChildren()) {
                    if (!skillId.getName().equals("icon")) {
                        maxLevel = (byte) MapleDataTool.getIntConvert("maxLevel", skillId.getChildByPath("common"), 0);
                        if (maxLevel > 30) {
                            maxLevel = 30;
                        }
                        if (MapleDataTool.getIntConvert("invisible", skillId, 0) == 0) { //스킬창에 안보이는 스킬은 올리지않음
                            if (getLevel() >= MapleDataTool.getIntConvert("reqLev", skillId, 0)) {
                                try {
                                    changeSkillLevel(SkillFactory.getSkill(Integer.parseInt(skillId.getName())), maxLevel, maxLevel);
                                } catch (NumberFormatException ex) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void changeTeachSkillsLevel() {
        for (Entry<Integer, SkillEntry> entry : getSkills().entrySet()) {
            Skill skill = SkillFactory.getSkill(entry.getKey());
            if (skill != null && skill.isTeachSkills()) {
                changeSkillData(skill, SkillConstants.getLinkSkillslevel(skill, 0, level), (byte) skill.getMasterLevel(), entry.getValue().expiration);
            }
        }
    }

    public void changeSkillsLevel(Map<Integer, SkillEntry> skills) {
        if (skills.isEmpty()) {
            return;
        }
        Map<Integer, SkillEntry> list = new HashMap<>();
        boolean hasRecovery = false, recalculate = false;
        for (Entry<Integer, SkillEntry> data : skills.entrySet()) {
            Skill skill = SkillFactory.getSkill(data.getKey());
            if (changeSkillData(skill, data.getValue().skillevel, data.getValue().masterlevel, data.getValue().expiration)) {
                list.put(data.getKey(), data.getValue());
                if (SkillConstants.isRecoveryIncSkill(data.getKey())) {
                    hasRecovery = true;
                }
                if (data.getKey() < 80000000) {
                    recalculate = true;
                }
            }
        }
        if (list.isEmpty()) { // nothing is changed
            return;
        }
        client.announce(MaplePacketCreator.updateSkills(list));
        reUpdateStat(hasRecovery, recalculate);
    }

    /*
     * 角色技能改變後更新角色能力狀態
     */
    private void reUpdateStat(boolean hasRecovery, boolean recalculate) {
        changed_skills = true;
        if (hasRecovery) {
            stats.relocHeal(this);
        }
        if (recalculate) {
            stats.recalcLocalStats(this);
        }
    }

    /*
     * 新的改變技能
     */
    public boolean changeSkillData(Skill skill, int newLevel, int newMasterlevel, long expiration) {
        if (skill == null || !SkillConstants.isApplicableSkill(skill.getId()) && !SkillConstants.isApplicableSkill_(skill.getId())) {
            return false;
        }
//        if (newLevel <= 0 && newMasterlevel <= 0) {
//            if (skills.containsKey(skill.getId())) {
//                skills.remove(skill.getId());
//            } else {
//                return false; //nothing happen
//            }
//        } else {
//            skills.put(skill.getId(), new SkillEntry(newLevel, newMasterlevel, expiration, getSkillTeachId(skill), getSkillPosition(skill)));
//        }
//        changed_skills = true;
        return changeSkillRecord(skill, newLevel, newMasterlevel, expiration, getSkillTeachId(skill), getSkillTeachTimes(skill), getSkillPosition(skill)) != null;
    }

    public SkillEntry changeSkillRecord(final Skill skill, final int newLevel, final int masterLevel, final long expiration, final int teachId, final int teachTimes, final byte pos) {
        SkillEntry skillEntry = null;
        int oldLevel;
        if (newLevel <= 0 && masterLevel <= 0) {
            if (this.skills.containsKey(skill.getId())) {
                skillEntry = this.skills.remove(skill.getId());
                oldLevel = skillEntry.skillevel;
                skillEntry.skillevel = newLevel;
                skillEntry.masterlevel = masterLevel;
                skillEntry.expiration = expiration;
                skillEntry.teachId = teachId;
                skillEntry.teachTimes = teachTimes;
                skillEntry.position = pos;
            } else {
                oldLevel = 0;
            }
        } else if (this.skills.containsKey(skill.getId())) {
            skillEntry = this.skills.get(skill.getId());
            oldLevel = skillEntry.skillevel;
            skillEntry.skillevel = newLevel;
            skillEntry.masterlevel = masterLevel;
            skillEntry.expiration = expiration;
            skillEntry.teachId = teachId;
            skillEntry.teachTimes = teachTimes;
            skillEntry.position = pos;
        } else {
            oldLevel = 0;
            skillEntry = new SkillEntry(newLevel, masterLevel, expiration, teachId, teachTimes, pos);
            this.skills.put(skill.getId(), skillEntry);
        }

        if (skill.getId() == 卡蒂娜.武器變換終章) {
            if (newLevel > 0) {
                if (oldLevel <= 0) {
                    MapleStatEffect effect = getSkillEffect(skill.getId());
                    if (effect != null) {
                        effect.applyTo(this);
                    }
                }
            } else {
                dispelBuff(skill.getId());
            }
        }
        this.changed_skills = true;
        return skillEntry;
    }

    public void changeSkillLevel_Skip(Map<Integer, SkillEntry> skill) {
        changeSkillLevel_Skip(skill, false);
    }

    public void changeSkillLevel_Skip(Map<Integer, SkillEntry> skill, boolean write) {
        if (skill.isEmpty()) {
            return;
        }
        Map<Integer, SkillEntry> newlist = new HashMap<>();
        for (Entry<Integer, SkillEntry> date : skill.entrySet()) {
            if (date.getKey() == null) {
                continue;
            }
            //System.err.println("changeSkillLevel_Skip - " + date.getKey().getId() + " skillevel " + date.getValue().skillevel + " masterlevel " + date.getValue().masterlevel + " - " + date.getKey().getName());
            newlist.put(date.getKey(), date.getValue());
            if (date.getValue().skillevel == 0 && date.getValue().masterlevel == 0) {
                skills.remove(date.getKey());
            } else {
                skills.put(date.getKey(), date.getValue());
            }
        }
        if (write && !newlist.isEmpty()) {
            client.announce(MaplePacketCreator.updateSkills(newlist));
        }
    }

    public void changePetSkillLevel(Map<Integer, SkillEntry> skill) {
        if (skill.isEmpty()) {
            return;
        }
        Map<Integer, SkillEntry> newlist = new HashMap<>();
        for (Entry<Integer, SkillEntry> date : skill.entrySet()) {
            if (date.getKey() == null) {
                continue;
            }
            //System.err.println("changePetSkillLevel - ID: " + date.getKey().getId() + " skillevel: " + date.getValue().skillevel + " masterlevel: " + date.getValue().masterlevel + " - " + date.getKey().getName());
            if (date.getValue().skillevel == 0 && date.getValue().masterlevel == 0) {
                if (skills.containsKey(date.getKey())) {
                    skills.remove(date.getKey());
                    newlist.put(date.getKey(), date.getValue());
                }
            } else if (getSkillLevel(date.getKey()) != date.getValue().skillevel) {
                skills.put(date.getKey(), date.getValue());
                newlist.put(date.getKey(), date.getValue());
            }
        }
        if (!newlist.isEmpty()) {
            for (Entry<Integer, SkillEntry> date : newlist.entrySet()) {
                client.announce(MaplePacketCreator.updatePetSkill(date.getKey(), date.getValue().skillevel, date.getValue().masterlevel, date.getValue().expiration));
            }
            reUpdateStat(false, true);
        }
    }

    public void changeTeachSkill(int skillid, int teachid, int skillevel, boolean delete) {
        Skill skill = SkillFactory.getSkill(skillid);
        if (skill == null) {
            return;
        }
        if (!delete) {
            long timeNow = System.currentTimeMillis();
            int tSkillId = SkillConstants.getTeamTeachSkillId(skillid);
            if (tSkillId > 1) {
                Skill tSkill = SkillFactory.getSkill(tSkillId);
                SkillEntry se = skills.get(tSkillId);
                if (tSkill != null && se == null) {
                    skills.put(tSkillId, new SkillEntry(skillevel, (byte) tSkill.getMasterLevel(), timeNow, id, 0));
                } else {
                    se.skillevel += skillevel;
                }
            }
            skills.put(skillid, new SkillEntry(skillevel, (byte) skill.getMasterLevel(), timeNow, teachid, sonOfLinkedSkills.get(skillid).getRight().teachTimes));
            linkSkills.put(skillid, new SkillEntry(skillevel, (byte) skill.getMasterLevel(), timeNow, teachid, sonOfLinkedSkills.get(skillid).getRight().teachTimes));
        } else {
            HashMap<Integer, Integer> hashMap = new HashMap<>();
            hashMap.put(skillid, teachid);
            skills.remove(skillid);
            linkSkills.remove(skillid);
            int[] tSkills = SkillConstants.getTeamTeachSkills(skillid);
            if (tSkills != null) {
                for (int id : tSkills) {
                    Skill linkSkill = SkillFactory.getSkill(id);
                    SkillEntry se = linkSkills.remove(id);
                    skills.remove(id);
                    if (linkSkill != null && se != null) {
                        hashMap.put(id, se.teachId);
                    }
                }
            }
            send(MaplePacketCreator.DeleteLinkSkillResult(hashMap));
        }
        changed_skills = true;
    }

    public void changeTeachSkill(int skillId, int toChrId) {
        SkillEntry ret = getSkillEntry(skillId);
        if (ret != null) {
            ret.teachId = toChrId;
            client.announce(MaplePacketCreator.updateSkill(skillId, toChrId, ret.masterlevel, ret.expiration));
            changed_skills = true;
        }
    }

    public void playerDead() {
        final SecondaryStat[] resistDeadStats = {
                SecondaryStat.HeavensDoor,
                SecondaryStat.PreReviveOnce,
                SecondaryStat.ReviveOnce,
                SecondaryStat.FlareTrick
        };

        for (SecondaryStat stat : resistDeadStats) {
            MapleStatEffect effect = getEffectForBuffStat(stat);
            if (effect != null) {
                boolean isPassiveEffect = false;
                switch (effect.getSourceId()) {
                    case 隱月.換魂:
                    case 隱月.死裡逃生:
                    case 隱月.死裡逃生_傳授:
                    case 暗夜行者.Switch_type_Dead_闇黑蔓延:
                        isPassiveEffect = true;
                        break;
                }
                if (isPassiveEffect && isSkillCooling(effect.getSourceId())) {
                    continue;
                }
                int recoveryHPR = effect.getX() <= 0 ? 100 : effect.getX();
                int coolTime = 0;
                switch (stat) {
                    case FlareTrick:
                        recoveryHPR = effect.getY();
                        break;
                    case ReviveOnce:
                        recoveryHPR = 100;
                        break;
                    case PreReviveOnce:
                        if (!effect.makeChanceResult(this)) {
                            if (isDebug()) {
                                dropMessage(10, "觸發死裡逃生BUFF失敗，概率" + effect.getProp() + "%。");
                            }
                            continue;
                        }
                        break;
                }
                stats.setHp(Math.min(stats.getCurrentMaxHP() * recoveryHPR / 100, stats.getCurrentMaxHP()));
                dispelEffect(effect.getSourceId());
                updateHPMP(false);
                if (effect.getSourceId() != 通用V核心.超新星通用.萬神殿_1) {
                    effect.unprimaryPassiveApplyTo(this);
                }
                if (isPassiveEffect) {
                    registerSkillCooldown(effect, true);
                }
                if (effect.getSourceId() == 暗夜行者.Switch_type_Dead_闇黑蔓延) {
                    effect.applyBuffEffect(this, this, effect.getDuration(), false, false, true, this.getPosition());
                }
                return;
            }
        }

        if (JobConstants.is黑騎士(job) && getBuffStatValueHolder(SecondaryStat.ReincarnationOnOff) != null && getTotalSkillLevel(黑騎士.轉生) > 0 && !isSkillCooling(黑騎士.轉生_狀態)) {
            getStat().setHp(getStat().getCurrentMaxHP());
            getStat().setMp(getStat().getCurrentMaxMP());
            this.updateHPMP(false);
            getSkillEffect(黑騎士.轉生_狀態).applyTo(this, true);
            return;
        }
        final MapleStatEffect skillEffect;
        if ((skillEffect = this.getSkillEffect(80010040)) != null && !this.isSkillCooling(80010040)) {
            this.registerSkillCooldown(skillEffect, true);
            this.heal();
            skillEffect.applyBuffEffect(this, this, 2000, false, false, true, this.getPosition());
            return;
        }
        if (!inEvent()) {
            DeadDebuff.setDebuff(this);
        }


        if (android != null) {
            android.showEmotion(this, "dead");
        }
        if (!stats.checkEquipDurabilitys(this, -100)) { //i guess this is how it works ?
            dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
        }
        dispelEffect(SecondaryStat.ShadowPartner);
        dispelEffect(SecondaryStat.Morph);
        dispelEffect(SecondaryStat.Flying);
        dispelEffect(SecondaryStat.RideVehicle);
        dispelEffect(SecondaryStat.Mechanic);
        dispelEffect(SecondaryStat.Regen);
        dispelEffect(SecondaryStat.IndieMHP);
        dispelEffect(SecondaryStat.IndieMMP);
        dispelEffect(SecondaryStat.EMHP);
        dispelEffect(SecondaryStat.EMMP);
        dispelEffect(SecondaryStat.MaxHP);
        dispelEffect(SecondaryStat.MaxMP);
        dispelEffect(SecondaryStat.SpiritLink);
        dispelEffect(SecondaryStat.StopForceAtomInfo);
        dispelEffect(SecondaryStat.NewFlying);
        dispelEffect(SecondaryStat.Frenzy);
        dispelSummons();
        checkFollow();
        specialStats.resetSpecialStats();

        if (pyramidSubway != null) {
            stats.setHp((short) 50, this);
            pyramidSubway.fail(this);
        }

        playerDeadResponse();

        if (getEventInstance() != null) {
            getEventInstance().getHooks().playerDied(this);
        }
    }

    public void playerDeadResponse() {
        int type = 1;
        int value = UIReviveType.UIReviveType_Normal.getType();
        if (android != null && (android.getItemId() == 1662072 || android.getItemId() == 1662073)) { // 戰鬥機器人
            send(EffectPacket.ProtectBuffGain(android.getItemId(), 0));
            value = UIReviveType.UIReviveType_CombatAndroid.getType();
        } else {
            // 凍結加持器
            if (getItemQuantity(5133000) > 0 || getItemQuantity(5133001) > 0) {
                type |= 2;
            }
            if (inEvent()) {
                if (getDeathCount() > 0) {
                    value = UIReviveType.UIReviveType_MagnusNormalHard.getType();
                } else {
                    value = UIReviveType.UIReviveType_OnUIDeathCountInfo.getType();
                    type &= ~2;
                }
            } else {
                if (haveItem(5420008)) {
                    // 高級服務復活 滿HPMP原地復活
                    value = UIReviveType.UIReviveType_PremiumUser.getType();
                } else if (getEffectForBuffStat(SecondaryStat.SoulStone) != null) {
                    // 靈魂之石復活 恢復HP原地復活
                    value = UIReviveType.UIReviveType_SoulStone.getType();
                } else if (this.getBossLog("原地復活") < ServerConfig.CHANNEL_PLAYER_RESUFREECOUNT && getLevel() >= 70) {
                    // 每日原地復活
                    value = UIReviveType.UIReviveType_PremiumUser2.getType();
                } else if (this.getItemQuantity(5510000) > 0) {
                    // 原地復活術復活 恢復40%HP原地復活
                    value = UIReviveType.UIReviveType_UpgradeTombItem.getType();
                } else if (this.getItemQuantity(5511001) > 0) {
                    // 女神的紡車復活 滿HPMP原地復活
                    value = UIReviveType.UIReviveType_Nemesis.getType();
                } else if (ServerConfig.partyQuestRevive && getPQPoint() >= 10) {
                    // 組隊點數復活 原地復活
                    value = UIReviveType.UIReviveType_UsingPartyPoint.getType();
                }
            }
        }

        int autoReviveTime = 0;
        int reviveDelay = 0;
        boolean reviveEnd = false;
        if (inEvent() && getDeathCount() >= 0) {
            if (getDeathCount() > 0) {
                autoReviveTime = 30;
                reviveDelay = 5;
            } else if (eventInstance != null && getDeathCount() == 0) {
                autoReviveTime = 30;
                reviveEnd = true;
//                autoReviveTime = (int) Math.ceil(eventInstance.getTimeLeft() / 1000.0);
//                reviveEnd = true;
//                if (eventInstance.getPlayers().stream().noneMatch(ret -> ret.getEventReviveCount() > 0 || ret.isAlive())) {
//                    eventInstance.getPlayers().forEach(ret -> ret.send(MaplePacketCreator.showEffect("hillah/fail")));
//                    eventInstance.restartEventTimer(3000);
//                }
            }
        }
        client.announce(EffectPacket.playerDeadConfirm(type, false, value, autoReviveTime, reviveDelay, reviveEnd));
    }

    public void updatePartyMemberHP() {
        if (party != null && client.getChannelServer() != null) {
            int channel = client.getChannel();
            for (PartyMember pm : party.getMembers()) {
                if (pm != null && pm.getFieldID() == getMapId() && pm.getChannel() == channel) {
                    MapleCharacter other = client.getChannelServer().getPlayerStorage().getCharacterByName(pm.getCharName());
                    if (other != null) {
                        other.getClient().write(UserRemote.receiveHP(this));
                    }
                }
            }
        }
    }

    public void receivePartyMemberHP() {
        if (party == null) {
            return;
        }
        int channel = client.getChannel();
        for (PartyMember pm : party.getMembers()) {
            if (pm != null && pm.getFieldID() == getMapId() && pm.getChannel() == channel) {
                MapleCharacter other = client.getChannelServer().getPlayerStorage().getCharacterByName(pm.getCharName());
                if (other != null) {
                    client.write(UserRemote.receiveHP(other));
                }
            }
        }
    }

    public void heal() {
        this.stats.heal(this);
    }

    /**
     * 恢復HP
     *
     * @param delta 恢復量
     */
    public void healHP(int delta) {
        addHP(delta);
        if (delta != 0) {
            client.announce(EffectPacket.showOwnHpHealed(delta));
            getMap().broadcastMessage(this, EffectPacket.showHpHealed(getId(), delta), false);
        }
    }

    /**
     * 恢復HP
     *
     * @param delta 恢復量
     */
    public void healMP(int delta) {
        addMP(delta);
        if (delta != 0) {
            client.announce(EffectPacket.showOwnHpHealed(delta));
            getMap().broadcastMessage(this, EffectPacket.showHpHealed(getId(), delta), false);
        }
    }

    public void healHPMP(int deltahp, int deltamp) {
        addHPMP(deltahp, deltamp, false, false);
        if (deltahp != 0 && deltamp != 0) {
            send(EffectPacket.showOwnHpHealed(deltahp != 0 ? deltahp : deltamp));
            getMap().broadcastMessage(this, EffectPacket.showHpHealed_Other(getId(), deltahp != 0 ? deltahp : (deltamp)), false);
        }
    }

    /**
     * Convenience function which adds the supplied parameter to the current hp
     * then directly does a updateSingleStat.
     *
     * @param delta
     */
    public void addHP(long delta) {
        if (stats.setHp((int) (stats.getHp() + delta), this)) {
            updateSingleStat(MapleStat.HP, stats.getHp());
        }
    }

    /**
     * Convenience function which adds the supplied parameter to the current mp
     * then directly does a updateSingleStat.
     *
     * @param delta
     */
    public void addMP(long delta) {
        addMP((int) delta, false);
    }

    public void addMP(int delta, boolean ignore) {
        if (JobConstants.isNotMpJob(getJob()) && GameConstants.getMPByJob(getJob()) <= 0) {
            return;
        }
        if ((delta < 0 && JobConstants.is惡魔殺手(getJob())) || !JobConstants.is惡魔殺手(getJob()) || ignore) {
            if (stats.setMp(stats.getMp() + delta)) {
                updateSingleStat(MapleStat.MP, stats.getMp());
            }
        }
    }

    public void addDemonMp(int delta) {
        if (delta > 0 && (getJob() == 3111 || getJob() == 3112)) {
            if (stats.setMp(stats.getMp() + delta)) {
                updateSingleStat(MapleStat.MP, stats.getMp());
            }
        }
    }

    public final void addHPMP(final int hpRate, final int mpRate) {
        this.addHPMP(stats.getCurrentMaxHP() * hpRate / 100, stats.getCurrentMaxMP() * mpRate / 100, false, true);
    }

    public final void addHPMP(final int hpDiff, final int mpDiff, final boolean item) {
        this.addHPMP(hpDiff, mpDiff, item, false);
    }

    public void addHPMP(int hpDiff, int mpDiff, boolean item, boolean show) {
        addhpmpLock.lock();
        try {
            if (isAlive()) {
                if (stats.getHp() == stats.getCurrentMaxHP()) {
                    show = false;
                }
                SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(SecondaryStat.DawnShield_ExHP, 80011248);
                if (mbsvh != null && mbsvh.value > 0 && hpDiff < 0) {
                    show = false;
                    addShieldHP(hpDiff);
                } else {
                    stats.setHp(stats.getHp() + hpDiff);
                    if ((mbsvh = getBuffStatValueHolder(SecondaryStat.RevenantGauge)) != null) {
                        if (stats.getHp() <= 0) {
                            stats.setHp(1);
                        }
                        if (hpDiff < 0) {
                            int maxZ = stats.getCurrentMaxHP() * mbsvh.effect.getQ() / 100;
                            if (mbsvh.z < maxZ) {
                                mbsvh.z = Math.min(maxZ, mbsvh.z + Math.abs(hpDiff));
                                send(BuffPacket.giveBuff(this, mbsvh.effect, Collections.singletonMap(SecondaryStat.RevenantGauge, mbsvh.effect.getSourceId())));
                            }
                        }
                    }
                    if ((mbsvh = getBuffStatValueHolder(SecondaryStat.CrossOverChain)) != null) {
                        if (hpDiff < 0) {
                            int qHP = stats.getCurrentMaxHP() * mbsvh.effect.getQ() / 100;
                            if (stats.getHp() < qHP) {
                                mbsvh.value = (int) Math.ceil((double) (stats.getHp() * mbsvh.effect.getX()) / qHP);
                            } else {
                                mbsvh.value = mbsvh.effect.getX();
                            }
                            mbsvh.z = (stats.getCurrentMaxHP() - stats.getHp()) * mbsvh.effect.getY() / 100;
                            send(BuffPacket.giveBuff(this, mbsvh.effect, Collections.singletonMap(SecondaryStat.CrossOverChain, mbsvh.effect.getSourceId())));
                        }
                    }
                }
                stats.setMp(stats.getMp() + mpDiff);
                updateHPMP(item);
                updatePartyMemberHP();
                if (show && client != null && map != null && hpDiff != 0) {
                    client.announce(EffectPacket.showIncDecHPRegen(-1, hpDiff));
                    map.broadcastMessage(this, EffectPacket.showIncDecHPRegen(id, hpDiff), getPosition());
                }
                if (!isAlive()) {
                    playerDead();
                }
            }
        } finally {
            addhpmpLock.unlock();
        }
    }

    public void updateHPMP(boolean itemReaction) {
        final Map<MapleStat, Long> statups = new EnumMap<>(MapleStat.class);
        statups.put(MapleStat.HP, (long) stats.getHp());
        statups.put(MapleStat.MP, (long) stats.getMp());
        client.announce(MaplePacketCreator.updatePlayerStats(statups, itemReaction, this));
    }

    public void addShieldHP(int hpDiff) {
        if (isAlive()) {
            SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(SecondaryStat.DawnShield_ExHP, 80011248);
            MapleStatEffect effect = getSkillEffect(80011248);
            if (mbsvh != null && mbsvh.effect != null && mbsvh.value > 0 && effect != null) {
                int shield = mbsvh.value + hpDiff;
                if (hpDiff > 0 && shield < 0 && (mbsvh.value > 0 || Math.abs(mbsvh.value) < hpDiff)) {
                    shield = Integer.MAX_VALUE;
                }
                if (shield > 0) {
                    mbsvh.value = Math.min(shield, getStat().getCurrentMaxHP());
                    send(BuffPacket.giveBuff(this, effect, Collections.singletonMap(SecondaryStat.DawnShield_ExHP, effect.getSourceId())));
                } else {
                    dispelEffect(effect.getSourceId());
                }
            }
        }
    }

    /**
     * Updates a single stat of this MapleCharacter for the Client. This method
     * only creates and sends an update packet, it does not update the stat
     * stored in this MapleCharacter instance.
     *
     * @param stat
     * @param newval
     */
    public void updateSingleStat(MapleStat stat, long newval) {
        updateSingleStat(stat, newval, true);
    }

    /**
     * Updates a single stat of this MapleCharacter for the Client. This method
     * only creates and sends an update packet, it does not update the stat
     * stored in this MapleCharacter instance.
     *
     * @param stat
     * @param newval
     * @param itemReaction
     */
    public void updateSingleStat(MapleStat stat, long newval, boolean itemReaction) {
        client.announce(MaplePacketCreator.updatePlayerStats(Collections.singletonMap(stat, newval), itemReaction, this));
    }

    public void gainFieldExp(long total, boolean bOnQuest) {
        gainExp(total, true, bOnQuest, true);
        send(EffectPacket.showFieldExpItemConsumed((int) total));
    }

    public void gainExp(long total, boolean show, boolean bOnQuest, boolean white) {
        if (ServerConfig.WORLD_BANGAINEXP) {
            dropMessage(6, "管理員禁止了經驗獲取。");
            return;
        }
        if (eventInstance != null && eventInstance.isPractice()) {
            return;
        }
        long needed = getExpNeededForLevel();
        if (level >= GameConstants.MAX_LEVEL) {
            setExp(0);
        } else {
            exp.addAndGet(total);
            if (this.exp.get() >= needed && this.getLevel() < GameConstants.MAX_LEVEL) {
                this.levelUp(true);
                needed = this.getExpNeededForLevel();
            }
            if (level >= GameConstants.MAX_LEVEL) {
                setExp(0);
            } else if (this.exp.get() >= needed) {
                setExp(needed - 1);
            }
        }
        if (total != 0L && this.exp.get() < 0L) {
            if (total > 0L) {
                this.exp.set(needed - 1);
            } else {
                this.exp.set(0);
            }
        }
        updateSingleStat(MapleStat.經驗, getExp());
        if (show && client != null) { // still show the expgain even if it's not there
            client.announce(MaplePacketCreator.showGainExp(total, white, bOnQuest, 0, 0, Collections.emptyMap()));
        }
    }

    public void monsterMultiKill() {
        int multiKill = Math.min(10, getCheatTracker().getMultiKill());
        if (multiKill > 2 && killMonsterExp.get() != 0) {
            float rate;
            switch (multiKill) {
                case 3:
                    rate = 0.01f;
                    break;
                case 4:
                    rate = 0.02f;
                    break;
                case 5:
                    rate = 0.03f;
                    break;
                case 6:
                    rate = 0.033f;
                    break;
                case 7:
                    rate = 0.036f;
                    break;
                case 8:
                    rate = 0.039f;
                    break;
                case 9:
                    rate = 0.042f;
                    break;
                default:
                    if (multiKill >= 10) {
                        rate = 0.045f;
                    } else {
                        rate = 0.0f;
                    }
                    break;
            }
            long multiKillExp = (long) (killMonsterExp.get() * rate);
            if (getRuneUseCooldown() <= 0) {
                int curseRate = 100 - map.getRuneCurseRate();
                multiKillExp *= curseRate / 100.0f;
            }
            gainExp(multiKillExp, false, false, false);

            MessageOption option = new MessageOption();
            option.setMode(0);
            option.setObjectId(getCheatTracker().getLastKillMobOid());
            option.setLongExp(multiKillExp);//多重擊殺額外經驗
            option.setCombo(multiKill);
            int color = 0;
            switch (World.getHoliday()) {
                case ChineseNewYear -> color = 6;
                case Halloween -> color = 1;
            }
            option.setColor(color);
            getClient().announce(CWvsContext.sendMessage(MessageOpcode.MS_StylishKillMessage, option));
        }
        killMonsterExp.set(0);
        if (multiKill == 0 || stopComboKill) {
            return;
        }
        final int combo = getCheatTracker().gainMonsterCombo();
        if (combo > 1) {
            MessageOption option = new MessageOption();
            option.setMode(1);
            option.setCombo(combo);
            option.setObjectId(getCheatTracker().getLastKillMobOid());
            int color = 0;
            switch (World.getHoliday()) {
                case ChineseNewYear -> color = 6;
                case Halloween -> color = 1;
            }
            option.setColor(color);
            getClient().announce(CWvsContext.sendMessage(MessageOpcode.MS_StylishKillMessage, option));
        }
    }

    public void dropComboKillBall(Point pos) {
        if (pos == null) {
            return;
        }
        final int combo = getCheatTracker().getMonsterCombo() + 1;
        int comboKillDrop;
        boolean candy = false;
        if (combo < 300) {
            comboKillDrop = candy ? 2023650 : 2023484; // 連續擊殺模式 - 咖啡色巧克力球 : 藍色
        } else if (combo < 700) {
            comboKillDrop = candy ? 2023651 : 2023494; // 連續擊殺模式 - 黃色巧克力球 : 紫色
        } else if (combo < 2000) {
            comboKillDrop = candy ? 2023652 : 2023495; // 連續擊殺模式 - 粉紅色巧克力球 : 紅色
        } else {
            comboKillDrop = 2023669; // Combo kill 遊行 - 金色
        }
        comboKillDrop = combo % 50 == 0 ? comboKillDrop : 0;
        if (comboKillDrop > 0 && getMap() != null) {
            Item dropItem = new Item(comboKillDrop, (byte) 0, (short) 1);
            final MapleMapItem mdrop = new MapleMapItem(dropItem, pos, this, this, (byte) 0, true);
            mdrop.setCollisionPickUp(true);
            mdrop.setOnlySelfID(getId());
            mdrop.setEnterType((byte) 1);
            mdrop.setDelay(0);
            getMap().spawnMobDrop(mdrop, null, this);
            mdrop.setEnterType((byte) 2);
        }
    }

    public void forceReAddItem_NoUpdate(Item item, MapleInventoryType type) {
        getInventory(type).removeSlot(item.getPosition());
        getInventory(type).addFromDB(item);
    }

    public void forceReAddItem(Item item) {
        forceReAddItem(item, ItemConstants.getInventoryType(item.getItemId(), false) == MapleInventoryType.EQUIP && item.getPosition() < 0 ? MapleInventoryType.EQUIPPED : ItemConstants.getInventoryType(item.getItemId()));
    }

    public void forceReAddItem(Item item, MapleInventoryType type) {
        forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            client.announce(InventoryPacket.modifyInventory(false, Collections.singletonList(new ModifyInventory(0, item)))); //發送獲得道具的封包
        }
    }

    public void forceUpdateItem(Item item) {
        forceUpdateItem(item, false);
    }

    public void petUpdateStats(MaplePet pet, boolean active) {
        List<ModifyInventory> mods = new LinkedList<>();
        Item Pet = getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition());
        if (Pet == null) {
            log.error("PetItem is null! inventorypos:" + pet.getInventoryPosition() + "chr:" + getName());
            return; // todo 重構PetItem
        }
        mods.add(new ModifyInventory(3, Pet));
        mods.add(new ModifyInventory(0, Pet));
        client.announce(InventoryPacket.modifyInventory(false, mods, this, active));
    }

    public void forceUpdateItem(Item item, boolean updateTick) {
        List<ModifyInventory> mods = new LinkedList<>();
        mods.add(new ModifyInventory(3, item)); //刪除道具
        mods.add(new ModifyInventory(0, item)); //獲得道具
        client.announce(InventoryPacket.modifyInventory(updateTick, mods, this));
    }

    public boolean isIntern() {
        return client.isIntern();
    }

    public boolean isGm() {
        return client.isGm();
    }

    public boolean isSuperGm() {
        return client.isSuperGm();
    }

    public boolean isAdmin() {
        return client.isAdmin();
    }

    public int getGmLevel() {
        return client.getGmLevel();
    }

    public void setGmLevel(int level) {
        if (getGmLevel() == level) {
            return;
        }
        client.setGmLevel(level);
        client.updateGmLevel();
    }

    public boolean hasGmLevel(int level) {
        return client.getGmLevel() >= level;
    }

    public boolean isDebug() {
        return isAdmin() && Config.isDevelop();
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public MapleInventory getInventory(byte type) {
        return inventory[MapleInventoryType.getByType(type).ordinal()];
    }

    public MapleInventory[] getInventorys() {
        return inventory;
    }

    /*
     * 檢測角色道具是否到期的處理
     */
    public boolean canExpiration(long now) {
        boolean b = lastExpirationTime > 0 && lastExpirationTime + (60 * 1000) < now;
//        System.out.println(lastExpirationTime + ", " + now + ", " + b);
        return b; //1分鐘檢測
    }

    public void expirationTask(boolean logout) {
        /*
         * 檢測道具到期
         */
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT)); //項鏈擴充任務
        long expiration;
        List<Integer> ret = new ArrayList<>();
        long currenttimes = System.currentTimeMillis();
        List<Triple<MapleInventoryType, Item, Boolean>> tobeRemoveItem = new ArrayList<>(); // 要刪除道具的數組 道具類型 道具 是否刪除.
        List<Item> tobeUnlockItem = new ArrayList<>(); // 解除道具鎖定的數組.
        MapleShopFactory factory = MapleShopFactory.getInstance();
//        List<Item> cheatItems = new ArrayList<>();
        rLCheck.lock();
        try {
            for (MapleInventoryType inv : MapleInventoryType.values()) {
                MapleInventory inventory = getInventory(inv);
                for (Item item : inventory) {
                    if (ServerConfig.CHEAT_ITEM_EXCLUDES_LIST.contains(item.getItemId())) {
                        continue;
                    }
                    //檢測異常道具
//                    String gmlog = item.getGMLog();
//                    Matcher matcher = MapleShop.LOG_PATTERN.matcher(gmlog);
//                    if (matcher.matches()) {
//                        String sId = matcher.group(1);
//                        if (StringUtil.isNumber(sId)) {
//                            int id = Integer.parseInt(sId);
//                            MapleShop shop = factory.getShop(id);
////                            dropMessage(5, id + item.getGMLog());
//                            if (shop == null || Client.client != null && shop.getItems(Client.client).parallelStream().noneMatch(it -> it.getItemId() == item.getItemId())) {
//                                cheatItems.add(item);
//                            }
//                        }
//                    }

                    expiration = item.getExpiration();
                    if ((expiration != -1 && !類型.寵物(item.getItemId()) && currenttimes > expiration) || (ii.isLogoutExpire(item.getItemId()) && logout)) {
                        if (ItemAttribute.Seal.check(item.getAttribute())) {
                            tobeUnlockItem.add(item); //添加解除鎖定的道具信息
                        } else if (!ii.isNickSkillTimeLimited(item.getItemId()) && currenttimes > expiration) {
                            if ((item instanceof Equip) && ((Equip) item).isMvpEquip()) { // Mvp裝備
                                item.setExpiration(-1);
                                ((Equip) item).setEnchantBuff((short) (((Equip) item).getEnchantBuff() | EnhanceResultType.EQUIP_MARK.getValue()));
                                forceUpdateItem(item);
                            } else {
                                tobeRemoveItem.add(new Triple<>(inv, item, true)); //添加刪除的道具信息
                            }
                        }
                    } else if (類型.寵物(item.getItemId())) { //寵物道具信息
                        if (ii.getLimitedLife(item.getItemId()) > 0) {
                            if (item.getPet() != null && item.getPet().getSecondsLeft() <= 0) {
                                tobeRemoveItem.add(new Triple<>(inv, item, true));
                            }
                        } else if (expiration >= 0 && currenttimes > expiration && item.getPet() != null) {
                            tobeRemoveItem.add(new Triple<>(inv, item, false));
                        }
                    } else if (item.getPosition() == -36) { //盛大項鏈擴充 T072修改 以前為 -37
                        if (stat == null || stat.getCustomData() == null || (!"0".equals(stat.getCustomData()) && Long.parseLong(stat.getCustomData()) < currenttimes)) { //項鏈擴充檢測
                            tobeRemoveItem.add(new Triple<>(inv, item, false));
                        }
                    }
                }
            }
        } finally {
            rLCheck.unlock();
        }
//        // 作弊道具
//        if (cheatItems.size() > 0) {
//            StringBuilder sb = new StringBuilder();
//            sb.append("檢測到你背包裡有作弊道具，將對你進行封號處理，如有疑問請與管理員聯繫。檢測到的作弊道具如下：\r\n");
//            for (Item cheatItem : cheatItems) {
//                sb.append(cheatItem.toString()).append("\r\n");
//            }
//            log.info("檢測到作弊道具：角色：" + name + " 詳情：" + sb.toString());
////            dropAlertNotice(sb.toString());
//            Client.announce(NPCPacket.OnSay((byte) 3, 0, false, 0, 0, (short) 0x10, false, false, sb.toString(), 0));
//            Timer.CheatTimer.getInstance().schedule(() -> ban("作弊道具檢測", false, true, false), 10000);
//        }

        /*
         * Left = 左邊
         * Mid = 中間
         * Right = 右邊
         */
        for (Triple<MapleInventoryType, Item, Boolean> itemz : tobeRemoveItem) {
            Item item = itemz.getMid();
            if (item == null) {
                log.error("道具到期 " + getName() + " 檢測道具已經過期，但道具為空，無法繼續執行。");
                continue;
            }
            if (itemz.getRight()) { //刪除道具
                if (MapleInventoryManipulator.removeFromSlot(client, itemz.getLeft(), item.getPosition(), item.getQuantity(), false)) {
                    ret.add(item.getItemId());
                }
                if (itemz.getLeft() == MapleInventoryType.EQUIPPED) {
                    equipChanged();
                }
            } else if (類型.寵物(item.getItemId())) {
                unequipSpawnPet(item.getPet(), true, (byte) 2);
            } else if (item.getPosition() == -36) { //盛大項鏈擴充 T072修改 以前為 -37
                short slot = getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                if (slot > -1) {
                    MapleInventoryManipulator.unequip(client, item.getPosition(), slot);
                }
            }
        }
        for (Item itemz : tobeUnlockItem) {
            itemz.setExpiration(-1);
            itemz.removeAttribute(ItemAttribute.Seal.getValue());
            forceUpdateItem(itemz);
            //dropMessage(6, "封印道具[" + ii.getName(itemz.getItem()) + "]封印時間已過期。");
        }
        this.pendingExpiration = ret;
        /*
         * 技能到期
         */
        List<Integer> tobeRemoveSkill = new ArrayList<>();
        Map<Integer, SkillEntry> tobeRemoveList = new HashMap<>();
        for (Entry<Integer, SkillEntry> skil : skills.entrySet()) {
            if (skil.getValue().expiration != -1 && currenttimes > skil.getValue().expiration && SkillConstants.getTeamTeachSkills(skil.getKey()) == null && !SkillConstants.isLinkSkills(skil.getKey()) && !SkillConstants.isTeachSkills(skil.getKey())) {
                tobeRemoveSkill.add(skil.getKey());
            }
        }
        for (Integer skil : tobeRemoveSkill) {
            tobeRemoveList.put(skil, new SkillEntry(0, (byte) 0, -1));
            this.skills.remove(skil);
            changed_skills = true;
        }
        this.pendingSkills = tobeRemoveList;
        if (stat != null && stat.getCustomData() != null && (!"0".equals(stat.getCustomData()) && Long.parseLong(stat.getCustomData()) < currenttimes)) { //expired bro
            quests.remove(7830);
            quests.remove(GameConstants.PENDANT_SLOT);
        }
        /*
         * 檢測項鏈佩戴位置錯誤的設置
         */
        Item itemFix = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -37); //以前的項鏈擴充 T072修改到 -38的位置
        if (itemFix != null && itemFix.getItemId() / 10000 != 119) {
            short slot = getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
            if (slot > -1) {
                MapleInventoryManipulator.unequip(client, itemFix.getPosition(), slot);
                dropMessage(5, "裝備道具[" + ii.getName(itemFix.getItemId()) + "]由於裝備的位置錯誤已自動取下。");
            }
        }
        /*
         * 檢測裝備痕跡位置錯誤的設置
         */
        List<Item> equipedItems = new LinkedList<>(getInventory(MapleInventoryType.EQUIPPED).getInventory().values());
        for (Item equiped : equipedItems) {
            if (!(equiped instanceof Equip) || !EnhanceResultType.EQUIP_MARK.check(((Equip) equiped).getEnchantBuff())) {
                continue;
            }
            short slot = getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
            if (slot > -1) {
                MapleInventoryManipulator.unequip(client, equiped.getPosition(), slot);
                forceUpdateItem(equiped);
            }
        }
        equipedItems.clear();
        equipedItems = null;
        /*
         * 發送到期封包
         */
        if (pendingExpiration != null) {
            if (!pendingExpiration.isEmpty()) {
                for (Integer itemId : pendingExpiration) { //發送道具到期封包
                    if (ii.isCash(itemId)) {
                        client.announce(MaplePacketCreator.showCashItemExpired(itemId));
                    } else {
                        client.announce(MaplePacketCreator.showItemExpired(itemId));
                    }
                }
            }
        }
        pendingExpiration = null;
        /*
         * 發送技能到期封包
         */
        if (pendingSkills != null) {
            if (!pendingSkills.isEmpty()) {
                client.announce(MaplePacketCreator.updateSkills(pendingSkills)); //發送刪除技能的封包
                client.announce(MaplePacketCreator.showSkillExpired(pendingSkills)); //發送技能到期提示
            }
            pendingSkills = null;
        }

        updateReward();

        lastExpirationTime = System.currentTimeMillis();
    }

    public MapleShop getShop() {
        return shop;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public boolean isScriptShop() {
        return this.scriptShop;
    }

    public void setScriptShop(boolean scriptShop) {
        this.scriptShop = scriptShop;
    }

    /*
     * 角色傳送石處理
     */
    public int[] getSavedLocations() {
        return savedLocations;
    }

    public int getSavedLocation(SavedLocationType type) {
        return type.getValue() < 0 || type.getValue() >= savedLocations.length ? 0 : savedLocations[type.getValue()];
    }

    public void saveLocation(SavedLocationType type) {
        if (type.getValue() < 0 || type.getValue() >= savedLocations.length) {
            return;
        }
        savedLocations[type.getValue()] = getMapId();
        changed_savedlocations = true;
    }

    public void saveLocation(SavedLocationType type, int mapz) {
        if (type.getValue() < 0 || type.getValue() >= savedLocations.length) {
            return;
        }
        savedLocations[type.getValue()] = mapz;
        changed_savedlocations = true;
    }

    public void clearSavedLocation(SavedLocationType type) {
        if (type.getValue() < 0 || type.getValue() >= savedLocations.length) {
            return;
        }
        savedLocations[type.getValue()] = -1;
        changed_savedlocations = true;
    }

    public int getCMapCount() {
        return cMapCount.getAndIncrement();
    }

    /*
     * V.110修改楓幣上限
     * 暫時不作修改
     */
    public long getMeso() {
        return meso.get();
    }

    public void gainMeso(long gain, boolean show) {
        gainMeso(gain, show, false);
    }

    public void gainMeso(long gain, boolean show, boolean inChat) {
        gainMeso(gain, show, inChat, true);
    }

    public void gainMeso(long gain, boolean show, boolean inChat, boolean enableAction) {
        if (meso.get() + gain < 0) {
            client.sendEnableActions();
            return;
        } else if (meso.get() + gain > ServerConfig.CHANNEL_PLAYER_MAXMESO) {
            gain = ServerConfig.CHANNEL_PLAYER_MAXMESO - meso.get();
        }
        meso.addAndGet(gain);
        updateSingleStat(MapleStat.楓幣, meso.get(), false);
        if (enableAction) {
            client.sendEnableActions();
        }
        if (show) {
            client.announce(MaplePacketCreator.showMesoGain(gain, inChat));
        }
        playerObservable.update();
    }

    public int getAccountID() {
        return accountid;
    }

    /*
     * 怪物仇人目標處理
     */
    public int getMobControlledSize() {
        return controlledMonsters.size();
    }

    public final boolean isControlMonster(final MapleMonster monster) {
        this.controlMonsterLock.lock();
        try {
            return this.controlledMonsters.contains(monster);
        } finally {
            this.controlMonsterLock.unlock();
        }
    }

    public void controlMonster(MapleMonster monster) {
        controlMonsterLock.lock();
        try {
            controlledMonsters.add(monster);
        } finally {
            controlMonsterLock.unlock();
        }
    }

    public void controlMonsterRemove(MapleMonster monster) {
        controlMonsterLock.lock();
        try {
            controlledMonsters.remove(monster);
        } finally {
            controlMonsterLock.unlock();
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (monster == null) {
            return;
        }
        if (monster.getController() == this) {
            monster.setControllerHasAggro(true);
        } else {
            monster.switchController(this);
        }
    }

    /*
     * 任務相關操作
     */
    public List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 1 && !q.isCustom() && !q.getQuest().isBlocked()) {
                ret.add(q);
            }
        }
        return ret;
    }

    public List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 2 && !q.isCustom() && !q.getQuest().isBlocked()) {
                ret.add(q);
            }
        }
        return ret;
    }

    public List<Pair<Integer, Long>> getCompletedMedals() {
        List<Pair<Integer, Long>> ret = new ArrayList<>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 2 && !q.isCustom() && !q.getQuest().isBlocked() && q.getQuest().getMedalItem() > 0 && ItemConstants.getInventoryType(q.getQuest().getMedalItem(), false) == MapleInventoryType.EQUIP) {
                ret.add(new Pair<>(q.getQuest().getId(), q.getCompletionTime()));
            }
        }
        return ret;
    }

    public void mobKilled(int id, int skillID) {
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() != 1 || !q.hasMobKills() || (q.isWorldShare() && (this.id != q.getFromChrID() || q.getFromChrID() == -2))) {
                continue;
            }
            if (q.mobKilled(id, skillID)) {
                int i = -1;
                for (int kills : q.getMobKills().values()) {
                    i++;
                    if (q.isWorldShare()) {
                        updateWorldShareInfo(q.getQuest().getId(), "m" + i, String.valueOf(kills));
                    } else {
                        updateOneQuestInfo(q.getQuest().getId(), "m" + i, String.valueOf(kills));
                    }
                }
                client.announce(MaplePacketCreator.updateQuestMobKills(q));
                if (q.getQuest().canComplete(this, null)) {
                    //client.write(GoingHandler.getShowQuestCompletion(q.getQuest().getId()));
                }
            }
        }
    }

    /**
     * 技能相關的操作
     */
    public Map<Integer, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public Map<Integer, SkillEntry> getLinkSkills() {
        return Collections.unmodifiableMap(linkSkills);
    }

    public Map<Integer, Pair<Integer, SkillEntry>> getSonOfLinkedSkills() {
        return Collections.unmodifiableMap(sonOfLinkedSkills);
    }

    /*
     * 獲取角色所有技能
     * 如果不是返回所有技能
     * 如果是封包發送就返回刪除有些需要過濾的技能
     */
    public Map<Integer, SkillEntry> getSkills(boolean packet) {
        if (!packet) {
            return Collections.unmodifiableMap(skills);
        }
        Map<Integer, SkillEntry> oldlist = new LinkedHashMap<>(skills);
        Map<Integer, SkillEntry> newlist = new LinkedHashMap<>();
        for (Entry<Integer, SkillEntry> skill : oldlist.entrySet()) {
            //去掉天使技能和特殊連接技能
            Skill skill1 = SkillFactory.getSkill(skill.getKey());
            if (skill1 != null && !skill1.isAngelSkill() && !skill1.isLinkedAttackSkill() && !skill1.isDefaultSkill()) {
                newlist.put(skill.getKey(), skill.getValue());
            }
        }
        return newlist;
    }

    public int getAllSkillLevels() {
        int rett = 0;
        for (Entry<Integer, SkillEntry> ret : skills.entrySet()) {
            Skill skill = SkillFactory.getSkill(ret.getKey());
            if (!skill.isBeginnerSkill() && !skill.isSpecialSkill() && ret.getValue().skillevel > 0) {
                rett += ret.getValue().skillevel;
            }
        }
        return rett;
    }

    public long getSkillExpiry(Skill skill) {
        if (skill == null) {
            return 0;
        }
        SkillEntry ret = skills.get(skill.getId());
        if (ret == null || ret.skillevel <= 0) {
            return 0;
        }
        return ret.expiration;
    }

    /*
     * 通過技能ID獲取技能等級
     */
    public int getSkillLevel(int skillid) {
        return getSkillLevel(SkillFactory.getSkill(skillid));
    }

    public int getSkillLevel(Skill skill) {
        if (skill == null) {
            return -1;
        }
        if (SkillConstants.isGeneralSkill(skill.getId()) || SkillConstants.isExtraSkill(skill.getId()) || skill.isSoulSkill() || SkillConstants.isRuneSkill(skill.getId())) {
            return 1;
        }
        int id = skill.getId();
        int skillLevel = 0;
        int root = SkillConstants.getSkillRoot(id);
        if (getJob() >= root && getJob() < root + 3) {
            for (Pair<String, Byte> require : skill.getRequiredSkills()) {
                switch (require.getLeft()) {
                    case "level": {
                        if (level < require.getRight()) {
                            return 0;
                        }
                        continue;
                    }
                    case "reqAmount": {
                        continue;
                    }
                    default: {
                        if (getSkillLevel(Integer.parseInt(require.getLeft())) < require.getRight()) {
                            return 0;
                        }
                    }
                }
            }
        }
        switch (id) {
            case 陰陽師.雙天狗_隱藏:
                return getSkillLevel(陰陽師.雙天狗) > 0 ? 1 : 0;
            case 80001089:
            case 80001242:
            case 初心者.升級:
            case 80011133: {
                return 1;
            }
            default: {
                final int n2 = id / 10000;
                final int n3 = id % 10000;
                if (id != 龍魔導士.龍神之怒 && !SkillConstants.ej(id) && JobConstants.isSameJob(n2, this.getJob())) {
                    switch (n3) {
                        case 1092:
                        case 1093:
                        case 1094:
                        case 1095: {
                            return 1;
                        }
                    }
                }
                skillLevel += stats.getSkillIncrement(skill.getId());
                skillLevel += stats.getEquipmentSkill(skill.getId());
                int[] tSkills = SkillConstants.getTeamTeachSkills(id);
                if (tSkills != null) {
                    for (int tID : tSkills) {
                        skillLevel += getSkillLevel(SkillConstants.getTeachSkillId(tID));
                    }
                }
                if (skillLevel <= 0 && skill.getFixLevel() > 0) {
                    skillLevel = skill.getFixLevel();
                } else {
                    SkillEntry ret = this.skills.get(id);
                    if (ret == null || ret.skillevel <= 0) {
                        return skillLevel;
                    }
                    skillLevel += ret.skillevel;
                    if (id / 10000 >= 9200 && id / 10000 <= 9204 || skill.isRecipe()) {
                        return skillLevel;
                    }
                    if (id % 10000 < 1000 && stats.getPassivePlus() > 0) {
                        skillLevel += Math.min(2, stats.getPassivePlus());
                    }
                    skillLevel = Math.min(skill.getTrueMax(), skillLevel + ((JobConstants.notNeedSPSkill(SkillConstants.getSkillRoot(id)) || skill.isVSkill()) ? 0 : (this.stats.combatOrders + ((skill.getMaxLevel() > 10 && (this.stats.incAllskill + skillLevel <= skill.getMaxLevel())) ? this.stats.incAllskill : 0))));
                    if (skillLevel > skill.getMaxLevel() && this.getSummonedFamiliar() == null && this.stats.combatOrders <= 0 && !skill.isVSkill()) {
                        skillLevel = skill.getMaxLevel();
                    }
                }
                return skillLevel;
            }
        }

    }

    /*
     * 通過技能ID獲取技能等級
     */
    public int getTotalSkillLevel(int skillid) {
        return getTotalSkillLevel(SkillFactory.getSkill(skillid));
    }

    public int getTotalSkillLevel(Skill skill) {
        if (true) {
            return getSkillLevel(skill);
        }
        if (skill == null) {
            return 0;
        }
        if (SkillConstants.isGeneralSkill(skill.getId()) || SkillConstants.isExtraSkill(skill.getId()) || skill.isSoulSkill() || SkillConstants.isRuneSkill(skill.getId())) {
            return 1;
        }
        switch (skill.getId()) {
            case 精靈遊俠.元素精靈_1:
            case 精靈遊俠.元素精靈_2: {
                return this.getTotalSkillLevel(精靈遊俠.元素精靈);
            }
            case 精靈遊俠.西皮迪亞_1:
            case 精靈遊俠.西皮迪亞_2:
                return this.getTotalSkillLevel(精靈遊俠.西皮迪亞);
            case 凱撒.超新星守護者_1:
            case 凱撒.超新星守護者_2: {
                return this.getTotalSkillLevel(凱撒.超新星守護者);
            }
        }
        int skillLevel;
        if (getJob() >= skill.getId() / 10000 && getJob() < skill.getId() / 10000 + 3) {
            skillLevel = skill.getFixLevel();
        } else {
            skillLevel = 0;
        }
        SkillEntry ret = this.skills.get(skill.getId());
        if (skill.isLinkSkills()) {
            ret = linkSkills.get(skill.getId());
        }
        if (ret == null || ret.skillevel <= 0) {
            return skillLevel;
        } else {
            skillLevel += ret.skillevel;
        }
        return Math.min(skill.getTrueMax(), skillLevel + ((skill.isBeginnerSkill() || skill.isVSkill()) ? 0 : (stats.combatOrders + (skill.getMaxLevel() > 10 ? stats.incAllskill : 0) + stats.getSkillIncrement(skill.getId()))));
    }

    public int getMasterLevel(int skillId) {
        return getMasterLevel(SkillFactory.getSkill(skillId));
    }

    public int getMasterLevel(Skill skill) {
        SkillEntry ret = skills.get(skill.getId());
        if (ret == null) {
            return 0;
        }
        return ret.masterlevel;
    }

    public int getSkillTeachId(int skillId) {
        return getSkillTeachId(SkillFactory.getSkill(skillId));
    }

    public int getSkillTeachId(Skill skill) {
        if (skill == null) {
            return 0;
        }
        SkillEntry ret = skills.get(skill.getId());
        if (ret == null || ret.teachId == 0) {
            return 0;
        }
        return ret.teachId;
    }

    public int getSkillTeachTimes(Skill skill) {
        if (skill == null) {
            return 0;
        }
        SkillEntry ret = skills.get(skill.getId());
        if (ret == null || ret.teachTimes == 0) {
            return 0;
        }
        return ret.teachTimes;
    }

    public byte getSkillPosition(int skillId) {
        return getSkillPosition(SkillFactory.getSkill(skillId));
    }

    public byte getSkillPosition(Skill skill) {
        if (skill == null) {
            return -1;
        }
        SkillEntry ret = skills.get(skill.getId());
        if (ret == null || ret.position == -1) {
            return -1;
        }
        return ret.position;
    }

    public SkillEntry getSkillEntry(int skillId) {
        return skills.get(skillId);
    }

    /*
     * 角色升級處理
     */
    public void levelUp() {
        levelUp(false);
    }

    /**
     * 角色升級處理
     *
     * @param canBurning 是否能計算燃燒
     */
    public void levelUp(boolean canBurning) {
        final int job = this.job; //創建棧變量加快訪問速度
        if (!JobConstants.is管理員(job)) {
            remainingAp += 5; //升級獲得5AP
        }

        //開始處理升級加HP/MP
        int maxhp = stats.getMaxHp(false);
        int maxmp = stats.getMaxMp(false);
        if (JobConstants.is零轉職業(job)) { // 新手
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(10, 12);
        } else if (JobConstants.is惡魔殺手(job) || JobConstants.is凱內西斯(job)) { // 惡魔獵手
            maxhp += Randomizer.rand(48, 52);
        } else if (JobConstants.is惡魔復仇者(job) || JobConstants.is陰陽師(job)) { // 惡魔復仇者
            maxhp += Randomizer.rand(30, 40);
        } else if ((job >= 100 && job <= 132) // 劍士
                || (job >= 1100 && job <= 1112) // 魂騎士
                || (job >= 5100 && job <= 5112) // 米哈爾
                || JobConstants.is皮卡啾(job)) { //皮卡啾
            maxhp += Randomizer.rand(48, 52);
            maxmp += Randomizer.rand(4, 6);
        } else if ((job >= 200 && job <= 232) // 魔法師
                || (job >= 1200 && job <= 1212) // 炎術士
                || (job >= 2700 && job <= 2712)) { //夜光法師
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(48, 52);
        } else if (job >= 3200 && job <= 3212) { //幻靈斗師
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(42, 44);
        } else if ((job >= 300 && job <= 322) // 弓箭手
                || (job >= 400 && job <= 434) // 盜賊
                || (job >= 1300 && job <= 1312) //風靈使者
                || (job >= 1400 && job <= 1412) //夜行者
                || (job >= 2300 && job <= 2312) //雙弩精靈
                || (job >= 2400 && job <= 2412) //幻影
                || (job >= 3300 && job <= 3312) //狂豹獵人
                || (job >= 3600 && job <= 3612)) { //尖兵
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(14, 16);
        } else if ((job >= 510 && job <= 512) // 拳手
                || (job >= 580 && job <= 582) // 拳手 - 新
                || (job >= 1510 && job <= 1512) //奇襲者
                || (job >= 6500 && job <= 6512)) { //天使破壞者
            maxhp += Randomizer.rand(37, 41);
            maxmp += Randomizer.rand(18, 22);
        } else if ((job >= 500 && job <= 532) //火炮手
                || (job >= 590 && job <= 592) //火炮手 - 新
                || (job >= 3500 && job <= 3512) //機械師
                || job == 1500) { // 奇襲者
            maxhp += Randomizer.rand(22, 26);
            maxmp += Randomizer.rand(18, 22);
        } else if (job >= 2100 && job <= 2112) { // 狂狼勇士
            maxhp += Randomizer.rand(50, 52);
            maxmp += Randomizer.rand(4, 6);
        } else if (JobConstants.is龍魔導士(job)) { // 龍神
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(50, 52);
        } else if (job >= 6100 && job <= 6112) { // 凱撒
            maxhp += Randomizer.rand(68, 74);
            maxmp += Randomizer.rand(4, 6);
        } else if (job >= 10100 && job <= 10112) { // 神之子
            maxhp += Randomizer.rand(48, 52);
        } else if (JobConstants.is隱月(job) || JobConstants.is劍豪(job)) {
            maxhp += Randomizer.rand(38, 42);
            maxmp += Randomizer.rand(20, 24);
        } else if (JobConstants.is爆拳槍神(job)) {
            maxhp += Randomizer.rand(48, 75);
            maxmp += Randomizer.rand(8, 18);
        } else if (JobConstants.is伊利恩(job)) {
            maxhp += Randomizer.rand(20, 34);
            maxmp += Randomizer.rand(18, 22);
        } else if (JobConstants.is卡蒂娜(job)) {
            maxhp += Randomizer.rand(30, 44);
            maxmp += Randomizer.rand(8, 18);
        } else if (JobConstants.is亞克(job)) {
            maxhp += Randomizer.rand(30, 44);
            maxmp += Randomizer.rand(8, 18);
        } else if (JobConstants.is虎影(job)) {
            maxhp += Randomizer.rand(30, 44);
            maxmp += Randomizer.rand(8, 18);
        } else if (JobConstants.is卡莉(job)) {
            maxhp += Randomizer.rand(30, 44);
            maxmp += Randomizer.rand(8, 18);
        } else if (JobConstants.is琳恩(job)) {
            maxhp += Randomizer.rand(30, 44);
            maxmp += Randomizer.rand(8, 18);
        } else { // 默認沒有寫的職業加血
            maxhp += Randomizer.rand(24, 38);
            maxmp += Randomizer.rand(12, 24);
            if (job != 800 && job != 900 && job != 910) {
                System.err.println("出現尚未建立加血數據的角色,職業代號是: " + job);
            }
        }
        maxmp += JobConstants.isNotMpJob(getJob()) ? 0 : stats.getTotalInt() / 10;//10智力+1MP
        if (JobConstants.is夜光(job) && getSkillLevel(夜光.光明力量) > 0) {
            maxmp += 10;
        }
        maxhp = Math.min(ServerConfig.CHANNEL_PLAYER_MAXHP, Math.abs(maxhp));
        maxmp = Math.min(ServerConfig.CHANNEL_PLAYER_MAXMP, Math.abs(maxmp));
        if (JobConstants.is惡魔殺手(job)) {
            maxmp = GameConstants.getMPByJob(job);
        } else if (JobConstants.is神之子(job)) {
            maxmp = 100;
            checkZeroWeapon();
        } else if (JobConstants.is陰陽師(job)) {
            maxmp = 100;
        } else if (JobConstants.isNotMpJob(job)) {
            maxmp = 10;
        }
        exp.addAndGet(-getExpNeededForLevel());
        if (exp.get() < 0) {
            exp.set(0);
        }
        level += 1;
        if (level >= GameConstants.MAX_LEVEL) {
            setExp(0);
        }
        // 修正到達140等時沒有任何分頁的問題
        if (level >= 140 && getQuestInfo(2498, "hyperstats") == null) {
            updateInfoQuest(2498, "hyperstats=0", true);
            client.announce(MaplePacketCreator.updateHyperPresets(this, 0, (byte) 0));
        }
        Map<MapleStat, Long> statup = new EnumMap<>(MapleStat.class);
        if (!JobConstants.is皮卡啾(job) && !JobConstants.is雪吉拉(job) && !JobConstants.is管理員(job)) {
            if (!JobConstants.is零轉職業(job)) {
                int spNum = MapleCharacter.getJobLvSP(subcategory == 1 ? 430 : job, level);
                if (JobConstants.is神之子(job)) {
                    remainingSp[0] += spNum;
                    remainingSp[1] += spNum;
                } else if (level > 10) {
                    remainingSp[JobConstants.getSkillBookByLevel(subcategory == 1 ? 430 : job, level)] += spNum;
                }
            }
            if (level <= 10) {
                stats.str += remainingAp;
                remainingAp = 0;
                statup.put(MapleStat.力量, (long) stats.getStr());
            } else if (level == 11 && JobConstants.is零轉職業(job)) {
                resetStats(4, 4, 4, 4);
                setKeyValue("Rest_AP", "True");
            }
        }
        stats.setInfo(maxhp, maxmp, stats.getCurrentMaxHP(), stats.getCurrentMaxMP());
        characterCard.recalcLocalStats(this);
        stats.recalcLocalStats(this);
        statup.put(MapleStat.MAXHP, (long) maxhp);
        statup.put(MapleStat.MAXMP, (long) maxmp);
        statup.put(MapleStat.HP, (long) stats.getCurrentMaxHP());
        statup.put(MapleStat.MP, (long) stats.getCurrentMaxMP());
        statup.put(MapleStat.經驗, exp.get());
        statup.put(MapleStat.等級, (long) level);
        statup.put(MapleStat.AVAILABLEAP, (long) remainingAp);
        statup.put(MapleStat.AVAILABLESP, (long) remainingSp[JobConstants.getSkillBookByLevel(job, level)]);
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        if (map != null) {
            map.broadcastMessage(this, EffectPacket.showForeignEffect(getId(), EffectOpcode.UserEffect_LevelUp), false);
        }
        notifyChanges();
        guildUpdate();
        baseSkills();
        changeTeachSkillsLevel();

        insertRanking("等級排行", level);
        playerObservable.update();
        if (level == 60 && getWorldShareInfo(18793, "q0") == null) {
            updateWorldShareInfo(18793, "q0", "0");
        }

        if (canBurning) {
            int minLv = 10;
            int maxLv;
            switch (burningChrType) {
                case Burning.燃燒加速器:
                    maxLv = 130;
                    break;
                case Burning.超級燃燒:
                    maxLv = 150;
                    break;
                case Burning.極限燃燒:
                    maxLv = 200;
                    break;
                case Burning.無:
                default:
                    maxLv = 0;
                    break;
            }
            if (level > minLv && level < maxLv) {
                long timeNow = System.currentTimeMillis();
                if (timeNow >= burningChrTime) {
                    burningChrType = Burning.無;
                    burningChrTime = -2;
                } else {
                    int nLvUPTimes = burningChrType == Burning.極限燃燒 ? Math.min(2, maxLv - level) : 2;
                    for (int i = 0; i < nLvUPTimes; i++) {
                        levelUp(false);
                    }
                }
            }
        } else {
            if (android != null) {
                android.showEmotion(this, "levelup");
            }
        }
    }

    public boolean canLevelUp() {
        boolean canLevelUp = true;
        String textinfo = "";
        switch (getJob()) {
            case 0:
            case 1000: // 騎士團0轉
            case 2000: // 狂狼勇士0轉
            case 2001: // 龍魔導師0轉
            case 3000: // 末日反抗者0轉
            case 2002: // 精靈遊俠0轉
            case 3001: // 惡魔殺手0轉
            case 3002: // 尖兵
            case 6000: // 凱撒
            case 6001: // 天使
            case 5000: // 米哈爾
            case 2003:
            case 2004:
            case 2005: // 隱月
            case 4001:
            case 4002:
            case 14000: //超能力者
                if (getLevel() >= 10) {
                    canLevelUp = false;
                    textinfo = "您現在可以進行第一次轉職了，在右下角點擊拍賣開始轉職吧。";
                }
                break;
            // 冒險家1轉
            case 100:
            case 200:
            case 300:
            case 500:
                // 騎士團1轉
            case 1100:
            case 1200:
            case 1300:
            case 1400:
            case 1500:
            case 2100: // 狂狼勇士1轉
            case 2200: // 龍魔導師1轉
            case 2300: // 精靈遊俠1轉
            case 3200: // 煉獄巫師1轉
            case 3300: // 狂豹獵人1轉
            case 3500: // 機甲戰神1轉
            case 3600:
            case 3700:
            case 501:  // 火炮手1轉
            case 3100: // 惡魔獵手1轉
            case 3101: // 惡魔復仇者1轉
            case 6100:
            case 6500:
            case 5100:
            case 2700:
            case 2400:
            case 2500: // 隱月1轉
            case 4100:
            case 4200:
            case 14200: //超能力者1轉
            {
                if (getLevel() >= 30) {
                    canLevelUp = false;
                    textinfo = "您現在可以進行第二次轉職了，在右下角點擊拍賣開始轉職吧。";
                }
                break;
            }
            // 冒險家2轉
            case 110:
            case 120:
            case 130:
            case 210:
            case 220:
            case 230:
            case 310:
            case 320:
            case 410:
            case 420:
            case 510:
            case 520:
                // 騎士團2轉
            case 1110:
            case 1210:
            case 1310:
            case 1410:
            case 1510:
            case 2110: // 狂狼勇士2轉
            case 2211: // 龍魔導師2轉
            case 3210: // 煉獄巫師2轉
            case 3310: // 狂豹獵人2轉
            case 3510: // 機甲戰神2轉
            case 3610:
            case 3710:
            case 530:  // 火炮手2轉
            case 2310: // 精靈遊俠2轉
            case 3110: // 惡魔殺手2轉
            case 3120:
            case 6510:
            case 6110:
            case 5110:
            case 570:
            case 2710:
            case 2410:
            case 2510: // 隱月2轉
            case 4110:
            case 4210:
            case 14210: //超能力者2轉
            {
                if (getLevel() >= 60) {
                    canLevelUp = false;
                    //textinfo = "您現在可以進行第三次轉職了，去冰原雪域的長老公館找到三轉教官進行轉職吧。";
                    textinfo = "您現在可以進行第三次轉職了，在右下角點擊拍賣開始轉職吧。";
                }
                break;
            }
            // 冒險家3轉
            case 111:
            case 121:
            case 131:
            case 211:
            case 221:
            case 231:
            case 311:
            case 321:
            case 411:
            case 421:
            case 511:
            case 521:
                // 騎士團3轉
            case 1111:
            case 1211:
            case 1311:
            case 1411:
            case 1511:
            case 2111: // 狂狼勇士3轉
            case 2214: // 龍魔導師3轉
            case 3211: // 煉獄巫師3轉
            case 3311: // 狂豹獵人3轉
            case 3511: // 機甲戰神3轉
            case 3711:
            case 531:  // 火炮手3轉
            case 2311: // 精靈遊俠3轉
            case 3111: // 惡魔殺手3轉
            case 3121:
            case 6111:
            case 6511:
            case 5111:
            case 3611:
            case 571:
            case 2711:
            case 2411:
            case 2511: // 隱月3轉
            case 4111:
            case 4211:
            case 14211: //超能力者3轉
            {
                if (getLevel() >= 100) {
                    canLevelUp = false;
                    //textinfo = "您現在可以進行第四次轉職了，去神木村祭祀之林找到四轉教官進行轉職吧。";
                    textinfo = "您現在可以進行第四次轉職了，在右下角點擊拍賣開始轉職吧。";
                }
                break;
            }
            //雙刀
            case 400: {
                //如果又雙刀的職業群創建的話
                if (getSubcategory() == 1) {
                    if (getLevel() >= 20) {
                        canLevelUp = false;
                        textinfo = "您現在可以進行第二次轉職了，在右下角點擊拍賣開始轉職吧。";
                    }
                } else if (getLevel() >= 30) {
                    canLevelUp = false;
                    textinfo = "您現在可以進行第二次轉職了，在右下角點擊拍賣開始轉職吧。";
                }
                break;
            }
            case 430: {
                if (getLevel() >= 30) {
                    canLevelUp = false;
                    textinfo = "您現在可以進行第三次轉職了，在右下角點擊拍賣開始轉職吧。";
                }
                break;
            }
            case 431: {
                if (getLevel() >= 45) {
                    canLevelUp = false;
                    textinfo = "您現在可以進行第四次轉職了，在右下角點擊拍賣開始轉職吧。";
                }
                break;
            }
            case 432: {
                if (getLevel() >= 60) {
                    canLevelUp = false;
                    textinfo = "您現在可以進行第五次轉職了，在右下角點擊拍賣開始轉職吧。";
                }
                break;
            }
            case 433: {
                if (getLevel() >= 100) {
                    canLevelUp = false;
                    textinfo = "您現在可以進行第六次轉職了，在右下角點擊拍賣開始轉職吧。";
                }
                break;
            }
        }
        if (!canLevelUp) {
            dropMessage(5, "[轉職提示] " + textinfo); // 請點擊拍賣按鈕進行轉職。
            return false;
        }
        return true;
    }

    /*
     * 鍵盤設置處理
     */
    public void changeKeybinding(int slot, int key, byte type, int action) {
        if (funcKeyMaps.size() <= slot) {
            return;
        }
        if (type != 0) {
            funcKeyMaps.get(slot).getKeymaps().put(key, new Keymapping(type, action));
        } else {
            funcKeyMaps.get(slot).getKeymaps().remove(key);
        }
        funcKeyMaps.get(slot).setChanged(true);
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
        changed_skillmacros = true;
    }

    public SkillMacro[] getMacros() {
        return skillMacros;
    }

    /*
     * 按時間來封號
     */
    public void tempban(String reason, Calendar duration, int greason, boolean IPMac) {
        if (IPMac) {
            client.banMacs();
        }

        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps;
            if (IPMac) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, client.getSessionIPAddress());
                ps.execute();
                ps.close();
            }
            client.disconnect(true, false);
            client.getSession().close();
            ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
            Timestamp TS = new Timestamp(duration.getTimeInMillis());
            ps.setTimestamp(1, TS);
            ps.setString(2, reason);
            ps.setInt(3, greason);
            ps.setInt(4, accountid);
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            System.err.println("Error while tempbanning" + ex);
        }
    }

    /*
     * 封停帳號
     */
    public boolean ban(String reason, boolean IPMac, boolean autoban, boolean hellban) {
        gainWarning(false);

        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
            ps.setInt(1, autoban ? 2 : 1);
            ps.setString(2, reason);
            ps.setInt(3, accountid);
            ps.execute();
            ps.close();
            if (IPMac) {
                client.banMacs();
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, client.getSessionIPAddress());
                ps.execute();
                ps.close();
                if (hellban) {
                    PreparedStatement psa = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                    psa.setInt(1, accountid);
                    ResultSet rsa = psa.executeQuery();
                    if (rsa.next()) {
                        PreparedStatement pss = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE email = ? OR SessionIP = ?");
                        pss.setInt(1, autoban ? 2 : 1);
                        pss.setString(2, reason);
                        pss.setString(3, rsa.getString("email"));
                        pss.setString(4, client.getSessionIPAddress());
                        pss.execute();
                        pss.close();
                    }
                    rsa.close();
                    psa.close();
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error while banning" + ex);
            return false;
        }
//        Client.disconnect(true, false);
        client.getSession().close();
        return true;
    }

    /**
     * 對像ID，角色的對象ID總是等於CID
     *
     * @return 對像ID
     */
    @Override
    public int getObjectId() {
        return getId();
    }

    /**
     * 拋出不支持的操作異常，玩家OID為只讀
     *
     * @param id
     */
    @Override
    public void setObjectId(int id) {
        throw new UnsupportedOperationException();
    }

    public MapleTrunk getTrunk() {
        return trunk;
    }

    /**
     * 添加可見的地圖對像
     *
     * @param mo
     */
    public void addVisibleMapObjectEx(MapleMapObject mo) {
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.add(mo);
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.add(mo);
            if (client != null) {
                mo.sendSpawnData(client);
            }
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
    }

    /**
     * 移除可見的地圖對像
     *
     * @param mo
     */
    public void removeVisibleMapObjectEx(MapleMapObject mo) {
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.remove(mo);
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.remove(mo);
            if (mo.getType() == MapleMapObjectType.MONSTER) {
                ((MapleMonster) mo).removeController(this);
            }
            if (client != null) {
                mo.sendDestroyData(client);
            }
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
    }

    /**
     * 是否為可見的地圖對像
     *
     * @param mo 地圖對像
     * @return true = 可見的對象 : false = 不可見的對象
     */
    public boolean isMapObjectVisible(MapleMapObject mo) {
        visibleMapObjectsLock.readLock().lock();
        try {
            return visibleMapObjects.contains(mo);
        } finally {
            visibleMapObjectsLock.readLock().unlock();
        }
    }

    //    public Collection<MapleMapObject> getAndWriteLockVisibleMapObjects() {
//        visibleMapObjectsLock.writeLock().lock();
//        return visibleMapObjects;
//    }
//
//    public void unlockWriteVisibleMapObjects() {
//        visibleMapObjectsLock.writeLock().unlock();
//    }
    public boolean isAlive() {
        return stats.getHp() > 0;
    }

    /**
     * 作用：發送角色離開地圖的數據包
     *
     * @param client 客戶端
     */
    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    /**
     * 作用：發送召喚角色相關的數據包
     *
     * @param client 客戶端
     */
    @Override
    public void sendSpawnData(MapleClient client) {
        if (client != null && client.getSession() != null && client.getPlayer() != null && client.getPlayer().allowedToTarget(this)) {
            //顯示玩家
            client.announce(MaplePacketCreator.spawnPlayerMapobject(this));
            client.announce(MaplePacketCreator.SpecialChairSitResult(getId(), false, false, null));
            client.announce(EffectPacket.getEffectSwitch(getId(), getEffectSwitch()));
            if (getMap() != null && getGuild() != null && getGuild().getImageLogo() != null && getGuild().getImageLogo().length > 0) {
                getMap().broadcastMessage(this, GuildPacket.loadGuildIcon(this), false);
            }
            //刷新隊員HP
            if (getParty() != null) {
                updatePartyMemberHP();
                receivePartyMemberHP();
            }
            //顯示寶貝龍
            if (dragon != null) {
                client.announce(SummonPacket.spawnDragon(dragon));
            }
//            if (lw != null) {
//                Client.announce(SummonPacket.spawnLittleWhite(lw));
//            }

            //顯示機器人
            if (android != null) {
                client.announce(AndroidPacket.spawnAndroid(this, android));
            }
            if (followid > 0 && followon) {
                client.announce(MaplePacketCreator.followEffect(followinitiator ? followid : id, followinitiator ? id : followid, null));
            }
            if (this.getSoulSkillID() > 0) {
                client.announce(MaplePacketCreator.updateSoulEffect(id, "1".equals(this.getQuestInfo(26535, "effect"))));
            }
            if (this.getBuffedValue(SecondaryStat.GuidedArrow) != null) {
                client.announce(ForcePacket.forceAtomCreate(this.getSpecialStat().getGuidedArrow()));
            }
        }
    }

    public void equipChanged() {
        if (map == null) {
            return;
        }
        map.broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        stats.recalcLocalStats(this);
        if (getMessenger() != null) {
            WorldMessengerService.getInstance().updateMessenger(getMessenger().getId(), getName(), client.getChannel());
        }
    }

    /*
     * 檢測角色背包和身上的複製裝備信息
     * 如果發現直接刪除裝備
     */
    public void checkCopyItems() {
        //檢測複製的裝備
        List<Integer> sns = new ArrayList<>(); //[道具的唯一ID信息]
        Map<Integer, Integer> checkItems = new HashMap<>(); //[道具唯一ID 道具ID]
        //檢測背包的裝備信息
        for (Item item : getInventory(MapleInventoryType.EQUIP).list()) {
            int sn = item.getSN();
            if (sn > 0) {
                if (checkItems.containsKey(sn)) { //發現重複的唯一ID裝備
                    if (checkItems.get(sn) == item.getItemId()) {
                        sns.add(sn);
                    }
                } else {
                    checkItems.put(sn, item.getItemId());
                }
            }
        }
        //檢測背包的時裝信息
        for (Item item : getInventory(MapleInventoryType.DECORATION).list()) {
            int sn = item.getSN();
            if (sn > 0) {
                if (checkItems.containsKey(sn)) { //發現重複的唯一ID裝備
                    if (checkItems.get(sn) == item.getItemId()) {
                        sns.add(sn);
                    }
                } else {
                    checkItems.put(sn, item.getItemId());
                }
            }
        }
        //檢測身上的裝備
        for (Item item : getInventory(MapleInventoryType.EQUIPPED).list()) {
            int sn = item.getSN();
            if (sn > 0) {
                if (checkItems.containsKey(sn)) { //發現重複的唯一ID裝備
                    if (checkItems.get(sn) == item.getItemId()) {
                        sns.add(sn);
                    }
                } else {
                    checkItems.put(sn, item.getItemId());
                }
            }
        }
        //如果重複的唯一ID數量大於0
        boolean autoban = false;
        for (Integer sn : sns) {
            MapleInventoryManipulator.removeAllBySN(client, sn);
            autoban = true;
        }
        if (autoban) {
            AutobanManager.getInstance().autoban(client, "偵測到複製裝備");
        }
        checkItems.clear();
        sns.clear();
    }

    /*
     * 角色所有寵物的信息
     */
    public List<MaplePet> getPets() {
        List<MaplePet> ret = new ArrayList<>();
        for (Item item : getInventory(MapleInventoryType.CASH).newList()) {
            if (item.getPet() != null) {
                ret.add(item.getPet());
            }
        }
        return ret;
    }

    /*
     * 角色召喚中的寵物的信息
     */
    public MaplePet[] getSpawnPets() {
        return spawnPets;
    }

    public MaplePet getSpawnPet(int index) {
        if (spawnPets == null || index >= spawnPets.length || index < 0) {
            return null;
        }
        return spawnPets[index];
    }

    public byte getPetIndex(int petId) {
        for (byte i = 0; i < 3; i++) {
            if (spawnPets[i] != null && spawnPets[i].getUniqueId() == petId) {
                return i;
            }
        }
        return -1;
    }

    public byte getPetIndex(MaplePet pet) {
        for (byte i = 0; i < 3; i++) {
            if (spawnPets[i] != null && spawnPets[i].getUniqueId() == pet.getUniqueId()) {
                return i;
            }
        }
        return -1;
    }

    public byte getPetByItemId(int petItemId) {
        for (byte i = 0; i < 3; i++) {
            if (spawnPets[i] != null && spawnPets[i].getPetItemId() == petItemId) {
                return i;
            }
        }
        return -1;
    }

    public int getNextEmptyPetIndex() {
        for (int i = 0; i < 3; i++) {
            if (spawnPets[i] == null) {
                return i;
            }
        }
        return 3;
    }

    public int getNoPets() {
        int ret = 0;
        for (int i = 0; i < 3; i++) {
            if (spawnPets[i] != null) {
                ret++;
            }
        }
        return ret;
    }

    public List<MaplePet> getSummonedPets() {
        List<MaplePet> ret = new ArrayList<>();
        for (byte i = 0; i < 3; i++) {
            if (spawnPets[i] != null && spawnPets[i].getSummoned()) {
                ret.add(spawnPets[i]);
            }
        }
        return ret;
    }

    public void addSpawnPet(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (spawnPets[i] == null) {
                spawnPets[i] = pet;
                pet.setSummoned((byte) (i + 1));
                return;
            }
        }
    }

    public void removeSpawnPet(MaplePet pet, boolean shiftLeft) {
        for (int i = 0; i < 3; i++) {
            if (spawnPets[i] != null) {
                if (spawnPets[i].getUniqueId() == pet.getUniqueId()) {
                    spawnPets[i] = null;
                    break;
                }
            }
        }
    }

    public void unequipAllSpawnPets() {
        for (int i = 0; i < 3; i++) {
            if (spawnPets[i] != null) {
                unequipSpawnPet(spawnPets[i], true, (byte) 0);
            }
        }
    }

    public void spawnPet(short slot) {
        spawnPet(slot, false, true);
    }

    public void spawnPet(short slot, boolean lead) {
        spawnPet(slot, lead, true);
    }

    /**
     * 召喚寵物
     *
     * @param slot      寵物在背包中的位置
     * @param lead      主寵物，第一個
     * @param broadcast 地圖發送當前數據包
     */
    public void spawnPet(short slot, boolean lead, boolean broadcast) {
        Item item = getInventory(MapleInventoryType.CASH).getItem(slot);
        if (item == null || !類型.寵物(item.getItemId())) {
            client.sendEnableActions();
            return;
        }
        switch (item.getItemId()) {
            case 5000047:   //羅伯
            case 5000028: { //進化龍
                MaplePet pet = MaplePet.createPet(item.getItemId() + 1, MapleInventoryIdentifier.getInstance());
                if (pet != null) {
                    MapleInventoryManipulator.addById(client, item.getItemId() + 1, 1, item.getOwner(), pet, 90, "雙擊寵物獲得: " + item.getItemId() + " 時間: " + DateUtil.getCurrentDate());
                    MapleInventoryManipulator.removeFromSlot(client, MapleInventoryType.CASH, slot, (short) 1, false);
                }
                break;
            }
            default: {
                MaplePet pet = item.getPet();
                if (pet != null && pet.getSecondsLeft() > 0 || item.getExpiration() == -3 || item.getExpiration() == -1 || item.getExpiration() > System.currentTimeMillis()) {
                    if (getPetIndex(pet) != -1) { // Already summoned, let's keep it
                        unequipSpawnPet(pet, true, (byte) 0);
                    } else {
                        if (getNoPets() == 3 && getSpawnPet(0) != null) {
                            unequipSpawnPet(getSpawnPet(0), false, (byte) 0);
                        } else if (lead) { // Follow the Lead
                            shiftPetsRight();
                        }
                        Point pos = getPosition();
                        pos.y -= 12;
                        pet.setPos(pos);
                        try {
                            pet.setFh(getMap().getFootholds().findBelow(pet.getPos()).getId());
                        } catch (NullPointerException e) {
                            pet.setFh(0); //lol, it can be fixed by movement
                        }
                        pet.setStance(0);
                        MapleQuestStatus stat = getQuestNAdd(MapleQuest.getInstance(GameConstants.ALLOW_PET_LOOT));
                        if (stat.getCustomData() == null || stat.getCustomData().equals("1")) {
                            pet.setCanPickup(true);
                            stat.setCustomData("1");
                        } else {
                            pet.setCanPickup(false);
                        }
                        addSpawnPet(pet);
                        for (int i = 0; i < pet.getBuffSkills().length; i++) {
                            String value = getOneInfo(101080 + pet.getSummonedValue() - 1, String.valueOf(10 * (pet.getSummonedValue() - 1) + i));
                            int skillId = value == null || !value.matches("^\\d+$") ? 0 : Integer.valueOf(value);
                            if (skillId > 0 && getSkillLevel(skillId) <= 0) {
                                skillId = 0;
                                updateOneInfo(101080 + pet.getSummonedValue() - 1, String.valueOf(10 * (pet.getSummonedValue() - 1) + i), "0");
                            }
                            pet.setBuffSkill(i, skillId);
                        }
                        if (getMap() != null) {
                            petUpdateStats(pet, true);
                            getMap().broadcastMessage(this, PetPacket.showPet(this, pet, false, (byte) 0), true);
                            getClient().announce(PetPacket.loadExceptionList(this, pet));
                            getClient().announce(PetPacket.petStatUpdate(this));
                            checkPetSkill();
                        }
                    }
                }
                break;
            }
        }
        client.sendEnableActions();
    }

    public void unequipSpawnPet(MaplePet pet, boolean shiftLeft, byte showType) {
        if (getPetIndex(pet) != -1 && getSpawnPet(getPetIndex(pet)) != null) {
            getSpawnPet(getPetIndex(pet)).setSummoned((byte) 0);
            getSpawnPet(getPetIndex(pet)).saveToDb();
        }
        petUpdateStats(pet, false);
        if (map != null) {
            map.broadcastMessage(this, PetPacket.showPet(this, pet, true, showType), true);
        }
        removeSpawnPet(pet, shiftLeft);
        checkPetSkill();
        //List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
        //stats.put(MapleStat.寵物, Integer.valueOf(0)));
        //showpetupdate isn't done here...
        //Client.announce(PetPacket.petStatUpdate(this));
        client.sendEnableActions();
    }

    public void shiftPetsRight() {
        if (spawnPets[2] == null) {
            spawnPets[2] = spawnPets[1];
            spawnPets[1] = spawnPets[0];
            spawnPets[0] = null;
        }
    }

    public void checkPetSkill() {
        Map<Integer, Integer> setHandling = new HashMap<>(); //寵物技能套裝集合
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (int i = 0; i < 3; i++) {
            if (spawnPets[i] != null) {
                int set = ii.getPetSetItemID(spawnPets[i].getPetItemId());
                if (set > 0) {
                    int value = 1;
                    if (setHandling.containsKey(set)) {
                        value += setHandling.get(set);
                    }
                    setHandling.put(set, value);
                }
            }
        }
        if (setHandling.isEmpty()) {
            Map<Integer, SkillEntry> chrSkill = new HashMap<>(getSkills());
            Map<Integer, SkillEntry> petSkill = new HashMap<>();
            for (Entry<Integer, SkillEntry> skill : chrSkill.entrySet()) {
                Skill skill1 = SkillFactory.getSkill(skill.getKey());
                if (skill1 != null && skill1.isPetPassive()) {
                    petSkill.put(skill.getKey(), new SkillEntry((byte) 0, (byte) 0, -1));
                }
            }
            if (!petSkill.isEmpty()) { //如果寵物被動觸發技能不為空
                changePetSkillLevel(petSkill);
            }
            return;
        }
        Map<Integer, SkillEntry> petSkillData = new HashMap<>();
        for (Entry<Integer, Integer> entry : setHandling.entrySet()) {
            StructSetItem setItem = ii.getSetItem(entry.getKey());
            if (setItem != null) {
                Map<Integer, StructSetItemStat> setItemStats = setItem.getSetItemStats();
                for (Entry<Integer, StructSetItemStat> ent : setItemStats.entrySet()) {
                    StructSetItemStat setItemStat = ent.getValue();
                    if (ent.getKey() <= entry.getValue()) {
                        if (setItemStat.skillId > 0 && setItemStat.skillLevel > 0 && getSkillLevel(setItemStat.skillId) <= 0) {
                            petSkillData.put(setItemStat.skillId, new SkillEntry((byte) setItemStat.skillLevel, (byte) 0, -1));
                        }
                    } else if (setItemStat.skillId > 0 && setItemStat.skillLevel > 0 && getSkillLevel(setItemStat.skillId) > 0) {
                        petSkillData.put(setItemStat.skillId, new SkillEntry((byte) 0, (byte) 0, -1));
                    }
                }
            }
        }
        if (!petSkillData.isEmpty()) {
            changePetSkillLevel(petSkillData);
        }
    }

    public void updateLastChangeMapTime() {
        lastChangeMapTime = System.currentTimeMillis();
    }

    public long getLastChangeMapTime() {
        return lastChangeMapTime;
    }

    public long getLastFameTime() {
        return lastfametime;
    }

    public List<Integer> getFamedCharacters() {
        return lastmonthfameids;
    }

    public List<Integer> getBattledCharacters() {
        return lastmonthbattleids;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (lastfametime >= System.currentTimeMillis() - 60 * 60 * 24 * 1000) {
            return FameStatus.NOT_TODAY;
        } else if (from == null || lastmonthfameids == null || lastmonthfameids.contains(from.getId())) {
            return FameStatus.NOT_THIS_MONTH;
        }
        return FameStatus.OK;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(to.getId());
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("ERROR writing famelog for char " + getName() + " to " + to.getName() + e);
        }
    }

    public boolean canBattle(MapleCharacter to) {
        return !(to == null || lastmonthbattleids == null || lastmonthbattleids.contains(to.getAccountID()));
    }

    public void hasBattled(MapleCharacter to) {
        lastmonthbattleids.add(to.getAccountID());
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO battlelog (accid, accid_to) VALUES (?, ?)");
            ps.setInt(1, getAccountID());
            ps.setInt(2, to.getAccountID());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("ERROR writing battlelog for char " + getName() + " to " + to.getName() + e);
        }
    }

    public MapleQuickSlot getQuickSlot() {
        return this.quickslot;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public byte getWorld() {
        return world;
    }

    public void setWorld(byte world) {
        this.world = world;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public boolean inEvent() {
        return eventInstance != null;
    }

    public ScriptEvent getEventInstance() {
        return eventInstance;
    }

    public void setEventInstance(ScriptEvent eventInstance) {
        this.eventInstance = eventInstance;
    }

    public boolean checkEvent() {
        return eventInstance != null && (map == null || map.getEvent() != eventInstance);
    }

    public void addTownPortal(TownPortal door) {
        townportals.add(door);
    }

    public void clearTownPortals() {
        townportals.clear();
    }

    public List<TownPortal> getTownPortals() {
        return new ArrayList<>(townportals);
    }

    public void addMechDoor(MechDoor door) {
        mechDoors.add(door);
    }

    public void clearMechDoors() {
        mechDoors.clear();
    }

    public List<MechDoor> getMechDoors() {
        return new ArrayList<>(mechDoors);
    }

    public void setSmega() {
        if (smega) {
            smega = false;
            dropMessage(5, "You have set megaphone to disabled mode");
        } else {
            smega = true;
            dropMessage(5, "You have set megaphone to enabled mode");
        }
    }

    public boolean getSmega() {
        return smega;
    }

    public PortableChair getChair() {
        return chair;
    }

    public void setChair(PortableChair chair) {
        this.chair = chair;
        stats.relocHeal(this);
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public int getItemEffectType() {
        return itemEffectType;
    }

    public void setItemEffectType(int itemEffectType) {
        this.itemEffectType = itemEffectType;
    }

    public int getActiveNickItemID() {
        final String questInfo;
        if ((questInfo = this.getQuestInfo(19019, "id")) == null) {
            return 0;
        }
        return Integer.parseInt(questInfo);
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    @Override
    public int getRange() {
        return GameConstants.maxViewRange();
    }

    public int getCurrentRep() {
        return currentrep;
    }

    public int getTotalRep() {
        return totalrep;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public int getTotalLosses() {
        return totalLosses;
    }

    public void increaseTotalWins() {
        totalWins++;
    }

    public void increaseTotalLosses() {
        totalLosses++;
    }

    public int getGuildId() {
        return guildid;
    }

    public void setGuildId(int newGuildId) {
        guildid = newGuildId;
        if (guildid > 0) {
            if (mgc == null) {
                mgc = new MapleGuildCharacter(this);
            } else {
                mgc.setGuildId(guildid);
            }
        } else {
            mgc = null;
            guildContribution = 0;
        }
    }

    public byte getGuildRank() {
        return guildrank;
    }

    public void setGuildRank(byte newRank) {
        guildrank = newRank;
        if (mgc != null) {
            mgc.setGuildRank(newRank);
        }
    }

    public int getGuildContribution() {
        return guildContribution;
    }

    public void setGuildContribution(int newContribution) {
        this.guildContribution = newContribution;
        if (mgc != null) {
            mgc.setGuildContribution(newContribution);
        }
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public byte getAllianceRank() {
        return allianceRank;
    }

    public void setAllianceRank(byte newRank) {
        allianceRank = newRank;
        if (mgc != null) {
            mgc.setAllianceRank(newRank);
        }
    }

    public MapleGuild getGuild() {
        if (getGuildId() <= 0) {
            return null;
        }
        return WorldGuildService.getInstance().getGuild(getGuildId());
    }

    public void guildUpdate() {
        if (guildid <= 0) {
            return;
        }
        mgc.setLevel(level);
        mgc.setJobId(job);
        WorldGuildService.getInstance().memberLevelJobUpdate(mgc);
    }

    public void saveGuildStatus() {
        MapleGuild.setOfflineGuildStatus(guildid, guildrank, guildContribution, allianceRank, id);
    }

    public boolean isBronzeIMvp() {
        return getMvpLevel() >= PlayerRank.MVP銅牌I.getLevel();
    }

    public boolean isBronzeIIMvp() {
        return getMvpLevel() >= PlayerRank.MVP銅牌II.getLevel();
    }

    public boolean isBronzeIIIMvp() {
        return getMvpLevel() >= PlayerRank.MVP銅牌III.getLevel();
    }

    public boolean isBronzeIVMvp() {
        return getMvpLevel() >= PlayerRank.MVP銅牌IV.getLevel();
    }

    public boolean isSilverMvp() {
        return getMvpLevel() >= PlayerRank.MVP銀牌.getLevel();
    }

    public boolean isGoldMvp() {
        return getMvpLevel() >= PlayerRank.MVP金牌.getLevel();
    }

    public boolean isDiamondMvp() {
        return getMvpLevel() >= PlayerRank.MVP鑽石.getLevel();
    }

    public boolean isRedMvp() {
        return getMvpLevel() >= PlayerRank.MVP紅鑽.getLevel();
    }

    public int getMvpLevel() {
        float rate = ServerConfig.MVP_AMOUNT_RATE;
        int amount = getMvpPayAmount();
        String nowMonth = DateUtil.getCurrentDate("yyyyMM");
        int level = 0;
        // 處理MVP階級取得日期
        for (int i = 1; i <= 8; i++) {
            int cost;
            switch (i) {
                case 1:
                    cost = GameConstants.MVPPAY_BRONZEI;
                    break;
                case 2:
                    cost = GameConstants.MVPPAY_BRONZEII;
                    break;
                case 3:
                    cost = GameConstants.MVPPAY_BRONZEIII;
                    break;
                case 4:
                    cost = GameConstants.MVPPAY_BRONZEIV;
                    break;
                case 5:
                    cost = GameConstants.MVPPAY_SILVER;
                    break;
                case 6:
                    cost = GameConstants.MVPPAY_GOLD;
                    break;
                case 7:
                    cost = GameConstants.MVPPAY_DIAMOND;
                    break;
                case 8:
                default:
                    cost = GameConstants.MVPPAY_RED;
                    break;
            }
            if (i >= 5) {
                amount = getMvpPayAmountMonthly();
            }
            if (amount < cost * rate) {
                break;
            } else {
                level = i;
            }
            if (i < 8) {
                String sp = getWorldShareInfo(6, "sp_" + i);
                if (sp == null) {
                    updateWorldShareInfo(6, "sp_" + i, nowMonth);
                }
            } else {
                String rp = getOneInfo(100561, "rp_" + i);
                if (rp == null) {
                    updateOneQuestInfo(100561, "rp_" + i, nowMonth);
                }
            }
        }
        if (level >= 5) {
            if (level < 8) {
                String sp = getWorldShareInfo(6, "sp_" + level);
                if (!sp.contains(nowMonth) && !sp.equalsIgnoreCase("R" + nowMonth)) {
                    updateWorldShareInfo(6, "sp_" + level, nowMonth);
                    updateWorldShareInfo(90, "1=1");
                }
            } else {
                String sp = getWorldShareInfo(6, "sp_5");
                if (!sp.contains(nowMonth) && !sp.equalsIgnoreCase("R" + nowMonth)) {
                    updateWorldShareInfo(6, "sp_5", "R" + nowMonth);
                    updateWorldShareInfo(90, "1=1");
                }
                String rp = getOneInfo(100561, "rp_" + level);
                if (!rp.contains(nowMonth) && !rp.equalsIgnoreCase(nowMonth + "R")) {
                    updateOneQuestInfo(100561, "rp_" + level, nowMonth);
                }
            }
        }
        return level;
    }

    public int getMvpPayAmount() {
        float rate = ServerConfig.MVP_AMOUNT_RATE;
        boolean isCustom = rate != 1.0f;
        String sAmount = getWorldShareInfo(5, (isCustom ? "c" : "") + "amount");
        return Math.abs(sAmount == null ? 0 : Integer.parseInt(sAmount));
    }

    public int getMvpPayAmountMonthly() {
        float rate = ServerConfig.MVP_AMOUNT_RATE;
        boolean isCustom = rate != 1.0f;
        int amount = 0;
        for (int i = 0; i <= 2; i++) {
            String month = DateUtil.getPreDate("M", -i).replaceAll("-", "").substring(0, 6);
            String sMonthAmount = getWorldShareInfo(4, (isCustom ? "c" : "") + month);
            amount += Math.abs(sMonthAmount == null ? 0 : Integer.parseInt(sMonthAmount));
        }
        return amount;
    }

    public void updateMvpLog(int quantity) {
        if (quantity >= 0) {
            return;
        }
        float rate = ServerConfig.MVP_AMOUNT_RATE;
        boolean isCustom = rate != 1.0f;
        quantity = Math.abs(quantity);
        String now = DateUtil.getCurrentDate("yyyyMMdd");
        String nowMonth = DateUtil.getCurrentDate("yyyyMM");
        String sNowMonthAmount = getWorldShareInfo(4, (isCustom ? "c" : "") + nowMonth);
        String last = getWorldShareInfo(4, "last");

        // 處理消費計總記錄
        String sTodayAmount = getWorldShareInfo(5, (isCustom ? "c" : "") + "todayAmount_" + now);
        if (sTodayAmount == null) {
            sTodayAmount = getWorldShareInfo(5, (isCustom ? "c" : "") + "todayAmount_" + last);
        }
        String sAmount = getWorldShareInfo(5, (isCustom ? "c" : "") + "amount");
        int todayAmount = Math.abs(sTodayAmount == null ? 0 : Integer.parseInt(sTodayAmount)) + quantity;
        int amount = Math.abs(sAmount == null ? 0 : Integer.parseInt(sAmount));
        int remainQuantity;
        if (amount < ((int) (5000 * rate))) {
            remainQuantity = Math.max(quantity - (((int) (5000 * rate)) - amount), 0);
        } else {
            remainQuantity = quantity;
        }
        amount += quantity;
        String amountData = "";
        if (isCustom) {
            amountData += "ctodayAmount_" + now + "=" + todayAmount + ";camount=" + amount + ";";
            amountData += "todayAmount_" + now + "=" + (int) (todayAmount / rate) + ";amount=" + (int) (amount / rate);
        } else {
            amountData += "todayAmount_" + now + "=" + todayAmount + ";amount=" + amount;
        }
        updateWorldShareInfo(5, amountData);

        // 處理每月消費記錄
        String payLogData = "";
        for (int i = 12; i >= 1; i--) {
            String preMonth = DateUtil.getPreDate("M", -i).replaceAll("-", "").substring(0, 6);
            String sPreMonthAmount = getWorldShareInfo(4, (isCustom ? "c" : "") + preMonth);
            if (sPreMonthAmount == null) {
                continue;
            }
            if (isCustom) {
                payLogData += "c" + preMonth + "=" + sPreMonthAmount + ";";
                payLogData += preMonth + "=" + (int) (Integer.parseInt(sPreMonthAmount) / rate) + ";";
            } else {
                payLogData += preMonth + "=" + sPreMonthAmount + ";";
            }
        }
        int nowMonthAmount = Math.abs(sNowMonthAmount == null ? 0 : Integer.parseInt(sNowMonthAmount)) + remainQuantity;
        if (isCustom) {
            payLogData += "c" + nowMonth + "=" + nowMonthAmount + ";";
            payLogData += nowMonth + "=" + (int) (nowMonthAmount / rate) + ";";
        } else {
            payLogData += nowMonth + "=" + nowMonthAmount + ";";
        }
        payLogData += "last=" + now;
        updateWorldShareInfo(4, payLogData);

        getMvpLevel();
    }

    public boolean modifyCSPoints(int type, int quantity) {
        return modifyCSPoints(type, quantity, false);
    }

    public boolean modifyCSPoints(int type, int quantity, boolean show) {
        return modifyCSPoints(type, quantity, show, true);
    }

    public boolean modifyCSPoints(int type, int quantity, boolean show, boolean updateMvp) {
        int itemID = 0;
        switch (type) {
            case 1:
                itemID = 2435892;
                if (quantity > 0 && getACash() + quantity < 0) {
                    if (show) {
                        send(UIPacket.ScriptProgressItemMessage(itemID, "樂豆點已達到上限！"));
                    }
                    return false;
                }
                setACash(getACash() + quantity);
                if (updateMvp) {
                    updateMvpLog(quantity);
                }
                break;
            case 2:
                itemID = 2432107;
                if (quantity > 0 && getMaplePoints(true) + quantity < 0) {
                    if (show) {
                        send(UIPacket.ScriptProgressItemMessage(itemID, "楓點已達到上限！"));
                    }
                    return false;
                }
                if (!client.modifyCSPoints(type, quantity)) {
                    return false;
                }
                playerObservable.update();
                client.announce(MaplePacketCreator.showCharCash(this));
                break;
            default:
                return false;
        }
        if (show ) {
            client.announce(EffectPacket.showSpecialEffect(EffectOpcode.UserEffect_ExpItemConsumed));
        }
        return true;
    }

    public int getCSPoints(int type) {
        switch (type) {
            case 1:
                return getACash();
            case 2:
                return getMaplePoints();
            case -1:
                return getACash() + getMaplePoints();
            default:
                return 0;
        }
    }

    public int getTotalTWD() {
        int twd = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT SUM(rmb) FROM paylog WHERE account = ?");
            ps.setString(1, getClient().getAccountName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                twd = rs.getInt(1);
            }
            ps.close();
        } catch (SQLException Ex) {
            log.error("獲取賬號儲值總數失敗.", Ex);
        }
        return twd;
    }

    public List<Pair<String, Integer>> getTotalTWDRanking(int limit) {
        List<Pair<String, Integer>> ret = new LinkedList<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            Calendar c = Calendar.getInstance();
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1, 0, 0, 0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            PreparedStatement ps = con.prepareStatement("SELECT account, SUM(rmb) FROM paylog WHERE date(`paytime`) >= ? GROUP BY account ORDER BY rmb DESC LIMIT ?");
            ps.setString(1, sdf.format(c.getTime().getTime()));
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(new Pair<>(rs.getString("account"), rs.getInt("rmb")));
            }
            ps.close();
        } catch (SQLException Ex) {
            log.error("獲取賬號儲值總數失敗.", Ex);
        }
        return ret;
    }

    public int getTWD() {
        int point = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT rmb FROM accounts WHERE name = ?")) {
                ps.setString(1, getClient().getAccountName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        point = rs.getInt("rmb");
                    }
                }
            }
        } catch (SQLException e) {
            log.error("獲取角色twd失敗。", e);
        }
        return point;
    }

    public void setTWD(final int point) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET rmb = ? WHERE id = ?")) {
                ps.setInt(1, point);
                ps.setInt(2, getClient().getAccID());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("角色設置TWD失敗。", e);
        }
        playerObservable.update();
    }

    public void gainTWD(final int point) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET rmb = rmb + ? WHERE id = ?")) {
                ps.setInt(1, point);
                ps.setInt(2, getClient().getAccID());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("角色增加TWD失敗。", e);
        }
        playerObservable.update();
    }

    public boolean hasEquipped(int itemid) {
        return inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid) >= 1;
    }

    public boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
        MapleInventoryType type = ItemConstants.getInventoryType(itemid);
        int possesed = inventory[type.ordinal()].countById(itemid);
        if (checkEquipped && (type == MapleInventoryType.EQUIP || type == MapleInventoryType.DECORATION)) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        if (greaterOrEquals) {
            return possesed >= quantity;
        } else {
            return possesed == quantity;
        }
    }

    public boolean haveItem(int itemid, int quantity) {
        return haveItem(itemid, quantity, true, true);
    }

    public boolean haveItem(int itemid) {
        return haveItem(itemid, 1, true, true);
    }

    /*
     * 查看玩家道具的數量
     */
    public int getItemQuantity(int itemid) {
        MapleInventoryType type = ItemConstants.getInventoryType(itemid);
        return getInventory(type).countById(itemid);
    }

    /*
     * 查看玩家道具的數量是否檢測當前穿戴的裝備
     */
    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = inventory[ItemConstants.getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }

    public int getEquipId(short slot) {
        MapleInventory equip = getInventory(MapleInventoryType.EQUIP);
        return equip.getItem(slot).getItemId();
    }

    public int getUseId(short slot) {
        MapleInventory use = getInventory(MapleInventoryType.USE);
        return use.getItem(slot).getItemId();
    }

    public int getSetupId(short slot) {
        MapleInventory setup = getInventory(MapleInventoryType.SETUP);
        return setup.getItem(slot).getItemId();
    }

    public int getCashId(short slot) {
        MapleInventory cash = getInventory(MapleInventoryType.CASH);
        return cash.getItem(slot).getItemId();
    }

    public int getEtcId(short slot) {
        MapleInventory etc = getInventory(MapleInventoryType.ETC);
        return etc.getItem(slot).getItemId();
    }

    public int getDecorationId(short slot) {
        MapleInventory etc = getInventory(MapleInventoryType.DECORATION);
        return etc.getItem(slot).getItemId();
    }

    /*
     * 好友
     */
    public byte getBuddyCapacity() {
        return buddylist.getCapacity();
    }

    public void setBuddyCapacity(byte capacity) {
        buddylist.setCapacity(capacity);
        client.announce(BuddyListPacket.updateBuddyCapacity(capacity));
    }

    /*
     * 聊天招待
     */
    public MapleMessenger getMessenger() {
        return messenger;
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    /*
     * 召喚獸處理
     */
    public List<MapleSummon> getSummonsReadLock() {
        summonsLock.lock();
        return summons;
    }

    public int getSummonsSize() {
        return summons.size();
    }

    public void unlockSummonsReadLock() {
        summonsLock.unlock();
    }

    public void addSummon(MapleSummon s) {
        summonsLock.lock();
        try {
            summons.add(s);
        } finally {
            summonsLock.unlock();
        }
    }

    public int getSummonCountBySkill(int skillID) {
        summonsLock.lock();
        try {
            return (int) summons.stream().filter(it -> it.getSkillId() == skillID).count();
        } finally {
            summonsLock.unlock();
        }
    }

    public final List<Integer> getSummonsOIDsBySkillID(final int n) {
        summonsLock.lock();
        try {
            return this.summons.stream().filter(summon -> summon.getSkillId() == n).map(MapleMapObject::getObjectId).collect(Collectors.toList());
        } finally {
            summonsLock.unlock();
        }
    }

    public MapleSummon getSummonBySkillID(int skillId) {
        summonsLock.lock();
        try {
            return summons.stream().filter(summon -> summon.getSkillId() == skillId).findFirst().orElse(null);
        } finally {
            summonsLock.unlock();
        }
    }

    public final void removeSummonBySkillID(final int skillID, final int animated) {
        final MapleSummon summon;
        if ((summon = this.getSummonBySkillID(skillID)) != null) {
            this.removeSummon(summon, animated);
        }
    }

    public final void removeSummon(final MapleSummon summon, final int animated) {
        if (summon != null) {
            summon.setAnimated(animated);
            cancelEffect(summon.getEffect(), false, summon.getCreateTime(), summon.getEffect().getStatups());
        }
    }

    public final void spawnSummons() {
        summonsLock.lock();
        try {
            for (MapleSummon summon : this.summons) {
                summon.setAnimated(1);
                summon.setPosition(this.getPosition());
                summon.setCurrentFh(this.getCurrentFH());
                summon.setMap(this.map);
                this.map.addMapObject(summon);
                summon.setAnimated(0);
            }
        } finally {
            summonsLock.unlock();
        }
    }

    public final void disappearSummons(final boolean b) {
        List<MapleSummon> list = new ArrayList<>();
        summonsLock.lock();
        try {
            for (MapleSummon summon : this.summons) {
                summon.setAnimated(12);
                summon.setMap(null);
                if (this.map != null) {
                    this.map.disappearMapObject(summon);
                }
                switch (summon.getMovementType()) {
                    case STOP:
                    case FIX_V_MOVE:
                    case SMART:
                    case WALK_RANDOM: {
                        list.add(summon);
                        break;
                    }
                    default: {
                        if (b) {
                            list.add(summon);
                        }
                    }
                }
            }
        } finally {
            summonsLock.unlock();
        }
        for (MapleSummon summon : list) {
            cancelEffect(summon.getEffect(), b, summon.getCreateTime(), summon.getEffect().getStatups());
        }
    }

    public boolean removeSummon(MapleSummon s) {
        summonsLock.lock();
        try {
            return summons.remove(s);
        } finally {
            summonsLock.unlock();
        }
    }

    public void removeSummon(int Skillid) {
        summonsLock.lock();
        MapleSummon delet = null;
        try {
            for (MapleSummon su : summons) {
                if (su.getSkillId() == Skillid) {
                    delet = su;
                    break;
                }
            }
            if (delet != null) {
                getMap().broadcastMessage(SummonPacket.removeSummon(delet, true));
                getMap().removeMapObject(delet);
                removeVisibleMapObjectEx(delet);
                summons.remove(delet);
            }
        } finally {
            summonsLock.unlock();
        }
    }

    /**
     * 當前技能是否已經有召喚獸
     */
    public boolean hasSummonBySkill(int skillId) {
        if (summons == null || summons.isEmpty()) {
            return false;
        }
        summonsLock.lock();
        try {
            for (MapleSummon summon : summons) {
                if (summon.getSkillId() == skillId) {
                    return true;
                }
            }
        } finally {
            summonsLock.unlock();
        }
        return false;
    }

    public void updateSummonBySkillID(int skillId, int newSkillId, boolean animated) {
        summonsLock.lock();
        try {
            summons.forEach(summon -> {
                if (summon.getSkillId() == skillId) {
                    summon.setSkillId(newSkillId);
                    this.getMap().broadcastMessage(SummonPacket.removeSummon(summon, animated));
                }
            });
        } finally {
            summonsLock.unlock();
        }
    }

    public int getCooldownLeftTime(int skillId) {
        cooldownLock.lock();
        try {
            for (MapleCoolDownValueHolder mcdvh : skillCooldowns.values()) {
                if (mcdvh.skillId == skillId) {
                    return mcdvh.getLeftTime();
                }
            }
            return 0;
        } finally {
            this.cooldownLock.unlock();
        }
    }

    /*
     * 技能冷卻處理
     */
    public void clearCooldown(boolean b) {
        cooldownLock.lock();
        try {
            Iterator<Entry<Integer, MapleCoolDownValueHolder>> iterator = this.skillCooldowns.entrySet().iterator();
            while (iterator.hasNext()) {
                final Entry<Integer, MapleCoolDownValueHolder> it = iterator.next();
                final MapleCoolDownValueHolder mcdvh = it.getValue();
                final Skill skill = SkillFactory.getSkill(it.getKey());
                if (skill != null) {
                    if (b && (skill.isHyperSkill() || skill.isNotCooltimeReduce() || skill.isNotCooltimeReset() || skill.isVSkill())) {
                        continue;
                    }
                    if (mcdvh != null) {
                        mcdvh.cancel();
                    }
                    if (this.client != null && this.map != null) {
                        this.client.announce(MaplePacketCreator.skillCooltimeSet(it.getKey(), 0));
                    }
                    iterator.remove();
                }
            }
        } finally {
            cooldownLock.unlock();
        }
    }

    public final void reduceAllSkillCooldown(final int time, boolean b) {
        this.cooldownLock.lock();
        Map<Integer, Integer> map = new HashMap<>();
        try {
            Iterator<Entry<Integer, MapleCoolDownValueHolder>> iterator = this.skillCooldowns.entrySet().iterator();
            while (iterator.hasNext()) {
                final Entry<Integer, MapleCoolDownValueHolder> it = iterator.next();
                final MapleCoolDownValueHolder mcdvh = it.getValue();
                final Skill skill = SkillFactory.getSkill(it.getKey());
                if (skill != null) {
                    if (b && (skill.isHyperSkill() || skill.isNotCooltimeReduce() || skill.isNotCooltimeReset() || skill.isVSkill())) {
                        continue;
                    }
                    if (mcdvh != null) {
                        int left = mcdvh.getLeftTime() - time;
                        if (left > 0) {
                            map.put(it.getKey(), left);
                        } else {
                            mcdvh.cancel();
                            if (this.client != null && this.map != null) {
                                this.client.announce(MaplePacketCreator.skillCooltimeSet(it.getKey(), 0));
                            }
                            iterator.remove();
                        }
                    }
                }
            }
        } finally {
            this.cooldownLock.unlock();
        }
        for (Entry<Integer, Integer> entry : map.entrySet()) {
            registerSkillCooldown(entry.getKey(), entry.getValue(), true);
        }
    }

    public final void reduceSkillCooldownRate(final int skillID, int reduceRate) {
        this.cooldownLock.lock();
        MapleCoolDownValueHolder mcdvh;
        try {
            mcdvh = this.skillCooldowns.get(skillID);
        } finally {
            this.cooldownLock.unlock();
        }
        if (mcdvh != null) {
            if ((reduceRate = mcdvh.getLeftTime() * (100 - reduceRate) / 100) > 0) {
                this.registerSkillCooldown(skillID, reduceRate, true);
                return;
            }
            this.cancelSkillCooldown(skillID);
        }
    }

    /**
     * 減少單個技能冷卻時間
     *
     * @param skillID    要減少冷卻時間的技能ID
     * @param reduceTime 減少的時間（單位：毫秒）
     */
    public final void reduceSkillCooldown(final int skillID, int reduceTime) {
        this.cooldownLock.lock();
        MapleCoolDownValueHolder mcdvh;
        try {
            mcdvh = this.skillCooldowns.get(skillID);
        } finally {
            this.cooldownLock.unlock();
        }
        if (mcdvh != null) {
            if ((reduceTime = mcdvh.getLeftTime() - reduceTime) > 0) {
                this.registerSkillCooldown(skillID, reduceTime, true);
            } else {
                this.cancelSkillCooldown(skillID);
            }
        }
    }

    public final void cancelSkillCooldown(final int skillID) {
        this.cooldownLock.lock();
        try {
            final MapleCoolDownValueHolder mcdvh = this.skillCooldowns.remove(skillID);
            if (mcdvh != null) {
                mcdvh.cancel();
            }
            if (this.client != null && this.map != null) {
                this.client.announce(MaplePacketCreator.skillCooltimeSet(skillID, 0));
            }
        } finally {
            this.cooldownLock.unlock();
        }
        switch (skillID) {
            case 黑騎士.轉生_狀態: {
                MapleStatEffect effect = getSkillEffect(黑騎士.轉生);
                if (effect != null) {
                    effect.applyBuffEffect(this, this, 100, false, false, false, getPosition());
                }
                break;
            }
            case 重砲指揮官.精準轟炸: {
                specialStats.setPoolMakerCount(0);
                send(MaplePacketCreator.poolMakerInfo(false, 0, 0));
                break;
            }
            default: {
                if (skillID == soulSkillID) {
                    setShowSoulEffect(true);
                }
                break;
            }
        }
    }

    public void registerSkillCooldown(MapleStatEffect eff, boolean send) {
        registerSkillCooldown(SkillConstants.getCooldownLinkSourceId(eff.getSourceId()), eff.getCooldown(this), send);
    }

    public void registerSkillCooldown(int skillID, int duration, boolean send) {
        if (ServerConfig.CHANNEL_PLAYER_DISABLECOOLDOWN || (isAdmin() && isInvincible()) && duration > 350) {
            if (isAdmin() && isInvincible()) {
                //dropMessage(-6, "伺服器管理員無敵狀態消除技能冷卻，技能：" + skillID + "冷卻時間：" + duration + " -> 350ms");
            }
            duration = 350;
        }
        cooldownLock.lock();
        try {
            MapleCoolDownValueHolder mcdvh = skillCooldowns.remove(skillID);
            if (mcdvh != null) {
                mcdvh.cancel();
            }
            if (duration > 0) {
                if (isDebug()) {
                    this.dropDebugMessage(0, "[CoolDown] Register CoolDown SkillID:" + skillID + " Duration:" + duration);
                }
                skillCooldowns.put(skillID, new MapleCoolDownValueHolder(skillID, duration, CoolDownTimer.getInstance().schedule(new MapleCoolDownValueHolder.CancelCooldownAction(skillID, this), duration)));
                if (send) {
                    client.announce(MaplePacketCreator.skillCooltimeSet(skillID, duration));
                }
            }
        } finally {
            cooldownLock.unlock();
        }
        if (send && skillID == 黑騎士.轉生_狀態 && duration > 0) {
            MapleStatEffect effect = getSkillEffect(黑騎士.轉生);
            if (effect != null) {
                effect.applyBuffEffect(this, this, duration, false, false, false, getPosition());
            }
        }
    }

    public final Map<Integer, MapleCoolDownValueHolder> getSkillCooldowns() {
        this.cooldownLock.lock();
        try {
            return this.skillCooldowns;
        } finally {
            this.cooldownLock.unlock();
        }
    }

    public boolean isSkillCooling(int skillid) {
        this.cooldownLock.lock();
        try {
            return skillCooldowns.containsKey(skillid);
        } finally {
            this.cooldownLock.unlock();
        }
    }

    public void registerSkillCooldown(int skillId, long startTime, long length) {
//        if (length > 0L) {
//            if (isAdmin()) {
//                dropDebugMessage(0, "[技能冷卻] 伺服器管理員消除技能冷卻時間(原時間:" + length / 1000L + "秒)");
//                Client.announce(MaplePacketCreator.skillCooldown(skillId, 0));
//                return;
//            }
//            Client.announce(MaplePacketCreator.skillCooldown(skillId, (int) length / 1000));
//            skillCooldowns.put(skillId, new MapleCoolDownValueHolder(skillId, startTime, length));
//        } else {
//            Client.announce(MaplePacketCreator.skillCooldown(skillId, 0));
//        }
        registerSkillCooldown(skillId, (int) length, true);
    }

    public void giveCoolDowns(List<MapleCoolDownValueHolder> cooldowns) {
        if (cooldowns != null) {
            for (MapleCoolDownValueHolder cooldown : cooldowns) {
                if (cooldown.getLeftTime() <= 0) {
                    cancelSkillCooldown(cooldown.skillId);
                } else {
                    registerSkillCooldown(cooldown.skillId, cooldown.getLeftTime(), true);
                }
            }
        } else {
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM skills_cooldowns WHERE charid = ?");
                ps.setInt(1, getId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getLong("length") + rs.getLong("StartTime") - System.currentTimeMillis() <= 0) {
                        continue;
                    }
                    registerSkillCooldown(rs.getInt("SkillID"), rs.getLong("StartTime"), rs.getLong("length"));
                }
                ps.close();
                rs.close();
                deleteWhereCharacterId(con, "DELETE FROM skills_cooldowns WHERE charid = ?");
            } catch (SQLException e) {
                System.err.println("Error while retriving cooldown from SQL storage");
            }
        }
    }

    public List<MapleCoolDownValueHolder> getCooldowns() {
        return getCooldowns(false, false);
    }

    public List<MapleCoolDownValueHolder> getCooldowns(boolean reduce) {
        return getCooldowns(reduce, false);
    }

    public List<MapleCoolDownValueHolder> getCooldowns(boolean reduce, boolean cancel) {
        List<MapleCoolDownValueHolder> ret = new ArrayList<>();
        cooldownLock.lock();
        try {
            for (MapleCoolDownValueHolder mc : skillCooldowns.values()) {
                if (mc != null) {
                    Skill skill = SkillFactory.getSkill(mc.skillId);
                    if (!reduce || skill == null || !skill.isHyperSkill()) {
                        ret.add(mc);
                    }
                }
            }
            return ret;
        } finally {
            this.cooldownLock.unlock();
        }
    }

    public long getCooldownLimit(int skillid) {
        cooldownLock.lock();
        try {
            for (MapleCoolDownValueHolder mcdvh : skillCooldowns.values()) {
                if (mcdvh.skillId == skillid) {
                    return System.currentTimeMillis() - mcdvh.startTime;
                }
            }
            return 0;
        } finally {
            this.cooldownLock.unlock();
        }
    }

    public void resetAllCooldowns(final boolean byskill) {
        getCooldowns(byskill).forEach(it -> cancelSkillCooldown(it.skillId));
    }

    public int getMulungEnergy() {
        return mulung_energy;
    }

    public void mulung_EnergyModify(boolean inc) {
        if (inc) {
            if (mulung_energy + 100 > 10000) {
                mulung_energy = 10000;
            } else {
                mulung_energy += 100;
            }
        } else {
            mulung_energy = 0;
        }
        client.announce(MaplePacketCreator.MulungEnergy(mulung_energy));
    }

    public void writeMapEventEffect(final String inc) {
        client.announce(MaplePacketCreator.showEffect(inc));
    }

    public void writeMulungEnergy() {
        client.announce(MaplePacketCreator.MulungEnergy(mulung_energy));
    }

    public void writeEnergy(String type, String inc) {
        client.announce(MaplePacketCreator.sendPyramidEnergy(type, inc));
    }

    public void writeStatus(String type, String inc) {
        client.announce(MaplePacketCreator.sendGhostStatus(type, inc));
    }

    public void writePoint(String type, String inc) {
        client.announce(MaplePacketCreator.sendGhostPoint(type, inc));
    }

    /*
     * 戰神連擊點數
     */
    public int getAranCombo() {
        return specialStats.getAranCombo();
    }

    public void gainAranCombo(int count, boolean show) {
        setAranCombo(getAranCombo() + count, show);
    }

    public void setAranCombo(int count, boolean show) {
        specialStats.setAranCombo(Math.max(0, Math.min(30000, count)));
        if (show) {
            client.announce(MaplePacketCreator.ShowAranCombo(getAranCombo() + 1));
        }
    }

    public long getKeyDownSkill_Time() {
        return keydown_skill;
    }

    public void setKeyDownSkill_Time(long keydown_skill) {
        this.keydown_skill = keydown_skill;
    }

    /*
     * 惡魔復仇者血之契約
     */
    public void checkBloodContract() {
        if (!JobConstants.is惡魔復仇者(job)) {
            return;
        }
        Skill skill = SkillFactory.getSkill(惡魔復仇者.血之限界);
        int skilllevel = getTotalSkillLevel(skill);
        if (skilllevel >= 1 && map != null) {
            skill.getEffect(skilllevel).applyTo(this);
        }
    }

    /*
     * 設置自由市場顯示的小黑板信息
     */
    public void setMarketChalkboard(String text) {
        if (map == null) {
            return;
        }
        map.broadcastMessage(MTSCSPacket.useChalkboard(getId(), text));
        if (chalkSchedule != null) {
            chalkSchedule.cancel(false);
            chalkSchedule = null;
        }
        chalkSchedule = WorldTimer.getInstance().schedule(() -> setChalkboard(null), 4 * 1000);
    }

    public String getChalkboard() {
        return chalktext;
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
        if (map != null) {
            map.broadcastMessage(MTSCSPacket.useChalkboard(getId(), text));
        }
    }

    public MapleMount getMount() {
        return mount;
    }

    public int[] getWishlist() {
        return wishlist;
    }

    public void setWishlist(int[] wl) {
        this.wishlist = wl;
        changed_wishlist = true;
    }

    public void clearWishlist() {
        for (int i = 0; i < 12; i++) {
            wishlist[i] = 0;
        }
        changed_wishlist = true;
    }

    public int getWishlistSize() {
        int ret = 0;
        for (int i = 0; i < 12; i++) {
            if (wishlist[i] > 0) {
                ret++;
            }
        }
        return ret;
    }

    public int[] getRocks() {
        return rocks;
    }

    public int getRockSize() {
        int ret = 0;
        for (int i = 0; i < 10; i++) {
            if (rocks[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromRocks(int map) {
        for (int i = 0; i < 10; i++) {
            if (rocks[i] == map) {
                rocks[i] = 999999999;
                changed_trocklocations = true;
                break;
            }
        }
    }

    public void addRockMap() {
        if (getRockSize() >= 10) {
            return;
        }
        rocks[getRockSize()] = getMapId();
        changed_trocklocations = true;
    }

    public boolean isRockMap(int id) {
        for (int i = 0; i < 10; i++) {
            if (rocks[i] == id) {
                return true;
            }
        }
        return false;
    }

    public int[] getRegRocks() {
        return regrocks;
    }

    public int getRegRockSize() {
        int ret = 0;
        for (int i = 0; i < 5; i++) {
            if (regrocks[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromRegRocks(int map) {
        for (int i = 0; i < 5; i++) {
            if (regrocks[i] == map) {
                regrocks[i] = 999999999;
                changed_trocklocations = true;
                break;
            }
        }
    }

    public void addRegRockMap() {
        if (getRegRockSize() >= 5) {
            return;
        }
        regrocks[getRegRockSize()] = getMapId();
        changed_trocklocations = true;
    }

    public boolean isRegRockMap(int id) {
        for (int i = 0; i < 5; i++) {
            if (regrocks[i] == id) {
                return true;
            }
        }
        return false;
    }

    public int[] getHyperRocks() {
        return hyperrocks;
    }

    public int getHyperRockSize() {
        int ret = 0;
        for (int i = 0; i < 13; i++) {
            if (hyperrocks[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromHyperRocks(int map) {
        for (int i = 0; i < 13; i++) {
            if (hyperrocks[i] == map) {
                hyperrocks[i] = 999999999;
                changed_trocklocations = true;
                break;
            }
        }
    }

    public void addHyperRockMap() {
        if (getRegRockSize() >= 13) {
            return;
        }
        hyperrocks[getHyperRockSize()] = getMapId();
        changed_trocklocations = true;
    }

    public boolean isHyperRockMap(int id) {
        for (int i = 0; i < 13; i++) {
            if (hyperrocks[i] == id) {
                return true;
            }
        }
        return false;
    }

    /*
     * 藥劑罐系統
     */
    public MaplePotionPot getPotionPot() {
        return potionPot;
    }

    public void setPotionPot(MaplePotionPot p) {
        this.potionPot = p;
    }

    public void dropMessageIfAdmin(int type, String message) {
        if (isAdmin()) {
            dropMessage(type, message);
        }
    }

    public void dropMessage(int type, String message) {
        if (type == -1) {
            client.announce(UIPacket.getTopMsg(message));
        } else if (type == -2) {
            client.announce(PlayerShopPacket.playerInterChat(message, 0, getName())); //0 or what
        } else if (type == -3) {
            client.announce(MaplePacketCreator.getChatText(getId(), message, getName(), isSuperGm(), 0, true, -1)); //1 = hide
        } else if (type == -4) {
            client.announce(MaplePacketCreator.getChatText(getId(), message, getName(), isSuperGm(), 1, true, -1)); //1 = hide
        } else if (type == -5) {
            client.announce(MaplePacketCreator.spouseMessage(UserChatMessageType.getByType(-5), message)); //pink - SPOUSE_MESSAGE 0x06
        } else if (type == -6) {
            client.announce(MaplePacketCreator.spouseMessage(UserChatMessageType.getByType(9), message)); //white bg - SPOUSE_MESSAGE 0x0A
        } else if (type == -7) {
            client.announce(UIPacket.getMidMsg(0, message, false));
        } else if (type == -8) {
            client.announce(UIPacket.getMidMsg(0, message, true));
        } else if (type == -9) {
            client.announce(MaplePacketCreator.showRedNotice(message));//SHOW_STATUS_INFO 0x0B
        } else if (type == -10) {
            client.announce(MaplePacketCreator.getFollowMessage(message));//SPOUSE_MESSAGE 0x0B
        } else if (type == -11) {
            client.announce(MaplePacketCreator.yellowChat(message));//SPOUSE_MESSAGE 0x07
        } else if (type == -12) {
            client.announce(UIPacket.addPopupSay(1540488, 10000, message, ""));
        } else if (type == 11) {
            client.announce(UIPacket.addPopupSay(1540488, 10000, message, ""));
        } else {
            client.announce(MaplePacketCreator.serverNotice(type, message));
        }
    }

    public void dropSpouseMessage(UserChatMessageType type, String message) {
        client.announce(MaplePacketCreator.spouseMessage(type, message));
    }

    /**
     * 用於在遊戲內輸出調試信息
     *
     * @param type    0普通 1警告 2錯誤
     * @param message
     */
    public void dropDebugMessage(int type, String message) {
        dropSpouseMessage(type == 0 ? UserChatMessageType.道具訊息 : type == 1 ? UserChatMessageType.方塊洗洗樂 : type == 2 ? UserChatMessageType.淺紫 : UserChatMessageType.頻道喇叭, message);
    }

    public void dropDebugMessage(String message) {
        dropDebugMessage(3, message);
    }

    public IMaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public void setPlayerShop(IMaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public int getConversation() {
        return inst.get();
    }

    public void setConversation(int inst) {
        this.inst.set(inst);
    }

    public void resetConversation() {
        this.setConversation(0);
    }

    public int getDirection() {
        return insd.get();
    }

    public void setDirection(int inst) {
        this.insd.set(inst);
    }

    public MapleCarnivalParty getCarnivalParty() {
        return carnivalParty;
    }

    public void setCarnivalParty(MapleCarnivalParty party) {
        carnivalParty = party;
    }

    public void addCP(int ammount) {
        totalCP += ammount;
        availableCP += ammount;
    }

    public void useCP(int ammount) {
        availableCP -= ammount;
    }

    public int getAvailableCP() {
        return availableCP;
    }

    public int getTotalCP() {
        return totalCP;
    }

    public void resetCP() {
        totalCP = 0;
        availableCP = 0;
    }

    public void addCarnivalRequest(MapleCarnivalChallenge request) {
        pendingCarnivalRequests.add(request);
    }

    public final MapleCarnivalChallenge getNextCarnivalRequest() {
        return pendingCarnivalRequests.pollLast();
    }

    public void clearCarnivalRequests() {
        pendingCarnivalRequests = new LinkedList<>();
    }

    public void startMonsterCarnival(final int enemyavailable, final int enemytotal) {
        client.announce(MonsterCarnivalPacket.startMonsterCarnival(this, enemyavailable, enemytotal));
    }

    public boolean getCanTalk() {
        return this.canTalk;
    }

    public void canTalk(boolean talk) {
        this.canTalk = talk;
    }

    /*
     * 角色的經驗倍數 是否有經驗卡
     */
    public double getEXPMod() {
        return stats.expCardRate > 0 ? (stats.expCardRate / 100.0) : 1.0;
    }

    /*
     * 角色的爆率倍數 是否有雙倍爆率卡
     */
    public double getDropMod() {
        return stats.dropCardRate > 0 ? (stats.dropCardRate / 100.0) : 1.0;
    }

    public int getACash() {
        return getClient().getACash();
    }

    public void setACash(final int point) {
        getClient().setACash(point);
        playerObservable.update();
    }

    public int getMaplePoints() {
        return getMaplePoints(false);
    }

    public int getMaplePoints(boolean onlyMPoint) {
        return getClient().getMaplePoints(onlyMPoint);
    }

    public int getMileage() {
        return getClient().getMileage();
    }

    public int modifyMileage(final int quantity) {
        return modifyMileage(quantity, 2, false, true, null);
    }

    public int modifyMileage(final int quantity, final int type) {
        return modifyMileage(quantity, type, false, true, null);
    }

    public int modifyMileage(final int quantity, final String log) {
        return modifyMileage(quantity, 2, false, true, log);
    }

    public int modifyMileage(final int quantity, final boolean show) {
        return modifyMileage(quantity, 2, show, true, null);
    }

    public int modifyMileage(final int quantity, final int type, final boolean show, final boolean limitMax, final String log) {
        if (quantity == 0) {
            return 0;
        }
        int itemID = 2431872;
        if (quantity > 0 && getMileage() + quantity < 0) {
            if (show) {
                send(UIPacket.ScriptProgressItemMessage(itemID, "里程已達到上限."));
            }
            return 3;
        }
        playerObservable.update();
        int result = quantity > 0 ? getClient().rechargeMileage(quantity, type, limitMax, log) : getClient().modifyMileage(quantity);
        if (show && result > 0 && result < 3) {
            switch (result) {
                case 1:
                    send(UIPacket.ScriptProgressItemMessage(itemID, "里程已達到每日上限！"));
                    break;
                case 2:
                    send(UIPacket.ScriptProgressItemMessage(itemID, "里程已達到每月上限！"));
                    break;
            }
            return result;
        }
        if (result == 0 && show && quantity != 0) {
            send(UIPacket.ScriptProgressItemMessage(itemID, (quantity > 0 ? "獲得 " : "消耗 ") + Math.abs(quantity) + " 里程！"));
            client.announce(EffectPacket.showSpecialEffect(EffectOpcode.UserEffect_ExpItemConsumed));
        }
        return result;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int p) {
        this.points = p;
    }

    public int getVPoints() {
        return vpoints;
    }

    public void setVPoints(int p) {
        this.vpoints = p;
    }

    public CashShop getCashInventory() {
        return cs;
    }

    public void removeItem(int id, int quantity) {
        MapleInventoryManipulator.removeById(client, ItemConstants.getInventoryType(id), id, quantity, true, false);
        client.announce(EffectPacket.getShowItemGain(id, (short) -quantity, true));
    }

    public void removeAll(int id) {
        removeAll(id, true, false);
    }

    public void removeAll(int itemId, boolean show, boolean checkEquipped) {
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        int possessed = getInventory(type).countById(itemId);
        if (possessed > 0) {
            MapleInventoryManipulator.removeById(getClient(), type, itemId, possessed, true, false);
            if (show) {
                getClient().announce(EffectPacket.getShowItemGain(itemId, (short) -possessed, true));
            }
        }
        if (checkEquipped && (type == MapleInventoryType.EQUIP || type == MapleInventoryType.DECORATION)) { //檢測當前穿戴的裝備
            type = MapleInventoryType.EQUIPPED;
            possessed = getInventory(type).countById(itemId);
            if (possessed > 0) {
                MapleInventoryManipulator.removeById(getClient(), type, itemId, possessed, true, false);
                if (show) {
                    getClient().announce(EffectPacket.getShowItemGain(itemId, (short) -possessed, true));
                }
                equipChanged();
            }
        }
    }

    public void removeItem(int itemId) {
        removeItem(itemId, false);
    }

    public void removeItem(int itemId, boolean show) {
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        if (type == MapleInventoryType.EQUIP || type == MapleInventoryType.DECORATION) { //check equipped
            type = MapleInventoryType.EQUIPPED;
            int possessed = getInventory(type).countById(itemId);
            if (possessed > 0) {
                MapleInventoryManipulator.removeById(getClient(), type, itemId, possessed, true, false);
                if (show) {
                    getClient().announce(EffectPacket.getShowItemGain(itemId, (short) -possessed, true));
                }
                equipChanged();
            }
        }
    }

    public MapleRing getMarriageRing() {
        MapleInventory iv = getInventory(MapleInventoryType.EQUIPPED);
        List<Item> equipped = iv.newList();
        Collections.sort(equipped);
        MapleRing ring, mrings = null;
        for (Item ite : equipped) {
            Equip item = (Equip) ite;
            if (item.getRing() != null) {
                ring = item.getRing();
                ring.setEquipped(true);
                if (mrings == null && 類型.結婚戒指(item.getItemId())) {
                    mrings = ring;
                }
            }
        }
        if (mrings == null) {
            MapleInventoryType[] tps = {MapleInventoryType.EQUIP, MapleInventoryType.DECORATION};
            for (MapleInventoryType tp : tps) {
                iv = getInventory(tp);
                for (Item ite : iv.list()) {
                    Equip item = (Equip) ite;
                    if (item.getRing() != null) {
                        ring = item.getRing();
                        ring.setEquipped(false);
                        if (mrings == null && 類型.結婚戒指(item.getItemId())) {
                            mrings = ring;
                        }
                    }
                }
                if (mrings != null) {
                    break;
                }
            }
        }
        return mrings;
    }

    public Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> getRings(boolean equip) {
        MapleInventory iv = getInventory(MapleInventoryType.EQUIPPED);
        List<Item> equipped = iv.newList();
        Collections.sort(equipped);
        List<MapleRing> crings = new ArrayList<>(), frings = new ArrayList<>(), mrings = new ArrayList<>();
        MapleRing ring;
        for (Item ite : equipped) {
            Equip item = (Equip) ite;
            if (item.getRing() != null && 類型.特效裝備(item.getItemId())) {
                ring = item.getRing();
                ring.setEquipped(true);
                if (equip) {
                    if (類型.戀人裝備(item.getItemId())) {
                        crings.add(ring);
                    } else if (類型.友情裝備(item.getItemId())) {
                        frings.add(ring);
                    } else if (類型.結婚戒指(item.getItemId())) {
                        mrings.add(ring);
                    }
                } else if (crings.isEmpty() && 類型.戀人裝備(item.getItemId())) {
                    crings.add(ring);
                } else if (frings.isEmpty() && 類型.友情裝備(item.getItemId())) {
                    frings.add(ring);
                } else if (mrings.isEmpty() && 類型.結婚戒指(item.getItemId())) {
                    mrings.add(ring);
                }
            }
        }
        if (equip) {
            MapleInventoryType[] tps = {MapleInventoryType.EQUIP, MapleInventoryType.DECORATION};
            for (MapleInventoryType tp : tps) {
                iv = getInventory(tp);
                for (Item ite : iv.list()) {
                    Equip item = (Equip) ite;
                    if (item.getRing() != null && 類型.特效裝備(item.getItemId())) {
                        ring = item.getRing();
                        ring.setEquipped(false);
                        if (類型.戀人裝備(item.getItemId())) {
                            crings.add(ring);
                        } else if (類型.友情裝備(item.getItemId())) {
                            frings.add(ring);
                        } else if (類型.結婚戒指(item.getItemId())) {
                            mrings.add(ring);
                        }
                    }
                }
            }
        }
        frings.sort(new RingComparator());
        crings.sort(new RingComparator());
        mrings.sort(new RingComparator());
        return new Triple<>(crings, frings, mrings);
    }

    public int getFH() {
        MapleFoothold fh = getMap().getFootholds().findBelow(this.getPosition());
        if (fh != null) {
            return fh.getId();
        }
        return 0;
    }

    public boolean canHP(long now) {
        if (lastHPTime + 5000 < now) {
            lastHPTime = now;
            return true;
        }
        return false;
    }

    public boolean canMP(long now) {
        if (lastMPTime + 5000 < now) {
            lastMPTime = now;
            return true;
        }
        return false;
    }

    public boolean canHPRecover(long now) {
        if (stats.hpRecoverTime > 0 && lastHPTime + stats.hpRecoverTime < now) {
            lastHPTime = now;
            return true;
        }
        return false;
    }

    public boolean canMPRecover(long now) {
        if (stats.mpRecoverTime > 0 && lastMPTime + stats.mpRecoverTime < now) {
            lastMPTime = now;
            return true;
        }
        return false;
    }

    public void checkFairy() {
        long startTime = 0;
        boolean inChat = true;
        MapleInventory iv = getInventory(MapleInventoryType.EQUIPPED);
        Item item;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<Integer> toRemove = new LinkedList<>();
        for (int slot : fairys.keySet()) {
            item = iv.getItem((short) -slot);
            if (item == null || ii.getBonusExps(item.getItemId()).isEmpty()) {
                if (!fairys.containsKey(slot)) {
                    startTime = System.currentTimeMillis();
                } else {
                    startTime = fairys.get(slot).getRight();
                }
                toRemove.add(slot);
                send(MaplePacketCreator.fairyPendantMessage(slot % 100, 0, 0, startTime, 0, inChat));
            }
        }
        for (int slot : toRemove) {
            fairys.remove(slot);
        }

        boolean show;
        int stage = 0;
        long time = 0;
        int newStage = 0;
        for (Entry<Integer, List<Integer>> bonusExps : stats.getEquipmentBonusExps().entrySet()) {
            show = false;
            item = iv.getItem((short) -bonusExps.getKey());
            if (item != null && !ii.getBonusExps(item.getItemId()).isEmpty()) {
                if (!fairys.containsKey(bonusExps.getKey())) {
                    stage = 0;
                    startTime = System.currentTimeMillis();
                    time = 0;
                    fairys.put(bonusExps.getKey(), new Pair(stage, startTime));
                    show = true;
                } else {
                    stage = fairys.get(bonusExps.getKey()).getLeft();
                    startTime = fairys.get(bonusExps.getKey()).getRight();
                    time = (System.currentTimeMillis() - startTime) / 60000;
                    newStage = Math.min((int) (time / 60), bonusExps.getValue().size() - 1);
                    fairys.put(bonusExps.getKey(), new Pair(Math.min(newStage, bonusExps.getValue().size()), startTime));
                    if (newStage != stage) {
                        stage = newStage;
                        show = true;
                    }
                }
                if (show) {
                    send(MaplePacketCreator.fairyPendantMessage(bonusExps.getKey() % 100, stage, bonusExps.getValue().get(stage), startTime, time, inChat));
                }
            }
        }
    }

    public boolean canFairy(long now) {
        return lastFairyTime > 0 && lastFairyTime + (60 * 60 * 1000) < now;
    }

    public void doFairy() {
        lastFairyTime = System.currentTimeMillis();
        if (getGuildId() > 0) {
            WorldGuildService.getInstance().gainGP(getGuildId(), 20, id);
            client.announce(UIPacket.getGPContribution(20));
        }
        traits.get(MapleTraitType.will).addExp(5, this); //willpower every hour
    }

    public Map<Integer, Pair<Integer, Long>> getFairys() {
        return fairys;
    }

    public int getTeam() {
        return coconutteam;
    }

    public void setTeam(int v) {
        this.coconutteam = v;
    }

    public void clearLinkMid() {
        linkMobs.clear();
        dispelEffect(SecondaryStat.GuidedBullet);
        dispelEffect(SecondaryStat.ArcaneAim);
    }

    public int getFirstLinkMid() {
        for (Integer lm : linkMobs.keySet()) {
            return lm;
        }
        return 0;
    }

    public Map<Integer, Integer> getAllLinkMid() {
        return linkMobs;
    }

    public void setLinkMid(int lm, int x) {
        linkMobs.put(lm, x);
    }

    public int getDamageIncrease(int lm) {
        if (linkMobs.containsKey(lm)) {
            return linkMobs.get(lm);
        }
        return 0;
    }

    public MapleExtractor getExtractor() {
        return extractor;
    }

    public void setExtractor(MapleExtractor me) {
        removeExtractor();
        this.extractor = me;
    }

    public void removeExtractor() {
        if (extractor != null) {
            map.broadcastMessage(MaplePacketCreator.removeExtractor(this.id));
            map.removeMapObject(extractor);
            extractor = null;
        }
    }

    public Event_PyramidSubway getPyramidSubway() {
        return pyramidSubway;
    }

    public void setPyramidSubway(Event_PyramidSubway ps) {
        this.pyramidSubway = ps;
    }

    /*
     * 0 = 機械師
     * 1 = 冒險家
     * 2 = 騎士團
     * 3 =  狂狼勇士
     * 4 = 龍神
     * 5 = 雙弩精靈
     * 6 = 惡魔獵手  惡魔復仇者
     * 0A = 夜光
     * 0B = 凱撒
     * 0C = 天使破壞者
     * 0E = 尖兵
     */
    public byte getSubcategory() {
        MapleJob mJob = MapleJob.getById(getJobWithSub());
        if (mJob != null && mJob.getSub() != subcategory) {
            subcategory = (byte) mJob.getSub();
        }
        return subcategory;
    }

    public void setSubcategory(int z) {
        this.subcategory = (byte) z;
    }

    public int itemQuantity(int itemid) {
        return getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid);
    }

    public RockPaperScissors getRPS() {
        return rps;
    }

    public void setRPS(RockPaperScissors rps) {
        this.rps = rps;
    }

    public long getNextConsume() {
        return nextConsume - 1000;
    }

    public void setNextConsume(long nc) {
        this.nextConsume = nc;
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public void changeChannel(int channel) {
        ChannelServer toch = ChannelServer.getInstance(channel);
        if (channel == client.getChannel() || toch == null || toch.isShutdown()) {
            client.announce(MaplePacketCreator.serverBlocked(1));
            return;
        }
        initialSpawnPoint();
        changeRemoval();
        ChannelServer ch = ChannelServer.getInstance(client.getChannel());
        if (getMessenger() != null) {
            WorldMessengerService.getInstance().silentLeaveMessenger(getMessenger().getId(), new MapleMessengerCharacter(this));
        }
        PlayerBuffStorage.addBuffsToStorage(getId(), getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(getId(), getCooldowns());
        CharacterTransfer ct = new CharacterTransfer(this, channel);
        World.ChannelChange_Data(ct, getId(), channel);
        ch.removePlayer(this);
        client.updateLoginState(MapleClient.CHANGE_CHANNEL, client.getSessionIPAddress());
        client.announce(MaplePacketCreator.getChannelChange(client, ch.getPort()));
        saveToDB(false, false);
        getMap().userLeaveField(this);
        client.setPlayer(null);
        client.setReceiving(false);
    }

    public void initialSpawnPoint() {
        MaplePortal portal = map.findClosestSpawnpoint(getPosition());
        initialSpawnPoint = (byte) (portal == null ? 0 : portal.getId());
    }

    public void expandInventory(byte type, int amount) {
        MapleInventory inv = getInventory(MapleInventoryType.getByType(type));
        inv.addSlot((byte) amount);
        client.announce(InventoryPacket.updateInventorySlotLimit(type, (byte) inv.getSlotLimit()));
    }

    /**
     * 作用：用於判斷角色是否可以被顯示在地圖中！ 判斷：角色不等於空，角色不是隱身狀態，當前角色管理員等級大於或者等於角色管理員等級
     *
     * @param other 參數 角色
     * @return
     */
    public boolean allowedToTarget(MapleCharacter other) {
        return other != null && (!other.isHidden() || getGmLevel() >= other.getGmLevel());
    }

    public int getFollowId() {
        return followid;
    }

    public void setFollowId(int fi) {
        this.followid = fi;
        if (fi == 0) {
            this.followinitiator = false;
            this.followon = false;
        }
    }

    public boolean isFollowOn() {
        return followon;
    }

    public void setFollowOn(boolean fi) {
        this.followon = fi;
    }

    public boolean isFollowInitiator() {
        return followinitiator;
    }

    public void setFollowInitiator(boolean fi) {
        this.followinitiator = fi;
    }

    public void checkFollow() {
        if (followid <= 0) {
            return;
        }
        if (followon) {
            map.broadcastMessage(MaplePacketCreator.followEffect(id, 0, null));
            map.broadcastMessage(MaplePacketCreator.followEffect(followid, 0, null));
        }
        MapleCharacter tt = map.getPlayerObject(followid);
        client.announce(MaplePacketCreator.getFollowMessage("已停止跟隨。"));
        if (tt != null) {
            tt.setFollowId(0);
            tt.getClient().announce(MaplePacketCreator.getFollowMessage("已停止跟隨。"));
        }
        setFollowId(0);
    }

    public int getMarriageId() {
        return marriageId;
    }

    public void setMarriageId(int mi) {
        this.marriageId = mi;
    }

    public int getMarriageItemId() {
        return marriageItemId;
    }

    public void setMarriageItemId(int mi) {
        this.marriageItemId = mi;
    }

    public boolean startPartyQuest(int questid) {
        boolean ret = false;
        MapleQuest q = MapleQuest.getInstance(questid);
        if (q == null || !q.isPartyQuest()) {
            return false;
        }
        if (!quests.containsKey(questid) || !questinfo.containsKey(questid)) {
            MapleQuestStatus status = getQuestNAdd(q);
            status.setStatus((byte) 1);
            updateQuest(status);
            switch (questid) {
                case 1300: //[競爭活動]納希沙漠競技場
                case 1301: //[競爭活動]怪物嘉年華
                case 1302: //[競爭活動]第2屆怪物嘉年華
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0;gvup=0;vic=0;lose=0;draw=0");
                    break;
                case 1303: //[競爭活動]霧海幽靈船
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;have1=0;rank=F;try=0;cmp=0;CR=0;VR=0;vic=0;lose=0");
                    break;
                case 1204: //[組隊任務]海盜船組隊任務
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;have2=0;have3=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                case 1206: //[組隊任務]毒霧森林
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                default:
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
            }
            ret = true;
        }
        return ret;
    }

    public String getQuestInfo(int questid, String key) {
        return getOneInfo(questid, key);
    }

    public String getOneInfo(int questid, String key) {
        if (!questinfo.containsKey(questid) || key == null || MapleQuest.getInstance(questid) == null) {
            return null;
        }
        String[] split = questinfo.get(questid).split(";");
        for (String x : split) {
            String[] split2 = x.split("="); //should be only 2
            if (split2.length == 2 && split2[0].equals(key)) {
                return split2[1];
            }
        }
        return null;
    }

    public void updateOneQuestInfo(int questid, String key, String value) {
        updateOneInfo(questid, key, value);
    }

    public void updateOneInfo(int questid, String key, String value) {
        updateOneInfo(questid, key, value, true);
    }

    public void updateOneInfo(int questid, String key, String value, boolean show) {
        if (key == null || MapleQuest.getInstance(questid) == null) {
            return;
        }
        if (!questinfo.containsKey(questid)) {
            if (value == null) {
                updateInfoQuest(questid, "", show);
            } else {
                updateInfoQuest(questid, key + "=" + value, show);
            }
            return;
        }
        String[] split = questinfo.get(questid).split(";");
        boolean changed = false;
        StringBuilder newQuest = new StringBuilder();
        for (String x : split) {
            String[] split2 = x.split("="); //should be only 2
            if (split2.length != 2) {
                continue;
            }
            if (split2[0].equals(key)) {
                if (value != null) {
                    newQuest.append(key).append("=").append(value);
                }
                changed = true;
            } else {
                newQuest.append(x);
            }
            newQuest.append(";");
        }
        if (!changed && value != null) {
            newQuest.append(key).append("=").append(value);
        }
        updateInfoQuest(questid, newQuest.toString().endsWith(";") ? newQuest.substring(0, newQuest.toString().length() - 1) : newQuest.toString(), show);
    }

    public Map<Integer, String> getWorldShareInfo() {
        return this.worldShareInfo;
    }

    public void updateWorldShareInfo(int quest, String data) {
        updateWorldShareInfo(quest, data, true);
    }

    public void updateWorldShareInfo(int quest, String data, boolean sent) {
        if (data == null || data.isEmpty()) {
            worldShareInfo.remove(quest);
        } else {
            worldShareInfo.put(quest, data);
        }
        this.changed_worldshareinfo = true;
        if (this.client == null || ShutdownServer.getInstance().isShutdown()) {
            return;
        }
        if (sent) {
            MessageOption option = new MessageOption();
            option.setObjectId(quest);
            option.setText(data == null ? "" : data);
            client.announce(CWvsContext.sendMessage(GameConstants.isWorldShareQuest(quest) ? MessageOpcode.MS_WorldShareRecordMessage : MessageOpcode.MS_QuestRecordExMessage, option));
        }
    }

    public void updateWorldShareInfo(final int quest, final String key, final String value) {
        updateWorldShareInfo(quest, key, value, true);
    }

    public void updateWorldShareInfo(final int quest, final String key, final String value, final boolean sent) {
        if (key == null) {
            return;
        }
        if (!this.worldShareInfo.containsKey(quest)) {
            this.updateWorldShareInfo(quest, value == null ? null : (key + "=" + value), sent);
            return;
        }
        final String[] split = this.worldShareInfo.get(quest).split(";");
        boolean b = false;
        final StringBuilder sb = new StringBuilder();
        for (int length = split.length, i = 0; i < length; ++i) {
            final String s3 = split[i];
            final String[] split2 = (s3).split("=");
            if ((split2).length == 2) {
                if (split2[0].equals(key)) {
                    if (value != null) {
                        sb.append(key).append("=").append(value);
                    }
                    b = true;
                } else {
                    sb.append(s3);
                }
                sb.append(";");
            }
        }
        if (!b && value != null) {
            sb.append(key).append("=").append(value);
        }
        String data = b ? sb.substring(0, sb.toString().length() - 1) : sb.toString();
        this.updateWorldShareInfo(quest, data.isEmpty() ? null : data, sent);
    }

    public String getWorldShareInfo(final int n, final String s) {
        String info = this.worldShareInfo.get(n);
        if (info == null) {
            return null;
        }
        for (String value : info.split(";")) {
            final String[] split2 = value.split("=");
            if (split2.length == 2 && split2[0].equals(s)) {
                return split2[1];
            }
        }
        return null;
    }

    public String getWorldShareInfo(final int n) {
        return this.worldShareInfo.get(n);
    }

    public void recalcPartyQuestRank(int questid) {
        if (MapleQuest.getInstance(questid) == null || !MapleQuest.getInstance(questid).isPartyQuest()) {
            return;
        }
        if (!startPartyQuest(questid)) {
            String oldRank = getOneInfo(questid, "rank");
            if (oldRank == null || oldRank.equals("S")) {
                return;
            }
            String newRank;
            switch (oldRank) {
                case "A":
                    newRank = "S";
                    break;
                case "B":
                    newRank = "A";
                    break;
                case "C":
                    newRank = "B";
                    break;
                case "D":
                    newRank = "C";
                    break;
                case "F":
                    newRank = "D";
                    break;
                default:
                    return;
            }
            List<Pair<String, Pair<String, Integer>>> questInfo = MapleQuest.getInstance(questid).getInfoByRank(newRank);
            if (questInfo == null) {
                return;
            }
            for (Pair<String, Pair<String, Integer>> q : questInfo) {
                boolean found = false;
                String val = getOneInfo(questid, q.right.left);
                if (val == null) {
                    return;
                }
                int vall;
                try {
                    vall = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    return;
                }
                switch (q.left) {
                    case "less":
                        found = vall < q.right.right;
                        break;
                    case "more":
                        found = vall > q.right.right;
                        break;
                    case "equal":
                        found = vall == q.right.right;
                        break;
                }
                if (!found) {
                    return;
                }
            }
            updateOneInfo(questid, "rank", newRank);
        }
    }

    public void tryPartyQuest(int questid) {
        if (MapleQuest.getInstance(questid) == null || !MapleQuest.getInstance(questid).isPartyQuest()) {
            return;
        }
        try {
            startPartyQuest(questid);
            pqStartTime = System.currentTimeMillis();
            updateOneInfo(questid, "try", String.valueOf(Integer.parseInt(getOneInfo(questid, "try")) + 1));
        } catch (Exception e) {
            System.out.println("tryPartyQuest error");
        }
    }

    public void endPartyQuest(int questid) {
        if (MapleQuest.getInstance(questid) == null || !MapleQuest.getInstance(questid).isPartyQuest()) {
            return;
        }
        try {
            startPartyQuest(questid);
            if (pqStartTime > 0) {
                long changeTime = System.currentTimeMillis() - pqStartTime;
                int mins = (int) (changeTime / 1000 / 60), secs = (int) (changeTime / 1000 % 60);
                int mins2 = Integer.parseInt(getOneInfo(questid, "min"));
                if (mins2 <= 0 || mins < mins2) {
                    updateOneInfo(questid, "min", String.valueOf(mins));
                    updateOneInfo(questid, "sec", String.valueOf(secs));
                    updateOneInfo(questid, "date", DateUtil.getCurrentDate());
                }
                int newCmp = Integer.parseInt(getOneInfo(questid, "cmp")) + 1;
                updateOneInfo(questid, "cmp", String.valueOf(newCmp));
                updateOneInfo(questid, "CR", String.valueOf((int) Math.ceil((newCmp * 100.0) / Integer.parseInt(getOneInfo(questid, "try")))));
                recalcPartyQuestRank(questid);
                pqStartTime = 0;
            }
        } catch (Exception e) {
            System.out.println("endPartyQuest error");
        }
    }

    public void havePartyQuest(int itemId) {
        int questid, index = -1;
        switch (itemId) {
            case 1002798:
                questid = 1200; //[組隊任務]迎月花保護月妙組隊任務
                break;
            case 1072369:
                questid = 1201; //[組隊任務]組隊挑戰任務
                break;
            case 1022073:
                questid = 1202; //[組隊任務]玩具城組隊任務
                break;
            case 1082232:
                questid = 1203; //[組隊任務]女神塔組隊任務
                break;
            case 1002571:
            case 1002572:
            case 1002573:
            case 1002574:
                questid = 1204; //[組隊任務]海盜船組隊任務
                index = itemId - 1002571;
                break;
            case 1102226:
                questid = 1303; //[競爭活動]霧海幽靈船
                break;
            case 1102227:
                questid = 1303; //[競爭活動]霧海幽靈船
                index = 0;
                break;
            case 1122010:
                questid = 1205; //[組隊任務]拯救羅密歐和朱麗葉
                break;
            case 1032061:
            case 1032060:
                questid = 1206; //[組隊任務]毒霧森林
                index = itemId - 1032060;
                break;
            case 3010018:
                questid = 1300; //[競爭活動]納希沙漠競技場
                break;
            case 1122007:
                questid = 1301; //[競爭活動]怪物嘉年華
                break;
            case 1122058:
                questid = 1302; //[競爭活動]第2屆怪物嘉年華
                break;
            default:
                return;
        }
        if (MapleQuest.getInstance(questid) == null || !MapleQuest.getInstance(questid).isPartyQuest()) {
            return;
        }
        startPartyQuest(questid);
        updateOneInfo(questid, "have" + (index == -1 ? "" : index), "1");
    }

    public boolean hasSummon() {
        return hasSummon;
    }

    public void setHasSummon(boolean summ) {
        this.hasSummon = summ;
    }

    public void removeAllTownPortal() {
        final Iterator<TownPortal> iterator = getTownPortals().iterator();
        while (iterator.hasNext()) {
            TownPortal townPortal = iterator.next();
            for (MapleCharacter chr : townPortal.getFieldMap().getCharacters()) {
                townPortal.sendDestroyData(chr.getClient());
            }
            for (MapleCharacter chr : townPortal.getTownMap().getCharacters()) {
                townPortal.sendDestroyData(chr.getClient());
            }
            for (TownPortal destroyDoor : getTownPortals()) {
                townPortal.getFieldMap().removeMapObject(destroyDoor);
                townPortal.getTownMap().removeMapObject(destroyDoor);
            }
        }
        clearTownPortals();
        townPortalLeaveTime = -1;
    }

    public long getTownPortalLeaveTime() {
        return townPortalLeaveTime;
    }

    public void setTownPortalLeaveTime(final long time) {
        townPortalLeaveTime = time;
    }

    public void checkTownPortalLeave() {
        if (townPortalLeaveTime <= 0) {
            return;
        }
        if (townPortalLeaveTime < System.currentTimeMillis()) {
            removeAllTownPortal();
        }
    }

    public void removeMechDoor() {
        for (MechDoor destroyDoor : getMechDoors()) {
            for (MapleCharacter chr : getMap().getCharacters()) {
                destroyDoor.sendDestroyData(chr.getClient());
            }
            getMap().removeMapObject(destroyDoor);
        }
        clearMechDoors();
    }

    public void changeRemoval() {
        changeRemoval(false);
    }

    public void changeRemoval(boolean dc) {
        if (getCheatTracker() != null && dc) {
            getCheatTracker().dispose();
        }
        dispelSummons();
        if (!dc) {
            dispelEffect(SecondaryStat.Flying);
            dispelEffect(SecondaryStat.RideVehicle);
            dispelEffect(SecondaryStat.Mechanic);
            dispelEffect(SecondaryStat.Regen);
            dispelEffect(SecondaryStat.SpiritLink);
            dispelEffect(SecondaryStat.NewFlying);
        }
        if (getPyramidSubway() != null) {
            getPyramidSubway().dispose(this);
        }
        if (playerShop != null && !dc) {
            playerShop.removeVisitor(this);
            if (playerShop.isOwner(this)) {
                playerShop.setOpen(true);
            }
        }
        if (!getTownPortals().isEmpty()) {
            removeAllTownPortal();
        }
        if (!getMechDoors().isEmpty()) {
            removeMechDoor();
        }
        if (this.map != null && this.summonedFamiliar != null) {
            this.map.disappearMapObject(this.summonedFamiliar);
        }
//        NPCScriptManager.dispose(client);
        if (eventInstance != null) {
            eventInstance.getHooks().playerDisconnected(this);
//            eventInstance.playerDisconnected(this, getId());
        }

    }

    //------------------------------------------------------------------------------------
    public void updateTick(int newTick) {
        anticheat.updateTick(newTick);
    }

    // 用於防止點擊NPC過快掉線-----------------------------------------------------------
    public long getCurrenttime() {
        return this.currenttime;
    }

    public void setCurrenttime(final long currenttime) {
        this.currenttime = currenttime;
    }

    public long getDeadtime() {
        return this.deadtime;
    }

    public void setDeadtime(final long deadtime) {
        this.deadtime = deadtime;
    }

    public long getLasttime() {
        return this.lasttime;
    }

    public void setLasttime(final long lasttime) {
        this.lasttime = lasttime;
    }

    public String getTeleportName() {
        return teleportname;
    }

    public void setTeleportName(String tname) {
        teleportname = tname;
    }

    public int getGachExp() {
        return gachexp;
    }

    public void setGachExp(int ge) {
        gachexp = ge;
    }

    public boolean isInBlockedMap() {
        if (!isAlive() || getPyramidSubway() != null || checkEvent() || getMap().getEMByMap(this)) {
            return true;
        }
        if ((getMapId() >= 680000210 && getMapId() <= 680000502) || getMapId() / 10000 == 925070000 / 10000 || getMapId() == GameConstants.JAIL) {
            return true;
        }
        return ServerConstants.isBlockedMapFM(getMapId());
    }

    public boolean isInTownMap() {
        if (hasBlockedInventory() || !getMap().isTown() || FieldLimitType.TELEPORTITEMLIMIT.check(getMap().getFieldLimit()) || checkEvent()) {
            return false;
        }
        return !ServerConstants.isBlockedMapFM(getMapId());
    }

    public boolean hasBlockedInventory() {
        return !isAlive() || getTrade() != null || getConversation() > 0 || getDirection() >= 0 || getPlayerShop() != null || map == null;
    }

    public int getChallenge() {
        return challenge;
    }

    public void setChallenge(int c) {
        this.challenge = c;
    }

    public short getFatigue() {
        return fatigue;
    }

    public void setFatigue(int j) {
        this.fatigue = (short) Math.max(0, j);
        updateSingleStat(MapleStat.疲勞, this.fatigue);
    }

    public void fakeRelog() {
        MapleMap mapp = getMap();
        stats.recalcLocalStats(this);
        mapp.userLeaveField(this);
        mapp.userEnterField(this);
        client.announce(MaplePacketCreator.getWarpToMap(this, map, 0, false));
        client.announce(MaplePacketCreator.serverNotice(5, "刷新人數據完成..."));
    }

    /*
     * 檢測幻靈重生是否能召喚怪物
     */
    public boolean canSummon() {
        return canSummon(5000);
    }

    public boolean canSummon(int checkTime) {
        if (lastSummonTime <= 0) {
            prepareSummonTime();
        }
        return lastSummonTime + checkTime < System.currentTimeMillis();
    }

    /*
     * 重置幻靈召喚獸時間間隔
     */
    private void prepareSummonTime() {
        lastSummonTime = System.currentTimeMillis();
    }

    public int getIntNoRecord(int questID) {
        MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(questID));
        if (stat == null || stat.getCustomData() == null) {
            return 0;
        }
        return Integer.parseInt(stat.getCustomData());
    }

    public int getIntRecord(int questID) {
        MapleQuestStatus stat = getQuestNAdd(MapleQuest.getInstance(questID));
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        return Integer.parseInt(stat.getCustomData());
    }

    public long getLongRecord(int questID) {
        MapleQuestStatus stat = getQuestNAdd(MapleQuest.getInstance(questID));
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        return Long.parseLong(stat.getCustomData());
    }

    public void setLongRecord(int questID, long record) {
        MapleQuestStatus stat = getQuestNAdd(MapleQuest.getInstance(questID));
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        stat.setCustomData(String.valueOf(record));
    }

    public void updatePetAuto() {
        client.announce(MaplePacketCreator.petAutoHP(getIntRecord(GameConstants.HP_ITEM))); // 2655
        client.announce(MaplePacketCreator.petAutoMP(getIntRecord(GameConstants.MP_ITEM))); // 2656
        client.announce(MaplePacketCreator.petAutoBuff(getIntRecord(GameConstants.BUFF_SKILL))); // 2657
    }

    public void setChangeTime() {
        getCheatTracker().resetInMapTime();
    }

    public Map<ReportType, Integer> getReports() {
        return reports;
    }

    public void addReport(ReportType type) {
        Integer value = reports.get(type);
        reports.put(type, value == null ? 1 : (value + 1));
        changed_reports = true;
    }

    public void clearReports(ReportType type) {
        reports.remove(type);
        changed_reports = true;
    }

    public void clearReports() {
        reports.clear();
        changed_reports = true;
    }

    public int getReportPoints() {
        int ret = 0;
        for (Integer entry : reports.values()) {
            ret += entry;
        }
        return ret;
    }

    public String getReportSummary() {
        StringBuilder ret = new StringBuilder();
        List<Pair<ReportType, Integer>> offenseList = new ArrayList<>();
        for (Entry<ReportType, Integer> entry : reports.entrySet()) {
            offenseList.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        offenseList.sort((o1, o2) -> {
            int thisVal = o1.getRight();
            int anotherVal = o2.getRight();
            return (thisVal < anotherVal ? 1 : (thisVal == anotherVal ? 0 : -1));
        });
        for (Pair<ReportType, Integer> anOffenseList : offenseList) {
            ret.append(StringUtil.makeEnumHumanReadable(anOffenseList.left.name()));
            ret.append(": ");
            ret.append(anOffenseList.right);
            ret.append(" ");
        }
        return ret.toString();
    }

    public short getScrolledPosition() {
        return scrolledPosition;
    }

    public void setScrolledPosition(short s) {
        this.scrolledPosition = s;
    }

    public MapleTrait getTrait(MapleTraitType t) {
        return traits.get(t);
    }

    public void forceCompleteQuest(int id) {
        MapleQuest.getInstance(id).forceComplete(this, 9270035); //NPC喬伊斯
    }

    /*
     * 礦物(藥草)背包
     */
    public Map<Byte, List<Item>> getAllExtendedSlots() {
        return extendedSlots;
    }

    public List<Item> getExtendedSlots(byte type) {
        return extendedSlots.get(type);
    }

    public int getExtendedItemId(byte type, int slot) {
        if (extendedSlots == null || extendedSlots.isEmpty() || extendedSlots.get(type) == null) {
            return -1;
        }
        for (Item itm : extendedSlots.get(type)) {
            if (itm.getExtendSlot() == slot) {
                return itm.getItemId();
            }
        }
        return -1;
    }

    /*
     * 定義機器人
     */
    public MapleAndroid getAndroid() {
        return android;
    }

    /*
     * 刷出機器人
     */
    public void setAndroid(MapleAndroid a) {
        if (checkHearts()) {
            android = a;
            if (map != null && a != null) {
                map.broadcastMessage(AndroidPacket.spawnAndroid(this, a));
                android.showEmotion(this, "hello");
            }
        }
    }

    /*
     * 移除機器人
     */
    public void removeAndroid() {
        if (map != null) {
            if (android != null) {
                android.showEmotion(this, "bye");
            }
            map.broadcastMessage(AndroidPacket.deactivateAndroid(this.id));
        }
        if (android != null) {
            android.saveToDb();
        }
        android = null;
    }

    /*
     * 更新機器人外觀
     */
    public void updateAndroidLook() {
        if (map != null) {
            map.broadcastMessage(AndroidPacket.updateAndroidLook(this));
        }
    }

    public void updateAndroidEquip(boolean unequip, Pair<Integer, Integer> item) {
        if (map != null) {
            map.broadcastMessage(AndroidPacket.updateAndroidEquip(this.getId(), unequip, item));
        }
    }

    /*
     * 檢測是否有機器人心臟 機器人 -32 機器人心臟 -33
     */
    public boolean checkHearts() {
        return getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -33) != null;
    }

    /*
     * 回購道具
     */
    public List<MapleShopItem> getRebuy() {
        return rebuy;
    }

    public List<MonsterFamiliar> getFamiliars() {
        changed_familiars = true;
        return familiars;
    }

    public MonsterFamiliar getSummonedFamiliar() {
        return summonedFamiliar;
    }

    public MonsterFamiliar removeFamiliarsInfo(final int n) {
        MonsterFamiliar mf = familiars.stream().filter(f -> f.getId() == n).findAny().orElse(null);
        if (mf != null) familiars.remove(mf);
        return mf;
    }

    public void addFamiliarsInfo(final MonsterFamiliar monsterFamiliar) {
        changed_familiars = true;
        familiars.add(monsterFamiliar);
    }

    public void updateFamiliar(MonsterFamiliar familiar) {
        setFamiliarsChanged(true);
        if (client != null) {
            client.write(OverseasPacket.extraEquipResult(ExtraEquipResult.updateFamiliarInfo(id, familiars.size(),
                    familiars.contains(familiar) ?
                            Collections.singletonMap(familiars.indexOf(familiar), familiar)
                            : Collections.emptyMap())));
        }
    }

    public void updateFamiliars() {
        setFamiliarsChanged(true);
        if (client != null) {
            client.write(OverseasPacket.extraEquipResult(ExtraEquipResult.updateFamiliarInfo(id, familiars.size(), familiars.stream()
                    .collect(Collectors.toMap(familiars::indexOf, Function.identity())))));
        }
    }

    public void spawnFamiliar(final MonsterFamiliar monsterFamiliar) {
        boolean old = false;
        if (summonedFamiliar != null && summonedFamiliar != monsterFamiliar) {
            summonedFamiliar.setSummoned(false);
            if (map != null) {
                map.disappearMapObject(this.summonedFamiliar);
            }
            old = true;
        }
        summonedFamiliar = monsterFamiliar;
        summonedFamiliar.setSummoned(true);
        summonedFamiliar.initPad();
        summonedFamiliar.setStance(0);
        summonedFamiliar.setPosition(this.getPosition());
        summonedFamiliar.setCurrentFh(this.getCurrentFH());
        setFamiliarsChanged(true);
        if (map != null) {
            write(OverseasPacket.extraEquipResult(ExtraEquipResult.spawnFamiliar(accountid, id, old, monsterFamiliar, getPosition(), true)));
            map.spawnMapObject(getId(), summonedFamiliar, OverseasPacket.extraEquipResult(ExtraEquipResult.spawnFamiliar(accountid, id, old, monsterFamiliar, getPosition(), false)).getData());
        }
    }

    public void initFamiliar(final MonsterFamiliar cbr) {
        if (summonedFamiliar != null) {
            summonedFamiliar.setSummoned(false);
        }
        summonedFamiliar = cbr;
        summonedFamiliar.setSummoned(true);
    }

    public void removeFamiliar() {
        if (summonedFamiliar != null) {
            summonedFamiliar.setSummoned(false);
            if (map != null) {
                map.disappearMapObject(summonedFamiliar);
            }
        }
        summonedFamiliar = null;
        write(OverseasPacket.extraEquipResult(ExtraEquipResult.removeFamiliar(getId(), true)));
    }

    /*
     * 道具寶寶
     */
    public MapleImp[] getImps() {
        return imps;
    }

    public void sendImp() {
        for (int i = 0; i < imps.length; i++) {
            if (imps[i] != null) {
                client.announce(MaplePacketCreator.updateImp(imps[i], ImpFlag.SUMMONED.getValue(), i, true));
            }
        }
    }

    public int getBattlePoints() {
        return pvpPoints;
    }

    public void setBattlePoints(int p) {
        if (p != pvpPoints) {
            client.announce(UIPacket.getBPMsg(p - pvpPoints));
            updateSingleStat(MapleStat.BATTLE_POINTS, p);
        }
        this.pvpPoints = p;
    }

    public int getTotalBattleExp() {
        return pvpExp;
    }

    public void setTotalBattleExp(int p) {
        int previous = pvpExp;
        this.pvpExp = p;
        if (p != previous) {
            stats.recalcPVPRank(this);
            updateSingleStat(MapleStat.BATTLE_EXP, stats.pvpExp);
            updateSingleStat(MapleStat.BATTLE_RANK, stats.pvpRank);
        }
    }

    public void changeTeam(int newTeam) {
        this.coconutteam = newTeam;
    }

    public boolean inPVP() {
        return eventInstance != null && eventInstance.getScriptName().startsWith("PVP") && ServerConfig.CHANNEL_OPENPVP;
    }

    public void applyIceGage(int x) {
        updateSingleStat(MapleStat.ICE_GAGE, x);
    }

    public Rectangle getBounds() {
        return new Rectangle(getPosition().x - 25, getPosition().y - 37, 50, 75);
    }

    /**
     * 惡魔獵手吸收精氣
     */
    public void handleForceGain(int forceGain) {
//        handleForceGain(oid, skillid, 0);
        if (isSkillCooling(惡魔殺手.高貴血統)) {
            allreadyForceGet += forceGain;
            if (allreadyForceGet >= 50) {
                allreadyForceGet -= 50;
                reduceSkillCooldown(惡魔殺手.高貴血統, 3000);
            }
        } else {
            allreadyForceGet = 0;
        }
    }

    public void handleForceGain(int moboid, int skillid, int extraForce) {
        if (!SkillConstants.isForceIncrease(skillid) && extraForce <= 0) {
            return;
        }
        int forceColor = 3;
        int forceGain = 1;
        if (this.getLevel() >= 30 && this.getLevel() < 70) {
            forceGain = 2;
        } else if (this.getLevel() >= 70 && this.getLevel() < 120) {
            forceGain = 3;
        } else if (this.getLevel() >= 120) {
            forceGain = 4;
        }
        MapleMonster mob = getMap().getMonsterByOid(moboid);
        if (mob != null && mob.getStats().isBoss()) {
//            forceGain = 10;
            forceColor = 10;
        } else {
            if (skillid == 惡魔殺手.惡魔狂斬 || skillid == 惡魔殺手.惡魔狂斬1 || skillid == 惡魔殺手.惡魔狂斬2 || skillid == 惡魔殺手.惡魔狂斬3) {
                int skilllevel = getSkillLevel(惡魔殺手.強化惡魔之力);
                if (skilllevel > 0) {
                    MapleStatEffect effect = SkillFactory.getSkill(惡魔殺手.強化惡魔之力).getEffect(skilllevel);
                    if (Randomizer.nextInt(100) > effect.getProp()) {
                        return;
                    }
                }
            } else if (skillid == 惡魔殺手.死亡詛咒) {
                forceColor = 5;
            }
        }
        forceGain = extraForce > 0 ? extraForce : forceGain;
        addDemonMp(forceGain);
        if (isSkillCooling(惡魔殺手.高貴血統)) {
            allreadyForceGet += forceGain;
            if (allreadyForceGet >= 50) {
                allreadyForceGet = 0;
                reduceSkillCooldown(惡魔殺手.高貴血統, 3000);
            }
        } else {
            allreadyForceGet = 0;
        }
        final MapleForceAtom force = new MapleForceAtom();
        force.setForceType(MapleForceType.惡魔DF.ordinal());
        force.setFromMob(moboid > 0);
        force.setFromMobOid(moboid);
        force.setInfo(MapleForceFactory.getInstance().getForceInfo_惡魔DF(this, 1, forceColor));
        send(ForcePacket.forceAtomCreate(force));
    }

    /*
     * 幻影卡片系統
     */
    public int getCardStack() {
        return specialStats.getCardStack();
    }

    public void setCardStack(int amount) {
        specialStats.setCardStack(amount);
    }

    /*
     * 幻影卡片數量
     */
    public int getCarteByJob() {
        if (getSkillLevel(幻影俠盜.審判) > 0) {
            return 40;
        } else if (getSkillLevel(幻影俠盜.卡牌審判) > 0) {
            return 20;
        }
        return 0;
    }

    public void handleCarteGain(int moid, boolean is5th) {
        if (is5th) {
            Skill skill = SkillFactory.getSkill(幻影俠盜.鬼牌_1);
            if (skill != null && getSkillLevel(幻影俠盜.命運鬼牌) > 0 && (skill.getEffect(getSkillLevel(幻影俠盜.命運鬼牌))) != null) {
//                incJudgementStack();
//                updateJudgementStack();
                send(ForcePacket.forceAtomCreate(MapleForceFactory.getInstance().getMapleForce(this, getSkillEffect(幻影俠盜.命運鬼牌), 0)));
            }
        } else {
            int[] arrn2 = new int[]{幻影俠盜.死神卡牌, 幻影俠盜.炫目卡牌};
            for (int skillid : arrn2) {
                Skill skill = SkillFactory.getSkill(skillid);
                if (skill != null && getSkillLevel(skill) > 0) {
                    MapleStatEffect effect = skill.getEffect(getSkillLevel(skill));
                    if (effect.makeChanceResult() && Randomizer.nextInt(100) <= getStat().getCriticalRate()) {
                        send(ForcePacket.forceAtomCreate(MapleForceFactory.getInstance().getMapleForce(this, getSkillEffect(幻影俠盜.鬼牌_1), 0, Collections.singletonList(moid))));
                        if (getCardStack() < getCarteByJob()) {
                            incJudgementStack();
                            updateJudgementStack();
                            return;
                        }
                    }
                }
            }
        }
    }

    public int getDecorate() {
        return decorate;
    }

    /*
     * 魔族之紋
     * 惡魔
     * 1012276 - 魔族之紋1 - (無描述)
     * 1012277 - 魔族之紋2 - (無描述)
     * 1012278 - 魔族之紋3 - (無描述)
     * 1012279 - 魔族之紋4 - (無描述)
     * 1012280 - 魔族之紋5 - (無描述)
     * 尖兵
     * 1012361 - 乾淨的臉 - (無描述)
     * 1012363 - 生成標記 - (無描述)
     */
    public void setDecorate(int id) {
        if ((id >= 1012276 && id <= 1012280) || id == 1012361 || id == 1012363 || id == 1012693) {
            this.decorate = id;
        } else {
            this.decorate = 0;
        }
    }

    /*
     * 獲取挑戰BOSS的次數設置
     */
    public int getBossLog(String boss) {
        return getBossLog(boss, 0);
    }

    public int getBossLog(String boss, int type) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            int count = 0;
            PreparedStatement ps;
            ps = con.prepareStatement("SELECT * FROM bosslog WHERE characterid = ? AND bossid = ?");
            ps.setInt(1, id);
            ps.setString(2, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                /*
                 * 年：calendar.get(Calendar.YEAR)
                 * 月：calendar.get(Calendar.MONTH)+1
                 * 日：calendar.get(Calendar.DAY_OF_MONTH)
                 * 星期：calendar.get(Calendar.DAY_OF_WEEK)-1
                 */
                count = rs.getInt("count");
                if (count < 0) {
                    return count;
                }
                Timestamp bossTime = rs.getTimestamp("time");
                rs.close();
                ps.close();
                if (type == 0) {
                    Calendar sqlcal = Calendar.getInstance();
                    if (bossTime != null) {
                        sqlcal.setTimeInMillis(bossTime.getTime());
                    }
                    if (sqlcal.get(Calendar.DAY_OF_MONTH) + 1 <= Calendar.getInstance().get(Calendar.DAY_OF_MONTH) || sqlcal.get(Calendar.MONTH) + 1 <= Calendar.getInstance().get(Calendar.MONTH) || sqlcal.get(Calendar.YEAR) + 1 <= Calendar.getInstance().get(Calendar.YEAR)) {
                        count = 0;
                        ps = con.prepareStatement("UPDATE bosslog SET count = 0, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND bossid = ?");
                        ps.setInt(1, id);
                        ps.setString(2, boss);
                        ps.executeUpdate();
                    }
                }
            } else {
                PreparedStatement psu = con.prepareStatement("INSERT INTO bosslog (characterid, bossid, count, type) VALUES (?, ?, ?, ?)");
                psu.setInt(1, id);
                psu.setString(2, boss);
                psu.setInt(3, 0);
                psu.setInt(4, type);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
            log.error("獲取BOSS挑戰次數.", Ex);
            return -1;
        }
    }

    /*
     * 增加挑戰BOSS的次數設置
     */
    public void setBossLog(String boss) {
        setBossLog(boss, 0);
    }

    public void setBossLog(String boss, int type) {
        setBossLog(boss, type, 1);
    }

    public void setBossLog(String boss, int type, int count) {
        int bossCount = getBossLog(boss, type);
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE bosslog SET count = ?, type = ?, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND bossid = ?");
            ps.setInt(1, bossCount + count);
            ps.setInt(2, type);
            ps.setInt(3, id);
            ps.setString(4, boss);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
            log.error("Error while set bosslog.", Ex);
        }
    }

    /*
     * 重置挑戰BOSS的次數設置
     */
    public void resetBossLog(String boss) {
        resetBossLog(boss, 0);
    }

    public void resetBossLog(String boss, int type) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE bosslog SET count = ?, type = ?, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND bossid = ?");
            ps.setInt(1, 0);
            ps.setInt(2, type);
            ps.setInt(3, id);
            ps.setString(4, boss);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
            log.error("重置BOSS次數失敗.", Ex);
        }
    }

    public int getBossLogAcc(String boss) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            int count = 0;
            PreparedStatement ps;
            ps = con.prepareStatement("SELECT * FROM bosslog WHERE accountid = ? AND bossid = ?");
            ps.setInt(1, accountid);
            ps.setString(2, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                /*
                 * 年：calendar.get(Calendar.YEAR)
                 * 月：calendar.get(Calendar.MONTH)+1
                 * 日：calendar.get(Calendar.DAY_OF_MONTH)
                 * 星期：calendar.get(Calendar.DAY_OF_WEEK)-1
                 */
                count = rs.getInt("count");
                if (count < 0) {
                    return count;
                }
                Timestamp bossTime = rs.getTimestamp("time");
                rs.close();
                ps.close();
                Calendar sqlcal = Calendar.getInstance();
                if (bossTime != null) {
                    sqlcal.setTimeInMillis(bossTime.getTime());
                }
                if (sqlcal.get(Calendar.DAY_OF_MONTH) + 1 <= Calendar.getInstance().get(Calendar.DAY_OF_MONTH) || sqlcal.get(Calendar.MONTH) + 1 <= Calendar.getInstance().get(Calendar.MONTH) || sqlcal.get(Calendar.YEAR) + 1 <= Calendar.getInstance().get(Calendar.YEAR)) {
                    count = 0;
                    ps = con.prepareStatement("UPDATE bosslog SET count = 0, time = CURRENT_TIMESTAMP() WHERE accountid = ? AND bossid = ?");
                    ps.setInt(1, accountid);
                    ps.setString(2, boss);
                    ps.executeUpdate();
                }
            } else {
                PreparedStatement psu = con.prepareStatement("INSERT INTO bosslog (accountid, characterid, bossid, count) VALUES (?, ?, ?, ?)");
                psu.setInt(1, accountid);
                psu.setInt(2, 0);
                psu.setString(3, boss);
                psu.setInt(4, 0);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
            log.error("獲取BOSS挑戰次數.", Ex);
            return -1;
        }
    }

    public void setBossLogAcc(String bossid) {
        setBossLogAcc(bossid, 0);
    }

    public void setBossLogAcc(String bossid, int bossCount) {
        bossCount += getBossLogAcc(bossid);
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE bosslog SET count = ?, characterid = ?, time = CURRENT_TIMESTAMP() WHERE accountid = ? AND bossid = ?");
            ps.setInt(1, bossCount + 1);
            ps.setInt(2, id);
            ps.setInt(3, accountid);
            ps.setString(4, bossid);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
            log.error("Error while set bosslog.", Ex);
        }
    }

    public int getEventLogForDay(String event) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            int count = 0;
            PreparedStatement ps;
            ps = con.prepareStatement("SELECT * FROM eventforday WHERE eventid = ?");
            ps.setString(1, event);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("count");
                    if (count < 0) {
                        return count;
                    }
                    Timestamp eventTime = rs.getTimestamp("time");
                    rs.close();
                    ps.close();
                    Calendar sqlcal = Calendar.getInstance();
                    if (eventTime != null) {
                        sqlcal.setTimeInMillis(eventTime.getTime());
                    }
                    if (sqlcal.get(Calendar.DAY_OF_MONTH) + 1 <= Calendar.getInstance().get(Calendar.DAY_OF_MONTH) || sqlcal.get(Calendar.MONTH) + 1 <= Calendar.getInstance().get(Calendar.MONTH) || sqlcal.get(Calendar.YEAR) + 1 <= Calendar.getInstance().get(Calendar.YEAR)) {
                        count = 0;
                        ps = con.prepareStatement("UPDATE eventforday SET count = 0, time = CURRENT_TIMESTAMP() WHERE eventid = ?");
                        ps.setString(1, event);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psu = con.prepareStatement("INSERT INTO eventforday (eventid, count) VALUES (?, ?)")) {
                        psu.setString(1, event);
                        psu.setInt(2, 0);
                        psu.executeUpdate();
                    }
                }
            }
            ps.close();
            return count;
        } catch (Exception Ex) {
            log.error("Error while get EventLogForDay.", Ex);
            return -1;
        }
    }

    public void setEventLogForDay(String eventid) {
        setEventLogForDay(eventid, 0);
    }

    public void setEventLogForDay(String eventid, int eventCount) {
        eventCount += getEventLogForDay(eventid);
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE eventforday SET count = ?, time = CURRENT_TIMESTAMP() WHERE eventid = ?")) {
                ps.setInt(1, eventCount + 1);
                ps.setString(2, eventid);
                ps.executeUpdate();
            }
        } catch (Exception Ex) {
            log.error("Error while set EventLogForDay.", Ex);
        }
    }

    public void resetEventLogForDay(String eventid) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE eventforday SET count = ?, time = CURRENT_TIMESTAMP() WHERE eventid = ?")) {
                ps.setInt(1, 0);
                ps.setString(2, eventid);
                ps.executeUpdate();
            }
        } catch (Exception Ex) {
            log.error("Error while reset EventLogForDay.", Ex);
        }
    }

    /*
     * 更新楓點信息
     */
    public void updateCash() {
        client.announce(MaplePacketCreator.showCharCash(this));
    }

    /*
     * 檢測玩家背包空間
     */
    public short getSpace(int type) {
        return getInventory(MapleInventoryType.getByType((byte) type)).getNumFreeSlot();
    }

    public boolean haveSpace(int type) {
        short slot = getInventory(MapleInventoryType.getByType((byte) type)).getNextFreeSlot();
        return (slot != -1);
    }

    public boolean haveSpaceForId(int itemid) {
        short slot = getInventory(ItemConstants.getInventoryType(itemid)).getNextFreeSlot();
        return (slot != -1);
    }

    public boolean canHold() {
        for (int i = 1; i <= 6; i++) {
            if (getInventory(MapleInventoryType.getByType((byte) i)).getNextFreeSlot() <= -1) {
                return false;
            }
        }
        return true;
    }

    public boolean canHoldSlots(int slot) {
        for (int i = 1; i <= 6; i++) {
            if (getInventory(MapleInventoryType.getByType((byte) i)).isFull(slot)) {
                return false;
            }
        }
        return true;
    }

    public boolean canHold(int itemid) {
        return getInventory(ItemConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public long getMerchantMeso() {
        long mesos = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM hiredmerch WHERE characterid = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                mesos = rs.getLong("Mesos");
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            log.error("獲取僱傭商店楓幣發生錯誤", se);
        }
        return mesos;
    }

    public void autoban(String reason, int greason) {
        /*
         * 年：calendar.get(Calendar.YEAR) 月：calendar.get(Calendar.MONTH)+1
         * 日：calendar.get(Calendar.DAY_OF_MONTH)
         * 星期：calendar.get(Calendar.DAY_OF_WEEK)-1
         */
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 3, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        Timestamp TS = new Timestamp(cal.getTimeInMillis());
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?");
            ps.setString(1, reason);
            ps.setTimestamp(2, TS);
            ps.setInt(3, greason);
            ps.setInt(4, accountid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("Error while autoban" + e);
        }
    }

    public boolean isBanned() {
        return isbanned;
    }

    public void sendPolice(int greason, String reason, int duration) {
        //announce(LoginPacket.sendPolice(greason, reason, duration));
        this.isbanned = true;
        WorldTimer.getInstance().schedule(() -> client.disconnect(true, false), duration);
    }

    public void sendPolice(String text) {
        client.announce(MaplePacketCreator.sendPolice(text));
        this.isbanned = true;
        WorldTimer.getInstance().schedule(() -> {
            client.disconnect(true, false);
            if (client.getSession().isActive()) {
                client.getSession().close();
            }
        }, 6000);
    }

    /*
     * 獲取角色創建的日期
     */
    public Timestamp getChrCreated() {
        if (createDate != null) {
            return createDate;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT createdate FROM characters WHERE id = ?");
            ps.setInt(1, this.getId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            createDate = rs.getTimestamp("createdate");
            rs.close();
            ps.close();
            return createDate;
        } catch (SQLException e) {
            log.error("獲取角色創建日期出錯", e);
            return new Timestamp(-1);
        }
    }

    /*
     * 檢測非GM角色是否在監獄地圖
     */
    public boolean isInJailMap() {
        return getMapId() == GameConstants.JAIL && !isGm();
    }

    /*
     * 角色警告次數
     */
    public int getWarning() {
        return warning;
    }

    public void setWarning(int warning) {
        this.warning = warning;
    }

    public void gainWarning(boolean warningEnabled) {
        this.warning += 1;
        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] 截至目前玩家: " + getName() + " (等級 " + getLevel() + ") 該用戶已被警告: " + warning + " 次！"));
        if (warningEnabled) {
            if (warning == 1) { //警告次數1
                dropMessage(5, "這是你的第一次警告！請注意在遊戲中勿使用非法程序！");
            } else if (warning == 2) { //警告次數2
                dropMessage(5, "警告現在是第 " + warning + " 次。如果你再得到一次警告就會封號處理！");
            } else if (warning >= 3) { //警告次數3
                ban(getName() + " 由於警告次數超過: " + warning + " 次，系統對其封號處理！", false, true, false);
                WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(0, "[系統封號] 玩家 " + getName() + " (等級 " + getLevel() + ") 由於警告次數過多，系統對其封號處理！"));
            }
        }
    }

    /*
     * 豆豆信息
     */
    public int getBeans() {
        return beans;
    }

    public void setBeans(int i) {
        this.beans = i;
    }

    public void gainBeans(int i, boolean show) {
        this.beans += i;
        if (show && i != 0) {
            dropMessage(-1, "您" + (i > 0 ? "獲得了 " : "消耗了 ") + Math.abs(i) + " 個豆豆.");
        }
    }

    /*
     * 傳授技能
     */
    public int teachSkill(int skillId, int toChrId, boolean notime) {
        int[] tSkills = SkillConstants.getTeamTeachSkills(skillId);
        List<Integer> linkSkills = new LinkedList<>();
        if (tSkills == null) {
            linkSkills.add(skillId);
        } else {
            for (int skill : tSkills) {
                linkSkills.add(skill);
            }
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            Calendar date = Calendar.getInstance();
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            for (int skill : linkSkills) {
                int cid;
                SkillEntry se;
                if (!sonOfLinkedSkills.containsKey(skill)) {
                    continue;
                } else {
                    cid = sonOfLinkedSkills.get(skill).getLeft();
                    se = sonOfLinkedSkills.get(skill).getRight();
                    if (!notime) {
                        if (se.expiration <= 0 || se.expiration < date.getTimeInMillis()) {
                            se.expiration = System.currentTimeMillis();
                            se.teachTimes = 0;
                        } else if (se.teachTimes >= ServerConfig.TeachCost.size()) {
                            return -1;
                        }
                        long cost = ServerConfig.TeachCost.get(se.teachTimes);
                        if (getMeso() < cost) {
                            return -1;
                        }
                        if (cost > 0) {
                            gainMeso(-cost, true);
                        }
                    }
                }
                if (tSkills != null && cid != id) {
                    continue;
                }
                int tSkill = SkillConstants.getTeamTeachSkillId(skill);
                if (tSkill > 1) {
                    List<Integer> CIDs = new LinkedList<>();
                    try (PreparedStatement ps = con.prepareStatement("SELECT characterid FROM skills WHERE skillid = ? AND teachId = ?")) {
                        ps.setInt(1, skill);
                        ps.setInt(2, cid);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                CIDs.add(rs.getInt("characterid"));
                            }
                        }
                    }
                    List<Pair<Integer, Integer>> infos = new LinkedList<>();
                    for (int ID : CIDs) {
                        try (PreparedStatement ps = con.prepareStatement("SELECT skilllevel FROM skills WHERE skillid = ? AND characterid = ?")) {
                            ps.setInt(1, tSkill);
                            ps.setInt(2, ID);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    try (PreparedStatement pse = con.prepareStatement("SELECT id FROM characters WHERE id = ? AND accountid = ? AND world = ?")) {
                                        pse.setInt(1, ID);
                                        pse.setInt(2, accountid);
                                        pse.setInt(3, world);
                                        try (ResultSet rse = pse.executeQuery()) {
                                            if (rse.next()) {
                                                infos.add(new Pair(ID, rs.getInt("skilllevel")));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (Pair<Integer, Integer> pair : infos) {
                        pair.right -= se.skillevel;
                        if (pair.right <= 0) {
                            try (PreparedStatement ps = con.prepareStatement("DELETE FROM skills WHERE skillid = ? AND characterid = ?")) {
                                ps.setInt(1, tSkill);
                                ps.setInt(2, pair.getLeft());
                                ps.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement ps = con.prepareStatement("UPDATE skills SET skilllevel = ? WHERE skillid = ? AND characterid = ?")) {
                                ps.setInt(1, pair.getRight());
                                ps.setInt(2, tSkill);
                                ps.setInt(3, pair.getLeft());
                                ps.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM skills WHERE skillid = ? AND teachId = ?")) {
                    ps.setInt(1, skill);
                    ps.setInt(2, cid);
                    ps.executeUpdate();
                }
                int teachId = SkillConstants.getTeachSkillId(skill);
                if (teachId != -1) {
                    if (!notime) {
                        se.expiration = System.currentTimeMillis();
                        se.teachTimes += 1;
                    }
                    se.teachId = notime ? 0 : this.id;
                    try (PreparedStatement ps = con.prepareStatement("UPDATE skills SET expiration = ?, teachId = ?, teachTimes = ? WHERE skillid = ? AND characterid = ?")) {
                        int n5 = 0;
                        ps.setLong(++n5, se.expiration);
                        ps.setInt(++n5, se.teachId);
                        ps.setInt(++n5, se.teachTimes);
                        ps.setInt(++n5, teachId);
                        ps.setInt(++n5, cid);
                        ps.executeUpdate();
                    }
                }
            }
            return 1;
        } catch (Exception Ex) {
            log.error("傳授技能失敗.", Ex);
            return -1;
        }
    }

    /*
     * 轉生系統
     */
    public void giveRebornBuff() {
        Reborn.giveRebornBuff(this);
    }

    public int getReborns() {
        return reborns;
    }

    public int getReborns1() {
        return reborns1;
    }

    public int getReborns2() {
        return reborns2;
    }

    public int getReborns3() {
        return reborns3;
    }

    public void gainReborns(int type) {
        if (type == 0) {
            this.reborns += 1;
        } else if (type == 1) {
            this.reborns1 += 1;
        } else if (type == 2) {
            this.reborns2 += 1;
        } else if (type == 3) {
            this.reborns3 += 1;
        }
    }

    public int getAPS() {
        return apstorage;
    }

    public void gainAPS(int aps) {
        this.apstorage += aps;
    }

    public void doReborn(int type) {
        doReborn(type, -1, true);
    }

    public void doReborn(int type, int ap) {
        doReborn(type, ap, true);
    }

    public void doReborn(int type, int ap, boolean clearSkill) {
        doReborn(type, ap, clearSkill, 4, 50);
    }

    public void doReborn(int type, int ap, boolean clearSkill, int defaultStats, int rebornStat) {
        Map<MapleStat, Long> stat = new EnumMap<>(MapleStat.class);
        if (clearSkill) {
            clearSkills(); //清理技能
        }
        gainReborns(type); //添加轉生數

        setLevel((short) 1); //設置等級為1級
        setExp(0); //設置經驗為0
        setJob(JobConstants.getBeginner(job)); //設置職業為 0轉職業
        resetStats(defaultStats, defaultStats, defaultStats, defaultStats); // 設置初始化屬性點
        stats.setInfo(rebornStat, rebornStat, rebornStat, rebornStat); // 設置轉生初始化血魔

        if (ap == -1) {
            ap = getReborns() * 5 + getReborns1() * 10 + getReborns2() * 15 + getReborns3() * 30;
        }
        setRemainingAp((short) ap);
        stats.recalcLocalStats(this);

        stat.put(MapleStat.AVAILABLEAP, (long) ap);
        stat.put(MapleStat.MAXHP, (long) rebornStat);
        stat.put(MapleStat.MAXMP, (long) rebornStat);
        stat.put(MapleStat.HP, (long) rebornStat);
        stat.put(MapleStat.MP, (long) rebornStat);
        stat.put(MapleStat.等級, (long) 1);
        stat.put(MapleStat.職業, (long) job);
        stat.put(MapleStat.經驗, (long) 0);

        client.announce(MaplePacketCreator.updatePlayerStats(stat, false, this));
    }

    public void clearSkills() {
        Map<Integer, SkillEntry> chrSkill = new HashMap<>(getSkills());
        Map<Integer, SkillEntry> newList = new HashMap<>();
        for (Entry<Integer, SkillEntry> skill : chrSkill.entrySet()) {
            newList.put(skill.getKey(), new SkillEntry((byte) 0, (byte) 0, -1));
        }
        changeSkillsLevel(newList);
        newList.clear();
        chrSkill.clear();
    }

    /*
     * 幻影複製技能
     */
    public Map<Integer, Byte> getSkillsWithPos() {
        Map<Integer, SkillEntry> chrskills = new HashMap<>(getSkills());
        Map<Integer, Byte> skillsWithPos = new LinkedHashMap<>();

        return skillsWithPos;
    }

    public int getStealMemorySkill(int position) {
        for (Entry<Integer, SkillEntry> skill : skills.entrySet()) {
            int pos = skill.getValue().position;
            if (pos >= 0 && pos < 16 && pos == position) {
                return skill.getKey();
            }
        }
        return 0;
    }

    /*
     * 獲取幻影裝備中的技能ID
     */
    public int getEquippedStealSkill(int skillid) {
        SkillEntry ret = skills.get(skillid);
        if (ret == null || ret.teachId == 0) {
            return 0;
        }
        return skills.get(ret.teachId) != null ? ret.teachId : 0;
    }

    /*
     * 裝備或者解除幻影裝備的技能
     */
    public void 修改幻影裝備技能(int skillId, int teachId) {
        SkillEntry ret = skills.get(skillId);
        if (ret != null) {
            Skill theskill = SkillFactory.getSkill(ret.teachId);
            if (theskill != null && theskill.isBuffSkill()) {
                cancelEffect(theskill.getEffect(1), false, -1);
            }
            ret.teachId = teachId;
            changed_skills = true;
            client.announce(MaplePacketCreator.修改幻影裝備技能(skillId, teachId));
        }
    }

    /*
     * 角色卡系統
     */
    public MapleCharacterCards getCharacterCard() {
        return characterCard;
    }

    /*
     * 榮譽系統
     */
    public InnerSkillEntry[] getInnerSkills() {
        return innerSkills;
    }

    public int getInnerSkillSize() {
        int ret = 0;
        for (int i = 0; i < 3; i++) {
            if (innerSkills[i] != null) {
                ret++;
            }
        }
        return ret;
    }

    public int getInnerSkillIdByPos(int position) {
        if (innerSkills[position] != null) {
            return innerSkills[position].getSkillId();
        }
        return 0;
    }

    public int getHonor() {
        return honor;
    }

    public void setHonor(int exp) {
        this.honor = exp;
    }

    public void gainHonor(int amount) {
        if (amount > 0) {
            dropMessage(5, "獲得名聲值:(+" + amount + ")");
        }
        honor += amount;
        client.announce(MaplePacketCreator.updateInnerStats(this));
    }

    /*
     * 檢測角色是否有內在技能
     * 如果沒有自動給予
     */
    public void checkInnerSkill() {
        if (level >= 50 && (innerSkills[0] == null || innerSkills[1] == null || innerSkills[2] == null)) {
            changeInnerSkill(new InnerSkillEntry(70000000, 1, (byte) 1, (byte) 0, false));
            changeInnerSkill(new InnerSkillEntry(70000001, 3, (byte) 2, (byte) 0, false));
            changeInnerSkill(new InnerSkillEntry(70000002, 5, (byte) 3, (byte) 0, true));
        }
    }

    /**
     * 改變角色內在能力的技能
     */
    public void changeInnerSkill(int skillId, int skillevel, int position, int rank) {
        changeInnerSkill(skillId, skillevel, position, rank, false);
    }

    public void changeInnerSkill(int skillId, int skillevel, int position, int rank, boolean replace) {
        if (replace || this.getInnerSkillIdByPos(position) <= 0) {
            this.changeInnerSkill(new InnerSkillEntry(skillId, skillevel, (byte) position, (byte) rank, true));
        }
    }

    /*
     * 改變內在技能（更新所有內在技能）
     */
    public void changeInnerSkill(InnerSkillEntry ise) {
        changed_innerSkills = true;
        // 由於內在技能數組從0開始，需對應位置減1
        innerSkills[ise.getPosition() - 1] = ise;
        // 將所有三筆內在技能資料組合成一個封包一次發送
        client.announce(MaplePacketCreator.updateInnerSkill(client.getPlayer(), innerSkills[0], innerSkills[1], innerSkills[2]));
    }


    public void checkHyperAP() {
        if (level >= 140) {
            Skill skil;
            Map<Integer, SkillEntry> list = new HashMap<>();
            for (int i = 80000400; i <= 80000417; i++) {
                skil = SkillFactory.getSkill(i);
                if (skil != null && getSkillLevel(skil) <= 0) {
                    list.put(i, new SkillEntry((byte) 0, (byte) skil.getMaxLevel(), -1));
                }
            }
            if (!list.isEmpty()) {
                changeSkillsLevel(list);
            }
        }
    }

    /*
     * 獲取角色勳章的名字
     */
    public String getMedalText() {
        String medal = "";
        Item medalItem = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -26);
        if (medalItem != null) {
            medal = "<" + MapleItemInformationProvider.getInstance().getName(medalItem.getItemId()) + "> ";
        }
        return medal;
    }

    /*
     * 新的角色泡點，按帳號計算
     */
    public int getGamePoints() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            int gamePoints = 0;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_info WHERE accId = ? AND worldId = ?");
            ps.setInt(1, getAccountID());
            ps.setInt(2, getWorld());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                gamePoints = rs.getInt("gamePoints");
                Timestamp updateTime = rs.getTimestamp("updateTime");
                Calendar sqlcal = Calendar.getInstance();
                if (updateTime != null) {
                    sqlcal.setTimeInMillis(updateTime.getTime());
                }
                if (sqlcal.get(Calendar.DAY_OF_MONTH) + 1 <= Calendar.getInstance().get(Calendar.DAY_OF_MONTH) || sqlcal.get(Calendar.MONTH) + 1 <= Calendar.getInstance().get(Calendar.MONTH) || sqlcal.get(Calendar.YEAR) + 1 <= Calendar.getInstance().get(Calendar.YEAR)) {
                    gamePoints = 0;
                    PreparedStatement psu = con.prepareStatement("UPDATE accounts_info SET gamePoints = 0, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?");
                    psu.setInt(1, getAccountID());
                    psu.setInt(2, getWorld());
                    psu.executeUpdate();
                    psu.close();
                }
            } else {
                PreparedStatement psu = con.prepareStatement("INSERT INTO accounts_info (accId, worldId, gamePoints) VALUES (?, ?, ?)");
                psu.setInt(1, getAccountID());
                psu.setInt(2, getWorld());
                psu.setInt(3, 0);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return gamePoints;
        } catch (Exception Ex) {
            log.error("獲取角色帳號的在線時間點出現錯誤 - 數據庫查詢失敗", Ex);
            return -1;
        }
    }

    public void gainGamePoints(int amount) {
        int gamePoints = getGamePoints() + amount;
        updateGamePoints(gamePoints);
    }

    public void resetGamePoints() {
        updateGamePoints(0);
    }

    public void updateGamePoints(int amount) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts_info SET gamePoints = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?")) {
                ps.setInt(1, amount);
                ps.setInt(2, getAccountID());
                ps.setInt(3, getWorld());
                ps.executeUpdate();
            }
        } catch (Exception Ex) {
            log.error("更新角色帳號的在線時間出現錯誤 - 數據庫更新失敗.", Ex);
        }
    }

    /**
     * 獲取某個技能的最大傷害上限
     *
     * @param skillId
     * @return
     */
    public long getMaxDamageOver(int skillId) {
        long maxDamage = ServerConfig.DAMAGE_LIMIT; //默認的攻擊上限
        /*    long skillMaxDamage; //技能的攻擊最大上限
        int incMaxDamage = getStat().incMaxDamage; //潛能增加的傷害上限
        Skill skill;
        if (skillId <= 0 || (skill = SkillFactory.getSkill(skillId)) == null) {
            return maxDamage + incMaxDamage;
        }
        if (skillId == 初心者.升級) {
            return maxDamage;
        }
        skillMaxDamage = skill.getMaxDamageOver();
        if (skillId == 暗影神偷.致命暗殺 && getTotalSkillLevel(暗影神偷.致命暗殺_無視防禦) > 0) {
            skillMaxDamage = 60000000;
        } else if (skillId == 夜光.絕對擊殺) {
            skillMaxDamage = maxDamage;
        } else if (skillId == 神射手.必殺狙擊) {
            skillMaxDamage = 200000000;
        } else if (skillId == 神射手.狙殺之擊) {
            skillMaxDamage = 300000000;
        } else if (skillId == 傑諾.偽裝掃蕩_狙擊) {
            skillMaxDamage = 55000000;
        } else if (skillId == 暗影神偷.致命暗殺 || skillId == 暗影神偷.致命暗殺_1) {
            skillMaxDamage = 100000000;
        }*/
        return maxDamage;
//        return skillMaxDamage + incMaxDamage;
    }

    /*
     * 測試傾向系統的經驗
     */
    public void addTraitExp(int exp) {
        traits.get(MapleTraitType.craft).addExp(exp, this);
    }

    public void setTraitExp(int exp) {
        traits.get(MapleTraitType.craft).addExp(exp, this);
    }

    /*
     * 好感度系統
     */
    public int getLove() {
        return love;
    }

    public void setLove(int love) {
        this.love = love;
    }

    public void addLove(int loveChange) {
        this.love += loveChange;
        MessengerRankingWorker.getInstance().updateRankFromPlayer(this);
    }

    public long getLastLoveTime() {
        return lastlovetime;
    }

    public Map<Integer, Long> getLoveCharacters() {
        return lastdayloveids;
    }

    /*
     * 0 = 成功
     * 1 = 未知錯誤
     * 2 = 今天無法增加
     */
    public int canGiveLove(MapleCharacter from) {
        if (from == null || lastdayloveids == null) { //如果要加的對象為空 或者好感度列表為空
            return 1;

        } else if (lastdayloveids.containsKey(from.getId())) { //如果給別人加好感度的列表有有這個玩家
            long lastTime = lastdayloveids.get(from.getId()); //獲取最後給這個玩家加好感度的時間
            if (lastTime >= System.currentTimeMillis() - 60 * 60 * 24 * 1000) {
                return 2;
            } else {
                return 0;
            }
        }
        return 0;
    }

    public void hasGiveLove(MapleCharacter to) {
        lastlovetime = System.currentTimeMillis();
        lastdayloveids.remove(to.getId());
        lastdayloveids.put(to.getId(), System.currentTimeMillis());
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO lovelog (characterid, characterid_to) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("ERROR writing lovelog for char " + getName() + " to " + to.getName() + e);
        }
    }

    /*
     * 角色升級需要的經驗
     */
    public long getExpNeededForLevel() {
        return GameConstants.getExpNeededForLevel(level);
    }

    /*
     * 角色的能力點
     */
    public int getPlayerStats() {
        return getHpApUsed() + getMpApUsed() + stats.getStr() + stats.getDex() + stats.getLuk() + stats.getInt() + getRemainingAp();
    }

    public int getMaxStats() {
        int total = 25;
        //計算專職贈送
        if (!JobConstants.is零轉職業(job)) {
            total += job % 10 == 1 ? 5 : job % 10 == 2 ? 10 : 0;
        }
        total += (level - 1) * 5; //普通職業的計算
        return total;
    }

    public boolean checkMaxStat() {
        return getPlayerStats() > getMaxStats();
    }

    public void resetStats(int str, int dex, int int_, int luk) {
        resetStats(str, dex, int_, luk, true);
    }

    public void resetStats(int str, int dex, int int_, int luk, boolean resetAll) {
        Map<MapleStat, Long> stat = new EnumMap<>(MapleStat.class);
        int total = stats.getStr() + stats.getDex() + stats.getLuk() + stats.getInt() + getRemainingAp(); //這個是直接重置能力點 不需要計算投資到hp的點數
        if (resetAll) {
            total += getHpApUsed();
            useHpAp(-getHpApUsed());
            total += getMpApUsed();
            useMpAp(-getMpApUsed());
        }
        total -= str;
        stats.str = (short) str;
        total -= dex;
        stats.dex = (short) dex;
        total -= int_;
        stats.int_ = (short) int_;
        total -= luk;
        stats.luk = (short) luk;

        if (JobConstants.is管理員(job)) {
            total = 0;
        }
        setRemainingAp((short) total);
        stats.recalcLocalStats(this);

        stat.put(MapleStat.力量, (long) str);
        stat.put(MapleStat.敏捷, (long) dex);
        stat.put(MapleStat.智力, (long) int_);
        stat.put(MapleStat.幸運, (long) luk);
        stat.put(MapleStat.AVAILABLEAP, (long) total);

        client.announce(MaplePacketCreator.updatePlayerStats(stat, false, this));
    }

    /*
     * 對角色進行SP技能初始化
     */
    public void spReset() {
        spReset(true);
    }

    public void spReset(boolean show) {
        spReset(-1, show);
    }

    public void spReset(int resetBeginnerJob) {
        spReset(resetBeginnerJob, true);
    }

    public void spReset(int resetBeginnerJob, boolean show) {
        int skillLevel;
        Map<Integer, SkillEntry> oldList = new HashMap<>(getSkills());
        Map<Integer, SkillEntry> newList = new HashMap<>();
        int teachSkillID = JobConstants.getTeachSkillID(job);
        int sk;
        for (Entry<Integer, SkillEntry> toRemove : oldList.entrySet()) {
            Skill skill = SkillFactory.getSkill(toRemove.getKey());
            sk = -1;
            if ((!skill.isBeginnerSkill() || skill.getId() / 10000 == resetBeginnerJob
                    || ((sk = SkillConstants.getLinkSkillId(skill.getId())) != -1 && teachSkillID != skill.getId()))
                    && (!skill.isSpecialSkill() || (skill.isHyperStat() && resetBeginnerJob != -1))
                    && (!skill.isHyperSkill() || !skill.canBeLearnedBy(getJobWithSub()))
                    && !skill.isVSkill()
            ) {
                if (skill.canBeLearnedBy(getJobWithSub()) && sk == -1) {
                    newList.put(toRemove.getKey(), new SkillEntry((byte) (toRemove.getValue().masterlevel > 0 ? 0 : -1), toRemove.getValue().masterlevel, toRemove.getValue().expiration));
                } else {
                    newList.put(toRemove.getKey(), new SkillEntry((byte) -1, (byte) 0, -1));
                }
            }
        }
        if (!newList.isEmpty()) {
            changeSkillsLevel(newList);
        }
        oldList.clear();
        newList.clear();
        resetSP(0);
        if (JobConstants.is管理員(job)) {
            remainingSp[0] += 11;
        } else if (!JobConstants.is零轉職業(job)) {
            int nJobGrade = JobConstants.getJobGrade(job);
            // 增加轉職SP
            for (int g = 0; g < nJobGrade; g++) {
                Pair<Integer, Integer> sps = MapleCharacter.getJobChangeSP(job, getSubcategory(), g);
                if (sps != null) {
                    remainingSp[sps.getLeft()] += sps.getRight();
                }
            }
            // 增加升等SP
            int jobId;
            if (JobConstants.is神之子(job)) {
                for (int lv = 101; lv <= level; lv++) {
                    int spNum = MapleCharacter.getJobLvSP(job, lv);
                    remainingSp[0] += spNum;
                    remainingSp[1] += spNum;
                    if (lv == 200) {
                        break;
                    }
                }
            } else {
                int[] jobLvs = subcategory == 1 ? new int[]{10, 20, 30, 45, 60, 100} : new int[]{10, 30, 60, 100};
                int g = 0;
                int lvMax;
                for (int lvMin : jobLvs) {
                    if (level < lvMin) {
                        break;
                    }
                    lvMax = jobLvs.length - 1 <= g ? level : jobLvs[g + 1];
                    if (level < lvMax) {
                        lvMax = level;
                    }
                    for (int lv = lvMin + 1; lv <= lvMax; lv++) {
                        int spNum = MapleCharacter.getJobLvSP(subcategory == 1 && g == 0 ? 430 : job, lv);
                        remainingSp[g] += spNum;
                    }
                    g++;
                }
            }
        }
        if (show) {
            updateSingleStat(MapleStat.AVAILABLESP, 0);
            client.sendEnableActions();
            baseSkills();
        }
    }

    /*
     * 新增變量
     */
    public int getPlayerPoints() {
        return playerPoints;
    }

    public void setPlayerPoints(int gain) {
        playerPoints = gain;
    }

    public void gainPlayerPoints(int gain) {
        if (playerPoints + gain < 0) {
            return;
        }
        playerPoints += gain;
    }

    public int getPlayerEnergy() {
        return playerEnergy;
    }

    public void setPlayerEnergy(int gain) {
        playerEnergy = gain;
    }

    public void gainPlayerEnergy(int gain) {
        if (playerEnergy + gain < 0) {
            return;
        }
        playerEnergy += gain;
    }

    /*
     * Pvp變量
     */
    public MaplePvpStats getPvpStats() {
        return pvpStats;
    }

    public int getPvpKills() {
        return pvpKills;
    }

    public void gainPvpKill() {
        this.pvpKills += 1;
        this.pvpVictory += 1;
        if (pvpVictory == 5) {
            map.broadcastMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.管理員對話, "[Pvp] 玩家 " + getName() + " 已經達到 5 連斬。"));
        } else if (pvpVictory == 10) {
            client.getChannelServer().broadcastGMPacket(MaplePacketCreator.spouseMessage(UserChatMessageType.管理員對話, "[Pvp] 玩家 " + getName() + " 已經達到 10 連斬。"));
        } else if (pvpVictory >= 20) {
            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.管理員對話, "[Pvp] 玩家 " + getName() + " 已經達到 " + pvpVictory + " 連斬。他(她)在頻道 " + client.getChannel() + " 地圖 " + map.getMapName() + " 中喊道誰能賜我一死."));
        } else {
            dropMessage(6, "當前: " + pvpVictory + " 連斬.");
        }
    }

    public int getPvpDeaths() {
        return pvpDeaths;
    }

    public void gainPvpDeath() {
        this.pvpDeaths += 1;
        this.pvpVictory = 0;
    }

    public int getPvpVictory() {
        return pvpVictory;
    }

    /*
     * 提示信息
     */
    public void dropTopMsg(String message) {
        client.announce(UIPacket.getTopMsg(message));
    }

    public void dropMidMsg(String message) {
        client.announce(UIPacket.offStaticScreenMessage());
        client.announce(UIPacket.getMidMsg(0, message, true));
    }

    public void clearMidMsg() {
        client.announce(UIPacket.offStaticScreenMessage());
    }

    /*
     * 特殊的頂部公告
     */
    public void dropSpecialTopMsg(String message, int fontSize, int fontColorType) {
        dropSpecialTopMsg(message, 0x00, fontSize, fontColorType, 0);
    }

    public void dropSpecialTopMsg(String msg, int fontNameType, int fontSize, int fontColorType, int fadeOutDelay) {
        if (fontSize < 10) {
            fontSize = 10;
        }
        client.announce(UIPacket.getSpecialTopMsg(msg, fontNameType, fontSize, fontColorType, fadeOutDelay));
    }

    /*
     * 新增函數
     * 帳號下的角色統計計算每日活動次數
     */
    public int getEventCount(String eventId) {
        return getEventCount(eventId, 0);
    }

    public int getEventCount(String eventId, int type) {
        return getEventCount(eventId, type, 1);
    }

    public int getEventCount(String eventId, int type, int resetDay) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            int count = 0;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_event WHERE accId = ? AND eventId = ?");
            ps.setInt(1, getAccountID());
            ps.setString(2, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                /*
                 * 年：calendar.get(Calendar.YEAR)
                 * 月：calendar.get(Calendar.MONTH)+1
                 * 日：calendar.get(Calendar.DAY_OF_MONTH)
                 * 星期：calendar.get(Calendar.DAY_OF_WEEK)-1
                 */
                count = rs.getInt("count");
                Timestamp updateTime = rs.getTimestamp("updateTime");
                if (type == 0) {
                    Calendar sqlcal = Calendar.getInstance();
                    if (updateTime != null) {
                        sqlcal.setTimeInMillis(updateTime.getTime());
                    }
                    if (sqlcal.get(Calendar.DAY_OF_MONTH) + resetDay <= Calendar.getInstance().get(Calendar.DAY_OF_MONTH) || sqlcal.get(Calendar.MONTH) + 1 <= Calendar.getInstance().get(Calendar.MONTH) || sqlcal.get(Calendar.YEAR) + 1 <= Calendar.getInstance().get(Calendar.YEAR)) {
                        count = 0;
                        PreparedStatement psu = con.prepareStatement("UPDATE accounts_event SET count = 0, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?");
                        psu.setInt(1, getAccountID());
                        psu.setString(2, eventId);
                        psu.executeUpdate();
                        psu.close();
                    }
                }
            } else {
                PreparedStatement psu = con.prepareStatement("INSERT INTO accounts_event (accId, eventId, count, type) VALUES (?, ?, ?, ?)");
                psu.setInt(1, getAccountID());
                psu.setString(2, eventId);
                psu.setInt(3, 0);
                psu.setInt(4, type);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
            log.error("獲取 EventCount 次數.", Ex);
            return -1;
        }
    }

    /*
     * 增加帳號下的角色統計計算每日活動次數
     */
    public void setEventCount(String eventId) {
        setEventCount(eventId, 0);
    }

    public void setEventCount(String eventId, int type) {
        setEventCount(eventId, type, 1);
    }

    public void setEventCount(String eventId, int type, int count) {
        setEventCount(eventId, type, count, 1, true);
    }

    public void setEventCount(String eventId, int type, int count, int date, boolean updateTime) {
        int eventCount = getEventCount(eventId, type, date);
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps;
            if (updateTime) {
                //如果 updateTime 為 ture 則更新 updateTime
                ps = con.prepareStatement("UPDATE accounts_event SET count = ?, type = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?");
            } else {
                ps = con.prepareStatement("UPDATE accounts_event SET count = ?, type = ? WHERE accId = ? AND eventId = ?");
            }
            ps.setInt(1, eventCount + count);
            ps.setInt(2, type);
            ps.setInt(3, getAccountID());
            ps.setString(4, eventId);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
            log.error("增加 EventCount 次數失敗.", Ex);
        }
    }

    /*
     * 重置帳號下的角色統計計算每日活動次數
     */
    public void resetEventCount(String eventId) {
        resetEventCount(eventId, 0);
    }

    public void resetEventCount(String eventId, int type) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts_event SET count = 0, type = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?");
            ps.setInt(1, type);
            ps.setInt(2, getAccountID());
            ps.setString(3, eventId);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
            log.error("重置 EventCount 次數失敗.", Ex);
        }
    }

    public boolean isSamePartyId(int partyId) {
        return partyId > 0 && party != null && party.getId() == partyId;
    }

    /*
     * 從地圖的1個位置移動到另外1個位置
     */
//    public void instantMapWarp(int portalId) {
//        if (map == null) {
//            return;
//        }
//        MaplePortal portal = map.getPortal(portalId);
//        if (portal == null) {
//            portal = map.getPortal(0);
//        }
//        Point portalPos = new Point(portal.getPosition());
//        Client.announce(MaplePacketCreator.instantMapWarp(getId(), portalPos));
//        checkFollow();
//        map.movePlayer(this, portalPos);
//    }

    /*
     * 角色1次對怪物的最大傷害 totDamageToMob
     */
    public long getTotDamageToMob() {
        return totDamageToMob;
    }

    public void setTotDamageToMob(long totDamageToMob) {
        this.totDamageToMob = totDamageToMob;
    }

    /*
     * 使用符文的時間間隔
     */
    public void prepareFuWenTime(long time) {
        lastFuWenTime = System.currentTimeMillis() + time;
    }

    public int getLastFuWenTime() {
        if (lastFuWenTime <= 0) {
            lastFuWenTime = System.currentTimeMillis();
        }
        long time = lastFuWenTime - System.currentTimeMillis();
        if (time <= 0) {
            return 0;
        }
        return (int) time;
    }

    /*
     * 獲取角色當前穿戴的技能皮膚信息
     */
    public Map<Integer, Integer> getSkillSkin() {
        Map<Integer, Integer> ret = new LinkedHashMap<>(); //封包發送需要的信息 [技能ID] [皮膚ID]
        List<Integer> theList = getInventory(MapleInventoryType.EQUIPPED).listSkillSkinIds();  //裝備中的技能皮膚ID集合
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (Integer i : theList) {
            int skillId = ii.getSkillSkinFormSkillId(i);
            Skill skill = SkillFactory.getSkill(skillId);
            if (skill != null) { //&& skill.canBeLearnedBy(getJob())
                ret.put(skillId, i); //加入集合 [技能ID] [皮膚ID]
            }
        }
        return ret;
    }

    /*
     * 移動到地圖的另外1個坐標
     */
//    public void instantMapWarp(Point portalPos) {
//        if (map == null || portalPos == null) {
//            return;
//        }
//        Client.announce(MaplePacketCreator.instantMapWarp(getId(), portalPos));
//        checkFollow();
//        map.movePlayer(this, portalPos);
//    }
    public final int[] StringtoInt(final String str) {
        int[] ret = new int[100]; //最大支持100個前置條件參數
        StringTokenizer toKenizer = new StringTokenizer(str, ",");
        String[] strx = new String[toKenizer.countTokens()];
        for (int i = 0; i < toKenizer.countTokens(); i++) {
            strx[i] = toKenizer.nextToken();
            ret[i] = Integer.parseInt(strx[i]);
        }
        return ret;
    }

    //高級任務系統 - 檢查基礎條件是否符合所有任務前置條件
    public final boolean MissionCanMake(final int missionid) {
        boolean ret = true;
        for (int i = 1; i < 5; i++) {
            if (!MissionCanMake(missionid, i)) { //檢查每一個任務條件是否滿足
                ret = false;
            }
        }
        return ret;
    }

    //高級任務系統 - 檢查基礎條件是否符合指定任務前置條件
    public final boolean MissionCanMake(final int missionid, final int checktype) {
        //checktype
        //1 檢查等級範圍
        //2 檢查職業
        //3 檢查物品
        //4 檢查前置任務
        boolean ret = false;
        int minlevel = -1, maxlevel = -1; //默認不限制接任務的等級範圍
        String joblist = "all", itemlist = "none", prelist = "none"; //默認所有職業可以接，默認不需要任何前置物品和任務
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT minlevel,maxlevel,joblist,itemlist,prelist FROM missionlist WHERE missionid = ?")) {
                ps.setInt(1, missionid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        minlevel = rs.getInt("minlevel");
                        maxlevel = rs.getInt("maxlevel");
                        joblist = rs.getString("joblist");
                        itemlist = rs.getString("itemlist");
                        prelist = rs.getString("prelist");
                    }
                }
            }
        } catch (final SQLException ex) {
            log.error("Error MissionCanMake:", ex);
        }
        //判斷檢查條件是否吻合
        switch (checktype) {
            case 1: //判斷級別是否符合要求
                if (minlevel > -1 && maxlevel > -1) { //雙範圍檢查
                    if (this.getLevel() >= minlevel && this.getLevel() <= maxlevel) {
                        ret = true;
                    }
                } else if (minlevel > -1 && maxlevel == -1) { //只有最小限制
                    if (this.getLevel() >= minlevel) {
                        ret = true;
                    }
                } else if (minlevel == -1 && maxlevel > -1) { //只有最大限制
                    if (this.getLevel() <= maxlevel) {
                        ret = true;
                    }
                } else if (minlevel == -1 && maxlevel == -1) { //如果是默認值-1，表示任何等級都可以接
                    ret = true;
                }
                break;
            case 2: //檢查職業是否符合要求
                if (joblist.equals("all")) { //所有職業多可以接
                    ret = true;
                } else {
                    for (int i : StringtoInt(joblist)) {
                        if (this.getJob() == i) { //只要自己的職業ID在這個清單裡，就是符合要求，立即跳出檢查
                            ret = true;
                            break;
                        }
                    }
                }
                break;
            case 3: //檢查前置物品是否有
                if (itemlist.equals("none")) { //沒有前置物品要求
                    ret = true;
                } else {
                    for (int i : StringtoInt(itemlist)) {
                        if (!this.haveItem(i)) { //如果沒有清單裡要求的物品，立即跳出檢查
                            ret = false;
                            break;
                        }
                    }
                }
                break;
            case 4: //檢查前置任務是否有完成
                if (prelist.equals("none")) { //前置任務是否完成
                    ret = true;
                } else {
                    for (int i : StringtoInt(prelist)) {
                        if (!MissionStatus(this.getId(), i, 0, 1)) { //如果要求的前置任務沒完成或從來沒接過，立即跳出檢查
                            ret = false;
                            break;
                        }
                    }
                }
                break;
        }
        return ret;
    }

    //高級任務函數 - 得到任務的等級數據
    public final int MissionGetIntData(final int missionid, final int checktype) {
        //checktype
        //1 最小等級
        //2 最大等級
        int ret = -1;
        int minlevel = -1, maxlevel = -1;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT minlevel,maxlevel FROM missionlist WHERE missionid = ?")) {
                ps.setInt(1, missionid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        minlevel = rs.getInt("minlevel");
                        maxlevel = rs.getInt("maxlevel");
                    }
                }
            }
            //判斷檢查條件是否吻合
            switch (checktype) {
                case 1:
                    ret = minlevel;
                    break;
                case 2:
                    ret = maxlevel;
                    break;
            }
        } catch (final SQLException ex) {
            log.error("Error MissionGetIntData:", ex);
        }
        return ret;
    }

    //高級任務函數 - 得到任務的的字符串型數據
    public final String MissionGetStrData(final int missionid, final int checktype) {
        //checktype
        //1 任務名稱
        //2 職業列表
        //3 物品列表
        //4 前置任務列表
        String ret = "";
        String missionname = "", joblist = "all", itemlist = "none", prelist = "none"; //默認所有職業可以接，默認不需要任何前置物品和任務
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT missionname,joblist,itemlist,prelist FROM missionlist WHERE missionid = ?")) {
                ps.setInt(1, missionid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        missionname = rs.getString("missionname");
                        joblist = rs.getString("joblist");
                        itemlist = rs.getString("itemlist");
                        prelist = rs.getString("prelist");
                    }
                }
            }
            //判斷檢查條件是否吻合
            switch (checktype) {
                case 1:
                    ret = missionname;
                    break;
                case 2:
                    ret = joblist;
                    break;
                case 3:
                    ret = itemlist;
                    break;
                case 4:
                    ret = prelist;
                    break;
            }
        } catch (SQLException ex) {
            log.error("Error MissionCanMake:", ex);
        }
        return ret;
    }

    //高級任務函數 - 直接輸出需要的職業列表串
    public final String MissionGetJoblist(final String joblist) {
        StringBuilder ret = new StringBuilder();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            for (int i : StringtoInt(joblist)) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM joblist WHERE id = ?")) {
                    ps.setInt(1, i);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            ret.append(",").append(rs.getString("jobname"));
                        }
                    }
                }
            }
        } catch (final SQLException ex) {
            log.error("Error MissionGetJoblist:", ex);
        }
        return ret.toString();
    }

    //高級任務系統 - 任務創建
    public final void MissionMake(final int charid, final int missionid, final int repeat, final long repeattime, final int lockmap, final int mobid) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO missionstatus VALUES (DEFAULT, ?, ?, ?, ?, ?, 0, DEFAULT, 0, 0, ?, 0, 0)")) {
                ps.setInt(1, missionid);
                ps.setInt(2, charid);
                ps.setInt(3, repeat);
                ps.setLong(4, repeattime);
                ps.setInt(5, lockmap);
                ps.setInt(6, mobid);
                ps.executeUpdate();
            }
        } catch (final SQLException ex) {
            log.error("Error MissionMake:", ex);
        }
    }

    //高級任務系統 - 重新做同一個任務
    public final void MissionReMake(final int charid, final int missionid, final int repeat, final long repeattime, final int lockmap) {
        int finish = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE missionstatus SET `repeat` = ?, repeattime = ?, lockmap = ?, finish = ?, minnum = 0 WHERE missionid = ? AND charid = ?")) {
                ps.setInt(1, repeat);
                ps.setLong(2, repeattime);
                ps.setInt(3, lockmap);
                ps.setInt(4, finish);
                ps.setInt(5, missionid);
                ps.setInt(6, charid);
                ps.executeUpdate();
            }
        } catch (final SQLException ex) {
            log.error("Error MissionFinish:", ex);
        }
    }

    //高級任務系統 - 任務完成
    public final void MissionFinish(final int charid, final int missionid) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE missionstatus SET finish = 1, lastdate = CURRENT_TIMESTAMP(), times = times+1, lockmap = 0 WHERE missionid = ? AND charid = ?")) {
                ps.setInt(1, missionid);
                ps.setInt(2, charid);
                ps.executeUpdate();
            }
        } catch (final SQLException ex) {
            //log.error("Error MissionFinish:", ex);
        }
    }

    // 高級任務系統 - 獲得任務完成次數
    public final int MissionGetFinish(final int charid, final int missionid) {
        int ret = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT times FROM missionstatus WHERE missionid = ? AND charid = ?")) {
                ps.setInt(1, missionid);
                ps.setInt(2, charid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ret = rs.getInt(1);
                    }
                }
            }
        } catch (final SQLException ex) {
            log.error("Error MissionFinish:", ex);
        }
        return ret;
    }

    //高級任務系統 - 放棄任務
    public final void MissionDelete(final int charid, final int missionid) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM missionstatus WHERE missionid = ? AND charid = ?")) {
                ps.setInt(1, missionid);
                ps.setInt(2, charid);
                ps.executeUpdate();
            }
        } catch (final SQLException ex) {
            log.error("Error MissionDelete:", ex);
        }
    }

    //高級任務系統 - 增加指定任務的打怪數量
    public final void MissionSetMinNum(final int charid, final int missionid, final int num) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE missionstatus SET `minnum` = ? WHERE missionid = ? AND charid = ?")) {
                ps.setInt(1, num);
                ps.setInt(2, missionid);
                ps.setInt(3, charid);
                ps.executeUpdate();
            }
        } catch (final SQLException ex) {
            log.error("Error MissionAddNum:", ex);
        }
    }

    //高級任務系統 - 增加指定任務的打怪數量
    public final void MissionAddMinNum(final int charid, final int missionid, final int num) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE missionstatus SET `minnum` = `minnum` + ? WHERE missionid = ? AND charid = ?")) {
                ps.setInt(1, num);
                ps.setInt(2, missionid);
                ps.setInt(3, charid);
                ps.executeUpdate();
            }
        } catch (final SQLException ex) {
            log.error("Error MissionAddNum:", ex);
        }
    }

    // 高級任務系統 - 獲取任務已經完成的怪物數量
    public final int MissionGetMinNum(final int charid, final int missionid, final int mobid) {
        int ret = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            String sql;
            if (mobid == 0) {
                sql = "SELECT minnum FROM missionstatus WHERE charid = ? AND missionid = ?";
            } else {
                sql = "SELECT minnum FROM missionstatus WHERE charid = ? AND missionid = ? AND mobid = ?";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (mobid == 0) {
                    ps.setInt(1, charid);
                    ps.setInt(2, missionid);
                } else {
                    ps.setInt(1, charid);
                    ps.setInt(2, missionid);
                    ps.setInt(3, mobid);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret = rs.getInt("minnum");
                        break;
                    }
                }
            }
        } catch (final SQLException ex) {
            log.error("Error MissionMob:", ex);
        }
        return ret;
    }

    // 高級任務系統 - 獲取任務需要消滅的怪物數量
    public final int MissionGetMaxNum(final int charid, final int missionid, final int mobid) {
        int ret = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            String sql;
            if (mobid == 0) {
                sql = "SELECT maxnum FROM missionstatus WHERE charid = ? AND missionid = ?";
            } else {
                sql = "SELECT maxnum FROM missionstatus WHERE charid = ? AND missionid = ? AND mobid = ?";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (mobid == 0) {
                    ps.setInt(1, charid);
                    ps.setInt(2, missionid);
                } else {
                    ps.setInt(1, charid);
                    ps.setInt(2, missionid);
                    ps.setInt(3, mobid);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret = rs.getInt("maxnum");
                        break;
                    }
                }
            }
        } catch (final SQLException ex) {
            log.error("Error MissionMob:", ex);
        }
        return ret;
    }

    // 高級任務系統 - 獲取任務需要消滅的怪物ID
    public final int MissionGetMobId(final int charid, final int missionid) {
        int ret = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT mobid FROM missionstatus WHERE charid = ? AND missionid = ?")) {
                ps.setInt(1, charid);
                ps.setInt(2, missionid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret = rs.getInt("mobid");
                        break;
                    }
                }
            }
        } catch (final SQLException ex) {
            log.error("Error MissionMob:", ex);
        }
        return ret;
    }

    //高級任務系統 - 增加指定任務的打怪數量
    public final void MissionSetMobId(final int charid, final int missionid, final int mobid) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE missionstatus SET `mobid` = ? WHERE missionid = ? AND charid = ?")) {
                ps.setInt(1, mobid);
                ps.setInt(2, missionid);
                ps.setInt(3, charid);
                ps.executeUpdate();
            }
        } catch (final SQLException ex) {
            log.error("Error MissionAddNum:", ex);
        }
    }

    //高級任務系統 - 指定任務的需要最大打怪數量
    public final void MissionMaxNum(final int missionid, final int maxnum) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE missionstatus SET `maxnum` = ? WHERE missionid = ? AND charid = ?")) {
                ps.setInt(1, maxnum);
                ps.setInt(2, missionid);
                ps.setInt(3, this.getId());
                ps.executeUpdate();
            }
        } catch (final SQLException ex) {
            log.error("Error MissionMaxNum:", ex);
        }
    }

    //高級任務系統 - 獲取repeattime
    public final long MissionGetRepeattime(final int charid, final int missionid) {
        long ret = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT repeattime FROM missionstatus WHERE charid = ? AND missionid = ?")) {
                ps.setInt(1, charid);
                ps.setInt(2, missionid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ret = rs.getLong("repeattime");
                        break;
                    }
                }
            }
        } catch (final SQLException ex) {
            log.error("Error MissionMob:", ex);
        }
        return ret;
    }

    //高級任務系統 - 放棄所有未完成任務
    public final void MissionDeleteNotFinish(final int charid) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM missionstatus WHERE finish = 0 AND charid = ?")) {

                ps.setInt(1, charid);
                ps.executeUpdate();
            }
        } catch (final SQLException ex) {
            log.error("Error MissionDeleteNotFinish:", ex);
        }
    }

    /* 高級任務系統 - 獲得任務是否可以做
     * checktype
     * 0 檢查此任務是否被完成了
     * 1 檢查此任務是否允許重複做
     * 2 檢查此任務重複做的時間間隔是否到
     * 3 檢查此任務是否到達最大的任務次數
     * 4 檢查是否接過此任務，即是否第一次做這個任務
     * 5 檢查是否接了鎖地圖傳送的任務
     */
    public final boolean MissionStatus(final int charid, final int missionid, final int maxtimes, final int checktype) {
        boolean ret = false; //默認是可以做
        int MissionMake = 0; //默認是沒有接過此任務
        long now = 0;
        long t = 0;
        Timestamp lastdate;
        int repeat = 0;
        int repeattime = 0;
        int finish = 0;
        int times = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            String sql;
            if (checktype == 5) {
                sql = "SELECT * FROM missionstatus WHERE lockmap = 1 AND charid = ?";
            } else {
                sql = "SELECT * FROM missionstatus WHERE missionid = ? AND charid = ?";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (checktype == 5) {
                    ps.setInt(1, charid);
                } else {
                    ps.setInt(1, missionid);
                    ps.setInt(2, charid);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        lastdate = rs.getTimestamp("lastdate");
                        repeat = rs.getInt("repeat");
                        repeattime = rs.getInt("repeattime");
                        finish = rs.getInt("finish");
                        times = rs.getInt("times");
                        t = lastdate.getTime();
                        now = System.currentTimeMillis();
                        MissionMake = 1; //標明這個任務已經接過了
                    }
                }
            }
        } catch (final SQLException ex) {
            log.error("Error MissionStatus:", ex);
        }
        //判斷檢查狀態類型
        switch (checktype) {
            case 0:
                if (finish == 1) {
                    ret = true;
                }
                break;
            case 1:
                if (repeat == 1) {
                    ret = true;
                }
                break;
            case 2:
                if (now - t > repeattime) { // 判斷如果有沒有到指定的重複做任務間隔時間
                    //已經到了間隔時間
                    ret = true;
                }
                break;
            case 3:
                if (times >= maxtimes) {
                    //任務到達最大次數
                    ret = true;
                }
                break;
            case 4:
                if (MissionMake == 1) {
                    //此任務已經接過了
                    ret = true;
                }
                break;
            case 5:
                if (MissionMake == 1) {
                    //已經接了鎖地圖的任務
                    ret = true;
                }
        }
        return ret;
    }

    public void gainItem(int itemId, int amount, String log) {
        MapleInventoryManipulator.addById(client, itemId, amount, log);
    }

    public void fixOnlineTime() {
        int day = getIntRecord(GameConstants.CHECK_DAY);
        int enter = getIntNoRecord(GameConstants.ENTER_CASH_SHOP);
        if (enter > 0 && getDay() != day) {
            setTodayOnlineTime(0);
            initTodayOnlineTime();
        }
        updateTodayDate();
        updataEnterShop(false);
    }

    public int getDay() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DATE);
    }

    public void updataEnterShop(boolean enter) {
        getQuestNAdd(MapleQuest.getInstance(GameConstants.ENTER_CASH_SHOP)).setCustomData(enter ? String.valueOf(1) : String.valueOf(0));
    }

    public Calendar getDaybyDay(int n2) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + n2);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.AM_PM, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public void initOnlineTime() {
        if (this.lastOnlineTime < 0) {
            final int o = StringTool.parseInt(this.getWorldShareInfo(900100, "date"));
            final int fu = DateUtil.getDate();
            int o2 = 0;
            if (fu == o) {
                o2 = StringTool.parseInt(this.getWorldShareInfo(900100, "OnlineTime"));
            } else {
                this.clearOnlineTime();
            }
            this.lastOnlineTime = Math.max(0, o2);
        }
    }

    public void saveOnlineTime() {
        this.updateWorldShareInfo(900100, "OnlineTime", String.valueOf(this.getOnlineTime()), false);
        this.updateWorldShareInfo(900100, "date", String.valueOf(DateUtil.getDate()), false);
    }

    public void clearOnlineTime() {
        this.updateWorldShareInfo(900100, "OnlineTime", "0", false);
        this.updateWorldShareInfo(900100, "date", String.valueOf(DateUtil.getDate()), false);
    }

    public int getOnlineTime() {
        return this.getNowOnlineTime() + lastOnlineTime;
    }

    /**
     * 获取本次登录至今的分钟数
     */
    public int getNowOnlineTime() {
        if (this.lastOnlineTime < 0) {
            this.lastOnlineTime = 0;
        }
        final long today = getDaybyDay(0).getTimeInMillis();
        if (this.logintime < today) {
            //昨日登录，即已经过了0点，重设登录时间为0点
            this.logintime = today;
            //清除昨日的登录时间
            this.clearOnlineTime();
            this.lastOnlineTime = 0;
        }
        return (int) (System.currentTimeMillis() - this.logintime) / 60000;
    }

    public long getLoginTime() {
        return this.logintime;
    }

    public int getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(int onlineTime) {
        this.lastOnlineTime = onlineTime;
    }

    public long getLTime() {
        return ltime;
    }

    public void setLTime() {
        this.ltime = System.currentTimeMillis();
    }

    public void updateTodayDate() {
        getQuestNAdd(MapleQuest.getInstance(GameConstants.CHECK_DAY)).setCustomData(String.valueOf(getDay()));
    }

    public int getTodayOnlineTime() {
        return todayonlinetime + (int) ((System.currentTimeMillis() - todayonlinetimestamp.getTime()) / 60000);
    }

    public void setTodayOnlineTime(int time) {
        todayonlinetime = time;
    }

    public int getTotalOnlineTime() {
        return totalonlinetime + (int) ((System.currentTimeMillis() - todayonlinetimestamp.getTime()) / 60000);
    }

    public void initTodayOnlineTime() {
        todayonlinetimestamp = new Timestamp(System.currentTimeMillis());
    }

    public void finishActivity(final int questid) {
        MapleActivity.finish(this, questid);
    }

    public final void openNpc(final int id) {
        openNpc(id, null);
    }

    public final void openNpc(final int id, final String mode) {
//        getClient().removeClickedNPC();
//        getScriptManager().stop(getScriptManager().getLastActiveScriptType());
//        getScriptManager().startNpcScript(id, 0, mode);
        MapleNPC npc = MapleLifeFactory.getNPC(id, mapid);
        if (npc != null) {
            getClient().removeClickedNPC();
            getScriptManager().dispose();
            getScriptManager().stop(getScriptManager().getLastActiveScriptType());
            getScriptManager().startNpcScript(id, 0, mode);
        }
    }

    public void updateVisitorKills(int n2, int n3) {
        client.announce(MaplePacketCreator.updateVisitorKills(n2, n3));
    }

    // 更改傷害皮膚
    public void changeDamageSkin(int id) {
        MapleQuest q = MapleQuest.getInstance(7291);
        if (q == null) {
            return;
        }
        MapleQuestStatus status = getQuestNAdd(q);
        status.setStatus((byte) 1);
        status.setCustomData(String.valueOf(id));
        updateQuest(status, true);
        send(InventoryPacket.UserDamageSkinSaveResult(4, 0, this));
        send(InventoryPacket.UserDamageSkinSaveResult(2, 0, this));
        send(InventoryPacket.showDamageSkin(this.getId(), this.getDamageSkin()));
        dropMessage(-9, "傷害字型已變更。");
    }

    /*
     * 獲取傷害皮膚的數值
     */
    public int getDamageSkin() {
        if (JobConstants.is神之子(job)) {
            return 0;
        }
        String data = getQuestNAdd(MapleQuest.getInstance(7291)).getCustomData();
        return data == null ? 0 : Integer.parseInt(data);
    }

    public final void insertRanking(String rankingname, int value) {
        RankingTop.getInstance().insertRanking(this, rankingname, value);
    }

    public synchronized Channel getChatSession() {
        return chatSession;
    }

    public void setChatSession(Channel session) {
        chatSession = session;
    }

    public int getWeaponPoint() {
        return weaponPoint;
    }

    public void setWeaponPoint(int wp) {
        this.weaponPoint = wp;
        client.announce(MaplePacketCreator.showGainWeaponPoint(wp));
        client.announce(MaplePacketCreator.updateWeaponPoint(getWeaponPoint()));
    }

    public void gainWeaponPoint(int wp) {
        this.weaponPoint += wp;
        if (wp > 0) {
            client.announce(MaplePacketCreator.showGainWeaponPoint(wp));
        }
        client.announce(MaplePacketCreator.updateWeaponPoint(getWeaponPoint()));
    }

    public Map<String, Integer> getCredits() {
        return credit;
    }

    public void setCredit(String name, int value) {
        credit.put(name, value);
    }

    public void gainCredit(String name, int value) {
        credit.put(name, getCredit(name) + value);
    }

    public int getCredit(String name) {
        if (credit.containsKey(name)) {
            return credit.get(name);
        }
        return 0;
    }

    public int getHayatoPoint() {
        return specialStats.getHayatoPoint();
    }

    public void setHayatoPoint(int jianqi) {
        specialStats.setHayatoPoint(jianqi);
        getClient().announce(MaplePacketCreator.updateHayatoPoint(specialStats.getHayatoPoint()));
    }

    public boolean checkSoulWeapon() {
        Equip weapon = (Equip) getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
        return weapon != null && weapon.getSoulSocketID() != 0;
    }

    public long getLastFullSoulMP() {
        return lastChangeFullSoulMP;
    }

    public int getSoulMP() {
        return soulMP;
    }

    public void setSoulMP(int soulcount) {
        int oldSoulMP = this.soulMP;
        this.soulMP = Math.min(Math.max(0, soulcount), maxSoulMP);
        if (client != null) {
            if (maxSoulMP > 0 || soulcount > 0) {
                client.announce(BuffPacket.giveBuff(this, getSkillEffect(soulSkillID), Collections.singletonMap(SecondaryStat.SoulMP, soulSkillID)));
                if (soulMP > oldSoulMP || soulcount > maxSoulMP) {
                    lastChangeFullSoulMP = System.currentTimeMillis();
                } else if (soulMP < oldSoulMP && oldSoulMP >= maxSoulMP) {
                    checkSoulState(false);
                }
            } else {
                client.announce(BuffPacket.temporaryStatReset(Collections.singletonList(SecondaryStat.SoulMP), this));
            }
        }
    }

    public void addSoulMP(int count) {
        setSoulMP(this.soulMP + count);
    }

    public int getSoulSkillID() {
        return soulSkillID;
    }

    public void setSoulSkillID(int soulSkillID) {
        this.soulSkillID = soulSkillID;
    }

    public short getSoulOption() {
        return soulOption;
    }

    public void setSoulOption(short soulOption) {
        this.soulOption = soulOption;
    }

    public boolean isShowSoulEffect() {
        return showSoulEffect;
    }

    public void setShowSoulEffect(boolean showSoulEffect) {
        this.showSoulEffect = showSoulEffect;
        if (showSoulEffect) {
            if (client != null) {
                client.announce(BuffPacket.giveBuff(this, getSkillEffect(soulSkillID), Collections.singletonMap(SecondaryStat.FullSoulMP, soulSkillID)));
            }
            if (map != null) {
                map.broadcastMessage(this, BuffPacket.giveForeignBuff(this, new HashMap<>(Collections.singletonMap(SecondaryStat.FullSoulMP, soulMP))), false);
            }
        } else {
            if (client != null) {
                client.announce(BuffPacket.temporaryStatReset(Collections.singletonList(SecondaryStat.FullSoulMP), this));
            }
            if (map != null) {
                map.broadcastMessage(this, BuffPacket.cancelForeignBuff(this, List.of(SecondaryStat.FullSoulMP)), false);
            }
        }
    }

    public int getMaxSoulMP() {
        return maxSoulMP;
    }

    public void setMaxSoulMP(int maxSoulMP) {
        this.maxSoulMP = maxSoulMP;
    }

    public void checkSoulState(boolean useskill) {
        MapleStatEffect effect = getSkillEffect(soulSkillID);
        if (effect != null) {
            if (useskill) {
                if (!ServerConfig.JMS_SOULWEAPON_SYSTEM && getSoulMP() >= effect.getSoulMpCon()) {
                    addSoulMP((short) (-effect.getSoulMpCon()));
                }
            }
            setShowSoulEffect(getSoulMP() >= (ServerConfig.JMS_SOULWEAPON_SYSTEM ? maxSoulMP : effect.getSoulMpCon()));
        }
    }

    public void handleSoulMP(final MapleMonster mob) {
        if (checkSoulWeapon()) {
            MapleQuest q;
            MapleQuestStatus status;
            if ((q = MapleQuest.getInstance(26535)) == null || (status = getQuestNoAdd(q)) == null || status.getCustomData() == null || status.getCustomData().equalsIgnoreCase("effect=1")) {
                final MapleMapItem item;
                (item = new MapleMapItem(new Item(4001536, (short) 1, (short) 1), new Point(mob.getPosition()), mob, this, (byte) 0, false, 0)).setPickedUp(true);
                item.setEnterType((byte) 0);
                item.setDelay(0);
                item.setPickUpID(getId());
                map.spawnMapObject(-1, item, null);
                item.setEnterType((byte) 2);
                item.setAnimation(2);
                map.disappearMapObject(item);
            }
            if (isAdmin() && isInvincible()) {
                //dropMessage(-6, "伺服器管理員無敵狀態蒐集靈魂球x10");
            }
            addSoulMP(Randomizer.rand(4, 5) * (isAdmin() && isInvincible() ? 10 : 1));
        }
    }

    /**
     * 虎影处理符文道力
     */
    public void handleHoYoungValue(int runeDiff, int scrollDiff) {
        if (!JobConstants.is虎影(job)) {
            return;
        }
        if (isDebug()) {
            runeDiff = runeDiff > 0 ? 100 : runeDiff;
            scrollDiff = scrollDiff > 0 ? 900 : scrollDiff;
        }
        specialStats.gainHoYoungRune(runeDiff);
        specialStats.gainHoYoungScroll(scrollDiff);
        client.announce(BuffPacket.setHoYoungRune(this));
    }

    /**
     * 虎影处理属性衔接阶段
     */
    public int handleHoYoungState(int type) {
        if (!JobConstants.is虎影(job)) {
            return 0;
        }
        switch (type) {
            case 1:
                specialStats.setHoYoungState1(1);
                break;
            case 2:
                specialStats.setHoYoungState2(1);
                break;
            case 3:
                specialStats.setHoYoungState3(1);
                break;
        }
        int currentState = specialStats.getHoYoungState1() + specialStats.getHoYoungState2() + specialStats.getHoYoungState3();
        if (currentState >= getHoYoungMaxState()) {
            specialStats.setHoYoungState1(0);
            specialStats.setHoYoungState2(0);
            specialStats.setHoYoungState3(0);
            final MapleStatEffect effect = getSkillEffect(虎影.調式);
            if (effect != null) {
                addHPMP(effect.getX(), effect.getY());
            }
        }
        client.announce(BuffPacket.setHoYoungState(this));
        return currentState;
    }

    private int getHoYoungMaxState() {
        switch (job) {
            case 16412:
            case 16411:
                return 3;
            case 16410:
                return 2;
            case 16400:
                return 1;
            default:
                return 0;
        }
    }

    public void iNeedSystemProcess() {
        setLastCheckProcess(System.currentTimeMillis());
        this.getClient().announce(MaplePacketCreator.SystemProcess());
    }

    public long getLastCheckProcess() {
        return lastCheckProcess;
    }

    public void setLastCheckProcess(long lastCheckProcess) {
        this.lastCheckProcess = lastCheckProcess;
    }

    public List<MapleProcess> getProcess() {
        return Process;
    }

    public void write(Packet msg) {
        if (client != null) {
            client.write(msg);
        }
    }

    public void send(final byte[] array) {
        if (client != null) {
            client.announce(array);
        }
    }

    public void send_other(final byte[] array, final boolean b) {
        getMap().broadcastMessage(this, array, b);
    }

    public List<Integer> getEffectSwitch() {
        return effectSwitch;
    }

    public void updateEffectSwitch(int pos) {
        for (Integer poss : effectSwitch) {
            if (poss == pos) {
                effectSwitch.remove(poss);
                return;
            }
        }
        effectSwitch.add(pos);
    }

    public void gainPP(int pp) {
        specialStats.gainPP(pp);
        client.announce(BuffPacket.showPP(this));
    }

    public int getMobKills() {
        return mobKills;
    }

    public void setMobKills(int mobKills) {
        this.mobKills = mobKills;
    }

    public long getLastMobKillTime() {
        return lastMobKillTime;
    }

    public void setLastMobKillTime(long lastMobKillTime) {
        this.lastMobKillTime = lastMobKillTime;
    }

    public MapleSigninStatus getSigninStatus() {
        return siginStatus;
    }

    public int getPQLog(String pqName) {
        return this.getPQLog(pqName, 0);
    }

    public int getPQLog(String pqName, int type) {
        return this.getPQLog(pqName, type, 1);
    }

    public int getDaysPQLog(String pqName, int day) {
        return this.getDaysPQLog(pqName, 0, day);
    }

    public int getDaysPQLog(String pqName, int type, int day) {
        return this.getPQLog(pqName, type, day);
    }

    public int getPQLog(String pqName, int type, int day) {
        return getPQLog(pqName, type, day, -1);
    }

    /**
     * 從數據庫讀取角色的PQLOG
     *
     * @param pqName      pqname
     * @param type        0
     * @param day         1
     * @param refreshHour 每日刷新時間,int 0-23
     * @return count
     * @throws IllegalArgumentException when refreshHour isn't 0-23
     */
    public int getPQLog(String pqName, int type, int day, int refreshHour) {
        if (refreshHour < 0) {
            refreshHour = ServerConfig.WORLD_REFRESH_TIME;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            int count = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT `count`,`time` FROM pqlog WHERE characterid = ? AND pqname = ?")) {
                ps.setInt(1, this.id);
                ps.setString(2, pqName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        count = rs.getInt("count");
                        Timestamp timestamp = rs.getTimestamp("time");
                        rs.close();
                        ps.close();
                        if (type == 0) {
                            Calendar calendar = Calendar.getInstance();
                            if (timestamp != null) {
                                calendar.setTimeInMillis(timestamp.getTime());
                                calendar.add(Calendar.DAY_OF_WEEK, day);
                            }
                            calendar.set(Calendar.HOUR_OF_DAY, refreshHour);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            if (calendar.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
                                count = 0;
                                try (PreparedStatement psi = con.prepareStatement("UPDATE pqlog SET count = 0, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND pqname = ?")) {
                                    psi.setInt(1, this.id);
                                    psi.setString(2, pqName);
                                    psi.executeUpdate();
                                }
                            }
                        }
                    } else {
                        try (PreparedStatement pss = con.prepareStatement("INSERT INTO pqlog (characterid, pqname, count, type) VALUES (?, ?, ?, ?)")) {
                            pss.setInt(1, this.id);
                            pss.setString(2, pqName);
                            pss.setInt(3, 0);
                            pss.setInt(4, type);
                            pss.executeUpdate();
                        }
                    }
                }
            }
            return count;
        } catch (SQLException e) {
            System.err.println("Error while get pqlog: " + e);
            return -1;
        }
    }

    public int getDayOfWeekPQLog(String pqName, int day) {
        return getDayOfWeekPQLog(pqName, day, -1);
    }

    public int getDayOfWeekPQLog(String pqName, int day, int refreshHour) {
        if (refreshHour < 0) {
            refreshHour = ServerConfig.WORLD_REFRESH_TIME;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            int count = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT `count`,`time` FROM pqlog WHERE characterid = ? AND pqname = ?")) {
                ps.setInt(1, this.id);
                ps.setString(2, pqName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        count = rs.getInt("count");
                        Timestamp timestamp = rs.getTimestamp("time");
                        rs.close();
                        ps.close();
                        if (day >= 0 && day <= 7) {
                            Calendar calendar = Calendar.getInstance();
                            if (timestamp != null) {
                                calendar.setTimeInMillis(timestamp.getTime());
                                if (day == 0) {
                                    calendar.add(Calendar.DAY_OF_WEEK, 1);
                                } else {
                                    if (day == 7) {
                                        day = 1;
                                    } else {
                                        day++;
                                    }
                                    calendar.set(Calendar.DAY_OF_WEEK, day);
                                    if (calendar.getTimeInMillis() <= timestamp.getTime()) {
                                        calendar.add(Calendar.DAY_OF_WEEK, 7);
                                    }
                                }
                            }
                            calendar.set(Calendar.HOUR_OF_DAY, refreshHour);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            if (calendar.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
                                count = 0;
                                try (PreparedStatement psi = con.prepareStatement("UPDATE pqlog SET count = 0, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND pqname = ?")) {
                                    psi.setInt(1, this.id);
                                    psi.setString(2, pqName);
                                    psi.executeUpdate();
                                }
                            }
                        }
                    } else {
                        try (PreparedStatement pss = con.prepareStatement("INSERT INTO pqlog (characterid, pqname, count, type) VALUES (?, ?, ?, ?)")) {
                            pss.setInt(1, this.id);
                            pss.setString(2, pqName);
                            pss.setInt(3, 0);
                            pss.setInt(4, 0);
                            pss.executeUpdate();
                        }
                    }
                }
            }
            return count;
        } catch (SQLException e) {
            System.err.println("Error while get pqlog: " + e);
            return -1;
        }
    }

    public void setPQLog(String pqname) {
        this.setPQLog(pqname, 0);
    }

    public void setPQLog(String pqname, int type) {
        this.setPQLog(pqname, type, 1);
    }

    public void setPQLog(String pqname, int type, int count) {
        this.setPQLog(pqname, type, count, -1);
    }

    public void setPQLog(String pqname, int type, int count, int refresh) {
        this.setPQLog(pqname, type, count, -1, true);
    }

    public void setPQLog(String pqname, int type, int count, int refresh, boolean updateTime) {
        if (refresh < 0) {
            refresh = ServerConfig.WORLD_REFRESH_TIME;
        }
        int times = this.getPQLog(pqname, type, 1, refresh);
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            if (updateTime) {
                try (PreparedStatement ps = con.prepareStatement("UPDATE pqlog SET count = ?, type = ?, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND pqname = ?")) {
                    ps.setInt(1, times + count);
                    ps.setInt(2, type);
                    ps.setInt(3, this.id);
                    ps.setString(4, pqname);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement("UPDATE pqlog SET count = ?, type = ? WHERE characterid = ? AND pqname = ?")) {
                    ps.setInt(1, times + count);
                    ps.setInt(2, type);
                    ps.setInt(3, this.id);
                    ps.setString(4, pqname);
                    ps.executeUpdate();
                }
            }

        } catch (SQLException sQLException) {
            System.err.println("Error while set pqlog: " + sQLException);
        }
    }

    public void resetPQLog(String pqname) {
        this.resetPQLog(pqname, 0);
    }

    public void resetPQLog(String pqname, int type) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE pqlog SET count = ?, type = ?, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND pqname = ?")) {
                ps.setInt(1, 0);
                ps.setInt(2, type);
                ps.setInt(3, this.id);
                ps.setString(4, pqname);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error while reset pqlog: " + e);
        }
    }

    public long getPQPoint() {
        return Long.parseLong(this.getOneInfo(7907, "point") != null ? this.getOneInfo(7907, "point") : "0");
    }

    public void gainPQPoint(long point) {
        this.updateOneInfo(7907, "point", String.valueOf(this.getPQPoint() + point));
    }

    public void gainQuestPoint(int questId, long point) {
        updateOneInfo(questId, "point", String.valueOf(Math.max(0, getQuestPoint(questId) + point)));
    }

    public void gainWorldShareQuestPoint(int questId, long point) {
        updateWorldShareInfo(questId, "point", String.valueOf(Math.max(0, getQuestPoint(questId) + point)));
    }

    public void doneSailQuestion() {
        for (int i2 = 0; i2 <= 4; ++i2) {
            MapleQuest.getInstance(17003 + i2).complete(this, 0, null);
            MapleQuest.getInstance(17003 + i2).complete(this, 0);
        }
        this.updateInfoQuest(17008, "T=0;L=1;E=0");
    }

    public long getLogintime() {
        return logintime;
    }

    public void setLogintime(long logintime) {
        this.logintime = logintime;
    }

    public long getQuestDiffTime() {
        return (System.currentTimeMillis() - this.logintime) / 1000 / 60;
    }

    public boolean isAttclimit() {
        return this.attclimit;
    }

    public void setAttclimit(boolean attclimit) {
        this.attclimit = attclimit;
    }

    public WeakReference<MapleReactor> getReactor() {
        return this.reactor;
    }

    public void setReactor(MapleReactor reactor) {
        this.reactor = new WeakReference<>(reactor);
    }

    public void sendEnableActions() {
        getClient().sendEnableActions();
    }

    public List<Integer> getDamSkinList() {
        return damSkinList;
    }

    public void initAllInfo() {
        String info = getWorldShareInfo(7);
        if (info == null || info.isEmpty()) {
            updateWorldShareInfo(7, "count=0;day=0;date=0");
            updateWorldShareInfo(7, "date", DateUtil.getFormatDate(System.currentTimeMillis() - 86400000L, "yyyyMMdd"));
        }
        final String questInfo = getQuestInfo(502117, "date");
        final String a1323 = DateUtil.getFormatDate(System.currentTimeMillis(), "yyyyMMdd");
        if (questInfo == null) {
            updateOneQuestInfo(502117, "date", a1323);
            updateOneQuestInfo(502117, "count", "0");
        } else if (!a1323.equals(questInfo)) {
            updateOneQuestInfo(502117, "date", a1323);
            updateOneQuestInfo(502117, "count", "0");
        }
    }

    public void initDamageSkinList() {
        String keyValue = this.getKeyValue("DAMAGE_SKIN");
        final String count = getOneInfo(56829, "count");
        if (count == null || count.isEmpty()) {
            updateOneInfo(56829, "count", String.valueOf(ServerConfig.defaultDamageSkinSlot));
        }
        int n = count == null ? ServerConfig.defaultDamageSkinSlot : Integer.parseInt(count);
        if (keyValue != null && !keyValue.isEmpty()) {
            keyValue = keyValue.replace("1050", "115");
            final String[] split = keyValue.split(",");
            for (int i = 0; i < split.length && i < n; ++i) {
                if (!split[i].isEmpty()) {
                    int skinId = Integer.parseInt(split[i]);
                    if (!this.getDamSkinList().contains(skinId)) {
                        this.getDamSkinList().add(skinId);
                    }
                }
            }
        }
        MapleQuest q = MapleQuest.getInstance(7291);
        if (q == null) {
            return;
        }
        if ("1050".equals(getQuestNAdd(q).getCustomData())) {
            q.forceStart(this, 0, "115");
        }
    }

    public Map<Integer, MapleHexaSkill> getHexaSkills() {
        return this.hexaSkills;
    }

    public Map<Integer, MapleHexaSkill> loadHexSkills() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM hexaskills WHERE charid = ?");
            ps.setInt(1, getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MapleHexaSkill mhs = new MapleHexaSkill(rs.getInt("id"), rs.getInt("skilllv"));
                hexaSkills.put(rs.getInt("id"), mhs);
            }
            rs.close();
            ps.close();
            con.close();
            return hexaSkills;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void addHexaSkill(MapleHexaSkill mhs) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement pse;
            pse = con.prepareStatement(
                    "INSERT INTO hexaskills (id, charid, skilllv) VALUES (?, ?, ?)");
            pse.setInt(1, mhs.getId());
            pse.setInt(2, getId());
            pse.setInt(3, mhs.getSkilllv());
            pse.executeUpdate();
            pse.close();
            this.hexaSkills.put(mhs.getId(), mhs);
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateHexaSkill(MapleHexaSkill mhs) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = null;
            ps = con.prepareStatement(
                    "UPDATE hexaskills SET skilllv = ? WHERE id = ? AND charid = ?");
            ps.setInt(1, mhs.getSkilllv());
            ps.setInt(2, mhs.getId());
            ps.setInt(3, getId());
            ps.executeUpdate();
            ps.close();
            hexaSkills.get(mhs.getId()).setSkilllv(mhs.getSkilllv());
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Map<Integer, MapleHexaStat> getHexaStats() {
        return this.hexaStats;
    }

    public Map<Integer, MapleHexaStat> loadHexStats() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM sixstats WHERE charid = ?");
            ps.setInt(1, getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MapleHexaStat mss = new MapleHexaStat(rs.getInt("solt"), rs.getInt("preset"));
                mss.setStatPreset1(rs.getInt("p0stat1"), rs.getInt("p0stat1lv"), rs.getInt("p0stat2"), rs.getInt("p0stat2lv"), rs.getInt("p0stat3"), rs.getInt("p0stat3lv"));
                mss.setStatPreset2(rs.getInt("p1stat1"), rs.getInt("p1stat1lv"), rs.getInt("p1stat2"), rs.getInt("p1stat2lv"), rs.getInt("p1stat3"), rs.getInt("p1stat3lv"));
                hexaStats.put(rs.getInt("solt"), mss);
            }
            rs.close();
            ps.close();
            con.close();
            return hexaStats;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void addHexaStats(MapleHexaStat mss) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement pse;
            pse = con.prepareStatement(
                    "INSERT INTO sixstats (charid, preset, solt, p0stat1, p0stat1lv, p0stat2, p0stat2lv, p0stat3, p0stat3lv, p1stat1, p1stat1lv, p1stat2, p1stat2lv, p1stat3, p1stat3lv) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            pse.setInt(1, getId());
            pse.setInt(2, mss.getPreset());
            pse.setInt(3, mss.getSolt());
            pse.setInt(4, mss.getMain0());
            pse.setInt(5, mss.getMain0Lv());
            pse.setInt(6, mss.getAddit0S1());
            pse.setInt(7, mss.getAddit0S1Lv());
            pse.setInt(8, mss.getAddit0S2());
            pse.setInt(9, mss.getAddit0S2Lv());
            pse.setInt(10, mss.getMain1());
            pse.setInt(11, mss.getMain1Lv());
            pse.setInt(12, mss.getAddit1S1());
            pse.setInt(13, mss.getAddit1S1Lv());
            pse.setInt(14, mss.getAddit1S2());
            pse.setInt(15, mss.getAddit1S2Lv());
            pse.executeUpdate();
            pse.close();
            this.hexaStats.put(mss.getSolt(), mss);
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateHexaStat(MapleHexaStat mss) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = null;
            ps = con.prepareStatement(
                    "UPDATE sixstats SET preset = ?, solt = ?, p0stat1 = ?, p0stat1lv = ? , p0stat2 = ?, p0stat2lv = ? , p0stat3 = ?, p0stat3lv = ?, p1stat1 = ?, p1stat1lv = ? , p1stat2 = ?, p1stat2lv = ? , p1stat3 = ?, p1stat3lv = ? WHERE charid = ?");
            ps.setInt(1, mss.getPreset());
            ps.setInt(2, mss.getSolt());
            ps.setInt(3, mss.getMain0());
            ps.setInt(4, mss.getMain0Lv());
            ps.setInt(5, mss.getAddit0S1());
            ps.setInt(6, mss.getAddit0S1Lv());
            ps.setInt(7, mss.getAddit0S2());
            ps.setInt(8, mss.getAddit0S2Lv());
            ps.setInt(9, mss.getMain1());
            ps.setInt(10, mss.getMain1Lv());
            ps.setInt(11, mss.getAddit1S1());
            ps.setInt(12, mss.getAddit1S1Lv());
            ps.setInt(13, mss.getAddit1S2());
            ps.setInt(14, mss.getAddit1S2Lv());
            ps.setInt(15, getId());
            ps.executeUpdate();
            ps.close();
            this.hexaStats.get(mss.getSolt()).setStatPreset1(mss.getMain0(), mss.getMain0Lv(), mss.getAddit0S1(), mss.getAddit0S1Lv(), mss.getAddit0S2(), mss.getAddit0S2Lv());
            this.hexaStats.get(mss.getSolt()).setStatPreset2(mss.getMain1(), mss.getMain1Lv(), mss.getAddit1S1(), mss.getAddit1S1Lv(), mss.getAddit1S2(), mss.getAddit1S2Lv());
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void addEdraSoul(int addEdraSoul) {
        int nowcount = getErda(0) + addEdraSoul;
        int newEdraSoulCount = 0, addEdraCount = 0;
        if (nowcount >= 1000) {
            newEdraSoulCount = nowcount % 1000;
            addEdraCount = nowcount / 1000;
        } else {
            newEdraSoulCount = nowcount;
        }
        int newErda = (getErda(1) + addEdraCount);
        if (newErda > 20) {
            newErda = 20;
        }
        updateInfoQuest(1489, "0exp=" + newEdraSoulCount + ";0=" + newErda, true);
    }

    public void reduceEdra(int edra, int fragments) {
        int fragmentCount = this.getItemQuantity(4009547)/* + this.getItemQuantity(4009548)*/;
        if (((fragmentCount - fragments) < 0) && ((getErda(1) - edra) < 0)) {
            getClient().announce(CWvsContext.sendHexaActionResult(8, 4, 0, 0)); // 靈魂艾爾達數量不足
        } else {
            // 刪除道具
            Item toRemove = getInventory(MapleInventoryType.ETC).findById(4009547);
            if (toRemove != null) {
                MapleInventoryManipulator.removeFromSlot(client, MapleInventoryType.ETC, toRemove.getPosition(), (short) fragments, false);
            }
            updateInfoQuest(1489, "0exp=" + getErda(0) + ";0=" + (getErda(1) - edra), true);
        }
    }

    public void gainErda(int itemid) {
        String msg = "";
        switch (itemid) {
            case 2636420:
                msg = "獲得靈魂艾爾達氣息10。";
                this.removeItem(2636420, 1);
                break;
            case 2636421:
                msg = "獲得靈魂艾爾達氣息200。";
                this.removeItem(2636421, 1);
                break;
            default:
                System.out.println("未知的gainErda物品:" + itemid);
                return;
        }
        getClient().announce(UIPacket.getSpecialTopMsg(msg, 3, 20, 9, 0));
        getClient().announce(EffectPacket.encodeUserEffectByPickUpItem(this, itemid));
    }

    public int getErda(int type) {
        // 1489
        // 0exp=480;0=3
        if (Objects.equals(getInfoQuest(1489), "")) {
            return 0;
        }
        int edraSoul = Integer.parseInt(getQuestInfo(1489, "0exp"));
        int edra = Integer.parseInt(getQuestInfo(1489, "0"));
        return type == 0 ? edraSoul : edra;
    }

    public List<MapleHyperStats> loadHyperStats(int pos) {
        List<MapleHyperStats> mhp = new LinkedList<MapleHyperStats>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM hyperstats WHERE charid = ? AND position = ?"); // Get
            // details..
            ps.setInt(1, getId());
            ps.setInt(2, pos);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mhp.add(new MapleHyperStats(pos, rs.getInt("skillid"), rs.getInt("skilllevel")));
            }
            rs.close();
            ps.close();
            con.close();
            return mhp;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public MapleHyperStats addHyperStats(int position, int skillid, int skilllevel) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement pse;
            pse = con.prepareStatement(
                    "INSERT INTO hyperstats (charid, position, skillid, skilllevel) VALUES (?, ?, ?, ?)");
            pse.setInt(1, getId());
            pse.setInt(2, position);
            pse.setInt(3, skillid);
            pse.setInt(4, skilllevel);
            pse.executeUpdate();
            pse.close();
            con.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        final MapleHyperStats mhs = new MapleHyperStats(position, skillid, skilllevel);
        mhs.setPosition(position);
        mhs.setSkillid(skillid);
        mhs.setSkillLevel(skilllevel);
        return mhs;
    }

    public MapleHyperStats UpdateHyperStats(int position, int skillid, int skilllevel) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = null;
            ps = con.prepareStatement(
                    "UPDATE hyperstats SET skilllevel = ? WHERE charid = ? AND position = ? AND skillid = ?");
            ps.setInt(1, skilllevel);
            ps.setInt(2, getId());
            ps.setInt(3, position);
            ps.setInt(4, skillid);
            ps.executeUpdate(); // Execute statement
            ps.close();
            con.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        final MapleHyperStats mhs = new MapleHyperStats(position, skillid, skilllevel);
        mhs.setPosition(position);
        mhs.setSkillid(skillid);
        mhs.setSkillLevel(skilllevel);
        return mhs;
    }

    public void resetHyperStats(int position, int skillid) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM hyperstats WHERE charid = ? AND position = ?");
            ps.setInt(1, id);
            ps.setInt(2, position);
            ps.execute();
            ps.close();
            con.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * 儲值函數 1 = 當前儲值金額 2 = 已經消費金額 3 = 總計消費金額 4 = 儲值獎勵
     */
    public int getHyPay(int type) {
        int pay = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM hypay WHERE accname = ?")) {
                ps.setString(1, getClient().getAccountName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (type == 1) { //當前儲值記錄
                            pay = rs.getInt("pay");
                        } else if (type == 2) { //已消費記錄
                            pay = rs.getInt("payUsed");
                        } else if (type == 3) { //當前消費總額
                            pay = rs.getInt("pay") + rs.getInt("payUsed");
                        } else if (type == 4) { //儲值獎勵
                            pay = rs.getInt("payReward");
                        } else {
                            pay = 0;
                        }
                    } else {
                        try (PreparedStatement psu = con.prepareStatement("INSERT INTO hypay (accname, pay, payUsed, payReward) VALUES (?, ?, ?, ?)")) {
                            psu.setString(1, getClient().getAccountName());
                            psu.setInt(2, 0); //當前儲值金額
                            psu.setInt(3, 0); //已經消費金額
                            psu.setInt(4, 0); //消費獎勵
                            psu.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("獲取儲值信息發生錯誤", e);
        }
        return pay;
    }

    public int addHyPay(int hypay) {
        int pay = getHyPay(1);
        int payUsed = getHyPay(2);
        int payReward = getHyPay(4);
        if (hypay > pay) {
            return -1;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE hypay SET pay = ? WHERE accname = ?")) {
                ps.setInt(1, pay - hypay); //當前儲值金額
                ps.setString(2, getClient().getAccountName());
                ps.executeUpdate();
                return 1;
            }
        } catch (SQLException e) {
            log.error("加減儲值信息發生錯誤", e);
            return -1;
        }
    }

    public int addPayUsed(int hypay) {
        int pay = getHyPay(1);
        int payUsed = getHyPay(2);
        int payReward = getHyPay(4);
        if (hypay > pay) {
            return -1;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE hypay SET payUsed = ? WHERE accname = ?")) {
                ps.setInt(1, payUsed + hypay); //已經消費金額
                ps.setString(2, getClient().getAccountName());
                ps.executeUpdate();
                return 1;
            }
        } catch (SQLException e) {
            log.error("加減儲值信息發生錯誤", e);
            return -1;
        }
    }

    public int addPayReward(int hypay) {
        int pay = getHyPay(1);
        int payUsed = getHyPay(2);
        int payReward = getHyPay(4);
        if (hypay > pay) {
            return -1;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE hypay SET payReward = ? WHERE accname = ?")) {
                ps.setInt(1, payReward + hypay); //消費獎勵
                ps.setString(2, getClient().getAccountName());
                ps.executeUpdate();
                return 1;
            }
        } catch (SQLException e) {
            log.error("加減儲值信息發生錯誤", e);
            return -1;
        }
    }

    public int delPayReward(int pay) {
        int payReward = getHyPay(4);
        if (pay <= 0) {
            return -1;
        }
        if (pay > payReward) {
            return -1;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE hypay SET payReward = ? WHERE accname = ?")) {
                ps.setInt(1, payReward - pay); //消費獎勵
                ps.setString(2, getClient().getAccountName());
                ps.executeUpdate();
                return 1;
            }
        } catch (SQLException ex) {
            log.error("加減消費獎勵信息發生錯誤", ex);
            return -1;
        }
    }

    public void gainVCraftCore(int quantity) {
        String data = getOneInfo(1477, "count");
        if (data != null) {
            int count = Integer.parseInt(data);
            updateOneInfo(1477, "count", String.valueOf(count + quantity));
        } else {
            updateOneInfo(1477, "count", String.valueOf(quantity));
        }
    }

    public void check5thJobQuest() {
        if (getQuestStatus(1465) == 1 && ltime > 10000000) {
            if (getQuestStatus(1474) == 1) {
                if (System.currentTimeMillis() - ltime >= 3600000) {
                    setLTime();
                    updateInfoQuest(1470, "on=1;remain=0;exp=500000000");
                    forceCompleteQuest(1474);
                }
            } else if (getQuestStatus(1475) == 1) {
                if (System.currentTimeMillis() - ltime >= 3600000) {
                    setLTime();
                    updateInfoQuest(1471, "on=1;remain=0;exp=500000000");
                    forceCompleteQuest(1475);
                }
            } else if (getQuestStatus(1476) == 1 && System.currentTimeMillis() - ltime >= 3600000) {
                setLTime();
                updateInfoQuest(1472, "on=1;remain=0;exp=500000000");
                forceCompleteQuest(1476);
            }
        }
    }

    public Map<Integer, VCoreSkillEntry> getVCoreSkill() {
        return vCoreSkills;
    }

    public AtomicInteger getvCoreSkillIndex() {
        return vCoreSkillIndex;
    }

    public void setVCoreSkills(Map<Integer, VCoreSkillEntry> vCoreSkills) {
        this.vCoreSkills = vCoreSkills;
    }

    public void addVCoreSkill(VCoreSkillEntry ah2) {
        this.vCoreSkills.put(this.vCoreSkillIndex.getAndIncrement(), ah2);
        this.changed_vcores = true;
    }

    public void setVCoreSkillSlot(int coreid, int slot) {
        if (this.vCoreSkills.get(coreid) != null) {
            this.vCoreSkills.get(coreid).setSlot(slot);
            this.changed_vcores = true;
        }
    }

    public void removeVCoreSkill(int coreid) {
        if (this.vCoreSkills.get(coreid) != null) {
            this.vCoreSkills.get(coreid).setSlot(0);
            this.changed_vcores = true;
        }
    }

    public int getVCoreSkillLevel(int skillid) {
        int level = 0;
        for (VCoreSkillEntry vse : vCoreSkills.values()) {
            for (int i = 1; i <= 3; ++i) {
                if (vse.getSlot() == 2 && vse.getSkill(i) == skillid) {
                    VMatrixSlot slot = vMatrixSlot.get(vse.getIndex());
                    if (slot != null) {
                        if (vse.getType() == 0 || vse.getType() == 2) {
                            return vse.getLevel() + slot.getExtend();
                        }
                        if (vse.getType() == 1) {
                            level += vse.getLevel() + slot.getExtend();
                        }
                    }
                }
            }
        }
        return level;
    }

    public boolean checkVCoreSkill(int skill) {
        for (VCoreSkillEntry entry : vCoreSkills.values()) {
            if (entry.getSlot() == 2 && entry.getSkill(1) == skill) {
                return true;
            }
        }
        return false;
    }

    public final Map<Integer, VMatrixSlot> getVMatrixSlot() {
        return this.vMatrixSlot;
    }

    public final int getNextVMatrixSlot() {
        for (VMatrixSlot slot : this.vMatrixSlot.values()) {
            if (slot.getIndex() < 0) {
                return slot.getSlot();
            }
        }
        return -1;
    }

    public final int getVMatrixPoint() {
        int n = Math.min(75, Math.max(0, level - 200));
        for (VMatrixSlot slot : this.vMatrixSlot.values()) {
            n -= slot.getExtend();
        }
        return Math.max(n, 0);
    }

    public int getRuneCoolDown() {
        int cooldown = getRuneUseCooldown();
        int cooldownAct = (int) (getRuneNextActionTime() - System.currentTimeMillis());
        return Math.max(cooldown, cooldownAct);
    }

    public int getRuneUseCooldown() {
        SecondaryStatValueHolder mbsvh = getBuffStatValueHolder(SecondaryStat.RuneStoneNoTime);
        return mbsvh == null ? 0 : mbsvh.getLeftTime();
    }

    public void setRuneNextUseTime(long time) {
        setKeyValue("RUNE_NEXT_USE_TIME", String.valueOf(System.currentTimeMillis() + time));
    }

    public boolean isBountyHunterCoolDown() {
        return getBountyHunterTime() - System.currentTimeMillis() >= 0;
    }

    public long getBountyHunterCoolDown() {
        return Math.max(getBountyHunterTime() - System.currentTimeMillis(), 0);
    }

    public long getBountyHunterTime() {
        if (getTempValues().get("BOUNTY_HUNTER_NEXT_TIME") == null) {
            getTempValues().put("BOUNTY_HUNTER_NEXT_TIME", System.currentTimeMillis());
        }
        return (long) getTempValues().get("BOUNTY_HUNTER_NEXT_TIME");
    }

    public void setBountyHunterTime(long time) {
        getTempValues().put("BOUNTY_HUNTER_NEXT_TIME", System.currentTimeMillis() + time);
    }

    public boolean isFireWolfCoolDown() {
        return getFireWolfTime() - System.currentTimeMillis() >= 0;
    }

    public long getFireWolfCoolDown() {
        return Math.max(getFireWolfTime() - System.currentTimeMillis(), 0);
    }

    public long getFireWolfTime() {
        if (getTempValues().get("FIRE_WOLF_NEXT_TIME") == null) {
            getTempValues().put("FIRE_WOLF_NEXT_TIME", System.currentTimeMillis());
        }
        return (long) getTempValues().get("FIRE_WOLF_NEXT_TIME");
    }

    public void setFireWolfTime(long time) {
        getTempValues().put("FIRE_WOLF_NEXT_TIME", System.currentTimeMillis() + time);
    }

    public long getRuneNextActionTime() {
        if (runeNextActionTime == 0) {
            runeNextActionTime = System.currentTimeMillis();
        }
        return runeNextActionTime;
    }

    public void setRuneNextActionTime(long time) {
        runeNextActionTime = System.currentTimeMillis() + time;
    }

    public void sendDathCount() {
        if (getDeathCount() >= 0) {
            client.announce(MaplePacketCreator.IndividualDeathCountInfo(this.getId(), this.getDeathCount()));
        }
    }

    public void updateBuffEffect(MapleStatEffect effect, Map<SecondaryStat, Integer> map) {
        long time = System.currentTimeMillis() - getBuffStatValueHolder(map.keySet().iterator().next()).startTime;
        send(BuffPacket.giveBuff(this, effect, map));
    }

    public long getLastUseVSkillTime() {
        return lastUseVSkillTime;
    }

    public void setLastUseVSkillTime(long lastUseVSkillTime) {
        this.lastUseVSkillTime = lastUseVSkillTime;
    }

    public boolean checkVSkillTime(final int seconds) {
        if (lastUseVSkillTime > System.currentTimeMillis() + 1000L * seconds) {
            return false;
        }
        this.lastUseVSkillTime = System.currentTimeMillis();
        return true;
    }

    public void handleAmmoClip(int n2) {
        if (JobConstants.is爆拳槍神(job)) {
            int n3 = Math.max(specialStats.getBullet() + n2, 0);
            specialStats.setBullet(Math.min(n3, specialStats.getMaxBullet()));
        }
    }

    public void handleCylinder(int n2) {
        if (JobConstants.is爆拳槍神(job)) {
            int n3 = Math.max(getCylinder() + n2, 0);
            specialStats.setCylinder(Math.min(n3, specialStats.getMaxBullet()));
        }
    }

    public int getCylinder() {
        return JobConstants.is爆拳槍神(job) ? specialStats.getCylinder() : 0;
    }

    public int getBullet() {
        return JobConstants.is爆拳槍神(job) ? specialStats.getBullet() : JobConstants.is開拓者(job) ? 3 : 0;
    }

    public void handleChargeBlaster() {
        MapleStatEffect effect = SkillFactory.getSkill(爆拳槍神.彈丸填裝).getEffect(getSkillLevel(爆拳槍神.彈丸填裝));
        if (JobConstants.is爆拳槍神(job) && getBullet() <= 0 && effect != null) {
            effect.applyTo(this);
        }
    }

    public int getHurtHP() {
        return specialStats.getHurtHP();
    }

    public void handle忍耐之盾(int n2) {
        if (JobConstants.is爆拳槍神(job) && getEffectForBuffStat(SecondaryStat.RWBarrier) == null) {
            specialStats.setHurtHP(n2);
            MapleStatEffect effect = SkillFactory.getSkill(爆拳槍神.續航防盾).getEffect(getTotalSkillLevel(爆拳槍神.續航防盾));
            if (this.isAlive() && effect != null) {
                effect.applyTo(this, true);
            }
        }
    }

    public int getBuffValue(int i) {
        return buffValue;
    }

    public void setBuffValue(int buffValue) {
        this.buffValue = buffValue;
    }

    public long getLastComboTime() {
        return lastComboTime;
    }

    public void setLastComboTime(long lastComboTime) {
        this.lastComboTime = lastComboTime;
    }

    public HiredFisher getHiredFisher() {
        return hiredFisher;
    }

    public void setHiredFisher(HiredFisher hiredFisher) {
        this.hiredFisher = hiredFisher;
    }

    public void handleHayatoPoint(int n) {
        if (JobConstants.is劍豪(job) && level >= 10) {
            Skill skill1 = SkillFactory.getSkill(劍豪.一般姿勢效果);
            Skill skill2 = SkillFactory.getSkill(劍豪.拔刀姿勢效果);
            Skill skill3 = SkillFactory.getSkill(劍豪.拔刀姿勢);
            specialStats.addHayatoPoint(n);
            int grade = 1;
            int hayatoPoint = getHayatoPoint();
            if (hayatoPoint >= 200 && hayatoPoint < 400) {
                grade = 2;
            } else if (hayatoPoint >= 400 && hayatoPoint < 600) {
                grade = 3;
            } else if (hayatoPoint >= 600 && hayatoPoint < 1000) {
                grade = 4;
            } else if (hayatoPoint >= 1000) {
                grade = 5;
            }
            if (this.getBuffedIntValue(SecondaryStat.BladeStanceMode) > 1 && getBuffedIntValue(SecondaryStat.BladeStancePower) != grade) {
                skill2.getEffect(grade).applyTo(this);
                skill3.getEffect(grade).applyTo(this);
            } else if ((n == -1 && getBuffedIntValue(SecondaryStat.BladeStanceMode) <= 0) || (getBuffedIntValue(SecondaryStat.IndieIgnoreMobpdpR) > 0 && getBuffedIntValue(SecondaryStat.IndieIgnoreMobpdpR) / 5 != grade)) {
                skill1.getEffect(grade).applyTo(this, true);
            }
            send(MaplePacketCreator.updateHayatoPoint(getHayatoPoint()));
        }
    }

    public void handlePPCount(int min) {
        if (JobConstants.is凱內西斯(job)) {
            if (this.getBuffedIntValue(SecondaryStat.KinesisPsychicOver) > 0 && min < 0) {
                min = Math.min((int) Math.ceil(min / 2.0), -1);
            }
            specialStats.setPP(Math.min(Math.max(0, specialStats.getPP() + min), 30));
            if (client != null) {
                client.announce(BuffPacket.showPP(this));
            }
        }
    }

    public List<MapleSummon> getAllLinksummon() {
        return allLinksummon;
    }

    public void addLinksummon(MapleSummon summon) {
        allLinksummon.add(summon);
    }

    public void removeLinksummon(MapleSummon summon) {
        if (allLinksummon.size() > 0) {
            allLinksummon.remove(summon);
        }
    }

    public void handelAngelReborn(MapleStatEffect effect) {
        if (effect == null) {
            return;
        }
        int skillID = effect.getSourceId();
        int linkId = SkillConstants.getLinkedAttackSkill(skillID);
        // 把天使腳色技能不能使用 關閉
//        if (SkillConstants.isAngelRebornSkill(skillID)) {
//            client.announce(MaplePacketCreator.skillNotActive(linkId));
//        }
        effect = getSkillEffect(linkId);
        if (effect == null) {
            return;
        }
        int prop = effect.getInfo().get(MapleStatInfo.onActive);
        if (linkId == 天使破壞者.靈魂探求者) {
            MapleStatEffect effect1 = getSkillEffect(天使破壞者.靈魂探求者_化妝);
            if (effect1 != null) {
                prop += 10;
            }
        }
        if (prop > 0) {
            MapleStatEffect effect1 = getSkillEffect(天使破壞者.親和力Ⅰ);
            if (effect1 != null) {
                prop += effect1.getX();
            }
            MapleStatEffect effect2 = getSkillEffect(天使破壞者.親和力Ⅲ);
            if (effect2 != null && specialStats.getAngelReborn() >= 2) {
                prop += effect2.getX();
            }
            boolean suc = Randomizer.isSuccess(prop);
            MapleStatEffect effect3 = getSkillEffect(天使破壞者.親和力Ⅳ);
            if (effect3 != null && !suc) {
                suc = Randomizer.isSuccess(effect3.getX());
            }
            if (isDebug()) {
                dropDebugMessage(1, "[天使技能重生] 最終機率：" + prop + " 是否成功：" + suc);
            }
            if (suc) {
                if (effect3 != null && Randomizer.isSuccess(50)) {
                    effect3.unprimaryPassiveApplyTo(this);
                }
                specialStats.resetAngelReborn();
                client.announce(MaplePacketCreator.skillActive());
                client.announce(EffectPacket.showResetOnStateForOnOffSkill(-1));
                map.broadcastMessage(this, EffectPacket.showResetOnStateForOnOffSkill(id), false);
                return;
            }
            if (getSkillEffect(天使破壞者.親和力Ⅲ) != null) {
                specialStats.gainAngelReborn();
            }
        }
    }

    public final int addWreckages(final Point point, final int n) {
        this.wreckagesLock.lock();
        try {
            final int dk = this.getSpecialStat().gainForceCounter();
            this.evanWreckages.add(new Triple<>(dk, (System.currentTimeMillis() + n), point));
            return dk;
        } finally {
            this.wreckagesLock.unlock();
        }
    }

    public final Map<Integer, Point> getWreckagesMap() {
        this.wreckagesLock.lock();
        try {
            final HashMap<Integer, Point> hashMap = new HashMap<>();
            for (Triple<Integer, Long, Point> evanWreckage : this.evanWreckages) {
                if (evanWreckage.mid > System.currentTimeMillis()) {
                    hashMap.put(evanWreckage.left, evanWreckage.right);
                }
            }
            this.evanWreckages.clear();
            return hashMap;
        } finally {
            this.wreckagesLock.unlock();
        }
    }

    public final List<Triple<Integer, Long, Point>> getEvanWreckages() {
        this.wreckagesLock.lock();
        try {
            return this.evanWreckages;
        } finally {
            this.wreckagesLock.unlock();
        }
    }

    public final void cleanEvanWreckages() {
        this.wreckagesLock.lock();
        try {
            this.evanWreckages.clear();
        } finally {
            this.wreckagesLock.unlock();
        }
    }

    public int getJudgementStack() {
        return judgementStack;
    }

    public void setJudgementStack(int judgementStack) {
        this.judgementStack = judgementStack;
    }

    public final void incJudgementStack() {
        ++this.judgementStack;
    }

    public final void updateJudgementStack() {
        if (this.judgementStack > 0 && client != null) {
            if (isDebug()) {
                dropDebugMessage(1, "[judgement stack] : " + judgementStack);
            }
            client.announce(MaplePacketCreator.updateCardStack(judgementStack));
        }
    }

    public void applyXenonEnegy(int x) {
        setBuffStatValue(SecondaryStat.SurplusSupply, getBuffedIntValue(SecondaryStat.SurplusSupply) + x);
        getSkillEffect(傑諾.蓄能系統).unprimaryApplyTo(this, null, true);
    }

    public void applyHayatoStance(int addPoint) {
        if (isAdmin() && isInvincible() && addPoint > 0) {
            addPoint += 100;
        }
        specialStats.addHayatoPoint(addPoint);
        final int hayatoPoint = specialStats.getHayatoPoint();
        final int p = Math.min(hayatoPoint / 100, 1000);
        final int level = p < 2 ? 1 : p < 4 ? 2 : p < 7 ? 3 : p < 10 ? 4 : 5;
        Integer value;
        Skill skill;
        if (getBuffedIntValue(SecondaryStat.BladeStanceMode) > 0) {
            value = getBuffedValue(SecondaryStat.BladeStancePower, 劍豪.拔刀姿勢效果);
            skill = SkillFactory.getSkill(劍豪.拔刀姿勢效果);
        } else {
            value = getBuffedValue(SecondaryStat.IndiePADR, 劍豪.一般姿勢效果);
            skill = SkillFactory.getSkill(劍豪.一般姿勢效果);
        }
        if (value == null || value != level) {
            skill.getEffect(level).unprimaryPassiveApplyTo(this);
        }
        client.announce(MaplePacketCreator.updateHayatoPoint(hayatoPoint));
    }

    public int getMobZoneState() {
        return mobZoneState;
    }

    public void setMobZoneState(int mobZoneState) {
        this.mobZoneState = mobZoneState;
    }

    public int getLarknessDiraction() {
        return larknessDiraction;
    }

    public void setLarknessDiraction(int larknessDiraction) {
        this.larknessDiraction = larknessDiraction;
    }

    public int getLarkness() {
        return larkness;
    }

    public void setLarkness(int larkness) {
        this.larkness = larkness;
    }

    public final void addLarkness(final int n) {
        this.larkness = Math.min(10000, Math.max(0, this.larkness + n));
    }

    public final void updateLarknessStack() {
        if (this.client != null && this.map != null) {
//            dropMessage(6, "夜光平衡模式：" + getLarknessDiraction() + " 點數：" + getLarkness());
            client.announce(BuffPacket.updateLuminousGauge(getLarkness(), getLarknessDiraction()));
        }
    }

    public boolean hasTruthGate() {
        return truthGate;
    }

    public void setTruthGate(boolean truthGate) {
        this.truthGate = truthGate;
    }

    public void setActiveEventNameTag(int index, int value) {
        updateOneInfo(14489, String.valueOf(index), String.valueOf(value));
    }

    public int getActiveEventNameTag(int index) {
        int intValue = -1;
        if (this.getOneInfo(14489, String.valueOf(index)) != null) {
            intValue = Integer.parseInt(this.getOneInfo(14489, String.valueOf(index)));
        }
        return intValue;
    }

    public CalcDamage getCalcDamage() {
        return calcDamage;
    }

    public int getLinkMobObjectID() {
        return linkMobObjectID;
    }

    public void setLinkMobObjectID(int linkMobObjectID) {
        this.linkMobObjectID = linkMobObjectID;
    }

    public void doHealPerTime() {
        if (this.anticheat.canNextHealHPMP() && (this.stats.recoverHP > 0 && this.stats.getHp() < this.stats.getCurrentMaxHP() || this.stats.recoverMP > 0 && this.stats.getMp() < this.stats.getCurrentMaxMP())) {
            this.addHPMP(this.stats.recoverHP, this.stats.recoverMP, false, this.stats.recoverHP > 0);
        }
        if (this.getStat().hpRecoverTime > 0 && this.anticheat.canNextHealHPMPS()) {
            this.anticheat.setNextHealHPMPS(this.getStat().hpRecoverTime);
            this.addHPMP(this.stats.getCurrentMaxHP() * this.stats.healHPR / 100, this.stats.getCurrentMaxMP() * this.stats.healMPR / 100, false, true);
        }
        if (this.getStat().mpcon_eachSecond < 0) {
            this.addHPMP(0, this.getStat().mpcon_eachSecond, false, false);
        }
    }

    public boolean gainVCoreSkill(final int vcoreoid, int nCount, boolean fromMake) {
        if (getVCoreSkill().size() + nCount > 495) {
            dropMessage(5, "無法再持有V核心。請透過強化或分解的方式減少持有中的V核心數量後再重新嘗試。");
            return false;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final VCoreDataEntry vcoredata = ii.getCoreData(vcoreoid);
        if (vcoredata == null) {
            dropMessage(1, "要製作的核心不存在。");
            return false;
        }
        if (fromMake) {
            if (!vcoredata.haveJob(String.valueOf(getJob())) && !vcoredata.haveJob("all") && !vcoredata.haveJob(JobConstants.getJobBranchName(getJob()))) {
                dropMessage(1, "要製作的核心不適用這個職業。");
                return false;
            }
            if (vcoredata.isNotAbleCraft()) {
                dropMessage(1, "不能製作這個核心。");
                return false;
            }
        }
        if (vcoredata.getType() == 2) {
            dropMessage(1, "暫時不支援製作這個核心。");
            return false;
        }
        int skill1 = 0;
        int skill2 = 0;
        int skill3 = 0;
        if (vcoredata.getConnectSkill().size() > 0) {
            skill1 = vcoredata.getConnectSkill().get(0);
        }
        StringBuilder sb = new StringBuilder();
        if (vcoredata.getType() == 1) {
            String skill1Name = "";
            List<Pair<Integer, String>> list = ii.getCoreJobSkill(getJob());
            for (Pair<Integer, String> skill : list) {
                if (skill.getLeft() > 0 && skill.getLeft() == skill1) {
                    skill1Name = skill.getRight();
                    break;
                }
            }
            for (int i = 0; i < nCount; i++) {
                sb.append("已獲得").append(skill1Name);
                Collections.shuffle(list);
                int selectedSkills = 0;

                for (Pair<Integer, String> skill : list) {
                    if (skill.getLeft() > 0 && skill.getLeft() != skill1 && selectedSkills < 2) {
                        sb.append("/").append(skill.getRight());
                        selectedSkills++;

                        if (selectedSkills == 1) {
                            skill2 = skill.getLeft();
                        } else if (selectedSkills == 2) {
                            skill3 = skill.getLeft();
                        }
                    }
                }

                addVCoreSkill(new VCoreSkillEntry(vcoreoid, 1, 0, skill1, skill2, skill3, -1, 1, -1));
                send(VCorePacket.updateVCoreList(this, false, 0, 0));
                if (!fromMake) {
                    send(VCorePacket.showVCoreItemUseEffect(vcoreoid, 1, skill1, skill2, skill3));
                }
                sb.append(" 核心。");
                if (i < nCount - 1) {
                    sb.append("\r\n");
                }
            }
        } else {
            for (int i = 0; i < nCount; i++) {
                addVCoreSkill(new VCoreSkillEntry(vcoreoid, 1, 0, skill1, skill2, skill3, -1, 1, -1));
                send(VCorePacket.updateVCoreList(this, false, 0, 0));
                if (!fromMake) {
                    send(VCorePacket.showVCoreItemUseEffect(vcoreoid, 1, skill1, skill2, skill3));
                }
                sb.append("已獲得").append(vcoredata.getName()).append(" 核心。");
                if (i < nCount - 1) {
                    sb.append("\r\n");
                }
            }
        }
        if (fromMake) {
            if (nCount > 1) {
                nCount = 0;
                skill2 = 0;
                skill3 = 0;
            }
            if (sb.length() > 0) {
                send(MaplePacketCreator.multiLineMessage(UserChatMessageType.系統, sb.toString()));
            }
            send(VCorePacket.addVCoreSkillResult(vcoreoid, 1, skill1, skill2, skill3, nCount));
        }
        return true;
    }

    public boolean gainRandVSkill(int nCoreType, boolean indieJob, boolean onlyJob) {
        if (getVCoreSkill().size() + 1 >= 32767) {
            dropMessage(1, "V核心已經達到最大值，無法再使用！");
            return false;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int coreid = 0;
        int skill1 = 0;
        int skill2 = 0;
        int skill3 = 0;
        List<VCoreDataEntry> coreList = new ArrayList<>(ii.getCoreDatasByJob(nCoreType, String.valueOf(getJob()), indieJob));
        if (!Randomizer.isSuccess(40) && !onlyJob) {
            coreList.addAll(ii.getCoreDatasByJob(nCoreType, "all"));
            coreList.addAll(ii.getCoreDatasByJob(nCoreType, JobConstants.getJobBranchName(getJob())));
        }
        if (coreList.size() <= 0) {
            return false;
        }
        Collections.shuffle(coreList);

        if (nCoreType == 0) {
            final VCoreDataEntry entry = coreList.get(Randomizer.nextInt(coreList.size()));
            if (entry != null) {
                coreid = entry.getId();
                skill1 = entry.getConnectSkill().get(0);
            }
        } else if (nCoreType == 1) {
            for (VCoreDataEntry entry : coreList) {
                if (entry.getConnectSkill().size() > 0 && entry.getConnectSkill().get(0) > 0) {
                    coreid = entry.getId();
                    skill1 = entry.getConnectSkill().get(0);
                    break;
                }
            }
            List<Pair<Integer, String>> list = ii.getCoreJobSkill(getJob());
            Collections.shuffle(list);
            for (Pair<Integer, String> skill : list) {
                if (skill.getLeft() > 0 && skill.getLeft() != skill1 && skill.getLeft() != skill2 && skill.getLeft() != skill3 && SkillFactory.getSkill(skill.getLeft()) != null) {
                    if (skill2 == 0) {
                        skill2 = skill.getLeft();
                    } else {
                        skill3 = skill.getLeft();
                    }
                }
            }
        }
        if (coreid > 0) {
            addVCoreSkill(new VCoreSkillEntry(coreid, 1, 0, skill1, skill2, skill3, -1, 1, -1));
            client.announce(VCorePacket.updateVCoreList(this, false, 0, 0));
            client.announce(VCorePacket.showVCoreItemUseEffect(coreid, 1, skill1, skill2, skill3));
            return true;
        }
        return false;
    }

    public void resetVSkills() {
        for (Entry<Integer, VCoreSkillEntry> entry : getVCoreSkill().entrySet()) {
            int k = entry.getKey();
            VCoreSkillEntry v = entry.getValue();
            if (v != null && v.getSlot() == 2) {
                VMatrixSlot slot = getVMatrixSlot().get(v.getIndex());
                if (slot != null && slot.getIndex() == v.getIndex()) {
                    slot.setIndex(-1);
                }
                v.setIndex(-1);
                setVCoreSkillSlot(k, 1);
                for (int i = 1; i <= 3; i++) {
                    if (v.getSkill(i) > 0) {
                        changeSingleSkillLevel(v.getSkill(i), (getVCoreSkillLevel(v.getSkill(i)) > 0) ? getVCoreSkillLevel(v.getSkill(i)) : -1, (byte) ((v.getType() == 2) ? 1 : ((v.getType() == 0) ? 25 : 50)));
                    }
                }
                if (client != null) {
                    client.announce(VCorePacket.updateVCoreList(this, true, 1, k));
                }
            }
        }
    }

    public void extractVCores() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (Entry<Integer, VCoreSkillEntry> entry : getVCoreSkill().entrySet()) {
            if (entry.getValue() == null || entry.getValue().getSlot() <= 0 || entry.getKey() == 40000000 || entry.getKey() == 10000024) {
                continue;
            }
            removeVCoreSkill(entry.getKey());
            Triple<Integer, Integer, Integer> vcoredata = ii.getVcores(entry.getValue().getType()).get(entry.getValue().getLevel());
            gainVCraftCore((entry.getValue().getType() == 0 ? 140 : entry.getValue().getType() == 1 ? 70 : 250) * (entry.getValue().getExp() / vcoredata.getMid() + 1));
        }
        send(VCorePacket.updateVCoreList(this, true, 5, 0));
    }

    public void modifiedAvatar() {
        if (map != null) {
            map.broadcastMessage(this, MaplePacketCreator.updateCharLook(this), getPosition());
        }
    }

    public boolean isInGameCurNode() {
        return inGameCurNode;
    }

    public void setInGameCurNode(boolean inGameCurNode) {
        this.inGameCurNode = inGameCurNode;
    }

    public int getQuestPoint(int questid) {
        String info = getOneInfo(questid, "point");
        if (info == null) {
            info = getWorldShareInfo(questid, "point");
        }
        return info == null ? 0 : Integer.parseInt(info);
    }

    public final void setBuyLimit(final int shopid, int itemid, int count, long resetTime) {
        NpcShopBuyLimit limit = buyLimit.computeIfAbsent(shopid, NpcShopBuyLimit::new);
        limit.update(itemid, count, resetTime);
        this.changed_buylimit = true;
    }

    public Map<Integer, NpcShopBuyLimit> getBuyLimit() {
        return buyLimit;
    }

    public int getBuyLimit(int shopid, int itemId) {
        return !buyLimit.containsKey(shopid) ? 0 : buyLimit.get(shopid).getCount(itemId);
    }

    public final void setAccountBuyLimit(final int shopid, int itemid, int count, long resetTime) {
        NpcShopBuyLimit limit = accountBuyLimit.computeIfAbsent(shopid, NpcShopBuyLimit::new);
        limit.update(itemid, count, resetTime);
        this.changed_accountbuylimit = true;
    }

    public Map<Integer, NpcShopBuyLimit> getAccountBuyLimit() {
        return accountBuyLimit;
    }

    public int getAccountBuyLimit(int shopid, int itemId) {
        return !accountBuyLimit.containsKey(shopid) ? 0 : accountBuyLimit.get(shopid).getCount(itemId);
    }

    public final void checkBuyLimit() {
        Iterator<Entry<Integer, NpcShopBuyLimit>> iterator = buyLimit.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, NpcShopBuyLimit> buyLimitEntry = iterator.next();
            Iterator<Entry<Integer, BuyLimitData>> dataIterator = buyLimitEntry.getValue().getData().entrySet().iterator();
            while (dataIterator.hasNext()) {
                if (dataIterator.next().getValue().getCount() <= 0) {
                    dataIterator.remove();
                    if (!changed_buylimit) {
                        changed_buylimit = true;
                    }
                }
            }
            if (buyLimitEntry.getValue().getData().isEmpty()) {
                iterator.remove();
            }
        }
        iterator = accountBuyLimit.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, NpcShopBuyLimit> buyLimitEntry = iterator.next();
            Iterator<Entry<Integer, BuyLimitData>> dataIterator = buyLimitEntry.getValue().getData().entrySet().iterator();
            while (dataIterator.hasNext()) {
                if (dataIterator.next().getValue().getCount() <= 0) {
                    dataIterator.remove();
                    if (!changed_accountbuylimit) {
                        changed_accountbuylimit = true;
                    }
                }
            }
            if (buyLimitEntry.getValue().getData().isEmpty()) {
                iterator.remove();
            }
        }
    }

    public void dropAlertNotice(String s) {
        dropMessage(1, s);
    }

    public Map<Integer, Integer> getSoulCollection() {
        return soulCollection;
    }

    public Map<Integer, String> getMobCollection() {
        return mobCollection;
    }

    public void updateMobCollection(int questId, String s) {
        this.mobCollection.put(questId, s);
        this.changed_mobcollection = true;
        if (this.client != null) {
            MessageOption option = new MessageOption();
            option.setObjectId(questId);
            option.setText(s);
            client.announce(CWvsContext.sendMessage(MessageOpcode.MS_CollectionRecordMessage, option));
        }
    }

    public void updateMobCollection(final int questId, final String key, final String value) {
        if (!this.mobCollection.containsKey(questId)) {
            this.updateMobCollection(questId, key + "=" + value);
            return;
        }
        final String[] split = this.mobCollection.get(questId).split(";");
        boolean b = false;
        final StringBuilder sb = new StringBuilder();
        String[] array;
        for (int length = (array = split).length, i = 0; i < length; ++i) {
            final String s3;
            final String[] split2;
            if ((split2 = (s3 = array[i]).split("=")).length == 2) {
                if (split2[0].equals(key)) {
                    sb.append(key).append("=").append(value);
                    b = true;
                } else {
                    sb.append(s3);
                }
                sb.append(";");
            }
        }
        if (!b) {
            sb.append(key).append("=").append(value);
        }
        this.updateMobCollection(questId, b ? sb.substring(0, sb.toString().length() - 1) : sb.toString());
    }

    public String getMobCollection(final int n, final String s) {
        if (!this.mobCollection.containsKey(n)) {
            return null;
        }
        String[] split;
        for (int length = (split = this.mobCollection.get(n).split(";")).length, i = 0; i < length; ++i) {
            final String[] split2;
            if ((split2 = split[i].split("=")).length == 2 && split2[0].equals(s)) {
                return split2[1];
            }
        }
        return null;
    }

    public String getMobCollection(final int n) {
        if (this.mobCollection.containsKey(n)) {
            return this.mobCollection.get(n);
        }
        return "";
    }

    public int getFreeMacrossTicket() {
        return 7 - this.getDaysPQLog("FreeMacrossTicket", 7);
    }

    public int getMacrossTicket() {
        String hg;
        if ((hg = this.getQuestNAdd(MapleQuest.getInstance(99997)).getCustomData()) == null) {
            hg = "0";
        }
        return Integer.parseInt(hg);
    }

    public void setMacrossTicket(final int n) {
        this.getQuestNAdd(MapleQuest.getInstance(99997)).setCustomData(String.valueOf(n));
    }

    public int getFlameBeads() {
        return getSpecialStat().getFlameBeads();
    }

    public void setFlameBeads(final int aDb) {
        getSpecialStat().setFlameBeads(aDb);
    }

    public int getPureBeads() {
        return getSpecialStat().getPureBeads();
    }

    public void setPureBeads(final int aAc) {
        getSpecialStat().setPureBeads(aAc);
    }

    public void addPureBeads(int n) {
        if (n < 0 || this.getTotalBeads() < 5) {
            getSpecialStat().addPureBeads(n);
            if (this.client != null) {
                client.announce(BuffPacket.setPureBeads(this));
            }
        }
    }

    public void addFlameBeads(int n) {
        if (n < 0 || this.getTotalBeads() < 5) {
            getSpecialStat().addFlameBeads(n);
            if (this.client != null) {
                client.announce(BuffPacket.setFlameBeads(this));
            }
        }
    }

    public int getGaleBeads() {
        return getSpecialStat().getGaleBeads();
    }

    public void setGaleBeads(final int aDc) {
        getSpecialStat().setGaleBeads(aDc);
    }

    public void addGaleBeads(int n) {
        if (n < 0 || this.getTotalBeads() < 5) {
            getSpecialStat().addGaleBeads(n);
            if (this.client != null) {
                client.announce(BuffPacket.setGaleBeads(this));
            }
        }
    }

    public int getAbyssBeads() {
        return getSpecialStat().getAbyssBeads();
    }

    public void setAbyssBeads(final int aDd) {
        getSpecialStat().setAbyssBeads(aDd);
    }

    public void addAbyssBeads(int n) {
        if (n < 0 || this.getTotalBeads() < 5) {
            getSpecialStat().addAbyssBeads(n);
            if (this.client != null) {
                client.announce(BuffPacket.setAbyssBeads(this));
            }
        }
    }

    public int getTotalBeads() {
        return Math.min(5, getAbyssBeads() + getGaleBeads() + getFlameBeads() + getPureBeads());
    }

    public int getErosions() {
        return getSpecialStat().getErosions();
    }

    public void setErosions(final int erosions) {
        getSpecialStat().setErosions(erosions);
    }

    public void addErosions(int n) {
        getSpecialStat().addErosions(n);
        if (this.client != null) {
            client.announce(BuffPacket.setErosions(this));
        }
    }

    public void sendNote(String name, String text) {
        MapleCharacterUtil.sendNote(MapleCharacterUtil.getIdByName(name), this.name, text, 0);
    }

    public void showNote() {
        MapleCharacterUtil.showNote(this);
    }

    public int getMeisterSkillEff() {
        int intValue = 0;
        if (this.getQuestInfo(25948, "E") != null) {
            intValue = Integer.parseInt(this.getQuestInfo(25948, "E"));
        }
        if (intValue > 1) {
            return 10000 * (intValue + 9200);
        }
        return 0;
    }

    public List<SecondaryStat> getTempStatsToRemove() {
        return tempStatsToRemove;
    }

    public int getAIFamiliarID() {
        return this.AIFamiliarID.getAndIncrement();
    }

    public void updatePlayerStats(final Map<MapleStat, Long> map, final boolean b) {
        this.updatePlayerStats(map, b, false);
    }

    public void updatePlayerStats(final Map<MapleStat, Long> map, final boolean b, final boolean b2) {
        if (!b2) {
            this.stats.recalcLocalStats(this);
        }
        this.client.announce(MaplePacketCreator.updatePlayerStats(map, b, this));
    }

    public int getInnerRank() {
        byte rank = 0;
        for (InnerSkillEntry ise : getInnerSkills()) {
            if (ise != null && ise.getRank() > rank) {
                rank = ise.getRank();
            }
        }
        return rank;
    }

    public void resetInnerSkill(byte path, int itemId, List<Integer> lockPosition, boolean temp, boolean maxLevel) {
        int innerSkillSize = getInnerSkillSize();
        int lines = innerSkillSize > 3 ? 3 : innerSkillSize;
        int maxRank = 3;
        // 能力傳播者或罕見奇幻傳播者：最高 Rank 為 2
        if (itemId == 2702000 || itemId == 2702001 || itemId == 2702002) {
            maxRank = 2;
        }
        InnerAbillity ia = InnerAbillity.getInstance();
        int innerRank = getInnerRank();

        // 若使用指定的 itemId 2702003 或 2702004，直接根據現有位置取得技能編號進行更新
        if (itemId == 2702003 || itemId == 2702004) {
            for (int i = 0; i < lines; i++) {
                if (lockPosition.contains(i + 1)) {
                    continue;
                }
                int skillId = getInnerSkillIdByPos(i);
                if (skillId == 0) {
                    continue;
                }
                int nMaxLevel = innerRank * 10 - 30 + SkillFactory.getSkill(skillId).getMaxLevel();
                InnerSkillEntry newSkill = new InnerSkillEntry(
                        skillId,
                        maxLevel ? nMaxLevel : Randomizer.rand(i == 0 ? nMaxLevel - 9 : 1, nMaxLevel),
                        (byte) (i + 1),
                        (byte) innerRank,
                        false
                );
                if (temp) {
                    getTempInnerSkills().add(newSkill);
                } else {
                    innerSkills[i] = newSkill;
                }
            }
        } else {
            int upgradeRate;
            int downgradeRate;
            switch (innerRank) {
                case 0:
                    upgradeRate = 300;
                    downgradeRate = 0;
                    break;
                case 1:
                    upgradeRate = 100;
                    downgradeRate = 50;
                    break;
                case 2:
                    upgradeRate = 50;
                    downgradeRate = 500;
                    break;
                case 3:
                    upgradeRate = 0;
                    downgradeRate = 900;
                    break;
                default:
                    upgradeRate = 0;
                    downgradeRate = 0;
                    break;
            }
            // 當 itemId 不屬於指定範圍時，設為 -1
            if (itemId > 0 && (itemId != 2702000 && itemId != 2702001 && itemId != 2702002)) {
                itemId = -1;
            }
            if (upgradeRate > Randomizer.nextInt(1000) && innerRank < maxRank && itemId > -2) {
                innerRank++;
            }
            if (downgradeRate > Randomizer.nextInt(1000) && innerRank > 0 && itemId > -1) {
                innerRank--;
            }
            // 先清除臨時技能列表
            getTempInnerSkills().clear();
            for (int i = 0; i < lines; i++) {
                if (lockPosition.contains(i + 1)) {
                    continue;
                }
                InnerSkillEntry newSkill = null;
                // 持續嘗試更新，直到取得一筆不重複的內在技能
                while (newSkill == null) {
                    newSkill = ia.renewSkill(innerRank, i + 1, i == lines - 1, maxLevel && i == 0);
                    if (newSkill != null) {
                        // 檢查是否重複：非臨時則檢查現有內在技能數組，臨時則檢查臨時列表
                        List<InnerSkillEntry> checkList = temp ? getTempInnerSkills() : Arrays.asList(innerSkills);
                        boolean duplicate = false;
                        for (InnerSkillEntry ski : checkList) {
                            if (ski != null && ski.getSkillId() == newSkill.getSkillId()) {
                                duplicate = true;
                                break;
                            }
                        }
                        if (duplicate) {
                            newSkill = null;
                        }
                    }
                }
                if (newSkill != null) {
                    if (temp) {
                        getTempInnerSkills().add(newSkill);
                    } else {
                        innerSkills[i] = newSkill;
                    }
                }
            }
        }
        if (!temp) {
            client.announce(MaplePacketCreator.updateInnerSkill(client.getPlayer(), innerSkills[0], innerSkills[1], innerSkills[2]));
        }
    }

    public final void addByItem(final Item item) {
        MapleInventoryManipulator.addbyItem(getClient(), item);
    }

    public List<InnerSkillEntry> getTempInnerSkills() {
        return tempInnerSkills;
    }

    /* by will */

    public MapleFieldActionBar getActionBar() {
        return actionBar;
    }

    public void setActionBar(MapleFieldActionBar actionBar) {
        this.actionBar = actionBar;
    }

    public AtomicLong getKillMonsterExp() {
        return killMonsterExp;
    }

    public void modifyMoonlightValue(int value) {
        this.setMoonlightValue(Math.max(0, Math.min(this.moonlightValue + value, 100)));
        if (this.client != null) {
            value = this.moonlightValue;
            final MaplePacketLittleEndianWriter hh;
            hh = new MaplePacketLittleEndianWriter();
            hh.writeShort(OutHeader.WILL_SET_MOON_GAUGE.getValue());
            hh.writeInt(value);
            client.announce(hh.getPacket());
        }
    }

    public int getMoonlightValue() {
        return moonlightValue;
    }

    public void setMoonlightValue(int moonlightValue) {
        this.moonlightValue = moonlightValue;
    }

    public SpecialChairTW getSpecialChairTW() {
        return specialChairTW;
    }

    public void setSpecialChairTW(SpecialChairTW specialChairTW) {
        this.specialChairTW = specialChairTW;
    }

    public SpecialChair getSpecialChair() {
        return specialChair;
    }

    public void setSpecialChair(SpecialChair specialChair) {
        this.specialChair = specialChair;
    }

    public Map<Integer, ForceAtomObject> getForceAtomObjects() {
        return (Map<Integer, ForceAtomObject>) getTempValues().computeIfAbsent("ForceAtom_OBJ", v -> new LinkedHashMap<>());
    }

    public Map<String, Object> getTempValues() {
        return tempValues;
    }

    public void handleAdeleCharge(int diff) {
        if (!JobConstants.is阿戴爾(getJob())) {
            return;
        }
        MapleStatEffect effect = getSkillEffect(阿戴爾.高級乙太);
        if (effect == null) {
            effect = getSkillEffect(阿戴爾.乙太);
        }
        if (effect != null) {
            if (diff > 0) {
                MapleStatEffect eff = getEffectForBuffStat(SecondaryStat.LWRestore);
                if (eff != null) {
                    diff += diff * eff.getX() / 100;
                }
            }
            if (getBuffedValue(SecondaryStat.LWSwordGauge) == null) {
                getSkillEffect(阿戴爾.乙太).applyTo(this, true);
            }
            int max = effect.getSourceId() == 阿戴爾.乙太 ? 300 : 400;
            specialStats.setAdeleCharge(Math.max(0, Math.min(max, specialStats.getAdeleCharge() + diff)));
            getClient().announce(BuffPacket.setAdeleCharge(this));
        }
    }

    public void handleMaliceCharge(int diff) {
        if (!JobConstants.is凱殷(getJob())) {
            return;
        }
        MapleStatEffect effect = getSkillEffect(凱殷.主導II);
        if (effect == null) {
            effect = getSkillEffect(凱殷.主導);
        }
        if (effect != null) {
            if (diff > 0) {
                MapleStatEffect eff = getEffectForBuffStat(SecondaryStat.LWRestore);
                if (eff != null) {
                    diff += diff * eff.getX() / 100;
                }
            }
            //y x
            int max = effect.getV() * 100 + 100;
            if (diff > 0 && specialStats.getMaliceCharge() == max) {
                return;
            }
            specialStats.setMaliceCharge(Math.max(0, Math.min(max, specialStats.getMaliceCharge() + diff)));
            if (isAdmin()) {
                dropMessage(5, "[Malice Charge] Diff: " + diff + " Now: " + specialStats.getMaliceCharge());
            }
            getClient().announce(BuffPacket.setMaliceCharge(this));
        }
    }

    public void handleAdeleObjectSword(MapleStatEffect effect, List<Integer> targets) {
        Map<Integer, ForceAtomObject> swordsMap = getForceAtomObjects();
        List<ForceAtomObject> createList = new ArrayList<>();
        List<ForceAtomObject> removeList = new ArrayList<>();
        Point pos = getPosition();

        if (isDebug()) {
            dropMessage(5, "[Adele Sword] Effect: " + effect);
        }
        switch (effect.getSourceId()) {
            case 阿戴爾.碎片: {
                for (int i = 0; i < 5; i++) {
                    boolean b = i % 2 == 1;
                    int n = (i + 1) / 2;
                    ForceAtomObject sword = new ForceAtomObject(specialStats.gainForceCounter(), 0, i, getId(), n * 15 * (b ? -1 : 1), effect.getSourceId());
                    sword.Position = new Point(0, 1);
                    sword.ObjPosition = new Point(pos.x + n * 40 * (b ? -1 : 1), pos.y - 120 + n * 20);
                    sword.EnableDelay = 600;
                    sword.Expire = 2400;
                    sword.CreateDelay = n * 120;
                    if (!targets.isEmpty()) {
                        sword.Target = targets.get(Randomizer.nextInt(targets.size()));
                    }
                    createList.add(sword);
                    swordsMap.put(sword.Idx, sword);
                }
                break;
            }
            case 阿戴爾.創造_1: {
                List<ForceAtomObject> swords = new LinkedList<>();
                for (ForceAtomObject sword : swordsMap.values()) {
                    if (sword.SkillId == getJob()) {
                        swords.add(sword);
                    }
                }
                if (targets != null) {
                    for (int i = 0; i < swords.size(); i++) {
                        ForceAtomObject sword = swords.get(i);
                        if (sword != null) {
                            getMap().broadcastMessage(AdelePacket.ForceAtomObjectAttack(getId(), sword.Idx, i == 0 ? swords.size() / 2 : 0), getPosition());
                        }
                    }
                }
                break;
            }
            case 阿戴爾.追蹤: {
                if (getBuffedValue(SecondaryStat.LWCreation) != null) {
                    if (specialStats.getAdeleCharge() > 0x9C) {
                        for (int i = 0; i < 2; i++) {
                            int x = pos.x + Randomizer.rand(-50, 50);
                            ForceAtomObject sword = new ForceAtomObject(specialStats.gainForceCounter(), 7, 0, getId(), 0, effect.getSourceId());
                            sword.Position = new Point(pos.x + Randomizer.rand(-500, 500), pos.y + Randomizer.rand(-400, 50));
                            sword.ObjPosition = new Point(pos.x + Randomizer.rand(-500, 500), pos.y + Randomizer.rand(-400, 50));
                            sword.Expire = 0x64 * 1000;
                            if (!targets.isEmpty()) {
                                sword.Target = targets.get(Randomizer.nextInt(targets.size()));
                            }
                            for (int j = 0; j < 3; j++) {
                                sword.addX(x + Randomizer.rand(-50, 50));
                            }
                            createList.add(sword);
                            swordsMap.put(sword.Idx, sword);
                        }
                        handleAdeleCharge(-0x9C);
                    }
                }
                break;
            }
            case 阿戴爾.無限: {
                for (int i = 0; i < effect.getBulletCount(); i++) {
                    ForceAtomObject sword = new ForceAtomObject(specialStats.gainForceCounter(), 8, 0, getId(), 0, effect.getSourceId());
                    sword.Position = new Point(pos.x + Randomizer.rand(-500, 500), pos.y + Randomizer.rand(-400, 50));
                    sword.ObjPosition = new Point(pos.x + Randomizer.rand(-500, 500), pos.y + Randomizer.rand(-400, 50));
                    sword.EnableDelay = 1320;
                    sword.Expire = effect.getY() * 1000;
                    if (!targets.isEmpty()) {
                        sword.Target = targets.get(Randomizer.nextInt(targets.size()));
                    }
                    createList.add(sword);
                    swordsMap.put(sword.Idx, sword);
                }
                break;
            }
        }
        if (getBuffedValue(SecondaryStat.LWCreation) == null) {
            Iterator<Entry<Integer, ForceAtomObject>> iterator = swordsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Integer, ForceAtomObject> sword = iterator.next();
                if (sword.getValue().SkillId == getJob()) {
                    removeList.add(sword.getValue());
                    iterator.remove();
                }
            }
        } else if (getBuffedValue(SecondaryStat.LWSwordGauge) != null) {
            int count = Math.min(6, specialStats.getAdeleCharge() / 100 * 2);
            List<ForceAtomObject> swords = new LinkedList<>();
            for (ForceAtomObject sword : swordsMap.values()) {
                if (sword.SkillId == getJob()) {
                    swords.add(sword);
                }
            }
            for (int i = 0; i < swords.size(); i++) {
                ForceAtomObject sword = swords.get(i);
                if (i >= count) {
                    removeList.add(swordsMap.remove(sword.Idx));
                } else if (sword == null) {
                    sword = new ForceAtomObject(specialStats.gainForceCounter(), i + 1, 0, getId(), 0, getJob());
                    sword.Position = new Point(pos.x - 100, pos.y + 100);
                    sword.ObjPosition = new Point(pos.x, pos.y);
                    swordsMap.put(sword.Idx, sword);
                    createList.add(sword);
                }
            }
        }
        if (!createList.isEmpty()) {
            getMap().broadcastMessage(AdelePacket.ForceAtomObject(getId(), createList, effect.getSourceId() == 阿戴爾.無限 ? 1 : 0), getPosition());
        }
    }

    public void playerIGDead() {
    }

    public long getAggressiveDamage() {
        return this.AggressiveDamage;
    }

    /**
     * @return the allForce
     */
    public int getAllForce() {
        return allForce;
    }

    /**
     * @param allForce the allForce to set
     */
    public void setAllForce(int allForce) {
        this.allForce = allForce;
    }

    public final void showScreenEffect(final String s) {
        send(FieldPacket.fieldEffect(FieldEffect.getFieldEffectScreen(s)));
    }

    public final void showTopScreenEffect(final String s, final int n) {
        send(FieldPacket.fieldEffect(FieldEffect.getFieldBackgroundEffectFromWz(s, n)));
    }

    public final void showScreenDelayedEffect(final String s, final int n) {
        send(FieldPacket.fieldEffect(FieldEffect.getOffFieldEffectFromWz(s, n)));
    }

    public final void showScreenAutoLetterBox(final String s, final int n) {
        send(FieldPacket.fieldEffect(FieldEffect.getFieldEffectFromWz(s, n)));
    }

    public final void setVCoreSkillChanged(final boolean change) {
        this.changed_vcores = change;
    }

    public final void setSoulCollectionChanged(final boolean change) {
        changed_soulcollection = change;
    }

    public final void setMobCollectionChanged(final boolean change) {
        this.changed_mobcollection = change;
    }

    public final void setFamiliarsChanged(final boolean change) {
        this.changed_familiars = change;
    }

    public SkillMacro[] getSkillMacros() {
        return skillMacros;
    }

    public int addAntiMacroFailureTimes() {
        return ++antiMacroFails;
    }

    public int getAntiMacroFailureTimes() {
        return antiMacroFails;
    }

    public void setAntiMacroFailureTimes(int times) {
        antiMacroFails = times;
    }

    public Map<Integer, List<Integer>> getSalon() {
        return salon;
    }

    public int[] getRuneStoneAction() {
        return runeStoneAction;
    }

    public void setRuneStoneAction(int[] action) {
        runeStoneAction = action;
    }

    public void updateReward() {
        List<MapleReward> rewards = new LinkedList<>();
        List<Integer> toRemove = new LinkedList<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM rewards WHERE `accid` = ? OR (`accid` IS NULL AND `cid` = ?)")) {
                ps.setInt(1, accountid);
                ps.setInt(2, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (rewards.size() >= 200) {// 避免禮物箱過多道具，導致封包過長38
                            break;
                        }
                        if (rs.getLong("end") > 0 && rs.getLong("end") <= System.currentTimeMillis()) {
                            toRemove.add(rs.getInt("id"));
                            continue;
                        }
                        rewards.add(new MapleReward(
                                rs.getInt("id"),
                                rs.getLong("start"),
                                rs.getLong("end"),
                                rs.getInt("type"),
                                rs.getInt("amount"),
                                rs.getInt("itemId"),
                                rs.getString("desc")));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Unable to update rewards: ", e);
        }
        for (int i : toRemove) {
            deleteReward(i);
        }
        client.announce(MaplePacketCreator.updateReward(0, (byte) 0x09, rewards, 0x09));
    }

    public MapleReward getReward(int id) {
        MapleReward reward = null;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM rewards WHERE `id` = ? AND (`accid` = ? OR (`accid` IS NULL AND `cid` = ?))")) {
                ps.setInt(1, id);
                ps.setInt(2, accountid);
                ps.setInt(3, this.id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        reward = new MapleReward(rs.getInt("id"), rs.getLong("start"), rs.getLong("end"), rs.getInt("type"), rs.getInt("amount"), rs.getInt("itemId"), rs.getString("desc"));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Unable to obtain reward information: ", e);
        }
        return reward;
    }

    public void addReward(boolean acc, int type, long amount, int item, String desc) {
        addReward(acc, 0L, 0L, type, amount, item, desc);
    }

    public void addReward(boolean acc, long start, long end, int type, long amount, int itemId, String desc) {
        addReward(acc ? accountid : 0, id, start, end, type, amount, itemId, desc);
    }

    public void deleteReward(int id) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM rewards WHERE `id` = ? AND (`accid` = ? OR (`accid` IS NULL AND `cid` = ?))")) {
                ps.setInt(1, id);
                ps.setInt(2, accountid);
                ps.setInt(3, this.id);
                ps.execute();
            }
        } catch (SQLException e) {
            log.error("Unable to delete reward: ", e);
        }
    }

    public int getBurningChrType() {
        return burningChrType;
    }

    public void setBurningChrType(int type) {
        burningChrType = type;
    }

    public long getBurningChrTime() {
        return burningChrTime;
    }

    public void setBurningChrTime(long time) {
        burningChrTime = time;
    }

    public Map<Integer, Integer> getForeverBuffs() {
        Map<Integer, Integer> buffs = new LinkedHashMap<>();
        if (getKeyValue("ForeverBuffs") != null) {
            for (String s : getKeyValue("ForeverBuffs").split(",")) {
                if (s.isEmpty()) {
                    continue;
                }
                buffs.put(Integer.parseInt(s.split("=")[0]), Integer.parseInt(s.split("=")[1]));
            }
        }
        return buffs;
    }

    public void updateForeverBuffs(Map<Integer, Integer> buffs) {
        String buffsString = "";
        for (Entry<Integer, Integer> entry : buffs.entrySet()) {
            buffsString += entry.getKey() + "=" + entry.getValue() + ",";
        }
        setKeyValue("ForeverBuffs", buffsString.isEmpty() ? null : buffsString.substring(0, buffsString.length() - 1));
    }

    public boolean freeJobChange(int newJob) {
        if (!JobConstants.is冒險家(job) || !JobConstants.is冒險家(newJob) || newJob / 100 != job / 100) {
            return false;
        }
        String lastJobChangeDate = getQuestNAdd(MapleQuest.getInstance(25957)).getCustomData();
        if (lastJobChangeDate == DateUtil.getCurrentDate("yyyyMMdd")) {
            dropMessage(1, "今天已經自由轉職過了.自由轉職1天只能1次. 凌晨12點後請再試一次.");
            return false;
        }
        String sCount = getOneInfo(25946, "count");
        int count = sCount == null || sCount.isEmpty() ? 0 : Integer.parseInt(sCount);
        updateOneQuestInfo(25946, "count", String.valueOf(count + 1));
        changeJob(newJob);
        MapleInventoryManipulator.addById(client, 2431849, 1, "自由轉職獲得");
        MapleQuest.getInstance(25957).forceStart(this, 0, DateUtil.getCurrentDate("yyyyMMdd"));

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_JobFreeChangeResult);
        mplew.write(0);
        send(mplew.getPacket());
        return true;
    }

    public boolean isOverMobLevelTip() {
        return overMobLevelTip;
    }

    public void setOverMobLevelTip(boolean val) {
        overMobLevelTip = val;
    }

    public int getInnerStormValue() {
        return ((Integer) tempValues.getOrDefault("InnerStormValue", 0)).intValue();
    }

    public boolean checkInnerStormValue() {
        if (getInventory(MapleInventoryType.EQUIPPED).findById(1113228) == null) {
            return false;
        }
        if (!anticheat.isAttacking()) {
            long lastTime = ((Long) tempValues.getOrDefault("InnerStormLastTIme", 0L)).longValue();
            long timeNow = System.currentTimeMillis();
            if (lastTime + 2000 < timeNow) {
                modifyInnerStormValue(-2 * (int) Math.floor((timeNow - lastTime) / 2000));
            }
        }
        return true;
    }

    public void modifyInnerStormValue(int quantity) {
        int oldValue = getInnerStormValue();
        if (getInventory(MapleInventoryType.EQUIPPED).findById(1113228) != null) {
            tempValues.put("InnerStormValue", Math.min(Math.max(getInnerStormValue() + quantity, 0), 100));
            if (quantity > 0) {
                tempValues.put("InnerStormLastTIme", System.currentTimeMillis());
            }
        } else {
            tempValues.remove("InnerStormValue");
            tempValues.remove("InnerStormLastTIme");
        }
        int nowValue = getInnerStormValue();
        int attack = 0;
        if (nowValue != 0) {
            attack = 2 * nowValue + 8;
            attack = attack % 5 == 0 ? attack / 5 : -1;
        }
        if (attack >= 0) {
            send(BuffPacket.giveBuff(this, null, Collections.singletonMap(SecondaryStat.InnerStorm, attack)));
        }
        write(OverseasPacket.extraEquipResult(ExtraEquipResult.updateInnerStormValue(id, nowValue, nowValue == 0 ? 0 : oldValue == 0 ? 1 : -1)));
    }

    public int getSelectedFamiliarTeamStat() {
        String sSelectedOption = getKeyValue("SelectedFamiliarTeamStat");
        return sSelectedOption == null ? 0 : Integer.valueOf(sSelectedOption);
    }

    public List<Short> getFamiliarTeamStats() {
        List<Short> listVal = new ArrayList<>();
        String sOptions = getKeyValue("FamiliarTeamStat");
        if (sOptions != null) {
            for (String option : sOptions.split(",")) {
                if (!option.matches("^\\d+$")) {
                    continue;
                }
                listVal.add(Short.valueOf(option));
                if (listVal.size() >= 3) {
                    break;
                }
            }
        }
        if (listVal.size() < 3) {
            for (int i = listVal.size(); i < 3; i++) {
                listVal.add((short) 0);
            }
        }
        return listVal;
    }

    public boolean setSelectedFamiliarTeamStat(int selected) {
        if (selected < 0 || selected > 2) {
            return false;
        }
        List<Short> listVal = getFamiliarTeamStats();
        if (listVal.size() < selected + 1) {
            return false;
        }
        setKeyValue("SelectedFamiliarTeamStat", String.valueOf(selected));
        return true;
    }

    public boolean changeFamiliarTeamStat(int optionIndex) {
        if (optionIndex < 1 || optionIndex > 21) {
            return false;
        }
        int selectedOption = getSelectedFamiliarTeamStat();
        List<Short> listVal = getFamiliarTeamStats();
        if (listVal.size() < selectedOption + 1 || (selectedOption > 0 && listVal.get(selectedOption - 1) == 0)) {
            return false;
        }
        listVal.remove(selectedOption);
        listVal.add(selectedOption, (short) optionIndex);
        String sOptions = "";
        for (short option : listVal) {
            sOptions += String.valueOf(option) + ",";
        }
        setKeyValue("FamiliarTeamStat", sOptions.substring(0, sOptions.length() - 1));
        return true;
    }

    public boolean isStopComboKill() {
        return stopComboKill;
    }

    public void setStopComboKill(boolean b) {
        stopComboKill = b;
        if (!b) {
            getCheatTracker().setLastAttackTime();
        }
    }

    public int getMapleUnionFightCoin() {
        String coin = getWorldShareInfo(18098, "coin");
        if (coin == null || coin.isEmpty()) {
            coin = "0";
            updateWorldShareInfo(18098, "coin=0");
        }
        return Integer.parseInt(coin);
    }

    public void checkMapleUnion(boolean onLoad) {
        if (getLevel() >= 60) {
            String questInfo = getWorldShareInfo(18793, "q0");
            if ("".equals(questInfo) || questInfo == null || ("1".equals(questInfo) && getQuestStatus(16013) != 2)) {
                updateWorldShareInfo(18793, "q0", "0");
                MapleQuest.getInstance(16013).reset(this);
            }
        }
        String oneInfo = getWorldShareInfo(18771, "rank");
        if (oneInfo != null && getQuestStatus(16013) != 2) {
            oneInfo = null;
        }
        if (oneInfo == null) {
            oneInfo = "101";
            updateWorldShareInfo(18771, "rank", "101");
        }
        if (mapleUnion.getState() != Integer.valueOf(oneInfo)) {
            mapleUnion.setState(Integer.valueOf(oneInfo));
            mapleUnion.update();
        }

        int coin = getMapleUnionFightCoin();
        if (onLoad) {
            send(MaplePacketCreator.updateMapleUnion(getMapleUnion()));
        } else {
            send(MaplePacketCreator.openMapleUnion(coin, getMapleUnion()));
        }

        MapleUnionData data = MapleUnionData.getInstance();
        int state = mapleUnion.getState();
        int level = state / 100;
        int grade = state % 10;
        MapleUnionData.MapleUnionRankData nowRank = null;
        if (data.getRankInfo().containsKey(level) && data.getRankInfo().get(level).containsKey(grade)) {
            nowRank = data.getRankInfo().get(level).get(grade);
        }
        client.announce(MaplePacketCreator.getMapleUnionCoinInfo(nowRank == null || coin < nowRank.getCoinStackMax() ? -1 : 0, nowRank == null ? 200 : nowRank.getCoinStackMax()));
    }

    public void gainMapleUnionPoint(int amount) {
        if (amount == 0) {
            return;
        }
        if (amount > 0) {
            String pt = getWorldShareInfo(18797, "PT");
            if (pt == null || pt.isEmpty() || !pt.matches("^\\d+$")) {
                pt = "0";
            }
            updateWorldShareInfo(18797, "PT", String.valueOf(Integer.valueOf(pt) + amount));
        }
        String point = getWorldShareInfo(500629, "point");
        if (point == null || point.isEmpty() || !point.matches("^\\d+$")) {
            point = "0";
        }
        int nPoint = Integer.valueOf(point);
        nPoint += amount;
        if (nPoint < 0) {
            nPoint = 0;
        }
        updateWorldShareInfo(500629, "point", String.valueOf(nPoint));
    }

    public void enableActions() {
        enableActions(true);
    }

    public void enableActions(boolean useTriggerForUI) {
        send(MaplePacketCreator.enableActions(this, useTriggerForUI));
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public boolean getDiseases() {
        return getDiseases();
    }

    //=====================================================================================

    /* seren */
    public void modifySunlightValue(int value) {
        this.setSunlightValue(Math.max(0, Math.min(this.sunlightValue + value, 100)));
    }

    public int getSunlightValue() {
        return sunlightValue;
    }

    public void setSunlightValue(int sunlightValue) {
        this.sunlightValue = sunlightValue;
    }

    /* Dusk */
    public boolean isDuskBlind() {
        return this.isDuskBlind;
    }

    public void setDuskBlind(final boolean duskBlind) {
        this.isDuskBlind = duskBlind;
    }

    public int getDuskGauge() {
        return this.duskGauge;
    }

    public void setDuskGauge(final int duskGauge) {
        this.duskGauge = duskGauge;
    }

    // Helper method to convert an integer to a byte array
    private byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    public long getLastSpawnBlindMobTime() {
        return lastSpawnBlindMobtime;
    }

    public void setLastSpawnBlindMobTime(long lastSpawnBlindMobtime) {
        this.lastSpawnBlindMobtime = lastSpawnBlindMobtime;
    }

    /* black */
    public byte getDeathCount() {
        return this.deathcount;
    }

    public void setDeathCount(int deathcount) {
        this.deathcount = (byte) deathcount;
    }

    public void setDeathCount(final byte de) {
        this.deathcount = de;
        this.getMap().broadcastMessage(CField.setDeathCount(this, this.deathcount));
        if (this.getMapId() == 450011990) {
            //this.getClient().getSession().writeAndFlush(JinhillahPacket.JinHillah(3, this, this.getMap()));

        } else if (this.getMapId() == 262031300) {
            final int deathcouint = 15 - this.getDeathCount();
            this.getClient().getSession().writeAndFlush(CWvsContext.onFieldSetVariable("TotalDeathCount", "15"));
            this.getClient().getSession().writeAndFlush(CWvsContext.onFieldSetVariable("DeathCount", String.valueOf(deathcouint)));
            final MapleMonster hardHilla = this.getMap().getMobObjectByID(8870100);
            if (hardHilla != null) {
                int hillaSkill = 8877100;
                hardHilla.setForcedMobStat(hillaSkill, 1L);
                final List<Pair<MonsterStatus, MonsterStatusEffect>> stats = new ArrayList<>();
            }
        }
        if (de > 0) {
            this.getMap().broadcastMessage(CField.showDeathCount(this, this.getDeathCount()));
        }
    }

    public int getDeathcount() {
        return this.deathcount;
    }

    public Object getSkillCustomValue(final int skillid) {
        if (this.customInfo == null || !this.customInfo.containsKey(skillid)) {
            return null;
        }
        if (skillid == 63111009 && this.customInfo.get(skillid) != null && this.customInfo.get(skillid).getValue() <= 0L) {
            return null;
        }
        if (this.customInfo.get(skillid) != null && this.customInfo.get(skillid).getValue() < 0L) {
            return null;
        }
        return this.customInfo.get(skillid) != null ? this.customInfo.get(skillid).getValue() : null;
    }

    public void setSkillCustomInfo(final int skillid, final long value, final long time) {
        this.customInfo.put(skillid, new SkillCustomInfo(value, time));
    }

    public int getBlackMageWB() {
        return this.blackmagewb;
    }

    /* this is blackmage */

    public void setBlackMageWB(final int v) {
        this.blackmagewb = v;
    }

    public void dispelDebuffs() {
        HashMap<SecondaryStat, Pair<Integer, Integer>> statupz = new HashMap<SecondaryStat, Pair<Integer, Integer>>();
    }

    public int getMoonGauge() {
        return moonlightValue;
    }

    public void setMoonGauge(final int lunaGauge) {
        this.moonlightValue = lunaGauge;
    }

    /* this is seren */
    public int getSerenStunGauge() {
        return this.SerenStunGauge;
    }

    public void setSerenStunGauge(final int SerenStunGauge) {
        this.SerenStunGauge = SerenStunGauge;
    }

    public void addSerenGauge(int add) {
        if (!this.getDiseases(SecondaryStat.MobFlashBang)) {
            this.SerenStunGauge += add;
            if (this.SerenStunGauge >= 1000) {
                MapleMonster seren;
                this.SerenStunGauge = 0;

                EnumMap<SecondaryStat, Pair<Integer, Integer>> diseases = new EnumMap<SecondaryStat, Pair<Integer, Integer>>(SecondaryStat.class);
                diseases.put(SecondaryStat.GiveMeHeal, new Pair<Integer, Integer>(1, 5000));
                diseases.put(SecondaryStat.MobFlashBang, new Pair<Integer, Integer>(1, 5000));
                diseases.put(SecondaryStat.IgnorePriestDispel, new Pair<Integer, Integer>(1, 5000));

                map.broadcastMessage(MaplePacketCreator.playSound("Sound/Field.img/SerenDeath/effect"));
                map.broadcastMessage(FieldPacket.fieldEffect(FieldEffect.getFieldEffectScreen("UI/UIWindow7.img/SerenDeath")));


//                getBuffedValue(SecondaryStat.GiveMeHeal, 182);
//                MapleMonster seren;
//                if ((getMapId() == 410002060 && (seren = getMap().getMonsterById(8880602)) != null) || (
//                        getMapId() == 410002160 && (seren = getMap().getMonsterById(8880632)) != null)) {

                this.getBuffedValue(SecondaryStat.GiveMeHeal);
                if (this.getMapId() == 410002060 && (seren = this.getMap().getMobObjectByID(8880602)) != null) {

                    seren.setSerenMidNightSetTotalTime(seren.getSerenMidNightSetTotalTime() - 1);
                    if (seren.getSerenMidNightSetTotalTime() <= 0) {

//                        getMap().broadcastMessage(Seren.SerenTimer(1, new int[]{120, 120, 0, 120, (seren.getSerenTimetype() == 4) ? -1 : 1}));
//                        getMap().killAllMonsters(false);
//                        getMap().broadcastMessage(EffectPacket.showCombustionMessage("#fn나눔고딕 ExtraBold##fs32##r#e如果沒有日落,沒有人可以對抗我。", 100, 1500));
//                        for (MapleCharacter chr : getMap().getAllCharactersThreadsafe()) {

                        this.getMap().broadcastMessage(Seren.SerenTimer(1, 120, 120, 0, 120, seren.getSerenTimetype() == 4 ? -1 : 1));
                        this.getMap().killAllMonsters(false);
                        for (MapleCharacter chr : this.getMap().getAllCharactersThreadsafe()) {
                            if (chr == null) {
                                continue;
                            }
                            chr.warpdelay(410000670, 7);
                        }
                    } else {
                        switch (seren.getSerenTimetype()) {
                            case 1: {
                                seren.setSerenNoonNowTime(seren.getSerenNoonNowTime() + 1);
                                seren.setSerenNoonTotalTime(seren.getSerenNoonTotalTime() + 1);
                                break;
                            }
                            case 2: {
                                seren.setSerenSunSetNowTime(seren.getSerenSunSetNowTime() + 1);
                                seren.setSerenSunSetTotalTime(seren.getSerenSunSetTotalTime() + 1);
                                break;
                            }
                            case 4: {
                                seren.setSerenDawnSetNowTime(seren.getSerenDawnSetNowTime() + 1);
                                seren.setSerenDawnSetTotalTime(seren.getSerenDawnSetTotalTime() + 1);
                                break;
                            }
                        }
                        if (seren.getSerenTimetype() != 3) {
                            seren.AddSerenTotalTimeHandler(seren.getSerenTimetype(), +5, seren.getSerenTimetype() == 4 ? -1 : 1);
                        }
                    }
                }
            }
            if (this.SerenStunGauge < 0) {
                this.SerenStunGauge = 0;
            }
            map.broadcastMessage(Seren.SerenUserStunGauge(1000, this.SerenStunGauge));
        }
    }

    public void warpdelay(int Mapid, int Delay) {
        MapTimer.getInstance().schedule(() ->
        {
            ChannelServer cserv = getClient().getChannelServer();
            MapleMap target = cserv.getMapFactory().getMap(Mapid);
            changeMap(target, target.getPortal(0));
        }, (Delay * 1000L));
    }

    public ScriptManager getScriptManager() {
        return scriptManager;
    }

    public AvatarData getAvatarData() {
        return new AvatarData(this);
    }

    public CharacterStat getCharacterStat() {
        return new CharacterStat(this);
    }

    public AvatarLook getAvatarLook() {
        return new AvatarLook(this, isBeta());
    }

    public AvatarLook getSecondAvatarLook() {
        return new AvatarLook(this, !isBeta());
    }

    /**
     * Disposes this Char, allowing it to send packets to the server again.
     */
    public void dispose() {
        client.removeClickedNPC();
//        write(WvsContext.exclRequest());

        enableActions(true);
    }

    public void clearAtomAttackRecords() {
        this.atomsAttackRecords.clear();
    }

    public Map<Integer, Integer> getAtomAttackRecords() {
        return this.atomsAttackRecords;
    }

    public int getAtomAttackRecord(int key) {
        return this.atomsAttackRecords.getOrDefault(key, 0);
    }

    public void removeAtomAttackRecord(int key) {
        this.atomsAttackRecords.remove(key);
    }

    public void putAtomAttackRecord(int key, int value) {
        atomRecordsLock.lock();
        try {
            this.atomsAttackRecords.put(key, value);
        } finally {
            atomRecordsLock.unlock();
        }
    }

    public void incAtomAttackRecord(int key) {
        atomRecordsLock.lock();
        try {
            this.atomsAttackRecords.put(key, this.atomsAttackRecords.getOrDefault(key, 0) + 1);
        } finally {
            atomRecordsLock.unlock();
        }
    }

    public void reduceAtomAttackRecord(int key) {
        atomRecordsLock.lock();
        try {
            this.atomsAttackRecords.put(key, this.atomsAttackRecords.getOrDefault(key, 0) - 1);
        } finally {
            atomRecordsLock.unlock();
        }
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        boolean changed = online != this.online;

        this.online = online;
        if (getParty() != null) {
            PartyMember pm = getParty().getPartyMemberByID(getId());
            if (pm != null) {
                List<Integer> changedTypes = pm.getChangedTypes(online ? this : null);
                pm.setChr(online ? this : null);
                for (int type : changedTypes) {
                    getParty().broadcast(WvsContext.partyResult(PartyResult.setMemberData(pm, type)));
                }
                getParty().updateFull();
            }
        }
    }

    /**
     * Notifies all groups (such as party, guild) about all your changes, such as level and job.
     */
    public void notifyChanges() {
        Party party = getParty();
        if (party != null) {
            party.updatePartyMemberInfoByChr(this);
            updatePartyMemberHP();
            receivePartyMemberHP();
        }
    }

    public void write(OutPacket outpacket) {
        send(outpacket.getData());
    }


    public enum FameStatus {

        OK,
        NOT_TODAY,
        NOT_THIS_MONTH
    }

    public class PlayerObservable extends Observable {

        final MapleCharacter player;

        public PlayerObservable(MapleCharacter player) {
            this.player = player;
        }

        public MapleCharacter getPlayer() {
            return player;
        }

        public void update() {
            setChanged();
            notifyObservers(player);
        }
    }

    public List<String> getScriptManagerDebug()  {
        return scriptManagerDebug;
    }

}
