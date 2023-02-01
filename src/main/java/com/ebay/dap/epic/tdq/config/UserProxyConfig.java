package com.ebay.dap.epic.tdq.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class UserProxyConfig {

    @Value("${proxy.host:c2sproxy.vip.ebay.com}")
    private String proxyHost;

    @Value("${proxy.port:8080}")
    private int proxyPort;

    @Value("${proxy.user:-}")
    private String proxyUsername;

    @Value("${proxy.password:-}")
    private String proxyPassword;

}
