package Server.world.guild;

import Packet.PacketHelper;
import lombok.Getter;
import lombok.Setter;
import tools.data.MaplePacketLittleEndianWriter;

import java.io.Serializable;

@Getter
public class MapleGuildSkill implements Serializable {

    public static final long serialVersionUID = 3565477792055301248L;
    private final int skillId;
    @Setter
    private String purchaser;
    @Setter
    private String activator;
    @Setter
    private long timestamp;
    @Setter
    private int level;

    public MapleGuildSkill(int skillid, int level, long timestamp, String purchaser, String activator) {
        this.timestamp = timestamp;
        this.skillId = skillid;
        this.level = level;
        this.purchaser = purchaser;
        this.activator = activator;
    }

    public void encode(MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(getLevel());
        mplew.writeLong(PacketHelper.getTime(getTimestamp()));
        mplew.writeMapleAsciiString(getPurchaser());
        mplew.writeMapleAsciiString(getActivator());
    }
}
