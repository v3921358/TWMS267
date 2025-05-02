package Net.server;

import Client.MapleCharacter;
import Client.MapleTraitType;
import Client.VMatrixOption;
import Client.inventory.*;
import Client.skills.SkillFactory;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Config.constants.enums.UserChatMessageType;
import Database.DatabaseLoader.DatabaseConnection;
import Database.tools.SqlTool;
import Net.server.buffs.MapleStatEffect;
import Net.server.buffs.MapleStatEffectFactory;
import Net.server.collection.SoulCollectionEntry;
import Net.server.factory.MobCollectionFactory;
import Plugin.provider.*;
import Server.auction.AuctionServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.*;
import tools.json.JSONObject;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapleItemInformationProvider {

    private static final Logger log = LoggerFactory.getLogger(MapleItemInformationProvider.class);
    private static final MapleItemInformationProvider instance = new MapleItemInformationProvider();
    protected final MapleDataProvider chrData = MapleDataProviderFactory.getCharacter();
    protected final MapleDataProvider etcData = MapleDataProviderFactory.getEtc();
    protected final MapleDataProvider itemData = MapleDataProviderFactory.getItem();
    protected final MapleDataProvider stringData = MapleDataProviderFactory.getString();
    protected final MapleDataProvider effectData = MapleDataProviderFactory.getEffect();
    protected final Map<Integer, StructFamiliar> familiars = new HashMap<>();
    protected final Map<Integer, LinkedList<StructItemOption>> familiar_option = new HashMap<>();
    protected final Map<Integer, StructSetItem> familiarSets = new HashMap<>();
    protected final Map<Integer, Map<Integer, Float>> familiarTable_pad = new HashMap<>();
    protected final Map<Integer, Map<Integer, Short>> familiarTable_rchance = new HashMap<>();
    protected final Map<Integer, Integer> familiarTable_fee_reinforce = new HashMap<>();
    protected final Map<Integer, Integer> familiarTable_fee_evolve = new HashMap<>();
    protected final Map<String, List<Triple<String, Point, Point>>> afterImage = new HashMap<>();
    protected final Map<Integer, MapleStatEffect> itemEffects = new HashMap<>();
    protected final Map<Integer, MapleStatEffect> itemEffectsEx = new HashMap<>();
    protected final Map<Integer, Integer> mobIds = new HashMap<>();
    protected final Map<Integer, Pair<Integer, Integer>> potLife = new HashMap<>(); //itemid to lifeid, levels
    protected final Map<Integer, StructAndroid> androidInfo = new HashMap<>(); //智能機器人的具體信息
    protected final Map<Integer, StructCrossHunterShop> crossHunterShop = new HashMap<>(); //十字獵人商店數據
    protected final Map<Integer, Integer> exclusiveEquip = new HashMap<>(); //禁止重複穿戴的道具數據 by 編號ID
    protected final Map<Integer, StructExclusiveEquip> exclusiveEquipInfo = new HashMap<>(); //禁止重複穿戴的道具數據 by 道具ID
    protected final Map<Integer, Integer> soulSkill = new TreeMap<>();
    protected final Map<Integer, ArrayList<Integer>> tempOption = new TreeMap<>();
    protected final Map<Integer, Pair<Integer, Integer>> socketReqLevel = new TreeMap<>();
    protected final Map<Integer, Integer> damageSkinBox = new TreeMap<>(Integer::compareTo);
    protected final Map<Integer, Integer> damageSkinBox_invert = new TreeMap<>(Integer::compareTo);
    protected final List<Integer> setItemInfoEffs = new ArrayList<>();
    protected final Map<Integer, VCoreDataEntry> coreDatas = new HashMap<>();
    protected final Map<Integer, Map<Integer, Triple<Integer, Integer, Integer>>> vcores = new HashMap<>();
    protected final Map<Integer, List<Pair<Integer, String>>> coreJobSkills = new HashMap<>();

    private static final Map<Integer, String> itemName = new HashMap<>();
    private static final Map<Integer, String> itemDesc = new HashMap<>();
    private static final Map<Integer, String> itemMsg = new HashMap<>();
    private static final Map<Integer, Map<String, Object>> itemDataCache = new HashMap<>();
    private static final Map<Integer, List<StructItemOption>> potentialData = new HashMap<>();
    private static final Map<Integer, Map<Integer, StructItemOption>> socketData = new HashMap<>();
    private static final Map<Integer, StructSetItem> setItemInfo = new HashMap<>();
    private static final Map<Integer, Map<Integer, Map<String, Integer>>> sealedEquipInfo = new HashMap<>();

    private final Map<Integer, Integer> soulItems = new HashMap<>();
    private final Map<Integer, Integer> soulSkills = new HashMap<>();
    private final Map<Integer, SoulCollectionEntry> soulCollections = new HashMap<>();

    public static MapleItemInformationProvider getInstance() {
        return instance;
    }

    public List<Integer> getSetItemInfoEffs() {
        return setItemInfoEffs;
    }

    public void runEtc() {
        /*
         * 加載機器人信息
         */
        MapleDataDirectoryEntry e = (MapleDataDirectoryEntry) etcData.getRoot().getEntry("Android");
        for (MapleDataEntry d : e.getFiles()) {
            MapleData iz = etcData.getData("Android/" + d.getName());
            StructAndroid android = new StructAndroid();
            int type = Integer.parseInt(d.getName().substring(0, 4));
            android.type = type;
            android.gender = MapleDataTool.getIntConvert("info/gender", iz, 0);
            android.shopUsable = MapleDataTool.getIntConvert("info/shopUsable", iz, 0) == 1;
            for (MapleData ds : iz.getChildByPath("costume/skin")) { //皮膚
                android.skin.add(MapleDataTool.getInt(ds, 2000));
            }
            for (MapleData ds : iz.getChildByPath("costume/hair")) { //髮型
                android.hair.add(MapleDataTool.getInt(ds, android.gender == 0 ? 20101 : 21101));
            }
            for (MapleData ds : iz.getChildByPath("costume/face")) { //臉型
                android.face.add(MapleDataTool.getInt(ds, android.gender == 0 ? 30110 : 31510));
            }
            androidInfo.put(type, android);
        }
        /*
         * 加載十字獵人商店數據
         */
        MapleData shopData = etcData.getData("CrossHunterChapter.img").getChildByPath("Shop");
        for (MapleData dat : shopData) {
            int key = Integer.parseInt(dat.getName());
            StructCrossHunterShop shop = new StructCrossHunterShop(MapleDataTool.getIntConvert("itemId", dat, 0), MapleDataTool.getIntConvert("tokenPrice", dat, -1), MapleDataTool.getIntConvert("potentialGrade", dat, 0));
            crossHunterShop.put(key, shop);
        }
        /*
         * 道具寶寶
         */
        MapleData lifesData = etcData.getData("ItemPotLifeInfo.img");
        for (MapleData d : lifesData) {
            if (d.getChildByPath("info") != null && MapleDataTool.getInt("type", d.getChildByPath("info"), 0) == 1) {
                potLife.put(MapleDataTool.getInt("counsumeItem", d.getChildByPath("info"), 0), new Pair<>(Integer.parseInt(d.getName()), d.getChildByPath("level").getChildren().size()));
            }
        }
        Optional.ofNullable(etcData.getData("AuctionData.img")).ifPresent(auctionData -> {
            for (final MapleData a1372 : auctionData) {
                final HashMap<Integer, Map<Integer, Map<Integer, Pair<Integer, Integer>>>> hashMap10 = new HashMap<>();
                final MapleData b14;
                if (a1372.getName().startsWith("ItemCategory_") && (b14 = auctionData.getChildByPath("ItemDetailCategory_" + a1372.getName().replace("ItemCategory_", ""))) != null) {
                    for (MapleData anA1372 : a1372) {
                        final MapleData b15 = b14.getChildByPath(anA1372.getName());
                        if ((b15) != null) {
                            final HashMap<Integer, Map<Integer, Pair<Integer, Integer>>> hashMap11 = new HashMap<>();
                            for (final MapleData a1373 : b15) {
                                final HashMap<Integer, Pair<Integer, Integer>> hashMap12 = new HashMap<>();
                                for (MapleData a1374 : a1373) {
                                    if (!a1374.getChildren().isEmpty() && a1374.getName().length() < 4) {
                                        hashMap12.put(Integer.parseInt(a1374.getName()), new Pair<>(MapleDataTool.getIntConvert("begin", a1374), MapleDataTool.getIntConvert("end", a1374)));
                                    }
                                }
                                hashMap11.put(hashMap11.size(), hashMap12);
                            }
                            hashMap10.put(hashMap10.size(), hashMap11);
                        }
                    }
                    AuctionServer.auctions.put(AuctionServer.auctions.size(), hashMap10);
                }
            }
        });

        List<Triple<String, Point, Point>> thePointK = new ArrayList<>();
        List<Triple<String, Point, Point>> thePointA = new ArrayList<>();

        MapleDataDirectoryEntry a = (MapleDataDirectoryEntry) chrData.getRoot().getEntry("Afterimage");
        for (MapleDataEntry b : a.getFiles()) {
            MapleData iz = chrData.getData("Afterimage/" + b.getName());
            List<Triple<String, Point, Point>> thePoint = new ArrayList<>();
            Map<String, Pair<Point, Point>> dummy = new HashMap<>();
            for (MapleData i : iz) {
                for (MapleData xD : i) {
                    if (xD.getName().contains("prone") || xD.getName().contains("double") || xD.getName().contains("triple")) {
                        continue;
                    }
                    if ((b.getName().contains("bow") || b.getName().contains("Bow")) && !xD.getName().contains("shoot")) {
                        continue;
                    }
                    if ((b.getName().contains("gun") || b.getName().contains("cannon")) && !xD.getName().contains("shot")) {
                        continue;
                    }
                    if (dummy.containsKey(xD.getName())) {
                        if (xD.getChildByPath("lt") != null) {
                            Point lt = (Point) xD.getChildByPath("lt").getData();
                            Point ourLt = dummy.get(xD.getName()).left;
                            if (lt.x < ourLt.x) {
                                ourLt.x = lt.x;
                            }
                            if (lt.y < ourLt.y) {
                                ourLt.y = lt.y;
                            }
                        }
                        if (xD.getChildByPath("rb") != null) {
                            Point rb = (Point) xD.getChildByPath("rb").getData();
                            Point ourRb = dummy.get(xD.getName()).right;
                            if (rb.x > ourRb.x) {
                                ourRb.x = rb.x;
                            }
                            if (rb.y > ourRb.y) {
                                ourRb.y = rb.y;
                            }
                        }
                    } else {
                        Point lt = null, rb = null;
                        if (xD.getChildByPath("lt") != null) {
                            lt = (Point) xD.getChildByPath("lt").getData();
                        }
                        if (xD.getChildByPath("rb") != null) {
                            rb = (Point) xD.getChildByPath("rb").getData();
                        }
                        dummy.put(xD.getName(), new Pair<>(lt, rb));
                    }
                }
            }
            for (Entry<String, Pair<Point, Point>> ez : dummy.entrySet()) {
                if (ez.getKey().length() > 2 && ez.getKey().substring(ez.getKey().length() - 2, ez.getKey().length() - 1).equals("D")) { //D = double weapon
                    thePointK.add(new Triple<>(ez.getKey(), ez.getValue().left, ez.getValue().right));
                } else if (ez.getKey().contains("PoleArm")) { //D = double weapon
                    thePointA.add(new Triple<>(ez.getKey(), ez.getValue().left, ez.getValue().right));
                } else {
                    thePoint.add(new Triple<>(ez.getKey(), ez.getValue().left, ez.getValue().right));
                }
            }
            afterImage.put(b.getName().substring(0, b.getName().length() - 4), thePoint);
        }
        afterImage.put("katara", thePointK); //hackish
        afterImage.put("aran", thePointA); //hackish
        //加載禁止重複穿戴的道具信息
        MapleData exclusiveEquipData = etcData.getData("ExclusiveEquip.img");
        StructExclusiveEquip exclusive;
        int exId;
        for (MapleData dat : exclusiveEquipData) {
            exclusive = new StructExclusiveEquip();
            exId = Integer.parseInt(dat.getName()); //編號ID
            String msg = MapleDataTool.getString("msg", dat, "");
            msg = msg.replace("\\r\\n", "\r\n");
            msg = msg.replace("-------<", "---<");
            msg = msg.replace(">------", ">---");
            exclusive.id = exId;
            exclusive.msg = msg;
            for (MapleData level : dat.getChildByPath("item")) {
                int itemId = MapleDataTool.getInt(level);
                exclusive.itemIDs.add(itemId);
                exclusiveEquip.put(itemId, exId);
            }
            exclusiveEquipInfo.put(exId, exclusive);
        }
        //自定義 老公老婆的戒指 編號 100
        exclusive = new StructExclusiveEquip();
        exId = 100; //編號ID
        exclusive.msg = "只能佩戴一個\r\n老公老婆的戒指。";
        for (int i = 1112446; i <= 1112495; i++) {
            exclusive.itemIDs.add(i);
            exclusiveEquip.put(i, exId);
        }
        exclusiveEquipInfo.put(exId, exclusive);
        //自定義 不速之客的戒指 編號 101
        exclusive = new StructExclusiveEquip();
        exId = 101; //編號ID
        exclusive.msg = "只能佩戴一個\r\n不速之客的戒指。";
        for (int i = 1112435; i <= 1112439; i++) {
            exclusive.itemIDs.add(i);
            exclusiveEquip.put(i, exId);
        }
        exclusiveEquipInfo.put(exId, exclusive);
        //自定義 十字旅團的戒指 編號 102
        exclusive = new StructExclusiveEquip();
        exId = 102; //編號ID
        exclusive.msg = "只能佩戴一個\r\n十字旅團的戒指。";
        for (int i = 1112599; i <= 1112613; i++) {
            exclusive.itemIDs.add(i);
            exclusiveEquip.put(i, exId);
        }
        exclusiveEquipInfo.put(exId, exclusive);
        DatabaseConnection.domain(con -> {
            List<Pair<Integer, Integer>> list = SqlTool.queryAndGetList("SELECT `itemid`, `skinid` FROM `damageskin`", rs -> new Pair<>(rs.getInt("itemid"), rs.getInt("skinid")));
            for (Pair<Integer, Integer> it : list) {
                if (damageSkinBox_invert.getOrDefault(it.right, -2) == -2) {
                    SqlTool.update(con, "DELETE FROM `damageskin` WHERE `itemid` = ? AND `skinid` = ?", it.left, it.right);
                    System.err.println("從WZ資料找不到ID為[ " + it.right + " ]的傷害皮膚，配置的道具ID[ " + it.left + " ]，從資料庫清理這個配置");
                    continue;
                }
                if (it.left <= 0 || damageSkinBox.containsKey(it.left)) {
                    SqlTool.update(con, "DELETE FROM `damageskin` WHERE `itemid` = ? AND `skinid` = ?", it.left, it.right);
                    if (it.left > 0) {
                        System.err.println("WZ資料已經存在[ skin:" + it.left + " ]的傷害皮膚配置，從資料庫清理這個配置");
                    }
                } else {
                    damageSkinBox.put(it.left, it.right);
                    if (!damageSkinBox_invert.containsKey(it.right) || damageSkinBox_invert.get(it.right) == -1) {
                        damageSkinBox_invert.put(it.right, it.left);
                    }
                }
            }
            return null;
        });

        for (MapleData mapleData : effectData.getData("ItemEff.img")) {
            setItemInfoEffs.add(Integer.valueOf(mapleData.getName()));
        }
        Collections.reverse(setItemInfoEffs);

        Optional.ofNullable(etcData.getData("VMatrixOption.img")).ifPresent(vmatrixData -> {
            Optional.ofNullable(vmatrixData.getChildByPath("info")).ifPresent(info -> {
                VMatrixOption.SlotMax = MapleDataTool.getInt("slotMax", info, 500);
                VMatrixOption.EquipSlotMin = MapleDataTool.getInt("equipSlotMin", info, 4);
                VMatrixOption.EquipSlotMax = MapleDataTool.getInt("equipSlotMax", info, 14);
                VMatrixOption.SpecialSlotMax = MapleDataTool.getInt("specialSlotMax", info, 1);
                VMatrixOption.ExtendLevel = MapleDataTool.getInt("extendLevel", info, 5);
                VMatrixOption.ExtendAF = MapleDataTool.getInt("extendAF", info, 0);
                VMatrixOption.GradeMax = MapleDataTool.getInt("gradeMax", info, 25);
                VMatrixOption.TotalGradeMax = MapleDataTool.getInt("totalGradeMax", info, 50);
                VMatrixOption.CraftSkillCoreCost = MapleDataTool.getInt("craftSkillCoreCost", info, 140);
                VMatrixOption.CraftEnchantCoreCost = MapleDataTool.getInt("craftEnchantCoreCost", info, 70);
                VMatrixOption.CraftSpecialCoreCost = MapleDataTool.getInt("craftSpecialCoreCost", info, 250);
                VMatrixOption.CraftGemstoneCost = MapleDataTool.getInt("craftGemstoneCost", info, 35);
                VMatrixOption.MatrixPointResetMeso = MapleDataTool.getInt("matrixPointResetMeso", info, 1000000);
                VMatrixOption.EquipSlotEnhanceMax = MapleDataTool.getInt("equipSlotEnhanceMax", info, 5);
            });
            Optional.ofNullable(vmatrixData.getChildByPath("slotExpansionMeso")).ifPresent(slotem -> {
                slotem.forEach(data -> {
                    Map<Integer, Long> map = new HashMap<>();
                    for (MapleData d : data) {
                        map.put(Integer.parseInt(d.getName()), MapleDataTool.getLong(d, 278440000));
                    }
                    VMatrixOption.SlotExpansionMeso.put(Integer.parseInt(data.getName()), map);
                });
            });
        });

        MobCollectionFactory.init(etcData.getData("mobCollection.img"));

        MapleData vcoreData = etcData.getData("VCore.img");
        for (MapleData coreData : vcoreData.getChildByPath("CoreData")) {
            VCoreDataEntry vCoreDataEntry = new VCoreDataEntry();
            vCoreDataEntry.setId(Integer.valueOf(coreData.getName()));
            vCoreDataEntry.setName(MapleDataTool.getString("name", coreData, ""));
            vCoreDataEntry.setDesc(MapleDataTool.getString("desc", coreData, ""));
            vCoreDataEntry.setType(MapleDataTool.getInt("type", coreData, 0));
            vCoreDataEntry.setMaxLevel(MapleDataTool.getInt("maxLevel", coreData, 0));
            vCoreDataEntry.setNobAbleGemStone(MapleDataTool.getInt("nobAbleGemStone", coreData, 0) == 1);
            vCoreDataEntry.setNotAbleCraft(MapleDataTool.getInt("notAbleCraft", coreData, 0) == 1);
            vCoreDataEntry.setDisassemble(MapleDataTool.getInt("noDisassemble", coreData, 0) == 1);
            MapleData job = coreData.getChildByPath("job");
            if (job != null) {
                for (MapleData jobData : job) {
                    String sJob = MapleDataTool.getString(jobData, "");
                    if (!sJob.isEmpty()) {
                        vCoreDataEntry.addJob(sJob);
                    }
                }
            }
            MapleData connectSkill1 = coreData.getChildByPath("connectSkill");
            if (connectSkill1 != null) {
                for (MapleData connectSkill : connectSkill1) {
                    int anInt = MapleDataTool.getInt(connectSkill, 0);
                    if (anInt > 0) {
                        vCoreDataEntry.getConnectSkill().add(anInt);
                    }
                }
            }
            coreDatas.put(vCoreDataEntry.getId(), vCoreDataEntry);
        }
        int vcoresindex = 0;
        for (MapleData enforcement : vcoreData.getChildByPath("Enforcement")) {
            HashMap<Integer, Triple<Integer, Integer, Integer>> map = new HashMap<>();
            for (MapleData subdata : enforcement) {
                map.put(Integer.valueOf(subdata.getName()), new Triple<>(MapleDataTool.getInt("nextExp", subdata, 0), MapleDataTool.getInt("expEnforce", subdata, 0), MapleDataTool.getInt("extract", subdata, 0)));
            }
            vcores.put(vcoresindex++, map);
        }
        for (MapleData jobSkill : vcoreData.getChildByPath("JobSkill")) {
            if (!jobSkill.getName().matches("^\\d+$")) {
                continue;
            }
            for (MapleData subdata : jobSkill) {
                coreJobSkills.computeIfAbsent(Integer.parseInt(jobSkill.getName()), k -> new ArrayList<>()).add(new Pair<>(MapleDataTool.getInt("id", subdata, 0), MapleDataTool.getString("name", subdata, "")));
            }
        }

        //加載靈魂收集
        Optional.ofNullable(etcData.getData("SoulCollection.img")).ifPresent(soulCollectionData -> {
            for (MapleData data : soulCollectionData) {
                final int id = Integer.valueOf((data).getName());
                final SoulCollectionEntry entry = new SoulCollectionEntry();
                final int soulSkill = MapleDataTool.getInt("soulSkill", data, -1);
                final int soulSkillH = MapleDataTool.getInt("soulSkillH", data, -1);
                final MapleData soulList;
                if ((soulList = data.getChildByPath("soulList")) != null) {
                    entry.setSoulSkill(soulSkill);
                    entry.setSoulSkillH(soulSkillH);
                    soulSkills.put(soulSkill, id);
                    soulSkills.put(soulSkillH, id);
                    for (MapleData soul : soulList) {
                        final int soulid = Integer.valueOf((soul).getName());
                        final int a3 = MapleDataTool.getInt("0", soul, -1);
                        final int a4 = MapleDataTool.getInt("1", soul, -1);
                        if (Integer.parseInt(soul.getName()) == 8 && soulSkillH > 0 && a3 > 0) {
                            soulItems.put(a3, soulSkillH);
                            entry.getItems().put(a3, soulid);
                        } else {
                            if (a3 > 0) {
                                soulItems.put(a3, soulSkill);
                                entry.getItems().put(a3, soulid);
                            }
                            if (a4 > 0) {
                                soulItems.put(a4, soulSkill);
                                entry.getItems().put(a4, soulid);
                            }
                        }
                    }
                    soulCollections.put(id, entry);
                }
            }
        });

    }

    /**
     * 加載套裝屬性 暫無使用 By ShaoFan 2017.8.10
     */
    public void loadSetItemData() {
        MapleData setsData = etcData.getData("SetItemInfo.img");
        StructSetItem SetItem;
        StructSetItemStat SetItemStat;
        for (MapleData dat : setsData) {
            SetItem = new StructSetItem();
            SetItem.setItemID = Integer.parseInt(dat.getName()); //套裝ID
            SetItem.setItemName = MapleDataTool.getString("setItemName", dat, ""); //套裝名字
            SetItem.completeCount = (byte) MapleDataTool.getIntConvert("completeCount", dat, 0); //套裝總數
            for (MapleData level : dat.getChildByPath("ItemID")) {
                if (level.getType() != MapleDataType.INT) {
                    for (MapleData leve : level) {
                        if (!leve.getName().equals("representName") && !leve.getName().equals("typeName")) {
                            try {
                                SetItem.itemIDs.add(MapleDataTool.getIntConvert(leve));
                            } catch (Exception e) {
                                System.err.println("出錯數據： leve = " + leve.getData());
                            }
                        }
                    }
                } else {
                    SetItem.itemIDs.add(MapleDataTool.getInt(level));
                }
            }
            for (MapleData level : dat.getChildByPath("Effect")) {
                SetItemStat = new StructSetItemStat();
                SetItemStat.incSTR = MapleDataTool.getIntConvert("incSTR", level, 0);
                SetItemStat.incDEX = MapleDataTool.getIntConvert("incDEX", level, 0);
                SetItemStat.incINT = MapleDataTool.getIntConvert("incINT", level, 0);
                SetItemStat.incLUK = MapleDataTool.getIntConvert("incLUK", level, 0);
                SetItemStat.incMHP = MapleDataTool.getIntConvert("incMHP", level, 0);
                SetItemStat.incMMP = MapleDataTool.getIntConvert("incMMP", level, 0);
                SetItemStat.incMHPr = MapleDataTool.getIntConvert("incMHPr", level, 0);
                SetItemStat.incMMPr = MapleDataTool.getIntConvert("incMMPr", level, 0);
                SetItemStat.incACC = MapleDataTool.getIntConvert("incACC", level, 0);
                SetItemStat.incEVA = MapleDataTool.getIntConvert("incEVA", level, 0);
                SetItemStat.incPDD = MapleDataTool.getIntConvert("incPDD", level, 0);
                SetItemStat.incMDD = MapleDataTool.getIntConvert("incMDD", level, 0);
                SetItemStat.incPAD = MapleDataTool.getIntConvert("incPAD", level, 0);
                SetItemStat.incMAD = MapleDataTool.getIntConvert("incMAD", level, 0);
                SetItemStat.incJump = MapleDataTool.getIntConvert("incJump", level, 0);
                SetItemStat.incSpeed = MapleDataTool.getIntConvert("incSpeed", level, 0);
                SetItemStat.incAllStat = MapleDataTool.getIntConvert("incAllStat", level, 0);
                SetItemStat.incPQEXPr = MapleDataTool.getIntConvert("incPQEXPr", level, 0);
                SetItemStat.incPVPDamage = MapleDataTool.getIntConvert("incPVPDamage", level, 0);
                SetItemStat.option1 = MapleDataTool.getIntConvert("Option/1/option", level, 0);
                SetItemStat.option2 = MapleDataTool.getIntConvert("Option/2/option", level, 0);
                SetItemStat.option3 = MapleDataTool.getIntConvert("Option/3/option", level, 0);
                SetItemStat.option1Level = MapleDataTool.getIntConvert("Option/1/level", level, 0);
                SetItemStat.option2Level = MapleDataTool.getIntConvert("Option/2/level", level, 0);
                SetItemStat.option3Level = MapleDataTool.getIntConvert("Option/3/level", level, 0);
                SetItemStat.skillId = MapleDataTool.getIntConvert("activeSkill/0/id", level, 0);
                SetItemStat.skillLevel = MapleDataTool.getIntConvert("activeSkill/0/level", level, 0);
                SetItem.setItemStat.put(Integer.parseInt(level.getName()), SetItemStat); //[激活屬性的數量] [激活後的套裝加成屬性]
            }
            setItemInfo.put(SetItem.setItemID, SetItem);
        }
    }

    /**
     * 加載潛能數據
     */
    public void loadPotentialData() {
        StructItemOption item;
        MapleData potsData = itemData.getData("ItemOption.img");
        List<StructItemOption> items;
        for (MapleData dat : potsData) {
            items = new LinkedList<>();
            for (MapleData potLevel : dat.getChildByPath("level")) {
                item = new StructItemOption();
                item.opID = Integer.parseInt(dat.getName());
                item.optionType = MapleDataTool.getIntConvert("info/optionType", dat, 0);
                item.reqLevel = MapleDataTool.getIntConvert("info/reqLevel", dat, 0);
                item.opString = MapleDataTool.getString("info/string", dat, "");
                for (MapleData potData : potLevel) {
                    if (!StructItemOption.types.contains(potData.getName())) {
                        StructItemOption.types.add(potData.getName());
                    }
                    if (potData.getName().equals("face")) {
                        item.face = MapleDataTool.getString(potData, "");
                    } else {
                        item.data.put(potData.getName(), MapleDataTool.getIntConvert(potData, 0));
                    }
                }
                switch (item.opID) {
                    case 31001: //可以使用好用的輕功技能
                    case 31002: //可以使用好用的時空門技能
                    case 31003: //可以使用好用的火眼晶晶技能
                    case 31004: //可以使用好用的神聖之火技能
                        item.data.put("skillID", (item.opID - 23001));
                        break;
                    case 41005: //可以使用強化戰鬥命令技能
                    case 41006: //可以使用強化進階祝福技能
                    case 41007: //可以使用強化極速領域技能
                        item.data.put("skillID", (item.opID - 33001));
                        break;
                }
                items.add(item);
            }
            potentialData.put(Integer.valueOf(dat.getName()), items);
        }
    }

    /**
     * 加載星岩數據
     */
    public void loadSocketData() {
        MapleData nebuliteData = itemData.getData("Install/0306.img");
        StructItemOption item;
        for (MapleData dat : nebuliteData) {
            item = new StructItemOption();
            item.opID = Integer.parseInt(dat.getName()); // Item Id
            item.optionType = MapleDataTool.getInt("optionType", dat.getChildByPath("socket"), 0);
            for (MapleData info : dat.getChildByPath("socket/option")) {
                String optionString = MapleDataTool.getString("optionString", info, "");
                int level = MapleDataTool.getInt("level", info, 0);
                if (level > 0) { // Save memory
                    item.data.put(optionString, level);
                }
            }
            switch (item.opID) {
                case 3063370: // Haste
                    item.data.put("skillID", 8000);
                    break;
                case 3063380: // Mystic Door
                    item.data.put("skillID", 8001);
                    break;
                case 3063390: // Sharp Eyes
                    item.data.put("skillID", 8002);
                    break;
                case 3063400: // Hyper Body
                    item.data.put("skillID", 8003);
                    break;
                case 3064470: // Combat Orders
                    item.data.put("skillID", 8004);
                    break;
                case 3064480: // Advanced Blessing
                    item.data.put("skillID", 8005);
                    break;
                case 3064490: // Speed Infusion
                    item.data.put("skillID", 8006);
                    break;
            }
            socketData.computeIfAbsent(ItemConstants.getNebuliteGrade(item.opID), key -> new HashMap<>());

            socketData.get(ItemConstants.getNebuliteGrade(item.opID)).put(item.opID, item);
        }
    }

    public void loadFamiliarItems() {
        /*
         * 加載萌獸信息
         */
        final MapleDataDirectoryEntry f = (MapleDataDirectoryEntry) chrData.getRoot().getEntry("Familiar");
        final MapleData familiarItemData = itemData.getData("Consume/0287.img");
        for (MapleDataEntry d : f.getFiles()) {
            final int id = Integer.parseInt(d.getName().substring(0, d.getName().length() - 4));
            for (MapleData info : chrData.getData("Familiar/" + d.getName())) {
                if (info.getName().equals("info")) {
                    int skillid = MapleDataTool.getIntConvert("skill/id", info, 0);
                    int effectAfter = MapleDataTool.getIntConvert("skill/effectAfter", info, 0);
                    String FAttribute = MapleDataTool.getString("FAttribute", info, null);
                    int FCategory = MapleDataTool.getIntConvert("FCategory", info, 0);
                    int range = MapleDataTool.getIntConvert("range", info, 0);
                    int mobid = MapleDataTool.getIntConvert("MobID", info, 0);
                    int monsterCardID = MapleDataTool.getIntConvert("monsterCardID", info, 0);
                    byte grade = (byte) MapleDataTool.getIntConvert("0" + monsterCardID + "/info/grade", familiarItemData, 0);
                    familiars.put(id, new StructFamiliar(id, skillid, effectAfter, mobid, monsterCardID, grade, FAttribute, FCategory, range));
                }
            }
        }

        final MapleData familiarTableData = etcData.getData("FamiliarTable.img");
        for (MapleData d : familiarTableData) {
            if (d.getName().equals("stat")) {
                for (MapleData subd : d) {
                    Map<Integer, Float> stats = new HashMap<>();
                    for (MapleData stat : subd) {
                        stats.put(Integer.valueOf(stat.getName()), MapleDataTool.getFloat(stat.getChildByPath("pad"), 0.0f));
                    }
                    familiarTable_pad.put(Integer.valueOf(subd.getName()), stats);
                }
            } else if (d.getName().equals("reinforce_chance")) {
                for (MapleData subd : d) {
                    Map<Integer, Short> chances = new HashMap<>();
                    for (MapleData chance : subd) {
                        chances.put(Integer.valueOf(chance.getName()), (Short) chance.getData());
                    }
                    familiarTable_rchance.put(Integer.valueOf(subd.getName()), chances);
                }
            } else if (d.getName().equals("fee")) {
                for (MapleData level : d.getChildByPath("reinforce").getChildren()) {
                    this.familiarTable_fee_reinforce.put(Integer.parseInt(level.getName()), MapleDataTool.getInt(level, 0));
                }
                for (MapleData level : d.getChildByPath("evolve").getChildren()) {
                    this.familiarTable_fee_evolve.put(Integer.parseInt(level.getName()), MapleDataTool.getInt(level, 0));
                }
            }
        }
        MapleData familiarSet = this.etcData.getData("FamiliarSet.img");
        StructSetItem SetItem;
        StructSetItemStat SetItemStat;
        for (MapleData sub : familiarSet.getChildren()) {
            SetItem = new StructSetItem();
            SetItem.setItemID = Integer.parseInt(sub.getName());
            SetItem.setItemName = MapleDataTool.getString("setName", sub, "");
            for (MapleData familiar : sub.getChildByPath("familiarList").getChildren()) {
                SetItem.itemIDs.add(MapleDataTool.getInt(familiar, 0));
            }
            SetItem.completeCount = (byte) SetItem.itemIDs.size();

            MapleData statsData = sub.getChildByPath("stats");
            SetItemStat = new StructSetItemStat();
            SetItemStat.incSpeed = MapleDataTool.getInt("incSpeed", statsData, 0);
            SetItemStat.incJump = MapleDataTool.getInt("incJump", statsData, 0);
            SetItemStat.incMHP = MapleDataTool.getInt("incMHP", statsData, 0);
            SetItemStat.incMMP = MapleDataTool.getInt("incMMP", statsData, 0);
            SetItemStat.incSTR = MapleDataTool.getInt("incSTR", statsData, 0);
            SetItemStat.incDEX = MapleDataTool.getInt("incDEX", statsData, 0);
            SetItemStat.incINT = MapleDataTool.getInt("incINT", statsData, 0);
            SetItemStat.incLUK = MapleDataTool.getInt("incLUK", statsData, 0);
            SetItemStat.incDEX = MapleDataTool.getInt("incDEX", statsData, 0);
            SetItemStat.incDEX = MapleDataTool.getInt("incDEX", statsData, 0);
            SetItemStat.incPAD = MapleDataTool.getInt("incPAD", statsData, 0);
            SetItemStat.incMAD = MapleDataTool.getInt("incMAD", statsData, 0);
            SetItemStat.incAllStat = MapleDataTool.getInt("incAllStat", statsData, 0);
            SetItem.setItemStat.put((int) SetItem.completeCount, SetItemStat);
            familiarSets.put(SetItem.setItemID, SetItem);
        }

        /*
         * 加載萌獸潛能訊息
         */
        for (final MapleData d : itemData.getData("FamiliarOption.img")) {
            final LinkedList<StructItemOption> options = new LinkedList<>();
            for (final MapleData subd : d.getChildByPath("level")) {
                final StructItemOption option = new StructItemOption();
                option.opID = Integer.parseInt(d.getName());
                option.optionType = MapleDataTool.getInt("info/optionType", d, 0);
                option.reqLevel = MapleDataTool.getInt("info/reqLevel", d, 0);
                option.opString = MapleDataTool.getString("info/string", d, "");
                for (final MapleData subdData : subd) {
                    if (!StructItemOption.types.contains(subdData.getName())) {
                        StructItemOption.types.add(subdData.getName());
                    }
                    if (subdData.getName().equals("face")) {
                        option.face = MapleDataTool.getString(subdData, "");
                    } else {
                        option.data.put(subdData.getName(), MapleDataTool.getIntConvert(subdData, 0));
                    }
                }
                options.add(option);
            }
            familiar_option.put(Integer.parseInt(d.getName()), options);
        }
        //log.info("共加載 " + familiar_option.size() + " 個萌獸潛能訊息."); /* 暫時關閉 */
    }

    public void runItems() {
        if (!itemDataCache.isEmpty()) {
            return;
        }
        AtomicBoolean loadFromSql = new AtomicBoolean(false);
        DatabaseConnection.domain(con -> {
            loadFromSql.set(InitializeServer.WzSqlName.wz_itemdata.check(con));
            return null;
        });
        if (loadFromSql.get()) {//load from sql
            DatabaseConnection.domain(con -> {
                SqlTool.queryAndGetList(con, "SELECT * FROM `wz_itemdata`", rs -> {
                    int itemid = rs.getInt("itemid");
                    itemDataCache.put(itemid, new JSONObject(rs.getString("data")).toMap());
                    String name = rs.getString("name");
                    String desc = rs.getString("desc");
                    String msg = rs.getString("msg");
                    if (!name.equals("")) {
                        itemName.put(itemid, name);
                    }
                    if (!desc.equals("")) {
                        itemDesc.put(itemid, desc);
                    }
                    if (!msg.equals("")) {
                        itemMsg.put(itemid, msg);
                    }
                    return null;
                });
                return null;
            });
        } else {//load from wz and insert into sql
            /*
             * 讀取所有物品名稱、描述
             */
            List<MapleData> mapleDatas = new ArrayList<>();
            for (MapleDataFileEntry filedata : stringData.getRoot().getFiles()) {
                switch (filedata.getName()) {
                    case "Eqp.img": {
                        MapleData data = stringData.getData(filedata.getName()).getChildByPath("Eqp");
                        for (MapleData typedata : data.getChildren()) {
                            mapleDatas.addAll(typedata.getChildren());
                        }
                        break;
                    }
                    case "Consume.img":
                    case "Ins.img":
                    case "Etc.img":
                    case "Cash.img":
                    case "Pet.img": {
                        MapleData data = filedata.getName().startsWith("Etc") ? stringData.getData(filedata.getName()).getChildByPath("Etc") : stringData.getData(filedata.getName());
                        mapleDatas.addAll(data.getChildren());
                        break;
                    }
                    default:
                        break;
                }
            }
            for (MapleData namedata : mapleDatas) {
                int itemid = Integer.parseInt(namedata.getName());
                String name = MapleDataTool.getString(namedata.getChildByPath("name"), "");
                String desc = MapleDataTool.getString(namedata.getChildByPath("desc"), "");
                String msg = MapleDataTool.getString(namedata.getChildByPath("msg"), "");
                itemName.put(itemid, name);
                itemDesc.put(itemid, desc);
                itemMsg.put(itemid, msg);
            }
            /*
             * Load Item Data
             */
            List<MapleDataProvider> dataProviders = new LinkedList<>();
            dataProviders.add(chrData);
            dataProviders.add(itemData);
            for (MapleDataProvider dataProvider : dataProviders) {
                for (MapleDataFileEntry topData : dataProvider.getRoot().getFiles()) {
                    if (dataProvider.equals(chrData) && topData.getName().matches("^\\d+\\.img$")) {
                        addItemDataToRedis(dataProvider.getData(topData.getName()), false);
                    }
                }
                for (MapleDataDirectoryEntry topDir : dataProvider.getRoot().getSubdirectories()) {
                    boolean isSpecial = topDir.getName().equals("Special");
                    if (!topDir.getName().equalsIgnoreCase("Afterimage")) {
                        for (MapleDataFileEntry ifile : topDir.getFiles()) {
                            MapleData iz = dataProvider.getData(topDir.getName() + "/" + ifile.getName());
                            if (dataProvider.equals(chrData) || topDir.getName().equals("Pet")) {
                                if (!iz.getName().equalsIgnoreCase("CommonFaceCN.img") && !iz.getName().equalsIgnoreCase("LinkCashWeaponData.img")) {
                                    addItemDataToRedis(iz, false);
                                }
                            } else {
                                for (MapleData data : iz) {
                                    addItemDataToRedis(data, isSpecial);
                                }
                            }
                        }
                    }
                }
            }
            DatabaseConnection.domain(con -> {
                InitializeServer.WzSqlName.wz_itemdata.drop(con);
                SqlTool.update(con, "CREATE TABLE `wz_itemdata` (`itemid` int NOT NULL,`data` mediumtext NOT NULL,`name` text NOT NULL,`desc` text NOT NULL,`msg` text NOT NULL,PRIMARY KEY (`itemid`))");

                for (Entry<Integer, Map<String, Object>> data : itemDataCache.entrySet()) {
                    SqlTool.update(con, "INSERT INTO `wz_itemdata` (`itemid`,`data`,`name`,`desc`,`msg`) VALUES (?,?,?,?,?)", new Object[]{
                            data.getKey(), new JSONObject(data.getValue()).toString(), itemName.getOrDefault(data.getKey(), ""), itemDesc.getOrDefault(data.getKey(), ""), itemMsg.getOrDefault(data.getKey(), "")
                    });
                }
                InitializeServer.WzSqlName.wz_itemdata.update(con);
                return null;
            });
        }
        //log.info("共加載 " + itemDataCache.size() + " 個道具訊息."); /* 暫時關閉 */
    }

    private void addItemDataToRedis(MapleData data, boolean isSpecial) {
        Map<String, Object> info = new HashMap<>();
        int itemid;
        String id;
        if (data.getName().endsWith(".img")) {
            id = data.getName().substring(0, data.getName().length() - 4).trim();
        } else {
            id = data.getName().trim();
        }
        if (!id.matches("^\\d+$")) {
            return;
        }
        itemid = Integer.parseInt(id);
        try {
            for (MapleData mapleData : data) {
                if (isSpecial) {
                    putSpecialItemInfo(info, mapleData, itemid);
                } else {
                    switch (mapleData.getName()) {
                        case "info":
                        case "req":
                        case "consumeItem":
                        case "mob":
                        case "replace":
                        case "skill":
                        case "reward":
                        case "spec":
                        case "specEx": { // 基本數據
                            info.put(mapleData.getName(), MapleDataTool.getAllMapleData(mapleData));
                            break;
                        }
                    }
                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (info.isEmpty()) {
            return;
        }
        itemDataCache.put(itemid, info);
    }

    /**
     * 添加ID為9開頭的特殊道具的名稱、icon鏈接ID
     */
    private void putSpecialItemInfo(Map<String, Object> info, MapleData mapleData, int itemid) {
        if (mapleData.getName().equalsIgnoreCase("name")) {
            itemName.put(itemid, MapleDataTool.getString(mapleData));
        } else if (mapleData.getName().equalsIgnoreCase("desc")) {
            itemDesc.put(itemid, MapleDataTool.getString(mapleData));
        } else if (mapleData.getName().equalsIgnoreCase("icon")) {
            Map<Object, Object> subinfos = new HashMap<>();
            if (mapleData.getChildren().isEmpty()) {
                String link = mapleData.getData().toString();
                if (!link.isEmpty()) {
                    String[] split = link.split("/");
                    for (int i = 0, splitLength = split.length; i < splitLength; i++) {
                        String s = split[i];
                        if (i == 1 && StringUtil.isNumber(s)) {
                            subinfos.put("_inlink", Integer.valueOf(s));
                            break;
                        }
                    }
                }
            } else {
                for (MapleData mapleData1 : mapleData.getChildren()) {
                    boolean isHash = mapleData1.getName().equals("_hash");
                    boolean isInLink = mapleData1.getName().equals("_inlink");
                    boolean isOutLink = mapleData1.getName().equals("_outlink");
                    if (isHash) {
                        subinfos.put(mapleData1.getName(), mapleData1.getData().toString());
                    } else if (isInLink || isOutLink) {
                        String link = mapleData1.getData().toString();
                        if (!link.isEmpty()) {
                            String[] split = link.split("/");
                            for (int i = 0; i < split.length; i++) {
                                if ((isInLink && i == 0 || isOutLink && i == 3) && StringUtil.isNumber(split[i])) {
                                    subinfos.put(mapleData1.getName(), Integer.valueOf(split[i]));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!subinfos.isEmpty()) {
                info.put("info", subinfos);
            }
        }
    }

    /*
     * 通過ID獲取潛能信息
     */
    public List<StructItemOption> getPotentialInfo(int potId) {
        return potentialData.get(potId);
    }

    /**
     * 獲取全部潛能數據
     *
     * @return
     */
    public Map<Integer, List<StructItemOption>> getAllPotentialInfo() {
        return getPotentialInfos(-1);
    }

    /**
     * 獲取部分潛能數據
     *
     * @param potId
     * @return
     */
    public Map<Integer, List<StructItemOption>> getPotentialInfos(int potId) {
        Map<Integer, List<StructItemOption>> ret = new HashMap<>();
        for (Entry<Integer, List<StructItemOption>> entry : potentialData.entrySet()) {
            if (potId == -1 || entry.getKey() >= potId) {
                ret.put(entry.getKey(), entry.getValue());
            }
        }
        return ret;
    }

    /*
     * 獲取裝備 指定潛能ID 的描述信息
     */
    public String resolvePotentialId(int itemId, int potId) {
        int eqLevel = getReqLevel(itemId);
        int potLevel;
        List<StructItemOption> potInfo = getPotentialInfo(potId);
        if (eqLevel == 0) {
            potLevel = 1;
        } else {
            potLevel = (eqLevel + 1) / 10;
            potLevel++;
        }
        if (potId <= 0) {
            return "沒有潛能屬性";
        }
        StructItemOption itemOption = potInfo.get(potLevel - 1);
        String ret = itemOption.opString;
        for (int i = 0; i < itemOption.opString.length(); i++) {
            //# denotes the beginning of the parameter name that needs to be replaced, e.g. "Weapon DEF: +#incPDD"
            if (itemOption.opString.charAt(i) == '#') {
                int j = i + 2;
                while ((j < itemOption.opString.length()) && itemOption.opString.substring(i + 1, j).matches("^(?!_)(?!.*?_$)[a-zA-Z0-9_\u4e00-\u9fa5]+$")) {
                    j++;
                }
                String curParam = itemOption.opString.substring(i, j);
                String curParamStripped;
                //get rid of any trailing percent signs on the parameter name
                if (j != itemOption.opString.length() || itemOption.opString.charAt(itemOption.opString.length() - 1) == '%') { //hacky
                    curParamStripped = curParam.substring(1, curParam.length() - 1);
                } else {
                    curParamStripped = curParam.substring(1);
                }
                String paramValue = Integer.toString(itemOption.get(curParamStripped));
                if (curParam.charAt(curParam.length() - 1) == '%') {
                    paramValue = paramValue.concat("%");
                }
                ret = ret.replace(curParam, paramValue);
            }
        }
        return ret;
    }

    /*
     * 通過ID獲取星岩信息
     */
    public StructItemOption getSocketInfo(int socketId) {
        int grade = ItemConstants.getNebuliteGrade(socketId);
        StructItemOption ret = socketData.get(grade).get(socketId);

        if (grade == -1 || ret == null) {
            return null;
        }
        return ret;
    }

    public Map<Integer, StructItemOption> getAllSocketInfo(int grade) {
        Map<Integer, StructItemOption> ret = new HashMap<>();
        try {
            ret = socketData.get(grade);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public Collection<Integer> getMonsterBookList() {
        return mobIds.values();
    }

    public Map<Integer, Integer> getMonsterBook() {
        return mobIds;
    }

    public Integer getItemIdByMob(int mobId) {
        return mobIds.get(mobId);
    }

    public Pair<Integer, Integer> getPot(int f) {
        return potLife.get(f);
    }

    public StructFamiliar getFamiliar(int f) {
        return familiars.get(f);
    }

    public Map<Integer, StructFamiliar> getFamiliars() {
        return familiars;
    }

    public Map<Integer, String> getAllItemNames() {
        return itemName;
    }

    public long getAllItemSize() {
        return itemDataCache.size();
    }

    public StructAndroid getAndroidInfo(int i) {
        return androidInfo.get(i);
    }

    protected MapleData getItemData(int itemId) {
        MapleData ret = null;
        String idStr = "0" + String.valueOf(itemId);
        MapleDataDirectoryEntry root = itemData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
                    ret = itemData.getData(topDir.getName() + "/" + iFile.getName());
                    if (ret == null) {
                        return null;
                    }
                    ret = ret.getChildByPath(idStr);
                    return ret;
                } else if (iFile.getName().equals(idStr.substring(1) + ".img")) {
                    ret = itemData.getData(topDir.getName() + "/" + iFile.getName());
                    return ret;
                }
            }
        }
        root = chrData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr + ".img")) {
                    ret = chrData.getData(topDir.getName() + "/" + iFile.getName());
                    return ret;
                }
            }
        }
        return ret;
    }

    public Point getItemLt(int itemId) { // 獲取物品的lt節點物品 0528.img
        MapleData item = getItemData(itemId);
        Point pData = (Point) item.getChildByPath("info/lt").getData();
        return pData;
    }

    public Point getItemRb(int itemId) { // 獲取物品的rb節點物品 0528.img
        MapleData item = getItemData(itemId);
        Point pData = (Point) item.getChildByPath("info/rb").getData();
        return pData;
    }

    @SuppressWarnings("unchecked")
    public <T> T getItemProperty(int itemId, String path, T defaultValue) {
        Map<?, ?> data = itemDataCache.get(itemId);
        if (data == null) {
            return defaultValue;
        }
        String[] loop = path.split("/");
        Object ret = null;
        for (String key : loop) {
            if (data.containsKey(key)) {
                if (data.get(key) instanceof Map<?, ?>) {
                    data = (Map<?, ?>) data.get(key);
                } else {
                    ret = data.get(key);
                    break;
                }
            } else {
                return defaultValue;
            }
        }

        if (ret == null) {
            ret = data;
        }
        if (ret != null && defaultValue != null && ret.getClass() != defaultValue.getClass()) {
            if (defaultValue instanceof Integer) {
                ret = Integer.valueOf(ret.toString());
            } else if (defaultValue instanceof Double) {
                ret = Double.valueOf(ret.toString());
            }
//            try {
//                ret = defaultValue.getClass().getMethod("valueOf", ret.getClass())
//                        .invoke(ret, ret.toString());
//            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                e.printStackTrace();
//            }
        }
        return (T) ret;
    }

    /**
     * Created by HERTZ on 2023/12/23
     *
     * @notice 每個單槽位的最大的道具數量
     */

    public short getSlotMax(int itemId) {
        if (ServerConfig.ITEM_MAXSLOT_MAP.containsKey(itemId) && ServerConfig.ITEM_MAXSLOT_MAP.get(itemId) > 0) {
            return ServerConfig.ITEM_MAXSLOT_MAP.get(itemId);
        }
        int ret = getItemProperty(itemId, "info/slotMax", ItemConstants.類型.裝備(itemId) ? 1 : 100);
        return (short) (ret == 1 && ItemConstants.類型.消耗(itemId) ? 100 : ret);
    }

    public int getFamiliarID(int itemId) {
        for (Entry<Integer, StructFamiliar> entry : familiars.entrySet()) {
            if (entry.getValue().getMonsterCardID() == itemId) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public int getPrice(int itemId) {
        if (getItemProperty(itemId, "info/autoPrice", -1) == 1 && getItemProperty(itemId, "info/price", 0) == 0) {
            return getItemProperty(itemId, "info/lv", -1) * 2;
        }
        return getItemProperty(itemId, "info/price", 0);
    }

    public double getUnitPrice(int itemId) {
        double unitPrice = getItemProperty(itemId, "info/unitPrice", -1.0);
        return unitPrice == 0.0 ? 1.0 : unitPrice;
    }

    protected int rand(int min, int max) {
        return Math.abs(Randomizer.rand(min, max));
    }

    public Equip levelUpEquip(Equip equip, Map<String, Integer> sta) {
        // Equip nEquip = (Equip) equip.copy();
        //is this all the stats?
        try {
            for (Entry<String, Integer> stat : sta.entrySet()) {
                switch (stat.getKey()) {
                    case "incSTRMin":
                        equip.setStr((short) (equip.getStr() + rand(stat.getValue(), sta.get("incSTRMax"))));
                        break;
                    case "incDEXMin":
                        equip.setDex((short) (equip.getDex() + rand(stat.getValue(), sta.get("incDEXMax"))));
                        break;
                    case "incINTMin":
                        equip.setInt((short) (equip.getInt() + rand(stat.getValue(), sta.get("incINTMax"))));
                        break;
                    case "incLUKMin":
                        equip.setLuk((short) (equip.getLuk() + rand(stat.getValue(), sta.get("incLUKMax"))));
                        break;
                    case "incPADMin":
                        equip.setPad((short) (equip.getPad() + rand(stat.getValue(), sta.get("incPADMax"))));
                        break;
                    case "incPDDMin":
                        equip.setPdd((short) (equip.getPdd() + rand(stat.getValue(), sta.get("incPDDMax"))));
                        break;
                    case "incMADMin":
                        equip.setMad((short) (equip.getMad() + rand(stat.getValue(), sta.get("incMADMax"))));
                        break;
                    case "incMDDMin":
                        equip.setMdd((short) (equip.getMdd() + rand(stat.getValue(), sta.get("incMDDMax"))));
                        break;
                    case "incACCMin":
                        equip.setAcc((short) (equip.getAcc() + rand(stat.getValue(), sta.get("incACCMax"))));
                        break;
                    case "incEVAMin":
                        equip.setAvoid((short) (equip.getAvoid() + rand(stat.getValue(), sta.get("incEVAMax"))));
                        break;
                    case "incSpeedMin":
                        equip.setSpeed((short) (equip.getSpeed() + rand(stat.getValue(), sta.get("incSpeedMax"))));
                        break;
                    case "incJumpMin":
                        equip.setJump((short) (equip.getJump() + rand(stat.getValue(), sta.get("incJumpMax"))));
                        break;
                    case "incMHPMin":
                        equip.setHp((short) (equip.getHp() + rand(stat.getValue(), sta.get("incMHPMax"))));
                        break;
                    case "incMMPMin":
                        equip.setMp((short) (equip.getMp() + rand(stat.getValue(), sta.get("incMMPMax"))));
                        break;
                    case "incMaxHPMin":
                        equip.setHp((short) (equip.getHp() + rand(stat.getValue(), sta.get("incMaxHPMax"))));
                        break;
                    case "incMaxMPMin":
                        equip.setMp((short) (equip.getMp() + rand(stat.getValue(), sta.get("incMaxMPMax"))));
                        break;
                }
            }
        } catch (NullPointerException e) {
            //catch npe because obviously the wz have some error XD
            e.printStackTrace();
        }
        return equip;
    }

    //    public ItemInformation getItemInformation(int itemId) {
//        return new ItemInformation();
//    }
    public <T> T getEquipAdditions(int itemId) {
        return getEquipAdditions(itemId, "");
    }

    public <T> T getEquipAdditions(int itemId, String path) {
        return getItemProperty(itemId, "info/addition/" + path, null);
    }

    public Map<String, Map<String, Integer>> getEquipIncrements(int itemId) {
        return getItemProperty(itemId, "info/level/info", null);
    }

    public List<Integer> getEquipSkills(int itemId) {
        Map<?, ?> data = getItemProperty(itemId, "info/level/case", null);
        if (data == null) {
            return null;
        }
        List<Integer> ret = new ArrayList<>();
        for (Entry<?, ?> entry : data.entrySet()) {
            if (entry.getKey().equals("Skill") && entry.getValue() instanceof Map<?, ?>) {
                for (Entry<?, ?> subentry : ((Map<?, ?>) entry.getValue()).entrySet()) {
                    if (subentry.getValue() instanceof Map<?, ?> && ((Map<?, ?>) subentry.getValue()).containsKey("id")) {
                        ret.add((Integer) ((Map<?, ?>) subentry.getValue()).get("id"));
                    }
                }
            }
        }
        return ret;
    }

    public int getEquipmentSkillsFixLevel(int itemId) {
        return getItemProperty(itemId, "info/level/fixLevel", 0);
    }

    public List<Pair<Integer, Integer>> getEquipmentSkills(int itemId) {
        Map<?, ?> data = getItemProperty(itemId, "info/level/case/0/1/EquipmentSkill", null);
        if (data != null) {
            List<Pair<Integer, Integer>> ret = new ArrayList<>();
            for (Entry<?, ?> entry : data.entrySet()) {
                Map map = (Map<?, ?>) entry.getValue();
                ret.add(new Pair<>((Integer) map.get("id"), (Integer) map.get("level")));
            }
            return ret;
        }
        return Collections.emptyList();
    }

    public List<Integer> getBonusExps(int itemId) {
        Map<?, ?> data = getItemProperty(itemId, "info/bonusExp", null);
        if (data != null) {
            List<Integer> ret = new ArrayList<>();
            for (Entry<?, ?> entry : data.entrySet()) {
                Map map = (Map<?, ?>) entry.getValue();
                ret.add((Integer) map.get("incExpR"));
            }
            return ret;
        }
        return Collections.emptyList();
    }

    /*
     * 檢測穿戴裝備的條件是否符合
     */
    public boolean canEquip(Equip eqp, int level, int job, int fame, int str, int dex, int luk, int int_, int supremacy) {
        boolean result = false;
        if (JobConstants.is管理員(job)) {
            return true;
        } else if ((level + supremacy) >= eqp.getTotalReqLevel() && str >= getReqStr(eqp.getItemId()) && dex >= getReqDex(eqp.getItemId()) && luk >= getReqLuk(eqp.getItemId()) && int_ >= getReqInt(eqp.getItemId())) {
            Integer fameReq = getReqPOP(eqp.getItemId());
            if (ServerConfig.WORLD_EQUIPCHECKFAME) {
                result = fameReq != null ? fame >= fameReq : fame >= 0;
            } else {
                result = fameReq == null || fame >= fameReq || fameReq == 0;
            }
        } else if ((level + supremacy) >= eqp.getTotalReqLevel() && JobConstants.is惡魔復仇者(job)) {
            result = true;
        }
        if (result) {
            result = canEquipByJob(eqp.getItemId(), job);
        }
        return result;
    }

    /*
     * 檢測穿戴裝備的職業是否符合
     */
    public boolean canEquipByJob(int itemid, int job) {
        int reqJob = getReqJob(itemid);
        boolean result = false;
        if (reqJob <= 0) {
            result = true;
        } else if (JobConstants.is劍士(job)) {
            result = (reqJob & 0x1) != 0;
        } else if (JobConstants.is法師(job)) {
            result = (reqJob & 0x2) != 0;
        } else if (JobConstants.is弓箭手(job)) {
            result = (reqJob & 0x4) != 0;
        } else if (JobConstants.is傑諾(job)) {
            result = ((reqJob & 0x8)) != 0 || ((reqJob & 0x10) != 0);
        } else if (JobConstants.is盜賊(job)) {
            result = (reqJob & 0x8) != 0;
        } else if (JobConstants.is海盜(job)) {
            result = (reqJob & 0x10) != 0;
        }
        if (result) {
            int reqSpecJob = getReqSpecJob(itemid);
            if (reqSpecJob > 0) {
                result = reqSpecJob == job / 100;
            } else if (MapleWeapon.調節器.check(itemid)) {
                result = JobConstants.is阿戴爾(job);
            } else if (MapleWeapon.閃亮克魯.check(itemid)) {
                result = JobConstants.is夜光(job);
            } else if (MapleWeapon.靈魂射手.check(itemid)) {
                result = JobConstants.is天使破壞者(job);
            } else if (MapleWeapon.魔劍.check(itemid)) {
                result = JobConstants.is惡魔復仇者(job);
            } else if (MapleWeapon.雙刀.check(itemid)) {
                result = JobConstants.is影武者(job);
            }
        }
        return result;
    }

    /**
     * 獲取裝備穿戴需要的等級
     */
    public int getReqLevel(int itemId) {
        return getItemProperty(itemId, "info/reqLevel", 0);
    }

    /**
     * 0x00 全職業通用 0x01 劍士 0x02 法師 0x04 弓手 0x08 盜賊 0x10 海盜 0x18 尖兵 也就是0x08+0x10
     */
    public int getReqJob(int itemId) {
        return getItemProperty(itemId, "info/reqJob", 0);
    }

    public int getReqSpecJob(int itemId) {
        int reqSpecJob = getItemProperty(itemId, "info/reqSpecJob", 0);
        return reqSpecJob == 0 ? getItemProperty(itemId, "info/reqJob2", 0) : reqSpecJob;
    }

    public int getReqStr(int itemId) {
        return getItemProperty(itemId, "info/reqSTR", 0);
    }

    public int getReqDex(int itemId) {
        return getItemProperty(itemId, "info/reqDEX", 0);
    }

    public int getReqInt(int itemId) {
        return getItemProperty(itemId, "info/reqINT", 0);
    }

    public int getReqLuk(int itemId) {
        return getItemProperty(itemId, "info/reqLUK", 0);
    }

    public Integer getReqPOP(int itemId) {
        return getItemProperty(itemId, "info/reqPOP", null);
    }

    public int getSlots(int itemId) {
        return getItemProperty(itemId, "info/tuc", 0);
    }

    public Integer getSetItemID(int itemId) {
        return getItemProperty(itemId, "info/setItemID", 0);
    }

    public boolean isEpicItem(int itemId) {
        return getItemProperty(itemId, "info/epicItem", 0) == 1;
    }

    public boolean isJokerToSetItem(int itemId) {
        return getItemProperty(itemId, "info/jokerToSetItem", 0) == 1;
    }

    public Map<Integer, StructSetItem> getSetItems() {
        return setItemInfo;
    }

    public StructSetItem getSetItem(int setItemId) {
        return setItemInfo.get(setItemId);
    }

    public Map<Integer, Integer> getScrollReqs(int itemId) {
        return getItemProperty(itemId, "req", null);
    }

    public int getScrollSuccess(int itemId) {
        return getScrollSuccess(itemId, 0);
    }

    public int getScrollSuccess(int itemId, int def) {
        return getItemProperty(itemId, "info/success", def);
    }

    public int getScrollCursed(int itemId) {
        return getItemProperty(itemId, "info/cursed", 0);
    }

    public Map<String, Integer> getItemBaseInfo(int itemId) {
        Map<?, ?> data = getItemProperty(itemId, "info", null);
        if (data == null) {
            return null;
        }

        Map<String, Integer> ret = new HashMap<>();
        for (Entry<?, ?> entry : data.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                ret.put(String.valueOf(entry.getKey()), (Integer) entry.getValue());
            }
        }
        return ret;
    }

    public Item scrollEquipWithId(Item equip, Item scroll, boolean whiteScroll, MapleCharacter chr, int vegas) {
        if (equip.getType() == 1) { //必須是裝備道具才可以升級屬性
            int scrollId = scroll.getItemId();
            if (ItemConstants.類型.裝備強化卷軸(scrollId)) { //裝備強化卷軸
                return scrollEnhance(equip, scroll, chr);
            } else if (ItemConstants.類型.潛能卷軸(scrollId)) { //潛能附加
                return scrollPotential(equip, scroll, chr);
            } else if (ItemConstants.類型.附加潛能卷軸(scrollId)) { //附加潛能
                return scrollPotentialAdd(equip, scroll, chr);
            } else if (ItemConstants.類型.回真卷軸(scrollId)) { //還原卷軸
                return scrollResetEquip(equip, scroll, chr);
            } else if (ItemConstants.類型.輪迴星火(scrollId)) {
                return scrollUpgradeItemEx(equip, scroll, chr, scrollId / 100 == 50645);
            }
            Equip nEquip = (Equip) equip;
            Map<String, Integer> data = getItemProperty(scrollId, "info", null);
            //成功幾率
            int succ = (ItemConstants.類型.提升卷(scrollId) && !ItemConstants.類型.武器攻擊力卷軸(scrollId) ? ItemConstants.卷軸.getSuccessTablet(scrollId, nEquip.getCurrentUpgradeCount()) : getScrollSuccess(scrollId));
            if (succ <= 0 && scrollId != 2040727 && scrollId != 2041058 && ItemConstants.類型.特殊卷軸(scrollId)) {
                succ = 100;
            }
            //失敗幾率
            int curse = (ItemConstants.類型.提升卷(scrollId) && !ItemConstants.類型.武器攻擊力卷軸(scrollId) ? ItemConstants.卷軸.getCurseTablet(scrollId, nEquip.getCurrentUpgradeCount()) : getScrollCursed(scrollId));
            //傾向系統提升幾率
            int craft = ItemConstants.類型.白衣卷軸(scrollId) ? 0 : chr.getTrait(MapleTraitType.craft).getLevel() / 10; //傾向系統的砸卷加成
            //幸運卷軸提升幾率
            int lucksKey = ItemAttribute.LuckyChance.check(equip.getAttribute()) ? 10 : 0; //裝備帶有幸運卷軸的砸卷加成
            boolean equipScrollSuccess = EnhanceResultType.SCROLL_SUCCESS.check(nEquip.getEnchantBuff());
            int success = equipScrollSuccess ? 100 : (succ + lucksKey + craft + getSuccessRates(scroll.getItemId()));
            success += vegas == 5610000 && success == 10 ? 20 : (vegas == 5610001 && success == 60 ? 30 : 0);
            if (chr.isAdmin()) {
                chr.dropSpouseMessage(UserChatMessageType.黑_黃, (ItemConstants.類型.特殊卷軸(scrollId) ? "特殊" : "普通") + "卷軸 - 默認幾率: " + succ + "% 傾向加成: " + craft + "% 幸運狀態加成: " + lucksKey + "% 最終概率: " + success + "% 失敗消失幾率: " + curse + "%");
            }
            if (ItemAttribute.LuckyChance.check(equip.getAttribute()) && !(ItemConstants.類型.特殊卷軸(scrollId) && scrollId != 2040727 && scrollId != 2041058)) {
                equip.removeAttribute(ItemAttribute.LuckyChance.getValue());
            }
            if (Randomizer.nextInt(100) <= success) {
                if (data != null) {
                    LotteryRandom lr = new LotteryRandom();
                    switch (scrollId) {
                        case 2612061:
                        case 2613050: {
                            data.put("PAD", Randomizer.rand(9, 12));
                            break;
                        }
                        case 2612062:
                        case 2613051: {
                            data.put("MAD", Randomizer.rand(9, 12));
                            break;
                        }
                        case 2048817:
                        case 2615031:
                        case 2616061: {
                            data.put("PAD", Randomizer.rand(5, 7));
                            break;
                        }
                        case 2046856: // 優質飾品攻擊力卷軸
                        case 2048804: { // 進階寵物裝備攻擊力卷軸
                            lr.addData(5, 15);
                            lr.addData(4, 85);
                            data.put("PAD", (int) lr.random());
                            break;
                        }
                        case 2048818:
                        case 2615032:
                        case 2616062: {
                            data.put("MAD", Randomizer.rand(5, 7));
                            break;
                        }
                        case 2046857: // 優質飾品魔力卷軸
                        case 2048805: { // 進階寵物裝備魔力卷軸
                            lr.addData(5, 15);
                            lr.addData(4, 85);
                            data.put("MAD", (int) lr.random());
                            break;
                        }
                        case 2046170:
                        case 2046171:
                        case 2046907:
                        case 2046909: {
                            data.put("PAD", Randomizer.rand(7, 10));
                            break;
                        }
                        case 2046908:
                        case 2046910: {
                            data.put("MAD", Randomizer.rand(7, 10));
                            break;
                        }
                        case 2612010:
                        case 2613000: {
                            data.put("PAD", Randomizer.rand(8, 11));
                            break;
                        }

                        case 2048856: { //救世寵物攻擊
                            data.put("PAD", Randomizer.rand(10, 15));
                            break;
                        }

                        case 2048857: { //救世寵物魔力
                            data.put("MAD", Randomizer.rand(10, 15));
                            break;
                        }

                        case 2613086:   //救世單手武器
                        case 2612099: { //救世雙手武器
                            data.put("PAD", Randomizer.rand(15, 20));
                            data.put("STR", Randomizer.rand(15, 20));
                            data.put("INT", Randomizer.rand(15, 20));
                            data.put("DEX", Randomizer.rand(15, 20));
                            data.put("LUK", Randomizer.rand(15, 20));
                            break;
                        }

                        case 2613087:   //救世單手魔力武器
                        case 2612100: { //救世雙手魔力武器
                            data.put("MAD", Randomizer.rand(15, 20));
                            data.put("STR", Randomizer.rand(15, 20));
                            data.put("INT", Randomizer.rand(15, 20));
                            data.put("DEX", Randomizer.rand(15, 20));
                            data.put("LUK", Randomizer.rand(15, 20));
                            break;
                        }

                        case 2616234: //防具攻擊
                        case 2615070: { //飾品攻擊
                            data.put("PAD", Randomizer.rand(10, 15));
                            break;
                        }

                        case 2616235: //防具魔力
                        case 2615071: { //飾品魔力
                            data.put("PAD", Randomizer.rand(10, 15));
                            break;
                        }

                        case 2613001: {
                            data.put("MAD", Randomizer.rand(8, 11));
                            break;
                        }


                        case 2048830: // 榮耀寵物裝備攻擊力卷軸
                        case 2615054: // 榮耀飾品攻擊力卷軸
                        case 2616218: { // 榮耀防具攻擊力卷軸
                            int[] props = {1, 2, 4, 6, 31, 28, 13, 7, 5, 2, 1};
                            for (int i = 0; i < props.length; i++) {
                                lr.addData(5 + i, props[i]);
                            }
                            data.put("PAD", (int) lr.random());
                            break;
                        }
                        case 2048831: // 榮耀寵物裝備魔力卷軸
                        case 2615055: // 榮耀飾品魔力卷軸
                        case 2616219: { // 榮耀防具魔力卷軸
                            int[] props = {1, 2, 4, 6, 31, 28, 13, 7, 5, 2, 1};
                            for (int i = 0; i < props.length; i++) {
                                lr.addData(5 + i, props[i]);
                            }
                            data.put("MAD", (int) lr.random());
                            break;
                        }
                        case 2613070: // 榮耀單手武器攻擊力卷軸
                        case 2612082: // 榮耀雙手武器攻擊力卷軸
                        case 2613071: // 榮耀單手武器魔力卷軸
                        case 2612083: { // 榮耀雙手武器魔力卷軸
                            int[] props = {1, 2, 4, 6, 31, 28, 13, 7, 5, 2, 1};
                            for (int i = 0; i < props.length; i++) {
                                lr.addData(10 + i, props[i]);
                            }
                            data.put("STR", (int) lr.random());
                            data.put("INT", (int) lr.random());
                            data.put("DEX", (int) lr.random());
                            data.put("LUK", (int) lr.random());
                            switch (scrollId) {
                                case 2613070:
                                case 2612082:
                                    data.put("PAD", (int) lr.random());
                                    break;
                                default:
                                    data.put("MAD", (int) lr.random());
                                    break;
                            }
                            break;
                        }
                        case 2613076: //命運單手武器攻擊力卷軸
                        case 2613077: //命運單手武器魔力卷軸
                        case 2612089: //命運雙手武器攻擊力卷軸
                        case 2612090: { //命運雙手武器魔力卷軸
                            int[] props = {4, 6, 31, 30, 14, 7, 5, 2, 1};
                            for (int i = 0; i < props.length; i++) {
                                lr.addData(12 + i, props[i]);
                            }
                            data.put("STR", (int) lr.random());
                            data.put("INT", (int) lr.random());
                            data.put("DEX", (int) lr.random());
                            data.put("LUK", (int) lr.random());
                            switch (scrollId) {
                                case 2613076:
                                case 2612089:
                                    data.put("PAD", (int) lr.random());
                                    break;
                                default:
                                    data.put("MAD", (int) lr.random());
                                    break;
                            }
                            break;
                        }
                        case 2048848: // 命運寵物攻擊PAD 09.17
                        case 2615060: // 命運飾品攻擊力卷軸
                        case 2616224: { // 命運防具攻擊力卷軸
                            int[] props = {4, 6, 31, 30, 14, 7, 5, 2, 1};
                            for (int i = 0; i < props.length; i++) {
                                lr.addData(7 + i, props[i]);
                            }
                            data.put("PAD", (int) lr.random());
                            break;
                        }
                        case 2048849: // 命運寵物攻擊PAD 09.17
                        case 2615061: // 命運飾品魔力卷軸
                        case 2616225: { // 命運防具魔力卷軸
                            int[] props = {4, 6, 31, 30, 14, 7, 5, 2, 1};
                            for (int i = 0; i < props.length; i++) {
                                lr.addData(7 + i, props[i]);
                            }
                            data.put("MAD", (int) lr.random());
                            break;
                        }
                    }
                }

                switch (scrollId) {
                    case 2049000: //白醫卷軸
                    case 2049001: //白醫卷軸
                    case 2049002: //白醫卷軸
                    case 2049003:
                    case 2049004: //白醫卷軸—仙
                    case 2049005: //白醫卷軸—神
                    case 2049024: //20%
                    case 2049025: { //100%
                        if (nEquip.getCurrentUpgradeCount() + nEquip.getRestUpgradeCount() < getSlots(nEquip.getItemId()) + nEquip.getTotalHammer()) {
                            nEquip.setRestUpgradeCount((byte) (nEquip.getRestUpgradeCount() + 1));
                        }
                        break;
                    }
                    case 2049006: //詛咒白醫卷軸
                    case 2049007: //詛咒白醫卷軸
                    case 2049008: { //詛咒白醫卷軸
                        if (nEquip.getCurrentUpgradeCount() + nEquip.getRestUpgradeCount() < getSlots(nEquip.getItemId()) + nEquip.getTotalHammer()) {
                            nEquip.setRestUpgradeCount((byte) (nEquip.getRestUpgradeCount() + 2));
                        }
                        break;
                    }
                    case 2040727: { //鞋子防滑卷軸 - 給鞋子增加防滑功能.成功率:10%, 對強化次數沒有影響
                        nEquip.addAttribute(ItemAttribute.NonSlip.getValue());
                        break;
                    }
                    case 2041058: { //披風防寒卷軸 - 給披肩增加防寒功能.成功率:10%, 對強化次數沒有影響
                        nEquip.addAttribute(ItemAttribute.ColdProof.getValue());
                        break;
                    }
                    case 2610200: { // 寵物裝備透明藥水
                        if (ItemAttribute.Hyalinize.check(nEquip.getAttribute())) {
                            nEquip.removeAttribute(ItemAttribute.Hyalinize.getValue());
                        } else {
                            nEquip.addAttribute(ItemAttribute.Hyalinize.getValue());
                        }
                        break;
                    }
                    default: {
                        if (ItemConstants.類型.混沌卷軸(scrollId)) {
                            int stat = ItemConstants.卷軸.getChaosNumber(scrollId);
                            int increase = ItemConstants.類型.樂觀混沌卷軸(scrollId) || isNegativeScroll(scrollId) ? 1 : Randomizer.nextBoolean() ? 1 : -1;
                            if (nEquip.getTotalStr() > 0) {
                                nEquip.setStr((short) (nEquip.getStr() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalDex() > 0) {
                                nEquip.setDex((short) (nEquip.getDex() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalInt() > 0) {
                                nEquip.setInt((short) (nEquip.getInt() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalLuk() > 0) {
                                nEquip.setLuk((short) (nEquip.getLuk() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalPad() > 0) {
                                nEquip.setPad((short) (nEquip.getPad() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalPdd() > 0) {
                                nEquip.setPdd((short) (nEquip.getPdd() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalMad() > 0) {
                                nEquip.setMad((short) (nEquip.getMad() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalMdd() > 0) {
                                nEquip.setMdd((short) (nEquip.getMdd() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalAcc() > 0) {
                                nEquip.setAcc((short) (nEquip.getAcc() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalAvoid() > 0) {
                                nEquip.setAvoid((short) (nEquip.getAvoid() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalSpeed() > 0) {
                                nEquip.setSpeed((short) (nEquip.getSpeed() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalJump() > 0) {
                                nEquip.setJump((short) (nEquip.getJump() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalHp() > 0) {
                                nEquip.setHp((short) (nEquip.getHp() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            if (nEquip.getTotalMp() > 0) {
                                nEquip.setMp((short) (nEquip.getMp() + (Randomizer.nextInt(stat) + 1) * increase));
                            }
                            break;
                        } else if (ItemConstants.類型.幸運日卷軸(scroll.getItemId()) || ItemConstants.類型.保護卷軸(scroll.getItemId())) {
                            if (ItemConstants.類型.幸運日卷軸(scroll.getItemId())) {// 幸運日
                                nEquip.addAttribute(ItemAttribute.LuckyChance.getValue());
                            }
                            if (ItemConstants.類型.保護卷軸(scroll.getItemId())) {// 保護捲軸
                                nEquip.addAttribute(ItemAttribute.NonCurse.getValue());
                            }
                        } else if (ItemConstants.類型.安全卷軸(scroll.getItemId())) {// 安全捲軸
                            nEquip.addAttribute(ItemAttribute.ProtectRUC.getValue());
                        } else if (ItemConstants.類型.卷軸保護卡(scroll.getItemId())) {// 捲軸保護卡
                            nEquip.addAttribute(ItemAttribute.ProtectScroll.getValue());
                        } else if (ItemConstants.類型.恢復卡(scroll.getItemId())) {// 恢復卡
                            nEquip.addAttribute(ItemAttribute.RegressScroll.getValue());
                        } else if (ItemConstants.類型.白衣卷軸(scrollId)) {
                            int recover = getRecover(scrollId);
                            int slots = getSlots(nEquip.getItemId());
                            if (nEquip.getCurrentUpgradeCount() + nEquip.getRestUpgradeCount() < slots + nEquip.getTotalHammer()) {
                                nEquip.setRestUpgradeCount((byte) (nEquip.getRestUpgradeCount() + recover));
                                break;
                            }
                            if (chr.isAdmin()) {
                                chr.dropMessage(-9, "砸卷錯誤：不存在卷軸升級次數 " + (recover == 0) + " 超過可恢復次數上限 " + (nEquip.getCurrentUpgradeCount() + nEquip.getRestUpgradeCount() >= slots + nEquip.getTotalHammer()));
                                break;
                            }
                            break;
                        } else {
                            for (Entry<?, ?> entry : data.entrySet()) {
                                if (!(entry.getValue() instanceof Integer)) {
                                    continue;
                                }

                                String key = ((String) entry.getKey()).toUpperCase();
                                key = key.startsWith("INC") ? key.substring(3) : key;
                                Integer value = (Integer) entry.getValue();
                                switch (key) {
                                    case "STR":
                                        nEquip.setStr((short) (nEquip.getStr() + value));
                                        break;
                                    case "DEX":
                                        nEquip.setDex((short) (nEquip.getDex() + value));
                                        break;
                                    case "INT":
                                        nEquip.setInt((short) (nEquip.getInt() + value));
                                        break;
                                    case "LUK":
                                        nEquip.setLuk((short) (nEquip.getLuk() + value));
                                        break;
                                    case "PAD":
                                        nEquip.setPad((short) (nEquip.getPad() + value));
                                        break;
                                    case "PDD":
                                        nEquip.setPdd((short) (nEquip.getPdd() + value));
                                        break;
                                    case "MAD":
                                        nEquip.setMad((short) (nEquip.getMad() + value));
                                        break;
                                    case "MDD":
                                        nEquip.setMdd((short) (nEquip.getMdd() + value));
                                        break;
                                    case "ACC":
                                        nEquip.setAcc((short) (nEquip.getAcc() + value));
                                        break;
                                    case "EVA":
                                        nEquip.setAvoid((short) (nEquip.getAvoid() + value));
                                        break;
                                    case "SPEED":
                                        nEquip.setSpeed((short) (nEquip.getSpeed() + value));
                                        break;
                                    case "JUMP":
                                        nEquip.setJump((short) (nEquip.getJump() + value));
                                        break;
                                    case "MHP":
                                        nEquip.setHp((short) (nEquip.getHp() + value));
                                        break;
                                    case "MMP":
                                        nEquip.setMp((short) (nEquip.getMp() + value));
                                        break;
                                }
                            }
                            break;
                        }
                    }
                }
                //砸捲成功後的處理
                if (!ItemConstants.類型.白衣卷軸(scrollId) && !ItemConstants.類型.特殊卷軸(scrollId)) {
                    if (ItemAttribute.ProtectRUC.check(nEquip.getAttribute())) {
                        nEquip.removeAttribute(ItemAttribute.ProtectRUC.getValue());
                    }
                    int scrollUseSlots = ItemConstants.類型.阿斯旺卷軸(scrollId) ? getSlots(scrollId) : 1;
                    nEquip.setRestUpgradeCount((byte) (nEquip.getRestUpgradeCount() - scrollUseSlots));
                    nEquip.setCurrentUpgradeCount((byte) (nEquip.getCurrentUpgradeCount() + scrollUseSlots));
                }
            } else {
                //砸卷失敗後的處理
                if (!whiteScroll && !ItemConstants.類型.白衣卷軸(scrollId) && !ItemConstants.類型.特殊卷軸(scrollId)) {
                    if (ItemAttribute.ProtectRUC.check(nEquip.getAttribute())) {
                        nEquip.removeAttribute(ItemAttribute.ProtectRUC.getValue());
                        chr.dropSpouseMessage(UserChatMessageType.黑_黃, "由於卷軸的效果，升級次數沒有減少。");
                    } else if (!MapleItemInformationProvider.getInstance().hasSafetyShield(scrollId)) {
                        int scrollUseSlots = ItemConstants.類型.阿斯旺卷軸(scrollId) ? getSlots(scrollId) : 1;
                        nEquip.setRestUpgradeCount((byte) (nEquip.getRestUpgradeCount() - scrollUseSlots));
                    }
                }
                if (Randomizer.nextInt(99) + 1 < curse) {
                    return null;
                }
            }
        }
        return equip;
    }

    /*
     * 裝備強化卷軸
     */
    public Item scrollEnhance(Item equip, Item scroll, MapleCharacter chr) {
        if (!ItemConstants.類型.裝備強化卷軸(scroll.getItemId())) {
            return equip;
        }
        if (equip.getType() != 1) {
            return equip;
        }
        Equip nEquip = (Equip) equip;
        int maxUpgrade = getItemProperty(scroll.getItemId(), "info/forceUpgradeWz2/maxUpgrade", 0);
        if (nEquip.getStarForceLevel() >= ItemConstants.卷軸.getMaxEnhance(nEquip.getItemId()) || (maxUpgrade != 0 && nEquip.getStarForceLevel() >= maxUpgrade)) {
            return equip;
        }
        int scrollId = scroll.getItemId();
        boolean noCursed = isNoCursedScroll(scrollId);
        int scrollForceUpgrade = getForceUpgrade(scrollId);
        int incForce = getItemProperty(scrollId, "info/forceUpgradeWz2/incForce", 0);
        int succ = getScrollSuccess(scrollId); //成功幾率
        int curse = noCursed ? 0 : 100; //失敗幾率 沒有就代表100%消失
        int craft = chr.getTrait(MapleTraitType.craft).getLevel() / 10; //傾向系統的砸卷加成
        if (scrollForceUpgrade == 0 && succ == 0) {
            boolean simple = scroll.getItemId() == 2049301 || scroll.getItemId() == 2049307;
            int start = simple ? 80 : 100;
            int second = simple ? 7 : 9;
            int level = nEquip.getStarForceLevel();
            if (level <= second) {
                succ = start - 10 * level;
            } else {
                succ = 5 / (level - second);
            }
        }
        int success = succ + succ * craft / 100;
        if (chr.isAdmin()) {
            chr.dropSpouseMessage(UserChatMessageType.黑_黃, (scrollForceUpgrade > 0 ? "星力" + scrollForceUpgrade + "星強化卷" : incForce > 0 ? "追加" + incForce + "星強化券" : "裝備強化卷軸") + " - 默認幾率: " + succ + "% 傾向加成: " + craft + "% 最終幾率: " + success + "% 失敗消失幾率: " + curse + "% 卷軸是否失敗不消失裝備: " + noCursed);
        }
        if (!Randomizer.isSuccess(success)) {
            return Randomizer.isSuccess(curse) ? null : nEquip;
        }
        if (scrollForceUpgrade > 0) {
            nEquip.setStarForceLevel((byte) scrollForceUpgrade);
        } else {
            nEquip.setStarForceLevel((byte) (nEquip.getStarForceLevel() + (incForce > 0 ? incForce : 1)));
        }
        return nEquip;
    }

    /*
     * 潛能附加卷軸
     */
    public Item scrollPotential(Item equip, Item scroll, MapleCharacter chr) {
        if (equip.getType() != 1) {
            return equip;
        }
        final Equip nEquip = (Equip) equip;
        int scrollId = scroll.getItemId();
        int success = getScrollSuccess(scrollId, 0);

        String scrollType;
        if (ItemConstants.類型.潛能卷軸(scroll.getItemId())) {
            switch (scroll.getItemId() / 100) {
                case 20494:
                    switch (scroll.getItemId()) {// 專用捲軸失敗不爆物判斷
                        case 2049404:// [6週年]潛在能力賦予卷軸
                        case 2049405:// 真. 楓葉之心項鍊專用潛在能力卷軸
                        case 2049414:// 紫色靈魂戒指專用潛在能力賦予卷軸
                        case 2049415:// 藍色靈魂戒指專用潛在能力賦予卷軸
                        case 2049421:// 心之項鍊潛在能力賦予卷軸
                        case 2049423:// 恰吉面具潛能賦予卷軸
                        case 2049424:// 賦予卡爾頓的鬍子潛在能力卷軸
                        case 2049426:// 10週年武器專用潛在能力附加卷軸
                        case 2049427:// 賞金獵人
                            scrollType = "專用";
                            break;
                        default:
                            if (scroll.getItemId() >= 2049427 && scroll.getItemId() <= 2049446) {// 賞金獵人
                                scrollType = "專用";
                            } else {
                                scrollType = "普通";
                            }
                    }
                    break;
                case 20497:
                    int nId = (scroll.getItemId() % 100) / 10;
                    switch (nId) {
                        case 0:
                        case 1:
                        case 3:
                            scrollType = "稀有";
                            break;
                        case 4:
                        case 5:
                        case 6:
                        case 9:
                            scrollType = "罕見";
                            break;
                        case 8:
                            scrollType = "傳說";
                            break;
                        default:
                            scrollType = "未知";
                            break;
                    }
                    break;
                default:
                    scrollType = "未知";
                    break;
            }
        } else {
            return nEquip;
        }

        if (success == 0) {
            switch (scroll.getItemId()) {// 普通卷轴成功率判断
                case 2049402:// 超級潛在能力賦予卷軸
                case 2049404:// [6週年]潛在能力賦予卷軸
                case 2049405:// 真. 楓葉之心項鍊專用潛在能力卷軸
                case 2049406:// 特殊賦予潛在能力卷軸
                case 2049414:// 紫色靈魂戒指專用潛在能力賦予卷軸
                case 2049415:// 藍色靈魂戒指專用潛在能力賦予卷軸
                case 2049417:// 特殊潛在能力賦予 卷軸
                case 2049419:// 特別潛在能力賦予捲軸
                case 2049423:// 恰吉面具潛能賦予卷軸
                    success = 100;
                    break;
                case 2049400:// 高級潛在能力賦予卷軸
                case 2049407:// 高級潛在能力賦予卷軸
                case 2049412:// 高級潛在能力賦予卷軸
                    success = 90;
                    break;
                case 2049421:// 心之項鍊潛在能力賦予卷軸
                case 2049424:// 賦予卡爾頓的鬍子潛在能力卷軸
                    success = 80;
                    break;
                case 2049401:// 潛在能力賦予卷軸
                case 2049408:// 潛在能力賦予卷軸
                case 2049416:// 潛在能力賦予卷軸
                    success = 70;
                    break;
                default:
                    if (scroll.getItemId() >= 2049427 && scroll.getItemId() <= 2049446) {// 賞金獵人
                        success = 100;
                    }
            }
        }

        if (chr.isAdmin()) {
            chr.dropSpouseMessage(UserChatMessageType.黑_黃, scrollType + "潛能附加卷軸 - 成功機率  =  " + success + "%");
        }
        if (success <= 0) {
            chr.dropMessage(1, "卷軸道具: " + scroll.getItemId() + " - " + this.getName(scroll.getItemId()) + " 成功幾率為: " + success + " 該卷軸可能還未修復。");
            chr.sendEnableActions();
            return nEquip;
        }
        if (!("稀有".equalsIgnoreCase(scrollType) || "罕見".equalsIgnoreCase(scrollType) || "傳說".equalsIgnoreCase(scrollType)) && nEquip.getState(false) != 0) {
            return nEquip;
        }
        if (Randomizer.nextInt(100) >= success) {
            return nEquip;
        }
        if ("稀有".equalsIgnoreCase(scrollType)) {// 稀有潛能捲軸
            nEquip.renewPotential(2, false);
        } else if ("罕見".equalsIgnoreCase(scrollType)) {// 罕見潛能捲軸
            nEquip.renewPotential(3, false);
        } else if ("傳說".equalsIgnoreCase(scrollType)) {// 傳說潛能捲軸
            nEquip.renewPotential(4, false);
        } else if (scrollId == 2049419) {// 附加3條潛能
            nEquip.renewPotential(true, false);
        } else {
            nEquip.renewPotential(false);
        }
        return nEquip;
    }

    /**
     * 附加潛能卷軸
     */
    public Item scrollPotentialAdd(Item equip, Item scroll, MapleCharacter chr) {
        if (equip.getType() != 1) { //檢測必須需要砸卷的道具為裝備
            return equip;
        }
        Equip nEquip = (Equip) equip;
        int scrollId = scroll.getItemId();
        int success = getScrollSuccess(scrollId, 0);
        if (!ItemConstants.類型.附加潛能卷軸(scroll.getItemId())) {
            chr.sendEnableActions();
            return nEquip;
        }
        if (success <= 0) {
            chr.dropMessage(1, "卷軸道具: " + scrollId + " - " + this.getName(scrollId) + " 成功幾率為: " + success + " 該卷軸可能還未修復。");
            chr.sendEnableActions();
            return nEquip;
        }
        if (chr.isAdmin()) {
            chr.dropSpouseMessage(UserChatMessageType.黑_黃, "附加潛能附加卷軸 - 成功機率  =  " + success + "%");
        }
        if (Randomizer.isSuccess(success)) {
            if (scrollId == 2048306) {
                nEquip.renewPotential(true, true);
            } else if (scrollId / 10 == 204973) { // 稀有
                nEquip.renewPotential(2, true);
            } else {
                nEquip.renewPotential(true);
            }
        }
        return nEquip;
    }

    /*
     * 還原卷軸
     */
    public Item scrollResetEquip(Item equip, Item scroll, MapleCharacter chr) {
        if (equip.getType() != 1) { //檢測必須需要砸卷的道具為裝備
            return equip;
        }
        Equip nEquip = (Equip) equip;
        int scrollId = scroll.getItemId();
        int succe = scrollId == 5064200 ? 100 : getScrollSuccess(scrollId); //成功幾率
        int curse = getScrollCursed(scrollId); //失敗幾率
        int craft = chr.getTrait(MapleTraitType.craft).getLevel() / 10; //傾向系統的砸卷加
        int lucksKey = ItemAttribute.LuckyChance.check(equip.getAttribute()) ? 10 : 0; //裝備帶有幸運卷軸的砸卷加成
        if (ItemAttribute.LuckyChance.check(equip.getAttribute())) {
            equip.removeAttribute(ItemAttribute.LuckyChance.getValue());
        }
        int success = succe + craft + lucksKey;
        boolean perfectReset = scrollId == 5064200 || getItemProperty(scrollId, "info/perfectReset", 0) == 1;
        if (chr.isAdmin()) {
            chr.dropSpouseMessage(UserChatMessageType.黑_黃, "還原卷軸 - 默認幾率: " + succe + "% 傾向加成: " + craft + "% 幸運卷軸狀態加成: " + lucksKey + "% 最終幾率: " + success + "% 失敗消失幾率: " + curse + "%");
        }
        if (Randomizer.nextInt(100) < success) {
            return resetEquipStats(nEquip, (byte) (perfectReset ? 0 : -1));
        } else if (Randomizer.nextInt(100) < curse) {
            return null;
        }
        return nEquip;
    }

    public Item scrollUpgradeItemEx(Item equip, Item scroll, MapleCharacter chr, boolean cs) {
        if (equip.getType() != 1) {
            return equip;
        }
        Equip nEquip = (Equip) equip;
        long oldFlag = nEquip.getFlameFlag();
        int scrollId = scroll.getItemId();
        long flag = NirvanaFlame.randomStateFromJson(nEquip, scrollId);
        if (flag > -1) {
            if(ItemConstants.類型.暗黑輪迴星火(scrollId)){
                chr.setKeyValue("nirvana_flame_state_old", String.valueOf(oldFlag));
                chr.setKeyValue("nirvana_flame_state_equip_position", String.valueOf(nEquip.getPosition()));
                chr.setKeyValue("nirvana_flame_state_new", String.valueOf(flag));
                chr.setKeyValue("nirvana_flame_state_itemId", String.valueOf(scrollId));
            }else{
                nEquip.setFlameFlag(flag);
            }
        }

        return nEquip;
    }

    public Item scrollSealedEquip(Item equip, Item scroll, MapleCharacter chr) {
        if (equip.getType() != 1) { //檢測必須需要砸卷的道具為裝備
            return equip;
        }
        Equip nEquip = (Equip) equip;
        if (!nEquip.isSealedEquip()) {
            chr.dropSpouseMessage(UserChatMessageType.黑_黃, "該裝備不是漩渦裝備，無法解除封印。");
            return equip;
        }
//        boolean is100 = scroll.getItemId() == 2610002;
//        return updateSealedEquip(nEquip, getSealedEquipInfo(equip.getItemId(), nEquip.getSealedLevel()), is100 || Randomizer.nextBoolean());
        return updateSealedEquip(nEquip, getSealedEquipInfo(equip.getItemId(), nEquip.getSealedLevel()), true);
    }

    public Equip updateSealedEquip(Equip equip, Map<String, Integer> sealedInfo, boolean up) {
        if (sealedInfo == null) {
            return equip;
        }
        int n = up ? 1 : -1;
        for (Entry<String, Integer> info : sealedInfo.entrySet()) {
            if (info.getKey().endsWith("STR")) {
                equip.setStr((short) (equip.getStr() + info.getValue() * n));
            } else if (info.getKey().endsWith("DEX")) {
                equip.setDex((short) (equip.getDex() + info.getValue() * n));
            } else if (info.getKey().endsWith("INT")) {
                equip.setInt((short) (equip.getInt() + info.getValue() * n));
            } else if (info.getKey().endsWith("LUK")) {
                equip.setLuk((short) (equip.getLuk() + info.getValue() * n));
            } else if (info.getKey().endsWith("PDD")) {
                equip.setPdd((short) (equip.getPdd() + info.getValue() * n));
            } else if (info.getKey().endsWith("MDD")) {
                equip.setMdd((short) (equip.getMdd() + info.getValue() * n));
            } else if (info.getKey().endsWith("MHP")) {
                equip.setHp((short) (equip.getHp() + info.getValue() * n));
            } else if (info.getKey().endsWith("MMP")) {
                equip.setMp((short) (equip.getMp() + info.getValue() * n));
            } else if (info.getKey().endsWith("PAD")) {
                equip.setPad((short) (equip.getPad() + info.getValue() * n));
            } else if (info.getKey().endsWith("MAD")) {
                equip.setMad((short) (equip.getMad() + info.getValue() * n));
            } else if (info.getKey().endsWith("ACC")) {
                equip.setAcc((short) (equip.getAcc() + info.getValue() * n));
            } else if (info.getKey().endsWith("EVA")) {
                equip.setAvoid((short) (equip.getAvoid() + info.getValue() * n));
            } else if (info.getKey().endsWith("IMDR")) {
                equip.setIgnorePDR((short) (equip.getIgnorePDR() + info.getValue() * n));
            } else if (info.getKey().endsWith("BDR") || info.getKey().endsWith("bdR")) {
                equip.setBossDamage((short) (equip.getBossDamage() + info.getValue() * n));
            }
        }
        equip.setSealedLevel((byte) (equip.getSealedLevel() + 1));
        equip.setSealedExp(0);
        return equip;
    }

    /*
     * 對裝備屬性進行還原 潛能 星岩屬性 道具外形保存不變
     */
    public Equip resetEquipStats(Equip oldEquip, byte resetType) {
        Equip newEquip = getEquipById(oldEquip.getItemId());
        newEquip.setPlatinumHammer(oldEquip.getPlatinumHammer());
        newEquip.setRestUpgradeCount((byte) (newEquip.getRestUpgradeCount() + oldEquip.getPlatinumHammer()));
        newEquip.setCharmEXP(oldEquip.getCharmEXP());
        newEquip.setEnchantBuff(oldEquip.getEnchantBuff());
        //設置道具的潛能 星岩 道具外形的信息
        newEquip.setState(oldEquip.getState(false), false); //設置新道具的潛能等級
        newEquip.setState(oldEquip.getState(true), true); //設置新道具的潛能等級
        newEquip.setPotential1(oldEquip.getPotential1()); //設置新道具的潛能屬性 1
        newEquip.setPotential2(oldEquip.getPotential2()); //設置新道具的潛能屬性 2
        newEquip.setPotential3(oldEquip.getPotential3()); //設置新道具的潛能屬性 3
        newEquip.setPotential4(oldEquip.getPotential4()); //設置新道具的潛能屬性 4
        newEquip.setPotential5(oldEquip.getPotential5()); //設置新道具的潛能屬性 5
        newEquip.setPotential6(oldEquip.getPotential6()); //設置新道具的潛能屬性 6
        newEquip.setSocket1(oldEquip.getSocket1()); //設置新道具的星岩屬性 1
        newEquip.setSocket2(oldEquip.getSocket2()); //設置新道具的星岩屬性 2
        newEquip.setSocket3(oldEquip.getSocket3()); //設置新道具的星岩屬性 3
        newEquip.setItemSkin(oldEquip.getItemSkin()); //設置新道具的外形狀態
        newEquip.setSoulOptionID(oldEquip.getSoulOptionID());
        newEquip.setSoulSocketID(oldEquip.getSoulSocketID());
        newEquip.setSoulOption(oldEquip.getSoulOption());
        newEquip.setSoulSkill(oldEquip.getSoulSkill());
        //設置一些道具原始的日誌狀態信息
        newEquip.setPosition(oldEquip.getPosition());
        newEquip.setQuantity(oldEquip.getQuantity());
        newEquip.setAttribute(oldEquip.getAttribute());
        newEquip.setOwner(oldEquip.getOwner());
        newEquip.setGMLog(oldEquip.getGMLog());
        newEquip.setExpiration(oldEquip.getTrueExpiration());
        newEquip.setSN(oldEquip.getSN());
        newEquip.setKarmaCount(oldEquip.getKarmaCount());

        switch (resetType) {
            case 0: // 完美回真
                // 去除無法交易狀態
                if (ItemAttribute.TradeBlock.check(newEquip.getAttribute())) {
                    newEquip.removeAttribute(ItemAttribute.TradeBlock.getValue());
                }
                // 去除楓方塊狀態，否則回真後無法交易裝備又不能剪刀，需要穿一次裝才能剪刀(這個是台版BUG)
                if (ItemAttribute.AnimaCube.check(newEquip.getAttribute())) {
                    newEquip.removeAttribute(ItemAttribute.AnimaCube.getValue());
                }
                break;
            case 1: // 亞克回真
                newEquip.setStarForce(new StarForce(oldEquip.getStarForce()));
            default:
                newEquip.setNirvanaFlame(new NirvanaFlame(oldEquip.getNirvanaFlame()));
                break;
        }
        return newEquip;
    }

    public Equip getEquipById(int equipId) {
        return getEquipById(equipId, -1);
    }

    public Equip getEquipById(int equipId, int ringId) {
        //if (!ItemConstants.類型.裝備(equipId)) {
        //    throw new IllegalArgumentException("非裝備ID: " + equipId);
        //}
        if (isCash(equipId) && ringId <= -1) {
            ringId = MapleInventoryIdentifier.getInstance();
        }
        Map<?, ?> data = getItemProperty(equipId, "info", null);
        Equip ret = new Equip(equipId, (short) 0, ringId, 0, (short) 0);
        if (data == null) {
            return ret;
        }

        short stats = ItemConstants.getStat(equipId, 0);
        if (stats > 0) {
            ret.setStr(stats);
            ret.setDex(stats);
            ret.setInt(stats);
            ret.setLuk(stats);
        }
        stats = ItemConstants.getATK(equipId, 0);
        if (stats > 0) {
            ret.setPad(stats);
            ret.setMad(stats);
        }
        stats = ItemConstants.getHpMp(equipId, 0);
        if (stats > 0) {
            ret.setHp(stats);
            ret.setMp(stats);
        }
        stats = ItemConstants.getDEF(equipId, 0);
        if (stats > 0) {
            ret.setPdd(stats);
            ret.setMdd(stats);
        }

        for (Entry<?, ?> entry : data.entrySet()) {
            if (!StringUtil.isNumber(entry.getValue().toString())) {
                continue;
            }

            String key = ((String) entry.getKey()).toUpperCase();
            key = key.startsWith("INC") ? key.substring(3) : key;

            Number value;
            try {
                value = Integer.valueOf(entry.getValue().toString());
            } catch (Exception e) {
                value = Double.parseDouble(entry.getValue().toString());
            }
            switch (key) {
                case "STR":
                    ret.setStr(value.shortValue());
                    break;
                case "DEX":
                    ret.setDex(value.shortValue());
                    break;
                case "INT":
                    ret.setInt(value.shortValue());
                    break;
                case "LUK":
                    ret.setLuk(value.shortValue());
                    break;
                case "PAD":
                    ret.setPad(value.shortValue());
                    break;
                case "PDD":
                    ret.setPdd(value.shortValue());
                    break;
                case "MAD":
                    ret.setMad(value.shortValue());
                    break;
                case "MDD":
                    ret.setMdd(value.shortValue());
                    break;
                case "ACC":
                    ret.setAcc(value.shortValue());
                    break;
                case "EVA":
                    ret.setAvoid(value.shortValue());
                    break;
                case "SPEED":
                    ret.setSpeed(value.shortValue());
                    break;
                case "JUMP":
                    ret.setJump(value.shortValue());
                    break;
                case "MHP":
                    ret.setHp(value.shortValue());
                    break;
                case "MMP":
                    ret.setMp(value.shortValue());
                    break;
                case "TUC":
                    ret.setRestUpgradeCount(value.byteValue());
                    break;
                case "CRAFT":
                    ret.setHands(value.shortValue());
                    break;
                case "DURABILITY":
                    ret.setDurability(value.intValue());
                    break;
                case "CHARMEXP":
                    ret.setCharmEXP(value.shortValue());
                    break;
                case "PVPDAMAGE":
                    ret.setPVPDamage(value.shortValue());
                    break;
                case "BDR":
                    ret.setBossDamage(value.shortValue());
                    break;
                case "IMDR":
                    ret.setIgnorePDR(value.shortValue());
                    break;
                case "DAMR":
                    ret.setTotalDamage(value.shortValue());
                    break;
                case "ARC":
                    ret.setARC(value.shortValue());
                    break;
                case "reqLevel":
                    ret.setReqLevel(value.shortValue());
                    break;
            }
        }

        Object cash = data.get("cash");
        if (cash != null && Boolean.valueOf(cash.toString()) && ret.getCharmEXP() <= 0) {
            short exp = 0;
            int identifier = equipId / 10000;
            if (ItemConstants.類型.武器(equipId) || identifier == 106) { //weapon overall
                exp = 60;
            } else if (identifier == 100) { //hats
                exp = 50;
            } else if (ItemConstants.類型.飾品(equipId) || identifier == 102 || identifier == 108 || identifier == 107) { //gloves shoes accessory
                exp = 40;
            } else if (identifier == 104 || identifier == 105 || identifier == 110) { //top bottom cape
                exp = 30;
            }
            ret.setCharmEXP(exp);
        }

        ret.setSN(ringId);
        return ret;
    }

    protected short getRandStatFusion(short defaultValue, int value1, int value2) {
        if (defaultValue == 0) {
            return 0;
        }
        int range = ((value1 + value2) / 2) - defaultValue;
        int rand = Randomizer.nextInt(Math.abs(range) + 1);
        return (short) (defaultValue + (range < 0 ? -rand : rand));
    }

    protected short getRandStat(short defaultValue, int maxRange) {
        if (defaultValue == 0) {
            return 0;
        }
        // vary no more than ceil of 10% of stat
        int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1), maxRange);
        return (short) ((defaultValue - lMaxRange) + Randomizer.nextInt(lMaxRange * 2 + 1));
    }

    protected short getRandStatAbove(short defaultValue, int maxRange) {
        if (defaultValue <= 0) {
            return 0;
        }
        int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1), maxRange);
        return (short) ((defaultValue) + Randomizer.nextInt(lMaxRange + 1));
    }

    public Equip randomizeStats(Equip equip) {
        equip.setStr(getRandStat(equip.getStr(), 5));
        equip.setDex(getRandStat(equip.getDex(), 5));
        equip.setInt(getRandStat(equip.getInt(), 5));
        equip.setLuk(getRandStat(equip.getLuk(), 5));
        equip.setMad(getRandStat(equip.getMad(), 5));
        equip.setPad(getRandStat(equip.getPad(), 5));
        equip.setAcc(getRandStat(equip.getAcc(), 5));
        equip.setAvoid(getRandStat(equip.getAvoid(), 5));
        equip.setJump(getRandStat(equip.getJump(), 5));
        equip.setHands(getRandStat(equip.getHands(), 5));
        equip.setSpeed(getRandStat(equip.getSpeed(), 5));
        equip.setPdd(getRandStat(equip.getPdd(), 10));
        equip.setMdd(getRandStat(equip.getMdd(), 10));
        equip.setHp(getRandStat(equip.getHp(), 10));
        if (!ItemConstants.isDemonShield(equip.getItemId())) {
            equip.setMp(getRandStat(equip.getMp(), 10));
        }
        equip.setSealedLevel((byte) (equip.isSealedEquip() ? 1 : 0));
        equip.setBossDamage((short) getBossDamageRate(equip.getItemId()));
        equip.setIgnorePDR((short) getIgnoreMobDmageRate(equip.getItemId()));
        equip.setTotalDamage((short) getTotalDamage(equip.getItemId()));
        equip.setPotential1(getOption(equip.getItemId(), 1));
        equip.setPotential2(getOption(equip.getItemId(), 2));
        equip.setPotential3(getOption(equip.getItemId(), 3));
        return equip;
    }

    public Equip randomizeStats_Above(Equip equip) {
        equip.setStr(getRandStatAbove(equip.getStr(), 5));
        equip.setDex(getRandStatAbove(equip.getDex(), 5));
        equip.setInt(getRandStatAbove(equip.getInt(), 5));
        equip.setLuk(getRandStatAbove(equip.getLuk(), 5));
        equip.setMad(getRandStatAbove(equip.getMad(), 5));
        equip.setPad(getRandStatAbove(equip.getPad(), 5));
        equip.setAcc(getRandStatAbove(equip.getAcc(), 5));
        equip.setAvoid(getRandStatAbove(equip.getAvoid(), 5));
        equip.setJump(getRandStatAbove(equip.getJump(), 5));
        equip.setHands(getRandStatAbove(equip.getHands(), 5));
        equip.setSpeed(getRandStatAbove(equip.getSpeed(), 5));
        equip.setPdd(getRandStatAbove(equip.getPdd(), 10));
        equip.setMdd(getRandStatAbove(equip.getMdd(), 10));
        equip.setHp(getRandStatAbove(equip.getHp(), 10));
        equip.setMp(getRandStatAbove(equip.getMp(), 10));
        equip.setSealedLevel((byte) (equip.isSealedEquip() ? 1 : 0));
        equip.setBossDamage((short) getBossDamageRate(equip.getItemId()));
        equip.setIgnorePDR((short) getIgnoreMobDmageRate(equip.getItemId()));
        equip.setTotalDamage((short) getTotalDamage(equip.getItemId()));
        equip.setPotential1(getOption(equip.getItemId(), 1));
        equip.setPotential2(getOption(equip.getItemId(), 2));
        equip.setPotential3(getOption(equip.getItemId(), 3));
        return equip;
    }

    public Equip fuse(Equip equip1, Equip equip2) {
        if (equip1.getItemId() != equip2.getItemId()) {
            return equip1;
        }
        Equip equip = (Equip) getEquipById(equip1.getItemId());
        equip.setStr(getRandStatFusion(equip.getStr(), equip1.getStr(), equip2.getStr()));
        equip.setDex(getRandStatFusion(equip.getDex(), equip1.getDex(), equip2.getDex()));
        equip.setInt(getRandStatFusion(equip.getInt(), equip1.getInt(), equip2.getInt()));
        equip.setLuk(getRandStatFusion(equip.getLuk(), equip1.getLuk(), equip2.getLuk()));
        equip.setMad(getRandStatFusion(equip.getMad(), equip1.getMad(), equip2.getMad()));
        equip.setPad(getRandStatFusion(equip.getPad(), equip1.getPad(), equip2.getPad()));
        equip.setAcc(getRandStatFusion(equip.getAcc(), equip1.getAcc(), equip2.getAcc()));
        equip.setAvoid(getRandStatFusion(equip.getAvoid(), equip1.getAvoid(), equip2.getAvoid()));
        equip.setJump(getRandStatFusion(equip.getJump(), equip1.getJump(), equip2.getJump()));
        equip.setHands(getRandStatFusion(equip.getHands(), equip1.getHands(), equip2.getHands()));
        equip.setSpeed(getRandStatFusion(equip.getSpeed(), equip1.getSpeed(), equip2.getSpeed()));
        equip.setPdd(getRandStatFusion(equip.getPdd(), equip1.getPdd(), equip2.getPdd()));
        equip.setMdd(getRandStatFusion(equip.getMdd(), equip1.getMdd(), equip2.getMdd()));
        equip.setHp(getRandStatFusion(equip.getHp(), equip1.getHp(), equip2.getHp()));
        equip.setMp(getRandStatFusion(equip.getMp(), equip1.getMp(), equip2.getMp()));
        return equip;
    }

    public int get休彼德蔓徽章點數(int itemId) {
        switch (itemId) {
            case 1182000: //休彼德蔓的青銅徽章
                return 3;
            case 1182001: //休彼德蔓的青銅徽章
                return 5;
            case 1182002: //休彼德蔓的白銀徽章
                return 7;
            case 1182003: //休彼德蔓的白銀徽章
                return 9;
            case 1182004: //休彼德蔓的黃金徽章
                return 13;
            case 1182005: //休彼德蔓的黃金徽章
                return 16;
        }
        return 0;
    }

    public Equip randomize休彼德蔓徽章(Equip equip) {
        int stats = get休彼德蔓徽章點數(equip.getItemId());
        if (stats > 0) {
            int prob = equip.getItemId() - 1182000;
            if (Randomizer.nextInt(15) <= prob) { //力量
                equip.setStr((short) Randomizer.nextInt(stats + prob));
            }
            if (Randomizer.nextInt(15) <= prob) { //敏捷
                equip.setDex((short) Randomizer.nextInt(stats + prob));
            }
            if (Randomizer.nextInt(15) <= prob) { //智力
                equip.setInt((short) Randomizer.nextInt(stats + prob));
            }
            if (Randomizer.nextInt(15) <= prob) { //幸運
                equip.setLuk((short) Randomizer.nextInt(stats + prob));
            }
            if (Randomizer.nextInt(30) <= prob) { //物理攻擊
                equip.setPad((short) Randomizer.nextInt(stats));
            }
            if (Randomizer.nextInt(10) <= prob) { //物理防禦
                equip.setPdd((short) Randomizer.nextInt(stats * 8));
            }
            if (Randomizer.nextInt(30) <= prob) { //魔法攻擊
                equip.setMad((short) Randomizer.nextInt(stats));
            }
            if (Randomizer.nextInt(10) <= prob) { //魔法防禦
                equip.setMdd((short) Randomizer.nextInt(stats * 8));
            }
            if (Randomizer.nextInt(8) <= prob) { //命中率
                equip.setAcc((short) Randomizer.nextInt(stats * 5));
            }
            if (Randomizer.nextInt(8) <= prob) { //迴避率
                equip.setAvoid((short) Randomizer.nextInt(stats * 5));
            }
            if (Randomizer.nextInt(10) <= prob) { //移動速度
                equip.setSpeed((short) Randomizer.nextInt(stats));
            }
            if (Randomizer.nextInt(10) <= prob) { //跳躍力
                equip.setJump((short) Randomizer.nextInt(stats));
            }
            if (Randomizer.nextInt(8) <= prob) { //HP
                equip.setHp((short) Randomizer.nextInt(stats * 10));
            }
            if (Randomizer.nextInt(8) <= prob) { //MP
                equip.setMp((short) Randomizer.nextInt(stats * 10));
            }
        }
        return equip;
    }

    public int getTotalStat(Equip equip) { //i get COOL when my defense is higher on gms...
        return equip.getTotalStr() + equip.getTotalDex() + equip.getTotalInt() + equip.getTotalLuk() + equip.getTotalMad() + equip.getTotalPad() + equip.getTotalJump()
                + equip.getTotalHands() + equip.getTotalSpeed() + equip.getTotalHp() + equip.getTotalMp() + equip.getTotalPdd();
    }

    /*
     * 設置裝備潛能
     * 應用與商店購買潛能帶裝備
     * -17 鑒定為B級裝備
     * -18 鑒定為A級裝備
     * -19 鑒定為S級裝備
     * -20 鑒定為SS級裝備
     */
    public Equip setPotentialState(Equip equip, int state) {
        if (equip.getState(false) == 0) {
            if (state == 1) {
                equip.setPotential1(-17);
            } else if (state == 2) {
                equip.setPotential1(-18);
            } else if (state == 3) {
                equip.setPotential1(-19);
            } else if (state == 4) {
                equip.setPotential1(-20);
            } else {
                equip.setPotential1(-17);
            }
        }
        return equip;
    }

    public MapleStatEffect getItemEffect(int itemId) {
        MapleStatEffect ret = itemEffects.get(itemId);
        if (ret == null) {
            MapleData item = getItemData(itemId);
            if (item == null || item.getChildByPath("spec") == null) {
                return null;
            }
            ret = MapleStatEffectFactory.loadItemEffectFromData(item.getChildByPath("spec"), itemId);
            itemEffects.put(itemId, ret);
        }
        return ret;
    }

    public MapleStatEffect getItemEffectEX(int itemId) {
        MapleStatEffect ret = itemEffectsEx.get(itemId);
        if (ret == null) {
            MapleData item = getItemData(itemId);
            if (item == null || item.getChildByPath("specEx") == null) {
                return null;
            }
            ret = MapleStatEffectFactory.loadItemEffectFromData(item.getChildByPath("specEx"), itemId);
            itemEffectsEx.put(itemId, ret);
        }
        return ret;
    }

    public int getCreateId(int id) {
        return getItemProperty(id, "info/create", 0);
    }

    public int getCardMobId(int id) {
        return getItemProperty(id, "info/mob", 0);
    }

    public int getBagType(int id) {
        return getItemProperty(id, "info/bagType", 0);
    }

    public int getWatkForProjectile(int itemId) {
        return getItemProperty(itemId, "info/incPAD", 0);
    }

    /*
     * 能上卷的道具
     * 添加智能機器人心臟
     * 添加寵物項圈
     */
    public boolean canScroll(int scrollid, int itemid) {
        return (scrollid / 100) % 100 == (itemid / 10000) % 100
                || (ItemConstants.類型.心臟(itemid))
                || (scrollid / 1000) % 100 == (itemid / 100) % 100
                || (itemid / 1000 == 1802 && scrollid / 100 == 20488);
    }

    /*
     * 獲取裝備道具的名稱
     */
    public String getName(int itemId) {
        String name = itemName.get(itemId);
        switch (itemId / 1000) {
            case 0:
                if (name == null || name.isEmpty()) {
                    name = itemName.get(itemId + 12000);
                }
                break;
            case 2:
                if (name == null || name.isEmpty()) {
                    name = itemName.get(itemId + 10000);
                }
                if (name != null && !name.isEmpty()) {
                    name += "(身體)";
                }
                break;
            case 12:
                if (name != null && !name.isEmpty()) {
                    name += "(頭部)";
                }
                break;
        }
        return name;
    }

    /*
     * 獲取裝備道具的描述
     */
    public String getDesc(int itemId) {
        return itemDesc.get(itemId);
    }

    public String getMsg(int itemId) {
        return itemMsg.get(itemId);
    }

    public short getItemMakeLevel(int itemId) {
        return getItemProperty(itemId, "info/lv", 0).shortValue();
    }

    /*
     * 0x10 notSale
     * 0x20 expireOnLogout
     * 0x40 pickUpBlock
     * 0x80 only 唯一裝備
     * 0x100 accountSharable 可以帳號共享裝備
     * 0x200 quest 任務道具
     * 0x400 tradeBlock 禁止交易
     * 0x800 accountShareTag
     * 0x1000 mobHP
     * 0x2000 nActivatedSocket 星岩裝備
     * 0x4000 superiorEqp 極真裝備
     * 0x8000 onlyEquip 只能裝備1件
     */

    /**
     * 是否可以賣出
     */
    public boolean cantSell(int itemId) { //true = cant sell, false = can sell
        return getItemProperty(itemId, "info/notSale", 0) == 1;
    }

    /**
     * 道具是否下線消失
     */
    public boolean isLogoutExpire(int itemId) {
        return getItemProperty(itemId, "info/expireOnLogout", 0) == 1;
    }

    /**
     * 道具是否禁止
     */
    public boolean isPickupBlocked(int itemId) {
        return getItemProperty(itemId, "info/pickUpBlock", 0) == 1;
    }

    public boolean isPickupRestricted(int itemId) {
        return (getItemProperty(itemId, "info/only", 0) == 1 || ItemConstants.isPickupRestricted(itemId)) && itemId != 4001168; //金楓葉
    }

    public boolean isAccountShared(int itemId) {
        return getItemProperty(itemId, "info/accountSharable", 0) == 1;
    }

    public boolean isQuestItem(int itemId) {
        return getItemProperty(itemId, "info/quest", 0) == 1 && itemId / 10000 != 301;
    }

    public boolean isDropRestricted(int itemId) {
        return (getItemProperty(itemId, "info/quest", 0) == 1 || getItemProperty(itemId, "info/tradeBlock", 0) == 1 || ItemConstants.isDropRestricted(itemId));
    }

    public boolean isTradeBlock(int itemId) {
        return getItemProperty(itemId, "info/tradeBlock", 0) == 1;
    }

    public boolean isShareTagEnabled(int itemId) {
        return getItemProperty(itemId, "info/accountShareTag", 0) == 1;
    }

    public boolean isSharableOnce(int itemId) {
        return getItemProperty(itemId, "info/sharableOnce", 0) == 1;
    }

    public boolean isMobHP(int itemId) {
        return getItemProperty(itemId, "info/mobHP", 0) == 1;
    }

    public boolean isEquipTradeBlock(int itemId) {
        return getItemProperty(itemId, "info/equipTradeBlock", 0) == 1;
    }

    /**
     * 是否帶鑲嵌星岩提示
     */
    public boolean isActivatedSocketItem(int itemId) {
        return getItemProperty(itemId, "info/nActivatedSocket", 0) == 1;
    }

    /**
     * 是否道具強化屬性得到大幅提升
     */
    public boolean isSuperiorEquip(int itemId) {
        return getItemProperty(itemId, "info/superiorEqp", 0) == 1;
    }

    /**
     * 是否為只能穿戴1件的裝備
     */
    public boolean isOnlyEquip(int itemId) {
        return getItemProperty(itemId, "info/onlyEquip", 0) == 1;
    }

    public int getStateChangeItem(int itemId) {
        return getItemProperty(itemId, "info/stateChangeItem", 0);
    }

    public int getMeso(int itemId) {
        return getItemProperty(itemId, "info/meso", 0);
    }

    public boolean isTradeAvailable(int itemId) {
        return getItemProperty(itemId, "info/tradeAvailable", 0) == 1;
    }

    public boolean isPKarmaEnabled(int itemId) {
        return getItemProperty(itemId, "info/tradeAvailable", 0) == 2;
    }

    public Pair<Integer, List<Map<String, String>>> getRewardItem(int itemId) {
        Map<?, ?> data = getItemProperty(itemId, "reward", null);
        if (data == null) {
            return null;
        }
        List<Map<String, String>> ret = new ArrayList<>();
        int totalprob = 0;
        for (Entry<?, ?> entry : data.entrySet()) {
            Map<String, String> rewards = new HashMap<>();
            ((Map<?, ?>) entry.getValue()).forEach((o, o2) -> rewards.put((String) o, o2 instanceof Integer ? String.valueOf(o2) : (String) o2));
            ret.add(rewards);
            totalprob += rewards.containsKey("prob") ? Integer.valueOf(rewards.get("prob")) : 0;
        }
        return new Pair<>(totalprob, ret);
    }

    @SuppressWarnings("unchecked")
    public Pair<Integer, Map<Integer, Integer>> questItemInfo(int itemId) {
        Integer questId = getItemProperty(itemId, "info/questId", 0);
        Map<?, ?> data = getItemProperty(itemId, "info/consumeItem", null);
        if (data == null) {
            return null;
        }
        Map<Integer, Integer> ret = new HashMap<>();
        for (Entry<?, ?> entry : data.entrySet()) {
            if (entry.getValue() instanceof Map<?, ?>) {
                Map<Integer, Integer> subentry = (Map<Integer, Integer>) entry.getValue();
                ret.put(subentry.get(0), subentry.get(1));
            } else {
                ret.put((Integer) entry.getValue(), 1);
            }
        }
        return new Pair<>(questId, ret);
    }

    public Map<String, String> replaceItemInfo(int itemId) {
        Map<String, String> data = getItemProperty(itemId, "info/replace", new HashMap<String, String>());
        if (data == null) {
            return null;
        }
        return data;
    }

    public List<Triple<String, Point, Point>> getAfterImage(String after) {
        return afterImage.get(after);
    }

    public String getAfterImage(int itemId) {
        return getItemProperty(itemId, "info/afterImage", null);
    }

    public boolean itemExists(int itemId) {
        return ItemConstants.getInventoryType(itemId) != MapleInventoryType.UNDEFINED && itemDataCache.containsKey(itemId);
    }

    public boolean isCash(int itemId) {
        return ItemConstants.getInventoryType(itemId, false) == MapleInventoryType.CASH || itemId / 1000000 == 9 || String.valueOf(getItemProperty(itemId, "info/cash", "0")).equals("1");
    }

    public int getExpCardRate(int itemId) {
        return getItemProperty(itemId, "info/rate", 0);
    }

    public int getExpCardMinLevel(int itemId) {
        return getItemProperty(itemId, "info/minLevel", 1);
    }

    public int getExpCardMaxLevel(int itemId) {
        return getItemProperty(itemId, "info/maxLevel", 274);
    }

    public boolean isExpOrDropCardTime(int itemId) {
        Map<Integer, String> data = getItemProperty(itemId, "info/time", null);
        if (data == null) {
            return false;
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/ShangHai"));
        String day = DateUtil.getDayInt(cal.get(Calendar.DAY_OF_WEEK));
        //System.out.println("當前時間: " + cal.get(Calendar.HOUR_OF_DAY));
        Map<String, String> times = new HashMap<>();
        for (String childdata : data.values()) { //MON:03-07
            String[] time = childdata.split(":");
            times.put(time[0], time[1]);
        }
        cal.get(Calendar.DAY_OF_WEEK);
        if (times.containsKey(day)) {
            String[] hourspan = times.get(day).split("-");
            int starthour = Integer.parseInt(hourspan[0]);
            int endhour = Integer.parseInt(hourspan[1]);
            //System.out.println("starthour: " + starthour + " endhour: " + endhour + " nowhour: " + cal.get(Calendar.HOUR_OF_DAY));
            if (cal.get(Calendar.HOUR_OF_DAY) >= starthour && cal.get(Calendar.HOUR_OF_DAY) <= endhour) {
                return true;
            }
        }
        return false;
    }

    /**
     * 腳本道具物品
     */
    public ScriptedItem getScriptedItemInfo(int itemId) {
        return new ScriptedItem(
                getItemProperty(itemId, "spec/npc", 0),
                getItemProperty(itemId, "spec/Plugin.script", ""),
                getItemProperty(itemId, "spec/runOnPickup", 0) == 1);
    }

    /**
     * 十字獵人商店數據
     */
    public StructCrossHunterShop getCrossHunterShop(int key) {
        if (crossHunterShop.containsKey(key)) {
            return crossHunterShop.get(key);
        }
        return null;
    }

    /**
     * 擁有漂浮效果的道具
     */
    public boolean isFloatCashItem(int itemId) {
        return (itemId / 10000) == 512 && getItemProperty(itemId, "info/floatType", 0) == 1;
    }

    /**
     * 寵物狀態信息
     */
    public short getPetFlagInfo(int itemId) {
        short flag = 0;
        if ((itemId / 10000) != 500) {
            return flag;
        }
        if (!itemExists(itemId)) {
            return flag;
        }
        if (getItemProperty(itemId, "info/pickupItem", 0) == 1) { //揀取道具
            flag |= PetFlag.PET_PICKUP_ITEM.getValue();
        }
        if (getItemProperty(itemId, "info/longRange", 0) == 1) { //擴大移動範圍
            flag |= PetFlag.PET_LONG_RANGE.getValue();
        }
        if (getItemProperty(itemId, "info/pickupAll", 0) == 1) { //範圍自動撿起
            flag |= PetFlag.PET_DROP_SWEEP.getValue();
        }
        if (getItemProperty(itemId, "info/sweepForDrop", 0) == 1) { //撿取無所有權的道具和楓幣
            flag |= PetFlag.PET_PICKUP_ALL.getValue();
        }
        if (getItemProperty(itemId, "info/consumeHP", 0) == 1) { //自動補HP藥水
            flag |= PetFlag.PET_CONSUME_HP.getValue();
        }
        if (getItemProperty(itemId, "info/consumeMP", 0) == 1) { //自動補MP藥水
            flag |= PetFlag.PET_CONSUME_MP.getValue();
        }
        if (getItemProperty(itemId, "info/autoBuff", 0) == 1) { //自動使用增益技能
            flag |= PetFlag.LP_PET_AUTO_BUFF.getValue();
        }
        return (short) (flag | ServerConfig.PET_DEFAULT_FLAG);
    }

    /**
     * 寵物觸發的套裝ID
     */
    public int getPetSetItemID(int itemId) {
        if (itemId / 10000 != 500) {
            return -1;
        }
        return getItemProperty(itemId, "info/setItemID", 0);
    }

    /**
     * 裝備加百分百HP
     */
    public int getItemIncMHPr(int itemId) {
        return getItemProperty(itemId, "info/incMHPr", 0);
    }

    /**
     * 裝備加百分百MP
     */
    public int getItemIncMMPr(int itemId) {
        return getItemProperty(itemId, "info/incMMPr", 0);
    }

    /**
     * 卷軸成功幾率 幾個特殊的卷 2046006 - 週年慶單手武器攻擊力卷軸 - 提高單手武器的功能物理攻擊力屬性。
     */
    public int getSuccessRates(int itemId) {
        if ((itemId / 10000) != 204) {
            return 0;
        }
        return getItemProperty(itemId, "info/successRates/0", 0);
    }

    /**
     * 強化卷軸成功提升的星級
     */
    public int getForceUpgrade(int itemId) {
        return getItemProperty(itemId, "info/forceUpgrade", 0);
    }

    /**
     * 自帶安全盾的卷軸
     */
    public boolean hasSafetyShield(int itemId) {
        return getItemProperty(itemId, "info/safetyShield", 0) == 1;
    }

    /**
     * 椅子恢復的HP和MP
     */
    public Pair<Integer, Integer> getChairRecovery(int itemId) {
        if (itemId / 10000 != 301) {
            return null;
        }
        return new Pair<>(getItemProperty(itemId, "info/recoveryHP", 0), getItemProperty(itemId, "info/recoveryMP", 0));
    }

    /**
     * 裝備帶默認BOSS攻擊
     */
    public int getBossDamageRate(int itemId) {
        return getItemProperty(itemId, "info/bdR", 0);
    }

    /**
     * 裝備帶默認無視怪物防禦
     */
    public int getIgnoreMobDmageRate(int itemId) {
        return getItemProperty(itemId, "info/imdR", 0);
    }

    public int getTotalDamage(int itemId) {
        return getItemProperty(itemId, "info/damR", 0);
    }

    /**
     * 裝備帶默認潛能屬性
     */
    public int getOption(int itemId, int level) {
        return getItemProperty(itemId, "info/option/" + (level - 1) + "option", 0);
    }

    /**
     * 獲取智能機器人的類型
     */
    public int getAndroidType(int itemId) {
        if (itemId / 10000 != 166) { //好像機器人道具為 1662000 - 1662034 和 1666000
            return 0;
        }
        return getItemProperty(itemId, "info/android", 1);
    }

    /**
     * 卷軸失敗不裝備不損壞的卷軸
     */
    public boolean isNoCursedScroll(int itemId) {
        return getItemProperty(itemId, "info/noCursed", 0) == 1;
    }

    /**
     * 正向卷軸 不減少道具屬性
     */
    public boolean isNegativeScroll(int itemId) {
        return getItemProperty(itemId, "info/noNegative", 0) == 1;
    }

    public int getRecover(int itemId) {
        return getItemProperty(itemId, "info/recover", 0);
    }

    /**
     * 是否禁止重複穿戴的裝備道具
     */
    public boolean isExclusiveEquip(int itemId) {
        return exclusiveEquip.containsKey(itemId);
    }

    public StructExclusiveEquip getExclusiveEquipInfo(int itemId) {
        if (exclusiveEquip.containsKey(itemId)) {
            int exclusiveId = exclusiveEquip.get(itemId);
            if (exclusiveEquipInfo.containsKey(exclusiveId)) {
                return exclusiveEquipInfo.get(exclusiveId);
            }
        }
        return null;
    }

    public Map<String, Integer> getSealedEquipInfo(int itemId, int level) {
        //TODO: 漩渦裝備屬性加載
        return sealedEquipInfo.computeIfAbsent(itemId, key -> getItemProperty(key, "info/sealed/info", new HashMap<>())).getOrDefault(String.valueOf(level), null);
//        if (sealedEquipInfo.containsKey(itemId) && sealedEquipInfo.get(itemId).containsKey(level)) {
//            return sealedEquipInfo.get(itemId).get(level);
//        }
//        return getItemProperty(itemId, "info/sealed/info/" + level, null);
    }

    /**
     * 獲取技能皮膚對應的技能ID
     */
    public int getSkillSkinFormSkillId(int itemId) {
        if (itemId / 1000 != 1603) {
            return 0;
        }
        return getItemProperty(itemId, "info/skillID", 0);
    }

    /**
     * 獲取道具鏈接的iconID
     *
     * @param itemId
     * @return
     */
    public int getInLinkID(int itemId) {
        Integer linkid = getItemProperty(itemId, "info/_inlink", 0);
        if (linkid == 0) {
            linkid = getItemProperty(itemId, "info/_outlink", 0);
        }
        return linkid != 0 && itemId != linkid ? getInLinkID(linkid) : itemId;
    }

    public Map<String, Integer> getBookSkillID(int itemId) {
        return getItemProperty(itemId, "info/skill", new HashMap<>());
    }

    public int getReqEquipLevelMax(int itemId) {
        return getItemProperty(itemId, "info/reqEquipLevelMax", GameConstants.MAX_LEVEL);
    }

    public boolean isSkinExist(int id) {
        return ItemConstants.類型.膚色(id) && itemDataCache.containsKey((id % 100) + 12000);
    }

    public boolean isHairExist(int id) {
        if (String.valueOf(id).length() == 8) {
            id /= 1000;
        }
        return ItemConstants.類型.髮型(id) && itemDataCache.containsKey(id);
    }

    public boolean isFaceExist(int id) {
        if (String.valueOf(id).length() == 8) {
            id /= 1000;
        }
        return ItemConstants.類型.臉型(id) && itemDataCache.containsKey(id);
    }

    public Pair<Integer, Integer> getSocketReqLevel(int itemId) {
        int socketId = itemId % 1000 + 1;
        if (!socketReqLevel.containsKey(socketId)) {
            MapleData skillOptionData = itemData.getData("SkillOption.img");
            MapleData socketData = skillOptionData.getChildByPath("socket");
            int reqLevelMax = MapleDataTool.getIntConvert(socketId + "/reqLevelMax", socketData, GameConstants.MAX_LEVEL);
            int reqLevelMin = MapleDataTool.getIntConvert(socketId + "/reqLevelMin", socketData, 70);
            socketReqLevel.put(socketId, new Pair<>(reqLevelMax, reqLevelMin));
        }
        return socketReqLevel.get(socketId);
    }

    public int getSoulSkill(int itemId) {
        int soulName = itemId % 1000 + 1;
        if (!soulSkill.containsKey(soulName)) {
            MapleData skillOptionData = itemData.getData("SkillOption.img");
            MapleData skillData = skillOptionData.getChildByPath("skill");
            int skillId = MapleDataTool.getIntConvert(soulName + "/skillId", skillData, 0);
            soulSkill.put(soulName, skillId);
        }
        return soulSkill.get(soulName);
    }

    public SoulCollectionEntry getSoulCollection(int soulid) {
        return soulCollections.get(soulid);
    }

    public ArrayList<Integer> getTempOption(int itemId) {
        int soulName = itemId % 1000 + 1;
        if (!tempOption.containsKey(soulName)) {
            MapleData skillOptionData = itemData.getData("SkillOption.img");
            MapleData tempOptionData = skillOptionData.getChildByPath("skill/" + soulName + "/tempOption");
            ArrayList<Integer> pots = new ArrayList<>();
            for (MapleData pot : tempOptionData) {
                pots.add(MapleDataTool.getIntConvert("id", pot, 1));
            }
            tempOption.put(soulName, pots);
        }
        return tempOption.get(soulName);
    }

    public Map<Integer, Map<Integer, Float>> getFamiliarTable_pad() {
        return familiarTable_pad;
    }

    public Map<Integer, Map<Integer, Short>> getFamiliarTable_rchance() {
        return familiarTable_rchance;
    }

    public Map<Integer, LinkedList<StructItemOption>> getFamiliar_option() {
        return familiar_option;
    }

    public Map<Integer, Integer> getDamageSkinBox() {
        return damageSkinBox;
    }

    public int getDamageSkinItemId(int n) {
        return damageSkinBox_invert.getOrDefault(n, -1);
    }

    public Map<Integer, VCoreDataEntry> getCoreDatas() {
        return coreDatas;
    }

    public VCoreDataEntry getCoreData(int id) {
        return coreDatas.get(id);
    }

    public Map<String, List<VCoreDataEntry>> getCoreDatasByType(final int type, boolean indieJob, boolean ableGemStone) {
        Map<String, List<VCoreDataEntry>> coreDatas = new HashMap<>();
        for (VCoreDataEntry vCoreDataEntry : this.coreDatas.values()) {
            if (type == vCoreDataEntry.getType() && (ableGemStone || !vCoreDataEntry.isNobAbleGemStone()) && (!indieJob || vCoreDataEntry.getJobs().size() == 1)) {
                for (String job : vCoreDataEntry.getJobs()) {
                    coreDatas.computeIfAbsent(job, k -> new ArrayList<>()).add(vCoreDataEntry);
                }
            }
        }
        return coreDatas;
    }

    public List<VCoreDataEntry> getCoreDatasByJob(int type, String job) {
        return getCoreDatasByJob(type, job, false);
    }

    public List<VCoreDataEntry> getCoreDatasByJob(int type, String job, boolean indieJob) {
        return getCoreDatasByJob(type, job, indieJob, false);
    }

    public List<VCoreDataEntry> getCoreDatasByJob(int type, String job, boolean indieJob, boolean ableGemStone) {
        return getCoreDatasByType(type, indieJob, ableGemStone).getOrDefault(job, new LinkedList<>());
    }

    public Map<Integer, Map<Integer, Triple<Integer, Integer, Integer>>> getVcores() {
        return vcores;
    }

    public Map<Integer, Triple<Integer, Integer, Integer>> getVcores(int type) {
        return vcores.get(type);
    }

    public Map<Integer, List<Pair<Integer, String>>> getCoreJobSkills() {
        return coreJobSkills;
    }

    public List<Pair<Integer, String>> getCoreJobSkill(int job) {
        return coreJobSkills.getOrDefault(job, new LinkedList<>());
    }

    public int getLife(int itemId) {
        return getItemProperty(itemId, "info/life", 90);
    }

    public Map<Integer, Integer> getSoulSkills() {
        return soulSkills;
    }

    public boolean isRunOnPickup(int itemId) {
        ScriptedItem si = getScriptedItemInfo(itemId);
        return si != null && si.runOnPickup();
    }

    public MapleStatEffect getNickItemEffect(int nickItemID) {
        int nickSkill = getItemProperty(nickItemID, "info/nickSkill", 0);
        return nickSkill == 0 ? null : SkillFactory.getSkillEffect(nickSkill, 1);
    }

    public boolean isNickSkillTimeLimited(int nickItemID) {
        int nickSkillTimeLimited = getItemProperty(nickItemID, "info/nickSkillTimeLimited", 0);
        return nickSkillTimeLimited == 1;
    }

    public int getLimitedLife(int itemId) {
        return getItemProperty(itemId, "info/limitedLife", 0);
    }

    public boolean isNoPetEquipStatMoveItem(int itemId) {
        return getItemProperty(itemId, "info/noPetEquipStatMoveItem", 0) == 1;
    }

    public boolean isFixedPotential(int itemId) {
        return getItemProperty(itemId, "info/fixedPotential", 0) == 1;
    }

    public boolean isOnly(int itemId) {
        return 2432107 == itemId || getItemProperty(itemId, "info/only", 0) == 1;
    }

    public boolean isChoice(int itemId) {
        return getItemProperty(itemId, "info/choice", 0) == 1;
    }

    public int getIncCharmExp(int itemId) {
        return getItemProperty(itemId, "info/incCharmExp", 0);
    }

    public boolean isConsumeOnPickup(int itemId) {
        return getItemProperty(itemId, "spec/onsumeOnPickup", 0) == 1;
    }

    public boolean isBossReward(int itemId) {
        return getItemProperty(itemId, "info/bossReward", 0) == 1;
    }
}
