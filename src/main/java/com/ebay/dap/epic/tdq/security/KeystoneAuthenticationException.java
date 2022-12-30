package com.ebay.dap.epic.tdq.security;

import org.springframework.security.core.AuthenticationException;

public class KeystoneAuthenticationException extends AuthenticationException {

    public KeystoneAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public KeystoneAuthenticationException(String msg) {
        super(msg);
    }
}
