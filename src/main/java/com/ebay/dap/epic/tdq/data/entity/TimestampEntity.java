package com.ebay.dap.epic.tdq.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TimestampEntity extends BaseEntity {
    protected LocalDateTime createTime;
    protected LocalDateTime updateTime;
}
