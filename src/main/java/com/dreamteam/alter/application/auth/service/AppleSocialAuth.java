package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.domain.auth.port.outbound.AppleAuthClient;
import com.dreamteam.alter.domain.auth.port.outbound.AppleRefreshTokenRepository;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AppleSocialAuth extends AbstractSocialAuth {

    private final AppleAuthClient appleAuthClient;
    private final AppleRefreshTokenRepository appleRefreshTokenEntityRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected SocialTokenResponseDto exchangeCodeForToken(String authorizationCode) {
        return appleAuthClient.exchangeCodeForToken(authorizationCode);
    }

    @Override
    protected SocialUserInfo getUserInfo(SocialTokenResponseDto socialTokens) {
        Jws<Claims> claimsJws = verifyIdentityToken(socialTokens.getIdentityToken());
        Claims claims = claimsJws.getPayload();
        String id = claims.getSubject();
        String email = claims.get("email", String.class);

        // RefreshToken 저장
        saveOrUpdateRefreshToken(id, socialTokens.getRefreshToken());

        return new SocialUserInfo(SocialProvider.APPLE, id, email);
    }

    @Override
    public boolean supports(SocialProvider provider) {
        return SocialProvider.APPLE.equals(provider);
    }

    private Jws<Claims> verifyIdentityToken(String identityToken) {
        try {
            // 1. JWT 토큰 헤더에서 kid 값 추출
            String[] jwtParts = identityToken.split("\\.");
            if (jwtParts.length < 2) {
                throw new IllegalArgumentException("유효한 JWT 형식이 아닙니다");
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(jwtParts[0]));
            JsonNode headerNode = objectMapper.readTree(headerJson);
            String tokenKid = headerNode.get("kid").asText();

            // 2. Apple에서 공개 키 목록 조회
            JsonNode keysNode = appleAuthClient.fetchPublicKeys();

            // 3. kid와 일치하는 키 찾기
            JsonNode matchingKey = null;
            for (JsonNode key : keysNode.get("keys")) {
                if (key.has("kid") && key.get("kid").asText().equals(tokenKid)) {
                    matchingKey = key;
                    break;
                }
            }

            if (matchingKey == null) {
                throw new RuntimeException("토큰의 kid와 일치하는 Apple 공개 키를 찾을 수 없습니다: " + tokenKid);
            }

            String n = matchingKey.get("n").asText();
            String e = matchingKey.get("e").asText();
            PublicKey publicKey = generatePublicKey(n, e);

            return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(identityToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Apple identity token", e);
        }
    }

    private PublicKey generatePublicKey(String n, String e) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
            byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate Apple public key", ex);
        }
    }

    private void saveOrUpdateRefreshToken(String socialId, String refreshToken) {
        appleRefreshTokenEntityRepository.saveOrUpdate(socialId, refreshToken);
    }

}
