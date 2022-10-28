package com.ebay.dap.epic.tdq.security.keystone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeystonePasswordCredentials {
  private String username;
  private String password;
  @Builder.Default
  private String type = "tfa";
}
