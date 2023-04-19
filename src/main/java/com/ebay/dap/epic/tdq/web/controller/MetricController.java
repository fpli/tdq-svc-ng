package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.vo.MetricChartVO;
import com.ebay.dap.epic.tdq.data.vo.MetricQueryParamVO;
import com.ebay.dap.epic.tdq.data.vo.ScorecardItemVO;
import com.ebay.dap.epic.tdq.data.vo.metric.MetricInfoVO;
import com.ebay.dap.epic.tdq.service.BatchMetricService;
import com.ebay.dap.epic.tdq.service.MetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/metric")
@Slf4j
@Tag(name = "Metric API", description = "Metric related API")
public class MetricController {

    @Autowired
    private MetricService metricService;
    @Autowired
    private BatchMetricService batchMetricService;


    @Operation(summary = "create a new metric")
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetricInfoVO> create(@RequestBody @Valid MetricInfoVO metricInfoVO) {
        MetricInfoVO saved = metricService.create(metricInfoVO);
        return ResponseEntity.ok(saved);
    }


    @Operation(summary = "list all metric metadata")
    @GetMapping("/listMetricAllInfo")
    public List<MetricInfoEntity> listMetricAllInfo() {
        return metricService.listMetricAllInfo();
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

    @Operation(summary = "list metric data")
    @GetMapping("listMetric")
    public List<ScorecardItemVO> listMetric(String metricKey){
        LocalDate date = LocalDate.now();
        return batchMetricService.listMetric(metricKey, date.minusMonths(1), date);
    }
}
