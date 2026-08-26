package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.chat.result.CreateChatRoomResult;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrGetChatRoom 테스트")
class CreateOrGetChatRoomTests {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @InjectMocks
    private CreateOrGetChatRoom sut;

    @Test
    @DisplayName("기존 채팅방이 없으면 새로 생성하고 참여자 2명의 chat_room_members 행을 생성한다")
    void execute_신규방생성_멤버행2개생성() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        AppActor actor = AppActor.from(user, List.of());

        given(chatRoomQueryRepository.findExistingChatRoom(1L, TokenScope.APP, 2L, TokenScope.APP))
            .willReturn(Optional.empty());

        ChatRoom savedRoom = mock(ChatRoom.class);
        given(savedRoom.getId()).willReturn(99L);
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(savedRoom);

        // when
        CreateChatRoomResult response = sut.execute(actor, 2L, TokenScope.APP);

        // then
        assertThat(response.chatRoomId()).isEqualTo(99L);

        ArgumentCaptor<List<ChatRoomMember>> captor = ArgumentCaptor.forClass(List.class);
        then(chatRoomMemberRepository).should().saveAll(captor.capture());
        List<ChatRoomMember> savedMembers = captor.getValue();

        assertThat(savedMembers).hasSize(2);
        assertThat(savedMembers)
            .extracting(ChatRoomMember::getChatRoomId, ChatRoomMember::getMemberId, ChatRoomMember::getMemberScope)
            .containsExactlyInAnyOrder(
                org.assertj.core.api.Assertions.tuple(99L, 1L, TokenScope.APP),
                org.assertj.core.api.Assertions.tuple(99L, 2L, TokenScope.APP)
            );
    }

    @Test
    @DisplayName("기존 채팅방이 있으면 재사용하고 chat_room_members 행을 생성하지 않는다")
    void execute_기존방재사용_멤버행미생성() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        AppActor actor = AppActor.from(user, List.of());

        ChatRoom existingRoom = mock(ChatRoom.class);
        given(existingRoom.getId()).willReturn(42L);
        given(chatRoomQueryRepository.findExistingChatRoom(1L, TokenScope.APP, 2L, TokenScope.APP))
            .willReturn(Optional.of(existingRoom));

        // when
        CreateChatRoomResult response = sut.execute(actor, 2L, TokenScope.APP);

        // then
        assertThat(response.chatRoomId()).isEqualTo(42L);
        then(chatRoomRepository).should(never()).save(any());
        then(chatRoomMemberRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("나간 뒤 재호출하면 같은 방으로 rejoin하고 새 방을 생성하지 않는다")
    void execute_나간뒤재호출_rejoin() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        AppActor actor = AppActor.from(user, List.of());

        ChatRoom existingRoom = mock(ChatRoom.class);
        given(existingRoom.getId()).willReturn(42L);
        given(chatRoomQueryRepository.findExistingChatRoom(1L, TokenScope.APP, 2L, TokenScope.APP))
            .willReturn(Optional.of(existingRoom));

        ChatRoomMember leftMember = mock(ChatRoomMember.class);
        given(leftMember.isActive()).willReturn(false);
        given(chatRoomMemberQueryRepository.findByRoomAndMember(42L, 1L, TokenScope.APP))
            .willReturn(Optional.of(leftMember));

        // when
        CreateChatRoomResult response = sut.execute(actor, 2L, TokenScope.APP);

        // then
        assertThat(response.chatRoomId()).isEqualTo(42L);
        then(leftMember).should().rejoin();
        then(chatRoomMemberRepository).should().save(leftMember);
        then(chatRoomRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("활성 멤버 상태에서 재호출하면 save를 호출하지 않는다")
    void execute_활성멤버재호출_save미호출() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        AppActor actor = AppActor.from(user, List.of());

        ChatRoom existingRoom = mock(ChatRoom.class);
        given(existingRoom.getId()).willReturn(42L);
        given(chatRoomQueryRepository.findExistingChatRoom(1L, TokenScope.APP, 2L, TokenScope.APP))
            .willReturn(Optional.of(existingRoom));

        ChatRoomMember activeMember = mock(ChatRoomMember.class);
        given(activeMember.isActive()).willReturn(true);
        given(chatRoomMemberQueryRepository.findByRoomAndMember(42L, 1L, TokenScope.APP))
            .willReturn(Optional.of(activeMember));

        // when
        CreateChatRoomResult response = sut.execute(actor, 2L, TokenScope.APP);

        // then
        assertThat(response.chatRoomId()).isEqualTo(42L);
        then(activeMember).should(never()).rejoin();
        then(chatRoomMemberRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("기존 방 재사용 시 상대방 멤버십은 조회하지 않는다")
    void execute_기존방재사용_상대방멤버십미조회() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        AppActor actor = AppActor.from(user, List.of());

        ChatRoom existingRoom = mock(ChatRoom.class);
        given(existingRoom.getId()).willReturn(42L);
        given(chatRoomQueryRepository.findExistingChatRoom(1L, TokenScope.APP, 2L, TokenScope.APP))
            .willReturn(Optional.of(existingRoom));

        ChatRoomMember activeMember = mock(ChatRoomMember.class);
        given(activeMember.isActive()).willReturn(true);
        given(chatRoomMemberQueryRepository.findByRoomAndMember(42L, 1L, TokenScope.APP))
            .willReturn(Optional.of(activeMember));

        // when
        sut.execute(actor, 2L, TokenScope.APP);

        // then
        then(chatRoomMemberQueryRepository).should(never()).findByRoomAndMember(42L, 2L, TokenScope.APP);
    }
}
