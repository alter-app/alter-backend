package com.dreamteam.alter.application.workspace.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.command.FixedWorkerScheduleCommand;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleRecurrenceType;

@ExtendWith(MockitoExtension.class)
class ManagerUpdateFixedWorkerScheduleTests {
    @Mock private WorkspaceQueryRepository workspaceQueryRepository;
    @Mock private WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;
    @InjectMocks private ManagerUpdateFixedWorkerSchedule useCase;

    @Test
    void legacyPatchPreservesMonthlyMetadataAndExcludesItself() {
        var schedule = schedule(WorkspaceWorkerScheduleRecurrenceType.MONTHLY, 2, "2026-12-31");
        when(schedule.getId()).thenReturn(9L);
        var actor = arrange(schedule, 1L);
        when(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(schedule.getWorkspaceWorker()))
            .thenReturn(List.of(schedule));

        useCase.execute(actor, 1L, 9L, command(null, null, null, null, "10:00"));
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(schedule.getRecurrenceType()).isEqualTo(WorkspaceWorkerScheduleRecurrenceType.MONTHLY);
        assertThat(schedule.getWeekOfMonth()).isEqualTo(2);
        assertThat(schedule.getStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(schedule.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void explicitPatchChangesRecurrenceAndClearsEndDate() {
        var schedule = schedule(WorkspaceWorkerScheduleRecurrenceType.MONTHLY, 2, "2026-12-31");
        var actor = arrange(schedule, 1L);
        when(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(schedule.getWorkspaceWorker())).thenReturn(List.of());
        useCase.execute(actor, 1L, 9L, command(WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-11-01", null, "09:00"));
        assertThat(schedule.getRecurrenceType()).isEqualTo(WorkspaceWorkerScheduleRecurrenceType.WEEKLY);
        assertThat(schedule.getWeekOfMonth()).isNull();
        assertThat(schedule.getStartDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(schedule.getEndDate()).isNull();
    }

    @Test
    void conflictingPatchDoesNotChangePersistentState() {
        var schedule = schedule(WorkspaceWorkerScheduleRecurrenceType.MONTHLY, 2, "2026-12-31");
        var actor = arrange(schedule, 1L);
        var existing = spy(WorkspaceWorkerSchedule.create(schedule.getWorkspaceWorker(),
            command(WorkspaceWorkerScheduleRecurrenceType.MONTHLY, 1, "2026-10-01", null, "09:00")));
        when(existing.getId()).thenReturn(8L);
        when(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(schedule.getWorkspaceWorker())).thenReturn(List.of(existing));
        assertThatThrownBy(() -> useCase.execute(actor, 1L, 9L,
            command(WorkspaceWorkerScheduleRecurrenceType.MONTHLY, 1, "2026-10-01", null, "10:00")))
            .isInstanceOf(CustomException.class)
            .satisfies(e -> assertThat(((CustomException)e).getErrorCode()).isEqualTo(ErrorCode.CONFLICT));
        assertThat(schedule.getWeekOfMonth()).isEqualTo(2);
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(schedule.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void invalidPeriodDoesNotChangePersistentState() {
        var schedule = schedule(WorkspaceWorkerScheduleRecurrenceType.MONTHLY, 2, "2026-12-31");
        var actor = arrange(schedule, 1L);
        assertThatThrownBy(() -> useCase.execute(actor, 1L, 9L,
            command(WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, "2026-11-01", "2026-10-01", "10:00")))
            .isInstanceOf(CustomException.class);
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(schedule.getWeekOfMonth()).isEqualTo(2);
        verify(workspaceWorkerScheduleQueryRepository, never()).getByWorkspaceWorker(any());
    }

    @Test
    void partialRecurrenceWithoutTypeIsRejected() {
        var schedule = schedule(WorkspaceWorkerScheduleRecurrenceType.MONTHLY, 2, null);
        var actor = arrange(schedule, 1L);
        assertThatThrownBy(() -> useCase.execute(actor, 1L, 9L, command(null, null, "2026-11-01", null, "09:00")))
            .isInstanceOf(CustomException.class);
        assertThat(schedule.getStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    @Test
    void anotherWorkspaceScheduleIsRejected() {
        var schedule = schedule(WorkspaceWorkerScheduleRecurrenceType.WEEKLY, null, null);
        var actor = arrange(schedule, 2L);
        assertThatThrownBy(() -> useCase.execute(actor, 1L, 9L, command(null, null, null, null, "10:00")))
            .isInstanceOf(CustomException.class);
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    }

    private ManagerActor arrange(WorkspaceWorkerSchedule schedule, Long workspaceId) {
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser manager = mock(ManagerUser.class);
        Workspace workspace = mock(Workspace.class);
        when(actor.getManagerUser()).thenReturn(manager);
        when(workspaceQueryRepository.existsByIdAndManagerUser(1L, manager)).thenReturn(true);
        when(workspaceWorkerScheduleQueryRepository.getByIdWithWorkspaceWorker(9L)).thenReturn(Optional.of(schedule));
        when(schedule.getWorkspaceWorker().getWorkspace()).thenReturn(workspace);
        when(workspace.getId()).thenReturn(workspaceId);
        return actor;
    }

    private WorkspaceWorkerSchedule schedule(WorkspaceWorkerScheduleRecurrenceType type, Integer nth, String end) {
        return spy(WorkspaceWorkerSchedule.create(mock(WorkspaceWorker.class), command(type, nth, "2026-10-01", end, "09:00")));
    }

    private FixedWorkerScheduleCommand command(WorkspaceWorkerScheduleRecurrenceType type, Integer nth,
        String start, String end, String time) {
        return new FixedWorkerScheduleCommand(DayOfWeek.TUESDAY, LocalTime.parse(time), DayOfWeek.TUESDAY,
            LocalTime.of(18, 0), type, nth, start == null ? null : LocalDate.parse(start), end == null ? null : LocalDate.parse(end));
    }
}
