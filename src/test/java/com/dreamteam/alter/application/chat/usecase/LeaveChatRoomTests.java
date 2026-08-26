package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveChatRoom 테스트")
class LeaveChatRoomTests {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    private LeaveChatRoom sut;

    @Test
    @DisplayName("DIRECT 방은 나갈 수 있다")
    void execute_DIRECT방_성공() {
        // given
        ChatRoom room = directRoom();
        ChatRoomMember member = ChatRoomMember.create(1L, 10L, TokenScope.APP);
        given(chatRoomQueryRepository.findById(1L)).willReturn(Optional.of(room));
        given(chatRoomMemberQueryRepository.findByRoomAndMember(1L, 10L, TokenScope.APP))
            .willReturn(Optional.of(member));

        // when
        sut.execute(10L, TokenScope.APP, 1L);

        // then
        assertThat(member.getLeftAt()).isNotNull();
        then(chatRoomMemberRepository).should().save(member);
    }

    @Test
    @DisplayName("GROUP 방의 활성 멤버는 나갈 수 없다")
    void execute_GROUP방_활성멤버는_불가() {
        // given
        ChatRoom room = groupRoom();
        ChatRoomMember member = ChatRoomMember.create(1L, 10L, TokenScope.APP);
        given(chatRoomQueryRepository.findById(1L)).willReturn(Optional.of(room));
        given(chatRoomMemberQueryRepository.findByRoomAndMember(1L, 10L, TokenScope.APP))
            .willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> sut.execute(10L, TokenScope.APP, 1L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ILLEGAL_ARGUMENT)
            .hasMessageContaining("업장 단톡방은 나갈 수 없습니다.");
        then(chatRoomMemberRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("GROUP 방의 비멤버는 NOT_FOUND (방 종류를 노출하지 않는다)")
    void execute_GROUP방_비멤버는_NOT_FOUND() {
        // given
        ChatRoom room = groupRoom();
        given(chatRoomQueryRepository.findById(1L)).willReturn(Optional.of(room));
        given(chatRoomMemberQueryRepository.findByRoomAndMember(1L, 10L, TokenScope.APP))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.execute(10L, TokenScope.APP, 1L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("채팅방 멤버가 아닙니다.");
        then(chatRoomMemberRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("방이 없으면 NOT_FOUND 예외")
    void execute_방없으면_NOT_FOUND() {
        // given
        given(chatRoomQueryRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.execute(10L, TokenScope.APP, 1L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("채팅방 멤버가 아니면 NOT_FOUND 예외")
    void execute_멤버_아니면_NOT_FOUND() {
        // given
        ChatRoom room = directRoom();
        given(chatRoomQueryRepository.findById(1L)).willReturn(Optional.of(room));
        given(chatRoomMemberQueryRepository.findByRoomAndMember(1L, 10L, TokenScope.APP))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.execute(10L, TokenScope.APP, 1L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
            .hasMessageContaining("채팅방 멤버가 아닙니다.");
    }

    @Test
    @DisplayName("MANAGER 스코프도 DIRECT 방을 나갈 수 있다")
    void execute_MANAGER스코프_DIRECT방_성공() {
        // given
        ChatRoom room = directRoom();
        ChatRoomMember member = ChatRoomMember.create(1L, 30L, TokenScope.MANAGER);
        given(chatRoomQueryRepository.findById(1L)).willReturn(Optional.of(room));
        given(chatRoomMemberQueryRepository.findByRoomAndMember(1L, 30L, TokenScope.MANAGER))
            .willReturn(Optional.of(member));

        // when
        sut.execute(30L, TokenScope.MANAGER, 1L);

        // then
        assertThat(member.getLeftAt()).isNotNull();
        then(chatRoomMemberRepository).should().save(member);
    }

    @Test
    @DisplayName("이미 나간 멤버면 예외 없이 no-op")
    void execute_이미나간멤버면_noop() {
        // given
        ChatRoom room = directRoom();
        ChatRoomMember member = ChatRoomMember.create(1L, 10L, TokenScope.APP);
        member.leave();
        given(chatRoomQueryRepository.findById(1L)).willReturn(Optional.of(room));
        given(chatRoomMemberQueryRepository.findByRoomAndMember(1L, 10L, TokenScope.APP))
            .willReturn(Optional.of(member));

        // when
        sut.execute(10L, TokenScope.APP, 1L);

        // then
        then(chatRoomMemberRepository).should(never()).save(any());
    }

    private ChatRoom directRoom() {
        return ChatRoom.create(10L, TokenScope.APP, 20L, TokenScope.APP);
    }

    private ChatRoom groupRoom() {
        return ChatRoom.createGroup(99L);
    }
}
