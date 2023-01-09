package com.ebay.dap.epic.tdq.data.vo.pageLevel;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageCardItemVO {
    String title;
    long amount;
    int incrementType;
    double increment;
    String dateType = "week";
    int order;
    List<Integer> pageIds = new ArrayList<>();
}
