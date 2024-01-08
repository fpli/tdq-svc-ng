package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.pronto.PageMetricDoc;
import com.ebay.dap.epic.tdq.data.vo.metric.MetricInfoVO;

import java.time.LocalDate;
import java.util.List;

public interface MetricService {

    MetricInfoVO create(MetricInfoVO metricInfoVO);

    List<MetricInfoEntity> listMetricAllInfo();

    MetricInfoEntity getMetricInfoEntityByMetricKey(String metricKey);

    List<MetricDoc> getDailyMetrics(LocalDate dt, String metricKey);

    List<MetricDoc> getDailyMetricsByLabel(LocalDate dt, String label);

    List<MetricDoc> getScorecardMetrics(LocalDate dt);

    List<MetricDoc> getDailyMetricSeries(String metricKey, LocalDate endDt, int size);

    List<MetricDoc> getDailyMetricDimensionSeries(String metricKey, LocalDate endDt, int size);

    Boolean dailyMetricExists(String metricKey, LocalDate dt);

    List<PageMetricDoc> getTop50PageMetricDoc(List<Integer> pageIds, LocalDate dt, Integer hr);

}
