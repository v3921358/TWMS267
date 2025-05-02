package SwordieX.client.party;

import Client.MapleCharacter;
import connection.Encodable;
import connection.OutPacket;
import SwordieX.util.Position;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

public class PartyMember implements Encodable {
    @Getter
    private MapleCharacter chr;
    @Setter
    @Getter
    private int partyBossCharacterID;
    @Setter
    @Getter
    private TownPortal townPortal;
    @Getter
    @Setter
    private int charID;
    @Setter
    private String name;
    @Getter
    @Setter
    private int job;
    @Getter
    @Setter
    private int subJob;
    @Getter
    @Setter
    private int hp;
    @Getter
    @Setter
    private int level;
    @Setter
    @Getter
    private int channel;
    @Setter
    @Getter
    private int fieldID;
    @Setter
    @Getter
    private int unk2;
    @Setter
    @Getter
    private int unk3;
    @Setter
    @Getter
    private int unk4;
    @Setter
    @Getter
    private int unk5;
    @Setter
    @Getter
    private int unk6;
    @Setter
    @Getter
    private int unk7;

    public PartyMember(MapleCharacter chr) {
        this.chr = chr;
        updateInfoByChar(chr);
    }

    public String getCharName() {
        return name;
    }

    public boolean isOnline() {
        return channel >= 0 && chr != null && chr.isOnline();
    }

    public void setChr(MapleCharacter chr) {
        this.chr = chr;
        updateInfoByChar(chr);
    }

    public void updateInfoByChar(MapleCharacter chr) {
        if (chr != null) {
            this.chr = chr;
            setCharID(chr.getId());
            setName(chr.getName());
            setJob(chr.getJob());
            setSubJob(chr.getSubcategory());
            setLevel(chr.getLevel());
            setHp(chr.getStat().getHp());
            setChannel(chr.getClient() == null ? -1 : chr.getClient().getChannel());
            setFieldID(chr.getMapId());
            List<Net.server.maps.TownPortal> doors = chr.getTownPortals();
            TownPortal tp = new TownPortal();
            if (!doors.isEmpty()) {
                Net.server.maps.TownPortal door = doors.get(0);
                tp.setTownID(door.getTownMap().getId());
                tp.setFieldID(door.getFieldMap().getId());
                tp.setSkillID(door.getSkillId());
                tp.setFieldPortal(new Position(door.getFieldPosition().x, door.getFieldPosition().y));
            } else {
                tp.setTownID(999999999);
                tp.setFieldID(999999999);
                tp.setFieldPortal(new Position(chr.getPosition().x, chr.getPosition().y));
            }
            setTownPortal(tp);
        } else {
            setFieldID(0);
            setChannel(-1);
            TownPortal tp = new TownPortal();
            tp.setTownID(999999999);
            tp.setFieldID(999999999);
            setTownPortal(tp);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PartyMember && ((PartyMember) obj).getChr().equals(getChr());
    }

    public List<Integer> getChangedTypes(MapleCharacter chr) {
        List<Integer> changedTypes = new LinkedList<>();
        if (chr != null) {
            if (!chr.getName().equalsIgnoreCase(getCharName())) {
                changedTypes.add(0);
            }
            if (chr.getJob() != getJob()) {
                changedTypes.add(1);
            }
            if (chr.getLevel() != getLevel()) {
                changedTypes.add(2);
            }
            if (chr.getClient() == null) {
                if (getChannel() != -1) {
                    changedTypes.add(3);
                }
            } else if (chr.getClient().getChannel() != getChannel()) {
                changedTypes.add(3);
            }
        } else {
            changedTypes.add(3);
        }
        return changedTypes;
    }

    public void encodeVal(OutPacket outPacket, int valType) {
        switch (valType) {
            case 0:
                outPacket.encodeString(name);
                break;
            case 1:
                outPacket.encodeInt(job);
                break;
            case 2:
                outPacket.encodeInt(level);
                break;
            case 3:
                outPacket.encodeInt(channel - 1);
                break;
            case 4:
                outPacket.encodeByte(unk2);
                break;
            case 5:
                outPacket.encodeByte(unk3);
                break;
            case 6:
                outPacket.encodeInt(unk4);
                outPacket.encodeInt(unk5);
                outPacket.encodeInt(unk6);
                outPacket.encodeFT(-2);
                outPacket.encodeInt(unk7);
                break;
            case 7:
                outPacket.encodeByte(0);
                break;
            case 8:
                outPacket.encodeArr(chr == null ? new byte[120] : chr.getAvatarData().getAvatarLook().getPackedCharacterLook());
                break;
        }
    }

    @Override
    public void encode(OutPacket outPacket) {
        outPacket.encodeInt(charID);
        if (charID > 0) {
            outPacket.encodeString(name);
            outPacket.encodeInt(job);
            outPacket.encodeInt(subJob);
            outPacket.encodeInt(level);
            outPacket.encodeInt(channel - 1);
            outPacket.encodeByte(unk2);
            outPacket.encodeByte(unk3);
            outPacket.encodeInt(unk4);
            outPacket.encodeInt(unk5);
            outPacket.encodeInt(unk6);
            //outPacket.encodeFT(FileTime.fromType(FileTime.Type.ZERO_TIME));
            outPacket.encodeLong(unk7);
            outPacket.encodeArr(chr == null ? new byte[120] : chr.getAvatarData().getAvatarLook().getPackedCharacterLook());
        }
    }
}
