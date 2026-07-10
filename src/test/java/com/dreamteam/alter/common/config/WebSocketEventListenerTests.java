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
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
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

    private Message<byte[]> messageWithSessionId(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECTED);
        accessor.setSessionId(sessionId);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Principal accessTokenPrincipal(TokenScope scope, Long memberId) {
        LoginUserDto dto = mock(LoginUserDto.class);
        lenient().when(dto.getScope()).thenReturn(scope);
        lenient().when(dto.getId()).thenReturn(memberId);
        return new AccessTokenAuthentication("token", dto, Collections.emptyList());
    }

    @Test
    @DisplayName("연결 시 인증된 사용자면 세션 id와 함께 markOnline 호출")
    void onConnected_인증된사용자_markOnline() {
        // given
        Principal principal = accessTokenPrincipal(TokenScope.APP, 1L);
        Message<byte[]> message = messageWithSessionId("sess1");
        SessionConnectedEvent event = new SessionConnectedEvent(this, message, principal);

        // when
        webSocketEventListener.onConnected(event);

        // then
        then(chatPresenceStore).should().markOnline(TokenScope.APP, 1L, "sess1");
    }

    @Test
    @DisplayName("연결 시 principal이 없으면 markOnline 호출 안함")
    void onConnected_principal없음_markOnline호출안함() {
        // given
        Message<byte[]> message = messageWithSessionId("sess1");
        SessionConnectedEvent event = new SessionConnectedEvent(this, message, null);

        // when
        webSocketEventListener.onConnected(event);

        // then
        then(chatPresenceStore).should(never()).markOnline(any(), any(), any());
    }

    @Test
    @DisplayName("연결 시 LoginUserDto가 아닌 principal이면 markOnline 호출 안함")
    void onConnected_알수없는principal_markOnline호출안함() {
        // given
        Principal principal = new TestingAuthenticationToken("foo", "bar");
        Message<byte[]> message = messageWithSessionId("sess1");
        SessionConnectedEvent event = new SessionConnectedEvent(this, message, principal);

        // when
        webSocketEventListener.onConnected(event);

        // then
        then(chatPresenceStore).should(never()).markOnline(any(), any(), any());
    }

    @Test
    @DisplayName("연결 시 세션 id가 없으면 markOnline 호출 안함")
    void onConnected_세션id없음_markOnline호출안함() {
        // given
        Principal principal = accessTokenPrincipal(TokenScope.APP, 1L);
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).build();
        SessionConnectedEvent event = new SessionConnectedEvent(this, message, principal);

        // when
        webSocketEventListener.onConnected(event);

        // then
        then(chatPresenceStore).should(never()).markOnline(any(), any(), any());
    }

    @Test
    @DisplayName("연결 해제 시 인증된 사용자면 세션 id와 함께 markOffline 호출")
    void onDisconnect_인증된사용자_markOffline() {
        // given
        Principal principal = accessTokenPrincipal(TokenScope.MANAGER, 2L);
        Message<byte[]> message = messageWithSessionId("session-1");
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, "session-1", CloseStatus.NORMAL, principal);

        // when
        webSocketEventListener.onDisconnect(event);

        // then
        then(chatPresenceStore).should().markOffline(TokenScope.MANAGER, 2L, "session-1");
    }

    @Test
    @DisplayName("연결 해제 시 principal이 없으면 markOffline 호출 안함")
    void onDisconnect_principal없음_markOffline호출안함() {
        // given
        Message<byte[]> message = messageWithSessionId("session-1");
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, "session-1", CloseStatus.NORMAL, null);

        // when
        webSocketEventListener.onDisconnect(event);

        // then
        then(chatPresenceStore).should(never()).markOffline(any(), any(), any());
    }

    @Test
    @DisplayName("다중 세션 - 하나의 세션만 종료해도 다른 세션은 여전히 markOnline 상태로 유지된다")
    void 다중세션_한세션종료해도_다른세션온라인유지() {
        // given: 같은 사용자가 두 개의 세션(sess1, sess2)으로 연결
        Principal principal = accessTokenPrincipal(TokenScope.APP, 1L);
        SessionConnectedEvent connectedSess1 = new SessionConnectedEvent(this, messageWithSessionId("sess1"), principal);
        SessionConnectedEvent connectedSess2 = new SessionConnectedEvent(this, messageWithSessionId("sess2"), principal);

        // when: 두 세션 모두 연결
        webSocketEventListener.onConnected(connectedSess1);
        webSocketEventListener.onConnected(connectedSess2);

        // then: 각 세션 id로 markOnline이 호출된다
        then(chatPresenceStore).should().markOnline(TokenScope.APP, 1L, "sess1");
        then(chatPresenceStore).should().markOnline(TokenScope.APP, 1L, "sess2");

        // when: sess1만 연결 해제
        SessionDisconnectEvent disconnectSess1 =
            new SessionDisconnectEvent(this, messageWithSessionId("sess1"), "sess1", CloseStatus.NORMAL, principal);
        webSocketEventListener.onDisconnect(disconnectSess1);

        // then: sess1만 markOffline이 호출되고, sess2는 여전히 온라인(참조 카운트가 남아 있음)
        then(chatPresenceStore).should().markOffline(TokenScope.APP, 1L, "sess1");
        then(chatPresenceStore).should(never()).markOffline(TokenScope.APP, 1L, "sess2");
    }
}
