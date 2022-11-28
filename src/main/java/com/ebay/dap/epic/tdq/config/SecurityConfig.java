package com.ebay.dap.epic.tdq.config;

import com.ebay.dap.epic.tdq.security.JwtAuthenticationFilter;
import com.ebay.dap.epic.tdq.security.KeystoneAuthenticationProvider;
import com.ebay.dap.epic.tdq.security.LoginProcessingFilter;
import com.ebay.dap.epic.tdq.security.RestAuthenticationEntryPoint;
import com.ebay.dap.epic.tdq.security.UserService;
import com.ebay.keystone.KeystoneClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true)
public class SecurityConfig {

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${global.web.security.enabled:true}")
  public boolean GLOBAL_WEB_SECURITY_ENABLED;


  @Bean
  public AuthenticationProvider authenticationProvider(KeystoneClient keystoneClient, UserService userService) throws Exception {
    return new KeystoneAuthenticationProvider(keystoneClient, userService);
  }


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationConfiguration authConfig) throws Exception {
    http
        .cors().and()
        // we don't need CSRF because we store token in header
        .csrf().disable()
        // disable cache control
        .headers().cacheControl().disable().and()
        // don't create session
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        // disable basic auth
        .httpBasic().disable()

        .authorizeRequests()

        // allow anonymous POSTs to login
        .antMatchers(HttpMethod.POST, "/api/login").permitAll()

        .antMatchers("/api/**").authenticated().and()

        // custom Json based authentication by POST of {"username":"<name>","password":"<password>"}
        // which sets the token header upon authentication
        .addFilterBefore(new LoginProcessingFilter(authConfig.getAuthenticationManager(), objectMapper), UsernamePasswordAuthenticationFilter.class)

        // custom Jwt based authentication based on the header previously given to the client
        .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

        .exceptionHandling().authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper));

    return http.build();
  }


  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() throws Exception {
    if (GLOBAL_WEB_SECURITY_ENABLED) {
      return (web) -> web.ignoring().antMatchers(HttpMethod.GET,
                                                 "/api/test",
                                                 "/v2/api-docs",
                                                 "/swagger-resources/**",
                                                 "/webjars/**",
                                                 "/admin/actuator/**");

    } else {
      return (web) -> web.ignoring().antMatchers("/**");
    }
  }

}
