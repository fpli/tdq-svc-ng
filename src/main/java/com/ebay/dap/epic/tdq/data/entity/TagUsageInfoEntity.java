package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("profiling_tag_usage")
public class TagUsageInfoEntity extends AuditableEntity {

    String tagName;

    private String platform;

    private String accountType;

    private String username;

    private Long accessCount;

    private LocalDate dt;
}
