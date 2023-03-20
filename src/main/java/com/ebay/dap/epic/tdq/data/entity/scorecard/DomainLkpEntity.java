package com.ebay.dap.epic.tdq.data.entity.scorecard;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.entity.AuditableEntity;
import lombok.Data;

@Data
@TableName("t_scorecard_domain_lkp")
public class DomainLkpEntity extends AuditableEntity {

    private String name;

}
