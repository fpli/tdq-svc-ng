package com.ebay.dap.epic.tdq.config;

import com.ebay.dap.epic.tdq.common.util.YamlPropertySourceFactory;
import com.ebay.dap.epic.tdq.service.mmd.CustomParams;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Configuration
@ConfigurationProperties(prefix = "mmdjson-custom-params")
@PropertySource(value = "classpath:mmd.yaml", factory = YamlPropertySourceFactory.class)
public class AllMetricsCustParams {
    Map<String, CustomParams> metricsCustParams;
}