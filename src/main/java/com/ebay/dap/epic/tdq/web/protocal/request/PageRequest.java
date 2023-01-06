package com.ebay.dap.epic.tdq.web.protocal.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class PageRequest {
    @Min(1)
    private int current = 1;

    @Min(1)
    @Max(500)
    private int size = 10;
}
