package com.dreamteam.alter.application.workspace.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerScheduleQueryRepository;

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
    void 대상워크스페이스없음_조기종료() {
        when(workspaceQueryRepository.findAllForNextMonthShiftGeneration(25, false))
            .thenReturn(List.of());

        generateNextMonthWorkspaceShift.execute();

        verify(workspaceQueryRepository, times(1)).findAllForNextMonthShiftGeneration(25, false);
        verify(workspaceWorkerScheduleQueryRepository, never()).findAllActivatedWithWorkspaceWorkerByWorkspaceIds(anyList());
        verify(generateNextMonthWorkspaceShiftTx, never()).execute(any(), anyList(), any(), anyMap());
    }

    @Test
    @DisplayName("말일에는 말일 플래그로 대상 워크스페이스를 조회한다")
    void 말일조회_말일플래그적용() {
        mockedLocalDate.when(LocalDate::now).thenReturn(MONTH_END_TODAY);
        when(workspaceQueryRepository.findAllForNextMonthShiftGeneration(28, true))
            .thenReturn(List.of());

        generateNextMonthWorkspaceShift.execute();

        verify(workspaceQueryRepository, times(1)).findAllForNextMonthShiftGeneration(28, true);
    }

    @Test
    @DisplayName("활성화된 스케줄이 있는 워크스페이스는 TX 실행기로 위임한다")
    void 활성화된스케줄존재_tx실행위임() {
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule schedule = createMockSchedule(worker);

        when(workspaceQueryRepository.findAllForNextMonthShiftGeneration(25, false))
            .thenReturn(List.of(workspace));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L)))
            .thenReturn(List.of(schedule));
        when(workspaceShiftQueryRepository.findConfirmedByWorkerIdsAndDateRange(anyList(), any(), any()))
            .thenReturn(List.of());
        when(generateNextMonthWorkspaceShiftTx.execute(eq(workspace), eq(List.of(schedule)), eq(YearMonth.of(2025, 2)), anyMap()))
            .thenReturn(new GenerateNextMonthWorkspaceShiftTx.GenerationResult(4, 0));

        generateNextMonthWorkspaceShift.execute();

        verify(generateNextMonthWorkspaceShiftTx, times(1))
            .execute(eq(workspace), eq(List.of(schedule)), eq(YearMonth.of(2025, 2)), anyMap());
    }

    @Test
    @DisplayName("일부 워크스페이스 처리 실패가 발생해도 나머지 워크스페이스 처리는 계속된다")
    void 일부워크스페이스실패_나머지정상처리() {
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
        when(workspaceShiftQueryRepository.findConfirmedByWorkerIdsAndDateRange(anyList(), any(), any()))
            .thenReturn(List.of());
        when(generateNextMonthWorkspaceShiftTx.execute(eq(workspace1), eq(List.of(schedule1)), eq(YearMonth.of(2025, 2)), anyMap()))
            .thenThrow(new RuntimeException("워크스페이스1 실패"));
        when(generateNextMonthWorkspaceShiftTx.execute(eq(workspace2), eq(List.of(schedule2)), eq(YearMonth.of(2025, 2)), anyMap()))
            .thenReturn(new GenerateNextMonthWorkspaceShiftTx.GenerationResult(4, 0));

        generateNextMonthWorkspaceShift.execute();

        verify(generateNextMonthWorkspaceShiftTx, times(1))
            .execute(eq(workspace1), eq(List.of(schedule1)), eq(YearMonth.of(2025, 2)), anyMap());
        verify(generateNextMonthWorkspaceShiftTx, times(1))
            .execute(eq(workspace2), eq(List.of(schedule2)), eq(YearMonth.of(2025, 2)), anyMap());
    }

    @Test
    @DisplayName("활성화된 스케줄이 없는 워크스페이스는 TX 실행을 건너뛴다")
    void 활성화된스케줄없음_tx실행건너뜀() {
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
        when(worker.getWorkspace()).thenReturn(workspace);
        when(worker.getId()).thenReturn(id);
        return worker;
    }

    private WorkspaceWorkerSchedule createMockSchedule(WorkspaceWorker worker) {
        WorkspaceWorkerSchedule schedule = mock(WorkspaceWorkerSchedule.class);
        when(schedule.getWorkspaceWorker()).thenReturn(worker);
        return schedule;
    }
}
