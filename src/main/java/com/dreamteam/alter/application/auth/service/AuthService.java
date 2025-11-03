package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.CustomJwtSubject;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.entity.Authorization;
import com.dreamteam.alter.domain.auth.port.outbound.AuthorizationQueryRepository;
import com.dreamteam.alter.domain.auth.port.outbound.AuthorizationRepository;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.auth.type.TokenType;
import com.dreamteam.alter.domain.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
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
    private final AuthorizationQueryRepository authorizationQueryRepository;

    public AuthService(
        @Value("${jwt.secret}") String secretKey,
        StringRedisTemplate stringRedisTemplate,
        ObjectMapper objectMapper,
        AuthorizationRepository authorizationRepository,
        AuthorizationQueryRepository authorizationQueryRepository
    ) {
        this.jwtSecretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.redisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.authorizationRepository = authorizationRepository;
        this.authorizationQueryRepository = authorizationQueryRepository;
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
                    user.getId()
                ),
                accessTokenExpiredAt
            );

            String refreshToken = generateJwt(
                CustomJwtSubject.of(
                    authorizationId,
                    scope,
                    TokenType.REFRESH,
                    user.getId()
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
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void saveAuthorization(Authorization authorization) throws JsonProcessingException {
        // DB
        Authorization savedAuthorization = authorizationRepository.save(authorization);

        String key = buildKey(
            authorization.getScope(),
            authorization.getUser().getId(),
            authorization.getId()
        );

        // Redis
        redisTemplate.opsForValue().set(
            key,
            objectMapper.writeValueAsString(savedAuthorization),
            accessTokenExpirationTime,
            TimeUnit.MILLISECONDS
        );
    }

    public String buildKey(TokenScope scope, Long userId, String authorizationId) {
        return String.join(":",
            AUTHORIZATION_PREFIX,
            String.valueOf(scope),
            String.valueOf(userId),
            authorizationId
        );
    }

    public void revokeAllExistingAuthorizations(User user) {
        List<Authorization> existingAuthorizations = authorizationQueryRepository.findAllByUser(user);

        if (existingAuthorizations.isEmpty()) {
            return;
        }

        // 사용자의 모든 인가 정보 무효화
        for (Authorization authorization : existingAuthorizations) {
            authorization.revoke();
            deleteAuthorizationFromRedis(authorization.getScope(), user.getId(), authorization.getId());
        }
    }

    private void deleteAuthorizationFromRedis(TokenScope scope, Long userId, String authorizationId) {
        redisTemplate.delete(buildKey(scope, userId, authorizationId));
    }

    /**
     * Firebase 커스텀 토큰 생성
     * uid를 Firebase uid로 사용하여 토큰 생성
     *
     * @param uid Firebase uid (User.id 또는 ManagerUser.id를 String으로 변환한 값)
     * @return Firebase 커스텀 토큰
     * @throws CustomException Firebase 토큰 생성 실패 시
     */
    public String generateFirebaseCustomToken(String uid) {
        try {
            return FirebaseAuth.getInstance().createCustomToken(uid);
        } catch (FirebaseAuthException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Firebase 커스텀 토큰 생성 중 오류 발생.");
        }
    }
}
