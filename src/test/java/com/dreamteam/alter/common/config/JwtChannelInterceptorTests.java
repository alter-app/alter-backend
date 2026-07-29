package com.dreamteam.alter.common.config;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.application.auth.provider.AccessTokenAuthenticationProvider;
import com.dreamteam.alter.application.auth.token.AccessTokenAuthentication;
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
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtChannelInterceptor 테스트")
class JwtChannelInterceptorTests {

    @Mock
    private AccessTokenAuthenticationProvider accessTokenAuthenticationProvider;

    @InjectMocks
    private JwtChannelInterceptor interceptor;

    private AccessTokenAuthentication authenticatedToken(String token) {
        return new AccessTokenAuthentication(token, mock(LoginUserDto.class), Collections.emptyList());
    }

    @Test
    @DisplayName("세션 종료 시 합성 DISCONNECT(immutable 헤더)는 인증 없이 통과시킨다")
    void preSend_합성DISCONNECT_immutable_통과() {
        // given: afterSessionEnded가 만드는 합성 DISCONNECT — 세션 user는 있지만 헤더는 immutable
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId("sess1");
        accessor.setUser(authenticatedToken("stored-token"));
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        // getMessageHeaders() 호출로 accessor는 immutable 상태

        // when & then
        assertThatCode(() -> interceptor.preSend(message, null)).doesNotThrowAnyException();
        then(accessTokenAuthenticationProvider).should(never()).authenticate(any());
    }

    @Test
    @DisplayName("CONNECT에 Authorization 헤더가 있으면 인증 후 user를 세팅한다")
    void preSend_CONNECT_인증성공_user세팅() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionId("sess1");
        accessor.setLeaveMutable(true);
        accessor.setNativeHeader("Authorization", "Bearer valid-token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Authentication authenticated = authenticatedToken("valid-token");
        given(accessTokenAuthenticationProvider.authenticate(any())).willReturn(authenticated);

        // when
        interceptor.preSend(message, null);

        // then
        StompHeaderAccessor result = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        assertThat(result.getUser()).isSameAs(authenticated);
    }

    @Test
    @DisplayName("CONNECT에 Authorization 헤더가 없으면 예외를 던진다")
    void preSend_CONNECT_헤더없음_예외() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionId("sess1");
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(message, null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Authorization");
    }
}
