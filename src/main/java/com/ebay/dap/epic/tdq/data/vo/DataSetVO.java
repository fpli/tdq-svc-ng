package com.ebay.dap.epic.tdq.data.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataSetVO {
    String type;
    String label;

    String backgroundColor;
    List<Double> data = new ArrayList<>();

    @Override
    public String toString() {
        return "DataSetVO{" +
                "type='" + type + '\'' +
                ", label='" + label + '\'' +
                ", data=" + data.stream().map(Double::longValue).toList() +
                '}';
    }
}
