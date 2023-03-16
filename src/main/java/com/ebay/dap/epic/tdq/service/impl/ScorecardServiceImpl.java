package com.ebay.dap.epic.tdq.service.impl;

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
        ScorecardItemVO scorecardItemVO = new ScorecardItemVO();
        scorecardItemVOList.add(scorecardItemVO);
        scorecardItemVO.setId(1);
        scorecardItemVO.setPid(0);
        scorecardItemVO.setKey("c_");
        scorecardItemVO.setCategory("completeness");
        scorecardItemVO.setCheckedItem("Guid");
        Map<String, Double> extMap = new HashMap<>();
        scorecardItemVO.setExtMap(extMap);
        extMap.put("viewItem", 95D);
        extMap.put("search", 95D);
        extMap.put("myEbay", 95D);
        extMap.put("checkout", 95D);
        extMap.put("signIn", 95D);
        extMap.put("homePage", 95D);

        scorecardItemVO = new ScorecardItemVO();
        scorecardItemVOList.add(scorecardItemVO);
        scorecardItemVO.setId(2);
        scorecardItemVO.setPid(1);
        scorecardItemVO.setKey("sid");
        scorecardItemVO.setCategory("completeness");
        scorecardItemVO.setCheckedItem("sid");
        extMap = new HashMap<>();
        scorecardItemVO.setExtMap(extMap);
        extMap.put("viewItem", 95D);
        extMap.put("search", 95D);
        extMap.put("myEbay", 95D);
        extMap.put("checkout", 95D);
        extMap.put("signIn", 95D);
        extMap.put("homePage", 95D);
        return scorecardItemVOList;
    }
}
