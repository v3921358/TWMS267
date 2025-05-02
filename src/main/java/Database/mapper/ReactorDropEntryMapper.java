package Database.mapper;

import Net.server.maps.ReactorDropEntry;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReactorDropEntryMapper implements IMapper<ReactorDropEntry> {
    @Override
    public ReactorDropEntry mapper(ResultSet rs) throws SQLException {
        return new ReactorDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("questid"), rs.getInt("minimum"), rs.getInt("maximum"));
    }
}
