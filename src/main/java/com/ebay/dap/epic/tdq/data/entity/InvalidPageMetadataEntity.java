package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("t_invalid_page_metadata")
public class InvalidPageMetadataEntity extends AuditableEntity {

    @TableField("page_id")
    private Integer pageId;

    private String environment;

    @TableField("life_cycle_state")
    private String lifeCycleState;

    @TableField("pool_name")
    private String poolName;

    @TableField("pool_notification")
    private String poolNotification;

    @TableField("resource_id")
    private String resourceId;

    @TableField("dt")
    private LocalDate dt;

    @TableField("event_cnt")
    private long eventCnt;

    @TableField("event_pct")
    private double eventPct;

    @TableField("app_owner")
    private String appOwner;

    @TableField("app_notification")
    private String appNotification;

    @TableField("app_jira")
    private String jiraLink;

}
