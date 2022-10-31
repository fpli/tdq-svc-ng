package com.ebay.dap.epic.tdq;

import com.ebay.fount.fountclient.DecryptionDirective;
import com.ebay.fount.managedfountclient.ManagedFountClient;
import com.ebay.fount.managedfountclient.ManagedFountClientBuilder;


public class FountClientTest {

    public static void main(String[] args) throws Exception {

//        System.setProperty("http.proxyHost", "c2syubi.vip.ebay.com");
//        System.setProperty("http.proxyPort", "8080");
//        System.setProperty("https.proxyHost", "");
//        System.setProperty("https.proxyPort", "");


        ManagedFountClient fountClientprod = (ManagedFountClient) new ManagedFountClientBuilder()
//        .appName(applicatioName)
//        .addFountDatasourceChangeListener()
                .decryptionDirective(DecryptionDirective.DECRYPT)
                .dbEnv("staging")
                .logicalDsNames("tdqmyhost")
                .build();
        System.out.println(fountClientprod.getDatasourceConfig("tdqmyhost"));

    }
}
