/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.login.handler;

import Client.MapleClient;
import Packet.LoginPacket;
import Packet.MaplePacketCreator;
import Server.channel.ChannelServer;
import Server.login.LoginServer;
import tools.data.MaplePacketReader;

/**
 * @author PlayDK
 */
public class WithSecondPasswordHandler {

    private static boolean loginFailCount(MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 5;
    }

    public static void handlePacket(MaplePacketReader slea, MapleClient c, boolean view) {
        String password = slea.readMapleAsciiString();
        int charId = slea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorldId(slea.readInt());
        }
        if (!c.isLoggedIn() || loginFailCount(c) || c.getSecondPassword() == null || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || c.getWorldId() != 0) { // TODOO: MULTI WORLDS
            c.getSession().close();
            return;
        }
        if (c.CheckSecondPassword(password) && password.length() >= 6 && password.length() <= 16) {
            if (c.getIdleTask() != null) {
                c.getIdleTask().cancel(true);
            }
            String s = c.getSessionIPAddress();
            LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1), c.getTempIP(), c.getChannel(), c.getMac());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
            // 官方機房
            c.announce(MaplePacketCreator.getServerIP(ChannelServer.getInstance(c.getChannel()).getPort(), charId));

            // 服務器
            c.announce(MaplePacketCreator.DummyGamaniaNat(ChannelServer.getInstance(c.getChannel()).getPort(), charId));
        } else {
            c.announce(LoginPacket.secondPwError((byte) 0x14));
        }
    }
}
