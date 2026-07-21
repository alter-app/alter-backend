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

import java.security.Principal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("PresenceHeartbeatChannelInterceptor 테스트")
class PresenceHeartbeatChannelInterceptorTests {

    @Mock
    private ChatPresenceStore chatPresenceStore;

    @InjectMocks
    private PresenceHeartbeatChannelInterceptor interceptor;

    private Message<byte[]> inboundMessage(Principal user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setSessionId("sess1");
        if (user != null) {
            accessor.setUser(user);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Principal accessTokenPrincipal(TokenScope scope, Long memberId) {
        LoginUserDto dto = mock(LoginUserDto.class);
        lenient().when(dto.getScope()).thenReturn(scope);
        lenient().when(dto.getId()).thenReturn(memberId);
        return new AccessTokenAuthentication("token", dto, Collections.emptyList());
    }

    @Test
    @DisplayName("인바운드 프레임에 인증된 사용자가 있으면 presence TTL을 갱신(touch)한다")
    void preSend_인증된사용자_touch() {
        // given
        Message<byte[]> message = inboundMessage(accessTokenPrincipal(TokenScope.APP, 1L));

        // when
        interceptor.preSend(message, null);

        // then
        then(chatPresenceStore).should().touch(TokenScope.APP, 1L);
    }

    @Test
    @DisplayName("user가 없으면 touch 호출 안함")
    void preSend_user없음_touch안함() {
        // given
        Message<byte[]> message = inboundMessage(null);

        // when
        interceptor.preSend(message, null);

        // then
        then(chatPresenceStore).should(never()).touch(any(), any());
    }

    @Test
    @DisplayName("LoginUserDto가 아닌 principal이면 touch 호출 안함")
    void preSend_알수없는principal_touch안함() {
        // given
        Message<byte[]> message = inboundMessage(new TestingAuthenticationToken("foo", "bar"));

        // when
        interceptor.preSend(message, null);

        // then
        then(chatPresenceStore).should(never()).touch(any(), any());
    }
}
