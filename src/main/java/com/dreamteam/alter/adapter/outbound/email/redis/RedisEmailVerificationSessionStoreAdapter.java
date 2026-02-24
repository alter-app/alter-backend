package com.dreamteam.alter.adapter.outbound.email.redis;

import com.dreamteam.alter.domain.email.port.outbound.EmailVerificationSessionStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisEmailVerificationSessionStoreAdapter implements EmailVerificationSessionStoreRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX_CODE = "auth:email:code:";
    private static final String KEY_PREFIX_COOLDOWN = "auth:email:cooldown:";
    private static final String KEY_PREFIX_ATTEMPTS = "auth:email:attempts:";
    private static final String KEY_PREFIX_SESSION = "auth:email:session:";
    private static final String KEY_PREFIX_SEND_FAILED = "auth:email:send-failed:";


    // --- 인증 코드 관련 ---
    @Override
    public void saveCode(String email, String code, Duration ttl) {
        redisTemplate.delete(KEY_PREFIX_ATTEMPTS + email);
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
        redisTemplate.delete(KEY_PREFIX_ATTEMPTS + email);
    }

    @Override
    public long incrementAttempt(String email, Duration ttl) {
        String key = KEY_PREFIX_ATTEMPTS + email;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            // 처음 생성된 키라면 TTL 설정 (코드 TTL과 맞추거나 별도 설정)
            redisTemplate.expire(key, ttl);
        }
        return attempts != null ? attempts : 1L;
    }

    // --- 쿨다운 관련 ---

    @Override
    public boolean isCooldown(String email) {
        return redisTemplate.hasKey(KEY_PREFIX_COOLDOWN + email);
    }

    @Override
    public void markCooldown(String email, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX_COOLDOWN + email, "true", ttl);
    }

    // --- 인증 세션 토큰 관련 (신규) ---

    @Override
    public String createVerificationSession(String email, Duration ttl) {
        String token = UUID.randomUUID().toString();
        // Key: 토큰, Value: 이메일
        redisTemplate.opsForValue().set(KEY_PREFIX_SESSION + token, email, ttl);
        return token;
    }

    @Override
    public Optional<String> getEmailBySession(String token) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX_SESSION + token));
    }

    @Override
    public void deleteSession(String token) {
        redisTemplate.delete(KEY_PREFIX_SESSION + token);
    }

    // --- 발송 실패 관련 ---

    @Override
    public void markSendFailed(String email) {
        redisTemplate.opsForValue().set(KEY_PREFIX_SEND_FAILED + email, "true", Duration.ofMinutes(10));
    }

    @Override
    public boolean isSendFailed(String email) {
        return redisTemplate.hasKey(KEY_PREFIX_SEND_FAILED + email);
    }

    @Override
    public void clearSendFailed(String email) {
        redisTemplate.delete(KEY_PREFIX_SEND_FAILED + email);
    }
}
