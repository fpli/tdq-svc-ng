package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.MetricInfoMapper;
import com.ebay.dap.epic.tdq.data.mapper.mystruct.MetricMapper;
import com.ebay.dap.epic.tdq.data.vo.metric.MetricInfoVO;
import com.ebay.dap.epic.tdq.service.MetricService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MetricServiceImpl implements MetricService {

    @Autowired
    private MetricInfoMapper metricInfoMapper;

    @Autowired
    private MetricMapper metricMapper;

    @Override
    public MetricInfoVO create(MetricInfoVO metricInfoVO) {
        MetricInfoEntity metricInfoEntity = metricMapper.toEntity(metricInfoVO);
        // set init status as active(1)
        metricInfoEntity.setStatus(1);
        // set init version to 1
        metricInfoEntity.setVersion(1);

        metricInfoMapper.insert(metricInfoEntity);

        return metricInfoVO;
    }

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
