package Server.BossEventHandler;

import Client.MapleCharacter;
import Client.MapleClient;
import Net.server.Timer;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMap;
import Opcode.Headler.OutHeader;
import Packet.PacketHelper;
import lombok.Getter;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

public class Caning {

    @Getter
    private static MapleClient c;

    @Getter
    private MapleCharacter chr;

    public static void startEvent_Field(MapleCharacter player) {
        MapleMonster monster = player.getMap().getMonsters().getFirst();
        MapleMap map = player.getMap();
        setSelMapLoad(player.getClient());
        monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            if (player.getEventInstance() == null) {
                    monster.getSchedule().cancel(true);
                    monster.setSchedule(null);
            }
            long time = System.currentTimeMillis();
            if(player.getMapId() == 410007220) {
                if (time - monster.lastLaserTime >= 10000) {
                    monster.lastLaserTime = time;
                    switch (Randomizer.rand(1, 2)) {
                        case 1:
                            player.getClient().ctx(750, "B7 86 01 00 08 00 00 00 28 0A 00 00 14 00 00 00 77 00 00 00 57 02 00 00 1D 00 00 00 01 00 00 00 03 00 00 00 14 00 00 00 00 00 00 00 A0 00 00 00 6E FD FF FF E1 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 01 00 00 00 64 01 00 00 6E FD FF FF 7D 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 02 00 00 00 B6 01 00 00 6E FD FF FF 89 01 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 03 00 00 00 54 07 00 00 6E FD FF FF 30 06 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 04 00 00 00 B6 07 00 00 6E FD FF FF 11 08 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 05 00 00 00 2A 08 00 00 6E FD FF FF 6C 07 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F5 03 00 00 BF 03 00 00 6E FD FF FF 61 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 E8 03 00 00 46 FD FF FF 6E FD FF FF 0C FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 EE 03 00 00 A4 FF FF FF 6E FD FF FF 63 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F2 03 00 00 16 03 00 00 6E FD FF FF 58 02 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F7 03 00 00 A1 04 00 00 6E FD FF FF A1 04 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F8 03 00 00 B8 04 00 00 6E FD FF FF 77 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F4 03 00 00 47 03 00 00 6E FD FF FF 8E 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 E9 03 00 00 FD FD FF FF 6E FD FF FF 40 FD FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F0 03 00 00 40 02 00 00 6E FD FF FF A5 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F3 03 00 00 63 03 00 00 6E FD FF FF F7 02 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 EB 03 00 00 AB FE FF FF 6E FD FF FF 4D FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 EC 03 00 00 6F FF FF FF 6E FD FF FF C2 FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 FA 03 00 00 B4 05 00 00 6E FD FF FF F5 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F6 03 00 00 83 04 00 00 6E FD FF FF D6 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03");
                            break;
                        case 2:
                            player.getClient().ctx(750, "B7 86 01 00 08 00 00 00 28 0A 00 00 14 00 00 00 77 00 00 00 57 02 00 00 1D 00 00 00 01 00 00 00 03 00 00 00 14 00 00 00 00 00 00 00 A0 00 00 00 6E FD FF FF E1 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 01 00 00 00 64 01 00 00 6E FD FF FF 7D 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 02 00 00 00 B6 01 00 00 6E FD FF FF 89 01 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 03 00 00 00 54 07 00 00 6E FD FF FF 30 06 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 04 00 00 00 B6 07 00 00 6E FD FF FF 11 08 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 05 00 00 00 2A 08 00 00 6E FD FF FF 6C 07 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F5 03 00 00 BF 03 00 00 6E FD FF FF 61 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 E8 03 00 00 46 FD FF FF 6E FD FF FF 0C FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 EE 03 00 00 A4 FF FF FF 6E FD FF FF 63 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F2 03 00 00 16 03 00 00 6E FD FF FF 58 02 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F7 03 00 00 A1 04 00 00 6E FD FF FF A1 04 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F8 03 00 00 B8 04 00 00 6E FD FF FF 77 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F4 03 00 00 47 03 00 00 6E FD FF FF 8E 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 E9 03 00 00 FD FD FF FF 6E FD FF FF 40 FD FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F0 03 00 00 40 02 00 00 6E FD FF FF A5 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F3 03 00 00 63 03 00 00 6E FD FF FF F7 02 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 EB 03 00 00 AB FE FF FF 6E FD FF FF 4D FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 EC 03 00 00 6F FF FF FF 6E FD FF FF C2 FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 FA 03 00 00 B4 05 00 00 6E FD FF FF F5 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F6 03 00 00 83 04 00 00 6E FD FF FF D6 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03");
                            break;
                        case 3:
                            player.getClient().ctx(750, "B7 86 01 00 08 00 00 00 28 0A 00 00 14 00 00 00 77 00 00 00 57 02 00 00 1D 00 00 00 01 00 00 00 03 00 00 00 14 00 00 00 00 00 00 00 A0 00 00 00 6E FD FF FF E1 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 01 00 00 00 64 01 00 00 6E FD FF FF 7D 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 02 00 00 00 B6 01 00 00 6E FD FF FF 89 01 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 03 00 00 00 54 07 00 00 6E FD FF FF 30 06 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 04 00 00 00 B6 07 00 00 6E FD FF FF 11 08 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 05 00 00 00 2A 08 00 00 6E FD FF FF 6C 07 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F6 03 00 00 83 04 00 00 6E FD FF FF D6 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 FC 03 00 00 45 06 00 00 6E FD FF FF 01 07 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 E9 03 00 00 FD FD FF FF 6E FD FF FF 40 FD FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F4 03 00 00 47 03 00 00 6E FD FF FF 8E 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F8 03 00 00 B8 04 00 00 6E FD FF FF 77 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 EF 03 00 00 35 00 00 00 6E FD FF FF E2 FF FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F7 03 00 00 A1 04 00 00 6E FD FF FF A1 04 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 FA 03 00 00 B4 05 00 00 6E FD FF FF F5 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 ED 03 00 00 8D FF FF FF 6E FD FF FF 8D FF FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 EB 03 00 00 AB FE FF FF 6E FD FF FF 4D FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 EC 03 00 00 6F FF FF FF 6E FD FF FF C2 FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 FD 03 00 00 77 08 00 00 6E FD FF FF 0B 08 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F0 03 00 00 40 02 00 00 6E FD FF FF A5 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 FB 03 00 00 78 06 00 00 6E FD FF FF 91 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03");
                            break;
                        case 4:
                            player.getClient().ctx(750, "B7 86 01 00 08 00 00 00 28 0A 00 00 14 00 00 00 77 00 00 00 57 02 00 00 1D 00 00 00 01 00 00 00 03 00 00 00 14 00 00 00 00 00 00 00 A0 00 00 00 6E FD FF FF E1 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 01 00 00 00 64 01 00 00 6E FD FF FF 7D 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 02 00 00 00 B6 01 00 00 6E FD FF FF 89 01 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 03 00 00 00 54 07 00 00 6E FD FF FF 30 06 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 04 00 00 00 B6 07 00 00 6E FD FF FF 11 08 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 05 00 00 00 2A 08 00 00 6E FD FF FF 6C 07 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 E8 03 00 00 46 FD FF FF 6E FD FF FF 0C FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 FB 03 00 00 78 06 00 00 6E FD FF FF 91 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 FD 03 00 00 77 08 00 00 6E FD FF FF 0B 08 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 EC 03 00 00 6F FF FF FF 6E FD FF FF C2 FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F5 03 00 00 BF 03 00 00 6E FD FF FF 61 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 ED 03 00 00 8D FF FF FF 6E FD FF FF 8D FF FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F4 03 00 00 47 03 00 00 6E FD FF FF 8E 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F3 03 00 00 63 03 00 00 6E FD FF FF F7 02 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 EA 03 00 00 33 FE FF FF 6E FD FF FF 7A FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 FC 03 00 00 45 06 00 00 6E FD FF FF 01 07 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F7 03 00 00 A1 04 00 00 6E FD FF FF A1 04 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F8 03 00 00 B8 04 00 00 6E FD FF FF 77 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 EF 03 00 00 35 00 00 00 6E FD FF FF E2 FF FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 E9 03 00 00 FD FD FF FF 6E FD FF FF 40 FD FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03");
                            break;
                    }
                }
            }
            if (time - monster.lastObstacleTime >= 60000) {
                monster.lastObstacleTime = time;
                  player.getClient().ctx(750, "BC 86 01 00 01 00 00 00 3D 00 00 00 FB 97 89 00 4E 07 00 00 61 00 00 00 E8 03 00 00");
                  player.getClient().ctx(247, "2E 00 A4 A3 B2 BB AA BA B6 C2 B6 B3 A4 77 A5 CD A6 A8 A1 43 A5 B2 B6 B7 AA FD A4 EE B2 56 A8 50 A1 41 C5 FD A8 65 B5 4C AA 6B B6 69 A4 4A A1 43 80 01 00 00 88 13 00 00 00");

            }
        }, 1000));
    }

    /* 不知道三小 */
    public static void MaybySec(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_CARNING_OUT_PACKET_1998.getValue());
        mplew.writeInt(5);
        mplew.writeInt(60000);
        c.announce(mplew.getPacket());
    }

    /* v264 咖凝 設置精神力 */
    public static void setSpirtValue(int value ,MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_CARNING_SEL_BOSS_FIRST.getValue());
        mplew.writeInt(0);
        mplew.writeInt(value);
        c.getPlayer().send(mplew.getPacket());
    }

    /* v264 咖凝 設置選擇場地加載 */
    public static void setSelMapLoad(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_CARNING_SEL_BOSS_FIRST.getValue());
        mplew.writeInt(1);
        mplew.writeInt(500);
        mplew.write(0);
        mplew.writeInt(500);
        mplew.write(0);
        mplew.writeInt(500);
        mplew.write(0);
        c.getPlayer().send(mplew.getPacket());
    }

    /* v264 咖凝 設置選擇場地加載 */
    public static void setSelMapLoadNext(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_CARNING_SEL_BOSS_FIRST.getValue());
        mplew.writeInt(2);
        mplew.writeInt(0);
        c.getPlayer().send(mplew.getPacket());
    }

    /* v264 咖凝 隊伍載入 */
    public static void setSelMapLoadParty(MapleClient c) {
        MaplePacketLittleEndianWriter sel = new MaplePacketLittleEndianWriter();
        sel.writeShort(OutHeader.BOSS_CARNING_OUT_PACKET_1995.getValue());
        sel.writeInt(0);
        sel.writeInt(c.getPlayer().getParty().getMembers().size());
        sel.writeInt(c.getPlayer().getId());
        sel.writeInt(-1);
        sel.writeInt(-1);
        sel.writeInt(675288320);
        sel.writeInt(1821852626);
        sel.writeInt(822481313);
        sel.writeInt(-1845239184);
        sel.writeInt(663120025);
        sel.writeInt(-1875766936);
        sel.writeInt(37443);
        sel.writeInt(-2147478848);
        sel.writeInt(76969);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.writeInt(0);
        sel.write(26);
        sel.writeMapleAsciiString(c.getPlayer().getName());
        sel.writeInt(c.getPlayer().getJob());
        sel.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        c.getPlayer().send(sel.getPacket());
    }

    /* @ return : / packetSize: 720
/* @ ver_264 Packet tools by Hertz - Date:2024-10-10 */
public static byte[] skill_100023_Right() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        int type = 2;
        switch (type){
            case 1:
                mplew.writeHexString("B7 86 01 00 08 00 00 00 28 0A 00 00 14 00 00 00 77 00 00 00 57 02 00 00 1D 00 00 00 01 00 00 00 03 00 00 00 14 00 00 00 00 00 00 00 A0 00 00 00 6E FD FF FF E1 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 01 00 00 00 64 01 00 00 6E FD FF FF 7D 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 02 00 00 00 B6 01 00 00 6E FD FF FF 89 01 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 03 00 00 00 54 07 00 00 6E FD FF FF 30 06 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 04 00 00 00 B6 07 00 00 6E FD FF FF 11 08 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 05 00 00 00 2A 08 00 00 6E FD FF FF 6C 07 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F3 03 00 00 63 03 00 00 6E FD FF FF F7 02 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F5 03 00 00 BF 03 00 00 6E FD FF FF 61 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 E9 03 00 00 FD FD FF FF 6E FD FF FF 40 FD FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 EE 03 00 00 A4 FF FF FF 6E FD FF FF 63 00 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 E8 03 00 00 46 FD FF FF 6E FD FF FF 0C FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F8 03 00 00 B8 04 00 00 6E FD FF FF 77 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F2 03 00 00 16 03 00 00 6E FD FF FF 58 02 00 00 D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 F9 03 00 00 49 05 00 00 6E FD FF FF F6 04 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 F4 03 00 00 47 03 00 00 6E FD FF FF 8E 03 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03 EA 03 00 00 33 FE FF FF 6E FD FF FF 7A FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 ED 03 00 00 8D FF FF FF 6E FD FF FF 8D FF FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 EB 03 00 00 AB FE FF FF 6E FD FF FF 4D FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 EC 03 00 00 6F FF FF FF 6E FD FF FF C2 FE FF FF D3 00 00 00 09 83 87 00 00 00 00 00 00 00 00 00 DD 03 FA 03 00 00 B4 05 00 00 6E FD FF FF F5 05 00 00 D3 00 00 00 09 83 87 00 01 00 00 00 01 00 00 00 DD 03");
                break;
            case 2:
                mplew.writeHexString("BC 86 01 00 01 00 00 00 3D 00 00 00 FB 97 89 00 4E 07 00 00 61 00 00 00 E8 03 00 00");
                new MapleClient().ctx(247,"2E 00 A4 A3 B2 BB AA BA B6 C2 B6 B3 A4 77 A5 CD A6 A8 A1 43 A5 B2 B6 B7 AA FD A4 EE B2 56 A8 50 A1 41 C5 FD A8 65 B5 4C AA 6B B6 69 A4 4A A1 43 80 01 00 00 88 13 00 00 00");
                break;
            case 3:
                mplew.writeHexString("");
                break;
            case 4:
                mplew.writeHexString("");
                break;
            case 5:
                mplew.writeHexString("");
                break;
        }
        return mplew.getPacket();
    }
    /* Size: 720 */

    /* @ ver_264 Packet tools by Hertz - Date:2024-10-10 */
