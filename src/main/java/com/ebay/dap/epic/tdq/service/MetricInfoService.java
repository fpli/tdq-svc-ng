package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;

import java.util.List;

public interface MetricInfoService {

    List<MetricInfoEntity> listMetricAllInfo();

    MetricInfoEntity getMetricInfoEntityByMetricKey(String metricKey);
}
