package com.ebay.dap.epic.tdq.service.scorecard;

import com.ebay.dap.epic.tdq.data.bo.scorecard.Rule;

public class GroovyScriptRuleExecutor implements RuleExecutor {


    @Override
    public boolean validate(Rule rule) {
        return true;
    }

    @Override
    public Rule execute(Rule rule) {
        return null;
    }
}
