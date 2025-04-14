package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.port.outbound.AppleAuthClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class AppleAuthClientImpl implements AppleAuthClient {

    @Value("${apple.client_id}")
    private String appleClientId;

    @Value("${apple.team_id}")
    private String appleTeamId;

    @Value("${apple.login_key}")
    private String appleLoginKey;

    @Value("${apple.redirect_uri}")
    private String appleRedirectUri;

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PrivateKey applePrivateKey;

    public AppleAuthClientImpl(
        RestTemplate restTemplate,
        @Value("${apple.private_key}") String encodedPrivateKey,
        ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.applePrivateKey = decodePrivateKey(encodedPrivateKey);
    }

    @Override
    public SocialTokenResponseDto exchangeCodeForToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", appleTeamId);
        params.add("client_secret", generateClientSecret());
        params.add("code", authorizationCode);
        params.add("redirect_uri", appleRedirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(APPLE_TOKEN_URL, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return new SocialTokenResponseDto(
                jsonNode.get("refresh_token").asText(),
                jsonNode.get("id_token").asText()
            );
        } catch (HttpClientErrorException e) {
            throw new CustomException(ErrorCode.SOCIAL_TOKEN_EXPIRED);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public JsonNode fetchPublicKeys() {
        try {
            ResponseEntity<String> keyResponse = restTemplate.getForEntity(APPLE_KEYS_URL, String.class);
            return objectMapper.readTree(keyResponse.getBody());
        } catch (HttpClientErrorException | JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private PrivateKey decodePrivateKey(String encodedPrivateKey) {
        try {
            byte[] fileBytes = Base64.getDecoder().decode(encodedPrivateKey);
            String originalP8Content = new String(fileBytes);

            String keyContent = originalP8Content
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateClientSecret() {
        return Jwts.builder()
            .header()
            .add("kid", appleLoginKey)
            .add("alg", "ES256")
            .and()
            .issuer(appleTeamId)
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(15552000)))
            .audience().add("https://appleid.apple.com").and()
            .subject(appleClientId)
            .signWith(applePrivateKey, Jwts.SIG.ES256)
            .compact();
    }

}
