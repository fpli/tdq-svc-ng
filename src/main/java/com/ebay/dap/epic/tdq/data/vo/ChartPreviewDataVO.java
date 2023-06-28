package com.ebay.dap.epic.tdq.data.vo;

import com.ebay.dap.epic.tdq.data.enums.ChartMode;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChartPreviewDataVO {

    private Long chartId;

    private String title;

    private String description;

    private ChartMode mode;

    private String step;

    private String viewCfg;

    private List<String> labels;

    private Map<String, List<ChartValueVO>> datasets;

    private Long timestamp;

}
