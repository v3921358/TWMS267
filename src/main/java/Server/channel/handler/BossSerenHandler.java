package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

public class BossSerenHandler {

    public static void SerenRazerHit(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        player.dropMessage(-11, "SEREN RAZER.");
        if (player == null || player.getMap() == null) return;
        if (slea.readByte() != 0 && !player.isInvincible()) {
            player.addHPMP(-50, 0);
        }
    }

    public static void SerenUpdateInfo(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        player.dropMessage(-11, "SEREN UPDATE INFO NOW.");
    }

    /* SCRIPT HIT PLAYER -SUNLIGHT 操作 */
    /* INFO 氣候視窗UI PACKET決定起始點 MAX 360 INT 一圈 */
    public static void initTimeChart(MapleCharacter player) {
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.SEREN_TIMER);
        int[] dayTimeArr = new int[]{110, 110, 30, 110};
        hh.writeInt(0);
        hh.writeInt(0);
        for (int i = 0; i < 4; i++) {
            hh.writeInt(dayTimeArr[i]);
        }
        player.setSerenStunGauge(+10);
        player.getClient().announce(hh.getPacket());
    }

    /* SCRIPT HIT PLAYER -SUNLIGHT 操作 */
    /* INFO 氣候視窗UI PACKET決定起始點 MAX 360 INT 一圈 */
    public static void updateTimeChart(MapleCharacter player, int dayTime) {
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.SEREN_TIMER);
        hh.writeInt(3);// 3=修改指針
        hh.writeInt(dayTime + 199);//指針位置
        player.getMap().broadcastMessage(hh.getPacket());
    }

    /* 410002060 交叉光炮軌跡 */
    public static void seren_unk1715(MapleCharacter player, int mobId, int second) { //不知道幹啥用
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.SEREN_UNK);
        hh.writeInt(mobId);
        hh.writeInt(second);
        player.getMap().broadcastMessage(hh.getPacket());
    }


}
