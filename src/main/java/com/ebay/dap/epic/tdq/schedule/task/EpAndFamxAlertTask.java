package com.ebay.dap.epic.tdq.schedule.task;

import com.ebay.dap.epic.tdq.service.AlertManager;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("EpAndFamxAlertTask")
@Lazy(false)
public class EpAndFamxAlertTask {

    @Autowired
    private AlertManager alertManager;

    @Scheduled(cron = "0 0 17 * * *", zone = "GMT-7")
    @SchedulerLock(name = "EpAndFamxAlertTask", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void run() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(2);
        alertManager.alertForEPTeamAndFamx(localDateTime);
    }
}
