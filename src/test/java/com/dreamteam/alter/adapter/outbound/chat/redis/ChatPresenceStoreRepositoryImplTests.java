package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatPresenceStoreRepositoryImpl 테스트")
class ChatPresenceStoreRepositoryImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private ChatPresenceStoreRepositoryImpl chatPresenceStoreRepository;

    private static final String KEY = "chat:presence:sessions:APP:1";

    @Test
    @DisplayName("markOnline 시 세션 id를 SET에 추가하고 TTL 60초로 갱신한다")
    void markOnline_세션추가() {
        // given
        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOperations = mock(SetOperations.class);
        given(redisTemplate.opsForSet()).willReturn(setOperations);

        // when
        chatPresenceStoreRepository.markOnline(TokenScope.APP, 1L, "sess1");

        // then
        then(setOperations).should().add(KEY, "sess1");
        then(redisTemplate).should().expire(KEY, Duration.ofSeconds(60));
    }

    @Test
    @DisplayName("markOffline 시 세션 id만 SET에서 제거한다")
    void markOffline_세션제거() {
        // given
        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOperations = mock(SetOperations.class);
        given(redisTemplate.opsForSet()).willReturn(setOperations);

        // when
        chatPresenceStoreRepository.markOffline(TokenScope.APP, 1L, "sess1");

        // then
        then(setOperations).should().remove(KEY, "sess1");
    }

    @Test
    @DisplayName("isOnline은 SET 크기가 0보다 크면 true")
    void isOnline_true() {
        // given
        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOperations = mock(SetOperations.class);
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.size(KEY)).willReturn(2L);

        // when
        boolean result = chatPresenceStoreRepository.isOnline(TokenScope.APP, 1L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isOnline은 SET 크기가 0이면 false")
    void isOnline_false_크기0() {
        // given
        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOperations = mock(SetOperations.class);
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.size(KEY)).willReturn(0L);

        // when
        boolean result = chatPresenceStoreRepository.isOnline(TokenScope.APP, 1L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isOnline은 SET이 없어(size=null) 오프라인이면 false")
    void isOnline_false_null() {
        // given
        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOperations = mock(SetOperations.class);
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.size(KEY)).willReturn(null);

        // when
        boolean result = chatPresenceStoreRepository.isOnline(TokenScope.APP, 1L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("filterOnline은 파이프라인 결과에서 SET 크기>0인 대상만 online으로 반환한다")
    void filterOnline_파이프라인_결과_매핑() {
        // given: 대상 1(APP:1)=2건, 대상 2(APP:2)=0건 → 1만 online
        ChatPresenceStore.PresenceTarget t1 = new ChatPresenceStore.PresenceTarget(TokenScope.APP, 1L);
        ChatPresenceStore.PresenceTarget t2 = new ChatPresenceStore.PresenceTarget(TokenScope.APP, 2L);
        given(redisTemplate.executePipelined(any(RedisCallback.class)))
            .willReturn(List.of(2L, 0L));

        // when
        Set<ChatPresenceStore.PresenceTarget> online = chatPresenceStoreRepository.filterOnline(List.of(t1, t2));

        // then
        assertThat(online).containsExactly(t1);
    }

    @Test
    @DisplayName("filterOnline은 대상이 비어있으면 빈 결과, 파이프라인 미호출")
    void filterOnline_대상없음() {
        // when
        Set<ChatPresenceStore.PresenceTarget> online = chatPresenceStoreRepository.filterOnline(List.of());

        // then
        assertThat(online).isEmpty();
        then(redisTemplate).should(org.mockito.Mockito.never()).executePipelined(any(RedisCallback.class));
    }
}
