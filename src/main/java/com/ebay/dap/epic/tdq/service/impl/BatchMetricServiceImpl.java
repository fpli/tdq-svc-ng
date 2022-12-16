package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.config.ProntoConfig;
import com.ebay.dap.epic.tdq.data.entity.MetricInfoEntity;
import com.ebay.dap.epic.tdq.data.vo.MetricChartVO;
import com.ebay.dap.epic.tdq.data.vo.MetricQueryParamVO;
import com.ebay.dap.epic.tdq.data.vo.MetricValueItemVO;
import com.ebay.dap.epic.tdq.service.BatchMetricService;
import com.ebay.dap.epic.tdq.service.MetricInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class BatchMetricServiceImpl implements BatchMetricService {

    private RestHighLevelClient restHighLevelClient;

    // todo: will be removed, only test local test
    public void setRestHighLevelClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    //private ElasticsearchAsyncClient elasticsearchAsyncClient;

    private static final String indexTemplate = "tdq.batch.profiling.metric.%s.%s";

    private static final String pattern = "yyyy-MM-dd";
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);

    @Autowired
    private ProntoConfig prontoEnv;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MetricInfoService metricInfoService;

    private LocalDate date;

    @Scheduled(cron = "0 0 11 * * *")
    @PostConstruct
    private void init(){
        if (LocalDateTime.now().getHour() < 10) {
            date = LocalDate.now().minusDays(2);
        } else {
            date = LocalDate.now().minusDays(1);
        }
    }

    @PostConstruct
    public void buildRestHighLevelClient(){
        RestClientBuilder builder = RestClient.builder(new HttpHost(this.prontoEnv.getHostname(), this.prontoEnv.getPort(), this.prontoEnv.getScheme()));
        if (StringUtils.isNotBlank(this.prontoEnv.getHostname())) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.prontoEnv.getApiKey(), this.prontoEnv.getApiValue()));
            builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }
        //RestClientTransport restClientTransport = new RestClientTransport(builder.build(), new JacksonJsonpMapper());
        //ElasticsearchClient elasticsearchClient = new ElasticsearchClient(restClientTransport);
        //elasticsearchAsyncClient = new ElasticsearchAsyncClient(restClientTransport);
        restHighLevelClient = new RestHighLevelClient(builder);
        log.info("restHighLevelClient is initialized.");
    }


    /**
     * only support daily batch metric but real time metric,
     * don't contain mmd info
     * @param metricQueryParamVO
     * @return
     */
    @Override
    public MetricChartVO retrieveBatchMetric(MetricQueryParamVO metricQueryParamVO) {
        String metricKey = metricQueryParamVO.getMetricKey();
        LocalDate to = metricQueryParamVO.getDate();
        String date = to.format(dateTimeFormatter);

        MetricChartVO metricChartVO = new MetricChartVO();
        metricChartVO.setDate(metricKey);
        metricChartVO.setDate(date);
        List<MetricValueItemVO> metricValueItemVOList = new ArrayList<>();
        metricChartVO.setMetricValueItemVOList(metricValueItemVOList);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder rootBuilder = QueryBuilders.boolQuery();
        rootBuilder.must(QueryBuilders.termQuery("metric_key", metricKey));
        rootBuilder.must(QueryBuilders.termQuery("dt", date));
        Map<String, Set<String>> dimensions = metricQueryParamVO.getDimensions();
        if (MapUtils.isNotEmpty(dimensions)) {
            for (Map.Entry<String, Set<String>> entry : dimensions.entrySet()) {
                rootBuilder.must(QueryBuilders.termsQuery("dimension." + entry.getKey() + ".raw", entry.getValue()));
            }
        }
        builder.query(rootBuilder);
        builder.size(0);
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("agg").field("dt").calendarInterval(DateHistogramInterval.DAY).format(pattern);
        SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("metricValue").field("value");
        dateHistogramAggregationBuilder.subAggregation(sumAggregationBuilder);
        builder.aggregation(dateHistogramAggregationBuilder);
        LocalDate from = to.minusMonths(1).plusDays(1);
        log.info("builder: {}", builder);
        SearchRequest searchRequest = new SearchRequest(calculateIndexes(from, to), builder);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = searchResponse.getAggregations();
            if (null != aggregations){
                Histogram agg = aggregations.get("agg");
                MetricValueItemVO metricValueItemVO;
                for (Histogram.Bucket bucket : agg.getBuckets()) {
                    String dt = bucket.getKeyAsString();
                    Sum metricValue = bucket.getAggregations().get("metricValue");
                    metricValueItemVO = new MetricValueItemVO();
                    metricChartVO.getMetricValueItemVOList().add(metricValueItemVO);
                    metricValueItemVO.setValue(metricValue.getValue());
                    metricValueItemVO.setTimestamp(LocalDate.parse(dt, dateTimeFormatter).atTime(0, 0).toEpochSecond(ZoneOffset.UTC));
                }
            }
        } catch (IOException e) {
            log.error("occurred errors during search index", e);
            throw new RuntimeException(e);
        }
        return metricChartVO;
    }

    @Override
    public String retrieveDimensionsByMetricKey(String metricKey, LocalDate date) throws Exception {
        ObjectNode dimension = objectMapper.createObjectNode();
        MetricInfoEntity metricInfoEntity = metricInfoService.getMetricInfoEntityByMetricKey(metricKey);
        if (null == metricInfoEntity || null == metricInfoEntity.getDimension()){
            return dimension.toPrettyString();
        }
        String entityDimension = metricInfoEntity.getDimension();
        String[] ds = entityDimension.replace("[", "").replace("]", "").split(",");
        if (null == date){
            date = this.date;
        }
        LocalDate begin = date.minusMonths(1);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder rootBuilder = QueryBuilders.boolQuery();
        rootBuilder.must(QueryBuilders.termQuery("metric_key", metricKey));
        rootBuilder.must(QueryBuilders.rangeQuery("dt").gte(dateTimeFormatter.format(begin)).lte(dateTimeFormatter.format(date)));
        builder.query(rootBuilder);
        builder.size(0);
        for (String d : ds) {
            TermsAggregationBuilder aggregation = AggregationBuilders.terms(d).field("dimension." + d + ".raw").size(20000).order(BucketOrder.key(true));
            builder.aggregation(aggregation);
        }
        SearchRequest searchRequest = new SearchRequest(calculateIndexes(begin, date), builder);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        log.info("searchRequest: {}", searchRequest);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        for (String d : ds) {
            ParsedStringTerms agg = searchResponse.getAggregations().get(d);
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (Terms.Bucket bucket : agg.getBuckets()) {
                String tagValue = bucket.getKeyAsString();
                arrayNode.add(tagValue);
            }
            dimension.set(d, arrayNode);
        }
        return dimension.toPrettyString();
    }

    private String[] calculateIndexes(LocalDate from, LocalDate to) {
        Set<String> results = new HashSet<>();
        LocalDate date = from;
        while (!date.isAfter(to)) {
            String dt = dateTimeFormatter.format(date);
            String index = String.format(indexTemplate, "prod", dt);
            results.add(index);
            date = date.plusDays(1);
        }
        log.info("search request indexes=>{}", StringUtils.join(results, ","));
        return results.toArray(new String[0]);
    }

}
