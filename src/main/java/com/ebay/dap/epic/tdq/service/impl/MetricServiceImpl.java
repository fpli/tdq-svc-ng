package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.MetricInfoMapper;
import com.ebay.dap.epic.tdq.data.mapper.mystruct.MetricMapper;
import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.pronto.PageMetricDoc;
import com.ebay.dap.epic.tdq.data.vo.metric.MetricInfoVO;
import com.ebay.dap.epic.tdq.service.MetricService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ByQueryResponse;
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
        log.info("Query Pronto for metric details");
        final String index = "tdq.batch.profiling.metric.prod." + dt.toString();
        IndexCoordinates idxNames = IndexCoordinates.of(index);

        Criteria criteria = new Criteria("metric_key").exists()
                                                      .and("dt").is(dt.toString())
                                                      .and("labels").is(label);

        Query query = new CriteriaQuery(criteria);

        List<MetricDoc> metricDocs = new ArrayList<>();
        SearchHits<MetricDoc> search = esOperations.search(query, MetricDoc.class, idxNames);
        for (SearchHit<MetricDoc> searchHit : search.getSearchHits()) {
            MetricDoc content = searchHit.getContent();
            metricDocs.add(content);
        }

        log.info("Finished querying Pronto, returned {} documents", metricDocs.size());
        return metricDocs;
    }

    @Override
    public List<MetricDoc> getScorecardMetrics(LocalDate dt) {
        return this.getDailyMetricsByLabel(dt, "scorecard");
    }

    /***
     * get metric from es, if date is not specified, default behavior is to get last 30 days metrics
     *
     * @param metricKey
     * @return
     */
    @Override
    public List<MetricDoc> getDailyMetricSeries(String metricKey, LocalDate endDt, int size) {
        Preconditions.checkNotNull(metricKey);
        String indexNames = "tdq.batch.profiling.metric.prod.*";
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexNames);

        // FIXME: handle errors: metrics data missing
        LocalDate startDt = endDt.minusDays(size);
        Criteria criteria = new Criteria("metric_key").is(metricKey)
                                                      .and("dimension").exists().not()
                                                      .and("dt").greaterThan(startDt.toString())
                                                      .and("dt").lessThanEqual(endDt.toString());

        Sort sort = Sort.by("dt").ascending();
        Query query = new CriteriaQuery(criteria).addSort(sort);
        SearchHits<MetricDoc> searchResults = esOperations.search(query, MetricDoc.class, indexCoordinates);
        List<MetricDoc> metricDocs = convertSearchHitsToMetricDocs(searchResults.getSearchHits());
        log.info("Retrieved {} documents from ES for metric: {}", metricDocs.size(), metricKey);

        return metricDocs;
    }

    @Override
    public List<MetricDoc> getDailyMetricDimensionSeries(String metricKey, LocalDate endDt, int size) {
        Preconditions.checkNotNull(metricKey);
        String indexNames = "tdq.batch.profiling.metric.prod.*";
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexNames);

        // FIXME: handle errors: metrics data missing
        LocalDate startDt = endDt.minusDays(size);
        Criteria criteria = new Criteria("metric_key").is(metricKey)
                                                      .and("dimension").exists()
                                                      .and("dt").greaterThan(startDt.toString())
                                                      .and("dt").lessThanEqual(endDt.toString());

        Sort sort = Sort.by("dt").ascending();
        Query query = new CriteriaQuery(criteria).addSort(sort);
        SearchHits<MetricDoc> searchResults = esOperations.search(query, MetricDoc.class, indexCoordinates);
        List<MetricDoc> metricDocs = convertSearchHitsToMetricDocs(searchResults.getSearchHits());
        log.info("Retrieved {} documents from ES for metric: {}", metricDocs.size(), metricKey);

        return metricDocs;
    }

    @Override
    public Boolean dailyMetricExists(String metricKey, LocalDate dt) {
        Preconditions.checkNotNull(metricKey);
        Preconditions.checkNotNull(dt);

        String indexName = "tdq.batch.profiling.metric.prod." + dt;
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);

        Criteria criteria = new Criteria("metric_key").is(metricKey);
        Query query = new CriteriaQuery(criteria).setPageable(Pageable.ofSize(1));

        SearchHits<MetricDoc> search = esOperations.search(query, MetricDoc.class, indexCoordinates);
        return search.getTotalHits() > 0;
    }

    @Override
    public List<PageMetricDoc> getTop50PageMetricDoc(List<Integer> pageIds, LocalDate dt, Integer hr) {

        IndexCoordinates indexCoordinates = IndexCoordinates.of("prod.metric.rt.page");

        List<String> dates = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            LocalDate d = dt.minusWeeks(i);
            dates.add(d.toString());
        }

        Criteria criteria = new Criteria("metric_key").is("hourly_event_cnt")
                .and("dt").in(dates)
                .and("hr").is(hr)
                .and("page_id").in(pageIds);

        Query query = new CriteriaQuery(criteria);

        SearchHits<PageMetricDoc> search = esOperations.search(query, PageMetricDoc.class, indexCoordinates);

        List<PageMetricDoc> results = new ArrayList<>();

        for (SearchHit<PageMetricDoc> searchHit : search.getSearchHits()) {
            PageMetricDoc content = searchHit.getContent();
            results.add(content);
        }

        return results;
    }

    @Override
    public void cleanUpTop50PageMetricDoc() {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("prod.metric.rt.page");
        Criteria criteria = new Criteria("metric_key").is("hourly_event_cnt");
        Query query = new CriteriaQuery(criteria).addSort(Sort.by("dt").descending()).setPageable(Pageable.ofSize(1));
        SearchHits<PageMetricDoc> searchHits = esOperations.search(query, PageMetricDoc.class, indexCoordinates);
        if (searchHits.isEmpty())
            return;

        PageMetricDoc pageMetricDoc = searchHits.getSearchHits().get(0).getContent();
        String dt = pageMetricDoc.getDt().toString();
        LocalDate localDate = LocalDate.parse(dt);
        String deadline = localDate.minusDays(90).toString();

        criteria = new Criteria("metric_key").is("hourly_event_cnt").and("dt").lessThan(deadline);

        query = new CriteriaQuery(criteria);
        ByQueryResponse byQueryResponse = esOperations.delete(query, PageMetricDoc.class, indexCoordinates);
        List<ByQueryResponse.Failure> failures = byQueryResponse.getFailures();
        if (!failures.isEmpty()){
            failures.forEach(failure -> log.error(" _id: {}, error: {}", "",  failure.getId(), failure.getCause()));
        } else {
            log.info("deleted {} docs at this time", byQueryResponse.getDeleted());
        }
    }

    private List<MetricDoc> convertSearchHitsToMetricDocs(List<SearchHit<MetricDoc>> searchHits) {
        List<MetricDoc> metricDocs = new ArrayList<>();
        for (SearchHit<MetricDoc> searchHit : searchHits) {
            MetricDoc content = searchHit.getContent();
            metricDocs.add(content);
        }
        return metricDocs;
    }
}
