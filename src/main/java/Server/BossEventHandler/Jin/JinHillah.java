package Server.BossEventHandler.Jin;

import Client.MapleCharacter;
import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;

/**
 * @author by Taiwan twms and SyncTwMs team [ Hertz ]  fix.txt...
 * @version TMS v245
 * 註記: 真希拉 handler -  for event start
 * 時間: 2023-8
 */

public class JinHillah {

    public static void start(MapleCharacter player) {
            player.getMap().showWeatherEffectNotice("希拉每隔一段時間會砍掉靈魂燃燒的蠟燭。小心別讓靈魂被奪走。", 254, 8000);
            player.send(candlelight()); // 蠟燭數量
            player.send(JinHillahDeadCountEffect()); // 生命圖標載入
            player.send(Hourglass()); // 沙漏載入 正設 150秒
    }


    public static byte[] candlelight() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.JIN_HILLAH.getValue());
        mplew.writeInt(0);
        mplew.writeInt(3);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] JinHillahDeadCountEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.JIN_HILLAH.getValue());
        mplew.writeInt(3);
        mplew.writeInt(5);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.write(0);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.write(0);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] setDeadCountEffect(MapleCharacter player, int count) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.JIN_HILLAH.getValue());
        mplew.writeInt(10);
        mplew.writeInt(1);
        mplew.writeInt(player.getId());
        mplew.writeInt(5);
        return mplew.getPacket();
    }

    public static byte[] Hourglass() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.JIN_HILLAH.getValue());
        mplew.writeInt(4);
        mplew.writeInt(150000);
        mplew.writeInt(247);
        mplew.writeInt(1);
        return mplew.getPacket();
    }
}
