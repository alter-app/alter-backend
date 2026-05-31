package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.inbound.WorkerResignationService;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserResignWorkspaceWorker 테스트")
class UserResignWorkspaceWorkerTest {

    @Mock
    private WorkspaceQueryRepository workspaceQueryRepository;

    @Mock
    private WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    @Mock
    private WorkerResignationService workerResignationService;

    @InjectMocks
    private UserResignWorkspaceWorker userResignWorkspaceWorker;

    @Test
    @DisplayName("업장이 없으면 WORKSPACE_NOT_FOUND 예외가 발생한다")
    void execute_업장없음_예외발생() {
        // given
        AppActor actor = mock(AppActor.class);
        when(workspaceQueryRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userResignWorkspaceWorker.execute(actor, 1L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND));
        verify(workerResignationService, never()).resign(any());
    }

    @Test
    @DisplayName("활성 근무자가 아니면 FORBIDDEN 예외가 발생한다")
    void execute_활성근무자아님_예외발생() {
        // given
        AppActor actor = mock(AppActor.class);
        User user = mock(User.class);
        Workspace workspace = mock(Workspace.class);
        when(actor.getUser()).thenReturn(user);
        when(workspaceQueryRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, user))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userResignWorkspaceWorker.execute(actor, 1L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
        verify(workerResignationService, never()).resign(any());
    }

    @Test
    @DisplayName("정상 요청이면 공통 퇴직 서비스에 위임한다")
    void execute_정상요청_공통서비스위임() {
        // given
        AppActor actor = mock(AppActor.class);
        User user = mock(User.class);
        Workspace workspace = mock(Workspace.class);
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        when(actor.getUser()).thenReturn(user);
        when(workspaceQueryRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, user))
            .thenReturn(Optional.of(worker));

        // when
        userResignWorkspaceWorker.execute(actor, 1L);

        // then
        verify(workerResignationService).resign(worker);
    }
}
