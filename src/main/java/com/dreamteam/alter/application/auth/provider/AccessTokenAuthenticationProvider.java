package com.dreamteam.alter.application.auth.provider;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.CustomJwtSubject;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.adapter.outbound.auth.external.AuthorizationRepositoryImpl;
import com.dreamteam.alter.application.auth.service.AuthService;
import com.dreamteam.alter.application.auth.token.AccessTokenAuthentication;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.exception.ExpiredAuthException;
import com.dreamteam.alter.domain.auth.exception.InternalAuthException;
import com.dreamteam.alter.domain.auth.exception.InvalidAuthException;
import com.dreamteam.alter.domain.auth.exception.RevokedAuthException;
import com.dreamteam.alter.domain.auth.type.AuthorizationStatus;
import com.dreamteam.alter.domain.auth.type.TokenType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;

@Component
@Transactional
public class AccessTokenAuthenticationProvider extends AbstractJwtAuthenticationProvider implements
                                                                                         AuthenticationProvider {

    private final AuthService authService;
    private final AuthorizationRepositoryImpl authorizationRepository;
    private final StringRedisTemplate redisTemplate;

    public AccessTokenAuthenticationProvider(
        @Value("${jwt.secret}") String jwtSecret,
        ObjectMapper objectMapper,
        AuthService authService,
        AuthorizationRepositoryImpl authorizationRepository,
        StringRedisTemplate redisTemplate
    ) {
        super(jwtSecret, objectMapper);
        this.authService = authService;
        this.authorizationRepository = authorizationRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return authenticate((AccessTokenAuthentication) authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AccessTokenAuthentication.class.isAssignableFrom(authentication);
    }

    private Authentication authenticate(AccessTokenAuthentication authentication) {
        Instant now = Instant.now();
        String accessToken = (String) authentication.getPrincipal();

        CustomJwtSubject subject = parseJwt(accessToken);
        if (BooleanUtils.isFalse(TokenType.ACCESS.equals(subject.getType()))) {
            return authentication;
        }

        Authorization authorization = getAuthorization(subject);
        if (ObjectUtils.isEmpty(authorization)) {
            Authorization dbAuthorization = getAuthorizationFromDB(subject.getAuthorizationId());

            if (AuthorizationStatus.REVOKED.equals(dbAuthorization.getStatus())) {
                throw new RevokedAuthException();
            }

            return authentication;
        }

        if (!now.isBefore(authorization.getAccessTokenExpiredAt())) {
            throw new ExpiredAuthException();
        }

        LoginUserDto userDetail = LoginUserDto.of(authorization);
        return new AccessTokenAuthentication(
            accessToken,
            userDetail,
            Collections.singletonList(new SimpleGrantedAuthority(authorization.getUser()
                .getRole()
                .toString()))
        );
    }

    private Authorization getAuthorization(CustomJwtSubject subject) {
        String key = authService.buildKey(subject.getScope(), subject.getUserId(), subject.getAuthorizationId());
        try {
            return objectMapper.readValue(
                redisTemplate.opsForValue().get(key),
                Authorization.class
            );
        } catch (JsonProcessingException e) {
            throw new InternalAuthException();
        }
    }

    private Authorization getAuthorizationFromDB(String authorizationId) {
        return authorizationRepository.findById(authorizationId)
            .orElseThrow(InvalidAuthException::new);
    }

}
