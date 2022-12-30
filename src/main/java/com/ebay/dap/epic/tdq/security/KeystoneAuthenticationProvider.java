package com.ebay.dap.epic.tdq.security;

import com.ebay.dap.epic.tdq.security.model.User;
import com.ebay.keystone.KeystoneClient;
import com.ebay.keystone.KeystoneResponse;
import com.ebay.keystone.exception.KeystoneAuthFailureException;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

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

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        KeystoneResponse keystoneResponse;

        try {
            log.info("Authenticating user {} via keystone", username);
            keystoneResponse = keystoneClient.auth(username, password);
        } catch (KeystoneAuthFailureException e) {
            throw new RuntimeException(e);
        }

        if (keystoneResponse.isAuthenticated()) {

            // get user object from DB
            User user = userService.login(username);
            List<SimpleGrantedAuthority> grantedAuthorities = user.getRoles()
                    .stream()
                    .map(e -> new SimpleGrantedAuthority(e.getRole()))
                    .toList();

            return new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
        } else {
            throw new KeystoneAuthenticationException(keystoneResponse.getErrorResponse().getError().getMessage());
        }

    }

    @Override
    public boolean supports(Class<?> clazz) {
        // set to true as we only have Keystone authentication
        return true;
    }
}
