package com.dreamteam.alter.application.auth.token;

import com.dreamteam.alter.domain.auth.entity.Authorization;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

public class RefreshTokenAuthentication extends AbstractAuthenticationToken {

    private static final GrantedAuthority ANONYMOUS_ROLE = new SimpleGrantedAuthority("ROLE_ANONYMOUS");

    private final String token;
    private final Authorization authorization;

    public RefreshTokenAuthentication(
        String token
    ) {
        super(List.of(ANONYMOUS_ROLE));
        this.token = token;
        this.authorization = null;
//        setAuthenticated(false);
    }

    public RefreshTokenAuthentication(
        String token,
        Authorization authorization
    ) {
        super(Collections.singletonList(getAuthority(authorization)));
        this.token = token;
        this.authorization = authorization;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public String getPrincipal() {
        return token;
    }

    @Override
    public Authorization getDetails() {
        return authorization;
    }

    private static SimpleGrantedAuthority getAuthority(Authorization authorization) {
        return new SimpleGrantedAuthority(authorization.getUser().getRole().toString());
    }

}
