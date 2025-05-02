package SwordieX.overseas.extraequip;

import Client.MapleCharacter;
import Client.MonsterFamiliar;
import Net.server.movement.LifeMovementFragment;
import Packet.PacketHelper;
import connection.Encodable;
import connection.OutPacket;
import SwordieX.util.Position;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExtraEquipResult implements Encodable {

    private final static int SINGLE_PACKET = 5;
    private final static int MULTI_PACKET = 7;

    private final int cid;
    private final int resultType;
    private final List<ExtraEquipPacket> packets;

    public ExtraEquipResult(int cid, int resultType) {
        this.cid = cid;
        this.resultType = resultType;
        packets = new LinkedList<>();
    }

    @Override
    public void encode(OutPacket outPacket) {
        outPacket.encodeInt(cid);
        outPacket.encodeByte(resultType);
        if (resultType == SINGLE_PACKET) {
            OutPacket subOutPacket = new OutPacket();
            if (!packets.isEmpty()) {
                ExtraEquipPacket packet = packets.get(0);
                if (!packet.Packets.isEmpty()) {
                    subOutPacket.encodeInt(packet.PacketType.ordinal());
                    encodeSub(subOutPacket, packet.Packets.get(0));
                }
            }
            outPacket.encodeShort(subOutPacket.getLength());
            outPacket.encodeArr(subOutPacket.getData());
        } else if (resultType == MULTI_PACKET) {
            for (ExtraEquipPacket packet : packets) {
                outPacket.encodeInt(packet.PacketType.ordinal());
                outPacket.encodeInt(packet.PacketType.MagicNumber);

                outPacket.encodeByte(!packet.Packets.isEmpty() ? 1 : 2);
                if (!packet.Packets.isEmpty()) {
                    for (ExtraEquipOption subPacket : packet.Packets) {
                        encodeSub(outPacket, subPacket);
                    }
                    outPacket.encodeShort(0);
                }
                outPacket.encodeByte(0);
            }
        }
    }

    public void encodeSub(OutPacket outPacket, ExtraEquipOption option) {
        outPacket.encodeShort(option.getType().getVal());
        switch (option.getType()) {
            case OwnerInfo:
                outPacket.encodeInt(option.getUserID());
                outPacket.encodeInt(option.getCid());
                break;

            // 內面耀光
            case InnerGlareSkills:
                outPacket.encodeReversedVarints(option.getBuffIds().size());
                for (int skillId : option.getBuffIds()) {
                    outPacket.encodeInt(skillId);
                }
                break;

            // 萌獸卡
            case FamiliarSpawnedValue:
                outPacket.encodeInt(option.getIntVal());
                outPacket.encodeInt(0);
                break;
            case FamiliarCardsData:
                outPacket.encodeShort(option.getSize());
                if (option.getFamiliars() != null) {
                    for (Map.Entry<Integer, MonsterFamiliar> entry : option.getFamiliars().entrySet()) {
                        outPacket.encodeShort(entry.getKey() + 1);
                        entry.getValue().encode(outPacket);
                    }
                }
                outPacket.encodeShort(0);
                break;
            case FamiliarTeamStatSelected:
                outPacket.encodeShort(option.getIntVal()); // 0~2 已選擇的組合屬性
                break;
            case FamiliarTeamStats:
                outPacket.encodeShort(3);
                if (option.getShortList() != null) {
                    for (int i = option.getIndex() <= 0 ? 1 : option.getIndex(); i <= option.getShortList().size(); i++) {
                        outPacket.encodeShort(i);
                        outPacket.encodeShort(option.getShortList().get(i - 1)); // 組合屬性 0~21
                        if (option.getIndex() > 0 || i > 3) break;
                    }
                }
                outPacket.encodeShort(0);
                break;
            case FamiliarGainExp:
                outPacket.encodeReversedVarints(option.getIntMap().size());
                for (int val : option.getIntMap().values()) {
                    outPacket.encodeShort(val);
                }
                break;
            case FamiliarUseCardPack:
                outPacket.encodeReversedVarints(option.getFamiliarids().size());
                for (final Pair<Integer, Integer> pair : option.getFamiliarids()) {
                    outPacket.encodeInt(pair.left);
                    outPacket.encodeByte(pair.right);
                    outPacket.encodeInt(0);
                }
                break;
            case FamiliarCardLock:
                outPacket.encodeByte(option.isSuccess());
                outPacket.encodeInt(option.getSn());
                outPacket.encodeByte(option.isLock());
                break;

            // 內面暴風
            case InnerStormSkillValue:
            case InnerStormSkillEffect:
                outPacket.encodeInt(option.getIntVal());
                break;

            // 萌獸實例
            case FamiliarLifeUnk7:
            case FamiliarLifeUnk8:
                outPacket.encodeLong(0);
                break;
            case FamiliarLifeUnk9:
            case FamiliarLifeUnk10:
            case FamiliarLifeUnk11:
            case FamiliarLifeUnk12:
                outPacket.encodeInt(0);
                break;
            case FamiliarLifeUnk13:
            case FamiliarLifeUnk14:
            case FamiliarLifeUnk16:
            case FamiliarLifeUnk18:
                outPacket.encodeByte(0);
                break;
            case FamiliarLifeUnk17:
                outPacket.encodeByte(1);
                break;
            case FamiliarLifeData:
                option.getFamiliar().encode(outPacket);
                break;
            case FamiliarLifePosition:
                outPacket.encodePositionInt(option.getPosition());
                break;
            case FamiliarLifeHp:
            case FamiliarLifeMp:
                outPacket.encodeInt(option.getIntVal());
                break;
            case FamiliarMove:
                MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
                PacketHelper.serializeMovementList(packet, option.getGatherDuration(), option.getNVal1(), option.getOPos(), option.getMPos(), option.getMoveRes(), new int[0]);
                byte[] bs = packet.getPacket();
                outPacket.encodeReversedVarints(bs.length);
                outPacket.encodeArr(bs);
                break;
            case FamiliarAttack:
                outPacket.encodeInt(option.getAnInt());
                outPacket.encodeReversedVarints(option.getIntListMap().size());
                for (Map.Entry<Integer, List<Integer>> entry : option.getIntListMap().entrySet()) {
                    outPacket.encodeInt(entry.getKey());
                    outPacket.encodeReversedVarints(entry.getValue().size());
                    for (int nVal : entry.getValue()) {
                        outPacket.encodeInt(nVal);
                    }
                }
                break;
        }
    }

    public static ExtraEquipResult initSpecialData(MapleCharacter chr) {
        int userId = chr.getAccountID();
        int chrId = chr.getId();
        ExtraEquipResult sr = new ExtraEquipResult(chrId, MULTI_PACKET);
        ExtraEquipPacket entry;
        ExtraEquipOption option;


        ExtraEquipOption ownerOption = new ExtraEquipOption(ExtraEquipType.OwnerInfo);
        ownerOption.setUserID(userId);
        ownerOption.setCid(chrId);
        ownerOption.setSize((byte) 0x00);

        entry = new ExtraEquipPacket(ExtraEquipMagic.SKILL_INNER_GLARE); // -2107976430
        entry.Packets.add(ownerOption); // 穿在身上的solt欄位 1793
        sr.packets.add(entry);

        entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_CARDS);
        entry.Packets.add(ownerOption);
        MonsterFamiliar familiar = chr.getSummonedFamiliar();
        if (familiar != null) {
            option = new ExtraEquipOption(ExtraEquipType.FamiliarSpawnedValue);
            option.setIntVal(ExtraEquipMagic.FAMILIAR_LIFE.ordinal());
            entry.Packets.add(option);
        }
        option = new ExtraEquipOption(ExtraEquipType.FamiliarCardsData);
        option.setSize(chr.getFamiliars().size());
        option.setFamiliars(chr.getFamiliars().stream()
                .collect(Collectors.toMap(chr.getFamiliars()::indexOf, Function.identity())));
        entry.Packets.add(option);
        int nSelectedOption = chr.getSelectedFamiliarTeamStat();
        if (nSelectedOption > 0) {
            option = new ExtraEquipOption(ExtraEquipType.FamiliarTeamStatSelected);
            option.setIntVal((short) (nSelectedOption - 1));
            entry.Packets.add(option);
        }
        option = new ExtraEquipOption(ExtraEquipType.FamiliarTeamStats);
        option.setShortList(chr.getFamiliarTeamStats());
        entry.Packets.add(option);
        sr.packets.add(entry);

        if (chr.checkInnerStormValue()) {
            entry = new ExtraEquipPacket(ExtraEquipMagic.SKILL_INNER_STORM);
            entry.Packets.add(ownerOption);
            sr.packets.add(entry);
        }
        if (familiar != null) {
            entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_LIFE);
            entry.Packets.add(ownerOption);
            sr.packets.add(entry);
        }

        return sr;
    }

    public static ExtraEquipResult spawnFamiliar(int accId, int chrId, boolean hasOld, MonsterFamiliar familiar, Point pos, boolean self) {
        ExtraEquipResult sr = new ExtraEquipResult(chrId, MULTI_PACKET);
        ExtraEquipPacket entry;
        if (hasOld) {
            entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_LIFE);
            sr.packets.add(entry);
        }
        entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_LIFE);
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk7));
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk8));
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk9));
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk10));
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk11));
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk12));
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk13));
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk14));
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.OwnerInfo);
        option.setUserID(accId);
        option.setCid(chrId);
        entry.Packets.add(option);
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk16));
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk17));
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarLifeUnk18));
        option = new ExtraEquipOption(ExtraEquipType.FamiliarLifeData);
        option.setFamiliar(familiar);
        entry.Packets.add(option);
        option = new ExtraEquipOption(ExtraEquipType.FamiliarLifePosition);
        option.setPosition(new Position(pos));
        entry.Packets.add(option);
        option = new ExtraEquipOption(ExtraEquipType.FamiliarLifeHp);
        option.setIntVal(2000);
        entry.Packets.add(option);
        option = new ExtraEquipOption(ExtraEquipType.FamiliarLifeMp);
        option.setIntVal(2000);
        entry.Packets.add(option);
        sr.packets.add(entry);
        if (self) {
            entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_CARDS);
            option = new ExtraEquipOption(ExtraEquipType.FamiliarSpawnedValue);
            option.setIntVal(ExtraEquipMagic.FAMILIAR_LIFE.ordinal());
            entry.Packets.add(option);
            sr.packets.add(entry);
        }
        return sr;
    }

    public static ExtraEquipResult removeFamiliar(int chrId, boolean self) {
        ExtraEquipResult sr = new ExtraEquipResult(chrId, MULTI_PACKET);
        ExtraEquipPacket entry;
        ExtraEquipOption option;
        if (self) {
            entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_CARDS);
            option = new ExtraEquipOption(ExtraEquipType.FamiliarSpawnedValue);
            option.setIntVal(ExtraEquipMagic.NONE.ordinal());
            entry.Packets.add(option);
            sr.packets.add(entry);
        }
        entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_LIFE);
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult updateFamiliarInfo(int chrId, int size, Map<Integer, MonsterFamiliar> familiars) {
        ExtraEquipResult sr = new ExtraEquipResult(chrId, MULTI_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_CARDS);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.FamiliarCardsData);
        option.setSize(size);
        option.setFamiliars(familiars);
        entry.Packets.add(option);
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult changeTeamStatSelected(MapleCharacter chr, int selected) {
        ExtraEquipResult sr = new ExtraEquipResult(chr.getId(), MULTI_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_CARDS);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.FamiliarTeamStatSelected);
        option.setIntVal((short) selected);
        entry.Packets.add(option);
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult changeTeamStats(MapleCharacter chr, int idx, List<Short> options) {
        ExtraEquipResult sr = new ExtraEquipResult(chr.getId(), MULTI_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_CARDS);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.FamiliarTeamStats);
        option.setIndex(idx);
        option.setShortList(options);
        entry.Packets.add(option);
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult equipInnerStorm(int accid, int cid) {
        ExtraEquipResult sr = new ExtraEquipResult(cid, MULTI_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.SKILL_INNER_STORM);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.OwnerInfo);
        option.setUserID(accid);
        option.setCid(cid);
        entry.Packets.add(option);
