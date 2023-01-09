package com.ebay.dap.epic.tdq.data.vo.pageLevel;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsageOfPageDetailVO {
    Integer pageId;
    List<UsageOfDayVO> usage;
}
