// 請依照您的專案結構設定正確的 package 名稱
package Net;

import Client.inventory.MapleInventoryIdentifier;
import Client.skills.SkillFactory;
import Config.configs.Config;
import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Config.constants.ServerConstants;
import Net.server.Timer;
import Net.server.*;
import Net.server.carnival.MapleCarnivalFactory;
import Net.server.cashshop.CashItemFactory;
import Net.server.life.MapleMonsterInformationProvider;
import Net.server.life.PlayerNPC;
import Net.server.shop.MapleShopFactory;
import Opcode.Headler.InHeader;
import Opcode.Headler.OutHeader;
import Plugin.provider.loaders.CashData;
import Plugin.provider.loaders.SkillData;
import Plugin.script.ReactorManager;
import Server.auction.AuctionServer;
import Server.cashshop.CashShopServer;
import Server.channel.ChannelServer;
import Server.login.LoginInformationProvider;
import Server.login.LoginServer;
import Server.world.WorldRespawnService;
import Server.world.guild.MapleGuild;
import SwordieX.client.User;
import connection.crypto.MapleCrypto;
import connection.netty.ChannelHandler;
import SwordieX.enums.WorldId;
import SwordieX.util.Util;
import SwordieX.world.World;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
@Slf4j
public class NetRun {
    private static final LocalDateTime loadStartTime = LocalDateTime.now();
    public static class Server extends Properties {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Server.class);
        @Getter
        @Setter
        private boolean online = false;

        private static final Server server = new Server();
        private static final Set<Integer> users = new HashSet<>();
        private static final List<World> worldList = new ArrayList<>();

        public static Server getInstance() {
            return server;
        }

        public List<World> getWorlds() {
            return worldList;
        }

        public World getWorldById(int id) {
            return Util.findWithPred(getWorlds(), w -> w.getWorldId().getVal() == id);
        }

        public void addUser(User user) {
            users.add(user.getId());
        }

        public void removeUser(User user) {
            users.remove(user.getId());
        }

        public boolean isUserLoggedIn(User user) {
            return users.contains(user.getId());
        }

        public static void init(String[] args) {
            System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
            ChannelHandler.initHandlers(false);
            MapleCrypto.initialize(ServerConstants.MapleMajor);
            worldList.add(new World(
                    WorldId.getByVal(ServerConfig.WORLD_ID),
                    ServerConfig.SERVER_NAME,
                    ServerConfig.CHANNELS_PER_WORLD,
                    ServerConfig.LOGIN_EVENTMESSAGE
            ));
        }

        public static void main(String[] args) {
            init(args);
        }
    }
    public static void main(String[] args) throws Exception {
        Config.load();
        log.info("啟動遊戲版本:TMS-268..");
        Timer.startAll();
        Server.main(args);
        Server.getInstance().setOnline(true);
        LoginServer.setOn();
        OutHeader.startCheck();
        log.info("OutHeader包頭反射已啟動，等待連接中。");
        InHeader.startCheck();
        log.info("InHeader包頭反射已啟動，等待連接中。");
        boolean dbInitOK = InitializeServer.initServer();
        if (!dbInitOK) {
            log.error("服務器端口初始化錯誤。");
            return;
        }
        List<CompletableFuture<Void>> loadTasks = new ArrayList<>();
        CompletableFuture<Void> initAllDataFuture = CompletableFuture.runAsync(() -> {
            try {
                InitializeServer.initAllData((now, total) -> {
                    if (total.get() > 0) {
                        log.info("[初始化伺服器] 已加載 {} / {}", now, total.get());
                    }
                    if (now == total.get()) {
                        log.info("[初始化伺服器] initAllData() 所有任務已完成。");
                    }
                });
            } catch (Exception e) {
                log.error("[初始化伺服器] initAllData() 發生錯誤：", e);
            }
        });
        loadTasks.add(initAllDataFuture);

        // 其他非同步加載任務
        loadTasks.add(CompletableFuture.runAsync(MapleOverrideData::init));
        loadTasks.add(CompletableFuture.runAsync(() -> ShutdownServer.getInstance().setShutdown(false)));
        loadTasks.add(CompletableFuture.runAsync(MapleDailyGift::initialize));
        loadTasks.add(CompletableFuture.runAsync(CashShopServer::run_startup_configurations));
        loadTasks.add(CompletableFuture.runAsync(AuctionServer.getInstance()::init));
        loadTasks.add(CompletableFuture.runAsync(MapleGuild::loadAll));
        loadTasks.add(CompletableFuture.runAsync(CashData::loadCashCommodities));
        loadTasks.add(CompletableFuture.runAsync(CashData::loadCashOldCommodities));
        loadTasks.add(CompletableFuture.runAsync(CashData::loadCashPackages));
        loadTasks.add(CompletableFuture.runAsync(CashItemFactory::getInstance));
        loadTasks.add(CompletableFuture.runAsync(LoginInformationProvider::getInstance));
        loadTasks.add(CompletableFuture.runAsync(CharacterCardFactory.getInstance()::initialize));
        loadTasks.add(CompletableFuture.runAsync(MTSStorage::load));
        loadTasks.add(CompletableFuture.runAsync(PredictCardFactory::getInstance));
        loadTasks.add(CompletableFuture.runAsync(MapleInventoryIdentifier::getInstance));
        loadTasks.add(CompletableFuture.runAsync(PlayerNPC::loadAll));
        loadTasks.add(CompletableFuture.runAsync(MapleCarnivalFactory::getInstance));
        loadTasks.add(CompletableFuture.runAsync(ItemConstants.TapJoyReward::init));
        loadTasks.add(CompletableFuture.runAsync(SkillFactory::loadSkillData));
        loadTasks.add(CompletableFuture.runAsync(SkillFactory::loadDelays));
        loadTasks.add(CompletableFuture.runAsync(SkillFactory::loadMemorySkills));
        loadTasks.add(CompletableFuture.runAsync(SkillFactory::getAllSkills));
        loadTasks.add(CompletableFuture.runAsync(SkillData::loadAllSkills));
        loadTasks.add(CompletableFuture.runAsync(LoginServer::runStartupConfigurations));

        // 等待所有非同步任務完成
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(loadTasks.toArray(new CompletableFuture[0]));
        try {
            combinedFuture.join();
        } catch (Exception e) {
            log.error("數據加載任務遇到錯誤，服務器啟動失敗。", e);
            System.exit(1);
        }
        log.info("所有數據加載任務均已完成。總共耗時：{} 秒。",
                Duration.between(loadStartTime, LocalDateTime.now()).toSeconds());

        // 啟動頻道伺服器
        ChannelServer.startChannel_Main();
        log.info("頻道服務器已啟動，等待連接中。");

        // 啟動怪物重生系統
        WorldRespawnService.getInstance();
        log.info("怪物重生系統已啟動，等待執行中。");

        // 清除反應堆掉落、商城與怪物的掉落資料
        ReactorManager.getInstance().clearDrops();
        log.info("反應堆掉落系統已清除掉落資料。");
        MapleShopFactory.getInstance().clear();
        MapleMonsterInformationProvider.getInstance().clearDrops();
        Timer.CheatTimer.getInstance().register(AutobanManager.getInstance(), 60000);
        log.info("服務器啟動完成，等待連接中。");
    }

}
