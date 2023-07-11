package com.ebay.dap.epic.tdq.schedule;

import com.ebay.dap.epic.tdq.data.entity.ScheduledTaskHistory;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.ScheduledTaskHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@Aspect
public class ScheduledTaskAspect {

    @Autowired
    private ScheduledTaskHistoryMapper mapper;

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void doIt(ProceedingJoinPoint pjp) {
        // use GMT-7 timezone to track job time
        LocalDateTime startTime = LocalDateTime.now(ZoneId.of("GMT-7"));
        ScheduledTaskHistory taskHistory = new ScheduledTaskHistory();

        String taskName = getTaskName(pjp);

        log.info("Start to execute scheduled task: {}", taskName);

        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            host = "Unknown Host";
        }

        taskHistory.setTask(taskName);
        taskHistory.setHost(host);
        taskHistory.setStartTime(startTime);
        taskHistory.setStatus(JobStatus.STARTED);

        mapper.insert(taskHistory);

        try {
            pjp.proceed();

            // use GMT-7 timezone to track job time
            LocalDateTime endTime = LocalDateTime.now(ZoneId.of("GMT-7"));
            long runningSec = ChronoUnit.SECONDS.between(startTime, endTime);

            taskHistory.setEndTime(endTime);
            taskHistory.setRunningSec(runningSec);
            taskHistory.setStatus(JobStatus.SUCCESS);

            mapper.updateById(taskHistory);

            log.info("Succeed to execute scheduled task: {}", taskName);

        } catch (Throwable e) {
            log.error("Failed to execute scheduled task: {}", taskName, e);
            LocalDateTime endTime = LocalDateTime.now(ZoneId.of("UTC"));
            long runningSec = ChronoUnit.SECONDS.between(startTime, endTime);

            taskHistory.setEndTime(endTime);
            taskHistory.setRunningSec(runningSec);
            taskHistory.setStatus(JobStatus.ERROR);
            taskHistory.setErrorMsg(e.getMessage());
            taskHistory.setErrorDetails(ExceptionUtils.getStackTrace(e));

            mapper.updateById(taskHistory);
        }

    }

    private String getTaskName(ProceedingJoinPoint pjp) {
        String clazzName = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        return clazzName + "." + methodName;
    }

}
