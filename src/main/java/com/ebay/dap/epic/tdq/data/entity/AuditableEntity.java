package com.ebay.dap.epic.tdq.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AuditableEntity extends BaseEntity {
    protected String createdBy;
    protected String updatedBy;
    protected LocalDateTime createTime;
    protected LocalDateTime updateTime;
}
