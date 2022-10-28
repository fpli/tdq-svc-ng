package com.ebay.dap.epic.tdq.security.keystone.response;

import lombok.Data;

@Data
public class KeystoneAccess {
  private KeystoneToken token;
  private KeystoneUser user;
}
