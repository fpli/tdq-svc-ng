package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.scorecard.CategoryResultEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.GroovyRuleDefEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.RuleResultEntity;
import com.ebay.dap.epic.tdq.data.enums.Category;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.CategoryResultMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.GroovyRuleDefMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.RuleResultMapper;
import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.vo.ScorecardDetailItemVO;
import com.ebay.dap.epic.tdq.data.vo.ScorecardItemVO;
import com.ebay.dap.epic.tdq.service.MetricService;
import com.ebay.dap.epic.tdq.service.ScorecardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ScorecardServiceImpl implements ScorecardService {

    @Autowired
    private GroovyRuleDefMapper ruleDefMapper;

    @Autowired
    private CategoryResultMapper categoryResultMapper;

    @Autowired
    private RuleResultMapper ruleResultMapper;

    @Autowired
    private MetricService metricService;

    @Override
    public List<ScorecardItemVO> listScore(LocalDate date) {
        List<ScorecardItemVO> scorecardItemVOList = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger atomicInteger = new AtomicInteger(1);
//        List<Category> categories = ruleDefMapper.listAllCategories();
        List<Category> categories = Arrays.asList(Category.values());
        categories.parallelStream().forEach(k -> {
            ScorecardItemVO scorecardItem = new ScorecardItemVO();
            scorecardItemVOList.add(scorecardItem);
            scorecardItem.setKey(k.name());
            scorecardItem.setType("category");
            scorecardItem.setId(atomicInteger.getAndIncrement());
            scorecardItem.setPid(0);
            scorecardItem.setCheckedItem(k.name());
            scorecardItem.setCategory(k.name());
            Map<String, Double> map = new HashMap<>();
            scorecardItem.setExtMap(map);
            //
            LambdaQueryWrapper<CategoryResultEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
            lambdaQueryWrapper.eq(CategoryResultEntity::getDt, date);
            lambdaQueryWrapper.eq(CategoryResultEntity::getCategory, k);
            List<CategoryResultEntity> categoryResultEntityList = categoryResultMapper.selectList(lambdaQueryWrapper);
            categoryResultEntityList.forEach(r -> map.put(r.getDomain(), r.getSubTotal().doubleValue()));

            LambdaQueryWrapper<GroovyRuleDefEntity> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(GroovyRuleDefEntity::getCategory, k);
            List<GroovyRuleDefEntity> groovyRuleDefEntityList = ruleDefMapper.selectList(queryWrapper);
            groovyRuleDefEntityList.forEach(item -> {
                LambdaQueryWrapper<RuleResultEntity> lambdaQuery = Wrappers.lambdaQuery();
                lambdaQuery.eq(RuleResultEntity::getRuleId, item.getId());
                lambdaQuery.eq(RuleResultEntity::getDt, date);
                List<RuleResultEntity> ruleResultEntityList = ruleResultMapper.selectList(lambdaQuery);
                ScorecardItemVO node = new ScorecardItemVO();
                scorecardItemVOList.add(node);
                node.setKey(item.getName());
                node.setType("checkItem");
                node.setId(atomicInteger.getAndIncrement());
                node.setPid(scorecardItem.getId());
                node.setCheckedItem(item.getName());
                node.setCategory(item.getCategory().name());
                Map<String, Double> nodeMap = new HashMap<>();
                node.setExtMap(nodeMap);
                ruleResultEntityList.forEach(ruleResultEntity -> nodeMap.put(ruleResultEntity.getDomain(), ruleResultEntity.getScore().doubleValue()));
            });

        });

        scorecardItemVOList.sort(Comparator.comparing(ScorecardItemVO::getCategory, Comparator.comparing(Category::valueOf)));
        // replace it with a flag and a map maybe better
        fillFinalScore(scorecardItemVOList, atomicInteger.getAndIncrement(), date);
        scorecardItemVOList.forEach(scorecardItemVO -> scorecardItemVO.setDate(date.toString()));
        return scorecardItemVOList;
    }

    @Override
    public List<String> fetchAvailableDates() {
        LambdaQueryWrapper<CategoryResultEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ge(CategoryResultEntity::getDt, LocalDate.now().minusMonths(3));
        List<CategoryResultEntity> categoryResultEntityList = categoryResultMapper.selectList(lambdaQueryWrapper);
        return categoryResultEntityList.stream().map(CategoryResultEntity::getDt).distinct().map(LocalDate::toString).toList();
    }

    private void fillFinalScore(List<ScorecardItemVO> scorecardItemVOList, int id, LocalDate date) {
        ScorecardItemVO scorecardItem = new ScorecardItemVO();
        scorecardItemVOList.add(scorecardItem);
        scorecardItem.setKey("Final Score");
        scorecardItem.setType("finalScore");
        scorecardItem.setId(id);
        scorecardItem.setPid(0);
        scorecardItem.setCheckedItem("");
        scorecardItem.setCategory("Final Score");
        Map<String, Double> map = new HashMap<>();
        scorecardItem.setExtMap(map);

        LambdaQueryWrapper<CategoryResultEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(CategoryResultEntity::getDt, date);
        List<CategoryResultEntity> categoryResultEntityList = categoryResultMapper.selectList(lambdaQueryWrapper);
        Map<String, CategoryResultEntity> domainMap = categoryResultEntityList.stream().collect(Collectors.toMap(CategoryResultEntity::getDomain, Function.identity(), (old, young) -> young));
        domainMap.forEach((domain, entity) -> map.put(domain, entity.getFinalScore().doubleValue()));
    }

    @Override
    public List<ScorecardDetailItemVO> listScoreDetail(String type, String name, LocalDate begin, LocalDate end) {
        List<ScorecardDetailItemVO> scorecardItemVOList = new ArrayList<>();
        switch (type) {
            case "finalScore" -> fillFinalScoreDetail(begin, end, scorecardItemVOList);
            case "checkItem" -> fillCheckedItemDetail(name, begin, end, scorecardItemVOList);
            case "category" -> fillCategoryDetail(name, begin, end, scorecardItemVOList);
        }
        scorecardItemVOList.sort(Comparator.comparing(ScorecardDetailItemVO::getDate));
        return scorecardItemVOList;
    }

    private void fillCategoryDetail(String category, LocalDate begin, LocalDate end, List<ScorecardDetailItemVO> scorecardItemVOList) {
        LambdaQueryWrapper<CategoryResultEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.between(CategoryResultEntity::getDt, begin, end);
        lambdaQueryWrapper.eq(CategoryResultEntity::getCategory, category);
        List<CategoryResultEntity> categoryResultEntityList = categoryResultMapper.selectList(lambdaQueryWrapper);
        Map<LocalDate, List<CategoryResultEntity>> dateListMap = categoryResultEntityList.stream().collect(Collectors.groupingBy(CategoryResultEntity::getDt));
        dateListMap.forEach((dt, list) -> {
            ScorecardDetailItemVO scorecardItem = new ScorecardDetailItemVO();
            scorecardItemVOList.add(scorecardItem);
            scorecardItem.setDate(dt.toString());
            Map<String, Double> map = new HashMap<>();
            scorecardItem.setExtMap(map);
            list.forEach(r -> map.put(r.getDomain(), r.getSubTotal().doubleValue()));
        });
    }

    private void fillCheckedItemDetail(String metricKey, LocalDate begin, LocalDate end, List<ScorecardDetailItemVO> scorecardItemVOList) {
        for (int i = 0; i <= Period.between(begin, end).getDays(); i++) {
            ScorecardDetailItemVO node = new ScorecardDetailItemVO();
            scorecardItemVOList.add(node);
            node.setDate(begin.plusDays(i).toString());
            Map<String, Double> nodeMap = new HashMap<>();
            node.setExtMap(nodeMap);
            List<MetricDoc> metricDocList = metricService.getDailyMetrics(begin.plusDays(i), metricKey);
            metricDocList.forEach(metricDoc -> nodeMap.put(metricDoc.getDimension().get("domain").toString(), metricDoc.getValue().doubleValue()));
        }
    }

    private void fillFinalScoreDetail(LocalDate begin, LocalDate end, List<ScorecardDetailItemVO> scorecardItemVOList) {
        LambdaQueryWrapper<CategoryResultEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.between(CategoryResultEntity::getDt, begin, end);
        List<CategoryResultEntity> categoryResultEntityList = categoryResultMapper.selectList(lambdaQueryWrapper);
        Map<LocalDate, Map<String, CategoryResultEntity>> localDateMapMap = categoryResultEntityList.stream().collect(Collectors.groupingBy(CategoryResultEntity::getDt, Collectors.toMap(CategoryResultEntity::getDomain, Function.identity(), (old, young) -> young)));
        localDateMapMap.forEach((dt, domainMap) -> {
            ScorecardDetailItemVO scorecardItem = new ScorecardDetailItemVO();
            scorecardItemVOList.add(scorecardItem);
            Map<String, Double> extMap = new HashMap<>();
            scorecardItem.setExtMap(extMap);
            domainMap.forEach((domain, entity) -> extMap.put(domain, entity.getFinalScore().doubleValue()));
        });
    }
}
