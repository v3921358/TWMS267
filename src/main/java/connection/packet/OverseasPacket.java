package connection.packet;

import Client.inventory.Item;
import Opcode.Headler.OutHeader;
import connection.OutPacket;
import SwordieX.overseas.extraequip.ExtraEquipResult;
import SwordieX.overseas.extrasystem.ExtraSystemResult;

import java.util.List;

public class OverseasPacket {

    public static OutPacket extraEquipResult(ExtraEquipResult eer) {
        OutPacket outPacket = new OutPacket(OutHeader.EXTRA_EQUIP_RESULT);
        outPacket.encode(eer);
        return outPacket;
    }

    public static OutPacket extraSystemResult(ExtraSystemResult esr) {
        OutPacket outPacket = new OutPacket(OutHeader.EXTRA_SYSTEM_RESULT);
        outPacket.encode(esr);
        return outPacket;
    }

    private static void encodeTmsEquipmentEnchantHead(OutPacket outPacket, int action, byte op, int nValue) {
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);
        outPacket.encodeByte(action);
        outPacket.encodeInt(0);
        outPacket.encodeByte(op);
        outPacket.encodeInt(nValue);
    }

    public static OutPacket getAnimusCubeRes(short opcode, int action, int value, int cubeId) {
        OutPacket outPacket = new OutPacket(opcode);

        encodeTmsEquipmentEnchantHead(outPacket, action, (byte) 3, value);
        outPacket.encodeInt(cubeId);

        return outPacket;
    }

    public static OutPacket getTmsCubeRes(short opcode, int action, int value) {
        OutPacket outPacket = new OutPacket(opcode);

        encodeTmsEquipmentEnchantHead(outPacket, action, (byte) 1, value);

        return outPacket;
    }

    public static OutPacket getAnimaCubeRes(short opcode, int action, int value, long cost) {
        OutPacket outPacket = new OutPacket(opcode);

        encodeTmsEquipmentEnchantHead(outPacket, action, (byte) 3, value);
        outPacket.encodeLong(cost);

        return outPacket;
    }

    public static OutPacket getAnimusCubeRes(short opcode, int action, int cubeId, Item item) {
        OutPacket outPacket = new OutPacket(opcode);

        encodeTmsEquipmentEnchantHead(outPacket, action, (byte) 7, 0);
        outPacket.encodeInt(cubeId);
        item.encode(outPacket);

        return outPacket;
    }

    public static OutPacket getHexaCubeRes(short opcode, int action, List<Integer> potids) {
        OutPacket outPacket = new OutPacket(opcode);

        encodeTmsEquipmentEnchantHead(outPacket, action, (byte) 7, potids == null ? 1 : 0);
        if (potids != null) {
            outPacket.encodeInt(potids.size() / 2);
            outPacket.encodeByte(potids.size() * 2);
            potids.forEach(outPacket::encodeInt);
        }

        return outPacket;
    }

    public static OutPacket getUniCubeRes(short opcode, int action, int op) {
        OutPacket outPacket = new OutPacket(opcode);

        encodeTmsEquipmentEnchantHead(outPacket, action, (byte) op, 0);

        return outPacket;
    }
}
