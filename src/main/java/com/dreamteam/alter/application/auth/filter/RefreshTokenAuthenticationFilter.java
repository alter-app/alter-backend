package com.dreamteam.alter.application.auth.filter;

import com.dreamteam.alter.application.auth.token.RefreshTokenAuthentication;
import com.dreamteam.alter.domain.auth.exception.InvalidAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

public class RefreshTokenAuthenticationFilter extends AbstractCustomAuthenticationFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    public RefreshTokenAuthenticationFilter(
        AuthenticationManager authenticationManager,
        AuthenticationEntryPoint authenticationEntryPoint
    ) {
        super(request -> true, authenticationManager, authenticationEntryPoint);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (ObjectUtils.isEmpty(request)) {
            throw new InvalidAuthException();
        }

        Authentication preAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if (ObjectUtils.isNotEmpty(preAuthentication) && preAuthentication.isAuthenticated()) {
            return preAuthentication;
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isBlank(authorization) || BooleanUtils.isFalse(StringUtils.containsIgnoreCase(authorization, BEARER_PREFIX))) {
            return null;
        }

        String refreshToken = StringUtils.replaceIgnoreCase(authorization, BEARER_PREFIX, "");
        RefreshTokenAuthentication refreshTokenAuthentication = new RefreshTokenAuthentication(refreshToken);

        return getAuthenticationManager().authenticate(refreshTokenAuthentication);
    }

}
