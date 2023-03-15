package com.ebay.dap.epic.tdq.service.scorecard;

import com.ebay.dap.epic.tdq.data.bo.scorecard.Rule;

public interface RuleExecutor<T extends Rule> {

    boolean validate(T rule);

    T execute(T rule);
}
