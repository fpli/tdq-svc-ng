package com.ebay.dap.epic.tdq.web.protocal.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorItem extends AbstractErrorItem {
    private int errCode;
    private String message;
}
