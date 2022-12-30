package com.ebay.dap.epic.tdq.data.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagCardItemVo {
    String title;
    long amount;
    double rate;
    int incrementType;
    double increment;
    String dateType = "week";
    int order;
    Set<String> tagNames = new HashSet<>();
}
