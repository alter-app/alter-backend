package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.application.chat.event.ChatMembershipLeftEvent;
import com.dreamteam.alter.domain.auth.type.TokenScope;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WorkerResignationServiceImpl workerResignationService;

    @Captor
    private ArgumentCaptor<ChatMembershipLeftEvent> eventCaptor;

    @Test
    @DisplayName("퇴직 정리 대상을 일괄 정리하고 worker를 퇴직 처리한다")
    void resign_정리대상존재_일괄정리() {
        // given
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        given(worker.getId()).willReturn(10L);
        Workspace workspace = mock(Workspace.class);
        given(workspace.getId()).willReturn(100L);
        User user = mock(User.class);
        given(user.getId()).willReturn(200L);
        given(worker.getWorkspace()).willReturn(workspace);
        given(worker.getUser()).willReturn(user);

        WorkspaceShift futureShift = mock(WorkspaceShift.class);
        given(workspaceShiftQueryRepository.findFutureShiftsByAssignedWorker(any(), any(LocalDateTime.class)))
            .willReturn(List.of(futureShift));

        WorkspaceWorkerSchedule fixedSchedule = mock(WorkspaceWorkerSchedule.class);
        given(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(worker))
            .willReturn(List.of(fixedSchedule));

        SubstituteRequest requesterRequest = mock(SubstituteRequest.class);
        given(substituteRequestQueryRepository.findAllActiveByRequesterWorkerId(10L))
            .willReturn(List.of(requesterRequest));

        SubstituteRequest targetRequest = mock(SubstituteRequest.class);
        given(substituteRequestQueryRepository.findAllPendingTargetRequestsByTargetWorkerId(10L))
            .willReturn(List.of(targetRequest));

        // when
        workerResignationService.resign(worker);

        // then
        verify(futureShift, times(1)).unassignWorker();
        verify(fixedSchedule, times(1)).delete();
        verify(requesterRequest, times(1)).cancel();
        verify(targetRequest, times(1)).cancelPendingTargetAndCancelIfNoPendingTargets(10L);
        verify(worker, times(1)).resign();
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        ChatMembershipLeftEvent event = eventCaptor.getValue();
        assertThat(event.getWorkspaceId()).isEqualTo(100L);
        assertThat(event.getMemberId()).isEqualTo(200L);
        assertThat(event.getScope()).isEqualTo(TokenScope.APP);
    }

    @Test
    @DisplayName("정리 대상이 없어도 worker를 퇴직 처리한다")
    void resign_정리대상없음_퇴직처리() {
        // given
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        given(worker.getId()).willReturn(20L);
        Workspace workspace = mock(Workspace.class);
        given(workspace.getId()).willReturn(300L);
        User user = mock(User.class);
        given(user.getId()).willReturn(400L);
        given(worker.getWorkspace()).willReturn(workspace);
        given(worker.getUser()).willReturn(user);
        given(workspaceShiftQueryRepository.findFutureShiftsByAssignedWorker(any(), any(LocalDateTime.class)))
            .willReturn(List.of());
        given(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(worker)).willReturn(List.of());
        given(substituteRequestQueryRepository.findAllActiveByRequesterWorkerId(20L)).willReturn(List.of());
        given(substituteRequestQueryRepository.findAllPendingTargetRequestsByTargetWorkerId(20L)).willReturn(List.of());

        // when
        workerResignationService.resign(worker);

        // then
        verify(worker, times(1)).resign();
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        ChatMembershipLeftEvent event = eventCaptor.getValue();
        assertThat(event.getWorkspaceId()).isEqualTo(300L);
        assertThat(event.getMemberId()).isEqualTo(400L);
        assertThat(event.getScope()).isEqualTo(TokenScope.APP);
    }
}
