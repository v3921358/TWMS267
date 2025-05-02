package Packet;

import Net.server.maps.TownPortal;
import Opcode.Headler.OutHeader;
import tools.data.MaplePacketLittleEndianWriter;

public class TownPortalPacket {

    public static byte[] onTownPortalCreated(TownPortal door) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TownPortalCreated.getValue());
        mplew.write(door.getState());
        mplew.writeInt(door.getOwnerId());
        mplew.writeInt(door.getSkillId());
        mplew.writePos(door.getPosition());
        return mplew.getPacket();
    }

    public static byte[] onTownPortalRemoved(int ownerId, boolean unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TownPortalRemoved.getValue());
        mplew.write(unk);
        mplew.writeInt(ownerId);
        return mplew.getPacket();
    }
}
