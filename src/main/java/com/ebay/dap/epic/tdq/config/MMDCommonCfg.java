package com.ebay.dap.epic.tdq.config;

import com.ebay.dap.epic.tdq.service.mmd.GlobalConfig;
import com.ebay.dap.epic.tdq.service.mmd.JobParam;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Configuration
@ConfigurationProperties(prefix = "mmd-common-cfg")
public class MMDCommonCfg {
    String url;
    Map<String, String> headParams;
    GlobalConfig globalConfig;
    JobParam jobParam;
}