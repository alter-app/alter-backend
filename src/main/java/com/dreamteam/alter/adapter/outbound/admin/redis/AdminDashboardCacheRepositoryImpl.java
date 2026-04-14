package com.dreamteam.alter.adapter.outbound.admin.redis;

import com.dreamteam.alter.domain.admin.port.outbound.AdminDashboardCacheRepository;
import com.dreamteam.alter.domain.admin.type.DashboardPeriod;
import com.dreamteam.alter.domain.admin.type.DashboardChartStatistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDashboardCacheRepositoryImpl implements AdminDashboardCacheRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "admin:dashboard:";
    private static final Duration TTL = Duration.ofMinutes(5);

    @Override
    public Optional<DashboardChartStatistics> find(DashboardPeriod period, int year) {
        try {
            String value = redisTemplate.opsForValue().get(buildKey(period, year));
            if (ObjectUtils.isEmpty(value)) return Optional.empty();
            return Optional.of(objectMapper.readValue(value, DashboardChartStatistics.class));
        } catch (DataAccessException e) {
            log.warn("Redis access failed for dashboard cache period={}, year={}", period, year, e);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize dashboard cache period={}, year={}", period, year, e);
            return Optional.empty();
        }
    }

    @Override
    public void save(DashboardPeriod period, int year, DashboardChartStatistics data) {
        try {
            String value = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(buildKey(period, year), value, TTL);
        } catch (DataAccessException e) {
            log.warn("Redis save failed for dashboard cache period={}, year={}", period, year, e);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize dashboard cache period={}, year={}", period, year, e);
        }
    }

    private String buildKey(DashboardPeriod period, int year) {
        return KEY_PREFIX + period + ":" + year;
    }
}
