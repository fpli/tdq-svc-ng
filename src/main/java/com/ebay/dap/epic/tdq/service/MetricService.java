package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.vo.metric.MetricInfoVO;

import java.time.LocalDate;
import java.util.List;

public interface MetricService {

    MetricInfoVO create(MetricInfoVO metricInfoVO);

    List<MetricInfoEntity> listMetricAllInfo();

    MetricInfoEntity getMetricInfoEntityByMetricKey(String metricKey);

    List<MetricDoc> getDailyMetrics(LocalDate dt, String metricKey);

    List<MetricDoc> getDailyMetricsByLabel(LocalDate dt, String label);
}
