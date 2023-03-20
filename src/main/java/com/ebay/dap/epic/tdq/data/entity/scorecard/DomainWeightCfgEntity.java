package com.ebay.dap.epic.tdq.data.entity.scorecard;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.entity.AuditableEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_scorecard_domain_weight_cfg")
public class DomainWeightCfgEntity extends AuditableEntity {

    private String domainName;

    private Long ruleId;

    private BigDecimal weight;

}
