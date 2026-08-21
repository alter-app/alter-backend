package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.application.file.FileUrlService;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.result.ChatRoomResult;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerGetChatRoom 테스트")
class ManagerGetChatRoomTests {

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
    private ManagerGetChatRoom sut;

    @Test
    @DisplayName("매니저 조회 주체 id는 ManagerUser.id가 아닌 ManagerActor.getUserId()(연관 User.id)로 해석되어 조회에 사용된다")
    void execute_participantId는_ManagerUser_id아닌_연관User_id로_해석된다() {
        // given
        Long chatRoomId = 1L;
        Long workspaceId = 500L;
        Long managerUserRowId = 999L; // ManagerUser.id (participantId로 쓰이면 안 되는 값)
        Long userId = 10L; // ManagerActor.getUserId()가 반환해야 하는 실제 participantId

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);
        ManagerUser managerUser = mock(ManagerUser.class);
        given(managerUser.getUser()).willReturn(user);
        ManagerActor actor = new ManagerActor(managerUserRowId, managerUser, null);

        ChatRoom groupRoom = ChatRoom.createGroup(workspaceId);
        given(chatRoomQueryRepository.findByIdAndParticipant(chatRoomId, userId, TokenScope.MANAGER))
            .willReturn(Optional.of(groupRoom));
        given(chatRoomMemberQueryRepository.countActiveByRoom(chatRoomId)).willReturn(3);

        Workspace workspace = mock(Workspace.class);
        given(workspace.getBusinessName()).willReturn("알터 카페 강남점");
        given(workspaceQueryRepository.findById(workspaceId)).willReturn(Optional.of(workspace));

        // when
        ChatRoomResult response = sut.execute(actor, chatRoomId);

        // then
        assertThat(response.type()).isEqualTo(ChatRoomType.GROUP);
        assertThat(response.roomName()).isEqualTo("알터 카페 강남점");
        assertThat(response.memberCount()).isEqualTo(3);
    }
}
