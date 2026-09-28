package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.GetMyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyWorkspaceWorkSummaryDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMySchedule 테스트")
class GetMyScheduleTests {

    @Mock
    private WorkspaceShiftQueryRepository workspaceShiftQueryRepository;

    @InjectMocks
    private GetMySchedule getMySchedule;

    private User user;
    private AppActor actor;
    private Workspace cafe;
    private Workspace store;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        actor = AppActor.from(user, List.of());
        cafe = mockWorkspace(1L, "A카페");
        store = mockWorkspace(2L, "B편의점");
    }

    private Workspace mockWorkspace(Long id, String name) {
        Workspace workspace = mock(Workspace.class);
        lenient().when(workspace.getId()).thenReturn(id);
        lenient().when(workspace.getBusinessName()).thenReturn(name);
        return workspace;
    }

    private WorkspaceShift shift(Workspace workspace, int day, int hour, int minutes) {
        LocalDateTime start = LocalDateTime.of(2026, 9, day, hour, 0);
        return WorkspaceShift.create(workspace, start, start.plusMinutes(minutes), "홀", WorkspaceShiftStatus.CONFIRMED);
    }

    private List<WorkspaceShift> monthShifts() {
        return List.of(
            shift(cafe, 1, 9, 180),
            shift(store, 1, 18, 330),
            shift(cafe, 2, 9, 240)
        );
    }

    @Test
    @DisplayName("월 조회 시 업장별 근무시간·예상 수입을 내리고 합계는 업장별 금액의 합이다")
    void execute_월조회_업장별내역() {
        given(workspaceShiftQueryRepository.findByUserAndDateRange(user, 2026, 9)).willReturn(monthShifts());

        GetMyScheduleResponseDto result = getMySchedule.execute(actor, new WorkScheduleInquiryRequestDto(2026, 9, null));

        assertThat(result.getWorkspaceSummaries())
            .extracting(
                MyWorkspaceWorkSummaryDto::getWorkspaceId,
                MyWorkspaceWorkSummaryDto::getWorkspaceName,
                MyWorkspaceWorkSummaryDto::getTotalWorkHours,
                MyWorkspaceWorkSummaryDto::getEstimatedSalary
            )
            .containsExactly(
                tuple(1L, "A카페", 7.0, 72_240L),
                tuple(2L, "B편의점", 5.5, 56_760L)
            );
        assertThat(result.getTotalWorkHours()).isEqualTo(12.5);
        assertThat(result.getEstimatedSalary()).isEqualTo(72_240L + 56_760L);
        assertThat(result.getSchedules()).hasSize(3);
    }

    @Test
    @DisplayName("일 조회 시 목록은 그날 근무, 근무시간·예상 수입·업장별 내역은 해당 월 기준이다")
    void execute_일조회_월기준수입() {
        given(workspaceShiftQueryRepository.findByUserAndDate(user, 2026, 9, 2))
            .willReturn(List.of(shift(cafe, 2, 9, 240)));
        given(workspaceShiftQueryRepository.findByUserAndDateRange(user, 2026, 9)).willReturn(monthShifts());

        GetMyScheduleResponseDto result = getMySchedule.execute(actor, new WorkScheduleInquiryRequestDto(2026, 9, 2));

        assertThat(result.getSchedules()).hasSize(1);
        assertThat(result.getTotalWorkHours()).isEqualTo(12.5);
        assertThat(result.getEstimatedSalary()).isEqualTo(129_000L);
        assertThat(result.getWorkspaceSummaries()).hasSize(2);
    }

    @Test
    @DisplayName("주 조회 시 근무시간은 이번 주 기준이고 예상 수입·업장별 내역은 내리지 않는다")
    void execute_주조회_수입없음() {
        given(workspaceShiftQueryRepository.findByUserAndWeeklyRange(any(), any(), any()))
            .willReturn(List.of(shift(cafe, 1, 9, 180)));

        GetMyScheduleResponseDto result = getMySchedule.execute(actor, new WorkScheduleInquiryRequestDto(null, null, null));

        assertThat(result.getTotalWorkHours()).isEqualTo(3.0);
        assertThat(result.getEstimatedSalary()).isNull();
        assertThat(result.getWorkspaceSummaries()).isNull();
    }
}
