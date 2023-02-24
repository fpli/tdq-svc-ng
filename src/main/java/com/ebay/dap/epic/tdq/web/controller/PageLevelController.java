package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.entity.CustomerGroupEntity;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.PageBasicInfoVO;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.PageCardItemVO;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.PageFamilyCardQueryVo;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.PageFamilyItemVO;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.PageGroupVO;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.PageItemVO;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.ProductAnalyzeVO;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.TrafficOfPageDetailVO;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.UsageOfDayVO;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.UsageOfPageDetailVO;
import com.ebay.dap.epic.tdq.service.PageProfilingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/api/pageLevel")
@Tag(name = "Page Profiling API", description = "Page profiling related API")
public class PageLevelController {

    @Autowired
    private PageProfilingService pageLevelManager;

    private final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

    private LocalDate adjustDate(LocalDate inputDate) {
        log.info("{}", inputDate);
        if (LocalDateTime.now().getHour() < 10) {
            return LocalDate.now().minusDays(2);
        }
        return LocalDate.now().minusDays(1);
    }

    @Operation(summary = "refresh page Infos")
    @GetMapping("refresh")
    public void refresh() {
        pageLevelManager.cleanUp();
    }

    @Operation(summary = "Doughnut Page Family")
    @GetMapping(path = "getDoughnutPageFamily")
    public List<PageFamilyItemVO> getDoughnutPageFamily(String date) {
        LocalDate localDate = null;
        if (Objects.nonNull(date)) {
            localDate = LocalDate.parse(date, yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        return pageLevelManager.getPageFamilyItemVO(localDate, 10);
    }

    @Operation(summary = "Doughnut Page Family for PA")
    @GetMapping(path = "getDoughnutPageFamilyForPA")
    public List<PageFamilyItemVO> getDoughnutPageFamilyForPA(String date) {
        LocalDate localDate = null;
        if (Objects.nonNull(date)) {
            localDate = LocalDate.parse(date, yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        //return pageLevelManager.getPageFamilyItemVOForPA(localDate);
        return pageLevelManager.getPageFamilyItemVOForCustomer(localDate);
    }

    @Operation(summary = "Page family card info")
    @PostMapping(path = "/getCardsByPageIds")
    public List<PageCardItemVO> getCardsByPageIds(@RequestBody PageFamilyCardQueryVo pageFamilyCardQueryVo) throws ExecutionException, InterruptedException {
        log.info("param:{}", pageFamilyCardQueryVo);
        LocalDate localDate = null;
        if (Objects.nonNull(pageFamilyCardQueryVo.getDate())) {
            localDate = LocalDate.parse(pageFamilyCardQueryVo.getDate(), yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        if (Objects.nonNull(pageFamilyCardQueryVo.getPageFamilyName())) {
            return pageLevelManager.getPageCardItemVO(pageFamilyCardQueryVo.getPageFamilyName(), pageFamilyCardQueryVo.getPageIds(), localDate);
        }
        return pageLevelManager.getPageCardItemVO(pageFamilyCardQueryVo.getPageFamilyNameList(), pageFamilyCardQueryVo.getPageIds(), localDate);
    }

    @Operation(summary = "Page family table info")
    @PostMapping(path = "/getPageFamilyTableByPageIds")
    public List<PageItemVO> getPageFamilyTableByPageIds(@RequestBody PageFamilyCardQueryVo pageFamilyCardQueryVo) {
        log.info("param:{}", pageFamilyCardQueryVo);
        LocalDate localDate = null;
        if (Objects.nonNull(pageFamilyCardQueryVo.getDate())) {
            localDate = LocalDate.parse(pageFamilyCardQueryVo.getDate(), yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        return pageLevelManager.getPageFamilyTableDataByPageIds(pageFamilyCardQueryVo.getPageFamilyName(), pageFamilyCardQueryVo.getPageIds(), localDate);
    }

    @Operation(summary = "Page Basic info")
    @GetMapping(path = "detail/{pageId}/basicInfo")
    public PageBasicInfoVO getBasicInfoOfPage(@PathVariable Integer pageId, String date) {
        LocalDate localDate = null;
        if (Objects.nonNull(date)) {
            localDate = LocalDate.parse(date, yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        return pageLevelManager.getBasicInfoOfPageDetail(pageId, localDate, 30);
    }

    @Operation(summary = "Page Traffic Data")
    @GetMapping(path = "detail/{pageId}/traffic", produces = APPLICATION_JSON_VALUE)
    public TrafficOfPageDetailVO getTrafficOfPageDetail(@PathVariable Integer pageId, String date, Integer offsetDays) {
        LocalDate localDate = null;
        if (Objects.nonNull(date)) {
            localDate = LocalDate.parse(date, yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        return pageLevelManager.getTrafficOfPageDetail(pageId, localDate, offsetDays);
    }

    @Operation(summary = "Page Usage Data")
    @GetMapping(path = "detail/{pageId}/usage")
    public UsageOfPageDetailVO getUsageOfPageDetail(@PathVariable Integer pageId, String date, Integer offsetDays) {
        UsageOfPageDetailVO usageOfPageDetailVO = new UsageOfPageDetailVO();
        usageOfPageDetailVO.setPageId(pageId);
        LocalDate localDate = null;
        if (Objects.nonNull(date)) {
            localDate = LocalDate.parse(date, yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        List<UsageOfDayVO> usageOfDayVOList = pageLevelManager.getUsageOfPageDetail(pageId, localDate, offsetDays);
        usageOfPageDetailVO.setUsage(usageOfDayVOList);
        return usageOfPageDetailVO;
    }

    @Operation(summary = "update Product analyze Page Family Config")
    @PostMapping(path = "/updatePAPageFamilyConfig")
    public String updatePAPageFamilyConfig(@RequestBody ProductAnalyzeVO productAnalyzeVO) {
        try {
            pageLevelManager.updatePAPageFamilyConfig(productAnalyzeVO);
            return "Success";
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return "Error";
        }
    }

    @Operation(summary = "update Customer Page Group Info")
    @PostMapping(path = "/updateCustomerPageGroupInfo")
    public String updateCustomerPageGroupInfo(@RequestHeader("Authorization") String authorization, @RequestBody PageGroupVO pageGroupVO) {
        try {
            CustomerGroupEntity customerGroupEntity = pageLevelManager.checkAuthorization(authorization);
            if (null == customerGroupEntity) {
                return "Authorization Error";
            } else {
                pageLevelManager.updateCustomerPageGroupInfo(customerGroupEntity, pageGroupVO);
                return "Success";
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return "Error";
        }
    }
}