public static byte[] Test(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);

        mplew.writeInt(4);
        mplew.write(0);

        return mplew.getPacket();
    }
    /* Size: 5 */

    /* @ ver_264 Packet tools by Hertz - Date:2024-10-10 */
    public static byte[] Test2(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);

        mplew.writeInt(4);
        mplew.write(0);

        return mplew.getPacket();
    }
    /* Size: 5 */

    /* @ ver_264 Packet tools by Hertz - Date:2024-10-10 */
    public static byte[] Test3(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);

        mplew.writeInt(4);
        mplew.write(0);

        return mplew.getPacket();
    }
    /* Size: 5 */

    /* @ ver_264 Packet tools by Hertz - Date:2024-10-10 */
    public static byte[] Test4(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);

        mplew.writeInt(4);
        mplew.write(0);

        return mplew.getPacket();
    }
    /* Size: 5 */

    /* @ ver_264 Packet tools by Hertz - Date:2024-10-10 */
    public static byte[] Test5(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);

        mplew.writeInt(4);
        mplew.write(0);

        return mplew.getPacket();
    }
    /* Size: 5 */

    /* @ ver_264 Packet tools by Hertz - Date:2024-10-10 */
    public static byte[] Test6(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);

        mplew.writeInt(4);
        mplew.write(0);

        return mplew.getPacket();
    }
    /* Size: 5 */
}
