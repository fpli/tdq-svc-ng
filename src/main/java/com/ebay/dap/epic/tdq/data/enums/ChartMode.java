package com.ebay.dap.epic.tdq.data.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChartMode {

  SINGLE(1, "single"),

  COMPOSE(2, "compose"),

  DIFF(3, "diff");

  @EnumValue
  private final int code;
  private final String name;
}
