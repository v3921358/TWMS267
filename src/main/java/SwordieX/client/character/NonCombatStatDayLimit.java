package SwordieX.client.character;

import connection.OutPacket;
import SwordieX.util.FileTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NonCombatStatDayLimit {
    private short charisma;
    private short charm;
    private short insight;
    private short will;
    private short craft;
    private short sense;
    private FileTime lastUpdateCharmByCashPR;
    private byte charmByCashPR;

    public NonCombatStatDayLimit(short charisma, short charm, byte charmByCashPR, short insight, short will, short craft, short sense, FileTime lastUpdateCharmByCashPR) {
        this.charisma = charisma;
        this.charm = charm;
        this.charmByCashPR = charmByCashPR;
        this.insight = insight;
        this.will = will;
        this.craft = craft;
        this.sense = sense;
        this.lastUpdateCharmByCashPR = lastUpdateCharmByCashPR;
    }

    public NonCombatStatDayLimit() {
        this((short) 0, (short) 0, (byte) 0, (short) 0, (short) 0, (short) 0, (short) 0, FileTime.fromType(FileTime.Type.ZERO_TIME));
    }

    public void encode(OutPacket outPacket) {
        outPacket.encodeInt(getCharisma()); // 28
        outPacket.encodeInt(getInsight()); // 32
        outPacket.encodeInt(getWill()); // 36
        outPacket.encodeInt(getCraft()); // 40
        outPacket.encodeInt(getSense()); // 44
        outPacket.encodeInt(getCharm()); // 48
        outPacket.encodeByte(0);
        outPacket.encodeArr("00 40 E0 FD 3B 37 4F 01");
        outPacket.encodeInt(FileTime.currentTime().toYYMMDDintValue()); // 20240627
    }
}
