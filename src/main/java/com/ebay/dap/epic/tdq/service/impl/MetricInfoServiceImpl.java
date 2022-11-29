package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.MetricInfoMapper;
import com.ebay.dap.epic.tdq.service.MetricInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MetricInfoServiceImpl implements MetricInfoService {

    @Autowired
    private MetricInfoMapper metricInfoMapper;


    @Override
    public List<MetricInfoEntity> listMetricAllInfo() {
        return metricInfoMapper.selectList(null);
    }

    @Override
    public MetricInfoEntity getMetricInfoEntityByMetricKey(String metricKey) {
        MetricInfoEntity metricInfo = new MetricInfoEntity();
        metricInfo.setMetricKey(metricKey);
        return metricInfoMapper.selectOne(Wrappers.query(metricInfo));
    }


}
