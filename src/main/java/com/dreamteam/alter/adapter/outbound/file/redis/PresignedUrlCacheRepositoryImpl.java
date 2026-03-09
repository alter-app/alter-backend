package com.dreamteam.alter.adapter.outbound.file.redis;

import com.dreamteam.alter.domain.file.PresignedUrlResult;
import com.dreamteam.alter.domain.file.port.outbound.PresignedUrlCacheRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresignedUrlCacheRepositoryImpl implements PresignedUrlCacheRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "file:presigned:";
    private static final long BUFFER_SECONDS = 60;

    @Override
    public Optional<PresignedUrlResult> findByFileId(String fileId) {
        try {
            String value = redisTemplate.opsForValue().get(KEY_PREFIX + fileId);
            if (ObjectUtils.isEmpty(value)) return Optional.empty();
            return Optional.of(objectMapper.readValue(value, PresignedUrlCacheEntry.class).toResult());
        } catch (DataAccessException e) {
            log.warn("Redis access failed for fileId={}", fileId, e);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize presigned URL cache for fileId={}", fileId, e);
            return Optional.empty();
        }
    }

    @Override
    public void save(String fileId, PresignedUrlResult result) {
        Duration ttl = Duration.between(Instant.now(), result.expiresAt()).minusSeconds(BUFFER_SECONDS);
        if (ttl.isNegative() || ttl.isZero()) return;

        try {
            String value = objectMapper.writeValueAsString(PresignedUrlCacheEntry.from(result));
            redisTemplate.opsForValue().set(KEY_PREFIX + fileId, value, ttl);
        } catch (DataAccessException e) {
            log.warn("Redis save failed for fileId={}", fileId, e);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize presigned URL cache for fileId={}", fileId, e);
        }
    }

    @Override
    public void deleteByFileId(String fileId) {
        try {
            redisTemplate.delete(KEY_PREFIX + fileId);
        } catch (DataAccessException e) {
            log.warn("Redis delete failed for fileId={}", fileId, e);
        }
    }
}
