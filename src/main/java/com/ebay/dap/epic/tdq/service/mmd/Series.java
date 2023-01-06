package com.ebay.dap.epic.tdq.service.mmd;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Series {
    String timestamp;
    double value;
}