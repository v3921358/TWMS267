package SwordieX.overseas.extraequip;

import java.util.LinkedList;
import java.util.List;

public class ExtraEquipPacket {
    public ExtraEquipMagic PacketType;
    public List<ExtraEquipOption> Packets = new LinkedList<>();

    public ExtraEquipPacket(final ExtraEquipMagic packetType) {
        this.PacketType = packetType;
    }
}
