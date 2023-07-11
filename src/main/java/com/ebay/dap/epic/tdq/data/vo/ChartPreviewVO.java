package com.ebay.dap.epic.tdq.data.vo;

import lombok.Data;

@Data
public class ChartPreviewVO {

    private Long chartId;
    private String title;
    private String description;
    private Integer displayOrder;
    private Long updateTime;

}
