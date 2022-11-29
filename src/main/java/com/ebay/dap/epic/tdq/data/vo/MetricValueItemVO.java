package com.ebay.dap.epic.tdq.data.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MetricValueItemVO {
    LocalDate date;
    Double    value;
}
