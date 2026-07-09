package com.dreamteam.alter.adapter.outbound.chat.redis;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
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

    private static final String KEY = "chat:presence:APP:1";

    @Test
    @DisplayName("markOnline 시 TTL 60초로 키를 저장한다")
    void markOnline_키저장() {
        // given
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        chatPresenceStoreRepository.markOnline(TokenScope.APP, 1L);

        // then
        then(valueOperations).should().set(KEY, "1", Duration.ofSeconds(60));
    }

    @Test
    @DisplayName("refresh 시 TTL을 60초로 갱신한다")
    void refresh_TTL갱신() {
        // when
        chatPresenceStoreRepository.refresh(TokenScope.APP, 1L);

        // then
        then(redisTemplate).should().expire(KEY, Duration.ofSeconds(60));
    }

    @Test
    @DisplayName("markOffline 시 키를 삭제한다")
    void markOffline_키삭제() {
        // when
        chatPresenceStoreRepository.markOffline(TokenScope.APP, 1L);

        // then
        then(redisTemplate).should().delete(KEY);
    }

    @Test
    @DisplayName("isOnline은 hasKey 결과를 그대로 반환한다 - true")
    void isOnline_true() {
        // given
        given(redisTemplate.hasKey(KEY)).willReturn(true);

        // when
        boolean result = chatPresenceStoreRepository.isOnline(TokenScope.APP, 1L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isOnline은 hasKey 결과를 그대로 반환한다 - false")
    void isOnline_false() {
        // given
        given(redisTemplate.hasKey(KEY)).willReturn(false);

        // when
        boolean result = chatPresenceStoreRepository.isOnline(TokenScope.APP, 1L);

        // then
        assertThat(result).isFalse();
    }
}
