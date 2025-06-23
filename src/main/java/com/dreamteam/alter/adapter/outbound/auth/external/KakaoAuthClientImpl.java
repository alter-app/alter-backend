package com.dreamteam.alter.adapter.outbound.auth.external;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialTokenResponseDto;
import com.dreamteam.alter.adapter.inbound.general.auth.dto.SocialUserInfo;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.port.outbound.KakaoAuthClient;
import com.dreamteam.alter.domain.user.type.PlatformType;
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

    private static final String KEY_GRANT_TYPE = "grant_type";
    private static final String VALUE_AUTHORIZATION_CODE = "authorization_code";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_REDIRECT_URI = "redirect_uri";
    private static final String KEY_CODE = "code";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private static final String KEY_ID = "id";
    private static final String KEY_KAKAO_ACCOUNT = "kakao_account";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_BIRTHYEAR = "birthyear";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_GENDER = "gender";

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public SocialTokenResponseDto exchangeCodeForToken(String authorizationCode, PlatformType platformType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(KEY_GRANT_TYPE, VALUE_AUTHORIZATION_CODE);
        params.add(KEY_CLIENT_ID, kakaoClientId);
        params.add(KEY_CODE, authorizationCode);
        if (platformType.equals(PlatformType.WEB)) {
            params.add(KEY_REDIRECT_URI, kakaoRedirectUri);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(KAKAO_OAUTH_URL, request, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            return SocialTokenResponseDto.withAccessAndRefresh(
                jsonNode.get(KEY_ACCESS_TOKEN).asText(),
                jsonNode.get(KEY_REFRESH_TOKEN).asText()
            );
        } catch (HttpClientErrorException e) {
            throw new CustomException(ErrorCode.SOCIAL_TOKEN_EXPIRED);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public SocialUserInfo getUserInfo(SocialTokenResponseDto socialTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(socialTokens.getAccessToken());

        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(KAKAO_USER_API_URL, HttpMethod.GET, request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String socialId = jsonNode.get(KEY_ID).asText();

            JsonNode accountNode = jsonNode.path(KEY_KAKAO_ACCOUNT);
            String email = accountNode.path(KEY_EMAIL).asText();
            String name = accountNode.has(KEY_NAME) ? accountNode.path(KEY_NAME).asText() : null;
            String birthyear = accountNode.has(KEY_BIRTHYEAR) ? accountNode.path(KEY_BIRTHYEAR).asText() : null;
            String birthday = accountNode.has(KEY_BIRTHDAY) ? accountNode.path(KEY_BIRTHDAY).asText() : null;
            String gender = accountNode.has(KEY_GENDER) ? accountNode.path(KEY_GENDER).asText() : null;

            return SocialUserInfo.of(
                SocialProvider.KAKAO,
                socialId,
                email,
                name,
                birthyear,
                birthday,
                gender
            );
        } catch (HttpClientErrorException e) {
            throw new CustomException(ErrorCode.SOCIAL_TOKEN_EXPIRED);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
