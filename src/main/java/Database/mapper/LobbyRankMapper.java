package Database.mapper;

import Net.server.events.MapleLobbyRank;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LobbyRankMapper implements IMapper<MapleLobbyRank> {

    @Override
    public MapleLobbyRank mapper(ResultSet rs) throws SQLException {
        MapleLobbyRank rank = new MapleLobbyRank();
        rank.playerID = rs.getInt("characters_id");
        rank.playerName = rs.getString("characters_name");
        rank.stage = rs.getInt("stage");
        rank.time = rs.getInt("time");
        rank.world = 0;
        rank.logtime = rs.getTimestamp("logtime").getTime();
        return rank;
    }
}
