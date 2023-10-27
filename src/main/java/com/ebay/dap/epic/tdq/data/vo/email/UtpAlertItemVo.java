package com.ebay.dap.epic.tdq.data.vo.email;

import lombok.Data;

@Data
public class UtpAlertItemVo {

    private String channel;

    private String threshold;

    private String currentVal;

    private String changePct;

}
