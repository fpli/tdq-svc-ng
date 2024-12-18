package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.common.util.DateUtil;
import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.CategoryResultEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.GroovyRuleDefEntity;
import com.ebay.dap.epic.tdq.data.entity.scorecard.RuleResultEntity;
import com.ebay.dap.epic.tdq.data.enums.Category;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.CategoryResultMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.GroovyRuleDefMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.MetricInfoMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.RuleResultMapper;
import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.vo.ScorecardDetailItemVO;
import com.ebay.dap.epic.tdq.data.vo.ScorecardDetailVO;
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

    @Autowired
    private MetricInfoMapper metricInfoMapper;

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
                node.setCheckedItem(item.getMetricKeys());
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
        return categoryResultEntityList.stream().map(CategoryResultEntity::getDt).distinct().sorted().map(LocalDate::toString).toList();
    }

    private void fillFinalScore(List<ScorecardItemVO> scorecardItemVOList, int id, LocalDate date) {
        ScorecardItemVO scorecardItem = new ScorecardItemVO();
        scorecardItemVOList.add(scorecardItem);
        scorecardItem.setKey("Final Score");
        scorecardItem.setType("finalScore");
        scorecardItem.setId(id);
        scorecardItem.setPid(0);
        scorecardItem.setCheckedItem("FINAL SCORE");
        scorecardItem.setCategory("FINAL SCORE");
        Map<String, Double> map = new HashMap<>();
        scorecardItem.setExtMap(map);

        LambdaQueryWrapper<CategoryResultEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(CategoryResultEntity::getDt, date);
        List<CategoryResultEntity> categoryResultEntityList = categoryResultMapper.selectList(lambdaQueryWrapper);
        Map<String, CategoryResultEntity> domainMap = categoryResultEntityList.stream().collect(Collectors.toMap(CategoryResultEntity::getDomain, Function.identity(), (old, young) -> young));
        domainMap.forEach((domain, entity) -> map.put(domain, entity.getFinalScore().doubleValue()));
    }

    private Period getAvailablePeriod(String label) {
        return switch (label){
            case "last month" -> Period.ofDays(1);
            case "last three months" -> Period.ofWeeks(1);
            case "last six months" -> Period.ofWeeks(2);
            case "last year" -> Period.ofMonths(1);
            default -> throw new IllegalStateException("Unexpected value: " + label);
        };
    }

    @Override
    public ScorecardDetailVO listScoreDetail(String type, String name, String label, LocalDate begin, LocalDate end) {
        log.info("type:{}, name: {}, label: {}, begin: {}, end: {}", type, name, label, begin, end);
        Period period = getAvailablePeriod(label);
        List<LocalDate> expectedDates = new ArrayList<>(DateUtil.datesStepBackBetween(begin, end, period).toList());
//        List<LocalDate> expectedDates = new ArrayList<>(begin.datesUntil(end.plusDays(1)).toList());
        ScorecardDetailVO scorecardDetailVO = new ScorecardDetailVO();
        fillDateRange(scorecardDetailVO.getDateRangeItemList());
        List<ScorecardDetailItemVO> scorecardItemVOList = scorecardDetailVO.getList();
        switch (type) {
            case "finalScore" -> fillFinalScoreDetail(expectedDates, scorecardItemVOList);
            case "checkItem" -> fillCheckedItemDetail(name, expectedDates, scorecardItemVOList, scorecardDetailVO.getBasicInfo());
            case "category" -> fillCategoryDetail(name, expectedDates, scorecardItemVOList);
        }
        // fill absent date
        if (!expectedDates.isEmpty()){
            expectedDates.forEach(dt -> {
                ScorecardDetailItemVO scorecardDetailItemVO = new ScorecardDetailItemVO();
                scorecardItemVOList.add(scorecardDetailItemVO);
                scorecardDetailItemVO.setDate(dt.toString());
            });
        }
        scorecardItemVOList.sort(Comparator.comparing(ScorecardDetailItemVO::getDate));
        return scorecardDetailVO;
    }

    private void fillDateRange(List<ScorecardDetailVO.DateRangeItem> dateRangeItemList){
        LocalDate pos = LocalDate.now().minusDays(1);
        ScorecardDetailVO.DateRangeItem lastOneMonth = new ScorecardDetailVO.DateRangeItem();
        lastOneMonth.setValue(pos.minusMonths(1).toString());
        lastOneMonth.setLabel("last month");
        ScorecardDetailVO.DateRangeItem lastThreeMonths = new ScorecardDetailVO.DateRangeItem();
        lastThreeMonths.setValue(pos.minusMonths(3).toString());
        lastThreeMonths.setLabel("last three months");
        ScorecardDetailVO.DateRangeItem lastSixMonths = new ScorecardDetailVO.DateRangeItem();
        lastSixMonths.setValue(pos.minusMonths(6).toString());
        lastSixMonths.setLabel("last six months");
        ScorecardDetailVO.DateRangeItem lastOneYear = new ScorecardDetailVO.DateRangeItem();
        lastOneYear.setValue(pos.minusYears(1).toString());
        lastOneYear.setLabel("last year");

        dateRangeItemList.add(lastOneYear);
        dateRangeItemList.add(lastSixMonths);
        dateRangeItemList.add(lastThreeMonths);
        dateRangeItemList.add(lastOneMonth);
        dateRangeItemList.sort(Comparator.comparing(ScorecardDetailVO.DateRangeItem::getValue).reversed());
    }

    private void fillCategoryDetail(String category, List<LocalDate> expectedDates, List<ScorecardDetailItemVO> scorecardItemVOList) {
        LambdaQueryWrapper<CategoryResultEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.in(CategoryResultEntity::getDt, expectedDates);
        lambdaQueryWrapper.eq(CategoryResultEntity::getCategory, category);
        List<CategoryResultEntity> categoryResultEntityList = categoryResultMapper.selectList(lambdaQueryWrapper);
        Map<LocalDate, List<CategoryResultEntity>> dateListMap = categoryResultEntityList.stream().collect(Collectors.groupingBy(CategoryResultEntity::getDt));
        dateListMap.forEach((dt, list) -> {
            expectedDates.remove(dt);
            ScorecardDetailItemVO scorecardDetailItemVO = new ScorecardDetailItemVO();
            scorecardItemVOList.add(scorecardDetailItemVO);
            scorecardDetailItemVO.setDate(dt.toString());
            Map<String, Double> map = new HashMap<>();
            scorecardDetailItemVO.setExtMap(map);
            list.forEach(r -> map.put(r.getDomain(), r.getSubTotal().doubleValue()));
        });
    }

    private void fillCheckedItemDetail(String metricKey, List<LocalDate> expectedDates, List<ScorecardDetailItemVO> scorecardItemVOList, Map<String, String> basicInfo) {
        LambdaQueryWrapper<MetricInfoEntity> lambdaQueryWrapper =  Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(MetricInfoEntity::getMetricKey, metricKey);
        MetricInfoEntity metricInfoEntity = metricInfoMapper.selectOne(lambdaQueryWrapper);

        basicInfo.put("metric_key", metricInfoEntity.getMetricKey());
//        basicInfo.put("metric_key", metricKey);
        // todo: fill ext info
        basicInfo.put("metric_name", metricInfoEntity.getMetricName());
//        basicInfo.put("metric_name", "metricInfoEntity.getMetricName()");
        basicInfo.put("description", metricInfoEntity.getDescription());
//        basicInfo.put("description", "metricInfoEntity.getDescription()");
        ListIterator<LocalDate> listIterator = expectedDates.listIterator();
        while (listIterator.hasNext()){
            LocalDate dt = listIterator.next();
            try {
                List<MetricDoc> metricDocList = metricService.getDailyMetrics(dt, metricKey);
                ScorecardDetailItemVO scorecardDetailItemVO = new ScorecardDetailItemVO();
                scorecardItemVOList.add(scorecardDetailItemVO);
                scorecardDetailItemVO.setDate(dt.toString());
                Map<String, Double> extMap = new HashMap<>();
                scorecardDetailItemVO.setExtMap(extMap);
                metricDocList.forEach(metricDoc -> extMap.put(metricDoc.getDimension().get("domain").toString(), metricDoc.getValue().doubleValue()));
                listIterator.remove();
            } catch (Exception e){
                log.error("some errors occurred while querying metric ", e);
            }
        }
    }

    private void fillFinalScoreDetail(List<LocalDate> expectedDates, List<ScorecardDetailItemVO> scorecardItemVOList) {
        LambdaQueryWrapper<CategoryResultEntity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.in(CategoryResultEntity::getDt, expectedDates);
        List<CategoryResultEntity> categoryResultEntityList = categoryResultMapper.selectList(lambdaQueryWrapper);
        Map<LocalDate, Map<String, CategoryResultEntity>> localDateMapMap = categoryResultEntityList.stream().collect(Collectors.groupingBy(CategoryResultEntity::getDt, Collectors.toMap(CategoryResultEntity::getDomain, Function.identity(), (old, young) -> young)));
        localDateMapMap.forEach((dt, domainMap) -> {
            ScorecardDetailItemVO scorecardDetailItemVO = new ScorecardDetailItemVO();
            scorecardItemVOList.add(scorecardDetailItemVO);
            scorecardDetailItemVO.setDate(dt.toString());
            expectedDates.remove(dt);
            Map<String, Double> extMap = new HashMap<>();
            scorecardDetailItemVO.setExtMap(extMap);
            domainMap.forEach((domain, entity) -> extMap.put(domain, entity.getFinalScore().doubleValue()));
        });
    }
}
