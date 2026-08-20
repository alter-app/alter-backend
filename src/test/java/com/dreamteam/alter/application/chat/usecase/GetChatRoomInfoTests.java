package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomResponseDto;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetChatRoomInfo 테스트")
class GetChatRoomInfoTests {

    @Mock
    private ChatRoomQueryRepository chatRoomQueryRepository;

    @Mock
    private ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @Mock
    private WorkspaceQueryRepository workspaceQueryRepository;

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private FileUrlService fileUrlService;

    @InjectMocks
    private GetChatRoomInfo sut;

    @Test
    @DisplayName("GROUP 채팅방 조회 성공 시 roomName은 업장명이 되고 상대방 필드는 null이다")
    void execute_GROUP_조회성공_업장명과_null상대방필드() {
        // given
        Long chatRoomId = 1L;
        Long workspaceId = 500L;
        Long participantId = 10L;
        AppActor actor = new AppActor(participantId, null, null);

        ChatRoom groupRoom = ChatRoom.createGroup(workspaceId);
        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, participantId, TokenScope.APP))
            .willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.countActiveByRoom(chatRoomId)).willReturn(8);

        Workspace workspace = mock(Workspace.class);
        given(workspace.getBusinessName()).willReturn("알터 카페 강남점");
        given(workspaceQueryRepository.findById(workspaceId)).willReturn(Optional.of(workspace));

        // when
        ChatRoomResponseDto response = sut.execute(actor, chatRoomId);

        // then
        assertThat(response.getType().value()).isEqualTo(ChatRoomType.GROUP);
        assertThat(response.getRoomName()).isEqualTo("알터 카페 강남점");
        assertThat(response.getMemberCount()).isEqualTo(8);
        assertThat(response.getOpponentId()).isNull();
        assertThat(response.getOpponentScope()).isNull();
        assertThat(response.getOpponentName()).isNull();
        assertThat(response.getOpponentProfileImageUrl()).isNull();
    }

    @Test
    @DisplayName("DIRECT 채팅방 조회 성공 시 상대방 정보와 프로필 URL이 채워진다")
    void execute_DIRECT_조회성공_상대방정보와_프로필URL_채워짐() {
        // given
        Long chatRoomId = 2L;
        Long participantId = 10L;
        Long opponentId = 20L;
        AppActor actor = new AppActor(participantId, null, null);

        ChatRoom directRoom = ChatRoom.create(participantId, TokenScope.APP, opponentId, TokenScope.APP);
        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, participantId, TokenScope.APP))
            .willReturn(Optional.of(directRoom));
        given(chatRoomMemberQueryRepository.countActiveByRoom(chatRoomId)).willReturn(2);

        User opponentUser = mock(User.class);
        given(opponentUser.getName()).willReturn("김알바");
        given(userQueryRepository.findById(opponentId)).willReturn(Optional.of(opponentUser));
        given(fileUrlService.resolveUrlByTarget(FileTargetType.USER_PROFILE, String.valueOf(opponentId)))
            .willReturn("https://cdn.example.com/opponent.png");

        // when
        ChatRoomResponseDto response = sut.execute(actor, chatRoomId);

        // then
        assertThat(response.getType().value()).isEqualTo(ChatRoomType.DIRECT);
        assertThat(response.getRoomName()).isEqualTo("김알바");
        assertThat(response.getMemberCount()).isEqualTo(2);
        assertThat(response.getOpponentId()).isEqualTo(opponentId);
        assertThat(response.getOpponentScope().value()).isEqualTo(TokenScope.APP);
        assertThat(response.getOpponentName()).isEqualTo("김알바");
        assertThat(response.getOpponentProfileImageUrl()).isEqualTo("https://cdn.example.com/opponent.png");
    }

    @Test
    @DisplayName("DIRECT 채팅방 상대방이 조회되지 않으면 이름은 '알 수 없음', 프로필 URL은 null이고 파일 조회는 호출되지 않는다")
    void execute_DIRECT_상대방없음_이름마스킹_프로필URL_null() {
        // given
        Long chatRoomId = 2L;
        Long participantId = 10L;
        Long opponentId = 20L;
        AppActor actor = new AppActor(participantId, null, null);

        ChatRoom directRoom = ChatRoom.create(participantId, TokenScope.APP, opponentId, TokenScope.APP);
        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, participantId, TokenScope.APP))
            .willReturn(Optional.of(directRoom));
        given(chatRoomMemberQueryRepository.countActiveByRoom(chatRoomId)).willReturn(2);

        given(userQueryRepository.findById(opponentId)).willReturn(Optional.empty());

        // when
        ChatRoomResponseDto response = sut.execute(actor, chatRoomId);

        // then
        assertThat(response.getOpponentName()).isEqualTo("알 수 없음");
        assertThat(response.getOpponentProfileImageUrl()).isNull();
        verify(fileUrlService, never()).resolveUrlByTarget(any(), any());
    }

    @Test
    @DisplayName("참여자가 아니면 NOT_FOUND 예외")
    void execute_참여자_아니면_NOT_FOUND() {
        // given
        Long chatRoomId = 3L;
        Long participantId = 999L;
        AppActor actor = new AppActor(participantId, null, null);

        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, participantId, TokenScope.APP))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.execute(actor, chatRoomId))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }
}
