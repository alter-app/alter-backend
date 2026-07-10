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

    @Mock
    private ChatPresenceStore chatPresenceStore;

    @Mock
    private AttachFilesUseCase attachFilesUseCase;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileUrlService fileUrlService;

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
        given(directRoom.getParticipant2Scope()).willReturn(TokenScope.APP);

        given(chatRoomQueryRepository.findById(100L)).willReturn(Optional.of(directRoom));
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn("안녕하세요");

        ChatMessage savedMessage = ChatMessage.create(100L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "안녕하세요");
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);
        given(userQueryRepository.findById(1L)).willReturn(Optional.empty());
        given(chatPresenceStore.isOnline(TokenScope.APP, 2L)).willReturn(false);

        // when
        sut.execute(user, request, 100L);

        // then
        then(chatMessageRepository).should().save(any(ChatMessage.class));
        then(messagingTemplate).should().convertAndSend(eq("/sub/chat.100"), any(Object.class));
        then(notificationService).should().sendNotificationOnly(eq(2L), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("DIRECT 방에서 상대가 온라인이면 FCM 미발송")
    void execute_DIRECT_상대_온라인이면_FCM_미발송() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);
        given(directRoom.getParticipant1Id()).willReturn(1L);
        given(directRoom.getParticipant1Scope()).willReturn(TokenScope.APP);
        given(directRoom.getParticipant2Id()).willReturn(2L);
        given(directRoom.getParticipant2Scope()).willReturn(TokenScope.APP);

        given(chatRoomQueryRepository.findById(100L)).willReturn(Optional.of(directRoom));
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn("안녕하세요");

        ChatMessage savedMessage = ChatMessage.create(100L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "안녕하세요");
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);
        given(chatPresenceStore.isOnline(TokenScope.APP, 2L)).willReturn(true);

        // when
        sut.execute(user, request, 100L);

        // then
        then(messagingTemplate).should().convertAndSend(eq("/sub/chat.100"), any(Object.class));
        then(notificationService).should(never()).sendNotificationOnly(any(), any(), anyString(), anyString());
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
    @DisplayName("GROUP 방에 발신자 외 멤버가 없으면 FCM은 미발송, 브로드캐스트만 발생")
    void execute_GROUP_멤버가_NORMAL_전송시_FCM_스킵() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getId()).willReturn(200L);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);

        given(chatRoomQueryRepository.findById(200L)).willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.existsActive(200L, 1L, TokenScope.APP)).willReturn(true);
        given(chatRoomMemberQueryRepository.findActiveByRoom(200L)).willReturn(List.of());

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn("그룹 메시지");

        ChatMessage savedMessage = ChatMessage.create(200L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "그룹 메시지");
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);
        given(userQueryRepository.findById(1L)).willReturn(Optional.empty());

        // when
        sut.execute(user, request, 200L);

        // then
        then(messagingTemplate).should().convertAndSend(eq("/sub/chat.200"), any(Object.class));
        then(notificationService).should(never()).sendNotificationOnly(any(), any(), anyString(), anyString());
        assertThat(savedMessage.getType()).isEqualTo(ChatMessageType.NORMAL);
    }

    @Test
    @DisplayName("GROUP 방에서 발신자·온라인 멤버는 제외하고 오프라인 멤버에게만 FCM 발송")
    void execute_GROUP_오프라인_멤버에게만_FCM_발송() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getId()).willReturn(200L);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);

        given(chatRoomQueryRepository.findById(200L)).willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.existsActive(200L, 1L, TokenScope.APP)).willReturn(true);

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
        given(chatPresenceStore.isOnline(TokenScope.APP, 2L)).willReturn(true);
        given(chatPresenceStore.isOnline(TokenScope.APP, 3L)).willReturn(false);

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn("그룹 메시지");

        ChatMessage savedMessage = ChatMessage.create(200L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "그룹 메시지");
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);
        given(userQueryRepository.findById(1L)).willReturn(Optional.empty());

        // when
        sut.execute(user, request, 200L);

        // then
        then(notificationService).should().sendNotificationOnly(eq(3L), any(), anyString(), anyString());
        then(notificationService).should(never()).sendNotificationOnly(eq(1L), any(), anyString(), anyString());
        then(notificationService).should(never()).sendNotificationOnly(eq(2L), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("내용도 이미지도 없으면 CHAT_EMPTY_MESSAGE 예외, 저장 안 됨")
    void execute_내용도_이미지도_없으면_예외() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);

        given(chatRoomQueryRepository.findById(100L)).willReturn(Optional.of(directRoom));
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn(null);
        given(request.getFileIds()).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> sut.execute(user, request, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_EMPTY_MESSAGE);

        then(chatMessageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("첨부 이미지가 10개 초과이면 CHAT_TOO_MANY_ATTACHMENTS 예외, 저장 안 됨")
    void execute_첨부가_10개_초과이면_예외() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);

        given(chatRoomQueryRepository.findById(100L)).willReturn(Optional.of(directRoom));
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        List<String> fileIds = java.util.stream.IntStream.range(0, 11)
            .mapToObj(i -> "f" + i)
            .toList();

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn(null);
        given(request.getFileIds()).willReturn(fileIds);

        // when & then
        assertThatThrownBy(() -> sut.execute(user, request, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_TOO_MANY_ATTACHMENTS);

        then(chatMessageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("이미지만 있는 메시지 전송시 FCM 알림 본문은 '이름: 사진을 보냈습니다'")
    void execute_이미지만_있으면_FCM_본문_사진안내() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);
        given(directRoom.getParticipant1Id()).willReturn(1L);
        given(directRoom.getParticipant1Scope()).willReturn(TokenScope.APP);
        given(directRoom.getParticipant2Id()).willReturn(2L);
        given(directRoom.getParticipant2Scope()).willReturn(TokenScope.APP);

        given(chatRoomQueryRepository.findById(100L)).willReturn(Optional.of(directRoom));
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn(null);
        given(request.getFileIds()).willReturn(List.of("f1"));

        ChatMessage savedMessage = ChatMessage.create(100L, 1L, TokenScope.APP, ChatMessageType.NORMAL, null);
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

        User sender = mock(User.class);
        given(sender.getName()).willReturn("홍길동");
        given(userQueryRepository.findById(1L)).willReturn(Optional.of(sender));
        given(chatPresenceStore.isOnline(TokenScope.APP, 2L)).willReturn(false);

        given(fileQueryRepository.findAllByTargetTypeAndTargetIdIn(any(), any())).willReturn(List.of());

        // when
        sut.execute(user, request, 100L);

        // then
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        then(notificationService).should().sendNotificationOnly(eq(2L), any(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).isEqualTo("홍길동: 사진을 보냈습니다");
    }

    @Test
    @DisplayName("텍스트 메시지 전송시 FCM 알림 본문은 기존 텍스트 미리보기 유지")
    void execute_텍스트_메시지면_FCM_본문_기존유지() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);
        given(directRoom.getParticipant1Id()).willReturn(1L);
        given(directRoom.getParticipant1Scope()).willReturn(TokenScope.APP);
        given(directRoom.getParticipant2Id()).willReturn(2L);
        given(directRoom.getParticipant2Scope()).willReturn(TokenScope.APP);

        given(chatRoomQueryRepository.findById(100L)).willReturn(Optional.of(directRoom));
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn("안녕하세요");

        ChatMessage savedMessage = ChatMessage.create(100L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "안녕하세요");
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

        User sender = mock(User.class);
        given(sender.getName()).willReturn("홍길동");
        given(userQueryRepository.findById(1L)).willReturn(Optional.of(sender));
        given(chatPresenceStore.isOnline(TokenScope.APP, 2L)).willReturn(false);

        // when
        sut.execute(user, request, 100L);

        // then
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        then(notificationService).should().sendNotificationOnly(eq(2L), any(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).isEqualTo("홍길동: 안녕하세요");
    }

    @Test
    @DisplayName("이미지 첨부시 attachFilesUseCase 호출 및 브로드캐스트 payload에 attachments 포함")
    void execute_이미지_첨부시_attach_호출_및_payload_포함() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = mock(ChatRoom.class);
        given(directRoom.getId()).willReturn(100L);
        given(directRoom.getType()).willReturn(ChatRoomType.DIRECT);
        given(directRoom.getParticipant1Id()).willReturn(1L);
        given(directRoom.getParticipant1Scope()).willReturn(TokenScope.APP);
        given(directRoom.getParticipant2Id()).willReturn(2L);
        given(directRoom.getParticipant2Scope()).willReturn(TokenScope.APP);

        given(chatRoomQueryRepository.findById(100L)).willReturn(Optional.of(directRoom));
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn(null);
        given(request.getFileIds()).willReturn(List.of("f1", "f2"));

        ChatMessage savedMessage = ChatMessage.create(100L, 1L, TokenScope.APP, ChatMessageType.NORMAL, null);
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);
        given(userQueryRepository.findById(1L)).willReturn(Optional.empty());
        given(chatPresenceStore.isOnline(TokenScope.APP, 2L)).willReturn(false);

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
        sut.execute(user, request, 100L);

        // then
        then(attachFilesUseCase).should().execute(
            eq(List.of("f1", "f2")), eq(FileTargetType.CHAT_MESSAGE), anyString(), eq(1L)
        );

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        then(messagingTemplate).should().convertAndSend(eq("/sub/chat.100"), captor.capture());
        ChatMessageResponse payload = (ChatMessageResponse) captor.getValue();
        assertThat(payload.getAttachments()).containsExactly(dto1, dto2);
    }
}
