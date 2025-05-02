package Net.server;

import Net.server.movement.LifeMovementFragment;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;

/**
 * @author setosan
 */
public class MapleMulitInfo implements LifeMovementFragment {

    public int skillid = 0;
    public int ObjectId = 0;
    public int direction = 0;
    public Point Position;
    public int Item = 0;
    public int damage = 0;
    public int Unknown1 = 0;
    public int Unknown2 = 0;

    public void readData(MaplePacketReader lea) {
        skillid = lea.readInt();
        Unknown1 = lea.readByte();
        ObjectId = lea.readInt();
        Unknown2 = lea.readShort();
        Position = lea.readPos();
        damage = lea.readInt();
        direction = lea.readByte();
    }

    @Override
    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.writeInt(skillid);
        lew.write(Unknown1);// = lea.readByte();
        lew.writeInt(ObjectId);// = lea.readInt();
        lew.writeShort(Unknown2);// = lea.readShort();
        lew.writePos(Position);
        lew.writeInt(damage);// = lea.readInt();
        lew.write(direction);// = lea.readByte();
    }

    public Point getPosition() {
        return Position;
    }

}
