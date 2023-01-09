package com.ebay.dap.epic.tdq.service;

import com.ebay.dap.epic.tdq.data.entity.CustomerGroupEntity;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface PageProfilingService {
    void cleanUp();

    List<PageFamilyItemVO> getPageFamilyItemVO(LocalDate localDate, int i);

    List<PageFamilyItemVO> getPageFamilyItemVOForCustomer(LocalDate localDate);

    List<PageCardItemVO> getPageCardItemVO(String pageFamilyName, List<Integer> pageIds, LocalDate localDate) throws ExecutionException, InterruptedException;

    List<PageCardItemVO> getPageCardItemVO(List<String> pageFamilyNameList, List<Integer> pageIds, LocalDate localDate) throws ExecutionException, InterruptedException;

    List<PageItemVO> getPageFamilyTableDataByPageIds(String pageFamilyName, List<Integer> pageIds, LocalDate localDate);

    PageBasicInfoVO getBasicInfoOfPageDetail(Integer pageId, LocalDate localDate, Integer offsetDays);

    TrafficOfPageDetailVO getTrafficOfPageDetail(Integer pageId, LocalDate localDate, Integer offsetDays);

    List<UsageOfDayVO> getUsageOfPageDetail(Integer pageId, LocalDate localDate, Integer offsetDays);

    void updatePAPageFamilyConfig(ProductAnalyzeVO productAnalyzeVO) throws JsonProcessingException;

    CustomerGroupEntity checkAuthorization(String authorization) throws Exception;

    long updateCustomerPageGroupInfo(CustomerGroupEntity customerGroupEntity, PageGroupVO pageGroupVO);
}
