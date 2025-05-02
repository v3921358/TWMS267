package SwordieX.client.character.avatar;

import Client.MapleCharacter;
import Client.inventory.*;
import Config.constants.JobConstants;
import connection.InPacket;
import connection.OutPacket;
import lombok.Getter;
import lombok.Setter;
import tools.data.ByteArrayByteStream;
import tools.data.ByteStream;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

@Setter
@Getter
public class AvatarLook {
    private int gender;
    private int skin;
    private int face;
    private int hair;
    private int job;
    private final Map<Byte, Integer> equips = new LinkedHashMap<>();
    private final Map<Byte, Integer> maskedEquips = new LinkedHashMap<>();
    private final Map<Byte, Integer> equipMixColors = new LinkedHashMap<>();
    private final Map<Byte, Integer> totemEquips = new LinkedHashMap<>();
    private int weaponStickerId;
    private int weaponId;
    private int subWeaponId;
    private int ear;
    private boolean drawElfEar;
    private final int[] pets = new int[3];
    private int defFaceAcc;
    private boolean isSecondLook;
    private String name;

    public AvatarLook() {
    }

    public AvatarLook(MapleCharacter chr, boolean second) {
        boolean zero = JobConstants.is神之子(chr.getJob());
        gender = zero && second ? 1 : chr.getGender();
        skin = second ? chr.getSecondSkinColor() : chr.getSkinColor();
        face = second ? chr.getSecondFace() : chr.getFace();
        job = chr.getJob();
        hair = second ? chr.getSecondHair() : chr.getHair();

        Map<Byte, Integer> angelEquip = new LinkedHashMap<>();
        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
        boolean angel = JobConstants.is天使破壞者(chr.getJob());

        for (Item item : equip.newList()) { //遍歷裝備列表
            int itemID = item.getItemId();
            if (item instanceof Equip && ((Equip) item).getItemSkin() > 0) {
                itemID = ((Equip) item).getItemSkin();
            }
            if (angel && second && item.getPosition() <= -1300 && item.getPosition() > -1310) {
                final byte b809;
                switch (b809 = (byte) (-item.getPosition() - 1300)) {
                    case 7: {
                        angelEquip.put((byte) 5, itemID);
                        break;
                    }
                    case 0: {
                        angelEquip.put((byte) 1, itemID);
                        break;
                    }
                    default: {
                        angelEquip.put(b809, itemID);
                        break;
                    }
                }
            }
            if (item.getPosition() <= -5000 && item.getPosition() >= -5002) {
                byte pos = (byte) -5000; //定義圖騰裝備的位置
                totemEquips.putIfAbsent(pos, itemID);
            }
            if (item.getPosition() >= -128) {
                // T069 如果身上裝備為武器 且 武器合成其他道具的外觀 就要外觀武器的ID
                byte pos = (byte) (-item.getPosition()); //定義裝備的位置pos
                if (pos < 100 && equips.get(pos) == null) {
                    if (second && angel && (pos >= 1 && pos <= 9 || pos == 13)) {
                        continue;
                    }
                    equips.put(pos, itemID);
                } else if ((pos > 100 || pos == -128) && pos != 111) {
                    pos = (byte) (pos == -128 ? 28 : pos - 100);
                    if (second && angel && (pos >= 1 && pos <= 9 || pos == 13)) {
                        continue;
                    }
                    if (equips.get(pos) != null) {
                        maskedEquips.put(pos, equips.get(pos));
                    }
                    equips.put(pos, itemID);
                } else if (equips.get(pos) != null) {
                    maskedEquips.put(pos, itemID);
                }
            }

        }
        if (angel && second) {
            if (!angelEquip.containsKey((byte) 5)) {
                if (chr.getKeyValue("Longcoat") == null) {
                    chr.setKeyValue("Longcoat", "1051291");
                }
                equips.put((byte) 5, Integer.valueOf(chr.getKeyValue("Longcoat")));
                maskedEquips.put((byte) 5, Integer.valueOf(chr.getKeyValue("Longcoat")));
            } else {
                equips.put((byte) 5, angelEquip.get((byte) 5));
                maskedEquips.clear();
            }
            equips.putAll(angelEquip);
            maskedEquips.putAll(angelEquip);
        }
        /*
         * 神之子主手和副手處理
         * 1572000 太刀類型 主手
         * 1562000 太劍類型 副手
         * 10 = 盾牌
         * 11 = 武器
         */
        if (zero) {
            int itemId = 0;
            if (equips.containsKey((byte) 10)) {
                itemId = equips.remove((byte) 10); //刪除盾牌
            }
            if (second) {
                equips.remove((byte) 11);
                if (itemId > 0)
                    equips.put((byte) 11, itemId); //將盾牌裝備放到主手
            }
        }

        Item cWeapon = equip.getItem((byte) -111);
        weaponStickerId = cWeapon != null ? cWeapon.getItemId() : 0;
        Item weapon = equip.getItem(zero && second ? (byte) -10 : (byte) -11); //神之子第2角色 顯示的武器是盾牌的
        weaponId = weapon != null ? weapon.getItemId() : 0;
        Item subWeapon = equip.getItem((byte) -10);
        subWeaponId = !zero && subWeapon != null ? subWeapon.getItemId() : 0;
        String questInfo = chr.getQuestInfo(7784, "sw");
        if (questInfo == null) {
            questInfo = "0";
        }
        int nEar = Integer.parseInt(questInfo);
        ear = JobConstants.getEar(chr.getJob(), nEar);
        drawElfEar = nEar != 0;
        for (int i = 0; i < pets.length; i++) {
            pets[i] = !second && chr.getSpawnPet(i) != null ? chr.getSpawnPet(i).getPetItemId() : 0;
        }
        defFaceAcc = chr.getDecorate();
        isSecondLook = second;
        name = chr.getName();
    }

