/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.channel;

import Database.DatabaseLoader.DatabaseConnectionEx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DojoRankingsDataHelper {
    public static class RankingsData {
        public long time;
        public int rank;
        public String name;
    }

    private static final DojoRankingsDataHelper instance = new DojoRankingsDataHelper();
    private static final int limit = 25;
    public static final HashMap<String, RankingsData> DojoRankingsData = new HashMap<>();
    public int totalCharacters = 0;

    public static DojoRankingsDataHelper getInstance() {
        return instance;
    }

    public static HashMap<String, RankingsData> loadLeaderboard() {
        DojoRankingsDataHelper ret = new DojoRankingsDataHelper();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT `name`, `time` FROM `dojorankings` ORDER BY `time` ASC LIMIT " + limit);
            ResultSet rs = ps.executeQuery();

            int i = 0;
            while (rs.next()) {
                if (rs.getInt("time") != 0) {
                    RankingsData RankingsData_ = new RankingsData();
                    RankingsData_.rank = (i + 1);
                    RankingsData_.time = rs.getInt("time");
                    RankingsData_.name = rs.getString("name");
                    DojoRankingsData.put(RankingsData_.name, RankingsData_);
                    //long time = (rs.getLong("endtime") - rs.getLong("starttime")) / 1000;
                    // ret.times[i] = time;
                    ret.totalCharacters++;
                    i++;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return DojoRankingsData;
    }
}
