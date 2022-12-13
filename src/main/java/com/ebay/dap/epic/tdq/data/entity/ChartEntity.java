package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.enums.ChartMode;
import lombok.Data;

@Data
@TableName("t_chart_info")
public class ChartEntity extends AuditableEntity {

  private String name;

  private String description;

  private ChartMode mode;

  private String metricKeys;

  private String exp;

  private String viewCfg;

}
