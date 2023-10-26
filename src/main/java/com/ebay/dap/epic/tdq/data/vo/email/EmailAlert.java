package com.ebay.dap.epic.tdq.data.vo.email;

import lombok.Data;

import java.util.List;

@Deprecated
@Data
public class EmailAlert {

    private String dt;

    private List<AlertItem> items;

}
