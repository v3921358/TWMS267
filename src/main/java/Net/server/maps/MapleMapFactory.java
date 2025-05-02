package Net.server.maps;

import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.enums.MapleFieldType;
import Database.DatabaseLoader.DatabaseConnection;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Database.tools.SqlTool;
import Net.server.InitializeServer;
import Net.server.MaplePortal;
import Net.server.life.AbstractLoadedMapleLife;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Net.server.life.MapleNPC;
import Net.server.maps.MapleNodes.DirectionInfo;
import Net.server.maps.MapleNodes.MapleNodeInfo;
import Net.server.maps.MapleNodes.MapleNodeStopInfo;
import Net.server.maps.MapleNodes.MaplePlatform;
import Net.server.maps.field.*;
import Plugin.provider.*;
import tools.Pair;
import tools.Randomizer;
import tools.StringUtil;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class MapleMapFactory {

    // UI.wz\UIWindow2.img\EasyMove\
    public enum QuickMove {

        大亂鬥(0, 9070004, 30, "移動到可以和其他玩家比試實力的大亂鬥區域#c<戰鬥廣場-赤壁>#。\n#c30級以上可以移動"),
        怪物公園(1, 9071003, 20, "和隊員們齊心合力攻略強悍怪物的區域.\n移動到#c<怪物公園>#.\n#c一般怪物公園:  100級以上可參加\n 怪物競技場: 70級 ~ 200級"),
        次元之鏡(2, 9010022, 10, "使用可傳送到組隊任務等各種副本地圖的#c<次元之鏡>#。"),
        自由市場(3, 9000087, 0, "移動到可以跟其他玩家交易的 #c<自由市場>#。"),
        梅斯特鎮(4, 9000088, 35, "可移動到專業技術村#c<梅斯特鎮>#。\n#c等級35以上才能移動"),
        大陸移動碼頭(5, 9000086, 0, "傳送到最近的#c<大陸移動碼頭>#。"), //Boats, Airplanes
        計程車(6, 9000089, 0, "使用#c<計程車>#可將角色移動到附近主要地區。"), //Taxi, Camel
        戴彼得(7, 9010040, 10, ""),
        被派來的藍多普(8, 0, 10, ""),
        被派來的露西亞(9, 0, 10, ""),
        打工(10, 9010041, 30, "獲得打工的酬勞。"),
        末日風暴防具商店(11, 9010047, 30, ""),
        末日風暴武器商店(12, 9010048, 30, ""),
        皇家美髮(13, 9000123, 0, "可以讓比克·艾德華為你修剪一頭帥氣的髮型。"),
        皇家整形(14, 9000124, 0, "可以讓Dr·塑膠洛伊為你進行整型手術。"),
        冬季限量防具商店(15, 9000152, 30, ""),
        冬季限量武器商店(16, 9000153, 30, ""),
        琳(17, 9000366, 30, "能使用高級服務專用金幣跟琳購買道具。"),
        巨商月妙(18, 9001088, 30, ""),
        彌莎(19, 9000226, 10, ""),
        楓之谷拍賣場(20, 9030300, 0, "透過艾格利其可使用楓之谷拍賣場。"),
        布雷妮(21, 9062008, 0, "可跟楓幣商城負責人布雷妮小姐進行對話。"),
        收集器(22, 9001212, 0, "可以和收藏家交易。"),
        蕾雅(23, 2010011, 0, "透過蕾雅可以往#c公會本部<英雄之殿>#移動。"),
        副官MR_潘喬(24, 9010111, 0, "透過副官MR.潘喬可以使用楓之谷聯盟硬幣商店"),
        戰國露西亞(100, 9130033, 30, ""),//戰國商店
        戰國藍多普(101, 9130032, 30, ""),//戰國商店
        初音未來(102, 0, 30, "移動至初音未來合作特設地圖#c<初音未來的演唱會會場>#。"),
        慧拉(104, 9201594, 10, "透過慧拉可拜訪結婚小鎮。"),
        組隊遊戲(105, 0, 30, "可移動到組隊遊戲的特別地區"),
        ;
        private final int _value;
        public final int VALUE, NPC, MIN_LEVEL;
        public final String DESC;

        QuickMove(int value, int npc, int minLevel, String desc) {
            _value = 1 << this.ordinal();
            VALUE = value;
            NPC = npc;
            MIN_LEVEL = minLevel;
            DESC = desc;
        }

        public final int getValue() {
            return _value;
        }

        public final boolean check(int flag) {
            return (flag & _value) != 0;
        }

        public final MapleQuickMove getMapleQuickMove() {
            MapleQuickMove mqm = new MapleQuickMove();
            mqm.VALUE = VALUE;
            mqm.NPC = NPC;
            mqm.MIN_LEVEL = MIN_LEVEL;
            mqm.DESC = DESC;
            return mqm;
        }
    }

    public enum QuickMoveMap {

        弓箭手村(100000000, QuickMove.大陸移動碼頭.getValue() | QuickMove.計程車.getValue()),
        魔法森林(101000000, QuickMove.大陸移動碼頭.getValue() | QuickMove.計程車.getValue()),
        勇士之村(102000000, QuickMove.大陸移動碼頭.getValue() | QuickMove.計程車.getValue()),
        墮落城市(103000000, QuickMove.大陸移動碼頭.getValue() | QuickMove.計程車.getValue()),
        維多利亞港(104000000, QuickMove.大陸移動碼頭.getValue() | QuickMove.計程車.getValue()),
        奇幻村(105000000, QuickMove.大陸移動碼頭.getValue() | QuickMove.計程車.getValue()),
        鯨魚號(120000100, QuickMove.大陸移動碼頭.getValue() | QuickMove.計程車.getValue()),
        耶雷弗(130000000, QuickMove.大陸移動碼頭.getValue()),
        瑞恩(140000000, QuickMove.大陸移動碼頭.getValue()),
        天空之城(200000000, QuickMove.大陸移動碼頭.getValue()),
        冰原雪域(211000000, 0),
        玩具城(220000000, QuickMove.大陸移動碼頭.getValue()),
        地球防禦本部(221000000, QuickMove.大陸移動碼頭.getValue()),
        童話村(222000000, QuickMove.大陸移動碼頭.getValue()),
        水之都(230000000, 0),
        神木村(240000000, QuickMove.大陸移動碼頭.getValue()),
        克里提亞斯(241000100, 0),
        桃花仙境(250000000, QuickMove.大陸移動碼頭.getValue()),
        靈藥幻境(251000000, 0),
        納希沙漠(260000000, QuickMove.大陸移動碼頭.getValue()),
        瑪迦提亞(261000000, 0),
        埃德爾斯坦(310000000, QuickMove.大陸移動碼頭.getValue()),
        萬神殿(400000000, 0),
        天堂(310070000, 0),
        被遺棄的露營地(105300000, 0),
        野蠻之星(402000000, 0),
        無名村落(450001000, 0),
        反轉城市(450014050, 0),
        啾啾村(450002000, 0),
        嚼嚼村落(450015060, 0),
        拉契爾恩(450003000, 0),
        魔菈斯(450006130, 0),
        艾斯佩拉(450007040, 0),
        賽拉斯(450016000, 0),
        月之橋(450009300, 0),
        苦痛迷宮(450011320, 0),
        利曼(450012300, 0),
        賽爾尼溫(410000500, 0),
        阿爾克斯(410003010, 0),
        奧迪溫(410007000, 0),
        ;

        private final int map;
        private final int npc;
        private final int generalNpc =
                QuickMove.怪物公園.getValue()
                        | QuickMove.次元之鏡.getValue()
                        | QuickMove.自由市場.getValue()
                        | QuickMove.梅斯特鎮.getValue()
                        | QuickMove.楓之谷拍賣場.getValue()
                        | QuickMove.布雷妮.getValue()
                        | QuickMove.收集器.getValue()
                        | QuickMove.蕾雅.getValue()
                        | QuickMove.副官MR_潘喬.getValue();

        private QuickMoveMap(int map, int npc) {
            this.map = map;
            this.npc = npc | generalNpc;
        }

        public int getMap() {
            return map;
        }

        public int getNPCFlag() {
            return npc;
        }
    }

    private static final MapleDataProvider source = MapleDataProviderFactory.getMap();
    private final HashMap<Integer, MapleMap> maps = new HashMap<>();
    private final HashMap<Integer, MapleMap> instanceMap = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private int channel;
    private static final Map<Integer, List<Integer>> linknpcs = new HashMap<>();
    private static final Map<Integer, String> mapName = new HashMap<>();
    private static final Map<Integer, String> mapStreet = new HashMap<>();

    public MapleMapFactory(int channel) {
        this.channel = channel;
    }

    public static void loadAllMapName() {
        MapleData nameData = MapleDataProviderFactory.getString().getData("Map.img");
        for (MapleData mapleData : nameData) {
            for (MapleData data : mapleData) {
                for (MapleData subdata : data) {
                    if (subdata.getName().equalsIgnoreCase("mapName")) {
                        mapName.put(Integer.valueOf(data.getName()), subdata.getData().toString());
                    }
                    if (subdata.getName().equalsIgnoreCase("streetName")) {
                        mapStreet.put(Integer.valueOf(data.getName()), subdata.getData().toString());
                    }
                }
            }
        }
    }

    public static String getMapName(int mapid) {
        if (mapName.containsKey(mapid)) {
            return mapName.get(mapid);
        } else {
            return "";
        }
    }

    public static Map<Integer, String> getMapNames() {
        return mapName;
    }

    public static String getMapStreetName(int mapid) {
        if (mapStreet.containsKey(mapid)) {
            return mapStreet.get(mapid);
        } else {
            return "";
        }
    }

    public static Map<Integer, String> getMapStreetNames() {
        return mapStreet;
    }

    public static void loadAllLinkNpc() {
        DatabaseConnection.domain(con -> {
            if (InitializeServer.WzSqlName.wz_maplinknpcs.check(con)) {//load from sql
                SqlTool.queryAndGetList(con, "SELECT * FROM `wz_maplinknpcs`", rs -> {
                    List<Integer> list = new ArrayList<>();
                    for (String npcid : rs.getString("npcids").split(",")) {
                        list.add(Integer.parseInt(npcid));
                    }
                    linknpcs.put(rs.getInt("mapid"), list);
                    return null;
                });
            } else {//load from wz and insert into sql
                InitializeServer.WzSqlName.wz_maplinknpcs.drop(con);
                SqlTool.update(con, "CREATE TABLE `wz_maplinknpcs` (`mapid` int NOT NULL,`npcids` text NOT NULL,PRIMARY KEY (`mapid`))");
                for (MapleDataDirectoryEntry directoryEntry : source.getRoot().getSubdirectories()) {
                    if (directoryEntry.getName().equals("Map")) {
                        for (MapleDataDirectoryEntry directoryEntry1 : directoryEntry.getSubdirectories()) {
                            if (directoryEntry1.getName().startsWith("Map")) {
                                for (MapleDataFileEntry fileEntry : directoryEntry1.getFiles()) {
                                    for (MapleData life : source.getData(directoryEntry.getName() + "/" + directoryEntry1.getName() + "/" + fileEntry.getName())) {
                                        if (life.getName().equals("life")) {
                                            List<Integer> npcids = new ArrayList<>();
                                            StringBuilder s = new StringBuilder();
                                            boolean b = false;
                                            for (MapleData mapleData : life) {
                                                MapleData type1 = mapleData.getChildByPath("type");
                                                if (type1 == null) {
                                                    continue;
                                                }
                                                String type = MapleDataTool.getString(type1);
                                                if (type != null && type.equals("n")) {
                                                    int npcid = MapleDataTool.getIntConvert(mapleData.getChildByPath("id"), 0);
                                                    npcids.add(npcid);
                                                    if (b) {
                                                        s.append(",");
                                                    } else {
                                                        b = true;
                                                    }
                                                    s.append(npcid);
                                                }
                                            }
                                            if (!npcids.isEmpty()) {
                                                int mapid = Integer.valueOf(fileEntry.getName().substring(0, 9));
                                                linknpcs.put(mapid, npcids);
                                                SqlTool.update(con, "INSERT INTO `wz_maplinknpcs` (`mapid`,`npcids`) VALUES (?,?)", mapid, s.toString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                InitializeServer.WzSqlName.wz_maplinknpcs.update(con);
            }
            return null;
        });

    }

    public static Map<Integer, List<Integer>> getAllLinkNpc() {
        return linknpcs;
    }

    /*
     * 獲取地圖的xml文件的名字
     */
    private static String getMapXMLName(int mapid) {
        String mapName = StringUtil.getLeftPaddedStr(Integer.toString(mapid), '0', 9);
        mapName = "Map/Map" + mapid / 100000000 + "/" + mapName + ".img";
        return mapName;
    }

    public MapleMap getMap(int mapid) {
        return getMap(mapid, true, true, true);
    }

    //backwards-compatible
    public MapleMap getMap(int mapid, boolean respawns, boolean npcs) {
        return getMap(mapid, respawns, npcs, true);
    }

    public MapleMap getMap(int mapid, boolean respawns, boolean npcs, boolean reactors) {
        if (mapid == 0) {
            return null;
        }
        Integer omapid = mapid;
        MapleMap map = maps.get(omapid);
        if (map == null) {
            lock.lock();
            try {
                map = maps.get(omapid);
                if (map != null) {
                    return map;
                }
                MapleData mapData;
                try {
                    mapData = source.getData(getMapXMLName(mapid));
                } catch (Exception e) {
                    mapData = null;
                }
                if (mapData == null) {
                    return null;
                }
                int linkMapId = -1;
                MapleData link = mapData.getChildByPath("info/link");
                if (link != null) {
                    linkMapId = MapleDataTool.getIntConvert("info/link", mapData);
                    mapData = source.getData(getMapXMLName(linkMapId));
                }

                float monsterRate = 0;
                if (respawns) {
                    MapleData mobRate = mapData.getChildByPath("info/mobRate");
                    if (mobRate != null) {
                        monsterRate = (Float) mobRate.getData();
                    }
                }
                int fieldType = MapleDataTool.getInt(mapData.getChildByPath("info/fieldType"), 0);
                int returnMapId = MapleDataTool.getInt("info/returnMap", mapData);
                switch (MapleFieldType.getByType(fieldType)) {
                    case FIELDTYPE_SEREN_STAGE1:
                    case FIELDTYPE_SEREN_STAGE2:
                        map = new BossSerenField(mapid, channel, returnMapId, monsterRate);
                        break;
                    case FIELDTYPE_WILL_DIFFRACTION:
                    case FIELDTYPE_WILL_MIRRORCAGE:
                    case FIELDTYPE_WILL_BLOODCAGE:
                        map = new BossWillField(mapid, channel, returnMapId, monsterRate);
                        break;
                    case FIELDTYPE_LUCIDDREAM:
                    case FIELDTYPE_LUCIDBROKEN: {
                        map = new BossLucidField(mapid, channel, returnMapId, monsterRate);
                        break;
                    }
                    case FIELDTYPE_CAPTURE_THE_FLAG: {
                        map = new ActionBarField(mapid, this.channel, returnMapId, monsterRate);
                        break;
                    }
                    case FIELDTYPE_HUNDREDBINGO: {
                        map = new BingoGameField(mapid, this.channel, returnMapId, monsterRate);
                        break;
                    }
                    case FIELDTYPE_HUNDREDOXQUIZ: {
                        map = new OXQuizField(mapid, this.channel, returnMapId, monsterRate);
                        break;
                    }
                    case FIELDTYPE_CAPTAINNOMADBATTLE: {
                        map = new RJ(mapid, this.channel, returnMapId, monsterRate);
                        break;
                    }
                    case FIELDTYPE_CAPTAINNOMAD: {
                        map = new RN(mapid, this.channel, returnMapId, monsterRate);
                        break;
                    }
                    default:
                        int freeMarket = mapid % 910000000;
                        if (freeMarket >= 1 && freeMarket <= 22) {
                            map = new MapleMap(GameConstants.getOverrideChangeToMap(mapid), channel, mapid, monsterRate);
                        } else {
                            map = new MapleMap(mapid, channel, returnMapId, monsterRate);
                        }
                        break;
                }
                map.setFieldType(fieldType);
                map.setLevelLimit(MapleDataTool.getInt(mapData.getChildByPath("info/lvLimit"), 1));
                map.setQuestLimit(MapleDataTool.getInt(mapData.getChildByPath("info/qrLimit"), 0));
                map.setBarrier(MapleDataTool.getInt(mapData.getChildByPath("info/barrier"), 0));
                map.setBarrierArc(MapleDataTool.getInt(mapData.getChildByPath("info/barrierArc"), 0));
                map.setBarrierAut(MapleDataTool.getInt(mapData.getChildByPath("info/barrierAut"), 0));
                // v118 自由市場房間xml數據不完整，因此填空為本圖內
                map.setMapMark(MapleDataTool.getString(mapData.getChildByPath("info/mapMark"), ""));
                loadPortals(map, mapData.getChildByPath("portal"));
                map.setTop(MapleDataTool.getInt(mapData.getChildByPath("info/VRTop"), 0));
                map.setLeft(MapleDataTool.getInt(mapData.getChildByPath("info/VRLeft"), 0));
                map.setBottom(MapleDataTool.getInt(mapData.getChildByPath("info/VRBottom"), 0));
                map.setRight(MapleDataTool.getInt(mapData.getChildByPath("info/VRRight"), 0));
                if (mapData.getChildByPath("areaCtrl") != null) {
                    List<String> allAreaCtrls = new LinkedList<>();
                    for (MapleData areaRoot : mapData.getChildByPath("areaCtrl")) {
                        allAreaCtrls.add(areaRoot.getName());
                    }
                    map.setAreaControls(allAreaCtrls);
                }

                List<MapleFoothold> allFootholds = new LinkedList<>();
                Point lBound = new Point();
                Point uBound = new Point();
                MapleFoothold fh;

                for (MapleData footRoot : mapData.getChildByPath("foothold")) {
                    for (MapleData footCat : footRoot) {
                        for (MapleData footHold : footCat) {
                            fh = new MapleFoothold(new Point(
                                    MapleDataTool.getInt(footHold.getChildByPath("x1"), 0),
                                    MapleDataTool.getInt(footHold.getChildByPath("y1"), 0)),
                                    new Point(
                                            MapleDataTool.getInt(footHold.getChildByPath("x2"), 0),
                                            MapleDataTool.getInt(footHold.getChildByPath("y2"), 0)),
                                    Integer.parseInt(footHold.getName()));
                            fh.setPrev((short) MapleDataTool.getInt(footHold.getChildByPath("prev"), 0));
                            fh.setNext((short) MapleDataTool.getInt(footHold.getChildByPath("next"), 0));

                            if (fh.getX1() < lBound.x) {
                                lBound.x = fh.getX1();
                            }
                            if (fh.getX2() > uBound.x) {
                                uBound.x = fh.getX2();
                            }
                            if (fh.getY1() < lBound.y) {
                                lBound.y = fh.getY1();
                            }
                            if (fh.getY2() > uBound.y) {
                                uBound.y = fh.getY2();
                            }
                            allFootholds.add(fh);
                        }
                    }
                }
                MapleFootholdTree fTree = new MapleFootholdTree(lBound, uBound);
                for (MapleFoothold foothold : allFootholds) {
                    fTree.insert(foothold);
                }
                map.setFootholds(fTree);
                if (map.getTop() == 0) {
                    map.setTop(lBound.y);
                }
                if (map.getBottom() == 0) {
                    map.setBottom(uBound.y);
                }
                if (map.getLeft() == 0) {
                    map.setLeft(lBound.x);
                }
                if (map.getRight() == 0) {
                    map.setRight(uBound.x);
                }
                int bossid = -1;
                String msg = null;
                if (mapData.getChildByPath("info/timeMob") != null) {
                    bossid = MapleDataTool.getInt(mapData.getChildByPath("info/timeMob/id"), 0);
                    msg = MapleDataTool.getString(mapData.getChildByPath("info/timeMob/message"), null);
                }

                try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM spawns WHERE mid = ?");
                    ps.setInt(1, omapid);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        int sqlid = rs.getInt("idd");
                        int sqlf = rs.getInt("f");
                        boolean sqlhide = false;
                        String sqltype = rs.getString("type");
                        int sqlfh = rs.getInt("fh");
                        int sqlcy = rs.getInt("cy");
                        int sqlrx0 = rs.getInt("rx0");
                        int sqlrx1 = rs.getInt("rx1");
                        int sqlx = rs.getInt("x");
                        int sqly = rs.getInt("y");
                        int sqlmobTime = rs.getInt("mobtime");
                        AbstractLoadedMapleLife sqlmyLife = loadLife(sqlid, sqlf, sqlhide, sqlfh, sqlcy, sqlrx0, sqlrx1, sqlx, sqly, sqltype, mapid);
                        switch (sqltype) {
                            case "n":
                                map.addMapObject(sqlmyLife);
                                break;
                            case "m":
                                MapleMonster monster = (MapleMonster) sqlmyLife;
                                map.addMonsterSpawn(monster, sqlmobTime, (byte) -1, null);
                                break;
                        }
                    }
                } catch (SQLException e) {
                    System.out.println("讀取SQL刷Npc和刷新怪物出錯.");
                }
                /*
                 * load life data (npc, monsters)
                 * 加載地圖刷新的NPC和怪物信息
                 */
                List<Point> herbRocks = new ArrayList<>();
                int lowestLevel = 200, highestLevel = 0;

                Map<Integer, Set<Integer>> hideinfo = ServerConfig.WORLD_HIDENPCS_MAP;
                for (MapleData life : mapData.getChildByPath("life")) {
                    final String type = MapleDataTool.getString(life.getChildByPath("type"), "n");
                    final String limited = MapleDataTool.getString("limitedname", life, "");
                    if ((npcs || !type.equals("n")) && (limited.isEmpty() || !limited.contains(map.getMapMark()))) { //alien pq stuff
                        String id = MapleDataTool.getString(life.getChildByPath("id"));
                        AbstractLoadedMapleLife myLife = loadLife(life, id, type, mapid);
                        if (myLife instanceof MapleMonster && !GameConstants.isNoSpawn(mapid)) {
                            MapleMonster mob = (MapleMonster) myLife;

                            herbRocks.add(map.addMonsterSpawn(mob, MapleDataTool.getInt("mobTime", life, 0), (byte) MapleDataTool.getInt("team", life, -1), mob.getId() == bossid ? msg : null).getPosition());
                            if (mob.getStats().getLevel() > highestLevel && !mob.getStats().isBoss()) {
                                highestLevel = mob.getStats().getLevel();
                            }
                            if (mob.getStats().getLevel() < lowestLevel && !mob.getStats().isBoss()) {
                                lowestLevel = mob.getStats().getLevel();
                            }
                        } else if (myLife instanceof MapleNPC) {
                            if (hideinfo.containsKey(-1) && hideinfo.get(-1).contains(myLife.getId())) {
                                continue;
                            }
                            if (hideinfo.containsKey(mapid) && hideinfo.get(mapid).contains(myLife.getId())) {
                                continue;
                            }
                            if (!limited.isEmpty() && !ServerConfig.WORLD_LIMITEDNAMES_LIST.contains(limited)) {
                                map.addHideNpc(myLife.getId());
                                continue;
                            }
                            map.addMapObject(myLife);
                        }
                    }
                }
                addAreaBossSpawn(map);
                map.setCreateMobInterval((short) MapleDataTool.getInt(mapData.getChildByPath("info/createMobInterval"), ServerConfig.ServerSpawnMobSec * 1000));
                map.setFixedMob(MapleDataTool.getInt(mapData.getChildByPath("info/fixedMobCapacity"), 0));
                map.setPartyBonusRate(GameConstants.getPartyPlay(mapid, MapleDataTool.getInt(mapData.getChildByPath("info/partyBonusR"), 0)));
                map.loadMonsterRate();
                map.setNodes(loadNodes(mapid, mapData));
                map.setSpawnPoints(herbRocks);
                /*
                 * 加載地圖刷新的反應堆信息
                 */
                String id;
                if (reactors && mapData.getChildByPath("reactor") != null) {
                    for (MapleData reactor : mapData.getChildByPath("reactor")) {
                        id = MapleDataTool.getString(reactor.getChildByPath("id"));
                        if (id != null) {
                            map.spawnReactor(loadReactor(reactor, id, (byte) MapleDataTool.getInt(reactor.getChildByPath("f"), 0)));
                        }
                    }
                }
                map.setFirstUserEnter(MapleDataTool.getString(mapData.getChildByPath("info/onFirstUserEnter"), ""));
                map.setUserEnter(mapid == GameConstants.JAIL ? "jail" : MapleDataTool.getString(mapData.getChildByPath("info/onUserEnter"), ""));
                if (reactors && herbRocks.size() > 0 && highestLevel >= 30 && map.getFirstUserEnter().equals("") && map.getUserEnter().equals("")) {
                    List<Integer> allowedSpawn = new ArrayList<>(24);
                    allowedSpawn.add(100011); //金色藥草堆
                    allowedSpawn.add(200011); //怦然心跳
                    if (highestLevel >= 100) {
                        for (int i = 0; i < 10; i++) {
                            for (int x = 0; x < 4; x++) { //to make heartstones rare
                                allowedSpawn.add(100000 + i);
                                allowedSpawn.add(200000 + i);
                            }
                        }
                    } else {
                        for (int i = (lowestLevel % 10 > highestLevel % 10 ? 0 : (lowestLevel % 10)); i < (highestLevel % 10); i++) {
                            for (int x = 0; x < 4; x++) { //to make heartstones rare
                                allowedSpawn.add(100000 + i);
                                allowedSpawn.add(200000 + i);
                            }
                        }
                    }
                    int numSpawn = Randomizer.nextInt(allowedSpawn.size()) / 6; //0-7
                    for (int i = 0; i < numSpawn && !herbRocks.isEmpty(); i++) {
                        int idd = allowedSpawn.get(Randomizer.nextInt(allowedSpawn.size()));
                        int theSpawn = Randomizer.nextInt(herbRocks.size());
                        MapleReactor myReactor = new MapleReactor(MapleReactorFactory.getReactor(idd), idd);
                        myReactor.setPosition(herbRocks.get(theSpawn));
                        myReactor.setDelay(idd % 100 == 11 ? 60000 : 5000); //in the reactor's wz
                        map.spawnReactor(myReactor);
                        herbRocks.remove(theSpawn);
                    }
                }
                /*
                 * 設置地圖一些狀態
                 */
                map.setClock(mapData.getChildByPath("clock") != null); //clock was changed in wz to have x,y,width,height
                map.setEverlast(MapleDataTool.getInt(mapData.getChildByPath("info/everlast"), 0) > 0);
                map.setTown(MapleDataTool.getInt(mapData.getChildByPath("info/town"), 0) > 0);
                map.setSoaring(MapleDataTool.getInt(mapData.getChildByPath("info/needSkillForFly"), 0) > 0);
                map.setPersonalShop(MapleDataTool.getInt(mapData.getChildByPath("info/personalShop"), 0) > 0);
                map.setEntrustedFishing(MapleDataTool.getInt(mapData.getChildByPath("info/entrustedFishing"), 0) > 0);
                map.setForceMove(MapleDataTool.getInt(mapData.getChildByPath("info/lvForceMove"), 0));
                map.setDecHP(MapleDataTool.getInt(mapData.getChildByPath("info/decHP"), 0));
                map.setDecHPInterval(MapleDataTool.getInt(mapData.getChildByPath("info/decHPInterval"), 10000));
                map.setProtectItem(MapleDataTool.getInt(mapData.getChildByPath("info/protectItem"), 0));
                map.setForcedReturnMap(mapid == 0 ? 999999999 : MapleDataTool.getInt(mapData.getChildByPath("info/forcedReturn"), 999999999));
                map.setTimeLimit(MapleDataTool.getInt(mapData.getChildByPath("info/timeLimit"), -1));
                map.setFieldLimit(MapleDataTool.getInt(mapData.getChildByPath("info/fieldLimit"), 0));
                map.setMiniMapOnOff(MapleDataTool.getInt(mapData.getChildByPath("info/miniMapOnOff"), 0) > 0);
                map.setRecoveryRate(MapleDataTool.getFloat(mapData.getChildByPath("info/recovery"), 1));
                map.setFixedMob(MapleDataTool.getInt(mapData.getChildByPath("info/fixedMobCapacity"), 0));
                map.setPartyBonusRate(GameConstants.getPartyPlay(mapid, MapleDataTool.getInt(mapData.getChildByPath("info/partyBonusR"), 0)));
                map.setConsumeItemCoolTime(MapleDataTool.getInt(mapData.getChildByPath("info/consumeItemCoolTime"), 0));

                if (mapData.getChildByPath("info/fieldAttackObj") != null) {
                    for (MapleData d : mapData.getChildByPath("info/fieldAttackObj")) {
                        final String regenObjTag = MapleDataTool.getString("regenObjTag", d, "");
                        final int objid = MapleDataTool.getIntConvert("id", d, -1);
                        final int regenTime = MapleDataTool.getIntConvert("regenTime", d, 0);
                        final boolean flip = MapleDataTool.getIntConvert("flip", d, 0) > 0;
                        final int destroyTime = MapleDataTool.getIntConvert("destroyTime", d, 0);
                        if (objid != -1) {
                            map.getFieldAttackObjInfo().add(new FieldAttackObjInfo(regenObjTag, objid, regenTime, flip, destroyTime));
                        }
                    }
                }

                if (mapData.getChildByPath("info/taggedObjRegenInfo") != null) {
                    for (MapleData d : mapData.getChildByPath("info/taggedObjRegenInfo")) {
                        TaggedObjRegenInfo info = new TaggedObjRegenInfo();
                        info.setRemoveTime(MapleDataTool.getIntConvert("removeTime", d, 0));
                        info.setTag(MapleDataTool.getString("sTag", d, ""));
                        info.setRegenTime(MapleDataTool.getIntConvert("regenTime", d, 0));
                        info.setFootHoldOffY(MapleDataTool.getIntConvert("footHoldOffY", d, 0));
                        map.getTaggedObjRegenInfo().add(info);
                    }
                }
                MapleData objtag = mapData.getChildByPath("5/obj");
                if (objtag != null) {
                    for (MapleData d : objtag) {
                        int idx = Integer.valueOf(d.getName());
                        String s = MapleDataTool.getString(d.getChildByPath("tags"), null);
                        if (s != null) {
                            map.getObjTag().put(idx, new Pair<>(s, new Point(MapleDataTool.getInt(d.getChildByPath("x"), 0), MapleDataTool.getInt(d.getChildByPath("y"), 0))));
                        }
                    }
                }
                switch (mapid) {
                    case 450004250:
                    case 450004550:
                    case 450004850:
                        MapleData lucidMapData = mapData.getChildByPath("5/obj");
                        if (lucidMapData != null) {
                            for (MapleData d : lucidMapData) {
                                String s = MapleDataTool.getString(d.getChildByPath("name"), null);
                                if (s != null) {
                                    map.getLacheln().put(s, new Point(MapleDataTool.getInt(d.getChildByPath("x"), 0), MapleDataTool.getInt(d.getChildByPath("y"), 0) - 3));
                                }
                            }
                        }
                        break;

                    case 280030000:
                    case 280030100:
                    case 280030200:
                        MapleData zakumMapData = mapData.getChildByPath("5/obj");
                        if (zakumMapData != null) {
                            for (MapleData d : zakumMapData) {
                                String s = MapleDataTool.getString(d.getChildByPath("name"), null);
                                if (s != null) {
                                    map.getSyncFH().add(new Pair<>(s, new Point(MapleDataTool.getInt(d.getChildByPath("x"), 0), MapleDataTool.getInt(d.getChildByPath("y"), 0))));
                                }
                            }
                        }
                        break;

                }

                initDefaultQuickMove(map);

                map.startFieldScript();

                maps.put(omapid, map);
            } finally {
                lock.unlock();
            }
        }
        return map;
    }

    private void initDefaultQuickMove(MapleMap map) {
        if (ServerConfig.QUICK_MOVE_LIST.isEmpty()) {
            map.QUICK_MOVE = new LinkedList<>();
            int npcs = 0;
            for (QuickMoveMap qmm : QuickMoveMap.values()) {
                if (qmm.getMap() == map.getId()) {
                    npcs = qmm.getNPCFlag();
                    break;
                }
            }
            for (QuickMove qm : QuickMove.values()) {
                if (qm.check(npcs)) {
                    map.QUICK_MOVE.add(qm.getMapleQuickMove());
                }
            }
        } else {
            map.QUICK_MOVE = ServerConfig.QUICK_MOVE_LIST;
        }
    }

    public void loadQuickMove() {
        for (MapleMap map : maps.values()) {
            initDefaultQuickMove(map);
        }
    }

    public MapleMap getInstanceMap(int instanceid) {
        return instanceMap.get(instanceid);
    }

    public void removeInstanceMap(int instanceid) {
        lock.lock();
        try {
            if (isInstanceMapLoaded(instanceid)) {
                instanceMap.remove(instanceid);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeMap(int instanceid) {
        lock.lock();
        try {
            if (isMapLoaded(instanceid)) {
                maps.remove(instanceid);
            }
        } finally {
            lock.unlock();
        }
    }

    public MapleMap CreateInstanceMap(int mapid, boolean respawns, boolean npcs, boolean reactors, int instanceid) {
        lock.lock();
        try {
            if (isInstanceMapLoaded(instanceid)) {
                return getInstanceMap(instanceid);
            }
        } finally {
            lock.unlock();
        }
        MapleData mapData = null;
        try {
            mapData = source.getData(getMapXMLName(mapid));
        } catch (Exception e) {
            return null;
        }
        if (mapData == null) {
            return null;
        }
        MapleData link = mapData.getChildByPath("info/link");
        if (link != null) {
            mapData = source.getData(getMapXMLName(MapleDataTool.getIntConvert("info/link", mapData)));
        }

        float monsterRate = 0;
        if (respawns) {
            MapleData mobRate = mapData.getChildByPath("info/mobRate");
            if (mobRate != null) {
                monsterRate = (Float) mobRate.getData();
            }
        }
        MapleMap map = new MapleMap(mapid, channel, MapleDataTool.getInt("info/returnMap", mapData), monsterRate);
        loadPortals(map, mapData.getChildByPath("portal"));
        map.setTop(MapleDataTool.getInt(mapData.getChildByPath("info/VRTop"), 0));
        map.setLeft(MapleDataTool.getInt(mapData.getChildByPath("info/VRLeft"), 0));
        map.setBottom(MapleDataTool.getInt(mapData.getChildByPath("info/VRBottom"), 0));
        map.setRight(MapleDataTool.getInt(mapData.getChildByPath("info/VRRight"), 0));
        map.setFieldType(MapleDataTool.getInt(mapData.getChildByPath("info/fieldType"), 0));
        map.setLevelLimit(MapleDataTool.getInt(mapData.getChildByPath("info/lvLimit"), 1));
        map.setQuestLimit(MapleDataTool.getInt(mapData.getChildByPath("info/qrLimit"), 0));
        if (mapData.getChildByPath("areaCtrl") != null) {
            List<String> allAreaCtrls = new LinkedList<>();
            for (MapleData areaRoot : mapData.getChildByPath("areaCtrl")) {
                allAreaCtrls.add(areaRoot.getName());
            }
            map.setAreaControls(allAreaCtrls);
        }

        List<MapleFoothold> allFootholds = new LinkedList<>();
        Point lBound = new Point();
        Point uBound = new Point();
        for (MapleData footRoot : mapData.getChildByPath("foothold")) {
            for (MapleData footCat : footRoot) {
                for (MapleData footHold : footCat) {
                    MapleFoothold fh = new MapleFoothold(new Point(
                            MapleDataTool.getInt(footHold.getChildByPath("x1")),
                            MapleDataTool.getInt(footHold.getChildByPath("y1"))),
                            new Point(
                                    MapleDataTool.getInt(footHold.getChildByPath("x2")),
                                    MapleDataTool.getInt(footHold.getChildByPath("y2"))),
                            Integer.parseInt(footHold.getName()));
                    fh.setPrev((short) MapleDataTool.getInt(footHold.getChildByPath("prev")));
                    fh.setNext((short) MapleDataTool.getInt(footHold.getChildByPath("next")));

                    if (fh.getX1() < lBound.x) {
                        lBound.x = fh.getX1();
                    }
                    if (fh.getX2() > uBound.x) {
                        uBound.x = fh.getX2();
                    }
                    if (fh.getY1() < lBound.y) {
                        lBound.y = fh.getY1();
                    }
                    if (fh.getY2() > uBound.y) {
                        uBound.y = fh.getY2();
                    }
                    allFootholds.add(fh);
                }
            }
        }
        MapleFootholdTree fTree = new MapleFootholdTree(lBound, uBound);
        for (MapleFoothold fh : allFootholds) {
            fTree.insert(fh);
        }
        map.setFootholds(fTree);
        if (map.getTop() == 0) {
            map.setTop(lBound.y);
        }
        if (map.getBottom() == 0) {
            map.setBottom(uBound.y);
        }
        if (map.getLeft() == 0) {
            map.setLeft(lBound.x);
        }
        if (map.getRight() == 0) {
            map.setRight(uBound.x);
        }
        int bossid = -1;
        String msg = null;
        if (mapData.getChildByPath("info/timeMob") != null) {
            bossid = MapleDataTool.getInt(mapData.getChildByPath("info/timeMob/id"), 0);
            msg = MapleDataTool.getString(mapData.getChildByPath("info/timeMob/message"), null);
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM spawns WHERE mid = ?");
            ps.setInt(1, mapid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int sqlid = rs.getInt("idd");
                int sqlf = rs.getInt("f");
                boolean sqlhide = false;
                String sqltype = rs.getString("type");
                int sqlfh = rs.getInt("fh");
                int sqlcy = rs.getInt("cy");
                int sqlrx0 = rs.getInt("rx0");
                int sqlrx1 = rs.getInt("rx1");
                int sqlx = rs.getInt("x");
                int sqly = rs.getInt("y");
                int sqlmobTime = rs.getInt("mobtime");
                AbstractLoadedMapleLife sqlmyLife = loadLife(sqlid, sqlf, sqlhide, sqlfh, sqlcy, sqlrx0, sqlrx1, sqlx, sqly, sqltype, mapid);
                if (sqltype.equals("n")) {
                    map.addMapObject(sqlmyLife);
                } else if (sqltype.equals("m")) {
                    MapleMonster monster = (MapleMonster) sqlmyLife;
                    map.addMonsterSpawn(monster, sqlmobTime, (byte) -1, null);
                }
            }
        } catch (SQLException e) {
            System.out.println("讀取SQL刷Npc和刷新怪物出錯.");
        }
        /*
         * load life data (npc, monsters)
         * 加載地圖刷新的NPC和怪物信息
         */
        List<Point> spawnPoints = new ArrayList<>();
        int lowestLevel = 200, highestLevel = 0;
        Map<Integer, Set<Integer>> hideinfo = ServerConfig.WORLD_HIDENPCS_MAP;
        for (MapleData life : mapData.getChildByPath("life")) {
            String type = MapleDataTool.getString(life.getChildByPath("type"), "n");
            String limited = MapleDataTool.getString("limitedname", life, "");
            if ((npcs || !type.equals("n")) && (limited.isEmpty() || !limited.contains(map.getMapMark()))) { // alien pq stuff
                String id = MapleDataTool.getString(life.getChildByPath("id"));
                AbstractLoadedMapleLife myLife = loadLife(life, id, type, mapid);
                if (myLife instanceof MapleMonster && !GameConstants.isNoSpawn(mapid)) {
                    MapleMonster mob = (MapleMonster) myLife;
                    int mobTime = MapleDataTool.getInt("mobTime", life, 7000);
                    Point spawnPosition = map.addMonsterSpawn(
                            mob,
                            mobTime, // 設置為 6.8 秒
                            (byte) MapleDataTool.getInt("team", life, -1),
                            mob.getId() == bossid ? msg : null
                    ).getPosition();
                    spawnPoints.add(spawnPosition);
                    if (mob.getStats().getLevel() > highestLevel && !mob.getStats().isBoss()) {
                        highestLevel = mob.getStats().getLevel();
                    }
                    if (mob.getStats().getLevel() < lowestLevel && !mob.getStats().isBoss()) {
                        lowestLevel = mob.getStats().getLevel();
                    }
                } else if (myLife instanceof MapleNPC) {
                    MapleNPC npc = (MapleNPC) myLife;
                    if (hideinfo.containsKey(-1) && hideinfo.get(-1).contains(npc.getId())) {
                        continue;
                    }
                    if (hideinfo.containsKey(mapid) && hideinfo.get(mapid).contains(npc.getId())) {
                        continue;
                    }
                    if (!limited.isEmpty() && !ServerConfig.WORLD_LIMITEDNAMES_LIST.contains(limited)) {
                        map.addHideNpc(npc.getId());
                        continue;
                    }

                    map.addMapObject(npc);
                }
            }
        }

        addAreaBossSpawn(map);
        map.setCreateMobInterval((short) 7000);
        map.setFixedMob(MapleDataTool.getInt(mapData.getChildByPath("info/fixedMobCapacity"), 0));
        map.setPartyBonusRate(GameConstants.getPartyPlay(mapid, MapleDataTool.getInt(mapData.getChildByPath("info/partyBonusR"), 0)));
        map.loadMonsterRate();
        map.setNodes(loadNodes(mapid, mapData));
        map.setSpawnPoints(spawnPoints);
        //load reactor data
        String id;
        if (reactors && mapData.getChildByPath("reactor") != null) {
            for (MapleData reactor : mapData.getChildByPath("reactor")) {
                id = MapleDataTool.getString(reactor.getChildByPath("id"));
                if (id != null) {
                    map.spawnReactor(loadReactor(reactor, id, (byte) MapleDataTool.getInt(reactor.getChildByPath("f"), 0)));
                }
            }
        }
        map.setClock(MapleDataTool.getInt(mapData.getChildByPath("info/clock"), 0) > 0);
        map.setEverlast(MapleDataTool.getInt(mapData.getChildByPath("info/everlast"), 0) > 0);
        map.setTown(MapleDataTool.getInt(mapData.getChildByPath("info/town"), 0) > 0);
        map.setSoaring(MapleDataTool.getInt(mapData.getChildByPath("info/needSkillForFly"), 0) > 0);
        map.setForceMove(MapleDataTool.getInt(mapData.getChildByPath("info/lvForceMove"), 0));
        map.setDecHP(MapleDataTool.getInt(mapData.getChildByPath("info/decHP"), 0));
        map.setDecHPr(MapleDataTool.getInt(mapData.getChildByPath("info/decHPr"), 0));
        map.setDecHPInterval(MapleDataTool.getInt(mapData.getChildByPath("info/decHPInterval"), 10000));
        map.setProtectItem(MapleDataTool.getInt(mapData.getChildByPath("info/protectItem"), 0));
        map.setForcedReturnMap(MapleDataTool.getInt(mapData.getChildByPath("info/forcedReturn"), 999999999));
        map.setTimeLimit(MapleDataTool.getInt(mapData.getChildByPath("info/timeLimit"), -1));
        map.setFieldLimit(MapleDataTool.getInt(mapData.getChildByPath("info/fieldLimit"), 0));
        map.setFirstUserEnter(MapleDataTool.getString(mapData.getChildByPath("info/onFirstUserEnter"), ""));
        map.setUserEnter(MapleDataTool.getString(mapData.getChildByPath("info/onUserEnter"), ""));
        map.setFieldScript(MapleDataTool.getString(mapData.getChildByPath("info/fieldScript"), ""));
        map.setMiniMapOnOff(MapleDataTool.getInt(mapData.getChildByPath("info/miniMapOnOff"), 0) > 0);
        map.setRecoveryRate(MapleDataTool.getFloat(mapData.getChildByPath("info/recovery"), 1));
        map.setConsumeItemCoolTime(MapleDataTool.getInt(mapData.getChildByPath("info/consumeItemCoolTime"), 0));
        map.setInstanceId(instanceid);
        lock.lock();
        try {
            instanceMap.put(instanceid, map);
        } finally {
            lock.unlock();
        }
        return map;
    }

    public int getLoadedMaps() {
        return maps.size();
    }

    public boolean isMapLoaded(int mapId) {
        return maps.containsKey(mapId);
    }

    public boolean isInstanceMapLoaded(int instanceid) {
        return instanceMap.containsKey(instanceid);
    }

    public void clearLoadedMap() {
        lock.lock();
        try {
            maps.clear();
        } finally {
            lock.unlock();
        }
    }

    public List<MapleMap> getAllLoadedMaps() {
        List<MapleMap> ret = new ArrayList<>();
        lock.lock();
        try {
            ret.addAll(maps.values());
            ret.addAll(instanceMap.values());
        } finally {
            lock.unlock();
        }
        return ret;
    }

    public Collection<MapleMap> getAllMaps() {
        return maps.values();
    }

    private AbstractLoadedMapleLife loadLife(MapleData life, String id, String type, int mapid) {
        AbstractLoadedMapleLife myLife = MapleLifeFactory.getLife(Integer.parseInt(id), type, mapid);
        if (myLife == null) {
            return null;
        }
        myLife.setCy(MapleDataTool.getInt(life.getChildByPath("cy")));
        MapleData dF = life.getChildByPath("f");
        if (dF != null) {
            myLife.setF(MapleDataTool.getInt(dF));
        }
        myLife.setCurrentFh(MapleDataTool.getInt(life.getChildByPath("fh")));
        myLife.setRx0(MapleDataTool.getInt(life.getChildByPath("rx0")));
        myLife.setRx1(MapleDataTool.getInt(life.getChildByPath("rx1")));
        myLife.setPosition(new Point(MapleDataTool.getIntConvert(life.getChildByPath("x")), MapleDataTool.getIntConvert(life.getChildByPath("y"))));

        if (MapleDataTool.getInt("hide", life, 0) == 1 && myLife instanceof MapleNPC) {
            myLife.setHide(true);
        }
        return myLife;
    }

    private AbstractLoadedMapleLife loadLife(int id, int f, boolean hide, int fh, int cy, int rx0, int rx1, int x, int y, String type, int mapid) {
        AbstractLoadedMapleLife myLife = MapleLifeFactory.getLife(id, type, mapid);
        if (myLife == null) {
            //System.out.println("加載npc錯誤，id：" + id + "  type：" + type);
            return null;
        }
        myLife.setCy(cy);
        myLife.setF(f);
        myLife.setCurrentFh(fh);
        myLife.setRx0(rx0);
        myLife.setRx1(rx1);
        myLife.setPosition(new Point(x, y));
        myLife.setHide(hide);
        return myLife;
    }

    private MapleReactor loadReactor(MapleData reactor, String id, byte FacingDirection) {
        MapleReactor myReactor = new MapleReactor(MapleReactorFactory.getReactor(Integer.parseInt(id)), Integer.parseInt(id));
        myReactor.setFacingDirection(FacingDirection);
        myReactor.setPosition(new Point(MapleDataTool.getInt(reactor.getChildByPath("x")), MapleDataTool.getInt(reactor.getChildByPath("y"))));
        myReactor.setDelay(MapleDataTool.getInt(reactor.getChildByPath("reactorTime")) * 1000);
        myReactor.setName(MapleDataTool.getString(reactor.getChildByPath("name"), ""));
        return myReactor;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    /*
     * 地圖設置刷新特殊怪物
     */
    private void addAreaBossSpawn(MapleMap map) {
        int monsterid = -1; //怪物的ID
        int mobtime = -1; //重新刷出怪物的時間 按秒計算
        String msg = null; //怪物刷出來的提示
        boolean shouldSpawn = true;
        boolean sendWorldMsg = false; //怪物出現時是全服公告提示
        Point pos1 = null, pos2 = null, pos3 = null; //怪物刷出的坐標

        switch (map.getId()) {
            case 104010200: // 維多利亞 - 小道2
                mobtime = 1200;
                monsterid = 2220000; // 紅蝸牛王
                msg = "天氣涼快了就會出現紅蝸牛王。";
                pos1 = new Point(189, 2);
                pos2 = new Point(478, 250);
                pos3 = new Point(611, 489);
                break;
            case 102020500: // 北部巖山 - 旋風地帶
                mobtime = 1200;
                monsterid = 3220000;
                msg = "樹妖王出現了。";
                pos1 = new Point(1121, 2130);
                pos2 = new Point(483, 2171);
                pos3 = new Point(1474, 1706);
                break;
            case 100020101: // 蘑菇之歌森林 - 蘑菇王小道
                mobtime = 1200;
                monsterid = 6130101;
                msg = "什麼地方出現了巨大的蘑菇。";
                pos1 = new Point(-311, 201);
                pos2 = new Point(-903, 197);
                pos3 = new Point(-568, 196);
                break;
            case 100020301: // 蘑菇之歌森林 - 藍蘑菇王森林
                mobtime = 1200;
                monsterid = 8220007;
                msg = "什麼地方出現了巨大的藍色蘑菇。";
                pos1 = new Point(-188, -657);
                pos2 = new Point(625, -660);
                pos3 = new Point(508, -648);
                break;
            case 105010301: // 蘑菇之歌森林 - 殭屍蘑菇王山丘
                mobtime = 1200;
                monsterid = 6300005;
                msg = "什麼地方出現了籠罩著陰暗氣息的巨大蘑菇。";
                pos1 = new Point(-130, -773);
                pos2 = new Point(504, -760);
                pos3 = new Point(608, -641);
                break;
            case 120030500: // 黃金海灘 - 溫暖的沙地
                mobtime = 1200;
                monsterid = 5220001;
                msg = "從沙灘裡慢慢的走出了一隻巨居蟹。";
                pos1 = new Point(-355, 179);
                pos2 = new Point(-1283, -113);
                pos3 = new Point(-571, -593);
                break;
            case 250010304: // 武陵 - 流浪熊的地盤
                mobtime = 2100;
                monsterid = 7220000;
                msg = "隨著微弱的口哨聲，肯德熊出現了。";
                pos1 = new Point(-210, 33);
                pos2 = new Point(-234, 393);
                pos3 = new Point(-654, 33);
                break;
            case 200010300: // 神秘島 - 天空樓梯Ⅱ
                mobtime = 1200;
                monsterid = 8220000;
                msg = "艾利傑出現了。";
                pos1 = new Point(665, 83);
                pos2 = new Point(672, -217);
                pos3 = new Point(-123, -217);
                break;
            case 250010503: // 武陵 - 妖怪森林
                mobtime = 1800;
                monsterid = 7220002;
                msg = "周邊的妖氣慢慢濃厚，可以聽到詭異的貓叫聲。";
                pos1 = new Point(-303, 543);
                pos2 = new Point(227, 543);
                pos3 = new Point(719, 543);
                break;
            case 222010310: // 隱藏地圖 - 月嶺
                mobtime = 2700;
                monsterid = 7220001;
                msg = "在陰暗的月光中隨著九尾狐的哭聲，可以感受到它陰氣。";
                pos1 = new Point(-169, -147);
                pos2 = new Point(-517, 93);
                pos3 = new Point(247, 93);
                break;
            case 103030400: // 沼澤地帶 - 深泥坑
                mobtime = 1800;
                monsterid = 6220000;
                msg = "從沼澤出現了巨大的多爾。";
                pos1 = new Point(-831, 109);
                pos2 = new Point(1525, -75);
                pos3 = new Point(-511, 107);
                break;
            case 101040300: // 詛咒森林 - 污染的樹
                mobtime = 1800;
                monsterid = 5220002;
                msg = "藍霧慢慢散去，浮士德慢慢的顯現了出來。";
                pos1 = new Point(600, -600);
                pos2 = new Point(600, -800);
                pos3 = new Point(600, -300);
                break;
            case 220050200: // 玩具城 - 丟失的時間<2>
                mobtime = 1500;
                monsterid = 5220003;
                msg = "嘀嗒嘀嗒! 隨著規則的指針聲出現了提莫。";
                pos1 = new Point(-467, 1032);
                pos2 = new Point(532, 1032);
                pos3 = new Point(-47, 1032);
                break;
            case 221040301: // 地球防禦本部 - 哥雷草原
                mobtime = 2400;
                monsterid = 6220001;
                msg = "厚重的機器運作聲，朱諾出現了!";
                pos1 = new Point(-4134, 416);
                pos2 = new Point(-4283, 776);
                pos3 = new Point(-3292, 776);
                break;
            case 240040401: // 隱藏地圖 - 大海獸 峽谷
                mobtime = 7200;
                monsterid = 8220003;
                msg = "大海獸出現了。";
                pos1 = new Point(-15, 2481);
                pos2 = new Point(127, 1634);
                pos3 = new Point(159, 1142);
                break;
            case 260010201: // 隱藏地圖 - 仙人掌爸爸沙漠
                mobtime = 3600;
                monsterid = 3220001;
                msg = "從沙塵中可以看到大宇的身影。";
                pos1 = new Point(-215, 275);
                pos2 = new Point(298, 275);
                pos3 = new Point(592, 275);
                break;
            case 251010102: // 百草堂 - 八十年藥草地
                mobtime = 3600;
                monsterid = 5220004;
                msg = "大王蜈蚣出現了。";
                pos1 = new Point(-41, 124);
                pos2 = new Point(-173, 126);
                pos3 = new Point(79, 118);
                break;
            case 261030000: // 隱藏地圖 - 研究所地下秘密通道
                mobtime = 2700;
                monsterid = 8220002;
                msg = "吉米拉出現了。";
                pos1 = new Point(-1094, -405);
                pos2 = new Point(-772, -116);
                pos3 = new Point(-108, 181);
                break;
            case 230020100: // 水下世界 - 海草之塔
                mobtime = 2700;
                monsterid = 4220000;
                msg = "在海草中間，出現了奇怪的蛤蚌。";
                pos1 = new Point(-291, -20);
                pos2 = new Point(-272, -500);
                pos3 = new Point(-462, 640);
                break;
            case 103020320: // 廢都地鐵 - 1號線第3區間
                mobtime = 1800;
                monsterid = 5090000;
                msg = "在地鐵的陰影中出現了什麼東西。";
                pos1 = new Point(79, 174);
                pos2 = new Point(-223, 296);
                pos3 = new Point(80, 275);
                break;
            case 103020420: // 廢都地鐵 - 2號線第3區間
                mobtime = 1800;
                monsterid = 5090000;
                msg = "在地鐵的陰影中出現了什麼東西。";
                pos1 = new Point(2241, 301);
                pos2 = new Point(1990, 301);
                pos3 = new Point(1684, 307);
                break;
            case 261020300: // 卡帕萊特研究所 - 研究所C-1 地區
                mobtime = 2700;
                monsterid = 7090000;
                msg = "自動警備系統出現了。";
                pos1 = new Point(312, 157);
                pos2 = new Point(539, 136);
                pos3 = new Point(760, 141);
                break;
            case 261020401: // 卡帕萊特研究所 - 禁止出入(除相關者外)
                mobtime = 2700;
                monsterid = 8090000;
                msg = "迪特和羅伊出現了。";
                pos1 = new Point(-263, 155);
                pos2 = new Point(-436, 122);
                pos3 = new Point(22, 144);
                break;
            case 250020300: // 武陵 - 上級修煉場
                mobtime = 2700;
                monsterid = 5090001;
                msg = "仙人玩偶出現了。";
                pos1 = new Point(1208, 27);
                pos2 = new Point(1654, 40);
                pos3 = new Point(927, -502);
                break;
            case 211050000: // 神秘島 - 寒冰平原
                mobtime = 2700;
                monsterid = 6090001;
                msg = "被束縛在冰裡的魔女睜開了眼睛。";
                pos1 = new Point(-233, -431);
                pos2 = new Point(-370, -426);
                pos3 = new Point(-526, -420);
                break;
            case 261010003: // 蒙特鳩研究所 - 研究所103號
                mobtime = 2700;
                monsterid = 6090004;
                msg = "陸陸貓出現了。";
                pos1 = new Point(-861, 301);
                pos2 = new Point(-703, 301);
                pos3 = new Point(-426, 287);
                break;
            case 222010300: // 童話村 - 狐狸山坡
                mobtime = 2700;
                monsterid = 6090003;
                msg = "書生鬼出現了。";
                pos1 = new Point(1300, -400);
                pos2 = new Point(1100, -100);
                pos3 = new Point(1100, 100);
                break;
            case 251010101: // 百草堂 - 六十年藥草地
                mobtime = 2700;
                monsterid = 6090002;
                msg = "竹林裡出現了一個來歷不明的青竹武士，只要打碎小竹片，就可讓青竹武士大發雷霆而葬失自制力，並將他打倒。";
                pos1 = new Point(-15, -449);
                pos2 = new Point(-114, -442);
                pos3 = new Point(-255, -446);
                break;
            case 211041400: // 神秘島 - 死亡之林Ⅳ
                mobtime = 2700;
                monsterid = 6090000;
                msg = "黑山老妖出現了！";
                pos1 = new Point(1672, 82);
                pos2 = new Point(2071, 10);
                pos3 = new Point(1417, 57);
                break;
            case 105030500: // 被詛咒的寺院 - 禁忌祭壇
                mobtime = 2700;
                monsterid = 8130100;
                msg = "巴洛古出現了。";
                pos1 = new Point(1275, -399);
                pos2 = new Point(1254, -412);
                pos3 = new Point(1058, -427);
                break;
            case 105020400: // 龍族洞穴 - 洞穴出口
                mobtime = 2700;
                monsterid = 8220008;
                msg = "出現了一個奇怪的商店。";
                pos1 = new Point(-163, 82);
                pos2 = new Point(958, 107);
                pos3 = new Point(706, -206);
                break;
            case 211040101: // 神秘島 - 雪人谷
                mobtime = 3600;
                monsterid = 8220001;
                msg = "馱狼雪人出現了。";
                pos1 = new Point(485, 244);
                pos2 = new Point(-60, 249);
                pos3 = new Point(208, 255);
                break;
            case 209000000: // 隱藏地圖 - 幸福村
                mobtime = 300;
                monsterid = 9500317;
                msg = "小雪人出現了。";
                pos1 = new Point(-115, 154);
                pos2 = new Point(-115, 154);
                pos3 = new Point(-115, 154);
                break;
            case 677000001: // 迷你地圖 - 牛魔王藏身之地
                mobtime = 60;
                monsterid = 9400612;
                msg = "牛魔王出現了。";
                pos1 = new Point(99, 60);
                pos2 = new Point(99, 60);
                pos3 = new Point(99, 60);
                break;
            case 677000003: // 迷你地圖 - 黑暗獨角獸藏身之地
                mobtime = 60;
                monsterid = 9400610;
                msg = "黑暗獨角獸出現了。";
                pos1 = new Point(6, 35);
                pos2 = new Point(6, 35);
                pos3 = new Point(6, 35);
                break;
            case 677000005: // 迷你地圖 - 印第安老斑鳩藏身之地
                mobtime = 60;
                monsterid = 9400609;
                msg = "印第安老斑鳩出現了。";
                pos1 = new Point(-277, 78);
                pos2 = new Point(547, 86);
                pos3 = new Point(-347, 80);
                break;
            case 677000007: // 迷你地圖 - 雪之貓女藏身之地
                mobtime = 60;
                monsterid = 9400611;
                msg = "雪之貓女出現了。";
                pos1 = new Point(117, 73);
                pos2 = new Point(117, 73);
                pos3 = new Point(117, 73);
                break;
            case 677000009: // 迷你地圖 - 沃勒福藏身之地
                mobtime = 60;
                monsterid = 9400613;
                msg = "沃勒福出現了。";
                pos1 = new Point(85, 66);
                pos2 = new Point(85, 66);
                pos3 = new Point(85, 66);
                break;
            case 931000500: // 秘密地圖 - 美洲豹棲息地
            case 931000502: // 秘密地圖 - 美洲豹棲息地
                mobtime = 3600; //2小時刷新
                monsterid = 9304005;
                msg = "美洲豹棲息地出現 劍齒豹 ，喜歡此坐騎的狂豹獵人職業可以前往抓捕。";
                pos1 = new Point(-872, -332);
                pos2 = new Point(409, -572);
                pos3 = new Point(-131, 0);
                shouldSpawn = false;
                sendWorldMsg = true;
                break;
            case 931000501: // 秘密地圖 - 美洲豹棲息地
            case 931000503: // 秘密地圖 - 美洲豹棲息地
                mobtime = 2 * 3600; //2小時刷新
                monsterid = 9304006;
                msg = "美洲豹棲息地出現 雪豹 ，喜歡此坐騎的狂豹獵人職業可以前往抓捕。";
                pos1 = new Point(-872, -332);
                pos2 = new Point(409, -572);
                pos3 = new Point(-131, 0);
                shouldSpawn = false;
                sendWorldMsg = true;
                break;
        }
        if (monsterid > 0) {
            map.addAreaMonsterSpawn(MapleLifeFactory.getMonster(monsterid), pos1, pos2, pos3, mobtime, msg, shouldSpawn, sendWorldMsg);
        }
    }

    private void loadPortals(MapleMap map, MapleData port) {
        if (port == null) {
            return;
        }
        int nextDoorPortal = 0x80;
        for (MapleData portal : port.getChildren()) {
            MaplePortal myPortal = new MaplePortal(MapleDataTool.getInt(portal.getChildByPath("pt")));
            myPortal.setName(MapleDataTool.getString(portal.getChildByPath("pn")));
            myPortal.setTarget(MapleDataTool.getString(portal.getChildByPath("tn")));
            myPortal.setTargetMapId(MapleDataTool.getInt(portal.getChildByPath("tm")));
            myPortal.setPosition(new Point(MapleDataTool.getInt(portal.getChildByPath("x")), MapleDataTool.getInt(portal.getChildByPath("y"))));
            String script = MapleDataTool.getString("script", portal, null);
            if (script != null && script.equals("")) {
                script = null;
            }
            myPortal.setScriptName(script);

            if (myPortal.getType() == MaplePortal.DOOR_PORTAL) {
                myPortal.setId(nextDoorPortal);
                nextDoorPortal++;
            } else {
                myPortal.setId(Integer.parseInt(portal.getName()));
            }
            map.addPortal(myPortal);
        }
    }

    private MapleNodes loadNodes(int mapid, MapleData mapData) {
        MapleNodes nodeInfo = new MapleNodes(mapid);
        if (mapData.getChildByPath("nodeInfo") != null) {
            for (MapleData node : mapData.getChildByPath("nodeInfo")) {
                try {
                    if (node.getName().equals("start")) {
                        nodeInfo.setNodeStart(MapleDataTool.getInt(node, 0));
                        continue;
                    }
                    List<Integer> edges = new ArrayList<>();
                    if (node.getChildByPath("edge") != null) {
                        for (MapleData edge : node.getChildByPath("edge")) {
                            edges.add(MapleDataTool.getInt(edge, -1));
                        }
                    }
                    MapleData stopInfoData = node.getChildByPath("stopInfo");
                    MapleNodeStopInfo stopInfo = null;
                    if (stopInfoData != null) {
                        final ArrayList<Pair<String, String>> list4 = new ArrayList<>();
                        final int b3 = MapleDataTool.getInt(stopInfoData.getChildByPath("stopDuration"), -1);
                        final String a = MapleDataTool.getString(stopInfoData.getChildByPath("scriptName"), "");
                        final int b4 = MapleDataTool.getInt(stopInfoData.getChildByPath("sayTic"), -1);
                        final int b5 = MapleDataTool.getInt(stopInfoData.getChildByPath("chatBalloon"), -1);
                        final boolean b6 = MapleDataTool.getInt(stopInfoData.getChildByPath("isWeather"), 0) > 0;
                        final boolean b7 = MapleDataTool.getInt(stopInfoData.getChildByPath("isRepeat"), 0) > 0;
                        final boolean b8 = MapleDataTool.getInt(stopInfoData.getChildByPath("isRandom"), 0) > 0;
                        MapleData sayInfoData = stopInfoData.getChildByPath("sayInfo");
                        if (sayInfoData != null) {
                            for (final MapleData zj4 : sayInfoData) {
                                list4.add(new Pair<>(MapleDataTool.getString(zj4.getChildByPath("say"), ""), MapleDataTool.getString(zj4.getChildByPath("say"), "")));
                            }
                        }
                        stopInfo = new MapleNodeStopInfo(a, b3, b4, b5, b6, b7, b8, list4);
                    }
                    MapleNodeInfo mni = new MapleNodeInfo(
                            Integer.parseInt(node.getName()),
                            MapleDataTool.getIntConvert("key", node, 0),
                            MapleDataTool.getIntConvert("x", node, 0),
                            MapleDataTool.getIntConvert("y", node, 0),
                            MapleDataTool.getIntConvert("attr", node, 0),
                            edges,
                            stopInfo
                    );
                    nodeInfo.addNode(mni);
                } catch (NumberFormatException ignored) {
                } //start, end, edgeInfo = we dont need it
            }
            nodeInfo.sortNodes();
        }
        for (int i = 1; i <= 7; i++) {
            if (mapData.getChildByPath(String.valueOf(i)) != null && mapData.getChildByPath(i + "/obj") != null) {
                for (MapleData node : mapData.getChildByPath(i + "/obj")) {
                    if (node.getChildByPath("SN_count") != null && node.getChildByPath("speed") != null) {
                        int sn_count = MapleDataTool.getIntConvert("SN_count", node, 0);
                        String name = MapleDataTool.getString("name", node, "");
                        int speed = MapleDataTool.getIntConvert("speed", node, 0);
                        if (sn_count <= 0 || speed <= 0 || name.equals("")) {
                            continue;
                        }
                        List<Integer> SN = new ArrayList<>();
                        for (int x = 0; x < sn_count; x++) {
                            SN.add(MapleDataTool.getIntConvert("SN" + x, node, 0));
                        }
                        MaplePlatform mni = new MaplePlatform(
                                name, MapleDataTool.getIntConvert("start", node, 2), speed,
                                MapleDataTool.getIntConvert("x1", node, 0),
                                MapleDataTool.getIntConvert("y1", node, 0),
                                MapleDataTool.getIntConvert("x2", node, 0),
                                MapleDataTool.getIntConvert("y2", node, 0),
                                MapleDataTool.getIntConvert("r", node, 0), SN);
                        nodeInfo.addPlatform(mni);
                    } else if (node.getChildByPath("tags") != null) {
                        String name = MapleDataTool.getString("tags", node, "");
                        nodeInfo.addFlag(new Pair<>(name, name.endsWith("3") ? 1 : 0)); //idk, no indication in wz
                    }
                }
            }
        }
        // load areas (EG PQ platforms)
        if (mapData.getChildByPath("area") != null) {
            int x1, y1, x2, y2;
            Rectangle mapArea;
            for (MapleData area : mapData.getChildByPath("area")) {
                x1 = MapleDataTool.getInt(area.getChildByPath("x1"));
                y1 = MapleDataTool.getInt(area.getChildByPath("y1"));
                x2 = MapleDataTool.getInt(area.getChildByPath("x2"));
                y2 = MapleDataTool.getInt(area.getChildByPath("y2"));
                mapArea = new Rectangle(x1, y1, (x2 - x1), (y2 - y1));
                nodeInfo.addMapleArea(mapArea);
            }
        }
        if (mapData.getChildByPath("CaptureTheFlag") != null) {
            MapleData mc = mapData.getChildByPath("CaptureTheFlag");
            for (MapleData area : mc) {
                nodeInfo.addGuardianSpawn(new Point(MapleDataTool.getInt(area.getChildByPath("FlagPositionX")), MapleDataTool.getInt(area.getChildByPath("FlagPositionY"))), area.getName().startsWith("Red") ? 0 : 1);
            }
        }
        if (mapData.getChildByPath("directionInfo") != null) {
            MapleData mc = mapData.getChildByPath("directionInfo");
            for (MapleData area : mc) {
                DirectionInfo di = new DirectionInfo(Integer.parseInt(area.getName()), MapleDataTool.getInt("x", area, 0), MapleDataTool.getInt("y", area, 0), MapleDataTool.getInt("forcedInput", area, 0) > 0);
                if (area.getChildByPath("EventQ") != null) {
                    for (MapleData event : area.getChildByPath("EventQ")) {
                        di.eventQ.add(MapleDataTool.getString(event));
                    }
                } else {
                    System.out.println("[loadNodes] 地圖: " + mapid + " 沒有找到EventQ.");
                }
                nodeInfo.addDirection(Integer.parseInt(area.getName()), di);
            }
        }
        if (mapData.getChildByPath("monsterCarnival") != null) {
            MapleData mc = mapData.getChildByPath("monsterCarnival");
            if (mc.getChildByPath("mobGenPos") != null) {
                for (MapleData area : mc.getChildByPath("mobGenPos")) {
                    nodeInfo.addMonsterPoint(MapleDataTool.getInt(area.getChildByPath("x")),
                            MapleDataTool.getInt(area.getChildByPath("y")),
                            MapleDataTool.getInt(area.getChildByPath("fh")),
                            MapleDataTool.getInt(area.getChildByPath("cy")),
                            MapleDataTool.getInt("team", area, -1));
                }
            }
            if (mc.getChildByPath("mob") != null) {
                for (MapleData area : mc.getChildByPath("mob")) {
                    nodeInfo.addMobSpawn(MapleDataTool.getInt(area.getChildByPath("id")), MapleDataTool.getInt(area.getChildByPath("spendCP")));
                }
            }
            if (mc.getChildByPath("guardianGenPos") != null) {
                for (MapleData area : mc.getChildByPath("guardianGenPos")) {
                    nodeInfo.addGuardianSpawn(new Point(MapleDataTool.getInt(area.getChildByPath("x")), MapleDataTool.getInt(area.getChildByPath("y"))), MapleDataTool.getInt("team", area, -1));
                }
            }
            if (mc.getChildByPath("skill") != null) {
                for (MapleData area : mc.getChildByPath("skill")) {
                    nodeInfo.addSkillId(MapleDataTool.getInt(area));
                }
            }
        }
        return nodeInfo;
    }
}
