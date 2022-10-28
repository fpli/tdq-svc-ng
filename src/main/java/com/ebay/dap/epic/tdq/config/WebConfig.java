package com.ebay.dap.epic.tdq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedHeaders("*")
            .exposedHeaders("X-Auth-Expired-Time", "X-Auth-Token", "X-Auth-Username")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedOrigins("*");
      }
    };
  }

}
