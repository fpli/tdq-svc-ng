package com.ebay.dap.epic.tdq.data.vo.pageLevel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageBasicInfoVO {
    Integer pageId;
    String pageName;
    String owner;
    @JsonIgnore
    String ownerEmail;
    int iframe;
    String createDt;
    String firstSeenDt;
    long dailyTraffic;
    long usageIn30Days;
}
