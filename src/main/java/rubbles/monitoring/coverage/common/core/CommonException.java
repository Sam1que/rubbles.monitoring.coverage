package rubbles.monitoring.coverage.common.core;

import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {
    public static final int FATAL_ERROR = 1;
    public static final int INTERRUPTED = 2;
    public static final int CONNECTION_ERROR = 3;
    public static final int LISTENER_ERROR = 4;

    private final int code;

    public CommonException(int code) {
        this.code = code;
    }

    public CommonException(String message, int code) {
        super(message);
        this.code = code;
    }

    public CommonException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public CommonException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public CommonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
}
