package connection.packet;

import Opcode.Headler.OutHeader;
import connection.OutPacket;

public class MapLoadable {

    public static OutPacket setMapTaggedObjectVisisble(String MapTagedObjectTag) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_SetMapTagedObjectVisible);
        outPacket.encodeString(MapTagedObjectTag);
        return outPacket;
    }
}
