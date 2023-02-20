package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.report.MetadataDetailEntity;
import com.ebay.dap.epic.tdq.data.entity.report.MetadataSummaryEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.MetadataDetailMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.MetadataSummaryMapper;
import com.ebay.dap.epic.tdq.data.vo.report.MetadataDetailVo;
import com.ebay.dap.epic.tdq.data.vo.report.MetadataSummaryVo;
import com.ebay.dap.epic.tdq.service.ReportService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private final HashMap<Integer, String> metrics = Maps.newHashMap();

    @Autowired
    private MetadataSummaryMapper metadataSummaryMapper;

    @Autowired
    private MetadataDetailMapper metadataDetailMapper;

    @PostConstruct
    public void init() {
        // TODO(yxiao6): remove hard-coded metrics look-up
        // click metrics
        metrics.put(1, "Total click traffic");
        metrics.put(2, "Click traffic without Click ID");
        metrics.put(3, "Click traffic with Click ID but not registered in Braavos");
        metrics.put(4, "Click traffic with Invalid Life Cycle State Click ID");

        // module click metrics
        metrics.put(5, "Total click traffic");
        metrics.put(6, "Click traffic without Module ID");
        metrics.put(7, "Click traffic with Module ID but not registered in Braavos");
        metrics.put(8, "Click traffic with Invalid Life Cycle State Module ID");

        // module view metrics
        metrics.put(9, "Total module view traffic");
        metrics.put(10, "View traffic without Module ID");
        metrics.put(11, "View traffic with Module ID but not registered in Braavos");
        metrics.put(12, "View traffic with Invalid Life Cycle State Module ID");
    }

    @Override
    public List<MetadataSummaryVo> getClickSummary(LocalDate dt) {
        log.info("Get Click metadata coverage summary report");
        return getSummaryReportVoList("Click", dt);
    }

    @Override
    public List<MetadataSummaryVo> getModuleSummary(LocalDate dt) {
        log.info("Get Module metadata coverage summary report");
        return getSummaryReportVoList("Module", dt);
    }

    @Override
    public List<MetadataDetailVo> getClickDetail(String domain, Integer metricId, LocalDate dt) {
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(metricId);
        Preconditions.checkNotNull(dt);

        log.info("Get Click metadata coverage detail report of date {} for {} domain, metricId is {}", dt, domain, metricId);

        final String metadataType = "Click";

        LambdaQueryWrapper<MetadataDetailEntity> queryWrapper = Wrappers.<MetadataDetailEntity>lambdaQuery()
                                                                        .eq(MetadataDetailEntity::getDt, dt)
                                                                        .eq(MetadataDetailEntity::getMetadataType, metadataType)
                                                                        .eq(MetadataDetailEntity::getDomain, domain)
                                                                        .eq(MetadataDetailEntity::getMetricId, metricId);

        List<MetadataDetailEntity> entities = metadataDetailMapper.selectList(queryWrapper);

        return entities.stream().map(e -> {
            MetadataDetailVo vo = new MetadataDetailVo();
            BeanUtils.copyProperties(e, vo);
            vo.setElementId(e.getElementInstanceId());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MetadataDetailVo> getModuleDetail(String domain, Integer metricId, LocalDate dt) {
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(metricId);
        Preconditions.checkNotNull(dt);

        log.info("Get Module metadata coverage detail report of date {} for {} domain, metricId is {}", dt, domain, metricId);

        final String metadataType = "Module";

        LambdaQueryWrapper<MetadataDetailEntity> queryWrapper = Wrappers.<MetadataDetailEntity>lambdaQuery()
                                                                        .eq(MetadataDetailEntity::getDt, dt)
                                                                        .eq(MetadataDetailEntity::getMetadataType, metadataType)
                                                                        .eq(MetadataDetailEntity::getDomain, domain)
                                                                        .eq(MetadataDetailEntity::getMetricId, metricId);

        List<MetadataDetailEntity> entities = metadataDetailMapper.selectList(queryWrapper);
        return entities.stream().map(e -> {
            MetadataDetailVo vo = new MetadataDetailVo();
            BeanUtils.copyProperties(e, vo);
            vo.setElementId(e.getElementInstanceId());
            return vo;
        }).collect(Collectors.toList());
    }

    private List<MetadataSummaryVo> getSummaryReportVoList(String metadataType, LocalDate dt) {
        if (dt == null) {
            dt = getLatestDtOfSummary();
            log.info("No date provided, use the latest source date {} to get the metadata summary.", dt);
        }

        LambdaQueryWrapper<MetadataSummaryEntity> queryWrapper = Wrappers.<MetadataSummaryEntity>lambdaQuery()
                                                                         .eq(MetadataSummaryEntity::getDt, dt)
                                                                         .eq(MetadataSummaryEntity::getMetadataType, metadataType);

        log.info("Retrieve {} metadata coverage summary report of date {} from database.", metadataType, dt);

        List<MetadataSummaryEntity> entities = metadataSummaryMapper.selectList(queryWrapper);
        return entities.stream().map(e -> {
            MetadataSummaryVo vo = new MetadataSummaryVo();
            BeanUtils.copyProperties(e, vo);
            vo.setMetric(metrics.get(e.getMetricId()));
            vo.setAll(e.getAllExp());
            return vo;
        }).collect(Collectors.toList());
    }

    private LocalDate getLatestDtOfSummary() {
        QueryWrapper<MetadataSummaryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(dt) as dt");
        MetadataSummaryEntity entity = metadataSummaryMapper.selectOne(queryWrapper);
        return entity.getDt();
    }

}
