package com.dreamteam.alter.common.config;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.application.auth.token.AccessTokenAuthentication;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketEventListener 테스트")
class WebSocketEventListenerTest {

    @Mock
    private ChatPresenceStore chatPresenceStore;

    @InjectMocks
    private WebSocketEventListener webSocketEventListener;

    @SuppressWarnings("unchecked")
    private final Message<byte[]> message = mock(Message.class);

    private Principal accessTokenPrincipal(TokenScope scope, Long memberId) {
        LoginUserDto dto = mock(LoginUserDto.class);
        lenient().when(dto.getScope()).thenReturn(scope);
        lenient().when(dto.getId()).thenReturn(memberId);
        return new AccessTokenAuthentication("token", dto, Collections.emptyList());
    }

    @Test
    @DisplayName("연결 시 인증된 사용자면 markOnline 호출")
    void onConnected_인증된사용자_markOnline() {
        // given
        Principal principal = accessTokenPrincipal(TokenScope.APP, 1L);
        SessionConnectedEvent event = new SessionConnectedEvent(this, message, principal);

        // when
        webSocketEventListener.onConnected(event);

        // then
        then(chatPresenceStore).should().markOnline(TokenScope.APP, 1L);
    }

    @Test
    @DisplayName("연결 시 principal이 없으면 markOnline 호출 안함")
    void onConnected_principal없음_markOnline호출안함() {
        // given
        SessionConnectedEvent event = new SessionConnectedEvent(this, message, null);

        // when
        webSocketEventListener.onConnected(event);

        // then
        then(chatPresenceStore).should(never()).markOnline(any(), any());
    }

    @Test
    @DisplayName("연결 시 LoginUserDto가 아닌 principal이면 markOnline 호출 안함")
    void onConnected_알수없는principal_markOnline호출안함() {
        // given
        Principal principal = new TestingAuthenticationToken("foo", "bar");
        SessionConnectedEvent event = new SessionConnectedEvent(this, message, principal);

        // when
        webSocketEventListener.onConnected(event);

        // then
        then(chatPresenceStore).should(never()).markOnline(any(), any());
    }

    @Test
    @DisplayName("연결 해제 시 인증된 사용자면 markOffline 호출")
    void onDisconnect_인증된사용자_markOffline() {
        // given
        Principal principal = accessTokenPrincipal(TokenScope.MANAGER, 2L);
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, "session-1", CloseStatus.NORMAL, principal);

        // when
        webSocketEventListener.onDisconnect(event);

        // then
        then(chatPresenceStore).should().markOffline(TokenScope.MANAGER, 2L);
    }

    @Test
    @DisplayName("연결 해제 시 principal이 없으면 markOffline 호출 안함")
    void onDisconnect_principal없음_markOffline호출안함() {
        // given
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, "session-1", CloseStatus.NORMAL, null);

        // when
        webSocketEventListener.onDisconnect(event);

        // then
        then(chatPresenceStore).should(never()).markOffline(any(), any());
    }
}