    public void encode(MaplePacketLittleEndianWriter mplew, boolean mega) {
        OutPacket outPacket = new OutPacket();
        encode(outPacket, mega);
        mplew.write(outPacket.getData());
    }

    public void encode(OutPacket outPacket, boolean mega) {
        outPacket.encodeByte(getGender());
        outPacket.encodeByte(getSkin());
        outPacket.encodeInt(0); // v267 add
        outPacket.encodeInt(getFace());
        outPacket.encodeInt(getJob());
        outPacket.encodeByte(mega ? 0 : 1);
        outPacket.encodeInt(getHair());

        for (Entry<Byte, Integer> entry : equips.entrySet()) {
            outPacket.encodeByte(entry.getKey());
            outPacket.encodeInt(entry.getValue());
        }
        outPacket.encodeByte(0xFF);
        for (Entry<Byte, Integer> entry : maskedEquips.entrySet()) {
            outPacket.encodeByte(entry.getKey());
            outPacket.encodeInt(entry.getValue());
        }
        outPacket.encodeByte(0xFF);
//        for (Entry<Byte, Integer> entry : equipMixColors.entrySet()) {
//            outPacket.encodeByte(entry.getKey());
//            outPacket.encodeInt(entry.getValue());
//        }
//        outPacket.encodeByte(0xFF);
        for (Entry<Byte, Integer> entry : totemEquips.entrySet()) {
            outPacket.encodeByte(entry.getKey());
            outPacket.encodeInt(entry.getValue());
        }
        outPacket.encodeByte(0xFF);

        outPacket.encodeInt(getWeaponStickerId());
        outPacket.encodeInt(getWeaponId());
        outPacket.encodeInt(getSubWeaponId());
        outPacket.encodeInt(ear);
        outPacket.encodeInt(0);
        outPacket.encodeBoolean(drawElfEar);
        outPacket.encodeInt(0);

        for (int pet : pets) { // 12 byte
            outPacket.encodeInt(pet);
        }

        // 148 byte
        outPacket.encodeArr(new byte[148]);

        if (JobConstants.hasDecorate(job)) {
            outPacket.encodeInt(defFaceAcc);
        } else if (JobConstants.is神之子(job) || JobConstants.is天使破壞者(job)) {
            outPacket.encodeBoolean(isSecondLook);
        }

        outPacket.encodeInt(154819);
        outPacket.encodeString(name, 15);
        outPacket.encodeArr(new byte[5]);
    }

