package com.ebay.dap.epic.tdq.common;

import static com.ebay.dap.epic.tdq.config.DataSourceEnvPostProcessor.CUR_ENV;

public class Constants {

    public static final String X_AUTH_TOKEN = "X-Auth-Token";
    public static final String X_AUTH_USERNAME = "X-Auth-Username";
    public static final String UTF_8 = "utf-8";
    public static final String APPLICATION_JSON = "application/json";

    public static final String QA_PROFILE = "QA";
    public static final String PRODUCTION_PROFILE = "Production";
    public static final String INTEGRATION_TEST_PROFILE = "it";

    public static final String ROLE_API_USER = "API_USER";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static boolean isProd(){
         return CUR_ENV.equals("prod");
    }

}
