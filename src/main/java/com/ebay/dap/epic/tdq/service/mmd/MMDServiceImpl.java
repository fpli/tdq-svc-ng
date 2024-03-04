package com.ebay.dap.epic.tdq.service.mmd;

import com.ebay.dap.epic.tdq.config.AllMetricsCustParams;
import com.ebay.dap.epic.tdq.config.C2SProxyConfig;
import com.ebay.dap.epic.tdq.config.MMDCommonCfg;
import com.ebay.dap.epic.tdq.data.entity.AnomalyItemEntity;
import com.ebay.dap.epic.tdq.data.entity.MMDRecordInfo;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.MMDRecordInfoMapper;
import com.ebay.dap.epic.tdq.service.impl.TagProfilingServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

import static com.ebay.dap.epic.tdq.common.Profile.C2S_PROXY;

@Slf4j
@Service
public class MMDServiceImpl implements MMDService {

    @Autowired
    private AllMetricsCustParams allMetricsCustParams;

    @Autowired
    private MMDCommonCfg mmdCommonCfg;

    @Autowired
    private MMDClient mmdClient;

    @Autowired
    private MMDRecordInfoMapper mmdRecordInfoMapper;

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

    @Autowired
    private C2SProxyConfig proxyConfig;

    private boolean usedProxy;

    @Autowired
    private ConfigurableEnvironment env;

    @PostConstruct
    public void init() {
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        if (env.acceptsProfiles(Profiles.of(C2S_PROXY))) {
            httpClient = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress(proxyConfig.getProxyHost(), proxyConfig.getProxyPort()))).build();
            usedProxy = true;
        } else {
            httpClient = HttpClient.newHttpClient();
            usedProxy = false;
        }
    }

    @Override
    public List<AnomalyItemEntity> bulkFindAnomalyDaily(String configKey, Map<String, List<Series>> mmdTimeSeries) throws MMDException {
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

        // call MMD api to get alert results
        MMDResult mmdResult = mmdClient.findAnomaly(mmdRequest);

        // log mmd request and response
        MMDRecordInfo mmdRecordInfo = new MMDRecordInfo();
        try {
            mmdRecordInfo.setPayload(objectMapper.writeValueAsString(mmdRequest));
            mmdRecordInfo.setResponse(objectMapper.writeValueAsString(mmdResult));
            mmdRecordInfo.setAnomalyType(1);
            mmdRecordInfo.setTimeInterval(1);
            mmdRecordInfo.setUid("page");
            mmdRecordInfoMapper.insert(mmdRecordInfo);
        } catch (Exception e) {
            throw new MMDException(e);
        }

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

        return anomalyItems;
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
//                mmdCommonCfg.setUrl("http://mmd-ng-pp-svc.mmd-prod-ns.svc.25.tess.io:80/mmd/find-anomaly");
                //}
                String jsonString = null;
                try {
                    jsonString = objectMapper.writeValueAsString(mmdRequest);
                    Instant start = Instant.now();
                    try {
                        String httpResult = MMDServiceImpl.this.tryDoPostWithJson(mmdCommonCfg.getUrl(), mmdCommonCfg.getHeadParams(), jsonString);
                        log.info("jobs size: {}, cost time: {} seconds", jobs.size(), Duration.between(start, Instant.now()).getSeconds());
//                        if (ConstantDefine.CUR_ENV.equalsIgnoreCase(ConstantDefine.ENV.QA)) {
                        //log.info("jsonString:{} \n, httpResult:{}", jsonString, httpResult);
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
                    String encoded = new String(Base64.getEncoder().encode((proxyConfig.getProxyUsername() + ":" + proxyConfig.getProxyPassword()).getBytes()));
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

}

