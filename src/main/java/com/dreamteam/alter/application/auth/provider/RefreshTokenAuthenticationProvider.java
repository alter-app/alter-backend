package com.dreamteam.alter.application.auth.provider;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.CustomJwtSubject;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.adapter.outbound.auth.external.AuthorizationRepositoryImpl;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.auth.token.RefreshTokenAuthentication;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.exception.ExpiredAuthException;
import com.dreamteam.alter.domain.auth.exception.InvalidAuthException;
import com.dreamteam.alter.domain.auth.exception.RevokedAuthException;
import com.dreamteam.alter.domain.auth.type.AuthorizationStatus;
import com.dreamteam.alter.domain.auth.type.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;

@Component
public class RefreshTokenAuthenticationProvider extends AbstractJwtAuthenticationProvider implements
                                                                                          AuthenticationProvider {

    private final AuthorizationRepositoryImpl authorizationRepository;

    public RefreshTokenAuthenticationProvider(
        @Value("${jwt.secret}") String jwtSecret,
        ObjectMapper objectMapper,
        AuthorizationRepositoryImpl authorizationRepository
    ) {
        super(jwtSecret, objectMapper);
        this.authorizationRepository = authorizationRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return authenticate((RefreshTokenAuthentication) authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return RefreshTokenAuthentication.class.isAssignableFrom(authentication);
    }

    private Authentication authenticate(RefreshTokenAuthentication authentication) {
        Instant now = Instant.now();
        String refreshToken = authentication.getPrincipal();

        CustomJwtSubject subject = parseJwt(refreshToken);
        if (BooleanUtils.isFalse(TokenType.REFRESH.equals(subject.getType()))) {
            return authentication;
        }

        Authorization authorization = authorizationRepository.findById(subject.getAuthorizationId())
            .orElseThrow(InvalidAuthException::new);

        if (BooleanUtils.isFalse(now.isBefore(authorization.getRefreshTokenExpiredAt()))
            || AuthorizationStatus.EXPIRED.equals(authorization.getStatus())) {
            throw new ExpiredAuthException();
        }


        if (AuthorizationStatus.REVOKED.equals(authorization.getStatus())) {
            throw new RevokedAuthException();
        }

        RefreshTokenAuthentication authorizedToken = new RefreshTokenAuthentication(refreshToken, authorization);
        authorizedToken.setAuthenticated(true);

        return authorizedToken;
    }

}
