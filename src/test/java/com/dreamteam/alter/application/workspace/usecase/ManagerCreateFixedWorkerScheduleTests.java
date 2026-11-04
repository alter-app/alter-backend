package com.dreamteam.alter.application.workspace.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dreamteam.alter.adapter.inbound.manager.workspace.dto.CreateWorkerScheduleRequestDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;
import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.domain.workspace.command.CreateFixedWorkerScheduleCommand;
import com.dreamteam.alter.domain.workspace.command.FixedWorkerScheduleCommand;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerScheduleRecurrenceType;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ManagerCreateFixedWorkerScheduleTests {
    @Mock private WorkspaceQueryRepository workspaceQueryRepository;
    @Mock private WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    @Mock private WorkspaceWorkerScheduleRepository workspaceWorkerScheduleRepository;
    @Mock private WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;
    @InjectMocks private ManagerCreateFixedWorkerSchedule useCase;

    @Test
    void execute_preservesMonthlyRecurrenceAndPeriod() throws Exception {
        ManagerActor actor = mock(ManagerActor.class);
        ManagerUser manager = mock(ManagerUser.class);
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        Workspace workspace = mock(Workspace.class);
        when(actor.getManagerUser()).thenReturn(manager);
        when(workspaceQueryRepository.existsByIdAndManagerUser(1L, manager)).thenReturn(true);
        when(workspaceWorkerQueryRepository.findById(10L)).thenReturn(Optional.of(worker));
        lenient().when(worker.getWorkspace()).thenReturn(workspace);
        lenient().when(workspace.getId()).thenReturn(1L);
        when(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(worker)).thenReturn(List.of());

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        CreateWorkerScheduleRequestDto request = mapper.readValue("""
            {"workspaceWorkerId":10,"schedules":[{
              "startDayOfWeek":"TUESDAY","startTime":"09:00:00",
              "endDayOfWeek":"TUESDAY","endTime":"18:00:00",
              "recurrenceType":"MONTHLY","weekOfMonth":2,
              "startDate":"2026-10-01","endDate":"2027-03-31"
            }]}
            """, CreateWorkerScheduleRequestDto.class);
        useCase.execute(actor, 1L, request.toCommand());

        ArgumentCaptor<List<WorkspaceWorkerSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(workspaceWorkerScheduleRepository).saveAll(captor.capture());
        var saved = mapper.valueToTree(captor.getValue().getFirst());
        assertThat(saved.path("recurrenceType").asText()).isEqualTo("MONTHLY");
        assertThat(saved.path("weekOfMonth").asInt()).isEqualTo(2);
        assertThat(captor.getValue().getFirst().getStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(captor.getValue().getFirst().getEndDate()).isEqualTo(LocalDate.of(2027, 3, 31));
    }

    @Test
    void legacyRequestCreatesIndefiniteWeeklySchedule() {
        var worker = mock(WorkspaceWorker.class);
        var actor = arrange(worker, 1L);
        when(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(worker)).thenReturn(List.of());
        var command = new FixedWorkerScheduleCommand(DayOfWeek.MONDAY, LocalTime.of(9, 0),
            DayOfWeek.MONDAY, LocalTime.of(18, 0), null, null, null, null);
        useCase.execute(actor, 1L, new CreateFixedWorkerScheduleCommand(10L, List.of(command)));
        ArgumentCaptor<List<WorkspaceWorkerSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(workspaceWorkerScheduleRepository).saveAll(captor.capture());
        var saved = captor.getValue().getFirst();
        assertThat(saved.getRecurrenceType()).isEqualTo(WorkspaceWorkerScheduleRecurrenceType.WEEKLY);
        assertThat(saved.getStartDate()).isNull();
        assertThat(saved.getEndDate()).isNull();
    }

    @Test
    void conflictsWithinSameRequestAreRejectedBeforeSave() {
        var worker = mock(WorkspaceWorker.class);
        var actor = arrange(worker, 1L);
        when(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(worker)).thenReturn(List.of());
        var command = monthly(2, "2026-10-01", null);
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
            useCase.execute(actor, 1L, new CreateFixedWorkerScheduleCommand(10L, List.of(command, command))))
            .isInstanceOf(CustomException.class);
        verify(workspaceWorkerScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void distinctMonthlyOccurrencesAreAcceptedTogether() {
        var worker = mock(WorkspaceWorker.class);
        var actor = arrange(worker, 1L);
        when(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(worker)).thenReturn(List.of());
        useCase.execute(actor, 1L, new CreateFixedWorkerScheduleCommand(10L, List.of(
            monthly(1, "2026-10-01", null), monthly(2, "2026-10-01", null)
        )));
        verify(workspaceWorkerScheduleRepository).saveAll(org.mockito.ArgumentMatchers.argThat(list -> list.size() == 2));
    }

    @Test
    void disjointPeriodsAreAccepted() {
        var worker = mock(WorkspaceWorker.class);
        var actor = arrange(worker, 1L);
        var existing = WorkspaceWorkerSchedule.create(worker, monthly(2, "2026-01-01", "2026-09-30"));
        when(workspaceWorkerScheduleQueryRepository.getByWorkspaceWorker(worker)).thenReturn(List.of(existing));
        useCase.execute(actor, 1L, new CreateFixedWorkerScheduleCommand(10L, List.of(monthly(2, "2026-10-01", null))));
        verify(workspaceWorkerScheduleRepository).saveAll(anyList());
    }

    @Test
    void anotherWorkspaceWorkerIsRejectedBeforeSave() {
        var worker = mock(WorkspaceWorker.class);
        var actor = arrange(worker, 2L);
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
            useCase.execute(actor, 1L, new CreateFixedWorkerScheduleCommand(10L, List.of(monthly(2, "2026-10-01", null)))))
            .isInstanceOf(CustomException.class);
        verify(workspaceWorkerScheduleRepository, never()).saveAll(anyList());
        verify(workspaceWorkerScheduleQueryRepository, never()).getByWorkspaceWorker(any());
    }

    private ManagerActor arrange(WorkspaceWorker worker, Long workspaceId) {
        var actor = mock(ManagerActor.class);
        var manager = mock(ManagerUser.class);
        var workspace = mock(Workspace.class);
        when(actor.getManagerUser()).thenReturn(manager);
        when(workspaceQueryRepository.existsByIdAndManagerUser(1L, manager)).thenReturn(true);
        when(workspaceWorkerQueryRepository.findById(10L)).thenReturn(Optional.of(worker));
        when(worker.getWorkspace()).thenReturn(workspace);
        when(workspace.getId()).thenReturn(workspaceId);
        return actor;
    }

    private FixedWorkerScheduleCommand monthly(int nth, String start, String end) {
        return new FixedWorkerScheduleCommand(DayOfWeek.TUESDAY, LocalTime.of(9, 0),
            DayOfWeek.TUESDAY, LocalTime.of(18, 0), WorkspaceWorkerScheduleRecurrenceType.MONTHLY, nth,
            LocalDate.parse(start), end == null ? null : LocalDate.parse(end));
    }
}
