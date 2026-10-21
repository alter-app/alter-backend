package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.GetMyScheduleResponseDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.MyWorkspaceWorkSummaryDto;
import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @Test
    @DisplayName("월 조회 시 업장별 근무시간·예상 수입을 업장 ID 오름차순으로 내리고 합계는 업장별 금액의 합이다")
    void execute_월조회_업장별내역() {
        // B편의점(id 2)의 근무가 그 달 가장 먼저 있어도 A카페(id 1)가 앞에 온다
        given(workspaceShiftQueryRepository.findByUserAndDateRange(user, 2026, 9)).willReturn(List.of(
            shift(store, 1, 8, 330),
            shift(cafe, 1, 14, 180),
            shift(cafe, 2, 9, 240)
        ));

        GetMyScheduleResponseDto result = getMySchedule.execute(actor, new WorkScheduleInquiryRequestDto(2026, 9, null));

        assertThat(result.getWorkspaceSummaries())
            .extracting(
                summary -> summary.getWorkspace().getWorkspaceId(),
                summary -> summary.getWorkspace().getWorkspaceName(),
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
    @DisplayName("월 조회 시 같은 업장이 다른 객체로 와도 업장 ID로 한 줄에 묶는다")
    void execute_월조회_업장ID로_묶음() {
        Workspace sameCafe = mockWorkspace(1L, "A카페");
        given(workspaceShiftQueryRepository.findByUserAndDateRange(user, 2026, 9)).willReturn(List.of(
            shift(cafe, 1, 9, 180),
            shift(sameCafe, 2, 9, 240)
        ));

        GetMyScheduleResponseDto result = getMySchedule.execute(actor, new WorkScheduleInquiryRequestDto(2026, 9, null));

        assertThat(result.getWorkspaceSummaries())
            .extracting(summary -> summary.getWorkspace().getWorkspaceId(), MyWorkspaceWorkSummaryDto::getTotalWorkHours)
            .containsExactly(tuple(1L, 7.0));
    }

    @Test
    @DisplayName("월 조회 시 근무가 없으면 예상 수입 0, 업장별 내역은 빈 목록이다")
    void execute_월조회_근무없음() {
        given(workspaceShiftQueryRepository.findByUserAndDateRange(user, 2026, 9)).willReturn(List.of());

        GetMyScheduleResponseDto result = getMySchedule.execute(actor, new WorkScheduleInquiryRequestDto(2026, 9, null));

        assertThat(result.getTotalWorkHours()).isZero();
        assertThat(result.getEstimatedSalary()).isZero();
        assertThat(result.getWorkspaceSummaries()).isEmpty();
    }

    @Test
    @DisplayName("일 조회 시 목록과 근무시간은 그날 기준이고 예상 수입·업장별 내역은 내리지 않는다")
    void execute_일조회_그날기준() {
        given(workspaceShiftQueryRepository.findByUserAndDate(user, LocalDate.of(2026, 9, 2)))
            .willReturn(List.of(shift(cafe, 2, 9, 240)));

        GetMyScheduleResponseDto result = getMySchedule.execute(actor, new WorkScheduleInquiryRequestDto(2026, 9, 2));

        assertThat(result.getSchedules()).hasSize(1);
        assertThat(result.getTotalWorkHours()).isEqualTo(4.0);
        assertThat(result.getEstimatedSalary()).isNull();
        assertThat(result.getWorkspaceSummaries()).isNull();
    }

    @ParameterizedTest
    @CsvSource({"2026, 9, 31", "2026, 13, 1", "2026, 2, 29"})
    @DisplayName("일 조회 시 존재하지 않는 날짜는 ILLEGAL_ARGUMENT")
    void execute_일조회_없는날짜(int year, int month, int day) {
        assertThatThrownBy(() -> getMySchedule.execute(actor, new WorkScheduleInquiryRequestDto(year, month, day)))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_ARGUMENT);
        verifyNoInteractions(workspaceShiftQueryRepository);
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
