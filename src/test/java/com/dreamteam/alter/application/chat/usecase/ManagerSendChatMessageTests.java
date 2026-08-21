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
import com.dreamteam.alter.domain.file.port.inbound.AttachFilesUseCase;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.application.file.FileUrlService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerSendChatMessage 테스트")
class ManagerSendChatMessageTests {

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

        given(chatRoomQueryRepository.findByIdAndParticipant(5L, 2L, TokenScope.MANAGER)).willReturn(Optional.of(groupRoom));

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

        given(chatRoomQueryRepository.findByIdAndParticipant(5L, 2L, TokenScope.MANAGER)).willReturn(Optional.empty());

        SendChatMessageRequestDto request = mock(SendChatMessageRequestDto.class);

        // when & then
        assertThatThrownBy(() -> sut.execute(managerUser, request, 5L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);

        then(chatMessageRepository).should(never()).save(any());
        then(eventPublisher).should(never()).publishEvent(any(ChatMessageSentEvent.class));
    }
}
