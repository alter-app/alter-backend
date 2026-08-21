package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import com.dreamteam.alter.application.chat.event.ChatMessageSentEvent;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatMessage;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
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
@DisplayName("SendChatMessage 테스트")
class SendChatMessageTests {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private AttachFilesUseCase attachFilesUseCase;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileUrlService fileUrlService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SendChatMessage sut;

    @Test
    @DisplayName("존재하지 않는 채팅방이면 NOT_FOUND")
    void execute_존재하지_않는_채팅방이면_NOT_FOUND() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP)).willReturn(Optional.empty());

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);

        // when & then
        assertThatThrownBy(() -> sut.execute(user, request, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
        then(chatMessageRepository).should(never()).save(any());
        then(eventPublisher).should(never()).publishEvent(any(ChatMessageSentEvent.class));
    }

    @Test
    @DisplayName("유저(APP)가 NOTICE 전송시 공지 권한 예외")
    void execute_유저가_NOTICE_전송시_예외() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = ChatRoom.create(1L, TokenScope.APP, 2L, TokenScope.APP);
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP)).willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getType()).willReturn(ChatMessageType.NOTICE);

        // when & then
        assertThatThrownBy(() -> sut.execute(user, request, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ILLEGAL_ARGUMENT)
            .hasMessage("공지는 매니저만 작성할 수 있습니다.");

        then(chatMessageRepository).should(never()).save(any());
        then(eventPublisher).should(never()).publishEvent(any(ChatMessageSentEvent.class));
    }

    @Test
    @DisplayName("DIRECT 방에서 정상 전송시 저장 후 ChatMessageSentEvent 발행")
    void execute_DIRECT_정상_전송시_이벤트_발행() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = ChatRoom.create(1L, TokenScope.APP, 2L, TokenScope.APP);
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP)).willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn("안녕하세요");

        ChatMessage savedMessage = ChatMessage.create(100L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "안녕하세요");
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

        // when
        sut.execute(user, request, 100L);

        // then
        then(chatMessageRepository).should().save(any(ChatMessage.class));

        ArgumentCaptor<ChatMessageSentEvent> captor = ArgumentCaptor.forClass(ChatMessageSentEvent.class);
        then(eventPublisher).should().publishEvent(captor.capture());
        ChatMessageSentEvent event = captor.getValue();
        assertThat(event.getChatRoom()).isSameAs(directRoom);
        assertThat(event.getSenderId()).isEqualTo(1L);
        assertThat(event.getSenderScope()).isEqualTo(TokenScope.APP);
        assertThat(event.getContent()).isEqualTo("안녕하세요");
        assertThat(event.getMessageResponse().getType()).isEqualTo(ChatMessageType.NORMAL);
    }

    @Test
    @DisplayName("GROUP 방 멤버가 정상 전송시 저장 후 이벤트 발행")
    void execute_GROUP_정상_전송시_이벤트_발행() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom groupRoom = mock(ChatRoom.class);

        given(chatRoomQueryRepository.findByIdAndParticipant(200L, 1L, TokenScope.APP)).willReturn(Optional.of(groupRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn("그룹 메시지");

        ChatMessage savedMessage = ChatMessage.create(200L, 1L, TokenScope.APP, ChatMessageType.NORMAL, "그룹 메시지");
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

        // when
        sut.execute(user, request, 200L);

        // then
        then(chatMessageRepository).should().save(any(ChatMessage.class));

        ArgumentCaptor<ChatMessageSentEvent> captor = ArgumentCaptor.forClass(ChatMessageSentEvent.class);
        then(eventPublisher).should().publishEvent(captor.capture());
        assertThat(captor.getValue().getChatRoom()).isSameAs(groupRoom);
        assertThat(captor.getValue().getMessageResponse().getType()).isEqualTo(ChatMessageType.NORMAL);
    }

    @Test
    @DisplayName("내용도 이미지도 없으면 빈 메시지 예외, 저장 안 됨")
    void execute_내용도_이미지도_없으면_예외() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = ChatRoom.create(1L, TokenScope.APP, 2L, TokenScope.APP);
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP)).willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn(null);
        given(request.getFileIds()).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> sut.execute(user, request, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ILLEGAL_ARGUMENT)
            .hasMessage("내용 또는 이미지를 첨부해야 합니다.");

        then(chatMessageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("첨부 이미지가 10개 초과이면 첨부 개수 예외, 저장 안 됨")
    void execute_첨부가_10개_초과이면_예외() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = ChatRoom.create(1L, TokenScope.APP, 2L, TokenScope.APP);
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP)).willReturn(Optional.of(directRoom));

        List<String> fileIds = java.util.stream.IntStream.range(0, 11)
            .mapToObj(i -> "f" + i)
            .toList();

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn(null);
        given(request.getFileIds()).willReturn(fileIds);

        // when & then
        assertThatThrownBy(() -> sut.execute(user, request, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ILLEGAL_ARGUMENT)
            .hasMessage("이미지는 최대 10개까지 첨부할 수 있습니다.");

        then(chatMessageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("이미지 첨부시 attachFilesUseCase 호출 및 이벤트 payload에 attachments 포함")
    void execute_이미지_첨부시_attach_호출_및_payload_포함() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        ChatRoom directRoom = ChatRoom.create(1L, TokenScope.APP, 2L, TokenScope.APP);
        given(chatRoomQueryRepository.findByIdAndParticipant(100L, 1L, TokenScope.APP)).willReturn(Optional.of(directRoom));

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);
        given(request.getContent()).willReturn(null);
        given(request.getFileIds()).willReturn(List.of("f1", "f2"));

        ChatMessage savedMessage = ChatMessage.create(100L, 1L, TokenScope.APP, ChatMessageType.NORMAL, null);
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
        sut.execute(user, request, 100L);

        // then
        then(attachFilesUseCase).should().execute(
            eq(List.of("f1", "f2")), eq(FileTargetType.CHAT_MESSAGE), anyString(), eq(1L)
        );

        ArgumentCaptor<ChatMessageSentEvent> captor = ArgumentCaptor.forClass(ChatMessageSentEvent.class);
        then(eventPublisher).should().publishEvent(captor.capture());
        assertThat(captor.getValue().getMessageResponse().getAttachments()).containsExactly(dto1, dto2);
    }
}