//        option = new SpecialOption(SpecialPacketType.InnerStormSkillValue);
//        option.setIntVal(0);
//        entry.Packets.add(option);
//        option = new SpecialOption(SpecialPacketType.InnerStormSkillEffect);
//        option.setIntVal(0);
//        entry.Packets.add(option);
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult unequipInnerStorm(int cid) {
        ExtraEquipResult sr = new ExtraEquipResult(cid, MULTI_PACKET);
        sr.packets.add(new ExtraEquipPacket(ExtraEquipMagic.SKILL_INNER_STORM));
        return sr;
    }

    public static ExtraEquipResult updateInnerStormValue(int chrId, int value, int effect) {
        ExtraEquipResult sr = new ExtraEquipResult(chrId, MULTI_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.SKILL_INNER_STORM);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.InnerStormSkillValue);
        option.setIntVal(value);
        entry.Packets.add(option);
        if (effect >= 0) {
            option = new ExtraEquipOption(ExtraEquipType.InnerStormSkillEffect);
            option.setIntVal(effect);
            entry.Packets.add(option);
        }
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult openCardPack(int chrId, List<Pair<Integer, Integer>> familiarids) {
        ExtraEquipResult sr = new ExtraEquipResult(chrId, SINGLE_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_CARDS);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.FamiliarUseCardPack);
        option.setFamiliarids(familiarids);
        entry.Packets.add(option);
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult familiarGainExp(int id, Map<Integer, Integer> hashMap) {
        ExtraEquipResult sr = new ExtraEquipResult(id, SINGLE_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_CARDS);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.FamiliarGainExp);
        option.setIntMap(hashMap);
        entry.Packets.add(option);
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult upgradeFamiliar(int id) {
        ExtraEquipResult sr = new ExtraEquipResult(id, SINGLE_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_CARDS);
        entry.Packets.add(new ExtraEquipOption(ExtraEquipType.FamiliarUpgrade));
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult familiarLock(int id, boolean success, int sn, boolean lock) {
        ExtraEquipResult sr = new ExtraEquipResult(id, SINGLE_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_CARDS);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.FamiliarCardLock);
        option.setSuccess(success);
        option.setSn(sn);
        option.setLock(lock);
        entry.Packets.add(option);
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult familiarMove(int id, final int gatherDuration, final int nVal1, final Point oPos, final Point mPos, final List<LifeMovementFragment> res) {
        ExtraEquipResult sr = new ExtraEquipResult(id, SINGLE_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_LIFE);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.FamiliarMove);
        option.setGatherDuration(gatherDuration);
        option.setNVal1(nVal1);
        option.setOPos(oPos);
        option.setMPos(mPos);
        option.setMoveRes(res);
        entry.Packets.add(option);
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult familiarAttack(int id, final int n, final Map<Integer, List<Integer>> map) {
        ExtraEquipResult sr = new ExtraEquipResult(id, SINGLE_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.FAMILIAR_LIFE);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.FamiliarAttack);
        option.setAnInt(n);
        option.setIntListMap(map);
        entry.Packets.add(option);
        sr.packets.add(entry);
        return sr;
    }

    public static ExtraEquipResult updateInnerGlareSkills(int id, List<Integer> buffIds) {
        ExtraEquipResult sr = new ExtraEquipResult(id, SINGLE_PACKET);
        ExtraEquipPacket entry = new ExtraEquipPacket(ExtraEquipMagic.SKILL_INNER_GLARE);
        ExtraEquipOption option = new ExtraEquipOption(ExtraEquipType.InnerGlareSkills);
        option.setBuffIds(buffIds);
        entry.Packets.add(option);
        sr.packets.add(entry);
        return sr;
    }
}
