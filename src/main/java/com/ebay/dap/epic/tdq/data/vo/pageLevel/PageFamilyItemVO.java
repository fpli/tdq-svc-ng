package com.ebay.dap.epic.tdq.data.vo.pageLevel;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageFamilyItemVO {
    String name;
    String pageFamilyName;
    List<String> pageFamilyNameList = new ArrayList<>();
    Double rate;
    Long cnt;
    List<Integer> pageIds = new ArrayList<>();
}
