package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.service.AlertManager;
import com.ebay.dap.epic.tdq.service.AnomalyDetector;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping(value = "/api/jobs")
@Tag(name = "Trigger Jobs API", description = "trigger jobs")
public class ScheduledJobTriggerController {

    @Autowired
    private AnomalyDetector anomalyDetector;

    @Autowired
    private AlertManager alertManager;

    @Operation(summary = "trigger abnormal page detecting")
    @GetMapping("triggerPageDetecting")
    public String triggerPageDetecting(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) throws Exception {
        anomalyDetector.findAbnormalPages(date);
        return "done";
    }

    @Operation(summary = "send abnormal page email")
    @GetMapping("sendAbnormalPageEmail")
    public String triggerPageDetectingAlert(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) throws Exception {
        alertManager.sendPageProfilingAlertEmail(date);
        return "done";
    }

    @Operation(summary = "trigger ads click fraud alert")
    @GetMapping("triggerAdsClickFraudAlert")
    public String triggerAdsClickFraudAlert(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) throws Exception {
        alertManager.adsClickFraud(date);
        return "done";
    }

    @Operation(summary = "trigger EPTeam And Famx alert")
    @GetMapping("triggerEPTeamAndFamxAlert")
    public String triggerEPTeamAndFamxAlert(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime date) throws Exception {
        alertManager.alertForEPTeamAndFamx(date);
        return "done";
    }

    @Operation(summary = "trigger CJS Search Metric Abnormal Detection alert")
    @GetMapping("triggerCjsSearchMetricAbnormalDetection")
    public String triggerCjsSearchMetricAbnormalDetection(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) throws Exception {
        alertManager.cjsSearchMetricAbnormalDetection(date);
        return "done";
    }

    @Operation(summary = "trigger CJS Ads Metric Abnormal Detection alert")
    @GetMapping("triggerCjsAdsMetricAbnormalDetection")
    public String triggerCjsAdsMetricAbnormalDetection(@RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) throws Exception {
        alertManager.cjsAdsMetricAbnormalDetection(date);
        return "done";
    }
}
