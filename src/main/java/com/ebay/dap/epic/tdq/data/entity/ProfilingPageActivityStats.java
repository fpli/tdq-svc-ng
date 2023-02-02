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
@TableName("profiling_page_activity_stats")
public class ProfilingPageActivityStats extends BaseEntity {
    @TableField("deployed_cnt")
    long deployedCnt;
    @TableField("active_in_90_days_cnt")
    long activeIn_90_days_cnt;
    @TableField("unused_cnt")
    long unused_cnt;
    LocalDate dt;
}