package com.dreamteam.alter.application.auth.provider;

import com.dreamteam.alter.application.auth.token.AnonymousAuthentication;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class AnonymousAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AnonymousAuthentication.class.isAssignableFrom(authentication);
    }

}
