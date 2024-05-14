package com.ebay.dap.epic.tdq.schedule.task;

import com.ebay.dap.epic.tdq.service.AlertManager;
import com.ebay.dap.epic.tdq.service.TagProfilingService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component("CJSSearchAlertTask")
@Lazy(false)
public class CJSSearchAlertTask {

    @Autowired
    private AlertManager alertManager;

    /**
     * Run at 17:00 PM MST everyday
     */
    @Scheduled(cron = "0 0 17 * * *", zone = "GMT-7")
    @SchedulerLock(name = "searchMetricAlert", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void run() throws Exception {
        LocalDate dt = LocalDate.now(ZoneId.of("GMT-7")).minusDays(1);
        alertManager.cjsSearchMetricAbnormalDetection(dt);
    }

}
