package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.vo.DateVo;
import com.ebay.dap.epic.tdq.data.vo.report.MetadataDetailVo;
import com.ebay.dap.epic.tdq.data.vo.report.MetadataSummaryVo;
import com.ebay.dap.epic.tdq.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/report")
@Slf4j
@Tag(name = "Report API", description = "Report related API")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Operation(summary = "Get Click metadata coverage summary report")
    @GetMapping(path = "/metadata/click_summary")
    public ResponseEntity<List<MetadataSummaryVo>> getClickSummary(@RequestParam(value = "dt", required = false)
                                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dt) {
        List<MetadataSummaryVo> results = reportService.getClickSummary(dt);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get Module metadata coverage summary report")
    @GetMapping(path = "/metadata/module_summary")
    public List<MetadataSummaryVo> getModuleSummary(@RequestParam(value = "dt", required = false)
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dt) {
        return reportService.getModuleSummary(dt);
    }

    @Operation(summary = "Get Click metadata coverage detail report")
    @GetMapping(path = "/metadata/click_detail")
    public List<MetadataDetailVo> getClickDetail(@RequestParam("domain") String domain,
                                                 @RequestParam("metric_id") Integer metricId,
                                                 @RequestParam("dt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dt) {
        return reportService.getClickDetail(domain, metricId, dt);
    }

    @Operation(summary = "Get Module metadata coverage detail report")
    @GetMapping(path = "/metadata/module_detail")
    public List<MetadataDetailVo> getModuleDetail(@RequestParam("domain") String domain,
                                                  @RequestParam("metric_id") Integer metricId,
                                                  @RequestParam("dt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dt) {
        return reportService.getModuleDetail(domain, metricId, dt);
    }

    @Operation(summary = "Get latest report date")
    @GetMapping(path = "/metadata/latest_dt")
    public ResponseEntity<DateVo> getLatestDt() {
        return ResponseEntity.ok(new DateVo(reportService.getLatestDt()));
    }
}
