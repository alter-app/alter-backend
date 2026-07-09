package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class SyncWorkspaceChatMembershipTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @InjectMocks
    private SyncWorkspaceChatMembership sut;

    @Nested
    @DisplayName("join")
    class JoinTests {

        @Test
        @DisplayName("이미 활성 멤버면 중복 생성 안함")
        void join_이미_활성_멤버면_중복_생성_안함() {
            // given
            ChatRoom groupRoom = mock(ChatRoom.class);
            given(groupRoom.getId()).willReturn(5L);
            given(chatRoomQueryRepository.findGroupRoomByWorkspaceId(1L))
                .willReturn(Optional.of(groupRoom));

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
            ChatRoom groupRoom = mock(ChatRoom.class);
            given(groupRoom.getId()).willReturn(5L);
            given(chatRoomQueryRepository.findGroupRoomByWorkspaceId(1L))
                .willReturn(Optional.of(groupRoom));

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
            given(chatRoomQueryRepository.findGroupRoomByWorkspaceId(1L))
                .willReturn(Optional.empty());

            ChatRoom createdRoom = mock(ChatRoom.class);
            given(createdRoom.getId()).willReturn(7L);
            given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(createdRoom);

            given(chatRoomMemberQueryRepository.findByRoomAndMember(7L, 10L, TokenScope.APP))
                .willReturn(Optional.empty());

            // when & then
            assertThatCode(() -> sut.join(1L, 10L, TokenScope.APP)).doesNotThrowAnyException();

            then(chatRoomRepository).should().save(any(ChatRoom.class));
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
