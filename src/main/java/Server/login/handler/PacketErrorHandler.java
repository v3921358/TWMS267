/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.login.handler;

import Client.MapleClient;
import Client.MapleJob;
import Client.SecondaryStat;
import Config.configs.Config;
import Config.constants.GameConstants;
import Config.constants.SkillConstants;
import Opcode.Headler.OutHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.HexTool;
import tools.StringUtil;
import tools.data.MaplePacketReader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author PlayDK
 */
public class PacketErrorHandler {

    private static final Logger log = LoggerFactory.getLogger("PacketErrorLog");

    private static final List<OutHeader> alreadyLoggedOpcode = new LinkedList<>();

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        if (slea.available() >= 14) {
            short mode = slea.readShort();
            String type_str = "Unknown?!";
            switch (mode) {
                case 0x01:
                    type_str = "SendBackupPacket";
                    break;
                case 0x02:
                    type_str = "Crash Report";
                    break;
                case 0x03:
                    type_str = "Exception";
                    break;
                default:
                    break;
            }
            int errorcode = slea.readInt(); // example error 38
            String errorcodeHex = "0x" + StringUtil.getLeftPaddedStr(String.valueOf(errorcode), '0', 8);
            short badPacketSize = slea.readShort(); //封包的大小
            slea.skip(4); // 未知
            int pHeader = slea.readShort(); //錯誤封包的包頭
            String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
            pHeaderStr = StringUtil.getLeftPaddedStr(pHeaderStr, '0', 4);
            OutHeader op = lookupSend(pHeader);
            if (!Config.isDevelop() && op != OutHeader.UNKNOWN) {
                if (alreadyLoggedOpcode.contains(op)) {
                    return;
                }
                alreadyLoggedOpcode.add(op);
            }
            int packetLen = (int) slea.available() + 2;
            byte[] packet = slea.read((int) slea.available());
            String AccountName = "null";
            String charName = "null";
            String charLevel = "null";
            String charJob = "null";
            String Map = "null";
            try {
                AccountName = c.getAccountName();
            } catch (Throwable e) {
            }
            try {
                charName = c.getPlayer().getName();
            } catch (Throwable e) {
            }
            try {
                charLevel = String.valueOf(c.getPlayer().getLevel());
            } catch (Throwable e) {
            }
            try {
                charJob = MapleJob.getTrueNameById(c.getPlayer().getJobWithSub()) + "(" + String.valueOf(c.getPlayer().getJob()) + ")";
            } catch (Throwable e) {
            }
            try {
                Map = c.getPlayer().getMap().toString();
            } catch (Throwable e) {
            }
            String tab = "";
            for (int i = 4; i > op.name().length() / 8; i--) {
                tab += "\t";
            }
            String t = packetLen >= 10 ? packetLen >= 100 ? packetLen >= 1000 ? "" : " " : "  " : "   ";
            log.error("\r\n"
                    + "帳號:" + AccountName + "\r\n"
                    + "角色:" + charName + "(等級:" + charLevel + ")" + "\r\n"
                    + "職業:" + charJob + "\r\n"
                    + "地圖:" + Map + "\r\n"
                    + "錯誤類型: " + type_str + "(" + mode + ")\n"
                    + "錯誤碼: " + errorcodeHex + "(" + errorcode + ")\n"
                    + "\r\n"
                    + "[LP]\t" + op.name() + tab + " \t包頭:" + pHeader + "(0x" + pHeaderStr + ")" + t + "[" + (badPacketSize - 4) + "字元]\r\n"
                    + "\r\n"
                    + (packet.length < 1 ? "" : (HexTool.toString(packet) + "\r\n"))
                    + (packet.length < 1 ? "" : (HexTool.toStringFromAscii(packet) + "\r\n")));
        }
    }

    private static OutHeader lookupSend(int val) {
        for (OutHeader op : OutHeader.values()) {
            if (op.getValue() == val) {
                return op;
            }
        }
        return OutHeader.UNKNOWN;
    }

    public static void handleErrorPacket(MaplePacketReader slea, OutHeader op, StringBuilder sb) {
        if (Objects.requireNonNull(op) == OutHeader.LP_UserEnterField) {
            slea.readLong();
            int chrId = slea.readInt();
            int level = slea.readInt();
            String chrName = slea.readMapleAsciiString();
            String ultExplorer = slea.readMapleAsciiString();
            int guildId = slea.readInt();
            String guildName = slea.readMapleAsciiString();
            int guildLogoBG = slea.readShort();
            int guildLogoBGColor = slea.readByte();
            int guildLogo = slea.readShort();
            int guildLogoColor = slea.readByte();
            int guildId2 = slea.readInt();
            slea.readInt(); // ?
            byte gender = slea.readByte();
            int popularity = slea.readInt();
            slea.readInt(); // 0
            slea.readByte(); // 0
            slea.readInt();
            sb.append("Caused by player: ").append(chrName).append("\r\n");
            List<String> list = new ArrayList<>();
            for (int i = 0; i < GameConstants.MAX_BUFFSTAT; i++) {
                int mask = slea.readInt();
                for (SecondaryStat cts : SecondaryStat.values()) {
                    if (SkillConstants.isShowForgenBuff(cts) && cts.getPosition() == i && (cts.getValue() & mask) != 0 && !SecondaryStat.getSpawnList().containsKey(cts)) {
                        list.add(cts.name());
                    }
                }
            }
            sb.append("BuffStat: ").append(list);
        }
    }

    public static void main(String[] args) {
//        String packetStr = "08 0E 00 00 8D 00 00 00 06 00 BF F1 C1 FA D5 BD 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 68 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 20 05 00 00 00 00 00 00 00 00 00 00 00 30 00 00 08 00 00 00 84 02 00 00 20 00 00 00 08 00 00 10 01 00 00 00 00 00 00 00 00 40 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 C1 8B 93 03 0A 00 07 9E A4 03 4A 01 DB 87 93 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 05 00 00 00 62 65 15 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E0 17 00 00 00 00 00 00 00 00 00 00 00 00 60 50 00 00 E0 17 00 00 00 9F 8D 00 00 0A 37 A3 14 00 0C 56 FB 10 00 01 37 57 0F 00 08 77 85 10 00 05 03 13 10 00 09 1C D1 10 00 1A 95 71 11 00 07 CF 60 10 00 0B 62 65 15 00 1D D2 46 11 00 11 D6 1F 11 00 FF 08 AC 84 10 00 05 E7 0F 10 00 07 D5 5E 10 00 09 14 D3 10 00 01 AA 51 0F 00 FF FF FF 00 00 00 00 62 65 15 00 37 A3 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C2 FE EE FF 06 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 01 00 00 9B 0A 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        // String packetStr = "3C 02 00 00 C8 00 00 00 04 00 51 42 51 42 00 00 04 00 B2 60 B7 52 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 88 16 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 0C 00 00 02 00 00 00 00 00 00 00 40 28 00 00 01 02 00 00 80 00 00 00 00 00 00 00 00 10 00 00 00 00 00 00 00 00 00 84 00 FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 01 00 ED 59 0F 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 00 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 00 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 01 B3 53 02 7A 00 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 00 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 00 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 00 00 00 00 00 00 00 00 01 BD 5E 58 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6C 3B 00 00 15 01 00 00 26 02 00 00 00 03 8F 69 00 00 6C 3B 00 00 00 F4 B5 00 00 08 4C 85 10 00 0F 8C FC 10 00 04 58 BF 0F 00 22 87 09 12 00 07 11 61 10 00 02 F0 71 0F 00 18 C3 80 1B 00 0E C1 80 1B 00 20 33 5C 19 00 09 16 D5 10 00 23 90 2A 12 00 21 68 83 19 00 10 D2 FC 10 00 0C 22 01 11 00 0D DE 03 11 00 1A C2 80 1B 00 0A 21 A7 14 00 24 CE 1F 11 00 05 81 07 10 00 1F 29 BB 11 00 15 E9 6D 11 00 03 7F 98 0F 00 1C C5 94 11 00 11 66 1F 11 00 16 8F 46 11 00 01 99 56 0F 00 0B E1 8F 13 00 FF 04 C8 BF 0F 00 07 07 60 10 00 08 48 85 10 00 10 C2 FC 10 00 0C F3 FB 10 00 0D 3D FC 10 00 02 FE 72 0F 00 09 5D D4 10 00 0F DF FB 10 00 05 33 10 10 00 03 18 99 0F 00 01 09 55 0F 00 FF FF 00 11 58 12 00 01 F0 57 12 00 FF C8 FB 19 00 E1 8F 13 00 21 A7 14 00 00 00 00 00 00 00 00 00 01 FC 4B 4C 00 FE 4B 4C 00 FD 4B 4C 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 D7 00 00 00 D7 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 FF FF 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 81 09 B1 01 24 00 00 00 01 00 00 00 00 FC 4B 4C 00 0A 00 C2 41 AC F5 A4 70 B4 63 C5 5D 5A 95 00 00 00 00 00 00 34 08 4E 01 02 8B 00 FF FF FF FF FF FF 64 00 00 00 01 01 00 00 00 FE 4B 4C 00 0A 00 D6 E6 B5 B5 A4 70 B4 63 C5 5D 55 95 00 00 00 00 00 00 D5 07 4E 01 02 86 00 FF FF FF FF FF FF 64 00 00 00 01 02 00 00 00 FD 4B 4C 00 0A 00 C0 51 C2 C5 A4 70 B4 63 C5 5D 58 95 00 00 00 00 00 00 71 08 4E 01 02 8F 00 FF FF FF FF FF FF 64 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 01 00 00 9B 0A 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 1E 72 61 A2 00 00 00 00 3C 02 00 00 01 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 00 01 00 00 00 05 00 00 00 00 00 00 00 00 04 00 00 00 5B 9E 1C 9B 00 00 00 00 3C 02 00 00 01 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 00 01 00 00 00 04 00 27 00 00 00 00 00 00 00 02 FF FF FF 50 21 98 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 63 03 00 00 00 00 05 00 56 9C 50 9C 5D 9C 00 04 00 00 01 00 00 00 05 00 0A 07 00 00 02 02 00 00 06 00 D0 07 00 00 07 00 D0 07 00 00 00 00 00 00 00 00 00 00 00 00 00";
        String packetStr = "10 D2 C6 B0 C8 B3 D7 01 FE 4E DD 00 78 00 00 00 0A 00 41 41 41 44 44 45 45 45 4C 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 A2 15 00 00 00 00 3C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A1 00 00 04 08 00 00 00 04 00 00 00 00 00 00 00 10 00 00 01 00 00 00 00 00 00 08 00 00 00 00 00 00 00 00 00 00 FC 1F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 96 57 76 E4 00 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 00 00 00 00 00 00 00 01 37 4A 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 64 08 3B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 0C 3A C7 00 00 08 3B 00 00 00 14 BE 00 00 05 C4 E2 0F 00 06 03 31 10 00 07 91 5C 10 00 08 38 83 10 00 0A 13 A9 14 00 0B 50 82 12 00 15 9E 71 11 00 23 98 2A 12 00 FF FF FF FF 00 00 00 00 50 82 12 00 13 A9 14 00 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7B 56 02 00 00 00 00 00 00 00 00 00 00 FF 00 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 FF FF 00 00 00 00 00 00 E7 00 EA 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 01 00 00 9B 0A 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF";

        MaplePacketReader slea = new MaplePacketReader(HexTool.getByteArrayFromHexString(packetStr));
        StringBuilder sb = new StringBuilder();
        handleErrorPacket(slea, OutHeader.LP_UserEnterField, sb);
        System.out.println(sb);
    }

}
