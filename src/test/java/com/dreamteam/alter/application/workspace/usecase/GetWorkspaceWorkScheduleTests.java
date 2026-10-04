package com.dreamteam.alter.application.workspace.usecase;

import com.dreamteam.alter.adapter.inbound.general.schedule.dto.WorkScheduleInquiryRequestDto;
import com.dreamteam.alter.common.constants.WorkspaceConstants;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.workspace.entity.Workspace;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceWorker;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceShiftQueryRepository;
import com.dreamteam.alter.domain.workspace.port.outbound.WorkspaceWorkerQueryRepository;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWorkspaceWorkScheduleTests {
    @Mock WorkspaceQueryRepository workspaceQueryRepository;
    @Mock WorkspaceShiftQueryRepository workspaceShiftQueryRepository;
    @Mock WorkspaceWorkerQueryRepository workspaceWorkerQueryRepository;
    @InjectMocks GetWorkspaceWorkSchedule useCase;

    @Test
    void execute_queriesOnlyAuthenticatedUsersShiftsAndPreservesCurrentWorkerSalary() {
        User user = mock(User.class);
        Workspace workspace = mock(Workspace.class);
        WorkspaceWorker current = WorkspaceWorker.create(workspace, user);
        WorkspaceWorker previous = WorkspaceWorker.create(workspace, user);
        previous.resign();
        WorkspaceWorker other = WorkspaceWorker.create(workspace, mock(User.class));
        WorkspaceShift own = shift(current, 5);
        WorkspaceShift history = shift(previous, 4);
        WorkspaceShift others = shift(other, 6);
        when(workspaceQueryRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, user))
            .thenReturn(Optional.of(current));
        lenient().when(workspaceShiftQueryRepository.findByWorkspaceAndDateRange(workspace, 2026, 10))
            .thenReturn(List.of(history, own, others));
        lenient().when(workspaceShiftQueryRepository.findByUserAndWorkspaceAndMonthFrom(
            user, workspace, 2026, 10, null))
            .thenReturn(List.of(history, own));

        var result = useCase.execute(AppActor.from(user, List.of()), 1L,
            new WorkScheduleInquiryRequestDto(2026, 10, null));

        assertThat(result.getSchedules()).hasSize(2);
        assertThat(result.getSchedules()).extracting("startDateTime")
            .containsExactly(history.getStartDateTime(), own.getStartDateTime());
        assertThat(result.getTotalWorkHours()).isEqualTo(3.0);
        assertThat(result.getEstimatedSalary()).isEqualTo(3L * WorkspaceConstants.MINIMUM_HOURLY_WAGE);
        verify(workspaceShiftQueryRepository).findByUserAndWorkspaceAndMonthFrom(
            user, workspace, 2026, 10, null);
        verify(workspaceShiftQueryRepository, never()).findByWorkspaceAndDateRange(any(), anyInt(), anyInt());
    }

    @Test
    void execute_outOfRangeMonthPreservesEmptyResponse() {
        User user = mock(User.class);
        Workspace workspace = mock(Workspace.class);
        WorkspaceWorker current = WorkspaceWorker.create(workspace, user);
        when(workspaceQueryRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, user))
            .thenReturn(Optional.of(current));
        when(workspaceShiftQueryRepository.findByUserAndWorkspaceAndMonthFrom(user, workspace, 2026, 13, null))
            .thenReturn(List.of());

        var result = useCase.execute(AppActor.from(user, List.of()), 1L,
            new WorkScheduleInquiryRequestDto(2026, 13, null));

        assertThat(result.getSchedules()).isEmpty();
        assertThat(result.getTotalWorkHours()).isZero();
        assertThat(result.getEstimatedSalary()).isZero();
    }

    @Test
    void execute_nonMemberCannotReadShifts() {
        User user = mock(User.class);
        Workspace workspace = mock(Workspace.class);
        when(workspaceQueryRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceWorkerQueryRepository.findActiveWorkerByWorkspaceAndUser(workspace, user))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(AppActor.from(user, List.of()), 1L,
            new WorkScheduleInquiryRequestDto(2026, 10, null)))
            .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.WORKSPACE_NOT_FOUND);
        verifyNoInteractions(workspaceShiftQueryRepository);
    }

    private WorkspaceShift shift(WorkspaceWorker worker, int day) {
        LocalDateTime start = LocalDateTime.of(2026, 10, day, 9, 0);
        WorkspaceShift shift = WorkspaceShift.create(worker.getWorkspace(), start, start.plusHours(3), "홀",
            WorkspaceShiftStatus.PLANNED);
        shift.assignWorker(worker);
        return shift;
    }
}
