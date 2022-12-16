package com.ebay.dap.epic.tdq.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChartConfig {
    String name;
    List<DatasetConfig> datasetConfigurations;
}
