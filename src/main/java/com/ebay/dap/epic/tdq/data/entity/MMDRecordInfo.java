package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("mmd_record")
public class MMDRecordInfo extends TimestampEntity {

    String payload;

    String response;

    Integer timeInterval;

    Integer anomalyType;

    String uid;

}