package Server.cashshop;

import Config.configs.ServerConfig;
import Server.ServerType;
import Server.channel.PlayerStorage;
import Server.netty.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CashShopServer {

    private static final Logger log = LoggerFactory.getLogger(CashShopServer.class);
    private static ServerConnection init;
    private static PlayerStorage players;
    private static boolean finishedShutdown = false;
    private static short port;

    public static void run_startup_configurations() {
        port = ServerConfig.CASH_PORT;
        players = new PlayerStorage(-10);
        try {
            init = new ServerConnection(port, 0, -10, ServerType.CashShopServer);
            init.run();
            log.info("[CASH SHOP] Start CashShopServer。");
        } catch (final Exception e) {
            throw new RuntimeException("商城伺服器綁定連接埠 " + port + " 失敗", e);
        }
    }

    public static short getPort() {
        return port;
    }

    public static PlayerStorage getPlayerStorage() {
        return players;
    }

    public static int getConnectedClients() {
        return getPlayerStorage().getConnectedClients();
    }

    public static void shutdown() {
        if (finishedShutdown) {
            return;
        }
        log.info("正在關閉商城伺服器...");
        players.disconnectAll();
        log.info("商城伺服器解除連接埠綁定...");
        init.close();
        finishedShutdown = true;
    }

    public static boolean isShutdown() {
        return finishedShutdown;
    }

    public static String getCashBlockedMsg(int itemId) {
        switch (itemId) {
            case 5050000: //洗能力點卷軸
            case 5072000: //高質地喇叭
            case 5073000: //心臟高級喇叭
            case 5074000: //白骨高級喇叭
            case 5076000: //道具喇叭
            case 5077000: //繽紛喇叭
            case 5079001: //蛋糕高級喇叭
            case 5079002: //餡餅高級喇叭
            case 5390000: //熾熱情景喇叭
            case 5390001: //絢爛情景喇叭
            case 5390002: //愛心情景喇叭
            case 5390003: //新年慶祝喇叭1
            case 5390004: //新年慶祝喇叭2
            case 5390005: //小老虎情景喇叭
            case 5390006: //咆哮老虎情景喇叭
            case 5390007: //球進了!情景喇叭
            case 5390008: //世界盃情景喇叭
            case 5390010: //鬼出沒情景喇叭
            case 5060003: //花生機
            case 5360000: //雙倍爆率卡一天權
            case 5360014: //雙倍爆率卡三小時權
            case 5360015: //雙倍爆率卡一天權
            case 5360016: //雙倍爆率卡一周權
                return "該道具只能通過NPC購買.";
        }
        return "該道具禁止購買.";
    }
}
