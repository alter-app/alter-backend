package com.dreamteam.alter.common.config;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;

/**
 * 인바운드 STOMP 프레임(SUBSCRIBE/SEND/heartbeat)마다 presence TTL을 갱신해
 * 연결 유지 중인 유저가 TTL 만료로 오프라인 오판되는 것을 막는다.
 * (markOnline은 CONNECT 시 1회만 호출되므로 갱신 경로가 별도로 필요하다.)
 */
@Component
@RequiredArgsConstructor
public class PresenceHeartbeatChannelInterceptor implements ChannelInterceptor {

    private final ChatPresenceStore chatPresenceStore;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        resolve(accessor.getUser())
            .ifPresent(principal -> chatPresenceStore.touch(principal.scope(), principal.memberId()));
        return message;
    }

    private Optional<PresencePrincipal> resolve(Principal principal) {
        if (principal instanceof Authentication authentication
            && authentication.getDetails() instanceof LoginUserDto loginUser) {
            return Optional.of(new PresencePrincipal(loginUser.getScope(), loginUser.getId()));
        }
        return Optional.empty();
    }

    private record PresencePrincipal(TokenScope scope, Long memberId) {
    }
}
