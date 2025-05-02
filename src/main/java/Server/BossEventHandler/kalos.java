package Server.BossEventHandler;

import Client.MapleCharacter;
import Client.MapleClient;
import Net.server.maps.MapleMap;
import Opcode.Headler.OutHeader;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.concurrent.TimeUnit;

public class kalos {

    public static void start(MapleCharacter player) {
        player.send(LoadKalosField());
        player.send(LoadKalosField2());
        player.send(LoadKalosField3());
        player.send(LoadKalosField4());
        MapleMap map = player.getMap();
        map.getTimerInstance().scheduleAtFixedRate(() -> {
            player.send(BallAttack(player.getClient()));
        }, 20000, 20000, TimeUnit.MILLISECONDS);
        map.getTimerInstance().scheduleAtFixedRate(() -> {
            int useFieldType = Randomizer.rand(1,4);
            switch (useFieldType) {
                case 1:
                    player.send(Imprisoned(player.getClient()));
                    break;
                case 2:
                    player.send(startUfo(player.getClient()));
                    break;
                case 3:
                    player.send(shock(player.getClient()));
                    break;
                case 4:
                    player.send(abyss(player.getClient()));
                    break;
            }
        }, 60000, 60000, TimeUnit.MILLISECONDS);
    }

    public static byte[] ReturnTimer(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_KALOS_ONPACKET.getValue());
        mplew.writeInt(1);
        mplew.writeInt(2);
        mplew.writeInt(60000);
        return mplew.getPacket();
    }

    public static byte[] LoadKalosField() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_KALOS_ONPACKET.getValue());
        mplew.writeInt(5);
        mplew.writeInt(25);
        mplew.writeInt(2000);
        mplew.writeInt(20);
        mplew.writeInt(100);
        mplew.writeInt(100);
        return mplew.getPacket();
    }

    public static byte[] LoadKalosField2() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_KALOS_ONPACKET.getValue());
        mplew.writeInt(1);
        mplew.writeInt(1);
        mplew.writeInt(60000);
        mplew.writeInt(150000);
        mplew.writeInt(4);
        mplew.writeInt(0);
        mplew.writeInt(-612);
        mplew.writeInt(-233);
        mplew.writeInt(1);
        mplew.writeInt(1626);
        mplew.writeInt(-233);
        mplew.writeInt(2);
        mplew.writeInt(630);
        mplew.writeInt(-553);
        mplew.writeInt(3);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(4);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.write(2);
        mplew.writeInt(0);
        mplew.write(3);
        mplew.writeInt(0);
        mplew.write(4);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.write(2);
        mplew.writeInt(0);
        mplew.write(3);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] LoadKalosField3() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_KALOS_ONPACKET.getValue());
        mplew.writeInt(5);
        mplew.writeInt(26);
        mplew.writeInt(10000);
        mplew.writeInt(7);
        mplew.writeInt(-800);
        mplew.writeInt(-800);
        mplew.writeInt(-400);
        mplew.writeInt(-800);
        mplew.writeInt(0);
        mplew.writeInt(-800);
        mplew.writeInt(400);
        mplew.writeInt(-800);
        mplew.writeInt(800);
        mplew.writeInt(-800);
        mplew.writeInt(1200);
        mplew.writeInt(-800);
        mplew.writeInt(1600);
        mplew.writeInt(-800);
        return mplew.getPacket();
    }

    public static byte[] LoadKalosField4() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_KALOS_ONPACKET.getValue());
        mplew.writeInt(5);
        mplew.writeInt(27);
        mplew.writeInt(10000);
        mplew.writeInt(1780);
        mplew.writeInt(5);
        mplew.writeInt(1690);
        mplew.writeInt(534);
        mplew.writeInt(1209);
        mplew.writeInt(534);
        mplew.writeInt(609);
        mplew.writeInt(534);
        mplew.writeInt(9);
        mplew.writeInt(534);
        mplew.writeInt(-609);
        mplew.writeInt(534);
        return mplew.getPacket();
    }

    public static byte[] BallAttack(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(100025);
        mplew.writeInt(9);
        mplew.writeInt(-150);
        mplew.writeInt(-330);
        mplew.writeInt(150);
        mplew.writeInt(0);
        mplew.writeInt(1080);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeInt(c.getPlayer().getMap().getMonsters().getFirst().getId());
        mplew.writeInt(6);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeInt(8);
        for (int i = 0; i < 8; i++) {
            mplew.writeShort(Randomizer.rand(c.getPlayer().getMap().getLeft(), c.getPlayer().getMap().getRight()));
            mplew.writeShort(Randomizer.rand(-398, 298));
        }
        return mplew.getPacket();
    }

        public static byte[] Imprisoned(MapleClient c) { // 囚禁之眼啟動
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_KALOS_ONPACKET.getValue());
        mplew.writeInt(3);
        mplew.writeInt(17);
        mplew.writeInt(c.getPlayer().getMap().getMonsters().getFirst().getId());
        mplew.writeInt(2);
        mplew.writeInt(100);
        mplew.writeInt(360);
        mplew.writeInt(15000);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(3);
        mplew.write(0);
        c.getPlayer().dropMessage(5, "在T-boy的干涉之下，囚禁之眼已從沉睡中甦醒。");
        return mplew.getPacket();
    }

    public static byte[] startUfo(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_KALOS_ONPACKET.getValue());
        mplew.writeInt(3);
        mplew.writeInt(10);
        mplew.writeInt(0);
        mplew.writeInt(330);
        mplew.writeInt(8881011);
        mplew.writeInt(4);
        mplew.writeInt(150);
        mplew.writeInt(10000);
        mplew.writeInt(200);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(2);
        mplew.writeInt(2);
        mplew.writeInt(1);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(965);
        mplew.writeInt(-657);
        mplew.writeInt(-720);
        mplew.writeInt(-553);
        mplew.writeInt(1785);
        mplew.writeInt(-553);
        c.getPlayer().dropMessage(5, "在T-boy的干涉之下，砲擊戰鬥機已啟動。");
        return mplew.getPacket();
    }

    public static byte[] shock(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_KALOS_ONPACKET.getValue());
        mplew.writeInt(3);
        mplew.writeInt(14);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(1);
        mplew.writeInt(500);
        mplew.writeInt(720);
        mplew.writeInt(c.getPlayer().getMap().getMonsters().getFirst().getId());
        mplew.writeInt(5);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(2);
        mplew.writeInt(9);
        mplew.writeInt(1690);
        mplew.writeInt(389);
        mplew.writeInt(1509);
        mplew.writeInt(-553);
        mplew.writeInt(1209);
        mplew.writeInt(389);
        mplew.writeInt(909);
        mplew.writeInt(-553);
        mplew.writeInt(609);
        mplew.writeInt(389);
        mplew.writeInt(309);
        mplew.writeInt(-553);
        mplew.writeInt(9);
        mplew.writeInt(389);
        mplew.writeInt(-309);
        mplew.writeInt(-553);
        mplew.writeInt(-609);
        mplew.writeInt(389);
        c.getPlayer().dropMessage(5, "在T-boy的干涉之下，奧迪溫球體開始監視敵人。");
        return mplew.getPacket();
    }

    public static byte[] abyss(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_KALOS_ONPACKET.getValue());
        mplew.writeInt(3);
        mplew.writeInt(21);
        mplew.writeInt(c.getPlayer().getMap().getMonsters().getFirst().getId());
        mplew.writeInt(3);
        mplew.writeInt(15000);
        mplew.writeInt(500);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(3);
        mplew.write(0);
        c.getPlayer().dropMessage(5, "在T-boy的干涉之下，深淵之眼已從沉睡中甦醒。");
        return mplew.getPacket();
    }
}