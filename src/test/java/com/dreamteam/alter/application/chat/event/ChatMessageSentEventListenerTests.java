package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageBroadcaster;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageSentEventListener 테스트")
class ChatMessageSentEventListenerTests {

    @Mock
    private ChatMessageBroadcaster chatMessageBroadcaster;

    @Mock
    private ChatPresenceStore chatPresenceStore;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @InjectMocks
    private ChatMessageSentEventListener listener;

    private ChatMessageResponse messageResponse(Long chatRoomId, Long senderId, String content) {
        return new ChatMessageResponse(
            1L, chatRoomId, senderId, TokenScope.APP, "홍길동", ChatMessageType.NORMAL, content, LocalDateTime.now()
        );
    }

    private ChatMessageSentEvent event(ChatRoom chatRoom, Long senderId, TokenScope senderScope, String content) {
        return new ChatMessageSentEvent(
            chatRoom, senderId, senderScope, content,
            messageResponse(chatRoom.getId(), senderId, content)
        );
    }

    @Test
    @DisplayName("DIRECT 상대가 오프라인이면 브로드캐스트 + 상대에게 FCM 발송")
    void onSent_DIRECT_상대_오프라인이면_FCM_발송() {
        // given
        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);
        given(directRoom.getParticipant1Id()).willReturn(1L);
        given(directRoom.getParticipant1Scope()).willReturn(TokenScope.APP);
        given(directRoom.getParticipant2Id()).willReturn(2L);
        given(directRoom.getParticipant2Scope()).willReturn(TokenScope.APP);

        given(userQueryRepository.findById(1L)).willReturn(Optional.empty());
        given(chatPresenceStore.isOnline(TokenScope.APP, 2L)).willReturn(false);

        // when
        listener.onSent(event(directRoom, 1L, TokenScope.APP, "안녕하세요"));

        // then
        then(chatMessageBroadcaster).should().broadcast(eq(100L), any(ChatMessageResponse.class));
        then(notificationService).should().sendNotificationOnly(eq(2L), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("DIRECT 상대가 온라인이면 브로드캐스트만, FCM 미발송")
    void onSent_DIRECT_상대_온라인이면_FCM_미발송() {
        // given
        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);
        given(directRoom.getParticipant1Id()).willReturn(1L);
        given(directRoom.getParticipant1Scope()).willReturn(TokenScope.APP);
        given(directRoom.getParticipant2Id()).willReturn(2L);
        given(directRoom.getParticipant2Scope()).willReturn(TokenScope.APP);

        given(chatPresenceStore.isOnline(TokenScope.APP, 2L)).willReturn(true);

        // when
        listener.onSent(event(directRoom, 1L, TokenScope.APP, "안녕하세요"));

        // then
        then(chatMessageBroadcaster).should().broadcast(eq(100L), any(ChatMessageResponse.class));
        then(notificationService).should(never()).sendNotificationOnly(any(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("GROUP 발신자·온라인 멤버는 제외하고 오프라인 멤버에게만 FCM 발송")
    void onSent_GROUP_오프라인_멤버에게만_FCM() {
        // given
        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getId()).willReturn(200L);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);

        ChatRoomMember senderMember = mock(ChatRoomMember.class);
        given(senderMember.getMemberId()).willReturn(1L);
        given(senderMember.getMemberScope()).willReturn(TokenScope.APP);

        ChatRoomMember onlineMember = mock(ChatRoomMember.class);
        given(onlineMember.getMemberId()).willReturn(2L);
        given(onlineMember.getMemberScope()).willReturn(TokenScope.APP);

        ChatRoomMember offlineMember = mock(ChatRoomMember.class);
        given(offlineMember.getMemberId()).willReturn(3L);
        given(offlineMember.getMemberScope()).willReturn(TokenScope.APP);

        given(chatRoomMemberQueryRepository.findActiveByRoom(200L))
            .willReturn(List.of(senderMember, onlineMember, offlineMember));
        // 온라인 멤버(2)만 online으로 반환 → 오프라인(3)에게만 발송
        given(chatPresenceStore.filterOnline(any()))
            .willReturn(Set.of(new ChatPresenceStore.PresenceTarget(TokenScope.APP, 2L)));
        given(userQueryRepository.findById(1L)).willReturn(Optional.empty());

        // when
        listener.onSent(event(groupRoom, 1L, TokenScope.APP, "그룹 메시지"));

        // then
        then(chatMessageBroadcaster).should().broadcast(eq(200L), any(ChatMessageResponse.class));
        then(notificationService).should()
            .sendNotificationOnlyToMany(eq(List.of(3L)), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("GROUP 발신자 외 멤버가 없으면 브로드캐스트만, FCM 미발송")
    void onSent_GROUP_멤버없음_FCM_스킵() {
        // given
        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getId()).willReturn(200L);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);

        given(chatRoomMemberQueryRepository.findActiveByRoom(200L)).willReturn(List.of());

        // when
        listener.onSent(event(groupRoom, 1L, TokenScope.APP, "그룹 메시지"));

        // then
        then(chatMessageBroadcaster).should().broadcast(eq(200L), any(ChatMessageResponse.class));
        then(notificationService).should(never()).sendNotificationOnlyToMany(any(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("이미지만 있는 메시지면 FCM 본문은 '이름: 사진을 보냈습니다'")
    void onSent_이미지만이면_FCM_본문_사진안내() {
        // given
        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);
        given(directRoom.getParticipant1Id()).willReturn(1L);
        given(directRoom.getParticipant1Scope()).willReturn(TokenScope.APP);
        given(directRoom.getParticipant2Id()).willReturn(2L);
        given(directRoom.getParticipant2Scope()).willReturn(TokenScope.APP);

        User sender = mock(User.class);
        given(sender.getName()).willReturn("홍길동");
        given(userQueryRepository.findById(1L)).willReturn(Optional.of(sender));
        given(chatPresenceStore.isOnline(TokenScope.APP, 2L)).willReturn(false);

        // when
        listener.onSent(event(directRoom, 1L, TokenScope.APP, null));

        // then
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        then(notificationService).should().sendNotificationOnly(eq(2L), any(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).isEqualTo("홍길동: 사진을 보냈습니다");
    }

    @Test
    @DisplayName("텍스트 메시지면 FCM 본문은 텍스트 미리보기")
    void onSent_텍스트면_FCM_본문_미리보기() {
        // given
        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);
        given(directRoom.getParticipant1Id()).willReturn(1L);
        given(directRoom.getParticipant1Scope()).willReturn(TokenScope.APP);
        given(directRoom.getParticipant2Id()).willReturn(2L);
        given(directRoom.getParticipant2Scope()).willReturn(TokenScope.APP);

        User sender = mock(User.class);
        given(sender.getName()).willReturn("홍길동");
        given(userQueryRepository.findById(1L)).willReturn(Optional.of(sender));
        given(chatPresenceStore.isOnline(TokenScope.APP, 2L)).willReturn(false);

        // when
        listener.onSent(event(directRoom, 1L, TokenScope.APP, "안녕하세요"));

        // then
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        then(notificationService).should().sendNotificationOnly(eq(2L), any(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).isEqualTo("홍길동: 안녕하세요");
    }
}
