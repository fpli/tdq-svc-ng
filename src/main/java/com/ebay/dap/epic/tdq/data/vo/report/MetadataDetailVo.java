package com.ebay.dap.epic.tdq.data.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MetadataDetailVo {

    private Long id;

    private Integer metricId;

    private String domain;

    private String eventType;

    private String pageIds;

    private String metadataId;

    private String metadataName;

    private String metadataDesc;

    private String elementId;

    private String samplePath;

    private String sampleElement;

    private Long traffic;

    private Long trafficP; // traffic cnt of Problem

    private String sampleUrl;

    private String exp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dt;

}
