package com.ebay.dap.epic.tdq.data.vo.email;

import lombok.Data;

//TODO: use a unified structure for alert emails
@Deprecated
@Data
public class AlertItem {

    private String title;

    private String threshold;

    private String currentValue;

    private String diffPct;

}
