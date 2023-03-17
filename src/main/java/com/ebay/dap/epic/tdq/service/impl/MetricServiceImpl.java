package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.MetricInfoMapper;
import com.ebay.dap.epic.tdq.data.mapper.mystruct.MetricMapper;
import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.vo.metric.MetricInfoVO;
import com.ebay.dap.epic.tdq.service.MetricService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MetricServiceImpl implements MetricService {

    @Autowired
    private MetricInfoMapper metricInfoMapper;

    @Autowired
    private MetricMapper metricMapper;

    @Autowired
    private ElasticsearchOperations esOperations;

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

    @Override
    public List<MetricDoc> getDailyMetrics(LocalDate dt, String metricKey) {
        String index = "tdq.batch.profiling.metric.prod." + dt.toString();
        IndexCoordinates idxNames = IndexCoordinates.of(index);

        Criteria criteria = new Criteria("metric_key").is(metricKey);
        Query query = new CriteriaQuery(criteria);

        List<MetricDoc> metricDocs = new ArrayList<>();
        SearchHits<MetricDoc> search = esOperations.search(query, MetricDoc.class, idxNames);
        for (SearchHit<MetricDoc> searchHit : search.getSearchHits()) {
            MetricDoc content = searchHit.getContent();
            metricDocs.add(content);
        }

        return metricDocs;
    }

    @Override
    public List<MetricDoc> getDailyMetricsByLabel(LocalDate dt, String label) {
        final String index = "tdq.batch.profiling.metric.prod." + dt.toString();
        IndexCoordinates idxNames = IndexCoordinates.of(index);

        Criteria criteria = new Criteria("metric_key").exists()
                                                      .and("label").is("scorecard");
        Query query = new CriteriaQuery(criteria);


        List<MetricDoc> metricDocs = new ArrayList<>();
        SearchHits<MetricDoc> search = esOperations.search(query, MetricDoc.class, idxNames);
        for (SearchHit<MetricDoc> searchHit : search.getSearchHits()) {
            MetricDoc content = searchHit.getContent();
            metricDocs.add(content);
        }

        return metricDocs;
    }


}
