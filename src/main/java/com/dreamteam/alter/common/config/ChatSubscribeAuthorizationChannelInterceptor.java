package com.dreamteam.alter.common.config;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.common.constants.ChatConstants;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;

/**
 * {@code /sub/chat.{chatRoomId}} 구독을 그 방의 활성 멤버만 허용한다.
 * 인증만 통과하면 임의의 roomId를 구독해 남의 채팅방 메시지를 실시간으로 받는 것을 막는다.
 * <p>
 * 거부는 예외를 던지지 않고 프레임을 드롭한다({@code preSend}가 {@code null} 반환). 방 하나 구독이
 * 거부됐다고 예외를 던지면 {@code StompSubProtocolHandler}가 세션 전체를 close하여, 같은 세션의
 * 다른 정상 구독까지 끊기고 클라이언트 재연결이 반복되는 루프로 이어질 수 있다. 인증 실패와 달리
 * 방 단위 인가 실패는 국소적이고 복구 가능한 문제이므로 세션은 유지한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSubscribeAuthorizationChannelInterceptor implements ChannelInterceptor {

    private final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (ObjectUtils.isEmpty(accessor) || !StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(ChatConstants.CHAT_SUBSCRIBE_DESTINATION_PREFIX)) {
            return message;
        }

        Long roomId = parseRoomId(destination);
        if (roomId == null) {
            log.warn("WebSocket 구독 드롭: destination의 roomId가 유효하지 않습니다. destination={}", destination);
            return null;
        }

        Optional<PrincipalInfo> principal = resolve(accessor.getUser());
        if (principal.isEmpty()) {
            log.warn("WebSocket 구독 드롭: 인증 정보가 없습니다. roomId={}", roomId);
            return null;
        }

        Long memberId = principal.get().memberId();
        TokenScope scope = principal.get().scope();
        if (!chatRoomMemberQueryRepository.existsActive(roomId, memberId, scope)) {
            log.warn("WebSocket 구독 드롭: 채팅방 활성 멤버가 아닙니다. roomId={}, memberId={}, scope={}",
                roomId, memberId, scope);
            return null;
        }

        return message;
    }

    private Long parseRoomId(String destination) {
        String roomIdPart = destination.substring(ChatConstants.CHAT_SUBSCRIBE_DESTINATION_PREFIX.length());
        try {
            return Long.valueOf(roomIdPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Optional<PrincipalInfo> resolve(Principal principal) {
        if (principal instanceof Authentication authentication
            && authentication.getDetails() instanceof LoginUserDto loginUser) {
            return Optional.of(new PrincipalInfo(loginUser.getScope(), loginUser.getId()));
        }
        return Optional.empty();
    }

    private record PrincipalInfo(TokenScope scope, Long memberId) {
    }
}
