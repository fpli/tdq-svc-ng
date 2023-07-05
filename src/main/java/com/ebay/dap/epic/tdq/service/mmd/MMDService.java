package com.ebay.dap.epic.tdq.service.mmd;


import com.ebay.dap.epic.tdq.data.entity.AnomalyItemEntity;

import java.util.List;
import java.util.Map;

public interface MMDService {

    // FIXME: do not return entity
    List<AnomalyItemEntity> bulkFindAnomalyDaily(String configKey, Map<String, List<Series>> mmdTimeSeries) throws MMDException;

    MMDResult mmdCallInBatch(Map<String, List<Series>> mmdTimeSeries, int n, Long count) throws Exception;

    String testMMDRestAPI(String body) throws Exception;

}
