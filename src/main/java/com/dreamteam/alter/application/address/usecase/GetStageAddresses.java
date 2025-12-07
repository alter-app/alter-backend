package com.dreamteam.alter.application.address.usecase;

import com.dreamteam.alter.adapter.inbound.general.address.dto.StageAddressRequestDto;
import com.dreamteam.alter.adapter.outbound.address.external.dto.SgisAuthResponse;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.address.model.SgisStageAddress;
import com.dreamteam.alter.domain.address.port.inbound.GetStageAddressesUseCase;
import com.dreamteam.alter.domain.address.port.outbound.SgisApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service("getStageAddresses")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetStageAddresses implements GetStageAddressesUseCase {

    private static final String TOKEN_CACHE_KEY = "SGIS:ACCESS_TOKEN";
    private static final String CACHE_PREFIX = "SGIS:ADDR:";
    private static final Duration CACHE_TTL = Duration.ofDays(1);

    private final SgisApiClient sgisApiClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<SgisStageAddress> execute(StageAddressRequestDto request) {
        String code = request.getCode();
        if (StringUtils.length(code) > 5) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "행정구역 코드는 최대 5자리여야 합니다.");
        }

        String cacheKey = CACHE_PREFIX + (StringUtils.isBlank(code) ? "ROOT" : code);

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isNotBlank(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<>() {});
            } catch (JsonProcessingException ignored) {
                redisTemplate.delete(cacheKey);
            }
        }

        List<SgisStageAddress> result = fetchWithRetry(code);
        try {
            String serialized = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, serialized, CACHE_TTL);
        } catch (JsonProcessingException ignored) {
            log.error("행정구역 캐시 저장 실패 code={}", code);
        }

        return result;
    }

    private List<SgisStageAddress> fetchWithRetry(String code) {
        try {
            String token = getOrCreateToken();
            return sgisApiClient.getStageAddresses(token, code);
        } catch (CustomException e) {
            if (!StringUtils.contains(e.getMessage(), "SGIS_TOKEN_EXPIRED")) {
                throw e;
            }
            redisTemplate.delete(TOKEN_CACHE_KEY);
            String token = getOrCreateToken();
            return sgisApiClient.getStageAddresses(token, code);
        }
    }

    private String getOrCreateToken() {
        String cached = redisTemplate.opsForValue().get(TOKEN_CACHE_KEY);
        if (StringUtils.isNotBlank(cached)) {
            return cached;
        }

        synchronized (this) {
            cached = redisTemplate.opsForValue().get(TOKEN_CACHE_KEY);
            if (StringUtils.isNotBlank(cached)) {
                return cached;
            }
            SgisAuthResponse auth = sgisApiClient.authenticate();

            String token = auth.getResult().getAccessToken();
            String timeoutText = auth.getResult().getAccessTimeout();

            long now = System.currentTimeMillis() / 1000;

            long expiry = Long.parseLong(timeoutText);
            long ttlSeconds = Math.max(60, expiry - now);

            redisTemplate.opsForValue().set(TOKEN_CACHE_KEY, token, Duration.ofSeconds(ttlSeconds));
            return token;
        }
    }
}
