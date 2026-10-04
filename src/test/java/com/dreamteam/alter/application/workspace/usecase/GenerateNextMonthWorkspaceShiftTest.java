package com.dreamteam.alter.application.workspace.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateNextMonthWorkspaceShift 테스트")
class GenerateNextMonthWorkspaceShiftTest {

    @Mock
    private WorkspaceQueryRepository workspaceQueryRepository;

    @Mock
    private WorkspaceWorkerScheduleQueryRepository workspaceWorkerScheduleQueryRepository;

    @Mock
    private WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @Mock
    private GenerateNextMonthWorkspaceShiftTx generateNextMonthWorkspaceShiftTx;

    @InjectMocks
    private GenerateNextMonthWorkspaceShift generateNextMonthWorkspaceShift;

    private MockedStatic<LocalDate> mockedLocalDate;

    private static final LocalDate FIXED_TODAY = LocalDate.of(2025, 1, 25);
    private static final LocalDate MONTH_END_TODAY = LocalDate.of(2025, 2, 28);

    @BeforeEach
    void setUp() {
        mockedLocalDate = mockStatic(LocalDate.class, CALLS_REAL_METHODS);
        mockedLocalDate.when(LocalDate::now).thenReturn(FIXED_TODAY);
    }

    @AfterEach
    void tearDown() {
        mockedLocalDate.close();
    }

    @Test
    @DisplayName("대상 워크스페이스가 없으면 스케줄 조회 없이 종료한다")
    void execute_earlyReturn_whenNoTargetWorkspaces() {
        when(workspaceQueryRepository.findAllForNextMonthShiftGeneration(25, false))
            .thenReturn(List.of());

        generateNextMonthWorkspaceShift.execute();

        verify(workspaceQueryRepository, times(1)).findAllForNextMonthShiftGeneration(25, false);
        verify(workspaceWorkerScheduleQueryRepository, never()).findAllActivatedWithWorkspaceWorkerByWorkspaceIds(anyList());
        verify(generateNextMonthWorkspaceShiftTx, never()).execute(any(), anyList(), any(), anyMap());
    }

    @Test
    @DisplayName("말일에는 말일 플래그로 대상 워크스페이스를 조회한다")
    void execute_usesMonthEndFlag_whenLastDayOfMonth() {
        mockedLocalDate.when(LocalDate::now).thenReturn(MONTH_END_TODAY);
        when(workspaceQueryRepository.findAllForNextMonthShiftGeneration(28, true))
            .thenReturn(List.of());

        generateNextMonthWorkspaceShift.execute();

        verify(workspaceQueryRepository, times(1)).findAllForNextMonthShiftGeneration(28, true);
    }

