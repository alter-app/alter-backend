package com.dreamteam.alter.common.config;

import com.dreamteam.alter.common.constants.ChatConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;
    private final ChatQueueSubscriptionGuardChannelInterceptor chatQueueSubscriptionGuardChannelInterceptor;
    private final PresenceHeartbeatChannelInterceptor presenceHeartbeatChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 실시간 전파는 convertAndSendToUser가 쓰는 유저 큐(prefix: "/queue")만 사용한다.
        // 과거 토픽 팬아웃("/sub")은 더 이상 아무도 publish하지 않아 제거했다.
        config.enableSimpleBroker(ChatConstants.CHAT_USER_QUEUE_PREFIX);
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-connect")
            .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // jwt가 먼저 user를 세팅 -> 유저 큐 SUBSCRIBE destination 화이트리스트 검사 -> presence TTL 갱신.
        registration.interceptors(
            jwtChannelInterceptor, chatQueueSubscriptionGuardChannelInterceptor, presenceHeartbeatChannelInterceptor);
    }
}
