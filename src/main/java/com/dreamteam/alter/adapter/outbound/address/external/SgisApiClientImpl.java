package com.dreamteam.alter.adapter.outbound.address.external;

import com.dreamteam.alter.adapter.outbound.address.external.dto.SgisAuthResponse;
import com.dreamteam.alter.adapter.outbound.address.external.dto.SgisStageAddressResponse;
import com.dreamteam.alter.domain.address.port.outbound.SgisApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class SgisApiClientImpl implements SgisApiClient {

    private static final String AUTH_URL = "https://sgisapi.mods.go.kr/OpenAPI3/auth/authentication.json";
    private static final String STAGE_URL = "https://sgisapi.mods.go.kr/OpenAPI3/addr/stage.json";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_CONSUMER_KEY = "consumer_key";
    private static final String KEY_CONSUMER_SECRET = "consumer_secret";
    private static final String KEY_CODE = "cd";

    @Value("${sgis.service-id}")
    private String serviceId;

    @Value("${sgis.service-secret}")
    private String serviceSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public SgisAuthResponse authenticate() throws JsonProcessingException {
        String url = UriComponentsBuilder
            .fromUriString(AUTH_URL)
            .queryParam(KEY_CONSUMER_KEY, serviceId)
            .queryParam(KEY_CONSUMER_SECRET, serviceSecret)
            .toUriString();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return objectMapper.readValue(response.getBody(), SgisAuthResponse.class);
    }

    @Override
    public SgisStageAddressResponse getStageAddresses(String accessToken, String code) throws JsonProcessingException {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(STAGE_URL)
            .queryParam(KEY_ACCESS_TOKEN, accessToken);

        if (StringUtils.isNotBlank(code)) {
            builder.queryParam(KEY_CODE, code);
        }

        String url = builder.toUriString();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return objectMapper.readValue(response.getBody(), SgisStageAddressResponse.class);
    }
}
