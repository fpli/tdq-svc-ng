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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;

import static com.ebay.dap.epic.tdq.common.Profile.C2S_PROXY;

@Slf4j
@Configuration
public class ElasticSearchConfig {

    @Autowired
    private UserProxyConfig proxyConfig;

    @Autowired
    private ProntoConfig prontoEnv;

    @Bean
    public RestHighLevelClient restHighLevelClient(ConfigurableEnvironment env) {
        RestHighLevelClient restHighLevelClient;
        //FIXME: remove the hard-coded url endpoint
        HttpHost httpHost = new HttpHost("estdq-datalvs.vip.ebay.com", 443, "https");
        RestClientBuilder builder = RestClient.builder(httpHost);
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        if (env.acceptsProfiles(Profiles.of(C2S_PROXY))) {
            if (StringUtils.isNotBlank(this.prontoEnv.getHostname())) {
                HttpHost proxy = new HttpHost(proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
                credentialsProvider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(proxyConfig.getProxyUsername(), proxyConfig.getProxyPassword()));
                credentialsProvider.setCredentials(new AuthScope(httpHost), new UsernamePasswordCredentials(this.prontoEnv.getUsername(), this.prontoEnv.getPassword()));
                builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setProxy(proxy).setDefaultCredentialsProvider(credentialsProvider));
            }
        } else {
            credentialsProvider.setCredentials(new AuthScope(httpHost), new UsernamePasswordCredentials(this.prontoEnv.getUsername(), this.prontoEnv.getPassword()));
            builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }
        restHighLevelClient = new RestHighLevelClient(builder);

        return restHighLevelClient;
    }

}
