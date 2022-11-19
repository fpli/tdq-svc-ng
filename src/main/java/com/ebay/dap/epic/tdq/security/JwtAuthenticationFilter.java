package com.ebay.dap.epic.tdq.security;

import com.ebay.dap.epic.tdq.common.Constants;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                  FilterChain filterChain) throws ServletException, IOException {

    log.debug("Authenticating JWT token");
    Authentication authentication = getAuthentication(httpRequest);
    if (authentication != null) {
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(httpRequest, httpResponse);
  }

  private Authentication getAuthentication(HttpServletRequest httpRequest) {
    final String token = httpRequest.getHeader(Constants.X_AUTH_TOKEN);
    final String username = httpRequest.getHeader(Constants.X_AUTH_USERNAME);

    if (token != null) {
      // 1. For api user, do basic auth
      if (username != null && username.equals("tdq-api-user") && token.startsWith("Basic ")) {
        String s = new String(Base64.getDecoder().decode(token.split(" ")[1]));
        if (s.split(":")[0].equals("tdq") && s.split(":")[1].equals("tdq")) {
          return new UsernamePasswordAuthenticationToken(username, null,
                                                         AuthorityUtils.createAuthorityList(Constants.ROLE_API_USER));
        }
        return null;
      }

      // 2. For human user, do jwt auth
      try {
        Claims claims = JwtUtils.parseToken(token);
        List<String> roles = (List<String>) claims.get("roles", List.class);
        Boolean expired = JwtUtils.isExpired(token);
        if (!expired) {
          return new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                                                         AuthorityUtils.createAuthorityList(roles.toArray(new String[0])));
        }
      } catch (Exception e) {
        log.error("Cannot validate jwt token", e);
        return null;
      }
    }
    return null;
  }
}
