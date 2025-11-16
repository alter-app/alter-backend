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
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final AccessTokenAuthenticationProvider accessTokenAuthenticationProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (ObjectUtils.isEmpty(accessor)) {
            return message;
        }

        String token = extractToken(accessor);
        if (StringUtils.isNotBlank(token)) {
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

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        SecurityContextHolder.clearContext();
        if (ObjectUtils.isNotEmpty(ex)) {
            log.error("WebSocket 메시지 전송 실패", ex);
        }
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // 1. Authorization 헤더에서 토큰 추출 시도
        String authToken = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.isNotBlank(authToken) && authToken.startsWith("Bearer ")) {
            return authToken.substring(7);
        }

        // 2. CONNECT 시에는 Authorization 헤더 필수
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.error("WebSocket CONNECT: Authorization 헤더가 없거나 형식이 잘못되었습니다.");
            throw new RuntimeException("WebSocket 인증 실패: Authorization 헤더가 필요합니다.");
        }

        // 3. 저장된 Authentication에서 토큰 추출
        Authentication storedAuth = (Authentication) accessor.getUser();
        if (ObjectUtils.isNotEmpty(storedAuth)
            && AccessTokenAuthentication.class.isAssignableFrom(storedAuth.getClass())) {
            return (String) storedAuth.getPrincipal();
        }

        return null;
    }
}
