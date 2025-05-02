package Server.BossEventHandler;

import Client.MapleCharacter;
import Client.MapleClient;
import Net.server.life.MapleMonster;
import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.concurrent.TimeUnit;

public class Angel {

    public static void start(MapleCharacter player) {
        enterAngel(player.getClient());
    }

    public static void enterAngel(MapleClient c) {
        c.announce(EnterFieldAngel(11));
        c.announce(EnterFieldAngel(5));
        c.announce(EnterFieldAngel(6));
        c.announce(EnterFieldAngel(8));
        c.announce(EnterFieldAngel(12));
        c.announce(EnterFieldAngel(9));
        c.getPlayer().getTimerInstance().scheduleAtFixedRate(() -> {
            MapleMonster monster = c.getPlayer().getMap().getMonsters().getFirst();
            long time = System.currentTimeMillis();
            if (time - monster.lastObstacleTime >= 90000) {
                monster.lastObstacleTime = time;
                c.getPlayer().send(EnterFieldAngel(2));
                c.getPlayer().send(EnterFieldAngelSpecial(8));
                c.getPlayer().send(EnterFieldAngelSpecial(6));
            }
            if (time - monster.lastThunderTime >= 95000) {
                monster.lastThunderTime = time;
                c.getPlayer().send(EnterFieldAngel(4));
                c.getPlayer().send(EnterFieldAngel(1));
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }


    public static byte[] EnterFieldAngel(int AngelOnPacket) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_ANGEL_ENTER_FIELD.getValue());
        mplew.writeInt(AngelOnPacket);
        switch (AngelOnPacket){
            case 1:
                mplew.writeInt(0);
                mplew.writeInt(408460376);
                mplew.writeInt(692);
                mplew.writeInt(-2199);
                mplew.writeInt(10);
                mplew.writeInt(15);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(-500);
                mplew.writeInt(20);
                mplew.writeInt(1000);
                mplew.writeInt(3);
                mplew.writeInt(1000);
                mplew.writeInt(1000);
                mplew.write(0);
                break;
            case 2:
                mplew.writeInt(717);
                mplew.writeInt(-1287);
                mplew.writeInt(717);
                mplew.writeInt(-118);
                mplew.writeInt(500);
                mplew.writeInt(300);
                mplew.writeInt(4000);
                break;
            case 4:
                mplew.writeInt(0);
                mplew.writeInt(20);
                mplew.writeInt(1000);
                break;
            case 5:
            case 6:
                mplew.writeInt(1);
                break;
            case 8:
                mplew.writeInt(0);
                mplew.writeInt(3);
                mplew.writeInt(8);
                mplew.writeInt(36);
                mplew.writeInt(-1639);
                mplew.writeInt(1375);
                mplew.writeInt(-1639);
                mplew.writeInt(36);
                mplew.writeInt(-1092);
                mplew.writeInt(1375);
                mplew.writeInt(-1092);
                mplew.writeInt(36);
                mplew.writeInt(-288);
                mplew.writeInt(1393);
                mplew.writeInt(-288);
                mplew.writeInt(36);
                mplew.writeInt(55);
                mplew.writeInt(1393);
                mplew.writeInt(55);
                mplew.writeInt(2);
                mplew.writeInt(2);
                mplew.writeInt(2);
                mplew.writeInt(2);
                mplew.writeInt(1);
                mplew.writeInt(1);
                mplew.writeInt(1);
                mplew.writeInt(1);
                mplew.writeInt(0);
                mplew.writeInt(1);
                mplew.writeInt(2);
                mplew.writeInt(3);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.write(0);
                mplew.writeShort(1);
                mplew.writeShort(1);
                mplew.writeShort(1);
                mplew.write(1);
                break;
            case 9:
                mplew.writeInt(100);
                mplew.writeInt(100);
                mplew.writeInt(100);
                break;
            case 11:
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case 12:
                mplew.writeInt(8880717);
                mplew.writeInt(12);
                mplew.writeInt(-1086);
                break;
            case 13:
                mplew.writeInt(10);
                mplew.writeInt(3);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] EnterFieldAngelSpecial(int FieldType) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_ANGEL_ENTER_FIELD.getValue());
        mplew.writeInt(FieldType);
        switch (FieldType){
            case 8:
                mplew.writeInt(1);
                mplew.writeInt(4);
                mplew.writeInt(1);
                mplew.writeInt(0);
                mplew.writeInt(3);
                mplew.writeInt(2);
                break;
            case 6:
                mplew.writeInt(3);
                break;
        }
        return mplew.getPacket();
    }



    public static byte[] startAngelGauge() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_ANGEL_GAUGE.getValue());
        mplew.writeInt(9);
        mplew.writeInt(100);
        mplew.writeInt(100);
        mplew.writeInt(100);
        return mplew.getPacket();
    }

    public static byte[] AngelXLaser(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(100023);
        mplew.writeInt(2);
        mplew.writeInt(8880713);
        mplew.writeInt(1);
        mplew.writeInt(3000);
        mplew.writeInt(40);
        mplew.writeInt(240);
        mplew.writeInt(1440);
        mplew.writeInt(210);
        mplew.writeInt(1);
        mplew.writeShort(1375);
        mplew.writeShort(-1125);
        mplew.writeShort((int) -player.getPosition().getX() -200);
        mplew.writeShort(player.getMap().getTop()-200);
        mplew.writeShort(3000);
        player.send(mplew.getPacket());
        return mplew.getPacket();
    }
}
