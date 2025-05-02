package connection.packet;

import Client.MapleCharacter;
import Client.inventory.Item;
import Opcode.Headler.OutHeader;
import Packet.PacketHelper;
import connection.OutPacket;
import SwordieX.enums.GroupMessageType;
import SwordieX.field.ClockPacket;
import SwordieX.field.fieldeffect.FieldEffect;
import tools.data.MaplePacketLittleEndianWriter;

public class FieldPacket {

    /**
     * Creates a Clock on a Field.
     *
     * @param clockPacket the clock to display
     * @return packet for the client
     */


    public static byte[] clock(ClockPacket clockPacket) {
        OutPacket outPacket = new OutPacket(OutHeader.CTX_Event_Field_Timer);
        clockPacket.encode(outPacket);
        return outPacket.getData();
    }

    public static byte[] fieldEffect(FieldEffect fieldEffect) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_FieldEffect);
        fieldEffect.encode(outPacket);
        return outPacket.getData();
    }


    public static OutPacket groupMessage(GroupMessageType gmt, MapleCharacter from, String msg) {

        OutPacket outPacket = new OutPacket(OutHeader.LP_GroupMessage.getValue());

        outPacket.encodeByte(gmt.ordinal());
        outPacket.encodeInt(from.getId());
        outPacket.encodeInt(from.getAccountID());

        outPacket.encodeString(from.getName());
        outPacket.encodeString(msg);

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        PacketHelper.addChaterName(mplew, from.getName(), msg);
        outPacket.encodeArr(mplew.getPacket());

        return outPacket;
    }

    public static OutPacket itemLinkedGroupMessage(GroupMessageType gmt, MapleCharacter from, String msg, Item item) {

        OutPacket outPacket = new OutPacket(OutHeader.ItemLinkedGroupMessage.getValue());

        outPacket.encodeByte(gmt.ordinal());

        outPacket.encodeInt(from.getAccountID());
        outPacket.encodeInt(from.getId());
        outPacket.encodeString(from.getName());
        outPacket.encodeString(msg);

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        PacketHelper.addChaterName(mplew, from.getName(), msg);
        outPacket.encodeArr(mplew.getPacket());

        outPacket.encodeByte(item != null);
        if (item != null) {
            outPacket.encodeByte(1);

            mplew = new MaplePacketLittleEndianWriter();
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
            outPacket.encodeArr(mplew.getPacket());

            outPacket.encodeString(item.getName());
        }

        return outPacket;
    }
}
