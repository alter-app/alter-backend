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

    @Test
    @DisplayName("CONNECT에서 provider가 미인증 Authentication을 반환하면 거부하고 user를 세팅하지 않는다")
    void preSend_CONNECT_미인증결과_거부() {
        // given: 토큰 타입 불일치·만료 처리 분기에서 provider가 details 없는 미인증 원본 토큰을 그대로 반환하는 상황
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionId("sess1");
        accessor.setLeaveMutable(true);
        accessor.setNativeHeader("Authorization", "Bearer expired-token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        AccessTokenAuthentication unauthenticated = new AccessTokenAuthentication("expired-token");
        given(accessTokenAuthenticationProvider.authenticate(any())).willReturn(unauthenticated);

        // when & then: 예외 메시지가 "WebSocket 인증 실패: WebSocket 인증 실패: ..."로 이중 래핑되면 안 된다
        // (미인증 거부 예외가 바로 아래 catch(Exception)에 다시 잡히면 발생하는 회귀).
        assertThatThrownBy(() -> interceptor.preSend(message, null))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("WebSocket 인증 실패: 유효하지 않은 토큰입니다.");
        StompHeaderAccessor result = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        assertThat(result.getUser()).isNull();
    }
}
