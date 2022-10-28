package com.ebay.dap.epic.tdq.security;

import com.ebay.dap.epic.tdq.security.keystone.KeystoneAuthRequest;
import com.ebay.dap.epic.tdq.security.keystone.KeystoneClient;
import com.ebay.dap.epic.tdq.security.keystone.KeystonePasswordCredentials;
import com.ebay.dap.epic.tdq.security.keystone.KeystoneRequest;
import com.ebay.dap.epic.tdq.security.keystone.response.KeystoneAuthResponse;
import com.ebay.dap.epic.tdq.security.model.Role;
import com.ebay.dap.epic.tdq.security.model.User;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Slf4j
public class KeystoneAuthenticationProvider implements AuthenticationProvider {

  private final KeystoneClient keystoneClient;
  private final UserService userService;

  public KeystoneAuthenticationProvider(KeystoneClient keystoneClient, UserService userService) {
    this.keystoneClient = keystoneClient;
    this.userService = userService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    Preconditions.checkNotNull(authentication);
    Preconditions.checkNotNull(authentication.getName());
    Preconditions.checkNotNull(authentication.getCredentials());

    String name = authentication.getName();
    String password = authentication.getCredentials().toString();

    KeystonePasswordCredentials passwordCredentials = KeystonePasswordCredentials.builder()
                                                                                 .username(name)
                                                                                 .password(password)
                                                                                 .build();

    KeystoneRequest request = new KeystoneRequest(new KeystoneAuthRequest(passwordCredentials));

    log.info("Authenticating user {} via keystone", name);
    KeystoneAuthResponse keystoneResponse = keystoneClient.auth(request);

    // get user object from DB
    User user = userService.updateUserLastLoginDate(name);

    List<SimpleGrantedAuthority> simpleGrantedAuthorities = user.getRoles()
                                               .stream()
                                               .map(Role::getAuthority)
                                               .collect(Collectors.toList());

    return new UsernamePasswordAuthenticationToken(name, null, simpleGrantedAuthorities);
  }

  @Override
  public boolean supports(Class<?> clazz) {
    // set to true as we only have Keystone authentication
    return true;
  }
}
