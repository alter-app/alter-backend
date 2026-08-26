package com.dreamteam.alter.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketConfig 테스트")
class WebSocketConfigTests {

    @Mock
    private JwtChannelInterceptor jwtChannelInterceptor;

    @Mock
    private ChatSubscribeAuthorizationChannelInterceptor chatSubscribeAuthorizationChannelInterceptor;

    @Mock
    private PresenceHeartbeatChannelInterceptor presenceHeartbeatChannelInterceptor;

    @Mock
    private ChatWebSocketSessionRegistry chatWebSocketSessionRegistry;

    @Mock
    private ChannelRegistration registration;

    @InjectMocks
    private WebSocketConfig sut;

    @Test
    @DisplayName("인바운드 채널 인터셉터는 jwt -> 구독 인가 -> presence 순서로 등록된다")
    void configureClientInboundChannel_인터셉터_순서() {
        // when
        sut.configureClientInboundChannel(registration);

        // then: jwt가 먼저 user를 세팅하고, 구독 인가를 거친 뒤 presence TTL을 갱신해야 하므로
        // 순서가 회귀하면(예: presence가 인가보다 먼저 오면) 이 검증이 실패한다.
        // 모의 객체는 기본적으로 참조 동일성으로 비교되므로 vararg 순서까지 그대로 검증된다.
        then(registration).should().interceptors(
            jwtChannelInterceptor,
            chatSubscribeAuthorizationChannelInterceptor,
            presenceHeartbeatChannelInterceptor
        );
    }

    @Test
    @DisplayName("configureWebSocketTransport은 연결/종료 시점에 세션 레지스트리를 채우는 데코레이터 팩토리를 등록한다")
    void configureWebSocketTransport_세션레지스트리_연동() throws Exception {
        // given
        WebSocketTransportRegistration transportRegistration = mock(WebSocketTransportRegistration.class);
        ArgumentCaptor<WebSocketHandlerDecoratorFactory> factoryCaptor =
            ArgumentCaptor.forClass(WebSocketHandlerDecoratorFactory.class);

        // when
        sut.configureWebSocketTransport(transportRegistration);

        // then
        then(transportRegistration).should().addDecoratorFactory(factoryCaptor.capture());

        WebSocketHandler delegate = mock(WebSocketHandler.class);
        WebSocketHandler decorated = factoryCaptor.getValue().decorate(delegate);
        WebSocketSession session = mock(WebSocketSession.class);

        decorated.afterConnectionEstablished(session);
        then(chatWebSocketSessionRegistry).should().registerSession(session);

        decorated.afterConnectionClosed(session, CloseStatus.NORMAL);
        then(chatWebSocketSessionRegistry).should().unregisterSession(session);
    }
}
