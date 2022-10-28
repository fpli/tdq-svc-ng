package com.ebay.dap.epic.tdq.security.keystone;

import org.springframework.security.core.AuthenticationException;

public class KeystoneNoPermissionException extends AuthenticationException {

  public KeystoneNoPermissionException(String msg, Throwable t) {
    super(msg, t);
  }

  public KeystoneNoPermissionException(String msg) {
    super(msg);
  }
}
