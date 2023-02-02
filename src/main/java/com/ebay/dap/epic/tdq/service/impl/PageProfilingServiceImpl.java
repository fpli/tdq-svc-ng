package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.data.entity.*;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.*;
import com.ebay.dap.epic.tdq.data.vo.pageLevel.*;
import com.ebay.dap.epic.tdq.service.PageProfilingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PageProfilingServiceImpl implements PageProfilingService {

    @Autowired
    private NonBotPageCountMapper nonBotPageCountRepo;

    @Autowired
    private BotPageCountMapper botPageCountRepo;

    @Autowired
    private PageLookUpInfoMapper pageLookUpInfoRepo;

    @Autowired
    private PageUsageMapper pageUsageRepo;

    @Autowired
    private AnomalyItemMapper anomalyItemRepository;

    @Autowired
    private ProfilingPageActivityStatsMapper profilingPageActivityStatsRepo;
    @Autowired
    private ProfilingUnusedPageMapper profilingUnusedPageRepo;
    @Autowired
    private ProfilingPageFamilyConfigInfoMapper pageFamilyConfigInfoRepo;
    @Autowired
    private ProfilingCustomerPageRelMapper profilingCustomerPageRelRepo;
    @Autowired
    private CustomerGroupMapper customerGroupInfoRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter yyyyMMdd1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ConcurrentMap<String, List<PageCardItemVO>> cache = new ConcurrentHashMap<>();
    private final ConcurrentMap<List<String>, List<PageCardItemVO>> cacheOfPA = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<PageItemVO>> cache2 = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<Integer>> pageFamilyMap = new ConcurrentHashMap<>();

    @Override
    @Scheduled(cron = "0 0 11 * * *")
    @PostConstruct
    public void cleanUp() {
        cache.clear();
        cacheOfPA.clear();
        cache2.clear();
        pageFamilyMap.clear();
        final LocalDate date;
        if (LocalDateTime.now().getHour() < 10) {
            date = LocalDate.now().minusDays(2);
        } else {
            date = LocalDate.now().minusDays(1);
        }
        try {
            List<PageFamilyItemVO> pageFamilyItemVOS = getPageFamilyItemVO(date, 10);
            List<Integer> pageIds = new ArrayList<>();
            pageFamilyItemVOS.forEach(pageFamilyItemVO -> pageFamilyMap.put(pageFamilyItemVO.getPageFamilyName(), pageFamilyItemVO.getPageIds()));
            log.info("pageFamilyMap:{}", pageFamilyMap);
            pageFamilyMap.values().forEach(pageIds::addAll);
            pageFamilyMap.put("all", pageIds);
            CompletableFuture.runAsync(() -> {
                pageFamilyMap.forEach((pageFamilyName, tempPageIds) -> {
                    try {
                        getPageCardItemVO(pageFamilyName, tempPageIds, date);
                    } catch (ExecutionException | InterruptedException e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
            });
        } catch (Exception e) {
            log.error("e: {}", e.getMessage());
        }
        String dt = yyyyMMdd.format(date.minusDays(100));
        nonBotPageCountRepo.deleteByDtLessThan(dt);
        botPageCountRepo.deleteByDtLessThan(dt);
    }

    @Override
    public List<PageFamilyItemVO> getPageFamilyItemVO(LocalDate localDate, int topN) {
        List<PageFamilyItemVO> pageFamilyItemVOList = new ArrayList<>();

        List<Long> totals = new ArrayList<>();
        final List<String> dts = Collections.singletonList(yyyyMMdd.format(localDate));
        final List<PageLookUpInfo> pageLookUpInfoList = pageLookUpInfoRepo.findAll();
        final Map<Boolean, List<PageLookUpInfo>> booleanListMap = pageLookUpInfoList.stream().collect(Collectors.partitioningBy(pageLookUpInfo -> pageLookUpInfo.getPageFamily() == null));
        final List<PageLookUpInfo> nonePageLookUpInfos = booleanListMap.get(true);
        final List<PageLookUpInfo> pageLookUpInfos = booleanListMap.get(false);

        final List<Integer> nonePageIds = nonePageLookUpInfos.stream().map(PageLookUpInfo::getPageId).collect(Collectors.toList());
        final List<BotPageCountEntity> botPageCountEntityList = botPageCountRepo.findAllByPageIdInAndDtIn(nonePageIds, dts);
        final List<NonBotPageCountEntity> nonBotPageCountEntityList = nonBotPageCountRepo.findAllByPageIdInAndDtIn(nonePageIds, dts);
        PageFamilyItemVO pageFamilyItemVO = new PageFamilyItemVO();
        pageFamilyItemVO.setPageFamilyName("Uncategorized");
        pageFamilyItemVO.setPageIds(nonePageIds);
        long NULLTotal = nonBotPageCountEntityList.stream().map(NonBotPageCountEntity::getTotal).reduce(0L, Long::sum) + botPageCountEntityList.stream().map(BotPageCountEntity::getTotal).reduce(0L, Long::sum);
        pageFamilyItemVO.setCnt(NULLTotal);
        pageFamilyItemVOList.add(pageFamilyItemVO);
        totals.add(NULLTotal);
        final Map<String, List<Integer>> map = pageLookUpInfos.stream().collect(Collectors.groupingBy(PageLookUpInfo::getPageFamily, Collectors.mapping(PageLookUpInfo::getPageId, Collectors.toList())));
        map.forEach((pageFamily, pageIds) -> {
            PageFamilyItemVO pageFamilyItem = new PageFamilyItemVO();
            pageFamilyItemVOList.add(pageFamilyItem);
            pageFamilyItem.setPageFamilyName(pageFamily);
            pageIds.sort(Integer::compareTo);
            pageFamilyItem.setPageIds(pageIds);
            final List<BotPageCountEntity> botPageCountEntities = botPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
            final List<NonBotPageCountEntity> nonBotPageCountEntities = nonBotPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
            final long total1 = nonBotPageCountEntities.stream().map(NonBotPageCountEntity::getTotal).reduce(0L, Long::sum) + botPageCountEntities.stream().map(BotPageCountEntity::getTotal).reduce(0L, Long::sum);
            pageFamilyItem.setCnt(total1);
            totals.add(total1);
        });
        final BigDecimal total = totals.stream().map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        pageFamilyItemVOList.sort(Comparator.comparingLong(PageFamilyItemVO::getCnt).reversed());
        List<PageFamilyItemVO> pageFamilyItemVOList2 = pageFamilyItemVOList;
        if (pageFamilyItemVOList.size() > topN) {
            pageFamilyItemVOList2 = new ArrayList<>(pageFamilyItemVOList.subList(0, topN));
            PageFamilyItemVO otherPageFamilyItemVO = new PageFamilyItemVO();
            pageFamilyItemVOList2.add(otherPageFamilyItemVO);
            otherPageFamilyItemVO.setPageFamilyName("Other");
            List<Integer> otherPageIds = new ArrayList<>();
            otherPageFamilyItemVO.setPageIds(otherPageIds);
            long otherCnt = 0L;
            pageFamilyItemVO.setPageIds(nonePageIds);
            PageFamilyItemVO itemVO;
            for (int i = topN; i < pageFamilyItemVOList.size(); i++) {
                itemVO = pageFamilyItemVOList.get(i);
                otherPageIds.addAll(itemVO.getPageIds());
                otherCnt = otherCnt + itemVO.getCnt();
            }
            otherPageFamilyItemVO.setCnt(otherCnt);
        }
        pageFamilyItemVOList2.forEach(pageFamilyItemVO1 -> pageFamilyItemVO1.setRate(new BigDecimal(pageFamilyItemVO1.getCnt()).multiply(BigDecimal.valueOf(100.00D)).divide(total, 2, RoundingMode.DOWN).doubleValue()));
        return pageFamilyItemVOList2;
    }

    @Override
    public List<PageFamilyItemVO> getPageFamilyItemVOForCustomer(LocalDate localDate) {
        log.info(localDate.format(yyyyMMdd1));
        List<PageFamilyItemVO> pageFamilyItemVOList = new ArrayList<>();
        List<CustomerGroupEntity> customerGroupEntities = customerGroupInfoRepo.findAll();
        if (CollectionUtils.isEmpty(customerGroupEntities)) {
            return pageFamilyItemVOList;
        }
        BigDecimal zero = BigDecimal.ZERO;
        List<BigDecimal> bigDecimalList = new ArrayList<>();
        customerGroupEntities.forEach(customerGroupEntity -> {
            String name = customerGroupEntity.getName();
            Long customerId = customerGroupEntity.getId();
            PageFamilyItemVO pageFamilyItemVO = new PageFamilyItemVO();
            pageFamilyItemVOList.add(pageFamilyItemVO);
            pageFamilyItemVO.setName(name);
            List<ProfilingCustomerPageRel> profilingPageGroups = profilingCustomerPageRelRepo.findAllByCustomerId(customerId);
            profilingPageGroups.forEach(profilingPageGroup -> pageFamilyItemVO.getPageIds().add(profilingPageGroup.getPageId()));
            List<PageLookUpInfo> pageLookUpInfos = pageLookUpInfoRepo.findAllByPageIdIn(pageFamilyItemVO.getPageIds());
            Set<String> set = new HashSet<>();
            pageLookUpInfos.forEach(pageLookUpInfo -> set.add(pageLookUpInfo.getPageFamily()));
            pageFamilyItemVO.getPageFamilyNameList().addAll(set);
            pageFamilyItemVO.setCnt((long) pageFamilyItemVO.getPageIds().size());
            bigDecimalList.add(BigDecimal.valueOf(pageFamilyItemVO.getCnt()));
            //pageFamilyItemVO.setRate(100.0);
        });
        BigDecimal total = bigDecimalList.stream().reduce(zero, BigDecimal::add);
        pageFamilyItemVOList.forEach(pageFamilyItemVO -> pageFamilyItemVO.setRate(new BigDecimal(pageFamilyItemVO.getCnt()).multiply(BigDecimal.valueOf(100.00D)).divide(total, 2, RoundingMode.DOWN).doubleValue()));
        return pageFamilyItemVOList;
    }

    @Override
    public List<PageCardItemVO> getPageCardItemVO(String pageFamilyName, List<Integer> pageIds, LocalDate localDate) throws ExecutionException, InterruptedException {
        if (Objects.nonNull(pageFamilyName) && cache.containsKey(pageFamilyName)) {
            List<PageCardItemVO> pageCardItemVOS = cache.get(pageFamilyName);
            if (!CollectionUtils.isEmpty(pageCardItemVOS)) {
                log.info("getPageCardItemVO, pageFamilyName: {}, cache: {}", pageFamilyName, cache.containsKey(pageFamilyName));
                return pageCardItemVOS;
            }
        }
        if (pageIds == null) {
            pageIds = pageFamilyMap.getOrDefault(pageFamilyName, Collections.emptyList());
        }
        List<Integer> temporaryPageIds = pageIds;
        List<PageCardItemVO> pageCardItemVOS = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getTotalActivePages(temporaryPageIds, localDate))),
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getTotalUnusedPages(pageFamilyName, localDate))),
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getTotalTraffic(temporaryPageIds, localDate))),
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getTotalNonBotTraffic(temporaryPageIds, localDate))),
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getTotalUsers(temporaryPageIds, localDate))),
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getAbnormalPages(temporaryPageIds, localDate)))
        ).get();
        List<PageCardItemVO> cardItemVOS = pageCardItemVOS.stream().sorted(Comparator.comparingInt(PageCardItemVO::getOrder)).collect(Collectors.toList());
        if (pageFamilyName != null && temporaryPageIds.size() > 0) {
            log.info("pageFamilyName: {}, date: {}, cardItemVOS:{}", pageFamilyName, localDate, cardItemVOS);
            cache.put(pageFamilyName, cardItemVOS);
        }
        return cardItemVOS;
    }

    @Override
    public List<PageCardItemVO> getPageCardItemVO(List<String> pageFamilyNameList, List<Integer> pageIds, LocalDate localDate) throws ExecutionException, InterruptedException {
        Set<List<String>> keySet = cacheOfPA.keySet();
        for (List<String> stringList : keySet) {
            if (org.apache.commons.collections.CollectionUtils.isEqualCollection(stringList, pageFamilyNameList)) {
                log.info("getPageCardItemVO of pa use cacheOfPA");
                return cacheOfPA.get(stringList);
            }
        }
        List<PageCardItemVO> pageCardItemVOS = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getTotalActivePages(pageIds, localDate))),
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getTotalUnusedPages(pageFamilyNameList, localDate))),
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getTotalTraffic(pageIds, localDate))),
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getTotalNonBotTraffic(pageIds, localDate))),
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getTotalUsers(pageIds, localDate))),
                CompletableFuture.runAsync(() -> pageCardItemVOS.add(getAbnormalPages(pageIds, localDate)))
        ).get();
        List<PageCardItemVO> cardItemVOS = pageCardItemVOS.stream().sorted(Comparator.comparingInt(PageCardItemVO::getOrder)).collect(Collectors.toList());
        cacheOfPA.put(pageFamilyNameList, cardItemVOS);
        return cardItemVOS;
    }


    @Override
    public List<PageItemVO> getPageFamilyTableDataByPageIds(String pageFamilyName, List<Integer> pageIds, LocalDate localDate) {
        if (Objects.nonNull(pageFamilyName) && cache2.containsKey(pageFamilyName)) {
            List<PageItemVO> pageItemVOList = cache2.get(pageFamilyName);
            if (!CollectionUtils.isEmpty(pageItemVOList)) {
                log.info("getPageFamilyTableDataByPageIds, pageFamilyName: {}, cache: {}", pageFamilyName, cache2.containsKey(pageFamilyName));
                return pageItemVOList;
            }
        }
        if (pageIds == null) {
            pageIds = pageFamilyMap.getOrDefault(pageFamilyName, Collections.emptyList());
        }
        List<PageLookUpInfo> pageLookUpInfos = pageLookUpInfoRepo.findAllByPageIdIn(pageIds);
        List<String> dts = Collections.singletonList(yyyyMMdd.format(localDate));
        List<NonBotPageCountEntity> nonBotPageCountEntities = nonBotPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        List<BotPageCountEntity> botPageCountEntities = botPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        ConcurrentMap<Integer, Long> nonBotMap = nonBotPageCountEntities.parallelStream().collect(Collectors.toConcurrentMap(NonBotPageCountEntity::getPageId, NonBotPageCountEntity::getTotal, (x, y) -> x));
        ConcurrentMap<Integer, Long> botMap = botPageCountEntities.parallelStream().collect(Collectors.toConcurrentMap(BotPageCountEntity::getPageId, BotPageCountEntity::getTotal, (x, y) -> x));
        List<PageUsageEntity> pageUsageEntities = pageUsageRepo.findAllByPageIdInAndDtIn(pageIds, Collections.singletonList(localDate));
        Map<Integer, Long> usageMap = pageUsageEntities.stream().collect(Collectors.groupingBy(PageUsageEntity::getPageId, Collectors.counting()));
        List<PageItemVO> pageItemVOList = new ArrayList<>(0);
        pageItemVOList.addAll(pageLookUpInfos.stream().map(pageLookUpInfo -> {
            PageItemVO pageItemVO = new PageItemVO();
            pageItemVO.setPageId(pageLookUpInfo.getPageId());
            pageItemVO.setPageName(pageLookUpInfo.getPageName());
            pageItemVO.setOwner(pageLookUpInfo.getOwner());
            pageItemVO.setIframe(pageLookUpInfo.getIframe());
            pageItemVO.setCreated(pageLookUpInfo.getDt().format(yyyyMMdd1));
            pageItemVO.setDailyVolume(nonBotMap.getOrDefault(pageLookUpInfo.getPageId(), 0L) + botMap.getOrDefault(pageLookUpInfo.getPageId(), 0L));
            pageItemVO.setAccessTotal(usageMap.getOrDefault(pageLookUpInfo.getPageId(), 0L));
            return pageItemVO;
        }).toList());
        List<PageItemVO> pageItemVOS = pageItemVOList.stream().sorted(Comparator.comparingLong(PageItemVO::getAccessTotal).reversed()).collect(Collectors.toList());
        if (pageFamilyName != null) {
            cache2.put(pageFamilyName, pageItemVOS);
        }
        return pageItemVOS;
    }

    @Override
    public PageBasicInfoVO getBasicInfoOfPageDetail(Integer pageId, LocalDate localDate, Integer offsetDays) {
        PageBasicInfoVO pageBasicInfoVO = new PageBasicInfoVO();
        List<PageLookUpInfo> pageLookUpInfoList = pageLookUpInfoRepo.findAllByPageIdIn(Collections.singletonList(pageId));
        if (CollectionUtils.isEmpty(pageLookUpInfoList)) {
            throw new NoSuchElementException(pageId + " not found.");
        }
        PageLookUpInfo pageLookUpInfo = pageLookUpInfoList.get(0);
        pageBasicInfoVO.setPageId(pageId);
        pageBasicInfoVO.setPageName(pageLookUpInfo.getPageName());
        pageBasicInfoVO.setOwner(pageLookUpInfo.getOwner());
        //pageBasicInfoVO.setOwnerEmail(pageLookUpInfo.getOwner()+"@ebay.com");
        pageBasicInfoVO.setIframe(pageLookUpInfo.getIframe());
        pageBasicInfoVO.setCreateDt(pageLookUpInfo.getDt().format(yyyyMMdd1));
        pageBasicInfoVO.setFirstSeenDt(pageLookUpInfo.getFirstSeenDt().format(yyyyMMdd1));

        long dailyTraffic = 0L;
        List<Integer> pageIds = Collections.singletonList(pageId);
        List<String> dts = new ArrayList<>();
        dts.add(yyyyMMdd.format(localDate));
        List<NonBotPageCountEntity> nonBotPageCountEntities = nonBotPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        List<BotPageCountEntity> botPageCountEntities = botPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        if (!CollectionUtils.isEmpty(nonBotPageCountEntities)) {
            dailyTraffic += nonBotPageCountEntities.get(0).getTotal();
        }
        if (!CollectionUtils.isEmpty(botPageCountEntities)) {
            dailyTraffic += botPageCountEntities.get(0).getTotal();
        }
        pageBasicInfoVO.setDailyTraffic(dailyTraffic);

        List<LocalDate> dts1 = new ArrayList<>();
        LocalDate endDate = localDate;
        int max = Math.max(30, offsetDays);
        for (int i = 0; i < max; i++) {
            dts1.add(endDate);
            endDate = endDate.minusDays(1);
        }
        List<PageUsageEntity> pageUsageEntityList = pageUsageRepo.findAllByPageIdInAndDtIn(pageIds, dts1);
        if (CollectionUtils.isEmpty(pageUsageEntityList)) {
            pageBasicInfoVO.setUsageIn30Days(0L);
        } else {
            pageBasicInfoVO.setUsageIn30Days(pageUsageEntityList.stream().map(PageUsageEntity::getAccessCount).reduce(0L, Long::sum));
        }
        return pageBasicInfoVO;
    }

    @Override
    public TrafficOfPageDetailVO getTrafficOfPageDetail(Integer pageId, LocalDate dt, Integer offsetDays) {
        TrafficOfPageDetailVO vo = new TrafficOfPageDetailVO();
        vo.setPageId(pageId);

        List<Integer> pageIds = Collections.singletonList(pageId);
        List<NonBotPageCountEntity> nonBotPageCountEntities = nonBotPageCountRepo
                .findAllByPageIdInAndDtGreaterThanEqualAndDtLessThanEqual(pageIds, dt.minusDays(offsetDays - 1).format(yyyyMMdd), dt.format(yyyyMMdd));
        List<BotPageCountEntity> botPageCountEntities = botPageCountRepo
                .findAllByPageIdInAndDtGreaterThanEqualAndDtLessThanEqual(pageIds, dt.minusDays(offsetDays - 1).format(yyyyMMdd), dt.format(yyyyMMdd));
        List<AnomalyItemEntity> pageAbnormalItems = anomalyItemRepository
                .findAllByTypeAndRefIdAndDtBetween("page", String.valueOf(pageId), dt.minusDays(offsetDays - 1), dt);
        Map<String, Long> nonBotMap = nonBotPageCountEntities.stream().collect(Collectors.toMap(NonBotPageCountEntity::getDt, NonBotPageCountEntity::getTotal, (x, y) -> x));
        Map<String, Long> botMap = botPageCountEntities.stream().collect(Collectors.toMap(BotPageCountEntity::getDt, BotPageCountEntity::getTotal, (x, y) -> x));
        List<PageAbnormalItemVO> pageAbnormalItemVOList = pageAbnormalItems.stream().map(e -> {
            PageAbnormalItemVO item = new PageAbnormalItemVO();
            item.setDt(e.getDt());
            item.setUBound(e.getUBound().longValue());
            item.setLBound(e.getLBound().longValue());
            return item;
        }).collect(Collectors.toList());

        List<TrafficOfDayVO> trafficOfDayVOList = new ArrayList<>();
        int i = offsetDays;
        while (i > 0) {
            LocalDate tmpDt = dt.minusDays(i - 1);
            TrafficOfDayVO pageFamilyTrafficVO = new TrafficOfDayVO();
            pageFamilyTrafficVO.setNonBot(nonBotMap.getOrDefault(tmpDt.format(yyyyMMdd), 0L));
            pageFamilyTrafficVO.setBot(botMap.getOrDefault(tmpDt.format(yyyyMMdd), 0L));
            pageFamilyTrafficVO.setTotal(nonBotMap.getOrDefault(tmpDt.format(yyyyMMdd), 0L) + botMap.getOrDefault(tmpDt.format(yyyyMMdd), 0L));
            pageFamilyTrafficVO.setDt(tmpDt.format(yyyyMMdd1));
            trafficOfDayVOList.add(pageFamilyTrafficVO);
            i--;
        }

        vo.setTraffic(trafficOfDayVOList);
        vo.setAbnormal(pageAbnormalItemVOList);

        return vo;
    }

    @Override
    public List<UsageOfDayVO> getUsageOfPageDetail(Integer pageId, LocalDate dt, Integer offsetDays) {
        List<UsageOfDayVO> usageOfDayVOList = new ArrayList<>();
        List<PageUsageEntity> pageUsageEntityList = pageUsageRepo.findAllByPageIdInAndDtBetween(Collections.singletonList(pageId), dt.minusDays(offsetDays), dt);
        if (!CollectionUtils.isEmpty(pageUsageEntityList)) {
            Map<LocalDate, List<PageUsageEntity>> localDateListMap = pageUsageEntityList.stream().collect(Collectors.groupingBy(PageUsageEntity::getDt));
            localDateListMap.forEach((date, entityList) -> {
                UsageOfDayVO usageOfDayVO = new UsageOfDayVO();
                List<String> users = new ArrayList<>();
                usageOfDayVOList.add(usageOfDayVO);
                usageOfDayVO.setDt(yyyyMMdd1.format(date));
                entityList.forEach(pageUsageEntity -> {
                    if ("Batch".equals(pageUsageEntity.getAccountType())) {
                        usageOfDayVO.setBatch(pageUsageEntity.getAccessCount());
                    } else if ("Individual".equals(pageUsageEntity.getAccountType())) {
                        usageOfDayVO.setIndividual(pageUsageEntity.getAccessCount());
                    }
                    users.add(pageUsageEntity.getUsername());
                });
                usageOfDayVO.setUsers(users);
            });
        }
        return usageOfDayVOList;
    }

    @Override
    public void updatePAPageFamilyConfig(ProductAnalyzeVO productAnalyzeVO) throws JsonProcessingException {
        String pageFamilyConfig = objectMapper.writeValueAsString(productAnalyzeVO.getPageFamilyDTOList());
        ProfilingPageFamilyConfigInfo familyConfigInfo = pageFamilyConfigInfoRepo.getOneByName("PA2.0");
        if (Objects.isNull(familyConfigInfo)) {
            familyConfigInfo = new ProfilingPageFamilyConfigInfo();
            familyConfigInfo.setName("PA2.0");
            familyConfigInfo.setPageFamilyConfig(pageFamilyConfig);
        } else {
            familyConfigInfo.setPageFamilyConfig(pageFamilyConfig);
        }
        pageFamilyConfigInfoRepo.save(familyConfigInfo);
    }

    @Override
    public CustomerGroupEntity checkAuthorization(String authorization) throws Exception {
        String[] strs = authorization.split(" ");
        if (strs.length < 2) {
            throw new Exception("illegal arguments");
        }
        String s = new String(Base64.getDecoder().decode(strs[1].getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        String[] strs2 = s.split(":");
        return customerGroupInfoRepo.getOneByApiKeyAndApiSecret(strs2[0], strs2[1]);
    }

    @Override
    public long updateCustomerPageGroupInfo(CustomerGroupEntity customerGroupInfo, PageGroupVO pageGroupVO) {
        Long customerId = customerGroupInfo.getId();
        List<ProfilingCustomerPageRel> profilingPageGroupList = profilingCustomerPageRelRepo.findAllByCustomerId(customerId);
        profilingCustomerPageRelRepo.deleteAll(profilingPageGroupList);
        List<ProfilingCustomerPageRel> list = new ArrayList<>();
        ProfilingCustomerPageRel profilingPageGroup;

        List<Integer> pageIds = pageGroupVO.getPageIds();
        for (Integer pageId : pageIds) {
            profilingPageGroup = new ProfilingCustomerPageRel();
            list.add(profilingPageGroup);
            profilingPageGroup.setCustomerId(customerId);
            profilingPageGroup.setPageId(pageId);
        }

        return profilingCustomerPageRelRepo.saveAll(list).size();
    }

    private PageCardItemVO getTotalActivePages(List<Integer> pageIds, LocalDate localDate) {
        Instant start = Instant.now();
        PageCardItemVO totalActivePagesCard = new PageCardItemVO();
        totalActivePagesCard.setOrder(0);
        totalActivePagesCard.setTitle("Total Active Pages");
        LocalDate endDate = localDate;
        List<String> dts = new ArrayList<>();
        for (int i = 0; i < 90; i++) {
            final String dt = yyyyMMdd.format(endDate);
            dts.add(dt);
            endDate = endDate.minusDays(1);
        }
        long nonBot_cnt = nonBotPageCountRepo.countDistinctPageIds(pageIds, dts);
        long bot_cnt = botPageCountRepo.countDistinctPageIds(pageIds, dts);
        //ConcurrentHashSet<Integer> distinctSet = new ConcurrentHashSet<>();
        //List<NonBotPageCountEntity> nonBotPageCountEntities = nonBotPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        //nonBotPageCountEntities.parallelStream().forEach(nonBotPageCountEntity -> distinctSet.add(nonBotPageCountEntity.getPageId()));
        //List<BotPageCountEntity> botPageCountEntities = botPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        //botPageCountEntities.parallelStream().forEach(botPageCountEntity -> distinctSet.add(botPageCountEntity.getPageId()));

        endDate = localDate.minusDays(7);
        dts.clear();
        for (int i = 0; i < 90; i++) {
            final String dt = yyyyMMdd.format(endDate);
            dts.add(dt);
            endDate = endDate.minusDays(1);
        }
        long nonBot_cnt1 = nonBotPageCountRepo.countDistinctPageIds(pageIds, dts);
        long bot_cnt1 = botPageCountRepo.countDistinctPageIds(pageIds, dts);
        //ConcurrentHashSet<Integer> distinctSet1 = new ConcurrentHashSet<>();
        //nonBotPageCountEntities = nonBotPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        //nonBotPageCountEntities.parallelStream().forEach(nonBotPageCountEntity -> distinctSet1.add(nonBotPageCountEntity.getPageId()));
        //botPageCountEntities = botPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        //botPageCountEntities.parallelStream().forEach(botPageCountEntity -> distinctSet1.add(botPageCountEntity.getPageId()));
        long amount = nonBot_cnt + bot_cnt;
        totalActivePagesCard.setAmount(amount);
        long y = nonBot_cnt1 + bot_cnt1;
        totalActivePagesCard.setIncrementType(Long.compare(amount, y));
        if (amount == 0) {
            if ((amount - y) != 0)
                totalActivePagesCard.setIncrement(100.0);
            else
                totalActivePagesCard.setIncrement(0.0);
        } else {
            if (0L == y){
                totalActivePagesCard.setIncrement(100.0);
            } else {
                totalActivePagesCard.setIncrement(new BigDecimal(Math.abs(amount - y)).multiply(BigDecimal.valueOf(100.00D)).divide(BigDecimal.valueOf(y), 2, RoundingMode.DOWN).doubleValue());
            }
        }
        log.info("getTotalActivePages: {}", Duration.between(start, Instant.now()).toMillis());
        return totalActivePagesCard;
    }

    private PageCardItemVO getTotalUnusedPages(List<String> pageFamilyNameList, LocalDate localDate) {
        Instant start = Instant.now();
        PageCardItemVO totalUnusedPagesCard = new PageCardItemVO();
        totalUnusedPagesCard.setOrder(1);
        totalUnusedPagesCard.setTitle("Total Unused Pages");

        List<ProfilingUnusedPageInfo> profilingUnusedPageInfoList = profilingUnusedPageRepo.findAllByPageFamilyNameInAndDt(pageFamilyNameList, localDate);
        if (CollectionUtils.isEmpty(profilingUnusedPageInfoList)) {
            totalUnusedPagesCard.setAmount(0);
        } else {
            long aLong = profilingUnusedPageInfoList.stream().map(ProfilingUnusedPageInfo::getUnusedCount).reduce(0L, Long::sum);
            totalUnusedPagesCard.setAmount(aLong);
            profilingUnusedPageInfoList = profilingUnusedPageRepo.findAllByPageFamilyNameInAndDt(pageFamilyNameList, localDate.minusDays(7));
            if (CollectionUtils.isEmpty(profilingUnusedPageInfoList)) {
                totalUnusedPagesCard.setIncrementType(1);
                totalUnusedPagesCard.setIncrement(100.0);
            } else {
                long aLong1 = profilingUnusedPageInfoList.stream().map(ProfilingUnusedPageInfo::getUnusedCount).reduce(0L, Long::sum);
                totalUnusedPagesCard.setIncrementType(Long.compare(aLong, aLong1));
                if (aLong1 == 0) {
                    if (aLong != 0) {
                        totalUnusedPagesCard.setIncrement(100.0);
                    } else {
                        totalUnusedPagesCard.setIncrement(0.0);
                    }
                } else {
                    totalUnusedPagesCard.setIncrement(new BigDecimal(Math.abs(aLong - aLong1)).multiply(BigDecimal.valueOf(100.00D)).divide(BigDecimal.valueOf(aLong1), 2, RoundingMode.DOWN).doubleValue());
                }
            }
        }
        log.info("getTotalUnusedPages: {}", Duration.between(start, Instant.now()).toMillis());
        return totalUnusedPagesCard;
    }

    private PageCardItemVO getTotalUnusedPages(String pageFamilyName, LocalDate localDate) {
        PageCardItemVO totalUnusedPagesCard = new PageCardItemVO();
        totalUnusedPagesCard.setOrder(1);
        totalUnusedPagesCard.setTitle("Total Unused Pages");
        if ("all".equals(pageFamilyName)) {
            List<ProfilingUnusedPageInfo> profilingUnusedPageInfos = profilingUnusedPageRepo.findAllByDtIn(Collections.singletonList(localDate));
            long aLong = profilingUnusedPageInfos.stream().map(ProfilingUnusedPageInfo::getUnusedCount).reduce(0L, Long::sum);
            totalUnusedPagesCard.setAmount(aLong);
            profilingUnusedPageInfos = profilingUnusedPageRepo.findAllByDtIn(Collections.singletonList(localDate.minusDays(7)));
            long aLong1 = profilingUnusedPageInfos.stream().map(ProfilingUnusedPageInfo::getUnusedCount).reduce(0L, Long::sum);
            if (aLong1 == 0) {
                totalUnusedPagesCard.setIncrementType(1);
                totalUnusedPagesCard.setIncrement(100.0);
            } else {
                totalUnusedPagesCard.setIncrementType(Long.compare(aLong, aLong1));
                totalUnusedPagesCard.setIncrement(new BigDecimal(Math.abs(aLong - aLong1)).multiply(BigDecimal.valueOf(100.00D)).divide(BigDecimal.valueOf(aLong1), 2, RoundingMode.DOWN).doubleValue());
            }
            return totalUnusedPagesCard;
        }
        if ("Uncategorized".equals(pageFamilyName)) {
            pageFamilyName = "NULL";
        }
        ProfilingUnusedPageInfo profilingUnusedPageInfo = profilingUnusedPageRepo.getOneByPageFamilyNameAndDt(pageFamilyName, localDate);
        if (profilingUnusedPageInfo == null) {
            totalUnusedPagesCard.setAmount(0);
        } else {
            totalUnusedPagesCard.setAmount(profilingUnusedPageInfo.getUnusedCount());
            ProfilingUnusedPageInfo unusedPageInfo = profilingUnusedPageRepo.getOneByPageFamilyNameAndDt(pageFamilyName, localDate.minusDays(7));
            if (unusedPageInfo == null) {
                totalUnusedPagesCard.setIncrementType(1);
                totalUnusedPagesCard.setIncrement(100.0);
            } else {
                totalUnusedPagesCard.setIncrementType(Long.compare(profilingUnusedPageInfo.getUnusedCount(), unusedPageInfo.getUnusedCount()));
                if (unusedPageInfo.getUnusedCount() == 0) {
                    if (profilingUnusedPageInfo.getUnusedCount() != 0) {
                        totalUnusedPagesCard.setIncrement(100.0);
                    } else {
                        totalUnusedPagesCard.setIncrement(0.0);
                    }
                } else {
                    totalUnusedPagesCard.setIncrement(new BigDecimal(Math.abs(profilingUnusedPageInfo.getUnusedCount() - unusedPageInfo.getUnusedCount())).multiply(BigDecimal.valueOf(100.00D)).divide(BigDecimal.valueOf(unusedPageInfo.getUnusedCount()), 2, RoundingMode.DOWN).doubleValue());
                }
            }
        }
        return totalUnusedPagesCard;
    }

    private PageCardItemVO getTotalUnusedPages(LocalDate localDate) {
        PageCardItemVO totalUnusedPagesCard = new PageCardItemVO();
        totalUnusedPagesCard.setOrder(1);
        totalUnusedPagesCard.setTitle("Total Unused Pages");
        List<ProfilingPageActivityStats> profilingPageActivityStatsList = profilingPageActivityStatsRepo.findAllByDt(localDate);
        if (CollectionUtils.isEmpty(profilingPageActivityStatsList)) {
            totalUnusedPagesCard.setAmount(0);
        } else {
            ProfilingPageActivityStats stats = profilingPageActivityStatsList.get(0);
            totalUnusedPagesCard.setAmount(stats.getUnused_cnt());
            LocalDate localDate1 = localDate.minusDays(7);
            List<ProfilingPageActivityStats> allByDt = profilingPageActivityStatsRepo.findAllByDt(localDate1);
            if (CollectionUtils.isEmpty(allByDt)) {
                totalUnusedPagesCard.setIncrementType(1);
                totalUnusedPagesCard.setIncrement(100.0);
            } else {
                ProfilingPageActivityStats activityStats = allByDt.get(0);
                totalUnusedPagesCard.setIncrementType(Long.compare(stats.getUnused_cnt(), activityStats.getUnused_cnt()));
                if (activityStats.getUnused_cnt() == 0) {
                    if ((stats.getUnused_cnt() - activityStats.getUnused_cnt()) != 0)
                        totalUnusedPagesCard.setIncrement(100.0);
                    else
                        totalUnusedPagesCard.setIncrement(0.0);
                } else {
                    totalUnusedPagesCard.setIncrement(new BigDecimal(Math.abs(stats.getUnused_cnt() - activityStats.getUnused_cnt())).multiply(BigDecimal.valueOf(100.00D)).divide(BigDecimal.valueOf(activityStats.getUnused_cnt()), 2, RoundingMode.DOWN).doubleValue());
                }
            }
        }
        return totalUnusedPagesCard;
    }

    private PageCardItemVO getTotalTraffic(List<Integer> pageIds, LocalDate localDate) {
        Instant start = Instant.now();
        PageCardItemVO totalTrafficCard = new PageCardItemVO();
        totalTrafficCard.setOrder(2);
        totalTrafficCard.setTitle("Total Traffic");
        final List<String> dts = Collections.singletonList(DateTimeFormatter.ofPattern("yyyyMMdd").format(localDate));
        final List<NonBotPageCountEntity> nonBotPageCountEntities = nonBotPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        final BigDecimal bigDecimal = nonBotPageCountEntities.parallelStream().map(NonBotPageCountEntity::getTotal).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        final List<BotPageCountEntity> botPageCountEntities = botPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        final BigDecimal bigDecimal2 = botPageCountEntities.parallelStream().map(BotPageCountEntity::getTotal).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);

        final LocalDate localDate1 = localDate.minusDays(7);
        final List<String> sevenDaysAgo = Collections.singletonList(DateTimeFormatter.ofPattern("yyyyMMdd").format(localDate1));
        final List<NonBotPageCountEntity> nonBotPageCountEntities1 = nonBotPageCountRepo.findAllByPageIdInAndDtIn(pageIds, sevenDaysAgo);
        final BigDecimal bigDecimal1 = nonBotPageCountEntities1.parallelStream().map(NonBotPageCountEntity::getTotal).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        final List<BotPageCountEntity> botPageCountEntities1 = botPageCountRepo.findAllByPageIdInAndDtIn(pageIds, sevenDaysAgo);
        final BigDecimal bigDecimal3 = botPageCountEntities1.parallelStream().map(BotPageCountEntity::getTotal).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal numerator = bigDecimal.add(bigDecimal2);
        BigDecimal denominator = bigDecimal1.add(bigDecimal3);
        totalTrafficCard.setIncrementType(numerator.compareTo(denominator));
        totalTrafficCard.setAmount(numerator.longValue());
        if (denominator.equals(BigDecimal.ZERO)) {
            if (!numerator.equals(BigDecimal.ZERO))
                totalTrafficCard.setIncrement(100.0);
            else
                totalTrafficCard.setIncrement(0.0);
        } else {
            totalTrafficCard.setIncrement(numerator.subtract(denominator).abs().multiply(BigDecimal.valueOf(100.00D)).divide(denominator, 2, RoundingMode.DOWN).doubleValue());
        }
        log.info("getTotalTraffic: {}", Duration.between(start, Instant.now()).toMillis());
        return totalTrafficCard;
    }

    private PageCardItemVO getTotalNonBotTraffic(List<Integer> pageIds, LocalDate localDate) {
        Instant start = Instant.now();
        PageCardItemVO totalNonBotTrafficCard = new PageCardItemVO();
        totalNonBotTrafficCard.setOrder(3);
        totalNonBotTrafficCard.setTitle("Total Non-bot Traffic");
        final List<String> dts = Collections.singletonList(DateTimeFormatter.ofPattern("yyyyMMdd").format(localDate));
        final List<NonBotPageCountEntity> nonBotPageCountEntities = nonBotPageCountRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        final BigDecimal bigDecimal = nonBotPageCountEntities.parallelStream().map(NonBotPageCountEntity::getTotal).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        final LocalDate localDate1 = localDate.minusDays(7);
        final List<NonBotPageCountEntity> nonBotPageCountEntities1 = nonBotPageCountRepo.findAllByPageIdInAndDtIn(pageIds, Collections.singletonList(DateTimeFormatter.ofPattern("yyyyMMdd").format(localDate1)));
        final BigDecimal bigDecimal1 = nonBotPageCountEntities1.parallelStream().map(NonBotPageCountEntity::getTotal).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalNonBotTrafficCard.setIncrementType(bigDecimal.compareTo(bigDecimal1));
        totalNonBotTrafficCard.setAmount(bigDecimal.longValue());
        if (bigDecimal1.equals(BigDecimal.ZERO)) {
            if (!bigDecimal.equals(BigDecimal.ZERO))
                totalNonBotTrafficCard.setIncrement(100.0);
            else
                totalNonBotTrafficCard.setIncrement(0.0);
        } else {
            totalNonBotTrafficCard.setIncrement(bigDecimal.subtract(bigDecimal1).abs().multiply(BigDecimal.valueOf(100.00D)).divide(bigDecimal1, 2, RoundingMode.DOWN).doubleValue());
        }
        log.info("getTotalNonBotTraffic: {}", Duration.between(start, Instant.now()).toMillis());
        return totalNonBotTrafficCard;
    }

    private PageCardItemVO getTotalUsers(List<Integer> pageIds, LocalDate localDate) {
        Instant start = Instant.now();
        PageCardItemVO totalUsersCard = new PageCardItemVO();
        totalUsersCard.setOrder(4);
        totalUsersCard.setTitle("Total Users");
        List<LocalDate> dts = new ArrayList<>();
        LocalDate endDate = localDate;
        for (int i = 0; i < 90; i++) {
            dts.add(endDate);
            endDate = endDate.minusDays(1);
        }
        List<PageUsageEntity> pageUsageEntityList = pageUsageRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        long userCount = pageUsageEntityList.parallelStream().map(PageUsageEntity::getUsername).distinct().count();
        endDate = localDate.minusDays(7);
        dts.clear();
        for (int i = 0; i < 90; i++) {
            dts.add(endDate);
            endDate = endDate.minusDays(1);
        }
        pageUsageEntityList = pageUsageRepo.findAllByPageIdInAndDtIn(pageIds, dts);
        long userCount1 = pageUsageEntityList.parallelStream().map(PageUsageEntity::getUsername).distinct().count();
        totalUsersCard.setIncrementType(Long.compare(userCount, userCount1));
        totalUsersCard.setAmount(userCount);
        if (userCount1 == 0) {
            if (userCount != 0) {
                totalUsersCard.setIncrement(100.0);
            } else {
                totalUsersCard.setIncrement(0.0);
            }
        } else {
            totalUsersCard.setIncrement(new BigDecimal(Math.abs(userCount - userCount1)).multiply(BigDecimal.valueOf(100.00D)).divide(new BigDecimal(userCount1), 2, RoundingMode.DOWN).doubleValue());
        }
        log.info("getTotalUsers: {}", Duration.between(start, Instant.now()).toMillis());
        return totalUsersCard;
    }

    private PageCardItemVO getAbnormalPages(List<Integer> pageIds, LocalDate localDate) {
        Instant start = Instant.now();
        PageCardItemVO abnormalPagesCard = new PageCardItemVO();
        abnormalPagesCard.setOrder(5);
        abnormalPagesCard.setTitle("Abnormal Pages");
        List<String> refIds = pageIds.stream().map(String::valueOf).collect(Collectors.toList());
        List<AnomalyItemEntity> entityList = anomalyItemRepository.findAllByTypeAndRefIdInAndDt("page", refIds, localDate);
        List<AnomalyItemEntity> entityList1 = anomalyItemRepository.findAllByTypeAndRefIdInAndDt("page", refIds, localDate.minusDays(7));
        abnormalPagesCard.setAmount(entityList.size());
        List<Integer> abnormalPageIds = entityList.stream().map(AnomalyItemEntity::getRefId).map(Integer::valueOf).toList();
        abnormalPagesCard.getPageIds().addAll(abnormalPageIds);
        abnormalPagesCard.setIncrementType(Long.compare(entityList.size(), entityList1.size()));
        if (entityList1.size() == 0) {
            if (entityList.size() != 0) {
                abnormalPagesCard.setIncrement(100.0);
            } else {
                abnormalPagesCard.setIncrement(0.0);
            }
        } else {
            abnormalPagesCard.setIncrement(new BigDecimal(Math.abs(entityList.size() - entityList1.size())).multiply(BigDecimal.valueOf(100.00D)).divide(new BigDecimal(entityList1.size()), 2, RoundingMode.DOWN).doubleValue());
        }
        log.info("getTotalUsers: {}", Duration.between(start, Instant.now()).toMillis());
        return abnormalPagesCard;
    }
}
