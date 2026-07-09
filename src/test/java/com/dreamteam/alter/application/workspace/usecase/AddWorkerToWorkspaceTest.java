package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.SyncWorkspaceChatMembershipUseCase;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddWorkerToWorkspace 테스트")
class AddWorkerToWorkspaceTest {

    @Mock
    private WorkspaceWorkerRepository workspaceWorkerRepository;

    @Mock
    private WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    @Mock
    private SyncWorkspaceChatMembershipUseCase syncWorkspaceChatMembership;

    @InjectMocks
    private AddWorkerToWorkspace addWorkerToWorkspace;

    @Test
    @DisplayName("신규 근무자 추가 시 업장 단톡에 join 한다")
    void execute_신규추가_단톡join호출() {
        // given
        Workspace workspace = mock(Workspace.class);
        when(workspace.getId()).thenReturn(1L);
        User user = mock(User.class);
        when(user.getId()).thenReturn(2L);

        when(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, user))
            .thenReturn(Optional.empty());

        // when
        addWorkerToWorkspace.execute(workspace, user);

        // then
        then(syncWorkspaceChatMembership).should().join(1L, 2L, TokenScope.APP);
    }

    @Test
    @DisplayName("이미 근무중인 근무자면 예외를 던지고 단톡 join을 호출하지 않는다")
    void execute_이미근무중_단톡join미호출() {
        // given
        Workspace workspace = mock(Workspace.class);
        User user = mock(User.class);
        WorkspaceWorker existingWorker = mock(WorkspaceWorker.class);

        when(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, user))
            .thenReturn(Optional.of(existingWorker));

        // when & then
        assertThatThrownBy(() -> addWorkerToWorkspace.execute(workspace, user))
            .isInstanceOf(CustomException.class);

        then(syncWorkspaceChatMembership).should(never()).join(any(), any(), any());
    }
}
