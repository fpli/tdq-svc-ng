package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.dto.AdsClickFraudDTO;
import com.ebay.dap.epic.tdq.data.dto.LegacyItemDTO;
import com.ebay.dap.epic.tdq.data.dto.PageAlertDto;
import com.ebay.dap.epic.tdq.data.dto.PageAlertItemDto;
import com.ebay.dap.epic.tdq.data.entity.AnomalyItemEntity;
import com.ebay.dap.epic.tdq.data.entity.CustomerGroupEntity;
import com.ebay.dap.epic.tdq.data.entity.EmailConfigEntity;
import com.ebay.dap.epic.tdq.data.entity.PageLookUpInfo;
import com.ebay.dap.epic.tdq.data.entity.ProfilingCustomerPageRel;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.AnomalyItemMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.CustomerGroupMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.EmailConfigEntityMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.NonBotPageCountMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.PageLookUpInfoMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.ProfilingCustomerPageRelMapper;
import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.vo.email.MultipleUidDTO;
import com.ebay.dap.epic.tdq.service.AlertManager;
import com.ebay.dap.epic.tdq.service.EmailService;
import com.ebay.dap.epic.tdq.service.MetricService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AlertManagerImpl implements AlertManager {

    @Autowired
    private AnomalyItemMapper anomalyItemMapper;

    @Autowired
    private PageLookUpInfoMapper pageLookUpInfoRepo;

    @Autowired
    private CustomerGroupMapper customerGroupRepo;

    @Autowired
    private ProfilingCustomerPageRelMapper profilingCustomerPageRelRepo;

    @Autowired
    private NonBotPageCountMapper nonBotPageCountRepo;

    @Autowired
    private EmailService emailService;

    private static final String emailSubject = "TDQ Alerts - Page Profiling Abnormal Alert";

    @Value("#{'${notification.email.cc}'.split(',')}")
    private List<String> ccList;

    @Value("#{'${notification.email.to}'.split(',')}")
    private List<String> toList;

    @Autowired
    private EmailConfigEntityMapper emailConfigEntityMapper;

    @Autowired
    private MetricService metricService;

    @Override
    public void sendPageProfilingAlertEmail(LocalDate dt) throws Exception {
        List<AnomalyItemEntity> abnormalPages = anomalyItemMapper.findAllAbnormalPagesOfDt(dt);
        log.info("There are {} abnormal pages detected on {}", abnormalPages.size(), dt);
        // only send alerts if there are abnormal pages detected
        if (CollectionUtils.isNotEmpty(abnormalPages)) {
            // send alerts to tdq team
            sendAlertToTDQTeam(abnormalPages, dt);

            List<CustomerGroupEntity> customers = customerGroupRepo.findAll();
            List<String> ccList = List.of();
            // only send alerts when the customer list is not empty
            if (!customers.isEmpty()){
                LambdaQueryWrapper<EmailConfigEntity> lambdaQuery = Wrappers.lambdaQuery();
                lambdaQuery.eq(EmailConfigEntity::getName, "Page Profiling Abnormal Alert To Customer");
                EmailConfigEntity emailConfigEntity = emailConfigEntityMapper.selectOne(lambdaQuery);
                ccList = Arrays.stream(emailConfigEntity.getCc().split(",")).map(String::strip).toList();
            }
            for (CustomerGroupEntity customer : customers) {
                final String to = customer.getEmailDl();
                final Long customerId = customer.getId();
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

                // get abnormal pages for customer
                List<AnomalyItemEntity> abnormalPagesOfCustomer = getAbnormalPagesOfCustomer(abnormalPages, customerId);
                log.info("There are {} abnormal pages detected for {} team on {}", abnormalPagesOfCustomer.size(), customer.getName(), dt);
                // only send alerts if an abnormal pages list is not empty
                if (CollectionUtils.isNotEmpty(abnormalPagesOfCustomer)) {
                    PageAlertDto<PageAlertItemDto> pageAlertDto = new PageAlertDto<>();
                    pageAlertDto.setDt(dt.toString());
                    pageAlertDto.setGroupName(customer.getName());

                    // abnormal pageIds
                    List<Integer> pageIds = abnormalPagesOfCustomer.stream()
                            .map(p -> Integer.parseInt(p.getRefId()))
                            .collect(Collectors.toList());

                    List<PageLookUpInfo> pageLkpList = pageLookUpInfoRepo.findAllByPageIdIn(pageIds);

                    List<PageAlertItemDto> pageAlertItemDtoList = abnormalPagesOfCustomer.stream().map(p -> {
                        Integer pageId = Integer.parseInt(p.getRefId());
                        PageAlertItemDto dto = new PageAlertItemDto();
                        dto.setPageId(pageId);
                        dto.setVolume(p.getValue().longValue());

                        Long avgLast7D = nonBotPageCountRepo.findAvgByPageIdAndBetweenDt(pageId,
                                dt.minusDays(7).format(formatter),
                                dt.minusDays(1).format(formatter));
                        dto.setAvgLast7D(avgLast7D);

                        PageLookUpInfo pageLookUpInfo = findPageLkpInList(pageLkpList, pageId);

                        if (pageLookUpInfo != null) {
                            dto.setPageName(pageLookUpInfo.getPageName());
                            dto.setPageFmly(pageLookUpInfo.getPageFamily());
                            dto.setIFrame(pageLookUpInfo.getIframe());
                        }
                        return dto;
                    }).collect(Collectors.toList());

                    pageAlertDto.setPages(pageAlertItemDtoList);

                    Context context = new Context();
                    context.setVariable("pageAlert", pageAlertDto);

                    emailService.sendHtmlEmail("page-profiling-alert",
                            context, emailSubject, Lists.newArrayList(to), ccList);
                }
            }
        }
    }

    private PageLookUpInfo findPageLkpInList(List<PageLookUpInfo> list, Integer pageId) {
        if (CollectionUtils.isEmpty(list)) return null;
        for (PageLookUpInfo pageLookUpInfo : list) {
            if (pageLookUpInfo.getPageId().equals(pageId)) {
                return pageLookUpInfo;
            }
        }
        return null;
    }

    private void sendAlertToTDQTeam(List<AnomalyItemEntity> abnormalPages, LocalDate dt) throws Exception {
        log.info("Sending alert email for abnormal pages to TDQ Admin");
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        PageAlertDto<PageAlertItemDto> pageAlertDto = new PageAlertDto<>();
        pageAlertDto.setDt(dt.toString());
        pageAlertDto.setGroupName("TDQ");

        // abnormal pageIds
        List<Integer> pageIds = abnormalPages.stream()
                .map(p -> Integer.parseInt(p.getRefId()))
                .collect(Collectors.toList());

        List<PageLookUpInfo> pageLkpList = pageLookUpInfoRepo.findAllByPageIdIn(pageIds);

        List<PageAlertItemDto> pageAlertItemDtoList = abnormalPages.stream().map(p -> {
            Integer pageId = Integer.parseInt(p.getRefId());
            PageAlertItemDto dto = new PageAlertItemDto();
            dto.setPageId(pageId);
            dto.setVolume(p.getValue().longValue());

            Long avgLast7D = nonBotPageCountRepo.findAvgByPageIdAndBetweenDt(pageId,
                    dt.minusDays(7).format(formatter),
                    dt.minusDays(1).format(formatter));
            dto.setAvgLast7D(avgLast7D);

            PageLookUpInfo pageLookUpInfo = findPageLkpInList(pageLkpList, pageId);

            if (pageLookUpInfo != null) {
                dto.setPageName(pageLookUpInfo.getPageName());
                dto.setPageFmly(pageLookUpInfo.getPageFamily());
                dto.setIFrame(pageLookUpInfo.getIframe());
            }
            return dto;
        }).collect(Collectors.toList());

        pageAlertDto.setPages(pageAlertItemDtoList);

        Context context = new Context();
        context.setVariable("pageAlert", pageAlertDto);

        LambdaQueryWrapper<EmailConfigEntity> lambdaQuery = Wrappers.lambdaQuery();
        lambdaQuery.eq(EmailConfigEntity::getName, "Page Profiling Abnormal Alert To TDQ");
        EmailConfigEntity emailConfigEntity = emailConfigEntityMapper.selectOne(lambdaQuery);
        List<String> to = Arrays.stream(emailConfigEntity.getRecipient().split(",")).map(String::strip).toList();
        List<String> cc = null;
        if (emailConfigEntity.getCc() != null) {
            cc = Arrays.stream(emailConfigEntity.getCc().split(",")).toList();
        }
        emailService.sendHtmlEmail("page-profiling-alert", context, emailSubject, to, cc);
    }

    private List<AnomalyItemEntity> getAbnormalPagesOfCustomer(List<AnomalyItemEntity> abnormalPages, Long customerId) {
        List<ProfilingCustomerPageRel> pageGroups = profilingCustomerPageRelRepo.findAllByCustomerId(customerId);
        if (CollectionUtils.isEmpty(pageGroups)) {
            return Collections.emptyList();
        }

        List<Integer> pageIds = pageGroups.stream()
                .map(ProfilingCustomerPageRel::getPageId)
                .toList();

        return abnormalPages.stream()
                .filter(p -> pageIds.contains(Integer.parseInt(p.getRefId())))
                .collect(Collectors.toList());
    }

    @Override
    public void multipleUidAlert(LocalDateTime dateTime) throws Exception {

    }

    @Override
    public void checkDailyData() throws Exception {

    }

    @Override
    public void alertForEPTeamAndFamx(LocalDateTime localDateTime) throws Exception {
        LocalDate localDate = localDateTime.toLocalDate();

        LegacyItemDTO legacyItemDTO_1 = new LegacyItemDTO(46, "Guids with 1 uid percent (All events)", "guids with 1 user percent of all events", 0.04, ">", "guid_cnt_all_uid_is_1", "%");
        LegacyItemDTO legacyItemDTO_2 = new LegacyItemDTO(47, "Guids with >= 2 uids percent (All events)", "guids with 2 or more users percent of all events", 0.02, ">", "guid_cnt_all_uid_gte_2", "%");
        LegacyItemDTO legacyItemDTO_3 = new LegacyItemDTO(51, "Guids with 0 uid (Valid events)", "Num of guids with 0 user for non-redirected and non-iframe events", 0.20, ">", "guid_cnt_valid_uid_is_0", null);
        LegacyItemDTO legacyItemDTO_4 = new LegacyItemDTO(52, "Guids with 1 uid (Valid events)", "Num of guids with 1 user for non-redirected and non-iframe events", 0.20, ">", "guid_cnt_valid_uid_is_1", null);
        LegacyItemDTO legacyItemDTO_5 = new LegacyItemDTO(53, "Guids with >= 2 uids (Valid events)", "Num of guids with 2 or more users for non-redirected and non-iframe events", 0.20, ">", "guid_cnt_valid_uid_gte_2", null);
        LegacyItemDTO legacyItemDTO_6 = new LegacyItemDTO(57, "Page level multiple uid events count", "events with multiple uids on page id level", 50000.00, ">", "multi_uid_events_cnt", null);
        LegacyItemDTO legacyItemDTO_7 = new LegacyItemDTO(60, "Sessions percent with >=2 uids", "Sessions percent with >=2 uids", 1.00, ">", "session_rate_valid_uid_gte_2", null);
        List<LegacyItemDTO> list = List.of(legacyItemDTO_1, legacyItemDTO_2, legacyItemDTO_3, legacyItemDTO_4, legacyItemDTO_5, legacyItemDTO_6, legacyItemDTO_7);

        List<Integer> allEventMetricIds = Arrays.asList(46, 47,  57, 60);
        List<Integer> validEventMetricIds = Arrays.asList(51, 52, 53);

        PageAlertDto<MultipleUidDTO> pageAlertDto = new PageAlertDto<>();
        pageAlertDto.setDt(localDate.toString());
        pageAlertDto.setGroupName(" ");
        pageAlertDto.setPages(new ArrayList<>());
        pageAlertDto.setList(new ArrayList<>());

        detectAbnormal(localDate, list, allEventMetricIds, validEventMetricIds, pageAlertDto);

        if (CollectionUtils.isEmpty(pageAlertDto.getPages()) && CollectionUtils.isEmpty(pageAlertDto.getList())){
            return;
        }
        pageAlertDto.setCnt(pageAlertDto.getPages().size() + pageAlertDto.getList().size());

        Context context = new Context();
        context.setVariable("alert", pageAlertDto);

        //String content = templateEngine.process("guid-x-uid-alert", context);
//        List<String> toEmailList = new ArrayList<>();
//        toEmailList.add("fangpli@ebay.com");
//        toEmailList.add("DL-eBay-Tracking-Data-Quality@ebay.com");
//        toEmailList.add("jingjzhang@ebay.com");
//        toEmailList.add("fechen@ebay.com");
//        toEmailList.add("yzou1@ebay.com");
//        toEmailList.add("hchen6@ebay.com");
//        List<String> ccEmailList = List.of("DL-eBay-Marketing-Support@ebay.com");

        emailService.sendHtmlEmail("guid-x-uid-alert", context, "EP and famx Alert");
    }

    private void detectAbnormal(LocalDate localDate, List<LegacyItemDTO> list, List<Integer> allEventMetricIds, List<Integer> validEventMetricIds, PageAlertDto<MultipleUidDTO> pageAlertDto) {
        allEventMetricIds.forEach(id -> {
            Optional<LegacyItemDTO> optional = list.stream().filter(legacyItemDTO -> legacyItemDTO.getId().equals(id)).findFirst();
            if (optional.isPresent()){
                LegacyItemDTO legacyItemDTO = optional.get();
                List<MetricDoc> metricDocList = metricService.getDailyMetrics(localDate, legacyItemDTO.getMetricKey());
                if (!metricDocList.isEmpty()) {
                    Optional<MetricDoc> docOptional = metricDocList.stream().filter(metricDoc1 -> metricDoc1.getDimension() == null).findFirst();
                    if (docOptional.isPresent()) {
                        MetricDoc metricDoc = docOptional.get();
                        double v = metricDoc.getValue().doubleValue();
                        double realV = v;
                        if (legacyItemDTO.getUnit() != null && "%".equals(legacyItemDTO.getUnit())) {
                            realV = v / 100;
                        }
                        if (checkAlert(legacyItemDTO, realV)){
                            MultipleUidDTO multipleUidDTO = new MultipleUidDTO();
                            multipleUidDTO.setMetricName(legacyItemDTO.getName());
                            multipleUidDTO.setDescription(legacyItemDTO.getDescription());
                            multipleUidDTO.setValueOfToday(realV);
                            multipleUidDTO.setThreshold(legacyItemDTO.getThreshold());
                            multipleUidDTO.setUnit(legacyItemDTO.getUnit() == null ? "-" : legacyItemDTO.getUnit());
                            pageAlertDto.getList().add(multipleUidDTO);
                        }
                    }
                }
            }
        });

        LocalDate sevenAgo = localDate.minusDays(7);
        validEventMetricIds.forEach(id -> {
            Optional<LegacyItemDTO> optional = list.stream().filter(legacyItemDTO -> legacyItemDTO.getId().equals(id)).findFirst();
            if (optional.isPresent()){
                LegacyItemDTO legacyItemDTO = optional.get();
                List<MetricDoc> metricDocList = metricService.getDailyMetrics(localDate, legacyItemDTO.getMetricKey());
                List<MetricDoc> metricDocList2 = metricService.getDailyMetrics(sevenAgo, legacyItemDTO.getMetricKey());
                if (!metricDocList.isEmpty() && !metricDocList2.isEmpty()) {
                    MetricDoc metricDoc = metricDocList.get(0);
                    double value = metricDoc.getValue().doubleValue();

                    MetricDoc metricDoc1 = metricDocList2.get(0);
                    double value1 = metricDoc1.getValue().doubleValue();

                    MultipleUidDTO multipleUidDTO = new MultipleUidDTO();
                    multipleUidDTO.setMetricName(legacyItemDTO.getName());
                    multipleUidDTO.setDescription(legacyItemDTO.getDescription());
                    multipleUidDTO.setValueOfToday(value);
                    multipleUidDTO.setValueOfYesterday(value1);
                    if (value1 != 0.0){
                        double a = value - value1;
                        if (a > 0){
                            multipleUidDTO.setIncreaseType("up");
                        } else if (a < 0){
                            multipleUidDTO.setIncreaseType("down");
                        } else {
                            multipleUidDTO.setIncreaseType("-");
                        }
                        double v = Math.abs(a) / value1;
                        if (v > legacyItemDTO.getThreshold()){
                            multipleUidDTO.setRate(v);
                            pageAlertDto.getPages().add(multipleUidDTO);
                        }
                    }
                }
            }
        });
    }

    private boolean checkAlert(LegacyItemDTO legacyItemDTO, double v) {
        Double threshold = legacyItemDTO.getThreshold();
        String thresholdType = legacyItemDTO.getThresholdType();
        return switch (thresholdType) {
            case "<=" -> v <= threshold;
            case "<" -> v < threshold;
            case ">=" -> v >= threshold;
            case ">" -> v > threshold;
            default -> false;
        };
    }


    @Override
    public void adsClickFraud(LocalDate date) throws Exception {
        List<MetricDoc> dailyMetrics = metricService.getDailyMetrics(date, "ads_click_fruad_detection");
        if (org.springframework.util.CollectionUtils.isEmpty(dailyMetrics)){
            return;
        }
        MetricDoc metricDoc = dailyMetrics.get(0);
        BigDecimal value = metricDoc.getValue();
        double v = value.doubleValue() / 100, threshold = 0.0002;
        if (threshold >= v){
            return;
        }
        AdsClickFraudDTO adsClickFraudDTO = new AdsClickFraudDTO();
        adsClickFraudDTO.setDt(date.toString());
        adsClickFraudDTO.setGroupName("Ads");
        adsClickFraudDTO.setValue(v);
        adsClickFraudDTO.setThreshold(threshold);

        Context context = new Context();
        context.setVariable("adsAlert", adsClickFraudDTO);

        LambdaQueryWrapper<EmailConfigEntity> lambdaQuery = Wrappers.lambdaQuery();
        lambdaQuery.eq(EmailConfigEntity::getName, "Ads Click fraud detection");
        EmailConfigEntity emailConfigEntity = emailConfigEntityMapper.selectOne(lambdaQuery);
        List<String> to = Arrays.stream(emailConfigEntity.getRecipient().split(",")).map(String::strip).toList();
        List<String> cc = null;
        if (emailConfigEntity.getCc() != null) {
            cc = Arrays.stream(emailConfigEntity.getCc().split(",")).toList();
        }
        String subject = Objects.requireNonNullElse(emailConfigEntity.getSubject(), "TDQ Alerts - Ads vs Soj event_timestamp gap");

        emailService.sendHtmlEmail("alert-ads-click-event-timestamp-gap", context, subject, to, cc);
    }
}
