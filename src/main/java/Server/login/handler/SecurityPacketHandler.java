/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.login.handler;

import Client.MapleClient;
import Packet.LoginPacket;
import tools.CheckCodeImageCreator;
import tools.data.MaplePacketReader;

/**
 * @author PlayDK
 */
public class SecurityPacketHandler {
    public static void handlePhysicalCheckPacket(MaplePacketReader slea, MapleClient c) {

    }

    public static void CreateVerify(MaplePacketReader slea, MapleClient c) {
        int n = 0;
        switch (slea.readByte()) {
            case 4: {//請求驗證碼
                String code = CheckCodeImageCreator.getRandCode(); //TODO: 還需要暫存驗證碼
                c.announce(LoginPacket.createCharResponse(69, CheckCodeImageCreator.createImage(code)));
                break;
            }
            case 8: {
                String code = slea.readMapleAsciiString();
                n = 9;
                break;
            }
            case 1: { // 重新產生驗證碼
                String code = CheckCodeImageCreator.getRandCode(); //TODO: 還需要暫存驗證碼
                c.announce(LoginPacket.createCharResponse(69, CheckCodeImageCreator.createImage(code)));
                break;
            }
            case 3: {
                String code = slea.readMapleAsciiString();
                n = 4;
                break;
            }
            case 0:
                String code = slea.readMapleAsciiString();
                //TODO: 檢查驗證碼
                c.announce(LoginPacket.createCharResponse(0, new byte[]{}));
                break;
        }
    }
}
