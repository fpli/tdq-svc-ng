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
import com.ebay.dap.epic.tdq.data.vo.ScorecardItemVO;
import com.ebay.dap.epic.tdq.service.ScorecardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class ScorecardServiceImpl implements ScorecardService {

    @Autowired
    private GroovyRuleDefMapper ruleDefMapper;

    @Autowired
    private CategoryResultMapper categoryResultMapper;

    @Autowired
    private RuleResultMapper ruleResultMapper;

    @Override
    public List<ScorecardItemVO> listScore(LocalDate date) {
        List<ScorecardItemVO> scorecardItemVOList = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger(1);
        List<Category> categories = ruleDefMapper.listAllCategories();

        categories.forEach(k -> {
            ScorecardItemVO scorecardItem = new ScorecardItemVO();
            scorecardItemVOList.add(scorecardItem);
            scorecardItem.setKey(k.name());
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
                node.setId(atomicInteger.getAndIncrement());
                node.setPid(scorecardItem.getId());
                node.setCheckedItem(item.getName());
                node.setCategory(item.getCategory().name());
                Map<String, Double> nodeMap = new HashMap<>();
                node.setExtMap(nodeMap);
                ruleResultEntityList.forEach(ruleResultEntity -> nodeMap.put(ruleResultEntity.getDomain(), ruleResultEntity.getScore().doubleValue()));
            });
        });
        return scorecardItemVOList;
    }
}
