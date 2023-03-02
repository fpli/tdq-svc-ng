package com.ebay.dap.epic.tdq.data.vo.metric;

import com.ebay.dap.epic.tdq.data.enums.MetricValueType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MetricInfoVO {
    @NotBlank
    private String metricKey;
    @NotBlank
    private String metricName;
    private String description;
    private String stage;
    private String category;
    @NotBlank
    private String level;
    @NotBlank
    private String source;
    @NotNull
    private MetricValueType valueType;
    @NotBlank
    private String collectInterval;
    private String dimension;
    private String dimensionSrcTbl;
    private String dimensionValCol;

}
