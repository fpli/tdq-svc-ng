package com.ebay.dap.epic.tdq.data.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChartDataVO {

    List<String> labels;
    List<DataSetVO> datasets = new ArrayList<>();
}


