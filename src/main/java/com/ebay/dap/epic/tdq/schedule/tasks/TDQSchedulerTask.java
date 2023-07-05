package com.ebay.dap.epic.tdq.schedule.tasks;

import com.ebay.dap.epic.tdq.service.AlertManager;
import com.ebay.dap.epic.tdq.service.AnomalyDetector;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@Lazy(false)
public class TDQSchedulerTask {

    @Autowired
    private AlertManager alertManager;

    @Autowired
    private AnomalyDetector anomalyDetector;

    @Scheduled(cron = "${tdqsvcngcfg.schedule.cron.multi-uid-alert}", zone = "GMT-7")
    @SchedulerLock(name = "TDQSchedulerTask_multipleUid", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void multipleUid(){
        try {
            LocalDateTime localDateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).minusDays(1);
            alertManager.multipleUidAlert(localDateTime);
        } catch (Exception e) {
            log.info("Failed multipleUid =====> Current DateTime is:" + DateTime.now());
            log.error(e.getMessage());
        }
    }

    @Scheduled(cron = "${tdqsvcngcfg.schedule.cron.page-profiling-anomaly-detect}", zone = "GMT-7")
    @SchedulerLock(name = "TDQSchedulerTask_pageProfilingAD", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void pageProfilingAD() throws Exception {
        LocalDate dt = LocalDate.now(ZoneId.of("GMT-7")).minusDays(1);
        anomalyDetector.findAbnormalPages(dt);
    }

    @Scheduled(cron = "${tdqsvcngcfg.schedule.cron.page-profiling-alert}", zone = "GMT-7")
    @SchedulerLock(name = "TDQSchedulerTask_sendPageProfilingAlerts", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void sendPageProfilingAlerts() throws Exception {
        LocalDate dt = LocalDate.now(ZoneId.of("GMT-7")).minusDays(1);
        alertManager.sendPageProfilingAlertEmail(dt);
    }

}
