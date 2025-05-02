package SwordieX.overseas.extrasystem;

import Client.inventory.Item;
import Packet.PacketHelper;
import connection.Encodable;
import connection.OutPacket;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExtraSystemResult implements Encodable {
    private ExtraSystemType type;
    private int b;
    private Item item;
    private int valueType;
    private int value;
    private int time;
    private int b3;
    private int b4;
    private long unkData;
    private long nextTime;

    public ExtraSystemResult(ExtraSystemType type) {
        this.type = type;
    }

    @Override
    public void encode(OutPacket outPacket) {
        outPacket.encodeInt(-1289454273);
        outPacket.encodeShort(type.getVal());
        switch (type) {
            case ExtraSystem1: {
                int mask = 0;
                if (b != 0) {
                    mask |= 1;
                }
                if (item != null) {
                    mask |= 2;
                }
                mask &= 3;
                outPacket.encodeByte(mask);
                if ((mask & 1) != 0) {
                    outPacket.encodeInt(b);
                }
                if (((mask >> 1) & 1) != 0) {
                    item.encode(outPacket);
                }
                break;
            }
            case ExtraTimerSystem: { // short 2
                outPacket.encodeInt(-39984108);
                outPacket.encodeInt(1090399061);
                outPacket.encodeLong(PacketHelper.getTime(System.currentTimeMillis()));
                break;
            }
            case ExtraSystem3: {
                break;
            }
            case ExtraSystem4: {
                outPacket.encodeInt(0);
                break;
            }
            case mukhyunSystemLock: {
                break;
            }
            case mukhyunPower: { // short
                outPacket.encodeArr("00 6F 00 00 00 B8 0B 00 00 01 00 00 00 01 F8 06 02 00 00");
                outPacket.encodeByte(b4);
                break;
            }
            case mukhyunPowerNext: { // short
                outPacket.encodeArr("00 6F 00 00 00 B8 0B 00 00 01 00 00 00 01 F8 06 02 00 00");
                outPacket.encodeByte(b4);
                break;
            }
            case enableActions: {
                break;
            }

            case mukhyunSkill_175111004: {
                outPacket.encodeInt(b3);
                outPacket.encodeByte(b4);
                break;
            }
        }
    }

    public static ExtraSystemResult extraTimerSystem() {
        ExtraSystemResult esr = new ExtraSystemResult(ExtraSystemType.ExtraTimerSystem);
        //esr.unkData = unkData;
        //esr.nextTime = nextTime;
        return esr;
    }

    public static ExtraSystemResult mukhyunSystemLock() {
        ExtraSystemResult result = new ExtraSystemResult(ExtraSystemType.mukhyunSystemLock);
        return result;
    }

    public static ExtraSystemResult mukhyunPower(int b4) {
        ExtraSystemResult result = new ExtraSystemResult(ExtraSystemType.mukhyunPower);
        result.b4 = b4;
        return result;
    }
    public static ExtraSystemResult mukhyunPowerNext(int b4) {
        ExtraSystemResult result = new ExtraSystemResult(ExtraSystemType.mukhyunPower);
        result.b4 = b4;
        return result;
    }

    public static ExtraSystemResult enableActions() {
        return new ExtraSystemResult(ExtraSystemType.enableActions);
    }

    public static ExtraSystemResult mukhyunSkill_175111004(int b3, int b4) {
        ExtraSystemResult esr = new ExtraSystemResult(ExtraSystemType.mukhyunSkill_175111004);
        esr.b3 = b3;
        esr.b4 = b4;
        return esr;
    }
}
