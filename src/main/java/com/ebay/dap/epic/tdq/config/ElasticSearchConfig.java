package com.ebay.dap.epic.tdq.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;

import static com.ebay.dap.epic.tdq.common.Constants.C2S_PROXY_PROFILE;

@Slf4j
@Configuration
public class ElasticSearchConfig {

    @Value("${proxy.host:c2sproxy.vip.ebay.com}")
    private String proxyHost;

    @Value("${proxy.port:8080}")
    private int proxyPort;

    @Value("${proxy.user:-}")
    private String proxyUsername;

    @Value("${proxy.password:-}")
    private String proxyPassword;

    @Autowired
    private ProntoConfig prontoEnv;

    @Bean
    public RestHighLevelClient restHighLevelClient(ConfigurableEnvironment env) {
        RestHighLevelClient restHighLevelClient = null;
        // FIXME(yxiao6): pronto host needs to be configurable
        HttpHost httpHost = new HttpHost("10.123.170.35", 9200, "http");
        if (env.acceptsProfiles(Profiles.of(C2S_PROXY_PROFILE))) {
            httpHost = new HttpHost("10.123.170.35", 9200, "http");
            RestClientBuilder builder = RestClient.builder(httpHost);
            if (StringUtils.isNotBlank(this.prontoEnv.getHostname())) {
                HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(proxyUsername, proxyPassword));
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.prontoEnv.getUsername(), this.prontoEnv.getPassword()));

                builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setProxy(proxy).setDefaultCredentialsProvider(credentialsProvider));
            }
            restHighLevelClient = new RestHighLevelClient(builder);
        } else {
//            restHighLevelClient = ServiceFactory.getRestHighLevelClient();
            restHighLevelClient = new RestHighLevelClient(RestClient.builder(httpHost));
        }

        log.info("RestHighLevelClient is initialized.");

        return restHighLevelClient;
    }

}
