package Server.channel.handler;

import Client.MapleCharacter;
import Packet.MobPacket;
import tools.data.MaplePacketReader;

public class LucidHandler {

    public static void handleSpecialAttackEnd(MaplePacketReader slea, MapleCharacter player) {
        if (player == null || player.getMap() == null) return;
        player.send(MobPacket.lucidSpecialHorn(false, 0, true));
        player.send(MobPacket.lucidFieldFly(false));
        player.send(MobPacket.lucidFieldFoothold(true, player.getMap().getLachelnList()));
    }


    public static void handleSpecialHorn(MaplePacketReader slea, MapleCharacter player) {
        byte action = slea.readByte();
        if (action == 1) {
            player.send(MobPacket.lucidSpecialHorn(false, action, true));
        }
    }
}
