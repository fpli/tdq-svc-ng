package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.vo.report.MetadataDetailVo;
import com.ebay.dap.epic.tdq.data.vo.report.MetadataSummaryVo;
import com.ebay.dap.epic.tdq.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/report")
@Api(value = "ReportController", tags = {"Report APIs"})
public class ReportController {

    @Autowired
    private ReportService reportService;

    @ApiOperation("Get Click metadata coverage summary report")
    @GetMapping(path = "/metadata/click_summary")
    public List<MetadataSummaryVo> getClickSummary(@RequestParam(value = "dt", required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dt) {
        return reportService.getClickSummary(dt);
    }

    @ApiOperation("Get Module metadata coverage summary report")
    @GetMapping(path = "/metadata/module_summary")
    public List<MetadataSummaryVo> getModuleSummary(@RequestParam(value = "dt", required = false)
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dt) {
        return reportService.getModuleSummary(dt);
    }

    @ApiOperation("Get Click metadata coverage detail report")
    @GetMapping(path = "/metadata/click_detail")
    public List<MetadataDetailVo> getClickDetail(@RequestParam("domain") String domain,
                                                 @RequestParam("metric_id") Integer metricId,
                                                 @RequestParam("dt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dt) {
        return reportService.getClickDetail(domain, metricId, dt);
    }

    @ApiOperation("Get Module metadata coverage detail report")
    @GetMapping(path = "/metadata/module_detail")
    public List<MetadataDetailVo> getModuleDetail(@RequestParam("domain") String domain,
                                                  @RequestParam("metric_id") Integer metricId,
                                                  @RequestParam("dt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dt) {
        return reportService.getModuleDetail(domain, metricId, dt);
    }
}
