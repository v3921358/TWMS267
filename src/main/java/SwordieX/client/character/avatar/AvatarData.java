package SwordieX.client.character.avatar;

import Client.MapleCharacter;
import Config.constants.JobConstants;
import SwordieX.client.character.CharacterStat;
import connection.OutPacket;
import lombok.Getter;
import lombok.Setter;
import tools.data.MaplePacketLittleEndianWriter;

@Setter
@Getter
public class AvatarData {
    private CharacterStat characterStat;
    private AvatarLook avatarLook;
    private AvatarLook secondAvatarLook;

    public AvatarData(MapleCharacter chr) {
        characterStat = new CharacterStat(chr);
        avatarLook = new AvatarLook(chr, false);
        secondAvatarLook = new AvatarLook(chr, true);
    }

    public void encode(MaplePacketLittleEndianWriter mplew) {
        OutPacket outPacket = new OutPacket();
        encode(outPacket);
        mplew.write(outPacket.getData());
    }

    public void encode(OutPacket outPacket) {
        characterStat.encode(outPacket);
        outPacket.encodeInt(8);
        outPacket.encodeInt(0);
        outPacket.encodeLong(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0); // -1
        outPacket.encodeByte(false);
        outPacket.encodeByte(false);
        outPacket.encodeString("");
        outPacket.encodeByte(false);
        outPacket.encodeString("");
        outPacket.encodeLong(0);
        outPacket.encodeByte(0);
        avatarLook.encode(outPacket, true);
        if (JobConstants.is神之子(getCharacterStat().getJob())) {
            secondAvatarLook.encode(outPacket, true);
        }
    }
}
