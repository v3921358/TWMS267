package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.inventory.*;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.SkillConstants;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.maps.FieldLimitType;
import Net.server.movement.LifeMovementFragment;
import Net.server.quest.MapleQuest;
import Opcode.Headler.OutHeader;
import Packet.EffectPacket;
import Packet.PetPacket;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.List;

public class PetHandler {

    /*
     * 召喚寵物
     */
    public static void SpawnPet(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        //[9A 00] [B8 19 35 01] [05] [00]
        chr.updateTick(slea.readInt());
        chr.spawnPet(slea.readShort(), false);
    }

    /*
     * 寵物自動加BUFF
     */
    public static void Pet_AutoBuff(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        int petid = slea.readInt();
        MaplePet pet = chr.getSpawnPet(petid);
        if (chr.getMap() == null || pet == null) {
            return;
        }
        int buffIndex = slea.readInt();
        int skillId = slea.readInt();
        if (skillId == 0 || chr.getSkillLevel(SkillConstants.getLinkedAttackSkill(skillId)) > 0) {
            chr.updateOneInfo(101080 + petid, String.valueOf(10 * petid + buffIndex), String.valueOf(skillId));
            pet.setBuffSkill(buffIndex, skillId);
            chr.petUpdateStats(pet, true);
        }
        c.sendEnableActions();
    }

    /*
     * 寵物自動喝藥
     */
    public static void Pet_AutoPotion(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        slea.skip(1);
        if (chr == null || chr.getMap() == null) {
            return;
        }
        chr.updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse;
        switch (ItemConstants.getInventoryType(itemId)) {
            case USE:
                toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
                break;
            case CASH:
                toUse = chr.getInventory(MapleInventoryType.CASH).findById(chr.getPotionPot().getItmeId());
                break;
            default:
                c.sendEnableActions();
                return;
        }
        if (!chr.isAlive() || chr.getMapId() == 749040100 || chr.getBuffStatValueHolder(SecondaryStat.StopPortion) != null || toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
            c.sendEnableActions();
            return;
        }
        long time = System.currentTimeMillis();
        if (chr.getNextConsume() > time) {
            chr.dropMessage(5, "暫時無法使用道具.");
            c.sendEnableActions();
            return;
        }
        if (!FieldLimitType.NOMOBCAPACITYLIMIT.check(chr.getMap().getFieldLimit())) { //cwk quick hack
            if (itemId == 5820000) {
                PotionPotHandler.usePotionPot(chr);
                return;
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (ii.getItemEffect(toUse.getItemId()).applyTo(chr)) {
                if (ii.getItemProperty(toUse.getItemId(), "info/notConsume", 0) == 0) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                }
                if (chr.getMap().getConsumeItemCoolTime() > 0) {
                    chr.setNextConsume(time + (chr.getMap().getConsumeItemCoolTime() * 1000L));
                }
            }
        } else {
            c.sendEnableActions();
        }
    }

    public static void PetExcludeItems(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        /*
         * FF 00
         * 00 00 00 00
         * 01
         * 63 BF 0F 00
         */
        int petSlot = slea.readInt();
        MaplePet pet = chr.getSpawnPet(petSlot);
        if (pet == null || !PetFlag.PET_IGNORE_PICKUP.check(pet.getFlags())) {
            c.sendEnableActions();
            return;
        }
        slea.readInt();
        slea.readInt();
        pet.clearExcluded(); //清除以前的過濾
        byte amount = slea.readByte(); //有多少個過濾的道具ID
        for (int i = 0; i < amount; i++) {
            pet.addExcluded(i, slea.readInt());
        }
        c.announce(PetPacket.loadExceptionList(chr, pet));
    }

    /*
     * 寵物說話
     */
    public static void PetChat(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        /*
         * FB 00
         * 00 00 00 00
         * 40 62 BB 00
         * 01 13
         * 06 00 DF C6 DF C6 DF C6
         */
        if (slea.available() < 12) {
            c.sendEnableActions();
            return;
        }
        int petid = slea.readInt();
        slea.readInt();
        if (chr == null || chr.getMap() == null || chr.getSpawnPet(petid) == null) {
            return;
        }
        short act = slea.readShort();
        String text = slea.readMapleAsciiString();
        if (text.length() < 1) {
            //FileoutputUtil.log(FileoutputUtil.寵物說話, "玩家寵物說話為空 - 操作: " + act + " 寵物ID: " + chr.getSpawnPet(petid).getPetItemId(), true);
            return;
        }
        chr.getMap().broadcastMessage(chr, PetPacket.petChat(chr.getId(), act, text, (byte) petid), true);
    }

    /*
     * 使用寵物命令
     */
    public static void PetCommand(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        /*
         * FC 00
         * 00 00 00 00
         * 00
         * 0C
         */
        int petId = slea.readInt();
        MaplePet pet = null;
        pet = chr.getSpawnPet((byte) petId);
        slea.readByte(); //always 0?
        if (pet == null) {
            c.sendEnableActions();
            return;
        }
        byte command = slea.readByte();
        PetCommand petCommand = PetDataFactory.getPetCommand(pet.getPetItemId(), command);
        if (petCommand == null) {
            c.sendEnableActions();
            return;
        }
        byte petIndex = chr.getPetIndex(pet);
        boolean success = false;
        if (Randomizer.nextInt(99) <= petCommand.getProbability()) {
            success = true;
            if (pet.getCloseness() < 30000) {
                int newCloseness = pet.getCloseness() + (petCommand.getIncrease() * ServerConfig.CHANNEL_RATE_TRAIT);
                if (newCloseness > 30000) {
                    newCloseness = 30000;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                    pet.setLevel(pet.getLevel() + 1);
                    c.announce(EffectPacket.showOwnPetLevelUp(petIndex));
                    chr.getMap().broadcastMessage(EffectPacket.showPetLevelUp(chr.getId(), petIndex));
                }
                chr.petUpdateStats(pet, true);
            }
        }
        chr.getMap().broadcastMessage(PetPacket.commandResponse(chr.getId(), (byte) petCommand.getCommand(), petIndex, success, false));
    }

