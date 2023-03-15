package com.ebay.dap.epic.tdq.service.scorecard;

import com.ebay.dap.epic.tdq.data.bo.scorecard.GroovyScriptRule;
import com.ebay.dap.epic.tdq.dsl.GroovyEngine;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GroovyScriptRuleExecutor implements RuleExecutor<GroovyScriptRule> {

    @Override
    public boolean validate(GroovyScriptRule rule) {
        //TODO: not implemented
        return true;
    }

    @Override
    public GroovyScriptRule execute(GroovyScriptRule rule) {
        Preconditions.checkNotNull(rule.getScript());
        Preconditions.checkNotNull(rule.getMetricKeys());
        Preconditions.checkNotNull(rule.getMetricValues());

        String script = rule.getScript();
        Map<String, Object> bindings = new HashMap<>();

        for (int i = 0; i < rule.getMetricKeys().size(); i++) {
            bindings.put(rule.getMetricKeys().get(i), rule.getMetricValues().get(i));
        }

        Integer score = GroovyEngine.evalAsInt(script, bindings);
        rule.setScore(score);
        return rule;
    }
}
