package com.ebay.dap.epic.tdq.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AuditableEntity extends TimestampEntity {
    protected String createdBy;
    protected String updatedBy;
}
