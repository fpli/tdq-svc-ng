package com.ebay.dap.epic.tdq.schedule.task;


import com.ebay.dap.epic.tdq.service.PageMetadataQualityService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("unregisteredPageMetadataTask")
@Lazy(false)
@Slf4j
public class UnregisteredPageMetadataTask {

    @Autowired
    private PageMetadataQualityService pageMetadataQualityService;

    @Scheduled(cron = "0 0 8 * * *", zone = "GMT-7")
    @SchedulerLock(name = "UnregisteredPageMetadataTask", lockAtLeastFor = "PT5M", lockAtMostFor = "PT20M")
    public void run(){
        pageMetadataQualityService.dailyTask(LocalDate.now().minusDays(2));
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "GMT-7")
    @SchedulerLock(name = "notifyApplicationOwner", lockAtLeastFor = "PT5M", lockAtMostFor = "PT20M")
    public void notifyApplicationOwner(){
        pageMetadataQualityService.dailyNotifyApplicationOwner(LocalDate.now().minusDays(1));
    }
}
