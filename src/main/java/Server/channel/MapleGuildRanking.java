package Server.channel;

import Database.DatabaseLoader.DatabaseConnectionEx;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class MapleGuildRanking {

    @Getter
    private static final MapleGuildRanking instance = new MapleGuildRanking();
    private final List<GuildRankingInfo> ranks = new LinkedList<>();

    public void load() {
        if (ranks.isEmpty()) {
            reload();
        }
    }

    public List<GuildRankingInfo> getRank() {
        return ranks;
    }

    private void reload() {
        ranks.clear();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds ORDER BY `GP` DESC LIMIT 50");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                GuildRankingInfo rank = new GuildRankingInfo(
                        rs.getString("name"),
                        rs.getInt("GP"),
                        rs.getInt("logo"),
                        rs.getInt("logoColor"),
                        rs.getInt("logoBG"),
                        rs.getInt("logoBGColor"));

                ranks.add(rank);
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error handling guildRanking" + e);
        }
    }

    @Getter
    public static class GuildRankingInfo {

        private final String name;
        private final int gP;
        private final int logo;
        private final int logoColor;
        private final int logoBG;
        private final int logoBGColor;

        public GuildRankingInfo(String name, int gp, int logo, int logocolor, int logobg, int logobgcolor) {
            this.name = name;
            this.gP = gp;
            this.logo = logo;
            this.logoColor = logocolor;
            this.logoBG = logobg;
            this.logoBGColor = logobgcolor;
        }

    }
}
