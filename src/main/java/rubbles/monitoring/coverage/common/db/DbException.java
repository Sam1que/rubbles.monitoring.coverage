package rubbles.monitoring.coverage.common.db;

import rubbles.monitoring.coverage.common.core.CommonException;

public class DbException extends CommonException {
    public static final int UNKNOWN_ERROR = 1100;
    public static final int INVALID_TABLE_NAME = 1101;
    public static final int SELECT_ERROR = 1102;
    public static final int INSERT_ERROR = 1103;
    public static final int ENRICH_ERROR = 1104;

    public DbException(int code) {
        super(code);
    }

    public DbException(String message, int code) {
        super(message, code);
    }

    public DbException(String message, Throwable cause, int code) {
        super(message, cause, code);
    }

    public DbException(Throwable cause, int code) {
        super(cause, code);
    }

    public DbException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int code) {
        super(message, cause, enableSuppression, writableStackTrace, code);
    }
}
