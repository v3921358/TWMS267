package Plugin.script.binding;

import Database.DatabaseLoader;
import Packet.GuildPacket;
import Server.world.guild.MapleGuild;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
public class ScriptGuild {
    @Getter
    private MapleGuild guild;

    public ScriptGuild(MapleGuild guild) {
        this.guild = guild;
    }

    public int getId() {
        return getGuild().getId();
    }

    public String getName() {
        return getGuild().getName();
    }

    public int getCapacity() {
        return getGuild().getCapacity();
    }

    public int getLevel() {
        return getGuild().getLevel();
    }

    //    public void setSp(int sp){
    //
    //    }

    //    public int getSp(){
    //
    //    }

    public void GainGP(int diff) {
        getGuild().gainGP(diff);
    }

    //    public void resetSkill(boolean hyper){
    //
    //    }


    public int getSkillLevel(int skillId) {
        return getGuild().getSkillLevel(skillId);
    }


    public boolean increaseCapacity(int amount) {
        int Capacity = getGuild().getCapacity() + amount;
        getGuild().broadcast(GuildPacket.guildCapacityChange(getGuild().getId(), Capacity));

        try (Connection con = DatabaseLoader.DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE guilds SET capacity = ? WHERE guildid = ?");
            ps.setInt(1, Capacity);
            ps.setInt(2, getGuild().getId());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            log.error("[MapleGuild] Saving guild capacity ERROR." + e);
            return false;
        }
        return true;
    }

}
