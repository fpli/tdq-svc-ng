package com.ebay.dap.epic.tdq.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagMetaDataVO {
    String tagName;
    String dataType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate creDate;
    String creUser;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate firstSeenDt;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate lastSeenDt;
    String description;
    double dailyTagSize;
    String dailyTagSizePercent;
    List<UsageOfDayVO> usageOfDayVOList;
    List<DailyTagSizeWithPercentVO> dailyTagSizeWithPercentVOList;
}
