package com.ebay.dap.epic.tdq.data.vo.alert;

import lombok.Data;

import java.util.List;

@Data
public class PageAlertVo {

    private String metricTime;

    private List<PageAlertItemVo> items;

}
