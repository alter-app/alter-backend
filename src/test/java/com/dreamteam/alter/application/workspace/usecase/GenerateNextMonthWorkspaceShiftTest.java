package com.dreamteam.alter.application.workspace.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorkerSchedule;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftRepository;
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
    private WorkspaceShiftRepository workspaceShiftRepository;

    @Mock
    private WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @InjectMocks
    private GenerateNextMonthWorkspaceShift generateNextMonthWorkspaceShift;

    private MockedStatic<LocalDate> mockedLocalDate;

    // 테스트 기준일: 2025년 1월 25일 (기본 생성일 25일에 해당)
    private static final LocalDate FIXED_TODAY = LocalDate.of(2025, 1, 25);

    @BeforeEach
    void setUp() {
        // CALLS_REAL_METHODS: 모든 static 메서드는 실제 구현을 사용하되, now()만 고정값을 반환한다
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
        // given
        when(workspaceQueryRepository.findAllByNextMonthShiftGenDay(25))
            .thenReturn(List.of());

        // when
        generateNextMonthWorkspaceShift.execute();

        // then
        verify(workspaceQueryRepository, times(1)).findAllByNextMonthShiftGenDay(25);
        verify(workspaceWorkerScheduleQueryRepository, never()).findAllActivatedWithWorkspaceWorkerByWorkspaceIds(anyList());
        verify(workspaceShiftRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("워크스페이스는 있지만 활성화된 고정 스케줄이 없으면 시프트를 생성하지 않는다")
    void 활성화된스케줄없음_시프트미생성() {
        // given
        Workspace workspace = createMockWorkspace(1L);
        when(workspaceQueryRepository.findAllByNextMonthShiftGenDay(25))
            .thenReturn(List.of(workspace));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L)))
            .thenReturn(List.of());

        // when
        generateNextMonthWorkspaceShift.execute();

        // then
        verify(workspaceShiftRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("매주 월요일 09:00~18:00 고정 스케줄에 대해 다음 달 시프트를 정상 생성한다")
    void 정상스케줄_시프트생성() {
        // given
        // 2025년 2월은 월요일이 4번: 3일, 10일, 17일, 24일
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule schedule = createMockSchedule(
            worker, DayOfWeek.MONDAY, LocalTime.of(9, 0), DayOfWeek.MONDAY, LocalTime.of(18, 0)
        );

        when(workspaceQueryRepository.findAllByNextMonthShiftGenDay(25))
            .thenReturn(List.of(workspace));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L)))
            .thenReturn(List.of(schedule));
        when(workspaceShiftQueryRepository.findConfirmedByWorkerIdsAndDateRange(anyList(), any(), any()))
            .thenReturn(List.of());

        // when
        generateNextMonthWorkspaceShift.execute();

        // then - 2월의 월요일 4주분 시프트가 생성되어야 한다
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkspaceShift>> captor = ArgumentCaptor.forClass(List.class);
        verify(workspaceShiftRepository, times(1)).saveAll(captor.capture());

        assertThat(captor.getValue()).hasSize(4);
    }

    @Test
    @DisplayName("이미 확정된 근무와 충돌하는 시간대는 건너뛴다")
    void 충돌시간대_건너뜀() {
        // given
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        WorkspaceWorkerSchedule schedule = createMockSchedule(
            worker, DayOfWeek.MONDAY, LocalTime.of(9, 0), DayOfWeek.MONDAY, LocalTime.of(18, 0)
        );

        // 첫 번째 월요일(2/3 09:00~18:00)과 겹치는 기존 확정 시프트 - 실제 엔티티로 생성해 Mockito 충돌 방지
        WorkspaceShift conflictingShift = WorkspaceShift.create(
            workspace,
            LocalDateTime.of(2025, 2, 3, 10, 0),
            LocalDateTime.of(2025, 2, 3, 12, 0),
            "기존 근무",
            WorkspaceShiftStatus.CONFIRMED
        );
        conflictingShift.assignWorker(worker);

        when(workspaceQueryRepository.findAllByNextMonthShiftGenDay(25))
            .thenReturn(List.of(workspace));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L)))
            .thenReturn(List.of(schedule));
        when(workspaceShiftQueryRepository.findConfirmedByWorkerIdsAndDateRange(anyList(), any(), any()))
            .thenReturn(List.of(conflictingShift));

        // when
        generateNextMonthWorkspaceShift.execute();

        // then - 충돌 1건 제외하고 3건만 생성
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkspaceShift>> captor = ArgumentCaptor.forClass(List.class);
        verify(workspaceShiftRepository, times(1)).saveAll(captor.capture());

        assertThat(captor.getValue()).hasSize(3);
    }

    @Test
    @DisplayName("야간 근무(금요일 22:00 ~ 토요일 06:00)가 정상적으로 처리된다")
    void 야간근무_정상처리() {
        // given
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker = createMockWorker(workspace, 10L);
        // 금요일 22:00 시작 → 토요일 06:00 종료 (야간 근무)
        WorkspaceWorkerSchedule schedule = createMockSchedule(
            worker, DayOfWeek.FRIDAY, LocalTime.of(22, 0), DayOfWeek.SATURDAY, LocalTime.of(6, 0)
        );

        when(workspaceQueryRepository.findAllByNextMonthShiftGenDay(25))
            .thenReturn(List.of(workspace));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L)))
            .thenReturn(List.of(schedule));
        when(workspaceShiftQueryRepository.findConfirmedByWorkerIdsAndDateRange(anyList(), any(), any()))
            .thenReturn(List.of());

        // when
        generateNextMonthWorkspaceShift.execute();

        // then - 2025년 2월 금요일: 7일, 14일, 21일, 28일 → 4건
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkspaceShift>> captor = ArgumentCaptor.forClass(List.class);
        verify(workspaceShiftRepository, times(1)).saveAll(captor.capture());

        List<WorkspaceShift> savedShifts = captor.getValue();
        assertThat(savedShifts).hasSize(4);

        // 첫 번째 시프트: 금요일 22:00 시작, 토요일 06:00 종료
        WorkspaceShift firstShift = savedShifts.getFirst();
        assertThat(firstShift.getStartDateTime()).isEqualTo(LocalDateTime.of(2025, 2, 7, 22, 0));
        assertThat(firstShift.getEndDateTime()).isEqualTo(LocalDateTime.of(2025, 2, 8, 6, 0));
    }

    @Test
    @DisplayName("여러 워크스페이스 중 하나가 실패해도 나머지는 정상 처리된다")
    void 일부워크스페이스실패_나머지정상처리() {
        // given
        Workspace workspace1 = createMockWorkspace(1L);
        Workspace workspace2 = createMockWorkspace(2L);
        WorkspaceWorker worker1 = createMockWorker(workspace1, 10L);
        WorkspaceWorker worker2 = createMockWorker(workspace2, 20L);

        // schedule1은 generateShiftsForWorkspace 내부에서 getStartDayOfWeek() 호출 시 예외 발생 (워크스페이스1 처리 실패 시뮬레이션)
        WorkspaceWorkerSchedule schedule1 = mock(WorkspaceWorkerSchedule.class);
        when(schedule1.getWorkspaceWorker()).thenReturn(worker1);
        when(schedule1.getStartDayOfWeek()).thenThrow(new RuntimeException("스케줄 처리 오류"));

        WorkspaceWorkerSchedule schedule2 = createMockSchedule(
            worker2, DayOfWeek.TUESDAY, LocalTime.of(10, 0), DayOfWeek.TUESDAY, LocalTime.of(19, 0)
        );

        when(workspaceQueryRepository.findAllByNextMonthShiftGenDay(25))
            .thenReturn(List.of(workspace1, workspace2));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L, 2L)))
            .thenReturn(List.of(schedule1, schedule2));
        when(workspaceShiftQueryRepository.findConfirmedByWorkerIdsAndDateRange(anyList(), any(), any()))
            .thenReturn(List.of());

        // when
        generateNextMonthWorkspaceShift.execute();

        // then - 워크스페이스2의 시프트만 저장되어야 한다
        verify(workspaceShiftRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("여러 근무자의 고정 스케줄이 한 워크스페이스에 존재할 때 모두 생성된다")
    void 복수근무자스케줄_모두생성() {
        // given
        Workspace workspace = createMockWorkspace(1L);
        WorkspaceWorker worker1 = createMockWorker(workspace, 10L);
        WorkspaceWorker worker2 = createMockWorker(workspace, 20L);

        WorkspaceWorkerSchedule schedule1 = createMockSchedule(
            worker1, DayOfWeek.MONDAY, LocalTime.of(9, 0), DayOfWeek.MONDAY, LocalTime.of(18, 0)
        );
        WorkspaceWorkerSchedule schedule2 = createMockSchedule(
            worker2, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), DayOfWeek.WEDNESDAY, LocalTime.of(19, 0)
        );

        when(workspaceQueryRepository.findAllByNextMonthShiftGenDay(25))
            .thenReturn(List.of(workspace));
        when(workspaceWorkerScheduleQueryRepository.findAllActivatedWithWorkspaceWorkerByWorkspaceIds(List.of(1L)))
            .thenReturn(List.of(schedule1, schedule2));
        when(workspaceShiftQueryRepository.findConfirmedByWorkerIdsAndDateRange(anyList(), any(), any()))
            .thenReturn(List.of());

        // when
        generateNextMonthWorkspaceShift.execute();

        // then - 2025년 2월: 월요일 4회 + 수요일 4회 = 8건
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkspaceShift>> captor = ArgumentCaptor.forClass(List.class);
        verify(workspaceShiftRepository, times(1)).saveAll(captor.capture());

        assertThat(captor.getValue()).hasSize(8);
    }

    // --- 헬퍼 메서드 ---

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
