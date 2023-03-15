package com.ebay.dap.epic.tdq.data.entity.scorecard;


import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.entity.AuditableEntity;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("t_scorecard_rule_result")
public class RuleResultEntity extends AuditableEntity {

    private String domain;

    private Long ruleId;

    private Integer score;

    private LocalDate dt;

}
