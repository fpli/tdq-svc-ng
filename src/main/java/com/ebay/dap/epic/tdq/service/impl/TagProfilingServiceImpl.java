package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.data.dto.TagDetailDTO;
import com.ebay.dap.epic.tdq.data.entity.*;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.*;
import com.ebay.dap.epic.tdq.data.vo.*;
import com.ebay.dap.epic.tdq.service.TagProfilingService;
import com.ebay.dap.epic.tdq.service.mmd.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TagProfilingServiceImpl implements TagProfilingService {

    public static final String TDQ_PROFILING_METRIC_S_S = "tdq.profiling.metric.%s.%s";

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static final List<String> TAGS = Arrays.asList("page_id", "bot", "app", "event_family");

    private static final ConcurrentHashMap<String, List<TagCardItemVo>> concurrentHashMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<TagRecordInfo>> cache = new ConcurrentHashMap<>();

    @Autowired
    private TagLookUpInfoMapper tagLookUpInfoRepo;

    @Autowired
    private TagUsageInfoMapper tagUsageInfoRepo;
    @Autowired
    private MMDService mmdService;

    @Autowired
    private MMDRecordInfoMapper mmdRecordInfoRepo;

    @Autowired
    private AnomalyItemMapper anomalyItemRepository;

    @Autowired
    private TagRecordMapper tagRecordRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private int tagTopN = 100;
    private int tagByVolumeTopN = 3000;
    public static double score = 0.9995;

    @Scheduled(cron = "0 0 11 * * *", zone = "GMT-7")
    @PostConstruct
    public void init() {
        CompletableFuture.runAsync(() -> {
            Instant begin = Instant.now();
            concurrentHashMap.clear();
            cache.clear();
            final LocalDate date;
            if (LocalDateTime.now().getHour() < 10) {
                date = LocalDate.now().minusDays(2);
            } else {
                date = LocalDate.now().minusDays(1);
            }
            try {
                MandatoryTag.TOPN_TAG_USAGE.clear();
                MandatoryTag.TOPN_TAG_USAGE.addAll(tagUsageInfoRepo.listTagNames(tagTopN, date));
                List<TagRecordInfo> tagRecordInfoList = getTagTable(null, date);
                int c = Math.min(tagByVolumeTopN, tagRecordInfoList.size());
                for (int i = 0; i < c; i++) {
                    MandatoryTag.TOPN_TAG_USAGE.add(tagRecordInfoList.get(i).getTagName());
                }
                log.info("MandatoryTag.TOPN_TAG_USAGE in init:" + MandatoryTag.TOPN_TAG_USAGE.size());
                getTagCardItemVo(date, 6000);
            } catch (Exception e) {
                log.error("init: {}", e.getMessage());
            }
            log.info("init duration: {}", Duration.between(begin, Instant.now()).getSeconds());
        });
    }

    @Override
    public void refresh() {
        init();
    }

    public List<Map.Entry<String, Integer>> getMapping() {
        try {
            Map<String, Integer> dailyTag = new HashMap<>();
            LocalDate dt = LocalDate.now();
            GetIndexRequest getIndexRequest = new GetIndexRequest(calculateIndexes(dt.minusMonths(1), dt));
            getIndexRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            GetIndexResponse getIndexResponse = restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
            Map<String, MappingMetadata> mappings = getIndexResponse.getMappings();
            mappings.forEach((k, v) -> {
                System.out.println(k);
                Map<String, Object> sourceAsMap = v.getSourceAsMap();
                Object v1 = sourceAsMap.get("properties");
                LinkedHashMap o = (LinkedHashMap) ((LinkedHashMap<?, ?>) (((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) v1).get("expr")).get("properties")).get("tag_size_attr")).get("properties")).get("tagMap"))).get("properties");
                dailyTag.put(k.split("\\.")[4], o.size());
            });

            return dailyTag.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Set<String> getActiveTagNames(LocalDate dt) throws IOException {
        String[] indexes = calculateIndexes(dt, dt);
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexes);
        getIndexRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        GetIndexResponse getIndexResponse = restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
        Map<String, MappingMetadata> mappings = getIndexResponse.getMappings();
        if (indexes.length > 0) {
            MappingMetadata mappingMetaData = mappings.get(indexes[0]);
            Map<String, Object> sourceAsMap = mappingMetaData.getSourceAsMap();
            Object v1 = sourceAsMap.get("properties");
            LinkedHashMap<String, ?> o = (LinkedHashMap) ((LinkedHashMap<?, ?>) (((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) v1).get("expr")).get("properties")).get("tag_size_attr")).get("properties")).get("tagMap"))).get("properties");
            return o.keySet();
        }
        return null;
    }

    @Override
    public Map<String, Double> getDailyTagsSize(List<String> tags, LocalDate date) {
        try {
            Map<String, Double> map = new HashMap<>();
            double dailyTagSize = getDailyTagSize(tags, date);
            map.put("tags", dailyTagSize);
            return map;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TagCardItemVo> getTagCardItemVo(LocalDate date, int n) throws InterruptedException, ExecutionException {
        String key = date.format(dateTimeFormatter) + "--" + n;
        if (concurrentHashMap.containsKey(key)) {
            return concurrentHashMap.get(key);
        }
        List<TagCardItemVo> tagCardItemVoList = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> tagCardItemVoList.add(getTagUsageCard(date))),
                CompletableFuture.runAsync(() -> tagCardItemVoList.addAll(getTagCountCard(date))),
                CompletableFuture.runAsync(() -> tagCardItemVoList.addAll(getInconsistentFormatTagsCountCard(date, n)))
                //CompletableFuture.runAsync(() -> tagCardItemVoList.add(getInaccurateTagRateCard(date))),
                //CompletableFuture.runAsync(() -> tagCardItemVoList.add(getFluctuantPopulationRateTagsCountCard(date)))
        ).get();
        tagCardItemVoList.sort(Comparator.comparingInt(TagCardItemVo::getOrder));
        concurrentHashMap.put(key, tagCardItemVoList);
        return tagCardItemVoList;
    }

    @Override
    public List<TagRecordInfo> getTagTable(Set<String> tagNameList, LocalDate date) {
        List<TagRecordInfo> tagRecordInfoList = null;
        try {
            Set<String> tagNames = tagNameList;
            if (CollectionUtils.isEmpty(tagNames)) {
                tagNames = getActiveTagNames(date);
                if (null == tagNames) {
                    tagNames = tagUsageInfoRepo.findAllByDtIn(Collections.singletonList(date)).stream().map(TagUsageInfoEntity::getTagName).collect(Collectors.toSet());
                }
            }
            ArrayList<String> arrayList = new ArrayList<>(tagNames);
            Collections.sort(arrayList);
            String key = date.format(dateTimeFormatter) + "-" + String.join(",", arrayList);
            if (!cache.isEmpty() && cache.containsKey(key)) {
                log.info("getTagTable from cache");
                return cache.get(key);
            }
            tagRecordInfoList = tagNames.parallelStream().map(tagName -> {
                try {
                    return getTagRecordInfo(tagName, date);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("error:", e);
                    return null;
                }
            }).filter(Objects::nonNull).sorted(Comparator.comparingDouble(TagRecordInfo::getTagVolume).reversed()).collect(Collectors.toList());
            List<TagRecordInfo> recordInfoList = tagRecordInfoList;
            CompletableFuture.runAsync(() -> {
                tagRecordRepo.deleteAllByDate(date);
                log.info("size:" + recordInfoList.size());
                recordInfoList.parallelStream().forEach(tagRecordInfo -> {
                    try {
                        TagRecord tagRecord = new TagRecord();
                        BeanUtils.copyProperties(tagRecordInfo, tagRecord);
                        tagRecord.setDt(date);
                        tagRecordRepo.save(tagRecord);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                    }
                });
            });
            cache.put(key, tagRecordInfoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tagRecordInfoList;
    }

    @Override
    public void configAbnormalTag(double TH_ANOMALY_SCORE, int topN, int tagByVolumeTopN) {
        this.tagTopN = topN;
        score = TH_ANOMALY_SCORE;
        this.tagByVolumeTopN = tagByVolumeTopN;
    }


    private TagRecordInfo getTagRecordInfo(String tagName, LocalDate date) throws Exception {
        log.info("1. enter getTagRecordInfo ");
        List<TagLookUpInfo> allByTagName = tagLookUpInfoRepo.findAllByTagName(tagName);
        TagRecordInfo tagRecordInfo = new TagRecordInfo();
        if (CollectionUtils.isEmpty(allByTagName)) {
            tagRecordInfo.setTagName(tagName);
        } else BeanUtils.copyProperties(allByTagName.get(0), tagRecordInfo);
        try {
            log.info("2. getTagRecordInfo -> checkTag");
            List<TagDetailVO> tagDetailVOList = checkTag(tagName, date, date);
            if (!CollectionUtils.isEmpty(tagDetailVOList)) {
                TagDetailVO tagDetailVO = tagDetailVOList.get(0);
                tagRecordInfo.setTagVolume(tagDetailVO.getTagCount());
                tagRecordInfo.setEventVolume(tagDetailVO.getEventCount());
                tagRecordInfo.setCoverage(tagDetailVO.getCompletenessPercent());
            }
            log.info("3. getTagRecordInfo -> getDailyTagSize");
            double dailyTagSize = getDailyTagSize(tagName, date);
            tagRecordInfo.setTagSize(dailyTagSize);
            List<UsageOfDayVO> usageOfDayVOList = getTagUsage(tagName, date, date);
            if (!CollectionUtils.isEmpty(usageOfDayVOList)) {
                UsageOfDayVO usageOfDayVO = usageOfDayVOList.get(0);
                tagRecordInfo.setAccessTotal(usageOfDayVO.getBatch() + usageOfDayVO.getIndividual());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return tagRecordInfo;
    }

    private TagCardItemVo getTagUsageCard(LocalDate date) {
        TagCardItemVo tagCardItemVo = new TagCardItemVo();
        tagCardItemVo.setTitle("Total Tag Usage");
        tagCardItemVo.setOrder(3);
        List<LocalDate> dts = new ArrayList<>();
        dts.add(date);
        dts.add(date.minusDays(7));
        List<TagUsageInfoEntity> tagUsageInfoList = tagUsageInfoRepo.findAllByDtIn(dts);
        if (!CollectionUtils.isEmpty(tagUsageInfoList)) {
            Map<Boolean, Long> booleanLongMap = tagUsageInfoList.stream().collect(Collectors.partitioningBy(tagUsageInfo -> tagUsageInfo.getDt().equals(date), Collectors.reducing(0L, TagUsageInfoEntity::getAccessCount, Long::sum)));
            Long aLong = booleanLongMap.get(true);
            tagCardItemVo.setAmount(aLong);
            Long aLong1 = booleanLongMap.get(false);
            tagCardItemVo.setIncrementType(Long.compare(aLong, aLong1));
            if (aLong1 == 0L) {
                if (0L == aLong) {
                    tagCardItemVo.setIncrement(0.0);
                } else {
                    tagCardItemVo.setIncrement(100.0);
                }
            } else {
                tagCardItemVo.setIncrement(getIncrement(aLong, aLong1));
            }
        }
        return tagCardItemVo;
    }

    private double getIncrement(Long aLong, Long aLong1) {
        return new BigDecimal(Math.abs(aLong - aLong1)).multiply(BigDecimal.valueOf(100.0)).divide(BigDecimal.valueOf(aLong1), 2, RoundingMode.DOWN).doubleValue();
    }

    private List<TagCardItemVo> getTagCountCard(LocalDate date) {
        List<String> tagNames = tagLookUpInfoRepo.findAllTagNames();

        TagCardItemVo activeTagCardItemVo = new TagCardItemVo();
        activeTagCardItemVo.setTitle("Total Active Tag Count");
        activeTagCardItemVo.setOrder(4);

        TagCardItemVo inactiveTagCardItemVo = new TagCardItemVo();
        inactiveTagCardItemVo.setTitle("Inactive Tag Count");
        inactiveTagCardItemVo.setOrder(2);

        try {
            Set<String> activeTagNames = getActiveTagNames(date);
            Set<String> activeTagNames7DaysAgo = getActiveTagNames(date.minusDays(7));
            if (activeTagNames == null) {
                activeTagCardItemVo.setAmount(0);
                inactiveTagCardItemVo.setAmount(0);
            } else {
                int amount = activeTagNames.size();
                activeTagCardItemVo.setAmount(amount);

                int amount1 = tagNames.size() - amount;
                inactiveTagCardItemVo.setAmount(amount1);

                if (null == activeTagNames7DaysAgo) {
                    activeTagCardItemVo.setIncrementType(1);
                    activeTagCardItemVo.setIncrement(100);

                    inactiveTagCardItemVo.setIncrementType(-1);
                    inactiveTagCardItemVo.setIncrement(0);
                } else {
                    int size = activeTagNames7DaysAgo.size();
                    activeTagCardItemVo.setIncrementType(Integer.compare(amount, size));
                    activeTagCardItemVo.setIncrement(getIncrement((long) amount, (long) size));

                    inactiveTagCardItemVo.setIncrementType(Integer.compare(amount1, tagNames.size() - size));
                    inactiveTagCardItemVo.setIncrement(getIncrement((long) amount1, (long) (tagNames.size() - size)));
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return Lists.newArrayList(activeTagCardItemVo, inactiveTagCardItemVo);
    }

    private List<TagCardItemVo> getInconsistentFormatTagsCountCard(LocalDate date, int n) {
        List<TagCardItemVo> tagCardItemVoList = new ArrayList<>();
        try {
            Set<String> activeTagNames = getActiveTagNames(date);

            LocalDate date1 = date.minusDays(7);

            if (!CollectionUtils.isEmpty(activeTagNames)) {
                Map<String, List<TagDetailVO>> listMap = activeTagNames.parallelStream().collect(Collectors.toMap(UnaryOperator.identity(), tagName -> {
                    try {
                        return checkTag(tagName, date.minusDays(90), date);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }));

                Map<String, List<Series>> mmdTimeSeries0 = new HashMap<>();
                Map<String, List<Series>> mmdTimeSeries1 = new HashMap<>();
//                Map<String, List<Series>> mmdTimeSeries2 = new HashMap<>();
                log.info("MandatoryTag.TOPN_TAG_USAGE:" + MandatoryTag.TOPN_TAG_USAGE.size());
                List<Long> list = new ArrayList<>();
                listMap.forEach((tagName, tagDetailVOList) -> {
                    if (MandatoryTag.TOPN_TAG_USAGE.contains(tagName)) {
                        mmdTimeSeries0.compute(tagName, (tag, seriesList) -> {
                            if (null == seriesList) {
                                seriesList = new ArrayList<>();
                            }
                            Series series;
                            for (TagDetailVO tagDetailVO : tagDetailVOList) {
                                series = new Series();
                                seriesList.add(series);
                                series.setTimestamp(tagDetailVO.getDt());
                                series.setValue(tagDetailVO.getCompletenessPercent());
                            }
                            return seriesList;
                        });
                    }
                    mmdTimeSeries1.compute(tagName, (tag, seriesList) -> {
                        if (null == seriesList) {
                            seriesList = new ArrayList<>();
                        }
                        Series series;
                        for (TagDetailVO tagDetailVO : tagDetailVOList) {
                            series = new Series();
                            seriesList.add(series);
                            series.setTimestamp(tagDetailVO.getDt());
                            series.setValue(tagDetailVO.getTagFormatConsistentPercent());
                        }
                        return seriesList;
                    });
//                    mmdTimeSeries2.compute(tagName, (tag, seriesList) -> {
//                        if (null == seriesList) {
//                            seriesList = new ArrayList<>();
//                        }
//                        Series series;
//                        for (TagDetailVO tagDetailVO : tagDetailVOList) {
//                            series = new Series();
//                            seriesList.add(series);
//                            series.setTimestamp(tagDetailVO.getDt());
//                            series.setValue(tagDetailVO.getTagAccuratePercent());
//                        }
//                        return seriesList;
//                    });
                    String format = dateTimeFormatter.format(date);
                    Optional<TagDetailVO> tagDetailVOOptional = tagDetailVOList.stream().filter(tagDetailVO -> format.equals(tagDetailVO.getDt())).findFirst();
                    tagDetailVOOptional.ifPresent(tagDetailVO -> list.add((long) tagDetailVO.getTagAccuratePercent()));
                });


                MMDResult mmdResult = mmdService.mmdCallInBatch(mmdTimeSeries0, n, null);
                if (mmdResult.getCode() != 200) {
                    log.error("mmd:{}", mmdResult);
                }
                List<JobResult> jobResultList = mmdResult.getJobs();
                TagCardItemVo tagCardItemVo = new TagCardItemVo();
                long countAnomalyTag = countAnomalyTag(jobResultList, Optional.of(tagCardItemVo.getTagNames()));
                if (countAnomalyTag > 0) {
                    Map<String, List<JobParam>> req = new HashMap<>();
                    List<JobParam> jobs = new ArrayList<>();
                    tagCardItemVo.getTagNames().forEach(tagName -> {
                        List<Series> series = mmdTimeSeries0.get(tagName);
                        JobParam mmdJob = new JobParam();
                        mmdJob.setId(tagName);
                        mmdJob.setLabel(tagName);
                        mmdJob.setSeries(series);
                        jobs.add(mmdJob);
                    });
                    req.put("jobs", jobs);
                    MMDRecordInfo mmdRecordInfo = new MMDRecordInfo();
                    mmdRecordInfo.setPayload(objectMapper.writeValueAsString(req));
                    mmdRecordInfo.setAnomalyType(-1);
                    mmdRecordInfo.setTimeInterval(-1);
                    mmdRecordInfo.setUid("*********");
                    mmdRecordInfo.setResponse("{}");
                    mmdRecordInfoRepo.save(mmdRecordInfo);
                }
                tagCardItemVo.setTitle("Fluctuant Population Rate Tags Count");
                tagCardItemVo.setOrder(0);
                tagCardItemVo.setAmount(countAnomalyTag);
                tagCardItemVoList.add(tagCardItemVo);

                mmdResult = mmdService.mmdCallInBatch(mmdTimeSeries1, n, null);
                if (mmdResult.getCode() != 200) {
                    log.error("mmd:{}", mmdResult);
                }
                jobResultList = mmdResult.getJobs();
                tagCardItemVo = new TagCardItemVo();
                long countAnomalyTag1 = countAnomalyTag(jobResultList, Optional.of(tagCardItemVo.getTagNames()));
                tagCardItemVo.setTitle("Inconsistent Format Tags Count");
                tagCardItemVo.setOrder(1);
                tagCardItemVo.setAmount(countAnomalyTag1);
                tagCardItemVoList.add(tagCardItemVo);

//                mmdResult = mmdService.mmdCallInBatch(mmdTimeSeries2, n);
//                if (mmdResult.getCode() != 200) {
//                    log.error("mmd:{}", mmdResult);
//                }
                //jobResultList = mmdResult.getJobs();
                //long countAnomalyTag2 = countAnomalyTag(jobResultList, Optional.of(tagCardItemVo.getTagNames()));
                tagCardItemVo = new TagCardItemVo();
                tagCardItemVo.setTitle("Inaccurate Tag Count");
                tagCardItemVo.setOrder(5);
                tagCardItemVo.setAmount(list.stream().reduce(0L, Long::sum));
                //tagCardItemVo.setRate(getPercent(countAnomalyTag2, activeTagNames.size()));
                tagCardItemVoList.add(tagCardItemVo);

                listMap = activeTagNames.parallelStream().collect(Collectors.toMap(UnaryOperator.identity(), tagName -> {
                    try {
                        return checkTag(tagName, date1.minusDays(90), date1);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }));

                mmdTimeSeries0.clear();
                mmdTimeSeries1.clear();
                //mmdTimeSeries2.clear();
                list.clear();
                listMap.forEach((tagName, tagDetailVOList) -> {
                    if (MandatoryTag.TOPN_TAG_USAGE.contains(tagName)) {
                        mmdTimeSeries0.compute(tagName, (tag, seriesList) -> {
                            if (null == seriesList) {
                                seriesList = new ArrayList<>();
                            }
                            Series series;
                            for (TagDetailVO tagDetailVO : tagDetailVOList) {
                                series = new Series();
                                seriesList.add(series);
                                series.setTimestamp(tagDetailVO.getDt());
                                series.setValue(tagDetailVO.getCompletenessPercent());
                            }
                            return seriesList;
                        });
                    }
                    mmdTimeSeries1.compute(tagName, (tag, seriesList) -> {
                        if (null == seriesList) {
                            seriesList = new ArrayList<>();
                        }
                        Series series;
                        for (TagDetailVO tagDetailVO : tagDetailVOList) {
                            series = new Series();
                            seriesList.add(series);
                            series.setTimestamp(tagDetailVO.getDt());
                            series.setValue(tagDetailVO.getTagFormatConsistentPercent());
                        }
                        return seriesList;
                    });
//                    mmdTimeSeries2.compute(tagName, (tag, seriesList) -> {
//                        if (null == seriesList) {
//                            seriesList = new ArrayList<>();
//                        }
//                        Series series;
//                        for (TagDetailVO tagDetailVO : tagDetailVOList) {
//                            series = new Series();
//                            seriesList.add(series);
//                            series.setTimestamp(tagDetailVO.getDt());
//                            series.setValue(tagDetailVO.getTagAccuratePercent());
//                        }
//                        return seriesList;
//                    });
                    String format = dateTimeFormatter.format(date1);
                    Optional<TagDetailVO> tagDetailVOOptional = tagDetailVOList.stream().filter(tagDetailVO -> format.equals(tagDetailVO.getDt())).findFirst();
                    tagDetailVOOptional.ifPresent(tagDetailVO -> list.add((long) tagDetailVO.getTagAccuratePercent()));
                });

                mmdResult = mmdService.mmdCallInBatch(mmdTimeSeries0, n, null);
                if (mmdResult.getCode() != 200) {
                    log.error("mmd:{}", mmdResult);
                }
                jobResultList = mmdResult.getJobs();
                countAnomalyTag = countAnomalyTag(jobResultList, Optional.empty());
                tagCardItemVo = tagCardItemVoList.get(0);
                tagCardItemVo.setIncrementType(Long.compare(tagCardItemVo.getAmount(), countAnomalyTag));
                if (countAnomalyTag == 0) {
                    tagCardItemVo.setIncrement(0);
                } else {
                    tagCardItemVo.setIncrement(getPercent(tagCardItemVo.getAmount() - countAnomalyTag, countAnomalyTag));
                }

                mmdResult = mmdService.mmdCallInBatch(mmdTimeSeries1, n, null);
                if (mmdResult.getCode() != 200) {
                    log.error("mmd:{}", mmdResult);
                }
                jobResultList = mmdResult.getJobs();
                countAnomalyTag1 = countAnomalyTag(jobResultList, Optional.empty());
                tagCardItemVo = tagCardItemVoList.get(1);
                tagCardItemVo.setIncrementType(Long.compare(tagCardItemVo.getAmount(), countAnomalyTag1));
                if (countAnomalyTag1 == 0) {
                    tagCardItemVo.setIncrement(0);
                } else {
                    tagCardItemVo.setIncrement(getPercent(tagCardItemVo.getAmount() - countAnomalyTag1, countAnomalyTag1));
                }

//                mmdResult = mmdService.mmdCallInBatch(mmdTimeSeries2, n);
//                if (mmdResult.getCode() != 200) {
//                    log.error("mmd:{}", mmdResult);
//                }
//                jobResultList = mmdResult.getJobs();
                //countAnomalyTag2 = countAnomalyTag(jobResultList, Optional.empty());

                tagCardItemVo = tagCardItemVoList.get(2);
                //double percent = getPercent(countAnomalyTag2, activeTagNames.size());
                Long countAnomalyTag2 = list.stream().reduce(0L, Long::sum);
                tagCardItemVo.setIncrementType(Long.compare(tagCardItemVo.getAmount(), countAnomalyTag2));
                if (countAnomalyTag2 == 0) {
                    tagCardItemVo.setIncrement(0);
                } else {
                    tagCardItemVo.setIncrement(getPercent(tagCardItemVo.getAmount() - countAnomalyTag2, countAnomalyTag2));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        return tagCardItemVoList;
    }

    private long countAnomalyTag(List<JobResult> jobResultList, Optional<Set<String>> tagNamesOptional) {
        if (CollectionUtils.isEmpty(jobResultList)) return 0;
        final LocalDate date;
        if (LocalDateTime.now().getHour() < 10) {
            date = LocalDate.now().minusDays(2);
        } else {
            date = LocalDate.now().minusDays(1);
        }
        return jobResultList.stream().filter(jobResult -> {
            List<MMDAlert> alerts = jobResult.getAlerts();
            if (!CollectionUtils.isEmpty(alerts)) {
                MMDAlert mmdAlert = alerts.get(0);
                Boolean isAnomaly = mmdAlert.getIsAnomaly();
                if (isAnomaly) {
                    String tag = jobResult.getLabel();
                    tagNamesOptional.ifPresent(tagNames -> tagNames.add(tag));
                    String dtStr = mmdAlert.getDtStr();
                    LocalDate localDate = LocalDate.parse(dtStr, dateTimeFormatter);
                    if (localDate.equals(date) && tagNamesOptional.isPresent()) {
                        anomalyItemRepository.deleteInBatch("tag", localDate, tag);
                        AnomalyItemEntity anomalyItemEntity = new AnomalyItemEntity();
                        anomalyItemEntity.setType("tag");
                        anomalyItemEntity.setRefId(tag);
                        anomalyItemEntity.setDt(localDate);
                        anomalyItemEntity.setRateValue(mmdAlert.getRawValue().doubleValue());
                        anomalyItemEntity.setLowerBound(mmdAlert.getLBound().doubleValue());
                        anomalyItemEntity.setUpperBound(mmdAlert.getUBound().doubleValue());
                        anomalyItemRepository.save(anomalyItemEntity);
                    }
                }
                return isAnomaly;
            }
            return false;
        }).count();
    }

    private List<TagDetailVO> checkTag(String tagName, LocalDate begin, LocalDate date) throws Exception {
        TagDimensionQueryVO tagDimensionQueryVO = new TagDimensionQueryVO();
        tagDimensionQueryVO.setTagName(tagName);
        tagDimensionQueryVO.setEndDt(date);
        tagDimensionQueryVO.setStartDt(begin);
        String dimensionsJSON = retrieveDimensionsOfTag(tagDimensionQueryVO);
        TypeReference<HashMap<String, Set<String>>> typeReference = new TypeReference<HashMap<String, Set<String>>>() {
        };
        Map<String, Set<String>> dimensions = objectMapper.readValue(dimensionsJSON, typeReference);
        Map<String, TagDetailDTO> detailDTOMap = getTagCompleteness(tagName, begin, date, dimensions);
        return detailDTOMap.values().stream().peek(tagDetailDTO -> tagDetailDTO.setLocalDate(LocalDate.parse(tagDetailDTO.getDt(), dateTimeFormatter))).sorted(Comparator.comparing(TagDetailDTO::getLocalDate)).map(tagDetailDTO -> {
            TagDetailVO tagDetailVO = new TagDetailVO();
            tagDetailVO.setDt(tagDetailDTO.getDt());
            tagDetailVO.setTagCount(tagDetailDTO.getTagCount());
            tagDetailVO.setEventCount(tagDetailDTO.getEventCount());
            if (tagDetailVO.getEventCount() == 0) {
                tagDetailVO.setCompletenessPercent(0);
            } else {
                tagDetailVO.setCompletenessPercent(getPercent(tagDetailVO.getTagCount(), tagDetailVO.getEventCount()));
            }
            if (tagDetailDTO.getTagCount() == 0) {
                tagDetailVO.setTagFormatConsistentPercent(0);
                tagDetailVO.setTagAccuratePercent(0);
            } else {
                // only replace
                tagDetailVO.setTagFormatConsistentPercent(tagDetailDTO.getTagFormatInconsistentCount());
                //tagDetailVO.setTagAccuratePercent(getPercent(tagDetailDTO.getTagInaccurateCount(), tagDetailVO.getTagCount()));
                tagDetailVO.setTagAccuratePercent(tagDetailDTO.getTagInaccurateCount());
            }
            return tagDetailVO;
        }).collect(Collectors.toList());
    }

    private List<UsageOfDayVO> getTagUsage(String tagName, LocalDate begin, LocalDate dt) {
        List<UsageOfDayVO> usageOfDayVOList = new ArrayList<>();
        List<TagUsageInfoEntity> tagUsageInfoList = tagUsageInfoRepo.findAllByTagNameAndDtBetweenOrderByDt(tagName, begin, dt);
        if (!CollectionUtils.isEmpty(tagUsageInfoList)) {
            Map<LocalDate, List<TagUsageInfoEntity>> dateListMap = tagUsageInfoList.stream().collect(Collectors.groupingBy(TagUsageInfoEntity::getDt));
            dateListMap.forEach(((localDate, tagUsageInfos) -> {
                UsageOfDayVO usageOfDayVO = new UsageOfDayVO();
                List<String> users = new ArrayList<>();
                usageOfDayVOList.add(usageOfDayVO);
                usageOfDayVO.setOriginDt(localDate);
                usageOfDayVO.setDt(dateTimeFormatter.format(localDate));
                tagUsageInfos.forEach(tagUsageInfo -> {
                    if ("Batch".equals(tagUsageInfo.getAccountType())) {
                        usageOfDayVO.setBatch(tagUsageInfo.getAccessCount());
                    } else if ("Individual".equals(tagUsageInfo.getAccountType())) {
                        usageOfDayVO.setIndividual(tagUsageInfo.getAccessCount());
                    }
                    users.add(tagUsageInfo.getUsername());
                });
                usageOfDayVO.setUsers(users);
            }));
        }

        BiPredicate<List<UsageOfDayVO>, LocalDate> biPredicate = (list, d) -> {
            for (UsageOfDayVO usageOfDayVO : list) {
                if (usageOfDayVO.getOriginDt().equals(d)) {
                    return true;
                }
            }
            return false;
        };

        while (!begin.isAfter(dt)) {
            if (!biPredicate.test(usageOfDayVOList, begin)) {
                UsageOfDayVO usageOfDayVO = new UsageOfDayVO();
                usageOfDayVO.setOriginDt(begin);
                usageOfDayVO.setDt(dateTimeFormatter.format(begin));
                usageOfDayVO.setBatch(0);
                usageOfDayVO.setIndividual(0);
                usageOfDayVO.setUsers(Collections.emptyList());
                usageOfDayVOList.add(usageOfDayVO);
            }
            begin = begin.plusDays(1);
        }

        usageOfDayVOList.sort(Comparator.comparing(UsageOfDayVO::getOriginDt));
        return usageOfDayVOList;
    }

    @Override
    public String retrieveDimensionsOfTag(TagDimensionQueryVO tagDimensionQueryVO) {
        try {
            ObjectNode dimension = objectMapper.createObjectNode();
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoolQueryBuilder rootBuilder = QueryBuilders.boolQuery();
            rootBuilder.must(QueryBuilders.termQuery("metric_key", "profiling_tag_size"));
            rootBuilder.must(QueryBuilders.rangeQuery("dt").gte(dateTimeFormatter.format(tagDimensionQueryVO.getStartDt())).lte(dateTimeFormatter.format(tagDimensionQueryVO.getEndDt())));
            rootBuilder.filter(QueryBuilders.existsQuery("expr.tag_size_attr.tagMap." + tagDimensionQueryVO.getTagName()));
            builder.query(rootBuilder);
            builder.size(0);
            for (String tag : TAGS) {
                TermsAggregationBuilder aggregation = AggregationBuilders.terms(tag).field("tags." + tag + ".raw").size(20000).order(BucketOrder.key(true));
                builder.aggregation(aggregation);
            }
            //log.info("dropdown search request {}", builder);
            SearchRequest searchRequest = new SearchRequest(calculateIndexes(tagDimensionQueryVO.getStartDt(), tagDimensionQueryVO.getEndDt()), builder);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            for (String tag : TAGS) {
                ParsedStringTerms agg = searchResponse.getAggregations().get(tag);
                ArrayNode arrayNode = objectMapper.createArrayNode();
                for (Terms.Bucket bucket : agg.getBuckets()) {
                    String tagValue = bucket.getKeyAsString();
                    arrayNode.add(tagValue);
                }
                dimension.set(tag, arrayNode);
            }
            return dimension.toPrettyString();
        } catch (IOException e) {
            log.error("retrieveDimensionsOfTag {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Override
    public TagMetaDataVO getTagMetaData(String tagName, LocalDate dt) {
        List<TagLookUpInfo> tagInfoList = tagLookUpInfoRepo.findAllByTagName(tagName);
        TagMetaDataVO tagMetaDataVO = new TagMetaDataVO();
        if (!CollectionUtils.isEmpty(tagInfoList)) {
            TagLookUpInfo tagLookUpInfo = tagInfoList.get(0);
            BeanUtils.copyProperties(tagLookUpInfo, tagMetaDataVO);
            tagMetaDataVO.setCreUser(tagLookUpInfo.getOwnerEmail());
        } else tagMetaDataVO.setTagName(tagName);
        tagMetaDataVO.setDailyTagSize(getDailyTagSize(tagName, dt));
        double totalTagSize = dailySize(dt);
        if (totalTagSize == 0) {
            tagMetaDataVO.setDailyTagSizePercent("N/A");
        } else {
            tagMetaDataVO.setDailyTagSizePercent(Double.toString(getPercent(tagMetaDataVO.getDailyTagSize(), totalTagSize)));
        }
        List<UsageOfDayVO> usageOfDayVOList = getTagUsage(tagName, dt.minusMonths(1), dt);
        tagMetaDataVO.setUsageOfDayVOList(usageOfDayVOList);
        List<DailyTagSizeWithPercentVO> dailyTagSizeWithPercentVOList = getDailyTagSizeWithPercentBatch(tagName, dt);
        tagMetaDataVO.setDailyTagSizeWithPercentVOList(dailyTagSizeWithPercentVOList);
        return tagMetaDataVO;
    }

    @Override
    public List<TagDetailVO> queryTagDetail(TagDetailFilterQueryVO tagDetailFilterQueryVO) {
        String tagName = tagDetailFilterQueryVO.getTagName();
        LocalDate dt = tagDetailFilterQueryVO.getDate();
        LocalDate begin = dt.minusMonths(1).plusDays(1);
        List<LocalDate> expectedDates = new ArrayList<>(begin.datesUntil(dt.plusDays(1)).toList());
        Map<String, Set<String>> dimensions = tagDetailFilterQueryVO.getDimensions();
        Map<String, TagDetailDTO> detailDTOMap = getTagCompleteness(tagName, begin, dt, dimensions);
        List<AnomalyItemEntity> anomalyItemEntityList = anomalyItemRepository.findAllByTypeAndRefIdAndDtBetween("tag", tagName, begin, dt);

        List<TagDetailVO> tagDetailVOList = detailDTOMap.values().stream().peek(tagDetailDTO -> tagDetailDTO.setLocalDate(LocalDate.parse(tagDetailDTO.getDt(), dateTimeFormatter))).sorted(Comparator.comparing(TagDetailDTO::getLocalDate)).map(tagDetailDTO -> {
            TagDetailVO tagDetailVO = new TagDetailVO();
            tagDetailVO.setDt(tagDetailDTO.getDt());
            expectedDates.remove(tagDetailDTO.getLocalDate());
            tagDetailVO.setTagCount(tagDetailDTO.getTagCount());
            tagDetailVO.setEventCount(tagDetailDTO.getEventCount());
            if (tagDetailVO.getEventCount() == 0) {
                tagDetailVO.setCompletenessPercent(0);
            } else {
                tagDetailVO.setCompletenessPercent(getPercent(tagDetailVO.getTagCount(), tagDetailVO.getEventCount()));
            }
            if (tagDetailDTO.getTagCount() == 0) {
                tagDetailVO.setTagFormatConsistentPercent(0);
                tagDetailVO.setTagAccuratePercent(0);
            } else {
                tagDetailVO.setTagFormatConsistentPercent(getPercent(tagDetailVO.getTagCount() - tagDetailDTO.getTagFormatInconsistentCount(), tagDetailVO.getTagCount()));
                tagDetailVO.setTagAccuratePercent(getPercent(tagDetailVO.getTagCount() - tagDetailDTO.getTagInaccurateCount(), tagDetailVO.getTagCount()));
            }
            for (AnomalyItemEntity anomalyItemEntity : anomalyItemEntityList) {
                if (tagDetailDTO.getLocalDate().equals(anomalyItemEntity.getDt())) {
                    tagDetailVO.setAnomaly(true);
                    tagDetailVO.setLBound(BigDecimal.valueOf(anomalyItemEntity.getLowerBound()));
                    tagDetailVO.setUBound(BigDecimal.valueOf(anomalyItemEntity.getUpperBound()));
                    break;
                }
            }
            return tagDetailVO;
        }).collect(Collectors.toList());

        if (!expectedDates.isEmpty()){
            expectedDates.forEach(date -> {
                TagDetailVO tagDetailVO = new TagDetailVO();
                tagDetailVO.setDt(date.toString());
                tagDetailVOList.add(tagDetailVO);
            });
        }
        return tagDetailVOList;
    }

    private Map<String, TagDetailDTO> getTagCompleteness(String tagName, LocalDate begin, LocalDate dt, Map<String, Set<String>> dimensions) {
        try {
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoolQueryBuilder rootBuilder = QueryBuilders.boolQuery();
            rootBuilder.must(QueryBuilders.termQuery("metric_key", "profiling_tag_size"));
            if (MapUtils.isNotEmpty(dimensions)) {
                for (Map.Entry<String, Set<String>> entry : dimensions.entrySet()) {
                    rootBuilder.must(QueryBuilders.termsQuery("tags." + entry.getKey() + ".raw", entry.getValue()));
                }
            }
            rootBuilder.must(QueryBuilders.rangeQuery("dt").gte(dateTimeFormatter.format(begin)).lte(dateTimeFormatter.format(dt)));
            rootBuilder.filter(QueryBuilders.existsQuery("expr.tag_size_attr.tagMap." + tagName));
            builder.query(rootBuilder);
            builder.size(0);
            DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("agg").field("dt").calendarInterval(DateHistogramInterval.DAY).format("yyyy-MM-dd");
            SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("tagCount").field("expr.tag_size_attr.tagMap." + tagName + ".count");
            dateHistogramAggregationBuilder.subAggregation(sumAggregationBuilder);
            SumAggregationBuilder inconsistentFormatTagsCount = AggregationBuilders.sum("inconsistentFormatTagsCount").field("expr.tag_size_attr.tagMap." + tagName + ".inconsistentFormatTagsCount");
            dateHistogramAggregationBuilder.subAggregation(inconsistentFormatTagsCount);
            SumAggregationBuilder inaccurateTagCount = AggregationBuilders.sum("inaccurateTagCount").field("expr.tag_size_attr.tagMap." + tagName + ".inaccurateTagCount");
            dateHistogramAggregationBuilder.subAggregation(inaccurateTagCount);
            builder.aggregation(dateHistogramAggregationBuilder);

            //log.info("getTagCompleteness search request {}", builder);
            SearchRequest searchRequest = new SearchRequest(calculateIndexes(begin, dt), builder);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Histogram agg = searchResponse.getAggregations().get("agg");
            Map<String, TagDetailDTO> detailDTOMap = new HashMap<>();
            for (Histogram.Bucket bucket : agg.getBuckets()) {
                String date = bucket.getKeyAsString();
                Sum tagCount = bucket.getAggregations().get("tagCount");
                Sum inconsistentFormatCount = bucket.getAggregations().get("inconsistentFormatTagsCount");
                Sum inaccurateCount = bucket.getAggregations().get("inaccurateTagCount");
                TagDetailDTO tagDetailDTO = new TagDetailDTO();
                tagDetailDTO.setDt(date);
                tagDetailDTO.setTagCount(tagCount.getValue());
                tagDetailDTO.setTagFormatInconsistentCount(inconsistentFormatCount.getValue());
                tagDetailDTO.setTagInaccurateCount(inaccurateCount.getValue());
                detailDTOMap.put(date, tagDetailDTO);
            }

            // optimize if it's slow
            builder = new SearchSourceBuilder();
            rootBuilder = QueryBuilders.boolQuery();
            rootBuilder.must(QueryBuilders.termQuery("metric_key", "profiling_tag_size"));
            if (MapUtils.isNotEmpty(dimensions)) {
                for (Map.Entry<String, Set<String>> entry : dimensions.entrySet()) {
                    rootBuilder.must(QueryBuilders.termsQuery("tags." + entry.getKey() + ".raw", entry.getValue()));
                }
            }
            rootBuilder.must(QueryBuilders.rangeQuery("dt").gte(dateTimeFormatter.format(begin)).lte(dateTimeFormatter.format(dt)));
            // if only event include tag
            //rootBuilder.filter(QueryBuilders.existsQuery("expr.tag_size_attr.tagMap." + tagName));
            builder.query(rootBuilder);
            builder.size(0);
            dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("agg").field("dt").calendarInterval(DateHistogramInterval.DAY).format("yyyy-MM-dd");
            SumAggregationBuilder aggregationBuilder = AggregationBuilders.sum("eventTotal").field("expr.tag_size_attr.total");
            dateHistogramAggregationBuilder.subAggregation(aggregationBuilder);
            builder.aggregation(dateHistogramAggregationBuilder);
            //log.info("getDailyTagSizeBatch search request {}", builder);
            searchRequest = new SearchRequest(calculateIndexes(begin, dt), builder);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            agg = searchResponse.getAggregations().get("agg");
            for (Histogram.Bucket bucket : agg.getBuckets()) {
                String date = bucket.getKeyAsString();
                detailDTOMap.compute(date, (key, tagDetailDTO) -> {
                    if (null == tagDetailDTO){
                        tagDetailDTO = new TagDetailDTO();
                        tagDetailDTO.setDt(date);
                    }
                    Sum eventTotal = bucket.getAggregations().get("eventTotal");
                    tagDetailDTO.setEventCount(eventTotal.getValue());
                    return tagDetailDTO;
                });
            }
            return detailDTOMap;
        } catch (Exception e) {
            log.error("getTagCompleteness: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private double getPercent(double molecular, double denominator) {
        return new BigDecimal(molecular / denominator).setScale(4, RoundingMode.DOWN).doubleValue();
    }

    private List<DailyTagSizeWithPercentVO> getDailyTagSizeWithPercentBatch(String tagName, LocalDate dt) {
        try {
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoolQueryBuilder rootBuilder = QueryBuilders.boolQuery();
            rootBuilder.must(QueryBuilders.termQuery("metric_key", "profiling_tag_size"));
            LocalDate begin = dt.minusMonths(1);
            rootBuilder.must(QueryBuilders.rangeQuery("dt").gte(dateTimeFormatter.format(begin)).lte(dateTimeFormatter.format(dt)));
            rootBuilder.filter(QueryBuilders.existsQuery("expr.tag_size_attr.tagMap." + tagName));
            builder.query(rootBuilder);
            builder.size(0);
            DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("agg").field("dt").calendarInterval(DateHistogramInterval.DAY).format("yyyy-MM-dd");
            SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("tagSize").field("expr.tag_size_attr.tagMap." + tagName + ".total");
            dateHistogramAggregationBuilder.subAggregation(sumAggregationBuilder);
            builder.aggregation(dateHistogramAggregationBuilder);

            //log.info("getDailyTagSizeBatch search request {}", builder);
            SearchRequest searchRequest = new SearchRequest(calculateIndexes(begin, dt), builder);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Histogram agg = searchResponse.getAggregations().get("agg");
            List<DailyTagSizeWithPercentVO> dailyTagSizeWithPercentVOList = new ArrayList<>();
            Map<String, DailyTagSizeWithPercentVO> map = new HashMap<>();
            for (Histogram.Bucket bucket : agg.getBuckets()) {
                String date = bucket.getKeyAsString();
                Sum tagSize = bucket.getAggregations().get("tagSize");
                DailyTagSizeWithPercentVO dailyTagSizeWithPercentVO = new DailyTagSizeWithPercentVO();
                dailyTagSizeWithPercentVO.setDt(date);
                dailyTagSizeWithPercentVO.setTagSize(tagSize.getValue());
                dailyTagSizeWithPercentVOList.add(dailyTagSizeWithPercentVO);
                map.put(date, dailyTagSizeWithPercentVO);
            }

            // optimize if it's slow
            builder = new SearchSourceBuilder();
            rootBuilder = QueryBuilders.boolQuery();
            rootBuilder.must(QueryBuilders.termQuery("metric_key", "profiling_tag_size"));
            rootBuilder.must(QueryBuilders.rangeQuery("dt").gte(dateTimeFormatter.format(begin)).lte(dateTimeFormatter.format(dt)));
            //rootBuilder.filter(QueryBuilders.existsQuery("expr.tag_size_attr.tagMap." + tagName));
            builder.query(rootBuilder);
            builder.size(0);
            dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("agg").field("dt").calendarInterval(DateHistogramInterval.DAY).format("yyyy-MM-dd");
            SumAggregationBuilder aggregationBuilder = AggregationBuilders.sum("totalTagSize").field("expr.tag_size_attr.totalLength");
            dateHistogramAggregationBuilder.subAggregation(aggregationBuilder);
            builder.aggregation(dateHistogramAggregationBuilder);
            //log.info("getDailyTagSizeBatch search request {}", builder);
            searchRequest = new SearchRequest(calculateIndexes(begin, dt), builder);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            agg = searchResponse.getAggregations().get("agg");
            for (Histogram.Bucket bucket : agg.getBuckets()) {
                String date = bucket.getKeyAsString();
                DailyTagSizeWithPercentVO dailyTagSizeWithPercentVO = map.get(date);
                if (dailyTagSizeWithPercentVO == null) continue;
                Sum totalTagSize = bucket.getAggregations().get("totalTagSize");
                if (totalTagSize.getValue() != 0) {
                    double percent = getPercent(dailyTagSizeWithPercentVO.getTagSize(), totalTagSize.getValue());
                    dailyTagSizeWithPercentVO.setPercent(percent);
                } else {
                    dailyTagSizeWithPercentVO.setPercent(0);
                }
            }
            return dailyTagSizeWithPercentVOList;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private double getDailyTagSize(String tagName, LocalDate dt) {
        try {
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoolQueryBuilder rootBuilder = QueryBuilders.boolQuery();
            rootBuilder.must(QueryBuilders.termQuery("metric_key", "profiling_tag_size"));
            rootBuilder.must(QueryBuilders.termQuery("dt", dateTimeFormatter.format(dt)));
            rootBuilder.filter(QueryBuilders.existsQuery("expr.tag_size_attr.tagMap." + tagName));
            builder.query(rootBuilder);
            builder.size(0);
            SumAggregationBuilder aggregationBuilder = AggregationBuilders.sum("tagSize").field("expr.tag_size_attr.tagMap." + tagName + ".total");
            builder.aggregation(aggregationBuilder);
            //log.info("getDailyTagSize search request {}", builder);
            SearchRequest searchRequest = new SearchRequest(calculateIndexes(dt, dt), builder);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Sum agg = searchResponse.getAggregations().get("tagSize");
            return agg.getValue();
        } catch (IOException e) {
            log.error("getDailyTagSize: {}", e.getMessage());
            return 0;
        }
    }

    private double getDailyTagSize(List<String> tagNames, LocalDate dt) {
        try {
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoolQueryBuilder rootBuilder = QueryBuilders.boolQuery();
            rootBuilder.must(QueryBuilders.termQuery("metric_key", "profiling_tag_size"));
            rootBuilder.must(QueryBuilders.termQuery("dt", dateTimeFormatter.format(dt)));
            //rootBuilder.filter(QueryBuilders.existsQuery("expr.tag_size_attr.tagMap." + tagName));
            builder.query(rootBuilder);
            builder.size(0);
            tagNames.forEach(tagName -> {
                SumAggregationBuilder aggregationBuilder = AggregationBuilders.sum("tagSize-" + tagName).field("expr.tag_size_attr.tagMap." + tagName + ".total");
                builder.aggregation(aggregationBuilder);
            });

            //log.info("getDailyTagSize search request {}", builder);
            SearchRequest searchRequest = new SearchRequest(calculateIndexes(dt, dt), builder);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            double result = 0.0;
            List<Double> results = new ArrayList<>();
            tagNames.forEach(tagName -> {
                Sum agg = searchResponse.getAggregations().get("tagSize-" + tagName);
                if (null != agg) {
                    results.add(agg.getValue());
                }
            });
            return results.stream().reduce(result, Double::sum);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private double dailySize(LocalDate dt) {
        try {
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoolQueryBuilder rootBuilder = QueryBuilders.boolQuery();
            rootBuilder.must(QueryBuilders.termQuery("metric_key", "profiling_tag_size"));
            rootBuilder.must(QueryBuilders.termQuery("dt", dateTimeFormatter.format(dt)));
            builder.query(rootBuilder);
            builder.size(0);
            SumAggregationBuilder aggregationBuilder = AggregationBuilders.sum("totalTagSize").field("expr.tag_size_attr.totalLength");
            builder.aggregation(aggregationBuilder);
            SearchRequest searchRequest = new SearchRequest(calculateIndexes(dt, dt), builder);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Sum agg = searchResponse.getAggregations().get("totalTagSize");
            return agg.getValue();
        } catch (IOException e) {
            log.error(e.getMessage());
            return 0;
        }
    }

    private String[] calculateIndexes(LocalDate from, LocalDate to) {
        Set<String> results = new HashSet<>();
        LocalDate date = from;
        String env = "qa";
        while (!date.isAfter(to)) {
            String dt = dateTimeFormatter.format(date);
            String index = String.format(TDQ_PROFILING_METRIC_S_S, env, dt);
            results.add(index);
            date = date.plusDays(1);
        }
        //log.info("search request indexes=>{}", StringUtils.join(results, ","));
        return results.toArray(new String[0]);
    }

    private String[] calculateIndexes(List<LocalDate> dates) {
        Set<String> results = new HashSet<>();
        String env = "ConstantDefine.CUR_ENV.toLowerCase()";
        for (LocalDate date : dates) {
            String dt = dateTimeFormatter.format(date);
            String index = String.format(TDQ_PROFILING_METRIC_S_S, env, dt);
            results.add(index);
        }
        log.info("search request indexes=>{}", StringUtils.join(results, ","));
        return results.toArray(new String[0]);
    }
}

class MandatoryTag {
    /**
     * refer: <a href="https://wiki.corp.ebay.com/display/TDQ/TDQ+Scorecard+-+Missing+Mandatory+Tags">mandatory tags</a>
     */
    public static final List<String> MANDATORY_TAGS = Arrays.asList(
            "t", "g", "h", "p", "TPool", "TMachine", "TStamp", "RemoteIP", "Server", "TDuration", "TPayload", "TType", "TName", "TStatus",
            "ec", "es", "nqc", "nqt",
            "udid", "memsz", "mav", "dn", "mos", "osv", "tzname", "carrier", "tz", "mnt", "ist"
    );

    public static final Set<String> TOPN_TAG_USAGE = new HashSet<>();
}