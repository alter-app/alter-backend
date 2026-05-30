package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
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
@DisplayName("ManagerResignWorkspaceWorker 테스트")
class ManagerResignWorkspaceWorkerTest {

    @Mock
    private WorkspaceQueryRepository workspaceQueryRepository;

    @Mock
    private WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;

    @Mock
    private WorkerResignationService workerResignationService;

    @InjectMocks
    private ManagerResignWorkspaceWorker managerResignWorkspaceWorker;

    @Test
    @DisplayName("관리 업장이 아니면 WORKSPACE_NOT_FOUND 예외가 발생한다")
    void execute_관리업장아님_예외발생() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        when(actor.getManagerUser()).thenReturn(managerUser);
        when(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> managerResignWorkspaceWorker.execute(actor, 1L, 10L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND));
        verify(workerResignationService, never()).resign(any());
    }

    @Test
    @DisplayName("근무자를 찾을 수 없으면 NOT_FOUND 예외가 발생한다")
    void execute_근무자없음_예외발생() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        when(actor.getManagerUser()).thenReturn(managerUser);
        when(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).thenReturn(true);
        when(workspaceWorkerQueryRepository.findById(10L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> managerResignWorkspaceWorker.execute(actor, 1L, 10L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
        verify(workerResignationService, never()).resign(any());
    }

    @Test
    @DisplayName("근무자가 다른 업장 소속이면 WORKSPACE_NOT_FOUND 예외가 발생한다")
    void execute_다른업장근무자_예외발생() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        when(actor.getManagerUser()).thenReturn(managerUser);
        when(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).thenReturn(true);

        Workspace workspace = mock(Workspace.class);
        when(workspace.getId()).thenReturn(2L);
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        when(worker.getWorkspace()).thenReturn(workspace);
        when(workspaceWorkerQueryRepository.findById(10L)).thenReturn(Optional.of(worker));

        // when & then
        assertThatThrownBy(() -> managerResignWorkspaceWorker.execute(actor, 1L, 10L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND));
        verify(workerResignationService, never()).resign(worker);
    }

    @Test
    @DisplayName("정상 요청이면 공통 퇴직 서비스에 위임한다")
    void execute_정상요청_공통서비스위임() {
        // given
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser managerUser = mock(ManagerUser.class);
        when(actor.getManagerUser()).thenReturn(managerUser);
        when(workspaceQueryRepository.existsByIdAndManagerUser(1L, managerUser)).thenReturn(true);

        Workspace workspace = mock(Workspace.class);
        when(workspace.getId()).thenReturn(1L);
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        when(worker.getWorkspace()).thenReturn(workspace);
        when(workspaceWorkerQueryRepository.findById(10L)).thenReturn(Optional.of(worker));

        // when
        managerResignWorkspaceWorker.execute(actor, 1L, 10L);

        // then
        verify(workerResignationService).resign(worker);
    }
}
