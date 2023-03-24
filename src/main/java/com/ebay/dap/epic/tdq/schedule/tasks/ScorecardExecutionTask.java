package com.ebay.dap.epic.tdq.schedule.tasks;

import com.ebay.dap.epic.tdq.service.scorecard.ExecutionEngine;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Component
public class ScorecardExecutionTask {

    @Autowired
    private ExecutionEngine executionEngine;

    // use MST to schedule as all UC4 job are based on MST
    @Scheduled(cron = "0 0 15 * * *", zone = "GMT-7")
    @SchedulerLock(name = "ScorecardExecutionTask", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void run() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT-7"));
        LocalDate dt = now.toLocalDate().minusDays(1);
        log.info("Start to run Scorecard execution task for date: {}", dt);
        executionEngine.process(dt);
        log.info("Finished Scorecard execution task for date: {}", dt);
    }

}
