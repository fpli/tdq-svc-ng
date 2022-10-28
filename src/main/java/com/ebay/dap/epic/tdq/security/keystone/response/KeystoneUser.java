package com.ebay.dap.epic.tdq.security.keystone.response;

import java.util.List;
import lombok.Data;

@Data
public class KeystoneUser {
  private String username;
  private String name;
  List<KeystoneRole> roles;
  List<KeystoneGroup> groups;
  private String id;
}
