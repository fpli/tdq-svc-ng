package com.ebay.dap.epic.tdq.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class UserProxyConfig {

    @Value("${PROXY_HOST:c2sproxy.vip.ebay.com}")
    private String proxyHost;

    @Value("${PROXY_PORT:8080}")
    private int proxyPort;

    @Value("${PROXY_USER:-}")
    private String proxyUsername;

    @Value("${PROXY_PASSWORD:-}")
    private String proxyPassword;

}
