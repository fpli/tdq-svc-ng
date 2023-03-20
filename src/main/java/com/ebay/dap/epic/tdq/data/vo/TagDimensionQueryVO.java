package com.ebay.dap.epic.tdq.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagDimensionQueryVO {

    String tagName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDt;
}