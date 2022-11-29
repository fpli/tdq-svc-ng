package com.ebay.dap.epic.tdq.config;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

import com.ebay.dap.epic.tdq.common.Constants;
import com.ebay.fount.fountclient.DecryptionDirective;
import com.ebay.fount.fountclient.FountDatasourceConfig;
import com.ebay.fount.managedfountclient.ManagedFountClient;
import com.ebay.fount.managedfountclient.ManagedFountClientBuilder;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

public class DataSourceEnvPostProcessor implements EnvironmentPostProcessor {

  private static final String LOGICAL_DS_NAME = "tdqmyhost";
  private static final String STAGING = "staging";
  private static final String PROD = "prod";

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment,
      SpringApplication application) {

    Set<String> activeProfiles = Sets.newHashSet(environment.getActiveProfiles());
    ManagedFountClient fountClient = null;

    if (activeProfiles.contains(Constants.QA_PROFILE)) {
      fountClient = (ManagedFountClient) new ManagedFountClientBuilder()
          .decryptionDirective(DecryptionDirective.DECRYPT)
          .dbEnv(STAGING)
          .logicalDsNames(LOGICAL_DS_NAME)
          .build();
    } else if (activeProfiles.contains(Constants.PRODUCTION_PROFILE)) {
      fountClient = (ManagedFountClient) new ManagedFountClientBuilder()
          .decryptionDirective(DecryptionDirective.DECRYPT)
          .dbEnv(PROD)
          .logicalDsNames(LOGICAL_DS_NAME)
          .build();
    } else if (activeProfiles.contains(Constants.INTEGRATION_TEST_PROFILE)) {
      fountClient = (ManagedFountClient) new ManagedFountClientBuilder()
          .decryptionDirective(DecryptionDirective.DECRYPT)
          .dbEnv(STAGING)
          .logicalDsNames(LOGICAL_DS_NAME)
          .build();
    }

    if (fountClient != null) {
      FountDatasourceConfig fdsc = fountClient.getDatasourceConfig(LOGICAL_DS_NAME);
      MutablePropertySources propertySources = environment.getPropertySources();
      Map<String, Object> properties = new HashMap<>();

      StringBuilder url = new StringBuilder(fdsc.getUrl());
      if (url.toString().contains("?")) {
        url.append("&");
      } else {
        url.append("?");
      }
      url.append("useSSL=False");

      // Spring managed logging system initialized only after Spring context is initialized
      // so use System.out.println to log the info.
      System.out.println(url);
      System.out.println(fdsc.getUser() + "/" + fdsc.getPassword());

      properties.put("spring.datasource.url", url.toString());
      properties.put("spring.datasource.username", fdsc.getUser());
      properties.put("spring.datasource.password", fdsc.getPassword());
      propertySources.addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
          new MapPropertySource("ds", properties));
    }
  }
}