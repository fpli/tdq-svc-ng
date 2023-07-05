package com.ebay.dap.epic.tdq.service.mmd;

public class MMDException extends Exception {

    public MMDException() {
    }

    public MMDException(String message) {
        super(message);
    }

    public MMDException(String message, Throwable cause) {
        super(message, cause);
    }

    public MMDException(Throwable cause) {
        super(cause);
    }

    public MMDException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}