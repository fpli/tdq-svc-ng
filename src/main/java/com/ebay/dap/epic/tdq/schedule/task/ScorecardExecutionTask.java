package com.ebay.dap.epic.tdq.schedule.task;

import com.ebay.dap.epic.tdq.service.scorecard.ExecutionEngine;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@Lazy(false)
public class ScorecardExecutionTask {

    @Autowired
    private ExecutionEngine executionEngine;

    // use MST(UTC-7) to schedule as all UC4 job are based on MST(UTC-7)
    @Scheduled(cron = "${tdqsvcngcfg.schedule.cron.scorecard-execution}", zone = "GMT-7")
    @SchedulerLock(name = "ScorecardExecutionTask", lockAtLeastFor = "PT10M", lockAtMostFor = "PT30M")
    public void run() throws Exception {
        // T-1 as dt
        LocalDate dt = LocalDate.now(ZoneId.of("GMT-7")).minusDays(1);
        executionEngine.process(dt);
    }

}