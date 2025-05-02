/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.login.handler;

import Client.MapleCharacterUtil;
import Client.MapleClient;
import Packet.LoginPacket;
import Packet.MaplePacketCreator;
import tools.Pair;
import tools.data.MaplePacketReader;

/**
 * @author PlayDK
 */
public class ShowAccCash {

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        int accId = slea.readInt(); //帳號ID
        if (c.getAccID() == accId) {
            if (c.getPlayer() != null) {
                c.announce(MaplePacketCreator.showPlayerCash(c.getPlayer()));
            } else {
                Pair<Integer, Integer> cashInfo = MapleCharacterUtil.getCashByAccId(accId);
                if (cashInfo == null) {
                    c.sendEnableActions();
                    return;
                }
                c.announce(LoginPacket.ShowAccCash(cashInfo.getLeft(), cashInfo.getRight()));
            }
        }
    }
}
