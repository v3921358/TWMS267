/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.login.handler;

import Client.MapleCharacterUtil;
import Client.MapleClient;
import Packet.LoginPacket;
import Server.login.LoginInformationProvider;
import tools.data.MaplePacketReader;

/**
 * @author PlayDK
 */
public class CheckCharNameHandler {

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        String name = slea.readMapleAsciiString();
        boolean nameUsed;
        if (MapleCharacterUtil.canCreateChar(name, c.isGm())) {
            if (LoginInformationProvider.getInstance().isForbiddenName(name)) {
                nameUsed = !c.isGm();
            } else {
                nameUsed = false;
            }
        } else {
            nameUsed = true;
        }
        c.announce(LoginPacket.charNameResponse(name, nameUsed));
    }
}
