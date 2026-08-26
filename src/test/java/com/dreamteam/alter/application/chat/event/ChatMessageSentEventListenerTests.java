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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
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
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @InjectMocks
    private ChatMessageSentEventListener listener;

    private ChatMessageResponse messageResponse(Long chatRoomId, Long senderId, String content, String senderName) {
        return new ChatMessageResponse(
            1L, chatRoomId, senderId, TokenScope.APP, senderName, ChatMessageType.NORMAL, content, LocalDateTime.now()
        );
    }

    private ChatMessageSentEvent event(ChatRoom chatRoom, Long senderId, TokenScope senderScope, String content) {
        return event(chatRoom, senderId, senderScope, content, "발신자");
    }

    private ChatMessageSentEvent event(
        ChatRoom chatRoom, Long senderId, TokenScope senderScope, String content, String senderName
    ) {
        return new ChatMessageSentEvent(
            chatRoom, senderId, senderScope, content,
            messageResponse(chatRoom.getId(), senderId, content, senderName)
        );
    }

    private ChatRoomMember member(Long memberId, TokenScope scope) {
        ChatRoomMember member = mock(ChatRoomMember.class);
        given(member.getMemberId()).willReturn(memberId);
        given(member.getMemberScope()).willReturn(scope);
        return member;
    }

    @Test
    @DisplayName("DIRECT 상대가 나갔으면(비활성 멤버) FCM 미발송")
    void onSent_DIRECT_상대가_나갔으면_FCM_미발송() {
        // given
        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);

        // 나간 상대는 findActiveByRoom 결과에 포함되지 않는다
        ChatRoomMember senderMember = member(1L, TokenScope.APP);
        given(chatRoomMemberQueryRepository.findActiveByRoom(100L))
            .willReturn(List.of(senderMember));

        // when
        listener.onSent(event(directRoom, 1L, TokenScope.APP, "안녕하세요"));

        // then
        then(chatMessageBroadcaster).should().broadcast(eq(100L), any(ChatMessageResponse.class));
        then(notificationService).should(never()).sendNotificationOnlyToMany(any(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("DIRECT 상대가 활성 + 오프라인이면 FCM 발송, 발신자 본인에게는 안 감")
    void onSent_DIRECT_상대_활성_오프라인이면_FCM_발송() {
        // given
        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);

        ChatRoomMember senderMember = member(1L, TokenScope.APP);
        ChatRoomMember opponentMember = member(2L, TokenScope.APP);
        given(chatRoomMemberQueryRepository.findActiveByRoom(100L))
            .willReturn(List.of(senderMember, opponentMember));
        given(chatPresenceStore.filterOnline(any())).willReturn(Set.of());

        // when
        listener.onSent(event(directRoom, 1L, TokenScope.APP, "안녕하세요"));

        // then
        then(chatMessageBroadcaster).should().broadcast(eq(100L), any(ChatMessageResponse.class));
        then(notificationService).should()
            .sendNotificationOnlyToMany(eq(List.of(2L)), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("DIRECT 상대가 온라인이면 브로드캐스트만, FCM 미발송")
    void onSent_DIRECT_상대_온라인이면_FCM_미발송() {
        // given
        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);

        ChatRoomMember senderMember = member(1L, TokenScope.APP);
        ChatRoomMember opponentMember = member(2L, TokenScope.APP);
        given(chatRoomMemberQueryRepository.findActiveByRoom(100L))
            .willReturn(List.of(senderMember, opponentMember));
        given(chatPresenceStore.filterOnline(any()))
            .willReturn(Set.of(new ChatPresenceStore.PresenceTarget(TokenScope.APP, 2L)));

        // when
        listener.onSent(event(directRoom, 1L, TokenScope.APP, "안녕하세요"));

        // then
        then(chatMessageBroadcaster).should().broadcast(eq(100L), any(ChatMessageResponse.class));
        then(notificationService).should(never()).sendNotificationOnlyToMany(any(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("GROUP 발신자·온라인 멤버는 제외하고 오프라인 멤버에게만 FCM 발송")
    void onSent_GROUP_오프라인_멤버에게만_FCM() {
        // given
        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getId()).willReturn(200L);

        ChatRoomMember senderMember = member(1L, TokenScope.APP);
        ChatRoomMember onlineMember = member(2L, TokenScope.APP);
        ChatRoomMember offlineMember = member(3L, TokenScope.APP);

        given(chatRoomMemberQueryRepository.findActiveByRoom(200L))
            .willReturn(List.of(senderMember, onlineMember, offlineMember));
        // 온라인 멤버(2)만 online으로 반환 → 오프라인(3)에게만 발송
        given(chatPresenceStore.filterOnline(any()))
            .willReturn(Set.of(new ChatPresenceStore.PresenceTarget(TokenScope.APP, 2L)));

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

        ChatRoomMember senderMember = member(1L, TokenScope.APP);
        ChatRoomMember opponentMember = member(2L, TokenScope.APP);
        given(chatRoomMemberQueryRepository.findActiveByRoom(100L))
            .willReturn(List.of(senderMember, opponentMember));
        given(chatPresenceStore.filterOnline(any())).willReturn(Set.of());

        // when
        listener.onSent(event(directRoom, 1L, TokenScope.APP, null, "홍길동"));

        // then
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        then(notificationService).should()
            .sendNotificationOnlyToMany(eq(List.of(2L)), any(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).isEqualTo("홍길동: 사진을 보냈습니다");
    }

    @Test
    @DisplayName("텍스트 메시지면 FCM 본문은 텍스트 미리보기")
    void onSent_텍스트면_FCM_본문_미리보기() {
        // given
        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);

        ChatRoomMember senderMember = member(1L, TokenScope.APP);
        ChatRoomMember opponentMember = member(2L, TokenScope.APP);
        given(chatRoomMemberQueryRepository.findActiveByRoom(100L))
            .willReturn(List.of(senderMember, opponentMember));
        given(chatPresenceStore.filterOnline(any())).willReturn(Set.of());

        // when
        listener.onSent(event(directRoom, 1L, TokenScope.APP, "안녕하세요", "홍길동"));

        // then
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        then(notificationService).should()
            .sendNotificationOnlyToMany(eq(List.of(2L)), any(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).isEqualTo("홍길동: 안녕하세요");
    }
}
