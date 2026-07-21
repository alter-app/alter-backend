package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import com.dreamteam.alter.application.chat.event.ChatMessageSentEvent;
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
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @Mock
    private AttachFilesUseCase attachFilesUseCase;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileUrlService fileUrlService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ManagerSendChatMessage sut;

    @Test
    @DisplayName("매니저가 GROUP 방 멤버로서 NOTICE 전송시 type=NOTICE로 저장되고 이벤트 발행됨")
    void execute_매니저가_GROUP_방에서_NOTICE_전송시_저장됨() {
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
        given(request.getType()).willReturn(ChatMessageType.NOTICE);
        given(request.getContent()).willReturn("공지 내용");

        ChatMessage savedMessage = ChatMessage.create(5L, 2L, TokenScope.MANAGER, ChatMessageType.NOTICE, "공지 내용");
        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        given(chatMessageRepository.save(captor.capture())).willReturn(savedMessage);

        // when
        sut.execute(managerUser, request, 5L);

        // then
        assertThat(captor.getValue().getType()).isEqualTo(ChatMessageType.NOTICE);
        then(eventPublisher).should().publishEvent(any(ChatMessageSentEvent.class));
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
        then(eventPublisher).should(never()).publishEvent(any(ChatMessageSentEvent.class));
    }

    @Test
    @DisplayName("내용도 이미지도 없으면 빈 메시지 예외, 저장 안 됨")
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
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ILLEGAL_ARGUMENT)
            .hasMessage("내용 또는 이미지를 첨부해야 합니다.");

        then(chatMessageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("첨부 이미지가 10개 초과이면 첨부 개수 예외, 저장 안 됨")
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
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ILLEGAL_ARGUMENT)
            .hasMessage("이미지는 최대 10개까지 첨부할 수 있습니다.");

        then(chatMessageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("이미지 첨부시 attachFilesUseCase 호출 및 이벤트 payload에 attachments 포함")
    void execute_이미지_첨부시_attach_호출_및_payload_포함() {
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

        ArgumentCaptor<ChatMessageSentEvent> captor = ArgumentCaptor.forClass(ChatMessageSentEvent.class);
        then(eventPublisher).should().publishEvent(captor.capture());
        assertThat(captor.getValue().getMessageResponse().getAttachments()).containsExactly(dto1, dto2);
    }
}
