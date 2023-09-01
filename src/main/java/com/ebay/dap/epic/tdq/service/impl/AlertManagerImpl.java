package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.data.dto.PageAlertDto;
import com.ebay.dap.epic.tdq.data.dto.PageAlertItemDto;
import com.ebay.dap.epic.tdq.data.entity.AnomalyItemEntity;
import com.ebay.dap.epic.tdq.data.entity.CustomerGroupEntity;
import com.ebay.dap.epic.tdq.data.entity.PageLookUpInfo;
import com.ebay.dap.epic.tdq.data.entity.ProfilingCustomerPageRel;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.AnomalyItemMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.CustomerGroupMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.NonBotPageCountMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.PageLookUpInfoMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.ProfilingCustomerPageRelMapper;
import com.ebay.dap.epic.tdq.service.AlertManager;
import com.ebay.dap.epic.tdq.service.EmailService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AlertManagerImpl implements AlertManager {

    @Autowired
    private TemplateEngine templateEngine;

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

    @Override
    public void sendPageProfilingAlertEmail(LocalDate dt) throws Exception {
        List<AnomalyItemEntity> abnormalPages = anomalyItemMapper.findAllAbnormalPagesOfDt(dt);
        log.info("There are {} abnormal pages detected on {}", abnormalPages.size(), dt);
        // only send alerts if there are abnormal pages detected
        if (CollectionUtils.isNotEmpty(abnormalPages)) {
            // send alerts to tdq team
            sendAlertToTDQTeam(abnormalPages, dt);

            List<CustomerGroupEntity> customers = customerGroupRepo.findAll();

            // only send alerts when the customer list is not empty
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

                    String content = templateEngine.process("page-profiling-alert", context);

                    emailService.sendHtmlEmail(content,
                            Lists.newArrayList(to),
                            ccList,
                            emailSubject);
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

        String content = templateEngine.process("page-profiling-alert", context);

        // send page profiling alerts to DL-eBay-Tracking-Data-Quality-Alert-Notify
        final List<String> to = List.of(
                "DL-eBay-Tracking-Data-Quality@ebay.com"
        );
        emailService.sendHtmlEmail(content, to, List.of("fangpli@ebay.com", "yxiao6@ebay.com"), emailSubject);
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

    }
}
