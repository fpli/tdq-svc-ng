package com.ebay.dap.epic.tdq.security.keystone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeystoneRequest {
  private KeystoneAuthRequest auth;
}
