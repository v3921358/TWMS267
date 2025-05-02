package Server.login;

import Client.MapleClient;
import Client.MapleEnumClass;
import Config.configs.ServerConfig;
import Handler.Login.LoginHandler;
import Net.server.Timer.PingTimer;
import Packet.LoginPacket;
import Packet.MaplePacketCreator;
import Server.channel.ChannelServer;
import connection.packet.Login;
import SwordieX.enums.LoginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;

public class LoginWorker {

    private static final Logger log = LoggerFactory.getLogger(LoginWorker.class);
    private static long lastUpdate = 0;

    public static void registerClient(final MapleClient c, boolean useKey) {
        if (ServerConfig.WORLD_ONLYADMIN && !c.isGm() && !c.isLocalhost()) {
            c.announce(MaplePacketCreator.serverNotice(1, "服務器維護中."));
            c.announce(LoginPacket.getLoginFailed(MapleEnumClass.AuthReply.GAME_DEFINITION_INFO));
            return;
        }
        if (System.currentTimeMillis() - lastUpdate > 600000) { // Update once every 10 minutes
            lastUpdate = System.currentTimeMillis();
            Map<Integer, Integer> load = ChannelServer.getChannelLoad();
            int usersOn = 0;
            if (load == null || load.size() <= 0) { // In an unfortunate event that Client.client logged in before load
                lastUpdate = 0;
                c.announce(LoginPacket.getLoginFailed(MapleEnumClass.AuthReply.GAME_CONNECTING_ACCOUNT));
                return;
            }
            /* 這個5決定是限制人數的數量 */
            double loadFactor = 32766 / ((double) LoginServer.getUserLimit() / load.size() / 100);
            for (Entry<Integer, Integer> entry : load.entrySet()) {
                usersOn += entry.getValue();
                /* 這個5決定是限制人數的數量 */
                load.put(entry.getKey(), Math.min(32766, (int) (entry.getValue() * loadFactor)));
            }
            LoginServer.setLoad(load, usersOn);
            lastUpdate = System.currentTimeMillis();
        }
        if (c.finishLogin() == 0) {
            if (useKey) {
                LoginHandler.handleLogoutWorld(c, null);
                c.write(Login.sendCheckPasswordResult(LoginType.Success, c, true));
            } else {
                c.write(Login.sendCheckPasswordResult(LoginType.Success, c, false));
                LoginHandler.handleLogoutWorld(c, null);
                LoginHandler.handleWorldListRequest(c, null);
            }

            c.setIdleTask(PingTimer.getInstance().schedule(c.getSession()::close, 10 * 60 * 10000));
        } else {
            c.announce(LoginPacket.getLoginFailed(MapleEnumClass.AuthReply.GAME_CONNECTING_ACCOUNT));
        }
    }
}
