package com.dreamteam.alter.application.auth.service;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.port.outbound.AppleAuthClient;
import com.dreamteam.alter.domain.auth.port.outbound.AppleRefreshTokenRepository;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AppleSocialAuth extends AbstractSocialAuth {

    private static final String KEY_EMAIL = "email";
    private static final String JWT_DELIMITER_REGEX = "\\.";
    private static final String KEY_KID = "kid";
    private static final String KEYS_PREFIX = "keys";
    private static final String KEY_N = "n";
    private static final String KEY_E = "e";
    private static final String RSA_ALGORITHM = "RSA";

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
        String email = claims.get(KEY_EMAIL, String.class);

        // RefreshToken 저장
        saveOrUpdateRefreshToken(id, socialTokens.getRefreshToken());

        return SocialUserInfo.of(SocialProvider.APPLE, id, email);
    }

    @Override
    public boolean supports(SocialProvider provider) {
        return SocialProvider.APPLE.equals(provider);
    }

    private Jws<Claims> verifyIdentityToken(String identityToken) {
        try {
            // 1. JWT 토큰 헤더에서 kid 값 추출
            String[] jwtParts = identityToken.split(JWT_DELIMITER_REGEX);
            if (jwtParts.length < 2) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(jwtParts[0]));
            JsonNode headerNode = objectMapper.readTree(headerJson);
            String tokenKid = headerNode.get(KEY_KID).asText();

            // 2. Apple에서 공개 키 목록 조회
            JsonNode keysNode = appleAuthClient.fetchPublicKeys();

            // 3. kid와 일치하는 키 찾기
            JsonNode matchingKey = null;
            for (JsonNode key : keysNode.get(KEYS_PREFIX)) {
                if (key.has(KEY_KID) && key.get(KEY_KID).asText().equals(tokenKid)) {
                    matchingKey = key;
                    break;
                }
            }

            if (ObjectUtils.isEmpty(matchingKey)) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            String n = matchingKey.get(KEY_N).asText();
            String e = matchingKey.get(KEY_E).asText();
            PublicKey publicKey = generatePublicKey(n, e);

            return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(identityToken);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private PublicKey generatePublicKey(String n, String e) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
            byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);

            KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
            return kf.generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void saveOrUpdateRefreshToken(String socialId, String refreshToken) {
        appleRefreshTokenEntityRepository.saveOrUpdate(socialId, refreshToken);
    }

}
