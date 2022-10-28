package com.ebay.dap.epic.tdq.security.model;

import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@AllArgsConstructor
public enum Role {

  // admin role, has all privileges
  ADMIN(1, new SimpleGrantedAuthority("ADMIN")),

  // normal user
  USER(2, new SimpleGrantedAuthority("USER"));


  private final int code;
  private final SimpleGrantedAuthority authority;

  public static Role fromCode(int code) {
    for (Role value : Role.values()) {
      if (value.code == code) {
        return value;
      }
    }
    throw new IllegalArgumentException("Couldn't find Role of " + code);
  }

  public String getCode() {
    return String.valueOf(code);
  }

  public SimpleGrantedAuthority getAuthority() {
    return this.authority;
  }

}
