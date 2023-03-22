package com.ebay.dap.epic.tdq.service.scorecard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ebay.dap.epic.tdq.common.exception.ScorecardExecutionException;
import com.ebay.dap.epic.tdq.data.bo.scorecard.CategoryResult;
import com.ebay.dap.epic.tdq.data.bo.scorecard.GroovyScriptRule;
import com.ebay.dap.epic.tdq.data.bo.scorecard.Rule;
import com.ebay.dap.epic.tdq.data.bo.scorecard.ScorecardResult;
import com.ebay.dap.epic.tdq.data.entity.scorecard.CategoryResultEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.DomainLkpEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.DomainWeightCfgEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.GroovyRuleDefEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.RuleResultEntity;
import com.ebay.dap.epic.tdq.data.enums.Category;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.DomainLkpMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.DomainWeightCfgMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.GroovyRuleDefMapper;
import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.repository.CategoryResultRepository;
import com.ebay.dap.epic.tdq.data.repository.RuleResultRepository;
import com.ebay.dap.epic.tdq.service.MetricService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Component
public class SimpleExecutionEngine implements ExecutionEngine {

    @Autowired
    private GroovyRuleDefMapper ruleDefMapper;

    @Autowired
    private RuleResultRepository ruleResultRepository;

    @Autowired
    private CategoryResultRepository categoryResultRepository;

    @Autowired
    private DomainLkpMapper domainLkpMapper;

    @Autowired
    private DomainWeightCfgMapper domainWeightCfgMapper;

    @Autowired
    private RuleExecutor executor;

    @Autowired
    private MetricService metricService;


    /***
     *  below are the steps to process scorecard:
     *  1. load domain lkp data from database
     *  2. get scorecard rule definitions from database
     *  3. covert rule definition to GroovyScriptRule BO
     *  4. get all metrics details from Pronto
     *  5. calculate scorecard for each domain
     *     - 5.1 parse metric values and set in rules
     *     - 5.2. execute groovy rule script to get score
     *     - 5.3 get rule weighted score based on the domain specified weight config
     *     - 5.4 get category sub-total score based on rule results
     *     - 5.5 get final score for the domain based on category sub-total
     *  6. save the scorecard results into database
     *
     * @param dt
     */
    @Transactional
    @Override
    public void process(LocalDate dt) {
        Preconditions.checkNotNull(dt);

        final List<ScorecardResult> domainScorecardResults = new ArrayList<>();

        // 1. load domain lkp data from database
        final List<String> domainList = domainLkpMapper.findAll().stream().map(DomainLkpEntity::getName).toList();
        if (CollectionUtils.isEmpty(domainList)) {
            throw new ScorecardExecutionException("No domain found");
        }
        log.info("Scorecard domain list is: {}", domainList);

        List<DomainWeightCfgEntity> domainWeightCfgEntities = domainWeightCfgMapper.findAll();
        Map<String, List<DomainWeightCfgEntity>> domainWeightCfgMap =
                domainWeightCfgEntities.stream()
                                       .collect(groupingBy(DomainWeightCfgEntity::getDomainName));


        // 2. get scorecard rule definitions from database
        List<GroovyRuleDefEntity> ruleDefEntities = ruleDefMapper.selectList(null);
        if (CollectionUtils.isEmpty(ruleDefEntities)) {
            throw new ScorecardExecutionException("No scorecard rule definition found");
        }

        // 3. covert rule definition to GroovyScriptRule BO
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
            rule.setScript(ruleDef.getGroovyScript());

            groovyScriptRules.add(rule);
        }
        Map<Category, List<Rule>> categoryRules = groovyScriptRules.stream().collect(groupingBy(Rule::getCategory));

        // 4. get all metrics details from Pronto
        List<MetricDoc> scorecardMetrics = metricService.getScorecardMetrics(dt);
        Map<String, List<MetricDoc>> domainMetricValues =
                scorecardMetrics.stream()
                                .collect(groupingBy(metricDoc -> metricDoc.getDimension().get("domain").toString()));

