package com.ebay.dap.epic.tdq.data.bo.scorecard;

import com.ebay.dap.epic.tdq.data.enums.Category;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public abstract class Rule {

    protected Long ruleId;

    protected String ruleName;

    // the order of metric key matters
    protected List<String> metricKeys;

    // the order of metric value matters
    protected List<String> metricValues;

    // scorecard will be only using daily metrics
    protected LocalDate metricDt;

    protected Category category;

    // by default, we only support two level sub category
    protected String subCategory1;

    protected String subCategory2;

    protected Integer executeOrder;

    protected BigDecimal weight;

    protected Integer score;

    protected Integer weightedScore;

}
