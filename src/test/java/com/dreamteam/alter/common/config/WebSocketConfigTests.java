package com.dreamteam.alter.common.config;

import com.dreamteam.alter.common.constants.ChatConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketConfig 테스트")
class WebSocketConfigTests {

    @Mock
    private JwtChannelInterceptor jwtChannelInterceptor;

    @Mock
    private ChatQueueSubscriptionGuardChannelInterceptor chatQueueSubscriptionGuardChannelInterceptor;

    @Mock
    private PresenceHeartbeatChannelInterceptor presenceHeartbeatChannelInterceptor;

    @Mock
    private ChannelRegistration registration;

    @Mock
    private MessageBrokerRegistry brokerRegistry;

    @InjectMocks
    private WebSocketConfig sut;

    @Test
    @DisplayName("인바운드 채널 인터셉터는 jwt -> 유저 큐 구독 화이트리스트 -> presence 순서로 등록된다")
    void configureClientInboundChannel_인터셉터_순서() {
        // when
        sut.configureClientInboundChannel(registration);

        // then: jwt가 먼저 user를 세팅 -> SUBSCRIBE destination 화이트리스트 검사 -> presence TTL 갱신
        // 순서가 회귀하면 이 검증이 실패한다.
        // 모의 객체는 기본적으로 참조 동일성으로 비교되므로 vararg 순서까지 그대로 검증된다.
        then(registration).should().interceptors(
            jwtChannelInterceptor,
            chatQueueSubscriptionGuardChannelInterceptor,
            presenceHeartbeatChannelInterceptor
        );
    }

    @Test
    @DisplayName("브로커는 유저 큐 prefix만 활성화한다 (과거 토픽 \"/sub\" 팬아웃 회귀 방지)")
    void configureMessageBroker_유저큐_prefix만_활성화() {
        // when
        sut.configureMessageBroker(brokerRegistry);

        // then: convertAndSendToUser가 실제로 배달되려면 이 prefix가 반드시 활성화돼 있어야 한다.
        // Redis 없으면 SKIP되는 통합 테스트가 유일한 커버였던 회귀 포인트.
        then(brokerRegistry).should().enableSimpleBroker(ChatConstants.CHAT_USER_QUEUE_PREFIX);
    }
}
