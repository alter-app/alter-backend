package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ChatPresenceStoreRepositoryImpl implements ChatPresenceStore {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "chat:presence:sessions:";
    private static final Duration TTL = Duration.ofSeconds(60);

    private String key(TokenScope scope, Long memberId) {
        return KEY_PREFIX + scope.name() + ":" + memberId;
    }

    @Override
    public void markOnline(TokenScope scope, Long memberId, String sessionId) {
        String key = key(scope, memberId);
        redisTemplate.opsForSet().add(key, sessionId);
        redisTemplate.expire(key, TTL);
    }

    @Override
    public void markOffline(TokenScope scope, Long memberId, String sessionId) {
        redisTemplate.opsForSet().remove(key(scope, memberId), sessionId);
    }

    @Override
    public void touch(TokenScope scope, Long memberId) {
        // 세션 키가 살아있는 동안에만 TTL을 연장한다(만료된 키는 재생성하지 않음).
        redisTemplate.expire(key(scope, memberId), TTL);
    }

    @Override
    public boolean isOnline(TokenScope scope, Long memberId) {
        Long size = redisTemplate.opsForSet().size(key(scope, memberId));
        return size != null && size > 0;
    }

    @Override
    public Set<PresenceTarget> filterOnline(Collection<PresenceTarget> targets) {
        if (targets == null || targets.isEmpty()) {
            return Set.of();
        }

        List<PresenceTarget> list = new ArrayList<>(targets);
        // 각 대상의 세션 SET 크기를 파이프라인으로 한 번에 조회(왕복 1회).
        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection conn = (StringRedisConnection) connection;
            for (PresenceTarget target : list) {
                conn.sCard(key(target.scope(), target.memberId()));
            }
            return null;
        });

        Set<PresenceTarget> online = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            Object result = i < results.size() ? results.get(i) : null;
            if (result instanceof Number size && size.longValue() > 0) {
                online.add(list.get(i));
            }
        }
        return online;
    }
}
