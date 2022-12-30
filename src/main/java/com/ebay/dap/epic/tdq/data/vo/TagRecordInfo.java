package com.ebay.dap.epic.tdq.data.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagRecordInfo {
    String tagName;
    String dataType;
    String description;
    double tagVolume;
    double eventVolume;
    double coverage;
    double tagSize;
    long accessTotal;
    int popularity;
}
