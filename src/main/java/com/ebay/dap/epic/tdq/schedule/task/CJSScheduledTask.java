package com.ebay.dap.epic.tdq.schedule.task;

import com.ebay.dap.epic.tdq.service.TagProfilingService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component("CJSScheduledTask")
@Lazy(false)
public class CJSScheduledTask {

    @Autowired
    private TagProfilingService tagProfilingService;

    /**
     * Run at 12:00 PM MST everyday
     */
    @Scheduled(cron = "0 0 12 * * *", zone = "GMT-7")
    @SchedulerLock(name = "CJSScheduledTask", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void run() throws Exception {
        List<String> list = List.of("Itmattr", "plmt", "po", "srpGist", "itmmeta", "cjsBeta", "cjs");
        tagProfilingService.cjsTagAlerting(LocalDate.now().minusDays(2), list);
    }

}
