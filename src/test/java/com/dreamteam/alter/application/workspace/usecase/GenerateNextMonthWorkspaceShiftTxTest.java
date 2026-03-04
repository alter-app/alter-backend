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
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void 스케줄없음_생성없음() {
        Workspace workspace = createMockWorkspace(1L);

        GenerateNextMonthWorkspaceShiftTx.GenerationResult result = generateNextMonthWorkspaceShiftTx.execute(
            workspace,
            List.of(),
            YearMonth.of(2025, 2),
            Map.of()
        );

        verify(workspaceShiftRepository, never()).saveAll(anyList());
        assertThat(result.created()).isEqualTo(0);
        assertThat(result.skipped()).isEqualTo(0);
    }

    @Test
    @DisplayName("매주 월요일 09:00~18:00 고정 스케줄에 대해 다음 달 시프트를 정상 생성한다")
    void 정상스케줄_시프트생성() {
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule schedule = createMockSchedule(
            worker, DayOfWeek.MONDAY, LocalTime.of(9, 0), DayOfWeek.MONDAY, LocalTime.of(18, 0)
        );

        GenerateNextMonthWorkspaceShiftTx.GenerationResult result = generateNextMonthWorkspaceShiftTx.execute(
            workspace,
            List.of(schedule),
            YearMonth.of(2025, 2),
            Map.of()
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
    void 충돌시간대_건너뜀() {
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
            Map.of(worker.getId(), List.of(conflictingShift))
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
    void 야간근무_정상처리() {
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule schedule = createMockSchedule(
            worker, DayOfWeek.FRIDAY, LocalTime.of(22, 0), DayOfWeek.SATURDAY, LocalTime.of(6, 0)
        );

        GenerateNextMonthWorkspaceShiftTx.GenerationResult result = generateNextMonthWorkspaceShiftTx.execute(
            workspace,
            List.of(schedule),
            YearMonth.of(2025, 2),
            Map.of()
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

    private Workspace createMockWorkspace(Long id) {
        return mock(Workspace.class);
    }

    private WorkspaceWorker createMockWorker(Workspace workspace, Long id) {
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        when(worker.getId()).thenReturn(id);
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
