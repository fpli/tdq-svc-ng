package com.ebay.dap.epic.tdq.data.vo.email;

import lombok.Data;

import java.util.List;

@Data
public class UtpAlertVo {

    private String metricTime;

    private List<UtpAlertItemVo> items;

}
