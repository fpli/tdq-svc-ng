package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.vo.MetricChartVO;
import com.ebay.dap.epic.tdq.data.vo.MetricQueryParamVO;
import com.ebay.dap.epic.tdq.service.BatchMetricService;
import com.ebay.dap.epic.tdq.service.MetricInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/metric")
@Slf4j
@Tag(name = "Metric API", description = "Metric related API")
public class MetricController {

    @Autowired
    private MetricInfoService metricInfoService;
    @Autowired
    private BatchMetricService batchMetricService;


    @Operation(summary = "list all metric metadata")
    @GetMapping("/listMetricAllInfo")
    public List<MetricInfoEntity> listMetricAllInfo() {
        return metricInfoService.listMetricAllInfo();
    }

    @Operation(summary = "get metric for batch")
    @PostMapping("/retrieveBatchMetric")
    public MetricChartVO retrieveBatchMetric(@RequestBody MetricQueryParamVO metricQueryParamVO) {
        return batchMetricService.retrieveBatchMetric(metricQueryParamVO);
    }

    @Operation(summary = "get tag metadata")
    @GetMapping("/retrieveDimensionsOfMetric")
    public String retrieveDimensionsOfMetric(String metricKey, LocalDate date) throws Exception {
        return batchMetricService.retrieveDimensionsByMetricKey(metricKey, date);
    }

}
