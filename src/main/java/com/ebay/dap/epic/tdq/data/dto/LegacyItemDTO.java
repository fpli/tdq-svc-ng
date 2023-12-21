package com.ebay.dap.epic.tdq.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LegacyItemDTO {
    Integer id;
    String name;
    String description;
    Double threshold;
    String thresholdType;
    String metricKey;
    String unit;
}
