package com.ebay.dap.epic.tdq.data.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagDetailDTO {
    String dt;
    LocalDate localDate;
    double tagCount;
    double eventCount;
    double tagFormatInconsistentCount;
    double tagInaccurateCount;
}