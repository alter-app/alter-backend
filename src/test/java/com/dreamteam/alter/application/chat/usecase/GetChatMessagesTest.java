package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatMessageResponseDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetChatMessages 테스트")
class GetChatMessagesTest {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatMessageQueryRepository chatMessageQueryRepository;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    private GetChatMessages sut;

    @BeforeEach
    void setUp() {
        sut = new GetChatMessages(
            chatRoomQueryRepository,
            chatMessageQueryRepository,
            new ObjectMapper().registerModule(new JavaTimeModule()),
            chatRoomMemberQueryRepository
        );
    }

    @Test
    @DisplayName("메시지별 안 읽은 사람 수를 계산한다 (읽은 사람/발신자 본인 제외)")
    void execute_메시지별_unreadCount_계산() {
        // given
        Long chatRoomId = 1L;
        Long senderId = 100L;
        AppActor actor = new AppActor(senderId, null, null);

        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, senderId, TokenScope.APP))
            .willReturn(Optional.of(ChatRoom.createGroup(1L)));

        ChatMessageResponse message = new ChatMessageResponse(
            8L, chatRoomId, senderId, TokenScope.APP, "hello", LocalDateTime.now()
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
        CursorPaginatedApiResponse<ChatMessageResponseDto> response =
            sut.execute(actor, chatRoomId, CursorPageRequestDto.of(null, 10));

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().getUnreadCount()).isEqualTo(2);
    }
}
