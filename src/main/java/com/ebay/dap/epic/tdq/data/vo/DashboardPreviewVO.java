package com.ebay.dap.epic.tdq.data.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardPreviewVO {

    private List<ChartPreviewVO> chartList = new ArrayList<>();

    private Long refreshTime;

}
