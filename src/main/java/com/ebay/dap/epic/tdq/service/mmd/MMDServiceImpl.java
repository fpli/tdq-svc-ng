package com.ebay.dap.epic.tdq.service.mmd;

import com.ebay.dap.epic.tdq.config.AllMetricsCustParams;
import com.ebay.dap.epic.tdq.config.MMDCommonCfg;
import com.ebay.dap.epic.tdq.data.entity.AnomalyItemEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.AnomalyItemMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.MMDRecordInfoMapper;
import com.ebay.dap.epic.tdq.service.impl.TagProfilingServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class MMDServiceImpl implements MMDService {

//    @Autowired
//    private MetricSummaryRepo metricSummaryDao;

    @Autowired
    private AllMetricsCustParams allMetricsCustParams;

    @Autowired
    private MMDCommonCfg mmdCommonCfg;

    @Autowired
    private MMDClient mmdClient;

    @Autowired
    private AnomalyItemMapper anomalyItemRepo;

    @Autowired
    private MMDRecordInfoMapper mmdRecordInfoRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String dayDateFormat = "yyyy-MM-dd";
    private static final String fullDateFormat = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(fullDateFormat);
    private static final int maxTryTimes = 5;
    private static final int intervalTime = 1;
    private static final int timeSeriesMiniSize = 30;
    private static final String USTimeZone = "-07:00";
    private static final int SUCCESS_CODE = 200;

    private HttpClient httpClient;

    @Value("${proxyUrl:c2sproxy.vip.ebay.com}")
    private String proxyUrl;
    @Value("${proxyPort:8080}")
    private int port;
    @Value("${proxy.user:fangpli}")
    private String username;
    @Value("${proxy.password:202104vvvvccnkllljkejjjfkithukgdbjkdrufkchrfcjihfe}")
    private String password;

    private boolean usedProxy;

    @Autowired
    private ConfigurableEnvironment env;

    @PostConstruct
    public void init() {
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        if (env.acceptsProfiles(Profiles.of("Dev"))) {
            httpClient = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(proxyUrl, port))).build();
            usedProxy = true;
        } else {
            httpClient = HttpClient.newHttpClient();
            usedProxy = false;
        }
    }

