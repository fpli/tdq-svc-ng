package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.schedule.JobStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scheduled_task_history")
public class ScheduledTaskHistory extends TimestampEntity {

    private String task;

    private String host;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long runningSec;

    private JobStatus status;

    private String errorMsg;

    private String errorDetails;

}
