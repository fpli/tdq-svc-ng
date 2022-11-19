package com.ebay.dap.epic.tdq.security;

import com.ebay.dap.epic.tdq.common.Constants;
import com.ebay.dap.epic.tdq.security.model.UserDto;
import com.ebay.dap.epic.tdq.web.protocal.response.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
public class LoginProcessingFilter extends AbstractAuthenticationProcessingFilter {

  private final ObjectMapper objectMapper;
  public static final String loginPath = "/api/login";

  public LoginProcessingFilter(AuthenticationManager authenticationManager,
                               ObjectMapper objectMapper) {
    super(new AntPathRequestMatcher(loginPath, "POST", false));
    this.setAuthenticationManager(authenticationManager);
    this.objectMapper = objectMapper;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, IOException {

    final UserDto user = objectMapper.readValue(request.getInputStream(), UserDto.class);
    log.debug("Attempt to authenticate user: {}", user.getUsername());
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
    return this.getAuthenticationManager().authenticate(authenticationToken);
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            AuthenticationException e) throws IOException {

    SecurityContextHolder.clearContext();
    ApiError apiError;

    log.error("User authentication failed");
    // set response header to 401
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    apiError = new ApiError(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
    apiError.setPath(loginPath);

    response.setCharacterEncoding(Constants.UTF_8);
    response.setContentType(Constants.APPLICATION_JSON);
    PrintWriter printWriter = response.getWriter();
    printWriter.append(objectMapper.writeValueAsString(apiError));
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                          FilterChain chain, Authentication authResult) {

    log.debug("Authentication success. Updating SecurityContextHolder to contain: " + authResult);

    SecurityContextHolder.getContext().setAuthentication(authResult);
    List<String> roles = authResult.getAuthorities()
                                   .stream()
                                   .map(GrantedAuthority::getAuthority)
                                   .collect(Collectors.toList());
    Map<String, Object> claims = Maps.newHashMap();
    claims.put("roles", roles);

    String token = JwtUtils.generateToken(claims, authResult.getName());

    response.addHeader(Constants.X_AUTH_TOKEN, token);
    response.addHeader(Constants.X_AUTH_USERNAME, authResult.getName());
    response.setStatus(HttpStatus.OK.value());
  }
}
