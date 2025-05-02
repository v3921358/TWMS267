package Database;

import com.alibaba.druid.pool.DruidPooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnectionEx {

    public static final int RETURN_GENERATED_KEYS = 1;
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionEx.class);

    private static class DatabaseConnectionHolder {
        private static final DatabaseConnectionEx instance = new DatabaseConnectionEx();
    }

    public static DatabaseConnectionEx getInstance() {
        return DatabaseConnectionHolder.instance;
    }

    public static DruidPooledConnection getConnection() throws DatabaseException {
        try {
            return DatabaseLoader.getConnection();
        } catch (DatabaseException e) {
            log.error("取得資料庫連線失敗", e);
            throw e;
        }
    }
}
