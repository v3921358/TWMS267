package Server.world;

import Client.*;
import Client.anticheat.CheatTracker;
import Client.anticheat.ReportType;
import Client.inventory.*;
import Client.skills.InnerSkillEntry;
import Client.skills.SkillEntry;
import Client.skills.SkillMacro;
import Net.server.MapleTrunk;
import Net.server.cashshop.CashShop;
import Net.server.shop.MapleShopItem;
import SwordieX.client.character.keys.FuncKeyMap;
import tools.Pair;

import java.util.*;
import java.util.Map.Entry;

/**
 * 角色在換頻道時的數據交換
 */

public class CharacterTransfer {

    public final Map<MapleTraitType, Integer> traits = new EnumMap<>(MapleTraitType.class);
    public final List<CharacterNameAndId> buddies = new LinkedList<>();
    public final Map<Integer, MapleQuestStatus> Quest = new LinkedHashMap<>();
    public final Map<Integer, SkillEntry> skills = new LinkedHashMap<>();
    public final Map<String, Integer> credit;
    public final Map<Byte, Integer> reports = new LinkedHashMap<>();
    public final List<Pair<Integer, Integer>> quickslot;
    public final Map<Integer, String> InfoQuest;
    public final Map<String, String> KeyValue;
    public final Map<Integer, String> worldShareInfo;
    public final Map<Integer, VCoreSkillEntry> vcoreskills = new LinkedHashMap<>();
    public final Map<Integer, VMatrixSlot> vMatrixSlot;
    public Map<Integer, Pair<Integer, SkillEntry>> sonOfLinedSkills = new LinkedHashMap<>();
    public Map<Integer, FuncKeyMap> funcKeyMaps = new LinkedHashMap<>();
    public int characterid, accountid, fame, pvpExp, pvpPoints,
            hair, face, mapid, guildid,
            partyid, messengerid, maplePoint, /*ACash,*/
            mount_itemid, mount_exp, points, vpoints, marriageId, maxhp, maxmp, hp, mp, friendshiptoadd,
            familyid, seniorid, junior1, junior2, currentrep, totalrep, gachexp, guildContribution, totalWins, totalLosses, todayonlinetime, totalonlinetime, weaponPoint;
    public int channel;
    public byte gender;
    public byte guildrank;
    public byte alliancerank;
    public byte buddysize;
    public byte world;
    public byte initialSpawnPoint;
    public byte skinColor;
    public byte mount_level;
    public byte mount_Fatigue;
    public byte subcategory;
    public long lastfametime, TranferTime, exp, meso;
    public String name, accountname, BlessOfFairy, BlessOfEmpress, chalkboard, tempIP;
    public int level, gmLevel;
    public short str;
    public short dex;
    public short int_;
    public short luk;
    public short remainingAp;
    public short hpApUsed;
    public short mpApUsed;
    public short job;
    public short fatigue;
    public int soulcount;
    public MapleInventory[] inventorys;
    public SkillMacro[] skillmacro = new SkillMacro[5];
    public MapleTrunk storage;
    public CashShop cs;
    public CheatTracker anticheat;
    public InnerSkillEntry[] innerSkills;
    public int[] savedlocation, wishlist, rocks, remainingSp, regrocks, hyperrocks, friendshippoints;
    public MaplePet[] spawnPets;
    public MapleImp[] imps;
    public List<Integer> famedcharacters = null, battledaccs = null;
    public List<MapleShopItem> rebuy = null;
    public int decorate;
    public int beans;
    public int warning;
    public int reborns, reborns1, reborns2, reborns3, apstorage;
    public int honor;
    public int love;
    public long lastLoveTime;
    public Map<Integer, Long> loveCharacters = null;
    public int playerPoints;
    public int playerEnergy;
    public MaplePvpStats pvpStats;
    public int pvpDeaths;
    public int pvpKills;
    public int pvpVictory;
    public MaplePotionPot potionPot;
    public PlayerSpecialStats SpecialStats;
    public MapleSigninStatus signinStatus;
    public List<Integer> effectSwitch;
    public List<MonsterFamiliar> familiars;
    public Map<Byte, List<Item>> extendedSlots = null;
    public long ltime;
    public MapleUnion mapleUnion;
    public MonsterFamiliar summonedFamiliar;
    public long loginTime;
    public int onlineTime;
    public int soulSkillID;
    public short soulOptionID;
    public int soulMP;
    public int maxSoulMP;
    public Map<Integer, List<Integer>> salon;
    public Map<Integer, Pair<Integer, Long>> fairys;
    public int burningChrType;
    public long burningChrTime;

