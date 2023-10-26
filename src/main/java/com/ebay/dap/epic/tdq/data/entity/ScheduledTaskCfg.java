package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("scheduled_task_cfg")
public class ScheduledTaskCfg extends AuditableEntity {

    private String task;

    private Boolean inactive;

}
