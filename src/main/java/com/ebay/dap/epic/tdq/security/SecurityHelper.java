package com.ebay.dap.epic.tdq.security;

import java.security.Principal;

public class SecurityHelper {

  public static String getUsername(Principal user) {
    return user == null ? "Anonymous" : user.getName();
  }
}