    @Test
    @DisplayName("활성화된 스케줄이 있는 워크스페이스는 TX 실행기로 위임한다")
    void execute_delegatesToTx_whenActivatedScheduleExists() {
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule schedule = createMockSchedule(worker);

        when(workspaceQueryRepository.findAllForNextMonthShiftGeneration(25, false))
            .thenReturn(List.of(workspace));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L)))
            .thenReturn(List.of(schedule));
        when(workspaceShiftQueryRepository.findConfirmedByUserIdsAndDateRange(anyList(), any(), any()))
            .thenReturn(List.of());
        when(generateNextMonthWorkspaceShiftTx.execute(eq(workspace), eq(List.of(schedule)), eq(YearMonth.of(2025, 2)), anyMap()))
            .thenReturn(new GenerateNextMonthWorkspaceShiftTx.GenerationResult(List.of(), 0));

        generateNextMonthWorkspaceShift.execute();

        verify(generateNextMonthWorkspaceShiftTx, times(1))
            .execute(eq(workspace), eq(List.of(schedule)), eq(YearMonth.of(2025, 2)), anyMap());
    }

    @Test
    @DisplayName("일부 워크스페이스 처리 실패가 발생해도 나머지 워크스페이스 처리는 계속된다")
    void execute_continuesProcessing_whenSomeWorkspacesFail() {
        Workspace workspace1 = createMockWorkspace(1L);
        Workspace workspace2 = createMockWorkspace(2L);
        WorkspaceWorker worker1 = createMockWorker(workspace1, 10L);
        WorkspaceWorker worker2 = createMockWorker(workspace2, 20L);
        WorkspaceWorkerSchedule schedule1 = createMockSchedule(worker1);
        WorkspaceWorkerSchedule schedule2 = createMockSchedule(worker2);

        when(workspaceQueryRepository.findAllForNextMonthShiftGeneration(25, false))
            .thenReturn(List.of(workspace1, workspace2));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L, 2L)))
            .thenReturn(List.of(schedule1, schedule2));
        when(workspaceShiftQueryRepository.findConfirmedByUserIdsAndDateRange(anyList(), any(), any()))
            .thenReturn(List.of());
        when(generateNextMonthWorkspaceShiftTx.execute(eq(workspace1), eq(List.of(schedule1)), eq(YearMonth.of(2025, 2)), anyMap()))
            .thenThrow(new RuntimeException("워크스페이스1 실패"));
        when(generateNextMonthWorkspaceShiftTx.execute(eq(workspace2), eq(List.of(schedule2)), eq(YearMonth.of(2025, 2)), anyMap()))
            .thenReturn(new GenerateNextMonthWorkspaceShiftTx.GenerationResult(List.of(), 0));

        generateNextMonthWorkspaceShift.execute();

        verify(generateNextMonthWorkspaceShiftTx, times(1))
            .execute(eq(workspace1), eq(List.of(schedule1)), eq(YearMonth.of(2025, 2)), anyMap());
        verify(generateNextMonthWorkspaceShiftTx, times(1))
            .execute(eq(workspace2), eq(List.of(schedule2)), eq(YearMonth.of(2025, 2)), anyMap());
    }

    @Test
    @DisplayName("기존 근무는 사용자 id로 묶이고, 앞 업장에서 생성한 근무는 뒤 업장 호출 전에 그 맵에 합쳐진다")
    void execute_mergesCreatedShifts_beforeNextWorkspace() {
        Workspace workspace1 = createMockWorkspace(1L);
        Workspace workspace2 = createMockWorkspace(2L);
        WorkspaceWorker worker1 = createMockWorker(workspace1, 10L);
        WorkspaceWorker worker2 = createMockWorker(workspace2, 10L);
        WorkspaceWorkerSchedule schedule1 = createMockSchedule(worker1);
        WorkspaceWorkerSchedule schedule2 = createMockSchedule(worker2);
        WorkspaceShift existingElsewhere = WorkspaceShift.create(
            mock(Workspace.class), LocalDateTime.of(2025, 2, 4, 9, 0), LocalDateTime.of(2025, 2, 4, 18, 0), "홀", WorkspaceShiftStatus.CONFIRMED);
        existingElsewhere.assignWorker(worker2);
        WorkspaceShift created = WorkspaceShift.create(
            workspace1, LocalDateTime.of(2025, 2, 3, 9, 0), LocalDateTime.of(2025, 2, 3, 18, 0), "고정 근무", WorkspaceShiftStatus.CONFIRMED);
        created.assignWorker(worker1);

        when(workspaceQueryRepository.findAllForNextMonthShiftGeneration(25, false))
            .thenReturn(List.of(workspace1, workspace2));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L, 2L)))
            .thenReturn(List.of(schedule1, schedule2));
        when(workspaceShiftQueryRepository.findConfirmedByUserIdsAndDateRange(eq(List.of(10L)), any(), any()))
            .thenReturn(List.of(existingElsewhere));
        when(generateNextMonthWorkspaceShiftTx.execute(eq(workspace1), eq(List.of(schedule1)), eq(YearMonth.of(2025, 2)), anyMap()))
            .thenReturn(new GenerateNextMonthWorkspaceShiftTx.GenerationResult(List.of(created), 0));
        Map<Long, List<WorkspaceShift>> seenByWorkspace2 = new HashMap<>();
        doAnswer(invocation -> {
            // 호출 시점의 내용을 복사해 둔다. 참조만 담으면 호출 뒤에 합쳐진 근무도 보이게 된다.
            invocation.<Map<Long, List<WorkspaceShift>>>getArgument(3)
                .forEach((userId, shifts) -> seenByWorkspace2.put(userId, List.copyOf(shifts)));
            return new GenerateNextMonthWorkspaceShiftTx.GenerationResult(List.of(), 0);
        }).when(generateNextMonthWorkspaceShiftTx)
            .execute(eq(workspace2), eq(List.of(schedule2)), eq(YearMonth.of(2025, 2)), anyMap());

        generateNextMonthWorkspaceShift.execute();

        assertThat(seenByWorkspace2.get(10L)).containsExactlyInAnyOrder(existingElsewhere, created);
    }

    @Test
    @DisplayName("활성화된 스케줄이 없는 워크스페이스는 TX 실행을 건너뛴다")
    void execute_skipsTx_whenNoActivatedSchedules() {
        Workspace workspace = createMockWorkspace(1L);

        when(workspaceQueryRepository.findAllForNextMonthShiftGeneration(25, false))
            .thenReturn(List.of(workspace));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L)))
            .thenReturn(List.of());

        generateNextMonthWorkspaceShift.execute();

        verify(generateNextMonthWorkspaceShiftTx, never()).execute(any(), anyList(), any(), anyMap());
    }

    private Workspace createMockWorkspace(Long id) {
        Workspace workspace = mock(Workspace.class);
        when(workspace.getId()).thenReturn(id);
        return workspace;
    }

    private WorkspaceWorker createMockWorker(Workspace workspace, Long id) {
        WorkspaceWorker worker = mock(WorkspaceWorker.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(worker.getWorkspace()).thenReturn(workspace);
        when(worker.getUser()).thenReturn(user);
        return worker;
    }

    private WorkspaceWorkerSchedule createMockSchedule(WorkspaceWorker worker) {
        WorkspaceWorkerSchedule schedule = mock(WorkspaceWorkerSchedule.class);
        when(schedule.getWorkspaceWorker()).thenReturn(worker);
        return schedule;
    }
}
