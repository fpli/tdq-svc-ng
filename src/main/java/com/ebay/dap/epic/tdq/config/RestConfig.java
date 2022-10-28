package com.ebay.dap.epic.tdq.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

  @Value("${keystone.url}")
  public String KEYSTONE_URL;

  @Bean("keystoneRestTemplate")
  public RestTemplate keystoneRestTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.rootUri(KEYSTONE_URL)
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(30))
        .build();
  }

}