    public CharacterTransfer() {
        famedcharacters = new ArrayList<>();
        battledaccs = new ArrayList<>();
        extendedSlots = new HashMap<>();
        loveCharacters = new LinkedHashMap<>();
        rebuy = new ArrayList<>();
        KeyValue = new LinkedHashMap<>();
        InfoQuest = new LinkedHashMap<>();
        worldShareInfo = new LinkedHashMap<>();
        quickslot = new ArrayList<>();
        credit = new LinkedHashMap<>();
        vMatrixSlot = new TreeMap<>();
        salon = new HashMap<>();
    }

    public CharacterTransfer(MapleCharacter chr) {
        this(chr, chr.getClient().getChannel());
    }

    public CharacterTransfer(MapleCharacter chr, int channel) {
        this.characterid = chr.getId();
        this.accountid = chr.getAccountID();
        this.accountname = chr.getClient().getAccountName();
        this.maplePoint = chr.getClient().getMaplePoints();
        this.gmLevel = chr.getClient().getGmLevel();
        this.channel = channel;


        this.vpoints = chr.getVPoints();
        this.name = chr.getName();
        this.fame = chr.getFame();
        this.love = chr.getLove();
        this.gender = chr.getGender();
        this.level = chr.getLevel();
        this.str = chr.getStat().getStr();
        this.dex = chr.getStat().getDex();
        this.int_ = chr.getStat().getInt();
        this.luk = chr.getStat().getLuk();
        this.hp = chr.getStat().getHp();
        this.mp = chr.getStat().getMp();
        this.maxhp = chr.getStat().getMaxHp(false);
        this.maxmp = chr.getStat().getMaxMp(false);
        this.exp = chr.getExp();
        this.hpApUsed = chr.getHpApUsed();
        this.mpApUsed = chr.getMpApUsed();
        this.remainingAp = chr.getRemainingAp();
        this.remainingSp = chr.getRemainingSps();
        this.meso = chr.getMeso();
        this.pvpExp = chr.getTotalBattleExp();
        this.pvpPoints = chr.getBattlePoints();
        this.skinColor = chr.getSkinColor();
        this.job = chr.getJob();
        this.hair = chr.getHair();
        this.face = chr.getFace();
        this.mapid = chr.getMapId();
        this.initialSpawnPoint = chr.getInitialSpawnpoint();
        this.marriageId = chr.getMarriageId();
        this.world = chr.getWorld();
        this.guildid = chr.getGuildId();
        this.guildrank = chr.getGuildRank();
        this.guildContribution = chr.getGuildContribution();
        this.alliancerank = chr.getAllianceRank();
        this.points = chr.getPoints();
        this.fairys = chr.getFairys();
        this.spawnPets = chr.getSpawnPets();
        this.subcategory = chr.getSubcategory();
        this.imps = chr.getImps();
        this.fatigue = chr.getFatigue();
        this.currentrep = chr.getCurrentRep();
        this.totalrep = chr.getTotalRep();
        this.totalWins = chr.getTotalWins();
        this.totalLosses = chr.getTotalLosses();
        this.gachexp = chr.getGachExp();
        this.anticheat = chr.getCheatTracker();
        this.tempIP = chr.getClient().getTempIP();
        this.rebuy = chr.getRebuy();
        this.decorate = chr.getDecorate();
        this.beans = chr.getBeans();
        this.warning = chr.getWarning();
        this.reborns = chr.getReborns();
        this.reborns1 = chr.getReborns1();
        this.reborns2 = chr.getReborns2();
        this.reborns3 = chr.getReborns3();
        this.apstorage = chr.getAPS();
        this.honor = chr.getHonor();
        this.playerPoints = chr.getPlayerPoints();
        this.playerEnergy = chr.getPlayerEnergy();
        this.pvpDeaths = chr.getPvpDeaths();
        this.pvpKills = chr.getPvpKills();
        this.pvpVictory = chr.getPvpVictory();
        this.friendshiptoadd = chr.getFriendShipToAdd();
        this.friendshippoints = chr.getFriendShipPoints();
        this.soulcount = chr.getSoulMP();
        summonedFamiliar = chr.getSummonedFamiliar();

        for (MapleTraitType t : MapleTraitType.values()) {
            this.traits.put(t, chr.getTrait(t).getTotalExp());
        }
        for (BuddylistEntry qs : chr.getBuddylist().getBuddies()) {
            this.buddies.add(new CharacterNameAndId(qs.getCharacterId(), qs.getName(), qs.getGroup(), qs.isVisible()));
        }
        for (Entry<ReportType, Integer> ss : chr.getReports().entrySet()) {
            this.reports.put(ss.getKey().i, ss.getValue());
        }
        this.buddysize = chr.getBuddyCapacity();

        this.partyid = chr.getParty() == null ? -1 : chr.getParty().getId();

        if (chr.getMessenger() != null) {
            this.messengerid = chr.getMessenger().getId();
        } else {
            this.messengerid = 0;
        }
        this.KeyValue = chr.getKeyValue_Map();
        this.InfoQuest = chr.getInfoQuest_Map();
        this.worldShareInfo = chr.getWorldShareInfo();
        for (Entry<Integer, MapleQuestStatus> qs : chr.getQuest_Map().entrySet()) {
            this.Quest.put(qs.getKey(), qs.getValue());
        }
        this.inventorys = chr.getInventorys();
        chr.getSkills().forEach(skills::put);
        chr.getSonOfLinkedSkills().forEach(sonOfLinedSkills::put);
        chr.getVCoreSkill().forEach(vcoreskills::put);
        this.SpecialStats = chr.getSpecialStat();
        this.BlessOfFairy = chr.getBlessOfFairyOrigin();
        this.BlessOfEmpress = chr.getBlessOfEmpressOrigin();
        this.chalkboard = chr.getChalkboard();
        this.skillmacro = chr.getMacros();
        this.innerSkills = chr.getInnerSkills();
        this.funcKeyMaps = chr.getFuncKeyMaps();
        this.quickslot = chr.getQuickSlot().Layout();
        this.savedlocation = chr.getSavedLocations();
        this.wishlist = chr.getWishlist();
        this.rocks = chr.getRocks();
        this.regrocks = chr.getRegRocks();
        this.hyperrocks = chr.getHyperRocks();
        this.famedcharacters = chr.getFamedCharacters();
        this.battledaccs = chr.getBattledCharacters();
        this.lastfametime = chr.getLastFameTime();
        this.storage = chr.getTrunk();
        this.pvpStats = chr.getPvpStats();
        this.potionPot = chr.getPotionPot();
        this.cs = chr.getCashInventory();
        this.extendedSlots = chr.getAllExtendedSlots();
        MapleMount mount = chr.getMount();
        this.mount_itemid = mount.getItemId();
        this.mount_Fatigue = mount.getFatigue();
        this.mount_level = mount.getLevel();
        this.mount_exp = mount.getExp();
        this.lastLoveTime = chr.getLastLoveTime();
        this.loveCharacters = chr.getLoveCharacters();
        TranferTime = System.currentTimeMillis();
        this.todayonlinetime = chr.getTodayOnlineTime();
        this.totalonlinetime = chr.getTotalOnlineTime();
        this.weaponPoint = chr.getWeaponPoint();
        this.credit = chr.getCredits();
        this.signinStatus = chr.getSigninStatus();
        this.effectSwitch = chr.getEffectSwitch();
        this.familiars = chr.getFamiliars();
        this.ltime = chr.getLTime();
        this.mapleUnion = chr.getMapleUnion();
        this.vMatrixSlot = chr.getVMatrixSlot();
        this.loginTime = chr.getLoginTime();
        this.onlineTime = chr.getLastOnlineTime();
        this.soulMP = chr.getSoulMP();
        this.soulSkillID = chr.getSoulSkillID();
        this.soulOptionID = chr.getSoulOption();
        this.maxSoulMP = chr.getMaxSoulMP();
        this.salon = chr.getSalon();
        this.burningChrType = chr.getBurningChrType();
        this.burningChrTime = chr.getBurningChrTime();
    }

}