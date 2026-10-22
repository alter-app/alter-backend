package com.dreamteam.alter.application.workspace.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.workspace.command.FixedWorkerScheduleCommand;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleRecurrenceType;

@ExtendWith(MockitoExtension.class)
class ManagerGetFixedWorkerScheduleListTests {
    @Mock private WorkspaceQueryRepository workspaceQueryRepository;
    @Mock private WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;
    @InjectMocks private ManagerGetFixedWorkerScheduleList useCase;

    @Test
    void returnsRecurrenceAndPeriodAsDomainResult() {
        var actor = mock(ManagerActor.class);
        when(workspaceQueryRepository.existsByIdAndManagerUser(1L, actor.getManagerUser())).thenReturn(true);
        var worker = mock(WorkspaceWorker.class);
        when(worker.getId()).thenReturn(10L);
        var schedule = WorkspaceWorkerSchedule.create(worker, new FixedWorkerScheduleCommand(
            DayOfWeek.TUESDAY, LocalTime.of(9, 0), DayOfWeek.TUESDAY, LocalTime.of(18, 0),
            WorkspaceWorkerScheduleRecurrenceType.MONTHLY, 2, LocalDate.of(2026, 10, 1), null
        ));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L)))
            .thenReturn(List.of(schedule));
        assertThat(useCase.execute(actor, 1L)).singleElement().satisfies(result -> {
            assertThat(result.workspaceWorkerId()).isEqualTo(10L);
            assertThat(result.recurrenceType()).isEqualTo(WorkspaceWorkerScheduleRecurrenceType.MONTHLY);
            assertThat(result.weekOfMonth()).isEqualTo(2);
            assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 10, 1));
            assertThat(result.endDate()).isNull();
        });
    }
}
