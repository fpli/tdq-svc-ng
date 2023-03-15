package com.ebay.dap.epic.tdq.data.entity.scorecard;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ebay.dap.epic.tdq.data.entity.AuditableEntity;
import com.ebay.dap.epic.tdq.data.enums.Category;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_scorecard_groovy_rule_def")
public class GroovyRuleDefEntity extends AuditableEntity {

    private String name;

    // the order of metric key matters, use ',' as delimiter
    private String metricKeys;

    private Category category;

    // by default, we only support two level sub category
    private String subCategory1;

    private String subCategory2;

    private Integer executeOrder;

    private BigDecimal defaultWeight;

    private String groovyScript;

}
