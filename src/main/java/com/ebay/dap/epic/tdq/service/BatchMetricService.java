package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.vo.MetricChartVO;
import com.ebay.dap.epic.tdq.data.vo.MetricQueryParamVO;

import java.time.LocalDate;

public interface BatchMetricService {

    MetricChartVO retrieveBatchMetric(MetricQueryParamVO metricQueryParamVO);

    String retrieveDimensionsByMetricKey(String metricKey, LocalDate date) throws Exception;
}
