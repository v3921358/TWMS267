package Server.BossEventHandler.Demian;

import Client.MapleCharacter;
import Net.server.maps.MapleMap;
import Opcode.Headler.OutHeader;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.util.concurrent.TimeUnit;

public class Demian {

    public static void start(MapleCharacter player) {
        MapleMap map = player.getMap();
        player.getMap().showWeatherEffectNotice("不知道戴米安對誰蓋烙印",216, 3000);
        map.getTimerInstance().scheduleAtFixedRate(()->{
        player.send(Tombstone(true));
        player.send(StigmaTime(30000));
      }, 5000, 5000, TimeUnit.MILLISECONDS);

      player.send(Tombstone(true));
      player.send(spawnSword(player));
      player.send(target(player.getId()));
      player.send(spawnSword2(player));

      map.getTimerInstance().scheduleAtFixedRate(()->{
          player.getMap().showWeatherEffectNotice("不知道戴米安對誰蓋烙印",216, 3000);
          player.send(Tombstone(false));
          player.send(StigmaTime(30000)); // 30s
      }, 30000, 30000, TimeUnit.MILLISECONDS);
      map.getTimerInstance().scheduleAtFixedRate(()->{
          player.send(Tombstone(true));
      }, 60000,30000, TimeUnit.MILLISECONDS);
    }

    public static byte[] Tombstone(boolean spawn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_DEMIAN_CREAT_TOMBSTONE.getValue());
        if(spawn) {
            int rx = Randomizer.rand(-1000, 1000);
            mplew.writeInt(0);
            mplew.writeInt(rx);
            mplew.writeInt(16);
            mplew.writeInt(2500);
            mplew.writeInt(1);
            mplew.writeMapleAsciiString("Map/Obj/BossDemian.img/demian/altar");
            mplew.write(0);
        } else {
            mplew.writeInt(1);
        }
        return mplew.getPacket();
    }

    public static byte[] spawnSword(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_DEMIAN_CREAT_FLYSWORD.getValue());
        mplew.write(1);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.write(4);
        mplew.writeInt(player.getMap().getMonsters().getFirst().getId());
        mplew.writeInt(895);
        mplew.writeInt(-200);
        return mplew.getPacket();
    }

    public static byte[] unkDemian() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_DEMIAN_SWORD_CREAT_ASK_IN_MAP.getValue());
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] target(int playerId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_DEMIAN_FIELD_TARGET.getValue());
        mplew.writeInt(1);
        mplew.writeInt(playerId);
        return mplew.getPacket();
    }

    public static byte[] spawnSword2(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_DEMIAN_FLYSWORD_NODE_ATTACK.getValue());
        mplew.writeInt(1);
        mplew.writeInt(player.getId());
        mplew.write(0);
        mplew.writeInt(1);
        mplew.writeInt(1);
        mplew.write(0);
        mplew.writeInt(30);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(895);
        mplew.writeInt(-200);
        return mplew.getPacket();
    }

    public static byte[] AttackNode(MaplePacketReader slea, MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_DEMIAN_FLYSWORD_NODE_ATTACK.getValue());
        mplew.writeInt(1);
        mplew.writeInt(player.getId());
        mplew.writeHexString("00 15 00 00 00 01 06 00 00 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F 03 00 00 D4 FE FF FF 01 06 00 01 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 05 00 00 32 00 00 00 01 06 00 02 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 9A 06 00 00 6A FF FF FF 01 06 00 03 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 68 06 00 00 32 00 00 00 01 06 00 04 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 74 04 00 00 6A FF FF FF 01 06 00 05 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 EA 01 00 00 32 00 00 00 01 06 00 06 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 BE 00 00 00 9C FF FF FF 01 06 00 07 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C4 FF FF FF E7 FF FF FF 01 06 00 08 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E4 02 00 00 E7 FF FF FF 01 06 00 09 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7A 03 00 00 64 00 00 00 01 06 00 0A 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 D2 05 00 00 38 FF FF FF 01 06 00 0B 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 9A 06 00 00 E7 FF FF FF 01 06 00 0C 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7A 03 00 00 E7 FF FF FF 01 06 00 0D 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1C 02 00 00 D4 FE FF FF 01 06 00 0E 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5A 00 00 00 64 00 00 00 01 06 00 0F 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C4 FF FF FF 9C FF FF FF 01 06 00 10 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7A 03 00 00 32 00 00 00 02 06 00 11 00 23 00 F4 01 00 00 00 00 00 00 00 00 00 00 00 00 7A 03 00 00 32 00 00 00 01 06 00 12 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F 03 00 00 D4 FE FF FF 02 06 00 13 00 3C 00 F4 01 00 00 00 00 00 00 00 00 00 00 00 00 7A 03 00 00 32 00 00 00 02 06 00 14 00 23 00 00 00 00 00 F8 2A 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00");
        return mplew.getPacket();
    }

    public static byte[] NodeFire(int pid, int x, int y){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AffectedAreaCreated.getValue());
        MapleCharacter player = MapleCharacter.getCharacterById(pid);
        mplew.writeInt(1020);
        mplew.write(1);
        mplew.writeInt(pid);
        mplew.writeInt(131);
        mplew.writeShort(28);
        mplew.writeShort(9);
        mplew.writeInt(436);
        mplew.writeInt(-169);
        mplew.writeInt(x);
        mplew.writeInt(y);
        mplew.writeInt(0);
        mplew.writeShort(x);
        mplew.writeShort(y);
        mplew.writeInt(0);
        mplew.writeInt(5);
        mplew.writeInt(1);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(2560000);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(65536);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] StigmaTime(int ms) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.STIGMA_TIME.getValue());
        mplew.writeInt(ms);
        return mplew.getPacket();
    }
}
