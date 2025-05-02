/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.login.handler;

import Client.MapleClient;
import Packet.LoginPacket;
import tools.data.MaplePacketReader;

/**
 * @author PlayDK
 */
public class ShowCharCards {

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        int accId = slea.readInt(); //帳號ID
        if (!c.isLoggedIn() || c.getAccID() != accId) {
            c.getSession().close();
            return;
        }
        c.announce(LoginPacket.showCharCards(c.getAccCardSlots()));
    }
}
