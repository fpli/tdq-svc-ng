package com.ebay.dap.epic.tdq.data.vo;

import lombok.Data;

@Data
public class ChartVO {

    Long id;
    String title;
    String description;
    String categoryId;
    int level;
}
