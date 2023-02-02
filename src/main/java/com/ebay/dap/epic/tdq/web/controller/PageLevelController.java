package com.ebay.dap.epic.tdq.web.controller;

import com.ebay.dap.epic.tdq.data.entity.CustomerGroupEntity;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.*;
import com.ebay.dap.epic.tdq.service.PageProfilingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@Api(value = "PageLevelController", tags = {"page Level"})
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

    @ApiOperation(value = "refresh page Infos", notes = "refresh page information")
    @GetMapping("refresh")
    public void refresh() {
        pageLevelManager.cleanUp();
    }

    @ApiOperation(value = "Doughnut Page Family", notes = "get Doughnut Page Family")
    @GetMapping(path = "getDoughnutPageFamily")
    @ApiImplicitParam(paramType = "query", name = "date", dataType = "String", value = "yyyyMMdd")
    public List<PageFamilyItemVO> getDoughnutPageFamily(String date) {
        LocalDate localDate = null;
        if (Objects.nonNull(date)) {
            localDate = LocalDate.parse(date, yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        return pageLevelManager.getPageFamilyItemVO(localDate, 10);
    }

    @ApiOperation(value = "Doughnut Page Family for PA", notes = "get Doughnut Page Family for pA")
    @GetMapping(path = "getDoughnutPageFamilyForPA")
    @ApiImplicitParam(paramType = "query", name = "date", dataType = "String", value = "yyyyMMdd")
    public List<PageFamilyItemVO> getDoughnutPageFamilyForPA(String date) {
        LocalDate localDate = null;
        if (Objects.nonNull(date)) {
            localDate = LocalDate.parse(date, yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        //return pageLevelManager.getPageFamilyItemVOForPA(localDate);
        return pageLevelManager.getPageFamilyItemVOForCustomer(localDate);
    }

    @ApiOperation(value = "Page family card info", notes = "getCardsByPageIds")
    @PostMapping(path = "/getCardsByPageIds")
    @ApiImplicitParam(paramType = "body", dataTypeClass = PageFamilyCardQueryVo.class)
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

    @ApiOperation(value = "Page family table info", notes = "getPageFamilyTableByPageIds")
    @PostMapping(path = "/getPageFamilyTableByPageIds")
    @ApiImplicitParam(paramType = "body", dataTypeClass = PageFamilyCardQueryVo.class)
    public List<PageItemVO> getPageFamilyTableByPageIds(@RequestBody PageFamilyCardQueryVo pageFamilyCardQueryVo) {
        log.info("param:{}", pageFamilyCardQueryVo);
        LocalDate localDate = null;
        if (Objects.nonNull(pageFamilyCardQueryVo.getDate())) {
            localDate = LocalDate.parse(pageFamilyCardQueryVo.getDate(), yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        return pageLevelManager.getPageFamilyTableDataByPageIds(pageFamilyCardQueryVo.getPageFamilyName(), pageFamilyCardQueryVo.getPageIds(), localDate);
    }

    @ApiOperation(value = "Page Basic info", notes = "Page Basic info")
    @GetMapping(path = "detail/{pageId}/basicInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "pageId", dataType = "int", required = true),
            @ApiImplicitParam(paramType = "query", name = "date", dataType = "String", example = "yyyyMMdd", required = true)
    })
    public PageBasicInfoVO getBasicInfoOfPage(@PathVariable Integer pageId, String date) {
        LocalDate localDate = null;
        if (Objects.nonNull(date)) {
            localDate = LocalDate.parse(date, yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        return pageLevelManager.getBasicInfoOfPageDetail(pageId, localDate, 30);
    }

    @ApiOperation(value = "Page Traffic Data", notes = "Page Traffic Data")
    @GetMapping(path = "detail/{pageId}/traffic", produces = APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "pageId", dataType = "int", required = true),
            @ApiImplicitParam(paramType = "query", name = "date", dataType = "String", example = "yyyyMMdd", required = true),
            @ApiImplicitParam(paramType = "query", name = "offsetDays", dataType = "int", required = true)
    })
    public TrafficOfPageDetailVO getTrafficOfPageDetail(@PathVariable Integer pageId, String date, Integer offsetDays) {
        LocalDate localDate = null;
        if (Objects.nonNull(date)) {
            localDate = LocalDate.parse(date, yyyyMMdd);
        }
        localDate = adjustDate(localDate);
        return pageLevelManager.getTrafficOfPageDetail(pageId, localDate, offsetDays);
    }

    @ApiOperation(value = "Page Usage Data", notes = "Page Usage Data")
    @GetMapping(path = "detail/{pageId}/usage")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "pageId", dataType = "int", required = true),
            @ApiImplicitParam(paramType = "query", name = "date", dataType = "String", example = "yyyyMMdd", required = true),
            @ApiImplicitParam(paramType = "query", name = "offsetDays", dataType = "int", required = true)
    })
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

    @ApiOperation(value = "update Product analyze Page Family Config", notes = "updatePAPageFamilyConfig")
    @PostMapping(path = "/updatePAPageFamilyConfig")
    @ApiImplicitParam(paramType = "body", dataTypeClass = ProductAnalyzeVO.class)
    public String updatePAPageFamilyConfig(@RequestBody ProductAnalyzeVO productAnalyzeVO) {
        try {
            pageLevelManager.updatePAPageFamilyConfig(productAnalyzeVO);
            return "Success";
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return "Error";
        }
    }

    @ApiOperation(value = "update Customer Page Group Info", notes = "updateCustomerPageGroupInfo")
    @PostMapping(path = "/updateCustomerPageGroupInfo")
    @ApiImplicitParam(paramType = "body", dataTypeClass = PageGroupVO.class)
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