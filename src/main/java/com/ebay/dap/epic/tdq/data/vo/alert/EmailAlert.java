package com.ebay.dap.epic.tdq.data.vo.alert;

import lombok.Data;

import java.util.List;

@Data
public class EmailAlert {

    private String dt;

    private List<AlertItem> items;

}
