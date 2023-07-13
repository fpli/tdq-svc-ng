package com.ebay.dap.epic.tdq.schedule.task;

import com.ebay.dap.epic.tdq.service.AlertManager;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Lazy(false)
public class MultipleUidAlertTask {

    @Autowired
    private AlertManager alertManager;

    @Scheduled(cron = "${tdqsvcngcfg.schedule.cron.multi-uid-alert}", zone = "GMT-7")
    @SchedulerLock(name = "MultipleUidAlertTask", lockAtLeastFor = "PT10M", lockAtMostFor = "PT30M")
    public void run() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).minusDays(1);
        alertManager.multipleUidAlert(localDateTime);
    }

}
