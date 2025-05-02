package SwordieX.client.party;

import connection.OutPacket;
import SwordieX.util.Position;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TownPortal {

    private int townID;
    private int fieldID;
    private int skillID;
    private Position fieldPortal;

    public TownPortal() {
        fieldPortal = new Position();
    }

    public void encode(OutPacket outPacket, boolean shortPos) {
        outPacket.encodeInt(getTownID());
        outPacket.encodeInt(getFieldID());
        outPacket.encodeInt(getSkillID());
        if (shortPos) {
            outPacket.encodePosition(getFieldPortal());
        } else {
            outPacket.encodePositionInt(getFieldPortal());
        }
    }
}
