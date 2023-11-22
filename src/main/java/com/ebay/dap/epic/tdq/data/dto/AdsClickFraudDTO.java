package com.ebay.dap.epic.tdq.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdsClickFraudDTO {
    private String groupName;
    private double value;
    private double threshold;
    private String dt;
}
