package com.ebay.dap.epic.tdq.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@TableName("mmd_record")
public class MMDRecordInfo extends AuditableEntity {
    String payload;
    String response;

    Integer timeInterval;

    Integer anomalyType;
    String uid;
}