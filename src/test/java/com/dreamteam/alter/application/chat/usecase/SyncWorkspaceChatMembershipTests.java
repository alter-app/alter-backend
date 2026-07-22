package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.application.chat.support.GroupChatRoomProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncWorkspaceChatMembership 테스트")
class SyncWorkspaceChatMembershipTests {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @Mock
    private GroupChatRoomProvider groupChatRoomProvider;

    @InjectMocks
    private SyncWorkspaceChatMembership sut;

    @Nested
    @DisplayName("join")
    class JoinTests {

        @Test
        @DisplayName("이미 활성 멤버면 중복 생성 안함")
        void join_이미_활성_멤버면_중복_생성_안함() {
            // given
            given(groupChatRoomProvider.getOrCreate(1L)).willReturn(5L);

            ChatRoomMember activeMember = ChatRoomMember.create(5L, 10L, TokenScope.APP);
            given(chatRoomMemberQueryRepository.findByRoomAndMember(5L, 10L, TokenScope.APP))
                .willReturn(Optional.of(activeMember));

            // when
            sut.join(1L, 10L, TokenScope.APP);

            // then
            then(chatRoomMemberRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("퇴장했던 멤버면 rejoin")
        void join_퇴장했던_멤버면_rejoin() {
            // given
            given(groupChatRoomProvider.getOrCreate(1L)).willReturn(5L);

            ChatRoomMember left = ChatRoomMember.create(5L, 10L, TokenScope.APP);
            left.leave();
            given(chatRoomMemberQueryRepository.findByRoomAndMember(5L, 10L, TokenScope.APP))
                .willReturn(Optional.of(left));

            // when
            sut.join(1L, 10L, TokenScope.APP);

            // then
            assertThat(left.isActive()).isTrue();
            then(chatRoomMemberRepository).should().save(left);
        }

        @Test
        @DisplayName("업장 단톡방이 없으면(레거시 업장) 단톡방을 새로 생성하고 멤버로 추가하며 예외를 던지지 않는다")
        void join_단톡방없으면_단톡방생성후_멤버추가_예외없음() {
            // given
            given(groupChatRoomProvider.getOrCreate(1L)).willReturn(7L);
            given(chatRoomMemberQueryRepository.findByRoomAndMember(7L, 10L, TokenScope.APP))
                .willReturn(Optional.empty());

            // when & then
            assertThatCode(() -> sut.join(1L, 10L, TokenScope.APP)).doesNotThrowAnyException();

            then(chatRoomMemberRepository).should().save(any(ChatRoomMember.class));
        }

        @Test
        @DisplayName("동시 생성 경쟁으로 방 생성이 유니크 위반이면 승자의 방을 재조회해 멤버를 추가한다")
        void join_방생성_경쟁시_재조회후_멤버추가() {
            // given: REQUIRES_NEW 방 생성이 유니크 위반으로 실패
            given(groupChatRoomProvider.getOrCreate(1L))
                .willThrow(new DataIntegrityViolationException("duplicate group room"));
            ChatRoom winnerRoom = mock(ChatRoom.class);
            given(winnerRoom.getId()).willReturn(9L);
            given(chatRoomQueryRepository.findGroupRoomByWorkspaceId(1L))
                .willReturn(Optional.of(winnerRoom));
            given(chatRoomMemberQueryRepository.findByRoomAndMember(9L, 10L, TokenScope.APP))
                .willReturn(Optional.empty());

            // when & then: 승자 방(9)을 재조회해 멤버 추가, 예외 없음
            assertThatCode(() -> sut.join(1L, 10L, TokenScope.APP)).doesNotThrowAnyException();

            then(chatRoomMemberRepository).should().save(any(ChatRoomMember.class));
        }
    }

    @Nested
    @DisplayName("leave")
    class LeaveTests {

        @Test
        @DisplayName("업장 단톡방이 없으면 아무 작업도 하지 않고 예외를 던지지 않는다")
        void leave_단톡방없으면_noop_예외없음() {
            // given
            given(chatRoomQueryRepository.findGroupRoomByWorkspaceId(1L))
                .willReturn(Optional.empty());

            // when & then
            assertThatCode(() -> sut.leave(1L, 10L, TokenScope.APP)).doesNotThrowAnyException();

            then(chatRoomMemberQueryRepository).should(never()).findByRoomAndMember(any(), any(), any());
            then(chatRoomMemberRepository).should(never()).save(any());
        }
    }
}
