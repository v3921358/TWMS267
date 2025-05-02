package Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger("Database");
    private final Connection conn;

    public DatabaseConnection() {
        this(DatabaseLoader.getConnection());
    }

    public DatabaseConnection(final boolean notAutoCommit) {
        this(DatabaseLoader.getConnection(), true);
    }

    private DatabaseConnection(final Connection conn) {
        this.conn = conn;
    }

    private DatabaseConnection(final Connection conn, final boolean notAutoCommit) {
        this.conn = conn;
        try {
            this.conn.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    public final Connection getConnection() {
        return conn;
    }

    public final void commit() {
        try {
            conn.commit();
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    public final void rollback() {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    @Override
    public final void close() {
        try {
            conn.close();
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    public static <T> T domain(DatabaseInterface<T> interfaces) {
        return domain(interfaces, "資料庫異常", false);
    }

    public static <T> T domain(DatabaseInterface<T> interfaces, String errmsg) {
        return domain(interfaces, errmsg, false);
    }

    public static <T> T domain(DatabaseInterface<T> interfaces, String errmsg, boolean needShutdown) {
        T object = null;
        try (DatabaseConnection con = new DatabaseConnection(true)) {
            object = interfaces.domain(con.getConnection());
            con.commit();
        } catch (Throwable e) {
            log.error(errmsg, e);
            if (needShutdown) {
                System.exit(0);
            }
        }
        return object;
    }

    public static <T> T domainThrowsException(DatabaseInterface<T> interfaces) throws DatabaseException {
        T object = null;
        try (DatabaseConnection con = new DatabaseConnection(true)) {
            object = interfaces.domain(con.getConnection());
            con.commit();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
        return object;
    }

    public interface DatabaseInterface<T> {
        T domain(Connection con) throws SQLException;
    }
}
