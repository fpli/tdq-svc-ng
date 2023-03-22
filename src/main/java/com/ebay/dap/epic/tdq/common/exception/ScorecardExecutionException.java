package com.ebay.dap.epic.tdq.common.exception;

public class ScorecardExecutionException extends RuntimeException {

    public ScorecardExecutionException() {
        super();
    }

    public ScorecardExecutionException(String message) {
        super(message);
    }

    public ScorecardExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScorecardExecutionException(Throwable cause) {
        super(cause);
    }

    protected ScorecardExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