    /*
     * 使用寵物食品
     */
    public static void PetFood(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        /*
         * 74 00
         * 04 3A 41 01
         * 06 00 - slot
         * 40 59 20 00 - itemId
         */
        if (chr == null || chr.getMap() == null) {
            return;
        }
        int previousFullness = 100;
        byte petslot = 0;
        MaplePet[] pets = chr.getSpawnPets();
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null && pets[i].getFullness() < previousFullness) {
                petslot = i;
                break;
            }
        }
        MaplePet pet = chr.getSpawnPet(petslot);
        chr.updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item petFood = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (pet == null || petFood == null || petFood.getItemId() != itemId || petFood.getQuantity() <= 0 || itemId / 10000 != 212) {
            c.sendEnableActions();
            return;
        }
        boolean gainCloseness = Randomizer.isSuccess(50);
        if (pet.getFullness() < 100) {
            int newFullness = pet.getFullness() + 30;
            if (newFullness > 100) {
                newFullness = 100;
            }
            pet.setFullness(newFullness);
            byte index = chr.getPetIndex(pet);
            if (gainCloseness && pet.getCloseness() < 30000) {
                int newCloseness = pet.getCloseness() + 1;
                if (newCloseness > 30000) {
                    newCloseness = 30000;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                    pet.setLevel(pet.getLevel() + 1);
                    c.announce(EffectPacket.showOwnPetLevelUp(index));
                    chr.getMap().broadcastMessage(EffectPacket.showPetLevelUp(chr.getId(), index));
                }
            }
            chr.petUpdateStats(pet, true);
            chr.getMap().broadcastMessage(c.getPlayer(), PetPacket.commandResponse(chr.getId(), (byte) 1, index, true, true), true);
        } else {
            if (gainCloseness) {
                int newCloseness = pet.getCloseness() - 1;
                if (newCloseness < 0) {
                    newCloseness = 0;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness < GameConstants.getClosenessNeededForLevel(pet.getLevel())) {
                    pet.setLevel(pet.getLevel() - 1);
                }
                chr.dropMessage(5, "您的寵物的飢餓感是滿值，如果繼續使用將會有50%的機率減少1點親密度。");
            }
            chr.petUpdateStats(pet, true);
            chr.getMap().broadcastMessage(chr, PetPacket.commandResponse(chr.getId(), (byte) 1, chr.getPetIndex(pet), false, true), true);
        }
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, true, false);
        c.sendEnableActions();
    }

    /*
     * 寵物移動
     */
    public static void MovePet(MaplePacketReader slea, MapleCharacter chr) {
        int petSlot = slea.readInt();
        slea.skip(1); //[01] V.103 新增
        final int gatherDuration = slea.readInt();
        final int nVal1 = slea.readInt();
        final Point mPos = slea.readPos();
        final Point oPos = slea.readPos();

        List<LifeMovementFragment> res;
        try {
            res = MovementParse.parseMovement(slea, 3);
        } catch (Exception e) {
            MovementParse.log.error("parseMovement error.type: pet", e);
            res = null;
        }
        if (res != null && chr != null && !res.isEmpty() && chr.getMap() != null) { // map crash hack
//            if (slea.available() != 8) {
//                System.out.println("slea.available != 8 (寵物移動出錯) 剩餘封包長度: " + slea.available());
//                FileoutputUtil.log(FileoutputUtil.Movement_Log, "slea.available != 8 (寵物移動出錯) 封包: " + slea.toString(true));
//                return;
//            }
            MaplePet pet = chr.getSpawnPet(petSlot);
            if (pet == null) {
                return;
            }
            chr.getSpawnPet(chr.getPetIndex(pet)).updatePosition(res);
            chr.getMap().broadcastMessage(chr, PetPacket.movePet(chr.getId(), petSlot, gatherDuration, nVal1, mPos, oPos, res), false);
        }
    }

    public static void AllowPetLoot(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        slea.skip(4);
        int data = slea.readShort();
        boolean canPickup = data > 0;
        chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.ALLOW_PET_LOOT)).setCustomData(String.valueOf(data));
        MaplePet[] pet = c.getPlayer().getSpawnPets();
        for (int i = 0; i < 3; i++) {
            if (pet[i] != null && pet[i].getSummoned()) {
                pet[i].setCanPickup(canPickup);
                chr.petUpdateStats(pet[i], true);
            }
        }
        c.announce(PetPacket.showPetPickUpMsg(canPickup, 1));
    }


    public static void AllowPetAutoEat(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CashPetSkillSettingResult.getValue());
        slea.skip(8);
        byte YesOrNo = slea.readByte();
        if (YesOrNo == 0) {
            chr.updateInfoQuest(GameConstants.寵物自動餵食, "autoEat=0");
        }
        if (YesOrNo == 1) {
            chr.updateInfoQuest(GameConstants.寵物自動餵食, "autoEat=1");
        }
        chr.send(mplew.getPacket());
    }
}
