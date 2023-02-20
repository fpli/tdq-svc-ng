package com.ebay.dap.epic.tdq.service.tasks;

import com.ebay.dap.epic.tdq.service.AlertManager;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//FIXME: 1. put package to root level with name `scheduling`
// 2. use aspect to log status and track time
@Slf4j
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
