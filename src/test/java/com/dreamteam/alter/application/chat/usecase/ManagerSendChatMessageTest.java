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
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerSendChatMessage 테스트")
class ManagerSendChatMessageTest {

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
    private ManagerSendChatMessage sut;

    @Test
    @DisplayName("매니저가 GROUP 방 멤버로서 NOTICE 전송시 type=NOTICE로 저장되고 브로드캐스트됨")
    void execute_매니저가_GROUP_방에서_NOTICE_전송시_저장됨() {
        // given
        User innerUser = mock(User.class);
        given(innerUser.getId()).willReturn(2L);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(managerUser.getUser()).willReturn(innerUser);

        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getId()).willReturn(5L);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);

        given(chatRoomQueryRepository.findById(5L)).willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.existsActive(5L, 2L, TokenScope.MANAGER)).willReturn(true);

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getType()).willReturn(ChatMessageType.NOTICE);
        given(request.getContent()).willReturn("공지 내용");

        ChatMessage savedMessage = ChatMessage.create(5L, 2L, TokenScope.MANAGER, ChatMessageType.NOTICE, "공지 내용");
        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        given(chatMessageRepository.save(captor.capture())).willReturn(savedMessage);

        // when
        sut.execute(managerUser, request, 5L);

        // then
        assertThat(captor.getValue().getType()).isEqualTo(ChatMessageType.NOTICE);
        then(messagingTemplate).should().convertAndSend(eq("/sub/chat.5"), any(Object.class));
        then(notificationService).should(never()).sendNotificationOnly(any(), any(), any(), any());
    }

    @Test
    @DisplayName("GROUP 방 비멤버 매니저가 전송하면 NOT_FOUND")
    void execute_GROUP_비멤버_매니저면_NOT_FOUND() {
        // given
        User innerUser = mock(User.class);
        given(innerUser.getId()).willReturn(2L);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(managerUser.getUser()).willReturn(innerUser);

        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);

        given(chatRoomQueryRepository.findById(5L)).willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.existsActive(5L, 2L, TokenScope.MANAGER)).willReturn(false);

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);

        // when & then
        assertThatThrownBy(() -> sut.execute(managerUser, request, 5L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);

        then(chatMessageRepository).should(never()).save(any());
    }
}
