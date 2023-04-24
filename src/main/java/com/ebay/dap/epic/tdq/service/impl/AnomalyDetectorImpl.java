package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.data.entity.NonBotPageCountEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.NonBotPageCountMapper;
import com.ebay.dap.epic.tdq.service.AnomalyDetector;
import com.ebay.dap.epic.tdq.service.mmd.MMDRestException;
import com.ebay.dap.epic.tdq.service.mmd.MMDService;
import com.ebay.dap.epic.tdq.service.mmd.Series;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Slf4j
public class AnomalyDetectorImpl implements AnomalyDetector {

    @Autowired
    private NonBotPageCountMapper nonBotPageCountMapper;

    @Autowired
    private MMDService mmdService;

    @Override
    public void findAbnormalPages(LocalDate dt) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 1. get pages id list with traffic > 10 million and first seen date is past 3 months
        List<Integer> pages = nonBotPageCountMapper.findPageIdsForMMD(dt);
        log.info("PageId list for sending to MMD: {}, count: {}, dt: {}", pages, pages.size(), dt);

        for (List<Integer> bulkPages : Lists.partition(pages, 10)) {
            List<NonBotPageCountEntity> pageHisTraffic =
                    nonBotPageCountMapper.findAllByPageIdInAndDtGreaterThanEqualAndDtLessThanEqual(bulkPages,
                            dt.minusDays(90).format(formatter),
                            dt.format(formatter));

            Map<Integer, List<NonBotPageCountEntity>> map = pageHisTraffic.stream()
                    .collect(groupingBy(NonBotPageCountEntity::getPageId));
            Map<String, List<Series>> mmdSeries = new HashMap<>();
            for (Map.Entry<Integer, List<NonBotPageCountEntity>> entry : map.entrySet()) {
                List<Series> series = entry.getValue().stream()
                        .map(e -> {
                            Series s = new Series();
                            s.setTimestamp(DateTimeFormatter.ISO_LOCAL_DATE.format(formatter.parse(e.getDt())));
                            s.setValue(e.getTotal().doubleValue());
                            return s;
                        })
                        .collect(Collectors.toList());
                mmdSeries.put(String.valueOf(entry.getKey()), series);
            }

            // 3. get bulk result and save to db
            try {
                mmdService.bulkFindAnomalyDaily("page_profiling_daily", mmdSeries);
            } catch (MMDRestException e) {
                log.error("error: {}", e.getMessage());
            }
        }

        // 4. done
        log.info("Run find abnormal pages task successfully");
    }
}
