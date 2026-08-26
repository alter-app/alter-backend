package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatSessionRevocationBroadcaster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatSessionRevokeEventListener 테스트")
class ChatSessionRevokeEventListenerTests {

    @Mock
    private ChatSessionRevocationBroadcaster chatSessionRevocationBroadcaster;

    @InjectMocks
    private ChatSessionRevokeEventListener sut;

    @Test
    @DisplayName("이벤트를 받으면 broadcaster에 그대로 위임한다")
    void onRevoked_broadcaster에_위임() {
        // given
        ChatSessionRevokeEvent event = new ChatSessionRevokeEvent(TokenScope.APP, 1L, 100L);

        // when
        sut.onRevoked(event);

        // then
        then(chatSessionRevocationBroadcaster).should().revoke(TokenScope.APP, 1L, 100L);
    }

    @Test
    @DisplayName("broadcaster가 예외를 던져도 리스너는 예외를 전파하지 않는다")
    void onRevoked_broadcaster_예외시_전파안함() {
        // given
        ChatSessionRevokeEvent event = new ChatSessionRevokeEvent(TokenScope.APP, 1L, 100L);
        willThrow(new RuntimeException("redis down"))
            .given(chatSessionRevocationBroadcaster).revoke(TokenScope.APP, 1L, 100L);

        // when & then
        assertThatCode(() -> sut.onRevoked(event)).doesNotThrowAnyException();
    }
}
