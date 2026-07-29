package com.dreamteam.alter.common.config;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatPresenceStore chatPresenceStore;

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        if (sessionId == null) {
            return;
        }
        resolve(event.getUser())
            .ifPresent(principal -> chatPresenceStore.markOnline(principal.scope(), principal.memberId(), sessionId));
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        if (sessionId == null) {
            return;
        }
        resolve(event.getUser())
            .ifPresent(principal -> chatPresenceStore.markOffline(principal.scope(), principal.memberId(), sessionId));
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
