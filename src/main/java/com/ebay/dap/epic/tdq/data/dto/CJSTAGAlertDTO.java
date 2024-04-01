package com.ebay.dap.epic.tdq.data.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CJSTAGAlertDTO {
    String tagName;
    long   tagVolume;
    double upperBound;
    double lowerBound;
}
