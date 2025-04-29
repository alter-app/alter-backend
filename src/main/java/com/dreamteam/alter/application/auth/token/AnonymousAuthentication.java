package com.dreamteam.alter.application.auth.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class AnonymousAuthentication extends AbstractAuthenticationToken {

    private static final GrantedAuthority ANONYMOUS_ROLE = new SimpleGrantedAuthority("ROLE_ANONYMOUS");

    public AnonymousAuthentication() {
        super(List.of(ANONYMOUS_ROLE));

        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

}
