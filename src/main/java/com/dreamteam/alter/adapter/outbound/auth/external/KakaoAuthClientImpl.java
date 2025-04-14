package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.port.outbound.KakaoAuthClient;
import com.dreamteam.alter.domain.user.type.SocialProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component("KakaoAuthClient")
@RequiredArgsConstructor
public class KakaoAuthClientImpl implements KakaoAuthClient {

    @Value("${kakao.client_id}")
    private String kakaoClientId;

    @Value("${kakao.redirect_uri}")
    private String kakaoRedirectUri;

    private static final String KAKAO_OAUTH_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_API_URL = "https://kapi.kakao.com/v2/user/me";

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SocialTokenResponseDto exchangeCodeForToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        SocialTokenResponseDto socialTokenResponseDto = null;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(KAKAO_OAUTH_URL, request, String.class);

            if (response.getStatusCode()
                .equals(HttpStatus.OK)) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                socialTokenResponseDto = new SocialTokenResponseDto(jsonNode.get("access_token")
                    .asText());
            }
        } catch (HttpClientErrorException e) {
            throw new CustomException(ErrorCode.SOCIAL_TOKEN_EXPIRED);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return socialTokenResponseDto;
    }

    @Override
    public SocialUserInfo getUserInfo(SocialTokenResponseDto socialTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(socialTokens.getAccessToken());

        HttpEntity<String> request = new HttpEntity<>(headers);
        SocialUserInfo socialUserInfo;
        try {
            ResponseEntity<String> response = restTemplate.exchange(KAKAO_USER_API_URL, HttpMethod.GET, request, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String id = jsonNode.get("id").asText();
            String email = jsonNode.path("kakao_account").path("email").asText();
            socialUserInfo = new SocialUserInfo(SocialProvider.KAKAO, id, email);
        } catch (HttpClientErrorException e) {
            throw new CustomException(ErrorCode.SOCIAL_TOKEN_EXPIRED);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return socialUserInfo;
    }

}
