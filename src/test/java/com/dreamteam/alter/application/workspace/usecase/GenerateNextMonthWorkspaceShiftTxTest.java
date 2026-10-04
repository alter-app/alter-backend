package com.dreamteam.alter.application.workspace.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateNextMonthWorkspaceShiftTx 테스트")
class GenerateNextMonthWorkspaceShiftTxTest {

    @Mock
    private WorkspaceShiftRepository workspaceShiftRepository;

    @InjectMocks
    private GenerateNextMonthWorkspaceShiftTx generateNextMonthWorkspaceShiftTx;

    @Test
    @DisplayName("스케줄이 비어있으면 저장하지 않고 0건 결과를 반환한다")
    void execute_noShiftsCreated_whenSchedulesEmpty() {
        Workspace workspace = createMockWorkspace(1L);

        GenerateNextMonthWorkspaceShiftTx.GenerationResult result = generateNextMonthWorkspaceShiftTx.execute(
            workspace,
            List.of(),
            YearMonth.of(2025, 2),
            new HashMap<>()
        );

        verify(workspaceShiftRepository, never()).saveAll(anyList());
        assertThat(result.created()).isEqualTo(0);
        assertThat(result.skipped()).isEqualTo(0);
    }

    @Test
    @DisplayName("매주 월요일 09:00~18:00 고정 스케줄에 대해 다음 달 시프트를 정상 생성한다")
    void execute_createsShifts_forFixedWeeklySchedule() {
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule schedule = createMockSchedule(
            worker, DayOfWeek.MONDAY, LocalTime.of(9, 0), DayOfWeek.MONDAY, LocalTime.of(18, 0)
        );

        GenerateNextMonthWorkspaceShiftTx.GenerationResult result = generateNextMonthWorkspaceShiftTx.execute(
            workspace,
            List.of(schedule),
            YearMonth.of(2025, 2),
            new HashMap<>()
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkspaceShift>> captor = ArgumentCaptor.forClass(List.class);
        verify(workspaceShiftRepository, times(1)).saveAll(captor.capture());

        assertThat(result.created()).isEqualTo(4);
        assertThat(result.skipped()).isEqualTo(0);
        assertThat(captor.getValue()).hasSize(4);
    }

    @Test
    @DisplayName("이미 확정된 근무와 충돌하는 시간대는 건너뛴다")
    void execute_skipsConflictingTimeSlots() {
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule schedule = createMockSchedule(
            worker, DayOfWeek.MONDAY, LocalTime.of(9, 0), DayOfWeek.MONDAY, LocalTime.of(18, 0)
        );

        WorkspaceShift conflictingShift = WorkspaceShift.create(
            workspace,
            LocalDateTime.of(2025, 2, 3, 10, 0),
            LocalDateTime.of(2025, 2, 3, 12, 0),
            "기존 근무",
            WorkspaceShiftStatus.CONFIRMED
        );
        conflictingShift.assignWorker(worker);

        GenerateNextMonthWorkspaceShiftTx.GenerationResult result = generateNextMonthWorkspaceShiftTx.execute(
            workspace,
            List.of(schedule),
            YearMonth.of(2025, 2),
            new HashMap<>(Map.of(worker.getUser().getId(), new ArrayList<>(List.of(conflictingShift))))
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkspaceShift>> captor = ArgumentCaptor.forClass(List.class);
        verify(workspaceShiftRepository, times(1)).saveAll(captor.capture());

        assertThat(result.created()).isEqualTo(3);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(captor.getValue()).hasSize(3);
    }

    @Test
    @DisplayName("야간 근무(금요일 22:00 ~ 토요일 06:00)가 정상적으로 처리된다")
    void execute_handlesOvernightShift() {
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule schedule = createMockSchedule(
            worker, DayOfWeek.FRIDAY, LocalTime.of(22, 0), DayOfWeek.SATURDAY, LocalTime.of(6, 0)
        );

        GenerateNextMonthWorkspaceShiftTx.GenerationResult result = generateNextMonthWorkspaceShiftTx.execute(
            workspace,
            List.of(schedule),
            YearMonth.of(2025, 2),
            new HashMap<>()
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkspaceShift>> captor = ArgumentCaptor.forClass(List.class);
        verify(workspaceShiftRepository, times(1)).saveAll(captor.capture());

        List<WorkspaceShift> savedShifts = captor.getValue();
        WorkspaceShift firstShift = savedShifts.getFirst();

        assertThat(result.created()).isEqualTo(4);
        assertThat(result.skipped()).isEqualTo(0);
        assertThat(savedShifts).hasSize(4);
        assertThat(firstShift.getStartDateTime()).isEqualTo(LocalDateTime.of(2025, 2, 7, 22, 0));
        assertThat(firstShift.getEndDateTime()).isEqualTo(LocalDateTime.of(2025, 2, 8, 6, 0));
    }

    @Test
    @DisplayName("생성한 근무는 결과로 돌려주고 전달받은 기존 근무 맵은 바꾸지 않는다")
    void execute_returnsCreatedShifts_withoutMutatingExistingMap() {
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule schedule = createMockSchedule(
            worker, DayOfWeek.MONDAY, LocalTime.of(9, 0), DayOfWeek.MONDAY, LocalTime.of(18, 0)
        );
        Map<Long, List<WorkspaceShift>> existing = Map.of();

        GenerateNextMonthWorkspaceShiftTx.GenerationResult result =
            generateNextMonthWorkspaceShiftTx.execute(workspace, List.of(schedule), YearMonth.of(2025, 2), existing);

        assertThat(result.createdShifts()).hasSize(4)
            .allSatisfy(shift -> assertThat(shift.getAssignedWorkspaceWorker()).isEqualTo(worker));
        assertThat(existing).isEmpty();
    }

    @Test
    @DisplayName("같은 업장 안에서 같은 사용자의 두 고정 스케줄이 겹치면 뒤 스케줄 회차를 건너뛴다")
    void execute_skipsConflict_betweenSchedulesOfSameUser_inSameCall() {
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule morning = createMockSchedule(
            worker, DayOfWeek.MONDAY, LocalTime.of(9, 0), DayOfWeek.MONDAY, LocalTime.of(18, 0)
        );
        WorkspaceWorkerSchedule afternoon = createMockSchedule(
            worker, DayOfWeek.MONDAY, LocalTime.of(13, 0), DayOfWeek.MONDAY, LocalTime.of(22, 0)
        );

        GenerateNextMonthWorkspaceShiftTx.GenerationResult result = generateNextMonthWorkspaceShiftTx.execute(
            workspace, List.of(morning, afternoon), YearMonth.of(2025, 2), new HashMap<>());

        assertThat(result.created()).isEqualTo(4);
        assertThat(result.skipped()).isEqualTo(4);
    }

    private Workspace createMockWorkspace(Long id) {
        return mock(Workspace.class);
    }

    private WorkspaceWorker createMockWorker(Workspace workspace, Long id) {
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(worker.getUser()).thenReturn(user);
        return worker;
    }

    private WorkspaceWorkerSchedule createMockSchedule(
        WorkspaceWorker worker,
        DayOfWeek startDayOfWeek,
        LocalTime startTime,
        DayOfWeek endDayOfWeek,
        LocalTime endTime
    ) {
        WorkspaceWorkerSchedule schedule = mock(WorkspaceWorkerSchedule.class);
        when(schedule.getWorkspaceWorker()).thenReturn(worker);
        when(schedule.getStartDayOfWeek()).thenReturn(startDayOfWeek);
        when(schedule.getStartTime()).thenReturn(startTime);
        when(schedule.getEndDayOfWeek()).thenReturn(endDayOfWeek);
        when(schedule.getEndTime()).thenReturn(endTime);
        return schedule;
    }
}
