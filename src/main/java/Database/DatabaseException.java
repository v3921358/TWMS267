package Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger("Database");
    private static final long serialVersionUID = -420103154764822555L;

    public DatabaseException(String msg) {
        super(msg);
    }

    public DatabaseException(Exception e) {
        super(e);
        log.error("數據庫錯誤", e);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
