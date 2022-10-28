package com.ebay.dap.epic.tdq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ConditionalOnExpression(value = "${useSwagger:false}")
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
public class Swagger2Config {

  @Bean
  public Docket createRestApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(new ApiInfoBuilder()
            .description("Sojourner Service API v1.0")
            .title("soj-service-api")
            .version("1.0.0")
            .build())
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.ebay.sojourner.service.web.controller"))
        .paths(PathSelectors.any())
        .build()
        .pathMapping("/");
  }
}
