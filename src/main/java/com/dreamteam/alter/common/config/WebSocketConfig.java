package com.dreamteam.alter.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;
    private final ChatSubscribeAuthorizationChannelInterceptor chatSubscribeAuthorizationChannelInterceptor;
    private final PresenceHeartbeatChannelInterceptor presenceHeartbeatChannelInterceptor;
    private final ChatWebSocketSessionRegistry chatWebSocketSessionRegistry;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/sub");
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-connect")
            .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // jwtChannelInterceptor가 먼저 user를 세팅하고, chatSubscribeAuthorizationChannelInterceptor가
        // 채팅방 구독을 인가한 뒤, presence TTL을 갱신한다.
        registration.interceptors(
            jwtChannelInterceptor, chatSubscribeAuthorizationChannelInterceptor, presenceHeartbeatChannelInterceptor);
    }

    // sessionId -> WebSocketSession 매핑의 유일한 진입점. 이 시점(raw WS 핸드셰이크)은
    // 아직 STOMP CONNECT 인증 전이라 principal을 모른다 — session.getId()만 안다.
    // (scope, memberId) -> sessionId 매핑은 인증 정보가 있는 WebSocketEventListener가 채운다.
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                chatWebSocketSessionRegistry.registerSession(session);
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                chatWebSocketSessionRegistry.unregisterSession(session);
                super.afterConnectionClosed(session, closeStatus);
            }
        });
    }
}
