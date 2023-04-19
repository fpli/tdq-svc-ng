package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.vo.MetricChartVO;
import com.ebay.dap.epic.tdq.data.vo.MetricQueryParamVO;
import com.ebay.dap.epic.tdq.data.vo.ScorecardItemVO;

import java.time.LocalDate;
import java.util.List;

public interface BatchMetricService {

    MetricChartVO retrieveBatchMetric(MetricQueryParamVO metricQueryParamVO);

    String retrieveDimensionsByMetricKey(String metricKey, LocalDate date) throws Exception;

    List<ScorecardItemVO> listMetric(String metricKey, LocalDate localDate, LocalDate date);
}
