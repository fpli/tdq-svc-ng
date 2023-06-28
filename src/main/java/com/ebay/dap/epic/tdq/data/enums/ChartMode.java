package com.ebay.dap.epic.tdq.data.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChartMode {

    SINGLE(1, "single"),

    MULTIPLE(2, "multiple"),

    COMPOSE(3, "compose"),

    DIFF(4, "diff"),

    BY_DIMENSION(5, "by_dimension");


    @EnumValue
    private final int code;
    private final String name;
}
