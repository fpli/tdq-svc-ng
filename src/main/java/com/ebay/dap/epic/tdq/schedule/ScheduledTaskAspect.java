package com.ebay.dap.epic.tdq.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.common.util.DateTimeUtils;
import com.ebay.dap.epic.tdq.data.entity.ScheduledTaskCfg;
import com.ebay.dap.epic.tdq.data.entity.ScheduledTaskHistory;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.ScheduledTaskCfgMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.ScheduledTaskHistoryMapper;
import com.ebay.dap.epic.tdq.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@Aspect
public class ScheduledTaskAspect {

    @Autowired
    private ScheduledTaskHistoryMapper scheduledTaskHistoryMapper;

    @Autowired
    private ScheduledTaskCfgMapper scheduledTaskCfgMapper;

    @Autowired
    private EmailService emailService;

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void doIt(ProceedingJoinPoint pjp) {
        // use GMT-7 timezone to track job time
        LocalDateTime startTime = DateTimeUtils.currentTime();
        ScheduledTaskHistory taskHistory = new ScheduledTaskHistory();

        String taskName = getTaskName(pjp);

        log.info("Start to execute scheduled task: {}", taskName);

        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "Unknown Host";
        }

        taskHistory.setTask(taskName);
        taskHistory.setHost(host);
        taskHistory.setStartTime(startTime);
        taskHistory.setStatus(JobStatus.STARTED);

        scheduledTaskHistoryMapper.insert(taskHistory);

        try {
            // skip if the task is set to inactive
            LambdaQueryWrapper<ScheduledTaskCfg> scheduledTaskCfgQuery = Wrappers.lambdaQuery();
            scheduledTaskCfgQuery.eq(ScheduledTaskCfg::getTask, taskName);
            ScheduledTaskCfg scheduledTaskCfg = scheduledTaskCfgMapper.selectOne(scheduledTaskCfgQuery);
            if (scheduledTaskCfg != null && scheduledTaskCfg.getInactive()) {
                log.info("Skipped {}", taskName);
                // use GMT-7 timezone to track job time
                LocalDateTime endTime = DateTimeUtils.currentTime();
                long runningSec = ChronoUnit.SECONDS.between(startTime, endTime);

                taskHistory.setEndTime(endTime);
                taskHistory.setRunningSec(runningSec);
                taskHistory.setStatus(JobStatus.SKIPPED);

                scheduledTaskHistoryMapper.updateById(taskHistory);
                return;
            }

            pjp.proceed();

            // use GMT-7 timezone to track job time
            LocalDateTime endTime = DateTimeUtils.currentTime();
            long runningSec = ChronoUnit.SECONDS.between(startTime, endTime);

            taskHistory.setEndTime(endTime);
            taskHistory.setRunningSec(runningSec);
            taskHistory.setStatus(JobStatus.SUCCESS);

            scheduledTaskHistoryMapper.updateById(taskHistory);

            log.info("Succeed to execute scheduled task: {}", taskName);

        } catch (Throwable e) {
            log.error("Failed to execute scheduled task: {}", taskName, e);
            LocalDateTime endTime = DateTimeUtils.currentTime();
            long runningSec = ChronoUnit.SECONDS.between(startTime, endTime);

            taskHistory.setEndTime(endTime);
            taskHistory.setRunningSec(runningSec);
            taskHistory.setStatus(JobStatus.ERROR);
            taskHistory.setErrorMsg(e.getMessage());
            taskHistory.setErrorDetails(ExceptionUtils.getStackTrace(e));

            // save task status into database
            scheduledTaskHistoryMapper.updateById(taskHistory);

            // send failure tasks to dev
            try {
                Context context = new Context();
                context.setVariable("taskVo", taskHistory);
                emailService.sendHtmlEmail(
                        "devops-scheduled-task-failure",
                        context,
                        "Scheduled Task Execution Failure",
                        List.of("yxiao6@ebay.com", "fangpli@ebay.com")
                );
            } catch (Exception ex) {
                // ignore email exception
            }
        }

    }

    private String getTaskName(ProceedingJoinPoint pjp) {
        String clazzName = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        return clazzName + "." + methodName;
    }

}
