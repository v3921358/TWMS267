package Handler.Player;

import Client.MapleCharacter;
import Client.MapleClient;
import Handler.Handler;
import Opcode.Headler.OutHeader;
import connection.InPacket;
import connection.OutPacket;

import static Opcode.Headler.InHeader.*;

public class PlayerHandler {


    public static OutPacket OutLockPacket(MapleClient c) {
        OutPacket say = new OutPacket(OutHeader.USE_ITEM_LOCK_V261_1376);
        return say;
    }

    @Handler(op = R_USER_DROP_MESO) /* 玩家丟棄楓幣 */
    public static void DropMeso(MapleCharacter chr, InPacket inPacket) {
        short unk = inPacket.decodeShort();
        short unk1 = inPacket.decodeShort();
        int meso = inPacket.decodeInt();
        chr.gainMeso(-meso, false, true);
        chr.getMap().spawnMesoDrop(meso, chr.getPosition(), chr, chr, true, (byte) 0);
        chr.getCheatTracker().checkDrop(true);
        chr.getClient().ctx(1416);
        chr.getClient().ctx(607, 0);
    }

    @Handler(op = BLACK_MAGIC_RECV)
        public static void useActionBar(MapleClient c, InPacket inPacket) {
            if(c.getPlayer().getKeyValue("BlackMage") == "0") {
                c.ctx(144, "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 3F BE C4 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 01 00 00 00 00 00 1E");
                c.getPlayer().setKeyValue("BlackMage", "1");
            } else {
                c.ctx(144, "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 3F BE C4 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 01 00 00 00 00 00 17");
                c.getPlayer().setKeyValue("BlackMage", "0");
            }
    }

    @Handler(op = CP_SelPotentialPath)
    public static void selPlayerPotenPath(MapleClient c, InPacket inPacket) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_SelCharPotentialSetPath);
        byte path = inPacket.decodeByte();
        outPacket.encodeByte(true);
        outPacket.encodeByte(path);
        c.write(outPacket);
    }
}
