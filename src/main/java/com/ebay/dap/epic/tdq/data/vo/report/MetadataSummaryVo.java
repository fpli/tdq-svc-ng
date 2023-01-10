package com.ebay.dap.epic.tdq.data.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MetadataSummaryVo {

    private Long id;

    private String metadataType;

    private String eventType;

    private Integer metricId;

    private String metric;

    private Long all;

    private Long dweb;

    private Long mweb;

    private Long webview;

    private Long android;

    private Long ios;

    private String domain;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dt;

}
