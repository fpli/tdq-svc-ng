package com.ebay.dap.epic.tdq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pronto")
public class ProntoConfig {
    String scheme;
    String hostname;
    Integer port;
    String apiKey;
    String apiValue;
}
