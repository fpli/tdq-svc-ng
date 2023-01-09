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
@TableName("profiling_page_usage")
public class PageUsageEntity extends BaseEntity {

    @TableField("page_id")
    private Integer pageId;

    @TableField("platform")
    private String platform;

    @TableField("account_type")
    private String accountType;

    @TableField("username")
    private String username;

    @TableField("access_count")
    private Long accessCount;

    @TableField("dt")
    private LocalDate dt;
}