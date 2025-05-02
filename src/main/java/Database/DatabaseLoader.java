package Database;

import Config.configs.Config;
import Config.configs.ServerConfig;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.util.Collections;
import java.util.Scanner;


public class DatabaseLoader {
    private static final DruidDataSource druidDataSource = new DruidDataSource();
    public static final Scanner scanner = new Scanner(System.in);

    static {
        checkConnection();

        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl(getDBUrl());
        druidDataSource.setUsername(ServerConfig.DB_USER);
        druidDataSource.setPassword(ServerConfig.DB_PASSWORD);
        Slf4jLogFilter filter = new Slf4jLogFilter();
//        Log4j2Filter filter = new Log4j2Filter();
        filter.setConnectionLogEnabled(false);
        filter.setStatementLogEnabled(false);
        filter.setResultSetLogEnabled(false);
        filter.setStatementExecutableSqlLogEnable(true);
        druidDataSource.setProxyFilters(Collections.singletonList(filter));
        druidDataSource.setInitialSize(ServerConfig.DB_INITIALPOOLSIZE);
        druidDataSource.setMinIdle(ServerConfig.DB_MINPOOLSIZE);
        druidDataSource.setMaxActive(ServerConfig.DB_MAXPOOLSIZE);
        druidDataSource.setMaxWait(60000);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);
        druidDataSource.setRemoveAbandoned(true);
        druidDataSource.setRemoveAbandonedTimeoutMillis(ServerConfig.DB_TIMEOUT);
        druidDataSource.setValidationQuery("SELECT 1 FROM dual");
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setPoolPreparedStatements(false);
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
    }

    private static String getDBUrl() {
        return "jdbc:mysql://" + ServerConfig.DB_IP + ":" + ServerConfig.DB_PORT + "/" + ServerConfig.DB_NAME + "?autoReconnect=true&characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull&useSSL=false";
    }

    private static void checkConnection() {
        try {
            DriverManager.getConnection(getDBUrl(), ServerConfig.DB_USER, ServerConfig.DB_PASSWORD).close();
        } catch (SQLException e) {
            System.err.println("無法連結到資料庫, 請先設定資料庫訊息");

            System.err.print("主機:");
            ServerConfig.DB_IP = scanner.next();
            Config.setProperty("db.ip", ServerConfig.DB_IP);

            System.err.print("埠:");
            ServerConfig.DB_PORT = scanner.next();
            Config.setProperty("db.port", ServerConfig.DB_PORT);

            System.err.print("資料庫名稱:");
            ServerConfig.DB_NAME = scanner.next();
            Config.setProperty("db.name", ServerConfig.DB_NAME);

            System.err.print("使用者名稱:");
            ServerConfig.DB_USER = scanner.next();
            Config.setProperty("db.user", ServerConfig.DB_USER);

            System.err.print("密碼:");
            ServerConfig.DB_PASSWORD = scanner.next();
            Config.setProperty("db.password", ServerConfig.DB_PASSWORD);

            checkConnection();
        }
    }

    public static DruidPooledConnection getConnection() {
        try {
            return druidDataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static void closeAll() {
        druidDataSource.close();
    }

    public static void restart() {
        if (druidDataSource.isClosed()) {
            try {
                druidDataSource.restart();
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }
    }

    public static class DatabaseConnection implements AutoCloseable {

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


    public static class DatabaseConnectionEx {

        public static final int RETURN_GENERATED_KEYS = 1;
        private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionEx.class);

        private static class DatabaseConnectionHolder {
            private static final DatabaseConnectionEx instance = new DatabaseConnectionEx();
        }

        public static DatabaseConnectionEx getInstance() {
            return DatabaseConnectionHolder.instance;
        }

        public DruidPooledConnection getConnection() throws DatabaseException {
            try {
                return DatabaseLoader.getConnection();
            } catch (DatabaseException e) {
                log.error("取得資料庫連線失敗", e);
                throw e;
            }
        }
    }

    public static class FileTimeAttributeConverter implements AttributeConverter<FileTime, Timestamp> {
        private static final LocalDateTime MAX_LDT = LocalDateTime.of(LocalDate.of(9999, 1, 1), LocalTime.of(0, 0, 1));
        private static final LocalDateTime MIN_LDT = LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.of(0, 0, 1));

        @Override
        public Timestamp convertToDatabaseColumn(FileTime fileTime) {
            Instant instant;
            if (fileTime == null) {
                instant = null;
            } else if (fileTime.toMillis() == FileTime.fromMillis(Long.MAX_VALUE).toMillis()) {
                instant = FileTimeAttributeConverter.MAX_LDT.atZone(ZoneId.systemDefault()).toInstant();
            } else if (fileTime.toMillis() == FileTime.fromMillis(Long.MIN_VALUE).toMillis()) {
                instant = FileTimeAttributeConverter.MIN_LDT.atZone(ZoneId.systemDefault()).toInstant();
            } else {
                instant = fileTime.toInstant();
            }
            return instant == null ? null : Timestamp.from(instant);
        }

        @Override
        public FileTime convertToEntityAttribute(Timestamp ts) {
            LocalDateTime ldt = null;
            if (ts != null) {
                ldt = ts.toLocalDateTime();
            }
            FileTime ft;
            if (ldt == null) {
                ft = null;
            } else if (ldt.getYear() == FileTimeAttributeConverter.MAX_LDT.getYear()) {
                ft = FileTime.fromMillis(Long.MAX_VALUE);
            } else if (ldt.getYear() == FileTimeAttributeConverter.MIN_LDT.getYear()) {
                ft = FileTime.fromMillis(Long.MIN_VALUE);
            } else {
                ft = FileTime.fromMillis(ts.getTime());
            }
            return ft;
        }
    }
}
