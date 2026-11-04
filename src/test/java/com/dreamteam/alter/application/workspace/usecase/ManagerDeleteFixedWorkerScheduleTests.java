package com.dreamteam.alter.application.workspace.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleStatus;

@ExtendWith(MockitoExtension.class)
class ManagerDeleteFixedWorkerScheduleTests {
    @Mock private WorkspaceQueryRepository workspaceQueryRepository;
    @Mock private WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;
    @InjectMocks private ManagerDeleteFixedWorkerSchedule useCase;

    @Test
    void deletesOnlyTheRule() {
        var schedule = schedule();
        var actor = arrange(schedule, 1L);
        useCase.execute(actor, 1L, 9L);
        assertThat(schedule.getStatus()).isEqualTo(WorkspaceWorkerScheduleStatus.DELETED);
    }

    @Test
    void rejectsAnotherWorkspaceRuleWithoutDeletingIt() {
        var schedule = schedule();
        var actor = arrange(schedule, 2L);
        assertThatThrownBy(() -> useCase.execute(actor, 1L, 9L)).isInstanceOf(CustomException.class);
        assertThat(schedule.getStatus()).isEqualTo(WorkspaceWorkerScheduleStatus.ACTIVATED);
    }

    private WorkspaceWorkerSchedule schedule() {
        return WorkspaceWorkerSchedule.create(mock(WorkspaceWorker.class),
            DayOfWeek.MONDAY, LocalTime.of(9, 0), DayOfWeek.MONDAY, LocalTime.of(18, 0));
    }

    private ManagerActor arrange(WorkspaceWorkerSchedule schedule, Long workspaceId) {
        var actor = mock(ManagerActor.class);
        var workspace = mock(Workspace.class);
        when(workspaceQueryRepository.existsByIdAndManagerUser(1L, actor.getManagerUser())).thenReturn(true);
        when(workspaceWorkerScheduleQueryRepository.findById(9L)).thenReturn(Optional.of(schedule));
        when(schedule.getWorkspaceWorker().getWorkspace()).thenReturn(workspace);
        when(workspace.getId()).thenReturn(workspaceId);
        return actor;
    }
}
