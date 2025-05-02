package Handler.Monster;

import Client.MapleClient;
import Handler.Handler;
import Opcode.Headler.OutHeader;
import connection.InPacket;
import connection.OutPacket;

import static Opcode.Headler.InHeader.BOSS_ANGEL_ONPACK;
import static Opcode.Headler.InHeader.BOSS_KALOS_ONPACKET;

public class MonsterHandler {

    @Handler(ops = BOSS_ANGEL_ONPACK)
    public static void AngelOnPacket(MapleClient c, InPacket inPacket) {
        OutPacket outPacket = new OutPacket(OutHeader.BOSS_ANGEL_ENTER_FIELD);
        int ActionBar = inPacket.decodeInt();
        int onPacketType = inPacket.decodeInt();
        switch (ActionBar) {
            case 4:
                outPacket.encodeInt(7);
                outPacket.encodeInt(2);
                outPacket.encodeInt(onPacketType);
                break;
            case 14:
                outPacket.encodeInt(9);
                outPacket.encodeInt(0);
                outPacket.encodeInt(100);
                outPacket.encodeInt(100);
                break;
        }
        c.write(outPacket);
    }

    @Handler(ops = BOSS_KALOS_ONPACKET)
    public static void kalosOnPacket(MapleClient c, InPacket inPacket) {
        OutPacket outPacket = new OutPacket(OutHeader.BOSS_KALOS_ONPACKET.getValue());
        int get = inPacket.decodeInt();
        switch (get) {
            case 1:
            case 3: {
                outPacket.encodeInt(1);
                outPacket.encodeInt(2);
                outPacket.encodeInt(59281);
                break;
            }
            case 5: {
                int get2 = inPacket.decodeInt();
                int get3 = inPacket.decodeInt();
                int get4 = inPacket.decodeInt();
                int get5 = inPacket.decodeInt();
                outPacket.encodeInt(5);
                outPacket.encodeInt(28);
                outPacket.encodeInt(0);
                outPacket.encodeInt(c.getPlayer().getId());
                outPacket.encodeInt(get3);
                outPacket.encodeInt(get4);
                outPacket.encodeInt(get5);
                break;
            }
            default:
                break;
        }
        int get2 = inPacket.decodeInt();
        switch (get2) {
            case 4: {
                inPacket.decodeArr(8);
                int x = inPacket.decodeInt();
                int y = inPacket.decodeInt();
                outPacket.encodeInt(3);
                outPacket.encodeInt(10);
                outPacket.encodeInt(4);
                outPacket.encodeInt(330);
                outPacket.encodeInt(c.getPlayer().getMap().getMonsters().getFirst().getId());
                outPacket.encodeInt(4);
                outPacket.encodeInt(150);
                outPacket.encodeInt(10000);
                outPacket.encodeInt(200);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(2);
                outPacket.encodeInt(2);
                outPacket.encodeShort(0);
                outPacket.encodeInt(x);
                outPacket.encodeInt(y);
                outPacket.encodeInt(176);
                outPacket.encodeInt(-657);
                outPacket.encodeInt(-720);
                outPacket.encodeInt(y);
                outPacket.encodeInt(1785);
                outPacket.encodeInt(y);
                break;
            }
            case 5: {
                outPacket.encodeInt(3);
                outPacket.encodeInt(18);
                outPacket.encodeInt(2);
                outPacket.encodeInt(1);
                outPacket.encodeInt(c.getPlayer().getPosition().x);
                outPacket.encodeInt(c.getPlayer().getPosition().y);
                break;
            }
            case 6: {
                int get3 = inPacket.decodeInt();
                if (get3 == 1) {
                    outPacket.encodeInt(3);
                    outPacket.encodeInt(22);
                    outPacket.encodeInt(2);
                    outPacket.encodeInt(3);
                    outPacket.encodeInt(17);
                    outPacket.encodeInt(1);
                    outPacket.encodeInt(c.getPlayer().getId());
                    outPacket.encodeInt(0);

                    outPacket.encodeInt(23);
                    outPacket.encodeInt(1);
                    outPacket.encodeInt(c.getPlayer().getId());
                    outPacket.encodeInt(0);

                    outPacket.encodeInt(29);
                    outPacket.encodeInt(1);
                    outPacket.encodeInt(c.getPlayer().getId());
                    outPacket.encodeInt(0);
                } else {
                    outPacket.encodeInt(3);
                    outPacket.encodeInt(22);
                    outPacket.encodeInt(1);
                    outPacket.encodeInt(3);
                    outPacket.encodeInt(17);
                    outPacket.encodeInt(1);
                    outPacket.encodeInt(c.getPlayer().getId());
                    outPacket.encodeInt(0);

                    outPacket.encodeInt(23);
                    outPacket.encodeInt(1);
                    outPacket.encodeInt(c.getPlayer().getId());
                    outPacket.encodeInt(0);

                    outPacket.encodeInt(29);
                    outPacket.encodeInt(1);
                    outPacket.encodeInt(c.getPlayer().getId());
                    outPacket.encodeInt(0);
                }
                break;
            }
            case 7: {
                int get3 = inPacket.decodeInt();
                outPacket.encodeInt(3);
                outPacket.encodeInt(24);
                outPacket.encodeInt(get3);
                outPacket.encodeInt(c.getPlayer().getId());
                break;
            }
            default:
                break;
        }
        c.write(outPacket);
    }
}
