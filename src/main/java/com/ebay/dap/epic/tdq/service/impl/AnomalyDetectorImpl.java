package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.data.entity.AnomalyItemEntity;
import com.ebay.dap.epic.tdq.data.entity.NonBotPageCountEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.AnomalyItemMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.NonBotPageCountMapper;
import com.ebay.dap.epic.tdq.service.AnomalyDetector;
import com.ebay.dap.epic.tdq.service.mmd.MMDException;
import com.ebay.dap.epic.tdq.service.mmd.MMDService;
import com.ebay.dap.epic.tdq.service.mmd.Series;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Slf4j
public class AnomalyDetectorImpl implements AnomalyDetector {

    @Autowired
    private NonBotPageCountMapper nonBotPageCountMapper;

    @Autowired
    private MMDService mmdService;

    @Autowired
    private AnomalyItemMapper anomalyItemMapper;

    final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyyMMdd");

    /***
     * 1. get pageId list with traffic > 10 million and first seen date is past 3 months
     * 2. assemble MMD request and bulk request to get alert results
     * 3. save alerts to db
     *
     * @param dt
     */
    @Override
    public void findAbnormalPages(LocalDate dt) throws MMDException {
        Preconditions.checkNotNull(dt);

        // 1. get pages id list with traffic > 10 million and first seen date is past 3 months
        List<Integer> pages = nonBotPageCountMapper.findPageIdsForMMD(dt);

        if (CollectionUtils.isEmpty(pages)) {
            log.info("No PageId is qualified to be sent to MMD for anomaly detection");
            return;
        }

        log.info("PageId list for sending to MMD: {}, count: {}, dt: {}", pages, pages.size(), dt);

        List<AnomalyItemEntity> allAnomalyItems = new ArrayList<>();

        final LocalDate startDt = dt.minusDays(90);

        // process 10 pages as a batch at once
        for (List<Integer> bulkPages : Lists.partition(pages, 10)) {
            List<NonBotPageCountEntity> pageCountList =
                    nonBotPageCountMapper.findAllByPageIdInAndDtGreaterThanEqualAndDtLessThanEqual(bulkPages,
                            startDt.format(dtFmt),
                            dt.format(dtFmt));

            Map<Integer, List<NonBotPageCountEntity>> map = pageCountList.stream()
                                                                         .collect(groupingBy(NonBotPageCountEntity::getPageId));

            Map<String, List<Series>> mmdSeries = covertToMMDSeries(map, startDt, dt);

            // 2. bulk request to MMD
            List<AnomalyItemEntity> anomalyItems = mmdService.bulkFindAnomalyDaily("page_profiling_daily", mmdSeries);
            allAnomalyItems.addAll(anomalyItems);
        }

        log.info("Found {} abnormal pages from MMD", allAnomalyItems.size());
        // 3. save alerts into db
        if (CollectionUtils.isNotEmpty(allAnomalyItems)) {
            log.info("Save {} abnormal pages into database", allAnomalyItems.size());
            anomalyItemMapper.saveAll(allAnomalyItems);
        }
    }

    private Map<String, List<Series>> covertToMMDSeries(Map<Integer, List<NonBotPageCountEntity>> map, LocalDate startDt, LocalDate endDt) {
        Map<String, List<Series>> mmdSeries = new HashMap<>();
        long days = ChronoUnit.DAYS.between(startDt, endDt) + 1;
        for (Map.Entry<Integer, List<NonBotPageCountEntity>> entry : map.entrySet()) {
            final Integer pageId = entry.getKey();
            List<NonBotPageCountEntity> pageCntList = entry.getValue();
            if (pageCntList.size() < days) {
                fillNullPageCount(pageCntList, startDt, endDt);
            }

            List<Series> series = pageCntList.stream()
                                             .map(e -> {
                                                 Series s = new Series();
                                                 s.setTimestamp(DateTimeFormatter.ISO_LOCAL_DATE.format(dtFmt.parse(e.getDt())));
                                                 s.setValue(e.getTotal().doubleValue());
                                                 return s;
                                             }).sorted(Comparator.comparing(Series::getTimestamp).reversed())
                                             .collect(Collectors.toList());

            mmdSeries.put(String.valueOf(pageId), series);
        }
        return mmdSeries;
    }

    private void fillNullPageCount(List<NonBotPageCountEntity> pageCntList, LocalDate startDt, LocalDate endDt) {
        if (CollectionUtils.isEmpty(pageCntList)) {
            throw new IllegalArgumentException();
        }
        final Integer pageId = pageCntList.get(0).getPageId();
        Set<String> dates = pageCntList.stream().map(NonBotPageCountEntity::getDt).collect(Collectors.toSet());
        LocalDate dt = startDt;
        while (dt.isBefore(endDt)) {
            if (!dates.contains(dt.format(dtFmt))) {
                NonBotPageCountEntity entity = new NonBotPageCountEntity();
                entity.setPageId(pageId);
                entity.setTotal(0L);
                entity.setDt(dt.format(dtFmt));
                pageCntList.add(entity);
            }
            dt = dt.plusDays(1);
        }
    }
}
