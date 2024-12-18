package com.ebay.dap.epic.tdq.config;

import com.ebay.dap.epic.tdq.common.FountEnv;
import com.ebay.dap.epic.tdq.common.Profile;
import com.ebay.fount.fountclient.DecryptionDirective;
import com.ebay.fount.fountclient.FountDatasourceConfig;
import com.ebay.fount.managedfountclient.ManagedFountClient;
import com.ebay.fount.managedfountclient.ManagedFountClientBuilder;
import com.google.common.collect.Sets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

public class DataSourceEnvPostProcessor implements EnvironmentPostProcessor {

    private static final String LOGICAL_DS_NAME = "tdqmyhost";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        Set<String> activeProfiles = Sets.newHashSet(environment.getActiveProfiles());
        ManagedFountClient fountClient = null;

        if (activeProfiles.contains(Profile.QA)) {
            fountClient = (ManagedFountClient) new ManagedFountClientBuilder()
                    .decryptionDirective(DecryptionDirective.DECRYPT)
                    .dbEnv(FountEnv.STAGING)
                    .logicalDsNames(LOGICAL_DS_NAME)
                    .build();
        } else if (activeProfiles.contains(Profile.PRODUCTION)) {
            fountClient = (ManagedFountClient) new ManagedFountClientBuilder()
                    .decryptionDirective(DecryptionDirective.DECRYPT)
                    .dbEnv(FountEnv.PROD)
                    .logicalDsNames(LOGICAL_DS_NAME)
                    .build();
        } else if (activeProfiles.contains(Profile.INTEGRATION_TEST)) {
            fountClient = (ManagedFountClient) new ManagedFountClientBuilder()
                    .decryptionDirective(DecryptionDirective.DECRYPT)
                    .dbEnv(FountEnv.STAGING)
                    .logicalDsNames(LOGICAL_DS_NAME)
                    .build();
        }

        if (fountClient != null) {
            FountDatasourceConfig fdsc = fountClient.getDatasourceConfig(LOGICAL_DS_NAME);
            MutablePropertySources propertySources = environment.getPropertySources();
            Map<String, Object> properties = new HashMap<>();

            StringBuilder url = new StringBuilder(fdsc.getUrl());
            if (url.toString().contains("?")) {
                if (!url.toString().contains("useSSL=")) {
                    url.append("&useSSL=false");
                }
            } else {
                // if no parameter set, add below parameters to url
                url.append("?useSSL=false");
            }


            // Spring managed logging system will be initialized only after Spring context is initialized
            // so use System.out.println to log the info.
            System.out.println("Original fount jdbc url is: " + fdsc.getUrl());
            System.out.println("Updated jdbc url is: " + url);
            System.out.println(fdsc.getUser() + "/" + fdsc.getPassword());

            properties.put("spring.datasource.url", url.toString());
            properties.put("spring.datasource.username", fdsc.getUser());
            properties.put("spring.datasource.password", fdsc.getPassword());
            propertySources.addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                    new MapPropertySource("ds", properties));
        }
    }
}