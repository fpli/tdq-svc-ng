package com.ebay.dap.epic.tdq.schedule.task;

import com.ebay.dap.epic.tdq.common.util.DateTimeUtils;
import com.ebay.dap.epic.tdq.data.entity.Top50PageEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.AlertSuppressionPageCfgMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.Top50PageMapper;
import com.ebay.dap.epic.tdq.data.pronto.PageMetricDoc;
import com.ebay.dap.epic.tdq.data.vo.email.PageAlertItemVo;
import com.ebay.dap.epic.tdq.data.vo.email.PageAlertVo;
import com.ebay.dap.epic.tdq.service.EmailService;
import com.ebay.dap.epic.tdq.service.MetricService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ebay.dap.epic.tdq.common.util.DateTimeUtils.instantToLocalDateTime;

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
    @Qualifier("externalEmailService")
    private EmailService externalEmailService;

    @Autowired
    private AlertSuppressionPageCfgMapper alertSuppressionPageCfgMapper;

    // threshold set to drop 50%
    private static final double THRESHOLD_PCT = -0.5;

    /**
     * Run every hour at 30'
     */
    @Scheduled(cron = "0 30 * * * *")
    @SchedulerLock(name = "Top50PageTrafficHourlyAlertTask", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void run() throws Exception {
        // TODO: add logic to detect if there is upstream delay.

        List<Top50PageEntity> top50Pages = top50PageMapper.findAll();

        // remove pages in alert_suppression_page_cfg
        List<Integer> suppressedPageIds = alertSuppressionPageCfgMapper.listValidPageIds();
        if (CollectionUtils.isNotEmpty(suppressedPageIds)) {
            suppressedPageIds.forEach(page -> top50Pages.removeIf(e -> e.getPageId().equals(page)));
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

                // get current datetime doc
                Optional<PageMetricDoc> currentPageMetricDocOption = pageMetricDocs.stream()
                                                                                   .filter(p -> dateTime.equals(instantToLocalDateTime(p.getMetricTime())))
                                                                                   .max(Comparator.comparing(PageMetricDoc::getRtEventCnt));

                if (currentPageMetricDocOption.isEmpty()) {
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
                    PageMetricDoc currentPageMetricDoc = currentPageMetricDocOption.get();

                    // remove current date time's data points
                    pageMetricDocs.removeIf(p -> dateTime.equals(instantToLocalDateTime(p.getMetricTime())));

                    double avgAsDouble = pageMetricDocs.stream()
                                                       .mapToLong(PageMetricDoc::getRtEventCnt)
                                                       .filter(e -> e > 1000)
                                                       .average()
                                                       .getAsDouble();
                    long avg = (long) avgAsDouble;

                    double diffPct = (currentPageMetricDoc.getRtEventCnt() - avgAsDouble) / avgAsDouble;

                    if (diffPct < THRESHOLD_PCT) {
                        PageAlertItemVo itemVo = new PageAlertItemVo();
                        itemVo.setPageId(pageId);
                        itemVo.setPageId(pageId);
                        itemVo.setPageName(page.getPageName());
                        itemVo.setPageFmly(page.getPageFamily());
                        itemVo.setIFrame(page.getIframe());
                        itemVo.setCurrentVal(currentPageMetricDoc.getRtEventCnt());
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
            emailService.sendHtmlEmail("alert-rt-top50-page", context, "Top50 Page RT Abnormal Alert");
            externalEmailService.sendHtmlEmail("alert-rt-top50-page", context, "Top50 Page RT Abnormal Alert - PagerDuty");
        }

    }

}
