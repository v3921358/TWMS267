package Server.BossEventHandler;


import Client.MapleCharacter;
import Client.MapleClient;
import Net.server.maps.MapleMap;
import Net.server.maps.field.BossWillField;
import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.concurrent.TimeUnit;

public class Will {

    private static MapleCharacter chr;

    public static MapleCharacter getChr() {
        return chr;
    }

    public static void start(MapleCharacter player) {
        MapleMap map = player.getMap();
        map.getTimerInstance().scheduleAtFixedRate(() -> {
           BossWillField.init();
        }, 1000,1000, TimeUnit.MILLISECONDS);
    }

    public static byte[] setMoonGauge(int max, int info) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.MoonlightValue.getValue());
        mplew.writeInt(max);
        mplew.writeInt(info);
        return mplew.getPacket();
    }

    public static byte[] addMoonGauge(MapleClient c, int MoonlightValue) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_INFO_MOON_GAUGE.getValue());
        mplew.writeInt(c.getPlayer().getMoonGauge()+MoonlightValue);
        return mplew.getPacket();
    }

    /* @ ver_264 Packet tools by Hertz - Date:2024-10-05 */
    public static void WillLockPower(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(87);
        mplew.write(1);
        mplew.writeInt(242);
        mplew.writeInt(5);
        c.announce(mplew.getPacket());
    }

    public static void showEyeEffect(MapleClient c, int set) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(1927);
        mplew.writeInt(0); // 0 show / 1 close
        mplew.writeInt(set); // 8880363
        mplew.writeInt(-405);
        mplew.writeInt(-400);
        c.announce(mplew.getPacket());
        MaplePacketLittleEndianWriter mplew2 = new MaplePacketLittleEndianWriter();
        mplew2.writeShort(1927);
        mplew2.writeInt(0); // 0 show / 1 close
        mplew2.writeInt(set); // 8880363
        mplew2.writeInt(-405);
        mplew2.writeInt(-400);
        getChr().send(mplew2.getPacket());
        MaplePacketLittleEndianWriter mplew3 = new MaplePacketLittleEndianWriter();
        mplew3.writeShort(1927);
        mplew3.writeInt(1);
        mplew3.writeInt(set);
        mplew3.writeInt(300);
        mplew3.writeInt(100);
        mplew3.writeInt(1800);
        mplew3.writeInt(5);
        mplew3.write(1);
        mplew3.writeInt(-442);
        mplew3.writeInt(695);
        mplew3.writeInt(160);
        getChr().send(mplew3.getPacket());
    }


    /**
     * @主核心版本:257
     * @日期:2024/1/3
     * @PacketAuthor:Hertz
     * @return_蜘蛛腳空中穿刺
     */

    public static byte[] 蜘蛛腳攻擊(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_WILL_GET_SPIDER_ATTACK.getValue());
        mplew.writeHexString("EC 80 87 00 F2 00 00 00 10 00 00 00 04 00 00 00 B0 04 00 00 28 23 00 00 D8 FF FF FF A8 FD FF FF 28 00 00 00 0A 00 00 00 18 00 00 00 B5 00 00 00 08 07 00 00 00 00 00 00 00 00 00 00 B6 00 00 00 08 07 00 00 9C FF FF FF 00 00 00 00 B7 00 00 00 08 07 00 00 64 00 00 00 00 00 00 00 B8 00 00 00 10 0E 00 00 70 FE FF FF 00 00 00 00 B9 00 00 00 10 0E 00 00 D4 FE FF FF 00 00 00 00 BA 00 00 00 10 0E 00 00 38 FF FF FF 00 00 00 00 BB 00 00 00 10 0E 00 00 90 01 00 00 00 00 00 00 BC 00 00 00 10 0E 00 00 2C 01 00 00 00 00 00 00 BD 00 00 00 10 0E 00 00 C8 00 00 00 00 00 00 00 BE 00 00 00 18 15 00 00 44 FD FF FF 00 00 00 00 BF 00 00 00 18 15 00 00 A8 FD FF FF 00 00 00 00 C0 00 00 00 18 15 00 00 0C FE FF FF 00 00 00 00 C1 00 00 00 18 15 00 00 BC 02 00 00 00 00 00 00 C2 00 00 00 18 15 00 00 58 02 00 00 00 00 00 00 C3 00 00 00 18 15 00 00 F4 01 00 00 00 00 00 00 C4 00 00 00 20 1C 00 00 9C FF FF FF 00 00 00 00 C5 00 00 00 20 1C 00 00 00 00 00 00 00 00 00 00 C6 00 00 00 20 1C 00 00 64 00 00 00 00 00 00 00 C7 00 00 00 28 23 00 00 44 FD FF FF 00 00 00 00 C8 00 00 00 28 23 00 00 A8 FD FF FF 00 00 00 00 C9 00 00 00 28 23 00 00 0C FE FF FF 00 00 00 00 CA 00 00 00 28 23 00 00 BC 02 00 00 00 00 00 00 CB 00 00 00 28 23 00 00 58 02 00 00 00 00 00 00 CC 00 00 00 28 23 00 00 F4 01 00 00 00 00 00 00");
        c.announce(mplew.getPacket());
        return mplew.getPacket();
    }

    /**
     * @主核心版本:257
     * @日期:2024/1/3
     * @PacketAuthor:Hertz
     * @return_開啟三階段血量視窗
     */
    public static byte[] 顯示三階段血量(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_WILL_SHOW_HP_PHASE_ALL.getValue());
        mplew.writeHexString("03 00 00 00 9A 02 00 00 4D 01 00 00 03 00 00 00 01 D4 80 87 00 00 20 04 C7 A3 07 00 00 00 20 04 C7 A3 07 00 00 01 D7 80 87 00 00 20 04 C7 A3 07 00 00 00 20 04 C7 A3 07 00 00 01 D8 80 87 00 00 20 04 C7 A3 07 00 00 00 20 04 C7 A3 07 00 00");
        c.announce(mplew.getPacket());
        return mplew.getPacket();
    }


    public static byte[] 場景變更(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_TELEPORT.getValue());
        mplew.writeInt(1);
        c.announce(mplew.getPacket());
        c.announce(場景跳躍(c));
        return mplew.getPacket();
    }

    public static byte[] 場景跳躍(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_WILL_JUMP_POSCTION.getValue());
        if (c.getPlayer().getPosition().getY() < 0) {
            mplew.writeLong(7959953343641550854L);
        }
        if ( c.getPlayer().getPosition().getY() > 0) {
            mplew.writeShort(4);
            mplew.writeInt(1886745712);
        }
        c.getPlayer().modifyMoonlightValue(-45);
        return mplew.getPacket();
    }

    /**
     * @主核心版本:257
     * @日期:2024/1/3
     * @PacketAuthor:Hertz
     * @return_
     */

    public static byte[] 一階力量解放(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_WILL_GET_SPIDER_ATTACK.getValue());
        c.getPlayer().getMap().showWeatherEffectNotice("威爾即將解放他的力量，虛假空間即將崩潰，請尋找真實空間躲避。", 245, 3000);
        mplew.writeHexString("D4 80 87 00 F2 00 00 00 05 00 00 00 02 00 00 00 00 4E FD FF FF B6 F5 FF FF B7 02 00 00 1D F8 FF FF 01 4E FD FF FF 39 FE FF FF B7 02 00 00 A0 00 00 00");
        c.announce(mplew.getPacket());
        c.announce(一階力量解放_蜘蛛網(c));
        return mplew.getPacket();
    }

    public static byte[] 一階力量解放_蜘蛛網(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.writeHexString("55 01 F2 00 00 00 05 00 00 00 00");
        c.announce(mplew.getPacket());
        return mplew.getPacket();
    }

    /**
     * @主核心版本:257
     * @日期:2024/1/3
     * @PacketAuthor:Hertz
     * @return_
     */

    public static byte[] willPacket6(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_SET_MOON_GAUGE.getValue());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * @主核心版本:257
     * @日期:2024/1/3
     * @PacketAuthor:Hertz
     * @return_
     */

    public static byte[] willPacket7(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_SET_MOON_GAUGE.getValue());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

}
