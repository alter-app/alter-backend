package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWorkspaceGroupChatRoom 테스트")
class GetWorkspaceGroupChatRoomTests {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @InjectMocks
    private GetWorkspaceGroupChatRoom sut;

    @Test
    @DisplayName("업장 단톡방 멤버면 채팅방 ID를 반환한다")
    void execute_멤버면_roomId_반환() {
        // given
        ChatRoom room = mock(ChatRoom.class);
        given(room.getId()).willReturn(1L);
        given(chatRoomQueryRepository.findGroupRoomByWorkspaceId(100L)).willReturn(Optional.of(room));
        given(chatRoomMemberQueryRepository.existsActive(1L, 10L, TokenScope.APP)).willReturn(true);

        // when
        Long roomId = sut.execute(10L, TokenScope.APP, 100L);

        // then
        assertThat(roomId).isEqualTo(1L);
    }

    @Test
    @DisplayName("업장 단톡방 멤버가 아니면 NOT_FOUND 예외")
    void execute_멤버_아니면_NOT_FOUND() {
        // given
        ChatRoom room = mock(ChatRoom.class);
        given(room.getId()).willReturn(1L);
        given(chatRoomQueryRepository.findGroupRoomByWorkspaceId(100L)).willReturn(Optional.of(room));
        given(chatRoomMemberQueryRepository.existsActive(1L, 10L, TokenScope.APP)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.execute(10L, TokenScope.APP, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("업장 단톡방이 없으면 NOT_FOUND 예외")
    void execute_단톡방_없으면_NOT_FOUND() {
        // given
        given(chatRoomQueryRepository.findGroupRoomByWorkspaceId(100L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.execute(10L, TokenScope.APP, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }
}
