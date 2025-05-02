package Server.login.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MaplePartTimeJob;
import Packet.LoginPacket;
import tools.data.MaplePacketReader;

public class PartJobHandler {

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        final byte mode = slea.readByte();
        final int cid = slea.readInt();
        final byte job = slea.readByte();
        if (mode == 1) {
            final MaplePartTimeJob partTime = MapleCharacter.getPartTime(cid);
            if (/*chr.getLevel() < 30 || */job < 0 || job > 5 || partTime.getReward() > 0 || (partTime.getJob() > 0 && partTime.getJob() <= 5)) {
                c.getSession().close();
                return;
            }
            long time = System.currentTimeMillis();// + 3600 * 1000;
            partTime.setTime(time);
            partTime.setJob(job);
            MapleCharacter.removePartTime(cid);
            MapleCharacter.addPartTime(partTime);
            c.announce(LoginPacket.updatePartTimeJob(partTime));
        } else if (mode == 2) {
            final MaplePartTimeJob partTime = MapleCharacter.getPartTime(cid);
            if (partTime.getReward() > 0 || partTime.getJob() < 0 || partTime.getJob() > 5) {
                c.getSession().close();
                return;
            }
            final long distance = (System.currentTimeMillis() - partTime.getTime()) / (60 * 60 * 1000L);
            if (distance > 1) {
                partTime.setReward((int) (((partTime.getJob() + 1) * 1000L) + distance));
            } else {
                partTime.setJob((byte) 0);
                partTime.setReward(0);
            }
            partTime.setTime(System.currentTimeMillis());
            MapleCharacter.removePartTime(cid);
            MapleCharacter.addPartTime(partTime);
            c.announce(LoginPacket.updatePartTimeJob(partTime));
        }
    }
}