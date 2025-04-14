package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.CustomJwtSubject;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthorizationRepository;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.auth.type.TokenType;
import com.dreamteam.alter.domain.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Value("${jwt.access-token.expiration-time}")
    private long accessTokenExpirationTime;

    @Value("${jwt.refresh-token.expiration-time}")
    private long refreshTokenExpirationTime;

    private static final String AUTHORIZATION_PREFIX = "AUTHORIZATION";

    private final SecretKey jwtSecretKey;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AuthorizationRepository authorizationRepository;

    public AuthService(
        @Value("${jwt.secret}") String secretKey,
        StringRedisTemplate stringRedisTemplate,
        ObjectMapper objectMapper,
        AuthorizationRepository authorizationRepository
    ) {
        this.jwtSecretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.redisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.authorizationRepository = authorizationRepository;
    }

    public String generateJwt(CustomJwtSubject jwtSubject, Instant expiredAt) throws JsonProcessingException {
        return Jwts.builder()
            .subject(objectMapper.writeValueAsString(jwtSubject))
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(expiredAt))
            .signWith(jwtSecretKey)
            .compact();
    }

    public Authorization generateAuthorization(User user, TokenScope scope) {
        String authorizationId = String.valueOf(UUID.randomUUID());
        Instant now = Instant.now();
        Instant accessTokenExpiredAt = now.plusMillis(accessTokenExpirationTime);
        Instant refreshTokenExpiredAt = now.plusMillis(refreshTokenExpirationTime);

        try {
            String accessToken = generateJwt(
                CustomJwtSubject.of(
                    authorizationId,
                    scope,
                    TokenType.ACCESS,
                    String.valueOf(user.getId())
                ),
                accessTokenExpiredAt
            );

            String refreshToken = generateJwt(
                CustomJwtSubject.of(
                    authorizationId,
                    scope,
                    TokenType.REFRESH,
                    String.valueOf(user.getId())
                ),
                refreshTokenExpiredAt
            );

            Authorization authorization = Authorization.create(
                authorizationId,
                user,
                scope,
                accessToken,
                accessTokenExpiredAt,
                refreshToken,
                refreshTokenExpiredAt
            );

            saveAuthorization(authorization);

            return authorization;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveAuthorization(Authorization authorization) throws JsonProcessingException {
        String key = buildKey(authorization);

        // Redis
        redisTemplate.opsForValue().set(
            key,
            objectMapper.writeValueAsString(authorization),
            accessTokenExpirationTime,
            TimeUnit.MILLISECONDS
        );

        // DB
        authorizationRepository.save(authorization);
    }

    private String buildKey(Authorization authorization) {
        return String.join(":",
            AUTHORIZATION_PREFIX,
            String.valueOf(authorization.getScope()),
            String.valueOf(authorization.getUser().getId()),
            authorization.getId()
        );
    }

}
