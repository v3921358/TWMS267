package connection.packet;

import Client.MapleCharacter;
import Opcode.Headler.OutHeader;
import connection.OutPacket;

public class UserRemote {

    public static OutPacket receiveHP(MapleCharacter chr) {
        return receiveHP(chr.getId(), chr.getStat().getHp(), chr.getStat().getCurrentMaxHP());
    }

    public static OutPacket receiveHP(int charID, int curHP, int maxHP) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_UserHP);

        outPacket.encodeInt(charID);
        outPacket.encodeInt(curHP);
        outPacket.encodeInt(maxHP);
        outPacket.encodeInt(0);
        outPacket.encodeInt(4);
        return outPacket;
    }
}
