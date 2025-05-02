package Net.server;

import Client.force.MapleForceFactory;
import Client.hexa.HexaFactory;
import Config.constants.ServerConstants;
import Database.DatabaseException;
import Database.DatabaseLoader.DatabaseConnection;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Database.tools.SqlTool;
import Net.server.cashshop.CashItemFactory;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonsterInformationProvider;
import Net.server.life.MobSkillFactory;
import Net.server.maps.MapleMapFactory;
import Net.server.maps.field.ActionBarField;
import Net.server.maps.field.BossLucidField;
import Net.server.maps.field.BossWillField;
import Net.server.quest.MapleQuestDumper;
import Net.server.shop.MapleShopFactory;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.loaders.SkillData;
import Plugin.provider.loaders.StringData;
import Plugin.script.ReactorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class InitializeServer {
    private static final Logger log = LoggerFactory.getLogger(InitializeServer.class);
    private static final ExecutorService executor = Executors.newWorkStealingPool(2);
    private static final String LOG_TABLE_NAME = "systemupdatelog";

    public static boolean initServer() {
        CompletableFuture<Boolean> initSettingFuture = CompletableFuture.supplyAsync(InitializeServer::initializeSetting, executor);
        CompletableFuture<Boolean> initUpdateLogFuture = CompletableFuture.supplyAsync(InitializeServer::initializeUpdateLog, executor);
        CompletableFuture<Boolean> initMySQLFuture = CompletableFuture.supplyAsync(InitializeServer::initializeMySQL, executor);

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(initSettingFuture, initUpdateLogFuture, initMySQLFuture);
        combinedFuture.join();
        initSettingFuture.join();
        initUpdateLogFuture.join();
        initMySQLFuture.join();
        executor.shutdown();
        return true;
    }

    private static boolean executeSql(String sql, Object... params) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            log.error("[EXCEPTION] Please check if the SQL server is active.", ex);
            return false;
        }
    }

    private static boolean initializeSetting() {
        return executeSql("UPDATE `accounts` SET `loggedin` = 0, `check` = 0");
    }

    private static boolean initializeUpdateLog() {
        if (!checkTableExists(LOG_TABLE_NAME)) {
            createTable(LOG_TABLE_NAME, "id INT(11) NOT NULL AUTO_INCREMENT, patchname VARCHAR(50) NOT NULL, "
                    + "lasttime TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP, PRIMARY KEY (id)");
        }
        return checkTableExists(LOG_TABLE_NAME);
    }

    private static boolean checkTableExists(String tableName) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("SHOW TABLES LIKE ?")) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Error checking table existence: " + tableName, e);
            return false;
        }
    }

    private static void createTable(String tableName, String tableDefinition) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableDefinition + ")")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error creating table: " + tableName, e);
        }
    }

    private static boolean checkTableExists_wz() {
        return DatabaseConnection.domain(con -> {
            boolean exist = false;
            try (PreparedStatement ps = con.prepareStatement("SHOW TABLES LIKE 'wztosqllog'");
                 ResultSet rs = ps.executeQuery()) {
                exist = rs.next();
            } catch (SQLException e) {
                log.error("Error checking wztosqllog table existence.", e);
            }
            if (!exist) {
                SqlTool.update(con, "DROP TABLE IF EXISTS `wztosqllog`");
                StringBuilder s = new StringBuilder("CREATE TABLE `wztosqllog` (")
                        .append("`version` SMALLINT NOT NULL, ")
                        .append("`hotfix_check` VARCHAR(40) NULL DEFAULT NULL, ");
                for (String name : WzSqlName.names()) {
                    s.append("`").append(name).append("` BOOLEAN NOT NULL, ");
                }
                s.append("PRIMARY KEY (`version`))");
                SqlTool.update(con, s.toString());
            }
            return exist;
        });
    }

    private static boolean initializeMySQL() {
        boolean allSuccess = true;
        for (UPDATE_PATCH patch : UPDATE_PATCH.values()) {
            if (!checkIsAppliedSQLPatch(patch.name())
                    && (!applySQLPatch(patch.getSQL()) || !insertUpdateLog(patch.name()))) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    private static boolean checkIsAppliedSQLPatch(String name) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id FROM systemupdatelog WHERE patchname = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            } catch (Exception ex) {
                SqlTool.update(con, "DROP TABLE IF EXISTS `systemupdatelog`");
                initializeUpdateLog();
                return checkIsAppliedSQLPatch(name);
            }
        } catch (SQLException e) {
            log.error("Error checking SQL patch: " + name, e);
        }
        return false;
    }

    private static boolean insertUpdateLog(String patchname) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO systemupdatelog(id, patchname, lasttime) VALUES (DEFAULT, ?, CURRENT_TIMESTAMP)")) {
            ps.setString(1, patchname);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            log.error("Error inserting update log: " + patchname, ex);
            return false;
        }
    }

    private static boolean applySQLPatch(String sql) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            log.error("Error applying SQL patch: " + sql, e);
            return false;
        }
    }

    public static boolean InitDataFinished = false;

    /**
     * 初始化所有資料 (共 23 項)。
     *
     * @param listener 每完成一項就會呼叫一次 listener.next(now, total)
     */
    public static void initAllData(DataCacheListener listener) {
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger total = new AtomicInteger(0);
        ExecutorService executor = Executors.newWorkStealingPool();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Task 1
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleItemInformationProvider.getInstance().runItems();
            } catch (Exception e) {
                log.error("[TASK] runItems() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 2
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                StringData.load();
            } catch (Exception e) {
                log.error("[TASK] StringData.load() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 3
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleItemInformationProvider.getInstance().loadSetItemData();
            } catch (Exception e) {
                log.error("[TASK] loadSetItemData() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 4
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleItemInformationProvider.getInstance().loadFamiliarItems();
            } catch (Exception e) {
                log.error("[TASK] loadFamiliarItems() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 5
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleItemInformationProvider.getInstance().runEtc();
            } catch (Exception e) {
                log.error("[TASK] runEtc() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 6
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleForceFactory.getInstance().initialize();
            } catch (Exception e) {
                log.error("[TASK] MapleForceFactory.initialize() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 7
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleItemInformationProvider.getInstance().loadPotentialData();
            } catch (Exception e) {
                log.error("[TASK] loadPotentialData() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 8
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleShopFactory.getInstance().loadShopData();
            } catch (Exception e) {
                log.error("[TASK] MapleShopFactory.loadShopData() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 9
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MobSkillFactory.initialize();
            } catch (Exception e) {
                log.error("[TASK] MobSkillFactory.initialize() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 10
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleUnionData.getInstance().init();
            } catch (Exception e) {
                log.error("[TASK] MapleUnionData.init() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 11
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleMapFactory.loadAllLinkNpc();
            } catch (Exception e) {
                log.error("[TASK] MapleMapFactory.loadAllLinkNpc() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 12
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleMapFactory.loadAllMapName();
            } catch (Exception e) {
                log.error("[TASK] MapleMapFactory.loadAllMapName() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 13
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                CashItemFactory.getInstance().initialize();
            } catch (Exception e) {
                log.error("[TASK] CashItemFactory.initialize() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 14
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleLifeFactory.initEliteMonster();
            } catch (Exception e) {
                log.error("[TASK] MapleLifeFactory.initEliteMonster() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 15
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleMonsterInformationProvider.getInstance().load();
            } catch (Exception e) {
                log.error("[TASK] MapleMonsterInformationProvider.load() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 16
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                ReactorManager.getInstance().loadDrops();
            } catch (Exception e) {
                log.error("[TASK] ReactorManager.loadDrops() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 17
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleLifeFactory.loadQuestCounts();
            } catch (Exception e) {
                log.error("[TASK] MapleLifeFactory.loadQuestCounts() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 18
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                MapleQuestDumper.getInstance().loadQuest();
            } catch (Exception e) {
                log.error("[TASK] MapleQuestDumper.loadQuest() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 19
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                BossWillField.init();
            } catch (Exception e) {
                log.error("[TASK] BossWillField.init() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 20
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                BossLucidField.init();
            } catch (Exception e) {
                log.error("[TASK] BossLucidField.init() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 21
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                ActionBarField.init();
            } catch (Exception e) {
                log.error("[TASK] ActionBarField.init() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 22
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                HexaFactory.loadAllHexaSkill();
            } catch (Exception e) {
                log.error("[TASK] HexaFactory.loadAllHexaSkill() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        // Task 23
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                SkillData.loadAllSkills();
            } catch (Exception e) {
                log.error("[TASK] SkillData.loadAllSkills() error:", e);
            } finally {
                listener.next(count.incrementAndGet(), total);
            }
        }, executor));

        total.set(futures.size());
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.whenComplete((result, error) -> {
            if (error != null) {
                log.error("One or more tasks completed exceptionally: ", error);
            } else {
                log.info("All 23 tasks completed successfully.");
            }
            executor.shutdown();
        });
    }

    /**
     * Listener 介面，用於在每完成一項任務時更新進度。
     * 例如：listener.next(5, total) => "5 / 23"
     */
    @FunctionalInterface
    public interface DataCacheListener {
        void next(int now, AtomicInteger total);
    }

    /**
     * 補丁列表
     */
    enum UPDATE_PATCH {
        真實符文屬性("ALTER TABLE `inventoryequipment` ADD COLUMN `aut` smallint(6) NOT NULL DEFAULT 0 AFTER `arclevel`"
                + ", ADD COLUMN `autexp` int(6) NOT NULL DEFAULT 0 AFTER `aut`"
                + ", ADD COLUMN `autlevel` smallint(6) NOT NULL DEFAULT 0 AFTER `autexp`"),
        寵物第二BUFF欄位("ALTER TABLE `pets` ADD COLUMN `skillid2` int(11) NOT NULL DEFAULT '0' AFTER `skillid`"),
        公會V240擴充職位欄位("ALTER TABLE `guilds` MODIFY COLUMN `rank1authority` int(10) NOT NULL DEFAULT -1 AFTER `rank5title`"
                + ", MODIFY COLUMN `rank2authority` int(10) NOT NULL DEFAULT 1663 AFTER `rank1authority`"
                + ", MODIFY COLUMN `rank3authority` int(10) NOT NULL DEFAULT 1024 AFTER `rank2authority`"
                + ", MODIFY COLUMN `rank4authority` int(10) NOT NULL DEFAULT 1024 AFTER `rank3authority`"
                + ", MODIFY COLUMN `rank5authority` int(10) NOT NULL DEFAULT 1024 AFTER `rank4authority`"
                + ", ADD COLUMN `rank6title` varchar(45) NOT NULL AFTER `rank5title`"
                + ", ADD COLUMN `rank7title` varchar(45) NOT NULL AFTER `rank6title`"
                + ", ADD COLUMN `rank8title` varchar(45) NOT NULL AFTER `rank7title`"
                + ", ADD COLUMN `rank9title` varchar(45) NOT NULL AFTER `rank8title`"
                + ", ADD COLUMN `rank10title` varchar(45) NOT NULL AFTER `rank9title`"
                + ", ADD COLUMN `rank6authority` int(10) NOT NULL DEFAULT '1024' AFTER `rank5authority`"
                + ", ADD COLUMN `rank7authority` int(10) NOT NULL DEFAULT '1024' AFTER `rank6authority`"
                + ", ADD COLUMN `rank8authority` int(10) NOT NULL DEFAULT '1024' AFTER `rank7authority`"
                + ", ADD COLUMN `rank9authority` int(10) NOT NULL DEFAULT '1024' AFTER `rank8authority`"
                + ", ADD COLUMN `rank10authority` int(10) NOT NULL DEFAULT '1024' AFTER `rank9authority`"),
        移除商城道具extra_flags屬性("ALTER TABLE `cashshop_modified_items` DROP COLUMN `extra_flags`"),
        公會職位預設值("ALTER TABLE `guilds` MODIFY COLUMN `rank6title` varchar(45) NOT NULL DEFAULT '公會成員4' AFTER `rank5title`"
                + ", MODIFY COLUMN `rank7title` varchar(45) NOT NULL DEFAULT '' AFTER `rank6title`"
                + ", MODIFY COLUMN `rank8title` varchar(45) NOT NULL DEFAULT '' AFTER `rank7title`"
                + ", MODIFY COLUMN `rank9title` varchar(45) NOT NULL DEFAULT '' AFTER `rank8title`"
                + ", MODIFY COLUMN `rank10title` varchar(45) NOT NULL DEFAULT '' AFTER `rank9title`"),
        鍵位欄位擴充補丁("ALTER TABLE `keymap` ADD COLUMN `slot` tinyint(3) unsigned NOT NULL DEFAULT 0 AFTER `characterid`"),
        移除pokemon和monsterbook表("DROP TABLE IF EXISTS `pokemon`, `monsterbook`"),
        寵物Buff欄屬性("ALTER TABLE `pets` DROP COLUMN `skillid`, DROP COLUMN `skillid2`"),
        移除extendedslots表("DROP TABLE IF EXISTS `extendedslots`"),
        道具新增extendedSlot屬性("ALTER TABLE `inventoryitems` ADD COLUMN `extendSlot` int(11) NOT NULL DEFAULT -1 AFTER `espos`"),
        增加傳授次數屬性("ALTER TABLE `skills` ADD COLUMN `teachTimes` int(11) NOT NULL DEFAULT 0 AFTER `teachId`"),
        極限屬性欄位("CREATE TABLE IF NOT EXISTS `hyperstats` (`id` int(11) NOT NULL AUTO_INCREMENT, `charid` int(11) NOT NULL, `position` int(11) NOT NULL, `skillid` int(11) NOT NULL, `skilllevel` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8"),
        移除character_coreauras表("DROP TABLE IF EXISTS `character_coreauras`"),
        移除角色髮型混色欄位("ALTER TABLE `characters` DROP COLUMN `basecolor`, DROP COLUMN `mixedcolor`, DROP COLUMN `probcolor`"),
        移除梳化間混色欄位("ALTER TABLE `salon` DROP COLUMN `basecolor`, DROP COLUMN `mixedcolor`, DROP COLUMN `probcolor`"),
        萌獸增加鎖定欄位("ALTER TABLE `familiars` ADD COLUMN `lock` tinyint(1) NOT NULL DEFAULT 0 AFTER `summon`"),
        增加HEXA屬性表("CREATE TABLE IF NOT EXISTS `sixstats` (`id` int(11) NOT NULL AUTO_INCREMENT, `charid` int(11) NOT NULL, `solt` int(11) NOT NULL, `preset` int(11) NOT NULL, `p0stat1` int(11) NOT NULL, `p0stat1lv` int(11) NOT NULL, `p0stat2` int(11) NOT NULL, `p0stat2lv` int(11) NOT NULL, `p0stat3` int(11) NOT NULL, `p0stat3lv` int(11) NOT NULL, `p1stat1` int(11) NOT NULL, `p1stat1lv` int(11) NOT NULL, `p1stat2` int(11) NOT NULL, `p1stat2lv` int(11) NOT NULL, `p1stat3` int(11) NOT NULL, `p1stat3lv` int(11) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        增加HEXA技能表("CREATE TABLE IF NOT EXISTS `hexaskills` (`sid` int(11) NOT NULL AUTO_INCREMENT, `id` int(11) NOT NULL, `charid` int(11) NOT NULL, `skilllv` int(11) NOT NULL, PRIMARY KEY (`sid`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

        private final String sql;

        UPDATE_PATCH(String sql) {
            this.sql = sql;
        }

        public String getSQL() {
            return sql;
        }
    }

    public enum WzSqlName {
        wz_delays,
        wz_skilldata,
        wz_skillsbyjob,
        wz_summonskill,
        wz_mountids,
        wz_familiarskill,
        wz_craftings,
        wz_finalattacks,
        wz_itemdata,
        wz_maplinknpcs,
        wz_questdata,
        wz_questactitemdata,
        wz_questactskilldata,
        wz_questactquestdata,
        wz_questreqdata,
        wz_questpartydata,
        wz_questactdata,
        wz_questcount,
        wz_npcnames,
        wz_mobskilldata;

        static String[] names() {
            WzSqlName[] values = values();
            String[] names = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                names[i] = values[i].name();
            }
            return names;
        }

        public boolean check(Connection con) {
            synchronized (WzSqlName.class) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `wztosqllog` WHERE `version` = ?")) {
                    String hfc = MapleDataProviderFactory.getHotfixCheck();
                    ps.setInt(1, ServerConstants.MapleMajor);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            if (Objects.equals(rs.getString("hotfix_check"), hfc)) {
                                return rs.getBoolean(this.name());
                            } else {
                                SqlTool.update(con, "DELETE FROM `wztosqllog` WHERE `version` = " + ServerConstants.MapleMajor);
                            }
                        }
                    } catch (Exception e) {
                        SqlTool.update(con, "DROP TABLE IF EXISTS `wztosqllog`");
                        checkTableExists_wz();
                    }
                    StringBuilder s = new StringBuilder("INSERT INTO `wztosqllog` VALUES(")
                            .append(ServerConstants.MapleMajor).append(",")
                            .append(hfc == null ? "NULL" : "\"" + hfc + "\"");
                    for (String name : names()) {
                        s.append(",false");
                    }
                    try {
                        SqlTool.update(con, s.append(")").toString());
                    } catch (DatabaseException e) {
                        SqlTool.update(con, "DROP TABLE IF EXISTS `wztosqllog`");
                    }
                    return false;
                } catch (SQLException e) {
                    throw new DatabaseException(e);
                }
            }
        }

        public synchronized void update(Connection con) {
            SqlTool.update(con, "UPDATE `wztosqllog` SET `" + name() + "` = ? WHERE `version` = ?", true, ServerConstants.MapleMajor);
        }

        public synchronized void drop(Connection con) {
            SqlTool.update(con, "DROP TABLE IF EXISTS `" + name() + "`");
        }
    }
}
