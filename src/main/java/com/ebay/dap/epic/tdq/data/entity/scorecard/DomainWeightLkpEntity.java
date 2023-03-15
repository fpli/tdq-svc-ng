package com.ebay.dap.epic.tdq.data.entity.scorecard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DomainWeightLkpEntity {

    private String domainName;

    private Long ruleId;

    private BigDecimal weight;

}
