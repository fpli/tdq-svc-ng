package com.ebay.dap.epic.tdq.data.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetricValueType {

    GAUGE(1, "Gauge"),

    PERCENT(2, "Percent");

    @EnumValue
    private final int code;
    private final String name;
}
