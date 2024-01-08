package com.ebay.dap.epic.tdq.schedule.task;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component("documentsCleaningUpTask")
@Lazy(false)
@Slf4j
public class DocumentsCleaningUpTask {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private void cleanUpTop50PageMetricDoc() {
        Instant start = Instant.now();
        String index = "prod.metric.rt.page";
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            TermQueryBuilder queryBuilder = QueryBuilders.termQuery("metric_key", "hourly_event_cnt");
            boolQueryBuilder.filter(queryBuilder);
            searchSourceBuilder.query(boolQueryBuilder);
            searchSourceBuilder.sort("dt", SortOrder.DESC);
            searchSourceBuilder.size(1);
            SearchRequest searchRequest = new SearchRequest(List.of(index).toArray(new String[0]), searchSourceBuilder);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            if (searchHits.length == 0)
                return;

            SearchHit documentFields = searchHits[0];
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            String dt = sourceAsMap.get("dt").toString();
            LocalDate localDate = LocalDate.parse(dt);
            String deadline = localDate.minusDays(90).toString();

            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest();
            deleteByQueryRequest.indices(index);
            deleteByQueryRequest.setConflicts("proceed");
            deleteByQueryRequest.setSlices(7);
            deleteByQueryRequest.setBatchSize(10_000);
            deleteByQueryRequest.setTimeout(TimeValue.timeValueMinutes(3));

            boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(queryBuilder);
            boolQueryBuilder.must(QueryBuilders.rangeQuery("dt").lte(deadline));
            deleteByQueryRequest.setQuery(boolQueryBuilder);

            BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
            List<BulkItemResponse.Failure> bulkFailures = bulkByScrollResponse.getBulkFailures();
            if (bulkFailures.isEmpty()){
                log.info("deleted {} docs at this time", bulkByScrollResponse.getDeleted());
            } else {
                bulkFailures.forEach(failure -> log.error(" _id: {}, error: {}", "",  failure.getId(), failure.getCause()));
            }
            log.info("cost time:{}", Duration.between(start, Instant.now()).toMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run at 00:00 AM MST every sunday
     */
    @Scheduled(cron = "0 0 0 ? * 0", zone = "GMT-7")
    @SchedulerLock(name = "DocumentsCleaningUpTask", lockAtLeastFor = "PT5M", lockAtMostFor = "PT20M")
    public void run(){
        cleanUpTop50PageMetricDoc();
    }

}
