package com.dreamteam.alter.application.auth.filter;

import com.dreamteam.alter.application.auth.token.AccessTokenAuthentication;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.exception.InvalidAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.AuthenticationEntryPoint;

public class AccessTokenAuthenticationFilter extends AbstractCustomAuthenticationFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    public AccessTokenAuthenticationFilter(
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

        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isBlank(authorization) || BooleanUtils.isFalse(StringUtils.containsIgnoreCase(authorization, BEARER_PREFIX))) {
            return null;
        }

        String accessToken = StringUtils.replaceIgnoreCase(authorization, BEARER_PREFIX, "");
        AccessTokenAuthentication authenticationToken = new AccessTokenAuthentication(accessToken);

        return getAuthenticationManager().authenticate(authenticationToken);
    }

}
