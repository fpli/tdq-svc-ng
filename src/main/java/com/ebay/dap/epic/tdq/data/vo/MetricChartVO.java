package com.ebay.dap.epic.tdq.data.vo;

import lombok.Data;

import java.util.List;

@Data
public class MetricChartVO {
    String metricKey;
    String date;
    List<MetricValueItemVO> metricValueItemVOList;
}
