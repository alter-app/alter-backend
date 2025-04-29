package com.dreamteam.alter.application.auth.token;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class AccessTokenAuthentication extends AbstractAuthenticationToken {

    private final String token;

    public AccessTokenAuthentication(
        String token
    ) {
        super(Collections.emptyList());
        this.token = token;
        setAuthenticated(false);
    }

    public AccessTokenAuthentication(
        String token,
        LoginUserDto loginUser,
        Collection<GrantedAuthority> authorities
    ) {
        super(authorities);
        this.token = token;
        setAuthenticated(true);
        setDetails(loginUser);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

}
