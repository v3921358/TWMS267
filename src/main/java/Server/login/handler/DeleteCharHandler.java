/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.login.handler;

import Client.MapleClient;
import Database.dao.AccountDao;
import Packet.LoginPacket;
import tools.data.MaplePacketReader;

/**
 * @author PlayDK
 */
public class DeleteCharHandler {

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        final String Secondpw_Client = slea.readMapleAsciiString();
        int chrId = slea.readInt();
        if (!c.login_Auth(chrId) || !c.isLoggedIn() || c.getSecondPassword() == null) {
            c.getSession().close();
            return;
        }
        if (c.CheckSecondPassword(Secondpw_Client)) {
            AccountDao.registerDeleteChr(c.getAccID(), c.getWorldId(), chrId);
            c.announce(LoginPacket.deleteReservedCharResponse(chrId, 0));
        } else {
            c.announce(LoginPacket.deleteReservedCharResponse(chrId, 20));
        }
    }

    public static void ReservedDeleteCharacterConfirm(MaplePacketReader slea, MapleClient c) {
        int chrId = slea.readInt();
        final String Secondpw_Client = slea.readMapleAsciiString();
        if (!c.login_Auth(chrId) || !c.isLoggedIn() || c.getSecondPassword() == null) {
            c.getSession().close();
            return;
        }
        if (c.CheckSecondPassword(Secondpw_Client)) {
            AccountDao.deregisterDeleteChr(c.getAccID(), c.getWorldId(), chrId);
            int state = AccountDao.deleteCharacter(c.getAccID(), chrId);
            c.announce(LoginPacket.deleteCharResponse(chrId, state));
        } else {
            c.announce(LoginPacket.deleteCharResponse(chrId, 20));
        }
    }

    public static void ReservedDeleteCharacterCancel(MaplePacketReader slea, MapleClient c) {
        int chrId = slea.readInt();
        if (!c.login_Auth(chrId) || !c.isLoggedIn()) {
            c.getSession().close();
            return; // Attempting to delete other character
        }
        AccountDao.deregisterDeleteChr(c.getAccID(), c.getWorldId(), chrId);
        c.announce(LoginPacket.ReservedDeleteCharacterCancelResult(chrId));
    }
}