    public void decode(MaplePacketReader slea) {
        byte[] now = new byte[0];
        ByteStream bs = slea.getByteStream();
        if (bs instanceof ByteArrayByteStream) {
            now = ((ByteArrayByteStream) bs).getNowBytes();
        }
        InPacket inPacket = new InPacket(now);
        decode(inPacket);
        slea.skip(inPacket.readerIndex());
    }

    public void decode(InPacket inPacket) {
        gender = inPacket.decodeByte();
        skin = inPacket.decodeByte();
        face = inPacket.decodeInt();
        job = inPacket.decodeInt();
        inPacket.decodeByte(); // ignored
        hair = inPacket.decodeInt();

        for (byte i = inPacket.decodeByte(); i != -1; i = inPacket.decodeByte()) {
            equips.put(i, inPacket.decodeInt());
        }
        for (byte i = inPacket.decodeByte(); i != -1; i = inPacket.decodeByte()) {
            maskedEquips.put(i, inPacket.decodeInt());
        }
        for (byte i = inPacket.decodeByte(); i != -1; i = inPacket.decodeByte()) {
            equipMixColors.put(i, inPacket.decodeInt());
        }
        for (byte i = inPacket.decodeByte(); i != -1; i = inPacket.decodeByte()) {
            totemEquips.put(i, inPacket.decodeInt());
        }

        weaponStickerId = inPacket.decodeInt();
        weaponId = inPacket.decodeInt();
        subWeaponId = inPacket.decodeInt();
        ear = inPacket.decodeInt();
        inPacket.decodeInt(); // new 199
        drawElfEar = inPacket.decodeByte() != 0;
        inPacket.decodeInt();

        for (int i = 0; i < 3; i++) {
            pets[i] = inPacket.decodeInt();
        }
        if (JobConstants.hasDecorate(job)) {
            defFaceAcc = inPacket.decodeInt();
        } else if (JobConstants.is神之子(job) || JobConstants.is天使破壞者(job)) {
            isSecondLook = inPacket.decodeByte() != 0;
        }
        inPacket.decodeInt();
        inPacket.decodeArr(5);
    }

