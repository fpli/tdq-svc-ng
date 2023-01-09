package com.ebay.dap.epic.tdq.data.vo.pageLevel;


import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class PageFamilyCardQueryVo {
    String pageFamilyName;
    List<String> pageFamilyNameList = new ArrayList<>();
    List<Integer> pageIds = new ArrayList<>();
    String date;
}
