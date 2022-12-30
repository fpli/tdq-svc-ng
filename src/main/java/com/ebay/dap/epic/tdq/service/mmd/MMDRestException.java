package com.ebay.dap.epic.tdq.service.mmd;

public class MMDRestException extends Exception {

    public MMDRestException() {
    }

    public MMDRestException(String message) {
        super(message);
    }

    public MMDRestException(String message, Throwable cause) {
        super(message, cause);
    }

    public MMDRestException(Throwable cause) {
        super(cause);
    }

    public MMDRestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}