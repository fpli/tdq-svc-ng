package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("anomaly_item")
public class AnomalyItemEntity extends BaseEntity {

    private String type;


    private String refId;

    private BigDecimal value;

    //TODO(yxiao6): consider hourly anomaly detection case
    private LocalDate dt;


    private BigDecimal uBound;


    private BigDecimal lBound;


    Double rateValue;


    Double lowerBound;

    Double upperBound;
}