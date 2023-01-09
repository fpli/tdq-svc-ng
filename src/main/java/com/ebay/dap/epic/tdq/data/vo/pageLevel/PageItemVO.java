package com.ebay.dap.epic.tdq.data.vo.pageLevel;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageItemVO {
    Integer pageId;
    String pageName;
    String owner;
    int iframe;
    String created;
    long dailyVolume;
    long accessTotal;
    int popularity;
}