//    @Override
//    public void updateBoundByMmd(int metricId, DateTime checkDateTime, String configType) throws Exception {
//        log.info("The MMD checkDate is " + checkDateTime);
//        if (!configType.equals("hour") && !configType.equals("day")) {
//            throw new RuntimeException("configType must be hour or day");
//        }
//        String checkDay = checkDateTime.toString(fullDateFormat);
//        List<MetricSummary> mmdSeries;
//        if (configType.equals("hour")) {
//            mmdSeries = metricSummaryDao.findMMD(metricId, checkDay, 21 * 24);
//        } else {
//            mmdSeries = metricSummaryDao.findMMD(metricId, checkDay, 90);
//        }
//        if (CollectionUtils.isEmpty(mmdSeries) || mmdSeries.size() < timeSeriesMiniSize) {
//            return;
//        }
//        timeZoneChange(mmdSeries);
//        MetricSummary needUpdMetric = checkAndGetUpdMetric(checkDay, mmdSeries);
////        List<MetricSummary> metricSummariesAferSplmt = preHandlerTimeSeris(mmdSeries, configType);
//        List<MetricSummary> metricSummariesAferSplmt = mmdSeries;
//        String mmdJsonString = genMMDJson(metricId, checkDateTime, metricSummariesAferSplmt, configType);
//        String httpResult = tryDoPostWithJson(mmdCommonCfg.getUrl(), mmdCommonCfg.getHeadParams(), mmdJsonString);
////        log.info("mmdJsonString:{}, httpResult:{}", mmdJsonString, httpResult);
//        updateMetricSummaryBoundsImpl(needUpdMetric, httpResult);
//    }

    @Override
    public void bulkFindAnomalyDaily(String configKey, Map<String, List<Series>> mmdTimeSeries) throws MMDRestException {
        log.info("Bulk find anomaly of: {}", mmdTimeSeries.keySet());
        MMDRequest mmdRequest = new MMDRequest();
        //TODO(yxiao6): refactor mmd request assemble logic
        GlobalConfig globalConfig = mmdCommonCfg.getGlobalConfig();
        globalConfig.setConfigType("day");
        globalConfig.setCustomParams(allMetricsCustParams.getMetricsCustParams().get("page_profiling_daily"));
        globalConfig.setNoiseRemoval(false); // if set to true, mmd will ignore the target dt data
        mmdRequest.setGlobalConf(globalConfig);

        // assemble mmd jobs payload
        List<JobParam> jobs = new ArrayList<>();
        for (Entry<String, List<Series>> entry : mmdTimeSeries.entrySet()) {
            JobParam mmdJob = new JobParam();
            mmdJob.setId(String.valueOf(jobs.size() + 1));
            mmdJob.setLabel(entry.getKey());
            mmdJob.setSeries(entry.getValue());
            jobs.add(mmdJob);
        }

        mmdRequest.setJobs(jobs);

        // call MMD http api to get result
        MMDResult mmdResult = mmdClient.findAnomaly(mmdRequest);
        List<JobResult> mmdJobResults = mmdResult.getJobs();
        List<AnomalyItemEntity> anomalyItems = new ArrayList<>();
        for (JobResult mmdJobResult : mmdJobResults) {
            List<MMDAlert> alerts = mmdJobResult.getAlerts();
            if (CollectionUtils.isNotEmpty(alerts)) {
                for (MMDAlert alert : alerts) {
                    if (alert.getIsAnomaly()) {
                        AnomalyItemEntity anomalyItem = new AnomalyItemEntity();
                        anomalyItem.setType("page");
                        anomalyItem.setRefId(mmdJobResult.getLabel());
                        anomalyItem.setValue(alert.getRawValue());
                        anomalyItem.setUBound(alert.getUBound());
                        anomalyItem.setLBound(alert.getLBound());
                        anomalyItem.setDt(LocalDate.parse(alert.getDtStr()));
                        anomalyItems.add(anomalyItem);
                    }
                }
            }
        }
        // save anomaly items if there is any
        if (CollectionUtils.isNotEmpty(anomalyItems)) {
            log.info("Found Anomaly Items from MMD: {}", anomalyItems);
            anomalyItemRepo.saveAll(anomalyItems);
        }
    }

    @Override
    public MMDResult mmdCallInBatch(Map<String, List<Series>> mmdTimeSeries, int n, Long count) {
        log.info("Bulk find anomaly of: {}", mmdTimeSeries.keySet());
        MMDRequest mmdRequest = new MMDRequest();

        GlobalConfig globalConfig = mmdCommonCfg.getGlobalConfig();
        globalConfig.setConfigType("day");
        //globalConfig.setCustomParams(allMetricsCustParams.getMetricsCustParams().get("Common"));
        globalConfig.setCustomParams(allMetricsCustParams.getMetricsCustParams().get("tag_profiling_daily"));
        globalConfig.getCustomParams().setThAnomalyScore(BigDecimal.valueOf(TagProfilingServiceImpl.score));
        globalConfig.setNoiseRemoval(false); // if set to true, mmd will ignore the target dt data
        mmdRequest.setGlobalConf(globalConfig);

        // assemble mmd jobs payload
        List<JobParam> jobs = new ArrayList<>();
        for (Entry<String, List<Series>> entry : mmdTimeSeries.entrySet()) {
            if (entry.getValue().size() < 90) continue;
            JobParam mmdJob = new JobParam();
            mmdJob.setId(entry.getKey());
            mmdJob.setLabel(entry.getKey());
            mmdJob.setSeries(entry.getValue());
            jobs.add(mmdJob);
        }
        //mmdRequest.setJobs(jobs);
        return ForkJoinPool.commonPool().invoke(new MMDCallTask(mmdRequest, jobs));
    }

    class MMDCallTask extends RecursiveTask<MMDResult> {

        private static final long serialVersionUID = 5335246460366775217L;
        MMDRequest mmdRequest;
        List<JobParam> jobs;

        private static final int SEQUENTIAL_THRESHOLD = 1000;

        MMDCallTask(MMDRequest mmdRequest, List<JobParam> jobs) {
            this.mmdRequest = mmdRequest;
            this.jobs = jobs;
        }

        @Override
        protected MMDResult compute() {
            if (jobs.size() <= SEQUENTIAL_THRESHOLD) {
                mmdRequest.setJobs(jobs);
                // call MMD http api to get result
                //if (ConstantDefine.CUR_ENV.equalsIgnoreCase(ConstantDefine.ENV.QA) || ConstantDefine.CUR_ENV.equalsIgnoreCase(ConstantDefine.ENV.PROD)) {
                mmdCommonCfg.setUrl("http://mmd-ng-pp-svc.mmd-prod-ns.svc.25.tess.io:80/mmd/find-anomaly");
                //}
                String jsonString = null;
                try {
                    jsonString = objectMapper.writeValueAsString(mmdRequest);
                    Instant start = Instant.now();
                    try {
                        String httpResult = MMDServiceImpl.this.tryDoPostWithJson(mmdCommonCfg.getUrl(), mmdCommonCfg.getHeadParams(), jsonString);
                        log.info("jobs size: {}, cost time: {} seconds", jobs.size(), Duration.between(start, Instant.now()).getSeconds());
//                        if (ConstantDefine.CUR_ENV.equalsIgnoreCase(ConstantDefine.ENV.QA)) {
                        log.info("jsonString:{} \n, httpResult:{}", jsonString, httpResult);
//                        }
                        return objectMapper.readValue(httpResult, MMDResult.class);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        log.error("jsonString: {}", jsonString);
                        e.printStackTrace();
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                int middle = jobs.size() >>> 1;
                List<JobParam> subList = jobs.subList(middle, jobs.size());
                this.jobs = jobs.subList(0, middle);
                MMDCallTask task = new MMDCallTask(mmdRequest.clone(), subList);
                task.fork();
                MMDResult thisResult = this.compute();
                MMDResult thatResult = task.join();
                if (null != thatResult && thatResult.getCode() == SUCCESS_CODE) {
                    thisResult.getJobs().addAll(thatResult.getJobs());
                }
                return thisResult;
            }
            return null;
        }
    }

    @Override
    public String testMMDRestAPI(String mmdJsonString) throws Exception {
        String httpResult = tryDoPostWithJson(mmdCommonCfg.getUrl(), mmdCommonCfg.getHeadParams(), mmdJsonString);
//        log.info("mmdJsonString:{}, httpResult:{}", mmdJsonString, httpResult);
        return httpResult;
    }

//    @Override
//    public void fillMMDBound(MetricSummarySubHour metricSummarySubHour, List<MetricSummarySubHour> metricSummaries) throws Exception {
//        EvtModelParam evtModelParam = new EvtModelParam();
//        GlobalConf globalConf = evtModelParam.getGlobalConf();
//        globalConf.setConfigType("minute");
//        globalConf.setModelType("evt");
//        Map<String, Object> customParams = new HashMap<>();
//        //customParams.put("BASE_LINE", 70);
//        customParams.put("MISSING_RATE", 0.2);
//        customParams.put("INIT_SIZE", 60);
//        customParams.put("HIDE_NORMAL_DETAIL", false);
//        globalConf.setCustomParams(customParams);
//        // not complete
//        JobParam jobParam = new JobParam();
//        jobParam.setId(metricSummarySubHour.getGroupId().toString());
//        jobParam.setLabel(metricSummarySubHour.getGroupId().toString());
//        ArrayList<Series> seriesList = new ArrayList<>();
//        jobParam.setSeries(seriesList);
//        metricSummaries.sort(Comparator.comparing(MetricSummarySubHour::getDateTime));
//        metricSummaries.forEach(metricSummarySubHour1 -> {
//            Series series = new Series();
//            series.setTimestamp(metricSummarySubHour1.getDateTime().format(dateTimeFormatter));
//            series.setValue(metricSummarySubHour1.getValue());
//            jobParam.getSeries().add(series);
//        });
//        evtModelParam.setJobs(Collections.singletonList(jobParam));
//        String payload = JSON.toJSONString(evtModelParam);
//        String response = tryDoPostWithJson(mmdCommonCfg.getUrl(), mmdCommonCfg.getHeadParams(), payload);
//        EvtOutputModel evtOutputModel = JSON.parseObject(response, EvtOutputModel.class);
//        List<JobModel> jobs = evtOutputModel.getJobs();
//        if (!org.springframework.util.CollectionUtils.isEmpty(jobs)) {
//            JobModel jobModel = jobs.get(0);
//            MsgModel msg = jobModel.getMsg();
//            OutputModel summary = msg.getSummary();
//            metricSummarySubHour.setAnomaly(summary.isAnomaly());
//            metricSummarySubHour.setLowerBound(summary.getLBound());
//            metricSummarySubHour.setUpperBound(summary.getUBound());
//            if (summary.isAnomaly()) {
//                MMDRecordInfo mmdRecordInfo = new MMDRecordInfo();
//                mmdRecordInfo.setUid(getUidOfMetricSummarySubHour(metricSummarySubHour));
//                Example<MMDRecordInfo> mmdRecordInfoExample = Example.of(mmdRecordInfo);
//                Optional<MMDRecordInfo> mmdRecordInfoOptional = mmdRecordInfoRepo.findOne(mmdRecordInfoExample);
//                mmdRecordInfo = mmdRecordInfoOptional.orElse(mmdRecordInfo);
//                mmdRecordInfo.setPayload(payload);
//                mmdRecordInfo.setResponse(response);
//                mmdRecordInfo.setTimeInterval(metricSummarySubHour.getTimeInterval());
//                mmdRecordInfo.setAnomalyType(0);
////                if(!mmdRecordInfoOptional.isPresent()){
////                    mmdRecordInfo.setCreateTime(LocalDateTime.now());
////                }
////                mmdRecordInfo.setUpdateTime(LocalDateTime.now());
//                mmdRecordInfoRepo.save(mmdRecordInfo);
//            } else {
//                if (summary.getV() < summary.getLBound() || summary.getV() > summary.getUBound()) {
//                    MMDRecordInfo mmdRecordInfo = new MMDRecordInfo();
//                    mmdRecordInfo.setUid(getUidOfMetricSummarySubHour(metricSummarySubHour));
//                    Example<MMDRecordInfo> mmdRecordInfoExample = Example.of(mmdRecordInfo);
//                    Optional<MMDRecordInfo> mmdRecordInfoOptional = mmdRecordInfoRepo.findOne(mmdRecordInfoExample);
//                    mmdRecordInfo = mmdRecordInfoOptional.orElse(mmdRecordInfo);
//                    mmdRecordInfo.setPayload(payload);
//                    mmdRecordInfo.setResponse(response);
//                    mmdRecordInfo.setTimeInterval(metricSummarySubHour.getTimeInterval());
//                    mmdRecordInfo.setAnomalyType(1);
////                    if(!mmdRecordInfoOptional.isPresent()){
////                        mmdRecordInfo.setCreateTime(LocalDateTime.now());
////                    }
////                    mmdRecordInfo.setUpdateTime(LocalDateTime.now());
//                    mmdRecordInfoRepo.save(mmdRecordInfo);
//                }
//            }
//        }
//    }

//    private String getUidOfMetricSummarySubHour(MetricSummarySubHour metricSummarySubHour) {
//        return String.valueOf(metricSummarySubHour.getGroupId()) + "-" + dateTimeFormatter.format(metricSummarySubHour.getDateTime()) + "-" + metricSummarySubHour.getTimeInterval();
//    }

//    private void timeZoneChange(List<MetricSummary> mmd) {
//        // datetime timezone change to use time
//        DateTimeZone usTimeZone = DateTimeZone.forID(USTimeZone);
//        if (!DateTimeZone.getDefault().equals(usTimeZone)) {
//            for (MetricSummary metricSummary : mmd) {
//                metricSummary.setDateTime(metricSummary.getDateTime().withZone(usTimeZone));
//            }
//        }
//    }

//    private MetricSummary checkAndGetUpdMetric(String checkDay, List<MetricSummary> dBResult) {
//        MetricSummary checkDayFromDB = dBResult.get(0);
//        if (!checkDayFromDB.getDateTime().toString(fullDateFormat).equals(checkDay)) {
//            throw new RuntimeException("can not get " + checkDay + " data");
//        }
//        return checkDayFromDB;
//    }

//    private List<MetricSummary> preHandlerTimeSeris(List<MetricSummary> mmdSeris, String configType) {
//        DateTime cursorDay = mmdSeris.get(mmdSeris.size() - 1).getDateTime();
//        List<MetricSummary> metricSummariesAferSplmt = new ArrayList<>();
//        MetricSummary metricSummary;
//        for (int i = mmdSeris.size() - 2; i != -1; ) {
//            metricSummary = mmdSeris.get(i);
//            MetricSummary summaryAhead = mmdSeris.get(i + 1);
//            DateTime metricDate = metricSummary.getDateTime();
//            if (!cursorDay.equals(metricDate)) {
//                MetricSummary supplement = new MetricSummary();
//                supplement.setDateTime(cursorDay);
//                supplement.setValue(summaryAhead.getValue());
//                metricSummariesAferSplmt.add(supplement);
//            } else {
//                metricSummariesAferSplmt.add(metricSummary);
//                i--;
//            }
//            if (Objects.equals(configType, "day")) {
//                cursorDay = cursorDay.plusDays(1);
//            } else if (Objects.equals(configType, "hour")) {
//                cursorDay = cursorDay.plusHours(1);
//            }
//        }
//        return metricSummariesAferSplmt;
//    }

//    private String genMMDJson(int metricId, DateTime checkDate, List<MetricSummary> metricSummariesAferSplmt, String configType) {
//        //get sample of configration
//        GlobalConfig globalConfigSample = mmdCommonCfg.getGlobalConfig();
//        JobParam jobParamSample = mmdCommonCfg.getJobParam();
//        //construct mmdParams
//        MMDParams mmdParams = new MMDParams();
//        GlobalConfig globalConfig = new GlobalConfig();
//        JobParam jobParam = new JobParam();
//        //shallow copy
//        BeanUtils.copyProperties(globalConfigSample, globalConfig);
//        BeanUtils.copyProperties(jobParamSample, jobParam);
//        //globalConfig set checkpoint
//        if (configType.equals("hour")) {
//            globalConfig.setCheckPoint(checkDate.toString(fullDateFormat));
//        } else if (configType.equals("day")) {
//            globalConfig.setCheckPoint(checkDate.toString(dayDateFormat));
//        }
//        globalConfig.setConfigType(configType);
//        Map<String, CustomParams> metricsCustParams = allMetricsCustParams.getMetricsCustParams();
//        //globalConfig set customParams
//        switch (metricId) {
//            case Global_Mandatory_Tag_Item_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Global_Mandatory_Tag_Item_Rate"));
//                break;
//            case Global_Mandatory_Tag_User_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Global_Mandatory_Tag_User_Rate"));
//                break;
//            case Marketing_Event_Volume:
//                globalConfig.setCustomParams(metricsCustParams.get("Marketing_Event_Volume"));
//                break;
//            case Transformation_Error_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Transformation_Error_Rate"));
//                break;
//            case Overall_Event_Volume:
//            case Overall_Event_Volume_Bot:
//            case Overall_Event_Volume_Non_Bot:
//                globalConfig.setCustomParams(metricsCustParams.get("Overall_Event_Volume"));
//                break;
//            case Consistency_Rate_Of_EP_Site_Id:
//                globalConfig.setCustomParams(metricsCustParams.get("Consistency_Rate_Of_EP_Site_Id"));
//                break;
//            case Qualification_Age_NQT:
//                globalConfig.setCustomParams(metricsCustParams.get("Qualification_Age_NQT"));
//                break;
//            case Search_Tag:
//                globalConfig.setCustomParams(metricsCustParams.get("Search_Tag"));
//                break;
//            case Native_Tag:
//                globalConfig.setCustomParams(metricsCustParams.get("Native_Tag"));
//                break;
//            case Site_Email_Click_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Site_Email_Click_Rate"));
//                break;
//            case Search_Tag_Cpnip_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Search_Tag_Cpnip_Rate"));
//                break;
//            case Search_Tag_Clktrack_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Search_Tag_Clktrack_Rate"));
//                break;
//            case Search_Tag_Icpp_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Search_Tag_Icpp_Rate"));
//                break;
//            case Native_Tag_Dn_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Native_Tag_Dn_Rate"));
//                break;
//            case Native_Tag_Mos_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Native_Tag_Mos_Rate"));
//                break;
//            case Native_Tag_Osv_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Native_Tag_Osv_Rate"));
//                break;
//            case Search_Tag_SrpGist_Rate:
//                globalConfig.setCustomParams(metricsCustParams.get("Search_Tag_SrpGist_Rate"));
//                break;
//            case Store_Event_Tot_Volume:
//                globalConfig.setCustomParams(metricsCustParams.get("Store_Event_Tot_Volume"));
//                break;
//            case URL_Query_String_User_Slctd_Id_Volume:
//                globalConfig.setCustomParams(metricsCustParams.get("URL_Query_String_User_Slctd_Id_Volume"));
//                break;
//            case Soid_owner_id_Volume:
//                globalConfig.setCustomParams(metricsCustParams.get("Soid_owner_id_Volume"));
//                break;
//            case URL_Query_String_Store_Name_Volume:
//                globalConfig.setCustomParams(metricsCustParams.get("URL_Query_String_Store_Name_Volume"));
//                break;
//            case Soid_Owner_public_id_Volume:
//                globalConfig.setCustomParams(metricsCustParams.get("Soid_Owner_public_id_Volume"));
//                break;
//            case REFRESHER_Item_Id_Volume:
//                globalConfig.setCustomParams(metricsCustParams.get("REFRESHER_Item_Id_Volume"));
//                break;
//            case SOJ_Item_Id_Volume:
//                globalConfig.setCustomParams(metricsCustParams.get("SOJ_Item_Id_Volume"));
//                break;
//            default:
//                globalConfig.setCustomParams(metricsCustParams.get("Common"));
//        }
//        ArrayList<Series> seriesList = new ArrayList<>();
//        for (MetricSummary summary : metricSummariesAferSplmt) {
//            Series series = new Series();
//            if (configType.equals("hour")) {
//                series.setTimestamp(summary.getDateTime().toString(fullDateFormat));
//            } else if (configType.equals("day")) {
//                series.setTimestamp(summary.getDateTime().toString(dayDateFormat));
//            }
//            series.setValue(summary.getValue());
//            seriesList.add(series);
//        }
//        //jobParam set series
//        jobParam.setSeries(seriesList);
//        //set mmdParams
//        mmdParams.setGlobalConf(globalConfig);
//        ArrayList<JobParam> jobs = new ArrayList<>();
//        jobs.add(jobParam);
//        mmdParams.setJobs(jobs);
//        //change mmdParams to jsonString
//        return JSON.toJSONString(mmdParams);
//    }

    private String tryDoPostWithJson(String url, Map<String, String> headParams, String jsonEntity) throws Exception {
        String httpResult = null;
        Exception exception = null;
        int currenTryTimes = 0;
        //Try maxTryTimes
        while (maxTryTimes != currenTryTimes) {
            try {
//                httpResult = HttpResult.doPostWithJson(url, headParams, jsonEntity);
                HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).POST(HttpRequest.BodyPublishers.ofString(jsonEntity)).setHeader("Content-Type", "application/json");
                headParams.forEach(builder::setHeader);
                if (usedProxy){
                    String encoded = new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
                    builder.setHeader("Proxy-Authorization", "Basic " + encoded);
                }
                HttpRequest httpRequest = builder.build();
                HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                httpResult = httpResponse.body();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
                try {
                    TimeUnit.SECONDS.sleep(intervalTime);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                currenTryTimes++;
                log.info("Connect have exception,max retryTimes is " + maxTryTimes + ", now current tryTimes is " + currenTryTimes);
            }
        }
        if (maxTryTimes == currenTryTimes) {
            log.error("Now try " + maxTryTimes + " times, MMD service still can not connect");
            throw exception;
        }
        return httpResult;
    }

//    private void updateMetricSummaryBoundsImpl(MetricSummary needUpdMetric, String httpResult) {
//        MMDResult mmdResult;
//        try {
//            mmdResult = JSON.parseObject(httpResult, MMDResult.class);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new RuntimeException("The mmd result can not be parse");
//        }
//        if (mmdResult.getCode() != SUCCESS_CODE) {
//            throw new RuntimeException("The mmd call has error, code is " + mmdResult.getCode() + " ,errormessage is " + mmdResult.getMessage());
//        }
//        if (CollectionUtils.isEmpty(mmdResult.getJobs()) || CollectionUtils.isEmpty(mmdResult.getJobs().get(0).getAlerts())) {
//            return;
//        }
//        BigDecimal lBound = mmdResult.getJobs().get(0).getAlerts().get(0).getLBound();
//        BigDecimal uBound = mmdResult.getJobs().get(0).getAlerts().get(0).getUBound();
//        metricSummaryDao.updateMetricSummaryBounds(lBound, uBound, needUpdMetric.getMetricId(), needUpdMetric.getDateTime().toString(fullDateFormat));
//    }
}

