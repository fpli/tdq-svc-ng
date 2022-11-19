package com.ebay.dap.epic.tdq.data.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_metric_info")
public class MetricInfoEntity {
   private Integer metric_id;
   private String metricKey;
   private String metricName;
   private String desc;
   private String stage;
   private String group;
   private String category;
   private String level;
   private String source;
   private String valueType;
   private String interval;
   private String dimension;
   private String dimensionSrcTbl;
   private String dimensionValCol;
   private Integer version;
}
