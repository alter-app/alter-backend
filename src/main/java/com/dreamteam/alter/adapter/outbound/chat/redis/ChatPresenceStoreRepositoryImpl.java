package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ChatPresenceStoreRepositoryImpl implements ChatPresenceStore {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "chat:presence:";
    private static final Duration TTL = Duration.ofSeconds(60);

    private String key(TokenScope scope, Long memberId) {
        return KEY_PREFIX + scope.name() + ":" + memberId;
    }

    @Override
    public void markOnline(TokenScope scope, Long memberId) {
        redisTemplate.opsForValue().set(key(scope, memberId), "1", TTL);
    }

    @Override
    public void refresh(TokenScope scope, Long memberId) {
        redisTemplate.expire(key(scope, memberId), TTL);
    }

    @Override
    public void markOffline(TokenScope scope, Long memberId) {
        redisTemplate.delete(key(scope, memberId));
    }

    @Override
    public boolean isOnline(TokenScope scope, Long memberId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(scope, memberId)));
    }
}
