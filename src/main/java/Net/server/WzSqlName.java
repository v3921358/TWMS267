package Net.server;

import Config.constants.ServerConstants;
import Database.DatabaseException;
import Database.tools.SqlTool;
import Plugin.provider.MapleDataProviderFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public enum WzSqlName {
    wz_delays,
    wz_skilldata,
    wz_skillsbyjob,
    wz_summonskill,
    wz_mountids,
    wz_familiarskill,
    wz_craftings,
    wz_finalattacks,
    wz_itemdata,
    wz_maplinknpcs,
    wz_questdata,
    wz_questactitemdata,
    wz_questactskilldata,
    wz_questactquestdata,
    wz_questreqdata,
    wz_questpartydata,
    wz_questactdata,
    wz_questcount,
    wz_npcnames,
    wz_mobskilldata;


    static String[] names() {
        WzSqlName[] values = values();
        int len = values.length;
        String[] names = new String[len];
        for (int i = 0; i < len; i++) {
            names[i] = values[i].name();
        }
        return names;
    }

    public boolean check(Connection con) {
        synchronized (WzSqlName.class) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `wztosqllog` WHERE `version` = ?")) {
                String hfc = MapleDataProviderFactory.getHotfixCheck();
                ps.setInt(1, ServerConstants.MapleMajor);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (Objects.equals(rs.getString("hotfix_check"), hfc)) {
                            return rs.getBoolean(this.name());
                        } else {
                            SqlTool.update(con, "DELETE FROM `wztosqllog` WHERE `version` = " + ServerConstants.MapleMajor);
                        }
                    }
                } catch (Exception e) {
                    SqlTool.update(con, "DROP TABLE IF EXISTS `wztosqllog`");
                    // checkTableisExist_wz();
                }
                StringBuilder s = new StringBuilder("INSERT INTO `wztosqllog` VALUES(").append(ServerConstants.MapleMajor).append(",").append(hfc == null ? "NULL" : "\"" + hfc + "\"");
                String[] names = names();
                for (int i = 0; i < names.length; i++) {
                    s.append(",false");
                }
                try {
                    SqlTool.update(con, s.append(")").toString());
                } catch (DatabaseException e) {
                    SqlTool.update(con, "DROP TABLE IF EXISTS `wztosqllog`");
                }

                return false;
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }
    }


    public synchronized void update(Connection con) {

        SqlTool.update(con, "UPDATE `wztosqllog` SET `" + name() + "` = ? WHERE `version` = ?", true, ServerConstants.MapleMajor);
    }

    public synchronized void drop(Connection con) {
        SqlTool.update(con, "DROP TABLE IF EXISTS `" + name() + "`");
    }
}
