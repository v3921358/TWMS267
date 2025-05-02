package Server.world.guild;

import SwordieX.enums.GuildAuthorityType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MapleGuildGrade {
    private int id;
    private String name;
    private int authority;

    public static GuildAuthorityType DefaultAuthority = GuildAuthorityType.UseSkill;

    public MapleGuildGrade() {
        name = "";
        authority = DefaultAuthority.getVal();
    }

    public MapleGuildGrade(String sName, int nAuthority) {
        name = sName;
        authority = nAuthority;
    }
}
