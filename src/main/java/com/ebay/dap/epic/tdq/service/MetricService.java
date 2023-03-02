package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.vo.metric.MetricInfoVO;

import java.util.List;

public interface MetricService {

    MetricInfoVO create(MetricInfoVO metricInfoVO);

    List<MetricInfoEntity> listMetricAllInfo();

    MetricInfoEntity getMetricInfoEntityByMetricKey(String metricKey);

}
