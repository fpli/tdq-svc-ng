package com.ebay.dap.epic.tdq.data.entity.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.entity.AuditableEntity;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("t_report_metadata_detail")
public class MetadataDetailEntity extends AuditableEntity {

    private Integer metricId;

    private String domain;

    private String eventType;

    private String pageIds;

    private String metadataType;

    private String metadataId;

    private String metadataName;

    private String metadataDesc;

    private String elementInstanceId;

    private String samplePath;

    private String sampleElement;

    private String exp;

    @TableField("traffic_cnt")
    private Long traffic;

    @TableField("traffic_p_cnt")
    private Long trafficP;

    private String sampleUrl;

    private LocalDate dt;
}
