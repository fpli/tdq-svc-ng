package com.ebay.dap.epic.tdq.schedule.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.EmailConfigEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.EmailConfigEntityMapper;
import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.vo.alert.AlertItem;
import com.ebay.dap.epic.tdq.data.vo.alert.EmailAlert;
import com.ebay.dap.epic.tdq.service.EmailService;
import com.ebay.dap.epic.tdq.service.MetricService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Component
@Lazy(false)
public class UTPDailyClickAlertTask {

    @Autowired
    private MetricService metricService;

    @Autowired
    private EmailService emailService;

    private static final String METRIC_KEY = "utp_click_daily_cnt";

    @Autowired
    private EmailConfigEntityMapper emailConfigEntityMapper;


    @Scheduled(cron = "${tdqsvcngcfg.schedule.cron.utp-daily-click-alert:-}", zone = "GMT-7")
    @SchedulerLock(name = "UTPDailyClickAlertTask", lockAtLeastFor = "PT10M", lockAtMostFor = "PT30M")
    public void run() throws Exception {
        // T-1 as endDt
        LocalDate endDt = LocalDate.now(ZoneId.of("GMT-7")).minusDays(1);
        log.info("Evaluate alert - utp daily click event volume by channel");

        // check if metric exists
        if (!metricService.dailyMetricExists(METRIC_KEY, endDt)) {
            throw new IllegalStateException("metric: " + METRIC_KEY + " does not exist in ES");
        }

        List<MetricDoc> metricDocs = metricService.getDailyMetricDimensionSeries(METRIC_KEY, endDt, 31);

        Map<String, List<MetricDoc>> channelCntMap = metricDocs.stream().collect(groupingBy(e -> e.getDimension().values().toArray()[0].toString()));

        EmailAlert emailAlert = new EmailAlert();
        emailAlert.setDt(endDt.toString());
        emailAlert.setItems(new ArrayList<>());

        for (Map.Entry<String, List<MetricDoc>> entry : channelCntMap.entrySet()) {
            String channel = entry.getKey();
            List<MetricDoc> channelMetricDocs = entry.getValue();
            List<MetricDoc> sorted = Lists.newArrayList(channelMetricDocs.stream().sorted(Comparator.comparing(MetricDoc::getDt).reversed()).toList());
            MetricDoc current = sorted.get(0);

            sorted.removeIf(metricDoc -> metricDoc.getDt().equals(endDt));

            double avg = sorted.stream().mapToLong(e -> e.getValue().longValue())
                               .average()
                               .orElse(0);

            long threshold = Math.round(avg / 2);

            if (!current.getDt().equals(endDt)) {
                // no value found on endDt, treat as 0
                // create alert item
                AlertItem item = new AlertItem();
                item.setTitle(channel);
                item.setThreshold(NumberFormat.getNumberInstance(Locale.US).format(threshold));
                item.setCurrentValue("0");
                item.setDiffPct("-100%");

                emailAlert.getItems().add(item);

            } else if (current.getValue().longValue() < threshold) {
                long curVal = current.getValue().longValue();
                double diff = (double) (curVal - threshold) / threshold;

                AlertItem item = new AlertItem();
                item.setTitle(channel);
                item.setThreshold(NumberFormat.getNumberInstance(Locale.US).format(threshold));
                item.setCurrentValue(NumberFormat.getNumberInstance(Locale.US).format(curVal));
                item.setDiffPct(new DecimalFormat("#.##%").format(diff));

                emailAlert.getItems().add(item);
            }
        }

        // TODO: save alerting in db
        if (emailAlert.getItems().size() > 0) {
            // send alert email
            log.info("Found alerting items for UTP Daily Click: {}", emailAlert);

            Context context = new Context();
            context.setVariable("emailAlert", emailAlert);

            LambdaQueryWrapper<EmailConfigEntity> lambdaQuery = Wrappers.lambdaQuery();
            lambdaQuery.eq(EmailConfigEntity::getName, "UTP Daily Click Volume");
            EmailConfigEntity emailConfigEntity = emailConfigEntityMapper.selectOne(lambdaQuery);

            List<String> to = Arrays.stream(emailConfigEntity.getRecipient().split(",")).map(String::strip).toList();
//            emailService.sendHtmlEmail("TDQ Alerts - UTP Daily Click Volume",
//                    "utp-daily-click-alert", context,
//                    Lists.newArrayList("DL-eBay-Tracking-Data-Quality-Dev@ebay.com"));
            emailService.sendHtmlEmail("TDQ Alerts - UTP Daily Click Volume",
                    "utp-daily-click-alert", context,
                    to);
        }

    }
}