        // 5. calculate scorecard for each domain
        for (String domain : domainList) {
            log.info("Calculate Scorecard for domain: {}", domain);
            ScorecardResult scorecardResult = new ScorecardResult();
            scorecardResult.setDomainName(domain);
            scorecardResult.setDt(dt);

            if (!domainMetricValues.containsKey(domain)) {
                throw new ScorecardExecutionException("No metrics value found for domain " + domain);
            }

            List<MetricDoc> metricDocs = domainMetricValues.get(domain);

            // 5.1 parse metric values and set in rules
            for (GroovyScriptRule rule : groovyScriptRules) {
                List<Object> metricValues = new ArrayList<>();
                for (String metricKey : rule.getMetricKeys()) {
                    for (MetricDoc metricDoc : metricDocs) {
                        if (metricDoc.getMetricKey().equals(metricKey)) {
                            metricValues.add(metricDoc.getValue());
                            break;
                        }
                    }
                }

                if (metricValues.size() != rule.getMetricKeys().size()) {
                    throw new ScorecardExecutionException("Metric values size is not equal to metric key size");
                }

                rule.setMetricValues(metricValues);
            }

            // 5.2. execute groovy rule script to get score
            for (GroovyScriptRule rule : groovyScriptRules) {
                executor.execute(rule);
            }

            // 5.3 get rule weighted score based on the domain specified weight config
            for (GroovyScriptRule rule : groovyScriptRules) {
                if (domainWeightCfgMap.containsKey(domain)) {
                    List<DomainWeightCfgEntity> domainWeightLkpEntities = domainWeightCfgMap.get(domain);
                    for (DomainWeightCfgEntity domainWeightLkpEntity : domainWeightLkpEntities) {
                        if (domainWeightLkpEntity.getRuleId().equals(rule.getRuleId())) {
                            rule.setWeight(domainWeightLkpEntity.getWeight());
                            break;
                        }
                    }
                }
                rule.setWeightedScore(rule.getWeight().multiply(new BigDecimal(rule.getScore())).intValue());
            }

            // 5.4 get category sub-total score based on rule results
            List<CategoryResult> categoryResults = new ArrayList<>();
            for (Map.Entry<Category, List<Rule>> entry : categoryRules.entrySet()) {
                Category category = entry.getKey();
                List<Rule> rules = entry.getValue();
                CategoryResult categoryResult = new CategoryResult();

                int sumScore = rules.stream()
                                    .mapToInt(Rule::getWeightedScore)
                                    .sum();

                double sumWeight = rules.stream()
                                        .mapToDouble(e -> e.getWeight().doubleValue())
                                        .sum();

                double subTotal = (sumScore / sumWeight);

                categoryResult.setSubTotal((int) subTotal);
                categoryResult.setCategory(category);
                categoryResult.setRules(rules);
                categoryResults.add(categoryResult);
            }
            if (CollectionUtils.isEmpty(categoryResults)) {
                throw new ScorecardExecutionException("No category results found");
            }

            // 5.5 get final score for the domain based on category sub-total
            double finalScore = categoryResults.stream()
                                               .mapToInt(CategoryResult::getSubTotal)
                                               .average()
                                               .getAsDouble();
            scorecardResult.setFinalScore((int) finalScore);
            scorecardResult.setCategoryResults(categoryResults);
            domainScorecardResults.add(scorecardResult);
            log.info("Finished Scorecard calculation for domain: {}", domain);
        }

        // check if results count is equal to domain counts
        if (domainScorecardResults.size() != domainList.size()) {
            throw new ScorecardExecutionException("Domain Scorecard results size is not equal to domain list size");
        }

        // 6. save the scorecard results into database
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

        // save rule results to database
        LambdaQueryWrapper<RuleResultEntity> ruleQueryWrapper = new LambdaQueryWrapper<>();
        ruleQueryWrapper.eq(RuleResultEntity::getDt, dt.toString());
        ruleResultRepository.remove(ruleQueryWrapper);
        ruleResultRepository.saveBatch(ruleResultEntityList);
        // save category results to database
        LambdaQueryWrapper<CategoryResultEntity> categoryQueryWrapper = new LambdaQueryWrapper<>();
        categoryQueryWrapper.eq(CategoryResultEntity::getDt, dt.toString());
        categoryResultRepository.remove(categoryQueryWrapper);
        categoryResultRepository.saveBatch(categoryResultEntityList);
    }
}
