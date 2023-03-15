package com.ebay.dap.epic.tdq.service.scorecard;

import com.ebay.dap.epic.tdq.data.bo.scorecard.Rule;

public interface RuleExecutor {

    boolean validate(Rule rule);

    Rule execute(Rule rule);
}
