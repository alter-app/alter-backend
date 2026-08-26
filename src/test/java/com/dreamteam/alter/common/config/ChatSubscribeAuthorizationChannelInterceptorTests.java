package com.dreamteam.alter.common.config;

import com.dreamteam.alter.adapter.inbound.general.auth.dto.LoginUserDto;
import com.dreamteam.alter.application.auth.token.AccessTokenAuthentication;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatSubscribeAuthorizationChannelInterceptor 테스트")
class ChatSubscribeAuthorizationChannelInterceptorTests {

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @InjectMocks
    private ChatSubscribeAuthorizationChannelInterceptor interceptor;

    private Message<byte[]> subscribeMessage(String destination, Principal user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setSessionId("sess1");
        accessor.setDestination(destination);
        if (user != null) {
            accessor.setUser(user);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<byte[]> commandMessage(StompCommand command) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId("sess1");
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Principal accessTokenPrincipal(TokenScope scope, Long memberId) {
        LoginUserDto dto = mock(LoginUserDto.class);
        lenient().when(dto.getScope()).thenReturn(scope);
        lenient().when(dto.getId()).thenReturn(memberId);
        return new AccessTokenAuthentication("token", dto, Collections.emptyList());
    }

    @Test
    @DisplayName("활성 멤버의 구독은 통과하고 메시지를 그대로 반환한다")
    void preSend_활성멤버_통과() {
        // given
        Message<byte[]> message = subscribeMessage("/sub/chat.10", accessTokenPrincipal(TokenScope.APP, 1L));
        given(chatRoomMemberQueryRepository.existsActive(10L, 1L, TokenScope.APP)).willReturn(true);

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        assertThat(result).isSameAs(message);
    }

    @Test
    @DisplayName("MANAGER 스코프의 활성 멤버 구독도 통과하고 메시지를 그대로 반환한다")
    void preSend_MANAGER활성멤버_통과() {
        // given
        Message<byte[]> message = subscribeMessage("/sub/chat.10", accessTokenPrincipal(TokenScope.MANAGER, 2L));
        given(chatRoomMemberQueryRepository.existsActive(10L, 2L, TokenScope.MANAGER)).willReturn(true);

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        assertThat(result).isSameAs(message);
    }

    @Test
    @DisplayName("활성 멤버가 아니면 프레임을 드롭한다(null 반환, 세션 유지)")
    void preSend_비활성멤버_드롭() {
        // given
        Message<byte[]> message = subscribeMessage("/sub/chat.10", accessTokenPrincipal(TokenScope.APP, 1L));
        given(chatRoomMemberQueryRepository.existsActive(10L, 1L, TokenScope.APP)).willReturn(false);

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("인증 정보가 없으면 프레임을 드롭한다(null 반환, 세션 유지)")
    void preSend_인증정보없음_드롭() {
        // given
        Message<byte[]> message = subscribeMessage("/sub/chat.10", null);

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        assertThat(result).isNull();
        then(chatRoomMemberQueryRepository).should(never()).existsActive(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("알 수 없는 principal이면 프레임을 드롭한다(null 반환, 세션 유지)")
    void preSend_알수없는principal_드롭() {
        // given
        Message<byte[]> message = subscribeMessage("/sub/chat.10", new TestingAuthenticationToken("foo", "bar"));

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        assertThat(result).isNull();
        then(chatRoomMemberQueryRepository).should(never()).existsActive(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("SUBSCRIBE가 아니면 리포지토리 조회 없이 통과한다")
    void preSend_SUBSCRIBE아님_통과() {
        // given
        Message<byte[]> message = commandMessage(StompCommand.SEND);

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        assertThat(result).isSameAs(message);
        then(chatRoomMemberQueryRepository).should(never()).existsActive(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("/sub/chat.으로 시작하지 않는 destination은 리포지토리 조회 없이 통과한다")
    void preSend_다른destination_통과() {
        // given
        Message<byte[]> message = subscribeMessage("/sub/other.10", accessTokenPrincipal(TokenScope.APP, 1L));

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        assertThat(result).isSameAs(message);
        then(chatRoomMemberQueryRepository).should(never()).existsActive(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("roomId가 숫자가 아니면 리포지토리 조회 없이 프레임을 드롭한다")
    void preSend_roomId숫자아님_드롭() {
        // given
        Message<byte[]> message = subscribeMessage("/sub/chat.abc", accessTokenPrincipal(TokenScope.APP, 1L));

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        assertThat(result).isNull();
        then(chatRoomMemberQueryRepository).should(never()).existsActive(anyLong(), anyLong(), any());
    }
}
