package com.ebay.dap.epic.tdq.schedule.task;

import com.ebay.dap.epic.tdq.service.AnomalyDetector;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@Lazy(false)
public class PageProfilingAnomalyDetectionTask {

    @Autowired
    private AnomalyDetector anomalyDetector;

    @Scheduled(cron = "${tdqsvcngcfg.schedule.cron.page-profiling-anomaly-detection}", zone = "GMT-7")
    @SchedulerLock(name = "PageProfilingAnomalyDetectionTask", lockAtLeastFor = "PT10M", lockAtMostFor = "PT30M")
    public void run() throws Exception {
        // T-1 as dt
        LocalDate dt = LocalDate.now(ZoneId.of("GMT-7")).minusDays(1);
        anomalyDetector.findAbnormalPages(dt);
    }


}
