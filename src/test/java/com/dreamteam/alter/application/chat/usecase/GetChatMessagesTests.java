package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.result.ChatMessageResult;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetChatMessages 테스트")
class GetChatMessagesTests {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatMessageQueryRepository chatMessageQueryRepository;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @Mock
    private FileQueryRepository fileQueryRepository;

    @Mock
    private FileUrlService fileUrlService;

    private GetChatMessages sut;

    @BeforeEach
    void setUp() {
        sut = new GetChatMessages(
            chatRoomQueryRepository,
            chatMessageQueryRepository,
            new ObjectMapper().registerModule(new JavaTimeModule()),
            chatRoomMemberQueryRepository,
            fileQueryRepository,
            fileUrlService
        );
    }

    @Test
    @DisplayName("메시지별 안 읽은 사람 수를 계산한다 (읽은 사람/발신자 본인 제외)")
    void execute_메시지별_unreadCount_계산() {
        // given
        Long chatRoomId = 1L;
        Long senderId = 100L;
        AppActor actor = new AppActor(senderId, null, null);

        ChatRoom directRoom = ChatRoom.create(senderId, TokenScope.APP, 999L, TokenScope.APP);
        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, senderId, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        ChatMessageResponse message = new ChatMessageResponse(
            8L, chatRoomId, senderId, TokenScope.APP, ChatMessageType.NORMAL, "hello", LocalDateTime.now()
        );
        given(chatMessageQueryRepository.getChatMessagesWithCursor(any(), any()))
            .willReturn(List.of(message));

        ChatRoomMember memberA = ChatRoomMember.create(chatRoomId, 200L, TokenScope.APP); // 발신자 아님
        memberA.updateLastRead(10L); // 읽음 (10 >= 8)
        ChatRoomMember memberB = ChatRoomMember.create(chatRoomId, 300L, TokenScope.APP);
        memberB.updateLastRead(5L); // 안 읽음 (5 < 8)
        ChatRoomMember memberC = ChatRoomMember.create(chatRoomId, 400L, TokenScope.APP);
        // memberC.lastReadMessageId == null → 안 읽음
        ChatRoomMember sender = ChatRoomMember.create(chatRoomId, senderId, TokenScope.APP); // 발신자 본인 → 제외

        given(chatRoomMemberQueryRepository.findActiveByRoom(chatRoomId))
            .willReturn(List.of(memberA, memberB, memberC, sender));

        // when
        CursorPaginatedApiResponse<ChatMessageResult> response =
            sut.execute(actor, chatRoomId, CursorPageRequestDto.of(null, 10));

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().unreadCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("메시지 목록 조회 시 첨부 파일을 일괄 조회하여 각 메시지에 매핑한다 (N+1 방지)")
    void execute_첨부파일_일괄조회_매핑() {
        // given
        Long chatRoomId = 1L;
        Long senderId = 100L;
        AppActor actor = new AppActor(senderId, null, null);

        ChatRoom directRoom = ChatRoom.create(senderId, TokenScope.APP, 999L, TokenScope.APP);
        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, senderId, TokenScope.APP))
            .willReturn(Optional.of(directRoom));

        ChatMessageResponse messageWithFile = new ChatMessageResponse(
            1L, chatRoomId, senderId, TokenScope.APP, ChatMessageType.NORMAL, "첨부 있음", LocalDateTime.now()
        );
        ChatMessageResponse messageWithoutFile = new ChatMessageResponse(
            2L, chatRoomId, senderId, TokenScope.APP, ChatMessageType.NORMAL, "첨부 없음", LocalDateTime.now()
        );
        given(chatMessageQueryRepository.getChatMessagesWithCursor(any(), any()))
            .willReturn(List.of(messageWithoutFile, messageWithFile));

        given(chatRoomMemberQueryRepository.findActiveByRoom(chatRoomId))
            .willReturn(Collections.emptyList());

        File file = File.create(
            FileTargetType.CHAT_MESSAGE,
            "photo.png",
            "stored/key.png",
            "https://cdn.example.com/photo.png",
            "image/png",
            1024L,
            com.dreamteam.alter.domain.file.type.BucketType.PUBLIC,
            senderId
        );
        file.attach(String.valueOf(messageWithFile.getId()));

        given(fileQueryRepository.findAllByTargetTypeAndTargetIdIn(
            eq(FileTargetType.CHAT_MESSAGE),
            any()
        )).willReturn(List.of(file));

        FileResponseDto fileResponseDto = FileResponseDto.of(file, "https://cdn.example.com/photo.png");
        given(fileUrlService.resolve(file)).willReturn(fileResponseDto);

        // when
        CursorPaginatedApiResponse<ChatMessageResult> response =
            sut.execute(actor, chatRoomId, CursorPageRequestDto.of(null, 10));

        // then
        assertThat(response.data()).hasSize(2);

        ChatMessageResult resultWithFile = response.data().stream()
            .filter(result -> result.id().equals(messageWithFile.getId()))
            .findFirst()
            .orElseThrow();
        ChatMessageResult resultWithoutFile = response.data().stream()
            .filter(result -> result.id().equals(messageWithoutFile.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(resultWithFile.attachments()).hasSize(1);
        assertThat(resultWithFile.attachments().getFirst().url()).isEqualTo("https://cdn.example.com/photo.png");
        assertThat(resultWithoutFile.attachments()).isEmpty();

        verify(fileQueryRepository, times(1))
            .findAllByTargetTypeAndTargetIdIn(eq(FileTargetType.CHAT_MESSAGE), any());
    }

    @Test
    @DisplayName("GROUP 채팅방의 활성 멤버는 메시지 목록을 조회할 수 있다")
    void execute_GROUP_활성멤버는_메시지목록_조회_성공() {
        // given
        Long chatRoomId = 2L;
        Long participantId = 500L;
        AppActor actor = new AppActor(participantId, null, null);

        ChatRoom groupRoom = mock(ChatRoom.class);
        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, participantId, TokenScope.APP))
            .willReturn(Optional.of(groupRoom));

        ChatMessageResponse message = new ChatMessageResponse(
            10L, chatRoomId, participantId, TokenScope.APP, ChatMessageType.NORMAL, "안녕하세요", LocalDateTime.now()
        );
        given(chatMessageQueryRepository.getChatMessagesWithCursor(any(), any()))
            .willReturn(List.of(message));
        given(chatRoomMemberQueryRepository.findActiveByRoom(chatRoomId))
            .willReturn(Collections.emptyList());

        // when
        CursorPaginatedApiResponse<ChatMessageResult> response =
            sut.execute(actor, chatRoomId, CursorPageRequestDto.of(null, 10));

        // then
        assertThat(response.data()).hasSize(1);
        verify(chatRoomQueryRepository).findByIdAndParticipant(chatRoomId, participantId, TokenScope.APP);
    }

    @Test
    @DisplayName("GROUP 채팅방의 비멤버는 메시지 목록을 조회할 수 없다 (NOT_FOUND)")
    void execute_GROUP_비멤버는_NOT_FOUND() {
        // given
        Long chatRoomId = 3L;
        Long participantId = 999L;
        AppActor actor = new AppActor(participantId, null, null);

        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, participantId, TokenScope.APP))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.execute(actor, chatRoomId, CursorPageRequestDto.of(null, 10)))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);

        verify(chatMessageQueryRepository, never()).getChatMessagesWithCursor(any(), any());
    }
}
