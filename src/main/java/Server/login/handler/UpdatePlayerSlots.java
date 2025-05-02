package Server.login.handler;

import Database.DatabaseLoader.DatabaseConnectionEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UpdatePlayerSlots {

    private static final Logger log = LoggerFactory.getLogger(UpdatePlayerSlots.class);

    public static void handlePacket(MaplePacketReader lea) {
        /*
         * 02 00 00 00 01 05 00 00 00 06 00 00 00 05 00 00 00 28 00 00 00 2C 00 00 00 2D 00 00 00
         * 02 00 00 00 01 05 00 00 00 05 00 00 00 28 00 00 00 06 00 00 00 2C 00 00 00 2D 00 00 00
         */
        lea.readInt(); // accid
        lea.skip(1);
        int size = lea.readInt();
        List<Integer> playerID = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            playerID.add(lea.readInt());
        }

        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET position = ? WHERE id = ?")) {
                for (int i = 0; i < playerID.size(); i++) {
                    ps.setInt(1, i);
                    ps.setInt(2, playerID.get(i));
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            log.error("變更角色位置出錯", e);
        }
    }
}
