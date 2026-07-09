package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.SyncWorkspaceChatMembershipUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMembershipSyncEventListener 테스트")
class ChatMembershipSyncEventListenerTest {

    @Mock
    private SyncWorkspaceChatMembershipUseCase syncWorkspaceChatMembership;

    @InjectMocks
    private ChatMembershipSyncEventListener listener;

    @Test
    @DisplayName("onJoined 은 이벤트 필드로 sync.join 을 호출한다")
    void onJoined_sync_join_호출() {
        // given
        ChatMembershipJoinedEvent event = new ChatMembershipJoinedEvent(1L, 2L, TokenScope.MANAGER);

        // when
        listener.onJoined(event);

        // then
        then(syncWorkspaceChatMembership).should().join(1L, 2L, TokenScope.MANAGER);
    }

    @Test
    @DisplayName("onJoined 에서 sync.join 이 예외를 던져도 삼키고 재전파하지 않는다")
    void onJoined_예외삼킴() {
        // given
        ChatMembershipJoinedEvent event = new ChatMembershipJoinedEvent(1L, 2L, TokenScope.MANAGER);
        willThrow(new RuntimeException("chat sync failed"))
            .given(syncWorkspaceChatMembership).join(1L, 2L, TokenScope.MANAGER);

        // when & then
        Assertions.assertThatCode(() -> listener.onJoined(event)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("onLeft 은 이벤트 필드로 sync.leave 를 호출한다")
    void onLeft_sync_leave_호출() {
        // given
        ChatMembershipLeftEvent event = new ChatMembershipLeftEvent(3L, 4L, TokenScope.APP);

        // when
        listener.onLeft(event);

        // then
        then(syncWorkspaceChatMembership).should().leave(3L, 4L, TokenScope.APP);
    }

    @Test
    @DisplayName("onLeft 에서 sync.leave 가 예외를 던져도 삼키고 재전파하지 않는다")
    void onLeft_예외삼킴() {
        // given
        ChatMembershipLeftEvent event = new ChatMembershipLeftEvent(3L, 4L, TokenScope.APP);
        willThrow(new RuntimeException("chat sync failed"))
            .given(syncWorkspaceChatMembership).leave(3L, 4L, TokenScope.APP);

        // when & then
        Assertions.assertThatCode(() -> listener.onLeft(event)).doesNotThrowAnyException();
    }
}
