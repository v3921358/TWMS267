package Database.tools;

import Database.DatabaseException;
import Database.DatabaseLoader.DatabaseConnection;
import Database.mapper.IMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.rtsp.RtspHeaders.Values.URL;

public class SqlTool {
    public static ResultSet query(Connection con, String query) throws SQLException {
        PreparedStatement stmt = con.prepareStatement(query);
        return stmt.executeQuery();
    }

    public static void domain(DatabaseAction action, String message) {
        try (Connection con = getConnection()) {
            action.execute(con);
            System.out.println(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, "root", "root");
    }


    interface DatabaseAction {
        Object execute(Connection con) throws Exception;
    }

    public static void update(final String sql) {
        DatabaseConnection.domain(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.executeUpdate();
            }
            return null;
        });
    }

    public static void update(Connection con, final String sql) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void update(final String sql, final Object... values) {
        DatabaseConnection.domain(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                compile(ps, values);
                ps.executeUpdate();
            }
            return null;
        });
    }

    public static void update(Connection con, final String sql, final Object... values) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            compile(ps, values);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Object updateAndGet(final String sql, final Object... values) {
        return updateAndGet(sql, 1, values);
    }

    public static Object updateAndGet(final String sql, final int columnIndex, final Object... values) {
        return DatabaseConnection.domain(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                compile(ps, values);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getObject(columnIndex);
                    }
                    return -1;
                }
            }
        });
    }

    public static int executeUpdate(final String sql, final Object... values) {
        return DatabaseConnection.domain(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                compile(ps, values);
                return ps.executeUpdate();
            }
        });
    }

    public static int executeUpdate(Connection con, final String sql, final Object... values) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            compile(ps, values);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }


    public static ResultSet query(Connection con, final String sql, Object... values) {
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            compile(ps, values);
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static <T> T queryAndGet(final String sql, final IMapper<T> rso) {
        return DatabaseConnection.domain(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rso.mapper(rs);
                }
            }
            return null;
        });
    }

    public static <T> T queryAndGet(Connection con, final String sql, final IMapper<T> rso) {
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rso.mapper(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return null;
    }

    public static <T> T queryAndGet(final String sql, final IMapper<T> rso, final Object... values) {
        return DatabaseConnection.domain(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                compile(ps, values);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rso.mapper(rs);
                    }
                }
            }
            return null;
        });
    }

    public static <T> T queryAndGet(Connection con, final String sql, final IMapper<T> rso, final Object... values) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            compile(ps, values);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rso.mapper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return null;
    }

    public static <T> List<T> queryAndGetList(final String sql, final IMapper<T> rso) {
        return DatabaseConnection.domain(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(rso.mapper(rs));
                }
                return list;
            }
        });
    }

    public static <T> List<T> queryAndGetList(Connection con, final String sql, final IMapper<T> rso) {
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rso.mapper(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static <T> List<T> queryAndGetList(final String sql, final IMapper<T> rso, final Object... values) {
        return DatabaseConnection.domain(con -> {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                compile(ps, values);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(rso.mapper(rs));
                    }
                    return list;
                }
            }
        });
    }

    public static <T> List<T> queryAndGetList(Connection con, final String sql, final IMapper<T> rso, final Object... values) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            compile(ps, values);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(rso.mapper(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static List<Map<String, Object>> customSqlResult(final String sql, final Object... values) {
        return DatabaseConnection.domainThrowsException(con -> {
            List<Map<String, Object>> list = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                compile(ps, values);
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        for (int i = 0; i < metaData.getColumnCount(); i++) {
                            String column = metaData.getColumnLabel(i + 1);
                            map.put(column, rs.getObject(column));
                        }
                        if (!map.isEmpty()) {
                            list.add(map);
                        }
                    }
                }
            }
            return list;
        });
    }

    public static boolean next(ResultSet rs) {
        try {
            return rs.next();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private static void compile(final PreparedStatement ps, final Object... values) {
        try {
            for (int i = 0; i < values.length; ++i) {
                ps.setObject(i + 1, values[i]);
            }
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }
}
