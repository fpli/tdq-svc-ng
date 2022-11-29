package com.ebay.dap.epic.tdq.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Data
public class MetricQueryParamVO {
    String metricKey;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;
    private final Map<String, Set<String>> dimensions;
}
