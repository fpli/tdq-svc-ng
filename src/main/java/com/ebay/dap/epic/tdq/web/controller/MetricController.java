package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.vo.MetricChartVO;
import com.ebay.dap.epic.tdq.data.vo.MetricQueryParamVO;
import com.ebay.dap.epic.tdq.service.BatchMetricService;
import com.ebay.dap.epic.tdq.service.MetricInfoService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/metric")
@Slf4j
public class MetricController {

    @Autowired
    private MetricInfoService metricInfoService;
    @Autowired
    private BatchMetricService batchMetricService;


    @ApiOperation("list all metric metadata")
    @GetMapping("/listMetricAllInfo")
    public List<MetricInfoEntity> listMetricAllInfo() {
        return metricInfoService.listMetricAllInfo();
    }

    @ApiOperation("get metric for batch")
    @PostMapping("/retrieveBatchMetric")
    @ApiImplicitParam(paramType = "body", dataTypeClass = MetricQueryParamVO.class)
    public MetricChartVO retrieveBatchMetric(@RequestBody MetricQueryParamVO metricQueryParamVO) {
        return batchMetricService.retrieveBatchMetric(metricQueryParamVO);
    }

    @GetMapping("/retrieveDimensionsOfMetric")
    @ApiOperation(value = "get tag metadata", notes = "get tag metadata")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "metricKey", dataType = "String", required = true),
            @ApiImplicitParam(paramType = "query", name = "date", dataType = "Date", example = "2022-08-07", format = "yyyy-MM-dd", required = false)
    })
    public String retrieveDimensionsOfMetric(String metricKey, LocalDate date) throws Exception {
        return batchMetricService.retrieveDimensionsByMetricKey(metricKey, date);
    }

}
