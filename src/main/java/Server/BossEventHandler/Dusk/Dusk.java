package Server.BossEventHandler.Dusk;

import Client.MapleCharacter;
import Client.MapleClient;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMap;
import Opcode.Headler.OutHeader;
import Packet.MaplePacketCreator;
import Packet.MobPacket;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Dusk {
    public static void start(MapleCharacter player) {
        MapleMonster dusk = player.getMap().getMonsters().getFirst();
        MapleMap map = player.getMap();
        player.send(changePhase(dusk.getObjectId(), (byte) 1));
        player.setDuskGauge(1);
        map.getTimerInstance().scheduleAtFixedRate(() -> {
            MapleMonster monster = player.getMap().getMonsters().getFirst();
            long time = System.currentTimeMillis();
            if( time - monster.lastObstacleTime >= 1000){
                monster.lastObstacleTime = System.currentTimeMillis();
                map.broadcastMessage(MaplePacketCreator.createObtacleAtom(1,65,65,map));
                map.broadcastMessage(MaplePacketCreator.createObtacleAtom(1,66,66,map));
                map.broadcastMessage(MaplePacketCreator.createObtacleAtom(1,67,67,map));
            }
            if( time - monster.skillAttack_II >= 8000){
                monster.skillAttack_II = System.currentTimeMillis();
                player.send(spawnTempFoothold(player));
            }
            if( time - monster.changePhase >= 60000){
                monster.changePhase = System.currentTimeMillis();
                player.send(changePhase(monster.getObjectId(), (byte) 0));
                player.getMap().showWeatherEffectNotice("防守的觸角，會進行強力攻擊！堅持下去就能攻擊露出的虛空之眼!", 250, 3000);
                player.send(spawnHandFloor(player));
            }
            if( time - monster.changePhase_II >= 85000){
                monster.changePhase_II = System.currentTimeMillis();
                player.send(actionLaser(player));
                player.getMap().showWeatherEffectNotice("以戴斯克為中心，四周的能量正被快速吸入!", 249, 5000);
                player.send(startLaser());
            }
            if( time - monster.changePhase_IV >= 95000){
                monster.changePhase_IV = System.currentTimeMillis();
                close(player.getClient());
                player.send(changePhase(monster.getObjectId(), (byte) 1));
            }
            for (MapleCharacter cchr : map.getCharacters()) {
                for (MapleCharacter chrs : player.getMap().getCharacters()) {
                    if (chrs.getDuskGauge() >= 1000) {
                        chrs.setDuskBlind(true);
                        chrs.getClient().ctx(OutHeader.LP_TemporaryStatSet.getValue(), "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 56 BF C4 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 01 00 00 00 00 00 0F");
                    }
                    if (chrs.getDuskGauge() <= 0) {
                        chrs.setDuskGauge(1);
                        chrs.setDuskBlind(false);
                        chrs.getClient().ctx(OutHeader.LP_TemporaryStatReset.getValue(), "01 01 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
                    }
                    if (chrs.isDuskBlind()) {
                        chrs.setDuskGauge(chrs.getDuskGauge() - 50);
                    } else {
                        chrs.setDuskGauge(chrs.getDuskGauge() + 10);
                    }
                    chrs.send(Dusk.updateGauge(chrs, chrs.isDuskBlind()));
                }
                if (cchr.isDuskBlind()) {
                    if (time - cchr.getLastSpawnBlindMobTime() >= 3000) {
                        for (int i = 0; i < 3; i++) {
                            MapleMonster mob = MapleLifeFactory.getMonster(8644653);
                            mob.setOwner(cchr.getId());
                            map.spawnMonsterOnGroundBelow(mob, new Point(Randomizer.rand(-650, 650), Randomizer.rand(-500, -200)));
                            cchr.setLastSpawnBlindMobTime(time);
                        }
                    }
                }
                if (cchr.getDuskGauge() <= 0) {
                    for (MapleMonster m : map.getAllMonstersThreadsafe(true)) {
                        if (m.getOwner() == cchr.getId()) {
                            m.setHp(0);
                            MobPacket.killMonster(player, m.getObjectId(), 1);
                            map.removeMapObject(m);
                            m.killed();
                        }
                    }
                    cchr.setDuskBlind(false);
                }
            }
        }, 1000,1000, TimeUnit.MILLISECONDS);
    }


    /* 更新儀表 */
    public static byte[] updateGauge(MapleCharacter player, boolean build) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.DUSK_GAUGE.getValue());
        mplew.writeBool(build);
        mplew.writeInt(player.getDuskGauge());
        mplew.writeInt(1000);
        return mplew.getPacket();
    }

    /* 龍捲風攻擊 */
    public static byte[] darkTornado(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobSpecialEffectBySkill.getValue());
        mplew.writeInt(player.getMap().getMonsters().getFirst().getObjectId()+1);
        mplew.writeInt(2);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /* 雷射攻擊 */
    public static byte[] actionLaser(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AffectedAreaCreated.getValue());
        mplew.writeInt(51);
        mplew.write(1);
        mplew.writeInt(53423912);
        mplew.writeInt(186);
        mplew.writeInt(11);
        mplew.writeInt(-664);
        mplew.writeInt(-940);
        mplew.writeInt(662);
        mplew.writeInt(-137);
        mplew.writeInt(8);
        mplew.writeInt(-10223661);
        mplew.writeInt(9895891);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(65280);
        mplew.writeInt(2155520);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(65536);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] startLaser() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AffectedAreaCreated.getValue());
        mplew.writeInt(52);
        mplew.write(1);
        mplew.writeInt(53423912);
        mplew.writeInt(252);
        mplew.writeInt(1441793);
        mplew.writeInt(-300);
        mplew.writeInt(-1022);
        mplew.writeInt(210);
        mplew.writeInt(-147);
        mplew.writeInt(0);
        mplew.writeInt(-10223661);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(65280);
        mplew.writeInt(1848320);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(65536);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /* 變更階段 */
    public static byte[] changePhase(int monsterObjId, int phase) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MobPhaseChange.getValue());
        mplew.writeInt(monsterObjId);
        mplew.writeInt(phase);
        return mplew.getPacket();
    }

    /* 手部攻擊 */
    public static byte[] spawnTempFoothold(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(100020);
        mplew.writeInt(2);
        mplew.writeInt(0);
        mplew.writeInt(1501);
        mplew.write(1);
        mplew.writeInt(1500);
        mplew.writeInt(1);
        mplew.writeInt(35);
        mplew.writeInt(75);
        mplew.writeInt(1020);
        mplew.writeInt(6);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(-2185);
        mplew.writeInt(300);
        mplew.writeInt(-120);
        mplew.writeInt(120);
        mplew.writeInt((int) player.getPosition().getX());
        mplew.writeInt(-157);
        mplew.write(1);
        mplew.write(0);
        player.send(darkTornado(player));
        return mplew.getPacket();
    }

    /* 生成平台 */
    public static byte[] spawnHandFloor(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(100020);
        mplew.writeInt(1);
        mplew.writeInt(2);
        mplew.writeInt(23160);
        mplew.writeInt(5222401);
        mplew.writeInt(706560);
        mplew.writeInt(-19456);
        mplew.writeInt(-19201);
        mplew.writeInt(507135);
        mplew.writeInt(768);
        mplew.writeInt(512);
        mplew.writeInt(1627390464);
        mplew.writeInt(1996488753);
        mplew.writeInt(754974711);
        mplew.writeInt(-1677721599);
        mplew.writeInt(1694498815);
        mplew.writeInt(369098752);
        mplew.writeInt(1627389955);
        mplew.writeInt(16777215);
        mplew.writeInt(5606401);
        mplew.writeInt(168960);
        mplew.writeInt(20736);
        mplew.writeInt(20736);
        mplew.writeInt(506880);
        mplew.writeInt(1024);
        mplew.writeInt(512);
        mplew.writeInt(1627390464);
        mplew.writeInt(1996488754);
        mplew.writeInt(754974711);
        mplew.writeInt(-1677721599);
        mplew.writeInt(1694498815);
        mplew.writeInt(201326592);
        mplew.writeInt(1644167166);
        mplew.writeInt(16777215);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static void close(MapleClient c){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AffectedAreaRemoved.getValue());
        mplew.writeInt(51);
        mplew.writeInt(1);
        c.announce(mplew.getPacket());
    }
}
