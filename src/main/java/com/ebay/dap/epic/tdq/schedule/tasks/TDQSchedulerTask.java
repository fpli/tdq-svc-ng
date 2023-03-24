package com.ebay.dap.epic.tdq.schedule.tasks;

import com.ebay.dap.epic.tdq.service.AlertManager;
import com.ebay.dap.epic.tdq.service.AnomalyDetector;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

//FIXME: use aspect to log status and track time
@Slf4j
@Component
public class TDQSchedulerTask {

    @Autowired
    private AlertManager alertManager;

    @Autowired
    private AnomalyDetector anomalyDetector;

    @Scheduled(cron = "0 0 11 * * *")
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

    @Scheduled(cron = "0 0 10 * * *")
    @SchedulerLock(name = "TDQSchedulerTask_pageUsageAD", lockAtLeastFor = "PT10M", lockAtMostFor = "PT30M")
    public void pageUsageAD() {
        LocalDate dt = LocalDate.now().minusDays(1);
        anomalyDetector.findAbnormalPages(dt);
    }

    @Scheduled(cron = "0 0 11 * * *")
    @SchedulerLock(name = "TDQSchedulerTask_sendPageProfilingAlerts", lockAtLeastFor = "PT10M", lockAtMostFor = "PT30M")
    public void sendPageProfilingAlerts() throws Exception {
        LocalDate dt = LocalDate.now().minusDays(1);
        //TODO(yxiao6): add job status track in the future

        //FIXME(yxiao6): this should be replaced with profile management
        //only send email in prod
        log.info("Sending Page-Profiling Abnormal Pages Alerts");
        alertManager.sendPageProfilingAlertEmail(dt);
    }

}
