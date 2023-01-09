package com.ebay.dap.epic.tdq.data.vo.pageLevel;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrafficOfPageDetailVO {
    Integer pageId;
    List<TrafficOfDayVO> traffic = new ArrayList<>();
    List<PageAbnormalItemVO> abnormal = new ArrayList<>();
}
