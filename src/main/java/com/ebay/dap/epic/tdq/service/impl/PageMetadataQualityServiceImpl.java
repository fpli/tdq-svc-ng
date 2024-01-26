package com.ebay.dap.epic.tdq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ebay.dap.epic.tdq.data.entity.PagePoolLKP;
import com.ebay.dap.epic.tdq.data.entity.UnregisterPageMetadataEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.PagePoolLKPMapper;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.UnregisterPageMetadataMapper;
import com.ebay.dap.epic.tdq.data.vo.BaseGeneralVO;
import com.ebay.dap.epic.tdq.service.PageMetadataQualityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.URLEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PageMetadataQualityServiceImpl implements PageMetadataQualityService {

    @Autowired
    private PagePoolLKPMapper pagePoolLKPMapper;

    @Autowired
    private UnregisterPageMetadataMapper unregisterPageMetadataMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private HttpClient httpClient;

    private String baseURL = "https://cms.vip.stratus.ebay.com/cms";

    @PostConstruct
    public void init(){
        //System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        httpClient = HttpClient.newHttpClient();
    }

    private String validateUser(String user, String password){
        //("_TDQ_SVC_USER", "SRB*qQcf@253!ypZnxN_Pg");
        String uri = "https://cms.vip.stratus.ebay.com/cms/validate/user/_TDQ_SVC_USER";
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(uri));
            builder.setHeader("X-Password", "SRB*qQcf@253!ypZnxN_Pg");
            HttpResponse<String> httpResponse = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            String body = httpResponse.body();
            Map<String, Object> map = objectMapper.readValue(body, Map.class);
            Map<String, String> status = (Map<String, String>) map.get("status");
            if ("200".equals(status.get("code"))){
                return map.get("token").toString();
            }
        } catch (IOException | InterruptedException e) {
            log.error("occurred exception: {0}", e);
        }
        return null;
    }

    @Override
    public int dailyTask(LocalDate date) {
        LongAdder longAdder = new LongAdder();
        LambdaQueryWrapper<PagePoolLKP> pagePoolLKPLambdaQueryWrapper = Wrappers.lambdaQuery(PagePoolLKP.class);
        pagePoolLKPLambdaQueryWrapper.ge(PagePoolLKP::getDt, date);
        List<PagePoolLKP> pagePoolLKPS = pagePoolLKPMapper.selectList(pagePoolLKPLambdaQueryWrapper);
        if (pagePoolLKPS.isEmpty()){
            return 0;
        }

        LambdaQueryWrapper<UnregisterPageMetadataEntity> lambdaQueryWrapper = Wrappers.lambdaQuery(UnregisterPageMetadataEntity.class);
        lambdaQueryWrapper.eq(UnregisterPageMetadataEntity::getDt, date);
        unregisterPageMetadataMapper.delete(lambdaQueryWrapper);

        String token = validateUser("", "");
        if (null == token){
            return 0;
        }
        //pagePoolLKPS.parallelStream().forEach(pagePoolLKP -> retrieveMetadata(pagePoolLKP, longAdder));
        ConcurrentMap<String, List<PagePoolLKP>> listConcurrentMap = pagePoolLKPS.parallelStream().collect(Collectors.groupingByConcurrent(PagePoolLKP::getPoolName));
        listConcurrentMap.forEach((poolName, list) -> {
            // todo
        });
        pagePoolLKPS.parallelStream().forEach(pagePoolLKP -> retrieveUnregisteredPageMetadata(pagePoolLKP, token, longAdder));
        return longAdder.intValue();
    }

    @Override
    public BaseGeneralVO<UnregisterPageMetadataEntity> listAllUnregisterPage(LocalDate date) {
        BaseGeneralVO<UnregisterPageMetadataEntity> baseGeneralVO = new BaseGeneralVO();
        LambdaQueryWrapper<UnregisterPageMetadataEntity> lambdaQueryWrapper = Wrappers.lambdaQuery(UnregisterPageMetadataEntity.class);
        lambdaQueryWrapper.eq(UnregisterPageMetadataEntity::getDt, date);
        //lambdaQueryWrapper.orderByDesc(UnregisterPageMetadataEntity::getTraffic);
        List<UnregisterPageMetadataEntity> unregisterPageMetadataEntities = unregisterPageMetadataMapper.selectList(lambdaQueryWrapper);
        baseGeneralVO.setDate(date.toString());
        baseGeneralVO.setList(unregisterPageMetadataEntities);
        baseGeneralVO.setCount(unregisterPageMetadataEntities.size());
        return baseGeneralVO;
    }

    private void retrieveUnregisteredPageMetadata(PagePoolLKP pagePoolLKP, String token, LongAdder longAdder){
        UnregisterPageMetadataEntity unregisterPageMetadataEntity = new UnregisterPageMetadataEntity();
        unregisterPageMetadataEntity.setPageId(pagePoolLKP.getPageId());
        unregisterPageMetadataEntity.setTraffic(pagePoolLKP.getTraffic());
        unregisterPageMetadataEntity.setPoolName(pagePoolLKP.getPoolName());
        unregisterPageMetadataEntity.setDt(pagePoolLKP.getDt());
        unregisterPageMetadataMapper.insert(unregisterPageMetadataEntity);

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.setHeader("Authorization", token);
        try {
            //String applicationServiceTemplate = "https://cms.vip.stratus.ebay.com/cms/repositories/cmsdb/branches/main/query/ApplicationService[@alias=\"%s\" and @paasRealm=\"Production\"]?mode=uri";
            String poolName = pagePoolLKP.getPoolName();
            //String url = String.format(applicationServiceTemplate, poolName);
            //URI uri = URI.create(URLEncoder.encode(url, StandardCharsets.UTF_8));
            String queryTemplate = "[@alias=\"%s\" and @paasRealm in (\"Production\", \"production\")]";

            String applicationServiceTemplate = "https://cms.vip.stratus.ebay.com/cms/repositories/cmsdb/branches/main/query/ApplicationService";
            //String poolName = "r1lyltrwdsecapplicationcont";
            String url = String.format(queryTemplate, poolName);
            URI uri = URI.create(URLEncoder.DEFAULT.encode(url, StandardCharsets.UTF_8));
            String actualUri = applicationServiceTemplate + uri + "?mode=uri";
            builder.uri(URI.create(actualUri));
            Map applicationServiceMap = doCallCMS(builder.build());
            if (null != applicationServiceMap) {
                Map applicationMap = (Map) applicationServiceMap.get("application");
                Map<String, String> refMap = (Map<String, String>) applicationMap.get("ref");
                String applicationURL = refMap.get("url");
                fillApplicationInfo(builder, applicationURL, unregisterPageMetadataEntity);
                Map onCallServiceMap = (Map) applicationServiceMap.get("onCallService");
                Map<String, String> ref_map = (Map<String, String>) onCallServiceMap.get("ref");
                String onCallServiceURL = ref_map.get("url");
                fillOnCallServiceEmail(builder, onCallServiceURL, unregisterPageMetadataEntity);
                unregisterPageMetadataMapper.updateById(unregisterPageMetadataEntity);
                longAdder.increment();
            }

        } catch (IOException | InterruptedException e) {
            log.error("occurred exception: {0}", e);
            throw new RuntimeException(e);
        }
    }

    //unregisterPageMetadataEntity.setJiraLink(app_jira);
    //unregisterPageMetadataEntity.setOwner(app_owner);
    private void fillApplicationInfo(HttpRequest.Builder builder, String applicationURL, UnregisterPageMetadataEntity unregisterPageMetadataEntity) throws IOException, InterruptedException {
        URI uri = URI.create(baseURL + applicationURL);
        builder.uri(uri);
        Map app_map = doCallCMS(builder.build());
        if (null != app_map) {
            Map jira_map = (Map) app_map.get("jira");
            Map jira_ref_map = (Map) jira_map.get("ref");
            String jira_url = jira_ref_map.get("url").toString();
            URI jira_uri = URI.create(baseURL + jira_url);
            builder.uri(jira_uri);
            Map project = doCallCMS(builder.build());
            if (null != project) {
                String projectKey = (String) project.get("projectKey");
                unregisterPageMetadataEntity.setJiraLink("https://jirap.corp.ebay.com/browse/" + projectKey);
            }

            Map consumer_map = (Map) app_map.get("consumer");
            Map consumer_ref_map = (Map) consumer_map.get("ref");
            String consumer_url = consumer_ref_map.get("url").toString();
            URI consomer_uri = URI.create(baseURL + consumer_url);
            builder.uri(consomer_uri);
            Map map4 = doCallCMS(builder.build());
            if (null != map4) {
                Map owner = (Map) map4.get("owner");
                Map ref = (Map) owner.get("ref");
                String owner_url = ref.get("url").toString();

                URI owner_uri = URI.create(baseURL + owner_url);
                builder.uri(owner_uri);
                Map map6 = doCallCMS(builder.build());
                if (null != map6) {
                    String resourceId = map6.get("resourceId").toString();
                    unregisterPageMetadataEntity.setOwner(resourceId);
                }
            }
        }
    }

    //unregisterPageMetadataEntity.setEmail(app_notification);
    private void fillOnCallServiceEmail(HttpRequest.Builder builder, String onCallServiceURL, UnregisterPageMetadataEntity unregisterPageMetadataEntity) throws IOException, InterruptedException {
        URI uri = URI.create(baseURL + onCallServiceURL);
        builder.uri(uri);
        Map map = doCallCMS(builder.build());
        if (null == map){
            return;
        }
        String email = (String) map.get("email");
        Map dedicatedTeamMap = (Map) map.get("dedicatedTeam");
        if (!CollectionUtils.isEmpty(dedicatedTeamMap)){
            Map ref_map = (Map) dedicatedTeamMap.get("ref");
            String url = (String) ref_map.get("url");
            builder.uri(URI.create(baseURL + url));
            Map map1 = doCallCMS(builder.build());
            List<String> dl_list = (List<String>) map1.get("dl");
            if (!CollectionUtils.isEmpty(dl_list)){
                email = dl_list.get(0);
            }
        }
        unregisterPageMetadataEntity.setEmail(email);
    }

    private Map doCallCMS(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = httpResponse.body();
        Map map = objectMapper.readValue(body, Map.class);
        Map<String, String> status = (Map<String, String>) map.get("status");
        if ("200".equals(status.get("code"))){
            List<Map> result = (List<Map>) map.get("result");
            if (!CollectionUtils.isEmpty(result)){
                return result.get(0);
            }
        }
        return null;
    }

}
