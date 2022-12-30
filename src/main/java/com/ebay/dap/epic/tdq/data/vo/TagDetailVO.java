package com.ebay.dap.epic.tdq.data.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagDetailVO {
    String dt;
    double tagCount;
    double eventCount;
    double completenessPercent;
    double tagFormatConsistentPercent;
    double tagAccuratePercent;
    boolean isAnomaly;
    BigDecimal uBound;
    BigDecimal lBound;
}