    public byte[] getPackedCharacterLook() {
        int[] equipArr = new int[11];
        for (byte i = 0; i < equipArr.length; i++) {
            equipArr[i] = equips.getOrDefault(i, 0);
        }
        int visibleWeaponId = weaponStickerId != 0 ? weaponStickerId : weaponId;
        int weaponType = 0;
        for (MapleWeapon type : MapleWeapon.values()) {
            if (type.getWeaponType() == visibleWeaponId / 1000 % 1000) {
                break;
            }
            if (++weaponType > 36) {
                weaponType = 0;
                break;
            }
        }

        byte[] data = new byte[120];
        encodeArrIndex(data, 0, gender & 1);
        encodeArrIndex(data, 0, (skin & 0x3FF) << 1);
        encodeArrIndex(data, 1, ((face > 0 ? face % 1000 : -1) & 0x3FF) << 3);
        encodeArrIndex(data, 2, (face / 1000 % 10 & 0xF) << 5);
        encodeArrIndex(data, 3, hair / 10000 == 4 ? 2 : 0);
        encodeArrIndex(data, 3, ((hair > 0 ? hair % 1000 : -1) & 0x3FF) << 2);
        encodeArrIndex(data, 4, (hair / 1000 % 10 & 0xF) << 4);
        encodeArrIndex(data, 5, (equipArr[1] > 0 ? equipArr[1] % 1000 : -1) & 0x3FF);
        encodeArrIndex(data, 6, (equipArr[1] / 1000 % 10 & 7) << 2);
        encodeArrIndex(data, 6, ((defFaceAcc > 0 ? defFaceAcc % 1000 : -1) & 0x3FF) << 5);
        encodeArrIndex(data, 7, (defFaceAcc / 1000 % 10 & 3) << 7);
        encodeArrIndex(data, 8, ((equipArr[3] > 0 ? equipArr[3] % 1000 : -1) & 0x3FF) << 1);
        encodeArrIndex(data, 9, (equipArr[3] / 1000 % 10 & 3) << 3);
        encodeArrIndex(data, 9, ((equipArr[4] > 0 ? equipArr[4] % 1000 : -1) & 0x3FF) << 5);
        encodeArrIndex(data, 10, (equipArr[4] / 1000 % 10 & 3) << 7);
        encodeArrIndex(data, 11, equipArr[5] / 10000 == 105 ? 2 : 0);
        encodeArrIndex(data, 11, ((equipArr[5] > 0 ? equipArr[5] % 1000 : -1) & 0x3FF) << 2);
        encodeArrIndex(data, 12, (equipArr[5] / 1000 % 10 & 0xF) << 4);
        encodeArrIndex(data, 13, (equipArr[6] > 0 ? equipArr[6] % 1000 : -1) & 0x3FF);
        encodeArrIndex(data, 14, (equipArr[6] / 1000 % 10 & 3) << 2);
        encodeArrIndex(data, 14, ((equipArr[7] > 0 ? equipArr[7] % 1000 : -1) & 0x3FF) << 4);
        encodeArrIndex(data, 15, (equipArr[7] / 1000 % 10 & 3) << 6);
        encodeArrIndex(data, 16, (equipArr[8] > 0 ? equipArr[8] % 1000 : -1) & 0x3FF);
        encodeArrIndex(data, 17, (equipArr[8] / 1000 % 10 & 3) << 2);
        encodeArrIndex(data, 17, ((equipArr[9] > 0 ? equipArr[9] % 1000 : -1) & 0x3FF) << 4);
        encodeArrIndex(data, 18, (equipArr[9] / 1000 % 10 & 3) << 6);
        encodeArrIndex(data, 19, equipArr[10] > 0 ? equipArr[10] / 10000 == 109 ? 1 : 3 - (equipArr[10] / 10000 == 134 ? 1 : 0) : 0);
        encodeArrIndex(data, 19, ((equipArr[10] > 0 ? equipArr[10] % 1000 : -1) & 0x3FF) << 2);
        encodeArrIndex(data, 20, (equipArr[10] / 1000 % 10 & 0xF) << 4);
        encodeArrIndex(data, 21, visibleWeaponId / 10000 == 170 ? 1 : 0);
        encodeArrIndex(data, 21, ((visibleWeaponId > 0 ? visibleWeaponId % 1000 : -1) & 0x3FF) << 1);
        encodeArrIndex(data, 22, (visibleWeaponId / 1000 % 10 & 3) << 3);
        encodeArrIndex(data, 22, weaponType << 5);
//        encodeArrIndex(data, 23, (getMixBaseHairColor() & 0xF) << 5);
//        encodeArrIndex(data, 24, getMixAddHairColor() & 0xF);
//        encodeArrIndex(data, 24, (getMixHairBaseProb() & 0xF) << 5);
        encodeArrIndex(data, 119, 24);
        return data;
    }

    private void encodeArrIndex(byte[] arr, int index, int value) {
        arr[index] |= (byte) (value & 0xFF);
        if (value > 0xFF && index < arr.length - 1) {
            arr[index + 1] |= (byte) (value >>> 8 & 0xFF);
        }
    }
}
