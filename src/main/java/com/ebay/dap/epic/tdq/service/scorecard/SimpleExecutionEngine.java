package com.ebay.dap.epic.tdq.service.scorecard;

import com.ebay.dap.epic.tdq.data.bo.scorecard.CategoryResult;
import com.ebay.dap.epic.tdq.data.bo.scorecard.GroovyScriptRule;
import com.ebay.dap.epic.tdq.data.bo.scorecard.Rule;
import com.ebay.dap.epic.tdq.data.bo.scorecard.ScorecardResult;
import com.ebay.dap.epic.tdq.data.client.pronto.ProntoClient;
import com.ebay.dap.epic.tdq.data.entity.scorecard.CategoryResultEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.DomainWeightLkpEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.GroovyRuleDefEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.RuleResultEntity;
import com.ebay.dap.epic.tdq.data.enums.Category;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.GroovyRuleDefMapper;
import com.ebay.dap.epic.tdq.data.repository.CategoryResultRepository;
import com.ebay.dap.epic.tdq.data.repository.RuleResultRepository;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Component
public class SimpleExecutionEngine implements ExecutionEngine {


    @Autowired
    private GroovyRuleDefMapper ruleDefMapper;

    @Autowired
    private RuleResultRepository ruleResultRepository;

    @Autowired
    private CategoryResultRepository categoryResultRepository;

    @Autowired
    private RuleExecutor executor;

    private ProntoClient prontoClient;

    //FIXME(yxiao6): hard-code for now
    private List<String> domainList = Lists.newArrayList(
            "ViewItem",
            "Search",
            "MyEbay",
            "Notification",
            "Checkout",
            "SignIn"
    );

    //FIXME(yxiao6): hard-code for now
    private List<DomainWeightLkpEntity> domainWeightList = Lists.newArrayList(
            new DomainWeightLkpEntity("ViewItem", 1001L, new BigDecimal("0.5")),
            new DomainWeightLkpEntity("Search", 1001L, new BigDecimal("0.7"))
    );


    /***
     *  below are the steps to process scorecard:
     *  1. get scorecard rule definitions from db
     *  2. covert rule definitions to groovy rule BO
     *  3. get metric values for metrics in rules
     *  4. execute groovy rule scripts to get score of rule
     *  5. get final score based on the domain specified weight
     *  6. get category sub-total score for each domain
     *  7. save the scorecard results into database
     *
     * @param dt
     */
    @Override
    public void process(LocalDate dt) {
        List<ScorecardResult> domainScorecardResults = new ArrayList<>();

        // 1. get scorecard rule definitions from db
        List<GroovyRuleDefEntity> ruleDefEntities = ruleDefMapper.selectList(null);


        // 2. covert rule definitions to groovy rule BO
        List<GroovyScriptRule> groovyScriptRules = new ArrayList<>();
        for (GroovyRuleDefEntity ruleDef : ruleDefEntities) {
            GroovyScriptRule rule = new GroovyScriptRule();
            rule.setRuleId(ruleDef.getId());
            rule.setRuleName(ruleDef.getName());
            rule.setCategory(ruleDef.getCategory());
            rule.setSubCategory1(ruleDef.getSubCategory1());
            rule.setSubCategory2(ruleDef.getSubCategory2());
            rule.setWeight(ruleDef.getDefaultWeight());
            rule.setMetricKeys(Arrays.stream(ruleDef.getMetricKeys().split(",")).collect(Collectors.toList()));
            rule.setMetricDt(dt);

            groovyScriptRules.add(rule);
        }

        Map<String, List<DomainWeightLkpEntity>> domainWeightLkp = domainWeightList.stream()
                                                                                   .collect(groupingBy(DomainWeightLkpEntity::getDomainName));

        // calculate scorecard for each domain
        for (String domain : domainList) {
            ScorecardResult scorecardResult = new ScorecardResult();
            scorecardResult.setDomainName(domain);
            scorecardResult.setDt(dt);

            // 3. get metric values for metrics in rules
            for (GroovyScriptRule rule : groovyScriptRules) {
                List<Object> metricValues = new ArrayList<>();
                for (String metricKey : rule.getMetricKeys()) {
                    Object metricValue = prontoClient.getDailyMetric(dt, metricKey, domain);
                    metricValues.add(metricValue);
                }
                rule.setMetricValues(metricValues);
            }

            // 4. execute groovy rule scripts to get score of rule
            for (GroovyScriptRule rule : groovyScriptRules) {
                executor.execute(rule);
            }

            // 5. get final score based on the domain specified weight
            for (GroovyScriptRule rule : groovyScriptRules) {
                if (domainWeightLkp.containsKey(domain)) {
                    List<DomainWeightLkpEntity> domainWeightLkpEntities = domainWeightLkp.get(domain);
                    for (DomainWeightLkpEntity domainWeightLkpEntity : domainWeightLkpEntities) {
                        if (domainWeightLkpEntity.getRuleId().equals(rule.getRuleId())) {
                            rule.setWeight(domainWeightLkpEntity.getWeight());
                        }
                    }
                }

                rule.setWeightedScore(rule.getWeight().multiply(new BigDecimal(rule.getScore())).intValue());
            }


            // 6. get category sub-total score for each domain
            List<CategoryResult> categoryResults = new ArrayList<>();
            Map<Category, List<Rule>> categoryRules = groovyScriptRules.stream().collect(groupingBy(Rule::getCategory));
            for (Map.Entry<Category, List<Rule>> entry : categoryRules.entrySet()) {
                Category category = entry.getKey();
                List<Rule> rules = entry.getValue();
                CategoryResult result = new CategoryResult();

                Integer asInt = rules.stream().mapToInt(Rule::getWeightedScore)
                                     .reduce(Integer::sum)
                                     .getAsInt();

                Double asDouble = rules.stream().mapToDouble(e -> e.getWeight().doubleValue())
                                       .reduce(Double::sum)
                                       .getAsDouble();

                Double v = (asInt / asDouble);

                result.setSubTotal(v.intValue());
                result.setCategory(category);

                categoryResults.add(result);
            }


        }

        // 7. save the scorecard results into database
        List<CategoryResultEntity> categoryResultEntityList = new LinkedList<>();
        List<RuleResultEntity> ruleResultEntityList = new LinkedList<>();

        for (ScorecardResult domainScorecardResult : domainScorecardResults) {
            final String domain = domainScorecardResult.getDomainName();
            final Integer finalScore = domainScorecardResult.getFinalScore();

            for (CategoryResult categoryResult : domainScorecardResult.getCategoryResults()) {
                CategoryResultEntity categoryResultEntity = new CategoryResultEntity();
                categoryResultEntity.setCategory(categoryResult.getCategory());
                categoryResultEntity.setSubTotal(categoryResult.getSubTotal());
                categoryResultEntity.setDomain(domain);
                categoryResultEntity.setFinalScore(finalScore);
                categoryResultEntity.setDt(dt);
                categoryResultEntityList.add(categoryResultEntity);

                for (Rule rule : categoryResult.getRules()) {
                    RuleResultEntity ruleResultEntity = new RuleResultEntity();
                    ruleResultEntity.setRuleId(rule.getRuleId());
                    ruleResultEntity.setDomain(domain);
                    ruleResultEntity.setScore(rule.getWeightedScore());
                    ruleResultEntity.setDt(dt);
                    ruleResultEntityList.add(ruleResultEntity);
                }
            }
        }
        // FIXME: add transactional support
        // save results to database
        ruleResultRepository.saveBatch(ruleResultEntityList);
        categoryResultRepository.saveBatch(categoryResultEntityList);


    }
}
