package Server.world.guild;

import Client.MapleCharacter;
import SwordieX.enums.GuildResponseType;
import lombok.Getter;
import lombok.Setter;

@Getter
public class MapleGuildResultOption {
    private final GuildResponseType type;
    @Setter
    private MapleGuild guild;
    @Setter
    private MapleCharacter character;
    @Setter
    private MapleGuildCharacter member;
    @Setter
    private MapleGuildSkill skill;
    @Setter
    private String tempName;

    public MapleGuildResultOption(GuildResponseType oType) {
        type = oType;
    }

    public static MapleGuildResultOption loadGuild(MapleCharacter oCharacter) {
        MapleGuildResultOption option = new MapleGuildResultOption(GuildResponseType.Res_LoadGuild_Done);
        option.setGuild(oCharacter.getGuild());
        option.setCharacter(oCharacter);
        return option;
    }

    public static MapleGuildResultOption createGuild(MapleCharacter oCharacter) {
        MapleGuildResultOption option = new MapleGuildResultOption(GuildResponseType.Res_CreateNewGuild_Done);
        option.setGuild(oCharacter.getGuild());
        option.setCharacter(oCharacter);
        return option;
    }

    public static MapleGuildResultOption createGuildAgreeReply(String sName) {
        MapleGuildResultOption option = new MapleGuildResultOption(GuildResponseType.Res_CreateGuildAgree_Reply);
        option.setTempName(sName);
        return option;
    }

    public static MapleGuildResultOption setGuildUnk(MapleCharacter oCharacter) {
        MapleGuildResultOption option = new MapleGuildResultOption(GuildResponseType.Res_Unk);
        option.setGuild(oCharacter.getGuild());
        option.setCharacter(oCharacter);
        return option;
    }

    public static MapleGuildResultOption setSkill(MapleCharacter oCharacter, MapleGuildSkill skill) {
        MapleGuildResultOption option = new MapleGuildResultOption(GuildResponseType.Res_SetSkill_Done);
        option.setGuild(oCharacter.getGuild());
        option.setCharacter(oCharacter);
        option.setSkill(skill);
        return option;
    }
}
