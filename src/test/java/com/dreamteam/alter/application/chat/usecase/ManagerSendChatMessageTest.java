package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.application.notification.NotificationService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatMessage;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatPresenceStore;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
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

import java.util.List;
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

    @Mock
    private ChatPresenceStore chatPresenceStore;

    @Mock
    private AttachFilesUseCase attachFilesUseCase;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileUrlService fileUrlService;

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
        given(chatRoomMemberQueryRepository.findActiveByRoom(5L)).willReturn(List.of());

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
    @DisplayName("매니저가 GROUP 방에서 NORMAL 전송시 오프라인 멤버에게만 FCM 발송")
    void execute_매니저가_GROUP_방에서_NORMAL_전송시_오프라인_멤버에게_FCM_발송() {
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

        ChatRoomMember senderMember = mock(ChatRoomMember.class);
        given(senderMember.getMemberId()).willReturn(2L);
        given(senderMember.getMemberScope()).willReturn(TokenScope.MANAGER);

        ChatRoomMember offlineMember = mock(ChatRoomMember.class);
        given(offlineMember.getMemberId()).willReturn(10L);
        given(offlineMember.getMemberScope()).willReturn(TokenScope.APP);

        given(chatRoomMemberQueryRepository.findActiveByRoom(5L))
            .willReturn(List.of(senderMember, offlineMember));
        given(chatPresenceStore.isOnline(TokenScope.APP, 10L)).willReturn(false);

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getType()).willReturn(ChatMessageType.NORMAL);
        given(request.getContent()).willReturn("일반 메시지");

        ChatMessage savedMessage = ChatMessage.create(5L, 2L, TokenScope.MANAGER, ChatMessageType.NORMAL, "일반 메시지");
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);
        given(userQueryRepository.findById(2L)).willReturn(Optional.empty());

        // when
        sut.execute(managerUser, request, 5L);

        // then
        then(notificationService).should().sendNotificationOnly(eq(10L), any(), any(), any());
        then(notificationService).should(never()).sendNotificationOnly(eq(2L), any(), any(), any());
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

    @Test
    @DisplayName("내용도 이미지도 없으면 CHAT_EMPTY_MESSAGE 예외, 저장 안 됨")
    void execute_내용도_이미지도_없으면_예외() {
        // given
        User innerUser = mock(User.class);
        given(innerUser.getId()).willReturn(2L);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(managerUser.getUser()).willReturn(innerUser);

        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);

        given(chatRoomQueryRepository.findById(5L)).willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.existsActive(5L, 2L, TokenScope.MANAGER)).willReturn(true);

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn(null);
        given(request.getFileIds()).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> sut.execute(managerUser, request, 5L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_EMPTY_MESSAGE);

        then(chatMessageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("첨부 이미지가 10개 초과이면 CHAT_TOO_MANY_ATTACHMENTS 예외, 저장 안 됨")
    void execute_첨부가_10개_초과이면_예외() {
        // given
        User innerUser = mock(User.class);
        given(innerUser.getId()).willReturn(2L);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(managerUser.getUser()).willReturn(innerUser);

        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);

        given(chatRoomQueryRepository.findById(5L)).willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.existsActive(5L, 2L, TokenScope.MANAGER)).willReturn(true);

        List<String> fileIds = java.util.stream.IntStream.range(0, 11)
            .mapToObj(i -> "f" + i)
            .toList();

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn(null);
        given(request.getFileIds()).willReturn(fileIds);

        // when & then
        assertThatThrownBy(() -> sut.execute(managerUser, request, 5L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_TOO_MANY_ATTACHMENTS);

        then(chatMessageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("이미지 첨부시 attachFilesUseCase 호출 및 브로드캐스트 payload에 attachments 포함")
    void execute_이미지_첨부시_attach_호출_및_payload_포함() {
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
        given(chatRoomMemberQueryRepository.findActiveByRoom(5L)).willReturn(List.of());

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn(null);
        given(request.getFileIds()).willReturn(List.of("f1", "f2"));

        ChatMessage savedMessage = ChatMessage.create(5L, 2L, TokenScope.MANAGER, ChatMessageType.NORMAL, null);
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

        File file1 = mock(File.class);
        File file2 = mock(File.class);
        given(fileQueryRepository.findAllByTargetTypeAndTargetIdIn(
            eq(FileTargetType.CHAT_MESSAGE), eq(List.of(String.valueOf(savedMessage.getId())))
        )).willReturn(List.of(file1, file2));

        FileResponseDto dto1 = FileResponseDto.of(file1, "https://cdn.example.com/f1");
        FileResponseDto dto2 = FileResponseDto.of(file2, "https://cdn.example.com/f2");
        given(fileUrlService.resolve(file1)).willReturn(dto1);
        given(fileUrlService.resolve(file2)).willReturn(dto2);

        // when
        sut.execute(managerUser, request, 5L);

        // then
        then(attachFilesUseCase).should().execute(
            eq(List.of("f1", "f2")), eq(FileTargetType.CHAT_MESSAGE), anyString(), eq(2L)
        );

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        then(messagingTemplate).should().convertAndSend(eq("/sub/chat.5"), captor.capture());
        ChatMessageResponse payload = (ChatMessageResponse) captor.getValue();
        assertThat(payload.getAttachments()).containsExactly(dto1, dto2);
    }
}
