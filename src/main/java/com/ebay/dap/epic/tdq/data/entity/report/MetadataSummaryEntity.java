package com.ebay.dap.epic.tdq.data.entity.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.entity.AuditableEntity;
import lombok.Data;

import java.time.LocalDate;


@Data
@TableName("t_report_metadata_summary")
public class MetadataSummaryEntity extends AuditableEntity {

    private Integer metricId;

    private String domain;

    private String metadataType;

    private String eventType;

    @TableField("`all_experience`")
    private Long allExp;

    private Long dweb;

    private Long mweb;

    private Long webview;

    private Long android;

    private Long ios;

    private LocalDate dt;
}
