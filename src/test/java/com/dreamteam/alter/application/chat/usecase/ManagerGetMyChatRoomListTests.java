package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequestDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPaginatedApiResponse;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.result.ChatRoomListResult;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.user.entity.User;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerGetMyChatRoomList 테스트")
class ManagerGetMyChatRoomListTests {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatMessageQueryRepository chatMessageQueryRepository;

    private ManagerGetMyChatRoomList sut;

    @BeforeEach
    void setUp() {
        sut = new ManagerGetMyChatRoomList(
            chatRoomQueryRepository,
            chatMessageQueryRepository,
            new ObjectMapper().registerModule(new JavaTimeModule())
        );
    }

    @Test
    @DisplayName("매니저 조회 주체 id는 ManagerUser.id가 아닌 ManagerActor.getUserId()(연관 User.id)로 해석되어 목록 조회에 사용된다")
    void execute_participantId는_ManagerUser_id아닌_연관User_id로_해석된다() {
        // given
        Long managerUserRowId = 999L; // ManagerUser.id (participantId로 쓰이면 안 되는 값)
        Long userId = 10L; // ManagerActor.getUserId()가 반환해야 하는 실제 participantId

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(managerUser.getUser()).willReturn(user);
        ManagerActor actor = new ManagerActor(managerUserRowId, managerUser, null);

        Long chatRoomId = 1L;
        ChatRoomListWithOpponentResponse groupRoom = new ChatRoomListWithOpponentResponse(
            chatRoomId, ChatRoomType.GROUP, LocalDateTime.now(), LocalDateTime.now(),
            "알터 카페 강남점", null, null, null, null, null, 5L
        );

        given(chatRoomQueryRepository.countChatRoomsByParticipant(userId, TokenScope.MANAGER)).willReturn(1L);
        given(chatRoomQueryRepository.getChatRoomListWithOpponent(eq(userId), eq(TokenScope.MANAGER), any()))
            .willReturn(List.of(groupRoom));
        given(chatMessageQueryRepository.getLatestMessageContentsByChatRoomIds(List.of(chatRoomId)))
            .willReturn(Map.of());

        // when
        CursorPaginatedApiResponse<ChatRoomListResult> response =
            sut.execute(actor, CursorPageRequestDto.of(null, 10));

        // then
        ChatRoomListResult result = response.data().getFirst();
        assertThat(result.roomName()).isEqualTo("알터 카페 강남점");
        assertThat(result.memberCount()).isEqualTo(5);
    }
}
