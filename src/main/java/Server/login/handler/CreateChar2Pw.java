/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.login.handler;

import Client.MapleClient;
import Config.configs.Config;
import Packet.LoginPacket;
import tools.CheckCodeImageCreator;
import tools.data.MaplePacketReader;

/**
 * @author PlayDK
 */
public class CreateChar2Pw {

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        final String Secondpw_Client = slea.readMapleAsciiString();
        if (!c.isLoggedIn() || loginFailCount(c) || c.getSecondPassword() == null) {
            c.getSession().close();
            System.err.println("伺服器主動斷開用戶端連結,調用位置: " + new Throwable().getStackTrace()[0]);
            return;
        }
        if (Config.isDevelop() || c.CheckSecondPassword(Secondpw_Client)) {
            if (false) //TODO: 是否開啟圖片驗證
            {
                String code = CheckCodeImageCreator.getRandCode(); //TODO: 還需要暫存驗證碼
                c.announce(LoginPacket.createCharResponse(69, CheckCodeImageCreator.createImage(code)));
            } else {
                c.announce(LoginPacket.createCharResponse(0, new byte[]{}));
            }

        } else {
            c.announce(LoginPacket.createCharResponse(20, new byte[]{}));
        }
    }

    private static boolean loginFailCount(MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 5;
    }
}
