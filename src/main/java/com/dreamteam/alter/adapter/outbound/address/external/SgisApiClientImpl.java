package com.dreamteam.alter.adapter.outbound.address.external;

import com.dreamteam.alter.adapter.outbound.address.external.dto.SgisAuthResponse;
import com.dreamteam.alter.adapter.outbound.address.external.dto.SgisStageAddressItem;
import com.dreamteam.alter.adapter.outbound.address.external.dto.SgisStageAddressResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.address.model.SgisStageAddress;
import com.dreamteam.alter.domain.address.port.outbound.SgisApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SgisApiClientImpl implements SgisApiClient {

    private static final String AUTH_URL = "https://sgisapi.mods.go.kr/OpenAPI3/auth/authentication.json";
    private static final String STAGE_URL = "https://sgisapi.mods.go.kr/OpenAPI3/addr/stage.json";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_CONSUMER_KEY = "consumer_key";
    private static final String KEY_CONSUMER_SECRET = "consumer_secret";
    private static final String KEY_CODE = "cd";
    private static final String KEY_INCLUDE_BOUNDARY = "pg_yn";

    @Value("${sgis.service-id}")
    private String serviceId;

    @Value("${sgis.service-secret}")
    private String serviceSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public SgisAuthResponse authenticate() {
        try {
            String url = UriComponentsBuilder
                .fromUriString(AUTH_URL)
                .queryParam(KEY_CONSUMER_KEY, serviceId)
                .queryParam(KEY_CONSUMER_SECRET, serviceSecret)
                .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            SgisAuthResponse authResponse = objectMapper.readValue(response.getBody(), SgisAuthResponse.class);

            if (ObjectUtils.isEmpty(authResponse.getResult()) || authResponse.getErrCd() != 0) {
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "SGIS 인증 토큰을 받을 수 없습니다.");
            }

            return authResponse;
        } catch (HttpStatusCodeException e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, extractErrorMessage(e.getResponseBodyAsString()));
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "SGIS 인증 응답을 처리할 수 없습니다.");
        }
    }

    @Override
    public List<SgisStageAddress> getStageAddresses(String accessToken, String code) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(STAGE_URL)
            .queryParam(KEY_ACCESS_TOKEN, accessToken)
            .queryParam(KEY_INCLUDE_BOUNDARY, "0");

        if (StringUtils.isNotBlank(code)) {
            builder.queryParam(KEY_CODE, code);
        }

        String url = builder.toUriString();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            SgisStageAddressResponse stageResponse = objectMapper.readValue(response.getBody(), SgisStageAddressResponse.class);

            if (stageResponse.getErrCd() != 0 || ObjectUtils.isEmpty(stageResponse.getResult())) {
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, extractErrorMessage(response.getBody()));
            }

            List<SgisStageAddressItem> result = stageResponse.getResult();

            return ObjectUtils.isEmpty(result)
                ? List.of()
                : result.stream()
                    .map(item -> SgisStageAddress.of(item.getCode(), item.getAddrName()))
                    .toList();
        } catch (HttpStatusCodeException e) {
            if (isTokenExpiredResponse(e)) {
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "SGIS_TOKEN_EXPIRED");
            }
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, extractErrorMessage(e.getResponseBodyAsString()));
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "SGIS 응답을 처리할 수 없습니다.");
        }
    }

    private boolean isTokenExpiredResponse(HttpStatusCodeException e) {
        int status = e.getStatusCode().value();
        if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
            return true;
        }
        String message = extractErrorMessage(e.getResponseBodyAsString());
        return ObjectUtils.isNotEmpty(message) && message.contains("만료");
    }

    private String extractErrorMessage(String responseBody) {
        if (ObjectUtils.isEmpty(responseBody)) {
            return ErrorCode.EXTERNAL_API_ERROR.getMessage();
        }

        try {
            JsonNode body = objectMapper.readTree(responseBody);
            JsonNode messageNode = body.path("errMsg");
            if (messageNode.isTextual()) {
                return messageNode.asText();
            }
        } catch (JsonProcessingException ignored) {
            // ignore parsing errors
        }

        return responseBody;
    }
}
