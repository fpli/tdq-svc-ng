package com.ebay.dap.epic.tdq.data.vo.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleUidDTO {
    String metricName;
    Double valueOfYesterday;
    Double valueOfToday;
    String increaseType;
    Double rate;
    String unit = "-";
    Double threshold;
    String description;
}