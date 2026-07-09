package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.port.inbound.SyncWorkspaceChatMembershipUseCase;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.SubstituteRequest;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.SubstituteRequestQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkerResignationServiceImpl 테스트")
class WorkerResignationServiceImplTest {

    @Mock
    private WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Mock
    private WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;

    @Mock
    private SubstituteRequestQueryRepository substituteRequestQueryRepository;

    @Mock
    private SyncWorkspaceChatMembershipUseCase syncWorkspaceChatMembership;

    @InjectMocks
    private WorkerResignationServiceImpl workerResignationService;

    @Test
    @DisplayName("퇴직 정리 대상을 일괄 정리하고 worker를 퇴직 처리한다")
    void resign_정리대상존재_일괄정리() {
        // given
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        when(worker.getId()).thenReturn(10L);
        Workspace workspace = mock(Workspace.class);
        when(workspace.getId()).thenReturn(100L);
        User user = mock(User.class);
        when(user.getId()).thenReturn(200L);
        when(worker.getWorkspace()).thenReturn(workspace);
        when(worker.getUser()).thenReturn(user);

        WorkspaceShift futureShift = mock(WorkspaceShift.class);
        when(workspaceShiftQueryRepository.findFutureShiftsByAssignedWorker(any(), any(LocalDateTime.class)))
            .thenReturn(List.of(futureShift));

        WorkspaceWorkerSchedule fixedSchedule = mock(WorkspaceWorkerSchedule.class);
        when(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(worker))
            .thenReturn(List.of(fixedSchedule));

        SubstituteRequest requesterRequest = mock(SubstituteRequest.class);
        when(substituteRequestQueryRepository.findAllActiveByRequesterWorkerId(10L))
            .thenReturn(List.of(requesterRequest));

        SubstituteRequest targetRequest = mock(SubstituteRequest.class);
        when(substituteRequestQueryRepository.findAllPendingTargetRequestsByTargetWorkerId(10L))
            .thenReturn(List.of(targetRequest));

        // when
        workerResignationService.resign(worker);

        // then
        verify(futureShift, times(1)).unassignWorker();
        verify(fixedSchedule, times(1)).delete();
        verify(requesterRequest, times(1)).cancel();
        verify(targetRequest, times(1)).cancelPendingTargetAndCancelIfNoPendingTargets(10L);
        verify(worker, times(1)).resign();
        verify(syncWorkspaceChatMembership, times(1)).leave(100L, 200L, TokenScope.APP);
    }

    @Test
    @DisplayName("정리 대상이 없어도 worker를 퇴직 처리한다")
    void resign_정리대상없음_퇴직처리() {
        // given
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        when(worker.getId()).thenReturn(20L);
        Workspace workspace = mock(Workspace.class);
        when(workspace.getId()).thenReturn(300L);
        User user = mock(User.class);
        when(user.getId()).thenReturn(400L);
        when(worker.getWorkspace()).thenReturn(workspace);
        when(worker.getUser()).thenReturn(user);
        when(workspaceShiftQueryRepository.findFutureShiftsByAssignedWorker(any(), any(LocalDateTime.class)))
            .thenReturn(List.of());
        when(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(worker)).thenReturn(List.of());
        when(substituteRequestQueryRepository.findAllActiveByRequesterWorkerId(20L)).thenReturn(List.of());
        when(substituteRequestQueryRepository.findAllPendingTargetRequestsByTargetWorkerId(20L)).thenReturn(List.of());

        // when
        workerResignationService.resign(worker);

        // then
        verify(worker, times(1)).resign();
        verify(syncWorkspaceChatMembership, times(1)).leave(300L, 400L, TokenScope.APP);
    }
}
