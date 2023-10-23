package com.ebay.dap.epic.tdq.config;

import com.ebay.dap.epic.tdq.common.util.DateTimeUtils;
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
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        List<Converter<?,?>> converters = new ArrayList<>();
        converters.add(LongToLocalDateTimeConverter.INSTANCE);
        converters.add(IsoStringToLocalDateTimeConverter.INSTANCE);

        return new ElasticsearchCustomConversions(converters);
    }

    @ReadingConverter
    enum LongToLocalDateTimeConverter implements Converter<Long, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(Long source) {
            return DateTimeUtils.tsToLocalDateTime(source);
        }
    }

    @ReadingConverter
    enum IsoStringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(String source) {
            Instant instant = Instant.parse(source);
            return DateTimeUtils.instantToLocalDateTime(instant);
        }
    }
}
