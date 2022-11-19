package com.ebay.dap.epic.tdq.data.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {

  // admin role, has all privileges
  ADMIN(1, "ADMIN"),

  // normal user
  USER(2, "USER");


  @EnumValue
  private final int code;
  private final String role;

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

}
