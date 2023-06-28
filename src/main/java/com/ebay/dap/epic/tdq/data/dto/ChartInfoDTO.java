package com.ebay.dap.epic.tdq.data.dto;

import com.ebay.dap.epic.tdq.data.enums.ChartMode;
import lombok.Data;

import java.util.List;

@Data
public class ChartInfoDTO {

    private Long chartId;

    private String title;

    private String description;

    private ChartMode mode;

    private List<String> metricKeys;

    private String exp;

    // json string
    private String viewCfg;

}
