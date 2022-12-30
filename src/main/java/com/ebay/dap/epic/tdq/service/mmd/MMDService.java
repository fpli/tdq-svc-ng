package com.ebay.dap.epic.tdq.service.mmd;


import java.util.List;
import java.util.Map;

public interface MMDService {

//  void updateBoundByMmd(int metricId, DateTime checkDateTime, String configType) throws Exception;

    void bulkFindAnomalyDaily(String configKey, Map<String, List<Series>> mmdTimeSeries) throws MMDRestException;

    MMDResult mmdCallInBatch(Map<String, List<Series>> mmdTimeSeries, int n, Long count) throws Exception;

    String testMMDRestAPI(String body) throws Exception;

    //void fillMMDBound(MetricSummarySubHour metricSummarySubHour, List<MetricSummarySubHour> metricSummaries) throws Exception;
}
