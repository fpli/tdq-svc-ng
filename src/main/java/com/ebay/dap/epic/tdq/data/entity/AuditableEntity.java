package com.ebay.dap.epic.tdq.data.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AuditableEntity extends BaseEntity {
  protected String createdBy;
  protected String updatedBy;
  protected LocalDateTime createTime;
  protected LocalDateTime updateTime;
}
