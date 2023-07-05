package com.ebay.dap.epic.tdq.config;

import com.ebay.dap.epic.tdq.common.Profile;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

//FIXME: Spring RestTemplate is deprecated, use other alternatives
@Deprecated
@Slf4j
@Configuration
public class RestTemplateConfig {

    private final MMDCommonCfg mmdCommonCfg;

    @Autowired
    private C2SProxyConfig proxyConfig;

    public RestTemplateConfig(MMDCommonCfg mmdCommonCfg) {
        this.mmdCommonCfg = mmdCommonCfg;
    }

    @Bean("mmdRestTemplate")
    public RestTemplate mmdRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                        Environment env) {
        log.info("Initialize mmdRestTemplate");
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().disableCookieManagement();

        // set proxy if the 'c2sproxy' profile is enabled
        if (env.acceptsProfiles(Profiles.of(Profile.C2S_PROXY))) {
            final String C2S_PROXY_HOST = proxyConfig.getProxyHost();
            final int C2S_PROXY_PORT = proxyConfig.getProxyPort();
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(C2S_PROXY_HOST, C2S_PROXY_PORT),
                    new UsernamePasswordCredentials(proxyConfig.getProxyUsername(), proxyConfig.getProxyPassword())
            );
            httpClientBuilder.setProxy(new HttpHost(C2S_PROXY_HOST, C2S_PROXY_PORT, "http"))
                    .setDefaultCredentialsProvider(credsProvider);
        }

        requestFactory.setHttpClient(httpClientBuilder.build());
        return restTemplateBuilder.requestFactory(() -> requestFactory)
                .rootUri(mmdCommonCfg.getUrl())
                .defaultHeader("BI_CLIENT_APP_ID", "TDQ_hourly")
                .defaultHeader("BI_CLIENT_APP_KEY", "tdQ_MMD_AweS0ME")
                .setConnectTimeout(Duration.ofSeconds(15))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
}