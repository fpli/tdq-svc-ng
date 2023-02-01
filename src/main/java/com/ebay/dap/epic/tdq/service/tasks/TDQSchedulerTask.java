package com.ebay.dap.epic.tdq.service.tasks;

import com.ebay.dap.epic.tdq.service.AlertManager;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Log4j2
@Component
public class TDQSchedulerTask {

    @Autowired
    private AlertManager alertManager;

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

    @Scheduled(cron = "0 0/5 * * * ?")
    @SchedulerLock(name = "TDQSchedulerTask_test", lockAtLeastFor = "PT1M", lockAtMostFor = "PT4M")
    public void test(){
        try {
            System.out.println(Thread.currentThread().getName() + " - TDQSchedulerTask_test");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
