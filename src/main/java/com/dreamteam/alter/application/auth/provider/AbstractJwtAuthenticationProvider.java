package com.dreamteam.alter.application.auth.provider;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.CustomJwtSubject;
import com.dreamteam.alter.domain.auth.exception.ExpiredAuthException;
import com.dreamteam.alter.domain.auth.exception.InternalAuthException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public abstract class AbstractJwtAuthenticationProvider implements AuthenticationProvider {

    protected final SecretKey jwtSecretKey;
    protected final ObjectMapper objectMapper;

    public AbstractJwtAuthenticationProvider(
        @Value("${jwt.secret}") String secretKey,
        ObjectMapper objectMapper
    ) {
        this.jwtSecretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.objectMapper = objectMapper;
    }

    protected CustomJwtSubject parseJwt(String token) throws ExpiredJwtException {
        try {
            Claims claims = checkJwt(token).getPayload();
            return objectMapper.readValue(claims.getSubject(), CustomJwtSubject.class);
        } catch (ExpiredJwtException e) {
            throw new ExpiredAuthException();
        } catch (Exception e) {
            throw new InternalAuthException();
        }
    }

    protected Jws<Claims> checkJwt(String token) throws ExpiredJwtException {
        try {
            return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token);
        } catch (
            UnsupportedJwtException
            | MalformedJwtException
            | IllegalArgumentException e
        ) {
            throw new InternalAuthException();
        }
    }

}
