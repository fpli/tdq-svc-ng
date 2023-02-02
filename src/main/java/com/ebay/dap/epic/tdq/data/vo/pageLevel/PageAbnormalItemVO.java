package com.ebay.dap.epic.tdq.data.vo.pageLevel;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PageAbnormalItemVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate dt;

    Long uBound;

    Long lBound;
}
