package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatMessage;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendChatMessage 테스트")
class SendChatMessageTest {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @InjectMocks
    private SendChatMessage sut;

    @Test
    @DisplayName("존재하지 않는 채팅방이면 NOT_FOUND")
    void execute_존재하지_않는_채팅방이면_NOT_FOUND() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(chatRoomQueryRepository.findById(100L)).willReturn(Optional.empty());

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);

        // when & then
        assertThatThrownBy(() -> sut.execute(user, request, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("유저(APP)가 NOTICE 전송시 CHAT_NOTICE_FORBIDDEN 예외")
    void execute_유저가_NOTICE_전송시_예외() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);

        given(chatRoomQueryRepository.findById(100L)).willReturn(Optional.of(directRoom));
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getType()).willReturn(ChatMessageType.NOTICE);

        // when & then
        assertThatThrownBy(() -> sut.execute(user, request, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_NOTICE_FORBIDDEN);

        then(chatMessageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("DIRECT 방에서 정상 전송시 저장·브로드캐스트·FCM 발송")
    void execute_DIRECT_정상_전송() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);
        given(directRoom.getParticipant1Id()).willReturn(1L);
        given(directRoom.getParticipant1Scope()).willReturn(TokenScope.APP);
        given(directRoom.getParticipant2Id()).willReturn(2L);

        given(chatRoomQueryRepository.findById(100L)).willReturn(Optional.of(directRoom));
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn("안녕하세요");

        ChatMessage savedMessage = ChatMessage.create(100L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "안녕하세요");
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);
        given(userQueryRepository.findById(1L)).willReturn(Optional.empty());

        // when
        sut.execute(user, request, 100L);

        // then
        then(chatMessageRepository).should().save(any(ChatMessage.class));
        then(messagingTemplate).should().convertAndSend(eq("/sub/chat.100"), any(Object.class));
        then(notificationService).should().sendNotificationOnly(eq(2L), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("GROUP 방에서 비멤버가 전송하면 NOT_FOUND")
    void execute_GROUP_비멤버면_NOT_FOUND() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);

        given(chatRoomQueryRepository.findById(200L)).willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.existsActive(200L, 1L, TokenScope.APP)).willReturn(false);

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);

        // when & then
        assertThatThrownBy(() -> sut.execute(user, request, 200L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);

        then(chatMessageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("GROUP 방 멤버가 NORMAL 전송시 FCM은 스킵되고 브로드캐스트만 발생")
    void execute_GROUP_멤버가_NORMAL_전송시_FCM_스킵() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getId()).willReturn(200L);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);

        given(chatRoomQueryRepository.findById(200L)).willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.existsActive(200L, 1L, TokenScope.APP)).willReturn(true);

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn("그룹 메시지");

        ChatMessage savedMessage = ChatMessage.create(200L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "그룹 메시지");
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

        // when
        sut.execute(user, request, 200L);

        // then
        then(messagingTemplate).should().convertAndSend(eq("/sub/chat.200"), any(Object.class));
        then(notificationService).should(never()).sendNotificationOnly(any(), any(), anyString(), anyString());
        assertThat(savedMessage.getType()).isEqualTo(ChatMessageType.NORMAL);
    }
}
