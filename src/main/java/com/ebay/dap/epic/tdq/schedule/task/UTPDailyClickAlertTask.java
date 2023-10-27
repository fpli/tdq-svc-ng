package com.ebay.dap.epic.tdq.schedule.task;

import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.vo.email.UtpAlertItemVo;
import com.ebay.dap.epic.tdq.data.vo.email.UtpAlertVo;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Component("UTPDailyClickAlertTask")
@Lazy(false)
public class UTPDailyClickAlertTask {

    @Autowired
    private MetricService metricService;

    @Autowired
    private EmailService emailService;

    private static final String METRIC_KEY = "utp_click_daily_cnt";


    /**
     * Run at 5:00 PM MST everyday
     */
    @Scheduled(cron = "0 0 17 * * *", zone = "GMT-7")
    @SchedulerLock(name = "UTPDailyClickAlertTask", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void run() throws Exception {
        // T-1 as endDt
        LocalDate endDt = LocalDate.now(ZoneId.of("GMT-7")).minusDays(1);
        log.info("Evaluate alert - utp daily click event volume by channel");

        // check if metric exists
        if (!metricService.dailyMetricExists(METRIC_KEY, endDt)) {
            throw new IllegalStateException("metric: " + METRIC_KEY + " does not exist in ES");
        }

        List<MetricDoc> metricDocs = metricService.getDailyMetricDimensionSeries(METRIC_KEY, endDt, 31);

        Map<String, List<MetricDoc>> channelCntMap = metricDocs.stream()
                                                               .collect(groupingBy(e -> e.getDimension()
                                                                                         .values()
                                                                                         .toArray()[0].toString()));

        UtpAlertVo alertVo = new UtpAlertVo();
        alertVo.setMetricTime(endDt.toString());
        alertVo.setItems(new ArrayList<>());

        for (Map.Entry<String, List<MetricDoc>> entry : channelCntMap.entrySet()) {
            String channel = entry.getKey();

            if (channel.equals("MRKT_SMS")) {
                // skip MRKT_SMS as it's in test phase
                continue;
            }

            List<MetricDoc> channelMetricDocs = entry.getValue();
            List<MetricDoc> sorted = Lists.newArrayList(channelMetricDocs.stream()
                                                                         .sorted(Comparator.comparing(MetricDoc::getDt)
                                                                                           .reversed())
                                                                         .toList());
            MetricDoc current = sorted.get(0);

            sorted.removeIf(metricDoc -> metricDoc.getDt().equals(endDt));

            double avg = sorted.stream().mapToLong(e -> e.getValue().longValue())
                               .average()
                               .orElse(0);

            long threshold = Math.round(avg / 2);

            if (!current.getDt().equals(endDt)) {
                // no value found on endDt, treat as 0
                // create alert item
                UtpAlertItemVo item = new UtpAlertItemVo();
                item.setChannel(channel);
                item.setThreshold(NumberFormat.getNumberInstance(Locale.US).format(threshold));
                item.setCurrentVal("0");
                item.setChangePct("-100%");

                alertVo.getItems().add(item);

            } else if (current.getValue().longValue() < threshold) {
                long curVal = current.getValue().longValue();
                double diff = (double) (curVal - threshold) / threshold;

                UtpAlertItemVo item = new UtpAlertItemVo();
                item.setChannel(channel);
                item.setThreshold(NumberFormat.getNumberInstance(Locale.US).format(threshold));
                item.setCurrentVal(NumberFormat.getNumberInstance(Locale.US).format(curVal));
                item.setChangePct(new DecimalFormat("#.##%").format(diff));

                alertVo.getItems().add(item);
            }
        }

        // TODO: save alerting in db
        if (alertVo.getItems().size() > 0) {
            // send alert email
            log.info("Found alerting items for UTP Daily Click: {}", alertVo);

            Context context = new Context();
            context.setVariable("emailAlert", alertVo);

            emailService.sendEmail("alert-utp-daily-click", context, "UTP Daily Click Volume");
        }

    }
}
