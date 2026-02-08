package com.dreamteam.alter.adapter.outbound.redis.email;

import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationTokenStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisEmailVerificationTokenStoreAdapter implements EmailVerificationTokenStorePort {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX_CODE = "auth:email:code:";
    private static final String KEY_PREFIX_VERIFIED = "auth:email:verified:";
    private static final String KEY_PREFIX_COOLDOWN = "auth:email:cooldown:";

    @Override
    public void saveCode(String email, String code, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX_CODE + email, code, ttl);
    }

    @Override
    public Optional<String> findCode(String email) {
        String code = redisTemplate.opsForValue().get(KEY_PREFIX_CODE + email);
        return Optional.ofNullable(code);
    }

    @Override
    public void deleteCode(String email) {
        redisTemplate.delete(KEY_PREFIX_CODE + email);
    }

    @Override
    public void markVerified(String email, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX_VERIFIED + email, "true", ttl);
    }

    @Override
    public boolean isVerified(String email) {
        return redisTemplate.hasKey(KEY_PREFIX_VERIFIED + email);
    }

    @Override
    public boolean isCooldown(String email) {
        return redisTemplate.hasKey(KEY_PREFIX_COOLDOWN + email);
    }

    @Override
    public void markCooldown(String email, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX_COOLDOWN + email, "true", ttl);
    }
}
