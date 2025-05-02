/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.maps;

import Client.MapleCharacter;
import Client.MapleClient;
import Net.server.Timer.WorldTimer;
import Packet.MaplePacketCreator;
import Server.world.WorldBroadcastService;

import java.util.LinkedList;
import java.util.List;

/**
 * @author PlayDK
 */
public class MapleTVEffect {

    private static boolean active;
    private final MapleCharacter user;
    private final int type;
    MapleClient c;
    private List<String> message = new LinkedList<>();
    private MapleCharacter partner = null;

    public MapleTVEffect(MapleCharacter User, MapleCharacter Partner, List<String> Msg, int Type) {
        this.message = Msg;
        this.user = User;
        this.type = Type;
        this.partner = Partner;
    }

    public static boolean isActive() {
        return active;
    }

    private void setActive(boolean set) {
        active = set;
    }

    public static int getDelayTime(int type) {
        switch (type) {
            case 0:
            case 3:
                return 15;
            case 1:
            case 4:
                return 30;
            case 2:
            case 5:
                return 60;
        }
        return 0;
    }

    public void stratMapleTV() {
        broadCastTV(true);
    }

    private void broadCastTV(boolean isActive) {
        setActive(isActive);
        if (isActive) {
            int delay = getDelayTime(type);
            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.enableTV());
            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.sendTV(user, message, type <= 2 ? type : type - 3, partner, delay));

            WorldTimer.getInstance().schedule(() -> broadCastTV(false), delay * 1000L);
        } else {
            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.removeTV());
        }
    }
}
