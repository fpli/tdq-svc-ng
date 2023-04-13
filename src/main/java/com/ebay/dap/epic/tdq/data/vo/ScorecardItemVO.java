package com.ebay.dap.epic.tdq.data.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScorecardItemVO {

    Integer id;
    Integer pid;
    String key;
    String category;
    String checkedItem;
    String date;

    Map<String, Double> extMap = new HashMap<>();
}
