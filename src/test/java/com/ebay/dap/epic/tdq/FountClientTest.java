package com.ebay.dap.epic.tdq;

import com.ebay.fount.fountclient.DecryptionDirective;
import com.ebay.fount.fountclient.FountDatasourceConfig;
import com.ebay.fount.managedfountclient.ManagedFountClient;
import com.ebay.fount.managedfountclient.ManagedFountClientBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class FountClientTest {

    @Test
    void test() {
        final String ds = "tdqmyhost";

        ManagedFountClient fountClient = (ManagedFountClient) new ManagedFountClientBuilder()
                //.appName(applicatioName)
                //.addFountDatasourceChangeListener()
                .decryptionDirective(DecryptionDirective.DECRYPT)
                .dbEnv("staging")
                .logicalDsNames(ds)
                .build();
        FountDatasourceConfig datasourceConfig = fountClient.getDatasourceConfig(ds);
        System.out.println(datasourceConfig);
        assertThat(datasourceConfig.getUrl()).isNotNull();
        assertThat(datasourceConfig.getUser()).isNotNull();
        assertThat(datasourceConfig.getPassword()).isNotNull();
    }

}
