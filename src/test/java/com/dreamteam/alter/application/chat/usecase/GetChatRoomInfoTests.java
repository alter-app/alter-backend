package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.result.ChatRoomResult;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
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

import java.util.List;
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
    private FileQueryRepository fileQueryRepository;

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
        ChatRoomResult response = sut.execute(actor, chatRoomId);

        // then
        assertThat(response.type()).isEqualTo(ChatRoomType.GROUP);
        assertThat(response.roomName()).isEqualTo("알터 카페 강남점");
        assertThat(response.memberCount()).isEqualTo(8);
        assertThat(response.opponentId()).isNull();
        assertThat(response.opponentScope()).isNull();
        assertThat(response.opponentName()).isNull();
        assertThat(response.opponentProfileImageUrl()).isNull();
    }

    @Test
    @DisplayName("GROUP 채팅방이지만 업장이 조회되지 않으면 roomName은 '알 수 없음'이다")
    void execute_GROUP_업장없음_roomName_알수없음() {
        // given
        Long chatRoomId = 1L;
        Long workspaceId = 500L;
        Long participantId = 10L;
        AppActor actor = new AppActor(participantId, null, null);

        ChatRoom groupRoom = ChatRoom.createGroup(workspaceId);
        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, participantId, TokenScope.APP))
            .willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.countActiveByRoom(chatRoomId)).willReturn(8);
        given(workspaceQueryRepository.findById(workspaceId)).willReturn(Optional.empty());

        // when
        ChatRoomResult response = sut.execute(actor, chatRoomId);

        // then
        assertThat(response.roomName()).isEqualTo("알 수 없음");
    }

    @Test
    @DisplayName("GROUP 채팅방인데 workspaceId가 null이면 예외 없이 roomName은 '알 수 없음'이고 업장 조회는 호출되지 않는다")
    void execute_GROUP_workspaceId_null_roomName_알수없음() {
        // given
        Long chatRoomId = 1L;
        Long participantId = 10L;
        AppActor actor = new AppActor(participantId, null, null);

        ChatRoom groupRoom = mock(ChatRoom.class);
        given(groupRoom.getType()).willReturn(ChatRoomType.GROUP);
        given(groupRoom.getWorkspaceId()).willReturn(null);
        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, participantId, TokenScope.APP))
            .willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.countActiveByRoom(chatRoomId)).willReturn(8);

        // when
        ChatRoomResult response = sut.execute(actor, chatRoomId);

        // then
        assertThat(response.roomName()).isEqualTo("알 수 없음");
        verify(workspaceQueryRepository, never()).findById(any());
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

        File profileFile = mock(File.class);
        FileResponseDto profileFileResponse = FileResponseDto.of(profileFile, "https://cdn.example.com/opponent.png");
        given(fileQueryRepository.findAllByTargetTypeAndTargetIdIn(FileTargetType.USER_PROFILE, List.of(String.valueOf(opponentId))))
            .willReturn(List.of(profileFile));
        given(fileUrlService.resolve(profileFile))
            .willReturn(profileFileResponse);

        // when
        ChatRoomResult response = sut.execute(actor, chatRoomId);

        // then
        assertThat(response.type()).isEqualTo(ChatRoomType.DIRECT);
        assertThat(response.roomName()).isEqualTo("김알바");
        assertThat(response.memberCount()).isEqualTo(2);
        assertThat(response.opponentId()).isEqualTo(opponentId);
        assertThat(response.opponentScope()).isEqualTo(TokenScope.APP);
        assertThat(response.opponentName()).isEqualTo("김알바");
        assertThat(response.opponentProfileImageUrl()).isEqualTo("https://cdn.example.com/opponent.png");
    }

    @Test
    @DisplayName("상대방 프로필 파일이 ATTACHED 상태로 2건이어도 예외 없이 가장 오래된 파일의 URL을 반환한다")
    void execute_DIRECT_프로필파일_중복2건_오래된파일_URL반환() {
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

        // findAllByTargetTypeAndTargetIdIn은 createdAt 오름차순으로 정렬되어 반환되므로 첫 번째가 가장 오래된 파일이다
        File oldestFile = mock(File.class);
        File newestFile = mock(File.class);
        FileResponseDto oldestFileResponse = FileResponseDto.of(oldestFile, "https://cdn.example.com/oldest.png");
        given(fileQueryRepository.findAllByTargetTypeAndTargetIdIn(FileTargetType.USER_PROFILE, List.of(String.valueOf(opponentId))))
            .willReturn(List.of(oldestFile, newestFile));
        given(fileUrlService.resolve(oldestFile))
            .willReturn(oldestFileResponse);

        // when
        ChatRoomResult response = sut.execute(actor, chatRoomId);

        // then
        assertThat(response.opponentProfileImageUrl()).isEqualTo("https://cdn.example.com/oldest.png");
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
        ChatRoomResult response = sut.execute(actor, chatRoomId);

        // then
        assertThat(response.opponentName()).isEqualTo("알 수 없음");
        assertThat(response.opponentProfileImageUrl()).isNull();
        verify(fileQueryRepository, never()).findAllByTargetTypeAndTargetIdIn(any(), any());
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
