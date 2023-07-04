package com.ebay.dap.epic.tdq.data.vo.alert;

import lombok.Data;

@Data
public class AlertItem {

    private String title;

    private String threshold;

    private String currentValue;

    private String diffPct;

}
