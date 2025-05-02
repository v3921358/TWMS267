/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.login.handler;

import Client.MapleClient;
import Config.constants.ServerConstants;
import Packet.MaplePacketCreator;
import Server.channel.ChannelServer;
import Server.login.JobType;
import Server.login.LoginServer;
import Server.world.World;
import tools.data.MaplePacketReader;

/**
 * @author PlayDK
 */
public class CharSelectedHandler {

    private static boolean loginFailCount(MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 5;
    }

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        int charId = slea.readInt();
        if (c.getPlayer() != null) {
            c.getSession().close();
            return;
        }
        if (!c.isLoggedIn() || loginFailCount(c) || !c.login_Auth(charId)) {
            c.sendEnableActions();
            return;
        }
        if (ChannelServer.getInstance(c.getChannel()) == null || c.getWorldId() != 0) {
            c.getSession().close();
            return;
        }
        int job = c.getCharacterJob(charId);
        if (job > -1) {
            JobType jobt = JobType.getByJob(job);
            if (jobt == null || !ServerConstants.isOpenJob(jobt.name())) {
                c.dropMessage("該職業暫未開放,敬請期待!");
                c.sendEnableActions();
                return;
            }
        }
        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        //c.announce(MaplePacketCreator.getWzCheck(LoginServer.getWzCheckPack()));
        String ip = c.getSessionIPAddress();
        LoginServer.putLoginAuth(charId, ip.substring(ip.indexOf('/') + 1), c.getTempIP(), c.getChannel(), c.getMac());
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, ip);
        World.clearChannelChangeDataByAccountId(c.getAccID());
        // 官方機房
        c.announce(MaplePacketCreator.getServerIP(ChannelServer.getInstance(c.getChannel()).getPort(), charId));

        // 服務器
        c.announce(MaplePacketCreator.DummyGamaniaNat(ChannelServer.getInstance(c.getChannel()).getPort(), charId));
    }
}
