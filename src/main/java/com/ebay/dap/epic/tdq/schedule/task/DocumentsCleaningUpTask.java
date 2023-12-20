package com.ebay.dap.epic.tdq.schedule.task;

import com.ebay.dap.epic.tdq.service.MetricService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("documentsCleaningUpTask")
@Lazy(false)
public class DocumentsCleaningUpTask {

    @Autowired
    private MetricService metricService;

    /**
     * Run at 00:00 AM MST every sunday
     */
    @Scheduled(cron = "0 0 0 ? * 0", zone = "GMT-7")
    @SchedulerLock(name = "DocumentsCleaningUpTask", lockAtLeastFor = "PT5M", lockAtMostFor = "PT20M")
    public void run(){
        metricService.cleanUpTop50PageMetricDoc();
    }

}
