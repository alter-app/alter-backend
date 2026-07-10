package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
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
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkChatRoomRead 테스트")
class MarkChatRoomReadTest {

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    private MarkChatRoomRead sut;

    @Test
    @DisplayName("lastReadMessageId를 갱신한다")
    void execute_lastReadMessageId_갱신() {
        // given
        ChatRoomMember member = ChatRoomMember.create(1L, 10L, TokenScope.APP);
        given(chatRoomMemberQueryRepository.findByRoomAndMember(1L, 10L, TokenScope.APP))
            .willReturn(Optional.of(member));

        // when
        sut.execute(10L, TokenScope.APP, 1L, 100L);

        // then
        assertThat(member.getLastReadMessageId()).isEqualTo(100L);
        then(chatRoomMemberRepository).should().save(member);
    }

    @Test
    @DisplayName("과거 id로는 되돌아가지 않는다 (멱등)")
    void execute_과거_id로는_되돌아가지_않음() {
        // given
        ChatRoomMember member = ChatRoomMember.create(1L, 10L, TokenScope.APP);
        member.updateLastRead(100L);
        given(chatRoomMemberQueryRepository.findByRoomAndMember(1L, 10L, TokenScope.APP))
            .willReturn(Optional.of(member));

        // when
        sut.execute(10L, TokenScope.APP, 1L, 50L);

        // then
        assertThat(member.getLastReadMessageId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("채팅방 멤버가 아니면 NOT_FOUND 예외")
    void execute_멤버_아니면_NOT_FOUND() {
        // given
        given(chatRoomMemberQueryRepository.findByRoomAndMember(1L, 10L, TokenScope.APP))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.execute(10L, TokenScope.APP, 1L, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("비활성(퇴장) 멤버면 NOT_FOUND 예외")
    void execute_비활성_멤버면_NOT_FOUND() {
        // given
        ChatRoomMember left = ChatRoomMember.create(1L, 10L, TokenScope.APP);
        left.leave();
        given(chatRoomMemberQueryRepository.findByRoomAndMember(1L, 10L, TokenScope.APP))
            .willReturn(Optional.of(left));

        // when & then
        assertThatThrownBy(() -> sut.execute(10L, TokenScope.APP, 1L, 100L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }
}
