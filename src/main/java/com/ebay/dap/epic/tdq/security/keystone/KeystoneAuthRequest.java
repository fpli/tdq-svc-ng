package com.ebay.dap.epic.tdq.security.keystone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeystoneAuthRequest {
  private KeystonePasswordCredentials passwordCredentials;
}
