package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.enums.ChartMode;
import lombok.Data;

@Data
@TableName("t_chart_info")
public class ChartInfoEntity extends AuditableEntity {

    private String name;

    private String description;

    private ChartMode mode;

    private String metricKeys;

    private String exp;

    private Integer dispOrder;

    private String viewCfg;

}
