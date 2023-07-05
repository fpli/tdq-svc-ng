package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.schedule.JobStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("scheduled_task_history")
public class ScheduledTaskHistory extends BaseEntity {

    private String task;

    private String host;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long runningSec;

    private JobStatus status;

    private String errorMsg;

    private String errorDetails;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
