package com.ebay.dap.epic.tdq.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import static com.ebay.dap.epic.tdq.common.Profile.C2S_PROXY;

@Slf4j
@Configuration
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {

    @Autowired
    private C2SProxyConfig proxyConfig;

    @Autowired
    private ProntoEnvConfig prontoEnv;

    @Autowired
    private ConfigurableEnvironment env;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {

        String hostAndPort = prontoEnv.getHostname() + ":" + prontoEnv.getPort();
        ClientConfiguration.TerminalClientConfigurationBuilder builder =
                ClientConfiguration.builder()
                                   .connectedTo(hostAndPort)
                                   .usingSsl()
                                   .withConnectTimeout(15000) //15s
                                   .withSocketTimeout(30000) //30s
                                   .withBasicAuth(prontoEnv.getUsername(), prontoEnv.getPassword());


        // only enable c2s proxy when the profile is active
        if (env.acceptsProfiles(Profiles.of(C2S_PROXY))) {
            // add c2s proxy configs
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(proxyConfig.getProxyHost(), proxyConfig.getProxyPort()),
                    new UsernamePasswordCredentials(proxyConfig.getProxyUsername(), proxyConfig.getProxyPassword())
            );

            builder.withClientConfigurer(
                    RestClients.RestClientConfigurationCallback.from(clientBuilder -> {
                        clientBuilder.setProxy(new HttpHost(proxyConfig.getProxyHost(), proxyConfig.getProxyPort(), "http"))
                                     .setDefaultCredentialsProvider(credsProvider);
                        return clientBuilder;
                    }));

        }

        final ClientConfiguration clientConfiguration = builder.build();

        return RestClients.create(clientConfiguration).rest();
    }

}
