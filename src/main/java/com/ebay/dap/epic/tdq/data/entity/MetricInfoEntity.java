package com.ebay.dap.epic.tdq.data.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.enums.MetricValueType;
import lombok.Data;

@Data
@TableName("t_metric_info")
public class MetricInfoEntity extends AuditableEntity {
  private String metricKey;
  private String metricName;
  private String description;
  private String stage;
  private String category;
  private String level;
  private String source;
  private MetricValueType valueType;
  private String collectInterval;
  private String dimension;
  private String dimensionSrcTbl;
  private String dimensionValCol;
  private Integer status;
  private Integer version;
}
