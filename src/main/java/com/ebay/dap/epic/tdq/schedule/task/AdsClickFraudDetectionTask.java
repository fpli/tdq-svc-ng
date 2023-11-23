package com.ebay.dap.epic.tdq.schedule.task;

import com.ebay.dap.epic.tdq.service.AlertManager;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("AdsClickFraudDetectionTask")
@Lazy(false)
public class AdsClickFraudDetectionTask {

    @Autowired
    private AlertManager alertManager;

    /**
     * Run at 5:00 PM MST everyday
     */
    @Scheduled(cron = "0 0 17 * * *", zone = "GMT-7")
    @SchedulerLock(name = "AdsClickFraudDetectionTask", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void run() throws Exception {
        alertManager.adsClickFraud(LocalDate.now().minusDays(2));
    }
}
