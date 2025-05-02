package Packet;

import Client.MapleCharacter;
import Client.MapleStat;
import Client.inventory.MaplePet;
import Net.server.movement.LifeMovementFragment;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.List;

public class PetPacket {

    public static byte[] showPetPickUpMsg(boolean canPickup, int pets) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CashPetPickUpOnOffResult.getValue());
        mplew.write(canPickup);
        mplew.write(pets);
        return mplew.getPacket();
    }

    public static byte[] showPet(MapleCharacter chr, MaplePet pet, boolean remove, byte showType) {
        return showPet(chr, pet, remove, showType, false);
    }

    public static byte[] showPet(MapleCharacter chr, MaplePet pet, boolean remove, byte showType, boolean show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_PetActivated.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getPetIndex(pet));
        if (remove) {
            mplew.writeShort(show ? 256 : 0);
        } else {
            mplew.write(1);
            mplew.write(1);
            mplew.writeInt(pet.getPetItemId());
            mplew.writeMapleAsciiString(pet.getName());
            mplew.writeLong(pet.getUniqueId());
            mplew.writeShort((pet.getPos()).x);
            mplew.writeShort((pet.getPos()).y - 20);
            mplew.write(pet.getStance());
            mplew.writeShort(pet.getFh());
            mplew.writeInt(pet.getColor());
            mplew.writeInt(pet.getPetItemId());
            mplew.writeShort(-1);
            mplew.writeInt(100);
        }
        return mplew.getPacket();
    }

    public static void addPetInfo(MaplePacketLittleEndianWriter mplew, MaplePet pet) {
        mplew.writeInt(pet.getPetItemId());
        mplew.writeMapleAsciiString(pet.getName());
        mplew.writeLong(pet.getUniqueId()); //寵物的SQL唯一ID
        mplew.writePos(pet.getPos().getLocation()); //寵物的坐標
        mplew.write(pet.getStance()); //姿勢
        mplew.writeShort(pet.getFh());
        mplew.writeInt(pet.getColor()); //getColor
        mplew.writeInt(pet.getPetItemId());
        mplew.writeShort(-1);
        mplew.writeInt(100);
    }

    public static byte[] movePet(int chrId, int slot, int gatherDuration, int nVal1, Point mPos, Point oPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_PetMove.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(slot);

        PacketHelper.serializeMovementList(mplew, gatherDuration, nVal1, mPos, oPos, moves, null);

        return mplew.getPacket();
    }

    public static byte[] petChat(int chaId, short act, String text, short slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_PetAction.getValue());
        mplew.writeInt(chaId);
        mplew.writeInt(0);
        mplew.writeShort(act);
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }

    public static byte[] petSpeak(int chaId, String text, short slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_PetActionSpeak.getValue());
        mplew.writeInt(chaId);
        mplew.writeInt(slot);
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }

    public static byte[] changePetName(MapleCharacter chr, String newname, final int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_PetNameChanged.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(slot);
        mplew.writeMapleAsciiString(newname);

        return mplew.getPacket();
    }

    public static byte[] petModified(MapleCharacter chr, final int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_PetModified.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(slot);
        mplew.writeShort(0);
        mplew.write(1);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] commandResponse(int chrId, byte command, short slot, boolean success, boolean food) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_PetActionCommand.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(slot);
        mplew.write(food ? 2 : 1);
        mplew.write(command);
        if (food) {
            mplew.writeInt(0); //T071修改為 Int
        } else {
            mplew.writeShort(success ? 1 : 0);  //T071修改為 byte
        }
        return mplew.getPacket();
    }

    public static byte[] showPetLevelUp(MapleCharacter chr, byte index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(EffectOpcode.UserEffect_Pet.getValue());
        mplew.write(0);
        mplew.writeInt(index);

        return mplew.getPacket();
    }

    public static byte[] loadExceptionList(MapleCharacter chr, MaplePet pet) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_PetLoadExceptionList.getValue());
        mplew.writeLong(pet.getUniqueId());
        List<Integer> excluded = pet.getExcluded();
        mplew.write(excluded.size());
        for (Integer anExcluded : excluded) {
            mplew.writeInt(anExcluded);
        }

        return mplew.getPacket();
    }

    public static byte[] petStatUpdate(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_StatChanged.getValue());
        mplew.write(0);
        mplew.write(0);
        mplew.write(1);
        mplew.writeLong(MapleStat.寵物.getValue());
        MaplePet[] pets = chr.getSpawnPets();
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.writeLong(pets[i].getUniqueId());
            } else {
                mplew.writeLong(0);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] changePetColor(MapleCharacter player, MaplePet pet) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_PetHueChanged.getValue());
        mplew.writeInt(player.getId());
        mplew.writeInt(player.getPetIndex(pet));
        mplew.writeInt(pet.getColor());

        return mplew.getPacket();
    }
}
