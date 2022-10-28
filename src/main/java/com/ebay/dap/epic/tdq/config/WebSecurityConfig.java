package com.ebay.dap.epic.tdq.config;

import com.ebay.dap.epic.tdq.security.JwtAuthenticationFilter;
import com.ebay.dap.epic.tdq.security.RestAuthenticationEntryPoint;
import com.ebay.dap.epic.tdq.security.KeystoneAuthenticationProvider;
import com.ebay.dap.epic.tdq.security.LoginProcessingFilter;
import com.ebay.dap.epic.tdq.security.UserService;
import com.ebay.dap.epic.tdq.security.keystone.KeystoneClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  @Qualifier("keystoneRestTemplate")
  private RestTemplate restTemplate;

  @Autowired
  private UserService userService;

  @Value("${global.web.security.enabled:true}")
  public boolean GLOBAL_WEB_SECURITY_ENABLED;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(new KeystoneAuthenticationProvider(new KeystoneClient(restTemplate), userService));
  }


  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        // @formatter:off

        .cors().and()

        // we don't need CSRF because we store token in header
        .csrf().disable()

        .headers().cacheControl().disable().and()

        // don't create session
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

        .authorizeRequests()

        // allow anonymous POSTs to login
        .antMatchers(HttpMethod.POST, "/api/login").permitAll()

        .antMatchers("/api/**").authenticated().and()

        // custom Json based authentication by POST of {"username":"<name>","password":"<password>"}
        // which sets the token header upon authentication
        .addFilterBefore(new LoginProcessingFilter(authenticationManager(), objectMapper), UsernamePasswordAuthenticationFilter.class)

        // custom Jwt based authentication based on the header previously given to the client
        .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

        .exceptionHandling().authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper));

        // @formatter:on
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    if (GLOBAL_WEB_SECURITY_ENABLED) {
      web.ignoring().antMatchers(HttpMethod.GET,"/api/test");
      web.ignoring().antMatchers(HttpMethod.GET,"/v2/api-docs");
      web.ignoring().antMatchers(HttpMethod.GET,"/swagger-resources/**");
      web.ignoring().antMatchers(HttpMethod.GET,"/webjars/**");
      web.ignoring().antMatchers(HttpMethod.GET,"/admin/actuator/**");
    } else {
      web.ignoring().antMatchers("/**");
    }
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

}
