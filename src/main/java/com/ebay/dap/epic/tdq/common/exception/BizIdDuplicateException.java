package com.ebay.dap.epic.tdq.common.exception;

public class BizIdDuplicateException extends ApiException {

  public BizIdDuplicateException() {
  }

  public BizIdDuplicateException(String message) {
    super(message);
  }

  public BizIdDuplicateException(String message, Throwable cause) {
    super(message, cause);
  }

  public BizIdDuplicateException(Throwable cause) {
    super(cause);
  }

  public BizIdDuplicateException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
