package com.dreamteam.alter.adapter.outbound.user.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SignupSessionCacheRepository {

    private final StringRedisTemplate redisTemplate;

    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void save(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void deleteAll(List<String> keys) {
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
