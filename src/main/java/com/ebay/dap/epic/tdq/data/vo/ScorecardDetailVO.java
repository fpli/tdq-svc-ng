package com.ebay.dap.epic.tdq.data.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScorecardDetailVO {
    Map<String, String> basicInfo = new HashMap<>();
    List<DateRangeItem> dateRangeItemList = new ArrayList<>();
    List<ScorecardDetailItemVO> list = new ArrayList<>();
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DateRangeItem {
        String label;
        String value;
        boolean disabled;
    }

}
