package com.ebay.dap.epic.tdq.config;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Log4j2
@Configuration
public class RestTemplateConfig {

    private final MMDCommonCfg mmdCommonCfg;

    public RestTemplateConfig(MMDCommonCfg mmdCommonCfg) {
        this.mmdCommonCfg = mmdCommonCfg;
    }

    @Bean("mmdRestTemplate")
    public RestTemplate mmdRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                        Environment env) {
        log.info("Initialize mmdRestTemplate");
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().disableCookieManagement();

        // local profile enable c2sproxy
        if (env.acceptsProfiles(Profiles.of("c2sproxy"))) {
            final String C2S_PROXY_HOST = "c2syubi.vip.ebay.com";
            final int C2S_PROXY_PORT = 8080;
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(C2S_PROXY_HOST, C2S_PROXY_PORT),
                    new UsernamePasswordCredentials(env.getProperty("NT_USER"), env.getProperty("YUBI_KEY"))
            );
            httpClientBuilder.setProxy(new HttpHost(C2S_PROXY_HOST, C2S_PROXY_PORT, "http"))
                    .setDefaultCredentialsProvider(credsProvider);
        }

        requestFactory.setHttpClient(httpClientBuilder.build());
        //if (ConstantDefine.CUR_ENV.equalsIgnoreCase(ConstantDefine.ENV.QA)){
        mmdCommonCfg.setUrl("http://mmd-ng-pp-svc.mmd-prod-ns.svc.25.tess.io:80/mmd/find-anomaly");
//        }
        return restTemplateBuilder.requestFactory(() -> requestFactory)
                .rootUri(mmdCommonCfg.getUrl())
                .defaultHeader("BI_CLIENT_APP_ID", "TDQ_hourly")
                .defaultHeader("BI_CLIENT_APP_KEY", "tdQ_MMD_AweS0ME")
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
}