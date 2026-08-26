package com.dreamteam.alter.common.config;

import com.dreamteam.alter.common.constants.ChatConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * SUBSCRIBE destination이 유저 큐 목적지({@code /user} + {@link ChatConstants#CHAT_MESSAGE_USER_QUEUE_DESTINATION})와
 * 정확히 일치하지 않으면 드롭한다.
 * <p>
 * Spring의 {@code DefaultSubscriptionRegistry}는 destination을 Ant 패턴으로 취급한다. 인증만 통과하면 누구나
 * {@code /queue/**}(브로커 prefix를 그대로 구독, {@code /user} 우회)로 이 인스턴스를 거치는 모든 유저의
 * 채팅 메시지를 받을 수 있어, {@code equals} 화이트리스트로 막는다({@code startsWith}는 접두어는 같지만
 * 그 자체가 와일드카드 패턴인 destination(e.g. {@code /queue/chat.messages**})을 걸러내지 못해 같은 구멍을
 * 남긴다).
 * <p>
 * 거부는 예외를 던지지 않고 프레임을 드롭한다({@code preSend}가 {@code null} 반환). 예외를 던지면
 * {@code StompSubProtocolHandler}가 세션 전체를 close해 같은 세션의 다른 정상 구독까지 끊기고 재연결 루프로
 * 이어질 수 있다(ALT-283 전례).
 */
@Slf4j
@Component
public class ChatQueueSubscriptionGuardChannelInterceptor implements ChannelInterceptor {

    private static final String ALLOWED_DESTINATION = "/user" + ChatConstants.CHAT_MESSAGE_USER_QUEUE_DESTINATION;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (ObjectUtils.isEmpty(accessor) || !StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return message;
        }

        String destination = accessor.getDestination();
        if (!ALLOWED_DESTINATION.equals(destination)) {
            log.warn("WebSocket 구독 드롭: 허용되지 않은 destination입니다. destination={}", destination);
            return null;
        }

        return message;
    }
}
