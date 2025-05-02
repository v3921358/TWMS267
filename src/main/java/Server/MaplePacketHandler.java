package Server;

import Client.MapleClient;
import Server.channel.ChannelServer;
import tools.data.MaplePacketReader;

@FunctionalInterface
public interface MaplePacketHandler {
    void handlePacket(MaplePacketReader slea, MapleClient c, ChannelServer cs);

}
