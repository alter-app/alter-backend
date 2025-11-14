package com.dreamteam.alter.common.config;

import com.dreamteam.alter.application.auth.provider.AccessTokenAuthenticationProvider;
import com.dreamteam.alter.application.auth.token.AccessTokenAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final AccessTokenAuthenticationProvider accessTokenAuthenticationProvider;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (ObjectUtils.isNotEmpty(accessor)) {
                    // CONNECT 시 인증 처리
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        String authToken = accessor.getFirstNativeHeader("Authorization");

                        if (StringUtils.isBlank(authToken) || !authToken.startsWith("Bearer ")) {
                            log.error("WebSocket CONNECT: Authorization 헤더가 없거나 형식이 잘못되었습니다.");
                            throw new RuntimeException("WebSocket 인증 실패: Authorization 헤더가 필요합니다.");
                        }

                        String token = authToken.substring(7);
                        try {
                            AccessTokenAuthentication authRequest = new AccessTokenAuthentication(token);
                            Authentication authentication = accessTokenAuthenticationProvider.authenticate(authRequest);
                            accessor.setUser(authentication);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } catch (Exception e) {
                            log.error("WebSocket 인증 실패: {}", e.getMessage(), e);
                            throw new RuntimeException("WebSocket 인증 실패: " + e.getMessage(), e);
                        }
                    }
                    // SEND, SUBSCRIBE 등 모든 메시지에서 SecurityContext 유지
                    else {
                        if (ObjectUtils.isNotEmpty(accessor.getUser())) {
                            SecurityContextHolder.getContext()
                                .setAuthentication((Authentication) accessor.getUser());
                        }
                    }
                }

                return message;
            }

            @Override
            public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
                if (ObjectUtils.isNotEmpty(ex)) {
                    log.error("WebSocket 메시지 전송 실패", ex);
                }
            }
        });
    }

}
