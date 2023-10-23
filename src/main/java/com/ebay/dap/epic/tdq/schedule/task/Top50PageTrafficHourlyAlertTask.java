package com.ebay.dap.epic.tdq.schedule.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.common.util.DateTimeUtils;
import com.ebay.dap.epic.tdq.data.entity.AlertSuppressionPageCfgEntity;
import com.ebay.dap.epic.tdq.data.entity.Top50PageEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.AlertSuppressionPageCfgMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.Top50PageMapper;
import com.ebay.dap.epic.tdq.data.pronto.PageMetricDoc;
import com.ebay.dap.epic.tdq.data.vo.alert.PageAlertItemVo;
import com.ebay.dap.epic.tdq.data.vo.alert.PageAlertVo;
import com.ebay.dap.epic.tdq.service.EmailService;
import com.ebay.dap.epic.tdq.service.MetricService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is just a temp solution to monitor top 50 pages traffic
 */
@Slf4j
@Component("Top50PageTrafficHourlyAlertTask")
@Lazy(false)
public class Top50PageTrafficHourlyAlertTask {

    @Autowired
    private MetricService metricService;

    @Autowired
    private Top50PageMapper top50PageMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AlertSuppressionPageCfgMapper alertSuppressionPageCfgMapper;

    @Scheduled(cron = "0 30 * * * *")
    @SchedulerLock(name = "Top50PageTrafficHourlyAlertTask", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void run() throws Exception {

        // TODO: add logic to detect if there is upstream delay.

        QueryWrapper<AlertSuppressionPageCfgEntity> query = Wrappers.query();
        query.select("page_id, max(suppress_util) as suppress_util")
             .groupBy("page_id");
        List<AlertSuppressionPageCfgEntity> alertSuppressionPageCfgEntities = alertSuppressionPageCfgMapper.selectList(query);

        List<Top50PageEntity> top50Pages = top50PageMapper.findAll();

        if (alertSuppressionPageCfgEntities != null) {
            for (AlertSuppressionPageCfgEntity entity : alertSuppressionPageCfgEntities) {
                if (entity.getSuppressUtil().isAfter(LocalDateTime.now())) {
                    top50Pages.removeIf(top50PageEntity -> top50PageEntity.getPageId().equals(entity.getPageId()));
                }
            }
        }

        List<Integer> top50pageIds = top50Pages.stream().map(Top50PageEntity::getPageId).toList();

        LocalDateTime dateTime = DateTimeUtils.instantToLocalDateTime(Instant.now())
                                              .minusHours(1)
                                              .truncatedTo(ChronoUnit.HOURS);

        List<PageMetricDoc> top50PageMetricDoc = metricService.getTop50PageMetricDoc(top50pageIds, dateTime.toLocalDate(), dateTime.getHour());

        Map<Integer, List<PageMetricDoc>> collect = top50PageMetricDoc.stream()
                                                                      .collect(Collectors.groupingBy(PageMetricDoc::getPageId));

        PageAlertVo vo = new PageAlertVo();
        vo.setMetricTime(dateTime.toString());
        vo.setItems(new ArrayList<>());

        for (Top50PageEntity page : top50Pages) {
            Integer pageId = page.getPageId();

            if (collect.containsKey(pageId)) {
                List<PageMetricDoc> pageMetricDocs = collect.get(pageId);

                pageMetricDocs.sort(Comparator.comparing(PageMetricDoc::getDt).reversed());
                PageMetricDoc pageMetricDoc = pageMetricDocs.get(0);
                if (!pageMetricDoc.getMetricTime().equals(dateTime)) {
                    // cannot find metric for given datetime
                    PageAlertItemVo itemVo = new PageAlertItemVo();
                    itemVo.setPageId(pageId);
                    itemVo.setPageName(page.getPageName());
                    itemVo.setPageFmly(page.getPageFamily());
                    itemVo.setIFrame(page.getIframe());
                    itemVo.setCurrentVal(-1L);
                    itemVo.setAvgOfLast4W(-1L);
                    vo.getItems().add(itemVo);
                } else {
                    pageMetricDocs.remove(0);
                    double asDouble = pageMetricDocs.stream()
                                                    .mapToLong(PageMetricDoc::getEventCnt)
                                                    .average()
                                                    .getAsDouble();
                    long avg = (long) asDouble;

                    double diffPct = (pageMetricDoc.getEventCnt() - asDouble) / asDouble;

                    if (Math.abs(diffPct) > 0.5) {
                        PageAlertItemVo itemVo = new PageAlertItemVo();
                        itemVo.setPageId(pageId);
                        itemVo.setPageId(pageId);
                        itemVo.setPageName(page.getPageName());
                        itemVo.setPageFmly(page.getPageFamily());
                        itemVo.setIFrame(page.getIframe());
                        itemVo.setCurrentVal(pageMetricDoc.getEventCnt());
                        itemVo.setAvgOfLast4W(avg);
                        vo.getItems().add(itemVo);
                    }
                }

            } else {
                // if cannot find any metrics, add to the error list
                PageAlertItemVo itemVo = new PageAlertItemVo();
                itemVo.setPageId(pageId);
                itemVo.setPageId(pageId);
                itemVo.setPageName(page.getPageName());
                itemVo.setPageFmly(page.getPageFamily());
                itemVo.setIFrame(page.getIframe());
                itemVo.setCurrentVal(-1L);
                itemVo.setAvgOfLast4W(-1L);
                vo.getItems().add(itemVo);
            }
        }


        if (CollectionUtils.isNotEmpty(vo.getItems())) {
            Context context = new Context();
            context.setVariable("pageAlert", vo);
            emailService.sendEmail("alert-rt-top50-page", "Top50 Page RT Abnormal Alert", context);
        }

    }

}
